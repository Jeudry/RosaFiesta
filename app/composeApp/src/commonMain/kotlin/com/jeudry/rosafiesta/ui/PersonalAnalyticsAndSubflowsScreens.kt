package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.domain.EventStatus

private val PersonalButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF3366), Color(0xFFE6007A)),
)

// ==============================================================================
// s16 · ACERCA DE ROSA FIESTA
// ==============================================================================
@Composable
internal fun RenderAboutScreenS16(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val reviewsState = viewModel?.companyReviews?.collectAsState()?.value
    val avgRatingText = remember(reviewsState) {
        viewModel?.companyAverageRatingFormatted() ?: "5.0"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .drawBehind {
                if (!palette.isDark) {
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
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Acerca de",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.4).sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }

            // Brand Header: Logo 84dp + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0x24EA4EA9), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfLogo(customSize = 84.dp, insetPadding = false)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val nameAnnotated = buildAnnotatedString {
                        append("Rosa ")
                        withStyle(SpanStyle(color = Color(0xFFD63F98))) {
                            append("Fiesta")
                        }
                    }
                    BasicText(
                        text = nameAnnotated,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.56).sp,
                            lineHeight = 28.sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                    BasicText(
                        text = "Decoración y planificación de eventos",
                        style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
            }

            // Metrics Card (3 columns: 127 / 8 / 4.9 ★)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                    .clickable { onSelectScreenCode("s17") }
                    .padding(vertical = 16.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BasicText(
                            "127",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                letterSpacing = (-0.52).sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText("eventos", style = typo.body.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(RfThemeState.currentPalette.line),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BasicText(
                            "8",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                letterSpacing = (-0.52).sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText("años", style = typo.body.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(RfThemeState.currentPalette.line),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText(
                                avgRatingText,
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.52).sp,
                                    color = palette.ink,
                                ),
                            )
                            CustomSvgIcon(
                                pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                size = 18.dp,
                                tint = Color(0xFFF7B500),
                                filled = true,
                            )
                        }
                        BasicText("calificación", style = typo.body.copy(fontSize = 13.sp, color = palette.muted))
                    }
                }
            }

            // Contact Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                    .padding(vertical = 6.dp),
            ) {
                Column {
                    BasicText(
                        text = "CONTACTO",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = RfThemeState.currentPalette.muted,
                        ),
                        modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 4.dp),
                    )

                    // Phone row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M5 4h3.5l1.5 4.5-2.2 1.3a11 11 0 0 0 6.4 6.4l1.3-2.2L20 15.5V19a1.5 1.5 0 0 1-1.6 1.5C10.6 20 4 13.4 3.5 5.6A1.5 1.5 0 0 1 5 4z",
                                size = 20.dp,
                                tint = Color(0xFFC2338F),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Teléfono", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            BasicText("+1 (829) 942-4971", style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x66865DEC), ambientColor = Color.Transparent)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                .clickable { onSelectScreenCode("s42") }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Llamar", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }

                    // Divider
                    Box(Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                    // WhatsApp row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s42") }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M4 20l1.3-4A8.5 8.5 0 1 1 8 18.8zM9 8.5c0 3.2 3.2 6.5 6.5 6.5l1-1.6-2-1.1-1 .8c-1-.4-2.1-1.5-2.5-2.5l.8-1-1.1-2z",
                                size = 20.dp,
                                tint = Color(0xFF4E6A10),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("WhatsApp", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            BasicText("Escríbele a María", style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                        CustomSvgIcon(
                            pathData = "M9 18l6-6-6-6",
                            size = 20.dp,
                            tint = RfThemeState.currentPalette.muted,
                        )
                    }

                    // Divider
                    Box(Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                    // Instagram row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s42") }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFE8FD)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M3 8.5A1.5 1.5 0 0 1 4.5 7H7l1.5-2.5h7L17 7h2.5A1.5 1.5 0 0 1 21 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5zM8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0",
                                size = 20.dp,
                                tint = Color(0xFF7A4FD8),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Instagram", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            BasicText("Mira nuestros montajes", style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                        CustomSvgIcon(
                            pathData = "M9 18l6-6-6-6",
                            size = 20.dp,
                            tint = RfThemeState.currentPalette.muted,
                        )
                    }

                    // Divider
                    Box(Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                    // Facebook row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s42") }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M5 8a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0M2 20c.6-3.2 3.3-5.5 6.5-5.5S14.4 16.8 15 20M15.5 4.6a3.5 3.5 0 0 1 0 6.8M18 14.8c2 .7 3.5 2.6 4 5.2",
                                size = 20.dp,
                                tint = Color(0xFF1F8A84),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Facebook", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            BasicText("Únete a la comunidad", style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                        CustomSvgIcon(
                            pathData = "M9 18l6-6-6-6",
                            size = 20.dp,
                            tint = RfThemeState.currentPalette.muted,
                        )
                    }
                }
            }

            // Map Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp)),
            ) {
                Column {
                    // Map Canvas
                    MapIllustrationS16(modifier = Modifier.fillMaxWidth().height(120.dp))

                    // Map bottom row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            BasicText("Santo Domingo, R.D.", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            BasicText("[Dirección del local]", style = typo.body.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                        }
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(999.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Cómo llegar", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MapIllustrationS16(modifier: Modifier = Modifier) {
    val land1 = remember { PathParser().parsePathString("M0 110C40 96 70 120 110 104S170 70 210 84 270 120 300 100V160H0Z").toPath() }
    val park1 = remember { PathParser().parsePathString("M180 10C210 0 250 14 256 40S230 76 204 70 168 40 180 10Z").toPath() }
    val park2 = remember { PathParser().parsePathString("M20 30C40 26 60 40 54 56S24 66 16 52 8 34 20 30Z").toPath() }
    val roadMain = remember { PathParser().parsePathString("M-10 128C60 112 120 140 190 120S280 96 310 104").toPath() }
    val pinPath = remember { PathParser().parsePathString("M150 95s-17-14-17-27a17 17 0 0 1 34 0c0 13-17 27-17 27z").toPath() }

    Canvas(modifier = modifier) {
        val s = size.width / 300f
        drawRect(Color(0xFFEEF6FB))

        scale(s, s, Offset.Zero) {
            drawPath(land1, Color(0xFFDFF0FB))
            drawPath(park1, Color(0xFFD7F2E6))
            drawPath(park2, Color(0xFFD7F2E6))
            drawPath(roadMain, Color(0xFFBFE1F5), style = Stroke(width = 12f))

            // White roads
            drawLine(Color.White, Offset(0f, 74f), Offset(300f, 74f), strokeWidth = 8f)
            drawLine(Color.White, Offset(110f, 0f), Offset(110f, 160f), strokeWidth = 8f)
            drawLine(Color.White, Offset(228f, 0f), Offset(196f, 160f), strokeWidth = 8f)
            drawLine(Color.White, Offset(0f, 20f), Offset(300f, 60f), strokeWidth = 8f)

            drawLine(Color.White, Offset(60f, 0f), Offset(60f, 160f), strokeWidth = 3.5f)
            drawLine(Color.White, Offset(0f, 140f), Offset(300f, 140f), strokeWidth = 3.5f)
            drawLine(Color.White, Offset(160f, 0f), Offset(170f, 160f), strokeWidth = 3.5f)

            // Pin shadow
            drawOval(Color(0x262B1730), topLeft = Offset(138f, 92.5f), size = androidx.compose.ui.geometry.Size(24f, 7f))

            // Pin
            drawPath(pinPath, Color(0xFFD12F7C))
            drawCircle(Color.White, radius = 6.5f, center = Offset(150f, 68f))
        }
    }
}

// ==============================================================================
// s17 · RESEÑAS DE LA EMPRESA
// ==============================================================================
@Composable
internal fun RenderCompanyReviewsS17(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    var selectedFilter by remember { mutableStateOf(0) }
    var showWriteReviewModal by remember { mutableStateOf(false) }
    var draftStars by remember { mutableStateOf(5) }
    var draftComment by remember { mutableStateOf("") }

    val reviews = viewModel?.companyReviews?.collectAsState()?.value ?: listOf(
        CompanyReviewUiModel(
            id = "rev-1",
            author = "Cliente",
            initial = "C",
            dateLabel = "Hoy",
            rating = 5,
            comment = "El montaje de nuestra boda superó todas las expectativas. Las sillas Tiffany y el arco floral quedaron de revista.",
            isVerified = true,
            avatarBgHex = 0xFFFDE6F2,
            avatarInkHex = 0xFFC2338F,
        ),
        CompanyReviewUiModel(
            id = "rev-2",
            author = "Cliente",
            initial = "C",
            dateLabel = "Hoy",
            rating = 5,
            comment = "Puntualidad impecable y atención personalizada con Rosa IA. ¡Recomendados 100%!",
            isVerified = true,
            avatarBgHex = 0xFFEFE8FD,
            avatarInkHex = 0xFF7A4FD8,
        ),
    )
    val avgRatingText = remember(reviews) {
        viewModel?.companyAverageRatingFormatted() ?: "5.0"
    }
    val verifiedCount = remember(reviews) { reviews.count { item -> item.isVerified } }
    val filteredReviews = remember(reviews, selectedFilter) {
        if (selectedFilter == 1) {
            reviews.filter { item -> item.isVerified }
        } else {
            reviews
        }
    }
    val totalCount = reviews.size.coerceAtLeast(1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .drawBehind {
                if (!palette.isDark) {
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
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s16") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Reseñas",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.4).sp,
                        color = palette.ink,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = avgRatingText,
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 48.sp,
                                letterSpacing = (-1).sp,
                                lineHeight = 44.sp,
                                color = palette.ink,
                            ),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(5) {
                                CustomSvgIcon(
                                    pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                    size = 16.dp,
                                    tint = Color(0xFFF7B500),
                                    filled = true,
                                )
                            }
                        }
                        BasicText(
                            text = "${reviews.size} reseñas",
                            style = typo.body.copy(fontSize = 13.sp, color = palette.muted),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (starLevel in 5 downTo 1) {
                            val starCount = reviews.count { item -> item.rating == starLevel }
                            val progress = starCount.toFloat() / totalCount.toFloat()
                            RatingBarRow(star = starLevel.toString(), progress = progress)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .then(
                            if (selectedFilter == 0) {
                                Modifier.shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x73865DEC), ambientColor = Color.Transparent)
                            } else {
                                Modifier
                            },
                        )
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selectedFilter == 0) {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            } else {
                                Brush.linearGradient(listOf(palette.surface, palette.surface))
                            },
                        )
                        .then(
                            if (selectedFilter != 0) Modifier.border(1.dp, palette.line, RoundedCornerShape(999.dp)) else Modifier,
                        )
                        .clickable { selectedFilter = 0 }
                        .padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        "Todas · ${reviews.size}",
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedFilter == 0) Color.White else palette.ink,
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .then(
                            if (selectedFilter == 1) {
                                Modifier.shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x73865DEC), ambientColor = Color.Transparent)
                            } else {
                                Modifier
                            },
                        )
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selectedFilter == 1) {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            } else {
                                Brush.linearGradient(listOf(palette.surface, palette.surface))
                            },
                        )
                        .then(
                            if (selectedFilter != 1) Modifier.border(1.dp, palette.line, RoundedCornerShape(999.dp)) else Modifier,
                        )
                        .clickable { selectedFilter = 1 }
                        .padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        "Verificadas · $verifiedCount",
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedFilter == 1) Color.White else palette.ink,
                        ),
                    )
                }
            }

            filteredReviews.forEach { reviewItem ->
                ReviewCardItem(
                    avatarBg = Color(reviewItem.avatarBgHex),
                    avatarTextColor = Color(reviewItem.avatarInkHex),
                    initial = reviewItem.initial,
                    author = reviewItem.author,
                    date = reviewItem.dateLabel,
                    comment = reviewItem.comment,
                    rating = reviewItem.rating,
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, palette.bg, palette.bg),
                    ),
                )
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp, top = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                    .clickable { showWriteReviewModal = true },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CustomSvgIcon(
                        pathData = "M4 20h4L19 9l-4-4L4 16zM13.5 6.5l4 4",
                        size = 20.dp,
                        tint = Color.White,
                    )
                    BasicText(
                        "Escribir una reseña",
                        style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }

        if (showWriteReviewModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88141024))
                    .clickable { showWriteReviewModal = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(palette.surface)
                        .clickable(enabled = false) {}
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 34.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            "Califica tu experiencia",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(palette.chipBg)
                                .clickable { showWriteReviewModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = palette.ink, size = 18.dp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (starValue in 1..5) {
                            val isStarSelected = starValue <= draftStars
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isStarSelected) Color(0xFFFFF2C9) else palette.chipBg)
                                    .clickable { draftStars = starValue },
                                contentAlignment = Alignment.Center,
                            ) {
                                CustomSvgIcon(
                                    pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                    size = 24.dp,
                                    tint = if (isStarSelected) Color(0xFFF7B500) else palette.muted,
                                    filled = isStarSelected,
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.chipBg)
                            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                    ) {
                        if (draftComment.isEmpty()) {
                            BasicText(
                                "Cuéntanos cómo quedó tu montaje o atención con Rosa Fiesta…",
                                style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                            )
                        }
                        BasicTextField(
                            value = draftComment,
                            onValueChange = { updated -> draftComment = updated },
                            textStyle = typo.body.copy(fontSize = 14.5.sp, color = palette.ink),
                            cursorBrush = SolidColor(Color(0xFFD63F98)),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                            .clickable {
                                val textToPublish = draftComment.trim().ifEmpty {
                                    "Excelente servicio y mobiliario impecable en nuestro evento."
                                }
                                viewModel?.submitCompanyReview(comment = textToPublish, rating = draftStars)
                                draftComment = ""
                                draftStars = 5
                                showWriteReviewModal = false
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            "Publicar reseña ($draftStars ★)",
                            style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBarRow(star: String, progress: Float) {
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicText(star, style = typo.body.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted), modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(RfThemeState.currentPalette.chipBg),
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFF7B500)),
                )
            }
        }
    }
}

@Composable
private fun ReviewCardItem(
    avatarBg: Color,
    avatarTextColor: Color,
    initial: String,
    author: String,
    date: String,
    comment: String,
    rating: Int = 5,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = initial,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = avatarTextColor,
                        ),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(author, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(rating.coerceIn(1, 5)) {
                            CustomSvgIcon(
                                pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                size = 13.dp,
                                tint = Color(0xFFF7B500),
                                filled = true,
                            )
                        }
                    }
                }

                BasicText(date, style = typo.body.copy(fontSize = 13.sp, color = palette.muted))
            }

            BasicText(
                comment,
                style = typo.body.copy(fontSize = 15.sp, lineHeight = 22.sp, color = palette.ink),
            )
        }
    }
}

// ==============================================================================
// s41 & s42 · CHAT DEL EVENTO (CLIENTE)
// ==============================================================================

@Composable
private fun CustomSvgIcon(
    pathData: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = Color(0xFF1A1A2E),
    strokeWidth: Float = 1.8f,
    filled: Boolean = false,
) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        scale(s, s, Offset.Zero) {
            if (filled) {
                drawPath(
                    path = path,
                    color = tint,
                )
            } else {
                drawPath(
                    path = path,
                    color = tint,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}

@Composable
private fun ChatDateBadge(
    label: String = "Ayer",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(RfThemeState.currentPalette.chipBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RfThemeState.currentPalette.muted,
            ),
        )
    }
}

@Composable
private fun ChatMessageBubble(
    text: String,
    time: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
        ) {
            Column {
                BasicText(
                    text = text,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = Color.White,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BasicText(
                        text = time,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = Color(0xFFFFD6E8),
                        ),
                    )
                    CustomSvgIcon(
                        pathData = "M5 12.5l4.5 4.5L19 7.5",
                        size = 14.dp,
                        tint = Color(0xFFFFD6E8),
                        strokeWidth = 2.2f,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMariaIncomingBubble(
    author: String,
    text: String,
    time: String,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        RfLogo(customSize = 28.dp, insetPadding = false)
        Box(
            modifier = Modifier
                .widthIn(max = 275.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                .background(palette.surface)
                .border(1.dp, palette.line, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText(
                    text = author,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD63F98),
                    ),
                )
                BasicText(
                    text = text,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        color = palette.ink,
                    ),
                )
                BasicText(
                    text = time,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 11.sp,
                        color = palette.muted,
                    ),
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Composable
internal fun RenderEventChatTabS41(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    var draftInputS41 by remember { mutableStateOf("") }
    var localFallbackMessages by remember {
        mutableStateOf(
            listOf(
                DirectChatMessageUiModel(
                    id = "loc-1",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Hola! Soy María de Rosa Fiesta 🌸 Estoy conectada para ayudarte con la cotización, el mobiliario o cualquier detalle de tu evento.",
                    timeLabel = "7:35 p. m.",
                    isFromClient = false,
                ),
                DirectChatMessageUiModel(
                    id = "loc-2",
                    senderName = "Cliente",
                    text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                    timeLabel = "7:38 p. m.",
                    isFromClient = true,
                ),
                DirectChatMessageUiModel(
                    id = "loc-3",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Claro que sí! Tenemos disponibilidad inmediata y las acabo de reservar en tu lista del Jardín Botánico ✨",
                    timeLabel = "7:40 p. m.",
                    isFromClient = false,
                ),
            ),
        )
    }
    val vmMessages = viewModel?.directChatMessages?.collectAsState()?.value
    val activeMessages = vmMessages ?: localFallbackMessages
    val scrollState = rememberScrollState()

    val sendChatText: (String) -> Unit = { rawText ->
        val trimmed = rawText.trim()
        if (trimmed.isNotEmpty()) {
            if (viewModel != null) {
                viewModel.sendDirectChatMessage(trimmed)
            } else {
                localFallbackMessages = localFallbackMessages + listOf(
                    DirectChatMessageUiModel(
                        id = "loc-${localFallbackMessages.size + 1}",
                        senderName = "Cliente",
                        text = trimmed,
                        timeLabel = "Ahora",
                        isFromClient = true,
                    ),
                    DirectChatMessageUiModel(
                        id = "loc-${localFallbackMessages.size + 2}",
                        senderName = "María · Rosa Fiesta",
                        text = "¡Recibido, muchas gracias! Ya lo anoté en tu expediente de evento 🌸",
                        timeLabel = "Ahora",
                        isFromClient = false,
                    ),
                )
            }
        }
    }

    val submitMessageS41 = {
        val trimmed = draftInputS41.trim()
        if (trimmed.isNotEmpty()) {
            sendChatText(trimmed)
            draftInputS41 = ""
        }
    }

    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 136.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M19 12H5M11 18l-6-6 6-6",
                        size = 22.dp,
                        tint = palette.ink,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = palette.ink,
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = "Vie 23 oct · Jardín Botánico",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = palette.muted,
                        ),
                        maxLines = 1,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("s83") },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M12 4v11M7 10.5l5 5 5-5M5 20h14",
                        size = 20.dp,
                        tint = palette.ink,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("s83") },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M15 6a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M15 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M8.6 10.6l6.8-3.6M8.6 13.4l6.8 3.6",
                        size = 20.dp,
                        tint = palette.ink,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { onSelectScreenCode("s40") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Detalles",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.muted,
                            ),
                        )
                    }

                    Column(
                        modifier = Modifier.height(44.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Chat",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.ink,
                                ),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(Color(0xFFD63F98)),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(palette.line),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfLogo(
                    customSize = 36.dp,
                    insetPadding = false,
                )
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "María · Equipo Rosa Fiesta",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.ink,
                        ),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8DB52C)),
                        )
                        BasicText(
                            text = "En línea · Responde al instante",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ChatDateBadge("Conversación activa")
                }

                activeMessages.forEach { msg ->
                    if (msg.isFromClient) {
                        ChatMessageBubble(
                            text = msg.text,
                            time = msg.timeLabel,
                        )
                    } else {
                        ChatMariaIncomingBubble(
                            author = msg.senderName,
                            text = msg.text,
                            time = msg.timeLabel,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(palette.surface)
                .border(1.dp, palette.line),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val quickPrompts = listOf(
                    "Cotizar sillas Tiffany",
                    "Consultar fecha",
                    "Formas de pago",
                    "Visitar el local",
                )
                quickPrompts.forEach { promptChip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(palette.chipBg)
                            .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                            .clickable { sendChatText(promptChip) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = promptChip,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD63F98),
                            ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .clickable {
                            sendChatText("📎 Plano-brindis-jardin.pdf adjuntado")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M20 11.5l-8.2 8.2a5 5 0 0 1-7-7L13 4.5a3.3 3.3 0 0 1 4.7 4.7l-8.2 8.2a1.7 1.7 0 0 1-2.4-2.4L14.5 7.5",
                        size = 22.dp,
                        tint = palette.muted,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (draftInputS41.isEmpty()) {
                        BasicText(
                            text = "Escríbele a María…",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                color = palette.muted,
                            ),
                        )
                    }
                    BasicTextField(
                        value = draftInputS41,
                        onValueChange = { updatedText -> draftInputS41 = updatedText },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitMessageS41() }),
                        textStyle = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = palette.ink,
                        ),
                        cursorBrush = SolidColor(Color(0xFFD63F98)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                        .clickable { submitMessageS41() },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M21 3L10 14M21 3l-6.5 18-4.5-7-7-4.5z",
                        size = 20.dp,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderEventChatS42(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    var draftInputS42 by remember { mutableStateOf("") }
    var localFallbackMessages by remember {
        mutableStateOf(
            listOf(
                DirectChatMessageUiModel(
                    id = "loc42-1",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Hola! Soy María de Rosa Fiesta 🌸 Estoy conectada para ayudarte con la cotización, el mobiliario o cualquier detalle de tu evento.",
                    timeLabel = "7:35 p. m.",
                    isFromClient = false,
                ),
                DirectChatMessageUiModel(
                    id = "loc42-2",
                    senderName = "Cliente",
                    text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                    timeLabel = "7:38 p. m.",
                    isFromClient = true,
                ),
                DirectChatMessageUiModel(
                    id = "loc42-3",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Claro que sí! Tenemos disponibilidad inmediata y las acabo de reservar en tu lista del Jardín Botánico ✨",
                    timeLabel = "7:40 p. m.",
                    isFromClient = false,
                ),
            ),
        )
    }
    val vmMessages = viewModel?.directChatMessages?.collectAsState()?.value
    val activeMessages = vmMessages ?: localFallbackMessages
    val scrollState = rememberScrollState()

    val sendChatText: (String) -> Unit = { rawText ->
        val trimmed = rawText.trim()
        if (trimmed.isNotEmpty()) {
            if (viewModel != null) {
                viewModel.sendDirectChatMessage(trimmed)
            } else {
                localFallbackMessages = localFallbackMessages + listOf(
                    DirectChatMessageUiModel(
                        id = "loc42-${localFallbackMessages.size + 1}",
                        senderName = "Cliente",
                        text = trimmed,
                        timeLabel = "Ahora",
                        isFromClient = true,
                    ),
                    DirectChatMessageUiModel(
                        id = "loc42-${localFallbackMessages.size + 2}",
                        senderName = "María · Rosa Fiesta",
                        text = "¡Recibido, muchas gracias! Ya lo anoté en tu expediente de evento 🌸",
                        timeLabel = "Ahora",
                        isFromClient = false,
                    ),
                )
            }
        }
    }

    val submitMessageS42 = {
        val trimmed = draftInputS42.trim()
        if (trimmed.isNotEmpty()) {
            sendChatText(trimmed)
            draftInputS42 = ""
        }
    }

    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 84.dp, bottom = 126.dp)
                .verticalScroll(scrollState)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(18.dp))
                    .clickable { onSelectScreenCode("s40") }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RfPhoto(
                    photoKey = RfPhotoKey.BODA,
                    contentDescription = "Boda",
                    cornerRadius = 12.dp,
                    modifier = Modifier.size(48.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = palette.ink,
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = "Vie 23 oct · 120 invitados",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            color = palette.muted,
                        ),
                    )
                }

                BasicText(
                    text = "Ver",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFC2338F),
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                ChatDateBadge("Chat directo con María")
            }

            activeMessages.forEach { msg ->
                if (msg.isFromClient) {
                    ChatMessageBubble(
                        text = msg.text,
                        time = msg.timeLabel,
                    )
                } else {
                    ChatMariaIncomingBubble(
                        author = msg.senderName,
                        text = msg.text,
                        time = msg.timeLabel,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(palette.surface)
                .border(1.dp, palette.line)
                .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onSelectScreenCode("BACK:s16") },
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M19 12H5M11 18l-6-6 6-6",
                    size = 22.dp,
                    tint = palette.ink,
                )
            }

            RfLogo(
                customSize = 42.dp,
                insetPadding = false,
            )

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "María · Rosa Fiesta",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink,
                    ),
                    maxLines = 1,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8DB52C)),
                    )
                    BasicText(
                        text = "En línea · Atención personalizada",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF4E6A10),
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onSelectScreenCode("s40") },
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M12 5h.01M12 12h.01M12 19h.01",
                    size = 22.dp,
                    tint = palette.ink,
                    strokeWidth = 2.6f,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(palette.surface)
                .border(1.dp, palette.line),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val quickPrompts = listOf(
                    "Cotizar sillas Tiffany",
                    "Consultar fecha",
                    "Formas de pago",
                    "Visitar el local",
                )
                quickPrompts.forEach { promptChip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(palette.chipBg)
                            .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                            .clickable { sendChatText(promptChip) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = promptChip,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD63F98),
                            ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .clickable {
                            draftInputS42 = "$draftInputS42 ✨"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M8.5 14.5c1.8 2 5.2 2 7 0M9 9.5h.01M15 9.5h.01",
                        size = 22.dp,
                        tint = palette.muted,
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .padding(start = 16.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (draftInputS42.isEmpty()) {
                            BasicText(
                                text = "Escríbele a María…",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 15.sp,
                                    color = palette.muted,
                                ),
                            )
                        }
                        BasicTextField(
                            value = draftInputS42,
                            onValueChange = { updatedText -> draftInputS42 = updatedText },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitMessageS42() }),
                            textStyle = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                color = palette.ink,
                            ),
                            cursorBrush = SolidColor(Color(0xFFD63F98)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                sendChatText("📷 Foto de referencia adjuntada")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        CustomSvgIcon(
                            pathData = "M3 8.5A1.5 1.5 0 0 1 4.5 7H7l1.5-2.5h7L17 7h2.5A1.5 1.5 0 0 1 21 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5zM8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0",
                            size = 20.dp,
                            tint = palette.muted,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                        .clickable { submitMessageS42() },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M21 3L10 14M21 3l-6.5 18-4.5-7-7-4.5z",
                        size = 20.dp,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s59 · PANEL · FINANZAS
// ==============================================================================
@Composable
internal fun RenderFinanceDashboardS59(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFinanceTab by remember { mutableStateOf(0) }

    AdminPanelScaffold(activeCode = "s59", onSelectScreenCode = onSelectScreenCode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEDEDF5))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PanelSegTab59("Movimientos", selected = selectedFinanceTab == 0, onClick = { selectedFinanceTab = 0 }, modifier = Modifier.weight(1f))
            PanelSegTab59("Facturas", selected = selectedFinanceTab == 1, onClick = { selectedFinanceTab = 1 }, modifier = Modifier.weight(1f))
            PanelSegTab59("Proveedores", selected = selectedFinanceTab == 2, onClick = { selectedFinanceTab = 2 }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                BasicText("Entradas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF4E6A10)))
                BasicText("RD$ 10,360", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.44).sp, color = Color(0xFF3F560C)))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                BasicText("Salidas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF5F6377)))
                BasicText("RD$ 1,450", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.44).sp, color = Color(0xFF151729)))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            PanelMovementRow59("Anticipo 30%", "Quinceañera Valentina Crystal", "+RD$ 1,860", RfIconKind.TREND_UP, Color(0xFFEEF6D6), Color(0xFF4E6A10), Color(0xFF4E6A10), divider = false)
            PanelMovementRow59("Pago total", "Gala Corporativa Grupo Caribe", "+RD$ 8,500", RfIconKind.TREND_UP, Color(0xFFEEF6D6), Color(0xFF4E6A10), Color(0xFF4E6A10), divider = true)
            PanelMovementRow59("Follaje y transporte", "Gasto · camiones", "−RD$ 1,450", RfIconKind.TRUCK, Color(0xFFFFF2C9), Color(0xFF7A5700), Color(0xFF865DEC), divider = true)
        }
    }
}

@Composable
private fun PanelSegTab59(label: String, selected: Boolean, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val typo = LocalRfTypography.current

    Box(
        modifier = modifier
            .height(38.dp)
            .then(
                if (selected) {
                    Modifier.shadow(6.dp, RoundedCornerShape(11.dp), spotColor = Color(0x334650B4))
                } else {
                    Modifier
                },
            )
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color(0xFF151729) else Color(0xFF5F6377),
            ),
        )
    }
}

@Composable
private fun PanelMovementRow59(
    title: String,
    subtitle: String,
    amount: String,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    amountColor: Color,
    divider: Boolean,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 20.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            BasicText(
                subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
            )
        }
        BasicText(
            amount,
            maxLines = 1,
            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = amountColor),
        )
    }
}

// ==============================================================================
// s60 a s74 · PANELES DE ANALÍTICA, LEADS, REPORTES Y BOT WHATSAPP
// ==============================================================================
@Composable
internal fun RenderInventoryCalendarS63(onSelectScreenCode: (String) -> Unit = {}) {
    val typo = LocalRfTypography.current
    var selectedDay63 by remember { mutableStateOf(20) }
    var currentMonthLabel by remember { mutableStateOf("Octubre 2026") }
    val days = remember {
        listOf(
            1 to Alta63, 2 to Media63, 3 to Media63, 4 to Alta63, 5 to Alta63, 6 to Alta63, 7 to Alta63,
            8 to Alta63, 9 to Media63, 10 to Baja63, 11 to Alta63, 12 to Alta63, 13 to Alta63, 14 to Alta63,
            15 to Alta63, 16 to Media63, 17 to Media63, 18 to Alta63, 19 to Alta63, 20 to Alta63, 21 to Alta63,
            22 to Media63, 23 to Agotado63, 24 to Baja63, 25 to Alta63, 26 to Alta63, 27 to Alta63, 28 to Alta63,
            29 to Alta63, 30 to Media63, 31 to Baja63,
        )
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Disponibilidad",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
            }
            Spacer(Modifier.height(14.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                        .padding(start = 10.dp, end = 10.dp, top = 14.dp, bottom = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).clickable {
                                currentMonthLabel = if (currentMonthLabel == "Octubre 2026") "Septiembre 2026" else "Octubre 2026"
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHEVRON_LEFT, tint = Color(0xFF151729), size = 20.dp)
                        }
                        BasicText(
                            currentMonthLabel,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, textAlign = TextAlign.Center, color = Color(0xFF151729)),
                        )
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).clickable {
                                currentMonthLabel = if (currentMonthLabel == "Octubre 2026") "Noviembre 2026" else "Octubre 2026"
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF151729), size = 20.dp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { label ->
                            BasicText(
                                label,
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, color = Color(0xFF5F6377)),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val cells = listOf<Pair<Int, Color>?>(null, null, null) + days
                        cells.chunked(7).forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                week.forEach { day ->
                                    if (day == null) {
                                        Spacer(Modifier.weight(1f))
                                    } else {
                                        CalendarDay63(
                                            day = day.first,
                                            status = day.second,
                                            modifier = Modifier.weight(1f),
                                            selected = day.first == selectedDay63,
                                            onClick = { selectedDay63 = day.first },
                                        )
                                    }
                                }
                                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalendarLegend63("Alta", Alta63)
                CalendarLegend63("Media", Media63)
                CalendarLegend63("Baja", Baja63)
                CalendarLegend63("Agotado", Agotado63)
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF4E6A10), size = 24.dp)
                BasicText(
                    buildAnnotatedString {
                        val availabilityLabel = when (days.firstOrNull { it.first == selectedDay63 }?.second) {
                            Media63 -> "Disponibilidad media"
                            Baja63 -> "Disponibilidad baja"
                            Agotado63 -> "Agotado"
                            else -> "Disponibilidad alta"
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Día $selectedDay63 · $availabilityLabel\n") }
                        withStyle(SpanStyle(color = Color(0xFF4E6A10))) {
                            if (selectedDay63 == 20) {
                                append("Sin reservas para este día")
                            } else {
                                append("Inventario sincronizado para el día $selectedDay63")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

private val Alta63 = Color(0xFF8DB52C)
private val Media63 = Color(0xFFF7B500)
private val Baja63 = Color(0xFFEA4EA9)
private val Agotado63 = Color(0xFF3B3550)

@Composable
private fun CalendarDay63(
    day: Int,
    status: Color,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFECEBFF) else RfThemeState.currentPalette.bg)
            .then(if (selected) Modifier.border(2.dp, Color(0xFFD63F98), RoundedCornerShape(12.dp)) else Modifier)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(
            day.toString(),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF151729)),
        )
        Spacer(Modifier.height(5.dp))
        Box(modifier = Modifier.width(18.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(status))
    }
}

@Composable
private fun CalendarLegend63(label: String, color: Color) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.width(12.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF151729)))
    }
}

@Composable
internal fun RenderLeadsPipelineS64(onSelectScreenCode: (String) -> Unit = {}) {
    var selectedLeadFilter by remember { mutableStateOf("Todos · 3") }

    LeadsScaffold(activeCode = "s64", onSelectScreenCode = onSelectScreenCode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf("Todos · 3", "Nuevos", "Contactados", "Calificados", "Propuesta").forEach { filterLabel ->
                LeadChip64(
                    label = filterLabel,
                    selected = selectedLeadFilter == filterLabel,
                    onClick = { selectedLeadFilter = filterLabel },
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        val shouldShowMariana = when (selectedLeadFilter) {
            "Todos · 3", "Nuevos" -> true
            else -> false
        }
        if (shouldShowMariana) {
            LeadPipelineCard64(
                name = "Mariana Almonte",
                status = "Nuevo",
                statusBackground = Color(0xFFDDF4F3),
                statusColor = Color(0xFF1F8A84),
                event = "Boda civil y recepción",
                budget = "RD$ 3,000 – 5,000",
                priority = "Prioridad alta",
                priorityColor = Color(0xFF4B47D6),
                priorityDot = Color(0xFF5B6CF0),
                onClick = { onSelectScreenCode("s65") },
            )
        }
        val shouldShowBanco = when (selectedLeadFilter) {
            "Todos · 3", "Propuesta", "Contactados" -> true
            else -> false
        }
        if (shouldShowBanco) {
            LeadPipelineCard64(
                name = "Banco Atlántico RD",
                status = "Propuesta",
                statusBackground = Color(0xFFEFE8FD),
                statusColor = Color(0xFF7A4FD8),
                event = "Aniversario corporativo",
                budget = "RD$ 7,000 – 12,000",
                priority = "Urgente",
                priorityColor = Color(0xFFB42345),
                priorityDot = Color(0xFFB42345),
                onClick = { onSelectScreenCode("s65") },
            )
        }
        val shouldShowPatricia = when (selectedLeadFilter) {
            "Todos · 3", "Calificados" -> true
            else -> false
        }
        if (shouldShowPatricia) {
            LeadPipelineCard64(
                name = "Patricia & Jorge",
                status = "Ganado",
                statusBackground = Color(0xFFEEF6D6),
                statusColor = Color(0xFF4E6A10),
                event = "Boda en playa, Juan Dolio",
                budget = "RD$ 4,000 – 6,000",
                priority = "Prioridad media",
                priorityColor = Color(0xFF7A5700),
                priorityDot = Color(0xFFF7B500),
                onClick = { onSelectScreenCode("s65") },
            )
        }
    }
}

@Composable
private fun LeadsScaffold(
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Leads",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                ReportIconButton(RfIconKind.REFRESH) { onSelectScreenCode(activeCode) }
                BotAddButton72 { onSelectScreenCode("s65") }
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(
                tabs = listOf("Pipeline" to "s64", "Seguimientos" to "s65", "Estadísticas" to "s66"),
                activeCode = activeCode,
                onSelectScreenCode = onSelectScreenCode,
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun LeadChip64(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .height(38.dp)
            .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ReportGradient else Brush.linearGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (selected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF151729),
            ),
        )
    }
}

@Composable
private fun LeadPipelineCard64(
    name: String,
    status: String,
    statusBackground: Color,
    statusColor: Color,
    event: String,
    budget: String,
    priority: String,
    priorityColor: Color,
    priorityDot: Color,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicText(
                name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF151729)),
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(statusBackground)
                    .padding(horizontal = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(status, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = statusColor))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF5F6377), size = 14.dp)
            BasicText(event, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicText(
                budget,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(priorityDot))
                BasicText(priority, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = priorityColor))
            }
        }
    }
}

@Composable
internal fun RenderLeadsFollowupS65(onSelectScreenCode: (String) -> Unit = {}) {
    val typo = LocalRfTypography.current

    LeadsScaffold(activeCode = "s65", onSelectScreenCode = onSelectScreenCode) {
        BasicText(
            buildAnnotatedString {
                append("Pendientes ")
                withStyle(SpanStyle(color = Color(0xFF5F6377), fontWeight = FontWeight.Medium)) { append("· 2") }
            },
            modifier = Modifier.padding(top = 6.dp),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
        )
        LeadFollowupCard65("Enviar catálogo actualizado de mobiliario cristal y flores.", "Vencido · 18 oct")
        LeadFollowupCard65("Llamada de cierre con el departamento de compras.", "Vencido · 18 oct")
    }
}

@Composable
private fun LeadFollowupCard65(task: String, due: String) {
    val typo = LocalRfTypography.current
    var isCompleted by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .clickable { isCompleted = !isCompleted }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCompleted) Color(0xFF8DB52C) else Color.Transparent)
                .border(2.dp, if (isCompleted) Color(0xFF8DB52C) else Color(0xFFCDBFCB), CircleShape)
                .clickable { isCompleted = !isCompleted },
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicText(
                task,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp, color = Color(0xFF151729)),
            )
            Row(
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isCompleted) Color(0xFFEEF6D6) else Color(0xFFFDE3EA))
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                RfIcon(kind = if (isCompleted) RfIconKind.CHECK else RfIconKind.WARNING, tint = if (isCompleted) Color(0xFF4E6A10) else Color(0xFFB42345), size = 13.dp)
                BasicText(if (isCompleted) "Completado" else due, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isCompleted) Color(0xFF4E6A10) else Color(0xFFB42345)))
            }
        }
    }
}

@Composable
internal fun RenderLeadsStatsS66(onSelectScreenCode: (String) -> Unit = {}) {
    val typo = LocalRfTypography.current

    AdminSubScaffold(
        title = "Leads",
        tabs = listOf("Pipeline" to "s64", "Seguimientos" to "s65", "Estadísticas" to "s66"),
        activeCode = "s66",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            ReportIconButton(RfIconKind.REFRESH) { onSelectScreenCode("s66") }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x996E82E6))
                .clip(RoundedCornerShape(24.dp))
                .background(ReportGradient)
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    BasicText("Tasa de conversión", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
                    Spacer(Modifier.height(8.dp))
                    BasicText("33.3%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, lineHeight = 46.sp, color = Color.White))
                }
                BasicText(
                    "1 de 3 leads\nganados",
                    style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 14.sp, textAlign = TextAlign.End),
                )
            }
        }

        ReportCard(contentPadding = PaddingValues(18.dp)) {
            BasicText("Embudo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FunnelRow66("Total", "3", 1f, Color(0xFF865DEC))
                FunnelRow66("Nuevos", "1", 0.33f, Color(0xFF1FBDC0))
                FunnelRow66("Calificados", "0", 0f, Color(0xFF865DEC))
                FunnelRow66("Ganados", "1", 0.33f, Color(0xFF8DB52C))
                FunnelRow66("Perdidos", "0", 0f, Color(0xFFB42345))
            }
        }
    }
}

private val ReportTabs = listOf("Ingresos" to "s67", "Eventos" to "s68", "Clientes" to "s69", "Inventario" to "s70")
private val ReportGradient = Brush.linearGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
)

@Composable
internal fun RenderReportsIncomeS67(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val payments = viewModel?.payments?.collectAsState()?.value ?: emptyList()
    val totalIngresos = if (payments.isNotEmpty()) payments.sumOf { it.amountDop } else 45230
    val totalGastos = totalIngresos * 28 / 100
    val gananciaNeta = totalIngresos - totalGastos

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s67",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            PeriodSelector67()
            ReportIconButton(RfIconKind.DOWNLOAD) {}
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x996E82E6))
                .clip(RoundedCornerShape(24.dp))
                .background(ReportGradient)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                BasicText("Ganancia neta", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
                BasicText("RD$ $gananciaNeta", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 40.sp, color = Color.White))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ReportMetric("Ingresos", "RD$ $totalIngresos")
                ReportMetric("Gastos", "RD$ $totalGastos")
            }
        }

        ReportCard(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
            BasicText("Ingresos por mes", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(14.dp))
            IncomeChart67()
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun").forEach { month ->
                    BasicText(
                        month,
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                }
            }
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("De dónde vienen", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BreakdownBar("Alquiler de equipos", "RD$ 28,500", 0.63f, Color(0xFFD63F98))
                BreakdownBar("Decoración", "RD$ 12,200", 0.27f, Color(0xFF865DEC))
                BreakdownBar("Servicios adicionales", "RD$ 4,530", 0.10f, Color(0xFF8DB52C))
            }
        }
    }
}

@Composable
private fun PeriodSelector67() {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText("Ene–Jun", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
        RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF151729), size = 16.dp)
    }
}

@Composable
private fun ReportMetric(label: String, value: String) {
    val typo = LocalRfTypography.current

    Column {
        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
        BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White))
    }
}

@Composable
private fun IncomeChart67() {
    val typo = LocalRfTypography.current
    val bars = listOf(
        Triple(72, "12k", Color(0xFFF9C7DD)),
        Triple(90, "15k", Color(0xFFF9C7DD)),
        Triple(48, "8k", Color(0xFFF9C7DD)),
        Triple(108, "18k", Color(0xFFF9C7DD)),
        Triple(84, "14k", Color(0xFFF9C7DD)),
        Triple(96, "16k", Color(0xFFD63F98)),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(Color(0xFFF3F1F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                val midY = size.height * 0.5f
                drawLine(Color(0xFFF3F1F6), Offset(0f, midY), Offset(size.width, midY), stroke)
            }
            .padding(horizontal = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEachIndexed { index, bar ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BasicText(
                        bar.second,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (index == bars.lastIndex) Color(0xFFC2338F) else Color(0xFF5F6377),
                        ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bar.first.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(bar.third),
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownBar(label: String, value: String, fraction: Float, color: Color) {
    val typo = LocalRfTypography.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF151729), fontSize = 14.sp))
            BasicText(value, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
        }
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F1F6))) {
            if (fraction > 0f) {
                Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
            }
        }
    }
}

@Composable
internal fun RenderReportsEventsS68(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val allEvents = viewModel?.events?.collectAsState()?.value ?: emptyList()
    val totalCount = if (allEvents.isNotEmpty()) allEvents.size else 156
    val completados = if (allEvents.isNotEmpty()) allEvents.count { it.status == EventStatus.COMPLETED } else 134
    val enProceso = if (allEvents.isNotEmpty()) allEvents.count { it.status != EventStatus.COMPLETED && it.status != EventStatus.CANCELLED } else 18
    val cancelados = if (allEvents.isNotEmpty()) allEvents.count { it.status == EventStatus.CANCELLED } else 4

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s68",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            PeriodSelector67()
            ReportIconButton(RfIconKind.DOWNLOAD) {}
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                BasicText("Eventos en total", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                BasicText("$totalCount", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 44.sp, color = Color(0xFF151729)))
            }
            EventRingIllustration68(size = 96.dp)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile68("$completados", "Completados", Color(0xFFEEF6D6), Color(0xFF3F560C), Color(0xFF4E6A10), Modifier.weight(1f))
            StatTile68("$enProceso", "En proceso", Color(0xFFFFF2C9), Color(0xFF6E4300), Color(0xFF7A5700), Modifier.weight(1f))
            StatTile68("$cancelados", "Cancelados", Color(0xFFFDE3EA), Color(0xFF8E1A35), Color(0xFFB42345), Modifier.weight(1f))
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("Por tipo de evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EventTypeRow68("Bodas", "70", "45%", 0.45f, Color(0xFFD63F98), RfIconKind.RING, Color(0xFFECEBFF), Color(0xFF4B47D6))
                EventTypeRow68("Cumpleaños", "39", "25%", 0.25f, Color(0xFFF7B500), RfIconKind.PARTY, Color(0xFFFFF2C9), Color(0xFF7A5700))
                EventTypeRow68("Otros", "24", "15%", 0.15f, Color(0xFF865DEC), RfIconKind.SPARKLE, Color(0xFFEFE8FD), Color(0xFF7A4FD8))
                EventTypeRow68("Corporativos", "23", "15%", 0.15f, Color(0xFF1FBDC0), RfIconKind.BOX, Color(0xFFDDF4F3), Color(0xFF1F8A84))
            }
        }
    }
}

@Composable
private fun StatTile68(
    value: String,
    label: String,
    background: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .padding(12.dp),
    ) {
        BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, color = valueColor))
        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = labelColor))
    }
}

@Composable
private fun EventTypeRow68(
    label: String,
    count: String,
    percent: String,
    fraction: Float,
    barColor: Color,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 18.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
                Row {
                    BasicText(count, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
                    BasicText(" · $percent", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F1F6))) {
                Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(RoundedCornerShape(4.dp)).background(barColor))
            }
        }
    }
}

@Composable
internal fun RenderReportsClientsS69(onSelectScreenCode: (String) -> Unit = {}) {
    val typo = LocalRfTypography.current

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s69",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            PeriodSelector67()
            ReportIconButton(RfIconKind.DOWNLOAD) {}
        },
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .shadow(18.dp, RoundedCornerShape(22.dp), spotColor = Color(0x996E82E6))
                    .clip(RoundedCornerShape(22.dp))
                    .background(ReportGradient)
                    .padding(16.dp),
            ) {
                BasicText("Clientes", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
                BasicText("248", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, color = Color.White))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                    .padding(16.dp),
            ) {
                BasicText("Nuevos este mes", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                BasicText("+18", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, color = Color(0xFF4E6A10)))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            BasicText(
                "Mejores clientes",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            TopClientRow69("1", "María González", "5 eventos", "RD$ 42,500", Color(0xFFFFF2C9), Color(0xFF7A5700))
            TopClientRow69("2", "Carlos Rodríguez", "4 eventos", "RD$ 38,200", Color(0xFFEDEDF5), Color(0xFF5F6377))
            TopClientRow69("3", "Ana Martínez", "3 eventos", "RD$ 28,900", Color(0xFFECEBFF), Color(0xFF4B47D6))
            Spacer(Modifier.height(6.dp))
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("Retención", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Box(Modifier.weight(0.45f).fillMaxSize().background(Color(0xFFEA4EA9)))
                Box(Modifier.weight(0.38f).fillMaxSize().background(Color(0xFF8DB52C)))
                Box(Modifier.weight(0.17f).fillMaxSize().background(Color(0xFFF7B500)))
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RetentionLegend("Primera vez", "45%", Color(0xFFEA4EA9))
                RetentionLegend("Recurrentes", "38%", Color(0xFF8DB52C))
                RetentionLegend("VIP", "17%", Color(0xFFF7B500))
            }
        }
    }
}

@Composable
private fun TopClientRow69(
    rank: String,
    name: String,
    events: String,
    amount: String,
    rankBackground: Color,
    rankColor: Color,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).background(rankBackground),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(rank, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = rankColor))
        }
        Column(modifier = Modifier.weight(1f)) {
            BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            BasicText(events, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
        }
        BasicText(amount, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF151729)))
    }
}

@Composable
private fun RetentionLegend(label: String, value: String, color: Color) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        BasicText(label, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)))
        BasicText(value, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
    }
}

@Composable
internal fun RenderReportsInventoryS70(onSelectScreenCode: (String) -> Unit = {}) {
    val typo = LocalRfTypography.current

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s70",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            ReportIconButton(RfIconKind.DOWNLOAD) {}
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(76.dp)) {
                    val stroke = 9.dp.toPx()
                    val radius = (size.minDimension - stroke) / 2f
                    drawCircle(Color(0xFFF3F1F6), radius = radius, style = Stroke(stroke))
                    drawArc(
                        color = Color(0xFFD63F98),
                        startAngle = -90f,
                        sweepAngle = 259f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
                BasicText("72%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF151729)))
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    BasicText("892 ", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 30.sp, color = Color(0xFF151729)))
                    BasicText("de 1,245", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF5F6377)))
                }
                Spacer(Modifier.height(4.dp))
                BasicText("artículos en uso ahora", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InventoryAlertTile70("23", "Stock bajo", Color(0xFFFFF2C9), Color(0xFF7A5700), RfIconKind.WARNING, Modifier.weight(1f))
            InventoryAlertTile70("8", "En mantenimiento", Color(0xFFEFE8FD), Color(0xFF7A4FD8), RfIconKind.SETTINGS, Modifier.weight(1f))
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("Uso por categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryUsageRow70("Sillas", 0.92f, "92%", 1f)
                CategoryUsageRow70("Decoración", 0.88f, "88%", 0.85f)
                CategoryUsageRow70("Mesas", 0.80f, "80%", 0.7f)
                CategoryUsageRow70("Mantelería", 0.76f, "76%", 0.55f)
                CategoryUsageRow70("Iluminación", 0.64f, "64%", 0.4f)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                .clip(RoundedCornerShape(18.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CHAIR, tint = Color(0xFF4E6A10), size = 20.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText("Sillas plegables", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
                BasicText("Próximo mantenimiento · limpieza profunda", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
            }
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ReportGradient)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("20 abr", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White))
            }
        }
    }
}

@Composable
private fun InventoryAlertTile70(
    value: String,
    label: String,
    background: Color,
    tint: Color,
    icon: RfIconKind,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(background).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RfIcon(kind = icon, tint = tint, size = 20.dp)
        Column {
            BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 24.sp, color = tint))
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = tint))
        }
    }
}

@Composable
private fun CategoryUsageRow70(label: String, fraction: Float, value: String, opacity: Float) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BasicText(label, modifier = Modifier.width(92.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
        Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFF3F1F6))) {
            Box(Modifier.fillMaxWidth(fraction).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF5B6CF0).copy(alpha = opacity)))
        }
        BasicText(value, modifier = Modifier.width(38.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729), textAlign = TextAlign.End))
    }
}

@Composable
private fun FunnelRow66(label: String, value: String, fraction: Float, color: Color) {
    val typo = LocalRfTypography.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
            BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF151729)))
        }
        Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF3F1F6))) {
            if (fraction > 0f) {
                Box(Modifier.fillMaxWidth(fraction).height(12.dp).clip(RoundedCornerShape(6.dp)).background(color))
            }
        }
    }
}

@Composable
private fun EventRingIllustration68(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val scale = this.size.minDimension / 120f
        fun circle(x: Float, y: Float, r: Float, color: Color) = drawCircle(color = color, radius = r * scale, center = Offset(x * scale, y * scale))

        drawOval(
            color = Color(0x142B1730),
            topLeft = Offset(10f * scale, 106.5f * scale),
            size = Size(100f * scale, 7f * scale),
        )
        circle(9f, 91f, 6.5f, Color(0xFFF9B6D3))
        circle(26.6f, 60f, 6.5f, Color(0xFFFFD98A))
        circle(60f, 47f, 6.5f, Color(0xFFB9A6F7))
        circle(93.4f, 60f, 6.5f, Color(0xFFA8D8F5))
        circle(111f, 91f, 6.5f, Color(0xFF9BE3C9))

        val ring = listOf(
            Triple(18f, 104f, Color(0xFFEC4F97)),
            Triple(20.5f, 86f, Color(0xFFF5B53B)),
            Triple(28f, 72f, Color(0xFF7650E8)),
            Triple(39f, 62.5f, Color(0xFF4AA8E0)),
            Triple(52.7f, 58f, Color(0xFF2FB58F)),
            Triple(67.3f, 58f, Color(0xFFEC4F97)),
            Triple(81f, 62.5f, Color(0xFFF5B53B)),
            Triple(92f, 72f, Color(0xFF7650E8)),
            Triple(99.5f, 86f, Color(0xFF4AA8E0)),
            Triple(102f, 104f, Color(0xFF2FB58F)),
        )
        ring.forEach { circle(it.first, it.second, 10.5f, it.third) }

        listOf(15f to 100f, 36f to 58.5f, 64f to 54f, 89f to 68f, 99f to 100f).forEach { (x, y) ->
            drawOval(
                color = Color(0x8CFFFFFF),
                topLeft = Offset((x - 2.4f) * scale, (y - 3.4f) * scale),
                size = Size(4.8f * scale, 6.8f * scale),
            )
        }

        val star = Path().apply {
            moveTo(60f * scale, 22f * scale)
            lineTo(61.6f * scale, 26.4f * scale)
            lineTo(66f * scale, 28f * scale)
            lineTo(61.6f * scale, 29.6f * scale)
            lineTo(60f * scale, 34f * scale)
            lineTo(58.4f * scale, 29.6f * scale)
            lineTo(54f * scale, 28f * scale)
            lineTo(58.4f * scale, 26.4f * scale)
            close()
        }
        drawPath(star, Color(0xFFF5B53B))
    }
}

@Composable
private fun ReportIconButton(icon: RfIconKind, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .rfIconButtonShadow(CircleShape, isAdmin = true)
            .clip(CircleShape)
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RfIcon(kind = icon, tint = Color(0xFF151729), size = 21.dp)
    }
}

@Composable
private fun ReportCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .padding(contentPadding),
        content = content,
    )
}

@Composable
private fun AdminSubScaffold(
    title: String,
    tabs: List<Pair<String, String>>,
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    title,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                trailing()
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(tabs = tabs, activeCode = activeCode, onSelectScreenCode = onSelectScreenCode)
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun AdminTabRow(
    tabs: List<Pair<String, String>>,
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    initialScroll: Dp = 0.dp,
) {
    val typo = LocalRfTypography.current
    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Designs for the last tabs shift the row left so the active tab stays fully visible.
    LaunchedEffect(initialScroll) {
        scrollState.scrollTo(with(density) { initialScroll.roundToPx() })
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(Color(0xFFE9E9F2), Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            tabs.forEach { (label, code) ->
                val active = code == activeCode
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clickable { if (!active) onSelectScreenCode(code) }
                        .drawBehind {
                            if (active) {
                                val stroke = 2.5.dp.toPx()
                                drawLine(Color(0xFF5B6CF0), Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        label,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (active) Color(0xFF151729) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderReviewModerationS71(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFilter by remember { mutableStateOf("Pendientes") }
    var ricardoStatus by remember { mutableStateOf("PENDING") }
    val companyReviews = viewModel?.companyReviews?.collectAsState()?.value ?: emptyList()

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Reseñas",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val pendingCount = if (ricardoStatus == "PENDING") "3" else "2"
                val verifiedCount = if (ricardoStatus == "VERIFIED") "11" else "10"
                val rejectedCount = if (ricardoStatus == "REJECTED") "2" else "1"
                ReviewChip71("Pendientes", pendingCount, selected = selectedFilter == "Pendientes") { selectedFilter = "Pendientes" }
                ReviewChip71("Verificadas", verifiedCount, selected = selectedFilter == "Verificadas") { selectedFilter = "Verificadas" }
                ReviewChip71("Rechazadas", rejectedCount, selected = selectedFilter == "Rechazadas") { selectedFilter = "Rechazadas" }
                ReviewChip71("Todas", null, selected = selectedFilter == "Todas") { selectedFilter = "Todas" }
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                val showRicardo = when (selectedFilter) {
                    "Rechazadas" -> ricardoStatus == "REJECTED"
                    else -> ricardoStatus != "REJECTED"
                }
                if (showRicardo) {
                    ReviewCard71(
                        initial = "R",
                        avatarBackground = Color(0xFFEFE8FD),
                        avatarTint = Color(0xFF7A4FD8),
                        name = "Ricardo Méndez",
                        event = "Gala Corporativa Grupo Caribe · 13 oct",
                        body = "¡Superó todas nuestras expectativas! El arco orgánico y las mesas imperiales lucieron espectaculares.",
                        response = if (ricardoStatus == "PENDING") "¡Gracias, Ricardo! Fue un honor acompañarles en su gala." else null,
                        verified = ricardoStatus == "VERIFIED",
                        onEditResponse = { onSelectScreenCode("A34") },
                        onReject = { ricardoStatus = "REJECTED" },
                        onVerify = { ricardoStatus = "VERIFIED" },
                    )
                }
                val showSofia = selectedFilter != "Rechazadas"
                if (showSofia) {
                    companyReviews.forEach { userReview ->
                        ReviewCard71(
                            initial = userReview.author.take(1).uppercase().ifBlank { "C" },
                            avatarBackground = Color(0xFFECEBFF),
                            avatarTint = Color(0xFF4B47D6),
                            name = userReview.author,
                            event = "Evento verificado · ${userReview.dateLabel}",
                            body = userReview.comment,
                            verified = true,
                        )
                    }
                    ReviewCard71(
                        initial = "S",
                        avatarBackground = Color(0xFFECEBFF),
                        avatarTint = Color(0xFF4B47D6),
                        name = "Sofía Hernández",
                        event = "Quinceañero Primaveral · 19 oct",
                        body = "Puntualidad impecable y mobiliario en perfecto estado. Recomendados al 100%.",
                        verified = true,
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ReviewChip71(
    label: String,
    count: String?,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .height(38.dp)
            .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ReportGradient else Brush.linearGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (selected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF151729),
            ),
        )
        if (count != null) {
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .widthIn(min = 22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) {
                            Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFFEDEDF5), Color(0xFFEDEDF5)))
                        },
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    count,
                    maxLines = 1,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else Color(0xFF5F6377),
                    ),
                )
            }
        }
    }
}

@Composable
private fun ReviewCard71(
    initial: String,
    avatarBackground: Color,
    avatarTint: Color,
    name: String,
    event: String,
    body: String,
    response: String? = null,
    verified: Boolean = false,
    onEditResponse: () -> Unit = {},
    onReject: () -> Unit = {},
    onVerify: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarBackground),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(initial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = avatarTint))
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
                BasicText(event, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF5F6377)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) {
                    RfIcon(kind = RfIconKind.STAR, tint = Color(0xFFF7B500), size = 15.dp, filled = true)
                }
            }
        }
        BasicText(
            body,
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF151729)),
        )
        if (response != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF6F6FB))
                    .clickable(onClick = onEditResponse)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RfIcon(kind = RfIconKind.UNDO, tint = Color(0xFF5F6377), size = 14.dp, modifier = Modifier.padding(top = 2.dp))
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Tu respuesta: ") }
                        append(response)
                        append(" ")
                        withStyle(SpanStyle(color = Color(0xFF4B47D6), fontWeight = FontWeight.Bold)) { append("Editar") }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
                )
            }
            Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color(0xFFF3C3CF), RoundedCornerShape(14.dp))
                        .clickable(onClick = onReject),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB42345), size = 16.dp)
                        BasicText("Rechazar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFB42345)))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReportGradient)
                        .clickable(onClick = onVerify),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                        BasicText("Verificar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White))
                    }
                }
            }
        }
        if (verified) {
            Row(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 13.dp)
                BasicText("Verificada · compra confirmada", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)))
            }
        }
    }
}

@Composable
internal fun RenderBotQuestionsS72(onSelectScreenCode: (String) -> Unit = {}) {
    var extraQuestions by remember { mutableStateOf(emptyList<Triple<String, String, String>>()) }

    BotScaffold(
        activeCode = "s72",
        onSelectScreenCode = onSelectScreenCode,
        action = {
            BotAddButton72 {
                val nextIndex = extraQuestions.size + 1
                extraQuestions = extraQuestions + Triple(
                    "# nueva-$nextIndex",
                    "¿Cuál es el horario de entrega #$nextIndex?",
                    "Coordinamos entrega e instalación 4 horas antes del inicio del evento.",
                )
            }
        },
    ) {
        BotQuestionCard72("# precio", "¿Cuánto cuesta?", "Nuestros precios varían según el tipo de evento. Contáctanos para una cotización personalizada.")
        BotQuestionCard72("# disponibilidad", "¿Está disponible?", "Para verificar disponibilidad necesitamos la fecha de tu evento. ¿Cuándo es?")
        BotQuestionCard72("# alquiler", "¿Cómo funciona el alquiler?", "Incluye entrega, montaje y recogida. El período típico es de 1 a 3 días.")
        BotQuestionCard72("# boda", "¿Ofrecen servicio para bodas?", "Sí, tenemos paquetes especiales para bodas: decoración completa, mesas, sillas, iluminación y más.")
        extraQuestions.forEach { questionTriple ->
            BotQuestionCard72(questionTriple.first, questionTriple.second, questionTriple.third)
        }
    }
}

@Composable
private fun BotScaffold(
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    action: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        "Bot de WhatsApp",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = Color(0xFF151729)),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF8DB52C)))
                        BasicText("Activo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)))
                    }
                }
                action()
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(
                tabs = listOf("Preguntas" to "s72", "Palabras clave" to "s73", "Chats" to "s74"),
                activeCode = activeCode,
                onSelectScreenCode = onSelectScreenCode,
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun BotAddButton72(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(8.dp, CircleShape, spotColor = Color(0x995B6CF0))
            .clip(CircleShape)
            .background(ReportGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
    }
}

@Composable
private fun BotKeywordChip72(text: String) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFEFE8FD))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicText(text, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7A4FD8)))
    }
}

@Composable
private fun BotQuestionCard72(tag: String, question: String, answer: String) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BotKeywordChip72(tag)
            BasicText(question, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            BasicText(answer, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF5F6377)))
        }
        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFF5F6377), size = 18.dp)
        }
    }
}

@Composable
internal fun RenderBotKeywordsS73(onSelectScreenCode: (String) -> Unit = {}) {
    val typo = LocalRfTypography.current
    var extraKeywords by remember { mutableStateOf(emptyList<Triple<String, String, String>>()) }
    val rows = listOf(
        Triple("precio", "¿Cuánto cuesta?", "1 respuesta"),
        Triple("disponibilidad", "¿Está disponible?", "1 respuesta"),
        Triple("alquiler", "¿Cómo funciona el alquiler?", "1 respuesta"),
        Triple("boda", "¿Ofrecen servicio para bodas?", "1 respuesta"),
    ) + extraKeywords

    BotScaffold(
        activeCode = "s73",
        onSelectScreenCode = onSelectScreenCode,
        action = {
            BotAddButton72 {
                val nextIndex = extraKeywords.size + 1
                extraKeywords = extraKeywords + Triple("entrega-$nextIndex", "¿Incluye transporte?", "1 respuesta")
            }
        },
    ) {
        BasicText(
            "Si un cliente escribe una de estas palabras, el bot responde con la pregunta vinculada.",
            modifier = Modifier.padding(top = 4.dp),
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF5F6377)),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            if (index > 0) {
                                val stroke = 1.dp.toPx()
                                drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .clickable { onSelectScreenCode("s72") },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFEFE8FD))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(row.first, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7A4FD8)))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(row.second, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
                        BasicText(row.third, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 16.dp)
                }
            }
        }
    }
}

@Composable
internal fun RenderBotConversationsS74(onSelectScreenCode: (String) -> Unit = {}) {
    var valeriaResolved by remember { mutableStateOf(false) }

    BotScaffold(
        activeCode = "s74",
        onSelectScreenCode = onSelectScreenCode,
        action = { ReportIconButton(RfIconKind.SEARCH) { onSelectScreenCode("s73") } },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            BotConversationRow74(
                initials = "VS",
                name = "Valeria Santos",
                time = "8:46 a. m.",
                preview = "Hola, ¿tienen disponibilidad de sillas Tiffany doradas para el 24 de octubre?",
                status = if (valeriaResolved) "Resuelta" else "Pendiente",
                statusBackground = if (valeriaResolved) Color(0xFFEEF6D6) else Color(0xFFFFF2C9),
                statusColor = if (valeriaResolved) Color(0xFF4E6A10) else Color(0xFF7A5700),
                phone = "+1 829 555 0198",
                avatarBackground = Color(0xFFECEBFF),
                avatarTint = Color(0xFF4B47D6),
                divider = false,
                onClick = { valeriaResolved = !valeriaResolved },
            )
            BotConversationRow74(
                initials = "CR",
                name = "Camila Rodríguez",
                time = "11:46 a. m.",
                preview = "¡Perfecto! Quisiera agendar la visita para ver el arco orgánico doble.",
                status = "Resuelta",
                statusBackground = Color(0xFFEEF6D6),
                statusColor = Color(0xFF4E6A10),
                phone = "+1 809 555 0142",
                avatarBackground = Color(0xFFEFE8FD),
                avatarTint = Color(0xFF7A4FD8),
                divider = true,
                onClick = { onSelectScreenCode("s62") },
            )
            BotConversationRow74(
                initials = "AM",
                name = "Andrés Mejía",
                time = "Ayer",
                preview = "Muchas gracias por enviarme la cotización actualizada del paquete VIP.",
                status = "Resuelta",
                statusBackground = Color(0xFFEEF6D6),
                statusColor = Color(0xFF4E6A10),
                phone = "+1 849 555 0271",
                avatarBackground = Color(0xFFDDF4F3),
                avatarTint = Color(0xFF1F8A84),
                divider = true,
                onClick = { onSelectScreenCode("s62") },
            )
        }
    }
}

@Composable
private fun BotConversationRow74(
    initials: String,
    name: String,
    time: String,
    preview: String,
    status: String,
    statusBackground: Color,
    statusColor: Color,
    phone: String,
    avatarBackground: Color,
    avatarTint: Color,
    divider: Boolean,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(avatarBackground),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(initials, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = avatarTint))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                BasicText(
                    name,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
                )
                BasicText(time, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
            }
            BasicText(preview, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF5F6377)))
            Row(
                modifier = Modifier.padding(top = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusBackground)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(status, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = statusColor))
                }
                BasicText(phone, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
            }
        }
    }
}

@Composable
private fun AdminPanelScaffold(
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    tabScroll: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Panel",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                BasicText("Octubre 2026", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF5F6377)))
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(
                tabs = listOf(
                    "Resumen" to "s58",
                    "Finanzas" to "s59",
                    "Seguros" to "s60",
                    "Auditoría" to "s61",
                    "Clientes" to "s62",
                ),
                activeCode = activeCode,
                onSelectScreenCode = onSelectScreenCode,
                initialScroll = tabScroll,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
internal fun RenderInsurancePanelS60(onSelectScreenCode: (String) -> Unit = {}) {
    val typo = LocalRfTypography.current

    AdminPanelScaffold(activeCode = "s60", onSelectScreenCode = onSelectScreenCode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFDDF4F3))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RfIcon(kind = RfIconKind.SHIELD, tint = Color(0xFF1F8A84), size = 22.dp)
            BasicText(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("3 pólizas activas\n") }
                    withStyle(SpanStyle(color = Color(0xFF5F6377))) { append("Todas con Seguros Universal RD") }
                },
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 19.sp, color = Color(0xFF151729)),
            )
        }
        PolicyRow60("POL-RF-5931B", onClick = { onSelectScreenCode("s61") })
        PolicyRow60("POL-RF-90892", onClick = { onSelectScreenCode("s61") })
        PolicyRow60("POL-RF-34B92", onClick = { onSelectScreenCode("s61") })
    }
}

@Composable
private fun PolicyRow60(
    code: String,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            BasicText(
                code,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            BasicText(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF5F6377))) { append("Cobertura ") }
                    withStyle(SpanStyle(color = Color(0xFF151729), fontWeight = FontWeight.Bold)) { append("RD$ 2,500") }
                },
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
            )
        }
        Box(
            modifier = Modifier
                .height(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFEEF6D6))
                .padding(horizontal = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText("Activa", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)))
        }
        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 16.dp)
    }
}

@Composable
internal fun RenderAuditPanelS61(onSelectScreenCode: (String) -> Unit = {}) {
    var selectedAuditFilter by remember { mutableStateOf("Todo") }

    AdminPanelScaffold(activeCode = "s61", onSelectScreenCode = onSelectScreenCode, tabScroll = 56.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AuditChip61("Todo", selected = selectedAuditFilter == "Todo") { selectedAuditFilter = "Todo" }
            AuditChip61("Eventos", selected = selectedAuditFilter == "Eventos") { selectedAuditFilter = "Eventos" }
            AuditChip61("Pagos", selected = selectedAuditFilter == "Pagos") { selectedAuditFilter = "Pagos" }
        }
        Spacer(Modifier.height(2.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            val showEvents = when (selectedAuditFilter) {
                "Todo", "Eventos" -> true
                else -> false
            }
            val showPayments = when (selectedAuditFilter) {
                "Todo", "Pagos" -> true
                else -> false
            }
            if (showEvents) {
                AuditTimelineRow61("19 oct · 9:05 p. m.", "Cotización ajustada y enviada", "Evento", RfIconKind.NOTE, Color(0xFFECEBFF), Color(0xFF4B47D6), isLast = false)
            }
            if (showPayments) {
                AuditTimelineRow61("18 oct · 11:05 p. m.", "Abono inicial registrado · RD$ 140", "Pago", RfIconKind.WALLET, Color(0xFFEEF6D6), Color(0xFF4E6A10), isLast = !showEvents)
            }
            if (showEvents) {
                AuditTimelineRow61("14 oct · 11:05 p. m.", "Evento finalizado e inventario recibido", "Evento", RfIconKind.CHECK, Color(0xFFEFE8FD), Color(0xFF7A4FD8), isLast = true)
            }
        }
    }
}

@Composable
private fun AuditChip61(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .height(36.dp)
            .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ReportGradient else Brush.linearGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (selected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF151729),
            ),
        )
    }
}

@Composable
private fun AuditTimelineRow61(
    date: String,
    title: String,
    category: String,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    isLast: Boolean,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier.width(40.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = icon, tint = iconTint, size = 18.dp)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(2.dp)
                        .padding(vertical = 6.dp)
                        .background(Color(0xFFE3E4EF)),
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(top = 2.dp, bottom = 22.dp)) {
            BasicText(
                date.uppercase(),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.7.sp, color = Color(0xFF5F6377)),
            )
            Spacer(Modifier.height(4.dp))
            BasicText(
                title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
            )
            Spacer(Modifier.height(2.dp))
            BasicText(category, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
        }
    }
}

@Composable
internal fun RenderClientsAnalyticsS62(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val clientName = viewModel?.profileName?.collectAsState()?.value ?: "Camila Rodríguez"
    val clientEmail = viewModel?.profileEmail?.collectAsState()?.value ?: "camila.rodriguez@correo.com"
    val clientInitial = clientName.trim().take(1).uppercase().ifBlank { "C" }
    val activeEvent = viewModel?.activeClientEvent()
    val eventsCount = viewModel?.events?.collectAsState()?.value?.size ?: 3

    AdminPanelScaffold(activeCode = "s62", onSelectScreenCode = onSelectScreenCode, tabScroll = 56.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFECEBFF)),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF4B47D6)))
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(clientName, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF151729)))
                BasicText(clientEmail, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
            }
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(RfThemeState.currentPalette.surface).border(1.dp, Color(0xFFECECF5), CircleShape).clickable { onSelectScreenCode("s74") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 20.dp)
            }
        }

        BasicText(
            "PRÓXIMO EVENTO",
            modifier = Modifier.padding(top = 6.dp),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFF2C9))
                .clickable { onSelectScreenCode("A13") }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.width(52.dp).height(56.dp).clip(RoundedCornerShape(14.dp)).background(RfThemeState.currentPalette.surface),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BasicText("23", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, lineHeight = 21.sp, color = Color(0xFF151729)))
                BasicText("OCT", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.7.sp, color = Color(0xFF151729)))
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(activeEvent?.title ?: "Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF151729)))
                BasicText(activeEvent?.dateLabel ?: "Viernes · 5:38 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF7A5700)))
            }
            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF7A5700), size = 18.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            ClientActionRow62("Eventos del cliente", RfIconKind.CALENDAR, Color(0xFFEFE8FD), Color(0xFF7A4FD8), badge = eventsCount.toString(), divider = false, onClick = { onSelectScreenCode("A11") })
            ClientActionRow62("Pagos", RfIconKind.WALLET, Color(0xFFEEF6D6), Color(0xFF4E6A10), divider = true, onClick = { onSelectScreenCode("s59") })
            ClientActionRow62("Notificaciones", RfIconKind.BELL, Color(0xFFDDF4F3), Color(0xFF1F8A84), divider = true, onClick = { onSelectScreenCode("A29") })
        }
    }
}

@Composable
private fun ClientActionRow62(
    label: String,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    badge: String? = null,
    divider: Boolean,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 18.dp)
        }
        BasicText(
            label,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF151729)),
        )
        if (badge != null) {
            BasicText(badge, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF5F6377)))
        }
        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 16.dp)
    }
}

@Composable
private fun RenderGenericPersonalSubPanel(
    title: String,
    subtitle: String,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(text = title, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
                    BasicText(text = subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFFFF3366), size = 24.dp)
                        BasicText("Sincronizado con Rosa IA", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 16.sp))
                    }
                    BasicText("Información actualizada en tiempo real conforme a los eventos y cotizaciones activas de Rosa Fiesta.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
