package com.jeudry.rosafiesta.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RfDesignSystemTest {
    @Test
    fun `verifica que los colores y variantes de tema personal y admin esten diferenciados`() {
        val personal = paletteForVariant(RfThemeVariant.PERSONAL)
        val admin = paletteForVariant(RfThemeVariant.ADMIN)
        val dark = paletteForVariant(RfThemeVariant.PERSONAL_DARK)

        assertFalse(personal.isAdmin)
        assertTrue(admin.isAdmin)
        assertTrue(dark.isDark)

        assertEquals(RfColors.PersonalInk, personal.ink)
        assertEquals(RfColors.AdminInk, admin.ink)
        assertEquals(RfColors.PersonalDarkBg, dark.bg)
    }

    @Test
    fun `verifica las 5 pestanas de TabBar personal y AdminBar administrativa`() {
        assertEquals(5, PersonalTab.entries.size)
        assertEquals("Rosa IA", PersonalTab.ROSA_IA.label)

        assertEquals(5, AdminTab.entries.size)
        assertEquals(2, AdminTab.ORDERS.badgeCount)
    }
}
