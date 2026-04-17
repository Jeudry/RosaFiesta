package store

import (
	"context"
	"database/sql"
	"strconv"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type LeadsStore struct {
	db *sql.DB
}

func (s *LeadsStore) CreateLead(ctx context.Context, lead *models.Lead) error {
	query := `
		INSERT INTO leads (source, status, priority, client_name, client_email, client_phone,
			event_type, event_date, guest_count, budget_min, budget_max, notes, assigned_to, next_follow_up)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
		RETURNING id, created_at, updated_at`

	return s.db.QueryRowContext(ctx, query,
		lead.Source, lead.Status, lead.Priority, lead.ClientName, lead.ClientEmail, lead.ClientPhone,
		lead.EventType, lead.EventDate, lead.GuestCount, lead.BudgetMin, lead.BudgetMax,
		lead.Notes, lead.AssignedTo, lead.NextFollowUp,
	).Scan(&lead.ID, &lead.CreatedAt, &lead.UpdatedAt)
}

func (s *LeadsStore) GetLeadByID(ctx context.Context, id uuid.UUID) (*models.Lead, error) {
	query := `
		SELECT id, source, status, priority, client_name, client_email, client_phone,
			event_type, event_date, guest_count, budget_min, budget_max, notes,
			assigned_to, last_contact_at, next_follow_up, converted_to_event_id, created_at, updated_at
		FROM leads WHERE id = $1`

	var lead models.Lead
	var assignedTo, convertedToEventID *uuid.UUID
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&lead.ID, &lead.Source, &lead.Status, &lead.Priority, &lead.ClientName,
		&lead.ClientEmail, &lead.ClientPhone, &lead.EventType, &lead.EventDate,
		&lead.GuestCount, &lead.BudgetMin, &lead.BudgetMax, &lead.Notes,
		&assignedTo, &lead.LastContactAt, &lead.NextFollowUp, &convertedToEventID,
		&lead.CreatedAt, &lead.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}
	lead.AssignedTo = assignedTo
	lead.ConvertedToEventID = convertedToEventID
	return &lead, nil
}

func (s *LeadsStore) GetLeads(ctx context.Context, status, assignedTo string, limit, offset int) ([]models.Lead, int, error) {
	countQuery := `SELECT COUNT(*) FROM leads WHERE 1=1`
	listQuery := `
		SELECT id, source, status, priority, client_name, client_email, client_phone,
			event_type, event_date, guest_count, budget_min, budget_max, notes,
			assigned_to, last_contact_at, next_follow_up, converted_to_event_id, created_at, updated_at
		FROM leads WHERE 1=1`

	args := []interface{}{}

	if status != "" {
		countQuery += ` AND status = $` + strconv.Itoa(len(args)+1)
		listQuery += ` AND status = $` + strconv.Itoa(len(args)+1)
		args = append(args, status)
	}
	if assignedTo != "" {
		countQuery += ` AND assigned_to = $` + strconv.Itoa(len(args)+1)
		listQuery += ` AND assigned_to = $` + strconv.Itoa(len(args)+1)
		args = append(args, assignedTo)
	}

	var total int
	if err := s.db.QueryRowContext(ctx, countQuery, args...).Scan(&total); err != nil {
		return nil, 0, err
	}

	listQuery += ` ORDER BY created_at DESC LIMIT $` + strconv.Itoa(len(args)+1) + ` OFFSET $` + strconv.Itoa(len(args)+2)
	args = append(args, limit, offset)

	rows, err := s.db.QueryContext(ctx, listQuery, args...)
	if err != nil {
		return nil, 0, err
	}
	defer rows.Close()

	var leads []models.Lead
	for rows.Next() {
		var lead models.Lead
		var aTo, convID *uuid.UUID
		if err := rows.Scan(
			&lead.ID, &lead.Source, &lead.Status, &lead.Priority, &lead.ClientName,
			&lead.ClientEmail, &lead.ClientPhone, &lead.EventType, &lead.EventDate,
			&lead.GuestCount, &lead.BudgetMin, &lead.BudgetMax, &lead.Notes,
			&aTo, &lead.LastContactAt, &lead.NextFollowUp, &convID,
			&lead.CreatedAt, &lead.UpdatedAt,
		); err != nil {
			return nil, 0, err
		}
		lead.AssignedTo = aTo
		lead.ConvertedToEventID = convID
		leads = append(leads, lead)
	}

	return leads, total, nil
}

func (s *LeadsStore) UpdateLeadStatus(ctx context.Context, id uuid.UUID, status string) error {
	query := `UPDATE leads SET status = $1, last_contact_at = NOW(), updated_at = NOW() WHERE id = $2`
	_, err := s.db.ExecContext(ctx, query, status, id)
	return err
}

func (s *LeadsStore) AddLeadFollowup(ctx context.Context, followup *models.LeadFollowup) error {
	query := `
		INSERT INTO lead_followups (lead_id, follow_up_date, follow_up_type, notes)
		VALUES ($1, $2, $3, $4)
		RETURNING id, created_at`
	return s.db.QueryRowContext(ctx, query,
		followup.LeadID, followup.FollowUpDate, followup.FollowUpType, followup.Notes,
	).Scan(&followup.ID, &followup.CreatedAt)
}

func (s *LeadsStore) GetLeadFollowups(ctx context.Context, leadID uuid.UUID) ([]models.LeadFollowup, error) {
	query := `
		SELECT id, lead_id, follow_up_date, follow_up_type, notes, completed, completed_at, created_at
		FROM lead_followups WHERE lead_id = $1 ORDER BY follow_up_date ASC`

	rows, err := s.db.QueryContext(ctx, query, leadID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var followups []models.LeadFollowup
	for rows.Next() {
		var f models.LeadFollowup
		if err := rows.Scan(&f.ID, &f.LeadID, &f.FollowUpDate, &f.FollowUpType,
			&f.Notes, &f.Completed, &f.CompletedAt, &f.CreatedAt); err != nil {
			return nil, err
		}
		followups = append(followups, f)
	}
	return followups, nil
}

func (s *LeadsStore) CompleteFollowup(ctx context.Context, id uuid.UUID) error {
	query := `UPDATE lead_followups SET completed = true, completed_at = NOW() WHERE id = $1`
	_, err := s.db.ExecContext(ctx, query, id)
	return err
}

func (s *LeadsStore) LogLeadActivity(ctx context.Context, leadID uuid.UUID, activityType, description string) error {
	query := `INSERT INTO lead_activities (lead_id, activity_type, description) VALUES ($1, $2, $3)`
	_, err := s.db.ExecContext(ctx, query, leadID, activityType, description)
	return err
}

func (s *LeadsStore) GetLeadActivities(ctx context.Context, leadID uuid.UUID) ([]models.LeadActivity, error) {
	query := `
		SELECT id, lead_id, activity_type, description, metadata, created_at
		FROM lead_activities WHERE lead_id = $1 ORDER BY created_at DESC`

	rows, err := s.db.QueryContext(ctx, query, leadID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var activities []models.LeadActivity
	for rows.Next() {
		var a models.LeadActivity
		if err := rows.Scan(&a.ID, &a.LeadID, &a.ActivityType, &a.Description, &a.Metadata, &a.CreatedAt); err != nil {
			return nil, err
		}
		activities = append(activities, a)
	}
	return activities, nil
}

func (s *LeadsStore) GetOverdueFollowups(ctx context.Context) ([]models.LeadFollowup, error) {
	query := `
		SELECT f.id, f.lead_id, f.follow_up_date, f.follow_up_type, f.notes, f.completed, f.completed_at, f.created_at
		FROM lead_followups f
		JOIN leads l ON f.lead_id = l.id
		WHERE f.completed = false AND f.follow_up_date < CURRENT_DATE
		ORDER BY f.follow_up_date ASC`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var followups []models.LeadFollowup
	for rows.Next() {
		var f models.LeadFollowup
		if err := rows.Scan(&f.ID, &f.LeadID, &f.FollowUpDate, &f.FollowUpType,
			&f.Notes, &f.Completed, &f.CompletedAt, &f.CreatedAt); err != nil {
			return nil, err
		}
		followups = append(followups, f)
	}
	return followups, nil
}

func (s *LeadsStore) GetLeadsStats(ctx context.Context) (*models.LeadStats, error) {
	stats := &models.LeadStats{}

	query := `SELECT
		COUNT(*) as total,
		COUNT(*) FILTER (WHERE status = 'new') as new_count,
		COUNT(*) FILTER (WHERE status = 'contacted') as contacted_count,
		COUNT(*) FILTER (WHERE status = 'qualified') as qualified_count,
		COUNT(*) FILTER (WHERE status = 'won') as won_count,
		COUNT(*) FILTER (WHERE status = 'lost') as lost_count
		FROM leads`

	err := s.db.QueryRowContext(ctx, query).Scan(
		&stats.Total, &stats.NewCount, &stats.ContactedCount,
		&stats.QualifiedCount, &stats.WonCount, &stats.LostCount,
	)
	if err != nil {
		return nil, err
	}

	if stats.Total > 0 {
		stats.ConversionRate = float64(stats.WonCount) / float64(stats.Total) * 100
	}

	return stats, nil
}
