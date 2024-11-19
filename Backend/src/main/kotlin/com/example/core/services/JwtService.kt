package com.example.core.services

import com.example.data.models.TokenClaim
import com.example.data.models.TokenConfig
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal

interface JwtService {
    suspend fun customValidator(credential: JWTCredential): JWTPrincipal?
    suspend fun audienceMatches(audience: String): Boolean
    suspend fun createRefreshToken(vararg claims: TokenClaim): String
    suspend fun createAccessToken(vararg claims: TokenClaim): String
}