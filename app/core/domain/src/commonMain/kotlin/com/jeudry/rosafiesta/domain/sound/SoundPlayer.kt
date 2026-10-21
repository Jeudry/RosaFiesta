package com.jeudry.rosafiesta.domain.sound

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Royalty-free (CC0 1.0 Public Domain) UI sound effects for RosaFiesta Personal and Admin apps.
 */
public enum class SoundEffect(
    /** Bundled CC0 `.m4a` file name under `composeResources/files/audio/`. */
    public val fileName: String,
    /** Primary fundamental frequency in Hz for acoustic PCM fallback synthesis. */
    public val primaryHz: Double,
    /** Secondary harmonic/interval frequency in Hz for acoustic PCM fallback synthesis. */
    public val secondaryHz: Double,
    /** Duration in milliseconds. */
    public val durationMs: Int,
) {
    /** Default zero-value no-op sound effect. */
    NONE(fileName = "", primaryHz = 0.0, secondaryHz = 0.0, durationMs = 0),

    /** Soft bubble tap for buttons, chips, and navigation tabs. */
    TAP_SOFT(fileName = "sfx_tap_soft.m4a", primaryHz = 520.0, secondaryHz = 780.0, durationMs = 45),

    /** Crisp pop when adding or incrementing an article in catalog or quote builder. */
    CART_ADD(fileName = "sfx_cart_add.m4a", primaryHz = 587.33, secondaryHz = 880.0, durationMs = 70),

    /** Warm whoosh/drop when sending a message or prompt to Rosa IA. */
    ROSA_SEND(fileName = "sfx_rosa_send.m4a", primaryHz = 440.0, secondaryHz = 659.25, durationMs = 85),

    /** Sparkle chime when Rosa IA replies with a moodboard or package proposal. */
    ROSA_REPLY(fileName = "sfx_rosa_reply.m4a", primaryHz = 659.25, secondaryHz = 987.77, durationMs = 140),

    /** Tactile wooden tick when marking a checklist step in `s68`, `A11`, or `A13`. */
    CHECKLIST_TICK(fileName = "sfx_checklist_tick.m4a", primaryHz = 740.0, secondaryHz = 1108.73, durationMs = 60),

    /** Harmonic harp chord when confirming a payment, quote approval, or digital signature. */
    PAYMENT_SUCCESS(fileName = "sfx_payment_success.m4a", primaryHz = 523.25, secondaryHz = 1046.50, durationMs = 190),

    /** Soft marimba double-tone when a validation error or stock alert is triggered. */
    ALERT_ERROR(fileName = "sfx_alert_error.m4a", primaryHz = 311.13, secondaryHz = 261.63, durationMs = 150),
}

/**
 * Multiplatform sound effects player with user-controllable mute and volume (`s84`).
 */
public interface SoundPlayer {
    /** Master switch for UI sound effects. */
    public var isEnabled: Boolean

    /** Master volume between `0.0f` and `1.0f`. */
    public var soundVolume: Float

    /** Plays the specified [effect] if [isEnabled] is true. */
    public fun play(effect: SoundEffect)

    /** Soft tap shortcut. */
    public fun playTap(): Unit = play(SoundEffect.TAP_SOFT)

    /** Cart / quote item increment shortcut. */
    public fun playCartAdd(): Unit = play(SoundEffect.CART_ADD)

    /** Rosa IA message sent shortcut. */
    public fun playRosaSend(): Unit = play(SoundEffect.ROSA_SEND)

    /** Rosa IA proposal received shortcut. */
    public fun playRosaReply(): Unit = play(SoundEffect.ROSA_REPLY)

    /** Logistics checklist step completed shortcut. */
    public fun playChecklistTick(): Unit = play(SoundEffect.CHECKLIST_TICK)

    /** Payment registered or contract signed shortcut. */
    public fun playPaymentSuccess(): Unit = play(SoundEffect.PAYMENT_SUCCESS)

    /** Validation or stock alert error shortcut. */
    public fun playAlertError(): Unit = play(SoundEffect.ALERT_ERROR)
}

/**
 * Deterministic no-op [SoundPlayer] used in unit/screenshot tests and muted environments.
 */
public class NoOpSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = 0.75f
    public var lastPlayedEffect: SoundEffect = SoundEffect.NONE
        private set

    override fun play(effect: SoundEffect) {
        if (isEnabled && effect != SoundEffect.NONE) {
            lastPlayedEffect = effect
        }
    }
}

/**
 * Synthesizes clean 16-bit mono 22050 Hz PCM WAV bytes for any [SoundEffect] so every platform can play
 * zero-latency royalty-free acoustic feedback without external codec dependencies.
 */
public object PcmSoundSynthesizer {
    private const val SAMPLE_RATE: Int = 22_050

    /**
     * Generates a complete RIFF/WAVE byte array for [effect] scaled by [volume].
     */
    public fun synthesizeWav(effect: SoundEffect, volume: Float = 0.75f): ByteArray {
        if (effect == SoundEffect.NONE || effect.durationMs <= 0) return ByteArray(0)
        val clampedVol = volume.coerceIn(0f, 1f).toDouble()
        val numSamples = (SAMPLE_RATE * effect.durationMs) / 1000
        val pcmDataSize = numSamples * 2
        val buffer = ByteArray(44 + pcmDataSize)

        writeWavHeader(buffer, numSamples, SAMPLE_RATE)

        val splitSample = numSamples / 2
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = if (i < splitSample) effect.primaryHz else effect.secondaryHz
            val attack = (i.toDouble() / (SAMPLE_RATE * 0.004)).coerceAtMost(1.0)
            val decay = exp(-14.0 * t)
            val wave = sin(2.0 * PI * freq * t) + 0.35 * sin(4.0 * PI * freq * t)
            val sampleValue = (wave * attack * decay * clampedVol * 16_000.0)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            val offset = 44 + i * 2
            buffer[offset] = (sampleValue and 0xFF).toByte()
            buffer[offset + 1] = ((sampleValue shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun writeWavHeader(buffer: ByteArray, numSamples: Int, sampleRate: Int) {
        val dataSize = numSamples * 2
        val totalSize = 36 + dataSize
        writeAscii(buffer, 0, "RIFF")
        writeInt32LE(buffer, 4, totalSize)
        writeAscii(buffer, 8, "WAVE")
        writeAscii(buffer, 12, "fmt ")
        writeInt32LE(buffer, 16, 16)
        writeInt16LE(buffer, 20, 1)
        writeInt16LE(buffer, 22, 1)
        writeInt32LE(buffer, 24, sampleRate)
        writeInt32LE(buffer, 28, sampleRate * 2)
        writeInt16LE(buffer, 32, 2)
        writeInt16LE(buffer, 34, 16)
        writeAscii(buffer, 36, "data")
        writeInt32LE(buffer, 40, dataSize)
    }

    private fun writeAscii(buffer: ByteArray, offset: Int, text: String) {
        for (i in text.indices) {
            buffer[offset + i] = text[i].code.toByte()
        }
    }

    private fun writeInt16LE(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    private fun writeInt32LE(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buffer[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buffer[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }
}
