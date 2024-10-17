@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.products.presentation.views

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
import com.example.core.presentation.designsystem.LogoIcon
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.RunIcon
import com.example.core.presentation.designsystem.components.RFFloatingActionBtn
import com.example.core.presentation.designsystem.components.RFScaffold
import com.example.core.presentation.designsystem.components.RFToolbar
import com.example.products.presentation.R
import com.example.products.presentation.actions.ProductsListAction
import com.example.products.presentation.components.ProductListItem
import com.example.products.presentation.states.ProductsListState
import com.example.products.presentation.viewModels.ProductsListVM
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductsListSR(
    viewModel: ProductsListVM = koinViewModel()
) {
    ProductsListScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductsListScreen(
    state: ProductsListState,
    onAction: (ProductsListAction) -> Unit
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState
    )

    RFScaffold(
        topAppBar = {
            RFToolbar(
                showBackButton = false,
                title = stringResource(id = R.string.products_list),
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
            ) {productUi ->
                ProductListItem(
                    productUi = productUi,
                    onDeleteClick = {
                        onAction(ProductsListAction.OnProductDelete(productUi.id))
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
private fun ProductsListSP() {
    RFTheme {
        ProductsListScreen(
            state = ProductsListState(),
            onAction = {}
        )
    }
}