package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.core.engine.UserRole
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.ArticleAvailabilityDay
import com.jeudry.rosafiesta.domain.AuditLogRecord
import com.jeudry.rosafiesta.domain.AvailabilityConflictItem
import com.jeudry.rosafiesta.domain.AvailabilityRangeCheck
import com.jeudry.rosafiesta.domain.BalloonProduct
import com.jeudry.rosafiesta.domain.BotFaqRecord
import com.jeudry.rosafiesta.domain.BotKeywordRecord
import com.jeudry.rosafiesta.domain.BotThreadRecord
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CardSessionResult
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.CatalogCategory
import com.jeudry.rosafiesta.domain.CatalogTrend
import com.jeudry.rosafiesta.domain.ChatMessageRecord
import com.jeudry.rosafiesta.domain.ChatThread
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.DamageIncident
import com.jeudry.rosafiesta.domain.DeliveryStop
import com.jeudry.rosafiesta.domain.DeliveryZone
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.EventGuest
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.EventPackage
import com.jeudry.rosafiesta.domain.EventScheduleBlock
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.EventTypeCount
import com.jeudry.rosafiesta.domain.EventTypeTemplate
import com.jeudry.rosafiesta.domain.InsurancePolicyRecord
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.MediaUploadAsset
import com.jeudry.rosafiesta.domain.MonthlyAnalyticsBar
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.OAuthProviderConfig
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PaymentsAnalyticsSummary
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.RealtimeStreamEvent
import com.jeudry.rosafiesta.domain.RefundRequest
import com.jeudry.rosafiesta.domain.ReportExportResult
import com.jeudry.rosafiesta.domain.ReportSeriesPoint
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.RosaChatReply
import com.jeudry.rosafiesta.domain.RosaSketchElement
import com.jeudry.rosafiesta.domain.RosaSketchProposal
import com.jeudry.rosafiesta.domain.SeasonBanner
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.StaffProfile
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome
import com.jeudry.rosafiesta.domain.security.UserPreferences

internal data class NormalizedPaymentMethod(
    val canonicalMethod: String,
    val bankName: String?,
)

internal object RosaFiestaBackendMappers {
    fun toCatalogCategory(dto: CategoryDto): CatalogCategory = CatalogCategory(
        id = dto.id.trim(),
        name = dto.name.trim().ifBlank { dto.id.trim() },
        illoKey = dto.illoKey.trim().ifBlank { "cat-default" },
        itemCount = dto.itemCount.toInt().coerceAtLeast(0),
    )

    fun toCatalogTrend(dto: CatalogTrendDto): CatalogTrend {
        val normalizedPhotoKey = dto.photoKey.replace('-', '_').trim()
        return CatalogTrend(
            id = dto.id.trim(),
            title = dto.title.trim(),
            eventsLabel = dto.eventsLabel.trim(),
            photo = PhotoAsset.fromKey(normalizedPhotoKey),
            photoUrl = dto.photoUrl.orEmpty().trim(),
            photoBgHex = dto.photoBgHex.trim().ifBlank { "#F3EEF6" },
            linkedCategory = dto.linkedCategory.orEmpty().trim(),
            linkedPackageId = dto.linkedPackageId.orEmpty().trim(),
            sortOrder = dto.sortOrder,
            active = dto.active,
        )
    }

    fun toSeasonBanner(dto: SeasonBannerDto): SeasonBanner = SeasonBanner(
        id = dto.id.trim().ifBlank { "default" },
        badgeLabel = dto.badgeLabel.trim(),
        title = dto.title.trim(),
        ctaLabel = dto.ctaLabel.trim(),
        targetScreenCode = dto.targetScreenCode.trim().ifBlank { "s21" },
        active = dto.active,
    )

    fun toCatalogArticle(
        dto: ArticleDto,
        index: Int,
        fallbackById: Map<String, CatalogArticle>,
        categories: List<CatalogCategory> = emptyList(),
    ): CatalogArticle {
        val fallback = fallbackById[dto.id]
        val normalizedPhotoKey = dto.photoKey.replace('-', '_')
        val photo = fallback?.photo ?: PhotoAsset.fromKey(normalizedPhotoKey)
        val resolvedCode = fallback?.code ?: "RF-${(index + 1).toString().padStart(3, '0')}"
        val resolvedCategory = resolveCategoryDisplayName(dto.categoryId, categories, fallback?.category)
        val variants = dto.colorVariants?.takeIf { list -> list.isNotEmpty() }
            ?: fallback?.variants
            ?: emptyList()
        val dimensions = dto.dimensions?.ifBlank { null }
            ?: fallback?.dimensions
            ?: ""
        val material = dto.material?.ifBlank { null }
            ?: fallback?.material
            ?: ""
        return CatalogArticle(
            id = dto.id,
            code = resolvedCode,
            name = dto.name,
            category = resolvedCategory,
            description = dto.description,
            pricePerDayDop = dto.dailyPriceDop.toInt(),
            totalStock = dto.totalStock.toInt(),
            availableOct23 = dto.availableStock.toInt(),
            photo = photo,
            variants = variants,
            dimensions = dimensions,
            material = material,
            visibleInCatalog = dto.visible,
            photoUrl = dto.photoUrl.orEmpty().ifBlank { fallback?.photoUrl.orEmpty() },
            damagedStock = dto.damagedStock.toInt(),
        )
    }

    fun toBalloonProduct(
        dto: ArticleDto,
        index: Int,
        fallbackById: Map<String, BalloonProduct>,
    ): BalloonProduct {
        val normalizedId = if (dto.id.startsWith("balloon-")) {
            dto.id
        } else {
            "balloon-${dto.id.removePrefix("art-")}"
        }
        val fallback = fallbackById[normalizedId] ?: fallbackById[dto.id]
        val code = fallback?.code ?: "BLN-${(index + 1).toString().padStart(3, '0')}"
        val colors = dto.colorVariants?.takeIf { list -> list.isNotEmpty() }
            ?: fallback?.colors
            ?: listOf("Rosa empolvado", "Dorado champagne")
        val dimensions = dto.dimensions?.ifBlank { null }
            ?: fallback?.dimensions
            ?: "Estándar"
        val material = dto.material?.ifBlank { null }
            ?: fallback?.material
            ?: "Látex biodegradable y foil metálico"
        val normalizedPhotoKey = dto.photoKey.replace('-', '_')
        val photo = fallback?.photo ?: PhotoAsset.fromKey(normalizedPhotoKey)
        return BalloonProduct(
            id = normalizedId,
            code = code,
            name = dto.name,
            description = dto.description,
            unitPriceDop = dto.dailyPriceDop.toInt(),
            totalStock = dto.totalStock.toInt(),
            availableStock = dto.availableStock.toInt(),
            colors = colors,
            dimensions = dimensions,
            material = material,
            photo = photo,
        )
    }

    fun toEventPackage(
        dto: PackageBundleDto,
        fallbackById: Map<String, EventPackage>,
    ): EventPackage {
        val fallback = fallbackById[dto.id]
        val normalizedPhotoKey = dto.photoKey.replace('-', '_')
        val photo = fallback?.photo ?: PhotoAsset.fromKey(normalizedPhotoKey)
        val itemLines = dto.items?.map { item ->
            "${item.quantity} × ${item.articleName} (RD$ ${item.unitPriceDop}/día)"
        }
        val resolvedItems = itemLines?.takeIf { list -> list.isNotEmpty() }
            ?: fallback?.includedItems
            ?: dto.itemsSummary.split(",").map { part -> part.trim() }.filter { part -> part.isNotEmpty() }
        val totalDop = dto.totalPriceDop.toInt()
        val savings = fallback?.savingsDop ?: (totalDop / 10).coerceAtLeast(50)
        return EventPackage(
            id = dto.id,
            name = dto.name,
            subtitle = dto.itemsSummary.ifBlank { fallback?.subtitle.orEmpty() },
            suggestedGuests = dto.guestCapacity.toInt(),
            priceDop = totalDop,
            savingsDop = savings,
            photo = photo,
            includedItems = resolvedItems,
            eventType = dto.eventType,
            visibleInCatalog = dto.visible,
            photoUrl = dto.photoUrl.orEmpty().ifBlank { fallback?.photoUrl.orEmpty() },
        )
    }

    fun toEventTemplate(dto: EventTemplateDto): EventTypeTemplate {
        val normalizedPhotoKey = dto.photoKey.replace('-', '_')
        return EventTypeTemplate(
            id = dto.id,
            name = dto.name,
            subtitle = dto.subtitle,
            defaultGuests = dto.defaultGuests.toInt(),
            startingPriceDop = dto.startingPriceRD.toInt(),
            photo = PhotoAsset.fromKey(normalizedPhotoKey),
            paletteHex = dto.paletteColors.orEmpty(),
        )
    }

    fun toClientEvent(
        dto: EventDto,
        fallbackById: Map<String, ClientEvent>,
    ): ClientEvent {
        val fallback = fallbackById[dto.id]
        val mappedStatus = mapEventStatus(dto.status)
        val mappedLines = dto.items.orEmpty().map { line ->
            EventLineItem(
                articleCode = line.articleCode,
                name = line.name,
                quantity = line.quantity.toInt(),
                unitPriceDop = line.unitPriceRD.toInt(),
                photo = selectPhotoForArticle("", line.name),
            )
        }
        val resolvedLines = mappedLines.ifEmpty { fallback?.items.orEmpty() }
        val photo = fallback?.photo ?: selectPhotoForEventType(dto.eventType)
        val hasDeposit = dto.paidDepositRD > 0L
        val depositMethod = fallback?.depositMethodLabel ?: if (hasDeposit) {
            "Anticipo registrado"
        } else {
            "Pendiente de anticipo"
        }
        val depositDate = fallback?.depositDateLabel ?: if (hasDeposit) {
            dto.dateLabel
        } else {
            "—"
        }
        return ClientEvent(
            id = dto.id,
            code = dto.code,
            title = dto.title,
            clientName = dto.clientName,
            eventType = dto.eventType,
            dateLabel = dto.dateLabel,
            daysUntilEvent = dto.daysUntilEvent.toInt(),
            venue = dto.venue,
            zone = dto.zone,
            guests = dto.guests.toInt(),
            articleCount = dto.articleCount.toInt(),
            totalDop = dto.totalRD.toInt(),
            paidDepositDop = dto.paidDepositRD.toInt(),
            depositMethodLabel = depositMethod,
            depositDateLabel = depositDate,
            balanceDueDateLabel = dto.balanceDueDate,
            status = mappedStatus,
            leadStaffName = dto.assignedLead,
            photo = photo,
            items = resolvedLines,
            priorChangesCount = dto.dateChangesUsed.toInt(),
            clientEmail = dto.clientEmail.orEmpty(),
            cancellationReason = dto.cancellationReason.orEmpty(),
            sketchJson = dto.sketchJson.orEmpty(),
            eventDateIso = dto.eventDateIso.orEmpty().ifBlank { fallback?.eventDateIso.orEmpty() },
            pickupDateIso = dto.pickupDateIso.orEmpty().ifBlank { fallback?.pickupDateIso.orEmpty() },
            returnDateIso = dto.returnDateIso.orEmpty().ifBlank { fallback?.returnDateIso.orEmpty() },
            quoteId = dto.quoteId.orEmpty().ifBlank { fallback?.quoteId.orEmpty() },
            closeoutCompleted = dto.closeoutCompleted,
            closeoutNotes = dto.closeoutNotes.orEmpty(),
            lastPaymentReminderAt = dto.lastPaymentReminderAt.orEmpty().ifBlank { fallback?.lastPaymentReminderAt.orEmpty() },
            lastPaymentReminderChannel = dto.lastPaymentReminderChannel.orEmpty().ifBlank { fallback?.lastPaymentReminderChannel.orEmpty() },
        )
    }

    fun toPaymentRecord(dto: PaymentDto): PaymentRecord {
        val normalizedStatus = dto.status.trim().uppercase()
        val isVerified = normalizedStatus == "VERIFIED"
        val isPaid = normalizedStatus == "PAID"
        val isCompleted = normalizedStatus == "COMPLETED"
        val verifiedFlag = when {
            isVerified -> true
            isPaid -> true
            isCompleted -> true
            else -> false
        }
        return PaymentRecord(
            id = dto.id,
            receiptNumber = dto.receiptNumber,
            eventId = dto.eventId,
            eventTitle = dto.eventTitle,
            clientName = dto.clientName,
            amountDop = dto.amountRD.toInt(),
            methodLabel = dto.method,
            kindLabel = dto.kind,
            dateLabel = dto.paidDateLabel,
            referenceCode = dto.referenceCode,
            verified = verifiedFlag,
            voucherUrl = dto.voucherUrl.orEmpty(),
            referenceNumber = dto.referenceNumber.orEmpty(),
            verifiedBy = dto.verifiedBy.orEmpty(),
            verifiedAtIso = dto.verifiedAtIso.orEmpty(),
            rejectionReason = dto.rejectionReason.orEmpty(),
            ncfType = dto.ncfType.orEmpty().ifBlank { "B02" },
            ncfNumber = dto.ncfNumber.orEmpty(),
            clientRnc = dto.clientRnc.orEmpty(),
            subtotalDop = dto.subtotalDop.toInt(),
            itbisDop = dto.itbisDop.toInt(),
            securityDepositDop = dto.securityDepositDop.toInt(),
            gatewaySessionId = dto.gatewaySessionId.orEmpty(),
            gatewayPaymentIntentId = dto.gatewayPaymentIntentId.orEmpty(),
        )
    }

    fun toQuoteRecord(dto: QuoteDto): QuoteRecord {
        val mappedLines = dto.lines.orEmpty().map { line ->
            EventLineItem(
                articleCode = line.articleCode,
                name = line.name,
                quantity = line.quantity.toInt(),
                unitPriceDop = line.unitPriceRD.toInt(),
                photo = selectPhotoForArticle("", line.name),
            )
        }
        return QuoteRecord(
            id = dto.id,
            code = dto.code,
            eventId = dto.eventId.orEmpty(),
            eventTitle = dto.eventTitle,
            clientName = dto.clientName,
            clientEmail = dto.clientEmail.orEmpty(),
            venue = dto.venue,
            dateLabel = dto.eventDateLabel,
            guests = dto.guests.toInt(),
            status = dto.status,
            lines = mappedLines,
            deliveryFeeDop = dto.deliveryFeeRD.toInt(),
            discountDop = dto.discountRD.toInt(),
            totalDop = dto.totalRD.toInt(),
            signatureSvg = dto.signatureSvg.orEmpty(),
            depositPercent = dto.depositPercent.toInt(),
            depositDop = dto.depositRD.toInt(),
            signedByName = dto.signedByName.orEmpty(),
            rejectionReason = dto.rejectionReason.orEmpty(),
            notes = dto.notes.orEmpty(),
            eventDateIso = dto.eventDateIso.orEmpty(),
            packageId = dto.packageId.orEmpty(),
            contractPdfUrl = dto.contractPdfUrl.orEmpty(),
            signedAtIso = dto.signedAtIso.orEmpty(),
        )
    }

    fun toRefundRequest(
        dto: RefundDto,
        fallbackById: Map<String, RefundRequest>,
    ): RefundRequest {
        val fallback = fallbackById[dto.id]
        val inferredDays = when {
            dto.refundPercent >= 90L -> 18
            dto.refundPercent >= 50L -> 10
            else -> 3
        }
        return RefundRequest(
            id = dto.id,
            eventId = dto.eventId,
            eventTitle = dto.eventTitle,
            clientName = dto.clientName,
            paidDepositDop = dto.paidRD.toInt(),
            refundPercent = dto.refundPercent.toInt(),
            refundAmountDop = dto.refundRD.toInt(),
            daysBefore = fallback?.daysBefore ?: inferredDays,
            reason = dto.reason,
            status = dto.status,
            requestedAtLabel = fallback?.requestedAtLabel ?: DomainClockFormatter.currentFullDateLabel(),
            approvedBy = dto.approvedBy.orEmpty(),
            approvedAtLabel = dto.approvedAtLabel.orEmpty(),
            gatewayRefundId = dto.gatewayRefundId.orEmpty(),
            gatewayStatus = dto.gatewayStatus.orEmpty(),
        )
    }

    fun toReviewRecord(
        dto: ReviewDto,
        eventsById: Map<String, ClientEvent>,
        fallbackById: Map<String, ReviewRecord>,
    ): ReviewRecord {
        val fallback = fallbackById[dto.id]
        val linkedEvent = eventsById[dto.eventId]
        val eventTitle = linkedEvent?.title ?: fallback?.eventTitle.orEmpty()
        val verified = fallback?.verified ?: (linkedEvent != null)
        return ReviewRecord(
            id = dto.id,
            eventId = dto.eventId,
            eventTitle = eventTitle,
            authorName = dto.clientName,
            rating = dto.rating.toInt().coerceIn(1, 5),
            comment = dto.comment,
            verified = verified,
            dateLabel = dto.createdAtLabel ?: fallback?.dateLabel ?: DomainClockFormatter.currentFullDateLabel(),
            adminReply = dto.adminReply ?: fallback?.adminReply.orEmpty(),
        )
    }

    fun toClientProfile(
        dto: ClientSummaryDto,
        fallbackById: Map<String, ClientProfile>,
    ): ClientProfile {
        val fallback = fallbackById[dto.id]
        val vipTag = fallback?.vipTag ?: if (dto.eventsCount >= 2L) "Cliente frecuente" else "Confirmada"
        return ClientProfile(
            id = dto.id,
            fullName = dto.fullName,
            email = dto.email,
            phone = dto.phone,
            zone = fallback?.zone ?: "Santo Domingo DN",
            eventsCount = dto.eventsCount.toInt(),
            totalContractedDop = dto.totalSpentDop.toInt(),
            pendingBalanceDop = dto.balanceDop.toInt(),
            vipTag = vipTag,
            notes = dto.notes ?: fallback?.notes.orEmpty(),
        )
    }

    fun toStaffProfile(
        dto: StaffMemberDto,
        events: List<ClientEvent>,
    ): StaffProfile {
        val mappedRole = mapUserRole(dto.role, dto.fullName)
        val parsedPermissions = dto.permissions.orEmpty().mapNotNull { rawToken ->
            val clean = rawToken.trim().uppercase()
            StaffPermission.entries.firstOrNull { permission -> permission.name == clean }
        }.toSet()
        val resolvedPermissions = parsedPermissions.ifEmpty {
            RosaFiestaEngine.permissionsForRole(mappedRole)
        }
        val activeEvents = events.count { event ->
            val matchesLead = event.leadStaffName.equals(dto.fullName, ignoreCase = true)
            val isActive = isActiveEvent(event)
            if (matchesLead) isActive else false
        }
        val canViewPayments = StaffPermission.PAYMENTS in resolvedPermissions
        return StaffProfile(
            id = dto.id,
            name = dto.fullName,
            roleLabel = dto.roleLabel,
            role = mappedRole,
            activeEventsCount = activeEvents,
            permissions = resolvedPermissions,
            canViewPayments = canViewPayments,
            email = dto.email.orEmpty(),
            phone = dto.phone.orEmpty(),
            active = dto.active,
        )
    }

    fun toDeliveryStop(dto: RouteStopDto): DeliveryStop = DeliveryStop(
        order = dto.order.toInt(),
        eventTitle = dto.eventTitle,
        venue = dto.venue,
        windowLabel = dto.windowLabel,
        vehicleAndCrew = dto.assignedVehicle,
        statusLabel = dto.status,
        eventId = dto.eventId,
    )

    fun toDamageIncident(
        dto: DamageReportDto,
        eventsById: Map<String, ClientEvent>,
    ): DamageIncident = DamageIncident(
        id = dto.id,
        eventTitle = eventsById[dto.eventId]?.title ?: dto.eventId,
        articleName = dto.articleName,
        damagedUnits = dto.damagedUnits.toInt(),
        missingUnits = dto.missingUnits.toInt(),
        replacementDop = dto.replacementRD.toInt(),
        notes = dto.notes,
        eventId = dto.eventId,
        articleCode = dto.articleCode,
        photoUrl = dto.photoUrl.orEmpty(),
        resolutionNotes = dto.resolutionNotes.orEmpty(),
        chargePaymentId = dto.chargePaymentId.orEmpty(),
    )

    fun toChecklistStep(
        dto: ChecklistItemDto,
        eventId: String,
    ): ChecklistStep = ChecklistStep(
        id = dto.id,
        stageLabel = dto.stage,
        title = dto.title,
        completed = dto.completed,
        completedBy = dto.completedBy.orEmpty().ifBlank { "Equipo" },
        timeLabel = dto.completedAt.orEmpty().ifBlank { if (dto.completed) "Completado" else "Pendiente" },
        eventId = eventId,
    )

    fun toOfflineQueueItem(dto: OfflineMutationDto): OfflineQueueItem = OfflineQueueItem(
        id = dto.id,
        actionLabel = dto.actionLabel,
        entityCode = dto.entityCode,
        timeAgo = dto.createdAt,
        synced = dto.synced,
        operationType = dto.operationType,
        payloadJson = dto.payloadJson.ifBlank { "{}" },
        idempotencyKey = dto.idempotencyKey,
    )

    fun toDeliveryZone(dto: DeliveryZoneDto): DeliveryZone = DeliveryZone(
        id = dto.id,
        name = dto.name,
        feeDop = dto.feeDop.toInt(),
        etaMinutes = dto.etaMinutes.toInt(),
        active = dto.active,
    )

    fun toDeliveryZoneDto(zone: DeliveryZone): DeliveryZoneDto = DeliveryZoneDto(
        id = zone.id,
        name = zone.name,
        feeDop = zone.feeDop.toLong(),
        etaMinutes = zone.etaMinutes.toLong(),
        active = zone.active,
    )

    fun fromPublicSettings(
        dto: PublicBusinessSettingsDto,
        fallback: BusinessSettings,
    ): BusinessSettings {
        val mappedZones = dto.deliveryZones?.map { zone -> toDeliveryZone(zone) }
            ?.takeIf { list -> list.isNotEmpty() }
            ?: fallback.zones
        return BusinessSettings(
            businessName = dto.companyName.ifBlank { fallback.businessName },
            rnc = dto.rnc.ifBlank { fallback.rnc },
            address = dto.address.ifBlank { fallback.address },
            phone = dto.phone.ifBlank { fallback.phone },
            whatsapp = dto.whatsapp.ifBlank { fallback.whatsapp },
            email = dto.email.ifBlank { fallback.email },
            businessHours = dto.businessHours.ifBlank { fallback.businessHours },
            instagramUrl = dto.instagram.ifBlank { fallback.instagramUrl },
            facebookUrl = dto.facebook.ifBlank { fallback.facebookUrl },
            depositPercent = dto.depositPercent.toInt(),
            itbisPercent = fallback.itbisPercent,
            zones = mappedZones,
        )
    }

    fun fromAdminSettings(
        output: SettingsOutputDto,
        fallback: BusinessSettings,
    ): BusinessSettings {
        val dto = output.settings
        val mappedZones = output.deliveryZones?.map { zone -> toDeliveryZone(zone) }
            ?.takeIf { list -> list.isNotEmpty() }
            ?: fallback.zones
        return BusinessSettings(
            businessName = dto.companyName.ifBlank { fallback.businessName },
            rnc = dto.rnc.ifBlank { fallback.rnc },
            address = dto.address.ifBlank { fallback.address },
            phone = dto.phone.ifBlank { fallback.phone },
            whatsapp = dto.whatsapp.ifBlank { fallback.whatsapp },
            email = dto.email.ifBlank { fallback.email },
            businessHours = dto.businessHours.ifBlank { fallback.businessHours },
            instagramUrl = dto.instagram.ifBlank { fallback.instagramUrl },
            facebookUrl = dto.facebook.ifBlank { fallback.facebookUrl },
            depositPercent = dto.depositPercent.toInt(),
            itbisPercent = dto.itbisPercent.toInt(),
            zones = mappedZones,
        )
    }

    fun toUpdateSettingsInputDto(settings: BusinessSettings): UpdateSettingsInputDto = UpdateSettingsInputDto(
        companyName = settings.businessName,
        rnc = settings.rnc,
        address = settings.address,
        phone = settings.phone,
        whatsapp = settings.whatsapp,
        email = settings.email,
        businessHours = settings.businessHours,
        instagram = settings.instagramUrl,
        facebook = settings.facebookUrl,
        depositPercent = settings.depositPercent.toLong(),
        itbisPercent = settings.itbisPercent.toLong(),
        deliveryZones = settings.zones.map { zone -> toDeliveryZoneDto(zone) },
    )

    fun toSupplierRecord(dto: SupplierDto): SupplierRecord = SupplierRecord(
        id = dto.id,
        name = dto.name,
        category = dto.category,
        contactName = dto.contactName,
        phone = dto.phone,
        email = dto.email,
        website = dto.website.orEmpty(),
        notes = dto.notes.orEmpty(),
        statusLabel = "Activo",
        specialty = dto.specialty,
        rating = dto.rating,
    )

    fun toLeadRecord(
        dto: LeadDto,
        fallbackChannel: String = "Web",
    ): LeadRecord = LeadRecord(
        id = dto.id,
        clientName = dto.fullName,
        eventType = dto.eventType,
        dateLabel = dto.eventDateLabel.orEmpty(),
        guests = dto.guests.toInt(),
        estimatedDop = dto.estimatedDop.toInt(),
        channel = fallbackChannel,
        stage = dto.stage,
        followUpNote = dto.notes.orEmpty(),
        phone = dto.phone,
        email = dto.email.orEmpty(),
    )

    fun toChatThread(dto: ChatThreadDto): ChatThread = ChatThread(
        id = dto.threadId,
        threadId = dto.threadId,
        contactName = dto.participantName,
        eventId = dto.eventId,
        eventTitle = dto.eventTitle,
        lastMessage = dto.lastMessage,
        timestampLabel = dto.updatedAt,
        unreadCount = dto.unreadCount.toInt(),
    )

    fun toChatMessageRecord(
        dto: ChatMessageDto,
        contactName: String,
    ): ChatMessageRecord = ChatMessageRecord(
        id = dto.id,
        threadId = dto.threadId,
        contactName = contactName,
        eventId = dto.eventId,
        eventTitle = dto.eventTitle,
        senderName = dto.senderName,
        senderRole = dto.senderRole,
        body = dto.body,
        attachmentName = dto.attachmentName.orEmpty(),
        timestampLabel = dto.createdAt,
        unreadCount = if (dto.unread) 1 else 0,
    )

    fun toEventGuest(dto: EventGuestDto): EventGuest = EventGuest(
        id = dto.id,
        eventId = dto.eventId,
        fullName = dto.fullName,
        rsvpStatus = dto.rsvpStatus,
        tableLabel = dto.tableLabel,
        notes = dto.dietaryNotes.orEmpty(),
    )

    fun toEventScheduleBlock(dto: ScheduleBlockDto): EventScheduleBlock {
        val statusNormalized = dto.status.trim().uppercase()
        val isCompleted = statusNormalized == "COMPLETED"
        val isDone = statusNormalized == "DONE"
        val completedFlag = if (isCompleted) true else isDone
        return EventScheduleBlock(
            id = dto.id,
            eventId = dto.eventId,
            timeRange = dto.timeRange,
            title = dto.title,
            ownerName = dto.ownerName,
            completed = completedFlag,
            sortOrder = dto.sortOrder.toInt(),
        )
    }

    fun toNotificationRecord(dto: NotificationDto): NotificationRecord = NotificationRecord(
        id = dto.id,
        title = dto.title,
        body = dto.body,
        category = dto.category,
        createdAtLabel = dto.createdAtLabel,
        isRead = dto.isRead,
        recipientRole = dto.recipientRole,
        actionPath = dto.actionPath.orEmpty(),
    )

    fun toInsurancePolicyRecord(dto: InsurancePolicyDto): InsurancePolicyRecord = InsurancePolicyRecord(
        id = dto.id,
        policyNumber = dto.policyNumber,
        providerName = dto.providerName,
        coverageType = dto.coverageType,
        maxCoverageDop = dto.maxCoverageDop.toInt(),
        validUntilLabel = dto.validUntilLabel,
        status = dto.status,
    )

    fun toAuditLogRecord(dto: AuditLogDto): AuditLogRecord = AuditLogRecord(
        id = dto.id,
        actorName = dto.actorName,
        actorRole = dto.actorRole,
        action = dto.action,
        entityType = dto.entityType,
        entityId = dto.entityId,
        summary = dto.summary,
        createdAtLabel = dto.createdAtLabel,
    )

    fun toBotFaqRecord(dto: BotFaqDto): BotFaqRecord = BotFaqRecord(
        id = dto.id,
        category = dto.category,
        question = dto.question,
        answer = dto.answer,
        active = dto.active,
    )

    fun toBotKeywordRecord(dto: BotKeywordDto): BotKeywordRecord = BotKeywordRecord(
        id = dto.id,
        keyword = dto.keyword,
        autoReply = dto.autoReply,
        handoffToHuman = dto.handoffToHuman,
    )

    fun toBotThreadRecord(dto: BotThreadDto): BotThreadRecord = BotThreadRecord(
        id = dto.id,
        customerName = dto.customerName,
        phone = dto.phone,
        lastMessage = dto.lastMessage,
        botPaused = dto.botPaused,
        assignedAgent = dto.assignedAgent.orEmpty(),
        updatedAtLabel = dto.updatedAtLabel,
    )

    fun toAvailabilityCalendar(dto: ArticleAvailabilityDto): ArticleAvailabilityCalendar = ArticleAvailabilityCalendar(
        articleId = dto.articleId,
        articleName = dto.articleName,
        month = dto.month,
        totalStock = dto.totalStock.toInt(),
        days = dto.days.orEmpty().map { day ->
            ArticleAvailabilityDay(
                dateIso = day.date,
                reservedUnits = day.reserved.toInt(),
                availableUnits = day.available.toInt(),
                lowStock = day.lowStock,
            )
        },
    )

    fun toRosaChatReply(dto: RosaChatResponseDto): RosaChatReply {
        val proposal = dto.proposal
        val mappedLines = proposal?.suggestedLines.orEmpty().map { line ->
            EventLineItem(
                articleCode = line.articleCode,
                name = line.name,
                quantity = line.quantity.toInt(),
                unitPriceDop = line.unitPriceRD.toInt(),
                photo = selectPhotoForArticle("", line.name),
            )
        }
        return RosaChatReply(
            reply = dto.reply,
            intent = dto.intent,
            actionCode = dto.actionCode.orEmpty(),
            styleTitle = proposal?.styleTitle.orEmpty(),
            estimatedTotalDop = proposal?.estimatedTotalRD?.toInt() ?: 0,
            withinBudget = proposal?.withinBudget ?: true,
            suggestedLines = mappedLines,
        )
    }

    fun toVoiceTranscriptionOutcome(dto: TranscriptionResultDto): VoiceTranscriptionOutcome = VoiceTranscriptionOutcome(
        transcript = dto.transcript,
        locale = dto.locale,
        voiceDetected = dto.voiceDetected,
        durationMs = dto.durationMs.toInt(),
        provider = dto.provider,
    )

    fun toReportExportResult(dto: ReportExportDto): ReportExportResult = ReportExportResult(
        reportType = dto.reportType,
        period = dto.period,
        filename = dto.filename,
        currency = dto.currency,
        totalDop = dto.totalRD.toInt(),
        itbisDop = dto.itbisRD.toInt(),
        generatedAt = dto.generatedAt,
        csvContent = dto.csv,
        series = dto.series.orEmpty().map { point ->
            ReportSeriesPoint(
                label = point.label,
                valueDop = point.valueRD.toInt(),
                count = point.count.toInt(),
            )
        },
    )

    fun toBusinessMetrics(
        summary: FinanceSummaryDto,
        occupancyRatePercent: Int,
    ): BusinessMetrics = BusinessMetrics(
        monthLabel = summary.monthLabel,
        octoberRevenueDop = summary.octRevenueRD.toInt(),
        accountsReceivableDop = summary.accountsReceivableRD.toInt(),
        activeEventsCount = summary.activeEventsCount.toInt(),
        newRequestsCount = summary.newQuoteRequests.toInt(),
        occupancyRatePercent = occupancyRatePercent,
    )

    fun computeHonestMetrics(
        events: List<ClientEvent>,
        payments: List<PaymentRecord>,
        quotes: List<QuoteRecord>,
        baseMetrics: BusinessMetrics,
    ): BusinessMetrics {
        val activeEvents = events.filter { event -> isActiveEvent(event) }
        val totalPaymentsDop = payments.sumOf { payment -> payment.amountDop }
        val activeReceivableDop = activeEvents.sumOf { event -> event.balanceDop }

        val pendingRequestKeys = LinkedHashSet<String>()
        events.forEach { event ->
            if (event.status == EventStatus.PENDING_QUOTE) {
                pendingRequestKeys.add(event.id)
            }
        }
        quotes.forEach { quote ->
            if (quote.status.equals("PENDING", ignoreCase = true)) {
                val requestKey = quote.eventId.ifBlank { quote.id }
                pendingRequestKeys.add(requestKey)
            }
        }

        return baseMetrics.copy(
            octoberRevenueDop = totalPaymentsDop.coerceAtLeast(0),
            accountsReceivableDop = activeReceivableDop.coerceAtLeast(0),
            activeEventsCount = activeEvents.size,
            newRequestsCount = pendingRequestKeys.size,
        )
    }

    fun normalizePaymentMethod(rawMethodLabel: String): NormalizedPaymentMethod {
        val lower = rawMethodLabel.trim().lowercase()
        val isCash = lower.contains("efectivo")
        val isCashEnglish = lower.contains("cash")
        val isCard = lower.contains("tarjeta")
        val isVisa = lower.contains("visa")
        val isMastercard = lower.contains("master")
        val canonicalMethod = when {
            isCash -> "Efectivo"
            isCashEnglish -> "Efectivo"
            isCard -> "Tarjeta"
            isVisa -> "Tarjeta"
            isMastercard -> "Tarjeta"
            else -> "Transferencia"
        }
        val bankName = when {
            lower.contains("popular") -> "Banco Popular"
            lower.contains("reservas") -> "Banreservas"
            lower.contains("bhd") -> "Banco BHD"
            lower.contains("scotiabank") -> "Scotiabank"
            else -> null
        }
        return NormalizedPaymentMethod(
            canonicalMethod = canonicalMethod,
            bankName = bankName,
        )
    }

    fun resolveDeliveryFeeDop(
        venue: String,
        settings: BusinessSettings,
    ): Int {
        val lowerVenue = venue.trim().lowercase()
        val matchedZone = settings.zones.firstOrNull { zone ->
            lowerVenue.contains(zone.name.lowercase())
        }
        if (matchedZone != null) {
            return matchedZone.feeDop
        }
        if (lowerVenue.contains("colonial")) {
            val colonialZone = settings.zones.firstOrNull { zone ->
                zone.name.contains("Colonial", ignoreCase = true)
            }
            if (colonialZone != null) return colonialZone.feeDop
        }
        return settings.zones.firstOrNull { zone -> zone.active }?.feeDop ?: 350
    }

    fun selectLeastLoadedStaffLead(
        staffMembers: List<StaffMember>,
        events: List<ClientEvent>,
    ): String {
        val operationalCandidates = staffMembers.filter { member ->
            val isActiveMember = member.active
            val isNotOwner = member.role != UserRole.ADMIN_OWNER
            if (isActiveMember) isNotOwner else false
        }
        val pool = operationalCandidates.ifEmpty { staffMembers }
        val chosen = pool.minByOrNull { member ->
            val assignedActiveEvents = events.count { event ->
                val sameLead = event.leadStaffName.equals(member.name, ignoreCase = true)
                val activeFlag = isActiveEvent(event)
                if (sameLead) activeFlag else false
            }
            assignedActiveEvents * 10 + member.activeEventsCount
        }
        return chosen?.name ?: "Yuli"
    }

    fun selectPhotoForArticle(category: String, name: String): PhotoAsset {
        val lowerCategory = category.lowercase()
        val lowerName = name.lowercase()
        return when {
            lowerCategory.contains("flor") -> PhotoAsset.CENTRO_FLORAL
            lowerName.contains("flor") -> PhotoAsset.CENTRO_FLORAL
            lowerCategory.contains("arco") -> PhotoAsset.ARCO_ROSA
            lowerCategory.contains("globo") -> PhotoAsset.ARCO_ROSA
            lowerName.contains("arco") -> PhotoAsset.ARCO_ROSA
            lowerCategory.contains("dulce") -> PhotoAsset.MESA_HELADOS
            lowerCategory.contains("helado") -> PhotoAsset.MESA_HELADOS
            lowerName.contains("dulce") -> PhotoAsset.MESA_HELADOS
            lowerName.contains("plegable") -> PhotoAsset.MESA_PLEGABLE
            lowerName.contains("mesa") -> PhotoAsset.MESA_REDONDA
            else -> PhotoAsset.SILLA_TIFFANY
        }
    }

    fun selectPhotoForEventType(eventType: String): PhotoAsset {
        val lower = eventType.lowercase()
        return when {
            lower.contains("quince") -> PhotoAsset.QUINCEANERA
            lower.contains("cumple") -> PhotoAsset.CUMPLE_INFANTIL
            lower.contains("baby") -> PhotoAsset.BABY_SHOWER
            lower.contains("grad") -> PhotoAsset.GRADUACION
            lower.contains("corp") -> PhotoAsset.CORPORATIVO
            else -> PhotoAsset.BODA
        }
    }

    fun isActiveEvent(event: ClientEvent): Boolean {
        if (event.status == EventStatus.CANCELLED) return false
        if (event.status == EventStatus.COMPLETED) return false
        return true
    }

    fun mapEventStatus(rawStatus: String): EventStatus {
        val normalized = rawStatus.trim().uppercase()
        return when (normalized) {
            "PENDING_QUOTE", "PENDING", "DRAFT" -> EventStatus.PENDING_QUOTE
            "IN_SETUP", "SETUP" -> EventStatus.IN_SETUP
            "COMPLETED", "DONE" -> EventStatus.COMPLETED
            "CANCELLED", "CANCELED" -> EventStatus.CANCELLED
            else -> EventStatus.CONFIRMED
        }
    }

    private fun resolveCategoryDisplayName(
        categoryId: String,
        categories: List<CatalogCategory>,
        fallback: String?,
    ): String {
        val trimmedId = categoryId.trim()
        val matchedCategory = categories.firstOrNull { candidate ->
            val sameId = candidate.id.equals(trimmedId, ignoreCase = true)
            val sameName = candidate.name.equals(trimmedId, ignoreCase = true)
            if (sameId) true else sameName
        }
        if (matchedCategory != null) {
            return matchedCategory.name
        }
        return mapCategoryLabel(categoryId, fallback)
    }

    private fun mapCategoryLabel(categoryId: String, fallback: String?): String {
        if (!fallback.isNullOrBlank()) return fallback
        val lower = categoryId.trim().lowercase()
        return when {
            lower.contains("mobil") -> "Mobiliario"
            lower.contains("flor") -> "Diseño floral"
            lower.contains("glob") -> "Arcos y globos"
            lower.contains("dulce") -> "Mesas de dulces"
            else -> categoryId.replaceFirstChar { character -> character.uppercase() }
        }
    }

    private fun mapUserRole(rawRole: String, fullName: String): UserRole {
        val lowerRole = rawRole.trim().lowercase()
        val lowerName = fullName.trim().lowercase()
        return when {
            lowerRole.contains("admin") -> UserRole.ADMIN_OWNER
            lowerName == "maría" -> UserRole.ADMIN_OWNER
            lowerName == "maria" -> UserRole.ADMIN_OWNER
            lowerRole.contains("logistic") -> UserRole.STAFF_LOGISTICS
            lowerName == "pedro" -> UserRole.STAFF_LOGISTICS
            lowerRole.contains("warehouse") -> UserRole.STAFF_WAREHOUSE
            lowerName == "andrés" -> UserRole.STAFF_WAREHOUSE
            lowerName == "andres" -> UserRole.STAFF_WAREHOUSE
            else -> UserRole.STAFF_DECORATION
        }
    }

    fun toMediaUploadAsset(dto: MediaAssetDto): MediaUploadAsset = MediaUploadAsset(
        id = dto.id,
        url = dto.url,
        mimeType = dto.mimeType,
        byteSize = dto.byteSize,
        filename = dto.filename,
    )

    fun toCardSessionResult(dto: CardSessionResultDto): CardSessionResult = CardSessionResult(
        sessionId = dto.sessionId,
        checkoutUrl = dto.checkoutUrl,
        provider = dto.provider,
    )

    fun toAvailabilityRangeCheck(dto: AvailabilityCheckOutputDto): AvailabilityRangeCheck = AvailabilityRangeCheck(
        available = dto.available,
        eventDateIso = dto.eventDateIso,
        pickupDateIso = dto.pickupDateIso,
        returnDateIso = dto.returnDateIso,
        conflicts = dto.conflicts.map { conflict ->
            AvailabilityConflictItem(
                articleId = conflict.articleId,
                articleName = conflict.articleName,
                requested = conflict.requested,
                available = conflict.available,
                dateRange = conflict.dateRange,
            )
        },
    )

    fun toRealtimeStreamEvent(
        dto: RealtimeEventDto,
        fallbackType: String = "",
        resolvedSeq: Long = dto.seq,
    ): RealtimeStreamEvent = RealtimeStreamEvent(
        seq = resolvedSeq,
        type = dto.type.ifBlank { fallbackType },
        entityId = dto.entityId,
        clientId = dto.clientId,
        audience = dto.audience.ifBlank { "all" },
        summary = dto.summary,
    )

    fun toUserPreferences(dto: UserPreferencesDto?): UserPreferences {
        if (dto == null) return UserPreferences()
        return UserPreferences(
            pushNotificationsEnabled = dto.pushNotificationsEnabled,
            whatsappAlertsEnabled = dto.whatsappAlertsEnabled,
            emailReceiptsEnabled = dto.emailReceiptsEnabled,
            biometricLockEnabled = dto.biometricLockEnabled,
            autoSyncWifiOnly = dto.autoSyncWifiOnly,
        )
    }

    fun toOAuthProviderConfig(dto: OAuthConfigOutputDto): OAuthProviderConfig = OAuthProviderConfig(
        googleEnabled = dto.googleEnabled,
        appleEnabled = dto.appleEnabled,
        googleClientId = dto.googleClientId,
        appleClientId = dto.appleClientId,
        webPushVapidPublicKey = dto.webPushVapidPublicKey,
    )

    fun toPaymentsAnalyticsSummary(dto: PaymentsAnalyticsDto): PaymentsAnalyticsSummary = PaymentsAnalyticsSummary(
        range = dto.range,
        monthlySeries = dto.monthlySeries.map { bar ->
            MonthlyAnalyticsBar(
                monthKey = bar.monthKey,
                monthLabel = bar.label,
                revenueDop = bar.revenueRD.toInt(),
                expensesDop = bar.expensesRD.toInt(),
                eventsCount = bar.eventsCount.toInt(),
            )
        },
        totalRevenueDop = dto.totalRevenueRD.toInt(),
        totalExpensesDop = dto.totalExpensesRD.toInt(),
        netMarginPercent = dto.netMarginPercent.toInt(),
        totalEventsCount = dto.totalEventsCount.toInt(),
        confirmedEventsCount = dto.confirmedEventsCount.toInt(),
        pendingEventsCount = dto.pendingEventsCount.toInt(),
        completedEventsCount = dto.completedEventsCount.toInt(),
        cancelledEventsCount = dto.cancelledEventsCount.toInt(),
        eventTypesBreakdown = dto.eventTypesBreakdown.map { item ->
            EventTypeCount(
                eventType = item.label.ifBlank { item.key },
                count = item.count.toInt(),
            )
        },
        uniqueClientsCount = dto.uniqueClientsCount.toInt(),
        repeatClientsCount = dto.repeatClientsCount.toInt(),
        collectionRatePercent = dto.collectionRatePercent.toInt(),
    )

    fun toRosaSketchProposal(dto: RosaSketchResponseDto): RosaSketchProposal = RosaSketchProposal(
        reply = dto.reply,
        elements = dto.elements.map { element ->
            RosaSketchElement(
                id = element.id,
                kind = element.kind,
                label = element.label,
                xRatio = element.xRatio,
                yRatio = element.yRatio,
                capacity = element.capacity,
            )
        },
        eventWidthMeters = dto.eventWidthMeters,
        eventDepthMeters = dto.eventDepthMeters,
        provider = dto.provider,
        model = dto.model,
    )
}

internal fun RealtimeEventDto.toDomain(
    fallbackType: String = "",
    resolvedSeq: Long = seq,
): RealtimeStreamEvent = RosaFiestaBackendMappers.toRealtimeStreamEvent(
    dto = this,
    fallbackType = fallbackType,
    resolvedSeq = resolvedSeq,
)
