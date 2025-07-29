package com.example.products.domain.validators

import com.example.core.domain.validators.ValidationResult

class ProductDescriptionVS(
    val notEmpty: Boolean = false,
    val minLengthValid: Boolean = false,
    val maxLengthValid: Boolean = false,
    private val actualLength: Int = 0,
    private val requiredMinLength: Int = 0,
    private val requiredMaxLength: Int = Int.MAX_VALUE
) : ValidationResult {
    override val isValid: Boolean get() =
        notEmpty.not() || (minLengthValid && maxLengthValid)

    override val errors: Map<String, Any> get() {
        val errorMap = mutableMapOf<String, Any>()

        if (notEmpty) {
            if (!minLengthValid) {
                errorMap["minLength"] = mapOf(
                    "actualValue" to actualLength,
                    "requiredLength" to requiredMinLength
                )
            }
            if (!maxLengthValid) {
                errorMap["maxLength"] = mapOf(
                    "actualValue" to actualLength,
                    "requiredLength" to requiredMaxLength
                )
            }
        }

        return errorMap
    }
}
