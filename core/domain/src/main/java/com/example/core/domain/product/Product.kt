package com.example.core.domain.product

import java.time.ZonedDateTime

data class Product (
  val id: String?,
  val name: String,
  val description: String?,
  val price: Double,
  val rentalPrice: Double?,
  val imageUrl: String?,
  val stock: Int,
  val created: ZonedDateTime
) {

}