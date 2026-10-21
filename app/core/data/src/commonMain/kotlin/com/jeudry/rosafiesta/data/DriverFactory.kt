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
