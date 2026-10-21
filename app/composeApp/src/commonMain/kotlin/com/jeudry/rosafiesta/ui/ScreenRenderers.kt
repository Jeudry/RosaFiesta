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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.AdminTabBar
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.designsystem.PersonalTabBar
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfField
import com.jeudry.rosafiesta.designsystem.RfHeroCard
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfIllo
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfOfflineBanner
import com.jeudry.rosafiesta.designsystem.RfPhoneFrame
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfSegmented
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
 * Renderiza cualquier pantalla de [ScreenCatalog] dentro de su marco oficial (390×820 móvil o 1440×900 web).
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
            modifier = modifier,
        )
        return
    }

    val isAdmin = spec.variant == RfThemeVariant.ADMIN
    RfPhoneFrame(
        variant = spec.variant,
        screenCode = spec.code,
        screenTitle = spec.title,
        bottomBar = {
            if (isAdmin) {
                val activeAdminTab = when (spec.sectionNumber) {
                    11 -> AdminTab.AGENDA
                    12, 13 -> AdminTab.ORDERS
                    16 -> AdminTab.CATALOG
                    17 -> AdminTab.SETTINGS
                    else -> AdminTab.DASHBOARD
                }
                AdminTabBar(
                    activeTab = activeAdminTab,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onSelectScreenCode("s55")
                            AdminTab.AGENDA -> onSelectScreenCode("A03")
                            AdminTab.ORDERS -> onSelectScreenCode("A05")
                            AdminTab.CATALOG -> onSelectScreenCode("s72")
                            AdminTab.SETTINGS -> onSelectScreenCode("A28")
                        }
                    },
                )
            } else if (spec.sectionNumber >= 3) {
                val activePersonalTab = when (spec.sectionNumber) {
                    4, 5 -> PersonalTab.CATALOG
                    6 -> PersonalTab.EVENTS
                    7 -> PersonalTab.ROSA_IA
                    8 -> PersonalTab.PROFILE
                    else -> PersonalTab.HOME
                }
                PersonalTabBar(
                    activeTab = activePersonalTab,
                    onSelectTab = { tab ->
                        when (tab) {
                            PersonalTab.HOME -> onSelectScreenCode("s14")
                            PersonalTab.CATALOG -> onSelectScreenCode("s18")
                            PersonalTab.ROSA_IA -> onSelectScreenCode("s40")
                            PersonalTab.EVENTS -> onSelectScreenCode("s33")
                            PersonalTab.PROFILE -> onSelectScreenCode("s50")
                        }
                    },
                )
            }
        },
        modifier = modifier.testTag("screen-${spec.code}"),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
        ) {
            ScreenHeaderBlock(spec)

            when (spec.archetype) {
                ScreenArchetype.BRAND_COVER, ScreenArchetype.SPLASH, ScreenArchetype.ONBOARDING ->
                    RenderCoverAndOnboardingContent(spec, onPrimaryAction)

                ScreenArchetype.AUTH_FORM ->
                    RenderAuthFormContent(spec, onPrimaryAction)

                ScreenArchetype.PERSONAL_HOME ->
                    RenderPersonalHomeContent(spec, onPrimaryAction, onSelectScreenCode)

                ScreenArchetype.PERSONAL_CATALOG, ScreenArchetype.PERSONAL_PRODUCT_DETAIL ->
                    RenderPersonalCatalogContent(spec, onPrimaryAction)

                ScreenArchetype.PERSONAL_CART_QUOTE, ScreenArchetype.PERSONAL_CONTRACT_SIGN, ScreenArchetype.PERSONAL_PAYMENT ->
                    RenderPersonalQuoteAndPaymentContent(spec, onPrimaryAction)

                ScreenArchetype.PERSONAL_EVENT_HUB, ScreenArchetype.PERSONAL_DATE_CANCEL ->
                    RenderPersonalEventsContent(spec, onPrimaryAction)

                ScreenArchetype.PERSONAL_ROSA_CHAT ->
                    RenderRosaAssistantContent(spec, isAdmin = false, onPrimaryAction = onPrimaryAction)

                ScreenArchetype.PERSONAL_PROFILE, ScreenArchetype.PERSONAL_SYSTEM_STATE ->
                    RenderPersonalProfileAndStateContent(spec, onPrimaryAction)

                ScreenArchetype.ADMIN_LOGIN_GATE, ScreenArchetype.ADMIN_ROLE_SWITCHER ->
                    RenderAdminGateAndRoleSwitchContent(spec, onSwitchAppMode, onPrimaryAction)

                ScreenArchetype.ADMIN_DASHBOARD, ScreenArchetype.ADMIN_CALENDAR ->
                    RenderAdminDashboardAndCalendarContent(spec, onPrimaryAction)

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
private fun ScreenHeaderBlock(spec: ScreenSpec) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RfLogo(size = RfLogoSize.SMALL)
            Spacer(Modifier.width(8.dp))
            Column {
                BasicText(
                    text = spec.sectionTitle.uppercase(),
                    style = typo.tagLabel.copy(color = palette.primaryAccent, fontSize = 10.sp),
                )
                BasicText(
                    text = spec.title,
                    style = typo.screenTitle.copy(fontSize = 19.sp),
                )
            }
        }
        RfTag(
            text = spec.highlightMetric,
            tone = if (palette.isAdmin) RfTagTone.INDIGO else RfTagTone.SOL,
        )
    }
    BasicText(
        text = spec.subtitle,
        style = typo.body.copy(color = palette.muted, fontSize = 12.5.sp),
    )
}

@Composable
private fun RenderCoverAndOnboardingContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
    RfHeroCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RfLogo(size = RfLogoSize.LARGE)
            RosaAvatar(size = RosaAvatarSize.LARGE)
        }
        Spacer(Modifier.height(10.dp))
        BasicText(
            text = "RosaFiesta · Tu fiesta soñada en República Dominicana",
            style = typo.heroTitle.copy(color = Color.White, fontSize = 22.sp),
        )
        Spacer(Modifier.height(6.dp))
        BasicText(
            text = "Mobiliario Tiffany, arcos orgánicos, diseño floral y planificación inteligente con Rosa IA.",
            style = typo.body.copy(color = Color.White.copy(alpha = 0.9f)),
        )
    }

    RfPhoto(
        photoKey = spec.heroPhoto,
        contentDescription = spec.title,
        bottomScrim = true,
        modifier = Modifier.fillMaxWidth().height(170.dp),
    )

    RfCard {
        BasicText("Precios transparentes por día", style = typo.cardTitle)
        Spacer(Modifier.height(6.dp))
        CanonicalSampleData.articles.take(3).forEach { art ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            ) {
                BasicText(art.name, style = typo.body)
                BasicText(art.formattedDailyRate, style = typo.bodyStrong)
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAuthFormContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val isValidationError = spec.code == "s85"
    RfCard {
        RfField(
            label = "Nombre completo",
            value = "Camila Rodríguez",
        )
        Spacer(Modifier.height(10.dp))
        RfField(
            label = "Correo electrónico",
            value = "camila@rosafiesta.do",
            helperOrError = if (isValidationError) "Este correo ya tiene una sesión activa" else "Enviaremos tus contratos y recibos aquí",
            isError = isValidationError,
        )
        Spacer(Modifier.height(10.dp))
        RfField(
            label = "Teléfono / WhatsApp (RD)",
            value = "809-555-0142",
        )
        Spacer(Modifier.height(12.dp))
        RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
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
                    style = typo.sectionTitle.copy(color = Color.White, fontSize = 19.sp),
                )
                BasicText(
                    text = "${boda.venue} · ${boda.guests} invitados · ${boda.articleCount} artículos",
                    style = typo.body.copy(color = Color.White.copy(alpha = 0.9f), fontSize = 12.5.sp),
                )
            }
            RosaAvatar(size = RosaAvatarSize.MEDIUM)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Anticipo 30 %: RD$ 1,350",
                style = typo.caption.copy(color = Color.White),
            )
            BasicText(
                text = "Saldo RD$ 3,150 (${boda.balanceDueDateLabel})",
                style = typo.bodyStrong.copy(color = RfColors.Sol, fontSize = 12.5.sp),
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RfChip("Bodas", selected = true, onClick = { onSelectScreenCode("s16") })
        RfChip("Quinceañeras", selected = false, onClick = { onSelectScreenCode("s16") })
        RfChip("Cumpleaños", selected = false, onClick = { onSelectScreenCode("s16") })
    }

    CanonicalSampleData.articles.take(3).forEach { article ->
        RfCard(onClick = { onSelectScreenCode("s23") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfPhoto(
                    photoKey = article.photo.toPhotoKey(),
                    contentDescription = article.name,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(article.name, style = typo.cardTitle)
                    BasicText(article.category, style = typo.caption)
                    Spacer(Modifier.height(4.dp))
                    RfTag(text = article.formattedDailyRate, tone = RfTagTone.ROSA)
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalCatalogContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
    RfSegmented(
        options = listOf("Artículos (6)", "Paquetes (3)", "Plantillas (6)"),
        selectedIndex = if (spec.code in setOf("s21", "s22")) 1 else 0,
        onSelect = {},
    )

    CanonicalSampleData.articles.take(4).forEach { article ->
        RfCard {
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
                        text = "Disponibles 23 oct: ${article.availableOct23} de ${article.totalStock}",
                        style = typo.caption,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RfTag(text = article.formattedDailyRate, tone = RfTagTone.ROSA)
                        RfTag(text = "En stock", tone = RfTagTone.LIMA)
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

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(boda.title, style = typo.cardTitle)
            RfTag(text = boda.code, tone = RfTagTone.VIOLETA)
        }
        Spacer(Modifier.height(4.dp))
        BasicText("${boda.dateLabel} · ${boda.venue} · ${boda.guests} invitados", style = typo.caption)
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Total del evento", style = typo.cardTitle)
            BasicText(RosaFiestaEngine.formatDop(boda.totalDop), style = typo.kpiPrice.copy(fontSize = 20.sp))
        }
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Anticipo 30 % (para reservar)", style = typo.bodyStrong)
            RfTag(text = RosaFiestaEngine.formatDop(boda.paidDepositDop), tone = RfTagTone.SOL)
        }
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Saldo 70 % (vence ${boda.balanceDueDateLabel})", style = typo.caption)
            BasicText(RosaFiestaEngine.formatDop(boda.balanceDop), style = typo.bodyStrong)
        }
    }

    if (spec.code in setOf("s28", "s29")) {
        RfCard {
            BasicText("Firma digital de contrato", style = typo.cardTitle)
            Spacer(Modifier.height(4.dp))
            BasicText(
                "Firmado por: Camila Rodríguez · Política: >7d 90 % · 3-7d 50 % · <3d 0 %. Primer cambio de fecha gratis.",
                style = typo.caption,
            )
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderPersonalEventsContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current

    CanonicalSampleData.events.take(2).forEach { event ->
        RfCard {
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
                        BasicText(event.title, style = typo.cardTitle)
                        RfTag(text = event.dateLabel, tone = RfTagTone.LIMA)
                    }
                    BasicText("${event.venue} · ${event.guests} inv. · ${event.articleCount} art.", style = typo.caption)
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        "Total ${RosaFiestaEngine.formatDop(event.totalDop)} · Saldo ${RosaFiestaEngine.formatDop(event.balanceDop)}",
                        style = typo.bodyStrong,
                    )
                }
            }
        }
    }

    if (spec.code in setOf("s36", "s37", "s75", "s76")) {
        RfCard {
            BasicText("Política de cambios y cancelación", style = typo.cardTitle)
            Spacer(Modifier.height(4.dp))
            BasicText("• Primer cambio de fecha: Gratis sujeto a disponibilidad", style = typo.body)
            BasicText("• Más de 7 días antes: 90 % de reembolso (RD$ 1,215)", style = typo.body)
            BasicText("• Entre 3 y 7 días antes: 50 % de reembolso (RD$ 675)", style = typo.body)
            BasicText("• Menos de 3 días antes: 0 % de reembolso", style = typo.body)
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
                RosaAvatar(size = RosaAvatarSize.LARGE, forceAdminPalette = isAdmin)
                Spacer(Modifier.width(12.dp))
                Column {
                    RfTag(
                        text = if (isAdmin) "Copiloto Administrativo · Índigo→Turquesa" else "Rosa IA · Asistente de Eventos",
                        tone = if (isAdmin) RfTagTone.TEAL else RfTagTone.VIOLETA,
                    )
                    Spacer(Modifier.height(4.dp))
                    BasicText(spec.title, style = typo.cardTitle)
                }
            }
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = if (isAdmin) {
                    "«María, verifiqué almacén para el lun 2 nov: tenemos las 100 sillas Tiffany y 13 mesas redondas libres para Valentina Crystal (RD$ 6,200).»"
                } else {
                    "«Camila, para tus 120 invitados en Jardín Botánico preparé 120 sillas Tiffany, 15 mesas redondas, 8 centros florales, 1 arco orgánico y estación dulce por RD$ 4,500.»"
                },
                style = typo.body,
            )
        }
    }

    RfCard {
        BasicText("Desglose inteligente sugerido por Rosa IA", style = typo.cardTitle)
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
private fun RenderPersonalProfileAndStateContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current

    if (spec.code == "s79") {
        RfOfflineBanner(isAdmin = false)
    }

    if (spec.code == "s88") {
        RfCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfIcon(kind = RfIconKind.WARNING, tint = RfColors.Error, size = 24.dp)
                Spacer(Modifier.width(10.dp))
                BasicText("No puedes eliminar tu cuenta aún", style = typo.cardTitle.copy(color = RfColors.Error))
            }
            Spacer(Modifier.height(6.dp))
            BasicText(
                "Tienes 2 eventos confirmados activos (Boda Real Rosa & Dorado el 23 oct y Quinceañera Valentina Crystal el 2 nov). Liquídalos o cancélalos antes de dar de baja tu cuenta.",
                style = typo.body,
            )
        }
    } else {
        RfCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfIllo(title = spec.title, accentColor = RfColors.RosaBrand)
                Spacer(Modifier.width(12.dp))
                Column {
                    BasicText("Camila Rodríguez", style = typo.cardTitle)
                    BasicText("camila@rosafiesta.do · 809-555-0142", style = typo.caption)
                    Spacer(Modifier.height(4.dp))
                    RfTag(text = "2 eventos activos · Saldo RD$ 7,490", tone = RfTagTone.ROSA)
                }
            }
        }
    }

    RfToast(message = "Cambios guardados en tu perfil de RosaFiesta", actionLabel = "OK")
    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminGateAndRoleSwitchContent(
    spec: ScreenSpec,
    onSwitchAppMode: () -> Unit,
    onPrimaryAction: () -> Unit,
) {
    val typo = LocalRfTypography.current

    RfHeroCard(useAdminGradient = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RfTag(text = "Cambio de rol · A30", tone = RfTagTone.TEAL)
                Spacer(Modifier.height(6.dp))
                BasicText(
                    text = "Sesión activa: María (Dueña)",
                    style = typo.sectionTitle.copy(color = Color.White),
                )
                BasicText(
                    text = "Alterna en un toque entre la App Administrativa y la vista de cliente en la App Personal.",
                    style = typo.body.copy(color = Color.White.copy(alpha = 0.9f)),
                )
            }
            RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = true)
        }
    }

    CanonicalSampleData.staffMembers.forEach { member ->
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    BasicText(member.name, style = typo.cardTitle)
                    BasicText(member.roleLabel, style = typo.caption)
                }
                RfTag(text = "${member.permissions.size} permisos", tone = RfTagTone.INDIGO)
            }
        }
    }

    RfButton(
        label = spec.primaryCta,
        onClick = {
            onSwitchAppMode()
            onPrimaryAction()
        },
    )
}

@Composable
private fun RenderAdminDashboardAndCalendarContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
    val metrics = CanonicalSampleData.businessMetrics

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RfCard(modifier = Modifier.weight(1f)) {
            BasicText("Ingresos oct", style = typo.caption)
            BasicText(
                text = RosaFiestaEngine.formatDop(metrics.octoberRevenueDop),
                style = typo.kpiPrice.copy(color = RfColors.IndigoBrand, fontSize = 20.sp),
            )
            RfTag(text = "+18 % vs sep", tone = RfTagTone.LIMA)
        }
        RfCard(modifier = Modifier.weight(1f)) {
            BasicText("Por cobrar", style = typo.caption)
            BasicText(
                text = RosaFiestaEngine.formatDop(metrics.accountsReceivableDop),
                style = typo.kpiPrice.copy(color = RfColors.SolTintInk, fontSize = 20.sp),
            )
            RfTag(text = "${metrics.activeEventsCount} eventos activos", tone = RfTagTone.SOL)
        }
    }

    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Agenda operativa · Oct / Nov",
                style = typo.cardTitle,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            RfTag(text = "${metrics.newRequestsCount} solicitudes", tone = RfTagTone.INDIGO)
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
                    BasicText("${evt.clientName} · ${evt.venue} (${evt.guests} inv.)", style = typo.caption)
                }
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
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(evt.title, style = typo.cardTitle)
                RfTag(text = evt.dateLabel, tone = RfTagTone.INDIGO)
            }
            BasicText("Cliente: ${evt.clientName} · ${evt.guests} invitados · ${evt.venue}", style = typo.caption)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("Estimado: ${RosaFiestaEngine.formatDop(evt.totalDop)}", style = typo.bodyStrong)
                BasicText("Anticipo 30 %: ${RosaFiestaEngine.formatDop((evt.totalDop * 30) / 100)}", style = typo.caption)
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
private fun RenderAdminLogisticsContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current

    if (spec.code in setOf("A36", "A37")) {
        RfOfflineBanner(pendingChangesCount = CanonicalSampleData.offlineQueue.size, isAdmin = true)
        CanonicalSampleData.offlineQueue.forEach { item ->
            RfCard {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(item.actionLabel, style = typo.bodyStrong)
                        BasicText("${item.entityCode} · ${item.timeAgo}", style = typo.caption)
                    }
                    RfTag(text = "En cola", tone = RfTagTone.SOL)
                }
            }
        }
    } else {
        RfCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("Checklist · Boda Real Rosa & Dorado", style = typo.cardTitle)
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
                        BasicText(step.title, style = typo.body.copy(fontSize = 12.5.sp))
                    }
                    RfTag(
                        text = step.completedBy,
                        tone = if (step.completed) RfTagTone.LIMA else RfTagTone.SOL,
                    )
                }
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
        BasicText("Finanzas de ${metrics.monthLabel}", style = typo.caption.copy(color = Color.White))
        BasicText(
            text = "Cobrado: ${RosaFiestaEngine.formatDop(metrics.octoberRevenueDop)} · Por cobrar: ${RosaFiestaEngine.formatDop(metrics.accountsReceivableDop)}",
            style = typo.sectionTitle.copy(color = Color.White),
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
                    BasicText("${payment.receiptNumber} · ${payment.methodLabel}", style = typo.caption)
                }
                Column(horizontalAlignment = Alignment.End) {
                    BasicText(RosaFiestaEngine.formatDop(payment.amountDop), style = typo.cardTitle)
                    RfTag(
                        text = if (payment.verified) "Verificado" else "Por verificar",
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

    if (spec.code == "A28") {
        CanonicalSampleData.staffMembers.forEach { staff ->
            RfCard {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(staff.name, style = typo.cardTitle)
                        BasicText(staff.roleLabel, style = typo.caption)
                    }
                    RfTag(
                        text = if (staff.name == "Yuli") "Sin pagos" else "Operativo",
                        tone = if (staff.name == "Yuli") RfTagTone.SOL else RfTagTone.INDIGO,
                    )
                }
            }
        }
    } else {
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
                        BasicText("Stock total: ${art.totalStock} · Libres 23 oct: ${art.availableOct23}", style = typo.caption)
                    }
                    RfTag(text = art.formattedDailyRate, tone = RfTagTone.INDIGO)
                }
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

/**
 * Pantalla `A39` — Panel web de escritorio de la App Administrativa (1440×900 dp, 3 columnas).
 */
@Composable
fun RenderAdminWebDesktopA39(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    RfTheme(variant = RfThemeVariant.ADMIN) {
        val palette = LocalRfPalette.current
        val typo = LocalRfTypography.current
        val metrics = CanonicalSampleData.businessMetrics

        Row(
            modifier = modifier
                .width(RfDimens.WebFrameWidth)
                .height(RfDimens.WebFrameHeight)
                .clip(RoundedCornerShape(24.dp))
                .background(palette.bg)
                .border(1.5.dp, palette.line, RoundedCornerShape(24.dp))
                .testTag("screen-${spec.code}"),
        ) {
            // Columna 1: Sidebar de navegación administrativa (240 dp)
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .width(240.dp)
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
                            BasicText("RosaFiesta", style = typo.sectionTitle)
                            RfTag(text = "Admin Web · A39", tone = RfTagTone.INDIGO)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    AdminTab.entries.forEachIndexed { idx, tab ->
                        RfChip(
                            label = if (tab.badgeCount > 0) "${tab.label} (${tab.badgeCount})" else tab.label,
                            selected = idx == 0,
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                RfCard {
                    BasicText("María (Dueña)", style = typo.bodyStrong)
                    BasicText("maria@rosafiesta.do", style = typo.caption)
                }
            }

            // Columna 2: Panel central de KPIs, agenda y eventos activos
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
                        BasicText("Panel Ejecutivo · Octubre 2026", style = typo.heroTitle)
                        BasicText(spec.subtitle, style = typo.body.copy(color = palette.muted))
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
                        BasicText("Ingresos de octubre", style = typo.caption)
                        BasicText(RosaFiestaEngine.formatDop(metrics.octoberRevenueDop), style = typo.kpiPrice)
                        RfTag(text = "Meta superada", tone = RfTagTone.LIMA)
                    }
                    RfCard(modifier = Modifier.weight(1f)) {
                        BasicText("Saldos por cobrar", style = typo.caption)
                        BasicText(RosaFiestaEngine.formatDop(metrics.accountsReceivableDop), style = typo.kpiPrice)
                        RfTag(text = "Vence jue 22 oct: RD$ 3,150", tone = RfTagTone.SOL)
                    }
                    RfCard(modifier = Modifier.weight(1f)) {
                        BasicText("Eventos activos", style = typo.caption)
                        BasicText("${metrics.activeEventsCount} eventos", style = typo.kpiPrice)
                        RfTag(text = "${metrics.newRequestsCount} solicitudes nuevas", tone = RfTagTone.INDIGO)
                    }
                }

                RfCard {
                    BasicText("Eventos confirmados y en cotización", style = typo.sectionTitle)
                    Spacer(Modifier.height(10.dp))
                    CanonicalSampleData.events.forEach { evt ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText("${evt.code} · ${evt.title}", style = typo.cardTitle)
                                BasicText("${evt.clientName} · ${evt.dateLabel} · ${evt.venue} · ${evt.guests} invitados", style = typo.caption)
                            }
                            RfTag(text = "Líder: ${evt.leadStaffName}", tone = RfTagTone.TEAL)
                            Spacer(Modifier.width(12.dp))
                            BasicText(RosaFiestaEngine.formatDop(evt.totalDop), style = typo.cardTitle)
                        }
                    }
                }
            }

            // Columna 3: Copiloto Rosa IA Admin + Equipo (340 dp)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight()
                    .background(palette.surface)
                    .border(1.dp, palette.line)
                    .padding(20.dp),
            ) {
                RfHeroCard(useAdminGradient = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = true)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            BasicText("Rosa IA · Copiloto", style = typo.cardTitle.copy(color = Color.White))
                            BasicText("Índigo → Turquesa", style = typo.caption.copy(color = Color.White.copy(alpha = 0.85f)))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    BasicText(
                        "«El camión #01 con Pedro y Andrés ya reportó 66 % de avance en Jardín Botánico para la Boda Real.»",
                        style = typo.body.copy(color = Color.White),
                    )
                }

                RfCard {
                    BasicText("Equipo operativo (A28)", style = typo.cardTitle)
                    Spacer(Modifier.height(8.dp))
                    CanonicalSampleData.staffMembers.forEach { member ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            BasicText(member.name, style = typo.bodyStrong)
                            BasicText(member.roleLabel, style = typo.caption)
                        }
                    }
                }
            }
        }
    }
}
