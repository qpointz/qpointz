# WI-135 — Mutation guards for inferred facets

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Planned  
**Type:** feat  
**Area:** metadata

## Summary

Ensure **`FacetService`** and REST facet mutation endpoints reject updates/deletes against **non-persisted** or **INFERRED** targets (clear **404** / **422** semantics). Aligns with **SPEC §3d**.

Normative: [`SPEC.md`](SPEC.md) §0, §3d.

## Scope

- Resolve by `assignmentUid`; not found → **404**.
- Wrong origin / synthetic leakage → **422** with clear message (exact mapping per implementation + OpenAPI).
- **KDoc** on service methods.

## Out of scope

- Read API shape (**WI-134**) beyond documenting error responses on mutation routes.

## Dependencies

- **WI-132** — `FacetOrigin` and model.
- **WI-133** — merged read semantics stable enough to reason about uid provenance.

## Acceptance criteria

- Unit / slice tests: unknown uid, captured happy path, rejected inferred/synthetic path.
- **`./gradlew :metadata:mill-metadata-core:test :metadata:mill-metadata-service:testIT`** (or equivalent) passes.

## Testing

```bash
./gradlew :metadata:mill-metadata-core:test :metadata:mill-metadata-service:testIT
```

## Commit

One logical `[feat]` commit; update [`STORY.md`](STORY.md); clean tree.
