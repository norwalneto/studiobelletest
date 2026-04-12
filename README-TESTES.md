# Testes recomendados (multi-tenant + subdomínio + WhatsApp)

## 1) Isolamento entre clientes
1. Criar tenant A e B.
2. Inserir cliente/agendamento no tenant A.
3. Consultar tenant B e validar ausência dos dados de A.

## 2) Resolução por subdomínio
1. Enviar request com host `cliente1.localhost`.
2. Validar roteamento para `tenant_id` associado no master.

## 3) Webhook no banco correto
1. Cadastrar `whatsapp_accounts.phone_number_id` para tenant A.
2. Enviar payload de webhook contendo esse `phone_number_id`.
3. Validar agendamento salvo apenas no banco do tenant A.

## 4) Segurança
1. Token de tenant A em subdomínio tenant B.
2. Validar bloqueio no filtro JWT por mismatch de tenant.
