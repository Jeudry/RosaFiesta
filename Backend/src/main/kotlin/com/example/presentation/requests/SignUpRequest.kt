package com.example.presentation.requests

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: String,
    val password: String,
    val email: String,
    val phoneNumber: String,
    val bornDate: String,
    val firstName: String,
    val lastName: String,
    val created: String,
    val avatar: String?
)