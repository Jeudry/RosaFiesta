-- Migración Pass-3: verificación de reseñas, alertas WhatsApp en ajustes del negocio,
-- fotos de montaje y teléfono de contacto en eventos/cotizaciones, y trazabilidad criptográfica de firma.

ALTER TABLE reviews ADD COLUMN IF NOT EXISTS verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE business_settings ADD COLUMN IF NOT EXISTS whatsapp_auto_alerts_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE events ADD COLUMN IF NOT EXISTS setup_photo_urls TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE events ADD COLUMN IF NOT EXISTS contact_phone TEXT NOT NULL DEFAULT '';

ALTER TABLE quotes ADD COLUMN IF NOT EXISTS signed_by_user_id TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS contract_hash_sha256 TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS contact_phone TEXT NOT NULL DEFAULT '';
