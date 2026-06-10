# WI-147 — Flow facet type definitions (dedicated seed file)

**Story:** flow-source-ui-facets  
**Status:** `planned`  
**Type:** `feat`  
**Area:** `metadata`

**Precedes (for acceptance):** **WI-146** / **WI-148** tests that need real **`flow-*`** facet types in the registry or **`contentSchema`** validation should run only after this WI’s YAML is on the classpath (see **[`STORY.md`](STORY.md)** — suggested execution order). Review copy: **[`review/platform-flow-facet-types.yaml`](review/platform-flow-facet-types.yaml)**.

## Goal

Add **`FacetTypeDefinition`** documents for the **flow** facet family (**`category: flow`**, binding
names **`flow-schema`**, **`flow-table`**, **`flow-column`**) in a **separate** seed resource
**`metadata/platform-flow-facet-types.yaml`** under **`mill-metadata-core`**, **not** in
`platform-bootstrap.yaml`. Wire **`mill.metadata.seed.resources`** (e.g. **`mill-service`**
`application.yml`) so this file is loaded **after** `platform-bootstrap.yaml`.

**References:** [`SPEC.md`](SPEC.md) §4 (URN review), **[Appendix A](SPEC.md#appendix-a-platform-flow-facet-typesyaml-draft)** (seed YAML), **[Appendix B](SPEC.md#appendix-b--example-inferred-payloads-per-facet-type)** (example payloads for tests and UI review).

## In scope

1. Create **`metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml`**
   matching the appendix draft (adjust `contentSchema` if import/validation requires).
2. Append classpath entry to **`apps/mill-service`** (and any other default seed lists used in CI)
   after bootstrap.
3. Facet URNs: **`urn:mill/metadata/facet-type:flow-schema`**, **`:flow-table`**, **`:flow-column`**
   with **`applicableTo`** schema / table / attribute entity types.

## Out of scope

- Runtime emission (**WI-146**); OpenAPI changes unless discovery breaks.

## Acceptance criteria

- Seed ledger / import succeeds; facet types visible to UI registry.
- Representative payloads match **[SPEC Appendix B](SPEC.md#appendix-b--example-inferred-payloads-per-facet-type)** closely enough that **WI-146** / **WI-148** can assert validation against **`contentSchema`**.
