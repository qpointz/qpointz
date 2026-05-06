# WI-256 — Saved queries persistence (Flyway + JPA)

Status: `planned`  
Type: `feature`  
Area: `persistence`, `ui` (contract)  
Backlog refs: **U-13**

## Goal

Add relational storage for **saved queries** matching the [`SavedQuery`](../../../../ui/mill-ui/src/types/query.ts) shape (id, name, description, sql, timestamps, tags).

## Scope

1. Flyway migration in [`mill-persistence`](../../../../persistence/mill-persistence/src/main/resources/db/migration) after latest `V*.sql`.
2. JPA entity in an appropriate module; respect **contract purity** ([`CLAUDE.md`](../../../../CLAUDE.md)) — domain types vs persistence mapping.
3. Optional seed rows mirroring [`mockQueries.ts`](../../../../ui/mill-ui/src/data/mockQueries.ts).

## Acceptance

- Migration applies cleanly on empty DB and existing CI fixture paths.
- Repository can **list all** and **find by id** for **WI-257**.

## Depends on

None (first WI in story).
