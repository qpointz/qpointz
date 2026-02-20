# Design Backlog

Consolidated tracking list of planned work across all design categories. Items are
milestone-selectable deliverables extracted from design documents in this folder.

**Legend:**
- **Status**: `backlog` | `planned` | `in-progress` | `done` | `cancelled`
- **Type**: ✨ feature | 💡 improvement | 🐛 fix | 🔧 refactoring | 🧪 test | 📝 docs
- **Source**: design document the item originates from (relative to `docs/design/`)

---

## data — Data Layer

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| D-1 | Add JSON logical type (Phase A): proto, Java core, backends, clients | ✨ feature | backlog | data/complex-type-support.md |
| D-2 | Add LIST native vector type (Phase B): proto, ListVector, producers, readers | ✨ feature | backlog | data/complex-type-support.md |
| D-3 | Add MAP native vector type (Phase B): proto, MapVector, single-segment PathSegment | ✨ feature | backlog | data/complex-type-support.md |
| D-4 | Add OBJECT native vector type (Phase B): MapVector with multi-segment PathSegment paths | ✨ feature | backlog | data/complex-type-support.md |
| D-5 | Implement PathSegment reconstruction and flattening algorithms (Java + Python) | ✨ feature | backlog | data/complex-type-support.md |
| D-6 | Add JSON/LIST/MAP/OBJECT to all type mapping tables in mill-type-system reference | 📝 docs | backlog | data/mill-type-system.md |
| D-7 | Fix JDBC type mapping: BOOL should map to Types.BOOLEAN not BLOB | 🐛 fix | backlog | data/mill-type-system.md |
| D-8 | Implement server GetDialect RPC and handshake supports_dialect flag | ✨ feature | backlog | data/mill-type-system.md |

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

---

## client — Client Libraries

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| C-1 | Implement mill/sql package with MillDialectDescriptor model and CALCITE_DEFAULT (Phase 9A) | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-2 | Add quote_identifier(), qualify() helpers and type mappings (to_sa_type, to_ibis_dtype) | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-3 | Implement DialectTester with ~80 SQL queries and DialectReport (Phase 9B) | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-4 | Populate full function catalog (scalar, aggregate, window) from tester output | ✨ feature | backlog | client/py-sql-dialect-plan.md |
| C-5 | Auto-generate py-sql-dialect-report.md with feature matrix | 📝 docs | backlog | client/py-sql-dialect-plan.md |
| C-6 | Update all 10 dialect YAMLs with expanded schema (Phase 3 of YAML schema) | 🔧 refactoring | backlog | client/sql-dialect-yaml-schema.md |
| C-7 | Rewrite AI consumer: replace SpecSqlDialect with typed prompt builder | 🔧 refactoring | backlog | client/sql-dialect-yaml-schema.md |
| C-8 | Fix H2 dialect YAML: case, missing/wrong functions, paging, parameter signatures | 🐛 fix | backlog | client/sql-dialect-yaml-schema.md |
| C-9 | Remove deprecated YAML fields (identifiers.case, paging.limit/top, ordering) | 🔧 refactoring | backlog | client/sql-dialect-yaml-schema.md |
| C-10 | Implement ibis BaseBackend wrapping MillClient (Phase 10) | ✨ feature | backlog | client/py-implementation-plan.md |
| C-11 | Map ibis expressions to Calcite-compatible SQL via sqlglot | ✨ feature | backlog | client/py-implementation-plan.md |
| C-12 | Implement PEP 249 DBAPI 2.0 shim (Phase 11) | ✨ feature | backlog | client/py-implementation-plan.md |
| C-13 | Implement SQLAlchemy MillDialect and MillSQLCompiler with schema introspection | ✨ feature | backlog | client/py-implementation-plan.md |
| C-14 | Register SQLAlchemy entry points for mill+grpc and mill+http | ✨ feature | backlog | client/py-implementation-plan.md |
| C-15 | Fix MillServerError: call super().__init__(message) | 🐛 fix | backlog | client/py-cold-start.md |
| C-16 | Fix Python type mappings: BOOL->BOOLEAN, identifier quoting from dialect YAML | 🐛 fix | backlog | client/py-sql-dialect-plan.md |

---

## metadata — Metadata Subsystem

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| M-1 | Create ValueMappingFacet in mill-ai-core and register via AiMetadataConfiguration | ✨ feature | backlog | metadata/metadata-ui-implementation-plan.md |
| M-2 | Create MetadataAdapterService implementing MetadataProvider over MetadataService | 🔧 refactoring | backlog | metadata/metadata-ui-implementation-plan.md |
| M-3 | Update MetadataConfiguration: replace deprecated beans, wire adapter service | 🔧 refactoring | backlog | metadata/metadata-ui-implementation-plan.md |
| M-4 | Migrate value mapping data from legacy YAML to facet format | ✨ feature | backlog | metadata/metadata-ui-implementation-plan.md |
| M-5 | Update AI components and tests to work via MetadataAdapterService | 🔧 refactoring | backlog | metadata/metadata-ui-implementation-plan.md |
| M-6 | Add value mapping REST API endpoints (GET/POST mappings, resolve term) | ✨ feature | backlog | metadata/metadata-ui-implementation-plan.md |
| M-7 | Display value mappings in metadata browser UI | ✨ feature | backlog | metadata/metadata-ui-implementation-plan.md |
| M-8 | Introduce ValueResolver abstraction with feature flag (legacy/faceted/hybrid) | 🔧 refactoring | backlog | metadata/metadata-implementation-roadmap.md |
| M-9 | Add parity tests: legacy vs facet value resolution | 🧪 test | backlog | metadata/metadata-implementation-roadmap.md |
| M-10 | Implement EnrichmentFacet, EnrichmentService, and approval workflow | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-11 | Implement JPA repository (document-style, JSONB) and CompositeMetadataRepository | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-12 | Implement MetadataSyncService for file<->JPA synchronization | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-13 | Remove legacy adapter layer: migrate all usages to MetadataService directly | 🔧 refactoring | backlog | metadata/metadata-provider-refactoring-plan.md |
| M-14 | Migrate SchemaMessageSpec to MetadataEntity; remove legacy model classes | 🔧 refactoring | backlog | metadata/metadata-provider-refactoring-plan.md |
| M-15 | Implement full-text and facet-aware search (Postgres/Elastic/Lucene) | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-16 | Implement DataQualityFacet and rule execution engine | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-17 | Implement SemanticFacet with vector store integration | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-18 | Implement LineageFacet and lineage graph API | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-19 | Implement scope resolution (user > team > role > global) with security context | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-20 | Add UI editing for metadata facets (DescriptiveFacet, RelationFacet, ConceptFacet, etc.) | ✨ feature | backlog | metadata/metadata-implementation-roadmap.md |
| M-21 | Fix MessageHelper parse error messages (generic HandshakeResponse) | 🐛 fix | backlog | metadata/metadata-service-design.md |
| M-22 | Remove ProtobufUtils dead code and register HTTP ServiceDescriptor | 🐛 fix | backlog | metadata/metadata-service-design.md |

---

## platform — Infrastructure and Cross-Cutting

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| P-1 | Migrate controllers to WebFlux: reactive repositories, services, Mono/Flux returns | 🔧 refactoring | backlog | platform/webflux-migration-plan.md |
| P-2 | Rewrite GrinderUIFilter as WebFlux WebFilter | 🔧 refactoring | backlog | platform/webflux-migration-plan.md |
| P-3 | Create ReactiveMessageHelper for protobuf/JSON conversion in WebFlux | ✨ feature | backlog | platform/webflux-migration-plan.md |
| P-4 | Replace MockMvc with WebTestClient across all affected test suites | 🧪 test | backlog | platform/webflux-migration-plan.md |
| P-5 | Spring Boot 4.0 pre-migration cleanup: hardcoded versions, javax->jakarta, spring.factories | 🐛 fix | backlog | platform/spring4-migration-plan.md |
| P-6 | Remove net.devh gRPC starter; reimplement gRPC server with raw grpc-java | 🔧 refactoring | backlog | platform/spring4-migration-plan.md |
| P-7 | Migrate Jackson 2.x to Jackson 3.0 (ObjectMapper->JsonMapper, package changes) | 🔧 refactoring | backlog | platform/spring4-migration-plan.md |
| P-8 | Upgrade Spring AI to 2.0.x and SpringDoc OpenAPI to 3.x | 💡 improvement | backlog | platform/spring4-migration-plan.md |
| P-9 | Review and fix Spring Security 7.0 breaking changes | 🔧 refactoring | backlog | platform/spring4-migration-plan.md |
| P-10 | Implement MCP Data Provider per specification (resources, tools, prompts) | ✨ feature | backlog | platform/mcp.md |
| P-11 | Create proto data_export_svc.proto and implement Data Export Service | ✨ feature | backlog | platform/data-export-service.md |
| P-12 | Use combined Calcite operator table (STANDARD + POSTGRESQL) | 💡 improvement | backlog | platform/calcite-dialect-comparison.md |
| P-13 | Add custom Calcite operators for AGE(timestamp,timestamp) and ILIKE | ✨ feature | backlog | platform/calcite-dialect-comparison.md |
| P-14 | Fix postgres.yml type mappings (COUNT->BIGINT, EXTRACT->NUMERIC, polymorphic) | 🐛 fix | backlog | platform/calcite-dialect-comparison.md |
| P-15 | Resolve duplicate mill.security.authorization.policy prefix across modules | 🐛 fix | backlog | platform/CONFIGURATION_INVENTORY.md |
| P-16 | Replace @Qualifier("LOJOKOJ") placeholder with meaningful qualifier | 🐛 fix | backlog | platform/CONFIGURATION_INVENTORY.md |
| P-17 | Complete RAG implementation: vector store value mapper, integration tests | ✨ feature | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md |
| P-18 | Add custom metrics, distributed tracing (OpenTelemetry), and structured logging | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md |
| P-19 | Implement query result caching, metadata caching, optimize connection pooling | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md |
| P-20 | Add rate limiting, audit logging, and policy testing framework | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md |
| P-21 | Add architecture diagrams, user guides, and troubleshooting guide | 📝 docs | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md |
| P-22 | Consider compression for vector blocks and serialization performance metrics | 💡 improvement | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md |

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

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| R-1 | Move services/mill-metadata-service to metadata/mill-metadata-service (iteration 15) | 🔧 refactoring | backlog | refactoring/01-iterations.md |
| R-2 | Remove Spring contamination from mill-metadata-core | 🔧 refactoring | backlog | refactoring/04-dependency-graph.md |
| R-3 | Remove Spring contamination from mill-data-backends | 🔧 refactoring | backlog | refactoring/04-dependency-graph.md |
| R-4 | Fix mill.backend.jdbc.multi-shema typo to multi-schema | 🐛 fix | backlog | refactoring/05-configuration-keys.md |
| R-5 | Resolve output-schema vs target-schema inconsistency in YAML and Java binding | 🐛 fix | backlog | refactoring/05-configuration-keys.md |
| R-6 | Fix mill.security.enabled vs mill.security.enable inconsistency in test configs | 🐛 fix | backlog | refactoring/05-configuration-keys.md |
| R-7 | Remove ghost keys (data-bot.*, jet-grpc.*) or implement their Java consumers | 🐛 fix | backlog | refactoring/05-configuration-keys.md |
| R-8 | Add missing additional-spring-configuration-metadata.json for 28+ mill.* keys | 💡 improvement | backlog | refactoring/05-configuration-keys.md |
| R-9 | Remove or use @ConditionalOnTestKit (dead annotation) | 🐛 fix | backlog | refactoring/05-configuration-keys.md |
| R-10 | Fix mill.services.jet-http.enable type in metadata JSON (String to Boolean) | 🐛 fix | backlog | refactoring/05-configuration-keys.md |
| R-11 | Review commented-out tests in AI and JDBC driver: delete, move, or re-enable | 🧪 test | backlog | refactoring/06-test-module-inventory.md |
| R-12 | Fix JDBC driver integration test infrastructure (re-enable disabled testIT classes) | 🧪 test | backlog | refactoring/06-test-module-inventory.md |
| R-13 | Reduce technical debt: review 119 files with TODOs/FIXMEs | 🔧 refactoring | backlog | platform/CODEBASE_ANALYSIS_CURRENT.md |
| R-14 | Refactor data module Spring configuration: review and implement in mill-data-autoconfigure | 🔧 refactoring | backlog | refactoring/05-configuration-keys.md |

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
| S-8 | Implement source persistence, CRUD API, programmatic builders (Phase 6) | ✨ feature | backlog | source/flow-kt-design.md |
| S-9 | Implement S3BlobSource, AzureBlobSource, HdfsBlobSource storage backends | ✨ feature | backlog | source/flow-kt-design.md |
| S-10 | Implement HivePartitionTableMapper and GlobTableMapper | ✨ feature | backlog | source/flow-kt-design.md |

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

---

## Summary

| Category    | Total   | ✨ feature | 💡 improvement | 🐛 fix | 🔧 refactoring | 🧪 test | 📝 docs |
| ----------- | ------- | --------- | -------------- | ------ | -------------- | ------- | ------- |
| data        | 8       | 6         | 0              | 1      | 0              | 0       | 1       |
| ai          | 23      | 16        | 4              | 1      | 2              | 0       | 0       |
| client      | 16      | 9         | 0              | 3      | 3              | 0       | 1       |
| metadata    | 22      | 13        | 0              | 2      | 6              | 1       | 0       |
| platform    | 22      | 6         | 7              | 4      | 4              | 1       | 0       |
| publish     | 4       | 1         | 2              | 0      | 0              | 0       | 1       |
| refactoring | 14      | 0         | 1              | 6      | 4              | 2       | 1       |
| source      | 10      | 4         | 3              | 1      | 1              | 0       | 0       |
| ui          | 10      | 4         | 4              | 0      | 1              | 1       | 0       |
| **Total**   | **129** | **59**    | **21**         | **18** | **21**         | **5**   | **4**   |
