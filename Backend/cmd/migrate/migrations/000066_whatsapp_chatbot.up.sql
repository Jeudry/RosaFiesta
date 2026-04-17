-- Feature #13: WhatsApp Chatbot - auto-respond FAQ
-- FAQ database for auto-responses

CREATE TABLE IF NOT EXISTS whatsapp_faqs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keyword TEXT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category TEXT DEFAULT 'general',
    priority INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    language TEXT DEFAULT 'es',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chatbot_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_phone TEXT NOT NULL,
    client_name TEXT,
    message TEXT NOT NULL,
    response TEXT,
    faq_id UUID REFERENCES whatsapp_faqs(id),
    was_helpful BOOLEAN,
    session_id TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_whatsapp_faqs_keyword ON whatsapp_faqs(keyword);
CREATE INDEX idx_whatsapp_faqs_active ON whatsapp_faqs(is_active);
CREATE INDEX idx_chatbot_conversations_phone ON chatbot_conversations(client_phone);
CREATE INDEX idx_chatbot_conversations_session ON chatbot_conversations(session_id);
