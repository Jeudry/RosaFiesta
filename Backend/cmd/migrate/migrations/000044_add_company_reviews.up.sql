-- Create company_reviews table for RosaFiesta company-wide reviews
CREATE TABLE company_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL DEFAULT '',
    source VARCHAR(50) NOT NULL DEFAULT 'direct',
    created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_company_reviews_user ON company_reviews(user_id);
CREATE INDEX idx_company_reviews_rating ON company_reviews(rating);
CREATE INDEX idx_company_reviews_source ON company_reviews(source);
