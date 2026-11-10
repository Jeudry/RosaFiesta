package postgres

import (
	"context"
	"strings"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	"github.com/Jeudry/rosafiesta/server/internal/operations/postgres/sqlcgen"
)

type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{}
	}
	return &Repository{pool: pool, queries: sqlcgen.New(pool)}
}

func (r *Repository) Enabled() bool {
	if r == nil {
		return false
	}
	return r.pool != nil
}

func (r *Repository) SeedCanonical(ctx context.Context, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	if err := r.seedFieldLogistics(ctx, now); err != nil {
		return err
	}
	if err := r.seedCRMAndReviews(ctx, now); err != nil {
		return err
	}
	if err := r.seedSettingsAndGovernance(ctx, now); err != nil {
		return err
	}
	return r.seedWhatsAppBot(ctx)
}

func (r *Repository) seedFieldLogistics(ctx context.Context, now time.Time) error {
	for _, logi := range domain.CanonicalLogistics() {
		for _, item := range logi.Checklist {
			if err := r.UpsertChecklistItem(ctx, logi.EventID, item); err != nil {
				return err
			}
		}
		for idx, report := range logi.DamageReports {
			ts := now.Add(time.Duration(idx) * time.Second)
			if err := r.RecordDamageIncident(ctx, report, ts); err != nil {
				return err
			}
		}
	}
	for idx, mutation := range domain.CanonicalOfflineQueue() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertOfflineMutation(ctx, mutation, ts); err != nil {
			return err
		}
	}
	return nil
}

func (r *Repository) UpsertChecklistItem(ctx context.Context, eventID string, item domain.ChecklistItem) error {
	if !r.Enabled() {
		return nil
	}
	stage := strings.TrimSpace(string(item.Stage))
	if stage == "" {
		stage = string(domain.StageOnSiteSetup)
	}
	params := sqlcgen.UpsertChecklistTaskParams{
		ID:               item.ID,
		EventID:          eventID,
		Stage:            stage,
		Title:            item.Title,
		AssigneeName:     item.CompletedBy,
		Completed:        item.Completed,
		CompletedAtLabel: item.CompletedAt,
	}
	return r.queries.UpsertChecklistTask(ctx, params)
}

func (r *Repository) ListChecklistItems(ctx context.Context, eventID string) (map[string]domain.ChecklistItem, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListChecklistTasksByEvent(ctx, eventID)
	if err != nil {
		return nil, err
	}
	items := make(map[string]domain.ChecklistItem, len(rows))
	for _, row := range rows {
		stage := domain.ChecklistStage(row.Stage)
		if stage == "" {
			stage = domain.StageOnSiteSetup
		}
		items[row.ID] = domain.ChecklistItem{
			ID:          row.ID,
			Stage:       stage,
			Title:       row.Title,
			Completed:   row.Completed,
			CompletedBy: row.AssigneeName,
			CompletedAt: row.CompletedAtLabel,
		}
	}
	return items, nil
}

func (r *Repository) RecordDamageIncident(ctx context.Context, report domain.DamageReport, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertDamageIncidentParams{
		ID:             report.ID,
		EventID:        report.EventID,
		ArticleCode:    report.ArticleCode,
		ArticleName:    report.ArticleName,
		DamagedUnits:   report.DamagedUnits,
		MissingUnits:   report.MissingUnits,
		ReplacementDop: report.ReplacementRD,
		Notes:          report.Notes,
		Status:         "open",
		CreatedAt:      now,
	}
	return r.queries.UpsertDamageIncident(ctx, params)
}

func (r *Repository) ListDamageIncidents(ctx context.Context, eventID string) ([]domain.DamageReport, error) {
	if !r.Enabled() {
		return nil, nil
	}
	trimmedEvent := strings.TrimSpace(eventID)
	var (
		rows []sqlcgen.DamageIncident
		err  error
	)
	if trimmedEvent != "" {
		rows, err = r.queries.ListDamageIncidentsByEvent(ctx, trimmedEvent)
	} else {
		rows, err = r.queries.ListDamageIncidents(ctx)
	}
	if err != nil {
		return nil, err
	}
	out := make([]domain.DamageReport, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.DamageReport{
			ID:            row.ID,
			EventID:       row.EventID,
			ArticleCode:   row.ArticleCode,
			ArticleName:   row.ArticleName,
			DamagedUnits:  row.DamagedUnits,
			MissingUnits:  row.MissingUnits,
			ReplacementRD: row.ReplacementDop,
			Notes:         row.Notes,
		})
	}
	return out, nil
}

func (r *Repository) UpsertOfflineMutation(ctx context.Context, mutation domain.OfflineMutation, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertOfflineMutationParams{
		ID:             mutation.ID,
		ActionLabel:    mutation.ActionLabel,
		EntityCode:     mutation.EntityCode,
		CreatedAtLabel: mutation.CreatedAt,
		Synced:         mutation.Synced,
		CreatedAt:      now,
	}
	return r.queries.UpsertOfflineMutation(ctx, params)
}

func (r *Repository) ListOfflineMutations(ctx context.Context) ([]domain.OfflineMutation, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListOfflineMutations(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.OfflineMutation, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.OfflineMutation{
			ID:          row.ID,
			ActionLabel: row.ActionLabel,
			EntityCode:  row.EntityCode,
			CreatedAt:   row.CreatedAtLabel,
			Synced:      row.Synced,
		})
	}
	return out, nil
}

func (r *Repository) MarkAllOfflineMutationsSynced(ctx context.Context) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.MarkAllOfflineMutationsSynced(ctx)
}
