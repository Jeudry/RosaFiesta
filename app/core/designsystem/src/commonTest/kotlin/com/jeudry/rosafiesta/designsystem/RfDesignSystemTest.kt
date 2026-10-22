package com.jeudry.rosafiesta.designsystem

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        assertEquals(RfColors.ToastPersonal, personal.toastBg)
        assertEquals(RfColors.ToastAdmin, admin.toastBg)
    }

    @Test
    fun `verifica las pestanas de TabBar personal modo cliente e invitado y AdminBar administrativa`() {
        assertEquals(
            listOf("Inicio", "Catálogo", "Rosa", "Eventos", "Favoritos"),
            PersonalTab.standardTabs.map { it.label },
        )
        assertEquals(
            listOf("Catálogo", "Favoritos", "Rosa", "Paquetes", "Entrar"),
            PersonalTab.guestTabs.map { it.label },
        )
        assertEquals(
            listOf("Inicio", "Eventos", "Rosa", "Inventario", "Más"),
            AdminTab.entries.map { it.label },
        )
        assertEquals(2, AdminTab.EVENTOS.badgeCount)
    }

    @Test
    fun `verifica la escala tipografica y medidas de la seccion 2 del documento de entrega`() {
        val typo = buildRfTypography(
            display = FontFamily.Default,
            body = FontFamily.Default,
            inkColor = RfColors.PersonalInk,
            mutedColor = RfColors.PersonalMuted,
        )

        assertEquals(26.sp, typo.rootTabTitle.fontSize)
        assertEquals(FontWeight.ExtraBold, typo.rootTabTitle.fontWeight)
        assertEquals(20.sp, typo.backScreenTitle.fontSize)
        assertEquals(22.sp, typo.sheetOrEmptyTitle.fontSize)
        assertEquals(34.sp, typo.bigAmount.fontSize)
        assertEquals(30.sp, typo.webKpi.fontSize)
        assertEquals(15.sp, typo.body.fontSize)
        assertEquals(12.sp, typo.sectionCap.fontSize)
        assertEquals(13.sp, typo.fieldLabel.fontSize)

        assertEquals(20.dp, RfDimens.ScreenHorizontalPadding)
        assertEquals(54.dp, RfDimens.StatusBarHeight)
        assertEquals(96.dp, RfDimens.BottomBarHeight)
        assertEquals(56.dp, RfDimens.RosaCenterButtonSize)
        assertEquals(26.dp, RfDimens.RosaCenterProtrusion)
        assertEquals(44.dp, RfDimens.IconButtonSize)
        assertEquals(46.dp, RfDimens.SwitchWidth)
        assertEquals(28.dp, RfDimens.SwitchHeight)
    }
}
