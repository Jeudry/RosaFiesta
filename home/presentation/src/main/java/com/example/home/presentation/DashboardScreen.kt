@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.products.presentation.components.ProductSmallCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreenRoot(
  viewModel: DashboardVM = koinViewModel(),
  onProductsList: () -> Unit,
  onProductDetail: (ProductId) -> Unit
) {
  DashboardScreen(
    state = viewModel.state,
    onAction = { action: BaseProductAction ->
      when (action) {
        is ProductAction.OnProductDetail -> onProductDetail(action.productId)
        is DashboardAction.OnProductsList -> onProductsList()
        else -> Unit
      }

      viewModel.onAction(action)
    }
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
  state: DashboardState,
  onAction: (BaseProductAction) -> Unit
) {
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
          .padding(bottom = 16.dp)
          .animateItemPlacement(),
        product = productUi,
        onAction = onAction
      )
    }
  }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
  RFTheme {
    DashboardScreen(
      state = DashboardState(),
      onAction = {}
    )
  }
}