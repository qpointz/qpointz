# Story: eliminate-entity-kind

## Goal

Remove redundant **`MetadataEntity.kind`** / **`entity_kind`** now that entity instance URNs are
typed (`urn:mill/model/<class>:<id>`). Entity class is derived from the URN path segment.

## Origin

Split from **typed-entity-urns** (closed April 2026) so the codec, dataset migration, and
documentation could ship before this cross-cutting metadata refactor.

## Work Items

- [ ] WI-144 — Eliminate `entity.kind` (`WI-144-eliminate-entity-kind.md`)
