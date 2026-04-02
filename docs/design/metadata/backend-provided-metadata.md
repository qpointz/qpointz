# Backend-provided metadata (inferred facets)

**Audience:** implementers, operators, reviewers  
**Status:** design reference (flow story archived: `docs/workitems/completed/20260402-flow-source-ui-facets/`, normative facet payloads and modules in **`SPEC.md`**)  
**Related:** [`metadata-layered-sources-and-ephemeral-facets.md`](metadata-layered-sources-and-ephemeral-facets.md), [`mill-metadata-domain-model.md`](mill-metadata-domain-model.md)

---

## Purpose

**Backend metadata** (in this document) means **read-only inferred** facet rows contributed by the **active data backend** through a **`MetadataSource`** bean. They **expose backend-specific information**: how storage, readers, table mapping, and similar **configuration** relate to catalog entities (schemas, tables, columns) — information that is **not** the same as generic logical structure alone.

Examples of intent:

- **Flow** — storage location and type, per-table reader inputs, column binding hints derived from source descriptors (see story **`SPEC.md`** for facet types **`flow-*`** and `originId` **`flow`**).
- **Future** — JDBC or Calcite backends may contribute their own facet families with distinct **`originId`** values and type keys, without overloading **logical layout**.

This complements:

| Layer | Typical `originId` (constant) | Role |
|--------|-------------------------------|------|
| **Repository** | `repository-local` | Captured assignments in the metadata store. |
| **Logical layout** | `logical-layout` | Structural/descriptive projection from **`SchemaProvider`** (catalog shape shared across backends). |
| **Backend-specific** | e.g. `flow` | Descriptor- or connector-level facts **specific to how this backend is configured**. |

Backend-provided rows use **`FacetOrigin.INFERRED`**, like **`LogicalLayoutMetadataSource`**: they are **not** persisted as **`FacetAssignment`** rows and **cannot** be edited through metadata mutation APIs when guards apply.

---

## Why a separate layer from logical layout?

**`LogicalLayoutMetadataSource`** answers: *what tables and columns exist in the physical catalog snapshot?*

**Backend metadata** answers: *how does **this** backend’s configuration (files, connection rules, mapping discipline) explain those objects to an operator?*

Keeping the two layers separate avoids overloading layout facets with backend-specific keys and allows **optional filtering** by **`originId`** on reads (`?origin=`).

---

## Spring wiring conventions

- **`MetadataSource`** implementations for **physical backends** live in **data** modules (e.g. **`mill-data-backends`** for flow).
- **Auto-configuration** that registers those beans lives in **`mill-data-autoconfigure`**.
- **Flow** registration is **flow-backend-specific**: configuration classes under **`io.qpointz.mill.autoconfigure.data.backend.flow`**, **`@AutoConfigureAfter(FlowBackendAutoConfiguration.class)`**, and conditions on **`mill.data.backend.type=flow`** plus **`mill.data.backend.flow.metadata.enabled`** (see story **`SPEC.md` §3). This is **intentionally not** the same package as **`LogicalLayoutMetadataSourceAutoConfiguration`** (`data.schema`), which applies whenever **`SchemaProvider`** exists.

**`mill-metadata-autoconfigure`** continues to aggregate **all** **`MetadataSource`** beans in **`FacetInstanceReadMerge`**; it does not own backend-specific beans.

---

## Configuration (flow)

Properties are defined on **`FlowBackendProperties`** (`mill.data.backend.flow`):

| Property | Meaning |
|----------|---------|
| **`metadata.enabled`** | When **`false`**, the flow descriptor **`MetadataSource`** is **not** registered; no contributions with **`originId` `flow`**. Query/schema behaviour of the flow backend is unchanged. Default **`true`**. |
| **`cache.facets.enabled`** | When **`false`**, facet inference is not snapshot-cached (always on demand / no-op cache). Default **`true`**. |
| **`cache.facets.ttl`** | Optional cache TTL for facet inference (e.g. `5m`). |

Schema caching (**`cache.schema.*`**) remains separate: it optimises Calcite context/schema reuse, not the metadata facet inference path.

Full YAML examples: story **`SPEC.md` §3.1 and public **[Flow backend](../../public/src/backends/flow.md)**.

---

## Facet types and seeds

Flow facet **type definitions** are seeded separately from **`platform-bootstrap.yaml`** (dedicated resource listing in **`mill.metadata.seed.resources`**) per story **`SPEC.md`**. URNs use the **`flow-*`** family and **`category: flow`**.

---

## REST and UI

- Read APIs expose merged **`FacetInstance`** rows with **`origin`**, **`originId`**, and **`editable`**; inferred backend rows are read-only in the UI when surfaced.
- Operators may use **`?origin=`** to narrow to **`flow`** (or other ids) for debugging or specialised clients.

---

## Related documents

- [`metadata-layered-sources-and-ephemeral-facets.md`](metadata-layered-sources-and-ephemeral-facets.md) — merge and sources table.
- [`../data/implementing-backend-metadata-source.md`](../data/implementing-backend-metadata-source.md) — **implementer guide:** shared foundation vs per-backend facet families (`originId`, `category`, payloads).
- [`../data/flow-facet-projection-extensibility.md`](../data/flow-facet-projection-extensibility.md) — **extensible** flow facet projection (storage / reader contributors, contexts, multiple `SourceDefinitionRepository` implementations).
- [`docs/workitems/completed/20260402-flow-source-ui-facets/SPEC.md`](../../workitems/completed/20260402-flow-source-ui-facets/SPEC.md) — facet payloads, modules, autoconfigure steps.
- Public: [Backend metadata](../../public/src/metadata/backend-metadata.md) — operator-oriented summary.
