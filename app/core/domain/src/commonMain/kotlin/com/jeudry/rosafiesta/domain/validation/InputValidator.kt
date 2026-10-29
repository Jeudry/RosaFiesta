package com.jeudry.rosafiesta.domain.validation

/**
 * Pure domain validator for authentication, registration, and user profile fields.
 * Enforces required status, minimum and maximum lengths, and strict format patterns.
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

    private val emailRegex: Regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    private val usernameRegex: Regex = Regex("^[A-Za-z0-9._-]+$")
    private val birthDateRegex: Regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")

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

    private fun maxDaysInMonth(month: Int, year: Int): Int {
        return when (month) {
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        val divisibleBy400 = year % 400 == 0
        if (divisibleBy400) return true
        val divisibleBy100 = year % 100 == 0
        if (divisibleBy100) return false
        return year % 4 == 0
    }
}
