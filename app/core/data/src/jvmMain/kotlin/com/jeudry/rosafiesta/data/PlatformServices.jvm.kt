package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.Executors
import javax.sound.sampled.AudioSystem

internal class DesktopSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = 0.75f
    public var lastPlayedEffect: SoundEffect = SoundEffect.NONE
        private set

    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-desktop-sfx").apply { isDaemon = true }
    }

    override fun play(effect: SoundEffect) {
        if (!isEnabled || effect == SoundEffect.NONE || soundVolume <= 0.01f) return
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

public actual fun createPlatformSecureStorage(): SecureStorage = DesktopSecureStorage()
