package com.example.core.services

import com.example.data.models.SaltedHash

interface HashingService {
    suspend fun generateSaltedHash(value: String, saltLength: Int = 32): SaltedHash
    suspend fun verify(value: String, saltedHash: SaltedHash): Boolean
}