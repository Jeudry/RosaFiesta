// Package domain define los contratos y matemáticas de disponibilidad por rango de fechas.
package domain

import (
	"fmt"
	"strconv"
	"strings"
	"time"
)

// ReservationStatus representa el estado de una reserva de inventario por rango de fechas.
type ReservationStatus string

const (
	ReservationHeld      ReservationStatus = "held"
	ReservationConfirmed ReservationStatus = "confirmed"
	ReservationReleased  ReservationStatus = "released"
	ReservationCancelled ReservationStatus = "cancelled"
)

// StockReservation registra unidades comprometidas en una ventana [PickupDateISO, ReturnDateISO].
type StockReservation struct {
	ID            string            `json:"id"`
	ArticleID     string            `json:"articleId"`
	EventID       string            `json:"eventId"`
	QuoteID       string            `json:"quoteId,omitempty"`
	Quantity      int               `json:"quantity"`
	PickupDateISO string            `json:"pickupDateIso"`
	ReturnDateISO string            `json:"returnDateIso"`
	Status        ReservationStatus `json:"status"`
	CreatedAt     time.Time         `json:"createdAt"`
	UpdatedAt     time.Time         `json:"updatedAt"`
}

// StockRequirement solicita una cantidad de unidades para validar o reservar.
type StockRequirement struct {
	ArticleRef string `json:"articleRef"`
	Name       string `json:"name,omitempty"`
	Quantity   int    `json:"quantity"`
}

// AvailabilityConflict detalla un faltante de stock en una fecha puntual.
type AvailabilityConflict struct {
	ArticleID   string `json:"articleId"`
	ArticleName string `json:"articleName"`
	Requested   int    `json:"requested"`
	Available   int    `json:"available"`
	DateRange   string `json:"dateRange"`
}

// AvailabilityCheckResult resume si la solicitud cabe en la ventana operativa solicitada.
type AvailabilityCheckResult struct {
	Available     bool                   `json:"available"`
	EventDateISO  string                 `json:"eventDateIso"`
	PickupDateISO string                 `json:"pickupDateIso"`
	ReturnDateISO string                 `json:"returnDateIso"`
	Conflicts     []AvailabilityConflict `json:"conflicts"`
}

// IsActive indica si la reserva bloquea inventario físico.
func (s ReservationStatus) IsActive() bool {
	if s == ReservationHeld {
		return true
	}
	return s == ReservationConfirmed
}

// TryParseDateISO convierte etiquetas comunes ("2026-11-28", "28/11/2026", "28 Nov 2026") a YYYY-MM-DD.
func TryParseDateISO(raw string) (time.Time, bool) {
	cleaned := strings.TrimSpace(raw)
	if cleaned == "" {
		return time.Time{}, false
	}
	if len(cleaned) >= 10 {
		prefix := cleaned[:10]
		if parsed, err := time.Parse("2006-01-02", prefix); err == nil {
			return parsed, true
		}
		if parsed, err := time.Parse("02/01/2006", prefix); err == nil {
			return parsed, true
		}
	}
	return parseSpanishDateLabel(cleaned)
}

// ParseEventDateRange normaliza la fecha y aplica margen de +/- 1 día para entrega y recogida.
func ParseEventDateRange(raw string, fallback time.Time) (eventISO, pickupISO, returnISO string) {
	parsed, ok := TryParseDateISO(raw)
	if !ok {
		if fallback.IsZero() {
			fallback = time.Now().UTC()
		}
		parsed = fallback.UTC()
	}
	eventISO = parsed.Format("2006-01-02")
	pickupISO = parsed.AddDate(0, 0, -1).Format("2006-01-02")
	returnISO = parsed.AddDate(0, 0, 1).Format("2006-01-02")
	return eventISO, pickupISO, returnISO
}

// DatesOverlap determina si dos rangos ISO inclusivos [start, end] se solapan.
func DatesOverlap(startA, endA, startB, endB string) bool {
	if startA > endB {
		return false
	}
	if endA < startB {
		return false
	}
	return true
}

// EffectiveAvailableForDate calcula las unidades libres descontando dañadas y el pico de reservas solapadas.
func EffectiveAvailableForDate(
	article Article,
	reservations []StockReservation,
	pickupISO string,
	returnISO string,
	excludeEventID string,
) int {
	overlapping := make([]StockReservation, 0)
	sampleDates := []string{pickupISO}
	for _, res := range reservations {
		if !isMatchingActiveReservation(res, article.ID, pickupISO, returnISO, excludeEventID) {
			continue
		}
		overlapping = append(overlapping, res)
		if res.PickupDateISO > pickupISO {
			sampleDates = append(sampleDates, res.PickupDateISO)
		}
	}
	free := article.UsableStock() - peakReservedAtDates(overlapping, sampleDates)
	if free < 0 {
		return 0
	}
	return free
}

func isMatchingActiveReservation(
	res StockReservation,
	articleID string,
	pickupISO string,
	returnISO string,
	excludeEventID string,
) bool {
	if res.ArticleID != articleID {
		return false
	}
	if !res.Status.IsActive() {
		return false
	}
	if excludeEventID != "" {
		if res.EventID == excludeEventID {
			return false
		}
	}
	return DatesOverlap(res.PickupDateISO, res.ReturnDateISO, pickupISO, returnISO)
}

func peakReservedAtDates(overlapping []StockReservation, sampleDates []string) int {
	peakReserved := 0
	for _, day := range sampleDates {
		dayReserved := 0
		for _, res := range overlapping {
			if reservationCoversDay(res, day) {
				dayReserved += res.Quantity
			}
		}
		if dayReserved > peakReserved {
			peakReserved = dayReserved
		}
	}
	return peakReserved
}

func reservationCoversDay(res StockReservation, day string) bool {
	if res.PickupDateISO > day {
		return false
	}
	return day <= res.ReturnDateISO
}

// BuildAvailabilityConflictMessage genera un resumen legible para el cliente cuando falta stock.
func BuildAvailabilityConflictMessage(conflicts []AvailabilityConflict) string {
	if len(conflicts) == 0 {
		return ""
	}
	first := conflicts[0]
	return fmt.Sprintf(
		"Stock insuficiente de %s para %s (solicitado: %d, disponible: %d).",
		first.ArticleName,
		first.DateRange,
		first.Requested,
		first.Available,
	)
}

func parseSpanishDateLabel(raw string) (time.Time, bool) {
	replacer := strings.NewReplacer(",", " ", " de ", " ", " del ", " ", ".", " ")
	tokens := strings.Fields(strings.ToLower(replacer.Replace(raw)))
	if len(tokens) >= 4 {
		if _, err := strconv.Atoi(tokens[0]); err != nil {
			tokens = tokens[1:]
		}
	}
	if len(tokens) < 3 {
		return time.Time{}, false
	}
	day, err := strconv.Atoi(tokens[0])
	if err != nil || day < 1 {
		return time.Time{}, false
	}
	if day > 31 {
		return time.Time{}, false
	}
	month, ok := spanishMonthNumber(tokens[1])
	if !ok {
		return time.Time{}, false
	}
	year, yearErr := strconv.Atoi(tokens[2])
	if yearErr != nil || year < 2000 {
		return time.Time{}, false
	}
	if year > 2100 {
		return time.Time{}, false
	}
	parsed := time.Date(year, time.Month(month), day, 0, 0, 0, 0, time.UTC)
	if int(parsed.Month()) != month {
		return time.Time{}, false
	}
	return parsed, true
}

func spanishMonthNumber(token string) (int, bool) {
	months := map[string]int{
		"ene": 1, "enero": 1, "jan": 1,
		"feb": 2, "febrero": 2,
		"mar": 3, "marzo": 3,
		"abr": 4, "abril": 4, "apr": 4,
		"may": 5, "mayo": 5,
		"jun": 6, "junio": 6,
		"jul": 7, "julio": 7,
		"ago": 8, "agosto": 8, "aug": 8,
		"sep": 9, "sept": 9, "septiembre": 9,
		"oct": 10, "octubre": 10,
		"nov": 11, "noviembre": 11,
		"dic": 12, "diciembre": 12, "dec": 12,
	}
	val, exists := months[token]
	return val, exists
}
