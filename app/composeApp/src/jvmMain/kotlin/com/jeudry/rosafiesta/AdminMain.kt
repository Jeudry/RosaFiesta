package com.jeudry.rosafiesta

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Punto de entrada independiente de escritorio para la App Administrativa de RosaFiesta (Negocio). */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RosaFiesta Admin",
        state = rememberWindowState(width = 420.dp, height = 870.dp),
    ) {
        AdminApp(initialScreenCode = "A00")
    }
}
