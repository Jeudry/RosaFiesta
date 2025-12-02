package com.adventist.adventist.api.controller

import com.adventist.adventist.api.dto.DeviceTokenDto
import com.adventist.adventist.api.dto.RegisterDeviceRequest
import com.adventist.adventist.api.mappers.toDto
import com.adventist.adventist.api.utils.requestUserId
import com.adventist.adventist.infra.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(private val pushNotificationService: PushNotificationService) {
  
  @PostMapping("/register")
  fun registerDeviceToken(
    @Valid @RequestBody body: RegisterDeviceRequest
  ): DeviceTokenDto {
    return pushNotificationService.registerDevice(
      userId = requestUserId,
      token = body.token,
      platform = body.platform.toDto()
    ).toDto()
  }
  
  @DeleteMapping("/{token}")
  fun unregisterDeviceToken(
    @PathVariable("token") token: String
  ) {
    pushNotificationService.unregisterDevice(
      token = token
    )
  }
  
  
}