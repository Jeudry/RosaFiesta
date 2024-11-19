package com.example.data.models

import com.example.database.models.ProductTable
import com.example.domain.models.Product
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ProductDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProductDAO>(ProductTable)

    var name by ProductTable.name
    var description by ProductTable.description
    var price by ProductTable.price
    var rentPrice by ProductTable.rentPrice
    var created by ProductTable.created
    var imageUrl by ProductTable.imageUrl
    var stock by ProductTable.stock
}

fun ProductDAO.toModel() = Product(
    id = id.value,
    name = name,
    description = description,
    price = price,
    rentPrice = rentPrice,
    created = created,
    imageUrl = imageUrl,
    stock = stock
)