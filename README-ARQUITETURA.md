# README – Arquitetura (Atualizada)

## Diagnóstico dos problemas encontrados
1. `TenantFilter` dependia de `X-Tenant-ID`, permitindo tenant vindo do frontend.
2. Webhook WhatsApp aceitava `tenantId` no DTO (`WhatsAppWebhookRequest`), inseguro e acoplado ao cliente.
3. Conta WhatsApp era global por `application.yaml`, sem isolamento por empresa.
4. `TenantService` criava banco usando `DataSource` primário (roteado), risco de usar datasource errado.
5. Ausência de resolução de tenant por subdomínio em requests HTTP.

## Solução implementada
- Master datasource explícito (`masterDataSource`) e roteamento tenant como datasource primário.
- Diretório master via `MasterTenantDirectory` (consultas de tenants e contas WhatsApp).
- `TenantFilter` resolve tenant por subdomínio e mantém fallback legado por header.
- Webhook processado por `phone_number_id` (Meta), nunca por `tenantId` de payload.
- Notificações WhatsApp enviadas por credenciais da empresa atual.

## Fluxo de webhook WhatsApp
1. `POST /whatsapp/webhook` recebe payload Meta.
2. Controller extrai `phone_number_id`.
3. Busca tenant no master (`whatsapp_accounts`).
4. Seta `TenantContext`.
5. Service cria/atualiza cliente e agenda no banco do tenant.

## Escalabilidade
- Sem dependência de memória local para mapear tenant/whatsapp.
- Resolução por dados da requisição + master DB.
- Compatível com múltiplas instâncias horizontais.
