# WI-267 - Analysis mode product model and API contract

Status: `planned`
Type: `feature`, `docs`
Area: `ui`, `services`, `product`
Backlog refs: **U-14**

## Goal

Define the shared product model for Mill Analysis so **SQL Analysis** and **Visual Analysis** are
first-class analysis types under one Analysis area.

This WI does not implement the full Visual Analysis experience. It establishes the canonical nouns,
API shapes, persistence boundaries, and migration path from the current saved-query-only model.

## Scope

1. Define top-level `Analysis` contract:
   - `id`
   - `type`: `SQL` or `VISUAL`
   - `name`
   - `description`
   - `tags`
   - `owner` / `createdBy`
   - `createdAt`, `updatedAt`
   - `specVersion`
   - `spec`
2. Define mode-specific specs:
   - `SqlAnalysisSpec`: SQL text plus optional parameter definitions.
   - `VisualAnalysisSpec`: paths, boards, parameters, optional dashboard layout.
3. Decide whether current `SavedQuery` becomes:
   - an alias/view over `Analysis(type=SQL)`, or
   - a backwards-compatible DTO backed by the new analysis table.
4. Define list/get API surface:
   - `GET /api/v1/analyses`
   - `GET /api/v1/analyses/{analysisId}`
   - Optional filtered list: `GET /api/v1/analyses?type=SQL|VISUAL`
5. Define compatibility posture for existing query endpoints:
   - Existing `GET /api/v1/queries` remains available for current SQL Analysis clients until the UI
     migrates.
   - `POST /api/v1/queries/execute` remains out of scope; execution stays under `/api/v1/query/**`.
6. Capture terminology:
   - User-facing: **SQL Analysis**, **Visual Analysis**, **Path**, **Board**.
   - Internal: `Analysis`, `AnalysisType`, `AnalysisSpec`, `BoardSpec`.
   - Avoid `Contour` in product strings, code packages, or docs except design references.

## Acceptance

- Story/design docs define the canonical `Analysis` shape and mode-specific specs.
- Backwards compatibility with saved SQL queries is explicit.
- REST API naming is documented and does not conflict with `/api/v1/query/**`.
- Product terminology is consistent across story docs and planned UI docs.
- Open questions are captured for implementation WIs rather than left implicit.

## Dependencies

- Current SQL Analysis full-stack story should remain compatible with this model.
- Existing query result execution service (`/api/v1/query/**`) is the execution foundation.

## Open Questions

- Should `Analysis.id` use human-readable slugs, UUIDs, or both?
- Should SQL Analysis reuse the existing `SavedQuery` DTO indefinitely, or should the UI move to
  `Analysis` DTOs immediately?
- What permission model exists for user-owned analysis objects versus shared/team analyses?
