# Metadata Facet Types as JSON Schema

Expose metadata facet type payload contracts as generated JSON Schema so external consumers can
perform basic payload validation and agents/tools can carry the facet model as standard JSON media.
Keep Mill's `FacetPayloadSchema` manifest as the source of truth; JSON Schema is a projection.

The story also adds an Admin Model UI view that lets operators inspect a facet type as JSON Schema
without editing that generated representation.

## Goals

1. Generate draft-07-compatible JSON Schema from `FacetPayloadSchema`.
2. Normalize required-field semantics so field-level `required` flags drive JSON Schema `required`.
3. Expose JSON Schema from the metadata facet type API for one facet type at a time.
4. Add a read-only JSON Schema inspection mode to `ui/mill-ui` facet type editing.
5. Document and test the contract well enough that later server-side validation can consume it.

## Non-Goals

- Do not replace `FacetPayloadSchema` with raw JSON Schema in storage or REST manifests.
- Do not make JSON Schema editable in the UI.
- Do not encode Mill semantics such as `applicableTo`, `enabled`, `mandatory`, scope ownership,
  merge behavior, or `targetCardinality` as JSON Schema validation rules.
- Do not add full server-side facet payload validation in this story.

## Work Items

- [x] WI-379 — Facet payload JSON Schema generator (`WI-379-facet-payload-json-schema-generator.md`)
- [x] WI-380 — Facet type JSON Schema REST API (`WI-380-facet-type-json-schema-rest-api.md`)
- [x] WI-381 — Facet type UI JSON Schema view (`WI-381-facet-type-ui-json-schema-view.md`)
- [x] WI-382 — Facet JSON Schema docs and verification (`WI-382-facet-json-schema-docs-and-validation.md`)

## Implementation Notes

- The generated schema describes a single facet payload instance. For `MULTIPLE` facet types,
  cardinality is exposed as `x-mill-targetCardinality`, not as an array wrapper.
- Mill-specific metadata is carried as annotations such as `x-mill-facetTypeUrn`,
  `x-mill-stereotype`, `x-mill-category`, `x-mill-applicableTo`, and `x-mill-schemaVersion`.
- Required-field normalization should prefer `FacetPayloadField.required == true`; object-level
  `required` lists are compatibility data and should be aligned with field flags.
