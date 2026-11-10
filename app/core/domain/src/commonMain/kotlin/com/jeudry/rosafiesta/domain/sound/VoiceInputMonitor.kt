package com.jeudry.rosafiesta.domain.sound

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Multiplatform microphone level and speech capture monitor for RosaFiesta voice search and assistant voice input.
 */
public interface VoiceInputMonitor {
    /** Whether microphone capture is currently active. */
    public val isListening: StateFlow<Boolean>

    /** Whether captured speech audio is currently being transcribed into text. */
    public val isTranscribing: StateFlow<Boolean>

    /** Normalized RMS audio amplitude between `0.0f` and `1.0f` captured from the hardware microphone. */
    public val amplitudeLevel: StateFlow<Float>

    /** Alias for [amplitudeLevel] exposing live RMS capture intensity. */
    public val level: StateFlow<Float>
        get() = amplitudeLevel

    /** Whether a physical audio capture line or speech engine was opened on the current device. */
    public val hasHardwareMicrophone: StateFlow<Boolean>

    /** Latest recognized Spanish transcript emitted during or at the end of speech capture. */
    public val recognizedTranscript: StateFlow<String>

    /** Alias for [recognizedTranscript]. */
    public val transcript: StateFlow<String>
        get() = recognizedTranscript

    /** Latest RIFF/WAVE 16-bit PCM capture buffer when direct PCM recording is supported on the platform. */
    public val capturedWavBytes: StateFlow<ByteArray?>

    /** Latest hardware or speech-recognition diagnostic message, or `null` when healthy. */
    public val errorMessage: StateFlow<String?>

    /** Starts listening to the platform microphone and invokes [onTranscriptResult] when speech ends. */
    public fun startListening(onTranscriptResult: (String) -> Unit = {})

    /** Stops listening and releases the platform audio line or recognizer. */
    public fun stopListening()
}

/**
 * Deterministic no-op [VoiceInputMonitor] used in unit tests.
 */
public class NoOpVoiceInputMonitor : VoiceInputMonitor {
    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _amplitudeLevel = MutableStateFlow(0f)
    override val amplitudeLevel: StateFlow<Float> = _amplitudeLevel.asStateFlow()

    private val _hasHardwareMicrophone = MutableStateFlow(false)
    override val hasHardwareMicrophone: StateFlow<Boolean> = _hasHardwareMicrophone.asStateFlow()

    private val _recognizedTranscript = MutableStateFlow("")
    override val recognizedTranscript: StateFlow<String> = _recognizedTranscript.asStateFlow()

    private val _capturedWavBytes = MutableStateFlow<ByteArray?>(null)
    override val capturedWavBytes: StateFlow<ByteArray?> = _capturedWavBytes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    override fun startListening(onTranscriptResult: (String) -> Unit) {
        _errorMessage.value = null
        _isListening.value = true
        _amplitudeLevel.value = 0f
    }

    override fun stopListening() {
        _isListening.value = false
        _isTranscribing.value = false
        _amplitudeLevel.value = 0f
    }
}

/**
 * Pure-Kotlin RIFF/WAVE encoder and RMS power meter for 16-bit little-endian mono PCM microphone captures.
 */
public object WavPcmEncoder {
    /**
     * Encodes 16-bit little-endian mono PCM samples into a complete RIFF/WAVE container.
     */
    public fun encodeMono16BitPcmToWav(
        pcmBytes: ByteArray,
        sampleRateHz: Int = 16_000,
    ): ByteArray {
        val safeRate = sampleRateHz.coerceAtLeast(8_000)
        val evenByteCount = pcmBytes.size - (pcmBytes.size % 2)
        val byteRate = safeRate * 2
        val totalDataLength = evenByteCount + 36
        val output = ByteArray(44 + evenByteCount)

        writeAscii(output, 0, "RIFF")
        writeInt32LittleEndian(output, 4, totalDataLength)
        writeAscii(output, 8, "WAVE")
        writeAscii(output, 12, "fmt ")
        writeInt32LittleEndian(output, 16, 16)
        writeInt16LittleEndian(output, 20, 1)
        writeInt16LittleEndian(output, 22, 1)
        writeInt32LittleEndian(output, 24, safeRate)
        writeInt32LittleEndian(output, 28, byteRate)
        writeInt16LittleEndian(output, 32, 2)
        writeInt16LittleEndian(output, 34, 16)
        writeAscii(output, 36, "data")
        writeInt32LittleEndian(output, 40, evenByteCount)

        if (evenByteCount > 0) {
            pcmBytes.copyInto(output, destinationOffset = 44, startIndex = 0, endIndex = evenByteCount)
        }
        return output
    }

    /**
     * Computes normalized RMS amplitude in `[0.0, 1.0]` from 16-bit signed little-endian PCM bytes.
     */
    public fun computeNormalizedRms(
        pcmBytes: ByteArray,
        bytesRead: Int = pcmBytes.size,
    ): Float {
        val safeBytes = bytesRead.coerceIn(0, pcmBytes.size)
        val sampleCount = safeBytes / 2
        if (sampleCount <= 0) return 0f

        var sumSquares = 0.0
        for (sampleIndex in 0 until sampleCount) {
            val lowByte = pcmBytes[sampleIndex * 2].toInt() and 0xFF
            val highByte = pcmBytes[sampleIndex * 2 + 1].toInt()
            val signedSample = (highByte shl 8) or lowByte
            val normalizedSample = signedSample / 32768.0
            sumSquares += normalizedSample * normalizedSample
        }
        val rawRms = sqrt(sumSquares / sampleCount)
        return (rawRms * 5.0).toFloat().coerceIn(0f, 1f)
    }

    private fun writeAscii(buffer: ByteArray, offset: Int, text: String) {
        for (charIndex in text.indices) {
            buffer[offset + charIndex] = text[charIndex].code.toByte()
        }
    }

    private fun writeInt16LittleEndian(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    private fun writeInt32LittleEndian(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buffer[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buffer[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }
}
