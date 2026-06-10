# WI-161 — `SchemaCatalogPort` contract in `mill-ai-v3` (drop direct `mill-data-schema-core`)

Status: `done`  
Type: `🔧 refactoring`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

**`mill-ai-v3`** declares **`implementation(project(":data:mill-data-schema-core"))`** and embeds **`SchemaFacetService`**, **`WithFacets`**, and **`RelationFacet`** in **`SchemaCapabilityDependency`**, **`SchemaToolHandlers`**, and **`SchemaExplorationAgent`**. That ties the core v3 module to the data merge boundary and makes **`mill-ai-v3`** unsuitable as a pure capability/runtime library independent of Mill Data.

## Goal

1. Introduce a **`SchemaCatalogPort`** (name finalised in implementation) in **`mill-ai-v3`** under the schema capability package, exposing the **four tool operations** with **JSON-stable result types** aligned to **`capabilities/schema.yaml`** (reuse or relocate the existing **`ListSchemasItem`**, **`ListTablesItem`**, **`ListColumnsItem`**, **`ListRelationsItem`**, **`RelationDirection`** shapes).
2. Replace **`SchemaCapabilityDependency(schemaFacetService: SchemaFacetService)`** with a dependency on that **port**.
3. Update **`SchemaCapability`** tool bindings to call the port (remove **`SchemaToolHandlers`** object from **`mill-ai-v3`** once WI-162 supplies the implementation package).
4. Update **`SchemaExplorationAgent`** constructor and **`fromEnv` / `fromConfig`** factories to accept the **port** instead of **`SchemaFacetService`**.
5. Remove **`implementation(project(":data:mill-data-schema-core"))`** from **`ai/mill-ai-v3/build.gradle.kts`**. Ensure **`mill-ai-v3`** sources contain **no** `io.qpointz.mill.data.*` imports after this WI + WI-162 land (this WI may leave temporary stubs only if the branch compiles only after WI-162 — prefer **single mergeable sequence**: WI-161 + WI-162 on the same branch in close commits, or one commit spanning both if trivial).

## Acceptance Criteria

- **`SchemaCapabilityDependency`** references only **`mill-ai-v3`** types (the port), not **`SchemaFacetService`**.
- **`SchemaExplorationAgent`** API uses the port, not **`SchemaFacetService`**.
- **`mill-ai-v3`** Gradle file has **no** `:data:` project dependency.
- **Unit tests** in **`mill-ai-v3`** can use a **fake port** (in-memory lists) for **`SchemaCapability`** or agent tests if any are added; existing profile/registry tests remain green.

## Out of Scope

- Implementing the port with **`SchemaFacetService`** — **WI-162**.
- CLI and **`DemoSchemaFacetService`** — **WI-163**.

## Deliverables

- Kotlin sources and Gradle change as above; KDoc on the port and dependency type per project conventions.

## Reference

- Current handlers: `ai/mill-ai-v3/.../SchemaToolHandlers.kt`
- Story: [`STORY.md`](STORY.md)
