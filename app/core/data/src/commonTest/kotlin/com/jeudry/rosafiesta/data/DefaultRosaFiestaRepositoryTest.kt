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

    @Test
    fun `gestiona autenticacion roles favoritos eventos pagos y chat en memoria`() {
        val repo = DefaultRosaFiestaRepository()
        assertEquals(false, repo.login("invalido", "123"))
        assertTrue(repo.login("yuli@rosafiesta.do", "rosa2026"))
        assertEquals("staff_yuli", repo.activeRole.value)

        repo.switchRole("admin")
        assertEquals("admin", repo.activeRole.value)

        repo.toggleFavorite("art-mesa-helados")
        assertTrue("art-mesa-helados" in repo.favorites.value)

        repo.toggleStaffPaymentAccess("Yuli")
        assertTrue(repo.staffMembers.value.first { it.name == "Yuli" }.canViewPayments)

        repo.createArticle("RF-088", "Silla Imperial", "Mobiliario", 120, 40)
        assertTrue(repo.articles.value.any { it.code == "RF-088" })

        repo.createEvent("Boda Jardín", "Elena Gómez", "12 dic 2026", "Casa de Campo", 120)
        assertTrue(repo.events.value.any { it.title == "Boda Jardín" })

        val change = repo.changeEventDate("evt-boda-real", "28 oct 2026")
        assertTrue(change.allowed)

        val payment = repo.registerPayment("evt-boda-real", 3500, "BHD León", "BHD-9988")
        assertEquals(3500, payment?.amountDop)

        val refund = repo.cancelEvent("evt-quince-valentina")
        assertEquals(90, refund.refundPercent)

        repo.sendChatMessage("Camila", "Hola equipo")
        assertTrue(repo.chatMessages.value.last().contains("Hola equipo"))
    }
}
