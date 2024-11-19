package com.example.core.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.models.TokenClaim
import com.example.data.models.TokenConfig
import com.example.database.models.UserTable.userName
import com.example.domain.repository.UserRepository
import io.ktor.server.application.Application
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.Date

class JwtServiceImpl(
    private val application: Application,
    private val userRepository: UserRepository
): JwtService {
    private val secret = getConfigProperty("jwt.secret")
    private val issuer = getConfigProperty("jwt.issuer")
    private val audience = getConfigProperty("jwt.audience")
    private val domain = getConfigProperty("jwt.domain")
    val realm = getConfigProperty("jwt.realm")

    val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    override suspend fun createAccessToken(vararg claims: TokenClaim): String =
        createJwtToken(3_600_000, *claims)

    override suspend fun createRefreshToken(vararg claims: TokenClaim): String =
        createJwtToken(86_400_000, *claims)

    override suspend fun audienceMatches(audience: String): Boolean = this.audience == audience

    private fun createJwtToken(expireIn: Int, vararg claims: TokenClaim): String {
        var token =JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + expireIn))


        claims.forEach {
            token = token.withClaim(it.name, it.value)
        }

        return token.sign(Algorithm.HMAC256(secret))
    }


    override suspend fun customValidator(credential: JWTCredential): JWTPrincipal? {
        val userName = extractName(credential)

        val foundUser = userName?.let { userRepository.retrieveByUserName(it) }
        return foundUser?.let {
            if(audienceMatches(credential)){
                JWTPrincipal(credential.payload)
            } else null
        }
    }

    private fun audienceMatches(credential: JWTCredential): Boolean {
        return credential.payload.audience.contains(audience)
    }

    private fun extractName(credential: JWTCredential): String? {
        return credential.payload.getClaim("username").asString()
    }

    private fun getConfigProperty(path: String) =
        application.environment.config.property(path).getString()
}