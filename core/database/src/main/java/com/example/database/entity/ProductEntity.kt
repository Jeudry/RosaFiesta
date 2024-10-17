package com.example.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.bson.types.ObjectId
import java.time.ZonedDateTime

@Entity
data class ProductEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = ObjectId().toHexString(),
    val name: String,
    val description: String?,
    val price: Double,
    val rentalPrice: Double?,
    val imageUrl: String?,
    val stock: Int,
    val created: String
)