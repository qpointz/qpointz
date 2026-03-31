# WI-138 — Backend logical-layout inferred facets

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Planned  
**Type:** feat  
**Area:** data

## Summary

Implement a **`MetadataSource`** that emits **`INFERRED`** **`FacetInstance`** rows from the Mill **logical** schema model exposed by **`SchemaProvider`** / proto **`Schema` / `Table` / `Field`**. Payloads describe Mill-facing structure only (no physical secrets story in this WI).

Normative: [`SPEC.md`](SPEC.md) §0, §3f, §3g, §7.

## Scope

- Read-only source; **`originId`** stable (e.g. constant aligned with design docs).
- Attach facets per **SPEC §3f** (no global mandatory matrix); typical targets include **`model`**, schema, table, column entities.
- Integrate with merge wired in **WI-133** (registered bean or explicit list).
- **KDoc** / JavaDoc where public types are introduced.

## Out of scope

- Flow / JDBC / filesystem **physical** descriptors — **WI-139** / follow-up story.
- REST DTO shape — **WI-134** (source must feed existing merge path).

## Dependencies

- **WI-132**
- **WI-137**
- **WI-133**

## Acceptance criteria

- At least one integration or unit path proves inferred facets for schema / table / column (and **`model`** if applicable) with `FacetOrigin.INFERRED`.
- Tests run clean for modules touched (e.g. `:data:mill-data-schema-core:test`, backend tests as needed).

## Testing

```bash
./gradlew :data:mill-data-schema-core:test :data:mill-data-backends:test
```

## Commit

One logical `[feat]` commit; update [`STORY.md`](STORY.md); clean tree.
