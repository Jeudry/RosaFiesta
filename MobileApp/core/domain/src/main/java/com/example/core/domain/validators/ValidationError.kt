package com.example.core.domain.validators

/**
 * Representa un error de validación específico con metadatos
 */
sealed class ValidationError {
    data object Required : ValidationError()
    data object NotEmpty : ValidationError()
    data object Pattern : ValidationError()

    data class MinLength(
        val actualValue: Int,
        val requiredLength: Int
    ) : ValidationError()

    data class MaxLength(
        val actualValue: Int,
        val requiredLength: Int
    ) : ValidationError()

    data class MinValue(
        val actualValue: Number,
        val requiredValue: Number
    ) : ValidationError()

    data class MaxValue(
        val actualValue: Number,
        val requiredValue: Number
    ) : ValidationError()

    data object PositiveValue : ValidationError()
    data object NegativeValue : ValidationError()

    data class Custom(
        val key: String,
        val parameters: Map<String, Any> = emptyMap()
    ) : ValidationError()
}

/**
 * Contiene todos los errores de validación organizados por tipo
 */
data class ValidationErrors(
    private val errorMap: Map<String, ValidationError> = emptyMap()
) {
    val isEmpty: Boolean get() = errorMap.isEmpty()
    val isNotEmpty: Boolean get() = errorMap.isNotEmpty()
    val errors: Map<String, ValidationError> get() = errorMap

    fun hasError(key: String): Boolean = errorMap.containsKey(key)
    fun getError(key: String): ValidationError? = errorMap[key]

    companion object {
        fun of(vararg errors: Pair<String, ValidationError>): ValidationErrors {
            return ValidationErrors(errors.toMap())
        }

        fun empty(): ValidationErrors = ValidationErrors()
    }
}
