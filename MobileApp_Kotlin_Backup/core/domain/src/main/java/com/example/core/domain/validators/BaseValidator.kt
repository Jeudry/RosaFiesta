package com.example.core.domain.validators

import java.util.regex.Pattern

/**
 * Validador base que contiene las validaciones más comunes
 * Todas las validaciones específicas pueden heredar de esta clase
 */
open class BaseValidator {

    /**
     * Valida un campo de texto con todas las reglas comunes
     */
    protected fun validateText(
        value: String?,
        minLength: Int = 0,
        maxLength: Int = Int.MAX_VALUE,
        pattern: String? = null,
        required: Boolean = true
    ): TextValidationResult {
        val text = value ?: ""
        val notEmpty = text.isNotEmpty()
        val minLengthValid = text.length >= minLength
        val maxLengthValid = text.length <= maxLength
        val patternValid = pattern?.let {
            Pattern.compile(it).matcher(text).matches()
        } ?: true

        return TextValidationResult(
            notEmpty = notEmpty,
            minLengthValid = minLengthValid,
            maxLengthValid = maxLengthValid,
            patternValid = patternValid,
            required = required
        )
    }

    /**
     * Valida un campo numérico (Double) con todas las reglas comunes
     */
    protected fun validateNumeric(
        value: Double?,
        minValue: Double = Double.MIN_VALUE,
        maxValue: Double = Double.MAX_VALUE,
        allowNegative: Boolean = false,
        required: Boolean = true
    ): NumericValidationResult {
        val hasValue = value != null
        val actualValue = value ?: 0.0
        val positiveValue = if (allowNegative) true else actualValue > 0
        val minRangeValid = actualValue >= minValue
        val maxRangeValid = actualValue <= maxValue

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = minRangeValid,
            maxRangeValid = maxRangeValid,
            required = required,
            hasValue = hasValue
        )
    }

    /**
     * Valida un campo numérico entero (Int) con todas las reglas comunes
     */
    protected fun validateNumeric(
        value: Int?,
        minValue: Int = Int.MIN_VALUE,
        maxValue: Int = Int.MAX_VALUE,
        allowNegative: Boolean = false,
        required: Boolean = true
    ): NumericValidationResult {
        val hasValue = value != null
        val actualValue = value ?: 0
        val positiveValue = if (allowNegative) true else actualValue > 0
        val minRangeValid = actualValue >= minValue
        val maxRangeValid = actualValue <= maxValue

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = minRangeValid,
            maxRangeValid = maxRangeValid,
            required = required,
            hasValue = hasValue
        )
    }

    /**
     * Valida un campo numérico largo (Long) con todas las reglas comunes
     */
    protected fun validateNumeric(
        value: Long?,
        minValue: Long = Long.MIN_VALUE,
        maxValue: Long = Long.MAX_VALUE,
        allowNegative: Boolean = false,
        required: Boolean = true
    ): NumericValidationResult {
        val hasValue = value != null
        val actualValue = value ?: 0L
        val positiveValue = if (allowNegative) true else actualValue > 0
        val minRangeValid = actualValue >= minValue
        val maxRangeValid = actualValue <= maxValue

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = minRangeValid,
            maxRangeValid = maxRangeValid,
            required = required,
            hasValue = hasValue
        )
    }

    /**
     * Valida si un campo requerido no está vacío
     */
    protected fun validateRequired(value: Any?): SimpleValidationResult {
        return SimpleValidationResult(valid = value != null)
    }

    /**
     * Valida un patrón regex
     */
    protected fun validatePattern(value: String?, pattern: String): SimpleValidationResult {
        val text = value ?: ""
        val valid = Pattern.compile(pattern).matcher(text).matches()
        return SimpleValidationResult(valid = valid)
    }

    /**
     * Valida que un valor esté dentro de un rango
     */
    protected fun <T : Comparable<T>> validateRange(
        value: T,
        minValue: T,
        maxValue: T
    ): SimpleValidationResult {
        val valid = value in minValue..maxValue
        return SimpleValidationResult(valid = valid)
    }

    /**
     * Combina múltiples resultados de validación
     */
    protected fun combineValidations(vararg results: ValidationResult): SimpleValidationResult {
        val allValid = results.all { it.isValid }
        return SimpleValidationResult(valid = allValid)
    }
}
