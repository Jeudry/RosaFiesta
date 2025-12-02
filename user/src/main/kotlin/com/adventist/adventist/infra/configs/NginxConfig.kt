package com.adventist.adventist.infra.configs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "nginx")
data class NginxConfig(
    var trustedIps: List<String> = emptyList(),
    var requireProxy: Boolean = true
)
