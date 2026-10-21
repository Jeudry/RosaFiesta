package com.jeudry.rosafiesta.ui

import androidx.lifecycle.ViewModel
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel multiplataforma compartido para `PersonalApp` y `AdminApp`.
 */
public class RosaFiestaViewModel(
    private val repository: RosaFiestaRepository,
) : ViewModel() {
    public val articles: StateFlow<List<CatalogArticle>> = repository.articles
    public val events: StateFlow<List<ClientEvent>> = repository.events
    public val payments: StateFlow<List<PaymentRecord>> = repository.payments
    public val checklist: StateFlow<List<ChecklistStep>> = repository.checklist
    public val offlineQueue: StateFlow<List<OfflineQueueItem>> = repository.offlineQueue
    public val metrics: StateFlow<BusinessMetrics> = repository.metrics

    public fun toggleChecklistStep(stepId: String) {
        repository.toggleChecklistStep(stepId)
    }

    public fun syncOfflineQueue() {
        repository.syncOfflineQueue()
    }

    public fun adjustArticleStock(articleId: String, delta: Int) {
        repository.adjustArticleStock(articleId, delta)
    }
}
