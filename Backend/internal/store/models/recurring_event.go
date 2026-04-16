package models

import (
	"time"

	"github.com/google/uuid"
)

type RecurringFrequency string

const (
	RecurringFrequencyWeekly   RecurringFrequency = "weekly"
	RecurringFrequencyBiweekly RecurringFrequency = "biweekly"
	RecurringFrequencyMonthly  RecurringFrequency = "monthly"
)

type RecurringEvent struct {
	ID             uuid.UUID         `json:"id"`
	UserID         uuid.UUID         `json:"user_id"`
	Name           string            `json:"name"`
	Location       string            `json:"location,omitempty"`
	GuestCount     int               `json:"guest_count"`
	Budget         float64           `json:"budget"`
	Frequency      RecurringFrequency `json:"frequency"`
	IntervalValue  int               `json:"interval_value"`
	DaysOfWeek     []int             `json:"days_of_week,omitempty"`
	StartDate      time.Time         `json:"start_date"`
	EndDate        *time.Time        `json:"end_date,omitempty"`
	NextRunDate    time.Time         `json:"next_run_date"`
	LastRunEventID *uuid.UUID        `json:"last_run_event_id,omitempty"`
	AutoCreate     bool              `json:"auto_create"`
	IsActive       bool              `json:"is_active"`
	CreatedAt      time.Time         `json:"created_at"`
	UpdatedAt      time.Time         `json:"updated_at"`
	User           *User             `json:"user,omitempty"`
}
