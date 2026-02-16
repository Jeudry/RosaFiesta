ALTER TABLE events ADD COLUMN additional_costs DECIMAL
(12, 2) DEFAULT 0.00;
ALTER TABLE events ADD COLUMN admin_notes TEXT;
