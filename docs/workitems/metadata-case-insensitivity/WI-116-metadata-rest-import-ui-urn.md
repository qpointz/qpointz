# WI-116 — Metadata REST, import/YAML, schema explorer, mill-ui: URN-only entity ids

Status: `planned`  
Type: `feature`  
Area: `metadata` service, `mill-data-schema-service`, `mill-data-schema-core` (DTO/consumer alignment), `ui/mill-ui`, optional **`ai/mill-ai-v3`** schema tools if facet/URN contracts shift  
Story: [`STORY.md`](./STORY.md)

## Goal

**Metadata REST** path `{id}` and payloads use **URN only**; remove **`?schema=&table=`** and catalog-shaped filters from metadata APIs (move list/discover to binding-owned explorer APIs if needed). **Import/YAML** accepts **URN entity ids only**; old dot-id files are handled by **offline** conversion (script/docs), not by the live import API unless an explicit dev-only shim is agreed. **SchemaExplorer** DTOs and **mill-ui** pass **`metadataEntityId`** as URN end-to-end.

**No backward compatibility for this story:** breaking API/import/UI changes in one delivery; **no** OpenAPI `deprecated`, **no** legacy id handling in import (fail fast on non-URN), **no** runtime shims — remove obsolete operations/paths from the spec entirely (e.g. `/facet-instances`).

## In scope

- `MetadataEntityController`, `DefaultMetadataImportService`, schema explorer DTOs/services.
- `ui/mill-ui` data-model and any callers of metadata APIs.
- **REST review (plan Phase 4b):** URN path encoding for `/{id}`; remove list `?schema`/`?table`; align `GET .../facets/{typeKey}` with JPA instance rows if still inconsistent; optional `uid` on facet PUT responses; **remove** `DELETE .../facet-instances/{facetUid}` — facet **occurrence** = same resource as `/facets/{typeKey}` with **`uid`** query param when MULTIPLE; update any clients/tests; add **`instanceUid`** on schema-explorer `FacetEnvelopeDto` when needed for deletes without a second metadata GET.
- **OpenAPI / SpringDoc (plan Phase 4c):** full annotation pass on metadata + schema-explorer controllers — `@Operation`, `@Parameter`, `@Schema` examples for URNs; spec reflects **final** surface only (**no** `deprecated` operations); DTO docs distinguishing **entity URN** vs **facet occurrence UUID**; update any checked-in or CI OpenAPI artefacts.
- **Cross-module checklist (plan Phase 4d):** verify every listed path — `SchemaExplorerService`/DTOs; **`schemaService.ts`** (metadata calls must use **`metadataEntityId`**, not tree dot-`id`); `DataModelLayout` / `EntityDetails`; import/export + `MetadataEntityDto`; **`SchemaToolHandlers`** / agent `contextId` vs metadata URN (coordinate with **WI-114** / **WI-112**).
- **Documentation:** complete every item in [`STORY.md`](./STORY.md) **Design & inventory doc checklist** (tick boxes in the PR description or update the file with `[x]` when done). **WI-118** verifies as-built consistency.

## Out of scope

- Core domain (**WI-114**) and DB (**WI-115**) except integration fixes.

## Code documentation (this WI)

- **Kotlin/Java** (`mill-metadata-service`, `mill-data-schema-service`, controllers, DTOs, services): **KDoc/JavaDoc** through **parameter level** on all **new or updated** non-test code.
- **`ui/mill-ui`:** **JSDoc/TSDoc** at **function / exported API** level for touched **`.ts` / `.tsx`** (services, hooks, significant handlers); document non-obvious component behaviour — **not** parameter-level on every React prop (per [`STORY.md`](./STORY.md)).

## Acceptance criteria

- API contract documented or OpenAPI updated if applicable.
- Manual or automated check: create/read entity via UI using **URN** for metadata reads/writes (not dot-path `entity.id` unless identical by design).
- `ui/mill-ui` tests and `schemaService` tests cover **encoded URN** path segments and facet delete with `uid` where applicable.
- Phase **4d** table in the Cursor plan has no remaining “must fix” rows for this story branch.
- **STORY.md** design/inventory checklist **fully checked** (or equivalent PR checklist).
- **KDoc/JavaDoc** (backend) and **function-level** TS docs (UI) as above for all touched sources.

## Commit

One logical commit for this WI, prefix `[feat]`, per `docs/workitems/RULES.md`.
