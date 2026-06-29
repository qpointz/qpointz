# Completed stories (archive)

Closed stories are **moved** here from `docs/workitems/<story-slug>/` — they are **not** deleted.

## Naming

Each archived story is a folder:

```text
docs/workitems/completed/YYYYMMDD-<story-slug>/
```

- **`YYYYMMDD`** — closure date (UTC unless your team standard says otherwise): the day the story is merge-ready or merged.
- **`<story-slug>`** — same lowercase hyphen-separated name the folder had while active.

## Finding the latest closures

Lexical **ascending** sort (A→Z) on folder names orders **older** dates before **newer** ones. To list **most recent first**, sort folder names **descending** (Z→A) in your file browser or IDE.

Optionally, keep a **newest-first** bullet list below as a human-maintained index (most recent at the top).

---

## Index (optional, newest first)

- [2026-06-29 — metadata-authoring-profiles (catalog-generic facet authoring, YAML profiles, lifecycle, per-capability intents; WI-354–WI-364)](20260629-metadata-authoring-profiles/STORY.md)
- [2026-06-29 — scenario-capture-export (opt-in capture + chat DB export to ScenarioPack YAML; WI-365)](20260629-scenario-capture-export/STORY.md)
- [2026-06-24 — dqm-metadata-facets (15 DQ facet types L1/L2, seeds, relplan sketches, UI schema defaults; WI-342–WI-344)](20260624-dqm-metadata-facets/STORY.md)
- [2026-06-23 — odata-service (OData v4 per-schema `.svc`, RelNode compose + Rel→Substrait adapter, Java 25; WI-324–WI-329)](20260623-odata-service/STORY.md)
- [2026-06-22 — ai-v3-mcp-server-poc (MCP capability exposure over HTTP, LangChain example; WI-325–WI-330)](20260622-ai-v3-mcp-server-poc/STORY.md)
- [2026-06-19 — ai-chat-table-naming (`ai_chat_*` renames V11, CASCADE delete V12, db naming convention; WI-323–WI-324)](20260619-ai-chat-table-naming/STORY.md)
- [2026-06-19 — ai-chat-persistence (unified `ai_chat`, ownership, ephemeral artifacts, ITs; WI-317–WI-321)](20260619-ai-chat-persistence/STORY.md)
- [2026-06-18 — pgvector-flyway-extension (Flyway V9 optional `CREATE EXTENSION vector`, H2-safe; WI-322)](20260618-pgvector-flyway-extension/STORY.md)
- [2026-06-16 — ai-artifact-emit-contract (YAML scenario harness, artefact emit contract, registry/coordinator, POC packs, live YAML; WI-300–WI-310)](20260616-ai-artifact-emit-contract/STORY.md)
- [2026-06-12 — ai-sql-view-restart (chat artefact presentation, QueryDataView, expand, profile switch; WI-289–WI-298)](20260612-ai-sql-view-restart/STORY.md)
- [2026-06-10 — ai-configuration-restructure (`mill.ai` layered config: providers, models, `data.embedding`, `vector-stores`; WI-284–WI-288)](20260610-ai-configuration-restructure/STORY.md)
- [2026-05-14 — cloud-blob-source (`cloud/{aws,gcp,azure}`, `BlobSource`, `mill.data.backend.metadata`; WI-262–WI-265, WI-271)](20260514-cloud-blob-source/STORY.md)
- [2026-05-11 — query-result-execution-service (`mill-data-query`, `/api/v1/query/**`, mill-ui Analysis; WI-262–WI-265)](20260511-query-result-execution-service/STORY.md)
- [2026-05-07 — streaming-export-service (`mill-export-service` `/services/export`, format SPI, mill-ui export; WI-250–WI-261)](20260507-streaming-export-service/STORY.md)
- [2026-05-06 — ai-v3-mill-ui-general-chat (mill-ui `/api/v1/ai/chats`, SSE, routes, artefacts; WI-229–WI-233)](20260506-ai-v3-mill-ui-general-chat/STORY.md)
- [2026-04-30 — spring4-migration-day-2 (Boot 4.0.6, Jackson 3, Spring AI 2.0.0-M5, Security 7, CI + docs; WI-201–WI-209)](20260430-spring4-migration-day-2/STORY.md)
- [2026-04-30 — spring4-pre-migration-cleanup (3.5.x hygiene before Boot 4 bump; WI-097–WI-104)](20260430-spring4-pre-migration-cleanup/STORY.md)
- [2026-04-29 — client-error-transparency (Problem Details data plane, mill-py + JDBC + gRPC parity, WI-013)](20260429-client-error-transparency/STORY.md)
- [2026-04-28 — ai-facet-catalog-inference (metadata QUERY + authoring CAPTURE, `MetadataReadPort`, facet URN captures, WI-204–WI-206)](20260428-ai-facet-catalog-inference/STORY.md)
- [2026-04-24 — mill-py-metadata-client (`mill.metadata` / `mill.schema_explorer`, canonical export/import, WI-192–WI-203)](20260424-mill-py-metadata-client/STORY.md)
- [2026-04-17 — schema-capability-metadata (relation payload normalization, `SchemaCatalogPort` + adapter, Skymill IT, `MetadataUrns`; WI-187–WI-191)](20260417-schema-capability-metadata/STORY.md)
- [2026-04-17 — value-mapping-facets-vector-lifecycle (facet types, `ValueSource`, refresh state, orchestrator, dataset seeds, pgvector `EmbeddingStore`; WI-181–WI-186)](20260417-value-mapping-facets-vector-lifecycle/STORY.md)
- [2026-04-16 — implement-value-mappings (`mill.ai` providers, embedding/vector harnesses, repository, sync, `ValueMappingService`, stack docs; WI-174–WI-180, WI-178)](20260416-implement-value-mappings/STORY.md)
- [2026-04-14 — ai-v3-chat-capability-dependencies (`CapabilityDependencyAssembler`, profile HTTP, IT/docs, CLI HTTP bench; WI-160, WI-167–WI-169)](20260414-ai-v3-chat-capability-dependencies/STORY.md)
- [2026-04-14 — ai-v3-schema-exploration-port (`SchemaCatalogPort`, `mill-ai-v3-data`, `SqlValidator`; WI-161–WI-166)](20260414-ai-v3-schema-exploration-port/STORY.md)
- [2026-04-13 — ai-sql-generate-capability (`sql-query` generate-only, SqlValidator, CLI; WI-156–WI-159)](20260413-ai-sql-generate-capability/STORY.md)
- [2026-04-02 — flow-source-ui-facets (flow inferred facets, mill-ui; WI-146–WI-150)](20260402-flow-source-ui-facets/STORY.md)
- [2026-04-02 — typed-entity-urns (typed model URNs; WI-144 → eliminate-entity-kind)](20260402-typed-entity-urns/STORY.md)
- [2026-04-01 — metadata-and-ui-improve-and-clean (multi-origin facets, UI cleanup)](20260401-metadata-and-ui-improve-and-clean/STORY.md)
- [2026-03-30 — metadata-rework (greenfield URN)](20260330-metadata-rework/STORY.md)

---

Normative rules: [`../RULES.md`](../RULES.md) (Story closure).
