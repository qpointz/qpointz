# WI-149 — UI facet ordering and validation

**Story:** flow-source-ui-facets  
**Status:** `planned`  
**Type:** `feat`  
**Area:** `ui`

## Goal

Ensure **mill-ui** Data Model entity details **display** flow facet cards sensibly and respect facet
type metadata (**`contentSchema`**, read-only inferred rows).

## In scope

1. Update **`facetTypeDisplayPriority.ts`** (or equivalent) so new flow facet URNs appear in a
   stable order near related structural / layout types.
2. Manual or automated check: load a flow-backed catalog, open schema / table / column entities, confirm
   cards render without console errors.
3. If generic object rendering is insufficient for key payloads, add a thin presenter (prefer
   schema-driven display first).

## Out of scope

- Editing inferred facet payloads in UI (remains repository-captured facets only, per existing
  guards).

## Acceptance criteria

- **`npm run test`** (Vitest) updated if display-order helpers change.
- Short verification note in MR (screenshots optional).

## Story closure reminder

Per **`docs/workitems/RULES.md`**: when the branch is MR-ready, update **`MILESTONE.md`**, **`BACKLOG.md`**,
**`docs/design/`** (data/metadata facet origins), **`docs/public/`** if user-visible behaviour merits it,
then archive this folder to **`docs/workitems/completed/YYYYMMDD-flow-source-ui-facets/`**.
