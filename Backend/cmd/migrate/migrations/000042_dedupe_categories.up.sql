-- Dedupe categories that share the same name. Keep the oldest row as canonical
-- and re-point any articles that reference the duplicates before deleting them.

WITH ranked AS (
    SELECT id,
           name,
           ROW_NUMBER() OVER (PARTITION BY name ORDER BY created ASC, id ASC) AS rn,
           FIRST_VALUE(id) OVER (PARTITION BY name ORDER BY created ASC, id ASC) AS canonical_id
    FROM categories
    WHERE deleted IS NULL
),
duplicates AS (
    SELECT id, canonical_id FROM ranked WHERE rn > 1
)
UPDATE articles
SET category_id = duplicates.canonical_id
FROM duplicates
WHERE articles.category_id = duplicates.id;

-- Now delete the duplicate rows.
WITH ranked AS (
    SELECT id,
           name,
           ROW_NUMBER() OVER (PARTITION BY name ORDER BY created ASC, id ASC) AS rn
    FROM categories
    WHERE deleted IS NULL
)
DELETE FROM categories
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

-- Add a unique partial index on name (only for non-deleted rows) so future
-- re-runs of the seed do not create duplicates.
CREATE UNIQUE INDEX IF NOT EXISTS categories_name_unique_active
    ON categories (name)
    WHERE deleted IS NULL;
