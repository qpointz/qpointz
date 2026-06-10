# Implement value mappings - persistence and sync

**Milestone:** **0.8.0** (see [`MILESTONE.md`](../../MILESTONE.md)).

**Work items (this folder):**

| WI | Document |
|----|----------|
| **WI-175** | [`WI-175-mill-ai-v3-ai-configuration-foundation.md`](WI-175-mill-ai-v3-ai-configuration-foundation.md) |
| **WI-176** | [`WI-176-embedding-model-harness.md`](WI-176-embedding-model-harness.md) |
| **WI-177** | [`WI-177-vector-store-harness.md`](WI-177-vector-store-harness.md) |
| **WI-174** | [`WI-174-value-mapping-embedding-repository.md`](WI-174-value-mapping-embedding-repository.md) - spec **finalized** (ready for implementation) |
| **WI-178** | [`WI-178-value-mappings-stack-documentation.md`](WI-178-value-mappings-stack-documentation.md) - **docs** (pre-story closure) |
| **WI-179** | [`WI-179-sync-vectors-hydration.md`](WI-179-sync-vectors-hydration.md) - column sync (values -> repository -> vector store) |
| **WI-180** | [`WI-180-value-mapping-service-orchestrator.md`](WI-180-value-mapping-service-orchestrator.md) - **ValueMappingService** (implementation) |

**Persistence vs search:** [**WI-174**](WI-174-value-mapping-embedding-repository.md) is the **golden source** for vectors in the DB. [**WI-177**](WI-177-vector-store-harness.md) is the **runtime vector store for similarity search**. [**WI-179**](WI-179-sync-vectors-hydration.md) defines the **reconciliation** between them (normative routine in that WI). [**WI-180**](WI-180-value-mapping-service-orchestrator.md) implements **ValueMappingService** for product code, with REST and metadata surfaces out of scope for this story unless a later WI adds them explicitly.

Deliver **repository**, **`mill.ai.*`** configuration, **LangChain4j** embedding and vector-store harnesses, then **sync**, **orchestrator**, and **stack documentation** so context is not lost across WIs.

## Execution order (dependencies)

**Core configuration and harnesses (implement first):**

1. **WI-175** - `mill.ai.providers` / AI configuration foundation (**no** dependency on other WIs in this story).
2. **WI-176** - embedding model harness (**depends on WI-175**).
3. **WI-177** - vector store harness - **single** backend per instance (**Spring conditional**); **MVP: in-memory**; **pgvector** / **Chroma** deferred ([**WI-177**](WI-177-vector-store-harness.md)) (**depends on WI-175**).
4. **WI-174** - embedding repository (**independent** of 175-177 at compile time; may proceed in parallel).

**Follow-on (after core pieces exist):**

5. **WI-179** - sync vectors - value source + repository + store (**depends on WI-174**, **WI-176**, **WI-177**); **spec** in [`WI-179`](WI-179-sync-vectors-hydration.md).
6. **WI-180** - **ValueMappingService** implementation (**depends on WI-174**, **WI-176**, **WI-177**, **WI-179** sync API). **Integration proof:** [`ChromaSkymillDistinctVectorIT`](../../../../ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt) is reimplemented to sync Chroma through `ValueMappingService`, not manual `EmbeddingStore.add` in the test (see [**WI-180**](WI-180-value-mapping-service-orchestrator.md) test plan). Opt-in Chroma: `MILL_CHROMA_IT_ENABLED` / `MILL_CHROMA_BASE_URL` (predecessor: [**WI-171**](../../planned/metadata-value-mapping/WI-171-chroma-skymill-vector-exploration.md)).

**Documentation (before story archive):**

7. **WI-178** - design + inventory + **STORY** alignment (**runs as a pre-closure sweep** so the stack stays navigable).

## Scope (this story)

- **WI-175:** **`mill.ai.*` AI configuration foundation** - first slice **`mill.ai.providers`**.
- **WI-176:** **Embedding model harness** - registry, **`embed()`**, LangChain4j + stub.
- **WI-177:** **Vector store harness** - LangChain4j **`EmbeddingStore`**, **in-memory** MVP, extensible config for future backends.
- **WI-174:** **Repository** - Flyway, ports, **`mill-ai-v3-persistence`** `testIT`.
- **WI-179 / WI-180 / WI-178:** see linked WI files.
- **Out of scope:** metadata facet bridge, REST/UI for mappings (see [`../../planned/metadata-value-mapping/STORY.md`](../../planned/metadata-value-mapping/STORY.md)).
- **Predecessor context:** exploratory Chroma + Skymill - [**WI-171**](../metadata-value-mapping/WI-171-chroma-skymill-vector-exploration.md).

## Placement

**Archived** under [`docs/workitems/completed/20260416-implement-value-mappings/`](.) (closure 2026-04-16; see [`RULES.md`](../../RULES.md)).

## Work Items

- [x] WI-175 - Mill AI v3 AI configuration foundation (`WI-175-mill-ai-v3-ai-configuration-foundation.md`)
- [x] WI-176 - Embedding model harness (`WI-176-embedding-model-harness.md`)
- [x] WI-177 - Vector store harness (`WI-177-vector-store-harness.md`)
- [x] WI-174 - Value mapping embedding repository (`WI-174-value-mapping-embedding-repository.md`)
- [x] WI-179 - Sync vectors - column reconciliation (`WI-179-sync-vectors-hydration.md`)
- [x] WI-180 - Value mapping service - implementation (`WI-180-value-mapping-service-orchestrator.md`)
- [x] WI-178 - Value mappings stack documentation (`WI-178-value-mappings-stack-documentation.md`)

### Planned as separate WIs (not in this folder yet)

- **Value sources (product)** - rich `AttributeValueSource` implementations beyond what **WI-179** needs for its port (JDBC, REST, etc.) remain **future work**. This story defines the **contract only**; concrete implementations are expected to be **provided externally** or added in later WIs.
- **Broader integration tests** - additional Skymill-style scenarios beyond the **ChromaSkymillDistinctVectorIT** refresh above (may build on **WI-171** learnings).

## Related stories

- Metadata bridge and API/UI: [`../../planned/metadata-value-mapping/STORY.md`](../../planned/metadata-value-mapping/STORY.md)
