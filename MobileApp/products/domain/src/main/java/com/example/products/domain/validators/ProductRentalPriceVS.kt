package com.example.products.domain.validators

data class ProductRentalPriceVS(
    val positiveValue: Boolean = false,
    val hasValue: Boolean = false
){
    val isValid = !hasValue || ( positiveValue)
}