package com.example.rosafiesta.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.rosafiesta.navigation.models.BottomNavigationItem

@Composable
fun NavigationBar(
  bottomNavigationItems: List<BottomNavigationItem>,
  route: String,
  navController: NavHostController
){
  androidx.compose.material3.NavigationBar {
    bottomNavigationItems.forEach { item ->
      val selected = route == item.route
      NavigationBarItem(
        selected = selected,
        label = { Text(text = item.title) },
        alwaysShowLabel = false,
        icon = {
          BadgedBox(
            badge = {
              if (item.badgeCount != null && item.badgeCount > 0) {
                Badge {
                  Text(text = item.badgeCount.toString())
                }
              } else if (item.hasNews) {
                Badge()
              }
            }
          ) {
            Icon(
              contentDescription = item.title,
              imageVector = if (selected) item.selectedIcon else item.unselectedIcon
            )
          }
        },
        onClick = {
          if (item.route != null) {
            navController.navigate(item.route)
          } else {
            navController.navigateUp()
          }
        }
      )
    }
  }
}