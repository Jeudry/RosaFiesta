package com.jeudry.rosafiesta.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Escala de forma, espaciado y medidas exactas (Sección 2.5 de `entrega-de-diseno.md`).
 */
public object RfDimens {
    public val Space4: Dp = 4.dp
    public val Space6: Dp = 6.dp
    public val Space8: Dp = 8.dp
    public val Space10: Dp = 10.dp
    public val Space12: Dp = 12.dp
    public val Space14: Dp = 14.dp
    public val Space16: Dp = 16.dp
    public val Space20: Dp = 20.dp
    public val Space24: Dp = 24.dp
    public val Space32: Dp = 32.dp

    // Márgenes de pantalla: 20 px a los lados; contenido desde y = 54 (debajo de la barra de estado)
    public val ScreenHorizontalPadding: Dp = 20.dp
    public val StatusBarHeight: Dp = 54.dp
    public val BlockSpacing: Dp = 12.dp

    // Tarjeta: radio 20
    public val CardRadius: Dp = 20.dp
    public val HeroCardRadius: Dp = 20.dp

    // Botón: alto 54, radio 16, texto 16 / 700
    public val ButtonRadius: Dp = 16.dp
    public val ButtonHeight: Dp = 54.dp
    public val BottomFixedButtonSideMargin: Dp = 20.dp
    public val BottomFixedButtonBottomMargin: Dp = 36.dp

    // Botón icono (ib): círculo de 44, fondo blanco
    public val IconButtonSize: Dp = 44.dp

    // Chip: alto 36, radio completo
    public val ChipHeight: Dp = 36.dp

    // Campo: alto 50–52, radio 14–16
    public val FieldHeight: Dp = 51.dp
    public val InputRadius: Dp = 15.dp

    // Mosaico de icono (ti): 40 × 40, radio 12, fondo de color suave
    public val IconTileSize: Dp = 40.dp
    public val IconTileRadius: Dp = 12.dp

    // Etiqueta (tag): alto 24, radio completo, 12 / 700
    public val TagHeight: Dp = 24.dp

    // Hoja inferior: radio 28 arriba, asa de 40 × 5, fondo atenuado rgba(30,20,50,.45)
    public val BottomSheetRadius: Dp = 28.dp
    public val BottomSheetHandleWidth: Dp = 40.dp
    public val BottomSheetHandleHeight: Dp = 5.dp

    // Barra inferior: alto 96; Rosa de 56 px en el centro, sobresaliendo 26 px
    public val BottomBarHeight: Dp = 96.dp
    public val RosaCenterButtonSize: Dp = 56.dp
    public val RosaCenterProtrusion: Dp = 26.dp

    // Interruptor: 46 × 28
    public val SwitchWidth: Dp = 46.dp
    public val SwitchHeight: Dp = 28.dp

    public val PhoneOuterRadius: Dp = 44.dp
    public val PhoneInnerRadius: Dp = 34.dp

    // Plataformas: Móvil 390 × 844 (iPhone 14/15), Web A39 1440 × 900
    public val PhoneFrameWidth: Dp = 390.dp
    public val PhoneFrameHeight: Dp = 844.dp
    public val WebFrameWidth: Dp = 1440.dp
    public val WebFrameHeight: Dp = 900.dp
}
