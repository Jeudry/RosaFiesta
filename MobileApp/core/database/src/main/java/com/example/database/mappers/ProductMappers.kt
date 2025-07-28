package com.example.database.mappers

import com.example.core.domain.product.Product
import com.example.database.entity.ProductEntity
import java.time.Instant
import java.time.ZoneId
import java.util.*

fun ProductEntity.toProduct(): Product {
    return Product(
        id = id,
        name = name,
        description = description,
        price = price,
        rentalPrice = rentalPrice,
        imageUrl = imageUrl,
        stock = stock,
        created = Instant.parse(created)
            .atZone(ZoneId.of("UTC")),
        color = color,
        size = size
    )
}

fun Product.toProductEntity(): ProductEntity {
    return ProductEntity(
        id = id ?: UUID.randomUUID(),
        name = name,
        description = description,
        price = price,
        rentalPrice = rentalPrice,
        imageUrl = imageUrl,
        stock = stock,
        created = created.toInstant().toString(),
        color = color,
        size = size
    )
}