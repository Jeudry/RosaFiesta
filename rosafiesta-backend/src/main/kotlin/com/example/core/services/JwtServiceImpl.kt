package com.example.core.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.models.TokenClaim
import com.example.data.models.TokenConfig
import com.example.database.models.UserTable.userName
import com.example.domain.repository.UserRepository
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.engine.applicationEnvironment
import java.util.Date

class JwtServiceImpl(
    private val userRepository: UserRepository,
    private val enviroment: ApplicationEnvironment
): JwtService {
     override val jwtConfig = TokenConfig(
        audience = getConfigProperty("jwt.audience"),
        domain = getConfigProperty("jwt.domain"),
        secret = getConfigProperty("jwt.secret"),
        realm = getConfigProperty("jwt.realm"),
         issuer = getConfigProperty("jwt.issuer")
         )

    override val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(jwtConfig.secret))
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .build()

    override suspend fun createAccessToken(vararg claims: TokenClaim): String =
        createJwtToken(3_600_000, *claims)

    override suspend fun createRefreshToken(vararg claims: TokenClaim): String =
        createJwtToken(86_400_000, *claims)

    override suspend fun audienceMatches(audience: String): Boolean = this.jwtConfig.audience == audience

    private fun createJwtToken(expireIn: Int, vararg claims: TokenClaim): String {
        var token =JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + expireIn))


        claims.forEach {
            token = token.withClaim(it.name, it.value)
        }

        return token.sign(Algorithm.HMAC256(jwtConfig.secret))
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
        return credential.payload.audience.contains(jwtConfig.audience)
    }

    private fun extractName(credential: JWTCredential): String? {
        return credential.payload.getClaim("username").asString()
    }

    private fun getConfigProperty(path: String): String {
        return enviroment.config.property(path).getString()
    }
}