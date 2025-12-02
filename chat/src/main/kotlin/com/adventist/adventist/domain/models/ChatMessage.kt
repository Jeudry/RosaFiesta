package com.adventist.adventist.domain.models

import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.ChatMessageId
import java.time.Instant

data class ChatMessage(
    val id: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val content: String,
    val createdAt: Instant
)