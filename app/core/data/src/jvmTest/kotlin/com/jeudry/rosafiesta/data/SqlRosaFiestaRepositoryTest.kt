package com.jeudry.rosafiesta.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SqlRosaFiestaRepositoryTest {
    @Test
    fun `persiste articulos checklist y cola offline en SQLite fuera del hilo principal`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RosaFiestaDatabase.Schema.create(driver)

        val repo = SqlRosaFiestaRepository(driver = driver, ioDispatcher = testDispatcher)

        assertEquals(6, repo.articles.value.size)
        assertEquals(6, repo.checklist.value.size)
        assertEquals(3, repo.offlineQueue.value.size)

        val initialStep = repo.checklist.value.first { it.id == "chk-5" }
        assertFalse(initialStep.completed)

        repo.toggleChecklistStepAsync("chk-5")
        val updatedStep = repo.checklist.value.first { it.id == "chk-5" }
        assertTrue(updatedStep.completed)

        repo.adjustArticleStockAsync("art-arco-rosa", 3)
        val updatedArco = repo.articles.value.first { it.id == "art-arco-rosa" }
        assertEquals(13, updatedArco.totalStock)
        assertEquals(8, updatedArco.availableOct23)

        repo.syncOfflineQueueAsync()
        assertTrue(repo.offlineQueue.value.all { it.synced })
    }
}
