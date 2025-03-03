package com.example.core.services

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.data.models.SaltedHash
import com.example.data.models.TokenClaim
import com.example.data.services.auth.SHA256HashingService
import com.example.data.services.users.UserService
import com.example.domain.models.User
import com.example.data.repositories.RefreshTokenRepositoryImpl
import com.example.domain.repository.UserRepository
import com.example.presentation.requests.AuthRequest
import com.example.presentation.responses.AuthResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val jwtService: JwtServiceImpl,
    private val refreshTokenRepositoryImpl: RefreshTokenRepositoryImpl,
    private val hashingService: SHA256HashingService,
): UserService {
    override suspend fun authenticate(loginRequest: AuthRequest): AuthResponse? {
        val user = userRepository.retrieveByUserName(loginRequest.username) ?: return null

        val isValidPassword = hashingService.verify(loginRequest.password, SaltedHash(user.password, user.salt))
        return if(isValidPassword){
            val claim = TokenClaim(
                name = "userName",
                value = user.userName
            )
            val token = jwtService.createAccessToken(claim)
            val refreshToken = jwtService.createRefreshToken(claim)
            refreshTokenRepositoryImpl.saveToken(refreshToken, user.userName)

            AuthResponse(token, refreshToken)
        } else null
    }

    override suspend fun refreshToken(accessToken: String): String? {
        val decodedRefreshToken = verifyRefreshToken(accessToken)
        val persistedUserName = refreshTokenRepositoryImpl.findUsernameByToken(accessToken)

        return if(decodedRefreshToken != null && persistedUserName != null) {
            val foundUser = userRepository.retrieveByUserName(persistedUserName)
            val usernameFromRefreshToken = decodedRefreshToken.getClaim("username").asString()

            if(foundUser?.userName == usernameFromRefreshToken) {
                val claim = TokenClaim(
                    name = "userName",
                    value = usernameFromRefreshToken
                )
                jwtService.createAccessToken(claim)
            } else null
        } else null
    }

    private suspend fun verifyRefreshToken(accessToken: String): DecodedJWT? {
        val decodedJWT = decodedJWT(accessToken)

        return decodedJWT?.let {
            val audienceMatches = jwtService.audienceMatches(it.audience.first())

            if(audienceMatches)
                decodedJWT
            else null
        }
    }

    private fun decodedJWT(token: String) = try {
        jwtService.jwtVerifier.verify(token)
    } catch (e: Exception) {
        null
    }

    override suspend fun retrieveAll(): List<User> =
        userRepository.retrieveAll()

    override suspend fun retrieveById(id: UUID): User? =
        userRepository.retrieveById(id = id)

    override suspend fun retrieveByUserName(name: String): User? =
        userRepository.retrieveByUserName(name = name)

    override suspend fun add(user: User): String? {
        val userExists = userRepository.retrieveByUserName(user.userName) != null
        if(userExists) return null
        val id =userRepository.add(user)

        return id
    }

    override suspend fun remove(id: UUID): Boolean =
        userRepository.remove(id = id)



    object Users : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val age = integer("age")

        override val primaryKey = PrimaryKey(id)
    }
    /*
        init {
            transaction(database) {
                SchemaUtils.create(Users)
            }
        }

        suspend fun create(user: ExposedUser): Int = dbQuery {
            Users.insert {
                it[name] = user.name
                it[age] = user.age
            }[Users.id]
        }

        suspend fun read(id: Int): ExposedUser? {
            return dbQuery {
                Users.selectAll()
                    .where { Users.id eq id }
                    .map { ExposedUser(it[Users.name], it[Users.age]) }
                    .singleOrNull()
            }
        }

        suspend fun update(id: Int, user: ExposedUser) {
            dbQuery {
                Users.update({ Users.id eq id }) {
                    it[name] = user.name
                    it[age] = user.age
                }
            }
        }

        suspend fun delete(id: Int) {
            dbQuery {
                Users.deleteWhere { Users.id.eq(id) }
            }
        }

        private suspend fun <T> dbQuery(block: suspend () -> T): T =
            newSuspendedTransaction(Dispatchers.IO) { block() }*/
}