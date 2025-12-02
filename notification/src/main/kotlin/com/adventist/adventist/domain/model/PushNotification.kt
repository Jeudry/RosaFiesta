package com.adventist.adventist.domain.model

import com.adventist.adventist.domain.types.ChatId
import java.util.*

data class PushNotification(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val recipients: List<DeviceToken>,
  val message: String,
  val chatId: ChatId,
  val data: Map<String, String>
)