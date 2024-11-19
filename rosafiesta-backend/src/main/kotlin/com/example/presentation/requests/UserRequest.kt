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
    val bornDate: String,
    val salt: String,
    val created: String,
    val avatar: String?,
    val firstName: String?,
    val lastName: String?
)

fun UserRequest.toModel(): User =
    User(
        id = UUID.randomUUID(),
        userName = username,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        bornDate = bornDate,
        salt = UUID.randomUUID().toString(),
        created = Date().toString(),
        avatar = avatar,
        firstName = firstName ?: "",
        lastName = lastName ?: ""
    )