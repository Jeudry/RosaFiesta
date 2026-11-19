package com.jeudry.rosafiesta

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.core.engine.AppView
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.designsystem.PreloadRfVisualAssets
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfTheme
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.dev.DevPanelScreen
import com.jeudry.rosafiesta.di.appModule
import com.jeudry.rosafiesta.ui.NavigationTransitionKind
import com.jeudry.rosafiesta.ui.Navigator
import com.jeudry.rosafiesta.ui.RenderAppBottomBar
import com.jeudry.rosafiesta.ui.RenderFloatingModalOverlay
import com.jeudry.rosafiesta.ui.RenderPersonalMoreOptionsPanel
import com.jeudry.rosafiesta.ui.RenderScreenSpec
import com.jeudry.rosafiesta.ui.RfSavedCartPreviewToast
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import com.jeudry.rosafiesta.ui.ScreenCatalog
import com.jeudry.rosafiesta.ui.isFloatingOverlayModalCode
import com.jeudry.rosafiesta.ui.resolvePersonalTabTarget
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
    val sharedUtilityCodes = setOf("s07", "s08", "s09", "s10", "s11", "s12", "s13", "s54", "s87")
    val isSharedUtilityScreen = spec.code in sharedUtilityCodes
    val isAdminApp = initialAppView == AppView.ADMIN
    val isNonAdminVariant = spec.variant != RfThemeVariant.ADMIN
    val isAdminActive = navigator.activeView == AppView.ADMIN
    val isNotPreviewingPersonal = !navigator.adminPreviewingPersonal
    val isSharedAdminUtility = isAdminActive && isSharedUtilityScreen && isNotPreviewingPersonal
    val isPersonalScreenInAdminApp = isAdminApp && isNonAdminVariant && !isSharedAdminUtility
    val isNotSharedUtility = !isSharedUtilityScreen
    val isPreviewBannerEligible = if (navigator.adminPreviewingPersonal) true else isNotSharedUtility
    val isNotPersonalScreenInAdminApp = !isPersonalScreenInAdminApp
    val shouldUseAdminTheme = isAdminActive && isNotPreviewingPersonal && isNotPersonalScreenInAdminApp
    val isDarkThemeEnabled by viewModel.isDarkTheme.collectAsState()
    val usePersonalDark = isDarkThemeEnabled && isNonAdminVariant && !isSharedAdminUtility
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
        val isAppLocked by viewModel.isAppLocked.collectAsState()
        val appLockError by viewModel.appLockError.collectAsState()
        val requiresReauthentication by viewModel.requiresReauthentication.collectAsState()
        LaunchedEffect(requiresReauthentication) {
            if (requiresReauthentication) {
                viewModel.consumeSessionExpiredPrompt()
                if (navigator.currentCode != "s07") {
                    navigator.navigateTo("s07")
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.bg)
                    .testTag("rosafiesta-root"),
            ) {
                val hasHiddenDevHeader = !showDevHeader
                val isPersonalPreviewVisible = if (hasHiddenDevHeader) isPersonalScreenInAdminApp else false
                val shouldShowReturnBanner = if (isPersonalPreviewVisible) isPreviewBannerEligible else false
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
                    viewModel.clearFormFeedback()
                    viewModel.soundPlayer.playTap()
                    val isAdminTarget = nextCode == "A01"
                    val shouldEnterAdminRoot = if (isAdminTarget) navigator.activeView != AppView.ADMIN else false
                    if (shouldEnterAdminRoot) {
                        navigator.switchToAdmin("A01")
                    } else {
                        navigator.navigateTo(nextCode)
                    }
                }
                val isOverlayModalActive = isFloatingOverlayModalCode(spec.code)
                var baseScreenCode by remember {
                    val initialBase = if (isOverlayModalActive) {
                        navigator.lastModalOriginCode ?: navigator.previousCode ?: "s18"
                    } else {
                        spec.code
                    }
                    mutableStateOf(initialBase)
                }
                if (!isOverlayModalActive) {
                    baseScreenCode = spec.code
                }
                val baseSpec = remember(baseScreenCode) { ScreenCatalog.findByCode(baseScreenCode) }
                var lastOverlayModalCode by remember { mutableStateOf<String?>(null) }
                var lastOverlayOriginCode by remember { mutableStateOf<String?>(null) }
                if (isOverlayModalActive) {
                    lastOverlayModalCode = spec.code
                    lastOverlayOriginCode = baseScreenCode
                }

                val isMobileScreen = !baseSpec.isDesktopWeb
                val isBottomBarScreen = shouldShowBottomBar(baseSpec.code)
                val hasBottomBar = if (isMobileScreen) isBottomBarScreen else false

                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    val usePersonalDesktopWorkspace = if (isMobileScreen) maxWidth >= 840.dp else false
                    val viewportWrapperModifier = if (usePersonalDesktopWorkspace) {
                        Modifier
                            .fillMaxSize()
                            .background(palette.bg)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    } else {
                        Modifier.fillMaxSize()
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = viewportWrapperModifier,
                    ) {
                        val shouldShowPersonalRail = if (usePersonalDesktopWorkspace) isBottomBarScreen else false
                        if (shouldShowPersonalRail) {
                            PersonalDesktopNavRail(
                                currentCode = baseSpec.code,
                                isMoreMenuOpen = showPersonalMoreMenu,
                                onTogglePersonalMoreMenu = {
                                    viewModel.soundPlayer.playTap()
                                    showPersonalMoreMenu = !showPersonalMoreMenu
                                },
                                onSelectScreenCode = navigateToCode,
                            )
                        }
                        val cardModifier = if (usePersonalDesktopWorkspace) {
                            Modifier
                                .widthIn(max = 560.dp)
                                .fillMaxHeight()
                                .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0x33151729))
                                .clip(RoundedCornerShape(28.dp))
                                .border(1.dp, palette.line, RoundedCornerShape(28.dp))
                                .background(palette.bg)
                        } else {
                            Modifier.fillMaxSize()
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = cardModifier,
                        ) {
                            PreloadRfVisualAssets()
                            LaunchedEffect(viewModel) {
                                viewModel.refreshSharedStateFromDisk()
                            }
                            val transitionKind = navigator.lastTransition
                            AnimatedContent(
                                targetState = baseSpec,
                                transitionSpec = {
                                    val isEnteringModal = ScreenCatalog.isBottomSheet(targetState.code)
                                    val isLeavingModal = ScreenCatalog.isBottomSheet(initialState.code)
                                    buildScreenTransition(
                                        kind = transitionKind,
                                        isEnteringModal = isEnteringModal,
                                        isLeavingModal = isLeavingModal,
                                    ).using(SizeTransform(clip = false))
                                },
                                contentKey = { screenItem -> screenAnimationGroupKey(screenItem.code) },
                                label = "rf-screen-transition",
                            ) { targetSpec ->
                                val targetGroupKey = screenAnimationGroupKey(targetSpec.code)
                                val currentGroupKey = screenAnimationGroupKey(baseSpec.code)
                                val isSameScreenGroup = targetGroupKey == currentGroupKey
                                val activeSpec = if (isSameScreenGroup) baseSpec else targetSpec
                                RenderScreenSpec(
                                    spec = activeSpec,
                                    viewModel = viewModel,
                                    isFrameHarness = false,
                                    includeBottomBar = false,
                                    underlyingScreenCode = navigator.previousCode,
                                    themeVariantOverride = themeVariant,
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

                            val showBottomNav = if (hasBottomBar) !usePersonalDesktopWorkspace else false
                            val savedPreview by viewModel.savedItemPreview.collectAsState()
                            LaunchedEffect(savedPreview) {
                                if (savedPreview != null) {
                                    kotlinx.coroutines.delay(3000L)
                                    viewModel.clearSavedItemPreview()
                                }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = savedPreview != null,
                                enter = fadeIn(animationSpec = tween(durationMillis = 180)) +
                                    slideInVertically(animationSpec = tween(durationMillis = 200)) { fullHeight -> -fullHeight / 2 },
                                exit = fadeOut(animationSpec = tween(durationMillis = 180)) +
                                    slideOutVertically(animationSpec = tween(durationMillis = 180)) { fullHeight -> -fullHeight / 2 },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .zIndex(21f)
                                    .padding(
                                        top = 68.dp,
                                        end = 16.dp,
                                        start = 48.dp,
                                    ),
                            ) {
                                val currentPreview = savedPreview
                                if (currentPreview != null) {
                                    RfSavedCartPreviewToast(
                                        preview = currentPreview,
                                        viewModel = viewModel,
                                        onOpenCart = {
                                            viewModel.clearSavedItemPreview()
                                            navigateToCode("s25")
                                        },
                                        onDismiss = { viewModel.clearSavedItemPreview() },
                                    )
                                }
                            }
                            val globalFeedback by viewModel.actionToast.collectAsState()
                            LaunchedEffect(globalFeedback) {
                                if (!globalFeedback.isNullOrBlank()) {
                                    kotlinx.coroutines.delay(2400L)
                                    viewModel.clearActionToast()
                                }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !globalFeedback.isNullOrBlank(),
                                enter = fadeIn(animationSpec = tween(durationMillis = 180)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 180)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .zIndex(20f)
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        bottom = if (showBottomNav) 86.dp else 82.dp,
                                    ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .shadow(10.dp, RoundedCornerShape(999.dp), spotColor = Color(0x401B6E3E))
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFF228B4E))
                                        .border(1.dp, Color(0xFF39A968), RoundedCornerShape(999.dp))
                                        .clickable { viewModel.clearActionToast() }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                ) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                    BasicText(
                                        text = globalFeedback.orEmpty(),
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                        ),
                                    )
                                }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showBottomNav,
                                enter = fadeIn(animationSpec = tween(durationMillis = 160)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 120)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                            ) {
                                RenderAppBottomBar(
                                    spec = baseSpec,
                                    onSelectScreenCode = navigateToCode,
                                    onTogglePersonalMoreMenu = {
                                        viewModel.soundPlayer.playTap()
                                        showPersonalMoreMenu = !showPersonalMoreMenu
                                    },
                                    isMoreMenuOpen = showPersonalMoreMenu,
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = showPersonalMoreMenu,
                                enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                                    androidx.compose.animation.scaleIn(
                                        initialScale = 0.92f,
                                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.9f, 1f),
                                        animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing),
                                    ),
                                exit = fadeOut(animationSpec = tween(durationMillis = 110)) +
                                    androidx.compose.animation.scaleOut(
                                        targetScale = 0.94f,
                                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.9f, 1f),
                                        animationSpec = tween(durationMillis = 110),
                                    ),
                            ) {
                                RenderPersonalMoreOptionsPanel(
                                    currentScreenCode = baseSpec.code,
                                    onDismiss = { showPersonalMoreMenu = false },
                                    onSelectOption = { targetCode ->
                                        showPersonalMoreMenu = false
                                        navigateToCode(targetCode)
                                    },
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = isOverlayModalActive,
                                enter = fadeIn(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(15f),
                            ) {
                                val overlayCode = lastOverlayModalCode
                                if (overlayCode != null) {
                                    RenderFloatingModalOverlay(
                                        screenCode = overlayCode,
                                        viewModel = viewModel,
                                        underlyingScreenCode = lastOverlayOriginCode,
                                        onSelectScreenCode = navigateToCode,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isAppLocked) {
                AppLockOverlay(
                    errorMessage = appLockError,
                    onTryBiometricUnlock = { viewModel.unlockAppWithBiometrics() },
                    onUnlockWithPassword = { passwordText -> viewModel.unlockAppWithPassword(passwordText) },
                    onLogout = { viewModel.logoutSession() },
                )
            }
        }
    }
}

private fun buildScreenTransition(
    kind: NavigationTransitionKind,
    isEnteringModal: Boolean,
    isLeavingModal: Boolean,
): ContentTransform = when (kind) {
    NavigationTransitionKind.MODAL_SHEET -> buildModalSheetTransition(
        isEnteringModal = isEnteringModal,
        isLeavingModal = isLeavingModal,
    )

    NavigationTransitionKind.TAB_NEXT ->
        slideInHorizontally(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> fullWidth },
        ).togetherWith(
            slideOutHorizontally(
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> -fullWidth },
            ),
        )

    NavigationTransitionKind.TAB_PREV ->
        slideInHorizontally(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> -fullWidth },
        ).togetherWith(
            slideOutHorizontally(
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> fullWidth },
            ),
        )

    NavigationTransitionKind.FORWARD ->
        slideInHorizontally(
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> fullWidth },
        ).togetherWith(
            slideOutHorizontally(
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
            ),
        ).apply {
            targetContentZIndex = 1f
        }

    NavigationTransitionKind.BACKWARD ->
        slideInHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
        ).togetherWith(
            slideOutHorizontally(
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> fullWidth },
            ),
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

private fun buildModalSheetTransition(
    isEnteringModal: Boolean,
    isLeavingModal: Boolean,
): ContentTransform {
    val enterOnlyModal = if (isEnteringModal) !isLeavingModal else false
    if (enterOnlyModal) {
        return fadeIn(
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        ).togetherWith(
            ExitTransition.None,
        ).apply {
            targetContentZIndex = 1f
        }
    }
    val exitOnlyModal = if (isLeavingModal) !isEnteringModal else false
    if (exitOnlyModal) {
        return EnterTransition.None.togetherWith(
            fadeOut(
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            ),
        ).apply {
            targetContentZIndex = 0f
        }
    }
    return fadeIn(
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
    ).togetherWith(
        fadeOut(
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        ),
    ).apply {
        targetContentZIndex = 1f
    }
}

private fun screenAnimationGroupKey(code: String): String = when (code) {
    "s03", "s04", "s05" -> "personal-onboarding"
    "s84", "s85" -> "event-reschedule-cancel"
    else -> code
}

@Composable
private fun PersonalDesktopNavRail(
    currentCode: String,
    isMoreMenuOpen: Boolean,
    onTogglePersonalMoreMenu: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val guestMode = ScreenCatalog.isGuestScreen(currentCode)
    val tabs = if (guestMode) PersonalTab.guestTabs else PersonalTab.standardTabs
    val activeTab = if (isMoreMenuOpen) PersonalTab.MAS else ScreenCatalog.activePersonalTab(currentCode)
    Column(
        modifier = Modifier
            .width(210.dp)
            .fillMaxHeight()
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1F151729))
            .clip(RoundedCornerShape(24.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(
            text = "RosaFiesta",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
        )
        tabs.forEach { tab ->
            val isSelected = tab == activeTab
            val rowBg = if (isSelected) Color(0xFFFDEAF4) else Color.Transparent
            val tintColor = if (isSelected) RfColors.RosaBrand else palette.ink
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(rowBg)
                    .clickable {
                        if (tab == PersonalTab.MAS) {
                            onTogglePersonalMoreMenu()
                        } else {
                            onSelectScreenCode(resolvePersonalTabTarget(tab = tab, guestMode = guestMode))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                RfIcon(kind = tab.icon, tint = tintColor, size = 18.dp)
                BasicText(
                    text = tab.label,
                    style = typo.body.copy(
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = tintColor,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AppLockOverlay(
    errorMessage: String?,
    onTryBiometricUnlock: () -> Unit,
    onUnlockWithPassword: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    var passwordInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onTryBiometricUnlock()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
            .background(palette.bg)
            .padding(24.dp)
            .testTag("app-lock-overlay"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(26.dp), spotColor = Color(0x33151729))
                .clip(RoundedCornerShape(26.dp))
                .background(palette.surface)
                .border(1.dp, palette.line, RoundedCornerShape(26.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(RfColors.RosaBrand, RfColors.VioletaBrand))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.LOCK, tint = Color.White, size = 26.dp)
            }
            BasicText(
                text = "Sesión protegida",
                style = typo.cardTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
            )
            BasicText(
                text = "Verifica tu identidad con biometría o ingresa tu contraseña para continuar.",
                style = typo.body.copy(fontSize = 13.5.sp, color = palette.muted, textAlign = TextAlign.Center),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(RfColors.RosaBrand, RfColors.VioletaBrand)))
                    .clickable { onTryBiometricUnlock() },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Desbloquear con biometría",
                    style = typo.buttonLabel.copy(color = Color.White, fontSize = 14.5.sp),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.chipBg)
                    .border(1.dp, palette.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (passwordInput.isEmpty()) {
                    BasicText(
                        text = "Contraseña de tu cuenta",
                        style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                    )
                }
                BasicTextField(
                    value = passwordInput,
                    onValueChange = { updated -> passwordInput = updated.take(64) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    cursorBrush = SolidColor(palette.ink),
                    textStyle = typo.body.copy(fontSize = 14.sp, color = palette.ink),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                BasicText(
                    text = errorMessage,
                    style = typo.caption.copy(fontSize = 12.5.sp, color = RfColors.Error, textAlign = TextAlign.Center),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.chipBg)
                        .clickable { onLogout() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Cerrar sesión",
                        style = typo.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.ink)
                        .clickable { onUnlockWithPassword(passwordInput) },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Desbloquear con clave",
                        style = typo.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = palette.surface),
                    )
                }
            }
        }
    }
}
