package com.jeudry.rosafiesta.domain.sound

import kotlin.math.PI
import kotlin.math.cos
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

    /** Soft tactile pop for buttons, chips, and navigation tabs. */
    TAP_SOFT(fileName = "sfx_tap_soft.m4a", primaryHz = 340.0, secondaryHz = 145.0, durationMs = 26),

    /** Crisp bubble pluck when adding or incrementing an article in catalog or quote builder. */
    CART_ADD(fileName = "sfx_cart_add.m4a", primaryHz = 460.0, secondaryHz = 780.0, durationMs = 68),

    /** Warm whoosh-pop when sending a message or prompt to Rosa IA. */
    ROSA_SEND(fileName = "sfx_rosa_send.m4a", primaryHz = 320.0, secondaryHz = 580.0, durationMs = 80),

    /** Sparkle bell chime when Rosa IA replies with a moodboard or package proposal. */
    ROSA_REPLY(fileName = "sfx_rosa_reply.m4a", primaryHz = 659.25, secondaryHz = 987.77, durationMs = 175),

    /** Tactile wooden tick when marking a checklist step in `s68`, `A11`, or `A13`. */
    CHECKLIST_TICK(fileName = "sfx_checklist_tick.m4a", primaryHz = 720.0, secondaryHz = 320.0, durationMs = 34),

    /** Harmonic harp chord when confirming a payment, quote approval, or digital signature. */
    PAYMENT_SUCCESS(fileName = "sfx_payment_success.m4a", primaryHz = 523.25, secondaryHz = 1046.50, durationMs = 240),

    /** Soft muted double-knock when a validation error or stock alert is triggered. */
    ALERT_ERROR(fileName = "sfx_alert_error.m4a", primaryHz = 280.0, secondaryHz = 170.0, durationMs = 145),
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
 * Synthesizes 16-bit mono 44100 Hz PCM WAV bytes using continuous-phase acoustic envelopes so every platform
 * plays warm, organic tactile UI feedback without harsh frequency-jump clicks.
 */
public object PcmSoundSynthesizer {
    /** Sample rate in Hz for all synthesized UI feedback WAV buffers. */
    public const val SAMPLE_RATE: Int = 44_100
    private const val TAIL_SILENCE_MS: Int = 5

    /**
     * Generates a complete RIFF/WAVE byte array for [effect] scaled by [volume].
     */
    public fun synthesizeWav(effect: SoundEffect, volume: Float = 0.75f): ByteArray {
        val isSilentEffect = effect == SoundEffect.NONE
        val hasInvalidDuration = effect.durationMs <= 0
        if (isSilentEffect || hasInvalidDuration) return ByteArray(0)

        val clampedVol = volume.coerceIn(0f, 1f).toDouble()
        val activeSamples = (SAMPLE_RATE * effect.durationMs) / 1000
        val tailSamples = (SAMPLE_RATE * TAIL_SILENCE_MS) / 1000
        val totalSamples = activeSamples + tailSamples
        val pcmDataSize = totalSamples * 2
        val buffer = ByteArray(44 + pcmDataSize)

        writeWavHeader(buffer, totalSamples, SAMPLE_RATE)
        val signal = renderSignal(effect, activeSamples)

        for (i in 0 until activeSamples) {
            val fadeOut = computeTailTaper(index = i, totalSamples = activeSamples, taperMs = 4.0)
            val sampleValue = (signal[i] * fadeOut * clampedVol * 12_000.0)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            val offset = 44 + i * 2
            buffer[offset] = (sampleValue and 0xFF).toByte()
            buffer[offset + 1] = ((sampleValue shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun renderSignal(effect: SoundEffect, numSamples: Int): DoubleArray = when (effect) {
        SoundEffect.NONE -> DoubleArray(numSamples)

        SoundEffect.TAP_SOFT -> synthesizePitchDropPercussion(
            numSamples = numSamples,
            startHz = 340.0,
            endHz = 145.0,
            pitchDecayRate = 185.0,
            ampDecayRate = 155.0,
            harmonicMix = 0.16,
            harmonicDecayRate = 300.0,
            attackMs = 1.4,
            gain = 0.85,
        )

        SoundEffect.CHECKLIST_TICK -> synthesizePitchDropPercussion(
            numSamples = numSamples,
            startHz = 720.0,
            endHz = 320.0,
            pitchDecayRate = 160.0,
            ampDecayRate = 125.0,
            harmonicMix = 0.22,
            harmonicDecayRate = 260.0,
            attackMs = 1.2,
            gain = 0.90,
        )

        SoundEffect.CART_ADD -> synthesizeRisingBubble(
            numSamples = numSamples,
            startHz = 460.0,
            endHz = 780.0,
            riseRate = 55.0,
            ampDecayRate = 52.0,
            harmonicMix = 0.18,
            gain = 0.85,
        )

        SoundEffect.ROSA_SEND -> synthesizeRisingBubble(
            numSamples = numSamples,
            startHz = 320.0,
            endHz = 580.0,
            riseRate = 42.0,
            ampDecayRate = 45.0,
            harmonicMix = 0.12,
            gain = 0.80,
        )

        SoundEffect.ROSA_REPLY -> synthesizeLayeredNotes(
            numSamples = numSamples,
            notesHz = doubleArrayOf(659.25, 987.77),
            offsetsMs = doubleArrayOf(0.0, 52.0),
            decayRate = 23.0,
            harmonicMix = 0.18,
            gain = 0.72,
        )

        SoundEffect.PAYMENT_SUCCESS -> synthesizeLayeredNotes(
            numSamples = numSamples,
            notesHz = doubleArrayOf(523.25, 659.25, 783.99, 1046.50),
            offsetsMs = doubleArrayOf(0.0, 42.0, 84.0, 126.0),
            decayRate = 20.0,
            harmonicMix = 0.15,
            gain = 0.62,
        )

        SoundEffect.ALERT_ERROR -> synthesizeDoubleKnock(numSamples)
    }

    private fun synthesizePitchDropPercussion(
        numSamples: Int,
        startHz: Double,
        endHz: Double,
        pitchDecayRate: Double,
        ampDecayRate: Double,
        harmonicMix: Double,
        harmonicDecayRate: Double,
        attackMs: Double,
        gain: Double,
    ): DoubleArray {
        val output = DoubleArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = endHz + (startHz - endHz) * exp(-pitchDecayRate * t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val attack = computeCosineAttack(tSeconds = t, attackSeconds = attackMs / 1000.0)
            val fundamental = sin(phase) * exp(-ampDecayRate * t)
            val overtone = harmonicMix * sin(2.0 * phase) * exp(-harmonicDecayRate * t)
            output[i] = (fundamental + overtone) * attack * gain
        }
        return output
    }

    private fun synthesizeRisingBubble(
        numSamples: Int,
        startHz: Double,
        endHz: Double,
        riseRate: Double,
        ampDecayRate: Double,
        harmonicMix: Double,
        gain: Double,
    ): DoubleArray {
        val output = DoubleArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = startHz + (endHz - startHz) * (1.0 - exp(-riseRate * t))
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val attack = computeCosineAttack(tSeconds = t, attackSeconds = 0.004)
            val body = sin(phase) + harmonicMix * sin(2.0 * phase) * exp(-ampDecayRate * 1.4 * t)
            output[i] = body * attack * exp(-ampDecayRate * t) * gain
        }
        return output
    }

    private fun synthesizeLayeredNotes(
        numSamples: Int,
        notesHz: DoubleArray,
        offsetsMs: DoubleArray,
        decayRate: Double,
        harmonicMix: Double,
        gain: Double,
    ): DoubleArray {
        val output = DoubleArray(numSamples)
        for (noteIndex in notesHz.indices) {
            val freq = notesHz[noteIndex]
            val startSample = ((offsetsMs[noteIndex] / 1000.0) * SAMPLE_RATE).toInt()
            for (i in startSample until numSamples) {
                val localT = (i - startSample).toDouble() / SAMPLE_RATE
                val phase = 2.0 * PI * freq * localT
                val attack = computeCosineAttack(tSeconds = localT, attackSeconds = 0.004)
                val overtone = harmonicMix * sin(2.0 * phase) * exp(-decayRate * 1.8 * localT)
                val tone = (sin(phase) + overtone) * attack * exp(-decayRate * localT)
                output[i] += tone * gain
            }
        }
        return output
    }

    private fun synthesizeDoubleKnock(numSamples: Int): DoubleArray {
        val firstKnock = synthesizePitchDropPercussion(
            numSamples = numSamples,
            startHz = 285.0,
            endHz = 190.0,
            pitchDecayRate = 120.0,
            ampDecayRate = 65.0,
            harmonicMix = 0.14,
            harmonicDecayRate = 180.0,
            attackMs = 2.0,
            gain = 0.78,
        )
        val secondOffsetSamples = (0.065 * SAMPLE_RATE).toInt()
        val secondKnock = synthesizePitchDropPercussion(
            numSamples = (numSamples - secondOffsetSamples).coerceAtLeast(0),
            startHz = 230.0,
            endHz = 155.0,
            pitchDecayRate = 120.0,
            ampDecayRate = 55.0,
            harmonicMix = 0.12,
            harmonicDecayRate = 160.0,
            attackMs = 2.0,
            gain = 0.85,
        )
        for (i in secondKnock.indices) {
            firstKnock[secondOffsetSamples + i] += secondKnock[i]
        }
        return firstKnock
    }

    private fun computeCosineAttack(tSeconds: Double, attackSeconds: Double): Double {
        val hasAttack = attackSeconds > 0.0
        val withinAttackWindow = tSeconds < attackSeconds
        if (!hasAttack || !withinAttackWindow) return 1.0
        val progress = (tSeconds / attackSeconds).coerceIn(0.0, 1.0)
        return 0.5 * (1.0 - cos(PI * progress))
    }

    private fun computeTailTaper(index: Int, totalSamples: Int, taperMs: Double): Double {
        val taperSamples = ((taperMs / 1000.0) * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val remaining = totalSamples - 1 - index
        if (remaining >= taperSamples) return 1.0
        val progress = (remaining.toDouble() / taperSamples).coerceIn(0.0, 1.0)
        return 0.5 * (1.0 - cos(PI * progress))
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
