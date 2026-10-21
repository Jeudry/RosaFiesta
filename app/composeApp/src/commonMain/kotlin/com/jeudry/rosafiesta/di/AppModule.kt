package com.jeudry.rosafiesta.di

import com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository
import com.jeudry.rosafiesta.data.RosaFiestaApiClient
import com.jeudry.rosafiesta.data.SqlRosaFiestaRepository
import com.jeudry.rosafiesta.data.createDefaultSqlDriverOrNull
import com.jeudry.rosafiesta.data.createPlatformSecureStorage
import com.jeudry.rosafiesta.data.createPlatformSoundPlayer
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo Koin compartido que registra `RosaFiestaRepository`, `RosaFiestaApiClient`, `SoundPlayer`, `SecureStorage` y `RosaFiestaViewModel`.
 */
public val appModule: Module
    get() = module {
        single { RosaFiestaApiClient(baseUrl = "http://localhost:8788") }
        single<SoundPlayer> { createPlatformSoundPlayer() }
        single<SecureStorage> { createPlatformSecureStorage() }
        single<RosaFiestaRepository> {
            val driver = createDefaultSqlDriverOrNull()
            if (driver != null) {
                SqlRosaFiestaRepository(driver = driver, apiClient = get())
            } else {
                DefaultRosaFiestaRepository()
            }
        }
        factory { RosaFiestaViewModel(repository = get(), soundPlayer = get(), secureStorage = get()) }
    }
