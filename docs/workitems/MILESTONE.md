# Milestones

**Draft release notes.** Treat this file as the **working draft** of **[`releases/RELEASE-x.y.z.md`](releases/)** for the **next** version only: the **`## x.y.z`** block below is pre-release material to be **promoted and polished** into **`RELEASE-x.y.z.md`** when git tag **`vx.y.z`** is cut. It must **not** retain sections for **already shipped** versions — those live only under **`releases/`** (for example **[`releases/RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)** for **`v0.7.0`**). After tagging, **`MILESTONE.md`** is reset to the **following** milestone draft only — see [`RULES.md`](RULES.md) § **Milestone ledger (`MILESTONE.md`)** and § **Release (version) process**.

**Baseline (work since last shipped tag):** **`v0.7.0`** — e.g. `git log v0.7.0..HEAD`.

## 0.8.0

**Target date:** TBD — **not released.** Companion stub: [`releases/RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md) (filled from this draft at tag time). **§ Completed** below = changes merged on `dev` **after** **`v0.7.0`** until **`v0.8.0`** is cut; narrative for **`v0.7.0`** itself is in **[`releases/RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)** only.

### Completed

**Draft body for [`RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md).** **0.7.0** shipped as **[`RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)**; this section accumulates the **0.8.0** delta until release. **§ Completed** = work **since `v0.7.0`**. Use **§ Archived stories** for closure-level summaries + links, **§ Cumulative WI deliveries** for the running WI checklist, and **[`BACKLOG.md`](BACKLOG.md)** for row-level status until **release housekeeping** (see [`RULES.md`](RULES.md) § **Release (version) process**).

#### Archived stories (closure date, newest first)

- **Mill application event bus foundation** (`general-event-bus`, closed **2026-06-19**): [`in-progress/general-event-bus/STORY.md`](in-progress/general-event-bus/STORY.md). **WI-311**–**WI-314** — Spring-free `:core:mill-events` contracts (`Event`, `EventPublisher`, `EventTransport`, `EventRouter`, `EventConsumer`, per-type subscriptions, DSL builder, `PublishMode`/`ProcessingMode` two-axis dispatch); `:core:mill-events-autoconfigure` with `mill.events.*` properties, `InMemoryEventTransport`, `SpringEventTransport`, dynamic consumer bean collection, `testIT` proving fan-out + failure isolation + async. Design [`docs/design/platform/general-event-bus.md`](../design/platform/general-event-bus.md). **BACKLOG** **P-38** `done`.

- **AI chat table naming + CASCADE delete** (`ai-chat-table-naming`, closed **2026-06-19**): [`completed/20260619-ai-chat-table-naming/STORY.md`](completed/20260619-ai-chat-table-naming/STORY.md). **WI-323** — Flyway **V11** cosmetic renames (`ai_chat_*` satellites); **WI-324** — **V12** orphan cleanup + `ON DELETE CASCADE` from `ai_chat`; `JpaConversationStore.delete`, `JpaChatDeleteCascadeIT`; canonical [`db-naming-convention.md`](../design/persistence/db-naming-convention.md). **BACKLOG** **A-76**, **A-77** `done`.

- **AI chat persistence** (`ai-chat-persistence`, closed **2026-06-19**): [`completed/20260619-ai-chat-persistence/STORY.md`](completed/20260619-ai-chat-persistence/STORY.md). **WI-317**–**WI-321** — unified **`ai_chat`** / **`ai_chat_turn`** (V10), JPA `ChatRegistry`, per-turn `profile_id`, ownership (**WI-318**, **A-75**), ephemeral artifact routing (**WI-319**), `AiChatPersistenceIT` + design docs (**WI-320**), capability descriptor YAML doc (**WI-321**). **WI-322** (pgvector V9) delivered in [`completed/20260618-pgvector-flyway-extension/`](completed/20260618-pgvector-flyway-extension/STORY.md). **`mill-persistence-autoconfigure`** wired into mill-service runtime.

- **pgvector Flyway extension** (`pgvector-flyway-extension`, closed **2026-06-18**): [`completed/20260618-pgvector-flyway-extension/STORY.md`](completed/20260618-pgvector-flyway-extension/STORY.md). **WI-322** — Flyway **V9** Java migration `V9__EnsurePgvectorExtension`: `CREATE EXTENSION IF NOT EXISTS vector` on PostgreSQL when available; **H2 never fails Flyway**; soft-fail when extension packages missing; hard-fail on permission denied. [`PersistenceAutoConfiguration`](../../persistence/mill-persistence-autoconfigure/src/main/kotlin/io/qpointz/mill/persistence/configuration/PersistenceAutoConfiguration.kt), [`apps/mill-service/application.yml`](../../apps/mill-service/src/main/resources/application.yml) profile comment. Delivered with **ai-chat-persistence** branch (MR !397).

- **Flow TranslatableTable scan** (`flow-translatable-table-scan`, closed **2026-06-18**): [`completed/20260618-flow-translatable-table-scan/STORY.md`](completed/20260618-flow-translatable-table-scan/STORY.md). **WI-311**, **WI-314**–**WI-316** — `FlowTableScan` / `TranslatableTable`, format-backed table statistics, `FlowEnumerableRuleSets` hash-join policy on `FlowTableScan.register()`, Skymill join perf IT + `mill-source-calcite.md`. Pushdown / Parquet projection (**WI-312**, **WI-313**) spun out to [`planned/flow-scan-pushdown/STORY.md`](planned/flow-scan-pushdown/STORY.md). Design [`docs/design/source/mill-source-calcite.md`](../design/source/mill-source-calcite.md). **BACKLOG** **D-9** `done`, **D-10** `planned`.

- **Artefact emit contract + scenario harness** (`ai-artifact-emit-contract`, closed **2026-06-16**): [`completed/20260616-ai-artifact-emit-contract/STORY.md`](completed/20260616-ai-artifact-emit-contract/STORY.md). **WI-300**–**WI-308**, **WI-310** — YAML-driven `ScenarioPack` replay harness in `mill-ai-test` (`ScriptedAgentRunner`, `ConversationRegressionRecord`, baseline comparator); `ArtifactDescriptorRegistry` + `ArtifactEmissionCoordinator` emit contract in `mill-ai`; registry-driven router + SSE bridge; capability manifests + `data-analysis` profile; POC scenario packs (acceptance); supplementary unit/service ITs; live YAML packs with `ProvidedAgentRunner` (gated testIT). Design [`docs/design/agentic/ai-v3-conversation-scenarios.md`](../design/agentic/ai-v3-conversation-scenarios.md), [`docs/design/agentic/artifact-emit-contract.md`](../design/agentic/artifact-emit-contract.md), [`docs/design/agentic/artifact-foundation.md`](../design/agentic/artifact-foundation.md). **BACKLOG** **A-92** `done`.

- **Chat artefact presentation (sql-view restart)** (`ai-sql-view-restart`, closed **2026-06-12**): [`completed/20260612-ai-sql-view-restart/STORY.md`](completed/20260612-ai-sql-view-restart/STORY.md). **WI-289**–**WI-298** — mill-ui chat artefact presentation on artefacts emission foundation: `chatArtifactTreatments`, condensed SQL/data preview, expand pane, shared **`QueryDataView`**, GET replay + attach-result wire, chat toolbar (Run all, mid-chat profile switch), content-pane header chrome; supersedes abandoned **`feat/ai-chat-sql-result-view`**. Design [`docs/design/ai/chat-artefact-architecture.md`](../design/ai/chat-artefact-architecture.md), [`docs/design/ui/mill-ui/GENERAL-CHAT-DESIGN.md`](../design/ui/mill-ui/GENERAL-CHAT-DESIGN.md). **BACKLOG** **U-15** `done`.

**Scope:** this subsection lists **only** story archives whose **`docs/workitems/completed/…` tree first landed after git tag `v0.7.0`** (verify with e.g. `git log v0.7.0..HEAD -- docs/workitems/completed/`). Archives dated **2026-03-30** through **2026-04-02** — including **metadata rework**, **metadata/UI**, **flow UI facets**, **typed URNs** — were committed **on or before** `v0.7.0`; they remain under [`completed/`](completed/) for traceability and are summarized in **[`releases/RELEASE-0.7.0.md`](releases/RELEASE-0.7.0.md)** (see also **[`completed/README.md`](completed/README.md)**). They are **not** duplicated here as **0.8.0** delta.

- **AI configuration restructure** (`ai-configuration-restructure`, closed **2026-06-10**): [`completed/20260610-ai-configuration-restructure/STORY.md`](completed/20260610-ai-configuration-restructure/STORY.md). **WI-284**–**WI-288** — layered **`mill.ai.*`**: `providers` (+ `type`), `models.chat` / `models.embedding`, optional **`vector-stores`** registry, **`data.embedding.<profile>`** pipelines (sources, refresh, vector store), **`chat`** capability hooks; clean break from legacy `mill.ai.model`, `embedding-model`, `value-mapping`, singleton `vector-store`; operator YAML + GCP template migration; design + public docs. **BACKLOG** **A-91** `done`.

- **mill-ui Analysis full stack** (`mill-ui-analysis-full-stack`, closed **2026-06-09**): [`completed/20260609-mill-ui-analysis-full-stack/STORY.md`](completed/20260609-mill-ui-analysis-full-stack/STORY.md). **WI-256**–**WI-260** — Flyway **`saved_query`** catalog + JPA port, **`mill-analysis-service`** REST **`/api/v1/analysis/**`** (dialect + saved-query CRUD), **`mill-service`** wiring, HTTP-only **`queryService`**, CodeMirror **`SqlCodeEditor`** with schema completions, Skymill-style **`testIT`** for catalog REST + auth. **BACKLOG** **U-13** `done`.

- **Cloud blob sources** (`cloud-blob-source`, closed **2026-05-14**): [`completed/20260514-cloud-blob-source/STORY.md`](completed/20260514-cloud-blob-source/STORY.md). **WI-262** (S3)–**WI-265** (wiring/docs), **WI-271** — `cloud/{aws,gcp,azure}` **`BlobSource`** + Boot autoconfigure, Skymill **`testIT`** over emulators, **`DescriptorPlaceholderResolver`** + **`SecretProvider`** SPI, **`mill.data.backend.metadata.*`** gating and facet redaction, design + public storage docs. **BACKLOG** **S-9** `done`.

- **Cloud configuration files / resource loading** (`cloud-resource-loading`, closed **2026-05-14**): [`completed/20260514-cloud-resource-loading/STORY.md`](completed/20260514-cloud-resource-loading/STORY.md). **WI-274**–**WI-279** — `BackendResourceLoader` + Spring adapter; flow `sources` and metadata `seed.resources` as Spring locations; **`ProtocolResolver`** implementations for **`s3://`**, **`gs://`**, **`azure-blob://`** in `mill-cloud-*-autoconfigure`; **`DefaultResourceLoader`** composition for metadata seeds and flow reads in servlet apps; emulator **`testIT`** + flow/metadata integration tests; design [`docs/design/platform/cloud-resource-loading.md`](../design/platform/cloud-resource-loading.md); public updates to flow and metadata operator guides.

- **Query result execution service** (`query-result-execution-service`, closed **2026-05-11**): [`completed/20260511-query-result-execution-service/STORY.md`](completed/20260511-query-result-execution-service/STORY.md). **WI-262**–**WI-265** — **`mill-data-query`** programmatic sessions (**`VectorBlock`** paging, Caffeine eviction, marshaller **`ServiceLoader`** SPI), **`mill-data-query-service`** REST **`/api/v1/query/**`** (query-driven **`GET`**, **`DELETE`** deallocation), **`mill-service`** + autoconfigure wiring, Skymill **`testIT`**; **mill-ui** Analysis **`queryService`** + BACKEND doc alignment. Design [`docs/design/platform/query-result-execution-service.md`](../design/platform/query-result-execution-service.md). **BACKLOG** **D-8** `done`.

- **Streaming HTTP export service** (`streaming-export-service`, closed **2026-05-07**): [`completed/20260507-streaming-export-service/STORY.md`](completed/20260507-streaming-export-service/STORY.md). **WI-250**–**WI-261** — **`mill-export-service`** **`/services/export`** (catalog, **`GET`** table export via Substrait named scan, **`POST /sql`** streaming export), **`ExportFormatProvider`** SPI + **`ExportFormatRegistry`** bean, **`ExportVectorBlockSource`** on **`DataOperationDispatcher`**, **`mill-data-format-json`** and format encoder adapters, OpenAPI **`@Tag("export")`**, **`/.well-known/mill`** **`data-export`** descriptor; **mill-ui** Analysis result download + Model table **Export** split button (**`modelTableExportEnabled`**). Design [`docs/design/platform/export-service.md`](../design/platform/export-service.md), configuration keys in [`docs/design/refactoring/05-configuration-keys.md`](../design/refactoring/05-configuration-keys.md). **BACKLOG** **D-7**, **P-36** `done`.

- **ai-v3 — mill-ui general chat** (`ai-v3-mill-ui-general-chat`, closed **2026-05-06**): [`completed/20260506-ai-v3-mill-ui-general-chat/STORY.md`](completed/20260506-ai-v3-mill-ui-general-chat/STORY.md). **WI-229**–**WI-233** — **mill-ui** general chat on **`/api/v1/ai/chats`** (list, create, detail, rename, delete, browser SSE with forward-compatible **`item.*`** handling), **`ChatContext`** server source of truth when REST mode is active, optional **`chatAgentPicker`**, structured artefact extension seam (**`WI-231`**) + **`AssistantReplyRouter`**, chat metadata-scope design (**`WI-233`**), Vitest coverage (**`WI-232`**), shareable routes **`/chat/:conversationId`**; AI v3 **LangChain4j** / **SSE mapper** updates for structured completion summaries. Design [`docs/design/agentic/ai-v3-chat-transport-extensions.md`](../design/agentic/ai-v3-chat-transport-extensions.md), [`docs/design/ui/mill-ui/GENERAL-CHAT-DESIGN.md`](../design/ui/mill-ui/GENERAL-CHAT-DESIGN.md). **BACKLOG** **U-11** `done`.

- **Spring Boot 4 — migration day 2** (`spring4-migration-day-2`, closed **2026-04-30**): [`completed/20260430-spring4-migration-day-2/STORY.md`](completed/20260430-spring4-migration-day-2/STORY.md). **WI-201**–**WI-209** — Spring Boot **4.0.6**, Spring Framework **7** / Spring Security **7**, Jackson **3** (`tools.jackson`, `JsonMapper`), SpringDoc **3.0.3**, Spring AI **2.0.0-M5**, Boot **4** starter coordinate renames, auth/persistence and transport **`testIT`** alignment (incl. JDBC **`EmbeddedSkymillGrpcServer`**), full-repo **`clean build`** / **`test`** / **`testIT`** green, migration plan + public **platform runtime** docs. Design [`docs/design/platform/spring4-migration-plan.md`](../design/platform/spring4-migration-plan.md). **BACKLOG** **P-5**–**P-9** `done` for platform spring4 stories (**P-5** pre-migration; **P-7**–**P-9** day 2).

- **Spring Boot 4 — pre-migration cleanup (3.5.x)** (`spring4-pre-migration-cleanup`, closed **2026-04-30**): [`completed/20260430-spring4-pre-migration-cleanup/STORY.md`](completed/20260430-spring4-pre-migration-cleanup/STORY.md). **WI-097**–**WI-104** — gRPC catalog hygiene, Jakarta/`javax.annotation` catalog, **`spring-security-test`** BOM alignment, `misc/` README hygiene, SpringDoc catalog audit, `META-INF` autoconfigure audit, Boot 4 jump-start grep inventory, **`spring4-migration-plan.md`** Phase 1 alignment. **BACKLOG** **P-5** `done`.

- **Client error transparency (data plane)** (`client-error-transparency`, closed **2026-04-29**): [`completed/20260429-client-error-transparency/STORY.md`](completed/20260429-client-error-transparency/STORY.md). **WI-013** — RFC 9457 Problem Details on data HTTP (`AccessServiceProblemAdvice`), JDBC body parsing (`HttpMillErrorBodies` / `HttpMillClient`), `mill-py` + gRPC trailing metadata for `trace_id`. Design [`docs/design/client/client-error-transparency.md`](../design/client/client-error-transparency.md). **BACKLOG** **P-31** `done`.

- **AI v3 — facet catalog inference + metadata capabilities** (`ai-facet-catalog-inference`, closed **2026-04-28**): [`completed/20260428-ai-facet-catalog-inference/STORY.md`](completed/20260428-ai-facet-catalog-inference/STORY.md). **WI-204**–**WI-206** — `metadata` / `metadata-authoring` capabilities (`capabilities/metadata.yaml`, `metadata-authoring.yaml`), `MetadataReadPort`, `MetadataCapabilityProvider`/`MetadataAuthoringCapabilityProvider`, **`SchemaExplorationAgentProfile`**: **`metadata`** QUERY only; **`SchemaAuthoringAgentProfile`**: **`metadata`** + **`metadata-authoring`** + artifact routing (`AgentEventRouter`); Spring **`AiV3JpaRepositoriesImportSelector`** + IT excludes duplicate JPA repo bootstrap; **`SchemaAuthoringCapability`**: catalog validation for captures, **`MetadataUrns`** facet-type URNs, `resolverHint` / capture-remediation prompts; **`schema-authoring.yaml`** manifest repair (`capture_relation`). Design [`docs/design/agentic/metadata-facet-catalog-v3.md`](../design/agentic/metadata-facet-catalog-v3.md).

- **mill-py platform HTTP clients** (`mill-py-metadata-client`, closed **2026-04-24**): [`completed/20260424-mill-py-metadata-client/STORY.md`](completed/20260424-mill-py-metadata-client/STORY.md). **WI-192**–**WI-203** — shared `httpx` helpers and Jet transport refactor; `mill.metadata` / `mill.metadata.aio`, `mill.schema_explorer` async; metadata DTOs, scopes import/export, entities/facets/catalog, integration tests; canonical **GET /export** YAML+JSON and scope filter (server); bulk import + JSON export parse helpers (**WI-203**); design [`docs/design/client/mill-py-platform-http.md`](../design/client/mill-py-platform-http.md). **BACKLOG** **C-23** `done`.

- **Schema capability — platform facet reconciliation** (`schema-capability-metadata`, closed **2026-04-17**): [`completed/20260417-schema-capability-metadata/STORY.md`](completed/20260417-schema-capability-metadata/STORY.md). **WI-187**–**WI-191** — bootstrap relation payload normalization (`RelationPayloadNormalization`, `SchemaFacets`), `DescriptiveFacet` `title` alias, `SchemaCatalogPort` + adapter (`displayName`, `joinSql`, model-root relations), `capabilities/schema.yaml`, Skymill **`SchemaFacetCatalogSkymillCanonicalIT`**, AI facet URNs on **`MetadataUrns`**. Design: [`docs/design/metadata/schema-facet-ai-tool-field-mapping.md`](../design/metadata/schema-facet-ai-tool-field-mapping.md). **BACKLOG** **A-90** `done`.

- **Value mapping — metadata facets, vector lifecycle** (`value-mapping-facets-vector-lifecycle`, closed **2026-04-17**): [`completed/20260417-value-mapping-facets-vector-lifecycle/STORY.md`](completed/20260417-value-mapping-facets-vector-lifecycle/STORY.md). **WI-181** facet types + **`ValueSource`** + **`syncFromSource`**; **WI-184** refresh state + **`STALE`**; **WI-182** startup + scheduled orchestrator; **WI-185** Skymill/Moneta extras seeds; **WI-186** LangChain4j **pgvector** + Mill Service **`pgvector`** / **`ai-pgvector`**. **WI-183** (resolver / capability) remains a **follow-on** in that archive. **BACKLOG** **A-89** `done`.

- **Value mappings — persistence, sync, stack** (`implement-value-mappings`, closed **2026-04-16**): [`completed/20260416-implement-value-mappings/STORY.md`](completed/20260416-implement-value-mappings/STORY.md). **WI-175**–**WI-177** `mill.ai` providers, embedding harness, in-memory vector store; **WI-174** `ValueMappingEmbeddingRepository` + JPA/Flyway; **WI-179** `VectorMappingSynchronizer`; **WI-180** `ValueMappingService` + sync autoconfigure; **WI-178** design/inventory/public alignment; opt-in **`ChromaSkymillDistinctVectorIT`**. **BACKLOG** **A-85**–**A-88** `done`.

- **AI v3 chat — capability dependencies over HTTP** (`ai-v3-chat-capability-dependencies`, closed **2026-04-14**): [`completed/20260414-ai-v3-chat-capability-dependencies/STORY.md`](completed/20260414-ai-v3-chat-capability-dependencies/STORY.md). **WI-167**–**WI-169**, **WI-160**. **BACKLOG** **A-84** `done`.

- **Schema exploration contract, `mill-ai-v3-data`, `SqlValidator`** (`ai-v3-schema-exploration-port`, closed **2026-04-14**): [`completed/20260414-ai-v3-schema-exploration-port/STORY.md`](completed/20260414-ai-v3-schema-exploration-port/STORY.md). **WI-161**–**WI-166**. **BACKLOG** **A-83** `done`.

- **`sql-query` generate-only + `SqlValidator` + CLI** (`ai-sql-generate-capability`, closed **2026-04-13**): [`completed/20260413-ai-sql-generate-capability/STORY.md`](completed/20260413-ai-sql-generate-capability/STORY.md). **WI-156** / **WI-158** / **WI-159** — `SqlQueryCapabilityDependency(validator)` only, `AiV3SqlValidatorAutoConfiguration`, CLI highlights generated SQL, ServiceLoader registration; execution stays host-side. Same technical detail as the **WI-156 / WI-158 / WI-159** bullet under **§ Cumulative WI deliveries** below.

#### Cumulative WI deliveries (toward 0.8.0)

Individual work items and bundles merged on `dev` in support of this milestone (including items that also appear under **Archived stories** above).

- **WI-323**–**WI-324** — `ai_chat_*` satellite table renames (**V11**), `ON DELETE CASCADE` FKs + delete ITs (**V12**), [`db-naming-convention.md`](../design/persistence/db-naming-convention.md); story **`ai-chat-table-naming`**, archived [`completed/20260619-ai-chat-table-naming/STORY.md`](completed/20260619-ai-chat-table-naming/STORY.md). **BACKLOG** **A-76**, **A-77** `done`.

- **WI-317**–**WI-321** — Durable AI chat: **`ai_chat`** schema (**V10**), JPA `ChatRegistry`, per-turn profile, ownership, ephemeral artifacts, `AiChatPersistenceIT`, design + capability descriptor docs; story **`ai-chat-persistence`**, archived [`completed/20260619-ai-chat-persistence/STORY.md`](completed/20260619-ai-chat-persistence/STORY.md). **BACKLOG** **A-75** `done`. **WI-322** in pgvector archive.

- **WI-311**, **WI-314**–**WI-316** — Flow `FlowTableScan` / TranslatableTable, table statistics, `FlowEnumerableRuleSets` hash-join policy, Skymill join perf IT + design docs; story **`flow-translatable-table-scan`**, archived [`completed/20260618-flow-translatable-table-scan/STORY.md`](completed/20260618-flow-translatable-table-scan/STORY.md). **BACKLOG** **D-9** `done`. Follow-on **WI-312**–**WI-313** in [`planned/flow-scan-pushdown/STORY.md`](planned/flow-scan-pushdown/STORY.md) (**D-10**).

- **WI-300**–**WI-308**, **WI-310** — YAML scenario harness (`ScenarioPack`, `ScriptedAgentRunner`, regression records, baseline comparator) + artefact emit contract (`ArtifactDescriptorRegistry`, `ArtifactEmissionCoordinator`, router/SSE bridge, `data-analysis` profile, POC acceptance packs, live YAML packs); story **`ai-artifact-emit-contract`**, archived [`completed/20260616-ai-artifact-emit-contract/STORY.md`](completed/20260616-ai-artifact-emit-contract/STORY.md). **BACKLOG** **A-92** `done`.

- **WI-289**–**WI-298** — mill-ui chat artefact presentation on artefacts foundation: `chatArtifactTreatments`, condensed/expand SQL views, shared **`QueryDataView`**, GET replay + attach-result, chat toolbar Run all + mid-chat profile switch; story **`ai-sql-view-restart`**, archived [`completed/20260612-ai-sql-view-restart/STORY.md`](completed/20260612-ai-sql-view-restart/STORY.md). **BACKLOG** **U-15** `done`.

- **WI-284**–**WI-288** — `mill.ai` configuration restructure: property bindings (`AiConfigurationProperties`, `DataEmbeddingConfigurationProperties`), resolvers + autoconfigure wiring, operator YAML migration, tests + legacy removal, design/public/inventory docs; story **`ai-configuration-restructure`**, archived [`completed/20260610-ai-configuration-restructure/STORY.md`](completed/20260610-ai-configuration-restructure/STORY.md). **BACKLOG** **A-91** `done`.

- **WI-256**–**WI-260** — mill-ui Analysis full stack: **`saved_query`** persistence + seeds, **`/api/v1/analysis/**`** catalog + dialect, HTTP **`queryService`**, CodeMirror SQL editor, catalog REST **`testIT`**; story **`mill-ui-analysis-full-stack`**, archived [`completed/20260609-mill-ui-analysis-full-stack/STORY.md`](completed/20260609-mill-ui-analysis-full-stack/STORY.md). **BACKLOG** **U-13** `done`.

- **WI-262** (S3)–**WI-265** + **WI-271** — Cloud **`BlobSource`** (`mill-cloud-{aws,gcp,azure}-*`), emulator **`testIT`**, unified **`mill.data.backend.metadata.*`**, placeholder resolution + metadata redaction, public `sources/storages/*` docs; story **`cloud-blob-source`**, archived [`completed/20260514-cloud-blob-source/STORY.md`](completed/20260514-cloud-blob-source/STORY.md). **BACKLOG** **S-9** `done`.

- **WI-262–WI-265** — Query result execution: **`mill-data-query`** session engine + marshaller SPI, **`mill-data-query-service`** REST **`/api/v1/query/**`**, **`mill-service`** wiring, Skymill **`testIT`**, design + **mill-ui** BACKEND alignment; story **`query-result-execution-service`**, archived [`completed/20260511-query-result-execution-service/STORY.md`](completed/20260511-query-result-execution-service/STORY.md). **BACKLOG** **D-8** `done`.

- **WI-250–WI-261** — Streaming HTTP export: format SPI + registry bean, JSON export format, backend vector source, **`mill-export-service`** REST + catalog + well-known, tests/docs/config keys, mill-ui Analysis + Model export UX; story **`streaming-export-service`**, archived [`completed/20260507-streaming-export-service/STORY.md`](completed/20260507-streaming-export-service/STORY.md).

- **WI-229–WI-233** — mill-ui general chat on unified AI v3 HTTP + SSE (**`/api/v1/ai/chats`**, profiles, context sync, artefact seams, metadata-scope spec, Vitest); story **`ai-v3-mill-ui-general-chat`**, archived [`completed/20260506-ai-v3-mill-ui-general-chat/STORY.md`](completed/20260506-ai-v3-mill-ui-general-chat/STORY.md).

- **WI-097–WI-104** — Spring Boot 4 pre-migration cleanup on **3.5.x** (gRPC/`misc`/Jakarta/SpringDoc/`META-INF` hygiene, jump-start inventory, migration plan Phase 1); story **`spring4-pre-migration-cleanup`**, archived [`completed/20260430-spring4-pre-migration-cleanup/STORY.md`](completed/20260430-spring4-pre-migration-cleanup/STORY.md).

- **WI-201–WI-209** — Spring Boot **4.0.x** migration day 2 (BOM bump, Jackson 3, Spring AI **2.0.0-M5**, SpringDoc **3**, Security **7**, starter renames, transport/JDBC **`testIT`**, full CI + docs); story **`spring4-migration-day-2`**, archived [`completed/20260430-spring4-migration-day-2/STORY.md`](completed/20260430-spring4-migration-day-2/STORY.md).

- **WI-013** — Client error transparency (Problem Details HTTP, JDBC + mill-py + gRPC parity); story **`client-error-transparency`**, archived [`completed/20260429-client-error-transparency/STORY.md`](completed/20260429-client-error-transparency/STORY.md).

- **WI-204–WI-206** — AI v3 metadata facet catalog + hosts (`metadata` QUERY + `metadata-authoring` CAPTURE, `MetadataReadPort`, Spring/`AiV3` wiring, schema-authoring catalog validation + URN payloads, design + CLI notes; story **`ai-facet-catalog-inference`**, archived [`completed/20260428-ai-facet-catalog-inference/STORY.md`](completed/20260428-ai-facet-catalog-inference/STORY.md)).

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
