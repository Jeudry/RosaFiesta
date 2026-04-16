-- Recurring Events
CREATE TABLE recurring_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255),
  guest_count INT DEFAULT 50,
  budget DECIMAL(10,2) DEFAULT 0,
  frequency VARCHAR(20) NOT NULL,  -- weekly, biweekly, monthly
  interval_value INT DEFAULT 1,   -- every N weeks/months
  days_of_week INT[],             -- [1,3,5] for Mon,Wed,Fri (ISO weekday)
  start_date DATE NOT NULL,
  end_date DATE,
  next_run_date DATE NOT NULL,
  last_run_event_id UUID REFERENCES events(id) ON DELETE SET NULL,
  auto_create BOOLEAN DEFAULT false,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
