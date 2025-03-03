package com.example.products.presentation.states

import com.example.products.presentation.model.CategoryUi
import com.example.products.presentation.model.ProductUi

data class ProductsListState (
    var productsList: List<ProductUi> = emptyList(),
    var categoriesList: List<CategoryUi> = emptyList()
)