package com.adventist.adventist.api.dto

import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.ChatMessageId
import com.adventist.adventist.domain.types.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val content: String,
    val createdAt: Instant,
    val senderId: UserId
)
