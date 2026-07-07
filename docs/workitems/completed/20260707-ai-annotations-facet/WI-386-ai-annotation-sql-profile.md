# WI-386 — SQL profile integration and tests

| Field | Value |
|--------|--------|
| **Story** | [`ai-annotations-facet`](STORY.md) |
| **Status** | `done` |
| **Type** | `feature` / `test` |
| **Area** | `ai` |
| **Depends on** | [**WI-385**](WI-385-ai-annotation-schema-tools.md) |
| **Enables** | Story acceptance (SQL end-to-end) |

## Problem

WI-385 exposes `aiAnnotations` on schema tool results, but **`sql-query`** does not read metadata
ports directly — it discovers structure via **`schema.list_*`** then generates SQL. Without explicit
prompt guidance, agents may ignore procedural instructions on `segments` (cities join / city-name
projection).

## Goal

Wire **`data-analysis`** profile so SQL generation **honors `aiAnnotations`** returned by schema
tools in the same turn, and prove the Skymill `segments` scenario end-to-end ([`GAPS.md`](GAPS.md) GAP-1, GAP-5).

## Deliver

### 1. Prompt / profile updates

**Files (at least one; prefer both where appropriate):**

- [`capabilities/sql-query.yaml`](../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml)
- [`profiles/platform-agent-profiles.yaml`](../../../ai/mill-ai/src/main/resources/profiles/platform-agent-profiles.yaml) — `data-analysis` profile / `data-analysis.intent`

**Guidance to add (normative intent):**

- After `list_tables` / `list_columns`, inspect `aiAnnotations` on matched rows.
- For `kind: sql_generation` (or omitted/default), apply instructions when building SQL — joins,
  projections, filters — before emitting final SQL.
- Instructions supplement `descriptive` text and `list_relations` join metadata; they do not
  replace explicit relation facets.
- Do not invent annotations; only use values returned by schema tools.

### 2. Scenario / integration test

**Skymill acceptance scenario** (scripted pack or IT):

1. User asks a question that requires `skymill.segments` (or equivalent phrasing).
2. Agent calls `schema.list_tables` (and related schema tools).
3. Generated SQL joins `cities` (or `skymill.cities`) for origin/destination and projects
   city-name columns rather than raw `origin` / `destination` when annotations are present.

Location: `mill-ai-test` scenario YAML and/or `mill-ai-data` / service IT — follow existing
`data-analysis` + concept/SQL patterns.

### 3. Profile matrix tests

- [`ProfileCapabilityMatrixTest`](../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/profile/ProfileCapabilityMatrixTest.kt) — no regression on `data-analysis` capability set.
- Optional: prompt content test asserting `aiAnnotations` / sql_generation guidance present in
  composed `data-analysis` system message.

## Out of scope

- New `sql-query` tools
- Direct `MetadataReadPort` injection into SQL capability
- Live LLM-required tests if repo policy mandates scripted packs only (use deterministic pack)

## Acceptance criteria

- `data-analysis` profile prompts document `aiAnnotations` consumption path.
- Scenario test passes: segments query → SQL with city-name joins/projections.
- Prompts document GAP-5 precedence: user turn overrides annotation defaults when explicit.
- No change to capabilities list on `data-analysis` beyond prompt/profile text.
- WI-385 contract tests remain green.

## Deliverables

- This work item definition.
- Prompt/profile updates + scenario test on the story branch.
