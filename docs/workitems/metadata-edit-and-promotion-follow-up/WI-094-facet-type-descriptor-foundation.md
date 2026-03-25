228         # WI-094 — Facet Type Descriptor Foundation

Status: `planned`
Type: `✨ feature`
Area: `metadata`, `ui`
Backlog refs: `M-10`, `M-20`

---

## Goal

Establish `FacetTypeDescriptor` as the canonical facet-type contract for registration, assignment,
validation, and UI rendering, with JSON/YAML descriptor inputs and deterministic URN normalization.

This WI provides the foundation for WI-090 facet editing and WI-091 promotion workflows.

---

## Scope

### 1) Descriptor model and API input formats

- Keep `FacetTypeDescriptor` as the canonical domain and API type.
- Every facet type is identified by a globally unique URN `typeKey`.
- `POST /api/v1/metadata/facets` accepts a single descriptor payload in:
  - `application/json`
  - `application/yaml` / `application/x-yaml` / `text/yaml`
- Descriptor input may use alias keys for convenience; backend normalizes to URNs.
- Descriptor must include:
  - descriptor-level `title` and `description`
  - per-field `title` and `description` for all declared fields (UI-required metadata)

### 2) Practical URN policy

- API/import may accept aliases like `table`.
- Backend normalizes immediately to URN form:
  - `urn:mill/metadata/entity-type:table`
- Persistence and responses use URNs only.
- Duplicate facet type URNs are rejected (`409 Conflict`) across API/import/sync flows.

### 3) Assignment and applicability rules (descriptor-configured)

- `applicableTo` is configured in descriptor and interpreted as:
  - omitted or empty => applicable to any entity type
  - non-empty => applicable only to listed entity-type URNs
- Validation must check both:
  - descriptor assignment applicability (`facetType` on entity type)
  - facet payload/content shape

### 4) Server-side permissive validator (exhaustive issue collection)

- Java validator validates facet assignment + content against descriptor.
- Validation is permissive and exhaustive: does not fail fast and accumulates all issues in one run.
- Output includes full list of issues with at least:
  - `severity`
  - `code`
  - `path`
  - `message`
- Validation execution is always local to the Mill instance.

### 5) Safe facet-type delete policy

- Facet type deletion is allowed only when no entities use that facet type.
- If in use, deletion returns `409 Conflict`.
- Add repository capability to query facet-type usage efficiently.

### 6) UI descriptor-driven rendering foundation

- UI uses descriptor fields to build:
  - view mode presentation
  - edit mode form controls
- V1 renderer subset:
  - strings, numbers, booleans, enums, arrays, nested objects
  - defer `oneOf`/`anyOf`/`allOf` and conditional schemas

### 7) Schema interoperability

- Descriptor remains canonical.
- Provide deterministic conversion of descriptor schema to JSON Schema for external validation/tooling.
- Date/time semantic support is modeled in descriptor types and mapped consistently to JSON Schema.

### 8) Facet type registry strategy boundary

- Add metadata configuration for registry strategy:
  - `mill.metadata.facet-type-registry.type` with default `local`
- `local` means facet types are loaded from local persistence (`FacetTypeRepository` / same DB).
- `portal` (future strategy) is a source of facet type descriptors only.
- Validation logic remains local regardless of registry strategy; registry-supplied descriptor data
  must be sufficient for local assignment/content validation.

### 9) OpenAPI contract updates (mandatory)

- Update OpenAPI documentation for facet type management endpoints to match WI-094 behavior.
- Ensure all API errors are raised and mapped via global
  `core/mill-core/src/main/java/io/qpointz/mill/excepions/statuses/MillStatusException.java`.
- Document accepted request content types for descriptor create/update:
  - `application/json`
  - `application/yaml` / `application/x-yaml` / `text/yaml`
- Document descriptor contract fields including:
  - unique `typeKey` URN
  - descriptor-level `title` / `description`
  - per-field `title` / `description`
  - `applicableTo` semantics (empty/omitted means any target)
- Document validation and delete conflict responses with concrete error examples:
  - duplicate URN (`409`)
  - facet type in use on delete (`409`)
  - malformed descriptor payload (`400`)
- Ensure API examples use URN-normalized values.

---

## Out of Scope

- Full advanced JSON Schema composition support (`oneOf`, `anyOf`, `allOf`).
- Declarative custom validation DSL/rules engine (future WI).
- Full UI authoring experience for all descriptor edge cases.

---

## Acceptance Criteria

- Facet type create endpoint accepts single JSON and YAML descriptor payloads.
- Each facet type has a unique URN `typeKey`; duplicates are rejected with `409`.
- Aliases are accepted at boundaries; persisted/read values are URN-normalized.
- Descriptor-level `title`/`description` and per-field `title`/`description` are required.
- Applicability rules are enforced from descriptor `applicableTo` semantics.
- Server validator returns exhaustive issue lists (no fail-fast), including multiple issues in one run.
- Delete facet type fails with `409` when in use by any metadata entity.
- UI can render V1 view/edit controls from descriptor fields without hardcoded facet classes.
- Descriptor-to-JSON-Schema conversion is deterministic and covered by tests.
- Registry strategy defaults to `local`; non-local registry does not perform remote runtime validation.
- OpenAPI for facet type endpoints is updated and consistent with implemented descriptor/validation behavior.
- All REST error cases are wired through global `MillStatusException` mapping (no endpoint-local
  ad-hoc error contracts).

---

## Implementation Notes

- Prefer reusing existing `FacetCatalog`, `FacetTypeDescriptor`, and `MetadataFacetController`
  paths to minimize contract churn.
- Concrete platform facet classes remain optional adapters, not a requirement for custom facets.
- Use global `MillStatusException` consistently for malformed input, conflict, and not-found cases
  so HTTP mapping remains shared across metadata/schema services.
- Add both unit and integration tests for:
  - descriptor parsing/normalization
  - assignment and content validation behavior
  - delete-in-use guard
  - JSON/YAML parity

