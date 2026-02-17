package models

// AdminStats represents summarized business metrics for the admin dashboard.
type AdminStats struct {
	TotalRevenue   float64            `json:"total_revenue"`
	TotalEvents    int                `json:"total_events"`
	RevenueByMonth map[string]float64 `json:"revenue_by_month"`
	EventsByStatus map[string]int     `json:"events_by_status"`
}
