# WI-157 — Value Mapping: Production Resolver + Facet-Driven RAG Scope

Status: `planned`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`, `metadata`  
Milestone: `0.8.0`

## Problem Statement

The **`value-mapping`** capability is partially implemented with **`MockValueMappingResolver`** in
local and CLI paths. **0.8.0** needs **real** resolution against metadata/services where available,
and a clear rule for **which attributes** require **RAG** (embedding-backed retrieval) versus simple
lookups.

## Goal

1. **Fully implement** production wiring for `get_value_mapping_attributes`, `get_value_mapping`,
   and **`ValueMappingResolver`** implementations that use platform metadata (aligned with
   **`planned/metadata-value-mapping`** where APIs land).
2. **Facet-driven RAG scope:** which columns/attributes are RAG-eligible **must** be specified via
   **metadata facets** (type keys + payload), not hard-coded in the capability. Resolver
   implementations **branch** on facet metadata (RAG path vs canonical value tables / APIs).
3. **Out of scope for the capability:** owning vector stores or embedding pipelines — pluggable
   consumers provide those services.

## In Scope

1. Replace or narrow mock usage to tests only; wire real dependencies in service/CLI paths when
   metadata is ready.
2. Document facet conventions in a design note (cross-link **WI-172** / **WI-173**).
3. Unit and integration tests for resolver branches and tool handlers.

## Out of Scope

- Implementing embedding training or corpus management.
- mill-ui changes except where required for E2E validation of the AI path.

## Acceptance Criteria

- RAG vs non-RAG behaviour is **data-driven** from facet metadata for at least one reference facet
  type (documented in the design note).
- **WI-151** parity matrix can reference this WI for **value resolution** parity vs v1.

## Deliverables

- This work item definition.
- Code and tests on the story branch per `docs/workitems/RULES.md`.

## Reference

- Former planning note lived under umbrella **WI-152** (*value-mapping*) — superseded by this story.
