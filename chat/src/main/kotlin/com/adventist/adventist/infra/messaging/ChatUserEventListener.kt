package com.adventist.adventist.infra.messaging

import com.adventist.adventist.domain.events.user.UserEvent
import com.adventist.adventist.domain.models.ChatParticipant
import com.adventist.adventist.infra.message_queue.MessageQueues
import com.adventist.adventist.services.ChatParticipantService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService
){
    private val logger = org.slf4j.LoggerFactory.getLogger(ChatUserEventListener::class.java)

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent){
            when(event){
                is UserEvent.Created -> {
                    chatParticipantService.createChatParticipant(
                        chatParticipant = ChatParticipant(
                            userId = event.userId,
                            username = event.username,
                            email = event.email,
                            profilePictureUrl = null
                        )
                    )
                    logger.info("Chat participant created for new user: ${event.userId}")
                }
                else -> Unit
            }
    }
}