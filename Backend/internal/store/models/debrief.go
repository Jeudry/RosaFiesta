package models

import (
	"time"
)

type EventDebrief struct {
	PunctualityScore float64           `json:"punctuality_score"`
	DelayedCritical  []DelayedItemInfo `json:"delayed_critical"`
	BudgetAnalysis   BudgetSummary     `json:"budget_analysis"`
	CompletionStats  CompletionStats   `json:"completion_stats"`
}

type DelayedItemInfo struct {
	Title        string        `json:"title"`
	ExpectedTime time.Time     `json:"expected_time"`
	ActualTime   time.Time     `json:"actual_time"`
	Delay        time.Duration `json:"delay"`
}

type BudgetSummary struct {
	EstimatedBudget float64 `json:"estimated_budget"`
	ActualSpent     float64 `json:"actual_spent"`
	Difference      float64 `json:"difference"`
	IsOverBudget    bool    `json:"is_over_budget"`
}

type CompletionStats struct {
	TotalTasks        int `json:"total_tasks"`
	CompletedTasks    int `json:"completed_tasks"`
	TotalTimeline     int `json:"total_timeline"`
	CompletedTimeline int `json:"completed_timeline"`
}
