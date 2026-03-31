# WI-134 — Resolved facets read API and OpenAPI

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Planned  
**Type:** feat  
**Area:** data, metadata, ui

## Summary

Expose **`facetsResolved`** (or equivalent name aligned with **SPEC §3c**) on read responses for **schema-facing** and **metadata entity** GETs, with **`scope`** and **`origin`** query parameters per **SPEC §3h**. One unified DTO for all origins. Update **`ui/mill-ui`** hand-written HTTP clients. **`ui/mill-grinder-ui`** is **abandoned** — do **not** regenerate or build it (**SPEC §0**; list it in **`.cursorignore`** at repo root).

Normative: [`SPEC.md`](SPEC.md) §0, §3c, §3h.

## Scope

- Add list field with `origin`, `originId`, `assignmentUid`, payload, facet type identity per **SPEC**.
- **Rename** read query param **`context` → `scope`**; add optional **`origin`** (comma-separated URNs or slugs).
- **OpenAPI** + controller annotations — **all** endpoints, query params, and response shapes touched by this story must match what ships (per **SPEC §0** OpenAPI rule); include new/changed **error** responses where **WI-135** defines them.
- **JavaDoc** / KDoc on controllers and DTOs.
- **`ui/mill-ui`:** search and update all services that pass **`context=`** on reads to **`scope=`** (and **`origin`** if added). **`npm run build`** must pass.

## Out of scope

- **`ui/mill-grinder-ui`** — legacy app; no fixes, codegen, or CI expectation from this story.
- Data Model constellation layout (**WI-136**) — but **WI-134** still owns **query-param** fixes in **mill-ui** services used by explorer/entity reads.

## Dependencies

- **WI-133**
- **WI-138** — so first shipped **`facetsResolved`** can include **INFERRED** logical-layout rows in contract tests.

## Acceptance criteria

- Generated **OpenAPI** artifact(s) for affected **services** **include** every changed path, query parameter, and response component (spot-check vs controller code; undocumented API changes are **not** complete).
- Contract tests: **CAPTURED** row with `assignmentUid`; **INFERRED** row with null/absent uid and stable `originId`.
- **`./gradlew`** for affected services (e.g. `:data:mill-data-schema-service:testIT`, metadata service tests as applicable).
- **`cd ui/mill-ui && npm run build`**

## Testing

```bash
./gradlew :data:mill-data-schema-service:testIT
cd ui/mill-ui && npm run build
```

## Commit

One logical `[feat]` commit; update [`STORY.md`](STORY.md); clean tree.
