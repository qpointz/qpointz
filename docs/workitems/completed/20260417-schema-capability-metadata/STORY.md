# Schema capability — platform facet reconciliation and AI v3 cleanup

**Closed:** **2026-04-17**.

**Milestone:** **0.8.0** (draft)

Align **AI v3 schema exploration tools** (`SchemaCapability`, `SchemaCatalogPort`, `capabilities/schema.yaml`) with **platform facet types** (`platform-bootstrap.yaml`, `platform-standard-facet-types.md`). **Implementation belongs in `data/` and `metadata/`** (`SchemaFacetService`, `SchemaFacets`, domain/serde); **`mill-ai-v3*` stays a thin adapter** over the unified **physical schema + facet-enriched** view. **Remove redundant** metadata/schema logic from **`ai/mill-ai-v3*`** once shared code is authoritative.

**Findings (analysis):** [`FINDINGS-facet-reconciliation.md`](FINDINGS-facet-reconciliation.md)

**Integration tests:** [**WI-190**](WI-190-schema-capability-integration-tests.md) — see [`SchemaFacetCatalogSkymillCanonicalIT`](../../../../ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/schema/it/SchemaFacetCatalogSkymillCanonicalIT.kt) in **`mill-ai-v3-data`** (flow backend + `skymill-canonical` / `skymill-extras-seed` YAML), aligned with [`apps/mill-service/application.yml`](../../../../apps/mill-service/application.yml) **`skymill`**.

## Actionable items (from gaps)

**Normative principle:** [`FacetTypeDefinition`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/FacetTypeDefinition.kt) from [`platform-bootstrap.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) (and [`platform-flow-facet-types.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml) where used) is the **source of truth** for **`contentSchema`** and **`applicableTo`**; implementation **aligns** to it (**WI-188** fixes code; rare exceptions documented by URN in **WI-187**).

| ID | Actionable item | Owner | Done when |
|:---|:---|:---|:---|
| A-1 | Publish design note: per–facet-type table `contentSchema` ↔ domain / REST / AI tool fields; cite URNs | WI-187 | [`schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md) |
| A-2 | Document `relation` / `relation-source` / `relation-target` wire shape vs `RelationFacet` (or mappers) | WI-187 | Same doc § relation |
| A-3 | Document `descriptive` seed fields (`title`, …) ↔ `DescriptiveFacet` + serde / migration | WI-187 | Same doc § descriptive (`@JsonAlias("title")`) |
| A-4 | Decide whether `list_*` exposes `ai-column-value-mapping*` (justify with `applicableTo` + product) | WI-187 decision; WI-189 if yes | Recorded in design note + tools |
| A-5 | Implement `SchemaFacets` / merge / serde so payloads match respective facet types | WI-188 | Tests green in data/metadata modules |
| A-6 | Optional: extract shared projection helpers for `SchemaFacetService` consumers | WI-188 | If adopted, used by REST and/or AI |
| A-7 | Decide mandatory shared mappers for `SchemaExplorerService` + `SchemaFacetCatalogAdapter` vs best-effort alignment | WI-188 / WI-189 | Call made in MR + doc note |
| A-8 | Align `SchemaCatalogPort`, adapter, `schema.yaml` to data-layer output | WI-189 | Matches WI-187 contract |
| A-9 | Confirm legacy DB payload policy (no migration vs follow-up migration WI) | WI-187 or story closure | Stated in design note or MR |
| A-10 | Add Skymill `testIT` (`skymill` profile like mill-service); assertions from `skymill-canonical.yaml` + `skymill-extras-seed.yaml` | WI-190 | CI runs testIT; anchored to YAML |
| A-11 | Pick **one** primary module for main Skymill schema IT (`mill-ai-v3-data` vs `mill-data-schema-core`) | WI-190 | **`mill-ai-v3-data`** — `SchemaFacetCatalogSkymillCanonicalIT` |
| A-12 | Audit and delete redundant `mill-ai-v3*` metadata/schema duplication | WI-191 | Net cleanup + tests green |
| A-13 | Cross-link MRs with **WI-204–WI-206** (facet catalog story) to avoid duplicate scope | Any WI touching catalog/tools | MR description |
| — | File-repository-only metadata coverage | Backlog / out of scope | Not this story |
| P-1 | On **first** completed WI: move story folder `planned/` → `in-progress/` | Process | Done; story archived under `completed/20260417-schema-capability-metadata/` |

## Work Items

- [x] WI-187 — Normative contract: **facet types (`FacetTypeDefinition` / seeds) as source of truth**; map domain + tools to `contentSchema` (`WI-187-normative-schema-tool-contract-and-facet-decisions.md`)
- [x] WI-188 — Data/metadata: facet taxonomy, binding, and serde (`WI-188-data-layer-facet-taxonomy-schema-facets.md`)
- [x] WI-189 — AI v3: `SchemaCatalogPort`, adapter, and `schema.yaml` alignment (`WI-189-ai-v3-schema-catalog-thin-layer.md`)
- [x] WI-190 — Tests: Skymill `testIT` (mill-service `skymill`-style setup) + golden regression (`WI-190-schema-capability-integration-tests.md`)
- [x] WI-191 — Delete redundant `mill-ai-v3*` implementation (`WI-191-cleanup-redundant-ai-v3-metadata-code.md`) — URN literals centralized in `MetadataUrns`; further duplication audit optional

## Related

- [`../../planned/ai-v3/WI-066-ai-v3-schema-exploration-scenarios.md`](../../planned/ai-v3/WI-066-ai-v3-schema-exploration-scenarios.md) (scenarios)
- [`../../../design/data/schema-facet-service.md`](../../../design/data/schema-facet-service.md)
- [`../../../design/metadata/platform-standard-facet-types.md`](../../../design/metadata/platform-standard-facet-types.md)
