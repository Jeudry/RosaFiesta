package com.example.rosafiesta

import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.example.home.presentation.DashboardScreenRoot
import com.example.products.presentation.views.ProductAddSR
import com.example.products.presentation.views.ProductsListSR

@ExperimentalFoundationApi
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
    productGraph(navController)
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
      DashboardScreenRoot(
        onProductsList = {
          navController.navigate("products-list")
        },
        onProductDetail = {
          navController.navigate("product-detail")
        },
        onProductAdd = {
          navController.navigate("products-add")
        }
      )
    }
  }
}

@ExperimentalFoundationApi
private fun NavGraphBuilder.productGraph(navController: NavHostController) {
  navigation(
    startDestination = "products-list",
    route = "products"
  ) {
    composable("products-add"){
       ProductAddSR(
         onSuccessfulAdd = {
           navController.navigate("dashboard")
         }
       )
    }
    composable("products-list"){
      ProductsListSR()
    }
  }
}