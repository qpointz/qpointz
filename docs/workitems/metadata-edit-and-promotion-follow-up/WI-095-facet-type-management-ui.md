# WI-095 — Facet Type Management UI

Status: `planned`
Type: `✨ feature`
Area: `ui`, `metadata`
Backlog refs: `M-10`, `M-20`

---

## Goal

Provide a simple admin UI in `ui/mill-ui` to manage facet types and author descriptor fields for
view/edit rendering, aligned with WI-094 descriptor contracts.

---

## Prerequisites

- WI-094 — Facet Type Descriptor Foundation
- Metadata facet type REST endpoints in `metadata-facets` API

---

## Scope

### 1) Admin navigation and routing

- Under `/admin`, add left sidebar group `Model`.
- Add subitem `Facet Types` under `Model`.
- Add route for facet type management page (e.g. `/admin/model/facet-types`).

### 2) Feature flags (enabled by default)

- Add feature flags:
  - `adminModelNavEnabled` = `true`
  - `adminFacetTypesEnabled` = `true`
- Gate sidebar group and subitem via these flags.
- Update `ui/mill-ui/docs/FEATURE-FLAGS.md` with:
  - description
  - default value
  - scope (components/routes)
  - behavior when disabled

### 3) Facet types list view

- Table/list with at least:
  - `typeKey` (URN)
  - `title`
  - `enabled`
  - `mandatory`
  - `applicableTo`
  - `updatedAt`
- Actions:
  - create
  - edit
  - delete

### 4) Simple facet type authoring (MVP)

- Keep authoring intentionally simple.
- Descriptor metadata section:
  - `typeKey` (unique URN, required)
  - `title` (required)
  - `description` (required)
  - `enabled`
  - `mandatory`
  - `applicableTo` (URN list; empty means any target)
- Field definition editor (V1 subset):
  - required for each field: key, `title`, `description`, type
  - support types: `string`, `number`, `boolean`, `enum`, `date`, `datetime`, `array`, `object`
  - basic constraints: required, enum values, min/max, length where applicable

### 5) Validation and feedback

- Perform immediate client-side form checks for required metadata (`title`/`description`).
- Use server validation response to display exhaustive issue list (no fail-fast assumption).
- Handle deletion conflicts (`409 in use`) with clear UI feedback.

### 6) Serialization and interoperability

- UI reads/writes descriptor in JSON to API.
- Optional read-only YAML preview/export may be added if low effort; not required for MVP.
- Preserve URN-normalized values in UI state and API payloads.

---

## Out of Scope

- Advanced schema composition editor (`oneOf`/`anyOf`/`allOf`, conditional logic).
- Bulk facet type import UX.
- Rich visual schema builder (drag-and-drop).
- Full portal registry management workflow UI.

---

## Acceptance Criteria

- `Model` sidebar group is visible under admin when `adminModelNavEnabled=true`.
- `Facet Types` subitem is visible when `adminFacetTypesEnabled=true`.
- Both entries are hidden when corresponding flags are disabled.
- Feature flag docs are updated in `ui/mill-ui/docs/FEATURE-FLAGS.md`.
- User can list, create, edit, and attempt delete of facet types from UI.
- Authoring requires descriptor-level and per-field `title` and `description`.
- UI supports `applicableTo` URN selection and empty-as-any semantics.
- Delete `409` conflict is surfaced with actionable message.

