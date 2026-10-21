package com.jeudry.rosafiesta.data

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement

public actual class DriverFactory {
    public actual fun createDriver(): SqlDriver = WasmInMemorySqlDriver()
}

public actual fun createDefaultSqlDriverOrNull(): SqlDriver? = null

private class WasmInMemorySqlDriver : SqlDriver {
    private var currentTransaction: Transacter.Transaction? = null

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> = mapper(EmptySqlCursor)

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> = QueryResult.Value(0L)

    override fun newTransaction(): QueryResult<Transacter.Transaction> {
        val enclosing = currentTransaction
        val tx = object : Transacter.Transaction() {
            override val enclosingTransaction: Transacter.Transaction? = enclosing

            override fun endTransaction(successful: Boolean): QueryResult<Unit> {
                currentTransaction = enclosing
                return QueryResult.Unit
            }
        }
        currentTransaction = tx
        return QueryResult.Value(tx)
    }

    override fun currentTransaction(): Transacter.Transaction? = currentTransaction

    override fun addListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) = Unit

    override fun removeListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) = Unit

    override fun notifyListeners(vararg queryKeys: String) = Unit

    override fun close() = Unit
}

private object EmptySqlCursor : SqlCursor {
    override fun next(): QueryResult<Boolean> = QueryResult.Value(false)
    override fun getString(index: Int): String? = null
    override fun getLong(index: Int): Long? = null
    override fun getBytes(index: Int): ByteArray? = null
    override fun getDouble(index: Int): Double? = null
    override fun getBoolean(index: Int): Boolean? = null
}
