-- Feature #59: Article Insurance

CREATE TABLE IF NOT EXISTS article_insurance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL,
    provider TEXT NOT NULL,
    policy_number TEXT NOT NULL,
    coverage_amount DECIMAL(10,2) NOT NULL,
    premium DECIMAL(10,2) NOT NULL,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS event_insurance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    provider TEXT NOT NULL,
    policy_number TEXT NOT NULL,
    coverage_amount DECIMAL(10,2) NOT NULL,
    premium DECIMAL(10,2) NOT NULL,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS insurance_claims (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    insurance_id UUID NOT NULL,
    insurance_type TEXT NOT NULL,
    claim_number TEXT NOT NULL,
    description TEXT NOT NULL,
    amount_claimed DECIMAL(10,2) NOT NULL,
    amount_approved DECIMAL(10,2),
    status TEXT DEFAULT 'pending',
    filed_at TIMESTAMPTZ DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);