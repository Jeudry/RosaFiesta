// Package postgres implementa la persistencia PostgreSQL del módulo de operaciones y logística.
package postgres

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	"github.com/Jeudry/rosafiesta/server/internal/operations/postgres/sqlcgen"
)

// Repository persiste logística, incidencias, colas fuera de línea, CRM y bot WhatsApp.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository crea una instancia de Repository sobre pgxpool.
func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{}
	}
	return &Repository{pool: pool, queries: sqlcgen.New(pool)}
}

// Enabled indica si el repositorio cuenta con conexión activa a PostgreSQL.
func (r *Repository) Enabled() bool {
	if r == nil {
		return false
	}
	if r.pool != nil {
		return true
	}
	return r.queries != nil
}

// SeedCanonical inserta los datos canónicos de demostración en PostgreSQL.
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
	seededEvents := make(map[string]struct{})
	for _, logi := range domain.CanonicalLogistics() {
		seededEvents[logi.EventID] = struct{}{}
		if err := r.UpsertEventLogistics(ctx, logi, logi.Venue, "en_curso", logi.DateLabel); err != nil {
			return err
		}
		if err := r.seedLogisticsChildren(ctx, logi, now); err != nil {
			return err
		}
	}
	if err := r.seedCanonicalRouteStops(ctx, seededEvents); err != nil {
		return err
	}
	for idx, mutation := range domain.CanonicalOfflineQueue() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertOfflineMutation(ctx, mutation, ts); err != nil {
			return err
		}
	}
	return nil
}

func (r *Repository) seedLogisticsChildren(ctx context.Context, logi domain.EventLogistics, now time.Time) error {
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
	return nil
}

func (r *Repository) seedCanonicalRouteStops(ctx context.Context, seededEvents map[string]struct{}) error {
	grouped := make(map[string][]domain.RouteStop)
	for _, stop := range domain.CanonicalRouteStops() {
		if _, exists := seededEvents[stop.EventID]; !exists {
			seededEvents[stop.EventID] = struct{}{}
			stub := domain.EventLogistics{
				EventID:     stop.EventID,
				EventTitle:  stop.EventTitle,
				Venue:       stop.Venue,
				SetupWindow: stop.WindowLabel,
				VehicleName: stop.AssignedVehicle,
			}
			if err := r.UpsertEventLogistics(ctx, stub, stop.Venue, stop.Status, ""); err != nil {
				return err
			}
		}
		grouped[stop.EventID] = append(grouped[stop.EventID], stop)
	}
	for eventID, stops := range grouped {
		if err := r.ReplaceDeliveryStops(ctx, eventID, stops); err != nil {
			return err
		}
	}
	return nil
}

// UpsertEventLogistics guarda o actualiza la ficha logística principal de un evento.
func (r *Repository) UpsertEventLogistics(
	ctx context.Context,
	logi domain.EventLogistics,
	zone string,
	status string,
	updatedAtLabel string,
) error {
	if !r.Enabled() {
		return nil
	}
	effectiveZone := strings.TrimSpace(zone)
	if effectiveZone == "" {
		effectiveZone = strings.TrimSpace(logi.Venue)
	}
	effectiveStatus := strings.TrimSpace(status)
	if effectiveStatus == "" {
		effectiveStatus = "programada"
	}
	return r.queries.UpsertEventLogistics(ctx, sqlcgen.UpsertEventLogisticsParams{
		EventID:        logi.EventID,
		EventTitle:     logi.EventTitle,
		Zone:           effectiveZone,
		WindowLabel:    logi.SetupWindow,
		DriverName:     logi.LeadStaffName,
		Vehicle:        logi.VehicleName,
		Status:         effectiveStatus,
		UpdatedAtLabel: updatedAtLabel,
	})
}

// ListEventLogistics lista las fichas logísticas almacenadas en PostgreSQL.
func (r *Repository) ListEventLogistics(ctx context.Context) ([]domain.EventLogistics, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListEventLogistics(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.EventLogistics, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.EventLogistics{
			EventID:       row.EventID,
			EventTitle:    row.EventTitle,
			DateLabel:     row.UpdatedAtLabel,
			SetupWindow:   row.WindowLabel,
			Venue:         row.Zone,
			LeadStaffName: row.DriverName,
			VehicleName:   row.Vehicle,
		})
	}
	return out, nil
}

// ReplaceDeliveryStops reemplaza las paradas de entrega/recogida asociadas a un evento.
func (r *Repository) ReplaceDeliveryStops(ctx context.Context, eventID string, stops []domain.RouteStop) error {
	if !r.Enabled() {
		return nil
	}
	if err := r.queries.DeleteDeliveryStopsByEvent(ctx, eventID); err != nil {
		return err
	}
	for idx, stop := range stops {
		order := stop.Order
		if order <= 0 {
			order = idx + 1
		}
		err := r.queries.UpsertDeliveryStop(ctx, sqlcgen.UpsertDeliveryStopParams{
			ID:        fmt.Sprintf("stop-%s-%d", eventID, order),
			EventID:   eventID,
			StopOrder: order,
			Title:     stop.EventTitle,
			Address:   stop.Venue,
			TimeLabel: stop.WindowLabel,
			Completed: stop.Status == "completado",
		})
		if err != nil {
			return err
		}
	}
	return nil
}

// ListDeliveryStops obtiene todas las paradas logísticas registradas en PostgreSQL.
func (r *Repository) ListDeliveryStops(ctx context.Context) ([]domain.RouteStop, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListAllDeliveryStops(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.RouteStop, 0, len(rows))
	for _, row := range rows {
		status := "programado"
		if row.Completed {
			status = "completado"
		}
		out = append(out, domain.RouteStop{
			Order:       row.StopOrder,
			EventID:     row.EventID,
			EventTitle:  row.Title,
			Venue:       row.Address,
			WindowLabel: row.TimeLabel,
			Status:      status,
		})
	}
	return out, nil
}

// UpsertChecklistItem inserta o actualiza el estado de una tarea operativa del checklist.
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

// ListChecklistItems obtiene los ítems de checklist persistidos para un evento.
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

// RecordDamageIncident inserta o actualiza una incidencia de daño o extravío de equipo.
func (r *Repository) RecordDamageIncident(ctx context.Context, report domain.DamageReport, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	status := strings.TrimSpace(report.Status)
	if status == "" {
		status = "open"
	}
	params := sqlcgen.UpsertDamageIncidentParams{
		ID:              report.ID,
		EventID:         report.EventID,
		ArticleCode:     report.ArticleCode,
		ArticleName:     report.ArticleName,
		DamagedUnits:    report.DamagedUnits,
		MissingUnits:    report.MissingUnits,
		ReplacementDop:  report.ReplacementRD,
		Notes:           report.Notes,
		PhotoUrl:        report.PhotoURL,
		ResolutionNotes: report.ResolutionNotes,
		ChargePaymentID: report.ChargePaymentID,
		Status:          status,
		CreatedAt:       now,
	}
	return r.queries.UpsertDamageIncident(ctx, params)
}

// ListDamageIncidents lista las incidencias registradas, opcionalmente filtradas por evento.
func (r *Repository) ListDamageIncidents(ctx context.Context, eventID string) ([]domain.DamageReport, error) {
	if !r.Enabled() {
		return nil, nil
	}
	trimmedEvent := strings.TrimSpace(eventID)
	if trimmedEvent != "" {
		rows, err := r.queries.ListDamageIncidentsByEvent(ctx, trimmedEvent)
		if err != nil {
			return nil, err
		}
		return mapDamageRowsByEvent(rows), nil
	}
	rows, err := r.queries.ListDamageIncidents(ctx)
	if err != nil {
		return nil, err
	}
	return mapDamageRowsAll(rows), nil
}

func mapDamageRowsByEvent(rows []sqlcgen.ListDamageIncidentsByEventRow) []domain.DamageReport {
	out := make([]domain.DamageReport, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.DamageReport{
			ID:              row.ID,
			EventID:         row.EventID,
			ArticleCode:     row.ArticleCode,
			ArticleName:     row.ArticleName,
			DamagedUnits:    row.DamagedUnits,
			MissingUnits:    row.MissingUnits,
			ReplacementRD:   row.ReplacementDop,
			Notes:           row.Notes,
			PhotoURL:        row.PhotoUrl,
			ResolutionNotes: row.ResolutionNotes,
			ChargePaymentID: row.ChargePaymentID,
			Status:          row.Status,
		})
	}
	return out
}

func mapDamageRowsAll(rows []sqlcgen.ListDamageIncidentsRow) []domain.DamageReport {
	out := make([]domain.DamageReport, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.DamageReport{
			ID:              row.ID,
			EventID:         row.EventID,
			ArticleCode:     row.ArticleCode,
			ArticleName:     row.ArticleName,
			DamagedUnits:    row.DamagedUnits,
			MissingUnits:    row.MissingUnits,
			ReplacementRD:   row.ReplacementDop,
			Notes:           row.Notes,
			PhotoURL:        row.PhotoUrl,
			ResolutionNotes: row.ResolutionNotes,
			ChargePaymentID: row.ChargePaymentID,
			Status:          row.Status,
		})
	}
	return out
}

// UpsertOfflineMutation inserta o actualiza una mutación de campo fuera de línea.
func (r *Repository) UpsertOfflineMutation(ctx context.Context, mutation domain.OfflineMutation, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	payloadBytes := []byte(strings.TrimSpace(mutation.PayloadJSON))
	if len(payloadBytes) == 0 {
		payloadBytes = []byte("{}")
	}
	params := sqlcgen.UpsertOfflineMutationParams{
		ID:             mutation.ID,
		ActionLabel:    mutation.ActionLabel,
		EntityCode:     mutation.EntityCode,
		OperationType:  mutation.OperationType,
		PayloadJson:    payloadBytes,
		IdempotencyKey: mutation.IdempotencyKey,
		CreatedAtLabel: mutation.CreatedAt,
		Synced:         mutation.Synced,
		CreatedAt:      now,
	}
	return r.queries.UpsertOfflineMutation(ctx, params)
}

// ListOfflineMutations obtiene las mutaciones registradas en la cola fuera de línea.
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
		payloadText := strings.TrimSpace(string(row.PayloadJson))
		if payloadText == "{}" {
			payloadText = ""
		}
		out = append(out, domain.OfflineMutation{
			ID:             row.ID,
			ActionLabel:    row.ActionLabel,
			EntityCode:     row.EntityCode,
			OperationType:  row.OperationType,
			PayloadJSON:    payloadText,
			IdempotencyKey: row.IdempotencyKey,
			CreatedAt:      row.CreatedAtLabel,
			Synced:         row.Synced,
		})
	}
	return out, nil
}

// MarkAllOfflineMutationsSynced marca todas las mutaciones de la cola como sincronizadas.
func (r *Repository) MarkAllOfflineMutationsSynced(ctx context.Context) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.MarkAllOfflineMutationsSynced(ctx)
}
