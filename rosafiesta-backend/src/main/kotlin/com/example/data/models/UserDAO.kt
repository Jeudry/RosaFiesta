package com.example.data.models

import com.example.database.models.ProductTable.description
import com.example.database.models.ProductTable.imageUrl
import com.example.database.models.ProductTable.price
import com.example.database.models.ProductTable.rentPrice
import com.example.database.models.ProductTable.stock
import com.example.database.models.UserTable
import com.example.database.models.UserTable.nullable
import com.example.database.models.UserTable.password
import com.example.database.models.UserTable.userName
import com.example.database.models.UserTable.varchar
import com.example.domain.models.Product
import com.example.domain.models.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var userName by UserTable.userName
    var firstName by UserTable.firstName
    var lastName by UserTable.lastName
    var email by UserTable.email
    var phoneNumber by UserTable.phoneNumber
    var bornDate by UserTable.bornDate
    var created by UserTable.created
    var avatar: String? by UserTable.avatar
    var password by UserTable.password
    var salt by UserTable.salt
}

fun UserDAO.toModel() = User(
    id = id.value,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phoneNumber = phoneNumber,
    bornDate = bornDate,
    created = created,
    avatar = avatar,
    password = password,
    userName = userName,
    salt = salt
)