package pdf

import (
	"bytes"
	"fmt"
	"time"

	"github.com/go-pdf/fpdf"
)

// QuoteData holds all the information needed to generate a quote PDF.
type QuoteData struct {
	EventName     string
	EventDate    string
	Location     string
	ClientName   string
	ClientEmail  string
	ClientPhone  string
	Items        []QuoteItem
	Subtotal     float64
	AdditionalCosts float64
	Total        float64
	PaymentMethod string
	AdminNotes   string
	QuoteNumber  string
	GeneratedAt  time.Time
}

type QuoteItem struct {
	Name         string
	Quantity     int
	UnitPrice    float64
	TotalPrice   float64
	Category     string
}

// GenerateQuotePDF creates a professional quote PDF and returns the PDF bytes.
func GenerateQuotePDF(data QuoteData) ([]byte, error) {
	pdf := fpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(15, 15, 15)
	pdf.AddPage()

	// Header - RosaFiesta branding
	pdf.SetFont("Helvetica", "B", 24)
	pdf.SetTextColor(255, 60, 172) // hotPink
	pdf.CellFormat(0, 12, "RosaFiesta", "", 0, "L", false, 0, "")
	pdf.Ln(6)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(100, 100, 100)
	pdf.CellFormat(0, 5, "Decoración y Ambientación para Eventos", "", 0, "L", false, 0, "")
	pdf.Ln(5)
	pdf.CellFormat(0, 5, "República Dominicana", "", 0, "L", false, 0, "")
	pdf.Ln(12)

	// Divider line
	pdf.SetDrawColor(255, 60, 172)
	pdf.SetLineWidth(0.8)
	pdf.Line(15, pdf.GetY(), 195, pdf.GetY())
	pdf.Ln(8)

	// Quote title
	pdf.SetFont("Helvetica", "B", 18)
	pdf.SetTextColor(40, 40, 40)
	pdf.CellFormat(0, 10, "Cotización", "", 0, "L", false, 0, "")
	pdf.Ln(12)

	// Quote meta info (right side)
	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(80, 80, 80)

	// Quote number and date on the right
	pdf.SetX(130)
	pdf.CellFormat(65, 6, fmt.Sprintf("No. %s", data.QuoteNumber), "", 0, "R", false, 0, "")
	pdf.Ln(5)
	pdf.SetX(130)
	pdf.CellFormat(65, 6, fmt.Sprintf("Fecha: %s", data.GeneratedAt.Format("02/01/2006")), "", 0, "R", false, 0, "")
	pdf.Ln(8)

	// Client info section
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "Información del Cliente", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(40, 40, 40)
	pdf.CellFormat(0, 6, fmt.Sprintf("Cliente: %s", data.ClientName), "", 0, "L", false, 0, "")
	pdf.Ln(5)
	pdf.CellFormat(0, 6, fmt.Sprintf("Email: %s", data.ClientEmail), "", 0, "L", false, 0, "")
	if data.ClientPhone != "" {
		pdf.Ln(5)
		pdf.CellFormat(0, 6, fmt.Sprintf("Teléfono: %s", data.ClientPhone), "", 0, "L", false, 0, "")
	}
	pdf.Ln(8)

	// Event info section
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "Detalles del Evento", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(40, 40, 40)
	pdf.CellFormat(0, 6, fmt.Sprintf("Evento: %s", data.EventName), "", 0, "L", false, 0, "")
	pdf.Ln(5)
	pdf.CellFormat(0, 6, fmt.Sprintf("Fecha: %s", data.EventDate), "", 0, "L", false, 0, "")
	pdf.Ln(5)
	pdf.CellFormat(0, 6, fmt.Sprintf("Lugar: %s", data.Location), "", 0, "L", false, 0, "")
	pdf.Ln(12)

	// Table header
	pdf.SetFont("Helvetica", "B", 9)
	pdf.SetFillColor(255, 60, 172)
	pdf.SetTextColor(255, 255, 255)
	pdf.CellFormat(80, 8, "Artículo", "TB", 0, "L", true, 0, "")
	pdf.CellFormat(25, 8, "Cantidad", "TB", 0, "C", true, 0, "")
	pdf.CellFormat(35, 8, "Precio Unit.", "TB", 0, "C", true, 0, "")
	pdf.CellFormat(35, 8, "Total", "TB", 0, "C", true, 0, "")
	pdf.Ln(8)

	// Table rows
	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(40, 40, 40)
	pdf.SetFillColor(248, 248, 248)

	for i, item := range data.Items {
		// Alternate row background
		if i%2 == 0 {
			pdf.SetFillColor(248, 248, 248)
		} else {
			pdf.SetFillColor(255, 255, 255)
		}

		pdf.CellFormat(80, 7, truncateString(item.Name, 40), "B", 0, "L", true, 0, "")
		pdf.CellFormat(25, 7, fmt.Sprintf("%d", item.Quantity), "B", 0, "C", true, 0, "")
		pdf.CellFormat(35, 7, formatCurrency(item.UnitPrice), "B", 0, "C", true, 0, "")
		pdf.CellFormat(35, 7, formatCurrency(item.TotalPrice), "B", 0, "C", true, 0, "")
		pdf.Ln(7)
	}

	// Totals section
	pdf.Ln(4)
	pdf.SetX(115)
	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(80, 80, 80)
	pdf.CellFormat(40, 7, "Subtotal:", "", 0, "R", false, 0, "")
	pdf.CellFormat(35, 7, formatCurrency(data.Subtotal), "", 0, "R", false, 0, "")
	pdf.Ln(7)

	if data.AdditionalCosts > 0 {
		pdf.SetX(115)
		pdf.CellFormat(40, 7, "Costos adicionales:", "", 0, "R", false, 0, "")
		pdf.CellFormat(35, 7, formatCurrency(data.AdditionalCosts), "", 0, "R", false, 0, "")
		pdf.Ln(7)
	}

	pdf.SetX(115)
	pdf.SetFont("Helvetica", "B", 12)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(40, 9, "TOTAL:", "T", 0, "R", false, 0, "")
	pdf.SetTextColor(40, 40, 40)
	pdf.CellFormat(35, 9, formatCurrency(data.Total), "T", 0, "R", false, 0, "")
	pdf.Ln(15)

	// Admin notes
	if data.AdminNotes != "" {
		pdf.SetFont("Helvetica", "B", 10)
		pdf.SetTextColor(255, 60, 172)
		pdf.CellFormat(0, 6, "Notas del Administrador", "", 0, "L", false, 0, "")
		pdf.Ln(6)
		pdf.SetFont("Helvetica", "", 9)
		pdf.SetTextColor(80, 80, 80)
		pdf.MultiCell(0, 5, data.AdminNotes, "", "L", false)
		pdf.Ln(8)
	}

	// Payment instructions
	pdf.SetFont("Helvetica", "B", 10)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 6, "Instrucciones de Pago", "", 0, "L", false, 0, "")
	pdf.Ln(6)
	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(80, 80, 80)

	switch data.PaymentMethod {
	case "transferencia":
		pdf.MultiCell(0, 5,
			"Transferencia bancaria a nombre de RosaFiesta.\n"+
				"Banco Popular: Cuenta de Ahorros No. XXXX-XXXX-XXXX\n"+
				"Banreservas: Cuenta de Ahorros No. YYYY-YYYY-YYYY\n"+
				"Enviar comprobante de pago al correo administracion@rosafiesta.com", "", "L", false)
	case "efectivo":
		pdf.MultiCell(0, 5,
			"Pago en efectivo en nuestras oficinas.\n"+
				"Dirección: [Dirección de RosaFiesta]\n"+
				"Horario: Lunes a Viernes 9:00 AM - 5:00 PM", "", "L", false)
	case "tarjeta":
		pdf.MultiCell(0, 5,
			"Pago con tarjeta de crédito/débito disponible en nuestras oficinas.\n"+
				"Se aplica un cargo adicional del 3% por procesamiento.", "", "L", false)
	default:
		pdf.MultiCell(0, 5, "El cliente será contactado para coordinar el método de pago.", "", "L", false)
	}

	pdf.Ln(10)

	// Footer
	pdf.SetDrawColor(200, 200, 200)
	pdf.SetLineWidth(0.3)
	pdf.Line(15, pdf.GetY(), 195, pdf.GetY())
	pdf.Ln(5)
	pdf.SetFont("Helvetica", "I", 8)
	pdf.SetTextColor(150, 150, 150)
	pdf.CellFormat(0, 5, "Esta cotización es válida por 15 días. Los precios pueden variar sin previo aviso.", "", 0, "C", false, 0, "")
	pdf.Ln(4)
	pdf.CellFormat(0, 5, fmt.Sprintf("Generado por RosaFiesta el %s", data.GeneratedAt.Format("02/01/2006 3:04 PM")), "", 0, "C", false, 0, "")

	var buf bytes.Buffer
	pdf.Output(&buf)
	return buf.Bytes(), nil
}

func formatCurrency(amount float64) string {
	return fmt.Sprintf("RD$ %.2f", amount)
}

func truncateString(s string, maxLen int) string {
	if len(s) <= maxLen {
		return s
	}
	return s[:maxLen-3] + "..."
}
