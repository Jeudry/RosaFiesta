package com.example.products.data.repository

import com.example.core.domain.SessionStorage
import com.example.core.domain.product.Product
import com.example.core.domain.product.ProductId
import com.example.core.domain.product.ProductsDataSource
import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.EmptyResult
import com.example.core.domain.utils.asEmptyDataResult
import com.example.products.domain.repositories.ProductsRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.core.domain.utils.Result as ResultResponse

class ProductsRepositoryImpl(
    private val productsDataSource: ProductsDataSource
): ProductsRepository {
    override fun getProducts(): Flow<List<Product>> {
        return productsDataSource.getProducts()
    }

    override suspend fun upsertProduct(
        product: Product
    ): EmptyResult<DataError> {
        return productsDataSource.upsertProduct(product).asEmptyDataResult()
    }

    override suspend fun deleteProduct(productId: ProductId) {
        productsDataSource.deleteProduct(productId)
    }
}