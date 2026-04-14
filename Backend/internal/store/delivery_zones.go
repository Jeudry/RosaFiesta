package store

import (
	"context"
	"database/sql"
	"strings"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type DeliveryZonesStore struct {
	db *sql.DB
}

func (s *DeliveryZonesStore) GetAll(ctx context.Context) ([]models.DeliveryZone, error) {
	query := `
		SELECT id, name, base_radius_km, max_radius_km, travel_fee, is_active, created_at
		FROM delivery_zones
		WHERE is_active = true
		ORDER BY base_radius_km ASC
	`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var zones []models.DeliveryZone
	for rows.Next() {
		var zone models.DeliveryZone
		err := rows.Scan(
			&zone.ID,
			&zone.Name,
			&zone.BaseRadiusKm,
			&zone.MaxRadiusKm,
			&zone.TravelFee,
			&zone.IsActive,
			&zone.CreatedAt,
		)
		if err != nil {
			return nil, err
		}
		zones = append(zones, zone)
	}

	if zones == nil {
		zones = []models.DeliveryZone{}
	}

	return zones, nil
}

func (s *DeliveryZonesStore) CalculateFee(ctx context.Context, address string) (*models.DeliveryFeeResponse, error) {
	address = strings.ToLower(address)

	// Keywords that indicate remote zones (outside San Cristóbal province)
	remoteKeywords := []string{
		"santo domingo", "santiago", "la romana", "la altagracia", "punta cana",
		"bayahibe", "boca chica", "san pedro de macoris", "samana", "las galeras",
		"puerto plata", "sosua", "cabarete", "rio san juan", "miches", "nagua",
		"higuey", "el seibo", "san jose de cofrentes", "hato mayor", "sabana de la mar",
		"dominican republic", "rep. dom.", "rd", "+1", "phone:", "whatsapp",
	}

	// Keywords that indicate San Cristóbal area (free/extended delivery)
	localKeywords := []string{
		"san cristobal", "san Cristóbal", "santa cruz", "santa Cruz",
		" Los Ranchos", "ranchitos", "La Valentina", "Hatillo",
		"Berjon", "Mendoza", "Pizarro", "San Juan", "Macoris",
		"km 27", "km 30", "carretera san cristobal", "av. libardo",
	}

	// Check for remote keywords first
	isRemote := false
	for _, keyword := range remoteKeywords {
		if strings.Contains(address, keyword) {
			isRemote = true
			break
		}
	}

	// Check for local keywords (they override remote detection)
	isLocal := false
	for _, keyword := range localKeywords {
		if strings.Contains(address, keyword) {
			isLocal = true
			break
		}
	}

	var response models.DeliveryFeeResponse
	if isLocal {
		// San Cristóbal Centro - free or low cost delivery
		response = models.DeliveryFeeResponse{
			Fee:     0,
			Zone:    "San Cristóbal Centro",
			Message: "Delivery gratuito en San Cristóbal",
		}
	} else if isRemote {
		// Remote zone - high fee
		response = models.DeliveryFeeResponse{
			Fee:     3500,
			Zone:    "Zona Remota",
			Message: "Tu dirección está en zona remota. El equipo de RosaFiesta coordinará contigo el envío.",
		}
	} else {
		// Default - extended zone
		response = models.DeliveryFeeResponse{
			Fee:     1500,
			Zone:    "San Cristóbal Extendido",
			Message: "Delivery dentro de la provincia de San Cristóbal",
		}
	}

	return &response, nil
}

func (s *DeliveryZonesStore) GetByID(ctx context.Context, id uuid.UUID) (*models.DeliveryZone, error) {
	query := `
		SELECT id, name, base_radius_km, max_radius_km, travel_fee, is_active, created_at
		FROM delivery_zones
		WHERE id = $1
	`

	var zone models.DeliveryZone
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&zone.ID,
		&zone.Name,
		&zone.BaseRadiusKm,
		&zone.MaxRadiusKm,
		&zone.TravelFee,
		&zone.IsActive,
		&zone.CreatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, ErrNotFound
		}
		return nil, err
	}

	return &zone, nil
}