# `FlowDescriptorMetadataSource` — expected inputs

**Audience:** implementer of **WI-146** / **WI-148**  
**Related:** [`SPEC.md`](SPEC.md) §2.4, [`flow-facet-projection-extensibility.md`](../../design/data/flow-facet-projection-extensibility.md), [`implementing-backend-metadata-source.md`](../../design/data/implementing-backend-metadata-source.md)

This document spells out **what the flow metadata source consumes** (constructor / collaborators, per-request arguments, and derived data from the flow stack).

---

## 1. Lifecycle: who constructs it

**Spring** creates **`FlowDescriptorMetadataSource`** from **`FlowDescriptorMetadataSourceAutoConfiguration`** when:

- `mill.data.backend.type=flow`
- `mill.data.backend.flow.metadata.enabled=true` (default)
- Required collaborator beans exist (see §2)

The source is a normal **`MetadataSource`** bean; **`FacetInstanceReadMerge`** injects the list of all sources.

---

## 2. Injected collaborators (expected constructor / fields)

These are **inputs in the sense of dependencies** — everything needed to map **catalog entity URNs** to **descriptor + resolution** state.

| Input | Type (expected) | Role |
|--------|-----------------|------|
| **Catalog of descriptors** | `SourceDefinitionRepository` *(or future narrow `SourceCatalogProvider`)* | **`getSourceDefinitions()`** → `Iterable<SourceDescriptor>` — **same** definitions the runtime uses. No duplicate YAML path list. |
| **Physical catalog** | `SchemaProvider` | Confirm **schema / table / column** exist for `entityId`; optional: read proto **`Schema`** / **`Table`** / **`Field`** for column lists when aligning attributes. |
| **Backend toggles + cache policy** | `FlowBackendProperties` | **`metadata.enabled`** (also enforced by auto-config), **`cache.facets.enabled`**, **`cache.facets.ttl`** — control facet **inference cache** behaviour. |
| **Projection (recommended)** | `FlowFacetProjectionOrchestrator` *(name may vary)* | Turns **context objects** (§4) into **`Map<String, Any?>`** payloads for each facet URN — keeps `FlowDescriptorMetadataSource` thin. |
| **Inference snapshot cache (recommended)** | `FlowFacetInferenceCache` *(Caffeine)* | **Key:** flow source name (`SourceDescriptor.name`). **Value:** immutable snapshot built via **`SourceResolver`** / materializer (tables, per-table reader contributions, attribute metadata) — **same path as query-time resolution**, not a second scanner. |

**Optional / internal:** contributors (`FlowStorageFacetContributor`, `FlowReaderFormatFacetContributor`) are usually **injected into the orchestrator**, not the metadata source directly.

---

## 3. Per-request API: `fetchForEntity`

```text
fetchForEntity(entityId: String, context: MetadataReadContext): List<FacetInstance>
```

### 3.1 `entityId` (what you must parse)

- **Canonical metadata entity URN** for the Data Model path, e.g.:
  - **Schema:** `urn:mill/model/schema:<name>` (same **name** as `SourceDescriptor.name` for flow).
  - **Table / column:** use existing helpers — **`ModelEntityUrn`**, **`SchemaModelRoot`** in **`mill-data-metadata`** (same as **`LogicalLayoutMetadataSource`**).
- **Parse** → **`schema`**, optional **`table`**, optional **`column`** (case rules follow platform / canonicalisation).

If **`entityId`** is not a model-path entity (or not flow-backed), return **empty list**.

### 3.2 `MetadataReadContext` (read pipeline inputs)

- **`context.isOriginActive(MetadataOriginIds.FLOW)`** — if **false**, return **empty list** (origin muted or filtered).
- **Scopes** — same merge rules as other sources; flow source typically emits **global** scope rows (mirror layout source pattern).
- **`?origin=`** parsing is already reflected in **`context`** before **`fetchForEntity`** runs.

---

## 4. Derived inputs (built inside the source, not injected as beans)

From **`SourceDefinitionRepository`** + **`entityId`**:

1. **Resolve descriptor** — find **`SourceDescriptor`** where **`name`** equals parsed **schema** name; if missing, **no facets** for that schema.
2. **Resolve inference snapshot** (prefer **cache**):
   - Call **`SourceResolver`** / existing flow resolution (**WI-146**) to obtain **tables**, **which readers feed each table**, **effective mapping** per reader, **column + attribute binding** for **`flow-column`**.
   - Cache by **source name** + policy from **`FlowBackendProperties.cache.facets`**.
3. **Build facet contexts** (see design doc) — e.g. **`FlowSourceFacetContext`**, **`FlowTableFacetContext`**, **`FlowColumnFacetContext`** — and pass to **orchestrator** to get **three** payload shapes (or **one** empty list if level does not apply).

**`SchemaProvider`** use:

- **Gate:** if **`!schemaProvider.isSchemaExists(schemaName)`** → optional early exit (descriptor says yes but catalog does not — treat as empty or diagnostic-only per product choice).
- **Column facet:** cross-check column exists on **proto table** when emitting **`flow-column`**.

---

## 5. Outputs (for clarity)

For each applicable **facet type** and entity level, emit **one** **`FacetInstance`** with:

- **`origin`** = **`INFERRED`**
- **`originId`** = **`MetadataOriginIds.FLOW`** (`"flow"`)
- **`facetTypeKey`** = `urn:mill/metadata/facet-type:flow-schema` | `:flow-table` | `:flow-column`
- **`payload`** = JSON-safe **map** per **SPEC** / Appendix B

---

## 6. Unit vs integration testing (pointers)

| Layer | Suggestion |
|-------|------------|
| **Unit** | Mock **`SourceDefinitionRepository`** with **in-memory** `SourceDescriptor`(s); mock or stub **resolver snapshot**; assert **`fetchForEntity`** payloads and **`originId`**. Test **cache hit** when **`cache.facets`** enabled. |
| **Integration (Skymill)** | Use **`test/datasets/skymill`** descriptors already in repo; spin slice with **`mill.data.backend.type=flow`**; assert **`facetsResolved`** (or metadata read) contains **`flow-*`** with expected **sourceName** / **tableInputs**. **WI-148** requires a **passing `testIT`** for this class of check. |
| **Integration (mixed formats)** | [`examples/06_MixingFormats`](../../../examples/06_MixingFormats) — one schema, **multiple reader formats**; optional scenario for richer **`flow-table`** assertions. |

---

## 7. Summary sentence

**Injected:** descriptor catalog + schema provider + flow properties + (recommended) orchestrator + inference cache.  
**Per call:** **`entityId`** + **`MetadataReadContext`**.  
**Derived:** matching **`SourceDescriptor`**, cached **resolver snapshot** for that source, then **orchestrator** → **`FacetInstance`** list.
