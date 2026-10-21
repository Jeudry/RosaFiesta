package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase

public actual class DriverFactory(
    private val url: String = JdbcSqliteDriver.IN_MEMORY,
) {
    public actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(url)
        RosaFiestaDatabase.Schema.create(driver)
        return driver
    }
}
