# WI-148 — Autoconfigure wiring + schema facet tests

**Story:** flow-source-ui-facets  
**Status:** `planned`  
**Type:** `feat`  
**Area:** `data`, `spring`

## Goal

Register the flow descriptor **`MetadataSource`** from **`data/mill-data-autoconfigure`** when the
flow backend is active, and prove **`facetsResolved`** on schema explorer DTOs contains the new
inferred rows. Structure configuration so **Calcite** / **JDBC** can register additional
**`MetadataSource`** beans the same way (`@ConditionalOnProperty` / `@ConditionalOnBean` per backend).

## In scope

1. Auto-configuration (mirror **`LogicalLayoutMetadataSourceAutoConfiguration`**): e.g.
   **`@ConditionalOnBean(SourceCatalogProvider.class)`** or explicit **`mill.data.backend.type=flow`**
   + merge-safe defaults. Inject **`FlowBackendProperties`** into **`FlowDescriptorMetadataSource`** /
   cache bean so **`mill.data.backend.flow.cache.facetInference.{enabled,ttl}`** controls Caffeine (**[`SPEC.md` §3.1](SPEC.md#31-flow-backend-configuration--facet-inference-cache)**).
2. Extend **`FlowBackendProperties`** (**Java**, existing pattern) with **`cache.facetInference`**
   nested properties; update **`FlowBackendAutoConfigurationTest`** (or equivalent) for binding.
3. **`SchemaFacetService`**-level tests: stub or flow YAML under test resources; assert facet types and
   origins (`originId` for the new source).
4. Optional **`testIT`** with **`mill.data.backend.type=flow`** if an existing pattern exists
   (**`SchemaFacetServiceSkyMillIT`**-style).

## Out of scope

- **mill-ui** changes (**WI-149**).

## Acceptance criteria

- `./gradlew` tests for touched modules pass (note scopes in MR).
- At least one test fails if **`FlowDescriptorMetadataSource`** stops contributing expected facet rows.
