@file:OptIn(ExperimentalFoundationApi::class)

package com.example.rosafiesta.navigation.graphs.products

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.core.presentation.ui.UiText
import com.example.products.presentation.views.ProductAddSR
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.productAddComposable(navHostData: NavHostData) {
  val route = UiText.StringResource(R.string.product_add_route).asString(navHostData.navController.context)
  val productsListRoute = UiText.StringResource(R.string.products_list_route).asString(navHostData.navController.context)
  return composable(route) {
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = UiText.StringResource(R.string.product_add_title)
            .asString(navHostData.navController.context)
        )
      )
    }
    ProductAddSR(
      onSuccessfulAdd = {
        navHostData.navController.navigate(productsListRoute)
      }
    )
  }
}