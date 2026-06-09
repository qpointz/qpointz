# WI-256 — Saved queries persistence (Flyway + JPA)

Status: `planned`  
Type: `feature`  
Area: `persistence`, `ui` (contract)  
Backlog refs: **U-13**

## Goal

Add relational storage for **saved queries** matching the [`SavedQuery`](../../../../ui/mill-ui/src/types/query.ts) shape (`id`, `name`, `description`, `sql`, `createdAt`, `updatedAt`, `tags`).

## Module layout (define before coding)

Follow **contract purity** ([`CLAUDE.md`](../../../../CLAUDE.md)); mirror **`mill-ai-v3`** + **`mill-ai-v3-persistence`**:

| Module | Contents |
|--------|----------|
| `data/mill-analysis-queries` (or `services/mill-analysis-queries-api`) | **`SavedQuery`** domain record; **`SavedQueryCatalog`** port (`findAll()`, `findById(id)`) — **no JPA** |
| `persistence/mill-analysis-persistence` | **`SavedQueryEntity`**, **`JpaSavedQueryRepository`**, **`JpaSavedQueryCatalog`** adapter — maps entity ↔ domain |
| *(WI-258)* `services/mill-analysis-queries-service` | REST only; depends on **port**, never on entity types |

- Entities live under `io.qpointz.mill.persistence.analysis.jpa.*` (fits existing `@EntityScan("io.qpointz.mill.persistence")`).
- Flyway stays centralized in [`mill-persistence`](../../../../persistence/mill-persistence/src/main/resources/db/migration).

## Scope

1. **Flyway** `V8__saved_queries.sql` after latest `V7__*.sql`:
   - Table `saved_query`: `id` (PK), `name`, `description`, `sql` (CLOB), `created_at`, `updated_at` (TIMESTAMP — H2 compat), `tags` (JSON or equivalent).
   - Index on `updated_at` for list ordering.
2. **Persistence module** Gradle setup + `test` / `testIT` suites per [`CLAUDE.md`](../../../../CLAUDE.md).
3. **Seed data (required):** Flyway `INSERT`s mirroring the six entries in [`mockQueries.ts`](../../../../ui/mill-ui/src/data/mockQueries.ts) so dev smoke and demos have a non-empty sidebar. Document seeds as **fixture data**, not user-created content.

## Acceptance

- Migration applies cleanly on empty DB and existing CI fixture paths.
- **`SavedQueryCatalog.findAll()`** and **`findById(id)`** return domain types only (no entity leakage).
- Seed rows present after migrate (six queries with stable ids from mock data).
- Unit or `testIT` coverage on adapter round-trip.

## Depends on

None (first WI in story).
