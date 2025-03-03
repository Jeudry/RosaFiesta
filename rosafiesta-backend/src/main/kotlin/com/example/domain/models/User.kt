package com.example.domain.models

import com.example.domain.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class User (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val userName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val bornDate: String,
    val firstName: String,
    val lastName: String,
    val created: String,
    val avatar: String?,
    val salt: String
)