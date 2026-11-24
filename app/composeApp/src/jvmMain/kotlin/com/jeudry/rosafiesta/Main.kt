package com.jeudry.rosafiesta

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Punto de entrada de escritorio para la App Personal de RosaFiesta (Clientes). */
fun main() {
    System.setProperty("rosafiesta.profile", "personal")
    application {
        val windowState = rememberWindowState(width = 410.dp, height = 860.dp)
        RosaFiestaDesktopWindow(
            title = "RosaFiesta",
            isAdmin = false,
            state = windowState,
        ) {
            PersonalApp(initialScreenCode = "p00")
        }
    }
}
