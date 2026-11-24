package com.jeudry.rosafiesta.domain.platform

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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
 * Outcome returned by a platform browser-based OAuth PKCE authorization flow.
 */
public data class OAuthBrowserOutcome(
    public val code: String? = null,
    public val state: String? = null,
    public val redirectUri: String = "",
    public val errorCode: String? = null,
)

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
     * Returns whether the host OS currently reports an active unmetered/Wi-Fi network transport.
     */
    public fun isOnWifiNetwork(): Boolean = true

    /**
     * Invokes host OS biometric/device-owner verification and invokes [onResult] with success state and optional reason code.
     */
    public fun authenticateDeviceOwner(
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        onResult(false, "DEVICE_AUTH_UNAVAILABLE")
    }

    /**
     * Executes an external-browser OAuth PKCE login session, computing [resolveAuthorizeUrl] with
     * the platform's active callback URI and awaiting the redirected authorization code.
     */
    public suspend fun launchOAuthBrowserFlow(
        resolveAuthorizeUrl: suspend (redirectUri: String) -> String?,
    ): OAuthBrowserOutcome = OAuthBrowserOutcome(errorCode = "OAUTH_BROWSER_UNAVAILABLE")

    /**
     * Requests OS notification permission / push token registration for [appVariant] (`personal` or `admin`).
     */
    public fun registerPushNotifications(
        appVariant: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        onResult("unknown", null)
    }

    /**
     * Requests OS notification permission or Web Push subscription using optional server [vapidPublicKey].
     */
    public fun registerPushNotificationsWithConfig(
        appVariant: String,
        vapidPublicKey: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        registerPushNotifications(appVariant, onResult)
    }

    /**
     * Displays an immediate local OS notification when remote alerts arrive while enabled by user preferences.
     */
    public fun showLocalNotification(title: String, body: String): Unit = Unit

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
            "svg" -> "image/svg+xml"
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
    public var simulatedWifiConnected: Boolean = true
    public var simulatedDeviceAuthOutcome: Pair<Boolean, String?> = false to "DEVICE_AUTH_UNAVAILABLE"
    public var queuedOAuthOutcome: OAuthBrowserOutcome = OAuthBrowserOutcome(errorCode = "OAUTH_BROWSER_UNAVAILABLE")
    public var simulatedPushRegistration: Pair<String, String?> = "android" to null
    public val shownNotifications: MutableList<Pair<String, String>> = mutableListOf()
    public var lastOAuthAuthorizeUrlRequested: String? = null
        private set

    override fun isOnWifiNetwork(): Boolean = simulatedWifiConnected

    override fun authenticateDeviceOwner(
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        onResult(simulatedDeviceAuthOutcome.first, simulatedDeviceAuthOutcome.second)
    }

    override suspend fun launchOAuthBrowserFlow(
        resolveAuthorizeUrl: suspend (redirectUri: String) -> String?,
    ): OAuthBrowserOutcome {
        val targetRedirect = queuedOAuthOutcome.redirectUri.ifBlank { "http://127.0.0.1:43123/callback" }
        lastOAuthAuthorizeUrlRequested = resolveAuthorizeUrl(targetRedirect)
        return queuedOAuthOutcome.copy(redirectUri = targetRedirect)
    }

    override fun registerPushNotifications(
        appVariant: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        onResult(simulatedPushRegistration.first, simulatedPushRegistration.second)
    }

    override fun showLocalNotification(title: String, body: String) {
        shownNotifications.add(title to body)
    }

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

/**
 * Result delivered when an external Stripe Checkout session returns via deep link or browser popup.
 */
public data class ExternalPaymentReturn(
    public val sessionId: String,
    public val eventId: String = "",
    public val cancelled: Boolean = false,
)

/**
 * Multiplatform event bridge relaying Stripe Checkout return callbacks to active ViewModels.
 */
public object ExternalPaymentCallbackBridge {
    private val _paymentReturns: MutableSharedFlow<ExternalPaymentReturn> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 8)
    public val paymentReturns: SharedFlow<ExternalPaymentReturn> = _paymentReturns.asSharedFlow()

    /**
     * Clears any cached replay emission after an active ViewModel consumes the payment return.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    public fun clearReplayedReturn() {
        _paymentReturns.resetReplayCache()
    }

    /**
     * Parses and broadcasts an incoming `rosafiesta://payment/...` or HTTPS return URL.
     */
    public fun dispatchPaymentDeepLink(rawUrl: String): Boolean {
        val parsed = parsePaymentReturnUrl(rawUrl) ?: return false
        _paymentReturns.tryEmit(parsed)
        return true
    }

    /**
     * Broadcasts a structured payment completion payload received via browser `postMessage`.
     */
    public fun dispatchPaymentPayload(
        sessionId: String,
        eventId: String = "",
        cancelled: Boolean = false,
    ) {
        val cleanSession = sessionId.trim()
        val hasSession = cleanSession.isNotEmpty()
        val isEmitAllowed = if (cancelled) true else hasSession
        if (!isEmitAllowed) return
        _paymentReturns.tryEmit(
            ExternalPaymentReturn(
                sessionId = cleanSession,
                eventId = eventId.trim(),
                cancelled = cancelled,
            ),
        )
    }

    /**
     * Parses `rosafiesta://payment/success?session_id=...` or `/cancel` deep link URLs.
     */
    public fun parsePaymentReturnUrl(rawUrl: String): ExternalPaymentReturn? {
        val clean = rawUrl.trim()
        val lower = clean.lowercase()
        val isPaymentScheme = lower.startsWith("rosafiesta://payment")
        val hasSessionParam = lower.contains("session_id=")
        val hasCancelFlag = lower.contains("cancelled=1")
        val isPaymentCallback = if (isPaymentScheme) {
            true
        } else if (lower.contains("/pagos")) {
            hasSessionParam || hasCancelFlag
        } else {
            false
        }
        if (!isPaymentCallback) return null
        val query = clean.substringAfter("?", missingDelimiterValue = "")
        val params = parseQueryParams(query)
        val sessionId = params["session_id"].orEmpty().trim()
        val eventId = params["event_id"].orEmpty().trim()
        val isCancelPath = lower.contains("/payment/cancel")
        val isCancelledParam = params["cancelled"] == "1"
        val isCancelled = if (isCancelPath) true else isCancelledParam
        if (!isCancelled) {
            if (sessionId.isEmpty()) return null
        }
        return ExternalPaymentReturn(
            sessionId = sessionId,
            eventId = eventId,
            cancelled = isCancelled,
        )
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        val map = linkedMapOf<String, String>()
        val pairs = query.split("&")
        for (pair in pairs) {
            if (pair.isBlank()) continue
            val key = pair.substringBefore("=").trim()
            val value = pair.substringAfter("=", missingDelimiterValue = "").trim()
            if (key.isNotEmpty()) {
                map[key] = value
            }
        }
        return map
    }
}

/**
 * Multiplatform event bridge relaying OS background transitions (`Activity.onStop`, etc.)
 * to active ViewModels so biometric session locks engage immediately.
 */
public object AppLifecycleBridge {
    private val _backgroundEvents: MutableSharedFlow<Unit> =
        MutableSharedFlow(extraBufferCapacity = 4)

    /**
     * Stream emitting whenever the host application enters background state.
     */
    public val backgroundEvents: SharedFlow<Unit> = _backgroundEvents.asSharedFlow()

    /**
     * Notifies active ViewModels that the app moved to the background.
     */
    public fun dispatchAppBackgrounded() {
        _backgroundEvents.tryEmit(Unit)
    }
}

/**
 * Multiplatform event bridge relaying full-page OAuth browser redirects (such as web reloads
 * reading `sessionStorage`) to active ViewModels.
 */
public object ExternalOAuthCallbackBridge {
    private val _oauthReturns: MutableSharedFlow<OAuthBrowserOutcome> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 4)

    /**
     * Stream emitting recovered OAuth authorization callback payloads.
     */
    public val oauthReturns: SharedFlow<OAuthBrowserOutcome> = _oauthReturns.asSharedFlow()

    /**
     * Clears any cached replay emission after an active ViewModel consumes the OAuth return.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    public fun clearReplayedReturn() {
        _oauthReturns.resetReplayCache()
    }

    /**
     * Broadcasts an [OAuthBrowserOutcome] payload to active ViewModels.
     */
    public fun dispatchOAuthReturn(outcome: OAuthBrowserOutcome) {
        _oauthReturns.tryEmit(outcome)
    }
}
