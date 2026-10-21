package com.jeudry.rosafiesta.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosaFiestaRepositoryTest {
    @Test
    fun `alterna pasos del checklist de montaje y sincroniza la cola offline`() {
        val repo = DefaultRosaFiestaRepository()
        val before = repo.checklist.value.first { it.id == "chk-5" }
        assertEquals(false, before.completed)

        repo.toggleChecklistStep("chk-5")
        val after = repo.checklist.value.first { it.id == "chk-5" }
        assertTrue(after.completed)

        repo.syncOfflineQueue()
        assertTrue(repo.offlineQueue.value.all { it.synced })
    }

    @Test
    fun `ajusta el stock de un articulo sin permitir cantidades negativas`() {
        val repo = DefaultRosaFiestaRepository()
        repo.adjustArticleStock("art-arco-rosa", 2)
        val updated = repo.articles.value.first { it.id == "art-arco-rosa" }
        assertEquals(12, updated.totalStock)
        assertEquals(7, updated.availableOct23)
    }
}
