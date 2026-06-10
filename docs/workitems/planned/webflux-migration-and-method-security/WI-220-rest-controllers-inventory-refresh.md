# WI-220 — REST controllers inventory refresh

Status: `planned`  
Type: `docs` (+ light validation)  
Area: `platform`, `security`  
Backlog refs: **P-34**

## Goal

Produce a **code-first inventory** of every production REST controller and mapped operation in the repo, suitable as the checklist for WebFlux migration and for **WI-227** (`@PreAuthorize` coverage).

## Scope

1. Update [`docs/design/security/REST-CONTROLLERS-INVENTORY.md`](../../../design/security/REST-CONTROLLERS-INVENTORY.md):
   - Rescan Gradle modules for `@RestController` / relevant request-mapping stereotypes.
   - List **class**, **base path**, **HTTP method**, **path**, **handler method** for each operation.
   - Note **public vs authenticated** expectations at the **path-prefix** level (cross-reference existing `SecurityFilterChain` matchers from [`docs/design/platform/CONFIGURATION_INVENTORY.md`](../../../design/platform/CONFIGURATION_INVENTORY.md) where helpful).
2. Refresh the optional runtime snapshot [`docs/design/security/_openapi-api-docs.json`](../../../design/security/_openapi-api-docs.json) when a representative OpenAPI host is available, or document **manual** verification steps if not.
3. Call out **discrepancies** between `webflux-migration-plan.md` controller names and current code (rename/split modules) so later WIs target real classes.

## Acceptance

- Inventory **summary counts** (controllers, operations) match a spot-check against code search.
- Every row needed for **WI-227** is identifiable (handler-level granularity).

## Depends on

None (first WI in story).
