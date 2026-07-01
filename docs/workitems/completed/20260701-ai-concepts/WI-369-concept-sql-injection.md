# WI-369 — Concept injection in data-analysis profile

Status: `done`  
Type: `✨ feature`  
Area: `ai`  
Milestone: 0.8.0  
Depends on: [WI-367](WI-367-concept-catalog-capability.md)

## Problem Statement

SQL / data-analysis agents need **domain semantics** (business definitions, SQL snippets, target
columns) when users ask questions using business language. v1 step-back extracted concept names as
strings but never loaded stored definitions. v3 should inject or discover model-level concepts from
the active `w` context so SQL generation can use persisted business definitions and indicative SQL
hints.

## Goal

Extend **`data-analysis`** so relevant **concepts** ground SQL and natural-language answers in
**general chat** (`/chat`, no `contextType=knowledge` binding).

## In Scope (high level)

1. Add **`concept`** to **`data-analysis`** profile capabilities (GAP-8 **locked**).
2. Inject or retrieve model-level concepts from the active `w` context for SQL reasoning.
3. Extend `data-analysis.intent` at the profile level to compose `concept.intent` with
   `sql-query.intent`, `schema.intent`, and `metadata-authoring.intent` without overlap (GAP-8
   **locked**): concept tools as semantic hints before SQL; `sql-query.intent` still owns
   DATA_QUERY.
4. `concept.system` prompt guidance: when to call concept tools as semantic hints before schema/SQL
   work, without owning SQL generation or validation.
5. **MCP:** with `mill.ai.mcp.profile=data-analysis`, `concept.*` QUERY tools appear in
   [`CapabilityMcpCatalog`](../../../../ai/mill-ai-mcp-core/src/main/kotlin/io/qpointz/mill/ai/mcp/CapabilityMcpCatalog.kt)
   alongside other profile tools — no new MCP wiring; `concept.yaml` tools MCP-enabled by default
   (WI-367).
6. Scenario tests: model-context turn receives concept summary before SQL generation.
7. `ProfileIntentPromptTest` and optional `CapabilityMcpCatalogTest` for `data-analysis` profile.

## Out of Scope

- Knowledge / contextual inline chat (`contextType=knowledge` — deferred WI-368)
- New concept authoring (WI-370)
- Full v1 step-back reasoner port

## Acceptance Criteria (draft — refine later)

- `data-analysis` profile lists **`concept`** in `platform-agent-profiles.yaml` and capability matrix
  tests.
- `data-analysis.intent` includes `concept.intent` as a semantic-hint route; `concept.intent` does
  not classify DATA_QUERY (`ProfileIntentPromptTest`).
- Model-context or data-analysis chat includes model-level concept summary in system prompt or uses
  concept tools before SQL.
- With `mill.ai.mcp.profile=data-analysis`, MCP catalog includes `concept.list_concept_tags`,
  `concept.list_concepts`, `concept.get_concept`, `concept.search_concepts`, and
  `concept.get_model_concepts` (QUERY tools only; no CAPTURE tools on `concept` capability).

## Deliverables

- This work item definition.
- Profile + injection + scenario tests on the story branch.
