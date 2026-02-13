package com.example.core.data.di

import com.example.core.data.auth.AuthServiceImpl
import com.example.core.data.auth.EncryptedSessionStorage
import com.example.core.data.auth.TokenValidatorImpl
import com.example.core.data.networking.HttpClientFactory
import com.example.core.domain.AuthService
import com.example.core.domain.SessionStorage
import com.example.core.domain.TokenValidator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
  single {
    HttpClientFactory(get()).build()
  }
  singleOf(::EncryptedSessionStorage).bind<SessionStorage>()
  singleOf(::TokenValidatorImpl).bind<TokenValidator>()
  singleOf(::AuthServiceImpl).bind<AuthService>()
}