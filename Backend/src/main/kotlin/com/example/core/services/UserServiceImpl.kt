package com.example.core.services

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.data.models.SaltedHash
import com.example.data.models.TokenClaim
import com.example.data.services.auth.SHA256HashingService
import com.example.data.services.users.UserService
import com.example.database.models.UserTable.userName
import com.example.domain.models.User
import com.example.domain.repository.RefreshTokenRepository
import com.example.domain.repository.UserRepository
import com.example.presentation.requests.AuthRequest
import com.example.presentation.responses.AuthResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import java.util.UUID

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val jwtService: JwtServiceImpl,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val hashingService: SHA256HashingService
): UserService {
    override suspend fun authenticate(loginRequest: AuthRequest): AuthResponse? {
        val user = userRepository.retrieveByUserName(loginRequest.username) ?: return null

        val isValidPassword = hashingService.verify(loginRequest.password, SaltedHash(user.password, user.salt))
        return if(isValidPassword){
            val claim = TokenClaim(
                name = "userName",
                value = user.userName
            )
            val token = jwtService.createAccessToken(claim)
            val refreshToken = jwtService.createRefreshToken(claim)
            refreshTokenRepository.saveToken(refreshToken, user.userName)

            AuthResponse(token, refreshToken)
        } else null
    }

    override suspend fun refreshToken(accessToken: String): String? {
        val decodedRefreshToken = verifyRefreshToken(accessToken)
        val persistedUserName = refreshTokenRepository.findUsernameByToken(accessToken)

        return if(decodedRefreshToken != null && persistedUserName != null) {
            val foundUser = userRepository.retrieveByUserName(persistedUserName)
            val usernameFromRefreshToken = decodedRefreshToken.getClaim("username").asString()

            if(foundUser?.userName == usernameFromRefreshToken) {
                val claim = TokenClaim(
                    name = "userName",
                    value = usernameFromRefreshToken
                )
                jwtService.createAccessToken(claim)
            } else null
        } else null
    }

    private suspend fun verifyRefreshToken(accessToken: String): DecodedJWT? {
        val decodedJWT = decodedJWT(accessToken)

        return decodedJWT?.let {
            val audienceMatches = jwtService.audienceMatches(it.audience.first())

            if(audienceMatches)
                decodedJWT
            else null
        }
    }

    private fun decodedJWT(token: String) = try {
        jwtService.jwtVerifier.verify(token)
    } catch (e: Exception) {
        null
    }

    override suspend fun retrieveAll(): List<User> =
        userRepository.retrieveAll()

    override suspend fun retrieveById(id: UUID): User? =
        userRepository.retrieveById(id = id)

    override suspend fun retrieveByUserName(name: String): User? =
        userRepository.retrieveByUserName(name = name)

    override suspend fun add(user: User): String? {
        val userExists = userRepository.retrieveByUserName(user.userName) != null
        if(userExists) return null
        val id =userRepository.add(user)

        return id
    }

    override suspend fun remove(id: UUID): Boolean =
        userRepository.remove(id = id)
}