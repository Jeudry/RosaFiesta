// Package domain define las entidades operativas de logística, incidencias, CRM y bot.
package domain

import "errors"

var (
	// ErrOperationNotFound indica que la orden logística del evento no existe.
	ErrOperationNotFound = errors.New("event logistics order not found")
	// ErrChecklistNotFound indica que el ítem del checklist operativo no existe.
	ErrChecklistNotFound = errors.New("checklist item not found")
	// ErrDamageIncidentNotFound indica que el reporte de daño solicitado no existe.
	ErrDamageIncidentNotFound = errors.New("damage incident not found")
	// ErrInvalidReplacement indica que el costo de reposición no puede ser negativo.
	ErrInvalidReplacement = errors.New("replacement charge must be greater than or equal to zero")
	// ErrReviewNotFound indica que la reseña solicitada no existe.
	ErrReviewNotFound = errors.New("review not found")
	// ErrLeadNotFound indica que el prospecto CRM no existe.
	ErrLeadNotFound = errors.New("crm lead not found")
	// ErrNotificationNotFound indica que la notificación no existe.
	ErrNotificationNotFound = errors.New("notification not found")
	// ErrNotificationForbidden indica que la notificación pertenece a otro destinatario.
	ErrNotificationForbidden = errors.New("notification belongs to another recipient")
	// ErrSupplierNotFound indica que el proveedor solicitado no existe.
	ErrSupplierNotFound = errors.New("supplier not found")
	// ErrBotEntryNotFound indica que la regla o FAQ del bot no existe.
	ErrBotEntryNotFound = errors.New("bot faq or keyword not found")
	// ErrWebhookSignatureInvalid indica que la firma HMAC del webhook de WhatsApp no es válida.
	ErrWebhookSignatureInvalid = errors.New("invalid whatsapp webhook signature")
	// ErrBotThreadNotFound indica que el hilo de conversación WhatsApp no existe.
	ErrBotThreadNotFound = errors.New("whatsapp bot thread not found")
	// ErrWhatsAppNotConfigured indica que WhatsApp Cloud API no está configurado.
	ErrWhatsAppNotConfigured = errors.New("whatsapp cloud api provider is not configured")
	// ErrWhatsAppVerifyFailed indica que el token de verificación del webhook de WhatsApp no coincide.
	ErrWhatsAppVerifyFailed = errors.New("whatsapp webhook verification failed")
	// ErrInvalidRating indica que la calificación de la reseña debe estar entre 1 y 5.
	ErrInvalidRating = errors.New("review rating must be between 1 and 5")
	// ErrInvalidOperationsInput indica parámetros inválidos en la operación.
	ErrInvalidOperationsInput = errors.New("invalid operations payload")
	// ErrInvalidReportRequest indica un tipo o período de reporte no soportado.
	ErrInvalidReportRequest = errors.New("invalid report type or period; use income|events|clients|inventory|dgii and 6m|12m")
)

// ChecklistStage agrupa los pasos operativos según la etapa del evento.
type ChecklistStage string

const (
	// StageWarehouseLoad corresponde al despacho y carga en bodega.
	StageWarehouseLoad ChecklistStage = "carga_bodega"
	// StageInTransit corresponde al traslado hacia la locación.
	StageInTransit ChecklistStage = "en_transito"
	// StageOnSiteSetup corresponde al montaje en sitio.
	StageOnSiteSetup ChecklistStage = "montaje_sitio"
	// StageTeardown corresponde al desmontaje y retorno a bodega.
	StageTeardown ChecklistStage = "desmontaje_recogida"
)

// ChecklistItem representa una tarea verificable de logística en campo.
type ChecklistItem struct {
	ID          string         `json:"id"`
	Stage       ChecklistStage `json:"stage"`
	Title       string         `json:"title"`
	Completed   bool           `json:"completed"`
	CompletedBy string         `json:"completedBy,omitempty"`
	CompletedAt string         `json:"completedAt,omitempty"`
}

// RouteStop describe una parada de entrega o recogida en la ruta diaria.
type RouteStop struct {
	Order           int    `json:"order"`
	EventID         string `json:"eventId"`
	EventTitle      string `json:"eventTitle"`
	Venue           string `json:"venue"`
	WindowLabel     string `json:"windowLabel"`
	AssignedVehicle string `json:"assignedVehicle"`
	Status          string `json:"status"`
}

// DamageReport registra artículos dañados o faltantes durante el evento.
type DamageReport struct {
	ID              string `json:"id"`
	EventID         string `json:"eventId"`
	ArticleCode     string `json:"articleCode"`
	ArticleName     string `json:"articleName"`
	DamagedUnits    int    `json:"damagedUnits"`
	MissingUnits    int    `json:"missingUnits"`
	ReplacementRD   int    `json:"replacementRD"`
	Notes           string `json:"notes"`
	PhotoURL        string `json:"photoUrl,omitempty"`
	ResolutionNotes string `json:"resolutionNotes,omitempty"`
	ChargePaymentID string `json:"chargePaymentId,omitempty"`
	Status          string `json:"status,omitempty"`
}

// OfflineMutation representa una operación capturada en campo sin conectividad.
type OfflineMutation struct {
	ID             string `json:"id"`
	ActionLabel    string `json:"actionLabel"`
	EntityCode     string `json:"entityCode"`
	OperationType  string `json:"operationType,omitempty"`
	PayloadJSON    string `json:"payloadJson,omitempty"`
	IdempotencyKey string `json:"idempotencyKey,omitempty"`
	CreatedAt      string `json:"createdAt"`
	Synced         bool   `json:"synced"`
}

// EventLogistics agrupa itinerario, equipo asignado, checklist e incidencias de un evento.
type EventLogistics struct {
	EventID        string          `json:"eventId"`
	EventTitle     string          `json:"eventTitle"`
	DateLabel      string          `json:"dateLabel"`
	SetupWindow    string          `json:"setupWindow"`
	TeardownWindow string          `json:"teardownWindow"`
	Venue          string          `json:"venue"`
	LeadStaffName  string          `json:"leadStaffName"`
	AssignedCrew   []string        `json:"assignedCrew"`
	VehicleName    string          `json:"vehicleName"`
	Checklist      []ChecklistItem `json:"checklist"`
	EvidencePhotos int             `json:"evidencePhotos"`
	DamageReports  []DamageReport  `json:"damageReports"`
}

// CompletionPercent calcula el porcentaje de tareas completadas del checklist.
func (l EventLogistics) CompletionPercent() int {
	if len(l.Checklist) == 0 {
		return 0
	}
	done := 0
	for _, item := range l.Checklist {
		if item.Completed {
			done++
		}
	}
	return (done * 100) / len(l.Checklist)
}

// CanonicalLogistics devuelve los registros logísticos iniciales de demostración.
func CanonicalLogistics() []EventLogistics {
	return []EventLogistics{
		{ //nolint:gosec
			EventID:        "evt-boda-real",
			EventTitle:     "Boda Real Rosa & Dorado",
			DateLabel:      "Vie 23 oct",
			SetupWindow:    "09:00 AM – 01:30 PM",
			TeardownWindow: "11:30 PM – 01:00 AM",
			Venue:          "Jardín Botánico · Pabellón de Orquídeas",
			LeadStaffName:  "Pedro Santos",
			AssignedCrew:   []string{"Pedro Santos", "Yuli Mercedes", "Andrés Cuevas"},
			VehicleName:    "Camión Daihatsu Blanco #01",
			EvidencePhotos: 4,
			Checklist:      bodaChecklist(),
			DamageReports: []DamageReport{
				{
					ID:            "dmg-1",
					EventID:       "evt-boda-real",
					ArticleCode:   "RF-004",
					ArticleName:   "Centro de mesa floral (base cristal)",
					DamagedUnits:  1,
					MissingUnits:  0,
					ReplacementRD: 120,
					Notes:         "Base de cristal astillada durante el brindis en mesa #4",
				},
			},
		},
	}
}

func bodaChecklist() []ChecklistItem {
	return []ChecklistItem{
		{
			ID: "chk-1", Stage: StageWarehouseLoad,
			Title:       "Contar y proteger 120 sillas Tiffany doradas con fundas",
			Completed:   true,
			CompletedBy: "Andrés Cuevas",
			CompletedAt: "08:15 AM",
		},
		{
			ID: "chk-2", Stage: StageWarehouseLoad,
			Title:       "Cargar 15 mesas redondas y estructura de arco orgánico",
			Completed:   true,
			CompletedBy: "Pedro Santos",
			CompletedAt: "08:40 AM",
		},
		{
			ID: "chk-3", Stage: StageInTransit,
			Title:       "Salida hacia Jardín Botánico y registro en garita de servicio",
			Completed:   true,
			CompletedBy: "Pedro Santos",
			CompletedAt: "09:20 AM",
		},
		{
			ID: "chk-4", Stage: StageOnSiteSetup,
			Title:       "Distribución de 15 mesas según plano del salón",
			Completed:   true,
			CompletedBy: "Pedro Santos",
			CompletedAt: "10:45 AM",
		},
		{
			ID:        "chk-5",
			Stage:     StageOnSiteSetup,
			Title:     "Instalación de arco orgánico rosa de 3.2 m y 8 centros florales",
			Completed: false,
		},
		{
			ID:        "chk-6",
			Stage:     StageTeardown,
			Title:     "Conteo final de retorno y revisión de daños post-evento",
			Completed: false,
		},
	}
}

// CanonicalRouteStops devuelve las paradas de ruta iniciales de demostración.
func CanonicalRouteStops() []RouteStop {
	return []RouteStop{
		{
			Order:           1,
			EventID:         "evt-boda-real",
			EventTitle:      "Boda Real Rosa & Dorado",
			Venue:           "Jardín Botánico · Av. República de Colombia",
			WindowLabel:     "09:00 AM – 01:30 PM",
			AssignedVehicle: "Camión #01 · Pedro Santos",
			Status:          "en_sitio",
		},
		{
			Order:           2,
			EventID:         "evt-quince-valentina",
			EventTitle:      "Inspección técnica · Salón Gran Mirador",
			Venue:           "Av. Abraham Lincoln, Piantini",
			WindowLabel:     "03:00 PM – 04:00 PM",
			AssignedVehicle: "Furgoneta #02 · Yuli Mercedes",
			Status:          "programado",
		},
	}
}

// CanonicalOfflineQueue devuelve las mutaciones fuera de línea iniciales de demostración.
func CanonicalOfflineQueue() []OfflineMutation {
	return []OfflineMutation{
		{
			ID:          "off-1",
			ActionLabel: "Checklist marcado: Distribución de 15 mesas",
			EntityCode:  "RF-2026-1023",
			CreatedAt:   "Hace 8 min",
			Synced:      false,
		},
		{
			ID:          "off-2",
			ActionLabel: "Foto de evidencia subida (Arco principal)",
			EntityCode:  "RF-2026-1023",
			CreatedAt:   "Hace 5 min",
			Synced:      false,
		},
		{
			ID:          "off-3",
			ActionLabel: "Ajuste de stock en bodega: +2 Centros florales",
			EntityCode:  "RF-004",
			CreatedAt:   "Hace 2 min",
			Synced:      false,
		},
	}
}
