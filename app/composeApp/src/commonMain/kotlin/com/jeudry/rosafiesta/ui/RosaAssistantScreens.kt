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
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.BalloonProduct
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.PhotoAsset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val RosaBackgroundGradient: Brush
    get() = RfThemeState.currentPalette.rosaAssistantBgGradient

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

private data class RosaEventTypeMetadata(
    val displayTitle: String,
    val spanishGreeting: String,
    val subtitleBadge: String,
    val iconKind: RfIconKind,
)

private fun photoAssetToRfPhotoKey(photo: PhotoAsset): RfPhotoKey = when (photo) {
    PhotoAsset.SILLA_TIFFANY -> RfPhotoKey.SILLA_TIFFANY
    PhotoAsset.MESA_PLEGABLE -> RfPhotoKey.MESA_PLEGABLE
    PhotoAsset.MESA_REDONDA -> RfPhotoKey.MESA_REDONDA
    PhotoAsset.CENTRO_FLORAL -> RfPhotoKey.CENTRO_FLORAL
    PhotoAsset.ARCO_ROSA -> RfPhotoKey.ARCO_ROSA
    PhotoAsset.MESA_HELADOS -> RfPhotoKey.MESA_HELADOS
    PhotoAsset.BODA -> RfPhotoKey.BODA
    PhotoAsset.QUINCEANERA -> RfPhotoKey.QUINCEANERA
    PhotoAsset.CUMPLE_INFANTIL -> RfPhotoKey.CUMPLE_INFANTIL
    PhotoAsset.BABY_SHOWER -> RfPhotoKey.BABY_SHOWER
    PhotoAsset.GRADUACION -> RfPhotoKey.GRADUACION
    PhotoAsset.CORPORATIVO -> RfPhotoKey.CORPORATIVO
}

private fun resolveRosaEventMetadata(rawEventType: String): RosaEventTypeMetadata {
    val cleanType = rawEventType.trim()
    val lower = cleanType.lowercase()
    return when {
        cleanType.isEmpty() -> RosaEventTypeMetadata(
            displayTitle = "Celebración",
            spanishGreeting = "¡Una celebración especial!",
            subtitleBadge = "Diseño a medida para tu celebración",
            iconKind = RfIconKind.SPARKLE,
        )

        lower.contains("boda") -> RosaEventTypeMetadata(
            displayTitle = "Boda",
            spanishGreeting = "¡Una boda!",
            subtitleBadge = "Ceremonia y recepción nupcial",
            iconKind = RfIconKind.RING,
        )

        lower.contains("cumple") -> RosaEventTypeMetadata(
            displayTitle = "Cumpleaños",
            spanishGreeting = "¡Un cumpleaños!",
            subtitleBadge = "Celebración, pastel y ambientación temática",
            iconKind = RfIconKind.CAKE,
        )

        lower.contains("baby") -> RosaEventTypeMetadata(
            displayTitle = "Baby shower",
            spanishGreeting = "¡Un baby shower!",
            subtitleBadge = "Bienvenida tierna y estación dulce",
            iconKind = RfIconKind.GIFT,
        )

        lower.contains("gender") -> RosaEventTypeMetadata(
            displayTitle = "Gender reveal",
            spanishGreeting = "¡Un gender reveal!",
            subtitleBadge = "Revelación especial con globos y detalles",
            iconKind = RfIconKind.PARTY,
        )

        lower.contains("corporat") -> RosaEventTypeMetadata(
            displayTitle = "Corporativo",
            spanishGreeting = "¡Una fiesta corporativa!",
            subtitleBadge = "Montaje ejecutivo, coctel y protocolo",
            iconKind = RfIconKind.STAR,
        )

        lower.contains("gradu") -> RosaEventTypeMetadata(
            displayTitle = "Graduación",
            spanishGreeting = "¡Una graduación!",
            subtitleBadge = "Brindis de logro y escenario fotográfico",
            iconKind = RfIconKind.STAR,
        )

        lower.contains("anivers") -> RosaEventTypeMetadata(
            displayTitle = "Aniversario",
            spanishGreeting = "¡Un aniversario!",
            subtitleBadge = "Velada romántica y detalles florales",
            iconKind = RfIconKind.HEART,
        )

        lower.contains("quince") -> RosaEventTypeMetadata(
            displayTitle = "Quinceañera",
            spanishGreeting = "¡Una quinceañera!",
            subtitleBadge = "Recepción de gala y pista principal",
            iconKind = RfIconKind.SPARKLE,
        )

        lower.contains("bautiz") -> RosaEventTypeMetadata(
            displayTitle = "Bautizo",
            spanishGreeting = "¡Un bautizo!",
            subtitleBadge = "Recepción familiar luminosa y delicada",
            iconKind = RfIconKind.SPARK,
        )

        else -> {
            val isFeminineNoun = listOf("fiesta", "cena", "gala", "recepción", "recepcion", "reunión", "reunion")
                .any { feminineWord -> lower.startsWith(feminineWord) }
            val grammaticalExclamation = if (isFeminineNoun) {
                "¡Una ${cleanType.lowercase()}!"
            } else {
                "¡Un evento de ${cleanType.lowercase()}!"
            }
            RosaEventTypeMetadata(
                displayTitle = cleanType,
                spanishGreeting = grammaticalExclamation,
                subtitleBadge = "Montaje personalizado · $cleanType",
                iconKind = RfIconKind.SPARKLE,
            )
        }
    }
}

private fun openRosaVoiceScreen(
    viewModel: RosaFiestaViewModel?,
    returnScreenCode: String,
    onSelectScreenCode: (String) -> Unit,
    initialText: String = "",
) {
    viewModel?.setVoiceReturnScreenCode(returnScreenCode)
    viewModel?.startVoiceInput(VoiceInputOrigin.ROSA_WIZARD, initialText)
    onSelectScreenCode("s35")
}

@Composable
private fun RosaWizardHeader(
    step: Int,
    totalSteps: Int = 6,
    cartCount: Int? = null,
    viewModel: RosaFiestaViewModel? = null,
    onBack: () -> Unit = {},
    onCart: () -> Unit = {},
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val observedDraftQuantities = viewModel?.draftQuantities?.collectAsState()?.value
    val liveDraftUnits = observedDraftQuantities
        ?.values
        ?.filter { unitCount -> unitCount > 0 }
        ?.sum()
        ?: viewModel?.totalDraftCartUnits()
        ?: 0
    val resolvedCartCount = cartCount ?: liveDraftUnits

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(2f)
            .background(if (RfThemeState.currentPalette.isDark) Color(0xFF1E1A36) else Color(0xFFDED5F7))
            .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .rfIconButtonShadow(CircleShape, isAdmin = false)
                .clip(CircleShape)
                .background(RfThemeState.currentPalette.surface)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(
                kind = RfIconKind.BACK,
                tint = RfThemeState.currentPalette.ink,
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
                    color = RfThemeState.currentPalette.ink,
                ),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(150.dp),
            ) {
                for (stepBarIndex in 1..totalSteps) {
                    val targetCode = when (stepBarIndex) {
                        1 -> if (step > 1) "BACK:s26" else "s26"
                        2 -> if (step > 2) "BACK:s28" else "s28"
                        3 -> if (step > 3) "BACK:s29" else "s29"
                        4 -> if (step > 4) "BACK:s30" else "s30"
                        5 -> if (step > 5) "BACK:s31" else "s31"
                        else -> "s32"
                    }
                    val canNavigateToSegment = stepBarIndex <= step
                    val segmentBackground = if (canNavigateToSegment) {
                        Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB)))
                    } else {
                        SolidColor(Color(0xFFDDD3F3))
                    }
                    val segmentClickModifier = if (canNavigateToSegment) {
                        Modifier.clickable { onSelectScreenCode(targetCode) }
                    } else {
                        Modifier
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(segmentBackground)
                            .then(segmentClickModifier),
                    )
                }
            }
        }

        Box(
            modifier = Modifier.size(44.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .clickable(onClick = onCart),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(
                    kind = RfIconKind.PARTY,
                    tint = RfThemeState.currentPalette.ink,
                    size = 22.dp,
                )
            }
            if (resolvedCartCount > 0) {
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
                        text = "$resolvedCartCount",
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

private data class DraftCartResolvedRow(
    val itemId: String,
    val lineItem: EventLineItem,
    val categoryLabel: String,
)

@Composable
internal fun RenderMyEventDraftS25(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value.orEmpty()
    val allArticles: List<CatalogArticle> = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val allBalloons: List<BalloonProduct> = viewModel?.balloonProducts?.collectAsState()?.value ?: CanonicalSampleData.balloonProducts
    val draftDiscountDop = viewModel?.draftDiscountDop?.collectAsState()?.value ?: 0
    val formError = viewModel?.formError?.collectAsState()?.value

    val rawLineItems: List<EventLineItem> = remember(draftQuantities, allArticles, allBalloons, viewModel) {
        viewModel?.draftLineItems() ?: emptyList()
    }

    val resolvedRows: List<DraftCartResolvedRow> = remember(rawLineItems, allArticles, allBalloons) {
        rawLineItems.map { lineItem ->
            val matchedArticle = allArticles.firstOrNull { articleItem -> articleItem.code == lineItem.articleCode }
            val matchedBalloon = allBalloons.firstOrNull { balloonItem -> balloonItem.code == lineItem.articleCode }
            val resolvedId = matchedArticle?.id ?: matchedBalloon?.id ?: lineItem.articleCode
            val categoryName = matchedArticle?.category ?: if (matchedBalloon != null) "Boutique de globos" else "Alquiler"
            DraftCartResolvedRow(
                itemId = resolvedId,
                lineItem = lineItem,
                categoryLabel = categoryName,
            )
        }
    }

    val totalItemsCount: Int = resolvedRows.sumOf { row -> row.lineItem.quantity }
    val subtotalDop: Int = resolvedRows.sumOf { row -> row.lineItem.subtotalDop }
    val appliedDiscountDop: Int = draftDiscountDop.coerceAtLeast(0).coerceAtMost(subtotalDop)
    val totalPriceDop: Int = (subtotalDop - appliedDiscountDop).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    0.0f to RfThemeState.currentPalette.radialPrimary,
                    0.6f to RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f),
                    center = Offset(0f, 0f),
                    radius = 400f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    0.0f to RfThemeState.currentPalette.radialSecondary,
                    0.6f to RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f),
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
            Spacer(modifier = Modifier.height(16.dp))

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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s18") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Mi evento",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.44).sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                    BasicText(
                        text = "Borrador de cotización",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Borrador",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!formError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFCE8E8))
                        .border(1.dp, Color(0xFFF5B6C2), RoundedCornerShape(14.dp))
                        .clickable { viewModel.clearFormError() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFB42345), size = 18.dp)
                        BasicText(
                            text = formError,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFFB42345),
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (resolvedRows.isEmpty()) {
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
                        BasicText(
                            text = "Tu carrito de evento está vacío",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "Explora el catálogo o la tienda de globos para armar tu cotización",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp)),
                ) {
                    Column {
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
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                                )
                                BasicText(
                                    "${resolvedRows.size} artículos · $totalItemsCount unidades",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicText(
                                "RD$ $subtotalDop",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = (-0.34).sp, color = RfThemeState.currentPalette.ink),
                            )
                        }

                        resolvedRows.forEach { row ->
                            val lineItem = row.lineItem
                            val photoKey = photoAssetToRfPhotoKey(lineItem.photo)
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))) {
                                    RfPhoto(photoKey = photoKey, contentDescription = lineItem.name, cornerRadius = 14.dp, modifier = Modifier.fillMaxSize())
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    BasicText(
                                        text = lineItem.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                    )
                                    BasicText(
                                        text = "${row.categoryLabel} · RD$ ${lineItem.unitPriceDop} c/u",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                    BasicText(
                                        text = "RD$ ${lineItem.subtotalDop}",
                                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = (-0.32).sp, color = RfThemeState.currentPalette.ink),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(12.dp)).height(36.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier.size(32.dp, 36.dp).clickable {
                                                viewModel?.clearFormError()
                                                viewModel?.adjustDraftQuantity(row.itemId, -1)
                                            },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.MINUS, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                                        }
                                        BasicText(
                                            text = "${lineItem.quantity}",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                        )
                                        Box(
                                            modifier = Modifier.size(32.dp, 36.dp).clickable {
                                                viewModel?.clearFormError()
                                                viewModel?.adjustDraftQuantity(row.itemId, 1)
                                            },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.PLUS, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                                        }
                                    }
                                    BasicText(
                                        text = "Quitar",
                                        modifier = Modifier.clickable {
                                            viewModel?.clearFormError()
                                            viewModel?.setDraftQuantity(row.itemId, 0)
                                        },
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                            }
                        }

                        if (appliedDiscountDop > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEEF6D6))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText(
                                    text = "Descuento de paquete aplicado",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4E6A10)),
                                )
                                BasicText(
                                    text = "- RD$ $appliedDiscountDop",
                                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF4E6A10)),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    RfIcon(kind = RfIconKind.PLUS, tint = RfThemeState.currentPalette.ink, size = 18.dp)
                    BasicText(
                        "Agregar más artículos",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                    )
                }
            }

            Spacer(modifier = Modifier.height(180.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line),
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
                        "$totalItemsCount unidades · ${resolvedRows.size} seleccionados",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                    )
                    BasicText(
                        "RD$ $totalPriceDop",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.48).sp,
                            color = RfThemeState.currentPalette.ink,
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
                        .clickable {
                            if (resolvedRows.isEmpty()) {
                                viewModel?.postError("Agrega al menos un artículo al carrito para ver la cotización oficial.")
                            } else {
                                viewModel?.clearFormError()
                                val syncedQuote = viewModel?.syncDraftToOfficialQuote()
                                val canOpenOfficialQuote = if (viewModel == null) true else syncedQuote != null
                                if (canOpenOfficialQuote) {
                                    onSelectScreenCode("s48")
                                }
                            }
                        },
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

@Composable
internal fun RenderRosaEventTypeS26(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val activeTypeLabel = vmEventTypeInput.ifBlank { vmRosaEventType }
    val activeMetadata = resolveRosaEventMetadata(activeTypeLabel)
    var promptText by remember { mutableStateOf("") }
    var isOtherSelected by remember { mutableStateOf(false) }
    var customEventTypeInput by remember { mutableStateOf("") }

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
            viewModel = viewModel,
            onBack = { onSelectScreenCode("BACK:s14") },
            onCart = { onSelectScreenCode("s25") },
            onSelectScreenCode = onSelectScreenCode,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 88.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    Column {
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
                        BasicText(
                            text = activeMetadata.subtitleBadge,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF7A4FD8)),
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
                            color = RfThemeState.currentPalette.ink,
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
                            color = RfThemeState.currentPalette.ink,
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
                                        if (item.name == "Otro") {
                                            isOtherSelected = true
                                        } else {
                                            isOtherSelected = false
                                            viewModel?.updateRosaParameters(eventType = item.name)
                                            viewModel?.setEventTypeInput(item.name)
                                            onSelectScreenCode("s27")
                                        }
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

            if (isOtherSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.5.dp, Color(0xFFB754CB), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BasicText(
                            text = "Personaliza tu celebración",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF7A4FD8),
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RfThemeState.currentPalette.chipBg)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (customEventTypeInput.isEmpty()) {
                                BasicText(
                                    text = "Describe tu tipo de evento...",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicTextField(
                                value = customEventTypeInput,
                                onValueChange = { updatedCustom ->
                                    customEventTypeInput = updatedCustom
                                    val cleanCustom = updatedCustom.trim()
                                    if (cleanCustom.isNotEmpty()) {
                                        viewModel?.updateRosaParameters(eventType = cleanCustom)
                                        viewModel?.setEventTypeInput(cleanCustom)
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("Graduación", "Aniversario", "Quinceañera").forEach { quickCustomType ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFFDE6F2))
                                        .clickable {
                                            customEventTypeInput = quickCustomType
                                            viewModel?.updateRosaParameters(eventType = quickCustomType)
                                            viewModel?.setEventTypeInput(quickCustomType)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    BasicText(
                                        text = quickCustomType,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFC2338F)),
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RosaButtonGradient)
                                .clickable {
                                    val resolvedCustom = customEventTypeInput.trim()
                                    if (resolvedCustom.isEmpty()) {
                                        viewModel?.postError("Escribe qué tipo de evento deseas celebrar.")
                                    } else {
                                        viewModel?.clearFormError()
                                        viewModel?.updateRosaParameters(eventType = resolvedCustom)
                                        viewModel?.setEventTypeInput(resolvedCustom)
                                        onSelectScreenCode("s27")
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Continuar con este evento",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .fillMaxWidth()
                .height(58.dp)
                .rfInputShadow(CircleShape, isAdmin = false)
                .clip(CircleShape)
                .background(RfThemeState.currentPalette.surface)
                .padding(start = 20.dp, end = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            val hasPromptText = promptText.isNotBlank()
            val submitS26Prompt = {
                if (promptText.isNotBlank()) {
                    val submittedText = promptText.trim()
                    applyNaturalLanguageEventPrompt(submittedText, viewModel)
                    viewModel?.sendConversationalRosaPrompt(submittedText)
                    promptText = ""
                    onSelectScreenCode("s27")
                } else {
                    openRosaVoiceScreen(
                        viewModel = viewModel,
                        returnScreenCode = "s26",
                        onSelectScreenCode = onSelectScreenCode,
                    )
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
                                color = RfThemeState.currentPalette.muted,
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
                            color = RfThemeState.currentPalette.ink,
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
        lower.contains("gender") -> "Gender reveal"
        lower.contains("gradu") -> "Graduación"
        lower.contains("anivers") -> "Aniversario"
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
        viewModel?.updateRosaParameters(eventType = parsed.eventType)
    }
    if (parsed.guestsCount != null) {
        viewModel?.setEventGuestsInput(parsed.guestsCount.toString())
        viewModel?.updateRosaParameters(guests = parsed.guestsCount)
    }
    if (parsed.dateLabel != null) {
        viewModel?.setEventDateInput(parsed.dateLabel)
    }
    if (parsed.venueLabel != null) {
        viewModel?.setEventVenueInput(parsed.venueLabel)
    }
    return parsed
}

@Composable
internal fun RenderRosaTutorialS27(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val selectedEventType = vmEventTypeInput.ifBlank { vmRosaEventType }.ifBlank { "Boda" }
    val eventMetadata = resolveRosaEventMetadata(selectedEventType)
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
            viewModel = viewModel,
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
                            RfIcon(kind = eventMetadata.iconKind, tint = Color.White, size = 18.dp)
                            Column {
                                BasicText(
                                    text = eventMetadata.displayTitle,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                    ),
                                )
                                BasicText(
                                    text = eventMetadata.subtitleBadge,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFF6EAFB),
                                    ),
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    BasicText(
                        text = "${eventMetadata.spanishGreeting} Me encanta.",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            lineHeight = (26 * 1.05f).sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RfThemeState.currentPalette.surface)
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
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("toca el micrófono") }
                                append(" y háblame.")
                            },
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 17.sp,
                                lineHeight = (17 * 1.45f).sp,
                                color = RfThemeState.currentPalette.ink,
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
            val dashEffect = PathEffect.dashPathEffect(
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
                val submittedText = promptText.trim()
                applyNaturalLanguageEventPrompt(submittedText, viewModel)
                viewModel?.sendConversationalRosaPrompt(submittedText)
                promptText = ""
                onSelectScreenCode("s28")
            } else {
                openRosaVoiceScreen(
                    viewModel = viewModel,
                    returnScreenCode = "s27",
                    onSelectScreenCode = onSelectScreenCode,
                )
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
                .background(RfThemeState.currentPalette.surface)
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
                        color = RfThemeState.currentPalette.ink,
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

@Composable
internal fun RenderRosaHelpS28(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val selectedEventType = vmEventTypeInput.ifBlank { vmRosaEventType }.ifBlank { "Boda" }
    val eventMetadata = resolveRosaEventMetadata(selectedEventType)
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
            viewModel = viewModel,
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
                    RfIcon(kind = eventMetadata.iconKind, tint = Color.White, size = 18.dp)
                    Column {
                        BasicText(
                            text = eventMetadata.displayTitle,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            ),
                        )
                        BasicText(
                            text = eventMetadata.subtitleBadge,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFF6EAFB),
                            ),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp)

                BasicText(
                    text = eventMetadata.spanishGreeting,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF9B45C8),
                    ),
                )

                BasicText(
                    text = "¿Te ayudo a elegir los artículos de alquiler?",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = RfThemeState.currentPalette.ink,
                        lineHeight = 28.sp,
                    ),
                )
                BasicText(
                    text = "Te recomiendo según tu ${eventMetadata.displayTitle.lowercase()}. Puedes cambiar todo después.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        color = RfThemeState.currentPalette.muted,
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
                    .background(RfThemeState.currentPalette.surface)
                    .clickable { onSelectScreenCode("s18") }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RfIcon(kind = RfIconKind.CATALOG, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                BasicText(
                    text = "No, prefiero ver el catálogo",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RfThemeState.currentPalette.ink,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val hasPromptText = promptText.isNotBlank()
        val submitS28Prompt = {
            if (promptText.isNotBlank()) {
                val submittedText = promptText.trim()
                applyNaturalLanguageEventPrompt(submittedText, viewModel)
                viewModel?.sendConversationalRosaPrompt(submittedText)
                promptText = ""
                onSelectScreenCode("s29")
            } else {
                openRosaVoiceScreen(
                    viewModel = viewModel,
                    returnScreenCode = "s28",
                    onSelectScreenCode = onSelectScreenCode,
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .height(58.dp)
                .clip(CircleShape)
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onSelectScreenCode("s34") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF9B45C8), size = 18.dp)
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (promptText.isEmpty()) {
                    BasicText(
                        text = "Escríbele a Rosa o habla…",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = RfThemeState.currentPalette.muted,
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
                        color = RfThemeState.currentPalette.ink,
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

private data class RosaSuggestedArticleUi(
    val articleId: String,
    val title: String,
    val subtitle: String,
    val priceDop: Int,
    val photoKey: RfPhotoKey,
    val photoBg: Color,
)

private fun resolveSuggestionPhotoBackdrop(photo: PhotoAsset): Color = when (photo) {
    PhotoAsset.CENTRO_FLORAL -> Color(0xFFFFF2C9)
    PhotoAsset.MESA_HELADOS -> Color(0xFFEEF6D6)
    PhotoAsset.ARCO_ROSA -> Color(0xFFFDE6F2)
    PhotoAsset.SILLA_TIFFANY -> Color(0xFFEFE8FD)
    PhotoAsset.MESA_REDONDA -> Color(0xFFDDF4F3)
    PhotoAsset.MESA_PLEGABLE -> Color(0xFFFFF2C9)
    else -> Color(0xFFF3ECF9)
}

private fun buildSuggestionUiFromCatalogArticle(articleItem: CatalogArticle): RosaSuggestedArticleUi {
    val cleanSubtitle = articleItem.material.ifBlank { articleItem.category }
    return RosaSuggestedArticleUi(
        articleId = articleItem.id,
        title = articleItem.name,
        subtitle = cleanSubtitle,
        priceDop = articleItem.pricePerDayDop,
        photoKey = photoAssetToRfPhotoKey(articleItem.photo),
        photoBg = resolveSuggestionPhotoBackdrop(articleItem.photo),
    )
}

@Composable
internal fun RenderRosaSuggestionsS29(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val catalogArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value.orEmpty()
    val rosaProposalMessage = viewModel?.rosaProposalMessage?.collectAsState()?.value.orEmpty()
    val eventTypeInputValue = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val rosaEventTypeValue = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val selectedEventType = eventTypeInputValue.ifBlank { rosaEventTypeValue.ifBlank { "Boda" } }
    val eventMetadata = resolveRosaEventMetadata(selectedEventType)

    var suggestionBatch by remember { mutableStateOf(0) }
    var hasRequestedConversationalPrompt by remember { mutableStateOf(false) }
    var fallbackAddedIds by remember { mutableStateOf(setOf("art-centro-floral")) }
    var promptText by remember { mutableStateOf("") }

    val allCatalogSuggestions = remember(catalogArticles) {
        catalogArticles.map { articleItem -> buildSuggestionUiFromCatalogArticle(articleItem) }
    }

    val displayedSuggestions = remember(
        allCatalogSuggestions,
        draftQuantities,
        suggestionBatch,
        hasRequestedConversationalPrompt,
    ) {
        if (allCatalogSuggestions.isEmpty()) {
            emptyList()
        } else {
            val prioritizedFromDraft = if (hasRequestedConversationalPrompt) {
                allCatalogSuggestions.filter { suggestionItem ->
                    val currentQty = draftQuantities[suggestionItem.articleId] ?: 0
                    currentQty > 0
                }
            } else {
                emptyList()
            }
            val orderedPool = if (prioritizedFromDraft.isNotEmpty()) {
                val prioritizedIds = prioritizedFromDraft.map { item -> item.articleId }.toSet()
                val remaining = allCatalogSuggestions.filterNot { item -> item.articleId in prioritizedIds }
                prioritizedFromDraft + remaining
            } else {
                allCatalogSuggestions
            }
            val pageSize = 3.coerceAtMost(orderedPool.size)
            val startIndex = (suggestionBatch * pageSize) % orderedPool.size
            val selectedItems = mutableListOf<RosaSuggestedArticleUi>()
            for (offsetIndex in 0 until pageSize) {
                val resolvedIndex = (startIndex + offsetIndex) % orderedPool.size
                selectedItems.add(orderedPool[resolvedIndex])
            }
            selectedItems
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            step = 3,
            viewModel = viewModel,
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
                    text = "Para tu ${eventMetadata.displayTitle.lowercase()} te recomiendo estos artículos",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.52).sp,
                        lineHeight = 30.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )

                if (rosaProposalMessage.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface.copy(alpha = 0.92f))
                            .border(1.dp, Color(0xFFE7DEF5), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = rosaProposalMessage,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                }
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
                    val liveQuantity = draftQuantities[suggestion.articleId] ?: 0
                    val isAdded = if (viewModel != null) {
                        liveQuantity > 0
                    } else {
                        suggestion.articleId in fallbackAddedIds
                    }
                    Box(
                        modifier = Modifier
                            .width(208.dp)
                            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                            .clip(RoundedCornerShape(22.dp))
                            .background(RfThemeState.currentPalette.surface),
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
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                                )
                                BasicText(
                                    text = suggestion.subtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BasicText(
                                        text = "RD$ ${suggestion.priceDop}",
                                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = RfThemeState.currentPalette.ink),
                                    )
                                    if (isAdded) {
                                        Row(
                                            modifier = Modifier
                                                .height(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEEF6D6))
                                                .clickable {
                                                    fallbackAddedIds = fallbackAddedIds - suggestion.articleId
                                                    viewModel?.setDraftQuantity(suggestion.articleId, 0)
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
                                                    fallbackAddedIds = fallbackAddedIds + suggestion.articleId
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
                    .background(RfThemeState.currentPalette.surface)
                    .clickable { suggestionBatch += 1 },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Otras sugerencias",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
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
                val cleanPrompt = promptText.trim()
                applyNaturalLanguageEventPrompt(cleanPrompt, viewModel)
                viewModel?.sendConversationalRosaPrompt(cleanPrompt)
                hasRequestedConversationalPrompt = true
                suggestionBatch = 0
                promptText = ""
            } else {
                openRosaVoiceScreen(
                    viewModel = viewModel,
                    returnScreenCode = "s29",
                    onSelectScreenCode = onSelectScreenCode,
                )
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
                .background(RfThemeState.currentPalette.surface)
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
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                    BasicTextField(
                        value = promptText,
                        onValueChange = { updatedText -> promptText = updatedText },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitS29Prompt() }),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
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

private data class RosaCategoryReviewSpec(
    val title: String,
    val defaultSubtitle: String?,
    val iconKind: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
    val articleId: String?,
)

private fun resolveCategoryCatalogArticleId(
    categoryTitle: String,
    catalogArticles: List<CatalogArticle>,
): String? {
    val lowerTitle = categoryTitle.lowercase()
    return when {
        lowerTitle.contains("silla") -> {
            val matched = catalogArticles.firstOrNull { item -> item.id == "art-silla-tiffany" }
            matched?.id ?: catalogArticles.firstOrNull { item -> item.name.contains("silla", ignoreCase = true) }?.id
        }

        lowerTitle == "mesas" -> {
            val matched = catalogArticles.firstOrNull { item -> item.id == "art-mesa-redonda" }
            matched?.id ?: catalogArticles.firstOrNull { item -> item.name.contains("mesa redonda", ignoreCase = true) }?.id
        }

        lowerTitle.contains("apoyo") -> {
            catalogArticles.firstOrNull { item -> item.id == "art-mesa-plegable" }?.id
        }

        lowerTitle.contains("centro") -> {
            val matched = catalogArticles.firstOrNull { item -> item.id == "art-centro-floral" }
            matched?.id ?: catalogArticles.firstOrNull { item -> item.name.contains("centro", ignoreCase = true) }?.id
        }

        lowerTitle.contains("dulce") -> {
            val matched = catalogArticles.firstOrNull { item -> item.id == "art-mesa-helados" }
            matched?.id ?: catalogArticles.firstOrNull { item -> item.name.contains("helado", ignoreCase = true) }?.id
        }

        lowerTitle.contains("arco") -> {
            val matched = catalogArticles.firstOrNull { item -> item.id == "art-arco-rosa" }
            matched?.id ?: catalogArticles.firstOrNull { item -> item.name.contains("arco", ignoreCase = true) }?.id
        }

        lowerTitle.contains("ilumin") -> {
            catalogArticles.firstOrNull { item ->
                val matchesCategory = item.category.contains("ilumin", ignoreCase = true)
                val matchesName = listOf("ilumin", "luces", "guirnalda").any { keyword ->
                    item.name.contains(keyword, ignoreCase = true)
                }
                if (matchesCategory) true else matchesName
            }?.id
        }

        lowerTitle.contains("mantel") -> {
            catalogArticles.firstOrNull { item ->
                val matchesCategory = item.category.contains("mantel", ignoreCase = true)
                val matchesName = item.name.contains("mantel", ignoreCase = true)
                if (matchesCategory) true else matchesName
            }?.id
        }

        else -> null
    }
}

@Composable
internal fun RenderRosaCategoriesS30(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val catalogArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value.orEmpty()
    val vmOmittedCategories = viewModel?.rosaOmittedCategories?.collectAsState()?.value
    val rosaProposalMessage = viewModel?.rosaProposalMessage?.collectAsState()?.value.orEmpty()

    var manualChosenCategories by remember { mutableStateOf(setOf("Sillas", "Mesas")) }
    var localOmittedCategories by remember { mutableStateOf(emptySet<String>()) }
    var showExtraIdeas by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }

    val activeOmittedCategories = vmOmittedCategories ?: localOmittedCategories

    val baseCategories = remember(catalogArticles) {
        listOf(
            RosaCategoryReviewSpec(
                title = "Sillas",
                defaultSubtitle = null,
                iconKind = RfIconKind.STORE,
                iconBg = Color(0xFFFFF2C9),
                iconTint = Color(0xFF7A5700),
                articleId = resolveCategoryCatalogArticleId("Sillas", catalogArticles),
            ),
            RosaCategoryReviewSpec(
                title = "Mesas",
                defaultSubtitle = null,
                iconKind = RfIconKind.BOX,
                iconBg = Color(0xFFEEF6D6),
                iconTint = Color(0xFF4E6A10),
                articleId = resolveCategoryCatalogArticleId("Mesas", catalogArticles),
            ),
            RosaCategoryReviewSpec(
                title = "Iluminación",
                defaultSubtitle = null,
                iconKind = RfIconKind.LIGHTBULB,
                iconBg = Color(0xFFDDF4F3),
                iconTint = Color(0xFF1F8A84),
                articleId = resolveCategoryCatalogArticleId("Iluminación", catalogArticles),
            ),
            RosaCategoryReviewSpec(
                title = "Mantelería",
                defaultSubtitle = null,
                iconKind = RfIconKind.GRID,
                iconBg = Color(0xFFEFE8FD),
                iconTint = Color(0xFF7A4FD8),
                articleId = resolveCategoryCatalogArticleId("Mantelería", catalogArticles),
            ),
            RosaCategoryReviewSpec(
                title = "Centros de mesa",
                defaultSubtitle = null,
                iconKind = RfIconKind.SPARK,
                iconBg = Color(0xFFFDE6F2),
                iconTint = Color(0xFFC2338F),
                articleId = resolveCategoryCatalogArticleId("Centros de mesa", catalogArticles),
            ),
        )
    }
    val extraCategories = remember(catalogArticles) {
        listOf(
            RosaCategoryReviewSpec(
                title = "Estación de dulces",
                defaultSubtitle = null,
                iconKind = RfIconKind.CAKE,
                iconBg = Color(0xFFFFF2C9),
                iconTint = Color(0xFF7A5700),
                articleId = resolveCategoryCatalogArticleId("Estación de dulces", catalogArticles),
            ),
            RosaCategoryReviewSpec(
                title = "Arcos y globos",
                defaultSubtitle = null,
                iconKind = RfIconKind.PARTY,
                iconBg = Color(0xFFFDE6F2),
                iconTint = Color(0xFFC2338F),
                articleId = resolveCategoryCatalogArticleId("Arcos y globos", catalogArticles),
            ),
            RosaCategoryReviewSpec(
                title = "Mesas de apoyo",
                defaultSubtitle = null,
                iconKind = RfIconKind.TABLE,
                iconBg = Color(0xFFEEF6D6),
                iconTint = Color(0xFF4E6A10),
                articleId = resolveCategoryCatalogArticleId("Mesas de apoyo", catalogArticles),
            ),
        )
    }
    val displayedCategories = if (showExtraIdeas) baseCategories + extraCategories else baseCategories

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
            viewModel = viewModel,
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
                        color = RfThemeState.currentPalette.ink,
                        lineHeight = 28.sp,
                    ),
                )
                if (rosaProposalMessage.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface.copy(alpha = 0.92f))
                            .border(1.dp, Color(0xFFE7DEF5), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = rosaProposalMessage,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(vertical = 6.dp),
            ) {
                displayedCategories.forEachIndexed { index, category ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                    }
                    val matchedArticleId = category.articleId
                    val matchedQuantity = if (matchedArticleId != null) {
                        draftQuantities[matchedArticleId] ?: 0
                    } else {
                        0
                    }
                    val isOmitted = category.title in activeOmittedCategories
                    val hasDraftUnits = matchedQuantity > 0
                    val isManuallySelected = category.title in manualChosenCategories
                    val hasLiveArticleBinding = if (viewModel != null) matchedArticleId != null else false
                    val isIncludedBeforeOmit = if (hasLiveArticleBinding) hasDraftUnits else isManuallySelected
                    val isChosen = if (isOmitted) false else isIncludedBeforeOmit

                    val resolvedSubtitle = when {
                        isOmitted -> "Omitida"

                        isChosen -> {
                            if (matchedQuantity > 0) {
                                "$matchedQuantity en tu evento"
                            } else {
                                category.defaultSubtitle ?: "Incluida"
                            }
                        }

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
                            viewModel?.toggleRosaOmittedCategory(category.title)
                            localOmittedCategories = if (isOmitted) {
                                localOmittedCategories - category.title
                            } else {
                                localOmittedCategories + category.title
                            }
                            if (!isOmitted) {
                                manualChosenCategories = manualChosenCategories - category.title
                                if (matchedArticleId != null) {
                                    viewModel?.setDraftQuantity(matchedArticleId, 0)
                                }
                            }
                        },
                        onChoose = {
                            if (isOmitted) {
                                viewModel?.toggleRosaOmittedCategory(category.title)
                                localOmittedCategories = localOmittedCategories - category.title
                            }
                            manualChosenCategories = manualChosenCategories + category.title
                            if (matchedArticleId != null) {
                                val nextQuantity = if (matchedQuantity > 0) matchedQuantity else 1
                                viewModel?.setDraftQuantity(matchedArticleId, nextQuantity)
                            }
                        },
                        onToggleChosen = {
                            manualChosenCategories = manualChosenCategories - category.title
                            if (matchedArticleId != null) {
                                viewModel?.setDraftQuantity(matchedArticleId, 0)
                            }
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
                    .background(RfThemeState.currentPalette.surface)
                    .clickable {
                        val openingIdeas = !showExtraIdeas
                        showExtraIdeas = openingIdeas
                        if (openingIdeas) {
                            viewModel?.sendConversationalRosaPrompt("Sugiere alternativas respetando mis categorías")
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = if (showExtraIdeas) "Ver menos" else "Más ideas",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RfThemeState.currentPalette.ink,
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
                val cleanPrompt = promptText.trim()
                applyNaturalLanguageEventPrompt(cleanPrompt, viewModel)
                viewModel?.sendConversationalRosaPrompt(cleanPrompt)
                showExtraIdeas = true
                promptText = ""
            } else {
                openRosaVoiceScreen(
                    viewModel = viewModel,
                    returnScreenCode = "s30",
                    onSelectScreenCode = onSelectScreenCode,
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .height(58.dp)
                .clip(CircleShape)
                .background(RfThemeState.currentPalette.surface)
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
                            color = RfThemeState.currentPalette.muted,
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
                        color = RfThemeState.currentPalette.ink,
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
                    color = RfThemeState.currentPalette.ink,
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
                            color = RfThemeState.currentPalette.muted,
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

@Composable
internal fun RenderRosaDetailsS31(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val vmEventDateInput = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty()
    val vmEventVenueInput = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty()
    val vmEventGuestsInput = viewModel?.eventGuestsInput?.collectAsState()?.value.orEmpty()
    val vmRosaGuestsCount = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 0
    val vmRosaIdea = viewModel?.rosaProposalMessage?.collectAsState()?.value.orEmpty()

    val initialEventType = vmEventTypeInput.ifBlank { vmRosaEventType }
    val initialGuests = vmEventGuestsInput.ifBlank {
        if (vmRosaGuestsCount > 0) vmRosaGuestsCount.toString() else ""
    }

    var eventTypeValue by remember(initialEventType) { mutableStateOf(initialEventType) }
    var dateValue by remember(vmEventDateInput) { mutableStateOf(vmEventDateInput) }
    var showEventCalendar by remember { mutableStateOf(false) }
    var guestsValue by remember(initialGuests) { mutableStateOf(initialGuests) }
    var venueValue by remember(vmEventVenueInput) { mutableStateOf(vmEventVenueInput) }
    var ideaValue by remember(vmRosaIdea) { mutableStateOf(vmRosaIdea) }
    var promptText by remember { mutableStateOf("") }

    val eventTypeIcon = resolveRosaEventMetadata(eventTypeValue).iconKind

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            step = 5,
            viewModel = viewModel,
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
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = eventTypeIcon, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (eventTypeValue.isEmpty()) {
                            BasicText(
                                text = "Tipo de evento",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                        BasicTextField(
                            value = eventTypeValue,
                            onValueChange = { updatedType ->
                                eventTypeValue = updatedType
                                viewModel?.setEventTypeInput(updatedType)
                                if (updatedType.isNotBlank()) {
                                    viewModel?.updateRosaParameters(eventType = updatedType)
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clickable { showEventCalendar = true }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.CALENDAR, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                    RfMaskedDateField(
                        value = dateValue,
                        onValueChange = { updatedDate ->
                            dateValue = updatedDate
                            viewModel?.setEventDateInput(updatedDate)
                        },
                        onOpenCalendar = { showEventCalendar = true },
                        placeholder = "Fecha del evento (DD/MM/AAAA)",
                        isAdmin = false,
                        compactInline = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.USER, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (guestsValue.isEmpty()) {
                            BasicText(
                                text = "¿Cuántas personas?",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                        BasicTextField(
                            value = guestsValue,
                            onValueChange = { updatedGuests ->
                                val numericGuests = updatedGuests.filter { charItem -> charItem.isDigit() }
                                guestsValue = numericGuests
                                viewModel?.setEventGuestsInput(numericGuests)
                                val parsedGuestInt = numericGuests.toIntOrNull()
                                if (parsedGuestInt != null) {
                                    viewModel?.updateRosaParameters(guests = parsedGuestInt)
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.LOCATION, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (venueValue.isEmpty()) {
                            BasicText(
                                text = "¿Dónde será?",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                        BasicTextField(
                            value = venueValue,
                            onValueChange = { updatedVenue ->
                                venueValue = updatedVenue
                                viewModel?.setEventVenueInput(updatedVenue)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 58.dp)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopStart) {
                        if (ideaValue.isEmpty()) {
                            BasicText(
                                text = "Cuéntame tu idea (opcional)",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                        BasicTextField(
                            value = ideaValue,
                            onValueChange = { updatedIdea ->
                                ideaValue = updatedIdea
                                viewModel?.setRosaProposalMessage(updatedIdea)
                            },
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

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
                        .background(RfThemeState.currentPalette.surface),
                ) {
                    val gridColor = Color(0xFFEFE8FA)
                    val gridStep = size.width / 4f
                    for (gridLineIndex in 1..3) {
                        drawLine(gridColor, start = Offset(gridLineIndex * gridStep, 0f), end = Offset(gridLineIndex * gridStep, size.height), strokeWidth = 1.dp.toPx())
                        drawLine(gridColor, start = Offset(0f, gridLineIndex * gridStep), end = Offset(size.width, gridLineIndex * gridStep), strokeWidth = 1.dp.toPx())
                    }
                    val strokePx = 2.dp.toPx()
                    drawCircle(color = Color(0xFFEA4EA9), radius = 6.dp.toPx(), center = Offset(size.width * 0.3f, size.height * 0.32f), style = Stroke(strokePx))
                    drawCircle(color = Color(0xFFEA4EA9), radius = 6.dp.toPx(), center = Offset(size.width * 0.68f, size.height * 0.32f), style = Stroke(strokePx))
                    drawRoundRect(color = Color(0xFF865DEC), topLeft = Offset(size.width * 0.21f, size.height * 0.61f), size = Size(size.width * 0.57f, size.height * 0.18f), cornerRadius = CornerRadius(2.dp.toPx()), style = Stroke(strokePx))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText("Boceto de distribución", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink))
                    BasicText("Dibuja tu espacio y la IA te propone ejemplos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = RfThemeState.currentPalette.muted))
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
                    .background(RfThemeState.currentPalette.surface)
                    .clickable { onSelectScreenCode("s29") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Más ideas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink))
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
                val cleanPrompt = promptText.trim()
                val parsed = applyNaturalLanguageEventPrompt(cleanPrompt, viewModel)
                viewModel?.sendConversationalRosaPrompt(cleanPrompt)
                if (parsed.eventType != null) {
                    eventTypeValue = parsed.eventType
                }
                if (parsed.dateLabel != null) {
                    dateValue = parsed.dateLabel
                }
                if (parsed.guestsCount != null) {
                    val cleanGuests = parsed.guestsCount.toString()
                    guestsValue = cleanGuests
                    viewModel?.setEventGuestsInput(cleanGuests)
                }
                if (parsed.venueLabel != null) {
                    venueValue = parsed.venueLabel
                }
                ideaValue = cleanPrompt
                promptText = ""
            } else {
                openRosaVoiceScreen(
                    viewModel = viewModel,
                    returnScreenCode = "s31",
                    onSelectScreenCode = onSelectScreenCode,
                )
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
                .background(RfThemeState.currentPalette.surface)
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (promptText.isEmpty()) {
                    BasicText(
                        text = "O díctale los detalles a Rosa…",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
                BasicTextField(
                    value = promptText,
                    onValueChange = { updatedText -> promptText = updatedText },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitS31Prompt() }),
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
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
        if (showEventCalendar) {
            RfInteractiveCalendarModal(
                title = "Fecha del evento",
                currentDateText = dateValue,
                minYear = 2026,
                maxYear = 2030,
                defaultYear = 2026,
                isAdmin = false,
                onDismiss = { showEventCalendar = false },
                onSelectDate = { formattedDate ->
                    dateValue = formattedDate
                    viewModel?.setEventDateInput(formattedDate)
                    showEventCalendar = false
                },
            )
        }
    }
}

@Composable
internal fun RenderRosaSummaryS32(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val rawEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val rawRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val eventType = rawEventTypeInput.ifBlank { rawRosaEventType.ifBlank { "Boda" } }
    val eventMetadata = resolveRosaEventMetadata(eventType)
    val eventDate = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty().trim()
    val eventVenue = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty().trim()
    val rawGuestsInput = viewModel?.eventGuestsInput?.collectAsState()?.value.orEmpty().trim()
    val rosaGuestsCount = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 0
    val resolvedGuestsCount = rawGuestsInput.toIntOrNull() ?: rosaGuestsCount
    val observedDraftQuantities = viewModel?.draftQuantities?.collectAsState()?.value
    val formErrorMessage = viewModel?.formError?.collectAsState()?.value

    val totalArticlesCount = observedDraftQuantities
        ?.values
        ?.filter { unitCount -> unitCount > 0 }
        ?.sum()
        ?: viewModel?.totalDraftCartUnits()
        ?: 0

    val hasConfirmedDate = eventDate.isNotBlank()
    val hasConfirmedVenue = eventVenue.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            step = 6,
            totalSteps = 6,
            viewModel = viewModel,
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
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }

            if (formErrorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = formErrorMessage,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFFB71C1C),
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(vertical = 4.dp),
            ) {
                Column {
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
                            RfIcon(kind = eventMetadata.iconKind, tint = Color(0xFFC2338F), size = 18.dp)
                        }
                        BasicText(
                            text = "Evento",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            modifier = Modifier.weight(1f),
                        )
                        BasicText(
                            text = eventType,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

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
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            modifier = Modifier.weight(1f),
                        )
                        if (hasConfirmedDate) {
                            BasicText(
                                text = eventDate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
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

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

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
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            modifier = Modifier.weight(1f),
                        )
                        if (hasConfirmedVenue) {
                            BasicText(
                                text = eventVenue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
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

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

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
                                .background(Color(0xFFEFE8FD)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.USER, tint = Color(0xFF7A4FD8), size = 18.dp)
                        }
                        BasicText(
                            text = "Invitados",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            modifier = Modifier.weight(1f),
                        )
                        val guestsLabel = if (resolvedGuestsCount > 0) "$resolvedGuestsCount personas" else "Por definir"
                        BasicText(
                            text = guestsLabel,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

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
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                            modifier = Modifier.weight(1f),
                        )
                        BasicText(
                            text = "$totalArticlesCount",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        )
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 18.dp)
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
                        if (rawEventTypeInput.isBlank()) {
                            viewModel?.setEventTypeInput(eventType)
                        }
                        val hasNumericGuestsInput = rawGuestsInput.toIntOrNull() != null
                        if (!hasNumericGuestsInput) {
                            if (resolvedGuestsCount > 0) {
                                viewModel?.setEventGuestsInput(resolvedGuestsCount.toString())
                            }
                        }
                        val eventCreated = viewModel?.submitQuickClientEvent() ?: true
                        if (eventCreated) {
                            val hasDraftItems = viewModel?.draftLineItems()?.isNotEmpty() ?: false
                            if (hasDraftItems) {
                                viewModel.syncDraftToOfficialQuote()
                            }
                            onSelectScreenCode("s33")
                        }
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
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }
        }
    }
}

@Composable
internal fun RenderRosaSubmittedS33(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val observedEvents = viewModel?.events?.collectAsState()?.value
    val observedSelectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val observedQuotes = viewModel?.quotes?.collectAsState()?.value
    val observedSelectedQuoteId = viewModel?.selectedQuoteId?.collectAsState()?.value
    val fallbackEventType = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val fallbackEventDate = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty()
    val fallbackEventVenue = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty()
    val fallbackGuests = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 0

    val activeEvent = remember(observedEvents, observedSelectedEventId) {
        viewModel?.activeClientEventOrNull()
    }
    val activeQuote = remember(observedQuotes, observedSelectedQuoteId) {
        viewModel?.selectedQuote()
    }

    val summaryTitle = activeEvent?.title
        ?: activeQuote?.eventTitle
        ?: fallbackEventType.ifBlank { "Mi evento Rosa Fiesta" }
    val summaryDate = activeEvent?.dateLabel
        ?: activeQuote?.dateLabel
        ?: fallbackEventDate.ifBlank { "Por confirmar" }
    val summaryGuests = activeEvent?.guests
        ?: activeQuote?.guests
        ?: fallbackGuests
    val summaryVenue = activeEvent?.venue
        ?: activeQuote?.venue
        ?: fallbackEventVenue.ifBlank { "Santo Domingo" }
    val draftEstimatedSubtotal = viewModel?.draftLineItems()?.sumOf { lineItem -> lineItem.subtotalDop } ?: 0
    val summaryTotalDop = activeQuote?.totalDop
        ?: activeEvent?.totalDop
        ?: draftEstimatedSubtotal

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
                    .background(RfThemeState.currentPalette.surface)
                    .clickable { onSelectScreenCode("BACK:s14") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 68.dp, bottom = 92.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Canvas(modifier = Modifier.size(156.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val sc = size.width / 120f

                    drawCircle(color = Color(0xFFDDF5EC), radius = 36f * sc, center = Offset(cx, cy))
                    drawCircle(color = Color(0xFF2FB58F), radius = 26f * sc, center = Offset(cx, cy))
                    val check = Path().apply {
                        moveTo((48f - 60f) * sc + cx, (61f - 60f) * sc + cy)
                        lineTo((56f - 60f) * sc + cx, (69f - 60f) * sc + cy)
                        lineTo((72f - 60f) * sc + cx, (52f - 60f) * sc + cy)
                    }
                    drawPath(check, Color.White, style = Stroke(6f * sc, cap = StrokeCap.Round, join = StrokeJoin.Round))

                    withTransform({
                        translate((59.5f - 60f) * sc + cx, (12f - 60f) * sc + cy)
                        rotate(20f)
                    }) {
                        drawRoundRect(Color(0xFFEC4F97), topLeft = Offset(-3.5f * sc, -6f * sc), size = Size(7f * sc, 12f * sc), cornerRadius = CornerRadius(2f * sc))
                    }
                    drawCircle(Color(0xFFF5B53B), radius = 4f * sc, center = Offset((98f - 60f) * sc + cx, (28f - 60f) * sc + cy))
                    val tri1 = Path().apply {
                        moveTo((104f - 60f) * sc + cx, (66f - 60f) * sc + cy)
                        lineTo((114f - 60f) * sc + cx, (71f - 60f) * sc + cy)
                        lineTo((104f - 60f) * sc + cx, (76f - 60f) * sc + cy)
                        close()
                    }
                    drawPath(tri1, Color(0xFF7650E8))
                    withTransform({
                        translate((89.5f - 60f) * sc + cx, (102f - 60f) * sc + cy)
                        rotate(-30f)
                    }) {
                        drawRoundRect(Color(0xFF4AA8E0), topLeft = Offset(-3.5f * sc, -6f * sc), size = Size(7f * sc, 12f * sc), cornerRadius = CornerRadius(2f * sc))
                    }
                    drawCircle(Color(0xFFEC4F97), radius = 4f * sc, center = Offset((38f - 60f) * sc + cx, (106f - 60f) * sc + cy))
                    val tri2 = Path().apply {
                        moveTo((16f - 60f) * sc + cx, (72f - 60f) * sc + cy)
                        lineTo((6f - 60f) * sc + cx, (68f - 60f) * sc + cy)
                        lineTo((14f - 60f) * sc + cx, (61f - 60f) * sc + cy)
                        close()
                    }
                    drawPath(tri2, Color(0xFFF5B53B))
                    withTransform({
                        translate((21.5f - 60f) * sc + cx, (28f - 60f) * sc + cy)
                        rotate(-35f)
                    }) {
                        drawRoundRect(Color(0xFF7650E8), topLeft = Offset(-3.5f * sc, -6f * sc), size = Size(7f * sc, 12f * sc), cornerRadius = CornerRadius(2f * sc))
                    }
                    drawCircle(Color(0xFF2FB58F), radius = 2.5f * sc, center = Offset((80f - 60f) * sc + cx, (14f - 60f) * sc + cy))
                    drawCircle(Color(0xFF4AA8E0), radius = 2.5f * sc, center = Offset((10f - 60f) * sc + cx, (44f - 60f) * sc + cy))
                    drawCircle(Color(0xFFEC4F97), radius = 2.5f * sc, center = Offset((110f - 60f) * sc + cx, (46f - 60f) * sc + cy))
                }

                Spacer(modifier = Modifier.height(8.dp))

                BasicText(
                    text = "¡Solicitud enviada!",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = (28 * 1.05f).sp,
                        color = RfThemeState.currentPalette.ink,
                        textAlign = TextAlign.Center,
                    ),
                )
                Spacer(modifier = Modifier.height(6.dp))
                BasicText(
                    text = "Gracias por confiar en nosotros. Te avisamos por correo y WhatsApp.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = RfThemeState.currentPalette.muted,
                        textAlign = TextAlign.Center,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        BasicText(
                            text = summaryTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        val guestsSegment = if (summaryGuests > 0) "$summaryGuests inv." else "Invitados por definir"
                        BasicText(
                            text = "$summaryDate · $guestsSegment",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                        BasicText(
                            text = summaryVenue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        BasicText(
                            text = "Estimado",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 11.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                        BasicText(
                            text = formatDopCurrency(summaryTotalDop),
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color(0xFF7A4FD8),
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(RfThemeState.currentPalette.surface)
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
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )

                    val steps = listOf(
                        Triple("1", "Revisamos tu evento", "El equipo de Rosa Fiesta ya lo tiene"),
                        Triple("2", "Recibes tu cotización", "Por correo y WhatsApp"),
                        Triple("3", "Apruebas y reservas", "Desde la app, en un toque"),
                    )

                    steps.forEach { stepTriple ->
                        val stepNumber = stepTriple.first
                        val stepTitle = stepTriple.second
                        val stepDesc = stepTriple.third
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            if (stepNumber == "1") {
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
                                            text = stepNumber,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White),
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(RfThemeState.currentPalette.chipBg),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = stepNumber,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                            }
                            Column {
                                BasicText(text = stepTitle, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, fontSize = 15.sp))
                                BasicText(text = stepDesc, style = TextStyle(fontFamily = typo.bodyFamily, color = RfThemeState.currentPalette.muted, fontSize = 13.sp))
                            }
                        }
                    }
                }
            }
        }

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
                    .clickable { onSelectScreenCode("s40") },
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
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                )
            }
        }
    }
}

@Composable
internal fun RenderRosaChatS34(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val initialProposalMessage = viewModel?.rosaProposalMessage?.collectAsState()?.value.orEmpty()
    val pickedAttachment = viewModel?.pickedAttachmentFile?.collectAsState()?.value
    var chatMessageText by remember(initialProposalMessage) {
        mutableStateOf(initialProposalMessage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RenderRosaEventTypeS26(onSelectScreenCode = onSelectScreenCode, viewModel = viewModel)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable { onSelectScreenCode("BACK:s28") },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(328.dp)
                .shadow(24.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), spotColor = Color(0x73AA46A0))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.surface)
                .clickable(enabled = true, onClick = {})
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
                                        viewModel?.updateRosaParameters(guests = 120)
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

                if (pickedAttachment != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFE8FD))
                            .clickable { viewModel.clearPickedAttachment() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFF7A4FD8), size = 14.dp)
                        BasicText(
                            text = "${pickedAttachment.fileName} (toca para quitar)",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF7A4FD8),
                            ),
                        )
                    }
                }

                val submitChatPrompt = {
                    val trimmedMessage = chatMessageText.trim()
                    val attachmentLabel = pickedAttachment?.fileName.orEmpty().trim()
                    val combinedPrompt = when {
                        trimmedMessage.isNotEmpty() -> {
                            if (attachmentLabel.isNotEmpty()) {
                                "$trimmedMessage (Referencia adjunta: $attachmentLabel)"
                            } else {
                                trimmedMessage
                            }
                        }

                        attachmentLabel.isNotEmpty() -> "Referencia visual adjunta: $attachmentLabel"

                        else -> ""
                    }
                    if (combinedPrompt.isNotEmpty()) {
                        applyNaturalLanguageEventPrompt(combinedPrompt, viewModel)
                        viewModel?.sendConversationalRosaPrompt(combinedPrompt)
                    }
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
                            color = RfThemeState.currentPalette.ink,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val hasAttachment = pickedAttachment != null
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (hasAttachment) Color(0xFFFDE6F2) else Color.Transparent)
                            .clickable {
                                if (hasAttachment) {
                                    viewModel.clearPickedAttachment()
                                } else {
                                    viewModel?.pickMediaAttachment()
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = RfIconKind.SHARE,
                            tint = if (hasAttachment) Color(0xFFC2338F) else Color(0xFF62627A),
                            size = 22.dp,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                openRosaVoiceScreen(
                                    viewModel = viewModel,
                                    returnScreenCode = "s34",
                                    onSelectScreenCode = onSelectScreenCode,
                                    initialText = chatMessageText,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.MIC, tint = RfThemeState.currentPalette.muted, size = 22.dp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable {
                                chatMessageText = ""
                                viewModel?.clearPickedAttachment()
                                onSelectScreenCode("BACK:s28")
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
                                color = RfThemeState.currentPalette.muted,
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

@Composable
internal fun RenderRosaVoiceS35(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.ROSA_WIZARD
    val isSearchMode = voiceOrigin == VoiceInputOrigin.SEARCH
    val vmListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val vmTranscribing = viewModel?.isVoiceTranscribing?.collectAsState()?.value ?: false
    val liveAmplitude = viewModel?.voiceAmplitude?.collectAsState()?.value ?: 0f
    val hasHardwareMic = viewModel?.hasHardwareMicrophone?.collectAsState()?.value ?: false
    val vmTranscript = viewModel?.voiceTranscript?.collectAsState()?.value.orEmpty()
    val vmVoiceError = viewModel?.voiceErrorMessage?.collectAsState()?.value
    val vmReturnScreenCode = viewModel?.voiceReturnScreenCode?.collectAsState()?.value.orEmpty()

    var editableTranscript by remember(vmTranscript) {
        mutableStateOf(vmTranscript)
    }

    LaunchedEffect(Unit) {
        val isUnsetOrigin = voiceOrigin == VoiceInputOrigin.NONE
        val resolvedOrigin = if (isUnsetOrigin) VoiceInputOrigin.ROSA_WIZARD else voiceOrigin
        if (!vmListening) {
            viewModel?.startVoiceInput(resolvedOrigin, editableTranscript)
        }
    }

    val resolvedReturnCode = remember(vmReturnScreenCode, isSearchMode) {
        val cleanReturn = vmReturnScreenCode.trim()
        if (isSearchMode) {
            val isDefaultWizardReturn = cleanReturn == "s28"
            val useSearchDefault = if (cleanReturn.isEmpty()) true else isDefaultWizardReturn
            if (useSearchDefault) "s80" else cleanReturn
        } else {
            cleanReturn.ifBlank { "s28" }
        }
    }

    val confirmVoiceAction = {
        viewModel?.stopVoiceInput()
        val finalTranscript = editableTranscript.trim()
        if (isSearchMode) {
            viewModel?.setSearchQuery(finalTranscript)
            onSelectScreenCode(resolvedReturnCode)
        } else {
            if (finalTranscript.isNotEmpty()) {
                applyNaturalLanguageEventPrompt(finalTranscript, viewModel)
                viewModel?.sendConversationalRosaPrompt(finalTranscript)
            }
            onSelectScreenCode(resolvedReturnCode)
        }
    }

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
                    .background(RfThemeState.currentPalette.surface)
                    .clickable {
                        viewModel?.stopVoiceInput()
                        onSelectScreenCode("BACK:$resolvedReturnCode")
                    },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }
            BasicText(
                text = if (isSearchMode) "Búsqueda por voz · Catálogo" else "Rosa IA · Voz en vivo",
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
            val pulseScale = (liveAmplitude.coerceIn(0f, 1f) * 24f).dp
            Box(
                modifier = Modifier
                    .size(232.dp + pulseScale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    RosaAvatar(
                        size = RosaAvatarSize.LARGE,
                        customSize = 120.dp,
                        forceAdminPalette = false,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val statusDotColor = when {
                        vmTranscribing -> Color(0xFF865DEC)
                        vmListening -> Color(0xFFEA4EA9)
                        else -> Color(0xFF2FB58F)
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusDotColor),
                    )
                    val statusLabel = when {
                        vmTranscribing -> "Procesando audio…"

                        vmListening -> {
                            if (hasHardwareMic) "Te escucho (micrófono activo)…" else "Escucha activa · escribe o dicta"
                        }

                        editableTranscript.isNotBlank() -> "Transcripción lista para confirmar"

                        hasHardwareMic -> "Toca reintentar para hablar o edita abajo"

                        else -> "Micrófono no detectado · escribe tu solicitud"
                    }
                    BasicText(
                        text = statusLabel,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                    )
                }

                if (vmVoiceError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF2C9))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        BasicText(
                            text = vmVoiceError,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF7A5700),
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(1.dp, Color(0xFFE7DEF5), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (editableTranscript.isEmpty()) {
                        val placeholderText = if (isSearchMode) {
                            "Di el nombre del artículo o escríbelo aquí…"
                        } else {
                            "Habla ahora o refina tu transcripción aquí…"
                        }
                        BasicText(
                            text = placeholderText,
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                lineHeight = 25.sp,
                                color = RfThemeState.currentPalette.muted,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                    BasicTextField(
                        value = editableTranscript,
                        onValueChange = { updatedText ->
                            editableTranscript = updatedText
                            viewModel?.setVoiceTranscript(updatedText)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { confirmVoiceAction() }),
                        textStyle = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            lineHeight = 27.sp,
                            color = RfThemeState.currentPalette.ink,
                            textAlign = TextAlign.Center,
                        ),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

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
                bars.forEachIndexed { index, barPair ->
                    val barHeight = barPair.first
                    val barColor = barPair.second
                    val waveActive = if (vmListening) true else vmTranscribing
                    val waveFactor = if (waveActive) {
                        val offsetWave = sin((index * 0.7) + (liveAmplitude * 6.0)).toFloat()
                        (0.55f + (liveAmplitude * 0.9f) + (offsetWave * 0.25f)).coerceIn(0.30f, 1.55f)
                    } else {
                        0.38f
                    }
                    val adjustedHeight = (barHeight.value * waveFactor).coerceIn(8f, 72f).dp
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(adjustedHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor),
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
                    .background(RfThemeState.currentPalette.surface)
                    .clickable {
                        viewModel?.stopVoiceInput()
                        val targetKeyboardScreen = if (isSearchMode) "s80" else "s34"
                        onSelectScreenCode(targetKeyboardScreen)
                    },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.GRID, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF9646C8), ambientColor = Color(0x4D9646C8))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                    .clickable { confirmVoiceAction() },
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
                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 24.dp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .clickable {
                        viewModel?.stopVoiceInput()
                        editableTranscript = ""
                        val nextOrigin = if (isSearchMode) VoiceInputOrigin.SEARCH else VoiceInputOrigin.ROSA_WIZARD
                        viewModel?.startVoiceInput(nextOrigin, "")
                    },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.REFRESH, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }
        }
    }
}

@Composable
internal fun InlineVoiceSearchBanner(
    viewModel: RosaFiestaViewModel?,
    onApplyVoiceQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val isListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val isTranscribing = viewModel?.isVoiceTranscribing?.collectAsState()?.value ?: false
    val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
    val voiceTranscript = viewModel?.voiceTranscript?.collectAsState()?.value.orEmpty()
    val voiceError = viewModel?.voiceErrorMessage?.collectAsState()?.value
    val isSearchOrigin = voiceOrigin == VoiceInputOrigin.SEARCH
    val isVoiceBusy = listOf(isListening, isTranscribing).any { active -> active }
    val shouldShowBanner = if (isVoiceBusy) isSearchOrigin else false
    if (!shouldShowBanner) {
        return
    }
    val liveAmplitude = viewModel?.voiceAmplitude?.collectAsState()?.value ?: 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
            .clip(RoundedCornerShape(18.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.5.dp, Color(0xFFEA4EA9), RoundedCornerShape(18.dp))
            .padding(14.dp),
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
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isTranscribing) Color(0xFF865DEC) else Color(0xFFEA4EA9)),
                )
                val statusText = when {
                    isTranscribing -> "Reconociendo tu voz…"
                    voiceError != null -> voiceError
                    voiceTranscript.isNotBlank() -> "“$voiceTranscript”"
                    else -> "Escuchando… habla para buscar en el catálogo"
                }
                BasicText(
                    text = statusText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.height(22.dp),
            ) {
                val baseHeights = listOf(8f, 14f, 20f, 12f, 22f, 16f, 10f)
                baseHeights.forEachIndexed { index, baseDp ->
                    val waveDelta = sin((index * 0.8) + (liveAmplitude * 6.0)).toFloat()
                    val scaleFactor = (0.55f + (liveAmplitude * 0.85f) + (waveDelta * 0.25f)).coerceIn(0.35f, 1.35f)
                    val barHeight = (baseDp * scaleFactor).coerceIn(6f, 22f).dp
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (index % 2 == 0) Color(0xFFEA4EA9) else Color(0xFF865DEC)),
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFDE6F2))
                    .clickable {
                        viewModel?.stopVoiceInput()
                        if (voiceTranscript.isNotBlank()) {
                            onApplyVoiceQuery(voiceTranscript.trim())
                        }
                    }
                    .padding(horizontal = 14.dp),
            ) {
                val actionText = if (voiceTranscript.isNotBlank()) "Buscar “$voiceTranscript”" else "Detener micrófono"
                BasicText(
                    text = actionText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFFC2338F),
                    ),
                )
            }
        }
    }
}

private enum class SketchElementKind {
    NONE,
    ROUND_TABLE,
    RECT_TABLE,
    CHAIR_GROUP,
    ARCH_STAGE,
    TEXT_LABEL,
}

private data class SketchCanvasElement(
    val id: String,
    val kind: SketchElementKind,
    val normX: Float,
    val normY: Float,
    val colorHex: Long,
    val label: String = "",
)

private fun buildDefaultSketchElements(tableCount: Int): List<SketchCanvasElement> {
    val clampedTables = tableCount.coerceIn(2, 12)
    val elements = mutableListOf(
        SketchCanvasElement(
            id = "arch-1",
            kind = SketchElementKind.ARCH_STAGE,
            normX = 0.50f,
            normY = 0.12f,
            colorHex = 0xFF865DEC,
            label = "Tarima",
        ),
        SketchCanvasElement(
            id = "lbl-dance",
            kind = SketchElementKind.TEXT_LABEL,
            normX = 0.50f,
            normY = 0.68f,
            colorHex = 0xFF8DB52C,
            label = "Pista de baile",
        ),
    )
    val candidatePositions = listOf(
        Pair(0.22f, 0.36f),
        Pair(0.78f, 0.36f),
        Pair(0.22f, 0.58f),
        Pair(0.78f, 0.58f),
        Pair(0.22f, 0.82f),
        Pair(0.78f, 0.82f),
        Pair(0.42f, 0.36f),
        Pair(0.58f, 0.36f),
        Pair(0.38f, 0.84f),
        Pair(0.62f, 0.84f),
        Pair(0.50f, 0.46f),
        Pair(0.50f, 0.26f),
    )
    for (tableIndex in 0 until clampedTables) {
        val posPair = candidatePositions[tableIndex % candidatePositions.size]
        elements.add(
            SketchCanvasElement(
                id = "round-${tableIndex + 1}",
                kind = SketchElementKind.ROUND_TABLE,
                normX = posPair.first,
                normY = posPair.second,
                colorHex = 0xFFEA4EA9,
                label = "M${tableIndex + 1}",
            ),
        )
    }
    return elements
}

private fun buildAiBalancedSketchLayout(guestCount: Int): List<SketchCanvasElement> {
    val safeGuests = guestCount.coerceAtLeast(16)
    val targetTables = (safeGuests / 8).coerceIn(2, 12)
    val generated = mutableListOf(
        SketchCanvasElement(
            id = "ai-arch",
            kind = SketchElementKind.ARCH_STAGE,
            normX = 0.50f,
            normY = 0.12f,
            colorHex = 0xFF865DEC,
            label = "Arco y tarima principal",
        ),
        SketchCanvasElement(
            id = "ai-dance",
            kind = SketchElementKind.TEXT_LABEL,
            normX = 0.50f,
            normY = 0.60f,
            colorHex = 0xFF8DB52C,
            label = "Pista central",
        ),
        SketchCanvasElement(
            id = "ai-entry",
            kind = SketchElementKind.TEXT_LABEL,
            normX = 0.50f,
            normY = 0.91f,
            colorHex = 0xFF1A1A2E,
            label = "Entrada",
        ),
    )
    val leftColumnCount = (targetTables + 1) / 2
    val rightColumnCount = targetTables - leftColumnCount
    for (leftIdx in 0 until leftColumnCount) {
        val denominator = (leftColumnCount + 1).coerceAtLeast(2)
        val posY = (0.28f + (leftIdx + 1) * (0.58f / denominator)).coerceIn(0.25f, 0.85f)
        generated.add(
            SketchCanvasElement(
                id = "ai-tbl-L$leftIdx",
                kind = SketchElementKind.ROUND_TABLE,
                normX = 0.23f,
                normY = posY,
                colorHex = 0xFFEA4EA9,
                label = "M${leftIdx + 1}",
            ),
        )
    }
    for (rightIdx in 0 until rightColumnCount) {
        val denominator = (rightColumnCount + 1).coerceAtLeast(2)
        val posY = (0.28f + (rightIdx + 1) * (0.58f / denominator)).coerceIn(0.25f, 0.85f)
        generated.add(
            SketchCanvasElement(
                id = "ai-tbl-R$rightIdx",
                kind = SketchElementKind.ROUND_TABLE,
                normX = 0.77f,
                normY = posY,
                colorHex = 0xFFEA4EA9,
                label = "M${leftColumnCount + rightIdx + 1}",
            ),
        )
    }
    generated.add(
        SketchCanvasElement(
            id = "ai-chairs-vip",
            kind = SketchElementKind.CHAIR_GROUP,
            normX = 0.50f,
            normY = 0.28f,
            colorHex = 0xFF1FBDC0,
            label = "Sillas ceremonia",
        ),
    )
    return generated
}

private fun sanitizeSketchToken(rawValue: String): String = rawValue
    .replace("|", " ")
    .replace(";", " ")
    .replace("\n", " ")
    .trim()

private fun serializeSketchElements(elements: List<SketchCanvasElement>): String = elements.joinToString(";") { item ->
    val safeId = sanitizeSketchToken(item.id)
    val safeLabel = sanitizeSketchToken(item.label)
    val roundedX = ((item.normX * 1000f).toInt() / 1000f)
    val roundedY = ((item.normY * 1000f).toInt() / 1000f)
    "$safeId|${item.kind.name}|$roundedX|$roundedY|${item.colorHex}|$safeLabel"
}

private fun parseSketchElements(
    rawSerialized: String,
    fallbackTableCount: Int,
): List<SketchCanvasElement> {
    val trimmed = rawSerialized.trim()
    if (trimmed.isEmpty()) {
        return buildDefaultSketchElements(fallbackTableCount)
    }
    val records = trimmed.split(";").filter { token -> token.isNotBlank() }
    val parsed = mutableListOf<SketchCanvasElement>()
    val maxRecords = records.size.coerceAtMost(80)
    for (recordIndex in 0 until maxRecords) {
        val parts = records[recordIndex].split("|")
        if (parts.size >= 5) {
            val resolvedKind = SketchElementKind.entries.firstOrNull { candidate ->
                candidate.name == parts[1].trim()
            } ?: SketchElementKind.NONE
            if (resolvedKind != SketchElementKind.NONE) {
                val parsedX = (parts[2].toFloatOrNull() ?: 0.5f).coerceIn(0.08f, 0.92f)
                val parsedY = (parts[3].toFloatOrNull() ?: 0.5f).coerceIn(0.08f, 0.92f)
                val parsedColor = parts[4].toLongOrNull() ?: 0xFFEA4EA9
                val parsedLabel = if (parts.size >= 6) parts[5] else ""
                parsed.add(
                    SketchCanvasElement(
                        id = parts[0].ifBlank { "el-$recordIndex" },
                        kind = resolvedKind,
                        normX = parsedX,
                        normY = parsedY,
                        colorHex = parsedColor,
                        label = parsedLabel,
                    ),
                )
            }
        }
    }
    return if (parsed.isEmpty()) buildDefaultSketchElements(fallbackTableCount) else parsed
}

private fun syncSketchElementsIdempotently(
    elements: List<SketchCanvasElement>,
    viewModel: RosaFiestaViewModel?,
) {
    val roundTableCount = elements.count { element -> element.kind == SketchElementKind.ROUND_TABLE }
    val rectTableCount = elements.count { element -> element.kind == SketchElementKind.RECT_TABLE }
    val chairGroupCount = elements.count { element -> element.kind == SketchElementKind.CHAIR_GROUP }
    val archStageCount = elements.count { element -> element.kind == SketchElementKind.ARCH_STAGE }
    val totalTables = roundTableCount + rectTableCount
    val computedChairs = (roundTableCount * 8) + (rectTableCount * 10) + (chairGroupCount * 6)
    val serialized = serializeSketchElements(elements)

    viewModel?.setSketchTableCount(totalTables.coerceIn(2, 12))
    viewModel?.setDraftQuantity("art-mesa-redonda", roundTableCount)
    viewModel?.setDraftQuantity("art-mesa-plegable", rectTableCount)
    viewModel?.setDraftQuantity("art-silla-tiffany", computedChairs)
    if (archStageCount > 0) {
        viewModel?.setDraftQuantity("art-arco-rosa", archStageCount)
    }
    viewModel?.saveSketchLayoutJson(serialized)
}

@Composable
private fun SketchCanvasPreviewSurface(
    elements: List<SketchCanvasElement>,
    isThreeDimensionalPreview: Boolean,
    modifier: Modifier = Modifier,
    onCanvasTap: (() -> Unit)? = null,
) {
    val typo = LocalRfTypography.current
    val clickableModifier = if (onCanvasTap != null) {
        Modifier.clickable { onCanvasTap() }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(clickableModifier),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val gridStep = (canvasW / 12f).coerceAtLeast(14f)
            val maxCols = (canvasW / gridStep).toInt().coerceAtMost(24)
            val maxRows = (canvasH / gridStep).toInt().coerceAtMost(30)

            for (colIdx in 1..maxCols) {
                val lineX = colIdx * gridStep
                if (lineX < canvasW) {
                    drawLine(Color(0xFFF3ECF4), Offset(lineX, 0f), Offset(lineX, canvasH), 1f)
                }
            }
            for (rowIdx in 1..maxRows) {
                val lineY = rowIdx * gridStep
                if (lineY < canvasH) {
                    drawLine(Color(0xFFF3ECF4), Offset(0f, lineY), Offset(canvasW, lineY), 1f)
                }
            }

            elements.forEach { element ->
                val centerX = element.normX * canvasW
                val centerY = element.normY * canvasH
                val itemColor = Color(element.colorHex)
                val elevationOffset = if (isThreeDimensionalPreview) 5.dp.toPx() else 0f

                when (element.kind) {
                    SketchElementKind.NONE -> Unit

                    SketchElementKind.ARCH_STAGE -> {
                        val stageW = canvasW * 0.62f
                        val stageH = canvasH * 0.11f
                        val topLeft = Offset(centerX - stageW / 2f, centerY - stageH / 2f)
                        if (isThreeDimensionalPreview) {
                            drawRoundRect(
                                color = itemColor.copy(alpha = 0.22f),
                                topLeft = Offset(topLeft.x + elevationOffset, topLeft.y + elevationOffset),
                                size = Size(stageW, stageH),
                                cornerRadius = CornerRadius(8.dp.toPx()),
                            )
                        }
                        drawRoundRect(
                            color = itemColor.copy(alpha = 0.12f),
                            topLeft = topLeft,
                            size = Size(stageW, stageH),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                        )
                        drawRoundRect(
                            color = itemColor,
                            topLeft = topLeft,
                            size = Size(stageW, stageH),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(width = 2.8.dp.toPx()),
                        )
                    }

                    SketchElementKind.ROUND_TABLE -> {
                        val radius = (canvasW * 0.068f).coerceIn(14f, 30f)
                        if (isThreeDimensionalPreview) {
                            drawCircle(
                                color = itemColor.copy(alpha = 0.22f),
                                radius = radius,
                                center = Offset(centerX + elevationOffset, centerY + elevationOffset),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = radius,
                                center = Offset(centerX, centerY),
                            )
                        }
                        drawCircle(
                            color = itemColor,
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2.8.dp.toPx()),
                        )
                        val chairDistance = radius + 5.dp.toPx()
                        for (seatIdx in 0 until 6) {
                            val angleRad = (seatIdx * 60.0) * PI / 180.0
                            val seatX = centerX + (chairDistance * cos(angleRad)).toFloat()
                            val seatY = centerY + (chairDistance * sin(angleRad)).toFloat()
                            drawCircle(
                                color = itemColor.copy(alpha = 0.55f),
                                radius = 2.8.dp.toPx(),
                                center = Offset(seatX, seatY),
                            )
                        }
                    }

                    SketchElementKind.RECT_TABLE -> {
                        val rectW = (canvasW * 0.20f).coerceAtLeast(36f)
                        val rectH = (canvasH * 0.068f).coerceAtLeast(18f)
                        val topLeft = Offset(centerX - rectW / 2f, centerY - rectH / 2f)
                        if (isThreeDimensionalPreview) {
                            drawRoundRect(
                                color = itemColor.copy(alpha = 0.24f),
                                topLeft = Offset(topLeft.x + elevationOffset, topLeft.y + elevationOffset),
                                size = Size(rectW, rectH),
                                cornerRadius = CornerRadius(5.dp.toPx()),
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = topLeft,
                                size = Size(rectW, rectH),
                                cornerRadius = CornerRadius(5.dp.toPx()),
                            )
                        }
                        drawRoundRect(
                            color = itemColor,
                            topLeft = topLeft,
                            size = Size(rectW, rectH),
                            cornerRadius = CornerRadius(5.dp.toPx()),
                            style = Stroke(width = 2.8.dp.toPx()),
                        )
                        for (seatCol in 0 until 4) {
                            val seatX = topLeft.x + (rectW * (seatCol + 0.5f) / 4f)
                            drawCircle(itemColor.copy(alpha = 0.55f), radius = 2.5.dp.toPx(), center = Offset(seatX, topLeft.y - 4.dp.toPx()))
                            drawCircle(itemColor.copy(alpha = 0.55f), radius = 2.5.dp.toPx(), center = Offset(seatX, topLeft.y + rectH + 4.dp.toPx()))
                        }
                    }

                    SketchElementKind.CHAIR_GROUP -> {
                        val totalRowWidth = (canvasW * 0.22f).coerceAtLeast(42f)
                        val startX = centerX - totalRowWidth / 2f
                        val stepChair = totalRowWidth / 5f
                        for (chairIdx in 0 until 6) {
                            val chairX = startX + chairIdx * stepChair
                            drawRoundRect(
                                color = itemColor,
                                topLeft = Offset(chairX - 4.dp.toPx(), centerY - 4.dp.toPx()),
                                size = Size(8.dp.toPx(), 8.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }

                    SketchElementKind.TEXT_LABEL -> {
                        val zoneW = (canvasW * 0.26f).coerceAtLeast(52f)
                        val zoneH = (canvasH * 0.09f).coerceAtLeast(22f)
                        drawRoundRect(
                            color = itemColor,
                            topLeft = Offset(centerX - zoneW / 2f, centerY - zoneH / 2f),
                            size = Size(zoneW, zoneH),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
                            ),
                        )
                    }
                }
            }
        }

        val labelElements = elements.filter { item ->
            val isExplicitLabel = item.kind == SketchElementKind.TEXT_LABEL
            val isStageWithLabel = if (item.kind == SketchElementKind.ARCH_STAGE) item.label.isNotBlank() else false
            if (isExplicitLabel) true else isStageWithLabel
        }
        labelElements.forEach { labelElement ->
            val verticalBiasOffset = ((labelElement.normY - 0.5f) * 200f).dp
            val horizontalBiasOffset = ((labelElement.normX - 0.5f) * 160f).dp
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = horizontalBiasOffset, y = verticalBiasOffset)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.90f))
                    .border(1.dp, Color(labelElement.colorHex).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                BasicText(
                    text = labelElement.label.ifBlank { "Zona" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }
        }
    }
}

@Composable
internal fun RenderRosaSketchS36(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val swatchColors = remember {
        listOf(
            Pair(Color(0xFFEA4EA9), 0xFFEA4EA9L),
            Pair(Color(0xFF865DEC), 0xFF865DECL),
            Pair(Color(0xFF8DB52C), 0xFF8DB52CL),
            Pair(Color(0xFFF7B500), 0xFFF7B500L),
            Pair(Color(0xFF1FBDC0), 0xFF1FBDC0L),
            Pair(Color(0xFF1A1A2E), 0xFF1A1A2EL),
        )
    }
    var selectedSwatchIndex by remember { mutableStateOf(0) }
    var selectedToolIndex by remember { mutableStateOf(2) }
    val vmTableCount = viewModel?.sketchTableCount?.collectAsState()?.value ?: 6
    val vmSketchJson = viewModel?.sketchLayoutJson?.collectAsState()?.value.orEmpty()
    val vmRosaGuests = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 80

    var canvasElements by remember(vmSketchJson) {
        mutableStateOf(parseSketchElements(vmSketchJson, vmTableCount))
    }
    var undoStack by remember { mutableStateOf(emptyList<List<SketchCanvasElement>>()) }
    var redoStack by remember { mutableStateOf(emptyList<List<SketchCanvasElement>>()) }
    var showLabelEditorModal by remember { mutableStateOf(false) }
    var customLabelInput by remember { mutableStateOf("Tarima DJ") }
    var nextElementSerial by remember { mutableStateOf(100) }

    val activeSwatchHex = swatchColors[selectedSwatchIndex].second
    val totalTablesCount = canvasElements.count { item ->
        val isRound = item.kind == SketchElementKind.ROUND_TABLE
        val isRect = item.kind == SketchElementKind.RECT_TABLE
        if (isRound) true else isRect
    }

    val commitElementsMutation: (List<SketchCanvasElement>) -> Unit = { updatedList ->
        undoStack = (undoStack + listOf(canvasElements)).takeLast(30)
        redoStack = emptyList()
        canvasElements = updatedList
    }

    val appendShapeElement: (SketchElementKind, String) -> Unit = { elementKind, defaultLabel ->
        val currentSameKindCount = canvasElements.count { existing -> existing.kind == elementKind }
        val gridColumn = currentSameKindCount % 3
        val gridRow = (currentSameKindCount / 3) % 3
        val computedX = when (elementKind) {
            SketchElementKind.ARCH_STAGE -> 0.50f
            else -> (0.24f + gridColumn * 0.26f).coerceIn(0.18f, 0.82f)
        }
        val computedY = when (elementKind) {
            SketchElementKind.ARCH_STAGE -> (0.12f + (currentSameKindCount % 2) * 0.72f).coerceIn(0.12f, 0.86f)
            else -> (0.32f + gridRow * 0.24f).coerceIn(0.22f, 0.86f)
        }
        val currentId = nextElementSerial
        nextElementSerial += 1
        val createdElement = SketchCanvasElement(
            id = "el-$currentId",
            kind = elementKind,
            normX = computedX,
            normY = computedY,
            colorHex = activeSwatchHex,
            label = defaultLabel,
        )
        commitElementsMutation(canvasElements + createdElement)
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
                    .background(RfThemeState.currentPalette.bg)
                    .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s32") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boceto del espacio",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = (19 * 1.1f).sp, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        text = "Vista 2D · $totalTablesCount mesas · ${canvasElements.size} elementos",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                    )
                }

                val canUndo = undoStack.isNotEmpty()
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            if (undoStack.isNotEmpty()) {
                                val previousSnapshot = undoStack.last()
                                undoStack = undoStack.dropLast(1)
                                redoStack = (redoStack + listOf(canvasElements)).takeLast(30)
                                canvasElements = previousSnapshot
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(
                        kind = RfIconKind.UNDO,
                        tint = if (canUndo) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                        size = 20.dp,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            commitElementsMutation(buildDefaultSketchElements(2))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.DELETE, tint = RfThemeState.currentPalette.ink, size = 20.dp)
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
                        text = "Agrega mesas, sillas, arcos o etiquetas.",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = RfThemeState.currentPalette.ink),
                    )
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(RosaButtonGradient)
                            .clickable {
                                val balanced = buildAiBalancedSketchLayout(vmRosaGuests)
                                commitElementsMutation(balanced)
                                syncSketchElementsIdempotently(balanced, viewModel)
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Ayúdame con IA",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SketchCanvasPreviewSurface(
                    elements = canvasElements,
                    isThreeDimensionalPreview = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(380.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(24.dp)),
                    onCanvasTap = {
                        when (selectedToolIndex) {
                            0 -> {
                                if (canvasElements.isNotEmpty()) {
                                    val movedLast = canvasElements.last().let { lastEl ->
                                        val shiftedX = if (lastEl.normX > 0.72f) 0.25f else (lastEl.normX + 0.14f)
                                        val shiftedY = if (lastEl.normY > 0.78f) 0.30f else (lastEl.normY + 0.10f)
                                        lastEl.copy(normX = shiftedX, normY = shiftedY)
                                    }
                                    commitElementsMutation(canvasElements.dropLast(1) + movedLast)
                                }
                            }

                            1 -> appendShapeElement(SketchElementKind.ARCH_STAGE, "Arco / Tarima")

                            2 -> appendShapeElement(SketchElementKind.ROUND_TABLE, "Mesa redonda")

                            3 -> appendShapeElement(SketchElementKind.RECT_TABLE, "Mesa imperial")

                            4 -> appendShapeElement(SketchElementKind.CHAIR_GROUP, "Fila sillas")

                            5 -> showLabelEditorModal = true

                            else -> Unit
                        }
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    swatchColors.forEachIndexed { index, swatchPair ->
                        val swatchColor = swatchPair.first
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

                Spacer(modifier = Modifier.height(16.dp))

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
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
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

                    Box(
                        modifier = toolButtonModifier(0) {
                            if (canvasElements.isNotEmpty()) {
                                val movedLast = canvasElements.last().let { lastEl ->
                                    val shiftedX = if (lastEl.normX > 0.72f) 0.25f else (lastEl.normX + 0.14f)
                                    val shiftedY = if (lastEl.normY > 0.78f) 0.30f else (lastEl.normY + 0.10f)
                                    lastEl.copy(normX = shiftedX, normY = shiftedY)
                                }
                                commitElementsMutation(canvasElements.dropLast(1) + movedLast)
                            }
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.MOVE, tint = if (selectedToolIndex == 0) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(1) {
                            appendShapeElement(SketchElementKind.ARCH_STAGE, "Arco / Tarima")
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SPARKLE, tint = if (selectedToolIndex == 1) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(2) {
                            appendShapeElement(SketchElementKind.ROUND_TABLE, "Mesa redonda")
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CIRCLE, tint = if (selectedToolIndex == 2) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(3) {
                            appendShapeElement(SketchElementKind.RECT_TABLE, "Mesa imperial")
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.TABLE, tint = if (selectedToolIndex == 3) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(4) {
                            appendShapeElement(SketchElementKind.CHAIR_GROUP, "Fila sillas")
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CHAIR, tint = if (selectedToolIndex == 4) Color.White else Color(0xFF62627A), size = 20.dp)
                    }
                    Box(
                        modifier = toolButtonModifier(5) {
                            showLabelEditorModal = true
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
                    val canRedo = redoStack.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = canRedo) {
                                if (redoStack.isNotEmpty()) {
                                    val restoredSnapshot = redoStack.last()
                                    redoStack = redoStack.dropLast(1)
                                    undoStack = (undoStack + listOf(canvasElements)).takeLast(30)
                                    canvasElements = restoredSnapshot
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        val redoTint = if (canRedo) Color(0xFF1A1A2E) else Color(0xFFA0A0B2)
                        RfIcon(kind = RfIconKind.REDO, tint = redoTint, size = 20.dp)
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
                        syncSketchElementsIdempotently(canvasElements, viewModel)
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

            if (showLabelEditorModal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(5f)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { showLabelEditorModal = false },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .clickable(enabled = true, onClick = {})
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        BasicText(
                            text = "Agregar etiqueta de zona",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("Tarima DJ", "Mesa de dulces", "Entrada").forEach { presetLabel ->
                                val isPresetSelected = customLabelInput.equals(presetLabel, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (isPresetSelected) Color(0xFFFDE6F2) else Color(0xFFF3F0FA))
                                        .clickable { customLabelInput = presetLabel }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = presetLabel,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isPresetSelected) Color(0xFFC2338F) else RfThemeState.currentPalette.ink,
                                        ),
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (customLabelInput.isEmpty()) {
                                BasicText(
                                    text = "Ej: Tarima DJ, Mesa de dulces, Entrada",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicTextField(
                                value = customLabelInput,
                                onValueChange = { updatedValue -> customLabelInput = updatedValue },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .clickable { showLabelEditorModal = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Cancelar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(RosaButtonGradient)
                                    .clickable {
                                        val resolvedLabel = customLabelInput.trim().ifBlank { "Zona especial" }
                                        appendShapeElement(SketchElementKind.TEXT_LABEL, resolvedLabel)
                                        showLabelEditorModal = false
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Insertar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
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
private fun SketchChip78(dotColor: Color, label: String) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(dotColor))
        BasicText(
            text = label,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
        )
    }
}

private fun starPath(cx: Float, cy: Float, outer: Float, inner: Float): Path {
    val path = Path()
    for (pointIndex in 0 until 10) {
        val radius = if (pointIndex % 2 == 0) outer else inner
        val angle = (-90.0 + pointIndex * 36.0) * PI / 180.0
        val posX = cx + (radius * cos(angle)).toFloat()
        val posY = cy + (radius * sin(angle)).toFloat()
        if (pointIndex == 0) path.moveTo(posX, posY) else path.lineTo(posX, posY)
    }
    path.close()
    return path
}

@Composable
internal fun RenderRosaSketchConfirmS78(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val vmSketchJson = viewModel?.sketchLayoutJson?.collectAsState()?.value.orEmpty()
    val vmSketchTables = viewModel?.sketchTableCount?.collectAsState()?.value ?: 6
    val vmRosaGuests = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 80
    val observedEvents = viewModel?.events?.collectAsState()?.value
    val observedSelectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val fallbackEventTitle = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()

    var previewElements by remember(vmSketchJson, vmSketchTables) {
        mutableStateOf(parseSketchElements(vmSketchJson, vmSketchTables))
    }

    val activeEventTitle = remember(observedEvents, observedSelectedEventId, fallbackEventTitle) {
        viewModel?.activeClientEventOrNull()?.title
            ?: fallbackEventTitle.ifBlank { CanonicalSampleData.events.first().title }
    }

    val roundTablesCount = previewElements.count { item -> item.kind == SketchElementKind.ROUND_TABLE }
    val rectTablesCount = previewElements.count { item -> item.kind == SketchElementKind.RECT_TABLE }
    val chairGroupsCount = previewElements.count { item -> item.kind == SketchElementKind.CHAIR_GROUP }
    val archCount = previewElements.count { item -> item.kind == SketchElementKind.ARCH_STAGE }
    val labelsSummary = previewElements
        .filter { item -> item.kind == SketchElementKind.TEXT_LABEL }
        .mapNotNull { item -> item.label.trim().ifBlank { null } }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
                .background(RfThemeState.currentPalette.bg)
                .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                    .clickable { onSelectScreenCode("BACK:s36") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Revisa tu boceto 2D",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = RfThemeState.currentPalette.ink),
                )
                BasicText(
                    text = activeEventTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
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
                    .height(244.dp)
                    .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(24.dp)),
            ) {
                SketchCanvasPreviewSurface(
                    elements = previewElements,
                    isThreeDimensionalPreview = true,
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                )

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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BasicText(
                    text = "LO QUE DIBUJASTE",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted, letterSpacing = 1.sp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SketchChip78(dotColor = Color(0xFFEA4EA9), label = "$roundTablesCount mesas redondas")
                        if (rectTablesCount > 0) {
                            SketchChip78(dotColor = Color(0xFF865DEC), label = "$rectTablesCount mesas imperiales")
                        }
                        if (archCount > 0) {
                            SketchChip78(dotColor = Color(0xFF865DEC), label = "$archCount tarima / arco")
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val totalEstimatedChairs = (roundTablesCount * 8) + (rectTablesCount * 10) + (chairGroupsCount * 6)
                        SketchChip78(dotColor = Color(0xFF1FBDC0), label = "$totalEstimatedChairs sillas calculadas")
                        val zoneLabelText = if (labelsSummary.isNotEmpty()) labelsSummary.joinToString(" · ") else "Pista de baile"
                        SketchChip78(dotColor = Color(0xFF8DB52C), label = zoneLabelText)
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
                                text = "Ordeno ${(vmRosaGuests / 8).coerceIn(2, 12)} mesas para tus $vmRosaGuests invitados, arco y pista central.",
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
                            .background(RfThemeState.currentPalette.surface)
                            .clickable {
                                val aiArranged = buildAiBalancedSketchLayout(vmRosaGuests)
                                previewElements = aiArranged
                                syncSketchElementsIdempotently(aiArranged, viewModel)
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
                    .clickable {
                        syncSketchElementsIdempotently(previewElements, viewModel)
                        onSelectScreenCode("s32")
                    },
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
                    .clickable {
                        syncSketchElementsIdempotently(previewElements, viewModel)
                        onSelectScreenCode("s36")
                    },
            ) {
                BasicText(
                    "Seguir editando",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                )
            }
        }
    }
}

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

    val isVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val isVoiceTranscribing = viewModel?.isVoiceTranscribing?.collectAsState()?.value ?: false
    val voiceTranscript = viewModel?.voiceTranscript?.collectAsState()?.value.orEmpty()
    val voiceErrorMessage = viewModel?.voiceErrorMessage?.collectAsState()?.value

    var eventTypeTitle by remember { mutableStateOf("Boda") }
    var assignedClient by remember { mutableStateOf<String?>(null) }
    var clientCycleIndex by remember { mutableStateOf(0) }
    var dateCycleIndex by remember { mutableStateOf(0) }
    var includeLighting by remember { mutableStateOf(false) }
    var includeDessertStation by remember { mutableStateOf(false) }
    var guestCount by remember { mutableStateOf(120) }
    var promptText by remember { mutableStateOf("") }
    var adminVoiceSessionStarted by remember { mutableStateOf(false) }
    var extraChatTurns by remember { mutableStateOf(emptyList<AdminRosaChatTurn>()) }
    val messagesScrollState = rememberScrollState()

    LaunchedEffect(voiceTranscript, adminVoiceSessionStarted) {
        val shouldFeedTranscript = if (adminVoiceSessionStarted) voiceTranscript.isNotBlank() else false
        if (shouldFeedTranscript) {
            promptText = voiceTranscript
        }
    }

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
        viewModel?.setEventVenueInput("Jardín Los Rosales")
        viewModel?.setEventGuestsInput(guestCount.toString())
        viewModel?.setNewEventTitle("$eventTypeTitle de $targetClient")
        viewModel?.setNewEventClient(targetClient)
        viewModel?.setNewEventDate(currentShortDate)
        viewModel?.setNewEventVenue("Jardín Los Rosales")
        viewModel?.setNewEventGuests(guestCount.toString())
        viewModel?.setDraftQuantity("art-silla-tiffany", chairCount)
        viewModel?.setDraftQuantity("art-mesa-redonda", tableCount)
        viewModel?.setDraftQuantity("art-centro-floral", tableCount)
        viewModel?.setDraftQuantity("art-arco-rosa", 1)
        if (includeDessertStation) {
            viewModel?.setDraftQuantity("art-mesa-helados", 1)
        }
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
            val hasExplicitGuestUpdate = if (parsedNumber != null) true else mentionsGuestsKeyword
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
                val mentionsQuitar = lower.contains("quitar")
                val mentionsSin = lower.contains("sin ")
                val wantsRemoval = if (mentionsQuitar) true else mentionsSin
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
                    viewModel?.setDraftQuantity("art-mesa-helados", 1)
                    replySegments.add("Incluí la estación de dulces y helados (+RD$ 150).")
                } else {
                    viewModel?.setDraftQuantity("art-mesa-helados", 0)
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
            val updatedLightingAdd = if (includeLighting) 180 else 0
            val updatedDessertAdd = if (includeDessertStation) 150 else 0
            val updatedTotal = updatedChairs * 8 + updatedTables * 20 + 95 + updatedLightingAdd + updatedDessertAdd
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
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable {
                            if (isVoiceListening) {
                                viewModel.stopVoiceInput()
                            }
                            onSelectScreenCode("BACK:A01")
                        },
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
                            .background(RfThemeState.currentPalette.surface)
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
                            .background(RfThemeState.currentPalette.surface)
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

                            proposalItems.forEach { proposalRow ->
                                val photo = proposalRow.first
                                val itemData = proposalRow.second
                                val tagInfo = proposalRow.third
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
                                            .background(RfThemeState.currentPalette.surface)
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

                extraChatTurns.forEach { turnItem ->
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
                            text = turnItem.userText,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color.White, lineHeight = 20.sp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .widthIn(max = 310.dp)
                            .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp), spotColor = Color(0x604650B4))
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = turnItem.rosaReply,
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
                val showVoiceStatusBanner = when {
                    isVoiceListening -> true
                    isVoiceTranscribing -> true
                    adminVoiceSessionStarted -> voiceErrorMessage != null
                    else -> false
                }
                if (showVoiceStatusBanner) {
                    val voiceBannerLabel = when {
                        isVoiceTranscribing -> "Transcribiendo instrucción de voz…"
                        isVoiceListening -> "Escuchando… toca el micrófono para detener"
                        else -> voiceErrorMessage ?: ""
                    }
                    if (voiceBannerLabel.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECEBFF))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = voiceBannerLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF4B47D6),
                                ),
                            )
                        }
                    }
                }

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
                            .background(RfThemeState.currentPalette.surface)
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
                        if (isVoiceListening) {
                            viewModel.stopVoiceInput()
                        }
                        val submitted = promptText
                        promptText = ""
                        adminVoiceSessionStarted = false
                        handleUserCommand(submitted)
                    } else {
                        if (isVoiceListening) {
                            viewModel.stopVoiceInput()
                            if (voiceTranscript.isNotBlank()) {
                                promptText = voiceTranscript
                            }
                        } else {
                            adminVoiceSessionStarted = true
                            viewModel?.startVoiceInput(VoiceInputOrigin.ROSA_WIZARD)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(58.dp)
                        .shadow(14.dp, CircleShape, spotColor = Color(0xC04650B4), ambientColor = Color(0x504650B4))
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
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
                                val inputPlaceholder = if (isVoiceListening) {
                                    "Escuchando instrucción…"
                                } else {
                                    "Pídele algo más a Rosa…"
                                }
                                BasicText(
                                    text = inputPlaceholder,
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

                        val actionButtonBrush = if (isVoiceListening) {
                            Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF7C5CF0)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
                        }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(10.dp, CircleShape, spotColor = Color(0xFA5B6CF0), ambientColor = Color(0x705B6CF0))
                                .clip(CircleShape)
                                .background(actionButtonBrush)
                                .clickable { submitAdminRosaPrompt() },
                            contentAlignment = Alignment.Center,
                        ) {
                            val buttonIcon = when {
                                hasPromptText -> RfIconKind.SEND
                                isVoiceListening -> RfIconKind.CLOSE
                                else -> RfIconKind.MIC
                            }
                            RfIcon(
                                kind = buttonIcon,
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
