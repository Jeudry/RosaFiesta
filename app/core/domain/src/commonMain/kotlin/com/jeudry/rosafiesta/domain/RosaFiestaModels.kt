package com.jeudry.rosafiesta.domain

import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.core.engine.UserRole

/**
 * Fotografías reales disponibles en el sistema de diseño (Sección 4.6).
 */
public enum class PhotoAsset(public val key: String, public val label: String) {
    SILLA_TIFFANY("silla_tiffany", "Silla Tiffany dorada"),
    MESA_PLEGABLE("mesa_plegable", "Mesa rectangular plegable"),
    MESA_REDONDA("mesa_redonda", "Mesa redonda para 8"),
    CENTRO_FLORAL("centro_floral", "Centro de mesa floral"),
    ARCO_ROSA("arco_rosa", "Arco orgánico rosa"),
    MESA_HELADOS("mesa_helados", "Mesa de dulces / helados"),
    BODA("boda", "Bodas"),
    QUINCEANERA("quinceanera", "Quinceañeras"),
    CUMPLE_INFANTIL("cumple_infantil", "Cumpleaños infantil"),
    BABY_SHOWER("baby_shower", "Baby shower"),
    GRADUACION("graduacion", "Graduaciones"),
    CORPORATIVO("corporativo", "Corporativos"),
}

/**
 * Ilustraciones vectoriales editoriales (`Illo`, Sección 4.7).
 */
public enum class IllustrationKind(public val title: String) {
    MOBILIARIO("Mobiliario"),
    GLOBOS("Arcos y globos"),
    FLORES("Diseño floral"),
    DULCES("Mesas de dulces"),
    MANTELERIA("Mantelería y vajilla"),
    ILUMINACION("Iluminación y pistas"),
    EMPTY_EVENTS("Sin eventos activos"),
    EMPTY_QUOTES("Sin cotizaciones pendientes"),
    EMPTY_SEARCH("Sin resultados de búsqueda"),
    CELEBRATION("Reserva confirmada"),
    ROSA_SPARK("Rosa IA"),
}

/**
 * Artículo del catálogo de alquiler por día (`s01`, `s18`–`s23`, `s63`, `A09`, `A23`).
 */
public data class CatalogArticle(
    public val id: String,
    public val code: String,
    public val name: String,
    public val category: String,
    public val description: String,
    public val pricePerDayDop: Int,
    public val totalStock: Int,
    public val availableOct23: Int,
    public val photo: PhotoAsset,
    public val variants: List<String>,
) {
    public val formattedDailyRate: String
        get() = RosaFiestaEngine.formatDailyRateDop(pricePerDayDop)
}

/**
 * Paquete preconfigurado de evento (`s21`, `s22`, `A24`).
 */
public data class EventPackage(
    public val id: String,
    public val name: String,
    public val subtitle: String,
    public val suggestedGuests: Int,
    public val priceDop: Int,
    public val savingsDop: Int,
    public val photo: PhotoAsset,
    public val includedItems: List<String>,
)

/**
 * Plantilla de tipo de evento (`s09`, `s16`).
 */
public data class EventTypeTemplate(
    public val id: String,
    public val name: String,
    public val subtitle: String,
    public val defaultGuests: Int,
    public val startingPriceDop: Int,
    public val photo: PhotoAsset,
    public val paletteHex: List<String>,
)

/**
 * Renglón de artículo dentro de un evento o cotización.
 */
public data class EventLineItem(
    public val articleCode: String,
    public val name: String,
    public val quantity: Int,
    public val unitPriceDop: Int,
    public val photo: PhotoAsset,
) {
    public val subtotalDop: Int
        get() = quantity * unitPriceDop
}

/**
 * Estado del ciclo de vida de un evento (`s26`–`s29`, `s33`–`s39`, `A05`–`A14`).
 */
public enum class EventStatus(public val label: String) {
    PENDING_QUOTE("En revisión"),
    CONFIRMED("Confirmado"),
    IN_SETUP("En montaje"),
    COMPLETED("Completado"),
    CANCELLED("Cancelado"),
}

/**
 * Evento planificado o confirmado en RosaFiesta (Sección 6).
 */
public data class ClientEvent(
    public val id: String,
    public val code: String,
    public val title: String,
    public val clientName: String,
    public val eventType: String,
    public val dateLabel: String,
    public val daysUntilEvent: Int,
    public val venue: String,
    public val zone: String,
    public val guests: Int,
    public val articleCount: Int,
    public val totalDop: Int,
    public val paidDepositDop: Int,
    public val depositMethodLabel: String,
    public val depositDateLabel: String,
    public val balanceDueDateLabel: String,
    public val status: EventStatus,
    public val leadStaffName: String,
    public val photo: PhotoAsset,
    public val items: List<EventLineItem>,
) {
    public val balanceDop: Int
        get() = (totalDop - paidDepositDop).coerceAtLeast(0)
}

/**
 * Registro de pago o abono (`s30`–`s32`, `s38`–`s39`, `s69`–`s71`, `A19`–`A22`).
 */
public data class PaymentRecord(
    public val id: String,
    public val receiptNumber: String,
    public val eventId: String,
    public val eventTitle: String,
    public val clientName: String,
    public val amountDop: Int,
    public val methodLabel: String,
    public val kindLabel: String,
    public val dateLabel: String,
    public val referenceCode: String,
    public val verified: Boolean,
)

/**
 * Paso del checklist de montaje en campo (`s68`, `A11`).
 */
public data class ChecklistStep(
    public val id: String,
    public val stageLabel: String,
    public val title: String,
    public val completed: Boolean,
    public val completedBy: String,
    public val timeLabel: String,
)

/**
 * Parada en la ruta de entrega del día (`A13`).
 */
public data class DeliveryStop(
    public val order: Int,
    public val eventTitle: String,
    public val venue: String,
    public val windowLabel: String,
    public val vehicleAndCrew: String,
    public val statusLabel: String,
)

/**
 * Incidencia de daño o pérdida post-evento (`A14`).
 */
public data class DamageIncident(
    public val id: String,
    public val eventTitle: String,
    public val articleName: String,
    public val damagedUnits: Int,
    public val missingUnits: Int,
    public val replacementDop: Int,
    public val notes: String,
)

/**
 * Cambio guardado localmente en la cola offline (`A36`, `A37`).
 */
public data class OfflineQueueItem(
    public val id: String,
    public val actionLabel: String,
    public val entityCode: String,
    public val timeAgo: String,
    public val synced: Boolean,
)

/**
 * Integrante del equipo de RosaFiesta (`A28`, `A30`).
 */
public data class StaffProfile(
    public val id: String,
    public val name: String,
    public val roleLabel: String,
    public val role: UserRole,
    public val activeEventsCount: Int,
    public val permissions: Set<StaffPermission>,
)

/**
 * Ficha de cliente en la cartera administrativa (`A26`, `A27`).
 */
public data class ClientProfile(
    public val id: String,
    public val fullName: String,
    public val email: String,
    public val phone: String,
    public val zone: String,
    public val eventsCount: Int,
    public val totalContractedDop: Int,
    public val pendingBalanceDop: Int,
    public val vipTag: String,
)

/**
 * Indicadores globales del negocio (Sección 6: octubre RD$ 12,450 · por cobrar RD$ 7,724 · 4 eventos activos · 2 solicitudes nuevas).
 */
public data class BusinessMetrics(
    public val monthLabel: String,
    public val octoberRevenueDop: Int,
    public val accountsReceivableDop: Int,
    public val activeEventsCount: Int,
    public val newRequestsCount: Int,
    public val occupancyRatePercent: Int,
)
