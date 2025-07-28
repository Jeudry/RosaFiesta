package com.example.rosafiesta

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.AuthService
import com.example.rosafiesta.navigation.models.NavState
import kotlinx.coroutines.launch

class MainViewModel(
  private val authService: AuthService
) : ViewModel() {
  
  var state by mutableStateOf(MainState(
  
  ))
    private set

  init {
    checkAuthenticationStatus()
  }

  private fun checkAuthenticationStatus() {
    viewModelScope.launch {
      state = state.copy(isCheckingAuth = true)

      try {
        val isAuthenticated = authService.isUserAuthenticated()
        state = state.copy(
          isLoggedIn = isAuthenticated,
          isCheckingAuth = false
        )
      } catch (e: Exception) {
        // En caso de error, asumimos que no está autenticado
        state = state.copy(
          isLoggedIn = false,
          isCheckingAuth = false
        )
      }
    }
  }

  fun refreshAuthStatus() {
    checkAuthenticationStatus()
  }

  fun setTitle(
    title: String
  ){
    state = state.copy(
      navState = state.navState!!.copy(
        title = title
      )
    )
  }
  
  fun setNavigationState(newNavState: NavState) {
    state = state.copy(
      navState = newNavState
    )
  }
}