# Mill Platform — Configuration Key Inventory

**Generated:** February 2026
**Scope:** All `mill.*` configuration keys, custom conditional annotations, metadata JSON coverage

---

## 1. Master Key Table

### mill.backend

| Key | Type | Declared in | Module | Consumed by | Metadata JSON | Notes |
|-----|------|-------------|--------|-------------|---------------|-------|
| `mill.backend.provider` | String | `BackendConfiguration` (`@ConfigurationProperties`) | data:mill-data-backends | `JdbcCalciteConfiguration` (`@ConditionalOnProperty`), `CalciteServiceConfiguration` (`@ConditionalOnProperty`) | none | Values: `jdbc`, `calcite` |
| `mill.backend.connection` | Map\<String,String\> | `BackendConfiguration` (`@ConfigurationProperties`) | data:mill-data-backends | `JdbcCalciteConfiguration.calciteContextFactory()`, `CalciteServiceConfiguration.calciteConextFactory()` | none | Sub-keys: `quoting`, `caseSensitive`, `unquotedCasing`, `model`, `fun`, `conformance`, `lenientOperatorLookup` |
| `mill.backend.jdbc.url` | String | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcContextImpl`, `JdbcCalciteContextFactory` | none | |
| `mill.backend.jdbc.driver` | String | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcContextImpl`, `JdbcCalciteContextFactory` | none | |
| `mill.backend.jdbc.user` | Optional\<String\> | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcContextImpl` | none | Default: `null` |
| `mill.backend.jdbc.password` | Optional\<String\> | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcContextImpl` | none | Default: `null` |
| `mill.backend.jdbc.target-schema` | Optional\<String\> | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcCalciteContextFactory` | none | Default: `ts`. Some YAMLs use `output-schema` instead — see Issues |
| `mill.backend.jdbc.schema` | Optional\<String\> | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcContextImpl` | none | Default: `null` |
| `mill.backend.jdbc.catalog` | Optional\<String\> | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcContextImpl` | none | Default: `null` |
| `mill.backend.jdbc.multi-shema` | Boolean | `JdbcCalciteConfiguration` (`@Value`) | data:mill-data-backends | `JdbcCalciteContextFactory` | none | **Typo** — should be `multi-schema`. Default: `false` |

### mill.security

| Key | Type | Declared in | Module | Consumed by | Metadata JSON | Notes |
|-----|------|-------------|--------|-------------|---------------|-------|
| `mill.security.enable` | Boolean | `SecurityConfig` (`@ConfigurationProperties`) | core:mill-security-autoconfigure | `OnSecurityEnabledCondition` (all `@ConditionalOnSecurity` users) | **security-autoconfigure** | Master switch |
| `mill.security.authentication.basic.enable` | Boolean | `PasswordAuthenticationConfiguration` (`@ConditionalOnProperty`) | core:mill-security-autoconfigure | — | none | |
| `mill.security.authentication.basic.file-store` | String | `PasswordAuthenticationConfiguration` (`@Value`) | core:mill-security-autoconfigure | — | none | Resource path to passwd YAML |
| `mill.security.authentication.oauth2-resource-server.enable` | Boolean | `OAuth2AuthenticationConfiguration` (`@ConditionalOnProperty`) | core:mill-security-autoconfigure | — | none | |
| `mill.security.authentication.oauth2-resource-server.jwt.jwk-set-uri` | String | Spring Boot OAuth2 auto-config | core:mill-security-autoconfigure | `OAuth2AuthenticationConfiguration` | **security-autoconfigure** | Standard Spring property |
| `mill.security.authentication.entra-id-token.enable` | Boolean | `EntraIdAuthenticationConfiguration` (`@ConditionalOnProperty`) | core:mill-security-autoconfigure | — | **security-autoconfigure** | |
| `mill.security.authorization.policy.enable` | Boolean | `PolicyConfiguration` (`@ConfigurationProperties`) | data:mill-data-autoconfigure | `DefaultFilterChainConfiguration` | **security-autoconfigure** | |
| `mill.security.authorization.policy.selector.granted-authority.remap` | Map\<String,String\> | `PolicyConfiguration` (`@ConfigurationProperties`) | data:mill-data-autoconfigure | `DefaultFilterChainConfiguration` → `GrantedAuthoritiesPolicySelector` | none | |
| `mill.security.authorization.policy.actions` | List\<PolicyActionDescriptor\> | `PolicyActionsConfiguration` (`@ConfigurationProperties`) | core:mill-security-autoconfigure | `DefaultFilterChainConfiguration` → `PolicyActionDescriptorRepository` | **security-autoconfigure** | Inline policy array |
| `mill.security.authorization.policy.actions[].policy` | String | `PolicyActionsConfiguration` | core:mill-security-autoconfigure | — | **security-autoconfigure** | |
| `mill.security.authorization.policy.actions[].verb` | ActionVerb | `PolicyActionsConfiguration` | core:mill-security-autoconfigure | — | **security-autoconfigure** | `ALLOW` or `DENY` |
| `mill.security.authorization.policy.actions[].action` | String | `PolicyActionsConfiguration` | core:mill-security-autoconfigure | — | **security-autoconfigure** | |
| `mill.security.authorization.policy.actions[].params` | Map\<String,Object\> | `PolicyActionsConfiguration` | core:mill-security-autoconfigure | — | **security-autoconfigure** | |

### mill.metadata

| Key | Type | Declared in | Module | Consumed by | Metadata JSON | Notes |
|-----|------|-------------|--------|-------------|---------------|-------|
| `mill.metadata.annotations` | String | `MetadataConfiguration` (`@ConditionalOnProperty`) | data:mill-data-autoconfigure | Bean selection: `FileAnnotationsRepository`, `NoneAnnotationsRepository` | none | Values: `file`, `none`, `v2` |
| `mill.metadata.relations` | String | `MetadataConfiguration` (`@ConditionalOnProperty`) | data:mill-data-autoconfigure | Bean selection: `FileRelationsProvider`, `NoneRelationsProvider` | none | Values: `file`, `none`, `v2` |
| `mill.metadata.file.repository.path` | Resource | `MetadataConfiguration` (`@Value` + `@ConditionalOnProperty`) | data:mill-data-autoconfigure | `FileRepository.from()` | none | **Legacy** — separate from greenfield metadata service |
| `mill.metadata.repository.type` | String | `MetadataProperties` (`@ConfigurationProperties`) | metadata:mill-metadata-autoconfigure | File / JPA / NoOp repository autoconfiguration | processor on Java class | Values: `file`, `jpa`, `noop`. Default: `file` |
| `mill.metadata.repository.file.path` | String | `MetadataProperties.Repository.File` | metadata:mill-metadata-autoconfigure | `MetadataFileRepositoryAutoConfiguration` | — | Comma-separated Spring resource paths. When `type=file`, **either** non-blank `path` **or** non-empty **`mill.metadata.seed.resources`** is required (fail-fast if both empty). |
| `mill.metadata.repository.file.writable` | Boolean | `MetadataProperties.Repository.File` | metadata:mill-metadata-autoconfigure | Reserved | — | Default: `false` |
| `mill.metadata.repository.file.watch` | Boolean | `MetadataProperties.Repository.File` | metadata:mill-metadata-autoconfigure | Reserved | — | Default: `false` |
| `mill.metadata.facet-type-registry.type` | String | `MetadataProperties.FacetTypeRegistry` | metadata:mill-metadata-autoconfigure | `MetadataCoreConfiguration` | — | Values: `inMemory`, `local`, `portal` (reserved) |
| `mill.metadata.seed.resources` | List\<String\> | `MetadataSeedProperties` | metadata:mill-metadata-autoconfigure | `MetadataSeedStartup` | — | Ordered seed YAML locations; sole startup path for platform scope/facet types (`classpath:metadata/platform-bootstrap.yaml` first in prod). Empty skips runner |
| `mill.metadata.seed.on-failure` | String | `MetadataSeedProperties` | metadata:mill-metadata-autoconfigure | `MetadataSeedStartup` | — | `fail-fast` (default) or `continue` |

**Removed (breaking):** `mill.metadata.storage.*`, `mill.metadata.v2.*` — use **`mill.metadata.repository.*`** and **`mill.metadata.seed.*`** only. See [`mill-metadata-domain-model.md`](../metadata/mill-metadata-domain-model.md).

### mill.ai

| Key | Type | Declared in | Module | Consumed by | Metadata JSON | Notes |
|-----|------|-------------|--------|-------------|---------------|-------|
| `mill.ai.nl2sql.enable` | String | `ValueMappingConfiguration` (`@ConfigurationProperties`) | ai:mill-ai-v1-core | `ValueMappingComponents` | none | |
| `mill.ai.nl2sql.dialect` | String | `ValueMappingConfiguration` (`@ConfigurationProperties`) + `Nl2SqlConfiguration` (`@Value`) | ai:mill-ai-v1-core | `Nl2SqlConfiguration` → `SqlDialect` bean | none | |
| `mill.ai.nl2sql.reasoner` | String | `ValueMappingConfiguration` (`@ConfigurationProperties`) | ai:mill-ai-v1-core | `ChatProcessor` | none | Values: `default`, `stepback`. Default: `default` |
| `mill.ai.nl2sql.value-mapping` | List\<Map\> | `Nl2SqlConfiguration` (Binder) | ai:mill-ai-v1-core | `DefaultValueRepository` | none | Polymorphic: `type=sql` |
| `mill.ai.nl2sql.valuemapping[]` | List\<Map\> | — (YAML only) | — | `ValueMappingConfiguration` | none | Alternate YAML spelling of value-mapping |
| `mill.ai.chat.memory` | String | `AIConfiguration` (`@ConditionalOnProperty`) | ai:mill-ai-v1-nlsql-chat-service | Bean selection: in-memory vs JDBC `ChatMemory` | none | Values: `in-memory`, `jdbc` |

### mill.services

| Key | Type | Declared in | Module | Consumed by | Metadata JSON | Notes |
|-----|------|-------------|--------|-------------|---------------|-------|
| `mill.services.grpc.enable` | Boolean | — (checked by `OnServiceEnabledCondition`) | data:mill-data-autoconfigure | `@ConditionalOnService("grpc")`: `MillGrpcService`, `GrpcServiceDescriptor`, `GrpcServiceSecurityConfiguration`, `MillGrpcServiceExceptionAdvice` | **data-grpc-service** | |
| `mill.services.grpc.port` | Integer | — (mapped to `grpc.server.port`) | data:mill-data-grpc-service | gRPC server config | **data-grpc-service** | |
| `mill.services.grpc.address` | String | — (mapped to `grpc.server.address`) | data:mill-data-grpc-service | gRPC server config | **data-grpc-service** | |
| `mill.services.jet-http.enable` | Boolean | — (checked by `OnServiceEnabledCondition`) | data:mill-data-autoconfigure | `@ConditionalOnService("jet-http")`: `AccessServiceController` | **data-http-service** | Type declared as String in JSON — should be Boolean |
| `mill.services.meta.enable` | Boolean | — (checked by `OnServiceEnabledCondition`) | data:mill-data-autoconfigure | `@ConditionalOnService("meta")`: `ApplicationDescriptorController` | **service-starter** | |
| `mill.services.public-base-url` | String | `ApplicationDescriptorConfiguration` (`@Value` via `Environment`) | core:mill-service-starter | `ApplicationDescriptorConfiguration.schemaDescriptors()` | **service-starter** | External base URL for descriptor links (defaults to `server.address`/`server.port`) |
| `mill.application.hosts.externals.<name>.scheme` | Enum | `ServiceAddressProperties` (`@ConfigurationProperties`) | core:mill-service-starter | `ApplicationDescriptorConfiguration` fallback URL resolution | **service-starter** | URL scheme (`http`, `https`, `grpc`) |
| `mill.application.hosts.externals.<name>.host` | String | `ServiceAddressProperties` (`@ConfigurationProperties`) | core:mill-service-starter | `ApplicationDescriptorConfiguration` fallback URL resolution | **service-starter** | Externally reachable host |
| `mill.application.hosts.externals.<name>.port` | Integer | `ServiceAddressProperties` (`@ConfigurationProperties`) | core:mill-service-starter | `ApplicationDescriptorConfiguration` fallback URL resolution | **service-starter** | Externally reachable port |
| `mill.services.ai-nl2data.enable` | Boolean | — (checked by `OnServiceEnabledCondition`) | data:mill-data-autoconfigure | `@ConditionalOnService("ai-nl2data")`: `AIConfiguration`, `JPAConfiguration`, `NlSqlChatServiceImpl`, `NlSqlChatController`, `GlobalExceptionHandler`, `ChatProcessor`, `ValueMappingComponents` | none | |
| `mill.services.data-bot.enable` | Boolean | — (YAML only) | — | **no Java consumer** | none | Ghost key |
| `mill.services.data-bot.prompt-file` | String | — (YAML only) | — | **no Java consumer** | none | Ghost key |
| `mill.services.data-bot.model-name` | String | — (YAML only) | — | **no Java consumer** | none | Ghost key |
| `mill.services.jet-grpc.enable` | Boolean | — (YAML only) | — | **no Java consumer** | none | Ghost key — possibly stale duplicate of `grpc` |
| `mill.services.jet-grpc.port` | Integer | — (YAML only) | — | **no Java consumer** | none | Ghost key |

### mill.ui

Embedded Mill UI (static assets + SPA filter) is gated by `mill.ui.enabled` (`@ConditionalOnProperty`), not `mill.services.*`.

| Key | Type | Declared in | Module | Consumed by | Metadata JSON | Notes |
|-----|------|-------------|--------|-------------|---------------|-------|
| `mill.ui.enabled` | Boolean | `MillUiProperties` | services:mill-ui-service | `MillUiSpaRoutingFilter`, `MillUiWebConfig` | **mill-ui-service** (processor) | Default: `true` |
| `mill.ui.version` | String | `MillUiProperties` | services:mill-ui-service | `MillUiWebConfig.addResourceHandlers()` | **mill-ui-service** | Default: `v2` |
| `mill.ui.app-base-path` | String | `MillUiProperties` | services:mill-ui-service | `MillUiSpaRoutingFilter`, `MillUiWebConfig` | **mill-ui-service** | Default: `/app` |
| `mill.ui.spa-index-path` | String | `MillUiProperties` | services:mill-ui-service | `MillUiSpaRoutingFilter` | **mill-ui-service** | Default: `/app/index.html` |

---

## 2. Metadata JSON Coverage

Processor-generated `spring-configuration-metadata.json` also exists for `MillUiProperties` under `services/mill-ui-service`. Four `additional-spring-configuration-metadata.json` files exist elsewhere:

| File | Module | Keys defined |
|------|--------|-------------|
| `core/mill-security-autoconfigure/src/main/resources/META-INF/additional-spring-configuration-metadata.json` | core:mill-security-autoconfigure | `mill.security.enable`, `mill.security.authentication.oauth2-resource-server.jwt`, `mill.security.authentication.entra-id-token.enable`, `mill.security.authorization.policy.enable`, `mill.security.authorization.policy.actions`, `mill.security.authorization.policy.actions.policy`, `mill.security.authorization.policy.actions.verb`, `mill.security.authorization.policy.actions[0].action`, `mill.security.authorization.policy.actions[0].params` |
| `core/mill-service-starter/src/main/resources/META-INF/additional-spring-configuration-metadata.json` | core:mill-service-starter | `mill.services.meta.enable`, `mill.services.public-base-url` |
| `data/mill-data-grpc-service/src/main/resources/META-INF/additional-spring-configuration-metadata.json` | data:mill-data-grpc-service | `mill.services.grpc.enable`, `mill.services.grpc.port`, `mill.services.grpc.address` |
| `data/mill-data-http-service/src/main/resources/META-INF/additional-spring-configuration-metadata.json` | data:mill-data-http-service | `mill.services.jet-http.enable` |

**Total**: 14 key entries across 4 files.

### Keys missing metadata JSON (no IDE auto-complete)

- All `mill.backend.*` keys (10 keys)
- `mill.security.authentication.basic.enable`, `mill.security.authentication.basic.file-store`
- `mill.security.authorization.policy.selector.granted-authority.remap`
- Legacy `mill.metadata.annotations`, `mill.metadata.relations`, `mill.metadata.file.repository.path` (no processor entry in table above). Greenfield **`mill.metadata.repository.*`** / **`mill.metadata.seed.*`** are declared on **`MetadataProperties`** / **`MetadataSeedProperties`** in **mill-metadata-autoconfigure** (Java `@ConfigurationProperties` → check generated `spring-configuration-metadata.json` in that module after build).
- All `mill.ai.*` keys (6 keys)
- `mill.services.ai-nl2data.enable`

---

## 3. Custom Conditional Annotations

These are annotations implemented **in this codebase** (not from Spring Boot) that gate bean/configuration activation:

| Annotation | Condition class | Defined in module | Checks property | Utility dep |
|------------|----------------|-------------------|-----------------|-------------|
| `@ConditionalOnService("name")` | `OnServiceEnabledCondition` | data:mill-data-autoconfigure | `mill.services.<name>.enable` | `SpringUtils` |
| `@ConditionalOnSecurity` | `OnSecurityEnabledCondition` | core:mill-security-autoconfigure | `mill.security.enable` | `SpringUtils` |
| `@ConditionalOnTestKit` | `OnTestKitCondition` | core:mill-test-kit | `mill.security.*` (any key present) | — |

`SpringUtils` (utility for property-group checks) is defined in `core:mill-security-autoconfigure` at `io.qpointz.mill.utils.SpringUtils`.

### Usage by module

#### @ConditionalOnService

| Service name | Classes annotated | Module |
|-------------|-------------------|--------|
| `"grpc"` | `MillGrpcService`, `GrpcServiceDescriptor`, `GrpcServiceSecurityConfiguration`, `MillGrpcServiceExceptionAdvice` | data:mill-data-grpc-service |
| `"jet-http"` | `AccessServiceController` | data:mill-data-http-service |
| `"meta"` | `ApplicationDescriptorController` | core:mill-service-starter |
| `"ai-nl2data"` | `AIConfiguration`, `JPAConfiguration`, `GlobalExceptionHandler`, `NlSqlChatServiceImpl`, `NlSqlChatController`, `ChatProcessor` | ai:mill-ai-v1-nlsql-chat-service |
| `"ai-nl2data"` | `ValueMappingComponents` | ai:mill-ai-v1-core |

#### @ConditionalOnSecurity

| Classes annotated | Module |
|-------------------|--------|
| `SecurityConfig`, `PolicyActionsConfiguration`, `AuthRoutesSecurityConfiguration`, `AppSecurityConfiguration`, `ServicesSecurityConfiguration`, `SwaggerSecurityConfig`, `ApiSecurityConfiguration`, `EntraIdAuthenticationConfiguration`, `OAuth2AuthenticationConfiguration`, `PasswordAuthenticationConfiguration` | core:mill-security-autoconfigure |
| `ApplicationDescriptor`, `DefaultFilterChainConfiguration` | data:mill-data-autoconfigure |
| `GrpcServiceSecurityConfiguration` | data:mill-data-grpc-service |
| `TestController` | core:mill-test-kit |

#### @ConditionalOnTestKit

**No consumers** — annotation is defined but never used. Dead code.

### Dependency implications

Every module using `@ConditionalOnService` must depend (directly or transitively) on `data:mill-data-autoconfigure`. Every module using `@ConditionalOnSecurity` must depend on `core:mill-security-autoconfigure`.

| Annotation | Providing module | Dependent modules |
|------------|-----------------|-------------------|
| `@ConditionalOnService` | data:mill-data-autoconfigure | data-grpc-service, data-http-service, service-starter, ai-v1-core, ai-v1-nlsql-chat-service |
| `@ConditionalOnSecurity` | core:mill-security-autoconfigure | data-autoconfigure, data-grpc-service, test-kit |

**Note**: `@ConditionalOnService` lives in `data:mill-data-autoconfigure` but is consumed across several lanes (core, ai, service assembly). Embedded Mill UI (`services:mill-ui-service`) uses `@ConditionalOnProperty("mill.ui.enabled")` instead. This cross-cutting annotation may belong in a more neutral shared module.

---

## 4. Issues and Observations

1. **Typo**: `mill.backend.jdbc.multi-shema` in `JdbcCalciteConfiguration` — should be `multi-schema`. Some YAML files already use the corrected spelling.

2. **Inconsistency**: `mill.backend.jdbc.output-schema` appears in several YAML files but is not bound by any `@Value` or `@ConfigurationProperties`. The Java code uses `mill.backend.jdbc.target-schema`. These appear to be two names for the same concept — `output-schema` is likely stale.

3. **Inconsistency**: `mill.security.enabled` (with trailing `d`) in `clients/etc/test-backend-server/application.yml` vs `mill.security.enable` everywhere else. The code checks `enable`.

4. **Duplicate prefix**: `mill.security.authorization.policy` is bound by both `PolicyConfiguration` (data-autoconfigure) and `PolicyActionsConfiguration` (security-autoconfigure) — two `@ConfigurationProperties` classes sharing one prefix across two modules.

5. **Legacy vs greenfield metadata**: `mill.metadata.file.repository.path` (legacy, in data-autoconfigure) supplies the old `MetadataProvider` file repo. The metadata **service** uses **`mill.metadata.repository.*`** and **`mill.metadata.seed.*`** (`mill-metadata-autoconfigure`). The obsolete **`mill.metadata.v2.*`** / **`mill.metadata.storage.*`** prefixes are removed.

6. **Ghost keys**: `mill.services.data-bot.*` (enable, prompt-file, model-name) and `mill.services.jet-grpc.*` (enable, port) appear in YAML configs but have **no Java consumer** in the codebase. Either dead config or not yet implemented.

7. **Missing metadata JSON**: 28+ keys lack `additional-spring-configuration-metadata.json` entries. Only `mill.security.*` and `mill.services.{grpc,jet-http,meta}.*` have IDE hints.

8. **Qualifier placeholder**: `@Qualifier("LOJOKOJ")` in `Nl2SqlConfiguration.vectorStoreDocumentSources()` — likely a placeholder that should be replaced with a meaningful qualifier name.

9. **Dead annotation**: `@ConditionalOnTestKit` is defined in `core:mill-test-kit` but never used anywhere.

10. **Cross-lane annotation**: `@ConditionalOnService` is defined in `data:mill-data-autoconfigure` but used by modules in core, ui, and ai lanes — forcing all of them to depend on the data lane's autoconfigure module.

11. **Type mismatch in metadata JSON**: `mill.services.jet-http.enable` is declared as `java.lang.String` in its metadata JSON but is semantically a Boolean.
