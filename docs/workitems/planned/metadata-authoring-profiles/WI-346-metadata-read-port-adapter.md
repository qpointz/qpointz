# WI-346 — `MetadataReadPort` in-process adapter and Spring wiring

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `metadata`  
Depends on: [WI-345](WI-345-metadata-authoring-design-contract.md)

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
   - `listEntityFacets(...)` — delegate to facet assignment read path (same filters as REST)
   - `validateFacetPayload(...)` — classpath / manifest rules via existing
     [`FacetPayloadStructureValidator`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/FacetPayloadStructureValidator.kt)
2. **Unit tests** with fakes or test doubles for catalog + entity reads
3. **`mill-ai-autoconfigure`**:
   - `@ConditionalOnBean` (or equivalent) registers real port when metadata stack present
   - Preserve `EmptyMetadataReadPort` as `@ConditionalOnMissingBean` fallback
4. **`mill-service` / Skymill** — verify bean wiring in integration slice (no new public REST)

## Out of Scope

- HTTP client loopback to `/api/v1/metadata` (in-process only)
- Metadata write / import APIs
- Changing `MetadataReadPort` interface surface beyond optional manifest fields already on
  `FacetTypeManifest` (e.g. `source`)

## Acceptance Criteria

- [ ] With metadata autoconfigure on classpath, `MetadataReadPort` bean is non-empty in Skymill IT
- [ ] `listFacetTypes()` returns platform seeds (e.g. `descriptive`, `relation`, DQ types) in IT
- [ ] `EmptyMetadataReadPort` still used when metadata module absent (unit test)
- [ ] No regression in `SchemaFacingCapabilityDependencyFactoryTest`

## Suggested commit

`[feat] WI-346: in-process MetadataReadPort adapter and Spring wiring`
