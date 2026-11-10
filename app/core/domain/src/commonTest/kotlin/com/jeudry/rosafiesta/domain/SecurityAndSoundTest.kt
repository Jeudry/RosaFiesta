package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.domain.security.ClientSessionMetadata
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.WavPcmEncoder
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecurityAndSoundTest {
    @Test
    fun `verifica que SensitiveDataRedactor censure correos telefonos tokens y referencias bancarias`() {
        val sampleJwt = buildSampleServerJwt(
            subjectId = "usr-camila",
            email = "camila@rosafiesta.do",
            role = "client",
            issuedAtEpochSeconds = 1_800_000_000L,
            expiresAtEpochSeconds = 1_800_086_400L,
        )
        val raw = "Error login camila@rosafiesta.do tel 809-555-0142 Authorization: Bearer rf-token-camila " +
            "jwt $sampleJwt password=MySecret123 ref BHD-2026-8841"
        val sanitized = SensitiveDataRedactor.redactText(raw)

        assertFalse(sanitized.contains("camila@rosafiesta.do"))
        assertFalse(sanitized.contains("809-555-0142"))
        assertFalse(sanitized.contains("rf-token-camila"))
        assertFalse(sanitized.contains(sampleJwt))
        assertFalse(sanitized.contains("MySecret123"))
        assertFalse(sanitized.contains("BHD-2026-8841"))

        assertTrue(sanitized.contains("c***a@rosafiesta.do"))
        assertTrue(sanitized.contains("809-***-**42"))
        assertTrue(sanitized.contains("Bearer [REDACTED]"))
        assertTrue(sanitized.contains("[JWT_REDACTED]"))
        assertTrue(sanitized.contains("password=[REDACTED]"))
        assertTrue(sanitized.contains("BHD-***-41"))
    }

    @Test
    fun `verifica que translateBusinessError traduzca por codigo de negocio y censure el fallback`() {
        val translated = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", "raw value")
        assertTrue(translated.contains("Correo o contraseña incorrectos"))

        val fallback = SensitiveDataRedactor.translateBusinessError("UNKNOWN_NEW_CODE", "Fallo para camila@rosafiesta.do")
        assertFalse(fallback.contains("camila@rosafiesta.do"))
        assertTrue(fallback.contains("c***a@rosafiesta.do"))
    }

    @Test
    fun `verifica que PcmSoundSynthesizer y WavPcmEncoder generen cabeceras RIFF WAVE validas y RMS real`() {
        val wavBytes = PcmSoundSynthesizer.synthesizeWav(SoundEffect.PAYMENT_SUCCESS, volume = 0.8f)
        assertTrue(wavBytes.size > 44)
        val riff = wavBytes.decodeToString(0, 4)
        val wave = wavBytes.decodeToString(8, 12)
        assertEquals("RIFF", riff)
        assertEquals("WAVE", wave)

        val pcmPayload = wavBytes.copyOfRange(44, wavBytes.size)
        val encodedCapture = WavPcmEncoder.encodeMono16BitPcmToWav(pcmPayload, sampleRateHz = 16_000)
        assertEquals("RIFF", encodedCapture.decodeToString(0, 4))
        assertEquals("WAVE", encodedCapture.decodeToString(8, 12))
        assertEquals(44 + pcmPayload.size, encodedCapture.size)

        val activeRms = WavPcmEncoder.computeNormalizedRms(pcmPayload)
        assertTrue(activeRms > 0.01f)
        val silenceRms = WavPcmEncoder.computeNormalizedRms(ByteArray(320))
        assertEquals(0f, silenceRms)

        val player = NoOpSoundPlayer()
        player.playPaymentSuccess()
        assertEquals(SoundEffect.PAYMENT_SUCCESS, player.lastPlayedEffect)
    }

    @Test
    fun `verifica almacenamiento seguro rechazo por expiracion exp y limpieza en cierre de sesion`() = runTest {
        val storage = InMemorySecureStorage()
        var fakeClockSeconds = 1_800_000_000L

        val serverJwt = buildSampleServerJwt(
            subjectId = "usr-maria",
            email = "maria@rosafiesta.do",
            role = "admin",
            issuedAtEpochSeconds = fakeClockSeconds,
            expiresAtEpochSeconds = fakeClockSeconds + 3_600L,
        )

        val savedSession = JwtAuthManager.persistSession(
            storage = storage,
            accessToken = serverJwt,
            metadata = ClientSessionMetadata(
                userId = "usr-maria",
                username = "maria",
                email = "maria@rosafiesta.do",
                fullName = "María",
                role = "admin",
                activeView = "admin",
                issuedAtEpochSeconds = fakeClockSeconds,
                expiresAtEpochSeconds = fakeClockSeconds + 3_600L,
            ),
            clockEpochSeconds = { fakeClockSeconds },
        )
        assertNotNull(savedSession)
        assertEquals("usr-maria", savedSession.userId)
        assertEquals("admin", savedSession.role)
        assertEquals("admin", savedSession.activeView)

        val activeToken = JwtAuthManager.readValidAccessToken(storage) { fakeClockSeconds + 1_800L }
        assertEquals(serverJwt, activeToken)

        fakeClockSeconds += 3_600L
        val expiredAtBoundary = JwtAuthManager.restoreValidSession(storage) { fakeClockSeconds }
        assertNull(expiredAtBoundary)
        assertNull(storage.get(SecureStorage.KEY_ACCESS_TOKEN))
        assertNull(storage.get(SecureStorage.KEY_SESSION_METADATA))

        val opaqueToken = "srv_live_opaque_access_998123"
        val opaqueSaved = JwtAuthManager.persistSession(
            storage = storage,
            accessToken = opaqueToken,
            metadata = ClientSessionMetadata(
                userId = "usr-camila",
                username = "camila",
                email = "camila@rosafiesta.do",
                fullName = "Camila Rodríguez",
                role = "client",
                activeView = "personal",
                issuedAtEpochSeconds = fakeClockSeconds,
                expiresAtEpochSeconds = fakeClockSeconds + 7_200L,
            ),
            clockEpochSeconds = { fakeClockSeconds },
        )
        assertNotNull(opaqueSaved)
        assertEquals(opaqueToken, JwtAuthManager.readValidAccessToken(storage) { fakeClockSeconds + 60L })

        JwtAuthManager.clearSession(storage)
        assertNull(JwtAuthManager.readValidAccessToken(storage) { fakeClockSeconds + 60L })
        assertNull(storage.get(SecureStorage.KEY_ACCESS_TOKEN))
        assertNull(storage.get(SecureStorage.KEY_USER_ROLE))
    }

    @Test
    fun `verifica que el cliente nunca falsifique JWT HS256 del servidor y valide exp estrictamente`() {
        val hash = JwtAuthManager.hashPassword("RosaFiesta2026", "usr-test-salt")
        assertTrue(JwtAuthManager.verifyPassword("RosaFiesta2026", "usr-test-salt", hash))
        assertFalse(JwtAuthManager.verifyPassword("WrongPass2026", "usr-test-salt", hash))

        val account = JwtAuthManager.defaultSeedAccounts.first()
        val nowSeconds = 1_800_100_000L
        val localEnvelope = JwtAuthManager.issueAccessToken(
            account = account,
            issuedAtEpochSeconds = nowSeconds,
            ttlSeconds = 600L,
        )
        assertTrue(localEnvelope.startsWith("rf-local."))
        assertEquals(2, localEnvelope.split('.').size)

        val validLocalClaims = JwtAuthManager.verifyAccessToken(localEnvelope) { nowSeconds + 300L }
        assertNotNull(validLocalClaims)
        assertEquals(account.id, validLocalClaims.subjectId)
        assertEquals(account.role, validLocalClaims.role)

        val expiredLocalClaims = JwtAuthManager.verifyAccessToken(localEnvelope) { nowSeconds + 600L }
        assertNull(expiredLocalClaims)

        val noneAlgHeader = JwtAuthManager.base64UrlEncode("""{"alg":"none","typ":"JWT"}""".encodeToByteArray())
        val payloadPart = JwtAuthManager.base64UrlEncode(
            """{"sub":"usr-camila","email":"camila@rosafiesta.do","exp":1999999999}""".encodeToByteArray(),
        )
        val fakeSigPart = JwtAuthManager.base64UrlEncode(ByteArray(32) { index -> index.toByte() })
        assertNull(JwtAuthManager.verifyAccessToken("$noneAlgHeader.$payloadPart.$fakeSigPart") { nowSeconds })
    }

    @Test
    fun `verifica que InputValidator valide formato longitud minima y maxima en correo nombre telefono fecha y usuario`() {
        assertNotNull(InputValidator.validateEmail("jeudry"))
        assertNotNull(InputValidator.validateEmail("a@b"))
        assertNotNull(InputValidator.validateEmail("usuario@dominio"))
        assertNull(InputValidator.validateEmail("yuli@rosafiesta.do"))

        assertNotNull(InputValidator.validateFullName("J"))
        assertNotNull(InputValidator.validateFullName("Jeudry123"))
        assertNull(InputValidator.validateFullName("Jeudry Rosario"))

        assertNotNull(InputValidator.validateUsername("ab"))
        assertNotNull(InputValidator.validateUsername("usuario con espacios"))
        assertNull(InputValidator.validateUsername("yuli_rf.96"))

        assertNotNull(InputValidator.validatePhone("809-555"))
        assertNotNull(InputValidator.validatePhone("809-ABC-0102"))
        assertNull(InputValidator.validatePhone("809-555-0102"))

        assertNotNull(InputValidator.validateBirthDate("1996-09-22"))
        assertNotNull(InputValidator.validateBirthDate("31/02/1996"))
        assertNotNull(InputValidator.validateBirthDate("22/09/2023"))
        assertNull(InputValidator.validateBirthDate("22/09/1996"))

        assertNotNull(InputValidator.validatePassword("12345"))
        assertNull(InputValidator.validatePassword("rosa2026"))
    }

    private fun buildSampleServerJwt(
        subjectId: String,
        email: String,
        role: String,
        issuedAtEpochSeconds: Long,
        expiresAtEpochSeconds: Long,
    ): String {
        val header = JwtAuthManager.base64UrlEncode("""{"alg":"HS256","typ":"JWT"}""".encodeToByteArray())
        val payload = JwtAuthManager.base64UrlEncode(
            """{"sub":"$subjectId","username":"$subjectId","email":"$email","role":"$role","iat":$issuedAtEpochSeconds,"exp":$expiresAtEpochSeconds}"""
                .encodeToByteArray(),
        )
        val testServerSignature = JwtAuthManager.hmacSha256(
            key = ByteArray(32) { byteIndex -> (byteIndex + 7).toByte() },
            message = "$header.$payload".encodeToByteArray(),
        )
        val signature = JwtAuthManager.base64UrlEncode(testServerSignature)
        return "$header.$payload.$signature"
    }
}
