# Changelog

## 2026-04-12

### Funcionalidades adicionadas
- Módulo de configuração da empresa (`company_profiles`).
- Módulo de serviços (`service_offerings`).
- Módulo de profissionais (`professionals` + `professional_services`).
- CRM de clientes (`customers` + `customer_histories`).
- Dashboard com métricas para frontend.
- Módulo de automações por tenant (`tenant_settings` + `automations`) com scheduler.
- Webhook WhatsApp para agendamento automático com extração via OpenAI.
- Evolução do agendamento com referências a cliente/serviço/profissional e preço.

### Melhorias feitas
- Regras avançadas no agendamento:
  - Validação de duração do serviço.
  - Validação de horário de funcionamento.
  - Validação de disponibilidade do profissional.
  - Atualização automática de CRM após atendimento.
- Tratamento de erro de negócio e logging estruturado de erro interno.

### Decisões técnicas
- Mantida a base de multi-tenancy existente (sem alterações em resolução dinâmica de tenant).
- Estratégia de compatibilidade: campos antigos de agendamento preservados.
- IA opcional nas automações via `tenant_settings.ai_enabled`.

### Evoluções futuras
- Melhorar parser de horário de funcionamento para múltiplas janelas por dia.
- Adicionar idempotência e assinatura no webhook WhatsApp.
- Criar trilha de auditoria completa para eventos de automação.
