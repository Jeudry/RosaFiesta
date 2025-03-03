package com.example.domain.models

import com.example.domain.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Product(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val price: Double,
    val rentPrice: Double?,
    val created: String,
    val stock: Int,
    val description: String?,
    val imageUrl: String?
)