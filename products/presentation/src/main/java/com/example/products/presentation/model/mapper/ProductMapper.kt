package com.example.products.presentation.model.mapper

import androidx.compose.ui.graphics.Color
import com.example.core.domain.product.Product
import com.example.core.domain.run.Run
import com.example.products.presentation.model.ProductUi

/**
 * Maps [Product] to [ProductUi]
 */
fun Product.toProductUi(): ProductUi {

    return ProductUi(
        id = id!!,
        name = name,
        description = description,
        price = price,
        rentalPrice = rentalPrice,
        imageUrl = imageUrl,
        created = created,
        stock = stock,
        color = Color(color),
        size = size,
        rating = rating
    )
}