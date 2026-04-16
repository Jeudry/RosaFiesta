package store

import (
	"context"
	"database/sql"
	"errors"
	"strconv"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type MaintenanceLogsStore struct {
	db *sql.DB
}

func (s *MaintenanceLogsStore) Create(ctx context.Context, log *models.ArticleMaintenanceLog) error {
	query := `
		INSERT INTO article_maintenance_logs (id, article_id, variant_id, maintenance_type, status,
		                                     description, performed_by, performed_at, next_maintenance_due, cost, created_by)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
		RETURNING created_at, updated_at`

	if log.ID == uuid.Nil {
		log.ID = uuid.New()
	}
	return s.db.QueryRowContext(ctx, query,
		log.ID, log.ArticleID, log.VariantID, log.MaintenanceType, log.Status,
		log.Description, log.PerformedBy, log.PerformedAt, log.NextMaintenanceDue, log.Cost, log.CreatedBy,
	).Scan(&log.CreatedAt, &log.UpdatedAt)
}

func (s *MaintenanceLogsStore) GetAll(ctx context.Context, status, maintType string) ([]models.ArticleMaintenanceLog, error) {
	query := `
		SELECT ml.id, ml.article_id, ml.variant_id, ml.maintenance_type, ml.status,
		       COALESCE(ml.description, ''), COALESCE(ml.performed_by, ''), ml.performed_at,
		       ml.next_maintenance_due, ml.cost, ml.created_at, ml.updated_at, ml.created_by,
		       a.name_template
		FROM article_maintenance_logs ml
		JOIN articles a ON ml.article_id = a.id
		WHERE 1=1`

	args := []interface{}{}
	argIdx := 1
	if status != "" {
		query += ` AND ml.status = $` + itoa(argIdx)
		args = append(args, status)
		argIdx++
	}
	if maintType != "" {
		query += ` AND ml.maintenance_type = $` + itoa(argIdx)
		args = append(args, maintType)
	}
	query += ` ORDER BY ml.created_at DESC`

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var logs []models.ArticleMaintenanceLog
	for rows.Next() {
		var log models.ArticleMaintenanceLog
		var variantID, createdBy sql.NullString
		var desc, performedBy sql.NullString
		var performedAt, nextDue sql.NullTime
		var cost sql.NullFloat64
		var articleName sql.NullString

		if err := rows.Scan(
			&log.ID, &log.ArticleID, &variantID, &log.MaintenanceType, &log.Status,
			&desc, &performedBy, &performedAt, &nextDue, &cost, &log.CreatedAt, &log.UpdatedAt, &createdBy, &articleName,
		); err != nil {
			return nil, err
		}
		log.Description = desc.String
		log.PerformedBy = performedBy.String
		if variantID.Valid {
			id, _ := uuid.Parse(variantID.String)
			log.VariantID = &id
		}
		if createdBy.Valid {
			id, _ := uuid.Parse(createdBy.String)
			log.CreatedBy = &id
		}
		if performedAt.Valid {
			log.PerformedAt = &performedAt.Time
		}
		if nextDue.Valid {
			log.NextMaintenanceDue = &nextDue.Time
		}
		if cost.Valid {
			log.Cost = &cost.Float64
		}
		logs = append(logs, log)
	}
	return logs, nil
}

func (s *MaintenanceLogsStore) GetByArticleID(ctx context.Context, articleID uuid.UUID) ([]models.ArticleMaintenanceLog, error) {
	query := `
		SELECT id, article_id, variant_id, maintenance_type, status,
		       COALESCE(description, ''), COALESCE(performed_by, ''), performed_at,
		       next_maintenance_due, cost, created_at, updated_at, created_by
		FROM article_maintenance_logs
		WHERE article_id = $1
		ORDER BY created_at DESC`

	rows, err := s.db.QueryContext(ctx, query, articleID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var logs []models.ArticleMaintenanceLog
	for rows.Next() {
		var log models.ArticleMaintenanceLog
		var variantID, createdBy sql.NullString
		var desc, performedBy sql.NullString
		var performedAt, nextDue sql.NullTime
		var cost sql.NullFloat64

		if err := rows.Scan(
			&log.ID, &log.ArticleID, &variantID, &log.MaintenanceType, &log.Status,
			&desc, &performedBy, &performedAt, &nextDue, &cost, &log.CreatedAt, &log.UpdatedAt, &createdBy,
		); err != nil {
			return nil, err
		}
		log.Description = desc.String
		log.PerformedBy = performedBy.String
		if variantID.Valid {
			id, _ := uuid.Parse(variantID.String)
			log.VariantID = &id
		}
		if createdBy.Valid {
			id, _ := uuid.Parse(createdBy.String)
			log.CreatedBy = &id
		}
		if performedAt.Valid {
			log.PerformedAt = &performedAt.Time
		}
		if nextDue.Valid {
			log.NextMaintenanceDue = &nextDue.Time
		}
		if cost.Valid {
			log.Cost = &cost.Float64
		}
		logs = append(logs, log)
	}
	return logs, nil
}

func (s *MaintenanceLogsStore) GetOverdue(ctx context.Context) ([]models.ArticleMaintenanceLog, error) {
	query := `
		SELECT ml.id, ml.article_id, ml.variant_id, ml.maintenance_type, ml.status,
		       COALESCE(ml.description, ''), COALESCE(ml.performed_by, ''), ml.performed_at,
		       ml.next_maintenance_due, ml.cost, ml.created_at, ml.updated_at, ml.created_by,
		       a.name_template
		FROM article_maintenance_logs ml
		JOIN articles a ON ml.article_id = a.id
		WHERE ml.next_maintenance_due < $1
		  AND ml.status NOT IN ('completed', 'cancelled')
		ORDER BY ml.next_maintenance_due ASC`

	rows, err := s.db.QueryContext(ctx, query, time.Now())
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var logs []models.ArticleMaintenanceLog
	for rows.Next() {
		var log models.ArticleMaintenanceLog
		var variantID, createdBy sql.NullString
		var desc, performedBy sql.NullString
		var performedAt, nextDue sql.NullTime
		var cost sql.NullFloat64

		if err := rows.Scan(
			&log.ID, &log.ArticleID, &variantID, &log.MaintenanceType, &log.Status,
			&desc, &performedBy, &performedAt, &nextDue, &cost, &log.CreatedAt, &log.UpdatedAt, &createdBy,
		); err != nil {
			return nil, err
		}
		if variantID.Valid {
			id, _ := uuid.Parse(variantID.String)
			log.VariantID = &id
		}
		if nextDue.Valid {
			log.NextMaintenanceDue = &nextDue.Time
		}
		if cost.Valid {
			log.Cost = &cost.Float64
		}
		logs = append(logs, log)
	}
	return logs, nil
}

func (s *MaintenanceLogsStore) Update(ctx context.Context, log *models.ArticleMaintenanceLog) error {
	query := `
		UPDATE article_maintenance_logs
		SET status = $2, performed_by = $3, performed_at = $4,
		    next_maintenance_due = $5, cost = $6, updated_at = NOW()
		WHERE id = $1`
	_, err := s.db.ExecContext(ctx, query,
		log.ID, log.Status, log.PerformedBy, log.PerformedAt, log.NextMaintenanceDue, log.Cost,
	)
	return err
}

func (s *MaintenanceLogsStore) GetByID(ctx context.Context, id uuid.UUID) (*models.ArticleMaintenanceLog, error) {
	query := `
		SELECT id, article_id, variant_id, maintenance_type, status,
		       COALESCE(description, ''), COALESCE(performed_by, ''), performed_at,
		       next_maintenance_due, cost, created_at, updated_at, created_by
		FROM article_maintenance_logs WHERE id = $1`

	var log models.ArticleMaintenanceLog
	var variantID, createdBy sql.NullString
	var desc, performedBy sql.NullString
	var performedAt, nextDue sql.NullTime
	var cost sql.NullFloat64

	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&log.ID, &log.ArticleID, &variantID, &log.MaintenanceType, &log.Status,
		&desc, &performedBy, &performedAt, &nextDue, &cost, &log.CreatedAt, &log.UpdatedAt, &createdBy,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}
	log.Description = desc.String
	log.PerformedBy = performedBy.String
	if variantID.Valid {
		id, _ := uuid.Parse(variantID.String)
		log.VariantID = &id
	}
	if createdBy.Valid {
		id, _ := uuid.Parse(createdBy.String)
		log.CreatedBy = &id
	}
	if performedAt.Valid {
		log.PerformedAt = &performedAt.Time
	}
	if nextDue.Valid {
		log.NextMaintenanceDue = &nextDue.Time
	}
	if cost.Valid {
		log.Cost = &cost.Float64
	}
	return &log, nil
}

func itoa(i int) string {
	return strconv.Itoa(i)
}
