package models

import (
	"time"

	"github.com/google/uuid"
)

type DeliveryZone struct {
	ID           uuid.UUID `json:"id"`
	Name         string    `json:"name"`
	BaseRadiusKm float64   `json:"base_radius_km"`
	MaxRadiusKm  float64   `json:"max_radius_km"`
	TravelFee    float64   `json:"travel_fee"`
	IsActive     bool      `json:"is_active"`
	CreatedAt   time.Time `json:"created_at"`
}

type DeliveryFeeRequest struct {
	Address string `json:"address" validate:"required"`
}

type DeliveryFeeResponse struct {
	Fee     int    `json:"fee"`
	Zone    string `json:"zone"`
	Message string `json:"message"`
}