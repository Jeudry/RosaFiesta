package com.example.products.presentation.actions

sealed interface ProductAddAction {
    data object OnCancelClick : ProductAddAction
    data object OnAddClick : ProductAddAction
}