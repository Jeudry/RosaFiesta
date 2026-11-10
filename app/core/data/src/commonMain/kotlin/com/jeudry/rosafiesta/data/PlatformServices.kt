package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor

/**
 * Creates the native [SoundPlayer] for the current target platform (Android, iOS, Desktop JVM, Web WasmJS).
 */
public expect fun createPlatformSoundPlayer(): SoundPlayer

/**
 * Creates the native [SecureStorage] for the current target platform (Android Keystore, iOS Keychain, JVM AES-GCM, or Wasm localStorage).
 */
public expect fun createPlatformSecureStorage(): SecureStorage

/**
 * Creates the native [VoiceInputMonitor] for the current target platform to capture live microphone audio and speech.
 */
public expect fun createPlatformVoiceInputMonitor(): VoiceInputMonitor

/**
 * Creates the native [PlatformActions] implementation for external URLs, native share sheets, document exports, and media picking.
 */
public expect fun createPlatformActions(): PlatformActions
