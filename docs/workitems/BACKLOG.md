M# Backlog

Consolidated tracking list of planned work across all categories. Items are
milestone-selectable deliverables extracted from design documents and work items.

**Legend:**
- **Status**: `backlog` | `planned` | `in-progress` | `done` | `cancelled`
- **Type**: ✨ feature | 💡 improvement | 🐛 fix | 🔧 refactoring | 🧪 test | 📝 docs
- **Source**: design document (relative to `docs/design/`) or work item (`docs/workitems/`)

---

## data — Data Layer

| #   | Item                                                                                    | Type      | Status  | Source                       |
| --- | --------------------------------------------------------------------------------------- | --------- | ------- | ---------------------------- |
| D-1 | Add JSON logical type (Phase A): proto, Java core, backends, clients                    | ✨ feature | backlog | data/complex-type-support.md |
| D-2 | Add LIST native vector type (Phase B): proto, ListVector, producers, readers            | ✨ feature | backlog | data/complex-type-support.md |
| D-3 | Add MAP native vector type (Phase B): proto, MapVector, single-segment PathSegment      | ✨ feature | backlog | data/complex-type-support.md |
| D-4 | Add OBJECT native vector type (Phase B): MapVector with multi-segment PathSegment paths | ✨ feature | backlog | data/complex-type-support.md |
| D-5 | Implement PathSegment reconstruction and flattening algorithms (Java + Python)          | ✨ feature | backlog | data/complex-type-support.md |
| D-6 | Add JSON/LIST/MAP/OBJECT to all type mapping tables in mill-type-system reference       | 📝 docs   | backlog | data/mill-type-system.md     |
| D-8 | Implement server GetDialect RPC and handshake supports_dialect flag                     | ✨ feature | done    | `MILESTONE.md` (WI-018/WI-019 completed) |

---

## ai — AI and NL-to-SQL

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| A-1 | Implement Step-Back UX backend: user-message field, tone guidance, clarification context | ✨ feature | backlog | ai/sb-ux-implementation-plan.md |
| A-2 | Implement Step-Back UX frontend: ClarificationMessage, AssistantMessage, natural-language bubbles | ✨ feature | backlog | ai/sb-ux-implementation-plan.md |
| A-3 | Make StepBackCard optional/debug-only, hidden for simple queries | 💡 improvement | backlog | ai/sb-ux-implementation-plan.md |
| A-4 | Display errors and uncertainty as natural language, not error alerts | 💡 improvement | backlog | ai/sb-ux-implementation-plan.md |
| A-5 | Add Step-Back UX end-to-end tests (clarification, simple query, tone, language mirroring) | 🧪 test | backlog | ai/sb-ux-implementation-plan.md |
| A-6 | Implement scenario detection and multi-step scenario planning layer | ✨ feature | backlog | ai/sb-scenarious.md |
| A-7 | Implement multi-step execution engine (FSM) for scenario steps | ✨ feature | backlog | ai/sb-scenarious.md |
| A-8 | Implement post-processing for comparison, correlation, and trend scenarios | ✨ feature | backlog | ai/sb-scenarious.md |
| A-9 | Create ChatApplicationResponse sealed interface and refactor Reasoner to return it | 🔧 refactoring | backlog | ai/sb-refactor-app-response.md |
| A-10 | Move intent mapping from ChatApplication into Reasoner (with IntentSpecs) | 🔧 refactoring | backlog | ai/sb-refactor-app-response.md |
| A-11 | Wire JSON/HTTP and gRPC/proto mappings for step-back payloads | ✨ feature | backlog | ai/sb-reasoning.md |
| A-12 | Gate SQL generation on successful verification and resolved clarifications | 🐛 fix | backlog | ai/sb-reasoning.md |
| A-13 | Normalize metadata-gaps to enrich-model structures and optionally enqueue for enrichment | ✨ feature | backlog | ai/sb-reasoning.md |
| A-14 | Add metrics for ambiguity frequency and clarification rates | 💡 improvement | backlog | ai/sb-reasoning.md |
| A-15 | Log step-back outputs with PII masking; add integration tests for failed verification | 💡 improvement | backlog | ai/sb-reasoning.md |
| A-16 | Implement Clarification Interpretation as separate LLM step (user answer -> structured values) | ✨ feature | backlog | ai/sb-interaction-model.md |
| A-17 | Implement Step-Back Resume LLM step (merge answers, READY vs WAITING state) | ✨ feature | backlog | ai/sb-interaction-model.md |
| A-18 | Enforce reasoning-id in UI<->backend contract for pause/continue/cancel | ✨ feature | backlog | ai/sb-interaction-model.md |
| A-19 | Implement reference conversation format and full-snapshot regression testing | ✨ feature | backlog | ai/regression-snapshotting.md |
| A-20 | Add regression comparison between versions (token/memory/cost/latency) | ✨ feature | backlog | ai/regression-snapshotting.md |
| A-21 | Implement Capability Protocol (streaming events: begin/continuation/end) | ✨ feature | backlog | ai/capabilities_design.md |
| A-22 | Implement Orchestrator, Chat Profiles, and task-class-to-Intent mapping | ✨ feature | backlog | ai/capabilities_design.md |
| A-23 | Implement Reasoner Descriptions per Capability and dynamic prompt construction | ✨ feature | backlog | ai/capabilities_design.md |
| A-24 | Create `ai/v3` module skeleton for side-by-side Kotlin runtime/capabilities/LangChain4j/test layout | ✨ feature | done | `design/agentic/v3-foundation-decisions.md` |
| A-25 | Define Hello World / platform-validation milestone for `ai/v3` | 📝 docs | done | `design/agentic/v3-foundation-decisions.md` |
| A-26 | Define `ai/v3` core domain vocabulary (capability, profile, protocol, run state, artifact, planner step) | 📝 docs | done | `design/agentic/v3-runtime-roles.md` |
| A-27 | Define `ai/v3` capability model and descriptor format | ✨ feature | done | `MILESTONE.md` (WI-038 completed) |
| A-28 | Implement framework-free dynamic capability discovery for `ai/v3` | ✨ feature | done | `design/agentic/v3-foundation-decisions.md` |
| A-29 | Define MCP-aligned exposure plan for `ai/v3` capabilities | 📝 docs | done | `design/agentic/v3-foundation-decisions.md` |
| A-30 | Identify first-class externalizable capability resources (tools, prompts, protocols, descriptors, examples) | 📝 docs | done | `design/agentic/v3-foundation-decisions.md` |
| A-31 | Define descriptor model for externally exposed capability assets/resources | ✨ feature | planned | `WI-042-ai-v3-external-capability-asset-descriptors.md` |
| A-32 | Define `ai/v3` protocol model and shared streaming event envelope | ✨ feature | done | `design/agentic/v3-capability-manifest.md`, `design/agentic/v3-runtime-roles.md` |
| A-33 | Define `ai/v3` agent profile model and context-based profile resolution | ✨ feature | done | `design/agentic/v3-runtime-roles.md` |
| A-34 | Define `ai/v3` planner and execution-loop contracts | ✨ feature | done | `design/agentic/v3-runtime-roles.md` |
| A-35 | Define `ai/v3` tool contract and tool-execution boundary | ✨ feature | done | `design/agentic/v3-capability-manifest.md` |
| A-36 | Define `ai/v3` run-state model with ephemeral vs durable workflow state | ✨ feature | done | `design/agentic/v3-runtime-roles.md` |
| A-37 | Add dedicated LangChain4j adapter layer to `ai/v3` | ✨ feature | done | `design/agentic/v3-runtime-roles.md` |
| A-38 | Define `ai/v3` validation approach with scenario and `testIT` coverage | 🧪 test | done | `design/agentic/v3-validation-harness.md` |
| A-39 | Define minimal validation capability set for Hello World agent | ✨ feature | done | `design/agentic/v3-foundation-decisions.md` |
| A-40 | Define Hello World / platform-validation agent profile | ✨ feature | done | `design/agentic/v3-runtime-roles.md` |
| A-41 | Define bounded Hello World workflow using trivial/no-op tools | ✨ feature | done | `design/agentic/v3-runtime-roles.md` |
| A-42 | Define Hello World streaming sequence with protocol-defined payloads | ✨ feature | done | `design/agentic/v3-runtime-roles.md` |
| A-43 | Add real-LLM-backed `testIT` coverage for Hello World agent | 🧪 test | done | `design/agentic/v3-validation-harness.md` |
| A-44 | Define Schema Exploration POC agent scope | 📝 docs | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md` |
| A-45 | Define planner responsibilities for Schema Exploration workflow | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md`, `ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/SchemaExplorationAgent.kt` |
| A-46 | Define observer responsibilities for Schema Exploration workflow | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md`, `ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/SchemaExplorationAgent.kt` |
| A-47 | Define minimal Schema Exploration tool set | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md` |
| A-48 | Add end-to-end scenarios for Schema Exploration workflow | 🧪 test | planned | `WI-059-ai-v3-schema-exploration-scenarios.md` |
| A-49 | Define `ai/v3` Schema capability as unified physical-schema plus schema-bound metadata surface | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md` |
| A-50 | Define initial Schema tool set for explain/inspection workflows | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md` |
| A-51 | Implement schema data aggregation boundary (`SchemaFacetService`, `*WithFacets`, `SchemaFacets`) | 🔧 refactoring | done | `MILESTONE.md` (WI-062 completed) |
| A-52 | Define Schema Exploration agent profile | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md` |
| A-53 | Define Schema Exploration workflow | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md` |
| A-54 | Define Schema Exploration streaming UX/event sequence | ✨ feature | done | `workitems/PLAN-ai-v3-schema-exploration-agent.md` |
| A-55 | Add end-to-end scenarios for Schema Exploration agent | 🧪 test | planned | `WI-066-ai-v3-schema-exploration-scenarios.md` |
| A-56 | Implement `ai/v3` MCP server POC exposing discovered capabilities, tools, prompts, and protocol resources | ✨ feature | backlog | `design/agentic/v3-foundation-decisions.md` |
| A-57 | Implement multi-mode protocol execution in `ai/v3` supporting `TEXT`, `STRUCTURED_FINAL`, and `STRUCTURED_STREAM` | ✨ feature | done | `design/agentic/v3-foundation-decisions.md` |
| A-58 | Extend schema agent with metadata-authoring intent detection and structured capture for descriptions and relations | ✨ feature | done | `MILESTONE.md` (WI-068 completed) |
| A-59 | Implement `ai/v3` `sql-dialect` capability backed by `SqlDialectSpec` with focused conventions, paging, join, and function tools | ✨ feature | done | `MILESTONE.md` (WI-069 completed) |
| A-60 | Implement three-layer authoring protocol: explicit planner-intent and authored-request structured boundaries before capture; `AgentEvent.AuthoredRequest`; `targetHints` on `PlannerDecision` | ✨ feature | backlog | `design/agentic/v3-authoring-protocol.md` |
| A-61 | Implement `ai/v3` `sql-query` capability for SQL generation, validation, execution, durable generated-SQL artifacts, and non-durable result references | ✨ feature | done | `MILESTONE.md` (WI-070 completed) |
| A-62 | Implement `ai/v3` `value-mapping` capability for resolving business phrases into structured stored-value mappings for SQL/chart/refine flows | ✨ feature | done | `MILESTONE.md` (WI-071 completed) |
| A-63 | Add in-memory conversation continuity to `ai/v3` CLI and agent boundaries so multi-turn refine-style follow-ups retain prior context | ✨ feature | done | `WI-072-ai-v3-cli-conversation-continuity-and-refine.md` |
| A-63 | Add in-memory conversation continuity to `ai/v3` CLI and agent boundaries so multi-turn refine-style follow-ups retain prior context | ✨ feature | done | `WI-072-ai-v3-cli-conversation-continuity-and-refine.md` |
| A-64a | → see **PS-1** (reclassified to persistence) | ✨ feature | done | `MILESTONE.md` (WI-073a completed) |
| A-64 | → see **PS-2** (reclassified to persistence) | ✨ feature | done | `MILESTONE.md` (WI-073 completed) |
| A-65 | → see **PS-3** (reclassified to persistence) | ✨ feature | done | `MILESTONE.md` (WI-074 completed) |
| A-66 | → see **PS-4** (reclassified to persistence) | ✨ feature | done | `MILESTONE.md` (WI-075 phase-1 observer seam completed) |
| A-68 | → see **PS-4a** / **PS-4d** (artifact relation indexer follow-up) | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` |
| A-69 | → see **PS-4b** / **PS-4c** / **PS-4e** / **PS-4f** (relation projection persistence follow-up) | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` |
| A-67 | Consolidate mill-ai-v3-core/langchain4j/capabilities into mill-ai-v3; simplify to native LangChain4j tool loop; remove custom planner | 🔧 refactoring | done | `WI-076-ai-v3-langchain4j-agent-simplification.md` |
| A-70 | Persist `ai/v3` chat metadata as a first-class resource (`ChatMetadata`, `ChatUpdate`, `ChatRegistry`, JPA + in-memory adapters, centralized migration) | ✨ feature | done | `design/agentic/v3-chat-service.md`, `MILESTONE.md` |
| A-71 | Rehydrate `ai/v3` chat runtime from persisted `profileId` and durable context binding | ✨ feature | done | `design/agentic/v3-chat-service.md`, `MILESTONE.md` |
| A-72 | Implement presentation-level SSE chat stream contract (`item.created`, `item.part.updated`, `item.completed`, `item.failed`) | ✨ feature | done | `design/agentic/v3-chat-service.md`, `MILESTONE.md` |
| A-73 | Add `ai/mill-ai-v3-service` unified chat API and `ChatService` orchestration boundary | ✨ feature | done | `design/agentic/v3-chat-service.md`, `MILESTONE.md` |
| A-74 | Replace compile-time `DefaultProfileRegistry` with a dynamic or Spring-managed runtime profile registry | ✨ feature | backlog | `design/agentic/v3-chat-service.md` |
| A-75 | Enforce ownership checks on `getChat`/`updateChat`/`deleteChat`/`sendMessage` by asserting `metadata.userId == resolvedUserId` before returning data or mutating state | 🐛 fix | backlog | `design/agentic/v3-implementation-findings.md` |
| A-76 | Implement `JpaConversationStore.delete(conversationId)` to remove transcript turns on chat hard-delete; add integration test asserting full removal | 🐛 fix | backlog | `design/agentic/v3-implementation-findings.md` |
| A-77 | Define and implement explicit delete policy for artifacts and run-events on chat hard-delete: schema FK or guaranteed async cleanup with observability | ✨ feature | backlog | `design/agentic/v3-implementation-findings.md` |
| A-78 | Validate profile ID against `ProfileRegistry` during `createChat`; return 4xx on unknown profile instead of deferred runtime failure | 🐛 fix | backlog | `design/agentic/v3-implementation-findings.md` |
| A-79 | Add explicit capability admission and per-tool authorization seam before tool invocation in `LangChain4jAgent`; emit denial events | ✨ feature | backlog | `design/agentic/v3-implementation-findings.md` |
| A-80 | Route `LangChain4jAgent` final synthesis through streaming path or isolate and document non-streaming mode explicitly | 💡 improvement | backlog | `design/agentic/v3-implementation-findings.md` |
| A-81 | Add targeted test coverage: ownership/auth on chatId ops, JPA delete contract, artifact/run-event cleanup on delete, `ChatRuntimeEventToSseMapper` edge cases, real streaming assertion in testIT | 🧪 test | backlog | `design/agentic/v3-implementation-findings.md` |
| A-82 | Document the final AI v3 chat service end-to-end (modules, REST API, persistence model, SSE contract, frontend guidance, maintenance notes) | 📝 docs | planned | `WI-084-ai-v3-chat-service-documentation.md` |

---

## client — Client Libraries

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| C-1 | Implement mill/sql package with MillDialectDescriptor model and CALCITE_DEFAULT (Phase 9A) | ✨ feature | done | `MILESTONE.md` (WI-021 completed) |
| C-2 | Add quote_identifier(), qualify() helpers and type mappings (to_sa_type, to_ibis_dtype); complex type mappings depend on D-2/D-3/D-4 | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-3 | Implement DialectTester with ~80 SQL queries and DialectReport (Phase 9B) | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-4 | Populate full function catalog (scalar, aggregate, window) from tester output | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-5 | Auto-generate py-sql-dialect-report.md with feature matrix | 📝 docs | backlog | client/py-sql-dialect-plan.md |
| C-6 | Update all 10 dialect YAMLs with expanded schema (Phase 3 of YAML schema) | 🔧 refactoring | backlog | client/sql-dialect-yaml-schema.md |
| C-7 | Rewrite AI consumer: replace SpecSqlDialect with typed prompt builder | 🔧 refactoring | done | `MILESTONE.md` (WI-020 completed) |
| C-8 | Fix H2 dialect YAML: case, missing/wrong functions, paging, parameter signatures | 🐛 fix | backlog | client/sql-dialect-yaml-schema.md |
| C-9 | Remove deprecated YAML fields (identifiers.case, paging.limit/top, ordering) | 🔧 refactoring | backlog | client/sql-dialect-yaml-schema.md |
| C-10 | Implement ibis BaseBackend wrapping MillClient (Phase 10) | ✨ feature | done | **WI-025** |
| C-11 | Map ibis expressions to Calcite-compatible SQL via sqlglot | ✨ feature | done | **WI-025** |
| C-12 | Implement PEP 249 DBAPI 2.0 shim (Phase 11) | ✨ feature | done | **WI-024** |
| C-13 | Implement SQLAlchemy MillDialect and MillSQLCompiler with schema introspection; complex type support depends on D-2/D-3/D-4 | ✨ feature | done | **WI-024** |
| C-14 | Register SQLAlchemy entry points for mill+grpc and mill+http | ✨ feature | done | **WI-024** |
| C-15 | Fix MillServerError: call super().__init__(message) | 🐛 fix | backlog | client/py-cold-start.md |
| C-16 | Fix Python type mappings: BOOL->BOOLEAN, identifier quoting from dialect YAML | 🐛 fix | backlog | client/py-sql-dialect-plan.md |
| C-17 | Implement managed ADO.NET provider for Mill (separate .NET track; OLE DB as optional follow-up) | ✨ feature | backlog | `WI-077-adonet-provider.md` |
| C-18 | Implement ODBC driver for Mill with native compatibility and BI-tool conformance | ✨ feature | backlog | client/odbc-driver-design.md |
| C-19 | Update clients (Python/JDBC and related SDK surfaces) for complex types and timezone semantics in schemas/contracts | ✨ feature | backlog | **TBD (new WI)** |
| C-20 | Fix JDBC HTTP content negotiation/decoding mismatch (http-json path still assumes protobuf response decode) | 🐛 fix | backlog | **TBD (new WI)** |
| C-21 | Fix Python HTTP client content negotiation/decoding mismatch parity with JDBC (protobuf-vs-json response handling) | 🐛 fix | backlog | **TBD (new WI)** |
| C-22 | Add ibis dialect correctness validation and certification gate (DialectTester + report + CI drift detection) | 🧪 test | backlog | **WI-023** |
| C-23 | Implement full JDBC `DatabaseMetaData` surface backed by dialect descriptor (limits, type-info, feature/capability methods) | ✨ feature | done | `MILESTONE.md` (WI-026 completed) |

---

## metadata — Metadata Subsystem

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| M-1 | Create ValueMappingFacet in mill-ai-core and register via AiMetadataConfiguration | ✨ feature | planned | `WI-027-metadata-value-mapping-bridge.md` |
| M-2 | Create MetadataAdapterService implementing MetadataProvider over MetadataService | 🔧 refactoring | planned | `WI-027-metadata-value-mapping-bridge.md` |
| M-3 | Update MetadataConfiguration: replace deprecated beans, wire adapter service | 🔧 refactoring | planned | `WI-027-metadata-value-mapping-bridge.md` |
| M-4 | Migrate value mapping data from legacy YAML to facet format | ✨ feature | planned | `WI-027-metadata-value-mapping-bridge.md` |
| M-5 | Update AI components and tests to work via MetadataAdapterService | 🔧 refactoring | planned | `WI-027-metadata-value-mapping-bridge.md` |
| M-6 | Add value mapping REST API endpoints (GET/POST mappings, resolve term) | ✨ feature | planned | `WI-028-metadata-value-mapping-api-and-ui.md` |
| M-7 | Display value mappings in metadata browser UI | ✨ feature | planned | `WI-028-metadata-value-mapping-api-and-ui.md` |
| M-8 | Introduce ValueResolver abstraction with feature flag (legacy/faceted/hybrid) | 🔧 refactoring | planned | `WI-027-metadata-value-mapping-bridge.md` |
| M-9 | Add parity tests: legacy vs facet value resolution | 🧪 test | planned | `WI-027-metadata-value-mapping-bridge.md` |
| M-10 | Implement EnrichmentFacet, EnrichmentService, and approval workflow | ✨ feature | planned | `docs/design/metadata/metadata-implementation-roadmap.md` |
| M-11 | → see **PS-6** (reclassified to persistence) | ✨ feature | done | `MILESTONE.md` (WI-087 completed) |
| M-12 | → see **PS-7** (reclassified to persistence) | ✨ feature | done | `MILESTONE.md` (WI-087 completed) |
| M-15 | Implement full-text and facet-aware search (Postgres/Elastic/Lucene) | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-16 | Implement DataQualityFacet and rule execution engine | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-17 | Implement SemanticFacet with vector store integration | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-18 | Implement LineageFacet and lineage graph API | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-19 | Implement scope resolution (user > team > role > global) with security context | ✨ feature | done | `MILESTONE.md` (WI-089 completed) |
| M-20 | Add UI editing for metadata facets (DescriptiveFacet, RelationFacet, ConceptFacet, etc.) | ✨ feature | done | `MILESTONE.md` (WI-090 / metadata stories closed March 2026) |
| M-21 | Fix MessageHelper parse error messages (generic HandshakeResponse) | 🐛 fix | done | `MILESTONE.md` (WI-085 completed) |
| M-22 | Remove ProtobufUtils dead code and register HTTP ServiceDescriptor | 🐛 fix | done | `MILESTONE.md` (WI-085 completed) |
| M-23 | Metadata promotion workflow (request, review, approve/reject) and REST surface | ✨ feature | backlog | `MILESTONE.md` (WI-091 deferred) |
| M-24 | Interactive metadata scope picker in Data Model (beyond implicit/global) + strict scope authorization for metadata writes | ✨ feature | backlog | `MILESTONE.md` (deferred from schema explorer closure) |
| M-25 | Schema list/tree REST performance hardening under large catalogs | 💡 improvement | backlog | `MILESTONE.md` (deferred from schema explorer closure) |
| M-27 | Extend metadata StructuralFacet/API/UI contracts for complex types (LIST/MAP/OBJECT + nested shape rendering); depends on D-2/D-3/D-4 | ✨ feature | planned | `WI-034-metadata-complex-type-support.md` |
| M-28 | Case-insensitive metadata entity identity (coordinate model + merge migration) | 🐛 fix | superseded | Superseded by **M-29** / `docs/workitems/metadata-case-insensitivity/STORY.md` (greenfield URN); **WI-111** not executed on URN branch |
| M-29 | Metadata URN platform: metadata modules deps = metadata+core only; SQL seeds from `platform-facet-types.json` in `mill-persistence`; PG primary + H2 PG mode; Java `@ConfigurationProperties`; squashed Flyway; facet audit listeners; canonical YAML file repo (`mill.metadata.repository.type=file`); `RelationFacet` / URN codec in `mill-data-schema-core`; **no backward compat**; KDoc/JavaDoc (UI function-level) | ✨ feature | done | `MILESTONE.md` (**WI-119–WI-128**); archived story `workitems/completed/20260330-metadata-rework/`; parallel WI plan in `metadata-case-insensitivity/STORY.md` (WI-111–WI-118) |
| M-30 | Document Mill UI **known facet field stereotypes** (`hyperlink`, `email`, `tags`, precedence, OBJECT/array-of-OBJECT hyperlink) — design + public MkDocs | 📝 docs | done | `design/metadata/mill-ui-facet-stereotypes.md`, `public/src/metadata/facet-stereotypes.md` |
| M-31 | Layered metadata: readonly **`MetadataSource`** (repository read adapter + runtime/system sources), merge into **`FacetInstanceReadMerge`** / metadata REST + **`SchemaFacetService`** in schema core; **ephemeral** facets (e.g. logical layout inferred, future **authorization**/policy) not metadata-persistent; read API with **per-facet provenance** and **`editable`**; Mill UI **full constellation visible**, **captured-only** edit | ✨ feature | done | `design/metadata/metadata-layered-sources-and-ephemeral-facets.md`, story `completed/20260401-metadata-and-ui-improve-and-clean/` |
| M-32 | **Facet type catalog (metadata capture follow-up):** list endpoint + Mill UI **facet type** admin show **`FacetTypeSource.DEFINED` and `OBSERVED`** (union/dedup); **OBSERVED** types visible when assignments created unknown keys; label source in UI; optional read-only / promote-to-defined for OBSERVED | ✨ feature | backlog | `metadata/metadata-facet-type-catalog-defined-and-observed.md` |
| M-33 | Eliminate redundant **`MetadataEntity.kind`** / **`entity_kind`** now that entity URNs are typed (`urn:mill/model/...`); domain, JPA, REST DTOs, YAML — **WI-144** | 🔧 refactoring | planned | `docs/workitems/eliminate-entity-kind/STORY.md` |

---

## platform — Infrastructure and Cross-Cutting

| #    | Item                                                                                                                                                                                     | Type           | Status  | Source                                          |
| ---- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- | ------- | ----------------------------------------------- |
| P-1  | Migrate controllers to WebFlux: reactive repositories, services, Mono/Flux returns                                                                                                       | 🔧 refactoring | backlog | platform/webflux-migration-plan.md              |
| P-2  | Rewrite MillUiSpaRoutingFilter as WebFlux WebFilter                                                                                                                                         | 🔧 refactoring | backlog | platform/webflux-migration-plan.md              |
| P-3  | Create ReactiveMessageHelper for protobuf/JSON conversion in WebFlux                                                                                                                     | ✨ feature      | backlog | platform/webflux-migration-plan.md              |
| P-4  | Replace MockMvc with WebTestClient across all affected test suites                                                                                                                       | 🧪 test        | backlog | platform/webflux-migration-plan.md              |
| P-5  | Spring Boot 4.0 pre-migration cleanup: hardcoded versions, javax->jakarta, spring.factories                                                                                              | 🐛 fix         | backlog | platform/spring4-migration-plan.md, **story `spring4-pre-migration-cleanup/`** (WI-097–WI-104) |
| P-6  | Remove net.devh gRPC starter; reimplement gRPC server with raw grpc-java                                                                                                                 | 🔧 refactoring | done | platform/spring4-migration-plan.md, `MILESTONE.md` (WI-085 completed) |
| P-7  | Migrate Jackson 2.x to Jackson 3.0 (ObjectMapper->JsonMapper, package changes)                                                                                                           | 🔧 refactoring | backlog | platform/spring4-migration-plan.md              |
| P-8  | Upgrade Spring AI to 2.0.x and SpringDoc OpenAPI to 3.x                                                                                                                                  | 💡 improvement | backlog | platform/spring4-migration-plan.md              |
| P-9  | Review and fix Spring Security 7.0 breaking changes                                                                                                                                      | 🔧 refactoring | backlog | platform/spring4-migration-plan.md              |
| P-10 | Implement MCP Data Provider per specification (resources, tools, prompts)                                                                                                                | ✨ feature      | backlog | platform/mcp.md                                 |
| P-11 | Create proto data_export_svc.proto and implement Data Export Service                                                                                                                     | ✨ feature      | backlog | platform/data-export-service.md                 |
| P-12 | Use combined Calcite operator table (STANDARD + POSTGRESQL)                                                                                                                              | 💡 improvement | backlog | platform/calcite-dialect-comparison.md          |
| P-13 | Add custom Calcite operators for AGE(timestamp,timestamp) and ILIKE                                                                                                                      | ✨ feature      | backlog | platform/calcite-dialect-comparison.md          |
| P-14 | Fix postgres.yml type mappings (COUNT->BIGINT, EXTRACT->NUMERIC, polymorphic)                                                                                                            | 🐛 fix         | backlog | platform/calcite-dialect-comparison.md          |
| P-16 | Replace @Qualifier("LOJOKOJ") placeholder with meaningful qualifier                                                                                                                      | 🐛 fix         | backlog | platform/CONFIGURATION_INVENTORY.md             |
| P-17 | Complete RAG implementation: vector store value mapper, integration tests                                                                                                                | ✨ feature      | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md           |
| P-18 | Add custom metrics, distributed tracing (OpenTelemetry), and structured logging                                                                                                          | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md           |
| P-19 | Implement query result caching, metadata caching, optimize connection pooling                                                                                                            | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md           |
| P-20 | Add rate limiting, audit logging, and policy testing framework                                                                                                                           | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md           |
| P-21 | Add architecture diagrams, user guides, and troubleshooting guide                                                                                                                        | 📝 docs        | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md           |
| P-22 | Consider compression for vector blocks and serialization performance metrics                                                                                                             | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md           |
| P-23 | Phase 1: SQL-direct execution path — QueryPlan record, cancelable QueryResult, ExecutionProvider/SqlProvider RelNode methods, SQL parameter support, route SQL in dispatcher             | 🔧 refactoring | backlog | platform/substrait-to-relnode-migration.md      |
| P-24 | Phase 2: Substrait input via RelNode — route Substrait through PlanConverter to QueryPlan, remove old execute(Plan)                                                                      | 🔧 refactoring | backlog | platform/substrait-to-relnode-migration.md      |
| P-25 | Phase 3: Port rewriters to Calcite — PlanRewriter/facets on RelNode/RexNode, TableFacetVisitor to RelShuttleImpl                                                                         | 🔧 refactoring | backlog | platform/substrait-to-relnode-migration.md      |
| P-26 | Phase 4: Clean up dead Substrait code — remove PlanHelper, DataTypeToSubstrait, old SqlProvider methods, substrait-core from mill-core                                                   | 🔧 refactoring | backlog | platform/substrait-to-relnode-migration.md      |
| P-27 | Implement Arrow Flight server for Mill query transport with canonical Mill->Arrow type mapping and per-column timestamp timezone semantics; complex type coverage depends on D-2/D-3/D-4 | ✨ feature      | backlog | platform/arrow-flight-server-design.md          |
| P-28 | Implement Arrow Flight SQL server for Mill with SQL metadata compatibility and per-column timezone semantics; complex type coverage depends on D-2/D-3/D-4                               | ✨ feature      | backlog | platform/arrow-flight-sql-server-design.md      |
| P-29 | Introduce proto/schema timezone extension (field-level TZ metadata) and propagate across source, backend, service, and client mappings                                                   | ✨ feature      | backlog | **WI-011**                                      |
| P-30 | Implement end-to-end timezone support (frontend to backend): preserve, expose, and validate field-level timezone metadata across contracts and UI flows                                  | ✨ feature      | backlog | **TBD (new WI)**                                |
| P-31 | Improve HTTP/gRPC error transparency: return structured Problem Details and propagate detail/code/traceId in Python and JDBC clients                                                     | 🐛 fix         | backlog | **WI-013**                                      |
| P-32 | Add reusable build-logic plugin for controlled multi-edition Spring Boot `bootDist`/`installBootDist` outputs in `apps/mill-service` (single app module, edition-specific install dirs) | ✨ feature      | backlog | **WI-014**                                      |
| P-33 | Explore Docker Buildx Bake to reduce Docker image build time across services and pipelines                                                                                                 | 💡 improvement | backlog | **TBD (new WI)**                                |
| P-34 | Define repository-wide REST exception/status-handling pattern for thin controllers, semantic status exceptions, and uniform error payloads                                                 | 📝 docs        | done    | `platform/rest-exception-handling-pattern.md`, `MILESTONE.md` |
| P-35 | Extract a shared Spring web module for reusable REST advice and standard error payload mapping across services                                                                             | ✨ feature      | backlog | `platform/rest-exception-handling-pattern.md`   |

---

## publish — Build, Release, and Documentation

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| PB-1 | Wire Maven publication pipeline into main .gitlab-ci.yml | 💡 improvement | backlog | publish/maven-publishing.md |
| PB-2 | Document Dokka/MkDocs two-phase build with architecture diagrams | 📝 docs | backlog | publish/documentation-generation.md |
| PB-3 | Publish Kotlin API docs (Dokka) alongside public documentation site | 💡 improvement | backlog | publish/dokka.md, publish/documentation-generation.md |
| PB-4 | Publish JARs to Maven Central as part of release process | ✨ feature | backlog | publish/maven-publishing.md |

---

## refactoring — Codebase Refactoring

| #    | Item                                                                                                              | Type           | Status    | Source                                   |
| ---- | ----------------------------------------------------------------------------------------------------------------- | -------------- | --------- | ---------------------------------------- |
| R-6  | Fix mill.security.enabled vs mill.security.enable inconsistency in test configs                                   | 🐛 fix         | backlog   | refactoring/05-configuration-keys.md     |
| R-7  | Remove ghost keys (data-bot.*, jet-grpc.*) or implement their Java consumers                                      | 🐛 fix         | backlog   | refactoring/05-configuration-keys.md     |
| R-8  | Add missing additional-spring-configuration-metadata.json for 28+ mill.* keys                                     | 💡 improvement | backlog   | refactoring/05-configuration-keys.md     |
| R-9  | Remove or use @ConditionalOnTestKit (dead annotation)                                                             | 🐛 fix         | backlog   | refactoring/05-configuration-keys.md     |
| R-10 | Fix mill.services.jet-http.enable type in metadata JSON (String to Boolean)                                       | 🐛 fix         | backlog   | refactoring/05-configuration-keys.md     |
| R-11 | Review commented-out tests in AI and JDBC driver: delete, move, or re-enable                                      | 🧪 test        | backlog   | refactoring/06-test-module-inventory.md  |
| R-12 | Fix JDBC driver integration test infrastructure (re-enable disabled testIT classes)                               | 🧪 test        | backlog   | refactoring/06-test-module-inventory.md  |
| R-13 | Reduce technical debt: review 119 files with TODOs/FIXMEs                                                         | 🔧 refactoring | backlog   | platform/CODEBASE_ANALYSIS_CURRENT.md    |
| R-14 | Refactor data module Spring configuration: review and implement in mill-data-autoconfigure                        | 🔧 refactoring | backlog   | refactoring/05-configuration-keys.md     |
| R-28 | ~~Extract Spring Boot @Configuration classes from mill-ai-v1-core~~ (skipped — v1 being replaced by v2)           | 🔧 refactoring | cancelled | **WI-010**                               |

---

## source — Data Source Framework

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| S-1 | Implement FilterableTable for filter push-down in Calcite sources | 💡 improvement | backlog | source/mill-source-calcite.md |
| S-2 | Implement FlowTable scan from vector blocks (avoid row materialization) | 💡 improvement | backlog | source/mill-source-calcite.md |
| S-3 | Add Caffeine caching at BlobSource and FormatHandler (Phase 5) | 💡 improvement | backlog | source/flow-kt-design.md |
| S-4 | Support native Calcite UUID in row type (currently VARCHAR) | 💡 improvement | backlog | source/mill-source-calcite.md |
| S-5 | Fix interval types round-trip through Calcite adapters | 🐛 fix | backlog | source/mill-source-calcite.md |
| S-6 | Implement ModifiableTable for write path (FlowTable currently read-only) | ✨ feature | backlog | source/mill-source-calcite.md |
| S-7 | Consolidate CalciteTypeMapper and RelToDatabaseTypeConverter into shared module | 🔧 refactoring | backlog | source/mill-source-calcite.md |
| S-8 | → see **PS-8** (reclassified to persistence) | ✨ feature | backlog | source/flow-kt-design.md |
| S-9 | Implement S3BlobSource, AzureBlobSource, HdfsBlobSource storage backends | ✨ feature | backlog | source/flow-kt-design.md |
| S-10 | Implement HivePartitionTableMapper and GlobTableMapper | ✨ feature | backlog | source/flow-kt-design.md |
| S-17 | Standardize format read/write naming: evaluate RecordSource vs RecordReader and align concrete format classes for consistency | 🔧 refactoring | backlog | **WI-011** |
| S-18 | Add ORC format support in data/formats (reader/writer + schema mapping); nested/complex types depend on D-2/D-3/D-4 | ✨ feature | backlog | **TBD (new WI)** |
| S-19 | Add Parquet complex type support (LIST/MAP/OBJECT) aligned with source type mapping; depends on D-2/D-3/D-4 | ✨ feature | backlog | data/complex-type-support.md |
| S-20 | Add JSONL/NDJSON format support in data/formats (schema inference + reader/writer); nested/complex types depend on D-2/D-3/D-4 | ✨ feature | backlog | **TBD (new WI)** |
| S-21 | Extend Arrow format support for complex types and timezone semantics after proto/client updates; depends on D-2/D-3/D-4 and P-29/P-30 | ✨ feature | backlog | source/arrow-format-design.md |

**Recommended WI sequence (dependency-driven):**
1. WI: Complex type support (`D-1`..`D-5` baseline)
2. WI: Timezone semantics (row + schema/proto) — align to `P-29` (proto/schema TZ extension) and `P-30` (end-to-end TZ support), combine proto-impacting TZ work in one item
3. WI: Client updates for complex types + TZ semantics (`C-19`, dependent on `P-29`/`P-30`)
4. WI: Metadata adaptation for complex types (`M-27`, depends on `D-2`/`D-3`/`D-4`)
5. WI: Extend Arrow support (`S-21`, depends on complex types + TZ work)
6. WI: Implement JSONL (`S-20`, depends on complex types)
7. WI: Implement ORC (`S-18`, depends on complex types)
8. WI: Extend Parquet with complex types (`S-19`)

---

## ui — Frontend and UX

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| U-1 | Refactor ChatProvider to use clarification context (reasoningId + initialQuestion) | 🔧 refactoring | backlog | ui/ux-clarification-and-notification.md |
| U-2 | Auto-activate clarification mode when clarification message arrives | ✨ feature | backlog | ui/ux-clarification-and-notification.md |
| U-3 | Replace multiple Reply buttons with single Reply/Answer button | 💡 improvement | backlog | ui/ux-clarification-and-notification.md |
| U-4 | Create reusable StatusIndicator component (mode + event feedback) | ✨ feature | backlog | ui/ux-clarification-and-notification.md |
| U-5 | Update PostMessage textarea to compact ChatGPT-style | 💡 improvement | backlog | ui/ux-clarification-and-notification.md |
| U-6 | Add caching for @ mention search results | 💡 improvement | backlog | ui/chat-input-enhancements.md |
| U-7 | Add fuzzy matching, keyboard shortcuts, and entity preview for command palette | 💡 improvement | backlog | ui/chat-input-enhancements.md |
| U-8 | Support multi-select for @ mention entities | ✨ feature | backlog | ui/chat-input-enhancements.md |
| U-9 | Support command parameters (e.g. /get-data limit=10) | ✨ feature | backlog | ui/chat-input-enhancements.md |
| U-10 | Add tests for command palette, @ mentions, keyboard nav, and error scenarios | 🧪 test | backlog | ui/chat-input-enhancements.md |
| U-11 | Migrate `mill-ui` general chat from legacy `/api/nl2sql/chats/*` to `/api/v1/ai/chats/*` and adopt `item.*` SSE handling | ✨ feature | planned | `WI-082-mill-ui-unified-ai-chat-integration.md` |
| U-12 | Redesign optional per-facet-type **view** and **edit** component registration (replacing removed bespoke facet presenters); descriptor-driven fallback remains default | ✨ feature | backlog | `design/ui/facet-view-customization.md` |

---

## persistence — Persistence Layer

Design reference: [`docs/design/persistence/persistence-overview.md`](../design/persistence/persistence-overview.md)

Delivery order: PS-1 → PS-3 → PS-2 → PS-4 → PS-5 → PS-6/PS-7 → PS-8

| # | Item | Type | Status | Source | Domain |
|---|------|------|--------|--------|--------|
| PS-1 | Create central `persistence/` module group (`mill-persistence`, `mill-persistence-autoconfigure`) with Flyway baseline, H2 PostgreSQL mode, autoconfiguration wiring, and adapter/testing conventions | ✨ feature | done | `MILESTONE.md` (WI-073a completed) | platform |
| PS-2 | Implement `ai/v3` Lane 3 — durable chat-memory persistence: `ChatMemoryStore` + `LlmMemoryStrategy` ports in `v3-core`, `InMemoryChatMemoryStore`, injected into agent/runtime | ✨ feature | done | `MILESTONE.md` (WI-073 completed) | ai/v3 |
| PS-3 | Implement `ai/v3` Lanes 1,2 — routed event propagation (`EventRoutingPolicy`, `RoutedAgentEvent`, publisher/listener), `ConversationStore`, `RunEventStore`, `ArtifactStore`, `ActiveArtifactPointerStore`; in-memory first; SSE-ready envelope | ✨ feature | done | `MILESTONE.md` (WI-074 completed) | ai/v3 |
| PS-4 | Implement `ai/v3` Lane 4 phase 1 — artifact observers and indexing seam: `ArtifactObserver`, `ArtifactIndexingRequest`, `NoOpArtifactObserver`, post-persist wiring, async/best-effort observer invocation | ✨ feature | done | `MILESTONE.md` (WI-075 phase-1 observer seam completed) | ai/v3 |
| PS-4a | Implement real `ArtifactRelationIndexer` contract and first concrete indexer | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4b | Implement `RelationStore` plus in-memory adapter for derived relation projections | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4c | Define derived relation model for `conversation -> object`, `artifact -> object`, and `run -> object` edges | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4d | Implement artifact-type-specific extraction logic for SQL, metadata-capture, and value-mapping artifacts | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4e | Implement rebuild/indexing flow from artifact history into derived relation projections | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4f | Add tests for relation derivation, persistence, rebuild, and observer/indexer integration | 🧪 test | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-5 | Add Spring/JPA durable adapters for `ai/v3` stores using `ai/mill-ai-v3-persistence`, centralized Flyway in `mill-persistence`, shared `EntityRef`/relation persistence, and `mill-ai-v3-autoconfigure` bean wiring | ✨ feature | done | `MILESTONE.md` (WI-078 completed) | ai/v3 |
| PS-6 | Implement metadata relational persistence: JPA entity + facet schema (JSONB), `MetadataJpaRepository`, optimistic concurrency, `CompositeMetadataRepository` blending file + JPA | ✨ feature | done | `MILESTONE.md` (WI-087 completed) | metadata |
| PS-7 | Implement `MetadataSyncService` for deterministic file→DB bootstrap and scheduled sync; `composite` / `jpa` repository mode switch; Flyway migration V5 | ✨ feature | done | `MILESTONE.md` (WI-087 completed) | metadata |
| PS-8 | Implement source definition persistence: CRUD API and programmatic builders for connection specs, format configs, blob source roots; Flyway migration; adapter in `mill-persistence` | ✨ feature | backlog | `source/flow-kt-design.md` | source |

---

## security — Authentication, Identity, and Access

Design reference: [`docs/design/security/auth-profile-pat-architecture.md`](../design/security/auth-profile-pat-architecture.md)
Implementation: [`docs/design/security/user-identity-jpa-implementation.md`](../design/security/user-identity-jpa-implementation.md)

| #      | Item | Type | Status | Source |
|--------|------|------|--------|--------|
| SEC-1  | Persistent user identity model: `users`, `user_credentials`, `user_identities`, `groups`, `group_memberships`, `user_profiles`, `auth_events`; `validated`/`locked` login gates; Flyway migration; `mill-security-persistence` module | ✨ feature | done | `MILESTONE.md` |
| SEC-1a | `JpaUserRepo` + `JpaPasswordAuthenticationConfiguration` + `PasswordEncoder`; `JpaUserIdentityResolutionService`; `JpaAuthAuditService`; integration tests | ✨ feature | done | `MILESTONE.md` |
| SEC-2  | PAT (Personal Access Token) issuance, secure hashed-token storage, and bearer token validation provider in `SecurityFilterChain`; PAT management UI (Access tab) | ✨ feature | backlog | `design/security/auth-profile-pat-architecture.md` |
| SEC-3a | `mill-security-auth-service`: `POST /auth/public/login`, `POST /auth/logout`, `GET /auth/me`; auth audit trail; structured logging | ✨ feature | done | `MILESTONE.md` |
| SEC-3b | `UserProfileService` + `PATCH /auth/profile`; real user data wired into `ProfileLayout` and `AppHeader` in `mill-ui` | ✨ feature | done | `MILESTONE.md` |
| SEC-3c | `POST /auth/public/register`; `mill.security.allow-registration` config gate; `RegisterPage`; `loginRegistration` feature flag | ✨ feature | done | `MILESTONE.md` |
| SEC-3d | `mill-ui` real auth: `authService.ts`, real `AuthContext`, `RequireAuth`, login error display, security-off behaviour; Vite proxy for `/auth` and `/.well-known` | ✨ feature | done | `MILESTONE.md` |
| SEC-3e | Extract `mill-security-autoconfigure`; merge `PolicyConfiguration` + `PolicyActionsConfiguration` into `PolicyAuthorizationConfiguration`; `secure` dev profile in `mill-service` | ✨ feature | done | `MILESTONE.md` |
| SEC-4  | OAuth/SSO federation: OIDC/Entra/GitHub/Google token validation; `OAuth2UserService` calling `resolveOrProvision`; provider UI buttons | ✨ feature | backlog | `design/security/auth-profile-pat-architecture.md` |
| SEC-5  | "Forgot password?" flow: reset request endpoint, email delivery, token validation, password update; activate dead UI link | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-6  | Admin user management: API + UI for creating, disabling, locking/unlocking users; manage `locked`/`validated` flags with `lockDate` and `lockReason` | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-7  | Email verification: `validated=FALSE` on registration, confirmation email, flip to `TRUE` on link visit; block login until validated | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-8  | Production password hasher: `BCryptPasswordHasher` bean replacing `NoOpPasswordHasher`; migration helper to re-hash existing `{noop}` credentials on next login | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-9  | Brute-force protection: login failure counter per subject; auto-set `locked=TRUE` after N failures; configurable threshold via `mill.security.*` | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |

---

## Summary

| Category        | Total   | ✨ feature | 💡 improvement | 🐛 fix | 🔧 refactoring | 🧪 test | 📝 docs |
| --------------- | ------- | --------- | -------------- | ------ | -------------- | ------- | ------- |
| data            | 7       | 6         | 0              | 0      | 0              | 0       | 1       |
| ai              | 29      | 16        | 5              | 4      | 2              | 1       | 1       |
| client          | 21      | 11        | 0              | 5      | 3              | 1       | 1       |
| metadata        | 16      | 11        | 0              | 2      | 1              | 1       | 1       |
| persistence     | 8       | 8         | 0              | 0      | 0              | 0       | 0       |
| platform        | 32      | 11        | 8              | 4      | 8              | 1       | 0       |
| publish         | 4       | 1         | 2              | 0      | 0              | 0       | 1       |
| refactoring     | 10      | 0         | 1              | 4      | 2              | 2       | 1       |
| security        | 14      | 14        | 0              | 0      | 0              | 0       | 0       |
| source          | 14      | 7         | 4              | 1      | 2              | 0       | 0       |
| ui              | 11      | 5         | 4              | 0      | 1              | 1       | 0       |
| **Total**       | **166** | **90**    | **24**         | **20** | **19**         | **7**   | **6**   |

