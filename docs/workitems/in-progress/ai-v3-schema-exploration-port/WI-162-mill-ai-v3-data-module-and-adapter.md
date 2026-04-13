# WI-162 — New `mill-ai-v3-data` module: `SchemaFacetService` → port adapter

Status: `done`  
Type: `🔧 refactoring`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

After **WI-161**, **`mill-ai-v3`** exposes a **schema exploration port** with no default binding to **`SchemaFacetService`**. Production and tests still need an implementation that maps **`io.qpointz.mill.data.schema.SchemaFacetService`** queries to the port’s flat DTOs — logic that today lives in **`SchemaToolHandlers`**.

## Goal

**Execution order (unblocks downstream WIs):** Land the **Gradle module skeleton** first so **`mill-ai-v3-data` exists on the branch** before **WI-165** adds validator code, **`testIT`** wiring, or autoconfigure consumers need the artifact. Same WI, sequenced commits: **(A) module + settings + empty/minimal `build.gradle.kts`**, **(B) adapter + tests**.

1. **(A)** Add Gradle module **`ai/mill-ai-v3-data`** (Kotlin), included from root **`settings.gradle.kts`** after **`mill-ai-v3`**; register in **`ai/build.gradle.kts`** dokka aggregate if required; minimal dependencies (**`api(project(":ai:mill-ai-v3"))`**) so the module compiles and **`./gradlew :ai:mill-ai-v3-data:compileKotlin`** succeeds.
2. **Dependencies (full module):** **`implementation(project(":ai:mill-ai-v3"))`**, **`implementation(project(":data:mill-data-schema-core"))`** (and any minimal **`core` / `metadata`** pieces required by the moved code — keep the module focused on **data-backed** AI adapters).
3. **(B)** Implement **`SchemaFacetServiceExploration`** (or equivalent) implementing **`SchemaExplorationPort`**, by **moving** the former **`SchemaToolHandlers`** body from **`mill-ai-v3`** into this module (same behaviour, **`SchemaFacetService`** as collaborator).
4. Provide a small **factory** or extension such as **`SchemaFacetService.asSchemaExplorationPort()`** for **non-Spring** call sites (tests, **temporary** CLI glue — **WI-163**). **Spring Boot** hosts should prefer the bean registered in **`mill-ai-v3-autoconfigure`** (**WI-164**).
5. **Tests:** Unit tests for the adapter (mock **`SchemaFacetService`** or minimal fake) proving list schemas/tables/columns/relations match pre-refactor behaviour for representative inputs.

## Acceptance Criteria

- **`./gradlew :ai:mill-ai-v3-data:test`** passes.
- **`mill-ai-v3-data`** contains **no** Spring dependencies unless explicitly required later (default: **framework-free** adapter module).
- **No duplicate** handler logic in **`mill-ai-v3`** after WI-161–162 complete.

## Out of Scope

- **`mill-ai-v3-autoconfigure`** Spring beans for the port — **WI-164** (canonical production wiring).
- **`mill-ai-v3-cli`** long-term shape — **WI-163** (temporary **`mill-ai-v3-data`** dependency acceptable until HTTP-only / Spring-based CLI).
- **Engine-backed `SqlValidator`** implementation — **WI-165** (same module **`mill-ai-v3-data`**, separate WI for SQL validation work).

## Deliverables

- New **`build.gradle.kts`**, sources, tests; **`settings.gradle.kts`** include.

## Reference

- **`SchemaFacetService`:** `data/mill-data-schema-core/.../SchemaFacetService.kt`
- Story: [`STORY.md`](STORY.md)
