package com.example.presentation.responses

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(
    val token: String
)