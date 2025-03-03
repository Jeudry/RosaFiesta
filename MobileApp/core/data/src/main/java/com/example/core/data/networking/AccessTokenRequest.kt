package com.example.core.data.networking

import kotlinx.serialization.Serializable

@Serializable
class AccessTokenRequest(
  val refreshToken: String,
  val userId: String
)

fun Create(postModel: PostModel) {
  return nil
}