@file:Suppress("OPT_IN_USAGE_FUTURE_ERROR")
@file:OptIn(ExperimentalFoundationApi::class)

package com.example.products.presentation.viewModels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.product.Product
import com.example.core.domain.run.Run
import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.Result
import com.example.core.presentation.ui.UiText
import com.example.core.presentation.ui.asUiText
import com.example.products.domain.repositories.ProductsRepository
import com.example.products.domain.validators.ProductDataValidator
import com.example.products.presentation.R
import com.example.products.presentation.actions.ProductAddAction
import com.example.products.presentation.events.ProductAddEvent
import com.example.products.presentation.states.ProductAddState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class ProductAddVM (
    private val productsRepository: ProductsRepository,
    private val productDataValidator: ProductDataValidator
): ViewModel(){
    private val eventChannel = Channel<ProductAddEvent>()
    val events = eventChannel.receiveAsFlow()

    var state by mutableStateOf(ProductAddState())
        private set

    fun onAction(action: ProductAddAction) {
        when (action) {
            ProductAddAction.OnAddClick -> addProduct()
            else -> Unit
        }
    }

    private fun addProduct() {
        viewModelScope.launch {
            state = state.copy(isAdding = true)

            val product = Product(
                id = null,
                name = state.name.text.toString(),
                description = state.description.text.toString(),
                price = state.price.text.toString().toDouble(),
                rentalPrice = state.rentalPrice.text.toString().toDoubleOrNull(),
                imageUrl = state.imageUrl.text.toString(),
                stock = state.stock.text.toString().toInt(),
                created = ZonedDateTime.now()
                .withZoneSameInstant(ZoneId.of("UTC")),
            )

            val result = productsRepository.upsertProduct(product)
            state = state.copy(isAdding = false)

            when (result) {
                is Result.Error -> {
                    if (result.error == DataError.Network.CONFLICT) {
                        eventChannel.send(
                            ProductAddEvent.Error(
                                UiText.StringResource(R.string.product_already_exist)
                            )
                        )
                    } else
                        eventChannel.send(ProductAddEvent.Error(result.error.asUiText()))
                }

                is Result.Success -> {
                    eventChannel.send(ProductAddEvent.AddSuccess)
                }
            }
        }
    }

    init {
        state.name.textAsFlow()
            .onEach { name ->
                val validator = productDataValidator.isValidName(name.toString().trim())
                state = state.copy(
                    isNameValid = validator,
                    canAdd = state.isValid()
                )
            }.launchIn(viewModelScope)

        state.description.textAsFlow()
            .onEach { description ->
                val validator = productDataValidator.isValidDescription(description.toString().trim())
                state = state.copy(
                    isDescriptionValid = validator,
                    canAdd = state.isValid()
                )
            }.launchIn(viewModelScope)

        state.price.textAsFlow()
            .onEach { price ->
                val validator = productDataValidator.isValidPrice(price.toString().toDoubleOrNull() ?: 0.0)
                state = state.copy(
                    isPriceValid = validator,
                    canAdd = state.isValid()
                )
            }.launchIn(viewModelScope)

        state.rentalPrice.textAsFlow()
            .onEach { rentalPrice ->
                val validator = productDataValidator.isValidRentalPrice(rentalPrice.toString().toDoubleOrNull())
                state = state.copy(
                    isRentalPriceValid = validator,
                    canAdd = state.isValid()
                )
            }.launchIn(viewModelScope)

        state.stock.textAsFlow()
            .onEach { stock ->
                val validator = productDataValidator.isValidStock(stock.toString().toIntOrNull() ?: 0)
                state = state.copy(
                    isStockValid = validator,
                    canAdd = state.isValid()
                )
            }.launchIn(viewModelScope)
    }
}