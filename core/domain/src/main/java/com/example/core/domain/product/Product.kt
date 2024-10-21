package com.example.core.domain.product

import java.time.ZoneId
import java.time.ZonedDateTime

data class Product (
  val id: String?,
  val name: String,
  val description: String?,
  val price: Double,
  val rentalPrice: Double?,
  val color: Long,
  val size: Double,
  val imageUrl: String?,
  val stock: Int = 0,
  val created: ZonedDateTime = ZonedDateTime.now()
    .withZoneSameInstant(ZoneId.of("UTC"))
)