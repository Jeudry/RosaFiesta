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

    // Márgenes de pantalla: 16 px a los lados; contenido desde y = 44 (debajo de la barra de estado)
    public val ScreenHorizontalPadding: Dp = 16.dp
    public val StatusBarHeight: Dp = 44.dp
    public val BlockSpacing: Dp = 10.dp

    // Tarjeta: radio 16
    public val CardRadius: Dp = 16.dp
    public val HeroCardRadius: Dp = 16.dp

    // Botón: alto 46, radio 14, texto 14.5 / 700
    public val ButtonRadius: Dp = 14.dp
    public val ButtonHeight: Dp = 46.dp
    public val BottomFixedButtonSideMargin: Dp = 16.dp
    public val BottomFixedButtonBottomMargin: Dp = 24.dp

    // Botón icono (ib): círculo de 38, fondo blanco
    public val IconButtonSize: Dp = 38.dp

    // Chip: alto 32, radio completo
    public val ChipHeight: Dp = 32.dp

    // Campo: alto 44, radio 13
    public val FieldHeight: Dp = 44.dp
    public val InputRadius: Dp = 13.dp

    // Mosaico de icono (ti): 34 × 34, radio 10, fondo de color suave
    public val IconTileSize: Dp = 34.dp
    public val IconTileRadius: Dp = 10.dp

    // Etiqueta (tag): alto 22, radio completo, 11.5 / 700
    public val TagHeight: Dp = 22.dp

    // Hoja inferior: radio 24 arriba, asa de 36 × 4, fondo atenuado rgba(30,20,50,.45)
    public val BottomSheetRadius: Dp = 24.dp
    public val BottomSheetHandleWidth: Dp = 36.dp
    public val BottomSheetHandleHeight: Dp = 4.dp

    // Barra inferior: alto 64; Rosa de 48 px en el centro, sobresaliendo 20 px
    public val BottomBarHeight: Dp = 64.dp
    public val BottomBarContentClearance: Dp = 90.dp
    public val RosaCenterButtonSize: Dp = 48.dp
    public val RosaCenterProtrusion: Dp = 20.dp

    // Interruptor: 42 × 24
    public val SwitchWidth: Dp = 42.dp
    public val SwitchHeight: Dp = 24.dp

    public val PhoneOuterRadius: Dp = 44.dp
    public val PhoneInnerRadius: Dp = 34.dp

    // Plataformas: Móvil 390 × 844 (iPhone 14/15), Web A39 1440 × 900
    public val PhoneFrameWidth: Dp = 390.dp
    public val PhoneFrameHeight: Dp = 844.dp
    public val WebFrameWidth: Dp = 1440.dp
    public val WebFrameHeight: Dp = 900.dp
}
