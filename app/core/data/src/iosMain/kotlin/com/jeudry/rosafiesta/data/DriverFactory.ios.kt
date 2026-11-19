package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase

public actual class DriverFactory(
    private val databaseName: String = "rosafiesta.db",
) {
    public actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(
            schema = RosaFiestaDatabase.Schema,
            name = databaseName,
        )
        RosaFiestaSchemaUpgrade.applySafeColumnUpgrades(driver)
        return driver
    }
}

public actual fun createDefaultSqlDriverOrNull(): SqlDriver? = runCatching { DriverFactory().createDriver() }.getOrNull()

public actual fun createDefaultLocalSnapshotCacheOrNull(): LocalSnapshotCache? = null
