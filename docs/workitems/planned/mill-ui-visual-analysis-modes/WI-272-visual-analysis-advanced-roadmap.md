# WI-272 - Visual Analysis parameters, dashboards, sharing, and materialization roadmap

Status: `planned`
Type: `feature`, `docs`
Area: `ui`, `services`, `platform`
Backlog refs: **U-14**

## Goal

Plan the advanced Visual Analysis capabilities that should follow the MVP board workflow: parameters,
dashboard mode, sharing/collaboration, materialization, and compute/correctness observability.

## Scope

1. Parameters:
   - string, number, boolean, date/time.
   - default value.
   - temporary override for the current viewer/session.
   - suggested values from a column with result caps.
   - multi-value parameters for string/number.
   - cross-filtered suggestion groups as a later phase.
2. Dashboard mode:
   - select boards for presentation.
   - arrange chart/table/KPI widgets.
   - expose only parameters used by selected boards.
   - chart-to-chart filtering as a later phase.
   - full-screen presentation and export as later phases.
3. Sharing and collaboration:
   - analysis owner/editor/viewer roles.
   - URL sharing.
   - permission checks against referenced data resources.
   - optimistic locking or revision model for concurrent edits.
4. Materialization:
   - save path result as a durable dataset/artifact.
   - preserve lineage from source refs and board specs.
   - explicit rebuild/update semantics.
   - validation before materialization for sensitive logic.
5. Compute and diagnostics:
   - per-board preview duration.
   - query/session ids linked to board previews.
   - warnings for expensive board order, especially joins before filters.
   - deterministic ordering warnings for window/rank operations when added.
   - timezone controls for date bucketing/display.
6. Public and design docs:
   - user-facing Visual Analysis guide.
   - backend API contract.
   - spec versioning and migration policy.

## Acceptance

- Advanced capabilities are organized into follow-up stories with clear dependencies.
- MVP implementation is not blocked by dashboard/materialization scope.
- Permission, lineage, and correctness requirements are documented before materialization.
- Product docs explain the distinction between SQL Analysis and Visual Analysis.

## Dependencies

- Visual Analysis MVP board workflow.
- Analysis catalog persistence and sharing/ownership model.
- Materialization depends on a durable dataset/artifact design.

## Suggested Follow-Up Stories

1. Visual Analysis parameters and suggestions.
2. Visual Analysis dashboard mode.
3. Visual Analysis sharing and permissions.
4. Visual Analysis materialization and lineage.
5. Visual Analysis compute diagnostics and optimization hints.
