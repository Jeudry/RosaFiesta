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
}
