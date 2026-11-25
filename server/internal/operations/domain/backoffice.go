package domain

import (
	"fmt"
	"math"
	"strings"
	"time"
)

type Review struct {
	ID             string `json:"id"`
	EventID        string `json:"eventId"`
	ClientName     string `json:"clientName"`
	Rating         int    `json:"rating"`
	Comment        string `json:"comment"`
	AdminReply     string `json:"adminReply,omitempty"`
	Verified       bool   `json:"verified"`
	CreatedAtLabel string `json:"createdAtLabel,omitempty"`
}

type Supplier struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	Specialty   string `json:"specialty"`
	ContactName string `json:"contactName"`
	Phone       string `json:"phone"`
	Email       string `json:"email"`
	Website     string `json:"website,omitempty"`
	Rating      string `json:"rating"`
	Notes       string `json:"notes,omitempty"`
}

type Lead struct {
	ID             string `json:"id"`
	FullName       string `json:"fullName"`
	Phone          string `json:"phone"`
	Email          string `json:"email,omitempty"`
	EventType      string `json:"eventType"`
	EventDateLabel string `json:"eventDateLabel,omitempty"`
	Guests         int    `json:"guests"`
	EstimatedDop   int    `json:"estimatedDop"`
	Stage          string `json:"stage"`
	Notes          string `json:"notes,omitempty"`
}

type BusinessSettings struct {
	ID                        string `json:"id"`
	CompanyName               string `json:"companyName"`
	RNC                       string `json:"rnc"`
	Address                   string `json:"address"`
	Phone                     string `json:"phone"`
	WhatsApp                  string `json:"whatsapp"`
	Email                     string `json:"email"`
	Instagram                 string `json:"instagram"`
	Facebook                  string `json:"facebook"`
	BusinessHours             string `json:"businessHours"`
	ItbisPercent              int    `json:"itbisPercent"`
	DepositPercent            int    `json:"depositPercent"`
	WhatsAppAutoAlertsEnabled bool   `json:"whatsappAutoAlertsEnabled"`
}

type DeliveryZone struct {
	ID         string `json:"id"`
	Name       string `json:"name"`
	FeeDop     int    `json:"feeDop"`
	EtaMinutes int    `json:"etaMinutes"`
	Active     bool   `json:"active"`
}

type PublicBusinessSettings struct {
	CompanyName    string         `json:"companyName"`
	RNC            string         `json:"rnc"`
	Address        string         `json:"address"`
	Phone          string         `json:"phone"`
	WhatsApp       string         `json:"whatsapp"`
	Email          string         `json:"email"`
	Instagram      string         `json:"instagram"`
	Facebook       string         `json:"facebook"`
	BusinessHours  string         `json:"businessHours"`
	ItbisPercent   int            `json:"itbisPercent"`
	DepositPercent int            `json:"depositPercent"`
	DeliveryZones  []DeliveryZone `json:"deliveryZones"`
}

func (b BusinessSettings) ToPublic(zones []DeliveryZone) PublicBusinessSettings {
	return PublicBusinessSettings{
		CompanyName:    b.CompanyName,
		RNC:            b.RNC,
		Address:        b.Address,
		Phone:          b.Phone,
		WhatsApp:       b.WhatsApp,
		Email:          b.Email,
		Instagram:      b.Instagram,
		Facebook:       b.Facebook,
		BusinessHours:  b.BusinessHours,
		ItbisPercent:   b.ItbisPercent,
		DepositPercent: b.DepositPercent,
		DeliveryZones:  zones,
	}
}

type Notification struct {
	ID             string `json:"id"`
	RecipientRole  string `json:"recipientRole"`
	UserID         string `json:"userId,omitempty"`
	Title          string `json:"title"`
	Body           string `json:"body"`
	Category       string `json:"category"`
	ActionPath     string `json:"actionPath,omitempty"`
	IsRead         bool   `json:"isRead"`
	CreatedAtLabel string `json:"createdAtLabel"`
}

type InsurancePolicy struct {
	ID              string `json:"id"`
	ProviderName    string `json:"providerName"`
	PolicyNumber    string `json:"policyNumber"`
	CoverageType    string `json:"coverageType"`
	MaxCoverageDop  int    `json:"maxCoverageDop"`
	ValidUntilLabel string `json:"validUntilLabel"`
	Status          string `json:"status"`
}

type AuditLog struct {
	ID             string `json:"id"`
	ActorName      string `json:"actorName"`
	ActorRole      string `json:"actorRole"`
	Action         string `json:"action"`
	EntityType     string `json:"entityType"`
	EntityID       string `json:"entityId"`
	Summary        string `json:"summary"`
	CreatedAtLabel string `json:"createdAtLabel"`
}

type BotFAQ struct {
	ID       string `json:"id"`
	Question string `json:"question"`
	Answer   string `json:"answer"`
	Category string `json:"category"`
	Active   bool   `json:"active"`
}

type BotKeyword struct {
	ID             string `json:"id"`
	Keyword        string `json:"keyword"`
	AutoReply      string `json:"autoReply"`
	HandoffToHuman bool   `json:"handoffToHuman"`
}

type BotThread struct {
	ID             string `json:"id"`
	CustomerName   string `json:"customerName"`
	Phone          string `json:"phone"`
	LastMessage    string `json:"lastMessage"`
	BotPaused      bool   `json:"botPaused"`
	AssignedAgent  string `json:"assignedAgent,omitempty"`
	UpdatedAtLabel string `json:"updatedAtLabel"`
}

type ReportPoint struct {
	Label   string `json:"label"`
	ValueRD int    `json:"valueRD"`
	Count   int    `json:"count"`
}

type ReportExport struct {
	ReportType  string        `json:"reportType"`
	Period      string        `json:"period"`
	GeneratedAt string        `json:"generatedAt"`
	Currency    string        `json:"currency"`
	TotalRD     int           `json:"totalRD"`
	ItbisRD     int           `json:"itbisRD"`
	Series      []ReportPoint `json:"series"`
	Filename    string        `json:"filename"`
	CSV         string        `json:"csv"`
}

func CanonicalReviews() []Review {
	return []Review{
		{
			ID:             "rev-gala-caribe",
			EventID:        "evt-gala-caribe",
			ClientName:     "Grupo Caribe",
			Rating:         5,
			Comment:        "Montaje impecable y puntual en toda la gala corporativa.",
			AdminReply:     "¡Gracias por confiar en RosaFiesta!",
			CreatedAtLabel: "13 oct",
		},
		{
			ID:             "rev-boda-real",
			EventID:        "evt-boda-real",
			ClientName:     "Camila Rodríguez",
			Rating:         5,
			Comment:        "El arco orgánico rosa y las sillas Tiffany doradas superaron nuestro sueño.",
			CreatedAtLabel: "19 oct",
		},
	}
}

func CanonicalSuppliers() []Supplier {
	return []Supplier{
		{
			ID:          "sup-flores-caribe",
			Name:        "Flores del Caribe SRL",
			Category:    "Floristería",
			Specialty:   "Rosas ecuatorianas y hortensias frescas",
			ContactName: "Elena Morales",
			Phone:       "809-555-0301",
			Email:       "pedidos@floresdelcaribe.do",
			Website:     "https://floresdelcaribe.do",
			Rating:      "4.9",
			Notes:       "Entregas refrigeradas martes y jueves en Piantini",
		},
		{
			ID:          "sup-cristaleria-ozama",
			Name:        "Cristalería Ozama",
			Category:    "Menaje",
			Specialty:   "Bases de cristal soplado y candelabros dorados",
			ContactName: "Marcos Tavárez",
			Phone:       "809-555-0419",
			Email:       "ventas@cristaleriaozama.do",
			Rating:      "4.8",
			Notes:       "Reposición en menos de 24 horas en el Distrito Nacional",
		},
	}
}

func CanonicalLeads() []Lead {
	return []Lead{
		{
			ID:             "lead-mateo",
			FullName:       "Laura Peña",
			Phone:          "809-555-0188",
			Email:          "laura.pena@correo.do",
			EventType:      "Cumpleaños de Mateo",
			EventDateLabel: "Sáb 07 nov",
			Guests:         40,
			EstimatedDop:   405,
			Stage:          "cotizado",
			Notes:          "Interesada en mesa de dulces y arco infantil",
		},
		{
			ID:             "lead-boda-marina",
			FullName:       "Sofía Almonte",
			Phone:          "829-555-0744",
			Email:          "sofia.almonte@correo.do",
			EventType:      "Boda frente al mar",
			EventDateLabel: "Sáb 21 nov",
			Guests:         140,
			EstimatedDop:   6800,
			Stage:          "nuevo",
			Notes:          "Solicitó catálogo de mobiliario dorado y carpa transparente",
		},
	}
}

func CanonicalBusinessSettings() BusinessSettings {
	return BusinessSettings{
		ID:             "default",
		CompanyName:    "RosaFiesta Eventos & Alquileres SRL",
		RNC:            "1-31-84920-2",
		Address:        "Av. Winston Churchill 1099, Piantini, Santo Domingo",
		Phone:          "+1 (809) 555-0100",
		WhatsApp:       "+1 (809) 555-0199",
		Email:          "hola@rosafiesta.do",
		Instagram:      "@rosafiesta.do",
		Facebook:       "facebook.com/rosafiestado",
		BusinessHours:  "Lun–Sáb 9:00 AM – 7:00 PM · Dom con cita previa",
		ItbisPercent:   18,
		DepositPercent: 30,
	}
}

func CanonicalDeliveryZones() []DeliveryZone {
	return []DeliveryZone{
		{ID: "zone-dn", Name: "Distrito Nacional (Piantini, Naco, Bella Vista)", FeeDop: 1200, EtaMinutes: 35, Active: true},
		{ID: "zone-sde", Name: "Santo Domingo Este & Norte", FeeDop: 2200, EtaMinutes: 55, Active: true},
		{ID: "zone-boca-chica", Name: "Boca Chica & Juan Dolio", FeeDop: 4500, EtaMinutes: 75, Active: true},
	}
}

func CanonicalNotifications() []Notification {
	camilaID := "11111111-1111-1111-1111-111111111111"
	return []Notification{
		{
			ID:             "ntf-boda-pago",
			RecipientRole:  "client",
			UserID:         camilaID,
			Title:          "Anticipo verificado",
			Body:           "Recibimos tu anticipo de RD$ 1,350 para Boda Real Rosa & Dorado.",
			Category:       "pagos",
			ActionPath:     "/events/evt-boda-real",
			IsRead:         false,
			CreatedAtLabel: "Hace 15 min",
		},
		{
			ID:             "ntf-admin-cotizacion",
			RecipientRole:  "admin",
			Title:          "Nueva solicitud de cotización",
			Body:           "Laura Peña solicitó propuesta para Cumpleaños de Mateo.",
			Category:       "cotizaciones",
			ActionPath:     "/admin/quotes/quo-cumple-mateo",
			IsRead:         false,
			CreatedAtLabel: "Hace 32 min",
		},
		{
			ID:             "ntf-staff-ruta",
			RecipientRole:  "staff",
			Title:          "Ruta de montaje asignada",
			Body:           "Camión #01 programado a las 09:00 AM hacia Jardín Botánico.",
			Category:       "logistica",
			ActionPath:     "/operations/routes",
			IsRead:         false,
			CreatedAtLabel: "Hace 1 h",
		},
	}
}

func CanonicalInsurancePolicies() []InsurancePolicy {
	return []InsurancePolicy{
		{
			ID:              "ins-seguros-universal",
			ProviderName:    "Seguros Universal",
			PolicyNumber:    "POL-RD-2026-8841",
			CoverageType:    "Todo riesgo mobiliario y responsabilidad civil en salones",
			MaxCoverageDop:  1500000,
			ValidUntilLabel: "31 dic 2026",
			Status:          "active",
		},
		{
			ID:              "ins-flota-banreservas",
			ProviderName:    "Seguros Reservas",
			PolicyNumber:    "POL-FLT-2026-3109",
			CoverageType:    "Flota de camiones de transporte y carga en tránsito",
			MaxCoverageDop:  850000,
			ValidUntilLabel: "30 jun 2027",
			Status:          "active",
		},
	}
}

func CanonicalAuditLogs() []AuditLog {
	return []AuditLog{
		{
			ID:             "aud-1",
			ActorName:      "María Rosario",
			ActorRole:      "admin",
			Action:         "VERIFY_PAYMENT",
			EntityType:     "payment",
			EntityID:       "pay-boda-anticipo",
			Summary:        "Verificó transferencia Banreservas RD$ 1,350 (BR-9948120)",
			CreatedAtLabel: "18 oct · 04:12 PM",
		},
		{
			ID:             "aud-2",
			ActorName:      "Pedro Santos",
			ActorRole:      "staff",
			Action:         "UPDATE_CHECKLIST",
			EntityType:     "event_logistics",
			EntityID:       "evt-boda-real",
			Summary:        "Completó distribución de 15 mesas en Jardín Botánico",
			CreatedAtLabel: "23 oct · 10:45 AM",
		},
	}
}

func CanonicalBotFAQs() []BotFAQ {
	return []BotFAQ{
		{
			ID:       "faq-anticipo",
			Question: "¿Cuánto es el anticipo para reservar fecha?",
			Answer:   "Reservamos tu fecha con el 30% de anticipo mediante transferencia (Popular, Banreservas, BHD León), tarjeta o efectivo.",
			Category: "pagos",
			Active:   true,
		},
		{
			ID:       "faq-entrega",
			Question: "¿Incluyen transporte y montaje en Santo Domingo?",
			Answer:   "Sí, nuestro equipo técnico entrega, monta y recoge el mobiliario en todo el Gran Santo Domingo, Boca Chica y Juan Dolio.",
			Category: "logistica",
			Active:   true,
		},
	}
}

func CanonicalBotKeywords() []BotKeyword {
	return []BotKeyword{
		{
			ID:             "kw-cotizar",
			Keyword:        "cotizar",
			AutoReply:      "¡Hola! Compártenos tipo de evento, fecha y cantidad de invitados para armar tu propuesta al instante.",
			HandoffToHuman: false,
		},
		{
			ID:             "kw-humano",
			Keyword:        "asesor",
			AutoReply:      "Enseguida te conectamos con María o Yuli de nuestro equipo de diseño.",
			HandoffToHuman: true,
		},
	}
}

func CanonicalBotThreads() []BotThread {
	return []BotThread{
		{
			ID:             "wapp-1",
			CustomerName:   "Camila Rodríguez",
			Phone:          "+1 (809) 555-0142",
			LastMessage:    "¿Podemos confirmar la hora de llegada del camión el viernes?",
			BotPaused:      true,
			AssignedAgent:  "María Rosario",
			UpdatedAtLabel: "Hace 10 min",
		},
		{
			ID:             "wapp-2",
			CustomerName:   "Laura Peña",
			Phone:          "+1 (809) 555-0188",
			LastMessage:    "Quiero cotizar cumpleaños infantil para 40 invitados",
			BotPaused:      false,
			UpdatedAtLabel: "Hace 28 min",
		},
	}
}

// ReportMonthMetric resume la actividad real agregada de un mes calendario.
type ReportMonthMetric struct {
	Year           int
	Month          time.Month
	IncomeRD       int
	EventsCount    int
	ClientsCount   int
	InventoryCount int
}

// ReportSnapshot encapsula métricas reales de la plataforma para construir reportes.
type ReportSnapshot struct {
	Months       []ReportMonthMetric
	ItbisPercent int
}

// BuildReportExport construye el reporte tabular usando la fecha actual y el ingreso del mes en curso.
func BuildReportExport(reportType, period string, currentMonthRevenueRD int, now time.Time) (ReportExport, error) {
	snapshot := ReportSnapshot{
		ItbisPercent: 18,
		Months: []ReportMonthMetric{
			{
				Year:     now.Year(),
				Month:    now.Month(),
				IncomeRD: currentMonthRevenueRD,
			},
		},
	}
	return BuildReportExportFromSnapshot(reportType, period, snapshot, now)
}

// BuildReportExportFromSnapshot construye el reporte usando métricas reales agregadas por mes.
func BuildReportExportFromSnapshot(reportType, period string, snapshot ReportSnapshot, now time.Time) (ReportExport, error) {
	normalizedType, okType := normalizeReportType(reportType)
	if !okType {
		return ReportExport{}, ErrInvalidReportRequest
	}
	normalizedPeriod, okPeriod := normalizeReportPeriod(period)
	if !okPeriod {
		return ReportExport{}, ErrInvalidReportRequest
	}
	monthCount := 6
	if normalizedPeriod == "12m" {
		monthCount = 12
	}
	itbisRate := snapshot.ItbisPercent
	if itbisRate < 0 {
		itbisRate = 18
	}
	series := buildReportSeriesFromSnapshot(normalizedType, monthCount, snapshot.Months, now)
	totalRD := 0
	for _, point := range series {
		totalRD += point.ValueRD
	}
	itbisRD := extractIncludedITBIS(totalRD, itbisRate)
	csvBody := formatReportCSVWithRate(series, itbisRate)
	return ReportExport{
		ReportType:  normalizedType,
		Period:      normalizedPeriod,
		GeneratedAt: now.UTC().Format(time.RFC3339),
		Currency:    "DOP",
		TotalRD:     totalRD,
		ItbisRD:     itbisRD,
		Series:      series,
		Filename:    fmt.Sprintf("rosafiesta-%s-%s.csv", normalizedType, normalizedPeriod),
		CSV:         csvBody,
	}, nil
}

func normalizeReportType(raw string) (string, bool) {
	cleaned := strings.ToLower(strings.TrimSpace(raw))
	if cleaned == "" {
		return "income", true
	}
	switch cleaned {
	case "income", "events", "clients", "inventory", "dgii":
		return cleaned, true
	default:
		return "", false
	}
}

func normalizeReportPeriod(raw string) (string, bool) {
	cleaned := strings.ToLower(strings.TrimSpace(raw))
	if cleaned == "" {
		return "6m", true
	}
	switch cleaned {
	case "6m", "12m":
		return cleaned, true
	default:
		return "", false
	}
}

func buildReportSeriesFromSnapshot(
	reportType string,
	monthCount int,
	metrics []ReportMonthMetric,
	now time.Time,
) []ReportPoint {
	byKey := make(map[string]ReportMonthMetric, len(metrics))
	for _, item := range metrics {
		key := fmt.Sprintf("%04d-%02d", item.Year, int(item.Month))
		byKey[key] = item
	}
	points := make([]ReportPoint, 0, monthCount)
	anchor := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, time.UTC)
	for offset := monthCount - 1; offset >= 0; offset-- {
		monthTime := anchor.AddDate(0, -offset, 0)
		key := fmt.Sprintf("%04d-%02d", monthTime.Year(), int(monthTime.Month()))
		metric := byKey[key]
		points = append(points, ReportPoint{
			Label:   formatSpanishMonthYear(monthTime),
			ValueRD: metric.IncomeRD,
			Count:   resolveMetricCount(reportType, metric),
		})
	}
	return points
}

func resolveMetricCount(reportType string, metric ReportMonthMetric) int {
	switch reportType {
	case "inventory":
		return metric.InventoryCount
	case "clients":
		return metric.ClientsCount
	default:
		return metric.EventsCount
	}
}

func formatSpanishMonthYear(ts time.Time) string {
	months := [12]string{
		"Ene", "Feb", "Mar", "Abr", "May", "Jun",
		"Jul", "Ago", "Sep", "Oct", "Nov", "Dic",
	}
	idx := int(ts.Month()) - 1
	if idx < 0 {
		idx = 0
	}
	if idx >= len(months) {
		idx = len(months) - 1
	}
	return fmt.Sprintf("%s %d", months[idx], ts.Year())
}

func formatReportCSVWithRate(series []ReportPoint, itbisRate int) string {
	var builder strings.Builder
	fmt.Fprintf(&builder, "Periodo,Monto_DOP,ITBIS_%d_DOP,Cantidad\n", itbisRate)
	for _, point := range series {
		itbis := extractIncludedITBIS(point.ValueRD, itbisRate)
		line := fmt.Sprintf("%s,%d,%d,%d\n", point.Label, point.ValueRD, itbis, point.Count)
		builder.WriteString(line)
	}
	return builder.String()
}

func extractIncludedITBIS(grossRD, itbisRate int) int {
	if grossRD <= 0 {
		return 0
	}
	if itbisRate <= 0 {
		return 0
	}
	divisor := 1.0 + float64(itbisRate)/100.0
	subtotal := int(math.Round(float64(grossRD) / divisor))
	if subtotal > grossRD {
		return 0
	}
	return grossRD - subtotal
}
