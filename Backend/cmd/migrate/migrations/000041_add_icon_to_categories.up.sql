ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS icon VARCHAR(64);

-- Populate existing categories with icon + image so the catalog grid renders nicely.
UPDATE categories
SET icon = 'chair',
    image_url = 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=800'
WHERE name = 'Furniture' AND (icon IS NULL OR icon = '');

UPDATE categories
SET icon = 'auto_awesome',
    image_url = 'https://images.unsplash.com/photo-1478146059778-26028b07395a?w=800'
WHERE name = 'Decor' AND (icon IS NULL OR icon = '');

-- Seed extra categories so the grid feels populated. Insert only if missing.
INSERT INTO categories (name, description, icon, image_url, created_by)
SELECT 'Iluminación', 'Luces, neón y guirnaldas para ambientar', 'lightbulb',
       'https://images.unsplash.com/photo-1514849302-984523450cf4?w=800', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Iluminación');

INSERT INTO categories (name, description, icon, image_url, created_by)
SELECT 'Floral', 'Arreglos, arcos y centros de mesa florales', 'local_florist',
       'https://images.unsplash.com/photo-1519741497674-611481863552?w=800', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Floral');

INSERT INTO categories (name, description, icon, image_url, created_by)
SELECT 'Globos', 'Arcos de globos y guirnaldas para toda celebración', 'celebration',
       'https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=800', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Globos');

INSERT INTO categories (name, description, icon, image_url, created_by)
SELECT 'Mantelería', 'Manteles, caminos de mesa y servilletas', 'table_restaurant',
       'https://images.unsplash.com/photo-1464699908537-0954e50791ee?w=800', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mantelería');

INSERT INTO categories (name, description, icon, image_url, created_by)
SELECT 'Mesa Dulce', 'Decoración completa para mesas de dulces y postres', 'cake',
       'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=800', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mesa Dulce');

INSERT INTO categories (name, description, icon, image_url, created_by)
SELECT 'Letreros', 'Letreros neón y backdrops personalizados', 'auto_awesome_motion',
       'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Letreros');
