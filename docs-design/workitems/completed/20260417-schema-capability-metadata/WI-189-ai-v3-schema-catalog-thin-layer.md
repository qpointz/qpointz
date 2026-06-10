# WI-189 — AI v3: schema catalog thin layer (`SchemaCatalogPort`, adapter, YAML)

Status: `planned`  
Type: `✨ feature` / `🔧 change`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

[`SchemaFacetCatalogAdapter`](../../../../ai/mill-ai-v3-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt) and [`SchemaCatalogPort`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaCatalogPort.kt) must **only** translate **`SchemaFacetService`** output into tool-facing DTOs — but today they **omit** fields and **duplicate** mapping concerns that should follow **WI-188**’s unified model.

## Goal

1. Update **`List*Item`** types and **[`capabilities/schema.yaml`](../../../../ai/mill-ai-v3/src/main/resources/capabilities/schema.yaml)** so tool **I/O** matches the **contract** from **WI-187** and the **data** from **WI-188** (e.g. `joinSql`, display name/tags where in scope, relation coverage).
2. Keep **`mill-ai-v3`** free of **`io.qpointz.mill.data.schema.*`** implementation types beyond the adapter boundary; **`mill-ai-v3-data`** implements **thin mapping only**.
3. If **WI-188** introduces shared DTO mappers used by **`SchemaExplorerService`**, **delegate** to them from the adapter where possible.

## In Scope

- `ai/mill-ai-v3` — `SchemaCatalogPort.kt`, manifests.
- `ai/mill-ai-v3-data` — `SchemaFacetCatalogAdapter.kt`, extension `asSchemaCatalogPort()`; autoconfigure if wiring changes.

## Out of Scope

- New metadata **merge** semantics (belongs in **WI-188**).
- **WI-191** cleanup of dead code (may trim after this WI lands).

## Acceptance Criteria

- **`./gradlew :ai:mill-ai-v3:test :ai:mill-ai-v3-data:test`** pass (and autoconfigure module if touched).
- Prompt/tool descriptions in **`schema.yaml`** stay accurate vs actual JSON returned.

## Deliverables

- Production code + tests (unit tests for adapter mapping).

## Depends On

- **WI-188** (stable `SchemaFacetService` / `SchemaFacets` behavior).
