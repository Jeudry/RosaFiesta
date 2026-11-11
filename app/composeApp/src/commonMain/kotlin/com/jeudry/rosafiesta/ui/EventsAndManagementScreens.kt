package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.platform.ExternalUrlBuilder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val EventBackgroundGradient = Brush.radialGradient(
    0.0f to RfThemeState.currentPalette.radialPrimary,
    0.6f to RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f),
    center = Offset(0f, 0f),
    radius = 450f,
)

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

private data class ParsedCalendarDate(
    val year: Int,
    val month: Int,
    val day: Int,
)

private fun photoKeyForAsset(photoAsset: PhotoAsset): RfPhotoKey = when (photoAsset) {
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

private fun spanishMonthLabel(monthNumber: Int): String = when (monthNumber) {
    1 -> "Enero"
    2 -> "Febrero"
    3 -> "Marzo"
    4 -> "Abril"
    5 -> "Mayo"
    6 -> "Junio"
    7 -> "Julio"
    8 -> "Agosto"
    9 -> "Septiembre"
    10 -> "Octubre"
    11 -> "Noviembre"
    else -> "Diciembre"
}

private fun parseEventCalendarDate(rawDateLabel: String, defaultYear: Int = 2026): ParsedCalendarDate? {
    val trimmedText = rawDateLabel.trim()
    if (trimmedText.isEmpty()) {
        return null
    }
    if (trimmedText.contains("/")) {
        val slashParts = trimmedText.split("/")
        val dayPart = slashParts.getOrNull(0)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()
        val monthPart = slashParts.getOrNull(1)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()
        val rawYearPart = slashParts.getOrNull(2)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()
        val resolvedYear = when {
            rawYearPart == null -> defaultYear
            rawYearPart < 100 -> 2000 + rawYearPart
            else -> rawYearPart
        }
        val hasSlashDay = dayPart != null
        val hasSlashMonth = monthPart != null
        val validSlashParts = if (hasSlashDay) hasSlashMonth else false
        if (validSlashParts) {
            val safeMonth = (monthPart ?: 1).coerceIn(1, 12)
            val maxDays = computeDaysInGregorianMonth(resolvedYear, safeMonth)
            val safeDay = (dayPart ?: 1).coerceIn(1, maxDays)
            return ParsedCalendarDate(year = resolvedYear, month = safeMonth, day = safeDay)
        }
    }
    val lowerLabel = trimmedText.lowercase()
    val detectedMonth = when {
        lowerLabel.contains("ene") -> 1
        lowerLabel.contains("feb") -> 2
        lowerLabel.contains("mar") -> 3
        lowerLabel.contains("abr") -> 4
        lowerLabel.contains("may") -> 5
        lowerLabel.contains("jun") -> 6
        lowerLabel.contains("jul") -> 7
        lowerLabel.contains("ago") -> 8
        lowerLabel.contains("sep") -> 9
        lowerLabel.contains("oct") -> 10
        lowerLabel.contains("nov") -> 11
        lowerLabel.contains("dic") -> 12
        else -> null
    } ?: return null
    val numericTokens = Regex("\\d+").findAll(lowerLabel).mapNotNull { matchResult ->
        matchResult.value.toIntOrNull()
    }.toList()
    val explicitYear = numericTokens.firstOrNull { tokenValue -> tokenValue >= 2020 } ?: defaultYear
    val dayCandidate = numericTokens.firstOrNull { tokenValue -> tokenValue in 1..31 } ?: return null
    val maxMonthDays = computeDaysInGregorianMonth(explicitYear, detectedMonth)
    if (dayCandidate > maxMonthDays) {
        return null
    }
    return ParsedCalendarDate(year = explicitYear, month = detectedMonth, day = dayCandidate)
}

private fun buildGoogleCalendarTemplateUrl(
    eventTitle: String,
    venue: String,
    dateSummary: String,
): String {
    val encodedTitle = ExternalUrlBuilder.percentEncode(eventTitle)
    val encodedLocation = ExternalUrlBuilder.percentEncode(venue)
    val encodedDetails = ExternalUrlBuilder.percentEncode("Evento RosaFiesta · $dateSummary")
    return "https://calendar.google.com/calendar/render?action=TEMPLATE&text=$encodedTitle&location=$encodedLocation&details=$encodedDetails"
}

@Composable
internal fun RenderMyEventsScreenS37(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var selectedTab by remember { mutableStateOf(0) }
    val observedEvents = viewModel?.events?.collectAsState()?.value
    val observedUser = viewModel?.currentAuthenticatedUser?.collectAsState()?.value
    val observedGuestSession = viewModel?.isExplicitGuestSession?.collectAsState()?.value
    val observedProfileName = viewModel?.profileName?.collectAsState()?.value

    val userEvents = remember(viewModel, observedEvents, observedUser, observedGuestSession, observedProfileName) {
        if (viewModel != null) {
            viewModel.clientEventsForCurrentUser()
        } else {
            CanonicalSampleData.events.filter { eventItem ->
                eventItem.clientName.equals("Camila Rodríguez", ignoreCase = true)
            }
        }
    }
    val upcomingEvents = remember(userEvents) {
        userEvents.filter { eventItem ->
            val isCompleted = eventItem.status == EventStatus.COMPLETED
            val isCancelled = eventItem.status == EventStatus.CANCELLED
            val isClosed = if (isCompleted) true else isCancelled
            !isClosed
        }
    }
    val pastEvents = remember(userEvents) {
        userEvents.filter { eventItem ->
            val isCompleted = eventItem.status == EventStatus.COMPLETED
            val isCancelled = eventItem.status == EventStatus.CANCELLED
            if (isCompleted) true else isCancelled
        }
    }

    val displayedEvents = when (selectedTab) {
        0 -> upcomingEvents
        1 -> pastEvents
        else -> userEvents
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "Mis eventos",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("s39") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CALENDAR, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfPrimaryButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s38") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val tabLabels = listOf(
                "Próximos · ${upcomingEvents.size}",
                "Pasados · ${pastEvents.size}",
                "Todos · ${userEvents.size}",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.chipBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tabLabels.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(
                                        6.dp,
                                        RoundedCornerShape(12.dp),
                                        spotColor = Color(0x33AA46A0),
                                        ambientColor = Color.Transparent,
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color(0xFF1A1A2E) else Color(0xFF62627A),
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFFC2338F), size = 22.dp)
                        }
                        BasicText(
                            text = "No tienes eventos en esta sección",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "Empieza una propuesta guiada con Rosa IA o registra los datos de tu celebración manualmente.",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = false)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RosaButtonGradient)
                                .clickable { onSelectScreenCode("s26") },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Crear con Rosa IA",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                ),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RfThemeState.currentPalette.chipBg)
                                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                                .clickable { onSelectScreenCode("s38") },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Crear manualmente",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayedEvents.forEach { eventItem ->
                        val photoKey = photoKeyForAsset(eventItem.photo)
                        val (statusBg, statusColor) = when (eventItem.status) {
                            EventStatus.CONFIRMED -> Color(0xFFEEF6D6) to Color(0xFF4E6A10)
                            EventStatus.IN_SETUP -> Color(0xFFDDF4F3) to Color(0xFF1F8A84)
                            EventStatus.PENDING_QUOTE -> RfThemeState.currentPalette.chipBg to Color(0xFF62627A)
                            EventStatus.COMPLETED -> Color(0xFFEFE8FD) to Color(0xFF7A4FD8)
                            EventStatus.CANCELLED -> Color(0xFFFCE8E8) to Color(0xFFB42345)
                        }

                        MyEventRow37(
                            title = eventItem.title,
                            date = eventItem.dateLabel,
                            status = eventItem.statusLabel,
                            statusBg = statusBg,
                            statusColor = statusColor,
                            photoKey = photoKey,
                            showReview = eventItem.status == EventStatus.COMPLETED,
                            onReviewClick = {
                                viewModel?.selectEvent(eventItem.id)
                                onSelectScreenCode("s52")
                            },
                            onClick = {
                                viewModel?.selectEvent(eventItem.id)
                                onSelectScreenCode("s40")
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun MyEventRow37(
    title: String,
    date: String,
    status: String,
    statusBg: Color,
    statusColor: Color,
    photoKey: RfPhotoKey,
    showReview: Boolean = false,
    onReviewClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3EEF6)),
        ) {
            RfPhoto(photoKey = photoKey, contentDescription = title)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            BasicText(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RfThemeState.currentPalette.ink,
                ),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    text = date,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                )
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusBg)
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = status,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = statusColor,
                        ),
                    )
                }
            }
        }

        if (showReview) {
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(999.dp))
                    .clickable(onClick = onReviewClick)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RfIcon(kind = RfIconKind.STAR, tint = RfThemeState.currentPalette.ink, size = 14.dp)
                BasicText(
                    text = "Reseñar",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }
        } else {
            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 20.dp)
        }
    }
}

private data class EventTypeCard(val name: String, val bg: Color, val illus: EventTypeIllus)

private enum class EventTypeIllus {
    NONE,
    CUMPLE,
    BODA,
    QUINCEANERA,
    BABY_SHOWER,
    GRADUACION,
    CORPORATIVO,
}

@Composable
internal fun RenderNewEventScreenS38(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val initialType = viewModel?.rosaEventType?.collectAsState()?.value?.ifBlank { "Boda" } ?: "Boda"
    val persistedTitle = viewModel?.eventTypeInput?.collectAsState()?.value.orEmpty()
    val persistedDate = viewModel?.eventDateInput?.collectAsState()?.value.orEmpty()
    val persistedVenue = viewModel?.eventVenueInput?.collectAsState()?.value.orEmpty()
    val persistedGuestsRaw = viewModel?.eventGuestsInput?.collectAsState()?.value.orEmpty()
    val defaultGuestsCount = viewModel?.rosaGuestsCount?.collectAsState()?.value ?: 80
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    val vmFormError = viewModel?.formError?.collectAsState()?.value

    var currentStep by remember { mutableStateOf(1) }
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var customEventTitle by remember { mutableStateOf(persistedTitle) }
    var dateInput by remember { mutableStateOf(persistedDate) }
    var venueInput by remember { mutableStateOf(persistedVenue) }
    var guestsInput by remember {
        val cleanPersistedDigits = persistedGuestsRaw.filter { charItem -> charItem.isDigit() }
        mutableStateOf(cleanPersistedDigits.ifEmpty { defaultGuestsCount.toString() })
    }
    var showCalendarModal by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val resolvedEventTitle = customEventTitle.trim().ifEmpty { selectedType }
    val activeErrorMessage = localValidationError ?: vmFormError

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            if (currentStep > 1) {
                                localValidationError = null
                                currentStep -= 1
                            } else {
                                onSelectScreenCode("BACK:s37")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
                BasicText(
                    text = "Nuevo evento",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.40).sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                    modifier = Modifier.weight(1f),
                )
                BasicText(
                    text = "$currentStep de 3",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = RfThemeState.currentPalette.muted,
                    ),
                )
            }

            val stepLabels = listOf(
                1 to "Tipo",
                2 to "Fecha y lugar",
                3 to "Invitados",
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    stepLabels.forEach { (stepNumber, _) ->
                        val isCompletedOrActive = stepNumber <= currentStep
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCompletedOrActive) Color(0xFFEA4EA9) else Color(0xFFE6DEEA))
                                .clickable(enabled = stepNumber <= currentStep) {
                                    localValidationError = null
                                    currentStep = stepNumber
                                },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    stepLabels.forEach { (stepNumber, label) ->
                        val isCurrent = stepNumber == currentStep
                        BasicText(
                            text = label,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (isCurrent) Color(0xFFC2338F) else Color(0xFF62627A),
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (activeErrorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFCE8E8))
                        .border(1.dp, Color(0xFFF5B6C4), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = activeErrorMessage,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFFB42345),
                        ),
                    )
                }
            }

            when (currentStep) {
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "¿Qué tipo de evento planeas?",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.48).sp,
                                lineHeight = 28.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "Elige la categoría y continúa para indicar fecha, lugar e invitados.",
                            style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    val eventTypes = listOf(
                        EventTypeCard("Cumpleaños", Color(0xFFFFF2C9), EventTypeIllus.CUMPLE),
                        EventTypeCard("Boda", Color(0xFFFDE6F2), EventTypeIllus.BODA),
                        EventTypeCard("Quinceañera", Color(0xFFEFE8FD), EventTypeIllus.QUINCEANERA),
                        EventTypeCard("Baby shower", Color(0xFFDDF4F3), EventTypeIllus.BABY_SHOWER),
                        EventTypeCard("Graduación", RfThemeState.currentPalette.chipBg, EventTypeIllus.GRADUACION),
                        EventTypeCard("Corporativo", Color(0xFFEEF6D6), EventTypeIllus.CORPORATIVO),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        eventTypes.chunked(2).forEach { rowCards ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                rowCards.forEach { cardItem ->
                                    val isSelected = cardItem.name == selectedType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(108.dp)
                                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(cardItem.bg)
                                            .then(
                                                if (isSelected) {
                                                    Modifier.border(2.5.dp, Color(0xFFB754CB), RoundedCornerShape(20.dp))
                                                } else {
                                                    Modifier
                                                },
                                            )
                                            .clickable {
                                                selectedType = cardItem.name
                                                localValidationError = null
                                                viewModel?.updateRosaParameters(eventType = cardItem.name)
                                            },
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .align(Alignment.TopStart)
                                                    .offset(x = 12.dp, y = 12.dp)
                                                    .shadow(8.dp, CircleShape, spotColor = Color(0xB36664E9))
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)),
                                                        ),
                                                    ),
                                            ) {
                                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                            }
                                        }
                                        EventTypeIllustrationCanvas38(
                                            illustration = cardItem.illus,
                                            modifier = Modifier
                                                .size(84.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = 2.dp),
                                        )
                                        BasicText(
                                            text = cardItem.name,
                                            style = TextStyle(
                                                fontFamily = typo.displayFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                letterSpacing = (-0.34).sp,
                                                color = RfThemeState.currentPalette.ink,
                                            ),
                                            modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 12.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "Nombre del evento (opcional)",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .rfInputShadow(RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicTextField(
                                value = customEventTitle,
                                onValueChange = { updatedText ->
                                    customEventTitle = updatedText
                                    localValidationError = null
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                                cursorBrush = SolidColor(Color(0xFFC2338F)),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (customEventTitle.isEmpty()) {
                                        BasicText(
                                            text = "Ej. $selectedType de Camila",
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 14.sp,
                                                color = RfThemeState.currentPalette.muted,
                                            ),
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFDE6F2))
                            .border(1.dp, Color(0xFFF2C2DF), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel?.setEventTypeInput(resolvedEventTitle)
                                viewModel?.updateRosaParameters(eventType = selectedType)
                                onSelectScreenCode("s28")
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFFC2338F), size = 18.dp)
                        BasicText(
                            text = "¿Prefieres crearlo con Rosa IA?",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFC2338F),
                            ),
                        )
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFFC2338F), size = 18.dp)
                    }
                }

                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "Fecha y lugar",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.48).sp,
                                lineHeight = 28.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "Selecciona cuándo será tu $selectedType y el salón o dirección.",
                            style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                            .clip(RoundedCornerShape(22.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText(
                                text = "Fecha del evento",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            RfMaskedDateField(
                                value = dateInput,
                                onValueChange = { updatedDate ->
                                    dateInput = updatedDate
                                    localValidationError = null
                                    viewModel?.setEventDateInput(updatedDate)
                                },
                                onOpenCalendar = { showCalendarModal = true },
                                placeholder = "DD/MM/AAAA",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText(
                                text = "Salón o dirección",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(RfThemeState.currentPalette.surface)
                                    .border(1.5.dp, Color(0xFFD9B8EC), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFFC2338F), size = 18.dp)
                                BasicTextField(
                                    value = venueInput,
                                    onValueChange = { updatedVenue ->
                                        venueInput = updatedVenue
                                        localValidationError = null
                                        viewModel?.setEventVenueInput(updatedVenue)
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = RfThemeState.currentPalette.ink,
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { innerTextField ->
                                        if (venueInput.isEmpty()) {
                                            BasicText(
                                                text = "Ej. Jardín Los Rosales, Piantini",
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontSize = 14.sp,
                                                    color = RfThemeState.currentPalette.muted,
                                                ),
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("Jardín Botánico", "Salón Piantini", "Terraza Colonial").forEach { suggestedVenue ->
                                val isChosenVenue = venueInput.trim().equals(suggestedVenue, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (isChosenVenue) Color(0xFFFDE6F2) else RfThemeState.currentPalette.chipBg)
                                        .clickable {
                                            venueInput = suggestedVenue
                                            localValidationError = null
                                            viewModel?.setEventVenueInput(suggestedVenue)
                                        }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = suggestedVenue,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (isChosenVenue) Color(0xFFC2338F) else RfThemeState.currentPalette.ink,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "Invitados y confirmación",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.48).sp,
                                lineHeight = 28.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "Indica el número estimado de asistentes y revisa el resumen antes de crear el evento.",
                            style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                            .clip(RoundedCornerShape(22.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BasicText(
                            text = "Cantidad de invitados",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(1.5.dp, Color(0xFFD9B8EC), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RfIcon(kind = RfIconKind.GUESTS, tint = Color(0xFFC2338F), size = 18.dp)
                            BasicTextField(
                                value = guestsInput,
                                onValueChange = { updatedText ->
                                    val digitsOnly = updatedText.filter { charItem -> charItem.isDigit() }.take(4)
                                    guestsInput = digitsOnly
                                    localValidationError = null
                                    viewModel?.setEventGuestsInput(digitsOnly)
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                                cursorBrush = SolidColor(Color(0xFFC2338F)),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (guestsInput.isEmpty()) {
                                        BasicText(
                                            text = "Ej. 120",
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 15.sp,
                                                color = RfThemeState.currentPalette.muted,
                                            ),
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                            BasicText(
                                text = "personas",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 13.sp,
                                    color = RfThemeState.currentPalette.muted,
                                ),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(40, 80, 120, 150).forEach { guestPreset ->
                                val isPresetSelected = guestsInput == guestPreset.toString()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (isPresetSelected) Color(0xFFFDE6F2) else RfThemeState.currentPalette.chipBg)
                                        .clickable {
                                            guestsInput = guestPreset.toString()
                                            localValidationError = null
                                            viewModel?.setEventGuestsInput(guestPreset.toString())
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = "$guestPreset",
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isPresetSelected) Color(0xFFC2338F) else RfThemeState.currentPalette.ink,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFDF8FC))
                            .border(1.dp, Color(0xFFF0DCEF), RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BasicText(
                            text = "RESUMEN DEL EVENTO",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFFC2338F),
                            ),
                        )
                        BasicText(
                            text = resolvedEventTitle,
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "Categoría: $selectedType",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                        BasicText(
                            text = "Fecha: ${dateInput.ifBlank { "Por definir" }}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                        BasicText(
                            text = "Lugar: ${venueInput.ifBlank { "Por definir" }}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                        BasicText(
                            text = "Invitados: ${guestsInput.ifBlank { "0" }} personas",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = RfThemeState.currentPalette.ink),
                        )
                    }
                }
            }
        }

        val primaryButtonLabel = when (currentStep) {
            1 -> "Siguiente: fecha y lugar"
            2 -> "Siguiente: invitados"
            else -> if (isSubmitting) "Creando evento..." else "Crear evento"
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
                .fillMaxWidth()
                .height(56.dp)
                .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                .clip(RoundedCornerShape(16.dp))
                .background(RosaButtonGradient)
                .clickable(enabled = !isSubmitting) {
                    when (currentStep) {
                        1 -> {
                            localValidationError = null
                            viewModel?.setEventTypeInput(resolvedEventTitle)
                            viewModel?.updateRosaParameters(eventType = selectedType)
                            currentStep = 2
                        }

                        2 -> {
                            val cleanDate = dateInput.trim()
                            val cleanVenue = venueInput.trim()
                            val hasDate = cleanDate.isNotEmpty()
                            val hasVenue = cleanVenue.isNotEmpty()
                            val canAdvanceToStepThree = if (hasDate) hasVenue else false
                            if (!canAdvanceToStepThree) {
                                localValidationError = "Completa la fecha y el lugar de tu evento para continuar."
                            } else {
                                localValidationError = null
                                viewModel?.setEventDateInput(cleanDate)
                                viewModel?.setEventVenueInput(cleanVenue)
                                currentStep = 3
                            }
                        }

                        else -> {
                            val cleanDate = dateInput.trim()
                            val cleanVenue = venueInput.trim()
                            val parsedGuests = guestsInput.trim().toIntOrNull() ?: 0
                            val hasDateAndVenue = if (cleanDate.isNotEmpty()) cleanVenue.isNotEmpty() else false
                            val canCreateManualEvent = if (hasDateAndVenue) parsedGuests > 0 else false
                            if (!canCreateManualEvent) {
                                localValidationError = "Indica una cantidad de invitados mayor a 0, fecha y lugar válidos."
                            } else {
                                localValidationError = null
                                viewModel?.setEventTypeInput(resolvedEventTitle)
                                viewModel?.setEventDateInput(cleanDate)
                                viewModel?.setEventVenueInput(cleanVenue)
                                viewModel?.setEventGuestsInput(parsedGuests.toString())
                                viewModel?.updateRosaParameters(guests = parsedGuests, eventType = selectedType)
                                val createdSuccessfully = if (viewModel != null) {
                                    viewModel.submitQuickClientEvent()
                                } else {
                                    true
                                }
                                if (createdSuccessfully) {
                                    onSelectScreenCode("s40")
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    text = primaryButtonLabel,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
            }
        }

        if (showCalendarModal) {
            Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                RfInteractiveCalendarModal(
                    title = "Fecha del evento",
                    subtitle = "Elige el día de tu celebración",
                    currentDateText = dateInput,
                    minYear = 2026,
                    maxYear = 2032,
                    defaultYear = 2026,
                    quickYears = listOf(2026, 2027, 2028, 2029),
                    isAdmin = false,
                    onSelectDate = { formattedDate ->
                        dateInput = formattedDate
                        localValidationError = null
                        viewModel?.setEventDateInput(formattedDate)
                        showCalendarModal = false
                    },
                    onDismiss = { showCalendarModal = false },
                )
            }
        }
    }
}

@Composable
private fun EventTypeIllustrationCanvas38(
    illustration: EventTypeIllus,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        when (illustration) {
            EventTypeIllus.NONE -> Unit

            EventTypeIllus.CUMPLE -> {
                drawRoundRect(
                    color = Color(0xFFFFF6EE),
                    topLeft = Offset(canvasWidth * 0.25f, canvasHeight * 0.48f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.50f, canvasHeight * 0.33f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * density),
                )
                drawRect(
                    color = Color(0xFFF9B6D3),
                    topLeft = Offset(canvasWidth * 0.25f, canvasHeight * 0.68f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.50f, canvasHeight * 0.05f),
                )
                drawRoundRect(
                    color = Color(0xFF7650E8),
                    topLeft = Offset(canvasWidth * 0.35f, canvasHeight * 0.34f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.04f, canvasHeight * 0.14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * density),
                )
                drawRoundRect(
                    color = Color(0xFF4AA8E0),
                    topLeft = Offset(canvasWidth * 0.48f, canvasHeight * 0.32f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.04f, canvasHeight * 0.17f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * density),
                )
                drawRoundRect(
                    color = Color(0xFF2FB58F),
                    topLeft = Offset(canvasWidth * 0.61f, canvasHeight * 0.34f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.04f, canvasHeight * 0.14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * density),
                )
                drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = Offset(canvasWidth * 0.37f, canvasHeight * 0.31f))
                drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = Offset(canvasWidth * 0.50f, canvasHeight * 0.29f))
                drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = Offset(canvasWidth * 0.63f, canvasHeight * 0.31f))
            }

            EventTypeIllus.BODA -> {
                val ringStroke = 6.5f * density
                drawCircle(
                    color = Color(0xFFD9A441),
                    radius = canvasWidth * 0.18f,
                    center = Offset(canvasWidth * 0.40f, canvasHeight * 0.58f),
                    style = Stroke(width = ringStroke),
                )
                drawCircle(
                    color = Color(0xFFF0CC7A),
                    radius = canvasWidth * 0.18f,
                    center = Offset(canvasWidth * 0.60f, canvasHeight * 0.60f),
                    style = Stroke(width = ringStroke),
                )
                val diamondPath = Path().apply {
                    moveTo(canvasWidth * 0.35f, canvasHeight * 0.36f)
                    lineTo(canvasWidth * 0.47f, canvasHeight * 0.30f)
                    lineTo(canvasWidth * 0.47f, canvasHeight * 0.37f)
                    lineTo(canvasWidth * 0.41f, canvasHeight * 0.44f)
                    close()
                }
                drawPath(diamondPath, color = Color(0xFFA8D8F5))
            }

            EventTypeIllus.QUINCEANERA -> {
                val crownPath = Path().apply {
                    moveTo(canvasWidth * 0.20f, canvasHeight * 0.75f)
                    lineTo(canvasWidth * 0.20f, canvasHeight * 0.50f)
                    lineTo(canvasWidth * 0.35f, canvasHeight * 0.62f)
                    lineTo(canvasWidth * 0.50f, canvasHeight * 0.40f)
                    lineTo(canvasWidth * 0.65f, canvasHeight * 0.62f)
                    lineTo(canvasWidth * 0.80f, canvasHeight * 0.50f)
                    lineTo(canvasWidth * 0.80f, canvasHeight * 0.75f)
                    close()
                }
                drawPath(crownPath, color = Color(0xFFD9A441))
                drawCircle(color = Color(0xFFEC4F97), radius = 5f * density, center = Offset(canvasWidth * 0.50f, canvasHeight * 0.38f))
                drawCircle(color = Color(0xFF7650E8), radius = 3.5f * density, center = Offset(canvasWidth * 0.30f, canvasHeight * 0.58f))
                drawCircle(color = Color(0xFF2FB58F), radius = 3.5f * density, center = Offset(canvasWidth * 0.70f, canvasHeight * 0.56f))
            }

            EventTypeIllus.BABY_SHOWER -> {
                val cordPath = Path().apply {
                    moveTo(canvasWidth * 0.05f, canvasHeight * 0.25f)
                    cubicTo(canvasWidth * 0.50f, canvasHeight * 0.38f, canvasWidth * 0.95f, canvasHeight * 0.25f, canvasWidth * 0.95f, canvasHeight * 0.25f)
                }
                drawPath(path = cordPath, color = Color(0xFF6E5A72).copy(alpha = 0.45f), style = Stroke(width = 1.5f * density))
                drawRoundRect(
                    color = Color(0xFF9BE3C9),
                    topLeft = Offset(canvasWidth * 0.33f, canvasHeight * 0.35f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.34f, canvasHeight * 0.38f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * density),
                )
                drawRoundRect(
                    color = Color(0xFF9BE3C9),
                    topLeft = Offset(canvasWidth * 0.18f, canvasHeight * 0.35f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.17f, canvasHeight * 0.16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density),
                )
                drawRoundRect(
                    color = Color(0xFF9BE3C9),
                    topLeft = Offset(canvasWidth * 0.65f, canvasHeight * 0.35f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.17f, canvasHeight * 0.16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density),
                )
                drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = Offset(canvasWidth * 0.50f, canvasHeight * 0.54f))
            }

            EventTypeIllus.GRADUACION -> {
                val capColor = Color(0xFF1A1A2E)
                drawRoundRect(
                    color = capColor,
                    topLeft = Offset(canvasWidth * 0.20f, canvasHeight * 0.45f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.60f, canvasHeight * 0.14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * density),
                )
                val hatPath = Path().apply {
                    moveTo(canvasWidth * 0.50f, canvasHeight * 0.28f)
                    lineTo(canvasWidth * 0.80f, canvasHeight * 0.45f)
                    lineTo(canvasWidth * 0.50f, canvasHeight * 0.60f)
                    lineTo(canvasWidth * 0.20f, canvasHeight * 0.45f)
                    close()
                }
                drawPath(hatPath, color = capColor)
                drawLine(
                    color = Color(0xFFF5B53B),
                    start = Offset(canvasWidth * 0.76f, canvasHeight * 0.45f),
                    end = Offset(canvasWidth * 0.76f, canvasHeight * 0.72f),
                    strokeWidth = 2.5f * density,
                )
                drawCircle(color = Color(0xFFF5B53B), radius = 5f * density, center = Offset(canvasWidth * 0.76f, canvasHeight * 0.75f))
            }

            EventTypeIllus.CORPORATIVO -> {
                val glassColor = Color(0xFFD9A441)
                val leftGlassPath = Path().apply {
                    moveTo(canvasWidth * 0.28f, canvasHeight * 0.25f)
                    lineTo(canvasWidth * 0.38f, canvasHeight * 0.25f)
                    lineTo(canvasWidth * 0.34f, canvasHeight * 0.50f)
                    lineTo(canvasWidth * 0.32f, canvasHeight * 0.50f)
                    close()
                }
                drawPath(leftGlassPath, color = glassColor)
                drawLine(color = glassColor, start = Offset(canvasWidth * 0.33f, canvasHeight * 0.50f), end = Offset(canvasWidth * 0.33f, canvasHeight * 0.72f), strokeWidth = 2.5f * density)
                val rightGlassPath = Path().apply {
                    moveTo(canvasWidth * 0.62f, canvasHeight * 0.25f)
                    lineTo(canvasWidth * 0.72f, canvasHeight * 0.25f)
                    lineTo(canvasWidth * 0.68f, canvasHeight * 0.50f)
                    lineTo(canvasWidth * 0.66f, canvasHeight * 0.50f)
                    close()
                }
                drawPath(rightGlassPath, color = glassColor)
                drawLine(color = glassColor, start = Offset(canvasWidth * 0.67f, canvasHeight * 0.50f), end = Offset(canvasWidth * 0.67f, canvasHeight * 0.72f), strokeWidth = 2.5f * density)
                drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 3f * density, center = Offset(canvasWidth * 0.32f, canvasHeight * 0.35f))
                drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 2f * density, center = Offset(canvasWidth * 0.36f, canvasHeight * 0.28f))
                drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 3f * density, center = Offset(canvasWidth * 0.68f, canvasHeight * 0.33f))
            }
        }
    }
}

@Composable
internal fun RenderCalendarScreenS39(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val observedEvents = viewModel?.events?.collectAsState()?.value
    val observedUser = viewModel?.currentAuthenticatedUser?.collectAsState()?.value
    val observedGuestSession = viewModel?.isExplicitGuestSession?.collectAsState()?.value
    val observedProfileName = viewModel?.profileName?.collectAsState()?.value

    val userEvents = remember(viewModel, observedEvents, observedUser, observedGuestSession, observedProfileName) {
        if (viewModel != null) {
            viewModel.clientEventsForCurrentUser()
        } else {
            CanonicalSampleData.events.filter { eventItem ->
                eventItem.clientName.equals("Camila Rodríguez", ignoreCase = true)
            }
        }
    }

    var monthOffset by remember { mutableStateOf(0) }
    var selectedDayFilter by remember { mutableStateOf<Int?>(null) }

    val baseMonthIndex = (2026 * 12) + 9 + monthOffset
    val displayedYear = baseMonthIndex.floorDiv(12)
    val displayedMonth = baseMonthIndex.mod(12) + 1
    val daysInMonth = computeDaysInGregorianMonth(displayedYear, displayedMonth)
    val leadingWeekdayOffset = computeGregorianMondayFirstOffset(displayedYear, displayedMonth)
    val totalGridCells = leadingWeekdayOffset + daysInMonth
    val totalGridRows = ((totalGridCells + 6) / 7).coerceIn(4, 6)

    val eventsWithParsedDates = remember(userEvents) {
        userEvents.map { eventItem ->
            eventItem to parseEventCalendarDate(eventItem.dateLabel, defaultYear = 2026)
        }
    }
    val eventsInDisplayedMonth = remember(eventsWithParsedDates, displayedYear, displayedMonth) {
        eventsWithParsedDates.filter { (_, parsedDate) ->
            val matchesYear = parsedDate?.year == displayedYear
            val matchesMonth = parsedDate?.month == displayedMonth
            if (matchesYear) matchesMonth else false
        }
    }
    val eventDaysInMonth = remember(eventsInDisplayedMonth) {
        eventsInDisplayedMonth.mapNotNull { (_, parsedDate) -> parsedDate?.day }.toSet()
    }
    val filteredEventEntries = remember(eventsInDisplayedMonth, selectedDayFilter) {
        val activeDay = selectedDayFilter
        if (activeDay == null) {
            eventsInDisplayedMonth
        } else {
            eventsInDisplayedMonth.filter { (_, parsedDate) -> parsedDate?.day == activeDay }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Calendario",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfPrimaryButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s38") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.chipBg)
                                .clickable {
                                    monthOffset = (monthOffset - 1).coerceAtLeast(-24)
                                    selectedDayFilter = null
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                        }

                        BasicText(
                            text = "${spanishMonthLabel(displayedMonth)} $displayedYear",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.chipBg)
                                .clickable {
                                    monthOffset = (monthOffset + 1).coerceAtMost(36)
                                    selectedDayFilter = null
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { dayLabel ->
                            BasicText(
                                text = dayLabel,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = RfThemeState.currentPalette.muted,
                                    fontSize = 12.sp,
                                ),
                            )
                        }
                    }

                    for (rowIndex in 0 until totalGridRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            for (columnIndex in 0 until 7) {
                                val cellIndex = (rowIndex * 7) + columnIndex
                                val dayNum = cellIndex - leadingWeekdayOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val isEventDay = eventDaysInMonth.contains(dayNum)
                                    val isSelectedDay = selectedDayFilter == dayNum
                                    val highlightDay = if (isSelectedDay) true else isEventDay
                                    val cellBgColor = when {
                                        isSelectedDay -> Color(0xFFEA4EA9)
                                        isEventDay -> Color(0xFFFDE6F2)
                                        else -> Color.Transparent
                                    }
                                    val cellTextColor = when {
                                        isSelectedDay -> Color.White
                                        isEventDay -> Color(0xFFC2338F)
                                        else -> RfThemeState.currentPalette.ink
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(cellBgColor)
                                            .clickable {
                                                selectedDayFilter = if (selectedDayFilter == dayNum) null else dayNum
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            BasicText(
                                                text = "$dayNum",
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontWeight = if (highlightDay) FontWeight.Bold else FontWeight.Normal,
                                                    color = cellTextColor,
                                                    fontSize = 13.sp,
                                                ),
                                            )
                                            if (isEventDay) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelectedDay) Color.White else Color(0xFFEA4EA9)),
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val listHeading = if (selectedDayFilter != null) {
                    "Eventos del $selectedDayFilter de ${spanishMonthLabel(displayedMonth).lowercase()} (${filteredEventEntries.size})"
                } else {
                    "Eventos en ${spanishMonthLabel(displayedMonth).lowercase()} (${filteredEventEntries.size})"
                }
                BasicText(
                    text = listHeading,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        color = RfThemeState.currentPalette.ink,
                        fontSize = 15.sp,
                    ),
                )
                if (selectedDayFilter != null) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFDE6F2))
                            .clickable { selectedDayFilter = null }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Ver todo el mes",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFC2338F),
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredEventEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    BasicText(
                        text = if (selectedDayFilter != null) {
                            "No hay eventos registrados para el día $selectedDayFilter. Toca \"Ver todo el mes\" para explorar otras fechas."
                        } else {
                            "Sin eventos programados en ${spanishMonthLabel(displayedMonth)} $displayedYear."
                        },
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            color = RfThemeState.currentPalette.muted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        ),
                    )
                }
            } else {
                filteredEventEntries.forEachIndexed { idx, (eventItem, _) ->
                    if (idx > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel?.selectEvent(eventItem.id)
                                onSelectScreenCode("s40")
                            }
                            .padding(14.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == 0) Color(0xFFEA4EA9) else Color(0xFF865DEC)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = eventItem.dateLabel,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = RfThemeState.currentPalette.ink,
                                        fontSize = 14.sp,
                                    ),
                                )
                                BasicText(
                                    text = "${eventItem.title} · ${eventItem.venue}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        color = RfThemeState.currentPalette.muted,
                                        fontSize = 13.sp,
                                    ),
                                )
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
internal fun RenderEventDetailScreenS40(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val observedUser = viewModel?.currentAuthenticatedUser?.collectAsState()?.value
    val observedGuestSession = viewModel?.isExplicitGuestSession?.collectAsState()?.value
    val observedProfileName = viewModel?.profileName?.collectAsState()?.value
    val activeEvent = remember(viewModel, eventsState, selectedEventId, observedUser, observedGuestSession, observedProfileName) {
        viewModel?.activeClientEventOrNull()
            ?: eventsState.firstOrNull { eventItem -> eventItem.id == selectedEventId }
            ?: eventsState.firstOrNull()
    }
    val eventTitle = activeEvent?.title ?: "Boda Real Rosa & Dorado"
    val activeVenue = activeEvent?.venue ?: "Jardín Botánico"
    val eventSubtitle = if (activeEvent != null) {
        "${activeEvent.dateLabel} · ${activeEvent.venue}"
    } else {
        "Vie 23 oct · Jardín Botánico"
    }
    val daysUntil = (activeEvent?.daysUntilEvent ?: 3).coerceAtLeast(0)
    val daysFormatted = daysUntil.toString().padStart(2, '0')

    val checklistSteps = viewModel?.checklist?.collectAsState()?.value ?: CanonicalSampleData.bodaChecklist
    val completedStepsCount = checklistSteps.count { stepItem -> stepItem.completed }
    val totalStepsCount = checklistSteps.size

    val vmEventTasks = viewModel?.eventTasksList?.collectAsState()?.value
    val completedTasksCount = vmEventTasks?.count { taskItem -> taskItem.completed } ?: 2
    val totalTasksCount = vmEventTasks?.size ?: 4

    val vmTimelineItems = viewModel?.eventTimelineItems?.collectAsState()?.value
    val timelineBlocksCount = vmTimelineItems?.size ?: 4
    val firstStartTime = vmTimelineItems?.firstOrNull()?.startTime ?: "09:00"
    val timeTokens = firstStartTime.split(":")
    val countdownHours = if (daysUntil == 0) {
        "00"
    } else {
        (timeTokens.getOrNull(0)?.filter { charItem -> charItem.isDigit() }?.take(2) ?: "09").padStart(2, '0')
    }
    val countdownMinutes = if (daysUntil == 0) {
        "00"
    } else {
        (timeTokens.getOrNull(1)?.filter { charItem -> charItem.isDigit() }?.take(2) ?: "00").padStart(2, '0')
    }

    val vmGuests = viewModel?.eventGuestsList?.collectAsState()?.value
    val confirmedGuestsCount = vmGuests?.count { guestItem -> guestItem.status == "Confirmado" } ?: 3
    val totalRegisteredGuests = vmGuests?.size ?: 4

    val paidDop = activeEvent?.paidDepositDop ?: 1350
    val totalDop = (activeEvent?.totalDop ?: 4500).coerceAtLeast(1)
    val paidFraction = (paidDop.toFloat() / totalDop.toFloat()).coerceIn(0f, 1f)
    val paidPercent = (paidFraction * 100).toInt()
    val vmFeedback = viewModel?.formFeedback?.collectAsState()?.value

    var showMoreActionsSheet by remember { mutableStateOf(false) }
    var localActionFeedback by remember { mutableStateOf<String?>(null) }
    val visibleBannerMessage = localActionFeedback ?: vmFeedback

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 30.dp),
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
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFEEEAF2), CircleShape)
                            .clickable { onSelectScreenCode("BACK:s37") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = eventTitle,
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        BasicText(
                            text = eventSubtitle,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFEEEAF2), CircleShape)
                            .clickable {
                                if (activeEvent != null) {
                                    val exportedFile = viewModel?.exportReceiptDocument(activeEvent)
                                    if (exportedFile != null) {
                                        localActionFeedback = "Documento exportado: $exportedFile"
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.DOWNLOAD, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFEEEAF2), CircleShape)
                            .clickable { showMoreActionsSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }
                }

                if (!visibleBannerMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEEF6D6))
                            .border(1.dp, Color(0xFFCDE298), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        BasicText(
                            text = visibleBannerMessage,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .height(44.dp)
                                .width(IntrinsicSize.Max),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Detalles",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD63F98),
                                    ),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp)
                                    .background(Color(0xFFEA4EA9)),
                            )
                        }

                        Row(
                            modifier = Modifier
                                .height(44.dp)
                                .clickable { onSelectScreenCode("s41") },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            BasicText(
                                text = "Chat",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RfThemeState.currentPalette.muted,
                                ),
                            )
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD45C))
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "2",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2E1A47),
                                    ),
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE9E6EE)),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(22.dp)),
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val scaleFactor = size.width / 350f
                        fun at(posX: Float, posY: Float) = Offset(posX * scaleFactor, posY * scaleFactor)

                        drawCircle(
                            color = Color(0xFFD9A441),
                            radius = 21f * scaleFactor,
                            center = at(295f, 57f),
                            style = Stroke(width = 5f * scaleFactor),
                        )
                        drawCircle(
                            color = Color(0xFFF0CC7A),
                            radius = 21f * scaleFactor,
                            center = at(312f, 59f),
                            style = Stroke(width = 5f * scaleFactor),
                        )
                        drawPath(
                            path = fivePointStar(at(222f, 25f), 11f * scaleFactor, 4.4f * scaleFactor),
                            color = Color(0xFFFFCE47),
                        )
                        drawPath(
                            path = fourPointSparkle(at(274f, 28f), 7f * scaleFactor),
                            color = Color(0xFFE86FC0),
                        )
                        drawPath(
                            path = fourPointSparkle(at(294f, 27f), 5f * scaleFactor),
                            color = Color(0xFF8FD3F0),
                        )
                        drawPath(
                            path = heartPath(at(335f, 81f), 6f * scaleFactor),
                            color = Color(0xFFF6C3DE),
                        )
                        drawCircle(Color(0xFFB9D84A), radius = 3.5f * scaleFactor, center = at(238f, 87f))
                    }

                    Column(modifier = Modifier.padding(start = 18.dp, top = 14.dp, end = 18.dp)) {
                        BasicText(
                            text = "FALTAN PARA TU EVENTO",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            CountdownUnit(value = daysFormatted, label = "días", color = Color(0xFFEB4F9E))
                            CountdownUnit(value = countdownHours, label = "horas", color = Color(0xFFF2894A))
                            CountdownUnit(value = countdownMinutes, label = "min", color = Color(0xFFE8B53A))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(22.dp),
                            spotColor = Color(0x60463C8C),
                            ambientColor = Color(0x20463C8C),
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                        .clickable { onSelectScreenCode("s43") }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Modo ejecución",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White,
                            ),
                        )
                        BasicText(
                            text = "Checklist para el día del evento",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                color = Color(0xFFE4E2F2),
                            ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD45C)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLAY, tint = Color.Black, size = 20.dp, filled = true)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(22.dp))
                        .padding(vertical = 4.dp),
                ) {
                    ActionMenuRow(
                        title = "Tareas",
                        subtitle = "$completedTasksCount de $totalTasksCount completadas",
                        icon = RfIconKind.CHECKLIST,
                        iconBg = Color(0xFFDDF4F3),
                        iconTint = Color(0xFF2E9C96),
                        onClick = { onSelectScreenCode("s45") },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                    ActionMenuRow(
                        title = "Cronograma",
                        subtitle = "$timelineBlocksCount actividades por hora",
                        icon = RfIconKind.CLOCK,
                        iconBg = Color(0xFFFFF2C9),
                        iconTint = Color(0xFFB07D00),
                        onClick = { onSelectScreenCode("s44") },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                    ActionMenuRow(
                        title = "Checklist y pagos",
                        subtitle = "$completedStepsCount de $totalStepsCount pasos listos",
                        icon = RfIconKind.WALLET,
                        iconBg = Color(0xFFFDE6F2),
                        iconTint = Color(0xFFC2338F),
                        onClick = { onSelectScreenCode("s46") },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                    ActionMenuRow(
                        title = "Lista de invitados",
                        subtitle = "$confirmedGuestsCount confirmados de $totalRegisteredGuests registrados",
                        icon = RfIconKind.GUESTS,
                        iconBg = Color(0xFFEFE8FD),
                        iconTint = Color(0xFF7A4FD8),
                        onClick = { onSelectScreenCode("s47") },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                    ActionMenuRow(
                        title = "Sincronizar calendario",
                        subtitle = "Google o Apple Calendar",
                        icon = RfIconKind.CALENDAR,
                        iconBg = Color(0xFFEEF6D6),
                        iconTint = Color(0xFF5E7A12),
                        onClick = {
                            val calendarUrl = buildGoogleCalendarTemplateUrl(
                                eventTitle = eventTitle,
                                venue = activeVenue,
                                dateSummary = eventSubtitle,
                            )
                            val openedCalendar = viewModel?.openExternalUrl(calendarUrl) == true
                            if (openedCalendar) {
                                localActionFeedback = "Abriendo Google Calendar para $eventTitle."
                            }
                        },
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(22.dp))
                        .clickable { onSelectScreenCode("s82") }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = "Pagos",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF6D6))
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "$paidPercent% pagado",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E5A12),
                                ),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEEEDF2)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(paidFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFCFE48A), Color(0xFFA9CC3A)))),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("Pagado", style = TextStyle(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText("RD$ $paidDop", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E7A12)))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("Total", style = TextStyle(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText("RD$ $totalDop", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        }
                    }
                }
            }

            if (showMoreActionsSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(12f)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { showMoreActionsSheet = false },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .clickable(enabled = false) {}
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                text = "Opciones del evento",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .clickable { showMoreActionsSheet = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 18.dp)
                            }
                        }

                        ActionMenuRow(
                            title = "Cambiar fecha",
                            subtitle = "Reprogramar fecha y horario de entrega",
                            icon = RfIconKind.CALENDAR,
                            iconBg = Color(0xFFFFF2C9),
                            iconTint = Color(0xFFB07D00),
                            onClick = {
                                showMoreActionsSheet = false
                                onSelectScreenCode("s84")
                            },
                        )
                        ActionMenuRow(
                            title = "Checklist y pagos",
                            subtitle = "Pasos previos y saldo pendiente",
                            icon = RfIconKind.WALLET,
                            iconBg = Color(0xFFFDE6F2),
                            iconTint = Color(0xFFC2338F),
                            onClick = {
                                showMoreActionsSheet = false
                                onSelectScreenCode("s46")
                            },
                        )
                        ActionMenuRow(
                            title = "Lista de invitados",
                            subtitle = "Confirmaciones RSVP y mesas",
                            icon = RfIconKind.GUESTS,
                            iconBg = Color(0xFFEFE8FD),
                            iconTint = Color(0xFF7A4FD8),
                            onClick = {
                                showMoreActionsSheet = false
                                onSelectScreenCode("s47")
                            },
                        )
                        ActionMenuRow(
                            title = "Compartir resumen",
                            subtitle = "Enviar ficha del evento",
                            icon = RfIconKind.SHARE,
                            iconBg = Color(0xFFDDF4F3),
                            iconTint = Color(0xFF1F8A84),
                            onClick = {
                                showMoreActionsSheet = false
                                val summaryBody = "$eventTitle · $eventSubtitle · Invitados: ${activeEvent?.guests ?: 120} · Total: RD$ $totalDop"
                                viewModel?.sharePlainText(eventTitle, summaryBody)
                                localActionFeedback = "Resumen de '$eventTitle' listo para compartir."
                            },
                        )
                        ActionMenuRow(
                            title = "Cancelar evento",
                            subtitle = "Revisar política de reembolso y baja",
                            icon = RfIconKind.CLOSE,
                            iconBg = Color(0xFFFCE8E8),
                            iconTint = Color(0xFFB42345),
                            onClick = {
                                showMoreActionsSheet = false
                                onSelectScreenCode("s85")
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun fivePointStar(center: Offset, outer: Float, inner: Float): Path {
    val path = Path()
    for (stepIndex in 0 until 10) {
        val radius = if (stepIndex % 2 == 0) outer else inner
        val angle = (-90.0 + stepIndex * 36.0) * PI / 180.0
        val coordX = center.x + (radius * cos(angle)).toFloat()
        val coordY = center.y + (radius * sin(angle)).toFloat()
        if (stepIndex == 0) path.moveTo(coordX, coordY) else path.lineTo(coordX, coordY)
    }
    path.close()
    return path
}

private fun fourPointSparkle(center: Offset, radius: Float): Path {
    val path = Path()
    val thin = radius * 0.28f
    path.moveTo(center.x, center.y - radius)
    path.lineTo(center.x + thin, center.y - thin)
    path.lineTo(center.x + radius, center.y)
    path.lineTo(center.x + thin, center.y + thin)
    path.lineTo(center.x, center.y + radius)
    path.lineTo(center.x - thin, center.y + thin)
    path.lineTo(center.x - radius, center.y)
    path.lineTo(center.x - thin, center.y - thin)
    path.close()
    return path
}

private fun heartPath(center: Offset, size: Float): Path {
    val path = Path()
    val scale = size * 0.1f
    path.moveTo(center.x, center.y + 9f * scale)
    path.cubicTo(
        center.x - 12f * scale,
        center.y + 1f * scale,
        center.x - 9f * scale,
        center.y - 10f * scale,
        center.x,
        center.y - 3f * scale,
    )
    path.cubicTo(
        center.x + 9f * scale,
        center.y - 10f * scale,
        center.x + 12f * scale,
        center.y + 1f * scale,
        center.x,
        center.y + 9f * scale,
    )
    path.close()
    return path
}

@Composable
private fun CountdownUnit(
    value: String,
    label: String,
    color: Color,
) {
    val typo = LocalRfTypography.current
    Column(horizontalAlignment = Alignment.Start) {
        BasicText(
            text = value,
            style = TextStyle(
                fontFamily = typo.displayFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                color = color,
                lineHeight = 40.sp,
            ),
        )
        BasicText(
            text = label,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = RfThemeState.currentPalette.muted,
            ),
        )
    }
}

@Composable
private fun ActionMenuRow(
    title: String,
    subtitle: String,
    icon: RfIconKind,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 20.dp)
        }

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RfThemeState.currentPalette.ink,
                ),
            )
            BasicText(
                text = subtitle,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 13.sp,
                    color = RfThemeState.currentPalette.muted,
                ),
            )
        }

        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 18.dp)
    }
}

@Composable
internal fun RenderExecutionModeScreenS43(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val checklistSteps = viewModel?.checklist?.collectAsState()?.value ?: CanonicalSampleData.bodaChecklist
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val activeEvent = viewModel?.activeClientEventOrNull()
        ?: eventsState.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: eventsState.firstOrNull()
    val eventTitle = activeEvent?.title ?: "Boda Real Rosa & Dorado"
    var fallbackCompletedIds by remember {
        mutableStateOf(
            CanonicalSampleData.bodaChecklist
                .filter { stepItem -> stepItem.completed }
                .map { stepItem -> stepItem.id }
                .toSet(),
        )
    }
    val totalCount = checklistSteps.size.coerceAtLeast(1)
    val completedCount = if (viewModel != null) {
        checklistSteps.count { stepItem -> stepItem.completed }
    } else {
        checklistSteps.count { stepItem -> fallbackCompletedIds.contains(stepItem.id) }
    }
    val progressFraction = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progressFraction * 100f).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfHeroBannerShadow(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp), isAdmin = false)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(RosaButtonGradient)
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 28.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable { onSelectScreenCode("BACK:s40") },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color.White, size = 22.dp)
                        }

                        Row(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E1A47)),
                            )
                            BasicText(
                                text = if (completedCount >= totalCount) "Completado" else "En curso",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47), fontSize = 12.sp),
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(84.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 8.dp.toPx()
                                val radius = 34.dp.toPx()
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.22f),
                                    radius = radius,
                                    style = Stroke(width = stroke),
                                )
                                drawArc(
                                    color = Color(0xFFFFD45C),
                                    startAngle = -90f,
                                    sweepAngle = 360f * progressFraction,
                                    useCenter = false,
                                    style = Stroke(
                                        width = stroke,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    ),
                                    topLeft = Offset(
                                        center.x - radius,
                                        center.y - radius,
                                    ),
                                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                                )
                            }
                            BasicText(
                                text = "$progressPercent%",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp),
                            )
                        }

                        Column {
                            BasicText(
                                text = "Modo ejecución",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    lineHeight = 25.sp,
                                    letterSpacing = (-0.5).sp,
                                ),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText(
                                text = "$eventTitle · $completedCount de $totalCount tareas",
                                style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 14.sp),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                checklistSteps.forEach { stepItem ->
                    val isCompleted = if (viewModel != null) {
                        stepItem.completed
                    } else {
                        fallbackCompletedIds.contains(stepItem.id)
                    }
                    val cleanResponsible = stepItem.completedBy.trim()
                    val rowSubtitle = if (cleanResponsible.isNotEmpty()) {
                        "${stepItem.stageLabel} · $cleanResponsible"
                    } else {
                        stepItem.stageLabel
                    }
                    ExecutionTaskRow43(
                        title = stepItem.title,
                        subtitle = rowSubtitle,
                        timeLabel = stepItem.timeLabel.trim(),
                        completed = isCompleted,
                        onClick = {
                            if (viewModel != null) {
                                viewModel.toggleChecklistStep(stepItem.id)
                            } else {
                                fallbackCompletedIds = if (isCompleted) {
                                    fallbackCompletedIds - stepItem.id
                                } else {
                                    fallbackCompletedIds + stepItem.id
                                }
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ExecutionTaskRow43(
    title: String,
    subtitle: String,
    timeLabel: String,
    completed: Boolean,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
            .clip(RoundedCornerShape(20.dp))
            .background(if (completed) Color(0xFFF6F4F9) else Color.White)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (completed) Color(0xFF8DB52C) else Color.White)
                .then(
                    if (completed) {
                        Modifier
                    } else {
                        Modifier.border(2.dp, Color(0xFFCDBFCB), RoundedCornerShape(10.dp))
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (completed) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 18.dp)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    color = if (completed) Color(0xFF62627A) else Color(0xFF1A1A2E),
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                ),
            )
            BasicText(
                text = subtitle,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = RfThemeState.currentPalette.muted,
                ),
            )
            if (timeLabel.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    RfIcon(kind = RfIconKind.CLOCK, tint = RfThemeState.currentPalette.muted, size = 14.dp)
                    BasicText(
                        text = timeLabel,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )
                }
            }
        }
    }
}

private data class TimelineItem44(
    val id: String,
    val start: String,
    val end: String,
    val dotColor: Color,
    val ringColor: Color,
    val title: String,
    val subtitle: String,
)

@Composable
internal fun RenderTimelineScreenS44(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val event = viewModel?.activeClientEvent() ?: eventsState.first()
    val persistedTimeline = viewModel?.eventTimelineItems?.collectAsState()?.value

    var fallbackBlocks by remember {
        mutableStateOf(
            listOf(
                TimelineItem44(
                    id = "tl-1",
                    start = "06:38",
                    end = "09:08",
                    dotColor = Color(0xFFF7B500),
                    ringColor = Color(0xFFFFF2C9),
                    title = "Descarga y montaje de mobiliario",
                    subtitle = "Llega el camión con 120 sillas Tiffany y mesas",
                ),
                TimelineItem44(
                    id = "tl-2",
                    start = "09:38",
                    end = "12:38",
                    dotColor = Color(0xFFEA4EA9),
                    ringColor = Color(0xFFFDE6F2),
                    title = "Arco orgánico y flores",
                    subtitle = "Montaje del backdrop principal y centros de mesa",
                ),
            ),
        )
    }

    var showAddBlockModal by remember { mutableStateOf(false) }
    var startTimeInput by remember { mutableStateOf("18:00") }
    var endTimeInput by remember { mutableStateOf("20:00") }
    var blockTitleInput by remember { mutableStateOf("") }
    var blockSubtitleInput by remember { mutableStateOf("") }
    var blockFormError by remember { mutableStateOf<String?>(null) }

    val dotPalette = listOf(
        Color(0xFFF7B500) to Color(0xFFFFF2C9),
        Color(0xFFEA4EA9) to Color(0xFFFDE6F2),
        Color(0xFF1FBDC0) to Color(0xFFDDF4F3),
        Color(0xFF865DEC) to Color(0xFFEFE8FD),
        Color(0xFF8DB52C) to Color(0xFFEEF6D6),
    )
    val timelineItems = if (persistedTimeline != null) {
        persistedTimeline.mapIndexed { idx, rowItem ->
            val (dotCol, ringCol) = dotPalette[idx % dotPalette.size]
            TimelineItem44(
                id = rowItem.id,
                start = rowItem.startTime,
                end = rowItem.endTime,
                dotColor = dotCol,
                ringColor = ringCol,
                title = rowItem.title,
                subtitle = rowItem.subtitle,
            )
        }
    } else {
        fallbackBlocks
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
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
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("BACK:s40") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Cronograma",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                lineHeight = 22.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "${event.title} · ${event.dateLabel}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RosaButtonGradient)
                            .clickable {
                                blockFormError = null
                                showAddBlockModal = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    timelineItems.forEachIndexed { index, item ->
                        TimelineRow44(
                            item = item,
                            isLast = index == timelineItems.lastIndex,
                            onDeleteClick = {
                                if (viewModel != null) {
                                    viewModel.removeEventTimelineItem(item.id)
                                } else {
                                    fallbackBlocks = fallbackBlocks.filterNot { blockItem -> blockItem.id == item.id }
                                }
                            },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDE6F2))
                        .border(1.dp, Color(0xFFF2C2DF), RoundedCornerShape(16.dp))
                        .clickable {
                            blockFormError = null
                            showAddBlockModal = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFFC2338F), size = 18.dp)
                        BasicText(
                            text = "Agregar bloque",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFC2338F),
                            ),
                        )
                    }
                }
            }

            if (showAddBlockModal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(12f)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { showAddBlockModal = false }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(24.dp))
                            .clickable(enabled = false) {}
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                text = "Agregar bloque horario",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .clickable { showAddBlockModal = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                            }
                        }

                        if (blockFormError != null) {
                            BasicText(
                                text = blockFormError.orEmpty(),
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB42345),
                                ),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicText("Inicio", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    BasicTextField(
                                        value = startTimeInput,
                                        onValueChange = { updatedText ->
                                            startTimeInput = updatedText
                                            blockFormError = null
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicText("Fin", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    BasicTextField(
                                        value = endTimeInput,
                                        onValueChange = { updatedText ->
                                            endTimeInput = updatedText
                                            blockFormError = null
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("Actividad", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = blockTitleInput,
                                    onValueChange = { updatedText ->
                                        blockTitleInput = updatedText
                                        blockFormError = null
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (blockTitleInput.isEmpty()) {
                                            BasicText("Ej. Brindis y entrada de novios", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("Detalle", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = blockSubtitleInput,
                                    onValueChange = { updatedText ->
                                        blockSubtitleInput = updatedText
                                        blockFormError = null
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (blockSubtitleInput.isEmpty()) {
                                            BasicText("Ej. Coordinación con DJ e iluminación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = false)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RosaButtonGradient)
                                .clickable {
                                    val cleanStart = startTimeInput.trim()
                                    val cleanEnd = endTimeInput.trim()
                                    val cleanTitle = blockTitleInput.trim()
                                    val cleanSubtitle = blockSubtitleInput.trim()
                                    val hasRequiredScheduleFields = if (cleanStart.isNotEmpty()) cleanTitle.isNotEmpty() else false
                                    if (!hasRequiredScheduleFields) {
                                        blockFormError = "Escribe al menos la hora de inicio y el título del bloque."
                                    } else {
                                        if (viewModel != null) {
                                            viewModel.addEventTimelineItem(
                                                startTime = cleanStart,
                                                endTime = cleanEnd.ifEmpty { cleanStart },
                                                customTitle = cleanTitle,
                                                customSubtitle = cleanSubtitle,
                                            )
                                        } else {
                                            val nextNum = fallbackBlocks.size + 1
                                            fallbackBlocks = fallbackBlocks + TimelineItem44(
                                                id = "tl-$nextNum",
                                                start = cleanStart,
                                                end = cleanEnd.ifEmpty { cleanStart },
                                                dotColor = Color(0xFF8DB52C),
                                                ringColor = Color(0xFFEEF6D6),
                                                title = cleanTitle,
                                                subtitle = cleanSubtitle.ifEmpty { event.title },
                                            )
                                        }
                                        blockTitleInput = ""
                                        blockSubtitleInput = ""
                                        blockFormError = null
                                        showAddBlockModal = false
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Guardar bloque",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow44(
    item: TimelineItem44,
    isLast: Boolean,
    onDeleteClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.width(48.dp).padding(top = 14.dp),
            horizontalAlignment = Alignment.End,
        ) {
            BasicText(
                text = item.start,
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
            )
            BasicText(
                text = item.end,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
            )
        }

        Box(
            modifier = Modifier.width(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .width(2.dp)
                        .height(110.dp)
                        .background(Color(0xFFEADFE7)),
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(item.dotColor)
                    .border(4.dp, item.ringColor, CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                .clip(RoundedCornerShape(20.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                .padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        text = item.title,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        text = item.subtitle,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 19.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                }
            }
        }
    }
}

private data class EventTaskRowState(
    val id: String,
    val title: String,
    val badgeText: String,
    val isUrgent: Boolean,
    val completed: Boolean,
)

@Composable
internal fun RenderTasksScreenS45(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val event = viewModel?.activeClientEvent() ?: eventsState.first()
    val vmTasks = viewModel?.eventTasksList?.collectAsState()?.value
    var fallbackTasks by remember {
        mutableStateOf(
            listOf(
                EventTaskRowState("tsk-1", "Realizar pago inicial de reserva (30%)", "Vence hoy", true, false),
                EventTaskRowState("tsk-2", "Inspección técnica de iluminación exterior", "Mañana", false, false),
                EventTaskRowState("tsk-3", "Confirmar paleta floral blanco y rosa pastel", "18 oct", false, true),
                EventTaskRowState("tsk-4", "Aprobar plano de mesas imperial y sillas Tiffany", "15 oct", false, true),
            ),
        )
    }

    var showAddTaskModal by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDueLabel by remember { mutableStateOf("Esta semana") }
    var taskFormError by remember { mutableStateOf<String?>(null) }

    val taskItems = vmTasks?.map { rowItem ->
        EventTaskRowState(
            id = rowItem.id,
            title = rowItem.title,
            badgeText = rowItem.badgeText,
            isUrgent = rowItem.isUrgent,
            completed = rowItem.completed,
        )
    } ?: fallbackTasks

    val completedCount = taskItems.count { taskItem -> taskItem.completed }
    val totalCount = taskItems.size.coerceAtLeast(1)
    val completionFraction = completedCount.toFloat() / totalCount.toFloat()
    val completionPercent = (completionFraction * 100f).toInt()
    val pendingTasks = taskItems.filter { taskItem -> !taskItem.completed }
    val completedTasks = taskItems.filter { taskItem -> taskItem.completed }

    fun toggleTaskById(taskId: String) {
        if (viewModel != null) {
            viewModel.toggleEventTask(taskId)
        } else {
            fallbackTasks = fallbackTasks.map { item ->
                if (item.id == taskId) item.copy(completed = !item.completed) else item
            }
        }
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
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
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("BACK:s40") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Tareas",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                lineHeight = 22.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = event.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RosaButtonGradient)
                            .clickable {
                                taskFormError = null
                                showAddTaskModal = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicText(
                                    text = "$completedCount",
                                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = RfThemeState.currentPalette.ink),
                                )
                                BasicText(
                                    text = "de ${taskItems.size} hechas",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicText(
                                text = "$completionPercent%",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4E6A10)),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RfThemeState.currentPalette.chipBg),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(completionFraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF8DB52C)),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDE6F2))
                        .border(1.dp, Color(0xFFF2C2DF), RoundedCornerShape(16.dp))
                        .clickable {
                            taskFormError = null
                            showAddTaskModal = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFFC2338F), size = 18.dp)
                        BasicText(
                            text = "Agregar tarea",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                        )
                    }
                }

                if (pendingTasks.isNotEmpty()) {
                    BasicText(
                        text = "PENDIENTES",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                            .clip(RoundedCornerShape(22.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp)),
                    ) {
                        Column {
                            pendingTasks.forEachIndexed { index, taskItem ->
                                if (index > 0) {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggleTaskById(taskItem.id) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 1.dp)
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(RfThemeState.currentPalette.surface)
                                            .border(2.dp, Color(0xFFCDBFCB), CircleShape),
                                    )
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BasicText(
                                            text = taskItem.title,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp, color = RfThemeState.currentPalette.ink),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .clip(CircleShape)
                                                .background(if (taskItem.isUrgent) Color(0xFFFFF2C9) else RfThemeState.currentPalette.chipBg)
                                                .padding(horizontal = 9.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = taskItem.badgeText,
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (taskItem.isUrgent) Color(0xFF7A5700) else Color(0xFF62627A),
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    BasicText(
                        text = "COMPLETADAS",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFF7F3F6))
                            .border(1.dp, Color(0xFFEDE4EA), RoundedCornerShape(22.dp)),
                    ) {
                        Column {
                            completedTasks.forEachIndexed { index, taskItem ->
                                if (index > 0) {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEDE4EA)))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggleTaskById(taskItem.id) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 1.dp)
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF8DB52C)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        BasicText(
                                            text = taskItem.title,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 16.sp,
                                                lineHeight = 21.sp,
                                                color = RfThemeState.currentPalette.muted,
                                                textDecoration = TextDecoration.LineThrough,
                                            ),
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        BasicText(
                                            text = taskItem.badgeText,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (showAddTaskModal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(12f)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { showAddTaskModal = false }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(24.dp))
                            .clickable(enabled = false) {}
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                text = "Nueva tarea del evento",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .clickable { showAddTaskModal = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                            }
                        }

                        if (taskFormError != null) {
                            BasicText(
                                text = taskFormError.orEmpty(),
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB42345),
                                ),
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("Descripción", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = newTaskTitle,
                                    onValueChange = { updatedText ->
                                        newTaskTitle = updatedText
                                        taskFormError = null
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (newTaskTitle.isEmpty()) {
                                            BasicText("Ej. Confirmar playlist de entrada", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText("Plazo / etiqueta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = newTaskDueLabel,
                                    onValueChange = { updatedText ->
                                        newTaskDueLabel = updatedText
                                        taskFormError = null
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Vence hoy", "Mañana", "Esta semana").forEach { duePreset ->
                                    val isPresetChosen = newTaskDueLabel.trim().equals(duePreset, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(if (isPresetChosen) Color(0xFFFDE6F2) else RfThemeState.currentPalette.chipBg)
                                            .clickable { newTaskDueLabel = duePreset }
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = duePreset,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = if (isPresetChosen) Color(0xFFC2338F) else RfThemeState.currentPalette.ink,
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = false)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RosaButtonGradient)
                                .clickable {
                                    val cleanTitle = newTaskTitle.trim()
                                    val cleanDue = newTaskDueLabel.trim().ifEmpty { "Pendiente" }
                                    if (cleanTitle.isEmpty()) {
                                        taskFormError = "Escribe el título de la tarea antes de guardar."
                                    } else {
                                        if (viewModel != null) {
                                            viewModel.addEventTask(customTitle = cleanTitle, badgeLabel = cleanDue)
                                        } else {
                                            val nextNum = fallbackTasks.size + 1
                                            fallbackTasks = listOf(
                                                EventTaskRowState("tsk-$nextNum", cleanTitle, cleanDue, false, false),
                                            ) + fallbackTasks
                                        }
                                        newTaskTitle = ""
                                        taskFormError = null
                                        showAddTaskModal = false
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Guardar tarea",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderChecklistScreenS46(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val checklist = viewModel?.checklist?.collectAsState()?.value ?: CanonicalSampleData.bodaChecklist
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val event = viewModel?.activeClientEvent() ?: eventsState.first()
    val totalEventDop = event.totalDop.coerceAtLeast(0)
    val paidDop = event.paidDepositDop.coerceAtLeast(0)
    val remainingDop = event.balanceDop
    val totalSteps = checklist.size.coerceAtLeast(1)
    val completedCount = checklist.count { checklistStep -> checklistStep.completed }
    val eventPhotoKey = photoKeyForAsset(event.photo)

    var fallbackToggledIds by remember {
        mutableStateOf(
            CanonicalSampleData.bodaChecklist
                .filter { checklistStep -> checklistStep.completed }
                .map { checklistStep -> checklistStep.id }
                .toSet(),
        )
    }

    val iconStyles = listOf(
        Triple(RfIconKind.GUESTS, Color(0xFFEFE8FD), Color(0xFF7A4FD8)),
        Triple(RfIconKind.BOX, Color(0xFFFDE6F2), Color(0xFFC2338F)),
        Triple(RfIconKind.LOCATION, Color(0xFFDDF4F3), Color(0xFF1F8A84)),
        Triple(RfIconKind.CHECKLIST, Color(0xFFEEF6D6), Color(0xFF4E6A10)),
        Triple(RfIconKind.SPARKLE, Color(0xFFFFF2C9), Color(0xFFB07D00)),
    )

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 30.dp),
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Antes del gran día",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFDE6F2))
                    .clickable { onSelectScreenCode("s40") }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3EEF6)),
                ) {
                    RfPhoto(photoKey = eventPhotoKey, contentDescription = event.title)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = event.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                    BasicText(
                        text = "${event.dateLabel} · ${event.venue}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFFC2338F),
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xCCFFFFFF)),
                        ) {
                            if (completedCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((completedCount.toFloat() / totalSteps).coerceIn(0f, 1f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(RosaButtonGradient),
                                )
                            }
                        }
                        BasicText(
                            text = "$completedCount de $totalSteps",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                checklist.forEachIndexed { stepIndex, stepItem ->
                    val isChecked = if (viewModel != null) {
                        stepItem.completed
                    } else {
                        fallbackToggledIds.contains(stepItem.id)
                    }
                    val (stepIcon, stepIconBg, stepIconTint) = iconStyles[stepIndex % iconStyles.size]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                            .clip(RoundedCornerShape(18.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
                            .clickable {
                                if (viewModel != null) {
                                    viewModel.toggleChecklistStep(stepItem.id)
                                } else {
                                    fallbackToggledIds = if (isChecked) {
                                        fallbackToggledIds - stepItem.id
                                    } else {
                                        fallbackToggledIds + stepItem.id
                                    }
                                }
                            }
                            .testTag("checklist-step-${stepItem.id}")
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChecklistBox46(checked = isChecked, ringColor = Color(0xFFCDBFCB))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(stepIconBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = stepIcon, tint = stepIconTint, size = 19.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = stepItem.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            BasicText(
                                text = stepItem.stageLabel,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 12.sp,
                                    color = RfThemeState.currentPalette.muted,
                                ),
                            )
                        }
                    }
                }

                val isBalanceSettled = remainingDop <= 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isBalanceSettled) Color(0xFFEEF6D6) else Color(0xFFFFF2C9))
                        .border(
                            1.dp,
                            if (isBalanceSettled) Color(0xFFCDE298) else Color(0xFFF0D58A),
                            RoundedCornerShape(20.dp),
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RfThemeState.currentPalette.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = RfIconKind.WALLET,
                            tint = if (isBalanceSettled) Color(0xFF4E6A10) else Color(0xFF7A5700),
                            size = 20.dp,
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = if (isBalanceSettled) "Evento saldado" else "Pago restante",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "RD$ $remainingDop · Pagado RD$ $paidDop de RD$ $totalEventDop",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (isBalanceSettled) Color(0xFF4E6A10) else Color(0xFF7A5700),
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(RosaButtonGradient)
                            .clickable {
                                if (isBalanceSettled) {
                                    onSelectScreenCode("s82")
                                } else {
                                    viewModel?.setPaymentCheckoutMode("BALANCE")
                                    onSelectScreenCode("s50")
                                }
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = if (isBalanceSettled) "Recibos" else "Pagar restante",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistBox46(checked: Boolean, ringColor: Color) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (checked) Color(0xFF8DB52C) else Color.Transparent)
            .border(2.dp, if (checked) Color(0xFF8DB52C) else ringColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
        }
    }
}

private data class GuestRowItem47(
    val id: String,
    val name: String,
    val details: String,
    val status: String,
)

@Composable
internal fun RenderGuestsScreenS47(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val event = viewModel?.activeClientEvent() ?: eventsState.first()
    val vmGuests = viewModel?.eventGuestsList?.collectAsState()?.value

    var fallbackGuests by remember {
        mutableStateOf(
            listOf(
                GuestRowItem47("gst-1", "Familia Pérez Martínez", "Confirmado · Mesa 2 · 4 personas", "Confirmado"),
                GuestRowItem47("gst-2", "Carlos & Andrea Sánchez", "Confirmado · Mesa 1 · 2 personas", "Confirmado"),
                GuestRowItem47("gst-3", "Dra. Laura Gómez", "Pendiente · Mesa 3 · 1 persona", "Pendiente"),
                GuestRowItem47("gst-4", "Lic. Roberto Fernández", "Confirmado · Mesa 4 · 2 personas", "Confirmado"),
            ),
        )
    }

    var showAddGuestModal by remember { mutableStateOf(false) }
    var guestNameInput by remember { mutableStateOf("") }
    var tableLabelInput by remember { mutableStateOf("Mesa 5") }
    var partySizeInput by remember { mutableStateOf("2") }
    var newGuestStatus by remember { mutableStateOf("Confirmado") }
    var guestFormError by remember { mutableStateOf<String?>(null) }

    val guestItems = vmGuests?.map { rowItem ->
        GuestRowItem47(
            id = rowItem.id,
            name = rowItem.name,
            details = rowItem.details,
            status = rowItem.status,
        )
    } ?: fallbackGuests

    val confirmedCount = guestItems.count { guestItem -> guestItem.status == "Confirmado" }
    val pendingCount = guestItems.count { guestItem -> guestItem.status == "Pendiente" }
    val declinedCount = guestItems.count { guestItem -> guestItem.status == "Rechazado" }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("BACK:s40") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Invitados",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                lineHeight = 22.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        BasicText(
                            text = "${guestItems.size} registrados · ${event.title}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RosaButtonGradient)
                            .clickable {
                                guestFormError = null
                                showAddGuestModal = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val isConfirmedFilter = selectedStatusFilter == "Confirmado"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFEEF6D6))
                            .then(
                                if (isConfirmedFilter) {
                                    Modifier.border(2.dp, Color(0xFF4E6A10), RoundedCornerShape(18.dp))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable {
                                selectedStatusFilter = if (isConfirmedFilter) "Todos" else "Confirmado"
                            }
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText(
                                text = "Confirmados",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted, fontSize = 12.sp),
                            )
                            BasicText(
                                text = "$confirmedCount",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10), fontSize = 24.sp),
                            )
                        }
                    }

                    val isPendingFilter = selectedStatusFilter == "Pendiente"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFFF2C9))
                            .then(
                                if (isPendingFilter) {
                                    Modifier.border(2.dp, Color(0xFF7A5700), RoundedCornerShape(18.dp))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable {
                                selectedStatusFilter = if (isPendingFilter) "Todos" else "Pendiente"
                            }
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText(
                                text = "Pendientes",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted, fontSize = 12.sp),
                            )
                            BasicText(
                                text = "$pendingCount",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700), fontSize = 24.sp),
                            )
                        }
                    }

                    val isDeclinedFilter = selectedStatusFilter == "Rechazado"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFDE6F2))
                            .then(
                                if (isDeclinedFilter) {
                                    Modifier.border(2.dp, Color(0xFFB42345), RoundedCornerShape(18.dp))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable {
                                selectedStatusFilter = if (isDeclinedFilter) "Todos" else "Rechazado"
                            }
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText(
                                text = "Rechazados",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted, fontSize = 12.sp),
                            )
                            BasicText(
                                text = "$declinedCount",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB42345), fontSize = 24.sp),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updatedText -> searchQuery = updatedText },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                BasicText(
                                    text = "Buscar invitado por nombre o mesa",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            innerTextField()
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDE6F2))
                        .border(1.dp, Color(0xFFF2C2DF), RoundedCornerShape(16.dp))
                        .clickable {
                            guestFormError = null
                            showAddGuestModal = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFFC2338F), size = 18.dp)
                        BasicText(
                            text = "Agregar invitado",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                        )
                    }
                }

                val filteredGuests = guestItems.filter { guestItem ->
                    val matchesStatus = if (selectedStatusFilter == "Todos") {
                        true
                    } else {
                        guestItem.status == selectedStatusFilter
                    }
                    val trimmedSearch = searchQuery.trim()
                    val matchesSearch = if (trimmedSearch.isEmpty()) {
                        true
                    } else {
                        "${guestItem.name} ${guestItem.details}".contains(trimmedSearch, ignoreCase = true)
                    }
                    if (matchesStatus) matchesSearch else false
                }

                if (filteredGuests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                text = "Sin invitados con filtro '$selectedStatusFilter'",
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                            )
                            if (selectedStatusFilter != "Todos") {
                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFFFDE6F2))
                                        .clickable { selectedStatusFilter = "Todos" }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = "Ver todos",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC2338F)),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredGuests.forEach { guestItem ->
                            val (badgeBg, badgeTextColor) = when (guestItem.status) {
                                "Confirmado" -> Color(0xFFEEF6D6) to Color(0xFF4E6A10)
                                "Rechazado" -> Color(0xFFFCE8E8) to Color(0xFFB42345)
                                else -> Color(0xFFFFF2C9) to Color(0xFF7A5700)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(RfThemeState.currentPalette.surface)
                                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                                    .clickable {
                                        if (viewModel != null) {
                                            viewModel.toggleEventGuestStatus(guestItem.id)
                                        } else {
                                            fallbackGuests = fallbackGuests.map { currentGuest ->
                                                if (currentGuest.id == guestItem.id) {
                                                    val nextStatus = when (currentGuest.status) {
                                                        "Confirmado" -> "Pendiente"
                                                        "Pendiente" -> "Rechazado"
                                                        else -> "Confirmado"
                                                    }
                                                    currentGuest.copy(status = nextStatus)
                                                } else {
                                                    currentGuest
                                                }
                                            }
                                        }
                                    }
                                    .padding(14.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        BasicText(
                                            text = guestItem.name,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, fontSize = 15.sp),
                                        )
                                        BasicText(
                                            text = guestItem.details,
                                            style = TextStyle(fontFamily = typo.bodyFamily, color = RfThemeState.currentPalette.muted, fontSize = 13.sp),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(CircleShape)
                                            .background(badgeBg)
                                            .padding(horizontal = 9.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = guestItem.status,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = badgeTextColor,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            if (showAddGuestModal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(12f)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { showAddGuestModal = false }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(24.dp))
                            .clickable(enabled = false) {}
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                text = "Registrar invitado",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .clickable { showAddGuestModal = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                            }
                        }

                        if (guestFormError != null) {
                            BasicText(
                                text = guestFormError.orEmpty(),
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB42345),
                                ),
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("Nombre o familia", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = guestNameInput,
                                    onValueChange = { updatedText ->
                                        guestNameInput = updatedText
                                        guestFormError = null
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (guestNameInput.isEmpty()) {
                                            BasicText("Ej. Familia Rosario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicText("Mesa", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    BasicTextField(
                                        value = tableLabelInput,
                                        onValueChange = { updatedText ->
                                            tableLabelInput = updatedText
                                            guestFormError = null
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicText("Personas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    BasicTextField(
                                        value = partySizeInput,
                                        onValueChange = { updatedText ->
                                            partySizeInput = updatedText.filter { charItem -> charItem.isDigit() }.take(2)
                                            guestFormError = null
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText("Estado RSVP", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Confirmado", "Pendiente", "Rechazado").forEach { statusOption ->
                                    val isChosenStatus = newGuestStatus == statusOption
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(if (isChosenStatus) Color(0xFFFDE6F2) else RfThemeState.currentPalette.chipBg)
                                            .clickable { newGuestStatus = statusOption },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = statusOption,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isChosenStatus) Color(0xFFC2338F) else RfThemeState.currentPalette.ink,
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = false)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RosaButtonGradient)
                                .clickable {
                                    val cleanGuestName = guestNameInput.trim()
                                    val cleanTable = tableLabelInput.trim().ifEmpty { "Mesa general" }
                                    val cleanPartySize = (partySizeInput.toIntOrNull() ?: 1).coerceAtLeast(1)
                                    if (cleanGuestName.isEmpty()) {
                                        guestFormError = "Escribe el nombre del invitado o familia."
                                    } else {
                                        if (viewModel != null) {
                                            viewModel.addEventGuest(
                                                guestName = cleanGuestName,
                                                tableNumberLabel = cleanTable,
                                                partySize = cleanPartySize,
                                                status = newGuestStatus,
                                            )
                                        } else {
                                            val nextNum = fallbackGuests.size + 1
                                            val peopleLabel = if (cleanPartySize == 1) "1 persona" else "$cleanPartySize personas"
                                            fallbackGuests = fallbackGuests + GuestRowItem47(
                                                id = "gst-$nextNum",
                                                name = cleanGuestName,
                                                details = "$newGuestStatus · $cleanTable · $peopleLabel",
                                                status = newGuestStatus,
                                            )
                                        }
                                        guestNameInput = ""
                                        guestFormError = null
                                        showAddGuestModal = false
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Guardar invitado",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderQuoteScreenS48(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val observedQuotes = viewModel?.quotes?.collectAsState()?.value
    val observedQuoteId = viewModel?.selectedQuoteId?.collectAsState()?.value
    val draftDiscountDop = viewModel?.draftDiscountDop?.collectAsState()?.value ?: 0
    val rawProfileName = viewModel?.profileName?.collectAsState()?.value.orEmpty().trim()
    val vmFormError = viewModel?.formError?.collectAsState()?.value

    val fallbackEvent = viewModel?.activeClientEvent()
        ?: events.firstOrNull { currentEvent -> currentEvent.id == selectedEventId }
        ?: events.first()

    val activeQuote: QuoteRecord? = remember(viewModel, observedQuotes, observedQuoteId, fallbackEvent.id) {
        viewModel?.selectedQuote()
            ?: observedQuotes?.firstOrNull { quoteItem -> quoteItem.eventId == fallbackEvent.id }
            ?: observedQuotes?.firstOrNull()
    }

    val quoteTitle = activeQuote?.eventTitle ?: fallbackEvent.title
    val quoteDateLabel = activeQuote?.dateLabel ?: fallbackEvent.dateLabel
    val quoteVenue = activeQuote?.venue ?: fallbackEvent.venue
    val quoteGuests = activeQuote?.guests ?: fallbackEvent.guests
    val quoteStatusLabel = activeQuote?.status?.ifBlank { "Lista para aprobar" } ?: "Lista para aprobar"

    val resolvedLines: List<EventLineItem> = remember(activeQuote, fallbackEvent) {
        val quoteLines = activeQuote?.lines.orEmpty()
        if (quoteLines.isNotEmpty()) {
            quoteLines
        } else if (fallbackEvent.items.isNotEmpty()) {
            fallbackEvent.items
        } else {
            val baseAmount = (fallbackEvent.totalDop - 350).coerceAtLeast(0)
            listOf(
                EventLineItem(
                    articleCode = "RF-BASE",
                    name = "Mobiliario y ambientación",
                    quantity = 1,
                    unitPriceDop = baseAmount,
                    photo = fallbackEvent.photo,
                ),
            )
        }
    }

    val subtotalDop = resolvedLines.sumOf { lineItem -> lineItem.subtotalDop }
    val deliveryFeeDop = activeQuote?.deliveryFeeDop ?: 350
    val discountDop = activeQuote?.discountDop ?: draftDiscountDop
    val totalDop = (subtotalDop + deliveryFeeDop - discountDop).coerceAtLeast(0)

    val initialSigner = remember(activeQuote?.id, activeQuote?.signedByName, rawProfileName) {
        val existingSignedBy = activeQuote?.signedByName.orEmpty().trim()
        val isPreviewGuest = rawProfileName.equals("Invitado Rosa Fiesta", ignoreCase = true)
        val isShortGuest = rawProfileName.equals("Invitado", ignoreCase = true)
        val hideProfileDefault = if (isPreviewGuest) true else isShortGuest
        when {
            existingSignedBy.isNotEmpty() -> existingSignedBy
            hideProfileDefault -> ""
            else -> rawProfileName
        }
    }
    var signerName by remember(initialSigner) { mutableStateOf(initialSigner) }
    var isSignatureAccepted by remember(activeQuote?.id) {
        mutableStateOf(activeQuote?.signatureSvg?.isNotBlank() == true)
    }
    var localContractError by remember { mutableStateOf<String?>(null) }
    val visibleContractError = localContractError ?: vmFormError

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 148.dp),
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Tu cotización",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                    modifier = Modifier.weight(1f),
                )

                val (statusChipBg, statusChipColor) = when {
                    quoteStatusLabel.contains("Aprob", ignoreCase = true) -> Color(0xFFEEF6D6) to Color(0xFF4E6A10)
                    quoteStatusLabel.contains("Rechaz", ignoreCase = true) -> Color(0xFFFCE8E8) to Color(0xFFB42345)
                    else -> Color(0xFFDDF4F3) to Color(0xFF1F8A84)
                }
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(statusChipBg)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = quoteStatusLabel,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = statusChipColor, fontSize = 12.sp),
                    )
                }
            }

            if (!visibleContractError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFCE8E8))
                        .border(1.dp, Color(0xFFF5B6C4), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = visibleContractError,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFFB42345),
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp)),
            ) {
                Column {
                    Column(
                        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = quoteTitle,
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                            BasicText(quoteDateLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                            BasicText(quoteVenue, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.USER, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                            BasicText("$quoteGuests invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 18.dp)) {
                        drawLine(
                            color = RfThemeState.currentPalette.line,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2f * density,
                        )
                    }

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        resolvedLines.forEach { lineItem ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = lineItem.name,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                    )
                                    BasicText(
                                        text = "${lineItem.quantity} unidades · RD$ ${lineItem.unitPriceDop} c/u",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                                BasicText(
                                    text = "RD$ ${lineItem.subtotalDop}",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Subtotal", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                            BasicText("RD$ $subtotalDop", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Transporte e instalación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                            BasicText("RD$ $deliveryFeeDop", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                        }

                        if (discountDop > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BasicText("Descuento aplicado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF4E6A10)))
                                BasicText("- RD$ $discountDop", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF4E6A10)))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Total", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink))
                            BasicText("RD$ $totalDop", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = RfThemeState.currentPalette.ink))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    text = "Firma digital del contrato",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = signerName,
                        onValueChange = { updatedText ->
                            signerName = updatedText
                            localContractError = null
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (signerName.isEmpty()) {
                                BasicText(
                                    text = "Escribe tu nombre completo para firmar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            innerTextField()
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSignatureAccepted) Color(0xFFFDF5FA) else RfThemeState.currentPalette.chipBg)
                        .border(
                            1.5.dp,
                            if (isSignatureAccepted) Color(0xFFC2338F) else RfThemeState.currentPalette.line,
                            RoundedCornerShape(16.dp),
                        )
                        .clickable {
                            isSignatureAccepted = !isSignatureAccepted
                            localContractError = null
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ChecklistBox46(checked = isSignatureAccepted, ringColor = Color(0xFFCDBFCB))
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = if (isSignatureAccepted) "Firma digital aplicada" else "Aceptar y firmar contrato digital",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RfThemeState.currentPalette.ink),
                            )
                            BasicText(
                                text = "Anticipo de reserva: 30% (RD$ ${(totalDop * 30) / 100})",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                        if (isSignatureAccepted) {
                            Canvas(modifier = Modifier.width(68.dp).height(34.dp)) {
                                val sigPath = Path().apply {
                                    moveTo(size.width * 0.08f, size.height * 0.72f)
                                    cubicTo(
                                        size.width * 0.28f,
                                        size.height * 0.14f,
                                        size.width * 0.55f,
                                        size.height * 0.85f,
                                        size.width * 0.92f,
                                        size.height * 0.30f,
                                    )
                                }
                                drawPath(sigPath, color = Color(0xFFC2338F), style = Stroke(width = 2.5f * density))
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF2C9))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("M", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF7A5700)))
                    }
                    Column {
                        BasicText("Nota de María · Rosa Fiesta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7A5700)))
                        BasicText("Incluye transporte, montaje puntual y revisión final en salón.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = RfThemeState.currentPalette.ink))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(RfThemeState.currentPalette.bg.copy(alpha = 0.95f))
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable {
                        val cleanSigner = signerName.trim()
                        if (cleanSigner.length < 3) {
                            val missingSignerMsg = "Escribe tu nombre completo para firmar el contrato."
                            localContractError = missingSignerMsg
                            viewModel?.postError(missingSignerMsg)
                        } else if (!isSignatureAccepted) {
                            val missingCheckMsg = "Marca la firma digital del contrato antes de aprobar."
                            localContractError = missingCheckMsg
                            viewModel?.postError(missingCheckMsg)
                        } else {
                            val generatedSignatureSvg = "<svg viewBox='0 0 180 48'><path d='M12 34 Q52 8 94 28 T168 20' stroke='#C2338F' stroke-width='3' fill='none'/></svg>"
                            if (viewModel != null) {
                                val resolvedQuoteId = activeQuote?.id
                                    ?: viewModel.syncDraftToOfficialQuote()?.id
                                    ?: viewModel.selectedQuoteId.value
                                val approvedRecord = viewModel.approveQuoteById(
                                    quoteId = resolvedQuoteId,
                                    signedByName = cleanSigner,
                                    signatureSvg = generatedSignatureSvg,
                                )
                                if (approvedRecord != null) {
                                    localContractError = null
                                    viewModel.setPaymentCheckoutMode("DEPOSIT")
                                    onSelectScreenCode("s50")
                                } else {
                                    localContractError = viewModel.formError.value ?: "No se pudo firmar la cotización."
                                }
                            } else {
                                localContractError = null
                                onSelectScreenCode("s50")
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Aprobar cotización",
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
                    .height(44.dp)
                    .clickable {
                        if (activeQuote != null) {
                            viewModel?.declineQuoteById(activeQuote.id, "Solicita ajuste")
                        }
                        onSelectScreenCode("s42")
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Rechazar / Solicitar ajuste",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFB42345),
                    ),
                )
            }
        }
    }
}
