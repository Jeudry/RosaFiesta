package models

import (
	"time"

	"github.com/google/uuid"
)

type Lead struct {
	ID                 uuid.UUID  `json:"id"`
	Source             string     `json:"source"`
	Status             string     `json:"status"`
	Priority           string     `json:"priority"`
	ClientName         string     `json:"client_name"`
	ClientEmail        string     `json:"client_email"`
	ClientPhone        string     `json:"client_phone"`
	EventType          string     `json:"event_type"`
	EventDate          *time.Time `json:"event_date"`
	GuestCount         int        `json:"guest_count"`
	BudgetMin          float64    `json:"budget_min"`
	BudgetMax          float64    `json:"budget_max"`
	Notes              string     `json:"notes"`
	AssignedTo         *uuid.UUID `json:"assigned_to"`
	LastContactAt      *time.Time `json:"last_contact_at"`
	NextFollowUp       *time.Time `json:"next_follow_up"`
	ConvertedToEventID *uuid.UUID `json:"converted_to_event_id"`
	CreatedAt          time.Time  `json:"created_at"`
	UpdatedAt          time.Time  `json:"updated_at"`
}

type LeadFollowup struct {
	ID           uuid.UUID  `json:"id"`
	LeadID       uuid.UUID  `json:"lead_id"`
	FollowUpDate time.Time  `json:"follow_up_date"`
	FollowUpType string     `json:"follow_up_type"`
	Notes        string     `json:"notes"`
	Completed    bool       `json:"completed"`
	CompletedAt  *time.Time `json:"completed_at"`
	CreatedAt    time.Time  `json:"created_at"`
}

type LeadActivity struct {
	ID           uuid.UUID              `json:"id"`
	LeadID       uuid.UUID              `json:"lead_id"`
	ActivityType string                 `json:"activity_type"`
	Description  string                 `json:"description"`
	Metadata     map[string]interface{} `json:"metadata"`
	CreatedAt    time.Time              `json:"created_at"`
}

type LeadStats struct {
	Total          int     `json:"total"`
	NewCount       int     `json:"new_count"`
	ContactedCount int     `json:"contacted_count"`
	QualifiedCount int     `json:"qualified_count"`
	WonCount       int     `json:"won_count"`
	LostCount      int     `json:"lost_count"`
	ConversionRate float64 `json:"conversion_rate"`
}

type CreateLeadRequest struct {
	ClientName  string  `json:"client_name" validate:"required"`
	ClientEmail string  `json:"client_email"`
	ClientPhone string  `json:"client_phone" validate:"required"`
	Source      string  `json:"source"`
	EventType   string  `json:"event_type"`
	EventDate   string  `json:"event_date"`
	GuestCount  int     `json:"guest_count"`
	BudgetMin   float64 `json:"budget_min"`
	BudgetMax   float64 `json:"budget_max"`
	Notes       string  `json:"notes"`
	AssignedTo  string  `json:"assigned_to"`
	Priority    string  `json:"priority"`
}

type CreateFollowupRequest struct {
	FollowUpDate string `json:"follow_up_date" validate:"required"`
	FollowUpType string `json:"follow_up_type" validate:"required"`
	Notes        string `json:"notes"`
}

type UpdateLeadStatusRequest struct {
	Status string `json:"status" validate:"required,oneof=new contacted qualified proposal negotiating won lost"`
}
