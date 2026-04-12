CREATE TABLE IF NOT EXISTS company_profiles (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    phone VARCHAR(40),
    whatsapp VARCHAR(40),
    address VARCHAR(255),
    business_hours VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS service_offerings (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    name VARCHAR(120) NOT NULL,
    duration_minutes INT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS professionals (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    name VARCHAR(120) NOT NULL,
    working_hours VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS professional_services (
    professional_id BIGINT NOT NULL,
    service_offering_id BIGINT NOT NULL,
    PRIMARY KEY (professional_id, service_offering_id),
    CONSTRAINT fk_professional_services_professional FOREIGN KEY (professional_id) REFERENCES professionals(id),
    CONSTRAINT fk_professional_services_service FOREIGN KEY (service_offering_id) REFERENCES service_offerings(id)
);

CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    email VARCHAR(180),
    last_visit TIMESTAMP,
    frequency VARCHAR(80),
    total_spent NUMERIC(12,2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS customer_histories (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL,
    service_name VARCHAR(140) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    attended_at TIMESTAMP NOT NULL,
    notes VARCHAR(300),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_customer_history_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS tenant_settings (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL UNIQUE,
    inactivity_days INT NOT NULL DEFAULT 30,
    ai_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    automation_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS automations (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    template VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

ALTER TABLE appointments ADD COLUMN IF NOT EXISTS customer_id BIGINT;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS service_id BIGINT;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS professional_id BIGINT;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS service_price NUMERIC(10,2);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_appointment_customer') THEN
        ALTER TABLE appointments ADD CONSTRAINT fk_appointment_customer FOREIGN KEY (customer_id) REFERENCES customers(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_appointment_service') THEN
        ALTER TABLE appointments ADD CONSTRAINT fk_appointment_service FOREIGN KEY (service_id) REFERENCES service_offerings(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_appointment_professional') THEN
        ALTER TABLE appointments ADD CONSTRAINT fk_appointment_professional FOREIGN KEY (professional_id) REFERENCES professionals(id);
    END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_company_profile_tenant ON company_profiles(tenant_id);
CREATE INDEX IF NOT EXISTS idx_service_offering_tenant ON service_offerings(tenant_id);
CREATE INDEX IF NOT EXISTS idx_professional_tenant ON professionals(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_tenant ON customers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customer_histories_tenant ON customer_histories(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_settings_tenant ON tenant_settings(tenant_id);
CREATE INDEX IF NOT EXISTS idx_automations_tenant ON automations(tenant_id);
