package com.example.presentation.requests

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val accessToken: String,
    val refreshToken: String
)