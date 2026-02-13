package com.example.core.domain.product

import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.Result
import kotlinx.coroutines.flow.Flow
import java.util.*

typealias ProductId = UUID

interface ProductsDataSource {
    fun getProducts(): Flow<List<Product>>
    suspend fun upsertProduct(product: Product): Result<ProductId, DataError.Local>
    suspend fun deleteProduct(productId: ProductId): Result<ProductId, DataError.Local>
    suspend fun getProduct(productId: ProductId): Result<Product, DataError.Local>
}