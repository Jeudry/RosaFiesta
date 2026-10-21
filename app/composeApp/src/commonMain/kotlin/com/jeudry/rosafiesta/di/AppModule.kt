package com.jeudry.rosafiesta.di

import com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository
import com.jeudry.rosafiesta.data.RosaFiestaApiClient
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo Koin compartido que registra `RosaFiestaRepository`, `RosaFiestaApiClient` y `RosaFiestaViewModel`.
 */
public val appModule: Module = module {
    single<RosaFiestaRepository> { DefaultRosaFiestaRepository() }
    single { RosaFiestaApiClient(baseUrl = "http://localhost:8788") }
    factory { RosaFiestaViewModel(repository = get()) }
}
