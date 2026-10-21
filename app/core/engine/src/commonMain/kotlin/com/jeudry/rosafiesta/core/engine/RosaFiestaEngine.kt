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
}
