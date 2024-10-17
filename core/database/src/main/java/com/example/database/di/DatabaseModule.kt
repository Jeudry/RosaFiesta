package com.example.database.di

import androidx.room.Room
import com.example.core.domain.product.Product
import com.example.core.domain.product.ProductsDataSource
import com.example.database.repositories.RoomLocalRunDataSource
import com.example.database.databases.RunDatabase
import com.example.core.domain.run.LocalRunDataSource
import com.example.database.databases.ProductDatabase
import com.example.database.repositories.RoomProductsDataSource
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            RunDatabase::class.java,
            "run.db"
        ).build()
    }
    single {
        Room.databaseBuilder(
            androidApplication(),
            ProductDatabase::class.java,
            "product.db"
        ).build()
    }

    single { get<RunDatabase>().runDao }
    single { get<ProductDatabase>().productDao }

    singleOf(::RoomLocalRunDataSource).bind<LocalRunDataSource>()

    singleOf(::RoomProductsDataSource).bind<ProductsDataSource>()
}