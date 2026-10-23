package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey

private val PersonalButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF3366), Color(0xFFE6007A)),
)

// ==============================================================================
// s16 · ACERCA DE ROSA FIESTA
// ==============================================================================
@Composable
internal fun RenderAboutScreenS16(
    onSelectScreenCode: (String) -> Unit = {},
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText(
                    text = "Acerca de",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Rosa Fiesta SRL", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E24), fontSize = 22.sp))
                    BasicText("Decoración y planificación de eventos inolvidables en República Dominicana.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 14.sp))

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            BasicText("127", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 24.sp))
                            BasicText("eventos realizados", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        Column {
                            BasicText("8", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 24.sp))
                            BasicText("años de experiencia", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(18.dp))
                    .clickable { onSelectScreenCode("s17") }
                    .padding(16.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        BasicText("Opiniones y Reseñas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
                        BasicText("Calificación promedio 5.0 ★★★★★", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                    }
                    RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFFFF3366), size = 18.dp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s17 · RESEÑAS DE LA EMPRESA
// ==============================================================================
@Composable
internal fun RenderCompanyReviewsS17(
    onSelectScreenCode: (String) -> Unit = {},
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s16") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText(
                    text = "Reseñas",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BasicText("5.0", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E24), fontSize = 36.sp))
                Column {
                    BasicText("★★★★★", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF59E0B), fontSize = 16.sp))
                    BasicText("Basado en eventos reales verificados", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val reviews = listOf(
                Pair("Patricia Lora", "Baby shower de ensueño. Las mesas y el arco floral superaron mis expectativas. 100% recomendados."),
                Pair("Grupo Caribe", "Gala corporativa impecable. Montaje puntual y atención a cada detalle."),
            )

            reviews.forEach { (author, text) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(author, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
                            BasicText("★★★★★", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF59E0B), fontSize = 12.sp))
                        }
                        BasicText(text, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
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
) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        scale(s, s, Offset.Zero) {
            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
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
            .background(Color(0xFFF1EFF5))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF62627A),
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
internal fun RenderEventChatTabS41(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 90.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                        .clickable { onSelectScreenCode("s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M19 12H5M11 18l-6-6 6-6",
                        size = 22.dp,
                        tint = Color(0xFF1A1A2E),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = "Vie 23 oct · Jardín Botánico",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF62627A),
                        ),
                        maxLines = 1,
                    )
                }

                // Download PDF
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EBF4), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M12 4v11M7 10.5l5 5 5-5M5 20h14",
                        size = 20.dp,
                        tint = Color(0xFF1A1A2E),
                    )
                }

                // Share
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EBF4), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M15 6a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M15 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M8.6 10.6l6.8-3.6M8.6 13.4l6.8 3.6",
                        size = 20.dp,
                        tint = Color(0xFF1A1A2E),
                    )
                }
            }

            // Tabs: Detalles / Chat
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
                                color = Color(0xFF62627A),
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
                                    color = Color(0xFF1A1A2E),
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
                        .background(Color(0xFFEEE9F2)),
                )
            }

            // Team Status Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(16.dp))
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
                        text = "Equipo Rosa Fiesta",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
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
                            text = "En línea",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }
            }

            // Messages section
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
                    ChatDateBadge("Ayer")
                }

                ChatMessageBubble(
                    text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                    time = "7:38 p. m.",
                )

                ChatMessageBubble(
                    text = "Ya revisé el presupuesto ajustado y el montaje en el Jardín Botánico nos encanta.",
                    time = "9:08 p. m.",
                )
            }
        }

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5))
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M20 11.5l-8.2 8.2a5 5 0 0 1-7-7L13 4.5a3.3 3.3 0 0 1 4.7 4.7l-8.2 8.2a1.7 1.7 0 0 1-2.4-2.4L14.5 7.5",
                    size = 22.dp,
                    tint = Color(0xFF62627A),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F0F3))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicText(
                    text = "Escribe un mensaje…",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF62627A),
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
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

@Composable
internal fun RenderEventChatS42(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Content container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 126.dp, start = 16.dp, end = 16.dp, bottom = 90.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Event Pill Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(18.dp))
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
                            color = Color(0xFF1A1A2E),
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = "Vie 23 oct · 120 invitados",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF62627A),
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

            // Date Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                ChatDateBadge("Ayer")
            }

            // Messages
            ChatMessageBubble(
                text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                time = "7:38 p. m.",
            )

            ChatMessageBubble(
                text = "Ya revisé el presupuesto ajustado y el montaje en el Jardín Botánico nos encanta.",
                time = "9:08 p. m.",
            )
        }

        // Top Header
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5))
                .padding(start = 12.dp, end = 12.dp, top = 52.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onSelectScreenCode("s40") },
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M19 12H5M11 18l-6-6 6-6",
                    size = 22.dp,
                    tint = Color(0xFF1A1A2E),
                )
            }

            RfLogo(
                customSize = 42.dp,
                insetPadding = false,
            )

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Equipo Rosa Fiesta",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
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
                        text = "En línea",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF4E6A10),
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M12 5h.01M12 12h.01M12 19h.01",
                    size = 22.dp,
                    tint = Color(0xFF1A1A2E),
                    strokeWidth = 2.6f,
                )
            }
        }

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5))
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M8.5 14.5c1.8 2 5.2 2 7 0M9 9.5h.01M15 9.5h.01",
                    size = 22.dp,
                    tint = Color(0xFF62627A),
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F0F3))
                    .padding(start = 16.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    text = "Escribe un mensaje…",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF62627A),
                    ),
                    modifier = Modifier.weight(1f),
                )

                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M3 8.5A1.5 1.5 0 0 1 4.5 7H7l1.5-2.5h7L17 7h2.5A1.5 1.5 0 0 1 21 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5zM8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0",
                        size = 20.dp,
                        tint = Color(0xFF62627A),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
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

// ==============================================================================
// s59 · PANEL FINANZAS CLIENTE / SOCIO
// ==============================================================================
@Composable
internal fun RenderFinanceDashboardS59(
    onSelectScreenCode: (String) -> Unit = {},
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    BasicText("Finanzas del Evento", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
                    BasicText("Octubre 2026 · Desglose presupuestario", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s58") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(20.dp))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Presupuesto Invertido", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                    BasicText("RD$ 10,360", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 24.sp))
                    BasicText("Abonado: RD$ 3,210 · Pendiente: RD$ 7,150", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1E1E24), fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s60 a s74 · PANELES DE ANALÍTICA, LEADS, REPORTES Y BOT WHATSAPP
// ==============================================================================
@Composable
internal fun RenderInsurancePanelS60(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Pólizas y Seguros", "Cobertura activa con Seguros Universal RD", onSelectScreenCode)

@Composable
internal fun RenderAuditPanelS61(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Auditoría de Eventos", "Registro y trazabilidad de cambios en tiempo real", onSelectScreenCode)

@Composable
internal fun RenderClientsAnalyticsS62(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Cartera de Clientes", "Métricas de recurrencia y satisfacción", onSelectScreenCode)

@Composable
internal fun RenderInventoryCalendarS63(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Disponibilidad de Mobiliario", "Calendario de ocupación por fecha", onSelectScreenCode)

@Composable
internal fun RenderLeadsPipelineS64(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Pipeline de Solicitudes", "3 prospectos calificados para cotización", onSelectScreenCode)

@Composable
internal fun RenderLeadsFollowupS65(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Seguimientos Pendientes", "2 clientes esperando llamada de confirmación", onSelectScreenCode)

@Composable
internal fun RenderLeadsStatsS66(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Estadísticas de Conversión", "33.3% tasa de confirmación de eventos", onSelectScreenCode)

@Composable
internal fun RenderReportsIncomeS67(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Reporte de Ingresos", "RD$ 32,780 generados este período", onSelectScreenCode)

@Composable
internal fun RenderReportsEventsS68(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Reporte de Eventos", "156 eventos realizados históricamente", onSelectScreenCode)

@Composable
internal fun RenderReportsClientsS69(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Reporte de Clientes", "248 clientes atendidos · +18 este mes", onSelectScreenCode)

@Composable
internal fun RenderReportsInventoryS70(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Rotación de Mobiliario", "1,245 artículos en inventario activo", onSelectScreenCode)

@Composable
internal fun RenderReviewModerationS71(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Moderación de Opiniones", "10 reseñas aprobadas con 5 estrellas", onSelectScreenCode)

@Composable
internal fun RenderBotQuestionsS72(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Respuestas Automáticas WhatsApp", "Preguntas frecuentes de precio y stock", onSelectScreenCode)

@Composable
internal fun RenderBotKeywordsS73(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Palabras Clave WhatsApp", "Filtros de reconocimiento para cotización rápida", onSelectScreenCode)

@Composable
internal fun RenderBotConversationsS74(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Chats del Bot WhatsApp", "Conversaciones automáticas con clientes", onSelectScreenCode)

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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
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
