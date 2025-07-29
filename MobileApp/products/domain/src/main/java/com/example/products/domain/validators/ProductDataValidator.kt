package com.example.products.domain.validators

class ProductDataValidator{

    fun isValidName(name: String): ProductNameVS {
        return ProductNameVS.create(
            value = name,
            minLength = MIN_NAME_LENGTH,
            maxLength = MAX_NAME_LENGTH
        )
    }

    fun isValidDescription(description: String?): ProductDescriptionVS {
        return ProductDescriptionVS.create(
            value = description,
            minLength = MIN_DESCRIPTION_LENGTH,
            maxLength = MAX_DESCRIPTION_LENGTH
        )
    }

    fun isValidPrice(price: Double): ProductPriceVS {
        return ProductPriceVS(
            value = price,
            minValue = MIN_PRICE_LENGTH
        )
    }

    fun isValidRentalPrice(price: Double?): ProductRentalPriceVS {
        return ProductRentalPriceVS(value = price)
    }

    fun isValidStock(stock: Int): ProductStockVS {
        return ProductStockVS(value = stock)
    }
    
    fun isValidColor(color: Long): ProductColorVS {
        return ProductColorVS(value = color)
    }
    
    fun isValidSize(size: Double): ProductSizeVS {
        return ProductSizeVS(
            value = size,
            maxValue = MAX_SIZE_LENGTH
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