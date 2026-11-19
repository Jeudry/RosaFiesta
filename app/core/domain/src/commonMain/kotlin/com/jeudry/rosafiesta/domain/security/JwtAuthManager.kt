package com.jeudry.rosafiesta.domain.security

import kotlin.time.Clock

/**
 * Synced user privacy, notification, and security preferences.
 */
public data class UserPreferences(
    public val pushNotificationsEnabled: Boolean = true,
    public val whatsappAlertsEnabled: Boolean = true,
    public val emailReceiptsEnabled: Boolean = true,
    public val biometricLockEnabled: Boolean = false,
    public val autoSyncWifiOnly: Boolean = false,
)

/**
 * Persistent user account model containing salted password hash and identity metadata.
 */
public data class RegisteredUserAccount(
    public val id: String,
    public val username: String,
    public val email: String,
    public val fullName: String,
    public val passwordHash: String,
    public val salt: String,
    public val birthDate: String,
    public val phone: String,
    public val role: String,
    public val preferences: UserPreferences = UserPreferences(),
)

/**
 * Decoded claims extracted from a server JWT or local offline session envelope after expiration validation.
 */
public data class JwtClaims(
    public val subjectId: String,
    public val username: String,
    public val email: String,
    public val fullName: String,
    public val role: String,
    public val birthDate: String,
    public val issuedAtEpochSeconds: Long,
    public val expiresAtEpochSeconds: Long,
)

/**
 * Structured client session metadata persisted in [SecureStorage] alongside a server access token.
 */
public data class ClientSessionMetadata(
    public val userId: String,
    public val username: String = "",
    public val email: String,
    public val fullName: String = "",
    public val role: String,
    public val birthDate: String = "",
    public val activeView: String = "personal",
    public val issuedAtEpochSeconds: Long = 0L,
    public val expiresAtEpochSeconds: Long = 0L,
)

/**
 * Active client session restored from [SecureStorage] after validating token expiration.
 */
public data class PersistedClientSession(
    public val accessToken: String,
    public val userId: String,
    public val username: String,
    public val email: String,
    public val fullName: String,
    public val role: String,
    public val birthDate: String,
    public val activeView: String,
    public val issuedAtEpochSeconds: Long,
    public val expiresAtEpochSeconds: Long,
    public val claims: JwtClaims? = null,
)

/**
 * Result of an authentication, registration, or password reset operation.
 */
public data class AuthOutcome(
    public val success: Boolean,
    public val errorCode: String? = null,
    public val errorMessage: String? = null,
    public val accessToken: String? = null,
    public val refreshToken: String? = null,
    public val account: RegisteredUserAccount? = null,
) {
    public companion object {
        /**
         * Builds a successful authentication outcome with an access token, optional refresh token, and account.
         */
        public fun authenticated(
            accessToken: String,
            account: RegisteredUserAccount,
            refreshToken: String? = null,
        ): AuthOutcome = AuthOutcome(
            success = true,
            accessToken = accessToken,
            refreshToken = refreshToken?.trim()?.ifEmpty { null },
            account = account,
        )

        /**
         * Builds a failed authentication outcome with a stable business error code and message.
         */
        public fun failure(
            errorCode: String,
            errorMessage: String,
        ): AuthOutcome = AuthOutcome(
            success = false,
            errorCode = errorCode,
            errorMessage = errorMessage,
        )
    }
}

/**
 * Multiplatform client authentication manager for salted SHA-256 local credential verification,
 * server JWT claim inspection with strict expiration enforcement, and encrypted [SecureStorage] session persistence.
 *
 * Client binaries never embed server JWT signing secrets or forge server-signed HS256 tokens.
 */
public object JwtAuthManager {
    private const val PASSWORD_HASH_ROUNDS: Int = 64
    private const val DEFAULT_TOKEN_TTL_SECONDS: Long = 86_400L * 7L
    private const val LOCAL_SESSION_PREFIX: String = "rf-local"
    private const val BASE64_URL_ALPHABET: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    private val sha256RoundConstants: IntArray = intArrayOf(
        0x428a2f98, 0x71374491, -0x4a3f0431, -0x164a245b, 0x3956c25b, 0x59f111f1, -0x6dc07d5c, -0x54e3a12b,
        -0x27f85568, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, -0x7f214e02, -0x6423f959, -0x3e640e8c,
        -0x1b64963f, -0x1041b87a, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        -0x67c1aeae, -0x57ce3993, -0x4ffcd838, -0x40a68039, -0x391ff40d, -0x2a586eb9, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, -0x7e3d36d2, -0x6d8dd37b,
        -0x5d40175f, -0x57e599b5, -0x3db47490, -0x3893ae5d, -0x2e6d17e7, -0x2966f9dc, -0xbf1ca7b, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, -0x7b3787ec, -0x7338fdf8, -0x6f410006, -0x5baf9315, -0x41065c09, -0x398e870e,
    )

    /**
     * Canonical offline demo accounts with pre-hashed local credentials.
     */

    /**
     * Admin and staff accounts seeded when starting the application cleanly from scratch.
     */
    public val defaultAdminSeedAccounts: List<RegisteredUserAccount> by lazy {
        listOf(
            createSeedAccount(
                id = "usr-maria",
                username = "maria",
                email = "maria@rosafiesta.do",
                fullName = "María",
                birthDate = "12/04/1990",
                phone = "809-555-0101",
                role = "admin",
            ),
            createSeedAccount(
                id = "usr-yuli",
                username = "yuli",
                email = "yuli@rosafiesta.do",
                fullName = "Yuli",
                birthDate = "22/09/1996",
                phone = "809-555-0102",
                role = "staff_yuli",
            ),
        )
    }

    /**
     * Default seeded accounts for tests and canonical demo mode.
     */
    public val defaultSeedAccounts: List<RegisteredUserAccount> by lazy {
        listOf(
            createSeedAccount(
                id = "usr-camila",
                username = "camila",
                email = "camila@rosafiesta.do",
                fullName = "Camila Rodríguez",
                birthDate = "15/08/1998",
                phone = "809-555-0142",
                role = "client",
            ),
        ) + defaultAdminSeedAccounts + listOf(
            createSeedAccount(
                id = "usr-pedro",
                username = "pedro",
                email = "pedro@rosafiesta.do",
                fullName = "Pedro",
                birthDate = "05/11/1993",
                phone = "809-555-0103",
                role = "staff_pedro",
            ),
            createSeedAccount(
                id = "usr-laura",
                username = "laura",
                email = "laura@rosafiesta.do",
                fullName = "Laura Peña",
                birthDate = "19/02/1995",
                phone = "809-555-0104",
                role = "client",
            ),
            createSeedAccount(
                id = "usr-ana",
                username = "ana",
                email = "ana@rosafiesta.do",
                fullName = "Ana Gómez",
                birthDate = "30/06/1997",
                phone = "809-555-0105",
                role = "client",
            ),
        )
    }

    /**
     * Returns the current wall-clock UTC epoch time in seconds.
     */
    public fun currentEpochSeconds(): Long = Clock.System.now().epochSeconds

    private fun createSeedAccount(
        id: String,
        username: String,
        email: String,
        fullName: String,
        birthDate: String,
        phone: String,
        role: String,
    ): RegisteredUserAccount {
        val salt = generateDeterministicSalt(email)
        val passwordHash = hashPassword("rosa2026", salt)
        return RegisteredUserAccount(
            id = id,
            username = username,
            email = email,
            fullName = fullName,
            passwordHash = passwordHash,
            salt = salt,
            birthDate = birthDate,
            phone = phone,
            role = role,
        )
    }

    /**
     * Generates a deterministic salt for an account identifier.
     */
    public fun generateDeterministicSalt(identifier: String): String {
        val normalized = identifier.trim().lowercase()
        val saltedInput = "rf-salt-v1:$normalized".encodeToByteArray()
        val digest = sha256(saltedInput)
        return bytesToHex(digest).take(24)
    }

    /**
     * Derives a multi-round salted HMAC-SHA256 password hash.
     */
    public fun hashPassword(rawPassword: String, salt: String): String {
        val cleanSalt = salt.trim().ifEmpty { "rf-default-salt" }
        val keyBytes = "rf-kdf-domain:$cleanSalt".encodeToByteArray()
        val trimmedPassword = rawPassword.trim()
        var currentBlock = "$cleanSalt:$trimmedPassword".encodeToByteArray()
        for (roundIndex in 0 until PASSWORD_HASH_ROUNDS) {
            currentBlock = hmacSha256(keyBytes, currentBlock)
        }
        return bytesToHex(currentBlock)
    }

    /**
     * Verifies a password in constant time against a stored salt and hash.
     */
    public fun verifyPassword(
        rawPassword: String,
        salt: String,
        expectedHash: String,
    ): Boolean {
        val computed = hashPassword(rawPassword, salt)
        val normalizedExpected = expectedHash.trim()
        return constantTimeEquals(computed, normalizedExpected)
    }

    /**
     * Creates a non-forged local offline session envelope (`rf-local.<payload>`) using real epoch timestamps.
     * Never signs or claims a server HS256 JWT.
     */
    public fun issueAccessToken(
        account: RegisteredUserAccount,
        issuedAtEpochSeconds: Long = currentEpochSeconds(),
        ttlSeconds: Long = DEFAULT_TOKEN_TTL_SECONDS,
    ): String {
        val safeTtl = ttlSeconds.coerceAtLeast(1L)
        val expiresAt = issuedAtEpochSeconds + safeTtl
        val payloadJson = buildClaimsPayloadJson(
            account = account,
            issuedAtEpochSeconds = issuedAtEpochSeconds,
            expiresAtEpochSeconds = expiresAt,
        )
        val payloadBytes = payloadJson.encodeToByteArray()
        val encodedPayload = base64UrlEncode(payloadBytes)
        return "$LOCAL_SESSION_PREFIX.$encodedPayload"
    }

    /**
     * Decodes a 3-part server JWT (`header.payload.signature`) or local offline session envelope
     * and strictly validates its `exp` claim against [clockEpochSeconds].
     *
     * Returns `null` when malformed, missing `exp`, or when `clockEpochSeconds() >= exp`.
     */
    public fun verifyAccessToken(
        token: String,
        clockEpochSeconds: () -> Long = ::currentEpochSeconds,
    ): JwtClaims? {
        val cleanToken = token.trim()
        if (cleanToken.isEmpty()) return null

        val parts = cleanToken.split(".")
        if (parts.size == 3) {
            return decodeThreePartJwtClaims(parts, clockEpochSeconds)
        }
        val isLocalEnvelope = parts.size == 2
        if (isLocalEnvelope) {
            val prefixMatches = parts[0] == LOCAL_SESSION_PREFIX
            if (!prefixMatches) return null
            return decodePayloadClaimsAndCheckExpiry(parts[1], clockEpochSeconds)
        }
        return null
    }

    /**
     * Persists a server access token (3-part JWT or opaque token) and optional [ClientSessionMetadata]
     * into [SecureStorage] after verifying that the session is not already expired.
     */
    public suspend fun persistSession(
        storage: SecureStorage,
        accessToken: String,
        metadata: ClientSessionMetadata? = null,
        clockEpochSeconds: () -> Long = ::currentEpochSeconds,
    ): PersistedClientSession? {
        val cleanToken = accessToken.trim()
        if (cleanToken.isEmpty()) return null

        val nowSeconds = clockEpochSeconds()
        val structuredToken = isStructuredJwtOrLocalToken(cleanToken)
        if (structuredToken) {
            val claims = verifyAccessToken(cleanToken, clockEpochSeconds)
            if (claims == null) {
                clearSession(storage)
                return null
            }
            val resolvedActiveView = metadata?.activeView?.trim()?.ifEmpty { "personal" } ?: "personal"
            val mergedMetadata = ClientSessionMetadata(
                userId = claims.subjectId,
                username = claims.username.ifEmpty { metadata?.username.orEmpty() },
                email = claims.email.ifEmpty { metadata?.email.orEmpty() },
                fullName = claims.fullName.ifEmpty { metadata?.fullName.orEmpty() },
                role = claims.role.ifEmpty { metadata?.role ?: "client" },
                birthDate = claims.birthDate.ifEmpty { metadata?.birthDate.orEmpty() },
                activeView = resolvedActiveView,
                issuedAtEpochSeconds = claims.issuedAtEpochSeconds,
                expiresAtEpochSeconds = claims.expiresAtEpochSeconds,
            )
            storage.put(SecureStorage.KEY_ACCESS_TOKEN, cleanToken)
            val serializedMetadata = serializeSessionMetadata(mergedMetadata)
            storage.put(SecureStorage.KEY_SESSION_METADATA, serializedMetadata)
            storage.put(SecureStorage.KEY_USER_ROLE, mergedMetadata.role)
            storage.put(SecureStorage.KEY_ACTIVE_VIEW, mergedMetadata.activeView)
            return buildPersistedSession(cleanToken, mergedMetadata, claims)
        }

        val hasExplicitExpiry = metadata != null
        if (hasExplicitExpiry) {
            val metadataExpiry = metadata.expiresAtEpochSeconds
            val hasPositiveExpiry = metadataExpiry > 0L
            if (hasPositiveExpiry) {
                val isMetadataExpired = nowSeconds >= metadataExpiry
                if (isMetadataExpired) {
                    clearSession(storage)
                    return null
                }
            }
        }

        val resolvedMetadata = metadata ?: ClientSessionMetadata(
            userId = "authenticated-user",
            email = "",
            role = "client",
            activeView = "personal",
            issuedAtEpochSeconds = nowSeconds,
            expiresAtEpochSeconds = 0L,
        )
        storage.put(SecureStorage.KEY_ACCESS_TOKEN, cleanToken)
        val serializedMetadata = serializeSessionMetadata(resolvedMetadata)
        storage.put(SecureStorage.KEY_SESSION_METADATA, serializedMetadata)
        if (resolvedMetadata.role.isNotBlank()) {
            storage.put(SecureStorage.KEY_USER_ROLE, resolvedMetadata.role)
        }
        if (resolvedMetadata.activeView.isNotBlank()) {
            storage.put(SecureStorage.KEY_ACTIVE_VIEW, resolvedMetadata.activeView)
        }
        return buildPersistedSession(cleanToken, resolvedMetadata, null)
    }

    /**
     * Restores an active session from [SecureStorage], validating `exp` against [clockEpochSeconds].
     * If the stored JWT or session metadata is expired, clears it from [SecureStorage] and returns `null`.
     */
    public suspend fun restoreValidSession(
        storage: SecureStorage,
        clockEpochSeconds: () -> Long = ::currentEpochSeconds,
    ): PersistedClientSession? {
        val rawToken = storage.get(SecureStorage.KEY_ACCESS_TOKEN)?.trim() ?: return null
        if (rawToken.isEmpty()) return null

        val rawMetadataJson = storage.get(SecureStorage.KEY_SESSION_METADATA)
        val storedMetadata = rawMetadataJson?.let { jsonText -> deserializeSessionMetadata(jsonText) }

        val structuredToken = isStructuredJwtOrLocalToken(rawToken)
        if (structuredToken) {
            val claims = verifyAccessToken(rawToken, clockEpochSeconds)
            if (claims == null) {
                clearSession(storage)
                return null
            }
            val activeViewFromStorage = storage.get(SecureStorage.KEY_ACTIVE_VIEW)?.trim().orEmpty()
            val resolvedActiveView = storedMetadata?.activeView?.ifBlank { activeViewFromStorage }
                ?: activeViewFromStorage.ifBlank { "personal" }
            val resolvedMetadata = ClientSessionMetadata(
                userId = claims.subjectId,
                username = claims.username.ifEmpty { storedMetadata?.username.orEmpty() },
                email = claims.email.ifEmpty { storedMetadata?.email.orEmpty() },
                fullName = claims.fullName.ifEmpty { storedMetadata?.fullName.orEmpty() },
                role = claims.role.ifEmpty { storedMetadata?.role ?: "client" },
                birthDate = claims.birthDate.ifEmpty { storedMetadata?.birthDate.orEmpty() },
                activeView = resolvedActiveView,
                issuedAtEpochSeconds = claims.issuedAtEpochSeconds,
                expiresAtEpochSeconds = claims.expiresAtEpochSeconds,
            )
            return buildPersistedSession(rawToken, resolvedMetadata, claims)
        }

        val nowSeconds = clockEpochSeconds()
        if (storedMetadata != null) {
            val hasPositiveExpiry = storedMetadata.expiresAtEpochSeconds > 0L
            if (hasPositiveExpiry) {
                val isExpired = nowSeconds >= storedMetadata.expiresAtEpochSeconds
                if (isExpired) {
                    clearSession(storage)
                    return null
                }
            }
            return buildPersistedSession(rawToken, storedMetadata, null)
        }

        val fallbackRole = storage.get(SecureStorage.KEY_USER_ROLE)?.trim()?.ifEmpty { "client" } ?: "client"
        val fallbackView = storage.get(SecureStorage.KEY_ACTIVE_VIEW)?.trim()?.ifEmpty { "personal" } ?: "personal"
        val minimalMetadata = ClientSessionMetadata(
            userId = "authenticated-user",
            email = "",
            role = fallbackRole,
            activeView = fallbackView,
        )
        return buildPersistedSession(rawToken, minimalMetadata, null)
    }

    /**
     * Reads the stored access token only if it has not expired according to [clockEpochSeconds].
     * Automatically clears expired tokens from [SecureStorage].
     */
    public suspend fun readValidAccessToken(
        storage: SecureStorage,
        clockEpochSeconds: () -> Long = ::currentEpochSeconds,
    ): String? {
        val validSession = restoreValidSession(storage, clockEpochSeconds)
        return validSession?.accessToken
    }

    /**
     * Clears persisted authentication tokens and session metadata from [SecureStorage].
     */
    public suspend fun clearSession(storage: SecureStorage) {
        storage.remove(SecureStorage.KEY_ACCESS_TOKEN)
        storage.remove(SecureStorage.KEY_SESSION_METADATA)
        storage.remove(SecureStorage.KEY_USER_ROLE)
        storage.remove(SecureStorage.KEY_ACTIVE_VIEW)
    }

    /**
     * Serializes [ClientSessionMetadata] into compact JSON suitable for [SecureStorage].
     */
    public fun serializeSessionMetadata(metadata: ClientSessionMetadata): String = buildString {
        append("{")
        append("\"userId\":\"").append(escapeJsonString(metadata.userId.trim())).append("\",")
        append("\"username\":\"").append(escapeJsonString(metadata.username.trim())).append("\",")
        append("\"email\":\"").append(escapeJsonString(metadata.email.trim())).append("\",")
        append("\"fullName\":\"").append(escapeJsonString(metadata.fullName.trim())).append("\",")
        append("\"role\":\"").append(escapeJsonString(metadata.role.trim())).append("\",")
        append("\"birthDate\":\"").append(escapeJsonString(metadata.birthDate.trim())).append("\",")
        append("\"activeView\":\"").append(escapeJsonString(metadata.activeView.trim())).append("\",")
        append("\"iat\":").append(metadata.issuedAtEpochSeconds).append(",")
        append("\"exp\":").append(metadata.expiresAtEpochSeconds)
        append("}")
    }

    /**
     * Deserializes [ClientSessionMetadata] from JSON stored in [SecureStorage].
     */
    public fun deserializeSessionMetadata(rawJson: String): ClientSessionMetadata? {
        val trimmed = rawJson.trim()
        if (!trimmed.startsWith("{")) return null
        val userId = extractJsonStringField(trimmed, "userId") ?: return null
        if (userId.isBlank()) return null
        val username = extractJsonStringField(trimmed, "username").orEmpty()
        val email = extractJsonStringField(trimmed, "email").orEmpty()
        val fullName = extractJsonStringField(trimmed, "fullName").orEmpty()
        val role = extractJsonStringField(trimmed, "role")?.ifBlank { "client" } ?: "client"
        val birthDate = extractJsonStringField(trimmed, "birthDate").orEmpty()
        val activeView = extractJsonStringField(trimmed, "activeView")?.ifBlank { "personal" } ?: "personal"
        val issuedAt = extractJsonLongField(trimmed, "iat") ?: 0L
        val expiresAt = extractJsonLongField(trimmed, "exp") ?: 0L
        return ClientSessionMetadata(
            userId = userId,
            username = username,
            email = email,
            fullName = fullName,
            role = role,
            birthDate = birthDate,
            activeView = activeView,
            issuedAtEpochSeconds = issuedAt,
            expiresAtEpochSeconds = expiresAt,
        )
    }

    private fun isStructuredJwtOrLocalToken(token: String): Boolean {
        val dotCount = token.count { character -> character == '.' }
        if (dotCount == 2) return true
        return token.startsWith("$LOCAL_SESSION_PREFIX.")
    }

    private fun buildPersistedSession(
        accessToken: String,
        metadata: ClientSessionMetadata,
        claims: JwtClaims?,
    ): PersistedClientSession = PersistedClientSession(
        accessToken = accessToken,
        userId = metadata.userId,
        username = metadata.username,
        email = metadata.email,
        fullName = metadata.fullName,
        role = metadata.role,
        birthDate = metadata.birthDate,
        activeView = metadata.activeView,
        issuedAtEpochSeconds = metadata.issuedAtEpochSeconds,
        expiresAtEpochSeconds = metadata.expiresAtEpochSeconds,
        claims = claims,
    )

    private fun buildClaimsPayloadJson(
        account: RegisteredUserAccount,
        issuedAtEpochSeconds: Long,
        expiresAtEpochSeconds: Long,
    ): String = buildString {
        append("{")
        append("\"sub\":\"").append(escapeJsonString(account.id)).append("\",")
        append("\"username\":\"").append(escapeJsonString(account.username)).append("\",")
        append("\"email\":\"").append(escapeJsonString(account.email)).append("\",")
        append("\"name\":\"").append(escapeJsonString(account.fullName)).append("\",")
        append("\"role\":\"").append(escapeJsonString(account.role)).append("\",")
        append("\"birthdate\":\"").append(escapeJsonString(account.birthDate)).append("\",")
        append("\"iat\":").append(issuedAtEpochSeconds).append(",")
        append("\"exp\":").append(expiresAtEpochSeconds)
        append("}")
    }

    private fun decodeThreePartJwtClaims(
        parts: List<String>,
        clockEpochSeconds: () -> Long,
    ): JwtClaims? {
        val encodedHeader = parts[0]
        val encodedPayload = parts[1]
        val encodedSignature = parts[2]
        if (encodedHeader.isEmpty()) return null
        if (encodedPayload.isEmpty()) return null
        if (encodedSignature.isEmpty()) return null

        val headerBytes = base64UrlDecode(encodedHeader) ?: return null
        val signatureBytes = base64UrlDecode(encodedSignature) ?: return null
        if (signatureBytes.size < 16) return null

        val headerJson = runCatching { headerBytes.decodeToString() }.getOrNull()?.trim() ?: return null
        if (!headerJson.startsWith("{")) return null
        val algorithm = extractJsonStringField(headerJson, "alg")?.trim()?.uppercase() ?: return null
        if (algorithm.isEmpty()) return null
        if (algorithm == "NONE") return null

        return decodePayloadClaimsAndCheckExpiry(encodedPayload, clockEpochSeconds)
    }

    private fun decodePayloadClaimsAndCheckExpiry(
        encodedPayload: String,
        clockEpochSeconds: () -> Long,
    ): JwtClaims? {
        val payloadBytes = base64UrlDecode(encodedPayload) ?: return null
        val payloadJson = runCatching { payloadBytes.decodeToString() }.getOrNull()?.trim() ?: return null
        if (!payloadJson.startsWith("{")) return null

        val subjectId = extractJsonStringField(payloadJson, "sub")
            ?: extractJsonStringField(payloadJson, "user_id")
            ?: return null
        if (subjectId.isBlank()) return null

        val expiresAt = extractJsonLongField(payloadJson, "exp") ?: return null
        if (expiresAt <= 0L) return null

        val nowSeconds = clockEpochSeconds()
        val isExpired = nowSeconds >= expiresAt
        if (isExpired) return null

        val username = extractJsonStringField(payloadJson, "username").orEmpty()
        val email = extractJsonStringField(payloadJson, "email").orEmpty()
        val fullName = extractJsonStringField(payloadJson, "name")
            ?: extractJsonStringField(payloadJson, "full_name")
            ?: ""
        val role = extractJsonStringField(payloadJson, "role")?.ifBlank { "client" } ?: "client"
        val birthDate = extractJsonStringField(payloadJson, "birthdate").orEmpty()
        val issuedAt = extractJsonLongField(payloadJson, "iat") ?: 0L

        return JwtClaims(
            subjectId = subjectId,
            username = username,
            email = email,
            fullName = fullName,
            role = role,
            birthDate = birthDate,
            issuedAtEpochSeconds = issuedAt,
            expiresAtEpochSeconds = expiresAt,
        )
    }

    /**
     * Computes HMAC-SHA256 according to RFC 2104.
     */
    public fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
        val blockSize = 64
        val normalizedKey = if (key.size > blockSize) {
            sha256(key)
        } else {
            key
        }
        val paddedKey = ByteArray(blockSize)
        normalizedKey.copyInto(paddedKey, 0, 0, normalizedKey.size)

        val innerPad = ByteArray(blockSize) { index -> (paddedKey[index].toInt() xor 0x36).toByte() }
        val outerPad = ByteArray(blockSize) { index -> (paddedKey[index].toInt() xor 0x5c).toByte() }

        val innerInput = ByteArray(innerPad.size + message.size)
        innerPad.copyInto(innerInput, 0)
        message.copyInto(innerInput, innerPad.size)
        val innerHash = sha256(innerInput)

        val outerInput = ByteArray(outerPad.size + innerHash.size)
        outerPad.copyInto(outerInput, 0)
        innerHash.copyInto(outerInput, outerPad.size)
        return sha256(outerInput)
    }

    /**
     * Computes SHA-256 digest according to FIPS 180-4.
     */
    public fun sha256(input: ByteArray): ByteArray {
        var h0 = 0x6a09e667
        var h1 = -0x4498517b
        var h2 = 0x3c6ef372
        var h3 = -0x5ab00ac6
        var h4 = 0x510e527f
        var h5 = -0x64fa9774
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19

        val bitLength = input.size.toLong() * 8L
        val remainder = (input.size + 1) % 64
        val zeroPaddingBytes = if (remainder <= 56) {
            56 - remainder
        } else {
            120 - remainder
        }
        val paddedSize = input.size + 1 + zeroPaddingBytes + 8
        val padded = ByteArray(paddedSize)
        input.copyInto(padded, 0)
        padded[input.size] = 0x80.toByte()
        for (byteIndex in 0 until 8) {
            val shiftBits = (7 - byteIndex) * 8
            padded[paddedSize - 8 + byteIndex] = ((bitLength ushr shiftBits) and 0xFFL).toByte()
        }

        val words = IntArray(64)
        for (chunkOffset in 0 until paddedSize step 64) {
            for (wordIndex in 0 until 16) {
                val base = chunkOffset + wordIndex * 4
                val b0 = (padded[base].toInt() and 0xFF) shl 24
                val b1 = (padded[base + 1].toInt() and 0xFF) shl 16
                val b2 = (padded[base + 2].toInt() and 0xFF) shl 8
                val b3 = padded[base + 3].toInt() and 0xFF
                words[wordIndex] = b0 or b1 or b2 or b3
            }
            for (wordIndex in 16 until 64) {
                val w15 = words[wordIndex - 15]
                val s0 = w15.rotateRight(7) xor w15.rotateRight(18) xor (w15 ushr 3)
                val w2 = words[wordIndex - 2]
                val s1 = w2.rotateRight(17) xor w2.rotateRight(19) xor (w2 ushr 10)
                words[wordIndex] = words[wordIndex - 16] + s0 + words[wordIndex - 7] + s1
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            var f = h5
            var g = h6
            var h = h7

            for (roundIndex in 0 until 64) {
                val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + sha256RoundConstants[roundIndex] + words[roundIndex]
                val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = s0 + maj

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            h5 += f
            h6 += g
            h7 += h
        }

        val digest = ByteArray(32)
        val finalState = intArrayOf(h0, h1, h2, h3, h4, h5, h6, h7)
        for (stateIndex in 0 until 8) {
            val word = finalState[stateIndex]
            val base = stateIndex * 4
            digest[base] = (word ushr 24).toByte()
            digest[base + 1] = (word ushr 16).toByte()
            digest[base + 2] = (word ushr 8).toByte()
            digest[base + 3] = word.toByte()
        }
        return digest
    }

    /**
     * Base64URL encodes bytes without padding according to RFC 4648 Section 5.
     */
    public fun base64UrlEncode(bytes: ByteArray): String {
        val output = StringBuilder((bytes.size * 4 + 2) / 3)
        for (index in bytes.indices step 3) {
            val byte0 = bytes[index].toInt() and 0xFF
            val hasByte1 = index + 1 < bytes.size
            val hasByte2 = index + 2 < bytes.size
            val byte1 = if (hasByte1) bytes[index + 1].toInt() and 0xFF else 0
            val byte2 = if (hasByte2) bytes[index + 2].toInt() and 0xFF else 0

            val triple = (byte0 shl 16) or (byte1 shl 8) or byte2
            output.append(BASE64_URL_ALPHABET[(triple ushr 18) and 0x3F])
            output.append(BASE64_URL_ALPHABET[(triple ushr 12) and 0x3F])
            if (hasByte1) {
                output.append(BASE64_URL_ALPHABET[(triple ushr 6) and 0x3F])
            }
            if (hasByte2) {
                output.append(BASE64_URL_ALPHABET[triple and 0x3F])
            }
        }
        return output.toString()
    }

    /**
     * Base64URL decodes an unpadded or padded string into raw bytes.
     */
    public fun base64UrlDecode(encoded: String): ByteArray? {
        val clean = encoded.trim().trimEnd('=')
        if (clean.isEmpty()) return ByteArray(0)
        if (clean.length % 4 == 1) return null
        val outputSize = (clean.length * 3) / 4
        val result = ByteArray(outputSize)
        var outIndex = 0
        for (charIndex in clean.indices step 4) {
            val c0 = BASE64_URL_ALPHABET.indexOf(clean[charIndex])
            val has1 = charIndex + 1 < clean.length
            if (!has1) return null
            val c1 = BASE64_URL_ALPHABET.indexOf(clean[charIndex + 1])
            if (c0 < 0) return null
            if (c1 < 0) return null

            val has2 = charIndex + 2 < clean.length
            val c2 = if (has2) BASE64_URL_ALPHABET.indexOf(clean[charIndex + 2]) else 0
            val has3 = charIndex + 3 < clean.length
            val c3 = if (has3) BASE64_URL_ALPHABET.indexOf(clean[charIndex + 3]) else 0
            if (c2 < 0) return null
            if (c3 < 0) return null

            val triple = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3
            if (outIndex < outputSize) {
                result[outIndex++] = ((triple ushr 16) and 0xFF).toByte()
            }
            if (has2) {
                if (outIndex < outputSize) {
                    result[outIndex++] = ((triple ushr 8) and 0xFF).toByte()
                }
            }
            if (has3) {
                if (outIndex < outputSize) {
                    result[outIndex++] = (triple and 0xFF).toByte()
                }
            }
        }
        return result
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val builder = StringBuilder(bytes.size * 2)
        for (byteValue in bytes) {
            val unsigned = byteValue.toInt() and 0xFF
            builder.append(hexChars[unsigned ushr 4])
            builder.append(hexChars[unsigned and 0x0F])
        }
        return builder.toString()
    }

    private fun constantTimeEquals(first: String, second: String): Boolean {
        if (first.length != second.length) {
            return false
        }
        var diff = 0
        for (index in first.indices) {
            diff = diff or (first[index].code xor second[index].code)
        }
        return diff == 0
    }

    private fun escapeJsonString(raw: String): String = raw.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun extractJsonStringField(json: String, fieldName: String): String? {
        val pattern = Regex(""""$fieldName"\s*:\s*"((?:\\.|[^"\\])*)"""")
        val match = pattern.find(json) ?: return null
        return match.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
    }

    private fun extractJsonLongField(json: String, fieldName: String): Long? {
        val pattern = Regex(""""$fieldName"\s*:\s*(-?\d+)""")
        val match = pattern.find(json) ?: return null
        return match.groupValues[1].toLongOrNull()
    }
}
