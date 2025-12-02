package com.adventist.adventist.infra.database.repositories

import com.adventist.adventist.domain.types.UserId
import com.adventist.adventist.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository


interface UserRepository: JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email:String, username: String): UserEntity?
}