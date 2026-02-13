package com.example.products.presentation.viewModels

import androidx.lifecycle.ViewModel
import com.example.products.domain.repositories.ProductsRepository

class ProductsVM(
    private val productsRepository: ProductsRepository
): ViewModel() {

}