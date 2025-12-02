package com.adventist.adventist.domain.exception

class RateLimitEx(
    val resetsInSeconds: Long
): RuntimeException("Rate limit exceeded. Try again in $resetsInSeconds seconds")