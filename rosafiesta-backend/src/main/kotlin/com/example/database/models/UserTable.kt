package com.example.database.models

import com.example.database.models.ProductTable.nullable
import org.jetbrains.exposed.dao.id.UUIDTable

object UserTable : UUIDTable("products") {
    val userName = varchar("userName", 256)
    val firstName = varchar("firstName", 256)
    val lastName = varchar("lastName", 256)
    val email = varchar("email", 256)
    val phoneNumber = varchar("phoneNumber", 256)
    val bornDate = varchar("bornDate", 256)
    val created = varchar("created", 50)
    val avatar = varchar("avatar", 5000).nullable()
    val password = varchar("password", 512)
    val salt = varchar("salt", 512)
}