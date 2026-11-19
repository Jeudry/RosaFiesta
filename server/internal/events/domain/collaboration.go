package domain

import (
	"fmt"
	"strconv"
	"strings"
	"time"
)

type EventGuest struct {
	ID           string    `json:"id"`
	EventID      string    `json:"eventId"`
	FullName     string    `json:"fullName"`
	RSVPStatus   string    `json:"rsvpStatus"`
	TableLabel   string    `json:"tableLabel"`
	DietaryNotes string    `json:"dietaryNotes,omitempty"`
	CreatedAt    time.Time `json:"createdAt"`
}

func IsValidRSVPStatus(status string) bool {
	switch status {
	case "pending", "confirmed", "declined":
		return true
	default:
		return false
	}
}

type ScheduleBlock struct {
	ID        string `json:"id"`
	EventID   string `json:"eventId"`
	TimeRange string `json:"timeRange"`
	Title     string `json:"title"`
	OwnerName string `json:"ownerName"`
	Status    string `json:"status"`
	SortOrder int    `json:"sortOrder"`
}

type ChatThread struct {
	ThreadID        string    `json:"threadId"`
	EventID         string    `json:"eventId"`
	EventTitle      string    `json:"eventTitle"`
	ParticipantName string    `json:"participantName"`
	LastMessage     string    `json:"lastMessage"`
	UnreadCount     int       `json:"unreadCount"`
	UpdatedAt       time.Time `json:"updatedAt"`
}

type ChatMessage struct {
	ID             string    `json:"id"`
	ThreadID       string    `json:"threadId"`
	EventID        string    `json:"eventId"`
	EventTitle     string    `json:"eventTitle"`
	SenderID       string    `json:"senderId"`
	SenderName     string    `json:"senderName"`
	SenderRole     string    `json:"senderRole"`
	RecipientLabel string    `json:"recipientLabel,omitempty"`
	Body           string    `json:"body"`
	AttachmentName string    `json:"attachmentName,omitempty"`
	AttachmentURL  string    `json:"attachmentUrl,omitempty"`
	Unread         bool      `json:"unread"`
	CreatedAt      time.Time `json:"createdAt"`
}

// ComputeEventDateRange resuelve la fecha ISO del evento y las fechas de retiro (-1d) y devolución (+1d).
func ComputeEventDateRange(explicitISO, dateLabel string, referenceYear int) (eventISO, pickupISO, returnISO string) {
	resolved := resolveSingleISODate(explicitISO, referenceYear)
	if resolved == "" {
		resolved = resolveSingleISODate(dateLabel, referenceYear)
	}
	if resolved == "" {
		return "", "", ""
	}
	parsed, err := time.Parse("2006-01-02", resolved)
	if err != nil {
		return resolved, "", ""
	}
	pickupISO = parsed.AddDate(0, 0, -1).Format("2006-01-02")
	returnISO = parsed.AddDate(0, 0, 1).Format("2006-01-02")
	return resolved, pickupISO, returnISO
}

func resolveSingleISODate(raw string, referenceYear int) string {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return ""
	}
	if len(trimmed) >= 10 {
		prefix := trimmed[:10]
		if parsedSlash, err := time.Parse("02/01/2006", prefix); err == nil {
			return parsedSlash.Format("2006-01-02")
		}
	}
	return EventISODate(trimmed, referenceYear)
}

func EventISODate(dateLabel string, referenceYear int) string {
	trimmed := strings.TrimSpace(dateLabel)
	if len(trimmed) >= 10 {
		prefix := trimmed[:10]
		if _, err := time.Parse("2006-01-02", prefix); err == nil {
			return prefix
		}
	}
	day, month, parsedYear := parseSpanishDateTokens(strings.Fields(trimmed))
	if parsedYear > 0 {
		referenceYear = parsedYear
	}
	if day == 0 {
		return ""
	}
	if month == 0 {
		return ""
	}
	if referenceYear <= 0 {
		referenceYear = 2026
	}
	return fmt.Sprintf("%04d-%02d-%02d", referenceYear, month, day)
}

func parseSpanishDateTokens(tokens []string) (day, month, year int) {
	for _, token := range tokens {
		cleaned := strings.Trim(token, ",.")
		lower := strings.ToLower(cleaned)
		if numericVal, err := strconv.Atoi(lower); err == nil {
			if numericVal >= 1900 {
				if numericVal <= 2100 {
					year = numericVal
				}
				continue
			}
			if numericVal >= 1 {
				if numericVal <= 31 {
					day = numericVal
				}
			}
			continue
		}
		if parsedMonth := spanishMonthNumber(lower); parsedMonth > 0 {
			month = parsedMonth
		}
	}
	return day, month, year
}

func spanishMonthNumber(lower string) int {
	months := map[string]int{
		"ene": 1, "enero": 1,
		"feb": 2, "febrero": 2,
		"mar": 3, "marzo": 3,
		"abr": 4, "abril": 4,
		"may": 5, "mayo": 5,
		"jun": 6, "junio": 6,
		"jul": 7, "julio": 7,
		"ago": 8, "agosto": 8,
		"sep": 9, "sept": 9, "septiembre": 9,
		"oct": 10, "octubre": 10,
		"nov": 11, "noviembre": 11,
		"dic": 12, "diciembre": 12,
	}
	return months[lower]
}

func MatchesArticleID(itemCode, targetArticleID string) bool {
	trimmedCode := strings.TrimSpace(itemCode)
	lowerCode := strings.ToLower(trimmedCode)
	trimmedTarget := strings.TrimSpace(targetArticleID)
	lowerTarget := strings.ToLower(trimmedTarget)
	if lowerCode == lowerTarget {
		return true
	}
	codeToSlug := map[string]string{
		"rf-001": "silla-tiffany",
		"rf-002": "mesa-plegable",
		"rf-003": "mesa-redonda",
		"rf-004": "centro-floral",
		"rf-005": "arco-rosa",
		"rf-006": "mesa-helados",
	}
	mapped, ok := codeToSlug[lowerCode]
	if !ok {
		return false
	}
	return mapped == lowerTarget
}

func CanonicalGuests(now time.Time) []EventGuest {
	return []EventGuest{
		{
			ID:           "gst-boda-1",
			EventID:      "evt-boda-real",
			FullName:     "Sofía Rodríguez",
			RSVPStatus:   "confirmed",
			TableLabel:   "Mesa 1 · Familia",
			DietaryNotes: "Sin gluten",
			CreatedAt:    now,
		},
		{
			ID:           "gst-boda-2",
			EventID:      "evt-boda-real",
			FullName:     "Diego Morales",
			RSVPStatus:   "confirmed",
			TableLabel:   "Mesa 1 · Familia",
			DietaryNotes: "",
			CreatedAt:    now,
		},
		{
			ID:           "gst-boda-3",
			EventID:      "evt-boda-real",
			FullName:     "Valeria Henríquez",
			RSVPStatus:   "pending",
			TableLabel:   "Mesa 2 · Amigos",
			DietaryNotes: "Vegetariana",
			CreatedAt:    now,
		},
	}
}

func CanonicalScheduleBlocks() []ScheduleBlock {
	return []ScheduleBlock{
		{
			ID:        "sch-boda-1",
			EventID:   "evt-boda-real",
			TimeRange: "14:00 - 16:30",
			Title:     "Descarga de mobiliario y armado de estructuras",
			OwnerName: "Pedro Santos",
			Status:    "confirmed",
			SortOrder: 1,
		},
		{
			ID:        "sch-boda-2",
			EventID:   "evt-boda-real",
			TimeRange: "16:30 - 18:00",
			Title:     "Instalación de arco floral y mesa de dulces",
			OwnerName: "Yuli Mercedes",
			Status:    "confirmed",
			SortOrder: 2,
		},
		{
			ID:        "sch-boda-3",
			EventID:   "evt-boda-real",
			TimeRange: "19:00 - 23:30",
			Title:     "Recepción principal y supervisión en sitio",
			OwnerName: "María",
			Status:    "pending",
			SortOrder: 3,
		},
	}
}

func CanonicalChatMessages(now time.Time) []ChatMessage {
	earlier := now.Add(-2 * time.Hour)
	recent := now.Add(-45 * time.Minute)
	return []ChatMessage{
		{
			ID:             "msg-boda-1",
			ThreadID:       "thr-evt-boda-real",
			EventID:        "evt-boda-real",
			EventTitle:     "Boda Real Rosa & Dorado",
			SenderID:       "11111111-1111-1111-1111-111111111111",
			SenderName:     "Camila Rodríguez",
			SenderRole:     "client",
			RecipientLabel: "Equipo RosaFiesta",
			Body:           "Hola equipo, confirmamos 120 invitados y acceso al Jardín Botánico desde las 2:00 PM.",
			Unread:         false,
			CreatedAt:      earlier,
		},
		{
			ID:             "msg-boda-2",
			ThreadID:       "thr-evt-boda-real",
			EventID:        "evt-boda-real",
			EventTitle:     "Boda Real Rosa & Dorado",
			SenderID:       "22222222-2222-2222-2222-222222222222",
			SenderName:     "María (Dueña)",
			SenderRole:     "admin",
			RecipientLabel: "Camila Rodríguez",
			Body:           "Recibido Camila. Pedro y Yuli llegarán puntual con las 120 sillas Tiffany y el arco orgánico.",
			Unread:         false,
			CreatedAt:      recent,
		},
		{
			ID:             "msg-quince-1",
			ThreadID:       "thr-evt-quince-valentina",
			EventID:        "evt-quince-valentina",
			EventTitle:     "Quinceañera Valentina Crystal",
			SenderID:       "33333333-3333-3333-3333-333333333333",
			SenderName:     "Yuli",
			SenderRole:     "staff",
			RecipientLabel: "Camila Rodríguez",
			Body:           "Te compartimos la paleta cristal y rosa para validar el arreglo central.",
			Unread:         true,
			CreatedAt:      now,
		},
	}
}
