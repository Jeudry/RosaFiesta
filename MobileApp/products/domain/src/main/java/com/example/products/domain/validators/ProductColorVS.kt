package com.example.products.domain.validators

data class ProductColorVS(
  val positiveValue: Boolean = false
){
  val isValid = positiveValue
}