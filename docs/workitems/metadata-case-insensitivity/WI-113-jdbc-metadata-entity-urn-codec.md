# WI-113 — JDBC binding: metadata entity URN codec + facet join

Status: `planned`  
Type: `feature` / `refactor`  
Area: `data` — `mill-data-schema-core`, `mill-data-schema-service`  
Story: [`STORY.md`](./STORY.md)

## Goal

In **`mill-data-schema-core`** (only), implement encoding from **catalog-facing shapes** (e.g. `SchemaProvider` / proto paths) to **opaque instance URNs** for metadata storage, **conforming to** the **metadata** URN pattern in [`STORY.md`](./STORY.md) (`urn:mill/<group>/<class>:<id>` — **not** a JDBC grammar; JDBC is only the **input** to the codec). Update **`SchemaFacetServiceImpl`** to match metadata entities **by URN** (not by coordinate fields). **`metadata/`** must not import JDBC tuple types for identity. REST layers use [`UrnSlug`](../../../core/mill-core/src/main/java/io/qpointz/mill/UrnSlug.java) for path segments (**WI-116**).

**Module rule:** **`data-schema-core`** may depend on **`metadata-core`**; **`metadata-core`** must **not** depend on **`data-schema-core`** ([`STORY.md`](./STORY.md) **#11**).

## In scope

- `MetadataEntityUrnCodec` (or equivalent name) and wiring in schema facet service.
- Tests proving match when URN is canonical per **WI-112**.
- Any **SchemaExplorerService** adjustments strictly needed for URN-based joins (deeper UI work in **WI-116**).

## Out of scope

- Removing coordinate columns from JPA (**WI-115**) or domain API (**WI-114**) — interface with stubs or adapters if needed across WIs.

## Code documentation (this WI)

- **Kotlin/Java** added or changed: **KDoc/JavaDoc** on every **new or updated** public type, method, constructor, and **parameter**, per [`STORY.md`](./STORY.md) **Code documentation** (tests exempt).

## Acceptance criteria

- Unit or integration tests: given a schema provider fixture, produced URNs align with metadata `entity_res` after **WI-115** (or temporary test doubles documented in this WI).
- No new `schemaName`/`tableName`/`attributeName` dependencies in `mill-metadata-core` introduced by this WI.
- **KDoc/JavaDoc** complete for all **non-test** sources touched in **`mill-data-schema-core`** and **`mill-data-schema-service`** by this WI.

## Commit

One logical commit for this WI, prefix `[feat]` or `[change]`, per `docs/workitems/RULES.md`.
