package com.jeudry.rosafiesta.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    public val availableStock: Long? = null,
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
    public val dailyPriceDop: Long? = null,
    public val totalStock: Long? = null,
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
)

@Serializable
public data class UpdateEventInputDto(
    public val title: String? = null,
    public val dateLabel: String? = null,
    public val daysUntilEvent: Long? = null,
    public val venue: String? = null,
    public val zone: String? = null,
    public val guests: Long? = null,
    public val status: String? = null,
    public val assignedLead: String? = null,
    public val sketchJson: String? = null,
    public val items: List<EventItemDto>? = null,
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
public data class RosaChatResponseDto(
    public val reply: String,
    public val intent: String,
    public val actionCode: String? = null,
    public val proposal: RosaMoodboardProposalDto? = null,
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
