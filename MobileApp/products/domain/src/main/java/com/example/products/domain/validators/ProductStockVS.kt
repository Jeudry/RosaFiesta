package com.example.products.domain.validators

data class ProductStockVS(
    val positiveValue: Boolean = false
){
    val isValid = positiveValue
}