-- Feature #59: Article Insurance
-- Insurance policies for high-value rental items

CREATE TABLE IF NOT EXISTS article_insurance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    policy_number TEXT UNIQUE NOT NULL,
    provider TEXT NOT NULL, -- "RosaFiesta Protection", "ThirdParty Insurance", etc.
    coverage_type TEXT NOT NULL, -- "damage", "loss", "theft", "comprehensive"
    coverage_amount DECIMAL(10, 2) NOT NULL, -- max coverage amount
    premium DECIMAL(10, 2) NOT NULL, -- cost per event
    deductible DECIMAL(10, 2) DEFAULT 0, -- amount client pays before coverage
    terms TEXT, -- coverage terms and conditions
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS event_insurance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    insurance_id UUID NOT NULL REFERENCES article_insurance(id),
    articles_covered JSONB NOT NULL, -- array of article_ids covered
    total_coverage DECIMAL(10, 2) NOT NULL,
    premium_paid DECIMAL(10, 2) NOT NULL,
    status TEXT NOT NULL DEFAULT 'active', -- active, claimed, expired, cancelled
    claim_id UUID, -- reference to insurance_claims if filed
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS insurance_claims (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_insurance_id UUID NOT NULL REFERENCES event_insurance(id),
    claim_number TEXT UNIQUE NOT NULL,
    incident_type TEXT NOT NULL, -- "damage", "loss", "theft"
    description TEXT NOT NULL,
    claimed_amount DECIMAL(10, 2) NOT NULL,
    approved_amount DECIMAL(10, 2),
    status TEXT NOT NULL DEFAULT 'pending', -- pending, under_review, approved, rejected, paid
    incident_date DATE NOT NULL,
    filed_date DATE NOT NULL DEFAULT CURRENT_DATE,
    resolution_notes TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_article_insurance_article ON article_insurance(article_id);
CREATE INDEX idx_article_insurance_active ON article_insurance(is_active);
CREATE INDEX idx_event_insurance_event ON event_insurance(event_id);
CREATE INDEX idx_event_insurance_status ON event_insurance(status);
CREATE INDEX idx_insurance_claims_event_insurance ON insurance_claims(event_insurance_id);
CREATE INDEX idx_insurance_claims_status ON insurance_claims(status);