package com.example.home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.products.domain.repositories.ProductsRepository
import com.example.products.presentation.model.mapper.toProductUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
          productsRepository.deleteProduct(action.productId)
        }
      }
      else -> Unit
    }
  }

  private fun logout(){
    applicationScope
  }
}