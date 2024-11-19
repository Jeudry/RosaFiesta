package com.example.products.presentation.events

import com.example.core.presentation.ui.UiText

sealed interface ProductDetailEvent {
    data object DeleteSuccess : ProductDetailEvent
    data class Error(val error: UiText) : ProductDetailEvent
}