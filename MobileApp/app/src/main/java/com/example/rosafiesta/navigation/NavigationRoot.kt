package com.example.rosafiesta.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import com.example.rosafiesta.navigation.graphs.auth.authGraph
import com.example.rosafiesta.navigation.graphs.home.homeGraph
import com.example.rosafiesta.navigation.graphs.products.productGraph
import com.example.rosafiesta.navigation.models.NavHostData

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun NavigationRoot(
  navHostData: NavHostData
  ) {
    NavHost(
      navController = navHostData.navController,
      startDestination = navHostData.startDestination
    ) {
      authGraph(navHostData)
      homeGraph(navHostData)
      productGraph(navHostData)
    }
}