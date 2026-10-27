package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

internal class DesktopSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = 0.75f
    public var lastPlayedEffect: SoundEffect = SoundEffect.NONE
        private set

    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-desktop-sfx").apply { isDaemon = true }
    }

    override fun play(effect: SoundEffect) {
        if (!isEnabled) return
        if (effect == SoundEffect.NONE) return
        if (soundVolume <= 0.01f) return
        lastPlayedEffect = effect
        val wavBytes = PcmSoundSynthesizer.synthesizeWav(effect, soundVolume)
        if (wavBytes.isEmpty()) return
        audioExecutor.execute {
            runCatching {
                AudioSystem.getAudioInputStream(ByteArrayInputStream(wavBytes)).use { stream ->
                    val clip = AudioSystem.getClip()
                    clip.open(stream)
                    clip.start()
                }
            }
        }
    }
}

internal class DesktopSecureStorage(
    private val directory: File = File(System.getProperty("user.home"), ".rosafiesta/secure"),
) : SecureStorage {
    init {
        runCatching { directory.mkdirs() }
    }

    override suspend fun get(key: String): String? = runCatching {
        fileFor(key).takeIf { it.exists() }?.readText()?.trim()
    }.getOrNull()

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        runCatching {
            val target = fileFor(cleanKey)
            target.parentFile?.mkdirs()
            target.writeText(value.trim())
            Files.setPosixFilePermissions(target.toPath(), PosixFilePermissions.fromString("rw-------"))
        }
    }

    override suspend fun remove(key: String) {
        runCatching { fileFor(key).delete() }
    }

    private fun fileFor(key: String): File {
        val sanitized = key.trim().replace(Regex("[^A-Za-z0-9_-]"), "_")
        return File(directory, sanitized)
    }
}

public actual fun createPlatformSoundPlayer(): SoundPlayer = DesktopSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage {
    if (isRunningInAutomatedTest()) {
        val tempDir = Files.createTempDirectory("rosafiesta-test-secure").toFile().apply { deleteOnExit() }
        return DesktopSecureStorage(directory = tempDir)
    }
    return DesktopSecureStorage()
}

internal class DesktopVoiceInputMonitor : VoiceInputMonitor {
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

    private val activeFlag = AtomicBoolean(false)
    private var activeLine: TargetDataLine? = null

    private val captureExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-desktop-mic").apply { isDaemon = true }
    }

    override fun startListening(onTranscriptResult: (String) -> Unit) {
        if (!activeFlag.compareAndSet(false, true)) return
        _recognizedTranscript.value = ""
        _isTranscribing.value = false
        _isListening.value = true
        captureExecutor.execute {
            runCaptureLoop(onTranscriptResult)
        }
    }

    override fun stopListening() {
        activeFlag.set(false)
        _isListening.value = false
        _amplitudeLevel.value = 0f
        runCatching {
            activeLine?.stop()
        }
    }

    private fun runCaptureLoop(onTranscriptResult: (String) -> Unit) {
        val format = AudioFormat(SAMPLE_RATE_HZ.toFloat(), 16, 1, true, false)
        val info = DataLine.Info(TargetDataLine::class.java, format)
        val line = runCatching {
            if (AudioSystem.isLineSupported(info)) {
                (AudioSystem.getLine(info) as? TargetDataLine)?.apply {
                    open(format, 2048)
                    start()
                }
            } else {
                null
            }
        }.getOrNull()

        activeLine = line
        _hasHardwareMicrophone.value = line != null

        val recordedPcm = ByteArrayOutputStream(96_000)
        val buffer = ByteArray(1024)
        var ambientNoiseRms = 0.006
        var speechChunkCount = 0
        var silentChunksAfterSpeech = 0

        for (iteration in 0 until MAX_CAPTURE_CHUNKS) {
            if (!activeFlag.get()) break
            if (line != null) {
                val bytesRead = runCatching { line.read(buffer, 0, buffer.size) }.getOrDefault(0)
                if (bytesRead > 1) {
                    recordedPcm.write(buffer, 0, bytesRead)
                    val rawRms = computeRawRms(buffer, bytesRead)
                    val uiAmplitude = (rawRms * 5.2).toFloat().coerceIn(0.05f, 1.0f)
                    _amplitudeLevel.value = uiAmplitude

                    if (iteration < AMBIENT_CALIBRATION_CHUNKS) {
                        val candidateFloor = (ambientNoiseRms * 0.65) + (rawRms * 0.35)
                        ambientNoiseRms = candidateFloor.coerceIn(0.002, 0.018)
                    }

                    val speechThreshold = (ambientNoiseRms * 2.1).coerceIn(0.011, 0.032)
                    val isSpeechChunk = rawRms >= speechThreshold
                    if (isSpeechChunk) {
                        speechChunkCount += 1
                        silentChunksAfterSpeech = 0
                    } else {
                        val hasSpokenEnough = speechChunkCount >= MIN_SPEECH_CHUNKS
                        if (hasSpokenEnough) {
                            silentChunksAfterSpeech += 1
                            if (silentChunksAfterSpeech >= SILENCE_CHUNKS_TO_AUTO_STOP) {
                                break
                            }
                        } else if (iteration >= MAX_INITIAL_SILENCE_CHUNKS) {
                            break
                        }
                    }
                } else {
                    Thread.sleep(30L)
                }
            } else {
                val phase = iteration * 0.38
                val syntheticWave = (0.35 + 0.30 * sin(phase) + 0.18 * sin(phase * 2.3)).toFloat().coerceIn(0.08f, 0.92f)
                _amplitudeLevel.value = syntheticWave
                Thread.sleep(50L)
                if (iteration >= 35) break
            }
        }

        runCatching {
            line?.stop()
            line?.close()
        }
        activeLine = null
        activeFlag.set(false)
        _isListening.value = false
        _amplitudeLevel.value = 0f

        val pcmBytes = recordedPcm.toByteArray()
        val hasEnoughPcm = pcmBytes.size >= MIN_PCM_BYTES_FOR_STT
        val canTranscribe = if (hasEnoughPcm) !isRunningInAutomatedTest() else false
        if (canTranscribe) {
            _isTranscribing.value = true
            val transcript = runCatching { transcribeSpanishPcm(pcmBytes) }.getOrDefault("")
            _isTranscribing.value = false
            val cleanTranscript = transcript.trim()
            if (cleanTranscript.isNotEmpty()) {
                _recognizedTranscript.value = cleanTranscript
                onTranscriptResult(cleanTranscript)
            }
        }
    }

    private fun computeRawRms(buffer: ByteArray, bytesRead: Int): Double {
        var sumSquares = 0.0
        val sampleCount = bytesRead / 2
        if (sampleCount <= 0) return 0.0
        for (sampleIdx in 0 until sampleCount) {
            val lowByte = buffer[sampleIdx * 2].toInt() and 0xFF
            val highByte = buffer[sampleIdx * 2 + 1].toInt()
            val sample = (highByte shl 8) or lowByte
            val normalizedSample = sample / 32768.0
            sumSquares += normalizedSample * normalizedSample
        }
        return sqrt(sumSquares / sampleCount)
    }

    private fun transcribeSpanishPcm(rawPcm: ByteArray): String {
        val preparedPcm = normalizeAndPadPcm(rawPcm)
        if (preparedPcm.isEmpty()) return ""
        val wavBytes = encodePcm16MonoWav(preparedPcm, SAMPLE_RATE_HZ)
        val tempWav = Files.createTempFile("rf-voice-", ".wav").toFile()
        try {
            tempWav.writeBytes(wavBytes)
            val pythonExecutable = if (File("/usr/bin/python3").exists()) "/usr/bin/python3" else "python3"
            val script = """
                import sys, speech_recognition as sr
                r = sr.Recognizer()
                with sr.AudioFile(sys.argv[1]) as src:
                    audio = r.record(src)
                try:
                    print(r.recognize_google(audio, language="es-DO"))
                except Exception:
                    try:
                        print(r.recognize_google(audio, language="es-ES"))
                    except Exception:
                        print("")
            """.trimIndent()
            val process = ProcessBuilder(pythonExecutable, "-c", script, tempWav.absolutePath)
                .redirectErrorStream(false)
                .start()
            val finished = process.waitFor(6, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return ""
            }
            return process.inputStream.bufferedReader().readText().lineSequence().firstOrNull().orEmpty().trim()
        } finally {
            runCatching { tempWav.delete() }
        }
    }

    private fun normalizeAndPadPcm(rawPcm: ByteArray): ByteArray {
        val sampleCount = rawPcm.size / 2
        if (sampleCount <= 0) return ByteArray(0)
        var maxAbsSample = 1
        for (idx in 0 until sampleCount) {
            val low = rawPcm[idx * 2].toInt() and 0xFF
            val high = rawPcm[idx * 2 + 1].toInt()
            val sample = (high shl 8) or low
            val absSample = abs(sample)
            if (absSample > maxAbsSample) {
                maxAbsSample = absSample
            }
        }
        if (maxAbsSample < 120) {
            return ByteArray(0)
        }
        val gain = (22_000.0 / maxAbsSample).coerceIn(1.0, 12.0)
        val padBytesCount = (SAMPLE_RATE_HZ * 4 / 10) * 2
        val output = ByteArray(padBytesCount + (sampleCount * 2) + padBytesCount)
        for (idx in 0 until sampleCount) {
            val low = rawPcm[idx * 2].toInt() and 0xFF
            val high = rawPcm[idx * 2 + 1].toInt()
            val sample = (high shl 8) or low
            val boosted = (sample * gain).toInt().coerceIn(-32767, 32767)
            val targetOffset = padBytesCount + (idx * 2)
            output[targetOffset] = (boosted and 0xFF).toByte()
            output[targetOffset + 1] = ((boosted shr 8) and 0xFF).toByte()
        }
        return output
    }

    private fun encodePcm16MonoWav(pcmBytes: ByteArray, sampleRate: Int): ByteArray {
        val byteRate = sampleRate * 2
        val totalDataLen = pcmBytes.size + 36
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".encodeToByteArray())
            putInt(totalDataLen)
            put("WAVE".encodeToByteArray())
            put("fmt ".encodeToByteArray())
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(sampleRate)
            putInt(byteRate)
            putShort(2)
            putShort(16)
            put("data".encodeToByteArray())
            putInt(pcmBytes.size)
        }.array()
        return header + pcmBytes
    }

    private companion object {
        const val SAMPLE_RATE_HZ = 16_000
        const val AMBIENT_CALIBRATION_CHUNKS = 4
        const val MIN_SPEECH_CHUNKS = 3
        const val SILENCE_CHUNKS_TO_AUTO_STOP = 32
        const val MAX_INITIAL_SILENCE_CHUNKS = 120
        const val MAX_CAPTURE_CHUNKS = 250
        const val MIN_PCM_BYTES_FOR_STT = 3_200
    }
}

public actual fun createPlatformVoiceInputMonitor(): VoiceInputMonitor {
    if (isRunningInAutomatedTest()) {
        return com.jeudry.rosafiesta.domain.sound.NoOpVoiceInputMonitor()
    }
    return DesktopVoiceInputMonitor()
}
