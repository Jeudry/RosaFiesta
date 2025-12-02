package com.adventist.adventist.api.mappers

import com.adventist.adventist.api.dto.ChatDto
import com.adventist.adventist.domain.models.Chat

fun Chat.toDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map {
            it.toDto()
        },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toDto(),
        creator = creator.toDto(),
    )
}