# Milestones

## 0.7.0

**Target date:** TBD

### Completed

Items delivered in this milestone.

- WI-015 — Core `mill-sql` bootstrap + feature-complete YAML schema
- WI-016 — Migrate `POSTGRES`/`H2`/`CALCITE`/`MYSQL` to new schema
- WI-017 — Kotlin typed dialect model + YAML loader (`core/mill-sql` only)
- WI-018 — `GetDialect` contracts for gRPC/HTTP + handshake support flag
- WI-019 — Server `GetDialect` implementation backed by migrated dialects
- WI-020 — Migrate AI dialect consumer to new typed runtime model
- WI-022 — Fully document SQL dialect schema in design docs
- WI-026 — JDBC full metadata implementation (`DatabaseMetaData` surface)
- WI-021 — Python remote dialect consumption over gRPC/HTTP
- WI-024 — Python SQLAlchemy implementation (MillDialect + compiler + entry points)
- WI-025 — Python ibis initial implementation (BaseBackend + SQL compilation, slice 1)
- WI-067 — `ai/v3` multi-mode protocol execution: `ProtocolDefinition` with `TEXT`,
  `STRUCTURED_FINAL`, `STRUCTURED_STREAM` modes; `ProtocolExecutor` interface;
  `LangChain4jProtocolExecutor` (token streaming, JSON schema response, NDJSON line buffer);
  `PlannerDecision.protocolId`; `AgentEvent` protocol variants (`ProtocolTextDelta`,
  `ProtocolFinal`, `ProtocolStreamEvent`); protocol declarations moved to capability YAML
  manifests (`manifest.allProtocols`); `LangChain4jAgent` (renamed from `OpenAiHelloWorldAgent`,
  profile as constructor param); unit tests for all three modes via fake `StreamingChatModel`

- WI-085 — Metadata service API cleanup: service layer refactored into `mill-metadata-service`
  and `mill-metadata-autoconfigure`; `MetadataService` and `MetadataScopeService` registered as
  autoconfiguration `@Bean`s; `MetadataChangeObserver` chain with `MetadataChangeObserverDelegate`
  marker interface eliminating circular injection; `NoOpMetadataRepository` and
  `NoOpMetadataScopeRepository` Kotlin object singletons as no-config fallbacks; component-scan
  pattern aligned with `mill-data-http-service` (controllers via `@RestController`, services via
  autoconfiguration); `@AutoConfigureAfter` ordering on `MetadataEntityServiceAutoConfiguration`
  and `MetadataImportExportAutoConfiguration`; `mill-metadata-persistence` added as `runtimeOnly`
  to `mill-service`

- WI-086 — Metadata REST controller redesign: four controllers under `/api/v1/metadata/**` —
  `MetadataEntityController` (read-only entity + facet endpoints), `MetadataFacetController`
  (facet type catalog CRUD), `MetadataScopeController` (scope lifecycle), and
  `MetadataImportExportController` (YAML bulk import/export); `MetadataUrns` utility with URN
  constants and `normaliseFacetTypePath`/`normaliseScopePath` helpers; full URN keys in storage,
  prefixed slugs in URL path variables; `MetadataExceptionHandler` `@RestControllerAdvice`;
  startup import runner (historically `mill.metadata.import-on-startup`; **current** keys: **`mill.metadata.seed.resources`**) supporting `classpath:` and `file:`
  resource URLs; all production code carries OpenAPI and KDoc annotations

- WI-087 — Metadata relational JPA persistence: new `metadata/mill-metadata-persistence` module;
  Flyway migration `V4__metadata.sql` with six tables (`metadata_scope`, `metadata_entity`,
  `metadata_facet_scope`, `metadata_facet_type`, `metadata_promotion`, `metadata_operation_audit`);
  six JPA entities and Spring Data repositories; three adapters — `JpaMetadataRepository`,
  `JpaFacetTypeRepository`, `JpaMetadataScopeRepository`; `JpaMetadataChangeObserver` for async
  audit persistence; `MetadataJpaPersistenceAutoConfiguration` activated by
  **`mill.metadata.repository.type=jpa`** (historical text below used `mill.metadata.storage.type=jpa`); H2 integration test suite with skymill dataset; JPA
  persistence wired into `skymill` and `moneta` application profiles. **Note (metadata rework):** greenfield DDL and table names evolved per **SPEC §8** (`metadata_audit`, `metadata_seed`, no promotion table); see [`docs/design/metadata/mill-metadata-domain-model.md`](../design/metadata/mill-metadata-domain-model.md).

- WI-089 — Metadata scopes and context composition: `MetadataScope` domain type;
  `MetadataScopeRepository` interface with `NoOpMetadataScopeRepository` fallback; full JPA adapter
  `JpaMetadataScopeRepository`; `MetadataScopeService` with global-scope protection on delete;
  `MetadataContext.parse()` accepting comma-separated scope slugs; facet resolution by ordered
  scope list (last-wins merge); `MetadataScopeController` REST endpoints (`GET/POST/DELETE
  /api/v1/metadata/scopes`); `MetadataScopeDto` and OpenAPI annotations

- WI-092 — `mill-ui` model view: real backend binding: `MetadataProvider` wired to live
  `MetadataApi` and `SchemaExplorerApi` clients; inline chat disabled (`inlineChatEnabled: false`);
  tree loading with 10 s timeout; entity loading by ID and by schema/table/attribute location;
  concepts loaded from backend; graceful empty-state when schema explorer backend is absent

- WI-093a — Physical schema explorer service: `mill-data-schema-service` with `/api/v1/schema/**`
  (context bootstrap, schema list, table/column detail, tree-oriented loading for the Model view)

- WI-093b — Metadata autoconfigure split and Model view integration: lazy per-table column loading,
  `context` query parameter for facet resolution, wiring to split `mill-metadata-autoconfigure` vs
  persistence modules

- WI-085 — gRPC server reimplementation and Skymill parity tests: raw grpc-java server as the only
  active gRPC transport (`services/mill-data-grpc-service/`); shared Skymill SQL query-case set;
  server `testIT` executing the shared query set against Skymill; JDBC driver `testIT` reusing the
  same query set for result/label parity; legacy net.devh server retained under `misc/` for reference

Completed WI markdown files are intentionally removed after delivery; this milestone list is the
retained canonical record of completed items.

**Metadata — Schema Explorer (`metadata-edit-and-explorer`, closed March 2026):** Delivered the
physical schema explorer REST surface (`data/mill-data-schema-service`, `/api/v1/schema/**` including
context bootstrapping and tree-oriented loading), metadata autoconfigure split, and `mill-ui` Data
Model wiring (lazy columns, context query param, facet resolution). Deferred to backlog: interactive
scope picker beyond `global`, strict scope authorization rules for writes, and runtime performance
hardening of schema list/tree endpoints (see `BACKLOG.md`).

**Metadata — Edit, Facet Registry, and UI Service (`metadata-edit-and-promotion-follow-up`, closed
March 2026):** Delivered WI-094–WI-099 and WI-090/WI-098 — facet type manifests and registry
(`inMemory` / `local`), admin facet-type management UI, platform descriptor migration, JPA
row-per-facet storage (`metadata_facet`, Flyway V8+), surrogate keys (V9), stable **`facet_uid`** per
row (V10) exposed on `GET .../entities/{id}/facets`, **MULTIPLE** delete requiring `uid` when more
than one instance exists, `DELETE .../facet-instances/{facetUid}`, `mill-ui` facet editor (schema-driven
forms, expert JSON), and **`services/mill-ui-service`** with `mill.ui.*` configuration. **JPA save**
replaces all facet rows for an entity from the domain snapshot so removals persist (fixes stale facets
  after delete). **WI-091** (metadata promotion workflow) remains **deferred** — see `BACKLOG.md`.

**Mill UI (`mill-ui-fixes`, closed March 2026):** **WI-105** — ESLint 9 flat config for `ui/mill-ui` (`npm run lint` clean; `react-refresh` relaxed for context modules; `_`-prefixed unused bindings). **WI-106** — Vitest `afterEach` `act` + timer flush in `src/test/setup.ts` to reduce async provider noise. **WI-108** — design pack under `docs/design/ui/mill-ui/` with stub `ui/mill-ui/docs/README.md` pointing to the canonical location. **WI-107** — shared explorer toolbar row (`layoutChrome`, `ExplorerSplitLayout`, `ViewPaneHeader`) on Model / Knowledge / Analysis; metadata scope `Select` wired to schema context and metadata scope APIs. **WI-109** — MULTIPLE-cardinality facet rendering in `EntityDetails` (per-instance cards, `{ relations: [...] }` envelope, sole `{}` counts as one instance). **WI-110** — pure **`facetPayloadUtils.ts`** extracted from `EntityDetails` with focused unit tests.

**Metadata — Greenfield rework (`metadata-rework`, closed March 2026):** **WI-119–WI-128** — URN-based **entity** identity (`urn:mill/...`, opaque to `mill-metadata-core`); **`FacetInstance`** row model with **`merge_action`** on **`metadata_entity_facet`**; effective-view merge in core (**`MetadataReader`** / **`MetadataView`**), not in JPA adapters alone; squashed Flyway **`V4__metadata_greenfield.sql`** (legacy metadata migrations V4–V10 removed per story); **`metadata_audit`**, **`metadata_seed`** ledger, ordered **`mill.metadata.seed.*`** startup seeds; **`mill.metadata.repository.*`** replaces removed **`mill.metadata.storage.*`**; REST/OpenAPI, canonical YAML import/export, and **`mill-ui`** URN paths; **WI-127** design sync ([`mill-metadata-domain-model.md`](../design/metadata/mill-metadata-domain-model.md), configuration inventories); **WI-128** public MkDocs metadata section. Normative spec and WI checklist (archived): [`completed/20260330-metadata-rework/STORY.md`](completed/20260330-metadata-rework/STORY.md), [`completed/20260330-metadata-rework/SPEC.md`](completed/20260330-metadata-rework/SPEC.md).

**Mill UI — facet types & Model view (branch `feat/metadata-rework-final`, merged toward dev after March 2026):** Facet **MULTIPLE** payload coercion + per-instance cards; **known stereotypes** documentation ([`mill-ui-facet-stereotypes.md`](../design/metadata/mill-ui-facet-stereotypes.md), public [`facet-stereotypes.md`](../public/src/metadata/facet-stereotypes.md)); **Facet types** admin list — category / applicable-to (**empty `applicableTo` = any** included when filtering) / enabled-only row, equal-width filters, **delete confirmation** modal; expert JSON/YAML editor **save** parses current buffer (YAML safe); hyperlink object **Title** + **URL** read layout; single-instance MULTIPLE caption hygiene. **BACKLOG** **M-30** marks stereotype docs done.

### In Progress

Items currently being implemented in this milestone.

### Planned

Items targeted next after in-progress work is completed.

- WI-023 — ibis dialect correctness validation and certification
  (`docs/workitems/ibis-dialect-validation/WI-023-ibis-dialect-correctness-validation.md`)

## 0.8.0

**Target date:** TBD

### Completed

Items delivered in this milestone.

- WI-038 — `ai/v3` capability model and descriptor format: `CapabilityDescriptor`,
  `Capability`, `CapabilityProvider`, `CapabilityRegistry`, `CapabilityManifest`, and
  manifest-driven prompts/tools/protocol declaration as the core composition surface for
  `ai/v3`
- WI-062 — Schema data aggregation boundary: `SchemaFacetService`, `*WithFacets` domain model,
  `SchemaFacets` (map-backed, typed facet holder), `SchemaFacetResult` with unbound metadata,
  `SchemaFacetAutoConfiguration`; unit + skymill integration tests in `data/mill-data-schema-core`
- WI-068 — `ai/v3` schema metadata authoring (stage 1): `schema-authoring` capability with
  `capture_description` and `capture_relation` CAPTURE-kind tools; `request_clarification`
  QUERY tool (planner-intercepted, language-safe); `SchemaAuthoringCapabilityProvider` and
  `SchemaAuthoringAgentProfile` composing `conversation + schema + schema-authoring`;
  `ToolKind.CAPTURE` enum on `ToolDefinition`; `buildObserver(tools)` routing to ANSWER on
  capture completion; `PlannerDecision.task` / `.subtype` / `authorMetadata()` factory;
  `schema-authoring.yaml` manifest (prompts: intent/request/batch, tools, `STRUCTURED_FINAL`
  protocol); `serializedPayload` facet-aligned (`facetType: "descriptive"` / `"relation"` with
  `relations: [...]` array); `SchemaExplorationAgent` updated to `SchemaAuthoringAgentProfile`
  with multi-CAPTURE batch support; `conversation.language` prompt for automatic language
  mirroring; `v3-authoring-protocol.md` design doc capturing deferred three-layer protocol gap
- WI-069 — `ai/v3` `sql-dialect` capability: `SqlDialectCapabilityProvider`,
  `SqlDialectCapabilityDependency(dialectSpec)`, `SqlDialectCapability` (manifest-wired),
  `SqlDialectToolHandlers` (pure, no framework coupling); five read-only tools —
  `get_sql_dialect_conventions` (overview + identifier + literal rules + function categories),
  `get_sql_paging_rules`, `get_sql_join_rules`, `get_sql_functions(category)`,
  `get_sql_function_info(name)`; `sql-dialect.yaml` manifest with directive system prompt
  requiring identifier quoting and literal rules to be applied in generated SQL;
  `SqlDialectCapabilityProvider` registered via `ServiceLoader`; `SchemaAuthoringAgentProfile`
  extended to include `sql-dialect`; `SchemaExplorationAgent` accepts `dialectSpec: SqlDialectSpec`
  and injects it as capability dependency; CLI wires Calcite dialect via
  `DialectRegistry.fromClasspathDefaults().requireDialect("calcite")`
- WI-070 — `ai/v3` `sql-query` capability: `SqlQueryCapabilityProvider`,
  `SqlQueryCapabilityDependency(validator, executor)`, `SqlQueryCapability` (manifest-wired),
  `SqlQueryToolHandlers` (pure stateless); two tools — `validate_sql` (structured pass/fail +
  free-text validator message wrapped in `SqlValidationArtifact`) and `execute_sql` (result
  metadata only, no row payloads, wrapped in `SqlResultReferenceArtifact`);
  `SqlValidationService` / `SqlExecutionService` `fun interface` boundaries;
  `MockSqlValidationService` and `MockSqlExecutionService` for local testing;
  `sql-query.yaml` manifest with three `STRUCTURED_FINAL` protocols
  (`sql-query.generated-sql`, `sql-query.validation`, `sql-query.result-ref`);
  `SqlQueryCapabilityProvider` registered via `ServiceLoader`;
  `SchemaAuthoringAgentProfile` already included `sql-query`; `SchemaExplorationAgent`
  accepts and injects `SqlQueryCapabilityDependency`; CLI wires mock services via
  `MockSqlValidationService` / `MockSqlExecutionService`; fixed pre-existing
  `QSynthYamlSchemaProvider` compile error (`NULLABILITY_UNSPECIFIED` →
  `NOT_SPECIFIED_NULL`); handler and capability wiring unit tests

- WI-071 — `ai/v3` `value-mapping` capability: `ValueMappingResolver` interface,
  `MappedAttribute` and `ValueResolution` data classes, `ValueMappingCapabilityDependency`,
  `ValueMappingCapabilityProvider`, `ValueMappingCapability` (manifest-wired),
  `ValueMappingToolHandlers` (pure stateless); two tools — `get_value_mapping_attributes(table)`
  (returns all attributes with `mapped` flag for a chosen table) and
  `get_value_mapping(table, attribute, values[])` (resolves user-facing phrases to canonical
  database values, `mappedValue: null` for unresolved terms); `value-mapping.yaml` manifest with
  planner system prompt enforcing lookup-before-literal rule and `value-mapping.result`
  `STRUCTURED_FINAL` protocol; `MockValueMappingResolver` for local wiring;
  `ValueMappingCapabilityProvider` registered via `ServiceLoader`;
  `SchemaAuthoringAgentProfile` extended to include `value-mapping`;
  `SchemaExplorationAgent` accepts and injects `ValueMappingResolver` dependency;
  CLI wires `MockValueMappingResolver`; handler and capability registry unit tests

- WI-072 — `ai/v3` in-memory conversation continuity: `ConversationSession`,
  `ConversationMessage`, `MessageRole` in `mill-ai-v3-core` (framework-free);
  `SchemaExplorationAgent.run()` extended to accept `ConversationSession` — replays prior
  `USER`/`ASSISTANT` pairs into LangChain4j messages list each turn, appends completed turn
  back to session; CLI creates one session per REPL lifetime and passes it across turns;
  `/clear` command resets session history; `v3-conversation-persistence.md` design doc
  capturing two-track persistence model (LLM chat memory vs UX conversation record) and
  recommended future work item split

- WI-073a — central persistence module bootstrap: `persistence/` project group with
  `mill-persistence` and `mill-persistence-autoconfigure`; baseline Flyway migration
  `V1__baseline.sql`; Spring Boot autoconfiguration import and `MillPersistenceProperties`;
  JPA entity/repository proof-of-shape adapter via `SchemaInfoEntity` and
  `SchemaInfoRepository`; persistence-layer test application and repository test proving the
  shared module wiring

- WI-073 — `ai/v3` Lane 3 chat-memory persistence: `ConversationMemory`,
  `MemoryProjectionInput`, `ChatMemoryStore`, and `LlmMemoryStrategy` contracts in
  `mill-ai-v3`; `InMemoryChatMemoryStore` and bounded memory projection wired into
  `LangChain4jAgent` and `SchemaExplorationAgent`; memory persisted by `conversationId`
  independently from durable transcript state

- WI-074 — `ai/v3` routed-event, transcript, artifact, and telemetry persistence:
  `AgentEventRouter`, `RoutedAgentEvent`, `EventRoutingPolicy`, `DefaultEventRoutingPolicy`,
  `AgentEventPublisher`, `ConversationStore`, `ArtifactStore`, `RunEventStore`,
  `ActiveArtifactPointerStore`, `RunTelemetryAccumulator`, and `AgentPersistenceContext`;
  in-memory persistence adapters; transcript/artifact linking for multi-artifact assistant
  turns; canonical artifact-bearing `tool.result` promotion for SQL artifacts; profile-owned
  artifact pointer overrides; CLI/runtime wiring through `LangChain4jAgent` and
  `SchemaExplorationAgent`

- WI-075 — `ai/v3` artifact observer seam (phase 1): `ArtifactObserver`,
  `ArtifactIndexingRequest`, `NoOpArtifactObserver`, post-persist observer invocation from
  `StandardPersistenceProjector`, default observer wiring in `AgentPersistenceContext`, and
  printed/logged indexing requests for persisted artifacts. Real relation indexing was split
  into follow-up backlog items `PS-4a` through `PS-4f`.

- WI-076 — `ai/v3` module consolidation and agent simplification: merged
  `mill-ai-v3-core`, `mill-ai-v3-langchain4j`, and `mill-ai-v3-capabilities` into a single
  `mill-ai-v3` module; replaced `ToolDefinition`/`ToolSchema`/`ToolSchemaType`/`ToolSchemaField`
  with LangChain4j `ToolSpecification` and JSON schema builder types; removed custom two-pass
  planner (`planWithModel`, `planToolArguments`, `AgentExecutor`, `Planner`, `Observer`,
  `PlannerDecision`, `Observation`); agent now uses native LangChain4j tool loop with
  `ToolExecutionResultMessage`; `CapabilityManifest.tool()` now produces `ToolBinding`
  (wrapping `ToolSpecification` + `ToolHandler` + `ToolKind`) directly; `ProtocolDefinition`
  and `ProtocolEventDefinition` updated to use `JsonObjectSchema` directly;
  `mill-ai-v3-cli` and `mill-ai-v3-test` updated to depend on merged module

- WI-078 — `ai/v3` chat metadata persistence foundation: first-class `ChatMetadata` and
  `ChatUpdate` model; `ChatRegistry` port separated from transcript persistence; in-memory and
  JPA-backed chat metadata adapters in `ai/mill-ai-v3-persistence`; centralized Flyway
  migration for `ai_chat_metadata` in `mill-persistence`; contextual uniqueness and favourite /
  rename support for persisted chats

- WI-081 — `ai/v3` chat runtime rehydration and context binding: persisted `profileId` made
  authoritative for chat reopen/resume; durable context binding reused on reload; runtime
  rehydration helper and profile registry introduced; contextual chat singleton semantics aligned
  to `(userId, contextType, contextId)`; compile-time default registry accepted as the initial
  baseline with a deferred dynamic-registry follow-up

- WI-080 — `ai/v3` presentation-level chat SSE model: `ChatSseEvent` envelope with stable
  `eventId`, `chatId`, `itemId`, `sequence`, `type`, and `timestamp`; public stream types
  `item.created`, `item.part.updated`, `item.completed`, and `item.failed`; explicit
  `code`/`reason` error payloads and nullable `item.completed.content` to avoid double-rendering
  after deltas

- WI-079 — unified `ai/v3` chat service module and API: new `ai/mill-ai-v3-service` module;
  thin `AiChatController` with OpenAPI annotations and `ai-chat` tag; `ChatService`
  orchestration boundary with `UnifiedChatService` implementation; unified `/api/v1/ai/chats`
  lifecycle and SSE endpoints for general and contextual chats; controller tests with mocked
  service and service-boundary tests for chat orchestration

- WI-083 — repository-wide REST exception/status-handling pattern: platform design baseline for
  thin controllers, semantic `MillStatusException` usage, centralized HTTP advice, and standard
  REST error payload direction; adopted as the reference pattern for `mill-ai-v3-service`

- SEC-1/1a/3a/3b/3c/3d/3e — User authentication and profile management (STORY-USER-AUTH):
  persistent JPA identity model (`users`, `user_credentials`, `user_identities`, `groups`,
  `group_memberships`, `user_profiles`, `auth_events`); `validated`/`locked` account login
  gates with `lockDate`/`lockReason`; `JpaUserIdentityResolutionService` and `JpaUserRepo`
  (provider/subject → canonical userId for all auth methods); `JpaAuthAuditService` writing
  to `auth_events` on every login/logout/register/profile-update; `mill-security-auth-service`
  REST endpoints (`POST /auth/public/login`, `POST /auth/public/register`, `POST /auth/logout`,
  `GET /auth/me`, `PATCH /auth/profile`); `mill-security-autoconfigure` module extracted from
  `mill-service-security`;   `mill-ui` fully wired: real `AuthContext`, `authService.ts`,
  `RequireAuth`, `LoginPage`/`RegisterPage`, profile editing, feature flag defaults
  (registration on, social providers off); `secure` dev profile (H2 + Flyway + default
  admin/admin seed); design: `docs/design/security/user-identity-jpa-implementation.md`

**Metadata — Multi-origin facets and UI cleanup (`metadata-and-ui-improve-and-clean`, closed April 2026):** **`MetadataSource`**, **`FacetOrigin`**, unified read **`FacetInstance`** with **`originId`**; **`RepositoryMetadataSource`**; **`LogicalLayoutMetadataSource`** in **`mill-data-metadata`** (**`MetadataOriginIds.LOGICAL_LAYOUT`**); new **`mill-data-metadata`** module (**`CatalogPath`**, **`SchemaModelRoot`**, inferred sources) with autoconfigure in **`mill-data-autoconfigure`** (**`LogicalLayoutMetadataSourceAutoConfiguration`**, **`SchemaFacetServiceAutoConfiguration`**, **`MetadataEntityUrnCodecAutoConfiguration`**); **`FacetInstanceReadMerge`** + **`DefaultFacetService.resolve`**; REST **`?scope=`** / **`?origin=`** with **`FacetInstanceDto`**; mutation guards (WI-135); **`model`** root (WI-137); read-path merge (WI-133); resolved read API + OpenAPI (WI-134); UI full constellation (WI-136); contracts + **`RepositoryMetadataSource`** (WI-132); **`EntityRepository`** = **`EntityReadSide`** + **`EntityWriteSide`**, **`FacetRepository`** = **`FacetReadSide`** + **`FacetWriteSide`** for persistence seams; facet class demotion + **`FacetPayloadUtils`** (WI-140); dead-code sweep (WI-130); design + public docs incl. multi-source user guide (WI-141). **WI-139** (flow/physical inferred source) **deferred**. Normative checklist and spec: [`completed/20260401-metadata-and-ui-improve-and-clean/STORY.md`](completed/20260401-metadata-and-ui-improve-and-clean/STORY.md). Backlog **M-31** done.

**Typed entity URNs (`typed-entity-urns`, closed 2026-04-02):** **WI-142** typed URN codec
(`urn:mill/model/<class>:<id>` for schema, table, attribute, model root, concept); **WI-143**
canonical datasets and persistence/read paths aligned to typed entity URNs; **WI-145** design +
public doc updates (`metadata-urn-platform`, canonical YAML spec, synthetic writer handoff,
`mill-metadata-domain-model`, `mill-ui` / operator docs). **WI-144** (drop redundant
`MetadataEntity.kind` / `entity_kind`) **deferred** to active story
[`eliminate-entity-kind/STORY.md`](eliminate-entity-kind/STORY.md). Archive:
[`completed/20260402-typed-entity-urns/STORY.md`](completed/20260402-typed-entity-urns/STORY.md).

### In Progress

Items currently being implemented in this milestone.

No active in-progress items currently.

### Planned

Items targeted next after 0.7.0 closure and backlog triage.

- WI-027 — Metadata value mapping bridge and parity
  (`docs/workitems/metadata-value-mapping/WI-027-metadata-value-mapping-bridge.md`)
- WI-028 — Metadata value mapping API and UI surface
  (`docs/workitems/metadata-value-mapping/WI-028-metadata-value-mapping-api-and-ui.md`)
- WI-085 — Metadata service API cleanup and error handling
  (`docs/workitems/metadata-persistence-and-editing/WI-085-metadata-service-cleanup.md`)
- WI-086 — Metadata REST controller redesign
  (`docs/workitems/metadata-persistence-and-editing/WI-086-metadata-rest-controller-redesign.md`)
- WI-087 — Metadata relational persistence and repository transition
  (`docs/workitems/metadata-persistence-and-editing/WI-087-metadata-relational-persistence.md`)
- WI-089 — Metadata scopes and context composition
  (`docs/workitems/metadata-persistence-and-editing/WI-089-metadata-scopes-and-contexts.md`)
- WI-092 — `mill-ui` model view: real backend binding and inline chat disable
  (`docs/workitems/metadata-persistence-and-editing/WI-092-mill-ui-model-view-backend-binding.md`)
- WI-034 — Metadata complex type support in structural facets and UI
  (`docs/workitems/metadata-complex-types/WI-034-metadata-complex-type-support.md`)
- WI-082 — `mill-ui` migration to the unified `ai/v3` chat API and `item.*` SSE protocol
  (`docs/workitems/ai-v3/WI-082-mill-ui-unified-ai-chat-integration.md`)
- WI-084 — AI v3 chat service documentation: module responsibilities, REST API, persistence model,
  SSE stream contract, runtime rehydration, and `mill-ui` integration guidance
  (`docs/workitems/WI-084-ai-v3-chat-service-documentation.md`)
