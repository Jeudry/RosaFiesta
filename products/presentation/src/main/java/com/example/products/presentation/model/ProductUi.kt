package com.example.products.presentation.model

import java.time.ZonedDateTime

data class ProductUi (
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val rentalPrice: Double?,
    val imageUrl: String?,
    val stock: Int,
    val created: ZonedDateTime
)