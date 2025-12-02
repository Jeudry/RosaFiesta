package com.adventist.adventist.domain.models

import com.adventist.adventist.domain.types.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String? = null,
)
