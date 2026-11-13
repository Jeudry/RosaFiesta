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

    public var lastModalOriginCode: String? by mutableStateOf(null)
        private set

    private val backStack = mutableStateListOf<String>()

    public val canGoBack: Boolean
        get() = backStack.isNotEmpty()

    public val previousCode: String?
        get() = backStack.lastOrNull() ?: lastModalOriginCode

    public fun navigateTo(code: String) {
        val leavingModalSheet = ScreenCatalog.isBottomSheet(currentCode)
        val backTransitionKind = if (leavingModalSheet) {
            NavigationTransitionKind.MODAL_SHEET
        } else {
            NavigationTransitionKind.BACKWARD
        }
        if (code.startsWith("BACK")) {
            val explicitTarget = code.substringAfter("BACK:", missingDelimiterValue = "").trim()
            val hasExplicitTarget = explicitTarget.isNotEmpty()
            if (hasExplicitTarget) {
                val targetBackIndex = backStack.lastIndexOf(explicitTarget)
                if (targetBackIndex >= 0) {
                    backStack.removeRange(targetBackIndex, backStack.size)
                    lastTransition = backTransitionKind
                    currentCode = explicitTarget
                    return
                }
            }
            if (popBack()) {
                return
            }
            val defaultHomeCode = if (activeView == AppView.ADMIN) "A01" else "s14"
            val fallbackTarget = explicitTarget.ifBlank { defaultHomeCode }
            if (fallbackTarget == currentCode) {
                return
            }
            lastTransition = backTransitionKind
            currentCode = fallbackTarget
            return
        }
        if (code == currentCode) return
        val isRootTab = code in ROOT_TAB_CODES
        val existingBackIndex = backStack.indexOf(code)
        if (existingBackIndex >= 0) {
            backStack.removeRange(existingBackIndex, backStack.size)
            val useTabSwitch = isRootTab && !leavingModalSheet
            lastTransition = if (useTabSwitch) {
                NavigationTransitionKind.TAB_SWITCH
            } else {
                backTransitionKind
            }
            currentCode = code
            return
        }
        val isSiblingTabSwitch = isSameSiblingTabGroup(currentCode, code)
        if (isSiblingTabSwitch) {
            lastTransition = NavigationTransitionKind.TAB_SWITCH
            currentCode = code
            return
        }
        val enteringModalSheet = ScreenCatalog.isBottomSheet(code)
        if (enteringModalSheet) {
            if (!leavingModalSheet) {
                lastModalOriginCode = currentCode
            }
        }
        val isModalTransition = if (enteringModalSheet) true else leavingModalSheet
        lastTransition = when {
            isModalTransition -> NavigationTransitionKind.MODAL_SHEET
            isRootTab -> NavigationTransitionKind.TAB_SWITCH
            else -> NavigationTransitionKind.FORWARD
        }
        val isHomeTab = code in HOME_TAB_CODES
        val isNonSplashOrigin = currentCode !in SPLASH_CODES
        val shouldRecordInBackStack = if (isNonSplashOrigin) !leavingModalSheet else false
        if (isHomeTab) {
            backStack.clear()
        } else if (shouldRecordInBackStack) {
            val shouldPushCurrent = backStack.lastOrNull() != currentCode
            if (shouldPushCurrent) {
                backStack.add(currentCode)
            }
        }
        currentCode = code
    }

    public fun popBack(): Boolean {
        if (backStack.isEmpty()) return false
        val leavingModalSheet = ScreenCatalog.isBottomSheet(currentCode)
        lastTransition = if (leavingModalSheet) {
            NavigationTransitionKind.MODAL_SHEET
        } else {
            NavigationTransitionKind.BACKWARD
        }
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

    private fun isSameSiblingTabGroup(firstCode: String, secondCode: String): Boolean = SIBLING_TAB_GROUPS.any { group ->
        val hasFirst = firstCode in group
        val hasSecond = secondCode in group
        if (hasFirst) hasSecond else false
    }

    private companion object {
        val SPLASH_CODES = setOf("p00", "A00")
        val HOME_TAB_CODES = setOf("s01", "s14", "s15", "A01")
        val ROOT_TAB_CODES = setOf("s01", "s14", "s15", "s18", "s26", "s37", "A01", "A08", "A09", "A11", "A12")
        val SIBLING_TAB_GROUPS = listOf(
            setOf("s84", "s85"),
            setOf("s58", "s59", "s60", "s61", "s62"),
            setOf("s64", "s65", "s66"),
            setOf("s67", "s68", "s69", "s70"),
            setOf("s72", "s73", "s74"),
        )
    }
}
