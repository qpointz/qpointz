# Metadata concepts

For a product-level overview first, see [Metadata in Mill](system.md).

This page describes the **ideas** behind Mill’s metadata model. You do not need to memorize internal identifiers to use the Data Model — but understanding these terms helps when reading facet panels or talking to operators.

---

## Entity

An **entity** is **one metadata record** in Mill: for example metadata bound to a schema, table, or column, or a standalone **concept** (a business definition that may point at many physical objects).

- In the **Data Model** tree, each node you select is a **catalog object** (schema → table → column). When metadata exists for that object, the UI shows a **metadata identity** (sometimes labeled in the header) that the server uses for APIs.
- **Concept** entities are broader business ideas that may reference several tables or columns; many teams curate these under **Knowledge** as well as in metadata.

### Entities are independent — there is no metadata “taxonomy”

If you are used to glossaries or catalogs built as **term hierarchies**, note:

- Mill’s metadata layer **does not** organise entities into a **nested classification tree** of its own. There is **no** built-in “entity type → subtype → instance” taxonomy inside metadata storage.
- Each entity is a **standalone record** with a stable identity. How business ideas relate to one another is expressed with **facet types** (relations, mappings, tags, and so on) — not by forcing every entity into a single parent/child taxonomy.
- The **tree** under **Model** is the **physical schema** (how the database is laid out), not a metadata taxonomy. Use it to **open** the record for a table or column; do not read that shape as “metadata lives in a hierarchy.”

---

## Facet types and facet assignments

Mill separates **what kind** of information you can attach from **the actual attachment** on a specific entity.

### Facet type (the “kind” of metadata)

A **facet type** is a **definition** on the server (installed via **`mill.metadata.seed.resources`** and/or admin/API changes). It describes:

- A **name** and human title (for example *Descriptive*, *Relations*).
- **Which entities** it may apply to (tables only, columns only, concepts, and so on).
- The **shape** of the payload (fields, JSON schema, or similar).
- Whether editors may attach **one** assignment per scope or **many** (for example several relation rows).

Facet types are **shared** across the deployment: many entities use the same type; the type does not “belong” to one table.

Administrators can inspect or adjust **facet type descriptors** where your deployment exposes **Admin → Model → Facet types** (if that UI is enabled). Descriptors may include optional **stereotypes** on payload fields — UI hints such as **tags**, **hyperlink**, and **email** (not enforced by the metadata API). See [Facet field stereotypes](facet-stereotypes.md) for the tags Mill recognises in the Data Model.

### Facet assignment (the value on an entity)

A **facet assignment** is **one concrete attachment**: a given **entity** + **facet type** + **scope** + **payload** (the actual title, tags, relation row, etc.).

- A **facet panel** in the UI shows the **effective** data for that type after any **scope overlays** are merged (see below). When you **edit**, you usually create or change assignments for the scope you are allowed to write (for example global vs team).
- If the facet type allows **multiple** assignments per scope, you see **several cards or rows** (for example multiple relations). Types that allow **only one** assignment per scope show a single block.

People often say “a **facet** on this table”; that usually means a **facet assignment** — an instance of a **facet type** on that entity. **Type** = template shared by the platform; **assignment** = the data stored for this entity (and scope).

<!-- SCREENSHOT_PLACEHOLDER: Data Model — entity header with facet sections stacked (read-only or edit mode) -->

---

## Scopes, overlays, and how values combine

**Scope** answers: **for whom or in which context** does this assignment apply? Typical scopes include:

- **Global** — the default everyone shares unless something narrower applies.
- **Team, role, tenant, or user** — when your deployment uses **scoped** metadata, these hold **overlays**: extra or alternative definitions for that audience.

### Overlay logic (merging)

When you open an entity, the product may combine assignments from **several scopes** (for example a global description plus team-specific notes). Treat each scope as a **layer**:

- Each layer can hold **facet assignments** for the same **facet type** on the same **entity**.
- Layers are **merged** in a **fixed order** configured for your deployment (often “more specific scopes win over broader ones”). The UI shows the **effective** result after merge.
- Editors may **clear or replace** contributions in a layer they are allowed to write; behaviour for deletes and history depends on scope class and release. If your organisation relies on overlays, confirm **merge order** and **who can write which scope** with your operator — see [Operator guide](operators.md).

Merge order is **not** something analysts set in the UI; it is platform behaviour. What matters for everyday use: **one entity + one facet type** can have **several assignments** under **different scopes**, and what you see is the **combined** view.

---

## Metadata identity vs catalog path

- **Catalog path** — What you see in the tree: `schema`, `schema.table`, or `schema.table.column`. This comes from the **schema explorer** and matches how you think about the database.
- **Metadata identity** — A stable id the **metadata service** uses so that APIs and storage always refer to the same record. The Mill UI surfaces this when present (for example in the entity header) and uses it for facet load/save.

You usually **navigate by path**; the UI bridges to the metadata id when facet editing is available.

---

## Import, export, and YAML

Operators often maintain **canonical YAML** files for bulk load or disaster recovery. The on-disk layout is documented for tooling authors in the repository design doc **metadata-canonical-yaml-spec**; operators should follow [Operator guide](operators.md) for **`mill.metadata.repository.*`**, **`mill.metadata.seed.resources`**, and API import.

---

## Related UI areas

| Area | Role |
|------|------|
| [Model](../mill-ui.md#model-view) | Browse schema; view/edit **facet assignments** per table/column when bound to metadata. |
| [Knowledge](../mill-ui.md#knowledge-view) | Business **concepts** (sidebar label **Knowledge** in Mill UI). |
| [Chat](../mill-ui.md#chat-view) | General and inline chat; behaviour depends on deployed `chatService` (see Mill UI doc). |
