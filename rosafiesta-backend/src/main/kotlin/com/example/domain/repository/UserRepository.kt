package com.example.domain.repository

import com.example.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun retrieveAll(): List<User>
    suspend fun retrieveById(id: UUID): User?
    suspend fun retrieveByUserName(name: String): User?
    suspend fun add(user: User): String
    suspend fun remove(id: UUID): Boolean
}