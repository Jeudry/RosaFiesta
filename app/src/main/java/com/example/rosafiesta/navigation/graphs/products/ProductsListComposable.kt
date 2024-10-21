package com.example.rosafiesta.navigation.graphs.products

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.core.presentation.ui.UiText
import com.example.products.presentation.views.ProductsListSR
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.productsListComposable(navHostData: NavHostData) {
  val route = UiText.StringResource(R.string.products_list_route).asString(navHostData.context)
  return composable(route) {
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = UiText.StringResource(R.string.products_list_title).asString(navHostData.context)
        )
      )
    }
    ProductsListSR()
  }
}