package com.example.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.database.dao.RunDao
import com.plcoding.core.database.entity.RunEntity

@Database(
    entities = [
        RunEntity::class
    ],
    version = 1
)
abstract class RunDatabase : RoomDatabase() {
    abstract val runDao: RunDao
}