package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"
)

type StatsStore struct {
	db *sql.DB
}

// GetSummary aggregates business metrics from the events table.
func (s *StatsStore) GetSummary(ctx context.Context) (*models.AdminStats, error) {
	stats := &models.AdminStats{
		RevenueByMonth: make(map[string]float64),
		EventsByStatus: make(map[string]int),
	}

	// 1. Total numbers and status distribution
	queryTotals := `
		SELECT status, COUNT(*), COALESCE(SUM(budget + additional_costs), 0)
		FROM events
		GROUP BY status
	`
	rows, err := s.db.QueryContext(ctx, queryTotals)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	for rows.Next() {
		var status string
		var count int
		var revenue float64
		if err := rows.Scan(&status, &count, &revenue); err != nil {
			return nil, err
		}
		stats.TotalEvents += count
		stats.TotalRevenue += revenue
		stats.EventsByStatus[status] = count
	}

	// 2. Revenue by month (last 12 months)
	queryMonthly := `
		SELECT TO_CHAR(date, 'YYYY-MM') as month, SUM(budget + additional_costs) as revenue
		FROM events
		WHERE date >= NOW() - INTERVAL '12 months'
		GROUP BY month
		ORDER BY month ASC
	`
	rowsMonthly, err := s.db.QueryContext(ctx, queryMonthly)
	if err != nil {
		return nil, err
	}
	defer rowsMonthly.Close()

	for rowsMonthly.Next() {
		var month string
		var revenue float64
		if err := rowsMonthly.Scan(&month, &revenue); err != nil {
			return nil, err
		}
		stats.RevenueByMonth[month] = revenue
	}

	return stats, nil
}
