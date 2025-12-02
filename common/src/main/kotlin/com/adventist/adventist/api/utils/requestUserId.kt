package com.adventist.adventist.api.utils

import com.adventist.adventist.domain.exceptions.UnauthorizedEx
import com.adventist.adventist.domain.types.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedEx()