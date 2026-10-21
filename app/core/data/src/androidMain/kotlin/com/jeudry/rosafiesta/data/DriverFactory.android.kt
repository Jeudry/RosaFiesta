package com.jeudry.rosafiesta.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase

public actual class DriverFactory(
    private val context: Context,
    private val databaseName: String = "rosafiesta.db",
) {
    public actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = RosaFiestaDatabase.Schema,
        context = context,
        name = databaseName,
    )
}
