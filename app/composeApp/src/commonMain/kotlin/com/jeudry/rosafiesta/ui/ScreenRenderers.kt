package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.AdminTabBar
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.designsystem.PersonalTabBar
import com.jeudry.rosafiesta.designsystem.RfBottomSheet
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfField
import com.jeudry.rosafiesta.designsystem.RfGradientPriceText
import com.jeudry.rosafiesta.designsystem.RfHeroCard
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconButton
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfIconTile
import com.jeudry.rosafiesta.designsystem.RfIllo
import com.jeudry.rosafiesta.designsystem.RfIlloKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfOfflineBanner
import com.jeudry.rosafiesta.designsystem.RfPhoneFrame
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfSegmented
import com.jeudry.rosafiesta.designsystem.RfSkeletonScreen
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfTheme
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RfToast
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.EventStatus

/**
 * Renderiza cualquier pantalla de [ScreenCatalog] dentro de su marco oficial (390×844 móvil o 1440×900 web).
 */
@Composable
fun RenderScreenSpec(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel? = null,
    onPrimaryAction: () -> Unit = {},
    onSelectScreenCode: (String) -> Unit = {},
    onSwitchAppMode: () -> Unit = {},
    isFrameHarness: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (spec.isDesktopWeb) {
        RenderAdminWebDesktopA39(
            spec = spec,
            onPrimaryAction = onPrimaryAction,
            onSelectScreenCode = onSelectScreenCode,
            fillAvailable = !isFrameHarness,
            modifier = modifier,
        )
        return
    }

    val offlineQueue = viewModel?.offlineQueue?.collectAsState()?.value ?: CanonicalSampleData.offlineQueue
    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    val isCustomSelfContainedScreen = spec.code in setOf(
        "p00", "A00", "s01", "s02", "s03", "s04", "s05", "s06", "s07", "s08", "s09", "s10", "s11", "s12", "s13", "s14", "s15",
        "s18", "s19", "s20", "s21", "s22", "s23", "s24",
        "s25", "s26", "s27", "s28", "s29", "s30", "s31", "s32", "s33", "s34", "s35", "s36", "s78", "A08",
        "s37", "s38", "s39", "s40", "s43", "s44", "s45", "s46", "s47", "s48",
        "s49", "s50", "s51", "s52", "s53", "s54", "s55", "s56", "s57", "s58", "s83",
    )
    val showBottomBar = when {
        spec.code in setOf(
            "p00", "A00", "s03", "s04", "s05", "s06", "s07", "s08", "s09", "s10", "s11", "s12", "s13", "s19", "s22", "A30",
            "s25", "s26", "s27", "s28", "s29", "s30", "s31", "s32", "s33", "s34", "s35", "s36", "s78", "A08",
            "s38", "s39", "s40", "s43", "s44", "s45", "s46", "s47", "s48",
            "s49", "s50", "s51", "s52", "s53", "s55", "s56", "s57", "s58", "s83",
        ) -> false

        else -> true
    }

    RfPhoneFrame(
        variant = spec.variant,
        screenCode = spec.code,
        screenTitle = spec.title,
        showMockStatusBar = isFrameHarness && spec.code !in setOf("p00", "A00"),
        wrapInPhoneBorder = isFrameHarness,
        bottomBar = {
            if (showBottomBar) {
                if (isAdmin) {
                    AdminTabBar(
                        activeTab = ScreenCatalog.activeAdminTab(spec.code),
                        onSelectTab = { tab ->
                            when (tab) {
                                AdminTab.INICIO -> onSelectScreenCode("A01")
                                AdminTab.EVENTOS -> onSelectScreenCode("A11")
                                AdminTab.ROSA -> onSelectScreenCode("A08")
                                AdminTab.INVENTARIO -> onSelectScreenCode("A09")
                                AdminTab.MAS -> onSelectScreenCode("A12")
                            }
                        },
                    )
                } else {
                    val guestMode = ScreenCatalog.isGuestScreen(spec.code)
                    PersonalTabBar(
                        activeTab = ScreenCatalog.activePersonalTab(spec.code),
                        guestMode = guestMode,
                        dark = spec.variant == RfThemeVariant.PERSONAL_DARK,
                        onSelectTab = { tab ->
                            when (tab) {
                                PersonalTab.INICIO -> onSelectScreenCode(if (guestMode) "s01" else "s14")
                                PersonalTab.CATALOGO -> onSelectScreenCode(if (guestMode) "s01" else "s18")
                                PersonalTab.ROSA -> onSelectScreenCode(if (guestMode) "s75" else "s26")
                                PersonalTab.EVENTOS -> onSelectScreenCode("s37")
                                PersonalTab.FAVORITOS -> onSelectScreenCode("s24")
                                PersonalTab.PAQUETES -> onSelectScreenCode("s21")
                                PersonalTab.ENTRAR -> onSelectScreenCode("s07")
                            }
                        },
                    )
                }
            }
        },
        modifier = modifier.testTag("screen-${spec.code}"),
    ) {
        if (ScreenCatalog.isBottomSheet(spec.code)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (spec.code !in setOf("s14", "s15")) {
                    ScreenHeaderBlock(spec, onSelectScreenCode)
                }
                RfBottomSheet(
                    title = spec.title,
                    onDismiss = onPrimaryAction,
                    modifier = Modifier.weight(1f),
                ) {
                    RenderSheetSpecificContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)
                }
            }
            return@RfPhoneFrame
        }

        if (isCustomSelfContainedScreen) {
            when (spec.code) {
                "p00" -> RenderPersonalSplashP00(onPrimaryAction = onPrimaryAction)
                "A00" -> RenderAdminSplashA00(onPrimaryAction = onPrimaryAction)
                "s01" -> RenderGuestCatalogS01(onSelectScreenCode = onSelectScreenCode)
                "s02" -> RenderGuestFavoritesS02(onSelectScreenCode = onSelectScreenCode)
                "s03", "s04", "s05" -> RenderOnboardingScreen(spec = spec, onSelectScreenCode = onSelectScreenCode)
                "s06" -> RenderBienvenidaScreen(spec = spec, onSelectScreenCode = onSelectScreenCode)
                "s07" -> RenderLoginScreenS07(viewModel = viewModel, onPrimaryAction = onPrimaryAction, onSelectScreenCode = onSelectScreenCode)
                "s08" -> RenderRegisterScreenS08(onPrimaryAction = onPrimaryAction, onSelectScreenCode = onSelectScreenCode)
                "s09", "s10", "s11", "s12", "s13" -> RenderRecoveryAndConfirmationScreens(spec = spec, onPrimaryAction = onPrimaryAction, onSelectScreenCode = onSelectScreenCode)
                "s14", "s15" -> RenderPersonalHomeScreen(spec = spec, onSelectScreenCode = onSelectScreenCode)
                "s18" -> RenderPersonalCatalogS18(onSelectScreenCode = onSelectScreenCode)
                "s19" -> RenderProductDetailS19(onSelectScreenCode = onSelectScreenCode)
                "s20" -> RenderCategoriesScreenS20(onSelectScreenCode = onSelectScreenCode)
                "s21" -> RenderPackagesScreenS21(onSelectScreenCode = onSelectScreenCode)
                "s22" -> RenderPackageDetailScreenS22(onSelectScreenCode = onSelectScreenCode)
                "s23" -> RenderShopScreenS23(onSelectScreenCode = onSelectScreenCode)
                "s24" -> RenderClientFavoritesScreenS24(onSelectScreenCode = onSelectScreenCode)
                "s25" -> RenderMyEventDraftS25(onSelectScreenCode = onSelectScreenCode)
                "s26" -> RenderRosaEventTypeS26(onSelectScreenCode = onSelectScreenCode)
                "s27" -> RenderRosaTutorialS27(onSelectScreenCode = onSelectScreenCode)
                "s28" -> RenderRosaHelpS28(onSelectScreenCode = onSelectScreenCode)
                "s29" -> RenderRosaSuggestionsS29(onSelectScreenCode = onSelectScreenCode)
                "s30" -> RenderRosaCategoriesS30(onSelectScreenCode = onSelectScreenCode)
                "s31" -> RenderRosaDetailsS31(onSelectScreenCode = onSelectScreenCode)
                "s32" -> RenderRosaSummaryS32(onSelectScreenCode = onSelectScreenCode)
                "s33" -> RenderRosaSubmittedS33(onSelectScreenCode = onSelectScreenCode)
                "s34" -> RenderRosaChatS34(onSelectScreenCode = onSelectScreenCode)
                "s35" -> RenderRosaVoiceS35(onSelectScreenCode = onSelectScreenCode)
                "s36" -> RenderRosaSketchS36(onSelectScreenCode = onSelectScreenCode)
                "s78" -> RenderRosaSketchConfirmS78(onSelectScreenCode = onSelectScreenCode)
                "A08" -> RenderAdminRosaA08(onSelectScreenCode = onSelectScreenCode)
                "s37" -> RenderMyEventsScreenS37(onSelectScreenCode = onSelectScreenCode)
                "s38" -> RenderNewEventScreenS38(onSelectScreenCode = onSelectScreenCode)
                "s39" -> RenderCalendarScreenS39(onSelectScreenCode = onSelectScreenCode)
                "s40" -> RenderEventDetailScreenS40(onSelectScreenCode = onSelectScreenCode)
                "s43" -> RenderExecutionModeScreenS43(onSelectScreenCode = onSelectScreenCode)
                "s44" -> RenderTimelineScreenS44(onSelectScreenCode = onSelectScreenCode)
                "s45" -> RenderTasksScreenS45(onSelectScreenCode = onSelectScreenCode)
                "s46" -> RenderChecklistScreenS46(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)
                "s47" -> RenderGuestsScreenS47(onSelectScreenCode = onSelectScreenCode)
                "s48" -> RenderQuoteScreenS48(onSelectScreenCode = onSelectScreenCode)
                "s49" -> RenderQuoteHistoryScreenS49(onSelectScreenCode = onSelectScreenCode)
                "s50" -> RenderPaymentCheckoutScreenS50(onSelectScreenCode = onSelectScreenCode)
                "s51" -> RenderPaymentSuccessScreenS51(onSelectScreenCode = onSelectScreenCode)
                "s52" -> RenderPostEventReviewScreenS52(onSelectScreenCode = onSelectScreenCode)
                "s53" -> RenderMyReservationsScreenS53(onSelectScreenCode = onSelectScreenCode)
                "s54" -> RenderUserProfileScreenS54(onSelectScreenCode = onSelectScreenCode)
                "s55" -> RenderSuppliersScreenS55(onSelectScreenCode = onSelectScreenCode)
                "s56" -> RenderNewSupplierScreenS56(onSelectScreenCode = onSelectScreenCode)
                "s57" -> RenderSpendingAnalyticsScreenS57(onSelectScreenCode = onSelectScreenCode)
                "s58" -> RenderFinancialDashboardScreenS58(onSelectScreenCode = onSelectScreenCode)
                "s83" -> RenderOfficialReceiptScreenS83(onSelectScreenCode = onSelectScreenCode)
            }
            return@RfPhoneFrame
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
        ) {
            if (spec.code == "A38") {
                RfToast(
                    message = "Evento actualizado y sincronizado con el equipo",
                    actionLabel = "Ver",
                    isAdmin = true,
                    onAction = onPrimaryAction,
                )
            }
            if (spec.code in setOf("s91", "A37")) {
                RfOfflineBanner(
                    pendingChangesCount = offlineQueue.size,
                    isAdmin = isAdmin,
                    onRetry = {
                        viewModel?.syncOfflineQueue()
                        onPrimaryAction()
                    },
                )
            }

            if (spec.code !in setOf("s14", "s15")) {
                ScreenHeaderBlock(spec, onSelectScreenCode)
            }

            RenderScreenBodyByCode(
                spec = spec,
                viewModel = viewModel,
                onPrimaryAction = onPrimaryAction,
                onSelectScreenCode = onSelectScreenCode,
                onSwitchAppMode = onSwitchAppMode,
            )

            if (spec.code == "s92") {
                RfToast(
                    message = "120 Sillas Tiffany agregadas a tu Boda Real",
                    actionLabel = "Deshacer",
                    isAdmin = false,
                    onAction = onPrimaryAction,
                )
            }
        }
    }
}

@Composable
private fun ScreenHeaderBlock(
    spec: ScreenSpec,
    onSelectScreenCode: (String) -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val isRootTab = spec.code in setOf("s01", "s02", "s14", "s15", "s18", "s21", "s24", "s37", "A01", "A11", "A09", "A12")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            if (isRootTab || spec.code in setOf("p00", "A00")) {
                RfLogo(size = RfLogoSize.SMALL)
            } else {
                RfIconButton(
                    icon = RfIconKind.BACK,
                    contentDescription = "Volver",
                    onClick = { onSelectScreenCode(if (palette.isAdmin) "A01" else "s14") },
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = spec.sectionTitle.uppercase(),
                    style = typo.sectionCap.copy(color = palette.primaryAccent),
                    maxLines = 1,
                )
                BasicText(
                    text = spec.title,
                    style = if (isRootTab) typo.rootTabTitle else typo.backScreenTitle,
                    maxLines = 2,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        when (spec.code) {
            "s14", "s15" -> RfIconButton(
                icon = RfIconKind.BELL,
                contentDescription = "Abrir notificaciones",
                onClick = { onSelectScreenCode("s79") },
            )

            "A01" -> RfIconButton(
                icon = RfIconKind.BELL,
                contentDescription = "Abrir notificaciones del negocio",
                onClick = { onSelectScreenCode("A29") },
            )

            "s40" -> RfIconButton(
                icon = RfIconKind.MORE_VERTICAL,
                contentDescription = "Opciones del evento: cambiar fecha o cancelar",
                onClick = { onSelectScreenCode("s84") },
            )

            else -> RfTag(
                text = spec.highlightMetric,
                tone = if (palette.isAdmin) RfTagTone.INDIGO else RfTagTone.SOL,
            )
        }
    }
    BasicText(
        text = spec.subtitle,
        style = typo.secondary,
    )
}

@Composable
private fun RenderSheetSpecificContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    when (spec.code) {
        "s75" -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfIllo(kind = RfIlloKind.CANDADO, size = 56.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Guarda tus favoritos y cotiza con Rosa IA", style = typo.cardTitle)
                    BasicText("Inicia sesión o crea tu cuenta gratis en menos de 1 minuto.", style = typo.secondary)
                }
            }
            RfButton(label = "Iniciar sesión", onClick = { onSelectScreenCode("s07") })
            RfButton(label = "Crear cuenta nueva", style = RfButtonStyle.SECONDARY, onClick = { onSelectScreenCode("s08") })
        }

        "s76" -> {
            RfCard {
                BasicText("1. Elige tu fecha y tipo de evento", style = typo.bodyStrong)
                BasicText("Verificamos stock real en Santo Domingo para el día de tu celebración.", style = typo.secondary)
                Spacer(Modifier.height(6.dp))
                BasicText("2. Arma tu lista con Rosa IA o el catálogo", style = typo.bodyStrong)
                BasicText("Precios transparentes desde RD$ 4 por Silla Tiffany.", style = typo.secondary)
                Spacer(Modifier.height(6.dp))
                BasicText("3. Reserva con el 30 % de anticipo", style = typo.bodyStrong)
                BasicText("El saldo del 70 % se liquida 24 h antes del montaje.", style = typo.secondary)
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        }

        "s77" -> {
            val commentVal = viewModel?.supportMessage?.collectAsState()?.value
                ?: "El arco orgánico y las 120 sillas Tiffany quedaron impecables en Jardín Botánico."
            RfCard {
                BasicText("Boda Real Rosa & Dorado · Vie 23 oct", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(5) {
                        RfIcon(kind = RfIconKind.STAR, tint = RfColors.Sol, size = 22.dp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                RfField(
                    label = "Tu comentario para María, Pedro y Yuli",
                    value = commentVal,
                    placeholder = "Escribe tu reseña...",
                    onValueChange = if (viewModel != null) ({ viewModel.setSupportMessage(it) }) else null,
                )
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        }

        "s81" -> {
            val selectedCat = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todos"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RfChip("Vie 23 oct", selected = true, onClick = {})
                RfChip(
                    label = "Sillas y mesas",
                    selected = selectedCat == "Mobiliario" || selectedCat == "Todos",
                    onClick = { viewModel?.setSelectedCategory("Mobiliario") },
                )
                RfChip(
                    label = "Arcos",
                    selected = selectedCat == "Arcos",
                    onClick = { viewModel?.setSelectedCategory("Arcos") },
                )
            }
            RfCard {
                BasicText("Rango de precio por día", style = typo.fieldLabel)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    RfGradientPriceText("RD$ 4.00")
                    RfGradientPriceText("RD$ 150.00")
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        }

        "s84" -> {
            val nextDate = viewModel?.dateChangeInput?.collectAsState()?.value ?: "Vie 30 oct"
            val banner = viewModel?.dateChangeBanner?.collectAsState()?.value ?: "Primer cambio de fecha gratis"
            RfCard {
                RfTag(text = banner, tone = RfTagTone.LIMA)
                Spacer(Modifier.height(6.dp))
                BasicText("Fecha actual: Vie 23 oct · Jardín Botánico", style = typo.bodyStrong)
                BasicText("Nueva fecha solicitada: $nextDate · Stock de 145 artículos disponible", style = typo.secondary)
                if (viewModel != null) {
                    Spacer(Modifier.height(6.dp))
                    RfField(
                        label = "Nueva fecha solicitada",
                        value = nextDate,
                        placeholder = "Ej. Vie 30 oct",
                        onValueChange = { viewModel.setDateChangeInput(it) },
                    )
                }
            }
            RfButton(
                label = spec.primaryCta,
                onClick = {
                    viewModel?.submitDateChange()
                    onPrimaryAction()
                },
            )
        }

        "s85" -> {
            RfCard {
                BasicText("Política de cancelación", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                BasicText("• Más de 7 días antes: 90 % de reembolso (RD$ 1,215)", style = typo.body)
                BasicText("• De 3 a 7 días antes: 50 % de reembolso (RD$ 675)", style = typo.body)
                BasicText("• Menos de 3 días antes: sin reembolso (RD$ 0)", style = typo.body)
            }
            RfButton(
                label = spec.primaryCta,
                style = RfButtonStyle.DANGER,
                onClick = {
                    viewModel?.submitCancelEvent()
                    onPrimaryAction()
                },
            )
        }
    }
}

@Composable
private fun RenderScreenBodyByCode(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
    onSwitchAppMode: () -> Unit,
) {
    val activeRole = viewModel?.activeRole?.collectAsState()?.value ?: "client"
    val staffList = viewModel?.staffMembers?.collectAsState()?.value ?: CanonicalSampleData.staffMembers
    val yuliCanViewPayments = staffList.firstOrNull { it.name.equals("Yuli", ignoreCase = true) }?.canViewPayments ?: false
    if (spec.code in setOf("A20", "A21", "A32", "A33") && activeRole == "staff_yuli" && !yuliCanViewPayments) {
        RenderYuliPaymentRestrictedScreen(spec, onSelectScreenCode)
        return
    }

    when (spec.code) {
        // Estados explícitos de la Sección 5
        "s90", "A36" -> {
            RfSkeletonScreen(isAdmin = spec.variant == RfThemeVariant.ADMIN)
        }

        "s89", "A35" -> {
            RenderEmptyStateScreen(spec, onPrimaryAction, onSelectScreenCode)
        }

        "s91", "A37" -> {
            RenderOfflineAndServerErrorScreen(spec, viewModel, onPrimaryAction)
        }

        "s88" -> {
            RenderDeleteAccountDisabledScreen(spec, viewModel, onPrimaryAction)
        }

        "s83", "A33" -> {
            RenderBigAmountReceiptOrRefundScreen(spec, onPrimaryAction)
        }

        "s35" -> {
            RenderRosaVoiceScreen(spec, onPrimaryAction)
        }

        "s36", "s78", "A18" -> {
            RenderSketchAndFloorPlanScreen(spec, onPrimaryAction, onSelectScreenCode)
        }

        "s44" -> {
            RenderTimelineScheduleScreen(spec, onPrimaryAction)
        }

        "s45", "s46" -> {
            RenderPersonalChecklistAndTasksScreen(spec, viewModel, onPrimaryAction)
        }

        "s47" -> {
            RenderGuestsAndTablesScreen(spec, onPrimaryAction)
        }

        "s79", "A29" -> {
            RenderNotificationsScreen(spec, onPrimaryAction, onSelectScreenCode)
        }

        "A05", "A06", "A07" -> {
            RenderAdminManualEventWizardScreen(spec, viewModel, onPrimaryAction, onSelectScreenCode)
        }

        "s57", "s68" -> {
            RenderAdminReportsAndUsageScreen(spec, onPrimaryAction)
        }

        "A28" -> {
            RenderTeamPermissionsScreen(spec, viewModel, onPrimaryAction)
        }

        "A30" -> {
            RenderAdminRoleSwitcherScreen(spec, viewModel, onSwitchAppMode, onPrimaryAction, onSelectScreenCode)
        }

        "s55", "s56" -> {
            RenderSuppliersScreen(spec, onPrimaryAction)
        }

        "s71", "A34" -> {
            RenderReviewsAndReplyScreen(spec, viewModel, onPrimaryAction)
        }

        "s72", "s73", "s74" -> {
            RenderWhatsAppBotScreen(spec, viewModel, onPrimaryAction)
        }

        "s41" -> {
            RenderTeamDirectChatScreen(spec, viewModel, onPrimaryAction)
        }

        else -> {
            when (spec.archetype) {
                ScreenArchetype.BRAND_COVER, ScreenArchetype.SPLASH, ScreenArchetype.ONBOARDING ->
                    RenderSplashAndOnboardingContent(spec, onPrimaryAction)

                ScreenArchetype.AUTH_FORM ->
                    RenderAuthFormByCode(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_HOME ->
                    RenderPersonalHomeContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_CATALOG, ScreenArchetype.PERSONAL_PRODUCT_DETAIL ->
                    RenderPersonalCatalogContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_CART_QUOTE, ScreenArchetype.PERSONAL_CONTRACT_SIGN, ScreenArchetype.PERSONAL_PAYMENT ->
                    RenderPersonalQuoteAndPaymentContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_EVENT_HUB, ScreenArchetype.PERSONAL_DATE_CANCEL ->
                    RenderPersonalEventsContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_ROSA_CHAT ->
                    RenderRosaAssistantContent(spec, viewModel, isAdmin = false, onPrimaryAction = onPrimaryAction)

                ScreenArchetype.PERSONAL_PROFILE, ScreenArchetype.PERSONAL_SYSTEM_STATE ->
                    RenderPersonalProfileAndStateContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.ADMIN_LOGIN_GATE, ScreenArchetype.ADMIN_ROLE_SWITCHER ->
                    RenderAdminRoleSwitcherScreen(spec, viewModel, onSwitchAppMode, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.ADMIN_DASHBOARD, ScreenArchetype.ADMIN_CALENDAR ->
                    RenderAdminDashboardAndCalendarContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.ADMIN_QUOTES ->
                    RenderAdminQuotesContent(spec, viewModel, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.ADMIN_LOGISTICS, ScreenArchetype.ADMIN_OFFLINE_SYNC ->
                    RenderAdminLogisticsContent(spec, viewModel, onPrimaryAction)

                ScreenArchetype.ADMIN_ROSA_COPILOT ->
                    RenderRosaAssistantContent(spec, viewModel, isAdmin = true, onPrimaryAction = onPrimaryAction)

                ScreenArchetype.ADMIN_FINANCE ->
                    RenderAdminFinanceContent(spec, viewModel, onPrimaryAction)

                ScreenArchetype.ADMIN_INVENTORY_STAFF ->
                    RenderAdminInventoryAndStaffContent(spec, viewModel, onPrimaryAction)

                ScreenArchetype.ADMIN_WEB_DESKTOP -> Unit
            }
        }
    }
}

@Composable
private fun RenderPersonalChecklistAndTasksScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val checklist = viewModel?.checklist?.collectAsState()?.value ?: CanonicalSampleData.bodaChecklist
    val completedCount = checklist.count { it.completed }
    val progressPct = (completedCount * 100) / checklist.size.coerceAtLeast(1)

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Boda Real Rosa & Dorado · Vie 23 oct",
                style = typo.cardTitle,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            RfTag(
                text = "$progressPct % ($completedCount/${checklist.size})",
                tone = RfTagTone.LIMA,
            )
        }
        Spacer(Modifier.height(4.dp))
        BasicText(
            text = "Jardín Botánico · Entrega y montaje verificados paso a paso",
            style = typo.secondary,
        )
        Spacer(Modifier.height(8.dp))
        checklist.forEach { step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel?.toggleChecklistStep(step.id) }
                    .padding(vertical = 4.dp)
                    .testTag("checklist-step-${step.id}"),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    RfIcon(
                        kind = if (step.completed) RfIconKind.CHECK else RfIconKind.CALENDAR,
                        tint = if (step.completed) RfColors.LimaTintInk else RfColors.RosaTintInk,
                        size = 16.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicText(
                        text = step.title,
                        style = typo.body.copy(fontSize = 13.sp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                RfTag(
                    text = step.completedBy,
                    tone = if (step.completed) RfTagTone.LIMA else RfTagTone.SOL,
                )
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderNotificationsScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    val notifications = if (isAdmin) {
        listOf(
            Triple("Nueva solicitud · Cumpleaños Safari Mateo", "Laura Méndez · Dom 25 oct · 60 inv. · RD$ 1,920", RfTagTone.INDIGO),
            Triple("Comprobante #REC-2026-0185 recibido", "Sofía Castillo · Quinceañera Valentina · RD$ 1,860", RfTagTone.SOL),
            Triple("Alerta de stock · Silla Tiffany Dorada", "160 de 200 u. reservadas para el vie 23 oct (80 %)", RfTagTone.TEAL),
            Triple("Checklist de montaje al 66 % (4/6)", "Pedro reportó avance en Jardín Botánico · Boda Real", RfTagTone.LIMA),
        )
    } else {
        listOf(
            Triple("Anticipo 30 % confirmado · RD$ 1,350", "Recibo #REC-2026-0184 · Boda Real Rosa & Dorado", RfTagTone.LIMA),
            Triple("Pedro y Yuli están en Jardín Botánico", "Montaje de 15 mesas y 120 sillas Tiffany en curso (4/6)", RfTagTone.ROSA),
            Triple("Recordatorio de saldo 70 % · RD$ 3,150", "Vence jue 22 oct (24 h antes de tu celebración)", RfTagTone.SOL),
        )
    }

    notifications.forEach { (title, detail, tone) ->
        RfCard(onClick = { onSelectScreenCode(if (isAdmin) "A03" else "s40") }) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(title, style = typo.bodyStrong)
                    Spacer(Modifier.height(2.dp))
                    BasicText(detail, style = typo.secondary)
                }
                Spacer(Modifier.width(8.dp))
                RfTag(
                    text = if (tone == RfTagTone.LIMA) "Listo" else "Nuevo",
                    tone = tone,
                )
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderYuliPaymentRestrictedScreen(
    spec: ScreenSpec,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    RfCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RfIconTile(
                icon = RfIconKind.SHIELD,
                backgroundColor = RfColors.ErrorTint,
                iconTint = RfColors.Error,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                RfTag(text = "Acceso restringido · Rol Yuli", tone = RfTagTone.SOL)
                Spacer(Modifier.height(4.dp))
                BasicText("Sin permiso de pagos ni reembolsos (${spec.code})", style = typo.cardTitle)
                BasicText(
                    "«Tu rol de Decoración y tareas no incluye acceso a pagos». María puede habilitar el permiso en A28.",
                    style = typo.secondary,
                )
            }
        }
    }
    RfButton(
        label = "Solicitar permiso en A28",
        style = RfButtonStyle.SECONDARY,
        onClick = { onSelectScreenCode("A28") },
    )
    RfButton(
        label = "Volver al inicio administrativo",
        onClick = { onSelectScreenCode("A01") },
    )
}

@Composable
private fun RenderTeamDirectChatScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val messages = viewModel?.chatMessages?.collectAsState()?.value ?: CanonicalSampleData.initialChatMessages
    val chatInput = viewModel?.chatInput?.collectAsState()?.value ?: ""

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Canal directo · Boda Real Rosa & Dorado", style = typo.cardTitle, modifier = Modifier.weight(1f))
            RfTag(text = "En línea", tone = RfTagTone.LIMA)
        }
        Spacer(Modifier.height(6.dp))
        messages.takeLast(4).forEach { msg ->
            val author = msg.substringBefore(":", "Equipo")
            val body = msg.substringAfter(":", msg).trim()
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                BasicText(author, style = typo.bodyStrong.copy(fontSize = 12.5.sp))
                BasicText(body, style = typo.secondary)
            }
        }
        Spacer(Modifier.height(6.dp))
        RfField(
            label = "Mensaje para María, Pedro y Yuli",
            value = chatInput,
            placeholder = "Escribe un mensaje para el equipo...",
            onValueChange = { viewModel?.setChatInput(it) },
        )
    }
    RfButton(
        label = spec.primaryCta,
        onClick = {
            viewModel?.submitChatMessage("Camila")
            onPrimaryAction()
        },
    )
}

@Composable
private fun RenderAdminManualEventWizardScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val isStep1 = spec.code == "A05"
    val isStep2 = spec.code == "A06"
    val isStep3 = spec.code == "A07"
    val clientVal = viewModel?.newEventClient?.collectAsState()?.value ?: "Laura Méndez · +1 (809) 555-0194"
    val titleVal = viewModel?.newEventTitle?.collectAsState()?.value ?: "Cumpleaños Safari Mateo · Infantil"
    val dateVal = viewModel?.newEventDate?.collectAsState()?.value ?: "Dom 25 oct 2026 · 9:00 a. m. · Club Naco (60 inv.)"
    val formError = viewModel?.formError?.collectAsState()?.value

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RfChip(
            label = "1 · Datos",
            selected = isStep1,
            onClick = { onSelectScreenCode("A05") },
        )
        RfChip(
            label = "2 · Artículos",
            selected = isStep2,
            onClick = { onSelectScreenCode("A06") },
        )
        RfChip(
            label = "3 · Resumen",
            selected = isStep3,
            onClick = { onSelectScreenCode("A07") },
        )
    }

    if (isStep1) {
        RfCard {
            RfField(
                label = "Cliente y WhatsApp",
                value = if (viewModel == null) "Laura Méndez · +1 (809) 555-0194" else clientVal,
                placeholder = "Nombre y teléfono del cliente",
                onValueChange = if (viewModel != null) ({ viewModel.setNewEventClient(it) }) else null,
            )
            Spacer(Modifier.height(8.dp))
            RfField(
                label = "Nombre y tipo de evento",
                value = if (viewModel == null) "Cumpleaños Safari Mateo · Infantil" else titleVal,
                placeholder = "Título de la celebración",
                onValueChange = if (viewModel != null) ({ viewModel.setNewEventTitle(it) }) else null,
            )
            Spacer(Modifier.height(8.dp))
            RfField(
                label = "Fecha, hora y salón",
                value = if (viewModel == null) "Dom 25 oct 2026 · 9:00 a. m. · Club Naco (60 inv.)" else dateVal,
                placeholder = "Fecha y lugar del evento",
                helperOrError = formError,
                isError = formError != null,
                onValueChange = if (viewModel != null) ({ viewModel.setNewEventDate(it) }) else null,
            )
        }
    } else {
        val mateo = CanonicalSampleData.events[2]
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = if (isStep3) titleVal else mateo.title,
                    style = typo.cardTitle,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                RfTag(text = if (isStep3) "3 · Resumen listo" else "Stock libre", tone = RfTagTone.LIMA)
            }
            if (isStep3) {
                Spacer(Modifier.height(4.dp))
                BasicText("Cliente: $clientVal", style = typo.secondary)
                BasicText("Agenda: $dateVal", style = typo.secondary)
            }
            Spacer(Modifier.height(6.dp))
            mateo.items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    BasicText("${item.quantity} × ${item.name}", style = typo.body)
                    BasicText(RosaFiestaEngine.formatDop(item.subtotalDop), style = typo.bodyStrong)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("Total cotizado (Anticipo 30 %: RD$ 576)", style = typo.bodyStrong, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                RfTag(text = RosaFiestaEngine.formatDop(mateo.totalDop), tone = RfTagTone.INDIGO)
            }
        }
    }

    RfButton(
        label = spec.primaryCta,
        onClick = {
            if ((isStep1 || isStep3) && viewModel != null) {
                viewModel.submitNewEvent()
            }
            onPrimaryAction()
        },
    )
}

@Composable
private fun RenderAdminReportsAndUsageScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val metrics = CanonicalSampleData.businessMetrics

    RfHeroCard(useAdminGradient = true) {
        BasicText("Rendimiento y rotación · ${metrics.monthLabel}", style = typo.secondary.copy(color = Color.White))
        BasicText(
            text = "${metrics.occupancyRatePercent} % uso de stock · Ingresos ${RosaFiestaEngine.formatDop(metrics.octoberRevenueDop)}",
            style = typo.backScreenTitle.copy(color = Color.White),
        )
    }

    val usageRows = listOf(
        Triple("Silla Tiffany Dorada (ART-001)", "160 / 200 u. rotadas · 4 eventos en oct", "80 % uso"),
        Triple("Arco Orgánico Rosa & Globos (ART-005)", "3 / 4 u. rotadas · Bodas y Quinceañeras", "75 % uso"),
        Triple("Mesa Redonda 10 personas (ART-003)", "22 / 30 u. rotadas · Jardín Botánico y Embajador", "73 % uso"),
        Triple("Centro Floral Rosa & Eucalipto (ART-004)", "18 / 25 u. rotadas · Margen neto 68 %", "72 % uso"),
    )

    usageRows.forEach { (name, detail, badge) ->
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(name, style = typo.bodyStrong)
                    BasicText(detail, style = typo.secondary)
                }
                Spacer(Modifier.width(8.dp))
                RfTag(text = badge, tone = RfTagTone.TEAL)
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderEmptyStateScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    RfCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        ) {
            RfIllo(
                kind = if (isAdmin) RfIlloKind.FLORES else RfIlloKind.GLOBOS,
                size = 84.dp,
            )
            BasicText(
                text = if (isAdmin) "Sin solicitudes pendientes hoy" else "Aún no tienes eventos planeados",
                style = typo.sheetOrEmptyTitle,
            )
            BasicText(
                text = if (isAdmin) {
                    "Todas las cotizaciones del día fueron respondidas. Puedes crear un evento manual o con Rosa IA."
                } else {
                    "Empieza tu Boda, Quinceañera o Cumpleaños con ayuda de Rosa IA o explorando el catálogo."
                },
                style = typo.secondary,
            )
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            RfButton(
                label = if (isAdmin) "Ver historial de cotizaciones" else "Explorar catálogo de artículos",
                style = RfButtonStyle.SECONDARY,
                onClick = { onSelectScreenCode(if (isAdmin) "A22" else "s18") },
            )
        }
    }
}

@Composable
private fun RenderOfflineAndServerErrorScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val offlineQueue = viewModel?.offlineQueue?.collectAsState()?.value ?: CanonicalSampleData.offlineQueue
    RfCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RfIconTile(
                icon = RfIconKind.WARNING,
                backgroundColor = RfColors.ErrorTint,
                iconTint = RfColors.Error,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText("Algo salió mal de nuestro lado…", style = typo.cardTitle)
                BasicText(
                    "Tus cambios están guardados localmente en el dispositivo y se sincronizarán al reconectar.",
                    style = typo.secondary,
                )
            }
        }
    }
    offlineQueue.forEach { item ->
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(item.actionLabel, style = typo.bodyStrong)
                    BasicText("${item.entityCode} · Guardado local (${item.timeAgo})", style = typo.secondary)
                }
                RfTag(text = "Guardado local", tone = RfTagTone.SOL)
            }
        }
    }
    RfButton(
        label = "Reintentar",
        onClick = {
            viewModel?.syncOfflineQueue()
            onPrimaryAction()
        },
    )
}

@Composable
private fun RenderDeleteAccountDisabledScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val activeEventsCount = events.count {
        it.clientName.contains("Camila", ignoreCase = true) &&
            it.status != EventStatus.CANCELLED &&
            it.status != EventStatus.COMPLETED
    }
    val canDelete = RosaFiestaEngine.canDeleteAccount(activeEventsCount)

    RfCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RfIconTile(
                icon = RfIconKind.WARNING,
                backgroundColor = RfColors.ErrorTint,
                iconTint = RfColors.Error,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText("Validación de eventos activos", style = typo.cardTitle.copy(color = RfColors.Error))
                BasicText(
                    if (activeEventsCount > 0) {
                        "Tienes $activeEventsCount eventos activos (Boda Real Rosa & Dorado el vie 23 oct y Quinceañera Valentina Crystal el lun 2 nov)."
                    } else {
                        "No tienes eventos activos pendientes. Ya puedes eliminar tu cuenta si lo deseas."
                    },
                    style = typo.secondary,
                )
            }
        }
    }
    RfButton(
        label = "Eliminar cuenta permanentemente",
        style = RfButtonStyle.DANGER,
        enabled = canDelete,
        onClick = onPrimaryAction,
    )
    RfButton(
        label = "Volver a mi perfil",
        style = RfButtonStyle.SECONDARY,
        onClick = onPrimaryAction,
    )
}

@Composable
private fun RenderBigAmountReceiptOrRefundScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val isRefund = spec.code == "A33"
    RfCard {
        RfTag(
            text = if (isRefund) "Reembolso 90 % (>7 días antes)" else "Recibo oficial #REC-2026-0184",
            tone = if (isRefund) RfTagTone.TEAL else RfTagTone.LIMA,
        )
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = if (isRefund) "RD$ 1,215" else "RD$ 1,350",
            style = typo.bigAmount,
        )
        Spacer(Modifier.height(4.dp))
        BasicText(
            text = if (isRefund) {
                "Boda Real Rosa & Dorado · Camila Rodríguez · Retención operativa 10 % (RD$ 135)"
            } else {
                "Anticipo 30 % · Boda Real Rosa & Dorado · Transferencia Banreservas · 18 oct"
            },
            style = typo.secondary,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            BasicText("Total del evento", style = typo.body)
            BasicText("RD$ 4,500", style = typo.bodyStrong)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            BasicText("Saldo pendiente (vence jue 22 oct)", style = typo.body)
            BasicText("RD$ 3,150", style = typo.bodyStrong)
        }
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderRosaVoiceScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RfDimens.CardRadius))
            .background(palette.rosaAssistantBgGradient)
            .padding(20.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RosaAvatar(size = RosaAvatarSize.XLARGE, glow = true)
            RfTag(text = "Escuchando en vivo · Micrófono activo", tone = RfTagTone.VIOLETA)
            BasicText(
                text = "«Quiero 120 sillas Tiffany doradas, 15 mesas redondas y un arco orgánico rosa para el viernes 23 de octubre en Jardín Botánico…»",
                style = typo.bodyStrong,
            )
            BasicText(
                text = "Rosa IA detectó 145 artículos · Total estimado RD$ 4,500",
                style = typo.secondary,
            )
        }
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderSketchAndFloorPlanScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = if (isAdmin) "Plano técnico · Jardín Botánico (20×15 m)" else "Lienzo de boceto · 15 mesas redondas",
                style = typo.cardTitle,
            )
            RfTag(
                text = "120 invitados",
                tone = if (isAdmin) RfTagTone.INDIGO else RfTagTone.ROSA,
            )
        }
        Spacer(Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isAdmin) RfColors.IndigoTint else RfColors.RosaTint)
                .padding(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                RfTag(text = "Arco orgánico (Entrada)", tone = RfTagTone.ROSA)
                RfTag(text = "Mesa dulce (Fondo)", tone = RfTagTone.SOL)
            }
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                (1..5).forEach { tableNum ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                    ) {
                        BasicText("M$tableNum", style = typo.fieldLabel)
                    }
                }
            }
            BasicText(
                text = "15 mesas × 8 sillas Tiffany = 120 puestos + 8 centros florales",
                style = typo.secondary,
            )
        }
    }
    if (!isAdmin) {
        RfButton(
            label = "Ayúdame con IA a distribuir mesas",
            style = RfButtonStyle.SECONDARY,
            onClick = { onSelectScreenCode("s78") },
        )
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderTimelineScheduleScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val milestones = listOf(
        Triple("8:15 a. m.", "Carga en almacén Santo Domingo", "Andrés · Completado"),
        Triple("9:00 a. m.", "Llegada y descarga en Jardín Botánico", "Pedro · Completado"),
        Triple("11:30 a. m.", "Montaje de 15 mesas y 120 sillas Tiffany", "Pedro · En curso"),
        Triple("2:00 p. m.", "Instalación de arco orgánico y 8 centros florales", "Yuli · Programado"),
        Triple("11:30 p. m.", "Desmontaje y retorno a bodega", "Pedro y Andrés"),
    )
    milestones.forEachIndexed { idx, (time, title, meta) ->
        RfCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfTag(text = time, tone = if (idx < 2) RfTagTone.LIMA else RfTagTone.SOL)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(title, style = typo.bodyStrong)
                    BasicText(meta, style = typo.secondary)
                }
            }
        }
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderGuestsAndTablesScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    RfCard {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            BasicText("120 invitados confirmados", style = typo.cardTitle)
            RfTag(text = "15 mesas de 8", tone = RfTagTone.LIMA)
        }
        Spacer(Modifier.height(6.dp))
        BasicText("Mesa 1 · Familia de la novia (8/8 puestos)", style = typo.bodyStrong)
        BasicText("Camila Rodríguez, Sofía Rodríguez, Roberto Rodríguez y 5 más", style = typo.secondary)
        Spacer(Modifier.height(6.dp))
        BasicText("Mesa 2 · Familia del novio (8/8 puestos)", style = typo.bodyStrong)
        BasicText("Daniel Mejía, Elena Mejía, Carlos Mejía y 5 más", style = typo.secondary)
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderTeamPermissionsScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val staffList = viewModel?.staffMembers?.collectAsState()?.value ?: CanonicalSampleData.staffMembers
    staffList.forEach { staff ->
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(staff.name, style = typo.cardTitle)
                    BasicText(staff.roleLabel, style = typo.secondary)
                }
                RfTag(
                    text = if (!staff.canViewPayments) "Sin pagos" else "Acceso rol",
                    tone = if (!staff.canViewPayments) RfTagTone.SOL else RfTagTone.INDIGO,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = if (!staff.canViewPayments) "Decoración y tareas (Pagos desactivados)" else "Ver pagos y cobros",
                    style = typo.body,
                )
                RfSwitch(
                    checked = staff.canViewPayments,
                    onCheckedChange = { viewModel?.toggleStaffPaymentAccess(staff.name) },
                )
            }
        }
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminRoleSwitcherScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onSwitchAppMode: () -> Unit,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    RfHeroCard(useAdminGradient = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RfTag(text = "Elegir cómo entrar · A30", tone = RfTagTone.TEAL)
                Spacer(Modifier.height(6.dp))
                BasicText("RosaFiesta · Selector de acceso", style = typo.backScreenTitle.copy(color = Color.White))
                BasicText(
                    "María (admin) ve todo. El equipo entra con permisos de A28. También puedes revisar la App Personal como cliente.",
                    style = typo.body.copy(color = Color.White.copy(alpha = 0.92f)),
                )
            }
            RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = true, glow = true)
        }
    }
    RfCard(
        onClick = {
            viewModel?.switchRole("admin")
            onSelectScreenCode("A01")
        },
    ) {
        BasicText("Entrar como Dueña (María · Admin)", style = typo.cardTitle)
        BasicText("Acceso completo a eventos, inventario, pagos, reportes y configuración.", style = typo.secondary)
    }
    RfCard(
        onClick = {
            viewModel?.switchRole("staff_yuli")
            onSelectScreenCode("A28")
        },
    ) {
        BasicText("Entrar como Equipo (Pedro, Yuli, Andrés)", style = typo.cardTitle)
        BasicText("Yuli: decoración y tareas, sin pagos · Pedro: logística · Andrés: almacén.", style = typo.secondary)
    }
    RfButton(
        label = "Revisar App Personal como cliente",
        style = RfButtonStyle.SECONDARY,
        onClick = {
            viewModel?.switchRole("client")
            onSwitchAppMode()
            onPrimaryAction()
        },
    )
}

@Composable
private fun RenderSuppliersScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val suppliers = listOf(
        Triple("Florería Jardín Real", "Rosas y follaje fresco · Vie 23 oct 7:00 a. m.", "RD$ 680"),
        Triple("Transporte Del Caribe", "Camión de apoyo para eventos >100 invitados", "RD$ 450"),
        Triple("Globos & Arte RD", "Insumos biodegradables para arcos orgánicos", "RD$ 320"),
    )
    suppliers.forEach { (name, detail, amount) ->
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(name, style = typo.cardTitle)
                    BasicText(detail, style = typo.secondary)
                }
                RfTag(text = amount, tone = RfTagTone.INDIGO)
            }
        }
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderReviewsAndReplyScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val depositVal = viewModel?.depositPercentInput?.collectAsState()?.value ?: "30"
    val deliveryVal = viewModel?.deliveryBaseInput?.collectAsState()?.value ?: "450"
    RfCard {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            BasicText("Camila Rodríguez · Boda Real Rosa & Dorado", style = typo.cardTitle)
            RfTag(text = "5.0 ★", tone = RfTagTone.SOL)
        }
        Spacer(Modifier.height(4.dp))
        BasicText(
            "«El montaje en Jardín Botánico estuvo listo a tiempo y Rosa IA nos ayudó a calcular exactamente las 15 mesas.»",
            style = typo.body,
        )
        if (spec.code == "A34") {
            Spacer(Modifier.height(8.dp))
            RfField(
                label = "Anticipo obligatorio (%)",
                value = depositVal,
                placeholder = "30",
                onValueChange = { viewModel?.setDepositPercentInput(it) },
            )
            Spacer(Modifier.height(6.dp))
            RfField(
                label = "Tarifa base de transporte (RD$)",
                value = deliveryVal,
                placeholder = "450",
                onValueChange = { viewModel?.setDeliveryBaseInput(it) },
            )
        }
    }
    RfButton(
        label = spec.primaryCta,
        onClick = {
            if (spec.code == "A34") {
                viewModel?.saveBusinessConfig()
            }
            onPrimaryAction()
        },
    )
}

@Composable
private fun RenderWhatsAppBotScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val whatsappEnabled = viewModel?.whatsappAlertsEnabled?.collectAsState()?.value ?: true
    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = when (spec.code) {
                        "s73" -> "Conversaciones activas en WhatsApp"
                        "s74" -> "Embudo de conversión WhatsApp → Reserva"
                        else -> "Bot de WhatsApp + Rosa IA"
                    },
                    style = typo.cardTitle,
                )
                BasicText("Línea +1 (809) 555-0199 · Responde precios por día y disponibilidad", style = typo.secondary)
            }
            RfSwitch(
                checked = whatsappEnabled,
                onCheckedChange = { viewModel?.toggleWhatsappAlerts() },
            )
        }
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = when (spec.code) {
                "s73" -> "Laura Méndez: «¿Tienen 60 sillas Tiffany para el dom 25 oct?» → Bot: «¡Sí! 160 libres (RD$ 4/día).»"
                "s74" -> "42 consultas atendidas en oct · 14 cotizaciones generadas · 4 eventos confirmados con anticipo."
                else -> "Plantilla activa: «¡Hola! Silla Tiffany RD$ 4/día, Mesa redonda RD$ 25/día, Arco RD$ 95/día.»"
            },
            style = typo.body,
        )
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}
