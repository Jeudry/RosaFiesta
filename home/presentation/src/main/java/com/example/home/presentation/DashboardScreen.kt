@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.presentation.designsystem.RFTheme
import com.example.products.presentation.components.ProductListItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreenRoot(
  viewModel: DashboardVM = koinViewModel(),
  onProductsList: () -> Unit,
  onProductDetail: (Int) -> Unit,
  scrollBehavior: TopAppBarScrollBehavior
) {
  DashboardScreen(
    state = viewModel.state,
    onAction = { action ->
      when (action) {
        DashboardAction.OnProductsList -> onProductsList()
        is DashboardAction.OnProductDetail -> TODO()
        else -> Unit
      }

      viewModel.onAction(action)
    },
    scrollBehavior = scrollBehavior
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
  state: DashboardState,
  onAction: (DashboardAction) -> Unit,
  scrollBehavior: TopAppBarScrollBehavior
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .nestedScroll(scrollBehavior.nestedScrollConnection)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    items(
      items = state.productsList,
      key = { it.id }
    ) { productUi ->
      ProductListItem(
        productUi = productUi,
        onDeleteClick = {
          onAction(DashboardAction.OnProductDelete(productUi.id))
        },
        modifier = Modifier
          .animateItemPlacement()
      )
    }
  }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
  val topAppBarState = rememberTopAppBarState()
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
    state = topAppBarState
  )
  
  RFTheme {
    DashboardScreen(
      state = DashboardState(),
      onAction = {},
      scrollBehavior = scrollBehavior
    )
  }
}