package com.example.data.services.products

import com.example.domain.models.Product
import java.util.UUID

interface ProductsService {
    suspend fun retrieveAll(): List<Product>
    suspend fun retrieveById(id: UUID): Product?
    suspend fun retrieveByName(name: String): Product?
    suspend fun addProduct(product: Product)
    suspend fun removeProduct(id: UUID): Boolean
}