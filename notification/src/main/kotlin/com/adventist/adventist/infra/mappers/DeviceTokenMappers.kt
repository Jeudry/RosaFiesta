package com.adventist.adventist.infra.mappers

import com.adventist.adventist.domain.model.DeviceToken
import com.adventist.adventist.infra.database.DeviceTokenEntity

fun DeviceTokenEntity.toModel(): DeviceToken {
  return DeviceToken(
    id = id,
    userId = userId,
    token = token,
    platform = platform.toModel(),
    createdAt = createdAt,
  )
}