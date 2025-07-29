package com.example.core.domain.validators

/**
 * Resultado base para cualquier validación
 */
interface ValidationResult {
    val isValid: Boolean
    val errors: Map<String, Any>
}

/**
 * Resultado de validación para campos de texto
 */
open class TextValidationResult(
    val notEmpty: Boolean = false,
    val minLengthValid: Boolean = false,
    val maxLengthValid: Boolean = false,
    val patternValid: Boolean = true,
    val required: Boolean = true,
    protected val actualLength: Int = 0,
    protected val requiredMinLength: Int = 0,
    protected val requiredMaxLength: Int = Int.MAX_VALUE
) : ValidationResult {
    override val isValid: Boolean get() = 
        if (required) notEmpty && minLengthValid && maxLengthValid && patternValid
        else !notEmpty || (minLengthValid && maxLengthValid && patternValid)

    override val errors: Map<String, Any> get() {
        val errorMap = mutableMapOf<String, Any>()

        if (required && !notEmpty) {
            errorMap["required"] = true
        }
        if (notEmpty || !required) {
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
            if (!patternValid) {
                errorMap["pattern"] = true
            }
        }

        return errorMap
    }
}

/**
 * Resultado de validación para campos numéricos
 */
open class NumericValidationResult(
    val positiveValue: Boolean = false,
    val minRangeValid: Boolean = false,
    val maxRangeValid: Boolean = false,
    val required: Boolean = true,
    val hasValue: Boolean = false,
    protected val actualValue: Number = 0,
    protected val requiredMinValue: Number = Double.MIN_VALUE,
    protected val requiredMaxValue: Number = Double.MAX_VALUE
) : ValidationResult {
    override val isValid: Boolean get() = 
        if (required) hasValue && positiveValue && minRangeValid && maxRangeValid
        else !hasValue || (positiveValue && minRangeValid && maxRangeValid)

    override val errors: Map<String, Any> get() {
        val errorMap = mutableMapOf<String, Any>()

        if (required && !hasValue) {
            errorMap["required"] = true
        }
        if (hasValue) {
            if (!positiveValue) {
                errorMap["positiveValue"] = true
            }
            if (!minRangeValid) {
                errorMap["minValue"] = mapOf(
                    "actualValue" to actualValue,
                    "requiredValue" to requiredMinValue
                )
            }
            if (!maxRangeValid) {
                errorMap["maxValue"] = mapOf(
                    "actualValue" to actualValue,
                    "requiredValue" to requiredMaxValue
                )
            }
        }

        return errorMap
    }
}

/**
 * Resultado de validación simple (solo válido/inválido)
 */
data class SimpleValidationResult(
    val valid: Boolean = false,
    private val customErrors: Map<String, Any> = emptyMap()
) : ValidationResult {
    override val isValid: Boolean get() = valid
    override val errors: Map<String, Any> get() = customErrors
}
