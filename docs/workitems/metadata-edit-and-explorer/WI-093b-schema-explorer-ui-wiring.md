# WI-093b — Schema Explorer UI Wiring

Status: `planned`
Type: `✨ feature`
Area: `ui`
Depends on: WI-093a (backend endpoints must be live)

---

## Goal

Update `mill-ui` to consume the hierarchical `/api/v1/schema/**` endpoints delivered in WI-093a.
The sidebar tree switches from a single bulk-load to **lazy-loading**: schemas are fetched first,
tables are loaded when a schema is expanded, columns when a table is expanded.

The existing `schemaService.ts` calls `GET /api/v1/schema/tree` (removed — no such endpoint) and
`GET /api/v1/schema/entities/{id}` (removed — replaced with path-based endpoints). Both must be
replaced.

The separate facets fetch via `GET /api/v1/metadata/entities/{id}/facets` is **removed** from the
schema service: WI-093a detail endpoints already include merged facets in the response.

---

## API Contract (from WI-093a)

```
GET /api/v1/schema/context                                              → SchemaContextDto (selected context)
GET /api/v1/schema?context=<scopes>                                     → SchemaListItemDto[]   (names + facets, no tables)
GET /api/v1/schema/{schema}?context=<scopes>                            → SchemaDto             (includes table summaries)
GET /api/v1/schema/{schema}/tables?context=<scopes>                     → TableSummaryDto[]
GET /api/v1/schema/{schema}/tables/{table}?context=<scopes>             → TableDto              (includes columns)
GET /api/v1/schema/{schema}/tables/{table}/columns?context=<scopes>     → ColumnDto[]
GET /api/v1/schema/{schema}/tables/{table}/columns/{column}?context=<scopes> → ColumnDto
```

No `/tree` endpoint — the sidebar is lazy-loaded level by level.

All facets use URN keys with metadata-style envelopes:
`Record<string, { facetType: string; payload: unknown }>`

`facets` may be absent; missing `facets` means empty collection.
List endpoints include descriptive facet only when present.

`context` follows metadata-service semantics. Backend returns `400` for malformed context inputs;
UI must handle this gracefully with non-crashing empty/error state messaging.

Context selector is deferred. In phase 1, UI reads current context from
`GET /api/v1/schema/context` and uses it for schema calls.

---

## TypeScript Types to Update

**`src/types/schema.ts`** — replace `SchemaEntity` / `SchemaService` with lazy-loading types:

```typescript
export type EntityType = 'SCHEMA' | 'TABLE' | 'COLUMN';

/** Sidebar list item returned by GET /api/v1/schema. */
export interface SchemaListItem {
  entityType: 'SCHEMA';
  id: string;
  metadataEntityId?: string;
  name: string;
  tableCount: number;
  facets?: EntityFacets; // descriptive only
}

/** Full schema detail with table list and optional facets */
export interface SchemaDetail {
  entityType: 'SCHEMA';
  id: string;
  metadataEntityId?: string;
  name: string;
  tables: TableSummary[];
  facets?: EntityFacets;
}

export interface TableSummary {
  entityType: 'TABLE';
  id: string;
  metadataEntityId?: string;
  name: string;
  tableType: 'TABLE' | 'VIEW';
  facets?: EntityFacets; // descriptive only
}

/** Full table detail with columns and optional facets */
export interface TableDetail {
  entityType: 'TABLE';
  id: string;
  metadataEntityId?: string;
  schemaName: string;
  name: string;
  tableType: 'TABLE' | 'VIEW';
  columns: ColumnDetail[];
  facets?: EntityFacets;
}

/** Column detail with optional facets */
export interface ColumnDetail {
  entityType: 'COLUMN';
  id: string;
  metadataEntityId?: string;
  schemaName: string;
  tableName: string;
  name: string;
  fieldIndex: number;
  type: DataTypeDescriptor;
  nullable: boolean;
  facets?: EntityFacets;
}

export interface DataTypeDescriptor {
  type: string;
  nullability?: string;
  precision?: number;
  scale?: number;
}

export interface ScopeOption {
  slug: string;
  urn: string;
  selectable: boolean;
}

export interface SchemaContext {
  selectedContext: string;      // "global" in phase 1
  selectedContextUrn: string;   // urn:mill/metadata/scope:global in phase 1
  availableScopes: ScopeOption[];
  selectorEnabled: boolean;     // false in phase 1
}

/** Internal tree state model used by DataModelLayout + SchemaTree. */
export interface SchemaNode {
  id: string;
  name: string;
  entityType: EntityType;
  metadataEntityId?: string;
  children?: SchemaNode[];
}
```

`EntityFacets` shape remains the same as today (`descriptive`, `structural`, `relations`), but
the facets are now extracted from the response's `facets` map using the URN keys:

```typescript
function extractFacets(raw?: Record<string, { facetType: string; payload: unknown }>): EntityFacets | null {
  if (!raw) return null;
  const result: EntityFacets = {};
  const d = raw['urn:mill/metadata/facet-type:descriptive'];
  const s = raw['urn:mill/metadata/facet-type:structural'];
  const r = raw['urn:mill/metadata/facet-type:relation'];
  if (d) result.descriptive = d.payload as DescriptiveFacet;
  if (s) result.structural  = s.payload as StructuralFacet;
  if (r) result.relations   = r.payload as RelationFacet[];
  return Object.keys(result).length ? result : null;
}
```

---

## `schemaService.ts` Rewrite

Replace the current three-method service with the lazy-loading contract:

```typescript
export interface SchemaService {
  /** GET /api/v1/schema/context — currently selected context (phase 1: global). */
  getContext(): Promise<SchemaContext>;
  /** GET /api/v1/schema — list schemas (no tables/columns — sidebar first load). */
  listSchemas(context: string): Promise<SchemaListItem[]>;
  /** GET /api/v1/schema/{schema} — schema with table summaries (on schema expand). */
  getSchema(schemaName: string, context: string): Promise<SchemaDetail | null>;
  /** GET /api/v1/schema/{schema}/tables/{table} — table with columns (on table expand). */
  getTable(schemaName: string, tableName: string, context: string): Promise<TableDetail | null>;
  /** GET /api/v1/schema/{schema}/tables/{table}/columns/{column} — column detail. */
  getColumn(schemaName: string, tableName: string, columnName: string, context: string): Promise<ColumnDetail | null>;
}
```

Implementation:

```typescript
const realSchemaService: SchemaService = {
  async getContext() {
    const res = await fetch('/api/v1/schema/context', { credentials: 'include' });
    if (!res.ok) {
      return {
        selectedContext: 'global',
        selectedContextUrn: 'urn:mill/metadata/scope:global',
        availableScopes: [{ slug: 'global', urn: 'urn:mill/metadata/scope:global', selectable: true }],
        selectorEnabled: false,
      };
    }
    return res.json() as Promise<SchemaContext>;
  },
  async listSchemas(context) {
    const res = await fetch(`/api/v1/schema?context=${encodeURIComponent(context)}`, { credentials: 'include' });
    if (!res.ok) return [];
    return res.json() as Promise<SchemaListItem[]>;
  },
  async getSchema(schemaName, context) {
    const res = await fetch(
      `/api/v1/schema/${encodeURIComponent(schemaName)}?context=${encodeURIComponent(context)}`,
      { credentials: 'include' }
    );
    if (!res.ok) return null;
    return res.json() as Promise<SchemaDetail>;
  },
  async getTable(schemaName, tableName, context) {
    const res = await fetch(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}?context=${encodeURIComponent(context)}`,
      { credentials: 'include' }
    );
    if (!res.ok) return null;
    return res.json() as Promise<TableDetail>;
  },
  async getColumn(schemaName, tableName, columnName, context) {
    const res = await fetch(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}/columns/${encodeURIComponent(columnName)}?context=${encodeURIComponent(context)}`,
      { credentials: 'include' }
    );
    if (!res.ok) return null;
    return res.json() as Promise<ColumnDetail>;
  },
};
```

**Remove** `realGetEntityFacets` and all `getEntityFacets` usage — facets come from the detail
endpoint now.

---

## `DataModelLayout` Update

The layout currently uses `getTree()` (removed) and `getEntityFacets(id)` (removed). Replace with
a lazy-loading tree:

1. On mount: call `listSchemas(selectedContext)` — renders the schema list (collapsed).
   - Before first load, call `getContext()` and keep `selectedContext`.
   - If context call fails, fallback to `"global"`.
2. On schema expand: call `getSchema(schemaName, selectedContext)` — renders table summaries as children.
3. On table expand: call `getTable(schemaName, tableName, selectedContext)` — renders column list.
4. On any node click: the detail panel shows the data already fetched for that level (no extra
   request needed if the node was already expanded). If the node was never expanded, fetch the
   detail endpoint for that level.
5. Pass the detail response directly to `EntityDetails`; no separate facets fetch.
6. Do not render a context selector yet (`selectorEnabled=false` in phase 1); document selector as deferred.

ID parsing helper (in `DataModelLayout` or a utility file):

```typescript
function parseEntityId(id: string): { schema: string; table?: string; column?: string } {
  const [schema, table, column] = id.split('.');
  return { schema, table, column };
}
```

`SchemaNode[]` is the UI tree state:
- root nodes from `SchemaListItem` as `entityType='SCHEMA'`
- schema children as `entityType='TABLE'`
- table children as `entityType='COLUMN'`

---

## `EntityDetails` Update

Currently receives `entity: SchemaEntity` and `facets: EntityFacets`. Update to accept a union
of the new detail types and derive facets from `detail.facets`:

```typescript
type EntityDetailData = SchemaDetail | TableDetail | ColumnDetail;
// Props: { detail: EntityDetailData | null }
```

Use discriminated union narrowing on `detail.entityType`:

```typescript
if (!detail) return <EmptyState />;
switch (detail.entityType) {
  case 'SCHEMA':
    // schema rendering
    break;
  case 'TABLE':
    // table rendering
    break;
  case 'COLUMN':
    // safe access: detail.type, detail.nullable
    break;
}
```

The structural facet for columns should also include `type` and `nullable` from the
physical data even when no `structural` metadata facet is set:
- `type` ← `detail.type` in the `'COLUMN'` branch
- `nullable` ← `detail.nullable` in the `'COLUMN'` branch

This ensures the structural quick-badges (Physical Type, Nullable) are populated from the
physical schema even without a metadata repository.

---

## UI Hardening

### Empty and error states

- Tree returns `[]` → sidebar shows "No schemas available" message (add to `SchemaTree`).
- `getSchema` / `getTable` / `getColumn` returns `null` (non-OK response) → detail panel shows
  empty state, no JS exception.
- `400` malformed-context responses are surfaced as a user-safe error state (no JS exception).

### Loading states

`DataModelLayout` should show a loading skeleton while `listSchemas()` is in-flight and while the
detail fetch is pending.

---

## Files to Modify

| File | Change |
|------|--------|
| `src/types/schema.ts` | Replace `SchemaEntity` with `SchemaNode`; add `SchemaListItem`, `SchemaDetail`, `TableDetail`, `ColumnDetail`, `SchemaContext` |
| `src/services/schemaService.ts` | Replace `getEntityById` + `getEntityFacets` with `getContext`, `getSchema`, `getTable`, `getColumn` |
| `src/components/data-model/DataModelLayout.tsx` | Use detail endpoints; remove facets fetch; add loading/error states |
| `src/components/data-model/EntityDetails.tsx` | Accept discriminated detail union and use `entityType` narrowing |
| `src/components/data-model/SchemaTree.tsx` | Add empty-state rendering |

---

## Tests

**`src/services/schemaService.test.ts`** (Vitest, fetch mocked)

- `getContext()` returns backend value; on non-OK response returns global fallback.
- `listSchemas(context)` → deserialises root `SchemaListItem[]` correctly.
- `getSchema("sales")` → returns `SchemaDetail` with `tables` and `facets`.
- `getTable("sales", "customers")` → returns `TableDetail` with `columns`.
- Any `!res.ok` response on detail endpoints → returns `null` (no throw).
- `400` malformed-context responses do not crash UI and render error/empty state.

**`src/components/data-model/SchemaTree.test.tsx`**

- Renders tree from mock `SchemaNode[]`.
- Renders empty state when list is `[]`.

**`src/components/data-model/DataModelLayout.test.tsx`**

- Calls `getContext()` first, then `listSchemas(selectedContext)`.
- Shows loading while context + schema calls are pending.
- Shows tree after resolve.
- Clicking a table node fetches `getTable()` and updates detail panel.

Run: `cd ui/mill-ui && npm run test`

---

## Acceptance Criteria

- Schema tree sidebar populates from `GET /api/v1/schema`.
- Clicking a schema/table/column node calls the matching hierarchical detail endpoint.
- Column `type` descriptor and nullable are shown even when no structural facet is configured.
- Metadata facets (descriptive, structural, relations) are shown when present.
- Empty tree, null detail, and network errors produce graceful UI states — no JS exceptions.
- Malformed-context (`400`) responses produce graceful UI states — no JS exceptions.
- UI consumes `GET /api/v1/schema/context`; phase 1 uses hardcoded `global` with selector disabled.
- Context selector UI is explicitly deferred and documented.
- `npm run test` and `npm run build` pass with no errors.
