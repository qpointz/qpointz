# Flow source — inferred facets for Data Model UI

**Spec (normative for design details):** [`SPEC.md`](SPEC.md) — class/module sketch, autoconfigure plan, facet types, YAML mapping table.

Expose **read-only, flow-specific metadata** (from Mill **flow** YAML descriptors) through the same
multi-origin facet pipeline used for logical layout, so operators see **storage**, **table mapping /
readers**, and **column attribute** configuration in **mill-ui** entity details when the active
backend is flow.

**Module placement:** implement the flow **`MetadataSource`** in **`data/mill-data-backends`**, wire
beans in **`data/mill-data-autoconfigure`**, reuse **`data/mill-data-metadata`** as foundation
(`AbstractInferredMetadataSource`, layout helpers — **no** flow-specific source class there). Put
**portable** contracts in **`metadata/mill-metadata-core`** (`MetadataOriginIds`, facet type
definitions in `platform-bootstrap.yaml`). **Generalize** facet URNs / payloads where sensible so
**Calcite** and **JDBC** backends can add parallel contributors later without a second bootstrap pass.

**Planning reference:** `.cursor/plans/Flow UI facets story-c1560ccd.plan.md` (local); execution
tracker is this file. Deferred placeholder: **WI-139** in
`docs/workitems/completed/20260401-metadata-and-ui-improve-and-clean/`.

## Work Items

- [ ] WI-146 — Source catalog contract + flow descriptor `MetadataSource` (`WI-146-flow-descriptor-metadata-source.md`)
- [ ] WI-147 — Flow facet type definitions in platform bootstrap (`WI-147-flow-facet-types-bootstrap.md`)
- [ ] WI-148 — Autoconfigure wiring + schema facet tests (`WI-148-flow-facets-autoconfigure-and-tests.md`)
- [ ] WI-149 — UI facet ordering and validation (`WI-149-flow-facets-ui-validation.md`)
