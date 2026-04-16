package models

import (
	"time"

	"github.com/google/uuid"
)

type MaintenanceType string

const (
	MaintenanceTypeCleaning     MaintenanceType = "cleaning"
	MaintenanceTypeRepair      MaintenanceType = "repair"
	MaintenanceTypeInspection  MaintenanceType = "inspection"
	MaintenanceTypeReplacement MaintenanceType = "replacement"
)

type MaintenanceStatus string

const (
	MaintenanceStatusScheduled  MaintenanceStatus = "scheduled"
	MaintenanceStatusInProgress MaintenanceStatus = "in_progress"
	MaintenanceStatusCompleted MaintenanceStatus = "completed"
	MaintenanceStatusCancelled MaintenanceStatus = "cancelled"
)

type ArticleMaintenanceLog struct {
	ID                 uuid.UUID        `json:"id"`
	ArticleID          uuid.UUID        `json:"article_id"`
	VariantID          *uuid.UUID       `json:"variant_id,omitempty"`
	MaintenanceType    MaintenanceType  `json:"maintenance_type"`
	Status             MaintenanceStatus `json:"status"`
	Description        string           `json:"description,omitempty"`
	PerformedBy        string           `json:"performed_by,omitempty"`
	PerformedAt        *time.Time       `json:"performed_at,omitempty"`
	NextMaintenanceDue *time.Time       `json:"next_maintenance_due,omitempty"`
	Cost               *float64         `json:"cost,omitempty"`
	CreatedAt          time.Time        `json:"created_at"`
	UpdatedAt          time.Time        `json:"updated_at"`
	CreatedBy          *uuid.UUID       `json:"created_by,omitempty"`
	Article            *Article         `json:"article,omitempty"`
}
