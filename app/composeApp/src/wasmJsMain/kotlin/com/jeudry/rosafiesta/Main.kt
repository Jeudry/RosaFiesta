package com.jeudry.rosafiesta

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jeudry.rosafiesta.core.engine.AppView
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    val body = document.body ?: return
    val search = window.location.search
    val rawHash = window.location.hash.removePrefix("#")
    val showDev = search.contains("dev=true") || rawHash == "dev"

    val queryParamScreen = search.split("&")
        .firstOrNull { it.startsWith("screen=") || it.startsWith("?screen=") }
        ?.substringAfter("screen=")
    val initialScreen = when {
        !queryParamScreen.isNullOrBlank() -> queryParamScreen
        rawHash.isNotBlank() && rawHash != "dev" -> rawHash
        else -> "s14"
    }
    val initialView = if (initialScreen.startsWith("A")) AppView.ADMIN else AppView.PERSONAL

    ComposeViewport(body) {
        RosaFiestaApp(
            initialAppView = initialView,
            initialScreenCode = initialScreen,
            showDevHeader = showDev,
        )
    }
}
