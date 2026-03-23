# WI-090 — Metadata User Editing

Status: `planned`
Type: `✨ feature`
Area: `metadata`, `ui`
Backlog refs: `M-10`, `M-20`

> **Story scope note:** This WI is out of scope for the current schema-alignment story
> (`WI-093a`/`WI-093b`) and remains deferred to a dedicated follow-up story.
>
> **Deferred — reason:** Facet types are dynamic (deployment-configurable, not a fixed enum).
> A generic editing UI cannot be built against a static field list — it requires a schema-driven
> form renderer (read `FacetTypeDescriptor.contentSchemaJson` → generate input controls) or a
> per-facet-type custom editor component. Neither the renderer contract nor the UX pattern has been
> designed. Implement after WI-089 scope model is live and the form-generation strategy is settled.

---

## Goal

Enable authenticated users to create, update, and delete metadata entities and their facets via
the REST API, with each write operation recorded in the operation audit log and scoped to the
actor's personal or team scope.

---

## Prerequisites

- WI-086 — REST controller layer and `MetadataChangeObserver` chain
- WI-087 — JPA persistence and `metadata_operation_audit` table
- WI-089 — Scope model and `MetadataScopeService`

---

## Planned Scope (deferred)

### Write endpoints on `MetadataEntityController`

```
POST   /api/v1/metadata/entities                       → 201 + Location
PUT    /api/v1/metadata/entities/{id}                  → 200
PATCH  /api/v1/metadata/entities/{id}                  → 200
DELETE /api/v1/metadata/entities/{id}                  → 204
PUT    /api/v1/metadata/entities/{id}/facets/{typeKey} ?context=<scope-urn>  → 200
DELETE /api/v1/metadata/entities/{id}/facets/{typeKey} ?context=<scope-urn>  → 204
GET    /api/v1/metadata/entities/{id}/history                               → 200 audit log
```

### Service layer — `MetadataEditService` in `mill-metadata-core`

- `createEntity(entity, actorId)` — validate, stamp audit fields, save, emit `EntityCreated`.
- `updateEntity(id, patch, actorId)` — load, merge, validate, save, emit `EntityUpdated`.
- `deleteEntity(id, actorId)` — scope ownership check, delete, emit `EntityDeleted`.
- `setFacet(entityId, facetType, scopeKey, data, actorId)` — validate against `FacetTypeDescriptor.contentSchemaJson`, check scope ownership, save, emit `FacetUpdated`.
- `deleteFacet(entityId, facetType, scopeKey, actorId)` — remove scope entry, emit `FacetDeleted`.
- Throws `MillStatuses.notFoundRuntime`, `MillStatuses.forbiddenRuntime`, `MillStatuses.unprocessableRuntime`.

### Scope ownership check

A user may write to:
- `urn:mill/metadata/scope:global` — requires an admin role
- `urn:mill/metadata/scope:user:<principalId>` — their own personal scope (always allowed)
- `urn:mill/metadata/scope:team:<teamName>` — if the principal is a member of that team

### UI editing forms (mill-ui)

Schema-driven form renderer: reads `FacetTypeDescriptor.contentSchemaJson` (JSON Schema) and
generates input controls. Requires a design document defining the renderer contract before
implementation begins.

---

## Open Design Questions

1. Which JSON Schema dialect does `contentSchemaJson` follow? (draft-07 / draft-2020-12?)
2. How are nested/complex facet payloads rendered? (recursive form? YAML editor fallback?)
3. What validation feedback does the API return for schema violations? (`422` + field errors?)
4. Can a user delete another user's personal-scope facet? (no — but who enforces: service or DB?)
5. Scope/context authorization policy: which context scopes may a caller request or write against
   (e.g. `user:alice` using `user:bob`, custom/chat scopes), and where is it enforced?

---

## Acceptance Criteria (to be written during implementation)

- Authenticated user can create/update/delete metadata entities via API.
- Writes to `user:<principalId>` scope always allowed; writes to `global` require admin role.
- Every write emits an observer event and produces an audit row in `metadata_operation_audit`.
- `GET .../history` returns audit rows for the entity ordered by `occurred_at` descending.
- UI form renderer generates controls from `contentSchemaJson` for at least the `descriptive`
  facet type.
