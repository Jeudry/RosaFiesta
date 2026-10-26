package com.jeudry.rosafiesta.domain.security

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
)

/**
 * Verified claims extracted from a signed HS256 JWT session token.
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
 * Result of an authentication, registration, or password reset operation.
 */
public data class AuthOutcome(
    public val success: Boolean,
    public val errorCode: String? = null,
    public val errorMessage: String? = null,
    public val accessToken: String? = null,
    public val account: RegisteredUserAccount? = null,
) {
    public companion object {
        /**
         * Builds a successful authentication outcome with a signed JWT and account.
         */
        public fun authenticated(
            accessToken: String,
            account: RegisteredUserAccount,
        ): AuthOutcome = AuthOutcome(
            success = true,
            accessToken = accessToken,
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
 * Multiplatform cryptographic manager for salted SHA-256 password hashing and RFC 7519 HS256 JWT tokens.
 */
public object JwtAuthManager {
    private const val JWT_SIGNING_SECRET: String = "rf-2026-jwt-hs256-signing-secret-key-do-santo-domingo"
    private const val PASSWORD_HASH_ROUNDS: Int = 64
    private const val DEFAULT_TOKEN_TTL_SECONDS: Long = 86_400L * 7L
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
     * Canonical seeded accounts with pre-hashed credentials (`rosa2026`).
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
        val digest = sha256("rf-salt-v1:$normalized".encodeToByteArray())
        return bytesToHex(digest).take(24)
    }

    /**
     * Derives a multi-round salted HMAC-SHA256 password hash.
     */
    public fun hashPassword(rawPassword: String, salt: String): String {
        val cleanSalt = salt.trim().ifEmpty { "rf-default-salt" }
        val keyBytes = "rf-pwd-pepper:$cleanSalt".encodeToByteArray()
        var currentBlock = "$cleanSalt:${rawPassword.trim()}".encodeToByteArray()
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
        return constantTimeEquals(computed, expectedHash.trim())
    }

    /**
     * Issues a signed RFC 7519 HS256 JWT token for the authenticated user account.
     */
    public fun issueAccessToken(
        account: RegisteredUserAccount,
        issuedAtEpochSeconds: Long = 1_793_052_000L,
    ): String {
        val expiresAt = issuedAtEpochSeconds + DEFAULT_TOKEN_TTL_SECONDS
        val headerJson = """{"alg":"HS256","typ":"JWT"}"""
        val payloadJson = buildString {
            append("{")
            append("\"sub\":\"").append(escapeJsonString(account.id)).append("\",")
            append("\"username\":\"").append(escapeJsonString(account.username)).append("\",")
            append("\"email\":\"").append(escapeJsonString(account.email)).append("\",")
            append("\"name\":\"").append(escapeJsonString(account.fullName)).append("\",")
            append("\"role\":\"").append(escapeJsonString(account.role)).append("\",")
            append("\"birthdate\":\"").append(escapeJsonString(account.birthDate)).append("\",")
            append("\"iat\":").append(issuedAtEpochSeconds).append(",")
            append("\"exp\":").append(expiresAt)
            append("}")
        }
        val encodedHeader = base64UrlEncode(headerJson.encodeToByteArray())
        val encodedPayload = base64UrlEncode(payloadJson.encodeToByteArray())
        val signingInput = "$encodedHeader.$encodedPayload"
        val signatureBytes = hmacSha256(
            key = JWT_SIGNING_SECRET.encodeToByteArray(),
            message = signingInput.encodeToByteArray(),
        )
        val encodedSignature = base64UrlEncode(signatureBytes)
        return "$signingInput.$encodedSignature"
    }

    /**
     * Verifies the HS256 signature and structure of a JWT token and extracts its claims.
     */
    public fun verifyAccessToken(token: String): JwtClaims? {
        val parts = token.trim().split(".")
        if (parts.size != 3) {
            return null
        }
        val encodedHeader = parts[0]
        val encodedPayload = parts[1]
        val encodedSignature = parts[2]
        val signingInput = "$encodedHeader.$encodedPayload"
        val expectedSigBytes = hmacSha256(
            key = JWT_SIGNING_SECRET.encodeToByteArray(),
            message = signingInput.encodeToByteArray(),
        )
        val expectedSignature = base64UrlEncode(expectedSigBytes)
        val signatureValid = constantTimeEquals(encodedSignature, expectedSignature)
        if (!signatureValid) {
            return null
        }
        val payloadBytes = base64UrlDecode(encodedPayload) ?: return null
        val payloadJson = payloadBytes.decodeToString()
        val subjectId = extractJsonStringField(payloadJson, "sub") ?: return null
        val username = extractJsonStringField(payloadJson, "username") ?: ""
        val email = extractJsonStringField(payloadJson, "email") ?: return null
        val fullName = extractJsonStringField(payloadJson, "name") ?: ""
        val role = extractJsonStringField(payloadJson, "role") ?: "client"
        val birthDate = extractJsonStringField(payloadJson, "birthdate") ?: ""
        val issuedAt = extractJsonLongField(payloadJson, "iat") ?: 0L
        val expiresAt = extractJsonLongField(payloadJson, "exp") ?: 0L
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
        var chunkOffset = 0
        while (chunkOffset < paddedSize) {
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

            chunkOffset += 64
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
        var index = 0
        while (index < bytes.size) {
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
            index += 3
        }
        return output.toString()
    }

    /**
     * Base64URL decodes an unpadded or padded string into raw bytes.
     */
    public fun base64UrlDecode(encoded: String): ByteArray? {
        val clean = encoded.trim().trimEnd('=')
        if (clean.isEmpty()) return ByteArray(0)
        val outputSize = (clean.length * 3) / 4
        val result = ByteArray(outputSize)
        var outIndex = 0
        var charIndex = 0
        while (charIndex < clean.length) {
            val c0 = BASE64_URL_ALPHABET.indexOf(clean[charIndex])
            val has1 = charIndex + 1 < clean.length
            if (!has1) return null
            val c1 = BASE64_URL_ALPHABET.indexOf(clean[charIndex + 1])
            val isInvalidPair = c0 < 0 || c1 < 0
            if (isInvalidPair) return null

            val has2 = charIndex + 2 < clean.length
            val c2 = if (has2) BASE64_URL_ALPHABET.indexOf(clean[charIndex + 2]) else 0
            val has3 = charIndex + 3 < clean.length
            val c3 = if (has3) BASE64_URL_ALPHABET.indexOf(clean[charIndex + 3]) else 0
            val isInvalidTail = c2 < 0 || c3 < 0
            if (isInvalidTail) return null

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
            charIndex += 4
        }
        return result
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val builder = StringBuilder(bytes.size * 2)
        for (byteVal in bytes) {
            val unsigned = byteVal.toInt() and 0xFF
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
        val pattern = Regex(""""$fieldName"\s*:\s*(\d+)""")
        val match = pattern.find(json) ?: return null
        return match.groupValues[1].toLongOrNull()
    }
}
