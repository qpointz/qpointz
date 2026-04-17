# WI-172 — Metadata Value Mapping Bridge and Parity

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`, `ai`  
Backlog refs: `M-1`, `M-2`, `M-3`, `M-4`, `M-5`, `M-8`, `M-9`

## Problem Statement

Metadata value mappings are split across the legacy metadata/value-mapper path and the newer
facet-based metadata model. Until Mill has a single feature-flagged bridge with parity tests,
NL2SQL behavior remains harder to evolve and validate.

## Goal

Introduce a migration-safe bridge from legacy value mapping to facet-based metadata so AI
components can consume a stable abstraction while the underlying provider is switched from
legacy to faceted metadata.

## In Scope

1. Create or finalize the typed `ValueMappingFacet` and register it in the metadata/AI runtime.
2. Introduce a value-resolution abstraction that supports `legacy`, `faceted`, and optional
   `hybrid` provider modes.
3. Implement an adapter from the new metadata service/facet model back to current
   `MetadataProvider` consumers where needed.
4. Migrate current value mapping data into facet-compatible repository content.
5. Add parity tests proving legacy and facet-backed resolution produce equivalent results for
   representative mappings and sources.

## Out of Scope

- Metadata UI editing workflows.
- Production JPA/composite persistence.
- Advanced facets beyond value mapping.

## Dependencies

- Existing metadata service and file-backed repository must remain available as the read path.
- AI callers may continue using legacy interfaces during the transition, but this WI defines the
  bridge that makes later migration possible.

## Coordination (refresh lifecycle)

[**WI-182**](../completed/20260417-value-mapping-facets-vector-lifecycle/WI-182-value-mapping-vector-refresh-lifecycle.md) § *Production metadata retrieval* specifies **indexing / refresh-time** loading of value-mapping facet rows from **`mill-data-metadata`** (greenfield joins on **`type_res`**) and assembly of **`CompositeValueSource`**. This WI focuses on **query-time** legacy ↔ faceted **resolution** and parity; do not treat the bridge as a substitute for WI-182’s refresh read path.

## Implementation Plan

1. **Facet contract**
   - Lock the `ValueMappingFacet` shape and registration path.
2. **Resolver abstraction**
   - Introduce `ValueResolver`-style abstraction and provider selection flag.
3. **Adapter path**
   - Implement metadata adapter/service wiring for current AI callers.
4. **Data migration**
   - Convert existing mapping YAML into facet-based repository content.
5. **Parity verification**
   - Add legacy-vs-faceted tests and representative NL2SQL integration checks.

## Acceptance Criteria

- Value mapping can be resolved through a single abstraction independent of legacy storage.
- Facet-backed resolution supports the current static mapping and source-backed use cases needed
  by NL2SQL.
- Configuration can select `legacy`, `faceted`, or `hybrid` resolution mode.
- Parity tests cover representative legacy mappings and fail on behavioral drift.
- Current AI flows continue to work when routed through the bridge.

## Test Plan (during implementation)

### Unit

- `ValueMappingFacet` serialization/deserialization tests.
- Resolver mode selection tests.
- Adapter extraction tests for mappings and sources.

### Integration

- Legacy vs faceted parity runs over representative metadata fixtures.
- NL2SQL integration checks with legacy mode and faceted mode enabled.

## Risks and Mitigations

- **Risk:** faceted mappings diverge subtly from legacy semantics.  
  **Mitigation:** lock fixture-based parity tests before flipping defaults.

- **Risk:** migration couples AI too tightly to metadata internals.  
  **Mitigation:** keep a narrow resolver interface and use adapter boundaries.

- **Risk:** mixed-mode rollout creates ambiguous debugging paths.  
  **Mitigation:** expose active provider mode clearly in configuration and logs.

## Deliverables

- This work item definition (`docs/workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md`).
- Feature-flagged bridge from legacy to facet-backed value resolution.
- Migrated value mapping fixtures/content and parity test coverage.
