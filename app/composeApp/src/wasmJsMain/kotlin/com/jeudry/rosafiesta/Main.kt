package com.jeudry.rosafiesta

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        RosaFiestaApp()
    }
}
