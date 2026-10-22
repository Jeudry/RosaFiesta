package com.jeudry.rosafiesta

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.jeudry.rosafiesta.di.appModule
import com.jeudry.rosafiesta.ui.NavigationTransitionKind
import com.jeudry.rosafiesta.ui.Navigator
import com.jeudry.rosafiesta.ui.RenderScreenSpec
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import com.jeudry.rosafiesta.ui.ScreenCatalog
import org.koin.dsl.koinApplication

/**
 * Punto de entrada de la **App Personal** (`RfTheme.personal`: rosa → violeta, fondo rubor cálido).
 */
@Composable
public fun PersonalApp(
    initialScreenCode: String = "s14",
) {
    RosaFiestaApp(
        initialAppView = AppView.PERSONAL,
        initialScreenCode = initialScreenCode,
    )
}

/**
 * Punto de entrada de la **App Administrativa** (`RfTheme.admin`: índigo → turquesa, fondo lavanda frío).
 */
@Composable
public fun AdminApp(
    initialScreenCode: String = "A01",
) {
    RosaFiestaApp(
        initialAppView = AppView.ADMIN,
        initialScreenCode = initialScreenCode,
    )
}

/**
 * Raíz compartida de RosaFiesta que soporta las dos aplicaciones del sistema:
 * - **App Personal** (`AppView.PERSONAL`: `p00`, `s01`–`s54`, `s75`–`s92`)
 * - **App Administrativa** (`AppView.ADMIN`: `A00`–`A39`, `s55`–`s74`)
 * Incluye transiciones direccionales naturales, sonidos CC0, cambio de vista de administrador (`A30`) y modo desarrollador.
 */
@Composable
public fun RosaFiestaApp(
    initialAppView: AppView = AppView.PERSONAL,
    initialScreenCode: String = if (initialAppView == AppView.ADMIN) "A01" else "s14",
    showDevHeader: Boolean = true,
    viewModel: RosaFiestaViewModel = remember {
        koinApplication { modules(appModule) }.koin.get()
    },
) {
    val navigator = remember(initialAppView, initialScreenCode) {
        Navigator(
            initialAppView = initialAppView,
            initialScreenCode = initialScreenCode,
        )
    }

    if (navigator.devOpen) {
        DevPanelScreen(
            onSwitchToPersonalApp = {
                viewModel.soundPlayer.playTap()
                navigator.switchToPersonal("s14", fromAdminPreview = false)
                navigator.devOpen = false
            },
            onSwitchToAdminApp = {
                viewModel.soundPlayer.playTap()
                navigator.switchToAdmin("A01")
                navigator.devOpen = false
            },
            onClose = {
                viewModel.soundPlayer.playTap()
                navigator.devOpen = false
            },
        )
        return
    }

    val spec = ScreenCatalog.findByCode(navigator.currentCode)
    val themeVariant = if (navigator.activeView == AppView.ADMIN && !navigator.adminPreviewingPersonal) {
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
            if (showDevHeader) {
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
                            selected = navigator.activeView == AppView.PERSONAL,
                            onClick = {
                                viewModel.soundPlayer.playTap()
                                navigator.switchToPersonal("s14", fromAdminPreview = false)
                            },
                            modifier = Modifier.testTag("switch-personal"),
                        )
                        RfChip(
                            label = "App Admin",
                            selected = navigator.activeView == AppView.ADMIN,
                            onClick = {
                                viewModel.soundPlayer.playTap()
                                navigator.switchToAdmin("A01")
                            },
                            modifier = Modifier.testTag("switch-admin"),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (navigator.adminPreviewingPersonal) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(RfColors.IndigoBrand)
                                    .clickable {
                                        viewModel.soundPlayer.playTap()
                                        navigator.switchToAdmin("A30")
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
                                .clickable {
                                    viewModel.soundPlayer.playTap()
                                    navigator.devOpen = true
                                }
                                .testTag("open-dev-panel"),
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val transitionKind = navigator.lastTransition
                AnimatedContent(
                    targetState = spec,
                    transitionSpec = { buildScreenTransition(transitionKind) },
                    contentKey = { it.code },
                    label = "rf-screen-transition",
                ) { targetSpec ->
                    RenderScreenSpec(
                        spec = targetSpec,
                        viewModel = viewModel,
                        isFrameHarness = false,
                        onSelectScreenCode = { nextCode ->
                            viewModel.soundPlayer.playTap()
                            navigator.navigateTo(nextCode)
                        },
                        onSwitchAppMode = {
                            viewModel.soundPlayer.playTap()
                            if (navigator.activeView == AppView.ADMIN) {
                                navigator.switchToPersonal("s14", fromAdminPreview = true)
                            } else {
                                navigator.switchToAdmin("A39")
                            }
                        },
                        onPrimaryAction = {
                            viewModel.soundPlayer.playTap()
                            navigator.advanceFlow()
                        },
                    )
                }
            }
        }
    }
}

private fun buildScreenTransition(kind: NavigationTransitionKind): ContentTransform = when (kind) {
    NavigationTransitionKind.MODAL_SHEET -> (
        slideInVertically(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            initialOffsetY = { fullHeight -> fullHeight / 5 },
        ) + fadeIn(animationSpec = tween(durationMillis = 220))
        ).togetherWith(
        slideOutVertically(
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            targetOffsetY = { fullHeight -> fullHeight / 6 },
        ) + fadeOut(animationSpec = tween(durationMillis = 160)),
    )

    NavigationTransitionKind.FORWARD -> (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> fullWidth / 6 },
        ) + fadeIn(animationSpec = tween(durationMillis = 220))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> -fullWidth / 8 },
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
    )

    NavigationTransitionKind.BACKWARD -> (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> -fullWidth / 6 },
        ) + fadeIn(animationSpec = tween(durationMillis = 200))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> fullWidth / 8 },
        ) + fadeOut(animationSpec = tween(durationMillis = 160)),
    )

    NavigationTransitionKind.TAB_SWITCH, NavigationTransitionKind.NONE -> (
        fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.985f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            )
        ).togetherWith(
        fadeOut(animationSpec = tween(durationMillis = 160)),
    )
}
