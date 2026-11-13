package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
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

/**
 * Reactive multiplatform implementation of [RosaFiestaRepository] seeded with [CanonicalSampleData]
 * and synchronized with [RosaFiestaApiClient] on Web (WasmJS) and offline-first environments.
 */
public class DefaultRosaFiestaRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val apiClient: RosaFiestaApiClient? = null,
) : RosaFiestaRepository {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

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
        submodules.propagateSessionTokenToApi(_currentSessionJwt.value)
        if (apiClient != null) {
            scope.launch {
                syncFromBackendAsync()
            }
        }
    }

    override suspend fun syncFromBackendAsync(): Boolean = submodules.syncFromBackendAsync()

    override fun refreshSharedState() {
        if (apiClient != null) {
            scope.launch {
                syncFromBackendAsync()
            }
        }
    }

    override fun toggleChecklistStep(stepId: String) {
        val cleanStepId = stepId.trim()
        val nowHm = DomainClockFormatter.currentTimeHmLabel()
        var targetEventId = ""
        var staffName = ""
        var nextCompleted = false
        _checklist.update { list ->
            list.map { step ->
                if (step.id == cleanStepId) {
                    nextCompleted = !step.completed
                    targetEventId = step.eventId
                    staffName = step.completedBy
                    step.copy(
                        completed = nextCompleted,
                        timeLabel = if (nextCompleted) nowHm else "Pendiente",
                    )
                } else {
                    step
                }
            }
        }
        val resolvedEventId = targetEventId.ifBlank {
            _events.value.firstOrNull()?.id.orEmpty()
        }
        if (resolvedEventId.isNotEmpty()) {
            scope.launch {
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
    }

    override fun syncOfflineQueue() {
        _offlineQueue.update { list ->
            list.map { item -> item.copy(synced = true) }
        }
        scope.launch {
            runCatching {
                apiClient?.syncOfflineQueue()
            }
        }
    }

    override fun adjustArticleStock(articleId: String, delta: Int) {
        var updatedArticle: CatalogArticle? = null
        _articles.update { list ->
            list.map { article ->
                if (article.id == articleId) {
                    val newStock = (article.totalStock + delta).coerceAtLeast(0)
                    val newAvail = (article.availableOct23 + delta).coerceIn(0, newStock)
                    val next = article.copy(totalStock = newStock, availableOct23 = newAvail)
                    updatedArticle = next
                    next
                } else {
                    article
                }
            }
        }
        val synced = updatedArticle
        if (synced != null) {
            scope.launch {
                runCatching {
                    apiClient?.updateCatalogArticle(
                        articleId = synced.id,
                        input = UpdateArticleInputDto(
                            totalStock = synced.totalStock.toLong(),
                            availableStock = synced.availableOct23.toLong(),
                        ),
                    )
                }
            }
        }
    }

    override fun login(email: String, password: String): Boolean = authenticateUser(email, password).success

    override suspend fun authenticateUserOnline(identifier: String, password: String): AuthOutcome = submodules.authenticateUserOnline(identifier, password, ::authenticateUser)

    override fun authenticateUser(identifier: String, password: String): AuthOutcome {
        val cleanIdentifier = identifier.trim().lowercase()
        val cleanPass = password.trim()
        val missingIdentifier = cleanIdentifier.isEmpty()
        val shortPassword = cleanPass.length < 4
        if (missingIdentifier) {
            return AuthOutcome.failure(
                errorCode = "AUTH_INVALID_CREDENTIALS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
            )
        }
        if (shortPassword) {
            return AuthOutcome.failure(
                errorCode = "AUTH_INVALID_CREDENTIALS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_INVALID_CREDENTIALS", null),
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
        val client = apiClient
        if (client != null) {
            scope.launch {
                val remoteLogin = runCatching {
                    client.login(LoginInputDto(email = matchedAccount.email, password = cleanPass))
                }.getOrNull()
                if (remoteLogin != null) {
                    _currentSessionJwt.value = remoteLogin.accessToken
                    client.setAccessToken(remoteLogin.accessToken)
                    syncFromBackendAsync()
                }
            }
        }
        return AuthOutcome.authenticated(accessToken = token, account = matchedAccount)
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

        val hasEmailAt = cleanEmail.contains("@")
        val hasEmailDomainDot = cleanEmail.substringAfter("@").contains(".")
        val validEmailFormat = if (hasEmailAt) hasEmailDomainDot else false
        val fullName = listOf(cleanFirst, cleanLast).filter { part -> part.isNotEmpty() }.joinToString(" ")
        val resolvedUsername = cleanUsername.ifEmpty { cleanEmail.substringBefore("@") }

        val emptyFullName = fullName.isEmpty()
        val shortUsername = resolvedUsername.length < 2
        if (emptyFullName) {
            return AuthOutcome.failure(
                errorCode = "AUTH_MISSING_FIELDS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_MISSING_FIELDS", null),
            )
        }
        if (shortUsername) {
            return AuthOutcome.failure(
                errorCode = "AUTH_MISSING_FIELDS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_MISSING_FIELDS", null),
            )
        }
        if (!validEmailFormat) {
            return AuthOutcome.failure(
                errorCode = "AUTH_MISSING_FIELDS",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_MISSING_FIELDS", null),
            )
        }
        if (cleanPassword.length < 6) {
            return AuthOutcome.failure(
                errorCode = "AUTH_WEAK_PASSWORD",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_WEAK_PASSWORD", null),
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
        val client = apiClient
        if (client != null) {
            scope.launch {
                val remoteOutput = runCatching {
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
                if (remoteOutput != null) {
                    _currentSessionJwt.value = remoteOutput.accessToken
                    client.setAccessToken(remoteOutput.accessToken)
                    syncFromBackendAsync()
                }
            }
        }
        return AuthOutcome.authenticated(accessToken = token, account = newAccount)
    }

    override fun resetUserPassword(identifier: String, newPassword: String): AuthOutcome {
        val cleanIdentifier = identifier.trim().lowercase()
        val cleanPassword = newPassword.trim()
        if (cleanPassword.length < 6) {
            return AuthOutcome.failure(
                errorCode = "AUTH_WEAK_PASSWORD",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_WEAK_PASSWORD", null),
            )
        }
        val targetIdentifier = cleanIdentifier.ifEmpty { _currentUserEmail.value }
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
        val emailOccupied = allAccounts.any { account ->
            val different = account.id != currentAccount.id
            val sameEmail = account.email.equals(cleanEmail, ignoreCase = true)
            if (different) sameEmail else false
        }
        if (emailOccupied) {
            return AuthOutcome.failure(
                errorCode = "AUTH_EMAIL_TAKEN",
                errorMessage = SensitiveDataRedactor.translateBusinessError("AUTH_EMAIL_TAKEN", null),
            )
        }
        val usernameOccupied = allAccounts.any { account ->
            val different = account.id != currentAccount.id
            val sameUsername = account.username.equals(cleanUsername, ignoreCase = true)
            if (different) sameUsername else false
        }
        if (usernameOccupied) {
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
            accounts.map { account ->
                if (account.id == updatedAccount.id) updatedAccount else account
            }
        }
        val token = JwtAuthManager.issueAccessToken(updatedAccount)
        _currentAuthenticatedUser.value = updatedAccount
        _currentSessionJwt.value = token
        _currentUserEmail.value = updatedAccount.email
        submodules.propagateSessionTokenToApi(token)
        scope.launch {
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
            reconstructed
        }
        _currentAuthenticatedUser.value = resolvedAccount
        _currentSessionJwt.value = cleanToken
        _currentUserEmail.value = resolvedAccount.email
        _activeRole.value = resolvedAccount.role
        submodules.propagateSessionTokenToApi(cleanToken)
        return resolvedAccount
    }

    override fun clearSession() {
        _currentSessionJwt.value = null
        apiClient?.setAccessToken(null)
    }

    override fun deleteCurrentAccount(): Boolean {
        val currentToken = _currentSessionJwt.value
        _currentSessionJwt.value = null
        _currentAuthenticatedUser.value = null
        _activeRole.value = "guest"
        apiClient?.setAccessToken(null)
        if (!currentToken.isNullOrBlank()) {
            scope.launch {
                runCatching {
                    apiClient?.deleteAccount(currentToken)
                }
            }
        }
        return true
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
        if (synced != null) {
            if (submodules.hasRealServerToken()) {
                scope.launch {
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
        if (submodules.hasRealServerToken()) {
            val threadId = submodules.chatThreads.value.firstOrNull()?.id ?: "thread-general"
            scope.launch {
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
            runCatching {
                apiClient?.createCatalogArticle(
                    CreateArticleInputDto(
                        id = newArticle.id,
                        name = newArticle.name,
                        categoryId = newArticle.category,
                        photoKey = photo.key,
                        dailyPriceDop = pricePerDayDop.toLong(),
                        totalStock = totalStock.toLong(),
                        availableStock = totalStock.toLong(),
                        description = newArticle.description,
                        visible = true,
                    ),
                )
            }
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

        val proposal = RosaFiestaEngine.buildRosaProposal(cleanTitle, guests)
        val lines = proposal.lines.map { line ->
            EventLineItem(
                articleCode = line.code,
                name = line.name,
                quantity = line.quantity,
                unitPriceDop = line.unitPriceDop,
                photo = RosaFiestaBackendMappers.selectPhotoForArticle("", line.name),
            )
        }
        val deliveryFeeDop = RosaFiestaBackendMappers.resolveDeliveryFeeDop(cleanVenue, businessSettings.value)
        val linesSubtotal = lines.sumOf { line -> line.subtotalDop }
        val consistentTotalDop = linesSubtotal + deliveryFeeDop
        val computedDaysUntil = DomainClockFormatter.computeDaysUntilEvent(cleanDate)
        val assignedLead = RosaFiestaBackendMappers.selectLeastLoadedStaffLead(_staffMembers.value, _events.value)
        val seq = 1120 + _events.value.size
        val newEvent = ClientEvent(
            id = "evt-$seq",
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
        submodules.recomputeHonestMetrics()
        scope.launch {
            runCatching {
                val eventItems = lines.map { item ->
                    EventItemDto(
                        articleCode = item.articleCode,
                        name = item.name,
                        quantity = item.quantity.toLong(),
                        unitPriceRD = item.unitPriceDop.toLong(),
                    )
                }
                apiClient?.createRemoteEvent(
                    CreateEventInputDto(
                        title = cleanTitle,
                        clientName = cleanClient,
                        dateLabel = cleanDate,
                        daysUntilEvent = computedDaysUntil.toLong(),
                        venue = cleanVenue,
                        zone = newEvent.zone,
                        guests = guests.toLong(),
                        assignedLead = assignedLead,
                        totalRD = consistentTotalDop.toLong(),
                        items = eventItems,
                    ),
                )
            }
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
        if (eval.feeDop > 0) {
            _metrics.update { current ->
                current.copy(accountsReceivableDop = current.accountsReceivableDop + eval.feeDop)
            }
        }
        val targetDays = DomainClockFormatter.computeDaysUntilEvent(cleanDate).toLong()
        scope.launch {
            runCatching {
                apiClient?.requestDateChange(
                    id = currentEvent.id,
                    input = DateChangeInputDto(
                        newDateLabel = cleanDate,
                        newDaysUntilEvent = targetDays,
                    ),
                )
            }
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
        _metrics.update { current ->
            current.copy(
                activeEventsCount = (current.activeEventsCount - 1).coerceAtLeast(0),
                accountsReceivableDop = (current.accountsReceivableDop - currentEvent.balanceDop).coerceAtLeast(0),
            )
        }
        scope.launch {
            runCatching {
                apiClient?.cancelEvent(currentEvent.id)
            }
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
        val normalizedPayment = RosaFiestaBackendMappers.normalizePaymentMethod(cleanMethod)
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
        _metrics.update { current ->
            current.copy(
                octoberRevenueDop = current.octoberRevenueDop + amountDop,
                accountsReceivableDop = (current.accountsReceivableDop - amountDop).coerceAtLeast(0),
            )
        }
        scope.launch {
            runCatching {
                apiClient?.registerPayment(
                    RegisterPaymentInputDto(
                        eventId = record.eventId,
                        eventTitle = record.eventTitle,
                        clientName = record.clientName,
                        amountRD = record.amountDop.toLong(),
                        method = normalizedPayment.canonicalMethod,
                        kind = record.kindLabel,
                        referenceCode = cleanRef,
                        bankName = normalizedPayment.bankName,
                    ),
                )
            }
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
