package com.jeudry.rosafiesta.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

public typealias AdminClientDto = ClientSummaryDto
public typealias StaffRecordDto = StaffMemberDto

@Serializable
public data class LogoutOutputDto(
    public val loggedOut: Boolean,
)

@Serializable
public data class UpdateProfileInputDto(
    public val avatarKey: String? = null,
    public val birthDate: String? = null,
    public val email: String? = null,
    public val fullName: String? = null,
    public val phone: String? = null,
    public val username: String? = null,
)

@Serializable
public data class ChangePasswordInputDto(
    public val currentPassword: String,
    public val newPassword: String,
)

@Serializable
public data class PasswordChangedOutputDto(
    public val updated: Boolean,
)

@Serializable
public data class OtpRequestInputDto(
    public val email: String,
)

@Serializable
public data class OtpRequestResultDto(
    public val sent: Boolean,
    public val expiresInSeconds: Long,
    public val devCode: String? = null,
)

@Serializable
public data class OtpVerifyInputDto(
    public val email: String,
    public val code: String,
)

@Serializable
public data class OtpVerifyResultDto(
    public val verified: Boolean,
    public val resetToken: String,
    public val expiresInSeconds: Long,
)

@Serializable
public data class OAuthLoginInputDto(
    public val provider: String,
    public val idToken: String,
)

@Serializable
public data class OAuthStartResultDto(
    public val provider: String,
    public val authorizeUrl: String,
    public val state: String,
)

@Serializable
public data class OAuthExchangeInputDto(
    public val provider: String,
    public val code: String,
    public val codeVerifier: String,
    public val redirectUri: String,
    public val state: String = "",
)

@Serializable
public data class PushTokenInputDto(
    public val platform: String,
    public val token: String,
    public val appVariant: String = "personal",
)

@Serializable
public data class DevicePushTokenDto(
    public val id: String,
    public val userId: String,
    public val platform: String,
    public val token: String,
    public val appVariant: String,
    public val enabled: Boolean,
    public val updatedAt: String,
)

@Serializable
public data class PushTokenOutputDto(
    public val registered: Boolean,
    public val device: DevicePushTokenDto,
)

@Serializable
public data class DeletePushTokenOutputDto(
    public val disabled: Boolean,
)

@Serializable
public data class UpdateClientInputDto(
    public val fullName: String? = null,
    public val email: String? = null,
    public val phone: String? = null,
    public val notes: String? = null,
)

@Serializable
public data class ClientOutputDto(
    public val client: ClientSummaryDto,
)

@Serializable
public data class InviteStaffInputDto(
    public val fullName: String,
    public val email: String,
    public val permissions: List<String>,
    public val password: String? = null,
    public val initialPassword: String? = null,
    public val phone: String? = null,
    public val role: String? = null,
    public val roleLabel: String? = null,
)

@Serializable
public data class UpdateStaffInputDto(
    public val active: Boolean? = null,
    public val fullName: String? = null,
    public val permissions: List<String>? = null,
    public val role: String? = null,
    public val roleLabel: String? = null,
)

@Serializable
public data class StaffMemberOutputDto(
    public val member: StaffMemberDto,
)

@Serializable
public data class PackageItemDto(
    public val articleId: String,
    public val articleName: String,
    public val quantity: Long,
    public val unitPriceDop: Long,
)

@Serializable
public data class CreateArticleInputDto(
    public val name: String,
    public val dailyPriceDop: Long,
    public val totalStock: Long,
    public val id: String? = null,
    public val categoryId: String? = null,
    public val photoKey: String? = null,
    public val photoUrl: String? = null,
    public val availableStock: Long? = null,
    public val damagedStock: Long? = null,
    public val lowStockThreshold: Long? = null,
    public val description: String? = null,
    public val colorVariants: List<String>? = null,
    public val dimensions: String? = null,
    public val material: String? = null,
    public val isDirectSale: Boolean? = null,
    public val visible: Boolean? = null,
)

@Serializable
public data class UpdateArticleInputDto(
    public val name: String? = null,
    public val categoryId: String? = null,
    public val photoKey: String? = null,
    public val photoUrl: String? = null,
    public val dailyPriceDop: Long? = null,
    public val totalStock: Long? = null,
    public val damagedStock: Long? = null,
    public val availableStock: Long? = null,
    public val lowStockThreshold: Long? = null,
    public val description: String? = null,
    public val colorVariants: List<String>? = null,
    public val dimensions: String? = null,
    public val material: String? = null,
    public val visible: Boolean? = null,
)

@Serializable
public data class ArticleOutputDto(
    public val article: ArticleDto,
)

@Serializable
public data class SavePackageInputDto(
    public val name: String,
    public val guestCapacity: Long,
    public val totalPriceDop: Long,
    public val id: String? = null,
    public val eventType: String? = null,
    public val photoKey: String? = null,
    public val photoUrl: String? = null,
    public val itemsSummary: String? = null,
    public val items: List<PackageItemDto>? = null,
    public val visible: Boolean? = null,
)

@Serializable
public data class UpdatePackageInputDto(
    public val name: String,
    public val guestCapacity: Long,
    public val totalPriceDop: Long,
    public val eventType: String? = null,
    public val photoKey: String? = null,
    public val photoUrl: String? = null,
    public val itemsSummary: String? = null,
    public val items: List<PackageItemDto>? = null,
    public val visible: Boolean? = null,
)

@Serializable
public data class PackageOutputDto(
    @SerialName("package")
    public val packageBundle: PackageBundleDto,
)

@Serializable
public data class AvailabilityDayDto(
    public val date: String,
    public val reserved: Long,
    public val available: Long,
    public val lowStock: Boolean,
)

@Serializable
public data class ArticleAvailabilityDto(
    public val articleId: String,
    public val articleName: String,
    public val month: String,
    public val totalStock: Long,
    public val days: List<AvailabilityDayDto>? = null,
)

@Serializable
public data class AvailabilityOutputDto(
    public val availability: ArticleAvailabilityDto,
)

@Serializable
public data class CreateEventInputDto(
    public val title: String,
    public val dateLabel: String,
    public val venue: String,
    public val guests: Long,
    public val eventDateIso: String? = null,
    public val clientName: String? = null,
    public val clientEmail: String? = null,
    public val eventType: String? = null,
    public val zone: String? = null,
    public val daysUntilEvent: Long? = null,
    public val assignedLead: String? = null,
    public val balanceDueDate: String? = null,
    public val totalRD: Long? = null,
    public val paidDepositRD: Long? = null,
    public val sketchJson: String? = null,
    public val items: List<EventItemDto>? = null,
    public val setupPhotoUrls: List<String>? = null,
    public val contactPhone: String? = null,
)

@Serializable
public data class UpdateEventInputDto(
    public val title: String? = null,
    public val dateLabel: String? = null,
    public val eventDateIso: String? = null,
    public val daysUntilEvent: Long? = null,
    public val venue: String? = null,
    public val zone: String? = null,
    public val guests: Long? = null,
    public val status: String? = null,
    public val assignedLead: String? = null,
    public val sketchJson: String? = null,
    public val items: List<EventItemDto>? = null,
    public val setupPhotoUrls: List<String>? = null,
    public val contactPhone: String? = null,
)

@Serializable
public data class EventGuestDto(
    public val id: String,
    public val eventId: String,
    public val fullName: String,
    public val rsvpStatus: String,
    public val tableLabel: String,
    public val createdAt: String = "",
    public val dietaryNotes: String? = null,
)

@Serializable
public data class GuestsListOutputDto(
    public val guests: List<EventGuestDto>? = null,
)

@Serializable
public data class GuestOutputDto(
    public val guest: EventGuestDto,
)

@Serializable
public data class AddGuestInputDto(
    public val fullName: String,
    public val rsvpStatus: String? = null,
    public val tableLabel: String? = null,
    public val dietaryNotes: String? = null,
)

@Serializable
public data class UpdateGuestInputDto(
    public val fullName: String? = null,
    public val rsvpStatus: String? = null,
    public val tableLabel: String? = null,
    public val dietaryNotes: String? = null,
)

@Serializable
public data class ScheduleBlockDto(
    public val id: String,
    public val eventId: String,
    public val timeRange: String,
    public val title: String,
    public val ownerName: String,
    public val status: String,
    public val sortOrder: Long = 0,
)

@Serializable
public data class ScheduleListOutputDto(
    public val blocks: List<ScheduleBlockDto>? = null,
)

@Serializable
public data class ScheduleBlockOutputDto(
    public val block: ScheduleBlockDto,
)

@Serializable
public data class AddScheduleBlockInputDto(
    public val timeRange: String,
    public val title: String,
    public val ownerName: String? = null,
    public val status: String? = null,
    public val sortOrder: Long? = null,
)

@Serializable
public data class DeleteScheduleBlockOutputDto(
    public val deleted: Boolean,
)

@Serializable
public data class ChatThreadDto(
    public val threadId: String,
    public val participantName: String,
    public val eventId: String,
    public val eventTitle: String,
    public val lastMessage: String,
    public val updatedAt: String,
    public val unreadCount: Long,
)

@Serializable
public data class ChatThreadsOutputDto(
    public val threads: List<ChatThreadDto>? = null,
)

@Serializable
public data class ChatMessageDto(
    public val id: String,
    public val threadId: String,
    public val eventId: String,
    public val eventTitle: String,
    public val senderId: String,
    public val senderName: String,
    public val senderRole: String,
    public val body: String,
    public val createdAt: String,
    public val unread: Boolean,
    public val attachmentName: String? = null,
    public val attachmentUrl: String? = null,
    public val recipientLabel: String? = null,
)

@Serializable
public data class ChatMessagesOutputDto(
    public val messages: List<ChatMessageDto>? = null,
)

@Serializable
public data class ChatMessageOutputDto(
    public val message: ChatMessageDto,
)

@Serializable
public data class PostChatMessageInputDto(
    public val body: String,
    public val attachmentName: String? = null,
    public val attachmentUrl: String? = null,
    public val eventId: String? = null,
    public val eventTitle: String? = null,
    public val recipientLabel: String? = null,
)

@Serializable
public data class RosaLayoutPlanDto(
    public val chairs: Long = 0,
    public val tables: Long = 0,
    public val centerpieces: Long = 0,
    public val arches: Long = 0,
    public val sweetTables: Long = 0,
    public val lightingStrands: Long = 0,
    public val canvasWidthMeters: Double = 0.0,
    public val canvasDepthMeters: Double = 0.0,
)

@Serializable
public data class RosaChatInputDto(
    public val message: String,
    public val eventType: String? = null,
    public val guests: Long? = null,
    public val budgetRD: Long? = null,
    public val excludedCategories: List<String>? = null,
)

@Serializable
public data class RosaSuggestedItemDto(
    public val articleCode: String,
    public val name: String? = null,
    public val quantity: Long,
)

@Serializable
public data class RosaChatResponseDto(
    public val reply: String,
    public val intent: String,
    public val actionCode: String? = null,
    public val proposal: RosaMoodboardProposalDto? = null,
    public val suggestedItems: List<RosaSuggestedItemDto> = emptyList(),
    public val provider: String? = null,
    public val model: String? = null,
)

@Serializable
public data class RosaTranscribeInputDto(
    public val audioBase64: String,
    public val locale: String? = null,
    public val mimeType: String? = null,
    public val promptHint: String? = null,
)

@Serializable
public data class TranscriptionResultDto(
    public val transcript: String,
    public val locale: String,
    public val provider: String,
    public val voiceDetected: Boolean,
    public val durationMs: Long,
    public val rmsEnergy: Double,
    public val sampleRateHz: Long,
)

@Serializable
public data class RefundDto(
    public val id: String,
    public val eventId: String,
    public val eventTitle: String,
    public val clientName: String,
    public val paidRD: Long,
    public val refundPercent: Long,
    public val refundRD: Long,
    public val reason: String,
    public val status: String,
    public val approvedBy: String? = null,
    public val approvedAtLabel: String? = null,
    public val gatewayRefundId: String? = null,
    public val gatewayStatus: String? = null,
)

@Serializable
public data class RefundsListOutputDto(
    public val refunds: List<RefundDto>? = null,
)

@Serializable
public data class RefundOutputDto(
    public val refund: RefundDto,
)

@Serializable
public data class DamagesListOutputDto(
    public val damages: List<DamageReportDto>? = null,
)

@Serializable
public data class ReviewDto(
    public val id: String,
    public val eventId: String,
    public val clientName: String,
    public val rating: Long,
    public val comment: String,
    public val verified: Boolean = false,
    public val adminReply: String? = null,
    public val createdAtLabel: String? = null,
)

@Serializable
public data class ReviewsListOutputDto(
    public val reviews: List<ReviewDto>? = null,
)

@Serializable
public data class ReviewOutputDto(
    public val review: ReviewDto,
)

@Serializable
public data class CreateReviewInputDto(
    public val eventId: String,
    public val rating: Long,
    public val comment: String,
    public val clientName: String? = null,
)

@Serializable
public data class ReplyReviewInputDto(
    public val adminReply: String,
    public val verified: Boolean? = null,
)

@Serializable
public data class SupplierDto(
    public val id: String,
    public val name: String,
    public val category: String,
    public val specialty: String,
    public val contactName: String,
    public val phone: String,
    public val email: String,
    public val rating: String,
    public val website: String? = null,
    public val notes: String? = null,
)

@Serializable
public data class SuppliersListOutputDto(
    public val suppliers: List<SupplierDto>? = null,
)

@Serializable
public data class SupplierOutputDto(
    public val supplier: SupplierDto,
)

@Serializable
public data class UpsertSupplierInputDto(
    public val name: String,
    public val category: String,
    public val specialty: String,
    public val phone: String,
    public val id: String? = null,
    public val contactName: String? = null,
    public val email: String? = null,
    public val rating: String? = null,
    public val website: String? = null,
    public val notes: String? = null,
)

@Serializable
public data class LeadDto(
    public val id: String,
    public val fullName: String,
    public val phone: String,
    public val eventType: String,
    public val guests: Long,
    public val estimatedDop: Long,
    public val stage: String,
    public val email: String? = null,
    public val eventDateLabel: String? = null,
    public val notes: String? = null,
)

@Serializable
public data class LeadsListOutputDto(
    public val leads: List<LeadDto>? = null,
)

@Serializable
public data class LeadOutputDto(
    public val lead: LeadDto,
)

@Serializable
public data class CreateLeadInputDto(
    public val fullName: String,
    public val phone: String,
    public val eventType: String,
    public val email: String? = null,
    public val eventDateLabel: String? = null,
    public val guests: Long? = null,
    public val estimatedDop: Long? = null,
    public val stage: String? = null,
    public val notes: String? = null,
)

@Serializable
public data class UpdateLeadInputDto(
    public val stage: String? = null,
    public val estimatedDop: Long? = null,
    public val notes: String? = null,
)

@Serializable
public data class DeliveryZoneDto(
    public val id: String,
    public val name: String,
    public val feeDop: Long,
    public val etaMinutes: Long,
    public val active: Boolean,
)

@Serializable
public data class BusinessSettingsDto(
    public val id: String,
    public val companyName: String,
    public val rnc: String,
    public val address: String,
    public val phone: String,
    public val whatsapp: String,
    public val email: String,
    public val businessHours: String,
    public val instagram: String,
    public val facebook: String,
    public val depositPercent: Long,
    public val itbisPercent: Long,
    public val whatsappAutoAlertsEnabled: Boolean = false,
)

@Serializable
public data class PublicBusinessSettingsDto(
    public val companyName: String,
    public val rnc: String,
    public val address: String,
    public val phone: String,
    public val whatsapp: String,
    public val email: String,
    public val businessHours: String,
    public val instagram: String,
    public val facebook: String,
    public val depositPercent: Long,
    public val itbisPercent: Long = 18L,
    public val deliveryZones: List<DeliveryZoneDto>? = null,
)

@Serializable
public data class PublicSettingsOutputDto(
    public val settings: PublicBusinessSettingsDto,
)

@Serializable
public data class SettingsOutputDto(
    public val settings: BusinessSettingsDto,
    public val deliveryZones: List<DeliveryZoneDto>? = null,
)

@Serializable
public data class UpdateSettingsInputDto(
    public val companyName: String? = null,
    public val rnc: String? = null,
    public val address: String? = null,
    public val phone: String? = null,
    public val whatsapp: String? = null,
    public val email: String? = null,
    public val businessHours: String? = null,
    public val instagram: String? = null,
    public val facebook: String? = null,
    public val depositPercent: Long? = null,
    public val itbisPercent: Long? = null,
    public val deliveryZones: List<DeliveryZoneDto>? = null,
    public val whatsappAutoAlertsEnabled: Boolean? = null,
)

@Serializable
public data class NotificationDto(
    public val id: String,
    public val title: String,
    public val body: String,
    public val category: String,
    public val createdAtLabel: String,
    public val isRead: Boolean,
    public val recipientRole: String,
    public val actionPath: String? = null,
    public val userId: String? = null,
)

@Serializable
public data class NotificationsOutputDto(
    public val notifications: List<NotificationDto>? = null,
)

@Serializable
public data class NotificationOutputDto(
    public val notification: NotificationDto,
)

@Serializable
public data class InsurancePolicyDto(
    public val id: String,
    public val policyNumber: String,
    public val providerName: String,
    public val coverageType: String,
    public val maxCoverageDop: Long,
    public val validUntilLabel: String,
    public val status: String,
)

@Serializable
public data class InsuranceOutputDto(
    public val policies: List<InsurancePolicyDto>? = null,
)

@Serializable
public data class AuditLogDto(
    public val id: String,
    public val actorName: String,
    public val actorRole: String,
    public val action: String,
    public val entityType: String,
    public val entityId: String,
    public val summary: String,
    public val createdAtLabel: String,
)

@Serializable
public data class AuditLogsOutputDto(
    public val logs: List<AuditLogDto>? = null,
)

@Serializable
public data class BotFaqDto(
    public val id: String,
    public val category: String,
    public val question: String,
    public val answer: String,
    public val active: Boolean,
)

@Serializable
public data class BotFaqsOutputDto(
    public val faqs: List<BotFaqDto>? = null,
)

@Serializable
public data class BotFaqOutputDto(
    public val faq: BotFaqDto,
)

@Serializable
public data class UpsertBotFaqInputDto(
    public val question: String,
    public val answer: String,
    public val active: Boolean,
    public val category: String? = null,
    public val id: String? = null,
)

@Serializable
public data class BotKeywordDto(
    public val id: String,
    public val keyword: String,
    public val autoReply: String,
    public val handoffToHuman: Boolean,
)

@Serializable
public data class BotKeywordsOutputDto(
    public val keywords: List<BotKeywordDto>? = null,
)

@Serializable
public data class BotKeywordOutputDto(
    public val keyword: BotKeywordDto,
)

@Serializable
public data class UpsertBotKeywordInputDto(
    public val keyword: String,
    public val autoReply: String,
    public val handoffToHuman: Boolean,
    public val id: String? = null,
)

@Serializable
public data class BotThreadDto(
    public val id: String,
    public val customerName: String,
    public val phone: String,
    public val lastMessage: String,
    public val botPaused: Boolean,
    public val updatedAtLabel: String,
    public val assignedAgent: String? = null,
)

@Serializable
public data class BotThreadsOutputDto(
    public val threads: List<BotThreadDto>? = null,
)

@Serializable
public data class BotThreadOutputDto(
    public val thread: BotThreadDto,
)

@Serializable
public data class UpdateBotThreadInputDto(
    public val botPaused: Boolean? = null,
    public val assignedAgent: String? = null,
    public val lastMessage: String? = null,
)

@Serializable
public data class ReportPointDto(
    public val label: String,
    public val valueRD: Long,
    public val count: Long,
)

@Serializable
public data class ReportExportDto(
    public val reportType: String,
    public val period: String,
    public val filename: String,
    public val currency: String,
    public val totalRD: Long,
    public val itbisRD: Long,
    public val generatedAt: String,
    public val csv: String,
    public val series: List<ReportPointDto>? = null,
)

@Serializable
public data class ReportExportOutputDto(
    public val report: ReportExportDto,
)

@Serializable
public data class MediaUploadInputDto(
    public val filename: String,
    @SerialName("mime_type") public val mimeType: String,
    public val purpose: String,
    @SerialName("data_base64") public val dataBase64: String,
)

@Serializable
public data class MediaAssetDto(
    public val id: String,
    @SerialName("owner_id") public val ownerId: String = "",
    @SerialName("owner_role") public val ownerRole: String = "",
    public val purpose: String = "",
    public val filename: String = "",
    @SerialName("mime_type") public val mimeType: String = "",
    @SerialName("byte_size") public val byteSize: Long = 0L,
    public val sha256: String = "",
    public val url: String,
    @SerialName("created_at") public val createdAt: String = "",
)

@Serializable
public data class AvailabilityConflictDto(
    public val articleId: String,
    public val articleName: String,
    public val requested: Int,
    public val available: Int,
    public val dateRange: String,
)

@Serializable
public data class AvailabilityCheckOutputDto(
    public val eventDateIso: String,
    public val pickupDateIso: String,
    public val returnDateIso: String,
    public val available: Boolean,
    public val conflicts: List<AvailabilityConflictDto> = emptyList(),
)

@Serializable
public data class CloseoutEventInputDto(
    public val notes: String = "",
)

@Serializable
public data class VerifyPaymentInputDto(
    public val notes: String = "",
    public val reason: String = "",
)

@Serializable
public data class RejectPaymentInputDto(
    public val reason: String,
)

@Serializable
public data class CreateCardSessionInputDto(
    public val eventId: String,
    public val eventTitle: String = "",
    public val amountRD: Long,
    public val kind: String = "deposit",
    public val ncfType: String = "B02",
    public val clientRnc: String = "",
    public val contactPhone: String? = null,
)

@Serializable
public data class CardSessionResultDto(
    public val sessionId: String,
    public val provider: String,
    public val checkoutUrl: String,
)

@Serializable
public data class CardSessionOutputDto(
    public val session: CardSessionResultDto,
)

@Serializable
public data class DGII607OutputDto(
    public val period: String,
    public val csv: String,
)

@Serializable
public data class ResolveDamageInputDto(
    public val notes: String = "",
    public val resolutionNotes: String = "",
)

@Serializable
public data class SendWhatsAppReplyInputDto(
    public val threadId: String = "",
    public val recipientPhone: String = "",
    public val message: String,
)

@Serializable
public data class OfflineMutationSyncItemDto(
    public val actionLabel: String,
    public val entityCode: String,
    public val operationType: String,
    public val payloadJson: String,
    public val idempotencyKey: String,
)

@Serializable
public data class OfflineSyncInputDto(
    public val mutations: List<OfflineMutationSyncItemDto>,
)

@Serializable
public data class RealtimeEventDto(
    public val seq: Long = 0L,
    public val id: String = "",
    public val type: String,
    @SerialName("entity_id") public val entityId: String = "",
    @SerialName("client_id") public val clientId: String = "",
    public val audience: String = "",
    public val summary: String = "",
    public val payload: JsonObject? = null,
    @SerialName("created_at") public val createdAt: String = "",
)

@Serializable
public data class UserPreferencesDto(
    public val pushNotificationsEnabled: Boolean = true,
    public val whatsappAlertsEnabled: Boolean = true,
    public val emailReceiptsEnabled: Boolean = true,
    public val biometricLockEnabled: Boolean = false,
    public val autoSyncWifiOnly: Boolean = false,
    public val eventRemindersEnabled: Boolean = true,
    public val preferredLanguage: String = "es",
    public val darkThemeEnabled: Boolean = false,
    public val savedPaymentMethod: String = "Transferencia",
    public val profileZone: String = "Distrito Nacional",
)

@Serializable
public data class UpdatePreferencesInputDto(
    public val pushNotificationsEnabled: Boolean? = null,
    public val whatsappAlertsEnabled: Boolean? = null,
    public val emailReceiptsEnabled: Boolean? = null,
    public val biometricLockEnabled: Boolean? = null,
    public val autoSyncWifiOnly: Boolean? = null,
    public val eventRemindersEnabled: Boolean? = null,
    public val preferredLanguage: String? = null,
    public val darkThemeEnabled: Boolean? = null,
    public val savedPaymentMethod: String? = null,
    public val profileZone: String? = null,
)

@Serializable
public data class RecoverUsernameInputDto(
    public val identifier: String,
)

@Serializable
public data class RecoverUsernameOutputDto(
    public val sent: Boolean = false,
    public val message: String = "",
)

@Serializable
public data class OAuthConfigOutputDto(
    public val googleEnabled: Boolean = false,
    public val appleEnabled: Boolean = false,
    public val googleClientId: String = "",
    public val appleClientId: String = "",
    public val webPushVapidPublicKey: String = "",
)

@Serializable
public data class PaymentReminderInputDto(
    public val channel: String = "whatsapp",
    public val message: String = "",
)

@Serializable
public data class PaymentReminderOutputDto(
    public val event: EventDto,
    public val sent: Boolean = false,
    public val channel: String = "whatsapp",
)

@Serializable
public data class MonthlyAnalyticsPointDto(
    public val monthKey: String = "",
    public val label: String = "",
    public val revenueRD: Long = 0L,
    public val expensesRD: Long = 0L,
    public val eventsCount: Long = 0L,
)

@Serializable
public data class CategoryCountPointDto(
    public val key: String = "",
    public val label: String = "",
    public val count: Long = 0L,
)

@Serializable
public data class PaymentsAnalyticsDto(
    public val range: String = "30d",
    public val monthlySeries: List<MonthlyAnalyticsPointDto> = emptyList(),
    public val totalRevenueRD: Long = 0L,
    public val totalExpensesRD: Long = 0L,
    public val netMarginPercent: Long = 0L,
    public val totalEventsCount: Long = 0L,
    public val confirmedEventsCount: Long = 0L,
    public val pendingEventsCount: Long = 0L,
    public val completedEventsCount: Long = 0L,
    public val cancelledEventsCount: Long = 0L,
    public val eventTypesBreakdown: List<CategoryCountPointDto> = emptyList(),
    public val uniqueClientsCount: Long = 0L,
    public val repeatClientsCount: Long = 0L,
    public val collectionRatePercent: Long = 0L,
)

@Serializable
public data class PaymentsAnalyticsOutputDto(
    public val analytics: PaymentsAnalyticsDto,
)

@Serializable
public data class RosaSketchInputDto(
    public val prompt: String,
    public val eventType: String = "",
    public val guests: Int = 80,
)

@Serializable
public data class RosaSketchElementDto(
    public val id: String = "",
    public val kind: String = "",
    public val label: String = "",
    public val xRatio: Double = 0.5,
    public val yRatio: Double = 0.5,
    public val capacity: Int = 0,
)

@Serializable
public data class RosaSketchResponseDto(
    public val reply: String = "",
    public val elements: List<RosaSketchElementDto> = emptyList(),
    public val eventWidthMeters: Double = 14.0,
    public val eventDepthMeters: Double = 10.0,
    public val provider: String = "",
    public val model: String = "",
)

@Serializable
public data class FavoritesOutputDto(
    public val favorites: List<String>? = null,
)

@Serializable
public data class EventTaskDto(
    public val id: String,
    public val eventId: String,
    public val title: String,
    public val assigneeName: String = "",
    public val done: Boolean = false,
    public val createdAt: String = "",
)

@Serializable
public data class EventTasksListOutputDto(
    public val tasks: List<EventTaskDto>? = null,
)

@Serializable
public data class EventTaskOutputDto(
    public val task: EventTaskDto,
)

@Serializable
public data class CreateEventTaskInputDto(
    public val title: String,
    public val assigneeName: String = "",
    public val done: Boolean = false,
)

@Serializable
public data class UpdateEventTaskInputDto(
    public val title: String? = null,
    public val assigneeName: String? = null,
    public val done: Boolean? = null,
)

@Serializable
public data class DeleteEntityOutputDto(
    public val deleted: Boolean = false,
    public val id: String = "",
)

@Serializable
public data class DeletePackageOutputDto(
    public val deletedId: String = "",
)

@Serializable
public data class ApproveRefundInputDto(
    public val approvedBy: String = "",
    public val method: String = "",
    public val releaseInventory: Boolean? = null,
    public val notifyWhatsapp: Boolean? = null,
    public val clientMessage: String = "",
)

@Serializable
public data class CancelEventInputDto(
    public val reason: String = "",
)

@Serializable
public data class PatchSupplierInputDto(
    public val name: String = "",
    public val category: String = "",
    public val specialty: String = "",
    public val contactName: String = "",
    public val phone: String = "",
    public val email: String = "",
    public val website: String = "",
    public val rating: String = "",
    public val notes: String = "",
)
