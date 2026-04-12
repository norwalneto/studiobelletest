# README – Explicação do Projeto

## Visão Geral
Este projeto é um **SaaS multi-tenant com banco por tenant**. O tenant é identificado por `X-Tenant-ID` no header e roteado dinamicamente para o DataSource correto.

A evolução implementada inclui:
- autenticação JWT com refresh token;
- gestão completa de usuários;
- gestão completa de agendamentos com prevenção de conflito;
- integração desacoplada com WhatsApp;
- integração desacoplada com OpenAI para geração de mensagens.

---

## Arquitetura por camadas

- **Controller**: recebe requisição, valida payload e delega para service.
- **Service**: regras de negócio (autenticação, conflito de agenda, autorização).
- **Repository**: persistência com Spring Data JPA.
- **DTO**: contratos de entrada/saída da API.
- **Security**: filtro JWT, contexto de usuário autenticado e utilitários.
- **Integration**: serviços externos (WhatsApp/OpenAI) desacoplados.
- **Tenant**: contexto e roteamento de DataSource já existente e preservado.

---

## Multi-tenant + autenticação

1. `TenantFilter` lê `X-Tenant-ID` e seta `TenantContext`.
2. `TenantRoutingDataSource` usa `TenantContext` para selecionar o banco do tenant.
3. `JwtAuthenticationFilter` valida o token e garante que o `tenantId` do token seja igual ao tenant da requisição.
4. O usuário autenticado é inserido em `SecurityContext` para validação de permissões.

> Resultado: mesmo com token válido, acesso é bloqueado se o tenant não corresponder.

---

## Fluxo de autenticação JWT

### Registro (`POST /auth/register`)
- valida payload;
- valida tenant atual;
- salva usuário com senha hash;
- gera access token + refresh token.

### Login (`POST /auth/login`)
- valida credenciais;
- valida tenant;
- invalida refresh token antigo;
- retorna novo access token + refresh token.

### Refresh (`POST /auth/refresh`)
- valida refresh token;
- verifica expiração/revogação;
- revoga token atual;
- gera novo par de tokens.

---

## Gestão de usuários

Endpoints (`/users`) com CRUD completo:
- `GET /users`
- `GET /users/{id}`
- `POST /users`
- `PUT /users/{id}`
- `DELETE /users/{id}`

Regras:
- acesso restrito a role `ADMIN` para operações administrativas;
- vínculo de usuário com tenant (`tenantId`) obrigatório;
- DTOs com validação.

---

## Sistema de agendamento

Endpoints (`/appointments`) com CRUD completo:
- `GET /appointments`
- `GET /appointments/{id}`
- `POST /appointments`
- `PUT /appointments/{id}`
- `DELETE /appointments/{id}`

Regras:
- agendamento vinculado ao tenant;
- vínculo com usuário responsável;
- validação de horário (`end > start`);
- bloqueio de conflito de horário por tenant.

---

## Integração com WhatsApp

`WhatsAppNotificationService` encapsula envio de notificação.
`WhatsAppClient` chama endpoint da Meta Cloud API.

Quando um agendamento é criado:
1. service persiste agendamento;
2. chama OpenAI para montar mensagem inteligente;
3. envia mensagem via WhatsApp.

---

## Integração com OpenAI

`OpenAiService` encapsula chamada `POST /v1/chat/completions`.

Uso atual:
- gerar mensagem amigável de confirmação de agendamento.

Fallback:
- sem `api-key`, retorna mensagem padrão local.

---

## Exemplos de requests JSON

### Register
```json
{
  "name": "Admin Tenant A",
  "email": "admin@tenant-a.com",
  "password": "SenhaForte123",
  "role": "ADMIN"
}
```

### Login
```json
{
  "email": "admin@tenant-a.com",
  "password": "SenhaForte123"
}
```

### Criar usuário
```json
{
  "name": "Operador",
  "email": "operador@tenant-a.com",
  "password": "SenhaForte123",
  "role": "USER",
  "active": true
}
```

### Criar agendamento
```json
{
  "ownerUserId": 1,
  "clientName": "Maria Souza",
  "clientPhone": "5511999999999",
  "serviceDescription": "Manicure e Pedicure",
  "startTime": "2026-05-01T10:00:00",
  "endTime": "2026-05-01T11:00:00"
}
```

---

## Configuração necessária (`application.yaml`)

```yaml
app:
  security:
    jwt:
      secret: "trocar-em-producao"
      access-token-ttl-seconds: 900
      refresh-token-ttl-seconds: 604800
  whatsapp:
    api-url: "https://graph.facebook.com/v19.0"
    access-token: "TOKEN_META"
    phone-number-id: "123456789"
  openai:
    api-key: "OPENAI_API_KEY"
    model: "gpt-4o-mini"
```

---

## Observações de produção

- Recomenda-se mover hash manual para BCrypt com `PasswordEncoder` do Spring Security.
- Recomenda-se trocar parser JWT manual por JJWT.
- Adicionar rate limit e auditoria para `/auth/*`.
- Adicionar fila assíncrona para envio de WhatsApp.
