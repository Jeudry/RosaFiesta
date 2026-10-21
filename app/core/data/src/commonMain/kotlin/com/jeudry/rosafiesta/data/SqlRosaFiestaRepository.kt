package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.core.engine.UserRole
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase
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
 * Implementación persistente de [RosaFiestaRepository] respaldada por SQLDelight ([RosaFiestaDatabase]).
 * Todas las consultas y mutaciones SQL se ejecutan fuera del hilo principal usando [ioDispatcher].
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

    private val _favorites = MutableStateFlow(CanonicalSampleData.initialFavorites)
    override val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _staffMembers = MutableStateFlow(CanonicalSampleData.staffMembers)
    override val staffMembers: StateFlow<List<StaffMember>> = _staffMembers.asStateFlow()

    private val _chatMessages = MutableStateFlow(CanonicalSampleData.initialChatMessages)
    override val chatMessages: StateFlow<List<String>> = _chatMessages.asStateFlow()

    init {
        seedInitialDataSynchronouslyIfEmpty()
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
                        permissionsCsv = member.permissions.joinToString(",") { it.name },
                        canViewPayments = if (member.canViewPayments) 1L else 0L,
                        sortOrder = index.toLong(),
                    )
                }
                CanonicalSampleData.initialChatMessages.forEach { msg ->
                    queries.insertChatMessage(msg)
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
    }

    private fun reloadArticlesFromSql() {
        val variantsById = CanonicalSampleData.articles.associate { it.id to it.variants }
        _articles.value = queries.selectAllArticles().executeAsList().map { row ->
            val photoAsset = PhotoAsset.entries.firstOrNull { it.key == row.photoKey } ?: PhotoAsset.SILLA_TIFFANY
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
                variants = variantsById[row.id] ?: listOf("Estándar"),
            )
        }
    }

    private fun reloadChecklistFromSql() {
        _checklist.value = queries.selectAllChecklistSteps().executeAsList().map { row ->
            ChecklistStep(
                id = row.id,
                stageLabel = row.stageLabel,
                title = row.title,
                completed = row.completed != 0L,
                completedBy = row.completedBy,
                timeLabel = row.timeLabel,
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
        val itemsByEventId = CanonicalSampleData.events.associate { it.id to it.items }
        val loadedEvents = queries.selectAllEvents().executeAsList().map { row ->
            val photoAsset = PhotoAsset.entries.firstOrNull { it.key == row.photoKey } ?: PhotoAsset.BODA
            val statusEnum = EventStatus.entries.firstOrNull { it.name == row.status } ?: EventStatus.CONFIRMED
            val fallbackItems = itemsByEventId[row.id] ?: buildFallbackItems(row.title, row.guests.toInt())
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
        val currentEvents = _events.value
        val currentPayments = _payments.value
        val activeEvents = currentEvents.filter {
            it.status != EventStatus.CANCELLED && it.status != EventStatus.COMPLETED
        }
        val pendingQuoteCount = currentEvents.count { it.status == EventStatus.PENDING_QUOTE }
        val totalPaymentsDop = currentPayments.sumOf { it.amountDop }
        val activeBalanceDop = activeEvents.sumOf { it.balanceDop }
        _metrics.value = CanonicalSampleData.businessMetrics.copy(
            octoberRevenueDop = (98 + totalPaymentsDop).coerceAtLeast(0),
            accountsReceivableDop = (activeBalanceDop - 1669).coerceAtLeast(0),
            activeEventsCount = activeEvents.size,
            newRequestsCount = 1 + pendingQuoteCount,
        )
    }

    private fun reloadFavoritesFromSql() {
        _favorites.value = queries.selectAllFavorites().executeAsList().toSet()
    }

    private fun reloadStaffFromSql() {
        val loadedStaff = queries.selectAllStaffPermissions().executeAsList().map { row ->
            val roleEnum = UserRole.entries.firstOrNull { it.name == row.role } ?: UserRole.STAFF_DECORATION
            val perms = row.permissionsCsv
                .split(",")
                .mapNotNull { token -> StaffPermission.entries.firstOrNull { it.name == token.trim() } }
                .toSet()
            StaffMember(
                id = row.id,
                name = row.name,
                roleLabel = row.roleLabel,
                role = roleEnum,
                activeEventsCount = row.activeEventsCount.toInt(),
                permissions = perms,
                canViewPayments = row.canViewPayments != 0L,
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
                photo = PhotoAsset.SILLA_TIFFANY,
            )
        }
    }

    /**
     * Ejecuta el cambio de estado de un paso del checklist en SQLite fuera del hilo principal.
     */
    public suspend fun toggleChecklistStepAsync(stepId: String): Unit = withContext(ioDispatcher) {
        var staffName = ""
        var nextCompleted = false
        sqlMutex.withLock {
            val current = queries.selectAllChecklistSteps().executeAsList().firstOrNull { it.id == stepId } ?: return@withContext
            nextCompleted = current.completed == 0L
            staffName = current.completedBy
            val nextTimeLabel = if (nextCompleted) "11:20 AM" else "Pendiente"
            queries.updateChecklistStepStatus(
                completed = if (nextCompleted) 1L else 0L,
                timeLabel = nextTimeLabel,
                id = stepId,
            )
            reloadChecklistFromSql()
        }
        runCatching {
            apiClient?.toggleChecklistItem(
                eventId = "evt-boda-real",
                itemId = stepId,
                input = ToggleChecklistInputDto(
                    completed = nextCompleted,
                    staffName = staffName,
                ),
            )
        }
    }

    /**
     * Marca toda la cola offline como sincronizada en SQLite fuera del hilo principal.
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
     * Ajusta el stock de un artículo en SQLite fuera del hilo principal.
     */
    public suspend fun adjustArticleStockAsync(articleId: String, delta: Int): Unit = withContext(ioDispatcher) {
        sqlMutex.withLock {
            val current = queries.selectAllArticles().executeAsList().firstOrNull { it.id == articleId } ?: return@withContext
            val newStock = (current.totalStock.toInt() + delta).coerceAtLeast(0)
            val newAvail = (current.availableOct23.toInt() + delta).coerceIn(0, newStock)
            queries.updateArticleStock(
                totalStock = newStock.toLong(),
                availableOct23 = newAvail.toLong(),
                id = articleId,
            )
            reloadArticlesFromSql()
        }
    }

    /**
     * Alterna un favorito en SQLite fuera del hilo principal.
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
     * Alterna el permiso de visualización de pagos de un miembro del staff en SQLite fuera del hilo principal.
     */
    public suspend fun toggleStaffPaymentAccessAsync(staffName: String): Unit = withContext(ioDispatcher) {
        val target = staffName.trim()
        if (target.isEmpty()) return@withContext
        sqlMutex.withLock {
            val current = queries.selectAllStaffPermissions().executeAsList()
                .firstOrNull { it.name.equals(target, ignoreCase = true) } ?: return@withContext
            val nextCanView = current.canViewPayments == 0L
            val existingPerms = current.permissionsCsv
                .split(",")
                .mapNotNull { token -> StaffPermission.entries.firstOrNull { it.name == token.trim() } }
                .toSet()
            val nextPerms = if (nextCanView) {
                existingPerms + StaffPermission.PAYMENTS
            } else {
                existingPerms - StaffPermission.PAYMENTS
            }
            val nextLabel = if (current.name.equals("Yuli", ignoreCase = true)) {
                if (nextCanView) "Decoración, tareas y consulta de pagos" else "Decoración y tareas (sin acceso a pagos)"
            } else {
                current.roleLabel
            }
            queries.updateStaffPaymentAccess(
                canViewPayments = if (nextCanView) 1L else 0L,
                permissionsCsv = nextPerms.joinToString(",") { it.name },
                roleLabel = nextLabel,
                name = current.name,
            )
            reloadStaffFromSql()
        }
    }

    /**
     * Inserta un mensaje de chat en SQLite fuera del hilo principal.
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
     * Crea o actualiza un artículo en SQLite fuera del hilo principal.
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
        if (cleanCode.isEmpty() || cleanName.isEmpty() || pricePerDayDop <= 0 || totalStock < 0) {
            return@withContext
        }
        val photo = selectPhotoForArticle(cleanCategory, cleanName)
        sqlMutex.withLock {
            queries.upsertArticle(
                id = "art-${cleanCode.lowercase()}",
                code = cleanCode,
                name = cleanName,
                category = cleanCategory,
                description = "$cleanName para eventos y celebraciones en Santo Domingo.",
                pricePerDayDop = pricePerDayDop.toLong(),
                totalStock = totalStock.toLong(),
                availableOct23 = totalStock.toLong(),
                photoKey = photo.key,
            )
            reloadArticlesFromSql()
        }
    }

    /**
     * Crea un evento nuevo en SQLite fuera del hilo principal.
     */
    public suspend fun createEventAsync(
        title: String,
        clientName: String,
        dateLabel: String,
        venue: String,
        guests: Int,
    ): Unit = withContext(ioDispatcher) {
        val cleanTitle = title.trim()
        val cleanClient = clientName.trim()
        val cleanDate = dateLabel.trim()
        val cleanVenue = venue.trim()
        if (cleanTitle.isEmpty() || cleanClient.isEmpty() || cleanDate.isEmpty() || cleanVenue.isEmpty() || guests <= 0) {
            return@withContext
        }
        sqlMutex.withLock {
            val existingCount = queries.selectAllEvents().executeAsList().size
            val proposal = RosaFiestaEngine.buildRosaProposal(cleanTitle, guests)
            val seq = 1120 + existingCount
            queries.upsertEvent(
                id = "evt-$seq",
                code = "RF-2026-$seq",
                title = cleanTitle,
                clientName = cleanClient,
                eventType = "Evento",
                dateLabel = cleanDate,
                daysUntilEvent = 14L,
                venue = cleanVenue,
                zone = "Santo Domingo DN",
                guests = guests.toLong(),
                articleCount = proposal.lines.sumOf { it.quantity }.toLong(),
                totalDop = proposal.totalDop.toLong(),
                paidDepositDop = 0L,
                depositMethodLabel = "Pendiente de anticipo",
                depositDateLabel = "—",
                balanceDueDateLabel = cleanDate,
                status = EventStatus.PENDING_QUOTE.name,
                leadStaffName = "Pedro",
                photoKey = PhotoAsset.BODA.key,
                priorChangesCount = 0L,
                sortOrder = existingCount.toLong(),
            )
            reloadEventsFromSql()
        }
    }

    /**
     * Evalúa y persiste el cambio de fecha de un evento en SQLite fuera del hilo principal.
     */
    public suspend fun changeEventDateAsync(eventId: String, newDateLabel: String): DateChangeResult = withContext(ioDispatcher) {
        val cleanDate = newDateLabel.trim()
        sqlMutex.withLock {
            val rows = queries.selectAllEvents().executeAsList()
            val target = rows.firstOrNull { it.id == eventId.trim() } ?: rows.first()
            val eval = RosaFiestaEngine.evaluateDateChange(target.daysUntilEvent.toInt(), target.priorChangesCount.toInt())
            if (eval.allowed && cleanDate.isNotEmpty()) {
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
    }

    /**
     * Cancela un evento en SQLite fuera del hilo principal y devuelve el desglose de reembolso.
     */
    public suspend fun cancelEventAsync(eventId: String): CancellationRefund = withContext(ioDispatcher) {
        sqlMutex.withLock {
            val rows = queries.selectAllEvents().executeAsList()
            val target = rows.firstOrNull { it.id == eventId.trim() } ?: rows.first()
            val refund = RosaFiestaEngine.calculateCancellationRefund(target.paidDepositDop.toInt(), target.daysUntilEvent.toInt())
            queries.updateEventStatus(
                status = EventStatus.CANCELLED.name,
                id = target.id,
            )
            reloadEventsFromSql()
            refund
        }
    }

    /**
     * Registra un pago en SQLite fuera del hilo principal y devuelve el comprobante generado.
     */
    public suspend fun registerPaymentAsync(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
    ): PaymentRecord? = withContext(ioDispatcher) {
        val cleanMethod = methodLabel.trim()
        val cleanRef = referenceCode.trim().ifEmpty { "REF-2026" }
        if (amountDop <= 0 || cleanMethod.isEmpty()) {
            return@withContext null
        }
        sqlMutex.withLock {
            val eventRows = queries.selectAllEvents().executeAsList()
            val target = eventRows.firstOrNull { it.id == eventId.trim() } ?: eventRows.first()
            val existingPayments = queries.selectAllPayments().executeAsList()
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
                methodLabel = "$cleanMethod · 20 oct",
                kindLabel = if (amountDop >= balanceDop) "Liquidación" else "Abono",
                dateLabel = "20 oct 2026",
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
    }

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
        scope.launch {
            toggleChecklistStepAsync(stepId)
        }
    }

    override fun syncOfflineQueue() {
        _offlineQueue.update { list ->
            list.map { it.copy(synced = true) }
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

    override fun login(email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = password.trim()
        val isValidEmail = cleanEmail.contains("@") && cleanEmail.substringAfter("@").contains(".")
        if (!isValidEmail || cleanPass.length < 4) {
            return false
        }
        _currentUserEmail.value = cleanEmail
        _activeRole.value = when {
            cleanEmail.startsWith("maria") -> "admin"
            cleanEmail.startsWith("yuli") -> "staff_yuli"
            else -> "client"
        }
        return true
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
        scope.launch {
            toggleStaffPaymentAccessAsync(target)
        }
    }

    override fun sendChatMessage(author: String, message: String) {
        val cleanMsg = message.trim()
        if (cleanMsg.isEmpty()) return
        val cleanAuthor = author.trim().ifEmpty { "Camila" }
        _chatMessages.update { it + "$cleanAuthor: $cleanMsg" }
        scope.launch {
            sendChatMessageAsync(cleanAuthor, cleanMsg)
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
        if (cleanCode.isEmpty() || cleanName.isEmpty() || pricePerDayDop <= 0 || totalStock < 0) {
            return
        }
        val photo = selectPhotoForArticle(cleanCategory, cleanName)
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
        if (cleanTitle.isEmpty() || cleanClient.isEmpty() || cleanDate.isEmpty() || cleanVenue.isEmpty() || guests <= 0) {
            return
        }
        val proposal = RosaFiestaEngine.buildRosaProposal(cleanTitle, guests)
        val lines = buildFallbackItems(cleanTitle, guests)
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
        scope.launch {
            createEventAsync(cleanTitle, cleanClient, cleanDate, cleanVenue, guests)
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
        scope.launch {
            changeEventDateAsync(currentEvent.id, cleanDate)
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
        scope.launch {
            registerPaymentAsync(targetEvent.id, amountDop, cleanMethod, cleanRef)
        }
        return record
    }

    private fun selectPhotoForArticle(category: String, name: String): PhotoAsset = when {
        category.contains("flor", ignoreCase = true) -> PhotoAsset.CENTRO_FLORAL
        category.contains("arco", ignoreCase = true) || category.contains("globo", ignoreCase = true) -> PhotoAsset.ARCO_ROSA
        category.contains("dulce", ignoreCase = true) || category.contains("helado", ignoreCase = true) -> PhotoAsset.MESA_HELADOS
        name.contains("mesa", ignoreCase = true) -> PhotoAsset.MESA_REDONDA
        else -> PhotoAsset.SILLA_TIFFANY
    }
}
