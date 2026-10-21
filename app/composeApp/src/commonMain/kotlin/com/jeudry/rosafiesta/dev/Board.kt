package com.jeudry.rosafiesta.dev

import androidx.compose.runtime.Composable

/**
 * Un tablero del sistema de diseño de RosaFiesta dibujado con datos canónicos de la Sección 6:
 * lo usan las pruebas de capturas y la galería interactiva del modo desarrollador.
 */
internal class Board(
    val code: String,
    val name: String,
    val isDesktop: Boolean = false,
    val content: @Composable () -> Unit,
)
