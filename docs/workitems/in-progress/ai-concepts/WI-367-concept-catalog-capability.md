# WI-367 — Concept catalog port and read capability

Status: `done`  
Type: `✨ feature`  
Area: `ai`  
Milestone: 0.8.0  
Depends on: [WI-366](WI-366-concept-metadata-model.md)

## Problem Statement

mill-ai exposes **schema** and generic **metadata** tools but no agent-facing **concept** abstraction.
Agents cannot list, search, or resolve business concepts that cross physical tables. Design
(`v3-foundation-decisions.md` §5.3) calls for dedicated `concept` tools separate from raw facet
APIs.

## Goal

Add a **`concept` capability** with read-only tools backed by a `ConceptCatalogPort` and
`mill-ai-data` adapter over the metadata service.

## In Scope (high level)

1. `ConceptCatalogPort` + wire types in `mill-ai`.
2. `ServiceConceptCatalogAdapter` in `mill-ai-data` over **`concept` facet assignments on
   `ModelEntityUrn.MODEL_ENTITY_ID`** in the active read scope (GAP-1 **locked**).
3. `capabilities/concept.yaml`, `ConceptCapabilityProvider`, `ServiceLoader` registration.
4. Tools (QUERY): `list_concept_tags`, `list_concepts`, `get_concept`, `search_concepts`,
   `get_model_concepts`.
5. Dependency wiring in autoconfigure + `SchemaFacingCapabilityDependencyFactory`.
6. Capability-local prompts only: `concept.intent` may classify concept lookup/explanation/search
   and concept definition/refinement signals, but must not classify SQL/data retrieval, schema-only
   exploration, or generic facet authoring globally.
7. **`concept.yaml` QUERY tools MCP-enabled by default** (GAP-8 **locked**): no `mcp.enabled:
   false`; exposed as `concept.*` when server profile includes `concept` (e.g.
   `mill.ai.mcp.profile=data-analysis`).
8. Unit tests for port/adapter and **`ConceptRefs`** (concept URN → slug → facet on model root).
9. Prompt composition tests following WI-363: concept prompts/tools do not overlap with `schema`,
   `sql-query`, `metadata`, or `metadata-authoring`.

## Out of Scope

- Prompt injection into chat runtime (WI-369)
- Authoring / capture (WI-370)
- Vector semantic search; `search_concepts` is lexical in v1
- Schema discovery tools, SQL validation/generation tools, and generic facet catalog tools

## Acceptance Criteria (draft — refine later)

- Capability discovered when profile lists `concept`.
- Tools return structured concept summaries and full detail from seeded fixtures (WI-366).
- `list_concept_tags` returns distinct tags from model-level concept facets in the active read scope.
- `list_concepts(tag=...)` filters deterministically by normalized exact tag match.
- `search_concepts(query, tag?)` performs deterministic lexical matching over name, description,
  and tags; it does not require a vector store.
- `get_model_concepts` returns model-level concept facets from the active `w` context.
- `concept.yaml` follows the WI-363 non-overlap rule: capability-local intent only, no duplicate
  schema/sql/metadata-authoring routes or tools.
- Manifest QUERY tools are MCP-enabled by default for profile-filtered exposure (GAP-8 **locked**).

## Deliverables

- This work item definition.
- Port, adapter, capability, tests on the story branch.
