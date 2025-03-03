package com.example.rosafiesta.navigation.graphs.auth

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.auth.presentation.intro.IntroScreenRoot
import com.example.core.presentation.ui.UiText
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

fun NavGraphBuilder.introComposable(
  navHostData: NavHostData
) {
  val route = UiText.StringResource(R.string.intro_route).asString(navHostData.context)
  
  return composable(route = route) {
    LaunchedEffect(Unit) {
      navHostData.mainViewModel.setNavigationState(
        NavState(
          route = route,
          title = UiText.StringResource(R.string.intro_title).asString(navHostData.context),
          showBackBtn = false
        )
      )
    }
    IntroScreenRoot(
      onSignUpClick = {
        navHostData.navController.navigate(UiText.StringResource(R.string.register_route).asString(navHostData.context))
      },
      onSignInClick = {
        navHostData.navController.navigate(UiText.StringResource(R.string.login_route).asString(navHostData.context))
      }
    )
  }
}