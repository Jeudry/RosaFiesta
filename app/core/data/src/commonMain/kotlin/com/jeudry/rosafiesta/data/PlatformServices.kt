package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.SoundPlayer

/**
 * Creates the native [SoundPlayer] for the current target platform (Android, iOS, Desktop JVM, Web WasmJS).
 */
public expect fun createPlatformSoundPlayer(): SoundPlayer

/**
 * Creates the native [SecureStorage] for the current target platform (Android Keystore, iOS Keychain, POSIX 0600, or Wasm storage).
 */
public expect fun createPlatformSecureStorage(): SecureStorage
