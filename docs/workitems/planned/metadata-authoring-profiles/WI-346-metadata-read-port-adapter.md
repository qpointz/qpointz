# WI-346 — `MetadataReadPort` in-process adapter and Spring wiring

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `metadata`  
Depends on: [WI-345](WI-345-metadata-authoring-design-contract.md), [WI-352](WI-352-metadata-content-entity-and-seed.md)  
**Stage:** 3 — branch `feat/metadata-read-port` (see [`STORY.md`](STORY.md))

## Problem Statement

[`MetadataReadPort`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataReadPort.kt)
is the sole dependency for **`metadata`** and **`metadata-authoring`** tools. Autoconfigure still
registers [`EmptyMetadataReadPort`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/EmptyMetadataReadPort.kt),
so **`list_facet_types`** returns `[]` and validation always fails on mill-service — the gap
**WI-205** described but did not land in **`mill-ai-data`**.

## Goal

Implement a production **`MetadataReadPort`** in **`mill-ai-data`** backed by in-process metadata
facet services, and wire it through **`mill-ai-autoconfigure`** when metadata beans are available.

## In Scope

1. **`mill-ai-data`** adapter (e.g. `ServiceMetadataReadPort`):
   - `listFacetTypes()` — merge DEFINED definitions; include OBSERVED-only keys when repository
     exposes them (best-effort alignment with **M-32**; DEFINED wins on dedup)
   - `getFacetType(facetTypeKey)` — optional efficiency helper for **`get_facet_type`** tool
   - `listEntityFacets(...)` — delegate to facet assignment read path (same filters as REST)
   - `listContent(targetUrn?, contentKind?)` / `getContent(contentUrn)` — **`MetadataContent`** rows (**WI-352**)
   - `listFacetCategories()` — distinct categories + joined category guidance content
   - `validateFacetPayload(facetTypeKey, payload, metadataEntityId?)` — classpath / manifest rules via
     [`FacetPayloadStructureValidator`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/FacetPayloadStructureValidator.kt);
     when **`metadataEntityId`** is present, also enforce facet type **`applicableTo`** vs target entity kind
     (resolve kind from URN; consult `FacetTypeManifest.applicableTo`)
2. **Unit tests** with fakes or test doubles for catalog + entity reads
3. **`mill-ai-autoconfigure`**:
   - `@ConditionalOnBean` (or equivalent) registers real port when metadata stack present
   - Preserve `EmptyMetadataReadPort` as `@ConditionalOnMissingBean` fallback
4. **`mill-service` / Skymill** — verify bean wiring in integration slice (no new public REST)

## Out of Scope

- **`MetadataContent`** entity / seeds / merger — **WI-352**
- HTTP client loopback to `/api/v1/metadata` (in-process only)
- Metadata write / import APIs
- Tool handler changes — **WI-347**
- **`HarnessMetadataReadPort`** catalog expansion — **this WI** ([`GAPS.md`](GAPS.md) §12)
- Changing `FacetTypeManifest` seed fields beyond what catalog already exposes (e.g. `source`)

## Port contract (locked — [`GAPS.md`](GAPS.md) §2)

Extend [`MetadataReadPort`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataReadPort.kt):

```kotlin
fun validateFacetPayload(
    facetTypeKey: String,
    payload: Map<String, Any?>,
    metadataEntityId: String? = null,
): List<String>
```

Update `EmptyMetadataReadPort`, harness port, and `validateFacetPayloadInternal` in **`mill-ai`** as part of this WI.

### Harness catalog expansion ([`GAPS.md`](GAPS.md) §12)

Expand [`HarnessMetadataReadPort`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/runner/ScenarioHarnessSupport.kt) in **this WI** (not WI-349):

| `facetTypeKey` | Purpose |
| -------------- | ------- |
| `descriptive` | existing |
| `relation-source`, `relation-target` | relation scenarios |
| `dq-null-check`, `dq-predicate` | DQ scenarios |

Minimal `FacetTypeManifest` + `contentSchema` per type; stub `listContent` / `listFacetCategories` / `getFacetType` as needed for port contract. Type keys align with **WI-352** seeds — **no** platform seed import in harness.

**WI-349** consumes this harness for scenario packs only.

## Acceptance Criteria

- [ ] With metadata autoconfigure on classpath, `MetadataReadPort` bean is non-empty in Skymill IT
- [ ] `listFacetTypes()` returns platform seeds (e.g. `descriptive`, `relation`, DQ types) in IT
- [ ] `listContent` / `getContent` return WI-352 platform seeds in IT
- [ ] `listFacetCategories()` returns `general`, `relation`, `data-quality` guidance rows
- [ ] `EmptyMetadataReadPort` still used when metadata module absent (unit test)
- [ ] `validateFacetPayload(..., metadataEntityId)` rejects type when **`applicableTo`** does not match target entity kind (unit test with fake catalog + URN)
- [ ] **`HarnessMetadataReadPort`** lists ≥5 facet types (descriptive, relation-source, relation-target, dq-null-check, dq-predicate) per §12
- [ ] No regression in `SchemaFacingCapabilityDependencyFactoryTest`

`[feat] WI-346: in-process MetadataReadPort adapter and Spring wiring`
