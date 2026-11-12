package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.designsystem.BalloonClusterIllustration
import com.jeudry.rosafiesta.designsystem.HeartOrbitIllustration
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfBrandWordmark
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.rotate as drawScopeRotate

// ==============================================================================
// p00 · SPLASH (APP PERSONAL)
// ==============================================================================

@Composable
internal fun RenderPersonalSplashP00(
    viewModel: RosaFiestaViewModel? = null,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val navigateFromPersonalSplash: () -> Unit = {
        val hasAuthenticatedSession = viewModel?.hasValidAuthenticatedSession() == true
        val isClientSession = if (hasAuthenticatedSession) !viewModel.isCurrentSessionAdminOrStaff() else false
        if (isClientSession) {
            onSelectScreenCode("s14")
        } else {
            onPrimaryAction()
        }
    }
    LaunchedEffect(Unit) {
        delay(1300L)
        navigateFromPersonalSplash()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    0.0f to Color(0xFFE04FAF),
                    0.45f to Color(0xFFB754CB),
                    0.75f to Color(0xFF865DEC),
                    1.0f to Color(0xFF6664E9),
                ),
            )
            .clickable(onClick = navigateFromPersonalSplash),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGarlandAndConfettiPersonal()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            // Doble halo oficial
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(188.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(188.dp)
                        .background(Color.White.copy(alpha = 0.07f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.White.copy(alpha = 0.14f), CircleShape),
                )
                RfLogo(customSize = 136.dp, insetPadding = false)
            }

            Spacer(Modifier.height(18.dp))

            BasicText(
                text = "Rosa Fiesta",
                style = typo.heroTitle.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                ),
            )

            Spacer(Modifier.height(8.dp))

            BasicText(
                text = "Todo para tu evento",
                style = typo.body.copy(
                    fontSize = 16.sp,
                    color = Color(0xFFFBEAF6),
                ),
            )
        }

        // Puntos de carga inferiores (y = 764dp / bottom 70dp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp),
        ) {
            Box(Modifier.size(10.dp).background(Color(0xFFFFD45C), CircleShape))
            Box(Modifier.size(10.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
            Box(Modifier.size(10.dp).background(Color(0xFFD6EA8C), CircleShape))
        }
    }
}

private fun DrawScope.drawGarlandAndConfettiPersonal() {
    val s = this.size.width / 390f
    scale(s, s, Offset.Zero) {
        val garlandPath = Path().apply {
            moveTo(-10f, 6f)
            quadraticTo(195f, 66f, 400f, 6f)
        }
        drawPath(
            path = garlandPath,
            color = Color.White.copy(alpha = 0.35f),
            style = Stroke(width = 1.2f, cap = StrokeCap.Round),
        )

        // Banderines
        val pennants = listOf(
            Triple(14f to 12f, 34f to 12f, 24f to 36f) to Color(0xFFFFD45C),
            Triple(58f to 22f, 78f to 22f, 68f to 46f) to Color.White.copy(alpha = 0.9f),
            Triple(102f to 29f, 122f to 29f, 112f to 53f) to Color(0xFFD6EA8C),
            Triple(146f to 34f, 166f to 34f, 156f to 58f) to Color(0xFFFFD45C),
            Triple(190f to 35f, 210f to 35f, 200f to 59f) to Color.White.copy(alpha = 0.9f),
            Triple(234f to 33f, 254f to 33f, 244f to 57f) to Color(0xFFD6EA8C),
            Triple(278f to 29f, 298f to 29f, 288f to 53f) to Color(0xFFFFD45C),
            Triple(322f to 22f, 342f to 22f, 332f to 46f) to Color.White.copy(alpha = 0.9f),
            Triple(366f to 12f, 386f to 12f, 376f to 36f) to Color(0xFFD6EA8C),
        )
        pennants.forEach { (points, color) ->
            val p = Path().apply {
                moveTo(points.first.first, points.first.second)
                lineTo(points.second.first, points.second.second)
                lineTo(points.third.first, points.third.second)
                close()
            }
            drawPath(p, color = color, style = Fill)
        }

        // Círculos tenues
        drawCircle(Color.White.copy(alpha = 0.07f), radius = 90f, center = Offset(300f, 160f))
        drawCircle(Color.White.copy(alpha = 0.06f), radius = 110f, center = Offset(70f, 700f))

        // Estrellas
        drawSvgStar(center = Offset(76f, 220f), scale = 1.1f, color = Color(0xFFFFD45C))
        drawSvgStar(center = Offset(320f, 600f), scale = 0.8f, color = Color.White.copy(alpha = 0.85f))

        // Confetti y puntos
        drawCircle(Color(0xFFD6EA8C), radius = 5f, center = Offset(300f, 300f))
        drawCircle(Color.White.copy(alpha = 0.8f), radius = 4f, center = Offset(96f, 560f))

        drawRoundRect(
            color = Color(0xFFFFD45C),
            topLeft = Offset(320f, 420f),
            size = Size(16f, 6f),
            cornerRadius = CornerRadius(3f, 3f),
        )
        drawRoundRect(
            color = Color(0xFFD6EA8C),
            topLeft = Offset(40f, 380f),
            size = Size(16f, 6f),
            cornerRadius = CornerRadius(3f, 3f),
        )
    }
}

private fun DrawScope.drawSvgStar(center: Offset, scale: Float, color: Color) {
    val p = Path().apply {
        moveTo(center.x + 0f * scale, center.y - 10f * scale)
        lineTo(center.x + 2.35f * scale, center.y - 3.24f * scale)
        lineTo(center.x + 9.5f * scale, center.y - 3.1f * scale)
        lineTo(center.x + 3.8f * scale, center.y + 1.24f * scale)
        lineTo(center.x + 5.9f * scale, center.y + 8.1f * scale)
        lineTo(center.x + 0f * scale, center.y + 4f * scale)
        lineTo(center.x - 5.9f * scale, center.y + 8.1f * scale)
        lineTo(center.x - 3.8f * scale, center.y + 1.24f * scale)
        lineTo(center.x - 9.5f * scale, center.y - 3.1f * scale)
        lineTo(center.x - 2.35f * scale, center.y - 3.24f * scale)
        close()
    }
    drawPath(p, color = color, style = Fill)
}

// ==============================================================================
// A00 · SPLASH (APP ADMINISTRATIVA)
// ==============================================================================

@Composable
internal fun RenderAdminSplashA00(
    viewModel: RosaFiestaViewModel? = null,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var progressTarget by remember { mutableStateOf(0.55f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "admin-splash-progress",
    )
    val navigateFromAdminSplash: () -> Unit = {
        if (viewModel != null) {
            val hasSession = viewModel.hasValidAuthenticatedSession()
            val isStaffOrAdmin = viewModel.isCurrentSessionAdminOrStaff()
            val canEnterDashboard = if (hasSession) isStaffOrAdmin else false
            if (canEnterDashboard) {
                onSelectScreenCode("A01")
            } else {
                onSelectScreenCode("A30")
            }
        } else {
            onPrimaryAction()
        }
    }
    LaunchedEffect(Unit) {
        progressTarget = 1.0f
        delay(1300L)
        navigateFromAdminSplash()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    0.0f to Color(0xFF7C5CF0),
                    0.50f to Color(0xFF5B6CF0),
                    1.0f to Color(0xFF2FB8C8),
                ),
            )
            .clickable(onClick = navigateFromAdminSplash),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleFactor = this.size.width / 390f
            scale(scaleFactor, scaleFactor, Offset.Zero) {
                drawCircle(Color.White.copy(alpha = 0.07f), radius = 110f, center = Offset(320f, 140f))
                drawCircle(Color.White.copy(alpha = 0.06f), radius = 120f, center = Offset(60f, 720f))
                drawLine(Color.White.copy(alpha = 0.08f), start = Offset(0f, 640f), end = Offset(390f, 640f))
                drawLine(Color.White.copy(alpha = 0.06f), start = Offset(0f, 680f), end = Offset(390f, 680f))
                drawSvgStar(center = Offset(300f, 250f), scale = 0.9f, color = Color(0xFFFFD45C))
                drawCircle(Color(0xFFD6EA8C), radius = 4f, center = Offset(84f, 250f))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 290.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(136.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.07f),
                            radius = size.width / 2f + 26.dp.toPx(),
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.14f),
                            radius = size.width / 2f + 12.dp.toPx(),
                        )
                    }
                    .shadow(elevation = 24.dp, shape = CircleShape, spotColor = Color(0x8C141E5A))
                    .clip(CircleShape)
                    .background(Color.White),
            ) {
                RfLogo(customSize = 136.dp, insetPadding = false)
            }

            Spacer(Modifier.height(18.dp))

            BasicText(
                text = "Rosa Fiesta",
                style = typo.heroTitle.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                ),
            )

            Spacer(Modifier.height(12.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(horizontal = 14.dp),
            ) {
                BasicText(
                    text = "ADMINISTRACIÓN",
                    style = typo.sectionCap.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.8.sp,
                    ),
                )
            }
        }

        // Barra de progreso inferior fiel a HTML (y = 762dp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
                .width(170.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.25f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFFD45C)),
            )
        }
    }
}

// ==============================================================================
// s03, s04, s05 · ONBOARDING (STEPS 1, 2, 3)
// ==============================================================================

@Composable
internal fun RenderOnboardingScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val step = when (spec.code) {
        "s03" -> 1
        "s04" -> 2
        "s05" -> 3
        else -> 1
    }

    val nextScreen = when (step) {
        1 -> "s04"
        2 -> "s05"
        else -> "s06"
    }

    val prevScreen = when (step) {
        2 -> "BACK:s03"
        3 -> "BACK:s04"
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (step == 1) {
                drawRect(
                    Brush.verticalGradient(
                        0.0f to Color(0xFFF8EBF4),
                        0.5f to Color(0xFFF6EFF8),
                        1.0f to Color(0xFFF3F1FA),
                    ),
                )
                val canvasScale = size.width / 390f
                scale(canvasScale, canvasScale, Offset.Zero) {
                    drawCircle(color = Color(0xFFCFE48A), radius = 22f, center = Offset(34f, 770f), style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFFE2CCF4), radius = 32f, center = Offset(352f, 720f), style = Stroke(width = 1.5f))
                    val diamondPath = Path().apply {
                        moveTo(330f, 470f)
                        lineTo(338f, 484f)
                        lineTo(330f, 498f)
                        lineTo(322f, 484f)
                        close()
                    }
                    drawPath(diamondPath, color = Color(0xFF9ED7DC), style = Stroke(width = 1.5f))
                    val squarePath = Path().apply {
                        moveTo(60f, 760f)
                        lineTo(80f, 754f)
                        lineTo(86f, 774f)
                        lineTo(66f, 780f)
                        close()
                    }
                    drawPath(squarePath, color = Color(0xFFC9B6F2), style = Stroke(width = 1.5f))
                    drawSvgStar(center = Offset(300f, 805f), scale = 1.1f, color = Color(0xFFFBCA3F))
                }
            } else {
                drawPersonalAtmosphereBackground()
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .personalTopBarBackground(isStep1 = step == 1)
                .padding(top = 14.dp, bottom = 10.dp, start = 20.dp, end = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfLogo(customSize = 36.dp)
                RfBrandWordmark(text = "Rosa Fiesta", fontSize = 18.sp)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(1.dp, Color(0xFFE9DDEB), RoundedCornerShape(999.dp))
                    .clickable { onSelectScreenCode("s06") }
                    .padding(horizontal = 16.dp),
            ) {
                BasicText(
                    text = "Omitir",
                    style = typo.body.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RfThemeState.currentPalette.muted,
                    ),
                )
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val isAdvancing = targetState > initialState
                buildOnboardingStepTransition(isAdvancing)
            },
            label = "personal-onboarding-step-content",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, bottom = 92.dp),
        ) { animatedStep ->
            val stepTag = when (animatedStep) {
                1 -> "01 · Catálogo"
                2 -> "02 · Rosa IA"
                else -> "03 · WhatsApp"
            }

            val stepColor = when (animatedStep) {
                1 -> Color(0xFFD63F98)
                2 -> Color(0xFF7A4FD8)
                else -> Color(0xFF4E6A10)
            }

            val stepTitle = when (animatedStep) {
                1 -> "Mira todo lo que tenemos"
                2 -> "Tu asistente para planificar"
                else -> "Confirma directo, sin trámites"
            }

            val stepSubtitle = when (animatedStep) {
                1 -> "Navega por nuestro catálogo de decoración y equipo para eventos. Sin registrarte, sin compromiso."
                2 -> "Dinos qué evento planeas y Rosa te sugiere artículos, categorías y más, paso a paso."
                else -> "Comparte tu lista por WhatsApp con María, la dueña, y confirma todo en minutos."
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    when (animatedStep) {
                        1 -> OnboardingIllustrationStep1()

                        2 -> OnboardingIllustrationStep2()

                        3 -> OnboardingIllustrationStep3(
                            onSendWhatsApp = {
                                viewModel?.openWhatsAppChat(
                                    message = "Hola RosaFiesta, quiero cotizar mi lista de evento",
                                )
                            },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                ) {
                    BasicText(
                        text = stepTag.uppercase(),
                        style = typo.sectionCap.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = stepColor,
                            letterSpacing = 1.sp,
                        ),
                    )

                    Spacer(Modifier.height(6.dp))

                    BasicText(
                        text = stepTitle,
                        style = typo.heroTitle.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF151729),
                            lineHeight = 34.sp,
                        ),
                    )

                    Spacer(Modifier.height(10.dp))

                    BasicText(
                        text = stepSubtitle,
                        style = typo.body.copy(
                            fontSize = 16.sp,
                            color = RfThemeState.currentPalette.muted,
                            lineHeight = 24.sp,
                        ),
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(2f)
                .padding(top = 10.dp, bottom = 18.dp, start = 28.dp, end = 28.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (stepDotIndex in 1..3) {
                    val isActive = stepDotIndex == step
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 28.dp else 8.dp,
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                        label = "personal-onboarding-dot-width",
                    )
                    val dotHeight by animateDpAsState(
                        targetValue = if (isActive) 6.dp else 8.dp,
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                        label = "personal-onboarding-dot-height",
                    )
                    val dotTargetCode = when (stepDotIndex) {
                        1 -> if (step > 1) "BACK:s03" else "s03"
                        2 -> if (step > 2) "BACK:s04" else "s04"
                        else -> "s05"
                    }
                    val dotBrush = if (isActive) {
                        when (step) {
                            1 -> Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                            2 -> SolidColor(Color(0xFF865DEC))
                            else -> SolidColor(Color(0xFF8DB52C))
                        }
                    } else {
                        val inactiveColor = if (step == 1) Color(0xFFDCD3E4) else Color(0xFFE3DCE8)
                        SolidColor(inactiveColor)
                    }
                    Box(
                        modifier = Modifier
                            .width(dotWidth)
                            .height(dotHeight)
                            .clip(RoundedCornerShape(999.dp))
                            .background(dotBrush)
                            .clickable { onSelectScreenCode(dotTargetCode) },
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val hasPreviousStep = prevScreen != null
                AnimatedVisibility(
                    visible = hasPreviousStep,
                    enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.85f),
                    exit = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.85f),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFE6DFEE), CircleShape)
                            .clickable {
                                val backCode = prevScreen ?: "BACK:s03"
                                onSelectScreenCode(backCode)
                            },
                    ) {
                        RfIcon(
                            kind = RfIconKind.BACK,
                            tint = Color(0xFF151729),
                            size = 22.dp,
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                when (step) {
                                    1 -> listOf(Color(0xFFEA4EA9), Color(0xFFB754CB))
                                    else -> listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))
                                },
                            ),
                        )
                        .shadow(elevation = 12.dp, shape = CircleShape, spotColor = Color(0x99D646A0))
                        .clickable { onSelectScreenCode(nextScreen) },
                ) {
                    RfIcon(
                        kind = RfIconKind.BACK,
                        tint = Color.White,
                        size = 24.dp,
                        modifier = Modifier.rotate(180f),
                    )
                }
            }
        }
    }
}

private fun buildOnboardingStepTransition(isAdvancing: Boolean): ContentTransform = if (isAdvancing) {
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> fullWidth / 3 },
        ) + fadeIn(animationSpec = tween(durationMillis = 240))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> -fullWidth / 4 },
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
    )
} else {
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> -fullWidth / 3 },
        ) + fadeIn(animationSpec = tween(durationMillis = 240))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> fullWidth / 4 },
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
    )
}

private fun Modifier.personalTopBarBackground(isStep1: Boolean = false): Modifier = this
    .clip(RoundedCornerShape(0.dp))
    .drawBehind {
        drawRect(RfThemeState.currentPalette.bg)
        if (isStep1) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFADEEB), Color(0x00FADEEB)),
                    center = Offset(size.width * 0.2f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width * 0.9f, 84.dp.toPx()),
                    radius = size.width * 0.8f,
                ),
            )
        } else {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width, 126.dp.toPx()),
                    radius = size.width * 0.8f,
                ),
            )
        }
    }

private fun DrawScope.drawPersonalAtmosphereBackground() {
    drawRect(RfThemeState.currentPalette.bg)
    drawRect(
        Brush.radialGradient(
            listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
            center = Offset(0f, 0f),
            radius = size.width * 1.1f,
        ),
    )
    drawRect(
        Brush.radialGradient(
            listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
            center = Offset(size.width, size.height * 0.30f),
            radius = size.width * 0.8f,
        ),
    )
}

@Composable
private fun OnboardingIllustrationStep1() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(328.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sX = size.width / 350f
            val sY = size.height / 340f
            scale(sX, sY, Offset.Zero) {
                // Fondo orgánico (arch blob)
                val blobPath = Path().apply {
                    moveTo(175f, 18f)
                    cubicTo(262f, 12f, 340f, 66f, 342f, 158f)
                    cubicTo(344f, 250f, 292f, 330f, 180f, 332f)
                    cubicTo(68f, 334f, 8f, 290f, 10f, 178f)
                    cubicTo(12f, 66f, 88f, 22f, 175f, 18f)
                    close()
                }
                drawPath(
                    blobPath,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFF9D3EA), Color(0xFFE4D6FB)),
                        start = Offset(0f, 0f),
                        end = Offset(350f, 340f),
                    ),
                    style = Fill,
                )

                // Cuerda de banderines
                val garlandPath = Path().apply {
                    moveTo(0f, 6f)
                    quadraticTo(175f, 50f, 350f, 6f)
                }
                drawPath(garlandPath, color = RfThemeState.currentPalette.ink.copy(alpha = 0.2f), style = Stroke(width = 1.3f))

                // 9 banderines
                val pennants = listOf(
                    Triple(20f to 12.9f, 40f to 12.9f, 30f to 36.9f) to Color(0xFFEA4EA9),
                    Triple(56f to 19.5f, 76f to 19.5f, 66f to 43.5f) to Color(0xFFFBCA3F),
                    Triple(92f to 24.2f, 112f to 24.2f, 102f to 48.2f) to Color(0xFFB9D84A),
                    Triple(128f to 27f, 148f to 27f, 138f to 51f) to Color(0xFF865DEC),
                    Triple(164f to 28f, 184f to 28f, 174f to 52f) to Color(0xFF5CC9C0),
                    Triple(200f to 27.1f, 220f to 27.1f, 210f to 51.1f) to Color(0xFFEA4EA9),
                    Triple(236f to 24.4f, 256f to 24.4f, 246f to 48.4f) to Color(0xFFFBCA3F),
                    Triple(272f to 19.8f, 292f to 19.8f, 282f to 43.8f) to Color(0xFFB9D84A),
                    Triple(308f to 13.4f, 328f to 13.4f, 318f to 37.4f) to Color(0xFF865DEC),
                )
                pennants.forEach { (pts, color) ->
                    val p = Path().apply {
                        moveTo(pts.first.first, pts.first.second)
                        lineTo(pts.second.first, pts.second.second)
                        lineTo(pts.third.first, pts.third.second)
                        close()
                    }
                    drawPath(p, color = color, style = Fill)
                }
            }
        }

        // Arco de globos (left: 35dp, top: 44dp, size: 280dp)
        BalloonArchIllustration(
            modifier = Modifier
                .offset(x = 35.dp, y = 44.dp)
                .size(280.dp),
        )

        // Mesa de pastel (left: 95dp, top: 168dp, size: 160dp)
        CakeTableIllustration(
            modifier = Modifier
                .offset(x = 95.dp, y = 168.dp)
                .size(160.dp),
        )

        // Silla Tiffany (left: 4dp, top: 214dp, size: 110dp)
        TiffanyChairIllustration(
            modifier = Modifier
                .offset(x = 4.dp, y = 214.dp)
                .size(110.dp),
        )

        // Florero (left: 246dp, top: 220dp, size: 104dp)
        FlowerVaseIllustration(
            modifier = Modifier
                .offset(x = 246.dp, y = 220.dp)
                .size(104.dp),
        )
    }
}

@Composable
private fun BalloonArchIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        scale(s, s, Offset.Zero) {
            // Sombra
            drawOval(
                color = Color(0xFF2B1730).copy(alpha = 0.08f),
                topLeft = Offset(60f - 50f, 110f - 3.5f),
                size = Size(100f, 7f),
            )
            // Globos pequeños
            drawCircle(Color(0xFFE2EFA6), radius = 6.5f, center = Offset(9f, 91f))
            drawCircle(Color(0xFFFFDF8A), radius = 6.5f, center = Offset(26.6f, 60f))
            drawCircle(Color(0xFFF9B6D3), radius = 6.5f, center = Offset(60f, 47f))
            drawCircle(Color(0xFFE2EFA6), radius = 6.5f, center = Offset(93.4f, 60f))
            drawCircle(Color(0xFFC9B6F7), radius = 6.5f, center = Offset(111f, 91f))

            // Globos grandes del arco
            drawCircle(Color(0xFFEA4EA9), radius = 10.5f, center = Offset(18f, 104f))
            drawCircle(Color(0xFFB9D84A), radius = 10.5f, center = Offset(20.5f, 86f))
            drawCircle(Color(0xFF865DEC), radius = 10.5f, center = Offset(28f, 72f))
            drawCircle(Color(0xFFFBCA3F), radius = 10.5f, center = Offset(39f, 62.5f))
            drawCircle(Color(0xFFEA4EA9), radius = 10.5f, center = Offset(52.7f, 58f))
            drawCircle(Color(0xFFB9D84A), radius = 10.5f, center = Offset(67.3f, 58f))
            drawCircle(Color(0xFF865DEC), radius = 10.5f, center = Offset(81f, 62.5f))
            drawCircle(Color(0xFFFBCA3F), radius = 10.5f, center = Offset(92f, 72f))
            drawCircle(Color(0xFFEA4EA9), radius = 10.5f, center = Offset(99.5f, 86f))
            drawCircle(Color(0xFFB9D84A), radius = 10.5f, center = Offset(102f, 104f))

            // Reflejos
            val hlColor = Color.White.copy(alpha = 0.55f)
            drawOval(hlColor, topLeft = Offset(15f - 2.4f, 100f - 3.4f), size = Size(4.8f, 6.8f))
            drawOval(hlColor, topLeft = Offset(36f - 2.4f, 58.5f - 3.4f), size = Size(4.8f, 6.8f))
            drawOval(hlColor, topLeft = Offset(64f - 2.4f, 54f - 3.4f), size = Size(4.8f, 6.8f))
            drawOval(hlColor, topLeft = Offset(89f - 2.4f, 68f - 3.4f), size = Size(4.8f, 6.8f))
            drawOval(hlColor, topLeft = Offset(99f - 2.4f, 100f - 3.4f), size = Size(4.8f, 6.8f))

            // Estrella
            val star = Path().apply {
                moveTo(60f, 22f)
                lineTo(61.6f, 26.4f)
                lineTo(66f, 28f)
                lineTo(61.6f, 29.6f)
                lineTo(60f, 34f)
                lineTo(58.4f, 29.6f)
                lineTo(54f, 28f)
                lineTo(58.4f, 26.4f)
                close()
            }
            drawPath(star, color = Color(0xFFFBCA3F), style = Fill)
        }
    }
}

@Composable
private fun CakeTableIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        scale(s, s, Offset.Zero) {
            // Sombra
            drawOval(
                color = Color(0xFF2B1730).copy(alpha = 0.08f),
                topLeft = Offset(60f - 46f, 110f - 3.5f),
                size = Size(92f, 7f),
            )
            // Falda de mesa
            drawRect(Color(0xFFF9B6D3), topLeft = Offset(16f, 74f), size = Size(88f, 34f))
            val pleatColor = Color(0xFFEC4F97).copy(alpha = 0.3f)
            listOf(30f, 44f, 58f, 72f, 86f).forEach { x ->
                drawLine(pleatColor, start = Offset(x, 80f), end = Offset(x, 108f), strokeWidth = 2f)
            }
            // Borde superior
            drawRect(Color(0xFFEC4F97), topLeft = Offset(14f, 72f), size = Size(92f, 8f))
            // Base del pastel
            val standPath = Path().apply {
                moveTo(46f, 72f)
                lineTo(52f, 65f)
                lineTo(68f, 65f)
                lineTo(74f, 72f)
                close()
            }
            drawPath(standPath, color = Color.White, style = Fill)

            // Piso inferior pastel
            drawRoundRect(Color(0xFFFFF6EE), topLeft = Offset(42f, 45f), size = Size(36f, 21f), cornerRadius = CornerRadius(4f, 4f))
            // Drip violeta
            val drip = Path().apply {
                moveTo(42f, 49f)
                quadraticTo(46f, 56f, 50f, 49f)
                quadraticTo(54f, 56f, 58f, 49f)
                quadraticTo(62f, 56f, 66f, 49f)
                quadraticTo(70f, 56f, 74f, 49f)
                quadraticTo(76f, 53f, 78f, 49f)
                lineTo(78f, 47f)
                quadraticTo(78f, 45f, 76f, 45f)
                lineTo(44f, 45f)
                quadraticTo(42f, 45f, 42f, 47f)
                close()
            }
            drawPath(drip, color = Color(0xFF7650E8), style = Fill)

            // Piso superior pastel
            drawRoundRect(Color(0xFFFFF6EE), topLeft = Offset(48f, 29f), size = Size(24f, 17f), cornerRadius = CornerRadius(4f, 4f))
            drawRoundRect(Color(0xFFF9B6D3), topLeft = Offset(48f, 29f), size = Size(24f, 5f), cornerRadius = CornerRadius(2.5f, 2.5f))

            // Vela
            drawRoundRect(Color(0xFF4AA8E0), topLeft = Offset(58.5f, 19f), size = Size(3f, 10f), cornerRadius = CornerRadius(1.5f, 1.5f))
            // Llama
            val flame = Path().apply {
                moveTo(60f, 11.5f)
                cubicTo(62.4f, 15f, 62.4f, 17.5f, 60f, 18.8f)
                cubicTo(57.6f, 17.5f, 57.6f, 15f, 60f, 11.5f)
                close()
            }
            drawPath(flame, color = Color(0xFFF5B53B), style = Fill)

            // Cupcake izquierdo
            val cupL = Path().apply {
                moveTo(24f, 72f)
                lineTo(26f, 63f)
                lineTo(36f, 63f)
                lineTo(38f, 72f)
                close()
            }
            drawPath(cupL, color = Color(0xFF2FB58F), style = Fill)
            drawCircle(Color(0xFFFFF6EE), radius = 6.5f, center = Offset(31f, 60f))
            drawCircle(Color(0xFFEC4F97), radius = 2.2f, center = Offset(31f, 54f))

            // Cupcake derecho
            val cupR = Path().apply {
                moveTo(82f, 72f)
                lineTo(84f, 63f)
                lineTo(94f, 63f)
                lineTo(96f, 72f)
                close()
            }
            drawPath(cupR, color = Color(0xFFF5B53B), style = Fill)
            drawCircle(Color(0xFFFFF6EE), radius = 6.5f, center = Offset(89f, 60f))
            drawCircle(Color(0xFFEC4F97), radius = 2.2f, center = Offset(89f, 54f))
        }
    }
}

@Composable
private fun TiffanyChairIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        scale(s, s, Offset.Zero) {
            // Sombra
            drawOval(
                color = Color(0xFF2B1730).copy(alpha = 0.08f),
                topLeft = Offset(60f - 30f, 108f - 3.5f),
                size = Size(60f, 7f),
            )
            val gold = Color(0xFFD9A441)
            // Patas exteriores
            drawLine(gold, Offset(45f, 66f), Offset(41f, 106f), strokeWidth = 3.4f, cap = StrokeCap.Round)
            drawLine(gold, Offset(75f, 66f), Offset(79f, 106f), strokeWidth = 3.4f, cap = StrokeCap.Round)
            // Patas interiores y travesaño
            drawLine(gold, Offset(49f, 70f), Offset(47f, 104f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            drawLine(gold, Offset(71f, 70f), Offset(73f, 104f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            drawLine(gold, Offset(43f, 92f), Offset(77f, 92f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            // Respaldo montantes
            drawLine(gold, Offset(46f, 64f), Offset(46f, 18f), strokeWidth = 3.4f, cap = StrokeCap.Round)
            drawLine(gold, Offset(74f, 64f), Offset(74f, 18f), strokeWidth = 3.4f, cap = StrokeCap.Round)
            // Travesaños horizontales
            drawLine(gold, Offset(46f, 18f), Offset(74f, 18f), strokeWidth = 3.4f, cap = StrokeCap.Round)
            drawLine(gold, Offset(46f, 30f), Offset(74f, 30f), strokeWidth = 3.4f, cap = StrokeCap.Round)
            drawLine(gold, Offset(46f, 50f), Offset(74f, 50f), strokeWidth = 3.4f, cap = StrokeCap.Round)
            // Husillos verticales
            drawLine(gold, Offset(54f, 30f), Offset(54f, 50f), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(gold, Offset(60f, 30f), Offset(60f, 50f), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(gold, Offset(66f, 30f), Offset(66f, 50f), strokeWidth = 2f, cap = StrokeCap.Round)
            // Cojín
            drawRoundRect(Color(0xFFF9B6D3), topLeft = Offset(38f, 59f), size = Size(44f, 10f), cornerRadius = CornerRadius(5f, 5f))
            drawRoundRect(Color.White.copy(alpha = 0.6f), topLeft = Offset(42f, 60f), size = Size(36f, 3f), cornerRadius = CornerRadius(1.5f, 1.5f))
        }
    }
}

@Composable
private fun FlowerVaseIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        scale(s, s, Offset.Zero) {
            // Sombra
            drawOval(
                color = Color(0xFF2B1730).copy(alpha = 0.08f),
                topLeft = Offset(60f - 22f, 108f - 3f),
                size = Size(44f, 6f),
            )
            // Hoja izquierda
            val leafL = Path().apply {
                moveTo(60f, 74f)
                cubicTo(40f, 72f, 28f, 58f, 32f, 46f)
                cubicTo(46f, 50f, 56f, 60f, 60f, 74f)
                close()
            }
            drawPath(leafL, color = Color(0xFF2FB58F), style = Fill)
            // Hoja derecha
            val leafR = Path().apply {
                moveTo(60f, 74f)
                cubicTo(80f, 72f, 93f, 60f, 89f, 48f)
                cubicTo(76f, 52f, 64f, 61f, 60f, 74f)
                close()
            }
            drawPath(leafR, color = Color(0xFF26A07D), style = Fill)
            // Tallos
            val stemColor = Color(0xFF1F8C6B)
            drawLine(stemColor, Offset(60f, 74f), Offset(48f, 44f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            drawLine(stemColor, Offset(60f, 74f), Offset(60f, 36f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            drawLine(stemColor, Offset(60f, 74f), Offset(75f, 46f), strokeWidth = 2.4f, cap = StrokeCap.Round)

            // Flor rosa izquierda
            drawCircle(Color(0xFFF9B6D3), radius = 10f, center = Offset(47f, 42f))
            drawArc(
                color = Color(0xFFEC4F97),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(47f - 3.5f, 42f - 3.5f),
                size = Size(7f, 7f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round),
            )
            drawArc(
                color = Color(0xFFEC4F97),
                startAngle = 180f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(47f - 5.5f, 42f - 5.5f),
                size = Size(11f, 11f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round),
            )

            // Flor amarilla derecha
            drawCircle(Color(0xFFFFD98A), radius = 10f, center = Offset(76f, 44f))
            drawArc(
                color = Color(0xFFD9952A),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(76f - 3.5f, 44f - 3.5f),
                size = Size(7f, 7f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round),
            )
            drawArc(
                color = Color(0xFFD9952A),
                startAngle = 180f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(76f - 5.5f, 44f - 5.5f),
                size = Size(11f, 11f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round),
            )

            // Flor magenta centro
            drawCircle(Color(0xFFEC4F97), radius = 12.5f, center = Offset(61f, 31f))
            drawArc(
                color = Color(0xFFC02A72),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(61f - 4.5f, 31f - 4.5f),
                size = Size(9f, 9f),
                style = Stroke(width = 2f, cap = StrokeCap.Round),
            )
            drawArc(
                color = Color(0xFFC02A72),
                startAngle = 180f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(61f - 7f, 31f - 7f),
                size = Size(14f, 14f),
                style = Stroke(width = 2f, cap = StrokeCap.Round),
            )

            // Puntos blancos
            drawCircle(Color.White, radius = 3f, center = Offset(35f, 58f))
            drawCircle(Color.White, radius = 3f, center = Offset(88f, 60f))

            // Macetero
            val pot = Path().apply {
                moveTo(46f, 72f)
                lineTo(74f, 72f)
                lineTo(70f, 104f)
                quadraticTo(60f, 108f, 50f, 104f)
                close()
            }
            drawPath(pot, color = Color(0xFFB9A6F7), style = Fill)
            drawLine(Color.White.copy(alpha = 0.55f), Offset(52f, 77f), Offset(50f, 100f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun OnboardingIllustrationStep2() {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val sX = size.width / 350f
            val sY = size.height / 340f
            scale(sX, sY, Offset.Zero) {
                val blobPath = Path().apply {
                    moveTo(180f, 14f)
                    cubicTo(276f, 10f, 344f, 78f, 340f, 170f)
                    cubicTo(336f, 262f, 276f, 334f, 170f, 330f)
                    cubicTo(64f, 326f, 6f, 268f, 12f, 160f)
                    cubicTo(18f, 52f, 84f, 18f, 180f, 14f)
                    close()
                }
                drawPath(blobPath, color = Color(0xFFEFE8FD), style = Fill)

                val star1 = Path().apply {
                    moveTo(250f, 40f)
                    lineTo(252.4f, 46.4f)
                    lineTo(258.8f, 48.8f)
                    lineTo(252.4f, 51.2f)
                    lineTo(250f, 57.6f)
                    lineTo(247.6f, 51.2f)
                    lineTo(241.2f, 48.8f)
                    lineTo(247.6f, 46.4f)
                    close()
                }
                drawPath(star1, color = Color(0xFFEA4EA9), style = Fill)

                val star2 = Path().apply {
                    moveTo(96f, 64f)
                    lineTo(97.6f, 68.2f)
                    lineTo(101.8f, 69.8f)
                    lineTo(97.6f, 71.4f)
                    lineTo(96f, 75.6f)
                    lineTo(94.4f, 71.4f)
                    lineTo(90.2f, 69.8f)
                    lineTo(94.4f, 68.2f)
                    close()
                }
                drawPath(star2, color = Color(0xFFF7B500), style = Fill)

                drawCircle(Color(0xFF8DB52C), radius = 4f, center = Offset(286f, 96f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(92.dp),
            ) {
                RosaAvatar(size = RosaAvatarSize.GIANT, glow = true)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp), spotColor = Color(0x40AA46A0))
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
            ) {
                BasicText(
                    text = "¿Qué evento quieres celebrar?",
                    style = LocalRfTypography.current.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 6.dp, bottomStart = 18.dp), spotColor = Color(0x66865DEC))
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 6.dp, bottomStart = 18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
            ) {
                BasicText(
                    text = "Una boda en un jardín",
                    style = LocalRfTypography.current.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .width(250.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp), spotColor = Color(0x40AA46A0))
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BasicText(
                        text = "¡Me encanta! Te sugiero:",
                        style = LocalRfTypography.current.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfPhoto(
                            photoKey = RfPhotoKey.SILLA_TIFFANY,
                            contentDescription = "Silla Tiffany",
                            cornerRadius = 12.dp,
                            modifier = Modifier.size(58.dp),
                        )
                        RfPhoto(
                            photoKey = RfPhotoKey.CENTRO_FLORAL,
                            contentDescription = "Centro Floral",
                            cornerRadius = 12.dp,
                            modifier = Modifier.size(58.dp),
                        )
                        RfPhoto(
                            photoKey = RfPhotoKey.ARCO_ROSA,
                            contentDescription = "Arco Rosa",
                            cornerRadius = 12.dp,
                            modifier = Modifier.size(58.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingIllustrationStep3(
    onSendWhatsApp: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val sX = size.width / 350f
            val sY = size.height / 340f
            scale(sX, sY, Offset.Zero) {
                val blobPath = Path().apply {
                    moveTo(168f, 12f)
                    cubicTo(262f, 6f, 346f, 70f, 338f, 172f)
                    cubicTo(330f, 274f, 262f, 336f, 158f, 328f)
                    cubicTo(54f, 320f, 4f, 262f, 14f, 154f)
                    cubicTo(24f, 46f, 74f, 18f, 168f, 12f)
                    close()
                }
                drawPath(blobPath, color = Color(0xFFEEF6D6), style = Fill)

                drawCircle(Color(0xFFF7B500), radius = 5f, center = Offset(306f, 60f))

                val star = Path().apply {
                    moveTo(30f, 252f)
                    lineTo(32f, 257.4f)
                    lineTo(37.4f, 259.4f)
                    lineTo(32f, 261.4f)
                    lineTo(30f, 266.8f)
                    lineTo(28f, 261.4f)
                    lineTo(22.6f, 259.4f)
                    lineTo(28f, 257.4f)
                    close()
                }
                drawPath(star, color = Color(0xFFEA4EA9), style = Fill)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 6.dp, start = 24.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(236.dp)
                    .rotate(-3f)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x40AA46A0))
                    .clip(RoundedCornerShape(24.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        BasicText(
                            text = "Tu lista",
                            style = LocalRfTypography.current.sheetOrEmptyTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "Boda Real Rosa & Dorado",
                            style = LocalRfTypography.current.secondary.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RfPhoto(
                            photoKey = RfPhotoKey.SILLA_TIFFANY,
                            contentDescription = "Sillas Tiffany",
                            cornerRadius = 10.dp,
                            modifier = Modifier.size(38.dp),
                        )
                        BasicText("Sillas Tiffany", style = LocalRfTypography.current.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink), modifier = Modifier.weight(1f))
                        BasicText("×40", style = LocalRfTypography.current.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RfPhoto(
                            photoKey = RfPhotoKey.ARCO_ROSA,
                            contentDescription = "Arco de globos",
                            cornerRadius = 10.dp,
                            modifier = Modifier.size(38.dp),
                        )
                        BasicText("Arco de globos", style = LocalRfTypography.current.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink), modifier = Modifier.weight(1f))
                        BasicText("×1", style = LocalRfTypography.current.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RfPhoto(
                            photoKey = RfPhotoKey.CENTRO_FLORAL,
                            contentDescription = "Centros de mesa",
                            cornerRadius = 10.dp,
                            modifier = Modifier.size(38.dp),
                        )
                        BasicText("Centros de mesa", style = LocalRfTypography.current.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink), modifier = Modifier.weight(1f))
                        BasicText("×8", style = LocalRfTypography.current.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E9E5B))
                            .clickable(onClick = onSendWhatsApp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.WHATSAPP, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = "Enviar por WhatsApp",
                                style = LocalRfTypography.current.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .shadow(elevation = 14.dp, shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 6.dp, bottomStart = 18.dp), spotColor = Color(0x40AA46A0))
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 6.dp, bottomStart = 18.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF6D6)),
                    ) {
                        BasicText(
                            text = "M",
                            style = LocalRfTypography.current.body.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10)),
                        )
                    }
                    Column {
                        BasicText(
                            text = "María · Rosa Fiesta",
                            style = LocalRfTypography.current.secondary.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText(
                                text = "¡Todo confirmado!",
                                style = LocalRfTypography.current.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                            )
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 16.dp)
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s06 · BIENVENIDA (STEP 4)
// ==============================================================================

@Composable
internal fun RenderBienvenidaScreen(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPersonalAtmosphereBackground()
            drawBienvenidaDecorations()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFE6DFEE), CircleShape)
                    .clickable { onSelectScreenCode("BACK:s05") },
            ) {
                RfIcon(
                    kind = RfIconKind.BACK,
                    tint = Color(0xFF151729),
                    size = 22.dp,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(116.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color(0x0D865DEC), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .background(Color(0x14EA4EA9), CircleShape),
                )
                RfLogo(customSize = 116.dp, insetPadding = false)
            }

            Spacer(Modifier.height(18.dp))

            Row {
                BasicText(
                    text = "Rosa ",
                    style = typo.heroTitle.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
                BasicText(
                    text = "Fiesta",
                    style = typo.heroTitle.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))),
                    ),
                )
            }

            Spacer(Modifier.height(12.dp))

            BasicText(
                text = "Todo para tu evento, guiado por inteligencia artificial.",
                style = typo.body.copy(
                    fontSize = 17.sp,
                    color = RfThemeState.currentPalette.muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 25.sp,
                ),
                modifier = Modifier.width(260.dp),
            )

            Spacer(Modifier.weight(1f).heightIn(min = 32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(Color(0xFFE3DCE8), CircleShape)
                        .clickable { onSelectScreenCode("BACK:s03") },
                )
                Box(
                    Modifier
                        .size(8.dp)
                        .background(Color(0xFFE3DCE8), CircleShape)
                        .clickable { onSelectScreenCode("BACK:s04") },
                )
                Box(
                    Modifier
                        .size(8.dp)
                        .background(Color(0xFFE3DCE8), CircleShape)
                        .clickable { onSelectScreenCode("BACK:s05") },
                )
                Box(
                    Modifier
                        .width(28.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x99C846B4), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
                        ),
                    )
                    .clickable { onSelectScreenCode("s08") },
            ) {
                BasicText(
                    text = "Comenzar",
                    style = typo.body.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x207850A0), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                    .clickable { onSelectScreenCode("s07") },
            ) {
                BasicText(
                    text = "Ya tengo cuenta",
                    style = typo.body.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }

            Spacer(Modifier.height(14.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel?.enterGuestMode()
                        onSelectScreenCode("s01")
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                BasicText(
                    text = "Explorar como invitado",
                    style = typo.body.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD63F98),
                        textDecoration = TextDecoration.Underline,
                    ),
                )
            }
        }
    }
}

private fun DrawScope.drawBienvenidaDecorations() {
    val s = this.size.width / 390f
    scale(s, s, Offset.Zero) {
        val garlandPath = Path().apply {
            moveTo(-10f, 4f)
            quadraticTo(195f, 64f, 400f, 4f)
        }
        drawPath(
            path = garlandPath,
            color = RfThemeState.currentPalette.ink.copy(alpha = 0.18f),
            style = Stroke(width = 1.2f, cap = StrokeCap.Round),
        )

        val pennants = listOf(
            Triple(14f to 10f, 34f to 10f, 24f to 34f) to Color(0xFFEA4EA9),
            Triple(58f to 20f, 78f to 20f, 68f to 44f) to Color(0xFFFBCA3F),
            Triple(102f to 27f, 122f to 27f, 112f to 51f) to Color(0xFFB9D84A),
            Triple(146f to 32f, 166f to 32f, 156f to 56f) to Color(0xFF865DEC),
            Triple(190f to 33f, 210f to 33f, 200f to 57f) to Color(0xFF1FBDC0),
            Triple(234f to 31f, 254f to 31f, 244f to 55f) to Color(0xFFEA4EA9),
            Triple(278f to 27f, 298f to 27f, 288f to 51f) to Color(0xFFFBCA3F),
            Triple(322f to 20f, 342f to 20f, 332f to 44f) to Color(0xFFB9D84A),
            Triple(366f to 10f, 386f to 10f, 376f to 34f) to Color(0xFF865DEC),
        )
        pennants.forEach { (points, color) ->
            val p = Path().apply {
                moveTo(points.first.first, points.first.second)
                lineTo(points.second.first, points.second.second)
                lineTo(points.third.first, points.third.second)
                close()
            }
            drawPath(p, color = color, style = Fill)
        }

        // Serpentinas oficiales
        val pinkStreamer = Path().apply {
            moveTo(-30f, 150f)
            cubicTo(30f, 90f, 70f, 210f, 130f, 140f)
            cubicTo(190f, 70f, 210f, 70f, 250f, 120f)
        }
        drawPath(pinkStreamer, color = Color(0xFFF3B8D8), style = Stroke(width = 7f, cap = StrokeCap.Round))

        val yellowStreamer = Path().apply {
            moveTo(420f, 250f)
            cubicTo(360f, 220f, 370f, 320f, 310f, 300f)
            cubicTo(250f, 280f, 250f, 220f, 228f, 268f)
        }
        drawPath(yellowStreamer, color = Color(0xFFFFE08A), style = Stroke(width = 6f, cap = StrokeCap.Round))

        val greenStreamer = Path().apply {
            moveTo(-16f, 500f)
            cubicTo(34f, 460f, 56f, 552f, 108f, 510f)
        }
        drawPath(greenStreamer, color = Color(0xFFD6EA8C), style = Stroke(width = 5f, cap = StrokeCap.Round))

        val lavenderStreamer = Path().apply {
            moveTo(408f, 540f)
            cubicTo(366f, 500f, 338f, 582f, 296f, 540f)
        }
        drawPath(lavenderStreamer, color = Color(0xFFD9CCFA), style = Stroke(width = 5f, cap = StrokeCap.Round))

        // Confetti y figuras
        drawCircle(Color(0xFF1FBDC0), radius = 6f, center = Offset(322f, 150f))
        val pinkTri = Path().apply {
            moveTo(70f, 380f)
            lineTo(84f, 388f)
            lineTo(70f, 396f)
            close()
        }
        drawPath(pinkTri, color = Color(0xFFEA4EA9), style = Fill)
        drawCircle(Color(0xFFFBCA3F), radius = 4f, center = Offset(200f, 168f))
        drawCircle(Color(0xFF1FBDC0), radius = 4f, center = Offset(44f, 610f))
        drawCircle(Color(0xFFEA4EA9), radius = 3f, center = Offset(350f, 640f))
    }
}

// ==============================================================================
// s07 · INICIAR SESIÓN
// ==============================================================================

@Composable
internal fun RenderLoginScreenS07(
    viewModel: RosaFiestaViewModel?,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
    onSwitchAppMode: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var hasAttemptedLogin by remember { mutableStateOf(false) }
    var localLoginError by remember { mutableStateOf<String?>(null) }
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    val shouldValidateEmail = if (hasAttemptedLogin) true else email.isNotEmpty()
    val emailFieldError = if (shouldValidateEmail) InputValidator.validateLoginIdentifier(email) else null
    val shouldValidatePassword = if (hasAttemptedLogin) true else password.isNotEmpty()
    val passwordFieldError = if (shouldValidatePassword) InputValidator.validatePassword(password) else null

    LaunchedEffect(Unit) {
        viewModel?.clearFormError()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPersonalAtmosphereBackground()
            val s = size.width / 390f
            scale(s, s, Offset.Zero) {
                drawCircle(color = Color(0xFFCFE48A), radius = 26f, center = Offset(30f, 660f), style = Stroke(width = 2f))
                drawCircle(color = Color(0xFFE2CCF4), radius = 34f, center = Offset(356f, 740f), style = Stroke(width = 1.5f))
                val sq1 = Path().apply {
                    moveTo(60f, 752f)
                    lineTo(74f, 748f)
                    lineTo(80f, 768f)
                    lineTo(66f, 772f)
                    close()
                }
                drawPath(sq1, color = Color(0xFFC9B6F2), style = Stroke(width = 1.5f))
                val sq2 = Path().apply {
                    moveTo(260f, 772f)
                    lineTo(286f, 766f)
                    lineTo(296f, 788f)
                    lineTo(270f, 794f)
                    close()
                }
                drawPath(sq2, color = Color(0xFFF3C4D8), style = Stroke(width = 1.5f))
                drawSvgStar(center = Offset(190f, 798f), scale = 1f, color = Color(0xFFFBCA3F))

                val starGreen = Path().apply {
                    moveTo(16f, 470f)
                    lineTo(19f, 477f)
                    lineTo(26f, 480f)
                    lineTo(19f, 483f)
                    lineTo(16f, 490f)
                    lineTo(13f, 483f)
                    lineTo(6f, 480f)
                    lineTo(13f, 477f)
                    close()
                }
                drawPath(starGreen, color = Color(0xFFB9D84A), style = Fill)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .personalTopBarBackground(isStep1 = true)
                .padding(top = 14.dp, bottom = 10.dp, start = 20.dp, end = 14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x334650B4), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                    .clickable { onSelectScreenCode("BACK:s06") },
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BasicText(
                    text = "Hola de nuevo",
                    style = typo.heroTitle.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF151729),
                        lineHeight = 26.sp,
                    ),
                )
                BasicText(
                    text = "Entra a tus eventos.",
                    style = typo.body.copy(
                        fontSize = 13.5.sp,
                        color = RfThemeState.currentPalette.muted,
                        lineHeight = 18.sp,
                    ),
                )
            }

            BalloonClusterIllustration(
                modifier = Modifier.size(68.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 98.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            BasicText("Correo o usuario", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
            Spacer(Modifier.height(8.dp))
            val emailBorderColor = if (emailFieldError != null) RfColors.Error else RfThemeState.currentPalette.line
            val emailBorderWidth = if (emailFieldError != null) 1.5.dp else 1.dp
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(emailBorderWidth, emailBorderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RfIcon(
                        kind = RfIconKind.MAIL,
                        tint = if (emailFieldError != null) RfColors.Error else Color(0xFF62627A),
                        size = 20.dp,
                    )
                    BasicTextField(
                        value = email,
                        onValueChange = { nextValue ->
                            val cappedEmail = nextValue.take(InputValidator.MAX_EMAIL_LENGTH)
                            email = cappedEmail
                            localLoginError = null
                            viewModel?.setLoginEmail(cappedEmail)
                        },
                        singleLine = true,
                        textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFD63F98)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (email.isEmpty()) {
                                BasicText(
                                    text = "tucorreo@ejemplo.com o usuario",
                                    style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF9A98AD)),
                                )
                            }
                            innerTextField()
                        },
                    )
                }
            }
            if (emailFieldError != null) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = emailFieldError,
                    style = typo.caption.copy(fontSize = 12.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("Contraseña", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                BasicText(
                    text = "¿La olvidaste?",
                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD63F98), textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable { onSelectScreenCode("s09") },
                )
            }
            Spacer(Modifier.height(8.dp))
            val passwordBorderColor = if (passwordFieldError != null) RfColors.Error else RfThemeState.currentPalette.line
            val passwordBorderWidth = if (passwordFieldError != null) 1.5.dp else 1.dp
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(passwordBorderWidth, passwordBorderColor, RoundedCornerShape(16.dp))
                    .padding(start = 16.dp, end = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        RfIcon(
                            kind = RfIconKind.LOCK,
                            tint = if (passwordFieldError != null) RfColors.Error else Color(0xFF62627A),
                            size = 20.dp,
                        )
                        val loginPasswordVisualTransformation = if (showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        }
                        BasicTextField(
                            value = password,
                            onValueChange = { nextPassword ->
                                val cappedPassword = nextPassword.take(InputValidator.MAX_PASSWORD_LENGTH)
                                password = cappedPassword
                                localLoginError = null
                                viewModel?.setLoginPassword(cappedPassword)
                            },
                            singleLine = true,
                            visualTransformation = loginPasswordVisualTransformation,
                            textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                            cursorBrush = SolidColor(Color(0xFFD63F98)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (password.isEmpty()) {
                                        BasicText(
                                            text = "Ingresa tu contraseña (6 a 64 caracteres)",
                                            style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF9A98AD)),
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { showPassword = !showPassword },
                    ) {
                        RfIcon(kind = RfIconKind.EYE, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                    }
                }
            }
            if (passwordFieldError != null) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = passwordFieldError,
                    style = typo.caption.copy(fontSize = 12.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                )
            }

            val displayedLoginError = localLoginError ?: formError
            if (!displayedLoginError.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEDF1))
                        .border(1.dp, RfColors.Error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BasicText(
                        text = displayedLoginError,
                        style = typo.caption.copy(fontSize = 12.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            val loginButtonBrush = if (isSubmitting) {
                Brush.horizontalGradient(listOf(Color(0xFFEA4EA9).copy(alpha = 0.65f), Color(0xFF865DEC).copy(alpha = 0.65f)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)))
            }
            val loginButtonLabel = if (isSubmitting) "Ingresando..." else "Ingresar"

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(loginButtonBrush)
                    .clickable(enabled = !isSubmitting) {
                        hasAttemptedLogin = true
                        val trimmedIdentifier = email.trim()
                        val checkIdError = InputValidator.validateLoginIdentifier(trimmedIdentifier)
                        val checkPassError = InputValidator.validatePassword(password)
                        val firstError = checkIdError ?: checkPassError
                        if (firstError != null) {
                            localLoginError = firstError
                            viewModel?.soundPlayer?.playAlertError()
                        } else {
                            localLoginError = null
                            viewModel?.setLoginEmail(trimmedIdentifier)
                            viewModel?.setLoginPassword(password)
                            val success = viewModel?.submitLogin() ?: true
                            if (success) {
                                val isAdminOrStaff = viewModel?.isCurrentSessionAdminOrStaff() == true
                                if (isAdminOrStaff) {
                                    onSwitchAppMode()
                                } else {
                                    onSelectScreenCode("s14")
                                }
                            }
                        }
                    },
            ) {
                BasicText(loginButtonLabel, style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE6DEEA)))
                BasicText("o continúa con", style = typo.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE6DEEA)))
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSubmitting) {
                            val socialSuccess = viewModel?.authenticateSocialAccount("Google", "") == true
                            if (socialSuccess) {
                                onSelectScreenCode("s14")
                            }
                        },
                ) {
                    BasicText("Google", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSubmitting) {
                            val socialSuccess = viewModel?.authenticateSocialAccount("Apple", "") == true
                            if (socialSuccess) {
                                onSelectScreenCode("s14")
                            }
                        },
                ) {
                    BasicText("Apple", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                }
            }

            Spacer(Modifier.weight(1f).heightIn(min = 20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("¿No tienes cuenta? ", style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                BasicText(
                    text = "Regístrate",
                    style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD63F98), textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable { onSelectScreenCode("s08") },
                )
            }
        }
    }
}

// ==============================================================================
// ILUSTRACIONES AUTH & RECUPERACIÓN (s08 - s12)
// ==============================================================================

@Composable
private fun KeyIllustration(size: Dp = 140.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 120f
        drawCircle(color = Color(0xFFFFF1D6), radius = 40f * s, center = Offset(60f * s, 60f * s))
        drawCircle(
            color = Color(0xFFF5B53B),
            radius = 15f * s,
            center = Offset(42f * s, 60f * s),
            style = Stroke(width = 9f * s),
        )
        drawRoundRect(
            color = Color(0xFFF5B53B),
            topLeft = Offset(55f * s, 55.5f * s),
            size = Size(44f * s, 9f * s),
            cornerRadius = CornerRadius(3f * s, 3f * s),
        )
        drawRoundRect(
            color = Color(0xFFF5B53B),
            topLeft = Offset(82f * s, 63f * s),
            size = Size(6f * s, 11f * s),
            cornerRadius = CornerRadius(1.5f * s, 1.5f * s),
        )
        drawRoundRect(
            color = Color(0xFFF5B53B),
            topLeft = Offset(92f * s, 63f * s),
            size = Size(6f * s, 15f * s),
            cornerRadius = CornerRadius(1.5f * s, 1.5f * s),
        )
        val starPath = Path().apply {
            moveTo(96f * s, 26f * s)
            lineTo(98f * s, 31f * s)
            lineTo(103f * s, 33f * s)
            lineTo(98f * s, 35f * s)
            lineTo(96f * s, 40f * s)
            lineTo(94f * s, 35f * s)
            lineTo(89f * s, 33f * s)
            lineTo(94f * s, 31f * s)
            close()
        }
        drawPath(starPath, color = Color(0xFFEC4F97))
        drawCircle(color = Color(0xFF7650E8), radius = 3f * s, center = Offset(24f * s, 30f * s))
    }
}

@Composable
private fun PadlockIllustration(size: Dp = 112.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 120f
        drawCircle(color = Color(0xFFFCE4EF), radius = 42f * s, center = Offset(60f * s, 62f * s))
        val shacklePath = Path().apply {
            moveTo(44f * s, 56f * s)
            lineTo(44f * s, 42f * s)
            arcTo(
                rect = Rect(44f * s, 26f * s, 76f * s, 58f * s),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            lineTo(76f * s, 56f * s)
        }
        drawPath(
            shacklePath,
            color = Color(0xFF7650E8),
            style = Stroke(width = 8f * s, cap = StrokeCap.Round),
        )
        drawRoundRect(
            color = Color(0xFFEC4F97),
            topLeft = Offset(34f * s, 52f * s),
            size = Size(52f * s, 44f * s),
            cornerRadius = CornerRadius(12f * s, 12f * s),
        )
        drawCircle(color = Color.White, radius = 6f * s, center = Offset(60f * s, 70f * s))
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(57.5f * s, 72f * s),
            size = Size(5f * s, 12f * s),
            cornerRadius = CornerRadius(2.5f * s, 2.5f * s),
        )
        val starPath = Path().apply {
            moveTo(96f * s, 30f * s)
            lineTo(98f * s, 35f * s)
            lineTo(103f * s, 37f * s)
            lineTo(98f * s, 39f * s)
            lineTo(96f * s, 44f * s)
            lineTo(94f * s, 39f * s)
            lineTo(89f * s, 37f * s)
            lineTo(94f * s, 35f * s)
            close()
        }
        drawPath(starPath, color = Color(0xFFF5B53B))
        drawCircle(color = Color(0xFF2FB58F), radius = 3f * s, center = Offset(22f * s, 44f * s))
    }
}

@Composable
private fun ActiveAccountIllustration(size: Dp = 220.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 120f
        drawCircle(color = Color(0xFFDDF5EC), radius = 36f * s, center = Offset(60f * s, 60f * s))
        drawCircle(color = Color(0xFF2FB58F), radius = 26f * s, center = Offset(60f * s, 60f * s))
        val checkPath = Path().apply {
            moveTo(48f * s, 61f * s)
            lineTo(56f * s, 69f * s)
            lineTo(72f * s, 52f * s)
        }
        drawPath(
            checkPath,
            color = Color.White,
            style = Stroke(width = 6f * s, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawScopeRotate(20f, pivot = Offset(59.5f * s, 12f * s)) {
            drawRoundRect(
                color = Color(0xFFEC4F97),
                topLeft = Offset(56f * s, 6f * s),
                size = Size(7f * s, 12f * s),
                cornerRadius = CornerRadius(2f * s, 2f * s),
            )
        }
        drawCircle(color = Color(0xFFF5B53B), radius = 4f * s, center = Offset(98f * s, 28f * s))
        val tri1 = Path().apply {
            moveTo(104f * s, 66f * s)
            lineTo(114f * s, 71f * s)
            lineTo(104f * s, 76f * s)
            close()
        }
        drawPath(tri1, color = Color(0xFF7650E8))
        drawScopeRotate(-30f, pivot = Offset(89.5f * s, 102f * s)) {
            drawRoundRect(
                color = Color(0xFF4AA8E0),
                topLeft = Offset(86f * s, 96f * s),
                size = Size(7f * s, 12f * s),
                cornerRadius = CornerRadius(2f * s, 2f * s),
            )
        }
        drawCircle(color = Color(0xFFEC4F97), radius = 4f * s, center = Offset(38f * s, 106f * s))
        val tri2 = Path().apply {
            moveTo(16f * s, 72f * s)
            lineTo(6f * s, 68f * s)
            lineTo(14f * s, 61f * s)
            close()
        }
        drawPath(tri2, color = Color(0xFFF5B53B))
        drawScopeRotate(-35f, pivot = Offset(21.5f * s, 28f * s)) {
            drawRoundRect(
                color = Color(0xFF7650E8),
                topLeft = Offset(18f * s, 22f * s),
                size = Size(7f * s, 12f * s),
                cornerRadius = CornerRadius(2f * s, 2f * s),
            )
        }
        drawCircle(color = Color(0xFF2FB58F), radius = 2.5f * s, center = Offset(80f * s, 14f * s))
        drawCircle(color = Color(0xFF4AA8E0), radius = 2.5f * s, center = Offset(10f * s, 44f * s))
        drawCircle(color = Color(0xFFEC4F97), radius = 2.5f * s, center = Offset(110f * s, 46f * s))
    }
}

@Composable
private fun VerifiedMailIllustration(size: Dp = 220.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 120f
        drawOval(
            color = Color(0x142B1730),
            topLeft = Offset(20f * s, 100.5f * s),
            size = Size(80f * s, 7f * s),
        )
        drawRoundRect(
            color = Color(0xFFECE6FD),
            topLeft = Offset(22f * s, 46f * s),
            size = Size(76f * s, 52f * s),
            cornerRadius = CornerRadius(8f * s, 8f * s),
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(32f * s, 26f * s),
            size = Size(56f * s, 50f * s),
            cornerRadius = CornerRadius(5f * s, 5f * s),
        )
        drawRoundRect(
            color = Color(0xFFEFE4EC),
            topLeft = Offset(32f * s, 26f * s),
            size = Size(56f * s, 50f * s),
            cornerRadius = CornerRadius(5f * s, 5f * s),
            style = Stroke(width = 1.5f * s),
        )
        drawRoundRect(
            color = Color(0xFFF9B6D3),
            topLeft = Offset(40f * s, 36f * s),
            size = Size(28f * s, 5f * s),
            cornerRadius = CornerRadius(2.5f * s, 2.5f * s),
        )
        drawRoundRect(
            color = Color(0xFFEFE4EC),
            topLeft = Offset(40f * s, 46f * s),
            size = Size(40f * s, 3.5f * s),
            cornerRadius = CornerRadius(1.75f * s, 1.75f * s),
        )
        drawRoundRect(
            color = Color(0xFFEFE4EC),
            topLeft = Offset(40f * s, 54f * s),
            size = Size(34f * s, 3.5f * s),
            cornerRadius = CornerRadius(1.75f * s, 1.75f * s),
        )
        val flapPath = Path().apply {
            moveTo(22f * s, 54f * s)
            lineTo(60f * s, 80f * s)
            lineTo(98f * s, 54f * s)
            lineTo(98f * s, 90f * s)
            quadraticTo(98f * s, 98f * s, 90f * s, 98f * s)
            lineTo(30f * s, 98f * s)
            quadraticTo(22f * s, 98f * s, 22f * s, 90f * s)
            close()
        }
        drawPath(flapPath, color = Color(0xFFB9A6F7))
        drawLine(
            color = Color(0xFFA48EF2),
            start = Offset(22f * s, 96f * s),
            end = Offset(50f * s, 72f * s),
            strokeWidth = 2f * s,
        )
        drawLine(
            color = Color(0xFFA48EF2),
            start = Offset(98f * s, 96f * s),
            end = Offset(70f * s, 72f * s),
            strokeWidth = 2f * s,
        )
        drawCircle(color = Color(0xFF2FB58F), radius = 14f * s, center = Offset(92f * s, 38f * s))
        drawCircle(
            color = Color.White,
            radius = 14f * s,
            center = Offset(92f * s, 38f * s),
            style = Stroke(width = 3f * s),
        )
        val checkPath = Path().apply {
            moveTo(85.5f * s, 38.5f * s)
            lineTo(90f * s, 43f * s)
            lineTo(98f * s, 34.5f * s)
        }
        drawPath(
            checkPath,
            color = Color.White,
            style = Stroke(width = 3.4f * s, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

// ==============================================================================
// s08 · REGISTRO
// ==============================================================================

@Composable
internal fun RenderRegisterScreenS08(
    viewModel: RosaFiestaViewModel? = null,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var birthDate by remember { mutableStateOf("") }
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var hasAttemptedRegister by remember { mutableStateOf(false) }
    var localRegisterError by remember { mutableStateOf<String?>(null) }
    val vmRegisterPhone = viewModel?.registerPhone?.collectAsState()?.value ?: ""
    var phone by remember(vmRegisterPhone) { mutableStateOf(vmRegisterPhone) }
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    val shouldCheckFirst = if (hasAttemptedRegister) true else firstName.isNotEmpty()
    val firstNameError = if (shouldCheckFirst) InputValidator.validatePersonNamePart(firstName, "El nombre") else null
    val shouldCheckLast = if (hasAttemptedRegister) true else lastName.isNotEmpty()
    val lastNameError = if (shouldCheckLast) InputValidator.validatePersonNamePart(lastName, "El apellido") else null
    val shouldCheckUser = if (hasAttemptedRegister) true else username.isNotEmpty()
    val usernameError = if (shouldCheckUser) InputValidator.validateUsername(username) else null
    val shouldCheckPass = if (hasAttemptedRegister) true else password.isNotEmpty()
    val passwordError = if (shouldCheckPass) InputValidator.validatePassword(password) else null
    val shouldCheckEmail = if (hasAttemptedRegister) true else email.isNotEmpty()
    val emailError = if (shouldCheckEmail) InputValidator.validateEmail(email) else null
    val shouldCheckPhone = phone.isNotBlank()
    val phoneError = if (shouldCheckPhone) InputValidator.validatePhone(phone) else null
    val shouldCheckBirth = if (hasAttemptedRegister) true else birthDate.isNotEmpty()
    val birthDateError = if (shouldCheckBirth) InputValidator.validateBirthDate(birthDate, isRequired = true) else null

    LaunchedEffect(Unit) {
        viewModel?.clearFormError()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                        center = Offset(0f, 0f),
                        radius = size.width * 1.1f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                        center = Offset(size.width, size.height * 0.15f),
                        radius = size.width * 0.8f,
                    ),
                )
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .personalTopBarBackground(isStep1 = false)
                .padding(top = 14.dp, bottom = 10.dp, start = 20.dp, end = 14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x66B450B4))
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                    .clickable { onSelectScreenCode("BACK:s07") },
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 20.dp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BasicText(
                    "Crea tu cuenta",
                    style = typo.heroTitle.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = RfThemeState.currentPalette.ink,
                        lineHeight = 26.sp,
                    ),
                )
                BasicText(
                    "Te toma menos de un minuto.",
                    style = typo.body.copy(fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted),
                )
            }

            RegisterBalloonArchHeaderIllustration(
                modifier = Modifier.size(width = 82.dp, height = 64.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                val firstBorderColor = if (firstNameError != null) RfColors.Error else RfThemeState.currentPalette.line
                val firstBorderWidth = if (firstNameError != null) 1.5.dp else 1.dp
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(firstBorderWidth, firstBorderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                ) {
                    BasicTextField(
                        value = firstName,
                        onValueChange = { nextFirstName ->
                            firstName = nextFirstName.take(InputValidator.MAX_PERSON_NAME_LENGTH)
                            localRegisterError = null
                            viewModel?.clearFormError()
                        },
                        singleLine = true,
                        textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFD63F98)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (firstName.isEmpty()) {
                                BasicText("Nombre (2–40)", style = typo.body.copy(fontSize = 14.5.sp, color = RfThemeState.currentPalette.muted))
                            }
                            inner()
                        },
                    )
                }
                val lastBorderColor = if (lastNameError != null) RfColors.Error else RfThemeState.currentPalette.line
                val lastBorderWidth = if (lastNameError != null) 1.5.dp else 1.dp
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(lastBorderWidth, lastBorderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                ) {
                    BasicTextField(
                        value = lastName,
                        onValueChange = { nextLastName ->
                            lastName = nextLastName.take(InputValidator.MAX_PERSON_NAME_LENGTH)
                            localRegisterError = null
                            viewModel?.clearFormError()
                        },
                        singleLine = true,
                        textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFD63F98)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (lastName.isEmpty()) {
                                BasicText("Apellido (2–40)", style = typo.body.copy(fontSize = 14.5.sp, color = RfThemeState.currentPalette.muted))
                            }
                            inner()
                        },
                    )
                }
            }
            val nameRowError = firstNameError ?: lastNameError
            if (nameRowError != null) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = nameRowError,
                    style = typo.caption.copy(fontSize = 11.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                val userBorderColor = if (usernameError != null) RfColors.Error else RfThemeState.currentPalette.line
                val userBorderWidth = if (usernameError != null) 1.5.dp else 1.dp
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(userBorderWidth, userBorderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RfIcon(
                            kind = RfIconKind.USER,
                            tint = if (usernameError != null) RfColors.Error else Color(0xFF62627A),
                            size = 18.dp,
                        )
                        BasicTextField(
                            value = username,
                            onValueChange = { nextUsername ->
                                username = nextUsername.take(InputValidator.MAX_USERNAME_LENGTH)
                                localRegisterError = null
                                viewModel?.clearFormError()
                            },
                            singleLine = true,
                            textStyle = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                            cursorBrush = SolidColor(Color(0xFFD63F98)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (username.isEmpty()) {
                                    BasicText(
                                        "Usuario (3–24)",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                                inner()
                            },
                        )
                    }
                }

                val passBorderColor = if (passwordError != null) RfColors.Error else RfThemeState.currentPalette.line
                val passBorderWidth = if (passwordError != null) 1.5.dp else 1.dp
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(passBorderWidth, passBorderColor, RoundedCornerShape(16.dp))
                        .padding(start = 12.dp, end = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            RfIcon(
                                kind = RfIconKind.LOCK,
                                tint = if (passwordError != null) RfColors.Error else Color(0xFF62627A),
                                size = 18.dp,
                            )
                            val registerPasswordVisualTransformation = if (showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            }
                            BasicTextField(
                                value = password,
                                onValueChange = { nextPassword ->
                                    password = nextPassword.take(InputValidator.MAX_PASSWORD_LENGTH)
                                    localRegisterError = null
                                    viewModel?.clearFormError()
                                },
                                singleLine = true,
                                visualTransformation = registerPasswordVisualTransformation,
                                textStyle = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                cursorBrush = SolidColor(Color(0xFFD63F98)),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (password.isEmpty()) {
                                            BasicText(
                                                "Clave (6–64)",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                            )
                                        }
                                        inner()
                                    }
                                },
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { showPassword = !showPassword },
                        ) {
                            RfIcon(kind = RfIconKind.EYE, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                        }
                    }
                }
            }
            val credentialsRowError = usernameError ?: passwordError
            if (credentialsRowError != null) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = credentialsRowError,
                    style = typo.caption.copy(fontSize = 11.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                )
            }

            Spacer(Modifier.height(10.dp))

            val emailBorderColor = if (emailError != null) RfColors.Error else RfThemeState.currentPalette.line
            val emailBorderWidth = if (emailError != null) 1.5.dp else 1.dp
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(emailBorderWidth, emailBorderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(
                        kind = RfIconKind.MAIL,
                        tint = if (emailError != null) RfColors.Error else Color(0xFF62627A),
                        size = 20.dp,
                    )
                    BasicTextField(
                        value = email,
                        onValueChange = { nextEmail ->
                            email = nextEmail.take(InputValidator.MAX_EMAIL_LENGTH)
                            localRegisterError = null
                            viewModel?.clearFormError()
                        },
                        singleLine = true,
                        textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFD63F98)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (email.isEmpty()) {
                                BasicText("Correo electrónico (ej. usuario@dominio.com)", style = typo.body.copy(fontSize = 14.5.sp, color = RfThemeState.currentPalette.muted))
                            }
                            inner()
                        },
                    )
                }
            }
            if (emailError != null) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = emailError,
                    style = typo.caption.copy(fontSize = 11.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                )
            }

            Spacer(Modifier.height(10.dp))

            val phoneBorderColor = if (phoneError != null) RfColors.Error else RfThemeState.currentPalette.line
            val phoneBorderWidth = if (phoneError != null) 1.5.dp else 1.dp
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(phoneBorderWidth, phoneBorderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(
                        kind = RfIconKind.PHONE,
                        tint = if (phoneError != null) RfColors.Error else Color(0xFF62627A),
                        size = 20.dp,
                    )
                    BasicTextField(
                        value = phone,
                        onValueChange = { nextPhone ->
                            val cappedPhone = nextPhone.take(InputValidator.MAX_PHONE_LENGTH)
                            phone = cappedPhone
                            localRegisterError = null
                            viewModel?.setRegisterPhone(cappedPhone)
                            viewModel?.clearFormError()
                        },
                        singleLine = true,
                        textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFD63F98)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerField ->
                            if (phone.isEmpty()) {
                                BasicText("Teléfono (ej. 809-555-0102)", style = typo.body.copy(fontSize = 14.5.sp, color = RfThemeState.currentPalette.muted))
                            }
                            innerField()
                        },
                    )
                }
            }
            if (phoneError != null) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = phoneError,
                    style = typo.caption.copy(fontSize = 11.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                )
            }

            Spacer(Modifier.height(10.dp))

            val birthBorderColor = if (birthDateError != null) RfColors.Error else RfThemeState.currentPalette.line
            val birthBorderWidth = if (birthDateError != null) 1.5.dp else 1.dp
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(birthBorderWidth, birthBorderColor, RoundedCornerShape(16.dp))
                    .padding(start = 16.dp, end = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showBirthDatePicker = true },
                    ) {
                        RfIcon(
                            kind = RfIconKind.CALENDAR,
                            tint = if (birthDateError != null) RfColors.Error else Color(0xFF62627A),
                            size = 20.dp,
                        )
                    }
                    RfMaskedDateField(
                        value = birthDate,
                        onValueChange = { nextBirthDate ->
                            birthDate = nextBirthDate.take(InputValidator.MAX_BIRTH_DATE_LENGTH)
                            localRegisterError = null
                            viewModel?.clearFormError()
                        },
                        onOpenCalendar = { showBirthDatePicker = true },
                        placeholder = "Fecha de nacimiento (DD/MM/AAAA)",
                        hasError = birthDateError != null,
                        isAdmin = false,
                        compactInline = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (birthDateError != null) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = birthDateError,
                    style = typo.caption.copy(fontSize = 11.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp, top = 8.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (acceptedTerms) Color(0xFFD63F98) else RfThemeState.currentPalette.line)
                        .clickable {
                            acceptedTerms = !acceptedTerms
                            localRegisterError = null
                        },
                ) {
                    if (acceptedTerms) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText(
                        text = "Acepto los términos de uso del servicio:",
                        style = typo.body.copy(fontSize = 13.5.sp, lineHeight = 19.sp, color = RfThemeState.currentPalette.muted),
                        modifier = Modifier.clickable {
                            acceptedTerms = !acceptedTerms
                            localRegisterError = null
                        },
                    )
                    BasicText(
                        text = "Términos y condiciones y Política de privacidad",
                        style = typo.body.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD63F98),
                            textDecoration = TextDecoration.Underline,
                        ),
                        modifier = Modifier.clickable { onSelectScreenCode("s87") },
                    )
                }
            }

            val displayedRegError = localRegisterError ?: formError
            if (!displayedRegError.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEDF1))
                        .border(1.dp, RfColors.Error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BasicText(
                        text = displayedRegError,
                        style = typo.caption.copy(fontSize = 12.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val registerButtonBrush = if (isSubmitting) {
                Brush.horizontalGradient(listOf(Color(0xFFEA4EA9).copy(alpha = 0.65f), Color(0xFF865DEC).copy(alpha = 0.65f)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)))
            }
            val registerButtonLabel = if (isSubmitting) "Creando cuenta..." else "Crear cuenta"

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(registerButtonBrush)
                    .clickable(enabled = !isSubmitting) {
                        hasAttemptedRegister = true
                        val checkFirstError = InputValidator.validatePersonNamePart(firstName, "El nombre")
                        val checkLastError = InputValidator.validatePersonNamePart(lastName, "El apellido")
                        val checkUserError = InputValidator.validateUsername(username)
                        val checkPassError = InputValidator.validatePassword(password)
                        val checkEmailError = InputValidator.validateEmail(email)
                        val checkPhoneError = if (phone.isNotBlank()) InputValidator.validatePhone(phone) else null
                        val checkBirthError = InputValidator.validateBirthDate(birthDate, isRequired = true)
                        val termsError = if (!acceptedTerms) "Debes aceptar los Términos y condiciones para continuar." else null
                        val firstValidationError = checkFirstError
                            ?: checkLastError
                            ?: checkUserError
                            ?: checkPassError
                            ?: checkEmailError
                            ?: checkPhoneError
                            ?: checkBirthError
                            ?: termsError
                        if (firstValidationError != null) {
                            localRegisterError = firstValidationError
                            viewModel?.soundPlayer?.playAlertError()
                        } else {
                            localRegisterError = null
                            viewModel?.setRegisterPhone(phone)
                            val registered = viewModel?.submitRegistration(
                                firstName = firstName,
                                lastName = lastName,
                                username = username,
                                email = email,
                                password = password,
                                birthDate = birthDate,
                                acceptedTerms = acceptedTerms,
                                phone = phone,
                            ) ?: true
                            if (registered) {
                                onSelectScreenCode("s14")
                            }
                        }
                    },
            ) {
                BasicText(registerButtonLabel, style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE6DEEA)))
                BasicText("o regístrate con", style = typo.body.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE6DEEA)))
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSubmitting) {
                            val socialSuccess = viewModel?.authenticateSocialAccount("Google", "") == true
                            if (socialSuccess) {
                                onPrimaryAction()
                            }
                        },
                ) {
                    BasicText("Google", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSubmitting) {
                            val socialSuccess = viewModel?.authenticateSocialAccount("Apple", "") == true
                            if (socialSuccess) {
                                onPrimaryAction()
                            }
                        },
                ) {
                    BasicText("Apple", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                }
            }

            Spacer(Modifier.weight(1f).heightIn(min = 18.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("¿Ya tienes cuenta? ", style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                BasicText(
                    text = "Inicia sesión",
                    style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F), textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable { onSelectScreenCode("s07") },
                )
            }
        }

        AnimatedVisibility(
            visible = showBirthDatePicker,
            enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.94f),
            exit = fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.94f),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
        ) {
            BirthDateCalendarModal(
                currentBirthDate = birthDate,
                onSelectBirthDate = { selectedFormattedDate ->
                    birthDate = selectedFormattedDate
                    showBirthDatePicker = false
                },
                onDismiss = { showBirthDatePicker = false },
            )
        }
    }
}

@Composable
private fun RegisterBalloonArchHeaderIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 120f
        val scaleY = size.height / 96f
        val uniformScale = minOf(scaleX, scaleY)
        val offsetX = (size.width - 120f * uniformScale) / 2f
        val offsetY = (size.height - 96f * uniformScale) / 2f - 18f * uniformScale
        scale(uniformScale, uniformScale, Offset.Zero) {
            val shiftX = offsetX / uniformScale
            val shiftY = offsetY / uniformScale
            drawOval(
                color = Color(0xFF2B1730).copy(alpha = 0.08f),
                topLeft = Offset(shiftX + 10f, shiftY + 106.5f),
                size = Size(100f, 7f),
            )
            drawCircle(Color(0xFFF9B6D3), radius = 6.5f, center = Offset(shiftX + 9f, shiftY + 91f))
            drawCircle(Color(0xFFFFD98A), radius = 6.5f, center = Offset(shiftX + 26.6f, shiftY + 60f))
            drawCircle(Color(0xFFB9A6F7), radius = 6.5f, center = Offset(shiftX + 60f, shiftY + 47f))
            drawCircle(Color(0xFFA8D8F5), radius = 6.5f, center = Offset(shiftX + 93.4f, shiftY + 60f))
            drawCircle(Color(0xFF9BE3C9), radius = 6.5f, center = Offset(shiftX + 111f, shiftY + 91f))

            drawCircle(Color(0xFFEC4F97), radius = 10.5f, center = Offset(shiftX + 18f, shiftY + 104f))
            drawCircle(Color(0xFFF5B53B), radius = 10.5f, center = Offset(shiftX + 20.5f, shiftY + 86f))
            drawCircle(Color(0xFF7650E8), radius = 10.5f, center = Offset(shiftX + 28f, shiftY + 72f))
            drawCircle(Color(0xFF4AA8E0), radius = 10.5f, center = Offset(shiftX + 39f, shiftY + 62.5f))
            drawCircle(Color(0xFF2FB58F), radius = 10.5f, center = Offset(shiftX + 52.7f, shiftY + 58f))
            drawCircle(Color(0xFFEC4F97), radius = 10.5f, center = Offset(shiftX + 67.3f, shiftY + 58f))
            drawCircle(Color(0xFFF5B53B), radius = 10.5f, center = Offset(shiftX + 81f, shiftY + 62.5f))
            drawCircle(Color(0xFF7650E8), radius = 10.5f, center = Offset(shiftX + 92f, shiftY + 72f))
            drawCircle(Color(0xFF4AA8E0), radius = 10.5f, center = Offset(shiftX + 99.5f, shiftY + 86f))
            drawCircle(Color(0xFF2FB58F), radius = 10.5f, center = Offset(shiftX + 102f, shiftY + 104f))

            val highlightColor = Color.White.copy(alpha = 0.55f)
            drawOval(highlightColor, topLeft = Offset(shiftX + 12.6f, shiftY + 96.6f), size = Size(4.8f, 6.8f))
            drawOval(highlightColor, topLeft = Offset(shiftX + 33.6f, shiftY + 55.1f), size = Size(4.8f, 6.8f))
            drawOval(highlightColor, topLeft = Offset(shiftX + 61.6f, shiftY + 50.6f), size = Size(4.8f, 6.8f))
            drawOval(highlightColor, topLeft = Offset(shiftX + 86.6f, shiftY + 64.6f), size = Size(4.8f, 6.8f))
            drawOval(highlightColor, topLeft = Offset(shiftX + 96.6f, shiftY + 96.6f), size = Size(4.8f, 6.8f))

            val starPath = Path().apply {
                moveTo(shiftX + 60f, shiftY + 22f)
                lineTo(shiftX + 61.6f, shiftY + 26.4f)
                lineTo(shiftX + 66f, shiftY + 28f)
                lineTo(shiftX + 61.6f, shiftY + 29.6f)
                lineTo(shiftX + 60f, shiftY + 34f)
                lineTo(shiftX + 58.4f, shiftY + 29.6f)
                lineTo(shiftX + 54f, shiftY + 28f)
                lineTo(shiftX + 58.4f, shiftY + 26.4f)
                close()
            }
            drawPath(starPath, color = Color(0xFFF5B53B), style = Fill)
        }
    }
}

@Composable
private fun BirthDateCalendarModal(
    currentBirthDate: String,
    onSelectBirthDate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val initialParts = currentBirthDate.split("/")
    val parsedMonth = initialParts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 12) ?: 8
    val parsedYear = initialParts.getOrNull(2)?.toIntOrNull()?.coerceIn(1940, 2008) ?: 1998
    val initialMaxDays = computeDaysInGregorianMonth(parsedYear, parsedMonth)
    val parsedDay = initialParts.getOrNull(0)?.toIntOrNull()?.coerceIn(1, initialMaxDays) ?: 15

    var selectedDay by remember { mutableStateOf(parsedDay) }
    var selectedMonth by remember { mutableStateOf(parsedMonth) }
    var selectedYear by remember { mutableStateOf(parsedYear) }

    val monthNames = remember {
        listOf(
            "Enero",
            "Febrero",
            "Marzo",
            "Abril",
            "Mayo",
            "Junio",
            "Julio",
            "Agosto",
            "Septiembre",
            "Octubre",
            "Noviembre",
            "Diciembre",
        )
    }

    val daysInSelectedMonth = computeDaysInGregorianMonth(selectedYear, selectedMonth)
    val leadingWeekdayOffset = computeGregorianMondayFirstOffset(selectedYear, selectedMonth)
    val clampedSelectedDay = selectedDay.coerceIn(1, daysInSelectedMonth)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151729).copy(alpha = 0.45f))
            .clickable { onDismiss() }
            .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x66151729))
                .clip(RoundedCornerShape(24.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    BasicText(
                        "Fecha de nacimiento",
                        style = typo.heroTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        "Selecciona día, mes y año",
                        style = typo.body.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F2F8))
                        .clickable { onDismiss() },
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 18.dp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F5FB))
                        .padding(horizontal = 6.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable {
                                val nextMonth = if (selectedMonth == 1) 12 else selectedMonth - 1
                                selectedMonth = nextMonth
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(selectedYear, nextMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 14.dp)
                    }
                    BasicText(
                        monthNames[selectedMonth - 1],
                        maxLines = 1,
                        style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable {
                                val nextMonth = if (selectedMonth == 12) 1 else selectedMonth + 1
                                selectedMonth = nextMonth
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(selectedYear, nextMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.ink, size = 14.dp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F5FB))
                        .padding(horizontal = 6.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable {
                                val nextYear = (selectedYear - 1).coerceAtLeast(1940)
                                selectedYear = nextYear
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(nextYear, selectedMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 14.dp)
                    }
                    BasicText(
                        selectedYear.toString(),
                        style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4FD8)),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable {
                                val nextYear = (selectedYear + 1).coerceAtMost(2008)
                                selectedYear = nextYear
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(nextYear, selectedMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.ink, size = 14.dp)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                val quickYears = listOf(1980, 1985, 1990, 1995, 1998, 2002, 2008)
                quickYears.forEach { yearOption ->
                    val isSelectedYear = yearOption == selectedYear
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelectedYear) Color(0xFF865DEC) else Color(0xFFF3EFF8))
                            .clickable {
                                selectedYear = yearOption
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(yearOption, selectedMonth))
                            }
                            .padding(horizontal = 10.dp),
                    ) {
                        BasicText(
                            yearOption.toString(),
                            style = typo.body.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelectedYear) Color.White else Color(0xFF62627A),
                            ),
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val weekHeaders = listOf("L", "M", "M", "J", "V", "S", "D")
                weekHeaders.forEach { dayHeader ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp),
                    ) {
                        BasicText(
                            dayHeader,
                            style = typo.body.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A98AD)),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val calendarSlots = List<Int?>(leadingWeekdayOffset) { null } + (1..daysInSelectedMonth).toList()
                calendarSlots.chunked(7).forEach { weekDays ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        for (slotIndex in 0 until 7) {
                            val dayValue = weekDays.getOrNull(slotIndex)
                            if (dayValue != null) {
                                val isDaySelected = dayValue == clampedSelectedDay
                                val dayBgBrush = if (isDaySelected) {
                                    Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                                } else {
                                    SolidColor(RfThemeState.currentPalette.bg)
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(dayBgBrush)
                                        .clickable {
                                            selectedDay = dayValue
                                        },
                                ) {
                                    BasicText(
                                        dayValue.toString(),
                                        style = typo.body.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isDaySelected) Color.White else Color(0xFF1A1A2E),
                                        ),
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            val formattedDay = clampedSelectedDay.toString().padStart(2, '0')
            val formattedMonth = selectedMonth.toString().padStart(2, '0')
            val formattedPreview = "$formattedDay/$formattedMonth/$selectedYear"

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x99C846B4))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                    .clickable {
                        onSelectBirthDate(formattedPreview)
                    },
            ) {
                BasicText(
                    "Confirmar $formattedPreview",
                    style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
        }
    }
}

// ==============================================================================
// s09, s10, s11, s12, s13 · FLUJO DE RECUPERACIÓN Y CONFIRMACIÓN
// ==============================================================================

@Composable
internal fun RenderRecoveryAndConfirmationScreens(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel? = null,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    var recoveryEmail by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var recoveryEmailError by remember { mutableStateOf<String?>(null) }
    val formError = viewModel?.formError?.collectAsState()?.value
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value
    LaunchedEffect(spec.code) {
        recoveryEmailError = null
        viewModel?.clearFormError()
    }

    when (spec.code) {
        "s09" -> {
            val liveRecError = if (recoveryEmail.isNotEmpty()) InputValidator.validateEmail(recoveryEmail) else recoveryEmailError
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RfThemeState.currentPalette.bg)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.1f,
                            ),
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                                center = Offset(size.width, size.height * 0.15f),
                                radius = size.width * 0.8f,
                            ),
                        )
                    },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .zIndex(2f)
                        .personalTopBarBackground(isStep1 = false)
                        .padding(top = 14.dp, bottom = 10.dp, start = 20.dp, end = 14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x66B450B4))
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:s07") },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(
                            "¿Olvidaste tu contraseña?",
                            style = typo.heroTitle.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = RfThemeState.currentPalette.ink,
                                lineHeight = 25.sp,
                            ),
                        )
                        BasicText(
                            "Te enviamos un enlace a tu correo.",
                            style = typo.body.copy(fontSize = 13.5.sp, lineHeight = 18.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    KeyIllustration(size = 68.dp)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 104.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                ) {
                    BasicText("Correo electrónico", style = typo.fieldLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                    Spacer(Modifier.height(8.dp))
                    val recBorderColor = if (liveRecError != null) RfColors.Error else RfThemeState.currentPalette.line
                    val recBorderWidth = if (liveRecError != null) 1.5.dp else 1.dp
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .rfInputShadow(RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(recBorderWidth, recBorderColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            RfIcon(
                                kind = RfIconKind.MAIL,
                                tint = if (liveRecError != null) RfColors.Error else Color(0xFF62627A),
                                size = 20.dp,
                            )
                            BasicTextField(
                                value = recoveryEmail,
                                onValueChange = { nextEmail ->
                                    recoveryEmail = nextEmail.take(InputValidator.MAX_EMAIL_LENGTH)
                                    recoveryEmailError = null
                                },
                                singleLine = true,
                                textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                cursorBrush = SolidColor(Color(0xFFD63F98)),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (recoveryEmail.isEmpty()) {
                                        BasicText("tucorreo@ejemplo.com", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF9A98AD)))
                                    }
                                    innerTextField()
                                },
                            )
                        }
                    }
                    if (liveRecError != null) {
                        Spacer(Modifier.height(6.dp))
                        BasicText(
                            text = liveRecError,
                            style = typo.caption.copy(fontSize = 12.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                        )
                    } else if (!formError.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        BasicText(
                            text = formError,
                            style = typo.caption.copy(fontSize = 12.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4), ambientColor = Color.Transparent)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                            .clickable {
                                val trimmedRec = recoveryEmail.trim()
                                val emailValidation = InputValidator.validateEmail(trimmedRec)
                                if (emailValidation != null) {
                                    recoveryEmailError = emailValidation
                                    viewModel?.soundPlayer?.playAlertError()
                                } else {
                                    recoveryEmailError = null
                                    viewModel?.setLoginEmail(trimmedRec)
                                    val sent = if (viewModel != null) {
                                        viewModel.requestPasswordResetOtp(trimmedRec)
                                    } else {
                                        true
                                    }
                                    if (sent) {
                                        onSelectScreenCode("s12")
                                    }
                                }
                            },
                    ) {
                        BasicText("Enviar enlace", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.INFO, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                        BasicText("Si no lo ves, revisa la carpeta de spam.", style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Spacer(Modifier.weight(1f).heightIn(min = 24.dp))

                    BasicText(
                        text = "Volver a iniciar sesión",
                        style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F), textDecoration = TextDecoration.Underline),
                        modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onSelectScreenCode("s07") },
                    )
                }
            }
        }

        "s10" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RfThemeState.currentPalette.bg)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.1f,
                            ),
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                                center = Offset(size.width, size.height * 0.15f),
                                radius = size.width * 0.8f,
                            ),
                        )
                    },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .zIndex(2f)
                        .personalTopBarBackground(isStep1 = false)
                        .padding(top = 14.dp, bottom = 10.dp, start = 20.dp, end = 14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x66B450B4))
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:s09") },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(
                            "Crea una nueva contraseña",
                            style = typo.heroTitle.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = RfThemeState.currentPalette.ink,
                                lineHeight = 25.sp,
                            ),
                        )
                        BasicText(
                            "Elige una que no uses en otras apps.",
                            style = typo.body.copy(fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    PadlockIllustration(size = 68.dp)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 104.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                ) {
                    BasicText("Código de verificación (6 dígitos)", style = typo.fieldLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .rfInputShadow(RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            RfIcon(kind = RfIconKind.SHIELD, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                            BasicTextField(
                                value = otpCode,
                                onValueChange = { nextOtp ->
                                    otpCode = nextOtp.filter { character -> character.isDigit() }.take(6)
                                    viewModel?.clearFormError()
                                },
                                singleLine = true,
                                textStyle = typo.body.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                                cursorBrush = SolidColor(Color(0xFFD63F98)),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (otpCode.isEmpty()) {
                                            BasicText("000000", style = typo.body.copy(fontSize = 15.sp, letterSpacing = 2.sp, color = Color(0xFF9A98AD)))
                                        }
                                        innerField()
                                    }
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    BasicText("Nueva contraseña", style = typo.fieldLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .rfInputShadow(RoundedCornerShape(16.dp), isActive = true)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(2.dp, Color(0xFFB754CB), RoundedCornerShape(16.dp))
                            .padding(start = 16.dp, end = 6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                RfIcon(kind = RfIconKind.LOCK, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                                val newPasswordVisualTransformation = if (showNewPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                }
                                BasicTextField(
                                    value = newPassword,
                                    onValueChange = { nextPass ->
                                        newPassword = nextPass.take(InputValidator.MAX_PASSWORD_LENGTH)
                                        viewModel?.clearFormError()
                                    },
                                    singleLine = true,
                                    visualTransformation = newPasswordVisualTransformation,
                                    textStyle = typo.body.copy(
                                        fontSize = 15.sp,
                                        color = RfThemeState.currentPalette.ink,
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFD63F98)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (newPassword.isEmpty()) {
                                                BasicText("Crea tu contraseña", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF9A98AD)))
                                            }
                                            innerTextField()
                                        }
                                    },
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(44.dp).clickable { showNewPassword = !showNewPassword },
                            ) {
                                RfIcon(kind = RfIconKind.EYE, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val hasMinLength = newPassword.length >= 8
                    val hasNumber = newPassword.any { char -> char.isDigit() }
                    val hasSymbol = newPassword.any { char -> !char.isLetterOrDigit() }
                    val metCount = listOf(newPassword.length >= 6, hasMinLength, hasNumber, hasSymbol).count { met -> met }
                    val strengthLabel = when {
                        newPassword.isEmpty() -> "Ingresa una clave"
                        metCount <= 1 -> "Débil"
                        metCount == 2 -> "Media"
                        metCount == 3 -> "Buena"
                        else -> "Fuerte"
                    }
                    val activeBarColor = if (metCount <= 1) Color(0xFFE07A2A) else Color(0xFF8DB52C)

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                            for (barIndex in 1..4) {
                                val barFill = if (barIndex <= metCount) activeBarColor else Color(0xFFE6DEEA)
                                Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(barFill))
                            }
                        }
                        BasicText(strengthLabel, style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                    }

                    Spacer(Modifier.height(10.dp))

                    val checkCirclePath = remember { androidx.compose.ui.graphics.vector.PathParser().parsePathString("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M8 12.5l2.8 2.8L16 10").toPath() }
                    val emptyCirclePath = remember { androidx.compose.ui.graphics.vector.PathParser().parsePathString("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0").toPath() }

                    val lengthColor = if (hasMinLength) Color(0xFF4E6A10) else Color(0xFF62627A)
                    val numberColor = if (hasNumber) Color(0xFF4E6A10) else Color(0xFF62627A)
                    val symbolColor = if (hasSymbol) Color(0xFF4E6A10) else Color(0xFF62627A)

                    BasicText(
                        text = "Mínimo 8 caracteres e incluye al menos un número",
                        style = typo.caption.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.muted),
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val s = size.width / 24f
                            scale(s, s, Offset.Zero) {
                                val path = if (hasMinLength) checkCirclePath else emptyCirclePath
                                drawPath(path, lengthColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                        BasicText("Al menos 8 caracteres", style = typo.body.copy(fontSize = 14.sp, color = lengthColor))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val s = size.width / 24f
                            scale(s, s, Offset.Zero) {
                                val path = if (hasNumber) checkCirclePath else emptyCirclePath
                                drawPath(path, numberColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                        BasicText("Incluye un número", style = typo.body.copy(fontSize = 14.sp, color = numberColor))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val s = size.width / 24f
                            scale(s, s, Offset.Zero) {
                                val path = if (hasSymbol) checkCirclePath else emptyCirclePath
                                drawPath(path, symbolColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                        BasicText("Incluye un símbolo (opcional)", style = typo.body.copy(fontSize = 14.sp, color = symbolColor))
                    }

                    Spacer(Modifier.height(22.dp))

                    BasicText("Confirmar contraseña", style = typo.fieldLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .rfInputShadow(RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                            .padding(start = 16.dp, end = 6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                RfIcon(kind = RfIconKind.LOCK, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                                val confirmPasswordVisualTransformation = if (showConfirmPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                }
                                BasicTextField(
                                    value = confirmPassword,
                                    onValueChange = { nextPass ->
                                        confirmPassword = nextPass.take(InputValidator.MAX_PASSWORD_LENGTH)
                                        viewModel?.clearFormError()
                                    },
                                    singleLine = true,
                                    visualTransformation = confirmPasswordVisualTransformation,
                                    textStyle = typo.body.copy(
                                        fontSize = 15.sp,
                                        color = RfThemeState.currentPalette.ink,
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFD63F98)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (confirmPassword.isEmpty()) {
                                                BasicText("Repite tu contraseña", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF9A98AD)))
                                            }
                                            innerTextField()
                                        }
                                    },
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(44.dp).clickable { showConfirmPassword = !showConfirmPassword },
                            ) {
                                RfIcon(kind = RfIconKind.EYE, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                            }
                        }
                    }

                    if (!formError.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        BasicText(
                            text = formError,
                            style = typo.caption.copy(fontSize = 12.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold),
                        )
                    }

                    Spacer(Modifier.weight(1f).heightIn(min = 24.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                            .clickable {
                                val targetEmail = recoveryEmail.ifBlank { viewModel?.loginEmail?.value.orEmpty() }
                                val resetOk = viewModel?.submitPasswordReset(
                                    identifier = targetEmail,
                                    newPassword = newPassword,
                                    confirmPassword = confirmPassword,
                                    otpCode = otpCode,
                                ) ?: true
                                if (resetOk) {
                                    onSelectScreenCode("s07")
                                }
                            },
                    ) {
                        BasicText("Guardar contraseña", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }

        "s11" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RfThemeState.currentPalette.bg)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.1f,
                            ),
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                                center = Offset(size.width, size.height * 0.15f),
                                radius = size.width * 0.8f,
                            ),
                        )
                    },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 36.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                ) {
                    ActiveAccountIllustration(size = 220.dp)

                    Spacer(Modifier.height(10.dp))

                    BasicText(
                        "¡Tu cuenta está activa!",
                        style = typo.heroTitle.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color = RfThemeState.currentPalette.ink,
                            lineHeight = 29.sp,
                            textAlign = TextAlign.Center,
                        ),
                    )

                    Spacer(Modifier.height(10.dp))

                    BasicText(
                        "Ya puedes iniciar sesión y empezar a planificar tu evento.",
                        style = typo.body.copy(fontSize = 16.sp, lineHeight = 24.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )

                    Spacer(Modifier.weight(1f).heightIn(min = 28.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                            .clickable { onSelectScreenCode("s07") },
                    ) {
                        BasicText("Ir a iniciar sesión", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }

        "s12" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RfThemeState.currentPalette.bg)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.1f,
                            ),
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                                center = Offset(size.width, size.height * 0.15f),
                                radius = size.width * 0.8f,
                            ),
                        )
                        val cx = size.width / 2f
                        val cy = 220.dp.toPx()
                        drawCircle(color = Color(0xFFEFE8FD), radius = 170.dp.toPx(), center = Offset(cx, cy), alpha = 0.55f)
                        drawCircle(color = Color(0xFFEFE8FD), radius = 120.dp.toPx(), center = Offset(cx, cy))
                    },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 36.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                ) {
                    VerifiedMailIllustration(size = 220.dp)

                    Spacer(Modifier.height(10.dp))

                    BasicText(
                        "Verifica tu correo",
                        style = typo.heroTitle.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.6).sp,
                            color = RfThemeState.currentPalette.ink,
                            lineHeight = 31.sp,
                            textAlign = TextAlign.Center,
                        ),
                    )

                    Spacer(Modifier.height(10.dp))

                    BasicText(
                        "Ingresa el código de 6 dígitos que enviamos a tu bandeja.",
                        style = typo.body.copy(fontSize = 16.sp, lineHeight = 24.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )

                    Spacer(Modifier.height(18.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .rfInputShadow(RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.5.dp, Color(0xFFB754CB), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                    ) {
                        BasicTextField(
                            value = verificationOtp,
                            onValueChange = { nextCode ->
                                verificationOtp = nextCode.filter { character -> character.isDigit() }.take(6)
                                viewModel?.clearFormError()
                            },
                            singleLine = true,
                            textStyle = typo.heroTitle.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 6.sp,
                                color = RfThemeState.currentPalette.ink,
                                textAlign = TextAlign.Center,
                            ),
                            cursorBrush = SolidColor(Color(0xFFD63F98)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerField ->
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                                    if (verificationOtp.isEmpty()) {
                                        BasicText(
                                            text = "000000",
                                            style = typo.heroTitle.copy(
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 6.sp,
                                                color = Color(0xFF9A98AD),
                                                textAlign = TextAlign.Center,
                                            ),
                                        )
                                    }
                                    innerField()
                                }
                            },
                        )
                    }

                    if (!formError.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        BasicText(
                            text = formError,
                            style = typo.caption.copy(fontSize = 12.5.sp, color = RfColors.Error, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                        )
                    } else if (!formFeedback.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        BasicText(
                            text = formFeedback,
                            style = typo.caption.copy(fontSize = 12.5.sp, color = Color(0xFF4E6A10), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    BasicText(
                        text = "Reenviar código",
                        style = typo.body.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD63F98),
                            textDecoration = TextDecoration.Underline,
                        ),
                        modifier = Modifier.clickable {
                            val resendTarget = recoveryEmail.ifBlank { viewModel?.loginEmail?.value.orEmpty() }
                            viewModel?.requestPasswordResetOtp(resendTarget)
                        },
                    )

                    Spacer(Modifier.weight(1f).heightIn(min = 24.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                            .clickable {
                                if (viewModel != null) {
                                    viewModel.verifyPasswordResetOtpCode(verificationOtp) { verified ->
                                        if (verified) {
                                            onSelectScreenCode("s10")
                                        }
                                    }
                                } else {
                                    onSelectScreenCode("s10")
                                }
                            },
                    ) {
                        BasicText("Verificar código", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }

        "s13" -> {
            val s13ProfileName = viewModel?.profileName?.collectAsState()?.value ?: "Invitado Rosa Fiesta"
            val s13FirstName = s13ProfileName.substringBefore(" ").ifBlank { "Cliente" }
            val s13AvatarInitial = s13FirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "R"
            val s13Email = viewModel?.profileEmail?.collectAsState()?.value ?: "cliente@rosafiesta.do"

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RfThemeState.currentPalette.bg)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.1f,
                            ),
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                                center = Offset(size.width, size.height * 0.15f),
                                radius = size.width * 0.8f,
                            ),
                        )
                    },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 36.dp, bottom = 20.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE6F2)),
                        ) {
                            BasicText(
                                s13AvatarInitial,
                                style = typo.heroTitle.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFC2338F),
                                ),
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        BasicText(
                            "¡Hola de nuevo, $s13FirstName!",
                            style = typo.heroTitle.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.6).sp,
                                color = RfThemeState.currentPalette.ink,
                                lineHeight = 31.sp,
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        BasicText(s13Email, style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Spacer(Modifier.height(28.dp))

                    val observedEvents = viewModel?.events?.collectAsState()?.value
                    val s13Events = remember(observedEvents, s13Email, viewModel) {
                        viewModel?.clientEventsForCurrentUser() ?: emptyList()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0x66C850AA))
                            .clip(RoundedCornerShape(22.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                            .padding(8.dp),
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                            ) {
                                BasicText("Tus eventos", style = typo.heroTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink))
                                BasicText("${s13Events.size} activos", style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            }

                            if (s13Events.isEmpty()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 18.dp),
                                ) {
                                    BasicText(
                                        text = "Aún no tienes eventos activos en tu cuenta.",
                                        style = typo.body.copy(
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RfThemeState.currentPalette.ink,
                                            textAlign = TextAlign.Center,
                                        ),
                                    )
                                    BasicText(
                                        text = "Explora nuestro catálogo o diseña tu celebración paso a paso con Rosa IA.",
                                        style = typo.secondary.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            color = RfThemeState.currentPalette.muted,
                                            textAlign = TextAlign.Center,
                                        ),
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFDE6F2))
                                                .clickable { onSelectScreenCode("s18") },
                                        ) {
                                            BasicText(
                                                text = "Explorar catálogo",
                                                style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)),
                                            )
                                        }
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFEFE8FD))
                                                .clickable { onSelectScreenCode("s26") },
                                        ) {
                                            BasicText(
                                                text = "Rosa IA",
                                                style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4FD8)),
                                            )
                                        }
                                    }
                                }
                            } else {
                                s13Events.take(3).forEachIndexed { eventIndex, eventItem ->
                                    val dateParts = eventItem.dateLabel.split(" ")
                                    val dayText = dateParts.firstOrNull { token -> token.any { character -> character.isDigit() } } ?: "${23 + eventIndex}"
                                    val monthText = dateParts.lastOrNull { token -> token.all { character -> character.isLetter() } }?.take(3)?.uppercase() ?: "OCT"
                                    val dateBg = when (eventIndex % 3) {
                                        0 -> Color(0xFFFDE6F2)
                                        1 -> Color(0xFFEFE8FD)
                                        else -> Color(0xFFDDF4F3)
                                    }
                                    val isConfirmed = eventItem.statusLabel.contains("Confirmad", ignoreCase = true)
                                    val isPendingPay = eventItem.statusLabel.contains("pagar", ignoreCase = true)
                                    val tagBg = when {
                                        isConfirmed -> Color(0xFFEEF6D6)
                                        isPendingPay -> Color(0xFFFFF2C9)
                                        else -> RfThemeState.currentPalette.chipBg
                                    }
                                    val tagColor = when {
                                        isConfirmed -> Color(0xFF4E6A10)
                                        isPendingPay -> Color(0xFF7A5700)
                                        else -> Color(0xFF62627A)
                                    }
                                    EventActiveItemRow(
                                        day = dayText,
                                        month = monthText,
                                        dateBg = dateBg,
                                        title = eventItem.title,
                                        tagText = eventItem.statusLabel,
                                        tagBg = tagBg,
                                        tagColor = tagColor,
                                        timing = eventItem.venue,
                                        onClick = {
                                            viewModel?.selectEvent(eventItem.id)
                                            onSelectScreenCode("s40")
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f).heightIn(min = 24.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(56.dp)
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                            .clickable { onSelectScreenCode("s14") },
                    ) {
                        BasicText("Continuar al inicio", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventActiveItemRow(
    day: String,
    month: String,
    dateBg: Color,
    title: String,
    tagText: String,
    tagBg: Color,
    tagColor: Color,
    timing: String,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 52.dp, height = 56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(dateBg),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BasicText(
                    day,
                    style = typo.heroTitle.copy(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RfThemeState.currentPalette.ink,
                        lineHeight = 22.sp,
                    ),
                )
                BasicText(
                    month,
                    style = typo.body.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                title,
                maxLines = 1,
                style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(tagBg)
                        .padding(horizontal = 9.dp),
                ) {
                    BasicText(tagText, style = typo.body.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tagColor))
                }
                BasicText(timing, style = typo.body.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
            }
        }

        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 20.dp)
    }
}

// ==============================================================================
// s01 · CATÁLOGO (MODO INVITADO)
// ==============================================================================

@Composable
internal fun RenderGuestCatalogS01(
    onSelectScreenCode: (String) -> Unit,
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val vmSelectedCategory = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todas"
    var selectedCategory by remember(vmSelectedCategory) {
        mutableStateOf(vmSelectedCategory.ifBlank { "Todas" })
    }
    val vmSearchQuery = viewModel?.searchQuery?.collectAsState()?.value ?: ""
    var searchQuery by remember(vmSearchQuery) { mutableStateOf(vmSearchQuery) }
    val observedArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val vmSortOrder = viewModel?.searchSortOrder?.collectAsState()?.value ?: "Más populares"
    var localSortOrder by remember(vmSortOrder) { mutableStateOf(vmSortOrder) }
    var showSortSelector by remember { mutableStateOf(false) }
    val observedMaxPrice = viewModel?.maxPriceFilterDop?.collectAsState()?.value ?: Int.MAX_VALUE
    val observedOnlyAvailable = viewModel?.onlyAvailableFilter?.collectAsState()?.value ?: false
    val favoriteIds = viewModel?.favorites?.collectAsState()?.value ?: emptySet()
    val isInlineVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
    val isSearchMicActive = if (isInlineVoiceListening) voiceOrigin == VoiceInputOrigin.SEARCH else false

    val filteredItems: List<CatalogArticle> = remember(
        observedArticles,
        selectedCategory,
        searchQuery,
        localSortOrder,
        observedMaxPrice,
        observedOnlyAvailable,
        viewModel,
    ) {
        if (viewModel != null) {
            viewModel.filteredCatalogArticles(
                categoryOverride = selectedCategory,
                queryOverride = searchQuery,
            )
        } else {
            val baseFiltered = CanonicalSampleData.articles.filter { article ->
                val visibleMatch = article.visibleInCatalog
                val categoryMatch = if (selectedCategory == "Todas") {
                    true
                } else {
                    article.category.contains(selectedCategory, ignoreCase = true)
                }
                val queryMatch = if (searchQuery.isBlank()) {
                    true
                } else {
                    article.name.contains(searchQuery, ignoreCase = true)
                }
                val matchesCatAndQuery = if (categoryMatch) queryMatch else false
                if (visibleMatch) matchesCatAndQuery else false
            }
            when (localSortOrder) {
                "Menor precio" -> baseFiltered.sortedBy { article -> article.pricePerDayDop }
                "Mayor precio" -> baseFiltered.sortedByDescending { article -> article.pricePerDayDop }
                "Alfabético" -> baseFiltered.sortedBy { article -> article.name }
                else -> baseFiltered.sortedByDescending { article -> article.totalStock }
            }
        }
    }

    val sortOptions = remember {
        listOf("Más populares", "Menor precio", "Mayor precio", "Alfabético")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                        center = Offset(0f, 0f),
                        radius = size.width * 1.1f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                        center = Offset(size.width, size.height * 0.15f),
                        radius = size.width * 0.8f,
                    ),
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                BasicText(
                    "Catálogo",
                    style = typo.heroTitle.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x66B450B4))
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("s81") },
                ) {
                    RfIcon(kind = RfIconKind.FILTER, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                    .padding(start = 16.dp, end = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        RfIcon(kind = RfIconKind.SEARCH, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                BasicText(
                                    "Buscar sillas, mesas, flores…",
                                    style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { updatedQuery ->
                                    searchQuery = updatedQuery
                                    viewModel?.setSearchQuery(updatedQuery)
                                },
                                singleLine = true,
                                textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSearchMicActive) {
                                    Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                                } else {
                                    SolidColor(Color(0xFFFDE6F2))
                                },
                            )
                            .clickable {
                                viewModel?.toggleInlineVoiceSearch()
                            },
                    ) {
                        val micTint = if (isSearchMicActive) Color.White else Color(0xFFC2338F)
                        RfIcon(kind = RfIconKind.MIC, tint = micTint, size = 20.dp)
                    }
                }
            }

            InlineVoiceSearchBanner(
                viewModel = viewModel,
                onApplyVoiceQuery = { spokenQuery ->
                    searchQuery = spokenQuery
                    viewModel?.setSearchQuery(spokenQuery)
                },
                modifier = Modifier.padding(top = 10.dp),
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .horizontalScroll(rememberScrollState()),
            ) {
                val baseCategories = listOf("Todas", "Iluminación", "Floral", "Globos", "Mobiliario")
                val categories = if (baseCategories.contains(selectedCategory)) {
                    baseCategories
                } else {
                    listOf("Todas", selectedCategory) + baseCategories.drop(1)
                }
                categories.forEach { categoryOption ->
                    val isSelected = categoryOption == selectedCategory
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                } else {
                                    SolidColor(RfThemeState.currentPalette.surface)
                                },
                            )
                            .border(1.dp, if (isSelected) Color.Transparent else RfThemeState.currentPalette.line, CircleShape)
                            .clickable {
                                selectedCategory = categoryOption
                                viewModel?.setSelectedCategory(categoryOption)
                            }
                            .padding(horizontal = 15.dp),
                    ) {
                        BasicText(
                            text = categoryOption,
                            style = typo.body.copy(
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF5F6377),
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().height(32.dp),
            ) {
                BasicText(
                    text = "${filteredItems.size} artículos",
                    style = typo.secondary.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSortSelector = !showSortSelector }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = localSortOrder,
                        style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                    )
                    RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                }
            }

            AnimatedVisibility(visible = showSortSelector) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                ) {
                    sortOptions.forEach { sortOption ->
                        val isSortActive = sortOption == localSortOrder
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSortActive) Color(0xFFEFE8FD) else RfThemeState.currentPalette.surface)
                                .border(
                                    1.dp,
                                    if (isSortActive) Color(0xFF865DEC) else RfThemeState.currentPalette.line,
                                    RoundedCornerShape(999.dp),
                                )
                                .clickable {
                                    localSortOrder = sortOption
                                    viewModel?.setSearchSortOrder(sortOption)
                                    showSortSelector = false
                                }
                                .padding(horizontal = 12.dp),
                        ) {
                            BasicText(
                                text = sortOption,
                                style = typo.body.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSortActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSortActive) Color(0xFF7A4FD8) else RfThemeState.currentPalette.muted,
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (filteredItems.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = RfThemeState.currentPalette.muted, size = 28.dp)
                    BasicText(
                        text = "No encontramos artículos con estos filtros",
                        style = typo.heroTitle.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RfThemeState.currentPalette.ink,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    BasicText(
                        text = "Prueba otra categoría o limpia la búsqueda para ver todo el catálogo.",
                        style = typo.secondary.copy(
                            fontSize = 13.5.sp,
                            color = RfThemeState.currentPalette.muted,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC))))
                            .clickable {
                                selectedCategory = "Todas"
                                searchQuery = ""
                                localSortOrder = "Más populares"
                                viewModel?.resetCatalogFilters()
                                viewModel?.setSearchQuery("")
                            }
                            .padding(horizontal = 18.dp),
                    ) {
                        BasicText(
                            text = "Limpiar filtros",
                            style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredItems.chunked(2).forEachIndexed { rowIndex, rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEachIndexed { columnIndex, article ->
                                val cardBg = when ((rowIndex * 2 + columnIndex) % 4) {
                                    0 -> Color(0xFFEFE8FD)
                                    1 -> Color(0xFFFDE6F2)
                                    2 -> Color(0xFFEEF6D6)
                                    else -> Color(0xFFFFF2C9)
                                }
                                val isFav = favoriteIds.contains(article.id)
                                GuestProductCard(
                                    title = article.name,
                                    desc = article.description,
                                    price = "RD$ ${article.pricePerDayDop}",
                                    photoKey = article.photo.toPhotoKey(),
                                    bg = cardBg,
                                    isFavorite = isFav,
                                    onCardClick = {
                                        viewModel?.selectArticle(article.id)
                                        onSelectScreenCode("s19")
                                    },
                                    onGuestRestrictedAction = {
                                        viewModel?.enterGuestMode()
                                        onSelectScreenCode("s75")
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(110.dp))
                }
            }
        }
    }
}

@Composable
private fun GuestProductCard(
    title: String,
    desc: String,
    price: String,
    photoKey: RfPhotoKey,
    bg: Color,
    isFavorite: Boolean = false,
    onCardClick: () -> Unit = {},
    onGuestRestrictedAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
                    .background(bg),
            ) {
                RfPhoto(
                    photoKey = photoKey,
                    contentDescription = title,
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f))
                        .clickable(onClick = onGuestRestrictedAction),
                ) {
                    val heartTint = if (isFavorite) Color(0xFFE04FAF) else RfThemeState.currentPalette.ink
                    RfIcon(kind = RfIconKind.HEART, tint = heartTint, filled = isFavorite, size = 18.dp)
                }
            }

            Column(modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
                BasicText(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                Spacer(Modifier.height(3.dp))
                BasicText(desc, maxLines = 1, overflow = TextOverflow.Ellipsis, style = typo.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        BasicText(price, style = typo.heroTitle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        BasicText(" /día", style = typo.secondary.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))),
                            )
                            .clickable(onClick = onGuestRestrictedAction),
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 18.dp)
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s02 · FAVORITOS (MODO INVITADO)
// ==============================================================================

@Composable
internal fun RenderGuestFavoritesS02(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val favoriteIds = viewModel?.favorites?.collectAsState()?.value ?: emptySet()
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val favoritedArticles = remember(favoriteIds, allArticles) {
        allArticles.filter { article -> favoriteIds.contains(article.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                    .clickable { onSelectScreenCode("BACK:s01") },
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }
            BasicText(
                text = "Favoritos",
                style = typo.heroTitle.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RfThemeState.currentPalette.ink,
                ),
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEFE8FD))
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.surface),
                ) {
                    RfIcon(
                        kind = RfIconKind.USER,
                        tint = Color(0xFF7A4FD8),
                        size = 22.dp,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    BasicText(
                        text = "Guarda tus favoritos en todos tus dispositivos.",
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            lineHeight = 19.6.sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .clickable { onSelectScreenCode("s07") },
                        ) {
                            BasicText(
                                text = "Iniciar sesión",
                                style = typo.body.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7A4FD8),
                                ),
                            )
                            RfIcon(
                                kind = RfIconKind.CHEVRON_RIGHT,
                                tint = Color(0xFF7A4FD8),
                                size = 16.dp,
                            )
                        }
                        BasicText(
                            text = "Crear cuenta",
                            style = typo.body.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD63F98),
                                textDecoration = TextDecoration.Underline,
                            ),
                            modifier = Modifier.clickable { onSelectScreenCode("s08") },
                        )
                    }
                }
            }
        }

        if (favoritedArticles.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            BasicText(
                text = "${favoritedArticles.size} guardados",
                style = typo.secondary.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                favoritedArticles.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEachIndexed { columnIndex, article ->
                            val cardBg = when ((rowIndex * 2 + columnIndex) % 4) {
                                0 -> Color(0xFFEFE8FD)
                                1 -> Color(0xFFFDE6F2)
                                2 -> Color(0xFFEEF6D6)
                                else -> Color(0xFFFFF2C9)
                            }
                            GuestProductCard(
                                title = article.name,
                                desc = article.description,
                                price = "RD$ ${article.pricePerDayDop}",
                                photoKey = article.photo.toPhotoKey(),
                                bg = cardBg,
                                isFavorite = true,
                                onCardClick = {
                                    viewModel?.selectArticle(article.id)
                                    onSelectScreenCode("s19")
                                },
                                onGuestRestrictedAction = {
                                    viewModel?.toggleFavorite(article.id)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Spacer(Modifier.height(64.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                HeartOrbitIllustration(
                    modifier = Modifier.size(190.dp),
                )

                Spacer(Modifier.height(8.dp))

                BasicText(
                    text = "Aún no tienes favoritos",
                    style = typo.heroTitle.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )

                Spacer(Modifier.height(8.dp))

                BasicText(
                    text = "Toca el corazón en cualquier artículo y lo guardamos aquí para más tarde.",
                    style = typo.body.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.5.sp,
                        color = RfThemeState.currentPalette.muted,
                        textAlign = TextAlign.Center,
                    ),
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(220.dp)
                        .height(54.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x667850A0))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s01") },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(
                            kind = RfIconKind.CATALOG,
                            tint = RfThemeState.currentPalette.ink,
                            size = 20.dp,
                        )
                        BasicText(
                            text = "Explorar catálogo",
                            style = typo.body.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                }
            }
        }
    }
}
