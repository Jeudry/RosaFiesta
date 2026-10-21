package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
                    RenderSheetSpecificContent(spec, onPrimaryAction, onSelectScreenCode)
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
                    pendingChangesCount = CanonicalSampleData.offlineQueue.size,
                    isAdmin = isAdmin,
                    onRetry = onPrimaryAction,
                )
            }

            ScreenHeaderBlock(spec, onSelectScreenCode)

            RenderScreenBodyByCode(
                spec = spec,
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
                    value = "El arco orgánico y las 120 sillas Tiffany quedaron impecables en Jardín Botánico.",
                )
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        }

        "s81" -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RfChip("Vie 23 oct", selected = true, onClick = {})
                RfChip("Sillas y mesas", selected = true, onClick = {})
                RfChip("Arcos", selected = false, onClick = {})
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
            RfCard {
                RfTag(text = "Primer cambio de fecha gratis", tone = RfTagTone.LIMA)
                Spacer(Modifier.height(6.dp))
                BasicText("Fecha actual: Vie 23 oct · Jardín Botánico", style = typo.bodyStrong)
                BasicText("Nueva fecha solicitada: Vie 30 oct · Stock de 145 artículos disponible", style = typo.secondary)
            }
            RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
        }

        "s85" -> {
            RfCard {
                BasicText("Política de cancelación", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                BasicText("• Más de 7 días antes: 90 % de reembolso (RD$ 1,215)", style = typo.body)
                BasicText("• De 3 a 7 días antes: 50 % de reembolso (RD$ 675)", style = typo.body)
                BasicText("• Menos de 3 días antes: sin reembolso (RD$ 0)", style = typo.body)
            }
            RfButton(label = spec.primaryCta, style = RfButtonStyle.DANGER, onClick = onPrimaryAction)
        }
    }
}

@Composable
private fun RenderScreenBodyByCode(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
    onSwitchAppMode: () -> Unit,
) {
    when (spec.code) {
        // Estados explícitos de la Sección 5
        "s90", "A36" -> {
            RfSkeletonScreen(isAdmin = spec.variant == RfThemeVariant.ADMIN)
        }

        "s89", "A35" -> {
            RenderEmptyStateScreen(spec, onPrimaryAction, onSelectScreenCode)
        }

        "s91", "A37" -> {
            RenderOfflineAndServerErrorScreen(spec, onPrimaryAction)
        }

        "s88" -> {
            RenderDeleteAccountDisabledScreen(spec, onPrimaryAction)
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

        "s47" -> {
            RenderGuestsAndTablesScreen(spec, onPrimaryAction)
        }

        "A28" -> {
            RenderTeamPermissionsScreen(spec, onPrimaryAction)
        }

        "A30" -> {
            RenderAdminRoleSwitcherScreen(spec, onSwitchAppMode, onPrimaryAction, onSelectScreenCode)
        }

        "s55", "s56" -> {
            RenderSuppliersScreen(spec, onPrimaryAction)
        }

        "s71", "A34" -> {
            RenderReviewsAndReplyScreen(spec, onPrimaryAction)
        }

        "s72", "s73", "s74" -> {
            RenderWhatsAppBotScreen(spec, onPrimaryAction)
        }

        else -> {
            when (spec.archetype) {
                ScreenArchetype.BRAND_COVER, ScreenArchetype.SPLASH, ScreenArchetype.ONBOARDING ->
                    RenderSplashAndOnboardingContent(spec, onPrimaryAction)

                ScreenArchetype.AUTH_FORM ->
                    RenderAuthFormByCode(spec, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_HOME ->
                    RenderPersonalHomeContent(spec, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_CATALOG, ScreenArchetype.PERSONAL_PRODUCT_DETAIL ->
                    RenderPersonalCatalogContent(spec, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_CART_QUOTE, ScreenArchetype.PERSONAL_CONTRACT_SIGN, ScreenArchetype.PERSONAL_PAYMENT ->
                    RenderPersonalQuoteAndPaymentContent(spec, onPrimaryAction)

                ScreenArchetype.PERSONAL_EVENT_HUB, ScreenArchetype.PERSONAL_DATE_CANCEL ->
                    RenderPersonalEventsContent(spec, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_ROSA_CHAT ->
                    RenderRosaAssistantContent(spec, isAdmin = false, onPrimaryAction = onPrimaryAction)

                ScreenArchetype.PERSONAL_PROFILE, ScreenArchetype.PERSONAL_SYSTEM_STATE ->
                    RenderPersonalProfileAndStateContent(spec, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.ADMIN_LOGIN_GATE, ScreenArchetype.ADMIN_ROLE_SWITCHER ->
                    RenderAdminRoleSwitcherScreen(spec, onSwitchAppMode, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.ADMIN_DASHBOARD, ScreenArchetype.ADMIN_CALENDAR ->
                    RenderAdminDashboardAndCalendarContent(spec, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.ADMIN_QUOTES ->
                    RenderAdminQuotesContent(spec, onPrimaryAction)

                ScreenArchetype.ADMIN_LOGISTICS, ScreenArchetype.ADMIN_OFFLINE_SYNC ->
                    RenderAdminLogisticsContent(spec, onPrimaryAction)

                ScreenArchetype.ADMIN_ROSA_COPILOT ->
                    RenderRosaAssistantContent(spec, isAdmin = true, onPrimaryAction = onPrimaryAction)

                ScreenArchetype.ADMIN_FINANCE ->
                    RenderAdminFinanceContent(spec, onPrimaryAction)

                ScreenArchetype.ADMIN_INVENTORY_STAFF ->
                    RenderAdminInventoryAndStaffContent(spec, onPrimaryAction)

                ScreenArchetype.ADMIN_WEB_DESKTOP -> Unit
            }
        }
    }
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
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
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
    CanonicalSampleData.offlineQueue.forEach { item ->
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
    RfButton(label = "Reintentar", onClick = onPrimaryAction)
}

@Composable
private fun RenderDeleteAccountDisabledScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
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
                    "Tienes 2 eventos activos (Boda Real Rosa & Dorado el vie 23 oct y Quinceañera Valentina Crystal el lun 2 nov).",
                    style = typo.secondary,
                )
            }
        }
    }
    RfButton(
        label = "Eliminar cuenta permanentemente",
        style = RfButtonStyle.DANGER,
        enabled = false,
        onClick = {},
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
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    CanonicalSampleData.staffMembers.forEach { staff ->
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
                    text = if (staff.name == "Yuli") "Sin pagos" else "Acceso rol",
                    tone = if (staff.name == "Yuli") RfTagTone.SOL else RfTagTone.INDIGO,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = if (staff.name == "Yuli") "Decoración y tareas (Pagos desactivados)" else "Ver pagos y cobros",
                    style = typo.body,
                )
                RfSwitch(checked = staff.name != "Yuli")
            }
        }
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminRoleSwitcherScreen(
    spec: ScreenSpec,
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
    RfCard(onClick = { onSelectScreenCode("A01") }) {
        BasicText("Entrar como Dueña (María · Admin)", style = typo.cardTitle)
        BasicText("Acceso completo a eventos, inventario, pagos, reportes y configuración.", style = typo.secondary)
    }
    RfCard(onClick = { onSelectScreenCode("A28") }) {
        BasicText("Entrar como Equipo (Pedro, Yuli, Andrés)", style = typo.cardTitle)
        BasicText("Yuli: decoración y tareas, sin pagos · Pedro: logística · Andrés: almacén.", style = typo.secondary)
    }
    RfButton(
        label = "Revisar App Personal como cliente",
        style = RfButtonStyle.SECONDARY,
        onClick = {
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
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
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
                label = "Respuesta sugerida por Rosa IA Admin",
                value = "¡Gracias, Camila! Fue un honor acompañar tu Boda Real Rosa & Dorado junto a Pedro y Yuli.",
            )
        }
    }
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderWhatsAppBotScreen(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current
    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText("Bot de WhatsApp + Rosa IA", style = typo.cardTitle)
                BasicText("Línea +1 (809) 555-0199 · Responde precios por día y disponibilidad", style = typo.secondary)
            }
            RfSwitch(checked = true)
        }
        Spacer(Modifier.height(8.dp))
        BasicText("Plantilla activa: «¡Hola! Silla Tiffany RD$ 4/día, Mesa redonda RD$ 25/día, Arco RD$ 95/día.»", style = typo.body)
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
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
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
                RfField(label = "Correo electrónico", value = "camila@rosafiesta.do")
                Spacer(Modifier.height(10.dp))
                RfField(label = "Contraseña", value = "••••••••••••", helperOrError = "¿Olvidaste tu contraseña? Recuperar en s09")
                Spacer(Modifier.height(12.dp))
                RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            }
        }

        "s09", "s10" -> {
            RfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RfIllo(kind = RfIlloKind.CANDADO, size = 52.dp)
                    Spacer(Modifier.width(12.dp))
                    BasicText(
                        text = if (spec.code == "s09") "Enviaremos un enlace seguro a camila@rosafiesta.do" else "Ingresa tu nueva contraseña segura",
                        style = typo.body,
                    )
                }
                Spacer(Modifier.height(10.dp))
                RfField(
                    label = if (spec.code == "s09") "Correo registrado" else "Nueva contraseña",
                    value = if (spec.code == "s09") "camila@rosafiesta.do" else "RosaFiesta2026*",
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
                RfField(label = "Nombre completo", value = "Camila Rodríguez")
                Spacer(Modifier.height(8.dp))
                RfField(label = "Correo electrónico", value = "camila@rosafiesta.do")
                Spacer(Modifier.height(8.dp))
                RfField(label = "Teléfono / WhatsApp", value = "+1 (809) 555-0142")
                Spacer(Modifier.height(12.dp))
                RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
            }
        }
    }
}

@Composable
private fun RenderPersonalHomeContent(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val boda = CanonicalSampleData.events.first()

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

    CanonicalSampleData.articles.take(3).forEach { article ->
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
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RfChip("Todos (6)", selected = spec.code !in setOf("s21", "s22"), onClick = { onSelectScreenCode("s18") })
        RfChip("Paquetes", selected = spec.code in setOf("s21", "s22"), onClick = { onSelectScreenCode("s21") })
        RfChip("Filtros", selected = false, onClick = { onSelectScreenCode("s81") })
    }

    CanonicalSampleData.articles.take(4).forEach { article ->
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
private fun RenderPersonalQuoteAndPaymentContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
    val boda = CanonicalSampleData.events.first()

    if (spec.code in setOf("s33", "s51")) {
        RfCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            ) {
                RfIllo(kind = RfIlloKind.EXITO, size = 76.dp)
                BasicText(spec.title, style = typo.sheetOrEmptyTitle)
                BasicText(spec.subtitle, style = typo.secondary)
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(boda.title, style = typo.cardTitle)
            RfTag(text = boda.code, tone = RfTagTone.VIOLETA)
        }
        Spacer(Modifier.height(4.dp))
        BasicText("${boda.dateLabel} · ${boda.venue} · ${boda.guests} invitados", style = typo.secondary)
        Spacer(Modifier.height(8.dp))
        boda.items.forEach { line ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            ) {
                BasicText("${line.quantity} × ${line.name}", style = typo.body)
                BasicText(RosaFiestaEngine.formatDop(line.subtotalDop), style = typo.bodyStrong)
            }
        }
        Spacer(Modifier.height(8.dp))
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Anticipo 30 % (transferencia, 18 oct)", style = typo.bodyStrong)
            RfTag(text = RosaFiestaEngine.formatDop(boda.paidDepositDop), tone = RfTagTone.LIMA)
        }
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Saldo 70 % (vence ${boda.balanceDueDateLabel})", style = typo.secondary)
            RfTag(text = RosaFiestaEngine.formatDop(boda.balanceDop), tone = RfTagTone.SOL)
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalEventsContent(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    if (spec.code == "s40") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            RfChip("Ejecución", selected = true, onClick = { onSelectScreenCode("s43") })
            RfChip("Cronograma", selected = false, onClick = { onSelectScreenCode("s44") })
            RfChip("Invitados", selected = false, onClick = { onSelectScreenCode("s47") })
        }
    }

    CanonicalSampleData.events.take(2).forEach { event ->
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
    isAdmin: Boolean,
    onPrimaryAction: () -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val proposal = RosaFiestaEngine.buildRosaProposal("Boda Real Rosa & Dorado", 120)

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
                text = if (isAdmin) {
                    "«María, verifiqué almacén para el lun 2 nov: tenemos las 100 sillas Tiffany y 13 mesas redondas libres para Quinceañera Valentina Crystal (RD$ 6,200).»"
                } else {
                    "«Camila, para tus 120 invitados en Jardín Botánico preparé 120 sillas Tiffany, 15 mesas redondas, 8 centros florales, 1 arco orgánico y mesa dulce por RD$ 4,500.»"
                },
                style = typo.body,
            )
        }
    }

    RfCard {
        BasicText("Lista y cotización armada por Rosa IA", style = typo.cardTitle)
        Spacer(Modifier.height(6.dp))
        proposal.lines.forEach { line ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            ) {
                BasicText("${line.quantity} × ${line.name}", style = typo.body)
                BasicText(RosaFiestaEngine.formatDop(line.subtotalDop), style = typo.bodyStrong)
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalProfileAndStateContent(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    RfCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RfIconTile(
                icon = RfIconKind.USER,
                backgroundColor = RfColors.RosaTint,
                iconTint = RfColors.RosaTintInk,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText("Camila Rodríguez", style = typo.cardTitle)
                BasicText("camila@rosafiesta.do · +1 (809) 555-0142", style = typo.secondary)
                Spacer(Modifier.height(4.dp))
                RfTag(text = "2 eventos activos · Saldo RD$ 7,490", tone = RfTagTone.ROSA)
            }
        }
    }

    if (spec.code == "s86") {
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("Recordatorios de pago y montaje", style = typo.bodyStrong)
                RfSwitch(checked = true)
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
    } else {
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

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminDashboardAndCalendarContent(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val metrics = CanonicalSampleData.businessMetrics

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
            RfTag(text = "4 eventos activos", tone = RfTagTone.LIMA)
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
        CanonicalSampleData.events.take(4).forEach { evt ->
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
private fun RenderAdminQuotesContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current

    CanonicalSampleData.events.filter { it.id in setOf("evt-cumple-mateo", "evt-grad-promo", "evt-boda-real") }.forEach { evt ->
        RfCard {
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
private fun RenderAdminLogisticsContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Boda Real Rosa & Dorado · Vie 23 oct", style = typo.cardTitle, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            RfTag(text = "66 % (4/6)", tone = RfTagTone.TEAL)
        }
        Spacer(Modifier.height(6.dp))
        CanonicalSampleData.bodaChecklist.forEach { step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
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
private fun RenderAdminFinanceContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
    val metrics = CanonicalSampleData.businessMetrics

    RfHeroCard(useAdminGradient = true) {
        BasicText("Finanzas de ${metrics.monthLabel}", style = typo.secondary.copy(color = Color.White))
        BasicText(
            text = "Ingresos: ${RosaFiestaEngine.formatDop(metrics.octoberRevenueDop)} · Por cobrar: ${RosaFiestaEngine.formatDop(metrics.accountsReceivableDop)}",
            style = typo.backScreenTitle.copy(color = Color.White),
        )
    }

    CanonicalSampleData.payments.forEach { payment ->
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
private fun RenderAdminInventoryAndStaffContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current

    CanonicalSampleData.articles.take(4).forEach { art ->
        RfCard {
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
