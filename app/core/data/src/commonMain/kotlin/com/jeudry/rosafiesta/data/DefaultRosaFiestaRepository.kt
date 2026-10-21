package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
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
}
