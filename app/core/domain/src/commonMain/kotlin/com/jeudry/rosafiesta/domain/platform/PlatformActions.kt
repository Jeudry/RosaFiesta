package com.jeudry.rosafiesta.domain.platform

/**
 * Binary payload and metadata of a user-selected media or document file (`jpg`, `jpeg`, `png`, `pdf`).
 */
public data class PickedMediaFile(
    public val fileName: String,
    public val mimeType: String,
    public val sizeBytes: Long,
    public val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedMediaFile) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (sizeBytes != other.sizeBytes) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + sizeBytes.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

/**
 * Multiplatform OS integration contract for external URLs, native share sheets, document exports, and media picking.
 */
public interface PlatformActions {
    /**
     * Resolves the default backend base URL for the current platform and environment overrides.
     */
    public fun defaultApiBaseUrl(): String

    /**
     * Opens an external `https://`, `http://`, `tel:`, or `mailto:` URL in the host OS browser, dialer, or maps app.
     */
    public fun openExternalUrl(url: String): Boolean

    /**
     * Invokes the host OS share flow or desktop/web export fallback with optional binary attachment.
     */
    public fun shareContent(
        title: String,
        text: String,
        fileName: String? = null,
        fileBytes: ByteArray? = null,
        mimeType: String = "application/pdf",
    ): Boolean

    /**
     * Saves or downloads a generated document (`.pdf`, `.csv`, `.txt`) and returns the saved location or filename.
     */
    public fun saveDocument(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String?

    /**
     * Opens the native file/media selector filtered to [allowedExtensions] and reads the chosen file bytes.
     */
    public suspend fun pickMediaFile(
        allowedExtensions: List<String> = listOf("jpg", "jpeg", "png", "pdf"),
    ): PickedMediaFile?

    /**
     * Launches the platform phone dialer for [rawPhone].
     */
    public fun dialPhoneNumber(rawPhone: String): Boolean {
        val telUrl = ExternalUrlBuilder.buildPhoneTelUrl(rawPhone) ?: return false
        return openExternalUrl(telUrl)
    }

    /**
     * Launches a WhatsApp conversation for [rawPhone] with an optional pre-filled [message].
     */
    public fun openWhatsAppChat(rawPhone: String, message: String = ""): Boolean {
        val whatsAppUrl = ExternalUrlBuilder.buildWhatsAppUrl(rawPhone, message) ?: return false
        return openExternalUrl(whatsAppUrl)
    }

    /**
     * Opens Google Maps or Apple Maps search for [query].
     */
    public fun openMapsSearch(query: String, preferAppleMaps: Boolean = false): Boolean {
        val mapsUrl = if (preferAppleMaps) {
            ExternalUrlBuilder.buildAppleMapsSearchUrl(query)
        } else {
            ExternalUrlBuilder.buildGoogleMapsSearchUrl(query)
        }
        val resolvedUrl = mapsUrl ?: return false
        return openExternalUrl(resolvedUrl)
    }
}

/**
 * Pure-Kotlin URL, filename, and MIME helpers for phone calls, WhatsApp, Maps, and exported documents.
 */
public object ExternalUrlBuilder {
    private const val UNRESERVED_URI_CHARS: String =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"
    private const val HEX_UPPER: String = "0123456789ABCDEF"

    /**
     * Builds a sanitized `tel:` URI for Dominican or international phone numbers, or `null` if invalid.
     */
    public fun buildPhoneTelUrl(rawPhone: String): String? {
        val trimmed = rawPhone.trim()
        if (trimmed.isEmpty()) return null

        val hasExplicitPlus = trimmed.startsWith("+")
        val digitsOnly = buildString {
            for (character in trimmed) {
                if (character.isDigit()) {
                    append(character)
                }
            }
        }
        if (digitsOnly.length < 7) return null

        val normalizedNumber = if (hasExplicitPlus) {
            "+$digitsOnly"
        } else if (isDominicanTenDigitNumber(digitsOnly)) {
            "+1$digitsOnly"
        } else {
            digitsOnly
        }
        return "tel:$normalizedNumber"
    }

    /**
     * Builds an official `https://wa.me/<digits>` deep link with optional URL-encoded [message].
     */
    public fun buildWhatsAppUrl(rawPhone: String, message: String = ""): String? {
        val digitsOnly = buildString {
            for (character in rawPhone.trim()) {
                if (character.isDigit()) {
                    append(character)
                }
            }
        }
        if (digitsOnly.length < 7) return null

        val internationalDigits = if (isDominicanTenDigitNumber(digitsOnly)) {
            "1$digitsOnly"
        } else {
            digitsOnly
        }
        val cleanMessage = message.trim()
        if (cleanMessage.isEmpty()) {
            return "https://wa.me/$internationalDigits"
        }
        val encodedText = percentEncode(cleanMessage)
        return "https://wa.me/$internationalDigits?text=$encodedText"
    }

    /**
     * Builds a universal Google Maps search URL for a venue address or sector in Dominican Republic.
     */
    public fun buildGoogleMapsSearchUrl(query: String): String? {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return null
        val encodedQuery = percentEncode(cleanQuery)
        return "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
    }

    /**
     * Builds an Apple Maps search URL for iOS and macOS devices.
     */
    public fun buildAppleMapsSearchUrl(query: String): String? {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return null
        val encodedQuery = percentEncode(cleanQuery)
        return "https://maps.apple.com/?q=$encodedQuery"
    }

    /**
     * RFC 3986 percent-encoder over UTF-8 bytes.
     */
    public fun percentEncode(raw: String): String {
        if (raw.isEmpty()) return ""
        val utf8Bytes = raw.encodeToByteArray()
        val builder = StringBuilder(utf8Bytes.size * 2)
        for (byteValue in utf8Bytes) {
            val unsigned = byteValue.toInt() and 0xFF
            val asciiChar = unsigned.toChar()
            val isUnreserved = UNRESERVED_URI_CHARS.indexOf(asciiChar) >= 0
            if (isUnreserved) {
                builder.append(asciiChar)
            } else {
                builder.append('%')
                builder.append(HEX_UPPER[unsigned ushr 4])
                builder.append(HEX_UPPER[unsigned and 0x0F])
            }
        }
        return builder.toString()
    }

    /**
     * Infers the standard MIME type from [fileName] extension.
     */
    public fun inferMimeType(
        fileName: String,
        fallbackMimeType: String = "application/octet-stream",
    ): String {
        val extension = fileName.trim().substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> "application/pdf"
            "csv" -> "text/csv"
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> fallbackMimeType
        }
    }

    /**
     * Sanitizes a user-facing filename by stripping path separators and unsafe characters.
     */
    public fun sanitizeFileName(
        rawFileName: String,
        fallbackFileName: String = "rosafiesta-documento.pdf",
    ): String {
        val trimmed = rawFileName.trim()
        if (trimmed.isEmpty()) return fallbackFileName
        val cleaned = buildString {
            for (character in trimmed) {
                val isAlphaNum = character.isLetterOrDigit()
                val isAllowedSymbol = character == '.' || character == '-' || character == '_'
                if (isAlphaNum) {
                    append(character)
                } else if (isAllowedSymbol) {
                    append(character)
                } else {
                    append('_')
                }
            }
        }.trim('_').trim('.')
        return cleaned.ifEmpty { fallbackFileName }
    }

    /**
     * Normalizes a backend base URL, stripping trailing slashes and falling back when blank.
     */
    public fun normalizeBaseUrl(
        candidateUrl: String?,
        fallbackUrl: String = "http://localhost:8788",
    ): String {
        val trimmed = candidateUrl?.trim().orEmpty().trimEnd('/')
        if (trimmed.isEmpty()) return fallbackUrl
        val isHttp = trimmed.startsWith("http://")
        val isHttps = trimmed.startsWith("https://")
        if (isHttp) return trimmed
        if (isHttps) return trimmed
        return fallbackUrl
    }

    private fun isDominicanTenDigitNumber(digits: String): Boolean {
        if (digits.length != 10) return false
        val areaCode = digits.take(3)
        if (areaCode == "809") return true
        if (areaCode == "829") return true
        return areaCode == "849"
    }
}

/**
 * Deterministic in-memory [PlatformActions] implementation for unit tests.
 */
public class NoOpPlatformActions(
    private val configuredBaseUrl: String = "http://localhost:8788",
) : PlatformActions {
    public var lastOpenedUrl: String? = null
        private set

    public var lastSharedTitle: String? = null
        private set

    public var lastSharedText: String? = null
        private set

    public var lastSavedFileName: String? = null
        private set

    public var lastSavedBytes: ByteArray? = null
        private set

    public var queuedPickedFile: PickedMediaFile? = null

    override fun defaultApiBaseUrl(): String = ExternalUrlBuilder.normalizeBaseUrl(configuredBaseUrl)

    override fun openExternalUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        lastOpenedUrl = trimmed
        return true
    }

    override fun shareContent(
        title: String,
        text: String,
        fileName: String?,
        fileBytes: ByteArray?,
        mimeType: String,
    ): Boolean {
        lastSharedTitle = title.trim()
        lastSharedText = text.trim()
        if (fileBytes != null) {
            lastSavedBytes = fileBytes.copyOf()
            lastSavedFileName = fileName?.let { rawName -> ExternalUrlBuilder.sanitizeFileName(rawName) }
        }
        return true
    }

    override fun saveDocument(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String? {
        if (bytes.isEmpty()) return null
        val safeName = ExternalUrlBuilder.sanitizeFileName(fileName)
        lastSavedFileName = safeName
        lastSavedBytes = bytes.copyOf()
        return safeName
    }

    override suspend fun pickMediaFile(allowedExtensions: List<String>): PickedMediaFile? = queuedPickedFile
}
