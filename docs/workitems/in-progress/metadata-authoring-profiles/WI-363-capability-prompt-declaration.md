# WI-363 — Capability prompt declaration and per-capability intent model

Status: `planned`  
Type: `✨ feature`, `📝 docs`  
Area: `ai`  
Depends on: [WI-362](WI-349-metadata-authoring-tests-docs.md) (stage 4 e2e baseline)  
**Stage:** **5** — branch `feat/meta-capability-prompts` (see [`STORY.md`](STORY.md))

## Problem Statement

Stage **3** ships **transitional** prompt wiring: `metadata-authoring.intent` classifies cross-capability
routes (`AUTHOR_FACET`, `DATA_QUERY`, `EXPLORE`, `CHAT`) so **`data-analysis`** can decompose mixed
SQL + facet turns in one place. That overlaps `sql-query` and `schema` concerns and contradicts the
target rule: **each capability declares only capability-scoped intents**; the **profile** composes a
non-overlapping union.

MR !412 review (2026-06-26) locked: **document transitional state**, **merge stages 3–4 without
contract change**, **rework in a dedicated stage** after lifecycle + e2e land.

Secondary hygiene: `AgentContext.contextType` default `"general"` collides with facet category
`general` and metadata `scope:global` vocabulary; `metadata-authoring.reasoning` enumerates tool
names that are already in the tool registry.

## Goal

Refactor capability YAML and profile prompts so intent routing is **explicit, non-overlapping, and
profile-composed** — without changing tool contracts or artefact shapes from stages 3–4.

## Normative target (locked for this WI)

| Layer | Owns |
| ----- | ---- |
| **`metadata-authoring`** | `AUTHOR_FACET`, `CHAT` (capability-local only) |
| **`sql-query`** | `DATA_QUERY` / `QUERY_DATA` intent prompt (new) |
| **`schema`** | `EXPLORE` / schema-discovery intent prompt (new or folded into `schema` system prompt) |
| **`metadata`** | No intent prompt — QUERY tools only |
| **Profile** (`data-analysis`, `metadata-authoring`, …) | Composes capability intents; resolves mixed turns |

**Mixed SQL + facets:** profile-level decomposition (e.g. `data-analysis.intent` or documented merge
order), not `metadata-authoring.intent` listing sibling-capability routes.

**Reasoning prompts:** normative **procedure** (ground → categories → type → validate → capture);
tool names optional — prefer role-based steps where reliability holds.

## In Scope

1. **Design note** — transitional intent (stages 3–4) vs target (stage 5) in
   [`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md) and
   [`GAPS.md`](GAPS.md) § intent
2. **Trim** `metadata-authoring.intent` — remove `DATA_QUERY` / `EXPLORE` sibling routes
3. **Add** `sql-query.intent` (and `schema` explore intent if not covered by existing prompts)
4. **Profile YAML** — `data-analysis` (and others as needed) declare composed intent / routing prompts
5. **`metadata-authoring.reasoning`** — align with procedure-vs-enumeration policy; document in design
6. **`AgentContext.contextType`** — JavaDoc + design glossary; optional rename plan (`chatFocusMode`)
   if low churn (or doc-only disambiguation in this WI)
7. **Scenario pack updates** — intent-routing scenarios from WI-362 assert new prompt ownership
8. **Remove** any remaining `schema-authoring` prompt references after WI-361

## Out of Scope

- Tool handler / `MetadataReadPort` contract changes (stages 3–4)
- Facet lifecycle / Accept/Reject (WI-360)
- New capabilities or MCP transport changes
- Live-LLM CI matrix
- Story archive / MILESTONE / BACKLOG (explicit user request only)

## Acceptance Criteria

- [ ] Design doc states **transitional** (stage 3–4) vs **target** (stage 5) intent ownership
- [ ] `metadata-authoring.intent` contains **no** `DATA_QUERY` or `EXPLORE` labels
- [ ] `sql-query.intent` exists and owns SQL/data-retrieval classification
- [ ] `data-analysis` profile composes non-overlapping intents across loaded capabilities
- [ ] Mixed-turn scenario pack passes with profile-level routing
- [ ] `metadata-authoring.reasoning` policy documented (procedure vs tool names)
- [ ] Glossary disambiguates `contextType`, metadata scope URN, facet category slug
- [ ] `:ai:mill-ai:test --tests "*Profile*"` and `:ai:mill-ai:test --tests "*Metadata*"` green
- [ ] `:ai:mill-ai-test:test --tests "*facet*"` intent scenarios updated and green

## Suggested commit

`[feat] WI-363: per-capability intents and profile prompt composition`
