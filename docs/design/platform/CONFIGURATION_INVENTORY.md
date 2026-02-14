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
| `mill.metadata.v2` | `MetadataProperties` | mill-metadata-core | Metadata storage type, file path |
| `mill.metadata.file.repository` | `MetadataConfiguration` (bean) | mill-service-core | Legacy file repository path |
| `mill.ai.nl2sql` | `ValueMappingConfiguration` | mill-ai-core | NL2SQL enable, dialect, reasoner, value-mapping |
| `mill.services.*` | `OnServiceEnabledCondition` | mill-service-core | Service toggles (grpc, jet-http, meta, grinder, ai-nl2data, data-bot) |

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
| `MetadataCoreConfiguration` | mill-metadata-core | — | Registers facets in `FacetRegistry` |
| `MetadataProperties` | mill-metadata-core | — | Properties only |
| `MetadataRepositoryAutoConfiguration` | mill-metadata-core | `mill.metadata.v2.storage.type=file` (default) | `MetadataRepository` (FileMetadataRepository) |
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
- `relations` — none | file | v2
- `annotations` — none | file | v2

### mill.metadata.file.repository (legacy)
- `path` — Resource path

### mill.metadata.v2
- `storage.type` — file | jpa | composite | external
- `file.path`, `file.watch`

### mill.ai.nl2sql
- `enable`, `dialect`, `reasoner`, `value-mapping[]`

### mill.ai.chat
- `memory` — in-memory | jdbc

### mill.services
- `grpc.port`, `grpc.address`, `grpc.enable`
- `jet-http.enable`
- `meta.enable`
- `grinder.enable`
- `ai-nl2data.enable`
- `data-bot.enable`

---

## 5. Refactoring Considerations

1. **Duplicate prefix** — `mill.security.authorization.policy` used by both `PolicyConfiguration` and `PolicyActionsConfiguration`; consider merging or splitting namespaces.
2. **Typo** — `mill.backend.jdbc.multi-shema` should be `multi-schema`.
3. **Legacy vs v2** — `mill.metadata.file.repository.path` (legacy) vs `mill.metadata.v2.file.path`; consolidate or document migration path.
4. **ConditionalOnService** — Services enabled via `mill.services.<name>.enable`; `OnServiceEnabledCondition` checks for presence of `mill.services.<name>` group.
5. **ConditionalOnSecurity** — Custom annotation; security beans gated by `mill.security.enable`.
6. **functionContextFlag** — Hack to detect Azure Function context; multiple security configs use `@ConditionalOnMissingBean(name = "functionContextFlag")`.
7. **Qualifier typo** — `Nl2SqlConfiguration.vectorStoreDocumentSources` uses `@Qualifier("LOJOKOJ")`; likely placeholder.
8. **Config file sprawl** — Many profile-specific YAML files; consider `spring.config.import` or consolidation.
