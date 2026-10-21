package com.jeudry.rosafiesta.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jeudry.rosafiesta.RosaFiestaApp
import com.jeudry.rosafiesta.core.engine.AppView

/** Punto de entrada de Android para la App Personal (clientes de RosaFiesta). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RosaFiestaApp(initialAppView = AppView.PERSONAL)
        }
    }
}
