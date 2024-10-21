package com.example.rosafiesta.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.rosafiesta.R
import com.example.rosafiesta.navigation.models.BottomNavigationItem

@Composable
fun getBottomNavigationItems(): List<BottomNavigationItem> {
  return listOf(
    BottomNavigationItem(
      title = stringResource(R.string.home_title),
      selectedIcon = Icons.Filled.Home,
      unselectedIcon = Icons.Outlined.Home,
      hasNews = false,
      route = stringResource(R.string.home_route)
    ),
    BottomNavigationItem(
      title = stringResource(R.string.products),
      selectedIcon = Icons.Filled.ShoppingBag,
      unselectedIcon = Icons.Outlined.ShoppingBag,
      route = stringResource(R.string.products_route)
    ),
    BottomNavigationItem(
      title = stringResource(R.string.analytics),
      selectedIcon = Icons.Filled.Analytics,
      unselectedIcon = Icons.Outlined.Analytics,
      route = stringResource(R.string.analytics_route)
    ),
    BottomNavigationItem(
      title = stringResource(R.string.menu),
      selectedIcon = Icons.Filled.Menu,
      unselectedIcon = Icons.Outlined.Menu
    )
  )
}