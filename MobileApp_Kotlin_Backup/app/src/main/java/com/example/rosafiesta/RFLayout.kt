@file:OptIn(ExperimentalFoundationApi::class)

package com.example.rosafiesta

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.core.presentation.designsystem.components.RFScaffold
import com.example.core.presentation.ui.UiText
import com.example.rosafiesta.navigation.NavigationRoot
import com.example.rosafiesta.navigation.getBottomNavigationItems
import com.example.rosafiesta.navigation.helpers.GetNavigationBottomBar
import com.example.rosafiesta.navigation.helpers.GetToolBar
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

@Suppress("UNUSED_EXPRESSION")
@ExperimentalMaterial3Api
@Composable
fun RFLayout(
  viewModel: MainViewModel
) {
  val current =  LocalContext.current
  
  val startDestination = if (
    viewModel.state.isLoggedIn
  ){
    NavState(
      route = UiText.StringResource(R.string.home_route).asString(current),
      title = UiText.StringResource(R.string.home_title).asString(current),
      showBackBtn = false,
      bottomNavItems = getBottomNavigationItems()
    )
  } else {
    NavState(
      route = UiText.StringResource(R.string.auth_route).asString(current),
      title = UiText.StringResource(R.string.intro_title).asString(current)
    )
  }
  
  LaunchedEffect(Unit){
    viewModel.setNavigationState(startDestination)
  }
  
  val navController = rememberNavController()
  
  val topAppBarState = rememberTopAppBarState()
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
    state = topAppBarState
  )
  if(viewModel.state.navState != null ) {
    val navState = viewModel.state.navState!!
    RFScaffold(
      bottomBar = {
        if (navState.isBottomNavVisible) {
          GetNavigationBottomBar(
            bottomNavigationItems = navState.bottomNavItems,
            route = navState.route,
            navController = navController
          )
        }
      },
      topAppBar = {
        if(navState.isTopVisible){
          GetToolBar(
            navState,
            scrollBehavior,
            onBackClick = { navController.popBackStack() }
          )
        }
      },
      floatingActionButton = navState.addBtn
    ) { padding ->
      Column(
        modifier = Modifier.padding(padding).nestedScroll(scrollBehavior.nestedScrollConnection)
      ) {
        NavigationRoot(
          navHostData = NavHostData(
            navController = navController,
            mainViewModel = viewModel,
            startDestination = startDestination.route,
            context = LocalContext.current
          )
        )
      }
    }
  }
}