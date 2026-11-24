package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.core.engine.DateRangeReservation
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.core.engine.UserRole
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.ArticleAvailabilityDay
import com.jeudry.rosafiesta.domain.AuditLogRecord
import com.jeudry.rosafiesta.domain.AvailabilityConflictItem
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
import com.jeudry.rosafiesta.domain.EventTypeCount
import com.jeudry.rosafiesta.domain.EventTypeTemplate
import com.jeudry.rosafiesta.domain.InsurancePolicyRecord
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.MediaUploadAsset
import com.jeudry.rosafiesta.domain.MonthlyAnalyticsBar
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
import com.jeudry.rosafiesta.domain.ReportSeriesPoint
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.RosaChatReply
import com.jeudry.rosafiesta.domain.RosaSketchProposal
import com.jeudry.rosafiesta.domain.SeasonBanner
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.StaffProfile
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome
import com.jeudry.rosafiesta.domain.platform.ReportCsvBuilder
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.security.UserPreferences
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    private val favoritesFlow: MutableStateFlow<Set<String>>? = null,
    private val onReplayPendingOutbox: suspend () -> Unit = {},
    private val onEnqueueOfflineMutation: suspend (
        actionLabel: String,
        entityCode: String,
        operationType: String,
        payloadJson: String,
        idempotencyKey: String,
    ) -> Unit = { _, _, _, _, _ -> },
) {
    private val coordinatorJson: Json = Json { ignoreUnknownKeys = true }
    private val _catalogCategories = MutableStateFlow(
        if (seedDemoData) {
            CanonicalSampleData.articles
                .map { article -> article.category.trim() }
                .filter { categoryName -> categoryName.isNotEmpty() }
                .distinct()
                .map { categoryName ->
                    CatalogCategory(
                        id = slugifyCategoryId(categoryName),
                        name = categoryName,
                        illoKey = defaultIlloKeyForCategoryName(categoryName),
                        itemCount = CanonicalSampleData.articles.count { article ->
                            article.category.equals(categoryName, ignoreCase = true)
                        },
                    )
                }
        } else {
            emptyList()
        },
    )
    val catalogCategories: StateFlow<List<CatalogCategory>> = _catalogCategories.asStateFlow()

    private val _catalogTrends = MutableStateFlow(
        if (seedDemoData) CanonicalSampleData.trends else emptyList(),
    )
    val catalogTrends: StateFlow<List<CatalogTrend>> = _catalogTrends.asStateFlow()

    private val _seasonBanner = MutableStateFlow(
        if (seedDemoData) CanonicalSampleData.seasonBanner else SeasonBanner(),
    )
    val seasonBanner: StateFlow<SeasonBanner> = _seasonBanner.asStateFlow()

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

    private val _eventTasks = MutableStateFlow<List<EventTaskRecord>>(emptyList())
    val eventTasks: StateFlow<List<EventTaskRecord>> = _eventTasks.asStateFlow()

    private val fallbackRealtimeConnected = MutableStateFlow(false)

    private val realtimeClient: RosaFiestaRealtimeClient? by lazy {
        apiClient?.let { client ->
            RosaFiestaRealtimeClient(
                httpClient = client.internalHttpClient(),
                baseUrl = client.internalBaseUrl(),
            )
        }
    }

    val isRealtimeConnected: StateFlow<Boolean>
        get() = realtimeClient?.isConnected ?: fallbackRealtimeConnected.asStateFlow()

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
        runCatching { onReplayPendingOutbox() }
        var anySucceeded = false
        var coreUpdated = false
        var submodulesUpdated = false

        runCatching {
            val response = client.listCategories()
            val remoteCategories = response.categories
            if (remoteCategories != null) {
                _catalogCategories.value = remoteCategories.map { dto ->
                    RosaFiestaBackendMappers.toCatalogCategory(dto)
                }
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.listArticles()
            val remoteArticles = response.articles
            if (remoteArticles != null) {
                val fallbackById = articlesFlow.value.associateBy { article -> article.id }
                val activeCategories = _catalogCategories.value
                val rentalDtos = remoteArticles.filter { dto -> dto.isDirectSale != true }
                articlesFlow.value = rentalDtos.mapIndexed { index, dto ->
                    RosaFiestaBackendMappers.toCatalogArticle(dto, index, fallbackById, activeCategories)
                }
                coreUpdated = true
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.listCatalogTrends(includeInactive = true)
            val remoteTrends = response.trends
            if (remoteTrends != null) {
                _catalogTrends.value = remoteTrends.map { dto ->
                    RosaFiestaBackendMappers.toCatalogTrend(dto)
                }
                anySucceeded = true
            }
        }

        runCatching {
            val response = client.getSeasonBanner()
            _seasonBanner.value = RosaFiestaBackendMappers.toSeasonBanner(response.banner)
            anySucceeded = true
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
            val roleLower = activeRoleFlow.value.trim().lowercase()
            val includeHiddenPackages = if (roleLower == "admin") true else roleLower.startsWith("staff")
            val response = client.listPackages(includeHidden = includeHiddenPackages)
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
                val meOutput = client.fetchCurrentUser()
                val current = currentAuthenticatedUserFlow.value
                if (current != null) {
                    val syncedPrefs = RosaFiestaBackendMappers.toUserPreferences(meOutput.user.preferences)
                    val refreshed = current.copy(
                        fullName = meOutput.user.fullName.ifBlank { current.fullName },
                        phone = meOutput.user.phone.ifBlank { current.phone },
                        birthDate = meOutput.user.birthDate.ifBlank { current.birthDate },
                        role = meOutput.user.role.ifBlank { current.role },
                        preferences = syncedPrefs,
                    )
                    upsertAccountInMemory(refreshed)
                    currentAuthenticatedUserFlow.value = refreshed
                    activeRoleFlow.value = refreshed.role
                    currentUserEmailFlow.value = refreshed.email
                    anySucceeded = true
                }
            }

            if (favoritesFlow != null) {
                runCatching {
                    val favResponse = client.listFavorites()
                    val remoteFavs = favResponse.favorites
                    if (remoteFavs != null) {
                        favoritesFlow.value = remoteFavs.toSet()
                        anySucceeded = true
                    }
                }
            }

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
                        val localPending = offlineQueueFlow.value.filter { item -> !item.synced }
                        val localKeys = localPending.map { item -> item.idempotencyKey }.filter { key -> key.isNotBlank() }.toSet()
                        val remoteMapped = remoteQueue
                            .map { dto -> RosaFiestaBackendMappers.toOfflineQueueItem(dto) }
                            .filterNot { item ->
                                val hasKey = item.idempotencyKey.isNotBlank()
                                if (hasKey) item.idempotencyKey in localKeys else false
                            }
                        offlineQueueFlow.value = localPending + remoteMapped
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

        if (anySucceeded) {
            submodulesUpdated = true
        }
        val shouldPersistSnapshot = if (coreUpdated) true else submodulesUpdated
        if (shouldPersistSnapshot) {
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
            preferences = RosaFiestaBackendMappers.toUserPreferences(remoteUser.preferences),
        )
        upsertAccountInMemory(syncedAccount)
        applyAuthenticatedSession(syncedAccount, remoteResult.accessToken)
        onPersistAccountAndSession(syncedAccount, remoteResult.accessToken)
        runCatching { syncFromBackendAsync() }
        AuthOutcome.authenticated(
            accessToken = remoteResult.accessToken,
            account = syncedAccount,
            refreshToken = remoteResult.refreshToken,
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
            preferences = RosaFiestaBackendMappers.toUserPreferences(remoteUser.preferences),
        )
        upsertAccountInMemory(syncedAccount)
        applyAuthenticatedSession(syncedAccount, remoteResult.accessToken)
        onPersistAccountAndSession(syncedAccount, remoteResult.accessToken)
        runCatching { syncFromBackendAsync() }
        AuthOutcome.authenticated(
            accessToken = remoteResult.accessToken,
            account = syncedAccount,
            refreshToken = remoteResult.refreshToken,
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
        val client = apiClient
        if (client == null) {
            return@withContext OtpChallengeOutcome(
                success = false,
                errorCode = "SMTP_PROVIDER_NOT_CONFIGURED",
                errorMessage = "Servicio de correo no configurado para recuperación de contraseña.",
            )
        }
        try {
            val remote = client.requestOtp(cleanEmail)
            OtpChallengeOutcome(
                success = remote.sent,
                expiresInSeconds = remote.expiresInSeconds.toInt(),
                devCode = remote.devCode.orEmpty(),
            )
        } catch (apiError: RosaFiestaApiException) {
            OtpChallengeOutcome(
                success = false,
                errorCode = apiError.errorCode,
                errorMessage = SensitiveDataRedactor.translateBusinessError(apiError.errorCode, apiError.message),
            )
        } catch (_: Throwable) {
            OtpChallengeOutcome(
                success = false,
                errorCode = "NETWORK_UNREACHABLE",
                errorMessage = "No se pudo contactar al servidor para enviar el código.",
            )
        }
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
        val client = apiClient
        if (client == null) {
            return@withContext OtpChallengeOutcome(
                success = false,
                errorCode = "SMTP_PROVIDER_NOT_CONFIGURED",
                errorMessage = "La verificación de código requiere conexión al servidor.",
            )
        }
        try {
            val remote = client.verifyOtp(cleanEmail, cleanCode)
            OtpChallengeOutcome(
                success = remote.verified,
                expiresInSeconds = remote.expiresInSeconds.toInt(),
                resetToken = remote.resetToken,
            )
        } catch (apiError: RosaFiestaApiException) {
            OtpChallengeOutcome(
                success = false,
                errorCode = apiError.errorCode,
                errorMessage = SensitiveDataRedactor.translateBusinessError(apiError.errorCode, apiError.message),
            )
        } catch (_: Throwable) {
            OtpChallengeOutcome(
                success = false,
                errorCode = "NETWORK_UNREACHABLE",
                errorMessage = "No se pudo verificar el código en el servidor.",
            )
        }
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
        val client = apiClient
        if (client == null) {
            return@withContext localResetFallback(cleanEmail, cleanPassword)
        }
        val remoteResult = executeRemoteCall(missingApiCode = "OTP_RESET_UNAVAILABLE") { activeClient ->
            activeClient.resetPassword(
                ResetPasswordInputDto(
                    email = cleanEmail,
                    identifier = cleanEmail,
                    newPassword = cleanPassword,
                    resetToken = resetToken.ifBlank { null },
                    otpCode = otpCode.ifBlank { null },
                ),
            )
        }
        val remote = remoteResult.getOrElse { error ->
            val apiError = error as? RosaFiestaApiException
            return@withContext AuthOutcome.failure(
                errorCode = apiError?.errorCode ?: "OTP_RESET_FAILED",
                errorMessage = apiError?.message ?: error.message ?: "No se pudo restablecer la contraseña.",
            )
        }
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
            preferences = RosaFiestaBackendMappers.toUserPreferences(user.preferences),
        )
        upsertAccountInMemory(updated)
        applyAuthenticatedSession(updated, remote.accessToken)
        onPersistAccountAndSession(updated, remote.accessToken)
        AuthOutcome.authenticated(
            accessToken = remote.accessToken,
            account = updated,
            refreshToken = remote.refreshToken,
        )
    }

    suspend fun loginWithOAuth(provider: String, idToken: String): AuthOutcome = withContext(ioDispatcher) {
        val cleanProvider = provider.trim().lowercase()
        val cleanIdToken = idToken.trim()
        if (cleanProvider.isEmpty()) {
            return@withContext AuthOutcome.failure("AUTH_OAUTH_INVALID", "Selecciona un proveedor válido.")
        }
        val client = apiClient
        if (client == null) {
            return@withContext AuthOutcome.failure("AUTH_OAUTH_UNAVAILABLE", "El acceso social requiere conexión con el servidor RosaFiesta.")
        }
        val oauthConfig = runCatching { client.getOAuthConfig() }.getOrNull()
        val providerConfigured = when (cleanProvider) {
            "google" -> oauthConfig?.googleEnabled ?: false
            "apple" -> oauthConfig?.appleEnabled ?: false
            else -> false
        }
        if (oauthConfig != null) {
            if (!providerConfigured) {
                return@withContext AuthOutcome.failure(
                    "AUTH_OAUTH_NOT_CONFIGURED",
                    "El proveedor ${cleanProvider.replaceFirstChar { ch -> ch.uppercase() }} aún no está habilitado en el servidor.",
                )
            }
        }
        if (cleanIdToken.isEmpty()) {
            return@withContext AuthOutcome.failure("AUTH_OAUTH_INVALID", "Token de identidad requerido para verificar tu cuenta.")
        }
        val remote = try {
            client.loginWithOAuth(cleanProvider, cleanIdToken)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            return@withContext mapOnlineAuthException(error)
        }
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
            role = user.role.ifBlank { "client" },
            preferences = RosaFiestaBackendMappers.toUserPreferences(user.preferences),
        )
        upsertAccountInMemory(account)
        applyAuthenticatedSession(account, remote.accessToken)
        onPersistAccountAndSession(account, remote.accessToken)
        AuthOutcome.authenticated(
            accessToken = remote.accessToken,
            account = account,
            refreshToken = remote.refreshToken,
        )
    }

    suspend fun startOAuthAuthorization(
        provider: String,
        redirectUri: String,
        codeChallenge: String,
        state: String,
    ): Result<String> = withContext(ioDispatcher) {
        val client = apiClient
            ?: return@withContext Result.failure(IllegalStateException("Servidor RosaFiesta sin conexión."))
        runCatching {
            val response = client.startOAuthAuthorization(provider, redirectUri, codeChallenge, state)
            response.authorizeUrl
        }
    }

    suspend fun exchangeOAuthCode(
        provider: String,
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): AuthOutcome = withContext(ioDispatcher) {
        val cleanProvider = provider.trim().lowercase()
        val client = apiClient
            ?: return@withContext AuthOutcome.failure("AUTH_OAUTH_UNAVAILABLE", "El acceso social requiere conexión con el servidor RosaFiesta.")
        val remote = try {
            client.exchangeOAuthCode(
                OAuthExchangeInputDto(
                    provider = cleanProvider,
                    code = code.trim(),
                    codeVerifier = codeVerifier.trim(),
                    redirectUri = redirectUri.trim(),
                ),
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            return@withContext mapOnlineAuthException(error)
        }
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
            role = user.role.ifBlank { "client" },
            preferences = RosaFiestaBackendMappers.toUserPreferences(user.preferences),
        )
        upsertAccountInMemory(account)
        applyAuthenticatedSession(account, remote.accessToken)
        onPersistAccountAndSession(account, remote.accessToken)
        AuthOutcome.authenticated(
            accessToken = remote.accessToken,
            account = account,
            refreshToken = remote.refreshToken,
        )
    }

    suspend fun refreshSession(refreshToken: String): AuthOutcome = withContext(ioDispatcher) {
        val cleanRefresh = refreshToken.trim()
        if (cleanRefresh.isEmpty()) {
            return@withContext AuthOutcome.failure("AUTH_SESSION_EXPIRED", "Tu sesión expiró. Inicia sesión nuevamente.")
        }
        val client = apiClient
            ?: return@withContext AuthOutcome.failure("AUTH_SESSION_EXPIRED", "Servidor RosaFiesta sin conexión.")
        val remote = try {
            client.refreshAccessToken(cleanRefresh)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            return@withContext mapOnlineAuthException(error)
        }
        val user = remote.user
        val salt = JwtAuthManager.generateDeterministicSalt("${user.email}:refresh")
        val existingAccount = userAccountsFlow.value.firstOrNull { candidate -> candidate.id == user.id }
        val account = RegisteredUserAccount(
            id = user.id,
            username = user.username.ifBlank { user.email.substringBefore("@") },
            email = user.email.trim().lowercase(),
            fullName = user.fullName,
            passwordHash = existingAccount?.passwordHash.orEmpty(),
            salt = existingAccount?.salt ?: salt,
            birthDate = user.birthDate,
            phone = user.phone,
            role = user.role.ifBlank { "client" },
            preferences = RosaFiestaBackendMappers.toUserPreferences(user.preferences),
        )
        upsertAccountInMemory(account)
        applyAuthenticatedSession(account, remote.accessToken)
        onPersistAccountAndSession(account, remote.accessToken)
        AuthOutcome.authenticated(
            accessToken = remote.accessToken,
            account = account,
            refreshToken = remote.refreshToken,
        )
    }

    fun setRefreshToken(token: String?) {
        apiClient?.setRefreshToken(token)
    }

    fun setSessionLifecycleCallbacks(
        onTokensRefreshed: suspend (accessToken: String, refreshToken: String) -> Unit,
        onSessionExpired: suspend () -> Unit,
    ) {
        apiClient?.setSessionLifecycleCallbacks(
            onTokensRefreshed = { accessToken, refreshToken ->
                currentSessionJwtFlow.value = accessToken
                propagateSessionTokenToApi(accessToken)
                onTokensRefreshed(accessToken, refreshToken)
            },
            onSessionExpired = onSessionExpired,
        )
    }

    suspend fun registerDevicePushToken(
        platform: String,
        token: String,
        appVariant: String,
    ): Result<Boolean> = withContext(ioDispatcher) {
        val client = apiClient ?: return@withContext Result.success(false)
        runCatching {
            val response = client.registerDevicePushToken(
                PushTokenInputDto(
                    platform = platform.trim().lowercase(),
                    token = token.trim(),
                    appVariant = appVariant.trim().ifBlank { "personal" },
                ),
            )
            response.registered
        }
    }

    suspend fun unregisterDevicePushToken(token: String = ""): Result<Boolean> = withContext(ioDispatcher) {
        val client = apiClient ?: return@withContext Result.success(false)
        runCatching {
            val response = client.unregisterDevicePushToken(token)
            response.disabled
        }
    }

    suspend fun changeUserPasswordOnline(
        currentPassword: String,
        newPassword: String,
    ): AuthOutcome = withContext(ioDispatcher) {
        val cleanCurrent = currentPassword.trim()
        val cleanNew = newPassword.trim()
        val passwordError = InputValidator.validatePassword(cleanNew)
        if (passwordError != null) {
            return@withContext AuthOutcome.failure("AUTH_WEAK_PASSWORD", passwordError)
        }
        val currentAccount = currentAuthenticatedUserFlow.value ?: return@withContext AuthOutcome.failure(
            errorCode = "AUTH_UNAUTHORIZED",
            errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
        )
        val client = apiClient
        val shouldCallServer = if (client != null) hasRealServerToken(client) else false
        if (shouldCallServer) {
            val remoteResult = executeRemoteCall(missingApiCode = "PASSWORD_CHANGE_UNAVAILABLE") { activeClient ->
                activeClient.changePassword(
                    ChangePasswordInputDto(
                        currentPassword = cleanCurrent,
                        newPassword = cleanNew,
                    ),
                )
            }
            if (remoteResult.isFailure) {
                val err = remoteResult.exceptionOrNull()
                val apiErr = err as? RosaFiestaApiException
                return@withContext AuthOutcome.failure(
                    errorCode = apiErr?.errorCode ?: "PASSWORD_CHANGE_FAILED",
                    errorMessage = apiErr?.message ?: err?.message ?: "No se pudo actualizar la contraseña.",
                )
            }
            return@withContext applyLocalPasswordUpdate(currentAccount, cleanNew)
        }
        changeUserPassword(cleanCurrent, cleanNew)
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
        return applyLocalPasswordUpdate(currentAccount, cleanNew)
    }

    private fun applyLocalPasswordUpdate(
        currentAccount: RegisteredUserAccount,
        newPassword: String,
    ): AuthOutcome {
        val nextSalt = JwtAuthManager.generateDeterministicSalt("${currentAccount.email}:changed")
        val nextHash = JwtAuthManager.hashPassword(newPassword, nextSalt)
        val updatedAccount = currentAccount.copy(passwordHash = nextHash, salt = nextSalt)
        upsertAccountInMemory(updatedAccount)
        val nextToken = currentSessionJwtFlow.value ?: JwtAuthManager.issueAccessToken(updatedAccount)
        applyAuthenticatedSession(updatedAccount, nextToken)
        scope.launch {
            onPersistAccountAndSession(updatedAccount, nextToken)
        }
        return AuthOutcome.authenticated(nextToken, updatedAccount)
    }

    internal fun clearUserScopedFlows() {
        _quotes.value = emptyList()
        _deliveryStops.value = emptyList()
        _damageIncidents.value = emptyList()
        _notifications.value = emptyList()
        _clients.value = emptyList()
        _refundRequests.value = emptyList()
        _leads.value = emptyList()
        _chatThreads.value = emptyList()
        _threadMessages.value = emptyList()
        _eventGuests.value = emptyList()
        _eventSchedule.value = emptyList()
        _insurancePolicies.value = emptyList()
        _auditLogs.value = emptyList()
        _botFaqs.value = emptyList()
        _botKeywords.value = emptyList()
        _botThreads.value = emptyList()
        _eventTasks.value = emptyList()
        _suppliers.value = emptyList()
    }

    fun logoutSession(localClear: () -> Unit) {
        val client = apiClient
        val currentToken = client?.currentAccessToken()?.trim().orEmpty()
        val currentRefresh = client?.currentRefreshToken()?.trim().orEmpty()
        clearUserScopedFlows()
        localClear()
        if (client != null) {
            val hasToken = currentToken.isNotEmpty()
            val hasRefresh = currentRefresh.isNotEmpty()
            val shouldCallRemoteLogout = if (hasToken) true else hasRefresh
            if (shouldCallRemoteLogout) {
                scope.launch {
                    runCatching {
                        client.logout(
                            refreshToken = currentRefresh,
                            bearerToken = currentToken,
                        )
                    }
                    client.setAccessToken(null)
                    client.setRefreshToken(null)
                }
            } else {
                client.setAccessToken(null)
                client.setRefreshToken(null)
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
        notes: String = "",
        sendImmediately: Boolean = false,
    ): QuoteRecord {
        val nextNumber = 1200 + _quotes.value.size + 1
        val subtotal = lines.sumOf { line -> line.subtotalDop }
        val total = (subtotal + deliveryFeeDop - discountDop).coerceAtLeast(0)
        val depositPercent = _businessSettings.value.depositPercent
        val deposit = (total * depositPercent) / 100
        val cleanNotes = notes.trim()
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
            status = if (sendImmediately) "SENT" else "PENDING",
            lines = lines,
            deliveryFeeDop = deliveryFeeDop,
            discountDop = discountDop,
            totalDop = total,
            depositPercent = depositPercent,
            depositDop = deposit,
            notes = cleanNotes,
        )
        _quotes.update { existing -> listOf(created) + existing }
        recomputeHonestMetrics()
        scope.launch {
            onPersistCoreSync()
            val dtoLines = lines.map { line ->
                QuoteLineDto(
                    articleCode = line.articleCode,
                    name = line.name,
                    quantity = line.quantity.toLong(),
                    unitPriceRD = line.unitPriceDop.toLong(),
                )
            }
            val payloadDto = CreateQuoteInputDto(
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
                notes = cleanNotes.ifBlank { null },
                sendImmediately = if (sendImmediately) true else null,
            )
            val remoteAttempt = runCatching {
                apiClient?.createQuote(payloadDto)
            }
            val response = remoteAttempt.getOrNull()
            if (response != null) {
                val mapped = RosaFiestaBackendMappers.toQuoteRecord(response.quote)
                _quotes.update { list ->
                    list.map { item -> if (item.id == created.id) mapped else item }
                }
                onPersistCoreSync()
            } else {
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Crear cotización ${created.code}",
                        created.id,
                        "create_quote",
                        coordinatorJson.encodeToString(payloadDto),
                        "idem-quote-create-${created.id}",
                    )
                }
            }
        }
        return created
    }

    fun sendQuote(
        quoteId: String,
        eventTitle: String = "",
        venue: String = "",
        dateLabel: String = "",
        guests: Int = 0,
        lines: List<EventLineItem> = emptyList(),
        deliveryFeeDop: Int? = null,
        discountDop: Int? = null,
        notes: String = "",
    ): QuoteRecord? {
        val targetId = quoteId.trim()
        var updatedRecord: QuoteRecord? = null
        val depositPercent = _businessSettings.value.depositPercent
        _quotes.update { list ->
            list.map { quote ->
                if (quote.id == targetId) {
                    val enriched = applyQuoteEnrichment(
                        quote = quote,
                        eventTitle = eventTitle,
                        venue = venue,
                        dateLabel = dateLabel,
                        guests = guests,
                        lines = lines,
                        deliveryFeeDop = deliveryFeeDop,
                        discountDop = discountDop,
                        notes = notes,
                        depositPercent = depositPercent,
                    )
                    updatedRecord = enriched
                    enriched
                } else {
                    quote
                }
            }
        }
        recomputeHonestMetrics()
        if (updatedRecord != null) {
            scope.launch {
                onPersistCoreSync()
                val dtoLines = lines.takeIf { items -> items.isNotEmpty() }?.map { line ->
                    QuoteLineDto(
                        articleCode = line.articleCode,
                        name = line.name,
                        quantity = line.quantity.toLong(),
                        unitPriceRD = line.unitPriceDop.toLong(),
                    )
                }
                val payloadDto = SendQuoteInputDto(
                    eventTitle = eventTitle.trim().ifBlank { null },
                    venue = venue.trim().ifBlank { null },
                    eventDateLabel = dateLabel.trim().ifBlank { null },
                    guests = if (guests > 0) guests.toLong() else null,
                    lines = dtoLines,
                    deliveryFeeRD = deliveryFeeDop?.toLong(),
                    discountRD = discountDop?.toLong(),
                    notes = notes.trim().ifBlank { null },
                )
                val remoteAttempt = runCatching {
                    apiClient?.sendQuote(id = targetId, input = payloadDto)
                }
                val response = remoteAttempt.getOrNull()
                if (response != null) {
                    val mapped = RosaFiestaBackendMappers.toQuoteRecord(response.quote)
                    _quotes.update { list ->
                        list.map { item -> if (item.id == targetId) mapped else item }
                    }
                    onPersistCoreSync()
                } else {
                    val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                    if (shouldQueueOffline) {
                        onEnqueueOfflineMutation(
                            "Enviar cotización $targetId",
                            targetId,
                            "send_quote",
                            coordinatorJson.encodeToString(payloadDto),
                            "idem-quote-send-$targetId",
                        )
                    }
                }
            }
        }
        return updatedRecord
    }

    private fun applyQuoteEnrichment(
        quote: QuoteRecord,
        eventTitle: String,
        venue: String,
        dateLabel: String,
        guests: Int,
        lines: List<EventLineItem>,
        deliveryFeeDop: Int?,
        discountDop: Int?,
        notes: String,
        depositPercent: Int,
    ): QuoteRecord {
        val nextTitle = eventTitle.trim().ifBlank { quote.eventTitle }
        val nextVenue = venue.trim().ifBlank { quote.venue }
        val nextDateLabel = dateLabel.trim().ifBlank { quote.dateLabel }
        val nextGuests = if (guests > 0) guests else quote.guests
        val nextLines = if (lines.isNotEmpty()) lines else quote.lines
        val nextDelivery = deliveryFeeDop ?: quote.deliveryFeeDop
        val nextDiscount = discountDop ?: quote.discountDop
        val nextNotes = notes.trim().ifBlank { quote.notes }
        val subtotal = nextLines.sumOf { line -> line.subtotalDop }
        val nextTotal = (subtotal + nextDelivery - nextDiscount).coerceAtLeast(0)
        val nextDeposit = (nextTotal * depositPercent) / 100
        return quote.copy(
            eventTitle = nextTitle,
            venue = nextVenue,
            dateLabel = nextDateLabel,
            guests = nextGuests,
            lines = nextLines,
            deliveryFeeDop = nextDelivery,
            discountDop = nextDiscount,
            totalDop = nextTotal,
            depositPercent = depositPercent,
            depositDop = nextDeposit,
            status = "SENT",
            rejectionReason = "",
            notes = nextNotes,
        )
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
                onPersistCoreSync()
                val payloadDto = SignContractInputDto(
                    signedByName = signedByName.trim(),
                    signatureSvg = signatureSvg.trim(),
                )
                val remoteAttempt = runCatching {
                    apiClient?.signContract(id = targetId, input = payloadDto)
                }
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Firmar contrato $targetId",
                        targetId,
                        "approve_quote",
                        coordinatorJson.encodeToString(payloadDto),
                        "idem-quote-approve-$targetId",
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
                onPersistCoreSync()
                val payloadDto = DeclineQuoteInputDto(reason = reason.trim())
                val remoteAttempt = runCatching {
                    apiClient?.declineQuote(id = targetId, input = payloadDto)
                }
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Rechazar cotización $targetId",
                        targetId,
                        "decline_quote",
                        coordinatorJson.encodeToString(payloadDto),
                        "idem-quote-decline-$targetId",
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
            onPersistCoreSync()
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
        val client = apiClient
        if (client != null) {
            try {
                val remote = client.sendRosaChat(
                    RosaChatInputDto(
                        message = cleanMessage,
                        eventType = eventType,
                        guests = guests.toLong(),
                        budgetRD = budgetDop.toLong(),
                        excludedCategories = excludedCategories.takeIf { list -> list.isNotEmpty() },
                    ),
                )
                return@withContext RosaFiestaBackendMappers.toRosaChatReply(remote)
            } catch (apiError: RosaFiestaApiException) {
                return@withContext RosaChatReply(
                    reply = SensitiveDataRedactor.translateBusinessError(apiError.errorCode, apiError.message),
                    intent = "ERROR",
                    styleTitle = "",
                    estimatedTotalDop = 0,
                    withinBudget = false,
                    suggestedLines = emptyList(),
                )
            } catch (_: Throwable) {
                return@withContext RosaChatReply(
                    reply = "No se pudo contactar el servicio de Rosa IA en este momento.",
                    intent = "ERROR",
                    styleTitle = "",
                    estimatedTotalDop = 0,
                    withinBudget = false,
                    suggestedLines = emptyList(),
                )
            }
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
            styleTitle = "$eventType · Propuesta personalizada",
            estimatedTotalDop = proposal.totalDop,
            withinBudget = fitsBudget,
            suggestedLines = lines,
        )
    }

    suspend fun transcribeVoicePrompt(wavBase64: String, locale: String): VoiceTranscriptionOutcome = withContext(ioDispatcher) {
        val client = apiClient
        if (client == null) {
            return@withContext VoiceTranscriptionOutcome(
                transcript = "",
                locale = locale,
                voiceDetected = false,
                durationMs = 0,
                provider = "STT_PROVIDER_NOT_CONFIGURED:Proveedor de transcripción por voz no configurado.",
            )
        }
        try {
            val remote = client.transcribeRosaAudio(wavBase64 = wavBase64, locale = locale)
            RosaFiestaBackendMappers.toVoiceTranscriptionOutcome(remote)
        } catch (apiError: RosaFiestaApiException) {
            val friendly = SensitiveDataRedactor.translateBusinessError(apiError.errorCode, apiError.message)
            VoiceTranscriptionOutcome(
                transcript = "",
                locale = locale,
                voiceDetected = false,
                durationMs = 0,
                provider = "${apiError.errorCode}:$friendly",
            )
        } catch (_: Throwable) {
            VoiceTranscriptionOutcome(
                transcript = "",
                locale = locale,
                voiceDetected = false,
                durationMs = 0,
                provider = "NETWORK_UNREACHABLE:No se pudo transcribir el audio.",
            )
        }
    }

    fun approveRefundWithOptions(
        refundId: String,
        options: RefundApprovalOptions,
    ): RefundRequest? {
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
                onPersistCoreSync()
                runCatching {
                    val response = apiClient?.approveRefund(
                        refundId = targetId,
                        input = ApproveRefundInputDto(
                            approvedBy = approver,
                            method = options.method,
                            releaseInventory = options.releaseInventory,
                            notifyWhatsapp = options.notifyWhatsApp,
                            clientMessage = options.clientMessage,
                        ),
                    )
                    if (response != null) {
                        val fallbackMap = _refundRequests.value.associateBy { req -> req.id }
                        val mapped = RosaFiestaBackendMappers.toRefundRequest(
                            dto = response.refund,
                            fallbackById = fallbackMap,
                        )
                        _refundRequests.update { current ->
                            current.map { candidate -> if (candidate.id == targetId) mapped else candidate }
                        }
                        onPersistCoreSync()
                    }
                }
            }
        }
        return approvedItem
    }

    fun approveRefund(refundId: String): RefundRequest? = approveRefundWithOptions(refundId = refundId, options = RefundApprovalOptions())

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
            onPersistCoreSync()
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
                onPersistCoreSync()
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
            onPersistCoreSync()
            runCatching {
                val response = apiClient?.createSupplier(
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
                if (response != null) {
                    val mapped = RosaFiestaBackendMappers.toSupplierRecord(response.supplier)
                    _suppliers.update { list ->
                        list.map { candidate -> if (candidate.id == created.id) mapped else candidate }
                    }
                    onPersistCoreSync()
                }
            }
        }
        return created
    }

    fun updateSupplier(
        supplierId: String,
        name: String,
        category: String,
        contactName: String,
        phone: String,
        email: String,
        website: String,
        notes: String,
    ): SupplierRecord? {
        val targetId = supplierId.trim()
        var updatedSupplier: SupplierRecord? = null
        _suppliers.update { list ->
            list.map { sup ->
                if (sup.id == targetId) {
                    val next = sup.copy(
                        name = name.trim().ifBlank { sup.name },
                        category = category.trim().ifBlank { sup.category },
                        contactName = contactName.trim(),
                        phone = phone.trim().ifBlank { sup.phone },
                        email = email.trim(),
                        website = website.trim(),
                        notes = notes.trim(),
                    )
                    updatedSupplier = next
                    next
                } else {
                    sup
                }
            }
        }
        val persisted = updatedSupplier ?: return null
        scope.launch {
            onPersistCoreSync()
            runCatching {
                val response = apiClient?.patchSupplier(
                    id = targetId,
                    input = PatchSupplierInputDto(
                        name = persisted.name,
                        category = persisted.category,
                        specialty = persisted.specialty,
                        contactName = persisted.contactName,
                        phone = persisted.phone,
                        email = persisted.email,
                        website = persisted.website,
                        rating = persisted.rating,
                        notes = persisted.notes,
                    ),
                )
                if (response != null) {
                    val mapped = RosaFiestaBackendMappers.toSupplierRecord(response.supplier)
                    _suppliers.update { list ->
                        list.map { item -> if (item.id == targetId) mapped else item }
                    }
                }
            }
        }
        return persisted
    }

    fun deleteSupplier(supplierId: String): Boolean {
        val targetId = supplierId.trim()
        val existed = _suppliers.value.any { item -> item.id == targetId }
        if (!existed) return false
        _suppliers.update { list -> list.filterNot { item -> item.id == targetId } }
        scope.launch {
            onPersistCoreSync()
            runCatching {
                apiClient?.deleteSupplier(targetId)
            }
        }
        return true
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
            onPersistCoreSync()
            runCatching {
                val response = apiClient?.createLead(
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
                if (response != null) {
                    val mapped = RosaFiestaBackendMappers.toLeadRecord(response.lead)
                    _leads.update { list ->
                        list.map { item -> if (item.id == created.id) mapped else item }
                    }
                }
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
                onPersistCoreSync()
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
            onPersistCoreSync()
            val payloadDto = AddGuestInputDto(
                fullName = created.fullName,
                rsvpStatus = created.rsvpStatus,
                tableLabel = created.tableLabel,
                dietaryNotes = created.notes.ifBlank { null },
            )
            val remoteAttempt = runCatching {
                apiClient?.addEventGuest(eventId = cleanEventId, input = payloadDto)
            }
            val response = remoteAttempt.getOrNull()
            if (response != null) {
                val mapped = RosaFiestaBackendMappers.toEventGuest(response.guest)
                _eventGuests.update { list ->
                    list.map { item -> if (item.id == created.id) mapped else item }
                }
                onPersistCoreSync()
            } else {
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Agregar invitado ${created.fullName}",
                        cleanEventId,
                        "add_guest",
                        coordinatorJson.encodeToString(payloadDto),
                        "idem-guest-add-$cleanEventId-${created.id}",
                    )
                }
            }
        }
        return created
    }

    fun deleteEventGuest(eventId: String, guestId: String): Boolean {
        val cleanEventId = eventId.trim()
        val cleanGuestId = guestId.trim()
        val existed = _eventGuests.value.any { guest -> guest.id == cleanGuestId }
        if (!existed) return false
        _eventGuests.update { list -> list.filterNot { guest -> guest.id == cleanGuestId } }
        scope.launch {
            onPersistCoreSync()
            val remoteAttempt = runCatching {
                apiClient?.deleteEventGuest(cleanEventId, cleanGuestId)
            }
            val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
            if (shouldQueueOffline) {
                onEnqueueOfflineMutation(
                    "Eliminar invitado $cleanGuestId",
                    cleanEventId,
                    "delete_guest",
                    """{"eventId":"$cleanEventId","guestId":"$cleanGuestId"}""",
                    "idem-guest-del-$cleanEventId-$cleanGuestId",
                )
            }
        }
        return true
    }

    suspend fun loadEventTasks(eventId: String): List<EventTaskRecord> = withContext(ioDispatcher) {
        val cleanEventId = eventId.trim()
        val client = apiClient ?: return@withContext _eventTasks.value.filter { task -> task.eventId == cleanEventId }
        val loaded = runCatching {
            val response = client.listEventTasks(cleanEventId)
            response.tasks.orEmpty().map { dto -> RosaFiestaBackendMappers.toEventTaskRecord(dto) }
        }.getOrNull()
        if (loaded != null) {
            _eventTasks.update { existing ->
                existing.filterNot { task -> task.eventId == cleanEventId } + loaded
            }
            onPersistCoreSync()
            return@withContext loaded
        }
        _eventTasks.value.filter { task -> task.eventId == cleanEventId }
    }

    fun createEventTask(eventId: String, title: String, assigneeName: String): EventTaskRecord? {
        val cleanEventId = eventId.trim()
        val cleanTitle = title.trim()
        if (cleanEventId.isEmpty()) return null
        if (cleanTitle.isEmpty()) return null
        val created = EventTaskRecord(
            id = "task-${_eventTasks.value.size + 1}-${cleanTitle.hashCode()}",
            eventId = cleanEventId,
            title = cleanTitle,
            assigneeName = assigneeName.trim(),
            done = false,
        )
        _eventTasks.update { list -> list + created }
        scope.launch {
            onPersistCoreSync()
            val payloadDto = CreateEventTaskInputDto(
                title = cleanTitle,
                assigneeName = created.assigneeName,
                done = false,
            )
            val remoteAttempt = runCatching {
                apiClient?.createEventTask(eventId = cleanEventId, input = payloadDto)
            }
            val response = remoteAttempt.getOrNull()
            if (response != null) {
                val mapped = RosaFiestaBackendMappers.toEventTaskRecord(response.task)
                _eventTasks.update { list ->
                    list.map { item -> if (item.id == created.id) mapped else item }
                }
                onPersistCoreSync()
            } else {
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Crear tarea ${created.title}",
                        cleanEventId,
                        "create_task",
                        coordinatorJson.encodeToString(payloadDto),
                        "idem-task-create-$cleanEventId-${created.id}",
                    )
                }
            }
        }
        return created
    }

    fun toggleEventTask(eventId: String, taskId: String, done: Boolean): EventTaskRecord? {
        val cleanEventId = eventId.trim()
        val cleanTaskId = taskId.trim()
        var updatedTask: EventTaskRecord? = null
        _eventTasks.update { list ->
            list.map { item ->
                if (item.id == cleanTaskId) {
                    val next = item.copy(done = done)
                    updatedTask = next
                    next
                } else {
                    item
                }
            }
        }
        val persisted = updatedTask ?: return null
        scope.launch {
            onPersistCoreSync()
            val payloadDto = UpdateEventTaskInputDto(done = done)
            val remoteAttempt = runCatching {
                apiClient?.updateEventTask(
                    eventId = cleanEventId,
                    taskId = cleanTaskId,
                    input = payloadDto,
                )
            }
            val response = remoteAttempt.getOrNull()
            if (response != null) {
                val mapped = RosaFiestaBackendMappers.toEventTaskRecord(response.task)
                _eventTasks.update { list ->
                    list.map { item -> if (item.id == cleanTaskId) mapped else item }
                }
                onPersistCoreSync()
            } else {
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Actualizar tarea $cleanTaskId",
                        cleanEventId,
                        "toggle_task",
                        coordinatorJson.encodeToString(payloadDto),
                        "idem-task-toggle-$cleanEventId-$cleanTaskId-$done",
                    )
                }
            }
        }
        return persisted
    }

    fun deleteEventTask(eventId: String, taskId: String): Boolean {
        val cleanEventId = eventId.trim()
        val cleanTaskId = taskId.trim()
        val existed = _eventTasks.value.any { item -> item.id == cleanTaskId }
        if (!existed) return false
        _eventTasks.update { list -> list.filterNot { item -> item.id == cleanTaskId } }
        scope.launch {
            onPersistCoreSync()
            val remoteAttempt = runCatching {
                apiClient?.deleteEventTask(cleanEventId, cleanTaskId)
            }
            val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
            if (shouldQueueOffline) {
                onEnqueueOfflineMutation(
                    "Eliminar tarea $cleanTaskId",
                    cleanEventId,
                    "delete_task",
                    """{"eventId":"$cleanEventId","taskId":"$cleanTaskId"}""",
                    "idem-task-del-$cleanEventId-$cleanTaskId",
                )
            }
        }
        return true
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
                onPersistCoreSync()
                val payloadDto = UpdateGuestInputDto(
                    fullName = fullName?.trim(),
                    rsvpStatus = rsvpStatus?.trim(),
                    tableLabel = tableLabel?.trim(),
                    dietaryNotes = notes?.trim(),
                )
                val remoteAttempt = runCatching {
                    apiClient?.updateEventGuest(
                        eventId = eventId.trim(),
                        guestId = targetId,
                        input = payloadDto,
                    )
                }
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Actualizar invitado $targetId",
                        eventId.trim(),
                        "update_guest",
                        coordinatorJson.encodeToString(payloadDto),
                        "idem-guest-upd-${eventId.trim()}-$targetId",
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
            onPersistCoreSync()
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
            onPersistCoreSync()
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
            onPersistCoreSync()
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
            onPersistCoreSync()
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
    ): StaffMember = inviteStaffMemberWithPassword(
        fullName = fullName,
        email = email,
        roleLabel = roleLabel,
        role = role,
        phone = phone,
        permissions = permissions,
        initialPassword = "",
    )

    fun inviteStaffMemberWithPassword(
        fullName: String,
        email: String,
        roleLabel: String,
        role: String,
        phone: String,
        permissions: List<String>,
        initialPassword: String,
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
        val cleanInitialPassword = initialPassword.trim().ifBlank { null }
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
                        initialPassword = cleanInitialPassword,
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
                onPersistCoreSync()
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
            onPersistCoreSync()
            runCatching {
                val response = apiClient?.createCatalogPackage(
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
                if (response != null) {
                    val mapped = RosaFiestaBackendMappers.toEventPackage(
                        dto = response.packageBundle,
                        fallbackById = mapOf(response.packageBundle.id to created),
                    )
                    _packages.update { list ->
                        list.map { item -> if (item.id == created.id) mapped else item }
                    }
                    onPersistCoreSync()
                }
            }
        }
        return created
    }

    fun deletePackage(packageId: String): Boolean {
        val targetId = packageId.trim()
        val existed = _packages.value.any { pkg -> pkg.id == targetId }
        if (!existed) return false
        _packages.update { list -> list.filterNot { pkg -> pkg.id == targetId } }
        scope.launch {
            onPersistCoreSync()
            runCatching {
                apiClient?.deleteCatalogPackage(targetId)
            }
        }
        return true
    }

    fun deleteStaffMember(staffId: String): Boolean {
        val targetId = staffId.trim()
        val existed = staffFlow.value.any { member -> member.id == targetId }
        if (!existed) return false
        staffFlow.update { list -> list.filterNot { member -> member.id == targetId } }
        scope.launch {
            onPersistCoreSync()
            runCatching {
                apiClient?.deleteStaffMember(targetId)
            }
        }
        return true
    }

    fun deleteBotFaq(faqId: String): Boolean {
        val targetId = faqId.trim()
        val existed = _botFaqs.value.any { item -> item.id == targetId }
        if (!existed) return false
        _botFaqs.update { list -> list.filterNot { item -> item.id == targetId } }
        scope.launch {
            onPersistCoreSync()
            runCatching {
                apiClient?.deleteBotFaq(targetId)
            }
        }
        return true
    }

    fun deleteBotKeyword(keywordId: String): Boolean {
        val targetId = keywordId.trim()
        val existed = _botKeywords.value.any { item -> item.id == targetId }
        if (!existed) return false
        _botKeywords.update { list -> list.filterNot { item -> item.id == targetId } }
        scope.launch {
            onPersistCoreSync()
            runCatching {
                apiClient?.deleteBotKeyword(targetId)
            }
        }
        return true
    }

    suspend fun downloadQuoteContractPdf(quoteId: String): Result<ByteArray> = withContext(ioDispatcher) {
        executeRemoteCall(missingApiCode = "CONTRACT_PDF_UNAVAILABLE") { client ->
            client.downloadQuoteContractPdf(quoteId)
        }
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
                onPersistCoreSync()
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

    suspend fun createCategoryAsync(
        name: String,
        illoKey: String,
    ): Result<CatalogCategory> = withContext(ioDispatcher) {
        val cleanName = name.trim()
        val validationError = InputValidator.validateCategoryName(cleanName, isRequired = true)
        if (validationError != null) {
            return@withContext Result.failure(IllegalArgumentException(validationError))
        }
        val cleanIlloKey = illoKey.trim().ifEmpty { "cat-default" }
        val existingDuplicate = _catalogCategories.value.any { candidate ->
            candidate.name.equals(cleanName, ignoreCase = true)
        }
        if (existingDuplicate) {
            return@withContext Result.failure(IllegalStateException("Ya existe una categoría llamada \"$cleanName\"."))
        }

        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    val response = client.createCatalogCategory(
                        SaveCategoryInputDto(
                            name = cleanName,
                            illoKey = cleanIlloKey,
                        ),
                    )
                    val mapped = RosaFiestaBackendMappers.toCatalogCategory(response.category)
                    _catalogCategories.update { list -> upsertCategoryInList(list, mapped) }
                    Result.success(mapped)
                } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }

        val localCreated = CatalogCategory(
            id = slugifyCategoryId(cleanName),
            name = cleanName,
            illoKey = cleanIlloKey,
            itemCount = articlesFlow.value.count { article ->
                article.category.equals(cleanName, ignoreCase = true)
            },
        )
        _catalogCategories.update { list -> upsertCategoryInList(list, localCreated) }
        Result.success(localCreated)
    }

    suspend fun updateCategoryAsync(
        categoryId: String,
        name: String,
        illoKey: String,
    ): Result<CatalogCategory> = withContext(ioDispatcher) {
        val targetId = categoryId.trim()
        val cleanName = name.trim()
        if (targetId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Categoría inválida."))
        }
        val validationError = InputValidator.validateCategoryName(cleanName, isRequired = true)
        if (validationError != null) {
            return@withContext Result.failure(IllegalArgumentException(validationError))
        }
        val cleanIlloKey = illoKey.trim().ifEmpty { "cat-default" }
        val duplicateOther = _catalogCategories.value.any { candidate ->
            val sameName = candidate.name.equals(cleanName, ignoreCase = true)
            val differentId = !candidate.id.equals(targetId, ignoreCase = true)
            if (sameName) differentId else false
        }
        if (duplicateOther) {
            return@withContext Result.failure(IllegalStateException("Ya existe otra categoría llamada \"$cleanName\"."))
        }
        val previousCategory = _catalogCategories.value.firstOrNull { candidate ->
            candidate.id.equals(targetId, ignoreCase = true)
        }

        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    val response = client.updateCatalogCategory(
                        categoryId = targetId,
                        input = UpdateCategoryInputDto(
                            name = cleanName,
                            illoKey = cleanIlloKey,
                        ),
                    )
                    val mapped = RosaFiestaBackendMappers.toCatalogCategory(response.category)
                    _catalogCategories.update { list -> upsertCategoryInList(list, mapped) }
                    relinkLocalArticlesCategoryName(previousCategory?.name, mapped.name)
                    Result.success(mapped)
                } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }

        val updatedLocal = CatalogCategory(
            id = targetId,
            name = cleanName,
            illoKey = cleanIlloKey,
            itemCount = previousCategory?.itemCount ?: 0,
        )
        _catalogCategories.update { list -> upsertCategoryInList(list, updatedLocal) }
        relinkLocalArticlesCategoryName(previousCategory?.name, updatedLocal.name)
        Result.success(updatedLocal)
    }

    suspend fun deleteCategoryAsync(categoryId: String): Result<Unit> = withContext(ioDispatcher) {
        val targetId = categoryId.trim()
        if (targetId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Categoría inválida."))
        }
        val targetCategory = _catalogCategories.value.firstOrNull { candidate ->
            candidate.id.equals(targetId, ignoreCase = true)
        } ?: return@withContext Result.failure(IllegalStateException("La categoría seleccionada no existe."))

        val hasLinkedArticles = articlesFlow.value.any { article ->
            val matchesName = article.category.equals(targetCategory.name, ignoreCase = true)
            val matchesId = article.category.equals(targetCategory.id, ignoreCase = true)
            if (matchesName) true else matchesId
        }
        if (hasLinkedArticles) {
            return@withContext Result.failure(
                IllegalStateException("No puedes eliminar \"${targetCategory.name}\" porque tiene artículos asociados."),
            )
        }

        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    client.deleteCatalogCategory(targetCategory.id)
                    _catalogCategories.update { list ->
                        list.filterNot { candidate -> candidate.id.equals(targetCategory.id, ignoreCase = true) }
                    }
                    Result.success(Unit)
                } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }

        _catalogCategories.update { list ->
            list.filterNot { candidate -> candidate.id.equals(targetCategory.id, ignoreCase = true) }
        }
        Result.success(Unit)
    }

    suspend fun createTrendAsync(trend: CatalogTrend): Result<CatalogTrend> = withContext(ioDispatcher) {
        val cleanTitle = trend.title.trim()
        val validationError = InputValidator.validateTrendTitle(cleanTitle, isRequired = true)
        if (validationError != null) {
            return@withContext Result.failure(IllegalArgumentException(validationError))
        }
        val duplicateTitle = _catalogTrends.value.any { candidate ->
            candidate.title.equals(cleanTitle, ignoreCase = true)
        }
        if (duplicateTitle) {
            return@withContext Result.failure(IllegalStateException("Ya existe una tendencia llamada \"$cleanTitle\"."))
        }
        val requestDto = SaveTrendInputDto(
            id = trend.id.trim().ifEmpty { null },
            title = cleanTitle,
            eventsLabel = trend.eventsLabel.trim(),
            photoKey = trend.photo.key,
            photoUrl = trend.photoUrl.trim().ifEmpty { null },
            photoBgHex = trend.photoBgHex.trim().ifEmpty { "#F3EEF6" },
            linkedCategory = trend.linkedCategory.trim().ifEmpty { null },
            linkedPackageId = trend.linkedPackageId.trim().ifEmpty { null },
            sortOrder = trend.sortOrder,
            active = trend.active,
        )
        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    val response = client.createCatalogTrend(requestDto)
                    val mapped = RosaFiestaBackendMappers.toCatalogTrend(response.trend)
                    _catalogTrends.update { list -> upsertTrendInList(list, mapped) }
                    Result.success(mapped)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }
        val localCreated = trend.copy(
            id = trend.id.trim().ifEmpty { "trend-${slugifyCategoryId(cleanTitle)}" },
            title = cleanTitle,
            eventsLabel = trend.eventsLabel.trim(),
            photoUrl = trend.photoUrl.trim(),
        )
        _catalogTrends.update { list -> upsertTrendInList(list, localCreated) }
        Result.success(localCreated)
    }

    suspend fun updateTrendAsync(trend: CatalogTrend): Result<CatalogTrend> = withContext(ioDispatcher) {
        val targetId = trend.id.trim()
        val cleanTitle = trend.title.trim()
        if (targetId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Tendencia inválida."))
        }
        val validationError = InputValidator.validateTrendTitle(cleanTitle, isRequired = true)
        if (validationError != null) {
            return@withContext Result.failure(IllegalArgumentException(validationError))
        }
        val duplicateOther = _catalogTrends.value.any { candidate ->
            val sameTitle = candidate.title.equals(cleanTitle, ignoreCase = true)
            val differentId = !candidate.id.equals(targetId, ignoreCase = true)
            if (sameTitle) differentId else false
        }
        if (duplicateOther) {
            return@withContext Result.failure(IllegalStateException("Ya existe otra tendencia llamada \"$cleanTitle\"."))
        }
        val requestDto = SaveTrendInputDto(
            id = targetId,
            title = cleanTitle,
            eventsLabel = trend.eventsLabel.trim(),
            photoKey = trend.photo.key,
            photoUrl = trend.photoUrl.trim().ifEmpty { null },
            photoBgHex = trend.photoBgHex.trim().ifEmpty { "#F3EEF6" },
            linkedCategory = trend.linkedCategory.trim().ifEmpty { null },
            linkedPackageId = trend.linkedPackageId.trim().ifEmpty { null },
            sortOrder = trend.sortOrder,
            active = trend.active,
        )
        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    val response = client.updateCatalogTrend(targetId, requestDto)
                    val mapped = RosaFiestaBackendMappers.toCatalogTrend(response.trend)
                    _catalogTrends.update { list -> upsertTrendInList(list, mapped) }
                    Result.success(mapped)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }
        val updatedLocal = trend.copy(id = targetId, title = cleanTitle)
        _catalogTrends.update { list -> upsertTrendInList(list, updatedLocal) }
        Result.success(updatedLocal)
    }

    suspend fun deleteTrendAsync(trendId: String): Result<Unit> = withContext(ioDispatcher) {
        val targetId = trendId.trim()
        if (targetId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Tendencia inválida."))
        }
        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    client.deleteCatalogTrend(targetId)
                    _catalogTrends.update { list ->
                        list.filterNot { candidate -> candidate.id.equals(targetId, ignoreCase = true) }
                    }
                    Result.success(Unit)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }
        _catalogTrends.update { list ->
            list.filterNot { candidate -> candidate.id.equals(targetId, ignoreCase = true) }
        }
        Result.success(Unit)
    }

    suspend fun saveSeasonBannerAsync(banner: SeasonBanner): Result<SeasonBanner> = withContext(ioDispatcher) {
        val cleanBadge = banner.badgeLabel.trim()
        val cleanTitle = banner.title.trim()
        val cleanCta = banner.ctaLabel.trim()
        val cleanTarget = banner.targetScreenCode.trim().ifEmpty { "s21" }
        if (banner.active) {
            val validationError = InputValidator.validateSeasonBannerTitle(cleanTitle, isRequired = true)
            if (validationError != null) {
                return@withContext Result.failure(IllegalArgumentException(validationError))
            }
        }
        val requestDto = SaveSeasonBannerInputDto(
            badgeLabel = cleanBadge,
            title = cleanTitle,
            ctaLabel = cleanCta,
            targetScreenCode = cleanTarget,
            active = banner.active,
        )
        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    val response = client.saveSeasonBanner(requestDto)
                    val mapped = RosaFiestaBackendMappers.toSeasonBanner(response.banner)
                    _seasonBanner.value = mapped
                    Result.success(mapped)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }
        val savedLocal = SeasonBanner(
            id = "default",
            badgeLabel = cleanBadge,
            title = cleanTitle,
            ctaLabel = cleanCta,
            targetScreenCode = cleanTarget,
            active = banner.active,
        )
        _seasonBanner.value = savedLocal
        Result.success(savedLocal)
    }

    private fun upsertTrendInList(
        currentList: List<CatalogTrend>,
        savedTrend: CatalogTrend,
    ): List<CatalogTrend> {
        val existingIdx = currentList.indexOfFirst { candidate ->
            candidate.id.equals(savedTrend.id, ignoreCase = true)
        }
        val nextList = if (existingIdx >= 0) {
            currentList.toMutableList().apply { set(existingIdx, savedTrend) }
        } else {
            currentList + savedTrend
        }
        return nextList.sortedBy { candidate -> candidate.sortOrder }
    }

    suspend fun deleteArticleAsync(articleId: String): Result<Unit> = withContext(ioDispatcher) {
        val targetId = articleId.trim()
        if (targetId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Artículo inválido."))
        }
        val targetArticle = articlesFlow.value.firstOrNull { candidate ->
            candidate.id.equals(targetId, ignoreCase = true)
        } ?: return@withContext Result.failure(IllegalStateException("El artículo seleccionado no existe."))

        val client = apiClient
        val isOnlineSession = if (client != null) hasRealServerToken(client) else false
        if (client != null) {
            if (isOnlineSession) {
                return@withContext try {
                    client.deleteCatalogArticle(targetArticle.id)
                    removeArticleFromLocalCatalog(targetArticle.id)
                    Result.success(Unit)
                } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
                    throw cancelled
                } catch (err: Exception) {
                    Result.failure(err)
                }
            }
        }

        removeArticleFromLocalCatalog(targetArticle.id)
        Result.success(Unit)
    }

    private fun removeArticleFromLocalCatalog(articleId: String) {
        articlesFlow.update { list ->
            list.filterNot { candidate -> candidate.id.equals(articleId, ignoreCase = true) }
        }
        val remaining = articlesFlow.value
        _catalogCategories.update { categories ->
            categories.map { category ->
                val count = remaining.count { article ->
                    val matchesName = article.category.equals(category.name, ignoreCase = true)
                    val matchesId = article.category.equals(category.id, ignoreCase = true)
                    if (matchesName) true else matchesId
                }
                category.copy(itemCount = count)
            }
        }
    }

    fun resolveCategoryWireId(rawCategoryNameOrId: String): String {
        val trimmed = rawCategoryNameOrId.trim()
        val matched = _catalogCategories.value.firstOrNull { candidate ->
            val matchesName = candidate.name.equals(trimmed, ignoreCase = true)
            val matchesId = candidate.id.equals(trimmed, ignoreCase = true)
            if (matchesName) true else matchesId
        }
        return matched?.id ?: trimmed
    }

    private fun relinkLocalArticlesCategoryName(previousName: String?, updatedName: String) {
        val cleanOld = previousName?.trim().orEmpty()
        if (cleanOld.isEmpty()) return
        if (cleanOld == updatedName) return
        articlesFlow.update { currentArticles ->
            currentArticles.map { article ->
                if (article.category.equals(cleanOld, ignoreCase = true)) {
                    article.copy(category = updatedName)
                } else {
                    article
                }
            }
        }
    }

    private fun upsertCategoryInList(
        list: List<CatalogCategory>,
        category: CatalogCategory,
    ): List<CatalogCategory> {
        val existingIndex = list.indexOfFirst { item -> item.id.equals(category.id, ignoreCase = true) }
        if (existingIndex < 0) {
            return list + category
        }
        return list.mapIndexed { index, existing ->
            if (index == existingIndex) category else existing
        }
    }

    private fun slugifyCategoryId(rawName: String): String {
        val normalized = rawName.trim().lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ü", "u")
            .replace("ñ", "n")
        val builder = StringBuilder()
        var previousDash = false
        for (character in normalized) {
            if (character.isLetterOrDigit()) {
                builder.append(character)
                previousDash = false
            } else if (!previousDash) {
                if (builder.isNotEmpty()) {
                    builder.append('-')
                    previousDash = true
                }
            }
        }
        return builder.toString().trim('-').ifEmpty { "categoria" }
    }

    private fun defaultIlloKeyForCategoryName(name: String): String {
        val lower = name.trim().lowercase()
        return when {
            lower.contains("flor") -> "floral"
            lower.contains("glob") || lower.contains("arco") -> "globos"
            lower.contains("dulce") -> "dulces"
            lower.contains("ilumin") || lower.contains("luz") -> "iluminacion"
            lower.contains("mantel") -> "manteleria"
            else -> "mobiliario"
        }
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
    ): CatalogArticle? = updateArticleWithMedia(
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
        photoUrl = null,
    )

    fun updateArticleWithMedia(
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
                        photoUrl = photoUrl?.trim() ?: article.photoUrl,
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
                val updateDto = UpdateArticleInputDto(
                    name = name?.trim(),
                    categoryId = category?.trim(),
                    description = description?.trim(),
                    dailyPriceDop = pricePerDayDop?.toLong(),
                    totalStock = totalStock?.toLong(),
                    availableStock = availableStock?.toLong(),
                    dimensions = dimensions?.trim(),
                    material = material?.trim(),
                    visible = visibleInCatalog,
                    photoUrl = photoUrl?.trim(),
                )
                val remoteAttempt = runCatching {
                    apiClient?.updateCatalogArticle(articleId = targetId, input = updateDto)
                }
                val shouldQueueOffline = if (apiClient != null) remoteAttempt.isFailure else false
                if (shouldQueueOffline) {
                    onEnqueueOfflineMutation(
                        "Actualizar artículo $targetId",
                        targetId,
                        "update_article",
                        coordinatorJson.encodeToString(updateDto),
                        "idem-article-upd-$targetId",
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
        val damaged = article?.damagedStock ?: 0
        val effectiveTotal = (total - damaged).coerceAtLeast(0)
        val reservations = buildArticleDateReservations(article)
        val days = (1..28).map { dayNumber ->
            val paddedDay = dayNumber.toString().padStart(2, '0')
            val dateIso = "$monthIso-$paddedDay"
            val avail = if (article == null) {
                0
            } else {
                RosaFiestaEngine.computeAvailableStockForRange(
                    totalStock = total,
                    damagedStock = damaged,
                    reservations = reservations,
                    articleId = article.id,
                    pickupDateIso = dateIso,
                    returnDateIso = dateIso,
                )
            }
            val reserved = (effectiveTotal - avail).coerceAtLeast(0)
            ArticleAvailabilityDay(
                dateIso = dateIso,
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
    ): DamageIncident = reportDamageWithMedia(
        eventId = eventId,
        articleCode = articleCode,
        articleName = articleName,
        damagedUnits = damagedUnits,
        missingUnits = missingUnits,
        replacementDop = replacementDop,
        notes = notes,
        photoUrl = "",
    )

    fun reportDamageWithMedia(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
        photoUrl: String,
    ): DamageIncident {
        val cleanEventId = eventId.trim()
        val cleanPhotoUrl = photoUrl.trim()
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
            photoUrl = cleanPhotoUrl,
        )
        _damageIncidents.update { list -> listOf(created) + list }
        scope.launch {
            onPersistCoreSync()
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
                        photoUrl = cleanPhotoUrl.ifBlank { null },
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
                onPersistCoreSync()
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
            onPersistCoreSync()
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
            onPersistCoreSync()
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
                onPersistCoreSync()
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

    suspend fun uploadMedia(
        filename: String,
        mimeType: String,
        purpose: String,
        bytes: ByteArray,
    ): Result<MediaUploadAsset> = withContext(ioDispatcher) {
        executeRemoteCall(missingApiCode = "MEDIA_UPLOAD_UNAVAILABLE") { client ->
            val asset = client.uploadMedia(
                filename = filename,
                mimeType = mimeType,
                purpose = purpose,
                bytes = bytes,
            )
            RosaFiestaBackendMappers.toMediaUploadAsset(asset)
        }
    }

    suspend fun fetchMediaBytes(urlOrId: String): Result<ByteArray> = withContext(ioDispatcher) {
        executeRemoteCall(missingApiCode = "MEDIA_FETCH_UNAVAILABLE") { client ->
            client.fetchMediaBytes(urlOrId)
        }
    }

    suspend fun verifyPayment(
        paymentId: String,
        notes: String = "",
    ): Result<PaymentRecord> = withContext(ioDispatcher) {
        val cleanId = paymentId.trim()
        val result = executeRemoteCall(missingApiCode = "PAYMENT_VERIFICATION_UNAVAILABLE") { client ->
            val updatedDto = client.verifyPayment(cleanId, notes)
            RosaFiestaBackendMappers.toPaymentRecord(updatedDto.payment)
        }
        val mapped = result.getOrNull()
        if (mapped != null) {
            upsertPaymentInMemory(mapped)
            onPersistCoreSync()
            runCatching { syncFromBackendAsync() }
        }
        result
    }

    suspend fun rejectPayment(
        paymentId: String,
        reason: String,
    ): Result<PaymentRecord> = withContext(ioDispatcher) {
        val cleanId = paymentId.trim()
        val result = executeRemoteCall(missingApiCode = "PAYMENT_REJECTION_UNAVAILABLE") { client ->
            val updatedDto = client.rejectPayment(cleanId, reason)
            RosaFiestaBackendMappers.toPaymentRecord(updatedDto.payment)
        }
        val mapped = result.getOrNull()
        if (mapped != null) {
            upsertPaymentInMemory(mapped)
            onPersistCoreSync()
        }
        result
    }

    suspend fun createCardPaymentSession(
        eventId: String,
        amountDop: Int,
        ncfType: String = "B02",
        clientRnc: String = "",
    ): Result<CardSessionResult> = withContext(ioDispatcher) {
        val cleanEventId = eventId.trim()
        val eventTitle = eventsFlow.value.firstOrNull { event -> event.id == cleanEventId }?.title.orEmpty()
        executeRemoteCall(missingApiCode = "CARD_GATEWAY_NOT_CONFIGURED") { client ->
            val output = client.createCardSession(
                CreateCardSessionInputDto(
                    eventId = cleanEventId,
                    eventTitle = eventTitle,
                    amountRD = amountDop.toLong(),
                    ncfType = ncfType.trim().ifEmpty { "B02" },
                    clientRnc = clientRnc.trim(),
                ),
            )
            RosaFiestaBackendMappers.toCardSessionResult(output.session)
        }
    }

    suspend fun completeEventCloseout(
        eventId: String,
        notes: String,
    ): Result<ClientEvent> = withContext(ioDispatcher) {
        val cleanEventId = eventId.trim()
        val fallbackMap = eventsFlow.value.associateBy { event -> event.id }
        val result = executeRemoteCall(missingApiCode = "EVENT_CLOSEOUT_UNAVAILABLE") { client ->
            val output = client.closeoutEvent(cleanEventId, notes)
            RosaFiestaBackendMappers.toClientEvent(output.event, fallbackMap)
        }
        val mapped = result.getOrNull()
        if (mapped != null) {
            eventsFlow.update { existing ->
                existing.map { item -> if (item.id == mapped.id) mapped else item }
            }
            onPersistCoreSync()
            runCatching { syncFromBackendAsync() }
        }
        result
    }

    suspend fun resolveDamageIncident(
        incidentId: String,
        notes: String,
    ): Result<DamageIncident> = withContext(ioDispatcher) {
        val cleanId = incidentId.trim()
        val eventsById = eventsFlow.value.associateBy { event -> event.id }
        val result = executeRemoteCall(missingApiCode = "DAMAGE_RESOLUTION_UNAVAILABLE") { client ->
            val output = client.resolveDamage(cleanId, notes)
            RosaFiestaBackendMappers.toDamageIncident(output.report, eventsById)
        }
        val mapped = result.getOrNull()
        if (mapped != null) {
            _damageIncidents.update { existing ->
                existing.map { item -> if (item.id == mapped.id) mapped else item }
            }
            runCatching { syncFromBackendAsync() }
        }
        result
    }

    suspend fun checkRangeAvailability(
        date: String,
        articleId: String,
        quantity: Int,
        excludeEventId: String = "",
    ): Result<AvailabilityRangeCheck> = withContext(ioDispatcher) {
        val client = apiClient
        if (client != null) {
            return@withContext executeRemoteCall(missingApiCode = "AVAILABILITY_UNAVAILABLE") { activeClient ->
                val dto = activeClient.checkAvailabilityRange(
                    dateIso = date,
                    articleId = articleId,
                    quantity = quantity,
                    excludeEventId = excludeEventId,
                )
                RosaFiestaBackendMappers.toAvailabilityRangeCheck(dto)
            }
        }
        checkLocalAvailabilityRange(date, articleId, quantity, excludeEventId)
    }

    suspend fun sendWhatsAppBotReply(
        threadId: String,
        phone: String,
        message: String,
    ): Result<BotThreadRecord> = withContext(ioDispatcher) {
        val cleanThreadId = threadId.trim()
        val result = executeRemoteCall(missingApiCode = "WHATSAPP_PROVIDER_NOT_CONFIGURED") { client ->
            val output = client.sendWhatsAppReply(
                SendWhatsAppReplyInputDto(
                    threadId = cleanThreadId,
                    recipientPhone = phone.trim(),
                    message = message.trim(),
                ),
            )
            RosaFiestaBackendMappers.toBotThreadRecord(output.thread)
        }
        val mapped = result.getOrNull()
        if (mapped != null) {
            _botThreads.update { list ->
                val exists = list.any { thread -> thread.id == mapped.id }
                if (exists) {
                    list.map { thread -> if (thread.id == mapped.id) mapped else thread }
                } else {
                    listOf(mapped) + list
                }
            }
        }
        result
    }

    suspend fun exportDgii607Csv(period: String = ""): Result<String> = withContext(ioDispatcher) {
        val client = apiClient
        if (client != null) {
            return@withContext executeRemoteCall(missingApiCode = "DGII_EXPORT_UNAVAILABLE") { activeClient ->
                val resolvedPeriod = period.trim().ifEmpty { "2026-10" }
                activeClient.exportDgii607(resolvedPeriod).csv
            }
        }
        Result.success(
            ReportCsvBuilder.buildDgii607Csv(
                payments = paymentsFlow.value,
                periodYyyymm = period,
            ),
        )
    }

    fun observeRealtimeEvents(): Flow<RealtimeStreamEvent> {
        val client = realtimeClient ?: return emptyFlow()
        return client.observeStream { currentSessionJwtFlow.value }
    }

    private suspend fun <T> executeRemoteCall(
        missingApiCode: String,
        block: suspend (RosaFiestaApiClient) -> T,
    ): Result<T> {
        val client = apiClient
        if (client == null) {
            return Result.failure(IllegalStateException(missingApiCode))
        }
        return try {
            Result.success(block(client))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (apiError: RosaFiestaApiException) {
            val friendly = SensitiveDataRedactor.translateBusinessError(apiError.errorCode, apiError.message)
            Result.failure(
                RosaFiestaApiException(
                    statusCode = apiError.statusCode,
                    errorCode = apiError.errorCode,
                    message = friendly,
                ),
            )
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private fun upsertPaymentInMemory(updated: PaymentRecord) {
        paymentsFlow.update { existing ->
            val found = existing.any { item -> item.id == updated.id }
            if (found) {
                existing.map { item -> if (item.id == updated.id) updated else item }
            } else {
                listOf(updated) + existing
            }
        }
    }

    private fun checkLocalAvailabilityRange(
        date: String,
        articleId: String,
        quantity: Int,
        excludeEventId: String,
    ): Result<AvailabilityRangeCheck> {
        val window = RosaFiestaEngine.computeTurnaroundWindow(date)
            ?: return Result.failure(IllegalArgumentException("INVALID_EVENT_DATE"))
        val targetKey = articleId.trim()
        val article = articlesFlow.value.firstOrNull { item ->
            val byId = item.id == targetKey
            val byCode = item.code == targetKey
            if (byId) true else byCode
        } ?: return Result.failure(IllegalArgumentException("ARTICLE_NOT_FOUND"))
        val reservations = buildArticleDateReservations(article)
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

    suspend fun fetchOAuthConfig(): Result<OAuthProviderConfig> = withContext(ioDispatcher) {
        val client = apiClient ?: return@withContext Result.success(OAuthProviderConfig())
        runCatching {
            val dto = client.getOAuthConfig()
            RosaFiestaBackendMappers.toOAuthProviderConfig(dto)
        }.recover { OAuthProviderConfig() }
    }

    suspend fun recoverUsernameByIdentifier(identifier: String): Result<String> = withContext(ioDispatcher) {
        val cleanIdentifier = identifier.trim()
        if (cleanIdentifier.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Ingresa un correo, teléfono o nombre completo válido."))
        }
        executeRemoteCall(missingApiCode = "USERNAME_RECOVERY_UNAVAILABLE") { client ->
            val output = client.recoverUsername(cleanIdentifier)
            output.message.ifBlank { "Si existe una cuenta asociada, te enviamos tu usuario por correo." }
        }
    }

    suspend fun updateUserPreferences(
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
    ): Result<RegisteredUserAccount> = withContext(ioDispatcher) {
        val current = currentAuthenticatedUserFlow.value
            ?: return@withContext Result.failure(IllegalStateException("AUTH_UNAUTHORIZED"))
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
        val optimistic = current.copy(preferences = updatedPrefs)
        upsertAccountInMemory(optimistic)
        currentAuthenticatedUserFlow.value = optimistic
        val activeToken = currentSessionJwtFlow.value.orEmpty()
        onPersistAccountAndSession(optimistic, activeToken)

        val client = apiClient
        val shouldSyncRemote = if (client != null) hasRealServerToken(client) else false
        if (!shouldSyncRemote) {
            return@withContext Result.success(optimistic)
        }
        val remoteResult = executeRemoteCall(missingApiCode = "PREFERENCES_UNAVAILABLE") { activeClient ->
            val output = activeClient.updateMyPreferences(
                UpdatePreferencesInputDto(
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
                ),
            )
            optimistic.copy(preferences = RosaFiestaBackendMappers.toUserPreferences(output.user.preferences))
        }
        val synced = remoteResult.getOrNull() ?: return@withContext remoteResult
        upsertAccountInMemory(synced)
        currentAuthenticatedUserFlow.value = synced
        onPersistAccountAndSession(synced, activeToken)
        Result.success(synced)
    }

    suspend fun sendEventPaymentReminder(
        eventId: String,
        channel: String = "whatsapp",
        message: String = "",
    ): Result<PaymentReminderOutcome> = withContext(ioDispatcher) {
        val cleanEventId = eventId.trim()
        val fallbackById = eventsFlow.value.associateBy { event -> event.id }
        val result = executeRemoteCall(missingApiCode = "PAYMENT_REMINDER_UNAVAILABLE") { client ->
            val output = client.sendPaymentReminder(
                eventId = cleanEventId,
                input = PaymentReminderInputDto(
                    channel = channel.trim().ifEmpty { "whatsapp" },
                    message = message.trim(),
                ),
            )
            val mappedEvent = RosaFiestaBackendMappers.toClientEvent(output.event, fallbackById)
            PaymentReminderOutcome(
                event = mappedEvent,
                sent = output.sent,
                channel = output.channel,
            )
        }
        val outcome = result.getOrNull()
        if (outcome != null) {
            eventsFlow.update { existing ->
                existing.map { item -> if (item.id == outcome.event.id) outcome.event else item }
            }
            onPersistCoreSync()
        }
        result
    }

    suspend fun confirmCardCheckoutSession(sessionId: String): Result<PaymentRecord> = withContext(ioDispatcher) {
        val cleanSessionId = sessionId.trim()
        val result = executeRemoteCall(missingApiCode = "CARD_CONFIRM_UNAVAILABLE") { client ->
            val output = client.confirmCardSession(cleanSessionId)
            RosaFiestaBackendMappers.toPaymentRecord(output.payment)
        }
        val mapped = result.getOrNull()
        if (mapped != null) {
            upsertPaymentInMemory(mapped)
            onPersistCoreSync()
            runCatching { syncFromBackendAsync() }
        }
        result
    }

    suspend fun fetchPaymentsAnalytics(range: String = "30d"): Result<PaymentsAnalyticsSummary> = withContext(ioDispatcher) {
        val cleanRange = range.trim().ifEmpty { "30d" }
        val client = apiClient
        val canFetchRemote = if (client != null) hasRealServerToken(client) else false
        if (canFetchRemote) {
            val remote = executeRemoteCall(missingApiCode = "ANALYTICS_UNAVAILABLE") { activeClient ->
                val output = activeClient.getPaymentsAnalytics(cleanRange)
                RosaFiestaBackendMappers.toPaymentsAnalyticsSummary(output.analytics)
            }
            if (remote.isSuccess) {
                return@withContext remote
            }
        }
        Result.success(buildLocalAnalyticsSummary(cleanRange))
    }

    suspend fun requestRosaSketch(
        prompt: String,
        eventType: String = "",
        guests: Int = 80,
    ): Result<RosaSketchProposal> = withContext(ioDispatcher) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.length < 2) {
            return@withContext Result.failure(IllegalArgumentException("Describe la distribución o estilo que deseas generar."))
        }
        executeRemoteCall(missingApiCode = "ROSA_SKETCH_UNAVAILABLE") { client ->
            val response = client.generateRosaSketch(
                RosaSketchInputDto(
                    prompt = cleanPrompt,
                    eventType = eventType.trim(),
                    guests = guests.coerceAtLeast(10),
                ),
            )
            RosaFiestaBackendMappers.toRosaSketchProposal(response)
        }
    }

    suspend fun updateEventItems(
        eventId: String,
        items: List<EventLineItem>,
    ): Result<ClientEvent> = withContext(ioDispatcher) {
        val cleanEventId = eventId.trim()
        val existingEvent = eventsFlow.value.firstOrNull { event -> event.id == cleanEventId }
            ?: return@withContext Result.failure(IllegalArgumentException("EVENT_NOT_FOUND"))
        val computedTotal = items.sumOf { line -> line.quantity * line.unitPriceDop }
        val updatedLocal = existingEvent.copy(
            items = items,
            articleCount = items.sumOf { line -> line.quantity },
            totalDop = computedTotal.coerceAtLeast(existingEvent.paidDepositDop),
        )
        eventsFlow.update { existing ->
            existing.map { item -> if (item.id == cleanEventId) updatedLocal else item }
        }
        onPersistCoreSync()

        val client = apiClient
        val shouldSyncRemote = if (client != null) hasRealServerToken(client) else false
        if (!shouldSyncRemote) {
            return@withContext Result.success(updatedLocal)
        }
        val fallbackById = eventsFlow.value.associateBy { event -> event.id }
        val remote = executeRemoteCall(missingApiCode = "EVENT_UPDATE_UNAVAILABLE") { activeClient ->
            val itemDtos = items.map { line ->
                EventItemDto(
                    articleCode = line.articleCode,
                    name = line.name,
                    quantity = line.quantity.toLong(),
                    unitPriceRD = line.unitPriceDop.toLong(),
                )
            }
            val output = activeClient.updateRemoteEvent(
                eventId = cleanEventId,
                input = UpdateEventInputDto(items = itemDtos),
            )
            RosaFiestaBackendMappers.toClientEvent(output.event, fallbackById)
        }
        val synced = remote.getOrNull()
        if (synced == null) {
            eventsFlow.update { existing ->
                existing.map { item -> if (item.id == cleanEventId) existingEvent else item }
            }
            onPersistCoreSync()
            return@withContext Result.failure(
                remote.exceptionOrNull() ?: IllegalStateException("EVENT_UPDATE_FAILED"),
            )
        }
        eventsFlow.update { existing ->
            existing.map { item -> if (item.id == synced.id) synced else item }
        }
        onPersistCoreSync()
        Result.success(synced)
    }

    suspend fun verifyAccountPassword(password: String): Result<Boolean> = withContext(ioDispatcher) {
        val cleanPassword = password.trim()
        val current = currentAuthenticatedUserFlow.value
            ?: return@withContext Result.failure(IllegalStateException("AUTH_UNAUTHORIZED"))
        if (cleanPassword.isEmpty()) {
            return@withContext Result.success(false)
        }
        if (current.passwordHash.isNotBlank()) {
            val verifiedLocally = JwtAuthManager.verifyPassword(cleanPassword, current.salt, current.passwordHash)
            if (verifiedLocally) {
                return@withContext Result.success(true)
            }
        }
        val client = apiClient
        if (client != null) {
            val remoteOk = runCatching {
                client.verifyCredentials(LoginInputDto(email = current.email, password = cleanPassword))
            }.getOrDefault(false)
            if (remoteOk) {
                return@withContext Result.success(true)
            }
        }
        Result.success(false)
    }

    private fun buildLocalAnalyticsSummary(range: String): PaymentsAnalyticsSummary {
        val verifiedPayments = paymentsFlow.value.filter { payment -> payment.verified }
        val totalRevenue = verifiedPayments.sumOf { payment -> payment.amountDop }
        val totalRefunds = _refundRequests.value
            .filter { refund ->
                val upper = refund.status.uppercase()
                val isAprob = upper.contains("APROB")
                val isApproved = upper.contains("APPROVED")
                if (isAprob) true else isApproved
            }
            .sumOf { refund -> refund.refundAmountDop }
        val totalDamages = _damageIncidents.value.sumOf { incident -> incident.replacementDop }
        val totalExpenses = totalRefunds + totalDamages
        val netMargin = if (totalRevenue > 0) (((totalRevenue - totalExpenses) * 100) / totalRevenue) else 0
        val eventsList = eventsFlow.value
        val breakdown = eventsList
            .groupBy { event -> event.eventType.ifBlank { "Otro" } }
            .map { (type, list) -> EventTypeCount(eventType = type, count = list.size) }
            .sortedByDescending { item -> item.count }
        val uniqueClients = eventsList.map { event -> event.clientName.trim().lowercase() }.filter { name -> name.isNotEmpty() }.distinct().size
        val repeatClients = eventsList
            .groupBy { event -> event.clientName.trim().lowercase() }
            .count { (name, list) ->
                if (name.isEmpty()) false else list.size > 1
            }
        val totalContracted = eventsList.filter { event -> event.status != EventStatus.CANCELLED }.sumOf { event -> event.totalDop }
        val totalPaid = eventsList.filter { event -> event.status != EventStatus.CANCELLED }.sumOf { event -> event.paidDepositDop }
        val collectionRate = if (totalContracted > 0) ((totalPaid * 100) / totalContracted) else 0
        val currentMonthBar = MonthlyAnalyticsBar(
            monthKey = DomainClockFormatter.currentYearMonthIso(),
            monthLabel = DomainClockFormatter.currentShortDateLabel().substringAfter(" "),
            revenueDop = totalRevenue,
            expensesDop = totalExpenses,
            eventsCount = eventsList.size,
        )
        val hasRevenue = totalRevenue > 0
        val hasExpenses = totalExpenses > 0
        val hasEvents = eventsList.isNotEmpty()
        val hasMonthlyActivity = when {
            hasRevenue -> true
            hasExpenses -> true
            else -> hasEvents
        }
        val monthlySeries = if (hasMonthlyActivity) listOf(currentMonthBar) else emptyList()
        val confirmedOrActiveCount = eventsList.count { event ->
            when (event.status) {
                EventStatus.CONFIRMED, EventStatus.IN_SETUP -> true
                else -> false
            }
        }
        return PaymentsAnalyticsSummary(
            range = range,
            monthlySeries = monthlySeries,
            totalRevenueDop = totalRevenue,
            totalExpensesDop = totalExpenses,
            netMarginPercent = netMargin,
            totalEventsCount = eventsList.size,
            confirmedEventsCount = confirmedOrActiveCount,
            pendingEventsCount = eventsList.count { event -> event.status == EventStatus.PENDING_QUOTE },
            completedEventsCount = eventsList.count { event -> event.status == EventStatus.COMPLETED },
            cancelledEventsCount = eventsList.count { event -> event.status == EventStatus.CANCELLED },
            eventTypesBreakdown = breakdown,
            uniqueClientsCount = uniqueClients,
            repeatClientsCount = repeatClients,
            collectionRatePercent = collectionRate,
        )
    }

    private fun buildArticleDateReservations(article: CatalogArticle?): List<DateRangeReservation> {
        val target = article ?: return emptyList()
        val reservations = mutableListOf<DateRangeReservation>()
        for (event in eventsFlow.value) {
            if (event.status == EventStatus.CANCELLED) continue
            if (event.status == EventStatus.COMPLETED) continue
            val turnaround = resolveEventTurnaroundWindow(event) ?: continue
            val matchedQuantity = sumMatchingArticleQuantity(event.items, target)
            if (matchedQuantity <= 0) continue
            val reservationStatus = if (event.status == EventStatus.PENDING_QUOTE) "held" else "confirmed"
            reservations.add(
                DateRangeReservation(
                    articleId = target.id,
                    eventId = event.id,
                    quantity = matchedQuantity,
                    pickupDateIso = turnaround.first,
                    returnDateIso = turnaround.second,
                    status = reservationStatus,
                ),
            )
        }
        return reservations
    }

    private fun resolveEventTurnaroundWindow(event: ClientEvent): Pair<String, String>? {
        val hasPickup = event.pickupDateIso.isNotBlank()
        val hasReturn = event.returnDateIso.isNotBlank()
        if (hasPickup) {
            if (hasReturn) {
                return event.pickupDateIso.trim() to event.returnDateIso.trim()
            }
        }
        val rawDate = event.eventDateIso.ifBlank { event.dateLabel }
        val window = RosaFiestaEngine.computeTurnaroundWindow(rawDate) ?: return null
        return window.pickupDateIso to window.returnDateIso
    }

    internal fun snapshotForPersistence(): PersistedSubmoduleSnapshot = PersistedSubmoduleSnapshot(
        quotes = _quotes.value,
        deliveryStops = _deliveryStops.value,
        damageIncidents = _damageIncidents.value,
        notifications = _notifications.value,
        extrasJson = encodeExtrasSnapshot(),
    )

    internal fun restoreFromPersistence(snapshot: PersistedSubmoduleSnapshot) {
        if (snapshot.quotes.isNotEmpty()) {
            _quotes.value = snapshot.quotes
        } else if (!seedDemoData) {
            _quotes.value = emptyList()
        }
        if (snapshot.deliveryStops.isNotEmpty()) {
            _deliveryStops.value = snapshot.deliveryStops
        } else if (!seedDemoData) {
            _deliveryStops.value = emptyList()
        }
        if (snapshot.damageIncidents.isNotEmpty()) {
            _damageIncidents.value = snapshot.damageIncidents
        } else if (!seedDemoData) {
            _damageIncidents.value = emptyList()
        }
        if (snapshot.notifications.isNotEmpty()) {
            _notifications.value = snapshot.notifications
        } else if (!seedDemoData) {
            _notifications.value = emptyList()
        }
        if (snapshot.extrasJson.isNotBlank()) {
            restoreExtrasSnapshot(snapshot.extrasJson)
        }
    }

    private fun encodeExtrasSnapshot(): String {
        val dto = PersistedSubmoduleExtrasDto(
            categories = _catalogCategories.value.map { cat ->
                CategoryDto(id = cat.id, name = cat.name, illoKey = cat.illoKey, itemCount = cat.itemCount.toLong())
            },
            trends = _catalogTrends.value.map { trend ->
                CatalogTrendDto(
                    id = trend.id,
                    title = trend.title,
                    eventsLabel = trend.eventsLabel,
                    photoKey = trend.photo.key,
                    photoUrl = trend.photoUrl.ifBlank { null },
                    photoBgHex = trend.photoBgHex,
                    linkedCategory = trend.linkedCategory.ifBlank { null },
                    linkedPackageId = trend.linkedPackageId.ifBlank { null },
                    sortOrder = trend.sortOrder,
                    active = trend.active,
                )
            },
            seasonBanner = SeasonBannerDto(
                id = _seasonBanner.value.id,
                badgeLabel = _seasonBanner.value.badgeLabel,
                title = _seasonBanner.value.title,
                ctaLabel = _seasonBanner.value.ctaLabel,
                targetScreenCode = _seasonBanner.value.targetScreenCode,
                active = _seasonBanner.value.active,
            ),
            packages = _packages.value.map { pkg ->
                PackageBundleDto(
                    id = pkg.id,
                    name = pkg.name,
                    eventType = pkg.eventType,
                    photoKey = pkg.photo.key,
                    totalPriceDop = pkg.priceDop.toLong(),
                    guestCapacity = pkg.suggestedGuests.toLong(),
                    itemsSummary = pkg.subtitle.ifBlank { pkg.includedItems.joinToString(", ") },
                    visible = pkg.visibleInCatalog,
                    photoUrl = pkg.photoUrl.ifBlank { null },
                )
            },
            clients = _clients.value.map { cli ->
                ClientSummaryDto(
                    id = cli.id,
                    fullName = cli.fullName,
                    email = cli.email,
                    phone = cli.phone,
                    eventsCount = cli.eventsCount.toLong(),
                    totalSpentDop = cli.totalContractedDop.toLong(),
                    balanceDop = cli.pendingBalanceDop.toLong(),
                    notes = cli.notes.ifBlank { null },
                )
            },
            refunds = _refundRequests.value.map { req ->
                RefundDto(
                    id = req.id,
                    eventId = req.eventId,
                    eventTitle = req.eventTitle,
                    clientName = req.clientName,
                    paidRD = req.paidDepositDop.toLong(),
                    refundPercent = req.refundPercent.toLong(),
                    refundRD = req.refundAmountDop.toLong(),
                    reason = req.reason,
                    status = req.status,
                    approvedBy = req.approvedBy.ifBlank { null },
                    approvedAtLabel = req.approvedAtLabel.ifBlank { null },
                    gatewayRefundId = req.gatewayRefundId.ifBlank { null },
                    gatewayStatus = req.gatewayStatus.ifBlank { null },
                )
            },
            reviews = _reviews.value.map { rev ->
                ReviewDto(
                    id = rev.id,
                    eventId = rev.eventId,
                    clientName = rev.authorName,
                    rating = rev.rating.toLong(),
                    comment = rev.comment,
                    adminReply = rev.adminReply.ifBlank { null },
                    createdAtLabel = rev.dateLabel.ifBlank { null },
                )
            },
            suppliers = _suppliers.value.map { sup ->
                SupplierDto(
                    id = sup.id,
                    name = sup.name,
                    category = sup.category,
                    specialty = sup.specialty,
                    contactName = sup.contactName,
                    phone = sup.phone,
                    email = sup.email,
                    website = sup.website.ifBlank { null },
                    rating = sup.rating,
                    notes = sup.notes.ifBlank { null },
                )
            },
            leads = _leads.value.map { lead ->
                LeadDto(
                    id = lead.id,
                    fullName = lead.clientName,
                    phone = lead.phone,
                    eventType = lead.eventType,
                    guests = lead.guests.toLong(),
                    estimatedDop = lead.estimatedDop.toLong(),
                    stage = lead.stage,
                    email = lead.email.ifBlank { null },
                    eventDateLabel = lead.dateLabel.ifBlank { null },
                    notes = lead.followUpNote.ifBlank { null },
                )
            },
            chatThreads = _chatThreads.value.map { thr ->
                ChatThreadDto(
                    threadId = thr.id,
                    participantName = thr.contactName,
                    eventId = thr.eventId,
                    eventTitle = thr.eventTitle,
                    lastMessage = thr.lastMessage,
                    updatedAt = thr.timestampLabel,
                    unreadCount = thr.unreadCount.toLong(),
                )
            },
            threadMessages = _threadMessages.value.map { msg ->
                ChatMessageDto(
                    id = msg.id,
                    threadId = msg.threadId,
                    eventId = msg.eventId,
                    eventTitle = msg.eventTitle,
                    senderId = "",
                    senderName = msg.senderName,
                    senderRole = msg.senderRole,
                    body = msg.body,
                    createdAt = msg.timestampLabel,
                    unread = msg.unreadCount > 0,
                    attachmentName = msg.attachmentName.ifBlank { null },
                    attachmentUrl = msg.attachmentUrl.ifBlank { null },
                )
            },
            eventGuests = _eventGuests.value.map { guest ->
                EventGuestDto(
                    id = guest.id,
                    eventId = guest.eventId,
                    fullName = guest.fullName,
                    rsvpStatus = guest.rsvpStatus,
                    tableLabel = guest.tableLabel,
                    dietaryNotes = guest.notes.ifBlank { null },
                )
            },
            eventSchedule = _eventSchedule.value.map { blk ->
                ScheduleBlockDto(
                    id = blk.id,
                    eventId = blk.eventId,
                    timeRange = blk.timeRange,
                    title = blk.title,
                    ownerName = blk.ownerName,
                    status = if (blk.completed) "COMPLETED" else "PENDING",
                    sortOrder = blk.sortOrder.toLong(),
                )
            },
            eventTasks = _eventTasks.value.map { tsk ->
                EventTaskDto(
                    id = tsk.id,
                    eventId = tsk.eventId,
                    title = tsk.title,
                    assigneeName = tsk.assigneeName,
                    done = tsk.done,
                )
            },
            botFaqs = _botFaqs.value.map { faq ->
                BotFaqDto(
                    id = faq.id,
                    category = faq.category,
                    question = faq.question,
                    answer = faq.answer,
                    active = faq.active,
                )
            },
            botKeywords = _botKeywords.value.map { kw ->
                BotKeywordDto(
                    id = kw.id,
                    keyword = kw.keyword,
                    autoReply = kw.autoReply,
                    handoffToHuman = kw.handoffToHuman,
                )
            },
            botThreads = _botThreads.value.map { bt ->
                BotThreadDto(
                    id = bt.id,
                    customerName = bt.customerName,
                    phone = bt.phone,
                    lastMessage = bt.lastMessage,
                    botPaused = bt.botPaused,
                    updatedAtLabel = bt.updatedAtLabel,
                    assignedAgent = bt.assignedAgent.ifBlank { null },
                )
            },
            businessSettings = BusinessSettingsDto(
                id = "default",
                companyName = _businessSettings.value.businessName,
                rnc = _businessSettings.value.rnc,
                address = _businessSettings.value.address,
                phone = _businessSettings.value.phone,
                whatsapp = _businessSettings.value.whatsapp,
                email = _businessSettings.value.email,
                businessHours = _businessSettings.value.businessHours,
                instagram = _businessSettings.value.instagramUrl,
                facebook = _businessSettings.value.facebookUrl,
                depositPercent = _businessSettings.value.depositPercent.toLong(),
                itbisPercent = _businessSettings.value.itbisPercent.toLong(),
            ),
            deliveryZones = _businessSettings.value.zones.map { zone ->
                DeliveryZoneDto(
                    id = zone.id,
                    name = zone.name,
                    feeDop = zone.feeDop.toLong(),
                    etaMinutes = zone.etaMinutes.toLong(),
                    active = zone.active,
                )
            },
        )
        return coordinatorJson.encodeToString(dto)
    }

    private fun shouldApplyRestoredCollection(hasItems: Boolean): Boolean = if (hasItems) true else !seedDemoData

    private fun restoreExtrasSnapshot(rawJson: String) {
        val dto = runCatching {
            coordinatorJson.decodeFromString<PersistedSubmoduleExtrasDto>(rawJson)
        }.getOrNull() ?: return
        if (shouldApplyRestoredCollection(dto.categories.isNotEmpty())) {
            _catalogCategories.value = dto.categories.map { item -> RosaFiestaBackendMappers.toCatalogCategory(item) }
        }
        if (shouldApplyRestoredCollection(dto.trends.isNotEmpty())) {
            _catalogTrends.value = dto.trends.map { item -> RosaFiestaBackendMappers.toCatalogTrend(item) }
        }
        dto.seasonBanner?.let { bannerDto ->
            _seasonBanner.value = RosaFiestaBackendMappers.toSeasonBanner(bannerDto)
        }
        if (shouldApplyRestoredCollection(dto.packages.isNotEmpty())) {
            _packages.value = dto.packages.map { item -> RosaFiestaBackendMappers.toEventPackage(item, emptyMap()) }
        }
        if (shouldApplyRestoredCollection(dto.clients.isNotEmpty())) {
            _clients.value = dto.clients.map { item -> RosaFiestaBackendMappers.toClientProfile(item, emptyMap()) }
        }
        if (shouldApplyRestoredCollection(dto.refunds.isNotEmpty())) {
            _refundRequests.value = dto.refunds.map { item -> RosaFiestaBackendMappers.toRefundRequest(item, emptyMap()) }
        }
        if (shouldApplyRestoredCollection(dto.reviews.isNotEmpty())) {
            val eventsById = eventsFlow.value.associateBy { event -> event.id }
            _reviews.value = dto.reviews.map { item -> RosaFiestaBackendMappers.toReviewRecord(item, eventsById, emptyMap()) }
        }
        if (shouldApplyRestoredCollection(dto.suppliers.isNotEmpty())) {
            _suppliers.value = dto.suppliers.map { item -> RosaFiestaBackendMappers.toSupplierRecord(item) }
        }
        if (shouldApplyRestoredCollection(dto.leads.isNotEmpty())) {
            _leads.value = dto.leads.map { item -> RosaFiestaBackendMappers.toLeadRecord(item) }
        }
        if (shouldApplyRestoredCollection(dto.chatThreads.isNotEmpty())) {
            _chatThreads.value = dto.chatThreads.map { item -> RosaFiestaBackendMappers.toChatThread(item) }
        }
        val contactNamesByThreadId = _chatThreads.value.associate { thr -> thr.id to thr.contactName }
        if (shouldApplyRestoredCollection(dto.threadMessages.isNotEmpty())) {
            _threadMessages.value = dto.threadMessages.map { item ->
                val resolvedContact = contactNamesByThreadId[item.threadId] ?: item.senderName
                RosaFiestaBackendMappers.toChatMessageRecord(item, resolvedContact)
            }
        }
        if (shouldApplyRestoredCollection(dto.eventGuests.isNotEmpty())) {
            _eventGuests.value = dto.eventGuests.map { item -> RosaFiestaBackendMappers.toEventGuest(item) }
        }
        if (shouldApplyRestoredCollection(dto.eventSchedule.isNotEmpty())) {
            _eventSchedule.value = dto.eventSchedule.map { item -> RosaFiestaBackendMappers.toEventScheduleBlock(item) }
        }
        if (shouldApplyRestoredCollection(dto.eventTasks.isNotEmpty())) {
            _eventTasks.value = dto.eventTasks.map { item -> RosaFiestaBackendMappers.toEventTaskRecord(item) }
        }
        if (shouldApplyRestoredCollection(dto.botFaqs.isNotEmpty())) {
            _botFaqs.value = dto.botFaqs.map { item -> RosaFiestaBackendMappers.toBotFaqRecord(item) }
        }
        if (shouldApplyRestoredCollection(dto.botKeywords.isNotEmpty())) {
            _botKeywords.value = dto.botKeywords.map { item -> RosaFiestaBackendMappers.toBotKeywordRecord(item) }
        }
        if (shouldApplyRestoredCollection(dto.botThreads.isNotEmpty())) {
            _botThreads.value = dto.botThreads.map { item -> RosaFiestaBackendMappers.toBotThreadRecord(item) }
        }
        val mappedZones = dto.deliveryZones.map { zoneDto ->
            com.jeudry.rosafiesta.domain.DeliveryZone(
                id = zoneDto.id,
                name = zoneDto.name,
                feeDop = zoneDto.feeDop.toInt(),
                etaMinutes = zoneDto.etaMinutes.toInt(),
                active = zoneDto.active,
            )
        }
        dto.businessSettings?.let { settingsDto ->
            _businessSettings.value = BusinessSettings(
                businessName = settingsDto.companyName,
                rnc = settingsDto.rnc,
                address = settingsDto.address,
                phone = settingsDto.phone,
                whatsapp = settingsDto.whatsapp,
                email = settingsDto.email,
                businessHours = settingsDto.businessHours,
                instagramUrl = settingsDto.instagram,
                facebookUrl = settingsDto.facebook,
                depositPercent = settingsDto.depositPercent.toInt(),
                itbisPercent = settingsDto.itbisPercent.toInt(),
                zones = mappedZones.ifEmpty { _businessSettings.value.zones },
            )
        }
    }

    private fun sumMatchingArticleQuantity(
        items: List<EventLineItem>,
        article: CatalogArticle,
    ): Int {
        var total = 0
        for (item in items) {
            val matchesCode = item.articleCode == article.code
            val matchesId = item.articleCode == article.id
            if (matchesCode) {
                total += item.quantity.coerceAtLeast(0)
            } else if (matchesId) {
                total += item.quantity.coerceAtLeast(0)
            }
        }
        return total
    }
}

@Serializable
private data class PersistedSubmoduleExtrasDto(
    val categories: List<CategoryDto> = emptyList(),
    val trends: List<CatalogTrendDto> = emptyList(),
    val seasonBanner: SeasonBannerDto? = null,
    val packages: List<PackageBundleDto> = emptyList(),
    val clients: List<ClientSummaryDto> = emptyList(),
    val refunds: List<RefundDto> = emptyList(),
    val reviews: List<ReviewDto> = emptyList(),
    val suppliers: List<SupplierDto> = emptyList(),
    val leads: List<LeadDto> = emptyList(),
    val chatThreads: List<ChatThreadDto> = emptyList(),
    val threadMessages: List<ChatMessageDto> = emptyList(),
    val eventGuests: List<EventGuestDto> = emptyList(),
    val eventSchedule: List<ScheduleBlockDto> = emptyList(),
    val eventTasks: List<EventTaskDto> = emptyList(),
    val botFaqs: List<BotFaqDto> = emptyList(),
    val botKeywords: List<BotKeywordDto> = emptyList(),
    val botThreads: List<BotThreadDto> = emptyList(),
    val businessSettings: BusinessSettingsDto? = null,
    val deliveryZones: List<DeliveryZoneDto> = emptyList(),
)

internal data class PersistedSubmoduleSnapshot(
    val quotes: List<QuoteRecord>,
    val deliveryStops: List<DeliveryStop>,
    val damageIncidents: List<DamageIncident>,
    val notifications: List<NotificationRecord>,
    val extrasJson: String = "",
)
