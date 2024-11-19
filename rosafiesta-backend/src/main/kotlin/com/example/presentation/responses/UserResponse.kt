package com.example.presentation.responses

import com.example.domain.models.User
import com.example.presentation.requests.UserRequest
import com.example.domain.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class UserResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String
)

fun UserResponse.fromModel(): UserResponse =
    UserResponse(
        id = UUID.randomUUID(),
        username = username,
        email = email,
        password = password,
        phoneNumber = phoneNumber
    )