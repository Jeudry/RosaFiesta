package com.adventist.adventist.domain.model

import com.adventist.adventist.domain.types.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean,
)
