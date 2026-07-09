# Backlog

Internal tracker for planned product items. **Open** work uses **`backlog`**, **`planned`**, or
**`in-progress`**. Shipped work is recorded under [`releases/`](releases/) (e.g.
[`RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md)); **`done`** rows are **pruned at release housekeeping**
— see [`RULES.md`](RULES.md) § **Release (version) process**.

**Legend:**
- **Status**: `backlog` | `planned` | `in-progress`
- **Type**: ✨ feature | 💡 improvement | 🐛 fix | 🔧 refactoring | 🧪 test | 📝 docs
- **Source**: design document (relative to `docs/design/`) or work item (`docs/workitems/`)
- **Releases:** [`RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md) (shipped); draft next release in [`MILESTONE.md`](MILESTONE.md) § **0.9.0**

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
| D-10 | Flow scan pushdown + Parquet column projection (**WI-312**, **WI-313**) | 💡 improvement | planned | [`planned/flow-scan-pushdown/STORY.md`](planned/flow-scan-pushdown/STORY.md) — cold-start architecture + code map in STORY |

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
| A-48 | Add end-to-end scenarios for Schema Exploration workflow | 🧪 test | planned | `docs/workitems/planned/ai-v3/WI-059-ai-v3-schema-exploration-scenarios.md` |
| A-55 | Add end-to-end scenarios for Schema Exploration agent | 🧪 test | planned | `docs/workitems/planned/ai-v3/WI-066-ai-v3-schema-exploration-scenarios.md` |
| A-60 | Implement three-layer authoring protocol: explicit planner-intent and authored-request structured boundaries before capture; `AgentEvent.AuthoredRequest`; `targetHints` on `PlannerDecision` | ✨ feature | backlog | `design/agentic/v3-authoring-protocol.md` |
| A-68 | → see **PS-4a** / **PS-4d** (artifact relation indexer follow-up) | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` |
| A-69 | → see **PS-4b** / **PS-4c** / **PS-4e** / **PS-4f** (relation projection persistence follow-up) | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` |
| A-74 | Replace compile-time `DefaultProfileRegistry` with a dynamic or Spring-managed runtime profile registry | ✨ feature | backlog | `design/agentic/v3-chat-service.md` |
| A-78 | Validate profile ID against `ProfileRegistry` during `createChat`; return 4xx on unknown profile instead of deferred runtime failure | 🐛 fix | backlog | `design/agentic/v3-implementation-findings.md` |
| A-79 | Add explicit capability admission and per-tool authorization seam before tool invocation in `LangChain4jAgent`; emit denial events | ✨ feature | backlog | `design/agentic/v3-implementation-findings.md` |
| A-80 | Route `LangChain4jAgent` final synthesis through streaming path or isolate and document non-streaming mode explicitly | 💡 improvement | backlog | `design/agentic/v3-implementation-findings.md` |
| A-81 | Add targeted test coverage: ownership denial on chatId ops, `ChatRuntimeEventToSseMapper` edge cases, real streaming assertion in testIT (delete/cascade covered by WI-324) | 🧪 test | backlog | `design/agentic/v3-implementation-findings.md` |
| A-82 | Document the final AI v3 chat service end-to-end (modules, REST API, persistence model, SSE contract, frontend guidance, maintenance notes) | 📝 docs | planned | `docs/workitems/planned/ai-v3/WI-084-ai-v3-chat-service-documentation.md` |
| A-93 | HTTP scenario runner for `mill-ai-test` (follow-up from artifact emit contract) | ✨ feature | backlog | deferred from `ai-artifact-emit-contract` WI-309 |
| A-94 | `ai:v3-integration` CI job: live-LLM scenario matrix in GitLab CI (gated on secrets) | 🧪 test | backlog | deferred from `ai-artifact-emit-contract` WI-309 |
| A-95 | Promote live-LLM YAML packs to default CI (currently gated testIT in WI-310) | 🧪 test | backlog | deferred from `ai-artifact-emit-contract` WI-309 |
| A-96 | stdio MCP bridge (`mill-ai-mcp-transport-stdio`) proxying local stdio → remote HTTP MCP | ✨ feature | backlog | descoped from **A-56** — [`backlog/WI-328-mill-ai-mcp-transport-stdio.md`](backlog/WI-328-mill-ai-mcp-transport-stdio.md); design: [`v3-mcp-capability-exposure.md`](design/agentic/v3-mcp-capability-exposure.md) §9 |
| A-97 | `analysis-copilot` backend profile + turn `context.values` consumption | ✨ feature | done | [`in-progress/analysis-inline-chat-foundation/WI-396-analysis-copilot-backend-profile.md`](in-progress/analysis-inline-chat-foundation/WI-396-analysis-copilot-backend-profile.md); design: [`ui/mill-ui/INLINE-CHAT-FOUNDATION.md`](design/ui/mill-ui/INLINE-CHAT-FOUNDATION.md) |
| A-98 | Deterministic artifact publish validation (runtime gate + capability validators) | 🔧 refactoring | planned | [`planned/artifact-publish-validation/STORY.md`](planned/artifact-publish-validation/STORY.md) |

---

## client — Client Libraries

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| C-2 | Add quote_identifier(), qualify() helpers and type mappings (to_sa_type, to_ibis_dtype); complex type mappings depend on D-2/D-3/D-4 | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-3 | Implement DialectTester with ~80 SQL queries and DialectReport (Phase 9B) | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-4 | Populate full function catalog (scalar, aggregate, window) from tester output | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-5 | Auto-generate py-sql-dialect-report.md with feature matrix | 📝 docs | backlog | client/py-sql-dialect-plan.md |
| C-6 | Update all 10 dialect YAMLs with expanded schema (Phase 3 of YAML schema) | 🔧 refactoring | backlog | client/sql-dialect-yaml-schema.md |
| C-8 | Fix H2 dialect YAML: case, missing/wrong functions, paging, parameter signatures | 🐛 fix | backlog | client/sql-dialect-yaml-schema.md |
| C-9 | Remove deprecated YAML fields (identifiers.case, paging.limit/top, ordering) | 🔧 refactoring | backlog | client/sql-dialect-yaml-schema.md |
| C-15 | Fix MillServerError: call super().__init__(message) | 🐛 fix | backlog | client/py-cold-start.md |
| C-16 | Fix Python type mappings: BOOL->BOOLEAN, identifier quoting from dialect YAML | 🐛 fix | backlog | client/py-sql-dialect-plan.md |
| C-17 | Implement managed ADO.NET provider for Mill (separate .NET track; OLE DB as optional follow-up) | ✨ feature | backlog | `docs/workitems/planned/adonet-provider/WI-077-adonet-provider.md` |
| C-18 | Implement ODBC driver for Mill with native compatibility and BI-tool conformance | ✨ feature | backlog | client/odbc-driver-design.md |
| C-19 | Update clients (Python/JDBC and related SDK surfaces) for complex types and timezone semantics in schemas/contracts | ✨ feature | backlog | **TBD (new WI)** |
| C-20 | Fix JDBC HTTP content negotiation/decoding mismatch (http-json path still assumes protobuf response decode) | 🐛 fix | backlog | **TBD (new WI)** |
| C-21 | Fix Python HTTP client content negotiation/decoding mismatch parity with JDBC (protobuf-vs-json response handling) | 🐛 fix | backlog | **TBD (new WI)** |
| C-22 | Add ibis dialect correctness validation and certification gate (DialectTester + report + CI drift detection) | 🧪 test | backlog | **`docs/workitems/planned/ibis-dialect-validation/WI-023-ibis-dialect-correctness-validation.md`** |

---

## metadata — Metadata Subsystem

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| M-1 | Create ValueMappingFacet in mill-ai-core and register via AiMetadataConfiguration | ✨ feature | planned | `docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md` |
| M-2 | Create MetadataAdapterService implementing MetadataProvider over MetadataService | 🔧 refactoring | planned | `docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md` |
| M-3 | Update MetadataConfiguration: replace deprecated beans, wire adapter service | 🔧 refactoring | planned | `docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md` |
| M-4 | Migrate value mapping data from legacy YAML to facet format | ✨ feature | planned | `docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md` |
| M-5 | Update AI components and tests to work via MetadataAdapterService | 🔧 refactoring | planned | `docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md` |
| M-6 | Add value mapping REST API endpoints (GET/POST mappings, resolve term) | ✨ feature | planned | `docs/workitems/planned/metadata-value-mapping/WI-173-metadata-value-mapping-api-and-ui.md` |
| M-7 | Display value mappings in metadata browser UI | ✨ feature | planned | `docs/workitems/planned/metadata-value-mapping/WI-173-metadata-value-mapping-api-and-ui.md` |
| M-8 | Introduce ValueResolver abstraction with feature flag (legacy/faceted/hybrid) | 🔧 refactoring | planned | `docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md` |
| M-9 | Add parity tests: legacy vs facet value resolution | 🧪 test | planned | `docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md` |
| M-10 | Implement EnrichmentFacet, EnrichmentService, and approval workflow | ✨ feature | planned | `docs/design/metadata/metadata-implementation-roadmap.md` |
| M-15 | Implement full-text and facet-aware search (Postgres/Elastic/Lucene) | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-16 | Implement DataQualityFacet and rule execution engine | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-17 | Implement SemanticFacet with vector store integration | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-18 | Implement LineageFacet and lineage graph API | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-23 | Metadata promotion workflow (request, review, approve/reject) and REST surface | ✨ feature | backlog | `MILESTONE.md` (WI-091 deferred) |
| M-24 | Interactive metadata scope picker in Data Model (beyond implicit/global) + strict scope authorization for metadata writes | ✨ feature | backlog | `MILESTONE.md` (deferred from schema explorer closure) |
| M-25 | Schema list/tree REST performance hardening under large catalogs | 💡 improvement | backlog | `MILESTONE.md` (deferred from schema explorer closure) |
| M-27 | Extend metadata StructuralFacet/API/UI contracts for complex types (LIST/MAP/OBJECT + nested shape rendering); depends on D-2/D-3/D-4 | ✨ feature | planned | `docs/workitems/planned/metadata-complex-types/WI-034-metadata-complex-type-support.md` |
| M-32 | **Facet type catalog (metadata capture follow-up):** list endpoint + Mill UI **facet type** admin show **`FacetTypeSource.DEFINED` and `OBSERVED`** (union/dedup); **OBSERVED** types visible when assignments created unknown keys; label source in UI; optional read-only / promote-to-defined for OBSERVED | ✨ feature | backlog | `metadata/metadata-facet-type-catalog-defined-and-observed.md` |
| M-34 | Eliminate redundant **`MetadataEntity.kind`** / **`entity_kind`** now that entity URNs are typed (`urn:mill/model/...`); domain, JPA, REST DTOs, YAML — **WI-144** | 🔧 refactoring | planned | `docs/workitems/planned/eliminate-entity-kind/STORY.md` |

---

## platform — Infrastructure and Cross-Cutting

| #    | Item                                                                                                                                                                                     | Type           | Status  | Source                                          |
| ---- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- | ------- | ----------------------------------------------- |
| P-1  | Migrate controllers to WebFlux: reactive repositories, services, Mono/Flux returns                                                                                                       | 🔧 refactoring | backlog | platform/webflux-migration-plan.md              |
| P-2  | Rewrite MillUiSpaRoutingFilter as WebFlux WebFilter                                                                                                                                         | 🔧 refactoring | backlog | platform/webflux-migration-plan.md              |
| P-3  | Create ReactiveMessageHelper for protobuf/JSON conversion in WebFlux                                                                                                                     | ✨ feature      | backlog | platform/webflux-migration-plan.md              |
| P-4  | Replace MockMvc with WebTestClient across all affected test suites                                                                                                                       | 🧪 test        | backlog | platform/webflux-migration-plan.md              |
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
| P-32 | Add reusable build-logic plugin for controlled multi-edition Spring Boot `bootDist`/`installBootDist` outputs in `apps/mill-service` (single app module, edition-specific install dirs) | ✨ feature      | backlog | `docs/design/build-system/gradle-editions.md`   |
| P-33 | Explore Docker Buildx Bake to reduce Docker image build time across services and pipelines                                                                                                 | 💡 improvement | backlog | **TBD (new WI)**                                |
| P-34 | WebFlux migration + REST inventory + `@PreAuthorize` stubs on all HTTP operations (**WI-220**–**WI-228**)                                                                                  | 🔧 refactoring | planned | [`planned/webflux-migration-and-method-security/STORY.md`](planned/webflux-migration-and-method-security/STORY.md) |
| P-35 | Extract a shared Spring web module for reusable REST advice and standard error payload mapping across services                                                                             | ✨ feature      | backlog | `platform/rest-exception-handling-pattern.md`   |
| P-37 | Named `mill.cloud.*` profiles for config resource loading: multiple auth endpoints per provider (e.g. metadata seeds on Azure account A, flow descriptor paths on account B); profile selection on `s3://` / `gs://` / `azure-blob://` URLs or explicit location→profile map | ✨ feature      | backlog | [`platform/cloud-resource-loading.md`](../design/platform/cloud-resource-loading.md) |
| P-39 | Event bus domain producers: metadata change bridge, artifact persist hooks, domain `EventPayload` types | ✨ feature | backlog | [`platform/general-event-bus.md`](../design/platform/general-event-bus.md) — follow-on `event-bus-domain-producers` |
| P-40 | Event bus side consumers: search index, SQL→schema relations, value-mapping refresh | ✨ feature | backlog | [`platform/general-event-bus.md`](../design/platform/general-event-bus.md) — follow-on `event-bus-consumers` |

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

| #    | Item                                                                                                              | Type           | Status  | Source                                   |
| ---- | ----------------------------------------------------------------------------------------------------------------- | -------------- | ------- | ---------------------------------------- |
| R-6  | Fix mill.security.enabled vs mill.security.enable inconsistency in test configs                                   | 🐛 fix         | backlog | refactoring/05-configuration-keys.md     |
| R-7  | Remove ghost keys (data-bot.*, jet-grpc.*) or implement their Java consumers                                      | 🐛 fix         | backlog | refactoring/05-configuration-keys.md     |
| R-8  | Add missing additional-spring-configuration-metadata.json for 28+ mill.* keys                                     | 💡 improvement | backlog | refactoring/05-configuration-keys.md     |
| R-9  | Remove or use @ConditionalOnTestKit (dead annotation)                                                             | 🐛 fix         | backlog | refactoring/05-configuration-keys.md     |
| R-10 | Fix mill.services.jet-http.enable type in metadata JSON (String to Boolean)                                       | 🐛 fix         | backlog | refactoring/05-configuration-keys.md     |
| R-11 | Review commented-out tests in AI and JDBC driver: delete, move, or re-enable                                      | 🧪 test        | backlog | refactoring/06-test-module-inventory.md  |
| R-12 | Fix JDBC driver integration test infrastructure (re-enable disabled testIT classes)                               | 🧪 test        | backlog | refactoring/06-test-module-inventory.md  |
| R-13 | Reduce technical debt: review 119 files with TODOs/FIXMEs                                                         | 🔧 refactoring | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md    |
| R-14 | Refactor data module Spring configuration: review and implement in mill-data-autoconfigure                        | 🔧 refactoring | backlog | refactoring/05-configuration-keys.md     |

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
| U-12 | Redesign optional per-facet-type **view** and **edit** component registration (replacing removed bespoke facet presenters); descriptor-driven fallback remains default | ✨ feature | backlog | `design/ui/facet-view-customization.md` |
| U-14 | mill-ui Analysis modes: SQL Analysis for technical users plus Visual Analysis path/board authoring for business users (**WI-267**–**WI-272**) | ✨ feature | planned | [`planned/mill-ui-visual-analysis-modes/STORY.md`](planned/mill-ui-visual-analysis-modes/STORY.md) |
| U-18 | Model explorer authorized scope management: RBAC for which scopes a user may use, add/switch scopes in UI (not full registry) | ✨ feature | backlog | follow-up to **U-17** / WI-378 deferred scope |
| U-19 | Context relations platform: generic related-object persistence, read API, UI provider; **analysis inline chat UI presence restore** after reload (**WI-408**+) | ✨ feature | planned | [`planned/context-relations/STORY.md`](planned/context-relations/STORY.md) — defers from [`analysis-inline-chat-foundation`](in-progress/analysis-inline-chat-foundation/STORY.md) |

---

## persistence — Persistence Layer

Design reference: [`docs/design/persistence/persistence-overview.md`](../design/persistence/persistence-overview.md)

Delivery order: PS-1 → PS-3 → PS-2 → PS-4 → PS-5 → PS-6/PS-7 → PS-8

| # | Item | Type | Status | Source | Domain |
|---|------|------|--------|--------|--------|
| PS-4a | Implement real `ArtifactRelationIndexer` contract and first concrete indexer | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4b | Implement `RelationStore` plus in-memory adapter for derived relation projections | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4c | Define derived relation model for `conversation -> object`, `artifact -> object`, and `run -> object` edges | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4d | Implement artifact-type-specific extraction logic for SQL, metadata-capture, and value-mapping artifacts | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4e | Implement rebuild/indexing flow from artifact history into derived relation projections | ✨ feature | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-4f | Add tests for relation derivation, persistence, rebuild, and observer/indexer integration | 🧪 test | backlog | `design/agentic/v3-persistence-lanes.md` | ai/v3 |
| PS-8 | Implement source definition persistence: CRUD API and programmatic builders for connection specs, format configs, blob source roots; Flyway migration; adapter in `mill-persistence` | ✨ feature | backlog | `source/flow-kt-design.md` | source |

---

## security — Authentication, Identity, and Access

Design reference: [`docs/design/security/auth-profile-pat-architecture.md`](../design/security/auth-profile-pat-architecture.md)
Implementation: [`docs/design/security/user-identity-jpa-implementation.md`](../design/security/user-identity-jpa-implementation.md)

| #      | Item | Type | Status | Source |
|--------|------|------|--------|--------|
| SEC-2  | PAT (Personal Access Token) issuance, secure hashed-token storage, and bearer token validation provider in `SecurityFilterChain`; PAT management UI (Access tab) | ✨ feature | backlog | `design/security/auth-profile-pat-architecture.md` |
| SEC-4  | OAuth/SSO federation: OIDC/Entra/GitHub/Google token validation; `OAuth2UserService` calling `resolveOrProvision`; provider UI buttons | ✨ feature | backlog | `design/security/auth-profile-pat-architecture.md` |
| SEC-5  | "Forgot password?" flow: reset request endpoint, email delivery, token validation, password update; activate dead UI link | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-6  | Admin user management: API + UI for creating, disabling, locking/unlocking users; manage `locked`/`validated` flags with `lockDate` and `lockReason` | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-7  | Email verification: `validated=FALSE` on registration, confirmation email, flip to `TRUE` on link visit; block login until validated | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-8  | Production password hasher: `BCryptPasswordHasher` bean replacing `NoOpPasswordHasher`; migration helper to re-hash existing `{noop}` credentials on next login | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |
| SEC-9  | Brute-force protection: login failure counter per subject; auto-set `locked=TRUE` after N failures; configurable threshold via `mill.security.*` | ✨ feature | backlog | `design/security/user-identity-jpa-implementation.md` |

---

## Summary

Counts are **open** rows only (`backlog` | `planned` | `in-progress`). Pruned at **0.8.0** release housekeeping (2026-07-07).

| Category    | Items |
| ----------- | ----- |
| data        | 7     |
| ai          | 38    |
| client      | 15    |
| metadata    | 20    |
| platform    | 27    |
| publish     | 4     |
| refactoring | 9     |
| source      | 14    |
| ui          | 13    |
| persistence | 7     |
| security    | 7     |
| **Total**   | **161** |
