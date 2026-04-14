# WI-165 — Data-backed `SqlValidator` / validation path in `mill-ai-v3-data`

Status: `done`  
Type: `✨ feature` / `🔧 refactoring`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

**`SqlQueryToolHandlers.validateSql`** (in **`mill-ai-v3`**) is already **thin**: it maps **`SqlValidationService.validate`** → **`SqlValidationArtifact`**. The **contracts** **`SqlValidator`**, **`SqlValidationService`**, and **`ValidationResult`** live in **`mill-ai-v3`** (**WI-159** / generate-only `sql-query` story). There is **no** shared, Mill-aware **default** implementation that uses the **SQL engine / dialect stack** (Calcite, **`mill-sql`**, or query planner hooks) — hosts and the CLI still use **`MockSqlValidationService`** or ad hoc lambdas.

Aligning with the **schema port** work, **engine-backed validation** should live in **`mill-ai-v3-data`** so **`mill-ai-v3`** stays free of **`data/`** and heavy SQL parser dependencies, while **`mill-ai-v3-autoconfigure`** can expose **optional** beans the same way as **`MillAiV3SqlValidatorAutoConfiguration`** already bridges **`SqlValidator` → `SqlValidationService`**.

## Validation semantics (pinned)

| Layer | Semantics |
|-------|-----------|
| **Unit tests** | Use **mocks / fakes** with controlled outcomes — same style as other capability unit tests; no requirement to spin parsers or schemas. |
| **Integration tests (`testIT`)** | **Parse + schema-bound**: validation must exercise **real parsing** and **catalog/schema binding** (invalid relation or column fails), not syntax-only stubs. |

## Goal

1. Implement a **`SqlValidator`** (or a dedicated internal type adapted via existing **`asSqlValidationService()`**) in **`mill-ai-v3-data`** that validates SQL using **project-appropriate** machinery (exact engine: **design + spike** in this WI — e.g. Calcite SQL parser + configured **`SqlDialectSpec`**, or a thin wrapper around an existing Mill validation utility if one exists). The implementation must be **thread-safe** per **`SqlValidator` KDoc**.
2. **Gradle:** add only the **minimum** dependencies on **`core/mill-sql`**, **`data`**, or other modules required for that validation path — keep **`mill-ai-v3`** itself free of those deps.
3. **Autoconfigure:** extend or add configuration in **`mill-ai-v3-autoconfigure`** so a **default** **`SqlValidator`** bean is registered **when** the data-backed implementation is on the classpath **and** the application has **not** already defined **`SqlValidator`** / **`SqlValidationService`** (reuse **`@ConditionalOnMissingBean`** patterns; coordinate with existing **`MillAiV3SqlValidatorAutoConfiguration`** — merge into one logical autoconfiguration class or split by concern, but avoid duplicate beans). See **Gradle note** below.
4. **Tests — unit:** validator-focused cases using **mocks** where appropriate (valid / invalid outcomes under controlled collaborators); autoconfigure test asserting bean registration when dependencies are satisfied.
5. **Tests — integration (`testIT`):** add **Skymill + flow** integration tests using the same fixture pattern as **`FlowDescriptorMetadataSourceIT`** in **`mill-data-backends`** (`flow.facet.it.root` → repo root, **`mill.data.backend.type=flow`**, **`mill-data-backends/config/test/flow-skymill.yaml`**, metadata seeds aligned with that IT). Assertions must reflect **parse + schema-bound** behaviour (e.g. well-formed SQL against real Skymill relations **passes**; unknown objects **fail** once implemented).
6. **Scaffold / red-test policy (optional ramp):** A **scaffold** implementation may land first (always fail or no-op) with **tests that encode the target behaviour** and **fail until** the real validator is implemented — acceptable on a feature branch; **CI for `main`/`dev`** should not stay red indefinitely — either gate **`testIT`** behind completion or merge validator + green tests in one story closure batch per **`RULES.md`**.
7. **CLI / demos:** **`mill-ai-v3-cli`** is a **playground / test bench** — optional switch from **`MockSqlValidationService`** when **`WI-163`** lands; **not** a hard requirement. **Primary consumer** of production wiring is **`mill-ai-v3-service`** (and other Boot hosts using **`mill-ai-v3-autoconfigure`**).

## Gradle note — `mill-ai-v3-autoconfigure` and `mill-ai-v3-data`

- Prefer **`implementation(project(":ai:mill-ai-v3-data"))`** on **`mill-ai-v3-autoconfigure`** when configuration classes **import** data-backed types or register beans that **instantiate** them at runtime. Using only **`compileOnly`** is appropriate only if the module references **`mill-ai-v3-data`** purely reflectively **and** runtime guarantees the artifact on the classpath — default for this story: **implementation** so tests and IDE resolve types consistently.

## Acceptance Criteria

- **`./gradlew :ai:mill-ai-v3-data:test :ai:mill-ai-v3-data:testIT :ai:mill-ai-v3-autoconfigure:test`** pass at story closure (with **`testIT`** exercising Skymill+flow when this WI is complete).
- **`mill-ai-v3`** remains without **`:data:`** / engine-only deps introduced for this WI.
- **`validate_sql` tool behaviour** (manifest + structured artifact shape) unchanged except for **better** validation messages / pass-fail accuracy — no breaking protocol changes.
- KDoc on new public surfaces per project conventions.

## Out of Scope

- **Executing** SQL inside the agent — remains **host-side** per **`ai-sql-generate-capability`** closure.
- Replacing **`MockSqlValidationService`** everywhere in tests — optional; keep mocks where unit isolation is needed.

## Relation to other WIs

- Depends on **`mill-ai-v3-data`** module existing — follow story **execution order** so the module scaffold lands before this WI’s code (**WI-162** phase 1).
- **`MillAiV3SqlValidatorAutoConfiguration`** — extend rather than duplicate (**see Goal §3**).

## Deliverables

- Kotlin sources in **`mill-ai-v3-data`**, autoconfigure updates, unit + **`testIT`** (Skymill+flow), short design note in **`docs/design/agentic/`** if behaviour is non-obvious.

## Reference

- **`SqlValidator`:** `ai/mill-ai-v3/.../SqlValidator.kt`
- **`MillAiV3SqlValidatorAutoConfiguration`:** `ai/mill-ai-v3-autoconfigure/.../MillAiV3SqlValidatorAutoConfiguration.kt`
- **Skymill+flow IT pattern:** `data/mill-data-backends/.../FlowDescriptorMetadataSourceIT.kt`
- Story: [`STORY.md`](STORY.md)
