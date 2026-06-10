# WI-224 — Metadata reactive stack and WebFlux controllers

Status: `planned`  
Type: `refactoring`  
Area: `metadata`, `platform`  
Backlog refs: **P-34**, **P-1**

## Goal

Implement the **reactive metadata** repository/service layer and migrate **metadata HTTP** controllers in this track to **Mono/Flux** return types per the migration plan.

## Scope

1. **`mill-metadata-core`**: `ReactiveMetadataRepository`, `ReactiveFileMetadataRepository` (wrap blocking file repo), **`ReactiveMetadataService`** (or equivalent naming per repo conventions).
2. **`services/mill-metadata-service`**: migrate controllers listed in [`webflux-migration-plan.md`](../../../design/platform/webflux-migration-plan.md) **and** align with [**WI-220** inventory](../../../design/security/REST-CONTROLLERS-INVENTORY.md) (class names/paths may differ).
3. Schema explorer endpoints **if** they remain in scope for the same deployment artifact as this story; otherwise list **explicit exclusions** in this WI’s completion notes.

## Acceptance

- Metadata REST operations behave equivalently to pre-migration contract where automated tests exist; add/extend tests in **WI-228** if coverage was missing.

## Depends on

**WI-223**
