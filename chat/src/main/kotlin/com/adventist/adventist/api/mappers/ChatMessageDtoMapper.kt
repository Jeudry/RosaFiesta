package com.adventist.adventist.api.mappers

import com.adventist.adventist.api.dto.ChatMessageDto
import com.adventist.adventist.domain.models.ChatMessage

fun ChatMessage.toDto(): ChatMessageDto {
    return ChatMessageDto(
        id = sender.userId,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}