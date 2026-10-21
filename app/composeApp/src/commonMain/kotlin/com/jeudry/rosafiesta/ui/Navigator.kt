package com.jeudry.rosafiesta.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jeudry.rosafiesta.core.engine.AppView

/**
 * Navegador reactivo de pila para las 133 pantallas de RosaFiesta (`PersonalApp` y `AdminApp`),
 * incluyendo el cambio de rol administrativo `A30` para revisar la App Personal como cliente.
 */
@Stable
public class Navigator(
    initialAppView: AppView = AppView.PERSONAL,
    initialScreenCode: String = if (initialAppView == AppView.ADMIN) "A01" else "s14",
) {
    public var activeView: AppView by mutableStateOf(initialAppView)
        private set

    public var currentCode: String by mutableStateOf(initialScreenCode)
        private set

    public var adminPreviewingPersonal: Boolean by mutableStateOf(false)
        private set

    public var devOpen: Boolean by mutableStateOf(false)

    private val backStack = mutableStateListOf<String>()

    public val canGoBack: Boolean
        get() = backStack.isNotEmpty()

    public fun navigateTo(code: String) {
        if (code == currentCode) return
        backStack.add(currentCode)
        currentCode = code
    }

    public fun popBack(): Boolean {
        if (backStack.isEmpty()) return false
        currentCode = backStack.removeAt(backStack.lastIndex)
        return true
    }

    public fun switchToPersonal(entryCode: String = "s14", fromAdminPreview: Boolean = false) {
        backStack.clear()
        adminPreviewingPersonal = fromAdminPreview
        activeView = AppView.PERSONAL
        currentCode = entryCode
    }

    public fun switchToAdmin(entryCode: String = "A01") {
        backStack.clear()
        adminPreviewingPersonal = false
        activeView = AppView.ADMIN
        currentCode = entryCode
    }

    public fun advanceFlow() {
        val next = ScreenCatalog.defaultNextCode(currentCode)
        navigateTo(next)
    }
}
