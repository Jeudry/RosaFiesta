package com.example.rosafiesta.navigation.graphs.products

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.core.presentation.designsystem.components.RFFloatingAddBtn
import com.example.core.presentation.ui.UiText
import com.example.products.presentation.views.ProductsListSR
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

fun NavGraphBuilder.productsListComposable(navHostData: NavHostData) {
  val route = UiText.StringResource(R.string.products_list_route).asString(navHostData.context)
  val addProductRoute = UiText.StringResource(R.string.product_add_route).asString(navHostData.context)
  val productDetailRouteNavigate = UiText.StringResource(R.string.product_detail_route_navigate).asString(navHostData.navController.context)
  return composable(route) {
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = UiText.StringResource(R.string.products_list_title).asString(navHostData.context),
          addBtn = {
            RFFloatingAddBtn(onClick = {
              navHostData.navController.navigate(addProductRoute)
            })
          }
        )
      )
    }
    ProductsListSR(
      onProductDetail = { productId ->
        navHostData.navController.navigate("${productDetailRouteNavigate}${productId}")
      }
    )
  }
}