package com.adventist.adventist.api.mappers

import com.adventist.adventist.api.dtos.AuthenticatedUserDto
import com.adventist.adventist.api.dtos.UserDto
import com.adventist.adventist.domain.model.AuthenticatedUser
import com.adventist.adventist.domain.model.User

fun AuthenticatedUser.toDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = this.user.toDto(),
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = this.id,
        email = this.email,
        username = this.username,
        hasEmailVerified = this.hasEmailVerified
    )
}