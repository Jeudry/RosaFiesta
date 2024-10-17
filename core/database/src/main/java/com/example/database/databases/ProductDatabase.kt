package com.example.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.database.dao.ProductDao
import com.example.database.dao.RunDao
import com.example.database.entity.ProductEntity
import com.plcoding.core.database.entity.RunEntity

@Database(
    entities = [
        ProductEntity::class
    ],
    version = 1
)
abstract class ProductDatabase: RoomDatabase() {
    abstract val productDao: ProductDao
}