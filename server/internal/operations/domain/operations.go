package domain

import "errors"

var (
	ErrOperationNotFound      = errors.New("event logistics order not found")
	ErrChecklistNotFound      = errors.New("checklist item not found")
	ErrInvalidReplacement     = errors.New("replacement charge must be greater than or equal to zero")
	ErrReviewNotFound         = errors.New("review not found")
	ErrLeadNotFound           = errors.New("crm lead not found")
	ErrNotificationNotFound   = errors.New("notification not found")
	ErrBotThreadNotFound      = errors.New("whatsapp bot thread not found")
	ErrInvalidRating          = errors.New("review rating must be between 1 and 5")
	ErrInvalidOperationsInput = errors.New("invalid operations payload")
	ErrInvalidReportRequest   = errors.New("invalid report type or period; use income|events|clients|inventory|dgii and 6m|12m")
)

type ChecklistStage string

const (
	StageWarehouseLoad ChecklistStage = "carga_bodega"
	StageInTransit     ChecklistStage = "en_transito"
	StageOnSiteSetup   ChecklistStage = "montaje_sitio"
	StageTeardown      ChecklistStage = "desmontaje_recogida"
)

type ChecklistItem struct {
	ID          string         `json:"id"`
	Stage       ChecklistStage `json:"stage"`
	Title       string         `json:"title"`
	Completed   bool           `json:"completed"`
	CompletedBy string         `json:"completedBy,omitempty"`
	CompletedAt string         `json:"completedAt,omitempty"`
}

type RouteStop struct {
	Order           int    `json:"order"`
	EventID         string `json:"eventId"`
	EventTitle      string `json:"eventTitle"`
	Venue           string `json:"venue"`
	WindowLabel     string `json:"windowLabel"`
	AssignedVehicle string `json:"assignedVehicle"`
	Status          string `json:"status"`
}

type DamageReport struct {
	ID            string `json:"id"`
	EventID       string `json:"eventId"`
	ArticleCode   string `json:"articleCode"`
	ArticleName   string `json:"articleName"`
	DamagedUnits  int    `json:"damagedUnits"`
	MissingUnits  int    `json:"missingUnits"`
	ReplacementRD int    `json:"replacementRD"`
	Notes         string `json:"notes"`
}

type OfflineMutation struct {
	ID          string `json:"id"`
	ActionLabel string `json:"actionLabel"`
	EntityCode  string `json:"entityCode"`
	CreatedAt   string `json:"createdAt"`
	Synced      bool   `json:"synced"`
}

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
