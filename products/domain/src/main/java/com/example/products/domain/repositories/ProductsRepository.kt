package com.example.products.domain.repositories

import com.example.core.domain.product.Product
import com.example.core.domain.product.ProductId
import com.example.core.domain.run.Run
import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.EmptyResult
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
  fun getProducts(): Flow<List<Product>>
  suspend fun upsertProduct(product: Product):EmptyResult<DataError>
  suspend fun deleteProduct(productId: ProductId)
}