@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.rosafiesta.navigation.graphs.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.core.presentation.designsystem.RunIcon
import com.example.core.presentation.designsystem.components.RFFloatingActionBtn
import com.example.core.presentation.ui.UiText
import com.example.home.presentation.DashboardScreenRoot
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

fun NavGraphBuilder.dashboardComposable(navHostData: NavHostData) {
  val route = UiText.StringResource(R.string.dashboard_route).asString(navHostData.navController.context)
  val productsListRoute = UiText.StringResource(R.string.products_list_route).asString(navHostData.navController.context)
  val productDetailRouteNavigate = UiText.StringResource(R.string.product_detail_route_navigate).asString(navHostData.navController.context)
  val productAddRoute = UiText.StringResource(R.string.product_add_route).asString(navHostData.navController.context)
  
  return composable(route) {
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = UiText.StringResource(R.string.dashboard_title).asString(navHostData.navController.context),
          showBackBtn = false,
          addBtn = {
            RFFloatingActionBtn(
              icon = RunIcon,
              onClick = {
                navHostData.navController.navigate(productAddRoute)
              }
            )
          }
        ))
    }
    DashboardScreenRoot(
      onProductsList = {
        navHostData.navController.navigate(productsListRoute)
      },
      onProductDetail = { productId ->
        navHostData.navController.navigate("${productDetailRouteNavigate}${productId}")
      }
    )
  }
}