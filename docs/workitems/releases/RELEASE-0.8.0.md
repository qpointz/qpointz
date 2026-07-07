# Release 0.8.0

Release date: **2026-07-07**  
Compare: [v0.7.0...v0.8.0](https://gitlab.qpointz.io/qpointz/qpointz/compare/v0.7.0...v0.8.0)

**Shipped record** — promoted from [`../MILESTONE.md`](../MILESTONE.md) § **0.8.0** draft during release housekeeping on the **`reease/0-8-0-preparation`** branch (before MR to `dev`). Next milestone draft: [`../MILESTONE.md`](../MILESTONE.md) § **0.9.0**.  
Backlog tracker: [`../BACKLOG.md`](../BACKLOG.md) — **`done`** rows pruned at this release per [`../RULES.md`](../RULES.md) § **Release (version) process**.

---

## Highlights

- **AI v3 runtime (`mill-ai*`)** — Promoted `mill-ai*` modules; layered **`mill.ai.*`** configuration (providers, models, `data.embedding`, `vector-stores`); durable **`ai_chat`** persistence with ownership and CASCADE delete; YAML scenario harness + opt-in scenario capture/export; artefact emit contract; MCP HTTP POC; value-mapping stack with pgvector.
- **mill-ui agentic chat** — Unified **`/api/v1/ai/chats`** with SSE **`item.*`** protocol; SQL/data artefact cards, Run all, mid-chat profile switch; facet condensed preview for **`facet-proposal`**; Mantine 9 migration.
- **SQL + chart visualizations** — `sql-query` **`describe_sql`** / **`execute_sql`**; enriched **`sql.generated`** payload with **`visualizations[]`**; **`chart-mapping`** capability; mill-ui Chart / Data / SQL tabs with local ECharts compiler.
- **Metadata authoring** — Catalog-generic **`metadata-authoring`** capability; YAML agent profiles; **`MetadataContent`** seeds; **`MetadataReadPort`**; multi-artifact batch + facet lifecycle (Accept/Reject via **`mill-events`**); per-capability intent prompts.
- **Metadata model** — **`concept`** and **`ai-annotation`** facet types; facet JSON Schema REST + admin view; DQ L1/L2 facet seeds; multi-scope model explorer (`?scope=` / tag filters).
- **Data plane** — Query result sessions (**`/api/v1/query/**`**); streaming HTTP export (**`/services/export`**); Flow **`TranslatableTable`** scan, table statistics, enumerable hash-join policy.
- **OData v4** — Per-schema **`.svc`** endpoints; RelNode compose + Rel→Substrait adapter; facet-driven **`$expand`** and EDM annotations; platform toolchain **Java 25**.
- **Platform** — Spring Boot **4** / Jackson **3** / Spring Security **7**; application event bus (**`mill-events`**); cloud blob sources + Spring resource loading for **`s3://`** / **`gs://`** / **`azure-blob://`**; GCP deployment wiring for OData and MCP.
- **Clients** — RFC 9457 Problem Details on data HTTP; mill-py **`mill.metadata`** / **`mill.schema_explorer`** HTTP clients.
- **mill-ui Analysis** — Saved-query catalog, **`/api/v1/analysis/**`**, CodeMirror SQL editor, HTTP **`queryService`**.

---

## Story closures (since v0.7.0)

Newest first — full archive index: [`../completed/README.md`](../completed/README.md).

### AI chart visualizations (`ai-chart-mapping`, 2026-07-07)

**WI-338**–**WI-341**, **WI-366**–**WI-370** (chart story) — `sql-query` execution/schema tools, SQL artifact completion coordinator, **`chart-mapping`** capability, wire/replay/scenarios, mill-ui chart rendering. Archive: [`completed/20260707-ai-chart-mapping/STORY.md`](../completed/20260707-ai-chart-mapping/STORY.md).

### AI annotations (`ai-annotations-facet`, 2026-07-07)

**WI-383**–**WI-388** — **`ai-annotation`** facet type, schema **`aiAnnotations[]`**, metadata authoring capture, **`sql-query`** / **`data-analysis`** prompt honor. Archive: [`completed/20260707-ai-annotations-facet/STORY.md`](../completed/20260707-ai-annotations-facet/STORY.md).

### Metadata facet JSON Schema (`metadata-facet-json-schema`, 2026-07-03)

**WI-379**–**WI-382** — Generated JSON Schema from **`FacetPayloadSchema`**, **`GET /api/v1/metadata/facets/{typeKey}/schema`**, admin read-only schema view. Archive: [`completed/20260703-metadata-facet-json-schema/STORY.md`](../completed/20260703-metadata-facet-json-schema/STORY.md).

### AI concepts (`ai-concepts`, 2026-07-01)

**WI-366**–**WI-370** (concept story), **WI-372** — **`concept`** facet, **`ConceptCatalogPort`**, **`data-analysis`** injection, authoring capture, **`mill.ai.chat.max-iterations`**. Archive: [`completed/20260701-ai-concepts/STORY.md`](../completed/20260701-ai-concepts/STORY.md).

### Model view multi-scope (`model-view-multi-scope`, 2026-07-01)

**WI-378** — URL-driven scope/readScope, tag filter pickers, chat deep-links. Archive: [`completed/20260701-model-view-multi-scope/STORY.md`](../completed/20260701-model-view-multi-scope/STORY.md).

### Metadata authoring profiles (`metadata-authoring-profiles`, 2026-06-29)

**WI-354**–**WI-364** — Catalog-generic facet authoring, YAML profiles, lifecycle events, Mantine 9. Archive: [`completed/20260629-metadata-authoring-profiles/STORY.md`](../completed/20260629-metadata-authoring-profiles/STORY.md).

### Scenario capture export (`scenario-capture-export`, 2026-06-29)

**WI-365** — **`mill.ai.chat.scenario-capture.enabled`**, **`GET …/scenario-export`**. Archive: [`completed/20260629-scenario-capture-export/STORY.md`](../completed/20260629-scenario-capture-export/STORY.md).

### Data Quality metadata facets (`dqm-metadata-facets`, 2026-06-24)

**WI-342**–**WI-344** — 15 L1/L2 DQ facet type seeds and design contract. Archive: [`completed/20260624-dqm-metadata-facets/STORY.md`](../completed/20260624-dqm-metadata-facets/STORY.md).

### OData v4 service (`odata-service`, 2026-06-23)

**WI-324**–**WI-329** — **`mill-data-odata`**, **`mill-data-odata-service`**, Java 25. Archive: [`completed/20260623-odata-service/STORY.md`](../completed/20260623-odata-service/STORY.md).

### AI v3 MCP server POC (`ai-v3-mcp-server-poc`, 2026-06-22)

**WI-325**–**WI-327**, **WI-329**, **WI-330** — MCP over Streamable HTTP; stdio descoped (**A-96**). Archive: [`completed/20260622-ai-v3-mcp-server-poc/STORY.md`](../completed/20260622-ai-v3-mcp-server-poc/STORY.md).

### General chat facet display (`ai-chat-facet-display`, 2026-06-19)

**WI-335**–**WI-337** — **`FacetCondensedPreview`**, shared read-only facet renderer. Archive: [`completed/20260619-ai-chat-facet-display/STORY.md`](../completed/20260619-ai-chat-facet-display/STORY.md).

### Event bus foundation (`general-event-bus`, 2026-06-19)

**WI-311**–**WI-314** — **`mill-events`** contracts + autoconfigure. Archive: [`completed/20260619-general-event-bus/STORY.md`](../completed/20260619-general-event-bus/STORY.md).

### AI chat persistence (`ai-chat-persistence`, `ai-chat-table-naming`, 2026-06-19)

**WI-317**–**WI-321**, **WI-323**–**WI-324** — Unified chat schema, ownership, CASCADE delete, naming convention. Archives: [`completed/20260619-ai-chat-persistence/`](../completed/20260619-ai-chat-persistence/STORY.md), [`completed/20260619-ai-chat-table-naming/`](../completed/20260619-ai-chat-table-naming/STORY.md).

### Flow TranslatableTable scan (`flow-translatable-table-scan`, 2026-06-18)

**WI-311**, **WI-314**–**WI-316** — **`FlowTableScan`**, statistics, hash-join policy. Pushdown follow-on: [`planned/flow-scan-pushdown/STORY.md`](../planned/flow-scan-pushdown/STORY.md). Archive: [`completed/20260618-flow-translatable-table-scan/STORY.md`](../completed/20260618-flow-translatable-table-scan/STORY.md).

### Artefact emit contract (`ai-artifact-emit-contract`, 2026-06-16)

**WI-300**–**WI-308**, **WI-310** — Scenario harness, emit coordinator, **`data-analysis`** profile. Archive: [`completed/20260616-ai-artifact-emit-contract/STORY.md`](../completed/20260616-ai-artifact-emit-contract/STORY.md).

### Chat artefact presentation (`ai-sql-view-restart`, 2026-06-12)

**WI-289**–**WI-298** — **`QueryDataView`**, expand pane, Run all toolbar. Archive: [`completed/20260612-ai-sql-view-restart/STORY.md`](../completed/20260612-ai-sql-view-restart/STORY.md).

### AI configuration restructure (`ai-configuration-restructure`, 2026-06-10)

**WI-284**–**WI-288** — Layered **`mill.ai.*`**. Archive: [`completed/20260610-ai-configuration-restructure/STORY.md`](../completed/20260610-ai-configuration-restructure/STORY.md).

### mill-ui Analysis (`mill-ui-analysis-full-stack`, 2026-06-09)

**WI-256**–**WI-260** — Saved queries, analysis REST, SQL editor. Archive: [`completed/20260609-mill-ui-analysis-full-stack/STORY.md`](../completed/20260609-mill-ui-analysis-full-stack/STORY.md).

### Cloud blob sources + resource loading (`cloud-blob-source`, `cloud-resource-loading`, 2026-05-14)

**WI-262**–**WI-265**, **WI-271**, **WI-274**–**WI-279** — **`BlobSource`** adapters, Spring protocol resolvers. Archives under [`completed/20260514-*`](../completed/README.md).

### Query result execution (`query-result-execution-service`, 2026-05-11)

**WI-262**–**WI-265** — **`mill-data-query`**, **`/api/v1/query/**`**. Archive: [`completed/20260511-query-result-execution-service/STORY.md`](../completed/20260511-query-result-execution-service/STORY.md).

### Streaming export (`streaming-export-service`, 2026-05-07)

**WI-250**–**WI-261** — **`mill-export-service`**, format SPI. Archive: [`completed/20260507-streaming-export-service/STORY.md`](../completed/20260507-streaming-export-service/STORY.md).

### mill-ui general chat (`ai-v3-mill-ui-general-chat`, 2026-05-06)

**WI-229**–**WI-233** — Unified AI chat HTTP + SSE in mill-ui. Archive: [`completed/20260506-ai-v3-mill-ui-general-chat/STORY.md`](../completed/20260506-ai-v3-mill-ui-general-chat/STORY.md).

### Spring Boot 4 migration (`spring4-migration-day-2`, `spring4-pre-migration-cleanup`, 2026-04-30)

**WI-097**–**WI-104**, **WI-201**–**WI-209** — Boot 4, Jackson 3, Spring AI 2.0.0-M5, Security 7. Archives: [`completed/20260430-spring4-*`](../completed/README.md).

### Client error transparency (`client-error-transparency`, 2026-04-29)

**WI-013** — Problem Details HTTP. Archive: [`completed/20260429-client-error-transparency/STORY.md`](../completed/20260429-client-error-transparency/STORY.md).

### AI facet catalog inference (`ai-facet-catalog-inference`, 2026-04-28)

**WI-204**–**WI-206** — **`metadata`** / **`metadata-authoring`** capabilities. Archive: [`completed/20260428-ai-facet-catalog-inference/STORY.md`](../completed/20260428-ai-facet-catalog-inference/STORY.md).

### mill-py metadata client (`mill-py-metadata-client`, 2026-04-24)

**WI-192**–**WI-203**. Archive: [`completed/20260424-mill-py-metadata-client/STORY.md`](../completed/20260424-mill-py-metadata-client/STORY.md).

### Schema capability metadata (`schema-capability-metadata`, 2026-04-17)

**WI-187**–**WI-191**. Archive: [`completed/20260417-schema-capability-metadata/STORY.md`](../completed/20260417-schema-capability-metadata/STORY.md).

### Value mapping facets (`value-mapping-facets-vector-lifecycle`, `implement-value-mappings`, 2026-04-16–17)

**WI-174**–**WI-186**. Archives: [`completed/20260416-implement-value-mappings/`](../completed/20260416-implement-value-mappings/STORY.md), [`completed/20260417-value-mapping-facets-vector-lifecycle/`](../completed/20260417-value-mapping-facets-vector-lifecycle/STORY.md).

### AI v3 chat dependencies + schema port (`ai-v3-chat-capability-dependencies`, `ai-v3-schema-exploration-port`, 2026-04-14)

**WI-160**, **WI-161**–**WI-169**. Archives under [`completed/20260414-*`](../completed/README.md).

### SQL generate capability (`ai-sql-generate-capability`, 2026-04-13)

**WI-156**, **WI-158**, **WI-159** — Generate-only **`sql-query`**. Archive: [`completed/20260413-ai-sql-generate-capability/STORY.md`](../completed/20260413-ai-sql-generate-capability/STORY.md).

---

## Backlog ID crosswalk (0.8.0 scope)

Rows below were **`done`** in [`BACKLOG.md`](../BACKLOG.md) at release and **pruned** during housekeeping.

| Area | IDs | Notes |
|------|-----|--------|
| **data** | D-7, D-8, D-9 | Export SPI, query sessions, Flow scan |
| **ai** | A-31, A-56, A-75–A-77, A-83–A-92, A-97–A-101 | v3 runtime through chart + ai-annotation |
| **client** | C-23 | mill-py metadata/schema_explorer |
| **metadata** | M-33, M-35, M-36 | DQ facets, JSON Schema, ai-annotation seed |
| **platform** | P-5, P-7–P-9, P-31, P-36, P-38, P-41 | Spring 4, errors, export, event bus, OData |
| **source** | S-9 | Cloud blob sources |
| **ui** | U-11, U-13, U-15–U-17, U-19 | Chat, analysis, artefacts, facets, scope, charts |

---

## Deferred / follow-ons

Still **planned** or **backlog** — not in **0.8.0**:

- **Flow scan pushdown** — [`planned/flow-scan-pushdown/`](../planned/flow-scan-pushdown/STORY.md) (**D-10**)
- **Metadata value mapping bridge** — [`planned/metadata-value-mapping/`](../planned/metadata-value-mapping/STORY.md) (**M-1**–**M-9**)
- **Concept object relations** — [`planned/concept-object-relations/`](../planned/concept-object-relations/STORY.md)
- **WebFlux migration + method security** — [`planned/webflux-migration-and-method-security/`](../planned/webflux-migration-and-method-security/STORY.md) (**P-34**)
- **Visual analysis modes** — [`planned/mill-ui-visual-analysis-modes/`](../planned/mill-ui-visual-analysis-modes/STORY.md) (**U-14**)
- **stdio MCP bridge** — **A-96** (descoped from MCP POC)
- **DQ rule execution engine** — **M-16**

---

## Documentation / process

- **Design:** [`docs/design/agentic/`](../../design/agentic/README.md), [`docs/design/metadata/`](../../design/metadata/README.md), [`docs/design/platform/`](../../design/platform/README.md)
- **Public:** [`docs/public/src/mill-ui.md`](../../public/src/mill-ui.md), [`docs/public/src/metadata/`](../../public/src/metadata/), [`docs/public/src/data-access/odata.md`](../../public/src/data-access/odata.md)
- **Workitems:** [`../RULES.md`](../RULES.md); next milestone draft **0.9.0** in [`../MILESTONE.md`](../MILESTONE.md) after tag
