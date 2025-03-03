package com.example.data.repositories

import com.example.domain.repository.RefreshTokenRepository

class RefreshTokenRepositoryImpl: RefreshTokenRepository {
    private val tokens = mutableMapOf<String, String>()

    override suspend fun findUsernameByToken(token: String): String? =
        tokens[token]

    override suspend fun saveToken(token: String, username: String) {
        tokens[token] = username
    }
}