package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Implementación persistente de [RosaFiestaRepository] respaldada por SQLDelight ([RosaFiestaDatabase]).
 * Todas las consultas y mutaciones SQL se ejecutan fuera del hilo principal usando [ioDispatcher].
 */
public class SqlRosaFiestaRepository(
    driver: SqlDriver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : RosaFiestaRepository {
    private val database: RosaFiestaDatabase = RosaFiestaDatabase(driver)
    private val queries = database.rosaFiestaDatabaseQueries
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

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
            }
        }
        reloadFromSql()
    }

    private fun reloadFromSql() {
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

    /**
     * Ejecuta el cambio de estado de un paso del checklist en SQLite fuera del hilo principal.
     */
    public suspend fun toggleChecklistStepAsync(stepId: String): Unit = withContext(ioDispatcher) {
        val current = _checklist.value.firstOrNull { it.id == stepId } ?: return@withContext
        val nextCompleted = !current.completed
        val nextTimeLabel = if (nextCompleted) "11:20 AM" else "Pendiente"
        queries.updateChecklistStepStatus(
            completed = if (nextCompleted) 1L else 0L,
            timeLabel = nextTimeLabel,
            id = stepId,
        )
        reloadFromSql()
    }

    /**
     * Marca toda la cola offline como sincronizada en SQLite fuera del hilo principal.
     */
    public suspend fun syncOfflineQueueAsync(): Unit = withContext(ioDispatcher) {
        queries.markAllOfflineMutationsSynced()
        reloadFromSql()
    }

    /**
     * Ajusta el stock de un artículo en SQLite fuera del hilo principal.
     */
    public suspend fun adjustArticleStockAsync(articleId: String, delta: Int): Unit = withContext(ioDispatcher) {
        val current = _articles.value.firstOrNull { it.id == articleId } ?: return@withContext
        val newStock = (current.totalStock + delta).coerceAtLeast(0)
        val newAvail = (current.availableOct23 + delta).coerceIn(0, newStock)
        queries.updateArticleStock(
            totalStock = newStock.toLong(),
            availableOct23 = newAvail.toLong(),
            id = articleId,
        )
        reloadFromSql()
    }

    override fun toggleChecklistStep(stepId: String) {
        scope.launch {
            toggleChecklistStepAsync(stepId)
        }
    }

    override fun syncOfflineQueue() {
        scope.launch {
            syncOfflineQueueAsync()
        }
    }

    override fun adjustArticleStock(articleId: String, delta: Int) {
        scope.launch {
            adjustArticleStockAsync(articleId, delta)
        }
    }
}
