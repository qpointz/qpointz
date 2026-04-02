# WI-145 — Design documentation update

**Story:** typed-entity-urns  
**Status:** Open  
**Type:** docs  
**Area:** docs

## Summary

Update design documents to reflect the typed URN form as the authoritative grammar,
remove references to `entity.kind`, and ensure the URN platform table is consistent
with the as-built state after WI-142–WI-144.

## Scope

### `docs/design/metadata/metadata-urn-platform.md`

- Replace the "Relational metadata entity URNs (current JDBC binding)" section with the
  new typed form. The `urn:mill/metadata/entity:…` form is no longer used.
- Update the URN grammar table: rows for `urn:mill/model/schema:…`,
  `urn:mill/model/table:…`, `urn:mill/model/attribute:…` were placeholders — replace
  with the actual `urn:mill/data/<class>:<id>` entries now in use.
- Remove the note about `MetadataEntityUrnCodec` producing `urn:mill/metadata/entity:…`;
  update to typed form.
- Update the `MetadataEntityUrnCodec` section to reflect new typed output.
- Note the removal of `entity.kind`; state that the class segment in the URN replaces it.

### `docs/design/metadata/mill-metadata-domain-model.md`

- Section 1 (Entity): remove mention of `kind` as an optional domain field.
- Update the relational catalog binding description to reference the new typed URN form.
- Section 6 (Persistence mapping): confirm `entity_kind` column is gone.

### `docs/design/metadata/metadata-canonical-yaml-spec.md`

- Remove `kind:` from entity document format examples.
- Add note that entity type is conveyed by the URN class segment.

### `docs/design/metadata/metadata-service-design.md` (if applicable)

- Scan for any reference to `entity.kind` or `urn:mill/metadata/entity:` and update.

## Out of scope

- Public user docs (`docs/public/`) — update only if the typed URN format is user-visible
  API surface (REST responses carry `metadataEntityId`; if public docs describe that field,
  add a brief note about the new format).

## Acceptance criteria

- `metadata-urn-platform.md` grammar table and binding sections reflect typed URNs.
- No design doc references `urn:mill/metadata/entity:` as the active form.
- No design doc documents `entity.kind` as a live field.
- All cross-references between design docs remain consistent.

## Commit

One logical `[docs]` commit; update `STORY.md`; clean tree.
