package com.jeudry.rosafiesta.ui

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize

// ==============================================================================
// p00 · SPLASH (APP PERSONAL)
// ==============================================================================

@Composable
internal fun RenderPersonalSplashP00(onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
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
            .clickable(onClick = onPrimaryAction),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGarlandAndConfettiPersonal()
        }

        // Centro: Logo con halo doble + Título + Subtítulo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 264.dp),
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
internal fun RenderAdminSplashA00(onPrimaryAction: () -> Unit) {
    val typo = LocalRfTypography.current
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
            .clickable(onClick = onPrimaryAction),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = this.size.width / 390f
            scale(s, s, Offset.Zero) {
                drawCircle(Color.White.copy(alpha = 0.07f), radius = 110f, center = Offset(320f, 140f))
                drawCircle(Color.White.copy(alpha = 0.06f), radius = 120f, center = Offset(60f, 720f))
                drawLine(Color.White.copy(alpha = 0.08f), start = Offset(0f, 640f), end = Offset(390f, 640f))
                drawLine(Color.White.copy(alpha = 0.06f), start = Offset(0f, 680f), end = Offset(390f, 680f))
                drawSvgStar(center = Offset(300f, 250f), scale = 0.9f, color = Color(0xFFFFD45C))
                drawCircle(Color(0xFFD6EA8C), radius = 4f, center = Offset(84f, 250f))
            }
        }

        // Centro: Logo con halo doble + Título + Tag ADMINISTRACIÓN
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 264.dp),
        ) {
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

            Spacer(Modifier.height(10.dp))

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

        // Barra de progreso inferior (y = 762dp)
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
                    .fillMaxWidth(0.55f)
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
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val step = when (spec.code) {
        "s03" -> 1
        "s04" -> 2
        "s05" -> 3
        else -> 1
    }

    val stepTag = when (step) {
        1 -> "01 · Catálogo"
        2 -> "02 · Rosa IA"
        else -> "03 · WhatsApp"
    }

    val stepColor = when (step) {
        1 -> Color(0xFFD63F98)
        2 -> Color(0xFF7A4FD8)
        else -> Color(0xFF4E6A10)
    }

    val stepTitle = when (step) {
        1 -> "Mira todo lo que tenemos"
        2 -> "Tu asistente para planificar"
        else -> "Confirma directo, sin trámites"
    }

    val stepSubtitle = when (step) {
        1 -> "Navega por nuestro catálogo de decoración y equipo para eventos. Sin registrarte, sin compromiso."
        2 -> "Dinos qué evento planeas y Rosa te sugiere artículos, categorías y más, paso a paso."
        else -> "Comparte tu lista por WhatsApp con María, la dueña, y confirma todo en minutos."
    }

    val nextScreen = when (step) {
        1 -> "s04"
        2 -> "s05"
        else -> "s06"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Barra superior: Logo + Rosa Fiesta + Botón Omitir
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, start = 20.dp, end = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfLogo(customSize = 36.dp)

                Row {
                    BasicText(
                        text = "Rosa ",
                        style = typo.sheetOrEmptyTitle.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF151729),
                        ),
                    )
                    BasicText(
                        text = "Fiesta",
                        style = typo.sheetOrEmptyTitle.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD63F98),
                        ),
                    )
                }
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
                        color = Color(0xFF62627A),
                    ),
                )
            }
        }

        // Burbuja central ilustrada (y = 118dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .offset(y = 118.dp)
                .padding(horizontal = 20.dp),
        ) {
            when (step) {
                1 -> OnboardingIllustrationStep1()
                2 -> OnboardingIllustrationStep2()
                3 -> OnboardingIllustrationStep3()
            }
        }

        // Bloque de texto descriptivo (y = 496dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 496.dp)
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

            Spacer(Modifier.height(8.dp))

            BasicText(
                text = stepTitle,
                style = typo.heroTitle.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF151729),
                    lineHeight = 34.sp,
                ),
            )

            Spacer(Modifier.height(12.dp))

            BasicText(
                text = stepSubtitle,
                style = typo.body.copy(
                    fontSize = 16.sp,
                    color = Color(0xFF5F6377),
                    lineHeight = 24.sp,
                ),
            )
        }

        // Navegación inferior: Dots + Botón Circular Siguiente
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 28.dp, end = 28.dp, bottom = 44.dp),
        ) {
            // Indicador de 4 pasos
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in 1..4) {
                    val isActive = i == step
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when (step) {
                                        1 -> Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                                        2 -> Brush.horizontalGradient(listOf(Color(0xFF865DEC), Color(0xFF6664E9)))
                                        else -> Brush.horizontalGradient(listOf(Color(0xFF8DB52C), Color(0xFF2FB58F)))
                                    },
                                ),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCD3E4)),
                        )
                    }
                }
            }

            // Botón circular de avance
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
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

@Composable
private fun OnboardingIllustrationStep1() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFFF9D3EA), Color(0xFFE4D6FB))),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Banderines superiores
            val arch = Path().apply {
                moveTo(0f, 6f)
                quadraticTo(w / 2f, 50f, w, 6f)
            }
            drawPath(arch, color = Color(0x331A1A2E), style = Stroke(width = 1.3f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Miniatura de mobiliario real
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RfPhoto(
                    photoKey = RfPhotoKey.SILLA_TIFFANY,
                    contentDescription = "Silla Tiffany",
                    cornerRadius = 16.dp,
                    modifier = Modifier.size(100.dp).border(1.dp, Color.White, RoundedCornerShape(16.dp)),
                )
                RfPhoto(
                    photoKey = RfPhotoKey.CENTRO_FLORAL,
                    contentDescription = "Centro Floral",
                    cornerRadius = 16.dp,
                    modifier = Modifier.size(100.dp).border(1.dp, Color.White, RoundedCornerShape(16.dp)),
                )
                RfPhoto(
                    photoKey = RfPhotoKey.ARCO_ROSA,
                    contentDescription = "Arco Rosa",
                    cornerRadius = 16.dp,
                    modifier = Modifier.size(100.dp).border(1.dp, Color.White, RoundedCornerShape(16.dp)),
                )
            }
        }
    }
}

@Composable
private fun OnboardingIllustrationStep2() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(36.dp))
            .background(Color(0xFFEFE8FD)),
    ) {
        // Rosa Avatar en la parte superior
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 18.dp),
        ) {
            RosaAvatar(size = RosaAvatarSize.HERO, glow = true)
        }

        // Burbujas de diálogo
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 130.dp),
        ) {
            // Mensaje de Rosa
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0x1F000000)),
            ) {
                BasicText(
                    text = "¿Qué evento quieres celebrar?",
                    style = LocalRfTypography.current.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                )
            }

            // Respuesta del usuario
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 4.dp, bottomStart = 18.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC))),
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                BasicText(
                    text = "Una boda en un jardín",
                    style = LocalRfTypography.current.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                )
            }

            // Recomendación con fotos
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                    .background(Color.White)
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(
                        text = "¡Me encanta! Te sugiero:",
                        style = LocalRfTypography.current.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfPhoto(
                            photoKey = RfPhotoKey.SILLA_TIFFANY,
                            contentDescription = "Silla",
                            cornerRadius = 8.dp,
                            modifier = Modifier.size(54.dp),
                        )
                        RfPhoto(
                            photoKey = RfPhotoKey.CENTRO_FLORAL,
                            contentDescription = "Flores",
                            cornerRadius = 8.dp,
                            modifier = Modifier.size(54.dp),
                        )
                        RfPhoto(
                            photoKey = RfPhotoKey.ARCO_ROSA,
                            contentDescription = "Arco",
                            cornerRadius = 8.dp,
                            modifier = Modifier.size(54.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingIllustrationStep3() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(36.dp))
            .background(Color(0xFFEEF6D6)),
    ) {
        // Tarjeta inclinada de WhatsApp
        Box(
            modifier = Modifier
                .padding(20.dp)
                .rotate(-2f)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x33AA46A0))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    text = "Tu lista",
                    style = LocalRfTypography.current.sheetOrEmptyTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                )
                BasicText(
                    text = "Boda Real Rosa & Dorado",
                    style = LocalRfTypography.current.secondary.copy(fontSize = 12.sp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Sillas Tiffany", style = LocalRfTypography.current.body.copy(fontSize = 13.sp))
                    BasicText("×40", style = LocalRfTypography.current.secondary.copy(fontSize = 13.sp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Arco de globos", style = LocalRfTypography.current.body.copy(fontSize = 13.sp))
                    BasicText("×1", style = LocalRfTypography.current.secondary.copy(fontSize = 13.sp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Centros de mesa", style = LocalRfTypography.current.body.copy(fontSize = 13.sp))
                    BasicText("×8", style = LocalRfTypography.current.secondary.copy(fontSize = 13.sp))
                }

                Spacer(Modifier.height(4.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2E9E5B)),
                ) {
                    BasicText(
                        text = "Enviar por WhatsApp",
                        style = LocalRfTypography.current.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
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
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGarlandAndConfettiPersonal()
        }

        // Centro: Logo con halo doble + Título + Subtítulo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 238.dp)
                .padding(horizontal = 32.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(116.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color(0x14EA4EA9), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .background(Color(0x0D865DEC), CircleShape),
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
                        color = Color(0xFF151729),
                    ),
                )
                BasicText(
                    text = "Fiesta",
                    style = typo.heroTitle.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEA4EA9),
                    ),
                )
            }

            Spacer(Modifier.height(12.dp))

            BasicText(
                text = "Todo para tu evento, guiado por inteligencia artificial.",
                style = typo.body.copy(
                    fontSize = 17.sp,
                    color = Color(0xFF62627A),
                    textAlign = TextAlign.Center,
                    lineHeight = 25.sp,
                ),
            )
        }

        // Navegación y CTAs inferiores (bottom 40dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            // Indicador del 4to paso
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
            ) {
                Box(Modifier.size(6.dp).background(Color(0xFFE3DCE8), CircleShape))
                Box(Modifier.size(6.dp).background(Color(0xFFE3DCE8), CircleShape))
                Box(Modifier.size(6.dp).background(Color(0xFFE3DCE8), CircleShape))
                Box(
                    Modifier
                        .width(28.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                )
            }

            // Botón primario Comenzar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
                        ),
                    )
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x99C846B4))
                    .clickable { onSelectScreenCode("s07") },
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

            // Botón secundario Ya tengo cuenta
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(16.dp))
                    .clickable { onSelectScreenCode("s07") },
            ) {
                BasicText(
                    text = "Ya tengo cuenta",
                    style = typo.body.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }
        }
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
) {
    val typo = LocalRfTypography.current
    var email by remember { mutableStateOf("v2_tester@example.com") }
    var password by remember { mutableStateOf("contrasena12") }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Botón volver
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = 20.dp, y = 56.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), CircleShape)
                .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x334650B4))
                .clickable { onSelectScreenCode("s06") },
        ) {
            RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 150.dp)
                .padding(horizontal = 24.dp),
        ) {
            BasicText(
                text = "Hola de nuevo",
                style = typo.heroTitle.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF151729),
                ),
            )
            Spacer(Modifier.height(8.dp))
            BasicText(
                text = "Entra para ver tus eventos y cotizaciones.",
                style = typo.body.copy(fontSize = 16.sp, color = Color(0xFF5F6377)),
            )

            Spacer(Modifier.height(28.dp))

            // Campo Correo
            BasicText("Correo o usuario", style = typo.fieldLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
            Spacer(Modifier.height(8.dp))
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = Color(0xFF5F6377), size = 20.dp)
                    BasicText(email, style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Campo Contraseña
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText("Contraseña", style = typo.fieldLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                BasicText(
                    text = "¿La olvidaste?",
                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C5CF0)),
                    modifier = Modifier.clickable { onSelectScreenCode("s09") },
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RfIcon(kind = RfIconKind.SHIELD, tint = Color(0xFF5F6377), size = 20.dp)
                        BasicText(if (showPassword) password else "••••••••••••", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                    }
                    Box(modifier = Modifier.clickable { showPassword = !showPassword }) {
                        RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF5F6377), size = 20.dp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Botón Ingresar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))),
                    )
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x995B6CF0))
                    .clickable {
                        viewModel?.submitLogin()
                        onPrimaryAction()
                    },
            ) {
                BasicText("Ingresar", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }

            Spacer(Modifier.height(24.dp))

            // Separador social
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE6DEEA)))
                BasicText("o continúa con", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE6DEEA)))
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)),
                ) {
                    BasicText("Google", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)),
                ) {
                    BasicText("Apple", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                }
            }
        }

        // Footer "¿No tienes cuenta? Regístrate"
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
        ) {
            BasicText("¿No tienes cuenta? ", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377)))
            BasicText(
                text = "Regístrate",
                style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0)),
                modifier = Modifier.clickable { onSelectScreenCode("s08") },
            )
        }
    }
}

// ==============================================================================
// s08 · REGISTRO
// ==============================================================================

@Composable
internal fun RenderRegisterScreenS08(
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = 20.dp, y = 56.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), CircleShape)
                .clickable { onSelectScreenCode("s07") },
        ) {
            RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 128.dp)
                .padding(horizontal = 24.dp),
        ) {
            BasicText("Crea tu cuenta", style = typo.heroTitle.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
            Spacer(Modifier.height(6.dp))
            BasicText("Te toma menos de un minuto.", style = typo.body.copy(fontSize = 16.sp, color = Color(0xFF5F6377)))

            Spacer(Modifier.height(20.dp))

            // Nombre y Apellido
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
                ) {
                    BasicText("Camila", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                }
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
                ) {
                    BasicText("Rodríguez", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Usuario
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = Color(0xFF5F6377), size = 20.dp)
                    BasicText("camilarodriguez", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Correo
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF5F6377), size = 20.dp)
                    BasicText("camila@rosafiesta.do", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Contraseña
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.SHIELD, tint = Color(0xFF5F6377), size = 20.dp)
                    BasicText("••••••••••••", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botón Crear cuenta
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onPrimaryAction() },
            ) {
                BasicText("Crear cuenta", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }

        // Footer "¿Ya tienes cuenta? Inicia sesión"
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
        ) {
            BasicText("¿Ya tienes cuenta? ", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377)))
            BasicText(
                text = "Inicia sesión",
                style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0)),
                modifier = Modifier.clickable { onSelectScreenCode("s07") },
            )
        }
    }
}

// ==============================================================================
// s09, s10, s11, s12, s13 · FLUJO DE RECUPERACIÓN Y CONFIRMACIÓN
// ==============================================================================

@Composable
internal fun RenderRecoveryAndConfirmationScreens(
    spec: ScreenSpec,
    onPrimaryAction: () -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    when (spec.code) {
        "s09" -> {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFC))) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset(x = 20.dp, y = 56.dp).size(44.dp).clip(CircleShape).background(Color.White).border(1.dp, Color(0xFFECECF5), CircleShape).clickable { onSelectScreenCode("s07") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
                }

                Column(modifier = Modifier.fillMaxWidth().offset(y = 150.dp).padding(horizontal = 24.dp)) {
                    BasicText("¿Olvidaste tu contraseña?", style = typo.heroTitle.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    Spacer(Modifier.height(8.dp))
                    BasicText("Escribe tu correo y te enviamos un enlace para crear una nueva.", style = typo.body.copy(fontSize = 16.sp, color = Color(0xFF5F6377)))

                    Spacer(Modifier.height(24.dp))

                    BasicText("Correo electrónico", style = typo.fieldLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
                    ) {
                        BasicText("camila@rosafiesta.do", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                    }

                    Spacer(Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))).clickable { onSelectScreenCode("s10") },
                    ) {
                        BasicText("Enviar enlace", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }

                BasicText(
                    text = "Volver a iniciar sesión",
                    style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C5CF0)),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp).clickable { onSelectScreenCode("s07") },
                )
            }
        }

        "s10" -> {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFC))) {
                Column(modifier = Modifier.fillMaxWidth().offset(y = 170.dp).padding(horizontal = 24.dp)) {
                    BasicText("Crea una nueva contraseña", style = typo.heroTitle.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    Spacer(Modifier.height(8.dp))
                    BasicText("Elige una que no uses en otras apps.", style = typo.body.copy(fontSize = 16.sp, color = Color(0xFF5F6377)))

                    Spacer(Modifier.height(22.dp))

                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(2.dp, Color(0xFFB754CB), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
                    ) {
                        BasicText("fiesta2026", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)))
                    }

                    Spacer(Modifier.height(14.dp))

                    // Barras de fuerza
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                            Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF8DB52C)))
                            Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF8DB52C)))
                            Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF8DB52C)))
                            Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE6DEEA)))
                        }
                        BasicText("Buena", style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                    }

                    Spacer(Modifier.height(24.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))).clickable { onSelectScreenCode("s11") },
                    ) {
                        BasicText("Guardar contraseña", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }

        "s11", "s12" -> {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFC))) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 24.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(96.dp).clip(CircleShape).background(Color(0xFFEEF6D6)),
                    ) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF2FB58F), size = 48.dp)
                    }

                    Spacer(Modifier.height(20.dp))

                    BasicText(
                        text = if (spec.code == "s11") "¡Tu cuenta está activa!" else "Correo verificado",
                        style = typo.heroTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                    )

                    Spacer(Modifier.height(10.dp))

                    BasicText(
                        text = if (spec.code == "s11") "Ya puedes iniciar sesión y empezar a planificar tu evento." else "Tu cuenta quedó activada. Ya puedes iniciar sesión.",
                        style = typo.body.copy(fontSize = 16.sp, color = Color(0xFF5F6377), textAlign = TextAlign.Center),
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable { onSelectScreenCode("s13") },
                ) {
                    BasicText(
                        text = if (spec.code == "s11") "Ir a iniciar sesión" else "Iniciar sesión",
                        style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }

        "s13" -> {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFC))) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp, start = 20.dp, end = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFEFE8FD)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("CR", style = typo.body.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0)))
                        }
                        Column {
                            BasicText("¡Hola, Camila!", style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                            BasicText("3 eventos activos", style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Tarjetas de eventos activos (23 OCT, 2 NOV, 9 NOV)
                    EventActiveSummaryCard("23", "OCT", "Boda Real Rosa & Dorado", "Por pagar", Color(0xFFFFF2C9), Color(0xFF7A5700), "en 3 días")
                    Spacer(Modifier.height(10.dp))
                    EventActiveSummaryCard("2", "NOV", "Quinceañera Valentina Crystal", "Confirmado", Color(0xFFEEF6D6), Color(0xFF4E6A10), "en 13 días")
                    Spacer(Modifier.height(10.dp))
                    EventActiveSummaryCard("9", "NOV", "Baby Shower Sofía en Rosas", "Borrador", Color(0xFFF1EFF5), Color(0xFF62627A), "en 20 días")
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable { onSelectScreenCode("s14") },
                ) {
                    BasicText("Continuar al inicio", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

@Composable
private fun EventActiveSummaryCard(
    day: String,
    month: String,
    title: String,
    status: String,
    statusBg: Color,
    statusColor: Color,
    timing: String,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 52.dp, height = 52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3EEF6)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText(day, style = typo.sheetOrEmptyTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    BasicText(month, style = typo.sectionCap.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(title, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        BasicText(status, style = typo.sectionCap.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor))
                    }
                    BasicText(timing, style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF5F6377), size = 18.dp, modifier = Modifier.rotate(180f))
        }
    }
}

// ==============================================================================
// s01 · CATÁLOGO (MODO INVITADO)
// ==============================================================================

@Composable
internal fun RenderGuestCatalogS01(
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    var selectedCategory by remember { mutableStateOf("Todas") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 58.dp, start = 20.dp, end = 20.dp),
    ) {
        // Encabezado: Título + Botón de filtros
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
                    color = Color(0xFF1A1A2E),
                ),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), CircleShape),
            ) {
                RfIcon(kind = RfIconKind.FILTER, tint = Color(0xFF1A1A2E), size = 22.dp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Barra de búsqueda con micrófono (height 54dp, radius 16dp)
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(16.dp))
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
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF62627A), size = 20.dp)
                    BasicText(
                        "Buscar sillas, mesas, flores…",
                        style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF62627A)),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDE6F2)),
                ) {
                    RfIcon(kind = RfIconKind.MIC, tint = Color(0xFFC2338F), size = 20.dp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Chips horizontales (height 38dp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .horizontalScroll(rememberScrollState()),
        ) {
            val categories = listOf("Todas", "Iluminación", "Floral", "Globos", "Mobiliario")
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            } else {
                                SolidColor(Color.White)
                            },
                        )
                        .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFEEE9F2), CircleShape)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 15.dp),
                ) {
                    BasicText(
                        text = cat,
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF1A1A2E),
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Conteo y orden (height 32dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().height(32.dp),
        ) {
            BasicText("15 artículos de alquiler", style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF62627A)))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BasicText("Populares", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E)))
                RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF1A1A2E), size = 16.dp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Grid 2x2 de productos canónicos
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GuestProductCard(
                    title = "Silla Tiffany Cristal",
                    desc = "Acrílica transparente",
                    price = "RD$ 4",
                    photoKey = RfPhotoKey.SILLA_TIFFANY,
                    bg = Color(0xFFEFE8FD),
                    modifier = Modifier.weight(1f),
                )
                GuestProductCard(
                    title = "Mesa dulce temática",
                    desc = "Decoración completa para dulces",
                    price = "RD$ 150",
                    photoKey = RfPhotoKey.MESA_HELADOS,
                    bg = Color(0xFFEEF6D6),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GuestProductCard(
                    title = "Arco de globos",
                    desc = "Rosa y nude, para la entrada",
                    price = "RD$ 95",
                    photoKey = RfPhotoKey.ARCO_ROSA,
                    bg = Color(0xFFFDE6F2),
                    modifier = Modifier.weight(1f),
                )
                GuestProductCard(
                    title = "Centro de mesa floral",
                    desc = "Flores naturales, base de madera",
                    price = "RD$ 35",
                    photoKey = RfPhotoKey.CENTRO_FLORAL,
                    bg = Color(0xFFFFF2C9),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(96.dp))
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
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
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
                // Botón favorito
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f)),
                ) {
                    RfIcon(kind = RfIconKind.HEART, tint = Color(0xFF1A1A2E), size = 18.dp)
                }
            }

            Column(modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
                BasicText(title, maxLines = 1, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)))
                Spacer(Modifier.height(3.dp))
                BasicText(desc, maxLines = 1, style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF62627A)))
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        BasicText(price, style = typo.heroTitle.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E)))
                        BasicText(" /día", style = typo.secondary.copy(fontSize = 12.sp, color = Color(0xFF62627A)))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))),
                            ),
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
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 58.dp, start = 20.dp, end = 20.dp),
    ) {
        BasicText("Favoritos", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))

        Spacer(Modifier.height(16.dp))

        // Banner para iniciar sesión
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText("Guarda tus favoritos en todos tus dispositivos.", style = typo.body.copy(fontSize = 14.sp, color = Color(0xFF151729)))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onSelectScreenCode("s07") },
                ) {
                    BasicText("Iniciar sesión", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4FD8)))
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF7A4FD8), size = 14.dp, modifier = Modifier.rotate(180f))
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // Estado vacío ilustrado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFCE4EF)),
            ) {
                RfIcon(kind = RfIconKind.HEART, tint = Color(0xFFEC4F97), size = 64.dp)
            }

            Spacer(Modifier.height(20.dp))

            BasicText("Aún no tienes favoritos", style = typo.heroTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))

            Spacer(Modifier.height(8.dp))

            BasicText(
                text = "Toca el corazón en cualquier artículo y lo guardamos aquí para más tarde.",
                style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377), textAlign = TextAlign.Center, lineHeight = 22.sp),
            )

            Spacer(Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                    .clickable { onSelectScreenCode("s01") },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF151729), size = 18.dp)
                    BasicText("Explorar catálogo", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                }
            }
        }
    }
}
