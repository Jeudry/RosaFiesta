package com.example.rosafiesta.navigation.graphs.auth

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.auth.presentation.register.RegisterScreenRoot
import com.example.core.presentation.ui.UiText
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

fun NavGraphBuilder.registerComposable(
  navHostData: NavHostData
) {
  val route =
    UiText.StringResource(R.string.register_route).asString(navHostData.navController.context)
  val loginRoute =
    UiText.StringResource(R.string.login_route).asString(navHostData.navController.context)
  
  return composable(route = route) {
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = UiText.StringResource(R.string.register_title)
            .asString(navHostData.navController.context),
          bottomNavItems = emptyList(),
          isTopVisible = false
        )
      )
    }
    RegisterScreenRoot(
      onSignInClick = {
        navHostData.navController.navigate(loginRoute) {
          popUpTo(route) {
            inclusive = true
            saveState = true
          }
          restoreState = true
        }
      },
      onSuccessfulRegistration = {
        navHostData.navController.navigate(loginRoute)
      }
    )
  }
}