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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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

/**
 * Pantalla `A39` — Versión web de escritorio de la App Administrativa (1440×900 dp con barra lateral).
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
        val metrics = CanonicalSampleData.businessMetrics

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
                                BasicText("Panel Administrativo", style = typo.secondary)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        AdminTab.entries.forEachIndexed { idx, tab ->
                            val iconKind = when (tab) {
                                AdminTab.INICIO -> RfIconKind.HOME
                                AdminTab.EVENTOS -> RfIconKind.CALENDAR
                                AdminTab.ROSA -> RfIconKind.SPARKLE
                                AdminTab.INVENTARIO -> RfIconKind.ORDERS
                                AdminTab.MAS -> RfIconKind.MORE_VERTICAL
                            }
                            val isSelected = idx == 0
                            val bgModifier = if (isSelected) {
                                Modifier.background(palette.primaryGradient, RoundedCornerShape(14.dp))
                            } else {
                                Modifier.background(palette.surface, RoundedCornerShape(14.dp))
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .then(bgModifier)
                                    .clickable {
                                        when (tab) {
                                            AdminTab.INICIO -> onSelectScreenCode("A01")
                                            AdminTab.EVENTOS -> onSelectScreenCode("A11")
                                            AdminTab.ROSA -> onSelectScreenCode("A08")
                                            AdminTab.INVENTARIO -> onSelectScreenCode("A09")
                                            AdminTab.MAS -> onSelectScreenCode("A12")
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    RfIcon(
                                        kind = iconKind,
                                        tint = if (isSelected) Color.White else palette.ink,
                                        size = 18.dp,
                                    )
                                    BasicText(
                                        text = tab.label,
                                        style = typo.buttonLabel.copy(
                                            color = if (isSelected) Color.White else palette.ink,
                                            fontSize = 14.sp,
                                        ),
                                    )
                                }
                                if (tab.badgeCount > 0) {
                                    RfTag(
                                        text = "${tab.badgeCount}",
                                        tone = if (isSelected) RfTagTone.SOL else RfTagTone.INDIGO,
                                    )
                                }
                            }
                        }
                    }
                    RfCard {
                        BasicText("María (Dueña · Admin)", style = typo.bodyStrong)
                        BasicText("Pedro · Yuli · Andrés", style = typo.secondary)
                    }
                }

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
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Panel Administrativo", style = typo.rootTabTitle)
                            BasicText("Resumen operativo, eventos activos y disponibilidad · Octubre 2026", style = typo.secondary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (fillAvailable) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(RfColors.RosaBrand)
                                        .clickable { onSelectScreenCode("s14") }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                ) {
                                    BasicText(
                                        text = "🌸 Probar App Personal",
                                        style = typo.buttonLabel.copy(color = Color.White),
                                    )
                                }
                            }
                            RfButton(
                                label = spec.primaryCta,
                                onClick = onPrimaryAction,
                                fullWidth = false,
                            )
                        }
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
                                    BasicText("Rosa IA · Copiloto", style = typo.cardTitle)
                                    RfTag(text = "En línea", tone = RfTagTone.LIMA)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            BasicText(
                                "«María, el saldo de Boda Real Rosa & Dorado (RD$ 3,150) vence el jue 22 oct y el de Quinceañera Valentina Crystal (RD$ 4,340) el vie 30 oct.»",
                                style = typo.body.copy(fontSize = 13.sp),
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                RfTag(text = "Revisar cobros", tone = RfTagTone.INDIGO)
                                RfTag(text = "Stock 23 oct", tone = RfTagTone.TEAL)
                            }
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
