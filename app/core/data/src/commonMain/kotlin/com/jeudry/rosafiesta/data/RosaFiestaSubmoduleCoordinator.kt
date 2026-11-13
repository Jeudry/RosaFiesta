package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.core.engine.UserRole
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.ArticleAvailabilityDay
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
import com.jeudry.rosafiesta.domain.ReportSeriesPoint
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.RosaChatReply
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.StaffProfile
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class RosaFiestaSubmoduleCoordinator(
    private val apiClient: RosaFiestaApiClient?,
    private val ioDispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
    private val articlesFlow: MutableStateFlow<List<CatalogArticle>>,
    private val eventsFlow: MutableStateFlow<List<ClientEvent>>,
    private val paymentsFlow: MutableStateFlow<List<PaymentRecord>>,
    private val offlineQueueFlow: MutableStateFlow<List<OfflineQueueItem>>,
    private val metricsFlow: MutableStateFlow<BusinessMetrics>,
    private val staffFlow: MutableStateFlow<List<StaffMember>>,
    private val chatMessagesFlow: MutableStateFlow<List<String>>,
    private val activeRoleFlow: MutableStateFlow<String>,
    private val currentUserEmailFlow: MutableStateFlow<String>,
    private val currentSessionJwtFlow: MutableStateFlow<String?>,
    private val currentAuthenticatedUserFlow: MutableStateFlow<RegisteredUserAccount?>,
    private val userAccountsFlow: MutableStateFlow<List<RegisteredUserAccount>>,
    private val onPersistAccountAndSession: suspend (RegisteredUserAccount, String) -> Unit = { _, _ -> },
    private val onPersistCoreSync: suspend () -> Unit = {},
    private val seedDemoData: Boolean = true,
) {
    private val _packages = MutableStateFlow(if (seedDemoData) CanonicalSampleData.packages else emptyList())
    val packages: StateFlow<List<EventPackage>> = _packages.asStateFlow()

    private val _templates = MutableStateFlow(if (seedDemoData) CanonicalSampleData.templates else emptyList())
    val templates: StateFlow<List<EventTypeTemplate>> = _templates.asStateFlow()

    private val _balloonProducts = MutableStateFlow(if (seedDemoData) CanonicalSampleData.balloonProducts else emptyList())
    val balloonProducts: StateFlow<List<BalloonProduct>> = _balloonProducts.asStateFlow()

    private val _clients = MutableStateFlow(if (seedDemoData) CanonicalSampleData.clients else emptyList())
    val clients: StateFlow<List<ClientProfile>> = _clients.asStateFlow()

    private val _deliveryStops = MutableStateFlow(if (seedDemoData) CanonicalSampleData.deliveryStops else emptyList())
    val deliveryStops: StateFlow<List<DeliveryStop>> = _deliveryStops.asStateFlow()

    private val _damageIncidents = MutableStateFlow(if (seedDemoData) CanonicalSampleData.damageIncidents else emptyList())
    val damageIncidents: StateFlow<List<DamageIncident>> = _damageIncidents.asStateFlow()

    private val _quotes = MutableStateFlow(if (seedDemoData) CanonicalSampleData.quotes else emptyList())
    val quotes: StateFlow<List<QuoteRecord>> = _quotes.asStateFlow()

    private val _refundRequests = MutableStateFlow(if (seedDemoData) CanonicalSampleData.refundRequests else emptyList())
    val refundRequests: StateFlow<List<RefundRequest>> = _refundRequests.asStateFlow()

    private val _reviews = MutableStateFlow(if (seedDemoData) CanonicalSampleData.reviews else emptyList())
    val reviews: StateFlow<List<ReviewRecord>> = _reviews.asStateFlow()

    private val _businessSettings = MutableStateFlow(
        if (seedDemoData) CanonicalSampleData.businessSettings else CanonicalSampleData.emptyBusinessSettings,
    )
    val businessSettings: StateFlow<BusinessSettings> = _businessSettings.asStateFlow()

    private val _suppliers = MutableStateFlow(if (seedDemoData) CanonicalSampleData.suppliers else emptyList())
    val suppliers: StateFlow<List<SupplierRecord>> = _suppliers.asStateFlow()

    private val _leads = MutableStateFlow(if (seedDemoData) CanonicalSampleData.leads else emptyList())
    val leads: StateFlow<List<LeadRecord>> = _leads.asStateFlow()

    private val _chatThreads = MutableStateFlow(if (seedDemoData) CanonicalSampleData.chatThreads else emptyList())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()

    private val _threadMessages = MutableStateFlow(if (seedDemoData) CanonicalSampleData.chatMessageRecords else emptyList())
    val threadMessages: StateFlow<List<ChatMessageRecord>> = _threadMessages.asStateFlow()

    private val _eventGuests = MutableStateFlow<List<EventGuest>>(emptyList())
    val eventGuests: StateFlow<List<EventGuest>> = _eventGuests.asStateFlow()

    private val _eventSchedule = MutableStateFlow(if (seedDemoData) CanonicalSampleData.eventScheduleBlocks else emptyList())
    val eventSchedule: StateFlow<List<EventScheduleBlock>> = _eventSchedule.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationRecord>>(emptyList())
    val notifications: StateFlow<List<NotificationRecord>> = _notifications.asStateFlow()

    private val _insurancePolicies = MutableStateFlow(if (seedDemoData) CanonicalSampleData.insurancePolicies else emptyList())
    val insurancePolicies: StateFlow<List<InsurancePolicyRecord>> = _insurancePolicies.asStateFlow()

    private val _auditLogs = MutableStateFlow(if (seedDemoData) CanonicalSampleData.auditLogs else emptyList())
    val auditLogs: StateFlow<List<AuditLogRecord>> = _auditLogs.asStateFlow()

    private val _botFaqs = MutableStateFlow(if (seedDemoData) CanonicalSampleData.botFaqs else emptyList())
    val botFaqs: StateFlow<List<BotFaqRecord>> = _botFaqs.asStateFlow()

    private val _botKeywords = MutableStateFlow(if (seedDemoData) CanonicalSampleData.botKeywords else emptyList())
    val botKeywords: StateFlow<List<BotKeywordRecord>> = _botKeywords.asStateFlow()

    private val _botThreads = MutableStateFlow(if (seedDemoData) CanonicalSampleData.botThreads else emptyList())
    val botThreads: StateFlow<List<BotThreadRecord>> = _botThreads.asStateFlow()

    private val localOtpCodesByEmail = mutableMapOf<String, String>()

    fun propagateSessionTokenToApi(token: String?) {
        val client = apiClient ?: return
        if (token == null) {
            client.setAccessToken(null)
            return
        }
        val clean = token.trim()
        val isLocalOfflineEnvelope = clean.startsWith("rf-local.")
        if (!isLocalOfflineEnvelope) {
            client.setAccessToken(clean)
            return
        }
        val existingClientToken = client.currentAccessToken()
        if (existingClientToken == null) {
            client.setAccessToken(clean)
        }
    }

    suspend fun syncFromBackendAsync(): Boolean = withContext(ioDispatcher) {
        val client = apiClient ?: return@withContext false
        var anySucceeded = false
        var coreUpdated = false

        runCatching {
            val response = client.listArticles()
            val remoteArticles = response.articles
            if (remoteArticles != null) {
                val fallbackById = articlesFlow.value.associateBy { article -> article.id }
                val rentalDtos = remoteArticles.filter { dto -> dto.isDirectSale != true }
                articlesFlow.value = rentalDtos.mapIndexed { index, dto ->
                    RosaFiestaBackendMappers.toCatalogArticle(dto, index, fallbackById)
                }
                coreUpdated = true
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.listBalloonProducts()
            val remoteBalloons = response.articles
            if (remoteBalloons != null) {
                val fallbackById = _balloonProducts.value.associateBy { product -> product.id }
                _balloonProducts.value = remoteBalloons.mapIndexed { index, dto ->
                    RosaFiestaBackendMappers.toBalloonProduct(dto, index, fallbackById)
                }
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.listPackages()
            val remotePackages = response.packages
            if (remotePackages != null) {
                val fallbackById = _packages.value.associateBy { bundle -> bundle.id }
                _packages.value = remotePackages.map { dto ->
                    RosaFiestaBackendMappers.toEventPackage(dto, fallbackById)
                }
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.listEventTemplates()
            val remoteTemplates = response.templates
            if (remoteTemplates != null) {
                _templates.value = remoteTemplates.map { dto ->
                    RosaFiestaBackendMappers.toEventTemplate(dto)
                }
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.getPublicSettings()
            _businessSettings.value = RosaFiestaBackendMappers.fromPublicSettings(
                dto = response.settings,
                fallback = _businessSettings.value,
            )
            anySucceeded = true
        }

        runCatching {
            val response = client.listCategories()
            val remoteCategories = response.categories
            if (remoteCategories != null) {
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.listReviews()
            val remoteReviews = response.reviews
            if (remoteReviews != null) {
                val eventsById = eventsFlow.value.associateBy { event -> event.id }
                val fallbackById = _reviews.value.associateBy { review -> review.id }
                _reviews.value = remoteReviews.map { dto ->
                    RosaFiestaBackendMappers.toReviewRecord(dto, eventsById, fallbackById)
                }
                anySucceeded = true
            }
        }

        if (hasRealServerToken(client)) {
            runCatching {
                val response = client.listEvents()
                val remoteEvents = response.events
                if (remoteEvents != null) {
                    val fallbackById = eventsFlow.value.associateBy { event -> event.id }
                    eventsFlow.value = remoteEvents.map { dto ->
                        RosaFiestaBackendMappers.toClientEvent(dto, fallbackById)
                    }
                    coreUpdated = true
                    anySucceeded = true
                }
            }

            syncEventGuestsAndSchedule(client)

            runCatching {
                val response = client.listQuotes()
                val remoteQuotes = response.quotes
                if (remoteQuotes != null) {
                    _quotes.value = remoteQuotes.map { dto ->
                        RosaFiestaBackendMappers.toQuoteRecord(dto)
                    }
                    anySucceeded = true
                }
            }

            runCatching {
                val response = client.listPayments()
                val remotePayments = response.payments
                if (remotePayments != null) {
                    paymentsFlow.value = remotePayments.map { dto ->
                        RosaFiestaBackendMappers.toPaymentRecord(dto)
                    }
                    coreUpdated = true
                    anySucceeded = true
                }
            }

            runCatching {
                val response = client.listChatThreads()
                val remoteThreads = response.threads
                if (remoteThreads != null) {
                    _chatThreads.value = remoteThreads.map { dto ->
                        RosaFiestaBackendMappers.toChatThread(dto)
                    }
                    anySucceeded = true
                }
            }

            syncThreadMessagesFromBackend(client)

            runCatching {
                val response = client.listNotifications()
                val remoteNotifications = response.notifications
                if (remoteNotifications != null) {
                    _notifications.value = remoteNotifications.map { dto ->
                        RosaFiestaBackendMappers.toNotificationRecord(dto)
                    }
                    anySucceeded = true
                }
            }

            val role = activeRoleFlow.value.trim().lowercase()
            val isAdmin = role == "admin"
            val isStaff = role.startsWith("staff")
            val canViewOperations = if (isAdmin) true else isStaff
            val isRestrictedYuli = role == "staff_yuli"
            val yuliHasPaymentOverride = staffFlow.value.any { member ->
                val isYuli = member.name.equals("Yuli", ignoreCase = true)
                if (isYuli) member.canViewPayments else false
            }
            val canViewFinance = when {
                isAdmin -> true
                !isStaff -> false
                !isRestrictedYuli -> true
                else -> yuliHasPaymentOverride
            }

            var syncedRemoteSummary = false
            if (canViewFinance) {
                runCatching {
                    val summaryOutput = client.getFinanceSummary()
                    metricsFlow.value = RosaFiestaBackendMappers.toBusinessMetrics(
                        summary = summaryOutput.summary,
                        occupancyRatePercent = metricsFlow.value.occupancyRatePercent,
                    )
                    syncedRemoteSummary = true
                    anySucceeded = true
                }
                runCatching {
                    val refundsOutput = client.listRefunds()
                    val remoteRefunds = refundsOutput.refunds
                    if (remoteRefunds != null) {
                        val fallbackById = _refundRequests.value.associateBy { request -> request.id }
                        _refundRequests.value = remoteRefunds.map { dto ->
                            RosaFiestaBackendMappers.toRefundRequest(dto, fallbackById)
                        }
                        anySucceeded = true
                    }
                }
            }

            if (canViewOperations) {
                runCatching {
                    val clientsOutput = client.listClients()
                    val remoteClients = clientsOutput.clients
                    if (remoteClients != null) {
                        val fallbackById = _clients.value.associateBy { profile -> profile.id }
                        _clients.value = remoteClients.map { dto ->
                            RosaFiestaBackendMappers.toClientProfile(dto, fallbackById)
                        }
                        anySucceeded = true
                    }
                }
                runCatching {
                    val staffOutput = client.listStaff()
                    val remoteStaff = staffOutput.staff
                    if (remoteStaff != null) {
                        staffFlow.value = remoteStaff.map { dto ->
                            RosaFiestaBackendMappers.toStaffProfile(dto, eventsFlow.value)
                        }
                        coreUpdated = true
                        anySucceeded = true
                    }
                }
                runCatching {
                    val routesOutput = client.listTodayRoutes()
                    val remoteStops = routesOutput.stops
                    if (remoteStops != null) {
                        _deliveryStops.value = remoteStops.map { dto ->
                            RosaFiestaBackendMappers.toDeliveryStop(dto)
                        }
                        anySucceeded = true
                    }
                }
                runCatching {
                    val queueOutput = client.listOfflineQueue()
                    val remoteQueue = queueOutput.queue
                    if (remoteQueue != null) {
                        offlineQueueFlow.value = remoteQueue.map { dto ->
                            RosaFiestaBackendMappers.toOfflineQueueItem(dto)
                        }
                        coreUpdated = true
                        anySucceeded = true
                    }
                }
                runCatching {
                    val damagesOutput = client.listDamages()
                    val remoteDamages = damagesOutput.damages
                    if (remoteDamages != null) {
                        val eventsById = eventsFlow.value.associateBy { event -> event.id }
                        _damageIncidents.value = remoteDamages.map { dto ->
                            RosaFiestaBackendMappers.toDamageIncident(dto, eventsById)
                        }
                        anySucceeded = true
                    }
                }
            }

            if (isAdmin) {
                runCatching {
                    val adminSettings = client.getAdminSettings()
                    _businessSettings.value = RosaFiestaBackendMappers.fromAdminSettings(
                        output = adminSettings,
                        fallback = _businessSettings.value,
                    )
                    anySucceeded = true
                }
                runCatching {
                    val suppliersOutput = client.listSuppliers()
                    val remoteSuppliers = suppliersOutput.suppliers
                    if (remoteSuppliers != null) {
                        _suppliers.value = remoteSuppliers.map { dto ->
                            RosaFiestaBackendMappers.toSupplierRecord(dto)
                        }
                    }
                }
                runCatching {
                    val leadsOutput = client.listLeads()
                    val remoteLeads = leadsOutput.leads
                    if (remoteLeads != null) {
                        _leads.value = remoteLeads.map { dto ->
                            RosaFiestaBackendMappers.toLeadRecord(dto)
                        }
                    }
                }
                runCatching {
                    val insuranceOutput = client.listInsurancePolicies()
                    val remotePolicies = insuranceOutput.policies
                    if (remotePolicies != null) {
                        _insurancePolicies.value = remotePolicies.map { dto ->
                            RosaFiestaBackendMappers.toInsurancePolicyRecord(dto)
                        }
                    }
                }
                runCatching {
                    val logsOutput = client.listAuditLogs()
                    val remoteLogs = logsOutput.logs
                    if (remoteLogs != null) {
                        _auditLogs.value = remoteLogs.map { dto ->
                            RosaFiestaBackendMappers.toAuditLogRecord(dto)
                        }
                    }
                }
                runCatching {
                    val faqsOutput = client.listBotFaqs()
                    val remoteFaqs = faqsOutput.faqs
                    if (remoteFaqs != null) {
                        _botFaqs.value = remoteFaqs.map { dto ->
                            RosaFiestaBackendMappers.toBotFaqRecord(dto)
                        }
                    }
                }
                runCatching {
                    val keywordsOutput = client.listBotKeywords()
                    val remoteKeywords = keywordsOutput.keywords
                    if (remoteKeywords != null) {
                        _botKeywords.value = remoteKeywords.map { dto ->
                            RosaFiestaBackendMappers.toBotKeywordRecord(dto)
                        }
                    }
                }
                runCatching {
                    val botThreadsOutput = client.listBotThreads()
                    val remoteBotThreads = botThreadsOutput.threads
                    if (remoteBotThreads != null) {
                        _botThreads.value = remoteBotThreads.map { dto ->
                            RosaFiestaBackendMappers.toBotThreadRecord(dto)
                        }
                    }
                }
            }

            if (!syncedRemoteSummary) {
                if (coreUpdated) {
                    recomputeHonestMetrics()
                }
            }
        }

        if (coreUpdated) {
            onPersistCoreSync()
        }
        anySucceeded
    }

    fun hasRealServerToken(client: RosaFiestaApiClient? = apiClient): Boolean {
        val sessionToken = currentSessionJwtFlow.value?.trim().orEmpty()
        val apiToken = client?.currentAccessToken()?.trim().orEmpty()
        val activeToken = sessionToken.ifEmpty { apiToken }
        if (activeToken.isEmpty()) {
            return false
        }
        return !activeToken.startsWith("rf-local.")
    }

    private suspend fun syncEventGuestsAndSchedule(client: RosaFiestaApiClient) {
        val currentEvents = eventsFlow.value
        if (currentEvents.isEmpty()) {
            _eventGuests.value = emptyList()
            _eventSchedule.value = emptyList()
            return
        }
        val syncedGuests = mutableListOf<EventGuest>()
        val syncedSchedule = mutableListOf<EventScheduleBlock>()
        var anyGuestsCallSucceeded = false
        var anyScheduleCallSucceeded = false
        currentEvents.forEach { eventItem ->
            runCatching {
                val guestsResp = client.listEventGuests(eventItem.id)
                anyGuestsCallSucceeded = true
                guestsResp.guests?.forEach { dto ->
                    syncedGuests.add(RosaFiestaBackendMappers.toEventGuest(dto))
                }
            }
            runCatching {
                val scheduleResp = client.listEventSchedule(eventItem.id)
                anyScheduleCallSucceeded = true
                scheduleResp.blocks?.forEach { dto ->
                    syncedSchedule.add(RosaFiestaBackendMappers.toEventScheduleBlock(dto))
                }
            }
        }
        if (anyGuestsCallSucceeded) {
            _eventGuests.value = syncedGuests
        }
        if (anyScheduleCallSucceeded) {
            _eventSchedule.value = syncedSchedule
        }
    }

    private suspend fun syncThreadMessagesFromBackend(client: RosaFiestaApiClient) {
        val currentThreads = _chatThreads.value
        if (currentThreads.isEmpty()) return
        val syncedMessages = mutableListOf<ChatMessageRecord>()
        currentThreads.forEach { thread ->
            runCatching {
                val resp = client.listThreadMessages(thread.id)
                resp.messages?.forEach { dto ->
                    syncedMessages.add(
                        RosaFiestaBackendMappers.toChatMessageRecord(
                            dto = dto,
                            contactName = thread.contactName,
                        ),
                    )
                }
            }
        }
        if (syncedMessages.isNotEmpty()) {
            _threadMessages.value = syncedMessages
        }
    }

    fun recomputeHonestMetrics() {
        metricsFlow.value = RosaFiestaBackendMappers.computeHonestMetrics(
            events = eventsFlow.value,
            payments = paymentsFlow.value,
            quotes = _quotes.value,
            baseMetrics = metricsFlow.value,
        )
    }

    suspend fun authenticateUserOnline(
        identifier: String,
        password: String,
        localFallback: (String, String) -> AuthOutcome,
    ): AuthOutcome = withContext(ioDispatcher) {
        val cleanIdentifier = identifier.trim().lowercase()
        val cleanPassword = password.trim()
        val identifierError = InputValidator.validateLoginIdentifier(cleanIdentifier)
        if (identifierError != null) {
            return@withContext AuthOutcome.failure("AUTH_INVALID_CREDENTIALS", identifierError)
        }
        val passwordError = InputValidator.validatePassword(cleanPassword)
        if (passwordError != null) {
            return@withContext AuthOutcome.failure("AUTH_INVALID_CREDENTIALS", passwordError)
        }

        val client = apiClient ?: return@withContext localFallback(identifier, password)
        val matchedLocal = userAccountsFlow.value.firstOrNull { account ->
            val byEmail = account.email.equals(cleanIdentifier, ignoreCase = true)
            val byUsername = account.username.equals(cleanIdentifier, ignoreCase = true)
            if (byEmail) true else byUsername
        }
        val resolvedEmail = when {
            matchedLocal != null -> matchedLocal.email
            cleanIdentifier.contains("@") -> cleanIdentifier
            else -> "$cleanIdentifier@rosafiesta.do"
        }

        val remoteResult = try {
            client.login(LoginInputDto(email = resolvedEmail, password = cleanPassword))
        } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            return@withContext mapOnlineAuthException(error)
        }

        val remoteUser = remoteResult.user
        val salt = JwtAuthManager.generateDeterministicSalt("${remoteUser.email}:${remoteUser.username}")
        val verifiedHash = JwtAuthManager.hashPassword(cleanPassword, salt)
        val syncedAccount = RegisteredUserAccount(
            id = remoteUser.id,
            username = remoteUser.username.ifBlank { remoteUser.email.substringBefore("@") },
            email = remoteUser.email.trim().lowercase(),
            fullName = remoteUser.fullName,
            passwordHash = verifiedHash,
            salt = salt,
            birthDate = remoteUser.birthDate,
            phone = remoteUser.phone,
            role = remoteUser.role.ifBlank { "client" },
        )
        upsertAccountInMemory(syncedAccount)
        applyAuthenticatedSession(syncedAccount, remoteResult.accessToken)
        onPersistAccountAndSession(syncedAccount, remoteResult.accessToken)
        runCatching { syncFromBackendAsync() }
        AuthOutcome.authenticated(
            accessToken = remoteResult.accessToken,
            account = syncedAccount,
        )
    }

    suspend fun registerUserOnline(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        phone: String,
        localFallback: () -> AuthOutcome,
    ): AuthOutcome = withContext(ioDispatcher) {
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
            return@withContext AuthOutcome.failure("AUTH_MISSING_FIELDS", fullNameError)
        }
        val usernameError = InputValidator.validateUsername(resolvedUsername)
        if (usernameError != null) {
            return@withContext AuthOutcome.failure("AUTH_MISSING_FIELDS", usernameError)
        }
        val emailError = InputValidator.validateEmail(cleanEmail)
        if (emailError != null) {
            return@withContext AuthOutcome.failure("AUTH_MISSING_FIELDS", emailError)
        }
        val birthDateError = InputValidator.validateBirthDate(cleanBirthDate, isRequired = true)
        if (birthDateError != null) {
            return@withContext AuthOutcome.failure("AUTH_MISSING_FIELDS", birthDateError)
        }
        val passwordError = InputValidator.validatePassword(cleanPassword)
        if (passwordError != null) {
            return@withContext AuthOutcome.failure("AUTH_WEAK_PASSWORD", passwordError)
        }

        val client = apiClient ?: return@withContext localFallback()
        val remoteResult = try {
            client.register(
                RegisterInputDto(
                    username = resolvedUsername,
                    fullName = fullName,
                    email = cleanEmail,
                    password = cleanPassword,
                    birthDate = cleanBirthDate,
                    phone = cleanPhone,
                ),
            )
        } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            return@withContext mapOnlineAuthException(error)
        }

        val remoteUser = remoteResult.user
        val salt = JwtAuthManager.generateDeterministicSalt("${remoteUser.email}:${remoteUser.username}")
        val verifiedHash = JwtAuthManager.hashPassword(cleanPassword, salt)
        val syncedAccount = RegisteredUserAccount(
            id = remoteUser.id,
            username = remoteUser.username.ifBlank { resolvedUsername },
            email = remoteUser.email.trim().lowercase(),
            fullName = remoteUser.fullName.ifBlank { fullName },
            passwordHash = verifiedHash,
            salt = salt,
            birthDate = remoteUser.birthDate.ifBlank { cleanBirthDate },
            phone = remoteUser.phone.ifBlank { cleanPhone },
            role = remoteUser.role.ifBlank { "client" },
        )
        upsertAccountInMemory(syncedAccount)
        applyAuthenticatedSession(syncedAccount, remoteResult.accessToken)
        onPersistAccountAndSession(syncedAccount, remoteResult.accessToken)
        runCatching { syncFromBackendAsync() }
        AuthOutcome.authenticated(
            accessToken = remoteResult.accessToken,
            account = syncedAccount,
        )
    }

    private fun mapOnlineAuthException(error: Throwable): AuthOutcome {
        if (error is RosaFiestaApiException) {
            val translated = SensitiveDataRedactor.translateBusinessError(error.errorCode, error.message)
            return AuthOutcome.failure(
                errorCode = error.errorCode,
                errorMessage = translated,
            )
        }
        return AuthOutcome.failure(
            errorCode = "NETWORK_ERROR",
            errorMessage = "No pudimos conectar con el servidor de RosaFiesta. Verifica que el backend esté activo e inténtalo de nuevo.",
        )
    }

    suspend fun requestPasswordOtp(email: String): OtpChallengeOutcome = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        val emailError = InputValidator.validateEmail(cleanEmail)
        if (emailError != null) {
            return@withContext OtpChallengeOutcome(
                success = false,
                errorCode = "AUTH_INVALID_EMAIL",
                errorMessage = emailError,
            )
        }
        val remote = runCatching {
            apiClient?.requestOtp(cleanEmail)
        }.getOrNull()
        if (remote != null) {
            return@withContext OtpChallengeOutcome(
                success = remote.sent,
                expiresInSeconds = remote.expiresInSeconds.toInt(),
                devCode = remote.devCode.orEmpty(),
            )
        }
        val generatedCode = "246810"
        localOtpCodesByEmail[cleanEmail] = generatedCode
        OtpChallengeOutcome(
            success = true,
            expiresInSeconds = 600,
            devCode = generatedCode,
        )
    }

    suspend fun verifyPasswordOtp(email: String, code: String): OtpChallengeOutcome = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        val cleanCode = code.trim()
        if (cleanEmail.isEmpty()) {
            return@withContext OtpChallengeOutcome(
                success = false,
                errorCode = "AUTH_INVALID_OTP",
                errorMessage = "Ingresa tu correo electrónico.",
            )
        }
        if (cleanCode.length < 4) {
            return@withContext OtpChallengeOutcome(
                success = false,
                errorCode = "AUTH_INVALID_OTP",
                errorMessage = "Ingresa el código de verificación.",
            )
        }
        val remote = runCatching {
            apiClient?.verifyOtp(cleanEmail, cleanCode)
        }.getOrNull()
        if (remote != null) {
            return@withContext OtpChallengeOutcome(
                success = remote.verified,
                expiresInSeconds = remote.expiresInSeconds.toInt(),
                resetToken = remote.resetToken,
            )
        }
        val expectedLocalCode = localOtpCodesByEmail[cleanEmail]
        val matchesLocalCode = if (expectedLocalCode != null) {
            expectedLocalCode == cleanCode
        } else {
            cleanCode.length == 6
        }
        if (!matchesLocalCode) {
            return@withContext OtpChallengeOutcome(
                success = false,
                errorCode = "AUTH_INVALID_OTP",
                errorMessage = "El código ingresado no es válido.",
            )
        }
        OtpChallengeOutcome(
            success = true,
            expiresInSeconds = 600,
            resetToken = "local-reset-$cleanEmail",
        )
    }

    suspend fun resetPasswordWithOtp(
        email: String,
        newPassword: String,
        resetToken: String,
        otpCode: String,
        localResetFallback: (String, String) -> AuthOutcome,
    ): AuthOutcome = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = newPassword.trim()
        val passwordError = InputValidator.validatePassword(cleanPassword)
        if (passwordError != null) {
            return@withContext AuthOutcome.failure("AUTH_WEAK_PASSWORD", passwordError)
        }
        val remote = runCatching {
            apiClient?.resetPassword(
                ResetPasswordInputDto(
                    email = cleanEmail,
                    identifier = cleanEmail,
                    newPassword = cleanPassword,
                    resetToken = resetToken.ifBlank { null },
                    otpCode = otpCode.ifBlank { null },
                ),
            )
        }.getOrNull()
        if (remote != null) {
            val user = remote.user
            val salt = JwtAuthManager.generateDeterministicSalt("${user.email}:reset")
            val nextHash = JwtAuthManager.hashPassword(cleanPassword, salt)
            val updated = RegisteredUserAccount(
                id = user.id,
                username = user.username.ifBlank { user.email.substringBefore("@") },
                email = user.email.trim().lowercase(),
                fullName = user.fullName,
                passwordHash = nextHash,
                salt = salt,
                birthDate = user.birthDate,
                phone = user.phone,
                role = user.role.ifBlank { "client" },
            )
            upsertAccountInMemory(updated)
            applyAuthenticatedSession(updated, remote.accessToken)
            onPersistAccountAndSession(updated, remote.accessToken)
            return@withContext AuthOutcome.authenticated(remote.accessToken, updated)
        }
        localResetFallback(cleanEmail, cleanPassword)
    }

    suspend fun loginWithOAuth(provider: String, idToken: String): AuthOutcome = withContext(ioDispatcher) {
        val cleanProvider = provider.trim().lowercase()
        val cleanIdToken = idToken.trim()
        if (cleanProvider.isEmpty()) {
            return@withContext AuthOutcome.failure("AUTH_OAUTH_INVALID", "Selecciona un proveedor válido.")
        }
        if (cleanIdToken.isEmpty()) {
            return@withContext AuthOutcome.failure("AUTH_OAUTH_INVALID", "Token de identidad vacío.")
        }
        val remote = runCatching {
            apiClient?.loginWithOAuth(cleanProvider, cleanIdToken)
        }.getOrNull()
        if (remote != null) {
            val user = remote.user
            val salt = JwtAuthManager.generateDeterministicSalt("${user.email}:oauth")
            val account = RegisteredUserAccount(
                id = user.id,
                username = user.username.ifBlank { user.email.substringBefore("@") },
                email = user.email.trim().lowercase(),
                fullName = user.fullName,
                passwordHash = "",
                salt = salt,
                birthDate = user.birthDate,
                phone = user.phone,
                role = "client",
            )
            upsertAccountInMemory(account)
            applyAuthenticatedSession(account, remote.accessToken)
            onPersistAccountAndSession(account, remote.accessToken)
            return@withContext AuthOutcome.authenticated(remote.accessToken, account)
        }
        val fallbackEmail = if (cleanIdToken.contains("@")) {
            cleanIdToken.lowercase()
        } else {
            "$cleanProvider.cliente@rosafiesta.do"
        }
        val username = fallbackEmail.substringBefore("@")
        val salt = JwtAuthManager.generateDeterministicSalt(fallbackEmail)
        val oauthAccount = RegisteredUserAccount(
            id = "usr-oauth-$username",
            username = username,
            email = fallbackEmail,
            fullName = "Cliente ${cleanProvider.replaceFirstChar { char -> char.uppercase() }}",
            passwordHash = "",
            salt = salt,
            birthDate = "",
            phone = "",
            role = "client",
        )
        upsertAccountInMemory(oauthAccount)
        val localToken = JwtAuthManager.issueAccessToken(oauthAccount)
        applyAuthenticatedSession(oauthAccount, localToken)
        onPersistAccountAndSession(oauthAccount, localToken)
        AuthOutcome.authenticated(localToken, oauthAccount)
    }

    fun changeUserPassword(
        currentPassword: String,
        newPassword: String,
    ): AuthOutcome {
        val cleanCurrent = currentPassword.trim()
        val cleanNew = newPassword.trim()
        val passwordError = InputValidator.validatePassword(cleanNew)
        if (passwordError != null) {
            return AuthOutcome.failure("AUTH_WEAK_PASSWORD", passwordError)
        }
        val currentAccount = currentAuthenticatedUserFlow.value ?: return AuthOutcome.failure(
            errorCode = "AUTH_UNAUTHORIZED",
            errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
        )
        val hasLocalHash = currentAccount.passwordHash.isNotBlank()
        if (hasLocalHash) {
            val verified = JwtAuthManager.verifyPassword(cleanCurrent, currentAccount.salt, currentAccount.passwordHash)
            if (!verified) {
                return AuthOutcome.failure(
                    errorCode = "AUTH_INVALID_CREDENTIALS",
                    errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
                )
            }
        }
        val nextSalt = JwtAuthManager.generateDeterministicSalt("${currentAccount.email}:changed")
        val nextHash = JwtAuthManager.hashPassword(cleanNew, nextSalt)
        val updatedAccount = currentAccount.copy(passwordHash = nextHash, salt = nextSalt)
        upsertAccountInMemory(updatedAccount)
        val nextToken = currentSessionJwtFlow.value ?: JwtAuthManager.issueAccessToken(updatedAccount)
        applyAuthenticatedSession(updatedAccount, nextToken)
        scope.launch {
            onPersistAccountAndSession(updatedAccount, nextToken)
            runCatching {
                apiClient?.changePassword(
                    ChangePasswordInputDto(
                        currentPassword = cleanCurrent,
                        newPassword = cleanNew,
                    ),
                )
            }
        }
        return AuthOutcome.authenticated(nextToken, updatedAccount)
    }

    fun logoutSession(localClear: () -> Unit) {
        localClear()
        _notifications.value = emptyList()
        _eventGuests.value = emptyList()
        apiClient?.setAccessToken(null)
        scope.launch {
            runCatching {
                apiClient?.logout()
            }
        }
    }

    fun createQuote(
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
    ): QuoteRecord {
        val nextNumber = 1200 + _quotes.value.size + 1
        val subtotal = lines.sumOf { line -> line.subtotalDop }
        val total = (subtotal + deliveryFeeDop - discountDop).coerceAtLeast(0)
        val depositPercent = _businessSettings.value.depositPercent
        val deposit = (total * depositPercent) / 100
        val created = QuoteRecord(
            id = "quo-$nextNumber",
            code = "COT-2026-$nextNumber",
            eventId = eventId,
            eventTitle = eventTitle.trim(),
            clientName = clientName.trim(),
            clientEmail = clientEmail.trim(),
            venue = venue.trim(),
            dateLabel = dateLabel.trim(),
            guests = guests,
            status = "PENDING",
            lines = lines,
            deliveryFeeDop = deliveryFeeDop,
            discountDop = discountDop,
            totalDop = total,
            depositPercent = depositPercent,
            depositDop = deposit,
        )
        _quotes.update { existing -> listOf(created) + existing }
        recomputeHonestMetrics()
        scope.launch {
            runCatching {
                val dtoLines = lines.map { line ->
                    QuoteLineDto(
                        articleCode = line.articleCode,
                        name = line.name,
                        quantity = line.quantity.toLong(),
                        unitPriceRD = line.unitPriceDop.toLong(),
                    )
                }
                val response = apiClient?.createQuote(
                    CreateQuoteInputDto(
                        eventTitle = created.eventTitle,
                        clientName = created.clientName,
                        eventDateLabel = created.dateLabel,
                        venue = created.venue,
                        guests = created.guests.toLong(),
                        lines = dtoLines,
                        deliveryFeeRD = deliveryFeeDop.toLong(),
                        discountRD = discountDop.toLong(),
                        clientEmail = created.clientEmail.ifBlank { null },
                        eventId = created.eventId.ifBlank { null },
                    ),
                )
                if (response != null) {
                    val mapped = RosaFiestaBackendMappers.toQuoteRecord(response.quote)
                    _quotes.update { list ->
                        list.map { item -> if (item.id == created.id) mapped else item }
                    }
                }
            }
        }
        return created
    }

    fun approveQuote(quoteId: String, signedByName: String, signatureSvg: String): QuoteRecord? {
        val targetId = quoteId.trim()
        var updatedRecord: QuoteRecord? = null
        _quotes.update { list ->
            list.map { quote ->
                if (quote.id == targetId) {
                    val approved = quote.copy(
                        status = "APPROVED",
                        signedByName = signedByName.trim(),
                        signatureSvg = signatureSvg.trim(),
                    )
                    updatedRecord = approved
                    approved
                } else {
                    quote
                }
            }
        }
        recomputeHonestMetrics()
        if (updatedRecord != null) {
            scope.launch {
                runCatching {
                    apiClient?.signContract(
                        id = targetId,
                        input = SignContractInputDto(
                            signedByName = signedByName.trim(),
                            signatureSvg = signatureSvg.trim(),
                        ),
                    )
                }
            }
        }
        return updatedRecord
    }

    fun declineQuote(quoteId: String, reason: String): QuoteRecord? {
        val targetId = quoteId.trim()
        var updatedRecord: QuoteRecord? = null
        _quotes.update { list ->
            list.map { quote ->
                if (quote.id == targetId) {
                    val declined = quote.copy(
                        status = "DECLINED",
                        rejectionReason = reason.trim(),
                    )
                    updatedRecord = declined
                    declined
                } else {
                    quote
                }
            }
        }
        recomputeHonestMetrics()
        if (updatedRecord != null) {
            scope.launch {
                runCatching {
                    apiClient?.declineQuote(
                        id = targetId,
                        input = DeclineQuoteInputDto(reason = reason.trim()),
                    )
                }
            }
        }
        return updatedRecord
    }

    fun duplicateQuote(quoteId: String): QuoteRecord? {
        val source = _quotes.value.firstOrNull { quote -> quote.id == quoteId.trim() } ?: return null
        val nextSeq = 1200 + _quotes.value.size + 1
        val duplicated = source.copy(
            id = "quo-$nextSeq",
            code = "COT-2026-$nextSeq",
            status = "PENDING",
            signatureSvg = "",
            signedByName = "",
            rejectionReason = "",
        )
        _quotes.update { existing -> listOf(duplicated) + existing }
        recomputeHonestMetrics()
        scope.launch {
            runCatching {
                apiClient?.duplicateQuote(source.id)
            }
        }
        return duplicated
    }

    suspend fun sendRosaPrompt(
        message: String,
        eventType: String,
        guests: Int,
        budgetDop: Int,
        excludedCategories: List<String>,
    ): RosaChatReply = withContext(ioDispatcher) {
        val cleanMessage = message.trim()
        val remote = runCatching {
            apiClient?.sendRosaChat(
                RosaChatInputDto(
                    message = cleanMessage,
                    eventType = eventType,
                    guests = guests.toLong(),
                    budgetRD = budgetDop.toLong(),
                    excludedCategories = excludedCategories.takeIf { list -> list.isNotEmpty() },
                ),
            )
        }.getOrNull()
        if (remote != null) {
            return@withContext RosaFiestaBackendMappers.toRosaChatReply(remote)
        }
        val proposal = RosaFiestaEngine.buildRosaProposal(eventType, guests.coerceAtLeast(10))
        val lines = proposal.lines.map { line ->
            EventLineItem(
                articleCode = line.code,
                name = line.name,
                quantity = line.quantity,
                unitPriceDop = line.unitPriceDop,
                photo = RosaFiestaBackendMappers.selectPhotoForArticle("", line.name),
            )
        }
        val fitsBudget = proposal.totalDop <= budgetDop
        RosaChatReply(
            reply = "Preparé una propuesta para $guests invitados (${proposal.lines.size} renglones) por RD$ ${proposal.totalDop}.",
            intent = "PROPOSAL",
            styleTitle = "$eventType Rosa & Dorado",
            estimatedTotalDop = proposal.totalDop,
            withinBudget = fitsBudget,
            suggestedLines = lines,
        )
    }

    suspend fun transcribeVoicePrompt(wavBase64: String, locale: String): VoiceTranscriptionOutcome = withContext(ioDispatcher) {
        val remote = runCatching {
            apiClient?.transcribeRosaAudio(wavBase64 = wavBase64, locale = locale)
        }.getOrNull()
        if (remote != null) {
            return@withContext RosaFiestaBackendMappers.toVoiceTranscriptionOutcome(remote)
        }
        val hasPayload = wavBase64.trim().length > 32
        VoiceTranscriptionOutcome(
            transcript = if (hasPayload) "Cotizar montaje para 80 invitados en jardín" else "",
            locale = locale,
            voiceDetected = hasPayload,
            durationMs = if (hasPayload) 2400 else 0,
            provider = "local-energy",
        )
    }

    fun approveRefund(refundId: String): RefundRequest? {
        val targetId = refundId.trim()
        val approver = currentAuthenticatedUserFlow.value?.fullName?.ifBlank { "María" } ?: "María"
        val nowLabel = DomainClockFormatter.currentFullDateLabel()
        var approvedItem: RefundRequest? = null
        _refundRequests.update { list ->
            list.map { item ->
                if (item.id == targetId) {
                    val updated = item.copy(
                        status = "APPROVED",
                        approvedBy = approver,
                        approvedAtLabel = nowLabel,
                    )
                    approvedItem = updated
                    updated
                } else {
                    item
                }
            }
        }
        if (approvedItem != null) {
            scope.launch {
                runCatching {
                    apiClient?.approveRefund(targetId)
                }
            }
        }
        return approvedItem
    }

    fun createReview(
        eventId: String,
        authorName: String,
        rating: Int,
        comment: String,
    ): ReviewRecord {
        val cleanEventId = eventId.trim()
        val linkedEvent = eventsFlow.value.firstOrNull { event -> event.id == cleanEventId }
        val boundedRating = rating.coerceIn(1, 5)
        val created = ReviewRecord(
            id = "rev-${_reviews.value.size + 1}",
            eventId = cleanEventId,
            eventTitle = linkedEvent?.title.orEmpty(),
            authorName = authorName.trim(),
            rating = boundedRating,
            comment = comment.trim(),
            verified = linkedEvent != null,
            dateLabel = DomainClockFormatter.currentFullDateLabel(),
        )
        _reviews.update { list -> listOf(created) + list }
        scope.launch {
            runCatching {
                apiClient?.createReview(
                    CreateReviewInputDto(
                        eventId = cleanEventId,
                        rating = boundedRating.toLong(),
                        comment = created.comment,
                        clientName = created.authorName,
                    ),
                )
            }
        }
        return created
    }

    fun replyToReview(reviewId: String, adminReply: String): ReviewRecord? {
        val targetId = reviewId.trim()
        val cleanReply = adminReply.trim()
        var updatedReview: ReviewRecord? = null
        _reviews.update { list ->
            list.map { review ->
                if (review.id == targetId) {
                    val next = review.copy(adminReply = cleanReply, verified = true)
                    updatedReview = next
                    next
                } else {
                    review
                }
            }
        }
        if (updatedReview != null) {
            scope.launch {
                runCatching {
                    apiClient?.replyToReview(targetId, cleanReply)
                }
            }
        }
        return updatedReview
    }

    fun createSupplier(
        name: String,
        category: String,
        contactName: String,
        phone: String,
        email: String,
        website: String,
        notes: String,
    ): SupplierRecord {
        val created = SupplierRecord(
            id = "sup-${_suppliers.value.size + 1}",
            name = name.trim(),
            category = category.trim(),
            contactName = contactName.trim(),
            phone = phone.trim(),
            email = email.trim(),
            website = website.trim(),
            notes = notes.trim(),
        )
        _suppliers.update { list -> list + created }
        scope.launch {
            runCatching {
                apiClient?.createSupplier(
                    UpsertSupplierInputDto(
                        name = created.name,
                        category = created.category,
                        specialty = created.specialty,
                        phone = created.phone,
                        contactName = created.contactName,
                        email = created.email,
                        website = created.website.ifBlank { null },
                        notes = created.notes.ifBlank { null },
                    ),
                )
            }
        }
        return created
    }

    fun createLead(
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
    ): LeadRecord {
        val created = LeadRecord(
            id = "lead-${_leads.value.size + 1}",
            clientName = clientName.trim(),
            eventType = eventType.trim(),
            dateLabel = dateLabel.trim(),
            guests = guests,
            estimatedDop = estimatedDop,
            channel = channel.trim(),
            stage = stage.trim(),
            followUpNote = followUpNote.trim(),
            phone = phone.trim(),
            email = email.trim(),
        )
        _leads.update { list -> listOf(created) + list }
        scope.launch {
            runCatching {
                apiClient?.createLead(
                    CreateLeadInputDto(
                        fullName = created.clientName,
                        phone = created.phone,
                        eventType = created.eventType,
                        email = created.email.ifBlank { null },
                        eventDateLabel = created.dateLabel.ifBlank { null },
                        guests = created.guests.toLong(),
                        estimatedDop = created.estimatedDop.toLong(),
                        stage = created.stage,
                        notes = created.followUpNote.ifBlank { null },
                    ),
                )
            }
        }
        return created
    }

    fun updateLead(
        leadId: String,
        stage: String?,
        estimatedDop: Int?,
        followUpNote: String?,
    ): LeadRecord? {
        val targetId = leadId.trim()
        var updatedLead: LeadRecord? = null
        _leads.update { list ->
            list.map { lead ->
                if (lead.id == targetId) {
                    val next = lead.copy(
                        stage = stage?.trim() ?: lead.stage,
                        estimatedDop = estimatedDop ?: lead.estimatedDop,
                        followUpNote = followUpNote?.trim() ?: lead.followUpNote,
                    )
                    updatedLead = next
                    next
                } else {
                    lead
                }
            }
        }
        if (updatedLead != null) {
            scope.launch {
                runCatching {
                    apiClient?.updateLead(
                        leadId = targetId,
                        input = UpdateLeadInputDto(
                            stage = stage?.trim(),
                            estimatedDop = estimatedDop?.toLong(),
                            notes = followUpNote?.trim(),
                        ),
                    )
                }
            }
        }
        return updatedLead
    }

    fun updateEventDetails(
        eventId: String,
        title: String?,
        venue: String?,
        zone: String?,
        guests: Int?,
        status: EventStatus?,
        leadStaffName: String?,
        sketchJson: String?,
    ): ClientEvent? {
        val targetId = eventId.trim()
        var updatedEvent: ClientEvent? = null
        eventsFlow.update { list ->
            list.map { event ->
                if (event.id == targetId) {
                    val next = event.copy(
                        title = title?.trim() ?: event.title,
                        venue = venue?.trim() ?: event.venue,
                        zone = zone?.trim() ?: event.zone,
                        guests = guests ?: event.guests,
                        status = status ?: event.status,
                        leadStaffName = leadStaffName?.trim() ?: event.leadStaffName,
                        sketchJson = sketchJson ?: event.sketchJson,
                    )
                    updatedEvent = next
                    next
                } else {
                    event
                }
            }
        }
        if (updatedEvent != null) {
            recomputeHonestMetrics()
            scope.launch {
                onPersistCoreSync()
                runCatching {
                    apiClient?.updateRemoteEvent(
                        eventId = targetId,
                        input = UpdateEventInputDto(
                            title = title?.trim(),
                            venue = venue?.trim(),
                            zone = zone?.trim(),
                            guests = guests?.toLong(),
                            status = status?.name,
                            assignedLead = leadStaffName?.trim(),
                            sketchJson = sketchJson,
                        ),
                    )
                }
            }
        }
        return updatedEvent
    }

    fun duplicateEvent(eventId: String): ClientEvent? {
        val source = eventsFlow.value.firstOrNull { event -> event.id == eventId.trim() } ?: return null
        val seq = 1120 + eventsFlow.value.size
        val duplicated = source.copy(
            id = "evt-$seq",
            code = "RF-2026-$seq",
            title = "${source.title} (Copia)",
            paidDepositDop = 0,
            depositMethodLabel = "Pendiente de anticipo",
            depositDateLabel = "—",
            status = EventStatus.PENDING_QUOTE,
            priorChangesCount = 0,
        )
        eventsFlow.update { list -> list + duplicated }
        recomputeHonestMetrics()
        scope.launch {
            onPersistCoreSync()
            runCatching {
                apiClient?.duplicateRemoteEvent(source.id)
            }
        }
        return duplicated
    }

    fun addEventGuest(
        eventId: String,
        fullName: String,
        rsvpStatus: String,
        tableLabel: String,
        notes: String,
    ): EventGuest {
        val cleanEventId = eventId.trim()
        val created = EventGuest(
            id = "gst-${_eventGuests.value.size + 1}",
            eventId = cleanEventId,
            fullName = fullName.trim(),
            rsvpStatus = rsvpStatus.trim(),
            tableLabel = tableLabel.trim(),
            notes = notes.trim(),
        )
        _eventGuests.update { list -> list + created }
        scope.launch {
            runCatching {
                apiClient?.addEventGuest(
                    eventId = cleanEventId,
                    input = AddGuestInputDto(
                        fullName = created.fullName,
                        rsvpStatus = created.rsvpStatus,
                        tableLabel = created.tableLabel,
                        dietaryNotes = created.notes.ifBlank { null },
                    ),
                )
            }
        }
        return created
    }

    fun updateEventGuest(
        eventId: String,
        guestId: String,
        fullName: String?,
        rsvpStatus: String?,
        tableLabel: String?,
        notes: String?,
    ): EventGuest? {
        val targetId = guestId.trim()
        var updatedGuest: EventGuest? = null
        _eventGuests.update { list ->
            list.map { guest ->
                if (guest.id == targetId) {
                    val next = guest.copy(
                        fullName = fullName?.trim() ?: guest.fullName,
                        rsvpStatus = rsvpStatus?.trim() ?: guest.rsvpStatus,
                        tableLabel = tableLabel?.trim() ?: guest.tableLabel,
                        notes = notes?.trim() ?: guest.notes,
                    )
                    updatedGuest = next
                    next
                } else {
                    guest
                }
            }
        }
        if (updatedGuest != null) {
            scope.launch {
                runCatching {
                    apiClient?.updateEventGuest(
                        eventId = eventId.trim(),
                        guestId = targetId,
                        input = UpdateGuestInputDto(
                            fullName = fullName?.trim(),
                            rsvpStatus = rsvpStatus?.trim(),
                            tableLabel = tableLabel?.trim(),
                            dietaryNotes = notes?.trim(),
                        ),
                    )
                }
            }
        }
        return updatedGuest
    }

    fun addEventScheduleBlock(
        eventId: String,
        timeRange: String,
        title: String,
        ownerName: String,
        completed: Boolean,
    ): EventScheduleBlock {
        val cleanEventId = eventId.trim()
        val nextSort = _eventSchedule.value.count { block -> block.eventId == cleanEventId } + 1
        val created = EventScheduleBlock(
            id = "sch-${_eventSchedule.value.size + 1}",
            eventId = cleanEventId,
            timeRange = timeRange.trim(),
            title = title.trim(),
            ownerName = ownerName.trim(),
            completed = completed,
            sortOrder = nextSort,
        )
        _eventSchedule.update { list -> list + created }
        scope.launch {
            runCatching {
                apiClient?.addEventScheduleBlock(
                    eventId = cleanEventId,
                    input = AddScheduleBlockInputDto(
                        timeRange = created.timeRange,
                        title = created.title,
                        ownerName = created.ownerName,
                        status = if (completed) "COMPLETED" else "PENDING",
                        sortOrder = nextSort.toLong(),
                    ),
                )
            }
        }
        return created
    }

    fun deleteEventScheduleBlock(eventId: String, blockId: String): Boolean {
        val targetId = blockId.trim()
        val hadBlock = _eventSchedule.value.any { block -> block.id == targetId }
        if (!hadBlock) return false
        _eventSchedule.update { list -> list.filterNot { block -> block.id == targetId } }
        scope.launch {
            runCatching {
                apiClient?.deleteEventScheduleBlock(eventId = eventId.trim(), blockId = targetId)
            }
        }
        return true
    }

    fun sendThreadMessage(
        threadId: String,
        senderName: String,
        body: String,
        attachmentName: String,
    ): ChatMessageRecord {
        val cleanThreadId = threadId.trim()
        val cleanBody = body.trim()
        val cleanSender = senderName.trim().ifEmpty { "María" }
        val timeLabel = DomainClockFormatter.currentTimeHmLabel()
        val existingThread = _chatThreads.value.firstOrNull { thread -> thread.id == cleanThreadId }
        val created = ChatMessageRecord(
            id = "msg-${_threadMessages.value.size + 1}",
            threadId = cleanThreadId,
            contactName = existingThread?.contactName ?: cleanSender,
            eventId = existingThread?.eventId.orEmpty(),
            eventTitle = existingThread?.eventTitle.orEmpty(),
            senderName = cleanSender,
            senderRole = activeRoleFlow.value,
            body = cleanBody,
            attachmentName = attachmentName.trim(),
            timestampLabel = timeLabel,
            unreadCount = 0,
        )
        _threadMessages.update { list -> list + created }
        if (existingThread != null) {
            _chatThreads.update { threads ->
                threads.map { thread ->
                    if (thread.id == cleanThreadId) {
                        thread.copy(
                            lastMessage = cleanBody,
                            timestampLabel = timeLabel,
                            unreadCount = 0,
                        )
                    } else {
                        thread
                    }
                }
            }
        } else {
            val newThread = ChatThread(
                id = cleanThreadId,
                threadId = cleanThreadId,
                contactName = cleanSender,
                eventId = "",
                eventTitle = "Consulta directa",
                lastMessage = cleanBody,
                timestampLabel = timeLabel,
                unreadCount = 0,
            )
            _chatThreads.update { threads -> listOf(newThread) + threads }
        }
        chatMessagesFlow.update { legacy -> legacy + "$cleanSender: $cleanBody" }
        scope.launch {
            runCatching {
                apiClient?.sendThreadMessage(
                    threadId = cleanThreadId,
                    input = PostChatMessageInputDto(
                        body = cleanBody,
                        attachmentName = attachmentName.trim().ifBlank { null },
                        eventId = existingThread?.eventId?.ifBlank { null },
                        eventTitle = existingThread?.eventTitle?.ifBlank { null },
                    ),
                )
            }
        }
        return created
    }

    fun saveBusinessSettings(updated: BusinessSettings): BusinessSettings {
        _businessSettings.value = updated
        scope.launch {
            runCatching {
                apiClient?.updateAdminSettings(
                    RosaFiestaBackendMappers.toUpdateSettingsInputDto(updated),
                )
            }
        }
        return updated
    }

    fun inviteStaffMember(
        fullName: String,
        email: String,
        roleLabel: String,
        role: String,
        phone: String,
        permissions: List<String>,
    ): StaffMember {
        val mappedRole = when {
            role.contains("admin", ignoreCase = true) -> UserRole.ADMIN_OWNER
            role.contains("logistic", ignoreCase = true) -> UserRole.STAFF_LOGISTICS
            role.contains("warehouse", ignoreCase = true) -> UserRole.STAFF_WAREHOUSE
            else -> UserRole.STAFF_DECORATION
        }
        val parsedPermissions = permissions.mapNotNull { raw ->
            StaffPermission.entries.firstOrNull { perm -> perm.name.equals(raw.trim(), ignoreCase = true) }
        }.toSet()
        val resolvedPermissions = parsedPermissions.ifEmpty {
            RosaFiestaEngine.permissionsForRole(mappedRole)
        }
        val created = StaffProfile(
            id = "staff-${staffFlow.value.size + 1}",
            name = fullName.trim(),
            roleLabel = roleLabel.trim(),
            role = mappedRole,
            activeEventsCount = 0,
            permissions = resolvedPermissions,
            canViewPayments = StaffPermission.PAYMENTS in resolvedPermissions,
            email = email.trim(),
            phone = phone.trim(),
            active = true,
        )
        staffFlow.update { list -> list + created }
        scope.launch {
            onPersistCoreSync()
            runCatching {
                apiClient?.inviteStaff(
                    InviteStaffInputDto(
                        fullName = created.name,
                        email = created.email,
                        permissions = resolvedPermissions.map { perm -> perm.name },
                        phone = created.phone.ifBlank { null },
                        role = role.trim(),
                        roleLabel = created.roleLabel,
                    ),
                )
            }
        }
        return created
    }

    fun updateStaffMember(
        staffId: String,
        fullName: String?,
        roleLabel: String?,
        active: Boolean?,
        permissions: List<String>?,
    ): StaffMember? {
        val targetId = staffId.trim()
        var updatedMember: StaffMember? = null
        staffFlow.update { list ->
            list.map { member ->
                if (member.id == targetId) {
                    val nextPerms = permissions?.mapNotNull { raw ->
                        StaffPermission.entries.firstOrNull { perm -> perm.name.equals(raw.trim(), ignoreCase = true) }
                    }?.toSet() ?: member.permissions
                    val next = member.copy(
                        name = fullName?.trim() ?: member.name,
                        roleLabel = roleLabel?.trim() ?: member.roleLabel,
                        active = active ?: member.active,
                        permissions = nextPerms,
                        canViewPayments = StaffPermission.PAYMENTS in nextPerms,
                    )
                    updatedMember = next
                    next
                } else {
                    member
                }
            }
        }
        if (updatedMember != null) {
            scope.launch {
                onPersistCoreSync()
                runCatching {
                    apiClient?.updateStaffMember(
                        staffId = targetId,
                        input = UpdateStaffInputDto(
                            fullName = fullName?.trim(),
                            roleLabel = roleLabel?.trim(),
                            active = active,
                            permissions = permissions,
                        ),
                    )
                }
            }
        }
        return updatedMember
    }

    fun updateClientProfile(
        clientId: String,
        fullName: String?,
        email: String?,
        phone: String?,
        notes: String?,
    ): ClientProfile? {
        val targetId = clientId.trim()
        var updatedClient: ClientProfile? = null
        _clients.update { list ->
            list.map { client ->
                if (client.id == targetId) {
                    val next = client.copy(
                        fullName = fullName?.trim() ?: client.fullName,
                        email = email?.trim() ?: client.email,
                        phone = phone?.trim() ?: client.phone,
                        notes = notes?.trim() ?: client.notes,
                    )
                    updatedClient = next
                    next
                } else {
                    client
                }
            }
        }
        if (updatedClient != null) {
            scope.launch {
                runCatching {
                    apiClient?.updateClient(
                        clientId = targetId,
                        input = UpdateClientInputDto(
                            fullName = fullName?.trim(),
                            email = email?.trim(),
                            phone = phone?.trim(),
                            notes = notes?.trim(),
                        ),
                    )
                }
            }
        }
        return updatedClient
    }

    fun createPackage(
        name: String,
        subtitle: String,
        eventType: String,
        suggestedGuests: Int,
        priceDop: Int,
        includedItems: List<String>,
        visibleInCatalog: Boolean,
    ): EventPackage {
        val photo = RosaFiestaBackendMappers.selectPhotoForEventType(eventType)
        val created = EventPackage(
            id = "pkg-${_packages.value.size + 1}",
            name = name.trim(),
            subtitle = subtitle.trim(),
            suggestedGuests = suggestedGuests,
            priceDop = priceDop,
            savingsDop = (priceDop / 10).coerceAtLeast(50),
            photo = photo,
            includedItems = includedItems,
            eventType = eventType.trim(),
            visibleInCatalog = visibleInCatalog,
        )
        _packages.update { list -> list + created }
        scope.launch {
            runCatching {
                apiClient?.createCatalogPackage(
                    SavePackageInputDto(
                        id = created.id,
                        name = created.name,
                        eventType = created.eventType,
                        photoKey = photo.key,
                        guestCapacity = suggestedGuests.toLong(),
                        totalPriceDop = priceDop.toLong(),
                        itemsSummary = subtitle.trim().ifBlank { includedItems.joinToString(", ") },
                        visible = visibleInCatalog,
                    ),
                )
            }
        }
        return created
    }

    fun updatePackage(
        packageId: String,
        name: String,
        subtitle: String,
        eventType: String,
        suggestedGuests: Int,
        priceDop: Int,
        includedItems: List<String>,
        visibleInCatalog: Boolean,
    ): EventPackage? {
        val targetId = packageId.trim()
        var updatedPkg: EventPackage? = null
        _packages.update { list ->
            list.map { bundle ->
                if (bundle.id == targetId) {
                    val next = bundle.copy(
                        name = name.trim(),
                        subtitle = subtitle.trim(),
                        eventType = eventType.trim(),
                        suggestedGuests = suggestedGuests,
                        priceDop = priceDop,
                        includedItems = includedItems,
                        visibleInCatalog = visibleInCatalog,
                    )
                    updatedPkg = next
                    next
                } else {
                    bundle
                }
            }
        }
        val persisted = updatedPkg
        if (persisted != null) {
            scope.launch {
                runCatching {
                    apiClient?.updateCatalogPackage(
                        packageId = targetId,
                        input = UpdatePackageInputDto(
                            name = persisted.name,
                            eventType = persisted.eventType,
                            photoKey = persisted.photo.key,
                            guestCapacity = persisted.suggestedGuests.toLong(),
                            totalPriceDop = persisted.priceDop.toLong(),
                            itemsSummary = persisted.subtitle.ifBlank { persisted.includedItems.joinToString(", ") },
                            visible = persisted.visibleInCatalog,
                        ),
                    )
                }
            }
        }
        return updatedPkg
    }

    fun updateArticle(
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
    ): CatalogArticle? {
        val targetId = articleId.trim()
        var updatedArticle: CatalogArticle? = null
        articlesFlow.update { list ->
            list.map { article ->
                if (article.id == targetId) {
                    val nextTotal = totalStock ?: article.totalStock
                    val nextAvailable = (availableStock ?: article.availableOct23).coerceIn(0, nextTotal)
                    val next = article.copy(
                        name = name?.trim() ?: article.name,
                        category = category?.trim() ?: article.category,
                        description = description?.trim() ?: article.description,
                        pricePerDayDop = pricePerDayDop ?: article.pricePerDayDop,
                        totalStock = nextTotal,
                        availableOct23 = nextAvailable,
                        dimensions = dimensions?.trim() ?: article.dimensions,
                        material = material?.trim() ?: article.material,
                        visibleInCatalog = visibleInCatalog ?: article.visibleInCatalog,
                    )
                    updatedArticle = next
                    next
                } else {
                    article
                }
            }
        }
        if (updatedArticle != null) {
            scope.launch {
                onPersistCoreSync()
                runCatching {
                    apiClient?.updateCatalogArticle(
                        articleId = targetId,
                        input = UpdateArticleInputDto(
                            name = name?.trim(),
                            categoryId = category?.trim(),
                            description = description?.trim(),
                            dailyPriceDop = pricePerDayDop?.toLong(),
                            totalStock = totalStock?.toLong(),
                            availableStock = availableStock?.toLong(),
                            dimensions = dimensions?.trim(),
                            material = material?.trim(),
                            visible = visibleInCatalog,
                        ),
                    )
                }
            }
        }
        return updatedArticle
    }

    suspend fun queryArticleAvailability(articleId: String, monthIso: String): ArticleAvailabilityCalendar = withContext(ioDispatcher) {
        val cleanId = articleId.trim()
        val remote = runCatching {
            apiClient?.getCatalogAvailability(cleanId, monthIso)
        }.getOrNull()
        if (remote != null) {
            return@withContext RosaFiestaBackendMappers.toAvailabilityCalendar(remote.availability)
        }
        val article = articlesFlow.value.firstOrNull { item -> item.id == cleanId }
        val total = article?.totalStock ?: 0
        val baseAvail = article?.availableOct23 ?: 0
        val days = (1..28).map { dayNumber ->
            val paddedDay = dayNumber.toString().padStart(2, '0')
            val reserved = if (dayNumber % 7 == 6) (total - baseAvail).coerceAtLeast(0) else 0
            val avail = (total - reserved).coerceAtLeast(0)
            ArticleAvailabilityDay(
                dateIso = "$monthIso-$paddedDay",
                reservedUnits = reserved,
                availableUnits = avail,
                lowStock = avail <= 5,
            )
        }
        ArticleAvailabilityCalendar(
            articleId = cleanId,
            articleName = article?.name.orEmpty(),
            month = monthIso,
            totalStock = total,
            days = days,
        )
    }

    fun reportDamage(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
    ): DamageIncident {
        val cleanEventId = eventId.trim()
        val eventTitle = eventsFlow.value.firstOrNull { event -> event.id == cleanEventId }?.title.orEmpty()
        val created = DamageIncident(
            id = "dmg-${_damageIncidents.value.size + 1}",
            eventTitle = eventTitle,
            articleName = articleName.trim(),
            damagedUnits = damagedUnits,
            missingUnits = missingUnits,
            replacementDop = replacementDop,
            notes = notes.trim(),
            eventId = cleanEventId,
            articleCode = articleCode.trim(),
        )
        _damageIncidents.update { list -> listOf(created) + list }
        scope.launch {
            runCatching {
                apiClient?.reportDamage(
                    eventId = cleanEventId,
                    input = ReportDamageInputDto(
                        articleCode = created.articleCode,
                        articleName = created.articleName,
                        damagedUnits = damagedUnits.toLong(),
                        missingUnits = missingUnits.toLong(),
                        replacementRD = replacementDop.toLong(),
                        notes = created.notes,
                    ),
                )
            }
        }
        return created
    }

    fun markNotificationRead(notificationId: String): NotificationRecord? {
        val targetId = notificationId.trim()
        var updatedItem: NotificationRecord? = null
        _notifications.update { list ->
            list.map { item ->
                if (item.id == targetId) {
                    val next = item.copy(isRead = true)
                    updatedItem = next
                    next
                } else {
                    item
                }
            }
        }
        if (updatedItem != null) {
            scope.launch {
                runCatching {
                    apiClient?.markNotificationRead(targetId)
                }
            }
        }
        return updatedItem
    }

    fun saveBotFaq(
        question: String,
        answer: String,
        category: String,
        active: Boolean,
        id: String,
    ): BotFaqRecord {
        val resolvedId = id.trim().ifEmpty { "faq-${_botFaqs.value.size + 1}" }
        val saved = BotFaqRecord(
            id = resolvedId,
            category = category.trim(),
            question = question.trim(),
            answer = answer.trim(),
            active = active,
        )
        _botFaqs.update { list ->
            val exists = list.any { item -> item.id == resolvedId }
            if (exists) {
                list.map { item -> if (item.id == resolvedId) saved else item }
            } else {
                list + saved
            }
        }
        scope.launch {
            runCatching {
                apiClient?.createBotFaq(
                    UpsertBotFaqInputDto(
                        id = resolvedId,
                        category = saved.category,
                        question = saved.question,
                        answer = saved.answer,
                        active = saved.active,
                    ),
                )
            }
        }
        return saved
    }

    fun saveBotKeyword(
        keyword: String,
        autoReply: String,
        handoffToHuman: Boolean,
        id: String,
    ): BotKeywordRecord {
        val resolvedId = id.trim().ifEmpty { "kw-${_botKeywords.value.size + 1}" }
        val saved = BotKeywordRecord(
            id = resolvedId,
            keyword = keyword.trim().lowercase(),
            autoReply = autoReply.trim(),
            handoffToHuman = handoffToHuman,
        )
        _botKeywords.update { list ->
            val exists = list.any { item -> item.id == resolvedId }
            if (exists) {
                list.map { item -> if (item.id == resolvedId) saved else item }
            } else {
                list + saved
            }
        }
        scope.launch {
            runCatching {
                apiClient?.createBotKeyword(
                    UpsertBotKeywordInputDto(
                        id = resolvedId,
                        keyword = saved.keyword,
                        autoReply = saved.autoReply,
                        handoffToHuman = saved.handoffToHuman,
                    ),
                )
            }
        }
        return saved
    }

    fun updateBotThread(
        threadId: String,
        botPaused: Boolean?,
        assignedAgent: String?,
        lastMessage: String?,
    ): BotThreadRecord? {
        val targetId = threadId.trim()
        var updatedThread: BotThreadRecord? = null
        _botThreads.update { list ->
            list.map { thread ->
                if (thread.id == targetId) {
                    val next = thread.copy(
                        botPaused = botPaused ?: thread.botPaused,
                        assignedAgent = assignedAgent?.trim() ?: thread.assignedAgent,
                        lastMessage = lastMessage?.trim() ?: thread.lastMessage,
                        updatedAtLabel = "Ahora",
                    )
                    updatedThread = next
                    next
                } else {
                    thread
                }
            }
        }
        if (updatedThread != null) {
            scope.launch {
                runCatching {
                    apiClient?.updateBotThread(
                        id = targetId,
                        input = UpdateBotThreadInputDto(
                            botPaused = botPaused,
                            assignedAgent = assignedAgent?.trim(),
                            lastMessage = lastMessage?.trim(),
                        ),
                    )
                }
            }
        }
        return updatedThread
    }

    suspend fun exportReport(reportType: String, period: String): ReportExportResult = withContext(ioDispatcher) {
        val remote = runCatching {
            apiClient?.exportReport(type = reportType, period = period)
        }.getOrNull()
        if (remote != null) {
            return@withContext RosaFiestaBackendMappers.toReportExportResult(remote.report)
        }
        val currentPayments = paymentsFlow.value
        val total = currentPayments.sumOf { payment -> payment.amountDop }
        val itbis = (total * _businessSettings.value.itbisPercent) / 100
        val csvRows = buildString {
            appendLine("recibo,evento,cliente,monto_dop,fecha")
            currentPayments.forEach { payment ->
                appendLine("${payment.receiptNumber},${payment.eventTitle},${payment.clientName},${payment.amountDop},${payment.dateLabel}")
            }
        }
        val series = currentPayments.map { payment ->
            ReportSeriesPoint(
                label = payment.dateLabel,
                valueDop = payment.amountDop,
                count = 1,
            )
        }
        ReportExportResult(
            reportType = reportType,
            period = period,
            filename = "rosafiesta-$reportType-$period.csv",
            currency = "DOP",
            totalDop = total,
            itbisDop = itbis,
            generatedAt = DomainClockFormatter.currentFullDateLabel(),
            csvContent = csvRows,
            series = series,
        )
    }

    private fun upsertAccountInMemory(account: RegisteredUserAccount) {
        userAccountsFlow.update { existing ->
            val found = existing.any { item ->
                val sameId = item.id == account.id
                val sameEmail = item.email.equals(account.email, ignoreCase = true)
                if (sameId) true else sameEmail
            }
            if (found) {
                existing.map { item ->
                    val sameId = item.id == account.id
                    val sameEmail = item.email.equals(account.email, ignoreCase = true)
                    val shouldReplace = if (sameId) true else sameEmail
                    if (shouldReplace) account else item
                }
            } else {
                existing + account
            }
        }
    }

    private fun applyAuthenticatedSession(account: RegisteredUserAccount, token: String) {
        currentAuthenticatedUserFlow.value = account
        currentSessionJwtFlow.value = token
        currentUserEmailFlow.value = account.email
        activeRoleFlow.value = account.role
        _notifications.value = emptyList()
        _eventGuests.value = emptyList()
        propagateSessionTokenToApi(token)
    }
}
