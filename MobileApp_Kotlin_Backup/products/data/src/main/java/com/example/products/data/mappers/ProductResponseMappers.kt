package com.example.products.data.mappers

import com.example.core.domain.product.Product
import com.example.products.data.models.ProductResponse
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
fun ProductResponse.toProduct(): Product {
    return Product(
        id = id,
        name = name,
        description = description,
        price = price,
        rentalPrice = rentalPrice,
        imageUrl = imageUrl,
        stock = stock,
        created = created,
        color = color,
        size = size
    )
}