package com.example.domain.repository

import com.example.domain.models.Product
import java.util.UUID

interface ProductsRepository {
    suspend fun retrieveAll(): List<Product>
    suspend fun retrieveById(id: UUID): Product?
    suspend fun retrieveByName(name: String): Product?
    suspend fun addProduct(product: Product)
    suspend fun removeProduct(id: UUID): Boolean
}