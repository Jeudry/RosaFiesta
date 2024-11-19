@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.products.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.domain.product.ProductId
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.ui.BaseProductAction
import com.example.core.presentation.ui.ProductAction
import com.example.products.presentation.components.CategoriesList
import com.example.products.presentation.components.ProductSmallCard
import com.example.products.presentation.states.ProductsListState
import com.example.products.presentation.viewModels.ProductsListVM
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductsListSR(
  viewModel: ProductsListVM = koinViewModel(),
  onProductDetail: (ProductId) -> Unit
) {
  ProductsListScreen(
    state = viewModel.state,
    onAction = { action ->
      when(action){
        is ProductAction.OnProductDetail -> onProductDetail(action.productId)
        else -> Unit
      }
      
      viewModel.onAction(action)
    }
  )
}

@Composable
private fun ProductsListScreen(
  state: ProductsListState,
  onAction: (BaseProductAction) -> Unit
) {
  Column {
    CategoriesList(
      categoryList = state.categoriesList
    )
    Spacer(modifier = Modifier.height(24.dp))
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(
        items = state.productsList,
        key = { it.id }
      ) { productUi ->
        ProductSmallCard(
          modifier = Modifier
            .padding(bottom = 16.dp),
          product = productUi,
          onAction = onAction
        )
      }
    }
  }
}

@Preview
@Composable
private fun ProductsListSP() {
  RFTheme {
    ProductsListScreen(
      state = ProductsListState(),
      onAction = {}
    )
  }
}