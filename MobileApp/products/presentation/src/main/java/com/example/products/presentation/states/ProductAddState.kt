@file:OptIn(ExperimentalFoundationApi::class)

package com.example.products.presentation.states

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.example.core.domain.validators.NumericValidationResult
import com.example.core.domain.validators.TextValidationResult
import com.example.products.domain.validators.ProductDescriptionVS

data class ProductAddState(
    val name: TextFieldState = TextFieldState(),
    val isNameValid: TextValidationResult = TextValidationResult(),
    val description: TextFieldState = TextFieldState(),
    val isDescriptionValid: ProductDescriptionVS = ProductDescriptionVS(),
    val price: TextFieldState = TextFieldState(),
    val isPriceValid: NumericValidationResult = NumericValidationResult(),
    val rentalPrice: TextFieldState = TextFieldState(),
    val isRentalPriceValid: NumericValidationResult = NumericValidationResult(),
    val imageUrl: TextFieldState = TextFieldState(),
    val stock: TextFieldState = TextFieldState(),
    val isStockValid: NumericValidationResult = NumericValidationResult(),
    val color: TextFieldState = TextFieldState(),
    val isColorValid: NumericValidationResult = NumericValidationResult(),
    val size: TextFieldState = TextFieldState(),
    val isSizeValid: NumericValidationResult = NumericValidationResult(),
    val isAdding: Boolean = false,
    val canAdd: Boolean = false
) {
    fun isValid(): Boolean {
        return isNameValid.isValid &&
                isDescriptionValid.isValid &&
                isPriceValid.isValid &&
                isRentalPriceValid.isValid &&
                isStockValid.isValid &&
                isColorValid.isValid &&
                isSizeValid.isValid && !isAdding
    }
}