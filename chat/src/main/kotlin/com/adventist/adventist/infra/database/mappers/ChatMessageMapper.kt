package com.adventist.adventist.infra.database.mappers

import com.adventist.adventist.domain.models.ChatMessage
import com.adventist.adventist.infra.database.entities.ChatMessageEntity

fun ChatMessageEntity.toModel(): ChatMessage {
    return ChatMessage(
        id = id!!,
        chatId = chatId!!,
        sender = sender!!.toModel(),
        content = content,
        createdAt = createdAt
    )
}