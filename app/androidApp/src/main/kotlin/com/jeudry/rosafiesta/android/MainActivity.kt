package com.jeudry.rosafiesta.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jeudry.rosafiesta.PersonalApp
import com.jeudry.rosafiesta.data.initAndroidDriverContext

/** Punto de entrada de Android para la App Personal (clientes de RosaFiesta). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAndroidDriverContext(applicationContext, databaseName = "rosafiesta-personal.db")
        setContent {
            PersonalApp(initialScreenCode = "p00")
        }
    }
}
