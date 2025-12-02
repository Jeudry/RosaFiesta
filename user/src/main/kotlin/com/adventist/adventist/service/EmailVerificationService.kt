package com.adventist.adventist.service

import com.adventist.adventist.domain.exceptions.InvalidTokenEx
import com.adventist.adventist.domain.events.user.UserEvent
import com.adventist.adventist.domain.exception.UserNotFoundEx
import com.adventist.adventist.domain.model.EmailVerificationToken
import com.adventist.adventist.infra.database.entities.EmailVerificationTokenEntity
import com.adventist.adventist.infra.database.mappers.toModel
import com.adventist.adventist.infra.database.repositories.EmailVerificationTokenRepository
import com.adventist.adventist.infra.database.repositories.UserRepository
import com.adventist.adventist.infra.message_queue.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant.now
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService (
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${adventist.email.verification.expiry-hours}") private val expiryHours: Long,
    private val eventPublisher: EventPublisher
){

    @Transactional
    fun resendVerificationEmail(email: String) {
        val token = createVerificationToken(email)
        if(token.user.hasEmailVerified){
            return
        }

        eventPublisher.publish(
            event = UserEvent.RequestResendVerification(
                userId = token.user.id,
                email = token.user.email,
                username = token.user.username,
                verificationToken = token.token
            )
        )
    }

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email) ?: throw UserNotFoundEx()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity,
        )

        return emailVerificationTokenRepository.save(token).toModel()
    }

    fun verifyEmail(token: String){
        val verificationToken = emailVerificationTokenRepository.findByToken(token) ?: throw UserNotFoundEx()

        if(verificationToken.isUsed){
            throw InvalidTokenEx("The token has already been used.")
        }

        if(verificationToken.isExpired){
            throw InvalidTokenEx("The token has expired.")
        }

        emailVerificationTokenRepository.save(verificationToken.apply {
            this.usedAt = now()
        })

        val user = userRepository.save(
            verificationToken.user.apply {
                hasVerifiedEmail = true
            }
        ).toModel()

        eventPublisher.publish(
            UserEvent.Verified(
                userId = verificationToken.user.id!!,
                email = verificationToken.user.email,
                username = verificationToken.user.username
            )
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens(){
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(now = now())
    }
}