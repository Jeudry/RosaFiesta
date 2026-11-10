package com.jeudry.rosafiesta.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CanonicalSampleDataTest {
    @Test
    fun `verifica que los precios por dia del catalogo coincidan con la Seccion 6`() {
        val byCode = CanonicalSampleData.articles.associateBy { it.code }
        assertEquals(4, byCode.getValue("RF-001").pricePerDayDop)
        assertEquals(20, byCode.getValue("RF-002").pricePerDayDop)
        assertEquals(25, byCode.getValue("RF-003").pricePerDayDop)
        assertEquals(35, byCode.getValue("RF-004").pricePerDayDop)
        assertEquals(95, byCode.getValue("RF-005").pricePerDayDop)
        assertEquals(150, byCode.getValue("RF-006").pricePerDayDop)
    }

    @Test
    fun `verifica que los eventos canonicos y saldos coincidan con la Seccion 6`() {
        val boda = CanonicalSampleData.events.first { it.id == "evt-boda-real" }
        assertEquals("Camila Rodríguez", boda.clientName)
        assertEquals(120, boda.guests)
        assertEquals(145, boda.articleCount)
        assertEquals(4500, boda.totalDop)
        assertEquals(1350, boda.paidDepositDop)
        assertEquals(3150, boda.balanceDop)

        val quince = CanonicalSampleData.events.first { it.id == "evt-quince-valentina" }
        assertEquals(6200, quince.totalDop)
        assertEquals(1860, quince.paidDepositDop)
        assertEquals(4340, quince.balanceDop)

        val mateo = CanonicalSampleData.events.first { it.id == "evt-cumple-mateo" }
        assertEquals(405, mateo.totalDop)

        val grad = CanonicalSampleData.events.first { it.id == "evt-grad-promo" }
        assertEquals(2140, grad.totalDop)

        val gala = CanonicalSampleData.events.first { it.id == "evt-gala-caribe" }
        assertEquals(8500, gala.totalDop)
    }

    @Test
    fun `verifica que los indicadores del negocio coincidan con la Seccion 6`() {
        val metrics = CanonicalSampleData.businessMetrics
        assertEquals(12450, metrics.octoberRevenueDop)
        assertEquals(7724, metrics.accountsReceivableDop)
        assertEquals(4, metrics.activeEventsCount)
        assertEquals(2, metrics.newRequestsCount)
        assertTrue(CanonicalSampleData.staffMembers.size == 4)
    }

    @Test
    fun `balloon products have distinct balloon ids and exact direct sale prices`() {
        val balloons = CanonicalSampleData.balloonProducts
        assertEquals(4, balloons.size)
        assertTrue(balloons.all { balloon -> balloon.id.startsWith("balloon-") })
        assertTrue(balloons.none { balloon -> balloon.id == "art-arco-rosa" })
        val pricesById = balloons.associate { balloon -> balloon.id to balloon.priceDop }
        assertEquals(28, pricesById.getValue("balloon-bouquet-helio"))
        assertEquals(22, pricesById.getValue("balloon-numero-metalico"))
        assertEquals(48, pricesById.getValue("balloon-burbuja-confeti"))
        assertEquals(65, pricesById.getValue("balloon-kit-guirnalda"))
    }

    @Test
    fun `refund requests reviews settings and chat threads cover canonical submodule seeds`() {
        val refundClients = CanonicalSampleData.refundRequests.map { refund -> refund.clientName }.toSet()
        assertTrue("Rafael Soto" in refundClients)
        assertTrue("Gabriela Núñez" in refundClients)

        val reviewAuthors = CanonicalSampleData.reviews.associateBy { review -> review.authorName }
        assertEquals(true, reviewAuthors.getValue("Camila Rodríguez").verified)
        assertEquals(true, reviewAuthors.getValue("Patricia Lora").verified)
        assertEquals(false, reviewAuthors.getValue("Ricardo Méndez").verified)

        val settings = CanonicalSampleData.businessSettings
        assertEquals("1-31-84920-2", settings.rnc)
        assertEquals("Av. Winston Churchill 1099, Piantini, Santo Domingo", settings.address)
        assertEquals("+18095550100", settings.phone)
        assertEquals("+18095550199", settings.whatsapp)
        assertEquals(30, settings.depositPercent)
        assertEquals(4, settings.zones.size)

        val threadContacts = CanonicalSampleData.chatThreads.map { thread -> thread.contactName }
        assertEquals(6, threadContacts.size)
        assertTrue(threadContacts.any { name -> name.contains("Camila") })
        assertTrue(threadContacts.any { name -> name.contains("Laura") })
        assertTrue(threadContacts.any { name -> name.contains("Valeria") })
        assertTrue(threadContacts.any { name -> name.contains("Sofía") })
        assertTrue(threadContacts.any { name -> name.contains("Andrés") })
        assertTrue(threadContacts.any { name -> name.contains("María") })

        val nearDays = DomainClockFormatter.computeDaysUntilEvent("25 nov 2026")
        val farDays = DomainClockFormatter.computeDaysUntilEvent("28 dic 2026")
        assertTrue(farDays > nearDays)
    }
}
