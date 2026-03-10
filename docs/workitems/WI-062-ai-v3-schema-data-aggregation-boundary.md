# WI-062 - AI v3 Schema Data Aggregation Boundary

Status: `planned`  
Type: `refactoring`  
Area: `ai`, `metadata`  
Backlog refs: `TBD`

## Problem Statement

Schema tools need a unified view over physical schema and schema-bound metadata, but the
aggregation boundary is not yet defined.

The aggregation boundary is especially important because the two inputs play different roles:

- `SchemaProvider` guarantees the physical model and existence of schemas, tables, and attributes
- `metadata` provides detached descriptive facets that may or may not exist for a given physical
  entity

The merged boundary must not blur those guarantees away.

## Goal

Define the adapter/service boundary that aggregates physical schema and schema-bound metadata for
the Schema capability.

The implementation target for this boundary is:

- `data/mill-data-schema-core` as a pure Kotlin module with no Spring dependencies
- `data/mill-data-autoconfigure` for bean wiring

## In Scope

1. Combine physical structure, descriptions, relations, and related schema-bound metadata.
2. Keep internal subsystem boundaries hidden from the LLM-facing tool layer.
3. Ensure aggregation outputs can represent both strong evidence and missing metadata explicitly.
4. Treat `SchemaProvider` as the authoritative physical source and metadata as enrichment layered
   onto matched physical entities.
5. Define how detached metadata is matched to physical schema coordinates such as schema, table,
   and attribute.
6. Define behavior for metadata that references missing or stale physical entities.
7. Define the core reusable service API, named `SchemaFacetService`.
8. Define the core reusable domain model using `*WithFacets` naming, including at least:
   - `SchemaWithFacets`
   - `SchemaTableWithFacets`
   - `SchemaAttributeWithFacets`
9. Implement the boundary in Kotlin.
10. Provide high-comprehensiveness unit tests for merge and matching behavior.
11. Provide integration tests using the existing skymill test model and dataset assets.
12. Treat preservation of the full physical schema as a non-negotiable contract of the boundary.
13. Ensure `*WithFacets` models retain all relevant physical schema properties and enough
    metadata linkage to trace back to physical schema origins and metadata entities/facets.

## Out of Scope

- Non-schema concept aggregation.

## Acceptance Criteria

- A coherent aggregation boundary exists for schema-oriented capability outputs.
- The aggregation boundary supports the first Schema Exploration workflow without forcing the
  agent to query multiple internal subsystems directly.
- The aggregation model preserves the difference between:
  - physically guaranteed entities
  - matched descriptive enrichment
  - missing metadata
  - stale or unbound metadata
- The boundary is specified as reusable data-domain infrastructure, not as an AI-only helper.
- The boundary naming is consistent with `SchemaFacetService` and `*WithFacets` models.
- All physical schema elements exposed by `SchemaProvider` remain available after merge.
- `*WithFacets` models preserve physical schema properties and traceability back to both:
  - physical schema origins
  - metadata entities and attached facets
- Unit test coverage is intentionally high for:
  - physical-to-metadata matching
  - missing metadata handling
  - stale or unbound metadata handling
  - relation attachment
  - provenance and coverage flags
- Integration tests validate the boundary against:
  - `test/skymill.yaml`
  - `test/datasets/skymill/`
  - `test/datasets/skymill/skymill-meta-repository.yaml`

## Testing Notes

Implementation should include:

- unit tests as the primary regression layer for merge logic
- integration tests for real end-to-end assembly with physical schema plus metadata

Unit tests should be comprehensive enough to cover:

- schema-level matching
- table-level matching
- attribute-level matching
- preservation of all physical schemas, tables, and attributes after merge
- facet merging by location
- missing descriptive facets
- relation facet attachment
- metadata rows that do not match any physical entity
- physically present entities with no metadata
- traceability from `*WithFacets` objects back to physical schema coordinates
- traceability from `*WithFacets` objects back to metadata entities and facet payloads

Integration coverage should use the existing skymill fixtures already present in the repository,
preferably reusing the same model and dataset family used by other `testIT` suites:

- `test/skymill.yaml`
- `test/datasets/skymill/` dataset assets
- `test/datasets/skymill/skymill-meta-repository.yaml`

Integration assertions must explicitly prove that all physical schema elements remain available
through the merged boundary even when metadata is missing.

At minimum, integration tests should assert:

- representative schemas, tables, and attributes from the physical skymill model are available
  through `SchemaFacetService`
- descriptive and relation facets from `skymill-meta-repository.yaml` are attached where expected
- missing metadata is represented as missing coverage, not as missing physical structure

## Domain Model Requirements

The `*WithFacets` domain model is not a thin metadata wrapper. Each object should carry enough
information to serve both UI and AI consumers without forcing them to re-open the original
sources just to understand what the object represents.

At minimum:

- `SchemaWithFacets` should preserve relevant physical schema properties from the source
  `Schema` object
- `SchemaTableWithFacets` should preserve relevant physical table properties such as names, type,
  and other physical characteristics exposed by the underlying physical schema model
- `SchemaAttributeWithFacets` should preserve relevant physical attribute properties such as
  names, physical type, nullability, and related physical characteristics where available

Each `*WithFacets` object should also contain enough information to trace back to:

- physical schema origin
  - schema name
  - table name
  - attribute name where applicable
  - physical identifiers or source-derived keys where applicable
- metadata origin
  - matched metadata entity id where applicable
  - available facet types
  - attached facet payloads or stable references to them

The design should assume that later consumers may need both:

- user-facing merged information
- precise origin tracking for debugging, UI drill-down, and LLM context construction

## Deliverables

- This work item definition (`docs/workitems/WI-062-ai-v3-schema-data-aggregation-boundary.md`).
