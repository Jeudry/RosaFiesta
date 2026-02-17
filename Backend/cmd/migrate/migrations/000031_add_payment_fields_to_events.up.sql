ALTER TABLE events ADD COLUMN payment_status VARCHAR
(50) DEFAULT 'pending';
ALTER TABLE events ADD COLUMN payment_method VARCHAR
(50);
ALTER TABLE events ADD COLUMN paid_at TIMESTAMP;
