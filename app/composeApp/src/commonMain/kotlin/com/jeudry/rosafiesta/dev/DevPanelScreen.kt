package com.jeudry.rosafiesta.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfTheme
import com.jeudry.rosafiesta.designsystem.RfThemeVariant

private enum class DevSubScreen {
    MENU,
    SCREENS,
    DESIGN_SYSTEM,
}

/**
 * Panel del modo desarrollador de RosaFiesta: permite inspeccionar las 133 pantallas (`ScreenGallery`),
 * los 11 componentes (`DesignGallery`) y alternar entre App Personal y App Administrativa.
 */
@Composable
internal fun DevPanelScreen(
    onSwitchToPersonalApp: () -> Unit,
    onSwitchToAdminApp: () -> Unit,
    onClose: () -> Unit,
) {
    var subScreen by rememberSaveable { mutableStateOf(DevSubScreen.MENU) }

    when (subScreen) {
        DevSubScreen.SCREENS -> ScreenGallery(onBack = { subScreen = DevSubScreen.MENU })

        DevSubScreen.DESIGN_SYSTEM -> DesignGallery(onBack = { subScreen = DevSubScreen.MENU })

        DevSubScreen.MENU -> {
            RfTheme(variant = RfThemeVariant.PERSONAL) {
                val palette = LocalRfPalette.current
                val typo = LocalRfTypography.current

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.bg)
                        .padding(20.dp),
                ) {
                    BasicText("Modo Desarrollador · RosaFiesta", style = typo.heroTitle)
                    BasicText(
                        "Inspecciona las 133 pantallas del sistema (Variante B), la galería de componentes o alterna entre las dos aplicaciones.",
                        style = typo.body,
                    )

                    RfCard {
                        BasicText("Galerías de diseño y capturas", style = typo.cardTitle)
                        Spacer(Modifier.height(8.dp))
                        RfButton(
                            label = "Explorar las 133 pantallas (p00, s01–s92, A00–A39)",
                            onClick = { subScreen = DevSubScreen.SCREENS },
                        )
                        Spacer(Modifier.height(8.dp))
                        RfButton(
                            label = "Ver los 11 componentes del Sistema de Diseño",
                            style = RfButtonStyle.SECONDARY,
                            onClick = { subScreen = DevSubScreen.DESIGN_SYSTEM },
                        )
                    }

                    RfCard {
                        BasicText("Cambiar aplicación activa (A30)", style = typo.cardTitle)
                        Spacer(Modifier.height(8.dp))
                        RfButton(
                            label = "Abrir App Personal (Camila Rodríguez · Cliente)",
                            onClick = onSwitchToPersonalApp,
                        )
                        Spacer(Modifier.height(8.dp))
                        RfButton(
                            label = "Abrir App Administrativa (María · Dueña y Equipo)",
                            style = RfButtonStyle.SOL,
                            onClick = onSwitchToAdminApp,
                        )
                    }

                    RfButton(
                        label = "Volver a la vista actual",
                        style = RfButtonStyle.SECONDARY,
                        onClick = onClose,
                    )
                }
            }
        }
    }
}
