# Multi-source facets (captured and inferred)

Mill can show **facet** information — descriptions, structural hints, relations, and similar context — that comes from **more than one place**. Some of it is **captured** in the metadata store; some is **inferred** at read time from the logical schema and other services. This page explains what you see in the product and how to interpret **origin** fields in APIs.

---

## Two kinds of facet rows

| Kind | Meaning | Editing |
|------|---------|---------|
| **Captured** | Stored as a normal metadata assignment for an entity. | You can add, change, or remove it **when your role allows** metadata edits. |
| **Inferred** | Derived when you view an entity — for example from the **logical catalog** (schemas, tables, columns) **or** from **backend-specific configuration** (active query backend). | **Read-only** in the metadata UI. You cannot treat it like a saved assignment. |

**Backend-specific inferred** facets expose **how the active backend is configured** (for example flow source descriptors): see [Backend metadata](backend-metadata.md).

The UI may show **both** together in a single view (sometimes called a **constellation**): captured entries you can edit, plus inferred entries that explain structure or defaults without duplicating them in storage.

---

## How to tell them apart

- **In the product:** inferred facets are presented as **read-only** context alongside captured data. Edit actions apply only to **captured** rows when you have permission.
- **In the API:** each facet instance includes **`origin`** (for example captured vs inferred), **`originId`** (which source contributed the row), and **`editable`** (whether an update is allowed for this principal). **Inferred** rows do not have a persisted assignment id in the same way as captured rows.

---

## `originId` (source attribution)

**`originId`** identifies **which reader** produced a facet contribution — for example the repository that loads stored assignments, or the logical layout reader that describes tables and columns from the catalog. It is useful for **support and debugging** (which layer supplied a value) and for **optional filtering** in advanced clients.

You do not need to memorize values for everyday use; treat **`originId`** as a stable label for the contributing pipeline.

---

## Optional API filters (`scope` and `origin`)

Read APIs that resolve facets for an entity accept:

- **`scope`** — which **scope chain** to use when overlapping assignments exist (comma-separated; order matters for how layers combine).
- **`origin`** — optional **limit** to one or more contributing sources (`originId` values). When omitted, **all** active sources participate.

A legacy **`context`** query parameter may still appear in older notes; it behaves like **`scope`** when **`scope`** is not provided.

---

## See also

- [Backend metadata](backend-metadata.md) — purpose and configuration for backend-provided facets (e.g. flow)
- [Concepts](concepts.md) — entities, facet types, scopes
- [Using metadata in Mill UI](mill-ui.md)
- [Metadata in Mill](system.md)
