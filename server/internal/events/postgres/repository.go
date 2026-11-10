package postgres

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	"github.com/Jeudry/rosafiesta/server/internal/events/postgres/sqlcgen"
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

func (r *Repository) SeedCollaboration(ctx context.Context, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	for _, guest := range domain.CanonicalGuests(now) {
		if err := r.UpsertGuest(ctx, guest); err != nil {
			return err
		}
	}
	for _, block := range domain.CanonicalScheduleBlocks() {
		if err := r.UpsertScheduleBlock(ctx, block); err != nil {
			return err
		}
	}
	for _, msg := range domain.CanonicalChatMessages(now) {
		if err := r.UpsertChatMessage(ctx, msg); err != nil {
			return err
		}
	}
	return nil
}

func (r *Repository) UpsertEvent(ctx context.Context, event domain.Event, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	sketch := event.SketchJSON
	if sketch == "" {
		sketch = "{}"
	}
	err := r.queries.UpsertEvent(ctx, sqlcgen.UpsertEventParams{
		ID:                 event.ID,
		Code:               event.Code,
		ClientID:           event.ClientID,
		ClientName:         event.ClientName,
		ClientEmail:        event.ClientEmail,
		Title:              event.Title,
		EventType:          event.EventType,
		PhotoKey:           event.EventType,
		Venue:              event.Venue,
		Zone:               event.Zone,
		EventDateLabel:     event.DateLabel,
		DaysUntil:          event.DaysUntilEvent,
		GuestsCount:        event.Guests,
		ArticlesCount:      event.ArticleCount,
		TotalDop:           event.TotalRD,
		DepositDop:         event.PaidDepositRD,
		PaidDop:            event.PaidDepositRD,
		PaidDepositDop:     event.PaidDepositRD,
		BalanceDop:         event.BalanceRD(),
		BalanceDueLabel:    event.BalanceDueDate,
		Status:             string(event.Status),
		DateChangesCount:   event.DateChangesUsed,
		AssignedLead:       event.AssignedLead,
		SketchJson:         sketch,
		CancellationReason: event.CancellationReason,
		CreatedAt:          now,
	})
	if err != nil {
		return err
	}
	return r.replaceEventItems(ctx, event.ID, event.Items)
}

func (r *Repository) replaceEventItems(ctx context.Context, eventID string, items []domain.EventItem) error {
	if err := r.queries.DeleteEventItems(ctx, eventID); err != nil {
		return err
	}
	for _, item := range items {
		if item.Quantity <= 0 {
			continue
		}
		err := r.queries.UpsertEventItem(ctx, sqlcgen.UpsertEventItemParams{
			EventID:      eventID,
			ArticleCode:  item.ArticleCode,
			Name:         item.Name,
			Quantity:     item.Quantity,
			UnitPriceDop: item.UnitPriceRD,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

func (r *Repository) UpdateDate(ctx context.Context, event domain.Event) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateEventDate(ctx, sqlcgen.UpdateEventDateParams{
		ID:               event.ID,
		EventDateLabel:   event.DateLabel,
		DaysUntil:        event.DaysUntilEvent,
		DateChangesCount: event.DateChangesUsed,
		TotalDop:         event.TotalRD,
		BalanceDop:       event.BalanceRD(),
	})
}

func (r *Repository) UpdateStatusAndReason(ctx context.Context, id string, status domain.EventStatus, reason string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateEventStatusAndReason(ctx, sqlcgen.UpdateEventStatusAndReasonParams{
		ID:                 id,
		Status:             string(status),
		CancellationReason: reason,
	})
}

func (r *Repository) ListEvents(ctx context.Context) ([]domain.Event, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListEvents(ctx)
	if err != nil {
		return nil, err
	}
	itemRows, err := r.queries.ListAllEventItems(ctx)
	if err != nil {
		return nil, err
	}
	itemsByEvent := make(map[string][]domain.EventItem)
	for _, item := range itemRows {
		itemsByEvent[item.EventID] = append(itemsByEvent[item.EventID], domain.EventItem{
			ArticleCode: item.ArticleCode,
			Name:        item.Name,
			Quantity:    item.Quantity,
			UnitPriceRD: item.UnitPriceDop,
		})
	}
	out := make([]domain.Event, 0, len(rows))
	for _, row := range rows {
		paidDeposit := row.PaidDepositDop
		if row.PaidDop > paidDeposit {
			paidDeposit = row.PaidDop
		}
		items := itemsByEvent[row.ID]
		if items == nil {
			items = []domain.EventItem{}
		}
		out = append(out, domain.Event{
			ID:                 row.ID,
			Code:               row.Code,
			ClientID:           row.ClientID,
			ClientName:         row.ClientName,
			ClientEmail:        row.ClientEmail,
			Title:              row.Title,
			EventType:          row.EventType,
			DateLabel:          row.EventDateLabel,
			DaysUntilEvent:     row.DaysUntil,
			Venue:              row.Venue,
			Zone:               row.Zone,
			Guests:             row.GuestsCount,
			ArticleCount:       row.ArticlesCount,
			TotalRD:            row.TotalDop,
			PaidDepositRD:      paidDeposit,
			BalanceDueDate:     row.BalanceDueLabel,
			Status:             domain.EventStatus(row.Status),
			DateChangesUsed:    row.DateChangesCount,
			AssignedLead:       row.AssignedLead,
			SketchJSON:         row.SketchJson,
			CancellationReason: row.CancellationReason,
			Items:              items,
		})
	}
	return out, nil
}

func (r *Repository) ListGuests(ctx context.Context, eventID string) ([]domain.EventGuest, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListEventGuests(ctx, eventID)
	if err != nil {
		return nil, err
	}
	out := make([]domain.EventGuest, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.EventGuest{
			ID:           row.ID,
			EventID:      row.EventID,
			FullName:     row.FullName,
			RSVPStatus:   row.RsvpStatus,
			TableLabel:   row.TableLabel,
			DietaryNotes: row.DietaryNotes,
			CreatedAt:    row.CreatedAt,
		})
	}
	return out, nil
}

func (r *Repository) UpsertGuest(ctx context.Context, guest domain.EventGuest) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertEventGuest(ctx, sqlcgen.UpsertEventGuestParams{
		ID:           guest.ID,
		EventID:      guest.EventID,
		FullName:     guest.FullName,
		RsvpStatus:   guest.RSVPStatus,
		TableLabel:   guest.TableLabel,
		DietaryNotes: guest.DietaryNotes,
		CreatedAt:    guest.CreatedAt,
	})
}

func (r *Repository) ListScheduleBlocks(ctx context.Context, eventID string) ([]domain.ScheduleBlock, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListScheduleBlocks(ctx, eventID)
	if err != nil {
		return nil, err
	}
	out := make([]domain.ScheduleBlock, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.ScheduleBlock{
			ID:        row.ID,
			EventID:   row.EventID,
			TimeRange: row.TimeRange,
			Title:     row.Title,
			OwnerName: row.OwnerName,
			Status:    row.Status,
			SortOrder: row.SortOrder,
		})
	}
	return out, nil
}

func (r *Repository) UpsertScheduleBlock(ctx context.Context, block domain.ScheduleBlock) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertScheduleBlock(ctx, sqlcgen.UpsertScheduleBlockParams{
		ID:        block.ID,
		EventID:   block.EventID,
		TimeRange: block.TimeRange,
		Title:     block.Title,
		OwnerName: block.OwnerName,
		Status:    block.Status,
		SortOrder: block.SortOrder,
	})
}

func (r *Repository) DeleteScheduleBlock(ctx context.Context, eventID, blockID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteScheduleBlock(ctx, sqlcgen.DeleteScheduleBlockParams{
		EventID: eventID,
		ID:      blockID,
	})
}

func (r *Repository) ListAllChatMessages(ctx context.Context) ([]domain.ChatMessage, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListAllChatMessages(ctx)
	if err != nil {
		return nil, err
	}
	return mapChatRows(rows), nil
}

func (r *Repository) ListChatMessagesByThread(ctx context.Context, threadID string) ([]domain.ChatMessage, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListChatMessagesByThread(ctx, threadID)
	if err != nil {
		return nil, err
	}
	return mapChatRows(rows), nil
}

func (r *Repository) UpsertChatMessage(ctx context.Context, msg domain.ChatMessage) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertChatMessage(ctx, sqlcgen.UpsertChatMessageParams{
		ID:             msg.ID,
		ThreadID:       msg.ThreadID,
		EventID:        msg.EventID,
		EventTitle:     msg.EventTitle,
		SenderID:       msg.SenderID,
		SenderName:     msg.SenderName,
		SenderRole:     msg.SenderRole,
		RecipientLabel: msg.RecipientLabel,
		Body:           msg.Body,
		AttachmentName: msg.AttachmentName,
		Unread:         msg.Unread,
		CreatedAt:      msg.CreatedAt,
	})
}

func mapChatRows(rows []sqlcgen.ChatMessage) []domain.ChatMessage {
	out := make([]domain.ChatMessage, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.ChatMessage{
			ID:             row.ID,
			ThreadID:       row.ThreadID,
			EventID:        row.EventID,
			EventTitle:     row.EventTitle,
			SenderID:       row.SenderID,
			SenderName:     row.SenderName,
			SenderRole:     row.SenderRole,
			RecipientLabel: row.RecipientLabel,
			Body:           row.Body,
			AttachmentName: row.AttachmentName,
			Unread:         row.Unread,
			CreatedAt:      row.CreatedAt,
		})
	}
	return out
}
