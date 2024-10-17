package com.example.home.presentation

import com.example.products.presentation.model.ProductUi

data class DashboardState(
  val productsList: List<ProductUi> = emptyList()
)