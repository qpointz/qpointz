# WI-155 — AI v3 Facet Catalog Awareness and Intent→Facet Inference

Status: `planned`  
Type: `✨ feature` / `📝 docs`  
Area: `ai`, `metadata`  
Milestone: `0.8.0`

## Problem Statement

Recent metadata work centers on **facet types** (`FacetTypeDefinition`), **facet instances**
(assignments on entities), **DEFINED** vs **OBSERVED** type sources, and layered read paths (see
`docs/design/metadata/metadata-facet-type-catalog-defined-and-observed.md`, **M-31** / **M-32**).
The **`schema`** capability today exposes **structural** exploration (`list_schemas`, `list_tables`,
`list_columns`, `list_relations`) via `SchemaFacetService` but **does not** expose the **facet
type catalog** or **descriptor** shapes the user cares about when they ask to “define a
relation”, “add descriptive text”, or “tag this column”.

Without structured access to **which facet types exist** and **what each type expects** (e.g.
`contentSchema`, target entity kinds), the LLM cannot reliably map **natural language** to
**facet type choice** and **entity-scoped assignments**. Operators also need a clear story for how
far automation can go vs mandatory human confirmation.

## Goal

Enable **ai/v3** to:

1. **Understand facet definitions** at least to the level needed for reasoning: type keys, human
   labels, applicable **target types** (table, attribute, …), and validation hints from descriptors
   (see `docs/design/metadata/facet-type-descriptor-formats.md`, **dynamic-facet-types-schema-and-validation.md**).
2. **Infer** from the user’s request **which facet type(s)** apply and **where** they should attach
   (which entity / URN / schema object), e.g. user says “define the relation between these tables”
   → **`relation`** (or equivalent) facet on the right **entity** subject to catalog rules.
3. Treat this as a **tool-backed** workflow: the runtime exposes **read-only tools** that return
   **available facet types** (and optionally **per-type detail**), and the **LLM reasons** over the
   user query + schema context + catalog to propose **facet type + target entity + payload
   skeleton**. Actual **mutation** / **capture** may continue to flow through existing
   **schema-authoring** CAPTURE tools and protocols where product policy requires confirmation.

## Feasibility Notes (planning)

- **What is realistic short-term:** listing **DEFINED** facet types (and later **OBSERVED** keys per
  **M-32**), attaching short **descriptor summaries** to prompts, and producing **structured
  proposals** (facet type id, target entity id, JSON payload outline) for the user or downstream
  capture pipeline.
- **What is harder / uncertain:** fully automatic **correct** payload generation for every facet
  without validation loops; expect **iterate-with-validator** or **human-in-the-loop** patterns,
  consistent with **`schema-authoring`** and clarification tools already in the agent profiles.
- **Dependencies:** metadata REST or `SchemaFacetService`-adjacent APIs must expose facet type
  listing and descriptors in a form agents can call (new read methods or dedicated HTTP client from
  the agent’s dependency container). Align with **`planned/metadata-value-mapping`** if facet
  conventions overlap (**WI-027** / **WI-028**).

## In Scope (when implemented)

1. Design note: **facet catalog → agent tools** mapping (which module owns the client, caching, and
   prompt injection budget).
2. One or more **tools** (new capability slice or extension of **`schema`** / **`schema-authoring`**):
   e.g. `list_facet_types`, `get_facet_type` — returning type key, display metadata, target
   constraints, and schema hints for payload shape.
3. **Prompt/system guidance** so the model ties user phrases (“relation”, “description”, “value
   map”, …) to **catalog entries** rather than inventing facet ids.
4. **Tests:** unit tests for tool handlers with fixture catalog responses; optional integration test
   if metadata service is available in test harness.
5. Explicit **non-goals** in the design note if certain inferences remain manual (e.g. only
   **suggestions**, not silent writes).

## Out of Scope

- Replacing the **metadata admin UI** for facet CRUD.
- Owning **vector / RAG** infrastructure — cross-link **WI-157** (`ai-value-mapping-capability`)
  for facet-selected RAG scope on attributes.

## Acceptance Criteria

- A **design document** under `docs/design/agentic/` (or `docs/design/metadata/`) describes the
  tool contract, inference stages, and limits of automation.
- **WI-151** parity matrix can reference this WI for **“metadata facet capture from NL”**
  versus v1 behaviour.

## Deliverables

- This work item definition.
- Implementation and tests on the story branch per `docs/workitems/RULES.md` (when the WI is
  executed).

## Reference

- `metadata/mill-metadata-core` — `FacetTypeDefinition`, `FacetType`, `FacetTypeSource`
- `docs/design/metadata/metadata-facet-type-catalog-defined-and-observed.md` — DEFINED vs OBSERVED
- `docs/design/metadata/facet-type-descriptor-formats.md`
- `ai/mill-ai-v3` — `SchemaCapability`, `SchemaAuthoringCapability` (CAPTURE / clarification flows)
- `docs/workitems/planned/metadata-value-mapping/` — **WI-027**, **WI-028** (overlap on facet payloads)
