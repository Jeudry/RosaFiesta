package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.domain.security.ClientSessionMetadata
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
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

        val rosaReplyWav = PcmSoundSynthesizer.synthesizeWav(SoundEffect.ROSA_REPLY, volume = 0.8f)
        assertTrue(rosaReplyWav.size > 44)
        assertEquals("RIFF", rosaReplyWav.decodeToString(0, 4))
        assertEquals("WAVE", rosaReplyWav.decodeToString(8, 12))
        val rosaReplyPcm = rosaReplyWav.copyOfRange(44, rosaReplyWav.size)
        assertTrue(WavPcmEncoder.computeNormalizedRms(rosaReplyPcm) > 0.01f)
        player.playRosaReply()
        assertEquals(SoundEffect.ROSA_REPLY, player.lastPlayedEffect)

        val customAudio = AppAudioConfig.DEFAULT
            .withVolumePercent(40)
            .copy(rosaAssistantSoundsEnabled = false)
        assertEquals(40, customAudio.volumePercent())
        assertTrue(customAudio.isModifiedFromDefault())
        player.soundVolume = customAudio.normalizedVolume
        player.enabledCategories = customAudio.activeCategories()
        player.playRosaSend()
        assertEquals(SoundEffect.ROSA_REPLY, player.lastPlayedEffect)
        player.playTap()
        assertEquals(SoundEffect.TAP_SOFT, player.lastPlayedEffect)

        player.soundVolume = 0f
        player.playPaymentSuccess()
        assertEquals(SoundEffect.TAP_SOFT, player.lastPlayedEffect)
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
        assertNotNull(InputValidator.validateBirthDate("22/09/1880"))
        assertNull(InputValidator.validateBirthDate("22/09/1996"))

        assertNotNull(InputValidator.validatePassword("12345"))
        assertNull(InputValidator.validatePassword("rosa2026"))
    }

    @Test
    fun `verifica mascaras formatos y validaciones extendidas de InputValidator`() {
        assertEquals("", InputValidator.filterPhoneInput("dfasfasfadsfsa"))
        assertEquals("(809) 555-0142", InputValidator.filterPhoneInput("8095550142"))
        assertEquals("+1 (809) 555-0142", InputValidator.filterPhoneInput("18095550142"))
        assertEquals("(809) 555", InputValidator.filterPhoneInput("809abc555"))

        val stepAfterArea = InputValidator.filterPhoneInputWithCursor("849", "8497", 4)
        assertEquals("(849) 7", stepAfterArea.text)
        assertEquals(7, stepAfterArea.cursorIndex)

        val stepAfterFive = InputValidator.filterPhoneInputWithCursor("(849) 7", "(849) 75", 8)
        assertEquals("(849) 75", stepAfterFive.text)
        assertEquals(8, stepAfterFive.cursorIndex)

        val stepAfterZero = InputValidator.filterPhoneInputWithCursor("(849) 75", "(849) 750", 9)
        assertEquals("(849) 750", stepAfterZero.text)
        assertEquals(9, stepAfterZero.cursorIndex)

        val stepMiddleInsert = InputValidator.filterPhoneInputWithCursor("(849) 750", "(849) 7250", 8)
        assertEquals("(849) 725-0", stepMiddleInsert.text)
        assertEquals(8, stepMiddleInsert.cursorIndex)

        val stepBackspaceMask = InputValidator.filterPhoneInputWithCursor("(849) 750", "(849)750", 5)
        assertEquals("(847) 50", stepBackspaceMask.text)
        assertEquals(3, stepBackspaceMask.cursorIndex)

        assertEquals("MD", InputValidator.extractInitials("María (Dueña)"))
        assertEquals("M", InputValidator.extractInitials("María (Dueña)", fallback = "C", maxInitials = 1))
        assertEquals("D", InputValidator.extractInitials("(Dueña) María", fallback = "C", maxInitials = 1))
        assertEquals("JR", InputValidator.extractInitials("Jeudry Rojas"))
        assertEquals("RF", InputValidator.extractInitials("()"))

        val expiryStep = InputValidator.filterCardExpiryInputWithCursor("12", "122", 3)
        assertEquals("12/2", expiryStep.text)
        assertEquals(4, expiryStep.cursorIndex)

        val timeStep = InputValidator.filterTimeInputWithCursor("16", "163", 3)
        assertEquals("16:3", timeStep.text)
        assertEquals(4, timeStep.cursorIndex)

        val digitsStep = InputValidator.filterDigitsOnlyWithCursor("1234", "12a34", 3, 6)
        assertEquals("1234", digitsStep.text)
        assertEquals(2, digitsStep.cursorIndex)

        assertNotNull(InputValidator.validateDominicanPhone("dfasfasfadsfsa"))
        assertNotNull(InputValidator.validateDominicanPhone("", isRequired = true))
        assertNull(InputValidator.validateDominicanPhone("", isRequired = false))
        assertNotNull(InputValidator.validateDominicanPhone("809-555"))
        assertNotNull(InputValidator.validateDominicanPhone("305-555-0102", allowInternational = false))
        assertNull(InputValidator.validateDominicanPhone("305-555-0102", allowInternational = true))
        assertNull(InputValidator.validateDominicanPhone("(809) 555-0102"))
        assertNull(InputValidator.validateDominicanPhone("(829) 555-0199"))
        assertNull(InputValidator.validateDominicanPhone("+1 (849) 555-0123"))

        assertEquals("1-31-84920-2", InputValidator.filterRncInput("131849202"))
        assertEquals("001-1234567-8", InputValidator.filterRncInput("00112345678"))
        assertEquals("1-31-84920", InputValidator.filterRncInput("131-abc-84920"))

        assertNull(InputValidator.validateRnc("", isRequired = false))
        assertNotNull(InputValidator.validateRnc("", isRequired = true))
        assertNull(InputValidator.validateRnc("1-31-84920-2", isRequired = true))
        assertNull(InputValidator.validateRnc("001-1234567-8", isRequired = true))
        assertNotNull(InputValidator.validateRnc("000000000", isRequired = true))
        assertNotNull(InputValidator.validateRnc("12345", isRequired = true))
        assertNotNull(InputValidator.validateRnc("1-31-ABCDE-2", isRequired = true))

        assertNull(InputValidator.validateEventDate("23/12/2026"))
        assertNull(InputValidator.validateEventDate("23/12/2026 - 25/12/2026"))
        assertNull(InputValidator.validateEventDate("29/02/2028"))
        assertNotNull(InputValidator.validateEventDate("01/01/2025"))
        assertNotNull(InputValidator.validateEventDate("29/02/2026"))
        assertNotNull(InputValidator.validateEventDate("32/01/2026"))
        assertNotNull(InputValidator.validateEventDate("15/13/2026"))
        assertNotNull(InputValidator.validateEventDate("23/10/2020"))
        assertNotNull(InputValidator.validateEventDate("23/10/2040"))
        assertNotNull(InputValidator.validateEventDate("2026-10-23"))
        assertNotNull(InputValidator.validatePaymentDate("23/12/2030"))
        assertNull(InputValidator.validatePaymentDate("10/11/2026"))

        assertEquals("16:30", InputValidator.filterTimeInput("1630"))
        assertEquals("09:5", InputValidator.filterTimeInput("09ab5"))
        assertEquals("08", InputValidator.filterTimeInput("08"))

        assertNull(InputValidator.validateTime24("00:00"))
        assertNull(InputValidator.validateTime24("16:30"))
        assertNull(InputValidator.validateTime24("23:59"))
        assertNotNull(InputValidator.validateTime24("24:00"))
        assertNotNull(InputValidator.validateTime24("12:60"))
        assertNotNull(InputValidator.validateTime24("9:30"))
        assertNotNull(InputValidator.validateTime24("ab:cd"))
        assertNotNull(InputValidator.validateTime24("", isRequired = true))
        assertNull(InputValidator.validateTime24("", isRequired = false))

        assertNull(InputValidator.validateCardNumber("4242 4242 4242 4242"))
        assertNull(InputValidator.validateCardNumber("4532 0151 1283 0366"))
        assertNotNull(InputValidator.validateCardNumber("4242 4242 4242 4241"))
        assertNotNull(InputValidator.validateCardNumber("4242"))
        assertNotNull(InputValidator.validateCardNumber("4242 4242 4242 4242 4242"))
        assertNotNull(InputValidator.validateCardNumber("4242 ABCD 4242 4242"))

        assertNull(InputValidator.validateCardCvv("123"))
        assertNull(InputValidator.validateCardCvv("1234"))
        assertNotNull(InputValidator.validateCardCvv("12"))
        assertNotNull(InputValidator.validateCardCvv("12345"))
        assertNotNull(InputValidator.validateCardCvv("12a"))
        assertNotNull(InputValidator.validateCardCvv("", isRequired = true))

        assertNull(InputValidator.validateMoneyAmount("1500"))
        assertNull(InputValidator.validateMoneyAmount("RD$ 12,500.50"))
        assertNotNull(InputValidator.validateMoneyAmount("abc"))
        assertNotNull(InputValidator.validateMoneyAmount("0"))
        assertNotNull(InputValidator.validateMoneyAmount("-50"))
        assertNotNull(InputValidator.validateMoneyAmount("10000001"))

        assertNull(InputValidator.validateGuestCount("1"))
        assertNull(InputValidator.validateGuestCount("150"))
        assertNull(InputValidator.validateGuestCount("${InputValidator.MAX_GUEST_COUNT}"))
        assertNotNull(InputValidator.validateGuestCount("0"))
        assertNotNull(InputValidator.validateGuestCount("-5"))
        assertNotNull(InputValidator.validateGuestCount("${InputValidator.MAX_GUEST_COUNT + 1}"))
        assertNotNull(InputValidator.validateGuestCount("abc"))

        assertEquals("(809) 555-0100", InputValidator.formatPhoneDisplay("8095550100"))
        assertEquals("+1 (809) 555-0100", InputValidator.formatPhoneDisplay("+18095550100"))
        assertEquals("1-31-84920-2", InputValidator.formatRncDisplay("131849202"))
        assertEquals("001-1234567-8", InputValidator.formatRncDisplay("00112345678"))
        assertEquals("20 de mayo de 2026", InputValidator.formatSpanishDateLabel("20/05/2026", includeYear = true))
        assertEquals("20 de mayo", InputValidator.formatSpanishDateLabel("20/05/2026", includeYear = false))
        assertEquals("31/02/2026", InputValidator.formatSpanishDateLabel("31/02/2026"))
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
