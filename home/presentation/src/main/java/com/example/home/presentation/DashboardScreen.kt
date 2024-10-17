@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.core.presentation.designsystem.AnalyticsIcon
import com.example.core.presentation.designsystem.LogoIcon
import com.example.core.presentation.designsystem.LogoutIcon
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.RunIcon
import com.example.core.presentation.designsystem.components.RFFloatingActionBtn
import com.example.core.presentation.designsystem.components.RFScaffold
import com.example.core.presentation.designsystem.components.RFToolbar
import com.example.core.presentation.ui.components.DropDownItem
import com.example.products.presentation.components.ProductListItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreenRoot(
  viewModel: DashboardVM = koinViewModel(),
  onProductsList: () -> Unit,
  onProductDetail: (Int) -> Unit,
  onProductAdd: () -> Unit
) {
  DashboardScreen(
    state = viewModel.state,
    onAction = { action ->
      when (action) {
        DashboardAction.OnProductsList -> onProductsList()
        is DashboardAction.OnProductDetail -> TODO()
        is DashboardAction.OnProductAdd -> onProductAdd()
        else -> Unit
      }

      viewModel.onAction(action)
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
  state: DashboardState,
  onAction: (DashboardAction) -> Unit
) {
  val topAppBarState = rememberTopAppBarState()
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
    state = topAppBarState
  )

  RFScaffold(
    topAppBar = {
      RFToolbar(
        showBackButton = false,
        title = stringResource(id = R.string.rosafiesta),
        scrollBehavior = scrollBehavior,
        startContent = {
          Icon(
            imageVector = LogoIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
          )
        }
      )
    },
    floatingActionButton = {
      RFFloatingActionBtn(
        icon = RunIcon,
        onClick = {
          onAction(DashboardAction.OnProductAdd)
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .padding(horizontal = 16.dp),
      contentPadding = padding,
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