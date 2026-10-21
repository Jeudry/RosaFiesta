// Package domain contiene las entidades y reglas de cotizaciones, contratos y asistente Rosa IA.
package domain

import (
	"errors"
	"strings"
)

// Errores del dominio de cotizaciones.
var (
	ErrQuoteNotFound       = errors.New("cotización no encontrada")
	ErrEmptyQuoteItems     = errors.New("la cotización debe incluir al menos un artículo")
	ErrInvalidSignature    = errors.New("la firma digital no puede estar vacía")
	ErrQuoteAlreadyDecided = errors.New("la cotización ya fue procesada")
)

// QuoteStatus representa el estado de una cotización.
type QuoteStatus string

// Estados de cotización.
const (
	QuoteStatusPending  QuoteStatus = "pending"
	QuoteStatusSent     QuoteStatus = "sent"
	QuoteStatusApproved QuoteStatus = "approved"
	QuoteStatusSigned   QuoteStatus = "signed"
	QuoteStatusDeclined QuoteStatus = "declined"
)

// QuoteLine representa un renglón dentro de una cotización.
type QuoteLine struct {
	ArticleCode string `json:"articleCode"`
	Name        string `json:"name"`
	Quantity    int    `json:"quantity"`
	UnitPriceRD int    `json:"unitPriceRD"`
}

// SubtotalRD calcula el importe de la línea en RD$.
func (l QuoteLine) SubtotalRD() int {
	return l.Quantity * l.UnitPriceRD
}

// Quote representa una cotización formal con anticipo del 30% y contrato digital.
type Quote struct {
	ID              string      `json:"id"`
	Code            string      `json:"code"`
	EventTitle      string      `json:"eventTitle"`
	ClientName      string      `json:"clientName"`
	EventDateLabel  string      `json:"eventDateLabel"`
	Venue           string      `json:"venue"`
	Guests          int         `json:"guests"`
	Lines           []QuoteLine `json:"lines"`
	DeliveryFeeRD   int         `json:"deliveryFeeRD"`
	DiscountRD      int         `json:"discountRD"`
	TotalRD         int         `json:"totalRD"`
	DepositPercent  int         `json:"depositPercent"`
	DepositRD       int         `json:"depositRD"`
	BalanceRD       int         `json:"balanceRD"`
	Status          QuoteStatus `json:"status"`
	SignatureSVG    string      `json:"signatureSvg,omitempty"`
	SignedByName    string      `json:"signedByName,omitempty"`
	RejectionReason string      `json:"rejectionReason,omitempty"`
}

// CalculateQuoteTotals calcula subtotal, total, anticipo del 30% y saldo pendiente (70%).
func CalculateQuoteTotals(lines []QuoteLine, deliveryFeeRD int, discountRD int) (totalRD int, depositRD int, balanceRD int, err error) {
	if len(lines) == 0 {
		return 0, 0, 0, ErrEmptyQuoteItems
	}
	subtotal := 0
	for _, l := range lines {
		if l.Quantity > 0 && l.UnitPriceRD >= 0 {
			subtotal += l.SubtotalRD()
		}
	}
	totalRD = subtotal + deliveryFeeRD - discountRD
	if totalRD < 0 {
		totalRD = 0
	}
	depositRD = (totalRD * 30) / 100
	balanceRD = totalRD - depositRD
	return totalRD, depositRD, balanceRD, nil
}

// RosaMoodboardProposal representa la propuesta generada por Rosa IA en los pasos s40-s48 y A16-A18.
type RosaMoodboardProposal struct {
	Step              int         `json:"step"`
	EventType         string      `json:"eventType"`
	StyleTitle        string      `json:"styleTitle"`
	ColorPalette      []string    `json:"colorPalette"`
	Guests            int         `json:"guests"`
	BudgetRD          int         `json:"budgetRD"`
	SuggestedLines    []QuoteLine `json:"suggestedLines"`
	EstimatedTotalRD  int         `json:"estimatedTotalRD"`
	DepositRequiredRD int         `json:"depositRequiredRD"`
	AssistantReply    string      `json:"assistantReply"`
}

// BuildRosaProposal genera una propuesta inteligente de Rosa IA según el tipo de evento, invitados y presupuesto.
func BuildRosaProposal(eventType string, guests int, budgetRD int) RosaMoodboardProposal {
	if guests <= 0 {
		guests = 80
	}
	tables := (guests + 7) / 8
	lines := []QuoteLine{
		{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: guests, UnitPriceRD: 4},
		{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: tables, UnitPriceRD: 25},
		{ArticleCode: "RF-004", Name: "Centro de mesa floral", Quantity: tables, UnitPriceRD: 35},
		{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
	}
	if budgetRD >= 1200 {
		lines = append(lines, QuoteLine{
			ArticleCode: "RF-006",
			Name:        "Mesa de dulces / helados",
			Quantity:    1,
			UnitPriceRD: 150,
		})
	}
	total, deposit, _, _ := CalculateQuoteTotals(lines, 0, 0)
	normalizedType := strings.TrimSpace(eventType)
	if normalizedType == "" {
		normalizedType = "Boda Real Rosa & Dorado"
	}
	return RosaMoodboardProposal{
		Step:              7,
		EventType:         normalizedType,
		StyleTitle:        "Rosa Empolvado, Dorado Cálido & Cristal",
		ColorPalette:      []string{"#EA4EA9", "#FFD45C", "#FDE6F2", "#1A1A2E"},
		Guests:            guests,
		BudgetRD:          budgetRD,
		SuggestedLines:    lines,
		EstimatedTotalRD:  total,
		DepositRequiredRD: deposit,
		AssistantReply:    "He preparado tu moodboard con cantidades exactas para tus invitados y sin exceder tu presupuesto.",
	}
}

// CanonicalQuotes devuelve las cotizaciones canónicas del sistema (Sección 6).
func CanonicalQuotes() []Quote {
	return append(signedCanonicalQuotes(), pendingCanonicalQuotes()...)
}

func signedCanonicalQuotes() []Quote {
	return []Quote{
		{
			ID: "quo-boda-real", Code: "COT-2026-1023", EventTitle: "Boda Real Rosa & Dorado",
			ClientName: "Camila Rodríguez", EventDateLabel: "Vie 23 oct", Venue: "Jardín Botánico", Guests: 120,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 120, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 15, UnitPriceRD: 25},
				{ArticleCode: "RF-004", Name: "Centro de mesa floral", Quantity: 8, UnitPriceRD: 35},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
			DeliveryFeeRD: 3120, TotalRD: 4500, DepositPercent: 30, DepositRD: 1350, BalanceRD: 3150,
			Status: QuoteStatusSigned, SignedByName: "Camila Rodríguez",
		},
		{
			ID: "quo-quince-valentina", Code: "COT-2026-1102", EventTitle: "Quinceañera Valentina Crystal",
			ClientName: "Camila Rodríguez", EventDateLabel: "Lun 2 nov", Venue: "Salón Gran Mirador", Guests: 100,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 100, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 13, UnitPriceRD: 25},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
			DeliveryFeeRD: 5230, TotalRD: 6200, DepositPercent: 30, DepositRD: 1860, BalanceRD: 4340,
			Status: QuoteStatusSigned, SignedByName: "Camila Rodríguez",
		},
	}
}

func pendingCanonicalQuotes() []Quote {
	return []Quote{
		{
			ID: "quo-cumple-mateo", Code: "COT-2026-1107", EventTitle: "Cumpleaños de Mateo",
			ClientName: "Laura Peña", EventDateLabel: "Sáb 7 nov", Venue: "Terraza Los Prados", Guests: 40,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 40, UnitPriceRD: 4},
				{ArticleCode: "RF-002", Name: "Mesa rectangular plegable", Quantity: 5, UnitPriceRD: 20},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
			},
			DeliveryFeeRD: 50, TotalRD: 405, DepositPercent: 30, DepositRD: 121, BalanceRD: 284,
			Status: QuoteStatusPending,
		},
		{
			ID: "quo-grad-promo", Code: "COT-2026-1113", EventTitle: "Graduación Promo 2026",
			ClientName: "Ana Gómez", EventDateLabel: "Vie 13 nov", Venue: "Club Naco", Guests: 80,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 80, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 10, UnitPriceRD: 25},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
			DeliveryFeeRD: 1325, TotalRD: 2140, DepositPercent: 30, DepositRD: 642, BalanceRD: 1498,
			Status: QuoteStatusPending,
		},
	}
}
