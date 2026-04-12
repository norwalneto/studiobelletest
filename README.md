# StudioBelle SaaS API

Backend Spring Boot para CRM inteligente + agendamento multi-tenant (database por tenant), focado em barbearias, salões e clínicas de estética.

## Módulos principais
- Autenticação JWT + refresh token.
- Multi-tenancy por banco de dados (já existente, mantido sem mudanças estruturais).
- Configuração da empresa por tenant.
- Catálogo de serviços e profissionais por tenant.
- CRM de clientes com histórico e inatividade.
- Agendamentos com regras avançadas (serviço, duração, disponibilidade e horário).
- Integração WhatsApp + OpenAI.
- Automações com scheduler (reativação, lembretes e pós-atendimento).
- Dashboard para frontend.

## Endpoints novos (resumo)
- `GET/PUT /company-profile`
- `GET/POST/PUT/DELETE /services`
- `GET/POST/PUT/DELETE /professionals`
- `GET/POST/PUT/DELETE /customers`
- `GET /customers/{id}/history`
- `GET /customers/inactive?days=30`
- `GET /dashboard/metrics`
- `GET/POST /automations`
- `GET/PUT /automations/settings`
- `POST /whatsapp/webhook`

## Observações de arquitetura
- Todos os dados de negócio usam `tenant_id` e são consultados filtrando tenant atual.
- Não houve alteração na estratégia de resolução dinâmica de tenant.
- O webhook de WhatsApp permite informar `tenantId` no payload para roteamento seguro.

## Execução
```bash
./mvnw spring-boot:run
```

## Migrações
- `V1` e `V2` mantidas.
- `V3__crm_company_automation.sql` adiciona CRM, configurações da empresa, automações e extensões em agendamento.
