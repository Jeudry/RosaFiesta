package store

import (
	"Backend/internal/store/models"
	"context"
	"database/sql"
)

type RolesStore struct {
	db *sql.DB
}

func (s *RolesStore) RetrieveByName(ctx context.Context, name string) (*models.Role, error) {
	query := `SELECT id, name, description, level FROM roles WHERE name = $1`

	role := &models.Role{}
	err := s.db.QueryRowContext(ctx, query, name).Scan(&role.ID, &role.Name, &role.Description, &role.Level)

	if err != nil {
		return nil, err
	}

	return role, nil
}
