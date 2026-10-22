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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfField
import com.jeudry.rosafiesta.designsystem.RfHeroCard
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfTheme
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.domain.CanonicalSampleData

@Composable
internal fun RenderAdminDashboardAndCalendarContent(
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
internal fun RenderAdminQuotesContent(
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
                    RfKeyValueSummaryRow(
                        label = "${item.quantity} × ${item.name}",
                        value = RosaFiestaEngine.formatDop(item.subtotalDop),
                    )
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
internal fun RenderAdminLogisticsContent(
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
internal fun RenderAdminFinanceContent(
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
internal fun RenderAdminInventoryAndStaffContent(
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

private data class A39NavItem(
    val label: String,
    val icon: RfIconKind,
    val badge: String? = null,
    val isSelected: Boolean = false,
    val code: String,
)

/**
 * Pantalla `A39` — Versión web de escritorio de la App Administrativa (1440×900 dp con barra lateral).
 * Fiel 1:1 a `docs/design/RosaFiesta-diseno/html/a39-admin-web-inicio.html` y `pantallas/a39-admin-web-inicio.png`.
 */
@Composable
public fun RenderAdminWebDesktopA39(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit = {},
    onSelectScreenCode: (String) -> Unit = {},
    fillAvailable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    RfTheme(variant = RfThemeVariant.ADMIN) {
        val palette = LocalRfPalette.current
        val typo = LocalRfTypography.current

        val frameModifier = if (fillAvailable) {
            modifier.fillMaxSize().testTag("screen-${spec.code}")
        } else {
            modifier
                .width(RfDimens.WebFrameWidth)
                .height(RfDimens.WebFrameHeight)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, palette.line, RoundedCornerShape(24.dp))
                .testTag("screen-${spec.code}")
        }

        RfRadialBackgroundBox(
            modifier = frameModifier,
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // ==========================================
                // 1. BARRA LATERAL (aside · 256 px)
                // ==========================================
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .width(256.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                        .border(width = 1.dp, color = Color(0xFFECECF5))
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Header del Sidebar: Logo + "Rosa Fiesta" + Tag "Admin"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 6.dp, end = 6.dp, bottom = 16.dp),
                        ) {
                            RfLogo(size = RfLogoSize.MEDIUM)
                            Column {
                                BasicText(
                                    text = "Rosa Fiesta",
                                    style = typo.backScreenTitle.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF151729),
                                    ),
                                )
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFFECEBFF))
                                        .padding(horizontal = 7.dp),
                                ) {
                                    BasicText(
                                        text = "Admin",
                                        style = typo.tagLabel.copy(
                                            color = Color(0xFF4B47D6),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }
                            }
                        }

                        // Navegación del Sidebar (9 enlaces)
                        val navItems = listOf(
                            A39NavItem("Inicio", RfIconKind.HOME, isSelected = true, code = "A01"),
                            A39NavItem("Solicitudes", RfIconKind.ORDERS, badge = "2", code = "A02"),
                            A39NavItem("Eventos", RfIconKind.CALENDAR, code = "A11"),
                            A39NavItem("Inventario", RfIconKind.CATALOG, code = "A09"),
                            A39NavItem("Cotizaciones", RfIconKind.RECEIPT, code = "A22"),
                            A39NavItem("Pagos", RfIconKind.WALLET, code = "A20"),
                            A39NavItem("Clientes", RfIconKind.USER, code = "A26"),
                            A39NavItem("Chats", RfIconKind.CHAT, badge = "2", code = "A14"),
                            A39NavItem("Reportes", RfIconKind.ORDERS, code = "s68"),
                        )

                        navItems.forEach { item ->
                            val bgModifier = if (item.isSelected) {
                                Modifier.background(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                )
                            } else {
                                Modifier
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(bgModifier)
                                    .clickable { onSelectScreenCode(item.code) }
                                    .padding(horizontal = 12.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    RfIcon(
                                        kind = item.icon,
                                        tint = if (item.isSelected) Color.White else Color(0xFF3A3D52),
                                        size = 19.dp,
                                    )
                                    BasicText(
                                        text = item.label,
                                        style = typo.body.copy(
                                            fontSize = 14.5.sp,
                                            fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (item.isSelected) Color.White else Color(0xFF3A3D52),
                                        ),
                                    )
                                }
                                if (item.badge != null) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .height(22.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color(0xFFFFD45C))
                                            .padding(horizontal = 7.dp),
                                    ) {
                                        BasicText(
                                            text = item.badge,
                                            style = typo.tagLabel.copy(
                                                color = Color(0xFF2E1A47),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom del Sidebar: Ajustes y Perfil de María
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectScreenCode("A31") }
                                .padding(horizontal = 12.dp),
                        ) {
                            RfIcon(
                                kind = RfIconKind.SETTINGS,
                                tint = Color(0xFF3A3D52),
                                size = 19.dp,
                            )
                            BasicText(
                                text = "Ajustes",
                                style = typo.body.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF3A3D52),
                                ),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFECECF5)),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECEBFF)),
                            ) {
                                BasicText(
                                    text = "M",
                                    style = typo.bodyStrong.copy(
                                        color = Color(0xFF4B47D6),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                    ),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = "María",
                                    style = typo.bodyStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                )
                                BasicText(
                                    text = "admin@rosafiesta.com",
                                    style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 2. CONTENIDO PRINCIPAL (main)
                // ==========================================
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                ) {
                    // Header de la pantalla
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "Martes 20 de octubre",
                                style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF5F6377)),
                            )
                            Spacer(Modifier.height(2.dp))
                            BasicText(
                                text = "Hola, María",
                                style = typo.rootTabTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold),
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Buscador con ⌘K
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .width(340.dp)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    RfIcon(
                                        kind = RfIconKind.SEARCH,
                                        tint = Color(0xFF5F6377),
                                        size = 18.dp,
                                    )
                                    BasicText(
                                        text = "Buscar eventos, clientes, artículos",
                                        style = typo.body.copy(fontSize = 14.sp, color = Color(0xFF5F6377)),
                                        maxLines = 1,
                                    )
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF2F2F8))
                                        .padding(horizontal = 7.dp, vertical = 3.dp),
                                ) {
                                    BasicText(
                                        text = "⌘K",
                                        style = typo.caption.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF5F6377),
                                        ),
                                    )
                                }
                            }

                            // Campana con badge 3
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                                    .clickable { onSelectScreenCode("A29") },
                            ) {
                                RfIcon(kind = RfIconKind.BELL, tint = Color(0xFF151729), size = 20.dp)
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD45C)),
                                ) {
                                    BasicText(
                                        text = "3",
                                        style = typo.tagLabel.copy(
                                            color = Color(0xFF2E1A47),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                        ),
                                    )
                                }
                            }

                            // Botón "Crear con Rosa"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                                    .clickable { onSelectScreenCode("A08") }
                                    .padding(horizontal = 16.dp),
                            ) {
                                RosaAvatar(size = RosaAvatarSize.MINI, forceAdminPalette = true)
                                BasicText(
                                    text = "Crear con Rosa",
                                    style = typo.buttonLabel.copy(
                                        color = Color(0xFF151729),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }

                            // Botón "Nuevo evento"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                                        ),
                                    )
                                    .clickable { onSelectScreenCode("A04") }
                                    .padding(horizontal = 18.dp),
                            ) {
                                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 18.dp)
                                BasicText(
                                    text = "Nuevo evento",
                                    style = typo.buttonLabel.copy(
                                        color = Color.White,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }

                            if (fillAvailable) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(RfColors.RosaBrand)
                                        .clickable { onSelectScreenCode("s14") }
                                        .padding(horizontal = 14.dp),
                                ) {
                                    BasicText(
                                        text = "🌸 App Personal",
                                        style = typo.buttonLabel.copy(color = Color.White, fontSize = 13.5.sp),
                                    )
                                }
                            }
                        }
                    }

                    // 4 Stat Cards
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AdminStatCard(
                            label = "Ingresos de octubre",
                            value = "RD$ 12,450",
                            subtext = "▲ 22% vs. septiembre",
                            subtextColor = Color(0xFF4E6A10),
                            iconKind = RfIconKind.ORDERS,
                            tileBg = Color(0xFFEEF6D6),
                            tileTint = Color(0xFF4E6A10),
                            modifier = Modifier.weight(1f),
                        )
                        AdminStatCard(
                            label = "Por cobrar",
                            value = "RD$ 7,724",
                            subtext = "3 pagos · 1 vence el jueves",
                            subtextColor = Color(0xFF5F6377),
                            iconKind = RfIconKind.WALLET,
                            tileBg = Color(0xFFFFF2C9),
                            tileTint = Color(0xFF7A5700),
                            modifier = Modifier.weight(1f),
                        )
                        AdminStatCard(
                            label = "Eventos activos",
                            value = "4",
                            subtext = "Próximo: vie 23 oct",
                            subtextColor = Color(0xFF5F6377),
                            iconKind = RfIconKind.CALENDAR,
                            tileBg = Color(0xFFECEBFF),
                            tileTint = Color(0xFF4B47D6),
                            modifier = Modifier.weight(1f),
                        )
                        AdminStatCard(
                            label = "Solicitudes nuevas",
                            value = "2",
                            subtext = "Est. RD$ 2,545 en total",
                            subtextColor = Color(0xFF5F6377),
                            iconKind = RfIconKind.ORDERS,
                            tileBg = Color(0xFFFDE6F2),
                            tileTint = Color(0xFFC2338F),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Sección Inferior (2 Columnas)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        // Columna Izquierda: Tabla y Gráfica
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            AdminUpcomingEventsTable(
                                onSelectScreenCode = onSelectScreenCode,
                                modifier = Modifier.weight(1.3f),
                            )

                            AdminMonthlyRevenueChart(
                                modifier = Modifier.weight(1.1f),
                            )
                        }

                        // Columna Derecha (360 px fija): Hero Card, Solicitudes, Atención hoy
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .width(360.dp)
                                .fillMaxHeight(),
                        ) {
                            AdminHeroEventBanner(
                                onSelectScreenCode = onSelectScreenCode,
                            )

                            AdminNewRequestsCard(
                                onSelectScreenCode = onSelectScreenCode,
                            )

                            AdminAttentionTodayCard(
                                onSelectScreenCode = onSelectScreenCode,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    label: String,
    value: String,
    subtext: String,
    subtextColor: Color,
    iconKind: RfIconKind,
    tileBg: Color,
    tileTint: Color,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = label,
                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tileBg),
            ) {
                RfIcon(kind = iconKind, tint = tileTint, size = 18.dp)
            }
        }
        BasicText(
            text = value,
            style = typo.webKpi.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
        )
        BasicText(
            text = subtext,
            style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = subtextColor),
        )
    }
}

@Composable
private fun AdminUpcomingEventsTable(
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
    ) {
        // Encabezado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            BasicText(
                text = "Próximos eventos",
                style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            )
            BasicText(
                text = "Ver todos",
                style = typo.buttonLabel.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B47D6),
                ),
                modifier = Modifier.clickable { onSelectScreenCode("A11") },
            )
        }

        // Fila de encabezado de tabla
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8FC))
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            BasicText("EVENTO", modifier = Modifier.weight(3.0f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("FECHA", modifier = Modifier.weight(0.85f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("CLIENTE", modifier = Modifier.weight(0.95f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("TOTAL", modifier = Modifier.weight(0.85f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("PAGADO", modifier = Modifier.weight(1.1f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("ESTADO", modifier = Modifier.weight(1.25f), style = typo.sectionCap.copy(fontSize = 11.sp))
        }

        // 4 Filas de eventos
        AdminEventRow(
            photoKey = RfPhotoKey.BODA,
            title = "Boda Real Rosa & Dorado",
            date = "Vie 23 oct",
            client = "Camila R.",
            total = "RD$ 4,500",
            percent = 30,
            statusLabel = "Ajustado",
            statusTone = RfTagTone.TEAL,
            onClick = { onSelectScreenCode("A13") },
        )
        AdminEventRow(
            photoKey = RfPhotoKey.QUINCEANERA,
            title = "Quinceañera Valentina Crystal",
            date = "Lun 2 nov",
            client = "Camila R.",
            total = "RD$ 6,200",
            percent = 30,
            statusLabel = "Confirmado",
            statusTone = RfTagTone.LIMA,
            onClick = { onSelectScreenCode("A13") },
        )
        AdminEventRow(
            photoKey = RfPhotoKey.CUMPLE_INFANTIL,
            title = "Cumpleaños de Mateo",
            date = "Sáb 7 nov",
            client = "Laura Peña",
            total = "RD$ 780",
            percent = 0,
            statusLabel = "Cotización enviada",
            statusTone = RfTagTone.SOL,
            onClick = { onSelectScreenCode("A13") },
        )
        AdminEventRow(
            photoKey = RfPhotoKey.BABY_SHOWER,
            title = "Baby Shower Sofía en Rosas",
            date = "Lun 9 nov",
            client = "Camila R.",
            total = "Por cotizar",
            percent = 0,
            statusLabel = "Borrador",
            statusTone = RfTagTone.INDIGO,
            onClick = { onSelectScreenCode("A13") },
        )
    }
}

@Composable
private fun AdminEventRow(
    photoKey: RfPhotoKey,
    title: String,
    date: String,
    client: String,
    total: String,
    percent: Int,
    statusLabel: String,
    statusTone: RfTagTone,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(3.0f),
        ) {
            RfPhoto(
                photoKey = photoKey,
                contentDescription = title,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            BasicText(
                text = title,
                style = typo.bodyStrong.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BasicText(date, modifier = Modifier.weight(0.85f), style = typo.body.copy(fontSize = 14.sp))
        BasicText(client, modifier = Modifier.weight(0.95f), style = typo.body.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicText(
            text = total,
            modifier = Modifier.weight(0.85f),
            style = typo.cardTitle.copy(
                fontSize = 15.sp,
                fontWeight = if (total.startsWith("RD$")) FontWeight.Bold else FontWeight.Normal,
                color = if (total.startsWith("RD$")) Color(0xFF151729) else Color(0xFF5F6377),
            ),
        )
        // Barra de progreso de pago
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1.1f),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEDEDF5)),
            ) {
                if (percent > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                                ),
                            ),
                    )
                }
            }
            BasicText(
                text = "$percent%",
                style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.width(30.dp),
            )
        }
        Box(modifier = Modifier.weight(1.25f)) {
            RfTag(text = statusLabel, tone = statusTone)
        }
    }
}

@Composable
private fun AdminMonthlyRevenueChart(
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Ingresos por mes",
                style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            )
            // Segmented pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F2F8))
                    .padding(3.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "6 meses",
                        style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "12 meses",
                        style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Gráfica de 6 barras
        val bars = listOf(
            Triple("may", "6.2k", 66.dp),
            Triple("jun", "9.8k", 105.dp),
            Triple("jul", "8.1k", 87.dp),
            Triple("ago", "11.3k", 121.dp),
            Triple("sep", "10.2k", 109.dp),
            Triple("oct", "12.45k", 133.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            bars.forEachIndexed { idx, (month, amount, barHeight) ->
                val isOct = idx == bars.lastIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    BasicText(
                        text = amount,
                        style = typo.caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOct) Color(0xFF151729) else Color(0xFF5F6377),
                        ),
                    )
                    val barBrush = if (isOct) {
                        Brush.linearGradient(
                            listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
                        )
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFE3E4F5), Color(0xFFE3E4F5)))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(barBrush),
                    )
                    BasicText(
                        text = month,
                        style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHeroEventBanner(
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        BasicText(
            text = "Este viernes · en 3 días",
            style = typo.caption.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp),
        )
        BasicText(
            text = "Boda Real Rosa & Dorado",
            style = typo.cardTitle.copy(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
        )
        BasicText(
            text = "Montaje 2:00 p. m. · Jardín Botánico · 120 invitados",
            style = typo.body.copy(color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp),
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .clickable { onSelectScreenCode("A13") }
                    .padding(horizontal = 14.dp),
            ) {
                BasicText(
                    text = "Ver evento",
                    style = typo.buttonLabel.copy(color = Color(0xFF4B47D6), fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onSelectScreenCode("A16") }
                    .padding(horizontal = 14.dp),
            ) {
                BasicText(
                    text = "Modo ejecución",
                    style = typo.buttonLabel.copy(color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun AdminNewRequestsCard(
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            BasicText(
                text = "Solicitudes nuevas",
                style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFD45C))
                    .padding(horizontal = 9.dp),
            ) {
                BasicText(
                    text = "2",
                    style = typo.tagLabel.copy(color = Color(0xFF2E1A47), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold),
                )
            }
        }

        // Fila 1: Cumpleaños de Mateo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            RfPhoto(
                photoKey = RfPhotoKey.CUMPLE_INFANTIL,
                contentDescription = "Cumpleaños de Mateo",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Cumpleaños de Mateo",
                    style = typo.bodyStrong.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                BasicText(
                    text = "Laura Peña · Sáb 7 nov",
                    style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                        ),
                    )
                    .clickable { onSelectScreenCode("A03") }
                    .padding(horizontal = 12.dp),
            ) {
                BasicText(
                    text = "Cotizar",
                    style = typo.buttonLabel.copy(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                )
            }
        }

        // Fila 2: Graduación Promo 2026
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            RfPhoto(
                photoKey = RfPhotoKey.GRADUACION,
                contentDescription = "Graduación Promo 2026",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Graduación Promo 2026",
                    style = typo.bodyStrong.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                BasicText(
                    text = "Ana Gómez · Vie 13 nov",
                    style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                        ),
                    )
                    .clickable { onSelectScreenCode("A03") }
                    .padding(horizontal = 12.dp),
            ) {
                BasicText(
                    text = "Cotizar",
                    style = typo.buttonLabel.copy(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun AdminAttentionTodayCard(
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
    ) {
        BasicText(
            text = "Atención hoy",
            style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        )

        // Fila 1: Saldo Boda Real
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
                .clickable { onSelectScreenCode("A20") }
                .padding(horizontal = 18.dp, vertical = 11.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFCE3E8)),
            ) {
                RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFFB42345), size = 17.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Saldo Boda Real",
                    style = typo.bodyStrong.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                BasicText(
                    text = "Vence el jue 22 oct",
                    style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                )
            }
            BasicText(
                text = "RD$ 3,150",
                style = typo.cardTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
            )
        }

        // Fila 2: Mesa dulce temática
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
                .clickable { onSelectScreenCode("A09") }
                .padding(horizontal = 18.dp, vertical = 11.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF2C9)),
            ) {
                RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF7A5700), size = 17.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Mesa dulce temática",
                    style = typo.bodyStrong.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                BasicText(
                    text = "Quedan 2 · stock bajo",
                    style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFF2C9))
                    .padding(horizontal = 9.dp),
            ) {
                BasicText(
                    text = "Stock bajo",
                    style = typo.tagLabel.copy(color = Color(0xFF7A5700), fontSize = 12.sp, fontWeight = FontWeight.Bold),
                )
            }
        }

        // Fila 3: Cancelaciones
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
                .clickable { onSelectScreenCode("A32") }
                .padding(horizontal = 18.dp, vertical = 11.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFECEBFF)),
            ) {
                RfIcon(kind = RfIconKind.CLOCK, tint = Color(0xFF4B47D6), size = 17.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "2 cancelaciones por revisar",
                    style = typo.bodyStrong.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                BasicText(
                    text = "Bautizo de Lucas · Cumpleaños...",
                    style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicText(
                text = "›",
                style = typo.cardTitle.copy(color = Color(0xFF5F6377), fontSize = 20.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}
