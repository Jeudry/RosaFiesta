package com.example.rosafiesta.navigation.models

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
  val route: String? = null,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val hasNews: Boolean = false,
  val badgeCount: Int? = null
)