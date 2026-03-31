# WI-133 — Read-path facet merge

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Planned  
**Type:** feat  
**Area:** metadata, data

## Summary

Wire **multiple** **`MetadataSource`** beans into read orchestration using **composition-first** merge (**SPEC §3i**): merge **`List<FacetInstance>`** per entity after **`MetadataReadContext`**, without making **`@Primary` FacetRepository** the default global merge. Cover **both** REST resolution (**`FacetService.resolve`**, **`MetadataEntityController`**, and **`MetadataView`**) **and** schema-bound aggregation (**`SchemaFacetServiceImpl`** or equivalent). **`MetadataView`** stays in the tree as a façade: update it for **`MetadataReadContext`** — it is **not** dead code (**WI-130** excludes it).

Normative: [`SPEC.md`](SPEC.md) §0, §3b, §3i.

## Scope

- **`MetadataView`:** keep; align with **`MetadataReadContext`** and merged **`FacetInstance`** reads (**WI-130** explicitly excludes it from dead-code removal).
- Inject / compose registered **`MetadataSource`** instances (incl. **`RepositoryMetadataSource`**).
- Apply **scope** + **origin** muting consistently with **WI-132** / **§3h**.
- Keep combining rules simple (**§3b**): duplicate inferred facet **type** per entity → treat as misconfiguration per product rule (logging or assertion strategy as implemented).
- Optional: extract **`CompositeMetadataSource`** **only** if the same merge is reused (second consumer) — **SPEC §3i**.
- **Discouraged:** default **`AggregatingFacetRepository`** wrapping all **`FacetRepository`** reads unless **SPEC** and reviewers explicitly accept the tradeoff (raw-row callers).
- **KDoc** / JavaDoc on new services and helpers.

## Out of scope

- New inferred **`MetadataSource`** implementations (**WI-138**).
- HTTP / OpenAPI (**WI-134**).

## Dependencies

- **WI-132**
- **WI-137** — `model` entity exists for attach points used in merge tests / fixtures.

## Acceptance criteria

- Merged reads return **`FacetInstance`** rows with correct **`origin`**, **`originId`**, **`assignmentUid`** where applicable.
- Tests cover at least: captured-only merge; muting one `originId`; two sources contributing different facet types.
- Relevant module tests pass (metadata core/service, schema core as touched).

## Testing

```bash
./gradlew :metadata:mill-metadata-core:test :metadata:mill-metadata-service:test
./gradlew :data:mill-data-schema-core:test
```

(Adjust if modules change during implementation.)

## Commit

One logical `[feat]` commit; update [`STORY.md`](STORY.md); clean tree.
