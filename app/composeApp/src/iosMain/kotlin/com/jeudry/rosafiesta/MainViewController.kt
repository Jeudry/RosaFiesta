package com.jeudry.rosafiesta

import androidx.compose.ui.window.ComposeUIViewController
import com.jeudry.rosafiesta.data.handleIosIncomingDeepLinkUrl
import com.jeudry.rosafiesta.data.registerIosApnsDeviceToken
import platform.UIKit.UIViewController

/** Punto de entrada de iOS para la App Personal de RosaFiesta. */
@Suppress("FunctionName")
public fun MainViewController(): UIViewController = ComposeUIViewController {
    PersonalApp(initialScreenCode = "p00")
}

/** Punto de entrada de iOS para la App Administrativa de RosaFiesta. */
@Suppress("FunctionName")
public fun AdminMainViewController(): UIViewController = ComposeUIViewController {
    AdminApp(initialScreenCode = "A00")
}

/**
 * Registers the hex-encoded APNs device token forwarded by iOS `UIApplicationDelegate`.
 */
public fun handleApnsDeviceToken(tokenHex: String) {
    registerIosApnsDeviceToken(tokenHex)
}

/**
 * Clears any pending iOS push registration callback when APNs registration fails.
 */
public fun handleApnsRegistrationFailure() {
    registerIosApnsDeviceToken(null)
}

/**
 * Dispatches an incoming custom URL scheme callback (`rosafiesta://payment/...`) opened by iOS.
 */
public fun handleIncomingDeepLinkUrl(urlString: String): Boolean = handleIosIncomingDeepLinkUrl(urlString)
