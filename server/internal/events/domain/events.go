// Package domain contiene las entidades y reglas de eventos, plantillas y estados de reserva.
package domain

import "errors"

// Errores del dominio de eventos.
var (
	ErrEventNotFound          = errors.New("evento no encontrado")
	ErrInvalidEventStatus     = errors.New("estado de evento inválido")
	ErrCannotCancelCompleted  = errors.New("no se puede cancelar un evento completado")
	ErrInvalidGuestCount      = errors.New("el número de invitados debe ser mayor a cero")
	ErrDateChangeNotPermitted = errors.New("el cambio de fecha no está permitido a menos de 48 horas del evento")
)

// EventStatus representa el ciclo de vida de un evento en RosaFiesta.
type EventStatus string

// Estados de un evento.
const (
	StatusDraft      EventStatus = "draft"
	StatusPending    EventStatus = "pending"
	StatusConfirmed  EventStatus = "confirmed"
	StatusInProgress EventStatus = "in_progress"
	StatusCompleted  EventStatus = "completed"
	StatusCancelled  EventStatus = "cancelled"
)

// EventTemplate representa una plantilla de tipo de evento (Bodas, Quinceañeras, etc.).
type EventTemplate struct {
	ID            string   `json:"id"`
	Slug          string   `json:"slug"`
	Name          string   `json:"name"`
	Subtitle      string   `json:"subtitle"`
	DefaultGuests int      `json:"defaultGuests"`
	StartingPrice int      `json:"startingPriceRD"`
	PhotoKey      string   `json:"photoKey"`
	PaletteColors []string `json:"paletteColors"`
}

// EventItem representa un artículo reservado dentro de un evento.
type EventItem struct {
	ArticleCode string `json:"articleCode"`
	Name        string `json:"name"`
	Quantity    int    `json:"quantity"`
	UnitPriceRD int    `json:"unitPriceRD"`
}

// SubtotalRD calcula el subtotal de la línea en RD$.
func (i EventItem) SubtotalRD() int {
	return i.Quantity * i.UnitPriceRD
}

// Event representa un evento planificado o confirmado de un cliente.
type Event struct {
	ID              string      `json:"id"`
	Code            string      `json:"code"`
	ClientName      string      `json:"clientName"`
	Title           string      `json:"title"`
	EventType       string      `json:"eventType"`
	DateLabel       string      `json:"dateLabel"`
	DaysUntilEvent  int         `json:"daysUntilEvent"`
	Venue           string      `json:"venue"`
	Zone            string      `json:"zone"`
	Guests          int         `json:"guests"`
	ArticleCount    int         `json:"articleCount"`
	TotalRD         int         `json:"totalRD"`
	PaidDepositRD   int         `json:"paidDepositRD"`
	BalanceDueDate  string      `json:"balanceDueDate"`
	Status          EventStatus `json:"status"`
	DateChangesUsed int         `json:"dateChangesUsed"`
	AssignedLead    string      `json:"assignedLead"`
	Items           []EventItem `json:"items"`
}

// BalanceRD devuelve el saldo pendiente por pagar en RD$.
func (e Event) BalanceRD() int {
	balance := e.TotalRD - e.PaidDepositRD
	if balance < 0 {
		return 0
	}
	return balance
}

// IsActive indica si el evento cuenta como activo (bloquea eliminación de cuenta s88).
func (e Event) IsActive() bool {
	return e.Status == StatusPending || e.Status == StatusConfirmed || e.Status == StatusInProgress
}

// RefundPolicyResult resume el cálculo de reembolso por cancelación (s37/s76).
type RefundPolicyResult struct {
	RefundPercent int    `json:"refundPercent"`
	RefundRD      int    `json:"refundRD"`
	RetainedRD    int    `json:"retainedRD"`
	PolicyTier    string `json:"policyTier"`
}

// CalculateCancellationRefund aplica la política oficial de cancelación:
// - Más de 7 días antes del evento: reembolso del 90% del anticipo.
// - Entre 3 y 7 días antes: reembolso del 50% del anticipo.
// - Menos de 3 días: sin reembolso (0%).
func CalculateCancellationRefund(paidDepositRD int, daysUntilEvent int) RefundPolicyResult {
	if paidDepositRD <= 0 {
		return RefundPolicyResult{
			RefundPercent: 0,
			RefundRD:      0,
			RetainedRD:    0,
			PolicyTier:    "sin_anticipo",
		}
	}
	if daysUntilEvent > 7 {
		refund := (paidDepositRD * 90) / 100
		return RefundPolicyResult{
			RefundPercent: 90,
			RefundRD:      refund,
			RetainedRD:    paidDepositRD - refund,
			PolicyTier:    "mas_de_7_dias_90_por_ciento",
		}
	}
	if daysUntilEvent >= 3 {
		refund := (paidDepositRD * 50) / 100
		return RefundPolicyResult{
			RefundPercent: 50,
			RefundRD:      refund,
			RetainedRD:    paidDepositRD - refund,
			PolicyTier:    "entre_3_y_7_dias_50_por_ciento",
		}
	}
	return RefundPolicyResult{
		RefundPercent: 0,
		RefundRD:      0,
		RetainedRD:    paidDepositRD,
		PolicyTier:    "menos_de_3_dias_0_por_ciento",
	}
}

// DateChangeEvaluation indica si un cambio de fecha es gratuito o requiere recargo (s36).
type DateChangeEvaluation struct {
	Allowed   bool   `json:"allowed"`
	IsFree    bool   `json:"isFree"`
	FeeRD     int    `json:"feeRD"`
	Reason    string `json:"reason"`
	NextCount int    `json:"nextCount"`
}

// EvaluateDateChange evalúa la política de cambio de fecha (primer cambio gratis sujeta a disponibilidad).
func EvaluateDateChange(e Event) (DateChangeEvaluation, error) {
	if e.Status == StatusCompleted || e.Status == StatusCancelled {
		return DateChangeEvaluation{}, ErrCannotCancelCompleted
	}
	if e.DaysUntilEvent < 2 {
		return DateChangeEvaluation{}, ErrDateChangeNotPermitted
	}
	if e.DateChangesUsed == 0 {
		return DateChangeEvaluation{
			Allowed:   true,
			IsFree:    true,
			FeeRD:     0,
			Reason:    "Primer cambio de fecha gratis según contrato",
			NextCount: 1,
		}, nil
	}
	return DateChangeEvaluation{
		Allowed:   true,
		IsFree:    false,
		FeeRD:     750,
		Reason:    "Cargo administrativo por cambio de fecha adicional",
		NextCount: e.DateChangesUsed + 1,
	}, nil
}

// CanonicalTemplates devuelve las 6 plantillas de tipos de evento de RosaFiesta.
func CanonicalTemplates() []EventTemplate {
	return []EventTemplate{
		{
			ID: "tpl-boda", Slug: "boda", Name: "Bodas",
			Subtitle: "Ceremonia romántica, recepción de gala y jardín", DefaultGuests: 120,
			StartingPrice: 4500, PhotoKey: "foto_boda",
			PaletteColors: []string{"#F48FB1", "#D4AF37", "#FFF8E7", "#4A2E35"},
		},
		{
			ID: "tpl-quince", Slug: "quinceanera", Name: "Quinceañeras",
			Subtitle: "Pista cristal, trono real y arco orgánico", DefaultGuests: 100,
			StartingPrice: 6200, PhotoKey: "foto_quinceanera",
			PaletteColors: []string{"#EA4EA9", "#945AE1", "#FDE6F2", "#1A1A2E"},
		},
		{
			ID: "tpl-cumple", Slug: "cumpleanos-infantil", Name: "Cumpleaños infantil",
			Subtitle: "Mesa de dulces, arco temático y rincón de fotos", DefaultGuests: 40,
			StartingPrice: 405, PhotoKey: "foto_cumple_infantil",
			PaletteColors: []string{"#FFD45C", "#B9D84A", "#2FB8C8", "#EA4EA9"},
		},
		{
			ID: "tpl-baby", Slug: "baby-shower", Name: "Baby shower",
			Subtitle: "Tonos pastel, estación de postres y bienvenida tierna", DefaultGuests: 50,
			StartingPrice: 1250, PhotoKey: "foto_baby_shower",
			PaletteColors: []string{"#FDE6F2", "#DDF4F3", "#FFF2C9", "#EFE8FD"},
		},
		{
			ID: "tpl-grad", Slug: "graduacion", Name: "Graduaciones",
			Subtitle: "Escenario de brindis, letras luminosas y banquete", DefaultGuests: 80,
			StartingPrice: 2140, PhotoKey: "foto_graduacion",
			PaletteColors: []string{"#2E1A47", "#FFD45C", "#FAFAFC", "#865DEC"},
		},
		{
			ID: "tpl-corp", Slug: "corporativo", Name: "Corporativos",
			Subtitle: "Gala empresarial, lanzamiento de marca y cóctel", DefaultGuests: 150,
			StartingPrice: 8500, PhotoKey: "foto_corporativo",
			PaletteColors: []string{"#151729", "#7C5CF0", "#2FB8C8", "#ECEBFF"},
		},
	}
}

// CanonicalEvents devuelve los eventos canónicos del sistema de diseño (Sección 6).
func CanonicalEvents() []Event {
	return append(canonicalCamilaEvents(), canonicalOtherEvents()...)
}

func canonicalCamilaEvents() []Event {
	return []Event{
		{
			ID: "evt-boda-real", Code: "RF-2026-1023", ClientName: "Camila Rodríguez",
			Title: "Boda Real Rosa & Dorado", EventType: "Boda", DateLabel: "Vie 23 oct",
			DaysUntilEvent: 3, Venue: "Jardín Botánico", Zone: "Santo Domingo DN",
			Guests: 120, ArticleCount: 145, TotalRD: 4500, PaidDepositRD: 1350,
			BalanceDueDate: "Jue 22 oct", Status: StatusConfirmed, AssignedLead: "Pedro Santos",
			Items: []EventItem{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 120, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 15, UnitPriceRD: 25},
				{ArticleCode: "RF-004", Name: "Centro de mesa floral", Quantity: 8, UnitPriceRD: 35},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
		},
		{
			ID: "evt-quince-valentina", Code: "RF-2026-1102", ClientName: "Camila Rodríguez",
			Title: "Quinceañera Valentina Crystal", EventType: "Quinceañera", DateLabel: "Lun 2 nov",
			DaysUntilEvent: 13, Venue: "Salón Gran Mirador", Zone: "Piantini, DN",
			Guests: 100, ArticleCount: 118, TotalRD: 6200, PaidDepositRD: 1860,
			BalanceDueDate: "Vie 30 oct", Status: StatusConfirmed, AssignedLead: "Yuli Mercedes",
			Items: []EventItem{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 100, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 13, UnitPriceRD: 25},
				{ArticleCode: "RF-004", Name: "Centro de mesa floral", Quantity: 3, UnitPriceRD: 35},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
		},
	}
}

func canonicalOtherEvents() []Event {
	return []Event{
		{
			ID: "evt-cumple-mateo", Code: "RF-2026-1107", ClientName: "Laura Peña",
			Title: "Cumpleaños de Mateo", EventType: "Cumpleaños infantil", DateLabel: "Sáb 7 nov",
			DaysUntilEvent: 18, Venue: "Terraza Los Prados", Zone: "Los Prados, DN",
			Guests: 40, ArticleCount: 46, TotalRD: 405, PaidDepositRD: 0,
			BalanceDueDate: "Jue 5 nov", Status: StatusPending, AssignedLead: "Pedro Santos",
			Items: []EventItem{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 40, UnitPriceRD: 4},
				{ArticleCode: "RF-002", Name: "Mesa rectangular plegable", Quantity: 5, UnitPriceRD: 20},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
			},
		},
		{
			ID: "evt-grad-promo", Code: "RF-2026-1113", ClientName: "Ana Gómez",
			Title: "Graduación Promo 2026", EventType: "Graduación", DateLabel: "Vie 13 nov",
			DaysUntilEvent: 24, Venue: "Club Naco", Zone: "Naco, DN",
			Guests: 80, ArticleCount: 92, TotalRD: 2140, PaidDepositRD: 642,
			BalanceDueDate: "Mié 11 nov", Status: StatusConfirmed, AssignedLead: "Andrés Cuevas",
			Items: []EventItem{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 80, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 10, UnitPriceRD: 25},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
		},
		{
			ID: "evt-gala-caribe", Code: "RF-2026-1012", ClientName: "Grupo Caribe",
			Title: "Gala Corporativa Grupo Caribe", EventType: "Corporativo", DateLabel: "Lun 12 oct",
			DaysUntilEvent: -8, Venue: "Hotel Embajador", Zone: "Bella Vista, DN",
			Guests: 180, ArticleCount: 210, TotalRD: 8500, PaidDepositRD: 8500,
			BalanceDueDate: "Vie 9 oct", Status: StatusCompleted, AssignedLead: "Pedro Santos",
			Items: []EventItem{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 180, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 22, UnitPriceRD: 25},
				{ArticleCode: "RF-004", Name: "Centro de mesa floral", Quantity: 8, UnitPriceRD: 35},
			},
		},
	}
}
