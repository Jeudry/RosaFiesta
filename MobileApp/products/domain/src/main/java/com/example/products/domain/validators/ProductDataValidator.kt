package com.example.products.domain.validators

import com.example.core.domain.validators.TextValidationResult
import com.example.core.domain.validators.NumericValidationResult

class ProductDataValidator {

    fun isValidName(name: String): TextValidationResult {
        val text = name.trim()
        val notEmpty = text.isNotEmpty()
        val minLengthValid = text.length >= MIN_NAME_LENGTH
        val maxLengthValid = text.length <= MAX_NAME_LENGTH

        return TextValidationResult(
            notEmpty = notEmpty,
            minLengthValid = minLengthValid,
            maxLengthValid = maxLengthValid,
            patternValid = true,
            required = true,
            actualLength = text.length,
            requiredMinLength = MIN_NAME_LENGTH,
            requiredMaxLength = MAX_NAME_LENGTH
        )
    }

    fun isValidDescription(description: String?): ProductDescriptionVS {
        val text = description?.trim() ?: ""
        val notEmpty = text.isNotEmpty()
        val minLengthValid = if (text.isNotEmpty()) text.length >= MIN_DESCRIPTION_LENGTH else true
        val maxLengthValid = text.length <= MAX_DESCRIPTION_LENGTH

        return ProductDescriptionVS(
            notEmpty = notEmpty,
            minLengthValid = minLengthValid,
            maxLengthValid = maxLengthValid,
            actualLength = text.length,
            requiredMinLength = MIN_DESCRIPTION_LENGTH,
            requiredMaxLength = MAX_DESCRIPTION_LENGTH
        )
    }

    fun isValidPrice(price: Double): NumericValidationResult {
        val positiveValue = price > 0
        val minRangeValid = price >= MIN_PRICE_LENGTH

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = minRangeValid,
            maxRangeValid = true,
            required = true,
            hasValue = true,
            actualValue = price,
            requiredMinValue = MIN_PRICE_LENGTH,
            requiredMaxValue = Double.MAX_VALUE
        )
    }

    fun isValidRentalPrice(price: Double?): NumericValidationResult {
        val hasValue = price != null
        val positiveValue = price?.let { it > 0 } ?: true

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = true,
            maxRangeValid = true,
            required = false, // Precio de renta es opcional
            hasValue = hasValue,
            actualValue = price ?: 0.0,
            requiredMinValue = 0.0,
            requiredMaxValue = Double.MAX_VALUE
        )
    }

    fun isValidStock(stock: Int): NumericValidationResult {
        val positiveValue = stock > 0

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = true,
            maxRangeValid = true,
            required = true,
            hasValue = true,
            actualValue = stock,
            requiredMinValue = 1,
            requiredMaxValue = Int.MAX_VALUE
        )
    }

    fun isValidColor(color: Long): NumericValidationResult {
        val positiveValue = color > 0

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = true,
            maxRangeValid = true,
            required = true,
            hasValue = true,
            actualValue = color,
            requiredMinValue = 1,
            requiredMaxValue = Long.MAX_VALUE
        )
    }

    fun isValidSize(size: Double): NumericValidationResult {
        val positiveValue = size > 0
        val maxRangeValid = size <= MAX_SIZE_LENGTH

        return NumericValidationResult(
            positiveValue = positiveValue,
            minRangeValid = true,
            maxRangeValid = maxRangeValid,
            required = true,
            hasValue = true,
            actualValue = size,
            requiredMinValue = 0.0,
            requiredMaxValue = MAX_SIZE_LENGTH
        )
    }

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 256
        const val MIN_DESCRIPTION_LENGTH = 5
        const val MAX_DESCRIPTION_LENGTH = 20000
        const val MIN_PRICE_LENGTH = 5.0
        const val MAX_SIZE_LENGTH = 99999.0
    }
}