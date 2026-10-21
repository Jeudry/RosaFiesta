package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
}
