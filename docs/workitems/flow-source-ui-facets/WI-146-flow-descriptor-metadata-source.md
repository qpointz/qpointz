# WI-146 — Source catalog contract + flow descriptor `MetadataSource`

**Story:** flow-source-ui-facets  
**Status:** `planned`  
**Type:** `feat`  
**Area:** `data`, `metadata`

## Goal

Introduce **`SourceCatalogProvider`** (or equivalent) in **`mill-data-source-core`** so Spring and
tests can depend on a narrow contract. Implement **`FlowDescriptorMetadataSource`** (read-only inferred
facets) in **`data/mill-data-backends`** (e.g. `io.qpointz.mill.data.backend.flow.metadata`), using
existing layout/foundation patterns from **`mill-data-metadata`** (`AbstractInferredMetadataSource`)
without adding flow-specific types there. Add **`MetadataOriginIds`** value(s) in
**`mill-metadata-core`** (shared by future Calcite/JDBC contributors).

## In scope

1. **`mill-data-source-core`:** catalog provider API; **`SourceDefinitionRepository`** implements it.
2. **`mill-data-backends`:** **`FlowDescriptorMetadataSource`** + **`FlowFacetInferenceCache`** (or
   equivalent) using **Caffeine** (`libs.caffeine`). Drive table/reader discovery through existing
   **`SourceResolver` / `SourceMaterializer`** (same as runtime flow), not a hand-rolled scanner.
3. Emit inferred **`FacetInstance`** rows for schema / table / column entities (model path +
   **`SchemaProvider`** + cached resolution snapshot).
4. Unit tests under **`mill-data-backends`** with sample descriptors (`config/test/*.yaml`); include
   at least one test that **cache hit** avoids second blob-resolution when TTL/config allows.

## Out of scope

- **`FlowBackendProperties`** nested **`cache.facetInference`** wiring (property class + bind test —
  may land in **WI-148** alongside auto-config); behaviour specified in [`SPEC.md` §3.1](SPEC.md#31-flow-backend-configuration--facet-inference-cache).
- Facet type YAML seed (**WI-147**).
- UI (**WI-149**).

## Acceptance criteria

- Flow metadata source merges via **`FacetInstanceReadMerge`** when registered (**WI-148**).
- Design allows a **second** `MetadataSource` bean for JDBC/Calcite later (same origin or distinct —
  documented in **WI-147**).
- No secrets beyond YAML; document path redaction if needed.
