# Guia de Estudo Profundo do Projeto `studiobelle`

> Este documento foi construído por engenharia reversa do código-fonte do repositório inteiro. A ideia aqui é te permitir entender o sistema **na prática**, classe por classe, fluxo por fluxo, com foco em arquitetura, execução real e manutenção.

---

## 1) Visão Geral do Sistema (baseado no código real)

### 1.1 O que o sistema faz
O sistema é uma API backend SaaS multi-tenant para operação de negócios de beleza/estética (salão, estúdio, clínica), com os seguintes pilares:

1. **Cadastro e autenticação de usuários** (`auth`, `user`, `security`);
2. **Agenda de atendimentos** (`appointment`);
3. **CRM de clientes e histórico de atendimento** (`customer`);
4. **Catálogo de serviços e profissionais** (`servicecatalog`, `professional`);
5. **Perfil da empresa por tenant** (`company`);
6. **Automações de relacionamento** (reativação de clientes inativos) (`automation`);
7. **Integração com WhatsApp** para envio e recebimento (`integration/whatsapp`);
8. **Integração com OpenAI** para humanização de mensagem e extração de dados de texto (`integration/openai`);
9. **Roteamento por tenant (database por tenant)** via subdomínio/header e diretório master (`tenant`, `tenantmaster`).

### 1.2 Qual problema ele resolve
O problema central resolvido é: **operar várias empresas (tenants) no mesmo backend, sem mistura de dados**.

Na prática:
- Cada empresa tem seu banco próprio;
- O sistema resolve qual tenant atender em cada requisição;
- A operação (usuários, clientes, agenda etc.) fica isolada por tenant;
- O WhatsApp também pode ser isolado por conta/tenant.

### 1.3 Tecnologias detectadas automaticamente
- Java 21;
- Spring Boot 4.0.5;
- Spring Web;
- Spring Data JPA;
- Hibernate;
- Flyway;
- PostgreSQL;
- Lombok;
- Maven Wrapper;
- Docker / Docker Compose;
- RestTemplate para integrações HTTP externas.

---

## 2) Arquitetura Real (não teórica)

### 2.1 Padrão arquitetural aplicado no código
O projeto utiliza um estilo **modular por domínio + arquitetura em camadas**:

- Camada de entrada HTTP: `controller`;
- Camada de regras: `service`;
- Camada de persistência: `repository`;
- Camada de modelo: `entity`;
- Camada de contrato externo/interno: `dto`.

Isso aparece de forma repetida em `appointment`, `customer`, `servicecatalog`, `professional`, `company`, `automation`, `auth`, `user`.

### 2.2 Como as camadas funcionam na prática
Fluxo típico:
1. Controller recebe JSON e valida com Bean Validation (`@Valid` + constraints em DTO);
2. Service faz regra de negócio e coordena chamadas;
3. Repository executa consulta/persistência JPA;
4. Entity representa tabela;
5. Response DTO encapsula retorno para API.

### 2.3 Arquitetura multi-tenant aplicada
Há dois contextos de dados:

1. **Master DB** (diretório global)
   - Tabela de tenants;
   - Tabela `whatsapp_accounts`;
   - Consultado por `MasterTenantDirectory` com `JdbcTemplate` + `masterDataSource`.

2. **Tenant DB (um por cliente)**
   - Tabelas de negócio (users, appointments, customers etc.);
   - Selecionado dinamicamente por `TenantRoutingDataSource` usando `TenantContext`.

Resolução de tenant:
- `TenantFilter` tenta subdomínio (host/X-Forwarded-Host) -> `TenantResolver` -> `MasterTenantDirectory`;
- fallback legado: header `X-Tenant-ID`;
- webhook WhatsApp resolve tenant por `phone_number_id`.

### 2.4 Onde está correto
- Bom isolamento de domínio por pacote;
- Boa separação controller/service/repository;
- Uso consistente de transação em operações de escrita;
- Existência de tratamento global de exceções;
- Segurança com validação de tenant no token.

### 2.5 Onde está incorreto/frágil
- `spring.jpa.hibernate.ddl-auto=update` coexistindo com Flyway (risco de drift de schema);
- JWT implementado manualmente por parsing de string (frágil);
- parte da automação ainda usa configuração global de WhatsApp;
- testes automatizados insuficientes;
- alguns pontos de modelagem/fluxo têm inconsistência (detalhados em seção de problemas).

---

## 3) Estrutura de Pastas Completa e Detalhada

## 3.1 Raiz do repositório
- `README.md`, `README-ARQUITETURA.md`, `README-DEPENDENCIAS.md`, `README-TESTES.md`, `CHANGELOG.md`: documentação auxiliar.
- `pom.xml`: build/dependências Maven.
- `Dockerfile`: empacotamento da aplicação em container.
- `docker-compose.yaml`: infraestrutura local (Postgres/Redis/PgAdmin).
- `mvnw`, `mvnw.cmd`, `.mvn/wrapper`: Maven Wrapper.
- `explicacao.md`: este guia.

## 3.2 `src/main/java/com/nwltecnologia/studiobelle`
Pacote raiz da aplicação.

### `appointment`
- Tipos: controller, dto, entity, repository, service.
- Objetivo: ciclo completo de agendamento.
- Conexões: user, customer, professional, servicecatalog, company, whatsapp, openai.

### `auth`
- Tipos: controller, dto, entity, repository, service.
- Objetivo: registro, login e refresh token.
- Conexões: user, security, tenant context.

### `automation`
- Tipos: controller, dto, entity, repository, service.
- Objetivo: configuração de automações e execução agendada de reativação.
- Conexões: customer, openai, whatsapp.

### `common`
- Tipos: dto simples, exceções, suporte de tenant.
- Objetivo: reutilização transversal.

### `company`
- Tipos: perfil da empresa por tenant.
- Conexões: appointment usa horário de funcionamento para validação.

### `config`
- Tipos: configuração Spring (`DataSourceConfig`).
- Objetivo: filtros HTTP e datasource master/roteado.

### `customer`
- Tipos: controller, dto, entities, repositories, service.
- Objetivo: CRM e histórico de consumo.

### `dashboard`
- Tipos: controller, dto, service.
- Objetivo: métricas resumidas por tenant.

### `integration/openai`
- Tipos: service de integração HTTP.
- Objetivo: humanização de texto e extração de dados de mensagens.

### `integration/whatsapp`
- Tipos: client, controller, dto, service.
- Objetivo: webhook e envio de mensagens.

### `professional`
- Tipos: módulo de profissionais e serviços que executam.

### `security`
- Tipos: filtro JWT, serviços de senha/token/contexto.

### `servicecatalog`
- Tipos: catálogo de serviços (nome, duração, preço, ativo).

### `tenant`
- Tipos: contexto, filtro, resolver e datasource roteável.

### `tenantmaster`
- Tipos: CRUD/provisionamento de tenant + consultas master com JDBC.

### `user`
- Tipos: entidade de usuário e CRUD administrativo.

### `util`
- Tipos: utilitário de saneamento de string.

## 3.3 `src/main/resources`
- `application.yaml`: propriedades principais;
- `db/migration/V1..V4`: evolução de schema com Flyway.

## 3.4 `src/test/java`
- `StudiobelleApplicationTests`: teste mínimo de contexto.

## 3.5 `target`
- Artefatos de build (classes compiladas e recursos copiados).

---

## 4) Dependências (`pom.xml`) explicadas no contexto real

1. `spring-boot-starter-data-jpa`
   - Necessária para `JpaRepository`, entidades JPA, transações e integração com Hibernate.
2. `spring-boot-starter-web`
   - Necessária para REST Controllers, filtros HTTP, serialização JSON.
3. `flyway-core`
   - Necessária para versionamento dos scripts `V1__...` a `V4__...`.
4. `postgresql` (runtime)
   - Driver JDBC utilizado por master e bancos de tenant.
5. `lombok`
   - Reduz código boilerplate em entidades/DTOs clássicos com `@Getter`, `@Setter`, `@Builder`, etc.
6. `spring-boot-starter-test` (test)
   - Base para JUnit/Spring Test no módulo de testes.

> Nenhuma dependência foi alterada e `pom.xml` não foi modificado.

---

## 5) Configurações importantes (`application.yaml`) linha/bloco por bloco

Arquivo define:

### `spring.application.name: studiobelle`
Nome lógico da aplicação.

### `spring.datasource`
Configuração do datasource principal (master):
- `url: jdbc:postgresql://localhost:5432/studiobelle`
- `username/password: postgres/postgres`
- Hikari pool (`maximum-pool-size`, `minimum-idle`).

### `spring.jpa`
- `ddl-auto: update` -> Hibernate tenta ajustar schema automaticamente;
- `show-sql: true` e `format_sql: true` -> logs SQL;
- `dialect`: PostgreSQL.

### `spring.flyway.enabled: true`
Flyway habilitado.

### `server.port: 8080`
Porta HTTP.

### `app.security.jwt`
- `secret`;
- TTL access token (900s);
- TTL refresh token (604800s = 7 dias).

### `app.whatsapp`
- `api-url` da Graph API;
- `access-token` e `phone-number-id` globais (há uso legado parcial).

### `app.openai`
- `api-key`;
- `model` (`gpt-4o-mini`).

### `logging.level`
Logs detalhados de SQL e bind types.

---

## 6) Migrações Flyway (`db/migration`) em detalhe

## V1__create_tenants.sql
Cria tabela `tenants` (master) com colunas iniciais de identificação e credenciais de DB.

## V2__create_user_auth_and_appointments.sql
Cria:
- `users`;
- `refresh_tokens`;
- `appointments` (modelo inicial);
- índices de apoio.

## V3__crm_company_automation.sql
Amplia domínio com:
- `company_profiles`, `service_offerings`, `professionals`, `professional_services`;
- `customers`, `customer_histories`;
- `tenant_settings`, `automations`;
- adiciona relacionamentos em `appointments` com customer/service/professional e preço.

## V4__master_tenant_and_whatsapp.sql
Evolui master:
- renomeia colunas em `tenants`;
- adiciona `subdomain`, campos WhatsApp, `created_at`;
- cria índices únicos;
- cria `whatsapp_accounts`.

---

## 7) Análise Profunda das Classes Java

> Organização: primeiro infraestrutura transversal, depois módulos de negócio.

## 7.1 Bootstrap

### Classe: `StudiobelleApplication`
- Caminho: `src/main/java/com/nwltecnologia/studiobelle/StudiobelleApplication.java`
- Responsabilidade: inicialização Spring Boot.

#### Análise do código
- `@SpringBootApplication`: agrega auto-configuração + component scan + config.
- `@EnableScheduling`: habilita execução de métodos anotados com `@Scheduled` (usado em automações).
- `main(...)`: ponto de entrada Java; chama `SpringApplication.run(...)`.

---

## 7.2 Configuração de datasource e filtros

### Classe: `DataSourceConfig`
- Caminho: `config/DataSourceConfig.java`
- Responsabilidade: montar estratégia de dados multi-tenant e ordem dos filtros.

#### Blocos importantes
1. `masterDataSourceProperties()`
   - `@ConfigurationProperties("spring.datasource")` lê propriedades do YAML.
2. `masterDataSource(...)`
   - cria datasource Hikari real do master DB.
3. `tenantFilter(...)`
   - registra `TenantFilter` com ordem 1 (executa antes de autenticação).
4. `jwtFilter(...)`
   - registra `JwtAuthenticationFilter` com ordem 2.
5. `dataSource(...)`
   - define `TenantRoutingDataSource` como primário;
   - fallback/default aponta para masterDataSource.

**Por quê isso importa?**
Sem ordem correta, JWT poderia validar token antes de tenant estar definido.

---

## 7.3 Multi-tenant core

### Classe: `TenantContext`
- ThreadLocal de tenant atual.
- Métodos:
  - `setTenant(String)`;
  - `getTenant()`;
  - `clear()`.

**Por quê?**
Permite que qualquer camada acesse o tenant da requisição corrente sem passar parâmetro manualmente em todas as assinaturas.

### Classe: `TenantRoutingDataSource`
- Extende `AbstractRoutingDataSource`.
- `determineCurrentLookupKey()` retorna `TenantContext.getTenant()`.
- `addTenant(...)` adiciona datasource em mapa interno e atualiza targets.
- `@PostConstruct init()` garante mapa inicial não nulo.

**Conexão com sistema:** quando JPA abre conexão, roteador escolhe datasource pelo tenant do ThreadLocal.

### Classe: `DataSourceFactory`
- Cria DataSource para tenant a partir de:
  - entidade `Tenant` ou
  - record `TenantDatabaseConfig`.
- Monta URL `jdbc:postgresql://localhost:5432/{databaseName}`.

**Ponto de atenção:** host/porta fixos.

### Classe: `TenantResolver`
- Encapsula resolução de tenant:
  - por subdomínio (`findBySubdomain` no master);
  - por `phone_number_id` (`findWhatsAppByPhoneNumberId`).

### Classe: `TenantFilter`
- Executa em toda requisição (`/*`), exceto bypass explícito de `/whatsapp/webhook`.
- Lógica do `doFilterInternal`:
  1. se webhook, só continua (tenant será definido no controller do webhook);
  2. resolve tenant por host/subdomínio;
  3. fallback para header `X-Tenant-ID`;
  4. seta `TenantContext`;
  5. limpa contexto no `finally`.

- `extractSubdomain(...)`:
  - remove porta;
  - suporta domínio com 3+ partes e cenário `cliente.localhost`.

### Classe: `TenantIdentifierResolver`
- Implementa `CurrentTenantIdentifierResolver` do Hibernate.
- retorna tenant atual ou `"default"`.

### Classe: `TenantLoader`
- No startup (`@PostConstruct`), consulta todos tenants no master e chama `routingDataSource.addTenant(...)`.

---

## 7.4 Tenant master

### Classe: `Tenant` (entity)
- Mapeia tabela `tenants` do master.
- Campos: id, nome, tenantId, subdomain, databaseName, username, password, phoneNumberId, businessAccountId, createdAt.
- Note mistura `nome` (campo Java legado) com `@Column(name = "name")`.

### Classe: `TenantRepository`
- `JpaRepository<Tenant, Long>` sem métodos custom.

### Classe: `TenantRequest`
- DTO mutável (`@Getter/@Setter`) para criação de tenant.
- Campos: nome, tenantId, subdomain, phoneNumberId, businessAccountId, accessToken.

### Classe: `TenantController`
- Endpoints:
  - `GET /tenants` -> lista tenants;
  - `GET /tenants/{id}` -> busca por id;
  - `POST /tenants` -> criação (retorna mensagem simples).

### Classe: `MasterTenantDirectory`
- Usa `JdbcTemplate` com `@Qualifier("masterDataSource")`.
- Métodos:
  - `findAllTenantDatabases()`;
  - `findBySubdomain(...)`;
  - `findByTenantId(...)`;
  - `findWhatsAppByPhoneNumberId(...)`;
  - `findWhatsAppByTenantId(...)`.

**Por quê JdbcTemplate aqui?**
Para diretório master, consultas simples e independentes de contexto tenant JPA.

### Classe: `TenantDatabaseConfig` (record)
- DTO imutável com dados mínimos do tenant para montar datasource.

### Classe: `WhatsAppAccountConfig` (record)
- DTO imutável com credenciais WhatsApp por tenant.

### Classe: `TenantService`
Responsável por provisionamento completo do tenant.

#### Método `criarTenant(TenantRequest request)` passo a passo
1. Sanitiza `tenantId` e nome de DB (`db_{nome}`);
2. Cria banco (`CREATE DATABASE ...`) com `masterDataSource`;
3. Executa Flyway nesse banco novo;
4. Monta entidade `Tenant` e persiste no master;
5. Se veio `phoneNumberId`, grava/atualiza `whatsapp_accounts`;
6. Cria DataSource do novo tenant e adiciona ao roteador.

#### Outros métodos
- `findAll()`, `findById(...)`: CRUD básico;
- `saveWhatsAppAccount(...)`: UPSERT em `whatsapp_accounts`;
- `criarBanco(...)`: DDL de criação;
- `rodarFlyway(...)`: execução de migração;
- `sanitize(...)`: normaliza string.

---

## 7.5 Segurança e autenticação

### Classe: `ApiSecurityException`
Exceção de domínio de segurança/autorização/autenticação.

### Classe: `AuthenticatedUser` (record)
Representa usuário autenticado no contexto da requisição:
- id, email, tenantId, role.

### Classe: `SecurityContext`
ThreadLocal para armazenar `AuthenticatedUser` do request atual.

### Classe: `SecuritySupport`
- `currentUser()` -> obtém usuário autenticado ou lança erro;
- `requireAdmin()` -> garante role ADMIN.

### Classe: `PasswordService`
- Hash PBKDF2 + salt aleatório por senha.
- `hashPassword(...)`: gera `salt:hash` em Base64.
- `matches(...)`: recalcula hash e compara em tempo constante aproximado.

**Por quê?**
Evita armazenar senha em texto puro e suporta verificação segura.

### Classe: `JwtService`
Implementa JWT HS256 manualmente.

#### `generateAccessToken(...)`
1. calcula `now` e `exp`;
2. monta header JSON fixo;
3. monta payload com `sub`, `email`, `tenantId`, `role`, `iat`, `exp`;
4. base64url encode;
5. assina com HmacSHA256;
6. retorna token + data expiração.

#### `parseAndValidate(...)`
1. separa token em 3 partes;
2. recomputa assinatura e compara;
3. decodifica payload;
4. extrai campos por parsing textual (`extractString`/`extractLong`);
5. valida expiração;
6. retorna `TokenPayload`.

### Classe: `JwtAuthenticationFilter`
Filtro de autenticação para rotas privadas.

#### Lógica de `doFilterInternal`
1. Se rota pública (`/auth/*`, `/tenants`, `/whatsapp/webhook`), passa direto.
2. Lê header Authorization Bearer.
3. Valida JWT via `JwtService`.
4. Garante tenant atual no `TenantContext`.
5. Garante que token pertence ao tenant atual.
6. Seta `SecurityContext` com usuário autenticado.
7. Continua chain.
8. Em erro, retorna 401 JSON.
9. Sempre limpa `SecurityContext`.

---

## 7.6 Tratamento comum de exceções

### Classe: `BusinessException`
Exceção para violação de regra de negócio (não segurança).

### Classe: `GlobalExceptionHandler`
`@RestControllerAdvice` centralizando resposta de erro.

- `handleBusiness(ApiSecurityException)` -> 401;
- `handleBusinessValidation(BusinessException)` -> 400;
- `handleValidation(MethodArgumentNotValidException)` -> 400 com mapa de campos;
- `handleGeneric(Exception)` -> 500 e log de stack trace.

### Classe: `ApiMessageResponse`
Record simples para mensagens de sucesso textual.

### Classe: `TenantSupport`
Componente utilitário para recuperar tenant atual e lançar erro padronizado se ausente.

---

## 7.7 Módulo Auth

### DTOs
- `AuthRequest`: email/senha com validação;
- `RegisterRequest`: nome/email/senha/role com constraints;
- `TokenRefreshRequest`: refresh token obrigatório;
- `AuthResponse`: access + refresh + tipo + expirações.

### Entidade: `RefreshToken`
- Tabela `refresh_tokens`;
- `@ManyToOne` com `User`;
- campos de token, expiração, revogação, createdAt;
- `@PrePersist` preenche data e default de `revoked`.

### Repository: `RefreshTokenRepository`
- `findByToken`;
- `deleteByUser`;
- `deleteByExpiresAtBefore`.

### Classe: `AuthService`
Regras centrais de autenticação.

#### `register(...)`
1. obtém tenant atual;
2. bloqueia e-mail já existente;
3. cria `User` com senha hasheada;
4. salva e chama `generateTokens`.

#### `login(...)`
1. busca usuário por e-mail;
2. valida tenant, status ativo e senha;
3. remove refresh tokens antigos do usuário;
4. gera novo par de tokens.

#### `refresh(...)`
1. busca refresh token;
2. valida revogação/expiração;
3. valida tenant do usuário dono;
4. marca token antigo como revogado;
5. emite novos tokens.

#### `generateTokens(User)`
- emite JWT access;
- gera refresh aleatório por UUID duplo;
- salva refresh token;
- monta `AuthResponse`.

#### `currentTenant()`
- lê `TenantContext`;
- lança `ApiSecurityException` se ausente.

### Classe: `AuthController`
- `POST /auth/register`;
- `POST /auth/login`;
- `POST /auth/refresh`.

Todos usam `@Valid` no body.

---

## 7.8 Módulo User

### Entidade: `User`
- tabela `users`;
- campos: id, name, email (unique), passwordHash, role(enum), tenantId, active, createdAt, updatedAt;
- `@PrePersist` inicializa datas e default active=true;
- `@PreUpdate` atualiza timestamp.

### Enum: `UserRole`
- `ADMIN`, `USER`.

### DTOs
- `UserRequest`: dados de criação/edição;
- `UserResponse`: retorno completo sem senha.

### Repository: `UserRepository`
- `findByEmail`;
- `findAllByTenantId`;
- `findByIdAndTenantId`.

### Classe: `UserService`
- Todos os métodos começam com autorização ADMIN (`securitySupport.requireAdmin()`).
- `findAll`, `findById`: leitura por tenant.
- `create`: valida e-mail único, hash de senha, salva.
- `update`: atualiza campos incluindo senha (sempre re-hash).
- `delete`: remove usuário do tenant.
- `currentTenant()` local lê `TenantContext`.

### Classe: `UserController`
CRUD HTTP em `/users` com retorno padronizado.

---

## 7.9 Módulo Company

### Entidade: `CompanyProfile`
- tabela `company_profiles` com `tenant_id` único;
- campos de identidade/contato/horário;
- timestamps com prePersist/preUpdate.

### Repository: `CompanyProfileRepository`
- `findByTenantId`.

### DTOs
- `CompanyProfileRequest` (validações de tamanho e obrigatoriedade);
- `CompanyProfileResponse`.

### Classe: `CompanyProfileService`
- `getCurrent()`:
  - busca perfil do tenant;
  - se não existe, cria padrão (`Minha Empresa`, horário default);
  - retorna response.
- `upsert(...)`:
  - busca por tenant ou cria novo objeto;
  - copia campos do request;
  - salva.

### Classe: `CompanyProfileController`
- `GET /company-profile`;
- `PUT /company-profile`.

---

## 7.10 Módulo Service Catalog

### Entidade: `ServiceOffering`
- tabela `service_offerings`;
- campos: tenantId, name, durationMinutes, price, active, timestamps;
- default de active no `@PrePersist`.

### Repository: `ServiceOfferingRepository`
- `findAllByTenantIdOrderByNameAsc`;
- `findByIdAndTenantId`.

### DTOs
- `ServiceOfferingRequest`: nome, duração (5..480), preço >=0, ativo;
- `ServiceOfferingResponse`.

### Classe: `ServiceOfferingService`
- `findAll`: lista por tenant.
- `create`: monta entity e salva.
- `update`: busca por id+tenant e atualiza campos.
- `delete`: remove por id+tenant.
- `mustFindByTenant`: helper usado por outros módulos (appointment/professional).

### Classe: `ServiceOfferingController`
Endpoints CRUD em `/services`.

---

## 7.11 Módulo Professional

### Entidade: `Professional`
- tabela `professionals`;
- `@ManyToMany` com `ServiceOffering` via tabela de junção `professional_services`;
- campos: tenantId, name, workingHours, services, timestamps.

### Repository: `ProfessionalRepository`
- listagem por tenant;
- busca por id+tenant.

### DTOs
- `ProfessionalRequest`: nome, workingHours, set de serviceIds obrigatório;
- `ProfessionalResponse`.

### Classe: `ProfessionalService`
- `findAll`: lista e converte;
- `create`:
  1. pega tenant;
  2. resolve services por IDs (valida existência no tenant);
  3. cria entity;
  4. salva.
- `update`: busca entity tenant-aware, atualiza nome/horário/serviços;
- `delete`: remove por id+tenant;
- `mustFindByTenant`: helper para AppointmentService;
- `resolveServices(...)`: loop de IDs chamando `serviceOfferingService.mustFindByTenant`;
- `toResponse(...)`: converte entity em DTO.

### Classe: `ProfessionalController`
CRUD em `/professionals`.

---

## 7.12 Módulo Customer

### Entidade: `Customer`
- tabela `customers`;
- campos: tenantId, nome, phone, email, lastVisit, frequency, totalSpent, notes, timestamps;
- prePersist define `totalSpent = 0`.

### Entidade: `CustomerHistory`
- tabela `customer_histories`;
- `@ManyToOne` para `Customer`;
- registra serviço, valor, data atendimento, notas, createdAt.

### Repositories
- `CustomerRepository`:
  - findAllByTenantIdOrderByNameAsc;
  - findByIdAndTenantId;
  - findByPhoneAndTenantId;
  - findAllByTenantIdAndLastVisitBefore.
- `CustomerHistoryRepository`:
  - histórico ordenado por data desc.

### DTOs
- `CustomerRequest`;
- `CustomerResponse`;
- `CustomerHistoryResponse`.

### Classe: `CustomerService`

#### `findAll`, `findById`, `create`, `update`, `delete`
CRUD tenant-aware padrão.

#### `findInactive(int inactivityDays)`
Calcula threshold (now - dias) e retorna clientes sem visita recente.

#### `findHistory(Long customerId)`
1. valida cliente no tenant;
2. busca histórico por tenant + customerId;
3. converte para DTO.

#### `createOrUpdateByPhone(tenantId, phone, name)`
- Se existe por telefone+tenant: atualiza nome se veio e salva;
- Se não existe: cria cliente mínimo com `frequency="NOVO"`.

#### `registerAttendance(...)`
1. atualiza cliente (`lastVisit`, `totalSpent`, `frequency`);
2. salva cliente;
3. cria e salva `CustomerHistory`.

#### `calculateFrequency(Customer)`
- NOVO se sem visita;
- ALTA se <=30 dias;
- MÉDIA se <=60;
- BAIXA acima disso.

### Classe: `CustomerController`
- `GET /customers`;
- `GET /customers/{id}`;
- `GET /customers/{id}/history`;
- `GET /customers/inactive?days=...`;
- `POST`, `PUT`, `DELETE`.

---

## 7.13 Módulo Appointment (núcleo de negócio)

### Entidade: `Appointment`
- tabela `appointments`;
- `@ManyToOne` obrigatória para `ownerUser`;
- `@ManyToOne` opcionais para customer/service/professional;
- campos de cliente (nome/telefone/descrição) redundantes para histórico de contexto;
- campos de horário e preço;
- timestamps com prePersist/preUpdate.

### Repository: `AppointmentRepository`
- listagem por tenant e ordenação;
- busca por id+tenant;
- checagens de conflito temporal global e por profissional.

### DTOs
- `AppointmentRequest`:
  - ownerUserId obrigatório;
  - clientName/clientPhone/serviceDescription obrigatórios;
  - start/end com `@Future`;
  - customerId/serviceId/professionalId opcionais.
- `AppointmentResponse`: retorno completo com ids correlacionados.

### Classe: `AppointmentService`
Classe mais densa de regra de negócio.

#### `findAll`, `findById`
Leitura por tenant com validação de existência.

#### `create(AppointmentRequest)` — simulação detalhada
1. `validateTimes(start,end)` garante fim > início.
2. Obtém tenant de `TenantContext`.
3. Carrega `ownerUser` por id+tenant.
4. Inicializa `service`, `professional`, `price`.
5. Se `serviceId` veio:
   - busca serviço no tenant;
   - exige ativo;
   - calcula end esperado por duração;
   - se não bater com request, lança BusinessException;
   - define price do serviço.
6. Se `professionalId` veio:
   - busca profissional no tenant;
   - se serviço definido, valida que profissional executa esse serviço;
   - chama `ensureProfessionalNoConflict`;
   - valida horário do profissional (`validateProfessionalHours`).
7. Valida horário da empresa (`validateCompanyBusinessHours`).
8. Valida conflito global (`ensureNoConflict`).
9. Resolve cliente (atualmente sempre chama `createOrUpdateByPhone`, independentemente de `customerId`).
10. Monta entidade `Appointment` com dados completos.
11. Salva.
12. Registra atendimento no CRM via `customerService.registerAttendance(...)`.
13. Gera mensagem amigável via OpenAI service.
14. Dispara notificação WhatsApp.
15. Retorna DTO.

#### `update(Long id, AppointmentRequest)`
1. valida horários;
2. busca appointment por id+tenant;
3. valida ownerUser;
4. varre lista de appointments do tenant para conflito (exceto próprio id);
5. atualiza campos principais;
6. salva e retorna.

#### `delete(Long id)`
Remove appointment do tenant.

#### Helpers
- `ensureNoConflict`, `ensureProfessionalNoConflict` usam exists query;
- `validateSimpleHourRange` parseia formato simples `HH:mm-HH:mm` (ou pega trecho após último espaço);
- `toResponse` converte entity em DTO.

### Classe: `AppointmentController`
CRUD HTTP em `/appointments`.

---

## 7.14 Módulo Dashboard

### DTO: `DashboardResponse`
Campos agregados de visão executiva.

### Classe: `DashboardService`
- Busca tenant atual;
- Calcula total/inativos/ativos via repositório de customer;
- Calcula appointments de hoje filtrando stream de todos do tenant;
- define `freeSlotsToday = 16 - appointmentsToday`;
- soma receita estimada de hoje.

### Classe: `DashboardController`
`GET /dashboard/metrics` retorna visão consolidada.

---

## 7.15 Módulo Automation

### Entidades
- `Automation`: tipo, ativo, template por tenant;
- `AutomationType`: `APPOINTMENT_REMINDER`, `REACTIVATION`, `POST_ATTENDANCE`;
- `TenantSettings`: config por tenant (inactivityDays, aiEnabled, automationEnabled).

### Repositories
- `AutomationRepository`: por tenant e tipo;
- `TenantSettingsRepository`: por tenant.

### DTOs
- `AutomationRequest`, `AutomationResponse`, `TenantSettingsRequest`.

### Classe: `TenantSettingsService`
- `getOrCreateCurrent()` cria defaults se não existir;
- `upsert(...)` atualiza settings.

### Classe: `AutomationService`

#### Responsabilidade
- Gerenciar automações por tenant;
- processar job periódico de reativação.

#### `findAllByCurrentTenant(tenantId)`
Lista automações do tenant.

#### `upsert(tenantId, request)`
Cria/atualiza automação por `(tenant, type)`.

#### `processReactivationAutomations()` (`@Scheduled(cron = "0 */30 * * * *")`)
Executa a cada 30 minutos:
1. Se `phoneNumberId` global vazio, retorna;
2. busca todos `TenantSettings`;
3. para cada tenant:
   - pula se automation disabled;
   - busca automação `REACTIVATION` ativa;
   - calcula threshold por `inactivityDays`;
   - busca clientes inativos;
   - para cada cliente com telefone:
   - monta mensagem com template (`{nome}`, `{dias}`);
   - opcionalmente humaniza com OpenAI se `aiEnabled`;
   - envia WhatsApp.

### Classe: `AutomationController`
- `GET /automations`;
- `POST /automations`;
- `GET /automations/settings`;
- `PUT /automations/settings`.

---

## 7.16 Integração OpenAI

### Classe: `OpenAiService`

#### Atributos
- `RestTemplate` interno;
- `apiKey` e `model` vindos de config.

#### Métodos
- `generateWhatsAppMessage(customerName, serviceName, startTimeText)`
  - monta frase base e passa em `humanizeMessage`.
- `humanizeMessage(baseMessage)`
  - sem apiKey: retorna base;
  - com apiKey: chama `ask(system,user)`.
- `extractAppointmentData(rawMessage)`
  - sem apiKey: fallback heurístico;
  - com apiKey: pede JSON com nome/data/hora/serviço e parseia de forma tolerante.
- `ask(...)`
  - monta request HTTP para `https://api.openai.com/v1/chat/completions`.
- `extractFallback(...)`
  - preenche defaults (cliente genérico, amanhã 10:00 etc.).
- `parseLooseJson(...)`
  - parser simples que tenta extrair pares `chave:valor`.
- `extractContent(...)`
  - percorre estrutura do retorno para pegar `choices[0].message.content`.

**Risco técnico:** parser e contrato do endpoint são tolerantes mas frágeis.

---

## 7.17 Integração WhatsApp

### Classe: `WhatsAppClient`
- envia mensagem texto via Graph API:
  - endpoint `{apiUrl}/{phoneNumberId}/messages`;
  - auth bearer com access token;
  - payload `messaging_product=whatsapp`.

### Classe: `WhatsAppNotificationService`
- usado após criação de appointment;
- pega tenant atual;
- consulta conta WhatsApp do tenant no master;
- dispara `WhatsAppClient.sendTextMessage(...)`.

### DTO: `WhatsAppWebhookRequest`
- `fromPhone`, `customerName`, `message`.

### Classe: `WhatsAppWebhookController`
Fluxo:
1. Recebe payload bruto (`JsonNode`) em `POST /whatsapp/webhook`.
2. Extrai `phone_number_id` via `findValue`.
3. Resolve tenant por `TenantResolver`.
4. Extrai primeira mensagem (`from`, `text.body`) e nome de perfil.
5. Se faltar mensagem, lança `BusinessException`.
6. Seta `TenantContext` manualmente no escopo do processamento.
7. Chama `WhatsAppWebhookService.process(...)`.
8. Limpa tenant no `finally`.

### Classe: `WhatsAppWebhookService`
1. Lê tenant atual;
2. Usa OpenAI para extrair dados de agendamento;
3. cria/atualiza customer por telefone;
4. escolhe ownerUser como primeiro usuário do tenant;
5. monta `AppointmentRequest` com 60 min padrão;
6. chama `appointmentService.create`.

---

## 7.18 Utilitário

### Classe: `StringUtils`
- método `sanitize(String)`:
  - normaliza acentos;
  - remove diacríticos;
  - minúsculas;
  - troca não alfanumérico por `_`;
  - compacta `_` repetidos;
  - remove `_` nas pontas.

Usado para gerar IDs/nomes seguros em provisionamento de tenant.

---

## 7.19 Teste existente

### Classe: `StudiobelleApplicationTests`
- `@SpringBootTest` + método `contextLoads` vazio.
- Verifica apenas se contexto da aplicação sobe.

---

## 8) Relacionamentos entre classes (injeção + JPA + dependências)

## 8.1 Injeção de dependência principal
- `AppointmentService` depende de 8 componentes (repo + serviços correlatos);
- `AuthService` depende de repositórios e serviços de segurança;
- `AutomationService` depende de dados CRM + integrações externas;
- `TenantService` depende de master datasource, repo, roteador e factory;
- `DataSourceConfig` conecta filtros e datasource routing.

## 8.2 Relações JPA
- `Appointment` -> `User` (`ManyToOne`, obrigatório);
- `Appointment` -> `Customer/ServiceOffering/Professional` (`ManyToOne`, opcionais);
- `RefreshToken` -> `User` (`ManyToOne`, obrigatório);
- `CustomerHistory` -> `Customer` (`ManyToOne`, obrigatório);
- `Professional` <-> `ServiceOffering` (`ManyToMany`, tabela `professional_services`).

## 8.3 Fluxo de dependência padrão
`Controller` -> `Service` -> `Repository` -> `Entity/DB`

Exemplos:
- `CustomerController` -> `CustomerService` -> `CustomerRepository`;
- `AuthController` -> `AuthService` -> `UserRepository/RefreshTokenRepository`;
- `AppointmentController` -> `AppointmentService` -> múltiplos serviços e repositórios.

---

## 9) Simulação de Fluxos Reais (execução passo a passo)

## 9.1 Fluxo A — Login
1. Requisição `POST /auth/login` com `X-Tenant-ID` ou subdomínio válido.
2. `TenantFilter` define tenant no `TenantContext`.
3. `JwtAuthenticationFilter` ignora rota pública `/auth/login`.
4. `AuthController.login` valida body e chama `AuthService.login`.
5. `AuthService`:
   - busca usuário por email;
   - valida tenant igual ao contexto;
   - valida ativo;
   - valida senha com `PasswordService.matches`;
   - remove refresh antigo;
   - gera access JWT + refresh.
6. Retorno `AuthResponse` com tokens e expiração.

## 9.2 Fluxo B — Criar agendamento manual (`POST /appointments`)
1. Tenant já resolvido no filtro.
2. JWT obrigatório validado; role não é forçada para este endpoint (apenas autenticação).
3. `AppointmentController.create` chama service.
4. `AppointmentService.create` executa todas validações de conflito, duração, horários.
5. Cliente é criado/atualizado.
6. Agendamento salvo.
7. Histórico CRM atualizado.
8. Mensagem WhatsApp enviada se houver configuração no master.
9. Retorna `AppointmentResponse`.

## 9.3 Fluxo C — Webhook WhatsApp
1. Meta chama `POST /whatsapp/webhook`.
2. `TenantFilter` pula esse endpoint.
3. `JwtAuthenticationFilter` também trata como público.
4. `WhatsAppWebhookController` extrai `phone_number_id` e resolve tenant no master.
5. Controller seta tenant manualmente.
6. `WhatsAppWebhookService` extrai intenção/dados da mensagem e cria appointment.
7. Contexto tenant é limpo no final.

## 9.4 Fluxo D — Job de reativação
1. Scheduler dispara a cada 30 minutos.
2. `AutomationService.processReactivationAutomations` carrega settings de todos tenants.
3. Para cada tenant apto, busca automação REACTIVATION ativa.
4. Identifica clientes inativos.
5. Personaliza mensagem.
6. Humaniza com OpenAI (se habilitado).
7. Envia WhatsApp.

---

## 10) Como rodar o projeto (comandos reais)

## 10.1 Requisitos
- Java 21;
- Docker/Docker Compose (opcional mas recomendado para infra local);
- Maven Wrapper (`./mvnw`).

## 10.2 Subir banco/local stack
```bash
docker compose up -d
```
Serviços definidos:
- postgres 15 (porta 5432);
- redis 7 (porta 6379);
- pgadmin (porta 5050).

## 10.3 Rodar aplicação
```bash
./mvnw spring-boot:run
```
ou gerar jar e executar:
```bash
./mvnw clean package
java -jar target/studiobelle-0.0.1-SNAPSHOT.jar
```

## 10.4 Dockerfile
- Base JDK 21 (`eclipse-temurin:21-jdk-jammy`);
- copia jar para `/app/app.jar`;
- entrypoint `java -jar app.jar`.

---

## 11) Como testar (prático)

## 11.1 Teste automatizado existente
Apenas `contextLoads` no `StudiobelleApplicationTests`.

## 11.2 Testes manuais recomendados com cURL

### 1) Criar tenant
```bash
curl -X POST http://localhost:8080/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "nome":"Studio A",
    "tenantId":"studio_a",
    "subdomain":"studioa",
    "phoneNumberId":"123456",
    "businessAccountId":"WABA_1",
    "accessToken":"TOKEN_META"
  }'
```

### 2) Registrar usuário admin
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: studio_a" \
  -d '{
    "name":"Admin Studio A",
    "email":"admin@studioa.com",
    "password":"12345678",
    "role":"ADMIN"
  }'
```

### 3) Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: studio_a" \
  -d '{"email":"admin@studioa.com","password":"12345678"}'
```

### 4) Criar serviço
```bash
curl -X POST http://localhost:8080/services \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "X-Tenant-ID: studio_a" \
  -H "Content-Type: application/json" \
  -d '{"name":"Corte Feminino","durationMinutes":60,"price":80.00,"active":true}'
```

### 5) Criar profissional
```bash
curl -X POST http://localhost:8080/professionals \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "X-Tenant-ID: studio_a" \
  -H "Content-Type: application/json" \
  -d '{"name":"Maria","workingHours":"08:00-18:00","serviceIds":[1]}'
```

### 6) Criar cliente
```bash
curl -X POST http://localhost:8080/customers \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "X-Tenant-ID: studio_a" \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana","phone":"5511999999999","email":"ana@email.com","notes":"Cliente VIP"}'
```

### 7) Criar agendamento
```bash
curl -X POST http://localhost:8080/appointments \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "X-Tenant-ID: studio_a" \
  -H "Content-Type: application/json" \
  -d '{
    "ownerUserId":1,
    "clientName":"Ana",
    "clientPhone":"5511999999999",
    "serviceDescription":"Corte Feminino",
    "startTime":"2030-01-10T10:00:00",
    "endTime":"2030-01-10T11:00:00",
    "customerId":1,
    "serviceId":1,
    "professionalId":1
  }'
```

### 8) Simular webhook WhatsApp
```bash
curl -X POST http://localhost:8080/whatsapp/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "entry":[{
      "changes":[{
        "value":{
          "metadata":{"phone_number_id":"123456"},
          "messages":[{"from":"5511999999999","text":{"body":"Oi, quero agendar sobrancelha amanhã às 10"}}],
          "contacts":[{"profile":{"name":"Ana"}}]
        }
      }]
    }]
  }'
```

---

## 12) Problemas Encontrados (análise sênior)

1. **Flyway + `ddl-auto=update` ao mesmo tempo**
   - Pode gerar mudanças implícitas fora dos scripts versionados.

2. **JWT artesanal com parsing textual**
   - `extractString/extractLong` por indexOf pode quebrar em payloads atípicos.

3. **Automação usa `phoneNumberId` global**
   - Contraria estratégia multi-tenant por conta WhatsApp no master.

4. **DataSourceFactory com host fixo `localhost`**
   - Reduz portabilidade para ambientes reais.

5. **TenantService cria banco por SQL concatenado**
   - Embora haja sanitização, é ponto sensível e merece hardening adicional.

6. **`AppointmentService.create` não aproveita `customerId` de forma distinta**
   - parâmetro existe no contrato, mas fluxo atual não diferencia.

7. **`DashboardService` usa agregação em memória**
   - carrega listas inteiras, potencial problema de performance.

8. **Cobertura de testes muito baixa**
   - apenas teste de contexto.

9. **Estratégia de erro HTTP pouco uniforme em algumas partes**
   - segurança usa 401; regra de negócio usa 400; faltam códigos de erro internos padronizados.

10. **Possível fragilidade de migração V1/V4 para coluna tenant**
   - renome envolvendo variação de case pode exigir cuidado em ambientes já existentes.

---

## 13) Correções detalhadas (sem quebrar projeto e sem alterar dependências)

## Problema 1: Flyway + ddl-auto
### Correção sugerida
- Definir por perfil:
  - `dev`: `ddl-auto=update` (opcional);
  - `prod`: `ddl-auto=validate` ou `none`.
### Passo a passo
1. criar `application-dev.yaml` e `application-prod.yaml`;
2. mover overrides de JPA;
3. garantir que todo ajuste de schema vá para Flyway.

## Problema 2: JWT parsing manual
### Correção sugerida
- Manter dependências atuais, mas refatorar parser para usar manipulação JSON robusta já disponível no Spring/Jackson.
### Passo a passo
1. ao validar token, decodificar payload;
2. mapear JSON com `ObjectMapper`;
3. validar campos obrigatórios;
4. manter assinatura e expiração como já estão.

## Problema 3: Automação WhatsApp global
### Correção sugerida
- Injetar `MasterTenantDirectory` em `AutomationService` e resolver conta por tenant em cada envio.
### Passo a passo
1. remover dependência do `phoneNumberId` global;
2. no loop do tenant, buscar `findWhatsAppByTenantId`;
3. enviar com token/phone daquele tenant.

## Problema 4: DataSourceFactory hardcoded
### Correção sugerida
- Ler host/porta base de propriedade de configuração.
### Passo a passo
1. criar propriedades `app.database.host` e `app.database.port`;
2. injetar via `@Value`;
3. montar URL dinamicamente.

## Problema 5: `customerId` ignorado em appointment
### Correção sugerida
- Respeitar semântica do request.
### Passo a passo
1. se `customerId != null`, buscar customer por id+tenant;
2. validar phone/name coerentes (ou sobrescrever com dados do customer);
3. senão usar `createOrUpdateByPhone`.

## Problema 6: Dashboard em memória
### Correção sugerida
- Criar consultas agregadas nos repositories (count/sum por intervalo).
### Passo a passo
1. adicionar métodos de contagem/soma por JPQL;
2. substituir streams em memória;
3. validar resultados com testes.

## Problema 7: Falta de testes
### Correção sugerida
- Implementar testes de integração por módulos críticos.
### Passo a passo
1. testes para auth (register/login/refresh);
2. testes para isolamento tenant;
3. testes para conflito de agenda;
4. testes de webhook.

---

## 14) Melhorias profissionais (SOLID/Clean Code/Manutenibilidade)

1. **Aplicar Use Cases explícitos** no módulo de agendamento
   - separar validações em componentes menores (`AvailabilityPolicy`, `BusinessHoursPolicy`, etc.).
2. **Reduzir acoplamento de `AppointmentService`**
   - atualmente coordena muitas responsabilidades em uma classe.
3. **Padronizar contratos de erro**
   - código interno + mensagem + timestamp + path.
4. **Adicionar idempotência no webhook**
   - evitar criação duplicada em retries da Meta.
5. **Observabilidade**
   - logs estruturados com tenantId/requestId em pontos críticos.
6. **Governança de segredo/configuração**
   - usar variáveis de ambiente e profiles de forma rígida.
7. **Hardening de provisionamento de tenant**
   - validações fortes em `TenantRequest`, limites de tamanho e políticas de nome.

---

## 15) Conclusão didática

O projeto já possui uma base sólida para SaaS multi-tenant real: separa bem domínios, tem fluxo de autenticação completo, integrações úteis (WhatsApp/OpenAI) e um pipeline claro de agenda+CRM.

Os maiores ganhos para um nível enterprise vêm de:
- robustez de segurança/token;
- consistência total da estratégia multi-tenant em todos os módulos;
- testes automatizados de regras críticas;
- refatoração de serviços “orquestradores grandes”.

Se você estudar este código com foco nas classes `TenantFilter`, `DataSourceConfig`, `AppointmentService`, `AuthService`, `AutomationService`, `WhatsAppWebhookController/Service`, vai entender o coração arquitetural do sistema e como ele se comporta em produção multi-tenant.

