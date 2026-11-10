package com.jeudry.rosafiesta

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jeudry.rosafiesta.core.engine.AppView
import kotlinx.browser.document
import kotlinx.browser.window

private val LocalDevHosts: Set<String> = setOf(
    "localhost",
    "127.0.0.1",
    "::1",
    "[::1]",
    "",
)

internal fun isLocalDevHost(hostname: String): Boolean = hostname.trim().lowercase() in LocalDevHosts

private fun resolveInitialWebRoute(
    hostname: String,
    search: String,
    rawHash: String,
): Pair<String, Boolean> {
    if (!isLocalDevHost(hostname)) {
        return "s14" to false
    }
    val hasDevQuery = search.contains("dev=true")
    val hasDevHash = rawHash == "dev"
    val showDev = if (hasDevQuery) true else hasDevHash

    val queryParamScreen = search.split("&")
        .firstOrNull { segment ->
            val startsClean = segment.startsWith("screen=")
            val startsPrefixed = segment.startsWith("?screen=")
            if (startsClean) true else startsPrefixed
        }
        ?.substringAfter("screen=")

    val hasQueryScreen = !queryParamScreen.isNullOrBlank()
    val isNonDevHash = rawHash != "dev"
    val hasDirectHashScreen = if (rawHash.isNotBlank()) isNonDevHash else false

    val initialScreen = when {
        hasQueryScreen -> queryParamScreen
        hasDirectHashScreen -> rawHash
        else -> "s14"
    }
    return initialScreen to showDev
}

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    val body = document.body ?: return
    val hostname = window.location.hostname
    val search = window.location.search
    val rawHash = window.location.hash.removePrefix("#")
    val (initialScreen, showDev) = resolveInitialWebRoute(
        hostname = hostname,
        search = search,
        rawHash = rawHash,
    )
    val initialView = if (initialScreen.startsWith("A")) AppView.ADMIN else AppView.PERSONAL

    ComposeViewport(body) {
        RosaFiestaApp(
            initialAppView = initialView,
            initialScreenCode = initialScreen,
            showDevHeader = showDev,
        )
    }
}
