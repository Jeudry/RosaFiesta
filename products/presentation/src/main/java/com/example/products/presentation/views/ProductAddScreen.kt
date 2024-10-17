package com.example.products.presentation.views

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.presentation.designsystem.CheckIcon
import com.example.core.presentation.designsystem.EmailIcon
import com.example.core.presentation.designsystem.LogoIcon
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.RunIcon
import com.example.core.presentation.designsystem.components.RFActionButton
import com.example.core.presentation.designsystem.components.RFFloatingActionBtn
import com.example.core.presentation.designsystem.components.RFScaffold
import com.example.core.presentation.designsystem.components.RFTextField
import com.example.core.presentation.designsystem.components.RFToolbar
import com.example.core.presentation.ui.ObserveAsEvents
import com.example.products.presentation.R
import com.example.products.presentation.actions.ProductAddAction
import com.example.products.presentation.events.ProductAddEvent
import com.example.products.presentation.states.ProductAddState
import com.example.products.presentation.viewModels.ProductAddVM
import org.koin.androidx.compose.koinViewModel

@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAddSR(
    viewModel: ProductAddVM = koinViewModel(),
    onSuccessfulAdd: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProductAddEvent.Error -> {
                keyboardController?.hide()
                Toast.makeText(
                    context,
                    event.error.asString(context = context),
                    Toast.LENGTH_LONG
                ).show()
            }

            ProductAddEvent.AddSuccess -> {
                keyboardController?.hide()
                Toast.makeText(
                    context,
                    R.string.product_added_successfully,
                    Toast.LENGTH_LONG
                ).show()
                onSuccessfulAdd()
            }
        }
    }

    ProductAddScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
private fun ProductAddScreen(
    state: ProductAddState,
    onAction: (ProductAddAction) -> Unit
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
                    onAction(ProductAddAction.OnAddClick)
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier.padding(padding)
        ){
            ProductForm(
                state = state,
                onAction = onAction
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun ProductForm(
    state: ProductAddState,
    onAction: (ProductAddAction) -> Unit
){
    Column() {
        RFTextField(
            state = state.name,
            startIcon = EmailIcon,
            endIcon = if (state.isNameValid.isValid) CheckIcon else null,
            hint = stringResource(id = R.string.name),
            title = stringResource(id = R.string.name),
            modifier = Modifier.fillMaxWidth(),
            additionalInfo = stringResource(id = R.string.must_be_a_valid_name),
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(32.dp))

        RFTextField(
            state = state.description,
            startIcon = EmailIcon,
            endIcon = if (state.isDescriptionValid.isValid) CheckIcon else null,
            hint = stringResource(id = R.string.description),
            title = stringResource(id = R.string.description),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(32.dp))

        RFTextField(
            state = state.price,
            startIcon = EmailIcon,
            endIcon = if (state.isPriceValid.isValid) CheckIcon else null,
            hint = stringResource(id = R.string.price),
            title = stringResource(id = R.string.price),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number,
            additionalInfo = stringResource(id = R.string.must_be_a_valid_price)
        )
        Spacer(modifier = Modifier.height(32.dp))

        RFTextField(
            state = state.stock,
            startIcon = EmailIcon,
            endIcon = if (state.isStockValid.isValid) CheckIcon else null,
            hint = stringResource(id = R.string.stock),
            title = stringResource(id = R.string.stock),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number,
            additionalInfo = stringResource(id = R.string.must_be_a_valid_stock)
        )
        Spacer(modifier = Modifier.height(32.dp))

        RFTextField(
            state = state.rentalPrice,
            startIcon = EmailIcon,
            endIcon = if (state.isRentalPriceValid.isValid) CheckIcon else null,
            hint = stringResource(id = R.string.rental_price),
            title = stringResource(id = R.string.rental_price),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(32.dp))

        RFTextField(
            state = state.imageUrl,
            startIcon = EmailIcon,
            endIcon = CheckIcon,
            hint = stringResource(id = R.string.product_image),
            title = stringResource(id = R.string.product_image),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(32.dp))

        RFActionButton(
            text = stringResource(id = R.string.add_product),
            isLoading = state.isAdding,
            enabled = state.isValid(),
            modifier = Modifier.fillMaxWidth()
        ) {
            onAction(ProductAddAction.OnAddClick)
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Suppress("OPT_IN_USAGE_FUTURE_ERROR")
@Preview
@Composable
private fun ProductAddSP() {
    RFTheme {
        ProductAddScreen(
            state = ProductAddState(),
            onAction = {}
        )
    }
}