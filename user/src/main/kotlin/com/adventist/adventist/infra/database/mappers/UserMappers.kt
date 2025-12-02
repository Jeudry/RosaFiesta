package com.adventist.adventist.infra.database.mappers

import com.adventist.adventist.domain.model.User
import com.adventist.adventist.infra.database.entities.UserEntity

fun UserEntity.toModel(): User {
    return User(
        id = id!!,
        email = email,
        username = username,
        hasEmailVerified = hasVerifiedEmail
    )
}
