package com.example.products.domain.validators

import com.example.core.domain.validators.PatternValidator
import java.util.prefs.Preferences.MAX_NAME_LENGTH

class ProductDataValidator{

    fun isValidName(name: String): ProductNameVS {
        val minLengthValid = name.length >= MIN_NAME_LENGTH
        val maxLengthValid = name.length <= MAX_NAME_LENGTH
        val notEmpty = name.isNotEmpty()

        return ProductNameVS(
            minLengthValid = minLengthValid,
            maxLengthValid = maxLengthValid,
            notEmpty = notEmpty
        )
    }

    fun isValidDescription(description: String?): ProductDescriptionVS {
        var minLengthValid = false
        var maxLengthValid = false

        val notEmpty = !description.isNullOrEmpty()

        if(notEmpty) {
            (description!!.length >= MIN_DESCRIPTION_LENGTH).also { minLengthValid = it }
            (description.length <= MAX_DESCRIPTION_LENGTH).also { maxLengthValid = it }
        }

        return ProductDescriptionVS(
            minLengthValid = minLengthValid,
            maxLengthValid = maxLengthValid,
            notEmpty = notEmpty
        )
    }

    fun isValidPrice(price: Double): ProductPriceVS {
        val minLengthValid = price >= MIN_PRICE_LENGTH
        val positiveValue = price > 0

        return ProductPriceVS(
            minLengthValid = minLengthValid,
            positiveValue = positiveValue
        )
    }

    fun isValidRentalPrice(price: Double?): ProductRentalPriceVS {
        var positiveValue = false

        if(price != null) {
            (price > 0).also { positiveValue = it }
        }

        val hasValue = price != null

        return ProductRentalPriceVS(
            positiveValue = positiveValue,
            hasValue = hasValue
        )
    }

    fun isValidStock(stock: Int): ProductStockVS {
        val positiveValue = stock > 0
        return ProductStockVS(
            positiveValue = positiveValue
        )
    }

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 256
        const val MIN_DESCRIPTION_LENGTH = 5
        const val MAX_DESCRIPTION_LENGTH = 10000
        const val MIN_PRICE_LENGTH = 5
    }
}