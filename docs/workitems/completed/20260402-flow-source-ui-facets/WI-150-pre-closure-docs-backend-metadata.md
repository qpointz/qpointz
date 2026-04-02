# WI-150 — Pre-closure: design + public docs for backend-provided metadata

**Story:** flow-source-ui-facets  
**Status:** `planned`  
**Type:** `docs`  
**Area:** `documentation`

## Goal

Before **story closure**, align **design** and **public** documentation with **backend-provided (inferred) metadata**: what it is for, how it appears next to captured facets, how operators configure it (using the **flow** backend as the first concrete example), and where normative technical detail lives.

## Scope

1. **`docs/design/metadata`** and **`docs/design/data`**
   - Add or extend a design note describing **backend-provided** `MetadataSource` contributions: purpose (**expose backend-specific configuration and binding facts** — not a second copy of the full YAML dump unless intended), **`originId`** attribution, merge with repository + logical layout, and **Spring placement** (e.g. flow-specific auto-configuration in `mill-data-autoconfigure`, not generic `data.schema` wiring).
   - **Extensible flow facet projection** (polymorphic storage, evolving reader formats, shared facets across descriptor repositories): **`docs/design/data/flow-facet-projection-extensibility.md`**, referenced from **SPEC.md** §5.1.
   - Update **`metadata-layered-sources-and-ephemeral-facets.md`** (registered sources table / related links).
   - Update **`docs/design/metadata/README.md`** index.
   - Where useful, add a short pointer in **`metadata-documentation.md`** (configuration section) to the new design note.

2. **`docs/public`**
   - Add a **user/operator-facing** page on **backend metadata** (purpose, read-only inferred facets, `originId`, relation to [Multi-source facets](metadata/multi-source-facets.md)).
   - Extend **[Flow backend](backends/flow.md)** with a concise **Data Model / backend metadata** subsection linking to that page; keep the **properties reference** consistent with **`FlowBackendProperties`** (`metadata.enabled`, `cache.facets.*`).
   - Register the new page in **`mkdocs.yml`** under **Metadata**.
   - Optionally link from **`metadata/index.md`** and **[Backends](backends/index.md)** shared configuration.

## Normative technical detail

Implementation facets, payload shapes, and module layout remain in **[`SPEC.md`](SPEC.md)** for this story. Docs should **summarise** and **link** to `SPEC.md` where operators or implementers need detail.

## Acceptance criteria

- A new reader can answer: **why** backend metadata exists (**surface backend-specific information** in the Data Model), **how** to turn it on/off for flow (**`mill.data.backend.flow.metadata.enabled`**), and **how** facet inference caching is tuned (**`mill.data.backend.flow.cache.facets`**).
- Design and public texts do not contradict **`SPEC.md`** or committed **`FlowBackendProperties`** fields.
- **`./gradlew`** is unaffected (docs-only WI); `mkdocs build` for `docs/public` succeeds if CI runs it.

## Deliverables (this branch)

- [`docs/design/metadata/backend-provided-metadata.md`](../../design/metadata/backend-provided-metadata.md) — design reference.
- [`docs/design/data/flow-facet-projection-extensibility.md`](../../design/data/flow-facet-projection-extensibility.md) — extensible projection model (storage/reader growth, contexts, contributors).
- [`docs/design/data/implementing-backend-metadata-source.md`](../../design/data/implementing-backend-metadata-source.md) — implementer guide for backend `MetadataSource` (foundation vs per-backend families).
- Updates: [`metadata-layered-sources-and-ephemeral-facets.md`](../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md), [`README.md`](../../design/metadata/README.md), [`metadata-documentation.md`](../../design/metadata/metadata-documentation.md) (configuration pointer).
- Public: [`docs/public/src/metadata/backend-metadata.md`](../../public/src/metadata/backend-metadata.md); links from [`metadata/index.md`](../../public/src/metadata/index.md), [`multi-source-facets.md`](../../public/src/metadata/multi-source-facets.md), [`backends/flow.md`](../../public/src/backends/flow.md), [`backends/index.md`](../../public/src/backends/index.md); [`mkdocs.yml`](../../public/mkdocs.yml) nav entry.

## Out of scope

- Implementing **`FlowDescriptorMetadataSource`** or changing production code (other WIs).
