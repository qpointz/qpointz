# Model view — facet boxes and presentation

This document describes how the **Data Model** explorer (`ui/mill-ui`, route `/model`) renders metadata facets on an entity, how that relates to facet type descriptors, and where custom presentation can be reintroduced later.

## Goals

- **Single standard path** for most facets: read-only and edit UIs derive from the facet type’s `FacetTypeManifest.payload` ([descriptor contract](./facet-type-descriptor-formats.md)), via `renderReadOnlyField` / `renderField` in `EntityDetails.tsx`.
- **MULTIPLE cardinality**: one **outer** facet box per facet type (shared title, Edit, Delete). Under read mode, each stored instance is a **nested card** using the **same** standard field renderer with the **item** schema (the manifest’s object payload).
- **Structural** facet: still uses a dedicated `StructuralFacet` component when the legacy convenience flags/data path applies (see below).

## Current behavior (`EntityDetails.tsx`)

| Case | Read presentation | Edit |
|------|-------------------|------|
| Facet type with `targetCardinality` **SINGLE** (default) | Standard: descriptor-driven fields, or read-only JSON if no descriptor | Whole facet payload; form + expert JSON |
| Facet type with `targetCardinality` **MULTIPLE** | One inner card per item in the normalized payload list; caption uses `name` when present, else `Entry i of n` | Same as SINGLE: entire array (or legacy `{ "relations": [...] }`) is edited as one value |
| URN ends with `:structural` **and** `facets.structural` is populated **and** `modelStructuralFacet` | `StructuralFacet` tailored UI | Standard path when user opens Edit (still the full facet type payload) |
| No `payload` schema on manifest | Read-only pretty-printed JSON | Expert JSON only if no `effectivePayloadSchema` |

Payload normalization for MULTIPLE instances (`multipleFacetItemValues`):

1. `null` / undefined → `[]`
2. Array → use as-is
3. Plain object with `relations` array → use that array (legacy relation envelope)
4. Empty object → `[]`
5. Otherwise → single-element list `[payload]`

## Customization points (for future revision)

1. **Per-URN presentation override**  
   Map facet type URN (or suffix after `urn:mill/metadata/facet-type:`) to a React component that receives `{ entity, facets, facetType, payload, descriptor, mode }`.  
   Today there is **no** such registry: descriptive and relation use the standard path only; structural is a **hard-coded** suffix check.

2. **“Promoted” platform facets**  
   Descriptive / relation / structural could be registered explicitly instead of `endsWith(':structural')` checks, so third-party URNs can opt into structural-style UX without naming hacks.

3. **Per-instance edit**  
   Current MULTIPLE editing is **whole-array**. Independent boxes could later get “Edit this row” that splices one index and calls `setEntityFacet` with the updated array (API remains one payload per facet type).

4. **Flags**  
   `modelStructuralFacet`, `modelQuickBadges`, and `modelPhysicalType` gate the tailored **structural** read branch (`StructuralFacet`), quick constraint badges, and the physical-type badge on attributes. Other facet types use the descriptor-driven path only (no per-type feature flags).

5. **Location of logic**  
   Facet box layout and standard renderers live in `ui/mill-ui/src/components/data-model/EntityDetails.tsx`.  
   The only remaining facet-specific presenter in that folder is `facets/StructuralFacet.tsx` (structural read view). Optional per-type **view/edit** component registration is future work; see [`docs/design/ui/facet-view-customization.md`](../ui/facet-view-customization.md).

## API: `GET /api/v1/metadata/entities/{id}/facets`

The handler returns a **JSON array** of `{ "facetType": "<URN>", "payload": ... }`, sorted by `facetType`, **not** a map keyed by facet type. JSON objects cannot have duplicate keys, so a map cannot represent two facets of the same type; the array shape is forward-compatible when the service persists multiple facet instances per type. Today the domain still holds at most one merged payload per facet type; the client builds `EntityFacets.byType` from the list (last wins if duplicates ever appear).

## Related

- Admin facet type editor: `FacetTypeEditPage.tsx`, descriptor schema → manifest.
- API: `GET/PUT/DELETE /api/v1/metadata/entities/{id}/facets/...` with merged context (see metadata service design).
