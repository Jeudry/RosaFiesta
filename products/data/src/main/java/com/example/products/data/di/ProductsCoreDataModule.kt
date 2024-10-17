package com.example.products.data.di

import com.example.products.data.repository.ProductsRepositoryImpl
import com.example.products.domain.repositories.ProductsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val productsCoreDataModule = module {
    singleOf(::ProductsRepositoryImpl).bind<ProductsRepository>()
}