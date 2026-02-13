package com.example.home.presentation.events

import com.example.core.presentation.ui.UiText
import com.example.products.presentation.events.ProductDetailEvent

sealed interface DashboardEvent {
    data object DeleteSuccess : DashboardEvent
    data class Error(val error: UiText) : DashboardEvent
}