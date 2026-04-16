package store

import (
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type FinancialStore struct {
	db *sql.DB
}

func NewFinancialStore(db *sql.DB) *FinancialStore {
	return &FinancialStore{db: db}
}

func (s *FinancialStore) CreateFinancialCategory(ctx context.Context, cat *models.FinancialCategory) error {
	query := `
		INSERT INTO financial_categories (name, type, description, color, is_active)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING id, created_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		cat.Name, cat.Type, cat.Description, cat.Color, cat.IsActive,
	).Scan(&cat.ID, &cat.CreatedAt)
}

func (s *FinancialStore) GetAllFinancialCategories(ctx context.Context) ([]models.FinancialCategory, error) {
	query := `SELECT id, name, type, description, color, is_active, created_at FROM financial_categories WHERE is_active = true ORDER BY type, name`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var categories []models.FinancialCategory
	for rows.Next() {
		var c models.FinancialCategory
		if err := rows.Scan(&c.ID, &c.Name, &c.Type, &c.Description, &c.Color, &c.IsActive, &c.CreatedAt); err != nil {
			return nil, err
		}
		categories = append(categories, c)
	}
	return categories, rows.Err()
}

func (s *FinancialStore) CreateFinancialRecord(ctx context.Context, rec *models.FinancialRecord) error {
	query := `
		INSERT INTO financial_records (event_id, category_id, type, amount, currency, description, reference_number, payment_method, recorded_by, record_date, metadata)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
		RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	var metadataJSON []byte
	if rec.Metadata != nil {
		metadataJSON, _ = json.Marshal(rec.Metadata)
	}

	return s.db.QueryRowContext(ctx, query,
		rec.EventID, rec.CategoryID, rec.Type, rec.Amount, rec.Currency, rec.Description,
		rec.ReferenceNumber, rec.PaymentMethod, rec.RecordedBy, rec.RecordDate, metadataJSON,
	).Scan(&rec.ID, &rec.CreatedAt, &rec.UpdatedAt)
}

func (s *FinancialStore) GetFinancialRecords(ctx context.Context, startDate, endDate string, recordType, categoryID string) ([]models.FinancialRecord, error) {
	query := `
		SELECT fr.id, fr.event_id, fr.category_id, fr.type, fr.amount, fr.currency, fr.description,
			fr.reference_number, fr.payment_method, fr.recorded_by, fr.record_date, fr.is_reconciled,
			fr.reconciled_at, fr.metadata, fr.created_at, fr.updated_at,
			fc.id, fc.name, fc.type, fc.description, fc.color
		FROM financial_records fr
		JOIN financial_categories fc ON fr.category_id = fc.id
		WHERE fr.record_date BETWEEN $1 AND $2`

	args := []interface{}{startDate, endDate}
	argIdx := 3

	if recordType != "" {
		query += fmt.Sprintf(" AND fr.type = $%d", argIdx)
		args = append(args, recordType)
		argIdx++
	}
	if categoryID != "" {
		query += fmt.Sprintf(" AND fr.category_id = $%d", argIdx)
		args = append(args, categoryID)
	}

	query += " ORDER BY fr.record_date DESC"

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var records []models.FinancialRecord
	for rows.Next() {
		var r models.FinancialRecord
		var cat models.FinancialCategory
		var metadataJSON []byte

		if err := rows.Scan(
			&r.ID, &r.EventID, &r.CategoryID, &r.Type, &r.Amount, &r.Currency, &r.Description,
			&r.ReferenceNumber, &r.PaymentMethod, &r.RecordedBy, &r.RecordDate, &r.IsReconciled,
			&r.ReconciledAt, &metadataJSON, &r.CreatedAt, &r.UpdatedAt,
			&cat.ID, &cat.Name, &cat.Type, &cat.Description, &cat.Color,
		); err != nil {
			return nil, err
		}

		if metadataJSON != nil {
			json.Unmarshal(metadataJSON, &r.Metadata)
		}
		r.Category = &cat
		records = append(records, r)
	}
	return records, rows.Err()
}

func (s *FinancialStore) GetFinancialSummary(ctx context.Context, startDate, endDate string) (*models.FinancialSummary, error) {
	query := `
		SELECT
			COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) as total_income,
			COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END), 0) as total_expenses
		FROM financial_records
		WHERE record_date BETWEEN $1 AND $2`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	var summary models.FinancialSummary
	summary.PeriodStart = startDate
	summary.PeriodEnd = endDate

	err := s.db.QueryRowContext(ctx, query, startDate, endDate).Scan(&summary.TotalIncome, &summary.TotalExpenses)
	if err != nil {
		return nil, err
	}

	summary.NetProfit = summary.TotalIncome - summary.TotalExpenses

	pendingQuery := `
		SELECT COALESCE(SUM(total - amount_paid), 0)
		FROM invoices
		WHERE status IN ('sent', 'partial') AND due_date < $1`
	err = s.db.QueryRowContext(ctx, pendingQuery, time.Now()).Scan(&summary.PendingPayments)

	return &summary, err
}

func (s *FinancialStore) CreateInvoice(ctx context.Context, inv *models.Invoice) error {
	query := `
		INSERT INTO invoices (invoice_number, event_id, client_id, subtotal, tax_amount, discount_amount, total, amount_paid, currency, status, issue_date, due_date, notes, terms, created_by)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
		RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		inv.InvoiceNumber, inv.EventID, inv.ClientID, inv.Subtotal, inv.TaxAmount,
		inv.DiscountAmount, inv.Total, inv.AmountPaid, inv.Currency, inv.Status,
		inv.IssueDate, inv.DueDate, inv.Notes, inv.Terms, inv.CreatedBy,
	).Scan(&inv.ID, &inv.CreatedAt, &inv.UpdatedAt)
}

func (s *FinancialStore) GetInvoices(ctx context.Context, clientID string, status string) ([]models.Invoice, error) {
	query := `
		SELECT id, invoice_number, event_id, client_id, subtotal, tax_amount, discount_amount,
			total, amount_paid, currency, status, issue_date, due_date, paid_date, notes, terms, created_by, created_at, updated_at
		FROM invoices WHERE 1=1`

	args := []interface{}{}
	argIdx := 1

	if clientID != "" {
		query += fmt.Sprintf(" AND client_id = $%d", argIdx)
		args = append(args, clientID)
		argIdx++
	}
	if status != "" {
		query += fmt.Sprintf(" AND status = $%d", argIdx)
		args = append(args, status)
	}

	query += " ORDER BY issue_date DESC"

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var invoices []models.Invoice
	for rows.Next() {
		var i models.Invoice
		if err := rows.Scan(
			&i.ID, &i.InvoiceNumber, &i.EventID, &i.ClientID, &i.Subtotal, &i.TaxAmount,
			&i.DiscountAmount, &i.Total, &i.AmountPaid, &i.Currency, &i.Status,
			&i.IssueDate, &i.DueDate, &i.PaidDate, &i.Notes, &i.Terms, &i.CreatedBy, &i.CreatedAt, &i.UpdatedAt,
		); err != nil {
			return nil, err
		}
		invoices = append(invoices, i)
	}
	return invoices, rows.Err()
}

func (s *FinancialStore) CreateExpenseVendor(ctx context.Context, vendor *models.ExpenseVendor) error {
	query := `
		INSERT INTO expense_vendors (name, contact_name, email, phone, address, category, notes, is_active)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		vendor.Name, vendor.ContactName, vendor.Email, vendor.Phone,
		vendor.Address, vendor.Category, vendor.Notes, vendor.IsActive,
	).Scan(&vendor.ID, &vendor.CreatedAt, &vendor.UpdatedAt)
}

func (s *FinancialStore) GetExpenseVendors(ctx context.Context) ([]models.ExpenseVendor, error) {
	query := `SELECT id, name, contact_name, email, phone, address, category, notes, is_active, created_at, updated_at FROM expense_vendors WHERE is_active = true ORDER BY name`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var vendors []models.ExpenseVendor
	for rows.Next() {
		var v models.ExpenseVendor
		if err := rows.Scan(&v.ID, &v.Name, &v.ContactName, &v.Email, &v.Phone, &v.Address, &v.Category, &v.Notes, &v.IsActive, &v.CreatedAt, &v.UpdatedAt); err != nil {
			return nil, err
		}
		vendors = append(vendors, v)
	}
	return vendors, rows.Err()
}

func (s *FinancialStore) CreateVendorPayment(ctx context.Context, payment *models.VendorPayment) error {
	query := `
		INSERT INTO vendor_payments (vendor_id, amount, currency, payment_date, payment_method, reference_number, description, recorded_by)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id, created_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		payment.VendorID, payment.Amount, payment.Currency, payment.PaymentDate,
		payment.PaymentMethod, payment.ReferenceNumber, payment.Description, payment.RecordedBy,
	).Scan(&payment.ID, &payment.CreatedAt)
}

func (s *FinancialStore) GetVendorPayments(ctx context.Context, vendorID uuid.UUID) ([]models.VendorPayment, error) {
	query := `SELECT id, vendor_id, amount, currency, payment_date, payment_method, reference_number, description, recorded_by, created_at FROM vendor_payments WHERE vendor_id = $1 ORDER BY payment_date DESC`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, vendorID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var payments []models.VendorPayment
	for rows.Next() {
		var p models.VendorPayment
		if err := rows.Scan(&p.ID, &p.VendorID, &p.Amount, &p.Currency, &p.PaymentDate, &p.PaymentMethod, &p.ReferenceNumber, &p.Description, &p.RecordedBy, &p.CreatedAt); err != nil {
			return nil, err
		}
		payments = append(payments, p)
	}
	return payments, rows.Err()
}

type InsuranceStore struct {
	db *sql.DB
}

func NewInsuranceStore(db *sql.DB) *InsuranceStore {
	return &InsuranceStore{db: db}
}

func (s *InsuranceStore) CreateArticleInsurance(ctx context.Context, ins *models.ArticleInsurance) error {
	query := `
		INSERT INTO article_insurance (article_id, policy_number, provider, coverage_type, coverage_amount, premium, deductible, terms, is_active)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
		RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		ins.ArticleID, ins.PolicyNumber, ins.Provider, ins.CoverageType,
		ins.CoverageAmount, ins.Premium, ins.Deductible, ins.Terms, ins.IsActive,
	).Scan(&ins.ID, &ins.CreatedAt, &ins.UpdatedAt)
}

func (s *InsuranceStore) GetArticleInsurance(ctx context.Context, articleID uuid.UUID) ([]models.ArticleInsurance, error) {
	query := `SELECT id, article_id, policy_number, provider, coverage_type, coverage_amount, premium, deductible, terms, is_active, created_at, updated_at FROM article_insurance WHERE article_id = $1 AND is_active = true`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, articleID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var insurances []models.ArticleInsurance
	for rows.Next() {
		var i models.ArticleInsurance
		if err := rows.Scan(&i.ID, &i.ArticleID, &i.PolicyNumber, &i.Provider, &i.CoverageType, &i.CoverageAmount, &i.Premium, &i.Deductible, &i.Terms, &i.IsActive, &i.CreatedAt, &i.UpdatedAt); err != nil {
			return nil, err
		}
		insurances = append(insurances, i)
	}
	return insurances, rows.Err()
}

func (s *InsuranceStore) CreateEventInsurance(ctx context.Context, ins *models.EventInsurance) error {
	query := `
		INSERT INTO event_insurance (event_id, insurance_id, articles_covered, total_coverage, premium_paid, status)
		VALUES ($1, $2, $3, $4, $5, $6)
		RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	articlesJSON, _ := json.Marshal(ins.ArticlesCovered)

	return s.db.QueryRowContext(ctx, query,
		ins.EventID, ins.InsuranceID, articlesJSON, ins.TotalCoverage, ins.PremiumPaid, ins.Status,
	).Scan(&ins.ID, &ins.CreatedAt, &ins.UpdatedAt)
}

func (s *InsuranceStore) GetEventInsurance(ctx context.Context, eventID uuid.UUID) (*models.EventInsurance, error) {
	query := `SELECT id, event_id, insurance_id, articles_covered, total_coverage, premium_paid, status, claim_id, created_at, updated_at FROM event_insurance WHERE event_id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	var ins models.EventInsurance
	var articlesJSON []byte

	err := s.db.QueryRowContext(ctx, query, eventID).Scan(
		&ins.ID, &ins.EventID, &ins.InsuranceID, &articlesJSON,
		&ins.TotalCoverage, &ins.PremiumPaid, &ins.Status, &ins.ClaimID, &ins.CreatedAt, &ins.UpdatedAt,
	)
	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}

	json.Unmarshal(articlesJSON, &ins.ArticlesCovered)
	return &ins, nil
}

func (s *InsuranceStore) CreateInsuranceClaim(ctx context.Context, claim *models.InsuranceClaim) error {
	query := `
		INSERT INTO insurance_claims (event_insurance_id, claim_number, incident_type, description, claimed_amount, status, incident_date, filed_date)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		claim.EventInsuranceID, claim.ClaimNumber, claim.IncidentType, claim.Description,
		claim.ClaimedAmount, claim.Status, claim.IncidentDate, claim.FiledDate,
	).Scan(&claim.ID, &claim.CreatedAt, &claim.UpdatedAt)
}

func (s *InsuranceStore) GetInsuranceClaims(ctx context.Context, status string) ([]models.InsuranceClaim, error) {
	query := `SELECT id, event_insurance_id, claim_number, incident_type, description, claimed_amount, approved_amount, status, incident_date, filed_date, resolution_notes, resolved_at, created_at, updated_at FROM insurance_claims WHERE 1=1`

	args := []interface{}{}
	argIdx := 1

	if status != "" {
		query += fmt.Sprintf(" AND status = $%d", argIdx)
		args = append(args, status)
	}

	query += " ORDER BY filed_date DESC"

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var claims []models.InsuranceClaim
	for rows.Next() {
		var c models.InsuranceClaim
		if err := rows.Scan(&c.ID, &c.EventInsuranceID, &c.ClaimNumber, &c.IncidentType, &c.Description, &c.ClaimedAmount, &c.ApprovedAmount, &c.Status, &c.IncidentDate, &c.FiledDate, &c.ResolutionNotes, &c.ResolvedAt, &c.CreatedAt, &c.UpdatedAt); err != nil {
			return nil, err
		}
		claims = append(claims, c)
	}
	return claims, rows.Err()
}

type PayPalStore struct {
	db *sql.DB
}

func NewPayPalStore(db *sql.DB) *PayPalStore {
	return &PayPalStore{db: db}
}

func (s *PayPalStore) CreateTransaction(ctx context.Context, tx *models.PayPalTransaction) error {
	query := `
		INSERT INTO paypal_transactions (paypal_order_id, paypal_capture_id, event_id, user_id, amount, currency, status, paypal_response)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		tx.PayPalOrderID, tx.PayPalCaptureID, tx.EventID, tx.UserID, tx.Amount, tx.Currency, tx.Status, tx.PayPalResponse,
	).Scan(&tx.ID, &tx.CreatedAt, &tx.UpdatedAt)
}

func (s *PayPalStore) UpdateTransactionCapture(ctx context.Context, orderID, captureID string) error {
	query := `UPDATE paypal_transactions SET paypal_capture_id = $1, status = 'completed', updated_at = NOW() WHERE paypal_order_id = $2`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, captureID, orderID)
	return err
}

func (s *PayPalStore) GetTransactionByOrderID(ctx context.Context, orderID string) (*models.PayPalTransaction, error) {
	query := `SELECT id, paypal_order_id, paypal_capture_id, event_id, user_id, amount, currency, status, paypal_response, created_at, updated_at FROM paypal_transactions WHERE paypal_order_id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	var tx models.PayPalTransaction
	err := s.db.QueryRowContext(ctx, query, orderID).Scan(
		&tx.ID, &tx.PayPalOrderID, &tx.PayPalCaptureID, &tx.EventID, &tx.UserID,
		&tx.Amount, &tx.Currency, &tx.Status, &tx.PayPalResponse, &tx.CreatedAt, &tx.UpdatedAt,
	)
	if err == sql.ErrNoRows {
		return nil, nil
	}
	return &tx, err
}

func (s *PayPalStore) GetTransactionsByEvent(ctx context.Context, eventID uuid.UUID) ([]models.PayPalTransaction, error) {
	query := `SELECT id, paypal_order_id, paypal_capture_id, event_id, user_id, amount, currency, status, paypal_response, created_at, updated_at FROM paypal_transactions WHERE event_id = $1 ORDER BY created_at DESC`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var txs []models.PayPalTransaction
	for rows.Next() {
		var tx models.PayPalTransaction
		if err := rows.Scan(&tx.ID, &tx.PayPalOrderID, &tx.PayPalCaptureID, &tx.EventID, &tx.UserID, &tx.Amount, &tx.Currency, &tx.Status, &tx.PayPalResponse, &tx.CreatedAt, &tx.UpdatedAt); err != nil {
			return nil, err
		}
		txs = append(txs, tx)
	}
	return txs, rows.Err()
}

type ClientAuditStore struct {
	db *sql.DB
}

func NewClientAuditStore(db *sql.DB) *ClientAuditStore {
	return &ClientAuditStore{db: db}
}

func (s *ClientAuditStore) LogClientAction(ctx context.Context, log *models.ClientAuditLog) error {
	query := `
		INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, recorded_by, ip_address)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id, created_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		log.UserID, log.Action, log.EntityType, log.EntityID, log.Details, log.RecordedBy, log.IPAddress,
	).Scan(&log.ID, &log.CreatedAt)
}

func (s *ClientAuditStore) GetClientAuditLog(ctx context.Context, userID uuid.UUID, limit int) ([]models.ClientAuditLog, error) {
	query := `
		SELECT id, user_id, action, entity_type, entity_id, details, recorded_by, ip_address, created_at
		FROM audit_logs
		WHERE user_id = $1
		ORDER BY created_at DESC
		LIMIT $2`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, userID, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var logs []models.ClientAuditLog
	for rows.Next() {
		var l models.ClientAuditLog
		if err := rows.Scan(&l.ID, &l.UserID, &l.Action, &l.EntityType, &l.EntityID, &l.Details, &l.RecordedBy, &l.IPAddress, &l.CreatedAt); err != nil {
			return nil, err
		}
		logs = append(logs, l)
	}
	return logs, rows.Err()
}

func (s *ClientAuditStore) GetAllAuditLogs(ctx context.Context, userID *uuid.UUID, action, entityType string, startDate, endDate string, limit, offset int) ([]models.ClientAuditLog, int, error) {
	countQuery := `SELECT COUNT(*) FROM audit_logs WHERE 1=1`
	query := `SELECT id, user_id, action, entity_type, entity_id, details, recorded_by, ip_address, created_at FROM audit_logs WHERE 1=1`

	args := []interface{}{}
	argIdx := 1

	if userID != nil {
		qs := fmt.Sprintf(" AND user_id = $%d", argIdx)
		countQuery += qs
		query += qs
		args = append(args, *userID)
		argIdx++
	}
	if action != "" {
		qs := fmt.Sprintf(" AND action = $%d", argIdx)
		countQuery += qs
		query += qs
		args = append(args, action)
		argIdx++
	}
	if entityType != "" {
		qs := fmt.Sprintf(" AND entity_type = $%d", argIdx)
		countQuery += qs
		query += qs
		args = append(args, entityType)
		argIdx++
	}
	if startDate != "" && endDate != "" {
		qs := fmt.Sprintf(" AND created_at BETWEEN $%d AND $%d", argIdx, argIdx+1)
		countQuery += qs
		query += qs
		args = append(args, startDate, endDate)
		argIdx += 2
	}

	var total int
	if err := s.db.QueryRowContext(ctx, countQuery, args...).Scan(&total); err != nil {
		return nil, 0, err
	}

	query += fmt.Sprintf(" ORDER BY created_at DESC LIMIT $%d OFFSET $%d", argIdx, argIdx+1)
	args = append(args, limit, offset)

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, 0, err
	}
	defer rows.Close()

	var logs []models.ClientAuditLog
	for rows.Next() {
		var l models.ClientAuditLog
		if err := rows.Scan(&l.ID, &l.UserID, &l.Action, &l.EntityType, &l.EntityID, &l.Details, &l.RecordedBy, &l.IPAddress, &l.CreatedAt); err != nil {
			return nil, 0, err
		}
		logs = append(logs, l)
	}
	return logs, total, rows.Err()
}

func (s *FinancialStore) GetIncomeByCategory(ctx context.Context, startDate, endDate string) (map[string]float64, error) {
	query := `
		SELECT fc.name, COALESCE(SUM(fr.amount), 0) as total
		FROM financial_records fr
		JOIN financial_categories fc ON fr.category_id = fc.id
		WHERE fr.type = 'income' AND fr.record_date BETWEEN $1 AND $2
		GROUP BY fc.name`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, startDate, endDate)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	result := make(map[string]float64)
	for rows.Next() {
		var name string
		var total float64
		if err := rows.Scan(&name, &total); err != nil {
			return nil, err
		}
		result[name] = total
	}
	return result, rows.Err()
}

func (s *FinancialStore) GetExpensesByCategory(ctx context.Context, startDate, endDate string) (map[string]float64, error) {
	query := `
		SELECT fc.name, COALESCE(SUM(fr.amount), 0) as total
		FROM financial_records fr
		JOIN financial_categories fc ON fr.category_id = fc.id
		WHERE fr.type = 'expense' AND fr.record_date BETWEEN $1 AND $2
		GROUP BY fc.name`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, startDate, endDate)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	result := make(map[string]float64)
	for rows.Next() {
		var name string
		var total float64
		if err := rows.Scan(&name, &total); err != nil {
			return nil, err
		}
		result[name] = total
	}
	return result, rows.Err()
}

func (s *FinancialStore) ReconcileRecord(ctx context.Context, recordID uuid.UUID) error {
	query := `UPDATE financial_records SET is_reconciled = true, reconciled_at = NOW() WHERE id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, recordID)
	return err
}

func (s *InsuranceStore) GetAllArticleInsurance(ctx context.Context) ([]models.InsuranceWithArticle, error) {
	query := `
		SELECT ai.id, ai.article_id, ai.policy_number, ai.provider, ai.coverage_type, ai.coverage_amount,
			ai.premium, ai.deductible, ai.terms, ai.is_active, ai.created_at, ai.updated_at,
			a.name, a.sku
		FROM article_insurance ai
		JOIN articles a ON ai.article_id = a.id
		WHERE ai.is_active = true
		ORDER BY a.name`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var results []models.InsuranceWithArticle
	for rows.Next() {
		var r models.InsuranceWithArticle
		if err := rows.Scan(
			&r.Insurance.ID, &r.Insurance.ArticleID, &r.Insurance.PolicyNumber, &r.Insurance.Provider,
			&r.Insurance.CoverageType, &r.Insurance.CoverageAmount, &r.Insurance.Premium, &r.Insurance.Deductible,
			&r.Insurance.Terms, &r.Insurance.IsActive, &r.Insurance.CreatedAt, &r.Insurance.UpdatedAt,
			&r.ArticleName, &r.ArticleSKU,
		); err != nil {
			return nil, err
		}
		results = append(results, r)
	}
	return results, rows.Err()
}

func (s *InsuranceStore) UpdateInsuranceClaimStatus(ctx context.Context, claimID uuid.UUID, status string, approvedAmount *float64, notes string) error {
	query := `UPDATE insurance_claims SET status = $1, approved_amount = $2, resolution_notes = $3, resolved_at = CASE WHEN $1 IN ('approved', 'rejected') THEN NOW() ELSE resolved_at END WHERE id = $4`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, status, approvedAmount, notes, claimID)
	return err
}

type NotificationsStore struct {
	db *sql.DB
}

func NewNotificationsStore(db *sql.DB) *NotificationsStore {
	return &NotificationsStore{db: db}
}

func (s *NotificationsStore) GetUserNotifications(ctx context.Context, userID uuid.UUID, limit int) ([]models.Notification, error) {
	query := `SELECT id, user_id, title, body, type, event_id, is_read, created_at FROM notifications WHERE user_id = $1 ORDER BY created_at DESC LIMIT $2`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, userID, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var notifications []models.Notification
	for rows.Next() {
		var n models.Notification
		if err := rows.Scan(&n.ID, &n.UserID, &n.Title, &n.Body, &n.Type, &n.EventID, &n.IsRead, &n.CreatedAt); err != nil {
			return nil, err
		}
		notifications = append(notifications, n)
	}
	return notifications, rows.Err()
}

func (s *NotificationsStore) Create(ctx context.Context, notification *models.Notification) error {
	query := `INSERT INTO notifications (user_id, title, body, type, event_id, is_read) VALUES ($1, $2, $3, $4, $5, $6) RETURNING id, created_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	return s.db.QueryRowContext(ctx, query,
		notification.UserID, notification.Title, notification.Body, notification.Type, notification.EventID, notification.IsRead,
	).Scan(&notification.ID, &notification.CreatedAt)
}
