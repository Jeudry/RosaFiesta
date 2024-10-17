package com.example.products.presentation.actions

import com.example.core.domain.product.ProductId
import com.example.products.presentation.model.ProductUi

sealed interface ProductsListAction {
    data class OnProductDetail(val productId: ProductId): ProductsListAction
    data class OnProductDelete(val productId: ProductId): ProductsListAction
}