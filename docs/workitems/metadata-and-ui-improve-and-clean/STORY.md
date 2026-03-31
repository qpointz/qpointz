# Story: metadata-and-ui-improve-and-clean

**Branch:** `fix/metadata-and-ui-improve-and-clean`
**Goal:** Clean up the metadata module — remove unused classes, eliminate the "blessed" concrete
facet type layer that contradicts the generic `FacetInstance` architecture, and address related
UI improvements.

## Context

The metadata module carries two conflicting facet representations: a generic schema-driven
`FacetInstance` pipeline (the current as-built architecture) and a legacy set of concrete Kotlin
facet classes (`DescriptiveFacet`, `ConceptFacet`, `ValueMappingFacet`, etc.) with their own
`merge()` / `validate()` / `setOwner()` lifecycle. The concrete classes create dual shape
definitions, dual merge semantics, and cross-module coupling that undermine the "all facet types
are equal" design.

Design doc: [`docs/design/metadata/facet-class-elimination.md`](../../design/metadata/facet-class-elimination.md)

## Work Items

- [ ] WI-130 — Remove dead code from metadata module (`WI-130-remove-dead-code.md`)
