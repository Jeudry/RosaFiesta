package com.example.rosafiesta.navigation.graphs.products

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.core.presentation.ui.UiText
import com.example.products.presentation.views.ProductDetailSR
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState
import java.util.*

fun NavGraphBuilder.productDetailComposable(navHostData: NavHostData) {
  val route = UiText.StringResource(R.string.product_detail_route).asString(navHostData.navController.context)
  val productIdString = UiText.StringResource(R.string.product_id).asString(navHostData.navController.context)
  
  return composable(
    route = route,
    arguments = listOf(navArgument(productIdString) { type = NavType.StringType })
  ){backStackEntry ->
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = "",
          showBackBtn = true,
          showLogo = false
        ))
    }
    
    val productId = backStackEntry.arguments?.getString(productIdString)
    if(productId != null){
      ProductDetailSR(productId = UUID.fromString(productId),
        onProductName = {navHostData.mainViewModel.setTitle(it)
      })
    } else {
      navHostData.navController.navigateUp()
    }
  }
}