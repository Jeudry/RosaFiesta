package com.adventist.adventist.api.dto

import java.time.Instant

data class ProfilePictureUploadResponse(
  val uploadUrl: String,
  val publicUrl: String,
  val headers: Map<String, String>,
  val expiresAt: Instant
)