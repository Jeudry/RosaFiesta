package postgres

import (
	"context"
	"fmt"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	"github.com/Jeudry/rosafiesta/server/internal/operations/postgres/sqlcgen"
)

func (r *Repository) seedCRMAndReviews(ctx context.Context, now time.Time) error {
	for idx, lead := range domain.CanonicalLeads() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertLead(ctx, lead, ts); err != nil {
			return err
		}
	}
	for _, sup := range domain.CanonicalSuppliers() {
		if err := r.UpsertSupplier(ctx, sup); err != nil {
			return err
		}
	}
	for idx, rev := range domain.CanonicalReviews() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertReview(ctx, rev, ts); err != nil {
			return err
		}
	}
	return nil
}

func (r *Repository) seedSettingsAndGovernance(ctx context.Context, now time.Time) error {
	if err := r.UpsertBusinessSettings(ctx, domain.CanonicalBusinessSettings(), now); err != nil {
		return err
	}
	for _, zone := range domain.CanonicalDeliveryZones() {
		if err := r.UpsertDeliveryZone(ctx, zone); err != nil {
			return err
		}
	}
	for idx, ntf := range domain.CanonicalNotifications() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertNotification(ctx, ntf, ts); err != nil {
			return err
		}
	}
	for _, pol := range domain.CanonicalInsurancePolicies() {
		if err := r.UpsertInsurancePolicy(ctx, pol); err != nil {
			return err
		}
	}
	for idx, aud := range domain.CanonicalAuditLogs() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertAuditLog(ctx, aud, ts); err != nil {
			return err
		}
	}
	return nil
}

func (r *Repository) seedWhatsAppBot(ctx context.Context) error {
	for _, faq := range domain.CanonicalBotFAQs() {
		if err := r.UpsertBotFAQ(ctx, faq); err != nil {
			return err
		}
	}
	for _, kw := range domain.CanonicalBotKeywords() {
		if err := r.UpsertBotKeyword(ctx, kw); err != nil {
			return err
		}
	}
	for _, th := range domain.CanonicalBotThreads() {
		if err := r.UpsertBotThread(ctx, th); err != nil {
			return err
		}
	}
	return nil
}

func (r *Repository) UpsertLead(ctx context.Context, lead domain.Lead, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertLeadParams{
		ID:             lead.ID,
		FullName:       lead.FullName,
		Phone:          lead.Phone,
		Email:          lead.Email,
		EventType:      lead.EventType,
		EventDateLabel: lead.EventDateLabel,
		Guests:         lead.Guests,
		EstimatedDop:   lead.EstimatedDop,
		Stage:          lead.Stage,
		Notes:          lead.Notes,
		CreatedAt:      now,
	}
	return r.queries.UpsertLead(ctx, params)
}

func (r *Repository) ListLeads(ctx context.Context) ([]domain.Lead, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListLeads(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Lead, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.Lead{
			ID:             row.ID,
			FullName:       row.FullName,
			Phone:          row.Phone,
			Email:          row.Email,
			EventType:      row.EventType,
			EventDateLabel: row.EventDateLabel,
			Guests:         row.Guests,
			EstimatedDop:   row.EstimatedDop,
			Stage:          row.Stage,
			Notes:          row.Notes,
		})
	}
	return out, nil
}

func (r *Repository) UpsertSupplier(ctx context.Context, sup domain.Supplier) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertSupplierParams{
		ID:          sup.ID,
		Name:        sup.Name,
		Category:    sup.Category,
		Specialty:   sup.Specialty,
		ContactName: sup.ContactName,
		Phone:       sup.Phone,
		Email:       sup.Email,
		Website:     sup.Website,
		Rating:      sup.Rating,
		Notes:       sup.Notes,
	}
	return r.queries.UpsertSupplier(ctx, params)
}

func (r *Repository) ListSuppliers(ctx context.Context) ([]domain.Supplier, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListSuppliers(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Supplier, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.Supplier{
			ID:          row.ID,
			Name:        row.Name,
			Category:    row.Category,
			Specialty:   row.Specialty,
			ContactName: row.ContactName,
			Phone:       row.Phone,
			Email:       row.Email,
			Website:     row.Website,
			Rating:      row.Rating,
			Notes:       row.Notes,
		})
	}
	return out, nil
}

func (r *Repository) UpsertReview(ctx context.Context, rev domain.Review, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertReviewParams{
		ID:         rev.ID,
		EventID:    rev.EventID,
		ClientName: rev.ClientName,
		Rating:     rev.Rating,
		Comment:    rev.Comment,
		AdminReply: rev.AdminReply,
		CreatedAt:  now,
	}
	return r.queries.UpsertReview(ctx, params)
}

func (r *Repository) ListReviews(ctx context.Context) ([]domain.Review, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListReviews(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Review, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.Review{
			ID:             row.ID,
			EventID:        row.EventID,
			ClientName:     row.ClientName,
			Rating:         row.Rating,
			Comment:        row.Comment,
			AdminReply:     row.AdminReply,
			CreatedAtLabel: formatShortSpanishDate(row.CreatedAt),
		})
	}
	return out, nil
}

func formatShortSpanishDate(ts time.Time) string {
	months := [12]string{
		"ene", "feb", "mar", "abr", "may", "jun",
		"jul", "ago", "sep", "oct", "nov", "dic",
	}
	idx := int(ts.Month()) - 1
	if idx < 0 {
		idx = 0
	}
	if idx >= len(months) {
		idx = len(months) - 1
	}
	return fmt.Sprintf("%d %s", ts.Day(), months[idx])
}

func (r *Repository) UpsertBusinessSettings(ctx context.Context, settings domain.BusinessSettings, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	id := settings.ID
	if id == "" {
		id = "default"
	}
	params := sqlcgen.UpsertBusinessSettingsParams{
		ID:             id,
		CompanyName:    settings.CompanyName,
		Rnc:            settings.RNC,
		Address:        settings.Address,
		Phone:          settings.Phone,
		Whatsapp:       settings.WhatsApp,
		Email:          settings.Email,
		Instagram:      settings.Instagram,
		Facebook:       settings.Facebook,
		BusinessHours:  settings.BusinessHours,
		ItbisPercent:   settings.ItbisPercent,
		DepositPercent: settings.DepositPercent,
		UpdatedAt:      now,
	}
	return r.queries.UpsertBusinessSettings(ctx, params)
}

func (r *Repository) GetBusinessSettings(ctx context.Context) (domain.BusinessSettings, error) {
	if !r.Enabled() {
		return domain.CanonicalBusinessSettings(), nil
	}
	row, err := r.queries.GetBusinessSettings(ctx)
	if err != nil {
		return domain.BusinessSettings{}, err
	}
	return domain.BusinessSettings{
		ID:             row.ID,
		CompanyName:    row.CompanyName,
		RNC:            row.Rnc,
		Address:        row.Address,
		Phone:          row.Phone,
		WhatsApp:       row.Whatsapp,
		Email:          row.Email,
		Instagram:      row.Instagram,
		Facebook:       row.Facebook,
		BusinessHours:  row.BusinessHours,
		ItbisPercent:   row.ItbisPercent,
		DepositPercent: row.DepositPercent,
	}, nil
}

func (r *Repository) UpsertDeliveryZone(ctx context.Context, zone domain.DeliveryZone) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertDeliveryZoneParams{
		ID:         zone.ID,
		Name:       zone.Name,
		FeeDop:     zone.FeeDop,
		EtaMinutes: zone.EtaMinutes,
		Active:     zone.Active,
	}
	return r.queries.UpsertDeliveryZone(ctx, params)
}

func (r *Repository) ListDeliveryZones(ctx context.Context) ([]domain.DeliveryZone, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListDeliveryZones(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.DeliveryZone, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.DeliveryZone{
			ID:         row.ID,
			Name:       row.Name,
			FeeDop:     row.FeeDop,
			EtaMinutes: row.EtaMinutes,
			Active:     row.Active,
		})
	}
	return out, nil
}

func (r *Repository) UpsertNotification(ctx context.Context, ntf domain.Notification, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertNotificationParams{
		ID:             ntf.ID,
		RecipientRole:  ntf.RecipientRole,
		UserID:         ntf.UserID,
		Title:          ntf.Title,
		Body:           ntf.Body,
		Category:       ntf.Category,
		ActionPath:     ntf.ActionPath,
		IsRead:         ntf.IsRead,
		CreatedAtLabel: ntf.CreatedAtLabel,
		CreatedAt:      now,
	}
	return r.queries.UpsertNotification(ctx, params)
}

func (r *Repository) ListNotifications(ctx context.Context) ([]domain.Notification, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListNotifications(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Notification, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.Notification{
			ID:             row.ID,
			RecipientRole:  row.RecipientRole,
			UserID:         row.UserID,
			Title:          row.Title,
			Body:           row.Body,
			Category:       row.Category,
			ActionPath:     row.ActionPath,
			IsRead:         row.IsRead,
			CreatedAtLabel: row.CreatedAtLabel,
		})
	}
	return out, nil
}

func (r *Repository) MarkNotificationRead(ctx context.Context, id string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.MarkNotificationRead(ctx, id)
}

func (r *Repository) UpsertInsurancePolicy(ctx context.Context, pol domain.InsurancePolicy) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertInsurancePolicyParams{
		ID:              pol.ID,
		ProviderName:    pol.ProviderName,
		PolicyNumber:    pol.PolicyNumber,
		CoverageType:    pol.CoverageType,
		MaxCoverageDop:  pol.MaxCoverageDop,
		ValidUntilLabel: pol.ValidUntilLabel,
		Status:          pol.Status,
	}
	return r.queries.UpsertInsurancePolicy(ctx, params)
}

func (r *Repository) ListInsurancePolicies(ctx context.Context) ([]domain.InsurancePolicy, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListInsurancePolicies(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.InsurancePolicy, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.InsurancePolicy{
			ID:              row.ID,
			ProviderName:    row.ProviderName,
			PolicyNumber:    row.PolicyNumber,
			CoverageType:    row.CoverageType,
			MaxCoverageDop:  row.MaxCoverageDop,
			ValidUntilLabel: row.ValidUntilLabel,
			Status:          row.Status,
		})
	}
	return out, nil
}

func (r *Repository) UpsertAuditLog(ctx context.Context, aud domain.AuditLog, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertAuditLogParams{
		ID:             aud.ID,
		ActorName:      aud.ActorName,
		ActorRole:      aud.ActorRole,
		Action:         aud.Action,
		EntityType:     aud.EntityType,
		EntityID:       aud.EntityID,
		Summary:        aud.Summary,
		CreatedAtLabel: aud.CreatedAtLabel,
		CreatedAt:      now,
	}
	return r.queries.UpsertAuditLog(ctx, params)
}

func (r *Repository) ListAuditLogs(ctx context.Context) ([]domain.AuditLog, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListAuditLogs(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.AuditLog, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.AuditLog{
			ID:             row.ID,
			ActorName:      row.ActorName,
			ActorRole:      row.ActorRole,
			Action:         row.Action,
			EntityType:     row.EntityType,
			EntityID:       row.EntityID,
			Summary:        row.Summary,
			CreatedAtLabel: row.CreatedAtLabel,
		})
	}
	return out, nil
}

func (r *Repository) UpsertBotFAQ(ctx context.Context, faq domain.BotFAQ) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertBotFAQParams{
		ID:       faq.ID,
		Question: faq.Question,
		Answer:   faq.Answer,
		Category: faq.Category,
		Active:   faq.Active,
	}
	return r.queries.UpsertBotFAQ(ctx, params)
}

func (r *Repository) ListBotFAQs(ctx context.Context) ([]domain.BotFAQ, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListBotFAQs(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.BotFAQ, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.BotFAQ{
			ID:       row.ID,
			Question: row.Question,
			Answer:   row.Answer,
			Category: row.Category,
			Active:   row.Active,
		})
	}
	return out, nil
}

func (r *Repository) UpsertBotKeyword(ctx context.Context, kw domain.BotKeyword) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertBotKeywordParams{
		ID:             kw.ID,
		Keyword:        kw.Keyword,
		AutoReply:      kw.AutoReply,
		HandoffToHuman: kw.HandoffToHuman,
	}
	return r.queries.UpsertBotKeyword(ctx, params)
}

func (r *Repository) ListBotKeywords(ctx context.Context) ([]domain.BotKeyword, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListBotKeywords(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.BotKeyword, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.BotKeyword{
			ID:             row.ID,
			Keyword:        row.Keyword,
			AutoReply:      row.AutoReply,
			HandoffToHuman: row.HandoffToHuman,
		})
	}
	return out, nil
}

func (r *Repository) UpsertBotThread(ctx context.Context, th domain.BotThread) error {
	if !r.Enabled() {
		return nil
	}
	params := sqlcgen.UpsertBotThreadParams{
		ID:             th.ID,
		CustomerName:   th.CustomerName,
		Phone:          th.Phone,
		LastMessage:    th.LastMessage,
		BotPaused:      th.BotPaused,
		AssignedAgent:  th.AssignedAgent,
		UpdatedAtLabel: th.UpdatedAtLabel,
	}
	return r.queries.UpsertBotThread(ctx, params)
}

func (r *Repository) ListBotThreads(ctx context.Context) ([]domain.BotThread, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListBotThreads(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.BotThread, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.BotThread{
			ID:             row.ID,
			CustomerName:   row.CustomerName,
			Phone:          row.Phone,
			LastMessage:    row.LastMessage,
			BotPaused:      row.BotPaused,
			AssignedAgent:  row.AssignedAgent,
			UpdatedAtLabel: row.UpdatedAtLabel,
		})
	}
	return out, nil
}
