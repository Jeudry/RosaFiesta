package com.example.home.presentation

import com.example.core.domain.product.ProductId
import com.example.products.presentation.model.ProductUi

sealed interface DashboardAction {
    data class OnProductDetail(val productId: ProductId): DashboardAction
    data object OnProductsList: DashboardAction
    data class OnProductDelete(val productId: ProductId): DashboardAction
}