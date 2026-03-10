# WI-061 - AI v3 Schema Capability Tool Set

Status: `planned`  
Type: `feature`  
Area: `ai`, `metadata`  
Backlog refs: `TBD`

## Problem Statement

The Schema capability needs a concrete tool surface to support the first exploration workflow
and later explain-style agents.

That tool surface must not pretend that metadata is the schema source of truth. Tools must expose
physical entities guaranteed by `SchemaProvider` and then attach metadata facets where available.

## Goal

Define the initial Schema tool set for `v3`.

## In Scope

1. Define tools for listing and inspecting schema entities.
2. Define tools for relations and merged descriptions.
3. Define how tools expose missing or partial metadata explicitly.
4. Keep the tool surface low-risk and exploration-oriented.
5. Define how tools surface the provenance of fields that came from the physical model versus the
   metadata layer when that distinction matters for runtime logic.
6. Define tool outputs in terms of reusable `*WithFacets` domain objects rather than ad hoc
   AI-specific DTOs.

## Out of Scope

- SQL/data retrieval behavior.

## Acceptance Criteria

- The Schema capability has a minimal but useful exploration-oriented tool set.
- The tool set clearly identifies the subset required by the first Schema Exploration agent.
- Tool contracts make it possible to tell whether a fact is physically guaranteed, descriptively
  enriched, or absent.
- The tool set is compatible with a `SchemaFacetService` and `SchemaWithFacets` model in
  `data/mill-data-schema-core`.

## Deliverables

- This work item definition (`docs/workitems/WI-061-ai-v3-schema-tool-set.md`).
