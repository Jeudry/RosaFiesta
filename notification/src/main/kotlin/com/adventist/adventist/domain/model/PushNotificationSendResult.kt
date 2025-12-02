package com.adventist.adventist.domain.model

data class PushNotificationSendResult(
  val succeded: List<DeviceToken>,
  val temporaryFailures: List<DeviceToken>,
  val permanentFailures: List<DeviceToken>
)