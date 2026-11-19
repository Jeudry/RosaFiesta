package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.domain.platform.ContractPdfBuilder
import com.jeudry.rosafiesta.domain.platform.ContractPdfDocument
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfBuilder
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfDocument
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfLineItem
import com.jeudry.rosafiesta.domain.platform.ReportCsvBuilder
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DomainFiscalAndPlatformExtensionsTest {
    @Test
    fun receiptAndContractPdfBuildersIncludeFiscalNcfRncItbisDepositAndVerification() {
        val receiptBytes = ReceiptPdfBuilder.buildPdfBytes(
            ReceiptPdfDocument(
                receiptNumber = "RF-REC-2026-009",
                issuedAtText = "24/10/2026",
                clientName = "Eventos Caribe SRL",
                eventTitle = "Gala Corporativa Octubre",
                paymentMethod = "Transferencia Banreservas",
                verificationStatus = "Verificado",
                subtotalDop = 10000L,
                paidAmountDop = 13800L,
                ncfType = "B01",
                ncfNumber = "B0100000042",
                clientRnc = "1-31-84920-2",
                itbisDop = 1800L,
                securityDepositDop = 2000L,
                signedByName = "Mariela Gomez",
                signedAtIso = "2026-10-20T14:30:00Z",
                lineItems = listOf(
                    ReceiptPdfLineItem(
                        description = "Tarima principal alfombrada",
                        quantity = 1,
                        unitPriceDop = 10000L,
                    ),
                ),
            ),
        )
        val rawReceiptPdfText = receiptBytes.decodeToString()
        val receiptPdfText = rawReceiptPdfText.replace("\\(", "(").replace("\\)", ")")
        assertTrue(rawReceiptPdfText.startsWith("%PDF-1."))
        assertTrue(receiptPdfText.contains("NCF (B01): B0100000042"))
        assertTrue(receiptPdfText.contains("RNC/Cedula: 1-31-84920-2"))
        assertTrue(receiptPdfText.contains("Subtotal gravado:"))
        assertTrue(receiptPdfText.contains("ITBIS (18%):"))
        assertTrue(receiptPdfText.contains("Deposito reembolsable:"))
        assertTrue(receiptPdfText.contains("Estado de verificacion: Verificado"))

        val contractBytes = ContractPdfBuilder.buildPdfBytes(
            ContractPdfDocument(
                contractNumber = "RF-CTR-2026-003",
                issuedAtText = "24/10/2026",
                clientName = "Camila Ortiz",
                clientRnc = "00112345678",
                eventTitle = "Boda Real Rosa",
                eventDateText = "2026-10-24",
                ncfType = "B01",
                ncfNumber = "B0100000043",
                subtotalDop = 50000L,
                itbisDop = 9000L,
                securityDepositDop = 5000L,
                totalDop = 64000L,
                paidAmountDop = 20000L,
                signedByName = "Camila Ortiz",
                signedAtIso = "2026-10-18T11:00:00Z",
            ),
        )
        val rawContractPdfText = contractBytes.decodeToString()
        val contractPdfText = rawContractPdfText.replace("\\(", "(").replace("\\)", ")")
        assertTrue(rawContractPdfText.startsWith("%PDF-1."))
        assertTrue(contractPdfText.contains("CONTRATO OFICIAL DE ALQUILER Y SERVICIOS"))
        assertTrue(contractPdfText.contains("NCF (B01): B0100000043"))
        assertTrue(contractPdfText.contains("RNC/Cedula: 00112345678"))
        assertTrue(contractPdfText.contains("Deposito reembolsable:"))
        assertTrue(contractPdfText.contains("Firmado: Camila Ortiz (2026-10-18)"))
    }

    @Test
    fun reportCsvBuilderBuildDgii607CsvFiltersVerifiedNcfPaymentsAndComputesFiscalFallback() {
        val verifiedWithFallbackSplit = PaymentRecord(
            id = "pay-1",
            receiptNumber = "REC-01",
            eventId = "evt-1",
            eventTitle = "Boda Jardín",
            clientName = "Sofía Méndez",
            amountDop = 13800,
            methodLabel = "Transferencia",
            kindLabel = "Abono",
            dateLabel = "2026-10-20",
            referenceCode = "BR-9981",
            verified = true,
            ncfType = "B01",
            ncfNumber = "B0100000010",
            clientRnc = "131849202",
            verifiedAtIso = "2026-10-20T12:00:00Z",
            securityDepositDop = 2000,
        )
        val unverifiedPayment = PaymentRecord(
            id = "pay-2",
            receiptNumber = "REC-02",
            eventId = "evt-2",
            eventTitle = "Cumpleaños",
            clientName = "Carlos Peña",
            amountDop = 5000,
            methodLabel = "Transferencia",
            kindLabel = "Anticipo",
            dateLabel = "2026-10-21",
            referenceCode = "PP-223",
            verified = false,
            ncfNumber = "B0200000011",
        )
        val rejectedPayment = verifiedWithFallbackSplit.copy(
            id = "pay-3",
            ncfNumber = "B0100000012",
            rejectionReason = "Comprobante ilegible",
        )
        val verifiedOtherMonth = verifiedWithFallbackSplit.copy(
            id = "pay-4",
            ncfNumber = "B0200000013",
            clientRnc = "",
            ncfType = "B02",
            dateLabel = "2026-11-05",
            verifiedAtIso = "2026-11-05T10:00:00Z",
            subtotalDop = 8000,
            itbisDop = 1440,
            securityDepositDop = 0,
            amountDop = 9440,
        )

        val octoberCsv = ReportCsvBuilder.buildDgii607Csv(
            payments = listOf(verifiedWithFallbackSplit, unverifiedPayment, rejectedPayment, verifiedOtherMonth),
            periodYyyymm = "2026-10",
        )
        assertTrue(octoberCsv.startsWith("RNC_Cedula,Tipo_NCF,NCF,"))
        assertTrue(octoberCsv.contains("131849202,B01,B0100000010,2026-10-20,evt-1,Sofía Méndez,10000,1800,2000,13800,Transferencia"))
        assertFalse(octoberCsv.contains("B0200000011"))
        assertFalse(octoberCsv.contains("B0100000012"))
        assertFalse(octoberCsv.contains("B0200000013"))

        val allPeriodsCsv = ReportCsvBuilder.buildDgii607Csv(
            payments = listOf(verifiedWithFallbackSplit, verifiedOtherMonth),
        )
        assertTrue(allPeriodsCsv.contains("B0100000010"))
        assertTrue(allPeriodsCsv.contains("B0200000013"))
    }

    @Test
    fun repositoryAvailabilityFallbackEvaluatesTurnaroundConflictsAndExcludesOwnEvent() {
        val chairArticle = CatalogArticle(
            id = "art-silla-tiffany",
            code = "RF-001",
            name = "Silla Tiffany dorada",
            category = "Sillas",
            description = "Silla dorada para recepción",
            pricePerDayDop = 4,
            totalStock = 100,
            availableOct23 = 100,
            photo = PhotoAsset.BODA,
            variants = listOf("Dorada"),
            damagedStock = 10,
        )
        val activeEvent = ClientEvent(
            id = "evt-active",
            code = "EV-100",
            title = "Boda Real",
            clientName = "Elena Torres",
            eventType = "Boda",
            dateLabel = "24 Oct 2026",
            daysUntilEvent = 4,
            venue = "Jardín Botánico",
            zone = "Norte",
            guests = 70,
            articleCount = 70,
            totalDop = 4500,
            paidDepositDop = 1500,
            depositMethodLabel = "Transferencia",
            depositDateLabel = "15 Oct 2026",
            balanceDueDateLabel = "22 Oct 2026",
            status = EventStatus.CONFIRMED,
            leadStaffName = "Rosa",
            photo = PhotoAsset.BODA,
            items = listOf(
                EventLineItem(
                    articleCode = "RF-001",
                    name = "Silla Tiffany dorada",
                    quantity = 70,
                    unitPriceDop = 4,
                    photo = PhotoAsset.BODA,
                ),
            ),
            eventDateIso = "2026-10-24",
            pickupDateIso = "2026-10-23",
            returnDateIso = "2026-10-25",
        )
        val cancelledEvent = activeEvent.copy(
            id = "evt-cancelled",
            status = EventStatus.CANCELLED,
        )

        val conflictResult = RepositoryAvailabilityFallback.check(
            date = "2026-10-25",
            articleId = "art-silla-tiffany",
            quantity = 30,
            excludeEventId = "",
            articles = listOf(chairArticle),
            events = listOf(activeEvent, cancelledEvent),
        ).getOrThrow()
        assertFalse(conflictResult.available)
        assertEquals("2026-10-24", conflictResult.pickupDateIso)
        assertEquals("2026-10-26", conflictResult.returnDateIso)
        assertEquals(1, conflictResult.conflicts.size)
        assertEquals(20, conflictResult.conflicts.first().available)

        val selfEditResult = RepositoryAvailabilityFallback.check(
            date = "2026-10-25",
            articleId = "RF-001",
            quantity = 80,
            excludeEventId = "evt-active",
            articles = listOf(chairArticle),
            events = listOf(activeEvent),
        ).getOrThrow()
        assertTrue(selfEditResult.available)
        assertTrue(selfEditResult.conflicts.isEmpty())

        val invalidDateResult = RepositoryAvailabilityFallback.check(
            date = "2026-02-29",
            articleId = "art-silla-tiffany",
            quantity = 1,
            excludeEventId = "",
            articles = listOf(chairArticle),
            events = emptyList(),
        )
        assertTrue(invalidDateResult.isFailure)
    }

    @Test
    fun sensitiveDataRedactorTranslatesAllNewBackendErrorCodesToSpanish() {
        val codes = listOf(
            "SMTP_PROVIDER_NOT_CONFIGURED",
            "OAUTH_PROVIDER_NOT_CONFIGURED",
            "AI_PROVIDER_NOT_CONFIGURED",
            "STT_PROVIDER_NOT_CONFIGURED",
            "CARD_GATEWAY_NOT_CONFIGURED",
            "WHATSAPP_PROVIDER_NOT_CONFIGURED",
            "STOCK_INSUFFICIENT_FOR_DATE",
            "ROSA_AUDIO_TOO_LONG",
            "ROSA_RATE_LIMITED",
            "INVALID_FISCAL_RNC",
        )
        for (errorCode in codes) {
            val translated = SensitiveDataRedactor.translateBusinessError(errorCode, "raw error: $errorCode")
            assertNotEquals("raw error: $errorCode", translated)
            assertFalse(translated.contains(errorCode))
            assertTrue(translated.isNotBlank())
        }
    }

    @Test
    fun extendedDomainModelsPreserveDefaultsAndEquality() {
        val media = MediaUploadAsset(
            id = "med-1",
            url = "https://cdn.rosafiesta.do/media/voucher.jpg",
            mimeType = "image/jpeg",
            byteSize = 48120L,
            filename = "voucher.jpg",
        )
        val card = CardSessionResult(
            sessionId = "cs_test_1",
            checkoutUrl = "https://checkout.rosafiesta.do/pay/1",
            provider = "azul",
        )
        val streamEvent = RealtimeStreamEvent(
            seq = 17L,
            type = "payment.verified",
            entityId = "pay-1",
        )
        val incident = DamageIncident(
            id = "dmg-1",
            eventTitle = "Boda",
            articleName = "Silla Tiffany",
            damagedUnits = 2,
            missingUnits = 0,
            replacementDop = 800,
            notes = "Rayadura leve",
            photoUrl = media.url,
            resolutionNotes = "Descontado de depósito",
            chargePaymentId = "pay-9",
        )
        val queueItem = OfflineQueueItem(
            id = "q-1",
            actionLabel = "Verificar pago",
            entityCode = "REC-01",
            timeAgo = "hace 2m",
            synced = false,
            operationType = "payment_verify",
            payloadJson = "{\"paymentId\":\"pay-1\"}",
            idempotencyKey = "idem-1",
        )
        assertEquals("all", streamEvent.audience)
        assertEquals("azul", card.provider)
        assertEquals("pay-9", incident.chargePaymentId)
        assertEquals("idem-1", queueItem.idempotencyKey)
    }
}
