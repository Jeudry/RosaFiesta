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
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
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

    private val _amplitudeLevel = MutableStateFlow(0f)
    override val amplitudeLevel: StateFlow<Float> = _amplitudeLevel.asStateFlow()

    private val _hasHardwareMicrophone = MutableStateFlow(false)
    override val hasHardwareMicrophone: StateFlow<Boolean> = _hasHardwareMicrophone.asStateFlow()

    private val activeFlag = AtomicBoolean(false)
    private var activeLine: TargetDataLine? = null

    private val captureExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-desktop-mic").apply { isDaemon = true }
    }

    override fun startListening() {
        if (!activeFlag.compareAndSet(false, true)) return
        _isListening.value = true
        captureExecutor.execute {
            runCaptureLoop()
        }
    }

    override fun stopListening() {
        activeFlag.set(false)
        _isListening.value = false
        _amplitudeLevel.value = 0f
        runCatching {
            activeLine?.stop()
            activeLine?.close()
            activeLine = null
        }
    }

    private fun runCaptureLoop() {
        val format = AudioFormat(16_000f, 16, 1, true, false)
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

        val buffer = ByteArray(1024)
        for (iteration in 0 until MAX_CAPTURE_ITERATIONS) {
            if (!activeFlag.get()) break
            if (line != null) {
                val bytesRead = runCatching { line.read(buffer, 0, buffer.size) }.getOrDefault(0)
                if (bytesRead > 1) {
                    val rms = computeNormalizedRms(buffer, bytesRead)
                    _amplitudeLevel.value = rms
                } else {
                    Thread.sleep(40L)
                }
            } else {
                val phase = iteration * 0.38
                val syntheticWave = (0.35 + 0.30 * sin(phase) + 0.18 * sin(phase * 2.3)).toFloat().coerceIn(0.08f, 0.92f)
                _amplitudeLevel.value = syntheticWave
                Thread.sleep(60L)
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
    }

    private fun computeNormalizedRms(buffer: ByteArray, bytesRead: Int): Float {
        var sumSquares = 0.0
        val sampleCount = bytesRead / 2
        if (sampleCount <= 0) return 0f
        for (sampleIdx in 0 until sampleCount) {
            val lowByte = buffer[sampleIdx * 2].toInt() and 0xFF
            val highByte = buffer[sampleIdx * 2 + 1].toInt()
            val sample = (highByte shl 8) or lowByte
            val normalizedSample = sample / 32768.0
            sumSquares += normalizedSample * normalizedSample
        }
        val rms = sqrt(sumSquares / sampleCount)
        return (rms * 4.5).toFloat().coerceIn(0.05f, 1.0f)
    }

    private companion object {
        const val MAX_CAPTURE_ITERATIONS = 1_500
    }
}

public actual fun createPlatformVoiceInputMonitor(): VoiceInputMonitor = DesktopVoiceInputMonitor()
