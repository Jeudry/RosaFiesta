package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.AdminTabBar
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.designsystem.PersonalTabBar
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfIconTile
import com.jeudry.rosafiesta.designsystem.RfPhoneFrame
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfThemeVariant

public fun shouldShowBottomBar(code: String): Boolean = code.lowercase() in setOf(
    "a01", "a02", "a09", "a11", "a12", "a32", "a35", "a36", "a37",
    "s01", "s02", "s14", "s15", "s16", "s17", "s18", "s20", "s21", "s23", "s24", "s26",
    "s37", "s53", "s54", "s80", "s82", "s86", "s87", "s88", "s89", "s90", "s91", "s92",
)

@Composable
public fun RenderAppBottomBar(
    spec: ScreenSpec,
    onSelectScreenCode: (String) -> Unit,
    onTogglePersonalMoreMenu: (() -> Unit)? = null,
    isMoreMenuOpen: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    if (isAdmin) {
        AdminTabBar(
            activeTab = ScreenCatalog.activeAdminTab(spec.code),
            modifier = modifier,
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
        val palette = LocalRfPalette.current
        val isDarkBar = if (palette.isDark) true else spec.variant == RfThemeVariant.PERSONAL_DARK
        val effectiveTab = if (isMoreMenuOpen) PersonalTab.MAS else ScreenCatalog.activePersonalTab(spec.code)
        PersonalTabBar(
            activeTab = effectiveTab,
            guestMode = guestMode,
            dark = isDarkBar,
            modifier = modifier,
            onSelectTab = { tab ->
                if (tab == PersonalTab.MAS) {
                    if (onTogglePersonalMoreMenu != null) {
                        onTogglePersonalMoreMenu()
                    } else {
                        onSelectScreenCode("s24")
                    }
                } else {
                    val targetCode = resolvePersonalTabTarget(tab = tab, guestMode = guestMode)
                    onSelectScreenCode(targetCode)
                }
            },
        )
    }
}

private fun resolvePersonalTabTarget(tab: PersonalTab, guestMode: Boolean): String = when (tab) {
    PersonalTab.INICIO -> if (guestMode) "s01" else "s14"
    PersonalTab.CATALOGO -> if (guestMode) "s01" else "s18"
    PersonalTab.ROSA -> if (guestMode) "s75" else "s26"
    PersonalTab.EVENTOS -> if (guestMode) "s75" else "s37"
    PersonalTab.FAVORITOS -> if (guestMode) "s02" else "s24"
    PersonalTab.MAS -> "s24"
    PersonalTab.PAQUETES -> if (guestMode) "s75" else "s21"
    PersonalTab.ENTRAR -> "s07"
}

private data class PersonalMoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: RfIconKind,
    val targetCode: String,
)

@Composable
internal fun RenderPersonalMoreOptionsPanel(
    currentScreenCode: String,
    onDismiss: () -> Unit,
    onSelectOption: (String) -> Unit,
    bottomClearance: Dp = 12.dp,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val sheetBodyInteractionSource = remember { MutableInteractionSource() }
    val options = remember {
        listOf(
            PersonalMoreMenuItem(
                title = "Mis favoritos",
                subtitle = "Artículos y paquetes guardados para tu evento",
                icon = RfIconKind.HEART,
                targetCode = "s24",
            ),
            PersonalMoreMenuItem(
                title = "Mis pagos y recibos",
                subtitle = "Historial de abonos, facturas y comprobantes",
                icon = RfIconKind.RECEIPT,
                targetCode = "s82",
            ),
            PersonalMoreMenuItem(
                title = "Mis reservas",
                subtitle = "Contratos activos, entregas y estado de piezas",
                icon = RfIconKind.ORDERS,
                targetCode = "s53",
            ),
            PersonalMoreMenuItem(
                title = "Ajustes",
                subtitle = "Tema oscuro, idioma, recordatorios y privacidad",
                icon = RfIconKind.SETTINGS,
                targetCode = "s86",
            ),
            PersonalMoreMenuItem(
                title = "Acerca de Rosa Fiesta",
                subtitle = "Reseñas 5.0 ★, historia y garantías de servicio",
                icon = RfIconKind.INFO,
                targetCode = "s16",
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x780E0B1A))
            .clickable { onDismiss() }
            .testTag("personal-more-scrim"),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(rememberModalSheetSlideModifier())
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = bottomClearance)
                .heightIn(max = 580.dp)
                .shadow(22.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x451A1A2E), spotColor = Color(0x451A1A2E))
                .clip(RoundedCornerShape(26.dp))
                .background(palette.surface)
                .border(1.dp, palette.line, RoundedCornerShape(26.dp))
                .clickable(
                    interactionSource = sheetBodyInteractionSource,
                    indication = null,
                ) {}
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .testTag("personal-more-panel"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(palette.line),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText(
                        text = "Más opciones",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = palette.ink,
                        ),
                    )
                    BasicText(
                        text = "Accesos rápidos a tu cuenta y preferencias",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.5.sp,
                            color = palette.muted,
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = palette.muted, size = 16.dp)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            options.forEach { option ->
                val isCurrentOption = currentScreenCode == option.targetCode
                val cardBg = if (isCurrentOption) palette.chipBg else palette.bg
                val borderColor = if (isCurrentOption) palette.primaryAccent.copy(alpha = 0.45f) else palette.lineSoft
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .clickable { onSelectOption(option.targetCode) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("more-option-${option.targetCode}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = option.icon, tint = Color.White, size = 20.dp)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(
                            text = option.title,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = palette.ink,
                            ),
                        )
                        BasicText(
                            text = option.subtitle,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                color = palette.muted,
                            ),
                        )
                    }
                    RfIcon(
                        kind = RfIconKind.CHEVRON_RIGHT,
                        tint = if (isCurrentOption) palette.primaryAccent else palette.muted,
                        size = 16.dp,
                    )
                }
            }
        }
    }
}

/**
 * Renderiza cualquier pantalla de [ScreenCatalog] dentro de su marco oficial (390×844 móvil o 1440×900 web).
 */
@Composable
public fun RenderScreenSpec(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel? = null,
    onPrimaryAction: () -> Unit = {},
    onSelectScreenCode: (String) -> Unit = {},
    onSwitchAppMode: () -> Unit = { onSelectScreenCode("s14") },
    isFrameHarness: Boolean = true,
    includeBottomBar: Boolean = true,
    underlyingScreenCode: String? = null,
    modifier: Modifier = Modifier,
) {
    if (spec.isDesktopWeb) {
        RenderAdminWebDesktopA39(
            spec = spec,
            viewModel = viewModel,
            onPrimaryAction = onPrimaryAction,
            onSelectScreenCode = onSelectScreenCode,
            onSwitchAppMode = onSwitchAppMode,
            fillAvailable = !isFrameHarness,
            modifier = modifier,
        )
        return
    }

    LaunchedEffect(spec.code) {
        kotlinx.coroutines.delay(280)
        viewModel?.refreshSharedStateFromDisk()
    }

    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    val isDarkThemeEnabled = viewModel?.isDarkTheme?.collectAsState()?.value ?: false
    val usePersonalDark = if (isAdmin) false else isDarkThemeEnabled
    val resolvedVariant = if (usePersonalDark) RfThemeVariant.PERSONAL_DARK else spec.variant
    val activeRoleValue = viewModel?.activeRole?.collectAsState()?.value
    val staffMembersValue = viewModel?.staffMembers?.collectAsState()?.value
    val isRestrictedForActiveStaff = remember(spec.code, activeRoleValue, staffMembersValue) {
        viewModel?.isScreenRestrictedForActiveStaff(spec.code) == true
    }
    val isBottomBarScreen = shouldShowBottomBar(spec.code)
    val showBottomBar = if (includeBottomBar) isBottomBarScreen else false
    var showPersonalMoreMenu by remember(spec.code) { mutableStateOf(false) }

    RfPhoneFrame(
        variant = resolvedVariant,
        screenCode = spec.code,
        screenTitle = spec.title,
        showMockStatusBar = false,
        wrapInPhoneBorder = false,
        fillAvailable = !isFrameHarness,
        bottomBar = null,
        modifier = modifier.testTag("screen-${spec.code}"),
    ) {
        if (isRestrictedForActiveStaff) {
            RenderYuliPaymentRestrictedScreen(
                spec = spec,
                onSelectScreenCode = onSelectScreenCode,
            )
            return@RfPhoneFrame
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (spec.code) {
                "p00" -> RenderPersonalSplashP00(
                    viewModel = viewModel,
                    onPrimaryAction = onPrimaryAction,
                    onSelectScreenCode = onSelectScreenCode,
                )

                "A00" -> RenderAdminSplashA00(
                    viewModel = viewModel,
                    onPrimaryAction = onPrimaryAction,
                    onSelectScreenCode = onSelectScreenCode,
                )

                "s01" -> RenderGuestCatalogS01(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s02" -> RenderGuestFavoritesS02(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s03", "s04", "s05" -> RenderOnboardingScreen(spec = spec, viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s06" -> RenderBienvenidaScreen(spec = spec, viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s07" -> RenderLoginScreenS07(
                    viewModel = viewModel,
                    onPrimaryAction = onPrimaryAction,
                    onSelectScreenCode = onSelectScreenCode,
                    onSwitchAppMode = onSwitchAppMode,
                )

                "s08" -> RenderRegisterScreenS08(viewModel = viewModel, onPrimaryAction = onPrimaryAction, onSelectScreenCode = onSelectScreenCode)

                "s09", "s10", "s11", "s12", "s13" -> RenderRecoveryAndConfirmationScreens(spec = spec, viewModel = viewModel, onPrimaryAction = onPrimaryAction, onSelectScreenCode = onSelectScreenCode)

                "s14", "s15" -> RenderPersonalHomeScreen(spec = spec, viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s18" -> RenderPersonalCatalogS18(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s19" -> RenderProductDetailS19(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s20" -> RenderCategoriesScreenS20(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s21" -> RenderPackagesScreenS21(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s22" -> RenderPackageDetailScreenS22(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s23" -> RenderShopScreenS23(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s24" -> RenderClientFavoritesScreenS24(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s25" -> RenderMyEventDraftS25(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s26" -> RenderRosaEventTypeS26(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s27" -> RenderRosaTutorialS27(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s28" -> RenderRosaHelpS28(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s29" -> RenderRosaSuggestionsS29(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s30" -> RenderRosaCategoriesS30(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s31" -> RenderRosaDetailsS31(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s32" -> RenderRosaSummaryS32(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s33" -> RenderRosaSubmittedS33(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s34" -> RenderRosaChatS34(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s35" -> RenderRosaVoiceS35(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s36" -> RenderRosaSketchS36(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s78" -> RenderRosaSketchConfirmS78(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A08" -> RenderAdminRosaA08(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s37" -> RenderMyEventsScreenS37(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s38" -> RenderNewEventScreenS38(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s39" -> RenderCalendarScreenS39(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s40" -> RenderEventDetailScreenS40(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s43" -> RenderExecutionModeScreenS43(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s44" -> RenderTimelineScreenS44(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s45" -> RenderTasksScreenS45(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s46" -> RenderChecklistScreenS46(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s47" -> RenderGuestsScreenS47(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s48" -> RenderQuoteScreenS48(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s49" -> RenderQuoteHistoryScreenS49(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s50" -> RenderPaymentCheckoutScreenS50(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s51" -> RenderPaymentSuccessScreenS51(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s52" -> RenderPostEventReviewScreenS52(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s53" -> RenderMyReservationsScreenS53(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s54" -> RenderUserProfileScreenS54(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s55" -> RenderSuppliersScreenS55(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s56" -> RenderNewSupplierScreenS56(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s57" -> RenderSpendingAnalyticsScreenS57(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s58" -> RenderFinancialDashboardScreenS58(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s83" -> RenderOfficialReceiptScreenS83(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A01" -> RenderAdminHomeA01(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A02" -> RenderAdminRequestsA02(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A03" -> RenderAdminQuoteBuilderA03(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A09" -> RenderAdminInventoryA09(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A11" -> RenderAdminEventsA11(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A12" -> RenderAdminMoreA12(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode, onSwitchAppMode = onSwitchAppMode)

                "A20" -> RenderAdminPaymentsA20(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A21" -> RenderAdminRecordPaymentA21(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A10" -> RenderAdminNewArticleA10(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A13" -> RenderAdminEventDetailA13(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A16" -> RenderAdminExecutionA16(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A17" -> RenderAdminTasksA17(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A18" -> RenderAdminSketchA18(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A19" -> RenderAdminCloseoutA19(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A23" -> RenderAdminArticleDetailA23(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A24" -> RenderAdminPackagesA24(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A25" -> RenderAdminPackageEditA25(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A26" -> RenderAdminClientsA26(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A27" -> RenderAdminClientProfileA27(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A28" -> RenderAdminTeamA28(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A04" -> RenderAdminCreateEventA04(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A05" -> RenderAdminManualEventA05(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A06" -> RenderAdminEventArticlesA06(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A07" -> RenderAdminEventSummaryA07(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A14" -> RenderAdminChatsA14(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A15" -> RenderAdminChatConversationA15(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A22" -> RenderAdminQuotesA22(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A29" -> RenderAdminNotificationsA29(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A30" -> RenderAdminEntryChoiceA30(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode, onSwitchAppMode = onSwitchAppMode)

                "A31" -> RenderAdminSettingsA31(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A32" -> RenderAdminCancellationsA32(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A33" -> RenderAdminRefundA33(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A34" -> RenderAdminReplyReviewA34(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A35" -> RenderAdminEmptyStateA35(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A36" -> RenderAdminLoadingStateA36(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A37" -> RenderAdminErrorStateA37(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "A38" -> RenderAdminSuccessStateA38(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s16" -> RenderAboutScreenS16(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s17" -> RenderCompanyReviewsS17(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s41" -> RenderEventChatTabS41(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s42" -> RenderEventChatS42(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s59" -> RenderFinanceDashboardS59(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s60" -> RenderInsurancePanelS60(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s61" -> RenderAuditPanelS61(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s62" -> RenderClientsAnalyticsS62(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s63" -> RenderInventoryCalendarS63(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s64" -> RenderLeadsPipelineS64(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s65" -> RenderLeadsFollowupS65(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s66" -> RenderLeadsStatsS66(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s67" -> RenderReportsIncomeS67(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s68" -> RenderReportsEventsS68(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s69" -> RenderReportsClientsS69(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s70" -> RenderReportsInventoryS70(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s71" -> RenderReviewModerationS71(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s72" -> RenderBotQuestionsS72(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s73" -> RenderBotKeywordsS73(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s74" -> RenderBotConversationsS74(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s75" -> RenderGuestAuthModalS75(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s76" -> RenderPlanningModalS76(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s77" -> RenderEventReviewsModalS77(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s79" -> RenderPersonalNotificationsS79(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s80" -> RenderPersonalSearchS80(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s81" -> RenderCatalogFiltersModalS81(
                    viewModel = viewModel,
                    onSelectScreenCode = onSelectScreenCode,
                    underlyingScreenCode = underlyingScreenCode,
                )

                "s82" -> RenderPersonalPaymentsS82(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s84" -> RenderChangeDateModalS84(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s85" -> RenderCancelEventModalS85(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s86" -> RenderAccountSettingsS86(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s87" -> RenderTermsS87(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s88" -> RenderDeleteAccountS88(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s89" -> RenderPersonalEmptyStateS89(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s90" -> RenderPersonalLoadingStateS90(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s91" -> RenderPersonalOfflineStateS91(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

                "s92" -> RenderPersonalSuccessStateS92(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)
            }
            if (showBottomBar) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    RenderAppBottomBar(
                        spec = spec,
                        onSelectScreenCode = { targetCode ->
                            showPersonalMoreMenu = false
                            onSelectScreenCode(targetCode)
                        },
                        onTogglePersonalMoreMenu = {
                            showPersonalMoreMenu = !showPersonalMoreMenu
                        },
                        isMoreMenuOpen = showPersonalMoreMenu,
                    )
                }
            }
            if (showPersonalMoreMenu) {
                RenderPersonalMoreOptionsPanel(
                    currentScreenCode = spec.code,
                    onDismiss = { showPersonalMoreMenu = false },
                    onSelectOption = { targetCode ->
                        showPersonalMoreMenu = false
                        onSelectScreenCode(targetCode)
                    },
                )
            }
        }
    }
}

@Composable
internal fun RenderYuliPaymentRestrictedScreen(
    spec: ScreenSpec,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("staff-payment-restricted-${spec.code}"),
    ) {
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
}
