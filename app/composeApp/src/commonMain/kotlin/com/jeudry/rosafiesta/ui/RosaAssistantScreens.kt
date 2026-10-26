package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
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
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ==============================================================================
// UTILIDADES COMPARTIDAS DE ROSA IA
// ==============================================================================

private val RosaBackgroundGradient = Brush.verticalGradient(
    0.0f to Color(0xFFDED5F7),
    0.4f to Color(0xFFF2EAF7),
    0.6f to Color(0xFFF5EEF7),
    1.0f to Color(0xFFD9D0F2),
)

private val AdminRosaBackgroundGradient = Brush.verticalGradient(
    0.0f to Color(0xFFE3E1FB),
    0.4f to Color(0xFFF1EEFA),
    0.6f to Color(0xFFF4F1FA),
    1.0f to Color(0xFFDCDDF8),
)

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

/**
 * Cabecera estándar del wizard de Rosa IA:
 * [Botón atrás/cerrar] -- Paso X de 6 (con 6 segmentos) -- [Botón Mi Evento (badge)]
 */
@Composable
private fun RosaWizardHeader(
    step: Int,
    totalSteps: Int = 6,
    cartCount: Int = 16,
    onBack: () -> Unit = {},
    onCart: () -> Unit = {},
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(2f)
            .background(Color(0xFFDED5F7))
            .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .rfIconButtonShadow(CircleShape, isAdmin = false)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(
                kind = RfIconKind.BACK,
                tint = Color(0xFF1A1A2E),
                size = 22.dp,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BasicText(
                text = "Paso $step de $totalSteps",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1A1A2E),
                ),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(150.dp),
            ) {
                for (stepBarIndex in 1..totalSteps) {
                    val targetCode = when (stepBarIndex) {
                        1 -> if (step > 1) "BACK:s27" else "s27"
                        2 -> if (step > 2) "BACK:s28" else "s28"
                        3 -> if (step > 3) "BACK:s29" else "s29"
                        4 -> if (step > 4) "BACK:s30" else "s30"
                        5 -> if (step > 5) "BACK:s31" else "s31"
                        else -> "s32"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (stepBarIndex <= step) {
                                    Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB)))
                                } else {
                                    SolidColor(Color(0xFFDDD3F3))
                                },
                            )
                            .clickable { onSelectScreenCode(targetCode) },
                    )
                }
            }
        }

        // Botón carrito / mi evento circular blanco con badge (sin recorte de badge)
        Box(
            modifier = Modifier.size(44.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onCart),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(
                    kind = RfIconKind.PARTY,
                    tint = Color(0xFF1A1A2E),
                    size = 22.dp,
                )
            }
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "$cartCount",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFF2E1A47),
                        ),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s25 · MI EVENTO (BORRADOR / CARRITO)
// ==============================================================================
@Composable
internal fun RenderMyEventDraftS25(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: mapOf("art-silla-tiffany" to 80, "art-mesa-redonda" to 12)
    val allArticles: List<CatalogArticle> = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val articlesMap: Map<String, CatalogArticle> = remember(allArticles) { allArticles.associateBy { it.id } }

    val activeDraftItems: List<Pair<CatalogArticle, Int>> = remember(draftQuantities, articlesMap) {
        draftQuantities.filter { it.value > 0 }.mapNotNull { (id, qty) ->
            articlesMap[id]?.let { article -> Pair(article, qty) }
        }
    }

    val totalItemsCount: Int = activeDraftItems.sumOf { it.second }
    val totalPriceDop: Int = activeDraftItems.sumOf { (article, qty) -> article.pricePerDayDop * qty }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    0.0f to Color(0xFFF9E3F1),
                    0.6f to Color(0x00F9E3F1),
                    center = Offset(0f, 0f),
                    radius = 400f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    0.0f to Color(0xFFEEE7FB),
                    0.6f to Color(0x00EEE7FB),
                    center = Offset(Float.MAX_VALUE, 0f),
                    radius = 350f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s18") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Mi evento",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.44).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    BasicText(
                        text = "Borrador de cotización",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF1EFF5))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Borrador",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF62627A)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeDraftItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFFC2338F), size = 48.dp)
                        BasicText("Tu carrito de evento está vacío", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)))
                        BasicText("Explora el catálogo y agrega mobiliario o decoración", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                    }
                }
            } else {
                // Card contenedor de artículos del borrador
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp)),
                ) {
                    Column {
                        // Header de la lista
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp, 12.dp, 14.dp, 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFDE6F2)),
                            ) {
                                RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFFC2338F), size = 20.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    "Artículos seleccionados",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                                )
                                BasicText(
                                    "${activeDraftItems.size} artículos · $totalItemsCount unidades",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                                )
                            }
                            BasicText(
                                "RD$ $totalPriceDop",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = (-0.34).sp, color = Color(0xFF1A1A2E)),
                            )
                        }

                        activeDraftItems.forEachIndexed { index, (article, qty) ->
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                            val photoKey = when (article.id) {
                                "art-silla-tiffany" -> RfPhotoKey.SILLA_TIFFANY
                                "art-mesa-redonda" -> RfPhotoKey.MESA_REDONDA
                                "art-centro-floral" -> RfPhotoKey.CENTRO_FLORAL
                                "art-arco-rosa" -> RfPhotoKey.ARCO_ROSA
                                "art-mesa-helados" -> RfPhotoKey.MESA_HELADOS
                                "art-mesa-plegable" -> RfPhotoKey.MESA_PLEGABLE
                                else -> RfPhotoKey.SILLA_TIFFANY
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))) {
                                    RfPhoto(photoKey = photoKey, contentDescription = article.name, cornerRadius = 14.dp, modifier = Modifier.fillMaxSize())
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    BasicText(article.name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                                    BasicText("${article.category} · RD$ ${article.pricePerDayDop} c/u", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                                    BasicText("RD$ ${article.pricePerDayDop * qty}", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = (-0.32).sp, color = Color(0xFF1A1A2E)))
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(12.dp)).height(36.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier.size(32.dp, 36.dp).clickable { viewModel?.adjustDraftQuantity(article.id, -1) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF1A1A2E), size = 16.dp)
                                        }
                                        BasicText("$qty", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                                        Box(
                                            modifier = Modifier.size(32.dp, 36.dp).clickable { viewModel?.adjustDraftQuantity(article.id, 1) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF1A1A2E), size = 16.dp)
                                        }
                                    }
                                    BasicText(
                                        "Quitar",
                                        modifier = Modifier.clickable { viewModel?.adjustDraftQuantity(article.id, -qty) },
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF62627A)),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón dashed "Agregar más artículos"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0xFFE3DCE8), RoundedCornerShape(16.dp))
                    .clickable { onSelectScreenCode("s18") },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF1A1A2E), size = 18.dp)
                    BasicText(
                        "Agregar más artículos",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(180.dp))
        }

        // Panel inferior fijo con precio + CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    BasicText(
                        "$totalItemsCount artículos · ${activeDraftItems.size} seleccionados",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)),
                    )
                    BasicText(
                        "RD$ $totalPriceDop",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.48).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s48") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Ver cotización oficial",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// ==============================================================================
// s26 · ROSA · TIPO DE EVENTO (PASO 1 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaEventTypeS26(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: mapOf("art-silla-tiffany" to 16)
    val totalCartItems = draftQuantities.values.sum().coerceAtLeast(16)
    var promptText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color(0xFFB9D84A), radius = 4.5f * density, center = Offset(318f * density, 168f * density))
            drawCircle(color = Color(0x33CFE48A), radius = 24f * density, center = Offset(40f * density, 700f * density))
        }

        RosaWizardHeader(
            step = 1,
            totalSteps = 6,
            cartCount = totalCartItems,
            onBack = { onSelectScreenCode("s18") },
            onCart = { onSelectScreenCode("s25") },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 88.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    Row {
                        BasicText(
                            text = "¡Hola! Soy ",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5A5672)),
                        )
                        BasicText(
                            text = "Rosa",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF9B45C8)),
                        )
                        BasicText(
                            text = ", tu asistente.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5A5672)),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText(
                        text = "¿Qué evento quieres",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            lineHeight = 28.sp,
                            letterSpacing = (-0.52).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    BasicText(
                        text = "celebrar?",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            lineHeight = 28.sp,
                            letterSpacing = (-0.52).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }
            }

            data class EventTypeItem(
                val name: String,
                val bg: Color,
                val isGradient: Boolean = false,
                val textColor: Color = Color(0xFF1A1A2E),
            )

            val items = listOf(
                EventTypeItem("Boda", Color(0xFFFDE6F2)),
                EventTypeItem("Cumpleaños", Color(0xFFFFF2C9)),
                EventTypeItem("Baby shower", Color(0xFFDDF4F3)),
                EventTypeItem("Gender reveal", Color(0xFFEFE8FD)),
                EventTypeItem("Corporativo", Color(0xFFEEF6D6)),
                EventTypeItem("Otro", Color(0xFF865DEC), isGradient = true, textColor = Color.White),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(116.dp)
                                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (item.isGradient) {
                                            Modifier.background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                                        } else {
                                            Modifier.background(item.bg)
                                        },
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                                    .clickable {
                                        viewModel?.updateRosaParameters(eventType = item.name)
                                        viewModel?.setEventTypeInput(item.name)
                                        onSelectScreenCode("s27")
                                    }
                                    .padding(start = 14.dp, bottom = 12.dp, end = 14.dp),
                                contentAlignment = Alignment.BottomStart,
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-2).dp),
                                ) {
                                    val widthPx = size.width
                                    val heightPx = size.height
                                    when (item.name) {
                                        "Boda" -> {
                                            val gold = Color(0xFFD9A441)
                                            val lightGold = Color(0xFFF0CC7A)
                                            drawCircle(color = gold, radius = 17f * density, center = Offset(widthPx * 0.45f, heightPx * 0.65f), style = Stroke(width = 5f * density))
                                            drawCircle(color = lightGold, radius = 17f * density, center = Offset(widthPx * 0.65f, heightPx * 0.67f), style = Stroke(width = 5f * density))
                                            val diamond = Path().apply {
                                                moveTo(widthPx * 0.40f, heightPx * 0.42f)
                                                lineTo(widthPx * 0.45f, heightPx * 0.35f)
                                                lineTo(widthPx * 0.50f, heightPx * 0.42f)
                                                lineTo(widthPx * 0.45f, heightPx * 0.50f)
                                                close()
                                            }
                                            drawPath(diamond, color = Color(0xFFA8D8F5))
                                        }

                                        "Cumpleaños" -> {
                                            drawRoundRect(color = Color(0xFFFFF6EE), topLeft = Offset(widthPx * 0.28f, heightPx * 0.50f), size = Size(widthPx * 0.52f, heightPx * 0.34f), cornerRadius = CornerRadius(5f * density))
                                            drawRoundRect(color = Color(0xFFEC4F97), topLeft = Offset(widthPx * 0.28f, heightPx * 0.50f), size = Size(widthPx * 0.52f, heightPx * 0.12f), cornerRadius = CornerRadius(4f * density))
                                            drawLine(color = Color(0xFF7650E8), start = Offset(widthPx * 0.38f, heightPx * 0.50f), end = Offset(widthPx * 0.38f, heightPx * 0.36f), strokeWidth = 4f * density, cap = StrokeCap.Round)
                                            drawLine(color = Color(0xFF4AA8E0), start = Offset(widthPx * 0.52f, heightPx * 0.50f), end = Offset(widthPx * 0.52f, heightPx * 0.32f), strokeWidth = 4f * density, cap = StrokeCap.Round)
                                            drawLine(color = Color(0xFF2FB58F), start = Offset(widthPx * 0.66f, heightPx * 0.50f), end = Offset(widthPx * 0.66f, heightPx * 0.36f), strokeWidth = 4f * density, cap = StrokeCap.Round)
                                            drawCircle(color = Color(0xFFF5B53B), radius = 3.5f * density, center = Offset(widthPx * 0.38f, heightPx * 0.31f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = Offset(widthPx * 0.52f, heightPx * 0.27f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 3.5f * density, center = Offset(widthPx * 0.66f, heightPx * 0.31f))
                                        }

                                        "Baby shower" -> {
                                            val onesie = Path().apply {
                                                moveTo(widthPx * 0.40f, heightPx * 0.36f)
                                                lineTo(widthPx * 0.32f, heightPx * 0.44f)
                                                lineTo(widthPx * 0.38f, heightPx * 0.52f)
                                                lineTo(widthPx * 0.42f, heightPx * 0.48f)
                                                lineTo(widthPx * 0.42f, heightPx * 0.78f)
                                                lineTo(widthPx * 0.62f, heightPx * 0.78f)
                                                lineTo(widthPx * 0.62f, heightPx * 0.48f)
                                                lineTo(widthPx * 0.66f, heightPx * 0.52f)
                                                lineTo(widthPx * 0.72f, heightPx * 0.44f)
                                                lineTo(widthPx * 0.64f, heightPx * 0.36f)
                                                close()
                                            }
                                            drawPath(onesie, color = Color(0xFFA8D8F5))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 5f * density, center = Offset(widthPx * 0.52f, heightPx * 0.55f))
                                        }

                                        "Gender reveal" -> {
                                            drawOval(color = Color(0xFFF9B6D3), topLeft = Offset(widthPx * 0.26f, heightPx * 0.30f), size = Size(widthPx * 0.32f, heightPx * 0.38f))
                                            drawOval(color = Color(0xFFA8D8F5), topLeft = Offset(widthPx * 0.52f, heightPx * 0.34f), size = Size(widthPx * 0.32f, heightPx * 0.38f))
                                        }

                                        "Corporativo" -> {
                                            val glassColor = Color(0xFFD9A441)
                                            drawLine(color = glassColor, start = Offset(widthPx * 0.38f, heightPx * 0.40f), end = Offset(widthPx * 0.38f, heightPx * 0.76f), strokeWidth = 2.5f * density)
                                            drawLine(color = glassColor, start = Offset(widthPx * 0.30f, heightPx * 0.76f), end = Offset(widthPx * 0.46f, heightPx * 0.76f), strokeWidth = 2.5f * density)
                                            drawLine(color = glassColor, start = Offset(widthPx * 0.62f, heightPx * 0.40f), end = Offset(widthPx * 0.62f, heightPx * 0.76f), strokeWidth = 2.5f * density)
                                            drawLine(color = glassColor, start = Offset(widthPx * 0.54f, heightPx * 0.76f), end = Offset(widthPx * 0.70f, heightPx * 0.76f), strokeWidth = 2.5f * density)
                                            drawCircle(color = Color(0xFFF5B53B), radius = 3f * density, center = Offset(widthPx * 0.50f, heightPx * 0.30f))
                                        }

                                        "Otro" -> {
                                            val star = Path().apply {
                                                moveTo(widthPx * 0.50f, heightPx * 0.28f)
                                                lineTo(widthPx * 0.58f, heightPx * 0.48f)
                                                lineTo(widthPx * 0.78f, heightPx * 0.52f)
                                                lineTo(widthPx * 0.58f, heightPx * 0.56f)
                                                lineTo(widthPx * 0.50f, heightPx * 0.76f)
                                                lineTo(widthPx * 0.42f, heightPx * 0.56f)
                                                lineTo(widthPx * 0.22f, heightPx * 0.52f)
                                                lineTo(widthPx * 0.42f, heightPx * 0.48f)
                                                close()
                                            }
                                            drawPath(star, color = Color.White.copy(alpha = 0.85f))
                                        }
                                    }
                                }

                                BasicText(
                                    text = item.name,
                                    style = TextStyle(
                                        fontFamily = typo.displayFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = item.textColor,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom input bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .fillMaxWidth()
                .height(58.dp)
                .rfInputShadow(CircleShape, isAdmin = false)
                .clip(CircleShape)
                .background(Color.White)
                .padding(start = 20.dp, end = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            val hasPromptText = promptText.isNotBlank()
            val submitS26Prompt = {
                if (promptText.isNotBlank()) {
                    applyNaturalLanguageEventPrompt(promptText, viewModel)
                    promptText = ""
                    onSelectScreenCode("s27")
                } else {
                    onSelectScreenCode("s34")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (promptText.isEmpty()) {
                        BasicText(
                            text = "Escríbele a Rosa o habla…",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }
                    BasicTextField(
                        value = promptText,
                        onValueChange = { updatedText -> promptText = updatedText },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitS26Prompt() }),
                        textStyle = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                        .clickable { submitS26Prompt() },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(
                        kind = if (hasPromptText) RfIconKind.SEND else RfIconKind.MIC,
                        tint = Color.White,
                        size = 22.dp,
                    )
                }
            }
        }
    }
}

private data class ParsedRosaPrompt(
    val eventType: String?,
    val guestsCount: Int?,
    val dateLabel: String?,
    val venueLabel: String?,
)

private fun parseRosaNaturalLanguagePrompt(rawText: String): ParsedRosaPrompt {
    val lower = rawText.trim().lowercase()
    val detectedEventType = when {
        lower.contains("quince") -> "Quinceañera"
        lower.contains("cumple") -> "Cumpleaños"
        lower.contains("baby") -> "Baby shower"
        lower.contains("gradu") -> "Graduación"
        lower.contains("corporat") -> "Corporativo"
        lower.contains("bautiz") -> "Bautizo"
        lower.contains("boda") -> "Boda"
        else -> null
    }
    val numberMatch = Regex("\\b(\\d{2,3})\\b").find(lower)
    val rawNumber = numberMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
    val isGuestRange = if (rawNumber != null) rawNumber in 15..500 else false
    val detectedGuests = if (isGuestRange) rawNumber else null
    val detectedDate = when {
        lower.contains("14") -> "Sábado 14 de noviembre"
        lower.contains("21") -> "Sábado 21 de noviembre"
        lower.contains("30") -> "Viernes 30 de octubre"
        lower.contains("7 nov") -> "Sábado 7 de noviembre"
        lower.contains("diciembre") -> "Viernes 4 de diciembre"
        lower.contains("noviembre") -> "Sábado 14 de noviembre"
        else -> null
    }
    val detectedVenue = when {
        lower.contains("botánico") -> "Jardín Botánico"
        lower.contains("botanico") -> "Jardín Botánico"
        lower.contains("jardín") -> "Jardín Los Rosales"
        lower.contains("jardin") -> "Jardín Los Rosales"
        lower.contains("piantini") -> "Salón Piantini"
        lower.contains("salón") -> "Salón Piantini"
        lower.contains("salon") -> "Salón Piantini"
        lower.contains("terraza") -> "Terraza Mirador"
        lower.contains("playa") -> "Club de Playa Boca Chica"
        else -> null
    }
    return ParsedRosaPrompt(
        eventType = detectedEventType,
        guestsCount = detectedGuests,
        dateLabel = detectedDate,
        venueLabel = detectedVenue,
    )
}

private fun applyNaturalLanguageEventPrompt(rawText: String, viewModel: RosaFiestaViewModel?): ParsedRosaPrompt {
    val trimmed = rawText.trim()
    val parsed = parseRosaNaturalLanguagePrompt(trimmed)
    if (trimmed.isEmpty()) return parsed
    viewModel?.setRosaProposalMessage(trimmed)
    if (parsed.eventType != null) {
        viewModel?.setEventTypeInput(parsed.eventType)
    }
    if (parsed.guestsCount != null) {
        viewModel?.setEventGuestsInput(parsed.guestsCount.toString())
    }
    if (parsed.dateLabel != null) {
        viewModel?.setEventDateInput(parsed.dateLabel)
    }
    if (parsed.venueLabel != null) {
        viewModel?.setEventVenueInput(parsed.venueLabel)
    }
    return parsed
}

// ==============================================================================
// s27 · ROSA · TUTORIAL / ONBOARDING (PASO 1 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaTutorialS27(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val selectedEventType = viewModel?.eventTypeInput?.collectAsState()?.value ?: "Boda"
    var promptText by remember { mutableStateOf("") }

    val pillGradient = Brush.linearGradient(
        listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)),
    )
    val micGradient = Brush.linearGradient(
        listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            step = 1,
            onBack = { onSelectScreenCode("BACK:s26") },
            onCart = { onSelectScreenCode("s25") },
            onSelectScreenCode = onSelectScreenCode,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 88.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 6.dp,
                                ),
                            )
                            .background(pillGradient)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RfIcon(kind = RfIconKind.RING, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = selectedEventType,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                ),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    BasicText(
                        text = "¡Una ${selectedEventType.lowercase()}! Me encanta.",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            lineHeight = (26 * 1.05f).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        BasicText(
                            text = "ANTES DE EMPEZAR",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = (0.08 * 12).sp,
                                color = Color(0xFF7A4FD8),
                            ),
                        )
                        BasicText(
                            text = buildAnnotatedString {
                                append("Cuando quieras darme más detalles, ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("escríbeme") }
                                append(" o ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("mantén presionado el micrófono") }
                                append(" y háblame.")
                            },
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 17.sp,
                                lineHeight = (17 * 1.45f).sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .height(46.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = false)
                                .clip(RoundedCornerShape(14.dp))
                                .background(pillGradient)
                                .clickable { onSelectScreenCode("s28") }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Entendido",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(140.dp))
            }
        }

        Canvas(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 130.dp)
                .size(width = 140.dp, height = 220.dp),
        ) {
            val scaleX = size.width / 140f
            val scaleY = size.height / 220f
            val dashEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                floatArrayOf(6f * scaleX, 7f * scaleX),
            )
            val strokeColor = Color(0xFF865DEC)
            val curvePath = Path().apply {
                moveTo(20f * scaleX, 10f * scaleY)
                cubicTo(
                    70f * scaleX,
                    30f * scaleY,
                    110f * scaleX,
                    80f * scaleY,
                    106f * scaleX,
                    150f * scaleY,
                )
            }
            drawPath(
                path = curvePath,
                color = strokeColor,
                style = Stroke(
                    width = 2.2f * scaleX,
                    cap = StrokeCap.Round,
                    pathEffect = dashEffect,
                ),
            )
            val arrowPath = Path().apply {
                moveTo(94f * scaleX, 140f * scaleY)
                lineTo(106f * scaleX, 158f * scaleY)
                lineTo(118f * scaleX, 141f * scaleY)
            }
            drawPath(
                path = arrowPath,
                color = strokeColor,
                style = Stroke(
                    width = 2.2f * scaleX,
                    cap = StrokeCap.Round,
                ),
            )
        }

        val hasPromptText = promptText.isNotBlank()
        val submitS27Prompt = {
            if (promptText.isNotBlank()) {
                applyNaturalLanguageEventPrompt(promptText, viewModel)
                promptText = ""
                onSelectScreenCode("s28")
            } else {
                onSelectScreenCode("s35")
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .fillMaxWidth()
                .height(58.dp)
                .rfInputShadow(RoundedCornerShape(999.dp), isAdmin = false)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE7DEF5), RoundedCornerShape(999.dp))
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (promptText.isEmpty()) {
                    BasicText(
                        text = "Escríbele a Rosa o habla…",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF9E9AB5),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = promptText,
                    onValueChange = { updatedText -> promptText = updatedText },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitS27Prompt() }),
                    textStyle = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp + 24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA4EA9).copy(alpha = 0.08f)),
                )
                Box(
                    modifier = Modifier
                        .size(46.dp + 12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA4EA9).copy(alpha = 0.16f)),
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(micGradient)
                        .clickable { submitS27Prompt() },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(
                        kind = if (hasPromptText) RfIconKind.SEND else RfIconKind.MIC,
                        tint = Color.White,
                        size = 22.dp,
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s28 · ROSA · ¿TE AYUDO? (PASO 2 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaHelpS28(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val selectedEventType = viewModel?.eventTypeInput?.collectAsState()?.value ?: "Boda"
    var promptText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFDED5F7),
                        Color(0xFFF2EAF7),
                        Color(0xFFF5EEF7),
                        Color(0xFFD9D0F2),
                    ),
                ),
            ),
    ) {
        RosaWizardHeader(
            step = 2,
            cartCount = 16,
            onBack = { onSelectScreenCode("BACK:s27") },
            onCart = { onSelectScreenCode("s25") },
            onSelectScreenCode = onSelectScreenCode,
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 224.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.RING, tint = Color.White, size = 18.dp)
                    BasicText(
                        text = selectedEventType,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        ),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp)

                BasicText(
                    text = "¿Te ayudo a elegir los artículos de alquiler?",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF1A1A2E),
                        lineHeight = 28.sp,
                    ),
                )
                BasicText(
                    text = "Te recomiendo según tu ${selectedEventType.lowercase()}. Puedes cambiar todo después.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        color = Color(0xFF62627A),
                        lineHeight = 24.sp,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                    .clickable { onSelectScreenCode("s29") }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RfIcon(kind = RfIconKind.SPARKLE, tint = Color.White, size = 22.dp)
                BasicText(
                    text = "Sí, busquemos juntas",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                    modifier = Modifier.weight(1f),
                )
                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s18") }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF1A1A2E), size = 22.dp)
                BasicText(
                    text = "No, prefiero ver el catálogo",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val hasPromptText = promptText.isNotBlank()
        val submitS28Prompt = {
            if (promptText.isNotBlank()) {
                applyNaturalLanguageEventPrompt(promptText, viewModel)
                promptText = ""
                onSelectScreenCode("s29")
            } else {
                onSelectScreenCode("s35")
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .height(58.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (promptText.isEmpty()) {
                    BasicText(
                        text = "Escríbele a Rosa o habla…",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }
                BasicTextField(
                    value = promptText,
                    onValueChange = { updatedText -> promptText = updatedText },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitS28Prompt() }),
                    textStyle = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .clickable { submitS28Prompt() },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(
                    kind = if (hasPromptText) RfIconKind.SEND else RfIconKind.MIC,
                    tint = Color.White,
                    size = 22.dp,
                )
            }
        }
    }
}

// ==============================================================================
// s29 · ROSA · SUGERENCIAS DE PAQUETES (PASO 3 DE 6)
// ==============================================================================
private data class RosaSuggestedArticleUi(
    val articleId: String,
    val title: String,
    val subtitle: String,
    val priceDop: Int,
    val photoKey: RfPhotoKey,
    val photoBg: Color,
)

@Composable
internal fun RenderRosaSuggestionsS29(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val selectedEventType = viewModel?.eventTypeInput?.collectAsState()?.value ?: "Boda"
    var suggestionBatch by remember { mutableStateOf(0) }
    var addedArticleIds by remember { mutableStateOf(setOf("art-centro-floral")) }
    var promptText by remember { mutableStateOf("") }

    val primarySuggestions = remember {
        listOf(
            RosaSuggestedArticleUi("art-centro-floral", "Centro de mesa floral", "Flores naturales, base de madera", 35, RfPhotoKey.CENTRO_FLORAL, Color(0xFFFFF2C9)),
            RosaSuggestedArticleUi("art-mesa-helados", "Mesa dulce temática", "Decoración completa para dulces", 150, RfPhotoKey.MESA_HELADOS, Color(0xFFEEF6D6)),
            RosaSuggestedArticleUi("art-arco-rosa", "Arco de globos", "Rosa y nude, para la entrada", 95, RfPhotoKey.ARCO_ROSA, Color(0xFFFDE6F2)),
        )
    }
    val alternateSuggestions = remember {
        listOf(
            RosaSuggestedArticleUi("art-silla-tiffany", "Silla Tiffany Cristal", "Acabado transparente de lujo", 8, RfPhotoKey.SILLA_TIFFANY, Color(0xFFEFE8FD)),
            RosaSuggestedArticleUi("art-mesa-redonda", "Mesa redonda 10p", "Madera sólida para banquete", 25, RfPhotoKey.MESA_REDONDA, Color(0xFFDDF4F3)),
            RosaSuggestedArticleUi("art-mesa-plegable", "Mesa de apoyo", "Ideal para estación de bebidas", 15, RfPhotoKey.MESA_PLEGABLE, Color(0xFFFFF2C9)),
        )
    }
    val displayedSuggestions = if (suggestionBatch % 2 == 0) primarySuggestions else alternateSuggestions
    val totalCartBadge = 16 + addedArticleIds.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            step = 3,
            cartCount = totalCartBadge,
            onBack = { onSelectScreenCode("BACK:s28") },
            onCart = { onSelectScreenCode("s25") },
            onSelectScreenCode = onSelectScreenCode,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 78.dp, bottom = 160.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    RosaAvatar(RosaAvatarSize.MEDIUM)
                }

                BasicText(
                    text = "Para tu ${selectedEventType.lowercase()} te recomiendo estos artículos",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.52).sp,
                        lineHeight = 30.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                displayedSuggestions.forEach { suggestion ->
                    val isAdded = suggestion.articleId in addedArticleIds
                    Box(
                        modifier = Modifier
                            .width(208.dp)
                            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White),
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(suggestion.photoBg),
                            ) {
                                RfPhoto(
                                    photoKey = suggestion.photoKey,
                                    contentDescription = suggestion.title,
                                    cornerRadius = 0.dp,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                BasicText(
                                    text = suggestion.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                                )
                                BasicText(
                                    text = suggestion.subtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BasicText(
                                        text = "RD$ ${suggestion.priceDop}",
                                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color(0xFF1A1A2E)),
                                    )
                                    if (isAdded) {
                                        Row(
                                            modifier = Modifier
                                                .height(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEEF6D6))
                                                .clickable {
                                                    addedArticleIds = addedArticleIds - suggestion.articleId
                                                    viewModel?.adjustDraftQuantity(suggestion.articleId, -1)
                                                }
                                                .padding(horizontal = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 16.dp)
                                            BasicText("Agregado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4E6A10)))
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .height(36.dp)
                                                .shadow(8.dp, CircleShape, spotColor = Color(0xB3865DEC))
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                                .clickable {
                                                    addedArticleIds = addedArticleIds + suggestion.articleId
                                                    viewModel?.adjustDraftQuantity(suggestion.articleId, 1)
                                                }
                                                .padding(horizontal = 14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 16.dp)
                                            BasicText("Agregar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { suggestionBatch += 1 },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Otras sugerencias",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s30") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Continuar",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
            }
        }

        val hasPromptText = promptText.isNotBlank()
        val submitS29Prompt = {
            if (promptText.isNotBlank()) {
                applyNaturalLanguageEventPrompt(promptText, viewModel)
                suggestionBatch += 1
                promptText = ""
            } else {
                onSelectScreenCode("s35")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .height(58.dp)
                .shadow(12.dp, CircleShape, spotColor = Color(0x26AA46A0))
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(start = 20.dp, end = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (promptText.isEmpty()) {
                        BasicText(
                            text = "Pide algo distinto…",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                        )
                    }
                    BasicTextField(
                        value = promptText,
                        onValueChange = { updatedText -> promptText = updatedText },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitS29Prompt() }),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0xB3865DEC))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                        .clickable { submitS29Prompt() },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(
                        kind = if (hasPromptText) RfIconKind.SEND else RfIconKind.MIC,
                        tint = Color.White,
                        size = 22.dp,
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s30 · ROSA · CATEGORÍAS (PASO 4 DE 6)
// ==============================================================================
private data class RosaCategoryReviewSpec(
    val title: String,
    val defaultSubtitle: String?,
    val iconKind: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
    val articleId: String,
)

@Composable
internal fun RenderRosaCategoriesS30(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var chosenCategories by remember { mutableStateOf(setOf("Sillas", "Mesas")) }
    var omittedCategories by remember { mutableStateOf(emptySet<String>()) }
    var showExtraIdeas by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }

    val baseCategories = remember {
        listOf(
            RosaCategoryReviewSpec("Sillas", "2 elegidas", RfIconKind.STORE, Color(0xFFFFF2C9), Color(0xFF7A5700), "art-silla-tiffany"),
            RosaCategoryReviewSpec("Mesas", "1 elegida", RfIconKind.BOX, Color(0xFFEEF6D6), Color(0xFF4E6A10), "art-mesa-redonda"),
            RosaCategoryReviewSpec("Iluminación", null, RfIconKind.LIGHTBULB, Color(0xFFDDF4F3), Color(0xFF1F8A84), "art-arco-rosa"),
            RosaCategoryReviewSpec("Mantelería", null, RfIconKind.GRID, Color(0xFFEFE8FD), Color(0xFF7A4FD8), "art-mesa-plegable"),
            RosaCategoryReviewSpec("Centros de mesa", null, RfIconKind.SPARK, Color(0xFFFDE6F2), Color(0xFFC2338F), "art-centro-floral"),
        )
    }
    val extraCategories = remember {
        listOf(
            RosaCategoryReviewSpec("Estación de dulces", null, RfIconKind.CAKE, Color(0xFFFFF2C9), Color(0xFF7A5700), "art-mesa-helados"),
        )
    }
    val displayedCategories = if (showExtraIdeas) baseCategories + extraCategories else baseCategories
    val badgeCount = 15 + chosenCategories.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFDED5F7),
                        Color(0xFFF2EAF7),
                        Color(0xFFF5EEF7),
                        Color(0xFFD9D0F2),
                    ),
                ),
            ),
    ) {
        RosaWizardHeader(
            step = 4,
            cartCount = badgeCount,
            onBack = { onSelectScreenCode("BACK:s29") },
            onCart = { onSelectScreenCode("s25") },
            onSelectScreenCode = onSelectScreenCode,
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 148.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp)
                BasicText(
                    text = "¿Te falta algo? Revisemos por categoría",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF1A1A2E),
                        lineHeight = 28.sp,
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(vertical = 6.dp),
            ) {
                displayedCategories.forEachIndexed { index, category ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    }
                    val isChosen = category.title in chosenCategories
                    val isOmitted = category.title in omittedCategories
                    val resolvedSubtitle = when {
                        isChosen -> category.defaultSubtitle ?: "1 elegida"
                        isOmitted -> "Omitida"
                        else -> null
                    }
                    CategoryReviewRow(
                        title = category.title,
                        subtitle = resolvedSubtitle,
                        iconKind = category.iconKind,
                        iconBg = category.iconBg,
                        iconTint = category.iconTint,
                        isChosen = isChosen,
                        isOmitted = isOmitted,
                        onOmit = {
                            omittedCategories = omittedCategories + category.title
                            chosenCategories = chosenCategories - category.title
                        },
                        onChoose = {
                            chosenCategories = chosenCategories + category.title
                            omittedCategories = omittedCategories - category.title
                            viewModel?.adjustDraftQuantity(category.articleId, 1)
                        },
                        onToggleChosen = {
                            chosenCategories = chosenCategories - category.title
                        },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable {
                        showExtraIdeas = !showExtraIdeas
                        omittedCategories = emptySet()
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = if (showExtraIdeas) "Ver menos" else "Más ideas",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                    .clickable { onSelectScreenCode("s31") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Siguiente",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
            }
        }

        val hasPromptText = promptText.isNotBlank()
        val submitS30Prompt = {
            if (promptText.isNotBlank()) {
                val lowerPrompt = promptText.lowercase()
                applyNaturalLanguageEventPrompt(promptText, viewModel)
                val mentionsLighting = lowerPrompt.contains("luz") || lowerPrompt.contains("ilumin")
                if (mentionsLighting) {
                    chosenCategories = chosenCategories + "Iluminación"
                    viewModel?.adjustDraftQuantity("art-arco-rosa", 1)
                }
                if (lowerPrompt.contains("mantel")) {
                    chosenCategories = chosenCategories + "Mantelería"
                    viewModel?.adjustDraftQuantity("art-mesa-plegable", 1)
                }
                val mentionsFlorals = lowerPrompt.contains("flor") || lowerPrompt.contains("centro")
                if (mentionsFlorals) {
                    chosenCategories = chosenCategories + "Centros de mesa"
                    viewModel?.adjustDraftQuantity("art-centro-floral", 1)
                }
                showExtraIdeas = true
                promptText = ""
            } else {
                onSelectScreenCode("s35")
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .height(58.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (promptText.isEmpty()) {
                    BasicText(
                        text = "Escríbele a Rosa o habla…",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }
                BasicTextField(
                    value = promptText,
                    onValueChange = { updatedText -> promptText = updatedText },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitS30Prompt() }),
                    textStyle = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .clickable { submitS30Prompt() },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(
                    kind = if (hasPromptText) RfIconKind.SEND else RfIconKind.MIC,
                    tint = Color.White,
                    size = 22.dp,
                )
            }
        }
    }
}

@Composable
private fun CategoryReviewRow(
    title: String,
    subtitle: String?,
    iconKind: RfIconKind,
    iconBg: Color,
    iconTint: Color,
    isChosen: Boolean,
    isOmitted: Boolean = false,
    onOmit: () -> Unit = {},
    onChoose: () -> Unit = {},
    onToggleChosen: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = iconKind, tint = iconTint, size = 20.dp)
        }

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                ),
            )
            if (subtitle != null) {
                BasicText(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOmitted) Color(0xFF62627A) else Color(0xFF4E6A10),
                    ),
                )
            }
        }

        if (isChosen) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleChosen),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 24.dp)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOmit)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (isOmitted) "Omitida" else "Omitir",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF62627A),
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                        .clickable(onClick = onChoose)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Elegir",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s31 · ROSA · DETALLES DEL EVENTO (PASO 5 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaDetailsS31(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var dateValue by remember { mutableStateOf("") }
    var guestsValue by remember { mutableStateOf("") }
    var venueValue by remember { mutableStateOf("") }
    var ideaValue by remember { mutableStateOf("") }
    var promptText by remember { mutableStateOf("") }

    val presetDates = remember { listOf("Sábado 14 de noviembre", "Sábado 21 de noviembre", "Viernes 4 de diciembre") }
    var datePresetIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            step = 5,
            cartCount = 17,
            onBack = { onSelectScreenCode("BACK:s30") },
            onCart = { onSelectScreenCode("s25") },
            onSelectScreenCode = onSelectScreenCode,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 148.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp)
                BasicText(
                    text = "Ahora, los detalles de tu evento",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        letterSpacing = (-0.52).sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
            ) {
                // Row 1: Fecha del evento
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clickable {
                            val selectedDate = presetDates[datePresetIndex % presetDates.size]
                            datePresetIndex += 1
                            dateValue = selectedDate
                            viewModel?.setEventDateInput(selectedDate)
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF62627A), size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (dateValue.isEmpty()) {
                            BasicText(
                                text = "Fecha del evento",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            )
                        }
                        BasicTextField(
                            value = dateValue,
                            onValueChange = { updatedDate ->
                                dateValue = updatedDate
                                viewModel?.setEventDateInput(updatedDate)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 18.dp)
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                // Row 2: Personas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.USER, tint = Color(0xFF62627A), size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (guestsValue.isEmpty()) {
                            BasicText(
                                text = "¿Cuántas personas?",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            )
                        }
                        BasicTextField(
                            value = guestsValue,
                            onValueChange = { updatedGuests ->
                                guestsValue = updatedGuests
                                viewModel?.setEventGuestsInput(updatedGuests)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                // Row 3: Lugar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF62627A), size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (venueValue.isEmpty()) {
                            BasicText(
                                text = "¿Dónde será?",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            )
                        }
                        BasicTextField(
                            value = venueValue,
                            onValueChange = { updatedVenue ->
                                venueValue = updatedVenue
                                viewModel?.setEventVenueInput(updatedVenue)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                // Row 4: Idea (textarea)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 58.dp)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF62627A), size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopStart) {
                        if (ideaValue.isEmpty()) {
                            BasicText(
                                text = "Cuéntame tu idea (opcional)",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            )
                        }
                        BasicTextField(
                            value = ideaValue,
                            onValueChange = { updatedIdea ->
                                ideaValue = updatedIdea
                                viewModel?.setRosaProposalMessage(updatedIdea)
                            },
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Boceto de distribución card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEFE8FD))
                    .clickable { onSelectScreenCode("s36") }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Canvas(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                ) {
                    val gridColor = Color(0xFFEFE8FA)
                    val step = size.width / 4f
                    for (gridLineIndex in 1..3) {
                        drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(gridLineIndex * step, 0f), end = androidx.compose.ui.geometry.Offset(gridLineIndex * step, size.height), strokeWidth = 1.dp.toPx())
                        drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, gridLineIndex * step), end = androidx.compose.ui.geometry.Offset(size.width, gridLineIndex * step), strokeWidth = 1.dp.toPx())
                    }
                    val strokePx = 2.dp.toPx()
                    drawCircle(color = Color(0xFFEA4EA9), radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.32f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokePx))
                    drawCircle(color = Color(0xFFEA4EA9), radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.68f, size.height * 0.32f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokePx))
                    drawRoundRect(color = Color(0xFF865DEC), topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.21f, size.height * 0.61f), size = androidx.compose.ui.geometry.Size(size.width * 0.57f, size.height * 0.18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(strokePx))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText("Boceto de distribución", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                    BasicText("Dibuja tu espacio y la IA te propone ejemplos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF62627A)))
                }
                RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF7A4FD8), size = 20.dp)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s29") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Más ideas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s32") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Ver resumen", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
            }
        }

        val hasPromptText = promptText.isNotBlank()
        val submitS31Prompt = {
            if (promptText.isNotBlank()) {
                val parsed = applyNaturalLanguageEventPrompt(promptText, viewModel)
                if (parsed.dateLabel != null) {
                    dateValue = parsed.dateLabel
                }
                if (parsed.guestsCount != null) {
                    guestsValue = "${parsed.guestsCount} personas"
                }
                if (parsed.venueLabel != null) {
                    venueValue = parsed.venueLabel
                }
                ideaValue = promptText
                promptText = ""
            } else {
                onSelectScreenCode("s35")
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .height(58.dp)
                .rfInputShadow(CircleShape, isAdmin = false)
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (promptText.isEmpty()) {
                    BasicText(
                        text = "O díctale los detalles a Rosa…",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                    )
                }
                BasicTextField(
                    value = promptText,
                    onValueChange = { updatedText -> promptText = updatedText },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitS31Prompt() }),
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .clickable { submitS31Prompt() },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(
                    kind = if (hasPromptText) RfIconKind.SEND else RfIconKind.MIC,
                    tint = Color.White,
                    size = 22.dp,
                )
            }
        }
    }
}

// ==============================================================================
// s32 · ROSA · RESUMEN (PASO 6 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaSummaryS32(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val eventType = viewModel?.eventTypeInput?.collectAsState()?.value?.ifBlank { "Boda" } ?: "Boda"
    val eventDate = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty()
    val eventVenue = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty()
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: mapOf("art-silla-tiffany" to 148, "art-mesa-redonda" to 12)
    val totalArticlesCount = draftQuantities.values.sum().let { sum -> if (sum > 0) sum else 160 }
    val isDefaultDate = eventDate == "14 Nov 2026"
    val hasConfirmedDate = if (eventDate.isBlank()) false else !isDefaultDate
    val isDefaultVenue = eventVenue == "Jardín Los Rosales"
    val hasConfirmedVenue = if (eventVenue.isBlank()) false else !isDefaultVenue

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            step = 6,
            totalSteps = 6,
            cartCount = 17,
            onBack = { onSelectScreenCode("BACK:s31") },
            onCart = { onSelectScreenCode("s25") },
            onSelectScreenCode = onSelectScreenCode,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 148.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                BasicText(
                    text = "Listo. Revisa que todo esté bien",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(vertical = 4.dp),
            ) {
                Column {
                    // Row 1: Evento
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s26") }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.RING, tint = Color(0xFFC2338F), size = 18.dp)
                        }
                        BasicText(
                            text = "Evento",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        BasicText(
                            text = eventType,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    // Row 2: Fecha
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s31") }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF1F8A84), size = 18.dp)
                        }
                        BasicText(
                            text = "Fecha",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        if (hasConfirmedDate) {
                            BasicText(
                                text = eventDate,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF2C9))
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Por confirmar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7A5700)),
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    // Row 3: Ubicación
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s31") }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF4E6A10), size = 18.dp)
                        }
                        BasicText(
                            text = "Ubicación",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        if (hasConfirmedVenue) {
                            BasicText(
                                text = eventVenue,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF2C9))
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Por confirmar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7A5700)),
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    // Row 4: Artículos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s25") }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF7A5700), size = 18.dp)
                        }
                        BasicText(
                            text = "Artículos",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        BasicText(
                            text = "$totalArticlesCount",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        )
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 18.dp)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.padding(top = 2.dp)) {
                    RfIcon(kind = RfIconKind.INFO, tint = Color(0xFF7A4FD8), size = 18.dp)
                }
                BasicText(
                    text = "El equipo de Rosa Fiesta revisa tu evento y te contacta con el precio final. No pagas nada todavía.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF3B3550),
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable {
                        viewModel?.submitQuickClientEvent()
                        onSelectScreenCode("s33")
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Solicitar cotización",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onSelectScreenCode("s30") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Agregar algo más",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }
        }
    }
}

// ==============================================================================
// s33 · ROSA · SOLICITUD ENVIADA
// ==============================================================================
@Composable
internal fun RenderRosaSubmittedS33(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
                .background(Color(0xFFDED5F7))
                .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s14") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF1A1A2E), size = 22.dp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 68.dp, bottom = 92.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            // Ilustración + título + subtítulo (posición top=96dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Ilustración de éxito con confeti SVG 170×170
                Canvas(modifier = Modifier.size(170.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    // Scale factor: viewBox 0 0 120 120, origin at (60,60)
                    val sc = size.width / 120f

                    // Outer circle DDF5EC r=36
                    drawCircle(color = Color(0xFFDDF5EC), radius = 36f * sc, center = Offset(cx, cy))
                    // Inner circle 2FB58F r=26
                    drawCircle(color = Color(0xFF2FB58F), radius = 26f * sc, center = Offset(cx, cy))
                    // Check mark: M48 61 l8 8 16-17
                    val check = Path().apply {
                        moveTo((48f - 60f) * sc + cx, (61f - 60f) * sc + cy)
                        lineTo((56f - 60f) * sc + cx, (69f - 60f) * sc + cy)
                        lineTo((72f - 60f) * sc + cx, (52f - 60f) * sc + cy)
                    }
                    drawPath(check, Color.White, style = Stroke(6f * sc, cap = StrokeCap.Round, join = StrokeJoin.Round))

                    // Confeti: pink rect rotated 20° (center 59.5, 12)
                    withTransform({
                        translate((59.5f - 60f) * sc + cx, (12f - 60f) * sc + cy)
                        rotate(20f)
                    }) {
                        drawRoundRect(Color(0xFFEC4F97), topLeft = Offset(-3.5f * sc, -6f * sc), size = androidx.compose.ui.geometry.Size(7f * sc, 12f * sc), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sc))
                    }
                    // Yellow circle (98, 28) r=4
                    drawCircle(Color(0xFFF5B53B), radius = 4f * sc, center = Offset((98f - 60f) * sc + cx, (28f - 60f) * sc + cy))
                    // Purple triangle (104,66)(114,71)(104,76)
                    val tri1 = Path().apply {
                        moveTo((104f - 60f) * sc + cx, (66f - 60f) * sc + cy)
                        lineTo((114f - 60f) * sc + cx, (71f - 60f) * sc + cy)
                        lineTo((104f - 60f) * sc + cx, (76f - 60f) * sc + cy)
                        close()
                    }
                    drawPath(tri1, Color(0xFF7650E8))
                    // Blue rect rotated -30° (center 89.5, 102)
                    withTransform({
                        translate((89.5f - 60f) * sc + cx, (102f - 60f) * sc + cy)
                        rotate(-30f)
                    }) {
                        drawRoundRect(Color(0xFF4AA8E0), topLeft = Offset(-3.5f * sc, -6f * sc), size = androidx.compose.ui.geometry.Size(7f * sc, 12f * sc), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sc))
                    }
                    // Pink dot (38, 106) r=4
                    drawCircle(Color(0xFFEC4F97), radius = 4f * sc, center = Offset((38f - 60f) * sc + cx, (106f - 60f) * sc + cy))
                    // Yellow triangle (16,72)(6,68)(14,61)
                    val tri2 = Path().apply {
                        moveTo((16f - 60f) * sc + cx, (72f - 60f) * sc + cy)
                        lineTo((6f - 60f) * sc + cx, (68f - 60f) * sc + cy)
                        lineTo((14f - 60f) * sc + cx, (61f - 60f) * sc + cy)
                        close()
                    }
                    drawPath(tri2, Color(0xFFF5B53B))
                    // Purple rect rotated -35° (center 21.5, 28)
                    withTransform({
                        translate((21.5f - 60f) * sc + cx, (28f - 60f) * sc + cy)
                        rotate(-35f)
                    }) {
                        drawRoundRect(Color(0xFF7650E8), topLeft = Offset(-3.5f * sc, -6f * sc), size = androidx.compose.ui.geometry.Size(7f * sc, 12f * sc), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sc))
                    }
                    // Small dots: green (80,14), blue (10,44), pink (110,46)
                    drawCircle(Color(0xFF2FB58F), radius = 2.5f * sc, center = Offset((80f - 60f) * sc + cx, (14f - 60f) * sc + cy))
                    drawCircle(Color(0xFF4AA8E0), radius = 2.5f * sc, center = Offset((10f - 60f) * sc + cx, (44f - 60f) * sc + cy))
                    drawCircle(Color(0xFFEC4F97), radius = 2.5f * sc, center = Offset((110f - 60f) * sc + cx, (46f - 60f) * sc + cy))
                }

                Spacer(modifier = Modifier.height(10.dp))

                BasicText(
                    text = "¡Solicitud enviada!",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = (28 * 1.05f).sp,
                        color = Color(0xFF1A1A2E),
                        textAlign = TextAlign.Center,
                    ),
                )
                Spacer(modifier = Modifier.height(6.dp))
                BasicText(
                    text = "Gracias por confiar en nosotros. Te avisamos por correo y WhatsApp.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF62627A),
                        textAlign = TextAlign.Center,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(54.dp))

            // Tarjeta "Qué sigue"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText(
                        text = "Qué sigue",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFF62627A),
                        ),
                    )

                    val steps = listOf(
                        Triple("1", "Revisamos tu evento", "El equipo de Rosa Fiesta ya lo tiene"),
                        Triple("2", "Recibes tu cotización", "Por correo y WhatsApp"),
                        Triple("3", "Apruebas y reservas", "Desde la app, en un toque"),
                    )

                    steps.forEach { (num, title, desc) ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            // Step 1 has glow ring via border
                            if (num == "1") {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .border(5.dp, Color(0xFFEFE8FD), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF865DEC)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = num,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White),
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1EFF5)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = num,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF62627A)),
                                    )
                                }
                            }
                            Column {
                                BasicText(text = title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                                BasicText(text = desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 13.sp))
                            }
                        }
                    }
                }
            }
        }

        // CTAs — posición absoluta bottom=40dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s25") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Ver mi evento",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onSelectScreenCode("s14") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Volver al inicio",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                )
            }
        }
    }
}

// ==============================================================================
// s34 · ROSA · MODO CHAT
// ==============================================================================
@Composable
internal fun RenderRosaChatS34(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var chatMessageText by remember {
        mutableStateOf("Queremos algo íntimo en un jardín, con flores blancas y rosadas")
    }
    var attachedReference by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RenderRosaEventTypeS26(onSelectScreenCode = onSelectScreenCode, viewModel = viewModel)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(312.dp)
                .shadow(24.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), spotColor = Color(0x73AA46A0))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 30.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Es para 120 personas", "En un jardín", "Rosa y dorado").forEach { chipText ->
                        val isChipIncluded = chatMessageText.contains(chipText, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(CircleShape)
                                .background(if (isChipIncluded) Color(0xFFFDE6F2) else Color(0xFFF3F0FA))
                                .clickable {
                                    chatMessageText = if (chatMessageText.isBlank()) {
                                        chipText
                                    } else {
                                        "$chatMessageText · $chipText"
                                    }
                                    if (chipText.contains("120")) {
                                        viewModel?.setEventGuestsInput("120")
                                    }
                                    if (chipText.contains("jardín", ignoreCase = true)) {
                                        viewModel?.setEventVenueInput("Jardín Los Rosales")
                                    }
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = chipText,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isChipIncluded) Color(0xFFC2338F) else Color(0xFF3B3550),
                                ),
                            )
                        }
                    }
                }

                if (attachedReference != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFE8FD))
                            .clickable { attachedReference = null }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFF7A4FD8), size = 14.dp)
                        BasicText(
                            text = "$attachedReference (toca para quitar)",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF7A4FD8)),
                        )
                    }
                }

                val submitChatPrompt = {
                    val trimmedMessage = chatMessageText.trim()
                    if (trimmedMessage.isNotEmpty()) {
                        applyNaturalLanguageEventPrompt(trimmedMessage, viewModel)
                    }
                    viewModel?.setRosaProposalMessage(chatMessageText)
                    onSelectScreenCode("s29")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    if (chatMessageText.isEmpty()) {
                        BasicText(
                            text = "Cuéntale a Rosa cómo imaginas tu evento…",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 18.sp,
                                lineHeight = 26.sp,
                                color = Color(0xFF9E9AB5),
                            ),
                        )
                    }
                    BasicTextField(
                        value = chatMessageText,
                        onValueChange = { updatedText -> chatMessageText = updatedText },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitChatPrompt() }),
                        textStyle = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 18.sp,
                            lineHeight = 26.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (attachedReference != null) Color(0xFFFDE6F2) else Color.Transparent)
                            .clickable {
                                attachedReference = if (attachedReference == null) "Inspiración jardín.jpg" else null
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = RfIconKind.SHARE,
                            tint = if (attachedReference != null) Color(0xFFC2338F) else Color(0xFF62627A),
                            size = 22.dp,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectScreenCode("s35") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.MIC, tint = Color(0xFF62627A), size = 22.dp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable {
                                chatMessageText = ""
                                onSelectScreenCode("s26")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Descartar",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(12.dp, CircleShape, spotColor = Color(0xB3C846B4))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
                                ),
                            )
                            .clickable { submitChatPrompt() },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 22.dp)
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s35 · ROSA · MODO VOZ
// ==============================================================================
@Composable
internal fun RenderRosaVoiceS35(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val voiceTranscripts = remember {
        listOf(
            "Una boda en un jardín para unas 120 personas en blanco y dorado",
            "Cumpleaños infantil para 60 invitados con arco de globos y mesa de dulces",
            "Cena corporativa para 150 personas con sillas Tiffany y centros florales",
        )
    }
    var transcriptIndex by remember { mutableStateOf(0) }
    var isListening by remember { mutableStateOf(true) }
    val activeTranscript = voiceTranscripts[transcriptIndex % voiceTranscripts.size]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(2f)
                .background(Color(0xFFDED5F7))
                .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s34") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF1A1A2E), size = 22.dp)
            }
            BasicText(
                text = "Paso 1 de 6 · Tipo de evento",
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF5A5672), fontSize = 13.sp),
            )
            Spacer(modifier = Modifier.size(44.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 104.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    RosaAvatar(
                        size = RosaAvatarSize.LARGE,
                        customSize = 124.dp,
                        forceAdminPalette = false,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFFEA4EA9) else Color(0xFF2FB58F)),
                    )
                    BasicText(
                        text = if (isListening) "Te escucho…" else "Transcripción lista",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                    )
                }
                BasicText(
                    text = activeTranscript,
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(76.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val bars = listOf(
                    Pair(10.dp, Color(0xFF865DEC)),
                    Pair(22.dp, Color(0xFFB9D84A)),
                    Pair(36.dp, Color(0xFFB54BD6)),
                    Pair(52.dp, Color(0xFFEA4EA9)),
                    Pair(30.dp, Color(0xFFEA4EA9)),
                    Pair(44.dp, Color(0xFFEA4EA9)),
                    Pair(58.dp, Color(0xFFFBCA3F)),
                    Pair(40.dp, Color(0xFFEA4EA9)),
                    Pair(24.dp, Color(0xFFEA4EA9)),
                    Pair(46.dp, Color(0xFFB54BD6)),
                    Pair(28.dp, Color(0xFF865DEC)),
                    Pair(16.dp, Color(0xFFB9D84A)),
                    Pair(8.dp, Color(0xFF865DEC)),
                )
                bars.forEachIndexed { index, (barHeight, color) ->
                    val isOddTranscript = transcriptIndex % 2 == 1
                    val isEvenBar = index % 2 == 0
                    val shouldBoostHeight = if (isOddTranscript) isEvenBar else false
                    val adjustedHeight = if (shouldBoostHeight) barHeight + 8.dp else barHeight
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(adjustedHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s34") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.GRID, tint = Color(0xFF1A1A2E), size = 22.dp)
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF9646C8), ambientColor = Color(0x4D9646C8))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                    .clickable {
                        viewModel?.setRosaProposalMessage(activeTranscript)
                        onSelectScreenCode("s28")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        transcriptIndex += 1
                        isListening = true
                        viewModel?.setRosaProposalMessage(voiceTranscripts[transcriptIndex % voiceTranscripts.size])
                    },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.REFRESH, tint = Color(0xFF1A1A2E), size = 22.dp)
            }
        }
    }
}

// ==============================================================================
// s36 · BOCETO DE DISTRIBUCIÓN
// ==============================================================================
@Composable
internal fun RenderRosaSketchS36(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val swatchColors = remember {
        listOf(
            Color(0xFFEA4EA9),
            Color(0xFF865DEC),
            Color(0xFF8DB52C),
            Color(0xFFF7B500),
            Color(0xFF1FBDC0),
            Color(0xFF1A1A2E),
        )
    }
    var selectedSwatchIndex by remember { mutableStateOf(0) }
    var selectedToolIndex by remember { mutableStateOf(1) }
    var tableCount by remember { mutableStateOf(6) }
    var showChairsAroundTables by remember { mutableStateOf(false) }
    var showVipZoneLabel by remember { mutableStateOf(false) }
    val activeTableColor = swatchColors[selectedSwatchIndex]

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
                    .background(Color(0xFFFAF6FC))
                    .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s32") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boceto del espacio",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = (19 * 1.1f).sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = "Vista desde arriba · $tableCount mesas",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            if (tableCount > 2) {
                                tableCount -= 1
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.UNDO, tint = Color(0xFF1A1A2E), size = 20.dp)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            tableCount = 4
                            showChairsAroundTables = false
                            showVipZoneLabel = false
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.DELETE, tint = Color(0xFF1A1A2E), size = 20.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp, bottom = 88.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 10.dp, bottom = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEFE8FD))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MINI)
                    BasicText(
                        text = "Dibuja tu espacio y te propongo ejemplos en 3D.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = (14 * 1.35f).sp, color = Color(0xFF1A1A2E)),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(400.dp)
                        .shadow(elevation = 0.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(24.dp))
                        .clickable {
                            if (tableCount < 8) {
                                tableCount += 1
                            } else {
                                tableCount = 6
                            }
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height
                        val scaleX = widthPx / 358f
                        val scaleY = heightPx / 400f

                        val stageLeft = 18f * scaleX
                        val stageTop = 20f * scaleY
                        val stageW = (340f - 18f) * scaleX
                        val stageH = (70f - 20f) * scaleY
                        drawRoundRect(
                            color = Color(0xFF865DEC),
                            topLeft = Offset(stageLeft, stageTop),
                            size = androidx.compose.ui.geometry.Size(stageW, stageH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * scaleX),
                            style = Stroke(3f * scaleX),
                        )

                        val tableR = 24f * scaleX
                        val allTableCenters = listOf(
                            Offset(70f * scaleX, 176f * scaleY),
                            Offset(288f * scaleX, 176f * scaleY),
                            Offset(70f * scaleX, 268f * scaleY),
                            Offset(288f * scaleX, 268f * scaleY),
                            Offset(70f * scaleX, 360f * scaleY),
                            Offset(288f * scaleX, 360f * scaleY),
                            Offset(145f * scaleX, 176f * scaleY),
                            Offset(215f * scaleX, 176f * scaleY),
                        )
                        allTableCenters.take(tableCount).forEach { centerOffset ->
                            drawCircle(activeTableColor, radius = tableR, center = centerOffset, style = Stroke(3f * scaleX))
                            if (showChairsAroundTables) {
                                drawCircle(activeTableColor.copy(alpha = 0.45f), radius = 4f * scaleX, center = Offset(centerOffset.x - tableR - 6f * scaleX, centerOffset.y))
                                drawCircle(activeTableColor.copy(alpha = 0.45f), radius = 4f * scaleX, center = Offset(centerOffset.x + tableR + 6f * scaleX, centerOffset.y))
                                drawCircle(activeTableColor.copy(alpha = 0.45f), radius = 4f * scaleX, center = Offset(centerOffset.x, centerOffset.y - tableR - 6f * scaleY))
                                drawCircle(activeTableColor.copy(alpha = 0.45f), radius = 4f * scaleX, center = Offset(centerOffset.x, centerOffset.y + tableR + 6f * scaleY))
                            }
                        }

                        val danceLeft = 128f * scaleX
                        val danceTop = 250f * scaleY
                        val danceRight = 230f * scaleX
                        val danceBottom = 340f * scaleY
                        val danceP = Path().apply {
                            moveTo(danceLeft, danceTop)
                            lineTo(danceRight, danceTop)
                            lineTo(danceRight, danceBottom)
                            lineTo(danceLeft, danceBottom)
                            close()
                        }
                        drawPath(
                            danceP,
                            color = Color(0xFF8DB52C),
                            style = Stroke(2.6f * scaleX, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f * scaleX, 7f * scaleX))),
                        )

                        val arrowPath = Path().apply {
                            moveTo(150f * scaleX, 375f * scaleY)
                            lineTo(179f * scaleX, 352f * scaleY)
                            lineTo(208f * scaleX, 375f * scaleY)
                            moveTo(179f * scaleX, 352f * scaleY)
                            lineTo(179f * scaleX, 390f * scaleY)
                        }
                        drawPath(arrowPath, color = Color(0xFF1A1A2E), style = Stroke(2.6f * scaleX, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }

                    if (showVipZoneLabel) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 86.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFDE6F2))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            BasicText(
                                text = "Zona VIP · Novios",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC2338F)),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    swatchColors.forEachIndexed { index, swatchColor ->
                        val isSelected = index == selectedSwatchIndex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .border(2.dp, Color.White, CircleShape)
                                            .padding(2.dp)
                                            .border(2.dp, Color(0xFF1A1A2E), CircleShape)
                                            .padding(2.dp)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clip(CircleShape)
                                .background(swatchColor)
                                .clickable { selectedSwatchIndex = index },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = Color(0x73B450B4),
                            ambientColor = Color(0x23B450B4),
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(18.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    val toolButtonModifier: (Int, () -> Unit) -> Modifier = { toolIdx, extraAction ->
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (selectedToolIndex == toolIdx) {
                                    Modifier.background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable {
                                selectedToolIndex = toolIdx
                                extraAction()
                            }
                    }

                    Box(modifier = toolButtonModifier(0) {}, contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.MOVE, tint = if (selectedToolIndex == 0) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(modifier = toolButtonModifier(1) {}, contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.EDIT, tint = if (selectedToolIndex == 1) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(2) {
                            if (tableCount < 8) tableCount += 1
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CIRCLE, tint = if (selectedToolIndex == 2) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(3) {
                            tableCount = if (tableCount < 8) tableCount + 1 else 6
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.TABLE, tint = if (selectedToolIndex == 3) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(4) {
                            showChairsAroundTables = !showChairsAroundTables
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CHAIR, tint = if (selectedToolIndex == 4) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(5) {
                            showVipZoneLabel = !showVipZoneLabel
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Aa",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = if (selectedToolIndex == 5) Color.White else Color(0xFF62627A),
                            ),
                        )
                    }
                    Box(
                        modifier = toolButtonModifier(6) {
                            if (tableCount < 8) tableCount += 1
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.REDO, tint = if (selectedToolIndex == 6) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .height(56.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable {
                        viewModel?.adjustDraftQuantity("art-mesa-redonda", tableCount - 6)
                        onSelectScreenCode("s78")
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "Revisar boceto",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White),
                )
                Spacer(modifier = Modifier.width(8.dp))
                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
            }
        }
    }
}

@Composable
private fun SketchChip78(dotColor: Color, label: String) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(dotColor))
        BasicText(
            text = label,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)),
        )
    }
}

@Composable
private fun BoxScope.SketchLabel78(label: String, yOffset: Dp) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(y = yOffset)
            .width(184.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF62627A)),
        )
    }
}

private fun starPath(cx: Float, cy: Float, outer: Float, inner: Float): Path {
    val path = Path()
    for (pointIndex in 0 until 10) {
        val radius = if (pointIndex % 2 == 0) outer else inner
        val angle = (-90.0 + pointIndex * 36.0) * PI / 180.0
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (pointIndex == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

// ==============================================================================
// s78 · BOCETO · CONFIRMAR
// ==============================================================================
@Composable
internal fun RenderRosaSketchConfirmS78(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
                .background(Color(0xFFFAF6FC))
                .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                    .clickable { onSelectScreenCode("BACK:s36") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Revisa tu boceto",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color(0xFF1A1A2E)),
                )
                BasicText(
                    text = "Boda Real Rosa & Dorado",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, bottom = 92.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp)
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .size(width = 184.dp, height = 226.dp),
                ) {
                    val scaleX = size.width / 358f
                    val scaleY = size.height / 440f

                    val gridStepX = 22f * scaleX
                    val gridStepY = 22f * scaleY
                    for (colIndex in 0..20) {
                        val gx = colIndex * gridStepX
                        if (gx <= size.width) {
                            drawLine(Color(0xFFF3ECF4), Offset(gx, 0f), Offset(gx, size.height), 1f)
                        }
                    }
                    for (rowIndex in 0..25) {
                        val gy = rowIndex * gridStepY
                        if (gy <= size.height) {
                            drawLine(Color(0xFFF3ECF4), Offset(0f, gy), Offset(size.width, gy), 1f)
                        }
                    }

                    drawRoundRect(
                        color = Color(0xFF865DEC),
                        topLeft = Offset(70f * scaleX, 36f * scaleY),
                        size = androidx.compose.ui.geometry.Size(218f * scaleX, 50f * scaleY),
                        cornerRadius = CornerRadius(6f * scaleX, 6f * scaleY),
                        style = Stroke(width = 3f * scaleX),
                    )

                    val tableRadius = 26f * scaleX
                    val tables = listOf(
                        Offset(134f * scaleX, 190f * scaleY),
                        Offset(224f * scaleX, 190f * scaleY),
                        Offset(70f * scaleX, 268f * scaleY),
                        Offset(288f * scaleX, 268f * scaleY),
                        Offset(70f * scaleX, 360f * scaleY),
                        Offset(288f * scaleX, 360f * scaleY),
                    )
                    tables.forEach { center ->
                        drawCircle(
                            color = Color(0xFFEA4EA9),
                            radius = tableRadius,
                            center = center,
                            style = Stroke(width = 3f * scaleX),
                        )
                    }

                    drawRoundRect(
                        color = Color(0xFF8DB52C),
                        topLeft = Offset(128f * scaleX, 250f * scaleY),
                        size = androidx.compose.ui.geometry.Size(102f * scaleX, 90f * scaleY),
                        cornerRadius = CornerRadius(4f * scaleX, 4f * scaleY),
                        style = Stroke(
                            width = 2.6f * scaleX,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(7f * scaleX, 7f * scaleX)),
                        ),
                    )

                    drawLine(
                        color = Color(0xFF1A1A2E),
                        start = Offset(179f * scaleX, 436f * scaleY),
                        end = Offset(179f * scaleX, 404f * scaleY),
                        strokeWidth = 2.6f * scaleX,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color(0xFF1A1A2E),
                        start = Offset(150f * scaleX, 428f * scaleY),
                        end = Offset(179f * scaleX, 404f * scaleY),
                        strokeWidth = 2.6f * scaleX,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color(0xFF1A1A2E),
                        start = Offset(208f * scaleX, 428f * scaleY),
                        end = Offset(179f * scaleX, 404f * scaleY),
                        strokeWidth = 2.6f * scaleX,
                        cap = StrokeCap.Round,
                    )
                }

                SketchLabel78(label = "Tarima", yOffset = (-91).dp)
                SketchLabel78(label = "Pista", yOffset = 29.dp)
                SketchLabel78(label = "Entrada", yOffset = 79.dp)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF5EAF9))
                        .clickable { onSelectScreenCode("s36") }
                        .padding(horizontal = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFFB54BD6), size = 15.dp)
                        BasicText("Editar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB54BD6)))
                    }
                }
            }

            val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value.orEmpty()
            val sketchTableCount = draftQuantities["art-mesa-redonda"] ?: 6
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BasicText(
                    text = "LO QUE DIBUJASTE",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF62627A), letterSpacing = 1.sp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SketchChip78(dotColor = Color(0xFF865DEC), label = "Tarima")
                        SketchChip78(dotColor = Color(0xFFEA4EA9), label = "$sketchTableCount mesas redondas")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SketchChip78(dotColor = Color(0xFF8DB52C), label = "Pista de baile")
                        SketchChip78(dotColor = Color(0xFF1A1A2E), label = "Entrada")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfHeroBannerShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .padding(16.dp),
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color(0xFFD6EA8C),
                        radius = 4.dp.toPx(),
                        center = Offset(size.width - 38.dp.toPx(), 44.dp.toPx()),
                    )
                    drawPath(
                        path = starPath(
                            cx = size.width - 16.dp.toPx(),
                            cy = 24.dp.toPx(),
                            outer = 10.dp.toPx(),
                            inner = 4.dp.toPx(),
                        ),
                        color = Color(0xFFFFD45C),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x38FFFFFF)),
                        ) {
                            RosaAvatar(size = RosaAvatarSize.MEDIUM)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "¿Te ayudo a completarlo?",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                            )
                            Spacer(Modifier.height(2.dp))
                            BasicText(
                                text = "Ordeno tu boceto, calculo cuántas sillas caben y te propongo ejemplos en 3D.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFFF6EAFB), lineHeight = 20.sp),
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .clickable {
                                viewModel?.adjustDraftQuantity("art-silla-tiffany", sketchTableCount * 10)
                                onSelectScreenCode("s32")
                            },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFFA23FB8), size = 18.dp)
                            BasicText(
                                "Ayúdame con IA",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFA23FB8)),
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s25") },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                    BasicText(
                        "Guardar en mi evento",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onSelectScreenCode("s36") },
            ) {
                BasicText(
                    "Seguir editando",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF62627A)),
                )
            }
        }
    }
}

// ==============================================================================
// A08 · ADMIN · ROSA IA
// ==============================================================================
private data class AdminRosaChatTurn(
    val userText: String,
    val rosaReply: String,
)

private fun formatDopCurrency(amount: Int): String {
    if (amount < 1000) {
        return "RD$ $amount"
    }
    val thousands = amount / 1000
    val remainder = (amount % 1000).toString().padStart(3, '0')
    return "RD$ $thousands,$remainder"
}

@Composable
internal fun RenderAdminRosaA08(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val clientCycle = remember { listOf("Camila Rodríguez", "Laura Peña", "Eventos Corporativos SD") }
    val dateCycle = remember {
        listOf(
            "Sáb 14 nov" to "14 de noviembre",
            "Sáb 21 nov" to "21 de noviembre",
            "Vie 4 dic" to "4 de diciembre",
        )
    }

    var eventTypeTitle by remember { mutableStateOf("Boda") }
    var assignedClient by remember { mutableStateOf<String?>(null) }
    var clientCycleIndex by remember { mutableStateOf(0) }
    var dateCycleIndex by remember { mutableStateOf(0) }
    var includeLighting by remember { mutableStateOf(false) }
    var includeDessertStation by remember { mutableStateOf(false) }
    var guestCount by remember { mutableStateOf(120) }
    var promptText by remember { mutableStateOf("") }
    var extraChatTurns by remember { mutableStateOf(emptyList<AdminRosaChatTurn>()) }
    val messagesScrollState = rememberScrollState()

    LaunchedEffect(extraChatTurns.size) {
        if (extraChatTurns.isNotEmpty()) {
            messagesScrollState.animateScrollTo(messagesScrollState.maxValue)
        }
    }

    val currentShortDate = dateCycle[dateCycleIndex % dateCycle.size].first
    val currentLongDate = dateCycle[dateCycleIndex % dateCycle.size].second
    val chairCount = guestCount
    val tableCount = (guestCount / 10).coerceAtLeast(1)
    val baseEstimateDop = chairCount * 8 + tableCount * 20 + 95
    val lightingSurcharge = if (includeLighting) 180 else 0
    val dessertSurcharge = if (includeDessertStation) 150 else 0
    val totalEstimateDop = baseEstimateDop + lightingSurcharge + dessertSurcharge
    val formattedEstimate = formatDopCurrency(totalEstimateDop)

    val syncDraftToViewModel = {
        val targetClient = assignedClient ?: "Camila Rodríguez"
        viewModel?.setEventTypeInput(eventTypeTitle)
        viewModel?.setEventDateInput(currentShortDate)
        viewModel?.setEventGuestsInput(guestCount.toString())
        viewModel?.setNewEventTitle("$eventTypeTitle de $targetClient")
        viewModel?.setNewEventClient(targetClient)
        viewModel?.setNewEventDate(currentShortDate)
        viewModel?.setNewEventGuests(guestCount.toString())
        viewModel?.setDraftQuantity("art-silla-tiffany", chairCount)
        viewModel?.setDraftQuantity("art-mesa-redonda", tableCount)
        viewModel?.setDraftQuantity("art-centro-floral", tableCount)
        viewModel?.setDraftQuantity("art-arco-rosa", 1)
    }

    val handleUserCommand: (String) -> Unit = { rawCommand ->
        val trimmed = rawCommand.trim()
        if (trimmed.isNotEmpty()) {
            val lower = trimmed.lowercase()
            val replySegments = mutableListOf<String>()

            val mentionsClient = listOf("cliente", "camila", "laura", "ana", "corporat", "asignar").any { keyword -> lower.contains(keyword) }
            if (mentionsClient) {
                val chosenClient = when {
                    lower.contains("laura") -> "Laura Peña"

                    lower.contains("ana") -> "Ana Gómez"

                    lower.contains("corporat") -> "Eventos Corporativos SD"

                    lower.contains("camila") -> "Camila Rodríguez"

                    else -> {
                        val cycled = clientCycle[clientCycleIndex % clientCycle.size]
                        clientCycleIndex += 1
                        cycled
                    }
                }
                assignedClient = chosenClient
                viewModel?.setNewEventClient(chosenClient)
                replySegments.add("Asigné el borrador a $chosenClient y vinculé su historial.")
            }

            val parsedNumber = Regex("\\b(\\d{2,3})\\b").find(lower)?.groupValues?.getOrNull(1)?.toIntOrNull()
            val mentionsGuestsKeyword = listOf("invitado", "persona", "silla").any { keyword -> lower.contains(keyword) }
            val hasExplicitGuestUpdate = parsedNumber != null || mentionsGuestsKeyword
            if (hasExplicitGuestUpdate) {
                val updatedGuests = parsedNumber ?: if (guestCount == 120) 150 else 120
                guestCount = updatedGuests
                val updatedTables = (updatedGuests / 10).coerceAtLeast(1)
                viewModel?.setEventGuestsInput(updatedGuests.toString())
                viewModel?.setNewEventGuests(updatedGuests.toString())
                viewModel?.setDraftQuantity("art-silla-tiffany", updatedGuests)
                viewModel?.setDraftQuantity("art-mesa-redonda", updatedTables)
                replySegments.add("Ajusté el borrador para $updatedGuests invitados ($updatedGuests sillas Tiffany y $updatedTables mesas redondas).")
            }

            val mentionsLighting = listOf("ilumin", "luz", "luces", "guirnalda").any { keyword -> lower.contains(keyword) }
            if (mentionsLighting) {
                val wantsRemoval = lower.contains("quitar") || lower.contains("sin ")
                includeLighting = if (wantsRemoval) false else !includeLighting
                if (includeLighting) {
                    replySegments.add("Agregué 4 sets de Guirnalda LED cálida (+RD$ 180) al borrador.")
                } else {
                    replySegments.add("Quité la iluminación adicional del borrador.")
                }
            }

            val mentionsDessert = listOf("dulce", "helado", "postre", "candy").any { keyword -> lower.contains(keyword) }
            if (mentionsDessert) {
                includeDessertStation = !includeDessertStation
                if (includeDessertStation) {
                    viewModel?.adjustDraftQuantity("art-mesa-helados", 1)
                    replySegments.add("Incluí la estación de dulces y helados (+RD$ 150).")
                } else {
                    replySegments.add("Retiré la estación de dulces del borrador.")
                }
            }

            val mentionsDate = listOf("fecha", "nov", "dic", "sáb", "viernes").any { keyword -> lower.contains(keyword) }
            if (mentionsDate) {
                dateCycleIndex += 1
                val nextDate = dateCycle[dateCycleIndex % dateCycle.size]
                viewModel?.setEventDateInput(nextDate.first)
                viewModel?.setNewEventDate(nextDate.first)
                replySegments.add("Actualicé la fecha del evento al ${nextDate.second} con disponibilidad confirmada.")
            }

            val parsedEventType = when {
                lower.contains("quince") -> "Quinceañera"
                lower.contains("cumple") -> "Cumpleaños"
                lower.contains("baby") -> "Baby Shower"
                lower.contains("gradu") -> "Graduación"
                lower.contains("corporat") -> "Corporativo"
                lower.contains("boda") -> "Boda"
                else -> null
            }
            if (parsedEventType != null) {
                eventTypeTitle = parsedEventType
                viewModel?.setEventTypeInput(parsedEventType)
                replySegments.add("Configuré el tipo de evento como $parsedEventType.")
            }

            val updatedChairs = guestCount
            val updatedTables = (guestCount / 10).coerceAtLeast(1)
            val updatedTotal = updatedChairs * 8 + updatedTables * 20 + 95 + (if (includeLighting) 180 else 0) + (if (includeDessertStation) 150 else 0)
            val replyText = if (replySegments.isNotEmpty()) {
                "${replySegments.joinToString(" ")} Nuevo estimado: ${formatDopCurrency(updatedTotal)}."
            } else {
                "Perfecto, tomé nota de \"$trimmed\" y actualicé las observaciones del borrador ($eventTypeTitle · ${formatDopCurrency(updatedTotal)})."
            }
            viewModel?.setRosaProposalMessage(trimmed)
            extraChatTurns = extraChatTurns + AdminRosaChatTurn(userText = trimmed, rosaReply = replyText)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AdminRosaBackgroundGradient)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp, forceAdminPalette = true)

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Rosa",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 20.sp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText("Asistente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .background(Color(0xFFECEBFF), CircleShape)
                                .padding(horizontal = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Admin", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(messagesScrollState)
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .widthIn(max = 290.dp)
                        .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp), spotColor = Color(0xFA5B6CF0), ambientColor = Color(0x705B6CF0))
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicText(
                        text = "Arma una boda para 120 personas el sábado 14 de noviembre en un jardín, blanco y dorado.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color.White, lineHeight = 21.sp),
                    )
                }

                Column(
                    modifier = Modifier.widthIn(max = 330.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(10.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp), spotColor = Color(0xC04650B4), ambientColor = Color(0x504650B4))
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        val bubbleText = buildAnnotatedString {
                            append("Listo. Revisé el inventario para el ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(currentLongDate)
                            }
                            append(" y te armé este borrador:")
                        }
                        BasicText(
                            text = bubbleText,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729), lineHeight = 21.sp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(20.dp)),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF7F6FF))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        RfIcon(kind = RfIconKind.RING, tint = Color(0xFF2E324A), size = 16.dp)
                                        BasicText(
                                            text = "$eventTypeTitle · $currentShortDate",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                        )
                                    }
                                    if (assignedClient != null) {
                                        BasicText(
                                            text = "Cliente: $assignedClient",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .background(Color(0xFFECEBFF), CircleShape)
                                        .clickable {
                                            guestCount = if (guestCount == 120) 150 else 120
                                        }
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = "$guestCount invitados",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)),
                                    )
                                }
                            }

                            val baseProposalItems = listOf(
                                Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal" to "$chairCount", "Disponible" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                                Triple(RfPhotoKey.MESA_REDONDA, "Mesa redonda" to "$tableCount", "Disponible" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                                Triple(RfPhotoKey.CENTRO_FLORAL, "Centro de mesa floral" to "$tableCount", "Disponible" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                                Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa" to "1", "Queda 1" to (Color(0xFFFFF2C9) to Color(0xFF7A5700))),
                            )
                            val withLightingItems = if (includeLighting) {
                                baseProposalItems + Triple(
                                    RfPhotoKey.BODA,
                                    "Guirnalda luces LED" to "4",
                                    "Agregado" to (Color(0xFFECEBFF) to Color(0xFF4B47D6)),
                                )
                            } else {
                                baseProposalItems
                            }
                            val proposalItems = if (includeDessertStation) {
                                withLightingItems + Triple(
                                    RfPhotoKey.MESA_HELADOS,
                                    "Carrito de helados" to "1",
                                    "Agregado" to (Color(0xFFECEBFF) to Color(0xFF4B47D6)),
                                )
                            } else {
                                withLightingItems
                            }

                            proposalItems.forEach { (photo, itemData, tagInfo) ->
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                    ) {
                                        RfPhoto(photoKey = photo, contentDescription = itemData.first, modifier = Modifier.fillMaxSize())
                                    }
                                    val itemTitle = buildAnnotatedString {
                                        append(itemData.first)
                                        append(" ")
                                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = Color(0xFF5F6377))) {
                                            append("× ${itemData.second}")
                                        }
                                    }
                                    BasicText(
                                        text = itemTitle,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(22.dp)
                                            .background(tagInfo.second.first, CircleShape)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(tagInfo.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tagInfo.second.second))
                                    }
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    BasicText("Estimado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                    BasicText(formattedEstimate, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .rfPrimaryButtonShadow(RoundedCornerShape(12.dp), isAdmin = true)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                            .clickable {
                                                syncDraftToViewModel()
                                                onSelectScreenCode("A07")
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                            BasicText("Usar borrador", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                                            .clickable {
                                                syncDraftToViewModel()
                                                onSelectScreenCode("A06")
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF151729), size = 16.dp)
                                            BasicText("Ajustar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                extraChatTurns.forEach { turn ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .widthIn(max = 290.dp)
                            .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp), spotColor = Color(0x805B6CF0))
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = turn.userText,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color.White, lineHeight = 20.sp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .widthIn(max = 310.dp)
                            .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp), spotColor = Color(0x604650B4))
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = turn.rosaReply,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729), lineHeight = 20.sp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val hasClientAssigned = assignedClient != null
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x804650B4), ambientColor = Color(0x304650B4))
                            .clip(CircleShape)
                            .background(if (hasClientAssigned) Color(0xFFECEBFF) else Color.White)
                            .border(1.dp, if (hasClientAssigned) Color(0xFF5B6CF0) else Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                handleUserCommand("Asignar a un cliente")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.USER, tint = if (hasClientAssigned) Color(0xFF4B47D6) else Color(0xFF151729), size = 16.dp)
                            BasicText(
                                text = assignedClient ?: "Asignar a un cliente",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (hasClientAssigned) Color(0xFF4B47D6) else Color(0xFF151729)),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x804650B4), ambientColor = Color(0x304650B4))
                            .clip(CircleShape)
                            .background(if (includeLighting) Color(0xFFECEBFF) else Color.White)
                            .border(1.dp, if (includeLighting) Color(0xFF5B6CF0) else Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                handleUserCommand("Agregar iluminación")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.LIGHTBULB, tint = if (includeLighting) Color(0xFF4B47D6) else Color(0xFF151729), size = 16.dp)
                            BasicText(
                                text = if (includeLighting) "Iluminación agregada" else "Agregar iluminación",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (includeLighting) Color(0xFF4B47D6) else Color(0xFF151729)),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x804650B4), ambientColor = Color(0x304650B4))
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                handleUserCommand("Cambiar fecha")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Cambiar fecha ($currentShortDate)",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                        )
                    }
                }

                val hasPromptText = promptText.isNotBlank()
                val submitAdminRosaPrompt = {
                    if (promptText.isNotBlank()) {
                        val submitted = promptText
                        promptText = ""
                        handleUserCommand(submitted)
                    } else {
                        handleUserCommand("Asignar a Camila Rodríguez y agregar iluminación cálida")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(58.dp)
                        .shadow(14.dp, CircleShape, spotColor = Color(0xC04650B4), ambientColor = Color(0x504650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), CircleShape)
                        .padding(start = 20.dp, end = 6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (promptText.isEmpty()) {
                                BasicText(
                                    text = "Pídele algo más a Rosa…",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            BasicTextField(
                                value = promptText,
                                onValueChange = { updatedText -> promptText = updatedText },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { submitAdminRosaPrompt() }),
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(10.dp, CircleShape, spotColor = Color(0xFA5B6CF0), ambientColor = Color(0x705B6CF0))
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                .clickable { submitAdminRosaPrompt() },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(
                                kind = if (hasPromptText) RfIconKind.SEND else RfIconKind.MIC,
                                tint = Color.White,
                                size = 22.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}
