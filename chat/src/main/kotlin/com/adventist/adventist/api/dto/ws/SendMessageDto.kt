package com.adventist.adventist.api.dto.ws

import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.ChatMessageId

data class SendMessageDto(
  val messageId: ChatMessageId? = null,
  val content: String,
  val chatId: ChatId? = null
)
