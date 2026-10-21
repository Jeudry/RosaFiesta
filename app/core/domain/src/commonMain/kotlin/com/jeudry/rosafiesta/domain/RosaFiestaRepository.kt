package com.jeudry.rosafiesta.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato de estado y operaciones compartidas entre la App Personal y la App Administrativa.
 */
public interface RosaFiestaRepository {
    public val articles: StateFlow<List<CatalogArticle>>
    public val events: StateFlow<List<ClientEvent>>
    public val payments: StateFlow<List<PaymentRecord>>
    public val checklist: StateFlow<List<ChecklistStep>>
    public val offlineQueue: StateFlow<List<OfflineQueueItem>>
    public val metrics: StateFlow<BusinessMetrics>

    public fun toggleChecklistStep(stepId: String)
    public fun syncOfflineQueue()
    public fun adjustArticleStock(articleId: String, delta: Int)
}
