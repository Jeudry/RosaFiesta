package com.adventist.adventist.api.mappers

import com.adventist.adventist.api.dto.ChatParticipantDto
import com.adventist.adventist.domain.models.ChatParticipant

fun ChatParticipant.toDto(): ChatParticipantDto {
    return ChatParticipantDto(
        id = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}