package com.adventist.adventist.domain.events.chat

import com.adventist.adventist.domain.events.AdventistEvent
import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.UserId
import java.time.Instant
import java.util.*

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now()
): AdventistEvent {
    data class NewMessage(
        val senderId: UserId,
        val senderUsername: String,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE
    ): ChatEvent(), AdventistEvent


}