package com.example.rosafiesta.navigation.graphs.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.example.core.presentation.ui.UiText
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData

fun NavGraphBuilder.authGraph(
  navHostData: NavHostData
) {
  return navigation(
    startDestination = UiText.StringResource(R.string.login_route).asString(navHostData.context),
    route = UiText.StringResource(R.string.auth_route).asString(navHostData.context)
  ) {
    registerComposable(navHostData)
    loginComposable(navHostData)
  }
}