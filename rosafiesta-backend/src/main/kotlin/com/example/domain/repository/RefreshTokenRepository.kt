package com.example.domain.repository

interface RefreshTokenRepository {
    suspend fun findUsernameByToken(token: String): String?
    suspend fun saveToken(token: String, username: String)
}