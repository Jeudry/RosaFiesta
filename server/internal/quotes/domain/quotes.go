package domain

import (
	"errors"
	"strings"
)

var (
	ErrQuoteNotFound       = errors.New("quote not found")
	ErrQuoteForbidden      = errors.New("caller is not allowed to access this quote")
	ErrEmptyQuoteItems     = errors.New("quote must include at least one line item")
	ErrInvalidSignature    = errors.New("digital signature and signer name are required")
	ErrQuoteAlreadyDecided = errors.New("quote has already been processed")
	ErrInvalidAudioPayload = errors.New("invalid or empty base64 audio payload")
)

type QuoteStatus string

const (
	QuoteStatusPending  QuoteStatus = "pending"
	QuoteStatusSent     QuoteStatus = "sent"
	QuoteStatusApproved QuoteStatus = "approved"
	QuoteStatusSigned   QuoteStatus = "signed"
	QuoteStatusDeclined QuoteStatus = "declined"
)

type QuoteLine struct {
	ArticleCode string `json:"articleCode"`
	Name        string `json:"name"`
	Quantity    int    `json:"quantity"`
	UnitPriceRD int    `json:"unitPriceRD"`
}

func (l QuoteLine) SubtotalRD() int {
	return l.Quantity * l.UnitPriceRD
}

type Quote struct {
	ID              string      `json:"id"`
	Code            string      `json:"code"`
	EventID         string      `json:"eventId,omitempty"`
	ClientID        string      `json:"clientId,omitempty"`
	ClientEmail     string      `json:"clientEmail,omitempty"`
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

func (q Quote) BelongsToClient(userID, email, fullName string) bool {
	trimmedUserID := strings.TrimSpace(userID)
	if trimmedUserID != "" {
		if q.ClientID == trimmedUserID {
			return true
		}
	}
	trimmedEmail := strings.TrimSpace(email)
	if trimmedEmail != "" {
		if strings.EqualFold(q.ClientEmail, trimmedEmail) {
			return true
		}
	}
	trimmedName := strings.TrimSpace(fullName)
	if trimmedName != "" {
		if strings.EqualFold(q.ClientName, trimmedName) {
			return true
		}
	}
	return false
}

func CalculateQuoteTotals(
	lines []QuoteLine,
	deliveryFeeRD int,
	discountRD int,
) (totalRD int, depositRD int, balanceRD int, err error) {
	if len(lines) == 0 {
		return 0, 0, 0, ErrEmptyQuoteItems
	}
	subtotal := 0
	for _, line := range lines {
		isPositiveQty := line.Quantity > 0
		if isPositiveQty {
			if line.UnitPriceRD >= 0 {
				subtotal += line.SubtotalRD()
			}
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

func CanonicalQuotes() []Quote {
	signed := signedCanonicalQuotes()
	pending := pendingCanonicalQuotes()
	return append(signed, pending...)
}

func signedCanonicalQuotes() []Quote {
	camilaID := "11111111-1111-1111-1111-111111111111"
	return []Quote{
		{
			ID:             "quo-boda-real",
			Code:           "COT-2026-1023",
			EventID:        "evt-boda-real",
			ClientID:       camilaID,
			ClientEmail:    "camila@rosafiesta.do",
			EventTitle:     "Boda Real Rosa & Dorado",
			ClientName:     "Camila Rodríguez",
			EventDateLabel: "Vie 23 oct",
			Venue:          "Jardín Botánico",
			Guests:         120,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 120, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 15, UnitPriceRD: 25},
				{ArticleCode: "RF-004", Name: "Centro de mesa floral", Quantity: 8, UnitPriceRD: 35},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
			DeliveryFeeRD:  3120,
			TotalRD:        4500,
			DepositPercent: 30,
			DepositRD:      1350,
			BalanceRD:      3150,
			Status:         QuoteStatusSigned,
			SignedByName:   "Camila Rodríguez",
		},
		{
			ID:             "quo-quince-valentina",
			Code:           "COT-2026-1102",
			EventID:        "evt-quince-valentina",
			ClientID:       camilaID,
			ClientEmail:    "camila@rosafiesta.do",
			EventTitle:     "Quinceañera Valentina Crystal",
			ClientName:     "Camila Rodríguez",
			EventDateLabel: "Lun 2 nov",
			Venue:          "Salón Gran Mirador",
			Guests:         100,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 100, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 13, UnitPriceRD: 25},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
			DeliveryFeeRD:  5230,
			TotalRD:        6200,
			DepositPercent: 30,
			DepositRD:      1860,
			BalanceRD:      4340,
			Status:         QuoteStatusSigned,
			SignedByName:   "Camila Rodríguez",
		},
	}
}

func pendingCanonicalQuotes() []Quote {
	return []Quote{
		{
			ID:             "quo-cumple-mateo",
			Code:           "COT-2026-1107",
			EventID:        "evt-cumple-mateo",
			ClientEmail:    "laura@rosafiesta.do",
			EventTitle:     "Cumpleaños de Mateo",
			ClientName:     "Laura Peña",
			EventDateLabel: "Sáb 7 nov",
			Venue:          "Terraza Los Prados",
			Guests:         40,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 40, UnitPriceRD: 4},
				{ArticleCode: "RF-002", Name: "Mesa rectangular plegable", Quantity: 5, UnitPriceRD: 20},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
			},
			DeliveryFeeRD:  50,
			TotalRD:        405,
			DepositPercent: 30,
			DepositRD:      121,
			BalanceRD:      284,
			Status:         QuoteStatusPending,
		},
		{
			ID:             "quo-grad-promo",
			Code:           "COT-2026-1113",
			EventID:        "evt-grad-promo",
			ClientEmail:    "ana@rosafiesta.do",
			EventTitle:     "Graduación Promo 2026",
			ClientName:     "Ana Gómez",
			EventDateLabel: "Vie 13 nov",
			Venue:          "Club Naco",
			Guests:         80,
			Lines: []QuoteLine{
				{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 80, UnitPriceRD: 4},
				{ArticleCode: "RF-003", Name: "Mesa redonda 8 personas", Quantity: 10, UnitPriceRD: 25},
				{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
				{ArticleCode: "RF-006", Name: "Mesa de dulces / helados", Quantity: 1, UnitPriceRD: 150},
			},
			DeliveryFeeRD:  1325,
			TotalRD:        2140,
			DepositPercent: 30,
			DepositRD:      642,
			BalanceRD:      1498,
			Status:         QuoteStatusPending,
		},
	}
}
