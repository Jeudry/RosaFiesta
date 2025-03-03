package com.example.rosafiesta

import com.example.rosafiesta.navigation.models.NavState

data class MainState(
  val isLoggedIn: Boolean = false,
  val isCheckingAuth: Boolean = false,
  val navState: NavState? = null
)