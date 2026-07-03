# WI-381 — Facet Type UI JSON Schema View

Status: `planned`  
Type: `feature`  
Area: `ui`, `metadata`

## Problem Statement

Facet type authors and operators can edit the Mill manifest in form or expert JSON/YAML mode, but
there is no safe way to inspect the external JSON Schema contract generated from that manifest.
Using expert mode for this purpose is risky because it is editable and tied to save behavior.

## Goal

Add a read-only JSON Schema view to Admin → Model → Facet Types.

## Scope

1. Add a TypeScript helper that mirrors backend JSON Schema generation for live draft preview.
2. Add a `JSON Schema` mode to `FacetTypeEditPage`.
3. Render the generated schema using the existing read-only code editor surface.
4. Add a copy action for the generated JSON.
5. Keep form editing, expert editing, save behavior, and read-only feature flag behavior intact.

## Acceptance Criteria

- Create mode can show JSON Schema for the unsaved draft manifest.
- Edit mode can show JSON Schema for the loaded manifest and subsequent unsaved changes.
- JSON Schema mode is read-only and does not allow editing the generated artifact.
- Save is disabled while JSON Schema mode is active.
- Existing expert mode still allows editing the manifest and switching back to form mode when the
  schema is form-compatible.

## Test Plan

- Unit tests for the TypeScript conversion helper.
- Component-level coverage for the JSON Schema mode where practical.
- `npm run test` and `npm run build` for `ui/mill-ui`.
