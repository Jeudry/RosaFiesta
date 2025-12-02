package com.adventist.adventist.api.dtos

import com.adventist.adventist.domain.types.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasEmailVerified: Boolean,
)
