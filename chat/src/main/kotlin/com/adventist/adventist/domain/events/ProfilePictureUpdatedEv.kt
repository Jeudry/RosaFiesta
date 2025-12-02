package com.adventist.adventist.domain.events

import com.adventist.adventist.domain.types.UserId

data class ProfilePictureUpdatedEv(
  val userId: UserId,
  val newUrl: String? = null
)