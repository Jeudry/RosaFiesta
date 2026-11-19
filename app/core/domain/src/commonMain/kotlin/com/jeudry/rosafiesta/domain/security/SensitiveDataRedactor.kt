package com.jeudry.rosafiesta.domain.security

/**
 * Redacts personal data, credentials, bank references, and raw SQL details before logging or surfacing errors in UI.
 */
public object SensitiveDataRedactor {
    private val bearerRegex = Regex("""(?i)\bBearer\s+[A-Za-z0-9\-._~+/]+=*""")
    private val jwtRegex = Regex("""\beyJ[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\b""")
    private val localSessionRegex = Regex("""\brf-local\.[A-Za-z0-9_-]{8,}\b""")
    private val secretKvRegex = Regex("""(?i)\b(password|passwd|secret|token|api_key|apikey)\s*([:=])\s*("[^"]*"|'[^']*'|[^\s,;]+)""")
    private val emailRegex = Regex("""\b([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\.[A-Za-z]{2,})\b""")
    private val phoneRegex = Regex("""\b(809|829|849)[-\s]?(\d{3})[-\s]?(\d{4})\b""")
    private val bankRefRegex = Regex("""\b(BHD|POP|BR|EFE|AZU)-(\d{2,4}-)?(\d{3,8})\b""")
    private val pgUrlRegex = Regex("""(?i)postgres(?:ql)?://[^\s"']+""")
    private val sqlStateRegex = Regex("""(?i)(SQLSTATE\s+[0-9A-Z]{5}|pq:\s+[^\n]+)""")

    /**
     * Masks sensitive tokens, emails, Dominican phone numbers, bank references, and database connection URLs.
     */
    public fun redactText(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        var sanitized = bearerRegex.replace(trimmed, "Bearer [REDACTED]")
        sanitized = jwtRegex.replace(sanitized, "[JWT_REDACTED]")
        sanitized = localSessionRegex.replace(sanitized, "[SESSION_REDACTED]")
        sanitized = secretKvRegex.replace(sanitized) { matchResult ->
            val key = matchResult.groupValues[1]
            val separator = matchResult.groupValues[2]
            "$key$separator[REDACTED]"
        }
        sanitized = pgUrlRegex.replace(sanitized, "postgres://[REDACTED]")
        sanitized = sqlStateRegex.replace(sanitized, "[DB_DETAIL_REDACTED]")
        sanitized = emailRegex.replace(sanitized) { matchResult -> maskEmail(matchResult.value) }
        sanitized = phoneRegex.replace(sanitized) { matchResult -> maskPhone(matchResult.value) }
        sanitized = bankRefRegex.replace(sanitized) { matchResult -> maskBankReference(matchResult.value) }
        return sanitized
    }

    /**
     * Masks the local part of an email address while keeping first/last character and domain.
     */
    public fun maskEmail(raw: String): String {
        val parts = raw.trim().split("@")
        if (parts.size != 2) return "[EMAIL_REDACTED]"
        val local = parts[0]
        val domain = parts[1]
        if (local.isEmpty()) return "[EMAIL_REDACTED]"
        return if (local.length <= 2) {
            "${local.first()}***@$domain"
        } else {
            "${local.first()}***${local.last()}@$domain"
        }
    }

    /**
     * Masks a Dominican phone number keeping area code and last 2 digits.
     */
    public fun maskPhone(raw: String): String {
        val cleaned = raw.trim()
        if (cleaned.length < 5) return "[PHONE_REDACTED]"
        val areaCode = cleaned.take(3)
        val lastTwo = cleaned.takeLast(2)
        return "$areaCode-***-**$lastTwo"
    }

    /**
     * Masks a bank transfer reference keeping bank prefix and last 2 digits.
     */
    public fun maskBankReference(raw: String): String {
        val cleaned = raw.trim()
        val dashIndex = cleaned.indexOf('-')
        val hasMissingPrefix = dashIndex <= 0
        val isTooShortForMask = cleaned.length < dashIndex + 3
        if (hasMissingPrefix) return "[REF_REDACTED]"
        if (isTooShortForMask) return "[REF_REDACTED]"
        val bank = cleaned.substring(0, dashIndex)
        val suffix = cleaned.takeLast(2)
        return "$bank-***-$suffix"
    }

    /**
     * Translates a backend business error code into a human-friendly message, falling back to a redacted message.
     */
    public fun translateBusinessError(code: String?, serverMessage: String?): String {
        val normalizedCode = code?.trim()?.uppercase().orEmpty()
        val mapped = when (normalizedCode) {
            "AUTH_INVALID_CREDENTIALS" -> "Correo o contraseña incorrectos. Verifica tu usuario o correo e inténtalo de nuevo."
            "AUTH_MISSING_TOKEN", "AUTH_INVALID_TOKEN" -> "Tu sesión ha expirado. Inicia sesión nuevamente."
            "AUTH_USERNAME_TAKEN" -> "Ese nombre de usuario ya está en uso. Elige otro."
            "AUTH_EMAIL_TAKEN" -> "Ya existe una cuenta registrada con ese correo electrónico."
            "AUTH_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres."
            "AUTH_MISSING_FIELDS" -> "Completa tu nombre, usuario, correo electrónico válido y contraseña."
            "AUTH_TERMS_REQUIRED" -> "Debes aceptar los Términos y condiciones para crear tu cuenta."
            "AUTH_PASSWORD_MISMATCH" -> "Las contraseñas no coinciden. Verifica ambos campos."
            "AUTH_ACCOUNT_NOT_FOUND" -> "No encontramos ninguna cuenta asociada a ese correo o usuario."
            "FORBIDDEN_PERMISSION" -> "Tu perfil operativo no tiene permisos para realizar esta acción."
            "FORBIDDEN_APP_VIEW" -> "Tu cuenta no tiene acceso a esta vista de la aplicación."
            "ACCOUNT_HAS_ACTIVE_EVENTS" -> "No puedes eliminar tu cuenta mientras tengas eventos activos o saldos pendientes."
            "QUOTE_NOT_FOUND" -> "No encontramos la cotización solicitada."
            "QUOTE_VALIDATION_FAILED" -> "Revisa los datos de la cotización: faltan artículos o montos válidos."
            "QUOTE_SIGNATURE_INVALID" -> "Debes ingresar tu nombre completo y trazar la firma digital del contrato."
            "EVENT_NOT_FOUND" -> "No encontramos el evento solicitado."
            "EVENT_DATE_CHANGE_REJECTED" -> "El cambio de fecha requiere al menos 48 horas de anticipación."
            "EVENT_CANCEL_REJECTED" -> "No fue posible procesar la cancelación del evento."
            "PAYMENT_VALIDATION_FAILED" -> "Verifica el monto en RD$ y el código de referencia bancaria antes de registrar el pago."
            "DAMAGE_REPORT_INVALID" -> "Completa el artículo, las unidades afectadas y el monto de reposición."
            "RATE_LIMIT_EXCEEDED" -> "Demasiados intentos seguidos. Espera un minuto antes de volver a intentarlo."
            "SMTP_PROVIDER_NOT_CONFIGURED" -> "El servidor de correo SMTP no está configurado en este entorno."
            "OAUTH_PROVIDER_NOT_CONFIGURED" -> "El proveedor de inicio de sesión Google o Apple no está configurado en este servidor."
            "AI_PROVIDER_NOT_CONFIGURED" -> "El proveedor de inteligencia artificial de Rosa no está configurado en el servidor."
            "STT_PROVIDER_NOT_CONFIGURED" -> "La transcripción automática de audio no está configurada en el servidor; escribe tu mensaje manualmente."
            "CARD_GATEWAY_NOT_CONFIGURED" -> "La pasarela de pagos con tarjeta no está configurada; utiliza transferencia bancaria o efectivo."
            "WHATSAPP_PROVIDER_NOT_CONFIGURED" -> "El proveedor de mensajería WhatsApp no está configurado en el servidor."
            "STOCK_INSUFFICIENT_FOR_DATE" -> "No hay inventario suficiente disponible para la ventana logística de esta fecha."
            "ROSA_AUDIO_TOO_LONG" -> "El audio supera el límite permitido. Envía una nota de voz más corta o escribe tu solicitud."
            "ROSA_RATE_LIMITED" -> "Rosa recibió demasiadas solicitudes seguidas. Espera unos segundos antes de volver a intentarlo."
            "INVALID_FISCAL_RNC" -> "El RNC o Cédula ingresado no es válido para comprobante fiscal B01 (requiere 9 u 11 dígitos)."
            "PAYMENT_NOT_PENDING" -> "Este pago ya fue procesado anteriormente y no se encuentra pendiente de verificación."
            "MEDIA_TOO_LARGE" -> "El archivo supera el tamaño máximo permitido de 10 MB."
            "MEDIA_UNSUPPORTED_MIME" -> "Formato de archivo no compatible. Usa imágenes PNG, JPEG, WebP o PDF."
            "EVENT_CLOSEOUT_REJECTED" -> "Este evento ya completó su cierre operativo o no puede cerrarse en este estado."
            else -> null
        }
        if (mapped != null) return mapped
        val rawFallback = serverMessage.orEmpty()
        val fallback = redactText(rawFallback)
        return fallback.ifBlank { "Ocurrió un inconveniente temporal. Por favor, inténtalo de nuevo." }
    }
}
