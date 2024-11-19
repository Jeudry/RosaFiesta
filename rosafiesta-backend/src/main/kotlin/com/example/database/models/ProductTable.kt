package com.example.database.models

import org.jetbrains.exposed.dao.id.UUIDTable

object ProductTable : UUIDTable("products") {
    val name = varchar("name", 50)
    val description = varchar("description", 50).nullable()
    val price = double("price")
    val rentPrice = double("rentPrice").nullable()
    val created = varchar("created", 50)
    val imageUrl = varchar("imageUrl", 5000).nullable()
    val stock = integer("stock")
}