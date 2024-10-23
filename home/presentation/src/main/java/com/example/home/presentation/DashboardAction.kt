package com.example.home.presentation

import com.example.core.presentation.ui.BaseProductAction

sealed interface DashboardAction: BaseProductAction {
    
    data object OnProductsList: DashboardAction
}