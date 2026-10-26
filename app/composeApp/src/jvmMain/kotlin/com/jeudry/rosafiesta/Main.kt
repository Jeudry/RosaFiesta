package com.jeudry.rosafiesta

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Punto de entrada de escritorio para la App Personal de RosaFiesta (Clientes). */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RosaFiesta",
        state = rememberWindowState(width = 410.dp, height = 860.dp),
    ) {
        PersonalApp(initialScreenCode = "p00")
    }
}
