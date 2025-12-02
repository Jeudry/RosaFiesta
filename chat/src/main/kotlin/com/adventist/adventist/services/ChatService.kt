package com.adventist.adventist.services

import com.adventist.adventist.api.dto.ChatMessageDto
import com.adventist.adventist.api.mappers.toDto
import com.adventist.adventist.domain.events.ChatCreatedEvent
import com.adventist.adventist.domain.events.ChatParticipantLeftEvent
import com.adventist.adventist.domain.events.ChatParticipantsJoinedEvent
import com.adventist.adventist.domain.exceptions.*
import com.adventist.adventist.domain.models.Chat
import com.adventist.adventist.domain.models.ChatMessage
import com.adventist.adventist.domain.types.ChatId
import com.adventist.adventist.domain.types.UserId
import com.adventist.adventist.infra.database.entities.ChatEntity
import com.adventist.adventist.infra.database.mappers.toModel
import com.adventist.adventist.infra.database.repositories.ChatMessageRepository
import com.adventist.adventist.infra.database.repositories.ChatParticipantRepository
import com.adventist.adventist.infra.database.repositories.ChatRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository.findByChatIdBefore(
            chatId = chatId,
            before = before,
            pageable = PageRequest.of(0, pageSize)
        ).content
            .asReversed()
            .map {
                it.toModel().toDto()
            }
    }


    fun getChatById(chatId: ChatId, userId: UserId): Chat?{
        return chatRepository.findChatById(chatId, userId)
            ?.toModel(lastMessageForChat(chatId))
    }

    fun findChatsByUser(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)
        val chatIds = chatEntities.mapNotNull { it.id }
        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds.toSet())
            .associateBy { it.chatId }

        return chatEntities
            .map {
                it.toModel(
                    lastMessage = latestMessages[it.id]?.toModel()
                )
            }.sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUsersId: Set<UserId>
    ): Chat {
        if (otherUsersId.contains(creatorId)) {
            throw SelfInvitationNotAllowedEx(creatorId)
        }

        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            otherUsersId.toList()
        )

        val allParticipants = (otherParticipants + creatorId)
        if(allParticipants.size < 2){
            throw InvalidChatSizeEx()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundEx(creatorId)

        return chatRepository.saveAndFlush(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toModel().also { entity ->
          applicationEventPublisher.publishEvent(
            ChatCreatedEvent(
                    chatId = entity.id!!,
                    participantIds = entity.participants.map { it.userId }
                )
            )
        }
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundEx()

        val isRequestingUserInTheChat = chat.participants.any {
            it.userId == requestUserId
        }

        if (!isRequestingUserInTheChat) {
            throw ForbiddenEx()
        }

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundEx(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toModel(lastMessage)

        applicationEventPublisher.publishEvent(
            ChatParticipantsJoinedEvent(
                chatId = chatId,
                usersId = userIds
            )
        )

        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId,
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundEx()

        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundEx(userId)

        val newParticipant = chat.participants.size - 1

        if(newParticipant == 0){
            chatRepository.delete(chat)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository.findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()?.toModel()
    }
}