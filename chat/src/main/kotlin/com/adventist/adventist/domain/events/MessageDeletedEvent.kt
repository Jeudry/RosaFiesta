package com.adventist.adventist.domain.events

import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId
){

}