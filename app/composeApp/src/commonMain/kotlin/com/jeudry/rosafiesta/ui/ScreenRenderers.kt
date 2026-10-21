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
import com.jeudry.rosafiesta.domain.PhotoAsset

private fun PhotoAsset.toPhotoKey(): RfPhotoKey = when (this) {
    PhotoAsset.SILLA_TIFFANY -> RfPhotoKey.SILLA_TIFFANY
    PhotoAsset.MESA_PLEGABLE -> RfPhotoKey.MESA_PLEGABLE
    PhotoAsset.MESA_REDONDA -> RfPhotoKey.MESA_REDONDA
    PhotoAsset.CENTRO_FLORAL -> RfPhotoKey.CENTRO_FLORAL
    PhotoAsset.ARCO_ROSA -> RfPhotoKey.ARCO_ROSA
    PhotoAsset.MESA_HELADOS -> RfPhotoKey.MESA_HELADOS
    PhotoAsset.BODA -> RfPhotoKey.BODA
    PhotoAsset.QUINCEANERA -> RfPhotoKey.QUINCEANERA
    PhotoAsset.CUMPLE_INFANTIL -> RfPhotoKey.CUMPLE_INFANTIL
    PhotoAsset.BABY_SHOWER -> RfPhotoKey.BABY_SHOWER
    PhotoAsset.GRADUACION -> RfPhotoKey.GRADUACION
    PhotoAsset.CORPORATIVO -> RfPhotoKey.CORPORATIVO
}

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
    modifier: Modifier = Modifier,
) {
    if (spec.isDesktopWeb) {
        RenderAdminWebDesktopA39(
            spec = spec,
            onPrimaryAction = onPrimaryAction,
            onSelectScreenCode = onSelectScreenCode,
            modifier = modifier,
        )
        return
    }

    val offlineQueue = viewModel?.offlineQueue?.collectAsState()?.value ?: CanonicalSampleData.offlineQueue
    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    val showBottomBar = when {
        spec.code in setOf("p00", "A00", "s03", "s04", "s05", "s06", "s07", "s08", "s09", "s10", "s11", "s12", "s13", "A30") -> false
        else -> true
    }

    RfPhoneFrame(
        variant = spec.variant,
        screenCode = spec.code,
        screenTitle = spec.title,
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
                ScreenHeaderBlock(spec, onSelectScreenCode)
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

            ScreenHeaderBlock(spec, onSelectScreenCode)

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

        "A05", "A06" -> {
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
                    "El perfil de Yuli tiene acceso exclusivo a decoración y tareas de campo. María puede habilitar el permiso en A28.",
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
            selected = !isStep1,
            onClick = { onSelectScreenCode("A06") },
        )
        RfChip(
            label = "3 · Resumen",
            selected = false,
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
                BasicText(mateo.title, style = typo.cardTitle, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                RfTag(text = "Stock libre", tone = RfTagTone.LIMA)
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
            if (isStep1 && viewModel != null) {
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

@Composable
private fun RenderSplashAndOnboardingContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    RfHeroCard(useAdminGradient = isAdmin) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RfLogo(size = RfLogoSize.LARGE)
            RosaAvatar(size = RosaAvatarSize.LARGE, forceAdminPalette = isAdmin, glow = true)
        }
        Spacer(Modifier.height(10.dp))
        BasicText(
            text = if (isAdmin) "RosaFiesta Admin · Gestión inteligente de eventos" else "RosaFiesta · Tu celebración soñada pieza por pieza",
            style = typo.sheetOrEmptyTitle.copy(color = Color.White),
        )
        Spacer(Modifier.height(4.dp))
        BasicText(
            text = spec.subtitle,
            style = typo.body.copy(color = Color.White.copy(alpha = 0.92f)),
        )
    }

    RfPhoto(
        photoKey = spec.heroPhoto,
        contentDescription = spec.title,
        bottomScrim = true,
        modifier = Modifier.fillMaxWidth().height(150.dp),
    )

    RfCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RfIllo(kind = RfIlloKind.GLOBOS, size = 48.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText("Precios oficiales por día", style = typo.cardTitle)
                BasicText("Silla Tiffany RD$ 4 · Mesa redonda RD$ 25 · Arco RD$ 95 · Mesa dulce RD$ 150", style = typo.secondary)
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAuthFormByCode(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val loginEmail = viewModel?.loginEmail?.collectAsState()?.value ?: "camila@rosafiesta.do"
    val loginPassword = viewModel?.loginPassword?.collectAsState()?.value ?: "••••••••••••"
    val regName = viewModel?.registerName?.collectAsState()?.value ?: "Camila Rodríguez"
    val regEmail = viewModel?.registerEmail?.collectAsState()?.value ?: "camila@rosafiesta.do"
    val regPhone = viewModel?.registerPhone?.collectAsState()?.value ?: "+1 (809) 555-0142"
    val formError = viewModel?.formError?.collectAsState()?.value

    when (spec.code) {
        "s06" -> {
            RfPhoto(
                photoKey = RfPhotoKey.BODA,
                contentDescription = "Bienvenida RosaFiesta",
                bottomScrim = true,
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            RfButton(label = "Iniciar sesión", onClick = { onSelectScreenCode("s07") })
            RfButton(label = "Crear cuenta", style = RfButtonStyle.SECONDARY, onClick = { onSelectScreenCode("s08") })
            RfButton(label = "Explorar como invitado", style = RfButtonStyle.SECONDARY, onClick = { onSelectScreenCode("s01") })
        }

        "s07" -> {
            RfCard {
                RfField(
                    label = "Correo electrónico",
                    value = loginEmail,
                    placeholder = "correo@rosafiesta.do",
                    onValueChange = if (viewModel != null) ({ viewModel.setLoginEmail(it) }) else null,
                )
                Spacer(Modifier.height(10.dp))
                RfField(
                    label = "Contraseña",
                    value = if (viewModel == null) "••••••••••••" else loginPassword,
                    placeholder = "Ingresa tu contraseña",
                    helperOrError = formError ?: "¿Olvidaste tu contraseña? Recuperar en s09",
                    isError = formError != null,
                    onValueChange = if (viewModel != null) ({ viewModel.setLoginPassword(it) }) else null,
                )
                Spacer(Modifier.height(12.dp))
                RfButton(
                    label = spec.primaryCta,
                    onClick = {
                        val ok = viewModel?.submitLogin() ?: true
                        if (ok) onPrimaryAction()
                    },
                )
            }
        }

        "s09", "s10" -> {
            RfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RfIllo(kind = RfIlloKind.CANDADO, size = 52.dp)
                    Spacer(Modifier.width(12.dp))
                    BasicText(
                        text = if (spec.code == "s09") "Enviaremos un enlace seguro a $loginEmail" else "Ingresa tu nueva contraseña segura",
                        style = typo.body,
                    )
                }
                Spacer(Modifier.height(10.dp))
                RfField(
                    label = if (spec.code == "s09") "Correo registrado" else "Nueva contraseña",
                    value = if (spec.code == "s09") loginEmail else loginPassword,
                    onValueChange = if (viewModel != null) {
                        { if (spec.code == "s09") viewModel.setLoginEmail(it) else viewModel.setLoginPassword(it) }
                    } else {
                        null
                    },
                )
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        }

        "s11", "s12", "s13" -> {
            RfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RfIllo(
                        kind = if (spec.code == "s11") RfIlloKind.CORREO else RfIlloKind.EXITO,
                        size = 56.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(spec.title, style = typo.cardTitle)
                        BasicText(spec.subtitle, style = typo.secondary)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        }

        else -> {
            RfCard {
                RfField(
                    label = "Nombre completo",
                    value = regName,
                    onValueChange = if (viewModel != null) ({ viewModel.setRegisterName(it) }) else null,
                )
                Spacer(Modifier.height(8.dp))
                RfField(
                    label = "Correo electrónico",
                    value = regEmail,
                    onValueChange = if (viewModel != null) ({ viewModel.setRegisterEmail(it) }) else null,
                )
                Spacer(Modifier.height(8.dp))
                RfField(
                    label = "Teléfono / WhatsApp",
                    value = regPhone,
                    onValueChange = if (viewModel != null) ({ viewModel.setRegisterPhone(it) }) else null,
                )
                Spacer(Modifier.height(12.dp))
                RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            }
        }
    }
}

@Composable
private fun RenderPersonalHomeContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val articles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val boda = events.first()

    when (spec.code) {
        "s16" -> {
            val evtType = viewModel?.eventTypeInput?.collectAsState()?.value ?: "Boda Real"
            val evtDate = viewModel?.eventDateInput?.collectAsState()?.value ?: "Vie 23 oct"
            val evtVenue = viewModel?.eventVenueInput?.collectAsState()?.value ?: "Jardín Botánico"
            val evtGuests = viewModel?.eventGuestsInput?.collectAsState()?.value ?: "120"
            RfCard {
                BasicText("Configurador rápido de celebración", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Tipo de celebración",
                    value = evtType,
                    onValueChange = { viewModel?.setEventTypeInput(it) },
                )
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Fecha y lugar",
                    value = "$evtDate · $evtVenue",
                    onValueChange = { viewModel?.setEventDateInput(it) },
                )
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Invitados estimados",
                    value = evtGuests,
                    onValueChange = { viewModel?.setEventGuestsInput(it) },
                )
            }
            CanonicalSampleData.templates.take(3).forEach { tpl ->
                RfCard(onClick = { viewModel?.setEventTypeInput(tpl.name) }) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(tpl.name, style = typo.bodyStrong)
                            BasicText("${tpl.subtitle} · ${tpl.defaultGuests} inv.", style = typo.secondary)
                        }
                        RfTag(text = "Desde ${RosaFiestaEngine.formatDop(tpl.startingPriceDop)}", tone = RfTagTone.ROSA)
                    }
                }
            }
            RfButton(
                label = spec.primaryCta,
                onClick = {
                    viewModel?.submitQuickClientEvent()
                    onPrimaryAction()
                },
            )
            return
        }

        "s17" -> {
            val query = viewModel?.searchQuery?.collectAsState()?.value ?: ""
            RfField(
                label = "Buscar en RosaFiesta",
                value = query,
                placeholder = "Silla Tiffany, mesa redonda, arco...",
                onValueChange = { viewModel?.setSearchQuery(it) },
            )
            val filtered = articles.filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.code.contains(query, ignoreCase = true)
            }
            filtered.take(4).forEach { art ->
                RfCard(onClick = { onSelectScreenCode("s19") }) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("${art.code} · ${art.name}", style = typo.bodyStrong)
                            BasicText("${art.category} · ${art.availableOct23} disponibles", style = typo.secondary)
                        }
                        RfGradientPriceText(art.formattedDailyRate)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }
    }

    RfHeroCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RfTag(text = "Próximo evento · ${boda.dateLabel}", tone = RfTagTone.SOL)
                Spacer(Modifier.height(6.dp))
                BasicText(
                    text = boda.title,
                    style = typo.backScreenTitle.copy(color = Color.White),
                )
                BasicText(
                    text = "${boda.venue} · ${boda.guests} invitados · ${boda.articleCount} artículos",
                    style = typo.secondary.copy(color = Color.White.copy(alpha = 0.9f)),
                )
            }
            RosaAvatar(size = RosaAvatarSize.MEDIUM, glow = true)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Anticipo 30 %: RD$ 1,350",
                style = typo.secondary.copy(color = Color.White, fontSize = 12.5.sp),
                maxLines = 1,
            )
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = "Saldo RD$ 3,150 (${boda.balanceDueDateLabel})",
                style = typo.bodyStrong.copy(color = RfColors.Sol, fontSize = 12.5.sp),
                maxLines = 1,
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RfChip("Bodas", selected = true, onClick = { onSelectScreenCode("s21") })
        RfChip("Quinceañeras", selected = false, onClick = { onSelectScreenCode("s21") })
        RfChip("Cómo planificar", selected = false, onClick = { onSelectScreenCode("s76") })
    }

    articles.take(3).forEach { article ->
        RfCard(onClick = { onSelectScreenCode("s19") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfPhoto(
                    photoKey = article.photo.toPhotoKey(),
                    contentDescription = article.name,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(article.name, style = typo.cardTitle)
                    BasicText(article.category, style = typo.secondary)
                    Spacer(Modifier.height(4.dp))
                    RfGradientPriceText(text = article.formattedDailyRate)
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalCatalogContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val articles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val favs = viewModel?.favorites?.collectAsState()?.value ?: CanonicalSampleData.initialFavorites
    val draftQty = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()

    when (spec.code) {
        "s02", "s19" -> {
            val art = articles.first()
            val qty = draftQty[art.id] ?: 120
            val isFav = art.id in favs
            RfPhoto(
                photoKey = art.photo.toPhotoKey(),
                contentDescription = art.name,
                bottomScrim = true,
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            RfCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("${art.code} · ${art.name}", style = typo.cardTitle)
                        BasicText(art.description, style = typo.secondary)
                    }
                    RfGradientPriceText(art.formattedDailyRate, style = typo.backScreenTitle)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    art.variants.forEachIndexed { idx, v ->
                        RfChip(v, selected = idx == 0, onClick = {})
                    }
                    RfChip(
                        label = if (isFav) "★ Favorito" else "☆ Guardar",
                        selected = isFav,
                        onClick = { viewModel?.toggleFavorite(art.id) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Cantidad para tu evento: $qty u.", style = typo.bodyStrong)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfChip("-10", selected = false, onClick = { viewModel?.adjustDraftQuantity(art.id, -10) })
                        RfChip("+10", selected = true, onClick = { viewModel?.adjustDraftQuantity(art.id, 10) })
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s20" -> {
            val art = articles.first()
            RfCard {
                BasicText("Calendario de stock · ${art.name}", style = typo.cardTitle)
                BasicText("Stock total en bodega: ${art.totalStock} unidades", style = typo.secondary)
                Spacer(Modifier.height(8.dp))
                listOf(
                    Triple("Vie 23 oct 2026", "${art.availableOct23} libres de ${art.totalStock}", RfTagTone.LIMA),
                    Triple("Sáb 24 oct 2026", "140 libres de ${art.totalStock}", RfTagTone.LIMA),
                    Triple("Lun 2 nov 2026", "100 libres de ${art.totalStock}", RfTagTone.SOL),
                ).forEach { (date, stockLabel, tone) ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        BasicText(date, style = typo.bodyStrong)
                        RfTag(text = stockLabel, tone = tone)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s21", "s22" -> {
            val packages = CanonicalSampleData.packages
            val displayed = if (spec.code == "s22") packages.take(1) else packages
            displayed.forEach { pkg ->
                RfCard(onClick = { onSelectScreenCode("s22") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RfPhoto(
                            photoKey = pkg.photo.toPhotoKey(),
                            contentDescription = pkg.name,
                            modifier = Modifier.size(68.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(pkg.name, style = typo.cardTitle)
                            BasicText("${pkg.subtitle} · ${pkg.suggestedGuests} inv.", style = typo.secondary)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                RfGradientPriceText(RosaFiestaEngine.formatDop(pkg.priceDop))
                                RfTag(text = "Ahorras ${RosaFiestaEngine.formatDop(pkg.savingsDop)}", tone = RfTagTone.LIMA)
                            }
                        }
                    }
                    if (spec.code == "s22") {
                        Spacer(Modifier.height(8.dp))
                        pkg.includedItems.forEach { line ->
                            BasicText("• $line", style = typo.body)
                        }
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s24", "s25" -> {
            val favArticles = articles.filter { it.id in favs }.ifEmpty { articles.take(3) }
            RfCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    BasicText(
                        text = if (spec.code == "s25") "Tablero: Boda Real Rosa & Dorado" else "Mis piezas favoritas (${favArticles.size})",
                        style = typo.cardTitle,
                    )
                    RfTag(text = "Vie 23 oct", tone = RfTagTone.ROSA)
                }
                BasicText("Toca cualquier pieza para alternar guardado o cotizar con Rosa IA.", style = typo.secondary)
            }
            favArticles.forEach { art ->
                RfCard(onClick = { viewModel?.toggleFavorite(art.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RfPhoto(photoKey = art.photo.toPhotoKey(), contentDescription = art.name, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(art.name, style = typo.bodyStrong)
                            BasicText(art.formattedDailyRate, style = typo.secondary)
                        }
                        RfTag(text = "★ Guardado", tone = RfTagTone.ROSA)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s80" -> {
            RfCard {
                BasicText("Comparativa de sillas y mesas", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                articles.take(3).forEach { art ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(art.name, style = typo.bodyStrong)
                            BasicText("Stock 23 oct: ${art.availableOct23}/${art.totalStock} · ${art.variants.joinToString()}", style = typo.secondary)
                        }
                        RfGradientPriceText(art.formattedDailyRate)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RfChip("Todos (${articles.size})", selected = spec.code !in setOf("s21", "s22"), onClick = { onSelectScreenCode("s18") })
        RfChip("Paquetes", selected = spec.code in setOf("s21", "s22"), onClick = { onSelectScreenCode("s21") })
        RfChip("Filtros", selected = false, onClick = { onSelectScreenCode("s81") })
    }

    articles.take(4).forEach { article ->
        RfCard(onClick = { onSelectScreenCode("s19") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfPhoto(
                    photoKey = article.photo.toPhotoKey(),
                    contentDescription = article.name,
                    modifier = Modifier.size(68.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(article.name, style = typo.cardTitle)
                    BasicText(
                        text = "Disponibles vie 23 oct: ${article.availableOct23} de ${article.totalStock}",
                        style = typo.secondary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RfGradientPriceText(text = article.formattedDailyRate)
                        RfTag(text = "Disponible", tone = RfTagTone.LIMA)
                    }
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalQuoteAndPaymentContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val payments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val boda = events.first()

    when (spec.code) {
        "s30" -> {
            RfCard {
                RfTag(text = "Contrato digital #CTR-2026-1023", tone = RfTagTone.VIOLETA)
                Spacer(Modifier.height(6.dp))
                BasicText("Cláusulas de alquiler y montaje · ${boda.title}", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                BasicText("1. Reserva confirmada con el 30 % de anticipo (${RosaFiestaEngine.formatDop(boda.paidDepositDop)}).", style = typo.body)
                BasicText("2. Liquidación del 70 % restante (${RosaFiestaEngine.formatDop(boda.balanceDop)}) 24 h antes del evento.", style = typo.body)
                BasicText("3. Primer cambio de fecha gratis con al menos 48 h de antelación.", style = typo.body)
                BasicText("4. Reembolso por cancelación: 90 % (>7 días), 50 % (3–7 días), 0 % (<3 días).", style = typo.body)
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s31" -> {
            val sigName = viewModel?.profileName?.collectAsState()?.value ?: "Camila Rodríguez"
            RfCard {
                BasicText("Firma electrónica del cliente", style = typo.cardTitle)
                BasicText("Acepto las condiciones de entrega, montaje y depósito de garantía.", style = typo.secondary)
                Spacer(Modifier.height(8.dp))
                RfField(
                    label = "Firma con tu nombre completo",
                    value = sigName,
                    onValueChange = { viewModel?.setProfileName(it) },
                )
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s32", "s38" -> {
            val amountVal = viewModel?.paymentAmountInput?.collectAsState()?.value ?: boda.balanceDop.toString()
            val methodVal = viewModel?.paymentMethodInput?.collectAsState()?.value ?: "Transferencia Banreservas"
            val refVal = viewModel?.paymentRefInput?.collectAsState()?.value ?: "BR-2026-9941"
            val err = viewModel?.formError?.collectAsState()?.value
            RfCard {
                BasicText(
                    text = if (spec.code == "s32") "Pago de anticipo 30 % (RD$ 1,350)" else "Liquidar saldo pendiente 70 % (${RosaFiestaEngine.formatDop(boda.balanceDop)})",
                    style = typo.cardTitle,
                )
                Spacer(Modifier.height(8.dp))
                RfField(
                    label = "Monto a pagar (RD$)",
                    value = amountVal,
                    onValueChange = { viewModel?.setPaymentAmountInput(it) },
                )
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Método bancario o tarjeta",
                    value = methodVal,
                    onValueChange = { viewModel?.setPaymentMethodInput(it) },
                )
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Número de referencia / comprobante",
                    value = refVal,
                    helperOrError = err,
                    isError = err != null,
                    onValueChange = { viewModel?.setPaymentRefInput(it) },
                )
            }
            RfButton(
                label = spec.primaryCta,
                onClick = {
                    viewModel?.submitPayment(boda.id)
                    onPrimaryAction()
                },
            )
            return
        }

        "s39", "s82" -> {
            payments.forEach { pay ->
                RfCard(onClick = { onSelectScreenCode("s83") }) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("${pay.receiptNumber} · ${pay.kindLabel}", style = typo.bodyStrong)
                            BasicText("${pay.eventTitle} · ${pay.methodLabel}", style = typo.secondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            RfGradientPriceText(RosaFiestaEngine.formatDop(pay.amountDop))
                            RfTag(text = if (pay.verified) "Verificado" else "En revisión", tone = RfTagTone.LIMA)
                        }
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }
    }

    if (spec.code in setOf("s33", "s51")) {
        RfCard(padding = 12.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                RfIllo(kind = RfIlloKind.EXITO, size = 48.dp)
                BasicText(spec.title, style = typo.sheetOrEmptyTitle)
                BasicText(
                    text = if (spec.code == "s51") "Recibo #REC-2026-0184 · Anticipo 30 % confirmado" else "Folio #COT-2026-1023 · Recibida por María",
                    style = typo.secondary,
                )
                RfGradientPriceText(
                    text = if (spec.code == "s51") "RD$ 1,350" else "RD$ 4,500",
                    style = typo.bigAmount,
                )
            }
        }
    }

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(boda.title, style = typo.cardTitle, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            RfTag(text = boda.code, tone = RfTagTone.VIOLETA)
        }
        Spacer(Modifier.height(4.dp))
        BasicText("${boda.dateLabel} · ${boda.venue} · ${boda.guests} invitados", style = typo.secondary)
        Spacer(Modifier.height(6.dp))
        boda.items.forEach { line ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 1.5.dp),
            ) {
                BasicText("${line.quantity} × ${line.name}", style = typo.body)
                BasicText(RosaFiestaEngine.formatDop(line.subtotalDop), style = typo.bodyStrong)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Total del evento", style = typo.cardTitle)
            RfGradientPriceText(text = RosaFiestaEngine.formatDop(boda.totalDop), style = typo.backScreenTitle)
        }
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Anticipo 30 % (transferencia, 18 oct)",
                style = typo.bodyStrong.copy(fontSize = 13.sp),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            RfTag(text = RosaFiestaEngine.formatDop(boda.paidDepositDop), tone = RfTagTone.LIMA)
        }
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Saldo 70 % (vence ${boda.balanceDueDateLabel})",
                style = typo.secondary.copy(fontSize = 13.sp),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            RfTag(text = RosaFiestaEngine.formatDop(boda.balanceDop), tone = RfTagTone.SOL)
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalEventsContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val boda = events.first()

    when (spec.code) {
        "s41" -> {
            RfCard {
                BasicText("Artículos reservados · ${boda.title}", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                boda.items.forEach { item ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    ) {
                        BasicText("${item.quantity} × ${item.name} (${item.articleCode})", style = typo.body)
                        BasicText(RosaFiestaEngine.formatDop(item.subtotalDop), style = typo.bodyStrong)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s42" -> {
            val stop = CanonicalSampleData.deliveryStops.first()
            RfCard {
                RfTag(text = stop.statusLabel, tone = RfTagTone.LIMA)
                Spacer(Modifier.height(6.dp))
                BasicText("Ruta en vivo hacia ${stop.venue}", style = typo.cardTitle)
                BasicText("Ventana: ${stop.windowLabel} · ${stop.vehicleAndCrew}", style = typo.secondary)
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s43" -> {
            RfCard {
                BasicText("Equipo asignado en campo", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                CanonicalSampleData.staffMembers.forEach { member ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(member.name, style = typo.bodyStrong)
                            BasicText(member.roleLabel, style = typo.secondary)
                        }
                        RfTag(text = "${member.activeEventsCount} eventos", tone = RfTagTone.ROSA)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s48", "s49" -> {
            RfPhoto(
                photoKey = RfPhotoKey.BODA,
                contentDescription = boda.title,
                bottomScrim = true,
                modifier = Modifier.fillMaxWidth().height(150.dp),
            )
            RfCard {
                BasicText(
                    text = if (spec.code == "s48") "Álbum compartido · 24 fotos del montaje" else "Cierre y recogida · Sin incidencias",
                    style = typo.cardTitle,
                )
                BasicText("Jardín Botánico · Verificado por Pedro y Camila Rodríguez", style = typo.secondary)
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "s52", "s53" -> {
            val nextDate = viewModel?.dateChangeInput?.collectAsState()?.value ?: "Vie 30 oct"
            val banner = viewModel?.dateChangeBanner?.collectAsState()?.value ?: "Primer cambio gratis (>48h antes)"
            RfCard {
                RfTag(
                    text = if (spec.code == "s52") banner else "Política de reembolso 90 % / 50 % / 0 %",
                    tone = if (spec.code == "s52") RfTagTone.LIMA else RfTagTone.SOL,
                )
                Spacer(Modifier.height(6.dp))
                if (spec.code == "s52") {
                    RfField(
                        label = "Nueva fecha para ${boda.title}",
                        value = nextDate,
                        onValueChange = { viewModel?.setDateChangeInput(it) },
                    )
                } else {
                    val refund = RosaFiestaEngine.calculateCancellationRefund(boda.paidDepositDop, boda.daysUntilEvent)
                    BasicText("Reembolso aplicable hoy (${refund.refundPercent} %): ${RosaFiestaEngine.formatDop(refund.refundDop)}", style = typo.cardTitle)
                    BasicText("Retención operativa: ${RosaFiestaEngine.formatDop(refund.retainedDop)}", style = typo.secondary)
                }
            }
            RfButton(
                label = spec.primaryCta,
                style = if (spec.code == "s53") RfButtonStyle.DANGER else RfButtonStyle.PRIMARY,
                onClick = {
                    if (spec.code == "s52") viewModel?.submitDateChange(boda.id) else viewModel?.submitCancelEvent(boda.id)
                    onPrimaryAction()
                },
            )
            return
        }
    }

    if (spec.code == "s40") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            RfChip("Ejecución", selected = true, onClick = { onSelectScreenCode("s43") })
            RfChip("Cronograma", selected = false, onClick = { onSelectScreenCode("s44") })
            RfChip("Invitados", selected = false, onClick = { onSelectScreenCode("s47") })
        }
    }

    events.take(2).forEach { event ->
        RfCard(onClick = { onSelectScreenCode("s40") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfPhoto(
                    photoKey = event.photo.toPhotoKey(),
                    contentDescription = event.title,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        BasicText(event.title, style = typo.cardTitle, modifier = Modifier.weight(1f))
                        RfTag(text = event.dateLabel, tone = RfTagTone.LIMA)
                    }
                    BasicText("${event.venue} · ${event.guests} inv. · ${event.articleCount} art.", style = typo.secondary)
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        "Total ${RosaFiestaEngine.formatDop(event.totalDop)} · Saldo ${RosaFiestaEngine.formatDop(event.balanceDop)}",
                        style = typo.bodyStrong,
                    )
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderRosaAssistantContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    isAdmin: Boolean,
    onPrimaryAction: () -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val rosaType = viewModel?.rosaEventType?.collectAsState()?.value ?: "Boda Real Rosa & Dorado"
    val rosaGuests = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 120
    val rosaPalette = viewModel?.rosaPalette?.collectAsState()?.value ?: "Rosa & Dorado"
    val isCustomized = rosaGuests != 120 || rosaType != "Boda Real Rosa & Dorado" || rosaPalette != "Rosa & Dorado"
    val proposal = RosaFiestaEngine.buildRosaProposal(rosaType, rosaGuests)
    val canonicalBoda = CanonicalSampleData.events.first()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RfDimens.HeroCardRadius))
            .background(palette.rosaAssistantBgGradient)
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RosaAvatar(size = RosaAvatarSize.LARGE, forceAdminPalette = isAdmin, glow = true)
                Spacer(Modifier.width(12.dp))
                Column {
                    RfTag(
                        text = if (isAdmin) "Rosa IA Admin · Índigo→Turquesa" else "Rosa IA · Asistente Personal",
                        tone = if (isAdmin) RfTagTone.TEAL else RfTagTone.VIOLETA,
                    )
                    Spacer(Modifier.height(4.dp))
                    BasicText(spec.title, style = typo.cardTitle)
                }
            }
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = when {
                    isAdmin ->
                        "«María, verifiqué almacén para el lun 2 nov: tenemos las 100 sillas Tiffany y 13 mesas redondas libres para Quinceañera Valentina Crystal (RD$ 6,200).»"

                    isCustomized ->
                        "«Camila, para tus $rosaGuests invitados en Jardín Botánico ($rosaPalette) preparé la propuesta completa por ${RosaFiestaEngine.formatDop(proposal.totalDop)}.»"

                    else ->
                        "«Camila, para tus 120 invitados en Jardín Botánico preparé 120 sillas Tiffany doradas, 15 mesas redondas, 8 centros florales y el arco orgánico rosa dentro de tu presupuesto de RD$ 4,500.»"
                },
                style = typo.body,
            )
        }
    }

    if (spec.code in setOf("s26", "s34", "A08", "A17", "s66", "s67")) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            RfChip("80 inv.", selected = rosaGuests == 80, onClick = { viewModel?.updateRosaParameters(guests = 80) })
            RfChip("120 inv.", selected = rosaGuests == 120, onClick = { viewModel?.updateRosaParameters(guests = 120) })
            RfChip("180 inv.", selected = rosaGuests == 180, onClick = { viewModel?.updateRosaParameters(guests = 180) })
        }
    }

    RfCard {
        BasicText("Lista y cotización armada por Rosa IA", style = typo.cardTitle)
        Spacer(Modifier.height(6.dp))
        if (isCustomized) {
            proposal.lines.forEach { line ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    BasicText("${line.quantity} × ${line.name}", style = typo.body)
                    BasicText(RosaFiestaEngine.formatDop(line.subtotalDop), style = typo.bodyStrong)
                }
            }
        } else {
            canonicalBoda.items.forEach { line ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    BasicText("${line.quantity} × ${line.name}", style = typo.body)
                    BasicText(RosaFiestaEngine.formatDop(line.subtotalDop), style = typo.bodyStrong)
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalProfileAndStateContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val nameVal = viewModel?.profileName?.collectAsState()?.value ?: "Camila Rodríguez"
    val emailVal = viewModel?.profileEmail?.collectAsState()?.value ?: "camila@rosafiesta.do"
    val phoneVal = viewModel?.profilePhone?.collectAsState()?.value ?: "+1 (809) 555-0142"
    val pushEnabled = viewModel?.pushNotificationsEnabled?.collectAsState()?.value ?: true

    RfCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RfIconTile(
                icon = RfIconKind.USER,
                backgroundColor = RfColors.RosaTint,
                iconTint = RfColors.RosaTintInk,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText(nameVal, style = typo.cardTitle)
                BasicText("$emailVal · $phoneVal", style = typo.secondary)
                Spacer(Modifier.height(4.dp))
                RfTag(text = "2 eventos activos · Saldo RD$ 7,490", tone = RfTagTone.ROSA)
            }
        }
    }

    when (spec.code) {
        "s86" -> {
            RfCard {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Recordatorios de pago y montaje", style = typo.bodyStrong)
                    RfSwitch(
                        checked = pushEnabled,
                        onCheckedChange = { viewModel?.togglePushNotifications() },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Modo oscuro (ver s15)", style = typo.bodyStrong)
                    RfSwitch(checked = false, onCheckedChange = { onSelectScreenCode("s15") })
                }
            }
        }

        "s87" -> {
            RfCard {
                BasicText("Términos y privacidad RosaFiesta", style = typo.cardTitle)
                Spacer(Modifier.height(4.dp))
                BasicText("• Reserva con 30 % de anticipo y saldo 24 h antes del evento.", style = typo.body)
                BasicText("• Primer cambio de fecha gratis con 48 h de antelación.", style = typo.body)
                BasicText("• Protección de datos personales y bancarios en Santo Domingo.", style = typo.body)
            }
        }

        else -> {
            RfCard(onClick = { onSelectScreenCode("s82") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RfIconTile(icon = RfIconKind.RECEIPT, backgroundColor = RfColors.LimaTint, iconTint = RfColors.LimaTintInk)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Mis pagos y recibos (s82 → s83)", style = typo.bodyStrong)
                        BasicText("Anticipo Boda Real RD$ 1,350 · Quinceañera RD$ 1,860", style = typo.secondary)
                    }
                }
            }
            RfCard(onClick = { onSelectScreenCode("s86") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RfIconTile(icon = RfIconKind.SHIELD, backgroundColor = RfColors.VioletaTint, iconTint = RfColors.VioletaTintInk)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Ajustes, términos (s87) y eliminar cuenta (s88)", style = typo.bodyStrong)
                        BasicText("Política 90 % / 50 % / 0 % y validación de eventos activos", style = typo.secondary)
                    }
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminDashboardAndCalendarContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events

    if (spec.code in setOf("A02", "s60")) {
        RfCard {
            BasicText("Calendario operativo · Octubre – Noviembre 2026", style = typo.cardTitle)
            Spacer(Modifier.height(6.dp))
            events.forEach { evt ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("${evt.dateLabel} · ${evt.title}", style = typo.bodyStrong)
                        BasicText("${evt.venue} · Responsable: ${evt.leadStaffName}", style = typo.secondary)
                    }
                    RfTag(text = evt.statusLabel, tone = RfTagTone.INDIGO)
                }
            }
        }
        RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RfCard(modifier = Modifier.weight(1f), onClick = { onSelectScreenCode("s58") }) {
            BasicText("Ingresos oct", style = typo.secondary)
            BasicText(
                text = RosaFiestaEngine.formatDop(metrics.octoberRevenueDop),
                style = typo.backScreenTitle.copy(color = RfColors.IndigoBrand),
            )
            RfTag(text = "${metrics.activeEventsCount} eventos activos", tone = RfTagTone.LIMA)
        }
        RfCard(modifier = Modifier.weight(1f), onClick = { onSelectScreenCode("s59") }) {
            BasicText("Por cobrar", style = typo.secondary)
            BasicText(
                text = RosaFiestaEngine.formatDop(metrics.accountsReceivableDop),
                style = typo.backScreenTitle.copy(color = RfColors.SolTintInk),
            )
            RfTag(text = "${metrics.newRequestsCount} solicitudes nuevas", tone = RfTagTone.SOL)
        }
    }

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Eventos de octubre y noviembre", style = typo.cardTitle, modifier = Modifier.weight(1f))
            RfTag(text = "Agenda viva", tone = RfTagTone.INDIGO)
        }
        Spacer(Modifier.height(8.dp))
        events.take(4).forEach { evt ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("${evt.dateLabel} · ${evt.title}", style = typo.bodyStrong)
                    BasicText("${evt.clientName} · ${evt.venue} (${evt.guests} inv.)", style = typo.secondary)
                }
                Spacer(Modifier.width(8.dp))
                RfTag(
                    text = RosaFiestaEngine.formatDop(evt.totalDop),
                    tone = if (evt.paidDepositDop > 0) RfTagTone.LIMA else RfTagTone.SOL,
                )
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminQuotesContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val mateo = events.firstOrNull { it.id == "evt-cumple-mateo" } ?: events.first()

    when (spec.code) {
        "A04", "A07" -> {
            RfCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    BasicText("${mateo.code} · ${mateo.title}", style = typo.cardTitle, modifier = Modifier.weight(1f))
                    RfTag(text = mateo.statusLabel, tone = RfTagTone.INDIGO)
                }
                BasicText("Cliente: ${mateo.clientName} · ${mateo.dateLabel} · ${mateo.venue}", style = typo.secondary)
                Spacer(Modifier.height(6.dp))
                mateo.items.forEach { item ->
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        BasicText("${item.quantity} × ${item.name}", style = typo.body)
                        BasicText(RosaFiestaEngine.formatDop(item.subtotalDop), style = typo.bodyStrong)
                    }
                }
                Spacer(Modifier.height(6.dp))
                BasicText(
                    "Total: ${RosaFiestaEngine.formatDop(mateo.totalDop)} · Anticipo 30 %: ${RosaFiestaEngine.formatDop((mateo.totalDop * 30) / 100)}",
                    style = typo.bodyStrong,
                )
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }
    }

    events.filter { it.id in setOf("evt-cumple-mateo", "evt-grad-promo", "evt-boda-real") }.forEach { evt ->
        RfCard(onClick = { onSelectScreenCode("A04") }) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(evt.title, style = typo.cardTitle, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                RfTag(text = evt.dateLabel, tone = RfTagTone.INDIGO)
            }
            BasicText("Cliente: ${evt.clientName} · ${evt.guests} invitados · ${evt.venue}", style = typo.secondary)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("Total: ${RosaFiestaEngine.formatDop(evt.totalDop)}", style = typo.bodyStrong)
                BasicText("Anticipo 30 %: ${RosaFiestaEngine.formatDop((evt.totalDop * 30) / 100)}", style = typo.secondary)
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminLogisticsContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val checklist = viewModel?.checklist?.collectAsState()?.value ?: CanonicalSampleData.bodaChecklist
    val completedCount = checklist.count { it.completed }
    val progressPct = (completedCount * 100) / checklist.size.coerceAtLeast(1)

    when (spec.code) {
        "A13", "s61" -> {
            CanonicalSampleData.deliveryStops.forEach { stop ->
                RfCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        BasicText("Parada #${stop.order} · ${stop.eventTitle}", style = typo.cardTitle, modifier = Modifier.weight(1f))
                        RfTag(text = stop.statusLabel, tone = RfTagTone.TEAL)
                    }
                    BasicText("${stop.venue} · ${stop.windowLabel}", style = typo.bodyStrong)
                    BasicText(stop.vehicleAndCrew, style = typo.secondary)
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "A14", "A15", "s62" -> {
            CanonicalSampleData.damageIncidents.forEach { inc ->
                RfCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        BasicText("${inc.eventTitle} · ${inc.articleName}", style = typo.cardTitle, modifier = Modifier.weight(1f))
                        RfTag(text = RosaFiestaEngine.formatDop(inc.replacementDop), tone = RfTagTone.SOL)
                    }
                    BasicText("Dañadas: ${inc.damagedUnits} u. · Faltantes: ${inc.missingUnits} u.", style = typo.bodyStrong)
                    BasicText(inc.notes, style = typo.secondary)
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }
    }

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Boda Real Rosa & Dorado · Vie 23 oct", style = typo.cardTitle, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            RfTag(text = "$progressPct % ($completedCount/${checklist.size})", tone = RfTagTone.TEAL)
        }
        Spacer(Modifier.height(6.dp))
        checklist.forEach { step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel?.toggleChecklistStep(step.id) }
                    .padding(vertical = 3.dp)
                    .testTag("checklist-step-${step.id}"),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    RfIcon(
                        kind = if (step.completed) RfIconKind.CHECK else RfIconKind.CALENDAR,
                        tint = if (step.completed) RfColors.LimaTintInk else RfColors.AdminMuted,
                        size = 16.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicText(step.title, style = typo.body.copy(fontSize = 13.sp))
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
private fun RenderAdminFinanceContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
    val payments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events

    when (spec.code) {
        "A21" -> {
            val amountVal = viewModel?.paymentAmountInput?.collectAsState()?.value ?: "3150"
            val methodVal = viewModel?.paymentMethodInput?.collectAsState()?.value ?: "Transferencia Banreservas"
            val refVal = viewModel?.paymentRefInput?.collectAsState()?.value ?: "BR-2026-9941"
            RfCard {
                BasicText("Registrar cobro o abono manual", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Monto recibido (RD$)",
                    value = amountVal,
                    onValueChange = { viewModel?.setPaymentAmountInput(it) },
                )
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Banco / Método de pago",
                    value = methodVal,
                    onValueChange = { viewModel?.setPaymentMethodInput(it) },
                )
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Referencia bancaria",
                    value = refVal,
                    onValueChange = { viewModel?.setPaymentRefInput(it) },
                )
            }
            RfButton(
                label = spec.primaryCta,
                onClick = {
                    viewModel?.submitPayment()
                    onPrimaryAction()
                },
            )
            return
        }

        "A32", "s59" -> {
            events.filter { it.balanceDop > 0 }.forEach { evt ->
                RfCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("${evt.title} · ${evt.clientName}", style = typo.bodyStrong)
                            BasicText("Vence: ${evt.balanceDueDateLabel} · Anticipo: ${RosaFiestaEngine.formatDop(evt.paidDepositDop)}", style = typo.secondary)
                        }
                        RfTag(text = "Saldo ${RosaFiestaEngine.formatDop(evt.balanceDop)}", tone = RfTagTone.SOL)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }
    }

    RfHeroCard(useAdminGradient = true) {
        BasicText("Finanzas de ${metrics.monthLabel}", style = typo.secondary.copy(color = Color.White))
        BasicText(
            text = "Ingresos: ${RosaFiestaEngine.formatDop(metrics.octoberRevenueDop)} · Por cobrar: ${RosaFiestaEngine.formatDop(metrics.accountsReceivableDop)}",
            style = typo.backScreenTitle.copy(color = Color.White),
        )
    }

    payments.forEach { payment ->
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("${payment.eventTitle} · ${payment.clientName}", style = typo.bodyStrong)
                    BasicText("${payment.receiptNumber} · ${payment.methodLabel}", style = typo.secondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    BasicText(RosaFiestaEngine.formatDop(payment.amountDop), style = typo.cardTitle)
                    RfTag(
                        text = if (payment.verified) "Confirmado" else "Pendiente",
                        tone = if (payment.verified) RfTagTone.LIMA else RfTagTone.SOL,
                    )
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminInventoryAndStaffContent(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val articles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles

    when (spec.code) {
        "A23", "s63" -> {
            val codeVal = viewModel?.newArticleCode?.collectAsState()?.value ?: "RF-007"
            val nameVal = viewModel?.newArticleName?.collectAsState()?.value ?: "Silla Chiavari Cristal"
            val priceVal = viewModel?.newArticlePrice?.collectAsState()?.value ?: "120"
            val stockVal = viewModel?.newArticleStock?.collectAsState()?.value ?: "60"
            RfCard {
                BasicText("Ficha de artículo de inventario", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                RfField(label = "Código SKU", value = codeVal, onValueChange = { viewModel?.setNewArticleCode(it) })
                Spacer(Modifier.height(6.dp))
                RfField(label = "Nombre del artículo", value = nameVal, onValueChange = { viewModel?.setNewArticleName(it) })
                Spacer(Modifier.height(6.dp))
                RfField(label = "Precio alquiler por día (RD$)", value = priceVal, onValueChange = { viewModel?.setNewArticlePrice(it) })
                Spacer(Modifier.height(6.dp))
                RfField(label = "Stock total en almacén", value = stockVal, onValueChange = { viewModel?.setNewArticleStock(it) })
            }
            RfButton(
                label = spec.primaryCta,
                onClick = {
                    viewModel?.submitNewArticle()
                    onPrimaryAction()
                },
            )
            return
        }

        "A24" -> {
            CanonicalSampleData.packages.forEach { pkg ->
                RfCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(pkg.name, style = typo.cardTitle)
                            BasicText("${pkg.suggestedGuests} inv. · ${pkg.includedItems.size} renglones incluidos", style = typo.secondary)
                        }
                        RfTag(text = RosaFiestaEngine.formatDop(pkg.priceDop), tone = RfTagTone.INDIGO)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "A25" -> {
            val promoCode = viewModel?.promoCodeInput?.collectAsState()?.value ?: "ROSA10"
            val promoPct = viewModel?.promoDiscountInput?.collectAsState()?.value ?: "10"
            RfCard {
                BasicText("Promociones y tarifas por temporada", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                RfField(label = "Código promocional", value = promoCode, onValueChange = { viewModel?.setPromoCodeInput(it) })
                Spacer(Modifier.height(6.dp))
                RfField(label = "Descuento aplicable (%)", value = promoPct, onValueChange = { viewModel?.setPromoDiscountInput(it) })
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "A26", "A27", "s65" -> {
            CanonicalSampleData.clients.forEach { client ->
                RfCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(client.fullName, style = typo.cardTitle)
                            BasicText("${client.email} · ${client.phone} · ${client.zone}", style = typo.secondary)
                            BasicText(
                                "Contratado: ${RosaFiestaEngine.formatDop(client.totalContractedDop)} · Saldo: ${RosaFiestaEngine.formatDop(client.pendingBalanceDop)}",
                                style = typo.bodyStrong,
                            )
                        }
                        RfTag(text = client.vipTag, tone = RfTagTone.TEAL)
                    }
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }

        "A31" -> {
            val wifiOnly = viewModel?.autoSyncWifiOnly?.collectAsState()?.value ?: false
            RfCard {
                BasicText("Preferencias operativas y respaldo", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Sincronizar fotos solo con Wi-Fi", style = typo.bodyStrong)
                    RfSwitch(checked = wifiOnly, onCheckedChange = { viewModel?.toggleAutoSyncWifiOnly() })
                }
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            return
        }
    }

    articles.take(4).forEach { art ->
        RfCard(onClick = { viewModel?.adjustArticleStock(art.id, 1) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfPhoto(
                    photoKey = art.photo.toPhotoKey(),
                    contentDescription = art.name,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("${art.code} · ${art.name}", style = typo.bodyStrong)
                    BasicText("Stock total: ${art.totalStock} · Libres 23 oct: ${art.availableOct23}", style = typo.secondary)
                }
                RfTag(text = art.formattedDailyRate, tone = RfTagTone.INDIGO)
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

/**
 * Pantalla `A39` — Versión web de escritorio de la App Administrativa (1440×900 dp con barra lateral).
 */
@Composable
fun RenderAdminWebDesktopA39(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit = {},
    onSelectScreenCode: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    RfTheme(variant = RfThemeVariant.ADMIN) {
        val palette = LocalRfPalette.current
        val typo = LocalRfTypography.current
        val metrics = CanonicalSampleData.businessMetrics

        RfRadialBackgroundBox(
            modifier = modifier
                .width(RfDimens.WebFrameWidth)
                .height(RfDimens.WebFrameHeight)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, palette.line, RoundedCornerShape(24.dp))
                .testTag("screen-${spec.code}"),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Columna 1: Barra lateral de navegación administrativa (250 dp)
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight()
                        .background(palette.surface)
                        .border(1.dp, palette.line)
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RfLogo(size = RfLogoSize.MEDIUM)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                BasicText("RosaFiesta", style = typo.backScreenTitle)
                                RfTag(text = "Versión Web · A39", tone = RfTagTone.INDIGO)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        AdminTab.entries.forEachIndexed { idx, tab ->
                            RfChip(
                                label = if (tab.badgeCount > 0) "${tab.label} (${tab.badgeCount})" else tab.label,
                                selected = idx == 0,
                                onClick = {
                                    when (tab) {
                                        AdminTab.INICIO -> onSelectScreenCode("A01")
                                        AdminTab.EVENTOS -> onSelectScreenCode("A11")
                                        AdminTab.ROSA -> onSelectScreenCode("A08")
                                        AdminTab.INVENTARIO -> onSelectScreenCode("A09")
                                        AdminTab.MAS -> onSelectScreenCode("A12")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    RfCard {
                        BasicText("María (Dueña · Admin)", style = typo.bodyStrong)
                        BasicText("Pedro · Yuli · Andrés", style = typo.secondary)
                    }
                }

                // Columna 2: Contenido central con KPIs web (30/800) y tabla de eventos
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(24.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            BasicText("Panel Administrativo Web · Octubre 2026", style = typo.rootTabTitle)
                            BasicText(spec.subtitle, style = typo.secondary)
                        }
                        RfButton(
                            label = spec.primaryCta,
                            onClick = onPrimaryAction,
                            fullWidth = false,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RfCard(modifier = Modifier.weight(1f)) {
                            BasicText("INGRESOS DE OCTUBRE", style = typo.sectionCap)
                            Spacer(Modifier.height(4.dp))
                            BasicText(RosaFiestaEngine.formatDop(metrics.octoberRevenueDop), style = typo.webKpi)
                            RfTag(text = "Pagado y confirmado", tone = RfTagTone.LIMA)
                        }
                        RfCard(modifier = Modifier.weight(1f)) {
                            BasicText("POR COBRAR", style = typo.sectionCap)
                            Spacer(Modifier.height(4.dp))
                            BasicText(RosaFiestaEngine.formatDop(metrics.accountsReceivableDop), style = typo.webKpi)
                            RfTag(text = "Vence 22 y 30 oct", tone = RfTagTone.SOL)
                        }
                        RfCard(modifier = Modifier.weight(1f)) {
                            BasicText("EVENTOS Y SOLICITUDES", style = typo.sectionCap)
                            Spacer(Modifier.height(4.dp))
                            BasicText("${metrics.activeEventsCount} activos · ${metrics.newRequestsCount} nuevas", style = typo.webKpi)
                            RfTag(text = "Oct – Nov 2026", tone = RfTagTone.INDIGO)
                        }
                    }

                    RfCard(modifier = Modifier.weight(1f)) {
                        BasicText("Eventos y cotizaciones del negocio", style = typo.cardTitle)
                        Spacer(Modifier.height(10.dp))
                        CanonicalSampleData.events.forEach { evt ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    RfPhoto(
                                        photoKey = evt.photo.toPhotoKey(),
                                        contentDescription = evt.title,
                                        modifier = Modifier.size(48.dp),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        BasicText("${evt.code} · ${evt.title}", style = typo.bodyStrong)
                                        BasicText(
                                            "${evt.clientName} · ${evt.dateLabel} · ${evt.venue} · ${evt.guests} inv.",
                                            style = typo.secondary,
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    BasicText("Total ${RosaFiestaEngine.formatDop(evt.totalDop)}", style = typo.bodyStrong)
                                    RfTag(
                                        text = if (evt.balanceDop == 0) "Liquidado" else "Saldo ${RosaFiestaEngine.formatDop(evt.balanceDop)}",
                                        tone = if (evt.balanceDop == 0) RfTagTone.LIMA else RfTagTone.SOL,
                                    )
                                }
                            }
                        }
                    }
                }

                // Columna 3: Rosa IA Copiloto (siempre degradado admin índigo → turquesa)
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                        .background(palette.surface)
                        .border(1.dp, palette.line)
                        .padding(20.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(RfDimens.CardRadius))
                            .background(palette.rosaAssistantBgGradient)
                            .padding(14.dp),
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = true, glow = true)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    BasicText("Rosa IA · Admin", style = typo.cardTitle)
                                    RfTag(text = "Índigo → Turquesa", tone = RfTagTone.TEAL)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            BasicText(
                                "«María, el saldo de Boda Real Rosa & Dorado (RD$ 3,150) vence el jue 22 oct y el de Quinceañera Valentina Crystal (RD$ 4,340) el vie 30 oct.»",
                                style = typo.body.copy(fontSize = 13.sp),
                            )
                        }
                    }

                    RfCard(modifier = Modifier.weight(1f)) {
                        BasicText("Precios y stock por día", style = typo.cardTitle)
                        Spacer(Modifier.height(8.dp))
                        CanonicalSampleData.articles.forEach { art ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            ) {
                                BasicText(
                                    text = art.name,
                                    style = typo.body.copy(fontSize = 12.5.sp),
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(Modifier.width(6.dp))
                                RfTag(
                                    text = "${art.formattedDailyRate} · ${art.availableOct23}/${art.totalStock}",
                                    tone = RfTagTone.INDIGO,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
