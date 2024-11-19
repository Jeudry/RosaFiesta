package com.example.presentation.requests

import com.example.domain.models.User
import com.example.presentation.responses.UserResponse
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class UserRequest(
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val bornDate: String
)

fun UserRequest.toModel(): User =
    User(
        id = UUID.randomUUID(),
        username = username,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        bornDate = Date.from(Date().toInstant())
    )