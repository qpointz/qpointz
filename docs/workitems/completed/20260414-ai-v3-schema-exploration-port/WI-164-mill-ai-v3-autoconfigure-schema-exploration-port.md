# WI-164 — `mill-ai-v3-autoconfigure`: register `SchemaCatalogPort` (canonical wiring)

Status: `done`  
Type: `🔧 refactoring`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

After **WI-161–162**, the **`SchemaCatalogPort`** implementation lives in **`mill-ai-v3-data`**, but **Spring Boot hosts** need a **single canonical place** that exposes a **`SchemaCatalogPort`** bean (or equivalent factory) built from the application’s **`SchemaFacetService`**, so **`AgentContext`** / **`SchemaCapabilityDependency`** wiring does not duplicate adapter construction in every entry point. **Primary product consumer:** **`mill-ai-v3-service`**. **`mill-ai-v3-cli`** remains a playground / test bench (**WI-163**); optional dependency on **`mill-ai-v3-data`** for local REPL only.

## Goal

1. Extend **`ai/mill-ai-v3-autoconfigure`** to register a **`SchemaCatalogPort`** bean when **`SchemaFacetService`** and the **`mill-ai-v3-data`** adapter are on the classpath — pattern consistent with existing **`MillAiV3SqlValidatorAutoConfiguration`** (`@ConditionalOnClass`, `@ConditionalOnBean`, `@ConditionalOnMissingBean`).
2. **Dependencies:** add **`implementation(project(":ai:mill-ai-v3-data"))`** for schema-port (and later **WI-165** SQL validator) auto-configuration that **imports** data-backed types and registers concrete beans. Reserve **`compileOnly(project(":ai:mill-ai-v3-data"))`** only if a configuration class references **`mill-ai-v3-data`** purely via reflection **and** runtime classpath is guaranteed — **default: `implementation`** so compilation, tests, and IDE resolution stay consistent (same rule as **WI-165** Gradle note).
3. **Ordering / conflict:** if **`SchemaCatalogPort`** is already provided by the host, **do not** override (`@ConditionalOnMissingBean(SchemaCatalogPort::class)`).
4. **KDoc** on the new configuration class; **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** entry if using Spring Boot 3 style imports.
5. **Tests:** slice or context test proving the bean is created when **`SchemaFacetService`** is present (mock or test double acceptable).

## Acceptance Criteria

- **`./gradlew :ai:mill-ai-v3-autoconfigure:test`** (and **`testIT`** if applicable) pass.
- **`mill-ai-v3-autoconfigure`** is the **documented** place for **production** wiring of **`SchemaFacetService` → `SchemaCatalogPort`**.
- **`mill-ai-v3-cli`** may still depend on **`mill-ai-v3-data`** **temporarily** (**WI-163**) for standalone REPL; **long term**, CLI should prefer **HTTP/SSE against a running service** or a **minimal Spring bootstrap** that reuses this autoconfiguration — note in **WI-163** / README, not necessarily implemented in this WI.

## Out of Scope

- **`WI-160`** full **`AgentContext`** dependency injection — may consume this bean once both land; coordinate in **`WI-160`** commit if needed.

## Deliverables

- Autoconfigure Kotlin + imports metadata + tests.

## Reference

- Story: [`STORY.md`](STORY.md)
- **`MillAiV3SqlValidatorAutoConfiguration`:** `ai/mill-ai-v3-autoconfigure/.../MillAiV3SqlValidatorAutoConfiguration.kt`
