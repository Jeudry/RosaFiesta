package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.DateRangeReservation
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.UserRole
import com.jeudry.rosafiesta.domain.platform.ReportCsvBuilder
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

private object RepositoryFallbackFlows {
    val catalogCategories: StateFlow<List<CatalogCategory>> = MutableStateFlow(emptyList())
    val catalogTrends: StateFlow<List<CatalogTrend>> = MutableStateFlow(emptyList())
    val seasonBanner: StateFlow<SeasonBanner> = MutableStateFlow(SeasonBanner())
    val packages: StateFlow<List<EventPackage>> = MutableStateFlow(CanonicalSampleData.packages)
    val templates: StateFlow<List<EventTypeTemplate>> = MutableStateFlow(CanonicalSampleData.templates)
    val balloonProducts: StateFlow<List<BalloonProduct>> = MutableStateFlow(CanonicalSampleData.balloonProducts)
    val clients: StateFlow<List<ClientProfile>> = MutableStateFlow(CanonicalSampleData.clients)
    val deliveryStops: StateFlow<List<DeliveryStop>> = MutableStateFlow(CanonicalSampleData.deliveryStops)
    val damageIncidents: StateFlow<List<DamageIncident>> = MutableStateFlow(CanonicalSampleData.damageIncidents)
    val quotes: StateFlow<List<QuoteRecord>> = MutableStateFlow(CanonicalSampleData.quotes)
    val refundRequests: StateFlow<List<RefundRequest>> = MutableStateFlow(CanonicalSampleData.refundRequests)
    val reviews: StateFlow<List<ReviewRecord>> = MutableStateFlow(CanonicalSampleData.reviews)
    val businessSettings: StateFlow<BusinessSettings> = MutableStateFlow(CanonicalSampleData.businessSettings)
    val suppliers: StateFlow<List<SupplierRecord>> = MutableStateFlow(CanonicalSampleData.suppliers)
    val leads: StateFlow<List<LeadRecord>> = MutableStateFlow(CanonicalSampleData.leads)
    val chatThreads: StateFlow<List<ChatThread>> = MutableStateFlow(CanonicalSampleData.chatThreads)
    val threadMessages: StateFlow<List<ChatMessageRecord>> = MutableStateFlow(CanonicalSampleData.chatMessageRecords)
    val eventGuests: StateFlow<List<EventGuest>> = MutableStateFlow(emptyList())
    val eventSchedule: StateFlow<List<EventScheduleBlock>> = MutableStateFlow(CanonicalSampleData.eventScheduleBlocks)
    val notifications: StateFlow<List<NotificationRecord>> = MutableStateFlow(emptyList())
    val insurancePolicies: StateFlow<List<InsurancePolicyRecord>> = MutableStateFlow(CanonicalSampleData.insurancePolicies)
    val auditLogs: StateFlow<List<AuditLogRecord>> = MutableStateFlow(CanonicalSampleData.auditLogs)
    val botFaqs: StateFlow<List<BotFaqRecord>> = MutableStateFlow(CanonicalSampleData.botFaqs)
    val botKeywords: StateFlow<List<BotKeywordRecord>> = MutableStateFlow(CanonicalSampleData.botKeywords)
    val botThreads: StateFlow<List<BotThreadRecord>> = MutableStateFlow(CanonicalSampleData.botThreads)
    val eventTasks: StateFlow<List<EventTaskRecord>> = MutableStateFlow(emptyList())
    val isRealtimeConnected: StateFlow<Boolean> = MutableStateFlow(false)
}

/**
 * Shared state and domain operations contract across the Client and Administrative RosaFiesta experiences.
 */
public interface RosaFiestaRepository {
    public val articles: StateFlow<List<CatalogArticle>>
    public val events: StateFlow<List<ClientEvent>>
    public val payments: StateFlow<List<PaymentRecord>>
    public val checklist: StateFlow<List<ChecklistStep>>
    public val offlineQueue: StateFlow<List<OfflineQueueItem>>
    public val metrics: StateFlow<BusinessMetrics>
    public val activeRole: StateFlow<String>
    public val currentUserEmail: StateFlow<String>
    public val currentSessionJwt: StateFlow<String?>
    public val currentAuthenticatedUser: StateFlow<RegisteredUserAccount?>
    public val registeredAccounts: StateFlow<List<RegisteredUserAccount>>
    public val favorites: StateFlow<Set<String>>
    public val staffMembers: StateFlow<List<StaffMember>>
    public val chatMessages: StateFlow<List<String>>

    public val packages: StateFlow<List<EventPackage>>
        get() = RepositoryFallbackFlows.packages
    public val catalogCategories: StateFlow<List<CatalogCategory>>
        get() = RepositoryFallbackFlows.catalogCategories
    public val catalogTrends: StateFlow<List<CatalogTrend>>
        get() = RepositoryFallbackFlows.catalogTrends
    public val seasonBanner: StateFlow<SeasonBanner>
        get() = RepositoryFallbackFlows.seasonBanner
    public val templates: StateFlow<List<EventTypeTemplate>>
        get() = RepositoryFallbackFlows.templates
    public val balloonProducts: StateFlow<List<BalloonProduct>>
        get() = RepositoryFallbackFlows.balloonProducts
    public val clients: StateFlow<List<ClientProfile>>
        get() = RepositoryFallbackFlows.clients
    public val deliveryStops: StateFlow<List<DeliveryStop>>
        get() = RepositoryFallbackFlows.deliveryStops
    public val damageIncidents: StateFlow<List<DamageIncident>>
        get() = RepositoryFallbackFlows.damageIncidents
    public val quotes: StateFlow<List<QuoteRecord>>
        get() = RepositoryFallbackFlows.quotes
    public val refundRequests: StateFlow<List<RefundRequest>>
        get() = RepositoryFallbackFlows.refundRequests
    public val reviews: StateFlow<List<ReviewRecord>>
        get() = RepositoryFallbackFlows.reviews
    public val businessSettings: StateFlow<BusinessSettings>
        get() = RepositoryFallbackFlows.businessSettings
    public val suppliers: StateFlow<List<SupplierRecord>>
        get() = RepositoryFallbackFlows.suppliers
    public val leads: StateFlow<List<LeadRecord>>
        get() = RepositoryFallbackFlows.leads
    public val chatThreads: StateFlow<List<ChatThread>>
        get() = RepositoryFallbackFlows.chatThreads
    public val threadMessages: StateFlow<List<ChatMessageRecord>>
        get() = RepositoryFallbackFlows.threadMessages
    public val eventGuests: StateFlow<List<EventGuest>>
        get() = RepositoryFallbackFlows.eventGuests
    public val eventSchedule: StateFlow<List<EventScheduleBlock>>
        get() = RepositoryFallbackFlows.eventSchedule
    public val notifications: StateFlow<List<NotificationRecord>>
        get() = RepositoryFallbackFlows.notifications
    public val insurancePolicies: StateFlow<List<InsurancePolicyRecord>>
        get() = RepositoryFallbackFlows.insurancePolicies
    public val auditLogs: StateFlow<List<AuditLogRecord>>
        get() = RepositoryFallbackFlows.auditLogs
    public val botFaqs: StateFlow<List<BotFaqRecord>>
        get() = RepositoryFallbackFlows.botFaqs
    public val botKeywords: StateFlow<List<BotKeywordRecord>>
        get() = RepositoryFallbackFlows.botKeywords
    public val botThreads: StateFlow<List<BotThreadRecord>>
        get() = RepositoryFallbackFlows.botThreads
    public val eventTasks: StateFlow<List<EventTaskRecord>>
        get() = RepositoryFallbackFlows.eventTasks
    public val isRealtimeConnected: StateFlow<Boolean>
        get() = RepositoryFallbackFlows.isRealtimeConnected

    public fun toggleChecklistStep(stepId: String)
    public fun syncOfflineQueue()
    public fun adjustArticleStock(articleId: String, delta: Int)
    public fun login(email: String, password: String): Boolean
    public fun authenticateUser(identifier: String, password: String): AuthOutcome
    public fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        phone: String = "",
    ): AuthOutcome
    public fun resetUserPassword(identifier: String, newPassword: String): AuthOutcome
    public fun updateUserProfile(
        fullName: String,
        email: String,
        phone: String,
        birthDate: String,
        username: String,
    ): AuthOutcome = AuthOutcome.failure(
        errorCode = "PROFILE_UPDATE_UNSUPPORTED",
        errorMessage = "No se pudo actualizar el perfil.",
    )
    public fun restoreSessionFromJwt(accessToken: String): RegisteredUserAccount?
    public fun clearSession()
    public fun switchRole(role: String)
    public fun toggleFavorite(articleId: String)
    public fun toggleStaffPaymentAccess(staffName: String)
    public fun sendChatMessage(author: String, message: String)
    public fun createArticle(
        code: String,
        name: String,
        category: String,
        pricePerDayDop: Int,
        totalStock: Int,
    )
    public fun createEvent(
        title: String,
        clientName: String,
        dateLabel: String,
        venue: String,
        guests: Int,
    )
    public fun changeEventDate(eventId: String, newDateLabel: String): DateChangeResult
    public fun cancelEvent(eventId: String): CancellationRefund

    /**
     * Cancela el evento indicando el motivo ingresado por el usuario.
     */
    public fun cancelEvent(eventId: String, reason: String): CancellationRefund = cancelEvent(eventId)
    public fun registerPayment(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
    ): PaymentRecord?
    public fun deleteCurrentAccount(): Boolean = true
    public fun refreshSharedState() {}

    /**
     * Synchronizes catalog, events, quotes, payments, CRM, logistics, reviews, and settings from the backend.
     */
    public suspend fun syncFromBackendAsync(): Boolean = false

    /**
     * Authenticates against the remote backend when reachable, falling back to local credential verification.
     */
    public suspend fun authenticateUserOnline(identifier: String, password: String): AuthOutcome = authenticateUser(identifier, password)

    /**
     * Registers a new account against the remote backend and awaits server persistence before authenticating.
     */
    public suspend fun registerUserOnline(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        phone: String = "",
    ): AuthOutcome = registerUser(
        firstName = firstName,
        lastName = lastName,
        username = username,
        email = email,
        password = password,
        birthDate = birthDate,
        phone = phone,
    )

    /**
     * Requests a one-time password recovery code for [email].
     */
    public suspend fun requestPasswordOtp(email: String): OtpChallengeOutcome = OtpChallengeOutcome(
        success = false,
        errorCode = "OTP_UNSUPPORTED",
        errorMessage = "Recuperación por código no disponible.",
    )

    /**
     * Verifies a one-time password recovery [code] for [email] and returns a reset token when valid.
     */
    public suspend fun verifyPasswordOtp(email: String, code: String): OtpChallengeOutcome = OtpChallengeOutcome(
        success = false,
        errorCode = "OTP_VERIFY_UNSUPPORTED",
        errorMessage = "No se pudo verificar el código.",
    )

    /**
     * Resets an account password using a verified [resetToken] or [otpCode].
     */
    public suspend fun resetPasswordWithOtp(
        email: String,
        newPassword: String,
        resetToken: String = "",
        otpCode: String = "",
    ): AuthOutcome = resetUserPassword(email, newPassword)

    /**
     * Signs in or registers an account through an OAuth identity [provider].
     */
    public suspend fun loginWithOAuth(provider: String, idToken: String): AuthOutcome = AuthOutcome.failure(
        errorCode = "OAUTH_UNSUPPORTED",
        errorMessage = "Inicio de sesión con proveedor externo no disponible.",
    )

    /**
     * Requests a PKCE OAuth authorization URL from the backend for [provider].
     */
    public suspend fun startOAuthAuthorization(
        provider: String,
        redirectUri: String,
        codeChallenge: String,
        state: String,
    ): Result<String> = Result.failure(IllegalStateException("OAuth PKCE start unavailable offline"))

    /**
     * Completes an OAuth authorization code + PKCE verifier exchange on the backend.
     */
    public suspend fun exchangeOAuthCode(
        provider: String,
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): AuthOutcome = AuthOutcome.failure(
        errorCode = "OAUTH_UNSUPPORTED",
        errorMessage = "Intercambio OAuth no disponible.",
    )

    /**
     * Exchanges a persisted [refreshToken] for a rotated access + refresh session pair.
     */
    public suspend fun refreshSession(refreshToken: String): AuthOutcome = AuthOutcome.failure(
        errorCode = "AUTH_SESSION_EXPIRED",
        errorMessage = "Tu sesión expiró. Inicia sesión nuevamente.",
    )

    /**
     * Updates the cached refresh token used for silent 401 token renewal and server-side logout revocation.
     */
    public fun setRefreshToken(token: String?): Unit = Unit

    /**
     * Registers session lifecycle callbacks invoked after background 401 token rotation or expiry.
     */
    public fun setSessionLifecycleCallbacks(
        onTokensRefreshed: suspend (accessToken: String, refreshToken: String) -> Unit,
        onSessionExpired: suspend () -> Unit,
    ): Unit = Unit

    /**
     * Registers a real device or browser push notification token for remote alerts.
     */
    public suspend fun registerDevicePushToken(
        platform: String,
        token: String,
        appVariant: String,
    ): Result<Boolean> = Result.success(false)

    /**
     * Disables one or all push notification device tokens for the authenticated account.
     */
    public suspend fun unregisterDevicePushToken(token: String = ""): Result<Boolean> = Result.success(false)

    /**
     * Updates the current user's password after verifying [currentPassword].
     */
    public fun changeUserPassword(currentPassword: String, newPassword: String): AuthOutcome = AuthOutcome.failure(
        errorCode = "PASSWORD_CHANGE_UNSUPPORTED",
        errorMessage = "No se pudo cambiar la contraseña.",
    )

    /**
     * Logs out the current session locally and notifies the remote server when online.
     */
    public fun logoutSession() {
        clearSession()
    }

    /**
     * Creates a formal quote and registers it in [quotes].
     */
    public fun createQuote(
        eventTitle: String,
        clientName: String,
        clientEmail: String = "",
        venue: String,
        dateLabel: String,
        guests: Int,
        lines: List<EventLineItem>,
        deliveryFeeDop: Int = 350,
        discountDop: Int = 0,
        eventId: String = "",
        notes: String = "",
        sendImmediately: Boolean = false,
    ): QuoteRecord {
        val subtotal = lines.sumOf { line -> line.subtotalDop }
        val total = (subtotal + deliveryFeeDop - discountDop).coerceAtLeast(0)
        return QuoteRecord(
            id = "quo-fallback",
            code = "COT-2026-0000",
            eventId = eventId,
            eventTitle = eventTitle,
            clientName = clientName,
            clientEmail = clientEmail,
            venue = venue,
            dateLabel = dateLabel,
            guests = guests,
            status = if (sendImmediately) "SENT" else "PENDING",
            lines = lines,
            deliveryFeeDop = deliveryFeeDop,
            discountDop = discountDop,
            totalDop = total,
            notes = notes,
        )
    }

    /**
     * Enriches, approves and sends an existing quote [quoteId] from Admin so the client can accept and sign it.
     */
    public fun sendQuote(
        quoteId: String,
        eventTitle: String = "",
        venue: String = "",
        dateLabel: String = "",
        guests: Int = 0,
        lines: List<EventLineItem> = emptyList(),
        deliveryFeeDop: Int? = null,
        discountDop: Int? = null,
        notes: String = "",
    ): QuoteRecord? = quotes.value.firstOrNull { quote -> quote.id == quoteId }

    /**
     * Approves and digitally signs [quoteId] with [signatureSvg].
     */
    public fun approveQuote(
        quoteId: String,
        signedByName: String,
        signatureSvg: String,
    ): QuoteRecord? = quotes.value.firstOrNull { quote -> quote.id == quoteId }

    /**
     * Declines [quoteId] with a client or administrative [reason].
     */
    public fun declineQuote(
        quoteId: String,
        reason: String,
    ): QuoteRecord? = quotes.value.firstOrNull { quote -> quote.id == quoteId }

    /**
     * Duplicates [quoteId] into a new editable pending quote.
     */
    public fun duplicateQuote(quoteId: String): QuoteRecord? = quotes.value.firstOrNull { quote -> quote.id == quoteId }

    /**
     * Sends a conversational prompt to Rosa IA and returns a structured reply and optional proposal.
     */
    public suspend fun sendRosaPrompt(
        message: String,
        eventType: String = "Boda",
        guests: Int = 100,
        budgetDop: Int = 4500,
        excludedCategories: List<String> = emptyList(),
    ): RosaChatReply = RosaChatReply(
        reply = "Propuesta ajustada para $guests invitados dentro de RD$ $budgetDop.",
        intent = "PROPOSAL",
    )

    /**
     * Transcribes a Base64-encoded WAV recording into Spanish text for Rosa IA.
     */
    public suspend fun transcribeVoicePrompt(
        wavBase64: String,
        locale: String = "es-DO",
    ): VoiceTranscriptionOutcome = VoiceTranscriptionOutcome(
        transcript = "",
        locale = locale,
        voiceDetected = false,
    )

    /**
     * Approves a pending [refundId] request.
     */
    public fun approveRefund(refundId: String): RefundRequest? = refundRequests.value.firstOrNull { request -> request.id == refundId }

    /**
     * Submits a new client [ReviewRecord] for [eventId].
     */
    public fun createReview(
        eventId: String,
        authorName: String,
        rating: Int,
        comment: String,
    ): ReviewRecord = ReviewRecord(
        id = "rev-fallback",
        eventId = eventId,
        eventTitle = "",
        authorName = authorName,
        rating = rating.coerceIn(1, 5),
        comment = comment,
        verified = true,
        dateLabel = DomainClockFormatter.currentFullDateLabel(),
    )

    /**
     * Saves an administrative [adminReply] on [reviewId].
     */
    public fun replyToReview(reviewId: String, adminReply: String): ReviewRecord? = reviews.value.firstOrNull { review -> review.id == reviewId }

    /**
     * Registers or updates a partner supplier in [suppliers].
     */
    public fun createSupplier(
        name: String,
        category: String,
        contactName: String,
        phone: String,
        email: String,
        website: String = "",
        notes: String = "",
    ): SupplierRecord = SupplierRecord(
        id = "sup-fallback",
        name = name,
        category = category,
        contactName = contactName,
        phone = phone,
        email = email,
        website = website,
        notes = notes,
    )

    /**
     * Adds a commercial opportunity into [leads].
     */
    public fun createLead(
        clientName: String,
        eventType: String,
        dateLabel: String,
        guests: Int,
        estimatedDop: Int,
        channel: String = "Web",
        stage: String = "Nuevo",
        followUpNote: String = "",
        phone: String = "",
        email: String = "",
    ): LeadRecord = LeadRecord(
        id = "lead-fallback",
        clientName = clientName,
        eventType = eventType,
        dateLabel = dateLabel,
        guests = guests,
        estimatedDop = estimatedDop,
        channel = channel,
        stage = stage,
        followUpNote = followUpNote,
        phone = phone,
        email = email,
    )

    /**
     * Updates pipeline [stage], [estimatedDop], or [followUpNote] for [leadId].
     */
    public fun updateLead(
        leadId: String,
        stage: String? = null,
        estimatedDop: Int? = null,
        followUpNote: String? = null,
    ): LeadRecord? = leads.value.firstOrNull { lead -> lead.id == leadId }

    /**
     * Updates editable attributes on [eventId].
     */
    public fun updateEventDetails(
        eventId: String,
        title: String? = null,
        venue: String? = null,
        zone: String? = null,
        guests: Int? = null,
        status: EventStatus? = null,
        leadStaffName: String? = null,
        sketchJson: String? = null,
    ): ClientEvent? = events.value.firstOrNull { event -> event.id == eventId }

    /**
     * Clones [eventId] into a new pending event draft.
     */
    public fun duplicateEvent(eventId: String): ClientEvent? = events.value.firstOrNull { event -> event.id == eventId }

    /**
     * Adds an invited guest to [eventId].
     */
    public fun addEventGuest(
        eventId: String,
        fullName: String,
        rsvpStatus: String = "PENDING",
        tableLabel: String = "Por asignar",
        notes: String = "",
    ): EventGuest = EventGuest(
        id = "gst-fallback",
        eventId = eventId,
        fullName = fullName,
        rsvpStatus = rsvpStatus,
        tableLabel = tableLabel,
        notes = notes,
    )

    /**
     * Updates RSVP status, table assignment, or dietary notes for [guestId].
     */
    public fun updateEventGuest(
        eventId: String,
        guestId: String,
        fullName: String? = null,
        rsvpStatus: String? = null,
        tableLabel: String? = null,
        notes: String? = null,
    ): EventGuest? = eventGuests.value.firstOrNull { guest -> guest.id == guestId }

    /**
     * Appends a timed run-of-show block to [eventId].
     */
    public fun addEventScheduleBlock(
        eventId: String,
        timeRange: String,
        title: String,
        ownerName: String,
        completed: Boolean = false,
    ): EventScheduleBlock = EventScheduleBlock(
        id = "sch-fallback",
        eventId = eventId,
        timeRange = timeRange,
        title = title,
        ownerName = ownerName,
        completed = completed,
    )

    /**
     * Removes [blockId] from [eventId]'s run-of-show schedule.
     */
    public fun deleteEventScheduleBlock(eventId: String, blockId: String): Boolean = false

    /**
     * Returns ordered messages belonging to [threadId].
     */
    public fun messagesForThread(threadId: String): List<ChatMessageRecord> = threadMessages.value.filter { record -> record.threadId == threadId }

    /**
     * Posts a message to [threadId] and updates the thread preview.
     */
    public fun sendThreadMessage(
        threadId: String,
        senderName: String,
        body: String,
        attachmentName: String = "",
    ): ChatMessageRecord = ChatMessageRecord(
        id = "msg-fallback",
        threadId = threadId,
        contactName = senderName,
        eventId = "",
        eventTitle = "",
        senderName = senderName,
        senderRole = activeRole.value,
        body = body,
        attachmentName = attachmentName,
        timestampLabel = DomainClockFormatter.currentTimeHmLabel(),
    )

    /**
     * Saves company contact details, tax settings, and delivery coverage zones.
     */
    public fun saveBusinessSettings(updated: BusinessSettings): BusinessSettings = updated

    /**
     * Invites a new staff member to the operational team.
     */
    public fun inviteStaffMember(
        fullName: String,
        email: String,
        roleLabel: String,
        role: String = "staff",
        phone: String = "",
        permissions: List<String> = emptyList(),
    ): StaffMember = StaffProfile(
        id = "staff-fallback",
        name = fullName,
        roleLabel = roleLabel,
        role = UserRole.STAFF_DECORATION,
        activeEventsCount = 0,
        permissions = RosaFiestaEngine.permissionsForRole(UserRole.STAFF_DECORATION),
        email = email,
        phone = phone,
    )

    /**
     * Updates a staff member's role, active state, or permissions.
     */
    public fun updateStaffMember(
        staffId: String,
        fullName: String? = null,
        roleLabel: String? = null,
        active: Boolean? = null,
        permissions: List<String>? = null,
    ): StaffMember? = staffMembers.value.firstOrNull { member -> member.id == staffId }

    /**
     * Updates administrative CRM fields for [clientId].
     */
    public fun updateClientProfile(
        clientId: String,
        fullName: String? = null,
        email: String? = null,
        phone: String? = null,
        notes: String? = null,
    ): ClientProfile? = clients.value.firstOrNull { profile -> profile.id == clientId }

    /**
     * Creates a preconfigured event rental package in [packages].
     */
    public fun createPackage(
        name: String,
        subtitle: String,
        eventType: String,
        suggestedGuests: Int,
        priceDop: Int,
        includedItems: List<String>,
        visibleInCatalog: Boolean = true,
    ): EventPackage = EventPackage(
        id = "pkg-fallback",
        name = name,
        subtitle = subtitle,
        suggestedGuests = suggestedGuests,
        priceDop = priceDop,
        savingsDop = (priceDop / 10).coerceAtLeast(50),
        photo = PhotoAsset.BODA,
        includedItems = includedItems,
        eventType = eventType,
        visibleInCatalog = visibleInCatalog,
    )

    /**
     * Updates an existing rental package in [packages].
     */
    public fun updatePackage(
        packageId: String,
        name: String,
        subtitle: String,
        eventType: String,
        suggestedGuests: Int,
        priceDop: Int,
        includedItems: List<String>,
        visibleInCatalog: Boolean = true,
    ): EventPackage? = packages.value.firstOrNull { bundle -> bundle.id == packageId }

    /**
     * Creates a new catalog category in [catalogCategories] and persists it remotely when online.
     */
    public suspend fun createCategory(
        name: String,
        illoKey: String = "mobiliario",
    ): Result<CatalogCategory> = Result.failure(IllegalStateException("CATEGORY_CREATE_UNSUPPORTED"))

    /**
     * Updates an existing catalog category's display name and illustration key.
     */
    public suspend fun updateCategory(
        categoryId: String,
        name: String,
        illoKey: String = "mobiliario",
    ): Result<CatalogCategory> = Result.failure(IllegalStateException("CATEGORY_UPDATE_UNSUPPORTED"))

    /**
     * Deletes an unused catalog category identified by [categoryId].
     */
    public suspend fun deleteCategory(categoryId: String): Result<Unit> = Result.failure(IllegalStateException("CATEGORY_DELETE_UNSUPPORTED"))

    /**
     * Creates a new seasonal trend card in [catalogTrends] and persists it remotely when online.
     */
    public suspend fun createTrend(trend: CatalogTrend): Result<CatalogTrend> = Result.failure(IllegalStateException("TREND_CREATE_UNSUPPORTED"))

    /**
     * Updates an existing seasonal trend card identified by [trend.id].
     */
    public suspend fun updateTrend(trend: CatalogTrend): Result<CatalogTrend> = Result.failure(IllegalStateException("TREND_UPDATE_UNSUPPORTED"))

    /**
     * Deletes a seasonal trend card identified by [trendId].
     */
    public suspend fun deleteTrend(trendId: String): Result<Unit> = Result.failure(IllegalStateException("TREND_DELETE_UNSUPPORTED"))

    /**
     * Saves the Personal Home seasonal hero banner configuration.
     */
    public suspend fun saveSeasonBanner(banner: SeasonBanner): Result<SeasonBanner> = Result.failure(IllegalStateException("SEASON_BANNER_UPDATE_UNSUPPORTED"))

    /**
     * Deletes an unused catalog article identified by [articleId].
     */
    public suspend fun deleteArticle(articleId: String): Result<Unit> = Result.failure(IllegalStateException("ARTICLE_DELETE_UNSUPPORTED"))

    /**
     * Updates specifications, pricing, stock, or catalog visibility on [articleId].
     */
    public fun updateArticle(
        articleId: String,
        name: String? = null,
        category: String? = null,
        description: String? = null,
        pricePerDayDop: Int? = null,
        totalStock: Int? = null,
        availableStock: Int? = null,
        dimensions: String? = null,
        material: String? = null,
        visibleInCatalog: Boolean? = null,
    ): CatalogArticle? = articles.value.firstOrNull { article -> article.id == articleId }

    /**
     * Queries day-by-day warehouse availability for [articleId] in [monthIso] (`YYYY-MM`).
     */
    public suspend fun queryArticleAvailability(
        articleId: String,
        monthIso: String = DomainClockFormatter.currentYearMonthIso(),
    ): ArticleAvailabilityCalendar {
        val article = articles.value.firstOrNull { candidate -> candidate.id == articleId }
        val total = article?.totalStock ?: 0
        val available = article?.availableOct23 ?: 0
        return ArticleAvailabilityCalendar(
            articleId = articleId,
            articleName = article?.name.orEmpty(),
            month = monthIso,
            totalStock = total,
            days = listOf(
                ArticleAvailabilityDay(
                    dateIso = "$monthIso-01",
                    reservedUnits = (total - available).coerceAtLeast(0),
                    availableUnits = available,
                    lowStock = available <= 5,
                ),
            ),
        )
    }

    /**
     * Records a post-event damage or missing-units incident for [eventId].
     */
    public fun reportDamage(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
    ): DamageIncident = DamageIncident(
        id = "dmg-fallback",
        eventTitle = "",
        articleName = articleName,
        damagedUnits = damagedUnits,
        missingUnits = missingUnits,
        replacementDop = replacementDop,
        notes = notes,
        eventId = eventId,
        articleCode = articleCode,
    )

    /**
     * Marks [notificationId] as read in [notifications].
     */
    public fun markNotificationRead(notificationId: String): NotificationRecord? = notifications.value.firstOrNull { item -> item.id == notificationId }

    /**
     * Creates or updates an automated bot FAQ item.
     */
    public fun saveBotFaq(
        question: String,
        answer: String,
        category: String = "General",
        active: Boolean = true,
        id: String = "",
    ): BotFaqRecord = BotFaqRecord(
        id = id.ifBlank { "faq-fallback" },
        category = category,
        question = question,
        answer = answer,
        active = active,
    )

    /**
     * Creates or updates an automated bot keyword rule.
     */
    public fun saveBotKeyword(
        keyword: String,
        autoReply: String,
        handoffToHuman: Boolean = false,
        id: String = "",
    ): BotKeywordRecord = BotKeywordRecord(
        id = id.ifBlank { "kw-fallback" },
        keyword = keyword,
        autoReply = autoReply,
        handoffToHuman = handoffToHuman,
    )

    /**
     * Pauses/resumes bot automation or assigns a human agent to [threadId].
     */
    public fun updateBotThread(
        threadId: String,
        botPaused: Boolean? = null,
        assignedAgent: String? = null,
        lastMessage: String? = null,
    ): BotThreadRecord? = botThreads.value.firstOrNull { thread -> thread.id == threadId }

    /**
     * Generates or fetches a financial/operational report export for [reportType] and [period].
     */
    public suspend fun exportReport(
        reportType: String = "ingresos",
        period: String = "2026-10",
    ): ReportExportResult {
        val total = payments.value.sumOf { payment -> payment.amountDop }
        val itbis = (total * 18) / 100
        return ReportExportResult(
            reportType = reportType,
            period = period,
            filename = "rosafiesta-$reportType-$period.csv",
            currency = "DOP",
            totalDop = total,
            itbisDop = itbis,
            generatedAt = DomainClockFormatter.currentFullDateLabel(),
            csvContent = "recibo,cliente,monto_dop\n",
            series = emptyList(),
        )
    }

    /**
     * Uploads an image or PDF asset to backend storage.
     */
    public suspend fun uploadMedia(
        filename: String,
        mimeType: String,
        purpose: String,
        bytes: ByteArray,
    ): Result<MediaUploadAsset> = Result.failure(IllegalStateException("MEDIA_UPLOAD_UNAVAILABLE"))

    /**
     * Marks a bank transfer or voucher payment as verified and generates fiscal receipt metadata.
     */
    public suspend fun verifyPayment(
        paymentId: String,
        notes: String = "",
    ): Result<PaymentRecord> = Result.failure(IllegalStateException("PAYMENT_VERIFICATION_UNAVAILABLE"))

    /**
     * Rejects a pending payment voucher with an explicit [reason].
     */
    public suspend fun rejectPayment(
        paymentId: String,
        reason: String,
    ): Result<PaymentRecord> = Result.failure(IllegalStateException("PAYMENT_REJECTION_UNAVAILABLE"))

    /**
     * Creates a hosted card gateway checkout session for an event payment.
     */
    public suspend fun createCardPaymentSession(
        eventId: String,
        amountDop: Int,
        ncfType: String = "B02",
        clientRnc: String = "",
    ): Result<CardSessionResult> = Result.failure(IllegalStateException("CARD_GATEWAY_NOT_CONFIGURED"))

    /**
     * Completes post-event pickup and closeout reconciliation for [eventId].
     */
    public suspend fun completeEventCloseout(
        eventId: String,
        notes: String,
    ): Result<ClientEvent> = Result.failure(IllegalStateException("EVENT_CLOSEOUT_UNAVAILABLE"))

    /**
     * Resolves a recorded post-event damage incident with closing [notes].
     */
    public suspend fun resolveDamageIncident(
        incidentId: String,
        notes: String,
    ): Result<DamageIncident> = Result.failure(IllegalStateException("DAMAGE_RESOLUTION_UNAVAILABLE"))

    /**
     * Evaluates multi-day pickup/event/return stock availability for [articleId] on [date].
     */
    public suspend fun checkRangeAvailability(
        date: String,
        articleId: String,
        quantity: Int,
        excludeEventId: String = "",
    ): Result<AvailabilityRangeCheck> = RepositoryAvailabilityFallback.check(
        date = date,
        articleId = articleId,
        quantity = quantity,
        excludeEventId = excludeEventId,
        articles = articles.value,
        events = events.value,
    )

    /**
     * Sends an operator WhatsApp reply inside [threadId].
     */
    public suspend fun sendWhatsAppBotReply(
        threadId: String,
        phone: String,
        message: String,
    ): Result<BotThreadRecord> = Result.failure(IllegalStateException("WHATSAPP_PROVIDER_NOT_CONFIGURED"))

    /**
     * Exports a DGII Formato 607 fiscal sales CSV report for [period].
     */
    public suspend fun exportDgii607Csv(
        period: String = "",
    ): Result<String> = Result.success(
        ReportCsvBuilder.buildDgii607Csv(
            payments = payments.value,
            periodYyyymm = period,
        ),
    )

    /**
     * Observes real-time backend push events from the SSE sync stream.
     */
    public fun observeRealtimeEvents(): Flow<RealtimeStreamEvent> = emptyFlow()

    /**
     * Registers a payment with optional transfer voucher URL, bank metadata, and Dominican NCF fields.
     */
    public fun registerDetailedPayment(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
        voucherUrl: String = "",
        ncfType: String = "B02",
        clientRnc: String = "",
        bankName: String = "",
        kindLabel: String = "Anticipo",
    ): PaymentRecord? = registerPayment(eventId, amountDop, methodLabel, referenceCode)

    /**
     * Invites a new staff member with an optional [initialPassword] for immediate credential login.
     */
    public fun inviteStaffMemberWithPassword(
        fullName: String,
        email: String,
        roleLabel: String,
        role: String = "staff",
        phone: String = "",
        permissions: List<String> = emptyList(),
        initialPassword: String = "",
    ): StaffMember = inviteStaffMember(fullName, email, roleLabel, role, phone, permissions)

    /**
     * Creates a catalog article including an uploaded remote [photoUrl].
     */
    public fun createArticleWithMedia(
        code: String,
        name: String,
        category: String,
        pricePerDayDop: Int,
        totalStock: Int,
        photoUrl: String = "",
    ) {
        createArticle(code, name, category, pricePerDayDop, totalStock)
    }

    /**
     * Updates an existing catalog article including an uploaded remote [photoUrl].
     */
    public fun updateArticleWithMedia(
        articleId: String,
        name: String? = null,
        category: String? = null,
        description: String? = null,
        pricePerDayDop: Int? = null,
        totalStock: Int? = null,
        availableStock: Int? = null,
        dimensions: String? = null,
        material: String? = null,
        visibleInCatalog: Boolean? = null,
        photoUrl: String? = null,
    ): CatalogArticle? = updateArticle(
        articleId = articleId,
        name = name,
        category = category,
        description = description,
        pricePerDayDop = pricePerDayDop,
        totalStock = totalStock,
        availableStock = availableStock,
        dimensions = dimensions,
        material = material,
        visibleInCatalog = visibleInCatalog,
    )

    /**
     * Records a post-event damage or missing-units incident with an optional evidence [photoUrl].
     */
    public fun reportDamageWithMedia(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
        photoUrl: String = "",
    ): DamageIncident = reportDamage(
        eventId = eventId,
        articleCode = articleCode,
        articleName = articleName,
        damagedUnits = damagedUnits,
        missingUnits = missingUnits,
        replacementDop = replacementDop,
        notes = notes,
    )

    /**
     * Downloads raw binary image or PDF bytes for a stored media [urlOrId].
     */
    public suspend fun fetchMediaBytes(urlOrId: String): Result<ByteArray> = Result.failure(IllegalStateException("MEDIA_FETCH_UNAVAILABLE"))

    /**
     * Queries backend OAuth provider enablement and client identifiers.
     */
    public suspend fun fetchOAuthConfig(): Result<OAuthProviderConfig> = Result.success(OAuthProviderConfig())

    /**
     * Sends a username reminder email by email, phone, or document identifier.
     */
    public suspend fun recoverUsernameByIdentifier(identifier: String): Result<String> = Result.failure(IllegalStateException("USERNAME_RECOVERY_UNAVAILABLE"))

    /**
     * Updates persistent notification, Wi-Fi sync, and biometric lock preferences on the authenticated account.
     */
    public suspend fun updateUserPreferences(
        pushNotificationsEnabled: Boolean? = null,
        whatsappAlertsEnabled: Boolean? = null,
        emailReceiptsEnabled: Boolean? = null,
        biometricLockEnabled: Boolean? = null,
        autoSyncWifiOnly: Boolean? = null,
        eventRemindersEnabled: Boolean? = null,
        preferredLanguage: String? = null,
        darkThemeEnabled: Boolean? = null,
        savedPaymentMethod: String? = null,
        profileZone: String? = null,
    ): Result<RegisteredUserAccount> {
        val current = currentAuthenticatedUser.value
            ?: return Result.failure(IllegalStateException("AUTH_UNAUTHORIZED"))
        val updatedPrefs = UserPreferences(
            pushNotificationsEnabled = pushNotificationsEnabled ?: current.preferences.pushNotificationsEnabled,
            whatsappAlertsEnabled = whatsappAlertsEnabled ?: current.preferences.whatsappAlertsEnabled,
            emailReceiptsEnabled = emailReceiptsEnabled ?: current.preferences.emailReceiptsEnabled,
            biometricLockEnabled = biometricLockEnabled ?: current.preferences.biometricLockEnabled,
            autoSyncWifiOnly = autoSyncWifiOnly ?: current.preferences.autoSyncWifiOnly,
            eventRemindersEnabled = eventRemindersEnabled ?: current.preferences.eventRemindersEnabled,
            preferredLanguage = preferredLanguage ?: current.preferences.preferredLanguage,
            darkThemeEnabled = darkThemeEnabled ?: current.preferences.darkThemeEnabled,
            savedPaymentMethod = savedPaymentMethod ?: current.preferences.savedPaymentMethod,
            profileZone = profileZone ?: current.preferences.profileZone,
        )
        return Result.success(current.copy(preferences = updatedPrefs))
    }

    /**
     * Sends an email/WhatsApp payment reminder for [eventId] and updates reminder tracking fields.
     */
    public suspend fun sendEventPaymentReminder(
        eventId: String,
        channel: String = "whatsapp",
        message: String = "",
    ): Result<PaymentReminderOutcome> = Result.failure(IllegalStateException("PAYMENT_REMINDER_UNAVAILABLE"))

    /**
     * Confirms a Stripe Checkout session and records/verifies the resulting payment.
     */
    public suspend fun confirmCardCheckoutSession(sessionId: String): Result<PaymentRecord> = Result.failure(IllegalStateException("CARD_CONFIRM_UNAVAILABLE"))

    /**
     * Fetches real aggregated monthly revenue/expense series and KPI metrics for [range].
     */
    public suspend fun fetchPaymentsAnalytics(range: String = "30d"): Result<PaymentsAnalyticsSummary> = Result.failure(IllegalStateException("ANALYTICS_UNAVAILABLE"))

    /**
     * Requests a Rosa AI 2D spatial layout sketch proposal via `/v1/quotes/rosa/sketch`.
     */
    public suspend fun requestRosaSketch(
        prompt: String,
        eventType: String = "",
        guests: Int = 80,
    ): Result<RosaSketchProposal> = Result.failure(IllegalStateException("ROSA_SKETCH_UNAVAILABLE"))

    /**
     * Persists updated furniture/item allocations onto [eventId] via `/v1/events/{id}`.
     */
    public suspend fun updateEventItems(
        eventId: String,
        items: List<EventLineItem>,
    ): Result<ClientEvent> = Result.failure(IllegalStateException("EVENT_UPDATE_UNAVAILABLE"))

    /**
     * Verifies the currently signed-in account's [password] to unlock the App Lock screen when biometrics are unavailable.
     */
    public suspend fun verifyAccountPassword(password: String): Result<Boolean> = Result.success(false)

    /**
     * Cambia la contraseña validando en el servidor cuando hay sesión activa en línea.
     */
    public suspend fun changeUserPasswordOnline(currentPassword: String, newPassword: String): AuthOutcome = changeUserPassword(currentPassword, newPassword)

    /**
     * Elimina la cuenta autenticada esperando confirmación del servidor antes de cerrar sesión.
     */
    public suspend fun deleteCurrentAccountOnline(): Result<Unit> = if (deleteCurrentAccount()) Result.success(Unit) else Result.failure(IllegalStateException("ACCOUNT_DELETE_FAILED"))

    /**
     * Aprueba una solicitud de reembolso pasando opciones de inventario, método y notificación WhatsApp.
     */
    public fun approveRefundWithOptions(refundId: String, options: RefundApprovalOptions): RefundRequest? = approveRefund(refundId)

    /**
     * Elimina un invitado de la lista del evento [eventId].
     */
    public fun deleteEventGuest(eventId: String, guestId: String): Boolean = false

    /**
     * Carga las tareas colaborativas del evento [eventId] desde el backend.
     */
    public suspend fun loadEventTasks(eventId: String): List<EventTaskRecord> = emptyList()

    /**
     * Crea una nueva tarea colaborativa asociada al evento [eventId].
     */
    public fun createEventTask(eventId: String, title: String, assigneeName: String): EventTaskRecord? = null

    /**
     * Marca o desmarca como completada la tarea [taskId] del evento [eventId].
     */
    public fun toggleEventTask(eventId: String, taskId: String, done: Boolean): EventTaskRecord? = null

    /**
     * Elimina una tarea colaborativa [taskId] del evento [eventId].
     */
    public fun deleteEventTask(eventId: String, taskId: String): Boolean = false

    /**
     * Actualiza un proveedor existente en el directorio operativo.
     */
    public fun updateSupplier(
        supplierId: String,
        name: String,
        category: String,
        contactName: String,
        phone: String,
        email: String,
        website: String = "",
        notes: String = "",
    ): SupplierRecord? = null

    /**
     * Elimina un proveedor del directorio operativo.
     */
    public fun deleteSupplier(supplierId: String): Boolean = false

    /**
     * Elimina una pregunta frecuente del bot de WhatsApp.
     */
    public fun deleteBotFaq(faqId: String): Boolean = false

    /**
     * Elimina una regla de palabra clave del bot de WhatsApp.
     */
    public fun deleteBotKeyword(keywordId: String): Boolean = false

    /**
     * Elimina un miembro del equipo operativo.
     */
    public fun deleteStaffMember(staffId: String): Boolean = false

    /**
     * Elimina un paquete preconfigurado del catálogo.
     */
    public fun deletePackage(packageId: String): Boolean = false

    /**
     * Descarga el contrato PDF real firmado para [quoteId].
     */
    public suspend fun downloadQuoteContractPdf(quoteId: String): Result<ByteArray> = Result.failure(IllegalStateException("CONTRACT_PDF_UNAVAILABLE"))
}

internal object RepositoryAvailabilityFallback {
    internal fun check(
        date: String,
        articleId: String,
        quantity: Int,
        excludeEventId: String,
        articles: List<CatalogArticle>,
        events: List<ClientEvent>,
    ): Result<AvailabilityRangeCheck> {
        val window = RosaFiestaEngine.computeTurnaroundWindow(date)
            ?: return Result.failure(IllegalArgumentException("INVALID_EVENT_DATE"))
        val article = findCatalogArticle(articles, articleId)
            ?: return Result.failure(IllegalArgumentException("ARTICLE_NOT_FOUND"))
        val reservations = buildDateRangeReservations(events, article)
        val availableStock = RosaFiestaEngine.computeAvailableStockForRange(
            totalStock = article.totalStock,
            damagedStock = article.damagedStock,
            reservations = reservations,
            articleId = article.id,
            pickupDateIso = window.pickupDateIso,
            returnDateIso = window.returnDateIso,
            excludeEventId = excludeEventId,
        )
        val safeRequested = quantity.coerceAtLeast(0)
        val isAvailable = availableStock >= safeRequested
        val conflicts = if (isAvailable) {
            emptyList()
        } else {
            listOf(
                AvailabilityConflictItem(
                    articleId = article.id,
                    articleName = article.name,
                    requested = safeRequested,
                    available = availableStock,
                    dateRange = "${window.pickupDateIso}..${window.returnDateIso}",
                ),
            )
        }
        return Result.success(
            AvailabilityRangeCheck(
                available = isAvailable,
                eventDateIso = window.eventDateIso,
                pickupDateIso = window.pickupDateIso,
                returnDateIso = window.returnDateIso,
                conflicts = conflicts,
            ),
        )
    }

    private fun findCatalogArticle(
        articles: List<CatalogArticle>,
        articleIdOrCode: String,
    ): CatalogArticle? {
        val target = articleIdOrCode.trim()
        for (article in articles) {
            if (article.id == target) return article
            if (article.code == target) return article
        }
        return null
    }

    private fun buildDateRangeReservations(
        events: List<ClientEvent>,
        article: CatalogArticle,
    ): List<DateRangeReservation> {
        val reservations = mutableListOf<DateRangeReservation>()
        for (event in events) {
            if (!isInventoryHoldingStatus(event.status)) continue
            val turnaround = resolveEventTurnaround(event) ?: continue
            val matchedQuantity = sumMatchingArticleQuantity(event.items, article)
            if (matchedQuantity <= 0) continue
            reservations.add(
                DateRangeReservation(
                    articleId = article.id,
                    eventId = event.id,
                    quantity = matchedQuantity,
                    pickupDateIso = turnaround.first,
                    returnDateIso = turnaround.second,
                    status = toReservationStatus(event.status),
                ),
            )
        }
        return reservations
    }

    private fun isInventoryHoldingStatus(status: EventStatus): Boolean {
        if (status == EventStatus.CANCELLED) return false
        return status != EventStatus.COMPLETED
    }

    private fun resolveEventTurnaround(event: ClientEvent): Pair<String, String>? {
        val hasExplicitPickup = event.pickupDateIso.isNotBlank()
        val hasExplicitReturn = event.returnDateIso.isNotBlank()
        if (hasExplicitPickup) {
            if (hasExplicitReturn) {
                return event.pickupDateIso.trim() to event.returnDateIso.trim()
            }
        }
        val rawDate = event.eventDateIso.ifBlank { event.dateLabel }
        val window = RosaFiestaEngine.computeTurnaroundWindow(rawDate) ?: return null
        return window.pickupDateIso to window.returnDateIso
    }

    private fun sumMatchingArticleQuantity(
        items: List<EventLineItem>,
        article: CatalogArticle,
    ): Int {
        var total = 0
        for (item in items) {
            val matchesArticleCode = item.articleCode == article.code
            val matchesArticleId = item.articleCode == article.id
            if (matchesArticleCode) {
                total += item.quantity.coerceAtLeast(0)
            } else if (matchesArticleId) {
                total += item.quantity.coerceAtLeast(0)
            }
        }
        return total
    }

    private fun toReservationStatus(status: EventStatus): String = if (status == EventStatus.PENDING_QUOTE) "held" else "confirmed"
}
