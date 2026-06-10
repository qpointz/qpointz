# Metadata in Mill

Mill **metadata** is structured business context attached to **entities**: schemas, tables, columns, or standalone **concepts**. It is separate from the **physical schema** (what your JDBC or Calcite backend exposes) but is **bound** to those objects when you use the Data Model so descriptions, relations, and other **facets** appear in the right place.

---

## What you get in the product

| Capability | Where it shows up |
|------------|-------------------|
| Descriptions, tags, owners | **Model** view — facet panels on each table or column |
| Relations between tables | Often a **Relations** (or similar) facet with multiple rows |
| Business **concepts** | **Knowledge** view and chat context |
| Governed definitions | Depends on facet types your operator deployed |

The UI loads facets through **`/api/v1/metadata/**`**. The tree and physical names come from **`/api/v1/schema/**`**. When both are configured, the explorer adds a **`metadataEntityId`** (a full Mill URN, usually **`urn:mill/model/schema:…`**, **`…/table:…`**, or **`…/attribute:…`** for catalog objects) to nodes that have a stored record so editing and chat tools can target the right row.

---

## Entities, facet types, assignments, and scopes (short)

- **Entity** — One metadata **record**, identified by a stable id inside the metadata service (the UI may show a shortened or canonical form). Entities are **standalone**; metadata does **not** nest them in its own taxonomy — the Model **tree** is the **physical schema**, not a classification hierarchy.
- **Facet type** — A **defined kind** of payload (descriptive, relations, mappings, …), shared across the deployment.
- **Facet assignment** — The **actual attachment** of that type to an entity: entity + facet type + **scope** + payload. Multiple assignments can exist when the type allows it or when **different scopes** each contribute (overlays).
- **Scope** — *Who* the assignment applies to (e.g. global vs team). When several scopes apply, the product **merges** them in a fixed order so you see one **effective** view (see [Concepts](concepts.md)).

For a full user-level explanation (independence from taxonomy, overlay logic), read [Concepts](concepts.md). For Spring properties and seeds, see [Operator guide](operators.md).

---

## YAML and bulk load

Operators often maintain **canonical multi-document YAML** for bulk import, disaster recovery, or GitOps-style review. The file layout (facet types, entities, normalisation rules) is specified for tooling authors in the repository design doc **metadata-canonical-yaml-spec.md**. At runtime:

- **Startup content** (global scope, standard facet types, and optional dataset YAML) is loaded **only** through **`mill.metadata.seed.resources`** — typically with **`classpath:metadata/platform-bootstrap.yaml`** listed first. Database migrations create **metadata tables only**; they do not insert scopes or facet types.
- A **file-backed repository** (`mill.metadata.repository.type=file`) uses **`mill.metadata.repository.file.path`** for the YAML store when configured; that is separate from the seed list (you need a non-blank path **or** non-empty seeds per validation).

Details and examples: [Operator guide](operators.md).

---

## Deeper technical reference

Implementers and integrators who need exact merge rules, `FacetInstance` rows, `merge_action`, and DDL alignment should read the internal design reference **`docs/design/metadata/mill-metadata-domain-model.md`** in the Mill source tree. The normative greenfield specification is **`docs/workitems/completed/20260330-metadata-rework/SPEC.md`** in the Mill source tree (archived story folder).

---

## Related pages

- [Metadata overview](index.md)
- [Concepts](concepts.md)
- [Operator guide](operators.md)
- [Using metadata in Mill UI](mill-ui.md)
- [Mill UI](../mill-ui.md) — feature flags, chat, troubleshooting
