package com.example.data.repositories

import com.example.domain.repository.DatabaseService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.h2.jdbc.JdbcConnection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.resource.FileSystemResourceAccessor
import java.io.File


class DatabaseImpl(
    private val ioContext: CoroutineContext = Dispatchers.IO,
    private val environment: ApplicationEnvironment
) : DatabaseService {

    override suspend fun initialize() {
        val databaseName = environment.config.property("postgres.database").getString()
        val baseUrl = environment.config.property("postgres.baseUrl").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        createDatabaseIfNotExists(baseUrl, databaseName, user, password)

        val dataSource = postgresDataSource()

        Database.connect(dataSource)

        migrateDatabaseSchema(dataSource)
    }

    // Move all db queries to io thread and wrap within a DB transaction
    override suspend fun <T> dbQuery(block: () -> T): T = withContext(ioContext) {
        transaction { block() }
    }

    private fun postgresDataSource(): HikariDataSource {
        val database = environment.config.property("postgres.database").getString()
        val baseUrl = environment.config.property("postgres.baseUrl").getString()
        val user = environment.config.property("postgres.user").getString()
        val pass = environment.config.property("postgres.password").getString()

        return HikariDataSource(
            HikariConfig().apply {
                username = user
                password = pass
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = baseUrl + database
                maximumPoolSize = 3
                isAutoCommit = true
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
        )
    }

    fun migrateDatabaseSchema(datasource: HikariDataSource){
        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(
                liquibase.database.jvm.JdbcConnection(datasource.connection)
            )
        val liquibase = Liquibase(
            "/db/changelog.sql",
            FileSystemResourceAccessor(File("src/main/resources")),
            database
        )
        liquibase.update("")
        liquibase.close()
    }

    private fun migrate(dataSource: DataSource) {
        try {
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("filesystem:src/main/kotlin/com/example/database/migration")
                .load()
            flyway.migrate()
        } catch (e: FlywayException) {
            e.printStackTrace()
        }
    }

    private fun createDatabaseIfNotExists(baseUrl: String, databaseName: String, user: String, password: String) {
        val url = baseUrl+"postgres"
        val connection: Connection? = null
        try {
            DriverManager.getConnection(url, user, password).use { conn ->
                val statement = conn.createStatement()
                val resultSet = statement.executeQuery("SELECT 1 FROM pg_database WHERE datname = '$databaseName'")
                if (!resultSet.next()) {
                    statement.executeUpdate("CREATE DATABASE $databaseName")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
    }
}