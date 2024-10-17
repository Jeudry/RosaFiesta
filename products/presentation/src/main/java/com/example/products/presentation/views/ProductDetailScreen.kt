@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.products.presentation.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.example.products.presentation.actions.ProductDetailAction
import com.example.products.presentation.model.ProductUi
import com.example.products.presentation.states.ProductDetailState
import com.example.products.presentation.viewModels.ProductDetailVM
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ProductDetailSR(
    viewModel: ProductDetailVM = koinViewModel()
) {
    ProductDetailScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@ExperimentalMaterial3Api
@Composable
private fun ProductDetailScreen(
    state: ProductDetailState?,
    onAction: (ProductDetailAction) -> Unit
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

    }
}

@Preview
@Composable
private fun ProductDetailSP() {
    RFTheme {
        ProductDetailScreen(
            state = ProductDetailState(
                product = ProductUi(
                    created = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")),
                    id = "123",
                    name = "name example",
                    description = "description example",
                    price = 50.0,
                    rentalPrice = 10.0,
                    imageUrl = "",
                    stock = 10
                )
            ),
            onAction = {}
        )
    }
}