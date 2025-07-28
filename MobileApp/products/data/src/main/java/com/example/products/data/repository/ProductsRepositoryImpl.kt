package com.example.products.data.repository

import com.example.core.data.models.Envelope
import com.example.core.data.networking.get
import com.example.core.data.networking.post
import com.example.core.domain.product.Product
import com.example.core.domain.product.ProductId
import com.example.core.domain.product.ProductsDataSource
import com.example.core.domain.utils.*
import com.example.products.data.mappers.toProduct
import com.example.products.data.mappers.toRequest
import com.example.products.data.models.ProductRequest
import com.example.products.data.models.ProductResponse
import com.example.products.domain.repositories.ProductsRepository
import io.ktor.client.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
class ProductsRepositoryImpl(
    private val productsDataSource: ProductsDataSource,
    private val httpClient: HttpClient
): ProductsRepository {
    override suspend fun getProducts(): Flow<List<Product>> = kotlinx.coroutines.flow.flow {
        val result: Result<Envelope<List<ProductResponse>>, DataError> =
            httpClient.get<Envelope<List<ProductResponse>>>("/v1/products")

        result.onSuccess { resultData ->
            val products = resultData.data.map { productResponse ->
                productResponse.toProduct()
            }
            emit(products)
        }.onError { error ->
            throw Exception("Error fetching products: $error")
        }
    }

    override suspend fun upsertProduct(
        product: Product
    ): EmptyResult<DataError> {
        val result = httpClient.post<ProductRequest, Unit>(
            route = "/v1/products",
            body = product.toRequest()
        )

        if (result is Result.Success) {
            productsDataSource.upsertProduct(product)
        }

        return result.asEmptyDataResult()
    }

    override suspend fun deleteProduct(productId: ProductId): Result<ProductId, DataError.Local>  {
        return productsDataSource.deleteProduct(productId)
    }

    override suspend fun getProduct(productId: ProductId): Result<Product, DataError.Local> {
        return productsDataSource.getProduct(productId)
    }
}