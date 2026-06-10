# WI-149 — UI facet ordering and validation

**Story:** flow-source-ui-facets  
**Status:** `planned`  
**Type:** `feat`  
**Area:** `ui`

## Goal

Ensure **mill-ui** Data Model entity details **display** flow facet cards sensibly and respect facet
type metadata (**`contentSchema`**, read-only inferred rows).

## In scope

1. **File:** **`ui/mill-ui/src/config/facetTypeDisplayPriority.ts`** — extend **`DEFAULT_FACET_TYPE_DISPLAY_PRIORITY`** (a **`readonly string[]`**: **earlier entries render first** among types that have data). Insert the three flow URNs **after** the built-in structural **`schema` / `table` / `column`** entries and **before** or adjacent to **`descriptive` / `links`** — exact order is a product choice; document the final order in the MR.
   - URN strings: **`urn:mill/metadata/facet-type:flow-schema`**, **`:flow-table`**, **`:flow-column`**.
   - **Mechanism:** **`EntityDetails.tsx`** uses **`facetTypeArrivalOrderFromRegistry`** with this list plus server registry order for any keys not listed (see [`facetTypeDisplayPriority.ts`](../../../ui/mill-ui/src/config/facetTypeDisplayPriority.ts)).
2. **Required automated test:** extend **`ui/mill-ui/src/utils/__tests__/sortFacetTypesByDisplayPriority.test.ts`** (or add **`facetTypeDisplayPriority.test.ts`** next to config) so that **when** the arrival list contains the three **`flow-*`** URNs and layout URNs, **`sortFacetTypesByDisplayPriority`** / arrival ordering places **`flow-schema`**, **`flow-table`**, **`flow-column`** in the **expected** relative order (assert on sorted output).
3. Manual check: load a flow-backed catalog, open schema / table / column entities, confirm cards render without console errors.
4. If generic object rendering is insufficient for key payloads, add a thin presenter (prefer schema-driven display first).

## Out of scope

- Editing inferred facet payloads in UI (remains repository-captured facets only, per existing
  guards).

## Acceptance criteria

- **`DEFAULT_FACET_TYPE_DISPLAY_PRIORITY`** updated in **`ui/mill-ui/src/config/facetTypeDisplayPriority.ts`** as above.
- **Vitest:** new or extended test file proving **`flow-*`** order relative to layout types ( **`npm run test`** green).
- Short MR note (screenshots optional).

## Story closure reminder

Per **`docs/workitems/RULES.md`**: when the branch is MR-ready, update **`MILESTONE.md`**, **`BACKLOG.md`**,
**`docs/design/`** (data/metadata facet origins), **`docs/public/`** if user-visible behaviour merits it,
then archive this folder to **`docs/workitems/completed/YYYYMMDD-flow-source-ui-facets/`**.
