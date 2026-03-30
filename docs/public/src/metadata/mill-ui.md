# Using metadata in Mill UI

Mill UI (**`ui/mill-ui`**) is where **readers** and **editors** interact with metadata alongside the physical schema. Routes are normally under the **`/app`** base path (e.g. **`/app/model`**). This page assumes the [Concepts](concepts.md) overview (or [Metadata in Mill](system.md)); for shell and config commands see [Operator guide](operators.md). For chat behaviour, feature flags, and Analysis/Connect, see the full [Mill UI](../mill-ui.md) page.

---

## Model view

Open **Model** from the sidebar (path **`/app/model`** when using the default Vite base). You get:

1. **Tree** — Schemas → tables → columns.
2. **Details** — When you select a node, the main panel shows headers, physical properties, and **facet** blocks.

<!-- SCREENSHOT_PLACEHOLDER: Mill UI sidebar — Model selected, tree expanded to schema.table -->

### Entity header

The header usually shows:

- Human-friendly **name** and **type** (schema, table, column).
- **Location** — Explorer path (`schema.table.column`).
- When metadata is bound, a **metadata identity** line may appear so you know facet APIs have a backing record.

<!-- SCREENSHOT_PLACEHOLDER: Entity details header showing location + metadata identity line -->

### Facet panels

Each **facet type** appears as its own section. What you see is the **effective** data after any **scope overlays** are merged (see [Concepts](concepts.md#scopes-overlays-and-how-values-combine)). **Read mode** shows structured fields when the server provides a **descriptor**; otherwise you may see JSON. **Edit mode** (when your role and deployment allow it) lets you change **assignments** for the scope you are allowed to write (for example global vs team). You can switch between a **form** driven by the payload schema and **Expert JSON/YAML** mode (same editor pattern as facet-type admin: JSON ↔ YAML toggle, format, Apply — **Save** sends the parsed object to the API).

**Multiple-instance facet types** show one **card per assignment** (for example several relations). Deleting or editing a specific row may require selecting the right instance when the server exposes instance ids.

<!-- SCREENSHOT_PLACEHOLDER: Facet panel — MULTIPLE cardinality with two inner cards -->

### When editing is disabled

Facet actions stay disabled if:

- Metadata backend is not configured (`NoOp` / empty repository).
- The schema explorer response does not include a **metadata entity id** for that node (no binding yet).
- You are not signed in or lack permission.

Ask an operator to confirm [persistence and imports](operators.md).

### Search

Use **header global search** (**Ctrl+K** / **Cmd+K** when enabled) to find schemas, tables, columns, concepts, and more; results navigate into **Model** or **Knowledge** as appropriate.

<!-- SCREENSHOT_PLACEHOLDER: Data Model search field with results list -->

### Sharing links

URLs follow **`/app/model/...`** (with the usual `/app` base path) so you can bookmark or share a specific table or column.

---

## Knowledge view

The sidebar label is **Knowledge** (`/app/knowledge`). It focuses on **business concepts** — definitions that may span tables and complement Model facets. See [Knowledge view](../mill-ui.md#knowledge-view) in the main Mill UI page.

<!-- SCREENSHOT_PLACEHOLDER: Knowledge view — category list and concept detail -->

---

## Admin — Metadata (facet types)

If **`/app/admin/model/facet-types`** is available (Admin sidebar **Metadata** → **Facet types** when facet-type flags are on), administrators can:

- Inspect **facet type manifests** (titles, **category** tab grouping, which entity kinds they apply to, single vs multiple cardinality).
- Adjust **payload schemas** that drive forms in the **Model** view, including optional per-field **stereotypes** (presentation hints such as `tags`, `hyperlink`, `email`; see [Facet field stereotypes](facet-stereotypes.md) and design doc `facet-type-descriptor-formats.md`).
- Use **Expert JSON mode** with a JSON/YAML toggle for the full manifest.

Changes affect **how** editors capture data, not the physical database. Coordinate with operators before altering production descriptors.

<!-- SCREENSHOT_PLACEHOLDER: Admin facet type list or edit screen -->

---

## Keyboard and accessibility

Standard Mill UI shortcuts (global search **Ctrl/Cmd+K**, theme toggle) apply when those header features are enabled; see [Mill UI](../mill-ui.md#interface-features). Facet forms behave like other form controls in the app.

---

## See also

- [Concepts](concepts.md) — entities, facets, scopes.
- [Mill UI](../mill-ui.md) — full UI reference (Chat, configuration summary for operators).
