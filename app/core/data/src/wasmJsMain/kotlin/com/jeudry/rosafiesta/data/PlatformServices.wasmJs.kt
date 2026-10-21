package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.SoundPlayer

public actual fun createPlatformSoundPlayer(): SoundPlayer = NoOpSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage = InMemorySecureStorage()
