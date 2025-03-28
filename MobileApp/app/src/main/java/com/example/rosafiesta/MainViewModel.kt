package com.example.rosafiesta

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.SessionStorage
import com.example.rosafiesta.navigation.models.NavState
import kotlinx.coroutines.launch

class MainViewModel(
  private val sessionStorage: SessionStorage
) : ViewModel() {
  
  var state by mutableStateOf(MainState(
  
  ))
    private set

  init {
    viewModelScope.launch {
      state = state.copy(isCheckingAuth = true)
      state = state.copy(isLoggedIn = sessionStorage.get() != null)
      state = state.copy(isCheckingAuth = false)
    }
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