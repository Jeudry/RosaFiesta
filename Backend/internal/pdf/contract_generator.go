package pdf

import (
	"bytes"
	"fmt"
	"time"

	"github.com/go-pdf/fpdf"
)

// ContractData holds all the information needed to generate a contract PDF.
type ContractData struct {
	EventName       string
	EventDate       string
	EventLocation   string
	EventType       string
	ClientName      string
	ClientEmail     string
	ClientPhone     string
	Items           []ContractItem
	Subtotal        float64
	DeliveryFee     float64
	AdditionalCosts float64
	Total           float64
	DepositPaid     float64
	RemainingAmount float64
	DueDate         string
	PaymentMethod   string
	GeneratedAt     time.Time
}

type ContractItem struct {
	Name       string
	Quantity   int
	UnitPrice  float64
	TotalPrice float64
}

// GenerateContract creates a formal contract PDF and returns the PDF bytes.
func GenerateContract(data ContractData) ([]byte, error) {
	pdf := fpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(20, 20, 20)
	pdf.AddPage()

	// Header
	pdf.SetFont("Helvetica", "B", 22)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 10, "RosaFiesta", "", 0, "L", false, 0, "")
	pdf.Ln(6)

	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(100, 100, 100)
	pdf.CellFormat(0, 4, "Decoración y Ambientación para Eventos", "", 0, "L", false, 0, "")
	pdf.Ln(4)
	pdf.CellFormat(0, 4, "San Cristóbal, República Dominicana", "", 0, "L", false, 0, "")
	pdf.Ln(10)

	// Divider
	pdf.SetDrawColor(255, 60, 172)
	pdf.SetLineWidth(0.8)
	pdf.Line(20, pdf.GetY(), 190, pdf.GetY())
	pdf.Ln(8)

	// Title
	pdf.SetFont("Helvetica", "B", 16)
	pdf.SetTextColor(40, 40, 40)
	pdf.CellFormat(0, 8, "CONTRATO DE SERVICIOS", "", 0, "C", false, 0, "")
	pdf.Ln(8)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(80, 80, 80)
	pdf.CellFormat(0, 5, "Número de contrato: " + generateContractNumber(data.GeneratedAt), "", 0, "C", false, 0, "")
	pdf.Ln(5)
	pdf.CellFormat(0, 5, fmt.Sprintf("Fecha: %s, San Cristóbal, RD", data.GeneratedAt.Format("02 de enero de 2006")), "", 0, "C", false, 0, "")
	pdf.Ln(12)

	// === SECTION 1: PARTIES ===
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "1. PARTES CONTRATANTES", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(40, 40, 40)

	pdf.MultiCell(0, 5, "El presente contrato se celebra entre RosaFiesta (el \"Proveedor\") y el cliente identificado abajo (el \"Cliente\").", "", "L", false)
	pdf.Ln(4)

	pdf.SetFont("Helvetica", "B", 10)
	pdf.CellFormat(0, 6, "PROVEEDOR:", "", 0, "L", false, 0, "")
	pdf.Ln(6)
	pdf.SetFont("Helvetica", "", 10)
	pdf.MultiCell(0, 5, "RosaFiesta\nDecoración y Ambientación para Eventos\nSan Cristóbal, República Dominicana", "", "L", false)
	pdf.Ln(4)

	pdf.SetFont("Helvetica", "B", 10)
	pdf.CellFormat(0, 6, "CLIENTE:", "", 0, "L", false, 0, "")
	pdf.Ln(6)
	pdf.SetFont("Helvetica", "", 10)
	pdf.MultiCell(0, 5, fmt.Sprintf("Nombre: %s\nEmail: %s\nTeléfono: %s", data.ClientName, data.ClientEmail, data.ClientPhone), "", "L", false)
	pdf.Ln(10)

	// === SECTION 2: EVENT DETAILS ===
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "2. DETALLES DEL EVENTO", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(40, 40, 40)
	pdf.CellFormat(0, 6, fmt.Sprintf("Nombre del evento: %s", data.EventName), "", 0, "L", false, 0, "")
	pdf.Ln(5)
	pdf.CellFormat(0, 6, fmt.Sprintf("Fecha: %s", data.EventDate), "", 0, "L", false, 0, "")
	pdf.Ln(5)
	pdf.CellFormat(0, 6, fmt.Sprintf("Lugar: %s", data.EventLocation), "", 0, "L", false, 0, "")
	if data.EventType != "" {
		pdf.Ln(5)
		pdf.CellFormat(0, 6, fmt.Sprintf("Tipo de evento: %s", data.EventType), "", 0, "L", false, 0, "")
	}
	pdf.Ln(10)

	// === SECTION 3: ITEMS ===
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "3. ARTÍCULOS Y SERVICIOS", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	// Table header
	pdf.SetFont("Helvetica", "B", 9)
	pdf.SetFillColor(255, 60, 172)
	pdf.SetTextColor(255, 255, 255)
	pdf.CellFormat(85, 8, "Descripción", "TB", 0, "L", true, 0, "")
	pdf.CellFormat(25, 8, "Cantidad", "TB", 0, "C", true, 0, "")
	pdf.CellFormat(35, 8, "Precio Unit.", "TB", 0, "C", true, 0, "")
	pdf.CellFormat(35, 8, "Subtotal", "TB", 0, "C", true, 0, "")
	pdf.Ln(8)

	// Table rows
	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(40, 40, 40)

	for i, item := range data.Items {
		if i%2 == 0 {
			pdf.SetFillColor(248, 248, 248)
		} else {
			pdf.SetFillColor(255, 255, 255)
		}

		pdf.CellFormat(85, 7, truncateString(item.Name, 45), "B", 0, "L", true, 0, "")
		pdf.CellFormat(25, 7, fmt.Sprintf("%d", item.Quantity), "B", 0, "C", true, 0, "")
		pdf.CellFormat(35, 7, formatCurrency(item.UnitPrice), "B", 0, "C", true, 0, "")
		pdf.CellFormat(35, 7, formatCurrency(item.TotalPrice), "B", 0, "C", true, 0, "")
		pdf.Ln(7)
	}

	// Totals
	pdf.Ln(4)
	pdf.SetX(110)
	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(80, 80, 80)
	pdf.CellFormat(40, 7, "Subtotal:", "", 0, "R", false, 0, "")
	pdf.CellFormat(35, 7, formatCurrency(data.Subtotal), "", 0, "R", false, 0, "")
	pdf.Ln(7)

	if data.DeliveryFee > 0 {
		pdf.SetX(110)
		pdf.CellFormat(40, 7, "Envío:", "", 0, "R", false, 0, "")
		pdf.CellFormat(35, 7, formatCurrency(data.DeliveryFee), "", 0, "R", false, 0, "")
		pdf.Ln(7)
	}

	if data.AdditionalCosts > 0 {
		pdf.SetX(110)
		pdf.CellFormat(40, 7, "Costos adicionales:", "", 0, "R", false, 0, "")
		pdf.CellFormat(35, 7, formatCurrency(data.AdditionalCosts), "", 0, "R", false, 0, "")
		pdf.Ln(7)
	}

	pdf.SetX(110)
	pdf.SetFont("Helvetica", "B", 12)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(40, 9, "TOTAL:", "T", 0, "R", false, 0, "")
	pdf.SetTextColor(40, 40, 40)
	pdf.CellFormat(35, 9, formatCurrency(data.Total), "T", 0, "R", false, 0, "")
	pdf.Ln(12)

	// === SECTION 4: PAYMENT ===
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "4. CONDICIONES DE PAGO", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(40, 40, 40)
	pdf.MultiCell(0, 5, fmt.Sprintf("Monto total del contrato: %s", formatCurrency(data.Total)), "", "L", false)
	pdf.Ln(3)
	pdf.MultiCell(0, 5, fmt.Sprintf("Anticipo pagado: %s", formatCurrency(data.DepositPaid)), "", "L", false)
	pdf.Ln(3)
	pdf.MultiCell(0, 5, fmt.Sprintf("Monto restante: %s", formatCurrency(data.RemainingAmount)), "", "L", false)
	pdf.Ln(3)
	pdf.MultiCell(0, 5, fmt.Sprintf("Fecha límite de pago: %s", data.DueDate), "", "L", false)
	pdf.Ln(3)
	pdf.MultiCell(0, 5, fmt.Sprintf("Método de pago: %s", data.PaymentMethod), "", "L", false)
	pdf.Ln(10)

	// === SECTION 5: SERVICES INCLUDED ===
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "5. SERVICIOS INCLUIDOS", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(40, 40, 40)
	pdf.MultiCell(0, 5, "Los siguientes servicios están incluidos en el presente contrato:", "", "L", false)
	pdf.Ln(3)
	services := []string{
		"- Montaje de equipos y decoración en el lugar del evento.",
		"- Desmontaje y retiro de equipos después del evento.",
		"- Transporte de equipos hacia y desde el lugar del evento.",
		"- Asesoría básica para la disposición de equipos.",
	}
	for _, s := range services {
		pdf.CellFormat(0, 5, s, "", 0, "L", false, 0, "")
		pdf.Ln(5)
	}
	pdf.Ln(4)

	// === SECTION 6: TERMS ===
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "6. TÉRMINOS Y CONDICIONES", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 9)
	pdf.SetTextColor(40, 40, 40)

	terms := []string{
		"6.1 El cliente se compromete a pagar el monto total acordado en este contrato antes de la fecha límite indicada.",
		"6.2 La devolución de equipos debe hacerse en las mismas condiciones en que fueron entregados. Cualquier daño será responsabilidad del cliente.",
		"6.3 RosaFiesta no se hace responsable por daños causados por terceros, fuerza mayor o circunstancias imprevistas.",
		"6.4 El montaje se realizará el día anterior al evento salvo que se acuerde lo contrario entre ambas partes.",
		"6.5 Cualquier modificación al contrato debe ser acordada por ambas partes por escrito.",
		"6.6 El cliente autoriza el uso de fotografías del evento para fines promocionales de RosaFiesta, salvo que se indique lo contrario.",
		"6.7 En caso de cancelación, se aplicarán cargos según las políticas de cancelación de RosaFiesta.",
	}

	for _, term := range terms {
		pdf.MultiCell(0, 5, term, "", "L", false)
		pdf.Ln(2)
	}
	pdf.Ln(6)

	// === SECTION 7: SIGNATURES ===
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(255, 60, 172)
	pdf.CellFormat(0, 7, "7. FIRMAS", "", 0, "L", false, 0, "")
	pdf.Ln(7)

	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(40, 40, 40)
	pdf.MultiCell(0, 5, "Ambas partes firman el presente contrato en señal de aceptación de todos los términos y condiciones aquí descritos.", "", "L", false)
	pdf.Ln(10)

	// Signature lines
	pdf.SetFont("Helvetica", "", 10)
	pdf.SetTextColor(80, 80, 80)

	// Client signature
	pdf.CellFormat(80, 6, "Cliente: ____________________________", "", 0, "L", false, 0, "")
	pdf.CellFormat(0, 6, "RosaFiesta: ____________________________", "", 0, "L", false, 0, "")
	pdf.Ln(6)
	pdf.CellFormat(80, 6, fmt.Sprintf("Fecha: %s", data.GeneratedAt.Format("02/01/2006")), "", 0, "L", false, 0, "")
	pdf.CellFormat(0, 6, fmt.Sprintf("Fecha: %s", data.GeneratedAt.Format("02/01/2006")), "", 0, "L", false, 0, "")
	pdf.Ln(12)

	// Footer
	pdf.SetDrawColor(200, 200, 200)
	pdf.SetLineWidth(0.3)
	pdf.Line(20, pdf.GetY(), 190, pdf.GetY())
	pdf.Ln(5)

	pdf.SetFont("Helvetica", "I", 8)
	pdf.SetTextColor(150, 150, 150)
	pdf.CellFormat(0, 5, "Este contrato es un documento oficial de RosaFiesta. Conserve una copia para sus registros.", "", 0, "C", false, 0, "")
	pdf.Ln(4)
	pdf.CellFormat(0, 5, fmt.Sprintf("Generado por RosaFiesta el %s", data.GeneratedAt.Format("02/01/2006 3:04 PM")), "", 0, "C", false, 0, "")

	var buf bytes.Buffer
	pdf.Output(&buf)
	return buf.Bytes(), nil
}

func generateContractNumber(t time.Time) string {
	return fmt.Sprintf("RF-CON-%d%02d%02d-%04d", t.Year(), t.Month(), t.Day(), t.Hour()*60+t.Minute())
}