package com.adventist.adventist.infra.rate_limiting

import com.adventist.adventist.infra.configs.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {
    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map { IpAddressMatcher(it) }

        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers: List<IpAddressMatcher> = nginxConfig.trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy
                proxy.contains(":") -> "$proxy/128"
                else -> "$proxy/32"
            }
            IpAddressMatcher(cidr)
        }

    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr
        if(!isTrustedIp(remoteAddr)){
            if(nginxConfig.requireProxy){
                logger.warn("Direct connection attempt from $remoteAddr")
                throw SecurityException("Not valid client IP in proxy headers, $remoteAddr")
            }

            return remoteAddr
        }

        val clientIp = extractFromXRealIp( request, remoteAddr )
        if(clientIp == null){
            logger.warn("No valid client IP found in proxy headers")
            if(nginxConfig.requireProxy){
                throw SecurityException("No valid client IP found in proxy headers")
            }
            return remoteAddr
        }

        return clientIp
    }

    private fun extractFromXRealIp(request: HttpServletRequest, proxyIp: String): String? {
        return request.getHeader("X-Real-IP")?.let { header ->
            validateAndNormalizeIp(header, "X-Real-IP", proxyIp)
        }
    }

    private fun validateAndNormalizeIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()
        if(trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)){
            logger.debug("Invalid IP address in $headerName $ip from proxy $proxyIp")
            return null
        }
        return try {
            val inetAddress = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> Inet4Address.getByName(trimmedIp)
                else -> {
                    logger.warn("Malformed IP address in $headerName: $ip from proxy $proxyIp")
                    return null
                }
            }

            if(isPrivateIp(inetAddress.hostAddress)){
                logger.debug("Private IP address in $headerName: $ip from proxy $proxyIp")
                null
            }

            inetAddress.hostAddress
        } catch (e: Exception){
            logger.warn("Malformed IP address in $headerName: $ip from proxy $proxyIp")
            null
        }
    }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { it.matches(ip) }
    }

    private fun isTrustedIp(ip: String): Boolean {
        return trustedMatchers.any { it.matches(ip) }
    }
}