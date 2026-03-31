# WI-132 — MetadataSource contract and RepositoryMetadataSource

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Planned  
**Type:** feat  
**Area:** metadata

## Summary

Introduce framework-free **read** contracts in **`mill-metadata-core`**: **`MetadataSource`**, **`FacetOrigin`**, **`MetadataReadContext`**, the **unified read** **`FacetInstance`**, and the **persisted row** **`FacetAssignment`**, plus **`RepositoryMetadataSource`**. **Order matters** (see below): the current production **`FacetInstance`** **is** the persistence row — it must be **renamed first** so the new read DTO can use the name **`FacetInstance`** without a compile clash.

Normative: [`SPEC.md`](SPEC.md) §0 (standards), §3a, §3h.

## Scope

### Sequence (do not reorder casually)

1. **Rename persistence row type:** today’s **`domain/facet/FacetInstance.kt`** (persisted row: `uid`, `entityId`, `facetTypeKey`, …) → **`FacetAssignment`** (or equivalent final name per **SPEC §3a**), including **`FacetRepository`** / **`FacetReadSide`** / **`FacetWriteSide`** method signatures, persistence adapter, and **all** call sites that meant “stored row.” This is a **large mechanical refactor**.
2. **Introduce new read DTO** **`FacetInstance`** (`origin`, `originId`, `assignmentUid`, payload, …) — **after** step 1.
3. **`RepositoryMetadataSource`:** maps **`FacetAssignment`** rows from **`FacetReadSide`** → **`List<FacetInstance>`** with `FacetOrigin.CAPTURED`.
4. Complete remaining contract work (same WI; **KDoc** on all public members — class, method, parameter):
    - **`MetadataSource`:** `originId`, `fetchForEntity(entityId, context)` (alias `contributeForEntity` optional).
    - **`MetadataReadContext`:** scopes + origins; semantics per **SPEC §3h** (muting by `originId`).
    - Repository naming toward **`EntityReadSide` / `EntityWriteSide`**, **`FacetReadSide` / `FacetWriteSide`** as needed for **`EntityRepository`** / **`FacetRepository`** evolution (per **SPEC §3a**); avoid a **`MetadataRepository`** type.
    - **`PersistenceCatalogReads`** remains distinct from **`MetadataSource`** (naming).
    - **Configuration** in autoconfigure modules: **SPEC §0** (`@ConfigurationProperties` Java or Kotlin + additional metadata JSON).

## Out of scope

- Merging inferred sources (**WI-133**).
- REST / OpenAPI changes (**WI-134**), except types consumed later.

## Dependencies

None (foundational).

## Acceptance criteria

- Types compile in **`mill-metadata-core`**; **`RepositoryMetadataSource`** unit-tested with **`FacetReadSide`** fake (muted origin → empty; persisted rows → `CAPTURED` + `originId`).
- **`./gradlew :metadata:mill-metadata-core:test`** succeeds.

## Testing

```bash
./gradlew :metadata:mill-metadata-core:test
```

## Commit

One logical `[feat]` commit; update [`STORY.md`](STORY.md) § Work items; clean tree before next WI.
