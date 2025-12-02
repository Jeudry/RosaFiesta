package com.adventist.adventist.service

import com.adventist.adventist.domain.exceptions.InvalidTokenEx
import com.adventist.adventist.domain.events.user.UserEvent
import com.adventist.adventist.domain.exception.EmailNotVerifiedEx
import com.adventist.adventist.domain.exception.InvalidCredentialsEx
import com.adventist.adventist.domain.exception.PasswordHashFailedEx
import com.adventist.adventist.domain.exception.UserAlreadyExistsEx
import com.adventist.adventist.domain.exception.UserNotFoundEx
import com.adventist.adventist.domain.model.AuthenticatedUser
import com.adventist.adventist.domain.model.User
import com.adventist.adventist.domain.types.UserId
import com.adventist.adventist.infra.database.entities.RefreshTokenEntity
import com.adventist.adventist.infra.database.entities.UserEntity
import com.adventist.adventist.infra.database.mappers.toModel
import com.adventist.adventist.infra.database.repositories.RefreshTokenRepository
import com.adventist.adventist.infra.database.repositories.UserRepository
import com.adventist.adventist.infra.message_queue.EventPublisher
import com.adventist.adventist.infra.security.PasswordEncoder
import com.adventist.adventist.services.JwtService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher
){
    fun register(username: String, email: String, password: String): User {
        val trimmedEmail = email.trim()

        val user = userRepository.findByEmailOrUsername(
            trimmedEmail,
            username.trim()
        )

        if(user != null){
            throw UserAlreadyExistsEx()
        }

        val passwordHashed = passwordEncoder.encode(password.trim())
            ?: throw PasswordHashFailedEx()

        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = trimmedEmail,
                username = username.trim(),
                hashedPassword = passwordHashed
            )
        ).toModel()

        val emailToken = emailVerificationService.createVerificationToken(trimmedEmail)

        eventPublisher.publish(
            event = UserEvent.Created(
                userId = savedUser.id,
                email = savedUser.email,
                username = savedUser.username,
                verificationToken = emailToken.token
            )
        )

        return savedUser
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email) ?: throw InvalidCredentialsEx()

        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsEx()
        }

        if(!user.hasVerifiedEmail){
            throw EmailNotVerifiedEx()
        }

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)
            storeRefreshToken(userId, refreshToken)
            AuthenticatedUser(
                user = user.toModel(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundEx()
    }

    @Transactional
     fun refresh(refreshToken: String): AuthenticatedUser {
        if(!jwtService.validateRefreshToken(refreshToken)){
            throw InvalidTokenEx("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundEx() }
        val hashed = hashToken(refreshToken)

        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            ) ?: throw InvalidTokenEx("Invalid refreshToken")

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            )

            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, newRefreshToken)

            AuthenticatedUser(
                user = user.toModel(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        } ?: throw UserNotFoundEx()
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                hashedToken = hashed,
                createdAt = Instant.now(),
                updatedAt = expiresAt
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}