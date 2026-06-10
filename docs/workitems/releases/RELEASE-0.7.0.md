# Release 0.7.0

Release date: **2026-04-10**  
Compare: [v0.6.0...v0.7.0](https://gitlab.qpointz.io/qpointz/qpointz/compare/v0.6.0...v0.7.0)

Canonical milestone tracker (summary): [`../MILESTONE.md`](../MILESTONE.md) § **0.7.0**  
Backlog tracker: [`../BACKLOG.md`](../BACKLOG.md) — between releases, **`done`** rows may remain until
**release housekeeping**; see [`../RULES.md`](../RULES.md) § **Release (version) process**.

---

## Highlights

- **SQL dialect platform:** Typed YAML dialect model in `core/mill-sql`, gRPC/HTTP `GetDialect`, server SDK, AI consumer migration, and design documentation (**WI-015–WI-022**).
- **Clients:** Python remote dialect over gRPC/HTTP (**WI-021**), SQLAlchemy MillDialect + compiler (**WI-024**), ibis BaseBackend slice (**WI-025**), JDBC `DatabaseMetaData` surface (**WI-026**).
- **`ai/v3`:** Multi-mode protocol execution (`TEXT`, `STRUCTURED_FINAL`, `STRUCTURED_STREAM`), capability YAML manifests, LangChain4j executor and tests (**WI-067**).
- **Metadata & Model view:** Service split/autoconfigure, REST redesign (`/api/v1/metadata/**`), JPA persistence and scopes, physical schema explorer (`/api/v1/schema/**`), `mill-ui` Data Model wiring (**WI-085–WI-087**, **WI-089**, **WI-092**, **WI-093a/b**).
- **Greenfield metadata & UI:** URN entity identity, Flyway squashes, seeds, REST/YAML/`mill-ui` alignment (**WI-119–WI-128**); facet registry, editing, and related UI work through March–April 2026 (story narratives below).
- **gRPC:** Raw grpc-java server as primary transport, shared Skymill SQL parity suites for server and JDBC driver `testIT` (tracked in milestone as distinct delivery from REST WI-085 — see completed list in § [Work items](#work-items-completed-in-070)).
- **Operations:** Mill service logging refresh (**P-36** — see **§ Backlog ID crosswalk**; would be **`done`** then **pruned** at release per [`RULES.md`](../RULES.md)).

---

## Work items (completed in 0.7.0)

Detailed prose for each item lived in per-WI markdown files removed after delivery; this list is the retained record.

- **WI-015** — Core `mill-sql` bootstrap + feature-complete YAML schema  
- **WI-016** — Migrate `POSTGRES` / `H2` / `CALCITE` / `MYSQL` to new schema  
- **WI-017** — Kotlin typed dialect model + YAML loader (`core/mill-sql` only)  
- **WI-018** — `GetDialect` contracts for gRPC/HTTP + handshake support flag  
- **WI-019** — Server `GetDialect` implementation backed by migrated dialects  
- **WI-020** — Migrate AI dialect consumer to new typed runtime model  
- **WI-022** — Fully document SQL dialect schema in design docs  
- **WI-026** — JDBC full metadata implementation (`DatabaseMetaData` surface)  
- **WI-021** — Python remote dialect consumption over gRPC/HTTP  
- **WI-024** — Python SQLAlchemy implementation (MillDialect + compiler + entry points)  
- **WI-025** — Python ibis initial implementation (BaseBackend + SQL compilation, slice 1)  
- **WI-067** — `ai/v3` multi-mode protocol execution (`ProtocolDefinition` with `TEXT`, `STRUCTURED_FINAL`, `STRUCTURED_STREAM`; `ProtocolExecutor`; `LangChain4jProtocolExecutor`; `PlannerDecision.protocolId`; `AgentEvent` protocol variants; manifests; `LangChain4jAgent`; tests with fake `StreamingChatModel`)  
- **WI-085** — Metadata service API cleanup: `mill-metadata-service` / `mill-metadata-autoconfigure`, observer chain, no-op fallbacks, import ordering, `runtimeOnly` persistence on `mill-service`  
- **WI-086** — Metadata REST controller redesign: four controllers under `/api/v1/metadata/**`, `MetadataUrns`, exception handler, startup seed keys **`mill.metadata.seed.resources`**, OpenAPI/KDoc  
- **WI-087** — Metadata relational JPA persistence (`mill-metadata-persistence`, Flyway, adapters). *Later greenfield DDL evolved per SPEC (e.g. `metadata_audit`, `metadata_seed`); see* [`docs/design/metadata/mill-metadata-domain-model.md`](../../design/metadata/mill-metadata-domain-model.md)  
- **WI-089** — Metadata scopes: domain, repository, service, `MetadataContext`, REST  
- **WI-092** — `mill-ui` model view: live `MetadataApi` / `SchemaExplorerApi`, timeouts, URN paths  
- **WI-093a** — `mill-data-schema-service` `/api/v1/schema/**`  
- **WI-093b** — Metadata autoconfigure split; lazy columns; `context` query param  
- **gRPC server (Skymill parity)** — Raw grpc-java server as the active transport (`services/mill-data-grpc-service/`); shared Skymill SQL query-case set; server `testIT` + JDBC driver `testIT` parity; legacy net.devh server under `misc/` for reference *(milestone text erroneously reused the WI-085 label for this track — treat as distinct delivery).*

---

## Story closures / deliveries (March–April 2026)

### Metadata — Schema Explorer (`metadata-edit-and-explorer`)

Physical schema explorer REST (`mill-data-schema-service`, `/api/v1/schema/**`), autoconfigure split, `mill-ui` Data Model wiring (lazy columns, `context`, facet resolution). **Deferred:** interactive scope picker beyond `global`, strict write authz, schema list/tree performance (**`BACKLOG.md`**).

### Metadata — Edit, facet registry, UI service (`metadata-edit-and-promotion-follow-up`)

**WI-094–WI-099**, **WI-090** / **WI-098**: facet manifests/registry, admin UI, JPA row-per-facet storage (**`metadata_facet`**, Flyway V8+), surrogate keys (V9), stable **`facet_uid`** (V10), MULTIPLE delete semantics, `mill-ui` facet editor, **`mill-ui-service`**, `mill.ui.*`. **WI-091** (promotion workflow) **deferred** — **`BACKLOG.md`**.

### Mill UI (`mill-ui-fixes`)

**WI-105** ESLint 9 flat config; **WI-106** Vitest setup; **WI-108** design pack pointer; **WI-107** explorer toolbar; **WI-109** MULTIPLE facet cards; **WI-110** `facetPayloadUtils.ts`.

### Metadata — Greenfield rework (`metadata-rework`)

**WI-119–WI-128**: URN entity identity, `FacetInstance` / `merge_action`, core merge (`MetadataReader` / `MetadataView`), squashed **`V4__metadata_greenfield.sql`**, `metadata_audit`, `metadata_seed`, `mill.metadata.seed.*`, `mill.metadata.repository.*`, REST/OpenAPI, YAML import/export, `mill-ui` URN paths, **WI-127** design sync, **WI-128** public docs. Archives: [`completed/20260330-metadata-rework/STORY.md`](../completed/20260330-metadata-rework/STORY.md), [`SPEC.md`](../completed/20260330-metadata-rework/SPEC.md).

### Mill UI — facet types & Model view (post–metadata-rework)

MULTIPLE payload coercion, stereotypes docs ([`mill-ui-facet-stereotypes.md`](../../design/metadata/mill-ui-facet-stereotypes.md), public [`facet-stereotypes.md`](../../public/src/metadata/facet-stereotypes.md)); Facet types admin UX; JSON/YAML expert editor, hyperlink layout. **M-30** delivered (see **§ Backlog ID crosswalk**).

### Mill service logging (`apps/mill-service`, 2026-04-10)

`logback-spring.xml` with Boot **`defaults.xml`** / **`file-appender.xml`**, **`springProperty`** for patterns and log file, **`%wEx`**, Logback/Jansi colors + ASCII `|` (avoids Spring **`%clr`** issues on Gradle/Windows), column alignment, `application.yml` rolling policy role unchanged, repo **`.gitignore`** **`**/logs/`**. **P-36**.

---

## Backlog ID crosswalk (0.7.0 scope)

This table lists **product backlog IDs** that **belonged to the 0.7.0 delivery wave**. A **one-off**
prune cleared **`done`** rows from [`BACKLOG.md`](../BACKLOG.md) when that file was tightened; **going
forward**, rows stay **`done`** until **release housekeeping** ([`RULES.md`](../RULES.md) § **Release (version) process**).
Use this section for grep / old notes; canonical delivery detail is **`MILESTONE.md`** / this file.

| Area | IDs (0.7.0 wave / since pruned from `BACKLOG`) | Notes |
|------|------------------------------|--------|
| Client | C-1, C-7, C-10–C-14, C-23 | Dialect + Python surfaces + JDBC metadata |
| Metadata | M-11, M-12, M-19–M-22, M-28–M-31, M-33 | Includes flow facets (**M-33**); **M-34** remains **planned** in live [`BACKLOG.md`](../BACKLOG.md) |
| Platform | P-6, P-34, P-36 | gRPC server move, REST error pattern doc, mill-service logging |
| AI | (many **A-** rows through A-73) | v3 foundation through chat service — see **`MILESTONE.md`** § 0.8.0 Completed |
| Persistence | PS-1–PS-7 | Lanes delivered through metadata JPA |
| Security | SEC-1, SEC-1a, SEC-3a–SEC-3e | Identity + auth REST + `mill-ui` wiring |
| Data | D-8 | GetDialect / handshake |

---

## Documentation / process

- **Workitems layout:** active unchecked stories under [`../planned/`](../planned/README.md); normative rules in [`../RULES.md`](../RULES.md).
- **Next milestone:** **WI-023** (ibis dialect validation) and other planned WIs — [`../MILESTONE.md`](../MILESTONE.md) § **0.8.0**.
