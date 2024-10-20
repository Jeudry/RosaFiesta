package com.example.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.database.Converters
import com.example.database.dao.ProductDao
import com.example.database.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class
    ],
    version = 2
)
@TypeConverters(Converters::class)

abstract class ProductDatabase: RoomDatabase() {
    abstract val productDao: ProductDao
}