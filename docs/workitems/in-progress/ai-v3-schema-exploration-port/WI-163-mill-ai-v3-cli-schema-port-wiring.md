# WI-163 — `mill-ai-v3-cli`: depend on `mill-ai-v3-data`, wire port, relocate demo helpers

Status: `done`  
Type: `🔧 refactoring` / `📝 docs`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

**`mill-ai-v3-cli`** currently depends on **`mill-data-schema-core`** and contains **`DemoSchemaFacetService`**, **`SchemaFacetServiceFactory`**, and **`QSynthYamlSchemaProvider`**, which pull **`io.qpointz.mill.data.*`** into the CLI sources. After **WI-161–162**, the CLI should obtain a **`SchemaExplorationPort`** via **`mill-ai-v3-data`** (demo + adapter) so the CLI does not need a **direct** `:data:` dependency for the default path.

**Positioning:** **`mill-ai-v3-service`** is the **primary product consumer** of **`mill-ai-v3-autoconfigure`**; **`mill-ai-v3-cli`** is a **playground / test bench**, not the canonical integration surface. A Gradle dependency from **`mill-ai-v3-cli` → `mill-ai-v3-data`** is **allowed as a temporary** measure for the **standalone REPL** (no Spring). **Canonical** wiring for applications remains **`mill-ai-v3-autoconfigure`** (**WI-164**): hosts obtain **`SchemaExplorationPort`** from Spring. Long term, the CLI may become **HTTP/SSE-only** or a **thin launcher** around a minimal Boot context so it does not duplicate wiring.

## Goal

1. Replace **`implementation(project(":data:mill-data-schema-core"))`** with **`implementation(project(":ai:mill-ai-v3-data"))`** (and trim any now-redundant deps). Treat this dependency as **temporary**; document that **production** wiring lives under **`mill-ai-v3-autoconfigure`**.
2. **`CliApp` / `SchemaExplorationAgent.fromEnv`:** pass a **`SchemaExplorationPort`** built from **`SchemaFacetService`** using the WI-162 factory (e.g. **`demoSchemaFacetService().asSchemaExplorationPort()`**) — **manual** glue acceptable here only because the REPL is not a Spring app.
3. **Relocate** **`DemoSchemaFacetService`** (and, if still needed, **`QSynthYamlSchemaProvider`**) into **`mill-ai-v3-data`** or keep minimal CLI-only glue — prefer **`mill-ai-v3-data`** for anything that implements **`SchemaFacetService`** or heavy data fixtures so **CLI stays thin**.
4. Update **`ai/mill-ai-v3-cli/README.md`** (and any **`CLAUDE.md` / developer one-liners** if they mention **`SchemaFacetService`** in CLI) to describe the module split and point to **`WI-164`** for Spring wiring.

## Acceptance Criteria

- **`./gradlew :ai:mill-ai-v3-cli:compileKotlin`** (and existing CLI checks) succeed **without** **`mill-ai-v3-cli`** declaring **`project(":data:...")`** unless a follow-up explicitly needs it (default: **none**).
- **`schema` agent** path still runs with **`SCHEMA_SOURCE=demo`** as today.
- Documentation states **`mill-ai-v3-cli` → `mill-ai-v3-data`** is **temporary** for non-Spring REPL; **`mill-ai-v3-autoconfigure`** (**WI-164**) is the **intended** wiring surface for services.

## Out of Scope

- **Skymill / live HTTP** `SchemaFacetService` wiring — may remain commented placeholders; document extension point.

## Deliverables

- CLI Kotlin moves, **`build.gradle.kts`**, README; story **`STORY.md`** tracking updated on completion per **`RULES.md`**.

## Reference

- Story: [`STORY.md`](STORY.md)
- **`CliApp.kt`:** `ai/mill-ai-v3-cli/.../CliApp.kt`
