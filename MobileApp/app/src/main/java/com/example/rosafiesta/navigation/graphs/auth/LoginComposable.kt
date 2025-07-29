package com.example.rosafiesta.navigation.graphs.auth

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.auth.presentation.login.LoginScreenRoot
import com.example.core.presentation.ui.UiText
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

fun NavGraphBuilder.loginComposable(
  navHostData: NavHostData
) {
  val route = UiText.StringResource(R.string.login_route).asString(navHostData.context)
  val registerRoute = UiText.StringResource(R.string.register_route).asString(navHostData.context)
  val homeRoute = UiText.StringResource(R.string.home_route).asString(navHostData.context)
  val authRoute = UiText.StringResource(R.string.auth_route).asString(navHostData.context)
  
  return composable(route) {
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = UiText.StringResource(R.string.login_title).asString(navHostData.context),
          showBackBtn = false
        )
      )
    }
    LoginScreenRoot(
      onLoginSuccess = {
        navHostData.navController.navigate(homeRoute) {
          popUpTo(authRoute) {
            inclusive = true
          }
        }
      },
      onSignUpClick = {
        navHostData.navController.navigate(registerRoute) {
          popUpTo(route) {
            inclusive = true
            saveState = true
          }
          restoreState = true
        }
      }
    )
  }
}