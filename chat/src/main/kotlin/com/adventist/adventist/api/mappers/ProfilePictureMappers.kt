package com.adventist.adventist.api.mappers

import com.adventist.adventist.api.dto.ProfilePictureUploadResponse
import com.adventist.adventist.domain.models.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toDto(): ProfilePictureUploadResponse {
  return ProfilePictureUploadResponse(
    uploadUrl = this.uploadUrl,
    publicUrl = this.publicUrl,
    headers = this.headers,
    expiresAt = this.expiresAt
  )
}