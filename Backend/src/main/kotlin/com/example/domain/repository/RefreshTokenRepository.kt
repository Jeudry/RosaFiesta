package com.example.domain.repository

class RefreshTokenRepository {

    private val tokens = mutableMapOf<String, String>()

    fun findUsernameByToken(token: String): String? =
        tokens[token]

    fun saveToken(token: String, username: String) {
        tokens[token] = username


    }
}