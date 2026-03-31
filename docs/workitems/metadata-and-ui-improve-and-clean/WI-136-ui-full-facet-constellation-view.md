# WI-136 — UI full facet constellation view

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Planned  
**Type:** feat  
**Area:** ui

## Summary

Update **`ui/mill-ui`** Data Model / entity detail experience to consume **`facetsResolved`** (**WI-134**): show **captured + inferred** together; **inferred** rows read-only with **`originId`** pill; **captured** behaviour unchanged. When **`model`** is present (**WI-137**), tree shows **`model`** root per **SPEC §3f**.

Normative: [`SPEC.md`](SPEC.md) §0, §3e, §3f.

## Scope

- **`EntityDetails`** (or successor): primary data from **`facetsResolved`**. **Fallback:** if the field is absent (server not yet on **WI-134**), keep today’s behaviour **only** as a **temporary** mixed-deployment shim. **Document in code** the removal condition (e.g. “remove after **WI-134** is deployed to all target environments” or “remove behind release X”) — avoid an indefinite silent fallback that masks API drift.
- **TSX:** document exported functions / main components per **SPEC §0**.
- **`npm run test`** + **`npm run build`**.

## Out of scope

- OpenAPI generation (**WI-134**).

## Dependencies

- **WI-134**
- **WI-137**

## Acceptance criteria

- Mixed captured/inferred fixtures render per **SPEC**.
- Tests and build green in **`ui/mill-ui`**.

## Testing

```bash
cd ui/mill-ui && npm run test -- --run && npm run build
```

## Commit

One logical `[feat]` commit; update [`STORY.md`](STORY.md); clean tree.
