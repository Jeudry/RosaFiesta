package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.core.engine.UserRole
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.AuditLogRecord
import com.jeudry.rosafiesta.domain.AvailabilityRangeCheck
import com.jeudry.rosafiesta.domain.BalloonProduct
import com.jeudry.rosafiesta.domain.BotFaqRecord
import com.jeudry.rosafiesta.domain.BotKeywordRecord
import com.jeudry.rosafiesta.domain.BotThreadRecord
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CardSessionResult
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.CatalogCategory
import com.jeudry.rosafiesta.domain.CatalogTrend
import com.jeudry.rosafiesta.domain.ChatMessageRecord
import com.jeudry.rosafiesta.domain.ChatThread
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.DamageIncident
import com.jeudry.rosafiesta.domain.DeliveryStop
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.EventGuest
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.EventPackage
import com.jeudry.rosafiesta.domain.EventScheduleBlock
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.EventTaskRecord
import com.jeudry.rosafiesta.domain.EventTypeTemplate
import com.jeudry.rosafiesta.domain.InsurancePolicyRecord
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.MediaUploadAsset
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.OAuthProviderConfig
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.OtpChallengeOutcome
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PaymentReminderOutcome
import com.jeudry.rosafiesta.domain.PaymentsAnalyticsSummary
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.RealtimeStreamEvent
import com.jeudry.rosafiesta.domain.RefundApprovalOptions
import com.jeudry.rosafiesta.domain.RefundRequest
import com.jeudry.rosafiesta.domain.ReportExportResult
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.RosaChatReply
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.RosaSketchProposal
import com.jeudry.rosafiesta.domain.SeasonBanner
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.security.UserPreferences
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistent SQLDelight-backed implementation of [RosaFiestaRepository] synchronized with [RosaFiestaApiClient].
 * Executes all SQLite reads/writes and HTTP requests off the main thread via [ioDispatcher].
 */
public class SqlRosaFiestaRepository(
    driver: SqlDriver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val apiClient: RosaFiestaApiClient? = null,
    private val seedDemoData: Boolean = true,
) : RosaFiestaRepository {
    private val database: RosaFiestaDatabase = RosaFiestaDatabase(driver)
    private val queries = database.rosaFiestaDatabaseQueries
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val sqlMutex = Mutex()
    private val sqlJsonCodec: Json = Json { ignoreUnknownKeys = true }
    private val initialSeedAccounts: List<RegisteredUserAccount> = if (seedDemoData) {
        JwtAuthManager.defaultSeedAccounts
    } else {
        JwtAuthManager.defaultAdminSeedAccounts
    }
    private val initialLoggedUser: RegisteredUserAccount? = if (seedDemoData) {
        initialSeedAccounts.firstOrNull()
    } else {
        null
    }

    private val checklistEventIdsByStepId: MutableMap<String, String> = if (seedDemoData) {
        CanonicalSampleData.bodaChecklist
            .associate { step -> step.id to step.eventId }
            .toMutableMap()
    } else {
        mutableMapOf()
    }

    private val _articles = MutableStateFlow(if (seedDemoData) CanonicalSampleData.articles else emptyList())
    override val articles: StateFlow<List<CatalogArticle>> = _articles.asStateFlow()

    private val _events = MutableStateFlow(if (seedDemoData) CanonicalSampleData.events else emptyList())
    override val events: StateFlow<List<ClientEvent>> = _events.asStateFlow()

    private val _payments = MutableStateFlow(if (seedDemoData) CanonicalSampleData.payments else emptyList())
    override val payments: StateFlow<List<PaymentRecord>> = _payments.asStateFlow()

    private val _checklist = MutableStateFlow(if (seedDemoData) CanonicalSampleData.bodaChecklist else emptyList())
    override val checklist: StateFlow<List<ChecklistStep>> = _checklist.asStateFlow()

    private val _offlineQueue = MutableStateFlow(if (seedDemoData) CanonicalSampleData.offlineQueue else emptyList())
    override val offlineQueue: StateFlow<List<OfflineQueueItem>> = _offlineQueue.asStateFlow()

    private val _metrics = MutableStateFlow(
        if (seedDemoData) CanonicalSampleData.businessMetrics else CanonicalSampleData.zeroBusinessMetrics,
    )
    override val metrics: StateFlow<BusinessMetrics> = _metrics.asStateFlow()

    private val _activeRole = MutableStateFlow(if (seedDemoData) "client" else "guest")
    override val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(if (seedDemoData) "camila@rosafiesta.do" else "")
    override val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val userAccountsState = MutableStateFlow(initialSeedAccounts)
    override val registeredAccounts: StateFlow<List<RegisteredUserAccount>> = userAccountsState.asStateFlow()

    private val _currentAuthenticatedUser = MutableStateFlow<RegisteredUserAccount?>(initialLoggedUser)
    override val currentAuthenticatedUser: StateFlow<RegisteredUserAccount?> = _currentAuthenticatedUser.asStateFlow()

    private val _currentSessionJwt = MutableStateFlow<String?>(
        initialLoggedUser?.let { seedUser -> JwtAuthManager.issueAccessToken(seedUser) },
    )
    override val currentSessionJwt: StateFlow<String?> = _currentSessionJwt.asStateFlow()

    private val _favorites = MutableStateFlow(if (seedDemoData) CanonicalSampleData.initialFavorites else emptySet())
    override val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _staffMembers = MutableStateFlow(
        if (seedDemoData) CanonicalSampleData.staffMembers else CanonicalSampleData.emptyAdminStaffMembers,
    )
    override val staffMembers: StateFlow<List<StaffMember>> = _staffMembers.asStateFlow()

    private val _chatMessages = MutableStateFlow(if (seedDemoData) CanonicalSampleData.initialChatMessages else emptyList())
    override val chatMessages: StateFlow<List<String>> = _chatMessages.asStateFlow()

    private val submodules = RosaFiestaSubmoduleCoordinator(
        apiClient = apiClient,
        ioDispatcher = ioDispatcher,
        scope = scope,
        articlesFlow = _articles,
        eventsFlow = _events,
        paymentsFlow = _payments,
        offlineQueueFlow = _offlineQueue,
        metricsFlow = _metrics,
        staffFlow = _staffMembers,
        chatMessagesFlow = _chatMessages,
        activeRoleFlow = _activeRole,
        currentUserEmailFlow = _currentUserEmail,
        currentSessionJwtFlow = _currentSessionJwt,
        currentAuthenticatedUserFlow = _currentAuthenticatedUser,
        userAccountsFlow = userAccountsState,
        onPersistAccountAndSession = ::persistAccountAndSessionInSql,
        onPersistCoreSync = ::persistInMemoryCoreFlowsToSql,
        seedDemoData = seedDemoData,
        favoritesFlow = _favorites,
        onReplayPendingOutbox = ::syncOfflineQueueAsync,
        onEnqueueOfflineMutation = ::enqueueOutboxMutation,
    )

    override val catalogCategories: StateFlow<List<CatalogCategory>>
        get() = submodules.catalogCategories
    override val catalogTrends: StateFlow<List<CatalogTrend>>
        get() = submodules.catalogTrends
    override val seasonBanner: StateFlow<SeasonBanner>
        get() = submodules.seasonBanner
    override val packages: StateFlow<List<EventPackage>>
        get() = submodules.packages
    override val templates: StateFlow<List<EventTypeTemplate>>
        get() = submodules.templates
    override val balloonProducts: StateFlow<List<BalloonProduct>>
        get() = submodules.balloonProducts
    override val clients: StateFlow<List<ClientProfile>>
        get() = submodules.clients
    override val deliveryStops: StateFlow<List<DeliveryStop>>
        get() = submodules.deliveryStops
    override val damageIncidents: StateFlow<List<DamageIncident>>
        get() = submodules.damageIncidents
    override val quotes: StateFlow<List<QuoteRecord>>
        get() = submodules.quotes
    override val refundRequests: StateFlow<List<RefundRequest>>
        get() = submodules.refundRequests
    override val reviews: StateFlow<List<ReviewRecord>>
        get() = submodules.reviews
    override val businessSettings: StateFlow<BusinessSettings>
        get() = submodules.businessSettings
    override val suppliers: StateFlow<List<SupplierRecord>>
        get() = submodules.suppliers
    override val leads: StateFlow<List<LeadRecord>>
        get() = submodules.leads
    override val chatThreads: StateFlow<List<ChatThread>>
        get() = submodules.chatThreads
    override val threadMessages: StateFlow<List<ChatMessageRecord>>
        get() = submodules.threadMessages
    override val eventGuests: StateFlow<List<EventGuest>>
        get() = submodules.eventGuests
    override val eventSchedule: StateFlow<List<EventScheduleBlock>>
        get() = submodules.eventSchedule
    override val notifications: StateFlow<List<NotificationRecord>>
        get() = submodules.notifications
    override val insurancePolicies: StateFlow<List<InsurancePolicyRecord>>
        get() = submodules.insurancePolicies
    override val auditLogs: StateFlow<List<AuditLogRecord>>
        get() = submodules.auditLogs
    override val botFaqs: StateFlow<List<BotFaqRecord>>
        get() = submodules.botFaqs
    override val botKeywords: StateFlow<List<BotKeywordRecord>>
        get() = submodules.botKeywords
    override val botThreads: StateFlow<List<BotThreadRecord>>
        get() = submodules.botThreads
    override val eventTasks: StateFlow<List<EventTaskRecord>>
        get() = submodules.eventTasks
    override val isRealtimeConnected: StateFlow<Boolean>
        get() = submodules.isRealtimeConnected

    init {
        seedInitialDataSynchronouslyIfEmpty()
        restorePersistedSessionFromSql()
        submodules.propagateSessionTokenToApi(_currentSessionJwt.value)
        if (apiClient != null) {
            scope.launch {
                syncUsersFromBackendSuspend()
                syncFromBackendAsync()
            }
        }
    }

    override suspend fun syncFromBackendAsync(): Boolean = submodules.syncFromBackendAsync()

    private fun upsertUserAccountRow(account: RegisteredUserAccount) {
        queries.upsertUserAccount(
            id = account.id,
            username = account.username,
            email = account.email,
            fullName = account.fullName,
            passwordHash = account.passwordHash,
            salt = account.salt,
            birthDate = account.birthDate,
            phone = account.phone,
            role = account.role,
            pushNotificationsEnabled = if (account.preferences.pushNotificationsEnabled) 1L else 0L,
            whatsappAlertsEnabled = if (account.preferences.whatsappAlertsEnabled) 1L else 0L,
            emailReceiptsEnabled = if (account.preferences.emailReceiptsEnabled) 1L else 0L,
            biometricLockEnabled = if (account.preferences.biometricLockEnabled) 1L else 0L,
            autoSyncWifiOnly = if (account.preferences.autoSyncWifiOnly) 1L else 0L,
            eventRemindersEnabled = if (account.preferences.eventRemindersEnabled) 1L else 0L,
            preferredLanguage = account.preferences.preferredLanguage.ifBlank { "es" },
            darkThemeEnabled = if (account.preferences.darkThemeEnabled) 1L else 0L,
            savedPaymentMethod = account.preferences.savedPaymentMethod.ifBlank { "Transferencia" },
            profileZone = account.preferences.profileZone.ifBlank { "Distrito Nacional" },
            avatarKey = account.avatarKey,
        )
    }

    private suspend fun persistAccountAndSessionInSql(
        account: RegisteredUserAccount,
        accessToken: String,
    ) {
        sqlMutex.withLock {
            upsertUserAccountRow(account)
            queries.upsertActiveSession(
                userId = account.id,
                accessToken = accessToken,
            )
            reloadUserAccountsFromSql()
        }
    }

    private suspend fun persistInMemoryCoreFlowsToSql() {
        val submoduleSnapshot = submodules.snapshotForPersistence()
        sqlMutex.withLock {
            database.transaction {
                queries.deleteAllArticles()
                _articles.value.forEach { article ->
                    upsertArticleRow(article)
                }
                _events.value.forEachIndexed { index, evt ->
                    upsertEventRow(evt, index)
                }
                _payments.value.forEachIndexed { index, payment ->
                    upsertPaymentRow(payment, index)
                }
                _offlineQueue.value.forEach { mutation ->
                    upsertOfflineMutationRow(mutation)
                }
                queries.clearStaffPermissions()
                _staffMembers.value.forEachIndexed { index, member ->
                    queries.upsertStaffPermission(
                        id = member.id,
                        name = member.name,
                        roleLabel = member.roleLabel,
                        role = member.role.name,
                        activeEventsCount = member.activeEventsCount.toLong(),
                        permissionsCsv = member.permissions.joinToString(",") { perm -> perm.name },
                        canViewPayments = if (member.canViewPayments) 1L else 0L,
                        sortOrder = index.toLong(),
                        email = member.email,
                        phone = member.phone,
                        active = if (member.active) 1L else 0L,
                    )
                }
                persistSubmoduleSnapshotRows(submoduleSnapshot)
            }
        }
    }

    private fun upsertArticleRow(article: CatalogArticle) {
        queries.upsertArticle(
            id = article.id,
            code = article.code,
            name = article.name,
            category = article.category,
            description = article.description,
            pricePerDayDop = article.pricePerDayDop.toLong(),
            totalStock = article.totalStock.toLong(),
            availableOct23 = article.availableOct23.toLong(),
            photoKey = article.photo.key,
            photoUrl = article.photoUrl,
            damagedStock = article.damagedStock.toLong(),
            variantsJson = sqlJsonCodec.encodeToString(article.variants),
            dimensions = article.dimensions,
            material = article.material,
            visibleInCatalog = if (article.visibleInCatalog) 1L else 0L,
        )
    }

    private fun upsertEventRow(evt: ClientEvent, sortIndex: Int) {
        val itemDtos = evt.items.map { item ->
            EventItemDto(
                articleCode = item.articleCode,
                name = item.name,
                quantity = item.quantity.toLong(),
                unitPriceRD = item.unitPriceDop.toLong(),
            )
        }
        queries.upsertEvent(
            id = evt.id,
            code = evt.code,
            title = evt.title,
            clientName = evt.clientName,
            eventType = evt.eventType,
            dateLabel = evt.dateLabel,
            daysUntilEvent = evt.daysUntilEvent.toLong(),
            venue = evt.venue,
            zone = evt.zone,
            guests = evt.guests.toLong(),
            articleCount = evt.articleCount.toLong(),
            totalDop = evt.totalDop.toLong(),
            paidDepositDop = evt.paidDepositDop.toLong(),
            depositMethodLabel = evt.depositMethodLabel,
            depositDateLabel = evt.depositDateLabel,
            balanceDueDateLabel = evt.balanceDueDateLabel,
            status = evt.status.name,
            leadStaffName = evt.leadStaffName,
            photoKey = evt.photo.key,
            priorChangesCount = evt.priorChangesCount.toLong(),
            sortOrder = sortIndex.toLong(),
            eventDateIso = evt.eventDateIso,
            pickupDateIso = evt.pickupDateIso,
            returnDateIso = evt.returnDateIso,
            quoteId = evt.quoteId,
            closeoutCompleted = if (evt.closeoutCompleted) 1L else 0L,
            closeoutNotes = evt.closeoutNotes,
            sketchJson = evt.sketchJson,
            lastPaymentReminderAt = evt.lastPaymentReminderAt,
            lastPaymentReminderChannel = evt.lastPaymentReminderChannel,
            itemsJson = sqlJsonCodec.encodeToString(itemDtos),
            clientEmail = evt.clientEmail,
            cancellationReason = evt.cancellationReason,
            setupPhotoUrlsJson = sqlJsonCodec.encodeToString(evt.setupPhotoUrls),
            contactPhone = evt.contactPhone,
        )
    }

    private fun upsertPaymentRow(payment: PaymentRecord, sortIndex: Int) {
        queries.upsertPayment(
            id = payment.id,
            receiptNumber = payment.receiptNumber,
            eventId = payment.eventId,
            eventTitle = payment.eventTitle,
            clientName = payment.clientName,
            amountDop = payment.amountDop.toLong(),
            methodLabel = payment.methodLabel,
            kindLabel = payment.kindLabel,
            dateLabel = payment.dateLabel,
            referenceCode = payment.referenceCode,
            verified = if (payment.verified) 1L else 0L,
            sortOrder = sortIndex.toLong(),
            voucherUrl = payment.voucherUrl,
            ncfType = payment.ncfType,
            ncfNumber = payment.ncfNumber,
            clientRnc = payment.clientRnc,
            subtotalDop = payment.subtotalDop.toLong(),
            itbisDop = payment.itbisDop.toLong(),
            securityDepositDop = payment.securityDepositDop.toLong(),
            rejectionReason = payment.rejectionReason,
        )
    }

    private fun upsertOfflineMutationRow(mutation: OfflineQueueItem) {
        queries.upsertOfflineMutation(
            id = mutation.id,
            actionLabel = mutation.actionLabel,
            entityCode = mutation.entityCode,
            timeAgo = mutation.timeAgo,
            synced = if (mutation.synced) 1L else 0L,
            operationType = mutation.operationType,
            payloadJson = mutation.payloadJson.ifBlank { "{}" },
            idempotencyKey = mutation.idempotencyKey,
        )
    }

    private fun persistSubmoduleSnapshotRows(snapshot: PersistedSubmoduleSnapshot) {
        queries.deleteAllQuotes()
        snapshot.quotes.forEachIndexed { index, quote ->
            val dtoLines = quote.lines.map { line ->
                QuoteLineDto(
                    articleCode = line.articleCode,
                    name = line.name,
                    quantity = line.quantity.toLong(),
                    unitPriceRD = line.unitPriceDop.toLong(),
                )
            }
            queries.upsertQuote(
                id = quote.id,
                code = quote.code,
                eventId = quote.eventId,
                eventTitle = quote.eventTitle,
                clientName = quote.clientName,
                clientEmail = quote.clientEmail,
                venue = quote.venue,
                dateLabel = quote.dateLabel,
                guests = quote.guests.toLong(),
                status = quote.status,
                linesJson = sqlJsonCodec.encodeToString(dtoLines),
                deliveryFeeDop = quote.deliveryFeeDop.toLong(),
                discountDop = quote.discountDop.toLong(),
                totalDop = quote.totalDop.toLong(),
                signatureSvg = quote.signatureSvg,
                depositPercent = quote.depositPercent.toLong(),
                depositDop = quote.depositDop.toLong(),
                signedByName = quote.signedByName,
                rejectionReason = quote.rejectionReason,
                eventDateIso = quote.eventDateIso,
                packageId = quote.packageId,
                contractPdfUrl = quote.contractPdfUrl,
                signedAtIso = quote.signedAtIso,
                sortOrder = index.toLong(),
                contactPhone = quote.contactPhone,
                signedByUserId = quote.signedByUserId,
                contractHashSha256 = quote.contractHashSha256,
            )
        }
        queries.clearDeliveryStops()
        snapshot.deliveryStops.forEach { stop ->
            queries.upsertDeliveryStop(
                stopOrder = stop.order.toLong(),
                eventId = stop.eventId,
                eventTitle = stop.eventTitle,
                venue = stop.venue,
                windowLabel = stop.windowLabel,
                vehicleAndCrew = stop.vehicleAndCrew,
                statusLabel = stop.statusLabel,
            )
        }
        queries.deleteAllDamageIncidents()
        snapshot.damageIncidents.forEach { incident ->
            queries.upsertDamageIncident(
                id = incident.id,
                eventId = incident.eventId,
                eventTitle = incident.eventTitle,
                articleCode = incident.articleCode,
                articleName = incident.articleName,
                damagedUnits = incident.damagedUnits.toLong(),
                missingUnits = incident.missingUnits.toLong(),
                replacementDop = incident.replacementDop.toLong(),
                notes = incident.notes,
                photoUrl = incident.photoUrl,
                resolutionNotes = incident.resolutionNotes,
                chargePaymentId = incident.chargePaymentId,
            )
        }
        queries.deleteAllNotifications()
        snapshot.notifications.forEach { notification ->
            queries.upsertNotification(
                id = notification.id,
                title = notification.title,
                body = notification.body,
                category = notification.category,
                createdAtLabel = notification.createdAtLabel,
                isRead = if (notification.isRead) 1L else 0L,
                recipientRole = notification.recipientRole,
                actionPath = notification.actionPath,
            )
        }
        if (snapshot.extrasJson.isNotBlank()) {
            queries.upsertSubmoduleSnapshot(
                snapshotKey = SUBMODULE_EXTRAS_SNAPSHOT_KEY,
                payloadJson = snapshot.extrasJson,
            )
        }
    }

    private fun seedInitialDataSynchronouslyIfEmpty() {
        if (!seedDemoData) {
            seedAdminAccountsOnlyIfEmpty()
            reloadFromSql()
            return
        }
        val existingArticles = queries.selectAllArticles().executeAsList()
        if (existingArticles.isEmpty()) {
            database.transaction {
                CanonicalSampleData.articles.forEach { art ->
                    upsertArticleRow(art)
                }
                CanonicalSampleData.bodaChecklist.forEach { step ->
                    checklistEventIdsByStepId[step.id] = step.eventId
                    queries.upsertChecklistStep(
                        id = step.id,
                        stageLabel = step.stageLabel,
                        title = step.title,
                        completed = if (step.completed) 1L else 0L,
                        completedBy = step.completedBy,
                        timeLabel = step.timeLabel,
                        eventId = step.eventId,
                    )
                }
                CanonicalSampleData.offlineQueue.forEach { item ->
                    upsertOfflineMutationRow(item)
                }
                CanonicalSampleData.events.forEachIndexed { index, evt ->
                    upsertEventRow(evt, index)
                }
                CanonicalSampleData.payments.forEachIndexed { index, pay ->
                    upsertPaymentRow(pay, index)
                }
                CanonicalSampleData.initialFavorites.forEach { favId ->
                    queries.insertFavorite(favId)
                }
                CanonicalSampleData.staffMembers.forEachIndexed { index, member ->
                    queries.upsertStaffPermission(
                        id = member.id,
                        name = member.name,
                        roleLabel = member.roleLabel,
                        role = member.role.name,
                        activeEventsCount = member.activeEventsCount.toLong(),
                        permissionsCsv = member.permissions.joinToString(",") { perm -> perm.name },
                        canViewPayments = if (member.canViewPayments) 1L else 0L,
                        sortOrder = index.toLong(),
                        email = member.email,
                        phone = member.phone,
                        active = if (member.active) 1L else 0L,
                    )
                }
                CanonicalSampleData.initialChatMessages.forEach { msg ->
                    queries.insertChatMessage(msg)
                }
                JwtAuthManager.defaultSeedAccounts.forEach { account ->
                    upsertUserAccountRow(account)
                }
            }
        }
        val existingUsers = queries.selectAllUserAccounts().executeAsList()
        if (existingUsers.isEmpty()) {
            database.transaction {
                JwtAuthManager.defaultSeedAccounts.forEach { account ->
                    upsertUserAccountRow(account)
                }
            }
        }
        reloadFromSql()
    }

    private fun seedAdminAccountsOnlyIfEmpty() {
        val existingStaff = queries.selectAllStaffPermissions().executeAsList()
        val existingUsers = queries.selectAllUserAccounts().executeAsList()
        val needsStaff = existingStaff.isEmpty()
        val needsUsers = existingUsers.isEmpty()
        val bothSeededAlready = if (!needsStaff) !needsUsers else false
        if (bothSeededAlready) {
            return
        }
        database.transaction {
            if (needsStaff) {
                CanonicalSampleData.emptyAdminStaffMembers.forEachIndexed { index, member ->
                    queries.upsertStaffPermission(
                        id = member.id,
                        name = member.name,
                        roleLabel = member.roleLabel,
                        role = member.role.name,
                        activeEventsCount = 0L,
                        permissionsCsv = member.permissions.joinToString(",") { perm -> perm.name },
                        canViewPayments = if (member.canViewPayments) 1L else 0L,
                        sortOrder = index.toLong(),
                        email = member.email,
                        phone = member.phone,
                        active = if (member.active) 1L else 0L,
                    )
                }
            }
            if (needsUsers) {
                JwtAuthManager.defaultAdminSeedAccounts.forEach { account ->
                    upsertUserAccountRow(account)
                }
            }
        }
    }

    private fun reloadFromSql() {
        reloadArticlesFromSql()
        reloadChecklistFromSql()
        reloadOfflineQueueFromSql()
        reloadEventsFromSql()
        reloadPaymentsFromSql()
        reloadFavoritesFromSql()
        reloadStaffFromSql()
        reloadChatFromSql()
        reloadUserAccountsFromSql()
        reloadExtendedSubmodulesFromSql()
    }

    private fun reloadExtendedSubmodulesFromSql() {
        val loadedQuotes = queries.selectAllQuotes().executeAsList().map { row ->
            val dtoLines = runCatching {
                sqlJsonCodec.decodeFromString<List<QuoteLineDto>>(row.linesJson)
            }.getOrDefault(emptyList())
            val domainLines = dtoLines.map { line ->
                EventLineItem(
                    articleCode = line.articleCode,
                    name = line.name,
                    quantity = line.quantity.toInt(),
                    unitPriceDop = line.unitPriceRD.toInt(),
                    photo = RosaFiestaBackendMappers.selectPhotoForArticle("", line.name),
                )
            }
            QuoteRecord(
                id = row.id,
                code = row.code,
                eventId = row.eventId,
                eventTitle = row.eventTitle,
                clientName = row.clientName,
                clientEmail = row.clientEmail,
                venue = row.venue,
                dateLabel = row.dateLabel,
                guests = row.guests.toInt(),
                status = row.status,
                lines = domainLines,
                deliveryFeeDop = row.deliveryFeeDop.toInt(),
                discountDop = row.discountDop.toInt(),
                totalDop = row.totalDop.toInt(),
                signatureSvg = row.signatureSvg,
                depositPercent = row.depositPercent.toInt(),
                depositDop = row.depositDop.toInt(),
                signedByName = row.signedByName,
                rejectionReason = row.rejectionReason,
                eventDateIso = row.eventDateIso,
                packageId = row.packageId,
                contractPdfUrl = row.contractPdfUrl,
                signedAtIso = row.signedAtIso,
                contactPhone = row.contactPhone,
                signedByUserId = row.signedByUserId,
                contractHashSha256 = row.contractHashSha256,
            )
        }
        val loadedStops = queries.selectAllDeliveryStops().executeAsList().map { row ->
            DeliveryStop(
                order = row.stopOrder.toInt(),
                eventTitle = row.eventTitle,
                venue = row.venue,
                windowLabel = row.windowLabel,
                vehicleAndCrew = row.vehicleAndCrew,
                statusLabel = row.statusLabel,
                eventId = row.eventId,
            )
        }
        val loadedDamages = queries.selectAllDamageIncidents().executeAsList().map { row ->
            DamageIncident(
                id = row.id,
                eventTitle = row.eventTitle,
                articleName = row.articleName,
                damagedUnits = row.damagedUnits.toInt(),
                missingUnits = row.missingUnits.toInt(),
                replacementDop = row.replacementDop.toInt(),
                notes = row.notes,
                eventId = row.eventId,
                articleCode = row.articleCode,
                photoUrl = row.photoUrl,
                resolutionNotes = row.resolutionNotes,
                chargePaymentId = row.chargePaymentId,
            )
        }
        val loadedNotifications = queries.selectAllNotifications().executeAsList().map { row ->
            NotificationRecord(
                id = row.id,
                title = row.title,
                body = row.body,
                category = row.category,
                createdAtLabel = row.createdAtLabel,
                isRead = row.isRead != 0L,
                recipientRole = row.recipientRole,
                actionPath = row.actionPath,
            )
        }
        val extrasJson = queries.selectAllSubmoduleSnapshots().executeAsList()
            .firstOrNull { row -> row.snapshotKey == SUBMODULE_EXTRAS_SNAPSHOT_KEY }
            ?.payloadJson
            .orEmpty()
        submodules.restoreFromPersistence(
            PersistedSubmoduleSnapshot(
                quotes = loadedQuotes,
                deliveryStops = loadedStops,
                damageIncidents = loadedDamages,
                notifications = loadedNotifications,
                extrasJson = extrasJson,
            ),
        )
    }

    private fun reloadUserAccountsFromSql() {
        val loadedUsers = queries.selectAllUserAccounts().executeAsList().map { row ->
            RegisteredUserAccount(
                id = row.id,
                username = row.username,
                email = row.email,
                fullName = row.fullName,
                passwordHash = row.passwordHash,
                salt = row.salt,
                birthDate = row.birthDate,
                phone = row.phone,
                role = row.role,
                avatarKey = row.avatarKey,
                preferences = UserPreferences(
                    pushNotificationsEnabled = row.pushNotificationsEnabled != 0L,
                    whatsappAlertsEnabled = row.whatsappAlertsEnabled != 0L,
                    emailReceiptsEnabled = row.emailReceiptsEnabled != 0L,
                    biometricLockEnabled = row.biometricLockEnabled != 0L,
                    autoSyncWifiOnly = row.autoSyncWifiOnly != 0L,
                    eventRemindersEnabled = row.eventRemindersEnabled != 0L,
                    preferredLanguage = row.preferredLanguage.ifBlank { "es" },
                    darkThemeEnabled = row.darkThemeEnabled != 0L,
                    savedPaymentMethod = row.savedPaymentMethod.ifBlank { "Transferencia" },
                    profileZone = row.profileZone.ifBlank { "Distrito Nacional" },
                ),
            )
        }
        if (loadedUsers.isNotEmpty()) {
            userAccountsState.value = loadedUsers
        }
    }

    private fun restorePersistedSessionFromSql() {
        val activeSession = runCatching {
            queries.selectActiveSession().executeAsOneOrNull()
        }.getOrNull() ?: return
        val restored = restoreSessionFromJwt(activeSession.accessToken)
        if (restored != null) {
            return
        }
        runCatching {
            queries.clearActiveSession()
        }
        _currentAuthenticatedUser.value = null
        _currentSessionJwt.value = null
        _currentUserEmail.value = ""
        _activeRole.value = "guest"
    }

    private suspend fun syncUsersFromBackendSuspend() {
        val client = apiClient ?: return
        runCatching {
            val response = client.listRegisteredUsers()
            val remoteUsers = response.users.orEmpty()
            if (remoteUsers.isEmpty()) return@runCatching
            sqlMutex.withLock {
                val existingRows = queries.selectAllUserAccounts().executeAsList()
                database.transaction {
                    remoteUsers.forEach { remote ->
                        val existing = existingRows.firstOrNull { row ->
                            row.email.equals(remote.email, ignoreCase = true)
                        }
                        val resolvedUsername = remote.username.trim().ifEmpty {
                            remote.email.substringBefore("@")
                        }
                        val remoteSalt = JwtAuthManager.generateDeterministicSalt("${remote.email}:$resolvedUsername")
                        val existingRole = existing?.role.orEmpty()
                        val normalizedEmail = remote.email.trim().lowercase()
                        val resolvedRole = when {
                            existingRole.isNotEmpty() -> existingRole
                            normalizedEmail == "yuli@rosafiesta.do" -> "staff_yuli"
                            remote.role.trim().isNotEmpty() -> remote.role.trim()
                            else -> "client"
                        }
                        val resolvedPrefs = if (remote.preferences != null) {
                            RosaFiestaBackendMappers.toUserPreferences(remote.preferences)
                        } else if (existing != null) {
                            UserPreferences(
                                pushNotificationsEnabled = existing.pushNotificationsEnabled != 0L,
                                whatsappAlertsEnabled = existing.whatsappAlertsEnabled != 0L,
                                emailReceiptsEnabled = existing.emailReceiptsEnabled != 0L,
                                biometricLockEnabled = existing.biometricLockEnabled != 0L,
                                autoSyncWifiOnly = existing.autoSyncWifiOnly != 0L,
                                eventRemindersEnabled = existing.eventRemindersEnabled != 0L,
                                preferredLanguage = existing.preferredLanguage.ifBlank { "es" },
                                darkThemeEnabled = existing.darkThemeEnabled != 0L,
                                savedPaymentMethod = existing.savedPaymentMethod.ifBlank { "Transferencia" },
                                profileZone = existing.profileZone.ifBlank { "Distrito Nacional" },
                            )
                        } else {
                            UserPreferences()
                        }
                        upsertUserAccountRow(
                            RegisteredUserAccount(
                                id = existing?.id ?: remote.id,
                                username = existing?.username?.ifEmpty { resolvedUsername } ?: resolvedUsername,
                                email = normalizedEmail,
                                fullName = remote.fullName.trim().ifEmpty { resolvedUsername },
                                passwordHash = existing?.passwordHash.orEmpty(),
                                salt = existing?.salt?.ifEmpty { remoteSalt } ?: remoteSalt,
                                birthDate = remote.birthDate.trim().ifEmpty { existing?.birthDate.orEmpty() },
                                phone = remote.phone.trim().ifEmpty { existing?.phone.orEmpty() },
                                role = resolvedRole,
                                preferences = resolvedPrefs,
                            ),
                        )
                    }
                }
                reloadUserAccountsFromSql()
            }
        }
    }

    private fun reloadArticlesFromSql() {
        val canonicalById = CanonicalSampleData.articles.associateBy { article -> article.id }
        val inMemoryById = _articles.value.associateBy { article -> article.id }
        _articles.value = queries.selectAllArticles().executeAsList().map { row ->
            val currentInMemory = inMemoryById[row.id]
            val canonical = canonicalById[row.id]
            val photoAsset = PhotoAsset.entries.firstOrNull { candidate -> candidate.key == row.photoKey }
                ?: PhotoAsset.SILLA_TIFFANY
            val decodedVariants = if (row.variantsJson.isNotBlank()) {
                runCatching {
                    sqlJsonCodec.decodeFromString<List<String>>(row.variantsJson)
                }.getOrNull()
            } else {
                null
            }
            val resolvedVariants = decodedVariants ?: currentInMemory?.variants ?: canonical?.variants ?: emptyList()
            CatalogArticle(
                id = row.id,
                code = row.code,
                name = row.name,
                category = row.category,
                description = row.description,
                pricePerDayDop = row.pricePerDayDop.toInt(),
                totalStock = row.totalStock.toInt(),
                availableOct23 = row.availableOct23.toInt(),
                photo = photoAsset,
                variants = resolvedVariants,
                dimensions = row.dimensions.ifBlank { currentInMemory?.dimensions ?: canonical?.dimensions.orEmpty() },
                material = row.material.ifBlank { currentInMemory?.material ?: canonical?.material.orEmpty() },
                visibleInCatalog = row.visibleInCatalog != 0L,
                photoUrl = row.photoUrl.ifBlank { currentInMemory?.photoUrl.orEmpty() },
                damagedStock = row.damagedStock.toInt(),
            )
        }
    }

    private fun reloadChecklistFromSql() {
        val defaultEventId = _events.value.firstOrNull()?.id.orEmpty()
        _checklist.value = queries.selectAllChecklistSteps().executeAsList().map { row ->
            val linkedEventId = row.eventId.ifBlank {
                checklistEventIdsByStepId[row.id] ?: defaultEventId
            }
            if (linkedEventId.isNotEmpty()) {
                checklistEventIdsByStepId[row.id] = linkedEventId
            }
            ChecklistStep(
                id = row.id,
                stageLabel = row.stageLabel,
                title = row.title,
                completed = row.completed != 0L,
                completedBy = row.completedBy,
                timeLabel = row.timeLabel,
                eventId = linkedEventId,
            )
        }
    }

    private fun reloadOfflineQueueFromSql() {
        _offlineQueue.value = queries.selectAllOfflineMutations().executeAsList().map { row ->
            OfflineQueueItem(
                id = row.id,
                actionLabel = row.actionLabel,
                entityCode = row.entityCode,
                timeAgo = row.timeAgo,
                synced = row.synced != 0L,
                operationType = row.operationType,
                payloadJson = row.payloadJson,
                idempotencyKey = row.idempotencyKey,
            )
        }
    }

    private fun reloadEventsFromSql() {
        val itemsByEventId = CanonicalSampleData.events.associate { event -> event.id to event.items }
        val inMemoryById = _events.value.associateBy { event -> event.id }
        val loadedEvents = queries.selectAllEvents().executeAsList().map { row ->
            val photoAsset = PhotoAsset.entries.firstOrNull { candidate -> candidate.key == row.photoKey }
                ?: PhotoAsset.BODA
            val statusEnum = EventStatus.entries.firstOrNull { candidate -> candidate.name == row.status }
                ?: EventStatus.CONFIRMED
            val currentInMemory = inMemoryById[row.id]
            val decodedItems = if (row.itemsJson.isNotBlank()) {
                runCatching {
                    sqlJsonCodec.decodeFromString<List<EventItemDto>>(row.itemsJson)
                }.getOrNull()?.map { dto ->
                    EventLineItem(
                        articleCode = dto.articleCode,
                        name = dto.name,
                        quantity = dto.quantity.toInt(),
                        unitPriceDop = dto.unitPriceRD.toInt(),
                        photo = RosaFiestaBackendMappers.selectPhotoForArticle("", dto.name),
                    )
                }
            } else {
                null
            }
            val resolvedItems = decodedItems ?: currentInMemory?.items ?: itemsByEventId[row.id] ?: emptyList()
            ClientEvent(
                id = row.id,
                code = row.code,
                title = row.title,
                clientName = row.clientName,
                eventType = row.eventType,
                dateLabel = row.dateLabel,
                daysUntilEvent = row.daysUntilEvent.toInt(),
                venue = row.venue,
                zone = row.zone,
                guests = row.guests.toInt(),
                articleCount = row.articleCount.toInt(),
                totalDop = row.totalDop.toInt(),
                paidDepositDop = row.paidDepositDop.toInt(),
                depositMethodLabel = row.depositMethodLabel,
                depositDateLabel = row.depositDateLabel,
                balanceDueDateLabel = row.balanceDueDateLabel,
                status = statusEnum,
                leadStaffName = row.leadStaffName,
                photo = photoAsset,
                items = resolvedItems,
                priorChangesCount = row.priorChangesCount.toInt(),
                clientEmail = row.clientEmail.ifBlank { currentInMemory?.clientEmail.orEmpty() },
                cancellationReason = row.cancellationReason.ifBlank { currentInMemory?.cancellationReason.orEmpty() },
                sketchJson = row.sketchJson.ifBlank { currentInMemory?.sketchJson.orEmpty() },
                eventDateIso = row.eventDateIso.ifBlank { currentInMemory?.eventDateIso.orEmpty() },
                pickupDateIso = row.pickupDateIso.ifBlank { currentInMemory?.pickupDateIso.orEmpty() },
                returnDateIso = row.returnDateIso.ifBlank { currentInMemory?.returnDateIso.orEmpty() },
                quoteId = row.quoteId.ifBlank { currentInMemory?.quoteId.orEmpty() },
                closeoutCompleted = row.closeoutCompleted != 0L,
                closeoutNotes = row.closeoutNotes.ifBlank { currentInMemory?.closeoutNotes.orEmpty() },
                lastPaymentReminderAt = row.lastPaymentReminderAt.ifBlank { currentInMemory?.lastPaymentReminderAt.orEmpty() },
                lastPaymentReminderChannel = row.lastPaymentReminderChannel.ifBlank { currentInMemory?.lastPaymentReminderChannel.orEmpty() },
                setupPhotoUrls = runCatching {
                    sqlJsonCodec.decodeFromString<List<String>>(row.setupPhotoUrlsJson)
                }.getOrDefault(emptyList()).ifEmpty { currentInMemory?.setupPhotoUrls.orEmpty() },
                contactPhone = row.contactPhone.ifBlank { currentInMemory?.contactPhone.orEmpty() },
            )
        }
        val shouldApplyEvents = if (loadedEvents.isNotEmpty()) true else !seedDemoData
        if (shouldApplyEvents) {
            _events.value = loadedEvents
            recalculateMetricsFromState()
        }
    }

    private fun reloadPaymentsFromSql() {
        val loadedPayments = queries.selectAllPayments().executeAsList().map { row ->
            PaymentRecord(
                id = row.id,
                receiptNumber = row.receiptNumber,
                eventId = row.eventId,
                eventTitle = row.eventTitle,
                clientName = row.clientName,
                amountDop = row.amountDop.toInt(),
                methodLabel = row.methodLabel,
                kindLabel = row.kindLabel,
                dateLabel = row.dateLabel,
                referenceCode = row.referenceCode,
                verified = row.verified != 0L,
                voucherUrl = row.voucherUrl,
                ncfType = row.ncfType,
                ncfNumber = row.ncfNumber,
                clientRnc = row.clientRnc,
                subtotalDop = row.subtotalDop.toInt(),
                itbisDop = row.itbisDop.toInt(),
                securityDepositDop = row.securityDepositDop.toInt(),
                rejectionReason = row.rejectionReason,
            )
        }
        val shouldApplyPayments = if (loadedPayments.isNotEmpty()) true else !seedDemoData
        if (shouldApplyPayments) {
            _payments.value = loadedPayments
            recalculateMetricsFromState()
        }
    }

    private fun recalculateMetricsFromState() {
        val metricsBaseline = if (seedDemoData) {
            CanonicalSampleData.businessMetrics
        } else {
            CanonicalSampleData.zeroBusinessMetrics
        }
        _metrics.value = RosaFiestaBackendMappers.computeHonestMetrics(
            events = _events.value,
            payments = _payments.value,
            quotes = submodules.quotes.value,
            baseMetrics = metricsBaseline,
        )
    }

    private fun reloadFavoritesFromSql() {
        _favorites.value = queries.selectAllFavorites().executeAsList().toSet()
    }

    private fun reloadStaffFromSql() {
        val canonicalById = CanonicalSampleData.staffMembers.associateBy { member -> member.id }
        val inMemoryById = _staffMembers.value.associateBy { member -> member.id }
        val loadedStaff = queries.selectAllStaffPermissions().executeAsList().map { row ->
            val roleEnum = UserRole.entries.firstOrNull { candidate -> candidate.name == row.role }
                ?: UserRole.STAFF_DECORATION
            val perms = row.permissionsCsv
                .split(",")
                .mapNotNull { token ->
                    StaffPermission.entries.firstOrNull { candidate -> candidate.name == token.trim() }
                }
                .toSet()
            val prev = inMemoryById[row.id] ?: canonicalById[row.id]
            StaffMember(
                id = row.id,
                name = row.name,
                roleLabel = row.roleLabel,
                role = roleEnum,
                activeEventsCount = row.activeEventsCount.toInt(),
                permissions = perms,
                canViewPayments = row.canViewPayments != 0L,
                email = row.email.ifBlank { prev?.email.orEmpty() },
                phone = row.phone.ifBlank { prev?.phone.orEmpty() },
                active = row.active != 0L,
            )
        }
        val shouldApplyStaff = if (loadedStaff.isNotEmpty()) true else !seedDemoData
        if (shouldApplyStaff) {
            _staffMembers.value = loadedStaff
        }
    }

    private fun reloadChatFromSql() {
        val loadedChat = queries.selectAllChatMessages().executeAsList()
        val shouldApplyChat = if (loadedChat.isNotEmpty()) true else !seedDemoData
        if (shouldApplyChat) {
            _chatMessages.value = loadedChat
        }
    }

    private fun buildFallbackItems(title: String, guests: Int): List<EventLineItem> {
        val proposal = RosaFiestaEngine.buildRosaProposal(title, guests.coerceAtLeast(10))
        return proposal.lines.map { line ->
            EventLineItem(
                articleCode = line.code,
                name = line.name,
                quantity = line.quantity,
                unitPriceDop = line.unitPriceDop,
                photo = RosaFiestaBackendMappers.selectPhotoForArticle("", line.name),
            )
        }
    }

    /**
     * Toggles a field checklist step in SQLite off the main thread and synchronizes with the backend.
     */
    public suspend fun toggleChecklistStepAsync(
        stepId: String,
        onError: (String) -> Unit = {},
    ): Unit = withContext(ioDispatcher) {
        val cleanStepId = stepId.trim()
        val nowHm = DomainClockFormatter.currentTimeHmLabel()
        var staffName = ""
        var nextCompleted = false
        var previousCompleted = 0L
        var previousTimeLabel = "Pendiente"
        var resolvedEventId = ""
        sqlMutex.withLock {
            val current = queries.selectAllChecklistSteps().executeAsList()
                .firstOrNull { row -> row.id == cleanStepId } ?: return@withContext
            previousCompleted = current.completed
            previousTimeLabel = current.timeLabel
            nextCompleted = current.completed == 0L
            staffName = current.completedBy
            resolvedEventId = current.eventId.ifBlank {
                checklistEventIdsByStepId[cleanStepId]
                    ?: _checklist.value.firstOrNull { item -> item.id == cleanStepId }?.eventId
                    ?: _events.value.firstOrNull()?.id.orEmpty()
            }
            val nextTimeLabel = if (nextCompleted) nowHm else "Pendiente"
            queries.updateChecklistStepStatus(
                completed = if (nextCompleted) 1L else 0L,
                timeLabel = nextTimeLabel,
                id = cleanStepId,
            )
            reloadChecklistFromSql()
        }
        val toggleInput = ToggleChecklistInputDto(
            completed = nextCompleted,
            staffName = staffName,
        )
        val client = apiClient
        val canSend = if (client != null) resolvedEventId.isNotEmpty() else false
        var rejectedByServer: RosaFiestaApiException? = null
        var failedTransport = false
        if (canSend) {
            try {
                client!!.toggleChecklistItem(
                    eventId = resolvedEventId,
                    itemId = cleanStepId,
                    input = toggleInput,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (apiError: RosaFiestaApiException) {
                rejectedByServer = apiError
            } catch (_: Throwable) {
                failedTransport = true
            }
        }
        if (rejectedByServer != null) {
            sqlMutex.withLock {
                queries.updateChecklistStepStatus(
                    completed = previousCompleted,
                    timeLabel = previousTimeLabel,
                    id = cleanStepId,
                )
                reloadChecklistFromSql()
            }
            onError(SensitiveDataRedactor.translateBusinessError(rejectedByServer.errorCode, rejectedByServer.message))
            return@withContext
        }
        val needsEnqueue = if (apiClient == null) true else failedTransport
        val hasTargetEvent = resolvedEventId.isNotEmpty()
        val shouldQueueToggle = if (needsEnqueue) hasTargetEvent else false
        if (shouldQueueToggle) {
            enqueueOutboxMutation(
                actionLabel = "Checklist $cleanStepId",
                entityCode = resolvedEventId,
                operationType = "toggle_checklist",
                payloadJson = sqlJsonCodec.encodeToString(toggleInput),
                idempotencyKey = "idem-chk-$cleanStepId-$nextCompleted",
            )
        }
    }

    /**
     * Marks offline mutations as synchronized in SQLite only when the backend replay succeeds.
     */
    public suspend fun syncOfflineQueueAsync(): Unit = withContext(ioDispatcher) {
        val pendingRows = sqlMutex.withLock {
            queries.selectPendingOfflineMutations().executeAsList()
        }
        val structuredPending = pendingRows.filter { row -> row.operationType.isNotBlank() }
        val client = apiClient
        var remoteSucceeded = false
        if (client != null) {
            val replayResult = runCatching {
                if (structuredPending.isNotEmpty()) {
                    val items = structuredPending.map { row ->
                        OfflineMutationSyncItemDto(
                            actionLabel = row.actionLabel,
                            entityCode = row.entityCode,
                            operationType = row.operationType,
                            payloadJson = row.payloadJson,
                            idempotencyKey = row.idempotencyKey.ifBlank { "idem-${row.id}" },
                        )
                    }
                    client.syncOfflineQueue(items)
                } else {
                    client.syncOfflineQueue()
                }
            }
            remoteSucceeded = replayResult.isSuccess
        }
        if (remoteSucceeded) {
            sqlMutex.withLock {
                pendingRows.forEach { row ->
                    queries.markOfflineMutationSyncedById(row.id)
                }
                reloadOfflineQueueFromSql()
            }
        }
    }

    /**
     * Adjusts warehouse stock for [articleId] in SQLite off the main thread.
     */
    public suspend fun adjustArticleStockAsync(articleId: String, delta: Int): Unit = withContext(ioDispatcher) {
        var newStock = 0
        var newAvail = 0
        sqlMutex.withLock {
            val current = queries.selectAllArticles().executeAsList()
                .firstOrNull { row -> row.id == articleId } ?: return@withContext
            newStock = (current.totalStock.toInt() + delta).coerceAtLeast(0)
            newAvail = (current.availableOct23.toInt() + delta).coerceIn(0, newStock)
            queries.updateArticleStock(
                totalStock = newStock.toLong(),
                availableOct23 = newAvail.toLong(),
                id = articleId,
            )
            reloadArticlesFromSql()
        }
        val updateInput = UpdateArticleInputDto(
            totalStock = newStock.toLong(),
            availableStock = newAvail.toLong(),
        )
        val remoteAttempt = runCatching {
            apiClient?.updateCatalogArticle(
                articleId = articleId,
                input = updateInput,
            )
        }
        val shouldQueueStock = if (apiClient == null) true else remoteAttempt.isFailure
        if (shouldQueueStock) {
            enqueueOutboxMutation(
                actionLabel = "Ajustar stock $articleId",
                entityCode = articleId,
                operationType = "adjust_stock",
                payloadJson = sqlJsonCodec.encodeToString(updateInput),
                idempotencyKey = "idem-stock-$articleId-$newStock-$newAvail",
            )
        }
    }

    /**
     * Toggles [articleId] in the persisted favorites list off the main thread.
     */
    public suspend fun toggleFavoriteAsync(articleId: String): Unit = withContext(ioDispatcher) {
        val cleanId = articleId.trim()
        if (cleanId.isEmpty()) return@withContext
        var willAdd = true
        sqlMutex.withLock {
            val currentFavs = queries.selectAllFavorites().executeAsList()
            if (cleanId in currentFavs) {
                willAdd = false
                queries.deleteFavorite(cleanId)
            } else {
                queries.insertFavorite(cleanId)
            }
            reloadFavoritesFromSql()
        }
        val client = apiClient
        if (client != null) {
            if (submodules.hasRealServerToken()) {
                runCatching {
                    val response = if (willAdd) client.addFavorite(cleanId) else client.removeFavorite(cleanId)
                    val remoteFavs = response.favorites
                    if (remoteFavs != null) {
                        sqlMutex.withLock {
                            queries.selectAllFavorites().executeAsList().forEach { existingId ->
                                if (existingId !in remoteFavs) queries.deleteFavorite(existingId)
                            }
                            remoteFavs.forEach { favId ->
                                queries.insertFavorite(favId)
                            }
                            reloadFavoritesFromSql()
                        }
                    }
                }
            }
        }
    }

    /**
     * Toggles payment visibility permission for [staffName] in SQLite off the main thread.
     */
    public suspend fun toggleStaffPaymentAccessAsync(staffName: String): Unit = withContext(ioDispatcher) {
        val target = staffName.trim()
        if (target.isEmpty()) return@withContext
        var staffId = ""
        var updatedPerms: List<String> = emptyList()
        var updatedLabel = ""
        sqlMutex.withLock {
            val current = queries.selectAllStaffPermissions().executeAsList()
                .firstOrNull { row -> row.name.equals(target, ignoreCase = true) } ?: return@withContext
            staffId = current.id
            val nextCanView = current.canViewPayments == 0L
            val existingPerms = current.permissionsCsv
                .split(",")
                .mapNotNull { token ->
                    StaffPermission.entries.firstOrNull { candidate -> candidate.name == token.trim() }
                }
                .toSet()
            val nextPerms = if (nextCanView) {
                existingPerms + StaffPermission.PAYMENTS
            } else {
                existingPerms - StaffPermission.PAYMENTS
            }
            updatedPerms = nextPerms.map { perm -> perm.name }
            updatedLabel = if (current.name.equals("Yuli", ignoreCase = true)) {
                if (nextCanView) {
                    "Decoración, tareas y consulta de pagos"
                } else {
                    "Decoración y tareas (sin acceso a pagos)"
                }
            } else {
                current.roleLabel
            }
            queries.updateStaffPaymentAccess(
                canViewPayments = if (nextCanView) 1L else 0L,
                permissionsCsv = updatedPerms.joinToString(","),
                roleLabel = updatedLabel,
                name = current.name,
            )
            reloadStaffFromSql()
        }
        if (staffId.isNotEmpty()) {
            runCatching {
                apiClient?.updateStaffMember(
                    staffId = staffId,
                    input = UpdateStaffInputDto(
                        roleLabel = updatedLabel,
                        permissions = updatedPerms,
                    ),
                )
            }
        }
    }

    /**
     * Inserts a formatted chat message into SQLite off the main thread.
     */
    public suspend fun sendChatMessageAsync(author: String, message: String): Unit = withContext(ioDispatcher) {
        val cleanMsg = message.trim()
        if (cleanMsg.isEmpty()) return@withContext
        val cleanAuthor = author.trim().ifEmpty { "Camila" }
        sqlMutex.withLock {
            queries.insertChatMessage("$cleanAuthor: $cleanMsg")
            reloadChatFromSql()
        }
    }

    /**
     * Creates or updates a rental catalog article in SQLite and synchronizes with the backend.
     */
    public suspend fun createArticleAsync(
        code: String,
        name: String,
        category: String,
        pricePerDayDop: Int,
        totalStock: Int,
        photoUrl: String = "",
        description: String = "",
        dimensions: String = "",
        material: String = "",
        visibleInCatalog: Boolean = true,
    ): Unit = withContext(ioDispatcher) {
        val cleanCode = code.trim()
        val cleanName = name.trim()
        val cleanCategory = category.trim()
        val resolvedCategoryId = submodules.resolveCategoryWireId(cleanCategory)
        val cleanPhotoUrl = photoUrl.trim()
        val cleanDimensions = dimensions.trim()
        val cleanMaterial = material.trim()
        if (cleanCode.isEmpty()) return@withContext
        if (cleanName.isEmpty()) return@withContext
        if (pricePerDayDop <= 0) return@withContext
        if (totalStock < 0) return@withContext

        val articleId = "art-${cleanCode.lowercase()}"
        val resolvedDescription = description.trim().ifEmpty {
            "$cleanName para eventos y celebraciones en Santo Domingo."
        }
        val photo = RosaFiestaBackendMappers.selectPhotoForArticle(cleanCategory, cleanName)
        val created = CatalogArticle(
            id = articleId,
            code = cleanCode,
            name = cleanName,
            category = cleanCategory,
            description = resolvedDescription,
            pricePerDayDop = pricePerDayDop,
            totalStock = totalStock,
            availableOct23 = totalStock,
            photo = photo,
            variants = emptyList(),
            dimensions = cleanDimensions,
            material = cleanMaterial,
            visibleInCatalog = visibleInCatalog,
            photoUrl = cleanPhotoUrl,
        )
        sqlMutex.withLock {
            upsertArticleRow(created)
            reloadArticlesFromSql()
        }
        val createInput = CreateArticleInputDto(
            id = articleId,
            name = cleanName,
            categoryId = resolvedCategoryId,
            photoKey = photo.key,
            dailyPriceDop = pricePerDayDop.toLong(),
            totalStock = totalStock.toLong(),
            availableStock = totalStock.toLong(),
            description = resolvedDescription,
            dimensions = cleanDimensions.ifBlank { null },
            material = cleanMaterial.ifBlank { null },
            visible = visibleInCatalog,
            photoUrl = cleanPhotoUrl.ifBlank { null },
        )
        val remoteAttempt = runCatching {
            apiClient?.createCatalogArticle(createInput)
        }
        val shouldQueueArticle = if (apiClient == null) true else remoteAttempt.isFailure
        if (shouldQueueArticle) {
            enqueueOutboxMutation(
                actionLabel = "Crear artículo $cleanCode",
                entityCode = articleId,
                operationType = "create_article",
                payloadJson = sqlJsonCodec.encodeToString(createInput),
                idempotencyKey = "idem-art-$articleId",
            )
        }
    }

    /**
     * Creates a new event in SQLite with real computed days-until-event, consistent delivery fee, and dynamic staff assignment.
     */
    public suspend fun createEventAsync(
        title: String,
        clientName: String,
        dateLabel: String,
        venue: String,
        guests: Int,
        preassignedEventId: String? = null,
        contactPhone: String = "",
    ): Unit = withContext(ioDispatcher) {
        val cleanTitle = title.trim()
        val cleanClient = clientName.trim()
        val cleanDate = dateLabel.trim()
        val cleanVenue = venue.trim()
        val cleanContactPhone = contactPhone.trim()
        if (cleanTitle.isEmpty()) return@withContext
        if (cleanClient.isEmpty()) return@withContext
        if (cleanDate.isEmpty()) return@withContext
        if (cleanVenue.isEmpty()) return@withContext
        if (guests < 0) return@withContext

        val safeProposalGuests = guests.coerceAtLeast(10)
        val proposal = RosaFiestaEngine.buildRosaProposal(cleanTitle, safeProposalGuests)
        val deliveryFeeDop = RosaFiestaBackendMappers.resolveDeliveryFeeDop(cleanVenue, businessSettings.value)
        val linesSubtotal = proposal.lines.sumOf { line -> line.subtotalDop }
        val consistentTotalDop = linesSubtotal + deliveryFeeDop
        val computedDaysUntil = DomainClockFormatter.computeDaysUntilEvent(cleanDate)
        val domainLines = proposal.lines.map { line ->
            EventLineItem(
                articleCode = line.code,
                name = line.name,
                quantity = line.quantity,
                unitPriceDop = line.unitPriceDop,
                photo = RosaFiestaBackendMappers.selectPhotoForArticle("", line.name),
            )
        }
        var assignedLead = ""
        var resolvedEventId = ""

        sqlMutex.withLock {
            val existingRows = queries.selectAllEvents().executeAsList()
            val existingByPreassigned = preassignedEventId?.let { targetId ->
                existingRows.indexOfFirst { row -> row.id == targetId }.takeIf { idx -> idx >= 0 }
            }
            val sortIndex = existingByPreassigned ?: existingRows.size
            val seq = 1120 + sortIndex
            resolvedEventId = preassignedEventId ?: "evt-$seq"
            val code = "RF-2026-$seq"
            assignedLead = RosaFiestaBackendMappers.selectLeastLoadedStaffLead(_staffMembers.value, _events.value)
            val turnaround = RosaFiestaEngine.computeTurnaroundWindow(cleanDate)
            val createdEvt = ClientEvent(
                id = resolvedEventId,
                code = code,
                title = cleanTitle,
                clientName = cleanClient,
                eventType = "Evento",
                dateLabel = cleanDate,
                daysUntilEvent = computedDaysUntil,
                venue = cleanVenue,
                zone = "Santo Domingo DN",
                guests = guests,
                articleCount = proposal.lines.sumOf { line -> line.quantity },
                totalDop = consistentTotalDop,
                paidDepositDop = 0,
                depositMethodLabel = "Pendiente de anticipo",
                depositDateLabel = "—",
                balanceDueDateLabel = cleanDate,
                status = EventStatus.PENDING_QUOTE,
                leadStaffName = assignedLead,
                photo = PhotoAsset.BODA,
                items = domainLines,
                priorChangesCount = 0,
                eventDateIso = turnaround?.eventDateIso.orEmpty(),
                pickupDateIso = turnaround?.pickupDateIso.orEmpty(),
                returnDateIso = turnaround?.returnDateIso.orEmpty(),
                contactPhone = cleanContactPhone,
            )
            upsertEventRow(createdEvt, sortIndex)
            reloadEventsFromSql()
        }

        val eventItems = proposal.lines.map { line ->
            EventItemDto(
                articleCode = line.code,
                name = line.name,
                quantity = line.quantity.toLong(),
                unitPriceRD = line.unitPriceDop.toLong(),
            )
        }
        val createEventInput = CreateEventInputDto(
            title = cleanTitle,
            clientName = cleanClient,
            dateLabel = cleanDate,
            daysUntilEvent = computedDaysUntil.toLong(),
            venue = cleanVenue,
            zone = "Santo Domingo DN",
            guests = guests.toLong(),
            assignedLead = assignedLead,
            totalRD = consistentTotalDop.toLong(),
            items = eventItems,
            contactPhone = cleanContactPhone.ifBlank { null },
        )
        val remoteEventAttempt = runCatching {
            apiClient?.createRemoteEvent(createEventInput)
        }
        val shouldQueueEvent = if (apiClient == null) true else remoteEventAttempt.isFailure
        if (shouldQueueEvent) {
            enqueueOutboxMutation(
                actionLabel = "Crear evento $cleanTitle",
                entityCode = resolvedEventId,
                operationType = "create_event",
                payloadJson = sqlJsonCodec.encodeToString(createEventInput),
                idempotencyKey = "idem-evt-$resolvedEventId",
            )
        }
        val remoteEventResult = remoteEventAttempt.getOrNull()
        val serverEventDto = remoteEventResult?.event
        val serverEventId = serverEventDto?.id?.trim().orEmpty()
        val needsIdReconcile = serverEventId.isNotEmpty()
        if (needsIdReconcile) {
            if (serverEventId != resolvedEventId) {
                sqlMutex.withLock {
                    val oldRows = queries.selectAllEvents().executeAsList()
                    val oldIdx = oldRows.indexOfFirst { row -> row.id == resolvedEventId }
                    if (oldIdx >= 0) {
                        val prevEvent = _events.value.firstOrNull { evt -> evt.id == resolvedEventId }
                        val existingMap = if (prevEvent != null) mapOf(serverEventId to prevEvent) else emptyMap()
                        val mapped = RosaFiestaBackendMappers.toClientEvent(
                            dto = serverEventDto!!,
                            fallbackById = existingMap,
                        )
                        queries.deleteEventById(resolvedEventId)
                        upsertEventRow(mapped, oldIdx)
                        reloadEventsFromSql()
                    }
                }
                resolvedEventId = serverEventId
            }
        }
        runCatching {
            val dtoLines = proposal.lines.map { line ->
                QuoteLineDto(
                    articleCode = line.code,
                    name = line.name,
                    quantity = line.quantity.toLong(),
                    unitPriceRD = line.unitPriceDop.toLong(),
                )
            }
            apiClient?.createQuote(
                CreateQuoteInputDto(
                    eventTitle = cleanTitle,
                    clientName = cleanClient,
                    eventDateLabel = cleanDate,
                    venue = cleanVenue,
                    guests = guests.toLong(),
                    lines = dtoLines,
                    deliveryFeeRD = deliveryFeeDop.toLong(),
                    discountRD = 0L,
                    eventId = resolvedEventId,
                ),
            )
        }
    }

    /**
     * Evaluates and persists an event date change in SQLite off the main thread.
     */
    public suspend fun changeEventDateAsync(eventId: String, newDateLabel: String): DateChangeResult = withContext(ioDispatcher) {
        val cleanDate = newDateLabel.trim()
        val evalResult = sqlMutex.withLock {
            val rows = queries.selectAllEvents().executeAsList()
            val target = rows.firstOrNull { row -> row.id == eventId.trim() }
                ?: return@withLock RosaFiestaEngine.evaluateDateChange(0, 2)
            val eval = RosaFiestaEngine.evaluateDateChange(
                daysUntilEvent = target.daysUntilEvent.toInt(),
                priorChangesCount = target.priorChangesCount.toInt(),
            )
            val canPersistDate = if (eval.allowed) cleanDate.isNotEmpty() else false
            if (canPersistDate) {
                queries.updateEventDateAndTotal(
                    dateLabel = cleanDate,
                    totalDop = target.totalDop + eval.feeDop,
                    priorChangesCount = target.priorChangesCount + 1L,
                    id = target.id,
                )
                reloadEventsFromSql()
            }
            eval
        }
        val canSyncDateChange = if (evalResult.allowed) cleanDate.isNotEmpty() else false
        if (canSyncDateChange) {
            val computedDays = DomainClockFormatter.computeDaysUntilEvent(cleanDate).toLong()
            val dateInput = DateChangeInputDto(
                newDateLabel = cleanDate,
                newDaysUntilEvent = computedDays,
            )
            val remoteAttempt = runCatching {
                apiClient?.requestDateChange(
                    id = eventId,
                    input = dateInput,
                )
            }
            val shouldQueueDate = if (apiClient == null) true else remoteAttempt.isFailure
            if (shouldQueueDate) {
                enqueueOutboxMutation(
                    actionLabel = "Cambio de fecha ${eventId.trim()}",
                    entityCode = eventId.trim(),
                    operationType = "date_change",
                    payloadJson = sqlJsonCodec.encodeToString(dateInput),
                    idempotencyKey = "idem-date-${eventId.trim()}-$cleanDate",
                )
            }
        }
        evalResult
    }

    /**
     * Cancels an event in SQLite off the main thread and returns its refund breakdown.
     */
    public suspend fun cancelEventAsync(eventId: String, reason: String = ""): CancellationRefund = withContext(ioDispatcher) {
        val cleanReason = reason.trim()
        var foundEvent = false
        val refundResult = sqlMutex.withLock {
            val rows = queries.selectAllEvents().executeAsList()
            val target = rows.firstOrNull { row -> row.id == eventId.trim() }
                ?: return@withLock RosaFiestaEngine.calculateCancellationRefund(0, 0)
            foundEvent = true
            val refund = RosaFiestaEngine.calculateCancellationRefund(
                paidDepositDop = target.paidDepositDop.toInt(),
                daysUntilEvent = target.daysUntilEvent.toInt(),
            )
            queries.updateEventStatus(
                status = EventStatus.CANCELLED.name,
                cancellationReason = cleanReason,
                id = target.id,
            )
            reloadEventsFromSql()
            refund
        }
        if (foundEvent) {
            val remoteAttempt = runCatching {
                apiClient?.cancelEvent(eventId, cleanReason)
            }
            val shouldQueueCancel = if (apiClient == null) true else remoteAttempt.isFailure
            if (shouldQueueCancel) {
                enqueueOutboxMutation(
                    actionLabel = "Cancelar evento ${eventId.trim()}",
                    entityCode = eventId.trim(),
                    operationType = "cancel_event",
                    payloadJson = sqlJsonCodec.encodeToString(CancelEventInputDto(reason = cleanReason)),
                    idempotencyKey = "idem-cancel-${eventId.trim()}",
                )
            }
        }
        refundResult
    }

    /**
     * Registers a payment in SQLite off the main thread using current system date labels and normalized API method names.
     */
    public suspend fun registerPaymentAsync(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
        voucherUrl: String = "",
        ncfType: String = "B02",
        clientRnc: String = "",
        bankName: String = "",
        kindLabel: String = "",
        contactPhone: String = "",
    ): PaymentRecord? = withContext(ioDispatcher) {
        val cleanMethod = methodLabel.trim()
        val cleanRef = referenceCode.trim().ifEmpty { "REF-2026" }
        val cleanVoucher = voucherUrl.trim()
        val cleanNcfType = ncfType.trim().ifEmpty { "B02" }.uppercase()
        val cleanRnc = clientRnc.trim()
        val cleanContactPhone = contactPhone.trim()
        if (amountDop <= 0) return@withContext null
        if (cleanMethod.isEmpty()) return@withContext null

        val shortDateLabel = DomainClockFormatter.currentShortDateLabel()
        val fullDateLabel = DomainClockFormatter.currentFullDateLabel()
        val normalizedPayment = RosaFiestaBackendMappers.normalizePaymentMethod(cleanMethod)
        val resolvedBank = bankName.trim().ifEmpty { normalizedPayment.bankName.orEmpty() }
        val isTransferMethod = normalizedPayment.canonicalMethod.equals("TRANSFER", ignoreCase = true)
        val requiresVerification = if (isTransferMethod) true else cleanVoucher.isNotEmpty()
        val resolvedVerified = !requiresVerification
        val fiscal = RosaFiestaEngine.computeFiscalBreakdown(
            totalDop = amountDop,
            ncfType = cleanNcfType,
            rawTaxId = cleanRnc,
        )
        val subtotalDop = fiscal.subtotalDop
        val itbisDop = fiscal.itbisDop

        val createdRecord = sqlMutex.withLock {
            val eventRows = queries.selectAllEvents().executeAsList()
            val target = eventRows.firstOrNull { row -> row.id == eventId.trim() } ?: return@withContext null
            val existingPayments = queries.selectAllPayments().executeAsList()
            val alreadyPersisted = existingPayments.firstOrNull { row ->
                val sameEvent = row.eventId == target.id
                val sameRef = row.referenceCode == cleanRef
                val sameAmount = row.amountDop == amountDop.toLong()
                val matchesRefAndAmount = if (sameRef) sameAmount else false
                if (sameEvent) matchesRefAndAmount else false
            }
            if (alreadyPersisted != null) {
                return@withLock PaymentRecord(
                    id = alreadyPersisted.id,
                    receiptNumber = alreadyPersisted.receiptNumber,
                    eventId = alreadyPersisted.eventId,
                    eventTitle = alreadyPersisted.eventTitle,
                    clientName = alreadyPersisted.clientName,
                    amountDop = alreadyPersisted.amountDop.toInt(),
                    methodLabel = alreadyPersisted.methodLabel,
                    kindLabel = alreadyPersisted.kindLabel,
                    dateLabel = alreadyPersisted.dateLabel,
                    referenceCode = alreadyPersisted.referenceCode,
                    verified = alreadyPersisted.verified != 0L,
                    voucherUrl = alreadyPersisted.voucherUrl,
                    ncfType = alreadyPersisted.ncfType,
                    ncfNumber = alreadyPersisted.ncfNumber,
                    clientRnc = alreadyPersisted.clientRnc,
                    subtotalDop = alreadyPersisted.subtotalDop.toInt(),
                    itbisDop = alreadyPersisted.itbisDop.toInt(),
                    securityDepositDop = alreadyPersisted.securityDepositDop.toInt(),
                    rejectionReason = alreadyPersisted.rejectionReason,
                )
            }
            val nextNumber = 185 + existingPayments.size
            val receipt = "REC-2026-${nextNumber.toString().padStart(4, '0')}"
            val balanceDop = (target.totalDop - target.paidDepositDop).toInt().coerceAtLeast(0)
            val resolvedKind = kindLabel.trim().ifEmpty {
                if (amountDop >= balanceDop) "Liquidación" else "Abono"
            }
            val record = PaymentRecord(
                id = "pay-${existingPayments.size + 1}",
                receiptNumber = receipt,
                eventId = target.id,
                eventTitle = target.title,
                clientName = target.clientName,
                amountDop = amountDop,
                methodLabel = "$cleanMethod · $shortDateLabel",
                kindLabel = resolvedKind,
                dateLabel = fullDateLabel,
                referenceCode = cleanRef,
                verified = resolvedVerified,
                voucherUrl = cleanVoucher,
                referenceNumber = cleanRef,
                ncfType = cleanNcfType,
                clientRnc = cleanRnc,
                subtotalDop = subtotalDop,
                itbisDop = itbisDop,
            )
            upsertPaymentRow(record, -(existingPayments.size + 1))
            if (resolvedVerified) {
                val newPaid = (target.paidDepositDop.toInt() + amountDop).coerceAtMost(target.totalDop.toInt())
                queries.updateEventPaidDeposit(
                    paidDepositDop = newPaid.toLong(),
                    depositMethodLabel = cleanMethod,
                    id = target.id,
                )
            }
            reloadPaymentsFromSql()
            reloadEventsFromSql()
            record
        }
        val remoteAttempt = runCatching {
            apiClient?.registerPayment(
                RegisterPaymentInputDto(
                    eventId = createdRecord.eventId,
                    eventTitle = createdRecord.eventTitle,
                    clientName = createdRecord.clientName,
                    amountRD = createdRecord.amountDop.toLong(),
                    method = normalizedPayment.canonicalMethod,
                    kind = createdRecord.kindLabel,
                    referenceCode = cleanRef,
                    bankName = resolvedBank.ifBlank { null },
                    voucherUrl = cleanVoucher.ifBlank { null },
                    ncfType = cleanNcfType,
                    clientRnc = cleanRnc.ifBlank { null },
                    referenceNumber = cleanRef,
                    contactPhone = cleanContactPhone.ifBlank { null },
                ),
            )
        }
        val shouldQueuePayment = if (apiClient == null) true else remoteAttempt.isFailure
        if (shouldQueuePayment) {
            enqueueOutboxMutation(
                actionLabel = "Registrar pago ${createdRecord.receiptNumber}",
                entityCode = createdRecord.eventId,
                operationType = "register_payment",
                payloadJson = sqlJsonCodec.encodeToString(
                    RegisterPaymentInputDto(
                        eventId = createdRecord.eventId,
                        eventTitle = createdRecord.eventTitle,
                        clientName = createdRecord.clientName,
                        amountRD = createdRecord.amountDop.toLong(),
                        method = normalizedPayment.canonicalMethod,
                        kind = createdRecord.kindLabel,
                        referenceCode = cleanRef,
                        bankName = resolvedBank.ifBlank { null },
                        voucherUrl = cleanVoucher.ifBlank { null },
                        ncfType = cleanNcfType,
                        clientRnc = cleanRnc.ifBlank { null },
                        referenceNumber = cleanRef,
                        contactPhone = cleanContactPhone.ifBlank { null },
                    ),
                ),
                idempotencyKey = "idem-pay-${createdRecord.id}-$cleanRef",
            )
        }
        createdRecord
    }

    private suspend fun enqueueOutboxMutation(
        actionLabel: String,
        entityCode: String,
        operationType: String,
        payloadJson: String,
        idempotencyKey: String,
    ) {
        sqlMutex.withLock {
            val existingCount = queries.selectAllOfflineMutations().executeAsList().size
            val item = OfflineQueueItem(
                id = "off-${existingCount + 1}",
                actionLabel = actionLabel,
                entityCode = entityCode,
                timeAgo = "Pendiente",
                synced = false,
                operationType = operationType,
                payloadJson = payloadJson,
                idempotencyKey = idempotencyKey,
            )
            upsertOfflineMutationRow(item)
            reloadOfflineQueueFromSql()
        }
    }

    override fun toggleChecklistStep(stepId: String) {
        toggleChecklistStep(stepId = stepId, onError = {})
    }

    override fun toggleChecklistStep(
        stepId: String,
        onError: (String) -> Unit,
    ) {
        val nowHm = DomainClockFormatter.currentTimeHmLabel()
        _checklist.update { list ->
            list.map { step ->
                if (step.id == stepId) {
                    val nextCompleted = !step.completed
                    step.copy(
                        completed = nextCompleted,
                        timeLabel = if (nextCompleted) nowHm else "Pendiente",
                    )
                } else {
                    step
                }
            }
        }
        scope.launch {
            toggleChecklistStepAsync(stepId, onError)
        }
    }

    override fun syncOfflineQueue() {
        scope.launch {
            syncOfflineQueueAsync()
        }
    }

    override fun adjustArticleStock(articleId: String, delta: Int) {
        _articles.update { list ->
            list.map { article ->
                if (article.id == articleId) {
                    val newStock = (article.totalStock + delta).coerceAtLeast(0)
                    val newAvail = (article.availableOct23 + delta).coerceIn(0, newStock)
                    article.copy(totalStock = newStock, availableOct23 = newAvail)
                } else {
                    article
                }
            }
        }
        scope.launch {
            adjustArticleStockAsync(articleId, delta)
        }
    }

    override fun login(email: String, password: String): Boolean = authenticateUser(email, password).success

    override suspend fun authenticateUserOnline(identifier: String, password: String): AuthOutcome = submodules.authenticateUserOnline(identifier, password, ::authenticateUser)

    override suspend fun registerUserOnline(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        phone: String,
    ): AuthOutcome = submodules.registerUserOnline(
        firstName = firstName,
        lastName = lastName,
        username = username,
        email = email,
        password = password,
        birthDate = birthDate,
        phone = phone,
        localFallback = {
            registerUser(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                birthDate = birthDate,
                phone = phone,
            )
        },
    )

    override fun authenticateUser(identifier: String, password: String): AuthOutcome {
        val cleanIdentifier = identifier.trim().lowercase()
        val cleanPass = password.trim()
        val identifierError = InputValidator.validateLoginIdentifier(cleanIdentifier)
        if (identifierError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_INVALID_CREDENTIALS",
                errorMessage = identifierError,
            )
        }
        val passwordError = InputValidator.validatePassword(cleanPass)
        if (passwordError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_INVALID_CREDENTIALS",
                errorMessage = passwordError,
            )
        }
        val matchedAccount = findAccountByIdentifier(userAccountsState.value, cleanIdentifier)
        if (matchedAccount == null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_INVALID_CREDENTIALS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
            )
        }
        if (matchedAccount.passwordHash.isEmpty()) {
            return AuthOutcome.failure(
                errorCode = "AUTH_INVALID_CREDENTIALS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
            )
        }
        val passwordValid = JwtAuthManager.verifyPassword(
            rawPassword = cleanPass,
            salt = matchedAccount.salt,
            expectedHash = matchedAccount.passwordHash,
        )
        if (!passwordValid) {
            return AuthOutcome.failure(
                errorCode = "AUTH_INVALID_CREDENTIALS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
            )
        }
        val token = JwtAuthManager.issueAccessToken(matchedAccount)
        _currentAuthenticatedUser.value = matchedAccount
        _currentSessionJwt.value = token
        _currentUserEmail.value = matchedAccount.email
        _activeRole.value = matchedAccount.role
        submodules.propagateSessionTokenToApi(token)
        persistActiveSessionAndSyncLoginAsync(matchedAccount, cleanPass, token)
        return AuthOutcome.authenticated(accessToken = token, account = matchedAccount)
    }

    private fun persistActiveSessionAndSyncLoginAsync(
        account: RegisteredUserAccount,
        rawPassword: String,
        localToken: String,
    ) {
        scope.launch {
            sqlMutex.withLock {
                queries.upsertActiveSession(
                    userId = account.id,
                    accessToken = localToken,
                )
            }
            val client = apiClient ?: return@launch
            val remoteLogin = runCatching {
                client.login(
                    LoginInputDto(
                        email = account.email,
                        password = rawPassword,
                    ),
                )
            }.getOrNull()
            if (remoteLogin != null) {
                _currentSessionJwt.value = remoteLogin.accessToken
                client.setAccessToken(remoteLogin.accessToken)
                sqlMutex.withLock {
                    queries.upsertActiveSession(
                        userId = account.id,
                        accessToken = remoteLogin.accessToken,
                    )
                }
                syncFromBackendAsync()
            }
        }
    }

    override fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        phone: String,
    ): AuthOutcome {
        val cleanFirst = firstName.trim()
        val cleanLast = lastName.trim()
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()
        val cleanBirthDate = birthDate.trim()
        val cleanPhone = phone.trim()

        val fullName = listOf(cleanFirst, cleanLast).filter { part -> part.isNotEmpty() }.joinToString(" ")
        val resolvedUsername = cleanUsername.ifEmpty { cleanEmail.substringBefore("@") }

        val fullNameError = InputValidator.validateFullName(fullName)
        if (fullNameError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_MISSING_FIELDS",
                errorMessage = fullNameError,
            )
        }
        val usernameError = InputValidator.validateUsername(resolvedUsername)
        if (usernameError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_MISSING_FIELDS",
                errorMessage = usernameError,
            )
        }
        val emailError = InputValidator.validateEmail(cleanEmail)
        if (emailError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_MISSING_FIELDS",
                errorMessage = emailError,
            )
        }
        val birthDateError = InputValidator.validateBirthDate(cleanBirthDate, isRequired = false)
        if (birthDateError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_MISSING_FIELDS",
                errorMessage = birthDateError,
            )
        }
        val passwordError = InputValidator.validatePassword(cleanPassword)
        if (passwordError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_WEAK_PASSWORD",
                errorMessage = passwordError,
            )
        }
        val existingAccounts = userAccountsState.value
        val emailTaken = existingAccounts.any { account -> account.email.equals(cleanEmail, ignoreCase = true) }
        if (emailTaken) {
            return AuthOutcome.failure(
                errorCode = "AUTH_EMAIL_TAKEN",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_EMAIL_TAKEN", null),
            )
        }
        val usernameTaken = existingAccounts.any { account -> account.username.equals(resolvedUsername, ignoreCase = true) }
        if (usernameTaken) {
            return AuthOutcome.failure(
                errorCode = "AUTH_USERNAME_TAKEN",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_USERNAME_TAKEN", null),
            )
        }

        val salt = JwtAuthManager.generateDeterministicSalt("$cleanEmail:$resolvedUsername")
        val passwordHash = JwtAuthManager.hashPassword(cleanPassword, salt)
        val newAccount = RegisteredUserAccount(
            id = "usr-${existingAccounts.size + 1}-$resolvedUsername",
            username = resolvedUsername,
            email = cleanEmail,
            fullName = fullName,
            passwordHash = passwordHash,
            salt = salt,
            birthDate = cleanBirthDate,
            phone = cleanPhone,
            role = "client",
        )
        userAccountsState.update { accounts -> accounts + newAccount }
        val token = JwtAuthManager.issueAccessToken(newAccount)
        _currentAuthenticatedUser.value = newAccount
        _currentSessionJwt.value = token
        _currentUserEmail.value = newAccount.email
        _activeRole.value = newAccount.role
        submodules.propagateSessionTokenToApi(token)
        scope.launch {
            sqlMutex.withLock {
                upsertUserAccountRow(newAccount)
                queries.upsertActiveSession(
                    userId = newAccount.id,
                    accessToken = token,
                )
                reloadUserAccountsFromSql()
            }
            val client = apiClient ?: return@launch
            val remoteReg = runCatching {
                client.register(
                    RegisterInputDto(
                        username = newAccount.username,
                        fullName = newAccount.fullName,
                        email = newAccount.email,
                        password = cleanPassword,
                        birthDate = newAccount.birthDate,
                        phone = newAccount.phone,
                    ),
                )
            }.getOrNull()
            if (remoteReg != null) {
                _currentSessionJwt.value = remoteReg.accessToken
                client.setAccessToken(remoteReg.accessToken)
                sqlMutex.withLock {
                    queries.upsertActiveSession(
                        userId = newAccount.id,
                        accessToken = remoteReg.accessToken,
                    )
                }
                syncFromBackendAsync()
            }
        }
        return AuthOutcome.authenticated(accessToken = token, account = newAccount)
    }

    override fun resetUserPassword(identifier: String, newPassword: String): AuthOutcome {
        val cleanIdentifier = identifier.trim().lowercase()
        val cleanPassword = newPassword.trim()
        val passwordError = InputValidator.validatePassword(cleanPassword)
        if (passwordError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_WEAK_PASSWORD",
                errorMessage = passwordError,
            )
        }
        val targetIdentifier = cleanIdentifier.ifEmpty { _currentUserEmail.value }
        val identifierError = InputValidator.validateLoginIdentifier(targetIdentifier)
        if (identifierError != null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_ACCOUNT_NOT_FOUND",
                errorMessage = identifierError,
            )
        }
        val existingAccount = findAccountByIdentifier(userAccountsState.value, targetIdentifier)
        if (existingAccount == null) {
            return AuthOutcome.failure(
                errorCode = "AUTH_ACCOUNT_NOT_FOUND",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_ACCOUNT_NOT_FOUND", null),
            )
        }
        val nextSalt = JwtAuthManager.generateDeterministicSalt("${existingAccount.email}:reset")
        val nextHash = JwtAuthManager.hashPassword(cleanPassword, nextSalt)
        val updatedAccount = existingAccount.copy(
            passwordHash = nextHash,
            salt = nextSalt,
        )
        userAccountsState.update { list ->
            list.map { account ->
                if (account.id == updatedAccount.id) updatedAccount else account
            }
        }
        val token = JwtAuthManager.issueAccessToken(updatedAccount)
        _currentAuthenticatedUser.value = updatedAccount
        _currentSessionJwt.value = token
        submodules.propagateSessionTokenToApi(token)
        scope.launch {
            sqlMutex.withLock {
                queries.updateUserAccountPassword(
                    passwordHash = updatedAccount.passwordHash,
                    salt = updatedAccount.salt,
                    id = updatedAccount.id,
                )
                queries.upsertActiveSession(
                    userId = updatedAccount.id,
                    accessToken = token,
                )
                reloadUserAccountsFromSql()
            }
            runCatching {
                apiClient?.resetPassword(
                    ResetPasswordInputDto(
                        identifier = updatedAccount.email,
                        email = updatedAccount.email,
                        newPassword = cleanPassword,
                    ),
                )
            }
        }
        return AuthOutcome.authenticated(accessToken = token, account = updatedAccount)
    }

    override fun updateUserProfile(
        fullName: String,
        email: String,
        phone: String,
        birthDate: String,
        username: String,
        avatarKey: String?,
    ): AuthOutcome {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()
        val cleanBirthDate = birthDate.trim()
        val cleanUsername = username.trim().lowercase()

        val nameError = InputValidator.validateFullName(cleanName)
        if (nameError != null) {
            return AuthOutcome.failure(errorCode = "PROFILE_INVALID_NAME", errorMessage = nameError)
        }
        val emailError = InputValidator.validateEmail(cleanEmail)
        if (emailError != null) {
            return AuthOutcome.failure(errorCode = "PROFILE_INVALID_EMAIL", errorMessage = emailError)
        }
        val phoneError = InputValidator.validatePhone(cleanPhone)
        if (phoneError != null) {
            return AuthOutcome.failure(errorCode = "PROFILE_INVALID_PHONE", errorMessage = phoneError)
        }
        val birthDateError = InputValidator.validateBirthDate(cleanBirthDate, isRequired = true)
        if (birthDateError != null) {
            return AuthOutcome.failure(errorCode = "PROFILE_INVALID_BIRTH_DATE", errorMessage = birthDateError)
        }
        val usernameError = InputValidator.validateUsername(cleanUsername)
        if (usernameError != null) {
            return AuthOutcome.failure(errorCode = "PROFILE_INVALID_USERNAME", errorMessage = usernameError)
        }

        val currentAccount = _currentAuthenticatedUser.value
            ?: findAccountByIdentifier(userAccountsState.value, _currentUserEmail.value)
            ?: return AuthOutcome.failure(
                errorCode = "AUTH_UNAUTHORIZED",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_UNAUTHORIZED", null),
            )

        val allAccounts = userAccountsState.value
        val emailOccupiedByOther = allAccounts.any { account ->
            val isDifferentAccount = account.id != currentAccount.id
            val sameEmail = account.email.equals(cleanEmail, ignoreCase = true)
            if (isDifferentAccount) sameEmail else false
        }
        if (emailOccupiedByOther) {
            return AuthOutcome.failure(
                errorCode = "AUTH_EMAIL_TAKEN",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_EMAIL_TAKEN", null),
            )
        }
        val usernameOccupiedByOther = allAccounts.any { account ->
            val isDifferentAccount = account.id != currentAccount.id
            val sameUsername = account.username.equals(cleanUsername, ignoreCase = true)
            if (isDifferentAccount) sameUsername else false
        }
        if (usernameOccupiedByOther) {
            return AuthOutcome.failure(
                errorCode = "AUTH_USERNAME_TAKEN",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_USERNAME_TAKEN", null),
            )
        }

        val resolvedAvatarKey = avatarKey?.trim() ?: currentAccount.avatarKey
        val updatedAccount = currentAccount.copy(
            fullName = cleanName,
            email = cleanEmail,
            phone = cleanPhone,
            birthDate = cleanBirthDate,
            username = cleanUsername,
            avatarKey = resolvedAvatarKey,
        )
        userAccountsState.update { accounts ->
            val hadExisting = accounts.any { account -> account.id == updatedAccount.id }
            if (hadExisting) {
                accounts.map { account ->
                    if (account.id == updatedAccount.id) updatedAccount else account
                }
            } else {
                accounts + updatedAccount
            }
        }
        val existingToken = _currentSessionJwt.value?.trim().orEmpty().ifEmpty {
            apiClient?.currentAccessToken()?.trim().orEmpty()
        }
        val preservedRefresh = apiClient?.currentRefreshToken()?.trim().orEmpty()
        val preservedToken = if (existingToken.isNotEmpty()) {
            existingToken
        } else {
            JwtAuthManager.issueAccessToken(updatedAccount)
        }
        _currentAuthenticatedUser.value = updatedAccount
        _currentSessionJwt.value = preservedToken
        _currentUserEmail.value = updatedAccount.email
        submodules.propagateSessionTokenToApi(preservedToken)
        scope.launch {
            sqlMutex.withLock {
                upsertUserAccountRow(updatedAccount)
                queries.upsertActiveSession(
                    userId = updatedAccount.id,
                    accessToken = preservedToken,
                )
                reloadUserAccountsFromSql()
            }
            runCatching {
                apiClient?.updateMyProfile(
                    UpdateProfileInputDto(
                        avatarKey = avatarKey?.trim(),
                        fullName = cleanName,
                        email = cleanEmail,
                        phone = cleanPhone,
                        birthDate = cleanBirthDate,
                        username = cleanUsername,
                    ),
                )
            }
        }
        return AuthOutcome.authenticated(
            accessToken = preservedToken,
            account = updatedAccount,
            refreshToken = preservedRefresh,
        )
    }

    override suspend fun updateUserAvatarOnline(avatarKey: String?): AuthOutcome = submodules.updateUserAvatarOnline(avatarKey)

    override suspend fun requestPasswordOtp(email: String): OtpChallengeOutcome = submodules.requestPasswordOtp(email)

    override suspend fun verifyPasswordOtp(email: String, code: String): OtpChallengeOutcome = submodules.verifyPasswordOtp(email, code)

    override suspend fun resetPasswordWithOtp(
        email: String,
        newPassword: String,
        resetToken: String,
        otpCode: String,
    ): AuthOutcome = submodules.resetPasswordWithOtp(email, newPassword, resetToken, otpCode, ::resetUserPassword)

    override suspend fun loginWithOAuth(provider: String, idToken: String): AuthOutcome = submodules.loginWithOAuth(provider, idToken)

    override suspend fun startOAuthAuthorization(
        provider: String,
        redirectUri: String,
        codeChallenge: String,
        state: String,
    ): Result<String> = submodules.startOAuthAuthorization(provider, redirectUri, codeChallenge, state)

    override suspend fun exchangeOAuthCode(
        provider: String,
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): AuthOutcome = submodules.exchangeOAuthCode(provider, code, codeVerifier, redirectUri)

    override suspend fun refreshSession(refreshToken: String): AuthOutcome = submodules.refreshSession(refreshToken)

    override fun setRefreshToken(token: String?) {
        submodules.setRefreshToken(token)
    }

    override fun setSessionLifecycleCallbacks(
        onTokensRefreshed: suspend (accessToken: String, refreshToken: String) -> Unit,
        onSessionExpired: suspend () -> Unit,
    ) {
        submodules.setSessionLifecycleCallbacks(onTokensRefreshed, onSessionExpired)
    }

    override suspend fun registerDevicePushToken(
        platform: String,
        token: String,
        appVariant: String,
    ): Result<Boolean> = submodules.registerDevicePushToken(platform, token, appVariant)

    override suspend fun unregisterDevicePushToken(token: String): Result<Boolean> = submodules.unregisterDevicePushToken(token)

    override fun changeUserPassword(currentPassword: String, newPassword: String): AuthOutcome = submodules.changeUserPassword(currentPassword, newPassword)

    override fun logoutSession() {
        submodules.logoutSession {
            clearSessionInternal(clearApiToken = false)
        }
    }

    override fun restoreSessionFromJwt(accessToken: String): RegisteredUserAccount? {
        val cleanToken = accessToken.trim()
        val claims = JwtAuthManager.verifyAccessToken(cleanToken) ?: return null
        val existingAccount = findAccountByIdentifier(userAccountsState.value, claims.email)
            ?: findAccountByIdentifier(userAccountsState.value, claims.username)
        val resolvedAccount = if (existingAccount != null) {
            existingAccount
        } else {
            val defaultSalt = JwtAuthManager.generateDeterministicSalt("${claims.email}:${claims.username}")
            val reconstructed = RegisteredUserAccount(
                id = claims.subjectId,
                username = claims.username.ifEmpty { claims.email.substringBefore("@") },
                email = claims.email,
                fullName = claims.fullName.ifEmpty { claims.username },
                passwordHash = "",
                salt = defaultSalt,
                birthDate = claims.birthDate,
                phone = "",
                role = claims.role.ifBlank { "client" },
            )
            userAccountsState.update { accounts -> accounts + reconstructed }
            scope.launch {
                sqlMutex.withLock {
                    upsertUserAccountRow(reconstructed)
                    queries.upsertActiveSession(
                        userId = reconstructed.id,
                        accessToken = cleanToken,
                    )
                    reloadUserAccountsFromSql()
                }
            }
            reconstructed
        }
        _currentAuthenticatedUser.value = resolvedAccount
        _currentSessionJwt.value = cleanToken
        _currentUserEmail.value = resolvedAccount.email
        _activeRole.value = resolvedAccount.role
        submodules.propagateSessionTokenToApi(cleanToken)
        scope.launch {
            sqlMutex.withLock {
                queries.upsertActiveSession(
                    userId = resolvedAccount.id,
                    accessToken = cleanToken,
                )
            }
        }
        return resolvedAccount
    }

    override fun clearSession() {
        clearSessionInternal(clearApiToken = true)
    }

    private fun resetInMemoryUserScopedState(clearApiToken: Boolean) {
        _currentSessionJwt.value = null
        _currentAuthenticatedUser.value = null
        _currentUserEmail.value = ""
        _activeRole.value = "guest"
        _events.value = emptyList()
        _payments.value = emptyList()
        _checklist.value = emptyList()
        _offlineQueue.value = emptyList()
        _favorites.value = emptySet()
        _chatMessages.value = emptyList()
        _staffMembers.value = emptyList()
        checklistEventIdsByStepId.clear()
        _metrics.value = CanonicalSampleData.zeroBusinessMetrics
        submodules.clearUserScopedFlows()
        if (clearApiToken) {
            apiClient?.setAccessToken(null)
            apiClient?.setRefreshToken(null)
        }
    }

    private fun wipeUserScopedSqlTablesInTransaction() {
        queries.clearActiveSession()
        queries.deleteAllEvents()
        queries.deleteAllPayments()
        queries.deleteAllQuotes()
        queries.clearDeliveryStops()
        queries.deleteAllDamageIncidents()
        queries.deleteAllNotifications()
        queries.deleteAllFavorites()
        queries.deleteAllChatMessages()
        queries.deleteAllChecklistSteps()
        queries.deleteAllOfflineMutations()
        queries.deleteAllSubmoduleSnapshots()
        queries.clearStaffPermissions()
    }

    private fun clearSessionInternal(clearApiToken: Boolean) {
        resetInMemoryUserScopedState(clearApiToken = clearApiToken)
        scope.launch {
            sqlMutex.withLock {
                database.transaction {
                    wipeUserScopedSqlTablesInTransaction()
                }
            }
        }
    }

    override suspend fun deleteCurrentAccountOnline(): Result<Unit> {
        val currentToken = _currentSessionJwt.value
        val deletedUserId = _currentAuthenticatedUser.value?.id?.trim().orEmpty()
        val client = apiClient
        if (client != null) {
            if (!currentToken.isNullOrBlank()) {
                val result = runCatching { client.deleteAccount(currentToken) }
                if (result.isFailure) {
                    return Result.failure(result.exceptionOrNull() ?: IllegalStateException("ACCOUNT_DELETE_FAILED"))
                }
            }
        }
        if (deletedUserId.isNotEmpty()) {
            userAccountsState.update { accounts ->
                accounts.filterNot { account -> account.id == deletedUserId }
            }
        }
        resetInMemoryUserScopedState(clearApiToken = true)
        sqlMutex.withLock {
            database.transaction {
                wipeUserScopedSqlTablesInTransaction()
                if (deletedUserId.isNotEmpty()) {
                    queries.deleteUserAccountById(deletedUserId)
                }
            }
            reloadUserAccountsFromSql()
        }
        return Result.success(Unit)
    }

    override fun deleteCurrentAccount(): Boolean {
        val currentToken = _currentSessionJwt.value
        val deletedUserId = _currentAuthenticatedUser.value?.id?.trim().orEmpty()
        if (deletedUserId.isNotEmpty()) {
            userAccountsState.update { accounts ->
                accounts.filterNot { account -> account.id == deletedUserId }
            }
        }
        resetInMemoryUserScopedState(clearApiToken = true)
        scope.launch {
            sqlMutex.withLock {
                database.transaction {
                    wipeUserScopedSqlTablesInTransaction()
                    if (deletedUserId.isNotEmpty()) {
                        queries.deleteUserAccountById(deletedUserId)
                    }
                }
                reloadUserAccountsFromSql()
            }
            if (!currentToken.isNullOrBlank()) {
                runCatching {
                    apiClient?.deleteAccount(currentToken)
                }
            }
        }
        return true
    }

    override fun refreshSharedState() {
        scope.launch {
            sqlMutex.withLock {
                runCatching {
                    reloadFromSql()
                }
            }
            if (apiClient != null) {
                syncFromBackendAsync()
            }
        }
    }

    private fun findAccountByIdentifier(
        accounts: List<RegisteredUserAccount>,
        rawIdentifier: String,
    ): RegisteredUserAccount? {
        val normalized = rawIdentifier.trim().lowercase()
        if (normalized.isEmpty()) return null
        val directMatch = accounts.firstOrNull { account ->
            val emailMatch = account.email.equals(normalized, ignoreCase = true)
            val usernameMatch = account.username.equals(normalized, ignoreCase = true)
            if (emailMatch) true else usernameMatch
        }
        if (directMatch != null) return directMatch
        val localPart = normalized.substringBefore("@")
        val isRosaFiestaDomain = normalized.endsWith("@rosafiesta.do")
        if (isRosaFiestaDomain) {
            return accounts.firstOrNull { account ->
                account.username.equals(localPart, ignoreCase = true)
            }
        }
        return null
    }

    override fun switchRole(role: String) {
        val normalized = role.trim().ifEmpty { "client" }
        _activeRole.value = normalized
        _currentUserEmail.value = when (normalized) {
            "admin" -> "maria@rosafiesta.do"
            "staff_yuli" -> "yuli@rosafiesta.do"
            "guest" -> "invitado@rosafiesta.do"
            else -> "camila@rosafiesta.do"
        }
        val targetView = when {
            normalized == "admin" -> "admin"
            normalized.startsWith("staff") -> "admin"
            else -> "personal"
        }
        if (submodules.hasRealServerToken()) {
            scope.launch {
                runCatching {
                    apiClient?.switchView(targetView = targetView)
                }
            }
        }
    }

    override fun toggleFavorite(articleId: String) {
        val cleanId = articleId.trim()
        if (cleanId.isEmpty()) return
        _favorites.update { current ->
            if (cleanId in current) current - cleanId else current + cleanId
        }
        scope.launch {
            toggleFavoriteAsync(cleanId)
        }
    }

    override fun toggleStaffPaymentAccess(staffName: String) {
        val target = staffName.trim()
        if (target.isEmpty()) return
        _staffMembers.update { list ->
            list.map { member ->
                if (member.name.equals(target, ignoreCase = true)) {
                    val nextCanView = !member.canViewPayments
                    val nextPerms = if (nextCanView) {
                        member.permissions + StaffPermission.PAYMENTS
                    } else {
                        member.permissions - StaffPermission.PAYMENTS
                    }
                    val nextLabel = if (member.name.equals("Yuli", ignoreCase = true)) {
                        if (nextCanView) {
                            "Decoración, tareas y consulta de pagos"
                        } else {
                            "Decoración y tareas (sin acceso a pagos)"
                        }
                    } else {
                        member.roleLabel
                    }
                    member.copy(
                        canViewPayments = nextCanView,
                        permissions = nextPerms,
                        roleLabel = nextLabel,
                    )
                } else {
                    member
                }
            }
        }
        scope.launch {
            toggleStaffPaymentAccessAsync(target)
        }
    }

    override fun sendChatMessage(author: String, message: String) {
        val cleanMsg = message.trim()
        if (cleanMsg.isEmpty()) return
        val cleanAuthor = author.trim().ifEmpty { "Camila" }
        _chatMessages.update { messages -> messages + "$cleanAuthor: $cleanMsg" }
        val threadId = submodules.chatThreads.value.firstOrNull()?.id ?: "thread-general"
        scope.launch {
            sendChatMessageAsync(cleanAuthor, cleanMsg)
            if (submodules.hasRealServerToken()) {
                runCatching {
                    apiClient?.sendThreadMessage(
                        threadId = threadId,
                        input = PostChatMessageInputDto(body = cleanMsg),
                    )
                }
            }
        }
    }

    override fun createArticle(
        code: String,
        name: String,
        category: String,
        pricePerDayDop: Int,
        totalStock: Int,
    ) {
        createArticleWithMedia(
            code = code,
            name = name,
            category = category,
            pricePerDayDop = pricePerDayDop,
            totalStock = totalStock,
            photoUrl = "",
        )
    }

    override fun createArticleWithMedia(
        code: String,
        name: String,
        category: String,
        pricePerDayDop: Int,
        totalStock: Int,
        photoUrl: String,
        description: String,
        dimensions: String,
        material: String,
        visibleInCatalog: Boolean,
    ) {
        val cleanCode = code.trim()
        val cleanName = name.trim()
        val cleanCategory = category.trim()
        val cleanPhotoUrl = photoUrl.trim()
        val cleanDimensions = dimensions.trim()
        val cleanMaterial = material.trim()
        if (cleanCode.isEmpty()) return
        if (cleanName.isEmpty()) return
        if (pricePerDayDop <= 0) return
        if (totalStock < 0) return

        val resolvedDescription = description.trim().ifEmpty {
            "$cleanName para eventos y celebraciones en Santo Domingo."
        }
        val photo = RosaFiestaBackendMappers.selectPhotoForArticle(cleanCategory, cleanName)
        val newArticle = CatalogArticle(
            id = "art-${cleanCode.lowercase()}",
            code = cleanCode,
            name = cleanName,
            category = cleanCategory,
            description = resolvedDescription,
            pricePerDayDop = pricePerDayDop,
            totalStock = totalStock,
            availableOct23 = totalStock,
            photo = photo,
            variants = emptyList(),
            dimensions = cleanDimensions,
            material = cleanMaterial,
            visibleInCatalog = visibleInCatalog,
            photoUrl = cleanPhotoUrl,
        )
        _articles.update { list ->
            val filtered = list.filterNot { candidate ->
                val sameId = candidate.id == newArticle.id
                val sameCode = candidate.code.equals(cleanCode, ignoreCase = true)
                if (sameId) true else sameCode
            }
            filtered + newArticle
        }
        scope.launch {
            createArticleAsync(
                code = cleanCode,
                name = cleanName,
                category = cleanCategory,
                pricePerDayDop = pricePerDayDop,
                totalStock = totalStock,
                photoUrl = cleanPhotoUrl,
                description = resolvedDescription,
                dimensions = cleanDimensions,
                material = cleanMaterial,
                visibleInCatalog = visibleInCatalog,
            )
        }
    }

    override fun createEvent(
        title: String,
        clientName: String,
        dateLabel: String,
        venue: String,
        guests: Int,
    ) {
        createEvent(
            title = title,
            clientName = clientName,
            dateLabel = dateLabel,
            venue = venue,
            guests = guests,
            contactPhone = "",
        )
    }

    override fun createEvent(
        title: String,
        clientName: String,
        dateLabel: String,
        venue: String,
        guests: Int,
        contactPhone: String,
    ) {
        val cleanTitle = title.trim()
        val cleanClient = clientName.trim()
        val cleanDate = dateLabel.trim()
        val cleanVenue = venue.trim()
        val cleanContactPhone = contactPhone.trim()
        if (cleanTitle.isEmpty()) return
        if (cleanClient.isEmpty()) return
        if (cleanDate.isEmpty()) return
        if (cleanVenue.isEmpty()) return
        if (guests < 0) return

        val safeProposalGuests = guests.coerceAtLeast(10)
        val lines = buildFallbackItems(cleanTitle, safeProposalGuests)
        val deliveryFeeDop = RosaFiestaBackendMappers.resolveDeliveryFeeDop(cleanVenue, businessSettings.value)
        val linesSubtotal = lines.sumOf { line -> line.subtotalDop }
        val consistentTotalDop = linesSubtotal + deliveryFeeDop
        val computedDaysUntil = DomainClockFormatter.computeDaysUntilEvent(cleanDate)
        val assignedLead = RosaFiestaBackendMappers.selectLeastLoadedStaffLead(_staffMembers.value, _events.value)
        val seq = 1120 + _events.value.size
        val eventId = "evt-$seq"
        val newEvent = ClientEvent(
            id = eventId,
            code = "RF-2026-$seq",
            title = cleanTitle,
            clientName = cleanClient,
            eventType = "Evento",
            dateLabel = cleanDate,
            daysUntilEvent = computedDaysUntil,
            venue = cleanVenue,
            zone = "Santo Domingo DN",
            guests = guests,
            articleCount = lines.sumOf { line -> line.quantity },
            totalDop = consistentTotalDop,
            paidDepositDop = 0,
            depositMethodLabel = "Pendiente de anticipo",
            depositDateLabel = "—",
            balanceDueDateLabel = cleanDate,
            status = EventStatus.PENDING_QUOTE,
            leadStaffName = assignedLead,
            photo = PhotoAsset.BODA,
            items = lines,
            contactPhone = cleanContactPhone,
        )
        _events.update { existing -> existing + newEvent }
        recalculateMetricsFromState()
        scope.launch {
            createEventAsync(
                title = cleanTitle,
                clientName = cleanClient,
                dateLabel = cleanDate,
                venue = cleanVenue,
                guests = guests,
                preassignedEventId = eventId,
                contactPhone = cleanContactPhone,
            )
        }
    }

    override fun changeEventDate(eventId: String, newDateLabel: String): DateChangeResult {
        val cleanDate = newDateLabel.trim()
        val currentEvent = _events.value.firstOrNull { event -> event.id == eventId.trim() }
            ?: return RosaFiestaEngine.evaluateDateChange(0, 2)
        val eval = RosaFiestaEngine.evaluateDateChange(currentEvent.daysUntilEvent, currentEvent.priorChangesCount)
        if (!eval.allowed) return eval
        if (cleanDate.isEmpty()) return eval

        _events.update { list ->
            list.map { evt ->
                if (evt.id == currentEvent.id) {
                    evt.copy(
                        dateLabel = cleanDate,
                        totalDop = evt.totalDop + eval.feeDop,
                        priorChangesCount = evt.priorChangesCount + 1,
                    )
                } else {
                    evt
                }
            }
        }
        recalculateMetricsFromState()
        scope.launch {
            changeEventDateAsync(currentEvent.id, cleanDate)
        }
        return eval
    }

    override fun cancelEvent(eventId: String): CancellationRefund = cancelEvent(eventId, "")

    override fun cancelEvent(eventId: String, reason: String): CancellationRefund {
        val currentEvent = _events.value.firstOrNull { event -> event.id == eventId.trim() }
            ?: return RosaFiestaEngine.calculateCancellationRefund(0, 0)
        val refund = RosaFiestaEngine.calculateCancellationRefund(currentEvent.paidDepositDop, currentEvent.daysUntilEvent)
        val cleanReason = reason.trim()
        _events.update { list ->
            list.map { evt ->
                if (evt.id == currentEvent.id) {
                    evt.copy(
                        status = EventStatus.CANCELLED,
                        cancellationReason = cleanReason,
                    )
                } else {
                    evt
                }
            }
        }
        recalculateMetricsFromState()
        scope.launch {
            cancelEventAsync(currentEvent.id, reason)
        }
        return refund
    }

    override fun registerPayment(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
    ): PaymentRecord? = registerDetailedPayment(
        eventId = eventId,
        amountDop = amountDop,
        methodLabel = methodLabel,
        referenceCode = referenceCode,
    )

    override fun registerDetailedPayment(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
        voucherUrl: String,
        ncfType: String,
        clientRnc: String,
        bankName: String,
        kindLabel: String,
        contactPhone: String,
    ): PaymentRecord? {
        val cleanMethod = methodLabel.trim()
        val cleanRef = referenceCode.trim().ifEmpty { "REF-2026" }
        val cleanVoucher = voucherUrl.trim()
        val cleanNcfType = ncfType.trim().ifEmpty { "B02" }.uppercase()
        val cleanRnc = clientRnc.trim()
        if (amountDop <= 0) return null
        if (cleanMethod.isEmpty()) return null

        val targetEvent = _events.value.firstOrNull { event -> event.id == eventId.trim() }
            ?: return null
        val nextNumber = 185 + _payments.value.size
        val receipt = "REC-2026-${nextNumber.toString().padStart(4, '0')}"
        val shortDateLabel = DomainClockFormatter.currentShortDateLabel()
        val fullDateLabel = DomainClockFormatter.currentFullDateLabel()
        val normalizedPayment = RosaFiestaBackendMappers.normalizePaymentMethod(cleanMethod)
        val isTransferMethod = normalizedPayment.canonicalMethod.equals("TRANSFER", ignoreCase = true)
        val requiresVerification = if (isTransferMethod) true else cleanVoucher.isNotEmpty()
        val resolvedVerified = !requiresVerification
        val fiscal = RosaFiestaEngine.computeFiscalBreakdown(
            totalDop = amountDop,
            ncfType = cleanNcfType,
            rawTaxId = cleanRnc,
        )
        val subtotalDop = fiscal.subtotalDop
        val itbisDop = fiscal.itbisDop
        val resolvedKind = kindLabel.trim().ifEmpty {
            if (amountDop >= targetEvent.balanceDop) "Liquidación" else "Abono"
        }
        val record = PaymentRecord(
            id = "pay-${_payments.value.size + 1}",
            receiptNumber = receipt,
            eventId = targetEvent.id,
            eventTitle = targetEvent.title,
            clientName = targetEvent.clientName,
            amountDop = amountDop,
            methodLabel = "$cleanMethod · $shortDateLabel",
            kindLabel = resolvedKind,
            dateLabel = fullDateLabel,
            referenceCode = cleanRef,
            verified = resolvedVerified,
            voucherUrl = cleanVoucher,
            referenceNumber = cleanRef,
            ncfType = cleanNcfType,
            clientRnc = cleanRnc,
            subtotalDop = subtotalDop,
            itbisDop = itbisDop,
        )
        _payments.update { existing -> listOf(record) + existing }
        if (resolvedVerified) {
            _events.update { list ->
                list.map { evt ->
                    if (evt.id == targetEvent.id) {
                        evt.copy(
                            paidDepositDop = (evt.paidDepositDop + amountDop).coerceAtMost(evt.totalDop),
                            depositMethodLabel = cleanMethod,
                        )
                    } else {
                        evt
                    }
                }
            }
            recalculateMetricsFromState()
        }
        scope.launch {
            registerPaymentAsync(
                eventId = targetEvent.id,
                amountDop = amountDop,
                methodLabel = cleanMethod,
                referenceCode = cleanRef,
                voucherUrl = cleanVoucher,
                ncfType = cleanNcfType,
                clientRnc = cleanRnc,
                bankName = bankName,
                kindLabel = resolvedKind,
                contactPhone = contactPhone,
            )
        }
        return record
    }

    override fun createQuote(
        eventTitle: String,
        clientName: String,
        clientEmail: String,
        venue: String,
        dateLabel: String,
        guests: Int,
        lines: List<EventLineItem>,
        deliveryFeeDop: Int,
        discountDop: Int,
        eventId: String,
        notes: String,
        sendImmediately: Boolean,
        contactPhone: String,
    ): QuoteRecord = submodules.createQuote(
        eventTitle = eventTitle,
        clientName = clientName,
        clientEmail = clientEmail,
        venue = venue,
        dateLabel = dateLabel,
        guests = guests,
        lines = lines,
        deliveryFeeDop = deliveryFeeDop,
        discountDop = discountDop,
        eventId = eventId,
        notes = notes,
        sendImmediately = sendImmediately,
        contactPhone = contactPhone,
    )

    override fun sendQuote(
        quoteId: String,
        eventTitle: String,
        venue: String,
        dateLabel: String,
        guests: Int,
        lines: List<EventLineItem>,
        deliveryFeeDop: Int?,
        discountDop: Int?,
        notes: String,
    ): QuoteRecord? = submodules.sendQuote(
        quoteId = quoteId,
        eventTitle = eventTitle,
        venue = venue,
        dateLabel = dateLabel,
        guests = guests,
        lines = lines,
        deliveryFeeDop = deliveryFeeDop,
        discountDop = discountDop,
        notes = notes,
    )

    override fun approveQuote(quoteId: String, signedByName: String, signatureSvg: String): QuoteRecord? = submodules.approveQuote(quoteId, signedByName, signatureSvg)

    override fun declineQuote(quoteId: String, reason: String): QuoteRecord? = submodules.declineQuote(quoteId, reason)

    override fun duplicateQuote(quoteId: String): QuoteRecord? = submodules.duplicateQuote(quoteId)

    override suspend fun sendRosaPrompt(
        message: String,
        eventType: String,
        guests: Int,
        budgetDop: Int,
        excludedCategories: List<String>,
    ): RosaChatReply = submodules.sendRosaPrompt(message, eventType, guests, budgetDop, excludedCategories)

    override suspend fun transcribeVoicePrompt(wavBase64: String, locale: String): VoiceTranscriptionOutcome = submodules.transcribeVoicePrompt(wavBase64, locale)

    override fun approveRefund(refundId: String): RefundRequest? = submodules.approveRefund(refundId)

    override fun createReview(
        eventId: String,
        authorName: String,
        rating: Int,
        comment: String,
    ): ReviewRecord = submodules.createReview(eventId, authorName, rating, comment)

    override fun replyToReview(
        reviewId: String,
        adminReply: String,
        verified: Boolean,
    ): ReviewRecord? = submodules.replyToReview(reviewId, adminReply, verified)

    override fun createSupplier(
        name: String,
        category: String,
        contactName: String,
        phone: String,
        email: String,
        website: String,
        notes: String,
    ): SupplierRecord = submodules.createSupplier(name, category, contactName, phone, email, website, notes)

    override fun createLead(
        clientName: String,
        eventType: String,
        dateLabel: String,
        guests: Int,
        estimatedDop: Int,
        channel: String,
        stage: String,
        followUpNote: String,
        phone: String,
        email: String,
    ): LeadRecord = submodules.createLead(
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

    override fun updateLead(
        leadId: String,
        stage: String?,
        estimatedDop: Int?,
        followUpNote: String?,
    ): LeadRecord? = submodules.updateLead(leadId, stage, estimatedDop, followUpNote)

    override fun updateEventDetails(
        eventId: String,
        title: String?,
        venue: String?,
        zone: String?,
        guests: Int?,
        status: EventStatus?,
        leadStaffName: String?,
        sketchJson: String?,
        setupPhotoUrls: List<String>?,
    ): ClientEvent? = submodules.updateEventDetails(
        eventId = eventId,
        title = title,
        venue = venue,
        zone = zone,
        guests = guests,
        status = status,
        leadStaffName = leadStaffName,
        sketchJson = sketchJson,
        setupPhotoUrls = setupPhotoUrls,
    )

    override suspend fun updateEventSetupPhotos(
        eventId: String,
        setupPhotoUrls: List<String>,
    ): Result<ClientEvent> = submodules.updateEventSetupPhotos(eventId, setupPhotoUrls)

    override fun duplicateEvent(eventId: String): ClientEvent? = submodules.duplicateEvent(eventId)

    override fun addEventGuest(
        eventId: String,
        fullName: String,
        rsvpStatus: String,
        tableLabel: String,
        notes: String,
    ): EventGuest = submodules.addEventGuest(eventId, fullName, rsvpStatus, tableLabel, notes)

    override fun updateEventGuest(
        eventId: String,
        guestId: String,
        fullName: String?,
        rsvpStatus: String?,
        tableLabel: String?,
        notes: String?,
    ): EventGuest? = submodules.updateEventGuest(eventId, guestId, fullName, rsvpStatus, tableLabel, notes)

    override fun addEventScheduleBlock(
        eventId: String,
        timeRange: String,
        title: String,
        ownerName: String,
        completed: Boolean,
    ): EventScheduleBlock = submodules.addEventScheduleBlock(eventId, timeRange, title, ownerName, completed)

    override fun deleteEventScheduleBlock(eventId: String, blockId: String): Boolean = submodules.deleteEventScheduleBlock(eventId, blockId)

    override fun sendThreadMessage(
        threadId: String,
        senderName: String,
        body: String,
        attachmentName: String,
    ): ChatMessageRecord = submodules.sendThreadMessage(threadId, senderName, body, attachmentName)

    override fun saveBusinessSettings(updated: BusinessSettings): BusinessSettings = submodules.saveBusinessSettings(updated)

    override suspend fun saveBusinessSettingsOnline(updated: BusinessSettings): Result<BusinessSettings> = submodules.saveBusinessSettingsOnline(updated)

    override fun inviteStaffMember(
        fullName: String,
        email: String,
        roleLabel: String,
        role: String,
        phone: String,
        permissions: List<String>,
    ): StaffMember = submodules.inviteStaffMember(fullName, email, roleLabel, role, phone, permissions)

    override fun updateStaffMember(
        staffId: String,
        fullName: String?,
        roleLabel: String?,
        active: Boolean?,
        permissions: List<String>?,
    ): StaffMember? = submodules.updateStaffMember(staffId, fullName, roleLabel, active, permissions)

    override fun updateClientProfile(
        clientId: String,
        fullName: String?,
        email: String?,
        phone: String?,
        notes: String?,
    ): ClientProfile? = submodules.updateClientProfile(clientId, fullName, email, phone, notes)

    override fun createPackage(
        name: String,
        subtitle: String,
        eventType: String,
        suggestedGuests: Int,
        priceDop: Int,
        includedItems: List<String>,
        visibleInCatalog: Boolean,
    ): EventPackage = submodules.createPackage(
        name = name,
        subtitle = subtitle,
        eventType = eventType,
        suggestedGuests = suggestedGuests,
        priceDop = priceDop,
        includedItems = includedItems,
        visibleInCatalog = visibleInCatalog,
    )

    override fun updatePackage(
        packageId: String,
        name: String,
        subtitle: String,
        eventType: String,
        suggestedGuests: Int,
        priceDop: Int,
        includedItems: List<String>,
        visibleInCatalog: Boolean,
    ): EventPackage? = submodules.updatePackage(
        packageId = packageId,
        name = name,
        subtitle = subtitle,
        eventType = eventType,
        suggestedGuests = suggestedGuests,
        priceDop = priceDop,
        includedItems = includedItems,
        visibleInCatalog = visibleInCatalog,
    )

    override suspend fun createCategory(
        name: String,
        illoKey: String,
    ): Result<CatalogCategory> = submodules.createCategoryAsync(name = name, illoKey = illoKey)

    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        illoKey: String,
    ): Result<CatalogCategory> = submodules.updateCategoryAsync(
        categoryId = categoryId,
        name = name,
        illoKey = illoKey,
    )

    override suspend fun deleteCategory(categoryId: String): Result<Unit> = submodules.deleteCategoryAsync(categoryId)

    override suspend fun createTrend(trend: CatalogTrend): Result<CatalogTrend> = submodules.createTrendAsync(trend)

    override suspend fun updateTrend(trend: CatalogTrend): Result<CatalogTrend> = submodules.updateTrendAsync(trend)

    override suspend fun deleteTrend(trendId: String): Result<Unit> = submodules.deleteTrendAsync(trendId)

    override suspend fun saveSeasonBanner(banner: SeasonBanner): Result<SeasonBanner> = submodules.saveSeasonBannerAsync(banner)

    override suspend fun deleteArticle(articleId: String): Result<Unit> {
        val outcome = submodules.deleteArticleAsync(articleId)
        if (outcome.isSuccess) {
            sqlMutex.withLock {
                queries.deleteArticleById(articleId.trim())
            }
        }
        return outcome
    }

    override fun updateArticle(
        articleId: String,
        name: String?,
        category: String?,
        description: String?,
        pricePerDayDop: Int?,
        totalStock: Int?,
        availableStock: Int?,
        dimensions: String?,
        material: String?,
        visibleInCatalog: Boolean?,
    ): CatalogArticle? = submodules.updateArticle(
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

    override suspend fun queryArticleAvailability(articleId: String, monthIso: String): ArticleAvailabilityCalendar = submodules.queryArticleAvailability(articleId, monthIso)

    override fun reportDamage(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
    ): DamageIncident = submodules.reportDamage(
        eventId = eventId,
        articleCode = articleCode,
        articleName = articleName,
        damagedUnits = damagedUnits,
        missingUnits = missingUnits,
        replacementDop = replacementDop,
        notes = notes,
    )

    override fun markNotificationRead(notificationId: String): NotificationRecord? = submodules.markNotificationRead(notificationId)

    override fun saveBotFaq(
        question: String,
        answer: String,
        category: String,
        active: Boolean,
        id: String,
    ): BotFaqRecord = submodules.saveBotFaq(question, answer, category, active, id)

    override fun saveBotKeyword(
        keyword: String,
        autoReply: String,
        handoffToHuman: Boolean,
        id: String,
    ): BotKeywordRecord = submodules.saveBotKeyword(keyword, autoReply, handoffToHuman, id)

    override fun updateBotThread(
        threadId: String,
        botPaused: Boolean?,
        assignedAgent: String?,
        lastMessage: String?,
    ): BotThreadRecord? = submodules.updateBotThread(threadId, botPaused, assignedAgent, lastMessage)

    override suspend fun exportReport(reportType: String, period: String): ReportExportResult = submodules.exportReport(reportType, period)

    override suspend fun uploadMedia(
        filename: String,
        mimeType: String,
        purpose: String,
        bytes: ByteArray,
    ): Result<MediaUploadAsset> = submodules.uploadMedia(filename, mimeType, purpose, bytes)

    override suspend fun fetchMediaBytes(urlOrId: String): Result<ByteArray> = submodules.fetchMediaBytes(urlOrId)

    override suspend fun verifyPayment(
        paymentId: String,
        notes: String,
    ): Result<PaymentRecord> = submodules.verifyPayment(paymentId, notes)

    override suspend fun rejectPayment(
        paymentId: String,
        reason: String,
    ): Result<PaymentRecord> = submodules.rejectPayment(paymentId, reason)

    override suspend fun createCardPaymentSession(
        eventId: String,
        amountDop: Int,
        ncfType: String,
        clientRnc: String,
        contactPhone: String,
    ): Result<CardSessionResult> = submodules.createCardPaymentSession(eventId, amountDop, ncfType, clientRnc, contactPhone)

    override suspend fun completeEventCloseout(
        eventId: String,
        notes: String,
    ): Result<ClientEvent> = submodules.completeEventCloseout(eventId, notes)

    override suspend fun resolveDamageIncident(
        incidentId: String,
        notes: String,
    ): Result<DamageIncident> = submodules.resolveDamageIncident(incidentId, notes)

    override suspend fun checkRangeAvailability(
        date: String,
        articleId: String,
        quantity: Int,
        excludeEventId: String,
    ): Result<AvailabilityRangeCheck> = submodules.checkRangeAvailability(date, articleId, quantity, excludeEventId)

    override suspend fun sendWhatsAppBotReply(
        threadId: String,
        phone: String,
        message: String,
    ): Result<BotThreadRecord> = submodules.sendWhatsAppBotReply(threadId, phone, message)

    override suspend fun exportDgii607Csv(period: String): Result<String> = submodules.exportDgii607Csv(period)

    override fun observeRealtimeEvents(): Flow<RealtimeStreamEvent> = submodules.observeRealtimeEvents()

    override fun inviteStaffMemberWithPassword(
        fullName: String,
        email: String,
        roleLabel: String,
        role: String,
        phone: String,
        permissions: List<String>,
        initialPassword: String,
    ): StaffMember = submodules.inviteStaffMemberWithPassword(
        fullName = fullName,
        email = email,
        roleLabel = roleLabel,
        role = role,
        phone = phone,
        permissions = permissions,
        initialPassword = initialPassword,
    )

    override fun updateArticleWithMedia(
        articleId: String,
        name: String?,
        category: String?,
        description: String?,
        pricePerDayDop: Int?,
        totalStock: Int?,
        availableStock: Int?,
        dimensions: String?,
        material: String?,
        visibleInCatalog: Boolean?,
        photoUrl: String?,
    ): CatalogArticle? = submodules.updateArticleWithMedia(
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
        photoUrl = photoUrl,
    )

    override fun reportDamageWithMedia(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
        photoUrl: String,
    ): DamageIncident = submodules.reportDamageWithMedia(
        eventId = eventId,
        articleCode = articleCode,
        articleName = articleName,
        damagedUnits = damagedUnits,
        missingUnits = missingUnits,
        replacementDop = replacementDop,
        notes = notes,
        photoUrl = photoUrl,
    )

    override suspend fun fetchOAuthConfig(): Result<OAuthProviderConfig> = submodules.fetchOAuthConfig()

    override suspend fun recoverUsernameByIdentifier(identifier: String): Result<String> = submodules.recoverUsernameByIdentifier(identifier)

    override suspend fun updateUserPreferences(
        pushNotificationsEnabled: Boolean?,
        whatsappAlertsEnabled: Boolean?,
        emailReceiptsEnabled: Boolean?,
        biometricLockEnabled: Boolean?,
        autoSyncWifiOnly: Boolean?,
        eventRemindersEnabled: Boolean?,
        preferredLanguage: String?,
        darkThemeEnabled: Boolean?,
        savedPaymentMethod: String?,
        profileZone: String?,
    ): Result<RegisteredUserAccount> = submodules.updateUserPreferences(
        pushNotificationsEnabled = pushNotificationsEnabled,
        whatsappAlertsEnabled = whatsappAlertsEnabled,
        emailReceiptsEnabled = emailReceiptsEnabled,
        biometricLockEnabled = biometricLockEnabled,
        autoSyncWifiOnly = autoSyncWifiOnly,
        eventRemindersEnabled = eventRemindersEnabled,
        preferredLanguage = preferredLanguage,
        darkThemeEnabled = darkThemeEnabled,
        savedPaymentMethod = savedPaymentMethod,
        profileZone = profileZone,
    )

    override suspend fun sendEventPaymentReminder(
        eventId: String,
        channel: String,
        message: String,
    ): Result<PaymentReminderOutcome> = submodules.sendEventPaymentReminder(
        eventId = eventId,
        channel = channel,
        message = message,
    )

    override suspend fun confirmCardCheckoutSession(sessionId: String): Result<PaymentRecord> = submodules.confirmCardCheckoutSession(sessionId)

    override suspend fun fetchPaymentsAnalytics(range: String): Result<PaymentsAnalyticsSummary> = submodules.fetchPaymentsAnalytics(range)

    override suspend fun requestRosaSketch(
        prompt: String,
        eventType: String,
        guests: Int,
    ): Result<RosaSketchProposal> = submodules.requestRosaSketch(
        prompt = prompt,
        eventType = eventType,
        guests = guests,
    )

    override suspend fun updateEventItems(
        eventId: String,
        items: List<EventLineItem>,
    ): Result<ClientEvent> = submodules.updateEventItems(eventId = eventId, items = items)

    override suspend fun verifyAccountPassword(password: String): Result<Boolean> = submodules.verifyAccountPassword(password)

    override suspend fun changeUserPasswordOnline(currentPassword: String, newPassword: String): AuthOutcome = submodules.changeUserPasswordOnline(currentPassword, newPassword)

    override fun approveRefundWithOptions(refundId: String, options: RefundApprovalOptions): RefundRequest? = submodules.approveRefundWithOptions(refundId, options)

    override fun deleteEventGuest(eventId: String, guestId: String): Boolean = submodules.deleteEventGuest(eventId, guestId)

    override suspend fun loadEventTasks(eventId: String): List<EventTaskRecord> = submodules.loadEventTasks(eventId)

    override fun createEventTask(eventId: String, title: String, assigneeName: String): EventTaskRecord? = submodules.createEventTask(eventId, title, assigneeName)

    override fun toggleEventTask(eventId: String, taskId: String, done: Boolean): EventTaskRecord? = submodules.toggleEventTask(eventId, taskId, done)

    override fun deleteEventTask(eventId: String, taskId: String): Boolean = submodules.deleteEventTask(eventId, taskId)

    override fun updateSupplier(
        supplierId: String,
        name: String,
        category: String,
        contactName: String,
        phone: String,
        email: String,
        website: String,
        notes: String,
    ): SupplierRecord? = submodules.updateSupplier(
        supplierId = supplierId,
        name = name,
        category = category,
        contactName = contactName,
        phone = phone,
        email = email,
        website = website,
        notes = notes,
    )

    override fun deleteSupplier(supplierId: String): Boolean = submodules.deleteSupplier(supplierId)

    override fun deleteBotFaq(faqId: String): Boolean = submodules.deleteBotFaq(faqId)

    override fun deleteBotKeyword(keywordId: String): Boolean = submodules.deleteBotKeyword(keywordId)

    override fun deleteStaffMember(staffId: String): Boolean = submodules.deleteStaffMember(staffId)

    override fun deletePackage(packageId: String): Boolean = submodules.deletePackage(packageId)

    override suspend fun downloadQuoteContractPdf(quoteId: String): Result<ByteArray> = submodules.downloadQuoteContractPdf(quoteId)

    private companion object {
        private const val SUBMODULE_EXTRAS_SNAPSHOT_KEY = "extras"
    }
}
