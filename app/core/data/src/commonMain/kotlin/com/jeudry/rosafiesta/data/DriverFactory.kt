package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Fábrica multiplataforma para construir el [SqlDriver] de [com.jeudry.rosafiesta.data.db.RosaFiestaDatabase].
 */
public expect class DriverFactory {
    public fun createDriver(): SqlDriver
}

/**
 * Crea un [SqlDriver] por defecto cuando la plataforma no requiere un contexto externo.
 */
public expect fun createDefaultSqlDriverOrNull(): SqlDriver?

/**
 * Key-value snapshot persistence abstraction used on browser targets (`wasmJs`) where SQLite is unavailable.
 */
public interface LocalSnapshotCache {
    public fun readSnapshot(key: String): String?

    public fun writeSnapshot(key: String, value: String)

    public fun removeSnapshot(key: String)
}

/**
 * Creates a platform [LocalSnapshotCache] on targets that use [DefaultRosaFiestaRepository] instead of SQLite.
 */
public expect fun createDefaultLocalSnapshotCacheOrNull(): LocalSnapshotCache?
