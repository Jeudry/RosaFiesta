package postgres

import (
	"context"
	"encoding/json"
	"strings"
	"time"

	"github.com/jackc/pgx/v5"
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
	return r.queries.UpsertQuote(ctx, buildUpsertQuoteParams(quote, now))
}

// SignQuoteTx bloquea la cotización con FOR UPDATE y persiste la firma atómicamente.
func (r *Repository) SignQuoteTx(
	ctx context.Context,
	id string,
	now time.Time,
	applySign func(existing domain.Quote) (domain.Quote, error),
) (domain.Quote, error) {
	tx, err := r.pool.BeginTx(ctx, pgx.TxOptions{})
	if err != nil {
		return domain.Quote{}, err
	}
	defer func() { _ = tx.Rollback(ctx) }()
	qtx := r.queries.WithTx(tx)
	row, err := qtx.GetQuoteByIDForUpdate(ctx, id)
	if err != nil {
		return domain.Quote{}, err
	}
	existing := mapQuoteRow(sqlcgen.ListQuotesRow(row))
	updated, err := applySign(existing)
	if err != nil {
		return domain.Quote{}, err
	}
	if err := qtx.UpsertQuote(ctx, buildUpsertQuoteParams(updated, now)); err != nil {
		return domain.Quote{}, err
	}
	if err := tx.Commit(ctx); err != nil {
		return domain.Quote{}, err
	}
	return updated, nil
}

func buildUpsertQuoteParams(quote domain.Quote, now time.Time) sqlcgen.UpsertQuoteParams {
	linesBytes, err := json.Marshal(quote.Lines)
	if err != nil {
		linesBytes = []byte("[]")
	}
	notes := strings.TrimSpace(quote.Notes)
	if notes == "" {
		notes = quote.SignedByName
		if quote.RejectionReason != "" {
			notes = quote.RejectionReason
		}
	}
	return sqlcgen.UpsertQuoteParams{
		ID:                 quote.ID,
		Code:               quote.Code,
		EventID:            quote.EventID,
		ClientID:           quote.ClientID,
		ClientEmail:        quote.ClientEmail,
		ClientName:         quote.ClientName,
		EventTitle:         quote.EventTitle,
		EventDateLabel:     quote.EventDateLabel,
		EventDateIso:       quote.EventDateISO,
		PackageID:          quote.PackageID,
		Venue:              quote.Venue,
		Guests:             quote.Guests,
		LinesJson:          linesBytes,
		DeliveryFeeDop:     quote.DeliveryFeeRD,
		DiscountDop:        quote.DiscountRD,
		TotalDop:           quote.TotalRD,
		DepositPercent:     resolveStoredDepositPercent(quote.DepositPercent),
		DepositDop:         quote.DepositRD,
		BalanceDop:         quote.BalanceRD,
		Status:             string(quote.Status),
		Version:            1,
		Notes:              notes,
		SignatureSvg:       quote.SignatureSVG,
		SignedByName:       quote.SignedByName,
		SignedAtIso:        quote.SignedAtISO,
		ContractPdfUrl:     quote.ContractPDFURL,
		RejectionReason:    quote.RejectionReason,
		SignedByUserID:     quote.SignedByUserID,
		ContractHashSha256: quote.ContractHashSHA256,
		ContactPhone:       quote.ContactPhone,
		CreatedAt:          now,
	}
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
		out = append(out, mapQuoteRow(row))
	}
	return out, nil
}

// GetQuoteByID recupera una cotización persistida por su identificador.
func (r *Repository) GetQuoteByID(ctx context.Context, id string) (domain.Quote, error) {
	if !r.Enabled() {
		return domain.Quote{}, domain.ErrQuoteNotFound
	}
	row, err := r.queries.GetQuoteByID(ctx, id)
	if err != nil {
		return domain.Quote{}, err
	}
	return mapQuoteRow(sqlcgen.ListQuotesRow(row)), nil
}

func mapQuoteRow(row sqlcgen.ListQuotesRow) domain.Quote {
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
	resolvedNotes := row.Notes
	if signedBy == "" {
		if domain.QuoteStatus(row.Status) == domain.QuoteStatusSigned {
			signedBy = row.Notes
			resolvedNotes = ""
		}
	}
	if rejection == "" {
		if domain.QuoteStatus(row.Status) == domain.QuoteStatusDeclined {
			rejection = row.Notes
			resolvedNotes = ""
		}
	}
	return domain.Quote{
		ID:                 row.ID,
		Code:               code,
		EventID:            row.EventID,
		ClientID:           row.ClientID,
		ClientEmail:        row.ClientEmail,
		ClientName:         row.ClientName,
		EventTitle:         row.EventTitle,
		EventDateLabel:     row.EventDateLabel,
		EventDateISO:       row.EventDateIso,
		PackageID:          row.PackageID,
		Venue:              row.Venue,
		Guests:             row.Guests,
		Lines:              lines,
		DeliveryFeeRD:      row.DeliveryFeeDop,
		DiscountRD:         row.DiscountDop,
		TotalRD:            row.TotalDop,
		DepositPercent:     resolveStoredDepositPercent(row.DepositPercent),
		DepositRD:          row.DepositDop,
		BalanceRD:          row.BalanceDop,
		Status:             domain.QuoteStatus(row.Status),
		Notes:              resolvedNotes,
		SignatureSVG:       row.SignatureSvg,
		SignedByName:       signedBy,
		SignedByUserID:     row.SignedByUserID,
		SignedAtISO:        row.SignedAtIso,
		ContractPDFURL:     row.ContractPdfUrl,
		ContractHashSHA256: row.ContractHashSha256,
		ContactPhone:       row.ContactPhone,
		RejectionReason:    rejection,
	}
}

func resolveStoredDepositPercent(pct int) int {
	if pct <= 0 {
		return 30
	}
	if pct > 100 {
		return 30
	}
	return pct
}
