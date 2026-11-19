package com.jeudry.rosafiesta.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfBitmapPhoto
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
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rememberDecodedImageBitmap
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.delay

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
        val star = Path().apply {
            moveTo(60f * s, 22f * s)
            lineTo(61.6f * s, 26.4f * s)
            lineTo(66f * s, 28f * s)
            lineTo(61.6f * s, 29.6f * s)
            lineTo(60f * s, 34f * s)
            lineTo(58.4f * s, 29.6f * s)
            lineTo(54f * s, 28f * s)
            lineTo(58.4f * s, 26.4f * s)
            close()
        }
        drawPath(star, Color(0xFFFBCA3F))
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
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
) {
    val palette = LocalRfPalette.current
    val isDark = if (palette.isDark) true else spec.variant == RfThemeVariant.PERSONAL_DARK
    val typo = LocalRfTypography.current
    val fullName = viewModel?.profileName?.collectAsState()?.value ?: "Invitado Rosa Fiesta"
    val avatarKey = viewModel?.profileAvatarKey?.collectAsState()?.value
    val avatarBytes = viewModel?.profileAvatarBytes?.collectAsState()?.value
    val firstName = fullName.substringBefore(" ").ifBlank { "Invitado" }
    val avatarInitial = InputValidator.extractInitials(firstName, fallback = "R", maxInitials = 1)
    val textColor = if (isDark) Color(0xFFF2F0F8) else Color(0xFF1A1A2E)
    val mutedColor = if (isDark) Color(0xFFA6A3BA) else Color(0xFF62627A)
    val cardBg = if (isDark) Color(0xFF1D1D30) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2A2A40) else RfThemeState.currentPalette.line
    val chipBg = if (isDark) Color(0x2EB54BD6) else Color(0xFFF5EAF9)
    val chipText = Color(0xFFB54BD6)
    val decoracionBg = if (isDark) Color(0xFF3A1630) else Color(0xFFFDE6F2)
    val mobiliarioBg = if (isDark) Color(0xFF26204A) else Color(0xFFEFE8FD)
    val mesaDulceBg = if (isDark) Color(0xFF2B3216) else Color(0xFFEEF6D6)
    val iluminacionBg = if (isDark) Color(0xFF3A2E14) else Color(0xFFFFF2C9)
    val arcosBg = if (isDark) Color(0xFF361B38) else Color(0xFFFCE8F6)
    val centrosBg = if (isDark) Color(0xFF1B332E) else Color(0xFFE0F7F2)
    val manteleriaBg = if (isDark) Color(0xFF232544) else Color(0xFFEAEBFF)

    val trendsScrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        for (cycle in 0 until 240) {
            delay(3200L)
            val maxScroll = trendsScrollState.maxValue
            val canAutoScroll = maxScroll > 0 && !trendsScrollState.isScrollInProgress
            if (canAutoScroll) {
                val stepPx = (maxScroll / 3).coerceAtLeast(180)
                val nextTarget = if (trendsScrollState.value + 40 >= maxScroll) {
                    0
                } else {
                    (trendsScrollState.value + stepPx).coerceAtMost(maxScroll)
                }
                trendsScrollState.animateScrollTo(
                    value = nextTarget,
                    animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                )
            }
        }
    }

    val vmSearchQuery = viewModel?.searchQuery?.collectAsState()?.value ?: ""
    var homeQuery by remember(vmSearchQuery) { mutableStateOf(vmSearchQuery) }
    val allHomeArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val featuredHomeArticles = remember(allHomeArticles) {
        allHomeArticles.filter { article -> article.visibleInCatalog }.take(4)
    }
    val notificationsList = viewModel?.notifications?.collectAsState()?.value
    val visibleClientNotifications = viewModel?.filterNotificationsForPersonalClient(notificationsList.orEmpty()).orEmpty()
    val hasUnreadNotifications = visibleClientNotifications.any { item -> !item.isRead }
    val draftQuantitiesState = viewModel?.draftQuantities?.collectAsState()?.value.orEmpty()
    val totalDraftCartCount = remember(draftQuantitiesState, viewModel) {
        viewModel?.totalDraftCartUnits() ?: 0
    }
    val matchingHomeArticles = remember(homeQuery, allHomeArticles) {
        val trimmedQuery = homeQuery.trim()
        if (trimmedQuery.isEmpty()) {
            emptyList()
        } else {
            allHomeArticles.filter { article ->
                val matchesName = article.name.contains(trimmedQuery, ignoreCase = true)
                val matchesCategory = article.category.contains(trimmedQuery, ignoreCase = true)
                val matchesCode = article.code.contains(trimmedQuery, ignoreCase = true)
                val matchesWithNameOrCat = matchesName || matchesCategory
                matchesWithNameOrCat || matchesCode
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 112.dp),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            // Profile Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                    .padding(2.dp)
                    .clickable { onSelectScreenCode("s54") },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(cardBg),
                ) {
                    val matchedPhoto = remember(avatarKey) {
                        if (avatarKey.isNullOrBlank()) {
                            null
                        } else {
                            RfPhotoKey.entries.firstOrNull { entry -> entry.name == avatarKey }
                        }
                    }
                    val isCustomAvatar = avatarKey?.startsWith("CUSTOM:") == true
                    val customAvatarBitmap = rememberDecodedImageBitmap(
                        bytes = if (isCustomAvatar) avatarBytes else null,
                    )
                    if (matchedPhoto != null) {
                        RfPhoto(
                            photoKey = matchedPhoto,
                            contentDescription = fullName,
                            cornerRadius = 999.dp,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (customAvatarBitmap != null) {
                        RfBitmapPhoto(
                            bitmap = customAvatarBitmap,
                            contentDescription = fullName,
                            cornerRadius = 999.dp,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (isCustomAvatar) {
                        RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFFC84FB8), size = 20.dp)
                    } else {
                        BasicText(
                            text = avatarInitial,
                            style = typo.rootTabTitle.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC84FB8),
                            ),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectScreenCode("s54") },
            ) {
                BasicText(
                    text = "Hola de nuevo",
                    style = typo.caption.copy(fontSize = 13.sp, color = mutedColor),
                )
                BasicText(
                    text = firstName,
                    style = typo.rootTabTitle.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    ),
                    maxLines = 1,
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape)
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(1.dp, cardBorder, CircleShape)
                    .clickable { onSelectScreenCode("s79") },
            ) {
                RfIcon(kind = RfIconKind.BELL, tint = textColor, size = 22.dp)
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 9.dp, end = 10.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53958))
                            .border(2.dp, cardBg, CircleShape),
                    )
                }
            }

            RfDraftCartPillButton(
                count = totalDraftCartCount,
                onClick = { onSelectScreenCode("s25") },
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(52.dp)
                .rfInputShadow(RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                .padding(start = 16.dp, end = 6.dp),
        ) {
            RfIcon(
                kind = RfIconKind.SEARCH,
                tint = mutedColor,
                size = 20.dp,
                modifier = Modifier.clickable { onSelectScreenCode("s80") },
            )
            BasicTextField(
                value = homeQuery,
                onValueChange = { updatedText ->
                    homeQuery = updatedText
                    viewModel?.setSearchQuery(updatedText)
                },
                singleLine = true,
                textStyle = typo.body.copy(fontSize = 15.sp, color = textColor),
                cursorBrush = SolidColor(Color(0xFFC2338F)),
                modifier = Modifier.weight(1f),
                decorationBox = { innerField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (homeQuery.isEmpty()) {
                            BasicText(
                                text = "¿Qué celebramos hoy? (sillas, arcos, mesas...)",
                                style = typo.body.copy(fontSize = 14.sp, color = mutedColor),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerField()
                    }
                },
            )
            if (homeQuery.isNotEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable {
                            homeQuery = ""
                            viewModel?.setSearchQuery("")
                        },
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = chipText, size = 14.dp)
                }
            }
            val isInlineVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
            val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
            val isSearchMicActive = if (isInlineVoiceListening) voiceOrigin == VoiceInputOrigin.SEARCH else false
            if (homeQuery.isBlank()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(chipBg)
                        .clickable { onSelectScreenCode("s81") },
                ) {
                    RfIcon(kind = RfIconKind.FILTER, tint = chipText, size = 18.dp)
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSearchMicActive) {
                            Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                        } else {
                            SolidColor(chipBg)
                        },
                    )
                    .clickable {
                        if (homeQuery.isNotBlank()) {
                            onSelectScreenCode("s81")
                        } else {
                            viewModel?.toggleInlineVoiceSearch()
                        }
                    },
            ) {
                val trailingIcon = if (homeQuery.isNotBlank()) RfIconKind.FILTER else RfIconKind.MIC
                val trailingIconTint = if (isSearchMicActive) Color.White else chipText
                RfIcon(kind = trailingIcon, tint = trailingIconTint, size = 20.dp)
            }
        }

        InlineVoiceSearchBanner(
            viewModel = viewModel,
            onApplyVoiceQuery = { spokenQuery ->
                homeQuery = spokenQuery
                viewModel?.setSearchQuery(spokenQuery)
            },
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        if (homeQuery.isNotBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText(
                        text = "${matchingHomeArticles.size} resultados para “${homeQuery.trim()}”",
                        style = typo.bodyStrong.copy(fontSize = 13.sp, color = textColor),
                    )
                    BasicText(
                        text = "Ver filtros →",
                        style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)),
                        modifier = Modifier.clickable { onSelectScreenCode("s81") },
                    )
                }
                if (matchingHomeArticles.isEmpty()) {
                    BasicText(
                        text = "No encontramos artículos con ese nombre. Prueba con “silla”, “mesa”, “arco” o “floral”.",
                        style = typo.caption.copy(fontSize = 13.sp, color = mutedColor),
                    )
                } else {
                    matchingHomeArticles.take(4).forEach { article ->
                        val resolvedPhoto = RfPhotoKey.entries.firstOrNull { entry ->
                            val matchesAsset = entry.assetKey == article.photo.key
                            val matchesEnumName = entry.name.equals(article.photo.key, ignoreCase = true)
                            listOf(matchesAsset, matchesEnumName).any { matched -> matched }
                        } ?: RfPhotoKey.MESA_REDONDA
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel?.selectArticle(article.id)
                                    onSelectScreenCode("s19")
                                }
                                .padding(vertical = 6.dp),
                        ) {
                            RfUploadedOrCatalogPhoto(
                                photoUrl = article.photoUrl,
                                fallbackPhotoKey = resolvedPhoto,
                                contentDescription = article.name,
                                viewModel = viewModel,
                                modifier = Modifier.size(48.dp),
                                cornerRadius = 10.dp,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = article.name,
                                    style = typo.bodyStrong.copy(fontSize = 14.sp, color = textColor),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                BasicText(
                                    text = "${article.category} · Stock ${article.totalStock}",
                                    style = typo.caption.copy(fontSize = 12.sp, color = mutedColor),
                                    maxLines = 1,
                                )
                            }
                            RfGradientPriceText("RD$ ${article.pricePerDayDop}/día")
                        }
                    }
                }
            }
        }

        val activeSeasonBanner = viewModel?.seasonBanner?.collectAsState()?.value ?: CanonicalSampleData.seasonBanner
        val showSeasonBanner = if (activeSeasonBanner.active) activeSeasonBanner.title.isNotBlank() else false
        if (showSeasonBanner) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(144.dp)
                    .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
            ) {
                // Background decorations: circles, 5-point star, and confetti
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val s = this.size.width / 350f
                    drawCircle(Color.White.copy(alpha = 0.10f), radius = 70f * s, center = Offset(300f * s, 30f * s))
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = 40f * s, center = Offset(330f * s, 170f * s))
                    drawCircle(Color(0xFFD6EA8C), radius = 5f * s, center = Offset(168f * s, 54f * s))
                    val star5 = Path().apply {
                        val points = listOf(
                            Offset(0f, -10f),
                            Offset(2.35f, -3.24f),
                            Offset(9.5f, -3.1f),
                            Offset(3.8f, 1.24f),
                            Offset(5.9f, 8.1f),
                            Offset(0f, 4f),
                            Offset(-5.9f, 8.1f),
                            Offset(-3.8f, 1.24f),
                            Offset(-9.5f, -3.1f),
                            Offset(-2.35f, -3.24f),
                        )
                        points.forEachIndexed { i, pt ->
                            val px = (196f + pt.x * 0.9f) * s
                            val py = (26f + pt.y * 0.9f) * s
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(star5, Color(0xFFFFD45C))
                    drawRoundRect(
                        color = Color(0xFFD6EA8C),
                        topLeft = Offset(222f * s, 58f * s),
                        size = Size(20f * s, 6f * s),
                        cornerRadius = CornerRadius(3f * s, 3f * s),
                    )
                }

                // Balloon arch decoration (210 x 210, right: -30px, bottom: -26px)
                HeroBalloonArchIllustration(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 30.dp, y = 26.dp),
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                ) {
                    if (activeSeasonBanner.badgeLabel.isNotBlank()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 10.dp),
                        ) {
                            BasicText(
                                text = activeSeasonBanner.badgeLabel,
                                style = typo.tagLabel.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E1A47),
                                    letterSpacing = 0.5.sp,
                                ),
                            )
                        }
                    }

                    BasicText(
                        text = activeSeasonBanner.title,
                        style = typo.rootTabTitle.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 26.sp,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 200.dp),
                    )

                    val resolvedCta = activeSeasonBanner.ctaLabel.ifBlank { "Explorar" }
                    val resolvedTarget = activeSeasonBanner.targetScreenCode.ifBlank { "s21" }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode(resolvedTarget) }
                            .padding(horizontal = 18.dp),
                    ) {
                        BasicText(
                            text = resolvedCta,
                            style = typo.buttonLabel.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA23FB8),
                            ),
                        )
                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFFA23FB8), size = 16.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                BasicText(
                    text = "Servicios",
                    style = typo.rootTabTitle.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    ),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable {
                            viewModel?.setSelectedCategory("Todas")
                            onSelectScreenCode("s18")
                        }
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

            val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
            if (vmCategories != null) {
                if (vmCategories.isEmpty()) {
                    RfEmptyStateCard(
                        icon = RfIconKind.CATALOG,
                        title = "Explora nuestro catálogo",
                        subtitle = "Descubre mobiliario disponible o diseña tu evento a medida con Rosa IA.",
                        actionLabel = "Ver catálogo completo",
                        compact = true,
                        onAction = {
                            viewModel.setSelectedCategory("Todas")
                            onSelectScreenCode("s18")
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                    ) {
                        vmCategories.forEachIndexed { index, category ->
                            val bgTint = if (index % 2 == 0) mobiliarioBg else decoracionBg
                            HomeServiceItem(
                                label = category.name,
                                bgColor = bgTint,
                                textColor = textColor,
                                onClick = {
                                    viewModel.setSelectedCategory(category.name)
                                    onSelectScreenCode("s18")
                                },
                            ) {
                                if (isCustomMediaIconKey(category.illoKey)) {
                                    RfCategoryCustomOrFallbackIcon(
                                        iconUrlOrKey = category.illoKey,
                                        contentDescription = category.name,
                                        viewModel = viewModel,
                                        size = 42.dp,
                                    )
                                } else {
                                    when {
                                        category.illoKey.contains("flor", ignoreCase = true) || category.name.contains("flor", ignoreCase = true) ->
                                            ServiceIllustrationFloral(modifier = Modifier.size(56.dp))

                                        category.illoKey.contains("dulce", ignoreCase = true) || category.name.contains("dulce", ignoreCase = true) ->
                                            ServiceIllustrationDulce(modifier = Modifier.size(56.dp))

                                        category.illoKey.contains("ilumin", ignoreCase = true) || category.name.contains("ilumin", ignoreCase = true) ->
                                            ServiceIllustrationLuz(modifier = Modifier.size(56.dp))

                                        category.illoKey.contains("glob", ignoreCase = true) || category.name.contains("glob", ignoreCase = true) ->
                                            RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFFE04FAF), size = 30.dp)

                                        else -> ServiceIllustrationMobiliario(modifier = Modifier.size(56.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                ) {
                    HomeServiceItem(
                        label = "Decoración",
                        bgColor = decoracionBg,
                        textColor = textColor,
                        onClick = {
                            viewModel?.setSelectedCategory("Decoración")
                            onSelectScreenCode("s18")
                        },
                    ) {
                        ServiceIllustrationFloral(modifier = Modifier.size(56.dp))
                    }

                    HomeServiceItem(
                        label = "Mobiliario",
                        bgColor = mobiliarioBg,
                        textColor = textColor,
                        onClick = {
                            viewModel?.setSelectedCategory("Mobiliario")
                            onSelectScreenCode("s18")
                        },
                    ) {
                        ServiceIllustrationMobiliario(modifier = Modifier.size(56.dp))
                    }

                    HomeServiceItem(
                        label = "Estaciones dulces",
                        bgColor = mesaDulceBg,
                        textColor = textColor,
                        onClick = {
                            viewModel?.setSelectedCategory("Estaciones dulces")
                            onSelectScreenCode("s18")
                        },
                    ) {
                        ServiceIllustrationDulce(modifier = Modifier.size(56.dp))
                    }

                    HomeServiceItem(
                        label = "Iluminación",
                        bgColor = iluminacionBg,
                        textColor = textColor,
                        onClick = {
                            viewModel?.setSelectedCategory("Iluminación")
                            onSelectScreenCode("s18")
                        },
                    ) {
                        ServiceIllustrationLuz(modifier = Modifier.size(56.dp))
                    }

                    HomeServiceItem(
                        label = "Arcos y globos",
                        bgColor = arcosBg,
                        textColor = textColor,
                        onClick = {
                            viewModel?.setSelectedCategory("Arcos y globos")
                            onSelectScreenCode("s18")
                        },
                    ) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFFE04FAF), size = 30.dp)
                    }

                    HomeServiceItem(
                        label = "Centros florales",
                        bgColor = centrosBg,
                        textColor = textColor,
                        onClick = {
                            viewModel?.setSelectedCategory("Centros florales")
                            onSelectScreenCode("s18")
                        },
                    ) {
                        RfIcon(kind = RfIconKind.STAR, tint = Color(0xFF1F9D8B), size = 30.dp)
                    }

                    HomeServiceItem(
                        label = "Mantelería",
                        bgColor = manteleriaBg,
                        textColor = textColor,
                        onClick = {
                            viewModel?.setSelectedCategory("Mantelería")
                            onSelectScreenCode("s18")
                        },
                    ) {
                        RfIcon(kind = RfIconKind.GIFT, tint = Color(0xFF6664E9), size = 30.dp)
                    }
                }
            }
        }

        val rawHomeTrends = viewModel?.catalogTrends?.collectAsState()?.value ?: CanonicalSampleData.trends
        val visibleHomeTrends = remember(rawHomeTrends) {
            rawHomeTrends.filter { item -> item.active }.sortedBy { item -> item.sortOrder }
        }
        if (visibleHomeTrends.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    BasicText(
                        text = "Tendencias",
                        style = typo.rootTabTitle.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
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
                        .horizontalScroll(trendsScrollState)
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
                ) {
                    visibleHomeTrends.forEach { trendItem ->
                        val resolvedTrendPhoto = RfPhotoKey.entries.firstOrNull { entry ->
                            val matchesAsset = entry.assetKey == trendItem.photo.key
                            val matchesEnum = entry.name.equals(trendItem.photo.key, ignoreCase = true)
                            if (matchesAsset) true else matchesEnum
                        } ?: RfPhotoKey.BODA
                        val lightCardBg = parseTrendBgHexOrDefault(trendItem.photoBgHex, Color(0xFFF3EEF6))
                        val resolvedBg = if (isDark) Color(0xFF26204A) else lightCardBg
                        HomeTrendCard(
                            title = trendItem.title,
                            eventsLabel = trendItem.eventsLabel,
                            photoKey = resolvedTrendPhoto,
                            photoUrl = trendItem.photoUrl,
                            viewModel = viewModel,
                            photoBg = resolvedBg,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textColor = textColor,
                            mutedColor = mutedColor,
                            onClick = {
                                when {
                                    trendItem.linkedPackageId.isNotBlank() -> {
                                        viewModel?.selectPackage(trendItem.linkedPackageId)
                                        onSelectScreenCode("s22")
                                    }

                                    trendItem.linkedCategory.isNotBlank() -> {
                                        viewModel?.setSelectedCategory(trendItem.linkedCategory)
                                        onSelectScreenCode("s18")
                                    }

                                    else -> onSelectScreenCode("s21")
                                }
                            },
                        )
                    }
                }
            }
        }

        if (featuredHomeArticles.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    BasicText(
                        text = "Artículos destacados",
                        style = typo.rootTabTitle.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        ),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(chipBg)
                            .clickable {
                                viewModel?.setSelectedCategory("Todas")
                                onSelectScreenCode("s18")
                            }
                            .padding(horizontal = 12.dp),
                    ) {
                        BasicText(
                            text = "Catálogo",
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
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                ) {
                    featuredHomeArticles.forEach { featuredArticle ->
                        val resolvedFeaturedPhoto = RfPhotoKey.entries.firstOrNull { entry ->
                            val matchesAsset = entry.assetKey == featuredArticle.photo.key
                            val matchesName = entry.name.equals(featuredArticle.photo.key, ignoreCase = true)
                            listOf(matchesAsset, matchesName).any { matched -> matched }
                        } ?: RfPhotoKey.ARCO_ROSA
                        Column(
                            modifier = Modifier
                                .width(196.dp)
                                .rfCardShadow(RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                .clickable {
                                    viewModel?.selectArticle(featuredArticle.id)
                                    onSelectScreenCode("s19")
                                },
                        ) {
                            RfUploadedOrCatalogPhoto(
                                photoUrl = featuredArticle.photoUrl,
                                fallbackPhotoKey = resolvedFeaturedPhoto,
                                contentDescription = featuredArticle.name,
                                viewModel = viewModel,
                                cornerRadius = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(94.dp),
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                BasicText(
                                    text = featuredArticle.name,
                                    style = typo.bodyStrong.copy(fontSize = 14.sp, color = textColor),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                BasicText(
                                    text = "RD$ ${featuredArticle.pricePerDayDop} /día",
                                    style = typo.caption.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2338F),
                                    ),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeServiceItem(
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    iconContent: @Composable () -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(74.dp)
                .clip(CircleShape)
                .background(bgColor),
        ) {
            iconContent()
        }
        BasicText(
            text = label,
            style = typo.caption.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeTrendCard(
    title: String,
    eventsLabel: String,
    photoKey: RfPhotoKey,
    photoUrl: String = "",
    viewModel: RosaFiestaViewModel? = null,
    photoBg: Color,
    cardBg: Color,
    cardBorder: Color,
    textColor: Color,
    mutedColor: Color,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = Modifier
            .width(230.dp)
            .rfCardShadow(RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(photoBg),
        ) {
            RfUploadedOrCatalogPhoto(
                photoUrl = photoUrl,
                fallbackPhotoKey = photoKey,
                contentDescription = title,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 0.dp,
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
                    RfIcon(kind = RfIconKind.TREND_UP, tint = Color(0xFF2E1A47), size = 14.dp)
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
                text = title,
                style = typo.rootTabTitle.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            BasicText(
                text = eventsLabel,
                style = typo.caption.copy(fontSize = 12.sp, color = mutedColor),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

private fun parseTrendBgHexOrDefault(
    rawHex: String,
    fallback: Color,
): Color {
    val cleanHex = rawHex.trim().removePrefix("#")
    if (cleanHex.length != 6) return fallback
    val parsedRgb = cleanHex.toLongOrNull(16) ?: return fallback
    return Color(0xFF000000L or parsedRgb)
}
