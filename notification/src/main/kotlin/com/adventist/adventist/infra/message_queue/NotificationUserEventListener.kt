package com.adventist.adventist.infra.message_queue

import com.adventist.adventist.domain.events.user.UserEvent
import com.adventist.adventist.infra.service.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Component
class NotificationUserEventListener(private val emailService: EmailService) {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent){
        when(event){
            is UserEvent.Created -> {
                emailService.sendVerificationEmail(
                    event.email,
                    event.username,
                    event.userId,
                    event.verificationToken
                )
            }
            is UserEvent.RequestResendVerification ->{
                emailService.sendVerificationEmail(
                    event.email,
                    event.username,
                    event.userId,
                    event.verificationToken
                )
            }
            is UserEvent.RequestResetPassword -> {
                emailService.sendPasswordResetEmail(
                    event.email,
                    event.username,
                    event.userId,
                    event.verificationToken,
                    Duration.ofMinutes(event.expiresInMinutes)
                )
            }
            else -> Unit
        }
    }
}