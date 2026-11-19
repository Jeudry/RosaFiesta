@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

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

@JsFun("() => { try { return typeof window !== 'undefined' && !!window.localStorage; } catch (err) { return false; } }")
private external fun jsHasSnapshotLocalStorage(): Boolean

@JsFun("(key) => { try { return window.localStorage.getItem(key); } catch (err) { return null; } }")
private external fun jsSnapshotLocalStorageGet(key: String): String?

@JsFun("(key, value) => { try { window.localStorage.setItem(key, value); return true; } catch (err) { return false; } }")
private external fun jsSnapshotLocalStorageSet(key: String, value: String): Boolean

@JsFun("(key) => { try { window.localStorage.removeItem(key); } catch (err) {} }")
private external fun jsSnapshotLocalStorageRemove(key: String)

@JsFun(
    """() => {
        try {
            if (typeof window === 'undefined' || !window.indexedDB) return;
            const req = window.indexedDB.open('rosafiesta_web_db', 1);
            req.onupgradeneeded = () => {
                const db = req.result;
                if (!db.objectStoreNames.contains('snapshots')) {
                    db.createObjectStore('snapshots', { keyPath: 'key' });
                }
            };
            req.onsuccess = () => {
                const db = req.result;
                if (!db.objectStoreNames.contains('snapshots')) return;
                const tx = db.transaction('snapshots', 'readonly');
                const store = tx.objectStore('snapshots');
                const allReq = store.getAll();
                allReq.onsuccess = () => {
                    const rows = allReq.result || [];
                    for (const row of rows) {
                        if (row && row.key && typeof row.value === 'string' && !window.localStorage.getItem(row.key)) {
                            window.localStorage.setItem(row.key, row.value);
                        }
                    }
                };
            };
        } catch (err) {}
    }""",
)
private external fun jsHydrateIndexedDbSnapshots()

@JsFun(
    """(key, value) => {
        try {
            if (typeof window === 'undefined' || !window.indexedDB) return;
            const req = window.indexedDB.open('rosafiesta_web_db', 1);
            req.onupgradeneeded = () => {
                const db = req.result;
                if (!db.objectStoreNames.contains('snapshots')) {
                    db.createObjectStore('snapshots', { keyPath: 'key' });
                }
            };
            req.onsuccess = () => {
                const db = req.result;
                if (!db.objectStoreNames.contains('snapshots')) return;
                const tx = db.transaction('snapshots', 'readwrite');
                tx.objectStore('snapshots').put({ key: key, value: value });
            };
        } catch (err) {}
    }""",
)
private external fun jsMirrorSnapshotToIndexedDb(key: String, value: String)

@JsFun(
    """(key) => {
        try {
            if (typeof window === 'undefined' || !window.indexedDB) return;
            const req = window.indexedDB.open('rosafiesta_web_db', 1);
            req.onsuccess = () => {
                const db = req.result;
                if (!db.objectStoreNames.contains('snapshots')) return;
                const tx = db.transaction('snapshots', 'readwrite');
                tx.objectStore('snapshots').delete(key);
            };
        } catch (err) {}
    }""",
)
private external fun jsDeleteSnapshotFromIndexedDb(key: String)

private class WasmLocalStorageSnapshotCache : LocalSnapshotCache {
    init {
        jsHydrateIndexedDbSnapshots()
    }

    override fun readSnapshot(key: String): String? {
        if (!jsHasSnapshotLocalStorage()) return null
        return jsSnapshotLocalStorageGet(key)
    }

    override fun writeSnapshot(key: String, value: String) {
        if (jsHasSnapshotLocalStorage()) {
            jsSnapshotLocalStorageSet(key, value)
        }
        jsMirrorSnapshotToIndexedDb(key, value)
    }

    override fun removeSnapshot(key: String) {
        if (jsHasSnapshotLocalStorage()) {
            jsSnapshotLocalStorageRemove(key)
        }
        jsDeleteSnapshotFromIndexedDb(key)
    }
}

public actual fun createDefaultLocalSnapshotCacheOrNull(): LocalSnapshotCache? = if (jsHasSnapshotLocalStorage()) WasmLocalStorageSnapshotCache() else null

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
