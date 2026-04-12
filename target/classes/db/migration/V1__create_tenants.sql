CREATE TABLE tenants (
                         id SERIAL PRIMARY KEY,
                         nome VARCHAR(255),
                         tenantId VARCHAR(255),
                         database_name VARCHAR(255),
                         username VARCHAR(255),
                         password VARCHAR(255)
);