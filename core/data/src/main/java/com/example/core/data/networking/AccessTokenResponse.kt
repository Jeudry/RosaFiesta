package com.example.core.data.networking

import kotlinx.serialization.Serializable

@Serializable
class AccessTokenResponse(
  val accessToken: String,
  val expirationTimestamp: String
)
