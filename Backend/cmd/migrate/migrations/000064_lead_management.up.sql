-- Feature #1: Lead Management/CRM
-- Leads capture, follow-ups, sales pipeline

CREATE TABLE IF NOT EXISTS leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source TEXT NOT NULL DEFAULT 'website',
    status TEXT NOT NULL DEFAULT 'new' CHECK (status IN ('new', 'contacted', 'qualified', 'proposal', 'negotiating', 'won', 'lost')),
    priority TEXT NOT NULL DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
    client_name TEXT NOT NULL,
    client_email TEXT,
    client_phone TEXT,
    event_type TEXT,
    event_date DATE,
    guest_count INTEGER,
    budget_min DECIMAL(10,2),
    budget_max DECIMAL(10,2),
    notes TEXT,
    assigned_to UUID REFERENCES users(id),
    last_contact_at TIMESTAMPTZ,
    next_follow_up DATE,
    converted_to_event_id UUID REFERENCES events(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lead_followups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    follow_up_date DATE NOT NULL,
    follow_up_type TEXT CHECK (follow_up_type IN ('call', 'email', 'whatsapp', 'visit', 'meeting')),
    notes TEXT,
    completed BOOLEAN DEFAULT false,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lead_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    activity_type TEXT NOT NULL CHECK (activity_type IN ('created', 'status_changed', 'note_added', 'follow_up_set', 'call_made', 'email_sent', 'whatsapp_sent', 'meeting_scheduled')),
    description TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_assigned_to ON leads(assigned_to);
CREATE INDEX idx_lead_followups_lead_id ON lead_followups(lead_id);
CREATE INDEX idx_lead_followups_date ON lead_followups(follow_up_date);
CREATE INDEX idx_lead_activities_lead_id ON lead_activities(lead_id);
