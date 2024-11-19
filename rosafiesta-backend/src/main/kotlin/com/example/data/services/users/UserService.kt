package com.example.data.services.users

import com.example.domain.models.User
import com.example.presentation.requests.AuthRequest
import com.example.presentation.responses.AuthResponse
import java.util.UUID

interface UserService {
    suspend fun retrieveAll(): List<User>
    suspend fun retrieveById(id: UUID): User?
    suspend fun retrieveByUserName(name: String): User?
    suspend fun add(user: User): String?
    suspend fun remove(id: UUID): Boolean
    suspend fun authenticate(loginRequest: AuthRequest): AuthResponse?
    suspend fun refreshToken(accessToken: String): String?
}