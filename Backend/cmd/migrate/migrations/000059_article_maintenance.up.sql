-- Equipment Maintenance Logs
CREATE TABLE article_maintenance_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  article_id UUID REFERENCES articles(id) ON DELETE CASCADE,
  variant_id UUID REFERENCES article_variants(id) ON DELETE SET NULL,
  maintenance_type VARCHAR(50) NOT NULL,  -- cleaning, repair, inspection, replacement
  status VARCHAR(50) DEFAULT 'scheduled', -- scheduled, in_progress, completed, cancelled
  description TEXT,
  performed_by VARCHAR(255),
  performed_at TIMESTAMPTZ,
  next_maintenance_due TIMESTAMPTZ,
  cost DECIMAL(10,2) DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  created_by UUID REFERENCES users(id)
);

ALTER TABLE articles ADD COLUMN maintenance_status VARCHAR(50) DEFAULT 'operational';
