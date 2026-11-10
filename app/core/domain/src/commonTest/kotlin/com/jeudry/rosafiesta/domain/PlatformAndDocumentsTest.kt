package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.domain.platform.Dgii607SaleRecord
import com.jeudry.rosafiesta.domain.platform.DgiiIdentificationType
import com.jeudry.rosafiesta.domain.platform.ExternalUrlBuilder
import com.jeudry.rosafiesta.domain.platform.FinancialReportCsvRow
import com.jeudry.rosafiesta.domain.platform.NoOpPlatformActions
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfBuilder
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfDocument
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfLineItem
import com.jeudry.rosafiesta.domain.platform.ReportCsvBuilder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlatformAndDocumentsTest {
    @Test
    fun `verifica que ReceiptPdfBuilder genere binario PDF 1 4 valido con tabla xref exacta y montos RD`() {
        val receipt = ReceiptPdfDocument(
            receiptNumber = "REC-2026-0842",
            issuedAtText = "10/11/2026 15:45",
            clientName = "Camila Rodríguez",
            clientDocumentOrEmail = "camila@rosafiesta.do",
            eventTitle = "Boda Jardín Colonial",
            eventDateText = "28/11/2026",
            paymentMethod = "Transferencia BHD León",
            paymentReference = "BHD-2026-8841",
            verificationStatus = "VERIFICADO",
            ncfNumber = "B0100000412",
            subtotalDop = 45_000L,
            itbisDop = 8_100L,
            discountDop = 2_000L,
            paidAmountDop = 25_550L,
            remainingBalanceDop = 25_550L,
            lineItems = listOf(
                ReceiptPdfLineItem(
                    description = "Arco orgánico premium y mesa imperial",
                    quantity = 1,
                    unitPriceDop = 30_000L,
                ),
                ReceiptPdfLineItem(
                    description = "Sillas Tiffany doradas",
                    quantity = 50,
                    unitPriceDop = 300L,
                ),
            ),
            notes = "Abono inicial del 50% confirmado en cuenta corporativa.",
        )

        val pdfBytes = ReceiptPdfBuilder.buildPdfBytes(receipt)
        assertTrue(pdfBytes.size > 600)

        val latin1String = buildString(pdfBytes.size) {
            for (byteValue in pdfBytes) {
                append((byteValue.toInt() and 0xFF).toChar())
            }
        }

        assertTrue(latin1String.startsWith("%PDF-1.4\n"))
        assertTrue(latin1String.endsWith("%%EOF\n"))
        assertTrue(latin1String.contains("/BaseFont /Helvetica"))
        assertTrue(latin1String.contains("/BaseFont /Helvetica-Bold"))
        assertTrue(latin1String.contains("RNC: 1-31-84920-2"))
        assertTrue(latin1String.contains("REC-2026-0842"))
        assertTrue(latin1String.contains("Camila Rodr"))
        assertTrue(latin1String.contains("VERIFICADO"))
        assertTrue(latin1String.contains("RD$ 45,000"))
        assertTrue(latin1String.contains("RD$ 25,550"))

        val startXrefMarker = "startxref\n"
        val startXrefPos = latin1String.lastIndexOf(startXrefMarker)
        assertTrue(startXrefPos > 0)
        val afterStartXref = latin1String.substring(startXrefPos + startXrefMarker.length)
        val xrefOffset = afterStartXref.lineSequence().first().trim().toInt()
        assertEquals("xref\n", latin1String.substring(xrefOffset, xrefOffset + 5))

        val xrefLines = latin1String.substring(xrefOffset).lines()
        assertEquals("0000000000 65535 f ", xrefLines[2])
        for (objectNumber in 1..6) {
            val xrefEntry = xrefLines[2 + objectNumber]
            assertEquals(19, xrefEntry.length)
            val objectByteOffset = xrefEntry.substring(0, 10).toInt()
            val expectedHeader = "$objectNumber 0 obj\n"
            val actualSlice = latin1String.substring(objectByteOffset, objectByteOffset + expectedHeader.length)
            assertEquals(expectedHeader, actualSlice)
        }
    }

    @Test
    fun `verifica que ReportCsvBuilder genere CSV con UTF8 BOM y formato fiscal DGII 607`() {
        val financialBytes = ReportCsvBuilder.buildFinancialReportCsvBytes(
            rows = listOf(
                FinancialReportCsvRow(
                    recordCode = "EVT-2026-104",
                    eventDate = "28/11/2026",
                    clientName = "Camila Rodríguez, SRL",
                    clientTaxId = "131884922",
                    eventTitle = "Boda \"Jardín\" Colonial",
                    paymentMethod = "Transferencia BHD",
                    verificationStatus = "VERIFICADO",
                    subtotalDop = 45_000L,
                    itbisDop = 8_100L,
                    totalDop = 53_100L,
                    paidDop = 30_000L,
                    pendingDop = 23_100L,
                ),
            ),
        )

        assertEquals(0xEF.toByte(), financialBytes[0])
        assertEquals(0xBB.toByte(), financialBytes[1])
        assertEquals(0xBF.toByte(), financialBytes[2])

        val csvText = financialBytes.copyOfRange(3, financialBytes.size).decodeToString()
        assertTrue(csvText.contains("\"Camila Rodríguez, SRL\""))
        assertTrue(csvText.contains("\"Boda \"\"Jardín\"\" Colonial\""))
        assertTrue(csvText.contains("53100"))

        val dgiiRecords = listOf(
            Dgii607SaleRecord(
                customerTaxId = "1-01-55421-9",
                identificationType = DgiiIdentificationType.RNC,
                ncfNumber = "B0100000125",
                invoiceDateYyyymmdd = "20261110",
                invoicedSubtotalDop = 50_000L,
                invoicedItbisDop = 9_000L,
                transferOrCheckAmountDop = 59_000L,
            ),
            Dgii607SaleRecord(
                customerTaxId = "001-1425896-7",
                identificationType = DgiiIdentificationType.UNKNOWN,
                ncfNumber = "B0200000811",
                invoiceDateYyyymmdd = "20261111",
                invoicedSubtotalDop = 20_000L,
                invoicedItbisDop = 3_600L,
                cardAmountDop = 23_600L,
            ),
        )

        val dgiiCsvBytes = ReportCsvBuilder.buildDgii607CsvBytes(
            companyRnc = "1-31-88492-2",
            periodYyyymm = "2026-11",
            records = dgiiRecords,
        )
        assertEquals(0xEF.toByte(), dgiiCsvBytes[0])
        val dgiiCsvText = dgiiCsvBytes.copyOfRange(3, dgiiCsvBytes.size).decodeToString()
        assertTrue(dgiiCsvText.contains("607,131884922,202611,101554219,1,B0100000125"))
        assertTrue(dgiiCsvText.contains("607,131884922,202611,00114258967,2,B0200000811"))

        val dgiiPipeText = ReportCsvBuilder.buildDgii607PipeBytes(
            companyRnc = "1-31-88492-2",
            periodYyyymm = "202611",
            records = dgiiRecords,
        ).decodeToString()
        assertTrue(dgiiPipeText.startsWith("607|131884922|202611|2\r\n"))
        assertTrue(dgiiPipeText.contains("101554219|1|B0100000125||01|20261110||50000.00|9000.00"))
    }

    @Test
    fun `verifica constructores de URLs externas para telefono WhatsApp mapas y NoOpPlatformActions`() = runTest {
        assertEquals("tel:+18095550142", ExternalUrlBuilder.buildPhoneTelUrl("809-555-0142"))
        assertEquals(
            "https://wa.me/18295550199?text=Hola%20RosaFiesta",
            ExternalUrlBuilder.buildWhatsAppUrl("829-555-0199", "Hola RosaFiesta"),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=Piantini%20Santo%20Domingo",
            ExternalUrlBuilder.buildGoogleMapsSearchUrl("Piantini Santo Domingo"),
        )
        assertEquals(
            "https://maps.apple.com/?q=Zona%20Colonial",
            ExternalUrlBuilder.buildAppleMapsSearchUrl("Zona Colonial"),
        )
        assertEquals("application/pdf", ExternalUrlBuilder.inferMimeType("comprobante.PDF"))
        assertEquals("text/csv", ExternalUrlBuilder.inferMimeType("dgii_607.csv"))

        val actions = NoOpPlatformActions(configuredBaseUrl = "http://10.0.2.2:8788/")
        assertEquals("http://10.0.2.2:8788", actions.defaultApiBaseUrl())
        assertTrue(actions.dialPhoneNumber("809-555-0101"))
        assertEquals("tel:+18095550101", actions.lastOpenedUrl)

        val sampleMedia = PickedMediaFile(
            fileName = "voucher_bhd.png",
            mimeType = "image/png",
            sizeBytes = 4L,
            bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47),
        )
        actions.queuedPickedFile = sampleMedia
        val picked = actions.pickMediaFile()
        assertNotNull(picked)
        assertEquals(sampleMedia, picked)
    }
}
