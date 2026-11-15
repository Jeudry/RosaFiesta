package com.jeudry.rosafiesta.domain.validation

import kotlin.math.roundToLong

/**
 * Pure domain validator, input mask filter, and presentation formatter for forms across RosaFiesta.
 * Enforces required status, minimum and maximum lengths, numeric bounds, and strict format rules.
 */
public object InputValidator {
    public const val MIN_NAME_LENGTH: Int = 2
    public const val MAX_PERSON_NAME_LENGTH: Int = 40
    public const val MAX_FULL_NAME_LENGTH: Int = 60
    public const val MIN_USERNAME_LENGTH: Int = 3
    public const val MAX_USERNAME_LENGTH: Int = 24
    public const val MAX_LOGIN_USERNAME_LENGTH: Int = 30
    public const val MIN_EMAIL_LENGTH: Int = 6
    public const val MAX_EMAIL_LENGTH: Int = 80
    public const val MIN_PASSWORD_LENGTH: Int = 6
    public const val MAX_PASSWORD_LENGTH: Int = 64
    public const val MIN_PHONE_DIGITS: Int = 10
    public const val MAX_PHONE_DIGITS: Int = 15
    public const val MAX_PHONE_LENGTH: Int = 18
    public const val MAX_BIRTH_DATE_LENGTH: Int = 10
    public const val MIN_BIRTH_YEAR: Int = 1925
    public const val MAX_BIRTH_YEAR: Int = 2010

    public const val MIN_BUSINESS_NAME_LENGTH: Int = 2
    public const val MAX_BUSINESS_NAME_LENGTH: Int = 80
    public const val MIN_EVENT_NAME_LENGTH: Int = 3
    public const val MAX_EVENT_NAME_LENGTH: Int = 80
    public const val MIN_ARTICLE_NAME_LENGTH: Int = 2
    public const val MAX_ARTICLE_NAME_LENGTH: Int = 80
    public const val MIN_PACKAGE_NAME_LENGTH: Int = 2
    public const val MAX_PACKAGE_NAME_LENGTH: Int = 80
    public const val MIN_ADDRESS_LENGTH: Int = 5
    public const val MAX_ADDRESS_LENGTH: Int = 140
    public const val MAX_WEBSITE_LENGTH: Int = 120
    public const val MIN_BUSINESS_HOURS_LENGTH: Int = 5
    public const val MAX_BUSINESS_HOURS_LENGTH: Int = 60
    public const val OTP_LENGTH: Int = 6
    public const val RNC_COMPANY_DIGITS: Int = 9
    public const val RNC_PERSONAL_DIGITS: Int = 11
    public const val MAX_RNC_LENGTH: Int = 14
    public const val MIN_CARD_DIGITS: Int = 13
    public const val MAX_CARD_DIGITS: Int = 19
    public const val MAX_CARD_FORMATTED_LENGTH: Int = 23
    public const val MIN_CVV_DIGITS: Int = 3
    public const val MAX_CVV_DIGITS: Int = 4
    public const val MIN_BANK_REFERENCE_LENGTH: Int = 4
    public const val MAX_BANK_REFERENCE_LENGTH: Int = 32
    public const val MIN_BANK_ACCOUNT_DIGITS: Int = 8
    public const val MAX_BANK_ACCOUNT_DIGITS: Int = 22
    public const val MAX_MONEY_AMOUNT: Double = 10_000_000.0
    public const val MAX_GUEST_COUNT: Int = 10_000
    public const val MAX_STOCK_COUNT: Int = 100_000
    public const val MAX_CAPACITY_COUNT: Int = 1_000
    public const val MAX_RENTAL_DAYS: Int = 365
    public const val MAX_NOTES_LENGTH: Int = 500
    public const val MAX_DESCRIPTION_LENGTH: Int = 800
    public const val MAX_CHAT_MESSAGE_LENGTH: Int = 600
    public const val MAX_SEARCH_QUERY_LENGTH: Int = 80
    public const val MIN_EVENT_YEAR: Int = 2024
    public const val MAX_EVENT_YEAR: Int = 2035

    private val dominicanAreaCodes: Set<String> = setOf("809", "829", "849")
    private val emailRegex: Regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    private val usernameRegex: Regex = Regex("^[A-Za-z0-9._-]+$")
    private val birthDateRegex: Regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
    private val time24Regex: Regex = Regex("^\\d{2}:\\d{2}$")
    private val cardExpiryRegex: Regex = Regex("^\\d{2}/\\d{2}$")
    private val bankReferenceRegex: Regex = Regex("^[A-Za-z0-9\\-_./ ]+$")
    private val websiteRegex: Regex = Regex("^(https?://)?[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+(/[^\\s]*)?$")

    /**
     * Validates a user's full display name (2..60 characters, letters and spaces).
     */
    public fun validateFullName(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return "El nombre completo es obligatorio (mínimo $MIN_NAME_LENGTH caracteres)."
        }
        if (trimmed.length < MIN_NAME_LENGTH) {
            return "El nombre completo debe tener al menos $MIN_NAME_LENGTH caracteres."
        }
        if (trimmed.length > MAX_FULL_NAME_LENGTH) {
            return "El nombre completo no puede superar los $MAX_FULL_NAME_LENGTH caracteres."
        }
        val hasInvalidCharacter = trimmed.any { character -> !isAllowedNameCharacter(character) }
        if (hasInvalidCharacter) {
            return "El nombre completo solo puede contener letras y espacios."
        }
        return null
    }

    /**
     * Validates a first name or last name field (2..40 characters, letters and spaces).
     */
    public fun validatePersonNamePart(rawValue: String, fieldLabel: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return "$fieldLabel es obligatorio ($MIN_NAME_LENGTH a $MAX_PERSON_NAME_LENGTH caracteres)."
        }
        if (trimmed.length < MIN_NAME_LENGTH) {
            return "$fieldLabel debe tener al menos $MIN_NAME_LENGTH caracteres."
        }
        if (trimmed.length > MAX_PERSON_NAME_LENGTH) {
            return "$fieldLabel no puede superar los $MAX_PERSON_NAME_LENGTH caracteres."
        }
        val hasInvalidCharacter = trimmed.any { character -> !isAllowedNameCharacter(character) }
        if (hasInvalidCharacter) {
            return "$fieldLabel solo puede contener letras y espacios."
        }
        return null
    }

    /**
     * Validates an account username (3..24 characters, alphanumeric, dot, hyphen, underscore).
     */
    public fun validateUsername(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return "El usuario es obligatorio ($MIN_USERNAME_LENGTH a $MAX_USERNAME_LENGTH caracteres)."
        }
        if (trimmed.length < MIN_USERNAME_LENGTH) {
            return "El usuario debe tener al menos $MIN_USERNAME_LENGTH caracteres."
        }
        if (trimmed.length > MAX_USERNAME_LENGTH) {
            return "El usuario no puede superar los $MAX_USERNAME_LENGTH caracteres."
        }
        val matchesPattern = usernameRegex.matches(trimmed)
        if (!matchesPattern) {
            return "El usuario solo permite letras, números, punto (.), guion (-) o guion bajo (_)."
        }
        return null
    }

    /**
     * Validates an email address (6..80 characters, standard user@domain.tld format).
     */
    public fun validateEmail(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return "El correo electrónico es obligatorio."
        }
        if (trimmed.length < MIN_EMAIL_LENGTH) {
            return "El correo electrónico debe tener al menos $MIN_EMAIL_LENGTH caracteres."
        }
        if (trimmed.length > MAX_EMAIL_LENGTH) {
            return "El correo electrónico no puede superar los $MAX_EMAIL_LENGTH caracteres."
        }
        val matchesEmailPattern = emailRegex.matches(trimmed)
        if (!matchesEmailPattern) {
            return "Ingresa un correo electrónico válido (ejemplo: usuario@dominio.com)."
        }
        return null
    }

    /**
     * Validates a login or recovery identifier (either a valid email address or a 3..30 character username).
     */
    public fun validateLoginIdentifier(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return "Ingresa tu correo electrónico o nombre de usuario."
        }
        if (trimmed.length < MIN_USERNAME_LENGTH) {
            return "El correo o usuario debe tener al menos $MIN_USERNAME_LENGTH caracteres."
        }
        if (trimmed.length > MAX_EMAIL_LENGTH) {
            return "El correo o usuario no puede superar los $MAX_EMAIL_LENGTH caracteres."
        }
        val looksLikeEmail = trimmed.contains("@")
        if (looksLikeEmail) {
            return validateEmail(trimmed)
        }
        if (trimmed.length > MAX_LOGIN_USERNAME_LENGTH) {
            return "El nombre de usuario no puede superar los $MAX_LOGIN_USERNAME_LENGTH caracteres."
        }
        val matchesUsername = usernameRegex.matches(trimmed)
        if (!matchesUsername) {
            return "Ingresa un correo válido (con @dominio.com) o un usuario sin espacios."
        }
        return null
    }

    /**
     * Validates a password string (6..64 characters).
     */
    public fun validatePassword(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return "La contraseña es obligatoria ($MIN_PASSWORD_LENGTH a $MAX_PASSWORD_LENGTH caracteres)."
        }
        if (trimmed.length < MIN_PASSWORD_LENGTH) {
            return "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres."
        }
        if (trimmed.length > MAX_PASSWORD_LENGTH) {
            return "La contraseña no puede superar los $MAX_PASSWORD_LENGTH caracteres."
        }
        return null
    }

    /**
     * Validates a phone number (10..15 digits, up to 18 formatted characters).
     */
    public fun validatePhone(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return "El teléfono es obligatorio (mínimo $MIN_PHONE_DIGITS dígitos, ej. 809-555-0102)."
        }
        if (trimmed.length > MAX_PHONE_LENGTH) {
            return "El teléfono no puede superar los $MAX_PHONE_LENGTH caracteres."
        }
        val hasInvalidPhoneChar = trimmed.any { character -> !isAllowedPhoneCharacter(character) }
        if (hasInvalidPhoneChar) {
            return "El teléfono solo puede contener números, guiones, espacios o +."
        }
        val digitCount = trimmed.count { character -> character.isDigit() }
        if (digitCount < MIN_PHONE_DIGITS) {
            return "El teléfono debe contener al menos $MIN_PHONE_DIGITS dígitos (ej. 809-555-0102)."
        }
        if (digitCount > MAX_PHONE_DIGITS) {
            return "El teléfono no puede superar los $MAX_PHONE_DIGITS dígitos."
        }
        return null
    }

    /**
     * Validates a birth date in DD/MM/YYYY format within the allowed year range (1925..2010).
     */
    public fun validateBirthDate(rawValue: String, isRequired: Boolean = true): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return if (isRequired) {
                "La fecha de nacimiento es obligatoria (DD/MM/AAAA)."
            } else {
                null
            }
        }
        val matchesDatePattern = birthDateRegex.matches(trimmed)
        if (!matchesDatePattern) {
            return "Ingresa la fecha en formato DD/MM/AAAA (ejemplo: 22/09/1996)."
        }
        val parts = trimmed.split("/")
        val day = parts[0].toIntOrNull() ?: return "El día de nacimiento no es válido."
        val month = parts[1].toIntOrNull() ?: return "El mes de nacimiento no es válido."
        val year = parts[2].toIntOrNull() ?: return "El año de nacimiento no es válido."

        val isMonthOutOfRange = month !in 1..12
        if (isMonthOutOfRange) {
            return "El mes de nacimiento debe estar entre 01 y 12."
        }
        val isYearOutOfRange = year !in MIN_BIRTH_YEAR..MAX_BIRTH_YEAR
        if (isYearOutOfRange) {
            return "El año de nacimiento debe estar entre $MIN_BIRTH_YEAR y $MAX_BIRTH_YEAR."
        }
        val maxDayInMonth = maxDaysInMonth(month = month, year = year)
        val isDayOutOfRange = day !in 1..maxDayInMonth
        if (isDayOutOfRange) {
            return "El día $day no es válido para el mes indicado (máximo $maxDayInMonth)."
        }
        return null
    }

    /**
     * Validates an optional phone number; blank returns null, otherwise delegates to [validatePhone].
     */
    public fun validateOptionalPhone(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return null
        return validatePhone(trimmed)
    }

    /**
     * Validates a Dominican phone number supporting (809)/(829)/(849) area codes or general international digits.
     */
    public fun validateDominicanPhone(
        rawValue: String,
        isRequired: Boolean = true,
        allowInternational: Boolean = false,
        fieldLabel: String = "El teléfono",
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "$fieldLabel es obligatorio."
            return null
        }
        val baseError = validatePhone(trimmed)
        if (baseError != null) return baseError
        val digits = filterDigitsOnly(trimmed, MAX_PHONE_DIGITS)
        if (isValidDominicanDigits(digits)) return null
        if (allowInternational) return null
        return "$fieldLabel debe iniciar con código 809, 829 u 849."
    }

    /**
     * Validates a WhatsApp contact number using Dominican phone rules.
     */
    public fun validateWhatsApp(
        rawValue: String,
        isRequired: Boolean = true,
        allowInternational: Boolean = true,
    ): String? = validateDominicanPhone(
        rawValue = rawValue,
        isRequired = isRequired,
        allowInternational = allowInternational,
        fieldLabel = "El WhatsApp",
    )

    /**
     * Validates a Dominican RNC (9 digits) or cédula/RNC personal (11 digits).
     */
    public fun validateRnc(
        rawValue: String,
        isRequired: Boolean = false,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "El RNC es obligatorio."
            return null
        }
        if (trimmed.length > MAX_RNC_LENGTH) return "El RNC no puede exceder $MAX_RNC_LENGTH caracteres."
        val hasInvalidCharacter = trimmed.any { character -> !isAllowedRncCharacter(character) }
        if (hasInvalidCharacter) return "El RNC solo puede contener dígitos y guiones."
        val digits = filterDigitsOnly(trimmed, RNC_PERSONAL_DIGITS + 1)
        val isCompanyRnc = digits.length == RNC_COMPANY_DIGITS
        val isPersonalRnc = digits.length == RNC_PERSONAL_DIGITS
        if (!isCompanyRnc) {
            if (!isPersonalRnc) {
                return "Ingresa un RNC válido de 9 u 11 dígitos."
            }
        }
        val isAllZeros = digits.all { digit -> digit == '0' }
        if (isAllZeros) return "Ingresa un RNC válido."
        return null
    }

    /**
     * Validates an optional email address; blank returns null, otherwise enforces [validateEmail].
     */
    public fun validateOptionalEmail(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return null
        return validateEmail(trimmed)
    }

    /**
     * Validates a business, event, article, package, or supplier display name.
     */
    public fun validateEntityName(
        rawValue: String,
        fieldLabel: String,
        minLength: Int = MIN_NAME_LENGTH,
        maxLength: Int = MAX_BUSINESS_NAME_LENGTH,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        val lengthError = validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = fieldLabel,
            isRequired = isRequired,
            minLength = minLength,
            maxLength = maxLength,
        )
        if (lengthError != null) return lengthError
        if (trimmed.isEmpty()) return null
        val hasLetterOrDigit = trimmed.any { character -> character.isLetterOrDigit() }
        if (!hasLetterOrDigit) return "$fieldLabel debe incluir letras o números."
        return null
    }

    /**
     * Validates a commercial business or storefront name.
     */
    public fun validateBusinessName(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateEntityName(
        rawValue = rawValue,
        fieldLabel = "El nombre del negocio",
        minLength = MIN_BUSINESS_NAME_LENGTH,
        maxLength = MAX_BUSINESS_NAME_LENGTH,
        isRequired = isRequired,
    )

    /**
     * Validates an event title.
     */
    public fun validateEventName(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateEntityName(
        rawValue = rawValue,
        fieldLabel = "El nombre del evento",
        minLength = MIN_EVENT_NAME_LENGTH,
        maxLength = MAX_EVENT_NAME_LENGTH,
        isRequired = isRequired,
    )

    /**
     * Validates a catalog article or inventory item name.
     */
    public fun validateArticleName(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateEntityName(
        rawValue = rawValue,
        fieldLabel = "El nombre del artículo",
        minLength = MIN_ARTICLE_NAME_LENGTH,
        maxLength = MAX_ARTICLE_NAME_LENGTH,
        isRequired = isRequired,
    )

    /**
     * Validates a service package name.
     */
    public fun validatePackageName(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateEntityName(
        rawValue = rawValue,
        fieldLabel = "El nombre del paquete",
        minLength = MIN_PACKAGE_NAME_LENGTH,
        maxLength = MAX_PACKAGE_NAME_LENGTH,
        isRequired = isRequired,
    )

    /**
     * Validates a supplier or partner name.
     */
    public fun validateSupplierName(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateEntityName(
        rawValue = rawValue,
        fieldLabel = "El nombre del suplidor",
        minLength = MIN_NAME_LENGTH,
        maxLength = MAX_BUSINESS_NAME_LENGTH,
        isRequired = isRequired,
    )

    /**
     * Validates a physical street address or event venue location.
     */
    public fun validateAddress(
        rawValue: String,
        fieldLabel: String = "La dirección",
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        val lengthError = validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = fieldLabel,
            isRequired = isRequired,
            minLength = MIN_ADDRESS_LENGTH,
            maxLength = MAX_ADDRESS_LENGTH,
        )
        if (lengthError != null) return lengthError
        if (trimmed.isEmpty()) return null
        val hasLetterOrDigit = trimmed.any { character -> character.isLetterOrDigit() }
        if (!hasLetterOrDigit) return "$fieldLabel debe ser una dirección válida."
        return null
    }

    /**
     * Validates an optional or required website URL or domain.
     */
    public fun validateWebsiteUrl(
        rawValue: String,
        isRequired: Boolean = false,
    ): String? {
        val trimmed = rawValue.trim()
        val lengthError = validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = "El sitio web",
            isRequired = isRequired,
            minLength = 4,
            maxLength = MAX_WEBSITE_LENGTH,
        )
        if (lengthError != null) return lengthError
        if (trimmed.isEmpty()) return null
        if (!websiteRegex.matches(trimmed)) {
            return "Ingresa una URL válida (ej. www.ejemplo.com)."
        }
        return null
    }

    /**
     * Validates an event or booking date in DD/MM/YYYY format within [minYear]..[maxYear].
     */
    public fun validateEventDate(
        rawValue: String,
        isRequired: Boolean = true,
        minYear: Int = MIN_EVENT_YEAR,
        maxYear: Int = MAX_EVENT_YEAR,
    ): String? = validateDateInRange(
        rawValue = rawValue,
        fieldLabel = "La fecha del evento",
        isRequired = isRequired,
        minYear = minYear,
        maxYear = maxYear,
    )

    /**
     * Validates a 24-hour clock string in HH:mm format.
     */
    public fun validateTime24(
        rawValue: String,
        fieldLabel: String = "La hora",
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "$fieldLabel es obligatoria."
            return null
        }
        if (!time24Regex.matches(trimmed)) return "$fieldLabel debe usar el formato HH:MM."
        val totalMinutes = parseTime24Minutes(trimmed)
        if (totalMinutes == null) return "$fieldLabel no es válida."
        return null
    }

    /**
     * Validates a 12-hour or 24-hour clock string (e.g., "09:00", "9:00 AM", "06:30 PM").
     */
    public fun validateTimeDisplay(
        rawValue: String,
        fieldLabel: String = "La hora",
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "$fieldLabel es obligatoria."
            return null
        }
        val parsedMinutes = parseFlexibleTimeMinutes(trimmed)
        if (parsedMinutes == null) return "$fieldLabel no tiene un formato válido."
        return null
    }

    /**
     * Validates an opening-to-closing time range ensuring end time is strictly after start time.
     */
    public fun validateTimeRange(
        startRaw: String,
        endRaw: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmedStart = startRaw.trim()
        val trimmedEnd = endRaw.trim()
        val bothBlank = trimmedStart.isEmpty() && trimmedEnd.isEmpty()
        if (bothBlank) {
            if (isRequired) return "El horario es obligatorio."
            return null
        }
        val startError = validateTimeDisplay(
            rawValue = trimmedStart,
            fieldLabel = "La hora de inicio",
            isRequired = true,
        )
        if (startError != null) return startError
        val endError = validateTimeDisplay(
            rawValue = trimmedEnd,
            fieldLabel = "La hora de fin",
            isRequired = true,
        )
        if (endError != null) return endError
        val startMinutes = parseFlexibleTimeMinutes(trimmedStart) ?: return "La hora de inicio no es válida."
        val endMinutes = parseFlexibleTimeMinutes(trimmedEnd) ?: return "La hora de fin no es válida."
        if (endMinutes <= startMinutes) return "La hora de cierre debe ser posterior a la de apertura."
        return null
    }

    /**
     * Validates a single-line business schedule or hours label (e.g. "Lun–Sáb · 9:00 AM – 6:00 PM").
     */
    public fun validateBusinessHours(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        val lengthError = validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = "El horario de atención",
            isRequired = isRequired,
            minLength = MIN_BUSINESS_HOURS_LENGTH,
            maxLength = MAX_BUSINESS_HOURS_LENGTH,
        )
        if (lengthError != null) return lengthError
        if (trimmed.isEmpty()) return null
        val hasDigit = trimmed.any { character -> character.isDigit() }
        if (!hasDigit) return "Incluye horas numéricas en el horario."
        return null
    }

    /**
     * Validates password confirmation matching [originalPassword].
     */
    public fun validatePasswordConfirmation(
        originalPassword: String,
        confirmationPassword: String,
    ): String? {
        if (confirmationPassword.isEmpty()) return "Confirma tu contraseña."
        if (confirmationPassword != originalPassword) return "Las contraseñas no coinciden."
        return null
    }

    /**
     * Validates a numeric OTP verification code of exact [expectedLength] digits.
     */
    public fun validateOtpCode(
        rawValue: String,
        expectedLength: Int = OTP_LENGTH,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "El código de verificación es obligatorio."
            return null
        }
        val hasNonDigit = trimmed.any { character -> !character.isDigit() }
        if (hasNonDigit) return "El código solo puede contener dígitos."
        if (trimmed.length != expectedLength) {
            return "El código debe tener exactamente $expectedLength dígitos."
        }
        return null
    }

    /**
     * Validates a payment cardholder name.
     */
    public fun validateCardholderName(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "El nombre del titular es obligatorio."
            return null
        }
        return validateFullName(trimmed)
    }

    /**
     * Validates a payment card PAN length, digits, and Luhn checksum.
     */
    public fun validateCardNumber(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "El número de tarjeta es obligatorio."
            return null
        }
        val hasInvalidChar = trimmed.any { character -> !isAllowedCardCharacter(character) }
        if (hasInvalidChar) return "El número de tarjeta solo admite dígitos y espacios."
        val digits = filterDigitsOnly(trimmed, MAX_CARD_DIGITS + 1)
        if (digits.length < MIN_CARD_DIGITS) {
            return "El número de tarjeta debe tener al menos $MIN_CARD_DIGITS dígitos."
        }
        if (digits.length > MAX_CARD_DIGITS) {
            return "El número de tarjeta no puede superar $MAX_CARD_DIGITS dígitos."
        }
        if (!isValidLuhn(digits)) return "El número de tarjeta no es válido."
        return null
    }

    /**
     * Validates a 3- or 4-digit card CVV/CVC security code.
     */
    public fun validateCardCvv(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "El CVV es obligatorio."
            return null
        }
        val hasNonDigit = trimmed.any { character -> !character.isDigit() }
        if (hasNonDigit) return "El CVV solo puede contener dígitos."
        if (trimmed.length < MIN_CVV_DIGITS) return "El CVV debe tener al menos $MIN_CVV_DIGITS dígitos."
        if (trimmed.length > MAX_CVV_DIGITS) return "El CVV no puede exceder $MAX_CVV_DIGITS dígitos."
        return null
    }

    /**
     * Validates a card expiry string in MM/YY format and ensures it is within valid month/year bounds.
     */
    public fun validateCardExpiry(
        rawValue: String,
        isRequired: Boolean = true,
        minTwoDigitYear: Int = 25,
        maxTwoDigitYear: Int = 45,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "La fecha de vencimiento es obligatoria."
            return null
        }
        if (!cardExpiryRegex.matches(trimmed)) return "Usa el formato MM/AA."
        val parts = trimmed.split("/")
        val month = parts[0].toIntOrNull() ?: return "El mes de vencimiento no es válido."
        val year = parts[1].toIntOrNull() ?: return "El año de vencimiento no es válido."
        if (month < 1) return "El mes de vencimiento no es válido."
        if (month > 12) return "El mes de vencimiento no es válido."
        if (year < minTwoDigitYear) return "El año de vencimiento no es válido."
        if (year > maxTwoDigitYear) return "El año de vencimiento no es válido."
        return null
    }

    /**
     * Validates a bank transfer reference code or receipt number.
     */
    public fun validateBankReference(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        val lengthError = validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = "La referencia bancaria",
            isRequired = isRequired,
            minLength = MIN_BANK_REFERENCE_LENGTH,
            maxLength = MAX_BANK_REFERENCE_LENGTH,
        )
        if (lengthError != null) return lengthError
        if (trimmed.isEmpty()) return null
        if (!bankReferenceRegex.matches(trimmed)) {
            return "La referencia bancaria contiene caracteres no permitidos."
        }
        return null
    }

    /**
     * Validates a bank account number (digits/hyphens/spaces within 8..22 digits).
     */
    public fun validateBankAccount(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "La cuenta bancaria es obligatoria."
            return null
        }
        val hasInvalidChar = trimmed.any { character -> !isAllowedRncCharacter(character) }
        if (hasInvalidChar) return "La cuenta bancaria solo puede contener dígitos y guiones."
        val digits = filterDigitsOnly(trimmed, MAX_BANK_ACCOUNT_DIGITS + 1)
        if (digits.length < MIN_BANK_ACCOUNT_DIGITS) {
            return "La cuenta debe tener al menos $MIN_BANK_ACCOUNT_DIGITS dígitos."
        }
        if (digits.length > MAX_BANK_ACCOUNT_DIGITS) {
            return "La cuenta no puede superar $MAX_BANK_ACCOUNT_DIGITS dígitos."
        }
        return null
    }

    /**
     * Validates a monetary amount (prices, deposits, surcharges, payments) within [minAmount]..[maxAmount].
     */
    public fun validateMoneyAmount(
        rawValue: String,
        fieldLabel: String = "El monto",
        isRequired: Boolean = true,
        minAmount: Double = 0.01,
        maxAmount: Double = MAX_MONEY_AMOUNT,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "$fieldLabel es obligatorio."
            return null
        }
        val normalized = normalizeCurrencyText(trimmed)
        val parsed = normalized.toDoubleOrNull()
        if (parsed == null) return "$fieldLabel debe ser un monto numérico válido."
        if (parsed.isNaN()) return "$fieldLabel debe ser un monto válido."
        if (parsed.isInfinite()) return "$fieldLabel debe ser un monto válido."
        if (parsed < minAmount) {
            val formattedMin = formatCurrencyDop(minAmount)
            return "$fieldLabel debe ser al menos $formattedMin."
        }
        if (parsed > maxAmount) {
            val formattedMax = formatCurrencyDop(maxAmount)
            return "$fieldLabel no puede superar $formattedMax."
        }
        return null
    }

    /**
     * Validates a percentage number within [minPercent]..[maxPercent].
     */
    public fun validatePercentage(
        rawValue: String,
        fieldLabel: String = "El porcentaje",
        isRequired: Boolean = true,
        minPercent: Int = 0,
        maxPercent: Int = 100,
    ): String? = validateIntegerCount(
        rawValue = rawValue,
        fieldLabel = fieldLabel,
        isRequired = isRequired,
        minValue = minPercent,
        maxValue = maxPercent,
    )

    /**
     * Validates a deposit percentage within 1%..100%.
     */
    public fun validateDepositPercent(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validatePercentage(
        rawValue = rawValue,
        fieldLabel = "El porcentaje de depósito",
        isRequired = isRequired,
        minPercent = 1,
        maxPercent = 100,
    )

    /**
     * Validates a whole integer count within [minValue]..[maxValue].
     */
    public fun validateIntegerCount(
        rawValue: String,
        fieldLabel: String,
        isRequired: Boolean = true,
        minValue: Int = 1,
        maxValue: Int = MAX_STOCK_COUNT,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "$fieldLabel es obligatorio."
            return null
        }
        val parsed = trimmed.toIntOrNull()
        if (parsed == null) return "$fieldLabel debe ser un número entero válido."
        if (parsed < minValue) return "$fieldLabel debe ser al menos $minValue."
        if (parsed > maxValue) return "$fieldLabel no puede exceder $maxValue."
        return null
    }

    /**
     * Validates a guest count within 1..[MAX_GUEST_COUNT].
     */
    public fun validateGuestCount(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateIntegerCount(
        rawValue = rawValue,
        fieldLabel = "La cantidad de invitados",
        isRequired = isRequired,
        minValue = 1,
        maxValue = MAX_GUEST_COUNT,
    )

    /**
     * Validates inventory stock within 0..[MAX_STOCK_COUNT].
     */
    public fun validateStockCount(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateIntegerCount(
        rawValue = rawValue,
        fieldLabel = "El inventario disponible",
        isRequired = isRequired,
        minValue = 0,
        maxValue = MAX_STOCK_COUNT,
    )

    /**
     * Validates daily capacity within 1..[MAX_CAPACITY_COUNT].
     */
    public fun validateCapacityCount(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? = validateIntegerCount(
        rawValue = rawValue,
        fieldLabel = "La capacidad diaria",
        isRequired = isRequired,
        minValue = 1,
        maxValue = MAX_CAPACITY_COUNT,
    )

    /**
     * Validates a duration in days within [minDays]..[maxDays].
     */
    public fun validateDaysCount(
        rawValue: String,
        fieldLabel: String = "Los días",
        isRequired: Boolean = true,
        minDays: Int = 0,
        maxDays: Int = MAX_RENTAL_DAYS,
    ): String? = validateIntegerCount(
        rawValue = rawValue,
        fieldLabel = fieldLabel,
        isRequired = isRequired,
        minValue = minDays,
        maxValue = maxDays,
    )

    /**
     * Validates optional or required freeform notes with length caps.
     */
    public fun validateNotes(
        rawValue: String,
        fieldLabel: String = "Las notas",
        isRequired: Boolean = false,
        minLength: Int = 0,
        maxLength: Int = MAX_NOTES_LENGTH,
    ): String? {
        val trimmed = rawValue.trim()
        return validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = fieldLabel,
            isRequired = isRequired,
            minLength = minLength,
            maxLength = maxLength,
        )
    }

    /**
     * Validates an article, package, or quote description.
     */
    public fun validateDescription(
        rawValue: String,
        fieldLabel: String = "La descripción",
        isRequired: Boolean = false,
        minLength: Int = 8,
        maxLength: Int = MAX_DESCRIPTION_LENGTH,
    ): String? {
        val trimmed = rawValue.trim()
        return validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = fieldLabel,
            isRequired = isRequired,
            minLength = minLength,
            maxLength = maxLength,
        )
    }

    /**
     * Validates an outgoing chat or support message.
     */
    public fun validateChatMessage(
        rawValue: String,
        isRequired: Boolean = true,
    ): String? {
        val trimmed = rawValue.trim()
        return validateRequiredAndLengthBounds(
            trimmed = trimmed,
            fieldLabel = "El mensaje",
            isRequired = isRequired,
            minLength = 1,
            maxLength = MAX_CHAT_MESSAGE_LENGTH,
        )
    }

    /**
     * Validates a search query string (always optional, enforces max length).
     */
    public fun validateSearchQuery(
        rawValue: String,
        maxLength: Int = MAX_SEARCH_QUERY_LENGTH,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.length > maxLength) {
            return "La búsqueda no puede superar $maxLength caracteres."
        }
        return null
    }

    /**
     * Filters [rawValue] down to ASCII digits truncated to [maxDigits].
     */
    public fun filterDigitsOnly(
        rawValue: String,
        maxDigits: Int = Int.MAX_VALUE,
    ): String {
        val builder = StringBuilder()
        for (character in rawValue) {
            if (builder.length >= maxDigits) break
            if (character.isDigit()) {
                builder.append(character)
            }
        }
        return builder.toString()
    }

    /**
     * Filters currency input keeping digits and at most one decimal dot with two fractional digits.
     */
    public fun filterDecimalCurrencyInput(
        rawValue: String,
        maxIntegerDigits: Int = 8,
    ): String {
        val sanitized = rawValue.replace(',', '.')
        val builder = StringBuilder()
        var hasDot = false
        var integerDigits = 0
        var decimalDigits = 0
        for (character in sanitized) {
            if (character.isDigit()) {
                val canAppendInteger = !hasDot && integerDigits < maxIntegerDigits
                val canAppendDecimal = hasDot && decimalDigits < 2
                if (canAppendInteger) {
                    builder.append(character)
                    integerDigits += 1
                } else if (canAppendDecimal) {
                    builder.append(character)
                    decimalDigits += 1
                }
            } else if (character == '.') {
                if (!hasDot) {
                    if (builder.isEmpty()) {
                        builder.append('0')
                        integerDigits = 1
                    }
                    builder.append('.')
                    hasDot = true
                }
            }
        }
        return builder.toString()
    }

    /**
     * Applies progressive Dominican phone masking as `(XXX) XXX-XXXX` (or `+1 (XXX) XXX-XXXX` for 11 digits starting with 1).
     */
    public fun filterPhoneInput(rawValue: String): String {
        val digits = filterDigitsOnly(rawValue, 11)
        if (digits.isEmpty()) return ""
        val hasUsCountryCode = digits.length == 11 && digits[0] == '1'
        if (hasUsCountryCode) {
            val local = digits.substring(1)
            val area = local.substring(0, 3)
            val middle = local.substring(3, 6)
            val tail = local.substring(6, 10)
            return "+1 ($area) $middle-$tail"
        }
        val capped = digits.take(10)
        return formatTenDigitPhoneMask(capped)
    }

    /**
     * Applies progressive Dominican RNC masking (`X-XX-XXXXX-X` for 9 digits, `XXX-XXXXXXX-X` for 11 digits).
     */
    public fun filterRncInput(rawValue: String): String {
        val digits = filterDigitsOnly(rawValue, RNC_PERSONAL_DIGITS)
        if (digits.isEmpty()) return ""
        if (digits.length > RNC_COMPANY_DIGITS) {
            return formatElevenDigitRnc(digits)
        }
        return formatNineDigitRnc(digits)
    }

    /**
     * Applies progressive DD/MM/YYYY date masking, keeping existing backspace behavior intact.
     */
    public fun filterDateInput(
        previousFormatted: String,
        newRawText: String,
    ): String {
        val digitsOnly = filterDigitsOnly(newRawText, 8)
        val previousDigits = filterDigitsOnly(previousFormatted, 8)
        val isSingleSlashDeletion = newRawText.length == previousFormatted.length - 1 &&
            digitsOnly == previousDigits
        val shouldDropLastDigit = isSingleSlashDeletion && digitsOnly.isNotEmpty()
        val effectiveDigits = if (shouldDropLastDigit) {
            digitsOnly.dropLast(1)
        } else {
            digitsOnly
        }
        val builder = StringBuilder()
        for (index in effectiveDigits.indices) {
            val needsSlashAfterDay = index == 2
            val needsSlashAfterMonth = index == 4
            if (needsSlashAfterDay) {
                builder.append('/')
            } else if (needsSlashAfterMonth) {
                builder.append('/')
            }
            builder.append(effectiveDigits[index])
        }
        return builder.toString()
    }

    /**
     * Applies progressive 24-hour `HH:mm` masking.
     */
    public fun filterTimeInput(rawValue: String): String {
        val digits = filterDigitsOnly(rawValue, 4)
        if (digits.length <= 2) return digits
        val hours = digits.substring(0, 2)
        val minutes = digits.substring(2)
        return "$hours:$minutes"
    }

    /**
     * Applies progressive payment card masking in groups of 4 digits (`XXXX XXXX XXXX XXXX`).
     */
    public fun filterCardNumberInput(rawValue: String): String {
        val digits = filterDigitsOnly(rawValue, MAX_CARD_DIGITS)
        val builder = StringBuilder()
        for (index in digits.indices) {
            val shouldInsertSpace = index > 0 && index % 4 == 0
            if (shouldInsertSpace) {
                builder.append(' ')
            }
            builder.append(digits[index])
        }
        return builder.toString()
    }

    /**
     * Applies progressive `MM/AA` card expiry masking.
     */
    public fun filterCardExpiryInput(rawValue: String): String {
        val digits = filterDigitsOnly(rawValue, 4)
        if (digits.length <= 2) return digits
        val month = digits.substring(0, 2)
        val year = digits.substring(2)
        return "$month/$year"
    }

    /**
     * Filters text to allowed person name characters truncated to [maxLength].
     */
    public fun filterPersonNameInput(
        rawValue: String,
        maxLength: Int = MAX_PERSON_NAME_LENGTH,
    ): String {
        val builder = StringBuilder()
        for (character in rawValue) {
            if (builder.length >= maxLength) break
            if (isAllowedNameCharacter(character)) {
                builder.append(character)
            }
        }
        return builder.toString()
    }

    /**
     * Filters text to valid username characters in lowercase truncated to [MAX_USERNAME_LENGTH].
     */
    public fun filterUsernameInput(rawValue: String): String {
        val builder = StringBuilder()
        for (character in rawValue) {
            if (builder.length >= MAX_USERNAME_LENGTH) break
            val isAllowed = character.isLetterOrDigit() ||
                character == '.' ||
                character == '_' ||
                character == '-'
            if (isAllowed) {
                builder.append(character.lowercaseChar())
            }
        }
        return builder.toString()
    }

    /**
     * Formats a stored phone string into `(XXX) XXX-XXXX` when 10 digits or `+X ...` when longer.
     */
    public fun formatPhoneDisplay(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return ""
        val digits = filterDigitsOnly(trimmed, MAX_PHONE_DIGITS)
        if (digits.length == 10) return formatTenDigitPhoneMask(digits)
        val hasUsCountryCode = digits.length == 11 && digits[0] == '1'
        if (hasUsCountryCode) {
            val local = digits.substring(1)
            val masked = formatTenDigitPhoneMask(local)
            return "+1 $masked"
        }
        return trimmed
    }

    /**
     * Formats a 9-digit RNC as `X-XX-XXXXX-X` or 11-digit RNC as `XXX-XXXXXXX-X`.
     */
    public fun formatRncDisplay(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return ""
        val digits = filterDigitsOnly(trimmed, RNC_PERSONAL_DIGITS + 1)
        if (digits.length == RNC_COMPANY_DIGITS) return formatNineDigitRnc(digits)
        if (digits.length == RNC_PERSONAL_DIGITS) return formatElevenDigitRnc(digits)
        return trimmed
    }

    /**
     * Formats a monetary amount in Dominican Pesos (`RD$12,500.00` or without decimals if whole).
     */
    public fun formatCurrencyDop(
        amount: Double,
        includeDecimalsWhenZero: Boolean = true,
    ): String {
        val isNegative = amount < 0.0
        val absoluteValue = if (isNegative) -amount else amount
        val totalCents = (absoluteValue * 100.0).roundToLong()
        val wholePart = totalCents / 100L
        val centsPart = (totalCents % 100L).toInt()
        val formattedWhole = formatGroupedThousands(wholePart)
        val signPrefix = if (isNegative) "-" else ""
        val omitZeroDecimals = !includeDecimalsWhenZero && centsPart == 0
        if (omitZeroDecimals) {
            return "${signPrefix}RD$$formattedWhole"
        }
        val paddedCents = centsPart.toString().padStart(2, '0')
        return "${signPrefix}RD$$formattedWhole.$paddedCents"
    }

    /**
     * Formats a whole integer amount in Dominican Pesos (`RD$12,500`).
     */
    public fun formatCurrencyDopInt(amount: Int): String = formatCurrencyDop(
        amount = amount.toDouble(),
        includeDecimalsWhenZero = false,
    )

    /**
     * Formats a 24-hour `HH:mm` or display time string into `h:mm AM/PM`.
     */
    public fun formatTimeDisplay12h(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return ""
        val totalMinutes = parseFlexibleTimeMinutes(trimmed) ?: return trimmed
        val hour24 = totalMinutes / 60
        val minute = totalMinutes % 60
        val suffix = if (hour24 >= 12) "PM" else "AM"
        val moduloHour = hour24 % 12
        val hour12 = if (moduloHour == 0) 12 else moduloHour
        val paddedMinute = minute.toString().padStart(2, '0')
        return "$hour12:$paddedMinute $suffix"
    }

    /**
     * Formats an opening/closing hour pair into `"9:00 AM – 6:00 PM"`.
     */
    public fun formatBusinessHoursDisplay(
        startRaw: String,
        endRaw: String,
    ): String {
        val startFormatted = formatTimeDisplay12h(startRaw)
        val endFormatted = formatTimeDisplay12h(endRaw)
        if (startFormatted.isEmpty()) return endFormatted
        if (endFormatted.isEmpty()) return startFormatted
        return "$startFormatted – $endFormatted"
    }

    /**
     * Formats a DD/MM/YYYY date into a Spanish display label (`"20 de mayo de 2026"` or `"20 de mayo"`).
     */
    public fun formatSpanishDateLabel(
        rawDate: String,
        includeYear: Boolean = true,
    ): String {
        val trimmed = rawDate.trim()
        val parsed = parseDateTriple(trimmed) ?: return trimmed
        val day = parsed.first
        val month = parsed.second
        val year = parsed.third
        if (month < 1) return trimmed
        if (month > 12) return trimmed
        val maxDay = maxDaysInMonth(month, year)
        if (day < 1) return trimmed
        if (day > maxDay) return trimmed
        val monthName = spanishMonthName(month)
        if (includeYear) return "$day de $monthName de $year"
        return "$day de $monthName"
    }

    /**
     * Masks a card PAN for safe UI display (`•••• •••• •••• 4242`).
     */
    public fun formatCardMaskedDisplay(rawCardNumber: String): String {
        val digits = filterDigitsOnly(rawCardNumber, MAX_CARD_DIGITS)
        if (digits.length < 4) return "••••"
        val lastFour = digits.takeLast(4)
        return "•••• •••• •••• $lastFour"
    }

    private fun validateRequiredAndLengthBounds(
        trimmed: String,
        fieldLabel: String,
        isRequired: Boolean,
        minLength: Int,
        maxLength: Int,
    ): String? {
        if (trimmed.isEmpty()) {
            if (isRequired) return "$fieldLabel es obligatorio."
            return null
        }
        if (trimmed.length < minLength) {
            return "$fieldLabel debe tener al menos $minLength caracteres."
        }
        if (trimmed.length > maxLength) {
            return "$fieldLabel no puede exceder $maxLength caracteres."
        }
        return null
    }

    private fun validateDateInRange(
        rawValue: String,
        fieldLabel: String,
        isRequired: Boolean,
        minYear: Int,
        maxYear: Int,
    ): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            if (isRequired) return "$fieldLabel es obligatoria."
            return null
        }
        if (trimmed.length != MAX_BIRTH_DATE_LENGTH) {
            return "$fieldLabel debe usar el formato DD/MM/AAAA."
        }
        if (!birthDateRegex.matches(trimmed)) {
            return "$fieldLabel debe usar el formato DD/MM/AAAA."
        }
        val parsed = parseDateTriple(trimmed) ?: return "$fieldLabel debe usar el formato DD/MM/AAAA."
        val day = parsed.first
        val month = parsed.second
        val year = parsed.third
        if (month < 1) return "El mes debe estar entre 01 y 12."
        if (month > 12) return "El mes debe estar entre 01 y 12."
        if (year < minYear) return "El año debe estar entre $minYear y $maxYear."
        if (year > maxYear) return "El año debe estar entre $minYear y $maxYear."
        val maxDay = maxDaysInMonth(month, year)
        if (day < 1) return "El día no es válido para el mes seleccionado."
        if (day > maxDay) return "El día no es válido para el mes seleccionado."
        return null
    }

    private fun parseDateTriple(trimmedDate: String): Triple<Int, Int, Int>? {
        if (!birthDateRegex.matches(trimmedDate)) return null
        val parts = trimmedDate.split("/")
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        return Triple(day, month, year)
    }

    private fun isValidDominicanDigits(digits: String): Boolean {
        if (digits.length == MIN_PHONE_DIGITS) {
            val areaCode = digits.substring(0, 3)
            return areaCode in dominicanAreaCodes
        }
        val isElevenWithUsPrefix = digits.length == 11 && digits[0] == '1'
        if (isElevenWithUsPrefix) {
            val areaCode = digits.substring(1, 4)
            return areaCode in dominicanAreaCodes
        }
        return false
    }

    private fun formatTenDigitPhoneMask(digits: String): String {
        if (digits.length <= 3) return digits
        if (digits.length <= 6) {
            val area = digits.substring(0, 3)
            val middle = digits.substring(3)
            return "($area) $middle"
        }
        val area = digits.substring(0, 3)
        val middle = digits.substring(3, 6)
        val tail = digits.substring(6)
        return "($area) $middle-$tail"
    }

    private fun formatNineDigitRnc(digits: String): String {
        if (digits.length <= 1) return digits
        if (digits.length <= 3) {
            return "${digits.substring(0, 1)}-${digits.substring(1)}"
        }
        if (digits.length <= 8) {
            return "${digits.substring(0, 1)}-${digits.substring(1, 3)}-${digits.substring(3)}"
        }
        return "${digits.substring(0, 1)}-${digits.substring(1, 3)}-${digits.substring(3, 8)}-${digits.substring(8)}"
    }

    private fun formatElevenDigitRnc(digits: String): String {
        if (digits.length <= 3) return digits
        if (digits.length <= 10) {
            return "${digits.substring(0, 3)}-${digits.substring(3)}"
        }
        return "${digits.substring(0, 3)}-${digits.substring(3, 10)}-${digits.substring(10)}"
    }

    private fun formatGroupedThousands(wholeValue: Long): String {
        val raw = wholeValue.toString()
        if (raw.length <= 3) return raw
        val builder = StringBuilder()
        var count = 0
        for (index in raw.length - 1 downTo 0) {
            builder.append(raw[index])
            count += 1
            val shouldInsertComma = count % 3 == 0 && index > 0
            if (shouldInsertComma) {
                builder.append(',')
            }
        }
        return builder.reverse().toString()
    }

    private fun normalizeCurrencyText(trimmed: String): String {
        val stripped = trimmed
            .replace("RD$", "", ignoreCase = true)
            .replace("$", "")
            .replace(" ", "")
        return stripped.replace(",", "")
    }

    private fun parseTime24Minutes(value: String): Int? {
        if (!time24Regex.matches(value)) return null
        val parts = value.split(":")
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour < 0) return null
        if (hour > 23) return null
        if (minute < 0) return null
        if (minute > 59) return null
        return hour * 60 + minute
    }

    private fun parseFlexibleTimeMinutes(rawValue: String): Int? {
        val trimmed = rawValue.trim()
        val time24 = parseTime24Minutes(trimmed)
        if (time24 != null) return time24
        val upper = trimmed.uppercase()
        val isAm = upper.endsWith("AM")
        val isPm = upper.endsWith("PM")
        val hasMeridiemSuffix = isAm || isPm
        if (!hasMeridiemSuffix) return null
        val clockPart = upper.removeSuffix("AM").removeSuffix("PM").trim()
        val parts = clockPart.split(":")
        if (parts.size != 2) return null
        val hour12 = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour12 < 1) return null
        if (hour12 > 12) return null
        if (minute < 0) return null
        if (minute > 59) return null
        val isMidnightTwelve = isAm && hour12 == 12
        val isAfternoonHour = isPm && hour12 != 12
        val normalizedHour = when {
            isMidnightTwelve -> 0
            isAfternoonHour -> hour12 + 12
            else -> hour12
        }
        return normalizedHour * 60 + minute
    }

    private fun isValidLuhn(digits: String): Boolean {
        var sum = 0
        var shouldDouble = false
        for (index in digits.length - 1 downTo 0) {
            val numeric = digits[index].digitToIntOrNull() ?: return false
            val adjusted = if (shouldDouble) {
                val doubled = numeric * 2
                if (doubled > 9) doubled - 9 else doubled
            } else {
                numeric
            }
            sum += adjusted
            shouldDouble = !shouldDouble
        }
        return sum % 10 == 0
    }

    private fun spanishMonthName(monthNumber: Int): String = when (monthNumber) {
        1 -> "enero"
        2 -> "febrero"
        3 -> "marzo"
        4 -> "abril"
        5 -> "mayo"
        6 -> "junio"
        7 -> "julio"
        8 -> "agosto"
        9 -> "septiembre"
        10 -> "octubre"
        11 -> "noviembre"
        else -> "diciembre"
    }

    private fun isAllowedNameCharacter(character: Char): Boolean {
        if (character.isLetter()) return true
        if (character == ' ') return true
        if (character == '\'') return true
        if (character == '-') return true
        return character == '.'
    }

    private fun isAllowedPhoneCharacter(character: Char): Boolean {
        if (character.isDigit()) return true
        if (character == '-') return true
        if (character == ' ') return true
        if (character == '+') return true
        if (character == '(') return true
        return character == ')'
    }

    private fun isAllowedRncCharacter(character: Char): Boolean {
        if (character.isDigit()) return true
        if (character == '-') return true
        return character == ' '
    }

    private fun isAllowedCardCharacter(character: Char): Boolean {
        if (character.isDigit()) return true
        if (character == ' ') return true
        return character == '-'
    }

    private fun maxDaysInMonth(month: Int, year: Int): Int = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    private fun isLeapYear(year: Int): Boolean {
        val divisibleBy400 = year % 400 == 0
        if (divisibleBy400) return true
        val divisibleBy100 = year % 100 == 0
        if (divisibleBy100) return false
        return year % 4 == 0
    }
}
