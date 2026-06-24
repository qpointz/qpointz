# OData EDM CSDL Annotations

**Status:** Shipped on `feat/odata-service`  
**Related:** [`odata-service.md`](odata-service.md), [OData CSDL annotations](https://docs.oasis-open.org/odata/odata-csdl-xml/v4.01/odata-csdl-xml-v4.01.html#_Toc38530341), [Org.OData.Core.V1](https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Core.V1.md)

---

## Goal

Expose Mill metadata facets as **OData v4 CSDL annotations** on entity types and structural properties in `$metadata`, using standard vocabulary terms (starting with **Core.Description**).

RWS 2.16 programmatic EDM builders (`PropertyImpl`, `EntityTypeImpl`) do not support attaching vocabulary annotations. Mill therefore:

1. Builds a parallel **annotation model** from facets while constructing the EDM.
2. Post-processes the RWS `$metadata` XML to inject `<Annotation>` elements and Core vocabulary references.

---

## Component layout (`mill-data-odata`)

| Type | Role |
|------|------|
| [`EdmFacetAnnotationContributor`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/annotation/EdmFacetAnnotationContributor.kt) | Maps one facet type → zero or more CSDL annotations |
| [`EdmAnnotationMapper`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/annotation/EdmAnnotationMapper.kt) | Registry of contributors; single entry point for facet → annotation mapping |
| [`EdmAnnotationModel`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/annotation/EdmAnnotationModel.kt) | Annotations keyed by [`EdmAnnotationTarget`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/annotation/EdmAnnotationTarget.kt) (entity type or structural property) |
| [`EntityDataModelFactory.buildPackageForSchema`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/edm/EntityDataModelFactory.kt) | Returns [`SchemaEdmPackage`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/edm/SchemaEdmPackage.kt) (RWS EDM + annotation model) |
| [`CsdlMetadataAnnotationEnhancer`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/render/CsdlMetadataAnnotationEnhancer.kt) | StAX post-processor for `$metadata` XML |

Service wiring (`mill-data-odata-service`):

| Type | Role |
|------|------|
| [`ODataEdmCache`](../../../services/mill-data-odata-service/src/main/kotlin/io/qpointz/mill/data/odata/service/edm/ODataEdmCache.kt) | Caches `SchemaEdmPackage` (EDM + annotations) |
| [`ODataEdmRegistryCache`](../../../services/mill-data-odata-service/src/main/kotlin/io/qpointz/mill/data/odata/service/edm/ODataEdmRegistryCache.kt) | Implements [`EdmAnnotationProvider`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/annotation/EdmAnnotationProvider.kt) |
| [`MillMetadataDocumentRenderer`](../../../services/mill-data-odata-service/src/main/kotlin/io/qpointz/mill/data/odata/service/render/MillMetadataDocumentRenderer.kt) | Replaces RWS `$metadata` renderer (score 101); enhances XML with annotations |

---

## Facet → vocabulary mapping (current)

| Facet | Source field | OData term | Notes |
|-------|--------------|------------|-------|
| `descriptive` | `description`, else `displayName` | `Core.Description` | Brief label / summary |
| `descriptive` | `businessMeaning` | `Core.LongDescription` | Extended business context |

Vocabulary reference emitted once per document:

```xml
<edmx:Reference Uri="https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Core.V1.xml">
  <edmx:Include Namespace="Org.OData.Core.V1" Alias="Core"/>
</edmx:Reference>
```

Constants: [`ODataVocabularyTerms`](../../../data/mill-data-odata/src/main/kotlin/io/qpointz/mill/data/odata/annotation/ODataVocabularyTerms.kt).

---

## Extending with new facet types

1. Add a class implementing `EdmFacetAnnotationContributor` (e.g. map `structural.isPrimaryKey` → a future term, or relation facets → navigation annotations).
2. Register it on `EdmAnnotationMapper(contributors = listOf(..., MyContributor))`.
3. Inject the custom `EdmAnnotationMapper` into `EntityDataModelFactory` via Spring autoconfigure if needed.
4. Add unit tests for the contributor and, when `$metadata`-visible, an integration assertion on `$metadata`.

No changes to `CsdlMetadataAnnotationEnhancer` are required unless new annotation value shapes (non-string terms, collection terms) are introduced.

---

## Planned mappings (not yet implemented)

| Facet | Candidate terms | Notes |
|-------|-----------------|-------|
| `descriptive` | `synonyms`, `tags`, `unit` | No single Core term; may need custom vocabulary or `LongDescription` composition |
| `structural` | `Core.Immutable`, precision hints | Limited Core coverage for PK/FK |
| `relation` | Navigation-specific terms | Partner/capacity often structural, not annotation |
| `concept`, `value-mapping` | Tooling / AI vocabularies | Out of initial OData BI scope |

---

## Example `$metadata` fragment

```xml
<EntityType Name="cities">
  <Key><PropertyRef Name="id"/></Key>
  <Annotation Term="Core.Description" String="Airport cities served by the airline."/>
  <Property Name="city" Type="Edm.String" Nullable="true">
    <Annotation Term="Core.Description" String="City name."/>
  </Property>
</EntityType>
```

---

## Tests

- Unit: `DescriptiveFacetAnnotationContributorTest`, `CsdlMetadataAnnotationEnhancerTest`, `EntityDataModelFactoryAnnotationTest`
- IT: `ODataSkymillIT.shouldReturnMetadataDocument` asserts `Core.Description` and Skymill city table description in `$metadata`
