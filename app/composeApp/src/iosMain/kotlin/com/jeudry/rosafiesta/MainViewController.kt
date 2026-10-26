package com.jeudry.rosafiesta

import androidx.compose.ui.window.ComposeUIViewController
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
