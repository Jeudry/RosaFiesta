package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.jeudry.rosafiesta.domain.EventPackage
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.RosaSketchElement
import com.jeudry.rosafiesta.domain.RosaSketchProposal
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private val RosaBackgroundGradient: Brush
    get() = RfThemeState.currentPalette.rosaAssistantBgGradient

private val AdminRosaBackgroundGradient: Brush
    get() = RfThemeState.currentPalette.rosaAssistantBgGradient

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

/**
 * Centered assistant dialog overlay with smooth fade/scale transitions and no-ripple backdrop dismiss.
 */
@Composable
private fun RfCenteredAnimatedAssistantModal(
    visible: Boolean,
    zIndexOrder: Float,
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 180)) + scaleIn(
            animationSpec = tween(durationMillis = 180),
            initialScale = 0.94f,
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 140)) + scaleOut(
            animationSpec = tween(durationMillis = 140),
            targetScale = 0.96f,
        ),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(zIndexOrder),
    ) {
        val backdropInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = backdropInteraction,
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

private fun validateOptionalRosaPrompt(rawValue: String): String? {
    if (rawValue.isEmpty()) {
        return null
    }
    return InputValidator.validateChatMessage(rawValue)
}

@Composable
private fun rememberOptionalPromptValidationError(
    promptText: String,
    forceShowError: Boolean = false,
): String? = rememberDebouncedValidationError(
    value = promptText,
    hasInteracted = promptText.isNotEmpty(),
    forceShowError = forceShowError,
    validator = ::validateOptionalRosaPrompt,
)

private fun resolveAssistantErrorBorderColor(
    hasError: Boolean,
    defaultColor: Color = RfThemeState.currentPalette.line,
): Color = if (hasError) Color(0xFFD63B52) else defaultColor

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

private fun resolveRosaHeaderSpeech(
    screenCode: String,
    eventType: String,
    venueLabel: String,
    proposalMessage: String,
    draftCount: Int,
    activeCategoryTitle: String = "",
): Pair<String, String> {
    val cleanType = eventType.trim()
    val cleanVenue = venueLabel.trim()
    val cleanProposal = proposalMessage.trim()
    val cleanCategory = activeCategoryTitle.trim()
    val eventMeta = resolveRosaEventMetadata(cleanType.ifEmpty { "Boda" })
    val eventLower = eventMeta.displayTitle.lowercase()
    return when (screenCode) {
        "s26" -> {
            val subtitle = if (cleanType.isNotEmpty()) {
                "Elegiste ${eventMeta.displayTitle} · Cámbialo o escríbeme"
            } else {
                "Elige una opción o escríbeme abajo"
            }
            "¿Qué evento quieres celebrar?" to subtitle
        }

        "s27" -> {
            val subtitle = cleanProposal.ifEmpty {
                "${eventMeta.displayTitle} · Cuéntame más por texto o voz"
            }
            "${eventMeta.spanishGreeting} Me encanta." to subtitle
        }

        "s28" -> "¿Te ayudo a elegir tus artículos?" to "Para tu $eventLower · Puedes cambiar todo luego"

        "s29" -> {
            val fallbackHint = if (draftCount > 0) {
                "$draftCount artículos en tu borrador · Pídeme más abajo"
            } else {
                "Elige piezas o pídeme algo distinto abajo"
            }
            val subtitle = cleanProposal.ifEmpty { fallbackHint }
            "Para tu $eventLower te recomiendo:" to subtitle
        }

        "s30" -> {
            val subtitle = cleanProposal.ifEmpty { "Elige categorías, cantidades y arma tu evento" }
            "¿Qué necesitas para tu $eventLower?" to subtitle
        }

        "s30_picker" -> {
            val titleText = if (cleanCategory.isNotEmpty()) {
                "Para $cleanCategory te recomiendo:"
            } else {
                "Elige tus artículos"
            }
            titleText to "Agrega o quita piezas a tu evento"
        }

        "s31" -> {
            val subtitle = if (cleanVenue.isNotEmpty()) {
                "$cleanVenue · Confirma fecha y horario"
            } else {
                "Confirma título, fecha, horario y dirección"
            }
            "Ahora, los detalles de tu evento" to subtitle
        }

        else -> {
            val subtitle = if (draftCount > 0) {
                "$draftCount artículo(s) seleccionados · Confirma tus datos"
            } else {
                "Confirma título, invitados, horario y ubicación"
            }
            "Listo. Revisa que todo esté bien" to subtitle
        }
    }
}

@Composable
private fun RosaWizardHeader(
    screenCode: String,
    cartCount: Int? = null,
    activeCategoryTitle: String = "",
    viewModel: RosaFiestaViewModel? = null,
    onBack: () -> Unit = {},
    onCart: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val observedDraftQuantities = viewModel?.draftQuantities?.collectAsState()?.value
    val liveDraftUnits = observedDraftQuantities
        ?.values
        ?.filter { unitCount -> unitCount > 0 }
        ?.sum()
        ?: viewModel?.totalDraftCartUnits()
        ?: 0
    val resolvedCartCount = cartCount ?: liveDraftUnits
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val vmVenueInput = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty()
    val vmProposalMessage = viewModel?.rosaProposalMessage?.collectAsState()?.value.orEmpty()
    val activeEventType = vmEventTypeInput.ifBlank { vmRosaEventType }
    val (assistantTitle, assistantSubtitle) = resolveRosaHeaderSpeech(
        screenCode = screenCode,
        eventType = activeEventType,
        venueLabel = vmVenueInput,
        proposalMessage = vmProposalMessage,
        draftCount = resolvedCartCount,
        activeCategoryTitle = activeCategoryTitle,
    )
    val subtitleColor = if (palette.isDark) Color(0xFFC5B4F8) else Color(0xFF7A4FD8)
    val headerBrush = if (palette.isDark) {
        Brush.verticalGradient(listOf(palette.surface, palette.surface))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE5DCFA), Color(0xFFEDE6FC)))
    }
    val headerBorderColor = if (palette.isDark) palette.lineSoft else Color(0xFFD8CCF2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(2f)
            .background(headerBrush),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(palette.surface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(
                    kind = RfIconKind.BACK,
                    tint = palette.ink,
                    size = 20.dp,
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false, customSize = 42.dp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    BasicText(
                        text = assistantTitle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            color = palette.ink,
                        ),
                    )
                    BasicText(
                        text = assistantSubtitle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            color = subtitleColor,
                        ),
                    )
                }
            }

            RfDraftCartPillButton(
                count = resolvedCartCount,
                onClick = onCart,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(headerBorderColor),
        )
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
    val draftEventTitle = viewModel?.clientEventTitleInput?.collectAsState()?.value.orEmpty()
    val draftEventType = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val draftEventDate = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty()
    val draftEventVenue = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty()
    val draftEventGuests = viewModel?.eventGuestsInput?.collectAsState()?.value.orEmpty()
    val hasConfiguredDraft = viewModel?.hasConfiguredEventDraft() == true

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

            if (hasConfiguredDraft) {
                val resolvedCardTitle = draftEventTitle.ifBlank { draftEventType.ifBlank { "Tu evento" } }
                val formattedDate = formatReadableSpanishDateText(draftEventDate)
                val guestsFragment = if (draftEventGuests.isNotBlank()) " · $draftEventGuests inv." else ""
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                        .clip(RoundedCornerShape(18.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
                        .clickable {
                            viewModel.startEventDraftSetup(returnScreenCode = "s25")
                            onSelectScreenCode("s38")
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDE6F2)),
                    ) {
                        RfIcon(kind = RfIconKind.PARTY, tint = Color(0xFFC2338F), size = 20.dp)
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        BasicText(
                            text = resolvedCardTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "$formattedDate · $draftEventVenue$guestsFragment",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFF3EBFF))
                            .padding(horizontal = 10.dp),
                    ) {
                        BasicText(
                            text = "Editar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4FD8)),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (resolvedRows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFFC2338F), size = 48.dp)
                        BasicText(
                            text = if (hasConfiguredDraft) "Ahora agrega artículos a tu evento" else "Tu carrito de evento está vacío",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, textAlign = TextAlign.Center),
                        )
                        BasicText(
                            text = "Explora el catálogo o la tienda de globos para armar tu cotización",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(46.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = false)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RosaButtonGradient)
                                .clickable { onSelectScreenCode("s18") }
                                .padding(horizontal = 22.dp),
                        ) {
                            BasicText(
                                text = "Explorar catálogo",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                            )
                        }
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
                                                viewModel?.adjustDraftQuantity(row.itemId, 1, showPreview = false)
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

            Spacer(modifier = Modifier.height(138.dp))
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
                                onSelectScreenCode("s18")
                            } else if (hasConfiguredDraft) {
                                viewModel.clearFormError()
                                onSelectScreenCode("s32")
                            } else {
                                viewModel?.clearFormError()
                                viewModel?.startEventDraftSetup(returnScreenCode = "s32")
                                onSelectScreenCode("s38")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val primaryCtaLabel = when {
                        resolvedRows.isEmpty() -> "Explorar catálogo"
                        hasConfiguredDraft -> "Revisar y solicitar cotización"
                        else -> "Configurar datos del evento"
                    }
                    BasicText(
                        text = primaryCtaLabel,
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
            screenCode = "s26",
            viewModel = viewModel,
            onBack = { onSelectScreenCode("BACK:s14") },
            onCart = { onSelectScreenCode("s25") },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 78.dp, bottom = 88.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
                        var hasInteractedCustomType by remember { mutableStateOf(false) }
                        var forceShowCustomTypeError by remember { mutableStateOf(false) }
                        val customTypeError = rememberDebouncedValidationError(
                            value = customEventTypeInput,
                            hasInteracted = hasInteractedCustomType,
                            forceShowError = forceShowCustomTypeError,
                        ) { candidate ->
                            InputValidator.validateEntityName(
                                rawValue = candidate,
                                fieldLabel = "El tipo de evento",
                                minLength = 2,
                                maxLength = InputValidator.MAX_EVENT_NAME_LENGTH,
                                isRequired = true,
                            )
                        }
                        BasicText(
                            text = "Personaliza tu celebración",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF7A4FD8),
                            ),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .border(
                                        width = 1.dp,
                                        color = resolveAssistantErrorBorderColor(
                                            hasError = customTypeError != null,
                                            defaultColor = Color.Transparent,
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                    )
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
                                        val capped = updatedCustom.take(InputValidator.MAX_EVENT_NAME_LENGTH)
                                        customEventTypeInput = capped
                                        hasInteractedCustomType = true
                                        forceShowCustomTypeError = false
                                        val cleanCustom = capped.trim()
                                        if (cleanCustom.isNotEmpty()) {
                                            viewModel?.updateRosaParameters(eventType = cleanCustom)
                                            viewModel?.setEventTypeInput(cleanCustom)
                                        }
                                    },
                                    singleLine = true,
                                    cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                    textStyle = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = RfThemeState.currentPalette.ink,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            RfValidationErrorText(errorMessage = customTypeError)
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
                                            hasInteractedCustomType = true
                                            forceShowCustomTypeError = false
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
                                    val immediateError = InputValidator.validateEntityName(
                                        rawValue = customEventTypeInput,
                                        fieldLabel = "El tipo de evento",
                                        minLength = 2,
                                        maxLength = InputValidator.MAX_EVENT_NAME_LENGTH,
                                        isRequired = true,
                                    )
                                    if (immediateError != null) {
                                        hasInteractedCustomType = true
                                        forceShowCustomTypeError = true
                                        viewModel?.postError(immediateError)
                                    } else {
                                        val resolvedCustom = customEventTypeInput.trim()
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

        val promptErrorS26 = rememberOptionalPromptValidationError(promptText = promptText)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RfValidationErrorText(errorMessage = promptErrorS26)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .rfInputShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(
                        width = 1.dp,
                        color = resolveAssistantErrorBorderColor(
                            hasError = promptErrorS26 != null,
                            defaultColor = Color.Transparent,
                        ),
                        shape = CircleShape,
                    )
                    .padding(start = 20.dp, end = 6.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val hasPromptText = promptText.isNotBlank()
                val submitS26Prompt = {
                    if (promptText.isNotBlank()) {
                        val promptValidation = InputValidator.validateChatMessage(promptText)
                        if (promptValidation == null) {
                            val submittedText = promptText.trim()
                            applyNaturalLanguageEventPrompt(submittedText, viewModel)
                            viewModel?.sendConversationalRosaPrompt(submittedText)
                            promptText = ""
                            onSelectScreenCode("s27")
                        }
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
                            onValueChange = { updatedText ->
                                promptText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitS26Prompt() }),
                            cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
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
    val explicitDateMatch = Regex(
        "\\b(\\d{1,2})\\s*(?:de\\s+)?(enero|ene|febrero|feb|marzo|mar|abril|abr|mayo|may|junio|jun|julio|jul|agosto|ago|septiembre|sep|octubre|oct|noviembre|nov|diciembre|dic)\\b",
    ).find(lower)
    val detectedDate = if (explicitDateMatch != null) {
        val dayNum = explicitDateMatch.groupValues[1].toIntOrNull() ?: 0
        val monthToken = explicitDateMatch.groupValues[2]
        val maxDayInMonth = when {
            monthToken.startsWith("feb") -> 29
            monthToken.startsWith("abr") -> 30
            monthToken.startsWith("jun") -> 30
            monthToken.startsWith("sep") -> 30
            monthToken.startsWith("nov") -> 30
            else -> 31
        }
        if (dayNum in 1..maxDayInMonth) {
            "$dayNum de $monthToken"
        } else {
            null
        }
    } else {
        when {
            lower.contains("diciembre") -> "4 de diciembre"
            lower.contains("noviembre") -> "14 de noviembre"
            lower.contains("octubre") -> "23 de octubre"
            else -> null
        }
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
            screenCode = "s27",
            viewModel = viewModel,
            onBack = { onSelectScreenCode("BACK:s26") },
            onCart = { onSelectScreenCode("s25") },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 78.dp, bottom = 88.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                .clickable { onSelectScreenCode("s30") }
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

                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        Canvas(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 92.dp)
                .size(width = 108.dp, height = 112.dp),
        ) {
            val scaleX = size.width / 108f
            val scaleY = size.height / 112f
            val dashEffect = PathEffect.dashPathEffect(
                floatArrayOf(6f * scaleX, 7f * scaleX),
            )
            val strokeColor = Color(0xFF865DEC)
            val curvePath = Path().apply {
                moveTo(14f * scaleX, 12f * scaleY)
                cubicTo(
                    54f * scaleX,
                    16f * scaleY,
                    84f * scaleX,
                    46f * scaleY,
                    82f * scaleX,
                    94f * scaleY,
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
                moveTo(71f * scaleX, 85f * scaleY)
                lineTo(82f * scaleX, 100f * scaleY)
                lineTo(93f * scaleX, 86f * scaleY)
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
                onSelectScreenCode("s30")
            } else {
                openRosaVoiceScreen(
                    viewModel = viewModel,
                    returnScreenCode = "s27",
                    onSelectScreenCode = onSelectScreenCode,
                )
            }
        }

        val promptErrorS27 = rememberOptionalPromptValidationError(promptText = promptText)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RfValidationErrorText(errorMessage = promptErrorS27)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .rfInputShadow(RoundedCornerShape(999.dp), isAdmin = false)
                    .clip(RoundedCornerShape(999.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(
                        width = 1.dp,
                        color = resolveAssistantErrorBorderColor(
                            hasError = promptErrorS27 != null,
                            defaultColor = Color(0xFFE7DEF5),
                        ),
                        shape = RoundedCornerShape(999.dp),
                    )
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
                        onValueChange = { updatedText ->
                            promptText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitS27Prompt() }),
                        cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
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
                            .clickable {
                                val canSendPrompt = if (promptText.isBlank()) {
                                    true
                                } else {
                                    InputValidator.validateChatMessage(promptText) == null
                                }
                                if (canSendPrompt) {
                                    submitS27Prompt()
                                }
                            },
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
            .background(RosaBackgroundGradient),
    ) {
        RosaWizardHeader(
            screenCode = "s28",
            viewModel = viewModel,
            onBack = { onSelectScreenCode("BACK:s27") },
            onCart = { onSelectScreenCode("s25") },
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxSize()
                .padding(top = 78.dp, bottom = 224.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
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

        val promptErrorS28 = rememberOptionalPromptValidationError(promptText = promptText)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RfValidationErrorText(errorMessage = promptErrorS28)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(
                        width = 1.dp,
                        color = resolveAssistantErrorBorderColor(
                            hasError = promptErrorS28 != null,
                            defaultColor = Color(0xFFE7DEF5),
                        ),
                        shape = CircleShape,
                    )
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
                        onValueChange = { updatedText ->
                            promptText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitS28Prompt() }),
                        cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
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
                        .clickable {
                            val canSendPrompt = if (promptText.isBlank()) {
                                true
                            } else {
                                InputValidator.validateChatMessage(promptText) == null
                            }
                            if (canSendPrompt) {
                                submitS28Prompt()
                            }
                        },
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

private fun balloonToCatalogArticle(balloon: BalloonProduct): CatalogArticle = CatalogArticle(
    id = balloon.id,
    code = balloon.code,
    name = balloon.name,
    category = "Globos personalizados",
    description = balloon.description,
    pricePerDayDop = balloon.unitPriceDop,
    totalStock = balloon.totalStock,
    availableOct23 = balloon.availableStock,
    photo = balloon.photo,
    variants = balloon.colors,
    dimensions = balloon.dimensions,
    material = balloon.material,
    visibleInCatalog = true,
)

private fun mergeCatalogArticlesWithWizardOptions(
    catalogArticles: List<CatalogArticle>,
    balloonProducts: List<BalloonProduct>,
    includePreviewExtras: Boolean,
): List<CatalogArticle> {
    val mergedById = linkedMapOf<String, CatalogArticle>()
    for (article in catalogArticles) {
        mergedById[article.id] = article
    }
    if (includePreviewExtras) {
        for (extra in CanonicalSampleData.rosaWizardExtras) {
            if (!mergedById.containsKey(extra.id)) {
                mergedById[extra.id] = extra
            }
        }
    }
    for (balloon in balloonProducts) {
        if (!mergedById.containsKey(balloon.id)) {
            mergedById[balloon.id] = balloonToCatalogArticle(balloon)
        }
    }
    return mergedById.values.toList()
}

private data class SelectedWizardArticleLine(
    val article: CatalogArticle,
    val quantity: Int,
)

private fun resolveSelectedWizardLines(
    allPoolArticles: List<CatalogArticle>,
    quantities: Map<String, Int>,
): List<SelectedWizardArticleLine> = allPoolArticles.mapNotNull { article ->
    val qty = quantities[article.id] ?: 0
    if (qty > 0) SelectedWizardArticleLine(article = article, quantity = qty) else null
}

private fun matchesEventTypeStem(
    candidateText: String,
    eventType: String,
): Boolean {
    val haystack = candidateText.lowercase()
    val needle = eventType.trim().lowercase()
    if (needle.isEmpty()) return true
    val canonicalStem = when {
        needle.contains("boda") -> "boda"
        needle.contains("quince") -> "quince"
        needle.contains("cumple") -> "cumple"
        needle.contains("infantil") -> "infantil"
        needle.contains("bautizo") -> "bautizo"
        needle.contains("gradu") -> "gradu"
        needle.contains("corp") -> "corp"
        else -> needle
    }
    return haystack.contains(canonicalStem)
}

private fun orderEventPackages(
    packagesPool: List<EventPackage>,
    eventType: String,
    selectedCategoryTitle: String = "",
): List<EventPackage> {
    val visiblePackages = packagesPool.filter { bundle -> bundle.visibleInCatalog }
    val matchingEventType = visiblePackages.filter { bundle ->
        val searchable = "${bundle.eventType} ${bundle.name} ${bundle.subtitle}"
        matchesEventTypeStem(searchable, eventType)
    }
    val poolByEvent = matchingEventType.ifEmpty { visiblePackages }
    val cleanCategory = selectedCategoryTitle.trim().lowercase()
    if (cleanCategory.isEmpty()) return poolByEvent
    val matchingCategory = poolByEvent.filter { bundle ->
        val itemsText = bundle.includedItems.joinToString(" ").lowercase()
        val hasSeatingMatch = if (cleanCategory.contains("mobiliario")) {
            itemsText.contains("silla") || itemsText.contains("mesa")
        } else {
            false
        }
        val hasFloralMatch = if (cleanCategory.contains("floral")) {
            itemsText.contains("floral") || itemsText.contains("centro")
        } else {
            false
        }
        val isDecorCategory = cleanCategory.contains("globo") || cleanCategory.contains("decor")
        val hasDecorMatch = if (isDecorCategory) {
            itemsText.contains("arco") || itemsText.contains("globo")
        } else {
            false
        }
        val hasDirectWord = itemsText.contains(cleanCategory)
        hasDirectWord || hasSeatingMatch || hasFloralMatch || hasDecorMatch
    }
    return matchingCategory.ifEmpty { poolByEvent }
}

private fun isArticleRelevantToEventType(
    articleItem: CatalogArticle,
    eventType: String,
): Boolean {
    val typeLower = eventType.trim().lowercase()
    val articleLower = "${articleItem.name} ${articleItem.category} ${articleItem.description}".lowercase()
    val isAdultElegantEvent = typeLower.contains("boda") || typeLower.contains("corp") || typeLower.contains("gala")
    val isChildishBalloonItem = articleLower.contains("helados") || articleLower.contains("piñata") || articleLower.contains("infantil")
    if (isAdultElegantEvent) {
        return !isChildishBalloonItem
    }
    return true
}

@Composable
private fun RosaSuggestionsCarouselSection(
    sectionTitle: String,
    suggestions: List<RosaSuggestedArticleUi>,
    activeQuantities: Map<String, Int>,
    onUpdateQuantity: (String, Int) -> Unit,
    secondaryChipLabel: String? = null,
    onSecondaryChipClick: (() -> Unit)? = null,
) {
    val typo = LocalRfTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = sectionTitle,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = (0.08 * 11).sp,
                    color = Color(0xFF6B5A8E),
                ),
            )
            if (secondaryChipLabel != null) {
                if (onSecondaryChipClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .clickable(onClick = onSecondaryChipClick)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        BasicText(
                            text = secondaryChipLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF7A4FD8),
                            ),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            suggestions.forEach { suggestion ->
                val liveQuantity = activeQuantities[suggestion.articleId] ?: 0
                RosaSuggestionArticleCard(
                    suggestion = suggestion,
                    quantity = liveQuantity,
                    onAdd = { onUpdateQuantity(suggestion.articleId, 1) },
                    onIncrement = { onUpdateQuantity(suggestion.articleId, liveQuantity + 1) },
                    onDecrement = { onUpdateQuantity(suggestion.articleId, liveQuantity - 1) },
                )
            }
        }
    }
}

@Composable
private fun RosaPromosCarouselSection(
    packages: List<EventPackage>,
    activePackageId: String?,
    onInspectPromo: (EventPackage) -> Unit,
) {
    val typo = LocalRfTypography.current
    if (packages.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "PROMOS Y PAQUETES",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = (0.08 * 11).sp,
                    color = Color(0xFF6B5A8E),
                ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            packages.forEach { packageItem ->
                val isApplied = activePackageId == packageItem.id
                RosaPromoPackageCard(
                    packageItem = packageItem,
                    isApplied = isApplied,
                    onInspectPromo = { onInspectPromo(packageItem) },
                )
            }
        }
    }
}

@Composable
private fun PromoPackageDetailModal(
    targetPackageItem: EventPackage?,
    isApplied: Boolean,
    onApplyPackage: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    var retainedPackage by remember { mutableStateOf(targetPackageItem) }
    if (targetPackageItem != null) {
        retainedPackage = targetPackageItem
    }

    RfCenteredAnimatedAssistantModal(
        visible = targetPackageItem != null,
        zIndexOrder = 8f,
        onDismiss = onDismiss,
    ) {
        val packageItem = retainedPackage ?: return@RfCenteredAnimatedAssistantModal
        val cardInteraction = remember { MutableInteractionSource() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .clickable(
                    interactionSource = cardInteraction,
                    indication = null,
                    onClick = {},
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(106.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFDE6F2)),
            ) {
                RfPhoto(
                    photoKey = photoAssetToRfPhotoKey(packageItem.photo),
                    contentDescription = packageItem.name,
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                if (packageItem.savingsDop > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        BasicText(
                            text = "Ahorra RD$ ${packageItem.savingsDop}",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }
            }
            BasicText(
                text = packageItem.name,
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = palette.ink,
                ),
            )
            BasicText(
                text = "${packageItem.subtitle} · Sugerido para ${packageItem.suggestedGuests} invitados",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = palette.muted,
                ),
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
            BasicText(
                text = "LO QUE SE ELEGIRÁ PARA TU EVENTO:",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF7A4FD8),
                ),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                packageItem.includedItems.forEach { lineItem ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFE8FD)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF7A4FD8), size = 12.dp)
                        }
                        BasicText(
                            text = lineItem,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                color = palette.ink,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "Precio paquete:",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted),
                )
                BasicText(
                    text = formatDopCurrency(packageItem.priceDop),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF7A4FD8)),
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
                        .background(palette.chipBg)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Cerrar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RosaButtonGradient)
                        .clickable(onClick = onApplyPackage),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (isApplied) "Aplicar de nuevo" else "Elegir paquete",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderRosaSuggestionsS29(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val catalogArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val balloonProducts = viewModel?.balloonProducts?.collectAsState()?.value ?: CanonicalSampleData.balloonProducts
    val packagesPool = viewModel?.packages?.collectAsState()?.value ?: CanonicalSampleData.packages
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value.orEmpty()
    val vmSelectedPackageId = viewModel?.selectedPackageId?.collectAsState()?.value
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val activeEventType = vmEventTypeInput.ifBlank { vmRosaEventType }.ifBlank { "Boda" }

    var suggestionBatch by remember { mutableStateOf(0) }
    var hasRequestedConversationalPrompt by remember { mutableStateOf(false) }
    var fallbackQuantities by remember { mutableStateOf(emptyMap<String, Int>()) }
    var fallbackPackageId by remember { mutableStateOf<String?>(null) }
    var showSelectedSummaryModal by remember { mutableStateOf(false) }
    var inspectedPromoPackage by remember { mutableStateOf<EventPackage?>(null) }
    var promptText by remember { mutableStateOf("") }

    val activeQuantities = if (viewModel != null) draftQuantities else fallbackQuantities
    val activePackageId = if (viewModel != null) vmSelectedPackageId else fallbackPackageId

    val isPreviewMode = viewModel == null
    val expandedArticlesPool = remember(catalogArticles, balloonProducts, activeEventType, isPreviewMode) {
        val merged = mergeCatalogArticlesWithWizardOptions(
            catalogArticles = catalogArticles,
            balloonProducts = balloonProducts,
            includePreviewExtras = isPreviewMode,
        )
        val filtered = merged.filter { articleItem -> isArticleRelevantToEventType(articleItem, activeEventType) }
        filtered.ifEmpty { merged }
    }
    val allCatalogSuggestions = remember(expandedArticlesPool) {
        expandedArticlesPool.map { articleItem -> buildSuggestionUiFromCatalogArticle(articleItem) }
    }
    val selectedLines = remember(expandedArticlesPool, activeQuantities) {
        resolveSelectedWizardLines(expandedArticlesPool, activeQuantities)
    }
    val totalChosenUnits = selectedLines.sumOf { line -> line.quantity }

    val updateArticleQuantity: (String, Int) -> Unit = { articleId, nextQty ->
        val clamped = nextQty.coerceAtLeast(0)
        if (viewModel != null) {
            viewModel.setDraftQuantity(articleId, clamped)
        } else {
            fallbackQuantities = if (clamped == 0) {
                fallbackQuantities - articleId
            } else {
                fallbackQuantities + (articleId to clamped)
            }
        }
    }

    val displayedSuggestions = remember(
        allCatalogSuggestions,
        activeQuantities,
        suggestionBatch,
        hasRequestedConversationalPrompt,
    ) {
        if (allCatalogSuggestions.isEmpty()) {
            emptyList()
        } else {
            val prioritizedFromDraft = if (hasRequestedConversationalPrompt) {
                allCatalogSuggestions.filter { suggestionItem ->
                    val currentQty = activeQuantities[suggestionItem.articleId] ?: 0
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
            val pageSize = 5.coerceAtMost(orderedPool.size)
            val startIndex = (suggestionBatch * pageSize) % orderedPool.size
            val selectedItems = mutableListOf<RosaSuggestedArticleUi>()
            for (offsetIndex in 0 until pageSize) {
                val resolvedIndex = (startIndex + offsetIndex) % orderedPool.size
                selectedItems.add(orderedPool[resolvedIndex])
            }
            selectedItems
        }
    }

    val orderedPackages = remember(packagesPool, activeEventType) {
        orderEventPackages(packagesPool, activeEventType)
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
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                screenCode = "s29",
                cartCount = totalChosenUnits,
                viewModel = viewModel,
                onBack = { onSelectScreenCode("BACK:s30") },
                onCart = { showSelectedSummaryModal = true },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                RosaSuggestionsCarouselSection(
                    sectionTitle = "RECOMENDADOS PARA TI",
                    suggestions = displayedSuggestions,
                    activeQuantities = activeQuantities,
                    onUpdateQuantity = updateArticleQuantity,
                    secondaryChipLabel = "Otras ideas ↻",
                    onSecondaryChipClick = { suggestionBatch += 1 },
                )

                RosaPromosCarouselSection(
                    packages = orderedPackages,
                    activePackageId = activePackageId,
                    onInspectPromo = { packageItem -> inspectedPromoPackage = packageItem },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("s18") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            "Ver catálogo",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RosaButtonGradient)
                            .clickable { onSelectScreenCode("s31") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            "Continuar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                        )
                    }
                }

                val promptErrorS29 = rememberOptionalPromptValidationError(promptText = promptText)
                RfValidationErrorText(errorMessage = promptErrorS29)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(12.dp, CircleShape, spotColor = Color(0x26AA46A0))
                        .border(
                            width = 1.dp,
                            color = resolveAssistantErrorBorderColor(
                                hasError = promptErrorS29 != null,
                                defaultColor = Color(0xFFE7DEF5),
                            ),
                            shape = CircleShape,
                        )
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
                                onValueChange = { updatedText ->
                                    promptText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { submitS29Prompt() }),
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
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
                                .clickable {
                                    val canSendPrompt = if (promptText.isBlank()) {
                                        true
                                    } else {
                                        InputValidator.validateChatMessage(promptText) == null
                                    }
                                    if (canSendPrompt) {
                                        submitS29Prompt()
                                    }
                                },
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

        SelectedItemsSummaryModal(
            visible = showSelectedSummaryModal,
            selectedLines = selectedLines,
            onUpdateQuantity = updateArticleQuantity,
            onClearAll = {
                if (viewModel != null) {
                    viewModel.clearDraftCart()
                } else {
                    fallbackQuantities = emptyMap()
                    fallbackPackageId = null
                }
            },
            onOpenCatalog = {
                showSelectedSummaryModal = false
                onSelectScreenCode("s18")
            },
            onDismiss = { showSelectedSummaryModal = false },
        )

        PromoPackageDetailModal(
            targetPackageItem = inspectedPromoPackage,
            isApplied = inspectedPromoPackage?.id == activePackageId,
            onApplyPackage = {
                val pendingPromo = inspectedPromoPackage
                if (pendingPromo != null) {
                    fallbackPackageId = pendingPromo.id
                    viewModel?.applyPackageToDraftCart(pendingPromo)
                }
                inspectedPromoPackage = null
            },
            onDismiss = { inspectedPromoPackage = null },
        )
    }
}

@Composable
private fun RosaSuggestionArticleCard(
    suggestion: RosaSuggestedArticleUi,
    quantity: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val isAdded = quantity > 0
    Box(
        modifier = Modifier
            .width(178.dp)
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
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
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BasicText(
                    text = suggestion.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                )
                BasicText(
                    text = suggestion.subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = "RD$ ${suggestion.priceDop}",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                    )
                    if (isAdded) {
                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF6D6))
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable(onClick = onDecrement),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "−",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4E6A10)),
                                )
                            }
                            BasicText(
                                text = quantity.toString(),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF4E6A10)),
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4E6A10))
                                    .clickable(onClick = onIncrement),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "+",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White),
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .shadow(6.dp, CircleShape, spotColor = Color(0xB3865DEC))
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                .clickable(onClick = onAdd)
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 13.dp)
                            BasicText("Agregar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RosaPromoPackageCard(
    packageItem: EventPackage,
    isApplied: Boolean,
    onInspectPromo: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .width(214.dp)
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .clickable(onClick = onInspectPromo),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(Color(0xFFFDE6F2)),
            ) {
                RfPhoto(
                    photoKey = photoAssetToRfPhotoKey(packageItem.photo),
                    contentDescription = packageItem.name,
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                if (packageItem.savingsDop > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        BasicText(
                            text = "Ahorra RD$ ${packageItem.savingsDop}",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BasicText(
                    text = packageItem.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                )
                BasicText(
                    text = packageItem.subtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, lineHeight = 14.sp, color = RfThemeState.currentPalette.muted),
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = "RD$ ${packageItem.priceDop}",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                    )
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isApplied) {
                                    Brush.linearGradient(listOf(Color(0xFFEEF6D6), Color(0xFFEEF6D6)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                },
                            )
                            .clickable(onClick = onInspectPromo)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = if (isApplied) "✓ Ver detalle" else "Elegir promo",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isApplied) Color(0xFF4E6A10) else Color.White,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private data class RosaCategoryReviewSpec(
    val title: String,
    val iconKind: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
    val articles: List<CatalogArticle>,
)

private fun resolveCategoryVisualStyle(categoryTitle: String): Triple<RfIconKind, Color, Color> {
    val lower = categoryTitle.lowercase()
    val isSeatingCategory = lower.contains("mobiliario") || lower.contains("silla")
    val isTableCategory = lower.contains("mesa") && !lower.contains("dulce")
    val isFloralCategory = lower.contains("floral") || lower.contains("centro")
    val isBalloonOrArchCategory = lower.contains("arco") || lower.contains("globo")
    val isLightingCategory = lower.contains("ilumin") || lower.contains("sonido")
    val isLinensCategory = lower.contains("mantel") || lower.contains("cristal")
    val isStageCategory = lower.contains("pista") || lower.contains("tarima")
    return when {
        isSeatingCategory -> Triple(RfIconKind.STORE, Color(0xFFFFF2C9), Color(0xFF7A5700))
        isTableCategory -> Triple(RfIconKind.BOX, Color(0xFFEEF6D6), Color(0xFF4E6A10))
        isFloralCategory -> Triple(RfIconKind.SPARK, Color(0xFFFDE6F2), Color(0xFFC2338F))
        isBalloonOrArchCategory -> Triple(RfIconKind.PARTY, Color(0xFFFDE6F2), Color(0xFFC2338F))
        lower.contains("dulce") -> Triple(RfIconKind.GIFT, Color(0xFFFFF2C9), Color(0xFF7A5700))
        isLightingCategory -> Triple(RfIconKind.SPARKLE, Color(0xFFDDF4F3), Color(0xFF1F8A84))
        isLinensCategory -> Triple(RfIconKind.CATALOG, Color(0xFFEFE8FD), Color(0xFF7A4FD8))
        isStageCategory -> Triple(RfIconKind.STAR, Color(0xFFEEF6D6), Color(0xFF4E6A10))
        else -> Triple(RfIconKind.BOX, Color(0xFFEFE8FD), Color(0xFF7A4FD8))
    }
}

private fun buildRealCategoryReviewSpecs(
    catalogArticles: List<CatalogArticle>,
): List<RosaCategoryReviewSpec> {
    val grouped = linkedMapOf<String, MutableList<CatalogArticle>>()
    for (article in catalogArticles) {
        val isVisible = article.visibleInCatalog
        val categoryName = article.category.trim()
        val hasCategory = categoryName.isNotEmpty()
        val shouldInclude = if (isVisible) hasCategory else false
        if (shouldInclude) {
            val bucket = grouped.getOrPut(categoryName) { mutableListOf() }
            bucket.add(article)
        }
    }
    return grouped.entries.map { entry ->
        val (iconKind, iconBg, iconTint) = resolveCategoryVisualStyle(entry.key)
        RosaCategoryReviewSpec(
            title = entry.key,
            iconKind = iconKind,
            iconBg = iconBg,
            iconTint = iconTint,
            articles = entry.value.toList(),
        )
    }
}

@Composable
internal fun RenderRosaCategoriesS30(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val catalogArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val balloonProducts = viewModel?.balloonProducts?.collectAsState()?.value ?: CanonicalSampleData.balloonProducts
    val packagesPool = viewModel?.packages?.collectAsState()?.value ?: CanonicalSampleData.packages
    val vmDraftQuantities = viewModel?.draftQuantities?.collectAsState()?.value.orEmpty()
    val vmSelectedPackageId = viewModel?.selectedPackageId?.collectAsState()?.value
    val vmOmittedCategories = viewModel?.rosaOmittedCategories?.collectAsState()?.value
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val activeEventType = vmEventTypeInput.ifBlank { vmRosaEventType }.ifBlank { "Boda" }

    var fallbackQuantities by remember { mutableStateOf(emptyMap<String, Int>()) }
    var fallbackPackageId by remember { mutableStateOf<String?>(null) }
    var localOmittedCategories by remember { mutableStateOf(emptySet<String>()) }
    var activePickerCategoryTitle by remember { mutableStateOf<String?>(null) }
    var showSelectedSummaryModal by remember { mutableStateOf(false) }
    var inspectedPromoPackage by remember { mutableStateOf<EventPackage?>(null) }
    var promptText by remember { mutableStateOf("") }

    val activeQuantities = if (viewModel != null) vmDraftQuantities else fallbackQuantities
    val activePackageId = if (viewModel != null) vmSelectedPackageId else fallbackPackageId
    val activeOmittedCategories = vmOmittedCategories ?: localOmittedCategories

    val isPreviewMode = viewModel == null
    val expandedArticles = remember(catalogArticles, balloonProducts, isPreviewMode) {
        mergeCatalogArticlesWithWizardOptions(
            catalogArticles = catalogArticles,
            balloonProducts = balloonProducts,
            includePreviewExtras = isPreviewMode,
        )
    }
    val displayedCategories = remember(expandedArticles) {
        buildRealCategoryReviewSpecs(expandedArticles)
    }
    val selectedLines = remember(expandedArticles, activeQuantities) {
        resolveSelectedWizardLines(expandedArticles, activeQuantities)
    }
    val totalChosenUnits = selectedLines.sumOf { line -> line.quantity }
    val totalChosenDop = selectedLines.sumOf { line -> line.quantity * line.article.pricePerDayDop }

    val activePickerCategory = remember(displayedCategories, activePickerCategoryTitle) {
        val targetTitle = activePickerCategoryTitle
        if (targetTitle == null) {
            null
        } else {
            displayedCategories.firstOrNull { categorySpec -> categorySpec.title == targetTitle }
        }
    }

    val updateCategoryArticleQuantity: (String, Int) -> Unit = { articleId, nextQuantity ->
        val clamped = nextQuantity.coerceAtLeast(0)
        if (viewModel != null) {
            viewModel.setDraftQuantity(articleId, clamped)
        } else {
            fallbackQuantities = if (clamped == 0) {
                fallbackQuantities - articleId
            } else {
                fallbackQuantities + (articleId to clamped)
            }
        }
    }

    val hasPromptText = promptText.isNotBlank()
    val submitS30Prompt = {
        if (promptText.isNotBlank()) {
            val cleanPrompt = promptText.trim()
            applyNaturalLanguageEventPrompt(cleanPrompt, viewModel)
            viewModel?.sendConversationalRosaPrompt(cleanPrompt)
            promptText = ""
        } else {
            openRosaVoiceScreen(
                viewModel = viewModel,
                returnScreenCode = "s30",
                onSelectScreenCode = onSelectScreenCode,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                screenCode = if (activePickerCategory != null) "s30_picker" else "s30",
                cartCount = totalChosenUnits,
                activeCategoryTitle = activePickerCategory?.title.orEmpty(),
                viewModel = viewModel,
                onBack = {
                    if (activePickerCategory != null) {
                        activePickerCategoryTitle = null
                    } else {
                        onSelectScreenCode("BACK:s27")
                    }
                },
                onCart = { showSelectedSummaryModal = true },
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (activePickerCategory != null) {
                    val categorySuggestions = remember(activePickerCategory) {
                        activePickerCategory.articles.map { articleItem ->
                            buildSuggestionUiFromCatalogArticle(articleItem)
                        }
                    }
                    val categoryPackages = remember(packagesPool, activeEventType, activePickerCategory) {
                        orderEventPackages(packagesPool, activeEventType, activePickerCategory.title)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        RosaSuggestionsCarouselSection(
                            sectionTitle = activePickerCategory.title.uppercase(),
                            suggestions = categorySuggestions,
                            activeQuantities = activeQuantities,
                            onUpdateQuantity = updateCategoryArticleQuantity,
                        )

                        RosaPromosCarouselSection(
                            packages = categoryPackages,
                            activePackageId = activePackageId,
                            onInspectPromo = { packageItem -> inspectedPromoPackage = packageItem },
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        SelectedSummaryBanner(
                            distinctCount = selectedLines.size,
                            totalUnits = totalChosenUnits,
                            totalDop = totalChosenDop,
                            onOpenSummary = { showSelectedSummaryModal = true },
                        )

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
                                val chosenUnitsInCategory = category.articles.sumOf { item -> activeQuantities[item.id] ?: 0 }
                                val chosenItemKinds = category.articles.count { item -> (activeQuantities[item.id] ?: 0) > 0 }
                                val isOmitted = category.title in activeOmittedCategories
                                val hasSelectedUnits = chosenUnitsInCategory > 0
                                val isChosen = if (isOmitted) false else hasSelectedUnits

                                val resolvedSubtitle = when {
                                    isOmitted -> "Omitida"
                                    isChosen -> "$chosenItemKinds artículo(s) · $chosenUnitsInCategory uds"
                                    else -> "${category.articles.size} opciones disponibles"
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
                                            for (item in category.articles) {
                                                updateCategoryArticleQuantity(item.id, 0)
                                            }
                                        }
                                    },
                                    onChoose = {
                                        if (isOmitted) {
                                            viewModel?.toggleRosaOmittedCategory(category.title)
                                            localOmittedCategories = localOmittedCategories - category.title
                                        }
                                        activePickerCategoryTitle = category.title
                                    },
                                    onToggleChosen = {
                                        activePickerCategoryTitle = category.title
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (activePickerCategory != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
                                .clip(RoundedCornerShape(16.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .clickable { activePickerCategoryTitle = null },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                "Volver",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                                .clip(RoundedCornerShape(16.dp))
                                .background(RosaButtonGradient)
                                .clickable { activePickerCategoryTitle = null },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                "Listo",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
                                .clip(RoundedCornerShape(16.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .clickable { onSelectScreenCode("s18") },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Ver catálogo",
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
                }

                val promptErrorS30 = rememberOptionalPromptValidationError(promptText = promptText)
                RfValidationErrorText(errorMessage = promptErrorS30)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(
                            width = 1.dp,
                            color = resolveAssistantErrorBorderColor(
                                hasError = promptErrorS30 != null,
                                defaultColor = Color(0xFFE7DEF5),
                            ),
                            shape = CircleShape,
                        )
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
                            onValueChange = { updatedText ->
                                promptText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitS30Prompt() }),
                            cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
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
                            .clickable {
                                val canSendPrompt = if (promptText.isBlank()) {
                                    true
                                } else {
                                    InputValidator.validateChatMessage(promptText) == null
                                }
                                if (canSendPrompt) {
                                    submitS30Prompt()
                                }
                            },
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

        SelectedItemsSummaryModal(
            visible = showSelectedSummaryModal,
            selectedLines = selectedLines,
            onUpdateQuantity = updateCategoryArticleQuantity,
            onClearAll = {
                if (viewModel != null) {
                    viewModel.clearDraftCart()
                } else {
                    fallbackQuantities = emptyMap()
                }
            },
            onOpenCatalog = {
                showSelectedSummaryModal = false
                onSelectScreenCode("s18")
            },
            onDismiss = { showSelectedSummaryModal = false },
        )

        PromoPackageDetailModal(
            targetPackageItem = inspectedPromoPackage,
            isApplied = inspectedPromoPackage?.id == activePackageId,
            onApplyPackage = {
                val pendingPromo = inspectedPromoPackage
                if (pendingPromo != null) {
                    fallbackPackageId = pendingPromo.id
                    viewModel?.applyPackageToDraftCart(pendingPromo)
                }
                inspectedPromoPackage = null
            },
            onDismiss = { inspectedPromoPackage = null },
        )
    }
}

@Composable
private fun SelectedSummaryBanner(
    distinctCount: Int,
    totalUnits: Int,
    totalDop: Int,
    onOpenSummary: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val hasSelections = totalUnits > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
            .clip(RoundedCornerShape(18.dp))
            .background(RfThemeState.currentPalette.surface)
            .clickable(onClick = onOpenSummary)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (hasSelections) Color(0xFFEEF6D6) else Color(0xFFEFE8FD)),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(
                kind = RfIconKind.ORDERS,
                tint = if (hasSelections) Color(0xFF4E6A10) else Color(0xFF7A4FD8),
                size = 19.dp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = if (hasSelections) "Lo que llevas elegido" else "Lista de artículos",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = RfThemeState.currentPalette.ink,
                ),
            )
            BasicText(
                text = if (hasSelections) {
                    "$distinctCount artículo(s) · $totalUnits uds · RD$ $totalDop"
                } else {
                    "Elige categorías o revisa tu lista aquí"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 12.sp,
                    color = if (hasSelections) Color(0xFF4E6A10) else RfThemeState.currentPalette.muted,
                ),
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (hasSelections) {
                        Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFF3EBFF), Color(0xFFF3EBFF)))
                    },
                )
                .clickable(onClick = onOpenSummary)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            BasicText(
                text = if (hasSelections) "Ver elegidos ($totalUnits)" else "Ver elegidos",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (hasSelections) Color.White else Color(0xFF7A4FD8),
                ),
            )
        }
    }
}

@Composable
private fun SelectedItemsSummaryModal(
    visible: Boolean,
    selectedLines: List<SelectedWizardArticleLine>,
    onUpdateQuantity: (String, Int) -> Unit,
    onClearAll: () -> Unit,
    onOpenCatalog: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val totalUnits = selectedLines.sumOf { line -> line.quantity }
    val totalDop = selectedLines.sumOf { line -> line.quantity * line.article.pricePerDayDop }

    RfCenteredAnimatedAssistantModal(
        visible = visible,
        zIndexOrder = 6f,
        onDismiss = onDismiss,
    ) {
        val cardInteraction = remember { MutableInteractionSource() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 28.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(palette.surface)
                .clickable(
                    interactionSource = cardInteraction,
                    indication = null,
                    onClick = {},
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Artículos elegidos",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = palette.ink),
                    )
                    BasicText(
                        text = "$totalUnits unidad(es) seleccionadas",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = palette.muted),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = palette.ink, size = 16.dp)
                }
            }

            if (selectedLines.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.chipBg)
                        .padding(horizontal = 16.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF945AE1), size = 28.dp)
                    BasicText(
                        text = "Aún no has elegido artículos",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink),
                    )
                    BasicText(
                        text = "Toca \"Elegir\" en cualquier categoría o explora el catálogo para armar tu lista.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = palette.muted, textAlign = TextAlign.Center),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 310.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    selectedLines.forEach { line ->
                        val article = line.article
                        val lineTotal = article.pricePerDayDop * line.quantity
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.chipBg)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RfPhoto(
                                photoKey = photoAssetToRfPhotoKey(article.photo),
                                contentDescription = article.name,
                                cornerRadius = 10.dp,
                                modifier = Modifier.size(48.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = article.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = palette.ink),
                                )
                                BasicText(
                                    text = "${article.category} · RD$ ${article.pricePerDayDop}/u · Sub RD$ $lineTotal",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = palette.muted),
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(palette.surface)
                                        .border(1.dp, palette.lineSoft, CircleShape)
                                        .clickable { onUpdateQuantity(article.id, line.quantity - 1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("−", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink))
                                }
                                BasicText(
                                    text = line.quantity.toString(),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = palette.ink),
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF945AE1))
                                        .clickable { onUpdateQuantity(article.id, line.quantity + 1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("+", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3EBFF))
                                        .clickable { onUpdateQuantity(article.id, line.quantity + 10) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                ) {
                                    BasicText("+10", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF7A4FD8)))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = "Subtotal estimado",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4E6A10)),
                    )
                    BasicText(
                        text = "RD$ $totalDop",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF4E6A10)),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (selectedLines.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFDE6F2))
                            .clickable(onClick = onClearAll)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Vaciar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC2338F)),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.chipBg)
                            .clickable(onClick = onOpenCatalog),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Ver catálogo",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RosaButtonGradient)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Listo",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                    )
                }
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
private fun WizardScheduleEditorSection(
    scheduleItems: List<EventTimelineItemUiModel>,
    onAddMoment: (String, String, String) -> Unit,
    onRemoveMoment: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    var startTimeInput by remember { mutableStateOf("") }
    var endTimeInput by remember { mutableStateOf("") }
    var momentTitleInput by remember { mutableStateOf("") }
    var hasInteractedStart by remember { mutableStateOf(false) }
    var hasInteractedEnd by remember { mutableStateOf(false) }
    var hasInteractedMomentTitle by remember { mutableStateOf(false) }
    var forceShowScheduleErrors by remember { mutableStateOf(false) }

    val validateEndTimeWithRange: (String) -> String? = { candidateEnd ->
        val formatErr = InputValidator.validateTime24(candidateEnd, fieldLabel = "La hora de fin")
        if (formatErr != null) {
            formatErr
        } else {
            InputValidator.validateTimeRange(startTimeInput, candidateEnd)
        }
    }
    val startTimeError = rememberDebouncedValidationError(
        value = startTimeInput,
        hasInteracted = hasInteractedStart,
        forceShowError = forceShowScheduleErrors,
    ) { candidate ->
        InputValidator.validateTime24(candidate, fieldLabel = "La hora de inicio")
    }
    val endTimeError = rememberDebouncedValidationError(
        value = "$startTimeInput|$endTimeInput",
        hasInteracted = hasInteractedEnd,
        forceShowError = forceShowScheduleErrors,
    ) { _ ->
        validateEndTimeWithRange(endTimeInput)
    }
    val momentTitleError = rememberDebouncedValidationError(
        value = momentTitleInput,
        hasInteracted = hasInteractedMomentTitle,
        forceShowError = forceShowScheduleErrors,
    ) { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "La actividad",
            minLength = 2,
            maxLength = InputValidator.MAX_EVENT_NAME_LENGTH,
            isRequired = true,
        )
    }
    val combinedScheduleError = listOfNotNull(startTimeError, endTimeError, momentTitleError).firstOrNull()
    val presetMoments = remember {
        listOf(
            Triple("17:30", "18:30", "Ceremonia"),
            Triple("18:30", "19:30", "Cóctel"),
            Triple("20:00", "21:30", "Cena y brindis"),
            Triple("21:30", "23:30", "Baile / Hora loca"),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
            .clip(RoundedCornerShape(24.dp))
            .background(palette.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "HORARIOS DEL EVENTO",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF7A4FD8),
                ),
            )
            BasicText(
                text = "${scheduleItems.size} definido(s)",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = palette.muted,
                ),
            )
        }
        BasicText(
            text = "Define a qué hora sucederá cada momento de tu evento.",
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 12.sp,
                color = palette.muted,
            ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presetMoments.forEach { preset ->
                val isAlreadyAdded = scheduleItems.any { item ->
                    item.title.equals(preset.third, ignoreCase = true)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isAlreadyAdded) Color(0xFFEEF6D6) else Color(0xFFEFE8FD))
                        .clickable {
                            if (!isAlreadyAdded) {
                                onAddMoment(preset.first, preset.second, preset.third)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    BasicText(
                        text = if (isAlreadyAdded) "✓ ${preset.third}" else "+ ${preset.third} (${preset.first})",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isAlreadyAdded) Color(0xFF4E6A10) else Color(0xFF7A4FD8),
                        ),
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.chipBg)
                        .border(
                            width = 1.dp,
                            color = resolveAssistantErrorBorderColor(
                                hasError = startTimeError != null,
                                defaultColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (startTimeInput.isEmpty()) {
                        BasicText("18:00", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                    }
                    BasicTextField(
                        value = startTimeInput,
                        onValueChange = { updated ->
                            startTimeInput = InputValidator.filterTimeInput(updated)
                            hasInteractedStart = true
                            forceShowScheduleErrors = false
                        },
                        singleLine = true,
                        cursorBrush = SolidColor(palette.primaryAccent),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = palette.ink),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.chipBg)
                        .border(
                            width = 1.dp,
                            color = resolveAssistantErrorBorderColor(
                                hasError = endTimeError != null,
                                defaultColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (endTimeInput.isEmpty()) {
                        BasicText("19:30", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                    }
                    BasicTextField(
                        value = endTimeInput,
                        onValueChange = { updated ->
                            endTimeInput = InputValidator.filterTimeInput(updated)
                            hasInteractedEnd = true
                            forceShowScheduleErrors = false
                        },
                        singleLine = true,
                        cursorBrush = SolidColor(palette.primaryAccent),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = palette.ink),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.chipBg)
                        .border(
                            width = 1.dp,
                            color = resolveAssistantErrorBorderColor(
                                hasError = momentTitleError != null,
                                defaultColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (momentTitleInput.isEmpty()) {
                        BasicText("Actividad...", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                    }
                    BasicTextField(
                        value = momentTitleInput,
                        onValueChange = { updated ->
                            momentTitleInput = updated.take(InputValidator.MAX_EVENT_NAME_LENGTH)
                            hasInteractedMomentTitle = true
                            forceShowScheduleErrors = false
                        },
                        singleLine = true,
                        cursorBrush = SolidColor(palette.primaryAccent),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.ink),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RosaButtonGradient)
                        .clickable {
                            val immediateStartError = InputValidator.validateTime24(startTimeInput, fieldLabel = "La hora de inicio")
                            val immediateEndError = validateEndTimeWithRange(endTimeInput)
                            val immediateTitleError = InputValidator.validateEntityName(
                                rawValue = momentTitleInput,
                                fieldLabel = "La actividad",
                                minLength = 2,
                                maxLength = InputValidator.MAX_EVENT_NAME_LENGTH,
                                isRequired = true,
                            )
                            val firstImmediateError = listOfNotNull(
                                immediateStartError,
                                immediateEndError,
                                immediateTitleError,
                            ).firstOrNull()
                            if (firstImmediateError != null) {
                                hasInteractedStart = true
                                hasInteractedEnd = true
                                hasInteractedMomentTitle = true
                                forceShowScheduleErrors = true
                            } else {
                                onAddMoment(startTimeInput.trim(), endTimeInput.trim(), momentTitleInput.trim())
                                momentTitleInput = ""
                                hasInteractedMomentTitle = false
                                forceShowScheduleErrors = false
                            }
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Agregar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White),
                    )
                }
            }
            RfValidationErrorText(errorMessage = combinedScheduleError)
        }
        if (scheduleItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scheduleItems.forEach { blockItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.chipBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val formattedStart = InputValidator.formatTimeDisplay12h(blockItem.startTime)
                        val formattedEnd = InputValidator.formatTimeDisplay12h(blockItem.endTime)
                        BasicText(
                            text = "$formattedStart - $formattedEnd · ${blockItem.title}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = palette.ink),
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicText(
                            text = "Quitar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFC2338F)),
                            modifier = Modifier.clickable { onRemoveMoment(blockItem.id) },
                        )
                    }
                }
            }
        }
    }
}

private data class RosaEventTypeOption(
    val label: String,
    val titlePrefix: String,
    val iconKind: RfIconKind,
    val accentColor: Color,
    val tintBackground: Color,
)

private val rosaEventTypeOptions = listOf(
    RosaEventTypeOption(
        label = "Boda",
        titlePrefix = "Boda de ",
        iconKind = RfIconKind.RING,
        accentColor = Color(0xFFC2338F),
        tintBackground = Color(0xFFFFF0F8),
    ),
    RosaEventTypeOption(
        label = "Cumpleaños",
        titlePrefix = "Cumpleaños de ",
        iconKind = RfIconKind.CAKE,
        accentColor = Color(0xFFD97706),
        tintBackground = Color(0xFFFFFBEB),
    ),
    RosaEventTypeOption(
        label = "Baby shower",
        titlePrefix = "Baby shower de ",
        iconKind = RfIconKind.GIFT,
        accentColor = Color(0xFF0284C7),
        tintBackground = Color(0xFFF0F9FF),
    ),
    RosaEventTypeOption(
        label = "Gender reveal",
        titlePrefix = "Gender reveal de ",
        iconKind = RfIconKind.PARTY,
        accentColor = Color(0xFF7A4FD8),
        tintBackground = Color(0xFFF5F3FF),
    ),
    RosaEventTypeOption(
        label = "Quinceañera",
        titlePrefix = "Quinceañera de ",
        iconKind = RfIconKind.SPARKLE,
        accentColor = Color(0xFFDB2777),
        tintBackground = Color(0xFFFDF2F8),
    ),
    RosaEventTypeOption(
        label = "Graduación",
        titlePrefix = "Graduación de ",
        iconKind = RfIconKind.STAR,
        accentColor = Color(0xFF4F46E5),
        tintBackground = Color(0xFFEEF2FF),
    ),
    RosaEventTypeOption(
        label = "Corporativo",
        titlePrefix = "Evento corporativo ",
        iconKind = RfIconKind.BAR_CHART,
        accentColor = Color(0xFF0F766E),
        tintBackground = Color(0xFFF0FDFA),
    ),
    RosaEventTypeOption(
        label = "Otro",
        titlePrefix = "",
        iconKind = RfIconKind.PLUS,
        accentColor = Color(0xFF64748B),
        tintBackground = Color(0xFFF8FAFC),
    ),
)

private fun findRosaEventTypeOption(eventType: String): RosaEventTypeOption? {
    val cleanType = eventType.trim()
    if (cleanType.isEmpty()) return null
    return rosaEventTypeOptions.firstOrNull { option ->
        option.label.equals(cleanType, ignoreCase = true)
    }
}

private fun isCustomEventTypeSelection(
    eventType: String,
    isOtherModeSelected: Boolean,
): Boolean {
    if (isOtherModeSelected) return true
    val cleanType = eventType.trim()
    if (cleanType.isEmpty()) return false
    val matchedPreset = findRosaEventTypeOption(cleanType) ?: return true
    return matchedPreset.label == "Otro"
}

private fun resolveNextAutoFilledTitle(
    currentTitle: String,
    previousAutoPrefix: String,
    selectedOption: RosaEventTypeOption,
): String? {
    val nextPrefix = selectedOption.titlePrefix
    if (nextPrefix.isEmpty()) return null
    if (currentTitle.isBlank()) return nextPrefix
    val matchesPreviousAuto = if (previousAutoPrefix.isNotEmpty()) {
        currentTitle == previousAutoPrefix
    } else {
        false
    }
    if (matchesPreviousAuto) return nextPrefix
    val matchesAnyPresetPrefix = rosaEventTypeOptions.any { option ->
        if (option.titlePrefix.isNotEmpty()) {
            currentTitle == option.titlePrefix
        } else {
            false
        }
    }
    if (matchesAnyPresetPrefix) return nextPrefix
    return null
}

@Composable
internal fun RenderRosaDetailsS31(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val vmClientTitle = viewModel?.clientEventTitleInput?.collectAsState()?.value.orEmpty()
    val vmEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val vmRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val vmEventDateInput = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty()
    val vmEventVenueInput = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty()
    val vmEventGuestsInput = viewModel?.eventGuestsInput?.collectAsState()?.value.orEmpty()
    val vmBudgetDop = viewModel?.rosaBudgetDop?.collectAsState()?.value ?: 0
    val vmEventNotesInput = viewModel?.eventNotesInput?.collectAsState()?.value.orEmpty()
    val isVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val isVoiceTranscribing = viewModel?.isVoiceTranscribing?.collectAsState()?.value ?: false
    val isRemoteVoiceTranscribing = viewModel?.isRemoteVoiceTranscribing?.collectAsState()?.value ?: false
    val activeVoiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
    val voiceHardwareError = viewModel?.voiceErrorMessage?.collectAsState()?.value

    val initialEventType = vmEventTypeInput.ifBlank { vmRosaEventType }

    var eventTitleValue by remember(vmClientTitle) {
        mutableStateOf(vmClientTitle.take(InputValidator.MAX_EVENT_NAME_LENGTH))
    }
    var eventTypeValue by remember(initialEventType) {
        mutableStateOf(initialEventType.take(InputValidator.MAX_EVENT_NAME_LENGTH))
    }
    var lastAutoTitlePrefix by remember { mutableStateOf("") }
    var isOtherTypeMode by remember(initialEventType) {
        mutableStateOf(
            isCustomEventTypeSelection(
                eventType = initialEventType,
                isOtherModeSelected = false,
            ),
        )
    }
    var dateValue by remember(vmEventDateInput) { mutableStateOf(vmEventDateInput) }
    var showEventCalendar by remember { mutableStateOf(false) }
    var showInAppMapModal by remember { mutableStateOf(false) }
    var guestsValue by remember(vmEventGuestsInput) {
        mutableStateOf(InputValidator.filterDigitsOnly(vmEventGuestsInput, 5))
    }
    var budgetValue by remember(vmBudgetDop) {
        mutableStateOf(if (vmBudgetDop > 0) vmBudgetDop.toString().take(8) else "")
    }
    var venueValue by remember(vmEventVenueInput) {
        mutableStateOf(vmEventVenueInput.take(InputValidator.MAX_ADDRESS_LENGTH))
    }
    var ideaValue by remember(vmEventNotesInput) {
        mutableStateOf(vmEventNotesInput.take(InputValidator.MAX_NOTES_LENGTH))
    }
    var promptText by remember { mutableStateOf("") }

    var hasInteractedTitle by remember { mutableStateOf(false) }
    var hasInteractedType by remember { mutableStateOf(false) }
    var hasInteractedDate by remember { mutableStateOf(false) }
    var hasInteractedGuests by remember { mutableStateOf(false) }
    var hasInteractedBudget by remember { mutableStateOf(false) }
    var hasInteractedVenue by remember { mutableStateOf(false) }
    var hasInteractedIdea by remember { mutableStateOf(false) }
    var forceShowFormErrors by remember { mutableStateOf(false) }
    var isBudgetFocused by remember { mutableStateOf(false) }

    val validateTitleS31: (String) -> String? = { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "El título del evento",
            minLength = 2,
            maxLength = InputValidator.MAX_EVENT_NAME_LENGTH,
            isRequired = false,
        )
    }
    val validateTypeS31: (String) -> String? = { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "El tipo de evento",
            minLength = 2,
            maxLength = InputValidator.MAX_EVENT_NAME_LENGTH,
            isRequired = true,
        )
    }
    val validateDateS31: (String) -> String? = { candidate ->
        InputValidator.validateEventDate(candidate, isRequired = false)
    }
    val validateGuestsS31: (String) -> String? = { candidate ->
        InputValidator.validateGuestCount(candidate, isRequired = false)
    }
    val validateBudgetS31: (String) -> String? = { candidate ->
        InputValidator.validateMoneyAmount(
            rawValue = candidate,
            fieldLabel = "El presupuesto",
            isRequired = false,
            minAmount = 100.0,
            maxAmount = InputValidator.MAX_MONEY_AMOUNT,
        )
    }
    val validateVenueS31: (String) -> String? = { candidate ->
        InputValidator.validateAddress(
            rawValue = candidate,
            fieldLabel = "La ubicación",
            isRequired = false,
        )
    }
    val validateIdeaS31: (String) -> String? = { candidate ->
        InputValidator.validateNotes(
            rawValue = candidate,
            fieldLabel = "Las notas",
            isRequired = false,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }

    val titleError = rememberDebouncedValidationError(
        value = eventTitleValue,
        hasInteracted = hasInteractedTitle,
        forceShowError = forceShowFormErrors,
        validator = validateTitleS31,
    )
    val typeError = rememberDebouncedValidationError(
        value = eventTypeValue,
        hasInteracted = hasInteractedType,
        forceShowError = forceShowFormErrors,
        validator = validateTypeS31,
    )
    val dateError = rememberDebouncedValidationError(
        value = dateValue,
        hasInteracted = hasInteractedDate,
        forceShowError = forceShowFormErrors,
        validator = validateDateS31,
    )
    val guestsError = rememberDebouncedValidationError(
        value = guestsValue,
        hasInteracted = hasInteractedGuests,
        forceShowError = forceShowFormErrors,
        validator = validateGuestsS31,
    )
    val budgetError = rememberDebouncedValidationError(
        value = budgetValue,
        hasInteracted = hasInteractedBudget,
        forceShowError = forceShowFormErrors,
        validator = validateBudgetS31,
    )
    val venueError = rememberDebouncedValidationError(
        value = venueValue,
        hasInteracted = hasInteractedVenue,
        forceShowError = forceShowFormErrors,
        validator = validateVenueS31,
    )
    val ideaError = rememberDebouncedValidationError(
        value = ideaValue,
        hasInteracted = hasInteractedIdea,
        forceShowError = forceShowFormErrors,
        validator = validateIdeaS31,
    )
    val promptErrorS31 = rememberOptionalPromptValidationError(promptText = promptText)

    val matchedPresetOption = findRosaEventTypeOption(eventTypeValue)
    val eventTypeHeaderIcon = if (eventTypeValue.isBlank()) {
        RfIconKind.PARTY
    } else {
        matchedPresetOption?.iconKind ?: resolveRosaEventMetadata(eventTypeValue).iconKind
    }
    val showCustomTypeInput = isCustomEventTypeSelection(
        eventType = eventTypeValue,
        isOtherModeSelected = isOtherTypeMode,
    )
    val isNotesOrigin = activeVoiceOrigin == VoiceInputOrigin.NOTES_INLINE
    val isNotesMicListening = if (isVoiceListening) isNotesOrigin else false
    val isAnyTranscribing = if (isVoiceTranscribing) true else isRemoteVoiceTranscribing
    val isNotesMicTranscribing = if (isAnyTranscribing) isNotesOrigin else false

    val hasPromptText = promptText.isNotBlank()
    val submitS31Prompt = {
        if (promptText.isNotBlank()) {
            if (InputValidator.validateChatMessage(promptText) == null) {
                val cleanPrompt = promptText.trim()
                val parsed = applyNaturalLanguageEventPrompt(cleanPrompt, viewModel)
                viewModel?.sendConversationalRosaPrompt(cleanPrompt)
                if (parsed.eventType != null) {
                    val cappedParsedType = parsed.eventType.take(InputValidator.MAX_EVENT_NAME_LENGTH)
                    eventTypeValue = cappedParsedType
                    hasInteractedType = true
                    isOtherTypeMode = isCustomEventTypeSelection(cappedParsedType, false)
                }
                if (parsed.dateLabel != null) {
                    dateValue = parsed.dateLabel
                    hasInteractedDate = true
                }
                if (parsed.guestsCount != null) {
                    val cleanGuests = InputValidator.filterDigitsOnly(parsed.guestsCount.toString(), 5)
                    guestsValue = cleanGuests
                    hasInteractedGuests = true
                    viewModel?.setEventGuestsInput(cleanGuests)
                }
                if (parsed.venueLabel != null) {
                    venueValue = parsed.venueLabel.take(InputValidator.MAX_ADDRESS_LENGTH)
                    hasInteractedVenue = true
                }
                val mergedIdea = if (ideaValue.isBlank()) cleanPrompt else "${ideaValue.trim()}\n$cleanPrompt"
                val cappedIdea = mergedIdea.take(InputValidator.MAX_NOTES_LENGTH)
                ideaValue = cappedIdea
                hasInteractedIdea = true
                viewModel?.setEventNotesInput(cappedIdea)
                promptText = ""
            }
        } else {
            viewModel?.toggleInlineVoiceNotes(ideaValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                screenCode = "s31",
                viewModel = viewModel,
                onBack = { onSelectScreenCode("BACK:s30") },
                onCart = { onSelectScreenCode("s25") },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = false)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RfThemeState.currentPalette.surface),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RfIcon(
                                kind = eventTypeHeaderIcon,
                                tint = matchedPresetOption?.accentColor ?: RfThemeState.currentPalette.muted,
                                size = 20.dp,
                            )
                            Column {
                                BasicText(
                                    text = "Tipo de evento",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RfThemeState.currentPalette.ink,
                                    ),
                                )
                                BasicText(
                                    text = "Elige el estilo para sugerirte el título ideal",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        color = RfThemeState.currentPalette.muted,
                                    ),
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rosaEventTypeOptions.chunked(2).forEach { optionRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    optionRow.forEach { option ->
                                        val isOtherChip = option.label == "Otro"
                                        val matchesOptionLabel = option.label.equals(eventTypeValue.trim(), ignoreCase = true)
                                        val isPresetSelected = if (!isOtherTypeMode) matchesOptionLabel else false
                                        val isSelected = if (isOtherChip) {
                                            showCustomTypeInput
                                        } else {
                                            isPresetSelected
                                        }
                                        val palette = RfThemeState.currentPalette
                                        val selectedChipBg = if (palette.isDark) {
                                            option.accentColor.copy(alpha = 0.22f)
                                        } else {
                                            option.tintBackground
                                        }
                                        val chipBg = if (isSelected) {
                                            selectedChipBg
                                        } else {
                                            palette.chipBg
                                        }
                                        val chipBorderColor = if (isSelected) {
                                            option.accentColor
                                        } else {
                                            palette.line
                                        }
                                        val badgeBg = if (isSelected) {
                                            option.accentColor
                                        } else if (palette.isDark) {
                                            option.accentColor.copy(alpha = 0.20f)
                                        } else {
                                            option.tintBackground
                                        }
                                        val badgeIconTint = if (isSelected) {
                                            Color.White
                                        } else {
                                            option.accentColor
                                        }
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .clip(RoundedCornerShape(13.dp))
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = chipBorderColor,
                                                    shape = RoundedCornerShape(13.dp),
                                                )
                                                .background(chipBg)
                                                .clickable {
                                                    hasInteractedType = true
                                                    forceShowFormErrors = false
                                                    if (isOtherChip) {
                                                        isOtherTypeMode = true
                                                        val currentPreset = findRosaEventTypeOption(eventTypeValue)
                                                        if (currentPreset != null) {
                                                            eventTypeValue = ""
                                                            viewModel?.setEventTypeInput("")
                                                        }
                                                    } else {
                                                        isOtherTypeMode = false
                                                        eventTypeValue = option.label
                                                        viewModel?.setEventTypeInput(option.label)
                                                        viewModel?.updateRosaParameters(eventType = option.label)
                                                        val autoTitle = resolveNextAutoFilledTitle(
                                                            currentTitle = eventTitleValue,
                                                            previousAutoPrefix = lastAutoTitlePrefix,
                                                            selectedOption = option,
                                                        )
                                                        if (autoTitle != null) {
                                                            val cappedAuto = autoTitle.take(InputValidator.MAX_EVENT_NAME_LENGTH)
                                                            eventTitleValue = cappedAuto
                                                            lastAutoTitlePrefix = cappedAuto
                                                            viewModel?.setClientEventTitleInput(cappedAuto)
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(badgeBg),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                RfIcon(
                                                    kind = option.iconKind,
                                                    tint = badgeIconTint,
                                                    size = 13.dp,
                                                )
                                            }
                                            BasicText(
                                                text = option.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 12.5.sp,
                                                    color = if (isSelected) option.accentColor else RfThemeState.currentPalette.ink,
                                                ),
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }
                                    if (optionRow.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        if (showCustomTypeInput) {
                            val palette = RfThemeState.currentPalette
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, palette.fieldBorder, RoundedCornerShape(12.dp))
                                    .background(palette.chipBg)
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RfIcon(kind = RfIconKind.PARTY, tint = Color(0xFF7A4FD8), size = 16.dp)
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (eventTypeValue.isEmpty()) {
                                        BasicText(
                                            text = "Escribe qué tipo de evento es (ej: Bautizo)",
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 13.5.sp,
                                                color = RfThemeState.currentPalette.muted,
                                            ),
                                        )
                                    }
                                    BasicTextField(
                                        value = eventTypeValue,
                                        onValueChange = { updatedType ->
                                            val cappedType = updatedType.take(InputValidator.MAX_EVENT_NAME_LENGTH)
                                            eventTypeValue = cappedType
                                            hasInteractedType = true
                                            forceShowFormErrors = false
                                            viewModel?.setEventTypeInput(cappedType)
                                            if (cappedType.isNotBlank()) {
                                                viewModel?.updateRosaParameters(eventType = cappedType)
                                            }
                                        },
                                        singleLine = true,
                                        cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                        textStyle = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = RfThemeState.currentPalette.ink,
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                    if (typeError != null) {
                        RfValidationErrorText(
                            errorMessage = typeError,
                            modifier = Modifier.padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                resolveAssistantErrorBorderColor(
                                    hasError = typeError != null,
                                    defaultColor = RfThemeState.currentPalette.lineSoft,
                                ),
                            ),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfIcon(kind = RfIconKind.EDIT, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (eventTitleValue.isEmpty()) {
                                val titleExampleHint = if (matchedPresetOption != null) {
                                    if (matchedPresetOption.titlePrefix.isNotEmpty()) {
                                        "Título (ej: ${matchedPresetOption.titlePrefix}Ana & Carlos)"
                                    } else {
                                        "Título del evento (ej: Boda Ana & Carlos)"
                                    }
                                } else {
                                    "Título del evento (ej: Boda Ana & Carlos)"
                                }
                                BasicText(
                                    text = titleExampleHint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicTextField(
                                value = eventTitleValue,
                                onValueChange = { updatedTitle ->
                                    val cappedTitle = updatedTitle.take(InputValidator.MAX_EVENT_NAME_LENGTH)
                                    eventTitleValue = cappedTitle
                                    hasInteractedTitle = true
                                    forceShowFormErrors = false
                                    viewModel?.setClientEventTitleInput(cappedTitle)
                                },
                                singleLine = true,
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    if (titleError != null) {
                        RfValidationErrorText(
                            errorMessage = titleError,
                            modifier = Modifier.padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                resolveAssistantErrorBorderColor(
                                    hasError = titleError != null,
                                    defaultColor = RfThemeState.currentPalette.lineSoft,
                                ),
                            ),
                    )
                    val displayDateLabel = formatReadableSpanishDateText(dateValue)
                    val hasChosenDate = displayDateLabel.isNotBlank()
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
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            BasicText(
                                text = if (hasChosenDate) displayDateLabel else "Fecha del evento (toca para elegir)",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = if (hasChosenDate) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 15.sp,
                                    color = if (hasChosenDate) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                                ),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFDE6F2))
                                .clickable { showEventCalendar = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFFC2338F), size = 14.dp)
                            BasicText(
                                text = if (hasChosenDate) "Cambiar" else "Elegir",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFC2338F),
                                ),
                            )
                        }
                    }
                    if (dateError != null) {
                        RfValidationErrorText(
                            errorMessage = dateError,
                            modifier = Modifier.padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                resolveAssistantErrorBorderColor(
                                    hasError = dateError != null,
                                    defaultColor = RfThemeState.currentPalette.lineSoft,
                                ),
                            ),
                    )
                    val showRawBudgetText = if (isBudgetFocused) true else budgetValue.isEmpty()
                    val displayBudgetText = if (showRawBudgetText) {
                        budgetValue
                    } else {
                        formatDopCurrency(budgetValue.toIntOrNull() ?: 0)
                    }
                    val hasNumericRowError = if (guestsError != null) true else budgetError != null
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .padding(start = 16.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RfIcon(kind = RfIconKind.USER, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (guestsValue.isEmpty()) {
                                    BasicText(
                                        text = "Invitados (opc.)",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                                BasicTextField(
                                    value = guestsValue,
                                    onValueChange = { updatedGuests ->
                                        val numericGuests = InputValidator.filterDigitsOnly(updatedGuests, 5)
                                        guestsValue = numericGuests
                                        hasInteractedGuests = true
                                        forceShowFormErrors = false
                                        viewModel?.setEventGuestsInput(numericGuests)
                                        val parsedGuestInt = numericGuests.toIntOrNull()
                                        if (parsedGuestInt != null) {
                                            viewModel?.updateRosaParameters(guests = parsedGuestInt)
                                        }
                                    },
                                    singleLine = true,
                                    cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = RfThemeState.currentPalette.ink),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            GuestStepperButtons(
                                onIncrement = {
                                    val currentCount = guestsValue.toIntOrNull() ?: 0
                                    val nextCount = (currentCount + 1).coerceIn(1, InputValidator.MAX_GUEST_COUNT)
                                    val updatedText = nextCount.toString()
                                    guestsValue = updatedText
                                    hasInteractedGuests = true
                                    forceShowFormErrors = false
                                    viewModel?.setEventGuestsInput(updatedText)
                                    viewModel?.updateRosaParameters(guests = nextCount)
                                },
                                onDecrement = {
                                    val currentCount = guestsValue.toIntOrNull() ?: 1
                                    val nextCount = (currentCount - 1).coerceIn(1, InputValidator.MAX_GUEST_COUNT)
                                    val updatedText = nextCount.toString()
                                    guestsValue = updatedText
                                    hasInteractedGuests = true
                                    forceShowFormErrors = false
                                    viewModel?.setEventGuestsInput(updatedText)
                                    viewModel?.updateRosaParameters(guests = nextCount)
                                },
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(RfThemeState.currentPalette.lineSoft),
                        )
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .padding(start = 10.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RfIcon(kind = RfIconKind.WALLET, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (budgetValue.isEmpty()) {
                                    BasicText(
                                        text = "Presupuesto RD$",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                                BasicTextField(
                                    value = displayBudgetText,
                                    onValueChange = { updatedBudget ->
                                        val numericBudget = InputValidator.filterDigitsOnly(updatedBudget, 8)
                                        budgetValue = numericBudget
                                        hasInteractedBudget = true
                                        forceShowFormErrors = false
                                        val parsedBudget = numericBudget.toIntOrNull() ?: 0
                                        viewModel?.setRosaBudgetDop(parsedBudget)
                                    },
                                    singleLine = true,
                                    cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = RfThemeState.currentPalette.ink),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { focusState ->
                                            isBudgetFocused = focusState.isFocused
                                        },
                                )
                            }
                            val showBudgetSuffix = if (isBudgetFocused) budgetValue.isNotEmpty() else false
                            if (showBudgetSuffix) {
                                BasicText(
                                    text = "RD$",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF7A4FD8),
                                    ),
                                )
                            }
                        }
                    }
                    if (guestsError != null) {
                        RfValidationErrorText(
                            errorMessage = guestsError,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                        )
                    }
                    if (budgetError != null) {
                        RfValidationErrorText(
                            errorMessage = budgetError,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                resolveAssistantErrorBorderColor(
                                    hasError = hasNumericRowError,
                                    defaultColor = RfThemeState.currentPalette.lineSoft,
                                ),
                            ),
                    )
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
                                    text = "Ubicación o lugar (ej: Jardín Botánico)",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicTextField(
                                value = venueValue,
                                onValueChange = { updatedVenue ->
                                    val cappedVenue = updatedVenue.take(InputValidator.MAX_ADDRESS_LENGTH)
                                    venueValue = cappedVenue
                                    hasInteractedVenue = true
                                    forceShowFormErrors = false
                                    viewModel?.setEventVenueInput(cappedVenue)
                                },
                                singleLine = true,
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF3EBFF))
                                .clickable { showInAppMapModal = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF7A4FD8), size = 14.dp)
                            BasicText(
                                text = "Maps",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF7A4FD8),
                                ),
                            )
                        }
                    }
                    if (venueError != null) {
                        RfValidationErrorText(
                            errorMessage = venueError,
                            modifier = Modifier.padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                resolveAssistantErrorBorderColor(
                                    hasError = venueError != null,
                                    defaultColor = RfThemeState.currentPalette.lineSoft,
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 96.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RfIcon(kind = RfIconKind.LIGHTBULB, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 84.dp),
                                contentAlignment = Alignment.TopStart,
                            ) {
                                if (ideaValue.isEmpty()) {
                                    BasicText(
                                        text = "Cuéntame tu idea o notas (opcional)",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                                BasicTextField(
                                    value = ideaValue,
                                    onValueChange = { updatedIdea ->
                                        val cappedIdea = updatedIdea.take(InputValidator.MAX_NOTES_LENGTH)
                                        ideaValue = cappedIdea
                                        hasInteractedIdea = true
                                        forceShowFormErrors = false
                                        viewModel?.setEventNotesInput(cappedIdea)
                                    },
                                    cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 84.dp),
                                )
                            }
                            val micButtonBrush = if (isNotesMicListening) {
                                Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFEC4899)))
                            } else {
                                RosaButtonGradient
                            }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(micButtonBrush)
                                    .clickable {
                                        viewModel?.toggleInlineVoiceNotes(ideaValue)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(
                                    kind = if (isNotesMicListening) RfIconKind.CLOSE else RfIconKind.MIC,
                                    tint = Color.White,
                                    size = 18.dp,
                                )
                            }
                        }
                        if (isNotesMicListening) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 32.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .clickable { viewModel?.stopVoiceInput() }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626)),
                                )
                                BasicText(
                                    text = "Escuchando… toca para detener y transcribir",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF991B1B),
                                    ),
                                )
                            }
                        } else if (isNotesMicTranscribing) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 32.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFF3EBFF))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                RfIcon(kind = RfIconKind.MIC, tint = Color(0xFF7A4FD8), size = 12.dp)
                                BasicText(
                                    text = "Transcribiendo audio…",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF7A4FD8),
                                    ),
                                )
                            }
                        }
                        val shouldShowNotesVoiceError = if (isNotesOrigin) !voiceHardwareError.isNullOrBlank() else false
                        if (shouldShowNotesVoiceError) {
                            RfValidationErrorText(
                                errorMessage = voiceHardwareError,
                                modifier = Modifier.padding(start = 32.dp),
                            )
                        }
                    }
                    if (ideaError != null) {
                        RfValidationErrorText(
                            errorMessage = ideaError,
                            modifier = Modifier.padding(start = 48.dp, end = 16.dp, bottom = 10.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable {
                            val immediateError = listOfNotNull(
                                validateTitleS31(eventTitleValue),
                                validateTypeS31(eventTypeValue),
                                validateDateS31(dateValue),
                                validateGuestsS31(guestsValue),
                                validateBudgetS31(budgetValue),
                                validateVenueS31(venueValue),
                                validateIdeaS31(ideaValue),
                            ).firstOrNull()
                            if (immediateError != null) {
                                hasInteractedTitle = true
                                hasInteractedType = true
                                hasInteractedDate = true
                                hasInteractedGuests = true
                                hasInteractedBudget = true
                                hasInteractedVenue = true
                                hasInteractedIdea = true
                                forceShowFormErrors = true
                            } else {
                                onSelectScreenCode("s32")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Siguiente", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                }

                RfValidationErrorText(errorMessage = promptErrorS31)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .rfInputShadow(CircleShape, isAdmin = false)
                        .border(
                            width = 1.dp,
                            color = resolveAssistantErrorBorderColor(
                                hasError = promptErrorS31 != null,
                                defaultColor = Color(0xFFE7DEF5),
                            ),
                            shape = CircleShape,
                        )
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
                            onValueChange = { updatedText ->
                                promptText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitS31Prompt() }),
                            cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
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
            }
        }
        AnimatedVisibility(
            visible = showEventCalendar,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)) + scaleIn(
                animationSpec = tween(durationMillis = 180),
                initialScale = 0.94f,
            ),
            exit = fadeOut(animationSpec = tween(durationMillis = 140)) + scaleOut(
                animationSpec = tween(durationMillis = 140),
                targetScale = 0.96f,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            RfInteractiveCalendarModal(
                title = "Fecha del evento",
                currentDateText = dateValue,
                isAdmin = false,
                dateMode = RfCalendarDateMode.EVENT_UPCOMING,
                allowRangeSelection = true,
                preferReadableFormat = false,
                onDismiss = { showEventCalendar = false },
                onSelectDate = { formattedDate ->
                    dateValue = formattedDate
                    hasInteractedDate = true
                    forceShowFormErrors = false
                    viewModel?.setEventDateInput(formattedDate)
                    showEventCalendar = false
                },
            )
        }
        if (showInAppMapModal) {
            RfInAppMapPickerModal(
                initialQuery = venueValue,
                onDismiss = { showInAppMapModal = false },
                onConfirmLocation = { selectedAddress ->
                    val cappedAddress = selectedAddress.take(InputValidator.MAX_ADDRESS_LENGTH)
                    venueValue = cappedAddress
                    hasInteractedVenue = true
                    forceShowFormErrors = false
                    viewModel?.setEventVenueInput(cappedAddress)
                    showInAppMapModal = false
                },
            )
        }
    }
}

@Composable
internal fun GuestStepperButtons(
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF3EBFF))
            .border(1.dp, Color(0xFFE0D1FB), RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(18.dp)
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(9.dp, 6.dp)) {
                val chevronUp = Path().apply {
                    moveTo(size.width * 0.5f, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = chevronUp, color = Color(0xFF7A4FD8))
            }
        }
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(1.dp)
                .background(Color(0xFFE0D1FB)),
        )
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(18.dp)
                .clickable(onClick = onDecrement),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(9.dp, 6.dp)) {
                val chevronDown = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width * 0.5f, size.height)
                    close()
                }
                drawPath(path = chevronDown, color = Color(0xFF7A4FD8))
            }
        }
    }
}

@Composable
private fun RosaSummaryDetailRow(
    iconKind: RfIconKind,
    iconBg: Color,
    iconTint: Color,
    label: String,
    valueText: String,
    pendingLabel: String? = null,
    isOptional: Boolean = false,
    showChevron: Boolean = false,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val hasValue = valueText.isNotBlank()
    val emptyPillBg = if (isOptional) Color(0xFFF3EBFF) else Color(0xFFFFF2C9)
    val emptyPillInk = if (isOptional) Color(0xFF7A4FD8) else Color(0xFF7A5700)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = iconKind, tint = iconTint, size = 18.dp)
        }
        BasicText(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted),
            modifier = Modifier.widthIn(min = 84.dp),
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (hasValue) {
                BasicText(
                    text = valueText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = palette.ink,
                        textAlign = TextAlign.End,
                    ),
                )
            } else {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(emptyPillBg)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = pendingLabel ?: "Por confirmar",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = emptyPillInk,
                        ),
                    )
                }
            }
        }
        if (showChevron) {
            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 18.dp)
        }
    }
}

@Composable
internal fun RenderRosaSummaryS32(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val clientEventTitle = viewModel?.clientEventTitleInput?.collectAsState()?.value.orEmpty().trim()
    val rawEventTypeInput = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val rawRosaEventType = viewModel?.rosaEventType?.collectAsState()?.value.orEmpty()
    val eventType = rawEventTypeInput.ifBlank { rawRosaEventType.ifBlank { "Boda" } }
    val eventMetadata = resolveRosaEventMetadata(eventType)
    val rawEventDate = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty().trim()
    val eventDate = formatReadableSpanishDateText(rawEventDate)
    val eventVenue = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty().trim()
    val rawGuestsInput = viewModel?.eventGuestsInput?.collectAsState()?.value.orEmpty().trim()
    val resolvedGuestsCount = rawGuestsInput.toIntOrNull() ?: 0
    val budgetDop = viewModel?.rosaBudgetDop?.collectAsState()?.value ?: 0
    val scheduleItems = viewModel?.wizardScheduleItems?.collectAsState()?.value.orEmpty()
    var fallbackSummaryScheduleItems by remember { mutableStateOf(emptyList<EventTimelineItemUiModel>()) }
    var showScheduleEditorInSummary by remember { mutableStateOf(true) }
    val activeSummaryScheduleItems = if (viewModel != null) scheduleItems else fallbackSummaryScheduleItems
    val guestListCount = viewModel?.eventGuestsList?.collectAsState()?.value?.size ?: 0
    val sketchLayoutJson = viewModel?.sketchLayoutJson?.collectAsState()?.value.orEmpty()
    val parsedSketchElements = remember(sketchLayoutJson) { parseSketchElements(sketchLayoutJson) }
    val sketchTablesCount = parsedSketchElements.count { element -> isTableElementKind(element.kind) }
    val observedDraftQuantities = viewModel?.draftQuantities?.collectAsState()?.value
    val formErrorMessage = viewModel?.formError?.collectAsState()?.value

    val totalArticlesCount = observedDraftQuantities
        ?.values
        ?.filter { unitCount -> unitCount > 0 }
        ?.sum()
        ?: viewModel?.totalDraftCartUnits()
        ?: 0

    val resolvedTitle = clientEventTitle.ifBlank { eventType }
    val resolvedGuestsText = if (resolvedGuestsCount > 0) "$resolvedGuestsCount personas" else ""
    val resolvedBudgetText = if (budgetDop > 0) formatDopCurrency(budgetDop) else ""
    val resolvedScheduleText = if (activeSummaryScheduleItems.isNotEmpty()) {
        val firstBlock = activeSummaryScheduleItems.first()
        val formattedFirstStart = InputValidator.formatTimeDisplay12h(firstBlock.startTime)
        if (activeSummaryScheduleItems.size == 1) {
            "$formattedFirstStart ${firstBlock.title}"
        } else {
            "${activeSummaryScheduleItems.size} momentos ($formattedFirstStart...)"
        }
    } else {
        ""
    }
    val resolvedGuestsListText = if (guestListCount > 0) "$guestListCount invitado(s)" else ""
    val resolvedSketchText = if (parsedSketchElements.isNotEmpty()) {
        "$sketchTablesCount mesas · ${parsedSketchElements.size} elem."
    } else {
        ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                screenCode = "s32",
                viewModel = viewModel,
                onBack = { onSelectScreenCode("BACK:s25") },
                onCart = { onSelectScreenCode("s25") },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                        val openDraftStepEditor: () -> Unit = {
                            viewModel?.startEventDraftSetup(returnScreenCode = "s32")
                            onSelectScreenCode("s38")
                        }
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.EDIT,
                            iconBg = Color(0xFFEFE8FD),
                            iconTint = Color(0xFF7A4FD8),
                            label = "Título",
                            valueText = resolvedTitle,
                            showChevron = true,
                            onClick = openDraftStepEditor,
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = eventMetadata.iconKind,
                            iconBg = Color(0xFFFDE6F2),
                            iconTint = Color(0xFFC2338F),
                            label = "Evento",
                            valueText = eventType,
                            showChevron = true,
                            onClick = openDraftStepEditor,
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.CALENDAR,
                            iconBg = Color(0xFFDDF4F3),
                            iconTint = Color(0xFF1F8A84),
                            label = "Fecha",
                            valueText = eventDate,
                            pendingLabel = "Por definir",
                            showChevron = true,
                            onClick = openDraftStepEditor,
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.LOCATION,
                            iconBg = Color(0xFFEEF6D6),
                            iconTint = Color(0xFF4E6A10),
                            label = "Ubicación",
                            valueText = eventVenue,
                            pendingLabel = "Por definir",
                            showChevron = true,
                            onClick = openDraftStepEditor,
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.USER,
                            iconBg = Color(0xFFEFE8FD),
                            iconTint = Color(0xFF7A4FD8),
                            label = "Invitados",
                            valueText = resolvedGuestsText,
                            pendingLabel = "Opcional · Definir",
                            isOptional = true,
                            showChevron = true,
                            onClick = openDraftStepEditor,
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.WALLET,
                            iconBg = Color(0xFFDDF4F3),
                            iconTint = Color(0xFF1F8A84),
                            label = "Presupuesto",
                            valueText = resolvedBudgetText,
                            pendingLabel = "Opcional · Definir",
                            isOptional = true,
                            showChevron = true,
                            onClick = { onSelectScreenCode("s31") },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.CLOCK,
                            iconBg = Color(0xFFFDE6F2),
                            iconTint = Color(0xFFC2338F),
                            label = "Horarios",
                            valueText = resolvedScheduleText,
                            pendingLabel = "Opcional · Agregar",
                            isOptional = true,
                            showChevron = true,
                            onClick = { showScheduleEditorInSummary = !showScheduleEditorInSummary },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.GUESTS,
                            iconBg = Color(0xFFEEF6D6),
                            iconTint = Color(0xFF4E6A10),
                            label = "Lista de invitados",
                            valueText = resolvedGuestsListText,
                            pendingLabel = "Opcional · Agregar",
                            isOptional = true,
                            showChevron = true,
                            onClick = {
                                viewModel?.setGuestsWizardMode(true)
                                onSelectScreenCode("s47")
                            },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.PARTY,
                            iconBg = Color(0xFFF3EBFF),
                            iconTint = Color(0xFF7A4FD8),
                            label = "Boceto del espacio",
                            valueText = resolvedSketchText,
                            pendingLabel = "Opcional · Diseñar",
                            isOptional = true,
                            showChevron = true,
                            onClick = { onSelectScreenCode("s36") },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        RosaSummaryDetailRow(
                            iconKind = RfIconKind.BOX,
                            iconBg = Color(0xFFFFF2C9),
                            iconTint = Color(0xFF7A5700),
                            label = "Artículos",
                            valueText = "$totalArticlesCount",
                            showChevron = true,
                            onClick = { onSelectScreenCode("s25") },
                        )
                    }
                }

                BasicText(
                    text = "Los apartados marcados como «Opcional» puedes completarlos ahora o más adelante en tu evento.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = RfThemeState.currentPalette.muted,
                    ),
                )

                if (showScheduleEditorInSummary) {
                    WizardScheduleEditorSection(
                        scheduleItems = activeSummaryScheduleItems,
                        onAddMoment = { startTime, endTime, momentTitle ->
                            if (viewModel != null) {
                                viewModel.addWizardScheduleItem(startTime, endTime, momentTitle)
                            } else {
                                val nextIndex = fallbackSummaryScheduleItems.size + 1
                                fallbackSummaryScheduleItems = fallbackSummaryScheduleItems + EventTimelineItemUiModel(
                                    id = "local-tl-$nextIndex",
                                    startTime = startTime.ifBlank { "18:00" },
                                    endTime = endTime.ifBlank { "19:30" },
                                    title = momentTitle,
                                    subtitle = "Definido por ti",
                                    dotColorHex = 0xFF865DEC,
                                    ringColorHex = 0xFFEFE8FD,
                                )
                            }
                        },
                        onRemoveMoment = { itemId ->
                            if (viewModel != null) {
                                viewModel.removeWizardScheduleItem(itemId)
                            } else {
                                fallbackSummaryScheduleItems = fallbackSummaryScheduleItems.filterNot { item -> item.id == itemId }
                            }
                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.padding(top = 2.dp)) {
                        RfIcon(kind = RfIconKind.INFO, tint = Color(0xFF7A4FD8), size = 18.dp)
                    }
                    BasicText(
                        text = "El equipo de Rosa Fiesta revisa tu evento y te envía la cotización oficial y las tareas de montaje. No pagas nada hasta aprobar.",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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
                            val submittedQuote = viewModel?.submitClientQuoteRequest() ?: true
                            if (submittedQuote) {
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
    val rawSummaryDate = activeEvent?.dateLabel
        ?: activeQuote?.dateLabel
        ?: fallbackEventDate.ifBlank { "Por confirmar" }
    val summaryDate = formatReadableSpanishDateText(rawSummaryDate)
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
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDED5F7))
                    .padding(top = 14.dp, bottom = 10.dp, start = 16.dp, end = 16.dp),
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
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Canvas(modifier = Modifier.size(106.dp).clip(RoundedCornerShape(0.dp))) {
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

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(
                        text = "¿Quieres adelantar detalles mientras cotizamos?",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFE8FD))
                                .clickable { onSelectScreenCode("s34") },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Chat con Rosa",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7A4FD8)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE7F9EE))
                                .clickable {
                                    viewModel?.openWhatsAppChat(
                                        message = "Hola Rosa Fiesta, acabo de enviar mi solicitud para $summaryTitle ($summaryDate).",
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "WhatsApp Rosa",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E7E45)),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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
}

@Composable
internal fun RenderRosaChatS34(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    underlyingScreenCode: String? = null,
) {
    val typo = LocalRfTypography.current
    val initialProposalMessage = viewModel?.rosaProposalMessage?.collectAsState()?.value.orEmpty()
    val pickedAttachment = viewModel?.pickedAttachmentFile?.collectAsState()?.value
    var chatMessageText by remember(initialProposalMessage) {
        mutableStateOf(initialProposalMessage.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH))
    }
    var hasInteractedChat by remember { mutableStateOf(false) }
    var forceShowChatError by remember { mutableStateOf(false) }

    val candidateOrigin = underlyingScreenCode.orEmpty().trim()
    val isDistinctOrigin = if (candidateOrigin.isNotEmpty()) candidateOrigin != "s34" else false
    val originScreenCode = if (isDistinctOrigin) candidateOrigin else "s28"
    val resolvedOriginSpec = remember(originScreenCode) {
        ScreenCatalog.findByCode(originScreenCode)
    }
    val dismissS34 = { onSelectScreenCode("BACK:$originScreenCode") }

    val validateChatDraft: (String) -> String? = { rawDraft ->
        if (rawDraft.isBlank()) {
            if (pickedAttachment != null) null else InputValidator.validateChatMessage(rawDraft)
        } else {
            InputValidator.validateChatMessage(rawDraft)
        }
    }
    val chatError = rememberDebouncedValidationError(
        value = chatMessageText,
        hasInteracted = hasInteractedChat,
        forceShowError = forceShowChatError,
        validator = validateChatDraft,
    )

    val scrimInteraction = remember { MutableInteractionSource() }
    val sheetInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        RenderScreenSpec(
            spec = resolvedOriginSpec,
            viewModel = viewModel,
            onPrimaryAction = {},
            onSelectScreenCode = onSelectScreenCode,
            onSwitchAppMode = {},
            isFrameHarness = false,
            includeBottomBar = false,
        )
        if (shouldShowBottomBar(originScreenCode)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(0.35f),
            ) {
                RenderAppBottomBar(
                    spec = resolvedOriginSpec,
                    onSelectScreenCode = {},
                    onTogglePersonalMoreMenu = {},
                    isMoreMenuOpen = false,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = scrimInteraction,
                    indication = null,
                    onClick = dismissS34,
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .then(rememberModalSheetSlideModifier())
                .heightIn(min = 328.dp, max = 420.dp)
                .shadow(24.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), spotColor = Color(0x73AA46A0))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.surface)
                .clickable(
                    interactionSource = sheetInteraction,
                    indication = null,
                    onClick = {},
                )
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
                        val palette = RfThemeState.currentPalette
                        val isChipIncluded = chatMessageText.contains(chipText, ignoreCase = true)
                        val selectedChipBg = if (palette.isDark) Color(0xFF3B213D) else Color(0xFFFDE6F2)
                        val selectedChipFg = if (palette.isDark) Color(0xFFF48FB1) else Color(0xFFC2338F)
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(CircleShape)
                                .background(if (isChipIncluded) selectedChipBg else palette.chipBg)
                                .clickable {
                                    val appended = if (chatMessageText.isBlank()) {
                                        chipText
                                    } else {
                                        "$chatMessageText · $chipText"
                                    }
                                    chatMessageText = appended.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                                    hasInteractedChat = true
                                    forceShowChatError = false
                                    if (chipText.contains("120")) {
                                        viewModel?.setEventGuestsInput("120")
                                        viewModel?.updateRosaParameters(guests = 120)
                                    }
                                    if (chipText.contains("jardín", ignoreCase = true)) {
                                        val fallbackVenue = if (viewModel == null) "Jardín Los Rosales" else "Jardín para evento"
                                        viewModel?.setEventVenueInput(fallbackVenue)
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
                                    color = if (isChipIncluded) selectedChipFg else palette.ink,
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
                val rosaErrorText = viewModel?.formError?.collectAsState()?.value
                if (!rosaErrorText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEEF1))
                            .border(1.dp, Color(0xFFB42345), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                    ) {
                        BasicText(
                            text = rosaErrorText,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                        )
                    }
                }

                val submitChatPrompt = {
                    val validationError = validateChatDraft(chatMessageText)
                    if (validationError != null) {
                        hasInteractedChat = true
                        forceShowChatError = true
                    } else {
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
                            val currentPickedFile = pickedAttachment
                            applyNaturalLanguageEventPrompt(combinedPrompt, viewModel)
                            val activeViewModel = viewModel
                            if (activeViewModel != null) {
                                if (currentPickedFile != null) {
                                    activeViewModel.uploadAndAttachMedia("chat", currentPickedFile) { uploadedUrl ->
                                        val promptWithAttachment = "$combinedPrompt [$uploadedUrl]"
                                        activeViewModel.sendConversationalRosaPrompt(promptWithAttachment)
                                        activeViewModel.clearPickedAttachment()
                                    }
                                } else {
                                    activeViewModel.sendConversationalRosaPrompt(combinedPrompt)
                                }
                            }
                        }
                        onSelectScreenCode("s29")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = resolveAssistantErrorBorderColor(
                                    hasError = chatError != null,
                                    defaultColor = RfThemeState.currentPalette.lineSoft,
                                ),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                            onValueChange = { updatedText ->
                                chatMessageText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                                hasInteractedChat = true
                                forceShowChatError = false
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitChatPrompt() }),
                            cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                            textStyle = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 18.sp,
                                lineHeight = 26.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    RfValidationErrorText(errorMessage = chatError)
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
                                hasInteractedChat = false
                                forceShowChatError = false
                                viewModel?.clearPickedAttachment()
                                dismissS34()
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
            val isDefaultWizardReturn = cleanReturn == "s30"
            val useSearchDefault = if (cleanReturn.isEmpty()) true else isDefaultWizardReturn
            if (useSearchDefault) "s80" else cleanReturn
        } else {
            cleanReturn.ifBlank { "s30" }
        }
    }

    val shouldAppendIdea = viewModel?.voiceAppendToIdea?.collectAsState()?.value ?: false
    val isAwaitingCaptureStop = if (vmListening) editableTranscript.isBlank() else false
    val confirmVoiceAction = {
        if (!vmTranscribing) {
            if (isAwaitingCaptureStop) {
                viewModel?.stopVoiceInput()
            } else {
                viewModel?.stopVoiceInput()
                val finalTranscript = editableTranscript.trim()
                if (isSearchMode) {
                    viewModel?.setSearchQuery(finalTranscript)
                    onSelectScreenCode(resolvedReturnCode)
                } else {
                    if (finalTranscript.isNotEmpty()) {
                        if (shouldAppendIdea) {
                            val existingNotes = viewModel.eventNotesInput.value.trim()
                            val combinedNotes = if (existingNotes.isEmpty()) {
                                finalTranscript
                            } else {
                                "$existingNotes\n$finalTranscript"
                            }
                            viewModel.setEventNotesInput(combinedNotes)
                            viewModel.setVoiceAppendToIdea(false)
                        } else {
                            applyNaturalLanguageEventPrompt(finalTranscript, viewModel)
                            viewModel?.sendConversationalRosaPrompt(finalTranscript)
                        }
                    } else {
                        viewModel?.setVoiceAppendToIdea(false)
                    }
                    onSelectScreenCode(resolvedReturnCode)
                }
            }
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
                        .size(136.dp)
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

                val voiceTranscriptError = rememberOptionalPromptValidationError(promptText = editableTranscript)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.72f))
                            .border(
                                width = 1.dp,
                                color = resolveAssistantErrorBorderColor(
                                    hasError = voiceTranscriptError != null,
                                    defaultColor = Color(0xFFE7DEF5),
                                ),
                                shape = RoundedCornerShape(18.dp),
                            )
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
                                val cappedTranscript = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                                editableTranscript = cappedTranscript
                                viewModel?.setVoiceTranscript(cappedTranscript)
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val canConfirm = if (editableTranscript.isBlank()) {
                                        true
                                    } else {
                                        InputValidator.validateChatMessage(editableTranscript) == null
                                    }
                                    if (canConfirm) {
                                        confirmVoiceAction()
                                    }
                                },
                            ),
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
                    RfValidationErrorText(errorMessage = voiceTranscriptError)
                    val remoteVoiceError = viewModel?.formError?.collectAsState()?.value ?: vmVoiceError
                    if (!remoteVoiceError.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFEEF1))
                                .border(1.dp, Color(0xFFB42345), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                        ) {
                            BasicText(
                                text = remoteVoiceError,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                            )
                        }
                    }
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
                        val centerActionIcon = if (isAwaitingCaptureStop) RfIconKind.CLOSE else RfIconKind.CHECK
                        RfIcon(kind = centerActionIcon, tint = Color.White, size = 24.dp)
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
    val hasVoiceError = !voiceError.isNullOrBlank()
    val isVoiceBusy = listOf(isListening, isTranscribing).any { active -> active }
    val hasBannerContent = listOf(isVoiceBusy, hasVoiceError).any { active -> active }
    val shouldShowBanner = if (hasBannerContent) isSearchOrigin else false
    if (!shouldShowBanner) {
        return
    }
    val liveAmplitude = viewModel?.voiceAmplitude?.collectAsState()?.value ?: 0f
    val isErrorOnlyState = if (hasVoiceError) !isVoiceBusy else false
    val bannerBorderColor = if (isErrorOnlyState) Color(0xFFD97706) else Color(0xFFEA4EA9)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
            .clip(RoundedCornerShape(18.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.5.dp, bannerBorderColor, RoundedCornerShape(18.dp))
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
                val indicatorColor = when {
                    isTranscribing -> Color(0xFF865DEC)
                    isErrorOnlyState -> Color(0xFFD97706)
                    else -> Color(0xFFEA4EA9)
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                )
                val statusText = when {
                    isTranscribing -> "Transcribiendo tu voz…"
                    hasVoiceError -> voiceError.orEmpty()
                    voiceTranscript.isNotBlank() -> "“$voiceTranscript”"
                    else -> "Escuchando… habla para buscar en el catálogo"
                }
                BasicText(
                    text = statusText,
                    maxLines = if (hasVoiceError) 3 else 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isErrorOnlyState) Color(0xFF92400E) else RfThemeState.currentPalette.ink,
                    ),
                )
            }

            if (isVoiceBusy) {
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
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isErrorOnlyState) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF3F0F8))
                        .clickable { viewModel?.dismissVoiceError() }
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "Cerrar",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF5A5672),
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFFDE6F2))
                        .clickable { viewModel?.toggleInlineVoiceSearch() }
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText(
                        text = "Reintentar",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFFC2338F),
                        ),
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFFDE6F2))
                        .clickable(enabled = !isTranscribing) {
                            viewModel?.stopVoiceInput()
                            if (voiceTranscript.isNotBlank()) {
                                onApplyVoiceQuery(voiceTranscript.trim())
                            }
                        }
                        .padding(horizontal = 14.dp),
                ) {
                    val actionText = when {
                        isTranscribing -> "Procesando…"
                        voiceTranscript.isNotBlank() -> "Buscar “$voiceTranscript”"
                        else -> "Detener y transcribir"
                    }
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
}

internal enum class SketchElementKind {
    NONE,
    ROUND_TABLE,
    RECT_TABLE,
    SINGLE_CHAIR,
    CHAIR_GROUP,
    ARCH_STAGE,
    TEXT_LABEL,
    FREEHAND_STROKE,
}

private enum class SketchToolKind(val title: String, val hint: String) {
    SELECT("Selector", "Toca para seleccionar, arrastra para mover o borra"),
    PENCIL("Lápiz", "Dibuja libremente sobre el lienzo"),
    STAGE("Tarima", "Toca el lienzo para poner tarima"),
    ROUND_TABLE("Mesa ○", "Toca el lienzo para poner mesa redonda"),
    RECT_TABLE("Mesa ▭", "Toca el lienzo para poner mesa imperial"),
    CHAIRS("Sillas", "Toca una mesa para rodearla de sillas o el piso"),
    TEXT("Texto", "Toca el lienzo para agregar etiqueta"),
    ERASE("Borrar", "Toca o desliza para borrar"),
}

internal data class SketchCanvasElement(
    val id: String,
    val kind: SketchElementKind,
    val normX: Float,
    val normY: Float,
    val colorHex: Long,
    val label: String = "",
    val points: List<Pair<Float, Float>> = emptyList(),
)

private fun isTableElementKind(kind: SketchElementKind): Boolean {
    if (kind == SketchElementKind.ROUND_TABLE) return true
    return kind == SketchElementKind.RECT_TABLE
}

private fun isPointNearTable(
    table: SketchCanvasElement,
    tapNormX: Float,
    tapNormY: Float,
    canvasAspectRatio: Float = 0.85f,
): Boolean {
    val safeAspect = canvasAspectRatio.coerceIn(0.45f, 2.2f)
    if (table.kind == SketchElementKind.ROUND_TABLE) {
        val deltaX = table.normX - tapNormX
        val deltaY = (table.normY - tapNormY) / safeAspect
        val distance = hypot(deltaX.toDouble(), deltaY.toDouble()).toFloat()
        return distance <= 0.145f
    }
    if (table.kind == SketchElementKind.RECT_TABLE) {
        val withinX = kotlin.math.abs(tapNormX - table.normX) <= 0.165f
        val withinY = kotlin.math.abs(tapNormY - table.normY) <= 0.115f
        return if (withinX) withinY else false
    }
    return false
}

private fun findNearestTableForChairPlacement(
    elements: List<SketchCanvasElement>,
    tapNormX: Float,
    tapNormY: Float,
    canvasAspectRatio: Float = 0.85f,
): SketchCanvasElement? {
    val safeAspect = canvasAspectRatio.coerceIn(0.45f, 2.2f)
    var bestTable: SketchCanvasElement? = null
    var bestDistance = Float.MAX_VALUE
    elements.forEach { element ->
        val candidateTable = isTableElementKind(element.kind)
        if (candidateTable) {
            val withinHitArea = isPointNearTable(element, tapNormX, tapNormY, safeAspect)
            if (withinHitArea) {
                val deltaX = element.normX - tapNormX
                val deltaY = (element.normY - tapNormY) / safeAspect
                val dist = hypot(deltaX.toDouble(), deltaY.toDouble()).toFloat()
                if (dist < bestDistance) {
                    bestDistance = dist
                    bestTable = element
                }
            }
        }
    }
    return bestTable
}

private fun computeTableSeatSlots(
    table: SketchCanvasElement,
    canvasAspectRatio: Float = 0.85f,
): List<Pair<Float, Float>> {
    val safeAspect = canvasAspectRatio.coerceIn(0.45f, 2.2f)
    if (table.kind == SketchElementKind.ROUND_TABLE) {
        val radiusX = 0.106f
        val radiusY = radiusX * safeAspect
        val maxRoundTableChairs = 10
        val angleStepDeg = 360.0 / maxRoundTableChairs.toDouble()
        return (0 until maxRoundTableChairs).map { slotIndex ->
            val angleRad = (-90.0 + slotIndex * angleStepDeg) * PI / 180.0
            val seatX = (table.normX + radiusX * cos(angleRad).toFloat()).coerceIn(0.03f, 0.97f)
            val seatY = (table.normY + radiusY * sin(angleRad).toFloat()).coerceIn(0.03f, 0.97f)
            Pair(seatX, seatY)
        }
    }
    if (table.kind == SketchElementKind.RECT_TABLE) {
        val xOffsets = listOf(-0.078f, -0.026f, 0.026f, 0.078f)
        val topSeats = xOffsets.map { dx -> Pair((table.normX + dx).coerceIn(0.03f, 0.97f), (table.normY - 0.068f).coerceIn(0.03f, 0.97f)) }
        val bottomSeats = xOffsets.map { dx -> Pair((table.normX + dx).coerceIn(0.03f, 0.97f), (table.normY + 0.068f).coerceIn(0.03f, 0.97f)) }
        val leftHead = Pair((table.normX - 0.132f).coerceIn(0.03f, 0.97f), table.normY.coerceIn(0.03f, 0.97f))
        val rightHead = Pair((table.normX + 0.132f).coerceIn(0.03f, 0.97f), table.normY.coerceIn(0.03f, 0.97f))
        return topSeats + bottomSeats + listOf(leftHead, rightHead)
    }
    return emptyList()
}

private fun isChairSlotOccupied(
    slotCoord: Pair<Float, Float>,
    existingElements: List<SketchCanvasElement>,
    canvasAspectRatio: Float = 0.85f,
): Boolean = existingElements.any { item ->
    val isChairKind = item.kind == SketchElementKind.SINGLE_CHAIR
    if (!isChairKind) {
        false
    } else {
        val safeAspect = canvasAspectRatio.coerceIn(0.45f, 2.2f)
        val deltaX = item.normX - slotCoord.first
        val deltaY = (item.normY - slotCoord.second) / safeAspect
        val distance = hypot(deltaX.toDouble(), deltaY.toDouble()).toFloat()
        distance < 0.026f
    }
}

/**
 * Places a single chair evenly around the perimeter of the tapped table (up to table capacity),
 * returning `null` if the tapped table already reached its maximum chair capacity, or places a
 * standalone floor chair when tapping away from tables.
 */
internal fun buildSmartChairElementForTap(
    elements: List<SketchCanvasElement>,
    tapNormX: Float,
    tapNormY: Float,
    colorHex: Long,
    chairId: String,
    canvasAspectRatio: Float = 0.85f,
): SketchCanvasElement? {
    val targetTable = findNearestTableForChairPlacement(elements, tapNormX, tapNormY, canvasAspectRatio)
    if (targetTable != null) {
        val seatSlots = computeTableSeatSlots(targetTable, canvasAspectRatio)
        val seatPrefix = "seat:${targetTable.id}:"
        val nextFreeSlot = seatSlots.withIndex().firstOrNull { indexedSlot ->
            val taggedMatch = elements.any { item -> item.label == "$seatPrefix${indexedSlot.index}" }
            if (taggedMatch) {
                false
            } else {
                !isChairSlotOccupied(indexedSlot.value, elements, canvasAspectRatio)
            }
        } ?: return null
        return SketchCanvasElement(
            id = chairId,
            kind = SketchElementKind.SINGLE_CHAIR,
            normX = nextFreeSlot.value.first,
            normY = nextFreeSlot.value.second,
            colorHex = colorHex,
            label = "$seatPrefix${nextFreeSlot.index}",
        )
    }
    return SketchCanvasElement(
        id = chairId,
        kind = SketchElementKind.SINGLE_CHAIR,
        normX = tapNormX.coerceIn(0.04f, 0.96f),
        normY = tapNormY.coerceIn(0.04f, 0.96f),
        colorHex = colorHex,
        label = "Silla",
    )
}

internal fun translateSketchElementWithAttachedChairs(
    elements: List<SketchCanvasElement>,
    movedIndex: Int,
    deltaX: Float,
    deltaY: Float,
): List<SketchCanvasElement> {
    val target = elements.getOrNull(movedIndex) ?: return elements
    val shiftedTarget = translateSketchElement(target, deltaX, deltaY)
    val actualDeltaX = shiftedTarget.normX - target.normX
    val actualDeltaY = shiftedTarget.normY - target.normY
    val movingTable = isTableElementKind(target.kind)
    val attachedSeatPrefix = "seat:${target.id}:"
    return elements.mapIndexed { idx, item ->
        when {
            idx == movedIndex -> {
                val movedLabel = if (target.kind == SketchElementKind.SINGLE_CHAIR) {
                    if (item.label.startsWith("seat:")) "Silla" else item.label
                } else {
                    item.label
                }
                shiftedTarget.copy(label = movedLabel)
            }

            movingTable -> {
                val isAttachedChair = if (item.kind == SketchElementKind.SINGLE_CHAIR) {
                    item.label.startsWith(attachedSeatPrefix)
                } else {
                    false
                }
                if (isAttachedChair) {
                    translateSketchElement(item, actualDeltaX, actualDeltaY)
                } else {
                    item
                }
            }

            else -> item
        }
    }
}

internal fun removeSketchElementById(
    elements: List<SketchCanvasElement>,
    elementId: String,
): List<SketchCanvasElement> = elements.filterNot { item -> item.id == elementId }

private fun resolveSketchElementDisplayName(element: SketchCanvasElement): String = when (element.kind) {
    SketchElementKind.NONE -> "Elemento"
    SketchElementKind.ROUND_TABLE -> "Mesa redonda"
    SketchElementKind.RECT_TABLE -> "Mesa imperial"
    SketchElementKind.SINGLE_CHAIR -> "Silla"
    SketchElementKind.CHAIR_GROUP -> "Fila de sillas"
    SketchElementKind.ARCH_STAGE -> element.label.ifBlank { "Tarima" }
    SketchElementKind.TEXT_LABEL -> element.label.ifBlank { "Etiqueta" }
    SketchElementKind.FREEHAND_STROKE -> "Trazo libre"
}

private fun defaultSketchColorHex(kind: SketchElementKind): Long = when (kind) {
    SketchElementKind.ROUND_TABLE -> 0xFF865DECL
    SketchElementKind.RECT_TABLE -> 0xFF1FBDC0L
    SketchElementKind.SINGLE_CHAIR -> 0xFF1FBDC0L
    SketchElementKind.CHAIR_GROUP -> 0xFF8DB52CL
    SketchElementKind.ARCH_STAGE -> 0xFFEA4EA9L
    SketchElementKind.TEXT_LABEL -> 0xFF1A1A2EL
    SketchElementKind.FREEHAND_STROKE -> 0xFFEA4EA9L
    else -> 0xFF865DECL
}

private fun mapSketchKindFromApi(rawKind: String): SketchElementKind = when (rawKind.trim().uppercase()) {
    "ROUND_TABLE" -> SketchElementKind.ROUND_TABLE
    "RECT_TABLE" -> SketchElementKind.RECT_TABLE
    "SINGLE_CHAIR" -> SketchElementKind.SINGLE_CHAIR
    "CHAIR_GROUP" -> SketchElementKind.CHAIR_GROUP
    "ARCH_STAGE" -> SketchElementKind.ARCH_STAGE
    "FREEHAND_STROKE" -> SketchElementKind.FREEHAND_STROKE
    else -> SketchElementKind.TEXT_LABEL
}

private fun mapProposalElementsToCanvas(proposal: RosaSketchProposal): List<SketchCanvasElement> {
    if (proposal.elements.isEmpty()) return emptyList()
    return proposal.elements.mapIndexed { index, apiElement: RosaSketchElement ->
        val kind = mapSketchKindFromApi(apiElement.kind)
        SketchCanvasElement(
            id = apiElement.id.ifBlank { "ai-api-${index + 1}" },
            kind = kind,
            normX = apiElement.xRatio.toFloat().coerceIn(0.05f, 0.95f),
            normY = apiElement.yRatio.toFloat().coerceIn(0.05f, 0.95f),
            colorHex = defaultSketchColorHex(kind),
            label = apiElement.label,
        )
    }
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
    var aiChairSerial = 1
    fun appendTableWithSurroundingChairs(table: SketchCanvasElement) {
        generated.add(table)
        val slots = computeTableSeatSlots(table).take(6)
        slots.forEachIndexed { seatIdx, coord ->
            generated.add(
                SketchCanvasElement(
                    id = "ai-chair-$aiChairSerial",
                    kind = SketchElementKind.SINGLE_CHAIR,
                    normX = coord.first,
                    normY = coord.second,
                    colorHex = 0xFF1FBDC0,
                    label = "seat:${table.id}:$seatIdx",
                ),
            )
            aiChairSerial += 1
        }
    }
    for (leftIdx in 0 until leftColumnCount) {
        val denominator = (leftColumnCount + 1).coerceAtLeast(2)
        val posY = (0.28f + (leftIdx + 1) * (0.58f / denominator)).coerceIn(0.25f, 0.85f)
        appendTableWithSurroundingChairs(
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
        appendTableWithSurroundingChairs(
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
    return generated
}

private fun sanitizeSketchToken(rawValue: String): String = rawValue
    .replace("|", " ")
    .replace(";", " ")
    .replace("\n", " ")
    .trim()

private fun serializeStrokePoints(points: List<Pair<Float, Float>>): String {
    if (points.isEmpty()) {
        return ""
    }
    return points.joinToString("~") { point ->
        val px = ((point.first * 1000f).toInt() / 1000f)
        val py = ((point.second * 1000f).toInt() / 1000f)
        "$px,$py"
    }
}

private fun parseStrokePoints(rawPoints: String): List<Pair<Float, Float>> {
    val trimmed = rawPoints.trim()
    if (trimmed.isEmpty()) {
        return emptyList()
    }
    return trimmed.split("~").mapNotNull { pairToken ->
        val coords = pairToken.split(",")
        val coordX = coords.getOrNull(0)?.toFloatOrNull()
        val coordY = coords.getOrNull(1)?.toFloatOrNull()
        if (coordX != null) {
            if (coordY != null) {
                Pair(coordX.coerceIn(0.01f, 0.99f), coordY.coerceIn(0.01f, 0.99f))
            } else {
                null
            }
        } else {
            null
        }
    }
}

private fun serializeSketchElements(elements: List<SketchCanvasElement>): String = elements.joinToString(";") { item ->
    val safeId = sanitizeSketchToken(item.id)
    val safeLabel = sanitizeSketchToken(item.label)
    val roundedX = ((item.normX * 1000f).toInt() / 1000f)
    val roundedY = ((item.normY * 1000f).toInt() / 1000f)
    val encodedPoints = serializeStrokePoints(item.points)
    "$safeId|${item.kind.name}|$roundedX|$roundedY|${item.colorHex}|$safeLabel|$encodedPoints"
}

private fun parseSketchElements(rawSerialized: String): List<SketchCanvasElement> {
    val trimmed = rawSerialized.trim()
    if (trimmed.isEmpty()) {
        return emptyList()
    }
    val records = trimmed.split(";").filter { token -> token.isNotBlank() }
    val parsed = mutableListOf<SketchCanvasElement>()
    val maxRecords = records.size.coerceAtMost(120)
    for (recordIndex in 0 until maxRecords) {
        val parts = records[recordIndex].split("|")
        if (parts.size >= 5) {
            val resolvedKind = SketchElementKind.entries.firstOrNull { candidate ->
                candidate.name == parts[1].trim()
            } ?: SketchElementKind.NONE
            if (resolvedKind != SketchElementKind.NONE) {
                val parsedX = (parts[2].toFloatOrNull() ?: 0.5f).coerceIn(0.02f, 0.98f)
                val parsedY = (parts[3].toFloatOrNull() ?: 0.5f).coerceIn(0.02f, 0.98f)
                val parsedColor = parts[4].toLongOrNull() ?: 0xFFEA4EA9
                val parsedLabel = if (parts.size >= 6) parts[5] else ""
                val parsedPoints = if (parts.size >= 7) parseStrokePoints(parts[6]) else emptyList()
                parsed.add(
                    SketchCanvasElement(
                        id = parts[0].ifBlank { "el-$recordIndex" },
                        kind = resolvedKind,
                        normX = parsedX,
                        normY = parsedY,
                        colorHex = parsedColor,
                        label = parsedLabel,
                        points = parsedPoints,
                    ),
                )
            }
        }
    }
    return parsed
}

private fun findNearestSketchElementIndex(
    elements: List<SketchCanvasElement>,
    targetNormX: Float,
    targetNormY: Float,
): Int {
    var bestIndex = -1
    var bestDistance = 0.12f
    elements.forEachIndexed { index, element ->
        var elementDistance = hypot(
            (element.normX - targetNormX).toDouble(),
            (element.normY - targetNormY).toDouble(),
        ).toFloat()
        if (element.kind == SketchElementKind.FREEHAND_STROKE) {
            element.points.forEach { strokePoint ->
                val vertexDistance = hypot(
                    (strokePoint.first - targetNormX).toDouble(),
                    (strokePoint.second - targetNormY).toDouble(),
                ).toFloat()
                if (vertexDistance < elementDistance) {
                    elementDistance = vertexDistance
                }
            }
        }
        if (elementDistance <= bestDistance) {
            bestDistance = elementDistance
            bestIndex = index
        }
    }
    return bestIndex
}

private fun translateSketchElement(
    element: SketchCanvasElement,
    deltaNormX: Float,
    deltaNormY: Float,
): SketchCanvasElement {
    val shiftedX = (element.normX + deltaNormX).coerceIn(0.04f, 0.96f)
    val shiftedY = (element.normY + deltaNormY).coerceIn(0.04f, 0.96f)
    val actualDeltaX = shiftedX - element.normX
    val actualDeltaY = shiftedY - element.normY
    val shiftedPoints = element.points.map { point ->
        Pair(
            (point.first + actualDeltaX).coerceIn(0.01f, 0.99f),
            (point.second + actualDeltaY).coerceIn(0.01f, 0.99f),
        )
    }
    return element.copy(
        normX = shiftedX,
        normY = shiftedY,
        points = shiftedPoints,
    )
}

private fun syncSketchElementsIdempotently(
    elements: List<SketchCanvasElement>,
    viewModel: RosaFiestaViewModel?,
) {
    val roundTableCount = elements.count { element -> element.kind == SketchElementKind.ROUND_TABLE }
    val rectTableCount = elements.count { element -> element.kind == SketchElementKind.RECT_TABLE }
    val singleChairCount = elements.count { element -> element.kind == SketchElementKind.SINGLE_CHAIR }
    val chairGroupCount = elements.count { element -> element.kind == SketchElementKind.CHAIR_GROUP }
    val archStageCount = elements.count { element -> element.kind == SketchElementKind.ARCH_STAGE }
    val totalTables = roundTableCount + rectTableCount
    val computedChairs = singleChairCount + (chairGroupCount * 6)
    val serialized = serializeSketchElements(elements)

    viewModel?.setSketchTableCount(totalTables.coerceIn(0, 12))
    viewModel?.setDraftQuantity("art-mesa-redonda", roundTableCount)
    viewModel?.setDraftQuantity("art-mesa-plegable", rectTableCount)
    viewModel?.setDraftQuantity("art-silla-tiffany", computedChairs)
    if (archStageCount > 0) {
        viewModel?.setDraftQuantity("art-arco-rosa", archStageCount)
    }
    viewModel?.saveSketchLayoutJson(serialized)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSingleSketchElement(
    element: SketchCanvasElement,
    isThreeDimensionalPreview: Boolean,
    isSelected: Boolean = false,
) {
    val canvasW = size.width
    val canvasH = size.height
    val centerX = element.normX * canvasW
    val centerY = element.normY * canvasH
    val itemColor = Color(element.colorHex)
    val elevationOffset = if (isThreeDimensionalPreview) 5.dp.toPx() else 0f
    val selectionColor = Color(0xFF7A4FD8)

    if (isSelected) {
        drawCircle(
            color = selectionColor.copy(alpha = 0.16f),
            radius = 22.dp.toPx(),
            center = Offset(centerX, centerY),
        )
        drawCircle(
            color = selectionColor,
            radius = 22.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
            ),
        )
    }

    when (element.kind) {
        SketchElementKind.NONE -> Unit

        SketchElementKind.FREEHAND_STROKE -> {
            if (element.points.size == 1) {
                val point = element.points.first()
                drawCircle(
                    color = itemColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(point.first * canvasW, point.second * canvasH),
                )
            } else if (element.points.size > 1) {
                val strokePath = Path().apply {
                    val startPt = element.points.first()
                    moveTo(startPt.first * canvasW, startPt.second * canvasH)
                    for (idx in 1 until element.points.size) {
                        val pt = element.points[idx]
                        lineTo(pt.first * canvasW, pt.second * canvasH)
                    }
                }
                drawPath(
                    path = strokePath,
                    color = itemColor,
                    style = Stroke(
                        width = 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }

        SketchElementKind.ARCH_STAGE -> {
            val stageW = canvasW * 0.54f
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
                style = Stroke(width = 2.6.dp.toPx()),
            )
        }

        SketchElementKind.ROUND_TABLE -> {
            val radius = (canvasW * 0.068f).coerceIn(14f, 28f)
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
                color = itemColor.copy(alpha = 0.09f),
                radius = radius,
                center = Offset(centerX, centerY),
            )
            drawCircle(
                color = itemColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.6.dp.toPx()),
            )
        }

        SketchElementKind.RECT_TABLE -> {
            val rectW = (canvasW * 0.20f).coerceAtLeast(36f)
            val rectH = (canvasH * 0.072f).coerceAtLeast(18f)
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
                color = itemColor.copy(alpha = 0.09f),
                topLeft = topLeft,
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(5.dp.toPx()),
            )
            drawRoundRect(
                color = itemColor,
                topLeft = topLeft,
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(5.dp.toPx()),
                style = Stroke(width = 2.6.dp.toPx()),
            )
        }

        SketchElementKind.SINGLE_CHAIR -> {
            val chairSize = 10.dp.toPx()
            val halfChair = chairSize / 2f
            drawRoundRect(
                color = itemColor.copy(alpha = 0.22f),
                topLeft = Offset(centerX - halfChair, centerY - halfChair),
                size = Size(chairSize, chairSize),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
            drawRoundRect(
                color = itemColor,
                topLeft = Offset(centerX - halfChair, centerY - halfChair),
                size = Size(chairSize, chairSize),
                cornerRadius = CornerRadius(3.dp.toPx()),
                style = Stroke(width = 2.dp.toPx()),
            )
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

@Composable
private fun SketchCanvasPreviewSurface(
    elements: List<SketchCanvasElement>,
    isThreeDimensionalPreview: Boolean,
    modifier: Modifier = Modifier,
    interactiveEnabled: Boolean = false,
    activeTool: SketchToolKind = SketchToolKind.PENCIL,
    activeColorHex: Long = 0xFFEA4EA9L,
    selectedElementId: String? = null,
    onSelectElement: ((String?) -> Unit)? = null,
    onTapAtNormalized: ((Float, Float) -> Unit)? = null,
    onTapWithAspect: ((Float, Float, Float) -> Unit)? = null,
    onCommitFreehandStroke: ((List<Pair<Float, Float>>) -> Unit)? = null,
    onCommitElementMove: ((Int, Float, Float) -> Unit)? = null,
    onEraseAtNormalized: ((Float, Float) -> Unit)? = null,
) {
    val typo = LocalRfTypography.current
    val latestElements by rememberUpdatedState(elements)
    val latestTool by rememberUpdatedState(activeTool)
    val latestOnSelectElement by rememberUpdatedState(onSelectElement)
    val latestOnTap by rememberUpdatedState(onTapAtNormalized)
    val latestOnTapWithAspect by rememberUpdatedState(onTapWithAspect)
    val latestOnCommitStroke by rememberUpdatedState(onCommitFreehandStroke)
    val latestOnCommitMove by rememberUpdatedState(onCommitElementMove)
    val latestOnErase by rememberUpdatedState(onEraseAtNormalized)
    var activeStrokePoints by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var draggingElementIndex by remember { mutableStateOf(-1) }
    var dragDeltaNorm by remember { mutableStateOf(Pair(0f, 0f)) }

    val displayedElements = remember(elements, draggingElementIndex, dragDeltaNorm) {
        if (draggingElementIndex in elements.indices) {
            translateSketchElementWithAttachedChairs(
                elements = elements,
                movedIndex = draggingElementIndex,
                deltaX = dragDeltaNorm.first,
                deltaY = dragDeltaNorm.second,
            )
        } else {
            elements
        }
    }

    val gestureModifier = if (interactiveEnabled) {
        Modifier
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val normX = (tapOffset.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0.04f, 0.96f)
                    val normY = (tapOffset.y / size.height.toFloat().coerceAtLeast(1f)).coerceIn(0.04f, 0.96f)
                    when (latestTool) {
                        SketchToolKind.PENCIL -> {
                            latestOnCommitStroke?.invoke(listOf(Pair(normX, normY)))
                        }

                        SketchToolKind.ERASE -> {
                            latestOnErase?.invoke(normX, normY)
                        }

                        SketchToolKind.SELECT -> {
                            val hitIdx = findNearestSketchElementIndex(latestElements, normX, normY)
                            val hitId = latestElements.getOrNull(hitIdx)?.id
                            latestOnSelectElement?.invoke(hitId)
                        }

                        else -> {
                            val widthFloat = size.width.toFloat().coerceAtLeast(1f)
                            val heightFloat = size.height.toFloat().coerceAtLeast(1f)
                            val aspectRatio = (widthFloat / heightFloat).coerceIn(0.45f, 2.2f)
                            val tapAspectCallback = latestOnTapWithAspect
                            if (tapAspectCallback != null) {
                                tapAspectCallback.invoke(normX, normY, aspectRatio)
                            } else {
                                latestOnTap?.invoke(normX, normY)
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val normX = (startOffset.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0.02f, 0.98f)
                        val normY = (startOffset.y / size.height.toFloat().coerceAtLeast(1f)).coerceIn(0.02f, 0.98f)
                        when (latestTool) {
                            SketchToolKind.PENCIL -> {
                                activeStrokePoints = listOf(Pair(normX, normY))
                            }

                            SketchToolKind.SELECT -> {
                                val hitIndex = findNearestSketchElementIndex(
                                    elements = latestElements,
                                    targetNormX = normX,
                                    targetNormY = normY,
                                )
                                draggingElementIndex = hitIndex
                                latestElements.getOrNull(hitIndex)?.id?.let { hitId ->
                                    latestOnSelectElement?.invoke(hitId)
                                }
                                dragDeltaNorm = Pair(0f, 0f)
                            }

                            SketchToolKind.ERASE -> {
                                latestOnErase?.invoke(normX, normY)
                            }

                            else -> Unit
                        }
                    },
                    onDragEnd = {
                        if (activeStrokePoints.isNotEmpty()) {
                            latestOnCommitStroke?.invoke(activeStrokePoints)
                            activeStrokePoints = emptyList()
                        }
                        if (draggingElementIndex >= 0) {
                            latestOnCommitMove?.invoke(
                                draggingElementIndex,
                                dragDeltaNorm.first,
                                dragDeltaNorm.second,
                            )
                            draggingElementIndex = -1
                            dragDeltaNorm = Pair(0f, 0f)
                        }
                    },
                    onDragCancel = {
                        activeStrokePoints = emptyList()
                        draggingElementIndex = -1
                        dragDeltaNorm = Pair(0f, 0f)
                    },
                ) { pointerChange, dragAmount ->
                    pointerChange.consume()
                    val canvasW = size.width.toFloat().coerceAtLeast(1f)
                    val canvasH = size.height.toFloat().coerceAtLeast(1f)
                    val posX = (pointerChange.position.x / canvasW).coerceIn(0.02f, 0.98f)
                    val posY = (pointerChange.position.y / canvasH).coerceIn(0.02f, 0.98f)
                    when (latestTool) {
                        SketchToolKind.PENCIL -> {
                            val previous = activeStrokePoints.lastOrNull()
                            val distance = if (previous != null) {
                                hypot((posX - previous.first).toDouble(), (posY - previous.second).toDouble()).toFloat()
                            } else {
                                1f
                            }
                            if (distance >= 0.008f) {
                                activeStrokePoints = activeStrokePoints + Pair(posX, posY)
                            }
                        }

                        SketchToolKind.SELECT -> {
                            if (draggingElementIndex >= 0) {
                                val deltaX = dragAmount.x / canvasW
                                val deltaY = dragAmount.y / canvasH
                                dragDeltaNorm = Pair(
                                    dragDeltaNorm.first + deltaX,
                                    dragDeltaNorm.second + deltaY,
                                )
                            }
                        }

                        SketchToolKind.ERASE -> {
                            latestOnErase?.invoke(posX, posY)
                        }

                        else -> Unit
                    }
                }
            }
    } else {
        Modifier
    }

    BoxWithConstraints(
        modifier = modifier.then(gestureModifier),
    ) {
        val surfaceWidth = maxWidth
        val surfaceHeight = maxHeight

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

            displayedElements.forEach { element ->
                val isElementSelected = element.id == selectedElementId
                drawSingleSketchElement(
                    element = element,
                    isThreeDimensionalPreview = isThreeDimensionalPreview,
                    isSelected = isElementSelected,
                )
            }

            if (activeStrokePoints.size > 1) {
                val liveStrokePath = Path().apply {
                    val startPt = activeStrokePoints.first()
                    moveTo(startPt.first * canvasW, startPt.second * canvasH)
                    for (idx in 1 until activeStrokePoints.size) {
                        val pt = activeStrokePoints[idx]
                        lineTo(pt.first * canvasW, pt.second * canvasH)
                    }
                }
                drawPath(
                    path = liveStrokePath,
                    color = Color(activeColorHex),
                    style = Stroke(
                        width = 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }

        if (displayedElements.isEmpty()) {
            if (activeStrokePoints.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFFB5A9D0), size = 28.dp)
                    BasicText(
                        text = "Dibuja tu espacio aquí",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF8C82A8),
                        ),
                    )
                    BasicText(
                        text = "Usa el lápiz para trazar libremente o elige mesas y sillas de la caja de herramientas",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFA098BA),
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }

        val labelElements = displayedElements.filter { item ->
            val isExplicitLabel = item.kind == SketchElementKind.TEXT_LABEL
            val isStageWithLabel = if (item.kind == SketchElementKind.ARCH_STAGE) item.label.isNotBlank() else false
            if (isExplicitLabel) true else isStageWithLabel
        }
        labelElements.forEach { labelElement ->
            val targetX = (surfaceWidth * labelElement.normX - 38.dp)
                .coerceIn(4.dp, (surfaceWidth - 82.dp).coerceAtLeast(4.dp))
            val targetY = (surfaceHeight * labelElement.normY - 12.dp)
                .coerceIn(4.dp, (surfaceHeight - 28.dp).coerceAtLeast(4.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = targetX, y = targetY)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.92f))
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
    var selectedTool by remember { mutableStateOf(SketchToolKind.PENCIL) }
    val vmSketchJson = viewModel?.sketchLayoutJson?.collectAsState()?.value.orEmpty()
    val vmRosaGuests = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 80

    LaunchedEffect(Unit) {
        if (vmSketchJson.isBlank()) {
            viewModel?.syncDraftQuantitiesToSketch()
        }
    }

    var canvasElements by remember(vmSketchJson) {
        mutableStateOf(parseSketchElements(vmSketchJson))
    }
    var undoStack by remember { mutableStateOf(emptyList<List<SketchCanvasElement>>()) }
    var redoStack by remember { mutableStateOf(emptyList<List<SketchCanvasElement>>()) }
    var showLabelEditorModal by remember { mutableStateOf(false) }
    var showAiShapeNoticeModal by remember { mutableStateOf(false) }
    var isGeneratingAiSketch by remember { mutableStateOf(false) }
    var pendingLabelCoordinates by remember { mutableStateOf(Pair(0.50f, 0.50f)) }
    var customLabelInput by remember { mutableStateOf("") }
    var nextElementSerial by remember { mutableStateOf(100) }
    var selectedElementId by remember { mutableStateOf<String?>(null) }
    var chairCapacityWarning by remember { mutableStateOf<String?>(null) }
    var sketchFallbackNotice by remember { mutableStateOf<String?>(null) }

    val selectedElement = remember(canvasElements, selectedElementId) {
        val activeId = selectedElementId ?: return@remember null
        canvasElements.firstOrNull { item -> item.id == activeId }
    }

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

    val appendShapeAt: (SketchElementKind, Float, Float, String) -> Unit = { elementKind, normX, normY, defaultLabel ->
        val currentId = nextElementSerial
        nextElementSerial += 1
        val createdElement = SketchCanvasElement(
            id = "el-$currentId",
            kind = elementKind,
            normX = normX.coerceIn(0.06f, 0.94f),
            normY = normY.coerceIn(0.06f, 0.94f),
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
                    .padding(top = 14.dp, bottom = 10.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s32") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boceto del espacio",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, lineHeight = 20.sp, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        text = "Vista 2D · $totalTablesCount mesas · ${canvasElements.size} elementos",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                    )
                }

                val canUndo = undoStack.isNotEmpty()
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable(enabled = canUndo) {
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
                        size = 18.dp,
                    )
                }

                val canRedo = redoStack.isNotEmpty()
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
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
                    RfIcon(
                        kind = RfIconKind.REDO,
                        tint = if (canRedo) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                        size = 18.dp,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            selectedElementId = null
                            if (canvasElements.isNotEmpty()) {
                                commitElementsMutation(emptyList())
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.DELETE, tint = RfThemeState.currentPalette.ink, size = 18.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 66.dp, bottom = 86.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (RfThemeState.currentPalette.isDark) RfThemeState.currentPalette.chipBg else Color(0xFFEFE8FD))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MINI)
                    val displayedBannerHint = chairCapacityWarning ?: selectedTool.hint
                    BasicText(
                        text = displayedBannerHint,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (chairCapacityWarning != null) Color(0xFFB42345) else RfThemeState.currentPalette.ink,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(RosaButtonGradient)
                            .clickable { showAiShapeNoticeModal = true }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Ayúdame con IA",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White),
                        )
                    }
                }

                val visibleCanvasNotice = chairCapacityWarning ?: sketchFallbackNotice
                if (!visibleCanvasNotice.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF4DA))
                            .border(1.dp, Color(0xFFE5B84B), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                        BasicText(
                            text = visibleCanvasNotice,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7A5700)),
                        )
                    }
                }

                SketchCanvasPreviewSurface(
                    elements = canvasElements,
                    isThreeDimensionalPreview = false,
                    interactiveEnabled = true,
                    activeTool = selectedTool,
                    activeColorHex = activeSwatchHex,
                    selectedElementId = selectedElementId,
                    onSelectElement = { chosenId ->
                        selectedElementId = chosenId
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp)),
                    onTapWithAspect = { normX, normY, canvasAspect ->
                        chairCapacityWarning = null
                        when (selectedTool) {
                            SketchToolKind.STAGE -> appendShapeAt(SketchElementKind.ARCH_STAGE, normX, normY, "Tarima")

                            SketchToolKind.ROUND_TABLE -> appendShapeAt(SketchElementKind.ROUND_TABLE, normX, normY, "Mesa")

                            SketchToolKind.RECT_TABLE -> appendShapeAt(SketchElementKind.RECT_TABLE, normX, normY, "Mesa")

                            SketchToolKind.CHAIRS -> {
                                val chairSerial = nextElementSerial
                                val placedChair = buildSmartChairElementForTap(
                                    elements = canvasElements,
                                    tapNormX = normX,
                                    tapNormY = normY,
                                    colorHex = activeSwatchHex,
                                    chairId = "chair-$chairSerial",
                                    canvasAspectRatio = canvasAspect,
                                )
                                if (placedChair != null) {
                                    nextElementSerial += 1
                                    selectedElementId = placedChair.id
                                    commitElementsMutation(canvasElements + placedChair)
                                } else {
                                    chairCapacityWarning = "Esta mesa ya alcanzó su máximo de 10 sillas."
                                }
                            }

                            SketchToolKind.TEXT -> {
                                pendingLabelCoordinates = Pair(normX, normY)
                                showLabelEditorModal = true
                            }

                            else -> Unit
                        }
                    },
                    onCommitFreehandStroke = { strokePoints ->
                        if (strokePoints.isNotEmpty()) {
                            val centerX = strokePoints.map { pt -> pt.first }.average().toFloat()
                            val centerY = strokePoints.map { pt -> pt.second }.average().toFloat()
                            val strokeId = nextElementSerial
                            nextElementSerial += 1
                            val createdStroke = SketchCanvasElement(
                                id = "stroke-$strokeId",
                                kind = SketchElementKind.FREEHAND_STROKE,
                                normX = centerX,
                                normY = centerY,
                                colorHex = activeSwatchHex,
                                points = strokePoints,
                            )
                            commitElementsMutation(canvasElements + createdStroke)
                        }
                    },
                    onCommitElementMove = { movedIndex, deltaX, deltaY ->
                        if (movedIndex in canvasElements.indices) {
                            val updatedList = translateSketchElementWithAttachedChairs(
                                elements = canvasElements,
                                movedIndex = movedIndex,
                                deltaX = deltaX,
                                deltaY = deltaY,
                            )
                            commitElementsMutation(updatedList)
                        }
                    },
                    onEraseAtNormalized = { normX, normY ->
                        val hitIndex = findNearestSketchElementIndex(canvasElements, normX, normY)
                        if (hitIndex in canvasElements.indices) {
                            val removedId = canvasElements[hitIndex].id
                            if (selectedElementId == removedId) {
                                selectedElementId = null
                            }
                            val remaining = removeSketchElementById(canvasElements, removedId)
                            commitElementsMutation(remaining)
                        }
                    },
                )

                if (selectedElement != null) {
                    val activeSelectedItem = selectedElement
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (RfThemeState.currentPalette.isDark) RfThemeState.currentPalette.chipBg else Color(0xFFF3ECFE))
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.MOVE, tint = RfThemeState.currentPalette.primaryAccent, size = 16.dp)
                        BasicText(
                            text = "${resolveSketchElementDisplayName(activeSelectedItem)} · Arrastra para mover",
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFFCE8EE))
                                .clickable {
                                    commitElementsMutation(removeSketchElementById(canvasElements, activeSelectedItem.id))
                                    selectedElementId = null
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RfIcon(kind = RfIconKind.DELETE, tint = Color(0xFFB42345), size = 13.dp)
                            BasicText(
                                text = "Borrar",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB42345),
                                ),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.surface)
                                .clickable { selectedElementId = null },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 13.dp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    swatchColors.forEachIndexed { index, swatchPair ->
                        val swatchColor = swatchPair.first
                        val isSelected = index == selectedSwatchIndex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
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

                SketchToolboxPanel(
                    selectedTool = selectedTool,
                    onSelectTool = { chosenTool ->
                        selectedTool = chosenTool
                    },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            onSelectScreenCode("s32")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Volver",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable {
                            syncSketchElementsIdempotently(canvasElements, viewModel)
                            onSelectScreenCode("s32")
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = "Guardar boceto",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
                }
            }

            var hasInteractedLabel by remember(showLabelEditorModal) { mutableStateOf(false) }
            var forceShowLabelError by remember(showLabelEditorModal) { mutableStateOf(false) }
            val labelError = rememberDebouncedValidationError(
                value = customLabelInput,
                hasInteracted = hasInteractedLabel,
                forceShowError = forceShowLabelError,
            ) { candidate ->
                InputValidator.validateEntityName(
                    rawValue = candidate,
                    fieldLabel = "La etiqueta",
                    minLength = 2,
                    maxLength = 30,
                    isRequired = true,
                )
            }

            RfCenteredAnimatedAssistantModal(
                visible = showAiShapeNoticeModal,
                zIndexOrder = 6f,
                onDismiss = { showAiShapeNoticeModal = false },
            ) {
                val aiNoticeInteraction = remember { MutableInteractionSource() }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .clickable(
                            interactionSource = aiNoticeInteraction,
                            indication = null,
                            onClick = {},
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MINI)
                        BasicText(
                            text = "Rosa IA dará forma a tu plano",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                    BasicText(
                        text = "Al aceptar, Rosa IA se encargará de organizar y darle forma automática al boceto de mesas, tarima y pista según tus $vmRosaGuests invitados.",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RfThemeState.currentPalette.chipBg)
                                .clickable { showAiShapeNoticeModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Cancelar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RosaButtonGradient)
                                .clickable(enabled = !isGeneratingAiSketch) {
                                    if (viewModel == null) {
                                        val balanced = buildAiBalancedSketchLayout(vmRosaGuests)
                                        commitElementsMutation(balanced)
                                        showAiShapeNoticeModal = false
                                    } else {
                                        isGeneratingAiSketch = true
                                        val promptText = "Organiza plano balanceado para $vmRosaGuests invitados"
                                        viewModel.requestRosaSketch(
                                            prompt = promptText,
                                            guests = vmRosaGuests,
                                        ) { proposal ->
                                            isGeneratingAiSketch = false
                                            val serverElements = if (proposal != null) mapProposalElementsToCanvas(proposal) else emptyList()
                                            val usedFallback = serverElements.isEmpty()
                                            val resolvedLayout = if (usedFallback) {
                                                sketchFallbackNotice = "IA remota no disponible · Se generó un plano local sugerido para $vmRosaGuests invitados."
                                                buildAiBalancedSketchLayout(vmRosaGuests)
                                            } else {
                                                sketchFallbackNotice = null
                                                serverElements
                                            }
                                            commitElementsMutation(resolvedLayout)
                                            syncSketchElementsIdempotently(resolvedLayout, viewModel)
                                            showAiShapeNoticeModal = false
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                if (isGeneratingAiSketch) "Diseñando con IA..." else "Aceptar y dar forma",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White),
                            )
                        }
                    }
                }
            }

            RfCenteredAnimatedAssistantModal(
                visible = showLabelEditorModal,
                zIndexOrder = 5f,
                onDismiss = { showLabelEditorModal = false },
            ) {
                val labelModalInteraction = remember { MutableInteractionSource() }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .clickable(
                            interactionSource = labelModalInteraction,
                            indication = null,
                            onClick = {},
                        )
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
                            val palette = RfThemeState.currentPalette
                            val isPresetSelected = customLabelInput.equals(presetLabel, ignoreCase = true)
                            val selectedChipBg = if (palette.isDark) Color(0xFF3B213D) else Color(0xFFFDE6F2)
                            val selectedChipFg = if (palette.isDark) Color(0xFFF48FB1) else Color(0xFFC2338F)
                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isPresetSelected) selectedChipBg else palette.chipBg)
                                    .clickable {
                                        customLabelInput = presetLabel
                                        hasInteractedLabel = true
                                        forceShowLabelError = false
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = presetLabel,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isPresetSelected) selectedChipFg else palette.ink,
                                    ),
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.dp,
                                    color = resolveAssistantErrorBorderColor(hasError = labelError != null),
                                    shape = RoundedCornerShape(14.dp),
                                )
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
                                onValueChange = { updatedValue ->
                                    customLabelInput = updatedValue.take(30)
                                    hasInteractedLabel = true
                                    forceShowLabelError = false
                                },
                                singleLine = true,
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                textStyle = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        RfValidationErrorText(errorMessage = labelError)
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
                                    val immediateError = InputValidator.validateEntityName(
                                        rawValue = customLabelInput,
                                        fieldLabel = "La etiqueta",
                                        minLength = 2,
                                        maxLength = 30,
                                        isRequired = true,
                                    )
                                    if (immediateError != null) {
                                        hasInteractedLabel = true
                                        forceShowLabelError = true
                                    } else {
                                        appendShapeAt(
                                            SketchElementKind.TEXT_LABEL,
                                            pendingLabelCoordinates.first,
                                            pendingLabelCoordinates.second,
                                            customLabelInput.trim(),
                                        )
                                        showLabelEditorModal = false
                                    }
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

@Composable
private fun SketchToolboxPanel(
    selectedTool: SketchToolKind,
    onSelectTool: (SketchToolKind) -> Unit,
) {
    val typo = LocalRfTypography.current
    val firstRowTools = remember {
        listOf(
            SketchToolKind.SELECT,
            SketchToolKind.PENCIL,
            SketchToolKind.STAGE,
            SketchToolKind.ROUND_TABLE,
        )
    }
    val secondRowTools = remember {
        listOf(
            SketchToolKind.RECT_TABLE,
            SketchToolKind.CHAIRS,
            SketchToolKind.TEXT,
            SketchToolKind.ERASE,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0x55B450B4),
                ambientColor = Color(0x1AB450B4),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        listOf(firstRowTools, secondRowTools).forEach { toolRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                toolRow.forEach { tool ->
                    val palette = RfThemeState.currentPalette
                    val isSelected = tool == selectedTool
                    val contentTint = if (isSelected) Color.White else palette.muted
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)),
                                        ),
                                    )
                                } else {
                                    Modifier.background(palette.chipBg)
                                },
                            )
                            .clickable { onSelectTool(tool) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        SketchToolIconGraphic(tool = tool, tint = contentTint)
                        BasicText(
                            text = tool.title,
                            maxLines = 1,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = contentTint,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SketchToolIconGraphic(
    tool: SketchToolKind,
    tint: Color,
) {
    val typo = LocalRfTypography.current
    when (tool) {
        SketchToolKind.SELECT -> RfIcon(kind = RfIconKind.MOVE, tint = tint, size = 15.dp)

        SketchToolKind.PENCIL -> RfIcon(kind = RfIconKind.EDIT, tint = tint, size = 15.dp)

        SketchToolKind.STAGE -> RfIcon(kind = RfIconKind.SPARKLE, tint = tint, size = 15.dp)

        SketchToolKind.ROUND_TABLE -> RfIcon(kind = RfIconKind.CIRCLE, tint = tint, size = 15.dp)

        SketchToolKind.RECT_TABLE -> RfIcon(kind = RfIconKind.TABLE, tint = tint, size = 15.dp)

        SketchToolKind.CHAIRS -> RfIcon(kind = RfIconKind.CHAIR, tint = tint, size = 15.dp)

        SketchToolKind.TEXT -> {
            BasicText(
                text = "Aa",
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = tint,
                ),
            )
        }

        SketchToolKind.ERASE -> RfIcon(kind = RfIconKind.CLOSE, tint = tint, size = 15.dp)
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
        mutableStateOf(parseSketchElements(vmSketchJson))
    }

    val activeEventTitle = remember(observedEvents, observedSelectedEventId, fallbackEventTitle) {
        viewModel?.activeClientEventOrNull()?.title
            ?: fallbackEventTitle.ifBlank { "Tu evento" }
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
                    .height(186.dp)
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
                        val singleChairsCount = previewElements.count { item -> item.kind == SketchElementKind.SINGLE_CHAIR }
                        val totalEstimatedChairs = singleChairsCount + (chairGroupsCount * 6)
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

                    var isGeneratingS78AiSketch by remember { mutableStateOf(false) }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .clickable(enabled = !isGeneratingS78AiSketch) {
                                if (viewModel == null) {
                                    val aiArranged = buildAiBalancedSketchLayout(vmRosaGuests)
                                    previewElements = aiArranged
                                } else {
                                    isGeneratingS78AiSketch = true
                                    viewModel.requestRosaSketch(
                                        prompt = "Generar boceto completo de mesas, arco y pista para $vmRosaGuests invitados",
                                        guests = vmRosaGuests,
                                    ) { proposal ->
                                        isGeneratingS78AiSketch = false
                                        val serverElements = if (proposal != null) mapProposalElementsToCanvas(proposal) else emptyList()
                                        val aiArranged = serverElements.ifEmpty { buildAiBalancedSketchLayout(vmRosaGuests) }
                                        previewElements = aiArranged
                                        syncSketchElementsIdempotently(aiArranged, viewModel)
                                    }
                                }
                            },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFFA23FB8), size = 18.dp)
                            BasicText(
                                if (isGeneratingS78AiSketch) "Organizando con Rosa IA..." else "Ayúdame con IA",
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
    val palette = RfThemeState.currentPalette
    val vmClients = viewModel?.clients?.collectAsState()?.value.orEmpty()
    val vmArticles = viewModel?.articles?.collectAsState()?.value.orEmpty()
    val clientCycle = remember(vmClients) {
        val dynamicNames = vmClients.map { client -> client.fullName }.filter { name -> name.isNotBlank() }
        if (dynamicNames.isNotEmpty()) {
            dynamicNames
        } else if (viewModel == null) {
            listOf("Camila Rodríguez", "Laura Peña", "Eventos Corporativos SD")
        } else {
            emptyList()
        }
    }
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
            promptText = voiceTranscript.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
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
    val chairUnitDop = vmArticles.firstOrNull { art -> art.id == "art-silla-tiffany" }?.pricePerDayDop ?: 8
    val tableUnitDop = vmArticles.firstOrNull { art -> art.id == "art-mesa-redonda" }?.pricePerDayDop ?: 20
    val archUnitDop = vmArticles.firstOrNull { art -> art.id == "art-arco-rosa" }?.pricePerDayDop ?: 95
    val dessertUnitDop = vmArticles.firstOrNull { art -> art.id == "art-mesa-helados" }?.pricePerDayDop ?: 150
    val baseEstimateDop = chairCount * chairUnitDop + tableCount * tableUnitDop + archUnitDop
    val lightingSurcharge = if (includeLighting) 180 else 0
    val dessertSurcharge = if (includeDessertStation) dessertUnitDop else 0
    val totalEstimateDop = baseEstimateDop + lightingSurcharge + dessertSurcharge
    val formattedEstimate = formatDopCurrency(totalEstimateDop)

    val syncDraftToViewModel = {
        val targetClient = assignedClient ?: clientCycle.firstOrNull() ?: "Cliente por asignar"
        val targetVenue = if (viewModel != null) {
            viewModel.eventVenueInput.value.trim()
                .ifBlank { viewModel.profileZone.value.trim() }
                .ifBlank { "Lugar por confirmar" }
        } else {
            "Jardín Los Rosales"
        }
        viewModel?.setEventTypeInput(eventTypeTitle)
        viewModel?.setEventDateInput(currentShortDate)
        viewModel?.setEventVenueInput(targetVenue)
        viewModel?.setEventGuestsInput(guestCount.toString())
        viewModel?.setNewEventTitle("$eventTypeTitle de $targetClient")
        viewModel?.setNewEventClient(targetClient)
        viewModel?.setNewEventDate(currentShortDate)
        viewModel?.setNewEventVenue(targetVenue)
        viewModel?.setNewEventGuests(guestCount.toString())
        viewModel?.setDraftQuantity("art-silla-tiffany", chairCount)
        viewModel?.setDraftQuantity("art-mesa-redonda", tableCount)
        viewModel?.setDraftQuantity("art-centro-floral", tableCount)
        viewModel?.setDraftQuantity("art-arco-rosa", 1)
        if (includeDessertStation) {
            viewModel?.setDraftQuantity("art-mesa-helados", 1)
        }
    }

    val handleUserCommand: (String, Boolean) -> Unit = { rawCommand, sendToRemote ->
        val trimmed = rawCommand.trim()
        if (trimmed.isNotEmpty()) {
            val lower = trimmed.lowercase()
            val replySegments = mutableListOf<String>()

            val mentionsClient = listOf("cliente", "asignar").any { keyword -> lower.contains(keyword) }
            val matchedClientName = vmClients.firstOrNull { client ->
                val firstName = client.fullName.trim().substringBefore(' ').lowercase()
                if (firstName.length >= 2) lower.contains(firstName) else false
            }?.fullName
            val shouldResolveClient = if (mentionsClient) true else matchedClientName != null
            if (shouldResolveClient) {
                val chosenClient = if (matchedClientName != null) {
                    matchedClientName
                } else if (viewModel == null) {
                    when {
                        lower.contains("laura") -> "Laura Peña"

                        lower.contains("ana") -> "Ana Gómez"

                        lower.contains("corporat") -> "Eventos Corporativos SD"

                        lower.contains("camila") -> "Camila Rodríguez"

                        clientCycle.isNotEmpty() -> {
                            val cycled = clientCycle[clientCycleIndex % clientCycle.size]
                            clientCycleIndex += 1
                            cycled
                        }

                        else -> ""
                    }
                } else if (clientCycle.isNotEmpty()) {
                    val cycled = clientCycle[clientCycleIndex % clientCycle.size]
                    clientCycleIndex += 1
                    cycled
                } else {
                    ""
                }
                if (chosenClient.isNotBlank()) {
                    assignedClient = chosenClient
                    viewModel?.setNewEventClient(chosenClient)
                    replySegments.add("Asigné el borrador a $chosenClient y vinculé su historial.")
                } else {
                    replySegments.add("No hay clientes registrados aún para asignar. Registra uno primero.")
                }
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
            val updatedDessertAdd = if (includeDessertStation) dessertUnitDop else 0
            val updatedTotal = updatedChairs * chairUnitDop + updatedTables * tableUnitDop + archUnitDop + updatedLightingAdd + updatedDessertAdd
            val replyText = if (replySegments.isNotEmpty()) {
                "${replySegments.joinToString(" ")} Nuevo estimado: ${formatDopCurrency(updatedTotal)}."
            } else {
                "Perfecto, tomé nota de \"$trimmed\" y actualicé las observaciones del borrador ($eventTypeTitle · ${formatDopCurrency(updatedTotal)})."
            }
            viewModel?.setRosaProposalMessage(trimmed)
            if (sendToRemote) {
                viewModel?.sendConversationalRosaPrompt(trimmed)
            }
            extraChatTurns = extraChatTurns + AdminRosaChatTurn(userText = trimmed, rosaReply = replyText)
        }
    }

    val adminIndigoPillBg = if (palette.isDark) palette.chipBg else Color(0xFFECEBFF)
    val adminIndigoPillInk = if (palette.isDark) Color(0xFFA7A5FF) else Color(0xFF4B47D6)

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
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable {
                            if (isVoiceListening) {
                                viewModel.stopVoiceInput()
                            }
                            onSelectScreenCode("BACK:A01")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }

                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp, forceAdminPalette = true)

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Rosa",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink, lineHeight = 20.sp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText("Asistente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = palette.muted))
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .background(adminIndigoPillBg, CircleShape)
                                .padding(horizontal = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Admin", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = adminIndigoPillInk))
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
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
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
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink, lineHeight = 21.sp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (palette.isDark) palette.chipBg else Color(0xFFF7F6FF))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        RfIcon(kind = RfIconKind.RING, tint = palette.ink, size = 16.dp)
                                        BasicText(
                                            text = "$eventTypeTitle · $currentShortDate",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
                                        )
                                    }
                                    if (assignedClient != null) {
                                        BasicText(
                                            text = "Cliente: $assignedClient",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = adminIndigoPillInk),
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .background(adminIndigoPillBg, CircleShape)
                                        .clickable {
                                            guestCount = if (guestCount == 120) 150 else 120
                                        }
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = "$guestCount invitados",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = adminIndigoPillInk),
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
                                    "Agregado" to (adminIndigoPillBg to adminIndigoPillInk),
                                )
                            } else {
                                baseProposalItems
                            }
                            val proposalItems = if (includeDessertStation) {
                                withLightingItems + Triple(
                                    RfPhotoKey.MESA_HELADOS,
                                    "Carrito de helados" to "1",
                                    "Agregado" to (adminIndigoPillBg to adminIndigoPillInk),
                                )
                            } else {
                                withLightingItems
                            }

                            proposalItems.forEach { proposalRow ->
                                val photo = proposalRow.first
                                val itemData = proposalRow.second
                                val tagInfo = proposalRow.third
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
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
                                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = palette.muted)) {
                                            append("× ${itemData.second}")
                                        }
                                    }
                                    BasicText(
                                        text = itemTitle,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
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

                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
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
                                    BasicText("Estimado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                                    BasicText(formattedEstimate, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink))
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
                                            .background(palette.surface)
                                            .border(1.dp, palette.fieldBorder, RoundedCornerShape(12.dp))
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
                                            RfIcon(kind = RfIconKind.EDIT, tint = palette.ink, size = 16.dp)
                                            BasicText("Ajustar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink))
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
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = turnItem.rosaReply,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.ink, lineHeight = 20.sp),
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
                                .background(adminIndigoPillBg)
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
                                    color = adminIndigoPillInk,
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
                            .background(if (hasClientAssigned) adminIndigoPillBg else palette.surface)
                            .border(1.dp, if (hasClientAssigned) Color(0xFF5B6CF0) else palette.line, CircleShape)
                            .clickable {
                                handleUserCommand("Asignar a un cliente", false)
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.USER, tint = if (hasClientAssigned) adminIndigoPillInk else palette.ink, size = 16.dp)
                            BasicText(
                                text = assignedClient ?: "Asignar a un cliente",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (hasClientAssigned) adminIndigoPillInk else palette.ink),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x804650B4), ambientColor = Color(0x304650B4))
                            .clip(CircleShape)
                            .background(if (includeLighting) adminIndigoPillBg else palette.surface)
                            .border(1.dp, if (includeLighting) Color(0xFF5B6CF0) else palette.line, CircleShape)
                            .clickable {
                                handleUserCommand("Agregar iluminación", false)
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.LIGHTBULB, tint = if (includeLighting) adminIndigoPillInk else palette.ink, size = 16.dp)
                            BasicText(
                                text = if (includeLighting) "Iluminación agregada" else "Agregar iluminación",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (includeLighting) adminIndigoPillInk else palette.ink),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x804650B4), ambientColor = Color(0x304650B4))
                            .clip(CircleShape)
                            .background(palette.surface)
                            .border(1.dp, palette.line, CircleShape)
                            .clickable {
                                handleUserCommand("Cambiar fecha", false)
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Cambiar fecha ($currentShortDate)",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
                        )
                    }
                }

                val adminPromptError = rememberOptionalPromptValidationError(promptText = promptText)
                val hasPromptText = promptText.isNotBlank()
                val submitAdminRosaPrompt = {
                    if (promptText.isNotBlank()) {
                        if (InputValidator.validateChatMessage(promptText) == null) {
                            if (isVoiceListening) {
                                viewModel.stopVoiceInput()
                            }
                            val submitted = promptText.trim()
                            promptText = ""
                            adminVoiceSessionStarted = false
                            handleUserCommand(submitted, true)
                        }
                    } else {
                        if (isVoiceListening) {
                            viewModel.stopVoiceInput()
                            if (voiceTranscript.isNotBlank()) {
                                promptText = voiceTranscript.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                            }
                        } else {
                            adminVoiceSessionStarted = true
                            viewModel?.startVoiceInput(VoiceInputOrigin.ROSA_WIZARD)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RfValidationErrorText(errorMessage = adminPromptError)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(14.dp, CircleShape, spotColor = Color(0xC04650B4), ambientColor = Color(0x504650B4))
                            .clip(CircleShape)
                            .background(palette.surface)
                            .border(
                                width = 1.dp,
                                color = resolveAssistantErrorBorderColor(
                                    hasError = adminPromptError != null,
                                    defaultColor = palette.fieldBorder,
                                ),
                                shape = CircleShape,
                            )
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
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.muted),
                                    )
                                }
                                BasicTextField(
                                    value = promptText,
                                    onValueChange = { updatedText ->
                                        promptText = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { submitAdminRosaPrompt() }),
                                    cursorBrush = SolidColor(palette.primaryAccent),
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
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
}
