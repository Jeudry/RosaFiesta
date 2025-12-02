package com.adventist.adventist.api.mappers

import com.adventist.adventist.api.dto.PlatformDto
import com.adventist.adventist.domain.model.DeviceToken

fun PlatformDto.toDto(): DeviceToken.Platform {
  return when (this) {
    PlatformDto.ANDROID -> DeviceToken.Platform.ANDROID
    PlatformDto.IOS -> DeviceToken.Platform.IOS
    PlatformDto.WEB -> DeviceToken.Platform.WEB
  }
}