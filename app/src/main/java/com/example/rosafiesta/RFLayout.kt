@file:OptIn(ExperimentalFoundationApi::class)

package com.example.rosafiesta

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.core.presentation.designsystem.LogoIcon
import com.example.core.presentation.designsystem.components.RFScaffold
import com.example.core.presentation.designsystem.components.RFToolbar
import com.example.rosafiesta.navigation.NavigationBar
import com.example.rosafiesta.navigation.NavigationRoot
import com.example.rosafiesta.navigation.getBottomNavigationItems
import com.example.rosafiesta.navigation.models.NavHostData
import com.example.rosafiesta.navigation.models.NavState

@Suppress("UNUSED_EXPRESSION")
@ExperimentalMaterial3Api
@Composable
fun RFLayout(
  viewModel: MainViewModel
) {
  val bottomNavigationItems = getBottomNavigationItems()
  val startDestination = if (
    viewModel.state.isLoggedIn
  ){
    NavState(
      route = stringResource(R.string.home_route),
      title = stringResource(R.string.home_title),
      showBackBtn = false
    )
  } else {
    NavState(
      route = stringResource(R.string.intro_route),
      title = stringResource(R.string.intro_title)
    )
  }
  
  viewModel.setNavigationState(startDestination)
  
  val navController = rememberNavController()
  
  lifecycleScope
  val navigationBar = NavigationBar(
    bottomNavigationItems = bottomNavigationItems,
    route = viewModel.state.navState!!.route,
    navController = navController
  )
  
  val topAppBarState = rememberTopAppBarState()
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
    state = topAppBarState
  )
  RFScaffold(
    bottomBar = {navigationBar},
    topAppBar = {
      RFToolbar(
        showBackButton = viewModel.state.navState!!.showBackBtn,
        title = viewModel.state.navState!!.title,
        scrollBehavior = scrollBehavior,
        startContent = {
          Icon(
            imageVector = LogoIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
          )
        }
      )
    },
    floatingActionButton = { viewModel.state.navState!!.addBtn }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
    ) {
      NavigationRoot(
        navHostData = NavHostData(
          navController = navController,
          mainViewModel = viewModel,
          startDestination = startDestination.route,
          context = LocalContext.current,
          scrollBehavior = scrollBehavior
        )
      )
    }
  }
}