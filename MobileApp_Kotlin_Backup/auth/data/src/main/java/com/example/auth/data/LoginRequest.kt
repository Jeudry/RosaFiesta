package com.example.auth.data

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class LoginRequest(
  val email: String,
  val password: String
)
