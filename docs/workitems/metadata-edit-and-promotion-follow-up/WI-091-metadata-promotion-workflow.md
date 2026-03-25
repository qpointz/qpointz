# WI-091 — Metadata Promotion Workflow

Status: `planned`
Type: `✨ feature`
Area: `metadata`, `ui`
Backlog refs: `M-20`

> **Story scope note:** This WI is in scope for the follow-up story
> `metadata-edit-and-promotion-follow-up`.
>
> **Implementation note:** Promotion is a multi-party workflow (request → review → approve/reject)
> and requires explicit decisions on reviewer roles, conflict strategy, and workflow UX.

---

## Goal

Allow a user to request that a facet they have authored in their personal or team scope be
promoted to a broader scope (e.g. global). A reviewer can approve or reject the request. On
approval, the facet payload is copied from the source scope into the target scope.

---

## Prerequisites

- WI-087 — `metadata_promotion` table (already in V4 SQL)
- WI-089 — Scope model and `MetadataScopeService`
- WI-090 — Write path (`MetadataEditService.setFacet`) used by `executePromotion`

---

## Planned Scope

### Domain types in `mill-metadata-core`

- `MetadataPromotion` — pure data class: `promotionId`, `entityId`, `facetType`,
  `sourceScopeId`, `targetScopeId`, `status: PromotionStatus` (PENDING / APPROVED / REJECTED),
  `requestedBy`, `reviewedBy?`, `requestedAt`, `reviewedAt?`, `notes?`.
- `MetadataPromotionRepository` — interface: `save`, `findById`, `findByEntityIdAndFacetType`,
  `findByStatus`, `findPendingForTargetScope`.
- `MetadataPromotionService`:
  - `requestPromotion(entityId, facetType, sourceScope, targetScope, requestedBy, notes?)` — creates PENDING.
  - `approvePromotion(promotionId, reviewedBy)` — sets APPROVED, calls `executePromotion`.
  - `rejectPromotion(promotionId, reviewedBy, notes?)` — sets REJECTED.
  - `executePromotion` (private) — loads source facet, calls `MetadataEditService.setFacet` on target scope.
  - `getPendingForScope(targetScopeKey)` — review queue.

### REST endpoints — `MetadataPromotionController`

`@RequestMapping("/api/v1/metadata/promotions")`

```
POST   /api/v1/metadata/promotions              → 201 (request)
GET    /api/v1/metadata/promotions/pending      → pending queue for reviewer
POST   /api/v1/metadata/promotions/{id}/approve → 200
POST   /api/v1/metadata/promotions/{id}/reject  → 200
GET    /api/v1/metadata/entities/{id}/facets/{type}/promotions → history
```

### JPA adapter in `mill-metadata-persistence`

- `JpaMetadataPromotionRepository` implements `MetadataPromotionRepository`.
- Delegates to `MetadataPromotionJpaRepository` (already defined in WI-087 Spring Data repo list).

---

## Open Design Questions

1. Who can approve a promotion to `global`? (any admin? scope owner? separate reviewer role?)
2. Conflict behavior: target scope already has a different value — auto-overwrite or require
   explicit `force`?
3. Should `executePromotion` delete the source scope facet, or keep both? (current plan: keep source)
4. Auto-approve path: should promotions to non-global scopes from an admin skip review?
5. Review UI: separate page? notification-driven? inline in entity detail panel?
6. Scope/context authorization policy: which scopes a caller may request/review/approve for
   cross-user contexts, and how violations are surfaced/audited.

---

## Acceptance Criteria (to be written during implementation)

- A user can request promotion of a facet from their scope to a target scope.
- Promotion records capture source/target scope, actor, timestamps, status.
- Approving a promotion copies the facet payload to the target scope via `MetadataEditService`.
- Source scope facet is preserved after promotion.
- `GET .../pending` returns all PENDING promotions for the caller's managed scopes.
- All operations emit `MetadataChangeEvent` and produce audit rows.

