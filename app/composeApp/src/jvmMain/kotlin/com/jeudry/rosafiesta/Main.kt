package com.jeudry.rosafiesta

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Punto de entrada de escritorio (macOS, Windows y Linux). */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RosaFiesta · App Personal y App Administrativa",
        state = rememberWindowState(width = 460.dp, height = 900.dp),
    ) {
        RosaFiestaApp()
    }
}
