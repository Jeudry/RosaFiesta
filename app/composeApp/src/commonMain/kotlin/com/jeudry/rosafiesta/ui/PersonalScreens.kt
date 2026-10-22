package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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
import com.jeudry.rosafiesta.designsystem.RfIcon
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

    if (spec.code in setOf("s14", "s15")) {
        RenderPersonalHomeScreen(spec = spec, onSelectScreenCode = onSelectScreenCode)
        return
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
private fun HeroBalloonArchIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        drawOval(
            color = Color(0x142B1730),
            topLeft = Offset(10f * s, 106.5f * s),
            size = Size(100f * s, 7f * s),
        )
        drawCircle(Color(0xFFE2EFA6), radius = 6.5f * s, center = Offset(9f * s, 91f * s))
        drawCircle(Color(0xFFFFDF8A), radius = 6.5f * s, center = Offset(26.6f * s, 60f * s))
        drawCircle(Color(0xFFF9B6D3), radius = 6.5f * s, center = Offset(60f * s, 47f * s))
        drawCircle(Color(0xFFE2EFA6), radius = 6.5f * s, center = Offset(93.4f * s, 60f * s))
        drawCircle(Color(0xFFC9B6F7), radius = 6.5f * s, center = Offset(111f * s, 91f * s))
        val balloons = listOf(
            Offset(18f, 104f) to Color(0xFFEA4EA9),
            Offset(20.5f, 86f) to Color(0xFFB9D84A),
            Offset(28f, 72f) to Color(0xFF865DEC),
            Offset(39f, 62.5f) to Color(0xFFFBCA3F),
            Offset(52.7f, 58f) to Color(0xFFEA4EA9),
            Offset(67.3f, 58f) to Color(0xFFB9D84A),
            Offset(81f, 62.5f) to Color(0xFF865DEC),
            Offset(92f, 72f) to Color(0xFFFBCA3F),
            Offset(99.5f, 86f) to Color(0xFFEA4EA9),
            Offset(102f, 104f) to Color(0xFFB9D84A),
        )
        balloons.forEach { (pos, color) ->
            drawCircle(color, radius = 10.5f * s, center = Offset(pos.x * s, pos.y * s))
        }
        val highlights = listOf(
            Offset(15f, 100f),
            Offset(36f, 58.5f),
            Offset(64f, 54f),
            Offset(89f, 68f),
            Offset(99f, 100f),
        )
        highlights.forEach { pos ->
            drawOval(
                color = Color(0x8CFFFFFF),
                topLeft = Offset((pos.x - 2.4f) * s, (pos.y - 3.4f) * s),
                size = Size(4.8f * s, 6.8f * s),
            )
        }
        drawCircle(Color(0xFFFFD45C), radius = 3.5f * s, center = Offset(60f * s, 26f * s))
    }
}

@Composable
private fun ServiceIllustrationFloral(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        drawOval(Color(0x142B1730), topLeft = Offset(38f * s, 105f * s), size = Size(44f * s, 6f * s))
        val leaf1 = Path().apply {
            moveTo(60f * s, 74f * s)
            cubicTo(40f * s, 72f * s, 28f * s, 58f * s, 32f * s, 46f * s)
            cubicTo(46f * s, 50f * s, 56f * s, 60f * s, 60f * s, 74f * s)
            close()
        }
        drawPath(leaf1, Color(0xFF2FB58F))
        val leaf2 = Path().apply {
            moveTo(60f * s, 74f * s)
            cubicTo(80f * s, 72f * s, 93f * s, 60f * s, 89f * s, 48f * s)
            cubicTo(76f * s, 52f * s, 64f * s, 61f * s, 60f * s, 74f * s)
            close()
        }
        drawPath(leaf2, Color(0xFF26A07D))
        drawLine(Color(0xFF1F8C6B), Offset(60f * s, 74f * s), Offset(48f * s, 44f * s), 2.4f * s, StrokeCap.Round)
        drawLine(Color(0xFF1F8C6B), Offset(60f * s, 74f * s), Offset(60f * s, 36f * s), 2.4f * s, StrokeCap.Round)
        drawLine(Color(0xFF1F8C6B), Offset(60f * s, 74f * s), Offset(75f * s, 46f * s), 2.4f * s, StrokeCap.Round)
        drawCircle(Color(0xFFF9B6D3), radius = 10f * s, center = Offset(47f * s, 42f * s))
        drawCircle(Color(0xFFFFD98A), radius = 10f * s, center = Offset(76f * s, 44f * s))
        drawCircle(Color(0xFFEC4F97), radius = 12.5f * s, center = Offset(61f * s, 31f * s))
        val vase = Path().apply {
            moveTo(46f * s, 72f * s)
            lineTo(74f * s, 72f * s)
            lineTo(70f * s, 104f * s)
            quadraticTo(60f * s, 108f * s, 50f * s, 104f * s)
            close()
        }
        drawPath(vase, Color(0xFFB9A6F7))
    }
}

@Composable
private fun ServiceIllustrationMobiliario(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        drawOval(Color(0x142B1730), topLeft = Offset(30f * s, 104.5f * s), size = Size(60f * s, 7f * s))
        val gold = Color(0xFFD9A441)
        drawLine(gold, Offset(45f * s, 66f * s), Offset(41f * s, 106f * s), 3.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(75f * s, 66f * s), Offset(79f * s, 106f * s), 3.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(49f * s, 70f * s), Offset(47f * s, 104f * s), 2.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(71f * s, 70f * s), Offset(73f * s, 104f * s), 2.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(43f * s, 92f * s), Offset(77f * s, 92f * s), 2.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(46f * s, 64f * s), Offset(46f * s, 18f * s), 3.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(74f * s, 64f * s), Offset(74f * s, 18f * s), 3.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(46f * s, 18f * s), Offset(74f * s, 18f * s), 3.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(46f * s, 30f * s), Offset(74f * s, 30f * s), 3.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(46f * s, 50f * s), Offset(74f * s, 50f * s), 3.4f * s, StrokeCap.Round)
        drawLine(gold, Offset(54f * s, 30f * s), Offset(54f * s, 50f * s), 2f * s, StrokeCap.Round)
        drawLine(gold, Offset(60f * s, 30f * s), Offset(60f * s, 50f * s), 2f * s, StrokeCap.Round)
        drawLine(gold, Offset(66f * s, 30f * s), Offset(66f * s, 50f * s), 2f * s, StrokeCap.Round)
        drawRoundRect(Color(0xFFF9B6D3), topLeft = Offset(38f * s, 59f * s), size = Size(44f * s, 10f * s), cornerRadius = CornerRadius(5f * s))
        drawRoundRect(Color(0x99FFFFFF), topLeft = Offset(42f * s, 60f * s), size = Size(36f * s, 3f * s), cornerRadius = CornerRadius(1.5f * s))
    }
}

@Composable
private fun ServiceIllustrationDulce(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        drawOval(Color(0x142B1730), topLeft = Offset(14f * s, 106.5f * s), size = Size(92f * s, 7f * s))
        drawRect(Color(0xFFF9B6D3), topLeft = Offset(16f * s, 74f * s), size = Size(88f * s, 34f * s))
        val skirtLine = Color(0x4DEC4F97)
        for (x in listOf(30f, 44f, 58f, 72f, 86f)) {
            drawLine(skirtLine, Offset(x * s, 80f * s), Offset(x * s, 108f * s), 2f * s)
        }
        drawRect(Color(0xFFEC4F97), topLeft = Offset(14f * s, 72f * s), size = Size(92f * s, 8f * s))
        drawRoundRect(Color(0xFFFFF6EE), topLeft = Offset(42f * s, 45f * s), size = Size(36f * s, 21f * s), cornerRadius = CornerRadius(4f * s))
        drawRoundRect(Color(0xFFFFF6EE), topLeft = Offset(48f * s, 29f * s), size = Size(24f * s, 17f * s), cornerRadius = CornerRadius(4f * s))
        drawRoundRect(Color(0xFFF9B6D3), topLeft = Offset(48f * s, 29f * s), size = Size(24f * s, 5f * s), cornerRadius = CornerRadius(2.5f * s))
        drawRoundRect(Color(0xFF4AA8E0), topLeft = Offset(58.5f * s, 19f * s), size = Size(3f * s, 10f * s), cornerRadius = CornerRadius(1.5f * s))
        drawCircle(Color(0xFFF5B53B), radius = 3.5f * s, center = Offset(60f * s, 15f * s))
        drawCircle(Color(0xFFFFF6EE), radius = 6.5f * s, center = Offset(31f * s, 60f * s))
        drawCircle(Color(0xFFEC4F97), radius = 2.2f * s, center = Offset(31f * s, 54f * s))
        drawCircle(Color(0xFFFFF6EE), radius = 6.5f * s, center = Offset(89f * s, 60f * s))
        drawCircle(Color(0xFFEC4F97), radius = 2.2f * s, center = Offset(89f * s, 54f * s))
    }
}

@Composable
private fun ServiceIllustrationLuz(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        val wire1 = Path().apply {
            moveTo(2f * s, 30f * s)
            cubicTo(20f * s, 52f * s, 40f * s, 52f * s, 60f * s, 36f * s)
            cubicTo(80f * s, 20f * s, 98f * s, 24f * s, 118f * s, 40f * s)
        }
        drawPath(wire1, Color(0x732B1730), style = Stroke(width = 1.5f * s))
        val wire2 = Path().apply {
            moveTo(2f * s, 72f * s)
            cubicTo(22f * s, 94f * s, 44f * s, 94f * s, 64f * s, 78f * s)
            cubicTo(84f * s, 62f * s, 100f * s, 66f * s, 118f * s, 82f * s)
        }
        drawPath(wire2, Color(0x732B1730), style = Stroke(width = 1.5f * s))
        val bulbs = listOf(
            Offset(14f, 52f) to Color(0xFFF5B53B),
            Offset(32f, 57f) to Color(0xFFEC4F97),
            Offset(50f, 51f) to Color(0xFF2FB58F),
            Offset(70f, 42f) to Color(0xFF7650E8),
            Offset(90f, 41f) to Color(0xFF4AA8E0),
            Offset(108f, 48f) to Color(0xFFF5B53B),
            Offset(22f, 97f) to Color(0xFF7650E8),
            Offset(42f, 100f) to Color(0xFFF5B53B),
            Offset(62f, 92f) to Color(0xFFEC4F97),
            Offset(84f, 84f) to Color(0xFF2FB58F),
            Offset(104f, 89f) to Color(0xFF4AA8E0),
        )
        bulbs.forEach { (pos, col) ->
            drawCircle(col.copy(alpha = 0.22f), radius = 10f * s, center = Offset(pos.x * s, pos.y * s))
            drawOval(col, topLeft = Offset((pos.x - 5f) * s, (pos.y - 7f) * s), size = Size(10f * s, 14f * s))
        }
    }
}

@Composable
internal fun RenderPersonalHomeScreen(
    spec: ScreenSpec,
    onSelectScreenCode: (String) -> Unit,
) {
    val isDark = spec.variant == RfThemeVariant.PERSONAL_DARK
    val typo = LocalRfTypography.current
    val textColor = if (isDark) Color(0xFFF2F0F8) else Color(0xFF1A1A2E)
    val mutedColor = if (isDark) Color(0xFFA6A3BA) else Color(0xFF62627A)
    val cardBg = if (isDark) Color(0xFF1D1D30) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2A2A40) else Color(0xFFF0EBF4)
    val chipBg = if (isDark) Color(0x2EB54BD6) else Color(0xFFF5EAF9)
    val chipText = Color(0xFFB54BD6)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Profile Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                    .padding(2.dp)
                    .clickable { onSelectScreenCode("s46") },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(cardBg),
                ) {
                    BasicText(
                        text = "C",
                        style = typo.rootTabTitle.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC84FB8),
                        ),
                    )
                }
            }

            // User Greeting
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Hola de nuevo",
                    style = typo.caption.copy(fontSize = 13.sp, color = mutedColor),
                )
                BasicText(
                    text = "Camila",
                    style = typo.rootTabTitle.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                    ),
                    maxLines = 1,
                )
            }

            // Notifications Bell
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(1.dp, cardBorder, CircleShape)
                    .clickable { onSelectScreenCode("s79") },
            ) {
                RfIcon(kind = RfIconKind.BELL, tint = textColor, size = 22.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 9.dp, end = 10.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFBCA3F))
                        .border(2.dp, cardBg, CircleShape),
                )
            }

            // Points Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                    .clickable { onSelectScreenCode("s37") }
                    .padding(start = 12.dp, end = 8.dp),
            ) {
                RfIcon(kind = RfIconKind.SPARK, tint = Color.White, size = 20.dp)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(24.dp)
                        .widthIn(min = 30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 7.dp),
                ) {
                    BasicText(
                        text = "160",
                        style = typo.caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E1A47),
                        ),
                    )
                }
            }
        }

        // Search Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                .clickable { onSelectScreenCode("s17") }
                .padding(start = 16.dp, end = 6.dp),
        ) {
            RfIcon(kind = RfIconKind.SEARCH, tint = mutedColor, size = 20.dp)
            BasicText(
                text = "¿Qué celebramos hoy?",
                style = typo.body.copy(fontSize = 15.sp, color = mutedColor),
                modifier = Modifier.weight(1f),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(chipBg),
            ) {
                RfIcon(kind = RfIconKind.MIC, tint = chipText, size = 20.dp)
            }
        }

        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(186.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
        ) {
            // Balloon arch decoration
            HeroBalloonArchIllustration(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 4.dp),
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 10.dp),
                ) {
                    BasicText(
                        text = "NUEVA TEMPORADA 2026",
                        style = typo.tagLabel.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E1A47),
                            letterSpacing = 0.5.sp,
                        ),
                    )
                }

                BasicText(
                    text = "Crea momentos inolvidables",
                    style = typo.rootTabTitle.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 26.sp,
                    ),
                    modifier = Modifier.widthIn(max = 190.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s21") }
                        .padding(horizontal = 18.dp),
                ) {
                    BasicText(
                        text = "Explorar paquetes",
                        style = typo.buttonLabel.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA23FB8),
                        ),
                    )
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val stroke = Stroke(width = size.width * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        drawLine(Color(0xFFA23FB8), Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), stroke.width, StrokeCap.Round)
                        val arrow = Path().apply {
                            moveTo(size.width * 0.55f, size.height * 0.25f)
                            lineTo(size.width * 0.8f, size.height * 0.5f)
                            lineTo(size.width * 0.55f, size.height * 0.75f)
                        }
                        drawPath(arrow, Color(0xFFA23FB8), style = stroke)
                    }
                }
            }
        }

        // Servicios Section
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = "Servicios",
                    style = typo.rootTabTitle.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                    ),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable { onSelectScreenCode("s18") }
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "Ver todo",
                        style = typo.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipText,
                        ),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Decoración
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).clickable { onSelectScreenCode("s18") },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDE6F2)),
                    ) {
                        ServiceIllustrationFloral(modifier = Modifier.size(56.dp))
                    }
                    BasicText(
                        text = "Decoración",
                        style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor),
                        maxLines = 1,
                    )
                }

                // Mobiliario
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).clickable { onSelectScreenCode("s18") },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFE8FD)),
                    ) {
                        ServiceIllustrationMobiliario(modifier = Modifier.size(56.dp))
                    }
                    BasicText(
                        text = "Mobiliario",
                        style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor),
                        maxLines = 1,
                    )
                }

                // Mesa dulce
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).clickable { onSelectScreenCode("s18") },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF6D6)),
                    ) {
                        ServiceIllustrationDulce(modifier = Modifier.size(56.dp))
                    }
                    BasicText(
                        text = "Mesa dulce",
                        style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor),
                        maxLines = 1,
                    )
                }

                // Iluminación
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).clickable { onSelectScreenCode("s18") },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF2C9)),
                    ) {
                        ServiceIllustrationLuz(modifier = Modifier.size(56.dp))
                    }
                    BasicText(
                        text = "Iluminación",
                        style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor),
                        maxLines = 1,
                    )
                }
            }
        }

        // Tendencias Section
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = "Tendencias",
                    style = typo.rootTabTitle.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                    ),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable { onSelectScreenCode("s21") }
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "Ver todo",
                        style = typo.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipText,
                        ),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                // Card 1: Graduaciones
                Column(
                    modifier = Modifier
                        .width(230.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("s21") },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    ) {
                        RfPhoto(
                            photoKey = RfPhotoKey.GRADUACION,
                            contentDescription = "Graduaciones",
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 9.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Canvas(modifier = Modifier.size(14.dp)) {
                                    val stroke = Stroke(width = size.width * 0.13f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    val path = Path().apply {
                                        moveTo(size.width * 0.15f, size.height * 0.75f)
                                        lineTo(size.width * 0.42f, size.height * 0.48f)
                                        lineTo(size.width * 0.60f, size.height * 0.66f)
                                        lineTo(size.width * 0.90f, size.height * 0.30f)
                                    }
                                    drawPath(path, Color(0xFF2E1A47), style = stroke)
                                    drawLine(Color(0xFF2E1A47), Offset(size.width * 0.65f, size.height * 0.30f), Offset(size.width * 0.90f, size.height * 0.30f), stroke.width, StrokeCap.Round)
                                    drawLine(Color(0xFF2E1A47), Offset(size.width * 0.90f, size.height * 0.30f), Offset(size.width * 0.90f, size.height * 0.55f), stroke.width, StrokeCap.Round)
                                }
                                BasicText(
                                    text = "Tendencia",
                                    style = typo.caption.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E1A47),
                                    ),
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                    ) {
                        BasicText(
                            text = "Graduaciones",
                            style = typo.rootTabTitle.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                            ),
                        )
                        BasicText(
                            text = "2.4K eventos",
                            style = typo.caption.copy(fontSize = 13.sp, color = mutedColor),
                        )
                    }
                }

                // Card 2: Bodas
                Column(
                    modifier = Modifier
                        .width(230.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("s21") },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    ) {
                        RfPhoto(
                            photoKey = RfPhotoKey.BODA,
                            contentDescription = "Bodas",
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 9.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Canvas(modifier = Modifier.size(14.dp)) {
                                    val stroke = Stroke(width = size.width * 0.13f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    val path = Path().apply {
                                        moveTo(size.width * 0.15f, size.height * 0.75f)
                                        lineTo(size.width * 0.42f, size.height * 0.48f)
                                        lineTo(size.width * 0.60f, size.height * 0.66f)
                                        lineTo(size.width * 0.90f, size.height * 0.30f)
                                    }
                                    drawPath(path, Color(0xFF2E1A47), style = stroke)
                                    drawLine(Color(0xFF2E1A47), Offset(size.width * 0.65f, size.height * 0.30f), Offset(size.width * 0.90f, size.height * 0.30f), stroke.width, StrokeCap.Round)
                                    drawLine(Color(0xFF2E1A47), Offset(size.width * 0.90f, size.height * 0.30f), Offset(size.width * 0.90f, size.height * 0.55f), stroke.width, StrokeCap.Round)
                                }
                                BasicText(
                                    text = "Tendencia",
                                    style = typo.caption.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E1A47),
                                    ),
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                    ) {
                        BasicText(
                            text = "Bodas",
                            style = typo.rootTabTitle.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                            ),
                        )
                        BasicText(
                            text = "1.8K eventos",
                            style = typo.caption.copy(fontSize = 13.sp, color = mutedColor),
                        )
                    }
                }
            }
        }
    }
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
