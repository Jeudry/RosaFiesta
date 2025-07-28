package com.example.products.data.di

import com.example.core.domain.product.ProductsDataSource
import com.example.products.data.repository.ProductsRepositoryImpl
import com.example.products.domain.repositories.ProductsRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

val productsDataModule = module {
    single<ProductsRepository> {
        ProductsRepositoryImpl(
            productsDataSource = get<ProductsDataSource>(),
            httpClient = get<HttpClient>()
        )
    }
}
