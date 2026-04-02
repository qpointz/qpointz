# WI-146 — Source catalog contract + flow descriptor `MetadataSource`

**Story:** flow-source-ui-facets  
**Status:** `planned`  
**Type:** `feat`  
**Area:** `data`, `metadata`

**Depends on:** **WI-147** for facet type seed on classpath before **acceptance** tests that validate against **`contentSchema`** / facet registry (**[`STORY.md`](STORY.md)** execution order). Core implementation may start earlier using mocks.

**Language:** **`FlowDescriptorMetadataSource`** and flow-local collaborators are implemented in **Java** in **`mill-data-backends`** (**SPEC §2.4**). Do not add Kotlin to that module without a dedicated Gradle change.

## Goal

Introduce **`SourceCatalogProvider`** in **`mill-data-source-core`** (narrow contract). **`SourceDefinitionRepository`** in **`mill-data-backends`** **implements or extends** it (**verify** today’s interface lives in `io.qpointz.mill.data.backend.flow` and matches **`Iterable<SourceDescriptor>`** semantics — adjust in **WI-146**, no shadow adapter). Implement **`FlowDescriptorMetadataSource`** (read-only inferred
facets) in **`data/mill-data-backends`** (e.g. `io.qpointz.mill.data.backend.flow.metadata`), using
existing layout/foundation patterns from **`mill-data-metadata`** (`AbstractInferredMetadataSource`, called from Java)
without adding flow-specific types there. Use **`MetadataOriginIds.FLOW`** (`"flow"`) as **`originId`**
for **every** flow inferred facet row (`flow-schema`, `flow-table`, `flow-column`); constant lives in
**`mill-metadata-core`**.

## In scope

1. **`mill-data-source-core`:** catalog provider API; **`SourceDefinitionRepository`** implements it.
2. **`mill-data-backends`:** **`FlowDescriptorMetadataSource`** + **`FlowFacetInferenceCache`** (or
   equivalent) using **Caffeine** (`libs.caffeine`). Drive table/reader discovery through existing
   **`SourceResolver` / `SourceMaterializer`** (same as runtime flow), not a hand-rolled scanner.
3. Emit inferred **`FacetInstance`** rows for schema / table / column entities (model path +
   **`SchemaProvider`** + cached resolution snapshot).
4. Unit tests under **`mill-data-backends`** with sample descriptors (`config/test/*.yaml`); include
   at least one test that **cache hit** avoids second blob-resolution when TTL/config allows.
5. **Dependencies:** add **`implementation(libs.caffeine)`** — alias already exists in **`libs.versions.toml`** (`caffeine` 3.x); verify no extra catalog edit needed.

## Out of scope

- **`FlowBackendProperties`** **`metadata`** / **`cache.facets`** — property class and binding tests are
  **already** in **`mill-data-autoconfigure`**; **WI-148** remains for **auto-config bean** + integration tests.
- Facet type YAML seed (**WI-147**).
- UI (**WI-149**).

## Acceptance criteria

- **`SourceCatalogProvider`** exists in **`mill-data-source-core`**; **`SourceDefinitionRepository`** satisfies it (compile-time).
- Flow metadata source merges via **`FacetInstanceReadMerge`** when registered (**WI-148**).
- Tests that assert **`contentSchema`** / registry behaviour for **`flow-*`** types require **WI-147** merged or equivalent test seed.
- Design allows **additional** backend `MetadataSource` beans (JDBC/Calcite, distinct **`originId`** /
  facet families) — see [`docs/design/data/implementing-backend-metadata-source.md`](../../../design/data/implementing-backend-metadata-source.md).
- No secrets beyond YAML; document path redaction if needed.
