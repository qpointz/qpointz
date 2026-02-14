# Mill Configuration Schema

This document captures every configuration key defined across the repository’s `application.yml` and `application-*.yml` files (collected via `git ls-files '*application*.yml'`).  
Keys are grouped by functional area, and each entry lists the module(s) that introduce them so you know where defaults live and which subprojects exercise a given option.

Legend for the “Sources” column:

- `apps/*` – Mill runtime services packaged in `apps/mill-service`.
- `core/*` – shared starters and test harnesses.
- `services/*` – deployable HTTP/GRPC services.
- `ai/*` – NL→SQL/AI modules.
- `clients/*` – sample backend servers and JDBC driver tests.
- `misc/*` – standalone examples (e.g., OAuth samples).

> All keys are optional unless referenced by a specific module. When adding new configuration properties, update this schema and cite the owning module.

---

## gRPC Layer

| Key | Description | Sources |
| --- | --- | --- |
| `grpc.server.address` | Bind address for Mill’s main gRPC endpoint (delegates to `mill.services.grpc.address` in prod). | `apps/mill-service/src/main/resources/application.yml` |
| `grpc.server.port` | gRPC port (commonly `${mill.services.grpc.port}` in prod, overridable per test). | `apps/mill-service`, `clients/mill-jdbc-driver`, `core/mill-starter-backends`, `services/mill-jet-grpc-service` |
| `grpc.server.in-process-name` | Name for in-process gRPC servers used by tests/JDBC driver. | `clients/mill-jdbc-driver`, `core/mill-starter-backends`, `services/mill-jet-grpc-service` |
| `grpc.server.security.enabled` | Toggle TLS for the embedded gRPC server. | `apps/mill-service/config/default/application-tls.yml`, `clients/etc/test-backend-server`, `misc/sample/mill/mill-oauth` |
| `grpc.server.security.certificate-chain` | Path to TLS certificate chain. | Same as above |
| `grpc.server.security.private-key` | Path to TLS private key. | Same as above |
| `grpc.client.test.address` | Target address for test gRPC clients. | `services/mill-jet-grpc-service/src/test/resources/application-test.yml` |
| `grpc.client.test-service-calcite.address` | Target address for Calcite-backed test service. | `core/mill-starter-backends/src/test/resources/application-test-calcite.yml` |

## Logging

| Key | Description | Sources |
| --- | --- | --- |
| `logging.level.root` | Default log level for Mill runtime/test harnesses. | `apps/mill-service`, `clients`, `core`, `ai`, `services` |
| `logging.level.io.qpointz.mill` | Mill package logging (set to `debug` in AI tests). | `ai/mill-ai-core/src/testIT`, `ai/mill-ai-core/src/testIT/...valuemap` |
| `logging.level.org.apache.calcite` | Verbosity for Calcite planner (used in AI + chat-service IT). | `ai/mill-ai-core/src/testIT`, `ai/mill-ai-nlsql-chat-service/src/testIT` |
| `logging.level.org.springframework.*` | Spring HTTP/Security tuning for UI and service tests. | `services/mill-jet-http-service`, `core/mill-starter-service`, `core/mill-starter-backends`, `ai/mill-ai-core/src/test` |
| `logging.level.com.fasterxml.jackson.databind` | JSON serialization logs for HTTP slice tests. | `services/mill-jet-http-service/src/test` |

## Management & Actuator

| Key | Description | Sources |
| --- | --- | --- |
| `management.endpoints.web.exposure.include` | Exposes actuator endpoints such as `health`, `info`, `prometheus`. | `apps/mill-service/application-moneta-local.yml`, docker samples |

## Mill AI (`mill.ai.*`)

| Key | Description | Sources |
| --- | --- | --- |
| `mill.ai.chat.memory` | Chat memory backend (`in-memory` or `jdbc`). | `apps/mill-service/application-moneta-local.yml`, AI IT configs |
| `mill.ai.nl2sql.enable` | Enables NL→SQL pipeline. | `apps/mill-service/application-moneta-local.yml`, AI IT configs |
| `mill.ai.nl2sql.dialect` | SQL dialect handed to prompt builders (e.g., `H2`). | Same as above |
| `mill.ai.nl2sql.valuemapping[].type/target/sql/cron` | Value-mapper jobs (cron schedule + SQL + sink target). Currently exercised in NL2SQL integration tests. | `ai/mill-ai-core/src/testIT/application-test-moneta-valuemap-it.yml` |

## Mill Backend & Metadata

| Key | Description | Sources |
| --- | --- | --- |
| `mill.backend.provider` | Backend implementation (`jdbc`, `calcite`, etc.). | `apps/mill-service`, `core` starters, JDBC tests |
| `mill.backend.connection.caseSensitive` / `quoting` / `unquotedCasing` | Identifier casing + quoting style. | `apps/mill-service`, `core` starters, AI configs, clients |
| `mill.backend.connection.model` | Calcite model file for virtual schemas. | `apps/mill-service/config/default/application-calcite-sample.yml`, `core/mill-starter-backends` |
| `mill.backend.connection.fun` / `conformance` | Dialect knobs for Oracle-style behaviour. | `apps/mill-service/application-moneta-local.yml` |
| `mill.backend.jdbc.url` / `driver` / `username` / `password` | JDBC connection info for the execution engine. | `apps/mill-service`, `clients/etc/test-backend-server`, `core` tests, `services/mill-jet-http-service` |
| `mill.backend.jdbc.target-schema` | Destination schema when generating SQL. | `apps/mill-service`, AI integration tests |
| `mill.backend.jdbc.output-schema` | Optional schema for storing outputs (used in JDBC sample configs). | `apps/mill-service/config/default/application-jdbc-sample.yml`, `clients/etc/test-backend-server` |
| `mill.metadata.relations` / `mill.metadata.annotations` | Metadata source (e.g., `none`, `v2`). | `apps/mill-service`, AI tests |
| `mill.metadata.v2.storage.type` & `.file.path` | File-based metadata repository settings. | `apps/mill-service/application-moneta-local.yml` & docker samples |
| `mill.metadata.file.repository.path` | Legacy file metadata location (AI tests). | `ai/mill-ai-core/src/test*` |

## Mill Services Toggles (`mill.services.*`)

| Key | Description | Sources |
| --- | --- | --- |
| `mill.services.ai-nl2data.enable` | Enables NL2SQL service. | `apps/mill-service`, AI tests |
| `mill.services.data-bot.enable/model-name/prompt-file` | Controls the LLM-driven data bot feature. | `apps/mill-service` configs, AI tests |
| `mill.services.grinder.enable` | Enables the Grinder UI/service. | `apps/mill-service/application-moneta-local.yml` |
| `mill.services.grpc.enable/address/port` | Toggles in-process gRPC server and network settings. | `apps/mill-service`, `core` backends/tests |
| `mill.services.jet-http.enable` | Enables HTTP gateway. | `apps/mill-service`, `services/mill-jet-http-service` |
| `mill.services.jet-grpc.enable/port` | Enables Jet gRPC bridge. | `apps/mill-service/application-moneta-local.yml` |
| `mill.services.meta.enable` | Turns on metadata service. | `apps/mill-service`, `core` starters |

## Mill Security (`mill.security.*`)

| Key | Description | Sources |
| --- | --- | --- |
| `mill.security.enable` | Master switch for authn/authz. | `apps/mill-service`, `clients/etc/test-backend-server`, `core/mill-security-core`, `services/mill-jet-*`, AI configs |
| `mill.security.authentication.basic.*` | Enables file-backed Basic auth and points to credential store. | `apps/mill-service/config/default/application-auth.yml`, `clients/etc/test-backend-server`, `core` security tests, `services/mill-jet-grpc-service` |
| `mill.security.authentication.oauth2-resource-server.*` | Enables JWT validation and JWK source. | `apps/mill-service/config/test/application-auth.yml`, `clients/etc/test-backend-server`, `core/mill-security-core` |
| `mill.security.authentication.entra-id-token.enable` | Flag for Entra ID token auth (core security tests). | `core/mill-security-core/src/test/resources/application-test-trivial.yml` |
| `mill.security.authorization.policy.*` | Declarative policy engine: selectors, action remaps, allow/deny verbs. | `apps/mill-service/src/main/resources/application.yml` |
| `mill.security.providers[].type/path/issuer-uri` | External identity provider definitions for OAuth sample. | `misc/sample/mill/mill-oauth/config/application.yml` |
| `mill.security.enabled` | Legacy flag used by test backend server (leave true when bridging). | `clients/etc/test-backend-server/application.yml` |

## Spring AI & MCP

| Key | Description | Sources |
| --- | --- | --- |
| `spring.ai.model.*` | Default models for chat/audio/embedding/image/moderation (set to `none` in prod app to avoid accidental calls). | `apps/mill-service/src/main/resources/application.yml`, AI tests |
| `spring.ai.chat.memory.repository.jdbc.schema` | SQL resource executed to bootstrap chat-memory tables. | `apps/mill-service/src/main/resources/application.yml`, `ai/mill-ai-nlsql-chat-service/src/testIT` |
| `spring.ai.chat.memory.repository.jdbc.initialize-schema` | When to auto-run the schema script (`always`, etc.). | `apps/mill-service/application-moneta-local.yml` |
| `spring.ai.openai.api-key` / `chat.model` / `chat.options.model` / `embedding.options.model` | Direct OpenAI model + credential settings. | `apps/mill-service`, `ai/mill-ai-core/src/testIT`, `ai/mill-ai-nlsql-chat-service/src/testIT` |
| `spring.ai.openai.azure.openai.api-key/endpoint/chat.options.deployment-name` | Azure-hosted OpenAI deployments. | `apps/mill-service/src/main/resources/application.yml` |
| `spring.ai.mcp.server.*` | Config for MCP servers embedded in Spring AI. | `apps/mill-service/src/main/resources/application.yml` |
| `spring.ai.openai.mcp.server.*` | Azure/OpenAI-specific MCP bridge. | `apps/mill-service/src/main/resources/application.yml` |
| `spring.mcp.server.*` | Non-AI Spring MCP server toggles (used broadly in apps + AI tests). | `apps/mill-service/application-moneta-local.yml`, `ai` modules |

## Spring Datasource, Hikari, and JPA

| Key | Description | Sources |
| --- | --- | --- |
| `spring.datasource.url/username/password` | Primary JDBC datasource for the service/chat storage. | `apps/mill-service`, docker samples, chat-service IT |
| `spring.datasource.hikari.connection-timeout` | Millisecond timeout when borrowing a connection. | `apps/mill-service/application-moneta-local.yml`, docker samples |
| `spring.datasource.hikari.idle-timeout` | Idle connection eviction period. | Same as above |
| `spring.datasource.hikari.max-lifetime` | Max lifetime before recycling. | Same as above |
| `spring.datasource.hikari.maximum-pool-size` | Maximum pool size. | Same |
| `spring.datasource.hikari.hikari.minimum-idle` | Legacy typo in Moneta config that maps to minimum idle connections. | Same |
| `spring.datasource.hikari.registerMbeans` | Enables JMX exposure for the pool. | Same |
| `spring.jpa.hibernate.ddl-auto` | Auto DDL mode for chat persistence/local samples (`create-drop`). | `apps/mill-service/application-moneta-local.yml`, chat-service IT |

## Spring Core & Profiles

| Key | Description | Sources |
| --- | --- | --- |
| `spring.main.web-application-type` | Forces servlet mode when embedding. | `apps/mill-service/src/main/resources/application.yml`, `core/mill-starter-backends` |
| `spring.main` (profile blocks) | Profile-specific overrides for JDBC/Calcite/auth/cmart scenarios. | `apps/mill-service/src/main/resources/application.yml` |
| `spring.main.web-application-type:servlet` | Inline property from Moneta local config (note the missing space). | `apps/mill-service/application-moneta-local.yml` |
| `spring.config.activate.on-profile` | Multi-document toggles for `local-jdbc`, `local-calcite`, `local-auth`, `local-cmart`. | `apps/mill-service/src/main/resources/application.yml`, `core/mill-starter-service` tests |
| `spring.profiles` | Static profile for Jet HTTP service (`jet-http`). | `services/mill-jet-http-service/src/main/resources/application-jet-http.yml` |

## Spring Datasource (AI / Chat Service)

| Key | Description | Sources |
| --- | --- | --- |
| `spring.datasource.*` (`ai/mill-ai-nlsql-chat-service`) | Chat service integration tests spin H2 via these settings. | `ai/mill-ai-nlsql-chat-service/src/testIT/resources/application-test-moneta-slim-it.yml` |

## Spring MCP Standalone (AI)

| Key | Description | Sources |
| --- | --- | --- |
| `spring.mcp.server.enabled/name/transport.*` | Repeated in AI core + chat service test configs to emulate MCP integration. | `ai/mill-ai-core/src/test*/resources`, `ai/mill-ai-nlsql-chat-service/src/testIT` |

## Service-Specific Configurations

| Key | Description | Sources |
| --- | --- | --- |
| `mill.services.jet-http.enable` | Also toggled in `services/mill-jet-http-service` sample to boot only HTTP stack. | `services/mill-jet-http-service/src/main/resources/application-jet-http.yml`, `services/mill-jet-http-service/src/test/resources/application-test-cmart.yml` |
| `spring.profiles` (`jet-http`) | Ensures the HTTP module only activates under the `jet-http` profile. | `services/mill-jet-http-service/src/main/resources/application-jet-http.yml` |
| `logging.level.*` (HTTP service) | Extra logging for controller tests and integration slices. | `services/mill-jet-http-service/src/test/resources/application-test-cmart.yml` |

## Sample/Test-Only Keys

The following keys appear exclusively in sample or test configurations but are documented for completeness:

| Key | Description | Sources |
| --- | --- | --- |
| `grpc.server.in-process-name` | In-process server for JDBC driver tests (`clients/mill-jdbc-driver`). |
| `mill.services.grpc.enable` (tests) | Enables the mock gRPC server inside starters and security tests. | `core/mill-security-core`, `core/mill-starter-backends`, `core/mill-starter-service`, `services/mill-jet-grpc-service` |
| `mill.security.authentication.basic.file-store` | Pointing to sample passwd files (e.g., `test/datasets/cmart/passwd.yaml`). | `apps/mill-service`, `core` security tests |
| `mill.security.authorization.policy.actions[*]` | Example deny rule for cmart dataset to demonstrate row filtering. | `apps/mill-service/src/main/resources/application.yml` |
| `mill.backend.connection.fun/conformance` | Oracle compatibility knobs in Moneta sample. | `apps/mill-service/application-moneta-local.yml` |
| `mill.ai.nl2sql.valuemapping[*]` | Synthetic configuration used solely in AI value-mapping integration suite. | `ai/mill-ai-core/src/testIT/resources/application-test-moneta-valuemap-it.yml` |

---

## Adding New Configuration Keys

1. Define the property under the appropriate prefix (prefer `mill.*` for Mill-specific features to avoid polluting Spring namespaces).
2. Update the owning module’s `application*.yml` with sensible defaults or documentation comments.
3. Amend this document with:
   - The exact key (dot notation; include array indices if relevant).
   - A short description (purpose, accepted values).
   - The module/file where it is introduced.

Keeping this schema accurate ensures all teams (services, AI, clients) understand the available toggles and environment expectations.
