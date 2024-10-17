package com.example.home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.utils.DataError
import com.example.core.presentation.ui.UiText
import com.example.core.presentation.ui.asUiText
import com.example.home.presentation.events.DashboardEvent
import com.example.products.domain.repositories.ProductsRepository
import com.example.products.presentation.model.mapper.toProductUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.example.core.domain.utils.Result as ResultResponse

/**
 * ViewModel for the dashboard.
 *
 * It retrieves the products
 *
 * @param productsRepository
 * @property state the state of the dashboard.
 * @constructor Creates a new instance of [DashboardVM].
 */
class DashboardVM(
    private val productsRepository: ProductsRepository,
    private val applicationScope: CoroutineScope
): ViewModel() {
  var state by mutableStateOf(DashboardState())
    private set

  private val eventChannel = Channel<DashboardEvent>()
  val eventsFlow = eventChannel.receiveAsFlow()

  init {
    productsRepository.getProducts().onEach { runs ->
      val productsUi = runs.map {productMap ->
        productMap.toProductUi()
      }
      state = state.copy(productsList = productsUi)
    }.launchIn(viewModelScope)
  }

  fun onAction(action: DashboardAction) {
    when (action) {
      is DashboardAction.OnProductDelete -> {
        viewModelScope.launch {
          when(val result = productsRepository.deleteProduct(action.productId)) {
            is ResultResponse.Error -> {
                eventChannel.send(DashboardEvent.Error(result.error.asUiText()))
            }
            is ResultResponse.Success -> {
              eventChannel.send(DashboardEvent.DeleteSuccess)
            }
          }
        }
      }
      else -> Unit
    }
  }

  private fun logout(){
    applicationScope
  }
}