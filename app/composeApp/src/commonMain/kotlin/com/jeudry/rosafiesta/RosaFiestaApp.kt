package com.jeudry.rosafiesta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.jeudry.rosafiesta.core.engine.AppView
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfTheme
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.dev.DevPanelScreen
import com.jeudry.rosafiesta.ui.RenderScreenSpec
import com.jeudry.rosafiesta.ui.ScreenCatalog

/**
 * Raíz compartida de RosaFiesta que soporta las dos aplicaciones del sistema:
 * - **App Personal** (`AppView.PERSONAL`: `p00`, `s01`–`s54`, `s75`–`s92`)
 * - **App Administrativa** (`AppView.ADMIN`: `A00`–`A39`, `s55`–`s74`)
 * Incluye cambio de vista de administrador (`A30`) y modo desarrollador (`DevPanelScreen`).
 */
@Composable
public fun RosaFiestaApp(
    initialAppView: AppView = AppView.PERSONAL,
    initialScreenCode: String = if (initialAppView == AppView.ADMIN) "s55" else "s14",
) {
    var activeView by rememberSaveable { mutableStateOf(initialAppView) }
    var currentCode by rememberSaveable { mutableStateOf(initialScreenCode) }
    var adminPreviewingPersonal by rememberSaveable { mutableStateOf(false) }
    var devOpen by rememberSaveable { mutableStateOf(false) }

    if (devOpen) {
        DevPanelScreen(
            onSwitchToPersonalApp = {
                activeView = AppView.PERSONAL
                currentCode = "s14"
                devOpen = false
            },
            onSwitchToAdminApp = {
                activeView = AppView.ADMIN
                currentCode = "s55"
                devOpen = false
            },
            onClose = { devOpen = false },
        )
        return
    }

    val spec = ScreenCatalog.findByCode(currentCode)
    val themeVariant = if (activeView == AppView.ADMIN && !adminPreviewingPersonal) {
        RfThemeVariant.ADMIN
    } else {
        spec.variant
    }

    RfTheme(variant = themeVariant) {
        val palette = LocalRfPalette.current
        val typo = LocalRfTypography.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(palette.bg)
                .testTag("rosafiesta-root"),
        ) {
            // Barra superior de control de vista (App Personal / App Administrativa / Galería 133 pantallas)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfChip(
                        label = "App Personal",
                        selected = activeView == AppView.PERSONAL,
                        onClick = {
                            activeView = AppView.PERSONAL
                            currentCode = "s14"
                        },
                        modifier = Modifier.testTag("switch-personal"),
                    )
                    RfChip(
                        label = "App Admin",
                        selected = activeView == AppView.ADMIN,
                        onClick = {
                            activeView = AppView.ADMIN
                            adminPreviewingPersonal = false
                            currentCode = "s55"
                        },
                        modifier = Modifier.testTag("switch-admin"),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (adminPreviewingPersonal) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(RfColors.IndigoBrand)
                                .clickable {
                                    adminPreviewingPersonal = false
                                    activeView = AppView.ADMIN
                                    currentCode = "A30"
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("banner-return-admin"),
                        ) {
                            BasicText(
                                text = "Volver a Negocio (A30)",
                                style = typo.tagLabel.copy(color = Color.White),
                            )
                        }
                    }
                    RfTag(
                        text = "Dev · 133 pantallas",
                        tone = RfTagTone.SOL,
                        modifier = Modifier
                            .clickable { devOpen = true }
                            .testTag("open-dev-panel"),
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                RenderScreenSpec(
                    spec = spec,
                    onSelectScreenCode = { nextCode -> currentCode = nextCode },
                    onSwitchAppMode = {
                        if (activeView == AppView.ADMIN) {
                            adminPreviewingPersonal = true
                            activeView = AppView.PERSONAL
                            currentCode = "s14"
                        } else {
                            activeView = AppView.ADMIN
                            currentCode = "s55"
                        }
                    },
                    onPrimaryAction = {
                        val list = if (activeView == AppView.ADMIN) {
                            ScreenCatalog.adminScreens
                        } else {
                            ScreenCatalog.personalScreens
                        }
                        val idx = list.indexOfFirst { it.code == spec.code }
                        val next = if (idx in 0 until list.lastIndex) list[idx + 1] else list.first()
                        currentCode = next.code
                    },
                )
            }
        }
    }
}
