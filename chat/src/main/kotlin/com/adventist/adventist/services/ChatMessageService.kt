package com.adventist.adventist.services

import com.adventist.adventist.domain.events.MessageDeletedEvent
import com.adventist.adventist.domain.events.chat.ChatEvent
import com.adventist.adventist.domain.exceptions.ChatMessageNotFoundEx
import com.adventist.adventist.domain.exceptions.ChatNotFoundEx
import com.adventist.adventist.domain.exceptions.ChatParticipantNotFoundEx
import com.adventist.adventist.domain.exceptions.ForbiddenEx
import com.adventist.adventist.domain.models.ChatMessage
import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.ChatMessageId
import com.adventist.adventist.domain.types.UserId
import com.adventist.adventist.infra.database.entities.ChatMessageEntity
import com.adventist.adventist.infra.database.mappers.toModel
import com.adventist.adventist.infra.database.repositories.ChatMessageRepository
import com.adventist.adventist.infra.database.repositories.ChatParticipantRepository
import com.adventist.adventist.infra.database.repositories.ChatRepository
import com.adventist.adventist.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher,
    private val messageCacheEvictionHelper: MessageCacheEvictionHelper
) {
    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId",
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null,
    ): ChatMessage{
        val chat = chatRepository.findChatById(
            chatId, senderId
        ) ?: throw ChatNotFoundEx()

        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundEx(senderId)

        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                id = messageId ?: UUID.randomUUID(),
                chat = chat,
                sender = sender,
                content = content,
                chatId = chatId,
            )
        )

        eventPublisher.publish(
            ChatEvent.NewMessage(
                senderId = sender.userId!!,
                senderUsername = sender.username,
                recipientIds = chat.participants.map {
                    it.userId!!
                }.toSet(),
                chatId = chatId,
                message = savedMessage.content,
            )
        )

        return savedMessage.toModel()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        deleterId: UserId,
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw ChatMessageNotFoundEx(messageId)

        if(message.sender!!.userId != deleterId){
            throw ForbiddenEx()
        }

        chatMessageRepository.deleteById(messageId)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId!!,
                messageId = messageId
            )
        )

        messageCacheEvictionHelper.evictMessagesCache(message.chatId!!)
    }
}