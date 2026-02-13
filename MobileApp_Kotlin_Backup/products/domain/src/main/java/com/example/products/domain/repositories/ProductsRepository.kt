package com.example.products.domain.repositories

import com.example.core.domain.product.Product
import com.example.core.domain.product.ProductId
import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.EmptyResult
import com.example.core.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
  suspend fun getProducts(): Flow<List<Product>>
  suspend fun upsertProduct(product: Product):EmptyResult<DataError>
  suspend fun deleteProduct(productId: ProductId): Result<ProductId, DataError.Local>
  suspend fun getProduct(productId: ProductId): Result<Product, DataError.Local>
}