package com.example.database.mappers

import com.example.core.domain.product.Product
import com.example.core.domain.run.Run
import com.example.database.entity.ProductEntity
import com.plcoding.core.database.entity.RunEntity
import org.bson.types.ObjectId
import java.time.Instant
import java.time.ZoneId

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
        id = id ?: ObjectId().toHexString(),
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