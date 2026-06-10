# Review: `platform-flow-facet-types.yaml`

This folder holds a **review copy** of the flow facet type seed **before** it lives on the classpath.

- **File:** [`platform-flow-facet-types.yaml`](platform-flow-facet-types.yaml) — three `FacetTypeDefinition` documents (`flow-schema`, `flow-table`, `flow-column`).
- **Normative spec:** [`../SPEC.md`](../SPEC.md) — §4, Appendix A (same content), Appendix B (example payloads).
- **Target install path (after review):** `metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml`
- **Seeding:** append to `mill.metadata.seed.resources` **after** `classpath:metadata/platform-bootstrap.yaml` (e.g. in `apps/mill-service`).

**Grammar:** `contentSchema` keys must match what `platform-bootstrap` facet types use (`type`, `fields`, `items`, `required`, etc.).

**Tests:** validate import against metadata seed pipeline; unit tests can load this resource from the story path only if you add a test fixture — prefer copying to `mill-metadata-core` test resources once stabilised.

### Integration / manual scenarios

- **Skymill datasets** under `test/datasets/skymill/` (repo) — flow metadata should reflect the configured source(s).
- **Mixing formats example:** `examples/06_MixingFormats` — single schema (`skymill`) mixing CSV, Parquet, Excel readers; useful to confirm **`flow-table.tableInputs`** lists multiple formats.
