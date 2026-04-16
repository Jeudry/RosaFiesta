-- Feature #61: PayPal Integration
-- Add PayPal payment method and transaction tracking

ALTER TABLE payment_methods ADD COLUMN IF NOT EXISTS paypal_client_id TEXT;
ALTER TABLE payment_methods ADD COLUMN IF NOT EXISTS paypal_secret TEXT;
ALTER TABLE payment_methods ADD COLUMN IF NOT EXISTS is_paypal_enabled BOOLEAN DEFAULT false;

CREATE TABLE IF NOT EXISTS paypal_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paypal_order_id TEXT UNIQUE NOT NULL,
    paypal_capture_id TEXT,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,
    currency TEXT DEFAULT 'USD',
    status TEXT NOT NULL DEFAULT 'pending', -- pending, completed, failed, refunded
    paypal_response JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_paypal_transactions_event ON paypal_transactions(event_id);
CREATE INDEX idx_paypal_transactions_user ON paypal_transactions(user_id);
CREATE INDEX idx_paypal_transactions_status ON paypal_transactions(status);