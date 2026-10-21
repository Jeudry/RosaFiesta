package com.jeudry.rosafiesta.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jeudry.rosafiesta.core.engine.AppView

/**
 * Dirección y estilo de transición animada entre pantallas.
 */
public enum class NavigationTransitionKind {
    /** Valor cero inicial sin transición. */
    NONE,

    /** Cambio de pestaña raíz en la barra inferior (crossfade + escala sutil). */
    TAB_SWITCH,

    /** Avance jerárquico hacia adelante (slide horizontal desde la derecha). */
    FORWARD,

    /** Retroceso en la pila de navegación (slide horizontal desde la izquierda). */
    BACKWARD,

    /** Hoja inferior o modal emergente (slide vertical desde abajo). */
    MODAL_SHEET,
}

/**
 * Navegador reactivo de pila para las 133 pantallas de RosaFiesta (`PersonalApp` y `AdminApp`),
 * incluyendo el cambio de rol administrativo `A30` y el cálculo automático del tipo de transición.
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

    public var lastTransition: NavigationTransitionKind by mutableStateOf(NavigationTransitionKind.NONE)
        private set

    public var adminPreviewingPersonal: Boolean by mutableStateOf(false)
        private set

    public var devOpen: Boolean by mutableStateOf(false)

    private val backStack = mutableStateListOf<String>()

    public val canGoBack: Boolean
        get() = backStack.isNotEmpty()

    public fun navigateTo(code: String) {
        if (code == currentCode) return
        lastTransition = when {
            ScreenCatalog.isBottomSheet(code) -> NavigationTransitionKind.MODAL_SHEET
            code in ROOT_TAB_CODES -> NavigationTransitionKind.TAB_SWITCH
            else -> NavigationTransitionKind.FORWARD
        }
        backStack.add(currentCode)
        currentCode = code
    }

    public fun popBack(): Boolean {
        if (backStack.isEmpty()) return false
        lastTransition = NavigationTransitionKind.BACKWARD
        currentCode = backStack.removeAt(backStack.lastIndex)
        return true
    }

    public fun switchToPersonal(entryCode: String = "s14", fromAdminPreview: Boolean = false) {
        backStack.clear()
        adminPreviewingPersonal = fromAdminPreview
        activeView = AppView.PERSONAL
        lastTransition = NavigationTransitionKind.TAB_SWITCH
        currentCode = entryCode
    }

    public fun switchToAdmin(entryCode: String = "A01") {
        backStack.clear()
        adminPreviewingPersonal = false
        activeView = AppView.ADMIN
        lastTransition = NavigationTransitionKind.TAB_SWITCH
        currentCode = entryCode
    }

    public fun advanceFlow() {
        val next = ScreenCatalog.defaultNextCode(currentCode)
        navigateTo(next)
    }

    private companion object {
        val ROOT_TAB_CODES = setOf("s01", "s14", "s15", "s18", "s21", "s24", "s26", "s37", "A01", "A08", "A09", "A11", "A12")
    }
}
