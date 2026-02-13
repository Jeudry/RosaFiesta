package com.example.products.data.models

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSerializationApi
@Serializable
data class ProductRequest(
    val name: String,
    val description: String?,
    val price: Double,
    @SerialName("rental_price") val rentalPrice: Double?,
    @SerialName("image_url") val imageUrl: String?,
    val stock: Int,
    val color: Long,
    val size: Double
)