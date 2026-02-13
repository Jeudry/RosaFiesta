package com.example.rosafiesta.navigation.models

import androidx.compose.runtime.Composable

data class NavState(
  val route: String,
  val title: String,
  val showBackBtn: Boolean = true,
  val showLogo: Boolean = true,
  val bottomNavItems: List<BottomNavigationItem> = emptyList(),
  val isTopVisible: Boolean = true,
  val addBtn: @Composable () -> Unit = {},
){
    val isBottomNavVisible: Boolean
        get() = bottomNavItems.isNotEmpty()
}