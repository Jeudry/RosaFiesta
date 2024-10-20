@file:OptIn(ExperimentalFoundationApi::class)

package com.example.products.presentation.states

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.example.auth.domain.PasswordValidationState
import com.example.products.domain.validators.ProductDescriptionVS
import com.example.products.domain.validators.ProductNameVS
import com.example.products.domain.validators.ProductPriceVS
import com.example.products.domain.validators.ProductRentalPriceVS
import com.example.products.domain.validators.ProductStockVS
import java.time.ZonedDateTime

data class ProductAddState(
    val name: TextFieldState = TextFieldState(),
    val isNameValid: ProductNameVS = ProductNameVS(),
    val description: TextFieldState = TextFieldState(),
    val isDescriptionValid: ProductDescriptionVS = ProductDescriptionVS(),
    val price: TextFieldState = TextFieldState(),
    val isPriceValid: ProductPriceVS = ProductPriceVS(),
    val rentalPrice: TextFieldState = TextFieldState(),
    val isRentalPriceValid: ProductRentalPriceVS = ProductRentalPriceVS(),
    val imageUrl: TextFieldState = TextFieldState(),
    val stock: TextFieldState = TextFieldState(),
    val isStockValid: ProductStockVS = ProductStockVS(),
    val isAdding: Boolean = false,
    val canAdd: Boolean = false
){
    fun isValid(): Boolean {
        return isNameValid.isValid
                && isDescriptionValid.isValid
                && isPriceValid.isValid
                && isRentalPriceValid.isValid
                && isStockValid.isValid
                && !isAdding
    }
}