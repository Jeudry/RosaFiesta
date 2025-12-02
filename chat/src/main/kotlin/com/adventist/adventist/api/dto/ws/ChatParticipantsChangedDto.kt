package com.adventist.adventist.api.dto.ws

import com.adventist.adventist.domain.types.ChatId

data class ChatParticipantsChangedDto(
  val chatId: ChatId
)