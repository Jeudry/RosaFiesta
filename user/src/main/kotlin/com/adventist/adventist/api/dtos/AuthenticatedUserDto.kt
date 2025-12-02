package com.adventist.adventist.api.dtos

import com.adventist.adventist.domain.model.User

data class AuthenticatedUserDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)
