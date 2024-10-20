@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rosafiesta.navigation.models

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.navigation.NavHostController
import com.example.rosafiesta.MainViewModel

data class NavHostData(
  val navController: NavHostController,
  val mainViewModel: MainViewModel,
  val startDestination: String,
  val context: Context,
  val scrollBehavior: TopAppBarScrollBehavior
)