# WI-060 - AI v3 Schema Capability

Status: `planned`  
Type: `feature`  
Area: `ai`, `metadata`  
Backlog refs: `TBD`

## Problem Statement

The first outward domain capability should expose schema-oriented understanding without leaking
internal subsystem structure to the LLM.

That capability must unify two different source types:

- physical schema from
  `data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/SchemaProvider.java`
- detached descriptive metadata from the `metadata/` modules

The physical source is authoritative for which schemas, tables, and attributes exist. Metadata
is authoritative for descriptive facets such as descriptions, relations, rules, and similar
annotations that are attached to physical entities but are not derivable from the physical model
alone.

## Goal

Define the `v3` Schema capability.

## In Scope

1. Define the schema capability boundary.
2. Treat physical schema and schema-bound metadata as a unified capability surface.
3. Keep the capability aligned with the first Schema Exploration agent while remaining reusable
   for later focused model-context agents.
4. Clarify which parts of the capability are required for the first POC versus later expansion.
5. Preserve source-of-truth semantics so the runtime can distinguish guaranteed physical facts
   from optional descriptive metadata.
6. Align the capability with a reusable `data/mill-data-schema-core` boundary rather than
   embedding merge logic directly in `ai/v3`.

## Out of Scope

- Non-physical concept capabilities.

## Acceptance Criteria

- A coherent Schema capability boundary is defined for the runtime and LLM-facing tool surface.
- The boundary is concrete enough to support the first Schema Exploration agent without
  overcommitting to later non-POC needs.
- The capability boundary explicitly defines `SchemaProvider` as the physical source of truth.
- The capability boundary explicitly defines `metadata` as detached descriptive enrichment bound
  onto physical entities.
- The capability can be backed by a reusable `SchemaFacetService` from
  `data/mill-data-schema-core`.

## Deliverables

- This work item definition (`docs/workitems/WI-060-ai-v3-schema-capability.md`).
