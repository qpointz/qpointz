# Metadata — Schema Explorer Alignment

Deliver the physical schema explorer service (`/api/v1/schema/**`) that lets the UI browse Calcite
schemas, tables, and columns with metadata facets, while aligning schema-core and REST semantics.

This story is scoped to Schema Explorer delivery in two WIs (WI-093a backend, WI-093b UI).
Deferred editing/promotion work (WI-090, WI-091) remains tracked as follow-up story work.

## Work Items

- [ ] WI-093a — Schema Explorer REST Service (`WI-093a-schema-explorer-rest-service.md`)
  - Status: **implemented, under runtime stabilization/validation**
- [ ] WI-093b — Schema Explorer UI Wiring (`WI-093b-schema-explorer-ui-wiring.md`)
  - Status: **implemented, under runtime stabilization/validation**

---

## Current Status Snapshot (2026-03-23)

### Delivered so far
- Schema core alignment completed (`attribute*` -> `column*` in schema-core read models).
- Context-aware schema facet resolution implemented (metadata-context parsing + malformed-context `400`).
- New schema REST service module implemented (`data/mill-data-schema-service`) with OpenAPI docs,
  exception handler, unit tests, and integration tests.
- Autoconfiguration split completed:
  - metadata-owned schema facet auto-config moved to `metadata/mill-metadata-autoconfigure`
  - data autoconfigure wiring reduced accordingly.
- UI wiring updated to new schema contract (`/api/v1/schema/**`, context bootstrapping endpoint,
  discriminated union types, loader states, table/column lazy-loading behavior).

### Known runtime issues observed during integration
- UI instability and delayed rendering were observed when pointing to a live backend.
- Backend endpoint `GET /api/v1/schema/schemas?context=global` was measured as slow in runtime
  (multi-second response under local testing), which amplified UI delay and race behavior.
- Related indicators/facets had temporary gaps during rollout (top indicator visibility and relation
  payload shape parsing); UI-side fixes were applied in working code.

### Stabilization work in progress
- Added backend tree endpoint design (`GET /api/v1/schema/tree`) to reduce UI fan-out.
- UI switched to single tree-load call path + race guards + explicit loading states.
- Final runtime validation depends on backend restart/deploy of latest code and re-test against live
  `http://localhost:8080`.

### Story completion criteria for closure
- Live backend serves latest schema-service endpoints (including `/api/v1/schema/tree`).
- End-to-end UI flow is stable under repeated navigation (no flicker, no stale selection overwrite).
- Model load and entity switching latency are acceptable under local runtime conditions.
- WI-093a/WI-093b checkboxes can be marked complete only after the above runtime validation passes.

---

## Dependency Map

```
WI-093a ──► WI-093b (UI wiring needs live backend)
```

---

## Deferred Follow-up (out of scope for this story)

- WI-090 — Metadata User Editing (`WI-090-metadata-user-editing.md`)
- WI-091 — Metadata Promotion Workflow (`WI-091-metadata-promotion-workflow.md`)
- Security/context authorization rules for scope usage (who may request/resolve which contexts)
  are deferred to a dedicated follow-up story.
- UI context selector (interactive scope picker) is deferred; this story only reserves the
  backend/UI contract via `GET /api/v1/schema/context` with hardcoded `global`.

These items stay deferred and require a dedicated planning/design round before implementation.

---

## Prerequisites (already delivered)

- WI-086 — REST controller layer and `MetadataChangeObserver` chain
- WI-087 — JPA persistence and Flyway V4 schema (includes `metadata_promotion` table)
- WI-089 — Scope model (`MetadataScope`, `MetadataScopeService`, `MetadataScopeRepository`)
- WI-092 — `mill-ui` model view read-only binding

---

## Implementation Standards

### Language
- **Kotlin** for all production code: services, controllers, entities, repositories, adapters,
  domain types, configuration classes.
- **Java** for `@ConfigurationProperties`-bound classes only (processor generates metadata.json
  automatically).
- New modules added in this story must follow the same rule: Kotlin by default, with Java only for
  `@ConfigurationProperties` classes that require generated Spring configuration metadata.

### Documentation
- All production code must carry KDoc/JavaDoc down to method and parameter level.
- Test classes are exempt; public test utility helpers should be documented.
- This applies to both newly written code and modified existing production code in this story.

### Testing
- Unit tests in `src/test/`: pure logic, no Spring context, no DB.
- Integration tests in `src/testIT/`: Spring Boot slice or full context, H2 in-memory DB.
- Each new endpoint needs happy-path + failure-path assertions.
- Jacoco coverage threshold: 0.8.

### Cross-cutting quality gates (mandatory for WI-093a/093b)
- Logging all the way: controller/service/mapping flow should emit structured logs with key
  identifiers and context (without noisy payload dumping).
- OpenAPI documentation must be detailed at parity with `ai/mill-ai-v3-service` style
  (operations, responses, parameter docs, DTO field docs, examples).
- Backend tests must include both unit tests with mocks and controller integration tests in
  `src/testIT` using in-memory H2 app context.
- `context` malformed input handling must return `400 Bad Request` with a clear message and be
  documented in OpenAPI responses.

### Exception/HTTP mapping direction
- Use a global mapping approach from `MillStatusException` to HTTP responses (shared pattern),
  and apply consistently in schema and metadata services.

### Persistence contract purity
- Domain types in `mill-metadata-core` must be free of JPA/persistence annotations.
- JPA entities in `mill-metadata-persistence` map to domain types before returning them.
