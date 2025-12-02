package com.adventist.adventist.infra.database.mappers

import com.adventist.adventist.domain.models.ChatParticipant
import com.adventist.adventist.infra.database.entities.ChatParticipantEntity

fun ChatParticipantEntity.toModel(): ChatParticipant {
    return ChatParticipant(
        userId = userId!!,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatParticipant.toEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}