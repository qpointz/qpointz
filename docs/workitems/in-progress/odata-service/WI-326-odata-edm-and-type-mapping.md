# WI-326 — `mill-data-odata` EDM and type mapping (RWS)

**Story:** [`odata-service`](STORY.md) · **Backlog:** P-41  
**Status:** done  
**Depends on:** WI-324

## Goal

Spring-free `mill-data-odata`: build RWS **`EntityDataModel`** from `SchemaFacetService` + type mapping.

**Before coding:** read [`COLDSTART.md`](COLDSTART.md) § WI-326 module skeleton and [`STORY.md`](STORY.md) § Architecture decisions.

## Scope

- `MillEntityDataModelFactory` (or `EntityDataModelFactory`) from physical schemas + facets
- `MillTypeToEdmMapper` (`DatabaseType` / proto fields → RWS Edm primitives)
- Navigation properties from `RelationFacet`
- Descriptive facet → OData annotations (`@Core.Description`)
- Unit tests (fixture + skymill relation fragment)
- `libs.versions.toml`: `com.sdl` **2.16.1** (`odata_api`, `odata_edm`); **no Olingo**

## Module

`data/mill-data-odata/` — depend on `mill-data-schema-core`, `mill-data-backend-core`, RWS `odata_api` + `odata_edm`.

## Out of scope

- Query execution (WI-327)
- HTTP (WI-328)
- Olingo / Apache Attic libraries

## Completion (normative)

After verify passes: update **tracker** (`STORY.md` `[x]`, this file status) → **commit** (include tracker in commit) → **push** → **CI green**. See [`STORY.md`](STORY.md) § Implementation delivery workflow.
