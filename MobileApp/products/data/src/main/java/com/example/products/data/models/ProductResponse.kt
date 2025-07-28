package com.example.products.data.models

import com.example.core.data.converters.UUIDSerializer
import com.example.core.data.converters.ZonedDateTimeSerializer
import com.example.core.domain.product.ProductId
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@InternalSerializationApi
@Serializable
data class ProductResponse(
    @Serializable(with = UUIDSerializer::class) val id: ProductId?,
    val name: String,
    val description: String?,
    val price: Double,
    @SerialName("rental_price") val rentalPrice: Double? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val stock: Int,
    val color: Long,
    val size: Double,
    @Serializable(with = ZonedDateTimeSerializer::class) val created: ZonedDateTime
)