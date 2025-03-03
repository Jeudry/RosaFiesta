package com.example.products.presentation.model

import androidx.compose.ui.graphics.Color
import java.time.ZonedDateTime

data class ProductUi (
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val rentalPrice: Double? = null,
    val color: Color,
    val size: Double,
    val imageUrl: String? = null,
    val stock: Int,
    val created: ZonedDateTime
)