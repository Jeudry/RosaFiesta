package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implementación reactiva de [RosaFiestaRepository] inicializada con [CanonicalSampleData]
 * y preparada para operar offline-first (`A36`, `A37`).
 */
public class DefaultRosaFiestaRepository : RosaFiestaRepository {
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

    override fun toggleChecklistStep(stepId: String) {
        _checklist.update { list ->
            list.map { step ->
                if (step.id == stepId) {
                    step.copy(
                        completed = !step.completed,
                        timeLabel = if (!step.completed) "11:20 AM" else "Pendiente",
                    )
                } else {
                    step
                }
            }
        }
    }

    override fun syncOfflineQueue() {
        _offlineQueue.update { list ->
            list.map { it.copy(synced = true) }
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
    }

    override fun login(email: String, password: String): Boolean = authenticateUser(email, password).success

    override fun authenticateUser(identifier: String, password: String): AuthOutcome {
        val cleanIdentifier = identifier.trim().lowercase()
        val cleanPass = password.trim()
        val isMissingCredentials = cleanIdentifier.isEmpty() || cleanPass.length < 4
        if (isMissingCredentials) {
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
        val validEmailFormat = hasEmailAt && hasEmailDomainDot
        val fullName = listOf(cleanFirst, cleanLast).filter { part -> part.isNotEmpty() }.joinToString(" ")
        val resolvedUsername = cleanUsername.ifEmpty { cleanEmail.substringBefore("@") }

        val missingCoreFields = fullName.isEmpty() || resolvedUsername.length < 2 || !validEmailFormat
        if (missingCoreFields) {
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
        val resolvedRole = when {
            cleanEmail.startsWith("maria") -> "admin"
            cleanEmail.startsWith("yuli") -> "staff_yuli"
            else -> "client"
        }
        val newAccount = RegisteredUserAccount(
            id = "usr-${existingAccounts.size + 1}-$resolvedUsername",
            username = resolvedUsername,
            email = cleanEmail,
            fullName = fullName,
            passwordHash = passwordHash,
            salt = salt,
            birthDate = cleanBirthDate,
            phone = cleanPhone,
            role = resolvedRole,
        )
        userAccountsState.update { accounts -> accounts + newAccount }
        val token = JwtAuthManager.issueAccessToken(newAccount)
        _currentAuthenticatedUser.value = newAccount
        _currentSessionJwt.value = token
        _currentUserEmail.value = newAccount.email
        _activeRole.value = newAccount.role
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
        return AuthOutcome.authenticated(accessToken = token, account = updatedAccount)
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
                        if (nextCanView) "Decoración, tareas y consulta de pagos" else "Decoración y tareas (sin acceso a pagos)"
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
    }

    override fun sendChatMessage(author: String, message: String) {
        val cleanMsg = message.trim()
        if (cleanMsg.isEmpty()) return
        val cleanAuthor = author.trim().ifEmpty { "Camila" }
        _chatMessages.update { it + "$cleanAuthor: $cleanMsg" }
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
        if (cleanCode.isEmpty() || cleanName.isEmpty() || pricePerDayDop <= 0 || totalStock < 0) {
            return
        }
        val photo = when {
            cleanCategory.contains("flor", ignoreCase = true) -> PhotoAsset.CENTRO_FLORAL
            cleanCategory.contains("arco", ignoreCase = true) || cleanCategory.contains("globo", ignoreCase = true) -> PhotoAsset.ARCO_ROSA
            cleanCategory.contains("dulce", ignoreCase = true) || cleanCategory.contains("helado", ignoreCase = true) -> PhotoAsset.MESA_HELADOS
            cleanName.contains("mesa", ignoreCase = true) -> PhotoAsset.MESA_REDONDA
            else -> PhotoAsset.SILLA_TIFFANY
        }
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
            val filtered = list.filterNot { it.id == newArticle.id || it.code.equals(cleanCode, ignoreCase = true) }
            filtered + newArticle
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
        if (cleanTitle.isEmpty() || cleanClient.isEmpty() || cleanDate.isEmpty() || cleanVenue.isEmpty() || guests <= 0) {
            return
        }
        val proposal = RosaFiestaEngine.buildRosaProposal(cleanTitle, guests)
        val lines = proposal.lines.map { line ->
            EventLineItem(
                articleCode = line.code,
                name = line.name,
                quantity = line.quantity,
                unitPriceDop = line.unitPriceDop,
                photo = PhotoAsset.SILLA_TIFFANY,
            )
        }
        val seq = 1120 + _events.value.size
        val newEvent = ClientEvent(
            id = "evt-$seq",
            code = "RF-2026-$seq",
            title = cleanTitle,
            clientName = cleanClient,
            eventType = "Evento",
            dateLabel = cleanDate,
            daysUntilEvent = 14,
            venue = cleanVenue,
            zone = "Santo Domingo DN",
            guests = guests,
            articleCount = lines.sumOf { it.quantity },
            totalDop = proposal.totalDop,
            paidDepositDop = 0,
            depositMethodLabel = "Pendiente de anticipo",
            depositDateLabel = "—",
            balanceDueDateLabel = cleanDate,
            status = EventStatus.PENDING_QUOTE,
            leadStaffName = "Pedro",
            photo = PhotoAsset.BODA,
            items = lines,
        )
        _events.update { it + newEvent }
        _metrics.update { current ->
            current.copy(
                activeEventsCount = current.activeEventsCount + 1,
                newRequestsCount = current.newRequestsCount + 1,
                accountsReceivableDop = current.accountsReceivableDop + newEvent.totalDop,
            )
        }
    }

    override fun changeEventDate(eventId: String, newDateLabel: String): DateChangeResult {
        val cleanDate = newDateLabel.trim()
        val currentEvent = _events.value.firstOrNull { it.id == eventId.trim() } ?: _events.value.first()
        val eval = RosaFiestaEngine.evaluateDateChange(currentEvent.daysUntilEvent, currentEvent.priorChangesCount)
        if (!eval.allowed || cleanDate.isEmpty()) {
            return eval
        }
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
            _metrics.update { it.copy(accountsReceivableDop = it.accountsReceivableDop + eval.feeDop) }
        }
        return eval
    }

    override fun cancelEvent(eventId: String): CancellationRefund {
        val currentEvent = _events.value.firstOrNull { it.id == eventId.trim() } ?: _events.value.first()
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
        if (amountDop <= 0 || cleanMethod.isEmpty()) {
            return null
        }
        val targetEvent = _events.value.firstOrNull { it.id == eventId.trim() } ?: _events.value.first()
        val nextNumber = 185 + _payments.value.size
        val receipt = "REC-2026-${nextNumber.toString().padStart(4, '0')}"
        val record = PaymentRecord(
            id = "pay-${_payments.value.size + 1}",
            receiptNumber = receipt,
            eventId = targetEvent.id,
            eventTitle = targetEvent.title,
            clientName = targetEvent.clientName,
            amountDop = amountDop,
            methodLabel = "$cleanMethod · 20 oct",
            kindLabel = if (amountDop >= targetEvent.balanceDop) "Liquidación" else "Abono",
            dateLabel = "20 oct 2026",
            referenceCode = cleanRef,
            verified = true,
        )
        _payments.update { listOf(record) + it }
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
        return record
    }
}
