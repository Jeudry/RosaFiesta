package com.jeudry.rosafiesta.ui

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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.EventPackage
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.StaffMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

private val AdminBackgroundGradient = Brush.radialGradient(
    0.0f to Color(0xFFE6E7FF),
    0.6f to Color(0x00E6E7FF),
    center = Offset(400f, 0f),
    radius = 450f,
)

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

private enum class AdminQuoteSourceKind {
    NONE,
    QUOTE,
    LEAD,
}

private data class AdminQuoteDraftState(
    val sourceKey: String = "",
    val sourceKind: AdminQuoteSourceKind = AdminQuoteSourceKind.NONE,
    val clientName: String = "",
    val clientEmail: String = "",
    val clientPhone: String = "",
    val eventTitle: String = "",
    val dateLabel: String = "",
    val guestsCount: Int = 40,
    val venue: String = "",
    val lines: List<EventLineItem> = emptyList(),
    val deliveryFeeText: String = "350",
    val discountText: String = "0",
    val notes: String = "Incluye montaje 2 horas antes y coordinación logística en salón.",
)

private val adminQuoteDraftFlow = MutableStateFlow(AdminQuoteDraftState())

private fun bothConditions(first: Boolean, second: Boolean): Boolean = if (first) second else false

private fun eitherCondition(first: Boolean, second: Boolean): Boolean = if (first) true else second

private fun photoKeyForAsset(photo: PhotoAsset): RfPhotoKey = when (photo) {
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

private fun photoKeyForEventType(eventType: String): RfPhotoKey {
    val normalized = eventType.lowercase()
    return when {
        normalized.contains("boda") -> RfPhotoKey.BODA
        normalized.contains("quince") -> RfPhotoKey.QUINCEANERA
        normalized.contains("baby") -> RfPhotoKey.BABY_SHOWER
        normalized.contains("grad") -> RfPhotoKey.GRADUACION
        normalized.contains("corp") -> RfPhotoKey.CORPORATIVO
        else -> RfPhotoKey.CUMPLE_INFANTIL
    }
}

private fun isExpiredRequestStatus(rawStatus: String): Boolean {
    val normalized = rawStatus.trim().uppercase()
    val isEnglishExpired = normalized == "EXPIRED"
    val isSpanishExpired = normalized == "VENCIDA"
    return eitherCondition(isEnglishExpired, isSpanishExpired)
}

private fun isQuotedRequestStatus(rawStatus: String): Boolean {
    val normalized = rawStatus.trim().uppercase()
    val isSentOrQuoted = eitherCondition(normalized == "SENT", normalized == "QUOTED")
    val isSpanishQuoted = eitherCondition(normalized == "COTIZADA", normalized.contains("COTIZACI"))
    val isApproved = normalized == "APPROVED"
    val matchesSentGroup = eitherCondition(isSentOrQuoted, isSpanishQuoted)
    return eitherCondition(matchesSentGroup, isApproved)
}

private fun buildStarterLinesForLead(
    lead: LeadRecord,
    catalog: List<CatalogArticle>,
): List<EventLineItem> {
    val chairArticle = catalog.firstOrNull { articleItem -> articleItem.id == "art-silla-tiffany" }
        ?: CanonicalSampleData.articles.first()
    val tableArticle = catalog.firstOrNull { articleItem -> articleItem.id == "art-mesa-redonda" }
        ?: CanonicalSampleData.articles[2]
    val archArticle = catalog.firstOrNull { articleItem -> articleItem.id == "art-arco-rosa" }
        ?: CanonicalSampleData.articles[4]
    val guestChairs = lead.guests.coerceAtLeast(20)
    val tableCount = ((guestChairs + 7) / 8).coerceAtLeast(2)
    return listOf(
        EventLineItem(
            articleCode = chairArticle.code,
            name = chairArticle.name,
            quantity = guestChairs,
            unitPriceDop = chairArticle.pricePerDayDop,
            photo = chairArticle.photo,
        ),
        EventLineItem(
            articleCode = tableArticle.code,
            name = tableArticle.name,
            quantity = tableCount,
            unitPriceDop = tableArticle.pricePerDayDop,
            photo = tableArticle.photo,
        ),
        EventLineItem(
            articleCode = archArticle.code,
            name = archArticle.name,
            quantity = 1,
            unitPriceDop = archArticle.pricePerDayDop,
            photo = archArticle.photo,
        ),
    )
}

private fun primeQuoteDraftFromQuote(
    quote: QuoteRecord,
    fallbackVenue: String,
    phone: String = "",
) {
    val resolvedVenue = quote.venue.trim().ifBlank { fallbackVenue }
    adminQuoteDraftFlow.value = AdminQuoteDraftState(
        sourceKey = "QUOTE:${quote.id}",
        sourceKind = AdminQuoteSourceKind.QUOTE,
        clientName = quote.clientName,
        clientEmail = quote.clientEmail,
        clientPhone = phone,
        eventTitle = quote.eventTitle,
        dateLabel = quote.dateLabel,
        guestsCount = quote.guests,
        venue = resolvedVenue,
        lines = quote.lines,
        deliveryFeeText = quote.deliveryFeeDop.toString(),
        discountText = quote.discountDop.toString(),
        notes = "Incluye montaje 2 horas antes en $resolvedVenue.",
    )
}

private fun primeQuoteDraftFromLead(
    lead: LeadRecord,
    fallbackVenue: String,
    catalog: List<CatalogArticle>,
) {
    val starterLines = buildStarterLinesForLead(lead, catalog)
    val noteText = lead.followUpNote.trim().ifBlank {
        "Propuesta comercial para ${lead.eventType} (${lead.guests} invitados)."
    }
    adminQuoteDraftFlow.value = AdminQuoteDraftState(
        sourceKey = "LEAD:${lead.id}",
        sourceKind = AdminQuoteSourceKind.LEAD,
        clientName = lead.clientName,
        clientEmail = lead.email,
        clientPhone = lead.phone,
        eventTitle = "${lead.eventType} · ${lead.clientName.substringBefore(" ")}",
        dateLabel = lead.dateLabel,
        guestsCount = lead.guests,
        venue = fallbackVenue,
        lines = starterLines,
        deliveryFeeText = "350",
        discountText = "0",
        notes = noteText,
    )
}

private fun appendCatalogArticleToAdminQuoteDraft(article: CatalogArticle) {
    adminQuoteDraftFlow.update { currentDraft ->
        val existingIndex = currentDraft.lines.indexOfFirst { lineItem ->
            eitherCondition(
                lineItem.articleCode.equals(article.code, ignoreCase = true),
                lineItem.name.equals(article.name, ignoreCase = true),
            )
        }
        val nextLines = if (existingIndex >= 0) {
            currentDraft.lines.mapIndexed { lineIndex, lineItem ->
                if (lineIndex == existingIndex) {
                    lineItem.copy(quantity = lineItem.quantity + 1)
                } else {
                    lineItem
                }
            }
        } else {
            currentDraft.lines + EventLineItem(
                articleCode = article.code,
                name = article.name,
                quantity = 1,
                unitPriceDop = article.pricePerDayDop,
                photo = article.photo,
            )
        }
        currentDraft.copy(lines = nextLines)
    }
}

// ==============================================================================
// A01 · INICIO (ADMIN DASHBOARD MÓVIL)
// ==============================================================================
@Composable
private fun AdminHeroWeddingRingsIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        scale(s, s, Offset.Zero) {
            // Circle 1: cx="49" cy="70" r="21" stroke="#D9A441" stroke-width="6.5"
            drawCircle(
                color = Color(0xFFD9A441),
                radius = 21f,
                center = Offset(49f, 70f),
                style = Stroke(width = 6.5f),
            )
            // Circle 2: cx="72" cy="72" r="21" stroke="#F0CC7A" stroke-width="6.5"
            drawCircle(
                color = Color(0xFFF0CC7A),
                radius = 21f,
                center = Offset(72f, 72f),
                style = Stroke(width = 6.5f),
            )
            // Overlapping arc: M63.5 52.6A21 21 0 0 1 69 62
            val arcPath = PathParser().parsePathString("M63.5 52.6A21 21 0 0 1 69 62").toPath()
            drawPath(arcPath, color = Color(0xFFD9A441), style = Stroke(width = 6.5f, cap = StrokeCap.Round))

            // Diamond on ring: M42 44L49 37L56 44L49 53Z
            val diamondPath = PathParser().parsePathString("M42 44L49 37L56 44L49 53Z").toPath()
            drawPath(diamondPath, color = Color(0xFFA8D8F5))
            drawPath(diamondPath, color = Color.White, style = Stroke(width = 1.6f, join = StrokeJoin.Round))

            // Star 1: M90 30l2 5 5 2-5 2-2 5-2-5-5-2 5-2z
            val star1 = PathParser().parsePathString("M90 30l2 5 5 2-5 2-2 5-2-5-5-2 5-2z").toPath()
            drawPath(star1, color = Color(0xFFEC4F97))

            // Star 2: M27 34l1.4 3.6 3.6 1.4-3.6 1.4-1.4 3.6-1.4-3.6-3.6-1.4 3.6-1.4z
            val star2 = PathParser().parsePathString("M27 34l1.4 3.6 3.6 1.4-3.6 1.4-1.4 3.6-1.4-3.6-3.6-1.4 3.6-1.4z").toPath()
            drawPath(star2, color = Color(0xFF7650E8))

            // Sparkle: M96 96c-2-3-6-2-6 1 0 3 6 6 6 6s6-3 6-6c0-3-4-4-6-1z
            val sparkle = PathParser().parsePathString("M96 96c-2-3-6-2-6 1 0 3 6 6 6 6s6-3 6-6c0-3-4-4-6-1z").toPath()
            drawPath(sparkle, color = Color(0xFFF9B6D3))
        }
    }
}

private data class AdminHomeAlertRow(
    val title: String,
    val subtitle: String,
    val trailingBadge: String,
    val isBadgePill: Boolean,
    val iconKind: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
    val onTap: () -> Unit,
)

private fun resolveAdminFirstName(rawProfileName: String): String {
    val trimmed = rawProfileName.trim()
    val isBlankName = trimmed.isEmpty()
    val isGuestLabel = trimmed.startsWith("Invitado", ignoreCase = true)
    val shouldFallbackToMaria = eitherCondition(isBlankName, isGuestLabel)
    if (shouldFallbackToMaria) {
        return "María"
    }
    return trimmed.substringBefore(" ")
}

private fun buildHeroTimingBadge(eventItem: ClientEvent): String {
    val readableDate = formatReadableSpanishDateText(eventItem.dateLabel)
    return when {
        eventItem.daysUntilEvent == 0 -> "Hoy · $readableDate"
        eventItem.daysUntilEvent == 1 -> "Mañana · $readableDate"
        eventItem.daysUntilEvent > 1 -> "$readableDate · en ${eventItem.daysUntilEvent} días"
        else -> readableDate
    }
}

@Composable
internal fun RenderAdminHomeA01(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val rawProfileName = viewModel?.profileName?.collectAsState()?.value ?: "María"
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
    val allPayments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val allQuotes = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val allLeads = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val allNotifications = viewModel?.notifications?.collectAsState()?.value ?: emptyList()
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings

    val adminFirstName = resolveAdminFirstName(rawProfileName)
    val unreadNotificationsCount = if (viewModel != null) {
        allNotifications.count { notificationItem -> !notificationItem.isRead }
    } else {
        3
    }

    val activeEvents = allEvents.filter { eventItem ->
        val notCompleted = eventItem.status != EventStatus.COMPLETED
        val notCancelled = eventItem.status != EventStatus.CANCELLED
        bothConditions(notCompleted, notCancelled)
    }
    val heroEvent = activeEvents.firstOrNull() ?: allEvents.firstOrNull()

    val collectedPaymentsDop = allPayments.sumOf { paymentItem -> paymentItem.amountDop }
    val totalRevenueDop = if (collectedPaymentsDop > 0) {
        collectedPaymentsDop
    } else {
        metrics.octoberRevenueDop
    }
    val activeEventsCount = if (allEvents.isNotEmpty()) {
        activeEvents.size
    } else {
        metrics.activeEventsCount
    }

    val pendingQuotes = allQuotes.filter { quoteItem ->
        quoteItem.status.equals("PENDING", ignoreCase = true)
    }
    val unquotedLeads = allLeads.filter { leadItem ->
        val notQuoted = !isQuotedRequestStatus(leadItem.stage)
        val notExpired = !isExpiredRequestStatus(leadItem.stage)
        bothConditions(notQuoted, notExpired)
    }
    val quotedClientNames = pendingQuotes.map { quoteItem -> quoteItem.clientName.lowercase() }.toSet()
    val distinctNewLeads = unquotedLeads.filter { leadItem ->
        leadItem.clientName.lowercase() !in quotedClientNames
    }
    val pendingRequestsCount = pendingQuotes.size + distinctNewLeads.size
    val fallbackVenue = businessSettings.zones.firstOrNull()?.name ?: "Jardín Los Rosales"

    val alertRows = remember(allEvents, pendingQuotes, distinctNewLeads, allArticles, fallbackVenue) {
        val builtRows = mutableListOf<AdminHomeAlertRow>()
        val unpaidEvents = allEvents.filter { eventItem -> eventItem.balanceDop > 0 }
        val topUnpaidEvent = unpaidEvents.firstOrNull()
        if (topUnpaidEvent != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Pago restante · ${topUnpaidEvent.title}",
                    subtitle = "Vence ${topUnpaidEvent.balanceDueDateLabel} · ${topUnpaidEvent.clientName}",
                    trailingBadge = formatAdminDop(topUnpaidEvent.balanceDop),
                    isBadgePill = false,
                    iconKind = RfIconKind.WALLET,
                    iconBg = Color(0xFFFFF2C9),
                    iconTint = Color(0xFF7A5700),
                    onTap = {
                        viewModel?.selectEvent(topUnpaidEvent.id)
                        onSelectScreenCode("A21")
                    },
                ),
            )
        }
        val topPendingQuote = pendingQuotes.firstOrNull()
        if (topPendingQuote != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Cotización · ${topPendingQuote.eventTitle}",
                    subtitle = "${topPendingQuote.clientName} · ${formatReadableSpanishDateText(topPendingQuote.dateLabel)}",
                    trailingBadge = formatAdminDop(topPendingQuote.totalDop),
                    isBadgePill = false,
                    iconKind = RfIconKind.ORDERS,
                    iconBg = Color(0xFFECEBFF),
                    iconTint = Color(0xFF4B47D6),
                    onTap = {
                        primeQuoteDraftFromQuote(topPendingQuote, fallbackVenue)
                        viewModel?.selectQuote(topPendingQuote.id)
                        onSelectScreenCode("A03")
                    },
                ),
            )
        }
        val topNewLead = distinctNewLeads.firstOrNull()
        if (topNewLead != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Solicitud nueva · ${topNewLead.clientName}",
                    subtitle = "${topNewLead.eventType} · ${formatReadableSpanishDateText(topNewLead.dateLabel)} · ${topNewLead.channel}",
                    trailingBadge = "Cotizar",
                    isBadgePill = true,
                    iconKind = RfIconKind.SPARK,
                    iconBg = Color(0xFFFDE6F2),
                    iconTint = Color(0xFFC2338F),
                    onTap = {
                        primeQuoteDraftFromLead(topNewLead, fallbackVenue, allArticles)
                        viewModel?.selectLead(topNewLead.id)
                        onSelectScreenCode("A03")
                    },
                ),
            )
        }
        val secondUnpaidEvent = unpaidEvents.drop(1).firstOrNull()
        if (secondUnpaidEvent != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Expediente · ${secondUnpaidEvent.title}",
                    subtitle = "${secondUnpaidEvent.clientName} · ${formatReadableSpanishDateText(secondUnpaidEvent.dateLabel)}",
                    trailingBadge = formatAdminDop(secondUnpaidEvent.balanceDop),
                    isBadgePill = false,
                    iconKind = RfIconKind.CALENDAR,
                    iconBg = Color(0xFFEEF6D6),
                    iconTint = Color(0xFF4E6A10),
                    onTap = {
                        viewModel?.selectEvent(secondUnpaidEvent.id)
                        onSelectScreenCode("A13")
                    },
                ),
            )
        }
        builtRows.take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0x24EA4EA9), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                RfLogo(size = RfLogoSize.MEDIUM)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BasicText(
                        text = "Rosa Fiesta",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .background(Color(0xFFECEBFF), CircleShape)
                            .padding(horizontal = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Admin",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
                BasicText(
                    text = "Hola, $adminFirstName",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729), lineHeight = 24.sp),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onSelectScreenCode("A29") },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BELL, tint = Color(0xFF151729), size = 22.dp)
                }
                if (unreadNotificationsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .defaultMinSize(minWidth = 20.dp)
                            .height(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD45C))
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "$unreadNotificationsCount",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
                        )
                    }
                }
            }
        }

        val heroTitle = heroEvent?.title ?: "Boda Real Rosa & Dorado"
        val heroBadge = if (heroEvent != null) buildHeroTimingBadge(heroEvent) else "Este viernes · en 3 días"
        val heroSubtitle = if (heroEvent != null) {
            "${heroEvent.clientName} · ${heroEvent.venue}"
        } else {
            "Montaje 2:00 p. m. · Jardín Botánico"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = true)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        0.0f to Color(0xFF7C5CF0),
                        0.55f to Color(0xFF5B6CF0),
                        1.0f to Color(0xFF2FB8C8),
                    ),
                )
                .clickable {
                    val targetHeroId = heroEvent?.id
                    if (targetHeroId != null) {
                        viewModel?.selectEvent(targetHeroId)
                    }
                    onSelectScreenCode("A13")
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scaleFactor = size.width / 350f
                scale(scaleFactor, scaleFactor, Offset.Zero) {
                    drawCircle(color = Color.White.copy(alpha = 0.1f), radius = 70f, center = Offset(300f, 20f))
                    drawCircle(color = Color(0xFFD6EA8C), radius = 4f, center = Offset(190f, 52f))
                    val starPath = PathParser().parsePathString("M0 -10L2.35 -3.24L9.5 -3.1L3.8 1.24L5.9 8.1L0 4L-5.9 8.1L-3.8 1.24L-9.5 -3.1L-2.35 -3.24Z").toPath()
                    translate(214f, 30f) {
                        scale(0.8f, 0.8f, Offset.Zero) {
                            drawPath(starPath, color = Color(0xFFFFD45C))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 16.dp, y = 18.dp)
                    .size(140.dp),
            ) {
                AdminHeroWeddingRingsIllustration(modifier = Modifier.fillMaxSize())
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = heroBadge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A3464)),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                BasicText(
                    text = heroTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 210.dp),
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 23.sp),
                )

                Spacer(modifier = Modifier.height(2.dp))

                BasicText(
                    text = heroSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFFE9EEFF)),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable { onSelectScreenCode("A20") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Ingresos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
                    BasicText(
                        text = formatAdminDop(totalRevenueDop),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E3048)),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable { onSelectScreenCode("A11") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Eventos activos", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
                    BasicText("$activeEventsCount", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E3048)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF2C9))
                    .clickable { onSelectScreenCode("A02") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Solicitudes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7A5700)))
                    BasicText("$pendingRequestsCount nuevas", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                }
            }
        }

        BasicText(
            text = "CREAR",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth().offset(y = (-6).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE9E8FF), Color(0xFFDDF2F6))))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A08") }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 36.dp, forceAdminPalette = true)
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Crear con Rosa IA", maxLines = 1, softWrap = false, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("Arma el borrador", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A05") }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFECEBFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF4B47D6), size = 20.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Crear manual", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A), lineHeight = 17.sp))
                            BasicText("Paso a paso", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.startEditingArticle(null)
                            onSelectScreenCode("A10")
                        }
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF4E6A10), size = 20.dp)
                        }
                        BasicText("Agregar\nartículo", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A), lineHeight = 17.sp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A02") }
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF7A5700), size = 20.dp)
                        }
                        BasicText(
                            text = "Solicitudes",
                            modifier = Modifier.weight(1f, fill = false),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                            maxLines = 1,
                            softWrap = false,
                        )
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 22.dp)
                                .height(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("$pendingRequestsCount", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)))
                        }
                    }
                }
            }
        }

        BasicText(
            text = "POR ATENDER",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-6).dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (alertRows.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 20.dp)
                        BasicText(
                            text = "Todo al día. No hay alertas pendientes.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                        )
                    }
                } else {
                    alertRows.forEachIndexed { alertIndex, alertItem ->
                        if (alertIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { alertItem.onTap() }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(alertItem.iconBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = alertItem.iconKind, tint = alertItem.iconTint, size = 20.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = alertItem.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                )
                                BasicText(
                                    text = alertItem.subtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                                )
                            }
                            if (alertItem.isBadgePill) {
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(alertItem.iconBg)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = alertItem.trailingBadge,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = alertItem.iconTint),
                                    )
                                }
                            } else {
                                BasicText(
                                    text = alertItem.trailingBadge,
                                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AdminRequestCardModel(
    val key: String,
    val title: String,
    val clientName: String,
    val dateLabel: String,
    val guests: Int,
    val articlesCountLabel: String,
    val estimatedDop: Int,
    val channelBadge: String,
    val rawStatus: String,
    val bucket: String,
    val phone: String,
    val photoKey: RfPhotoKey,
    val quoteRecord: QuoteRecord?,
    val leadRecord: LeadRecord?,
)

@Composable
internal fun RenderAdminRequestsA02(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val articlesList = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings
    val fallbackVenue = businessSettings.zones.firstOrNull()?.name ?: "Jardín Los Rosales"

    val requestCards = remember(quotesList, leadsList) {
        val unifiedList = mutableListOf<AdminRequestCardModel>()
        val coveredClients = mutableSetOf<String>()

        for (quoteItem in quotesList) {
            val matchedLead = leadsList.firstOrNull { leadItem ->
                leadItem.clientName.equals(quoteItem.clientName, ignoreCase = true)
            }
            coveredClients.add(quoteItem.clientName.trim().lowercase())
            val isLeadQuotedStage = if (matchedLead != null) {
                isQuotedRequestStatus(matchedLead.stage)
            } else {
                false
            }
            val effectiveRawStatus = if (isExpiredRequestStatus(quoteItem.status)) {
                quoteItem.status
            } else if (isLeadQuotedStage) {
                matchedLead?.stage ?: quoteItem.status
            } else {
                quoteItem.status
            }
            val bucket = when {
                isExpiredRequestStatus(effectiveRawStatus) -> "EXPIRED"
                isQuotedRequestStatus(effectiveRawStatus) -> "QUOTED"
                else -> "NEW"
            }
            val firstLinePhoto = quoteItem.lines.firstOrNull()?.photo
            val resolvedPhoto = if (firstLinePhoto != null) {
                photoKeyForAsset(firstLinePhoto)
            } else {
                photoKeyForEventType(quoteItem.eventTitle)
            }
            val channelText = matchedLead?.channel?.let { channelName -> "Vía $channelName" } ?: "Con Rosa IA"
            unifiedList.add(
                AdminRequestCardModel(
                    key = "quote-${quoteItem.id}",
                    title = quoteItem.eventTitle,
                    clientName = quoteItem.clientName,
                    dateLabel = quoteItem.dateLabel,
                    guests = quoteItem.guests,
                    articlesCountLabel = "${quoteItem.lines.size} artículos",
                    estimatedDop = quoteItem.totalDop,
                    channelBadge = channelText,
                    rawStatus = effectiveRawStatus,
                    bucket = bucket,
                    phone = matchedLead?.phone.orEmpty(),
                    photoKey = resolvedPhoto,
                    quoteRecord = quoteItem,
                    leadRecord = matchedLead,
                ),
            )
        }

        for (leadItem in leadsList) {
            val normalizedClient = leadItem.clientName.trim().lowercase()
            if (normalizedClient !in coveredClients) {
                val bucket = when {
                    isExpiredRequestStatus(leadItem.stage) -> "EXPIRED"
                    isQuotedRequestStatus(leadItem.stage) -> "QUOTED"
                    else -> "NEW"
                }
                unifiedList.add(
                    AdminRequestCardModel(
                        key = "lead-${leadItem.id}",
                        title = "${leadItem.eventType} · ${leadItem.clientName.substringBefore(" ")}",
                        clientName = leadItem.clientName,
                        dateLabel = leadItem.dateLabel,
                        guests = leadItem.guests,
                        articlesCountLabel = leadItem.stage,
                        estimatedDop = leadItem.estimatedDop,
                        channelBadge = "Vía ${leadItem.channel}",
                        rawStatus = leadItem.stage,
                        bucket = bucket,
                        phone = leadItem.phone,
                        photoKey = photoKeyForEventType(leadItem.eventType),
                        quoteRecord = null,
                        leadRecord = leadItem,
                    ),
                )
            }
        }
        unifiedList
    }

    val totalCount = requestCards.size
    val newCount = requestCards.count { cardItem -> cardItem.bucket == "NEW" }
    val quotedCount = requestCards.count { cardItem -> cardItem.bucket == "QUOTED" }
    val expiredCount = requestCards.count { cardItem -> isExpiredRequestStatus(cardItem.rawStatus) }

    var selectedRequestTab by remember { mutableStateOf(0) }
    val requestTabs = listOf(
        "Todas ($totalCount)",
        "Nuevas ($newCount)",
        "Cotizadas ($quotedCount)",
        "Vencidas ($expiredCount)",
    )

    val filteredCards = when (selectedRequestTab) {
        1 -> requestCards.filter { cardItem -> cardItem.bucket == "NEW" }
        2 -> requestCards.filter { cardItem -> cardItem.bucket == "QUOTED" }
        3 -> requestCards.filter { cardItem -> isExpiredRequestStatus(cardItem.rawStatus) }
        else -> requestCards
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
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

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Solicitudes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                )
                BasicText(
                    text = "Cotizaciones y prospectos entrantes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(Color(0xFFEDEDF5), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            requestTabs.forEachIndexed { tabIndex, tabTitle ->
                val isSelectedTab = selectedRequestTab == tabIndex
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .then(
                            if (isSelectedTab) {
                                Modifier
                                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4), ambientColor = Color.Transparent)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                            } else {
                                Modifier.clip(RoundedCornerShape(12.dp))
                            },
                        )
                        .clickable { selectedRequestTab = tabIndex }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = tabTitle,
                        maxLines = 1,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isSelectedTab) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelectedTab) Color(0xFF2E324A) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4B47D6), size = 24.dp)
                    BasicText(
                        text = if (selectedRequestTab == 3) "No hay solicitudes vencidas" else "Sin solicitudes en este filtro",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Todas las cotizaciones activas están vigentes.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377), textAlign = TextAlign.Center),
                    )
                }
            }
        } else {
            filteredCards.forEach { requestItem ->
                val openQuoteBuilder: () -> Unit = {
                    val linkedQuote = requestItem.quoteRecord
                    val linkedLead = requestItem.leadRecord
                    if (linkedQuote != null) {
                        primeQuoteDraftFromQuote(linkedQuote, fallbackVenue, requestItem.phone)
                        viewModel?.selectQuote(linkedQuote.id)
                    } else if (linkedLead != null) {
                        primeQuoteDraftFromLead(linkedLead, fallbackVenue, articlesList)
                        viewModel?.selectLead(linkedLead.id)
                    }
                    onSelectScreenCode("A03")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { openQuoteBuilder() },
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                            ) {
                                RfPhoto(photoKey = requestItem.photoKey, contentDescription = requestItem.title, modifier = Modifier.fillMaxSize())
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                BasicText(
                                    text = requestItem.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                )
                                BasicText(
                                    text = "${requestItem.clientName} · ${formatReadableSpanishDateText(requestItem.dateLabel)}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .background(Color(0xFFFDE6F2), CircleShape)
                                            .padding(horizontal = 9.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = requestItem.channelBadge,
                                            maxLines = 1,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFC2338F)),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .background(Color(0xFFF2F2F8), CircleShape)
                                            .padding(horizontal = 9.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = "${requestItem.guests} invitados",
                                            maxLines = 1,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                                        )
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = requestItem.articlesCountLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    BasicText("est. ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                    BasicText(
                                        text = formatAdminDop(requestItem.estimatedDop),
                                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF6D6))
                                    .border(1.dp, Color(0xFFE0EDBF), CircleShape)
                                    .clickable {
                                        viewModel?.selectChatThreadForContact(requestItem.clientName)
                                        if (requestItem.phone.isNotBlank()) {
                                            viewModel?.openWhatsAppChat(
                                                rawPhone = requestItem.phone,
                                                message = "Hola ${requestItem.clientName}, revisamos tu solicitud de ${requestItem.title}.",
                                            )
                                        }
                                        onSelectScreenCode("A15")
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                            }

                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                    .clickable { openQuoteBuilder() }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    BasicText("Cotizar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 16.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RfIcon(kind = RfIconKind.INFO, tint = Color(0xFF5F6377), size = 16.dp)
            BasicText(
                text = "Las solicitudes llegan desde Rosa IA, Instagram, Web o WhatsApp.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
            )
        }
    }
}

private fun formatAdminDop(amount: Int): String {
    if (amount < 1000) {
        return "RD$ $amount"
    }
    val thousands = amount / 1000
    val remainder = (amount % 1000).toString().padStart(3, '0')
    return "RD$ $thousands,$remainder"
}

@Composable
internal fun RenderAdminQuoteBuilderA03(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val articlesList = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings
    val selectedQuoteId = viewModel?.selectedQuoteId?.collectAsState()?.value.orEmpty()
    val selectedLeadId = viewModel?.selectedLeadId?.collectAsState()?.value.orEmpty()
    val formError = viewModel?.formError?.collectAsState()?.value
    val draftState by adminQuoteDraftFlow.collectAsState()

    val fallbackVenue = businessSettings.zones.firstOrNull()?.name ?: "Jardín Los Rosales"
    val explicitQuote = quotesList.firstOrNull { quoteItem -> quoteItem.id == selectedQuoteId }
    val explicitLead = leadsList.firstOrNull { leadItem -> leadItem.id == selectedLeadId }
    val resolvedQuote = viewModel?.selectedQuote() ?: CanonicalSampleData.quotes[1]
    val resolvedLead = viewModel?.selectedLead()

    LaunchedEffect(selectedQuoteId, selectedLeadId) {
        val currentKey = adminQuoteDraftFlow.value.sourceKey
        val expectedQuoteKey = if (explicitQuote != null) "QUOTE:${explicitQuote.id}" else ""
        val expectedLeadKey = if (explicitLead != null) "LEAD:${explicitLead.id}" else ""
        val alreadyMatchesQuote = bothConditions(expectedQuoteKey.isNotEmpty(), currentKey == expectedQuoteKey)
        val alreadyMatchesLead = bothConditions(expectedLeadKey.isNotEmpty(), currentKey == expectedLeadKey)
        val alreadyInitializedForSelection = eitherCondition(alreadyMatchesQuote, alreadyMatchesLead)
        if (!alreadyInitializedForSelection) {
            if (explicitQuote != null) {
                primeQuoteDraftFromQuote(explicitQuote, fallbackVenue)
            } else if (explicitLead != null) {
                primeQuoteDraftFromLead(explicitLead, fallbackVenue, articlesList)
            } else if (currentKey.isEmpty()) {
                val selectedQuoteOrNull = viewModel?.selectedQuote()
                if (selectedQuoteOrNull != null) {
                    primeQuoteDraftFromQuote(selectedQuoteOrNull, fallbackVenue)
                } else if (resolvedLead != null) {
                    primeQuoteDraftFromLead(resolvedLead, fallbackVenue, articlesList)
                } else {
                    primeQuoteDraftFromQuote(resolvedQuote, fallbackVenue)
                }
            }
        }
    }

    var localValidationError by remember { mutableStateOf<String?>(null) }
    var showQuickCatalogSheet by remember { mutableStateOf(false) }

    val activeLines = draftState.lines
    val articlesSubtotal = activeLines.sumOf { lineItem -> lineItem.subtotalDop }
    val parsedTransportDop = draftState.deliveryFeeText.filter { digitChar -> digitChar.isDigit() }.toIntOrNull() ?: 0
    val parsedDiscountDop = draftState.discountText.filter { digitChar -> digitChar.isDigit() }.toIntOrNull() ?: 0
    val totalQuoteDop = (articlesSubtotal + parsedTransportDop - parsedDiscountDop).coerceAtLeast(0)
    val activeArticleLinesCount = activeLines.size
    val displayClientName = draftState.clientName.ifBlank { resolvedQuote.clientName }
    val displayEventTitle = draftState.eventTitle.ifBlank { resolvedQuote.eventTitle }
    val displayDateLabel = draftState.dateLabel.ifBlank { resolvedQuote.dateLabel }
    val displayVenue = draftState.venue.ifBlank { fallbackVenue }
    val clientInitial = displayClientName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
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
                        .clickable { onSelectScreenCode("BACK:A02") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cotizar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "$displayEventTitle · $displayDateLabel",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = displayClientName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                            )
                            BasicText(
                                text = "${draftState.guestsCount} invitados · $displayVenue",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFECECF5), CircleShape)
                                .clickable {
                                    viewModel?.selectChatThreadForContact(displayClientName)
                                    onSelectScreenCode("A15")
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 20.dp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF4B47D6), size = 16.dp)
                            BasicTextField(
                                value = draftState.venue,
                                onValueChange = { nextVenue ->
                                    adminQuoteDraftFlow.update { stateItem -> stateItem.copy(venue = nextVenue) }
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF2E324A)),
                                decorationBox = { innerTextField ->
                                    if (draftState.venue.isBlank()) {
                                        BasicText(
                                            text = fallbackVenue,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF8C90A6)),
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = "ARTÍCULOS",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)),
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFECEBFF))
                        .clickable { showQuickCatalogSheet = !showQuickCatalogSheet }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = if (showQuickCatalogSheet) "Cerrar lista rápida" else "+ Catálogo rápido",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                    )
                }
            }

            if (showQuickCatalogSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF9F9FE))
                        .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText(
                            text = "Toca un artículo para sumarlo a la cotización:",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                        )
                        articlesList.take(6).forEach { catalogItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .clickable {
                                        appendCatalogArticleToAdminQuoteDraft(catalogItem)
                                        localValidationError = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = catalogItem.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                    )
                                    BasicText(
                                        text = "${formatAdminDop(catalogItem.pricePerDayDop)}/día · ${catalogItem.availableOct23} disp.",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 18.dp)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    if (activeLines.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Sin artículos seleccionados. Agrega piezas del inventario.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377), textAlign = TextAlign.Center),
                            )
                        }
                    } else {
                        activeLines.forEachIndexed { lineIndex, lineItem ->
                            if (lineIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                ) {
                                    RfPhoto(
                                        photoKey = photoKeyForAsset(lineItem.photo),
                                        contentDescription = lineItem.name,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    BasicText(
                                        text = lineItem.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = formatAdminDop(lineItem.unitPriceDop),
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF2E324A)),
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .height(22.dp)
                                                .background(Color(0xFFEEF6D6), CircleShape)
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = formatAdminDop(lineItem.subtotalDop),
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)),
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2F2F8))
                                        .padding(horizontal = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                adminQuoteDraftFlow.update { currentDraft ->
                                                    val nextQty = lineItem.quantity - 1
                                                    val updatedLines = if (nextQty <= 0) {
                                                        currentDraft.lines.filterIndexed { itemIndex, _ -> itemIndex != lineIndex }
                                                    } else {
                                                        currentDraft.lines.mapIndexed { itemIndex, existingLine ->
                                                            if (itemIndex == lineIndex) existingLine.copy(quantity = nextQty) else existingLine
                                                        }
                                                    }
                                                    currentDraft.copy(lines = updatedLines)
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                    }
                                    BasicText(
                                        text = "${lineItem.quantity}",
                                        modifier = Modifier.defaultMinSize(minWidth = 24.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A), textAlign = TextAlign.Center),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                adminQuoteDraftFlow.update { currentDraft ->
                                                    val updatedLines = currentDraft.lines.mapIndexed { itemIndex, existingLine ->
                                                        if (itemIndex == lineIndex) existingLine.copy(quantity = existingLine.quantity + 1) else existingLine
                                                    }
                                                    currentDraft.copy(lines = updatedLines)
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clickable {
                                viewModel?.setInventoryPickerForQuote(true)
                                onSelectScreenCode("A09")
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                        Spacer(Modifier.width(6.dp))
                        BasicText("Agregar del inventario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BasicText("Transporte e instalación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("RD$", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        BasicTextField(
                            value = draftState.deliveryFeeText,
                            onValueChange = { updatedText ->
                                val cleanDigits = updatedText.filter { digitChar -> digitChar.isDigit() }
                                adminQuoteDraftFlow.update { stateItem -> stateItem.copy(deliveryFeeText = cleanDigits) }
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                            modifier = Modifier.widthIn(min = 32.dp, max = 68.dp),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 70.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicTextField(
                    value = draftState.notes,
                    onValueChange = { updatedText ->
                        adminQuoteDraftFlow.update { stateItem -> stateItem.copy(notes = updatedText) }
                    },
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729), lineHeight = 19.sp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val displayedError = localValidationError ?: formError
            if (!displayedError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDE3EA))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BasicText(
                        text = displayedError,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5))
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    BasicText("Total · $activeArticleLinesCount artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    BasicText(formatAdminDop(totalQuoteDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable {
                            if (activeLines.isEmpty()) {
                                localValidationError = "Agrega al menos un artículo antes de enviar la cotización."
                            } else {
                                localValidationError = null
                                val activeVm = viewModel
                                if (activeVm != null) {
                                    val createdQuote = activeVm.createFormalQuoteFromAdmin(
                                        eventTitle = displayEventTitle,
                                        clientName = displayClientName,
                                        clientEmail = draftState.clientEmail,
                                        venue = displayVenue,
                                        dateLabel = displayDateLabel,
                                        guests = draftState.guestsCount,
                                        lines = activeLines,
                                        deliveryFeeDop = parsedTransportDop,
                                        discountDop = parsedDiscountDop,
                                    )
                                    val matchingLead = leadsList.firstOrNull { leadItem ->
                                        leadItem.clientName.equals(displayClientName, ignoreCase = true)
                                    }
                                    if (matchingLead != null) {
                                        activeVm.updateCommercialLead(
                                            leadId = matchingLead.id,
                                            stage = "Cotización enviada",
                                            followUpNote = draftState.notes,
                                        )
                                    }
                                    if (createdQuote != null) {
                                        onSelectScreenCode("A22")
                                    }
                                } else {
                                    onSelectScreenCode("A22")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 20.dp)
                        BasicText("Enviar cotización", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

private fun matchesInventoryCategoryChip(articleItem: CatalogArticle, selectedCategory: String): Boolean = when (selectedCategory) {
    "Todos" -> true

    "Mobiliario" -> {
        val inFurniture = articleItem.category.contains("Mobiliario", ignoreCase = true)
        val inChairsOrTables = eitherCondition(
            articleItem.category.contains("Sillas", ignoreCase = true),
            articleItem.category.contains("Mesas", ignoreCase = true),
        )
        eitherCondition(inFurniture, inChairsOrTables)
    }

    "Floral" -> eitherCondition(
        articleItem.category.contains("Flor", ignoreCase = true),
        articleItem.name.contains("floral", ignoreCase = true),
    )

    "Globos" -> {
        val inBalloonCat = articleItem.category.contains("Globo", ignoreCase = true)
        val inBalloonName = eitherCondition(
            articleItem.name.contains("globo", ignoreCase = true),
            articleItem.name.contains("arco", ignoreCase = true),
        )
        eitherCondition(inBalloonCat, inBalloonName)
    }

    "Mesa dulce" -> {
        val inSweetCat = eitherCondition(
            articleItem.category.contains("Dulce", ignoreCase = true),
            articleItem.category.contains("Estaciones", ignoreCase = true),
        )
        eitherCondition(inSweetCat, articleItem.name.contains("helado", ignoreCase = true))
    }

    else -> true
}

private fun isLowStockArticle(articleItem: CatalogArticle): Boolean = eitherCondition(
    articleItem.availableOct23 in 1..5,
    articleItem.totalStock <= 5,
)

@Composable
internal fun RenderAdminInventoryA09(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val articles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val isQuotePickerMode = viewModel?.inventoryPickerForQuote?.collectAsState()?.value ?: false

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedStockSortOption by remember { mutableStateOf("Todos") }
    var showFilterSheet by remember { mutableStateOf(false) }

    val categoryChips = remember { listOf("Todos", "Mobiliario", "Floral", "Globos", "Mesa dulce") }
    val filterSortOptions = remember {
        listOf("Todos", "Stock bajo", "Ocultos en catálogo", "Mayor precio", "Menor precio")
    }

    val distinctCategoriesCount = remember(articles) {
        articles.map { articleItem -> articleItem.category.trim().lowercase() }.distinct().size
    }

    val baseFilteredArticles = articles.filter { articleItem ->
        val matchesCategory = matchesInventoryCategoryChip(articleItem, selectedCategory)
        val trimmedSearch = searchQuery.trim()
        val matchesNameOrCat = eitherCondition(
            articleItem.name.contains(trimmedSearch, ignoreCase = true),
            articleItem.category.contains(trimmedSearch, ignoreCase = true),
        )
        val matchesCode = articleItem.code.contains(trimmedSearch, ignoreCase = true)
        val matchesSearch = if (trimmedSearch.isEmpty()) {
            true
        } else {
            eitherCondition(matchesNameOrCat, matchesCode)
        }
        val matchesVisibilityOrStock = when (selectedStockSortOption) {
            "Stock bajo" -> isLowStockArticle(articleItem)
            "Ocultos en catálogo" -> !articleItem.visibleInCatalog
            else -> true
        }
        val matchesPrimary = bothConditions(matchesCategory, matchesSearch)
        bothConditions(matchesPrimary, matchesVisibilityOrStock)
    }

    val filteredArticles = when (selectedStockSortOption) {
        "Mayor precio" -> baseFilteredArticles.sortedByDescending { articleItem -> articleItem.pricePerDayDop }
        "Menor precio" -> baseFilteredArticles.sortedBy { articleItem -> articleItem.pricePerDayDop }
        else -> baseFilteredArticles
    }

    val disponiblesCount = articles.count { articleItem -> articleItem.availableOct23 > 5 }
    val reservadosCount = articles.count { articleItem -> articleItem.totalStock - articleItem.availableOct23 > 0 }
    val stockBajoCount = articles.count { articleItem -> isLowStockArticle(articleItem) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isQuotePickerMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Modo selección para cotización",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                        BasicText(
                            text = "Toca un artículo para sumarlo al borrador",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFFE9EEFF)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable {
                                viewModel.setInventoryPickerForQuote(false)
                                onSelectScreenCode("BACK:A03")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = "Volver",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Inventario",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
                BasicText(
                    text = "${articles.size} artículos · $distinctCategoriesCount categorías",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("s63") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF151729), size = 20.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable {
                        viewModel?.startEditingArticle(null)
                        onSelectScreenCode("A10")
                    },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        BasicText(
                            text = "Buscar artículo",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updatedText -> searchQuery = updatedText },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showFilterSheet) Color(0xFF5B6CF0) else Color(0xFFECEBFF))
                        .clickable { showFilterSheet = !showFilterSheet },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(
                        kind = RfIconKind.FILTER,
                        tint = if (showFilterSheet) Color.White else Color(0xFF4B47D6),
                        size = 18.dp,
                    )
                }
            }
        }

        if (showFilterSheet) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(18.dp))
                    .padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText(
                            text = "Filtrar y ordenar inventario",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        RfIcon(
                            kind = RfIconKind.CLOSE,
                            tint = Color(0xFF5F6377),
                            size = 16.dp,
                            modifier = Modifier.clickable { showFilterSheet = false },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        filterSortOptions.forEach { optionLabel ->
                            val isOptionActive = selectedStockSortOption == optionLabel
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isOptionActive) Color(0xFF4B47D6) else Color(0xFFF2F2F8))
                                    .clickable {
                                        selectedStockSortOption = optionLabel
                                        showFilterSheet = false
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = optionLabel,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isOptionActive) Color.White else Color(0xFF2E324A),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categoryChips.forEach { categoryLabel ->
                val isCategorySelected = selectedCategory == categoryLabel
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isCategorySelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            },
                        )
                        .clickable { selectedCategory = categoryLabel }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        categoryLabel,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isCategorySelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCategorySelected) Color.White else Color(0xFF2E324A),
                        ),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column {
                    BasicText(disponiblesCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F560C)))
                    BasicText("Disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFECEBFF))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column {
                    BasicText(reservadosCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)))
                    BasicText("Reservados hoy", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFDE3EA))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column {
                    BasicText(stockBajoCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2B48)))
                    BasicText("Stock bajo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFFDC2B48)))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (filteredArticles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "No se encontraron artículos con los filtros activos.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                        )
                    }
                } else {
                    filteredArticles.forEachIndexed { itemIndex, articleItem ->
                        if (itemIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        val photoKey = photoKeyForAsset(articleItem.photo)
                        val isLowStock = isLowStockArticle(articleItem)
                        val tagText = when {
                            !articleItem.visibleInCatalog -> "Oculto"
                            isLowStock -> "Stock bajo"
                            else -> "Activo"
                        }
                        val tagBg = when {
                            !articleItem.visibleInCatalog -> Color(0xFFF2F2F8)
                            isLowStock -> Color(0xFFFDE3EA)
                            else -> Color(0xFFEEF6D6)
                        }
                        val tagColor = when {
                            !articleItem.visibleInCatalog -> Color(0xFF5F6377)
                            isLowStock -> Color(0xFFDC2B48)
                            else -> Color(0xFF4E6A10)
                        }

                        val handleArticleTap: () -> Unit = {
                            viewModel?.soundPlayer?.playTap()
                            if (isQuotePickerMode) {
                                appendCatalogArticleToAdminQuoteDraft(articleItem)
                                viewModel.setInventoryPickerForQuote(false)
                                onSelectScreenCode("BACK:A03")
                            } else {
                                viewModel?.selectArticle(articleItem.id)
                                onSelectScreenCode("A23")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { handleArticleTap() }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                RfPhoto(photoKey = photoKey, contentDescription = articleItem.name, modifier = Modifier.fillMaxSize())
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                BasicText(
                                    text = articleItem.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                )
                                BasicText(
                                    text = "${articleItem.totalStock} unid. · RD$ ${articleItem.pricePerDayDop}/día",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                            }

                            if (isQuotePickerMode) {
                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFECEBFF))
                                        .clickable { handleArticleTap() }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 14.dp)
                                        BasicText(
                                            text = "Agregar",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .background(tagBg, CircleShape)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(tagText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tagColor))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun extractDayNumberTokens(dateLabel: String): Set<String> {
    val tokens = mutableSetOf<String>()
    var activeRun = ""
    for (charIndex in 0 until dateLabel.length) {
        val currentChar = dateLabel[charIndex]
        if (currentChar.isDigit()) {
            activeRun += currentChar
        } else {
            if (activeRun.isNotEmpty()) {
                val parsed = activeRun.toIntOrNull()
                if (parsed != null) {
                    tokens.add(parsed.toString())
                }
                activeRun = ""
            }
        }
    }
    if (activeRun.isNotEmpty()) {
        val parsed = activeRun.toIntOrNull()
        if (parsed != null) {
            tokens.add(parsed.toString())
        }
    }
    return tokens
}

private fun statusTagStyleForEvent(eventItem: ClientEvent): Triple<String, Color, Color> = when (eventItem.status) {
    EventStatus.CONFIRMED -> Triple("Confirmado", Color(0xFFEEF6D6), Color(0xFF4E6A10))
    EventStatus.IN_SETUP -> Triple("En montaje", Color(0xFFDDF4F3), Color(0xFF1F8A84))
    EventStatus.PENDING_QUOTE -> Triple("En revisión", Color(0xFFFFF2C9), Color(0xFF7A5700))
    EventStatus.COMPLETED -> Triple("Cerrado", Color(0xFFEEF6D6), Color(0xFF4E6A10))
    EventStatus.CANCELLED -> Triple("Cancelado", Color(0xFFFDE3EA), Color(0xFFB42345))
}

@Composable
internal fun RenderAdminEventsA11(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics

    var selectedDayFilter by remember { mutableStateOf("Todos") }
    var selectedEventSegment by remember { mutableStateOf(0) }

    val upcomingEvents = allEvents.filter { eventItem ->
        val notDone = eventItem.status != EventStatus.COMPLETED
        val notCancelled = eventItem.status != EventStatus.CANCELLED
        bothConditions(notDone, notCancelled)
    }
    val receivableEvents = allEvents.filter { eventItem -> eventItem.balanceDop > 0 }
    val pastEvents = allEvents.filter { eventItem ->
        val isCompletedOrCancelled = eitherCondition(
            eventItem.status == EventStatus.COMPLETED,
            eventItem.status == EventStatus.CANCELLED,
        )
        eitherCondition(isCompletedOrCancelled, eventItem.daysUntilEvent < 0)
    }

    val segmentedEvents = when (selectedEventSegment) {
        1 -> receivableEvents
        2 -> pastEvents
        else -> upcomingEvents
    }

    val displayedEvents = if (selectedDayFilter == "Todos") {
        segmentedEvents
    } else {
        segmentedEvents.filter { eventItem ->
            selectedDayFilter in extractDayNumberTokens(eventItem.dateLabel)
        }
    }

    val dayNumbersWithEvents = remember(allEvents) {
        allEvents.flatMap { eventItem -> extractDayNumberTokens(eventItem.dateLabel) }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Eventos",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
                BasicText(
                    text = "${metrics.monthLabel} · ${upcomingEvents.size} próximos",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("s63") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF151729), size = 20.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A04") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val isAllSelected = selectedDayFilter == "Todos"
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isAllSelected) {
                            Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        } else {
                            Modifier.background(Color.White).border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        },
                    )
                    .clickable { selectedDayFilter = "Todos" }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    BasicText(
                        text = "Ver",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isAllSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF5F6377)),
                    )
                    BasicText(
                        text = "Todos",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isAllSelected) Color.White else Color(0xFF151729)),
                    )
                }
            }

            val weekDays = listOf(
                "L" to "19",
                "M" to "20",
                "X" to "21",
                "J" to "22",
                "V" to "23",
                "S" to "24",
                "D" to "25",
            )

            weekDays.forEach { dayPair ->
                val dayLetter = dayPair.first
                val dayNumber = dayPair.second
                val isSelected = selectedDayFilter == dayNumber
                val hasMatchingEvent = dayNumber in dayNumbersWithEvents
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                            },
                        )
                        .clickable {
                            selectedDayFilter = if (isSelected) "Todos" else dayNumber
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(
                            text = dayLetter,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF5F6377)),
                        )
                        BasicText(
                            text = dayNumber,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) Color.White else Color(0xFF151729)),
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (hasMatchingEvent) Color(0xFFFFD45C) else Color.Transparent),
                        )
                    }
                }
            }
        }

        val eventSegments = listOf(
            "Próximos · ${upcomingEvents.size}",
            "Por cobrar · ${receivableEvents.size}",
            "Pasados · ${pastEvents.size}",
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEDF5), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            eventSegments.forEachIndexed { segmentIndex, segmentTitle ->
                val isSegmentSelected = selectedEventSegment == segmentIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSegmentSelected) Color.White else Color.Transparent)
                        .clickable { selectedEventSegment = segmentIndex },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = segmentTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isSegmentSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSegmentSelected) Color(0xFF2E324A) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }

        if (displayedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BasicText(
                        text = "No hay eventos programados el día $selectedDayFilter",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFECEBFF))
                            .clickable { selectedDayFilter = "Todos" }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = "Mostrar todos los eventos",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayedEvents.forEach { eventItem ->
                    val tagTriple = statusTagStyleForEvent(eventItem)
                    val paymentProgress = if (eventItem.totalDop > 0) {
                        (eventItem.paidDepositDop.toFloat() / eventItem.totalDop.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel?.selectEvent(eventItem.id)
                                onSelectScreenCode("A13")
                            }
                            .padding(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                            ) {
                                RfPhoto(
                                    photoKey = photoKeyForAsset(eventItem.photo),
                                    contentDescription = eventItem.title,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                BasicText(
                                    text = eventItem.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                )
                                BasicText(
                                    text = "${formatReadableSpanishDateText(eventItem.dateLabel)} · ${eventItem.clientName} · ${eventItem.venue}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(22.dp)
                                            .background(tagTriple.second, CircleShape)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = tagTriple.first,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tagTriple.third),
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFEDEDF5)),
                                    ) {
                                        if (paymentProgress > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(paymentProgress)
                                                    .height(5.dp)
                                                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))),
                                            )
                                        }
                                    }

                                    BasicText(
                                        text = "${(paymentProgress * 100).toInt()}%",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resolveAdminRoleLabel(rawRole: String): String {
    val normalized = rawRole.trim().lowercase()
    return when {
        normalized.contains("decor") -> "Decoración"
        normalized.contains("logist") -> "Logística"
        normalized.contains("ware") -> "Almacén"
        normalized.startsWith("staff") -> "Equipo"
        else -> "Dueña"
    }
}

@Composable
internal fun RenderAdminMoreA12(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    onSwitchAppMode: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val rawProfileName = viewModel?.profileName?.collectAsState()?.value ?: "María"
    val rawActiveRole = viewModel?.activeRole?.collectAsState()?.value ?: "admin"
    val rawUserEmail = viewModel?.currentUserEmail?.collectAsState()?.value.orEmpty()

    val adminDisplayName = resolveAdminFirstName(rawProfileName)
    val adminRoleLabel = resolveAdminRoleLabel(rawActiveRole)
    val adminDisplayEmail = rawUserEmail.trim().ifBlank { "maria@rosafiesta.do" }

    var showWebDesktopModal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText(
                text = "Más",
                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable { onSelectScreenCode("A31") }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0x24EA4EA9), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfLogo(size = RfLogoSize.MEDIUM)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "$adminDisplayName · $adminRoleLabel",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = adminDisplayEmail,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }

                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                }
            }

            val tiles = listOf(
                Triple("Chats", RfIconKind.CHAT, "A14" to (Color(0xFFDDF4F3) to Color(0xFF1F8A84))),
                Triple("Pagos", RfIconKind.WALLET, "A20" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                Triple("Clientes", RfIconKind.USER, "A26" to (Color(0xFFFDE6F2) to Color(0xFFC2338F))),
                Triple("Cotizaciones", RfIconKind.ORDERS, "A22" to (Color(0xFFFFF2C9) to Color(0xFF7A5700))),
                Triple("Paquetes", RfIconKind.GIFT, "A24" to (Color(0xFFECEBFF) to Color(0xFF4B47D6))),
                Triple("Panel", RfIconKind.TREND_UP, "s58" to (Color(0xFFECEBFF) to Color(0xFF4B47D6))),
                Triple("Reportes", RfIconKind.TREND_UP, "s67" to (Color(0xFFDDF4F3) to Color(0xFF1F8A84))),
                Triple("Leads", RfIconKind.LOCATION, "s64" to (Color(0xFFFDE6F2) to Color(0xFFC2338F))),
                Triple("Proveedores", RfIconKind.TRUCK, "s55" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                Triple("Reseñas", RfIconKind.STAR, "s71" to (Color(0xFFFFF2C9) to Color(0xFF7A5700))),
                Triple("Bot WhatsApp", RfIconKind.WHATSAPP, "s72" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                Triple("Equipo", RfIconKind.USER, "A28" to (Color(0xFFECEBFF) to Color(0xFF4B47D6))),
                Triple("Cancelaciones", RfIconKind.WARNING, "A32" to (Color(0xFFFCE3E8) to Color(0xFFB42345))),
                Triple("Calendario", RfIconKind.CALENDAR, "s63" to (Color(0xFFDDF4F3) to Color(0xFF1F8A84))),
                Triple("Versión web", RfIconKind.GLOBE, "WEB_MODAL" to (Color(0xFFF2F2F8) to Color(0xFF5F6377))),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tiles.chunked(3).forEach { rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowTiles.forEach { tileEntry ->
                            val label = tileEntry.first
                            val iconKind = tileEntry.second
                            val screenTarget = tileEntry.third.first
                            val tileColors = tileEntry.third.second
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                                    .clickable {
                                        if (screenTarget == "WEB_MODAL") {
                                            showWebDesktopModal = true
                                        } else {
                                            onSelectScreenCode(screenTarget)
                                        }
                                    }
                                    .padding(vertical = 9.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(tileColors.first),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = iconKind, tint = tileColors.second, size = 18.dp)
                                    }
                                    BasicText(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                    )
                                }
                            }
                        }
                        repeat(3 - rowTiles.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("A31") }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF2F2F8)), contentAlignment = Alignment.Center) {
                            RfIcon(kind = RfIconKind.SETTINGS, tint = Color(0xFF5F6377), size = 18.dp)
                        }
                        BasicText("Ajustes del negocio", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.switchRole("client")
                                onSwitchAppMode()
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFDE6F2)), contentAlignment = Alignment.Center) {
                            RfIcon(kind = RfIconKind.USER, tint = Color(0xFFC2338F), size = 18.dp)
                        }
                        BasicText("Entrar como cliente", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.logoutSession()
                                onSelectScreenCode("A30")
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFDE3EA)), contentAlignment = Alignment.Center) {
                            RfIcon(kind = RfIconKind.LOGOUT, tint = Color(0xFFDC2B48), size = 18.dp)
                        }
                        BasicText("Cerrar sesión", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2B48)))
                    }
                }
            }
        }

        if (showWebDesktopModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73141632))
                    .clickable { showWebDesktopModal = false },
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText(
                            text = "Panel Web Desktop (1440px)",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                        )
                        RfIcon(
                            kind = RfIconKind.CLOSE,
                            tint = Color(0xFF5F6377),
                            size = 20.dp,
                            modifier = Modifier.clickable { showWebDesktopModal = false },
                        )
                    }
                    BasicText(
                        text = "El centro administrativo de escritorio está optimizado para monitores amplios de 1440px. Puedes abrirlo directamente en tu navegador o explorar la vista previa adaptada.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF5F6377), lineHeight = 19.sp),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AdminButtonGradient)
                            .clickable {
                                showWebDesktopModal = false
                                viewModel?.openExternalUrl("https://admin.rosafiesta.do")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RfIcon(kind = RfIconKind.GLOBE, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = "Abrir en navegador",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFECEBFF))
                            .clickable {
                                showWebDesktopModal = false
                                onSelectScreenCode("A39")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Vista previa adaptada",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminPaymentsA20(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val paymentsList = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics

    val receivableEvents = eventsList.filter { eventItem -> eventItem.balanceDop > 0 }
    val collectedMonthDop = if (paymentsList.isNotEmpty()) {
        paymentsList.sumOf { paymentItem -> paymentItem.amountDop }
    } else {
        metrics.octoberRevenueDop
    }
    val receivableTotalDop = receivableEvents.sumOf { eventItem -> eventItem.balanceDop }

    var selectedPaymentTab by remember { mutableStateOf(0) }
    var remindedEventIds by remember { mutableStateOf(emptySet<String>()) }

    val paymentTabs = listOf(
        "Todos",
        "Cobrado (${paymentsList.size})",
        "Por cobrar (${receivableEvents.size})",
    )

    val showReceivablesSection = eitherCondition(selectedPaymentTab == 0, selectedPaymentTab == 2)
    val showCollectedSection = eitherCondition(selectedPaymentTab == 0, selectedPaymentTab == 1)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Pagos", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    BasicText(metrics.monthLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                        .clickable { selectedPaymentTab = 1 }
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Cobrado este mes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE9EEFF)))
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicText(
                            text = formatAdminDop(collectedMonthDop),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF2C9))
                        .clickable { selectedPaymentTab = 2 }
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Por cobrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7A5700)))
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicText(
                            text = formatAdminDop(receivableTotalDop),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700)),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEDEDF5))
                    .padding(4.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    paymentTabs.forEachIndexed { tabIndex, tabLabel ->
                        val isSelected = selectedPaymentTab == tabIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { selectedPaymentTab = tabIndex },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = tabLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF2E324A) else Color(0xFF5F6377),
                                ),
                            )
                        }
                    }
                }
            }

            if (showReceivablesSection) {
                BasicText("CUENTAS POR COBRAR", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377), letterSpacing = 1.sp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
                ) {
                    Column {
                        if (receivableEvents.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Todos los eventos tienen su saldo cubierto.",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                            }
                        } else {
                            receivableEvents.forEachIndexed { rowIndex, receivableEvent ->
                                val isReminded = receivableEvent.id in remindedEventIds
                                val clientInitial = receivableEvent.clientName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
                                val isUrgentDue = receivableEvent.daysUntilEvent <= 5
                                if (rowIndex > 0) {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel?.selectEvent(receivableEvent.id)
                                            onSelectScreenCode("A21")
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFDE6F2)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        BasicText(
                                            text = "${receivableEvent.title} · ${receivableEvent.clientName}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                        )
                                        BasicText(
                                            text = "Vence ${receivableEvent.balanceDueDateLabel}",
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 12.sp,
                                                color = if (isUrgentDue) Color(0xFFB42345) else Color(0xFF7A5700),
                                            ),
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        BasicText(
                                            text = formatAdminDop(receivableEvent.balanceDop),
                                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                                        )
                                        Row(
                                            modifier = Modifier.clickable {
                                                remindedEventIds = if (isReminded) {
                                                    remindedEventIds - receivableEvent.id
                                                } else {
                                                    viewModel?.postFeedback("Recordatorio enviado a ${receivableEvent.clientName}.")
                                                    remindedEventIds + receivableEvent.id
                                                }
                                            },
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            RfIcon(
                                                kind = if (isReminded) RfIconKind.CHECK else RfIconKind.WHATSAPP,
                                                tint = if (isReminded) Color(0xFF4E6A10) else Color(0xFF4B47D6),
                                                size = 13.dp,
                                            )
                                            BasicText(
                                                text = if (isReminded) "Recordado" else "Recordar",
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isReminded) Color(0xFF4E6A10) else Color(0xFF4B47D6),
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

            if (showCollectedSection) {
                BasicText("COBROS REGISTRADOS", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377), letterSpacing = 1.sp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
                ) {
                    Column {
                        paymentsList.forEachIndexed { paymentIndex, paymentItem ->
                            if (paymentIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val linkedEvent = eventsList.firstOrNull { eventItem -> eventItem.id == paymentItem.eventId }
                                        if (linkedEvent != null) {
                                            viewModel?.exportReceiptDocument(event = linkedEvent, payment = paymentItem)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (paymentItem.verified) Color(0xFFEEF6D6) else Color(0xFFFFF2C9)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(
                                        kind = if (paymentItem.verified) RfIconKind.CHECK else RfIconKind.CLOCK,
                                        tint = if (paymentItem.verified) Color(0xFF4E6A10) else Color(0xFF7A5700),
                                        size = 18.dp,
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = "${paymentItem.clientName} · ${paymentItem.kindLabel}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                                    )
                                    BasicText(
                                        text = "${paymentItem.eventTitle} · ${paymentItem.methodLabel}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                    )
                                }

                                BasicText(
                                    text = "+${formatAdminDop(paymentItem.amountDop)}",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable {
                        val firstUnpaid = receivableEvents.firstOrNull()
                        if (firstUnpaid != null) {
                            viewModel?.selectEvent(firstUnpaid.id)
                        }
                        onSelectScreenCode("A21")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                    BasicText("Registrar pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminRecordPaymentA21(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value.orEmpty()
    val pickedAttachment = viewModel?.pickedAttachmentFile?.collectAsState()?.value
    val formError = viewModel?.formError?.collectAsState()?.value

    val selectableEvents = remember(allEvents) {
        val unpaidFirst = allEvents.filter { eventItem -> eventItem.balanceDop > 0 }
        if (unpaidFirst.isNotEmpty()) unpaidFirst else allEvents
    }
    val initialSelectedEvent = selectableEvents.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: allEvents.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: selectableEvents.first()

    var chosenEventId by remember(selectedEventId) { mutableStateOf(initialSelectedEvent.id) }
    val chosenEvent = allEvents.firstOrNull { eventItem -> eventItem.id == chosenEventId } ?: initialSelectedEvent

    var showEventDropdown by remember { mutableStateOf(false) }
    var amountDigitsText by remember(chosenEvent.id) {
        val prefillDop = if (chosenEvent.balanceDop > 0) chosenEvent.balanceDop else chosenEvent.totalDop
        mutableStateOf(prefillDop.toString())
    }
    var selectedMethod by remember { mutableStateOf("Transferencia") }
    var referenceText by remember(chosenEvent.id) {
        mutableStateOf("BR-${chosenEvent.code.takeLast(4)}-01")
    }
    var paymentDate by remember { mutableStateOf(DomainClockFormatter.currentFullDateLabel()) }
    var sendWhatsAppReceipt by remember { mutableStateOf(true) }
    var localErrorText by remember { mutableStateOf<String?>(null) }

    val parsedAmountDop = amountDigitsText.filter { digitChar -> digitChar.isDigit() }.toIntOrNull() ?: 0
    val clientFirstName = chosenEvent.clientName.trim().substringBefore(" ").ifBlank { "Cliente" }
    val clientInitial = clientFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    Box(modifier = Modifier.fillMaxSize()) {
        RenderAdminPaymentsA20(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable { onSelectScreenCode("A20") },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3E4EF)),
                )

                BasicText(
                    text = "Registrar pago",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .clickable { showEventDropdown = !showEventDropdown }
                        .padding(start = 8.dp, end = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }

                        BasicText(
                            text = "${chosenEvent.title} · ${chosenEvent.clientName}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                        )

                        RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF5F6377), size = 18.dp)
                    }
                }

                if (showEventDropdown) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8F8FC))
                            .border(1.dp, Color(0xFFDCDDEA), RoundedCornerShape(14.dp))
                            .padding(8.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectableEvents.forEach { optionEvent ->
                                val isChosen = optionEvent.id == chosenEvent.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isChosen) Color(0xFFECEBFF) else Color.White)
                                        .clickable {
                                            chosenEventId = optionEvent.id
                                            viewModel?.selectEvent(optionEvent.id)
                                            localErrorText = null
                                            showEventDropdown = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    BasicText(
                                        text = "${optionEvent.title} · ${optionEvent.clientName}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF151729)),
                                    )
                                    BasicText(
                                        text = formatAdminDop(optionEvent.balanceDop),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BasicText("Monto (RD$)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            BasicText(
                                text = "RD$",
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)),
                            )
                            BasicTextField(
                                value = amountDigitsText,
                                onValueChange = { updatedText ->
                                    amountDigitsText = updatedText.filter { digitChar -> digitChar.isDigit() }.take(7)
                                    localErrorText = null
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF151729),
                                    textAlign = TextAlign.Center,
                                ),
                                modifier = Modifier.widthIn(min = 80.dp, max = 180.dp),
                            )
                        }
                    }
                    val balanceHint = if (parsedAmountDop >= chosenEvent.balanceDop) {
                        "Salda el balance pendiente (${formatAdminDop(chosenEvent.balanceDop)})"
                    } else {
                        "Abono parcial · restarán ${formatAdminDop((chosenEvent.balanceDop - parsedAmountDop).coerceAtLeast(0))}"
                    }
                    BasicText(balanceHint, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }

                val paymentMethods = listOf("Transferencia", "Efectivo", "Tarjeta")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentMethods.forEach { methodLabel ->
                        val isMethodSelected = selectedMethod == methodLabel
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isMethodSelected) {
                                        Modifier.background(AdminButtonGradient)
                                    } else {
                                        Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                                    },
                                )
                                .clickable {
                                    selectedMethod = methodLabel
                                    localErrorText = null
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                methodLabel,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    fontWeight = if (isMethodSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMethodSelected) Color.White else Color(0xFF2E324A),
                                ),
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = referenceText,
                            onValueChange = { updatedText ->
                                referenceText = updatedText
                                localErrorText = null
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF2E324A)),
                            decorationBox = { innerField ->
                                if (referenceText.isBlank()) {
                                    BasicText(
                                        text = "Referencia / Banco",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF8C90A6)),
                                    )
                                }
                                innerField()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    var showPaymentDatePicker by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(start = 10.dp, end = 6.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        RfMaskedDateField(
                            value = paymentDate,
                            onValueChange = { updatedText -> paymentDate = updatedText },
                            onOpenCalendar = { showPaymentDatePicker = true },
                            placeholder = "Elegir fecha",
                            isAdmin = true,
                            compactInline = true,
                            embeddedInContainer = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (showPaymentDatePicker) {
                        RfInteractiveCalendarModal(
                            title = "Fecha del pago",
                            currentDateText = paymentDate,
                            minYear = 2024,
                            maxYear = 2030,
                            defaultYear = 2026,
                            isAdmin = true,
                            onDismiss = { showPaymentDatePicker = false },
                            onSelectDate = { formattedDate ->
                                paymentDate = formattedDate
                                showPaymentDatePicker = false
                            },
                        )
                    }
                }

                val attachedFileName = pickedAttachment?.fileName
                val hasAttachment = !attachedFileName.isNullOrBlank()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (hasAttachment) Color(0xFFECEBFF) else Color.Transparent)
                        .border(2.dp, if (hasAttachment) Color(0xFF5B6CF0) else Color(0xFFC9CAE6), RoundedCornerShape(14.dp))
                        .clickable { viewModel?.pickMediaAttachment() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (hasAttachment) "Adjunto: $attachedFileName" else "Adjuntar comprobante",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sendWhatsAppReceipt = !sendWhatsAppReceipt }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Enviar recibo a $clientFirstName ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        BasicText("por WhatsApp", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(if (sendWhatsAppReceipt) AdminButtonGradient else SolidColor(Color(0xFFDCDDEA)))
                            .padding(3.dp),
                        contentAlignment = if (sendWhatsAppReceipt) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                    }
                }

                val activeError = localErrorText ?: formError
                if (!activeError.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDE3EA))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        BasicText(
                            text = activeError,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable {
                            val cleanRef = referenceText.trim()
                            val needsRef = selectedMethod != "Efectivo"
                            val missingRequiredRef = bothConditions(needsRef, cleanRef.length < 3)
                            if (parsedAmountDop <= 0) {
                                localErrorText = "Ingresa un monto mayor que RD$ 0."
                            } else if (missingRequiredRef) {
                                localErrorText = "Ingresa el código o referencia bancaria del pago."
                            } else {
                                localErrorText = null
                                val activeVm = viewModel
                                if (activeVm != null) {
                                    activeVm.selectEvent(chosenEvent.id)
                                    activeVm.setPaymentAmountInput(parsedAmountDop.toString())
                                    activeVm.setPaymentMethodInput("$selectedMethod · $paymentDate")
                                    activeVm.setPaymentRefInput(cleanRef.ifBlank { "EFE-CAJA" })
                                    val recorded = activeVm.submitPayment(chosenEvent.id)
                                    if (recorded != null) {
                                        if (sendWhatsAppReceipt) {
                                            activeVm.exportReceiptDocument(
                                                event = chosenEvent,
                                                payment = recorded,
                                                shareInsteadOfSave = true,
                                            )
                                        }
                                        onSelectScreenCode("A20")
                                    }
                                } else {
                                    onSelectScreenCode("A20")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

// ==============================================================================
// A13 · DETALLE DEL EVENTO (ADMIN)
// ==============================================================================
@Composable
internal fun RenderAdminEventDetailA13(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = eventsList.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: viewModel?.activeClientEvent()
        ?: eventsList.firstOrNull()
        ?: CanonicalSampleData.events.first()
    val checklistList = viewModel?.checklist?.collectAsState()?.value ?: emptyList()
    val eventTasks = viewModel?.eventTasksList?.collectAsState()?.value ?: emptyList()
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value

    val clientName = selectedEvent.clientName.ifBlank { "Camila Rodríguez" }
    val clientInitial = clientName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
    val paidDop = selectedEvent.paidDepositDop
    val remainingDop = (selectedEvent.totalDop - paidDop).coerceAtLeast(0)
    val safeTotalForProgress = selectedEvent.totalDop.coerceAtLeast(1)
    val paidFraction = (paidDop.toFloat() / safeTotalForProgress.toFloat()).coerceIn(0.05f, 1f)

    val rentedItems = selectedEvent.items.ifEmpty { CanonicalSampleData.events.first().items }
    val itemTypesCount = rentedItems.size
    val totalRentedUnits = rentedItems.sumOf { lineItem -> lineItem.quantity }

    val matchingChecklist = checklistList.filter { stepItem -> stepItem.eventId == selectedEvent.id }
    val activeChecklist = matchingChecklist.ifEmpty { checklistList }
    val doneStepsCount = activeChecklist.count { stepItem -> stepItem.completed } + eventTasks.count { taskItem -> taskItem.completed }
    val totalStepsCount = (activeChecklist.size + eventTasks.size).coerceAtLeast(6)
    val tasksProgressSubtitle = if (activeChecklist.isEmpty() == false) {
        "$doneStepsCount de $totalStepsCount hechas"
    } else {
        "3 de 6 hechas"
    }

    var showMoreOptionsModal by remember { mutableStateOf(false) }
    var showRentedItemsModal by remember { mutableStateOf(false) }

    val heroPhotoKey = photoKeyForEventType(selectedEvent.title)
    val daysBadgeText = when {
        selectedEvent.daysUntilEvent == 0 -> "Hoy"
        selectedEvent.daysUntilEvent == 1 -> "Mañana"
        selectedEvent.daysUntilEvent > 1 -> "En ${selectedEvent.daysUntilEvent} días"
        else -> formatReadableSpanishDateText(selectedEvent.dateLabel)
    }
    val executionCtaBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)))

    val openClientChat: () -> Unit = {
        viewModel?.selectChatThreadForContact(clientName, selectedEvent.id)
        onSelectScreenCode("A15")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        .clickable { onSelectScreenCode("BACK:A11") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = selectedEvent.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "${formatReadableSpanishDateText(selectedEvent.dateLabel)} · ${selectedEvent.venue}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { showMoreOptionsModal = true },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            if (!formFeedback.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BasicText(
                        text = formFeedback,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(20.dp)),
            ) {
                RfPhoto(photoKey = heroPhotoKey, contentDescription = selectedEvent.title, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(0.3f to Color.Transparent, 1.0f to Color(0x8C151729))),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD45C))
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(daysBadgeText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E1A47)))
                    }

                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDF4F3))
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(selectedEvent.statusLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F8A84)))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDE6F2)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = clientName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "${selectedEvent.guests} invitados · ${selectedEvent.venue}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .clickable { openClientChat() },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF151729), size = 20.dp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Pagos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        Row(verticalAlignment = Alignment.Bottom) {
                            BasicText("Total ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            BasicText(formatAdminDop(selectedEvent.totalDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEDEDF5)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(paidFraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AdminButtonGradient),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Row {
                                BasicText("Pagado ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                BasicText(formatAdminDop(paidDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            }
                            BasicText("Faltan ${formatAdminDop(remainingDop)}", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7A5700)))
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(CircleShape)
                                .background(AdminButtonGradient)
                                .clickable {
                                    viewModel?.selectEvent(selectedEvent.id)
                                    onSelectScreenCode("A21")
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 14.dp)
                                BasicText("Pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { showRentedItemsModal = true }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF4E6A10), size = 18.dp)
                        }
                        Column {
                            BasicText("Artículos alquilados", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText("$itemTypesCount tipos · $totalRentedUnits unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.selectEvent(selectedEvent.id)
                            onSelectScreenCode("A17")
                        }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECEBFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHECKLIST, tint = Color(0xFF4B47D6), size = 18.dp)
                        }
                        Column {
                            BasicText("Tareas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText(tasksProgressSubtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.selectEvent(selectedEvent.id)
                            onSelectScreenCode("A18")
                        }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.COPY, tint = Color(0xFF7A5700), size = 18.dp)
                        }
                        Column {
                            BasicText("Boceto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText("Ver plano", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { openClientChat() }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF1F8A84), size = 18.dp)
                        }
                        Column {
                            BasicText("Chat", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText(clientName.substringBefore(" "), maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .clickable {
                        viewModel?.selectEvent(selectedEvent.id)
                        onSelectScreenCode("A19")
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFFC2338F), size = 18.dp)
                        }
                        Column {
                            BasicText("Retorno e inspección final", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText("Revisar devolución de mobiliario y cierre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(executionCtaBrush)
                    .clickable {
                        viewModel?.selectEvent(selectedEvent.id)
                        onSelectScreenCode("A16")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.PLAY, tint = Color.White, size = 20.dp, filled = true)
                    BasicText("Modo ejecución", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        if (showMoreOptionsModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73151729))
                    .clickable { showMoreOptionsModal = false }
                    .padding(20.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Opciones del evento", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable { showMoreOptionsModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }

                    val optionEntries = listOf(
                        Triple("Duplicar evento", "Crear copia editable en agenda", RfIconKind.COPY) to {
                            val duplicatedEvent = viewModel?.duplicateExistingEvent(selectedEvent.id)
                            if (duplicatedEvent != null) {
                                viewModel.selectEvent(duplicatedEvent.id)
                            }
                            showMoreOptionsModal = false
                            onSelectScreenCode("A11")
                        },
                        Triple("Descargar orden PDF", "Guardar orden operativa del evento", RfIconKind.SEND) to {
                            val generatedFile = viewModel?.exportReceiptDocument(selectedEvent, null, shareInsteadOfSave = false)
                            if (generatedFile != null) {
                                viewModel.postFeedback("Orden PDF descargada: $generatedFile")
                            }
                            showMoreOptionsModal = false
                        },
                        Triple("Registrar pago", "Abonar anticipo o saldo pendiente", RfIconKind.PLUS) to {
                            viewModel?.selectEvent(selectedEvent.id)
                            showMoreOptionsModal = false
                            onSelectScreenCode("A21")
                        },
                        Triple("Inspección y cierre", "Revisar devolución y daños", RfIconKind.CHECKLIST) to {
                            viewModel?.selectEvent(selectedEvent.id)
                            showMoreOptionsModal = false
                            onSelectScreenCode("A19")
                        },
                    )

                    optionEntries.forEach { optionPair ->
                        val meta = optionPair.first
                        val actionHandler = optionPair.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF7F7FC))
                                .clickable { actionHandler() }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RfIcon(kind = meta.third, tint = Color(0xFF4B47D6), size = 18.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(meta.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                                BasicText(meta.second, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }
                }
            }
        }

        if (showRentedItemsModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73151729))
                    .clickable { showRentedItemsModal = false }
                    .padding(20.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Artículos alquilados", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            BasicText("$itemTypesCount líneas · $totalRentedUnits unidades reservadas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable { showRentedItemsModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }

                    rentedItems.forEach { rentedLine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF7F7FC))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RfPhoto(
                                photoKey = photoKeyForAsset(rentedLine.photo),
                                contentDescription = rentedLine.name,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = rentedLine.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                )
                                BasicText(
                                    text = "${rentedLine.articleCode} · ${rentedLine.quantity} unid.",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            BasicText(
                                text = formatAdminDop(rentedLine.subtotalDop),
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AdminButtonGradient)
                            .clickable { showRentedItemsModal = false },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Cerrar detalle", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

private data class ExecutionStageRow(
    val rowKey: String,
    val domainStepId: String?,
    val stageIndex: Int,
    val done: Boolean,
    val title: String,
    val subtitle: String,
    val avatarLetter: String,
    val avatarColors: Pair<Color, Color>,
)

private fun classifyChecklistStageIndex(rawStageLabel: String): Int {
    val normalized = rawStageLabel.trim().lowercase()
    val isLoadKeyword = eitherCondition(normalized.contains("carga"), normalized.contains("camión"))
    val isWarehouseKeyword = eitherCondition(normalized.contains("almac"), normalized.contains("prepar"))
    val isPreKeyword = eitherCondition(normalized.contains("logíst"), normalized.contains("prev"))
    val matchesLoadGroup = eitherCondition(eitherCondition(isLoadKeyword, isWarehouseKeyword), isPreKeyword)
    val isTeardownKeyword = eitherCondition(normalized.contains("desmont"), normalized.contains("recog"))
    val isCloseoutKeyword = eitherCondition(normalized.contains("retir"), normalized.contains("cierre"))
    val matchesTeardownGroup = eitherCondition(isTeardownKeyword, isCloseoutKeyword)
    return when {
        matchesLoadGroup -> 0
        matchesTeardownGroup -> 2
        else -> 1
    }
}

private fun resolveAssigneeAvatarPalette(rawAssignee: String): Pair<String, Pair<Color, Color>> {
    val cleanName = rawAssignee.trim()
    val initial = cleanName.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
    val normalized = cleanName.lowercase()
    val colors = when {
        normalized.startsWith("p") -> Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84))
        normalized.startsWith("y") -> Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))
        normalized.startsWith("a") -> Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))
        else -> Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))
    }
    return Pair(initial, colors)
}

@Composable
internal fun RenderAdminExecutionA16(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = eventsList.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: viewModel?.activeClientEvent()
        ?: eventsList.firstOrNull()
        ?: CanonicalSampleData.events.first()
    val deliveryStops = viewModel?.deliveryStops?.collectAsState()?.value ?: emptyList()
    val allChecklist = viewModel?.checklist?.collectAsState()?.value ?: emptyList()

    val matchingStop = deliveryStops.firstOrNull { stopItem ->
        val matchesId = bothConditions(stopItem.eventId.isNotBlank(), stopItem.eventId == selectedEvent.id)
        val matchesTitle = stopItem.eventTitle.equals(selectedEvent.title, ignoreCase = true)
        eitherCondition(matchesId, matchesTitle)
    } ?: deliveryStops.firstOrNull()

    val eventSpecificChecklist = allChecklist.filter { stepItem -> stepItem.eventId == selectedEvent.id }
    val activeChecklist = eventSpecificChecklist.ifEmpty { allChecklist }

    var selectedPhaseIndex by remember { mutableStateOf(1) }
    var fallbackDoneByKey by remember(selectedEvent.id) {
        mutableStateOf(
            mapOf(
                "load-0" to true,
                "load-1" to true,
                "load-2" to true,
                "montage-0" to true,
                "montage-1" to true,
                "montage-2" to false,
                "montage-3" to false,
                "teardown-0" to false,
                "teardown-1" to false,
                "teardown-2" to false,
            ),
        )
    }
    var uploadedPhotos by remember(selectedEvent.id) { mutableStateOf(listOf<String>()) }

    val domainStageRows = activeChecklist.map { stepItem ->
        val stageIdx = classifyChecklistStageIndex(stepItem.stageLabel)
        val assigneeLabel = stepItem.completedBy.ifBlank { "Equipo" }
        val avatarInfo = resolveAssigneeAvatarPalette(assigneeLabel)
        ExecutionStageRow(
            rowKey = "domain-${stepItem.id}",
            domainStepId = stepItem.id,
            stageIndex = stageIdx,
            done = stepItem.completed,
            title = stepItem.title,
            subtitle = "${stepItem.stageLabel} · ${stepItem.timeLabel}",
            avatarLetter = avatarInfo.first,
            avatarColors = avatarInfo.second,
        )
    }

    val loadRows = domainStageRows.filter { rowItem -> rowItem.stageIndex == 0 }.ifEmpty {
        listOf(
            ExecutionStageRow("load-0", null, 0, fallbackDoneByKey["load-0"] ?: true, "Verificar orden de carga en almacén", "Conteo de sillas, mesas y mantelería", "A", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))),
            ExecutionStageRow("load-1", null, 0, fallbackDoneByKey["load-1"] ?: true, "Proteger estructuras y bases florales", "Embalaje acolchado en camión", "P", Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84))),
            ExecutionStageRow("load-2", null, 0, fallbackDoneByKey["load-2"] ?: true, "Salida de ruta hacia el salón", matchingStop?.vehicleAndCrew ?: "Camión principal · cuadrilla lista", "A", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))),
        )
    }

    val montageRows = domainStageRows.filter { rowItem -> rowItem.stageIndex == 1 }.ifEmpty {
        listOf(
            ExecutionStageRow("montage-0", null, 1, fallbackDoneByKey["montage-0"] ?: true, "Colocar mesas según plano aprobado", "${selectedEvent.guests} invitados · distribución validada", "P", Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84))),
            ExecutionStageRow("montage-1", null, 1, fallbackDoneByKey["montage-1"] ?: true, "Vestir mesas y sillas Tiffany", "Mantelería blanca y lazos alineados", "Y", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))),
            ExecutionStageRow("montage-2", null, 1, fallbackDoneByKey["montage-2"] ?: false, "Montar arco principal y fondo", "Entrada principal · nivelación segura", "Y", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))),
            ExecutionStageRow("montage-3", null, 1, fallbackDoneByKey["montage-3"] ?: false, "Ubicar centros de mesa florales", "Revisión final de iluminación y detalles", "M", Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))),
        )
    }

    val teardownRows = domainStageRows.filter { rowItem -> rowItem.stageIndex == 2 }.ifEmpty {
        listOf(
            ExecutionStageRow("teardown-0", null, 2, fallbackDoneByKey["teardown-0"] ?: false, "Recoger bases florales y cristalería", "Separar piezas frágiles en cajas", "Y", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))),
            ExecutionStageRow("teardown-1", null, 2, fallbackDoneByKey["teardown-1"] ?: false, "Plegar y contar sillas y mesas", "Verificar estado antes de subir", "P", Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84))),
            ExecutionStageRow("teardown-2", null, 2, fallbackDoneByKey["teardown-2"] ?: false, "Retorno a almacén y cierre de ruta", "Entrega de llaves y acta firmada", "A", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))),
        )
    }

    val allStageRows = loadRows + montageRows + teardownRows
    val totalCompletedCount = allStageRows.count { rowItem -> rowItem.done }
    val totalRowCount = allStageRows.size.coerceAtLeast(1)
    val loadDoneCount = loadRows.count { rowItem -> rowItem.done }
    val montageDoneCount = montageRows.count { rowItem -> rowItem.done }
    val teardownDoneCount = teardownRows.count { rowItem -> rowItem.done }

    val displayedStageRows = when (selectedPhaseIndex) {
        0 -> loadRows
        2 -> teardownRows
        else -> montageRows
    }
    val activePhaseBadgeLabel = when (selectedPhaseIndex) {
        0 -> "Hoy · Carga"
        2 -> "Hoy · Desmontaje"
        else -> "Hoy · Montaje"
    }
    val venueTitle = matchingStop?.venue?.ifBlank { selectedEvent.venue } ?: selectedEvent.venue
    val venueSubtitle = if (matchingStop != null) {
        "${matchingStop.windowLabel} · ${matchingStop.vehicleAndCrew} · ${selectedEvent.clientName}"
    } else {
        "${formatReadableSpanishDateText(selectedEvent.dateLabel)} · Contacto: ${selectedEvent.clientName}"
    }
    val headerHeroBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 112.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(headerHeroBrush)
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(Color(0x2EFFFFFF))
                                .clickable { onSelectScreenCode("BACK:A13") },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color.White, size = 22.dp)
                        }

                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF2E1A47)))
                                BasicText(activePhaseBadgeLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47)))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(78.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Color(0x38FFFFFF), style = Stroke(width = 8.dp.toPx()))
                                drawArc(
                                    color = Color(0xFFFFD45C),
                                    startAngle = -90f,
                                    sweepAngle = (totalCompletedCount.toFloat() / totalRowCount.toFloat()) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                                )
                            }
                            BasicText("$totalCompletedCount/$totalRowCount", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = selectedEvent.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText(
                                text = "${matchingStop?.windowLabel ?: formatReadableSpanishDateText(selectedEvent.dateLabel)} · $venueTitle",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEDEDF5))
                        .padding(4.dp),
                ) {
                    val phaseLabels = listOf(
                        "Carga · $loadDoneCount/${loadRows.size}",
                        "Montaje · $montageDoneCount/${montageRows.size}",
                        "Desmontaje · $teardownDoneCount/${teardownRows.size}",
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        phaseLabels.forEachIndexed { phaseIndex, phaseLabel ->
                            val isSelectedPhase = selectedPhaseIndex == phaseIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .then(
                                        if (isSelectedPhase) {
                                            Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White)
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .clickable { selectedPhaseIndex = phaseIndex },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = phaseLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelectedPhase) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelectedPhase) Color(0xFF2E324A) else Color(0xFF5F6377),
                                    ),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF1F8A84), size = 20.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = venueTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                            )
                            BasicText(
                                text = venueSubtitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFECEBFF))
                                .clickable { viewModel?.openMapsSearch(venueTitle) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Mapa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                        }
                    }
                }

                displayedStageRows.forEach { stageRow ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (stageRow.done) Color(0xFFF6F6FB) else Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .clickable {
                                val stepId = stageRow.domainStepId
                                if (stepId != null) {
                                    viewModel?.toggleChecklistStep(stepId)
                                } else {
                                    val updatedMap = fallbackDoneByKey.toMutableMap()
                                    val currentFlag = updatedMap[stageRow.rowKey] ?: stageRow.done
                                    updatedMap[stageRow.rowKey] = !currentFlag
                                    fallbackDoneByKey = updatedMap
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (stageRow.done) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF8DB52C)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .border(2.dp, Color(0xFFC9CAE6), RoundedCornerShape(10.dp)),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = stageRow.title,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 15.sp,
                                        fontWeight = if (stageRow.done) FontWeight.Normal else FontWeight.Medium,
                                        color = if (stageRow.done) Color(0xFF5F6377) else Color(0xFF2E324A),
                                        textDecoration = if (stageRow.done) TextDecoration.LineThrough else null,
                                    ),
                                )
                                BasicText(
                                    text = stageRow.subtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(stageRow.avatarColors.first),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(stageRow.avatarLetter, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = stageRow.avatarColors.second))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(18.dp))
                        .clickable {
                            if (viewModel != null) {
                                viewModel.pickMediaAttachment(listOf("jpg", "jpeg", "png")) { pickedFile ->
                                    val resolvedName = pickedFile.fileName.ifBlank { "montaje-${uploadedPhotos.size + 1}.jpg" }
                                    if (!uploadedPhotos.contains(resolvedName)) {
                                        uploadedPhotos = uploadedPhotos + resolvedName
                                    }
                                }
                            } else {
                                val nextDemoPhoto = "montaje-${uploadedPhotos.size + 1}.jpg"
                                uploadedPhotos = uploadedPhotos + nextDemoPhoto
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECEBFF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 18.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText("Subir fotos del montaje", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                                BasicText(
                                    text = if (uploadedPhotos.isEmpty()) "Adjuntar evidencia visual de la etapa" else "${uploadedPhotos.size} foto(s) adjuntada(s)",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                            }
                        }

                        if (uploadedPhotos.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                uploadedPhotos.forEach { photoName ->
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFEEF6D6))
                                            .clickable {
                                                uploadedPhotos = uploadedPhotos.filterNot { itemPhoto -> itemPhoto == photoName }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        BasicText(
                                            text = "$photoName ×",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable {
                        viewModel?.postFeedback("Avance operativo actualizado ($totalCompletedCount/$totalRowCount).")
                        onSelectScreenCode("A13")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                    BasicText("Guardar avance y volver", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

private data class TeamTaskDisplayEntry(
    val entryKey: String,
    val checklistStepId: String?,
    val eventTaskId: String?,
    val groupHeader: String,
    val title: String,
    val subtitle: String,
    val assigneeName: String,
    val completed: Boolean,
)

private fun resolveStaffFirstNamesForTasks(staffMembers: List<StaffMember>): List<String> {
    val defaultNames = listOf("María", "Pedro", "Yuli", "Andrés")
    val dynamicNames = staffMembers.map { staffItem ->
        staffItem.name.trim().substringBefore(" ")
    }.filter { firstName -> firstName.isNotEmpty() }
    val combined = defaultNames + dynamicNames
    val uniqueList = mutableListOf<String>()
    combined.forEach { candidateName ->
        val alreadyExists = uniqueList.any { existingName -> existingName.equals(candidateName, ignoreCase = true) }
        if (!alreadyExists) {
            uniqueList.add(candidateName)
        }
    }
    return uniqueList
}

@Composable
internal fun RenderAdminTasksA17(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = eventsList.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: viewModel?.activeClientEvent()
        ?: eventsList.firstOrNull()
        ?: CanonicalSampleData.events.first()
    val allChecklist = viewModel?.checklist?.collectAsState()?.value ?: emptyList()
    val eventTasks = viewModel?.eventTasksList?.collectAsState()?.value ?: emptyList()
    val staffMembers = viewModel?.staffMembers?.collectAsState()?.value ?: emptyList()

    val assigneeOptions = resolveStaffFirstNamesForTasks(staffMembers)

    var selectedMemberChip by remember { mutableStateOf("Todas") }
    var showNewTaskModal by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedTaskAssignee by remember(assigneeOptions) {
        mutableStateOf(assigneeOptions.firstOrNull() ?: "María")
    }
    var modalValidationError by remember { mutableStateOf<String?>(null) }

    var localFallbackTasks by remember(selectedEvent.id) {
        mutableStateOf(
            listOf(
                TeamTaskDisplayEntry("fb-1", null, null, "HOY · COORDINACIÓN", "Confirmar paleta floral", "10:00 a. m.", "Yuli", true),
                TeamTaskDisplayEntry("fb-2", null, null, "HOY · COORDINACIÓN", "Recordar saldo pendiente al cliente", "5:00 p. m.", "María", false),
                TeamTaskDisplayEntry("fb-3", null, null, "VÍSPERA · TALLER", "Preparar estructuras y arcos", "3:00 p. m. · en el taller", "Yuli", false),
                TeamTaskDisplayEntry("fb-4", null, null, "VÍSPERA · TALLER", "Revisar y limpiar sillas y mesas", "4:00 p. m.", "Pedro", false),
                TeamTaskDisplayEntry("fb-5", null, null, "DÍA DEL EVENTO", "Cargar el camión principal", "12:30 p. m.", "Andrés", false),
                TeamTaskDisplayEntry("fb-6", null, null, "DÍA DEL EVENTO", "Montaje completo en salón", "2:00 – 5:00 p. m.", "Pedro", false),
            ),
        )
    }

    val matchingChecklist = allChecklist.filter { stepItem -> stepItem.eventId == selectedEvent.id }
    val activeChecklist = matchingChecklist.ifEmpty { allChecklist }

    val domainChecklistEntries = activeChecklist.mapIndexed { stepIdx, stepItem ->
        val fallbackAssignee = assigneeOptions[stepIdx % assigneeOptions.size.coerceAtLeast(1)]
        val resolvedAssignee = stepItem.completedBy.trim().ifBlank { fallbackAssignee }.substringBefore(" ")
        TeamTaskDisplayEntry(
            entryKey = "chk-${stepItem.id}",
            checklistStepId = stepItem.id,
            eventTaskId = null,
            groupHeader = stepItem.stageLabel.uppercase(),
            title = stepItem.title,
            subtitle = stepItem.timeLabel,
            assigneeName = resolvedAssignee,
            completed = stepItem.completed,
        )
    }

    val customTaskEntries = eventTasks.map { taskItem ->
        val rawBadge = taskItem.badgeText.trim()
        val matchesKnownStaff = assigneeOptions.firstOrNull { staffName ->
            rawBadge.contains(staffName, ignoreCase = true)
        }
        val resolvedAssignee = matchesKnownStaff ?: assigneeOptions.firstOrNull() ?: "María"
        TeamTaskDisplayEntry(
            entryKey = "evt-${taskItem.id}",
            checklistStepId = null,
            eventTaskId = taskItem.id,
            groupHeader = "TAREAS DEL EQUIPO",
            title = taskItem.title,
            subtitle = "Asignada a $resolvedAssignee · ${rawBadge.ifBlank { "Pendiente" }}",
            assigneeName = resolvedAssignee,
            completed = taskItem.completed,
        )
    }

    val baseEntries = if (domainChecklistEntries.isNotEmpty()) domainChecklistEntries else localFallbackTasks
    val combinedEntries = customTaskEntries + baseEntries
    val filteredEntries = combinedEntries.filter { entryItem ->
        val showAll = selectedMemberChip == "Todas"
        val matchesAssignee = entryItem.assigneeName.equals(selectedMemberChip, ignoreCase = true)
        eitherCondition(showAll, matchesAssignee)
    }
    val groupedEntries = filteredEntries.groupBy { entryItem -> entryItem.groupHeader }
    val activeChipGradient = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
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
                        .clickable { onSelectScreenCode("BACK:A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Tareas del equipo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = selectedEvent.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                        .clip(CircleShape)
                        .background(activeChipGradient)
                        .clickable {
                            modalValidationError = null
                            newTaskTitle = ""
                            showNewTaskModal = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val isAllSelected = selectedMemberChip == "Todas"
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isAllSelected) {
                                Modifier.background(activeChipGradient)
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            },
                        )
                        .clickable { selectedMemberChip = "Todas" }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Todas",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isAllSelected) Color.White else Color(0xFF2E324A),
                        ),
                    )
                }

                assigneeOptions.forEach { memberName ->
                    val palette = resolveAssigneeAvatarPalette(memberName)
                    val isMemberSelected = selectedMemberChip.equals(memberName, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .then(
                                if (isMemberSelected) {
                                    Modifier.background(activeChipGradient)
                                } else {
                                    Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                                },
                            )
                            .clickable { selectedMemberChip = memberName }
                            .padding(start = 5.dp, end = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(palette.second.first),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(palette.first, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = palette.second.second))
                            }
                            BasicText(
                                text = memberName,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    fontWeight = if (isMemberSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isMemberSelected) Color.White else Color(0xFF2E324A),
                                ),
                            )
                        }
                    }
                }
            }

            if (groupedEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Sin tareas asignadas a $selectedMemberChip en este evento.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            groupedEntries.forEach { groupPair ->
                val sectionTitle = groupPair.key
                val sectionTasks = groupPair.value

                BasicText(
                    text = sectionTitle,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                    modifier = Modifier.padding(top = 2.dp),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
                ) {
                    Column {
                        sectionTasks.forEachIndexed { taskIndex, taskEntry ->
                            if (taskIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            }
                            val avatarMeta = resolveAssigneeAvatarPalette(taskEntry.assigneeName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val stepId = taskEntry.checklistStepId
                                        val evtTaskId = taskEntry.eventTaskId
                                        if (stepId != null) {
                                            viewModel?.toggleChecklistStep(stepId)
                                        } else if (evtTaskId != null) {
                                            viewModel?.toggleEventTask(evtTaskId)
                                        } else {
                                            localFallbackTasks = localFallbackTasks.map { existingRow ->
                                                if (existingRow.entryKey == taskEntry.entryKey) {
                                                    existingRow.copy(completed = !existingRow.completed)
                                                } else {
                                                    existingRow
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (taskEntry.completed) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF8DB52C)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = taskEntry.title,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (taskEntry.completed) Color(0xFF5F6377) else Color(0xFF151729),
                                            textDecoration = if (taskEntry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                        ),
                                    )
                                    BasicText(
                                        text = taskEntry.subtitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(avatarMeta.second.first),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(avatarMeta.first, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = avatarMeta.second.second))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNewTaskModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73151729))
                    .clickable { showNewTaskModal = false }
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Nueva tarea de equipo", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable { showNewTaskModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText("Título de la tarea", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A3D52)))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF7F7FC))
                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (newTaskTitle.isEmpty()) {
                                BasicText("Ej. Verificar mantelería dorada", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF8A8FA6)))
                            }
                            BasicTextField(
                                value = newTaskTitle,
                                onValueChange = { updatedText -> newTaskTitle = updatedText },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText("Responsable", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A3D52)))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            assigneeOptions.forEach { assigneeOption ->
                                val isAssigneeSelected = selectedTaskAssignee.equals(assigneeOption, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isAssigneeSelected) {
                                                Modifier.background(activeChipGradient)
                                            } else {
                                                Modifier.background(Color(0xFFF2F2F8))
                                            },
                                        )
                                        .clickable { selectedTaskAssignee = assigneeOption }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = assigneeOption,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isAssigneeSelected) Color.White else Color(0xFF2E324A),
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    if (!modalValidationError.isNullOrBlank()) {
                        BasicText(
                            text = modalValidationError ?: "",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(activeChipGradient)
                            .clickable {
                                val cleanTitle = newTaskTitle.trim()
                                if (cleanTitle.isEmpty()) {
                                    modalValidationError = "Escribe el nombre de la tarea para asignarla."
                                } else {
                                    modalValidationError = null
                                    if (viewModel != null) {
                                        viewModel.addEventTask(cleanTitle, selectedTaskAssignee)
                                    } else {
                                        val createdKey = "fb-new-${localFallbackTasks.size + 1}"
                                        val addedEntry = TeamTaskDisplayEntry(
                                            entryKey = createdKey,
                                            checklistStepId = null,
                                            eventTaskId = null,
                                            groupHeader = "TAREAS DEL EQUIPO",
                                            title = cleanTitle,
                                            subtitle = "Asignada a $selectedTaskAssignee",
                                            assigneeName = selectedTaskAssignee,
                                            completed = false,
                                        )
                                        localFallbackTasks = listOf(addedEntry) + localFallbackTasks
                                    }
                                    newTaskTitle = ""
                                    showNewTaskModal = false
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Crear tarea", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

// ==============================================================================
// A18 · PLANO Y DISTRIBUCIÓN
// ==============================================================================
private fun buildSketchBlueprintJson(
    eventId: String,
    tableCount: Int,
    guestsCount: Int,
    viewMode: String,
): String {
    val chairsCount = tableCount * 10
    return "{\"eventId\":\"$eventId\",\"mode\":\"$viewMode\",\"roomMeters\":\"12.0x8.0\",\"corridorMeters\":1.8,\"powerOutlets\":\"110V\",\"tables\":$tableCount,\"chairs\":$chairsCount,\"guests\":$guestsCount}"
}

private fun buildTableCanvasOffsets(tableCount: Int): List<Offset> {
    val candidateSlots = listOf(
        Offset(82f, 150f),
        Offset(179f, 150f),
        Offset(276f, 150f),
        Offset(74f, 232f),
        Offset(284f, 232f),
        Offset(74f, 314f),
        Offset(284f, 314f),
        Offset(125f, 376f),
        Offset(233f, 376f),
        Offset(125f, 206f),
        Offset(233f, 206f),
        Offset(179f, 376f),
        Offset(56f, 108f),
        Offset(302f, 108f),
        Offset(56f, 376f),
        Offset(302f, 376f),
    )
    val clampedCount = tableCount.coerceIn(1, candidateSlots.size)
    return candidateSlots.take(clampedCount)
}

@Composable
internal fun RenderAdminSketchA18(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = eventsList.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: viewModel?.activeClientEvent()
        ?: eventsList.firstOrNull()
        ?: CanonicalSampleData.events.first()
    val vmTableCount = viewModel?.sketchTableCount?.collectAsState()?.value
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value

    var fallbackTableCount by remember(selectedEvent.id) {
        val suggestedTables = ((selectedEvent.guests + 9) / 10).coerceIn(4, 16)
        mutableStateOf(suggestedTables)
    }
    var selectedViewMode by remember { mutableStateOf("Plano") }
    var localSaveNotice by remember { mutableStateOf<String?>(null) }

    val activeTableCount = (vmTableCount ?: fallbackTableCount).coerceIn(1, 16)
    val calculatedSeats = activeTableCount * 10
    val clientFirstName = selectedEvent.clientName.trim().substringBefore(" ").ifBlank { "Camila" }
    val isOriginalSelected = selectedViewMode == "Boceto original"
    val isPlanoSelected = selectedViewMode == "Plano"
    val primaryActionBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    val adjustTablesByDelta: (Int) -> Unit = { delta ->
        localSaveNotice = null
        if (viewModel != null) {
            viewModel.adjustSketchTables(delta)
        } else {
            fallbackTableCount = (fallbackTableCount + delta).coerceIn(1, 16)
        }
    }

    val saveCurrentBlueprint: () -> Unit = {
        val payloadJson = buildSketchBlueprintJson(
            eventId = selectedEvent.id,
            tableCount = activeTableCount,
            guestsCount = selectedEvent.guests,
            viewMode = selectedViewMode,
        )
        if (viewModel != null) {
            viewModel.saveSketchLayoutJson(payloadJson, selectedEvent.id)
            viewModel.postFeedback("Plano guardado: $activeTableCount mesas ($calculatedSeats sillas).")
        } else {
            localSaveNotice = "Plano guardado: $activeTableCount mesas ($calculatedSeats sillas)."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
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
                        .clickable { onSelectScreenCode("BACK:A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boceto de $clientFirstName",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "${selectedEvent.title} · ${selectedEvent.venue}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEDEDF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (isOriginalSelected) {
                                Modifier.shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4)).clip(RoundedCornerShape(12.dp)).background(Color.White)
                            } else {
                                Modifier
                            },
                        )
                        .clickable { selectedViewMode = "Boceto original" },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Boceto original",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isOriginalSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isOriginalSelected) Color(0xFF2E324A) else Color(0xFF5F6377),
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (isPlanoSelected) {
                                Modifier.shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4)).clip(RoundedCornerShape(12.dp)).background(Color.White)
                            } else {
                                Modifier
                            },
                        )
                        .clickable { selectedViewMode = "Plano" },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Plano",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isPlanoSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isPlanoSelected) Color(0xFF2E324A) else Color(0xFF5F6377),
                        ),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "$activeTableCount mesas · $calculatedSeats sillas",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Meta del evento: ${selectedEvent.guests} invitados",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F8))
                            .clickable { adjustTablesByDelta(-1) },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("−", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    }
                    BasicText(
                        text = "$activeTableCount",
                        modifier = Modifier.widthIn(min = 24.dp),
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), textAlign = TextAlign.Center),
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(primaryActionBrush)
                            .clickable { adjustTablesByDelta(1) },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("+", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            if (isOriginalSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFFFF8FB), Color(0xFFF7F5FF))))
                        .border(1.dp, Color(0xFFF3DCEB), RoundedCornerShape(20.dp)),
                ) {
                    val tablePositions = buildTableCanvasOffsets(activeTableCount)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scaleX = size.width / 358f
                        val scaleY = size.height / 440f
                        scale(scaleX, scaleY, Offset.Zero) {
                            drawArc(
                                color = Color(0xFFFAD1E6),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = true,
                                topLeft = Offset(86f, 22f),
                                size = Size(186f, 92f),
                            )
                            drawArc(
                                color = Color(0xFFC2338F),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(86f, 22f),
                                size = Size(186f, 92f),
                                style = Stroke(width = 3f, cap = StrokeCap.Round),
                            )

                            drawRoundRect(
                                color = Color(0xFFFFF2C9),
                                topLeft = Offset(246f, 34f),
                                size = Size(92f, 44f),
                                cornerRadius = CornerRadius(12f, 12f),
                            )
                            drawRoundRect(
                                color = Color(0xFFD9A441),
                                topLeft = Offset(246f, 34f),
                                size = Size(92f, 44f),
                                cornerRadius = CornerRadius(12f, 12f),
                                style = Stroke(width = 2f),
                            )

                            drawRoundRect(
                                color = Color(0xFFEEF6D6),
                                topLeft = Offset(128f, 236f),
                                size = Size(102f, 88f),
                                cornerRadius = CornerRadius(16f, 16f),
                            )

                            tablePositions.forEach { tableCenter ->
                                drawCircle(color = Color(0xFFFDE6F2), radius = 21f, center = tableCenter)
                                drawCircle(color = Color(0xFFC2338F), radius = 21f, center = tableCenter, style = Stroke(width = 2f))
                                for (chairIndex in 0 until 10) {
                                    val angle = chairIndex * (kotlin.math.PI * 2 / 10)
                                    val chairX = tableCenter.x + kotlin.math.cos(angle).toFloat() * 29f
                                    val chairY = tableCenter.y + kotlin.math.sin(angle).toFloat() * 29f
                                    drawCircle(color = Color(0xFFFFD45C), radius = 3.5f, center = Offset(chairX, chairY))
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = 14.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.92f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            BasicText("Arco floral rosa & dorado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(y = 20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF8E5))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            BasicText("Estación de postres", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                        }
                        BasicText(
                            text = "Pista central",
                            modifier = Modifier.align(Alignment.Center).offset(y = 32.dp),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                        )
                        Row(
                            modifier = Modifier.align(Alignment.BottomStart),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFF3DCEB), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                BasicText("Vista conceptual decorativa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC2338F)))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE3E4EF), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                BasicText("${selectedEvent.guests} invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF4F7FF))
                        .border(1.5.dp, Color(0xFFBBC6F2), RoundedCornerShape(20.dp)),
                ) {
                    val tablePositions = buildTableCanvasOffsets(activeTableCount)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scaleX = size.width / 358f
                        val scaleY = size.height / 440f
                        scale(scaleX, scaleY, Offset.Zero) {
                            for (gridX in 1..8) {
                                val xCoord = gridX * 40f
                                drawLine(
                                    color = Color(0xFFDCE3FA),
                                    start = Offset(xCoord, 20f),
                                    end = Offset(xCoord, 420f),
                                    strokeWidth = 1f,
                                )
                            }
                            for (gridY in 1..10) {
                                val yCoord = gridY * 40f
                                drawLine(
                                    color = Color(0xFFDCE3FA),
                                    start = Offset(18f, yCoord),
                                    end = Offset(340f, yCoord),
                                    strokeWidth = 1f,
                                )
                            }

                            drawRect(
                                color = Color(0xFF3949AB),
                                topLeft = Offset(18f, 24f),
                                size = Size(322f, 392f),
                                style = Stroke(width = 2f),
                            )

                            drawLine(
                                color = Color(0xFF1F8A84),
                                start = Offset(118f, 92f),
                                end = Offset(118f, 404f),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                            )
                            drawLine(
                                color = Color(0xFF1F8A84),
                                start = Offset(240f, 92f),
                                end = Offset(240f, 404f),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                            )

                            drawRoundRect(
                                color = Color(0xFFE3E8FF),
                                topLeft = Offset(76f, 34f),
                                size = Size(206f, 44f),
                                cornerRadius = CornerRadius(6f, 6f),
                            )
                            drawRoundRect(
                                color = Color(0xFF3949AB),
                                topLeft = Offset(76f, 34f),
                                size = Size(206f, 44f),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = Stroke(width = 1.8f),
                            )

                            tablePositions.forEach { tableCenter ->
                                drawCircle(color = Color.White, radius = 19f, center = tableCenter)
                                drawCircle(color = Color(0xFF3949AB), radius = 19f, center = tableCenter, style = Stroke(width = 1.8f))
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF151729))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            BasicText("12.0 m × 8.0 m", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            BasicText("110V · 2 tomas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47)))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDDF4F3))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            BasicText("Pasillos 1.8 m libres", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F8A84)))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFDE3EA))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            BasicText("Salida de emergencia", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB42345)))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText("LEYENDA TÉCNICA DEL PLANO", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = Color(0xFF5F6377)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Salón: 12.0 m × 8.0 m", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF151729)))
                            BasicText("Pasillo servicio: 1.8 m", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F8A84)))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Energía: circuitos 110V", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7A5700)))
                            BasicText("Ruta evacuación: despejada", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFB42345)))
                        }
                    }
                }
            }

            val feedbackText = localSaveNotice ?: formFeedback
            if (!feedbackText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BasicText(
                        text = feedbackText,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryActionBrush)
                        .clickable { saveCurrentBlueprint() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar plano", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }

                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(16.dp))
                        .clickable {
                            saveCurrentBlueprint()
                            viewModel?.selectChatThreadForContact(selectedEvent.clientName, selectedEvent.id)
                            onSelectScreenCode("A15")
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color(0xFF4B47D6), size = 18.dp)
                        BasicText("Enviar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminCloseoutA19(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = eventsList.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: viewModel?.activeClientEvent()
        ?: eventsList.firstOrNull()
        ?: CanonicalSampleData.events.first()

    val inspectionItems = selectedEvent.items.ifEmpty { CanonicalSampleData.events.first().items }

    var damagedUnitsByCode by remember(selectedEvent.id) {
        mutableStateOf(mapOf<String, Int>())
    }
    var damageNotes by remember(selectedEvent.id) { mutableStateOf("") }
    var attachedEvidenceName by remember(selectedEvent.id) { mutableStateOf<String?>(null) }

    val totalDamagedUnits = inspectionItems.sumOf { lineItem ->
        (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
    }
    val totalDamageChargeDop = inspectionItems.sumOf { lineItem ->
        val damagedCount = (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
        val unitReplacementDop = lineItem.unitPriceDop.coerceAtLeast(25)
        damagedCount * unitReplacementDop
    }

    val hasDamagedItems = totalDamagedUnits > 0
    val damageSummaryBadgeText = if (hasDamagedItems) {
        "$totalDamagedUnits dañadas · +${formatAdminDop(totalDamageChargeDop)}"
    } else {
        "0 dañadas · RD$ 0"
    }
    val primaryCtaLabel = if (hasDamagedItems) {
        "Cobrar y cerrar evento"
    } else {
        "Cerrar evento sin cargos"
    }

    val remainingContractDop = (selectedEvent.totalDop - selectedEvent.paidDepositDop).coerceAtLeast(0)
    val finalBalanceDop = remainingContractDop + totalDamageChargeDop
    val closeoutButtonBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
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
                        .clickable { onSelectScreenCode("BACK:A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cierre del evento",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "${selectedEvent.title} · ${formatReadableSpanishDateText(selectedEvent.dateLabel)}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "DEVOLUCIÓN DE ARTÍCULOS",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (hasDamagedItems) Color(0xFFFDE3EA) else Color(0xFFEEF6D6))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = damageSummaryBadgeText,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasDamagedItems) Color(0xFFB42345) else Color(0xFF4E6A10),
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    inspectionItems.forEachIndexed { itemIndex, lineItem ->
                        if (itemIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        val damagedQty = (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
                        val goodQty = (lineItem.quantity - damagedQty).coerceAtLeast(0)
                        val itemHasDamage = damagedQty > 0
                        val unitFeeDop = lineItem.unitPriceDop.coerceAtLeast(25)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (itemHasDamage) Color(0xFFFFF8E5) else Color.White)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                RfPhoto(
                                    photoKey = photoKeyForAsset(lineItem.photo),
                                    contentDescription = lineItem.name,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = lineItem.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                    )
                                    BasicText(
                                        text = "$goodQty/${lineItem.quantity} en buen estado · Reposición ${formatAdminDop(unitFeeDop)} c/u",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (itemHasDamage) Color(0xFFFDE3EA) else Color(0xFFEEF6D6))
                                        .clickable {
                                            val updatedMap = damagedUnitsByCode.toMutableMap()
                                            updatedMap[lineItem.articleCode] = if (itemHasDamage) 0 else 1.coerceAtMost(lineItem.quantity)
                                            damagedUnitsByCode = updatedMap
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                ) {
                                    BasicText(
                                        text = if (itemHasDamage) "Con daño" else "Buen estado",
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (itemHasDamage) Color(0xFFB42345) else Color(0xFF4E6A10),
                                        ),
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                BasicText(
                                    text = if (itemHasDamage) "$damagedQty dañada(s) · +${formatAdminDop(damagedQty * unitFeeDop)}" else "Sin unidades dañadas",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (itemHasDamage) Color(0xFFB42345) else Color(0xFF5F6377),
                                    ),
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF2F2F8))
                                            .clickable {
                                                val updatedMap = damagedUnitsByCode.toMutableMap()
                                                updatedMap[lineItem.articleCode] = (damagedQty - 1).coerceAtLeast(0)
                                                damagedUnitsByCode = updatedMap
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("−", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                                    }
                                    BasicText(
                                        text = "$damagedQty",
                                        modifier = Modifier.widthIn(min = 22.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729), textAlign = TextAlign.Center),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF2F2F8))
                                            .clickable {
                                                val updatedMap = damagedUnitsByCode.toMutableMap()
                                                updatedMap[lineItem.articleCode] = (damagedQty + 1).coerceAtMost(lineItem.quantity)
                                                damagedUnitsByCode = updatedMap
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("+", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (hasDamagedItems) {
                BasicText(
                    text = "EVIDENCIA Y OBSERVACIONES",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (viewModel != null) {
                                        viewModel.pickMediaAttachment(listOf("jpg", "jpeg", "png")) { pickedFile ->
                                            attachedEvidenceName = pickedFile.fileName.ifBlank { "evidencia-cierre.jpg" }
                                        }
                                    } else {
                                        attachedEvidenceName = "evidencia-cierre.jpg"
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val hasPhoto = !attachedEvidenceName.isNullOrBlank()
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, if (hasPhoto) Color(0xFF8DB52C) else Color(0xFFC9CAE6), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (hasPhoto) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 20.dp)
                                } else {
                                    RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 20.dp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = if (hasPhoto) "Evidencia: $attachedEvidenceName" else "Adjuntar foto del daño",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                )
                                BasicText(
                                    text = "Queda registrada en el acta de inspección",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF7F7FC))
                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (damageNotes.isEmpty()) {
                                BasicText("Notas de inspección (ej. base fisurada)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF8A8FA6)))
                            }
                            BasicTextField(
                                value = damageNotes,
                                onValueChange = { updatedText -> damageNotes = updatedText },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            BasicText(
                text = "RESUMEN DE LIQUIDACIÓN",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Total del evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText(formatAdminDop(selectedEvent.totalDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText(formatAdminDop(selectedEvent.paidDepositDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Cargo por daño", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText(formatAdminDop(totalDamageChargeDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (hasDamagedItems) Color(0xFFB42345) else Color(0xFF151729)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Saldo a cobrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText(formatAdminDop(finalBalanceDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(closeoutButtonBrush)
                    .clickable {
                        if (viewModel != null) {
                            if (hasDamagedItems) {
                                inspectionItems.forEach { lineItem ->
                                    val damagedQty = (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
                                    if (damagedQty > 0) {
                                        val unitFeeDop = lineItem.unitPriceDop.coerceAtLeast(25)
                                        val resolvedNotes = damageNotes.trim().ifBlank {
                                            "Inspección de cierre: $damagedQty unid. dañadas"
                                        }
                                        viewModel.reportEventDamageIncident(
                                            eventId = selectedEvent.id,
                                            articleCode = lineItem.articleCode,
                                            articleName = lineItem.name,
                                            damagedUnits = damagedQty,
                                            missingUnits = 0,
                                            replacementDop = damagedQty * unitFeeDop,
                                            notes = resolvedNotes,
                                        )
                                    }
                                }
                            }
                            viewModel.completeEventInspection(selectedEvent.id)
                        }
                        onSelectScreenCode("A13")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                    BasicText(primaryCtaLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

// ==============================================================================
// A10 · NUEVO ARTÍCULO ADMIN
// ==============================================================================
private fun findNextNonCollidingSku(existingArticles: List<CatalogArticle>): String {
    for (candidateNumber in 7..999) {
        val padded = candidateNumber.toString().padStart(3, '0')
        val candidateCode = "RF-$padded"
        val collides = existingArticles.any { articleItem ->
            articleItem.code.equals(candidateCode, ignoreCase = true)
        }
        if (!collides) {
            return candidateCode
        }
    }
    return "RF-999"
}

private fun shiftYearMonthIso(currentMonthIso: String, deltaMonths: Int): String {
    val parts = currentMonthIso.split("-")
    val baseYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val baseMonth = parts.getOrNull(1)?.toIntOrNull() ?: 10
    val zeroBasedTotal = (baseYear * 12) + (baseMonth - 1) + deltaMonths
    val targetYear = zeroBasedTotal / 12
    val targetMonth = (zeroBasedTotal % 12) + 1
    val paddedMonth = targetMonth.toString().padStart(2, '0')
    return "$targetYear-$paddedMonth"
}

private fun formatYearMonthSpanish(monthIso: String): String {
    val parts = monthIso.split("-")
    val yearPart = parts.getOrNull(0) ?: "2026"
    val monthNumber = parts.getOrNull(1)?.toIntOrNull() ?: 10
    val monthName = when (monthNumber) {
        1 -> "enero"
        2 -> "febrero"
        3 -> "marzo"
        4 -> "abril"
        5 -> "mayo"
        6 -> "junio"
        7 -> "julio"
        8 -> "agosto"
        9 -> "septiembre"
        10 -> "octubre"
        11 -> "noviembre"
        else -> "diciembre"
    }
    return "$monthName $yearPart"
}

@Composable
internal fun RenderAdminNewArticleA10(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val editingArticleId = viewModel?.editingArticleId?.collectAsState()?.value
    val articleCode = viewModel?.newArticleCode?.collectAsState()?.value ?: "RF-007"
    val articleName = viewModel?.newArticleName?.collectAsState()?.value ?: "Mesa plegable 1.8 m"
    val articleCat = viewModel?.newArticleCategory?.collectAsState()?.value ?: "Mobiliario"
    val articlePrice = viewModel?.newArticlePrice?.collectAsState()?.value ?: "20"
    val articleStock = viewModel?.newArticleStock?.collectAsState()?.value ?: "30"
    val formError = viewModel?.formError?.collectAsState()?.value

    var articleDescription by remember { mutableStateOf("Resina blanca, para 8 personas. Ideal para exteriores.") }
    var articleDimensions by remember { mutableStateOf("1.80 m × 0.75 m") }
    var articleMaterial by remember { mutableStateOf("Resina y estructura metálica") }
    var visibleInCatalog by remember { mutableStateOf(true) }
    var pickedPhotoName by remember { mutableStateOf<String?>(null) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(editingArticleId) {
        viewModel?.clearFormError()
        localValidationError = null
        if (editingArticleId != null) {
            val matchedArticle = allArticles.firstOrNull { articleItem -> articleItem.id == editingArticleId }
            if (matchedArticle != null) {
                articleDescription = matchedArticle.description
                articleDimensions = matchedArticle.dimensions
                articleMaterial = matchedArticle.material
                visibleInCatalog = matchedArticle.visibleInCatalog
            }
        } else {
            val cleanCode = articleCode.trim()
            val codeCollides = allArticles.any { articleItem ->
                articleItem.code.equals(cleanCode, ignoreCase = true)
            }
            val needsFreshCode = eitherCondition(cleanCode.isEmpty(), codeCollides)
            if (needsFreshCode) {
                viewModel?.startEditingArticle(null)
                val refreshedCode = viewModel?.newArticleCode?.value?.trim().orEmpty()
                val refreshedStillCollides = allArticles.any { articleItem ->
                    articleItem.code.equals(refreshedCode, ignoreCase = true)
                }
                val mustAssignFallbackSku = eitherCondition(refreshedCode.isEmpty(), refreshedStillCollides)
                if (mustAssignFallbackSku) {
                    val uniqueSku = findNextNonCollidingSku(allArticles)
                    viewModel?.setNewArticleCode(uniqueSku)
                }
            }
        }
    }

    val isEditMode = editingArticleId != null
    val headerTitle = if (isEditMode) "Editar artículo" else "Nuevo artículo"
    val editingArticleObj = allArticles.firstOrNull { articleItem -> articleItem.id == editingArticleId }
    val previewPhotoKey = if (editingArticleObj != null) {
        photoKeyForAsset(editingArticleObj.photo)
    } else {
        RfPhotoKey.MESA_PLEGABLE
    }
    val hasPickedPhoto = !pickedPhotoName.isNullOrBlank()
    val primarySaveBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
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
                        .clickable { onSelectScreenCode("BACK:A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = headerTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "SKU $articleCode · Catálogo e inventario",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(18.dp)),
                ) {
                    RfPhoto(photoKey = previewPhotoKey, contentDescription = articleName, modifier = Modifier.fillMaxSize())
                }

                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, if (hasPickedPhoto) Color(0xFF8DB52C) else Color(0xFFC9CAE6), RoundedCornerShape(18.dp))
                        .clickable {
                            if (viewModel != null) {
                                viewModel.pickMediaAttachment(listOf("jpg", "jpeg", "png")) { pickedFile ->
                                    pickedPhotoName = pickedFile.fileName.ifBlank { "articulo-foto.jpg" }
                                }
                            } else {
                                pickedPhotoName = "articulo-foto.jpg"
                            }
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (hasPickedPhoto) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 22.dp)
                            BasicText(
                                text = pickedPhotoName ?: "Foto lista",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10), textAlign = TextAlign.Center),
                            )
                        } else {
                            RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 22.dp)
                            BasicText("Agregar foto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF2F2F8))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Hasta 6 fotos. La primera es la portada.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.width(116.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Código SKU", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = articleCode,
                            onValueChange = { updatedCode -> viewModel?.setNewArticleCode(updatedCode) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Nombre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = articleName,
                            onValueChange = { updatedName -> viewModel?.setNewArticleName(updatedName) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText("Categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val categories = listOf("Mobiliario", "Floral", "Globos", "Mesa dulce", "Iluminación")
                    categories.forEach { categoryLabel ->
                        val isSelected = categoryLabel.equals(articleCat, ignoreCase = true)
                        val showMobiliarioIcon = bothConditions(isSelected, categoryLabel == "Mobiliario")
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .shadow(if (isSelected) 6.dp else 4.dp, CircleShape, spotColor = if (isSelected) Color(0xC05B6CF0) else Color(0x604650B4), ambientColor = if (isSelected) Color(0x405B6CF0) else Color(0x204650B4))
                                .clip(CircleShape)
                                .background(if (isSelected) primarySaveBrush else SolidColor(Color.White))
                                .then(if (!isSelected) Modifier.border(1.dp, Color(0xFFE9E9F2), CircleShape) else Modifier)
                                .clickable { viewModel?.setNewArticleCategory(categoryLabel) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (showMobiliarioIcon) {
                                    RfIcon(kind = RfIconKind.BOX, tint = Color.White, size = 16.dp)
                                }
                                BasicText(
                                    text = categoryLabel,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF2E324A),
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Precio por día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText("RD$", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicTextField(
                                value = articlePrice,
                                onValueChange = { updatedPrice -> viewModel?.setNewArticlePrice(updatedPrice) },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Cantidad", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicTextField(
                                value = articleStock,
                                onValueChange = { updatedStock -> viewModel?.setNewArticleStock(updatedStock) },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                modifier = Modifier.weight(1f),
                            )
                            BasicText("unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Dimensiones", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = articleDimensions,
                            onValueChange = { updatedDimensions -> articleDimensions = updatedDimensions },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF2E324A)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Material", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = articleMaterial,
                            onValueChange = { updatedMaterial -> articleMaterial = updatedMaterial },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF2E324A)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Descripción", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicTextField(
                        value = articleDescription,
                        onValueChange = { updatedDescription -> articleDescription = updatedDescription },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                    .clickable { visibleInCatalog = !visibleInCatalog }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Visible en el catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                        BasicText("Los clientes lo verán y podrán pedirlo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(30.dp)
                            .clip(CircleShape)
                            .background(if (visibleInCatalog) primarySaveBrush else SolidColor(Color(0xFFDCDDEA)))
                            .padding(3.dp),
                        contentAlignment = if (visibleInCatalog) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }
            }

            val activeError = localValidationError ?: formError
            if (!activeError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDE3EA))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BasicText(
                        text = activeError,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(primarySaveBrush)
                    .clickable {
                        val trimmedName = articleName.trim()
                        val parsedPrice = articlePrice.filter { digitChar -> digitChar.isDigit() }.toIntOrNull() ?: 0
                        if (trimmedName.isEmpty()) {
                            localValidationError = "Ingresa el nombre comercial del artículo."
                        } else if (parsedPrice <= 0) {
                            localValidationError = "Ingresa un precio diario mayor que RD$ 0."
                        } else {
                            localValidationError = null
                            val saved = viewModel?.submitNewArticle(
                                description = articleDescription.trim(),
                                dimensions = articleDimensions.trim(),
                                material = articleMaterial.trim(),
                                visibleInCatalog = visibleInCatalog,
                            ) ?: true
                            if (saved) {
                                onSelectScreenCode("A09")
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

@Composable
internal fun RenderAdminArticleDetailA23(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val articleId = viewModel?.selectedArticleId?.collectAsState()?.value ?: "art-silla-tiffany"
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val selectedArticle = allArticles.firstOrNull { articleItem -> articleItem.id == articleId }
        ?: allArticles.firstOrNull()
        ?: CanonicalSampleData.articles.first()

    var selectedMonthIso by remember(selectedArticle.id) {
        mutableStateOf(DomainClockFormatter.currentYearMonthIso())
    }
    var availabilityCalendar by remember(selectedArticle.id) {
        mutableStateOf<ArticleAvailabilityCalendar?>(null)
    }
    var selectedDayNumber by remember(selectedArticle.id, selectedMonthIso) {
        mutableStateOf<Int?>(null)
    }

    LaunchedEffect(selectedArticle.id, selectedMonthIso) {
        availabilityCalendar = viewModel?.queryWarehouseAvailability(selectedArticle.id, selectedMonthIso)
    }

    val photoKey = photoKeyForAsset(selectedArticle.photo)
    val calendarDays = availabilityCalendar?.days ?: emptyList()
    val peakReservedInMonth = calendarDays.maxOfOrNull { dayEntry -> dayEntry.reservedUnits }
        ?: (selectedArticle.totalStock - selectedArticle.availableOct23).coerceAtLeast(0)
    val currentAvailableUnits = (selectedArticle.totalStock - peakReservedInMonth).coerceAtLeast(0)
    val reservedDatesCount = calendarDays.count { dayEntry -> dayEntry.reservedUnits > 0 }
    val monthTitleLabel = "Reservas · ${formatYearMonthSpanish(selectedMonthIso)}"
    val visibilityStatusLabel = if (selectedArticle.visibleInCatalog) {
        "Visible en el catálogo"
    } else {
        "Oculto en el catálogo"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
        ) {
            RfPhoto(photoKey = photoKey, contentDescription = selectedArticle.name, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.0f to Color(0x4D151729), 0.4f to Color.Transparent)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A09") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable {
                        viewModel?.startEditingArticle(selectedArticle.id)
                        onSelectScreenCode("A10")
                    },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF151729), size = 20.dp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 226.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFFF6F6FB)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = selectedArticle.name,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "${selectedArticle.code} · ${selectedArticle.category} · $visibilityStatusLabel",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        BasicText(
                            text = formatAdminDop(selectedArticle.pricePerDayDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                        )
                        BasicText("por día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }
                }

                BasicText(
                    text = "${selectedArticle.description} (${selectedArticle.dimensions} · ${selectedArticle.material})",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377), lineHeight = 18.sp),
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("${selectedArticle.totalStock}", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                            BasicText("En total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2F2F8))
                                        .clickable { viewModel?.adjustArticleStock(selectedArticle.id, -1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("−", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFECEBFF))
                                        .clickable { viewModel?.adjustArticleStock(selectedArticle.id, 1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("+", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("$currentAvailableUnits", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10)))
                            BasicText("Disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("$peakReservedInMonth", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700)))
                            BasicText("En reserva", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(monthTitleLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                                BasicText("$reservedDatesCount día(s) con reservas activas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2F2F8))
                                        .clickable { selectedMonthIso = shiftYearMonthIso(selectedMonthIso, -1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("‹", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2F2F8))
                                        .clickable { selectedMonthIso = shiftYearMonthIso(selectedMonthIso, 1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("›", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                                }
                            }
                        }

                        val totalDaysInGrid = calendarDays.size.takeIf { dayCount -> dayCount > 0 } ?: 30
                        val dayNumbers = (1..totalDaysInGrid).toList()
                        val rows = dayNumbers.chunked(7)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            rows.forEach { rowDays ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    rowDays.forEach { dayNumber ->
                                        val matchingDayInfo = calendarDays.getOrNull(dayNumber - 1)
                                        val reservedUnitsOnDay = matchingDayInfo?.reservedUnits ?: if (dayNumber == 23) peakReservedInMonth else 0
                                        val availableUnitsOnDay = matchingDayInfo?.availableUnits ?: (selectedArticle.totalStock - reservedUnitsOnDay).coerceAtLeast(0)
                                        val isFull = bothConditions(reservedUnitsOnDay > 0, availableUnitsOnDay <= 0)
                                        val isPartial = bothConditions(reservedUnitsOnDay > 0, availableUnitsOnDay > 0)
                                        val isSelectedDay = selectedDayNumber == dayNumber

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .then(
                                                    if (isFull) {
                                                        Modifier.background(AdminButtonGradient)
                                                    } else if (isPartial) {
                                                        Modifier.background(Color(0xFFECEBFF))
                                                    } else if (isSelectedDay) {
                                                        Modifier.border(1.5.dp, Color(0xFF5B6CF0), RoundedCornerShape(9.dp))
                                                    } else {
                                                        Modifier
                                                    },
                                                )
                                                .clickable { selectedDayNumber = dayNumber },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = "$dayNumber",
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = when {
                                                        isFull -> Color.White
                                                        isPartial -> Color(0xFF4B47D6)
                                                        else -> Color(0xFF2E324A)
                                                    },
                                                ),
                                            )
                                        }
                                    }
                                    if (rowDays.size < 7) {
                                        repeat(7 - rowDays.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedDayNumber != null) {
                            val chosenIndex = (selectedDayNumber ?: 1) - 1
                            val chosenDayInfo = calendarDays.getOrNull(chosenIndex)
                            val dayReserved = chosenDayInfo?.reservedUnits ?: 0
                            val dayAvailable = chosenDayInfo?.availableUnits ?: (selectedArticle.totalStock - dayReserved).coerceAtLeast(0)
                            BasicText(
                                text = "Día $selectedDayNumber: $dayAvailable disponibles · $dayReserved reservadas",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF5B6CF0)))
                                BasicText("Todo reservado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFECEBFF)))
                                BasicText("Parcial", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PackageDraftLineItem(
    val articleId: String,
    val name: String,
    val unitPriceDop: Int,
    val quantity: Int,
    val photoKey: RfPhotoKey,
)

private fun buildCanonicalBodasRosaLines(): List<PackageDraftLineItem> = listOf(
    PackageDraftLineItem("art-arco-rosa", "Arco de globos rosa", 95, 1, RfPhotoKey.ARCO_ROSA),
    PackageDraftLineItem("art-centro-floral", "Centro de mesa floral", 35, 10, RfPhotoKey.CENTRO_FLORAL),
    PackageDraftLineItem("art-silla-tiffany", "Silla Tiffany Cristal", 4, 120, RfPhotoKey.SILLA_TIFFANY),
    PackageDraftLineItem("art-mesa-redonda", "Mesa redonda", 25, 3, RfPhotoKey.MESA_REDONDA),
)

private fun buildFallbackAdminPackages(): List<EventPackage> = listOf(
    EventPackage(
        id = "pkg-bodas-rosa",
        name = "Bodas Rosa",
        subtitle = "Ceremonia y recepción romántica",
        suggestedGuests = 120,
        priceDop = 850,
        savingsDop = 150,
        photo = PhotoAsset.BODA,
        includedItems = listOf("1x Arco de globos rosa", "10x Centro de mesa floral", "120x Silla Tiffany Cristal", "3x Mesa redonda"),
        eventType = "Boda",
        visibleInCatalog = true,
    ),
    EventPackage(
        id = "pkg-quince-premium",
        name = "Quinceañera Premium",
        subtitle = "Salón de gala en cristal y flores",
        suggestedGuests = 100,
        priceDop = 1200,
        savingsDop = 165,
        photo = PhotoAsset.QUINCEANERA,
        includedItems = listOf("1x Arco de globos rosa", "12x Centro de mesa floral", "100x Silla Tiffany Cristal", "10x Mesa redonda"),
        eventType = "Quinceañera",
        visibleInCatalog = true,
    ),
    EventPackage(
        id = "pkg-cumple-infantil",
        name = "Cumpleaños Infantil",
        subtitle = "Estación dulce y montaje colorido",
        suggestedGuests = 50,
        priceDop = 450,
        savingsDop = 50,
        photo = PhotoAsset.CUMPLE_INFANTIL,
        includedItems = listOf("1x Mesa de helados", "1x Arco de globos rosa", "40x Silla Tiffany Cristal"),
        eventType = "Cumpleaños",
        visibleInCatalog = true,
    ),
    EventPackage(
        id = "pkg-corporativo",
        name = "Evento Corporativo",
        subtitle = "Mobiliario ejecutivo y sonido base",
        suggestedGuests = 80,
        priceDop = 600,
        savingsDop = 55,
        photo = PhotoAsset.CORPORATIVO,
        includedItems = listOf("80x Silla Tiffany Cristal", "8x Mesa plegable 1.8 m", "8x Centro de mesa floral"),
        eventType = "Corporativo",
        visibleInCatalog = true,
    ),
)

private fun parseIncludedItemsIntoDraftLines(
    includedItems: List<String>,
    catalogArticles: List<CatalogArticle>,
): List<PackageDraftLineItem> {
    if (includedItems.isEmpty()) {
        return buildCanonicalBodasRosaLines()
    }
    return includedItems.mapIndexed { itemIndex, rawEntry ->
        val trimmed = rawEntry.trim()
        val leadingDigits = trimmed.takeWhile { charItem -> charItem.isDigit() }
        val parsedQty = leadingDigits.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val remainder = trimmed.drop(leadingDigits.length).trimStart('x', 'X', ' ', '·', '-')
        val labelName = remainder.ifBlank { trimmed }
        val matchedCatalog = catalogArticles.firstOrNull { articleItem ->
            val nameContainsLabel = articleItem.name.contains(labelName, ignoreCase = true)
            val labelContainsName = labelName.contains(articleItem.name, ignoreCase = true)
            eitherCondition(nameContainsLabel, labelContainsName)
        }
        if (matchedCatalog != null) {
            PackageDraftLineItem(
                articleId = matchedCatalog.id,
                name = matchedCatalog.name,
                unitPriceDop = matchedCatalog.pricePerDayDop.coerceAtLeast(1),
                quantity = parsedQty,
                photoKey = photoKeyForAsset(matchedCatalog.photo),
            )
        } else {
            PackageDraftLineItem(
                articleId = "custom-$itemIndex",
                name = labelName,
                unitPriceDop = 35,
                quantity = parsedQty,
                photoKey = RfPhotoKey.CENTRO_FLORAL,
            )
        }
    }
}

@Composable
internal fun RenderAdminPackagesA24(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val packagesList = viewModel?.packages?.collectAsState()?.value ?: buildFallbackAdminPackages()
    var fallbackVisibilityById by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val activePackageCount = packagesList.count { packageItem ->
        fallbackVisibilityById[packageItem.id] ?: packageItem.visibleInCatalog
    }
    val primaryPillGradient = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
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
                    .clickable { onSelectScreenCode("BACK:A12") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Paquetes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                )
                BasicText(
                    text = "$activePackageCount activos en el catálogo",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(primaryPillGradient)
                    .clickable {
                        viewModel?.startEditingPackage("NEW")
                        onSelectScreenCode("A25")
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("+ Nuevo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }

        packagesList.forEach { packageItem ->
            val isPackageActive = fallbackVisibilityById[packageItem.id] ?: packageItem.visibleInCatalog
            val normalValue = (packageItem.priceDop + packageItem.savingsDop).coerceAtLeast(1)
            val discountPct = ((packageItem.savingsDop * 100) / normalValue).coerceAtLeast(0)
            val discountBadge = if (discountPct > 0) "$discountPct% desc." else "${packageItem.suggestedGuests} inv."

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable {
                        viewModel?.selectPackage(packageItem.id)
                        viewModel?.startEditingPackage(packageItem.id)
                        onSelectScreenCode("A25")
                    }
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfPhoto(
                        photoKey = photoKeyForAsset(packageItem.photo),
                        contentDescription = packageItem.name,
                        modifier = Modifier.size(76.dp).clip(RoundedCornerShape(14.dp)),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        BasicText(
                            text = packageItem.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicText(
                                text = "${packageItem.includedItems.size} artículos · ",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                            )
                            BasicText(
                                text = discountBadge,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)),
                            )
                        }
                        BasicText(
                            text = formatAdminDop(packageItem.priceDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(if (isPackageActive) primaryPillGradient else SolidColor(Color(0xFFDCDDEA)))
                            .clickable {
                                val nextVisible = !isPackageActive
                                val updatedMap = fallbackVisibilityById.toMutableMap()
                                updatedMap[packageItem.id] = nextVisible
                                fallbackVisibilityById = updatedMap
                                if (viewModel != null) {
                                    viewModel.startEditingPackage(packageItem.id)
                                    viewModel.saveAdminPackage(
                                        name = packageItem.name,
                                        subtitle = packageItem.subtitle,
                                        eventType = packageItem.eventType,
                                        suggestedGuests = packageItem.suggestedGuests,
                                        priceDop = packageItem.priceDop,
                                        includedItems = packageItem.includedItems,
                                        visibleInCatalog = nextVisible,
                                    )
                                    viewModel.startEditingPackage(null)
                                }
                            }
                            .padding(3.dp),
                        contentAlignment = if (isPackageActive) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminPackageEditA25(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val packagesList = viewModel?.packages?.collectAsState()?.value ?: buildFallbackAdminPackages()
    val editingPackageId = viewModel?.editingPackageId?.collectAsState()?.value
    val catalogArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val formError = viewModel?.formError?.collectAsState()?.value

    val matchedExistingPackage = if (editingPackageId != null) {
        packagesList.firstOrNull { packageItem -> packageItem.id == editingPackageId }
    } else {
        null
    }
    val isCreatingNewPackage = editingPackageId == "NEW"

    var packageName by remember(editingPackageId) {
        val initialName = when {
            isCreatingNewPackage -> ""
            matchedExistingPackage != null -> matchedExistingPackage.name
            else -> "Bodas Rosa"
        }
        mutableStateOf(initialName)
    }
    var packageSubtitle by remember(editingPackageId) {
        val initialSub = when {
            isCreatingNewPackage -> ""
            matchedExistingPackage != null -> matchedExistingPackage.subtitle
            else -> "Ceremonia y recepción romántica"
        }
        mutableStateOf(initialSub)
    }
    var packageEventType by remember(editingPackageId) {
        val initialType = when {
            isCreatingNewPackage -> "Boda"
            matchedExistingPackage != null -> matchedExistingPackage.eventType
            else -> "Boda"
        }
        mutableStateOf(initialType)
    }
    var suggestedGuestsText by remember(editingPackageId) {
        val initialGuests = when {
            isCreatingNewPackage -> "50"
            matchedExistingPackage != null -> matchedExistingPackage.suggestedGuests.toString()
            else -> "120"
        }
        mutableStateOf(initialGuests)
    }
    var visibleInCatalog by remember(editingPackageId) {
        val initialVisible = matchedExistingPackage?.visibleInCatalog ?: true
        mutableStateOf(initialVisible)
    }
    var packageLines by remember(editingPackageId) {
        val initialLines = when {
            isCreatingNewPackage -> emptyList()
            matchedExistingPackage != null -> parseIncludedItemsIntoDraftLines(matchedExistingPackage.includedItems, catalogArticles)
            else -> buildCanonicalBodasRosaLines()
        }
        mutableStateOf(initialLines)
    }
    var discountPercent by remember(editingPackageId) {
        val initialDiscount = when {
            isCreatingNewPackage -> 10

            matchedExistingPackage != null -> {
                val baseTotal = (matchedExistingPackage.priceDop + matchedExistingPackage.savingsDop).coerceAtLeast(1)
                ((matchedExistingPackage.savingsDop * 100) / baseTotal).coerceIn(5, 40)
            }

            else -> 15
        }
        mutableStateOf(initialDiscount)
    }

    var showCatalogPickerModal by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val normalPriceDop = packageLines.sumOf { lineEntry -> lineEntry.unitPriceDop * lineEntry.quantity }
    val discountedPriceDop = normalPriceDop * (100 - discountPercent) / 100
    val headerTitle = if (isCreatingNewPackage) "Nuevo paquete" else "Editar paquete"
    val previewPhoto = matchedExistingPackage?.let { existingPkg -> photoKeyForAsset(existingPkg.photo) }
        ?: photoKeyForEventType(packageEventType)
    val eventTypeOptions = listOf("Boda", "Quinceañera", "Cumpleaños", "Corporativo")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            .clickable {
                                viewModel?.startEditingPackage(null)
                                onSelectScreenCode("BACK:A24")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(headerTitle, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                        BasicText(
                            text = packageName.ifBlank { "Configura artículos y descuento" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp)),
                    ) {
                        RfPhoto(photoKey = previewPhoto, contentDescription = packageName.ifBlank { "Paquete" }, modifier = Modifier.fillMaxSize())
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("Nombre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (packageName.isEmpty()) {
                                BasicText("Ej. Paquete Jardín Imperial", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF8A8FA6)))
                            }
                            BasicTextField(
                                value = packageName,
                                onValueChange = { updatedText -> packageName = updatedText },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("Subtítulo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (packageSubtitle.isEmpty()) {
                                BasicText("Descripción breve", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF8A8FA6)))
                            }
                            BasicTextField(
                                value = packageSubtitle,
                                onValueChange = { updatedSubtitle -> packageSubtitle = updatedSubtitle },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF2E324A)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Column(modifier = Modifier.width(98.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("Invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            BasicTextField(
                                value = suggestedGuestsText,
                                onValueChange = { updatedGuests -> suggestedGuestsText = updatedGuests },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    eventTypeOptions.forEach { typeOption ->
                        val isTypeSelected = packageEventType.equals(typeOption, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isTypeSelected) {
                                        Modifier.background(AdminButtonGradient)
                                    } else {
                                        Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                                    },
                                )
                                .clickable { packageEventType = typeOption }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = typeOption,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (isTypeSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isTypeSelected) Color.White else Color(0xFF2E324A),
                                ),
                            )
                        }
                    }
                }

                BasicText("INCLUYE", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377), letterSpacing = 1.sp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp)),
                ) {
                    Column {
                        if (packageLines.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Agrega artículos del catálogo para armar este paquete.",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                            }
                        }

                        packageLines.forEachIndexed { lineIndex, lineEntry ->
                            if (lineIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                ) {
                                    RfPhoto(photoKey = lineEntry.photoKey, contentDescription = lineEntry.name, modifier = Modifier.fillMaxSize())
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = lineEntry.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                    )
                                    BasicText(
                                        text = "RD$ ${lineEntry.unitPriceDop} c/u",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = Color(0xFF5F6377)),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2F2F8))
                                        .padding(horizontal = 3.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    val nextQty = lineEntry.quantity - 1
                                                    packageLines = if (nextQty <= 0) {
                                                        packageLines.filterIndexed { currentIdx, _ -> currentIdx != lineIndex }
                                                    } else {
                                                        packageLines.mapIndexed { currentIdx, currentItem ->
                                                            if (currentIdx == lineIndex) currentItem.copy(quantity = nextQty) else currentItem
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText("−", style = TextStyle(fontSize = 15.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                        }
                                        BasicText(
                                            text = "${lineEntry.quantity}",
                                            modifier = Modifier.widthIn(min = 24.dp),
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A), textAlign = TextAlign.Center),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    packageLines = packageLines.mapIndexed { currentIdx, currentItem ->
                                                        if (currentIdx == lineIndex) currentItem.copy(quantity = currentItem.quantity + 1) else currentItem
                                                    }
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText("+", style = TextStyle(fontSize = 15.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clickable { showCatalogPickerModal = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                                BasicText("Agregar artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .clickable {
                            discountPercent = when (discountPercent) {
                                15 -> 20
                                20 -> 10
                                else -> 15
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Descuento del paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                            BasicText("$discountPercent%", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEDEDF5)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((discountPercent / 50f).coerceIn(0.1f, 1f))
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AdminButtonGradient),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                BasicText("Precio normal ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                BasicText(
                                    text = formatAdminDop(normalPriceDop),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377), textDecoration = TextDecoration.LineThrough),
                                )
                            }
                            BasicText(
                                text = formatAdminDop(discountedPriceDop),
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                            )
                        }
                    }
                }

                val activeError = localValidationError ?: formError
                if (!activeError.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDE3EA))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        BasicText(
                            text = activeError,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable {
                            val trimmedName = packageName.trim()
                            if (trimmedName.isEmpty()) {
                                localValidationError = "Ingresa el nombre comercial del paquete."
                            } else if (packageLines.isEmpty()) {
                                localValidationError = "Agrega al menos un artículo al paquete."
                            } else {
                                localValidationError = null
                                val activeVm = viewModel
                                if (activeVm != null) {
                                    if (isCreatingNewPackage) {
                                        activeVm.startEditingPackage(null)
                                    }
                                    val parsedGuests = suggestedGuestsText.filter { digitChar -> digitChar.isDigit() }.toIntOrNull()?.coerceAtLeast(10) ?: 50
                                    val formattedIncluded = packageLines.map { lineEntry ->
                                        "${lineEntry.quantity}x ${lineEntry.name}"
                                    }
                                    val resolvedSub = packageSubtitle.trim().ifBlank {
                                        "Paquete $packageEventType · ${packageLines.size} artículos"
                                    }
                                    val savedPackage = activeVm.saveAdminPackage(
                                        name = trimmedName,
                                        subtitle = resolvedSub,
                                        eventType = packageEventType,
                                        suggestedGuests = parsedGuests,
                                        priceDop = discountedPriceDop.coerceAtLeast(1),
                                        includedItems = formattedIncluded,
                                        visibleInCatalog = visibleInCatalog,
                                    )
                                    if (savedPackage != null) {
                                        activeVm.startEditingPackage(null)
                                        onSelectScreenCode("A24")
                                    }
                                } else {
                                    onSelectScreenCode("A24")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        if (showCatalogPickerModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73151729))
                    .clickable { showCatalogPickerModal = false }
                    .padding(20.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Agregar artículo al paquete", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable { showCatalogPickerModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }

                    catalogArticles.take(6).forEach { catalogItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF7F7FC))
                                .clickable {
                                    val existingIdx = packageLines.indexOfFirst { lineEntry ->
                                        eitherCondition(
                                            lineEntry.articleId == catalogItem.id,
                                            lineEntry.name.equals(catalogItem.name, ignoreCase = true),
                                        )
                                    }
                                    packageLines = if (existingIdx >= 0) {
                                        packageLines.mapIndexed { idx, lineEntry ->
                                            if (idx == existingIdx) lineEntry.copy(quantity = lineEntry.quantity + 1) else lineEntry
                                        }
                                    } else {
                                        packageLines + PackageDraftLineItem(
                                            articleId = catalogItem.id,
                                            name = catalogItem.name,
                                            unitPriceDop = catalogItem.pricePerDayDop.coerceAtLeast(1),
                                            quantity = 1,
                                            photoKey = photoKeyForAsset(catalogItem.photo),
                                        )
                                    }
                                    showCatalogPickerModal = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            RfPhoto(
                                photoKey = photoKeyForAsset(catalogItem.photo),
                                contentDescription = catalogItem.name,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = catalogItem.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                )
                                BasicText(
                                    text = "${catalogItem.code} · RD$ ${catalogItem.pricePerDayDop}/día",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 18.dp)
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// A26 · DIRECTORIO DE CLIENTES
// ==============================================================================
@Composable
internal fun RenderAdminClientsA26(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val repositoryClients = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
    val registeredAccounts = viewModel?.registeredAccounts?.collectAsState()?.value ?: emptyList()

    val existingEmails = repositoryClients.map { profile -> profile.email.trim().lowercase() }.toSet()
    val extraClients = registeredAccounts
        .filter { account -> account.email.trim().lowercase() !in existingEmails }
        .map { account ->
            ClientProfile(
                id = "cli-reg-${account.username}",
                fullName = account.fullName,
                email = account.email,
                phone = account.phone.ifBlank { "Sin teléfono" },
                zone = "Santo Domingo",
                eventsCount = 0,
                totalContractedDop = 0,
                pendingBalanceDop = 0,
                vipTag = "Registrado",
            )
        }
    val allClients = repositoryClients + extraClients

    val vipClientsCount = allClients.count { profile -> isVipClientProfile(profile) }
    val balanceClientsCount = allClients.count { profile -> profile.pendingBalanceDop > 0 }
    val newClientsCount = allClients.count { profile -> isNewClientProfile(profile) }

    val filteredClients = allClients.filter { profile ->
        val matchesChip = when (selectedFilterIndex) {
            1 -> isVipClientProfile(profile)
            2 -> profile.pendingBalanceDop > 0
            3 -> isNewClientProfile(profile)
            else -> true
        }
        val trimmedQuery = searchQuery.trim()
        val combinedText = "${profile.fullName} ${profile.email} ${profile.phone} ${profile.zone} ${profile.vipTag}"
        val matchesQuery = if (trimmedQuery.isEmpty()) {
            true
        } else {
            combinedText.contains(trimmedQuery, ignoreCase = true)
        }
        bothConditions(matchesChip, matchesQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
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
                    .clickable { onSelectScreenCode("BACK:A12") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Clientes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                )
                BasicText(
                    text = "${filteredClients.size} clientes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (searchQuery.isEmpty()) {
                    BasicText("Buscar por nombre, correo o teléfono", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { updatedText -> searchQuery = updatedText },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        val filterChips = listOf(
            "Todos · ${allClients.size}",
            "VIP · $vipClientsCount",
            "Con saldo · $balanceClientsCount",
            "Nuevos · $newClientsCount",
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filterChips.forEachIndexed { chipIndex, chipLabel ->
                val isSelected = selectedFilterIndex == chipIndex
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            },
                        )
                        .clickable { selectedFilterIndex = chipIndex }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        chipLabel,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF2E324A),
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (filteredClients.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "No se encontraron clientes con ese criterio.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                        )
                    }
                }
                filteredClients.forEachIndexed { clientIndex, profile ->
                    if (clientIndex > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    val avatarColors = clientAvatarColors(clientIndex)
                    val tagColors = clientTagColors(profile)
                    val initialChar = profile.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
                    val summaryLine = if (profile.pendingBalanceDop > 0) {
                        "${profile.eventsCount} eventos · saldo RD$ ${profile.pendingBalanceDop}"
                    } else if (profile.totalContractedDop > 0) {
                        "${profile.eventsCount} eventos · pagó RD$ ${profile.totalContractedDop}"
                    } else {
                        "${profile.email} · ${profile.phone}"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.selectClient(profile.id)
                                onSelectScreenCode("A27")
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(avatarColors.first),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(initialChar, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = avatarColors.second))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(profile.fullName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText(summaryLine, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(tagColors.first)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(profile.vipTag.ifBlank { "Cliente" }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tagColors.second))
                        }
                    }
                }
            }
        }
    }
}

private fun isVipClientProfile(profile: ClientProfile): Boolean {
    val tag = profile.vipTag.lowercase()
    val hasVipTag = eitherCondition(tag.contains("vip"), tag.contains("frecuente"))
    val highSpend = profile.totalContractedDop >= 5000
    return eitherCondition(hasVipTag, highSpend)
}

private fun isNewClientProfile(profile: ClientProfile): Boolean {
    val tag = profile.vipTag.lowercase()
    val hasNewTag = eitherCondition(tag.contains("nuev"), tag.contains("registrad"))
    return eitherCondition(hasNewTag, profile.eventsCount <= 1)
}

private fun clientAvatarColors(index: Int): Pair<Color, Color> = when (index % 4) {
    0 -> Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))
    1 -> Pair(Color(0xFFEEF6D6), Color(0xFF4E6A10))
    2 -> Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))
    else -> Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))
}

private fun clientTagColors(profile: ClientProfile): Pair<Color, Color> = when {
    profile.pendingBalanceDop > 0 -> Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))
    isVipClientProfile(profile) -> Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))
    else -> Pair(Color(0xFFEEF6D6), Color(0xFF4E6A10))
}

private fun matchesClientRecord(eventItem: ClientEvent, client: ClientProfile): Boolean {
    val cleanEventEmail = eventItem.clientEmail.trim()
    val cleanClientEmail = client.email.trim()
    val hasEmails = bothConditions(cleanEventEmail.isNotEmpty(), cleanClientEmail.isNotEmpty())
    val emailMatches = if (hasEmails) cleanEventEmail.equals(cleanClientEmail, ignoreCase = true) else false
    if (emailMatches) {
        return true
    }
    return eventItem.clientName.trim().equals(client.fullName.trim(), ignoreCase = true)
}

@Composable
internal fun RenderAdminClientProfileA27(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val selectedClientId = viewModel?.selectedClientId?.collectAsState()?.value ?: CanonicalSampleData.clients.first().id
    val clientsList = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
    val activeClient = clientsList.firstOrNull { profile -> profile.id == selectedClientId }
        ?: viewModel?.selectedClient()
        ?: CanonicalSampleData.clients.first()

    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val paymentsState = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments

    val clientEvents = eventsState.filter { eventItem -> matchesClientRecord(eventItem, activeClient) }
    val clientPayments = paymentsState.filter { paymentItem ->
        paymentItem.clientName.trim().equals(activeClient.fullName.trim(), ignoreCase = true)
    }
    val paidFromPayments = clientPayments.sumOf { paymentItem -> paymentItem.amountDop }
    val paidFromEvents = clientEvents.sumOf { eventItem -> eventItem.paidDepositDop }
    val paidFallback = (activeClient.totalContractedDop - activeClient.pendingBalanceDop).coerceAtLeast(0)
    val totalPaidDop = when {
        paidFromPayments > 0 -> paidFromPayments
        paidFromEvents > 0 -> paidFromEvents
        else -> paidFallback
    }
    val displayedEventsCount = maxOf(clientEvents.size, activeClient.eventsCount)

    val defaultClientNote = activeClient.notes.ifBlank {
        "Nota interna de ${activeClient.fullName}: zona ${activeClient.zone} · seguimiento personalizado."
    }
    var internalNote by remember(activeClient.id, activeClient.notes) { mutableStateOf(defaultClientNote) }
    var showEditClientModal by remember { mutableStateOf(false) }
    var editName by remember(activeClient.id) { mutableStateOf(activeClient.fullName) }
    var editEmail by remember(activeClient.id) { mutableStateOf(activeClient.email) }
    var editPhone by remember(activeClient.id) { mutableStateOf(activeClient.phone) }
    var editNotes by remember(activeClient.id) { mutableStateOf(internalNote) }
    var editValidationError by remember { mutableStateOf<String?>(null) }

    val clientInitial = activeClient.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        .clickable { onSelectScreenCode("BACK:A26") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Cliente",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable {
                            editName = activeClient.fullName
                            editEmail = activeClient.email
                            editPhone = activeClient.phone
                            editNotes = internalNote
                            editValidationError = null
                            showEditClientModal = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFDE6F2)),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F), fontSize = 28.sp))
                }
                BasicText(activeClient.fullName, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 22.sp))
                BasicText("${activeClient.email} · ${activeClient.phone}", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                BasicText("${activeClient.zone} · ${activeClient.vipTag}", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF4B47D6), fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { viewModel?.dialPhoneNumber(activeClient.phone) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.PHONE, tint = Color(0xFF151729), size = 18.dp)
                        BasicText("Llamar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.openWhatsAppChat(
                                activeClient.phone,
                                "Hola ${activeClient.fullName}, te escribimos de Rosa Fiesta sobre tus eventos.",
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                        BasicText("WhatsApp", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.selectChatThreadForContact(activeClient.fullName)
                            onSelectScreenCode("A15")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF4B47D6), size = 18.dp)
                        BasicText("Chat", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFECEBFF))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        BasicText("$displayedEventsCount", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6), fontSize = 20.sp))
                        BasicText("Eventos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6), fontSize = 12.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        BasicText("RD$ $totalPaidDop", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F560C), fontSize = 20.sp))
                        BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10), fontSize = 12.sp))
                    }
                }
            }

            BasicText("EVENTOS", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    if (clientEvents.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(
                                text = "Sin eventos registrados aún",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFECEBFF))
                                    .clickable {
                                        viewModel?.setNewEventClient(activeClient.fullName)
                                        onSelectScreenCode("A05")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                BasicText("Crear evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                            }
                        }
                    }

                    clientEvents.forEachIndexed { eventIndex, clientEvent ->
                        if (eventIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel?.selectEvent(clientEvent.id)
                                    onSelectScreenCode("A13")
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                RfPhoto(photoKey = photoKeyForAsset(clientEvent.photo), contentDescription = clientEvent.title, modifier = Modifier.fillMaxSize())
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(clientEvent.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                                BasicText("${formatReadableSpanishDateText(clientEvent.dateLabel)} · ${clientEvent.statusLabel}", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFF8E5))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF7A5700), size = 18.dp)
                        BasicTextField(
                            value = internalNote,
                            onValueChange = { updatedNote -> internalNote = updatedNote },
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF151729)),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF7A5700))
                                .clickable {
                                    viewModel?.updateSelectedClientProfile(
                                        fullName = activeClient.fullName,
                                        email = activeClient.email,
                                        phone = activeClient.phone,
                                        notes = internalNote,
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            BasicText("Guardar nota", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showEditClientModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73151729))
                    .clickable { showEditClientModal = false }
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Editar datos del cliente", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable { showEditClientModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }

                    BasicText("Nombre completo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicTextField(
                        value = editName,
                        onValueChange = { nextName -> editName = nextName },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )

                    BasicText("Correo electrónico", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicTextField(
                        value = editEmail,
                        onValueChange = { nextEmail -> editEmail = nextEmail },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )

                    BasicText("Teléfono", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicTextField(
                        value = editPhone,
                        onValueChange = { nextPhone -> editPhone = nextPhone },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )

                    BasicText("Notas CRM", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicTextField(
                        value = editNotes,
                        onValueChange = { nextNotes -> editNotes = nextNotes },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )

                    if (!editValidationError.isNullOrBlank()) {
                        BasicText(
                            text = editValidationError.orEmpty(),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AdminButtonGradient)
                            .clickable {
                                val cleanName = editName.trim()
                                val cleanEmail = editEmail.trim()
                                if (cleanName.length < 2) {
                                    editValidationError = "Ingresa un nombre válido para el cliente."
                                } else if (!cleanEmail.contains("@")) {
                                    editValidationError = "Ingresa un correo electrónico válido."
                                } else {
                                    viewModel?.selectClient(activeClient.id)
                                    viewModel?.updateSelectedClientProfile(
                                        fullName = cleanName,
                                        email = cleanEmail,
                                        phone = editPhone.trim(),
                                        notes = editNotes.trim(),
                                    )
                                    internalNote = editNotes.trim()
                                    showEditClientModal = false
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Guardar cambios", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

private data class StaffPermissionOptionRow(
    val permission: StaffPermission,
    val label: String,
)

private fun resolveStaffAvatarPalette(memberName: String): Pair<Color, Color> {
    val clean = memberName.trim().lowercase()
    return when {
        clean.contains("mar") -> Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))
        clean.contains("pedro") -> Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84))
        clean.contains("yuli") -> Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))
        else -> Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))
    }
}

private fun resolveStaffRoleBadge(member: StaffMember): String = when {
    member.roleLabel.contains("Dueñ", ignoreCase = true) -> "Admin"
    member.roleLabel.contains("Admin", ignoreCase = true) -> "Admin"
    StaffPermission.EVENTS !in member.permissions -> "Limitado"
    else -> "Equipo"
}

private fun permissionsForInvitedRoleLabel(roleLabel: String): List<String> = when {
    roleLabel.contains("Admin", ignoreCase = true) -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.DECORATION.name,
        StaffPermission.TASKS.name,
        StaffPermission.INVENTORY.name,
        StaffPermission.PAYMENTS.name,
        StaffPermission.REPORTS.name,
    )

    roleLabel.contains("Decor", ignoreCase = true) -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.DECORATION.name,
        StaffPermission.TASKS.name,
        StaffPermission.INVENTORY.name,
    )

    roleLabel.contains("Log", ignoreCase = true) -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.TASKS.name,
        StaffPermission.INVENTORY.name,
    )

    else -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.TASKS.name,
    )
}

@Composable
internal fun RenderAdminTeamA28(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val staffList = viewModel?.staffMembers?.collectAsState()?.value ?: CanonicalSampleData.staffMembers
    val formError = viewModel?.formError?.collectAsState()?.value

    val initialMemberId = staffList.firstOrNull { member -> member.name.equals("Yuli", ignoreCase = true) }?.id
        ?: staffList.first().id
    var selectedStaffId by remember { mutableStateOf(initialMemberId) }
    val activeMember = staffList.firstOrNull { member -> member.id == selectedStaffId } ?: staffList.first()

    var showInviteModal by remember { mutableStateOf(false) }
    var inviteName by remember { mutableStateOf("") }
    var inviteEmail by remember { mutableStateOf("") }
    var invitePhone by remember { mutableStateOf("") }
    var inviteRoleLabel by remember { mutableStateOf("Decoración y eventos") }
    var inviteLocalError by remember { mutableStateOf<String?>(null) }

    val roleChoices = listOf("Decoración y eventos", "Logística y montaje", "Almacén y entregas", "Administración")
    val permissionRows = remember {
        listOf(
            StaffPermissionOptionRow(StaffPermission.EVENTS, "Ver y editar eventos"),
            StaffPermissionOptionRow(StaffPermission.INVENTORY, "Inventario y artículos"),
            StaffPermissionOptionRow(StaffPermission.PAYMENTS, "Pagos y cobros"),
            StaffPermissionOptionRow(StaffPermission.REPORTS, "Reportes del negocio"),
            StaffPermissionOptionRow(StaffPermission.DECORATION, "Montaje y decoración de eventos"),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
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
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Equipo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "${staffList.size} personas",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable {
                            inviteName = ""
                            inviteEmail = ""
                            invitePhone = ""
                            inviteLocalError = null
                            viewModel?.clearFormError()
                            showInviteModal = true
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.USER, tint = Color.White, size = 18.dp)
                        BasicText("Invitar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    staffList.forEachIndexed { memberIndex, member ->
                        if (memberIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        val isHighlighted = member.id == activeMember.id
                        val avatarPalette = resolveStaffAvatarPalette(member.name)
                        val roleBadge = resolveStaffRoleBadge(member)
                        val tagBg = if (roleBadge == "Admin") {
                            Color(0xFFECEBFF)
                        } else if (roleBadge == "Equipo") {
                            Color(0xFFDDF4F3)
                        } else {
                            Color(0xFFF2F2F8)
                        }
                        val tagInk = if (roleBadge == "Admin") {
                            Color(0xFF4B47D6)
                        } else if (roleBadge == "Equipo") {
                            Color(0xFF1F8A84)
                        } else {
                            Color(0xFF5F6377)
                        }
                        val initialChar = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "S"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isHighlighted) Color(0xFFF7F6FF) else Color.White)
                                .clickable { selectedStaffId = member.id }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(avatarPalette.first),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(initialChar, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = avatarPalette.second))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(member.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                                BasicText(member.roleLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                            }
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(tagBg)
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(roleBadge, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tagInk))
                            }
                        }
                    }
                }
            }

            BasicText(
                text = "PERMISOS DE ${activeMember.name.uppercase()}",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    permissionRows.forEachIndexed { permIndex, optionRow ->
                        val isPaymentsRow = optionRow.permission == StaffPermission.PAYMENTS
                        val inPermissionSet = optionRow.permission in activeMember.permissions
                        val isPermissionActive = if (isPaymentsRow) {
                            eitherCondition(activeMember.canViewPayments, inPermissionSet)
                        } else {
                            inPermissionSet
                        }

                        if (permIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val nextSet = activeMember.permissions.toMutableSet()
                                    val nextActive = !isPermissionActive
                                    if (nextActive) {
                                        nextSet.add(optionRow.permission)
                                    } else {
                                        nextSet.remove(optionRow.permission)
                                    }
                                    val permissionNames = nextSet.map { perm -> perm.name }
                                    viewModel?.updateStaffPermissions(
                                        staffId = activeMember.id,
                                        permissions = permissionNames,
                                    )
                                    if (isPaymentsRow) {
                                        val needsPaymentSync = activeMember.canViewPayments != nextActive
                                        if (needsPaymentSync) {
                                            viewModel?.toggleStaffPaymentAccess(activeMember.name)
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(optionRow.label, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 28.dp)
                                    .clip(CircleShape)
                                    .background(if (isPermissionActive) Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))) else SolidColor(Color(0xFFDCDDEA)))
                                    .padding(3.dp),
                                contentAlignment = if (isPermissionActive) Alignment.CenterEnd else Alignment.CenterStart,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showInviteModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73151729))
                    .clickable { showInviteModal = false }
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Invitar miembro al equipo", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable { showInviteModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }

                    BasicText("Nombre del colaborador", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicTextField(
                        value = inviteName,
                        onValueChange = { nextName -> inviteName = nextName },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )

                    BasicText("Correo electrónico", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicTextField(
                        value = inviteEmail,
                        onValueChange = { nextEmail -> inviteEmail = nextEmail },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )

                    BasicText("Teléfono", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicTextField(
                        value = invitePhone,
                        onValueChange = { nextPhone -> invitePhone = nextPhone },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7FC))
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )

                    BasicText("Rol inicial", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        roleChoices.forEach { roleChoice ->
                            val selectedRole = inviteRoleLabel == roleChoice
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedRole) Color(0xFF4B47D6) else Color(0xFFF2F2F8))
                                    .clickable { inviteRoleLabel = roleChoice }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = roleChoice,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedRole) Color.White else Color(0xFF2E324A),
                                    ),
                                )
                            }
                        }
                    }

                    val activeModalError = inviteLocalError ?: formError
                    if (!activeModalError.isNullOrBlank()) {
                        BasicText(
                            text = activeModalError,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AdminButtonGradient)
                            .clickable {
                                val cleanName = inviteName.trim()
                                val cleanEmail = inviteEmail.trim()
                                if (cleanName.length < 2) {
                                    inviteLocalError = "Ingresa el nombre completo del colaborador."
                                } else if (!cleanEmail.contains("@")) {
                                    inviteLocalError = "Ingresa un correo electrónico válido."
                                } else {
                                    inviteLocalError = null
                                    val perms = permissionsForInvitedRoleLabel(inviteRoleLabel)
                                    val invitedMember = viewModel?.inviteNewStaffMember(
                                        fullName = cleanName,
                                        email = cleanEmail,
                                        roleLabel = inviteRoleLabel,
                                        phone = invitePhone.trim(),
                                        permissions = perms,
                                    )
                                    val canClose = if (viewModel == null) true else invitedMember != null
                                    if (canClose) {
                                        if (invitedMember != null) {
                                            selectedStaffId = invitedMember.id
                                        }
                                        showInviteModal = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Enviar invitación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}
