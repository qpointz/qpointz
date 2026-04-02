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
   + merge-safe defaults; gate registration with **`mill.data.backend.flow.metadata.enabled`** (default **`true`**; **`false`** ⇒ no flow metadata bean). Inject **`FlowBackendProperties`** into **`FlowDescriptorMetadataSource`** /
   cache bean so **`mill.data.backend.flow.cache.facets.{enabled,ttl}`** controls Caffeine (**[`SPEC.md` §3.1](SPEC.md#31-flow-backend-configuration--metadata-switch-and-facet-cache)**).
2. Ensure **`FlowBackendProperties`** exposes **`metadata`** + **`cache.facets`** (see **WI-146** / SPEC §3.1); extend **`FlowBackendAutoConfigurationTest`** (or equivalent) for binding.
3. Unit-level **`SchemaFacetService`** (or equivalent) tests: flow YAML under test resources; assert facet types and
   **`originId`** **`flow`** on resolved rows.
4. **Required integration test (`testIT`):** Spring context with **`mill.data.backend.type=flow`** (reuse **Skymill** / `test/datasets/skymill` or **[`examples/06_MixingFormats`](../../../examples/06_MixingFormats)**-style descriptors) such that a schema explorer / facet read path returns **`facetsResolved`** (or equivalent DTO) **containing** at least one inferred facet with **`originId: flow`** and a **`flow-*`** `facetTypeKey`. Follow an existing **`testIT`** pattern (e.g. schema facet service IT) — “optional” is **not** sufficient for story closure.

## Out of scope

- **mill-ui** changes (**WI-149**).

## Acceptance criteria

- `./gradlew` tests for touched modules pass (note scopes in MR).
- At least one **unit** test fails if **`FlowDescriptorMetadataSource`** stops contributing expected facet rows.
- **Integration:** `./gradlew :<module>:testIT` (or root aggregate) includes a **passing** **`testIT`** that proves **end-to-end** **`facetsResolved`** (or metadata read API) includes **`originId: flow`** for a flow-backed entity.
