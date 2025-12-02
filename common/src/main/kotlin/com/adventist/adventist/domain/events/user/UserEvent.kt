package com.adventist.adventist.domain.events.user

import com.adventist.adventist.domain.events.AdventistEvent
import com.adventist.adventist.domain.types.UserId
import java.time.Instant
import java.util.UUID

sealed class UserEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE,
    override val occurredAt: Instant = Instant.now()
): AdventistEvent {

    data class Created (
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_CREATED_KEY
    ): UserEvent(), AdventistEvent

    data class Verified (
        val userId: UserId,
        val email: String,
        val username: String,
        override val eventKey: String = UserEventConstants.USER_VERIFIED
    ): UserEvent(), AdventistEvent

    data class RequestResendVerification (
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESEND_VERIFICATION
    ): UserEvent(), AdventistEvent

    data class RequestResetPassword (
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        val expiresInMinutes: Long,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESET_PASSWORD
    ): UserEvent(), AdventistEvent
}