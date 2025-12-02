package com.adventist.adventist.domain.events

import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.UserId

data class ChatCreatedEvent(
  val chatId: ChatId,
  val participantIds: List<UserId>,
)