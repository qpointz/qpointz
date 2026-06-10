# WI-137 — Model root entity

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Done  
**Type:** feat  
**Area:** data, metadata, ui

## Summary

Add a first-class **`model`** metadata entity with stable identity **`urn:mill/metadata/entity:model-entity`** and **`entityKind = model`**, owned by **`mill-data-schema-*`**, so instance-level and top-level facets have a real attachment point. **`metadata`** stays ignorant of special meaning.

Normative: [`SPEC.md`](SPEC.md) §0, §3f.

## Scope

- Taxonomy / DTO / explorer integration: **`model`** above schemas in the schema/model read model.
- **`SchemaExplorerService`** (and related DTOs) expose **`model`** as selectable entity.
- **`assignableTo`** / manifests / validation paths include **`model`** where facet types allow.
- **JavaDoc / KDoc** to parameter level on new public APIs.

## Out of scope

- Implementing inferred **`MetadataSource`** payloads (**WI-138**).
- UI constellation rendering (**WI-136**), beyond what is needed to **surface** `model` in explorer APIs.

## Dependencies

- **WI-132** — stable type names and read context for facet reads if wire-up touches metadata core.

## Acceptance criteria

- Stable URN + `entityKind` match **SPEC §3f**.
- Integration tests or service tests prove **`model`** appears in explorer responses and can hold facets without affecting relational SQL structure.
- **`./gradlew`** tests agreed in implementation for touched modules pass (e.g. `:data:mill-data-schema-core:test`, `:data:mill-data-schema-service:test`).

## Testing

```bash
./gradlew :data:mill-data-schema-core:test :data:mill-data-schema-service:test
```

## Commit

One logical `[feat]` commit; update [`STORY.md`](STORY.md); clean tree.
