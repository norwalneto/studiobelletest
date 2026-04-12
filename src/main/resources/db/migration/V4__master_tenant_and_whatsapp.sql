ALTER TABLE tenants RENAME COLUMN tenantid TO tenant_id;
ALTER TABLE tenants RENAME COLUMN nome TO name;

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS subdomain VARCHAR(120);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS phone_number_id VARCHAR(120);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS business_account_id VARCHAR(120);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

CREATE UNIQUE INDEX IF NOT EXISTS uk_tenants_tenant_id ON tenants(tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tenants_subdomain ON tenants(subdomain);

CREATE TABLE IF NOT EXISTS whatsapp_accounts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(120) NOT NULL,
    phone_number_id VARCHAR(120) NOT NULL,
    business_account_id VARCHAR(120),
    access_token TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_whatsapp_accounts_tenant UNIQUE (tenant_id),
    CONSTRAINT uk_whatsapp_accounts_phone UNIQUE (phone_number_id)
);
