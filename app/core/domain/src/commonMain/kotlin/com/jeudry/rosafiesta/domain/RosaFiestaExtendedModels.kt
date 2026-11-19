package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.domain.security.JwtAuthManager

/**
 * Direct-sale balloon catalog item displayed in the balloon boutique (`s23`).
 */
public data class BalloonProduct(
    public val id: String,
    public val code: String,
    public val name: String,
    public val description: String,
    public val unitPriceDop: Int,
    public val totalStock: Int,
    public val availableStock: Int,
    public val colors: List<String>,
    public val dimensions: String = "Estándar",
    public val material: String = "Látex biodegradable y foil metálico",
    public val photo: PhotoAsset = PhotoAsset.ARCO_ROSA,
) {
    public val priceDop: Int
        get() = unitPriceDop
}

/**
 * Formal event quote and digital contract record (`s24`, `s25`, `A15`–`A18`).
 */
public data class QuoteRecord(
    public val id: String,
    public val code: String,
    public val eventId: String,
    public val eventTitle: String,
    public val clientName: String,
    public val clientEmail: String,
    public val venue: String,
    public val dateLabel: String,
    public val guests: Int,
    public val status: String,
    public val lines: List<EventLineItem>,
    public val deliveryFeeDop: Int,
    public val discountDop: Int,
    public val totalDop: Int,
    public val signatureSvg: String = "",
    public val depositPercent: Int = 30,
    public val depositDop: Int = (totalDop * depositPercent) / 100,
    public val signedByName: String = "",
    public val rejectionReason: String = "",
    public val notes: String = "",
    public val eventDateIso: String = "",
    public val packageId: String = "",
    public val contractPdfUrl: String = "",
    public val signedAtIso: String = "",
) {
    public val balanceDop: Int
        get() = (totalDop - depositDop).coerceAtLeast(0)
}

/**
 * Cancellation refund request tracked for administrative review and approval (`A32`, `A33`).
 */
public data class RefundRequest(
    public val id: String,
    public val eventId: String,
    public val eventTitle: String,
    public val clientName: String,
    public val paidDepositDop: Int,
    public val refundPercent: Int,
    public val refundAmountDop: Int,
    public val daysBefore: Int,
    public val reason: String,
    public val status: String,
    public val requestedAtLabel: String,
    public val approvedBy: String = "",
    public val approvedAtLabel: String = "",
    public val gatewayRefundId: String = "",
    public val gatewayStatus: String = "",
)

/**
 * Client rating and testimonial for a completed event (`s17`, `s71`, `A34`).
 */
public data class ReviewRecord(
    public val id: String,
    public val eventId: String,
    public val eventTitle: String,
    public val authorName: String,
    public val rating: Int,
    public val comment: String,
    public val verified: Boolean,
    public val dateLabel: String,
    public val adminReply: String = "",
)

/**
 * Delivery coverage zone with transport fee and estimated transit duration (`A35`).
 */
public data class DeliveryZone(
    public val id: String,
    public val name: String,
    public val feeDop: Int,
    public val etaMinutes: Int = 45,
    public val active: Boolean = true,
)

/**
 * Public and administrative company configuration for RosaFiesta (`A35`).
 */
public data class BusinessSettings(
    public val businessName: String = "RosaFiesta Eventos & Alquileres",
    public val rnc: String = "1-31-84920-2",
    public val address: String = "Av. Winston Churchill 1099, Piantini, Santo Domingo",
    public val phone: String = "+18095550100",
    public val whatsapp: String = "+18095550199",
    public val email: String = "hola@rosafiesta.do",
    public val businessHours: String = "Lun–Sáb 9:00 AM – 7:00 PM",
    public val instagramUrl: String = "https://instagram.com/rosafiesta.do",
    public val facebookUrl: String = "https://facebook.com/rosafiesta.do",
    public val depositPercent: Int = 30,
    public val itbisPercent: Int = 18,
    public val zones: List<DeliveryZone> = emptyList(),
)

/**
 * External floral, transport, pastry, or audiovisual partner record.
 */
public data class SupplierRecord(
    public val id: String,
    public val name: String,
    public val category: String,
    public val contactName: String,
    public val phone: String,
    public val email: String,
    public val website: String = "",
    public val notes: String = "",
    public val statusLabel: String = "Activo",
    public val specialty: String = category,
    public val rating: String = "4.9",
)

/**
 * Prospective commercial opportunity in the sales pipeline.
 */
public data class LeadRecord(
    public val id: String,
    public val clientName: String,
    public val eventType: String,
    public val dateLabel: String,
    public val guests: Int,
    public val estimatedDop: Int,
    public val channel: String,
    public val stage: String,
    public val followUpNote: String = "",
    public val phone: String = "",
    public val email: String = "",
)

/**
 * Conversation thread summary linking a client or staff member to an event.
 */
public data class ChatThread(
    public val id: String,
    public val threadId: String = id,
    public val contactName: String,
    public val eventId: String,
    public val eventTitle: String,
    public val lastMessage: String,
    public val timestampLabel: String,
    public val unreadCount: Int,
    public val roleTag: String = "Cliente",
)

/**
 * Individual message entry inside a conversation thread.
 */
public data class ChatMessageRecord(
    public val id: String,
    public val threadId: String,
    public val contactName: String,
    public val eventId: String,
    public val eventTitle: String,
    public val senderName: String,
    public val senderRole: String,
    public val body: String,
    public val attachmentName: String = "",
    public val timestampLabel: String,
    public val unreadCount: Int = 0,
    public val attachmentUrl: String = "",
)

/**
 * Confirmed or invited guest assigned to a table within an event.
 */
public data class EventGuest(
    public val id: String,
    public val eventId: String,
    public val fullName: String,
    public val rsvpStatus: String,
    public val tableLabel: String,
    public val notes: String = "",
)

/**
 * Timed run-of-show milestone inside an event schedule.
 */
public data class EventScheduleBlock(
    public val id: String,
    public val eventId: String,
    public val timeRange: String,
    public val title: String,
    public val ownerName: String,
    public val completed: Boolean = false,
    public val sortOrder: Int = 0,
)

/**
 * In-app operational or client notification item.
 */
public data class NotificationRecord(
    public val id: String,
    public val title: String,
    public val body: String,
    public val category: String,
    public val createdAtLabel: String,
    public val isRead: Boolean,
    public val recipientRole: String = "all",
    public val actionPath: String = "",
)

/**
 * Active liability or equipment insurance policy record.
 */
public data class InsurancePolicyRecord(
    public val id: String,
    public val policyNumber: String,
    public val providerName: String,
    public val coverageType: String,
    public val maxCoverageDop: Int,
    public val validUntilLabel: String,
    public val status: String,
)

/**
 * Immutable administrative audit log entry.
 */
public data class AuditLogRecord(
    public val id: String,
    public val actorName: String,
    public val actorRole: String,
    public val action: String,
    public val entityType: String,
    public val entityId: String,
    public val summary: String,
    public val createdAtLabel: String,
)

/**
 * Automated assistant frequently asked question entry.
 */
public data class BotFaqRecord(
    public val id: String,
    public val category: String,
    public val question: String,
    public val answer: String,
    public val active: Boolean = true,
)

/**
 * Automated assistant keyword trigger and handoff rule.
 */
public data class BotKeywordRecord(
    public val id: String,
    public val keyword: String,
    public val autoReply: String,
    public val handoffToHuman: Boolean = false,
)

/**
 * Automated WhatsApp or web bot conversation thread status.
 */
public data class BotThreadRecord(
    public val id: String,
    public val customerName: String,
    public val phone: String,
    public val lastMessage: String,
    public val botPaused: Boolean,
    public val assignedAgent: String = "",
    public val updatedAtLabel: String,
)

/**
 * Single day entry in an article's monthly stock availability breakdown.
 */
public data class ArticleAvailabilityDay(
    public val dateIso: String,
    public val reservedUnits: Int,
    public val availableUnits: Int,
    public val lowStock: Boolean,
)

/**
 * Monthly stock availability calendar for a rental catalog article.
 */
public data class ArticleAvailabilityCalendar(
    public val articleId: String,
    public val articleName: String,
    public val month: String,
    public val totalStock: Int,
    public val days: List<ArticleAvailabilityDay>,
)

/**
 * Outcome of requesting or verifying a one-time password recovery code.
 */
public data class OtpChallengeOutcome(
    public val success: Boolean,
    public val expiresInSeconds: Int = 0,
    public val resetToken: String = "",
    public val devCode: String = "",
    public val errorCode: String? = null,
    public val errorMessage: String? = null,
)

/**
 * Conversational reply and optional proposal generated by Rosa IA.
 */
public data class RosaChatReply(
    public val reply: String,
    public val intent: String,
    public val actionCode: String = "",
    public val styleTitle: String = "",
    public val estimatedTotalDop: Int = 0,
    public val withinBudget: Boolean = true,
    public val suggestedLines: List<EventLineItem> = emptyList(),
)

/**
 * Result of transcribing voice audio input for Rosa IA.
 */
public data class VoiceTranscriptionOutcome(
    public val transcript: String,
    public val locale: String,
    public val voiceDetected: Boolean,
    public val durationMs: Int = 0,
    public val provider: String = "local",
)

/**
 * Aggregated metric point inside a financial or operational export report.
 */
public data class ReportSeriesPoint(
    public val label: String,
    public val valueDop: Int,
    public val count: Int,
)

/**
 * Generated CSV and summary series for financial and operational reports.
 */
public data class ReportExportResult(
    public val reportType: String,
    public val period: String,
    public val filename: String,
    public val currency: String = "DOP",
    public val totalDop: Int,
    public val itbisDop: Int,
    public val generatedAt: String,
    public val csvContent: String,
    public val series: List<ReportSeriesPoint>,
)

/**
 * Uploaded binary asset returned by the backend media endpoint.
 */
public data class MediaUploadAsset(
    public val id: String,
    public val url: String,
    public val mimeType: String,
    public val byteSize: Long,
    public val filename: String,
)

/**
 * Checkout session handle returned when initiating a hosted card payment.
 */
public data class CardSessionResult(
    public val sessionId: String,
    public val checkoutUrl: String,
    public val provider: String,
)

/**
 * Stock conflict detail for a catalog article within a requested logistics window.
 */
public data class AvailabilityConflictItem(
    public val articleId: String,
    public val articleName: String,
    public val requested: Int,
    public val available: Int,
    public val dateRange: String,
)

/**
 * Result of validating multi-day pickup/event/return availability against active reservations.
 */
public data class AvailabilityRangeCheck(
    public val available: Boolean,
    public val eventDateIso: String,
    public val pickupDateIso: String,
    public val returnDateIso: String,
    public val conflicts: List<AvailabilityConflictItem> = emptyList(),
)

/**
 * Real-time SSE event pushed by the backend sync stream.
 */
public data class RealtimeStreamEvent(
    public val seq: Long,
    public val type: String,
    public val entityId: String,
    public val clientId: String = "",
    public val audience: String = "all",
    public val summary: String = "",
)

/**
 * Deterministic multiplatform civil clock and Spanish date parser anchored to Dominican Republic time (UTC-4).
 */
public object DomainClockFormatter {
    private const val DOMINICAN_UTC_OFFSET_SECONDS: Long = -4L * 3600L
    private const val SECONDS_PER_DAY: Long = 86_400L

    private val spanishShortMonths: List<String> = listOf(
        "ene",
        "feb",
        "mar",
        "abr",
        "may",
        "jun",
        "jul",
        "ago",
        "sep",
        "oct",
        "nov",
        "dic",
    )

    /**
     * Formats the current Dominican local clock time as `HH:mm`.
     */
    public fun currentTimeHmLabel(epochSeconds: Long = JwtAuthManager.currentEpochSeconds()): String {
        val localSeconds = epochSeconds + DOMINICAN_UTC_OFFSET_SECONDS
        val secondsOfDay = localSeconds.mod(SECONDS_PER_DAY)
        val hour = (secondsOfDay / 3600L).toInt()
        val minute = ((secondsOfDay % 3600L) / 60L).toInt()
        val paddedHour = hour.toString().padStart(2, '0')
        val paddedMinute = minute.toString().padStart(2, '0')
        return "$paddedHour:$paddedMinute"
    }

    /**
     * Formats the current Dominican calendar date as `dd MMM` in Spanish (for example `10 nov`).
     */
    public fun currentShortDateLabel(epochSeconds: Long = JwtAuthManager.currentEpochSeconds()): String {
        val civil = toLocalCivilDate(epochSeconds)
        val monthName = spanishShortMonths[civil.month - 1]
        return "${civil.day} $monthName"
    }

    /**
     * Formats the current Dominican calendar date as `dd MMM yyyy` in Spanish (for example `10 nov 2026`).
     */
    public fun currentFullDateLabel(epochSeconds: Long = JwtAuthManager.currentEpochSeconds()): String {
        val civil = toLocalCivilDate(epochSeconds)
        val monthName = spanishShortMonths[civil.month - 1]
        return "${civil.day} $monthName ${civil.year}"
    }

    /**
     * Formats the current Dominican year and month as `YYYY-MM`.
     */
    public fun currentYearMonthIso(epochSeconds: Long = JwtAuthManager.currentEpochSeconds()): String {
        val civil = toLocalCivilDate(epochSeconds)
        val paddedMonth = civil.month.toString().padStart(2, '0')
        return "${civil.year}-$paddedMonth"
    }

    /**
     * Returns the current Dominican civil calendar date as `(year, month, day)`.
     */
    public fun currentCivilDateTriple(
        epochSeconds: Long = JwtAuthManager.currentEpochSeconds(),
    ): Triple<Int, Int, Int> {
        val civil = toLocalCivilDate(epochSeconds)
        return Triple(civil.year, civil.month, civil.day)
    }

    /**
     * Computes whole calendar days between [nowEpochSeconds] (in UTC-4) and a Spanish, ISO, or slash [dateLabel].
     */
    public fun computeDaysUntilEvent(
        dateLabel: String,
        nowEpochSeconds: Long = JwtAuthManager.currentEpochSeconds(),
    ): Int {
        val todayCivil = toLocalCivilDate(nowEpochSeconds)
        val todayEpochDay = epochDayFromCivil(todayCivil.year, todayCivil.month, todayCivil.day)
        val parsedTarget = parseDateLabel(dateLabel, todayCivil.year, todayEpochDay) ?: return 30
        val targetEpochDay = epochDayFromCivil(parsedTarget.year, parsedTarget.month, parsedTarget.day)
        return (targetEpochDay - todayEpochDay).toInt()
    }

    private data class CivilDate(
        val year: Int,
        val month: Int,
        val day: Int,
    )

    private fun toLocalCivilDate(epochSeconds: Long): CivilDate {
        val localSeconds = epochSeconds + DOMINICAN_UTC_OFFSET_SECONDS
        val epochDay = localSeconds.floorDiv(SECONDS_PER_DAY)
        return civilFromEpochDay(epochDay)
    }

    private fun parseDateLabel(
        rawLabel: String,
        defaultYear: Int,
        todayEpochDay: Long,
    ): CivilDate? {
        val cleaned = rawLabel.trim().lowercase()
        if (cleaned.isEmpty()) return null

        val isoCandidate = parseIsoDate(cleaned)
        if (isoCandidate != null) return isoCandidate

        val slashCandidate = parseSlashDate(cleaned)
        if (slashCandidate != null) return slashCandidate

        return parseSpanishWordsDate(cleaned, defaultYear, todayEpochDay)
    }

    private fun parseIsoDate(cleaned: String): CivilDate? {
        val parts = cleaned.split("-")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].take(2).toIntOrNull() ?: return null
        return validateCivilDate(year, month, day)
    }

    private fun parseSlashDate(cleaned: String): CivilDate? {
        val parts = cleaned.split("/")
        if (parts.size != 3) return null
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        return validateCivilDate(year, month, day)
    }

    private fun parseSpanishWordsDate(
        cleaned: String,
        defaultYear: Int,
        todayEpochDay: Long,
    ): CivilDate? {
        val tokens = cleaned
            .replace(",", " ")
            .replace(".", " ")
            .split(" ")
            .map { token -> token.trim() }
            .filter { token -> token.isNotEmpty() }

        var resolvedDay: Int? = null
        var resolvedMonth: Int? = null
        var explicitYear: Int? = null

        for (token in tokens) {
            val numericValue = token.toIntOrNull()
            if (numericValue != null) {
                val isYearToken = numericValue >= 1900
                if (isYearToken) {
                    explicitYear = numericValue
                } else {
                    val hasNoDayYet = resolvedDay == null
                    if (hasNoDayYet) {
                        resolvedDay = numericValue
                    }
                }
            } else {
                val matchedMonth = matchSpanishMonth(token)
                if (matchedMonth != null) {
                    resolvedMonth = matchedMonth
                }
            }
        }

        val day = resolvedDay ?: return null
        val month = resolvedMonth ?: return null
        val hasExplicitYear = explicitYear != null
        if (hasExplicitYear) {
            return validateCivilDate(explicitYear, month, day)
        }

        val sameYearCandidate = validateCivilDate(defaultYear, month, day) ?: return null
        val candidateEpochDay = epochDayFromCivil(sameYearCandidate.year, sameYearCandidate.month, sameYearCandidate.day)
        val daysDelta = candidateEpochDay - todayEpochDay
        val shouldRollToNextYear = daysDelta < -60L
        if (shouldRollToNextYear) {
            return validateCivilDate(defaultYear + 1, month, day)
        }
        return sameYearCandidate
    }

    private fun matchSpanishMonth(token: String): Int? = when {
        token.startsWith("ene") -> 1
        token.startsWith("feb") -> 2
        token.startsWith("mar") -> 3
        token.startsWith("abr") -> 4
        token.startsWith("may") -> 5
        token.startsWith("jun") -> 6
        token.startsWith("jul") -> 7
        token.startsWith("ago") -> 8
        token.startsWith("sep") -> 9
        token.startsWith("oct") -> 10
        token.startsWith("nov") -> 11
        token.startsWith("dic") -> 12
        else -> null
    }

    private fun validateCivilDate(year: Int, month: Int, day: Int): CivilDate? {
        if (year < 1970) return null
        if (year > 2100) return null
        if (month < 1) return null
        if (month > 12) return null
        if (day < 1) return null
        if (day > 31) return null
        return CivilDate(year = year, month = month, day = day)
    }

    private fun civilFromEpochDay(epochDay: Long): CivilDate {
        val shiftedDays = epochDay + 719_468L
        val era = if (shiftedDays >= 0L) {
            shiftedDays / 146_097L
        } else {
            (shiftedDays - 146_096L) / 146_097L
        }
        val dayOfEra = shiftedDays - era * 146_097L
        val yearOfEra = (dayOfEra - dayOfEra / 1_460L + dayOfEra / 36_524L - dayOfEra / 146_096L) / 365L
        val rawYear = yearOfEra + era * 400L
        val dayOfYear = dayOfEra - (365L * yearOfEra + yearOfEra / 4L - yearOfEra / 100L)
        val marchMonthOffset = (5L * dayOfYear + 2L) / 153L
        val day = (dayOfYear - (153L * marchMonthOffset + 2L) / 5L + 1L).toInt()
        val month = if (marchMonthOffset < 10L) {
            (marchMonthOffset + 3L).toInt()
        } else {
            (marchMonthOffset - 9L).toInt()
        }
        val year = if (month <= 2) {
            (rawYear + 1L).toInt()
        } else {
            rawYear.toInt()
        }
        return CivilDate(year = year, month = month, day = day)
    }

    private fun epochDayFromCivil(year: Int, month: Int, day: Int): Long {
        val adjustedYear = if (month <= 2) year.toLong() - 1L else year.toLong()
        val era = if (adjustedYear >= 0L) {
            adjustedYear / 400L
        } else {
            (adjustedYear - 399L) / 400L
        }
        val yearOfEra = adjustedYear - era * 400L
        val marchMonth = if (month > 2) month.toLong() - 3L else month.toLong() + 9L
        val dayOfYear = (153L * marchMonth + 2L) / 5L + day.toLong() - 1L
        val dayOfEra = yearOfEra * 365L + yearOfEra / 4L - yearOfEra / 100L + dayOfYear
        return era * 146_097L + dayOfEra - 719_468L
    }
}

/**
 * OAuth provider availability and configured client IDs from `/v1/auth/oauth/config`.
 */
public data class OAuthProviderConfig(
    public val googleEnabled: Boolean = false,
    public val appleEnabled: Boolean = false,
    public val googleClientId: String = "",
    public val appleClientId: String = "",
    public val webPushVapidPublicKey: String = "",
)

/**
 * Result of dispatching a payment reminder for an event via `/v1/events/{id}/payment-reminder`.
 */
public data class PaymentReminderOutcome(
    public val event: ClientEvent,
    public val sent: Boolean,
    public val channel: String,
)

/**
 * Monthly financial bar point returned by `/v1/payments/analytics`.
 */
public data class MonthlyAnalyticsBar(
    public val monthKey: String,
    public val monthLabel: String,
    public val revenueDop: Int,
    public val expensesDop: Int,
    public val eventsCount: Int,
)

/**
 * Event volume grouped by canonical event type.
 */
public data class EventTypeCount(
    public val eventType: String,
    public val count: Int,
)

/**
 * Real aggregated analytics metrics and charts returned by `/v1/payments/analytics`.
 */
public data class PaymentsAnalyticsSummary(
    public val range: String = "30d",
    public val monthlySeries: List<MonthlyAnalyticsBar> = emptyList(),
    public val totalRevenueDop: Int = 0,
    public val totalExpensesDop: Int = 0,
    public val netMarginPercent: Int = 0,
    public val totalEventsCount: Int = 0,
    public val confirmedEventsCount: Int = 0,
    public val pendingEventsCount: Int = 0,
    public val completedEventsCount: Int = 0,
    public val cancelledEventsCount: Int = 0,
    public val eventTypesBreakdown: List<EventTypeCount> = emptyList(),
    public val uniqueClientsCount: Int = 0,
    public val repeatClientsCount: Int = 0,
    public val collectionRatePercent: Int = 0,
)

/**
 * Positioned floorplan element generated by `/v1/quotes/rosa/sketch`.
 */
public data class RosaSketchElement(
    public val id: String,
    public val kind: String,
    public val label: String,
    public val xRatio: Double,
    public val yRatio: Double,
    public val capacity: Int = 0,
)

/**
 * AI-generated 2D floorplan sketch proposal returned by `/v1/quotes/rosa/sketch`.
 */
public data class RosaSketchProposal(
    public val reply: String,
    public val elements: List<RosaSketchElement> = emptyList(),
    public val eventWidthMeters: Double = 14.0,
    public val eventDepthMeters: Double = 10.0,
    public val provider: String = "",
    public val model: String = "",
)
