package com.example.presentation.di

import com.example.core.services.ProductsServiceImpl
import com.example.data.repositories.PostgresProductRepository
import com.example.data.services.products.ProductsService
import com.example.domain.repository.ProductsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val productsModule = module {
    singleOf(::ProductsServiceImpl).bind<ProductsService>()
    singleOf(::PostgresProductRepository).bind<ProductsRepository>()
}