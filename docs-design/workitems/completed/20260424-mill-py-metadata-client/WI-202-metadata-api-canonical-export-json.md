# WI-202 — Metadata API: full canonical export (YAML + JSON), selective facet scope, import doc accuracy

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`  
Story: [`STORY.md`](STORY.md)

## Context

Canonical metadata uses **`kind:`-discriminated multi-document** YAML ([`MetadataYamlSerializer`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt), SPEC §15.x). [`MetadataYamlSerializer.serialize`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt) already supports **scopes**, **facet type definitions**, **entities**, and **facetsByEntity** in a fixed order. Today [`DefaultMetadataImportService.export`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/service/DefaultMetadataImportService.kt) only passes **entities + facets** (single-scope facet filter), so export is **not** yet full Skymill-style parity.

[`MetadataImportExportController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataImportExportController.kt) KDoc/OpenAPI still describe import as `entities:` / `facet-types:` sections, but [`MetadataYamlSerializer.deserialize`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt) **rejects** the legacy `entities:` list envelope — docs must match the runtime.

**Ordering:** Multi-seed import remains **client-side** concat + one `POST /import`.

## Goal

### 1. Full canonical export (YAML + JSON)

- **Populate** [`MetadataImportService.export`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/service/MetadataImportService.kt) (and implementation) so output uses **`MetadataYamlSerializer.serialize` / `serializeJson`** with:
  1. **All** persisted **`MetadataScope`** rows → `kind: MetadataScope` documents.
  2. **`kind: FacetTypeDefinition` documents — locked policy:** emit **only** facet types that are **defined in the catalog with a known schema** (registered / manifest-backed types where re-export carries a stable type definition). **Do not** emit `FacetTypeDefinition` for **undefined** or **flexible-open** facet kinds where there is no catalog definition to serialize.
  3. **All** **`MetadataEntity`** rows → `kind: MetadataEntity` documents with **`facets`** lists filtered per **§2** below.

**Important:** §1.2 limits **type-definition documents** only. It does **not** limit **facet instance rows** under §1.3. Each entity’s **`facets`** list must include **every** facet assignment that matches **§2** (scope filter), **including** assignments whose type has **no** `FacetTypeDefinition` in the export — same observability as “export all facets on entities”; the only narrowing is **`scope`**, not “catalog-defined type only.”
- **JSON:** same ordered **list of document maps** as YAML multi-doc, encoded as a **JSON array** (one object per document).
- **`GET /api/v1/metadata/export`:** `format` query `yaml` (default) | `json`. **`format` wins** over `Accept` when both are present; if only `Accept` is set, optional negotiation may follow in implementation (document result).

### 2. Export scope selection — facet rows only (`scope` query)

| Input | Behaviour |
|--------|-----------|
| **Omitted** / empty | **Global** facet filter only: include facet assignments whose **`scopeUrn`** is **global** (same default as today’s single-scope export). |
| **Comma-separated** | Normalise each token; **union:** include facet row if **`scopeUrn`** matches **any** listed scope. |
| **`all`** / **`*`** | No facet scope filter (all facet rows for every entity). |

**Scope / definition documents (§1.1–1.2)** are **not** filtered by this parameter — only **embedded facet rows** under entities are.

**Errors:** malformed / unknown scope tokens → **400** (existing metadata error pattern) unless product chooses otherwise.

### 3. Import — documentation only (same endpoint)

- **No** multi-file API. Update **`POST /import`** KDoc, `@Operation`, and OpenAPI text: **only** `kind:`-discriminated **multi-document** YAML (optional `---`); **no** `entities:` list envelope; align wording with [`MetadataYamlSerializer.deserialize`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt) error message intent.

## Acceptance

- Service + controller tests: export contains **MetadataScope** and **FacetTypeDefinition** documents **only for catalog-defined types (§1.2)** **and** entity docs; facet filtering matches §2; YAML and JSON **same document count and order**. Where the test fixture mixes defined and undefined/flexible facet types, assert definition docs appear **only** for the defined type(s), and assert **entity `facets`** still lists **all** instance rows matching §2 for **both** kinds (no “defined-type-only” facet row filter).
- **Import** OpenAPI/controller descriptions no longer claim unsupported `entities:` / `facet-types:` list shapes.
- Swagger annotations cover `format`, `scope`, and response content types.

## Dependencies

- **WI-203** / **WI-194** depend on this WI for canonical export/import semantics.

## Out of scope

- Changing Skymill **authoring** files.  
- Server-side **bundle** import.  
- New import wire format.
