# Milestones

**Draft release notes.** Treat this file as the **working draft** of **[`releases/RELEASE-x.y.z.md`](releases/)** for the **next** version only: the **`## x.y.z`** block below is pre-release material to be **promoted and polished** into **`RELEASE-x.y.z.md`** when git tag **`vx.y.z`** is cut. It must **not** retain sections for **already shipped** versions — those live only under **`releases/`** (for example **[`releases/RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)** for **`v0.7.0`**). After tagging, **`MILESTONE.md`** is reset to the **following** milestone draft only — see [`RULES.md`](RULES.md) § **Milestone ledger (`MILESTONE.md`)** and § **Release (version) process**.

**Baseline (work since last shipped tag):** **`v0.7.0`** — e.g. `git log v0.7.0..HEAD`.

## 0.8.0

**Target date:** TBD — **not released.** Companion stub: [`releases/RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md) (filled from this draft at tag time). **§ Completed** below = changes merged on `dev` **after** **`v0.7.0`** until **`v0.8.0`** is cut; narrative for **`v0.7.0`** itself is in **[`releases/RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)** only.

### Completed

**Draft body for [`RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md).** **0.7.0** shipped as **[`RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)**; this section accumulates the **0.8.0** delta until release. **§ Completed** = work **since `v0.7.0`**. Use **§ Archived stories** for closure-level summaries + links, **§ Cumulative WI deliveries** for the running WI checklist, and **[`BACKLOG.md`](BACKLOG.md)** for row-level status until **release housekeeping** (see [`RULES.md`](RULES.md) § **Release (version) process**).

#### Archived stories (closure date, newest first)

**Scope:** this subsection lists **only** story archives whose **`docs/workitems/completed/…` tree first landed after git tag `v0.7.0`** (verify with e.g. `git log v0.7.0..HEAD -- docs/workitems/completed/`). Archives dated **2026-03-30** through **2026-04-02** — including **metadata rework**, **metadata/UI**, **flow UI facets**, **typed URNs** — were committed **on or before** `v0.7.0`; they remain under [`completed/`](completed/) for traceability and are summarized in **[`releases/RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)** (see also **[`completed/README.md`](completed/README.md)**). They are **not** duplicated here as **0.8.0** delta.

- **mill-py platform HTTP clients** (`mill-py-metadata-client`, closed **2026-04-24**): [`completed/20260424-mill-py-metadata-client/STORY.md`](completed/20260424-mill-py-metadata-client/STORY.md). **WI-192**–**WI-203** — shared `httpx` helpers and Jet transport refactor; `mill.metadata` / `mill.metadata.aio`, `mill.schema_explorer` async; metadata DTOs, scopes import/export, entities/facets/catalog, integration tests; canonical **GET /export** YAML+JSON and scope filter (server); bulk import + JSON export parse helpers (**WI-203**); design [`docs/design/client/mill-py-platform-http.md`](../design/client/mill-py-platform-http.md). **BACKLOG** **C-23** `done`.

- **Schema capability — platform facet reconciliation** (`schema-capability-metadata`, closed **2026-04-17**): [`completed/20260417-schema-capability-metadata/STORY.md`](completed/20260417-schema-capability-metadata/STORY.md). **WI-187**–**WI-191** — bootstrap relation payload normalization (`RelationPayloadNormalization`, `SchemaFacets`), `DescriptiveFacet` `title` alias, `SchemaCatalogPort` + adapter (`displayName`, `joinSql`, model-root relations), `capabilities/schema.yaml`, Skymill **`SchemaFacetCatalogSkymillCanonicalIT`**, AI facet URNs on **`MetadataUrns`**. Design: [`docs/design/metadata/schema-facet-ai-tool-field-mapping.md`](../design/metadata/schema-facet-ai-tool-field-mapping.md). **BACKLOG** **A-90** `done`.

- **Value mapping — metadata facets, vector lifecycle** (`value-mapping-facets-vector-lifecycle`, closed **2026-04-17**): [`completed/20260417-value-mapping-facets-vector-lifecycle/STORY.md`](completed/20260417-value-mapping-facets-vector-lifecycle/STORY.md). **WI-181** facet types + **`ValueSource`** + **`syncFromSource`**; **WI-184** refresh state + **`STALE`**; **WI-182** startup + scheduled orchestrator; **WI-185** Skymill/Moneta extras seeds; **WI-186** LangChain4j **pgvector** + Mill Service **`pgvector`** / **`ai-pgvector`**. **WI-183** (resolver / capability) remains a **follow-on** in that archive. **BACKLOG** **A-89** `done`.

- **Value mappings — persistence, sync, stack** (`implement-value-mappings`, closed **2026-04-16**): [`completed/20260416-implement-value-mappings/STORY.md`](completed/20260416-implement-value-mappings/STORY.md). **WI-175**–**WI-177** `mill.ai` providers, embedding harness, in-memory vector store; **WI-174** `ValueMappingEmbeddingRepository` + JPA/Flyway; **WI-179** `VectorMappingSynchronizer`; **WI-180** `ValueMappingService` + sync autoconfigure; **WI-178** design/inventory/public alignment; opt-in **`ChromaSkymillDistinctVectorIT`**. **BACKLOG** **A-85**–**A-88** `done`.

- **AI v3 chat — capability dependencies over HTTP** (`ai-v3-chat-capability-dependencies`, closed **2026-04-14**): [`completed/20260414-ai-v3-chat-capability-dependencies/STORY.md`](completed/20260414-ai-v3-chat-capability-dependencies/STORY.md). **WI-167**–**WI-169**, **WI-160**. **BACKLOG** **A-84** `done`.

- **Schema exploration contract, `mill-ai-v3-data`, `SqlValidator`** (`ai-v3-schema-exploration-port`, closed **2026-04-14**): [`completed/20260414-ai-v3-schema-exploration-port/STORY.md`](completed/20260414-ai-v3-schema-exploration-port/STORY.md). **WI-161**–**WI-166**. **BACKLOG** **A-83** `done`.

- **`sql-query` generate-only + `SqlValidator` + CLI** (`ai-sql-generate-capability`, closed **2026-04-13**): [`completed/20260413-ai-sql-generate-capability/STORY.md`](completed/20260413-ai-sql-generate-capability/STORY.md). **WI-156** / **WI-158** / **WI-159** — `SqlQueryCapabilityDependency(validator)` only, `AiV3SqlValidatorAutoConfiguration`, CLI highlights generated SQL, ServiceLoader registration; execution stays host-side. Same technical detail as the **WI-156 / WI-158 / WI-159** bullet under **§ Cumulative WI deliveries** below.

#### Cumulative WI deliveries (toward 0.8.0)

Individual work items and bundles merged on `dev` in support of this milestone (including items that also appear under **Archived stories** above).

- **WI-192–WI-203** — mill-py platform HTTP (`mill.metadata`, `mill.schema_explorer`, aio), metadata export/import alignment, bulk helpers (story **`mill-py-metadata-client`**, archived [`completed/20260424-mill-py-metadata-client/STORY.md`](completed/20260424-mill-py-metadata-client/STORY.md)).

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

- **WI-156 / WI-158 / WI-159** — `sql-query` **generate-only** semantics (story
  **`ai-sql-generate-capability`**, archived
  [`completed/20260413-ai-sql-generate-capability/STORY.md`](completed/20260413-ai-sql-generate-capability/STORY.md)):
  `SqlQueryCapabilityDependency(validator)` only (no in-agent `execute_sql`);
  `SqlValidator` + `AiV3SqlValidatorAutoConfiguration`; `sql-query.yaml` protocols
  **`sql-query.generated-sql`** and **`sql-query.validation`** only; CLI highlights generated SQL;
  `CapabilityProvider` ServiceLoader descriptor
  **`META-INF/services/io.qpointz.mill.ai.core.capability.CapabilityProvider`**
  (execution remains host-side)

- **WI-187–WI-191** — Schema capability — platform facet reconciliation (story
  **`schema-capability-metadata`**, archived
  [`completed/20260417-schema-capability-metadata/STORY.md`](completed/20260417-schema-capability-metadata/STORY.md)):
  `RelationPayloadNormalization` + `SchemaFacets` merge for bootstrap `source`/`target` relation
  seeds; `DescriptiveFacet` JSON `title` alias; `SchemaCatalogPort` / `SchemaFacetCatalogAdapter`
  (`displayName`, `joinSql`, model-root + table relations); `capabilities/schema.yaml`;
  `SchemaFacetCatalogSkymillCanonicalIT`; AI facet type URNs on `MetadataUrns`

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

### In Progress

Items currently being implemented in this milestone.

No active in-progress items currently.

### Planned

Items not yet delivered (still **planned** or **in progress** elsewhere — not part of **§ Archived stories** above).

- WI-171 — Chroma + Skymill vector exploration (mill-ai-v3-data)
  (`docs/workitems/planned/metadata-value-mapping/WI-171-chroma-skymill-vector-exploration.md`)
- WI-172 — Metadata value mapping bridge and parity
  (`docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md`)
- WI-173 — Metadata value mapping API and UI surface
  (`docs/workitems/planned/metadata-value-mapping/WI-173-metadata-value-mapping-api-and-ui.md`)
- WI-034 — Metadata complex type support in structural facets and UI
  (`docs/workitems/planned/metadata-complex-types/WI-034-metadata-complex-type-support.md`)
- WI-082 — `mill-ui` migration to the unified `ai/v3` chat API and `item.*` SSE protocol
  (`docs/workitems/planned/ai-v3/WI-082-mill-ui-unified-ai-chat-integration.md`)
- WI-084 — AI v3 chat service documentation: module responsibilities, REST API, persistence model,
  SSE stream contract, runtime rehydration, and `mill-ui` integration guidance
  (`docs/workitems/planned/ai-v3/WI-084-ai-v3-chat-service-documentation.md`)
- WI-023 — ibis dialect correctness validation and certification
  (`docs/workitems/planned/ibis-dialect-validation/WI-023-ibis-dialect-correctness-validation.md`)
