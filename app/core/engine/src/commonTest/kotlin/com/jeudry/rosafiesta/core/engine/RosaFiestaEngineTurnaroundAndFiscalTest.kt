package com.jeudry.rosafiesta.core.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RosaFiestaEngineTurnaroundAndFiscalTest {
    @Test
    fun computeTurnaroundWindowParsesIsoSlashAndSpanishMonthDates() {
        val isoWindow = RosaFiestaEngine.computeTurnaroundWindow("2026-10-24")
        assertNotNull(isoWindow)
        assertEquals("2026-10-24", isoWindow.eventDateIso)
        assertEquals("2026-10-23", isoWindow.pickupDateIso)
        assertEquals("2026-10-25", isoWindow.returnDateIso)

        val slashWindow = RosaFiestaEngine.computeTurnaroundWindow("24/10/2026")
        assertEquals(isoWindow, slashWindow)

        val spanishShortWindow = RosaFiestaEngine.computeTurnaroundWindow("24 Oct 2026")
        assertEquals(isoWindow, spanishShortWindow)

        val spanishLongWindow = RosaFiestaEngine.computeTurnaroundWindow("24 de octubre de 2026")
        assertEquals(isoWindow, spanishLongWindow)
    }

    @Test
    fun computeTurnaroundWindowHandlesLeapYearsAndYearBoundaries() {
        val leapReturn = RosaFiestaEngine.computeTurnaroundWindow("2028-02-28")
        assertNotNull(leapReturn)
        assertEquals("2028-02-27", leapReturn.pickupDateIso)
        assertEquals("2028-02-29", leapReturn.returnDateIso)

        val leapPickup = RosaFiestaEngine.computeTurnaroundWindow("2028-03-01")
        assertNotNull(leapPickup)
        assertEquals("2028-02-29", leapPickup.pickupDateIso)
        assertEquals("2028-03-02", leapPickup.returnDateIso)

        val nonLeapPickup = RosaFiestaEngine.computeTurnaroundWindow("2026-03-01")
        assertNotNull(nonLeapPickup)
        assertEquals("2026-02-28", nonLeapPickup.pickupDateIso)
        assertEquals("2026-03-02", nonLeapPickup.returnDateIso)

        val newYearsDay = RosaFiestaEngine.computeTurnaroundWindow("2026-01-01")
        assertNotNull(newYearsDay)
        assertEquals("2025-12-31", newYearsDay.pickupDateIso)
        assertEquals("2026-01-02", newYearsDay.returnDateIso)

        val newYearsEve = RosaFiestaEngine.computeTurnaroundWindow("2026-12-31")
        assertNotNull(newYearsEve)
        assertEquals("2026-12-30", newYearsEve.pickupDateIso)
        assertEquals("2027-01-01", newYearsEve.returnDateIso)

        assertNull(RosaFiestaEngine.computeTurnaroundWindow("2026-02-29"))
        assertNull(RosaFiestaEngine.computeTurnaroundWindow("1900-02-29"))
        assertNotNull(RosaFiestaEngine.computeTurnaroundWindow("2000-02-29"))
        assertNull(RosaFiestaEngine.computeTurnaroundWindow("fecha invalida"))
    }

    @Test
    fun rangesOverlapDetectsTouchingAndDisjointWindows() {
        assertTrue(
            RosaFiestaEngine.rangesOverlap(
                "2026-10-23",
                "2026-10-25",
                "2026-10-25",
                "2026-10-27",
            ),
        )
        assertTrue(
            RosaFiestaEngine.rangesOverlap(
                "2026-10-23",
                "2026-10-25",
                "2026-10-24",
                "2026-10-26",
            ),
        )
        assertFalse(
            RosaFiestaEngine.rangesOverlap(
                "2026-10-23",
                "2026-10-25",
                "2026-10-26",
                "2026-10-28",
            ),
        )
        assertFalse(RosaFiestaEngine.rangesOverlap("", "2026-10-25", "2026-10-24", "2026-10-26"))
    }

    @Test
    fun computeAvailableStockForRangeSubtractsDamagedOverlappingActiveAndExcludesTargetEvent() {
        val reservations = listOf(
            DateRangeReservation(
                articleId = "art-silla-tiffany",
                eventId = "evt-1",
                quantity = 80,
                pickupDateIso = "2026-10-23",
                returnDateIso = "2026-10-25",
                status = "confirmed",
            ),
            DateRangeReservation(
                articleId = "art-silla-tiffany",
                eventId = "evt-2",
                quantity = 30,
                pickupDateIso = "2026-10-25",
                returnDateIso = "2026-10-27",
                status = "HELD",
            ),
            DateRangeReservation(
                articleId = "art-silla-tiffany",
                eventId = "evt-3",
                quantity = 50,
                pickupDateIso = "2026-10-23",
                returnDateIso = "2026-10-25",
                status = "released",
            ),
            DateRangeReservation(
                articleId = "art-silla-tiffany",
                eventId = "evt-4",
                quantity = 40,
                pickupDateIso = "2026-10-28",
                returnDateIso = "2026-10-30",
                status = "confirmed",
            ),
            DateRangeReservation(
                articleId = "art-mesa-redonda",
                eventId = "evt-5",
                quantity = 12,
                pickupDateIso = "2026-10-23",
                returnDateIso = "2026-10-25",
                status = "confirmed",
            ),
        )

        val available = RosaFiestaEngine.computeAvailableStockForRange(
            totalStock = 200,
            damagedStock = 10,
            reservations = reservations,
            articleId = "art-silla-tiffany",
            pickupDateIso = "2026-10-24",
            returnDateIso = "2026-10-26",
        )
        assertEquals(80, available)

        val availableWhenExcludingOwnEvent = RosaFiestaEngine.computeAvailableStockForRange(
            totalStock = 200,
            damagedStock = 10,
            reservations = reservations,
            articleId = "art-silla-tiffany",
            pickupDateIso = "2026-10-24",
            returnDateIso = "2026-10-26",
            excludeEventId = "evt-1",
        )
        assertEquals(160, availableWhenExcludingOwnEvent)
    }

    @Test
    fun validateFiscalTaxIdChecksB02AndB01RncOrCedula() {
        assertTrue(RosaFiestaEngine.validateFiscalTaxId("B02", ""))
        assertTrue(RosaFiestaEngine.validateFiscalTaxId("b02", "invalid"))
        assertTrue(RosaFiestaEngine.validateFiscalTaxId("B01", "1-31-84920-2"))
        assertTrue(RosaFiestaEngine.validateFiscalTaxId("B01", "001-1234567-8"))
        assertFalse(RosaFiestaEngine.validateFiscalTaxId("B01", ""))
        assertFalse(RosaFiestaEngine.validateFiscalTaxId("B01", "12345"))
        assertFalse(RosaFiestaEngine.validateFiscalTaxId("B01", "000-00000-0"))
        assertFalse(RosaFiestaEngine.validateFiscalTaxId("B04", "131849202"))
    }

    @Test
    fun computeFiscalBreakdownExtractsItbisExcludingSecurityDepositWithoutRoundingLoss() {
        val b01 = RosaFiestaEngine.computeFiscalBreakdown(
            totalDop = 13800,
            ncfType = "b01",
            rawTaxId = "1-31-84920-2",
            securityDepositDop = 2000,
        )
        assertEquals(13800, b01.totalDop)
        assertEquals(10000, b01.subtotalDop)
        assertEquals(1800, b01.itbisDop)
        assertEquals(2000, b01.securityDepositDop)
        assertEquals("B01", b01.ncfType)
        assertEquals("131849202", b01.rncDigits)
        assertEquals(b01.totalDop, b01.subtotalDop + b01.itbisDop + b01.securityDepositDop)

        val oddTotal = RosaFiestaEngine.computeFiscalBreakdown(totalDop = 10000)
        assertEquals(8475, oddTotal.subtotalDop)
        assertEquals(1525, oddTotal.itbisDop)
        assertEquals(oddTotal.totalDop, oddTotal.subtotalDop + oddTotal.itbisDop + oddTotal.securityDepositDop)
        assertEquals("B02", oddTotal.ncfType)
    }

    @Test
    fun syncSketchAndArticleCountsPreservesManualPlacementsAndTrimsAutoExcess() {
        val syncedLines = RosaFiestaEngine.syncSketchElementsToArticleCounts(
            listOf(
                SketchElementDescriptor(id = "e1", furnitureType = "MESA_REDONDA"),
                SketchElementDescriptor(id = "e2", furnitureType = "ROUND_TABLE"),
                SketchElementDescriptor(id = "e3", furnitureType = "MESA_IMPERIAL"),
                SketchElementDescriptor(id = "e4", furnitureType = "SILLA_TIFFANY", unitCount = 8),
                SketchElementDescriptor(id = "e5", furnitureType = "ARCO_GLOBOS"),
                SketchElementDescriptor(id = "e6", furnitureType = "TARIMA"),
                SketchElementDescriptor(id = "e7", furnitureType = "MESA_DULCE"),
                SketchElementDescriptor(id = "label", furnitureType = "TEXT_LABEL", label = "Entrada"),
            ),
        )
        assertEquals(6, syncedLines.size)
        assertEquals(2, syncedLines.first { line -> line.articleId == "art-mesa-redonda" }.quantity)
        assertEquals(1, syncedLines.first { line -> line.articleId == "art-mesa-plegable" }.quantity)
        assertEquals(8, syncedLines.first { line -> line.articleId == "art-silla-tiffany" }.quantity)
        assertEquals(1, syncedLines.first { line -> line.articleId == "art-arco-rosa" }.quantity)
        assertEquals(1, syncedLines.first { line -> line.articleId == "ext-tarima-alfombrada" }.quantity)
        assertEquals(1, syncedLines.first { line -> line.articleId == "art-mesa-helados" }.quantity)

        val manualMesa = SketchElementDescriptor(
            id = "manual-1",
            furnitureType = "MESA_REDONDA",
            x = 240f,
            y = 180f,
            isAutoPlaced = false,
        )
        val labelElement = SketchElementDescriptor(
            id = "lbl-1",
            furnitureType = "TEXT_LABEL",
            label = "Pista",
            x = 400f,
            y = 300f,
        )
        val expanded = RosaFiestaEngine.syncArticleCountsToSketchElements(
            existingElements = listOf(manualMesa, labelElement),
            itemCounts = mapOf("art-mesa-redonda" to 3, "TARIMA" to 1),
        )
        assertEquals(5, expanded.size)
        assertEquals(manualMesa, expanded[0])
        assertEquals(labelElement, expanded[1])
        assertTrue(expanded.drop(2).all { item -> item.isAutoPlaced })

        val idempotentRepeat = RosaFiestaEngine.syncArticleCountsToSketchElements(
            existingElements = expanded,
            itemCounts = mapOf("art-mesa-redonda" to 3, "TARIMA" to 1),
        )
        assertEquals(expanded, idempotentRepeat)

        val trimmed = RosaFiestaEngine.syncArticleCountsToSketchElements(
            existingElements = expanded,
            itemCounts = mapOf("art-mesa-redonda" to 1, "TARIMA" to 0),
        )
        assertEquals(listOf(manualMesa, labelElement), trimmed)
    }
}
