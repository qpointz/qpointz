# WI-158 — mill-ai-v3-cli: Generate-Only `sql-query`, Playground + Shared Autowiring

Status: `planned`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`  
Milestone: `0.8.0`

## Character (non-requirements)

**`mill-ai-v3-cli`** is an **interactive playground** to get a **feel for AI v3** (capabilities,
tools, events). It is **not** a product surface: there are **no** specific **usability**,
**performance**, **SLA**, or **polish** requirements. Prefer **simple** wiring and output over
production-grade UX or benchmarking.

## Problem Statement

The **`mill-ai-v3-cli`** schema path **manually** constructs collaborators in **`CliApp.kt`**
(**`MockSqlValidationService`**, **`MockSqlExecutionService`**, demo schema services, etc.) and prints
the full **agent event stream**. That is fine for a zero-Spring smoke test, but it **diverges** from
how **`mill-ai-v3-autoconfigure`** and downstream **`data/`**, **`ai/`**, and **`metadata/`** modules
assemble **real** schema, SQL dialect, and (after **WI-159**) **`SqlValidator`** beans.

After **WI-156**, **`sql-query`** is **generate-only**; the CLI must match that contract **and** the
team wants the CLI to **reuse the same autowiring** as services so manual runs can hit **real
backends** (metadata, Calcite/data sources, production-style validators) instead of ad-hoc mocks.

## Goal

1. **Align `sql-query` wiring** with **WI-156** (no executor on the capability dependency; no
   **`execute_sql`** as the primary CLI outcome; output emphasizes **generated SQL**).
2. **Reuse Spring Boot / autoconfigure composition** used elsewhere: evolve **`ai/mill-ai-v3-cli/`**
   so it boots a **minimal application context** (or documented equivalent) that **imports** the same
   **`@AutoConfiguration`** / starter stack as the v3 chat path **where practical** — pulling beans from
   **`ai/mill-ai-v3-autoconfigure`**, and any **`data/`** / **`metadata/`** auto-configuration needed for
   **`SchemaFacetService`**, dialect, **`SqlValidator`**, and related dependencies **if not already
   composed for CLI** (this WI closes that gap).
3. **CLI output** for the schema agent should still **highlight generated SQL** (filter, summary, or
   protocol-focused printing) so operators are not drowned in unrelated events.

**Intent (playground):** where practical, one wiring path for “v3 + data + metadata” so
**`./gradlew :ai:mill-ai-v3-cli:run`** can optionally hit **real** infrastructure via config — enough
to **try** the stack, not to satisfy non-functional requirements.

## Dependencies

- **WI-156** — stable **`SqlQueryCapabilityDependency`** and generate-only protocols.
- **WI-159** — **`SqlValidator`** contract and **`mill-ai-v3-autoconfigure`** registration; the
  playground **may** reuse that stack when convenient; mocks remain acceptable for a quick **taste**
  of v3.

## In Scope

1. **Gradle / module dependencies** on **`mill-ai-v3-autoconfigure`** and whatever **`data/`** /
   **`metadata/`** / **`ai/`** modules are needed to **taste** real backends — keep the graph **no
   larger than necessary** for that playground goal (no obligation to mirror the full service).
2. **Bootstrap change:** replace or wrap the current **`main`** entry so the CLI runs inside a
   **Spring context** (or a documented subset) that **reuses** auto-configuration beans; keep a
   **fast** path or profile for **mock-only** local dev if needed.
3. **Wire schema exploration agent** dependencies from the context (**`SchemaExplorationAgent`**
   factory or equivalent) instead of only manual **`new`** in **`CliApp.kt`**, where that aligns with
   existing service code.
4. **Output** behaviour per **Goal §3** (generated SQL first).
5. **Tests** or **docs**: how to run against real backends vs mocks; optional smoke test if the module
   gains a test source set.

## Out of Scope

- Implementing **WI-159**’s concrete **`SqlValidator`** (product owner / other module).
- Full **SQL execution** inside the CLI (postprocessor / host responsibility).
- Parity with **v1** NL-to-SQL.
- **Usability reviews**, **performance tuning**, load testing, or CLI-as-a-supported product.

## Acceptance Criteria

- Playground remains **usable enough** for a developer to **try** v3; **no** bar for UX quality or
  latency.
- **`mill-ai-v3-cli`** uses **shared autowiring** for v3 + data/metadata **where this WI introduces
  it** and **where it stays simple** — avoid duplicating beans unnecessarily; sketchy manual glue is
  acceptable if documented in KDoc or a short module README.
- Schema agent runs with **generate-only** **`sql-query`** (**WI-156**); no **`MockSqlExecutionService`**
  on the **`sql-query`** path.
- **Optional:** with config, a developer can aim the CLI at **real** backends **without** editing code
  for every experiment — **not** a hard requirement if autowiring is awkward for a thin playground.
- Output **roughly** highlights **generated SQL** vs noise; **no** requirement for polished
  formatting.
- **`./gradlew :ai:mill-ai-v3-cli:run`** (or Boot run task if main class changes) noted in commit or a
  one-line note under **`ai/mill-ai-v3-cli/`**.

## Deliverables

- This work item definition.
- Code, Gradle, and any small README on the story branch per `docs/workitems/RULES.md`.

## Reference

- **WI-156** — `sql-query` generate semantics (`WI-156-ai-v3-sql-query-generate-semantics.md`).
- **WI-159** — **`SqlValidator`** + autoconfigure (`WI-159-ai-v3-sql-validation-service.md`).
- Modules: `ai/mill-ai-v3-cli/`, `ai/mill-ai-v3-autoconfigure/`, relevant `data/`, `metadata/`.
- Story: [`STORY.md`](STORY.md).
