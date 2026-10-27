package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase
import java.io.File

public actual class DriverFactory(
    private val url: String = JdbcSqliteDriver.IN_MEMORY,
) {
    public actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(url)
        runCatching { RosaFiestaDatabase.Schema.create(driver) }
        return driver
    }
}

public actual fun createDefaultSqlDriverOrNull(): SqlDriver? = runCatching {
    val homeDir = System.getProperty("user.home").orEmpty().ifBlank { "." }
    val appDir = File(homeDir, ".rosafiesta")
    if (!appDir.exists()) {
        appDir.mkdirs()
    }
    val dbFile = File(appDir, "rosafiesta.db")
    DriverFactory(url = "jdbc:sqlite:${dbFile.absolutePath}").createDriver()
}.getOrNull()
