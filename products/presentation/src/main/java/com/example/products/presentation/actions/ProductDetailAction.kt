package com.example.products.presentation.actions

import com.example.core.domain.product.ProductId

sealed interface ProductDetailAction {
    data object OnBack: ProductDetailAction
    data object OnProductDelete: ProductDetailAction
}