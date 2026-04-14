CREATE TABLE delivery_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    base_radius_km DECIMAL(5,2) NOT NULL DEFAULT 50.0,
    max_radius_km DECIMAL(5,2) NOT NULL DEFAULT 100.0,
    travel_fee DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Base zone: San Cristóbal city center
INSERT INTO delivery_zones (name, base_radius_km, max_radius_km, travel_fee)
VALUES ('San Cristóbal Centro', 10.0, 30.0, 0.0);

-- Extended zone: San Cristóbal province (up to 2 hours)
INSERT INTO delivery_zones (name, base_radius_km, max_radius_km, travel_fee)
VALUES ('San Cristóbal Extendido', 30.0, 80.0, 1500.00);

-- Remote: Beyond 2 hours (with travel fee)
INSERT INTO delivery_zones (name, base_radius_km, max_radius_km, travel_fee)
VALUES ('Zona Remota', 80.0, 150.0, 3500.00);