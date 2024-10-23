package com.example.core.presentation.ui

import com.example.core.domain.product.ProductId

interface BaseProductAction

sealed interface ProductAction:BaseProductAction {
    data class OnProductDetail(val productId: ProductId) : ProductAction
    data class OnProductDelete(val productId: ProductId): ProductAction
}