ALTER TABLE articles ADD COLUMN stock_quantity INTEGER DEFAULT 0;
-- Seed some initial stock for testing
UPDATE articles SET stock_quantity = 50 WHERE name_template LIKE '%Silla%';
UPDATE articles SET stock_quantity = 20 WHERE name_template LIKE '%Mesa%';
UPDATE articles SET stock_quantity = 10 WHERE name_template LIKE '%Mantel%';
