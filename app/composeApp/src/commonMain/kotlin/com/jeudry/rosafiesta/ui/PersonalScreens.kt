package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfField
import com.jeudry.rosafiesta.designsystem.RfGradientPriceText
import com.jeudry.rosafiesta.designsystem.RfHeroCard
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfIconTile
import com.jeudry.rosafiesta.designsystem.RfIllo
import com.jeudry.rosafiesta.designsystem.RfIlloKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.domain.CanonicalSampleData

@Composable
internal fun RenderSplashAndOnboardingContent(spec: ScreenSpec, onPrimaryAction: () -> Unit) {
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
internal fun RenderAuthFormByCode(
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
internal fun RenderPersonalHomeContent(
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
internal fun RenderPersonalCatalogContent(
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
                RfQuantityStepperRow(
                    label = "Cantidad para tu evento: $qty u.",
                    onDecrement = { viewModel?.adjustDraftQuantity(art.id, -10) },
                    onIncrement = { viewModel?.adjustDraftQuantity(art.id, 10) },
                )
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
internal fun RenderPersonalQuoteAndPaymentContent(
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
internal fun RenderPersonalEventsContent(
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
                    RfKeyValueSummaryRow(
                        label = "${item.quantity} × ${item.name} (${item.articleCode})",
                        value = RosaFiestaEngine.formatDop(item.subtotalDop),
                    )
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
internal fun RenderRosaAssistantContent(
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
                RfKeyValueSummaryRow(
                    label = "${line.quantity} × ${line.name}",
                    value = RosaFiestaEngine.formatDop(line.subtotalDop),
                )
            }
        } else {
            canonicalBoda.items.forEach { line ->
                RfKeyValueSummaryRow(
                    label = "${line.quantity} × ${line.name}",
                    value = RosaFiestaEngine.formatDop(line.subtotalDop),
                )
            }
        }
    }

    RfButton(label = spec.primaryCta, onClick = onPrimaryAction)
}

@Composable
internal fun RenderPersonalProfileAndStateContent(
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
    val soundEnabled = viewModel?.soundEffectsEnabled?.collectAsState()?.value ?: true

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
                RfPreferenceToggleRow(
                    label = "Recordatorios de pago y montaje",
                    checked = pushEnabled,
                    onCheckedChange = { viewModel?.togglePushNotifications() },
                )
                Spacer(Modifier.height(8.dp))
                RfPreferenceToggleRow(
                    label = "Efectos de sonido y respuesta táctil (CC0)",
                    checked = soundEnabled,
                    onCheckedChange = { viewModel?.toggleSoundEffects() },
                )
                Spacer(Modifier.height(8.dp))
                RfPreferenceToggleRow(
                    label = "Modo oscuro (ver s15)",
                    checked = false,
                    onCheckedChange = { onSelectScreenCode("s15") },
                )
            }
        }

        "s87" -> {
            RfLegalClauseCard(
                badgeText = "Contrato y Depósito",
                tone = RfTagTone.ROSA,
                title = "1. Alquiler, Reserva y Cancelación",
                bullets = listOf(
                    "Reserva en firme con 30 % de anticipo y firma digital del contrato (s28).",
                    "Liquidación del 70 % restante antes del inicio del montaje en salón.",
                    "Primer cambio de fecha gratis con al menos 48 h de antelación (sujeto a stock).",
                    "Reembolso escalonado: 90 % (>30 días), 50 % (15–29 días), 0 % (<15 días).",
                    "Reposición de piezas dañadas o faltantes según tarifa vigente (A14).",
                ),
            )
            RfLegalClauseCard(
                badgeText = "Ley 172-13 RD",
                tone = RfTagTone.VIOLETA,
                title = "2. Privacidad y Datos Sensibles Censurados",
                bullets = listOf(
                    "Cumplimiento estricto de la Ley No. 172-13 de Protección de Datos Personales (RD).",
                    "Contraseñas cifradas con bcrypt y sesión resguardada en SecureStorage (AES-256-GCM / Keychain).",
                    "Censura automática de correos, teléfonos, tokens y referencias bancarias en logs.",
                    "Derecho ARCO y eliminación de cuenta (s88) garantizados sin eventos activos pendientes.",
                ),
            )
            RfLegalClauseCard(
                badgeText = "DGII · CC0 / OFL",
                tone = RfTagTone.LIMA,
                title = "3. Facturación Fiscal y Licencias Abiertas",
                bullets = listOf(
                    "Emisión de Comprobante Fiscal NCF (B01 Crédito Fiscal / B02 Consumo) con ITBIS 18 %.",
                    "Tipografías Bricolage Grotesque y Figtree bajo SIL Open Font License 1.1.",
                    "Efectos de sonido interactivos bajo licencia Creative Commons CC0 1.0 Public Domain.",
                ),
            )
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
