# WI-145 — Design & public documentation (typed entity URNs)

**Story:** typed-entity-urns  
**Status:** Done  
**Type:** docs  
**Area:** docs

## Summary

Refresh **design** and **public** documentation so **typed** `urn:mill/model/…` instance URNs are the authoritative description for relational and logical-model metadata entities, and the retired flat `urn:mill/metadata/entity:…` form is clearly historical.

## Delivered updates

| Area | Files |
|------|--------|
| URN platform | `docs/design/metadata/metadata-urn-platform.md` |
| Domain model | `docs/design/metadata/mill-metadata-domain-model.md` |
| Canonical YAML | `docs/design/metadata/metadata-canonical-yaml-spec.md` |
| Synthetic → canonical writer | `docs/design/metadata/metadata-synthetic-to-canonical-writer-handoff.md` |
| Public / operators | `docs/public/src/mill-ui.md`, `docs/public/src/metadata/operators.md`, `docs/public/src/metadata/system.md`, `docs/public/src/metadata/index.md` |

## Notes

- **`urn:mill/metadata/entity-type:…`** remains the vocabulary for **facet-type applicability**; it is not an instance URN.
- Optional **`MetadataEntity.kind`** / **`entityKind`** in YAML is documented as redundant with the URN class segment; removal is **WI-144**.

## Commit

`[docs]` — aligns with typed-entity-urns story.
