# Story: typed-entity-urns

## Goal

Replace the flat, typeless `urn:mill/metadata/entity:<local>` entity URN form with
**typed URNs** that embed the entity class in the URN path segment:

```
urn:mill/model/schema:<schema>
urn:mill/model/table:<schema>.<table>
urn:mill/model/attribute:<schema>.<table>.<column>
urn:mill/model/model:model-entity
urn:mill/model/concept:<id>
```

The entity type is then fully derivable from the URN — no separate `entity.kind` field is
needed for relational or first-class entities. The story also removes `entity.kind` from
the domain and persistence layer.

## Motivation

- Current URNs convey no type information; the data layer infers type by dot-counting, and
  the `entity.kind` field is a redundant parallel channel for the same information.
- Typed URNs are self-describing, enable URI/slug routing per entity class, and align with
  the `urn:mill/<group>/<class>:<id>` grammar already documented in `metadata-urn-platform.md`.
- The metadata layer already stores URNs as opaque strings — no metadata-core code changes.

## Work Items

- [x] WI-142 — Typed URN codec (`WI-142-typed-urn-codec.md`)
- [ ] WI-143 — Dataset and persistence migration (`WI-143-dataset-persistence-migration.md`)
- [ ] WI-144 — Eliminate `entity.kind` (`WI-144-eliminate-entity-kind.md`)
- [ ] WI-145 — Design documentation update (`WI-145-design-doc-update.md`)
