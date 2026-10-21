package com.jeudry.rosafiesta.domain.security

/**
 * Redacts personal data, credentials, bank references, and raw SQL details before logging or surfacing errors in UI.
 */
public object SensitiveDataRedactor {
    private val bearerRegex = Regex("""(?i)\bBearer\s+[A-Za-z0-9\-._~+/]+=*""")
    private val jwtRegex = Regex("""\beyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b""")
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
        sanitized = secretKvRegex.replace(sanitized) { match ->
            val key = match.groupValues[1]
            val separator = match.groupValues[2]
            "$key$separator[REDACTED]"
        }
        sanitized = pgUrlRegex.replace(sanitized, "postgres://[REDACTED]")
        sanitized = sqlStateRegex.replace(sanitized, "[DB_DETAIL_REDACTED]")
        sanitized = emailRegex.replace(sanitized) { match -> maskEmail(match.value) }
        sanitized = phoneRegex.replace(sanitized) { match -> maskPhone(match.value) }
        sanitized = bankRefRegex.replace(sanitized) { match -> maskBankReference(match.value) }
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
        if (dashIndex <= 0 || cleaned.length < dashIndex + 3) return "[REF_REDACTED]"
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
            "AUTH_INVALID_CREDENTIALS" -> "Correo o contraseña incorrectos. Verifica tus datos e inténtalo de nuevo."
            "AUTH_MISSING_TOKEN", "AUTH_INVALID_TOKEN" -> "Tu sesión ha expirado. Inicia sesión nuevamente."
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
            else -> null
        }
        if (mapped != null) return mapped
        val fallback = redactText(serverMessage.orEmpty())
        return fallback.ifBlank { "Ocurrió un inconveniente temporal. Por favor, inténtalo de nuevo." }
    }
}
