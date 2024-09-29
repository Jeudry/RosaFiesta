package com.example.rosafiesta

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.example.auth.presentation.intro.IntroScreenRoot
import com.example.auth.presentation.login.LoginScreenRoot
import com.example.auth.presentation.register.RegisterScreenRoot

@Composable
fun NavigationRoot(
  navController: NavHostController,
  isLoggedIn: Boolean
) {
  NavHost(
    navController = navController,
    startDestination = if (isLoggedIn) "home" else "auth"
  ) {
    authGraph(navController)
    homeGraph(navController)
  }
}

private fun NavGraphBuilder.authGraph(
  navController: NavHostController
) {
  navigation(startDestination = "intro", route = "auth") {
    composable(route = "intro") {
      IntroScreenRoot(
        onSignUpClick = {
          navController.navigate("register")
        },
        onSignInClick = {
          navController.navigate("login")
        }
      )
    }
    
    composable(route = "register") {
      RegisterScreenRoot(
        onSignInClick = {
          navController.navigate("login") {
            popUpTo("register") {
              inclusive = true
              saveState = true
            }
            restoreState = true
          }
        },
        onSuccessfulRegistration = {
          navController.navigate("login")
        }
      )
    }
    
    composable("login") {
      LoginScreenRoot(
        onLoginSuccess = {
          navController.navigate("home") {
            popUpTo("auth") {
              inclusive = true
            }
          }
        },
        onSignUpClick = {
          navController.navigate("register") {
            popUpTo("login") {
              inclusive = true
              saveState = true
            }
            restoreState = true
          }
        }
      )
    }
  }
}

private fun NavGraphBuilder.homeGraph(
  navController: NavHostController
) {
  navigation(
    startDestination = "dashboard",
    route = "home"
  ) {
    composable("dashboard") {
      RunOverviewScreenRoot(
        onStartRunClick = {
          navController.navigate("")
        },
        onLogoutClick = {
          navController.navigate("auth") {
            popUpTo("run") {
              inclusive = true
            }
          }
        }
      )
    }
  }
}