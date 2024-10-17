package com.example.core.domain.product

import com.example.core.domain.run.Run
import com.example.core.domain.run.RunId
import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.DataError.Network
import com.example.core.domain.utils.Result
import kotlinx.coroutines.flow.Flow

typealias ProductId = String

interface ProductsDataSource {
    fun getProducts(): Flow<List<Product>>
    suspend fun upsertProduct(product: Product): Result<ProductId, DataError.Local>
    suspend fun deleteProduct(productId: ProductId)
}