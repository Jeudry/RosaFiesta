package com.jeudry.rosafiesta

import androidx.compose.ui.window.ComposeUIViewController
import com.jeudry.rosafiesta.core.engine.AppView
import platform.UIKit.UIViewController

/** Punto de entrada de iOS para la App Personal de RosaFiesta. */
@Suppress("FunctionName")
public fun MainViewController(): UIViewController = ComposeUIViewController {
    RosaFiestaApp(initialAppView = AppView.PERSONAL)
}

/** Punto de entrada de iOS para la App Administrativa de RosaFiesta. */
@Suppress("FunctionName")
public fun AdminMainViewController(): UIViewController = ComposeUIViewController {
    RosaFiestaApp(initialAppView = AppView.ADMIN)
}
