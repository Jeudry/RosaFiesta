package com.example.core.services

import com.example.data.services.products.ProductsService
import com.example.domain.models.Product
import com.example.domain.repository.ProductsRepository
import java.util.UUID

class ProductsServiceImpl(
    private val repository: ProductsRepository
): ProductsService {
    override suspend fun retrieveAll(): List<Product> {
        return repository.retrieveAll()
    }

    override suspend fun retrieveById(id: UUID): Product? {
        return repository.retrieveById(id)
    }

    override suspend fun retrieveByName(name: String): Product? {
        return repository.retrieveByName(name)
    }

    override suspend fun addProduct(product: Product) {
        repository.addProduct(product)
    }

    override suspend fun removeProduct(id: UUID): Boolean {
        return repository.removeProduct(id)
    }

}