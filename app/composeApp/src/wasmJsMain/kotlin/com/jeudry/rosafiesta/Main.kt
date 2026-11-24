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

private fun hasQueryParam(
    search: String,
    paramName: String,
    expectedValue: String,
): Boolean {
    val cleanSearch = search.removePrefix("?")
    if (cleanSearch.isBlank()) return false
    return cleanSearch.split("&").any { segment ->
        val key = segment.substringBefore("=").trim().lowercase()
        val value = segment.substringAfter("=", missingDelimiterValue = "").trim().lowercase()
        val matchesKey = key == paramName.lowercase()
        val matchesValue = value == expectedValue.lowercase()
        if (matchesKey) matchesValue else false
    }
}

internal fun isAdminWebRoute(
    hostname: String,
    pathname: String,
    search: String,
): Boolean {
    val cleanHost = hostname.trim().lowercase()
    if (cleanHost.startsWith("admin.")) return true
    val cleanPath = pathname.trim().lowercase()
    if (cleanPath == "/admin") return true
    if (cleanPath.startsWith("/admin/")) return true
    return hasQueryParam(search, "mode", "admin")
}

private fun resolveInitialWebRoute(
    hostname: String,
    pathname: String,
    search: String,
    rawHash: String,
): Pair<String, Boolean> {
    val isAdminMode = isAdminWebRoute(hostname = hostname, pathname = pathname, search = search)
    val defaultScreen = if (isAdminMode) "A01" else "s14"
    if (!isLocalDevHost(hostname)) {
        return defaultScreen to false
    }
    val hasDevQuery = search.contains("dev=true")
    val hasDevHash = rawHash == "dev"
    val showDev = if (hasDevQuery) true else hasDevHash

    val queryParamScreen = search.removePrefix("?")
        .split("&")
        .firstOrNull { segment -> segment.startsWith("screen=") }
        ?.substringAfter("screen=")

    val hasQueryScreen = !queryParamScreen.isNullOrBlank()
    val isNonDevHash = rawHash != "dev"
    val hasDirectHashScreen = if (rawHash.isNotBlank()) isNonDevHash else false

    val initialScreen = when {
        hasQueryScreen -> queryParamScreen
        hasDirectHashScreen -> rawHash
        else -> defaultScreen
    }
    return initialScreen to showDev
}

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    val body = document.body ?: return
    val hostname = window.location.hostname
    val pathname = window.location.pathname
    val search = window.location.search
    val rawHash = window.location.hash.removePrefix("#")
    val (initialScreen, showDev) = resolveInitialWebRoute(
        hostname = hostname,
        pathname = pathname,
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
