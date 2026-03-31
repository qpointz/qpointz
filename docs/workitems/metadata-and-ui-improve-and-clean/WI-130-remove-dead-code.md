# WI-130 — Remove dead code (final sweep)

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Pending  
**Type:** change  
**Area:** metadata

## Summary

Remove metadata-module types with **no** production consumers **after** larger refactors landed, so newly orphaned code is visible. Inventory below was a **planning snapshot** — **re-verify** before deleting.

Normative: [`SPEC.md`](SPEC.md) §0, §1.

## Scope

### Tier 1 — unused (verify)

| Class | Module / Path |
|-------|---------------|
| `FacetTypeConflictException` | `mill-metadata-core` / `domain/facet/exceptions/FacetTypeManifestExceptions.kt` |
| `FacetTypeNotFoundException` | same |
| `FacetTypeDescriptorDto` | `mill-metadata-service` / `api/dto/` |
| `FacetDto` | `mill-metadata-service` / `api/dto/` |

### Excluded from dead-code removal (active façade)

- **`MetadataView`** (`mill-metadata-core` / `service/MetadataView.kt`) — thin read helper calling **`facetService.resolve`**; **WI-133** migrates it to **`MetadataReadContext`**. **Do not delete** here unless **WI-133** replaces it first and all callers are updated.

### Tier 2 — clusters (verify)

| Class | Module / Path |
|-------|---------------|
| `MetadataSnapshotService`, `DefaultMetadataSnapshotService` | `mill-metadata-core` / `service/` |
| `ResourceResolver`, `SpringResourceResolver`, `ClasspathResourceResolver` | core / autoconfigure / test |

### Tier 3–4 — orphan / legacy-ui-only

| Class | Notes |
|-------|--------|
| `PlatformFacetTypeDefinitions` | Verify usage |
| `TreeNodeDto`, `SearchResultDto` | If only **retired** UI consumed them |

**Keep:** classes inside **`FacetPayloadFieldJsonSerde`** used by Jackson; **`FacetTypeManifestInvalidException`**.

## Out of scope

- Facet class demotion (**WI-140**) — separate deletes.

## Dependencies

- **WI-140** complete (or document explicit exceptions if run earlier).

## Acceptance criteria

- **`./gradlew :metadata:mill-metadata-core:test`** and **`:metadata:mill-metadata-service:test`** pass; broader **`./gradlew build`** if scope crosses modules.
- Inventory updated in this file or **SPEC §1** if items change.

## Testing

```bash
./gradlew :metadata:mill-metadata-core:test :metadata:mill-metadata-service:test
./gradlew build
```

## Commit

One logical `[change]` commit; update [`STORY.md`](STORY.md); clean tree.
