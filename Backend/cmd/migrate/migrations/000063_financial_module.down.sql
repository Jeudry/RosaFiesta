-- Feature #53: Financial Module
-- Income/expense tracking and invoicing

CREATE TABLE IF NOT EXISTS financial_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('income', 'expense')),
    description TEXT,
    color TEXT DEFAULT '#6366F1',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS financial_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events(id) ON DELETE SET NULL,
    category_id UUID NOT NULL REFERENCES financial_categories(id),
    type TEXT NOT NULL CHECK (type IN ('income', 'expense')),
    amount DECIMAL(10, 2) NOT NULL,
    currency TEXT DEFAULT 'DOP',
    description TEXT NOT NULL,
    reference_number TEXT, -- invoice number, receipt number, etc.
    payment_method TEXT, -- "cash", "transfer", "card", "paypal"
    recorded_by UUID REFERENCES users(id),
    record_date DATE NOT NULL DEFAULT CURRENT_DATE,
    is_reconciled BOOLEAN DEFAULT false,
    reconciled_at TIMESTAMPTZ,
    metadata JSONB, -- additional flexible data
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number TEXT UNIQUE NOT NULL,
    event_id UUID REFERENCES events(id) ON DELETE SET NULL,
    client_id UUID NOT NULL REFERENCES users(id),
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    amount_paid DECIMAL(10, 2) DEFAULT 0,
    currency TEXT DEFAULT 'DOP',
    status TEXT NOT NULL DEFAULT 'draft', -- draft, sent, paid, partial, overdue, cancelled
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE,
    paid_date DATE,
    notes TEXT,
    terms TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS expense_vendors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    contact_name TEXT,
    email TEXT,
    phone TEXT,
    address TEXT,
    category TEXT, -- "supplies", "equipment", "services", "utilities", "other"
    notes TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vendor_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id UUID NOT NULL REFERENCES expense_vendors(id),
    amount DECIMAL(10, 2) NOT NULL,
    currency TEXT DEFAULT 'DOP',
    payment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    payment_method TEXT NOT NULL, -- "cash", "transfer", "check"
    reference_number TEXT,
    description TEXT,
    recorded_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Insert default financial categories
INSERT INTO financial_categories (name, type, color, description) VALUES
    ('Alquiler de equipos', 'income', '#22C55E', 'Ingresos por alquiler de artículos'),
    ('Venta de productos', 'income', '#10B981', 'Ingresos por ventas directas'),
    ('Servicios adicionales', 'income', '#14B8A6', 'Cargos por delivery, montaje, etc'),
    ('Proveedores', 'expense', '#EF4444', 'Pagos a proveedores y servicios'),
    ('Transporte', 'expense', '#F97316', 'Gastos de transporte y combustible'),
    ('Mantenimiento', 'expense', '#EAB308', 'Reparación y limpieza de equipos'),
    ('Servicios públicos', 'expense', '#6366F1', 'Electricidad, agua, internet'),
    ('Marketing', 'expense', '#EC4899', 'Publicidad y promoción'),
    ('Administración', 'expense', '#8B5CF6', 'Gastos generales de oficina');

CREATE INDEX idx_financial_records_event ON financial_records(event_id);
CREATE INDEX idx_financial_records_category ON financial_records(category_id);
CREATE INDEX idx_financial_records_type ON financial_records(type);
CREATE INDEX idx_financial_records_date ON financial_records(record_date);
CREATE INDEX idx_financial_records_reconciled ON financial_records(is_reconciled);
CREATE INDEX idx_invoices_event ON invoices(event_id);
CREATE INDEX idx_invoices_client ON invoices(client_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_vendor_payments_vendor ON vendor_payments(vendor_id);