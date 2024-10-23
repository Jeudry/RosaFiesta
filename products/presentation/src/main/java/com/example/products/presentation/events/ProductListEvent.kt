package com.example.products.presentation.events

import com.example.core.presentation.ui.UiText

sealed interface ProductListEvent {
  data object DeleteSuccess : ProductListEvent
  data class Error(val error: UiText) : ProductListEvent
}