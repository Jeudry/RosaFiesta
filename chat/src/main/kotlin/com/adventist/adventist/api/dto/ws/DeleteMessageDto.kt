package com.adventist.adventist.api.dto.ws

import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.ChatMessageId

data class DeleteMessageDto(
  val chatId: ChatId,
  val messageId: ChatMessageId
)