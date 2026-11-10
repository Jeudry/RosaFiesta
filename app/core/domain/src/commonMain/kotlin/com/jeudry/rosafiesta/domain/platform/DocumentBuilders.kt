package com.jeudry.rosafiesta.domain.platform

import kotlin.math.abs

/**
 * Individual itemized line inside a RosaFiesta payment or quote PDF receipt.
 */
public data class ReceiptPdfLineItem(
    public val description: String,
    public val quantity: Int,
    public val unitPriceDop: Long,
    public val lineTotalDop: Long = unitPriceDop * quantity.toLong(),
)

/**
 * Structured payload for generating an authentic RosaFiesta PDF 1.4 receipt in Dominican Pesos (`RD$`).
 */
public data class ReceiptPdfDocument(
    public val receiptNumber: String,
    public val issuedAtText: String,
    public val clientName: String,
    public val eventTitle: String,
    public val paymentMethod: String,
    public val verificationStatus: String,
    public val subtotalDop: Long,
    public val paidAmountDop: Long,
    public val companyName: String = "RosaFiesta Eventos & Alquileres SRL",
    public val companyRnc: String = "1-31-84920-2",
    public val ncfNumber: String = "",
    public val clientDocumentOrEmail: String = "",
    public val eventDateText: String = "",
    public val paymentReference: String = "",
    public val itbisDop: Long = 0L,
    public val discountDop: Long = 0L,
    public val remainingBalanceDop: Long = 0L,
    public val lineItems: List<ReceiptPdfLineItem> = emptyList(),
    public val notes: String = "",
)

/**
 * Pure-Kotlin PDF 1.4 binary generator producing byte-accurate `xref` tables, Helvetica Type1 font dictionaries,
 * vector header graphics, and WinAnsi-encoded Spanish receipt content in `RD$`.
 */
public object ReceiptPdfBuilder {
    /**
     * Generates a complete, valid `%PDF-1.4` binary document for [document].
     */
    public fun buildPdfBytes(document: ReceiptPdfDocument): ByteArray {
        val streamContentBytes = buildPageContentStream(document)
        val buffer = GrowableByteBuffer(initialCapacity = streamContentBytes.size + 1024)

        buffer.appendAscii("%PDF-1.4\n%")
        buffer.appendByte(0xE2)
        buffer.appendByte(0xE3)
        buffer.appendByte(0xCF)
        buffer.appendByte(0xD3)
        buffer.appendAscii("\n")

        val objectOffsets = IntArray(7)

        objectOffsets[1] = buffer.size
        buffer.appendAscii("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

        objectOffsets[2] = buffer.size
        buffer.appendAscii("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n")

        objectOffsets[3] = buffer.size
        buffer.appendAscii(
            "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] " +
                "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>\nendobj\n",
        )

        objectOffsets[4] = buffer.size
        buffer.appendAscii(
            "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n",
        )

        objectOffsets[5] = buffer.size
        buffer.appendAscii(
            "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n",
        )

        objectOffsets[6] = buffer.size
        buffer.appendAscii("6 0 obj\n<< /Length ${streamContentBytes.size} >>\nstream\n")
        buffer.appendBytes(streamContentBytes)
        buffer.appendAscii("\nendstream\nendobj\n")

        val xrefOffset = buffer.size
        buffer.appendAscii("xref\n0 7\n")
        buffer.appendAscii("0000000000 65535 f \n")
        for (objectNumber in 1..6) {
            val formattedOffset = objectOffsets[objectNumber].toString().padStart(10, '0')
            buffer.appendAscii("$formattedOffset 00000 n \n")
        }

        buffer.appendAscii("trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n")
        buffer.appendAscii(xrefOffset.toString())
        buffer.appendAscii("\n%%EOF\n")
        return buffer.toByteArray()
    }

    /**
     * Formats an integer or cent-free Dominican Peso amount into `RD$ 12,500`.
     */
    public fun formatDopAmount(amountDop: Long): String {
        val isNegative = amountDop < 0L
        val absoluteDigits = abs(amountDop).toString()
        val grouped = buildString {
            for (index in absoluteDigits.indices) {
                if (index > 0) {
                    val remaining = absoluteDigits.length - index
                    if (remaining % 3 == 0) {
                        append(',')
                    }
                }
                append(absoluteDigits[index])
            }
        }
        return if (isNegative) "-RD$ $grouped" else "RD$ $grouped"
    }

    private fun buildPageContentStream(document: ReceiptPdfDocument): ByteArray {
        val stream = GrowableByteBuffer(initialCapacity = 2048)

        stream.appendAscii("0.918 0.282 0.600 rg\n")
        stream.appendAscii("36 748 523 62 re f\n")

        val companyHeader = document.companyName.trim().ifEmpty { "RosaFiesta Eventos & Alquileres SRL" }
        val rncText = "RNC: ${document.companyRnc.trim().ifEmpty { "1-31-84920-2" }}"
        val receiptNo = "Comprobante: ${document.receiptNumber.trim()}"

        appendPdfText(stream, font = "F2", fontSize = 16, x = 50, y = 786, rgb = "1 1 1", text = companyHeader)
        appendPdfText(stream, font = "F1", fontSize = 10, x = 50, y = 766, rgb = "1 1 1", text = "$rncText  |  Santo Domingo, R.D.")
        appendPdfText(stream, font = "F2", fontSize = 11, x = 360, y = 786, rgb = "1 1 1", text = receiptNo)
        appendPdfText(
            stream = stream,
            font = "F1",
            fontSize = 9,
            x = 360,
            y = 766,
            rgb = "1 1 1",
            text = "Fecha: ${document.issuedAtText.trim()}",
        )

        stream.appendAscii("0.965 0.953 0.980 rg\n")
        stream.appendAscii("36 628 523 106 re f\n")

        var metaY = 714
        appendPdfText(
            stream = stream,
            font = "F2",
            fontSize = 11,
            x = 50,
            y = metaY,
            rgb = "0.102 0.102 0.180",
            text = "Cliente: ${document.clientName.trim()}",
        )
        if (document.clientDocumentOrEmail.isNotBlank()) {
            appendPdfText(
                stream = stream,
                font = "F1",
                fontSize = 10,
                x = 330,
                y = metaY,
                rgb = "0.350 0.340 0.430",
                text = "Contacto/Doc: ${document.clientDocumentOrEmail.trim()}",
            )
        }
        metaY -= 20
        appendPdfText(
            stream = stream,
            font = "F2",
            fontSize = 11,
            x = 50,
            y = metaY,
            rgb = "0.102 0.102 0.180",
            text = "Evento: ${document.eventTitle.trim()}",
        )
        if (document.eventDateText.isNotBlank()) {
            appendPdfText(
                stream = stream,
                font = "F1",
                fontSize = 10,
                x = 330,
                y = metaY,
                rgb = "0.350 0.340 0.430",
                text = "Fecha evento: ${document.eventDateText.trim()}",
            )
        }
        metaY -= 20
        appendPdfText(
            stream = stream,
            font = "F1",
            fontSize = 10,
            x = 50,
            y = metaY,
            rgb = "0.102 0.102 0.180",
            text = "Metodo de pago: ${document.paymentMethod.trim()}",
        )
        if (document.paymentReference.isNotBlank()) {
            appendPdfText(
                stream = stream,
                font = "F1",
                fontSize = 10,
                x = 330,
                y = metaY,
                rgb = "0.102 0.102 0.180",
                text = "Referencia: ${document.paymentReference.trim()}",
            )
        }
        metaY -= 20
        appendPdfText(
            stream = stream,
            font = "F2",
            fontSize = 10,
            x = 50,
            y = metaY,
            rgb = "0.145 0.490 0.290",
            text = "Estado de verificacion: ${document.verificationStatus.trim()}",
        )
        if (document.ncfNumber.isNotBlank()) {
            appendPdfText(
                stream = stream,
                font = "F2",
                fontSize = 10,
                x = 330,
                y = metaY,
                rgb = "0.102 0.102 0.180",
                text = "NCF: ${document.ncfNumber.trim()}",
            )
        }

        var cursorY = 598
        appendPdfText(
            stream = stream,
            font = "F2",
            fontSize = 11,
            x = 36,
            y = cursorY,
            rgb = "0.102 0.102 0.180",
            text = "DESGLOSE DE CONCEPTOS EN RD$",
        )
        cursorY -= 12
        stream.appendAscii("0.880 0.860 0.910 RG 1 w 36 $cursorY m 559 $cursorY l S\n")
        cursorY -= 18

        appendPdfText(stream, font = "F2", fontSize = 9, x = 40, y = cursorY, rgb = "0.380 0.380 0.480", text = "Concepto")
        appendPdfText(stream, font = "F2", fontSize = 9, x = 330, y = cursorY, rgb = "0.380 0.380 0.480", text = "Cant.")
        appendPdfText(stream, font = "F2", fontSize = 9, x = 385, y = cursorY, rgb = "0.380 0.380 0.480", text = "Precio Unit.")
        appendPdfText(stream, font = "F2", fontSize = 9, x = 475, y = cursorY, rgb = "0.380 0.380 0.480", text = "Subtotal")
        cursorY -= 16

        val itemsToRender = if (document.lineItems.isNotEmpty()) {
            document.lineItems.take(12)
        } else {
            listOf(
                ReceiptPdfLineItem(
                    description = "Servicios y alquileres para ${document.eventTitle.trim()}",
                    quantity = 1,
                    unitPriceDop = document.subtotalDop,
                    lineTotalDop = document.subtotalDop,
                ),
            )
        }

        for (lineItem in itemsToRender) {
            val truncatedDesc = lineItem.description.trim().take(48)
            val unitFormatted = formatDopAmount(lineItem.unitPriceDop)
            val lineTotalFormatted = formatDopAmount(lineItem.lineTotalDop)
            appendPdfText(stream, font = "F1", fontSize = 10, x = 40, y = cursorY, rgb = "0.102 0.102 0.180", text = truncatedDesc)
            appendPdfText(stream, font = "F1", fontSize = 10, x = 335, y = cursorY, rgb = "0.102 0.102 0.180", text = lineItem.quantity.toString())
            appendPdfText(stream, font = "F1", fontSize = 10, x = 385, y = cursorY, rgb = "0.102 0.102 0.180", text = unitFormatted)
            appendPdfText(stream, font = "F2", fontSize = 10, x = 475, y = cursorY, rgb = "0.102 0.102 0.180", text = lineTotalFormatted)
            cursorY -= 18
        }

        cursorY -= 6
        stream.appendAscii("0.880 0.860 0.910 RG 1 w 36 $cursorY m 559 $cursorY l S\n")
        cursorY -= 22

        val subtotalFormatted = formatDopAmount(document.subtotalDop)
        appendPdfText(stream, font = "F1", fontSize = 10, x = 340, y = cursorY, rgb = "0.350 0.340 0.430", text = "Subtotal:")
        appendPdfText(stream, font = "F2", fontSize = 10, x = 465, y = cursorY, rgb = "0.102 0.102 0.180", text = subtotalFormatted)
        cursorY -= 18

        val itbisFormatted = formatDopAmount(document.itbisDop)
        appendPdfText(stream, font = "F1", fontSize = 10, x = 340, y = cursorY, rgb = "0.350 0.340 0.430", text = "ITBIS (18%):")
        appendPdfText(stream, font = "F2", fontSize = 10, x = 465, y = cursorY, rgb = "0.102 0.102 0.180", text = itbisFormatted)
        cursorY -= 18

        if (document.discountDop > 0L) {
            val discountFormatted = "-${formatDopAmount(document.discountDop)}"
            appendPdfText(stream, font = "F1", fontSize = 10, x = 340, y = cursorY, rgb = "0.350 0.340 0.430", text = "Descuento:")
            appendPdfText(stream, font = "F2", fontSize = 10, x = 465, y = cursorY, rgb = "0.145 0.490 0.290", text = discountFormatted)
            cursorY -= 18
        }

        val paidFormatted = formatDopAmount(document.paidAmountDop)
        appendPdfText(stream, font = "F2", fontSize = 12, x = 340, y = cursorY, rgb = "0.486 0.361 0.941", text = "Monto pagado:")
        appendPdfText(stream, font = "F2", fontSize = 12, x = 465, y = cursorY, rgb = "0.486 0.361 0.941", text = paidFormatted)
        cursorY -= 20

        val remainingFormatted = formatDopAmount(document.remainingBalanceDop)
        appendPdfText(stream, font = "F1", fontSize = 10, x = 340, y = cursorY, rgb = "0.350 0.340 0.430", text = "Balance pendiente:")
        appendPdfText(stream, font = "F2", fontSize = 10, x = 465, y = cursorY, rgb = "0.102 0.102 0.180", text = remainingFormatted)
        cursorY -= 30

        if (document.notes.isNotBlank()) {
            val trimmedNote = document.notes.trim().take(92)
            appendPdfText(stream, font = "F1", fontSize = 9, x = 36, y = cursorY, rgb = "0.350 0.340 0.430", text = "Notas: $trimmedNote")
        }

        appendPdfText(
            stream = stream,
            font = "F1",
            fontSize = 8,
            x = 36,
            y = 48,
            rgb = "0.450 0.450 0.550",
            text = "Documento emitido digitalmente por RosaFiesta Republica Dominicana. Todos los importes expresados en Pesos Dominicanos (RD$).",
        )

        return stream.toByteArray()
    }

    private fun appendPdfText(
        stream: GrowableByteBuffer,
        font: String,
        fontSize: Int,
        x: Int,
        y: Int,
        rgb: String,
        text: String,
    ) {
        stream.appendAscii("BT\n/$font $fontSize Tf\n$rgb rg\n$x $y Td\n(")
        for (character in text) {
            val winAnsiByte = toWinAnsiByte(character)
            val isBackslash = winAnsiByte == '\\'.code
            val isOpenParen = winAnsiByte == '('.code
            val isCloseParen = winAnsiByte == ')'.code
            if (isBackslash) {
                stream.appendByte('\\'.code)
                stream.appendByte('\\'.code)
            } else if (isOpenParen) {
                stream.appendByte('\\'.code)
                stream.appendByte('('.code)
            } else if (isCloseParen) {
                stream.appendByte('\\'.code)
                stream.appendByte(')'.code)
            } else {
                stream.appendByte(winAnsiByte)
            }
        }
        stream.appendAscii(") Tj\nET\n")
    }

    private fun toWinAnsiByte(character: Char): Int {
        val codePoint = character.code
        if (codePoint in 0x20..0x7E) return codePoint
        if (codePoint in 0xA0..0xFF) return codePoint
        return when (codePoint) {
            0x20AC -> 0x80
            0x201A -> 0x82
            0x201E -> 0x84
            0x2026 -> 0x85
            0x2013 -> 0x96
            0x2014 -> 0x97
            0x2018 -> 0x91
            0x2019 -> 0x92
            0x201C -> 0x93
            0x201D -> 0x94
            0x2022 -> 0x95
            else -> '?'.code
        }
    }

    private class GrowableByteBuffer(initialCapacity: Int = 512) {
        private var data = ByteArray(initialCapacity.coerceAtLeast(64))
        var size: Int = 0
            private set

        fun appendByte(byteValue: Int) {
            ensureCapacity(size + 1)
            data[size] = (byteValue and 0xFF).toByte()
            size += 1
        }

        fun appendBytes(source: ByteArray) {
            if (source.isEmpty()) return
            ensureCapacity(size + source.size)
            source.copyInto(data, destinationOffset = size)
            size += source.size
        }

        fun appendAscii(asciiText: String) {
            ensureCapacity(size + asciiText.length)
            for (index in asciiText.indices) {
                data[size + index] = (asciiText[index].code and 0x7F).toByte()
            }
            size += asciiText.length
        }

        fun toByteArray(): ByteArray = data.copyOf(size)

        private fun ensureCapacity(requiredCapacity: Int) {
            if (requiredCapacity <= data.size) return
            var nextCapacity = data.size * 2
            if (nextCapacity < requiredCapacity) {
                nextCapacity = requiredCapacity
            }
            data = data.copyOf(nextCapacity)
        }
    }
}

/**
 * Dominican DGII tax identification type codes for Formato 607 sales declarations.
 */
public enum class DgiiIdentificationType(public val dgiiCode: String) {
    /** Default unspecified identification type. */
    UNKNOWN("0"),

    /** Registro Nacional de Contribuyentes (9 digits). */
    RNC("1"),

    /** Cédula de Identidad y Electoral (11 digits). */
    CEDULA("2"),

    /** Foreign passport or non-resident identifier. */
    PASSPORT("3"),
}

/**
 * Single row for RosaFiesta financial and event revenue CSV exports (`RD$`).
 */
public data class FinancialReportCsvRow(
    public val recordCode: String,
    public val eventDate: String,
    public val clientName: String,
    public val clientTaxId: String,
    public val eventTitle: String,
    public val paymentMethod: String,
    public val verificationStatus: String,
    public val subtotalDop: Long,
    public val itbisDop: Long,
    public val totalDop: Long,
    public val paidDop: Long,
    public val pendingDop: Long,
)

/**
 * Single fiscal sales record for Dominican Republic DGII Formato 607 reporting.
 */
public data class Dgii607SaleRecord(
    public val customerTaxId: String,
    public val identificationType: DgiiIdentificationType,
    public val ncfNumber: String,
    public val invoiceDateYyyymmdd: String,
    public val invoicedSubtotalDop: Long,
    public val invoicedItbisDop: Long,
    public val modifiedNcfNumber: String = "",
    public val incomeTypeCode: String = "01",
    public val withholdingDateYyyymmdd: String = "",
    public val cashAmountDop: Long = 0L,
    public val transferOrCheckAmountDop: Long = 0L,
    public val cardAmountDop: Long = 0L,
    public val creditSaleAmountDop: Long = 0L,
)

/**
 * Pure-Kotlin UTF-8 BOM CSV and Dominican DGII Formato 607 fiscal tabular report builder.
 */
public object ReportCsvBuilder {
    private val utf8BomBytes: ByteArray = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    /**
     * Encodes arbitrary tabular [headers] and [rows] into RFC 4180 CSV prefixed with UTF-8 BOM (`EF BB BF`)
     * so Excel, Numbers, and LibreOffice render Spanish accents and `RD$` symbols accurately.
     */
    public fun buildCsvBytes(
        headers: List<String>,
        rows: List<List<String>>,
    ): ByteArray {
        val textContent = buildString {
            if (headers.isNotEmpty()) {
                appendCsvRow(headers)
            }
            for (row in rows) {
                appendCsvRow(row)
            }
        }
        val bodyBytes = textContent.encodeToByteArray()
        val result = ByteArray(utf8BomBytes.size + bodyBytes.size)
        utf8BomBytes.copyInto(result, destinationOffset = 0)
        bodyBytes.copyInto(result, destinationOffset = utf8BomBytes.size)
        return result
    }

    /**
     * Generates a UTF-8 BOM financial & billing report CSV for RosaFiesta orders and events.
     */
    public fun buildFinancialReportCsvBytes(rows: List<FinancialReportCsvRow>): ByteArray {
        val headers = listOf(
            "Codigo",
            "Fecha Evento",
            "Cliente",
            "RNC o Cedula",
            "Evento",
            "Metodo de Pago",
            "Estado",
            "Subtotal RD$",
            "ITBIS RD$",
            "Total RD$",
            "Pagado RD$",
            "Pendiente RD$",
        )
        val mappedRows = rows.map { record ->
            listOf(
                record.recordCode,
                record.eventDate,
                record.clientName,
                record.clientTaxId,
                record.eventTitle,
                record.paymentMethod,
                record.verificationStatus,
                record.subtotalDop.toString(),
                record.itbisDop.toString(),
                record.totalDop.toString(),
                record.paidDop.toString(),
                record.pendingDop.toString(),
            )
        }
        return buildCsvBytes(headers = headers, rows = mappedRows)
    }

    /**
     * Generates a UTF-8 BOM Dominican DGII Formato 607 fiscal sales declaration in spreadsheet-ready CSV format.
     */
    public fun buildDgii607CsvBytes(
        companyRnc: String,
        periodYyyymm: String,
        records: List<Dgii607SaleRecord>,
    ): ByteArray {
        val cleanCompanyRnc = digitsOnly(companyRnc)
        val cleanPeriod = digitsOnly(periodYyyymm).take(6)
        val headers = listOf(
            "Formato",
            "RNC Emisor",
            "Periodo AAAAMM",
            "RNC/Cedula Comprador",
            "Tipo Id",
            "NCF",
            "NCF Modificado",
            "Tipo Ingreso",
            "Fecha Comprobante",
            "Fecha Retencion",
            "Monto Facturado RD$",
            "ITBIS Facturado RD$",
            "Efectivo RD$",
            "Transferencia/Cheque RD$",
            "Tarjeta RD$",
            "Venta a Credito RD$",
        )
        val csvRows = records.map { sale ->
            val resolvedTipoId = if (sale.identificationType == DgiiIdentificationType.UNKNOWN) {
                inferDgiiIdentificationCode(sale.customerTaxId)
            } else {
                sale.identificationType.dgiiCode
            }
            listOf(
                "607",
                cleanCompanyRnc,
                cleanPeriod,
                digitsAndLettersOnly(sale.customerTaxId),
                resolvedTipoId,
                sale.ncfNumber.trim(),
                sale.modifiedNcfNumber.trim(),
                sale.incomeTypeCode.trim().ifEmpty { "01" },
                digitsOnly(sale.invoiceDateYyyymmdd).take(8),
                digitsOnly(sale.withholdingDateYyyymmdd).take(8),
                formatFixedTwoDecimals(sale.invoicedSubtotalDop),
                formatFixedTwoDecimals(sale.invoicedItbisDop),
                formatFixedTwoDecimals(sale.cashAmountDop),
                formatFixedTwoDecimals(sale.transferOrCheckAmountDop),
                formatFixedTwoDecimals(sale.cardAmountDop),
                formatFixedTwoDecimals(sale.creditSaleAmountDop),
            )
        }
        return buildCsvBytes(headers = headers, rows = csvRows)
    }

    /**
     * Generates the official pipe-delimited (`|`) DGII Formato 607 declaration file (`607|RNC|AAAAMM|Cantidad`).
     */
    public fun buildDgii607PipeBytes(
        companyRnc: String,
        periodYyyymm: String,
        records: List<Dgii607SaleRecord>,
    ): ByteArray {
        val cleanCompanyRnc = digitsOnly(companyRnc)
        val cleanPeriod = digitsOnly(periodYyyymm).take(6)
        val content = buildString {
            append("607|")
            append(cleanCompanyRnc).append('|')
            append(cleanPeriod).append('|')
            append(records.size)
            append("\r\n")
            for (sale in records) {
                val resolvedTipoId = if (sale.identificationType == DgiiIdentificationType.UNKNOWN) {
                    inferDgiiIdentificationCode(sale.customerTaxId)
                } else {
                    sale.identificationType.dgiiCode
                }
                append(digitsAndLettersOnly(sale.customerTaxId)).append('|')
                append(resolvedTipoId).append('|')
                append(sale.ncfNumber.trim()).append('|')
                append(sale.modifiedNcfNumber.trim()).append('|')
                append(sale.incomeTypeCode.trim().ifEmpty { "01" }).append('|')
                append(digitsOnly(sale.invoiceDateYyyymmdd).take(8)).append('|')
                append(digitsOnly(sale.withholdingDateYyyymmdd).take(8)).append('|')
                append(formatFixedTwoDecimals(sale.invoicedSubtotalDop)).append('|')
                append(formatFixedTwoDecimals(sale.invoicedItbisDop)).append('|')
                append("0.00|0.00|0.00|0.00|0.00|0.00|0.00|")
                append(formatFixedTwoDecimals(sale.cashAmountDop)).append('|')
                append(formatFixedTwoDecimals(sale.transferOrCheckAmountDop)).append('|')
                append(formatFixedTwoDecimals(sale.cardAmountDop)).append('|')
                append(formatFixedTwoDecimals(sale.creditSaleAmountDop)).append('|')
                append("0.00|0.00|0.00")
                append("\r\n")
            }
        }
        return content.encodeToByteArray()
    }

    private fun StringBuilder.appendCsvRow(columns: List<String>) {
        for (columnIndex in columns.indices) {
            if (columnIndex > 0) {
                append(',')
            }
            val escapedCell = escapeCsvCell(columns[columnIndex])
            append(escapedCell)
        }
        append("\r\n")
    }

    private fun escapeCsvCell(rawCell: String): String {
        val sanitized = neutralizeFormulaPrefix(rawCell.trim())
        val hasComma = sanitized.indexOf(',') >= 0
        val hasQuote = sanitized.indexOf('"') >= 0
        val hasLineFeed = sanitized.indexOf('\n') >= 0
        val hasCarriageReturn = sanitized.indexOf('\r') >= 0
        var requiresQuoting = false
        if (hasComma) requiresQuoting = true
        if (hasQuote) requiresQuoting = true
        if (hasLineFeed) requiresQuoting = true
        if (hasCarriageReturn) requiresQuoting = true

        if (!requiresQuoting) return sanitized
        val doubledQuotes = sanitized.replace("\"", "\"\"")
        return "\"$doubledQuotes\""
    }

    private fun neutralizeFormulaPrefix(cell: String): String {
        if (cell.isEmpty()) return ""
        val firstChar = cell[0]
        if (firstChar == '=') return "'$cell"
        if (firstChar == '@') return "'$cell"
        if (firstChar == '+') return "'$cell"
        if (firstChar == '-') {
            val isNumericLiteral = cell.drop(1).all { character ->
                character.isDigit() || character == '.'
            }
            val hasDigits = cell.length > 1
            if (hasDigits) {
                if (isNumericLiteral) return cell
            }
            return "'$cell"
        }
        return cell
    }

    private fun inferDgiiIdentificationCode(rawTaxId: String): String {
        val digits = digitsOnly(rawTaxId)
        if (digits.length == 9) return DgiiIdentificationType.RNC.dgiiCode
        if (digits.length == 11) return DgiiIdentificationType.CEDULA.dgiiCode
        return DgiiIdentificationType.PASSPORT.dgiiCode
    }

    private fun formatFixedTwoDecimals(amountDop: Long): String = "$amountDop.00"

    private fun digitsOnly(raw: String): String = buildString {
        for (character in raw) {
            if (character.isDigit()) {
                append(character)
            }
        }
    }

    private fun digitsAndLettersOnly(raw: String): String = buildString {
        for (character in raw.trim()) {
            if (character.isLetterOrDigit()) {
                append(character)
            }
        }
    }
}
