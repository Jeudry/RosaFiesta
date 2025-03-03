package com.example.products.presentation.events

import com.example.core.presentation.ui.UiText

sealed interface ProductAddEvent {
    data object AddSuccess : ProductAddEvent
    data class Error(val error: UiText) : ProductAddEvent
}