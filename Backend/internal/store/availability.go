package store

import (
	"context"
	"database/sql"
	"time"

	"github.com/google/uuid"
)

type AvailabilityStore struct {
	db *sql.DB
}

type ArticleAvailability struct {
	ArticleID  uuid.UUID `json:"article_id"`
	TotalStock int       `json:"total_stock"`
	Reserved   int       `json:"reserved"`
	Available  int       `json:"available"`
}

type CalendarDay struct {
	Date         string                `json:"date"`
	DayOfWeek    int                   `json:"day_of_week"`
	IsWeekend    bool                  `json:"is_weekend"`
	EventsCount  int                   `json:"events_count"`
	ItemsUsed    map[string]int        `json:"items_used"`
	Availability []ArticleAvailability `json:"availability"`
}

func (s *AvailabilityStore) CheckArticleAvailability(ctx context.Context, articleID uuid.UUID, date time.Time) (int, error) {
	var totalStock int
	stockQuery := `SELECT stock FROM article_variants WHERE id = $1`
	if err := s.db.QueryRowContext(ctx, stockQuery, articleID).Scan(&totalStock); err != nil {
		return 0, err
	}

	var reserved int
	reservedQuery := `
		SELECT COALESCE(SUM(quantity_used), 0)
		FROM inventory_availability
		WHERE article_id = $1 AND event_date = $2 AND status NOT IN ('returned')`
	if err := s.db.QueryRowContext(ctx, reservedQuery, articleID, date).Scan(&reserved); err != nil {
		return 0, err
	}

	return totalStock - reserved, nil
}

func (s *AvailabilityStore) GetArticleAvailabilityRange(ctx context.Context, articleID uuid.UUID, startDate, endDate time.Time) ([]CalendarDay, error) {
	query := `
		SELECT
			av.event_date,
			COALESCE(SUM(av.quantity_used), 0) as used
		FROM inventory_availability av
		WHERE av.article_id = $1
			AND av.event_date BETWEEN $2 AND $3
			AND av.status NOT IN ('returned')
		GROUP BY av.event_date
		ORDER BY av.event_date`

	rows, err := s.db.QueryContext(ctx, query, articleID, startDate, endDate)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	usageMap := make(map[string]int)
	for rows.Next() {
		var eventDate time.Time
		var used int
		if err := rows.Scan(&eventDate, &used); err != nil {
			return nil, err
		}
		usageMap[eventDate.Format("2006-01-02")] = used
	}

	days := []CalendarDay{}
	for d := startDate; !d.After(endDate); d = d.AddDate(0, 0, 1) {
		day := CalendarDay{
			Date:      d.Format("2006-01-02"),
			DayOfWeek: int(d.Weekday()),
			IsWeekend: d.Weekday() == time.Saturday || d.Weekday() == time.Sunday,
			ItemsUsed: make(map[string]int),
		}
		if used, ok := usageMap[day.Date]; ok {
			day.ItemsUsed[articleID.String()] = used
		}
		days = append(days, day)
	}

	return days, nil
}

func (s *AvailabilityStore) GetAllArticlesAvailability(ctx context.Context, date time.Time) ([]ArticleAvailability, error) {
	query := `
		SELECT
			av.article_id,
			av.stock,
			COALESCE(used.quantity_used, 0) as reserved
		FROM article_variants av
		LEFT JOIN (
			SELECT article_id, SUM(quantity_used) as quantity_used
			FROM inventory_availability
			WHERE event_date = $1 AND status NOT IN ('returned')
			GROUP BY article_id
		) used ON av.article_id = used.article_id
		WHERE av.is_active = true`

	rows, err := s.db.QueryContext(ctx, query, date)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var availability []ArticleAvailability
	for rows.Next() {
		var a ArticleAvailability
		if err := rows.Scan(&a.ArticleID, &a.TotalStock, &a.Reserved); err != nil {
			return nil, err
		}
		a.Available = a.TotalStock - a.Reserved
		availability = append(availability, a)
	}

	return availability, nil
}

func (s *AvailabilityStore) ReserveInventory(ctx context.Context, articleID, eventID uuid.UUID, date time.Time, quantity int) error {
	query := `
		INSERT INTO inventory_availability (article_id, event_id, event_date, quantity_used, status)
		VALUES ($1, $2, $3, $4, 'reserved')
		ON CONFLICT (article_id, event_id, event_date) DO UPDATE
		SET quantity_used = inventory_availability.quantity_used + $4`
	_, err := s.db.ExecContext(ctx, query, articleID, eventID, date, quantity)
	return err
}

func (s *AvailabilityStore) ReleaseInventory(ctx context.Context, articleID, eventID uuid.UUID, date time.Time) error {
	query := `
		UPDATE inventory_availability
		SET status = 'returned', updated_at = NOW()
		WHERE article_id = $1 AND event_id = $2 AND event_date = $3`
	_, err := s.db.ExecContext(ctx, query, articleID, eventID, date)
	return err
}

func (s *AvailabilityStore) ConfirmInventory(ctx context.Context, articleID, eventID uuid.UUID, date time.Time) error {
	query := `
		UPDATE inventory_availability
		SET status = 'confirmed', updated_at = NOW()
		WHERE article_id = $1 AND event_id = $2 AND event_date = $3`
	_, err := s.db.ExecContext(ctx, query, articleID, eventID, date)
	return err
}

func (s *AvailabilityStore) GetCalendarView(ctx context.Context, startDate, endDate time.Time) ([]CalendarDay, error) {
	query := `
		SELECT
			ia.event_date,
			COUNT(DISTINCT ia.event_id) as events_count,
			COUNT(*) as entries_count
		FROM inventory_availability ia
		WHERE ia.event_date BETWEEN $1 AND $2
		GROUP BY ia.event_date
		ORDER BY ia.event_date`

	rows, err := s.db.QueryContext(ctx, query, startDate, endDate)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	eventsMap := make(map[string]int)
	for rows.Next() {
		var eventDate time.Time
		var eventsCount, entriesCount int
		if err := rows.Scan(&eventDate, &eventsCount, &entriesCount); err != nil {
			return nil, err
		}
		eventsMap[eventDate.Format("2006-01-02")] = eventsCount
	}

	days := []CalendarDay{}
	for d := startDate; !d.After(endDate); d = d.AddDate(0, 0, 1) {
		day := CalendarDay{
			Date:      d.Format("2006-01-02"),
			DayOfWeek: int(d.Weekday()),
			IsWeekend: d.Weekday() == time.Saturday || d.Weekday() == time.Sunday,
			ItemsUsed: make(map[string]int),
		}
		if count, ok := eventsMap[day.Date]; ok {
			day.EventsCount = count
		}
		days = append(days, day)
	}

	return days, nil
}
