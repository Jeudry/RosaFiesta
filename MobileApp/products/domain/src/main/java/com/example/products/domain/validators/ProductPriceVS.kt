package com.example.products.domain.validators

data class ProductPriceVS(
    val minLengthValid: Boolean = false,
    val positiveValue: Boolean = false
){
    val isValid = minLengthValid && positiveValue
}