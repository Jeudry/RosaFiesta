package com.example.data.repositories

import com.example.data.db.suspendTransaction
import com.example.data.models.ProductDAO
import com.example.data.models.toModel
import com.example.database.models.ProductTable
import com.example.domain.models.Product
import com.example.domain.repository.ProductsRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.UUID

class PostgresProductRepository: ProductsRepository {
    override suspend fun retrieveAll(): List<Product> = suspendTransaction {
        ProductDAO.all().map{ it.toModel() }
    }

    override suspend fun retrieveById(id: UUID): Product? = suspendTransaction {
       ProductDAO.find { (ProductTable.id eq id) }
           .limit(1)
           .map{ it.toModel() }
           .firstOrNull()
    }

    override suspend fun retrieveByName(name: String): Product? = suspendTransaction{
        ProductDAO.find { (ProductTable.name eq name) }
            .limit(1)
            .map{ it.toModel() }
            .firstOrNull()
    }

    override suspend fun addProduct(product: Product): Unit = suspendTransaction {
        ProductDAO.new {
            name = product.name
            description = product.description
            price = product.price
            rentPrice = product.rentPrice
            created = product.created
            imageUrl = product.imageUrl
            stock = product.stock
        }
    }

    override suspend fun removeProduct(id: UUID) = suspendTransaction {
        val rowsDeleted = ProductTable.deleteWhere {
            ProductTable.id eq id
        }
        rowsDeleted == 1
    }
}