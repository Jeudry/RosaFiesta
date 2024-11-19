package com.example.data.repositories

import com.example.data.db.suspendTransaction
import com.example.data.models.ProductDAO
import com.example.data.models.UserDAO
import com.example.data.models.toModel
import com.example.database.models.ProductTable
import com.example.database.models.UserTable
import com.example.database.models.UserTable.avatar
import com.example.database.models.UserTable.bornDate
import com.example.database.models.UserTable.email
import com.example.database.models.UserTable.firstName
import com.example.database.models.UserTable.lastName
import com.example.database.models.UserTable.password
import com.example.database.models.UserTable.phoneNumber
import com.example.database.models.UserTable.userName
import com.example.domain.models.Product
import com.example.domain.models.User
import com.example.domain.repository.ProductsRepository
import com.example.domain.repository.UserRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.UUID

class PostgresUserRepository: UserRepository {
    override suspend fun retrieveAll(): List<User> = suspendTransaction {
        UserDAO.all().map{ it.toModel() }
    }


    override suspend fun retrieveById(id: UUID): User? = suspendTransaction {
        UserDAO.find { (UserTable.id eq id) }
            .limit(1)
            .map{ it.toModel() }
            .firstOrNull()
    }

    override suspend fun retrieveByUserName(name: String): User? = suspendTransaction{
        UserDAO.find { (UserTable.userName eq name) }
            .limit(1)
            .map{ it.toModel() }
            .firstOrNull()
    }

    override suspend fun add(user: User): String {
        val userDao = UserDAO.new {
            userName = user.userName
            firstName = user.firstName
            lastName = user.lastName
            email = user.email
            phoneNumber = user.phoneNumber
            bornDate = user.bornDate
            created = user.created
            avatar = user.avatar
            password = user.password
        }
        suspendTransaction {
            userDao
        }
        return userDao.id.value.toString()
    }

    override suspend fun remove(id: UUID): Boolean = suspendTransaction {
        val rowsDeleted = UserTable.deleteWhere {
            UserTable.id eq id
        }
        rowsDeleted == 1
    }
}