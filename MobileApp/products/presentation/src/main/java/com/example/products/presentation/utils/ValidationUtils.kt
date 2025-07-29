package com.example.products.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.core.domain.validators.ValidationResult
import com.example.products.presentation.R

/**
 * Mapas de plantillas de mensajes para cada tipo de error
 */
private val errorMessageTemplates = mapOf(
    "required" to R.string.error_field_required,
    "minLength" to R.string.error_min_length,
    "maxLength" to R.string.error_max_length,
    "minValue" to R.string.error_min_value,
    "maxValue" to R.string.error_max_value,
    "pattern" to R.string.error_invalid_format,
    "positiveValue" to R.string.error_positive_value
)

/**
 * Función utilitaria que convierte resultados de validación en mensajes de error interpolados
 * Usa el sistema nativo de Android para interpolación
 */
@Composable
fun ValidationResult.toErrorMessage(): String? {
    if (isValid) return null

    val context = LocalContext.current

    // Obtener el primer error para mostrar
    val firstError = errors.entries.firstOrNull() ?: return "Error de validación"
    val errorKey = firstError.key
    val errorValue = firstError.value

    // Obtener la plantilla del mensaje
    val messageTemplateId = errorMessageTemplates[errorKey]
        ?: return "Error en campo: $errorKey"

    return when (errorValue) {
        is Boolean -> {
            // Error simple sin parámetros (ej: required, positiveValue)
            context.getString(messageTemplateId)
        }
        is Map<*, *> -> {
            // Error con metadatos - usar sistema nativo de Android
            val params = errorValue as Map<String, Any>
            interpolateWithNativeSystem(context, messageTemplateId, params)
        }
        else -> context.getString(messageTemplateId)
    }
}

/**
 * Usa el sistema nativo de Android para interpolación
 */
private fun interpolateWithNativeSystem(
    context: android.content.Context,
    messageTemplateId: Int,
    params: Map<String, Any>
): String {
    return when {
        // Para errores de longitud: %1$d = requiredLength, %2$d = actualValue
        params.containsKey("requiredLength") && params.containsKey("actualValue") -> {
            context.getString(
                messageTemplateId,
                params["requiredLength"],
                params["actualValue"]
            )
        }
        // Para errores de valor: %1$s = requiredValue, %2$s = actualValue
        params.containsKey("requiredValue") && params.containsKey("actualValue") -> {
            context.getString(
                messageTemplateId,
                params["requiredValue"],
                params["actualValue"]
            )
        }
        // Fallback a mensaje sin parámetros
        else -> context.getString(messageTemplateId)
    }
}
