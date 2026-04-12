# StudioBelle SaaS API

Backend Spring Boot para CRM inteligente + agendamento multi-tenant com **database por tenant**, roteamento por **subdomínio** e integração **WhatsApp por empresa**.

## O que foi evoluído
- Master database como diretório de tenants (`tenants`) e contas WhatsApp (`whatsapp_accounts`).
- Resolução automática de tenant por subdomínio (`cliente1.seusistema.com`) no `TenantFilter`.
- Webhook do WhatsApp sem `tenantId` no payload: tenant é resolvido por `phone_number_id` no master.
- Envio de mensagens WhatsApp por tenant (token + phone_number_id por empresa).
- Compatibilidade preservada: fallback para `X-Tenant-ID` em ambientes legados.

## Arquitetura multi-tenant (database por cliente)
1. Request chega no backend.
2. `TenantFilter` extrai subdomínio do host e resolve `tenant_id` no master.
3. `TenantContext` guarda o tenant da requisição atual.
4. `TenantRoutingDataSource` seleciona automaticamente o banco do tenant.
5. Todas as operações de negócio ocorrem no banco isolado da empresa.

> Para endpoints de webhook, o tenant é definido no controller a partir do `phone_number_id` do evento.

## WhatsApp por empresa
- Cada empresa possui um registro em `whatsapp_accounts`:
  - `tenant_id`
  - `phone_number_id`
  - `business_account_id`
  - `access_token`
- No webhook:
  1. extrai `phone_number_id` do payload Meta;
  2. busca no master;
  3. define `TenantContext`;
  4. processa agendamento no banco correto.
- No envio de notificações:
  - token e phone number são buscados por tenant no master.

## Subdomínio
Suporta:
- `cliente1.localhost` (dev)
- `cliente1.seusistema.com` (produção)

## Executar
```bash
./mvnw spring-boot:run
```

## Migrações
- `V1`: tabela inicial de tenants.
- `V2`: auth e appointments.
- `V3`: CRM/company/automation.
- `V4`: ajustes do master (`tenant_id`, `subdomain`, campos WhatsApp) + `whatsapp_accounts`.
