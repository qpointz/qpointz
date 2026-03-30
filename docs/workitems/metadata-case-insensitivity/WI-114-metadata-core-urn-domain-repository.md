# WI-114 — metadata-core: URN domain + repository without JDBC coordinates

Status: `planned`  
Type: `feature` / `refactor`  
Area: `metadata` — `mill-metadata-core`, interfaces used by `mill-metadata-persistence`; edits in **`mill-data-schema-core`** only to **host moved types** (no new **`metadata` → `data`** dependency)  
Story: [`STORY.md`](./STORY.md)

## Goal

Add **`EntityPath`** and **`TypedEntityLocator`** (names locked in **WI-112**). Remove **`schemaName` / `tableName` / `attributeName`** from **`MetadataEntity`** and any **catalog-shaped** public types in **metadata-core**. Remove **`MetadataRepository.findByLocation`** and related APIs.

**Relation / JDBC semantics:** Stored metadata treats cross-entity references as **opaque URN strings** (grammar only). **Move** [`RelationFacet`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/core/RelationFacet.kt) (and similar) to **`mill-data-schema-core`**; **`mill-metadata-core`** must **not** import those types (**module rule:** [`STORY.md`](./STORY.md) **#11**). Update **`ai/mill-ai-v3`** **`SchemaToolHandlers`** to import from **`mill-data-schema-core`**.

## In scope

- Domain model and repository contracts in **metadata-core** (depends only on **metadata** + **core**).
- **`MetadataService`** and flows that compared coordinates — switch to **instance URNs** only.
- **Clean removal** on story branch (no deprecation runway).

## Out of scope

- Flyway and JPA entity column drops (**WI-115**).
- REST controllers (**WI-116**).

## Code documentation (this WI)

- **Kotlin/Java** added or changed: **KDoc/JavaDoc** through **parameter level** on all touched **non-test** types in **`mill-metadata-core`** and **`mill-data-schema-core`** (moved types), plus **`ai/mill-ai-v3`** import fixes — per [`STORY.md`](./STORY.md).

## Acceptance criteria

- `mill-metadata-core` compiles with no catalog coordinate fields on the public entity model.
- **No Gradle dependency** from any **`metadata/*`** module to **`data/*`**, **`ai/*`**, **`apps/*`**, or **`clients/*`** introduced by this WI.
- Repository API has no `findByLocation`; callers updated with persistence (**WI-115**) on the same branch as needed.
- Relation facet typing lives in **`mill-data-schema-core`**; **metadata-core** stores opaque JSON / URN strings only.
- **KDoc/JavaDoc** complete for all **non-test** sources touched by this WI in the above modules.

## Commit

One logical commit for this WI (or one per dependent module if required), prefix `[feat]` / `[change]`, per `docs/workitems/RULES.md`.
