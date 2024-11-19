package com.example.products.domain.validators

data class ProductSizeVS(
  val positiveValue: Boolean = false,
  val maxLengthValid: Boolean = false
){
  val isValid = positiveValue
}