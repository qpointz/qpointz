# Metadata — Schema Explorer Alignment

Deliver the physical schema explorer service (`/api/v1/schema/**`) that lets the UI browse Calcite
schemas, tables, and columns with metadata facets, while aligning schema-core and REST semantics.

This story is scoped to Schema Explorer delivery in two WIs (WI-093a backend, WI-093b UI).
Deferred editing/promotion work (WI-090, WI-091) remains tracked as follow-up story work.

## Work Items

- [ ] WI-093a — Schema Explorer REST Service (`WI-093a-schema-explorer-rest-service.md`)
- [ ] WI-093b — Schema Explorer UI Wiring (`WI-093b-schema-explorer-ui-wiring.md`)

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
