package com.example.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class ProductEntity(
    @PrimaryKey(autoGenerate = false)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String?,
    val price: Double,
    val rentalPrice: Double?,
    val color: Long,
    val size: Double,
    val imageUrl: String?,
    val stock: Int = 0,
    val created: String
)