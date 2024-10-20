package com.example.rosafiesta.navigation.graphs.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.example.core.presentation.ui.UiText
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData

fun NavGraphBuilder.homeGraph(
  navHostData: NavHostData
) {
  return navigation(
    startDestination = UiText.StringResource(R.string.dashboard_route).asString(navHostData.context),
    route = UiText.StringResource(R.string.home_route).asString(navHostData.context)
  ) {
    dashboardComposable(navHostData)
  }
}