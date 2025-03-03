package com.example.presentation.di

import com.example.data.repositories.DatabaseImpl
import com.example.domain.repository.DatabaseService
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val appModule = module {
    single<CoroutineContext> { Dispatchers.IO }
    singleOf(::DatabaseImpl).bind(DatabaseService::class)
}