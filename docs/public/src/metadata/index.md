# Metadata

Mill’s **metadata** layer stores business context about your data: descriptions, relationships, value mappings, and more. It sits next to the **physical schema** (what the database actually contains) and is what the **Model** and **Knowledge** areas of [Mill UI](../mill-ui.md) read and, when enabled, let you edit.

This section explains the **metadata model** in plain language and how to **understand** and **manage** it as a user or operator.

---

## Who this is for

| Audience | Start here |
|----------|------------|
| **Analysts & data owners** | [Concepts](concepts.md), then [Using metadata in Mill UI](mill-ui.md) |
| **Platform operators** | [Metadata in Mill](system.md) then [Operator guide](operators.md) — repository type, seeds, YAML |

---

## Why metadata matters

- **Better answers in Chat** — Descriptions and value mappings help the system map business words to real columns and values.
- **Shared understanding** — Teams see the same definitions, owners, and tags in the Data Model.
- **Governance** — Classifications, concepts, and relations document how data fits together.

---

## How it fits together

```text
Physical schema          Metadata
(Calcite / JDBC)         (Mill metadata store)
     │                          │
     └──── Model + Knowledge UI ───────┘
              │
         Chat & tools use both
```

The explorer shows **tables and columns** from your backend. When Mill has stored metadata for those objects, you also see **facet panels** (descriptions, relations, etc.) backed by a stable **metadata identity** the server assigns or imports.

**Concepts** (user level): each bound object is an **entity** — a **standalone** metadata record, **not** a node in a Mill-managed term taxonomy. **Facet types** define *what* you can attach; **facet assignments** are the *values* on that entity, optionally layered by **scope** so global and team-specific contributions **merge** into what you see. Details: [Concepts](concepts.md).

---

## Pages in this section

- **[Metadata in Mill](system.md)** — What metadata is in the product, APIs at a glance, link to design reference.
- **[Concepts](concepts.md)** — Entities (independent of taxonomy), facet types vs assignments, scopes and overlay merge, identities (without implementation detail).
- **[Multi-source facets](multi-source-facets.md)** — Captured vs inferred facets, read-only inferred rows, **`originId`**, and optional **`scope`** / **`origin`** filters on reads.
- **[Facet field stereotypes](facet-stereotypes.md)** — Optional UI hints (`email`, `hyperlink`, `tags`) on facet payload fields.
- **[Using metadata in Mill UI](mill-ui.md)** — Data Model, Context, editing, admin facet types; screenshot placeholders for future illustrations.
- **[Operator guide](operators.md)** — **`mill.metadata.repository.*`**, **`mill.metadata.seed.*`**, YAML seeds, backups.

For deeper technical contracts (YAML import format, URN grammar), see the design docs in the main repository under `docs/design/metadata/`; this public section stays **user- and operator-oriented**.
