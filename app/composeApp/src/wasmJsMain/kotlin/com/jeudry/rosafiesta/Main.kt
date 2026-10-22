package com.jeudry.rosafiesta

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    val body = document.body ?: return
    val showDev = window.location.search.contains("dev=true") || window.location.hash == "#dev"
    ComposeViewport(body) {
        RosaFiestaApp(showDevHeader = showDev)
    }
}
