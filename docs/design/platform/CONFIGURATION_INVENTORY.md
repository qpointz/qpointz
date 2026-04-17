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
| `mill.ai.model` | `AiModelProperties` | mill-ai-v3-autoconfigure | Chat LLM: provider, model name, api-key, base-url |
| `mill.ai` (nested: `enabled`, `providers`, `embedding-model`) | `AiConfigurationProperties` | mill-ai-v3-autoconfigure | Master `enabled` flag; provider map + named embedding profiles (`AiProviderEntry`, `EmbeddingModelProfile`) |
| `mill.ai.value-mapping` | `ValueMappingConfigurationProperties` | mill-ai-v3-autoconfigure | `embedding-model` references a key under `mill.ai.embedding-model` |
| `mill.ai.vector-store` | `VectorStoreConfigurationProperties` | mill-ai-v3-autoconfigure | Backend `in-memory` or `chroma`; nested `chroma.*`; single `EmbeddingStore` per context |

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
| `GrpcServiceSecurityConfiguration` | mill-jet-grpc-service | `@ConditionalOnService("grpc")` + `@ConditionalOnSecurity` | gRPC auth readers, `GrpcSecurityMetadataSource` |

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
| `AiModelProperties` | mill-ai-v3-autoconfigure | `@ConfigurationProperties("mill.ai.model")` | Chat LLM configuration |
| `AiConfigurationProperties` | mill-ai-v3-autoconfigure | `@ConfigurationProperties("mill.ai")` | `providers`, `embedding-model` maps; `AiV3AutoConfiguration` |
| `ValueMappingConfigurationProperties` | mill-ai-v3-autoconfigure | `@ConfigurationProperties("mill.ai.value-mapping")` | `embedding-model` profile name |
| `VectorStoreConfigurationProperties` | mill-ai-v3-autoconfigure | `@ConfigurationProperties("mill.ai.vector-store")` | `backend` + nested `chroma` (HTTP URL, API version, tenant, database, collection, timeout) |
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
| `FunctionContainerConfig` | mill-azure-service-function | Azure Function container config |
| `RapidsWorkerApplicationConfiguration` | rapids-srv-worker | Rapids worker |
| `GrpcServiceApplicationConfiguration` | rapids-grpc-service | Rapids gRPC |

---

## 3. Application YAML/Properties Files

### Main application config
- `apps/mill-service/src/main/resources/application.yml` — primary config with profiles: default, local-jdbc, local-calcite, local-auth, local-cmart

### Sample / default configs
- `apps/mill-service/config/default/application-*.yml` — calcite-sample, jdbc-sample, moneta-sample, tls, auth
- `apps/mill-service/config/samples/application-moneta.yml`
- `apps/mill-service/config/test/application-auth.yml`

### Service-specific
- `services/mill-jet-http-service/src/main/resources/application-jet-http.yml`
- `misc/cloud/mill-azure-service-function/src/main/resources/application.properties`
- `misc/rapids/rapids-srv-worker/src/main/resources/application.yaml`
- `misc/rapids/rapids-grpc-service/src/main/resources/application.yaml`

### Test resources
- `core/mill-starter-backends/src/test*/resources/application-test-*.yaml` — jdbc, jdbc-multi-schema, calcite, moneta-it
- `core/mill-service-core/src/test/resources/application-test-*.yaml` — cmart, jdbc, calcite
- `core/mill-security-core/src/test/resources/application-test-*.yml` — trivial, policy-inline, custom-security
- `core/mill-starter-service/src/test/resources/application-test-jdbc.yml`
- `services/mill-jet-grpc-service/src/test/resources/application-test.yml`
- `services/mill-jet-http-service/src/test/resources/application-test-cmart.yml`
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

### mill.ai.model (AI v3 chat — `AiModelProperties`)
- `provider`, `api-key`, `model-name`, `base-url`

### mill.ai.providers (WI-175 — `AiConfigurationProperties.providers`)
- `<providerId>.api-key`, `<providerId>.base-url` — per-provider credentials (OpenAI-compatible first)

### mill.ai.embedding-model (WI-176 — `AiConfigurationProperties.embeddingModel`)
- `<name>.provider` — provider id (`stub`, `openai`, …); `stub` for deterministic tests
- `<name>.model-name`, `<name>.dimension` — non-secret embedding params

### mill.ai.value-mapping (WI-176 — `ValueMappingConfigurationProperties`)
- `embedding-model` — name of a profile under `mill.ai.embedding-model`
- `max-content-length` — max length for value-mapping embedding line / persisted `content` (default **2048**; see value-mapping facets G-5)
- `refresh.on-startup.enabled` — global **`APP_STARTUP`** gate (default **true**)
- `refresh.schedule.enabled` — register scheduled refresh job (default **true**)
- `refresh.schedule.interval` — **`Duration`** tick cadence for scheduled passes (default **PT15M**)

### mill.ai.vector-store (WI-177 — `VectorStoreConfigurationProperties`)
- `backend` — `in-memory` (default) or `chroma`; **one** active backend per application context — see [`../ai/mill-ai-configuration.md`](../ai/mill-ai-configuration.md)
- `chroma.base-url`, `chroma.api-version`, `chroma.tenant-name`, `chroma.database-name`, `chroma.collection-name`, `chroma.timeout` — used when `backend=chroma`

### mill.services
- `grpc.port`, `grpc.address`, `grpc.enable`
- `jet-http.enable`
- `meta.enable`
- `grinder.enable`
- `ai-nl2data.enable`
- `data-bot.enable`

---

## 5. Refactoring Considerations

1. **`mill.ai.*` extension** — Provider map (`mill.ai.providers`), embedding registry (`mill.ai.embedding-model`), vector store (`mill.ai.vector-store`), and value-mapping references (`mill.ai.value-mapping`) are documented in [`../ai/mill-ai-configuration.md`](../ai/mill-ai-configuration.md); Java `@ConfigurationProperties` classes in **`mill-ai-v3-autoconfigure`** (WI-175–WI-177); sync/service wiring in **`ValueMappingSyncAutoConfiguration`** (WI-179/WI-180).
2. **Duplicate prefix** — `mill.security.authorization.policy` used by both `PolicyConfiguration` and `PolicyActionsConfiguration`; consider merging or splitting namespaces.
3. **Typo** — `mill.backend.jdbc.multi-shema` should be `multi-schema`.
4. **Legacy vs greenfield** — `mill.metadata.file.repository.path` (legacy data-layer repo) vs **`mill.metadata.repository.*`** (metadata service); document migration in operator docs.
5. **ConditionalOnService** — Services enabled via `mill.services.<name>.enable`; `OnServiceEnabledCondition` checks for presence of `mill.services.<name>` group.
6. **ConditionalOnSecurity** — Custom annotation; security beans gated by `mill.security.enable`.
7. **functionContextFlag** — Hack to detect Azure Function context; multiple security configs use `@ConditionalOnMissingBean(name = "functionContextFlag")`.
8. **Qualifier typo** — `Nl2SqlConfiguration.vectorStoreDocumentSources` uses `@Qualifier("LOJOKOJ")`; likely placeholder.
9. **Config file sprawl** — Many profile-specific YAML files; consider `spring.config.import` or consolidation.
10. **Value mapping observability** — Metrics for sync / embed / vector-store paths are not yet part of configuration surface; **action points** are tracked in [`../ai/value-mapping-observability-actions.md`](../ai/value-mapping-observability-actions.md) (see [`mill-configuration.md`](mill-configuration.md) **Observability gaps**).
