package com.example.products.presentation.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.products.domain.repositories.ProductsRepository
import com.example.products.presentation.actions.ProductsListAction
import com.example.products.presentation.model.mapper.toProductUi
import com.example.products.presentation.states.ProductsListState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProductsListVM(
    private val productsRepository: ProductsRepository
): ViewModel() {
    var state by mutableStateOf(ProductsListState())
        private set

    init {
        productsRepository.getProducts().onEach { runs ->
            val productsUi = runs.map {productMap ->
                productMap.toProductUi()
            }
            state = state.copy(productsList = productsUi)
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ProductsListAction) {
        when (action) {
            is ProductsListAction.OnProductDelete -> {
                viewModelScope.launch {
                    productsRepository.deleteProduct(action.productId)
                }
            }
            is ProductsListAction.OnProductDetail -> TODO()
        }
    }
}