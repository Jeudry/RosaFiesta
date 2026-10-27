package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
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
    public val activeRole: StateFlow<String>
    public val currentUserEmail: StateFlow<String>
    public val currentSessionJwt: StateFlow<String?>
    public val currentAuthenticatedUser: StateFlow<RegisteredUserAccount?>
    public val registeredAccounts: StateFlow<List<RegisteredUserAccount>>
    public val favorites: StateFlow<Set<String>>
    public val staffMembers: StateFlow<List<StaffMember>>
    public val chatMessages: StateFlow<List<String>>

    public fun toggleChecklistStep(stepId: String)
    public fun syncOfflineQueue()
    public fun adjustArticleStock(articleId: String, delta: Int)
    public fun login(email: String, password: String): Boolean
    public fun authenticateUser(identifier: String, password: String): AuthOutcome
    public fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        phone: String = "",
    ): AuthOutcome
    public fun resetUserPassword(identifier: String, newPassword: String): AuthOutcome
    public fun switchRole(role: String)
    public fun toggleFavorite(articleId: String)
    public fun toggleStaffPaymentAccess(staffName: String)
    public fun sendChatMessage(author: String, message: String)
    public fun createArticle(
        code: String,
        name: String,
        category: String,
        pricePerDayDop: Int,
        totalStock: Int,
    )
    public fun createEvent(
        title: String,
        clientName: String,
        dateLabel: String,
        venue: String,
        guests: Int,
    )
    public fun changeEventDate(eventId: String, newDateLabel: String): DateChangeResult
    public fun cancelEvent(eventId: String): CancellationRefund
    public fun registerPayment(
        eventId: String,
        amountDop: Int,
        methodLabel: String,
        referenceCode: String,
    ): PaymentRecord?
}
