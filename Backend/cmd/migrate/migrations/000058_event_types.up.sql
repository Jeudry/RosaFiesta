-- Event Types: pre-configured event templates with suggested items
CREATE TABLE event_types (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(100) NOT NULL,
  description TEXT,
  suggested_budget_min DECIMAL(10,2),
  suggested_budget_max DECIMAL(10,2),
  default_guest_count INT DEFAULT 50,
  color VARCHAR(7) DEFAULT '#FF3CAC',
  icon VARCHAR(50) DEFAULT '🎉',
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE event_type_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type_id UUID REFERENCES event_types(id) ON DELETE CASCADE,
  article_id UUID REFERENCES articles(id) ON DELETE CASCADE,
  category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
  quantity INT DEFAULT 1,
  sort_order INT DEFAULT 0,
  UNIQUE(event_type_id, article_id)
);

-- Seed default event types
INSERT INTO event_types (name, description, suggested_budget_min, suggested_budget_max, default_guest_count, color, icon) VALUES
  ('Cumpleaños 15', 'Fiesta de quinceañera tradicional', 25000, 80000, 150, '#FF3CAC', '🎂'),
  ('Boda', 'Ceremonia y recepción nupcial', 50000, 200000, 200, '#FFB800', '💒'),
  ('Baby Shower', 'Celebración迎接新生儿', 8000, 25000, 40, '#00D4AA', '🍼'),
  ('Graduación', 'Celebración de fin de estudios', 15000, 50000, 100, '#8B5CF6', '🎓'),
  ('Cumpleaños Infantil', 'Fiesta infantil temática', 5000, 15000, 30, '#4FC3F7', '🎈'),
  ('Corporativo', 'Evento empresarial o de empresa', 20000, 100000, 80, '#607D8B', '🏢');
