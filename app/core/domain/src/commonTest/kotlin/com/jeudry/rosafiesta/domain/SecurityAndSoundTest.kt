package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
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
        val raw = "Error login camila@rosafiesta.do tel 809-555-0142 Authorization: Bearer rf-token-camila password=MySecret123 ref BHD-2026-8841"
        val sanitized = SensitiveDataRedactor.redactText(raw)

        assertFalse(sanitized.contains("camila@rosafiesta.do"))
        assertFalse(sanitized.contains("809-555-0142"))
        assertFalse(sanitized.contains("rf-token-camila"))
        assertFalse(sanitized.contains("MySecret123"))
        assertFalse(sanitized.contains("BHD-2026-8841"))

        assertTrue(sanitized.contains("c***a@rosafiesta.do"))
        assertTrue(sanitized.contains("809-***-**42"))
        assertTrue(sanitized.contains("Bearer [REDACTED]"))
        assertTrue(sanitized.contains("password=[REDACTED]"))
        assertTrue(sanitized.contains("BHD-***-41"))
    }

    @Test
    fun `verifica que translateBusinessError traduzca por codigo de negocio y censure el fallback`() {
        val translated = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", "raw secret")
        assertTrue(translated.contains("Correo o contraseña incorrectos"))

        val fallback = SensitiveDataRedactor.translateBusinessError("UNKNOWN_NEW_CODE", "Fallo para camila@rosafiesta.do")
        assertFalse(fallback.contains("camila@rosafiesta.do"))
        assertTrue(fallback.contains("c***a@rosafiesta.do"))
    }

    @Test
    fun `verifica que PcmSoundSynthesizer genere cabeceras RIFF WAVE validas para los efectos CC0`() {
        val wavBytes = PcmSoundSynthesizer.synthesizeWav(SoundEffect.PAYMENT_SUCCESS, volume = 0.8f)
        assertTrue(wavBytes.size > 44)
        val riff = wavBytes.decodeToString(0, 4)
        val wave = wavBytes.decodeToString(8, 12)
        assertEquals("RIFF", riff)
        assertEquals("WAVE", wave)

        val player = NoOpSoundPlayer()
        player.playPaymentSuccess()
        assertEquals(SoundEffect.PAYMENT_SUCCESS, player.lastPlayedEffect)
    }

    @Test
    fun `verifica que InMemorySecureStorage guarde recupere y elimine tokens de sesion`() = runTest {
        val storage = InMemorySecureStorage()
        storage.put(SecureStorage.KEY_ACCESS_TOKEN, "  rf-token-maria  ")
        assertEquals("rf-token-maria", storage.get(SecureStorage.KEY_ACCESS_TOKEN))

        storage.remove(SecureStorage.KEY_ACCESS_TOKEN)
        assertNull(storage.get(SecureStorage.KEY_ACCESS_TOKEN))
    }

    @Test
    fun `verifica firma y validacion de tokens JWT HS256 y hashing de contrasenas con sal`() {
        val hash = JwtAuthManager.hashPassword("RosaFiesta2026!", "usr-test-salt")
        assertTrue(JwtAuthManager.verifyPassword("RosaFiesta2026!", "usr-test-salt", hash))
        assertFalse(JwtAuthManager.verifyPassword("WrongPass2026!", "usr-test-salt", hash))

        val account = JwtAuthManager.defaultSeedAccounts.first()
        val token = JwtAuthManager.issueAccessToken(account, issuedAtEpochSeconds = 1_793_050_000L)
        assertEquals(3, token.split('.').size)

        val verified = JwtAuthManager.verifyAccessToken(token)
        assertNotNull(verified)
        assertEquals(account.id, verified.subjectId)
        assertEquals(account.username, verified.username)
        assertEquals(account.role, verified.role)

        val tamperedToken = token.dropLast(2) + "AA"
        assertNull(JwtAuthManager.verifyAccessToken(tamperedToken))
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
}
