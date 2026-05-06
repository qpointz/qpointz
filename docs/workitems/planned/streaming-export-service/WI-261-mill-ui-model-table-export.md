# WI-261 — mill-ui: Model view table export (split button)

Status: `planned`  
Type: `feature`  
Area: `ui`  
Backlog refs: **P-36**

## Goal

In **[`ui/mill-ui`](../../../../ui/mill-ui)** **Data Model** detail panel **[`EntityDetails.tsx`](../../../../ui/mill-ui/src/components/data-model/EntityDetails.tsx)**, when the selected entity is a **TABLE**, show an **Export** control **immediately before** the existing **Add Facet** split button (~line 1420). Export calls the streaming export service (**[`WI-253`](./WI-253-mill-export-service-http.md)**): **`GET /services/export/schemas/{schema}/tables/{table}?format=…`** with **`schemaName`** / **`tableName`** from **[`TableDetail`](../../../../ui/mill-ui/src/types/schema.ts)** (`encodeURIComponent` on path segments).

## Feature flag

1. Add **`modelTableExportEnabled`** to **[`FeatureFlags`](../../../../ui/mill-ui/src/features/defaults.ts)** / **`defaultFeatureFlags`** under the **Model View Details** comment block (or adjacent), documented in line comments.
2. Default **`true`** for now (per product request); remote merge via **[`featureService`](../../../../ui/mill-ui/src/services/featureService.ts)** remains supported so ops can disable without redeploying client defaults.
3. When **`false`**, hide the entire Export split button (no placeholder).

## UI behaviour

1. **Split button** (same Mantine pattern as **Add Facet**: **`Button`** + **`Menu.Target`** **`ActionIcon`** in a **`Group gap={0}`**):  
   - **Primary label:** e.g. **Export** (or **Export CSV** if default is `csv`).  
   - **Chevron** opens **`Menu.Dropdown`** listing **available formats** from **`GET /services/export/formats`** (effective HTTP list per **`mill.data.services.export.formats`**).  
   - **Primary click** exports using a **default** format: prefer first item in the fetched list, or **`csv`** if present, else first alphabetically — document the rule in code comment.
2. **Table-only:** render only when **`entity.entityType === 'TABLE'`** and **`flags.modelTableExportEnabled`**.
3. **Loading / errors:** while formats load, disable or show loading on control; on failure (`404` / network), **`notifications.show`** and keep button disabled or offer retry (minimal: toast + disabled).
4. **Download:** **`fetch`** with **`credentials: 'include'`** (same as **WI-255**), read **`Content-Disposition`** for filename fallback, **`blob`** + object URL trigger download; revoke URL after.

## Scope

1. Prefer a small dedicated helper or **`exportService.ts`** (under **`src/services/`**, exported from **`api.ts`** if that is the module convention) for **formats list** + **table export URL** building — avoid bloating **`EntityDetails.tsx`**.
2. **Vitest** for URL encoding / default-format selection if non-trivial.
3. No static server-side format list in UI beyond what the API returns.

## Acceptance

- Manual smoke: select a table in Model view → **Export** downloads; menu shows server formats; works only when **`mill.data.services.export.enable`** is true on backend.
- With **`modelTableExportEnabled: false`** (feature payload or local override), control is hidden.
- **`npm run test`** green for **`ui/mill-ui`**.

## Depends on

**WI-253** (minimum). **WI-254** optional for stable **`/formats`** contract.
