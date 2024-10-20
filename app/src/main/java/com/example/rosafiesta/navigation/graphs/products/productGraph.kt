package com.example.rosafiesta.navigation.graphs.products

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.example.core.presentation.ui.UiText
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData

@ExperimentalFoundationApi
fun NavGraphBuilder.productGraph(navHostData: NavHostData) {
  return navigation(
    startDestination = UiText.StringResource(R.string.products_list_route).asString(navHostData.context),
    route = UiText.StringResource(R.string.products_route).asString(navHostData.context)
  ) {
    productAddComposable(navHostData)
    productsListComposable(navHostData)
  }
}