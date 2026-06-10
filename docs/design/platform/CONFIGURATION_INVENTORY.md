# Spring Configuration Inventory

> Generated for configuration refactoring planning. Maps all Spring configuration files, `@ConfigurationProperties`, and corresponding consuming code.

---

## 1. Configuration Property Prefixes & Classes

| Prefix | Class | Module | Purpose |
|--------|-------|--------|---------|
| `mill.backend` | `BackendConfiguration` | mill-starter-backends | Backend provider (jdbc/calcite), connection settings |
| `mill.backend.jdbc` | `JdbcCalciteConfiguration` | mill-starter-backends | JDBC URL, driver, credentials, schema |
| `mill.backend.calcite` | `CalciteServiceConfiguration` | mill-starter-backends | Calcite model path |
| `mill.security` | `SecurityConfig` | mill-security-core | Security enable flag |
| `mill.security.authorization.policy` | `PolicyConfiguration` | mill-service-core | Policy selector, granted-authority remap |
| `mill.security.authorization.policy` | `PolicyActionsConfiguration` | mill-security-core | Policy actions array |
| `mill.security.authentication.oauth2-resource-server` | `OAuth2AuthenticationConfiguration` | mill-security-core | JWT JWK URI |
| `mill.metadata` | `MetadataProperties` | mill-metadata-autoconfigure | Repository backend (`repository.type`, `repository.file.*`), facet-type registry |
| `mill.metadata.seed` | `MetadataSeedProperties` | mill-metadata-autoconfigure | Ordered startup seed resources, `on-failure` policy |
| `mill.metadata.file.repository` | `MetadataConfiguration` (bean) | mill-service-core | Legacy file repository path (data autoconfigure; not the greenfield metadata service) |
| `mill.ai.nl2sql` | `ValueMappingConfiguration` | mill-ai-core | NL2SQL enable, dialect, reasoner, value-mapping |
| `mill.services.*` | `OnServiceEnabledCondition` | mill-service-core | Service toggles (grpc, jet-http, meta, grinder, ai-nl2data, data-bot) |
| `mill.ai` (nested: `enabled`, `providers`, `models`, `vector-stores`) | `AiConfigurationProperties` | mill-ai-v3-autoconfigure | Master flag; providers (`type`); `models.chat` / `models.embedding`; optional `vector-stores` registry |
| `mill.ai.data` (nested: `embedding`) | `DataEmbeddingConfigurationProperties` | mill-ai-v3-autoconfigure | Per-profile pipeline: `model`, `vector-store`, `max-content-length`, `refresh`, `sources[]` |
| `mill.ai.chat` (nested: `model`, `value-mapping.embedding`, `schema-search.embedding`) | `AiV3ChatProperties` | mill-ai-v3-autoconfigure | Chat defaults; capability hooks into `data.embedding` profiles |

---

## 2. @Configuration Classes (by domain)

### 2.1 Backend / Data Access

| Class | Module | Condition | Key Beans |
|-------|--------|-----------|-----------|
| `BackendConfiguration` | mill-starter-backends | — | `SimpleExtension.ExtensionCollection` |
| `JdbcCalciteConfiguration` | mill-starter-backends | `mill.backend.provider=jdbc` | `JdbcContextFactory`, `ExecutionProvider`, `SchemaProvider`, `SqlProvider`, `CalciteContextFactory`, `PlanConverter` |
| `CalciteServiceConfiguration` | mill-starter-backends | `mill.backend.provider=calcite` | `CalciteContextFactory`, `PlanConverter`, `SchemaProvider`, `ExecutionProvider`, `SqlProvider` |

**Consumers of BackendConfiguration / JdbcCalciteConfiguration:**
- `JdbcCalciteContextFactory`, `JdbcContextFactoryImpl`, `JdbcContextImpl` — use config for connection/schema
- Tests: `JdbcConnectionProviderTestIT`, `JdbcBackendExecutionTestIT`, `MultiSchemaBackendTests`, `JdbcCalciteContextFactoryTest`, `JdbcExecutionProviderTest`, `JdbcSchemaProviderTest`, `BaseTest`, `SchemaPlusSchemaProviderTest`, `MillGrpcServiceBaseTest`, `ServiceBaseTest`, `ColumnsMetadataTest`, `InProcessTest`

### 2.2 Security

| Class | Module | Condition | Key Beans |
|-------|--------|-----------|-----------|
| `SecurityConfig` | mill-security-core | `@ConditionalOnSecurity` | `AuthenticationMethods`, `AuthenticationProvider`, `AuthenticationManager` |
| `PolicyConfiguration` | mill-service-core | — | Properties only (selector, granted-authority remap) |
| `PolicyActionsConfiguration` | mill-security-core | `@ConditionalOnSecurity` | Properties only (actions array) |
| `OAuth2AuthenticationConfiguration` | mill-security-core | `@ConditionalOnSecurity` + `mill.security.authentication.oauth2-resource-server.enable` | `AuthenticationMethod` (OAuth2) |
| `PasswordAuthenticationConfiguration` | mill-security-core | `@ConditionalOnSecurity` + `mill.security.authentication.basic.enable` | `PasswordEncoder`, `AuthenticationMethod` (basic) |
| `EntraIdAuthenticationConfiguration` | mill-security-core | `mill.security.authentication.entra-id-token.enable` | Entra ID auth |
| `ApiSecurityConfiguration` | mill-security-core | `@ConditionalOnMissingBean(functionContextFlag)` | SecurityFilterChain for `/api/**` |
| `AppSecurityConfiguration` | mill-security-core | same | SecurityFilterChain for `/app/**` |
| `ServicesSecurityConfiguration` | mill-security-core | same | SecurityFilterChain for `/services/**` |
| `AuthRoutesSecurityConfiguration` | mill-security-core | same | SecurityFilterChain for auth routes |
| `WellKnownSecurityConfiguration` | mill-security-core | same | SecurityFilterChain for `/.well-known/**` |
| `SwaggerSecurityConfig` | mill-security-core | same | SecurityFilterChain for swagger |
| `MillGrpcConfiguration` / `GrpcSecurityInterceptor` | `services/mill-data-grpc-service` | gRPC enable + security metadata on **grpc-java** | Netty server, Spring Security–backed interceptor |

**Consumers:**
- `FunctionContainerConfig` — `@AutoConfigureBefore(SecurityConfig.class)`
- `BackendFunctions` — imports `SecurityConfig`
- `DefaultFilterChainConfiguration` — uses `PolicyActionsConfiguration`, `PolicyConfiguration`

### 2.3 Metadata

| Class | Module | Condition | Key Beans |
|-------|--------|-----------|-----------|
| `MetadataCoreConfiguration` | mill-metadata-autoconfigure | — | Facet type registry beans, `FacetCatalog` |
| `MetadataProperties` | mill-metadata-autoconfigure | `@EnableConfigurationProperties` | `mill.metadata.repository.*`, `mill.metadata.facet-type-registry.*` |
| `MetadataSeedProperties` | mill-metadata-autoconfigure | — | `mill.metadata.seed.*` |
| `MetadataJpaPersistenceAutoConfiguration` | mill-metadata-autoconfigure | `mill.metadata.repository.type=jpa` | JPA repositories and entities (via `mill-metadata-persistence`) |
| `MetadataFileRepositoryAutoConfiguration` | mill-metadata-autoconfigure | `mill.metadata.repository.type=file` | In-memory repository adapters + path/seed validation |
| `MetadataSeedAutoConfiguration` | mill-metadata-autoconfigure | — | `MetadataSeedStartup` + `ApplicationRunner` — loads `mill.metadata.seed.resources` (no-op when list empty) |
| `MetadataRepositoryAutoConfiguration` | mill-metadata-autoconfigure | `@AutoConfigureAfter` JPA | NoOp repository fallbacks when real beans missing |
| `MetadataConfiguration` | mill-service-core | — | `MetadataProvider`, `FileRepository` (legacy `mill.metadata.file.repository.path`) |

**Consumers:**
- `MetadataRepositoryAutoConfiguration` — uses `MetadataProperties`
- `MetadataV2RelationsProvider` — `mill.metadata.relations=v2`
- `MetadataV2AnniotationsProvider` — `mill.metadata.annotations=v2`
- `FileRelationsProvider` — `mill.metadata.relations=file`
- `FileAnnotationsRepository` — `mill.metadata.annotations=file`
- `NoneAnnotationsRepository` — `mill.metadata.annotations=none`
- `NoneRelationsProvider` — `mill.metadata.relations=none`

### 2.4 AI / NL2SQL

| Class | Module | Condition | Key Beans |
|-------|--------|-----------|-----------|
| `ValueMappingConfiguration` | mill-ai-core | — | Properties: enable, dialect, reasoner, value-mapping |
| `Nl2SqlConfiguration` | mill-ai-core | — | `SqlDialect`, `vectorStoreDocumentSources` |
| `AIConfiguration` | mill-ai-nlsql-chat-service | `@ConditionalOnService("ai-nl2data")` | `ChatMemory`, `VectorStore`, `ValueRepository`, `ValueMapper` |
| `JPAConfiguration` | mill-ai-nlsql-chat-service | `@ConditionalOnService("ai-nl2data")` | JPA repositories |
| `GlobalExceptionHandler` | mill-ai-nlsql-chat-service | `@ConditionalOnService("ai-nl2data")` | Exception handling |

**Consumers:**
- `ChatProcessor` — injects `ValueMappingConfiguration`
- `Nl2SqlConfiguration` — binds `mill.ai.nl2sql.dialect`, `mill.ai.nl2sql.value-mapping`
- `AIConfiguration` — `mill.ai.chat.memory` (in-memory | jdbc)

### 2.4.1 AI v3 (`mill-ai-v3-autoconfigure`)

| Class | Module | Condition | Key Beans |
|-------|--------|-----------|-----------|
| `AiConfigurationProperties` | mill-ai-v3-autoconfigure | `@ConfigurationProperties("mill.ai")` | `providers`, `models`, `vector-stores` |
| `DataEmbeddingConfigurationProperties` | mill-ai-v3-autoconfigure | `@ConfigurationProperties("mill.ai.data")` | `embedding` profile map |
| `AiV3ChatProperties` | mill-ai-v3-autoconfigure | `@ConfigurationProperties("mill.ai.chat")` | `model`, `value-mapping.embedding`, chat service defaults |
| `AiV3AutoConfiguration` / `EmbeddingAutoConfiguration` / `VectorStoreAutoConfiguration` | mill-ai-v3-autoconfigure | `@ConditionalOn*` | `AiModelProviderRegistry`, `EmbeddingHarness`, `EmbeddingStore` |
| `ValueMappingSyncAutoConfiguration` | mill-ai-v3-autoconfigure | `@AutoConfigureAfter` AI + JPA | `VectorMappingSynchronizer`, `ValueMappingService` when repository + harness + store exist |
| `AiV3JpaConfiguration` | mill-ai-v3-autoconfigure | JPA on classpath | `ValueMappingEmbeddingRepository` JPA adapter (with `AiEmbeddingModelRepository` / `AiValueMappingRepository`) |

**Design:** [`../ai/mill-ai-configuration.md`](../ai/mill-ai-configuration.md)

### 2.5 Service Core

| Class | Module | Condition | Key Beans |
|-------|--------|-----------|-----------|
| `DefaultServiceConfiguration` | mill-service-core | — | `ServiceHandler`, `ResultAllocator`, `SecurityDispatcher`, `SubstraitDispatcher`, `DataOperationDispatcher`, `PlanRewriteChain`, `PlanDispatcher` |
| `DefaultFilterChainConfiguration` | mill-service-core | `@ConditionalOnSecurity` | `PolicyRepository`, `PolicyEvaluator`, `PolicySelector`, `TableFacetFactory`, `tableFacetPlanRewriter` |

### 2.6 Other

| Class | Module | Purpose |
|-------|--------|---------|
| `OpenApiConfig` | mill-metadata-service | OpenAPI/Swagger metadata |
| `FunctionContainerConfig` | `misc/cloud/mill-azure-service-function` | Azure Function container config (not in root composite build) |

*Historical note:* the **`misc/rapids/`** tree (`rapids-srv-worker`, `rapids-grpc-service`, etc.) was **removed** from this repository; see [`spring4-migration-plan.md`](spring4-migration-plan.md).

---

## 3. Application YAML/Properties Files

### Main application config
- `apps/mill-service/src/main/resources/application.yml` — primary config with profiles: default, local-jdbc, local-calcite, local-auth, local-cmart

### Sample / default configs
- `apps/mill-service/config/default/application-*.yml` — calcite-sample, jdbc-sample, moneta-sample, tls, auth
- `apps/mill-service/config/samples/application-moneta.yml`
- `apps/mill-service/config/test/application-auth.yml`

### Service-specific
- `misc/cloud/mill-azure-service-function/src/main/resources/application.properties` (sample Azure Function; not built by root `settings.gradle.kts`)

### Test resources
- `core/mill-starter-backends/src/test*/resources/application-test-*.yaml` — jdbc, jdbc-multi-schema, calcite, moneta-it
- `core/mill-service-core/src/test/resources/application-test-*.yaml` — cmart, jdbc, calcite
- `core/mill-security-core/src/test/resources/application-test-*.yml` — trivial, policy-inline, custom-security
- `core/mill-starter-service/src/test/resources/application-test-jdbc.yml`
- `services/mill-data-grpc-service/src/testIT/resources/application-test.yml`
- `services/mill-data-http-service/src/test/resources/application-test-cmart.yml`
- `clients/mill-jdbc-driver/src/test/resources/application-in-proc-test.yml`
- `clients/etc/test-backend-server/application*.yml` — default, tls, auth
- `ai/mill-ai-core/src/test*/resources/application-test-*.yml` — moneta-it, valuemap-it, slim-it
- `ai/mill-ai-nlsql-chat-service/src/testIT/resources/application-test-moneta-slim-it.yml`

---

## 4. Property Reference (mill.*)

### mill.backend
- `provider` — jdbc | calcite
- `connection.quoting`, `connection.caseSensitive`, `connection.unquotedCasing`, `connection.model` (calcite)

### mill.backend.jdbc
- `url`, `driver`, `username`/`user`, `password`, `target-schema`, `schema`, `catalog`, `multi-shema` (typo)

### mill.security
- `enable` — master switch

### mill.security.authentication.basic
- `enable`, `file-store`

### mill.security.authentication.oauth2-resource-server
- `enable`, `jwt.jwk-set-uri`

### mill.security.authentication.entra-id-token
- `enable`

### mill.security.authorization.policy
- `enable`, `selector.granted-authority.remap`, `actions[]`

### mill.metadata
- `relations` — none | file | v2 (data autoconfigure — annotations/relations providers)
- `annotations` — none | file | v2

### mill.metadata.file.repository (legacy)
- `path` — Resource path (legacy `MetadataProvider` file repo, not greenfield metadata service)

### mill.metadata.repository (greenfield metadata service)
- `type` — file | jpa | noop
- `file.path`, `file.writable`, `file.watch`

### mill.metadata.facet-type-registry
- `type` — inMemory | local | portal (reserved)

### mill.metadata.seed
- `resources` — list of resource URIs (ordered); **sole** mechanism for platform/global scope and standard facet types at startup (`classpath:metadata/platform-bootstrap.yaml`)
- `on-failure` — fail-fast | continue

### mill.ai.nl2sql
- `enable`, `dialect`, `reasoner`, `value-mapping[]`

### mill.ai.chat
- `memory` — in-memory | jdbc

### mill.ai.providers (`AiConfigurationProperties.providers`)
- `<providerId>.type` — provider implementation (v1: `openai`)
- `<providerId>.api-key`, `<providerId>.base-url` — per-provider credentials

### mill.ai.models (`AiConfigurationProperties.models`)
- `chat.<name>.provider`, `chat.<name>.model-name`
- `embedding.<name>.provider`, `embedding.<name>.model-name`, `embedding.<name>.dimension`

### mill.ai.vector-stores (`AiConfigurationProperties.vectorStores`)
- `<id>.backend` — `in-memory`, `chroma`, or `pgvector`
- `<id>.chroma.*`, `<id>.pgvector.*` — shared connection templates

### mill.ai.data.embedding (`DataEmbeddingConfigurationProperties.embedding`)
- `<profile>.model` — key into `mill.ai.models.embedding`
- `<profile>.vector-store.backend` — built-in id or `vector-stores` registry key
- `<profile>.vector-store.chroma.*`, `<profile>.vector-store.pgvector.*`
- `<profile>.max-content-length` (default **2048**)
- `<profile>.refresh.on-startup.enabled`, `<profile>.refresh.schedule.enabled`, `<profile>.refresh.schedule.interval`
- `<profile>.sources[]` — v1: one `type: metadata-facets` entry

### mill.ai.chat (`AiV3ChatProperties`)
- `model` — key into `mill.ai.models.chat`
- `default-profile`, `default-user-id`, `max-title-length`
- `value-mapping.embedding` — key into `mill.ai.data.embedding`
- `schema-search.embedding` — reserved capability hook
- **Mill Service** — profiles **`chromadb`** / **`ai-pgvector`** override `data.embedding.default.vector-store` (see [`../ai/mill-ai-configuration.md`](../ai/mill-ai-configuration.md))

### mill.services
- `grpc.port`, `grpc.address`, `grpc.enable`
- `jet-http.enable`
- `meta.enable`
- `grinder.enable`
- `ai-nl2data.enable`
- `data-bot.enable`

---

## 5. Refactoring Considerations

1. **`mill.ai.*` layers** — `providers`, `models`, optional `vector-stores`, `data.embedding` profiles, and `chat` capability hooks — documented in [`../ai/mill-ai-configuration.md`](../ai/mill-ai-configuration.md); implemented in **`mill-ai-v3-autoconfigure`** (WI-284–WI-288).
2. **Duplicate prefix** — `mill.security.authorization.policy` used by both `PolicyConfiguration` and `PolicyActionsConfiguration`; consider merging or splitting namespaces.
3. **Typo** — `mill.backend.jdbc.multi-shema` should be `multi-schema`.
4. **Legacy vs greenfield** — `mill.metadata.file.repository.path` (legacy data-layer repo) vs **`mill.metadata.repository.*`** (metadata service); document migration in operator docs.
5. **ConditionalOnService** — Services enabled via `mill.services.<name>.enable`; `OnServiceEnabledCondition` checks for presence of `mill.services.<name>` group.
6. **ConditionalOnSecurity** — Custom annotation; security beans gated by `mill.security.enable`.
7. **functionContextFlag** — Hack to detect Azure Function context; multiple security configs use `@ConditionalOnMissingBean(name = "functionContextFlag")`.
8. **Qualifier typo** — `Nl2SqlConfiguration.vectorStoreDocumentSources` uses `@Qualifier("LOJOKOJ")`; likely placeholder.
9. **Config file sprawl** — Many profile-specific YAML files; consider `spring.config.import` or consolidation.
10. **Value mapping observability** — Metrics for sync / embed / vector-store paths are not yet part of configuration surface; **action points** are tracked in [`../ai/value-mapping-observability-actions.md`](../ai/value-mapping-observability-actions.md) (see [`mill-configuration.md`](mill-configuration.md) **Observability gaps**).
