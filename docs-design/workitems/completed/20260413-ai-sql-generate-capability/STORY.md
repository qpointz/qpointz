# AI v3 — SQL query capability: generate semantics (0.8.0)

**Status:** closed 2026-04-13 — archived under [`completed/20260413-ai-sql-generate-capability/`](.).

**Milestone:** **0.8.0**

Refactor the **`sql-query`** capability so it reflects **`sql.generate`** semantics: **`validate_sql`**
remains the agent-facing tool for checking SQL; the capability **validates and emits generated SQL
artifacts** only. **Execution and result persistence** are **not** part of the LLM tool path inside
`mill-ai-v3`. A **host postprocessor** (e.g. chat implementation) **intercepts** validated or
generated SQL and calls **application-side** SQL execution outside this module.

**Scope:** **v3 only.** v1 NL-to-SQL and Step-Back pipelines are **not** compatibility targets and may
be dropped in favor of v3. **No backward compatibility** with v1 behavior or APIs is required for
this story.

**Kotlin documentation:** All **new or materially changed** Kotlin in this story must carry **KDoc**
down to **class, function, and parameter** level (every public and internal production symbol touched
by the story). Test code is exempt unless it exposes a shared test utility.

**Language:** Prefer **Kotlin** for all new or changed implementation in this story. **Exception:**
Spring **`@ConfigurationProperties`** (or similar property-bound configuration types that rely on the
annotation processor for **`spring-configuration-metadata.json`**) should be implemented in **Java**
per project conventions; surrounding wiring may remain Kotlin.

## Work Items

- [x] WI-156 — SQL query capability: generate, not execute (`WI-156-ai-v3-sql-query-generate-semantics.md`)
- [x] WI-159 — `SqlValidator` contract + `mill-ai-v3-autoconfigure` wiring (`WI-159-ai-v3-sql-validation-service.md`)
- [x] WI-158 — mill-ai-v3-cli playground: generate-only `sql-query`, optional autowiring / real backends (`WI-158-mill-ai-v3-cli-generated-sql-output.md`)

## Related stories

- [`../../planned/ai-capability-admission/STORY.md`](../../planned/ai-capability-admission/STORY.md) — admission and tool
  observability (orthogonal; intersects if tool surfaces or invocation paths change).

## Design references (normative for v3)

- [`../../../design/agentic/README.md`](../../../design/agentic/README.md) — v3 agentic runtime and
  capability contracts.
- [`../../../design/ai/capabilities_design.md`](../../../design/ai/capabilities_design.md) — passive
  capabilities and separation of reasoning from system-owned execution (principles; v3 is the
  implementation target).

## Story closure — `docs/design/ai/`

**Done (2026-04-13):** [`../../../design/ai/capabilities_design.md`](../../../design/ai/capabilities_design.md) §15 (v3 SQL boundary); agentic developer manual and capabilities chapter updated for **`sql-query`** generate-only semantics and correct **`CapabilityProvider`** ServiceLoader path; [`../../../design/ai/README.md`](../../../design/ai/README.md) index line for `capabilities_design.md`. Normative contracts: [`../../../design/agentic/`](../../../design/agentic/).
