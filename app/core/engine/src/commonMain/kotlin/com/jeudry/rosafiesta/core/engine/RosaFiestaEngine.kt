package com.jeudry.rosafiesta.core.engine

/**
 * Vista activa dentro del ecosistema RosaFiesta (`A30`).
 */
public enum class AppView {
    PERSONAL,
    ADMIN,
}

/**
 * Rol dentro de RosaFiesta (`A28`, `A30`).
 */
public enum class UserRole {
    CLIENT,
    ADMIN_OWNER,
    STAFF_LOGISTICS,
    STAFF_DECORATION,
    STAFF_WAREHOUSE,
}

/**
 * Permisos granulares del equipo operativo (`A28`).
 */
public enum class StaffPermission {
    EVENTS,
    DECORATION,
    TASKS,
    INVENTORY,
    PAYMENTS,
    REFUNDS,
    REPORTS,
    SETTINGS,
}

/**
 * Resultado del cálculo de cotización con anticipo del 30% y saldo del 70%.
 */
public data class QuoteCalculation(
    public val subtotalDop: Int,
    public val deliveryFeeDop: Int,
    public val discountDop: Int,
    public val totalDop: Int,
    public val depositPercent: Int,
    public val depositDop: Int,
    public val balanceDop: Int,
)

/**
 * Tramo de la política de cancelación (`s37`, `s76`).
 */
public enum class CancellationTier {
    MORE_THAN_SEVEN_DAYS_90_PERCENT,
    THREE_TO_SEVEN_DAYS_50_PERCENT,
    LESS_THAN_THREE_DAYS_NO_REFUND,
}

/**
 * Desglose de reembolso por cancelación de evento (`s37`, `s76`).
 */
public data class CancellationRefund(
    public val tier: CancellationTier,
    public val refundPercent: Int,
    public val refundDop: Int,
    public val retainedDop: Int,
)

/**
 * Resultado de evaluación de cambio de fecha (`s36`).
 */
public data class DateChangeResult(
    public val allowed: Boolean,
    public val isFree: Boolean,
    public val feeDop: Int,
    public val message: String,
)

/**
 * Renglón sugerido por el asistente Rosa IA (`s46`, `s47`, `A17`).
 */
public data class SuggestedLine(
    public val code: String,
    public val name: String,
    public val quantity: Int,
    public val unitPriceDop: Int,
) {
    public val subtotalDop: Int
        get() = quantity * unitPriceDop
}

/**
 * Propuesta completa de moodboard y cotización generada por Rosa IA (`s46`–`s48`, `A16`–`A18`).
 */
public data class RosaProposal(
    public val step: Int,
    public val eventType: String,
    public val styleName: String,
    public val paletteHex: List<String>,
    public val guests: Int,
    public val lines: List<SuggestedLine>,
    public val totalDop: Int,
    public val depositDop: Int,
    public val balanceDop: Int,
)

/**
 * Three-day logistics window around an event date (`pickup = event - 1d`, `return = event + 1d`) in ISO `YYYY-MM-DD`.
 */
public data class TurnaroundWindow(
    public val eventDateIso: String,
    public val pickupDateIso: String,
    public val returnDateIso: String,
)

/**
 * Reservation slice spanning a pickup-to-return window for date-range stock availability checks.
 */
public data class DateRangeReservation(
    public val articleId: String,
    public val eventId: String,
    public val quantity: Int,
    public val pickupDateIso: String,
    public val returnDateIso: String,
    public val status: String,
)

/**
 * Dominican fiscal breakdown separating taxable subtotal, 18% ITBIS, and refundable deposit.
 */
public data class FiscalBreakdown(
    public val totalDop: Int,
    public val subtotalDop: Int,
    public val itbisDop: Int,
    public val securityDepositDop: Int,
    public val ncfType: String,
    public val rncDigits: String,
)

/**
 * Portable 2D canvas element descriptor for bidirectional sketch and quote line synchronization.
 */
public data class SketchElementDescriptor(
    public val id: String,
    public val furnitureType: String,
    public val label: String = "",
    public val x: Float = 0f,
    public val y: Float = 0f,
    public val isAutoPlaced: Boolean = false,
    public val unitCount: Int = 1,
)

/**
 * Canonical catalog article mapping produced when synchronizing a 2D event sketch to quote lines.
 */
public data class SyncedLineDescriptor(
    public val articleId: String,
    public val furnitureType: String,
    public val title: String,
    public val quantity: Int,
)

/**
 * Motor puro de formato monetario y reglas de negocio de RosaFiesta.
 */
public object RosaFiestaEngine {
    public const val DEFAULT_DEPOSIT_PERCENT: Int = 30
    public const val SUBSEQUENT_DATE_CHANGE_FEE_DOP: Int = 750

    /**
     * Formatea un monto entero en pesos dominicanos con separador de miles (`RD$ 4,500`).
     */
    public fun formatDop(amountDop: Int): String {
        val negative = amountDop < 0
        val digits = kotlin.math.abs(amountDop).toString()
        val grouped = buildString {
            for (i in digits.indices) {
                if (i > 0 && (digits.length - i) % 3 == 0) {
                    append(',')
                }
                append(digits[i])
            }
        }
        return if (negative) "-RD$ $grouped" else "RD$ $grouped"
    }

    /**
     * Formatea el precio de alquiler por día de un artículo (`RD$ 4/día`).
     */
    public fun formatDailyRateDop(pricePerDayDop: Int): String = "${formatDop(pricePerDayDop)}/día"

    /**
     * Calcula el total, anticipo del 30% y saldo pendiente (70%) de una cotización.
     */
    public fun calculateQuote(
        lineSubtotalsDop: List<Int>,
        deliveryFeeDop: Int = 0,
        discountDop: Int = 0,
    ): QuoteCalculation {
        val subtotal = lineSubtotalsDop.filter { it > 0 }.sum()
        val total = (subtotal + deliveryFeeDop - discountDop).coerceAtLeast(0)
        val deposit = (total * DEFAULT_DEPOSIT_PERCENT) / 100
        val balance = (total - deposit).coerceAtLeast(0)
        return QuoteCalculation(
            subtotalDop = subtotal,
            deliveryFeeDop = deliveryFeeDop,
            discountDop = discountDop,
            totalDop = total,
            depositPercent = DEFAULT_DEPOSIT_PERCENT,
            depositDop = deposit,
            balanceDop = balance,
        )
    }

    /**
     * Aplica la política oficial de cancelación:
     * - > 7 días: 90% de reembolso del anticipo.
     * - 3 a 7 días: 50% de reembolso del anticipo.
     * - < 3 días: 0% de reembolso.
     */
    public fun calculateCancellationRefund(paidDepositDop: Int, daysUntilEvent: Int): CancellationRefund {
        val safeDeposit = paidDepositDop.coerceAtLeast(0)
        return when {
            daysUntilEvent > 7 -> {
                val refund = (safeDeposit * 90) / 100
                CancellationRefund(
                    tier = CancellationTier.MORE_THAN_SEVEN_DAYS_90_PERCENT,
                    refundPercent = 90,
                    refundDop = refund,
                    retainedDop = safeDeposit - refund,
                )
            }

            daysUntilEvent >= 3 -> {
                val refund = (safeDeposit * 50) / 100
                CancellationRefund(
                    tier = CancellationTier.THREE_TO_SEVEN_DAYS_50_PERCENT,
                    refundPercent = 50,
                    refundDop = refund,
                    retainedDop = safeDeposit - refund,
                )
            }

            else -> CancellationRefund(
                tier = CancellationTier.LESS_THAN_THREE_DAYS_NO_REFUND,
                refundPercent = 0,
                refundDop = 0,
                retainedDop = safeDeposit,
            )
        }
    }

    /**
     * Evalúa el cambio de fecha de un evento (`s36`): primer cambio gratis con al menos 48h de antelación.
     */
    public fun evaluateDateChange(daysUntilEvent: Int, priorChangesCount: Int): DateChangeResult {
        if (daysUntilEvent < 2) {
            return DateChangeResult(
                allowed = false,
                isFree = false,
                feeDop = 0,
                message = "No se permiten cambios de fecha a menos de 48 horas del evento.",
            )
        }
        if (priorChangesCount <= 0) {
            return DateChangeResult(
                allowed = true,
                isFree = true,
                feeDop = 0,
                message = "Primer cambio de fecha gratis sujeto a disponibilidad.",
            )
        }
        return DateChangeResult(
            allowed = true,
            isFree = false,
            feeDop = SUBSEQUENT_DATE_CHANGE_FEE_DOP,
            message = "Cambio adicional con cargo administrativo de ${formatDop(SUBSEQUENT_DATE_CHANGE_FEE_DOP)}.",
        )
    }

    /**
     * Devuelve los permisos activos de cada rol en `A28`.
     * Yuli (`STAFF_DECORATION`) tiene acceso a decoración y tareas, sin acceso a pagos ni reembolsos.
     */
    public fun permissionsForRole(role: UserRole): Set<StaffPermission> = when (role) {
        UserRole.ADMIN_OWNER -> StaffPermission.entries.toSet()

        UserRole.STAFF_LOGISTICS -> setOf(
            StaffPermission.EVENTS,
            StaffPermission.TASKS,
            StaffPermission.INVENTORY,
        )

        UserRole.STAFF_DECORATION -> setOf(
            StaffPermission.DECORATION,
            StaffPermission.TASKS,
        )

        UserRole.STAFF_WAREHOUSE -> setOf(
            StaffPermission.INVENTORY,
            StaffPermission.TASKS,
        )

        UserRole.CLIENT -> emptySet()
    }

    /**
     * Determina si un usuario con el rol dado puede alternar a la vista solicitada (`A30`).
     */
    public fun canAccessView(role: UserRole, targetView: AppView): Boolean = when (targetView) {
        AppView.PERSONAL -> true
        AppView.ADMIN -> role != UserRole.CLIENT
    }

    /**
     * Determina si un cliente puede eliminar su cuenta (`s88`).
     * Si tiene eventos activos confirmados o en curso, debe liquidarlos o cancelarlos primero.
     */
    public fun canDeleteAccount(activeEventsCount: Int): Boolean = activeEventsCount <= 0

    /**
     * Construye una propuesta de moodboard y artículos sugeridos por Rosa IA (`s40`–`s48`, `A16`–`A18`).
     */
    public fun buildRosaProposal(eventType: String, guests: Int, includeSweetTable: Boolean = true): RosaProposal {
        val safeGuests = guests.coerceAtLeast(20)
        val roundTables = (safeGuests + 7) / 8
        val centerpieces = (roundTables / 2).coerceAtLeast(4)
        val baseLines = buildList {
            add(SuggestedLine(code = "RF-001", name = "Silla Tiffany dorada", quantity = safeGuests, unitPriceDop = 4))
            add(SuggestedLine(code = "RF-003", name = "Mesa redonda 8 personas", quantity = roundTables, unitPriceDop = 25))
            add(SuggestedLine(code = "RF-004", name = "Centro de mesa floral", quantity = centerpieces, unitPriceDop = 35))
            add(SuggestedLine(code = "RF-005", name = "Arco orgánico rosa", quantity = 1, unitPriceDop = 95))
            if (includeSweetTable) {
                add(SuggestedLine(code = "RF-006", name = "Mesa de dulces / helados", quantity = 1, unitPriceDop = 150))
            }
        }
        val quote = calculateQuote(baseLines.map { it.subtotalDop })
        return RosaProposal(
            step = 7,
            eventType = eventType.ifBlank { "Boda Real Rosa & Dorado" },
            styleName = "Rosa Empolvado, Dorado Cálido & Cristal",
            paletteHex = listOf("#EA4EA9", "#FFD45C", "#FDE6F2", "#1A1A2E"),
            guests = safeGuests,
            lines = baseLines,
            totalDop = quote.totalDop,
            depositDop = quote.depositDop,
            balanceDop = quote.balanceDop,
        )
    }

    /**
     * Parses `YYYY-MM-DD`, `DD/MM/YYYY`, or Spanish month labels into a 3-day logistics turnaround window.
     */
    public fun computeTurnaroundWindow(rawDate: String): TurnaroundWindow? {
        val eventDate = parseCalendarDate(rawDate) ?: return null
        val pickupDate = shiftCalendarDay(eventDate, -1) ?: return null
        val returnDate = shiftCalendarDay(eventDate, 1) ?: return null
        return TurnaroundWindow(
            eventDateIso = eventDate.toIsoString(),
            pickupDateIso = pickupDate.toIsoString(),
            returnDateIso = returnDate.toIsoString(),
        )
    }

    /**
     * Returns `true` when two inclusive date windows overlap chronologically.
     */
    public fun rangesOverlap(
        startA: String,
        endA: String,
        startB: String,
        endB: String,
    ): Boolean {
        val parsedStartA = parseCalendarDate(startA) ?: return false
        val parsedEndA = parseCalendarDate(endA) ?: return false
        val parsedStartB = parseCalendarDate(startB) ?: return false
        val parsedEndB = parseCalendarDate(endB) ?: return false
        val rangeStartA = minCalendarDate(parsedStartA, parsedEndA)
        val rangeEndA = maxCalendarDate(parsedStartA, parsedEndA)
        val rangeStartB = minCalendarDate(parsedStartB, parsedEndB)
        val rangeEndB = maxCalendarDate(parsedStartB, parsedEndB)
        val endsBeforeOtherStarts = rangeEndA.sortKey() < rangeStartB.sortKey()
        if (endsBeforeOtherStarts) return false
        val otherEndsBeforeStarts = rangeEndB.sortKey() < rangeStartA.sortKey()
        if (otherEndsBeforeStarts) return false
        return true
    }

    /**
     * Computes remaining rentable stock for [articleId] across [pickupDateIso]..[returnDateIso].
     */
    public fun computeAvailableStockForRange(
        totalStock: Int,
        damagedStock: Int,
        reservations: List<DateRangeReservation>,
        articleId: String,
        pickupDateIso: String,
        returnDateIso: String,
        excludeEventId: String = "",
    ): Int {
        val safeDamaged = damagedStock.coerceAtLeast(0)
        val usableStock = (totalStock - safeDamaged).coerceAtLeast(0)
        if (articleId.isBlank()) return usableStock
        val hasValidTargetWindow = rangesOverlap(pickupDateIso, returnDateIso, pickupDateIso, returnDateIso)
        if (!hasValidTargetWindow) return usableStock
        var reservedUnits = 0
        for (reservation in reservations) {
            if (reservation.articleId != articleId) continue
            val isExcluded = isExcludedReservationEvent(reservation.eventId, excludeEventId)
            if (isExcluded) continue
            if (!isBlockingReservationStatus(reservation.status)) continue
            val overlapsTarget = rangesOverlap(
                pickupDateIso,
                returnDateIso,
                reservation.pickupDateIso,
                reservation.returnDateIso,
            )
            if (!overlapsTarget) continue
            reservedUnits += reservation.quantity.coerceAtLeast(0)
        }
        return (usableStock - reservedUnits).coerceAtLeast(0)
    }

    /**
     * Validates a Dominican NCF fiscal tax ID (`B02` requires none; `B01` requires 9-digit RNC or 11-digit Cedula).
     */
    public fun validateFiscalTaxId(
        ncfType: String,
        rawTaxId: String,
    ): Boolean {
        val normalizedType = ncfType.trim().uppercase()
        if (normalizedType == "B02") return true
        if (normalizedType != "B01") return false
        val digits = extractDigits(rawTaxId)
        val hasNonZero = digits.any { character -> character != '0' }
        if (!hasNonZero) return false
        if (digits.length == 9) return true
        return digits.length == 11
    }

    /**
     * Extracts taxable subtotal and 18% ITBIS from [totalDop] after subtracting [securityDepositDop].
     */
    public fun computeFiscalBreakdown(
        totalDop: Int,
        ncfType: String = "B02",
        rawTaxId: String = "",
        securityDepositDop: Int = 0,
    ): FiscalBreakdown {
        val safeTotal = totalDop.coerceAtLeast(0)
        val safeDeposit = securityDepositDop.coerceIn(0, safeTotal)
        val normalizedNcf = ncfType.trim().uppercase().ifBlank { "B02" }
        val rncDigits = extractDigits(rawTaxId)
        val taxableDop = (safeTotal - safeDeposit).coerceAtLeast(0)
        val subtotalDop = if (taxableDop == 0) {
            0
        } else {
            ((taxableDop.toLong() * 100L + 59L) / 118L).toInt()
        }
        val itbisDop = taxableDop - subtotalDop
        return FiscalBreakdown(
            totalDop = safeTotal,
            subtotalDop = subtotalDop,
            itbisDop = itbisDop,
            securityDepositDop = safeDeposit,
            ncfType = normalizedNcf,
            rncDigits = rncDigits,
        )
    }

    /**
     * Maps placed 2D sketch furniture elements into canonical catalog line counts.
     */
    public fun syncSketchElementsToArticleCounts(
        elements: List<SketchElementDescriptor>,
    ): List<SyncedLineDescriptor> {
        val quantitiesByArticleId = linkedMapOf<String, Int>()
        val mappingsByArticleId = linkedMapOf<String, SketchFurnitureMapping>()
        for (element in elements) {
            val mapping = resolveFurnitureMapping(element.furnitureType) ?: continue
            val existingCount = quantitiesByArticleId[mapping.articleId] ?: 0
            val elementUnits = element.unitCount.coerceAtLeast(1)
            quantitiesByArticleId[mapping.articleId] = existingCount + elementUnits
            mappingsByArticleId[mapping.articleId] = mapping
        }
        return quantitiesByArticleId.entries.mapNotNull { entry ->
            val mapping = mappingsByArticleId[entry.key] ?: return@mapNotNull null
            SyncedLineDescriptor(
                articleId = mapping.articleId,
                furnitureType = mapping.canonicalType,
                title = mapping.defaultTitle,
                quantity = entry.value,
            )
        }
    }

    /**
     * Synchronizes target article/furniture quantities onto [existingElements] without moving existing items.
     */
    public fun syncArticleCountsToSketchElements(
        existingElements: List<SketchElementDescriptor>,
        itemCounts: Map<String, Int>,
    ): List<SketchElementDescriptor> {
        val targetByArticleId = aggregateTargetArticleCounts(itemCounts)
        val trimmedElements = trimExcessAutoPlacedElements(existingElements, targetByArticleId)
        val currentCounts = countExistingUnitsByArticleId(trimmedElements)
        val occupiedIds = trimmedElements.map { element -> element.id }.toMutableSet()
        val result = trimmedElements.toMutableList()
        var gridSlotIndex = 0
        for (entry in targetByArticleId.entries) {
            val mapping = resolveMappingByArticleId(entry.key) ?: continue
            val currentCount = currentCounts[mapping.articleId] ?: 0
            val missingCount = entry.value - currentCount
            if (missingCount <= 0) continue
            for (additionIndex in 0 until missingCount) {
                val placement = nextFreeGridPosition(result, gridSlotIndex)
                gridSlotIndex = placement.nextSlotIndex
                val newId = nextUniqueAutoElementId(mapping.articleId, occupiedIds)
                occupiedIds.add(newId)
                result.add(
                    SketchElementDescriptor(
                        id = newId,
                        furnitureType = mapping.canonicalType,
                        label = mapping.defaultTitle,
                        x = placement.x,
                        y = placement.y,
                        isAutoPlaced = true,
                        unitCount = 1,
                    ),
                )
            }
        }
        return result
    }

    private data class CalendarDate(
        val year: Int,
        val month: Int,
        val day: Int,
    ) {
        fun sortKey(): Int = (year * 10000) + (month * 100) + day

        fun toIsoString(): String = "${padLeft(year, 4)}-${padLeft(month, 2)}-${padLeft(day, 2)}"
    }

    private data class SketchFurnitureMapping(
        val canonicalType: String,
        val articleId: String,
        val defaultTitle: String,
        val aliases: Set<String>,
    )

    private val sketchFurnitureMappings: List<SketchFurnitureMapping> = listOf(
        SketchFurnitureMapping(
            canonicalType = "MESA_REDONDA",
            articleId = "art-mesa-redonda",
            defaultTitle = "Mesa redonda para 8",
            aliases = setOf("MESA_REDONDA", "ROUND_TABLE"),
        ),
        SketchFurnitureMapping(
            canonicalType = "MESA_IMPERIAL",
            articleId = "art-mesa-plegable",
            defaultTitle = "Mesa rectangular plegable",
            aliases = setOf("MESA_IMPERIAL", "RECT_TABLE", "MESA_PLEGABLE", "MESA_RECTANGULAR"),
        ),
        SketchFurnitureMapping(
            canonicalType = "SILLA_TIFFANY",
            articleId = "art-silla-tiffany",
            defaultTitle = "Silla Tiffany dorada",
            aliases = setOf("SILLA_TIFFANY", "SINGLE_CHAIR"),
        ),
        SketchFurnitureMapping(
            canonicalType = "ARCO_GLOBOS",
            articleId = "art-arco-rosa",
            defaultTitle = "Arco orgánico de globos",
            aliases = setOf("ARCO_GLOBOS", "ARCH_STAGE", "ARCO_ROSA"),
        ),
        SketchFurnitureMapping(
            canonicalType = "TARIMA",
            articleId = "ext-tarima-alfombrada",
            defaultTitle = "Tarima principal alfombrada",
            aliases = setOf("TARIMA", "TARIMA_PRINCIPAL"),
        ),
        SketchFurnitureMapping(
            canonicalType = "MESA_DULCE",
            articleId = "art-mesa-helados",
            defaultTitle = "Mesa de dulces / helados",
            aliases = setOf("MESA_DULCE", "MESA_HELADOS", "CANDY_TABLE"),
        ),
    )

    private data class GridPlacement(
        val x: Float,
        val y: Float,
        val nextSlotIndex: Int,
    )

    private fun isExcludedReservationEvent(
        reservationEventId: String,
        excludeEventId: String,
    ): Boolean {
        if (excludeEventId.isBlank()) return false
        return reservationEventId == excludeEventId
    }

    private fun isBlockingReservationStatus(status: String): Boolean {
        val normalized = status.trim().lowercase()
        if (normalized == "held") return true
        return normalized == "confirmed"
    }

    private fun parseCalendarDate(rawDate: String): CalendarDate? {
        val trimmed = rawDate.trim()
        if (trimmed.isEmpty()) return null
        val isoCandidate = parseIsoDate(trimmed)
        if (isoCandidate != null) return isoCandidate
        val slashCandidate = parseSlashOrDmyDate(trimmed)
        if (slashCandidate != null) return slashCandidate
        return parseMonthLabelDate(trimmed)
    }

    private fun parseIsoDate(input: String): CalendarDate? {
        val dateToken = input.substringBefore('T').trim()
        val parts = dateToken.split('-')
        if (parts.size != 3) return null
        val yearText = parts[0]
        if (yearText.length != 4) return null
        val year = yearText.toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null
        return buildValidatedDate(year, month, day)
    }

    private fun parseSlashOrDmyDate(input: String): CalendarDate? {
        val separator = if ('/' in input) {
            '/'
        } else if ('-' in input) {
            '-'
        } else {
            return null
        }
        val parts = input.split(separator)
        if (parts.size != 3) return null
        val yearText = parts[2].trim()
        if (yearText.length != 4) return null
        val day = parts[0].trim().toIntOrNull() ?: return null
        val month = parts[1].trim().toIntOrNull() ?: return null
        val year = yearText.toIntOrNull() ?: return null
        return buildValidatedDate(year, month, day)
    }

    private fun parseMonthLabelDate(input: String): CalendarDate? {
        val normalized = input
            .lowercase()
            .replace(",", " ")
            .replace(".", " ")
        val rawTokens = normalized.split(' ').filter { token -> token.isNotBlank() }
        val tokens = rawTokens.filter { token -> token != "de" && token != "del" }
        if (tokens.size < 3) return null
        val day = tokens[0].toIntOrNull() ?: return null
        val month = monthNumberFromLabel(tokens[1]) ?: return null
        val yearText = tokens[2]
        if (yearText.length != 4) return null
        val year = yearText.toIntOrNull() ?: return null
        return buildValidatedDate(year, month, day)
    }

    private fun monthNumberFromLabel(token: String): Int? = when (token.trim().lowercase()) {
        "ene", "enero", "jan", "january" -> 1
        "feb", "febrero", "february" -> 2
        "mar", "marzo", "march" -> 3
        "abr", "abril", "apr", "april" -> 4
        "may", "mayo" -> 5
        "jun", "junio", "june" -> 6
        "jul", "julio", "july" -> 7
        "ago", "agosto", "aug", "august" -> 8
        "sep", "sept", "septiembre", "set", "setiembre", "september" -> 9
        "oct", "octubre", "october" -> 10
        "nov", "noviembre", "november" -> 11
        "dic", "diciembre", "dec", "december" -> 12
        else -> null
    }

    private fun buildValidatedDate(
        year: Int,
        month: Int,
        day: Int,
    ): CalendarDate? {
        val isYearOutOfRange = year < 1 || year > 9999
        if (isYearOutOfRange) return null
        val maxDay = daysInGregorianMonth(year, month)
        if (maxDay == 0) return null
        val isDayOutOfRange = day < 1 || day > maxDay
        if (isDayOutOfRange) return null
        return CalendarDate(year = year, month = month, day = day)
    }

    private fun shiftCalendarDay(
        date: CalendarDate,
        deltaDays: Int,
    ): CalendarDate? {
        if (deltaDays == 0) return date
        if (deltaDays < 0) {
            if (date.day > 1) return date.copy(day = date.day - 1)
            val prevMonth = if (date.month == 1) 12 else date.month - 1
            val prevYear = if (date.month == 1) date.year - 1 else date.year
            val lastDayOfPrevMonth = daysInGregorianMonth(prevYear, prevMonth)
            return buildValidatedDate(prevYear, prevMonth, lastDayOfPrevMonth)
        }
        val currentMonthDays = daysInGregorianMonth(date.year, date.month)
        if (date.day < currentMonthDays) return date.copy(day = date.day + 1)
        val nextMonth = if (date.month == 12) 1 else date.month + 1
        val nextYear = if (date.month == 12) date.year + 1 else date.year
        return buildValidatedDate(nextYear, nextMonth, 1)
    }

    private fun isGregorianLeapYear(year: Int): Boolean {
        if (year % 400 == 0) return true
        if (year % 100 == 0) return false
        return year % 4 == 0
    }

    private fun daysInGregorianMonth(
        year: Int,
        month: Int,
    ): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isGregorianLeapYear(year)) 29 else 28
        else -> 0
    }

    private fun minCalendarDate(
        first: CalendarDate,
        second: CalendarDate,
    ): CalendarDate = if (first.sortKey() <= second.sortKey()) first else second

    private fun maxCalendarDate(
        first: CalendarDate,
        second: CalendarDate,
    ): CalendarDate = if (first.sortKey() >= second.sortKey()) first else second

    private fun padLeft(
        value: Int,
        width: Int,
    ): String = value.toString().padStart(width, '0')

    private fun extractDigits(rawValue: String): String {
        val builder = StringBuilder()
        for (character in rawValue) {
            if (character.isDigit()) {
                builder.append(character)
            }
        }
        return builder.toString()
    }

    private fun resolveFurnitureMapping(rawType: String): SketchFurnitureMapping? {
        val normalized = rawType.trim().uppercase()
        return sketchFurnitureMappings.firstOrNull { mapping -> normalized in mapping.aliases }
    }

    private fun resolveMappingByArticleId(articleId: String): SketchFurnitureMapping? {
        val normalizedId = articleId.trim().lowercase()
        val directMatch = sketchFurnitureMappings.firstOrNull { mapping -> mapping.articleId == normalizedId }
        if (directMatch != null) return directMatch
        return resolveFurnitureMapping(articleId)
    }

    private fun aggregateTargetArticleCounts(
        itemCounts: Map<String, Int>,
    ): Map<String, Int> {
        val targetByArticleId = linkedMapOf<String, Int>()
        for (entry in itemCounts.entries) {
            val mapping = resolveMappingByArticleId(entry.key) ?: continue
            val current = targetByArticleId[mapping.articleId] ?: 0
            targetByArticleId[mapping.articleId] = current + entry.value.coerceAtLeast(0)
        }
        return targetByArticleId
    }

    private fun countExistingUnitsByArticleId(
        elements: List<SketchElementDescriptor>,
    ): Map<String, Int> {
        val counts = linkedMapOf<String, Int>()
        for (element in elements) {
            val mapping = resolveFurnitureMapping(element.furnitureType) ?: continue
            val existing = counts[mapping.articleId] ?: 0
            counts[mapping.articleId] = existing + element.unitCount.coerceAtLeast(1)
        }
        return counts
    }

    private fun trimExcessAutoPlacedElements(
        existingElements: List<SketchElementDescriptor>,
        targetByArticleId: Map<String, Int>,
    ): List<SketchElementDescriptor> {
        val currentCounts = countExistingUnitsByArticleId(existingElements).toMutableMap()
        val dropIndices = mutableSetOf<Int>()
        for (index in existingElements.indices.reversed()) {
            val element = existingElements[index]
            if (!element.isAutoPlaced) continue
            val mapping = resolveFurnitureMapping(element.furnitureType) ?: continue
            val currentCount = currentCounts[mapping.articleId] ?: 0
            val targetCount = targetByArticleId[mapping.articleId] ?: 0
            if (currentCount > targetCount) {
                dropIndices.add(index)
                currentCounts[mapping.articleId] = currentCount - element.unitCount.coerceAtLeast(1)
            }
        }
        if (dropIndices.isEmpty()) return existingElements
        val remainingElements = ArrayList<SketchElementDescriptor>(existingElements.size)
        for (index in existingElements.indices) {
            if (index !in dropIndices) {
                remainingElements.add(existingElements[index])
            }
        }
        return remainingElements
    }

    private fun nextFreeGridPosition(
        elements: List<SketchElementDescriptor>,
        startSlotIndex: Int,
    ): GridPlacement {
        val maxSearchSlots = startSlotIndex + elements.size + 64
        for (slot in startSlotIndex until maxSearchSlots) {
            val column = slot % 5
            val row = slot / 5
            val candidateX = 60f + (column * 110f)
            val candidateY = 60f + (row * 110f)
            val occupied = isPositionOccupied(elements, candidateX, candidateY)
            if (!occupied) {
                return GridPlacement(x = candidateX, y = candidateY, nextSlotIndex = slot + 1)
            }
        }
        val fallbackColumn = maxSearchSlots % 5
        val fallbackRow = maxSearchSlots / 5
        return GridPlacement(
            x = 60f + (fallbackColumn * 110f),
            y = 60f + (fallbackRow * 110f),
            nextSlotIndex = maxSearchSlots + 1,
        )
    }

    private fun isPositionOccupied(
        elements: List<SketchElementDescriptor>,
        targetX: Float,
        targetY: Float,
    ): Boolean {
        for (element in elements) {
            val deltaX = absFloat(element.x - targetX)
            val deltaY = absFloat(element.y - targetY)
            val isClose = deltaX < 24f && deltaY < 24f
            if (isClose) return true
        }
        return false
    }

    private fun absFloat(value: Float): Float = if (value < 0f) -value else value

    private fun nextUniqueAutoElementId(
        articleId: String,
        occupiedIds: Set<String>,
    ): String {
        val maxSuffix = occupiedIds.size + 1000
        for (suffix in 1..maxSuffix) {
            val candidate = "auto-$articleId-$suffix"
            if (candidate !in occupiedIds) return candidate
        }
        return "auto-$articleId-${maxSuffix + 1}"
    }
}
