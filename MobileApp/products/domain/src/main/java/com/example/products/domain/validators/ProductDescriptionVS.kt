package com.example.products.domain.validators

data class ProductDescriptionVS (
    val minLengthValid: Boolean = false,
    val maxLengthValid: Boolean = false,
    val notEmpty: Boolean = false,
){
    val isValid: Boolean get() = !notEmpty || (minLengthValid && maxLengthValid)
}