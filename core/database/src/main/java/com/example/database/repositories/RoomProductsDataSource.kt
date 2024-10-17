package com.example.database.repositories

import android.database.sqlite.SQLiteFullException
import com.example.core.domain.product.Product
import com.example.core.domain.product.ProductId
import com.example.core.domain.product.ProductsDataSource
import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.Result
import com.example.database.dao.ProductDao
import com.example.database.mappers.toProduct
import com.example.database.mappers.toProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.core.domain.utils.Result as ResultResponse

class RoomProductsDataSource(
    private val productDao: ProductDao
): ProductsDataSource {
    override fun getProducts(): Flow<List<Product>> {
        return productDao.getProducts()
            .map { productEntities ->
                productEntities.map { it.toProduct() }
            }
    }

    override suspend fun upsertProduct(product: Product): Result<ProductId, DataError.Local> {
        return try {
            val entity = product.toProductEntity()
            productDao.upsertProduct(entity)
            ResultResponse.Success(entity.id)
        } catch (e: SQLiteFullException) {
            ResultResponse.Error(DataError.Local.DISK_FULL)
        }
    }

    override suspend fun deleteProduct(productId: ProductId) {
        productDao.deleteProduct(productId)
    }
}