package com.jeudry.rosafiesta.core.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RosaFiestaEngineTest {
    @Test
    fun `formatea montos en pesos dominicanos con separador de miles`() {
        assertEquals("RD$ 4,500", RosaFiestaEngine.formatDop(4500))
        assertEquals("RD$ 12,450", RosaFiestaEngine.formatDop(12450))
        assertEquals("RD$ 4/día", RosaFiestaEngine.formatDailyRateDop(4))
    }

    @Test
    fun `calcula anticipo del 30 por ciento y saldo del 70 por ciento`() {
        val calc = RosaFiestaEngine.calculateQuote(
            lineSubtotalsDop = listOf(480, 375, 280, 95, 150),
            deliveryFeeDop = 3120,
            discountDop = 0,
        )
        assertEquals(4500, calc.totalDop)
        assertEquals(1350, calc.depositDop)
        assertEquals(3150, calc.balanceDop)
    }

    @Test
    fun `aplica la politica de cancelacion en los tres tramos de dias`() {
        val moreThan7 = RosaFiestaEngine.calculateCancellationRefund(paidDepositDop = 1350, daysUntilEvent = 10)
        assertEquals(90, moreThan7.refundPercent)
        assertEquals(1215, moreThan7.refundDop)
        assertEquals(135, moreThan7.retainedDop)

        val threeTo7 = RosaFiestaEngine.calculateCancellationRefund(paidDepositDop = 1350, daysUntilEvent = 5)
        assertEquals(50, threeTo7.refundPercent)
        assertEquals(675, threeTo7.refundDop)

        val lessThan3 = RosaFiestaEngine.calculateCancellationRefund(paidDepositDop = 1350, daysUntilEvent = 2)
        assertEquals(0, lessThan3.refundPercent)
        assertEquals(0, lessThan3.refundDop)
        assertEquals(1350, lessThan3.retainedDop)
    }

    @Test
    fun `permite el primer cambio de fecha gratis y cobra a partir del segundo`() {
        val firstChange = RosaFiestaEngine.evaluateDateChange(daysUntilEvent = 5, priorChangesCount = 0)
        assertTrue(firstChange.allowed)
        assertTrue(firstChange.isFree)
        assertEquals(0, firstChange.feeDop)

        val secondChange = RosaFiestaEngine.evaluateDateChange(daysUntilEvent = 5, priorChangesCount = 1)
        assertTrue(secondChange.allowed)
        assertFalse(secondChange.isFree)
        assertEquals(750, secondChange.feeDop)

        val tooLate = RosaFiestaEngine.evaluateDateChange(daysUntilEvent = 1, priorChangesCount = 0)
        assertFalse(tooLate.allowed)
    }

    @Test
    fun `restringe pagos a Yuli y bloquea eliminacion de cuenta con eventos activos`() {
        val yuliPerms = RosaFiestaEngine.permissionsForRole(UserRole.STAFF_DECORATION)
        assertTrue(StaffPermission.DECORATION in yuliPerms)
        assertTrue(StaffPermission.TASKS in yuliPerms)
        assertFalse(StaffPermission.PAYMENTS in yuliPerms)

        assertTrue(RosaFiestaEngine.canAccessView(UserRole.ADMIN_OWNER, AppView.ADMIN))
        assertTrue(RosaFiestaEngine.canAccessView(UserRole.ADMIN_OWNER, AppView.PERSONAL))
        assertFalse(RosaFiestaEngine.canAccessView(UserRole.CLIENT, AppView.ADMIN))

        assertFalse(RosaFiestaEngine.canDeleteAccount(activeEventsCount = 2))
        assertTrue(RosaFiestaEngine.canDeleteAccount(activeEventsCount = 0))
    }

    @Test
    fun `genera propuesta de Rosa IA con cantidades proporcionales a invitados`() {
        val proposal = RosaFiestaEngine.buildRosaProposal(eventType = "Boda", guests = 120)
        assertEquals(7, proposal.step)
        assertEquals(120, proposal.guests)
        assertEquals(120, proposal.lines.first().quantity)
        assertTrue(proposal.totalDop > 0)
        assertTrue(proposal.depositDop > 0)
    }
}
