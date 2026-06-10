# WI-188 — Data layer: facet taxonomy, binding, and serde

Status: `planned`  
Type: `✨ feature` / `🔧 change`  
Area: `data`, `metadata`  
Milestone: `0.8.0`

## Problem Statement

[`SchemaFacets.fromResolved`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacets.kt) only promotes a **subset** of platform facet types; **`relation-source` / `relation-target`** are not folded into exploration; **descriptive** serde may not match seed **`title`**; **`MetadataUrns`** may omit **`ai-column-value-mapping*`** keys. All of this blocks a **single enriched view** that AI and REST can share.

## Goal

Implement **binding and taxonomy** in **`data/mill-data-schema-core`** and **`metadata/mill-metadata-core`** per **WI-187** decisions:

- Extend or adjust **`SchemaFacets`** (and related merge/convert paths) so platform-standard facets needed for schema exploration are **materialized** consistently.
- Normalize or deserialize **relation** payloads to **`RelationFacet`** where applicable; include **relation-source** / **relation-target** in the **unified** relation view if required by WI-187.
- Align **`DescriptiveFacet`** (or import path) with **`title`/`displayName`** policy.
- Add **`MetadataUrns`** / short-key normalization for **`ai-column-value-mapping*`** if those types must participate in the schema layer (scope per WI-187).

## In Scope

- Kotlin changes in **`data/mill-data-schema-core`** and **`metadata/mill-metadata-core`**; tests in those modules.
- Optional: shared projection helpers consumed later by **`SchemaExplorerService`** (same WI or thin follow-up in **WI-189** if split).

## Out of Scope

- **`mill-ai-v3`** tool YAML and **`SchemaCatalogPort`** DTO changes — **WI-189** (may depend on this WI).

## Acceptance Criteria

- **`./gradlew :data:mill-data-schema-core:test`** (and affected metadata module tests) pass.
- New or updated tests prove **facet resolution** for the scenarios agreed in WI-187 (e.g. relation shapes, descriptive fields).
- No new **parallel** merge logic under **`ai/*`**.

## Deliverables

- Production code + tests; KDoc on new public APIs per repo conventions.

## Depends On

- **WI-187** (normative decisions).
