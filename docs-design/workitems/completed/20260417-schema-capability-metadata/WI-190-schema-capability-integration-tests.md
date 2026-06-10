# WI-190 — Integration tests: schema tools vs platform facets

Status: `planned`  
Type: `🧪 test`  
Area: `data`, `ai`  
Milestone: `0.8.0`

## Problem Statement

Facet payload shapes from seeds and repositories need **regression protection** end-to-end: **`SchemaFacetService`** → **adapter** → **`SchemaCatalogPort`** tool results. Unit tests alone may miss wiring across modules.

## Goal

Add **integration** tests that prove the **full stack** — physical Skymill model + seeded metadata — resolves correctly through **`SchemaFacetService`** and into **`SchemaCatalogPort`** tool results.

### Skymill profile alignment (required)

- Use a Spring **`testIT`** setup **aligned with** [`apps/mill-service/application.yml`](../../../../apps/mill-service/application.yml) **`skymill`** profile: **flow** backend pointing at Skymill parquet under `test/datasets/skymill/`, **JPA** metadata repository, **seed** resources including **`classpath:metadata/platform-bootstrap.yaml`**, **`classpath:metadata/platform-flow-facet-types.yaml`**, and the **Skymill canonical + extras** files:
  - `file:../../test/datasets/skymill/skymill-canonical.yaml`
  - `file:../../test/datasets/skymill/skymill-extras-seed.yaml`
- Reuse the same **property shape** as existing IT slices (e.g. [`SqlValidatorSkymillFlowItApplication`](../../../../ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/sql/it/SqlValidatorSkymillFlowItApplication.kt) + `src/testIT/resources/application.yml`): mirror **`mill.*`** / **`spring.*`** layout from **`apps/mill-service/application.yml`** `skymill` so behaviour matches the reference app.

### Stable contract sources (required)

Author **assertions** from the same files the app loads — do not duplicate facet text or relation structure in test code without anchoring to these documents:

| File | Role |
|------|------|
| [`test/datasets/skymill/skymill-canonical.yaml`](../../../../test/datasets/skymill/skymill-canonical.yaml) | **Primary** stable contracts: entities, facet assignments, and payloads as checked into the repo (baseline metadata + physical binding). |
| [`test/datasets/skymill/skymill-extras-seed.yaml`](../../../../test/datasets/skymill/skymill-extras-seed.yaml) | **Extras** (e.g. additional facets, value-mapping / rich examples): use for assertions on descriptions, relations, or other facets **present in this file**. |

When a seed line changes, **tests should fail** if tool output no longer reflects the **facet types** and **`contentSchema`** expressed there (per **WI-187**). Prefer referencing **entity URNs**, **schema/table/column** coordinates, and **substring** or **structured** checks that track the YAML, not unrelated magic strings.

### Assertions (minimum)

1. **Physical model** — schemas/tables/columns from **`SchemaProvider`** / flow match expectations **derived from** the Skymill dataset and entity coverage in **`skymill-canonical.yaml`** (and **`skymill-extras-seed.yaml`** where those entities extend the model).
2. **Descriptions** — **`descriptive`** facet text **as defined** in the stable YAML files appears on **`list_schemas` / `list_tables` / `list_columns`** for the matching entities.
3. **Relations** — **`list_relations`** matches **relation** (and related) facet data **as authored** in **`skymill-canonical.yaml`** / **`skymill-extras-seed.yaml`** for the chosen tables, per **WI-187** facet-type alignment.

Add **unit** or lighter tests only where IT is too heavy; **Skymill-backed IT** is the **acceptance** bar for this WI.

Prefer placement in **`ai/mill-ai-v3-data`** `testIT` (already has Skymill flow patterns) and/or **`data/mill-data-schema-core`** `testIT` — follow existing suite patterns.

## In Scope

- Test code + minimal fixtures; no product behavior change unless a test exposes a **bug** fixed in the same WI (keep fixes small).

## Out of Scope

- Full UI or REST contract tests (unless one shared assertion helper is trivial).

## Acceptance Criteria

- At least one **`testIT`** class runs with **Skymill** data and **mill-service `skymill`-equivalent** config (flow + seeds as above) and asserts **physical + descriptive + relations** resolution.
- New tests run in CI for the chosen module(s) (`./gradlew … test` / `testIT` as configured).
- Tests fail if **WI-188**/**WI-189** regressions reintroduce drift between platform facets and tool output.

## Depends On

- **WI-188**, **WI-189** (behavior stable enough to assert).
