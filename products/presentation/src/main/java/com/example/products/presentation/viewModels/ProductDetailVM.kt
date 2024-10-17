package com.example.products.presentation.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.core.domain.product.ProductId
import com.example.products.domain.repositories.ProductsRepository
import com.example.products.presentation.model.ProductUi
import com.example.products.presentation.states.ProductDetailState
import androidx.lifecycle.viewModelScope
import com.example.core.domain.utils.Result
import com.example.core.domain.utils.map
import com.example.core.presentation.ui.asUiText
import com.example.products.presentation.actions.ProductDetailAction
import com.example.products.presentation.events.ProductDetailEvent
import com.example.products.presentation.model.mapper.toProductUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.example.core.domain.utils.Result as ResultResponse

class ProductDetailVM(
    private val productsRepository: ProductsRepository,
    productId: ProductId
): ViewModel() {
    var state by mutableStateOf<ProductDetailState?>(null)
        private set

    private val eventChannel = Channel<ProductDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            when(val result = productsRepository.getProduct(productId)) {
                is ResultResponse.Error -> {
                    eventChannel.send(ProductDetailEvent.Error(result.error.asUiText()))
                }
                is ResultResponse.Success -> {
                    val product = ProductDetailState(result.data.toProductUi())
                    state = product
                }
            }
        }
    }

    fun onAction(action: ProductDetailAction) {
        when(action) {
            is ProductDetailAction.OnProductDelete -> {
                viewModelScope.launch {
                    when(val result = productsRepository.deleteProduct(state!!.product.id)) {
                        is Result.Error -> {
                            eventChannel.send(ProductDetailEvent.Error(result.error.asUiText()))
                        }
                        is ResultResponse.Success -> {
                            eventChannel.send(ProductDetailEvent.DeleteSuccess)
                        }
                    }
                }
            }
            else -> Unit
        }
    }
}