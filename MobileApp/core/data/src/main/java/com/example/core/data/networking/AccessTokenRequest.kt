package com.example.core.data.networking

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
class AccessTokenRequest(
  val refreshToken: String,
  val userId: String
)