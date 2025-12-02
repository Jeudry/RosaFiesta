package com.adventist.adventist.api.dto

import com.adventist.adventist.domain.types.UserId

data class ChatParticipantDto(
    val id: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String? = null,
)
