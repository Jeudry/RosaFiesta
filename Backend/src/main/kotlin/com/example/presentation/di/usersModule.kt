package com.example.presentation.di

import com.example.core.services.HashingService
import com.example.core.services.JwtService
import com.example.core.services.UserServiceImpl
import com.example.data.repositories.PostgresUserRepository
import com.example.data.services.auth.SHA256HashingService
import com.example.domain.repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val usersModule = module {
    singleOf(::UserServiceImpl).bind(UserServiceImpl::class)
    singleOf(::PostgresUserRepository).bind(UserRepository::class)
    singleOf(::SHA256HashingService).bind(HashingService::class)
    singleOf(::JwtTokenService).bind(JwtService::class)
}