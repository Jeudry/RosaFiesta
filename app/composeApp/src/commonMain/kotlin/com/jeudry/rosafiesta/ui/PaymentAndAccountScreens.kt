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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.designsystem.rfTrackValidationFocus
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PeriodSpendingAnalytics
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.SpendingAnalyticsCalculator
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.validation.InputValidator
import com.jeudry.rosafiesta.domain.validation.MaskedTextCursorResult
import kotlinx.coroutines.delay

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

private val AdminProfileButtonGradient = Brush.horizontalGradient(
    listOf(RfColors.IndigoBrand, RfColors.Teal),
)

private fun profileEditAccentColor(isAdmin: Boolean): Color = if (isAdmin) RfColors.IndigoTintInk else Color(0xFFC2338F)

private fun profileInputBorderColor(isAdmin: Boolean): Color = if (isAdmin) Color(0xFFC7C5EE) else Color(0xFFD6B9E8)

private data class QuoteHistoryItem49(
    val quoteId: String,
    val eventId: String,
    val code: String,
    val title: String,
    val dateLocation: String,
    val tagText: String,
    val tagBg: Color,
    val tagTextCol: Color,
    val stepText: String,
    val activeSteps: Int,
    val stepColor: Color,
    val guests: String,
    val price: String,
) {
    companion object {
        fun fromQuoteRecord(quoteItem: QuoteRecord): QuoteHistoryItem49 {
            val normalizedStatus = quoteItem.status.trim().uppercase()
            val isSigned = normalizedStatus in setOf("SIGNED", "FIRMADA", "CONFIRMADA")
            val isReadyToAccept = normalizedStatus in setOf("SENT", "ENVIADA", "APPROVED", "APROBADA")
            val isDeclined = normalizedStatus in setOf("REJECTED", "DECLINED", "RECHAZADA", "CANCELLED")
            val isAcceptedStage = if (isSigned) true else isReadyToAccept
            val tagText = when {
                isSigned -> "Aceptada"
                isReadyToAccept -> "Lista para aceptar"
                isDeclined -> "Ajuste solicitado"
                else -> "En revisión"
            }
            val tagBg = when {
                isAcceptedStage -> Color(0xFFEEF6D6)
                isDeclined -> Color(0xFFFFEDF1)
                else -> Color(0xFFFFF2C9)
            }
            val tagTextCol = when {
                isAcceptedStage -> Color(0xFF4E6A10)
                isDeclined -> Color(0xFFB42345)
                else -> Color(0xFF7A5700)
            }
            val activeSteps = when {
                isSigned -> 4
                isReadyToAccept -> 3
                isDeclined -> 1
                else -> 2
            }
            val stepColor = when {
                isAcceptedStage -> Color(0xFF8DB52C)
                isDeclined -> Color(0xFFD63F6A)
                else -> Color(0xFFF7B500)
            }
            val formattedPrice = RosaFiestaEngine.formatDop(quoteItem.totalDop)
            return QuoteHistoryItem49(
                quoteId = quoteItem.id,
                eventId = quoteItem.eventId,
                code = quoteItem.code,
                title = quoteItem.eventTitle,
                dateLocation = "${formatReadableSpanishDateText(quoteItem.dateLabel)} · ${quoteItem.venue}",
                tagText = tagText,
                tagBg = tagBg,
                tagTextCol = tagTextCol,
                stepText = "${quoteItem.code} · Paso $activeSteps de 5 · $tagText",
                activeSteps = activeSteps,
                stepColor = stepColor,
                guests = "${quoteItem.guests} invitados",
                price = formattedPrice,
            )
        }
    }
}

private fun filterQuotesForActiveSession(
    allQuotes: List<QuoteRecord>,
    viewModel: RosaFiestaViewModel?,
    profileEmail: String,
    profileName: String,
    isExplicitGuest: Boolean,
): List<QuoteRecord> {
    if (viewModel == null) {
        return allQuotes
    }
    if (viewModel.isCurrentSessionAdminOrStaff()) {
        return allQuotes
    }
    if (isExplicitGuest) {
        return emptyList()
    }
    val ownedEventIds = viewModel.clientEventsForCurrentUser().map { eventItem -> eventItem.id }.toSet()
    val cleanEmail = profileEmail.trim().lowercase()
    val cleanName = profileName.trim().lowercase()
    return allQuotes.filter { quoteItem ->
        val matchesOwnedEvent = ownedEventIds.contains(quoteItem.eventId)
        val quoteEmail = quoteItem.clientEmail.trim().lowercase()
        val matchesEmail = if (cleanEmail.isNotEmpty()) quoteEmail == cleanEmail else false
        val quoteClientName = quoteItem.clientName.trim().lowercase()
        val matchesName = if (cleanName.isNotEmpty()) quoteClientName == cleanName else false
        when {
            matchesOwnedEvent -> true
            matchesEmail -> true
            matchesName -> true
            else -> false
        }
    }
}

@Composable
internal fun RenderQuoteHistoryScreenS49(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val allQuotes = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    viewModel?.events?.collectAsState()?.value
    viewModel?.currentAuthenticatedUser?.collectAsState()?.value
    val profileEmail = viewModel?.profileEmail?.collectAsState()?.value.orEmpty()
    val profileName = viewModel?.profileName?.collectAsState()?.value.orEmpty()
    val isExplicitGuest = viewModel?.isExplicitGuestSession?.collectAsState()?.value ?: false

    val visibleQuotes = filterQuotesForActiveSession(
        allQuotes = allQuotes,
        viewModel = viewModel,
        profileEmail = profileEmail,
        profileName = profileName,
        isExplicitGuest = isExplicitGuest,
    )
    val quoteCards = visibleQuotes.map { quoteRecord -> QuoteHistoryItem49.fromQuoteRecord(quoteRecord) }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cotizaciones",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = palette.ink,
                        ),
                    )
                    BasicText(
                        text = "${quoteCards.size} registrada(s)",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = palette.muted,
                        ),
                    )
                }
            }

            if (quoteCards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFFC2338F), size = 32.dp)
                    BasicText(
                        text = "Aún no tienes cotizaciones activas",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = palette.ink,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    BasicText(
                        text = "Diseña tu evento con Rosa IA o explora el catálogo para recibir una cotización formal.",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.5.sp,
                            color = palette.muted,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RosaButtonGradient)
                            .clickable { onSelectScreenCode("s26") }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Cotizar con Rosa IA",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                            ),
                        )
                    }
                }
            } else {
                quoteCards.forEach { quoteItem ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(22.dp),
                                spotColor = Color(0x30505078),
                                ambientColor = Color(0x15505078),
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                            .clickable {
                                viewModel?.selectQuote(quoteItem.quoteId)
                                if (quoteItem.eventId.isNotBlank()) {
                                    viewModel?.selectEvent(quoteItem.eventId)
                                }
                                onSelectScreenCode("s48")
                            }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    BasicText(
                                        text = quoteItem.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = palette.ink,
                                        ),
                                    )
                                    BasicText(
                                        text = quoteItem.dateLocation,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 13.sp,
                                            color = palette.muted,
                                        ),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(quoteItem.tagBg)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = quoteItem.tagText,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = quoteItem.tagTextCol,
                                        ),
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    for (stepNumber in 1..5) {
                                        val barColor = if (stepNumber <= quoteItem.activeSteps) {
                                            quoteItem.stepColor
                                        } else {
                                            palette.chipBg
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(barColor),
                                        )
                                    }
                                }
                                BasicText(
                                    text = quoteItem.stepText,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = palette.muted,
                                    ),
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    RfIcon(kind = RfIconKind.GUESTS, tint = palette.muted, size = 16.dp)
                                    BasicText(
                                        text = quoteItem.guests,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 14.sp,
                                            color = palette.muted,
                                        ),
                                    )
                                }
                                BasicText(
                                    text = quoteItem.price,
                                    style = TextStyle(
                                        fontFamily = typo.displayFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = palette.ink,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawDigits = text.text
        val formattedBuilder = StringBuilder()
        for (index in rawDigits.indices) {
            val needsSeparator = index > 0 && index % 4 == 0
            if (needsSeparator) {
                formattedBuilder.append(' ')
            }
            formattedBuilder.append(rawDigits[index])
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, rawDigits.length)
                if (clampedOffset == 0) {
                    return 0
                }
                return clampedOffset + (clampedOffset - 1) / 4
            }

            override fun transformedToOriginal(offset: Int): Int {
                val formattedLength = formattedBuilder.length
                val clampedOffset = offset.coerceIn(0, formattedLength)
                val spaceCount = clampedOffset / 5
                return (clampedOffset - spaceCount).coerceIn(0, rawDigits.length)
            }
        }
        return TransformedText(
            text = AnnotatedString(formattedBuilder.toString()),
            offsetMapping = offsetMapping,
        )
    }
}

private fun validateCheckoutPaymentInputs(
    selectedMethod: String,
    transferRefInput: String,
): String? = when (selectedMethod) {
    "Transferencia" -> InputValidator.validateBankReference(transferRefInput)
    else -> null
}

@Composable
internal fun RenderPaymentCheckoutScreenS50(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val activeEvent = if (viewModel != null) {
        viewModel.activeClientEventOrNull()
    } else {
        events.firstOrNull { currentEvent -> currentEvent.id == selectedId }
            ?: events.firstOrNull()
            ?: CanonicalSampleData.events.first()
    }
    if (activeEvent == null) {
        PersonalEmptyEventStateScaffold(
            headerTitle = "Pago y reserva",
            backTargetCode = "BACK:s37",
            message = "Crea un evento o aprueba una cotización antes de realizar un pago.",
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }
    val checkoutModeRaw = viewModel?.paymentCheckoutMode?.collectAsState()?.value ?: "DEPOSIT"
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: BusinessSettings()
    val profileZone = viewModel?.profileZone?.collectAsState()?.value.orEmpty()
    val profilePhone = viewModel?.profilePhone?.collectAsState()?.value.orEmpty()
    val savedMethod = viewModel?.savedPaymentMethod?.collectAsState()?.value.orEmpty()
    val persistedRef = viewModel?.paymentRefInput?.collectAsState()?.value.orEmpty()
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    val isBalanceMode = checkoutModeRaw.trim().equals("BALANCE", ignoreCase = true)
    val depositPercent = businessSettings.depositPercent.coerceIn(10, 90)
    val rawDepositDop = ((activeEvent.totalDop * depositPercent) / 100).coerceAtLeast(1)
    val hasSmallerPositiveBalance = if (activeEvent.balanceDop > 0) activeEvent.balanceDop < rawDepositDop else false
    val depositToPayDop = if (hasSmallerPositiveBalance) activeEvent.balanceDop else rawDepositDop
    val amountToPayDop = if (isBalanceMode) {
        activeEvent.balanceDop.coerceAtLeast(1)
    } else {
        depositToPayDop
    }

    val initialMethod = when {
        savedMethod.contains("Transferencia", ignoreCase = true) -> "Transferencia"
        savedMethod.contains("Efectivo", ignoreCase = true) -> "Efectivo en tienda"
        else -> "Tarjeta"
    }
    var selectedMethod by remember { mutableStateOf(initialMethod) }
    var selectedBank by remember { mutableStateOf("Banreservas") }
    var transferRefInput by remember { mutableStateOf(persistedRef) }
    var selectedNcfType by remember { mutableStateOf(viewModel?.paymentNcfType?.value ?: "B02") }
    var clientRncInput by remember { mutableStateOf(viewModel?.paymentClientRnc?.value.orEmpty()) }
    val uploadedVoucherUrl = viewModel?.paymentVoucherUrl?.collectAsState()?.value.orEmpty()
    val pickedVoucherFile = viewModel?.pickedAttachmentFile?.collectAsState()?.value

    val itbisRate = businessSettings.itbisPercent.coerceAtLeast(0)
    val itbisDivisor = 100 + itbisRate
    val netSubtotalDop = (amountToPayDop * 100) / itbisDivisor
    val calculatedItbisDop = (amountToPayDop - netSubtotalDop).coerceAtLeast(0)

    var contactPhoneText by remember(profilePhone) { mutableStateOf(InputValidator.formatPhoneDisplay(profilePhone)) }

    val fallbackAddress = "${activeEvent.venue} · ${activeEvent.zone}"
    val displayedDeliveryAddress = profileZone.ifBlank { fallbackAddress }
    var isEditingAddress by remember { mutableStateOf(false) }
    var draftDeliveryAddress by remember(displayedDeliveryAddress) { mutableStateOf(displayedDeliveryAddress) }
    var localCheckoutError by remember { mutableStateOf<String?>(null) }

    var hasInteractedDeliveryAddress by remember { mutableStateOf(false) }
    var forceShowAddressError by remember { mutableStateOf(false) }
    var hasInteractedTransferRef by remember { mutableStateOf(false) }
    var hasInteractedContactPhone by remember { mutableStateOf(false) }
    var forceShowCheckoutErrors by remember { mutableStateOf(false) }

    val deliveryAddressBlur = rememberFieldBlurState()
    val transferRefBlur = rememberFieldBlurState()
    val contactPhoneBlur = rememberFieldBlurState()

    val debouncedAddressError = rememberDebouncedValidationError(
        value = draftDeliveryAddress,
        hasInteracted = hasInteractedDeliveryAddress,
        forceShowError = forceShowAddressError,
        hasBlurred = deliveryAddressBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateAddress(
            rawValue = candidate,
            fieldLabel = "La dirección de entrega",
            isRequired = true,
        )
    }
    val debouncedTransferRefError = rememberDebouncedValidationError(
        value = transferRefInput,
        hasInteracted = hasInteractedTransferRef,
        forceShowError = forceShowCheckoutErrors,
        hasBlurred = transferRefBlur.hasBlurred,
    ) { candidate ->
        if (selectedMethod != "Transferencia") {
            null
        } else {
            InputValidator.validateBankReference(candidate)
        }
    }
    val debouncedContactPhoneError = rememberDebouncedValidationError(
        value = contactPhoneText,
        hasInteracted = hasInteractedContactPhone,
        forceShowError = forceShowCheckoutErrors,
        hasBlurred = contactPhoneBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateDominicanPhone(
            rawValue = candidate,
            isRequired = false,
            allowInternational = false,
            fieldLabel = "El teléfono de contacto",
        )
    }

    val cashReferenceCode = "EFE-${activeEvent.code}"
    val formattedAmountToPay = RosaFiestaEngine.formatDop(amountToPayDop)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(palette.radialPrimary, palette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(palette.radialSecondary, palette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .clickable { onSelectScreenCode("BACK:s48") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = if (isBalanceMode) "Liquidar saldo" else "Pago de anticipo",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = palette.ink),
                    )
                    BasicText(
                        text = activeEvent.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RfIcon(kind = RfIconKind.SHIELD, tint = palette.muted, size = 14.dp)
                    BasicText("Pago seguro", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                }
            }

            if (localCheckoutError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(rfAdminDangerPillBg())
                        .border(1.dp, RfColors.Error.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = localCheckoutError.orEmpty(),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = rfAdminDangerPillInk()),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.chipBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val modeOptions = listOf(
                    "DEPOSIT" to "Anticipo ($depositPercent%)",
                    "BALANCE" to "Saldo total",
                )
                modeOptions.forEach { modePair ->
                    val modeCode = modePair.first
                    val modeTitle = modePair.second
                    val selected = if (modeCode == "BALANCE") isBalanceMode else !isBalanceMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) palette.surface else Color.Transparent)
                            .clickable {
                                viewModel?.setPaymentCheckoutMode(modeCode)
                                localCheckoutError = null
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = modeTitle,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selected) palette.ink else palette.muted,
                            ),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(rfAdminSuccessPillBg()),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.TRUCK, tint = rfAdminSuccessPillInk(), size = 20.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Entrega para ${activeEvent.clientName}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = palette.muted),
                        )
                        BasicText(
                            text = displayedDeliveryAddress,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = palette.ink),
                        )
                    }

                    BasicText(
                        text = if (isEditingAddress) "Cancelar" else "Cambiar",
                        modifier = Modifier.clickable {
                            draftDeliveryAddress = displayedDeliveryAddress
                            hasInteractedDeliveryAddress = false
                            deliveryAddressBlur.reset()
                            forceShowAddressError = false
                            isEditingAddress = !isEditingAddress
                        },
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                    )
                }

                if (isEditingAddress) {
                    val addressBorderColor = if (debouncedAddressError != null) RfColors.Error else Color(0xFFD6B9E8)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.bg)
                                .border(1.dp, addressBorderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (draftDeliveryAddress.isEmpty()) {
                                BasicText(
                                    text = "Dirección o salón del evento",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted),
                                )
                            }
                            BasicTextField(
                                value = draftDeliveryAddress,
                                onValueChange = { nextAddress ->
                                    draftDeliveryAddress = nextAddress.take(InputValidator.MAX_ADDRESS_LENGTH)
                                    hasInteractedDeliveryAddress = true
                                    localCheckoutError = null
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.ink),
                                cursorBrush = SolidColor(Color(0xFFC2338F)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .rfTrackValidationFocus(
                                        focusKey = deliveryAddressBlur,
                                        onFocusChanged = deliveryAddressBlur::onFocusStateChanged,
                                    ),
                            )
                        }
                        RfValidationErrorText(errorMessage = debouncedAddressError)
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFC2338F))
                                .clickable {
                                    val addressError = InputValidator.validateAddress(
                                        rawValue = draftDeliveryAddress,
                                        fieldLabel = "La dirección de entrega",
                                        isRequired = true,
                                    )
                                    if (addressError != null) {
                                        forceShowAddressError = true
                                        localCheckoutError = addressError
                                    } else {
                                        viewModel?.setProfileZone(draftDeliveryAddress.trim())
                                        forceShowAddressError = false
                                        localCheckoutError = null
                                        isEditingAddress = false
                                    }
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Guardar dirección",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White),
                            )
                        }
                    }
                }
            }

            BasicText(
                text = "Método de pago",
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink),
            )

            val paymentMethods = listOf("Transferencia", "Tarjeta", "Efectivo en tienda")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentMethods.forEach { methodName ->
                    val isMethodSelected = selectedMethod == methodName
                    val methodIcon = when (methodName) {
                        "Transferencia" -> RfIconKind.ORDERS
                        else -> RfIconKind.WALLET
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.surface)
                            .border(
                                width = if (isMethodSelected) 2.dp else 1.dp,
                                color = if (isMethodSelected) Color(0xFFB754CB) else palette.line,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable {
                                selectedMethod = methodName
                                localCheckoutError = null
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RfIcon(kind = methodIcon, tint = palette.ink, size = 20.dp)
                            BasicText(
                                text = methodName,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = palette.ink),
                                modifier = Modifier.weight(1f),
                            )
                            if (isMethodSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .shadow(6.dp, CircleShape, spotColor = Color(0xB36664E9))
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 12.dp)
                                }
                            }
                        }
                    }
                }
            }

            when (selectedMethod) {
                "Transferencia" -> {
                    BasicText(
                        text = "Banco receptor y comprobante",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink),
                    )
                    val availableBanks = listOf("Banreservas", "Banco Popular", "BHD")
                    val selectedPillBg = if (palette.isDark) Color(0xFF3E1930) else Color(0xFFFDE6F2)
                    val selectedPillInk = if (palette.isDark) Color(0xFFF48FB1) else Color(0xFFC2338F)
                    val indigoTintBg = rfAdminIndigoPillBg()
                    val indigoTintInk = rfAdminIndigoPillInk()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        availableBanks.forEach { bankOption ->
                            val isBankSelected = selectedBank == bankOption
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isBankSelected) selectedPillBg else palette.surface)
                                    .border(
                                        width = if (isBankSelected) 1.5.dp else 1.dp,
                                        color = if (isBankSelected) selectedPillInk else palette.line,
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    .clickable {
                                        selectedBank = bankOption
                                        localCheckoutError = null
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = bankOption,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = if (isBankSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = if (isBankSelected) selectedPillInk else palette.ink,
                                    ),
                                )
                            }
                        }
                    }

                    val accountInstructions = when (selectedBank) {
                        "Banco Popular" -> "Cuenta Corriente Popular · 794-88210-3 · RosaFiesta SRL (RNC ${businessSettings.rnc})"
                        "BHD" -> "Cuenta Corriente BHD · 214-0093812-0 · RosaFiesta SRL (RNC ${businessSettings.rnc})"
                        else -> "Cuenta Corriente Banreservas · 960-114892-1 · RosaFiesta SRL (RNC ${businessSettings.rnc})"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(indigoTintBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = accountInstructions,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = indigoTintInk),
                        )
                    }

                    CheckoutInputBox50(
                        value = transferRefInput,
                        placeholder = "Número de referencia o comprobante (ej. BR-994812)",
                        leadingIcon = RfIconKind.ORDERS,
                        isError = debouncedTransferRefError != null,
                        errorMessage = debouncedTransferRefError,
                        onFocusChanged = transferRefBlur::onFocusStateChanged,
                        onValueChange = { nextRef ->
                            transferRefInput = nextRef.take(InputValidator.MAX_BANK_REFERENCE_LENGTH)
                            hasInteractedTransferRef = true
                            viewModel?.setPaymentRefInput(transferRefInput)
                            localCheckoutError = null
                        },
                    )

                    val hasUploadedVoucher = uploadedVoucherUrl.isNotBlank()
                    val voucherLabel = when {
                        hasUploadedVoucher -> "Comprobante subido (${pickedVoucherFile?.fileName ?: "archivo"})"
                        pickedVoucherFile != null -> "Subiendo ${pickedVoucherFile.fileName}..."
                        else -> "Adjuntar comprobante de transferencia (JPG/PNG/PDF)"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (hasUploadedVoucher) rfAdminSuccessPillBg() else palette.chipBg)
                            .border(
                                width = 1.5.dp,
                                color = if (hasUploadedVoucher) rfAdminSuccessPillInk() else palette.fieldBorder,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                viewModel?.pickMediaAttachment(listOf("jpg", "jpeg", "png", "pdf")) { chosenFile ->
                                    viewModel.uploadAndAttachMedia("voucher", chosenFile) { uploadedUrl ->
                                        viewModel.setPaymentVoucherUrl(uploadedUrl)
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RfIcon(
                                kind = if (hasUploadedVoucher) RfIconKind.CHECK else RfIconKind.CAMERA,
                                tint = if (hasUploadedVoucher) rfAdminSuccessPillInk() else palette.primaryAccent,
                                size = 18.dp,
                            )
                            BasicText(
                                text = voucherLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (hasUploadedVoucher) rfAdminSuccessPillInk() else palette.ink,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    if (hasUploadedVoucher) {
                        RfUploadedOrCatalogPhoto(
                            photoUrl = uploadedVoucherUrl,
                            fallbackPhotoKey = RfPhotoKey.BODA,
                            contentDescription = "Vista previa del comprobante",
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            cornerRadius = 14.dp,
                        )
                    }
                }

                "Efectivo en tienda" -> {
                    BasicText(
                        text = "Pago presencial en showroom",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = businessSettings.address,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink),
                        )
                        BasicText(
                            text = "Horario: ${businessSettings.businessHours}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted),
                        )
                        BasicText(
                            text = "Orden de caja generada: $cashReferenceCode",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = rfAdminSuccessPillInk()),
                        )
                    }
                }

                else -> {
                    BasicText(
                        text = "Pago seguro con tarjeta vía Stripe",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(rfAdminIndigoPillBg())
                            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = "Al continuar abriremos la pasarela cifrada de Stripe en tu navegador para completar el pago con tarjeta de forma segura.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = palette.ink),
                        )
                        BasicText(
                            text = "No almacenamos números ni códigos CVV de tu tarjeta en el dispositivo.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = rfAdminIndigoPillInk()),
                        )
                    }
                }
            }

            CheckoutInputBox50(
                value = contactPhoneText,
                placeholder = "Teléfono de contacto (ej. 809-555-0142)",
                leadingIcon = RfIconKind.PHONE,
                isError = debouncedContactPhoneError != null,
                errorMessage = debouncedContactPhoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                cursorMask = InputValidator::filterPhoneInputWithCursor,
                onFocusChanged = contactPhoneBlur::onFocusStateChanged,
                onValueChange = { updatedPhone ->
                    contactPhoneText = updatedPhone
                    hasInteractedContactPhone = true
                    localCheckoutError = null
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    text = "Comprobante fiscal (NCF · DGII)",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink),
                )
                val selectedNcfBg = if (palette.isDark) Color(0xFF3E1930) else Color(0xFFFDE6F2)
                val selectedNcfInk = if (palette.isDark) Color(0xFFF48FB1) else Color(0xFFC2338F)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("B02" to "Consumidor Final (B02)", "B01" to "Crédito Fiscal (B01)").forEach { ncfPair ->
                        val ncfCode = ncfPair.first
                        val isNcfSelected = selectedNcfType == ncfCode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isNcfSelected) selectedNcfBg else palette.chipBg)
                                .border(
                                    width = if (isNcfSelected) 1.5.dp else 1.dp,
                                    color = if (isNcfSelected) selectedNcfInk else palette.line,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable {
                                    selectedNcfType = ncfCode
                                    viewModel?.setPaymentNcfType(ncfCode)
                                    localCheckoutError = null
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = ncfPair.second,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isNcfSelected) selectedNcfInk else palette.ink,
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                    }
                }
                if (selectedNcfType == "B01") {
                    CheckoutInputBox50(
                        value = clientRncInput,
                        placeholder = "RNC (9 dígitos) o Cédula (11 dígitos)",
                        leadingIcon = RfIconKind.SHIELD,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorMask = { prev, next, sel ->
                            InputValidator.filterDigitsOnlyWithCursor(prev, next, sel, 11)
                        },
                        onValueChange = { cleanDigits ->
                            clientRncInput = cleanDigits
                            viewModel?.setPaymentClientRnc(cleanDigits)
                            localCheckoutError = null
                        },
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BasicText("Subtotal gravado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted))
                    BasicText(RosaFiestaEngine.formatDop(netSubtotalDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = palette.ink))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BasicText("ITBIS ($itbisRate%)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted))
                    BasicText(RosaFiestaEngine.formatDop(calculatedItbisDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = palette.ink))
                }
                val estimatedDepositGuaranteeDop = ((activeEvent.totalDop * 10) / 100).coerceAtLeast(0)
                if (estimatedDepositGuaranteeDop > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Garantía reembolsable est. (10%)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = rfAdminSuccessPillInk()))
                        BasicText(RosaFiestaEngine.formatDop(estimatedDepositGuaranteeDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminSuccessPillInk()))
                    }
                }
            }

            if (!localCheckoutError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(rfAdminDangerPillBg())
                        .border(1.dp, RfColors.Error.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                ) {
                    BasicText(
                        text = localCheckoutError!!,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = rfAdminDangerPillInk()),
                    )
                }
            }

            val pendingCardSession = viewModel?.pendingCardCheckoutSession?.collectAsState()?.value
            if (pendingCardSession != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(rfAdminIndigoPillBg())
                        .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BasicText(
                        text = "Pasarela Stripe abierta (${pendingCardSession.sessionId.takeLast(8)})",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = rfAdminIndigoPillInk()),
                    )
                    BasicText(
                        text = "Cuando completes el pago en tu navegador, presiona verificar para emitir tu comprobante oficial.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, lineHeight = 18.sp, color = palette.ink),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RosaButtonGradient)
                            .clickable(enabled = !isSubmitting) {
                                viewModel.confirmPendingCardCheckout(
                                    sessionId = pendingCardSession.sessionId,
                                    onConfirmed = {
                                        localCheckoutError = null
                                        onSelectScreenCode("s51")
                                    },
                                    onError = { errMsg -> localCheckoutError = errMsg },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = if (isSubmitting) "Verificando pago..." else "Verificar pago con tarjeta",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(palette.surface)
                .border(1.dp, palette.line)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 34.dp),
        ) {
            val buttonLabel = when {
                isSubmitting -> "Procesando pago…"
                isBalanceMode -> "Liquidar saldo pendiente"
                else -> "Pagar anticipo ($depositPercent%)"
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSubmitting) palette.muted else Color.Transparent)
                    .then(if (isSubmitting) Modifier else Modifier.background(RosaButtonGradient))
                    .clickable(enabled = !isSubmitting) {
                        if (isSubmitting) return@clickable
                        val methodValidationError = validateCheckoutPaymentInputs(
                            selectedMethod = selectedMethod,
                            transferRefInput = transferRefInput,
                        )
                        val phoneError = InputValidator.validateDominicanPhone(
                            rawValue = contactPhoneText,
                            isRequired = false,
                            allowInternational = false,
                            fieldLabel = "El teléfono de contacto",
                        )
                        val firstError = methodValidationError ?: phoneError
                        if (firstError != null) {
                            forceShowCheckoutErrors = true
                            localCheckoutError = firstError
                            viewModel?.soundPlayer?.playAlertError()
                            return@clickable
                        }

                        val resolvedMethodLabel = when (selectedMethod) {
                            "Transferencia" -> "Transferencia · $selectedBank"
                            "Efectivo en tienda" -> "Efectivo en tienda"
                            else -> "Tarjeta (Stripe)"
                        }
                        val resolvedReference = when (selectedMethod) {
                            "Transferencia" -> transferRefInput.trim()
                            "Efectivo en tienda" -> cashReferenceCode
                            else -> "STRIPE-CHECKOUT"
                        }

                        val trimmedContactPhone = contactPhoneText.trim()
                        val activeViewModel = viewModel
                        if (activeViewModel == null) {
                            localCheckoutError = null
                            onSelectScreenCode("s51")
                        } else if (selectedMethod == "Tarjeta") {
                            activeViewModel.startCardCheckoutSession(
                                eventId = activeEvent.id,
                                amountDop = amountToPayDop,
                                ncfType = selectedNcfType,
                                clientRnc = clientRncInput,
                                contactPhone = trimmedContactPhone,
                                onSessionReady = {
                                    localCheckoutError = null
                                },
                                onUnavailableOrError = { gatewayError ->
                                    localCheckoutError = gatewayError
                                },
                            )
                        } else {
                            activeViewModel.setPaymentAmountInput(amountToPayDop.toString())
                            activeViewModel.setPaymentMethodInput(resolvedMethodLabel)
                            activeViewModel.setPaymentRefInput(resolvedReference)
                            activeViewModel.setSavedPaymentMethod(resolvedMethodLabel)
                            val paidRecord = activeViewModel.submitDetailedPaymentFlow(
                                eventId = activeEvent.id,
                                amountDop = amountToPayDop,
                                methodLabel = resolvedMethodLabel,
                                bankName = if (selectedMethod == "Transferencia") selectedBank else "",
                                referenceCode = resolvedReference,
                                voucherUrl = uploadedVoucherUrl,
                                ncfType = selectedNcfType,
                                clientRnc = clientRncInput,
                                contactPhone = trimmedContactPhone,
                                kindLabel = if (isBalanceMode) "Saldo" else "Abono",
                            )
                            if (paidRecord != null) {
                                localCheckoutError = null
                                onSelectScreenCode("s51")
                            } else {
                                localCheckoutError = activeViewModel.formError.value
                                    ?: "No se pudo registrar el pago. Revisa los datos ingresados."
                            }
                        }
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = buttonLabel,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = Color.White),
                    )
                    BasicText(
                        text = formattedAmountToPay,
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.5.sp, color = Color.White),
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutInputBox50(
    value: String,
    placeholder: String,
    leadingIcon: RfIconKind?,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    cursorMask: ((previousFormatted: String, newRawText: String, newSelectionEnd: Int) -> MaskedTextCursorResult)? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val fieldFocusKey = remember { Any() }
    val hasFieldError = if (isError) true else errorMessage != null
    val borderColor = if (hasFieldError) RfColors.Error else palette.line

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.surface)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (leadingIcon != null) {
                    RfIcon(kind = leadingIcon, tint = palette.muted, size = 18.dp)
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        BasicText(
                            text = placeholder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = palette.muted),
                        )
                    }
                    if (cursorMask != null) {
                        RfMaskedDigitBasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            maskTransform = cursorMask,
                            singleLine = true,
                            visualTransformation = visualTransformation,
                            keyboardOptions = keyboardOptions,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                            cursorBrush = SolidColor(Color(0xFFC2338F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfTrackValidationFocus(
                                    focusKey = fieldFocusKey,
                                    onFocusChanged = { isFocused -> onFocusChanged?.invoke(isFocused) },
                                ),
                        )
                    } else {
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            singleLine = true,
                            keyboardOptions = keyboardOptions,
                            visualTransformation = visualTransformation,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                            cursorBrush = SolidColor(Color(0xFFC2338F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfTrackValidationFocus(
                                    focusKey = fieldFocusKey,
                                    onFocusChanged = { isFocused -> onFocusChanged?.invoke(isFocused) },
                                ),
                        )
                    }
                }
            }
        }
        RfValidationErrorText(errorMessage = errorMessage)
    }
}

@Composable
internal fun RenderPaymentSuccessScreenS51(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val payments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val selectedId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val event = if (viewModel != null) {
        viewModel.activeClientEventOrNull()
    } else {
        events.firstOrNull { currentEvent -> currentEvent.id == selectedId }
            ?: events.firstOrNull()
            ?: CanonicalSampleData.events.first()
    }
    if (event == null) {
        PersonalEmptyEventStateScaffold(
            headerTitle = "Confirmación de pago",
            backTargetCode = "BACK:s37",
            message = "Aún no tienes pagos registrados porque todavía no creas un evento.",
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }
    val clientPayments = viewModel?.clientPaymentsForCurrentUser() ?: payments
    val latestPayment = clientPayments.firstOrNull { paymentItem -> paymentItem.eventId == event.id }
        ?: payments.firstOrNull { paymentItem -> paymentItem.eventId == event.id }
        ?: clientPayments.firstOrNull()
    val fallbackPaid = if (event.paidDepositDop > 0) event.paidDepositDop else (event.totalDop * 30 / 100).coerceAtLeast(100)
    val paidAmountDop = latestPayment?.amountDop ?: fallbackPaid
    val receiptCode = latestPayment?.receiptNumber ?: "REC-${event.code}"
    val paymentMethodLabel = latestPayment?.methodLabel ?: event.depositMethodLabel
    val pendingCardSession = viewModel?.pendingCardCheckoutSession?.collectAsState()?.value
    val isSubmittingCardConfirm = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    var cardVerifyError by remember { mutableStateOf<String?>(null) }
    var shareConfirmed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        // Decorative background garland & shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 390f
            val scaleY = size.height / 844f

            // Garland wire
            val wirePath = Path().apply {
                moveTo(-10f * scaleX, 50f * scaleY)
                quadraticTo(195f * scaleX, 110f * scaleY, 400f * scaleX, 50f * scaleY)
            }
            drawPath(
                wirePath,
                color = RfThemeState.currentPalette.ink.copy(alpha = 0.18f),
                style = Stroke(width = 1.2f * density),
            )

            // 9 Garland flags
            val flags = listOf(
                Triple(listOf(Offset(14f, 56f), Offset(34f, 56f), Offset(24f, 80f)), Color(0xFFEC4F97), 1f),
                Triple(listOf(Offset(58f, 66f), Offset(78f, 66f), Offset(68f, 90f)), Color(0xFFFBCA3F), 1f),
                Triple(listOf(Offset(102f, 73f), Offset(122f, 73f), Offset(112f, 97f)), Color(0xFF5CC9C0), 1f),
                Triple(listOf(Offset(146f, 78f), Offset(166f, 78f), Offset(156f, 102f)), Color(0xFF865DEC), 1f),
                Triple(listOf(Offset(190f, 79f), Offset(210f, 79f), Offset(200f, 103f)), Color(0xFFB9D84A), 1f),
                Triple(listOf(Offset(234f, 77f), Offset(254f, 77f), Offset(244f, 101f)), Color(0xFFEC4F97), 1f),
                Triple(listOf(Offset(278f, 73f), Offset(298f, 73f), Offset(288f, 97f)), Color(0xFFFBCA3F), 1f),
                Triple(listOf(Offset(322f, 66f), Offset(342f, 66f), Offset(332f, 90f)), Color(0xFF5CC9C0), 1f),
                Triple(listOf(Offset(366f, 56f), Offset(386f, 56f), Offset(376f, 80f)), Color(0xFF865DEC), 1f),
            )

            flags.forEach { (pts, color, _) ->
                val p = Path().apply {
                    moveTo(pts[0].x * scaleX, pts[0].y * scaleY)
                    lineTo(pts[1].x * scaleX, pts[1].y * scaleY)
                    lineTo(pts[2].x * scaleX, pts[2].y * scaleY)
                    close()
                }
                drawPath(p, color = color)
            }

            // Hollow circles
            drawCircle(
                color = Color(0xFFF2C4DF),
                radius = 30f * density,
                center = Offset(52f * scaleX, 200f * scaleY),
                style = Stroke(width = 1.5f * density),
            )
            drawCircle(
                color = Color(0xFFCFE48A),
                radius = 24f * density,
                center = Offset(344f * scaleX, 250f * scaleY),
                style = Stroke(width = 2f * density),
            )

            // Diamond at (330, 150)
            val diamond = Path().apply {
                moveTo(330f * scaleX, 150f * scaleY)
                lineTo(338f * scaleX, 164f * scaleY)
                lineTo(330f * scaleX, 178f * scaleY)
                lineTo(322f * scaleX, 164f * scaleY)
                close()
            }
            drawPath(diamond, color = Color(0xFF9ED7DC), style = Stroke(width = 1.5f * density))

            // Sparkle 4-point at (70, 290)
            val spk1 = Path().apply {
                moveTo(70f * scaleX, 282f * scaleY)
                lineTo(73f * scaleX, 290f * scaleY)
                lineTo(81f * scaleX, 293f * scaleY)
                lineTo(73f * scaleX, 296f * scaleY)
                lineTo(70f * scaleX, 304f * scaleY)
                lineTo(67f * scaleX, 296f * scaleY)
                lineTo(59f * scaleX, 293f * scaleY)
                lineTo(67f * scaleX, 290f * scaleY)
                close()
            }
            drawPath(spk1, color = Color(0xFFF7C9DF))

            // Solid dots
            drawCircle(color = Color(0xFFB9D84A), radius = 5f * density, center = Offset(140f * scaleX, 150f * scaleY))
            drawCircle(color = Color(0xFFFBCA3F), radius = 3.5f * density, center = Offset(150f * scaleX, 220f * scaleY))

            // Star 5-point at (246, 136)
            drawCircle(color = Color(0xFFFBCA3F), radius = 6f * density, center = Offset(246f * scaleX, 136f * scaleY))

            // Bottom sparkles
            drawCircle(color = Color(0xFFFBCA3F).copy(alpha = 0.85f), radius = 5f * density, center = Offset(360f * scaleX, 700f * scaleY))
            drawCircle(color = Color(0xFFB9D84A), radius = 4f * density, center = Offset(30f * scaleX, 740f * scaleY))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp, bottom = 148.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header at top 130dp
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(14.dp, CircleShape, spotColor = Color(0xCC50BEC8), ambientColor = Color(0x4050BEC8))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF5CC9C0), Color(0xFF5AB7E0)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        val p = Path().apply {
                            moveTo(5f * density, 12.5f * density)
                            lineTo(9.5f * density, 17f * density)
                            lineTo(19f * density, 7.5f * density)
                        }
                        drawPath(p, color = Color.White, style = Stroke(width = 3.5f * density, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }

                BasicText(
                    text = "¡Evento confirmado!",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFFEB4F9E), Color(0xFFF2894A), Color(0xFFE3B23C), Color(0xFF9CC04B))),
                    ),
                )

                BasicText("Todo listo para ${formatReadableSpanishDateText(event.dateLabel)} · ${event.venue}.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ticket card at top 330dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
            ) {
                // Main white card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0x50C850AA),
                            ambientColor = Color(0x20C850AA),
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(RfThemeState.currentPalette.surface),
                ) {
                    Column {
                        // Card header
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Canvas(modifier = Modifier.size(26.dp)) {
                                    val s = size.width / 24f
                                    val horn = Path().apply {
                                        moveTo(4f * s, 20f * s)
                                        lineTo(8.5f * s, 8f * s)
                                        lineTo(16f * s, 15.5f * s)
                                        close()
                                    }
                                    drawPath(horn, color = Color.White, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    val ribbons = Path().apply {
                                        moveTo(14f * s, 4f * s)
                                        quadraticTo(14f * s, 6f * s, 13f * s, 7.5f * s)
                                        moveTo(17f * s, 7.5f * s)
                                        quadraticTo(18.5f * s, 6.5f * s, 20.5f * s, 7.5f * s)
                                        moveTo(20f * s, 12f * s)
                                        quadraticTo(18.5f * s, 11.5f * s, 16.8f * s, 13f * s)
                                    }
                                    drawPath(ribbons, color = Color.White, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    drawCircle(color = Color.White, radius = 0.9f * s, center = Offset(10f * s, 4.5f * s))
                                    drawCircle(color = Color.White, radius = 0.9f * s, center = Offset(20f * s, 4f * s))
                                    drawCircle(color = Color.White, radius = 0.9f * s, center = Offset(21f * s, 16f * s))
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                BasicText(event.title, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 22.sp, color = RfThemeState.currentPalette.ink))
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF6D6))
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFA9CC3A)))
                                        BasicText("Confirmado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E5A12)))
                                    }
                                }
                            }
                        }

                        // Dashed line
                        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 18.dp)) {
                            drawLine(
                                color = Color(0xFFEEE8F2),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 2f * density,
                            )
                        }

                        Column(
                            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(28.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Comprobante", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                BasicText(receiptCode, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF2C9))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(formatReadableSpanishDateText(event.dateLabel), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF7A5700)))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(28.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Método", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                BasicText(
                                    text = paymentMethodLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(28.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                BasicText("${event.articleCount}", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Monto pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                                BasicText(
                                    text = RosaFiestaEngine.formatDop(paidAmountDop),
                                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, color = Color(0xFF3FB59A)),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = (-12).dp, y = 80.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.bg),
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .offset(x = 12.dp, y = 80.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.bg),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
                .background(RfThemeState.currentPalette.bg)
                .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (pendingCardSession != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF5D3FD3))
                        .clickable(enabled = !isSubmittingCardConfirm) {
                            viewModel.confirmPendingCardCheckout(
                                sessionId = pendingCardSession.sessionId,
                                onConfirmed = { cardVerifyError = null },
                                onError = { errorText -> cardVerifyError = errorText },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (isSubmittingCardConfirm) "Verificando pago en Stripe..." else "Verificar pago con tarjeta",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                    )
                }
                if (!cardVerifyError.isNullOrBlank()) {
                    BasicText(
                        text = cardVerifyError.orEmpty(),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFFB42345)),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s83") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Ver recibo oficial",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECE6F0), RoundedCornerShape(16.dp))
                        .clickable {
                            val exported = viewModel?.exportReceiptDocument(
                                event = event,
                                payment = latestPayment,
                                shareInsteadOfSave = true,
                            )
                            val canConfirm = if (viewModel == null) true else !exported.isNullOrBlank()
                            if (canConfirm) {
                                shareConfirmed = true
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(
                            kind = if (shareConfirmed) RfIconKind.CHECK else RfIconKind.SHARE,
                            tint = if (shareConfirmed) Color(0xFF4E6A10) else RfThemeState.currentPalette.ink,
                            size = 17.dp,
                        )
                        BasicText(
                            text = if (shareConfirmed) "Compartido" else "Compartir comprobante",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (shareConfirmed) Color(0xFF4E6A10) else RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { onSelectScreenCode("s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Mis eventos",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderPostEventReviewScreenS52(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val event = if (viewModel != null) {
        viewModel.activeClientEventOrNull()
    } else {
        events.firstOrNull { currentEvent -> currentEvent.id == selectedId }
            ?: events.firstOrNull()
            ?: CanonicalSampleData.events.first()
    }
    if (event == null) {
        PersonalEmptyEventStateScaffold(
            headerTitle = "Reseña del evento",
            backTargetCode = "BACK:s37",
            message = "Cuando tengas un evento finalizado podrás evaluar la experiencia aquí.",
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    val damageIncidents = viewModel?.damageIncidents?.collectAsState()?.value ?: CanonicalSampleData.damageIncidents
    val tasks = viewModel?.eventTasksList?.collectAsState()?.value ?: emptyList()
    val timeline = viewModel?.eventTimelineItems?.collectAsState()?.value ?: emptyList()

    val completedTaskCount = tasks.count { taskItem -> taskItem.completed }
    val taskSummaryText = if (tasks.isEmpty()) "Sin tareas" else "$completedTaskCount de ${tasks.size} listas"
    val timelineSummaryText = if (timeline.isEmpty()) "Sin actividades" else "${timeline.size} actividades"

    val eventDamages = damageIncidents.filter { incident -> incident.eventId == event.id }
    val damageChargeDop = eventDamages.sumOf { incident -> incident.replacementDop }
    val estimatedBudgetDop = event.totalDop
    val realBudgetDop = estimatedBudgetDop + damageChargeDop

    var selectedRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var hasInteractedReviewComment by remember { mutableStateOf(false) }
    var forceShowReviewError by remember { mutableStateOf(false) }
    var localReviewError by remember { mutableStateOf<String?>(null) }
    val debouncedReviewCommentError = rememberDebouncedValidationError(
        value = reviewComment,
        hasInteracted = hasInteractedReviewComment,
        forceShowError = forceShowReviewError,
    ) { candidate ->
        InputValidator.validateNotes(
            rawValue = candidate,
            fieldLabel = "La reseña",
            isRequired = true,
            minLength = 6,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(palette.radialPrimary, palette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(palette.radialSecondary, palette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable { onSelectScreenCode("BACK:s40") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Así salió tu evento",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = palette.ink),
                        )
                        BasicText(
                            text = "${event.title} · ${formatReadableSpanishDateText(event.dateLabel)}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEFE8FD))
                        .padding(20.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Canvas(modifier = Modifier.size(92.dp)) {
                            val strokeW = 10f * density
                            drawCircle(color = Color.White, radius = 36f * density, center = Offset(46f * density, 46f * density), style = Stroke(width = strokeW))
                            drawCircle(color = Color(0xFF8DB52C), radius = 36f * density, center = Offset(46f * density, 46f * density), style = Stroke(width = strokeW, cap = StrokeCap.Round))
                            val checkPath = Path().apply {
                                moveTo(34f * density, 47f * density)
                                lineTo(42f * density, 55f * density)
                                lineTo(58f * density, 38f * density)
                            }
                            drawPath(checkPath, color = Color(0xFF4E6A10), style = Stroke(width = 4.5f * density, cap = StrokeCap.Round))
                        }

                        Column {
                            BasicText("Puntualidad", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7A4FD8)))
                            BasicText("100%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 30.sp, color = palette.ink))
                            BasicText("Todo llegó a tiempo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        BasicText("Liquidación final", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink))
                        val maxBarDop = maxOf(estimatedBudgetDop, realBudgetDop, 1)
                        val contractedFraction = (estimatedBudgetDop.toFloat() / maxBarDop.toFloat()).coerceIn(0.08f, 1f)
                        val realFraction = (realBudgetDop.toFloat() / maxBarDop.toFloat()).coerceIn(0.08f, 1f)

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BasicText("Contratado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                                BasicText(RosaFiestaEngine.formatDop(estimatedBudgetDop), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink))
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFF3EFF6))) {
                                Box(modifier = Modifier.fillMaxWidth(contractedFraction).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFBFB0D8)))
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BasicText("Total cierre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                                BasicText(RosaFiestaEngine.formatDop(realBudgetDop), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink))
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFF3EFF6))) {
                                Box(modifier = Modifier.fillMaxWidth(realFraction).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF8DB52C)))
                            }
                        }

                        val hasDamageCharges = damageChargeDop > 0
                        val damageCardBg = if (hasDamageCharges) Color(0xFFFFF2C9) else Color(0xFFEEF6D6)
                        val damageCardFg = if (hasDamageCharges) Color(0xFF7A5700) else Color(0xFF4E6A10)
                        val damageSummaryLabel = if (hasDamageCharges) {
                            "Cargos por daños (${eventDamages.size} incidencia)"
                        } else {
                            "Sin cargos por daños"
                        }
                        val damageAmountLabel = if (hasDamageCharges) {
                            RosaFiestaEngine.formatDop(damageChargeDop)
                        } else {
                            "RD$ 0"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(damageCardBg)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText(
                                    text = damageSummaryLabel,
                                    modifier = Modifier.weight(1f),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = damageCardFg),
                                )
                                BasicText(
                                    text = damageAmountLabel,
                                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = damageCardFg),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                        .padding(vertical = 4.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp).clickable { onSelectScreenCode("s45") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Tareas completadas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = palette.ink))
                            BasicText(taskSummaryText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp).clickable { onSelectScreenCode("s44") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Cronograma ejecutado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = palette.ink))
                            BasicText(timelineSummaryText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = "Califica tu experiencia",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink),
                        )
                        BasicText(
                            text = "$selectedRating de 5",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC2338F)),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (starValue in 1..5) {
                            val isStarFilled = starValue <= selectedRating
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isStarFilled) Color(0xFFFFF2C9) else palette.chipBg)
                                    .clickable {
                                        selectedRating = starValue
                                        localReviewError = null
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(
                                    kind = RfIconKind.STAR,
                                    tint = if (isStarFilled) Color(0xFFE5A000) else palette.muted,
                                    size = 22.dp,
                                )
                            }
                        }
                    }

                    val activeReviewCommentError = localReviewError ?: debouncedReviewCommentError
                    val commentBorderColor = if (activeReviewCommentError != null) RfColors.Error else palette.line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.bg)
                            .border(1.dp, commentBorderColor, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                    ) {
                        if (reviewComment.isEmpty()) {
                            BasicText(
                                text = "Escribe tu reseña sobre el montaje y la atención (mínimo 6 caracteres)…",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted),
                            )
                        }
                        BasicTextField(
                            value = reviewComment,
                            onValueChange = { nextText ->
                                reviewComment = nextText.take(InputValidator.MAX_NOTES_LENGTH)
                                hasInteractedReviewComment = true
                                localReviewError = null
                            },
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.ink),
                            cursorBrush = SolidColor(Color(0xFFC2338F)),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    RfValidationErrorText(errorMessage = activeReviewCommentError)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(palette.line),
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable(enabled = !isSubmitting) {
                            val commentValidationError = InputValidator.validateNotes(
                                rawValue = reviewComment,
                                fieldLabel = "La reseña",
                                isRequired = true,
                                minLength = 6,
                                maxLength = InputValidator.MAX_NOTES_LENGTH,
                            )
                            if (commentValidationError != null) {
                                forceShowReviewError = true
                                localReviewError = commentValidationError
                                viewModel?.soundPlayer?.playAlertError()
                                return@clickable
                            }
                            val cleanComment = reviewComment.trim()
                            val activeViewModel = viewModel
                            val published = if (activeViewModel != null) {
                                activeViewModel.submitCompanyReview(cleanComment, selectedRating)
                            } else {
                                true
                            }
                            if (published) {
                                localReviewError = null
                                onSelectScreenCode("s37")
                            } else {
                                localReviewError = activeViewModel?.formError?.value
                                    ?: "No se pudo publicar la reseña."
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.STAR, tint = Color.White, size = 20.dp)
                        BasicText(
                            text = "Publicar reseña ($selectedRating ★)",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

private fun isPastReservationStatus(status: EventStatus): Boolean = when (status) {
    EventStatus.COMPLETED -> true
    EventStatus.CANCELLED -> true
    EventStatus.PENDING_QUOTE -> false
    EventStatus.CONFIRMED -> false
    EventStatus.IN_SETUP -> false
}

@Composable
internal fun RenderMyReservationsScreenS53(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    var selectedTab by remember { mutableStateOf("Activas") }
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    viewModel?.currentAuthenticatedUser?.collectAsState()?.value
    viewModel?.profileName?.collectAsState()?.value
    viewModel?.profileEmail?.collectAsState()?.value
    viewModel?.isExplicitGuestSession?.collectAsState()?.value

    val userEvents = viewModel?.clientEventsForCurrentUser() ?: eventsState
    val filteredEvents = when (selectedTab) {
        "Activas" -> userEvents.filter { eventItem -> !isPastReservationStatus(eventItem.status) }
        "Pasadas" -> userEvents.filter { eventItem -> isPastReservationStatus(eventItem.status) }
        else -> userEvents
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (includeHeader) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable { onSelectScreenCode("BACK:s54") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                    }
                    BasicText(
                        text = "Mis reservas",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = palette.ink),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.chipBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf("Activas", "Pasadas", "Todas").forEach { tabLabel ->
                    val selected = tabLabel == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .then(if (selected) Modifier.rfCardShadow(RoundedCornerShape(12.dp), isAdmin = false) else Modifier)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) palette.surface else Color.Transparent)
                            .clickable { selectedTab = tabLabel },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = tabLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (selected) palette.ink else palette.muted,
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (filteredEvents.isEmpty()) {
                    val emptyHeadline = when (selectedTab) {
                        "Activas" -> "No tienes reservas activas"
                        "Pasadas" -> "Aún no tienes reservas pasadas"
                        else -> "Aún no tienes reservas registradas"
                    }
                    val emptySubtext = when (selectedTab) {
                        "Pasadas" -> "Aquí aparecerán tus eventos finalizados o cancelados."
                        else -> "Explora el catálogo o cotiza con Rosa IA para apartar tu próxima celebración."
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFFC2338F), size = 30.dp)
                        BasicText(
                            text = emptyHeadline,
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = palette.ink, textAlign = TextAlign.Center),
                        )
                        BasicText(
                            text = emptySubtext,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted, textAlign = TextAlign.Center),
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RosaButtonGradient)
                                .clickable { onSelectScreenCode("s18") }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Explorar catálogo",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White),
                            )
                        }
                    }
                } else {
                    filteredEvents.forEach { eventItem ->
                        val totalAmount = eventItem.totalDop.coerceAtLeast(1)
                        val paidAmount = eventItem.paidDepositDop.coerceAtLeast(0)
                        val remainingAmount = eventItem.balanceDop
                        val progressFraction = (paidAmount.toFloat() / totalAmount.toFloat()).coerceIn(0.05f, 1.0f)
                        val lowerTitle = eventItem.title.lowercase()
                        val isCorporateEvent = listOf("corp", "gala").any { keyword -> lowerTitle.contains(keyword) }
                        val photoKey = when {
                            lowerTitle.contains("boda") -> RfPhotoKey.BODA
                            lowerTitle.contains("quince") -> RfPhotoKey.QUINCEANERA
                            lowerTitle.contains("baby") -> RfPhotoKey.BABY_SHOWER
                            isCorporateEvent -> RfPhotoKey.CORPORATIVO
                            else -> RfPhotoKey.CUMPLE_INFANTIL
                        }
                        val tagBg = when (eventItem.status) {
                            EventStatus.CONFIRMED -> Color(0xFFEEF6D6)
                            EventStatus.COMPLETED -> Color(0xFFEFE8FD)
                            EventStatus.CANCELLED -> Color(0xFFFFEDF1)
                            EventStatus.PENDING_QUOTE -> Color(0xFFFFF2C9)
                            EventStatus.IN_SETUP -> Color(0xFFDDF4F3)
                        }
                        val tagTextColor = when (eventItem.status) {
                            EventStatus.CONFIRMED -> Color(0xFF4E6A10)
                            EventStatus.COMPLETED -> Color(0xFF7A4FD8)
                            EventStatus.CANCELLED -> Color(0xFFB42345)
                            EventStatus.PENDING_QUOTE -> Color(0xFF7A5700)
                            EventStatus.IN_SETUP -> Color(0xFF1F8A84)
                        }
                        ReservationCard53(
                            title = eventItem.title,
                            meta = "${formatReadableSpanishDateText(eventItem.dateLabel)} · ${eventItem.venue}",
                            tag = eventItem.statusLabel,
                            tagBackground = tagBg,
                            tagColor = tagTextColor,
                            photoKey = photoKey,
                            paid = RosaFiestaEngine.formatDop(paidAmount),
                            total = RosaFiestaEngine.formatDop(eventItem.totalDop),
                            remaining = if (remainingAmount > 0) "Resta ${RosaFiestaEngine.formatDop(remainingAmount)}" else "Saldo al día",
                            progress = progressFraction,
                            onClick = {
                                viewModel?.selectEvent(eventItem.id)
                                onSelectScreenCode("s40")
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(RfDimens.BottomBarContentClearance))
        }
    }
}

@Composable
private fun ReservationCard53(
    title: String,
    meta: String,
    tag: String,
    tagBackground: Color,
    tagColor: Color,
    photoKey: RfPhotoKey,
    paid: String,
    total: String,
    remaining: String,
    progress: Float,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            RfPhoto(photoKey = photoKey, contentDescription = title, modifier = Modifier.size(52.dp), cornerRadius = 14.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                BasicText(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                )
                BasicText(
                    meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                )
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(tagBackground)
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(tag, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = tagColor))
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(RfThemeState.currentPalette.bg)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.muted)) { append("Pagado ") }
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.ink, fontWeight = FontWeight.Bold)) { append(paid) }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
                )
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.muted)) { append("de ") }
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.ink, fontWeight = FontWeight.Bold)) { append(total) }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFEADFE7))) {
                Box(modifier = Modifier.fillMaxWidth(progress).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF8DB52C)))
            }
            BasicText(remaining, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
        }
    }
}

@Composable
internal fun RenderUserProfileScreenS54(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val profileName = viewModel?.profileName?.collectAsState()?.value ?: "Invitado Rosa Fiesta"
    val profileEmail = viewModel?.profileEmail?.collectAsState()?.value ?: "cliente@rosafiesta.do"
    val profilePhone = viewModel?.profilePhone?.collectAsState()?.value ?: "809-555-0142"
    val profileUsername = viewModel?.profileUsername?.collectAsState()?.value ?: profileEmail.substringBefore("@")
    val profileBirthDate = viewModel?.profileBirthDate?.collectAsState()?.value ?: ""
    val profileAvatarKey = viewModel?.profileAvatarKey?.collectAsState()?.value
    val profileAvatarBytes = viewModel?.profileAvatarBytes?.collectAsState()?.value
    val formError = viewModel?.formError?.collectAsState()?.value
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value
    val hasValidSession = viewModel?.hasValidAuthenticatedSession() == true
    val isStaffOrAdmin = viewModel?.isCurrentSessionAdminOrStaff() == true
    val isAdminProfileMode = if (hasValidSession) isStaffOrAdmin else false
    val isAdminVisual = if (palette.isAdmin) true else isAdminProfileMode
    val profileAccentColor = if (isAdminVisual) RfColors.IndigoTintInk else Color(0xFFC2338F)
    val profileActiveChipBg = if (isAdminVisual) RfColors.IndigoTint else Color(0xFFFDE6F2)
    val profilePrimaryGradient = if (isAdminVisual) AdminProfileButtonGradient else RosaButtonGradient
    val profileBackTarget = if (isAdminVisual) "BACK:A01" else "BACK:s14"

    LaunchedEffect(formFeedback) {
        if (!formFeedback.isNullOrBlank()) {
            delay(3000L)
            viewModel.clearFormFeedback()
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var draftName by remember(profileName) { mutableStateOf(profileName) }
    var draftEmail by remember(profileEmail) { mutableStateOf(profileEmail) }
    var draftPhone by remember(profilePhone) { mutableStateOf(InputValidator.formatPhoneDisplay(profilePhone)) }
    var draftBirthDate by remember(profileBirthDate) { mutableStateOf(profileBirthDate) }
    var draftUsername by remember(profileUsername, profileEmail) {
        mutableStateOf(profileUsername.ifBlank { profileEmail.substringBefore("@") })
    }
    var draftAvatarKey by remember(profileAvatarKey) { mutableStateOf(profileAvatarKey) }
    var showBirthDateCalendar by remember { mutableStateOf(false) }
    var showPhotoPickerModal by remember { mutableStateOf(false) }
    var showLogoutConfirmModal by remember { mutableStateOf(false) }
    var localProfileError by remember { mutableStateOf<String?>(null) }

    var hasInteractedName by remember { mutableStateOf(false) }
    var hasInteractedEmail by remember { mutableStateOf(false) }
    var hasInteractedPhone by remember { mutableStateOf(false) }
    var hasInteractedBirthDate by remember { mutableStateOf(false) }
    var hasInteractedUsername by remember { mutableStateOf(false) }
    var forceShowProfileErrors by remember { mutableStateOf(false) }
    val nameBlurState = rememberFieldBlurState()
    val emailBlurState = rememberFieldBlurState()
    val phoneBlurState = rememberFieldBlurState()
    val birthDateBlurState = rememberFieldBlurState()
    val usernameBlurState = rememberFieldBlurState()

    val liveNameError = rememberDebouncedValidationError(
        value = draftName,
        hasInteracted = hasInteractedName,
        forceShowError = forceShowProfileErrors,
        hasBlurred = nameBlurState.hasBlurred,
    ) { candidate ->
        if (!isEditing) null else InputValidator.validateFullName(candidate)
    }
    val liveEmailError = rememberDebouncedValidationError(
        value = draftEmail,
        hasInteracted = hasInteractedEmail,
        forceShowError = forceShowProfileErrors,
        hasBlurred = emailBlurState.hasBlurred,
    ) { candidate ->
        if (!isEditing) null else InputValidator.validateEmail(candidate)
    }
    val livePhoneError = rememberDebouncedValidationError(
        value = draftPhone,
        hasInteracted = hasInteractedPhone,
        forceShowError = forceShowProfileErrors,
        hasBlurred = phoneBlurState.hasBlurred,
    ) { candidate ->
        if (!isEditing) {
            null
        } else {
            InputValidator.validateDominicanPhone(
                rawValue = candidate,
                isRequired = true,
                allowInternational = false,
                fieldLabel = "El teléfono",
            )
        }
    }
    val liveBirthDateError = rememberDebouncedValidationError(
        value = draftBirthDate,
        hasInteracted = hasInteractedBirthDate,
        forceShowError = forceShowProfileErrors,
        hasBlurred = birthDateBlurState.hasBlurred,
    ) { candidate ->
        if (!isEditing) null else InputValidator.validateBirthDate(candidate, isRequired = true)
    }
    val liveUsernameError = rememberDebouncedValidationError(
        value = draftUsername,
        hasInteracted = hasInteractedUsername,
        forceShowError = forceShowProfileErrors,
        hasBlurred = usernameBlurState.hasBlurred,
    ) { candidate ->
        if (!isEditing) null else InputValidator.validateUsername(candidate)
    }

    val resetProfileEditInteractions = {
        hasInteractedName = false
        hasInteractedEmail = false
        hasInteractedPhone = false
        hasInteractedBirthDate = false
        hasInteractedUsername = false
        forceShowProfileErrors = false
        nameBlurState.reset()
        emailBlurState.reset()
        phoneBlurState.reset()
        birthDateBlurState.reset()
        usernameBlurState.reset()
    }

    val saveProfileChanges: () -> Unit = {
        val syncNameError = InputValidator.validateFullName(draftName)
        val syncEmailError = InputValidator.validateEmail(draftEmail)
        val syncPhoneError = InputValidator.validateDominicanPhone(
            rawValue = draftPhone,
            isRequired = true,
            allowInternational = false,
            fieldLabel = "El teléfono",
        )
        val syncBirthDateError = InputValidator.validateBirthDate(draftBirthDate, isRequired = true)
        val syncUsernameError = InputValidator.validateUsername(draftUsername)
        val firstValidationError = syncNameError
            ?: syncEmailError
            ?: syncPhoneError
            ?: syncBirthDateError
            ?: syncUsernameError
        if (firstValidationError != null) {
            forceShowProfileErrors = true
            localProfileError = firstValidationError
            viewModel?.soundPlayer?.playAlertError()
        } else {
            val activeViewModel = viewModel
            val savedOk = activeViewModel?.saveProfile(
                draftName = draftName,
                draftEmail = draftEmail,
                draftPhone = draftPhone,
                draftBirthDate = draftBirthDate,
                draftUsername = draftUsername,
                draftAvatarKey = draftAvatarKey,
            ) ?: true
            if (savedOk) {
                resetProfileEditInteractions()
                localProfileError = null
                isEditing = false
            } else {
                localProfileError = activeViewModel.formError.value
            }
        }
    }

    val displayedNameForInitials = if (isEditing) draftName else profileName
    val initials = InputValidator.extractInitials(displayedNameForInitials)

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = isAdminVisual)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable {
                            viewModel?.clearFormError()
                            viewModel?.clearFormFeedback()
                            onSelectScreenCode(profileBackTarget)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                BasicText(
                    text = if (isEditing) "Editar perfil" else "Mi perfil",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = palette.ink),
                )
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = isAdminVisual)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .border(1.dp, palette.line, CircleShape)
                            .clickable {
                                draftName = profileName
                                draftEmail = profileEmail
                                draftPhone = InputValidator.formatPhoneDisplay(profilePhone)
                                draftBirthDate = profileBirthDate
                                draftUsername = profileUsername.ifBlank { profileEmail.substringBefore("@") }
                                draftAvatarKey = profileAvatarKey
                                resetProfileEditInteractions()
                                localProfileError = null
                                viewModel?.clearFormError()
                                isEditing = false
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = RfIconKind.CLOSE,
                            tint = palette.muted,
                            size = 18.dp,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = isAdminVisual)
                        .clip(CircleShape)
                        .background(if (isEditing) profileActiveChipBg else palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable {
                            if (!isEditing) {
                                draftName = profileName
                                draftEmail = profileEmail
                                draftPhone = InputValidator.formatPhoneDisplay(profilePhone)
                                draftBirthDate = profileBirthDate
                                draftUsername = profileUsername.ifBlank { profileEmail.substringBefore("@") }
                                draftAvatarKey = profileAvatarKey
                                resetProfileEditInteractions()
                                localProfileError = null
                                viewModel?.clearFormError()
                                isEditing = true
                            } else {
                                saveProfileChanges()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(
                        kind = if (isEditing) RfIconKind.CHECK else RfIconKind.EDIT,
                        tint = if (isEditing) profileAccentColor else palette.ink,
                        size = 20.dp,
                    )
                }
            }

            val activeErrorBanner = localProfileError ?: formError
            if (!activeErrorBanner.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEDF1))
                        .border(1.dp, RfColors.Error.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = activeErrorBanner,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = RfColors.Error,
                        ),
                    )
                }
            } else {
                val hasSuccessFeedback = !formFeedback.isNullOrBlank()
                val showSuccessBanner = if (hasSuccessFeedback) !isEditing else false
                if (showSuccessBanner) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEEF6D6))
                            .border(1.dp, Color(0xFF4E6A10).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .clickable { viewModel?.clearFormFeedback() }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        BasicText(
                            text = formFeedback.orEmpty(),
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 6.dp),
                verticalAlignment = if (isEditing) Alignment.Top else Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RfEditableProfileAvatar(
                    initials = initials,
                    avatarKey = if (isEditing) draftAvatarKey else profileAvatarKey,
                    customImageBytes = profileAvatarBytes,
                    size = 84.dp,
                    isEditing = true,
                    onClickEditPhoto = { showPhotoPickerModal = true },
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (isEditing) 8.dp else 2.dp),
                ) {
                    if (isEditing) {
                        ProfileHeaderEditableField54(
                            label = "Nombre completo (2–60)",
                            value = draftName,
                            placeholder = "Ej. Camila Rodríguez",
                            errorText = liveNameError,
                            maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                            isTitleStyle = true,
                            onFocusChanged = { focused ->
                                nameBlurState.onFocusStateChanged(focused) { hasInteractedName = true }
                            },
                            onValueChange = { nextName ->
                                draftName = InputValidator.filterPersonNameInput(
                                    rawValue = nextName,
                                    maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                                )
                                hasInteractedName = true
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                        ProfileHeaderEditableField54(
                            label = "Correo electrónico",
                            value = draftEmail,
                            placeholder = "usuario@dominio.com",
                            errorText = liveEmailError,
                            maxLength = InputValidator.MAX_EMAIL_LENGTH,
                            isTitleStyle = false,
                            onFocusChanged = { focused ->
                                emailBlurState.onFocusStateChanged(focused) { hasInteractedEmail = true }
                            },
                            onValueChange = { nextEmail ->
                                draftEmail = nextEmail.take(InputValidator.MAX_EMAIL_LENGTH)
                                hasInteractedEmail = true
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                    } else {
                        BasicText(
                            profileName,
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 24.sp, color = palette.ink),
                        )
                        BasicText(
                            profileEmail,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted),
                        )
                    }
                }
            }

            ProfileCaption54(if (isEditing) "DATOS (HAZ CLIC EN CADA CAMPO PARA EDITAR)" else "DATOS")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
            ) {
                ProfileRow54("Teléfono", RfIconKind.PHONE) {
                    if (isEditing) {
                        ProfileInlineInputBox54(
                            value = draftPhone,
                            placeholder = "809-555-0102",
                            hasError = livePhoneError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            cursorMask = InputValidator::filterPhoneInputWithCursor,
                            onFocusChanged = { focused ->
                                phoneBlurState.onFocusStateChanged(focused) { hasInteractedPhone = true }
                            },
                            onValueChange = { nextPhone ->
                                draftPhone = nextPhone
                                hasInteractedPhone = true
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                    } else {
                        BasicText(
                            InputValidator.formatPhoneDisplay(profilePhone),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = profileAccentColor),
                        )
                    }
                }
                if (isEditing) {
                    ProfileInlineFieldHelper54(
                        errorText = livePhoneError,
                        hintText = "Formato dominicano: 809/829/849-555-0102",
                    )
                }
                ProfileDivider54()
                ProfileRow54(
                    label = "Cumpleaños",
                    icon = RfIconKind.CAKE,
                    onClick = if (isEditing) {
                        { showBirthDateCalendar = true }
                    } else {
                        null
                    },
                ) {
                    if (isEditing) {
                        RfMaskedDateField(
                            value = draftBirthDate,
                            placeholder = "Fecha de nacimiento",
                            hasError = liveBirthDateError != null,
                            compactInline = true,
                            readableFormatter = ::formatSpanishDateWithWords,
                            onFocusChanged = { focused ->
                                birthDateBlurState.onFocusStateChanged(focused) { hasInteractedBirthDate = true }
                            },
                            modifier = Modifier.width(184.dp),
                            onValueChange = { formattedBirthDate ->
                                draftBirthDate = formattedBirthDate.take(InputValidator.MAX_BIRTH_DATE_LENGTH)
                                hasInteractedBirthDate = true
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                            onOpenCalendar = { showBirthDateCalendar = true },
                        )
                    } else {
                        BasicText(
                            formatSpanishDateWithWords(profileBirthDate),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = profileAccentColor),
                        )
                    }
                }
                if (isEditing) {
                    ProfileInlineFieldHelper54(
                        errorText = liveBirthDateError,
                        hintText = "Escribe o toca el calendario para elegir tu fecha",
                    )
                }
                ProfileDivider54()
                ProfileRow54("Usuario", RfIconKind.USER) {
                    if (isEditing) {
                        ProfileInlineInputBox54(
                            value = draftUsername,
                            placeholder = "usuario",
                            hasError = liveUsernameError != null,
                            onFocusChanged = { focused ->
                                usernameBlurState.onFocusStateChanged(focused) { hasInteractedUsername = true }
                            },
                            onValueChange = { nextUsername ->
                                draftUsername = InputValidator.filterUsernameInput(nextUsername)
                                hasInteractedUsername = true
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                    } else {
                        BasicText(
                            profileUsername.ifBlank { profileEmail.substringBefore("@") },
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.muted),
                        )
                    }
                }
                if (isEditing) {
                    ProfileInlineFieldHelper54(
                        errorText = liveUsernameError,
                        hintText = "3 a 24 caracteres (letras, números, ., -, _)",
                    )
                }
            }

            if (isEditing) {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = isAdminVisual)
                        .clip(RoundedCornerShape(16.dp))
                        .background(profilePrimaryGradient)
                        .clickable { saveProfileChanges() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Guardar cambios",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = Color.White),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            ProfileCaption54("ACCESOS RÁPIDOS")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
            ) {
                if (isAdminProfileMode) {
                    ProfileRow54(
                        label = "Pagos del negocio",
                        icon = RfIconKind.WALLET,
                        onClick = { onSelectScreenCode("A20") },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 18.dp)
                    }
                    ProfileDivider54()
                    ProfileRow54(
                        label = "Ajustes del negocio",
                        icon = RfIconKind.SETTINGS,
                        onClick = { onSelectScreenCode("A31") },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 18.dp)
                    }
                    ProfileDivider54()
                    ProfileRow54(
                        label = "Equipo y permisos",
                        icon = RfIconKind.USER,
                        onClick = { onSelectScreenCode("A28") },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 18.dp)
                    }
                } else {
                    ProfileRow54(
                        label = "Pagos",
                        icon = RfIconKind.WALLET,
                        onClick = { onSelectScreenCode("s82") },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 18.dp)
                    }
                    ProfileDivider54()
                    ProfileRow54(
                        label = "Mis reservas",
                        icon = RfIconKind.CALENDAR,
                        onClick = { onSelectScreenCode("s53") },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 18.dp)
                    }
                    ProfileDivider54()
                    ProfileRow54(
                        label = "Eliminar cuenta",
                        icon = RfIconKind.TRASH,
                        labelColor = Color(0xFFB42345),
                        onClick = { onSelectScreenCode("s88") },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFFB42345), size = 18.dp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
            ) {
                ProfileRow54(
                    label = "Cerrar sesión",
                    icon = RfIconKind.LOGOUT,
                    labelColor = Color(0xFFB42345),
                    onClick = { showLogoutConfirmModal = true },
                ) {}
            }
            Spacer(modifier = Modifier.height(RfDimens.BottomBarContentClearance))
        }

        AnimatedVisibility(
            visible = showLogoutConfirmModal,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(120)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
        ) {
            val logoutScrimInteractionSource = remember { MutableInteractionSource() }
            val logoutCardInteractionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF151729).copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = logoutScrimInteractionSource,
                        indication = null,
                        onClick = { showLogoutConfirmModal = false },
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(20.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = logoutCardInteractionSource,
                            indication = null,
                            onClick = {},
                        )
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    BasicText(
                        text = "¿Cerrar sesión?",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = palette.ink),
                    )
                    BasicText(
                        text = "Cerraremos tu sesión actual en este dispositivo. Tus eventos y cotizaciones seguirán guardados en tu cuenta.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = palette.muted),
                    )
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
                                .clickable { showLogoutConfirmModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Cancelar",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFB42345))
                                .clickable {
                                    showLogoutConfirmModal = false
                                    viewModel?.logoutSession()
                                    val logoutTargetCode = if (isAdminProfileMode) "A30" else "s06"
                                    onSelectScreenCode(logoutTargetCode)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Sí, cerrar sesión",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                            )
                        }
                    }
                }
            }
        }

        if (showBirthDateCalendar) {
            RfInteractiveCalendarModal(
                title = "Cumpleaños",
                subtitle = "Selecciona tu día, mes y año de nacimiento",
                currentDateText = draftBirthDate,
                isAdmin = isAdminVisual,
                dateMode = RfCalendarDateMode.BIRTH_AGE_LIMITED,
                onSelectDate = { selectedDate ->
                    draftBirthDate = selectedDate
                    localProfileError = null
                    viewModel?.clearFormError()
                    showBirthDateCalendar = false
                },
                onDismiss = { showBirthDateCalendar = false },
            )
        }

        if (showPhotoPickerModal) {
            RfProfilePhotoPickerModal(
                initials = initials,
                currentAvatarKey = if (isEditing) draftAvatarKey else profileAvatarKey,
                viewModel = viewModel,
                customImageBytes = profileAvatarBytes,
                onPickDevicePhoto = {
                    viewModel?.pickProfileAvatarFromDevice { pickedFile ->
                        draftAvatarKey = "CUSTOM:${pickedFile.fileName}"
                        showPhotoPickerModal = false
                    }
                },
                onSelectAvatarKey = { chosenKey ->
                    draftAvatarKey = chosenKey
                    viewModel?.setProfileAvatarKey(chosenKey)
                    showPhotoPickerModal = false
                },
                onDismiss = { showPhotoPickerModal = false },
            )
        }
    }
}

@Composable
private fun ProfileCaption54(label: String) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current

    BasicText(
        label,
        modifier = Modifier.padding(horizontal = 20.dp),
        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = palette.muted),
    )
}

@Composable
private fun ProfileDivider54() {
    val palette = LocalRfPalette.current
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.line))
}

@Composable
private fun ProfileRow54(
    label: String,
    icon: RfIconKind,
    labelColor: Color = LocalRfPalette.current.ink,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RfIcon(kind = icon, tint = labelColor, size = 20.dp)
        BasicText(
            label,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = if (labelColor == Color(0xFFB42345)) FontWeight.SemiBold else FontWeight.Normal, color = labelColor),
        )
        trailing()
    }
}

@Composable
private fun ProfileHeaderEditableField54(
    label: String,
    value: String,
    placeholder: String,
    errorText: String?,
    maxLength: Int,
    isTitleStyle: Boolean,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val hasError = errorText != null
    val borderColor = if (hasError) RfColors.Error else profileInputBorderColor(LocalRfPalette.current.isAdmin)
    val bgColor = if (hasError) {
        if (palette.isDark) Color(0xFF3A1D28) else Color(0xFFFFF7F9)
    } else {
        palette.surface
    }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = label,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasError) RfColors.Error else Color(0xFF945AE1),
                ),
            )
            BasicText(
                text = "${value.length}/$maxLength",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 10.5.sp,
                    color = palette.muted,
                ),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(1.4.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = if (isTitleStyle) 15.sp else 13.5.sp,
                            color = palette.muted,
                        ),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = if (isTitleStyle) typo.displayFamily else typo.bodyFamily,
                        fontWeight = if (isTitleStyle) FontWeight.Bold else FontWeight.Medium,
                        fontSize = if (isTitleStyle) 15.5.sp else 13.5.sp,
                        color = palette.ink,
                    ),
                    cursorBrush = SolidColor(profileEditAccentColor(LocalRfPalette.current.isAdmin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfTrackValidationFocus(focusKey = remember { Any() }) { isFocused ->
                            onFocusChanged?.invoke(isFocused)
                        },
                )
            }
            RfIcon(
                kind = RfIconKind.EDIT,
                tint = if (hasError) RfColors.Error else profileEditAccentColor(LocalRfPalette.current.isAdmin),
                size = 14.dp,
            )
        }
        if (errorText != null) {
            BasicText(
                text = errorText,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RfColors.Error,
                ),
            )
        }
    }
}

@Composable
private fun ProfileInlineInputBox54(
    value: String,
    placeholder: String,
    hasError: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    cursorMask: ((previousFormatted: String, newRawText: String, newSelectionEnd: Int) -> MaskedTextCursorResult)? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val borderColor = if (hasError) RfColors.Error else profileInputBorderColor(LocalRfPalette.current.isAdmin)
    val bgColor = if (hasError) {
        if (palette.isDark) Color(0xFF3A1D28) else Color(0xFFFFF5F7)
    } else if (palette.isDark) {
        Color(0xFF26243D)
    } else {
        RfThemeState.currentPalette.bg
    }

    Row(
        modifier = Modifier
            .width(184.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.4.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                BasicText(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 13.5.sp,
                        color = palette.muted,
                    ),
                )
            }
            if (cursorMask != null) {
                RfMaskedDigitBasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    maskTransform = cursorMask,
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    textStyle = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = palette.ink,
                    ),
                    cursorBrush = SolidColor(profileEditAccentColor(LocalRfPalette.current.isAdmin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfTrackValidationFocus(focusKey = remember { Any() }) { isFocused ->
                            onFocusChanged?.invoke(isFocused)
                        },
                )
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    textStyle = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = palette.ink,
                    ),
                    cursorBrush = SolidColor(profileEditAccentColor(LocalRfPalette.current.isAdmin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfTrackValidationFocus(focusKey = remember { Any() }) { isFocused ->
                            onFocusChanged?.invoke(isFocused)
                        },
                )
            }
        }
        RfIcon(
            kind = RfIconKind.EDIT,
            tint = if (hasError) RfColors.Error else profileEditAccentColor(LocalRfPalette.current.isAdmin),
            size = 13.dp,
        )
    }
}

@Composable
private fun ProfileInlineFieldHelper54(
    errorText: String?,
    hintText: String,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val displayColor = if (errorText != null) RfColors.Error else palette.muted
    val displayWeight = if (errorText != null) FontWeight.SemiBold else FontWeight.Normal

    BasicText(
        text = errorText ?: hintText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
        style = TextStyle(
            fontFamily = typo.bodyFamily,
            fontSize = 11.sp,
            fontWeight = displayWeight,
            color = displayColor,
        ),
    )
}

private data class SupplierDisplayItem55(
    val id: String,
    val name: String,
    val subtitle: String,
    val phone: String,
    val statusLabel: String,
) {
    val initial: String
        get() = name.trim().take(1).uppercase().ifBlank { "P" }
}

private fun buildSuppliersDisplayList(
    domainSuppliers: List<SupplierRecord>,
    uiSuppliers: List<SupplierUiModel>,
): List<SupplierDisplayItem55> {
    val mappedDomain = domainSuppliers.map { record ->
        val subtitleText = if (record.contactName.isNotBlank()) {
            "${record.category} · ${record.contactName}"
        } else {
            record.category
        }
        SupplierDisplayItem55(
            id = record.id,
            name = record.name,
            subtitle = subtitleText,
            phone = record.phone,
            statusLabel = record.statusLabel,
        )
    }
    val domainIds = mappedDomain.map { item -> item.id }.toSet()
    val extraUiItems = uiSuppliers
        .filter { uiItem -> !domainIds.contains(uiItem.id) }
        .map { uiItem ->
            SupplierDisplayItem55(
                id = uiItem.id,
                name = uiItem.name,
                subtitle = uiItem.category,
                phone = uiItem.phone,
                statusLabel = uiItem.statusLabel,
            )
        }
    return mappedDomain + extraUiItems
}

@Composable
internal fun RenderSuppliersScreenS55(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }
    val domainSuppliers = viewModel?.suppliers?.collectAsState()?.value ?: emptyList()
    val fallbackUiSuppliers = if (viewModel == null) {
        listOf(
            SupplierUiModel("sup-1", "Flores del Jarabacoa Premium", "Flores y follaje · Doña Carmen Polanco", "+1 809 555 2201", "5.0"),
            SupplierUiModel("sup-2", "Cristalería & Eventos Caribe", "Cristalería · Ing. Marcos Tavares", "+1 809 555 2202", "4.9"),
        )
    } else {
        emptyList()
    }
    val uiSuppliers = viewModel?.suppliersList?.collectAsState()?.value ?: fallbackUiSuppliers
    val suppliers = buildSuppliersDisplayList(domainSuppliers, uiSuppliers)
    val feedbackBanner = viewModel?.formFeedback?.collectAsState()?.value
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    var pendingDeleteSupplier by remember { mutableStateOf<SupplierDisplayItem55?>(null) }

    val palette = RfThemeState.currentPalette
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(palette.bg)
            if (!palette.isDark) {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                        center = Offset(size.width, 0f),
                        radius = size.width * 1.1f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFF1E9FB), Color(0x00F1E9FB)),
                        center = Offset(0f, size.height * 0.12f),
                        radius = size.width * 0.8f,
                    ),
                )
            }
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .semantics {
                            contentDescription = "Volver"
                            role = Role.Button
                        }
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        "Proveedores",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = palette.ink),
                    )
                    BasicText("${suppliers.size} activos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0xB35B6CF0))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .semantics {
                            contentDescription = "Nuevo proveedor"
                            role = Role.Button
                        }
                        .clickable { onSelectScreenCode("s56") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFFFFFFFF), size = 20.dp)
                }
            }

            if (!feedbackBanner.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(rfAdminSuccessPillBg())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = feedbackBanner,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = rfAdminSuccessPillInk()),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.fieldBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = palette.muted, size = 18.dp)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        BasicText("Buscar proveedor", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.muted))
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updatedText -> searchQuery = updatedText.take(InputValidator.MAX_SEARCH_QUERY_LENGTH) },
                        singleLine = true,
                        cursorBrush = SolidColor(palette.primaryAccent),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val trimmedSupplierQuery = searchQuery.trim()
                val filteredSuppliers = suppliers.filter { supplier ->
                    if (trimmedSupplierQuery.isEmpty()) {
                        true
                    } else {
                        val combinedSupplierText = "${supplier.name} ${supplier.subtitle} ${supplier.phone}"
                        combinedSupplierText.contains(trimmedSupplierQuery, ignoreCase = true)
                    }
                }
                val evenInitialBg = rfAdminIndigoPillBg()
                val evenInitialColor = rfAdminIndigoPillInk()
                val oddInitialBg = if (palette.isDark) Color(0xFF14343B) else Color(0xFFDDF4F3)
                val oddInitialColor = if (palette.isDark) Color(0xFF67E8F9) else Color(0xFF1F8A84)
                filteredSuppliers.forEachIndexed { supplierIndex, supplier ->
                    val isEvenSupplier = supplierIndex % 2 == 0
                    val whatsAppGreeting = "Hola ${supplier.name}, te escribimos de RosaFiesta"
                    SupplierCard55(
                        initial = supplier.initial,
                        initialBackground = if (isEvenSupplier) evenInitialBg else oddInitialBg,
                        initialColor = if (isEvenSupplier) evenInitialColor else oddInitialColor,
                        name = supplier.name,
                        contact = supplier.subtitle,
                        phone = InputValidator.formatPhoneDisplay(supplier.phone),
                        onEditClick = {
                            viewModel?.selectSupplierForEdit(supplier.id)
                            onSelectScreenCode("s56")
                        },
                        onDeleteClick = if (viewModel != null) {
                            { pendingDeleteSupplier = supplier }
                        } else {
                            null
                        },
                        onPhoneClick = { viewModel?.dialPhoneNumber(supplier.phone) },
                        onWhatsAppClick = { viewModel?.openWhatsAppChat(supplier.phone, whatsAppGreeting) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        val supplierToDelete = pendingDeleteSupplier
        RfDeleteConfirmationDialog(
            visible = supplierToDelete != null,
            title = "Eliminar proveedor",
            message = if (supplierToDelete != null) {
                "¿Seguro que deseas eliminar a \"${supplierToDelete.name}\" de tus proveedores?"
            } else {
                ""
            },
            errorMessage = formError,
            isSubmitting = isSubmitting,
            onDismiss = { pendingDeleteSupplier = null },
            onConfirm = {
                if (supplierToDelete != null) {
                    viewModel?.removeSupplier(supplierToDelete.id)
                    pendingDeleteSupplier = null
                }
            },
        )
    }
}

@Composable
private fun SupplierCard55(
    initial: String,
    initialBackground: Color,
    initialColor: Color,
    name: String,
    contact: String,
    phone: String,
    onEditClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null,
    onPhoneClick: () -> Unit = {},
    onWhatsAppClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(initialBackground),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(initial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = initialColor))
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp, color = palette.ink),
                )
                BasicText(
                    contact,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .semantics {
                            contentDescription = "Editar proveedor"
                            role = Role.Button
                        }
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MORE_VERT, tint = rfAdminIndigoPillInk(), size = 18.dp)
                }
                if (onDeleteClick != null) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(rfAdminDangerPillBg())
                            .semantics {
                                contentDescription = "Eliminar proveedor"
                                role = Role.Button
                            }
                            .clickable(onClick = onDeleteClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = rfAdminDangerPillInk(), size = 16.dp)
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicText(
                phone,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = palette.ink),
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.chipBg)
                    .semantics {
                        contentDescription = "Llamar proveedor"
                        role = Role.Button
                    }
                    .clickable(onClick = onPhoneClick),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PHONE, tint = palette.ink, size = 18.dp)
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(rfAdminSuccessPillBg())
                    .semantics {
                        contentDescription = "WhatsApp proveedor"
                        role = Role.Button
                    }
                    .clickable(onClick = onWhatsAppClick),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WHATSAPP, tint = rfAdminSuccessPillInk(), size = 18.dp)
            }
        }
    }
}

private val AdminPanelGradient = Brush.linearGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
)

@Composable
private fun AdminPanelIconButton(
    icon: RfIconKind,
    accessibilityLabel: String = "",
    onClick: () -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val resolvedLabel = accessibilityLabel.ifBlank {
        when (icon) {
            RfIconKind.BACK -> "Volver"
            RfIconKind.CLOSE -> "Cerrar"
            RfIconKind.PLUS -> "Agregar"
            else -> "Acción"
        }
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .rfIconButtonShadow(CircleShape, isAdmin = true)
            .clip(CircleShape)
            .background(palette.surface)
            .border(1.dp, palette.line, CircleShape)
            .semantics {
                contentDescription = resolvedLabel
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RfIcon(kind = icon, tint = palette.ink, size = 21.dp)
    }
}

@Composable
private fun AdminPanelCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val palette = RfThemeState.currentPalette
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(22.dp))
            .padding(contentPadding),
        content = content,
    )
}

@Composable
internal fun RenderNewSupplierScreenS56(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val editingId = viewModel?.editingSupplierId?.collectAsState()?.value
    val domainSuppliers = viewModel?.suppliers?.collectAsState()?.value.orEmpty()
    val editingSupplier = remember(editingId, domainSuppliers) {
        if (editingId == null) null else domainSuppliers.firstOrNull { sup -> sup.id == editingId }
    }
    var companyName by remember(editingSupplier) { mutableStateOf(editingSupplier?.name.orEmpty()) }
    var selectedCategory by remember(editingSupplier) { mutableStateOf(editingSupplier?.category?.ifBlank { "Flores y follaje" } ?: "Flores y follaje") }
    var contactPerson by remember(editingSupplier) { mutableStateOf(editingSupplier?.specialty.orEmpty()) }
    var phoneNumber by remember(editingSupplier) { mutableStateOf(editingSupplier?.phone.orEmpty()) }
    var emailAddress by remember(editingSupplier) { mutableStateOf(editingSupplier?.email.orEmpty()) }
    var websiteUrl by remember(editingSupplier) { mutableStateOf(editingSupplier?.website.orEmpty()) }
    var supplierNotes by remember(editingSupplier) { mutableStateOf(editingSupplier?.notes.orEmpty()) }
    var localSupplierError by remember { mutableStateOf<String?>(null) }

    var hasInteractedCompany by remember { mutableStateOf(false) }
    var hasInteractedContact by remember { mutableStateOf(false) }
    var hasInteractedPhone by remember { mutableStateOf(false) }
    var hasInteractedEmail by remember { mutableStateOf(false) }
    var hasInteractedWebsite by remember { mutableStateOf(false) }
    var hasInteractedNotes by remember { mutableStateOf(false) }
    var forceShowSupplierErrors by remember { mutableStateOf(false) }

    val debouncedCompanyError = rememberDebouncedValidationError(
        value = companyName,
        hasInteracted = hasInteractedCompany,
        forceShowError = forceShowSupplierErrors,
    ) { candidate ->
        InputValidator.validateSupplierName(candidate, isRequired = true)
    }
    val debouncedContactError = rememberDebouncedValidationError(
        value = contactPerson,
        hasInteracted = hasInteractedContact,
        forceShowError = forceShowSupplierErrors,
    ) { candidate ->
        InputValidator.validateFullName(candidate)
    }
    val debouncedPhoneError = rememberDebouncedValidationError(
        value = phoneNumber,
        hasInteracted = hasInteractedPhone,
        forceShowError = forceShowSupplierErrors,
    ) { candidate ->
        InputValidator.validateDominicanPhone(
            rawValue = candidate,
            isRequired = true,
            allowInternational = false,
            fieldLabel = "El teléfono",
        )
    }
    val debouncedEmailError = rememberDebouncedValidationError(
        value = emailAddress,
        hasInteracted = hasInteractedEmail,
        forceShowError = forceShowSupplierErrors,
    ) { candidate ->
        InputValidator.validateOptionalEmail(candidate)
    }
    val debouncedWebsiteError = rememberDebouncedValidationError(
        value = websiteUrl,
        hasInteracted = hasInteractedWebsite,
        forceShowError = forceShowSupplierErrors,
    ) { candidate ->
        InputValidator.validateWebsiteUrl(candidate, isRequired = false)
    }
    val debouncedNotesError = rememberDebouncedValidationError(
        value = supplierNotes,
        hasInteracted = hasInteractedNotes,
        forceShowError = forceShowSupplierErrors,
    ) { candidate ->
        InputValidator.validateNotes(
            rawValue = candidate,
            fieldLabel = "Las notas",
            isRequired = false,
            minLength = 0,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }

    val categoryOptions = listOf(
        "Flores y follaje",
        "Mobiliario",
        "Cristalería",
        "Transporte",
        "Pastelería",
        "Audiovisual",
    )

    val palette = RfThemeState.currentPalette
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            RfRadialBackgroundBox(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AdminPanelIconButton(RfIconKind.CLOSE) {
                            viewModel?.selectSupplierForEdit(null)
                            onSelectScreenCode("BACK:s55")
                        }
                        BasicText(
                            if (editingSupplier != null) "Editar proveedor" else "Nuevo proveedor",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = palette.ink),
                        )
                    }

                    if (localSupplierError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(rfAdminDangerPillBg())
                                .border(1.dp, RfColors.Error.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            BasicText(
                                text = localSupplierError.orEmpty(),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = rfAdminDangerPillInk()),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminField56(
                            label = "Nombre de la empresa",
                            placeholder = "Ej. Flores del Valle",
                            icon = RfIconKind.BOX,
                            required = true,
                            boundValue = companyName,
                            isError = debouncedCompanyError != null,
                            errorMessage = debouncedCompanyError,
                            onBoundValueChange = { updatedText ->
                                companyName = updatedText.take(InputValidator.MAX_BUSINESS_NAME_LENGTH)
                                hasInteractedCompany = true
                                localSupplierError = null
                            },
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText(
                                text = "Categoría *",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = palette.ink),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                categoryOptions.forEach { categoryOption ->
                                    val isCategorySelected = selectedCategory == categoryOption
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(if (isCategorySelected) rfAdminIndigoPillBg() else palette.surface)
                                            .border(
                                                width = if (isCategorySelected) 1.5.dp else 1.dp,
                                                color = if (isCategorySelected) palette.primaryAccent else palette.fieldBorder,
                                                shape = RoundedCornerShape(999.dp),
                                            )
                                            .semantics { role = Role.Button }
                                            .clickable {
                                                selectedCategory = categoryOption
                                                localSupplierError = null
                                            }
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = categoryOption,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontWeight = if (isCategorySelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isCategorySelected) rfAdminIndigoPillInk() else palette.muted,
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        AdminField56(
                            label = "Persona de contacto",
                            placeholder = "Nombre y apellido",
                            icon = RfIconKind.USER,
                            required = true,
                            boundValue = contactPerson,
                            isError = debouncedContactError != null,
                            errorMessage = debouncedContactError,
                            onBoundValueChange = { updatedText ->
                                contactPerson = InputValidator.filterPersonNameInput(
                                    rawValue = updatedText,
                                    maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                                )
                                hasInteractedContact = true
                                localSupplierError = null
                            },
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.weight(1f)) {
                                AdminField56(
                                    label = "Teléfono",
                                    placeholder = "809-000-0000",
                                    required = true,
                                    boundValue = phoneNumber,
                                    isError = debouncedPhoneError != null,
                                    errorMessage = debouncedPhoneError,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    cursorMask = InputValidator::filterPhoneInputWithCursor,
                                    onBoundValueChange = { updatedText ->
                                        phoneNumber = updatedText
                                        hasInteractedPhone = true
                                        localSupplierError = null
                                    },
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                AdminField56(
                                    label = "Correo",
                                    placeholder = "correo@empresa.do",
                                    boundValue = emailAddress,
                                    isError = debouncedEmailError != null,
                                    errorMessage = debouncedEmailError,
                                    onBoundValueChange = { updatedText ->
                                        emailAddress = updatedText.take(InputValidator.MAX_EMAIL_LENGTH)
                                        hasInteractedEmail = true
                                        localSupplierError = null
                                    },
                                )
                            }
                        }
                        AdminField56(
                            label = "Sitio web",
                            placeholder = "www.ejemplo.com",
                            icon = RfIconKind.SHARE,
                            boundValue = websiteUrl,
                            isError = debouncedWebsiteError != null,
                            errorMessage = debouncedWebsiteError,
                            onBoundValueChange = { updatedText ->
                                websiteUrl = updatedText.take(InputValidator.MAX_WEBSITE_LENGTH)
                                hasInteractedWebsite = true
                                localSupplierError = null
                            },
                        )
                        AdminNotes56(
                            notesValue = supplierNotes,
                            isError = debouncedNotesError != null,
                            errorMessage = debouncedNotesError,
                            onNotesChange = { updatedNotes ->
                                supplierNotes = updatedNotes.take(InputValidator.MAX_NOTES_LENGTH)
                                hasInteractedNotes = true
                                localSupplierError = null
                            },
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(palette.line),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x995B6CF0))
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminPanelGradient)
                        .clickable {
                            val companyError = InputValidator.validateSupplierName(companyName, isRequired = true)
                            val contactError = InputValidator.validateFullName(contactPerson)
                            val phoneError = InputValidator.validateDominicanPhone(
                                rawValue = phoneNumber,
                                isRequired = true,
                                allowInternational = false,
                                fieldLabel = "El teléfono",
                            )
                            val emailError = InputValidator.validateOptionalEmail(emailAddress)
                            val websiteError = InputValidator.validateWebsiteUrl(websiteUrl, isRequired = false)
                            val notesError = InputValidator.validateNotes(
                                rawValue = supplierNotes,
                                fieldLabel = "Las notas",
                                isRequired = false,
                                minLength = 0,
                                maxLength = InputValidator.MAX_NOTES_LENGTH,
                            )
                            val validationError = companyError
                                ?: contactError
                                ?: phoneError
                                ?: emailError
                                ?: websiteError
                                ?: notesError
                            if (validationError != null) {
                                forceShowSupplierErrors = true
                                localSupplierError = validationError
                                viewModel?.soundPlayer?.playAlertError()
                                return@clickable
                            }

                            val activeViewModel = viewModel
                            val saved = if (activeViewModel == null) {
                                true
                            } else if (editingSupplier != null) {
                                activeViewModel.editSupplier(
                                    supplierId = editingSupplier.id,
                                    name = companyName.trim(),
                                    category = selectedCategory,
                                    phone = phoneNumber.trim(),
                                    contactName = contactPerson.trim(),
                                    email = emailAddress.trim(),
                                    website = websiteUrl.trim(),
                                    notes = supplierNotes.trim(),
                                )
                            } else {
                                activeViewModel.addSupplier(
                                    name = companyName.trim(),
                                    category = selectedCategory,
                                    phone = phoneNumber.trim(),
                                    contactName = contactPerson.trim(),
                                    email = emailAddress.trim(),
                                    website = websiteUrl.trim(),
                                    notes = supplierNotes.trim(),
                                )
                            }
                            if (saved) {
                                localSupplierError = null
                                activeViewModel?.selectSupplierForEdit(null)
                                onSelectScreenCode("BACK:s55")
                            } else {
                                localSupplierError = activeViewModel?.formError?.value
                                    ?: "No se pudo guardar el proveedor."
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar proveedor", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                }
            }
        }
    }
}

@Composable
private fun AdminField56(
    label: String,
    placeholder: String,
    icon: RfIconKind? = null,
    required: Boolean = false,
    boundValue: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    cursorMask: ((previousFormatted: String, newRawText: String, newSelectionEnd: Int) -> MaskedTextCursorResult)? = null,
    onBoundValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val hasFieldError = if (isError) true else errorMessage != null
    val borderColor = if (hasFieldError) RfColors.Error else palette.fieldBorder

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = palette.ink))
            if (required) {
                BasicText(" *", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = rfAdminIndigoPillInk()))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x224650B4))
                .clip(RoundedCornerShape(14.dp))
                .background(palette.surface)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon != null) {
                RfIcon(kind = icon, tint = palette.muted, size = 18.dp)
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (boundValue.isEmpty()) {
                    BasicText(
                        placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.muted),
                    )
                }
                if (cursorMask != null) {
                    RfMaskedDigitBasicTextField(
                        value = boundValue,
                        onValueChange = onBoundValueChange,
                        maskTransform = cursorMask,
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                        cursorBrush = SolidColor(palette.primaryAccent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    BasicTextField(
                        value = boundValue,
                        onValueChange = onBoundValueChange,
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        cursorBrush = SolidColor(palette.primaryAccent),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        RfValidationErrorText(errorMessage = errorMessage)
    }
}

@Composable
private fun AdminNotes56(
    notesValue: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    onNotesChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val hasFieldError = if (isError) true else errorMessage != null
    val borderColor = if (hasFieldError) RfColors.Error else palette.fieldBorder

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText("Notas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = palette.ink))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x224650B4))
                .clip(RoundedCornerShape(14.dp))
                .background(palette.surface)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 14.dp, top = 14.dp),
        ) {
            if (notesValue.isEmpty()) {
                BasicText(
                    "Condiciones, horarios, lo que ofrece…",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.muted),
                )
            }
            BasicTextField(
                value = notesValue,
                onValueChange = onNotesChange,
                cursorBrush = SolidColor(palette.primaryAccent),
                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                modifier = Modifier.fillMaxSize(),
            )
        }
        RfValidationErrorText(errorMessage = errorMessage)
    }
}

private data class ExpenseCategoryStat57(
    val label: String,
    val amountDop: Int,
    val barColor: Color,
)

@Composable
internal fun RenderSpendingAnalyticsScreenS57(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val paymentsState = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val refundsState = viewModel?.refundRequests?.collectAsState()?.value ?: CanonicalSampleData.refundRequests
    val damageIncidentsState = viewModel?.damageIncidents?.collectAsState()?.value ?: CanonicalSampleData.damageIncidents
    val analyticsSummaryState = viewModel?.analyticsSummary?.collectAsState()?.value

    val periodOptions = listOf("3m", "6m", "12m", "Histórico")
    var selectedPeriod by remember { mutableStateOf("3m") }

    LaunchedEffect(viewModel, selectedPeriod) {
        val rangeToken = SpendingAnalyticsCalculator.mapPeriodToAnalyticsRange(selectedPeriod)
        viewModel?.loadAnalytics(rangeToken)
    }

    val periodAnalytics: PeriodSpendingAnalytics = remember(
        selectedPeriod,
        analyticsSummaryState,
        eventsState,
        paymentsState,
        refundsState,
        damageIncidentsState,
    ) {
        if (viewModel != null) {
            viewModel.computePeriodSpendingAnalytics(periodLabel = selectedPeriod)
        } else {
            SpendingAnalyticsCalculator.compute(
                periodLabel = selectedPeriod,
                analyticsSummary = analyticsSummaryState,
                events = eventsState,
                payments = paymentsState,
                refunds = refundsState,
                damageIncidents = damageIncidentsState,
            )
        }
    }

    val totalIncomeDop = periodAnalytics.totalIncomeDop
    val categoryStats = periodAnalytics.expenseCategories.map { category ->
        val barColor = when (category.key) {
            "refunds" -> Color(0xFFD63F98)
            "damage" -> Color(0xFF5B6CF0)
            else -> Color(0xFF1FBDC0)
        }
        ExpenseCategoryStat57(
            label = category.label,
            amountDop = category.amountDop,
            barColor = barColor,
        )
    }
    val totalExpensesDop = periodAnalytics.totalExpensesDop
    val expensesDenominator = totalExpensesDop.coerceAtLeast(1)

    val trendMonths = periodAnalytics.trendMonths
    val trendSeriesValues = periodAnalytics.trendValuesDop
    val trendDeltaPercent = periodAnalytics.trendDeltaPercent
    val trendDeltaLabel = if (trendDeltaPercent >= 0) "+$trendDeltaPercent%" else "$trendDeltaPercent%"

    val confirmedCount = periodAnalytics.confirmedCount
    val completedCount = periodAnalytics.completedCount
    val quoteCount = periodAnalytics.quoteCount
    val otherCount = periodAnalytics.otherCount
    val totalPeriodEvents = confirmedCount + completedCount + quoteCount + otherCount

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminPanelIconButton(RfIconKind.BACK, accessibilityLabel = "Volver") { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Análisis",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = palette.ink),
                )
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(12.dp))
                        .semantics {
                            contentDescription = "Cambiar período de análisis"
                            role = Role.Button
                        }
                        .clickable {
                            val currentIndex = periodOptions.indexOf(selectedPeriod)
                            val nextIndex = (currentIndex + 1) % periodOptions.size
                            selectedPeriod = periodOptions[nextIndex]
                        }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BasicText(selectedPeriod, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = palette.ink))
                    RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = palette.ink, size = 15.dp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                periodOptions.forEach { periodOption ->
                    val isPeriodSelected = periodOption == selectedPeriod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPeriodSelected) rfAdminIndigoPillBg() else palette.surface)
                            .border(
                                width = 1.dp,
                                color = if (isPeriodSelected) palette.primaryAccent else palette.line,
                                shape = RoundedCornerShape(10.dp),
                            )
                            .semantics { role = Role.Button }
                            .clickable { selectedPeriod = periodOption },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = periodOption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (isPeriodSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isPeriodSelected) rfAdminIndigoPillInk() else palette.muted,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(AdminPanelGradient)
                            .padding(16.dp),
                    ) {
                        BasicText("Ingresos ($selectedPeriod)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFFF6EAFB)))
                        Spacer(Modifier.height(6.dp))
                        BasicText(
                            text = RosaFiestaEngine.formatDop(totalIncomeDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, color = Color.White),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                            .padding(16.dp),
                    ) {
                        BasicText("Gastos operativos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted))
                        Spacer(Modifier.height(6.dp))
                        BasicText(
                            text = RosaFiestaEngine.formatDop(totalExpensesDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 26.sp, color = palette.ink),
                        )
                    }
                }

                AdminPanelCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Tendencia de ingresos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (trendDeltaPercent >= 0) rfAdminSuccessPillBg() else rfAdminDangerPillBg())
                                .padding(horizontal = 9.dp, vertical = 3.dp),
                        ) {
                            BasicText(
                                text = trendDeltaLabel,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = if (trendDeltaPercent >= 0) rfAdminSuccessPillInk() else rfAdminDangerPillInk(),
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    IncomeTrend57(monthLabels = trendMonths, seriesValues = trendSeriesValues)
                }

                AdminPanelCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                    BasicText("Gastos por categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink))
                    Spacer(Modifier.height(12.dp))
                    if (categoryStats.isEmpty()) {
                        BasicText(
                            text = "Sin gastos operativos registrados en este período.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            categoryStats.forEach { categoryStat ->
                                val rawFraction = categoryStat.amountDop.toFloat() / expensesDenominator.toFloat()
                                val barFraction = if (categoryStat.amountDop > 0) rawFraction.coerceIn(0.05f, 1f) else 0f
                                val percentValue = (rawFraction * 100f).toInt()
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        BasicText(
                                            text = categoryStat.label,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, color = palette.ink),
                                        )
                                        BasicText(
                                            text = "${RosaFiestaEngine.formatDop(categoryStat.amountDop)} ($percentValue%)",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = palette.muted),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(palette.chipBg),
                                    ) {
                                        if (barFraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(barFraction)
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(categoryStat.barColor),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminPanelCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                    BasicText("Eventos por estado ($totalPeriodEvents)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink))
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusLegend57("Confirmado", "$confirmedCount", Color(0xFF8DB52C))
                            StatusLegend57("Completado", "$completedCount", Color(0xFF865DEC))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusLegend57("En revisión", "$quoteCount", Color(0xFF1FBDC0))
                            StatusLegend57("Otros", "$otherCount", palette.muted)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun IncomeTrend57(
    monthLabels: List<String>,
    seriesValues: List<Int>,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val maxVal = (seriesValues.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
    val areaFillColor = rfAdminIndigoPillBg().copy(alpha = if (palette.isDark) 0.5f else 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            listOf(30f, 75f, 120f).forEach { gridY ->
                drawLine(palette.line, Offset(0f, gridY), Offset(size.width, gridY), 1.dp.toPx())
            }
            if (seriesValues.isNotEmpty()) {
                val count = seriesValues.size
                val paddingX = 12f
                val usableWidth = (size.width - paddingX * 2f).coerceAtLeast(1f)
                val stepX = if (count > 1) usableWidth / (count - 1).toFloat() else 0f
                val points = seriesValues.mapIndexed { index, value ->
                    val pointX = paddingX + stepX * index.toFloat()
                    val ratio = (value.toFloat() / maxVal).coerceIn(0f, 1f)
                    val pointY = 112f - ratio * 84f
                    Offset(pointX, pointY)
                }
                val trendLine = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (idx in 1 until points.size) {
                        val prev = points[idx - 1]
                        val curr = points[idx]
                        val midX = (prev.x + curr.x) / 2f
                        cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }
                }
                val filledArea = Path().apply {
                    addPath(trendLine)
                    lineTo(points.last().x, 128f)
                    lineTo(points.first().x, 128f)
                    close()
                }
                drawPath(filledArea, areaFillColor)
                drawPath(trendLine, Color(0xFFD63F98), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                val lastPoint = points.last()
                drawCircle(palette.surface, radius = 5.dp.toPx(), center = lastPoint)
                drawCircle(Color(0xFFD63F98), radius = 5.dp.toPx(), center = lastPoint, style = Stroke(width = 2.5.dp.toPx()))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
            monthLabels.forEach { monthLabel ->
                BasicText(
                    monthLabel,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = palette.muted, textAlign = TextAlign.Center),
                )
            }
        }
    }
}

@Composable
private fun StatusLegend57(label: String, count: String, color: Color) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        BasicText(label, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.ink))
        BasicText(count, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink))
    }
}

@Composable
internal fun RenderFinancialDashboardScreenS58(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val paymentsState = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val clientsState = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
    val damageIncidents = viewModel?.damageIncidents?.collectAsState()?.value ?: CanonicalSampleData.damageIncidents
    val (currentYear, currentMonth, _) = remember { DomainClockFormatter.currentCivilDateTriple() }
    val monthFullNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
    )
    val dynamicMonthLabel = "${monthFullNames[(currentMonth - 1).coerceIn(0, 11)]} $currentYear"
    val fallbackMetrics = BusinessMetrics(
        monthLabel = dynamicMonthLabel,
        octoberRevenueDop = 12450,
        accountsReceivableDop = 7724,
        activeEventsCount = 4,
        newRequestsCount = 2,
        occupancyRatePercent = 78,
    )
    val metricsState = viewModel?.metrics?.collectAsState()?.value ?: fallbackMetrics

    val collectedDop = paymentsState.sumOf { paymentItem -> paymentItem.amountDop }
    val monthlyRevenueDop = if (paymentsState.isNotEmpty()) collectedDop else metricsState.octoberRevenueDop
    val sparklineValues = paymentsState.takeLast(6).map { paymentItem -> paymentItem.amountDop }
    val activeEventsCount = eventsState.count { eventItem -> eventItem.status != EventStatus.CANCELLED }
    val incidentsCount = damageIncidents.size
    val clientCount = clientsState.size

    val palette = RfThemeState.currentPalette
    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminPanelIconButton(RfIconKind.BACK, accessibilityLabel = "Volver") { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Panel",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = palette.ink),
                )
                BasicText(metricsState.monthLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = palette.muted))
            }
            Spacer(modifier = Modifier.height(12.dp))
            AdminTabs58(activeCode = "s58", onSelectScreenCode = onSelectScreenCode)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x996E82E6))
                        .clip(RoundedCornerShape(24.dp))
                        .background(AdminPanelGradient)
                        .padding(20.dp),
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        if (sparklineValues.isNotEmpty()) {
                            val originX = size.width - 170f
                            val originY = size.height - 110f
                            val maxAmount = (sparklineValues.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
                            val pointCount = sparklineValues.size
                            val stepX = if (pointCount > 1) 190f / (pointCount - 1).toFloat() else 0f
                            val sparkPoints = sparklineValues.mapIndexed { index, amount ->
                                val px = originX + stepX * index.toFloat()
                                val ratio = (amount.toFloat() / maxAmount).coerceIn(0f, 1f)
                                val py = originY + 100f - ratio * 90f
                                Offset(px, py)
                            }
                            val strokePath = Path().apply {
                                moveTo(sparkPoints.first().x, sparkPoints.first().y)
                                for (idx in 1 until sparkPoints.size) {
                                    val prev = sparkPoints[idx - 1]
                                    val curr = sparkPoints[idx]
                                    val midX = (prev.x + curr.x) / 2f
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }
                            val areaPath = Path().apply {
                                addPath(strokePath)
                                lineTo(sparkPoints.last().x, originY + 130f)
                                lineTo(sparkPoints.first().x, originY + 130f)
                                close()
                            }
                            drawPath(areaPath, Color(0x1FFFD45C))
                            drawPath(strokePath, Color(0xFFFFD45C), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.TREND_UP, tint = Color(0xFFF6EAFB), size = 16.dp)
                            BasicText("Ingresos del mes (${paymentsState.size} pagos)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFF6EAFB)))
                        }
                        Spacer(Modifier.height(8.dp))
                        BasicText(
                            text = RosaFiestaEngine.formatDop(monthlyRevenueDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, color = Color.White),
                        )
                        Spacer(Modifier.height(4.dp))
                        BasicText(
                            text = "Por cobrar: ${RosaFiestaEngine.formatDop(metricsState.accountsReceivableDop)} · Ocupación ${metricsState.occupancyRatePercent}%",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFFF6EAFB)),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(22.dp)),
                ) {
                    PanelSummaryRow58(
                        icon = RfIconKind.CALENDAR,
                        iconBackground = rfAdminIndigoPillBg(),
                        iconTint = rfAdminIndigoPillInk(),
                        title = "Eventos activos",
                        value = "$activeEventsCount",
                        divider = false,
                        onLinkClick = { onSelectScreenCode("A11") },
                    )
                    PanelSummaryRow58(
                        icon = RfIconKind.WARNING,
                        iconBackground = rfAdminWarnPillBg(),
                        iconTint = rfAdminWarnPillInk(),
                        title = "Siniestros registrados",
                        link = "Revisar incidencias",
                        onLinkClick = { onSelectScreenCode("s60") },
                        value = "$incidentsCount",
                        divider = true,
                    )
                    PanelSummaryRow58(
                        icon = RfIconKind.USER,
                        iconBackground = rfAdminSuccessPillBg(),
                        iconTint = rfAdminSuccessPillInk(),
                        title = "Cartera de clientes",
                        value = "$clientCount",
                        divider = true,
                        onLinkClick = { onSelectScreenCode("s62") },
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun AdminTabs58(activeCode: String, onSelectScreenCode: (String) -> Unit) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val tabs = listOf("Resumen" to "s58", "Finanzas" to "s59", "Seguros" to "s60", "Auditoría" to "s61", "Clientes" to "s62")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(palette.line, Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            tabs.forEach { tabEntry ->
                val label = tabEntry.first
                val code = tabEntry.second
                val active = code == activeCode
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .semantics { role = Role.Tab }
                        .clickable { if (!active) onSelectScreenCode(code) }
                        .drawBehind {
                            if (active) {
                                val stroke = 2.5.dp.toPx()
                                drawLine(palette.primaryAccent, Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        label,
                        maxLines = 1,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (active) palette.ink else palette.muted,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelSummaryRow58(
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    value: String,
    divider: Boolean,
    link: String? = null,
    onLinkClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(palette.line, Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .heightIn(min = 72.dp)
            .semantics { role = Role.Button }
            .clickable(onClick = onLinkClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 20.dp)
        }
        if (link != null) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = palette.ink))
                BasicText(
                    link,
                    modifier = Modifier.clickable(onClick = onLinkClick),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = rfAdminWarnPillInk()),
                )
            }
        } else {
            BasicText(
                title,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = palette.ink),
            )
        }
        BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = palette.ink))
    }
}

@Composable
internal fun RenderOfficialReceiptScreenS83(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: BusinessSettings()
    val profileName = viewModel?.profileName?.collectAsState()?.value.orEmpty()
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val paymentsState = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val event = if (viewModel != null) {
        viewModel.activeClientEventOrNull()
    } else {
        eventsState.first()
    }
    if (event == null) {
        PersonalEmptyEventStateScaffold(
            headerTitle = "Recibo oficial",
            backTargetCode = "BACK:s37",
            message = "Aún no hay recibos emitidos porque todavía no tienes eventos.",
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }
    val userPayments = viewModel?.clientPaymentsForCurrentUser() ?: paymentsState
    val latestPayment = userPayments.firstOrNull { paymentItem -> paymentItem.eventId == event.id }
        ?: paymentsState.firstOrNull { paymentItem -> paymentItem.eventId == event.id }
        ?: userPayments.firstOrNull()

    val savedMethod = viewModel?.savedPaymentMethod?.collectAsState()?.value ?: event.depositMethodLabel
    val totalEventDop = event.totalDop
    val paidDop = latestPayment?.amountDop ?: event.paidDepositDop
    val remainingDop = (totalEventDop - paidDop).coerceAtLeast(0)
    val receiptCode = latestPayment?.receiptNumber ?: "REC-${event.code}"
    val receiptMethod = latestPayment?.methodLabel ?: savedMethod
    val rawReceiptDate = latestPayment?.dateLabel ?: event.depositDateLabel.ifBlank { event.dateLabel }
    val receiptDate = formatReadableSpanishDateText(rawReceiptDate)
    val billedClientName = latestPayment?.clientName?.ifBlank { event.clientName } ?: profileName.ifBlank { event.clientName }
    val receiptConcept = latestPayment?.kindLabel ?: "Abono · ${event.title}"

    var receiptShared by remember { mutableStateOf(false) }
    var pdfDownloaded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
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
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable { onSelectScreenCode("BACK") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Recibo oficial",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = palette.ink),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                        BasicText(
                            text = "No. $receiptCode",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable {
                                val exported = viewModel?.exportReceiptDocument(
                                    event = event,
                                    payment = latestPayment,
                                    shareInsteadOfSave = true,
                                )
                                val canConfirm = if (viewModel == null) true else !exported.isNullOrBlank()
                                if (canConfirm) {
                                    receiptShared = true
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = if (receiptShared) RfIconKind.CHECK else RfIconKind.SHARE,
                            tint = if (receiptShared) Color(0xFF4E6A10) else palette.ink,
                            size = 19.dp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.surface)
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(elevation = 4.dp, shape = CircleShape, spotColor = Color(0x24B450B4))
                                .clip(CircleShape)
                                .background(palette.surface)
                                .border(1.dp, Color(0x25EA4EA9), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "RF",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFEA4EA9),
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        BasicText(
                            text = businessSettings.businessName,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink, textAlign = TextAlign.Center),
                        )
                        BasicText(
                            text = "RNC ${businessSettings.rnc}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val isRejectedReceipt = latestPayment?.rejectionReason?.isNotBlank() == true
                        val isPendingReceipt = if (isRejectedReceipt) {
                            false
                        } else {
                            latestPayment?.verified == false
                        }
                        val statusBadgeBg = when {
                            isRejectedReceipt -> Color(0xFFFFE5EA)
                            isPendingReceipt -> Color(0xFFFFF2C9)
                            else -> Color(0xFFEEF6D6)
                        }
                        val statusBadgeText = when {
                            isRejectedReceipt -> Color(0xFFB42345)
                            isPendingReceipt -> Color(0xFF7A5700)
                            else -> Color(0xFF4E6A10)
                        }
                        val statusBadgeLabel = when {
                            isRejectedReceipt -> "Comprobante rechazado"
                            isPendingReceipt -> "Pendiente de verificación"
                            else -> "Verificado por finanzas"
                        }
                        val statusBadgeIcon = when {
                            isRejectedReceipt -> RfIconKind.CLOSE
                            isPendingReceipt -> RfIconKind.CLOCK
                            else -> RfIconKind.CHECK
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(statusBadgeBg)
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RfIcon(kind = statusBadgeIcon, tint = statusBadgeText, size = 13.dp)
                                BasicText(
                                    text = statusBadgeLabel,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = statusBadgeText),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        BasicText(
                            text = RosaFiestaEngine.formatDop(paidDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 26.sp, color = palette.ink),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        BasicText(
                            text = "$receiptConcept · ${event.title}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted, textAlign = TextAlign.Center),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Canvas(modifier = Modifier.fillMaxWidth().height(1.5.dp)) {
                            drawLine(
                                color = Color(0xFFE6DEEA),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val receiptSubtotal = latestPayment?.subtotalDop ?: ((paidDop * 100) / 118)
                        val receiptItbis = latestPayment?.itbisDop ?: (paidDop - receiptSubtotal).coerceAtLeast(0)
                        val ncfLabel = latestPayment?.ncfNumber?.ifBlank { latestPayment.ncfType } ?: "B02"
                        val clientRncValue = latestPayment?.clientRnc.orEmpty()
                        val rejectionReasonValue = latestPayment?.rejectionReason.orEmpty()
                        val summaryRows = buildList {
                            add("Fecha de emisión" to receiptDate)
                            add("Método" to receiptMethod)
                            add("Comprobante (NCF)" to ncfLabel)
                            if (clientRncValue.isNotBlank()) {
                                add("RNC / Cédula cliente" to clientRncValue)
                            }
                            add("Subtotal gravado" to RosaFiestaEngine.formatDop(receiptSubtotal))
                            add("ITBIS (18%)" to RosaFiestaEngine.formatDop(receiptItbis))
                            add("Cliente" to billedClientName)
                            add("Evento" to "${formatReadableSpanishDateText(event.dateLabel)} · ${event.venue}")
                        }
                        if (rejectionReasonValue.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFEEF1))
                                    .padding(10.dp),
                            ) {
                                BasicText(
                                    text = "Motivo de rechazo: $rejectionReasonValue",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                                )
                            }
                        }
                        val receiptVoucherUrl = latestPayment?.voucherUrl.orEmpty()
                        if (receiptVoucherUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            RfUploadedOrCatalogPhoto(
                                photoUrl = receiptVoucherUrl,
                                fallbackPhotoKey = RfPhotoKey.BODA,
                                contentDescription = "Comprobante de transferencia",
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxWidth().height(96.dp),
                                cornerRadius = 12.dp,
                            )
                        }

                        summaryRows.forEach { rowEntry ->
                            val rowLabel = rowEntry.first
                            val rowValue = rowEntry.second
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                BasicText(rowLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted))
                                BasicText(
                                    text = rowValue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = palette.ink),
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            }
                        }

                        if (event.items.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
                            Spacer(modifier = Modifier.height(8.dp))
                            BasicText(
                                text = "DETALLE DE RENGLONES",
                                modifier = Modifier.fillMaxWidth(),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, letterSpacing = 0.8.sp, color = palette.muted),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            event.items.forEach { lineItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BasicText(
                                        text = "${lineItem.quantity} × ${lineItem.name}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.ink),
                                    )
                                    BasicText(
                                        text = RosaFiestaEngine.formatDop(lineItem.subtotalDop),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = palette.ink),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Total contratado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                            BasicText(RosaFiestaEngine.formatDop(totalEventDop), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.ink))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Monto abonado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                            BasicText(RosaFiestaEngine.formatDop(paidDop), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4E6A10)))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Saldo pendiente", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = palette.muted))
                            BasicText(
                                text = RosaFiestaEngine.formatDop(remainingDop),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp, color = Color(0xFF7A5700)),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BasicText(
                    text = "${businessSettings.businessName} · RNC ${businessSettings.rnc}\n${businessSettings.address}",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = palette.muted, textAlign = TextAlign.Center, lineHeight = 18.sp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(palette.line),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .rfIconButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                            .clickable {
                                val exported = viewModel?.exportReceiptDocument(
                                    event = event,
                                    payment = latestPayment,
                                    shareInsteadOfSave = true,
                                )
                                val canConfirm = if (viewModel == null) true else !exported.isNullOrBlank()
                                if (canConfirm) {
                                    receiptShared = true
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RfIcon(kind = if (receiptShared) RfIconKind.CHECK else RfIconKind.SHARE, tint = if (receiptShared) Color(0xFF4E6A10) else palette.ink, size = 18.dp)
                            BasicText(
                                text = if (receiptShared) "Compartido" else "Compartir",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = if (receiptShared) Color(0xFF4E6A10) else palette.ink),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(54.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RosaButtonGradient)
                            .clickable {
                                val exported = viewModel?.exportReceiptDocument(
                                    event = event,
                                    payment = latestPayment,
                                    shareInsteadOfSave = false,
                                )
                                val canConfirm = if (viewModel == null) true else !exported.isNullOrBlank()
                                if (canConfirm) {
                                    pdfDownloaded = true
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RfIcon(kind = if (pdfDownloaded) RfIconKind.CHECK else RfIconKind.DOWNLOAD, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = if (pdfDownloaded) "PDF descargado" else "Descargar PDF",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}
