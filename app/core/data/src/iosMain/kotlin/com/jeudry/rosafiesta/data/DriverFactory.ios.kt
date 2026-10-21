package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase

public actual class DriverFactory(
    private val databaseName: String = "rosafiesta.db",
) {
    public actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = RosaFiestaDatabase.Schema,
        name = databaseName,
    )
}
