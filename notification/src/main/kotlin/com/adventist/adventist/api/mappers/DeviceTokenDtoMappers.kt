package com.adventist.adventist.api.mappers

import com.adventist.adventist.api.dto.DeviceTokenDto
import com.adventist.adventist.domain.model.DeviceToken

fun DeviceToken.toDto(): DeviceTokenDto {
  return DeviceTokenDto(
    userId = userId,
    token = token,
    createdAt = createdAt
  )
}