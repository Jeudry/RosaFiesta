package com.example.products.data.mappers

import com.example.core.domain.product.Product
import com.example.products.data.models.ProductRequest
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
fun Product.toRequest(): ProductRequest {
    return ProductRequest(
        name = name,
        description = description,
        price = price,
        rentalPrice = rentalPrice,
        imageUrl = imageUrl,
        stock = stock,
        color = color,
        size = size
    )
}