# Facet class elimination — removing "blessed" concrete facet types

**Status:** Shipped (story `metadata-and-ui-improve-and-clean`, WI-140; April 2026)  
**Date:** March–April 2026

---

## Problem statement (historical)

The metadata module historically maintained **two parallel representations** for some facet types: a **generic** manifest-driven pipeline (`FacetPayloadSchema` + opaque `Map<String, Any?>` payloads) and a set of **Kotlin facet classes** with `merge()` / `validate()` / `setOwner()` on a **`MetadataFacet`** hierarchy. That split duplicated shapes, merge semantics, and validation.

## As-built model

- **Persistence:** **`FacetAssignment`** rows in **`FacetRepository`**; the unified **read** projection is **`FacetInstance`** with **`FacetOrigin`**, **`originId`**, and payload map.
- **Typed consumption:** callers that need Java/Kotlin types use **`FacetPayloadUtils.convert(payload, clazz)`** (`mill-metadata-core`) with standard Jackson mapping — **no** `FacetClassResolver` / **`FacetConverter`** registry.
- **Data-oriented types:** **`DescriptiveFacet`**, **`ConceptFacet`**, **`ValueMappingFacet`**, **`TableLocator`**, **`TableType`**, **`ConceptTarget`** remain as **plain** `data class` / enum **without** a shared facet lifecycle base class (WI-140).

---

## Architectural issues addressed

| # | Issue | Resolution |
|---|-------|------------|
| 1 | Dual shape definition (manifest vs Kotlin fields) | Manifest + schema remain authoritative; Kotlin types are optional helpers for known shapes. |
| 2 | Dual merge semantics | Row-level merge via **`FacetInstanceReadMerge`** + **`MergeAction`** on assignments; no parallel class-level `merge()`. |
| 3 | Dual validation | Structural validation via **`FacetTypeManifestNormalizer`** / manifests; consumers validate typed views locally if needed. |
| 4 | Registry indirection | **`FacetPayloadUtils`** replaces resolver/converter bridge. |

---

## Removed or retired components

- **`FacetClassResolver`**, **`FacetConverter`** — removed; use **`FacetPayloadUtils`**.
- **`MetadataFacet`**, **`AbstractFacet`** — removed; data classes do not implement a facet lifecycle interface.

---

## Follow-up cleanup (WI-130)

The following types had **no** production consumers and were **deleted** in WI-130:

| Removed | Notes |
|---------|--------|
| `FacetTypeConflictException`, `FacetTypeNotFoundException` | Never thrown; **`FacetTypeManifestInvalidException`** kept |
| `FacetDto`, `FacetTypeDescriptorDto` | Orphan service DTOs; API uses **`FacetInstanceDto`** |
| `MetadataSnapshotService`, `DefaultMetadataSnapshotService` | Unwired |
| `ResourceResolver`, `SpringResourceResolver`, `ClasspathResourceResolver` | Unused wiring |

**Still unused (not removed in WI-130):** **`PlatformFacetTypeDefinitions`** — no non-self references; candidate for a future sweep if product does not adopt it.

**Thin helper:** **`MetadataView`** (`mill-metadata-core`) — excluded from WI-130; migrate call sites to **`MetadataReadContext`** / **`FacetService`** as needed.

**Keep:** **`FacetPayloadFieldJsonSerde`** and related Jackson helpers where referenced; **`FacetTypeManifestInvalidException`**.

---

## Related documents

- [`mill-metadata-domain-model.md`](mill-metadata-domain-model.md)
- [`metadata-layered-sources-and-ephemeral-facets.md`](metadata-layered-sources-and-ephemeral-facets.md)
- [`dynamic-facet-types-schema-and-validation.md`](dynamic-facet-types-schema-and-validation.md)
