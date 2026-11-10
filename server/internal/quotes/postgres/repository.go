package postgres

import (
	"context"
	"encoding/json"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/postgres/sqlcgen"
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

func (r *Repository) UpsertQuote(ctx context.Context, quote domain.Quote, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	linesBytes, err := json.Marshal(quote.Lines)
	if err != nil {
		linesBytes = []byte("[]")
	}
	notes := quote.SignedByName
	if quote.RejectionReason != "" {
		notes = quote.RejectionReason
	}
	return r.queries.UpsertQuote(ctx, sqlcgen.UpsertQuoteParams{
		ID:              quote.ID,
		Code:            quote.Code,
		EventID:         quote.EventID,
		ClientID:        quote.ClientID,
		ClientEmail:     quote.ClientEmail,
		ClientName:      quote.ClientName,
		EventTitle:      quote.EventTitle,
		EventDateLabel:  quote.EventDateLabel,
		Venue:           quote.Venue,
		Guests:          quote.Guests,
		LinesJson:       linesBytes,
		DeliveryFeeDop:  quote.DeliveryFeeRD,
		DiscountDop:     quote.DiscountRD,
		TotalDop:        quote.TotalRD,
		DepositDop:      quote.DepositRD,
		BalanceDop:      quote.BalanceRD,
		Status:          string(quote.Status),
		Version:         1,
		Notes:           notes,
		SignatureSvg:    quote.SignatureSVG,
		SignedByName:    quote.SignedByName,
		RejectionReason: quote.RejectionReason,
		CreatedAt:       now,
	})
}

func (r *Repository) ListQuotes(ctx context.Context) ([]domain.Quote, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListQuotes(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Quote, 0, len(rows))
	for _, row := range rows {
		var lines []domain.QuoteLine
		if len(row.LinesJson) > 0 {
			_ = json.Unmarshal(row.LinesJson, &lines)
		}
		if lines == nil {
			lines = []domain.QuoteLine{}
		}
		code := row.Code
		if code == "" {
			code = row.EventID
		}
		signedBy := row.SignedByName
		rejection := row.RejectionReason
		if signedBy == "" {
			if domain.QuoteStatus(row.Status) == domain.QuoteStatusSigned {
				signedBy = row.Notes
			}
		}
		if rejection == "" {
			if domain.QuoteStatus(row.Status) == domain.QuoteStatusDeclined {
				rejection = row.Notes
			}
		}
		out = append(out, domain.Quote{
			ID:              row.ID,
			Code:            code,
			EventID:         row.EventID,
			ClientID:        row.ClientID,
			ClientEmail:     row.ClientEmail,
			ClientName:      row.ClientName,
			EventTitle:      row.EventTitle,
			EventDateLabel:  row.EventDateLabel,
			Venue:           row.Venue,
			Guests:          row.Guests,
			Lines:           lines,
			DeliveryFeeRD:   row.DeliveryFeeDop,
			DiscountRD:      row.DiscountDop,
			TotalRD:         row.TotalDop,
			DepositPercent:  30,
			DepositRD:       row.DepositDop,
			BalanceRD:       row.BalanceDop,
			Status:          domain.QuoteStatus(row.Status),
			SignatureSVG:    row.SignatureSvg,
			SignedByName:    signedBy,
			RejectionReason: rejection,
		})
	}
	return out, nil
}
