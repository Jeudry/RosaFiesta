package com.jeudry.rosafiesta.di

import com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository
import com.jeudry.rosafiesta.data.RosaFiestaApiClient
import com.jeudry.rosafiesta.data.SqlRosaFiestaRepository
import com.jeudry.rosafiesta.data.createDefaultSqlDriverOrNull
import com.jeudry.rosafiesta.data.createPlatformActions
import com.jeudry.rosafiesta.data.createPlatformSecureStorage
import com.jeudry.rosafiesta.data.createPlatformSoundPlayer
import com.jeudry.rosafiesta.data.createPlatformVoiceInputMonitor
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared Koin dependency graph registering `PlatformActions`, `RosaFiestaApiClient`,
 * `RosaFiestaRepository`, `SoundPlayer`, `SecureStorage`, `VoiceInputMonitor`, and `RosaFiestaViewModel`.
 */
public val appModule: Module
    get() = buildAppModule(seedDemoData = false)

/**
 * Builds the Koin dependency graph, optionally enabling canonical sample seed data for tests.
 */
public fun buildAppModule(seedDemoData: Boolean = false): Module = module {
    single<PlatformActions> { createPlatformActions() }
    single {
        val actions: PlatformActions = get()
        RosaFiestaApiClient(baseUrl = actions.defaultApiBaseUrl())
    }
    single<SoundPlayer> { createPlatformSoundPlayer() }
    single<SecureStorage> { createPlatformSecureStorage() }
    single<VoiceInputMonitor> { createPlatformVoiceInputMonitor() }
    single<RosaFiestaRepository> {
        val driver = createDefaultSqlDriverOrNull()
        if (driver != null) {
            SqlRosaFiestaRepository(
                driver = driver,
                apiClient = get(),
                seedDemoData = seedDemoData,
            )
        } else {
            DefaultRosaFiestaRepository(
                apiClient = get(),
                seedDemoData = seedDemoData,
            )
        }
    }
    factory {
        RosaFiestaViewModel(
            repository = get(),
            soundPlayer = get(),
            secureStorage = get(),
            voiceInputMonitor = get(),
            platformActions = get(),
        )
    }
}
