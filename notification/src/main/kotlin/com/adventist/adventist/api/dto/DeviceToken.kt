package com.adventist.adventist.api.dto

import com.adventist.adventist.domain.types.UserId
import java.time.Instant

data class DeviceTokenDto(
  val userId: UserId,
  val token: String,
  val createdAt: Instant
)