package models

import (
	"time"

	"github.com/google/uuid"
)

type PayPalTransaction struct {
	ID              uuid.UUID `json:"id"`
	PayPalOrderID   string    `json:"paypal_order_id"`
	PayPalCaptureID *string   `json:"paypal_capture_id,omitempty"`
	EventID         uuid.UUID `json:"event_id"`
	UserID          uuid.UUID `json:"user_id"`
	Amount          float64   `json:"amount"`
	Currency        string    `json:"currency"`
	Status          string    `json:"status"`
	PayPalResponse  *string   `json:"paypal_response,omitempty"`
	CreatedAt       time.Time `json:"created_at"`
	UpdatedAt       time.Time `json:"updated_at"`
}

type ArticleInsurance struct {
	ID             uuid.UUID `json:"id"`
	ArticleID      uuid.UUID `json:"article_id"`
	PolicyNumber   string    `json:"policy_number"`
	Provider       string    `json:"provider"`
	CoverageType   string    `json:"coverage_type"`
	CoverageAmount float64   `json:"coverage_amount"`
	Premium        float64   `json:"premium"`
	Deductible     float64   `json:"deductible"`
	Terms          *string   `json:"terms,omitempty"`
	IsActive       bool      `json:"is_active"`
	CreatedAt      time.Time `json:"created_at"`
	UpdatedAt      time.Time `json:"updated_at"`
}

type EventInsurance struct {
	ID              uuid.UUID  `json:"id"`
	EventID         uuid.UUID  `json:"event_id"`
	InsuranceID     uuid.UUID  `json:"insurance_id"`
	ArticlesCovered []string   `json:"articles_covered"`
	TotalCoverage   float64    `json:"total_coverage"`
	PremiumPaid     float64    `json:"premium_paid"`
	Status          string     `json:"status"`
	ClaimID         *uuid.UUID `json:"claim_id,omitempty"`
	CreatedAt       time.Time  `json:"created_at"`
	UpdatedAt       time.Time  `json:"updated_at"`
}

type InsuranceClaim struct {
	ID               uuid.UUID  `json:"id"`
	EventInsuranceID uuid.UUID  `json:"event_insurance_id"`
	ClaimNumber      string     `json:"claim_number"`
	IncidentType     string     `json:"incident_type"`
	Description      string     `json:"description"`
	ClaimedAmount    float64    `json:"claimed_amount"`
	ApprovedAmount   *float64   `json:"approved_amount,omitempty"`
	Status           string     `json:"status"`
	IncidentDate     time.Time  `json:"incident_date"`
	FiledDate        time.Time  `json:"filed_date"`
	ResolutionNotes  *string    `json:"resolution_notes,omitempty"`
	ResolvedAt       *time.Time `json:"resolved_at,omitempty"`
	CreatedAt        time.Time  `json:"created_at"`
	UpdatedAt        time.Time  `json:"updated_at"`
}

type FinancialCategory struct {
	ID          uuid.UUID `json:"id"`
	Name        string    `json:"name"`
	Type        string    `json:"type"`
	Description *string   `json:"description,omitempty"`
	Color       string    `json:"color"`
	IsActive    bool      `json:"is_active"`
	CreatedAt   time.Time `json:"created_at"`
}

type FinancialRecord struct {
	ID              uuid.UUID          `json:"id"`
	EventID         *uuid.UUID         `json:"event_id,omitempty"`
	CategoryID      uuid.UUID          `json:"category_id"`
	Type            string             `json:"type"`
	Amount          float64            `json:"amount"`
	Currency        string             `json:"currency"`
	Description     string             `json:"description"`
	ReferenceNumber *string            `json:"reference_number,omitempty"`
	PaymentMethod   *string            `json:"payment_method,omitempty"`
	RecordedBy      *uuid.UUID         `json:"recorded_by,omitempty"`
	RecordDate      time.Time          `json:"record_date"`
	IsReconciled    bool               `json:"is_reconciled"`
	ReconciledAt    *time.Time         `json:"reconciled_at,omitempty"`
	Metadata        *map[string]any    `json:"metadata,omitempty"`
	CreatedAt       time.Time          `json:"created_at"`
	UpdatedAt       time.Time          `json:"updated_at"`
	Category        *FinancialCategory `json:"category,omitempty"`
}

type Invoice struct {
	ID             uuid.UUID  `json:"id"`
	InvoiceNumber  string     `json:"invoice_number"`
	EventID        *uuid.UUID `json:"event_id,omitempty"`
	ClientID       uuid.UUID  `json:"client_id"`
	Subtotal       float64    `json:"subtotal"`
	TaxAmount      float64    `json:"tax_amount"`
	DiscountAmount float64    `json:"discount_amount"`
	Total          float64    `json:"total"`
	AmountPaid     float64    `json:"amount_paid"`
	Currency       string     `json:"currency"`
	Status         string     `json:"status"`
	IssueDate      time.Time  `json:"issue_date"`
	DueDate        *time.Time `json:"due_date,omitempty"`
	PaidDate       *time.Time `json:"paid_date,omitempty"`
	Notes          *string    `json:"notes,omitempty"`
	Terms          *string    `json:"terms,omitempty"`
	CreatedBy      *uuid.UUID `json:"created_by,omitempty"`
	CreatedAt      time.Time  `json:"created_at"`
	UpdatedAt      time.Time  `json:"updated_at"`
}

type ExpenseVendor struct {
	ID          uuid.UUID `json:"id"`
	Name        string    `json:"name"`
	ContactName *string   `json:"contact_name,omitempty"`
	Email       *string   `json:"email,omitempty"`
	Phone       *string   `json:"phone,omitempty"`
	Address     *string   `json:"address,omitempty"`
	Category    *string   `json:"category,omitempty"`
	Notes       *string   `json:"notes,omitempty"`
	IsActive    bool      `json:"is_active"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

type VendorPayment struct {
	ID              uuid.UUID  `json:"id"`
	VendorID        uuid.UUID  `json:"vendor_id"`
	Amount          float64    `json:"amount"`
	Currency        string     `json:"currency"`
	PaymentDate     time.Time  `json:"payment_date"`
	PaymentMethod   string     `json:"payment_method"`
	ReferenceNumber *string    `json:"reference_number,omitempty"`
	Description     *string    `json:"description,omitempty"`
	RecordedBy      *uuid.UUID `json:"recorded_by,omitempty"`
	CreatedAt       time.Time  `json:"created_at"`
}

type ClientAuditLog struct {
	ID         uuid.UUID  `json:"id"`
	UserID     uuid.UUID  `json:"user_id"`
	Action     string     `json:"action"`
	EntityType string     `json:"entity_type"`
	EntityID   *string    `json:"entity_id,omitempty"`
	Details    *string    `json:"details,omitempty"`
	RecordedBy *uuid.UUID `json:"recorded_by,omitempty"`
	IPAddress  *string    `json:"ip_address,omitempty"`
	CreatedAt  time.Time  `json:"created_at"`
}

type FinancialSummary struct {
	TotalIncome     float64 `json:"total_income"`
	TotalExpenses   float64 `json:"total_expenses"`
	NetProfit       float64 `json:"net_profit"`
	PendingPayments float64 `json:"pending_payments"`
	PeriodStart     string  `json:"period_start"`
	PeriodEnd       string  `json:"period_end"`
}

type InsuranceWithArticle struct {
	Insurance   ArticleInsurance `json:"insurance"`
	ArticleName string           `json:"article_name"`
	ArticleSKU  string           `json:"article_sku"`
}

type Notification struct {
	ID        uuid.UUID `json:"id"`
	UserID    uuid.UUID `json:"user_id"`
	Title     string    `json:"title"`
	Body      string    `json:"body"`
	Type      string    `json:"type"`
	EventID   *string   `json:"event_id,omitempty"`
	IsRead    bool      `json:"is_read"`
	CreatedAt time.Time `json:"created_at"`
}
