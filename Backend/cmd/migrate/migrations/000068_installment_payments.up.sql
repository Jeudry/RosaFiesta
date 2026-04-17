ALTER TABLE events ADD COLUMN deposit_paid BOOLEAN DEFAULT FALSE;
ALTER TABLE events ADD COLUMN deposit_amount INT DEFAULT 0;
ALTER TABLE events ADD COLUMN deposit_paid_at TIMESTAMPTZ;
ALTER TABLE events ADD COLUMN remaining_amount INT DEFAULT 0;
ALTER TABLE events ADD COLUMN installment_due_date TIMESTAMPTZ;
ALTER TABLE events ADD COLUMN total_quote INT DEFAULT 0;

CREATE TABLE installment_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    amount INT NOT NULL,
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'pending',
    due_date TIMESTAMPTZ,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_installment_payments_event_id ON installment_payments(event_id);
CREATE INDEX idx_installment_payments_due_date ON installment_payments(due_date) WHERE payment_status = 'pending';