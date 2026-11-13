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
import com.jeudry.rosafiesta.domain.BalloonProduct
import com.jeudry.rosafiesta.domain.BotFaqRecord
import com.jeudry.rosafiesta.domain.BotKeywordRecord
import com.jeudry.rosafiesta.domain.BotThreadRecord
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
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
import com.jeudry.rosafiesta.domain.EventTypeTemplate
import com.jeudry.rosafiesta.domain.InsurancePolicyRecord
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.OtpChallengeOutcome
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.RefundRequest
import com.jeudry.rosafiesta.domain.ReportExportResult
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.RosaChatReply
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Persistent SQLDelight-backed implementation of [RosaFiestaRepository] synchronized with [RosaFiestaApiClient].
 * Executes all SQLite reads/writes and HTTP requests off the main thread via [ioDispatcher].
 */
public class SqlRosaFiestaRepository(
    driver: SqlDriver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val apiClient: RosaFiestaApiClient? = null,
) : RosaFiestaRepository {
    private val database: RosaFiestaDatabase = RosaFiestaDatabase(driver)
    private val queries = database.rosaFiestaDatabaseQueries
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val sqlMutex = Mutex()

    private val checklistEventIdsByStepId: MutableMap<String, String> = CanonicalSampleData.bodaChecklist
        .associate { step -> step.id to step.eventId }
        .toMutableMap()

    private val _articles = MutableStateFlow(CanonicalSampleData.articles)
    override val articles: StateFlow<List<CatalogArticle>> = _articles.asStateFlow()

    private val _events = MutableStateFlow(CanonicalSampleData.events)
    override val events: StateFlow<List<ClientEvent>> = _events.asStateFlow()

    private val _payments = MutableStateFlow(CanonicalSampleData.payments)
    override val payments: StateFlow<List<PaymentRecord>> = _payments.asStateFlow()

    private val _checklist = MutableStateFlow(CanonicalSampleData.bodaChecklist)
    override val checklist: StateFlow<List<ChecklistStep>> = _checklist.asStateFlow()

    private val _offlineQueue = MutableStateFlow(CanonicalSampleData.offlineQueue)
    override val offlineQueue: StateFlow<List<OfflineQueueItem>> = _offlineQueue.asStateFlow()

    private val _metrics = MutableStateFlow(CanonicalSampleData.businessMetrics)
    override val metrics: StateFlow<BusinessMetrics> = _metrics.asStateFlow()

    private val _activeRole = MutableStateFlow("client")
    override val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("camila@rosafiesta.do")
    override val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val userAccountsState = MutableStateFlow(JwtAuthManager.defaultSeedAccounts)
    override val registeredAccounts: StateFlow<List<RegisteredUserAccount>> = userAccountsState.asStateFlow()

    private val _currentAuthenticatedUser = MutableStateFlow<RegisteredUserAccount?>(
        JwtAuthManager.defaultSeedAccounts.firstOrNull(),
    )
    override val currentAuthenticatedUser: StateFlow<RegisteredUserAccount?> = _currentAuthenticatedUser.asStateFlow()

    private val _currentSessionJwt = MutableStateFlow<String?>(
        JwtAuthManager.defaultSeedAccounts.firstOrNull()?.let { seedUser ->
            JwtAuthManager.issueAccessToken(seedUser)
        },
    )
    override val currentSessionJwt: StateFlow<String?> = _currentSessionJwt.asStateFlow()

    private val _favorites = MutableStateFlow(CanonicalSampleData.initialFavorites)
    override val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _staffMembers = MutableStateFlow(CanonicalSampleData.staffMembers)
    override val staffMembers: StateFlow<List<StaffMember>> = _staffMembers.asStateFlow()

    private val _chatMessages = MutableStateFlow(CanonicalSampleData.initialChatMessages)
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
    )

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

    private suspend fun persistAccountAndSessionInSql(
        account: RegisteredUserAccount,
        accessToken: String,
    ) {
        sqlMutex.withLock {
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
            )
            queries.upsertActiveSession(
                userId = account.id,
                accessToken = accessToken,
            )
            reloadUserAccountsFromSql()
        }
    }

    private suspend fun persistInMemoryCoreFlowsToSql() {
        sqlMutex.withLock {
            database.transaction {
                _articles.value.forEach { article ->
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
                    )
                }
                _events.value.forEachIndexed { index, evt ->
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
                        sortOrder = index.toLong(),
                    )
                }
                _payments.value.forEachIndexed { index, payment ->
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
                        sortOrder = index.toLong(),
                    )
                }
                _offlineQueue.value.forEach { mutation ->
                    queries.upsertOfflineMutation(
                        id = mutation.id,
                        actionLabel = mutation.actionLabel,
                        entityCode = mutation.entityCode,
                        timeAgo = mutation.timeAgo,
                        synced = if (mutation.synced) 1L else 0L,
                    )
                }
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
                    )
                }
            }
        }
    }

    private fun seedInitialDataSynchronouslyIfEmpty() {
        val existingArticles = queries.selectAllArticles().executeAsList()
        if (existingArticles.isEmpty()) {
            database.transaction {
                CanonicalSampleData.articles.forEach { art ->
                    queries.upsertArticle(
                        id = art.id,
                        code = art.code,
                        name = art.name,
                        category = art.category,
                        description = art.description,
                        pricePerDayDop = art.pricePerDayDop.toLong(),
                        totalStock = art.totalStock.toLong(),
                        availableOct23 = art.availableOct23.toLong(),
                        photoKey = art.photo.key,
                    )
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
                    )
                }
                CanonicalSampleData.offlineQueue.forEach { item ->
                    queries.upsertOfflineMutation(
                        id = item.id,
                        actionLabel = item.actionLabel,
                        entityCode = item.entityCode,
                        timeAgo = item.timeAgo,
                        synced = if (item.synced) 1L else 0L,
                    )
                }
                CanonicalSampleData.events.forEachIndexed { index, evt ->
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
                        sortOrder = index.toLong(),
                    )
                }
                CanonicalSampleData.payments.forEachIndexed { index, pay ->
                    queries.upsertPayment(
                        id = pay.id,
                        receiptNumber = pay.receiptNumber,
                        eventId = pay.eventId,
                        eventTitle = pay.eventTitle,
                        clientName = pay.clientName,
                        amountDop = pay.amountDop.toLong(),
                        methodLabel = pay.methodLabel,
                        kindLabel = pay.kindLabel,
                        dateLabel = pay.dateLabel,
                        referenceCode = pay.referenceCode,
                        verified = if (pay.verified) 1L else 0L,
                        sortOrder = index.toLong(),
                    )
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
                    )
                }
                CanonicalSampleData.initialChatMessages.forEach { msg ->
                    queries.insertChatMessage(msg)
                }
                JwtAuthManager.defaultSeedAccounts.forEach { account ->
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
                    )
                }
            }
        }
        val existingUsers = queries.selectAllUserAccounts().executeAsList()
        if (existingUsers.isEmpty()) {
            database.transaction {
                JwtAuthManager.defaultSeedAccounts.forEach { account ->
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
                    )
                }
            }
        }
        reloadFromSql()
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
        val matchedByUserId = userAccountsState.value.firstOrNull { account ->
            account.id == activeSession.userId
        }
        if (matchedByUserId != null) {
            _currentAuthenticatedUser.value = matchedByUserId
            _currentSessionJwt.value = activeSession.accessToken
            _currentUserEmail.value = matchedByUserId.email
            _activeRole.value = matchedByUserId.role
        }
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
                        queries.upsertUserAccount(
                            id = existing?.id ?: remote.id,
                            username = existing?.username?.ifEmpty { resolvedUsername } ?: resolvedUsername,
                            email = normalizedEmail,
                            fullName = remote.fullName.trim().ifEmpty { resolvedUsername },
                            passwordHash = existing?.passwordHash.orEmpty(),
                            salt = existing?.salt?.ifEmpty { remoteSalt } ?: remoteSalt,
                            birthDate = remote.birthDate.trim().ifEmpty { existing?.birthDate.orEmpty() },
                            phone = remote.phone.trim().ifEmpty { existing?.phone.orEmpty() },
                            role = resolvedRole,
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
                variants = currentInMemory?.variants ?: canonical?.variants ?: listOf("Estándar"),
                dimensions = currentInMemory?.dimensions ?: canonical?.dimensions ?: "Estándar",
                material = currentInMemory?.material ?: canonical?.material ?: "Resina y acabado premium",
                visibleInCatalog = currentInMemory?.visibleInCatalog ?: canonical?.visibleInCatalog ?: true,
            )
        }
    }

    private fun reloadChecklistFromSql() {
        val defaultEventId = _events.value.firstOrNull()?.id ?: "evt-boda-real"
        _checklist.value = queries.selectAllChecklistSteps().executeAsList().map { row ->
            val linkedEventId = checklistEventIdsByStepId[row.id] ?: defaultEventId
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
            val fallbackItems = currentInMemory?.items
                ?: itemsByEventId[row.id]
                ?: buildFallbackItems(row.title, row.guests.toInt())
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
                items = fallbackItems,
                priorChangesCount = row.priorChangesCount.toInt(),
                clientEmail = currentInMemory?.clientEmail.orEmpty(),
                cancellationReason = currentInMemory?.cancellationReason.orEmpty(),
                sketchJson = currentInMemory?.sketchJson.orEmpty(),
            )
        }
        if (loadedEvents.isNotEmpty()) {
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
            )
        }
        if (loadedPayments.isNotEmpty()) {
            _payments.value = loadedPayments
            recalculateMetricsFromState()
        }
    }

    private fun recalculateMetricsFromState() {
        _metrics.value = RosaFiestaBackendMappers.computeHonestMetrics(
            events = _events.value,
            payments = _payments.value,
            quotes = submodules.quotes.value,
            baseMetrics = CanonicalSampleData.businessMetrics,
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
                email = prev?.email.orEmpty(),
                phone = prev?.phone.orEmpty(),
                active = prev?.active ?: true,
            )
        }
        if (loadedStaff.isNotEmpty()) {
            _staffMembers.value = loadedStaff
        }
    }

    private fun reloadChatFromSql() {
        val loadedChat = queries.selectAllChatMessages().executeAsList()
        if (loadedChat.isNotEmpty()) {
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
    public suspend fun toggleChecklistStepAsync(stepId: String): Unit = withContext(ioDispatcher) {
        val cleanStepId = stepId.trim()
        val nowHm = DomainClockFormatter.currentTimeHmLabel()
        var staffName = ""
        var nextCompleted = false
        var resolvedEventId = ""
        sqlMutex.withLock {
            val current = queries.selectAllChecklistSteps().executeAsList()
                .firstOrNull { row -> row.id == cleanStepId } ?: return@withContext
            nextCompleted = current.completed == 0L
            staffName = current.completedBy
            resolvedEventId = checklistEventIdsByStepId[cleanStepId]
                ?: _checklist.value.firstOrNull { item -> item.id == cleanStepId }?.eventId
                ?: _events.value.firstOrNull()?.id.orEmpty()
            val nextTimeLabel = if (nextCompleted) nowHm else "Pendiente"
            queries.updateChecklistStepStatus(
                completed = if (nextCompleted) 1L else 0L,
                timeLabel = nextTimeLabel,
                id = cleanStepId,
            )
            reloadChecklistFromSql()
        }
        if (resolvedEventId.isNotEmpty()) {
            runCatching {
                apiClient?.toggleChecklistItem(
                    eventId = resolvedEventId,
                    itemId = cleanStepId,
                    input = ToggleChecklistInputDto(
                        completed = nextCompleted,
                        staffName = staffName,
                    ),
                )
            }
        }
    }

    /**
     * Marks all offline mutations as synchronized in SQLite and flushes the remote offline queue.
     */
    public suspend fun syncOfflineQueueAsync(): Unit = withContext(ioDispatcher) {
        runCatching {
            apiClient?.syncOfflineQueue()
        }
        sqlMutex.withLock {
            queries.markAllOfflineMutationsSynced()
            reloadOfflineQueueFromSql()
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
        runCatching {
            apiClient?.updateCatalogArticle(
                articleId = articleId,
                input = UpdateArticleInputDto(
                    totalStock = newStock.toLong(),
                    availableStock = newAvail.toLong(),
                ),
            )
        }
    }

    /**
     * Toggles [articleId] in the persisted favorites list off the main thread.
     */
    public suspend fun toggleFavoriteAsync(articleId: String): Unit = withContext(ioDispatcher) {
        val cleanId = articleId.trim()
        if (cleanId.isEmpty()) return@withContext
        sqlMutex.withLock {
            val currentFavs = queries.selectAllFavorites().executeAsList()
            if (cleanId in currentFavs) {
                queries.deleteFavorite(cleanId)
            } else {
                queries.insertFavorite(cleanId)
            }
            reloadFavoritesFromSql()
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
    ): Unit = withContext(ioDispatcher) {
        val cleanCode = code.trim()
        val cleanName = name.trim()
        val cleanCategory = category.trim().ifEmpty { "Mobiliario" }
        if (cleanCode.isEmpty()) return@withContext
        if (cleanName.isEmpty()) return@withContext
        if (pricePerDayDop <= 0) return@withContext
        if (totalStock < 0) return@withContext

        val articleId = "art-${cleanCode.lowercase()}"
        val description = "$cleanName para eventos y celebraciones en Santo Domingo."
        val photo = RosaFiestaBackendMappers.selectPhotoForArticle(cleanCategory, cleanName)
        sqlMutex.withLock {
            queries.upsertArticle(
                id = articleId,
                code = cleanCode,
                name = cleanName,
                category = cleanCategory,
                description = description,
                pricePerDayDop = pricePerDayDop.toLong(),
                totalStock = totalStock.toLong(),
                availableOct23 = totalStock.toLong(),
                photoKey = photo.key,
            )
            reloadArticlesFromSql()
        }
        runCatching {
            apiClient?.createCatalogArticle(
                CreateArticleInputDto(
                    id = articleId,
                    name = cleanName,
                    categoryId = cleanCategory,
                    photoKey = photo.key,
                    dailyPriceDop = pricePerDayDop.toLong(),
                    totalStock = totalStock.toLong(),
                    availableStock = totalStock.toLong(),
                    description = description,
                    visible = true,
                ),
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
    ): Unit = withContext(ioDispatcher) {
        val cleanTitle = title.trim()
        val cleanClient = clientName.trim()
        val cleanDate = dateLabel.trim()
        val cleanVenue = venue.trim()
        if (cleanTitle.isEmpty()) return@withContext
        if (cleanClient.isEmpty()) return@withContext
        if (cleanDate.isEmpty()) return@withContext
        if (cleanVenue.isEmpty()) return@withContext
        if (guests <= 0) return@withContext

        val proposal = RosaFiestaEngine.buildRosaProposal(cleanTitle, guests)
        val deliveryFeeDop = RosaFiestaBackendMappers.resolveDeliveryFeeDop(cleanVenue, businessSettings.value)
        val linesSubtotal = proposal.lines.sumOf { line -> line.subtotalDop }
        val consistentTotalDop = linesSubtotal + deliveryFeeDop
        val computedDaysUntil = DomainClockFormatter.computeDaysUntilEvent(cleanDate)
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
            queries.upsertEvent(
                id = resolvedEventId,
                code = code,
                title = cleanTitle,
                clientName = cleanClient,
                eventType = "Evento",
                dateLabel = cleanDate,
                daysUntilEvent = computedDaysUntil.toLong(),
                venue = cleanVenue,
                zone = "Santo Domingo DN",
                guests = guests.toLong(),
                articleCount = proposal.lines.sumOf { line -> line.quantity }.toLong(),
                totalDop = consistentTotalDop.toLong(),
                paidDepositDop = 0L,
                depositMethodLabel = "Pendiente de anticipo",
                depositDateLabel = "—",
                balanceDueDateLabel = cleanDate,
                status = EventStatus.PENDING_QUOTE.name,
                leadStaffName = assignedLead,
                photoKey = PhotoAsset.BODA.key,
                priorChangesCount = 0L,
                sortOrder = sortIndex.toLong(),
            )
            reloadEventsFromSql()
        }

        runCatching {
            val eventItems = proposal.lines.map { line ->
                EventItemDto(
                    articleCode = line.code,
                    name = line.name,
                    quantity = line.quantity.toLong(),
                    unitPriceRD = line.unitPriceDop.toLong(),
                )
            }
            apiClient?.createRemoteEvent(
                CreateEventInputDto(
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
                ),
            )
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
            val target = rows.firstOrNull { row -> row.id == eventId.trim() } ?: rows.first()
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
            runCatching {
                apiClient?.requestDateChange(
                    id = eventId,
                    input = DateChangeInputDto(
                        newDateLabel = cleanDate,
                        newDaysUntilEvent = computedDays,
                    ),
                )
            }
        }
        evalResult
    }

    /**
     * Cancels an event in SQLite off the main thread and returns its refund breakdown.
     */
    public suspend fun cancelEventAsync(eventId: String): CancellationRefund = withContext(ioDispatcher) {
        val refundResult = sqlMutex.withLock {
            val rows = queries.selectAllEvents().executeAsList()
            val target = rows.firstOrNull { row -> row.id == eventId.trim() } ?: rows.first()
            val refund = RosaFiestaEngine.calculateCancellationRefund(
                paidDepositDop = target.paidDepositDop.toInt(),
                daysUntilEvent = target.daysUntilEvent.toInt(),
            )
            queries.updateEventStatus(
                status = EventStatus.CANCELLED.name,
                id = target.id,
            )
            reloadEventsFromSql()
            refund
        }
        runCatching {
            apiClient?.cancelEvent(eventId)
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
    ): PaymentRecord? = withContext(ioDispatcher) {
        val cleanMethod = methodLabel.trim()
        val cleanRef = referenceCode.trim().ifEmpty { "REF-2026" }
        if (amountDop <= 0) return@withContext null
        if (cleanMethod.isEmpty()) return@withContext null

        val shortDateLabel = DomainClockFormatter.currentShortDateLabel()
        val fullDateLabel = DomainClockFormatter.currentFullDateLabel()
        val normalizedPayment = RosaFiestaBackendMappers.normalizePaymentMethod(cleanMethod)

        val createdRecord = sqlMutex.withLock {
            val eventRows = queries.selectAllEvents().executeAsList()
            val target = eventRows.firstOrNull { row -> row.id == eventId.trim() } ?: eventRows.first()
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
                )
            }
            val nextNumber = 185 + existingPayments.size
            val receipt = "REC-2026-${nextNumber.toString().padStart(4, '0')}"
            val balanceDop = (target.totalDop - target.paidDepositDop).toInt().coerceAtLeast(0)
            val record = PaymentRecord(
                id = "pay-${existingPayments.size + 1}",
                receiptNumber = receipt,
                eventId = target.id,
                eventTitle = target.title,
                clientName = target.clientName,
                amountDop = amountDop,
                methodLabel = "$cleanMethod · $shortDateLabel",
                kindLabel = if (amountDop >= balanceDop) "Liquidación" else "Abono",
                dateLabel = fullDateLabel,
                referenceCode = cleanRef,
                verified = true,
            )
            queries.upsertPayment(
                id = record.id,
                receiptNumber = record.receiptNumber,
                eventId = record.eventId,
                eventTitle = record.eventTitle,
                clientName = record.clientName,
                amountDop = record.amountDop.toLong(),
                methodLabel = record.methodLabel,
                kindLabel = record.kindLabel,
                dateLabel = record.dateLabel,
                referenceCode = record.referenceCode,
                verified = 1L,
                sortOrder = -(existingPayments.size + 1).toLong(),
            )
            val newPaid = (target.paidDepositDop.toInt() + amountDop).coerceAtMost(target.totalDop.toInt())
            queries.updateEventPaidDeposit(
                paidDepositDop = newPaid.toLong(),
                depositMethodLabel = cleanMethod,
                id = target.id,
            )
            reloadPaymentsFromSql()
            reloadEventsFromSql()
            record
        }
        runCatching {
            apiClient?.registerPayment(
                RegisterPaymentInputDto(
                    eventId = createdRecord.eventId,
                    eventTitle = createdRecord.eventTitle,
                    clientName = createdRecord.clientName,
                    amountRD = createdRecord.amountDop.toLong(),
                    method = normalizedPayment.canonicalMethod,
                    kind = createdRecord.kindLabel,
                    referenceCode = cleanRef,
                    bankName = normalizedPayment.bankName,
                ),
            )
        }
        createdRecord
    }

    override fun toggleChecklistStep(stepId: String) {
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
            toggleChecklistStepAsync(stepId)
        }
    }

    override fun syncOfflineQueue() {
        _offlineQueue.update { list ->
            list.map { item -> item.copy(synced = true) }
        }
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
                queries.upsertUserAccount(
                    id = newAccount.id,
                    username = newAccount.username,
                    email = newAccount.email,
                    fullName = newAccount.fullName,
                    passwordHash = newAccount.passwordHash,
                    salt = newAccount.salt,
                    birthDate = newAccount.birthDate,
                    phone = newAccount.phone,
                    role = newAccount.role,
                )
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
            ?: JwtAuthManager.defaultSeedAccounts.first()

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

        val updatedAccount = currentAccount.copy(
            fullName = cleanName,
            email = cleanEmail,
            phone = cleanPhone,
            birthDate = cleanBirthDate,
            username = cleanUsername,
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
        val token = JwtAuthManager.issueAccessToken(updatedAccount)
        _currentAuthenticatedUser.value = updatedAccount
        _currentSessionJwt.value = token
        _currentUserEmail.value = updatedAccount.email
        submodules.propagateSessionTokenToApi(token)
        scope.launch {
            sqlMutex.withLock {
                queries.upsertUserAccount(
                    id = updatedAccount.id,
                    username = updatedAccount.username,
                    email = updatedAccount.email,
                    fullName = updatedAccount.fullName,
                    passwordHash = updatedAccount.passwordHash,
                    salt = updatedAccount.salt,
                    birthDate = updatedAccount.birthDate,
                    phone = updatedAccount.phone,
                    role = updatedAccount.role,
                )
                queries.upsertActiveSession(
                    userId = updatedAccount.id,
                    accessToken = token,
                )
                reloadUserAccountsFromSql()
            }
            runCatching {
                apiClient?.updateMyProfile(
                    UpdateProfileInputDto(
                        fullName = cleanName,
                        email = cleanEmail,
                        phone = cleanPhone,
                        birthDate = cleanBirthDate,
                        username = cleanUsername,
                    ),
                )
            }
        }
        return AuthOutcome.authenticated(accessToken = token, account = updatedAccount)
    }

    override suspend fun requestPasswordOtp(email: String): OtpChallengeOutcome = submodules.requestPasswordOtp(email)

    override suspend fun verifyPasswordOtp(email: String, code: String): OtpChallengeOutcome = submodules.verifyPasswordOtp(email, code)

    override suspend fun resetPasswordWithOtp(
        email: String,
        newPassword: String,
        resetToken: String,
        otpCode: String,
    ): AuthOutcome = submodules.resetPasswordWithOtp(email, newPassword, resetToken, otpCode, ::resetUserPassword)

    override suspend fun loginWithOAuth(provider: String, idToken: String): AuthOutcome = submodules.loginWithOAuth(provider, idToken)

    override fun changeUserPassword(currentPassword: String, newPassword: String): AuthOutcome = submodules.changeUserPassword(currentPassword, newPassword)

    override fun logoutSession() {
        submodules.logoutSession(::clearSession)
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
                    queries.upsertUserAccount(
                        id = reconstructed.id,
                        username = reconstructed.username,
                        email = reconstructed.email,
                        fullName = reconstructed.fullName,
                        passwordHash = reconstructed.passwordHash,
                        salt = reconstructed.salt,
                        birthDate = reconstructed.birthDate,
                        phone = reconstructed.phone,
                        role = reconstructed.role,
                    )
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
        _currentSessionJwt.value = null
        apiClient?.setAccessToken(null)
        scope.launch {
            sqlMutex.withLock {
                queries.clearActiveSession()
            }
        }
    }

    override fun deleteCurrentAccount(): Boolean {
        val currentToken = _currentSessionJwt.value
        _currentSessionJwt.value = null
        _currentAuthenticatedUser.value = null
        _activeRole.value = "guest"
        apiClient?.setAccessToken(null)
        scope.launch {
            sqlMutex.withLock {
                queries.clearActiveSession()
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
        var updatedMember: StaffMember? = null
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
                    val updated = member.copy(
                        canViewPayments = nextCanView,
                        permissions = nextPerms,
                        roleLabel = nextLabel,
                    )
                    updatedMember = updated
                    updated
                } else {
                    member
                }
            }
        }
        val synced = updatedMember
        scope.launch {
            toggleStaffPaymentAccessAsync(target)
            if (synced != null) {
                if (submodules.hasRealServerToken()) {
                    runCatching {
                        apiClient?.updateStaffMember(
                            staffId = synced.id,
                            input = UpdateStaffInputDto(
                                roleLabel = synced.roleLabel,
                                permissions = synced.permissions.map { perm -> perm.name.lowercase() },
                            ),
                        )
                    }
                }
            }
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
        val cleanCode = code.trim()
        val cleanName = name.trim()
        val cleanCategory = category.trim().ifEmpty { "Mobiliario" }
        if (cleanCode.isEmpty()) return
        if (cleanName.isEmpty()) return
        if (pricePerDayDop <= 0) return
        if (totalStock < 0) return

        val photo = RosaFiestaBackendMappers.selectPhotoForArticle(cleanCategory, cleanName)
        val newArticle = CatalogArticle(
            id = "art-${cleanCode.lowercase()}",
            code = cleanCode,
            name = cleanName,
            category = cleanCategory,
            description = "$cleanName para eventos y celebraciones en Santo Domingo.",
            pricePerDayDop = pricePerDayDop,
            totalStock = totalStock,
            availableOct23 = totalStock,
            photo = photo,
            variants = listOf("Estándar"),
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
            createArticleAsync(cleanCode, cleanName, cleanCategory, pricePerDayDop, totalStock)
        }
    }

    override fun createEvent(
        title: String,
        clientName: String,
        dateLabel: String,
        venue: String,
        guests: Int,
    ) {
        val cleanTitle = title.trim()
        val cleanClient = clientName.trim()
        val cleanDate = dateLabel.trim()
        val cleanVenue = venue.trim()
        if (cleanTitle.isEmpty()) return
        if (cleanClient.isEmpty()) return
        if (cleanDate.isEmpty()) return
        if (cleanVenue.isEmpty()) return
        if (guests <= 0) return

        val lines = buildFallbackItems(cleanTitle, guests)
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
        )
        _events.update { existing -> existing + newEvent }
        recalculateMetricsFromState()
        scope.launch {
            createEventAsync(cleanTitle, cleanClient, cleanDate, cleanVenue, guests, preassignedEventId = eventId)
        }
    }

    override fun changeEventDate(eventId: String, newDateLabel: String): DateChangeResult {
        val cleanDate = newDateLabel.trim()
        val currentEvent = _events.value.firstOrNull { event -> event.id == eventId.trim() } ?: _events.value.first()
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

    override fun cancelEvent(eventId: String): CancellationRefund {
        val currentEvent = _events.value.firstOrNull { event -> event.id == eventId.trim() } ?: _events.value.first()
        val refund = RosaFiestaEngine.calculateCancellationRefund(currentEvent.paidDepositDop, currentEvent.daysUntilEvent)
        _events.update { list ->
            list.map { evt ->
                if (evt.id == currentEvent.id) {
                    evt.copy(status = EventStatus.CANCELLED)
                } else {
                    evt
                }
            }
        }
        recalculateMetricsFromState()
        scope.launch {
            cancelEventAsync(currentEvent.id)
        }
        return refund
    }

    override fun registerPayment(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
    ): PaymentRecord? {
        val cleanMethod = methodLabel.trim()
        val cleanRef = referenceCode.trim().ifEmpty { "REF-2026" }
        if (amountDop <= 0) return null
        if (cleanMethod.isEmpty()) return null

        val targetEvent = _events.value.firstOrNull { event -> event.id == eventId.trim() } ?: _events.value.first()
        val nextNumber = 185 + _payments.value.size
        val receipt = "REC-2026-${nextNumber.toString().padStart(4, '0')}"
        val shortDateLabel = DomainClockFormatter.currentShortDateLabel()
        val fullDateLabel = DomainClockFormatter.currentFullDateLabel()
        val record = PaymentRecord(
            id = "pay-${_payments.value.size + 1}",
            receiptNumber = receipt,
            eventId = targetEvent.id,
            eventTitle = targetEvent.title,
            clientName = targetEvent.clientName,
            amountDop = amountDop,
            methodLabel = "$cleanMethod · $shortDateLabel",
            kindLabel = if (amountDop >= targetEvent.balanceDop) "Liquidación" else "Abono",
            dateLabel = fullDateLabel,
            referenceCode = cleanRef,
            verified = true,
        )
        _payments.update { existing -> listOf(record) + existing }
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
        scope.launch {
            registerPaymentAsync(targetEvent.id, amountDop, cleanMethod, cleanRef)
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

    override fun replyToReview(reviewId: String, adminReply: String): ReviewRecord? = submodules.replyToReview(reviewId, adminReply)

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
    ): ClientEvent? = submodules.updateEventDetails(
        eventId = eventId,
        title = title,
        venue = venue,
        zone = zone,
        guests = guests,
        status = status,
        leadStaffName = leadStaffName,
        sketchJson = sketchJson,
    )

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
}
