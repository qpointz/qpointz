# WI-156 — SQL Query Capability: Generate, Not Execute

Status: `planned`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

The **`sql-query`** capability today couples **validation** with **`execute_sql`** and **result
reference** artefacts, and wires **`SqlExecutionService`** into the capability dependency path. That
fits an interactive “generate and run in the same tool loop” story; **AI v3** targets **capabilities
that are agnostic to a specific UX or host**. The capability should **stop at validated, generated
SQL**; **running queries and surfacing rows** belong to **consumers** (chat service, UI, JDBC,
etc.), not to the agent’s **`sql-query`** tool surface.

**`validate_sql`** must **keep** its role: a **first-class, agent-visible** tool that checks SQL and
returns validation (including normalized SQL where applicable).

**Execution** must **not** be invoked by the model as part of this capability’s normal contract.
Instead, a **postprocessor or host** (outside `mill-ai-v3`) **intercepts** validated or generated SQL
from the run (artifacts / routed events) and passes it to an **application-side SQL execution
service**—implemented and owned **outside** this capability module.

**v1 alignment:** None. v1 NL-to-SQL is **out of scope** for this work item and **must not** constrain
naming, protocols, or behavior. **No backward compatibility** with v1 APIs or flows is required.

## Goal

Align **`sql-query`** with **generate-only** semantics for the **agent**:

- **Validate** candidate SQL via **`validate_sql`** and **emit** a canonical **generated SQL**
  artifact (e.g. protocols such as `sql-query.generated-sql` in `capabilities/sql-query.yaml`).
- **Remove** execution and result-storage from the **capability** boundary: no
  **`SqlExecutionService`** in **`SqlQueryCapabilityDependency`**; **no agent-facing `execute_sql`**
  tool (or equivalent) on this capability. YAML, handlers, **AgentEventRouter** rules, prompts, and
  tests must reflect **generated / validated SQL only** on the capability side—**consistently**.
- Document the **host contract**: which artifacts or events the postprocessor reads, and that
  **execution** is **application-side** (see design refs in story `STORY.md`).

## In Scope

1. Refactor `io.qpointz.mill.ai.capabilities.sqlquery` (handlers, capability wiring, mocks, CLI
   wiring) per the contract above.
2. Update manifests, prompts, and agent instructions (e.g. `SchemaExplorationAgent`) so the model
   targets **validate + generated SQL** only, not **execute** semantics inside the capability.
3. Tests: unit + affected integration tests; no silent removal of coverage.

## Out of Scope

- Building the **full consumer** execution pipeline (UI/query service) beyond what tests require.
- LangChain4j refactors except where required for this contract.
- v1 NL-to-SQL, Step-Back, or legacy chat processors as compatibility or parity targets.

## Acceptance Criteria

- **`validate_sql`** remains an **agent-visible** tool with a clear validation role; capability
  output includes **generated SQL** (or equivalent) as agreed in YAML/protocols.
- Capability tools and **`SqlQueryCapabilityDependency`** do **not** execute SQL or persist query
  results; **no** agent-invoked **`execute_sql`** on **`sql-query`**.
- Host/postprocessor path is **defined** at the documentation level: consumers **intercept**
  validated/generated SQL and run execution **outside** `mill-ai-v3`.
- **v3-only:** changes are justified by v3 contracts and `docs/design/agentic/`; **no** requirement to
  match or preserve v1 behavior.

## Deliverables

- This work item definition (updated).
- Code and tests on the story branch per `docs/workitems/RULES.md`.

## Reference

- Story: [`STORY.md`](STORY.md).
- **WI-159** — **`SqlValidator`** contract + autoconfigure wiring (`WI-159-ai-v3-sql-validation-service.md`).
- **WI-158** — v3 CLI playground wiring (`WI-158-mill-ai-v3-cli-generated-sql-output.md`).
- Former planning note under umbrella **WI-152** (*sql-query* § generate) — superseded by this story.
