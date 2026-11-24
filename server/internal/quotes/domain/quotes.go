// Package domain modela las cotizaciones, contratos digitales y análisis de asistentes de RosaFiesta.
package domain

import (
	"bytes"
	"errors"
	"fmt"
	"strings"
)

var (
	// ErrQuoteNotFound indica que la cotización solicitada no existe.
	ErrQuoteNotFound = errors.New("quote not found")
	// ErrQuoteForbidden indica que el llamador no tiene acceso a la cotización.
	ErrQuoteForbidden = errors.New("caller is not allowed to access this quote")
	// ErrEmptyQuoteItems exige al menos una línea de artículos en la cotización.
	ErrEmptyQuoteItems = errors.New("quote must include at least one line item")
	// ErrInvalidSignature exige firma digital y nombre del firmante al aprobar contrato.
	ErrInvalidSignature = errors.New("digital signature and signer name are required")
	// ErrQuoteAlreadyDecided impide reprocesar una cotización ya firmada o rechazada.
	ErrQuoteAlreadyDecided = errors.New("quote has already been processed")
	// ErrQuoteNotSent exige que la cotización esté enviada por administración antes de firmarse.
	ErrQuoteNotSent = errors.New("quote must be sent before contract signature")
	// ErrInvalidQuoteItem rechaza artículos inexistentes o cantidades inválidas en cotizaciones de cliente.
	ErrInvalidQuoteItem = errors.New("quote line references an unknown or invalid catalog item")
	// ErrInvalidAudioPayload indica audio base64 vacío o inválido.
	ErrInvalidAudioPayload = errors.New("invalid or empty base64 audio payload")
	// ErrInsufficientStock indica inventario insuficiente en la fecha solicitada.
	ErrInsufficientStock = errors.New("stock insuficiente para la fecha solicitada")
	// ErrAIProviderNotConfigured indica que no existe clave de API configurada para el asistente Rosa.
	ErrAIProviderNotConfigured = errors.New("el proveedor de IA no está configurado")
	// ErrSTTProviderNotConfigured indica que no existe proveedor real de transcripción configurado.
	ErrSTTProviderNotConfigured = errors.New("el proveedor de transcripción de voz no está configurado")
	// ErrAudioTooLong rechaza notas de voz que exceden el límite de 25 segundos.
	ErrAudioTooLong = errors.New("el audio excede la duración máxima permitida de 25 segundos")
	// ErrAIRateLimited indica que el llamador alcanzó el límite de solicitudes por ventana temporal.
	ErrAIRateLimited = errors.New("límite temporal de consultas a Rosa alcanzado")
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
	EventDateISO    string      `json:"eventDateIso,omitempty"`
	PackageID       string      `json:"packageId,omitempty"`
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
	Notes           string      `json:"notes,omitempty"`
	SignatureSVG    string      `json:"signatureSvg,omitempty"`
	SignedByName    string      `json:"signedByName,omitempty"`
	SignedAtISO     string      `json:"signedAtIso,omitempty"`
	ContractPDFURL  string      `json:"contractPdfUrl,omitempty"`
	RejectionReason string      `json:"rejectionReason,omitempty"`
}

func (q Quote) BelongsToClient(userID, email, _ string) bool {
	storedClientID := strings.TrimSpace(q.ClientID)
	if storedClientID != "" {
		trimmedUserID := strings.TrimSpace(userID)
		if trimmedUserID == "" {
			return false
		}
		return storedClientID == trimmedUserID
	}
	trimmedEmail := strings.TrimSpace(email)
	if trimmedEmail == "" {
		return false
	}
	return strings.EqualFold(q.ClientEmail, trimmedEmail)
}

func CalculateQuoteTotals(
	lines []QuoteLine,
	deliveryFeeRD int,
	discountRD int,
) (totalRD int, depositRD int, balanceRD int, err error) {
	return CalculateQuoteTotalsWithDeposit(lines, deliveryFeeRD, discountRD, 30)
}

// CalculateQuoteTotalsWithDeposit calcula total, anticipo configurable y saldo pendiente.
func CalculateQuoteTotalsWithDeposit(
	lines []QuoteLine,
	deliveryFeeRD int,
	discountRD int,
	depositPercent int,
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
	pct := depositPercent
	if pct <= 0 {
		pct = 30
	}
	if pct > 100 {
		pct = 30
	}
	depositRD = (totalRD * pct) / 100
	balanceRD = totalRD - depositRD
	return totalRD, depositRD, balanceRD, nil
}

// BuildQuoteContractPDF genera un documento PDF 1.4 válido con el detalle del contrato y firma.
func BuildQuoteContractPDF(quote Quote) []byte {
	lines := buildContractLines(quote)
	var stream bytes.Buffer
	stream.WriteString("BT\n/F1 11 Tf\n48 750 Td\n15 TL\n")
	for idx, line := range lines {
		escaped := escapePDFText(line)
		if idx == 0 {
			fmt.Fprintf(&stream, "(%s) Tj\n", escaped)
		} else {
			fmt.Fprintf(&stream, "T* (%s) Tj\n", escaped)
		}
	}
	stream.WriteString("ET\n")
	streamBytes := stream.Bytes()

	objects := []string{
		"<< /Type /Catalog /Pages 2 0 R >>",
		"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
		"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
		fmt.Sprintf("<< /Length %d >>\nstream\n%sendstream", len(streamBytes), streamBytes),
		"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
	}

	var out bytes.Buffer
	out.WriteString("%PDF-1.4\n")
	offsets := make([]int, len(objects)+1)
	for i, obj := range objects {
		offsets[i+1] = out.Len()
		fmt.Fprintf(&out, "%d 0 obj\n%s\nendobj\n", i+1, obj)
	}
	xrefOffset := out.Len()
	fmt.Fprintf(&out, "xref\n0 %d\n0000000000 65535 f \n", len(objects)+1)
	for i := 1; i <= len(objects); i++ {
		fmt.Fprintf(&out, "%010d 00000 n \n", offsets[i])
	}
	fmt.Fprintf(&out, "trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n", len(objects)+1, xrefOffset)
	return out.Bytes()
}

func buildContractLines(quote Quote) []string {
	statusLabel := string(quote.Status)
	signerLabel := strings.TrimSpace(quote.SignedByName)
	if signerLabel == "" {
		signerLabel = "Pendiente de firma"
	}
	lines := []string{
		"ROSAFIESTA SRL - CONTRATO Y COTIZACION",
		fmt.Sprintf("Codigo: %s | Estado: %s", quote.Code, statusLabel),
		fmt.Sprintf("Evento: %s", quote.EventTitle),
		fmt.Sprintf("Cliente: %s (%s)", quote.ClientName, quote.ClientEmail),
		fmt.Sprintf("Fecha: %s | Lugar: %s | Invitados: %d", quote.EventDateLabel, quote.Venue, quote.Guests),
		"------------------------------------------------------------",
		"Detalle de articulos:",
	}
	for _, item := range quote.Lines {
		lines = append(lines, fmt.Sprintf("- %s x%d @ RD$%d = RD$%d", item.Name, item.Quantity, item.UnitPriceRD, item.SubtotalRD()))
	}
	lines = append(lines,
		"------------------------------------------------------------",
		fmt.Sprintf("Transporte: RD$%d | Descuento: RD$%d", quote.DeliveryFeeRD, quote.DiscountRD),
		fmt.Sprintf("Total: RD$%d | Anticipo (%d%%): RD$%d | Saldo: RD$%d", quote.TotalRD, quote.DepositPercent, quote.DepositRD, quote.BalanceRD),
		fmt.Sprintf("Firmado por: %s | Fecha firma: %s", signerLabel, quote.SignedAtISO),
	)
	return lines
}

func escapePDFText(raw string) string {
	var b strings.Builder
	for _, r := range raw {
		switch r {
		case '\\', '(', ')':
			b.WriteByte('\\')
			b.WriteRune(r)
		default:
			if isPrintableASCII(r) {
				b.WriteRune(r)
			} else {
				b.WriteByte(' ')
			}
		}
	}
	return b.String()
}

func isPrintableASCII(r rune) bool {
	if r < 32 {
		return false
	}
	return r <= 126
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
