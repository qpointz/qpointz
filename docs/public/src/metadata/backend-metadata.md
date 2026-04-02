# Backend metadata in the Data Model

Mill can show **extra facet panels** on schemas, tables, and columns that come **directly from the active query backend**, not from the metadata repository. Think of this as **“how this backend is wired”**: storage paths, file readers, table mapping, and similar **operator-facing** detail that helps you audit a deployment next to **captured** descriptions and **logical** structure.

---

## What it is for

**Purpose:** **Expose backend-specific information** — facts that depend on *this* backend’s configuration (for example Mill **flow** source descriptors) — in the same **Data Model** views that already show stored metadata and inferred **logical layout**.

That lets you:

- See **why** a table appears in the catalog (readers, mapping rules) without opening YAML on the server (subject to what your org chooses to surface).
- Distinguish **repository-backed** glossaries from **runtime-inferred** structure and **backend** binding context.

This layer is **read-only** in the UI: it is **not** a substitute for editing captured facets.

---

## How it relates to other facet sources

Mill merges several **sources** of facet rows when you open entity details:

| Source | What you see | Typical `originId` in APIs |
|--------|----------------|----------------------------|
| **Metadata repository** | Descriptions, relations, and other **saved** assignments you can edit (when allowed). | `repository-local` |
| **Logical layout** | Structural hints from the **catalog snapshot** (schemas, tables, columns) shared across backends. | `logical-layout` |
| **Backend metadata** | **Configuration- or connector-specific** facets for the **active** backend (flow descriptors today; others may follow). | e.g. `flow` |

Details on captured vs inferred and **optional API filters**: [Multi-source facets](multi-source-facets.md).

---

## Flow backend (file-based sources)

When **`mill.data.backend.type`** is **`flow`**, Mill may register a metadata contributor that emits **flow** facet types (for example storage summary, table inputs, column binding — exact keys are defined in the product’s facet type catalog; technical shape is documented in the repository story spec).

### Turn backend metadata on or off

All properties are under **`mill.data.backend.flow`**.

| Property | Default | What it does |
|----------|---------|----------------|
| **`metadata.enabled`** | `true` | Set to `false` to **stop registering** the flow descriptor metadata source. No **`originId: flow`** rows appear in the Data Model; **queries and schema discovery keep working**. |
| **`cache.facets.enabled`** | `true` | When `false`, facet inference for those panels is always computed on demand (no snapshot cache). |
| **`cache.facets.ttl`** | unset | Optional duration (e.g. `5m`) controlling how long snapshot inference may be cached. |

**Note:** **`cache.schema.*`** is separate: it controls **Calcite schema** reuse for query planning, not the Data Model facet cache above.

### Example

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        metadata:
          enabled: true
        cache:
          facets:
            enabled: true
            ttl: 5m
        sources:
          - ./config/my-source.yaml
```

Full flow backend reference (sources, dialect, schema cache): [Flow backend](../backends/flow.md).

---

## Other backends

JDBC and Calcite backends may gain their own **backend metadata** contributors in the future, each with a dedicated **`originId`** and facet types. The same merge rules apply: read APIs combine repository, logical layout, and backend-specific inferred rows unless you filter by **`origin`**.

### Evolving storage and formats (flow)

Flow may gain **new storage kinds** (cloud object stores, etc.) and **new file formats** without changing the **names** of the Data Model facet types: extra detail appears inside **payload fields** (`params`-style maps). Implementers: see the repository design note `docs/design/data/flow-facet-projection-extensibility.md`.

---

## See also

- [Multi-source facets](multi-source-facets.md) — `origin`, `originId`, `?scope=` / `?origin=`
- [Metadata in Mill](system.md) — product overview
- [Flow backend](../backends/flow.md) — configuration reference for file-based query
- Design (repository): `docs/design/metadata/backend-provided-metadata.md`
