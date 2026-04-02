# Flow facet projection — extensibility and multiple implementations

**Status:** design proposal (normative facet shapes and story scope: `docs/workitems/flow-source-ui-facets/SPEC.md`)  
**Audience:** implementers extending storage types, reader formats, or alternate `SourceDefinitionRepository` implementations  
**Related:** [`flow-backend.md`](../source/flow-backend.md), [`backend-provided-metadata.md`](../metadata/backend-provided-metadata.md), [`implementing-backend-metadata-source.md`](implementing-backend-metadata-source.md), [`metadata-layered-sources-and-ephemeral-facets.md`](../metadata/metadata-layered-sources-and-ephemeral-facets.md)

---

## 1. Review — gaps the story SPEC already covers

| Topic | Where addressed |
|--------|-----------------|
| Polymorphic **storage** and **mapping** in **UI/API** | SPEC §1 — payloads use **`type` discriminator + `params` KV**; avoid rigid `contentSchema` branches per variant. |
| New **reader formats** without new facet URNs | Same — **`format`** + **`params`** KV on **`tableInputs[]`** rows. |
| Single **`originId`** for all flow facets | `MetadataOriginIds.FLOW` (`flow`). |
| Inference path = runtime path | SPEC §1–2 — **`SourceResolver`** / materializer, not a parallel scanner. |
| Configuration | SPEC §3.1 — **`metadata.enabled`**, **`cache.facets`**. |

**Deficits addressed by this document:**

- **Code-level** extensibility: how new **`StorageDescriptor`** subtypes (e.g. S3, Azure Data Lake) and new **reader** kinds plug into **facet building** without editing a monolithic mapper.
- **Same facets, different “flow implementations”**: today YAML files via **`MultiFileSourceRepository`**; tomorrow DB-backed or generated descriptors — **facet types stay `flow-*`**; only **inputs** to projection change.
- **Named “companion” contracts** next to existing flow types: **context** objects and **contributor** interfaces (SPI-like) so `mill-data-source-core` / backends stay cohesive.

---

## 2. Design principle: stable facet contract, pluggable projection

**Public facet contract** (URNs, top-level keys, **`originId`**) should remain **stable** across storage and format growth.

**Extension** happens inside **payload fragments**:

- **`flow-schema` · `storage`:** `type` (string) + **`params`** (object) — new storage kinds add fields inside **`params`** (and document operator-visible keys). Optional **`FlowStorageFacetContributor`** (below) maps `StorageDescriptor` → `{ type, params }`.
- **`flow-table` · `tableInputs[]`:** each row has **`format`** (string) + **`effectiveMapping`** + **`params`** — new formats extend **`params`**; optional **`FlowReaderFormatFacetContributor`** maps `ReaderDescriptor` → row fragments.
- **`flow-column` · `binding`:** `type` + optional **`params`** — attribute kinds stay KV-oriented.

**Metadata seeding** / `contentSchema` stays **coarse** (OBJECT + ARRAY of OBJECT); validation is **lenient** so unknown keys do not break reads.

---

## 3. Flow facet contexts (companion “views”, not duplicate domain)

These are **read-only bundles** passed into projection logic. They are **not** replacements for **`SourceDescriptor`**, **`ResolvedSource`**, or Calcite types — they **carry exactly what facet builders need** to avoid leaking every internal type into `FlowDescriptorMetadataSource`.

| Context (proposed name) | Role |
|-------------------------|------|
| **`FlowSourceFacetContext`** | One **catalog source** (schema name = descriptor `name`): **`SourceDescriptor`**, optional **resolved snapshot** (tables, blob listings) from the same inference path as runtime. |
| **`FlowTableFacetContext`** | **`FlowSourceFacetContext`** + **table name** + **contributing reader resolutions** (ordered list aligned with SPEC **`tableInputs`**). |
| **`FlowColumnFacetContext`** | **`FlowTableFacetContext`** (or minimal parent) + **column name** + **binding** resolution (attribute vs inferred). |

Naming note: “**WithFacets**” was avoided — facets are **projections** of the source, not mix-ins onto `SourceDescriptor`. **Context** makes the one-way **read model** explicit.

Implementations may use **one** facade (`FlowFacetProjectionOrchestrator`) that accepts these contexts and returns **`Map<String, Any?>`** payloads per facet URN.

---

## 4. Contributor interfaces (proposal)

Contributors are **small, testable units** registered in an ordered list (first match or composite by discriminator). They align with existing **Jackson polymorphism** on **`StorageDescriptor`** and reader **`type`** strings.

### 4.1 Storage → `flow-schema` fragment

```kotlin
/**
 * Maps a concrete [StorageDescriptor] to the JSON-safe `storage` block
 * (`type` + `params`) inside the flow-schema facet payload.
 */
interface FlowStorageFacetContributor {
    /** Discriminator value(s) this contributor handles (e.g. `local`, future `s3`, `azure_adls`). */
    fun supportedStorageTypes(): Set<String>

    fun contributeStorageFacet(
        storage: StorageDescriptor,
        ctx: FlowSourceFacetContext,
    ): Map<String, Any?>
}
```

- **Default** implementation handles **`local`** → `type` + **`params`** e.g. `rootPath`.
- **S3 / ADLS:** new modules or subpackages register additional contributors; **`supportedStorageTypes()`** returns the new **`JsonTypeName`** values.
- **Fallback:** a generic contributor serialises unknown **`StorageDescriptor`** to **`type`** + shallow KV if safe for operators (redaction policy in §6).

### 4.2 Reader format → `tableInputs[]` row fragment

```kotlin
/**
 * Enriches one table-input row: `format`, `params`, optional `label`, `readerIndex`.
 * `effectiveMapping` may be produced by shared merge logic, not each format.
 */
interface FlowReaderFormatFacetContributor {
    fun supportedReaderTypes(): Set<String>

    fun contributeReaderRowFragment(
        reader: ReaderDescriptor,
        readerIndex: Int,
        ctx: FlowTableFacetContext,
    ): Map<String, Any?>
}
```

New formats (e.g. **`delta`**, **`orc`**) add a contributor or extend a **default** branch — **no new facet URN**.

### 4.3 Orchestration

- **`FlowFacetProjectionOrchestrator`** (or `FlowFacetPayloadFactory`) in **`mill-data-backends`**:
  - Resolves **storage** contributor by `storage` discriminator (from Jackson type or `StorageDescriptor::class`).
  - For each table, resolves **reader** contributors per **`reader.type`**.
  - Builds **`flow-schema`**, **`flow-table`**, **`flow-column`** maps keyed by SPEC field names.
- **Registration:** Spring beans implementing the interfaces (preferred in Boot) or **`ServiceLoader`** if core must stay Spring-free (match **`StorageDescriptor`** SPI style).

**Same facet maps from different flow implementations:** any code path that can build **`FlowSourceFacetContext`** / **`FlowTableFacetContext`** / **`FlowColumnFacetContext`** can call the **same orchestrator**. **`SourceDefinitionRepository`** (file vs DB) only changes **where `SourceDescriptor` instances come from** — not facet URNs or merge **`originId`**.

---

## 5. Alternate repository / “flow implementation” wiring

| Implementation | Effect on facets |
|----------------|------------------|
| **`MultiFileSourceRepository`** | Today’s YAML → same projection. |
| Future **DB / API repository** | Same **`SourceDescriptor`** model (or adapter normalising to it) → **same** orchestrator. |
| **Tests** | Build descriptors in memory → orchestrator unit tests without filesystem.

Document for each new repository: **normalisation** rules if the wire shape differs from YAML (must still produce **`StorageDescriptor`** + **`ReaderDescriptor`** for the generic projection stack).

---

## 6. Security and evolution

- **Secrets:** connection strings, SAS tokens, and keys must **not** appear raw in **`params`** unless explicitly operator-visible; prefer **redacted** / **referenced** fields (`credentialRef`, `vaultKey`) — align with SPEC §1 security note and deployment policy.
- **Backward compatibility:** additive **`params`** keys only by default; breaking renames require facet type version or new URN (avoid unless necessary).
- **Tests:** each new **`FlowStorageFacetContributor`** / **`FlowReaderFormatFacetContributor`** should have **golden JSON** fragment tests; orchestrator integration test asserts **full** facet payloads for a mixed local + mock-future-storage descriptor.

---

## 7. Module placement (proposal)

| Piece | Module |
|-------|--------|
| **`FlowSourceFacetContext`**, **`FlowTableFacetContext`**, **`FlowColumnFacetContext`** | **`mill-data-source-core`** (with descriptors) *or* **`mill-data-backends`** if contexts must reference resolver types — **prefer backends** until a second module needs them. |
| **`FlowStorageFacetContributor`**, **`FlowReaderFormatFacetContributor`**, orchestrator | **`mill-data-backends`** (`flow.metadata` package). |
| Default **local** + **csv**/etc. contributors | **`mill-data-backends`**. |
| **S3 / cloud** contributors | **`mill-data-backends`** subpackage or new **`mill-data-flow-storage-s3`** style module if optional deps explode. |

---

## 8. Relation to `SourceDescriptor` polymorphism

**`StorageDescriptor`** already uses **`@JsonTypeName`** + SPI for deserialization ([`StorageDescriptor.kt`](../../../data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/descriptor/StorageDescriptor.kt)). **Facet contributors** should use the **same discriminator strings** so YAML, runtime, and **facet `storage.type`** stay aligned.

---

## See also

- Story **[`SPEC.md`](../../workitems/flow-source-ui-facets/SPEC.md)** — payload sketches, caching, autoconfigure.
- **[`backend-provided-metadata.md`](../metadata/backend-provided-metadata.md)** — why backend metadata exists in the merge stack.
