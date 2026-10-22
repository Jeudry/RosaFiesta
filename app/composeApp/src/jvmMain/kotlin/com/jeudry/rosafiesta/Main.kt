package com.jeudry.rosafiesta

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jeudry.rosafiesta.core.engine.AppView

/** Punto de entrada de escritorio (macOS, Windows y Linux). */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RosaFiesta · Gestión Inteligente de Eventos",
        state = rememberWindowState(width = 1280.dp, height = 820.dp),
    ) {
        RosaFiestaApp(
            initialAppView = AppView.ADMIN,
            initialScreenCode = "A39",
            showDevHeader = false,
        )
    }
}
