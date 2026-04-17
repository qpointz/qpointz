# Metadata — Value Mapping Bridge and API/UI Surface

Introduce a migration-safe bridge from legacy value mapping to the facet-based metadata model,
then expose value mappings as first-class API resources and make them visible in the metadata
browser UI.

Embedding persistence and delta sync (repository, value sources, sync service) are tracked in a
separate story: [`../../completed/20260416-implement-value-mappings/STORY.md`](../../completed/20260416-implement-value-mappings/STORY.md).

Facet types for **which columns** to index / **retrieval** tuning, **startup + scheduled** vector refresh, and **capability** wiring to the vector store: [`../completed/20260417-value-mapping-facets-vector-lifecycle/STORY.md`](../completed/20260417-value-mapping-facets-vector-lifecycle/STORY.md) (**WI-181–WI-183**).

## Work Items

- [ ] WI-171 — Chroma + Skymill vector exploration (`WI-171-chroma-skymill-vector-exploration.md`)
- [ ] WI-172 — Metadata Value Mapping Bridge and Parity (`WI-172-metadata-value-mapping-bridge.md`)
- [ ] WI-173 — Metadata Value Mapping API and UI Surface (`WI-173-metadata-value-mapping-api-and-ui.md`)
