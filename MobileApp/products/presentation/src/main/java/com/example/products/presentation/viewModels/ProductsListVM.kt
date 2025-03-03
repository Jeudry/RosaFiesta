package com.example.products.presentation.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.utils.Result
import com.example.core.presentation.ui.BaseProductAction
import com.example.core.presentation.ui.ProductAction
import com.example.core.presentation.ui.asUiText
import com.example.products.domain.repositories.ProductsRepository
import com.example.products.presentation.events.ProductListEvent
import com.example.products.presentation.model.CategoryUi
import com.example.products.presentation.model.mapper.toProductUi
import com.example.products.presentation.states.ProductsListState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProductsListVM(
    private val productsRepository: ProductsRepository
): ViewModel() {
    var state by mutableStateOf(ProductsListState(
        categoriesList = generateCategories()
    ))
        private set
    
    private val eventChannel = Channel<ProductListEvent>()
    val eventsFlow = eventChannel.receiveAsFlow()
    
    init {
        productsRepository.getProducts().onEach { runs ->
            val productsUi = runs.map {productMap ->
                productMap.toProductUi()
            }
            state = state.copy(productsList = productsUi)
        }.launchIn(viewModelScope)
    }

    fun onAction(action: BaseProductAction) {
        when (action) {
            is ProductAction.OnProductDelete -> {
                viewModelScope.launch {
                    when(val result = productsRepository.deleteProduct(action.productId)) {
                        is Result.Error -> {
                            eventChannel.send(ProductListEvent.Error(result.error.asUiText()))
                        }
                        is Result.Success -> {
                            eventChannel.send(ProductListEvent.DeleteSuccess)
                        }
                    }
                }
            }
            else -> Unit
        }
    }
    
    private fun generateCategories(): List<CategoryUi> {
        val categoryList = mutableListOf<CategoryUi>()
        
        categoryList.add(
            CategoryUi(
                name = "Sneakers",
                id = "Sneakers"
            )
        )

        categoryList.add(
            CategoryUi(
                name = "Boots",
                id = "Boots"
            )
        )

        categoryList.add(
            CategoryUi(
                name = "Loafers",
                id = "Loafers"
            )
        )
        
        categoryList.add(
            CategoryUi(
                name = "Sandals",
                id = "Sandals"
            )
        )
        
        categoryList.add(
            CategoryUi(
                name = "Flats",
                id = "Flats"
            )
        )
        
        return categoryList
    }
}