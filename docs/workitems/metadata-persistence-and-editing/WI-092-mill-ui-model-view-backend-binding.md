# WI-092 — `mill-ui` Model View: Real Backend Binding and Inline Chat Disable

## Objective

Wire the `mill-ui` model view to real backend endpoints (replacing mock services), disable the
inline chat feature flag, and confirm that relation facets are rendered from the `relation` facet
payload already present on the loaded entity — no dead API call path remains.

---

## Scope

Three independent changes, all in `ui/mill-ui/`:

| # | Change | Backend dependency |
|---|--------|-------------------|
| 1 | Disable `inlineChatEnabled` in `defaults.ts` | None |
| 2 | Real schema HTTP client (`getTree`, `getEntityById`) | Future schema service WI (TBD) |
| 3 | Real metadata facets HTTP client (`getEntityFacets`) | WI-086, WI-087 |

Changes 2 and 3 are implemented immediately (frontend is ready when the backend ships); the mock
is replaced in one commit so no parallel mock/real duplication remains.

---

## Dependencies

- **WI-086** — new `/api/v1/metadata/**` endpoints (entity + facets REST layer)
- **WI-087** — JPA persistence backing those endpoints
- **Schema service WI (TBD)** — new `data/mill-data-schema-service` module delivering
  `GET /api/v1/schema/tree` and `GET /api/v1/schema/entities/{id}`

This WI may be committed before backends are ready; the frontend will fail gracefully (empty
tree, no entity selected) until the backend WIs land on the same branch.

---

## Change 1 — Disable Inline Chat

**File:** `ui/mill-ui/src/features/defaults.ts`

Set `inlineChatEnabled: false` in `defaultFeatureFlags`.
The `InlineChatButton` component already has a kill-switch guard (`if (!flags.inlineChatEnabled) return null`)
so setting this flag propagates everywhere with zero other code changes.

All other `inlineChat*` flags may stay `true` — they have no effect while `inlineChatEnabled` is
off.

---

## Change 2 — Real Schema Service

**File:** `ui/mill-ui/src/services/schemaService.ts`

Replace the mock implementation with `realSchemaService`. The `SchemaService` interface
(`src/types/schema.ts`) does not change.

Expected backend endpoints:

```
GET /api/v1/schema/tree
  → 200: SchemaEntity[]   (tree with children nested, depth=2)
  → 200: []               (empty array if no schemas loaded)

GET /api/v1/schema/entities/{id}
  → 200: SchemaEntity     (id, type, name, children?)
  → 404: entity not found → service returns null
```

Response type `SchemaEntity` is already defined in `src/types/schema.ts`; the server must match
this shape exactly (same field names, same `type` values: `SCHEMA | TABLE | ATTRIBUTE`).

Implementation pattern — follow `authService.ts`:
- Plain `fetch()` with relative URLs and `credentials: 'include'`
- No `API_BASE_URL` constant (Vite proxy handles dev; same origin in prod)
- Throw on unexpected non-404 errors; return `null` on 404 for `getEntityById`

```ts
const realSchemaService: SchemaService = {
  async getTree() {
    const res = await fetch('/api/v1/schema/tree', { credentials: 'include' });
    if (!res.ok) return [];
    return res.json() as Promise<SchemaEntity[]>;
  },
  async getEntityById(id: string) {
    const res = await fetch(`/api/v1/schema/entities/${encodeURIComponent(id)}`, {
      credentials: 'include',
    });
    if (res.status === 404) return null;
    if (!res.ok) return null;
    return res.json() as Promise<SchemaEntity>;
  },
  async getEntityFacets(id: string) {
    // Implemented in Change 3 (see below) — kept here for co-location
    return realGetEntityFacets(id);
  },
};

export const schemaService: SchemaService = realSchemaService;
```

Remove the entire `mockSchemaService` and the `import` of `mockSchema` once the swap is made.

---

## Change 3 — Real Metadata Facets Client

**File:** `ui/mill-ui/src/services/schemaService.ts` (same file — facets call lives alongside tree)

Expected backend endpoint after WI-086/WI-087 (added as the all-facets convenience endpoint in WI-086):

```
GET /api/v1/metadata/entities/{id}/facets?context=<csv-urns>
  → 200: { [facetTypeUrn: string]: FacetEntry }
  → 404: no entity record → treat as empty facets {}

FacetEntry:
{
  "facetType": "urn:mill/metadata/facet-type:descriptive" | "urn:mill/metadata/facet-type:structural" | ...,
  "payload": object   // shape depends on facetType
}
```

Keys in the response map use URN notation (as stored after WI-086 normalisation).
`context` defaults to `urn:mill/metadata/scope:global` when omitted — pass the default for
read-only model view so the call is explicit.

**Response mapping to `EntityFacets`:**

| Facet type URN key | Maps to `EntityFacets` field | Payload shape |
|--------------------|------------------------------|---------------|
| `"urn:mill/metadata/facet-type:descriptive"` | `descriptive: DescriptiveFacet` | Single object — direct cast |
| `"urn:mill/metadata/facet-type:structural"` | `structural: StructuralFacet` | Single object — direct cast |
| `"urn:mill/metadata/facet-type:relation"` | `relations: RelationFacet[]` | Array — payload is the array |

The `relation` facet payload is an **array** (multiple relations per entity), while `descriptive`
and `structural` payloads are single objects. The mapping function must handle this distinction.

```ts
// Response body map keys are full URN strings (returned by the server).
// The ?context= query param uses prefixed slugs — no encodeURIComponent needed.
const FACET_DESCRIPTIVE  = 'urn:mill/metadata/facet-type:descriptive';
const FACET_STRUCTURAL   = 'urn:mill/metadata/facet-type:structural';
const FACET_RELATION     = 'urn:mill/metadata/facet-type:relation';
const CONTEXT_GLOBAL     = 'global';  // prefixed slug — server expands to full URN

async function realGetEntityFacets(id: string): Promise<EntityFacets> {
  const url = `/api/v1/metadata/entities/${encodeURIComponent(id)}/facets?context=${CONTEXT_GLOBAL}`;
  const res = await fetch(url, { credentials: 'include' });
  if (!res.ok) return {};
  const map = await res.json() as Record<string, { facetType: string; payload: unknown }>;

  const result: EntityFacets = {};
  if (map[FACET_DESCRIPTIVE]) result.descriptive = map[FACET_DESCRIPTIVE].payload as DescriptiveFacet;
  if (map[FACET_STRUCTURAL])  result.structural  = map[FACET_STRUCTURAL].payload  as StructuralFacet;
  if (map[FACET_RELATION])    result.relations   = map[FACET_RELATION].payload    as RelationFacet[];
  return result;
}
```

### Relation facets — no dead code path

`EntityDetails.tsx` already renders `facets.relations` via `RelationFacet.tsx` when the
`modelRelationsFacet` flag is on. No additional changes are needed to the rendering layer.
`knowledgeRelatedEntities` (used in the Context/Knowledge view) is a separate concern and out of
scope for this WI.

---

## Files to Change

| File | Change |
|------|--------|
| `ui/mill-ui/src/features/defaults.ts` | `inlineChatEnabled: false` |
| `ui/mill-ui/src/services/schemaService.ts` | Replace mock with `realSchemaService`; implement `realGetEntityFacets`; remove mock imports |

No new files are needed. No type changes to `src/types/schema.ts`.

---

## Tests

- `ui/mill-ui/src/components/__tests__/DataModelLayout.test.tsx` — update if it imports
  `mockSchema` data directly; swap to mocked `fetch` via `vi.fn()` for tree and entity calls.
- `ui/mill-ui/src/components/__tests__/EntityDetails.test.tsx` — verify `inlineChatEnabled: false`
  in test feature flags causes `InlineChatButton` to render nothing (already covered by the
  kill-switch guard; just confirm the prop is passed correctly).
- `ui/mill-ui/src/features/__tests__/FeatureFlagContext.test.tsx` — verify `defaultFeatureFlags`
  has `inlineChatEnabled: false`.

Run: `cd ui/mill-ui && npm run test`

---

## Verification Checklist

- [ ] Opening `/model` loads a real schema tree (or graceful empty state) — no mock data visible
- [ ] Selecting a table shows real facets from the metadata backend
- [ ] Relations tab shows relations from the `relation` facet payload only
- [ ] `InlineChatButton` is absent from the entity header (inline chat disabled)
- [ ] No TypeScript errors: `npm run build` exits 0
- [ ] All Vitest tests pass: `npm run test` exits 0

---

## Documentation Update — Feature Flags Catalog

As part of completing this WI, review and update `ui/mill-ui/docs/FEATURE-FLAGS.md` to reflect
the current default state of every flag in `src/features/defaults.ts`.

**Why:** `FEATURE-FLAGS.md` was authored with all flags defaulting to `true`. Several flags have
since been changed (login providers default to `false`; `inlineChatEnabled` is set to `false` by
this WI). The catalog must stay in sync with the actual `defaultFeatureFlags` object so it
remains a reliable reference.

**How to update:**

1. Read `src/features/defaults.ts` — take the `defaultFeatureFlags` object as the single source
   of truth.
2. In `FEATURE-FLAGS.md`, update the **Default** column for every flag that differs from `true`.
3. Update the total flag count and the *Last updated* date at the bottom of the file.
4. If any flags have been added or removed since the last update, add or delete the corresponding
   rows in the relevant category table.

**Flags known to be non-`true` after this WI:**

| Flag | Default |
|------|---------|
| `loginGithub` | `false` |
| `loginGoogle` | `false` |
| `loginMicrosoft` | `false` |
| `loginAws` | `false` |
| `loginAzure` | `false` |
| `inlineChatEnabled` | `false` |

Verify this list is complete by diffing `defaultFeatureFlags` against the catalog before closing
the WI.
