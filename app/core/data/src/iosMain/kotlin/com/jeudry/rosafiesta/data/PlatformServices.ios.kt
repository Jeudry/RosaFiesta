@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

internal class IosSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = 0.75f
    private var activePlayer: AVAudioPlayer? = null

    override fun play(effect: SoundEffect) {
        if (!isEnabled || effect == SoundEffect.NONE || soundVolume <= 0.01f) return
        runCatching {
            val haptic = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            haptic.prepare()
            haptic.impactOccurred()
        }
        runCatching {
            val baseName = effect.fileName.substringBeforeLast(".")
            val ext = effect.fileName.substringAfterLast(".", "m4a")
            val bundleUrl = NSBundle.mainBundle.URLForResource(
                name = baseName,
                withExtension = ext,
                subdirectory = "compose-resources/composeResources/rosafiesta.composeapp.generated.resources/files/audio",
            )
            val player = if (bundleUrl != null) {
                AVAudioPlayer(contentsOfURL = bundleUrl, error = null)
            } else {
                val wavBytes = PcmSoundSynthesizer.synthesizeWav(effect, soundVolume)
                val nsData = wavBytes.usePinned { pinned ->
                    NSData.create(bytes = pinned.addressOf(0), length = wavBytes.size.toULong())
                }
                AVAudioPlayer(data = nsData, error = null)
            }
            player.volume = soundVolume.coerceIn(0f, 1f)
            player.prepareToPlay()
            player.play()
            activePlayer = player
        }
    }
}

internal class IosSecureStorage : SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun get(key: String): String? {
        val cleanKey = scopedKey(key)
        return defaults.stringForKey(cleanKey)
    }

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        defaults.setObject(value.trim(), forKey = scopedKey(cleanKey))
    }

    override suspend fun remove(key: String) {
        defaults.removeObjectForKey(scopedKey(key))
    }

    private fun scopedKey(raw: String): String = "com.jeudry.rosafiesta.secure.${raw.trim()}"
}

public actual fun createPlatformSoundPlayer(): SoundPlayer = IosSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage = IosSecureStorage()
