package com.jeudry.rosafiesta

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.jeudry.rosafiesta.di.appModule
import com.jeudry.rosafiesta.ui.NavigationTransitionKind
import com.jeudry.rosafiesta.ui.Navigator
import com.jeudry.rosafiesta.ui.RenderAppBottomBar
import com.jeudry.rosafiesta.ui.RenderPersonalMoreOptionsPanel
import com.jeudry.rosafiesta.ui.RenderScreenSpec
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import com.jeudry.rosafiesta.ui.ScreenCatalog
import com.jeudry.rosafiesta.ui.shouldShowBottomBar
import org.koin.dsl.koinApplication

/**
 * Punto de entrada de la **App Personal** (`RfTheme.personal`: rosa → violeta, fondo rubor cálido).
 */
@Composable
public fun PersonalApp(
    initialScreenCode: String = "p00",
) {
    RosaFiestaApp(
        initialAppView = AppView.PERSONAL,
        initialScreenCode = initialScreenCode,
        showDevHeader = false,
    )
}

/**
 * Punto de entrada de la **App Administrativa** (`RfTheme.admin`: índigo → turquesa, fondo lavanda frío).
 */
@Composable
public fun AdminApp(
    initialScreenCode: String = "A00",
) {
    RosaFiestaApp(
        initialAppView = AppView.ADMIN,
        initialScreenCode = initialScreenCode,
        showDevHeader = false,
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
    showDevHeader: Boolean = false,
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
    val isAdminApp = initialAppView == AppView.ADMIN
    val isNonAdminVariant = spec.variant != RfThemeVariant.ADMIN
    val isPersonalScreenInAdminApp = isAdminApp && isNonAdminVariant
    val isAdminActive = navigator.activeView == AppView.ADMIN
    val isNotPreviewingPersonal = !navigator.adminPreviewingPersonal
    val isNotPersonalScreenInAdminApp = !isPersonalScreenInAdminApp
    val shouldUseAdminTheme = isAdminActive && isNotPreviewingPersonal && isNotPersonalScreenInAdminApp
    val isDarkThemeEnabled by viewModel.isDarkTheme.collectAsState()
    val usePersonalDark = isDarkThemeEnabled && isNonAdminVariant
    val themeVariant = if (shouldUseAdminTheme) {
        RfThemeVariant.ADMIN
    } else if (usePersonalDark) {
        RfThemeVariant.PERSONAL_DARK
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
            val shouldShowReturnBanner = !showDevHeader && isPersonalScreenInAdminApp
            if (shouldShowReturnBanner) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF151729))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    BasicText(
                        text = "Vista previa de cliente",
                        style = typo.tagLabel.copy(color = Color(0xFFE9EEFF)),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(RfColors.IndigoBrand)
                            .clickable {
                                viewModel.soundPlayer.playTap()
                                navigator.switchToAdmin("A01")
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        BasicText(
                            text = "Volver a Administración",
                            style = typo.tagLabel.copy(color = Color.White),
                        )
                    }
                }
            }
            if (showDevHeader) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surface)
                        .windowInsetsPadding(WindowInsets.statusBars)
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

            var showPersonalMoreMenu by remember(spec.code) { mutableStateOf(false) }
            val navigateToCode: (String) -> Unit = { nextCode ->
                showPersonalMoreMenu = false
                viewModel.soundPlayer.playTap()
                navigator.navigateTo(nextCode)
            }
            val isMobileScreen = !spec.isDesktopWeb
            val isBottomBarScreen = shouldShowBottomBar(spec.code)
            val hasBottomBar = if (isMobileScreen) isBottomBarScreen else false

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val transitionKind = navigator.lastTransition
                AnimatedContent(
                    targetState = spec,
                    transitionSpec = {
                        buildScreenTransition(transitionKind).using(SizeTransform(clip = false))
                    },
                    contentKey = { screenItem -> screenAnimationGroupKey(screenItem.code) },
                    label = "rf-screen-transition",
                ) { targetSpec ->
                    val targetGroupKey = screenAnimationGroupKey(targetSpec.code)
                    val currentGroupKey = screenAnimationGroupKey(spec.code)
                    val isSameScreenGroup = targetGroupKey == currentGroupKey
                    val activeSpec = if (isSameScreenGroup) spec else targetSpec
                    RenderScreenSpec(
                        spec = activeSpec,
                        viewModel = viewModel,
                        isFrameHarness = false,
                        includeBottomBar = false,
                        underlyingScreenCode = navigator.previousCode,
                        onSelectScreenCode = navigateToCode,
                        onSwitchAppMode = {
                            showPersonalMoreMenu = false
                            viewModel.soundPlayer.playTap()
                            if (navigator.activeView == AppView.ADMIN) {
                                navigator.switchToPersonal("s14", fromAdminPreview = true)
                            } else {
                                navigator.switchToAdmin("A01")
                            }
                        },
                        onPrimaryAction = {
                            showPersonalMoreMenu = false
                            viewModel.soundPlayer.playTap()
                            val currentUser = viewModel.currentAuthenticatedUser.value
                            val isSplashScreen = navigator.currentCode == "p00"
                            val hasCustomUser = currentUser != null
                            val isNotDefaultSeed = currentUser?.id != "usr-camila"
                            val isCustomSession = if (hasCustomUser) isNotDefaultSeed else false
                            val shouldSkipOnboarding = if (isSplashScreen) isCustomSession else false
                            if (shouldSkipOnboarding) {
                                navigator.navigateTo("s14")
                            } else {
                                navigator.advanceFlow()
                            }
                        },
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = hasBottomBar,
                    enter = fadeIn(animationSpec = tween(durationMillis = 160)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 120)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                ) {
                    RenderAppBottomBar(
                        spec = spec,
                        onSelectScreenCode = navigateToCode,
                        onTogglePersonalMoreMenu = {
                            viewModel.soundPlayer.playTap()
                            showPersonalMoreMenu = !showPersonalMoreMenu
                        },
                        isMoreMenuOpen = showPersonalMoreMenu,
                    )
                }

                if (showPersonalMoreMenu) {
                    RenderPersonalMoreOptionsPanel(
                        currentScreenCode = spec.code,
                        onDismiss = { showPersonalMoreMenu = false },
                        onSelectOption = { targetCode ->
                            showPersonalMoreMenu = false
                            navigateToCode(targetCode)
                        },
                    )
                }
            }
        }
    }
}

private fun buildScreenTransition(kind: NavigationTransitionKind): ContentTransform = when (kind) {
    NavigationTransitionKind.MODAL_SHEET ->
        fadeIn(
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        ).togetherWith(
            fadeOut(
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
            ),
        ).apply {
            targetContentZIndex = 1f
        }

    NavigationTransitionKind.FORWARD -> (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> fullWidth / 3 },
        ) + fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> -fullWidth / 5 },
        ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)),
    ).apply {
        targetContentZIndex = 1f
    }

    NavigationTransitionKind.BACKWARD -> (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> -fullWidth / 5 },
        ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> fullWidth / 3 },
        ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)),
    ).apply {
        targetContentZIndex = 0f
    }

    NavigationTransitionKind.TAB_SWITCH, NavigationTransitionKind.NONE ->
        fadeIn(
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        ).togetherWith(
            fadeOut(
                animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
            ),
        )
}

private fun screenAnimationGroupKey(code: String): String = when (code) {
    "s03", "s04", "s05" -> "personal-onboarding"
    "s84", "s85" -> "event-reschedule-cancel"
    else -> code
}
