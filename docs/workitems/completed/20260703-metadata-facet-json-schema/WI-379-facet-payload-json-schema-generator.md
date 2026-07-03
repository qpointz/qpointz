# WI-379 — Facet Payload JSON Schema Generator

Status: `planned`  
Type: `feature`  
Area: `metadata`

## Problem Statement

Facet type manifests already declare payload shape through `FacetPayloadSchema`, but external
consumers generally expect JSON Schema. Today they must either understand Mill's recursive schema
tree or skip validation. This blocks lightweight client validation and makes it harder to pass
facet payload contracts to tools and models as standard media.

## Goal

Add a metadata-core converter that generates a draft-07-compatible JSON Schema projection from
`FacetPayloadSchema` and `FacetTypeManifest`.

## Scope

1. Add a converter under `metadata/mill-metadata-core`.
2. Support `OBJECT`, `ARRAY`, `STRING`, `NUMBER`, `BOOLEAN`, and `ENUM`.
3. Preserve `title`, `description`, `format`, and `default`.
4. Generate JSON Schema `required` from field-level `FacetPayloadField.required`.
5. Carry Mill annotations using `x-mill-*` fields.
6. Normalize object-level `required` during manifest normalization so returned manifests and
   generated schema agree.

## Acceptance Criteria

- Nested object, array, enum, scalar, format, default, and stereotype cases generate predictable
  JSON Schema maps.
- A field with `required = false` does not appear in the JSON Schema `required` list.
- A field with omitted/true `required` appears in the generated `required` list.
- Existing platform facet definitions still pass strict manifest normalization.

## Test Plan

- Unit tests for `FacetPayloadJsonSchema`.
- Unit tests for required-list normalization in `FacetTypeManifestNormalizer`.
- Existing platform DQ and bootstrap seed tests remain green.
