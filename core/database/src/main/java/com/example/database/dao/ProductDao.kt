package com.example.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.core.domain.product.ProductId
import com.example.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM productentity ORDER BY created DESC")
    fun getProducts(): Flow<List<ProductEntity>>

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Query("DELETE FROM productentity WHERE id=:productId")
    suspend fun deleteProduct(productId: ProductId)
}