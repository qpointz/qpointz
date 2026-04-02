# Flow source — inferred facets for Data Model UI

**Spec (normative for design details):** [`SPEC.md`](SPEC.md) — class/module sketch, autoconfigure plan, facet types, YAML mapping table.  
**Metadata source inputs (implementer):** [`METADATA-SOURCE-INPUTS.md`](METADATA-SOURCE-INPUTS.md).  
**Facet seed review copy:** [`review/platform-flow-facet-types.yaml`](review/platform-flow-facet-types.yaml).

Expose **read-only, flow-specific metadata** (from Mill **flow** YAML descriptors) through the same
multi-origin facet pipeline used for logical layout, so operators see **storage**, **table mapping /
readers**, and **column attribute** configuration in **mill-ui** entity details when the active
backend is flow.

**Module placement:** implement the flow **`MetadataSource`** in **`data/mill-data-backends`**, wire
beans in **`data/mill-data-autoconfigure`**, reuse **`data/mill-data-metadata`** as foundation
(`AbstractInferredMetadataSource`, layout helpers — **no** flow-specific source class there). Put
**portable** contracts in **`metadata/mill-metadata-core`** (`MetadataOriginIds`, facet type
definitions in **`platform-flow-facet-types.yaml`** seeded after **`platform-bootstrap.yaml`**). **Generalize** facet URNs / payloads where sensible so
**Calcite** and **JDBC** backends can add parallel contributors later without a second bootstrap pass.

**Extensibility:** facet payloads stay **`type` + `params` KV** so **new storage kinds** (S3, Azure Data Lake, …) and **new reader formats** do not require new facet type URNs. **Projection code** should use **contributor** interfaces and **facet context** read models (`docs/design/data/flow-facet-projection-extensibility.md`) so the same **`flow-*`** facets apply whether descriptors come from YAML files or future repository implementations.

**Planning reference:** `.cursor/plans/Flow UI facets story-c1560ccd.plan.md` (local); execution
tracker is this file. Deferred placeholder: **WI-139** in
`docs/workitems/completed/20260401-metadata-and-ui-improve-and-clean/`.

## Work Items

- [ ] WI-147 — Flow facet types in dedicated seed YAML (`WI-147-flow-facet-types-bootstrap.md`)
- [ ] WI-146 — Source catalog contract + flow descriptor `MetadataSource` (`WI-146-flow-descriptor-metadata-source.md`)
- [ ] WI-148 — Autoconfigure wiring + schema facet tests (`WI-148-flow-facets-autoconfigure-and-tests.md`)
- [ ] WI-149 — UI facet ordering and validation (`WI-149-flow-facets-ui-validation.md`)
- [ ] WI-150 — Pre-closure: design + public docs for backend-provided metadata (`WI-150-pre-closure-docs-backend-metadata.md`)

### Suggested execution order

| Order | WI | Rationale |
|-------|-----|-----------|
| 1 | **WI-147** | **`flow-*`** `FacetTypeDefinition` rows must be on the classpath (or test fixtures) before **WI-146** / **WI-148** can assert registry / **`contentSchema`** validation or realistic merge behaviour. |
| 2 | **WI-146** | Metadata source + **`SourceCatalogProvider`** + Caffeine cache — **Java** in **`mill-data-backends`** per **SPEC §2.4**. |
| 3 | **WI-148** | Auto-config bean + **required** integration test for **`facetsResolved`** (**`originId` `flow`**). |
| 4 | **WI-149** | mill-ui display order (can overlap late). |
| 5 | **WI-150** | Docs closure (can track in parallel). |

**Merge reality:** If **WI-146** lands before **WI-147**, unit tests may **mock** facet types; **acceptance** tests that need the real seed or **`contentSchema`** checks **must not** claim green until **WI-147** is merged or the seed file is present in test resources.
