# Explicação Técnica Completa do Repositório `studiobelle`

## 1) Visão geral do projeto

## Propósito identificado no código
O projeto é uma **API backend SaaS** para gestão de salão/estética, combinando:
- CRM de clientes;
- Agenda de atendimentos;
- Catálogo de serviços e profissionais;
- Dashboard de métricas;
- Autenticação com JWT + refresh token;
- Integração com WhatsApp (envio + webhook);
- Multi-tenant com **isolamento por banco por tenant** (database-per-tenant).

Na prática, ele resolve o problema de operar **várias empresas clientes no mesmo sistema**, sem misturar dados, além de automatizar relacionamento com clientes (reativação e confirmação de agendamentos).

## Tecnologias detectadas automaticamente
- **Java 21**;
- **Spring Boot 4.0.5**;
- Spring Web (REST);
- Spring Data JPA;
- Hibernate;
- Flyway;
- PostgreSQL;
- Lombok;
- Maven Wrapper;
- Docker + Docker Compose;
- Integração HTTP com `RestTemplate` (OpenAI e WhatsApp Cloud API).

---

## 2) Arquitetura do sistema

## Padrão arquitetural
O código segue principalmente um padrão **em camadas (Controller → Service → Repository → Banco)**, com módulos por domínio:
- `appointment`, `customer`, `servicecatalog`, `professional`, `company`, `automation`, `auth`, `user`, etc.

Também existe um subdomínio técnico transversal de **infraestrutura multi-tenant**:
- `tenant` e `tenantmaster`.

## Comunicação entre camadas
1. **Controller** recebe HTTP, valida DTO e delega para Service.
2. **Service** aplica regras de negócio e orquestra entidades/repositórios.
3. **Repository (JPA)** acessa dados por tenant.
4. **TenantContext + TenantRoutingDataSource** define qual banco será usado na requisição.

## Boas práticas encontradas
- DTOs com validação (`jakarta.validation`);
- Uso consistente de `@Transactional` em escrita;
- Tratamento global de exceções (`GlobalExceptionHandler`);
- Isolamento de regras por módulos;
- Uso de `tenantId` explícito nos agregados para reforço de escopo.

## Pontos de atenção arquitetural
- Há **mistura de Flyway + `ddl-auto: update`**, o que pode gerar divergência de schema em produção;
- Alguns serviços usam fallback global de WhatsApp em vez de conta por tenant (inconsistência);
- `TenantRoutingDataSource` usa `HashMap` mutável sem sincronização para atualização dinâmica;
- JWT é implementado manualmente (funciona, mas mais sujeito a erros de segurança e parsing frágil).

---

## 3) Estrutura completa de pastas e responsabilidades

> Abaixo estão as pastas relevantes do repositório e como entram no sistema.

- `/.mvn/wrapper` → infraestrutura do Maven Wrapper.
- `/src/main/java/com/nwltecnologia/studiobelle` → código principal da aplicação.
  - `/appointment` → módulo de agendamentos.
  - `/auth` → autenticação, login, registro e refresh token.
  - `/automation` → automações de comunicação e configurações por tenant.
  - `/common` → exceções, DTOs comuns e suporte reutilizável.
  - `/company` → perfil da empresa (nome, horário, contatos).
  - `/config` → configuração de datasource e filtros.
  - `/customer` → CRM de clientes e histórico de atendimentos.
  - `/dashboard` → métricas agregadas.
  - `/integration/openai` → integração IA para humanização/extração de mensagens.
  - `/integration/whatsapp` → cliente e webhook WhatsApp.
  - `/professional` → profissionais e associação com serviços.
  - `/security` → contexto de autenticação, JWT e segurança.
  - `/servicecatalog` → catálogo de serviços da empresa.
  - `/tenant` → resolução/roteamento dinâmico de tenant por subdomínio/header.
  - `/tenantmaster` → diretório master de tenants e contas WhatsApp.
  - `/user` → gestão de usuários do tenant.
  - `/util` → utilitários (`StringUtils`).
- `/src/main/resources`
  - `application.yaml` → configuração da aplicação.
  - `/db/migration` → scripts Flyway (`V1`..`V4`).
- `/src/test/java` → teste de contexto Spring Boot.
- `/Dockerfile` → imagem da aplicação Java.
- `/docker-compose.yaml` → stack local com Postgres/Redis/PgAdmin.
- `/target` → artefatos gerados de build (não-fonte).
- `README*.md`, `CHANGELOG.md` → documentação auxiliar.

---

## 4) Explicação dos principais arquivos Java

## Inicialização
- `StudiobelleApplication`: ponto de entrada Spring Boot e habilitação de scheduler.

## Configuração e infraestrutura
- `DataSourceConfig`: cria `masterDataSource`, registra filtros (`TenantFilter`, `JwtAuthenticationFilter`) e define o datasource roteável como `@Primary`.
- `TenantContext`: armazena tenant atual em `ThreadLocal`.
- `TenantFilter`: resolve tenant por subdomínio (`X-Forwarded-Host`/host) com fallback `X-Tenant-ID`.
- `TenantRoutingDataSource`: escolhe datasource pelo tenant do contexto.
- `TenantResolver`: consulta `MasterTenantDirectory` para resolver tenant por subdomínio/phone_number_id.
- `TenantLoader`: no startup, carrega tenants do master e registra no roteador.

## Segurança e autenticação
- `JwtAuthenticationFilter`: valida token Bearer para rotas privadas e injeta usuário no `SecurityContext`.
- `JwtService`: gera e valida JWT HS256 manualmente.
- `PasswordService`: hash PBKDF2WithHmacSHA256 + salt por senha.
- `AuthService`: register, login e refresh token com checagens de tenant.
- `UserService`: CRUD de usuários restrito a ADMIN (`SecuritySupport.requireAdmin()`).

## Domínio de negócio
- `AppointmentService`: regra principal de agenda (conflito de horários, validação de duração, horário da empresa/profissional, CRM pós-atendimento e notificação WhatsApp).
- `CustomerService`: CRUD, cálculo de frequência e histórico de atendimento.
- `ServiceOfferingService`: CRUD de serviços por tenant.
- `ProfessionalService`: CRUD de profissionais + relação many-to-many com serviços.
- `CompanyProfileService`: leitura/upsert de perfil da empresa.
- `DashboardService`: consolida indicadores por tenant.
- `AutomationService`: CRUD de templates e job agendado de reativação de clientes.

## Integrações
- `OpenAiService`: humanização de mensagens e extração de dados de agendamento de texto livre.
- `WhatsAppClient`: envio de mensagens via Graph API.
- `WhatsAppNotificationService`: obtém credenciais WhatsApp por tenant no master e envia notificações.
- `WhatsAppWebhookController/Service`: recebe webhook, resolve tenant por `phone_number_id`, extrai dados da mensagem e cria agendamento.

## Tenant master
- `TenantService`: provisiona tenant (cria DB, roda Flyway, salva no master e registra datasource).
- `MasterTenantDirectory`: consultas JDBC no banco master (tenants e whatsapp_accounts).

---

## 5) Relacionamentos entre classes

## Injeção de dependência
Predominância de injeção por construtor (boa prática). Exemplos:
- `AppointmentService` injeta repositórios e serviços de customer/professional/service/whatsapp/openai.
- `AuthService` injeta userRepository + refreshTokenRepository + passwordService + jwtService.

## Herança/composição
- Não há herança de domínio relevante.
- Predomina **composição** (serviços chamando outros serviços/repos).

## Fluxo real principal (Controller → Service → Repository)
- Ex.: `AppointmentController.create` → `AppointmentService.create` → `AppointmentRepository.save` (+ chamadas complementares em `CustomerService`, `WhatsAppNotificationService`).

---

## 6) Fluxos reais do sistema

## Fluxo de criação de agendamento
1. Controller recebe `AppointmentRequest`.
2. Service valida janela temporal (`start < end`).
3. Busca usuário dono no tenant.
4. Se houver serviço: valida ativo, duração e preço.
5. Se houver profissional: valida aptidão, conflito e horário de trabalho.
6. Valida horário da empresa.
7. Verifica conflito global na agenda.
8. Cria/atualiza cliente por telefone.
9. Persiste agendamento.
10. Registra histórico de atendimento no CRM.
11. Gera mensagem (OpenAI opcional) e envia WhatsApp.

## Fluxo de consulta
- Endpoints GET por módulo sempre filtram por tenant atual (`findAllByTenantId...`, `findByIdAndTenantId...`).

## Fluxo de autenticação
1. Tenant é definido no `TenantFilter`.
2. Login busca usuário por e-mail e valida tenant atual + senha + ativo.
3. Gera access token + refresh token persistido.
4. Requisições privadas passam no `JwtAuthenticationFilter`.
5. Filtro valida assinatura, expiração e tenant do token.

## Fluxo do webhook WhatsApp
1. Controller recebe payload Meta.
2. Extrai `phone_number_id`.
3. Resolve tenant no master (`whatsapp_accounts`).
4. Define `TenantContext` para execução.
5. Extrai mensagem e remetente.
6. `WhatsAppWebhookService` usa OpenAI para extrair nome/data/hora/serviço.
7. Cria agendamento automaticamente.

---

## 7) Como rodar o projeto

## Requisitos
- Java 21;
- Maven Wrapper (`./mvnw`);
- PostgreSQL local (ou Docker Compose).

## Subir infraestrutura local
```bash
docker compose up -d
```

## Executar aplicação
```bash
./mvnw spring-boot:run
```

## Observação importante
`application.yaml` está apontando para `jdbc:postgresql://localhost:5432/studiobelle` com usuário/senha `postgres/postgres`.

---

## 8) Como testar

## Testes automatizados existentes
- Existe apenas teste de contexto: `StudiobelleApplicationTests.contextLoads()`.

## Testes manuais recomendados por endpoint
- Autenticação:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant_a" \
  -d '{"name":"Admin","email":"admin@a.com","password":"12345678","role":"ADMIN"}'
```
- Criar serviço/profissional/cliente/agendamento com token Bearer.
- Webhook WhatsApp:
```bash
curl -X POST http://localhost:8080/whatsapp/webhook \
  -H "Content-Type: application/json" \
  -d '{"entry":[{"changes":[{"value":{"metadata":{"phone_number_id":"123"},"messages":[{"from":"5511999999999","text":{"body":"quero unha amanhã às 10"}}]}}]}]}'
```

---

## 9) Configurações importantes

## `application.yaml`
- `spring.datasource.*`: conexão do master DB.
- `spring.jpa.hibernate.ddl-auto: update`.
- `spring.flyway.enabled: true`.
- `app.security.jwt.*`: segredo e TTLs.
- `app.whatsapp.*`: URL API (e campos globais legados).
- `app.openai.*`: chave e modelo.

## Variáveis sensíveis
- Chave JWT (`app.security.jwt.secret`);
- Access token WhatsApp;
- API key OpenAI;
- Credenciais de banco.

Recomendação: externalizar tudo via variáveis de ambiente por perfil (`dev`, `prod`).

---

## 10) Dependências do projeto (extraídas do `pom.xml`)

1. `spring-boot-starter-data-jpa`  
   Usado por repositórios JPA (`JpaRepository`) e entidades do domínio.

2. `spring-boot-starter-web`  
   Usado por controllers REST, filtros HTTP e serialização JSON.

3. `flyway-core`  
   Usado para versionamento de schema (`V1`..`V4`) e criação/evolução de tabelas.

4. `postgresql` (runtime)  
   Driver JDBC para PostgreSQL.

5. `lombok` (optional)  
   Reduz boilerplate de entidades/classes (`@Getter`, `@Setter`, `@Builder`, etc.).

6. `spring-boot-starter-test` (test)  
   Base de testes de integração/unidade (`@SpringBootTest`, JUnit 5).

> Nenhuma dependência foi alterada nesta análise.

---

## 11) Problemas encontrados

## Arquitetura e consistência
1. **Inconsistência entre modelagem e migration inicial**: V1 cria `tenantId` e depois V4 renomeia `tenantid`; em PostgreSQL não-quoted, o nome vira minúsculo e pode causar falha em alguns cenários de migração.
2. **`ddl-auto: update` junto com Flyway**: em produção, pode gerar mudanças não controladas fora dos scripts versionados.
3. **Automação usa `app.whatsapp.phone-number-id` global** em vez de conta por tenant, diferente da abordagem de `whatsapp_accounts` usada em notificações.
4. **`TenantService.criarBanco` usa SQL concatenado** com nome de banco vindo de input sanitizado (risco reduzido, mas ainda melhor usar whitelist rígida + validações de tamanho).
5. **`DataSourceFactory` hardcoded em localhost:5432/postgres** limita deploy em ambientes diversos.
6. **JWT parsing manual por `String#indexOf`** é frágil para payloads não triviais e dificulta evolução segura.
7. **`UserRepository.findByEmail` é global** e pode bloquear e-mails iguais entre tenants (talvez desejado, mas normalmente SaaS permite por tenant).
8. **`AppointmentService.create` ignora diferença entre `customerId` nulo e preenchido**, pois chama sempre `createOrUpdateByPhone`.

## Qualidade/testes
9. Cobertura de testes muito baixa (somente teste de contexto).
10. Falta padronização de logging em fluxos críticos de integração (webhook/automação).

---

## 12) Correções recomendadas (sem quebrar e sem alterar dependências)

1. **Desativar `ddl-auto` em produção** (`validate` ou `none` por profile).  
   Motivo: manter rastreabilidade e previsibilidade de schema com Flyway.

2. **Unificar estratégia WhatsApp por tenant no `AutomationService`** usando `MasterTenantDirectory.findWhatsAppByTenantId(...)`.  
   Motivo: evitar envio com número global incorreto e manter isolamento por empresa.

3. **Refatorar `DataSourceFactory` para ler host/porta/user padrão via config** (sem nova dependência).  
   Motivo: reduzir acoplamento com localhost e facilitar ambientes cloud.

4. **Fortalecer validações de `TenantRequest`** (camada DTO com Bean Validation já disponível).  
   Motivo: reduzir entradas inválidas no provisionamento.

5. **Corrigir fluxo de `customerId` no agendamento**:
   - se `customerId` vier preenchido, buscar cliente por id+tenant e validar telefone/nome;
   - se vier nulo, usar `createOrUpdateByPhone`.
   Motivo: preservar intenção do contrato da API.

6. **Evoluir parser de horário (`validateSimpleHourRange`)** para múltiplas janelas e dias da semana.  
   Motivo: regra atual é simplificada e pode aceitar horários indevidos.

7. **Adicionar testes de integração por módulo** (auth, tenant, appointment, webhook).  
   Motivo: reduzir regressões em regras críticas de negócio.

---

## 13) Sugestões de melhoria (refatoração e boas práticas)

- Introduzir **camada de casos de uso** para regras complexas de agendamento (Clean Architecture leve).
- Padronizar resposta de erro com código interno + trace id.
- Criar política de idempotência para webhook WhatsApp (evitar agendamento duplicado).
- Criar auditoria de automações enviadas (quem, quando, tenant, status).
- Melhorar limites de transação e reduzir carregamento de listas inteiras para métricas (usar queries agregadas no banco).
- Revisar políticas de unicidade por tenant (ex.: e-mail de usuário).
- Documentar contratos de endpoint em OpenAPI (pode ser feito futuramente sem quebrar domínio).

---

## 14) Conclusão

O repositório está funcional e já apresenta uma base robusta para SaaS multi-tenant com isolamento por banco e integração WhatsApp. O desenho por camadas está bem encaminhado e os domínios principais estão separados. Os pontos mais críticos para evolução segura estão em **consistência de configuração de schema**, **padronização total de multi-tenant nas integrações** e **ampliação de testes automatizados**.

