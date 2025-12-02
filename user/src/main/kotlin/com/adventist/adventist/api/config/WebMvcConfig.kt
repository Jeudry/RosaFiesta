package com.adventist.adventist.api.config

import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class WebMvcConfig(
    private val ipRateLimitInterceptor: IpRateLimitInterceptor
): WebMvcConfigurer {
    override fun addInterceptors(registry: org.springframework.web.servlet.config.annotation.InterceptorRegistry) {
        registry.addInterceptor(ipRateLimitInterceptor)
            .addPathPatterns("/api/**")
    }
}