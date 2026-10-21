package com.jeudry.rosafiesta.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase

private var androidApplicationContext: Context? = null
private var androidDatabaseName: String = "rosafiesta.db"

internal fun currentAndroidContext(): Context? = androidApplicationContext

/**
 * Inicializa el contexto de aplicación de Android para crear el [SqlDriver] persistente en disco.
 */
public fun initAndroidDriverContext(
    context: Context,
    databaseName: String = "rosafiesta.db",
) {
    androidApplicationContext = context.applicationContext
    androidDatabaseName = databaseName
}

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

public actual fun createDefaultSqlDriverOrNull(): SqlDriver? {
    val ctx = androidApplicationContext ?: return null
    return DriverFactory(context = ctx, databaseName = androidDatabaseName).createDriver()
}
