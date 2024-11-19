package com.example.presentation.requests.products

import kotlinx.serialization.Serializable

@Serializable
data class ProductRequest(
    val name: String,
    val price: Double,
    val rentPrice: Double?,
    val stock: Int,
    val description: String?,
    val imageUrl: String?
)