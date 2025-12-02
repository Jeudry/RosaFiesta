package com.adventist.adventist.api.dto.ws

import com.adventist.adventist.domain.types.UserId

data class ProfilePictureUpdateDto(
  val userId: UserId,
  val newUrl: String? = null
  )