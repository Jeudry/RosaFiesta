package com.adventist.adventist.domain.events

import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)
