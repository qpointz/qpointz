# WI-185 — Value mapping metadata seeds for Skymill & Moneta test datasets

| Field | Value |
|--------|--------|
| **Story** | [`value-mapping-facets-vector-lifecycle`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` / `docs` |
| **Area** | `metadata`, `test` |
| **Depends on** | [**WI-181**](WI-181-value-mapping-facet-types.md) — **`FacetTypeDefinition`** for **`ai-column-value-mapping`** (and optional **`ai-column-value-mapping-values`**) must exist in **`metadata/mill-metadata-core/.../platform-bootstrap.yaml`** before these seeds load. |

## Problem

Synthetic test models **Skymill** ([`test/skymill.yaml`](../../../../test/skymill.yaml), [`test/datasets/skymill/`](../../../../test/datasets/skymill/)) and **Moneta** ([`test/moneta.yaml`](../../../../test/moneta.yaml), [`test/datasets/moneta/`](../../../../test/datasets/moneta/)) ship canonical metadata ([`skymill-meta-seed-canonical.yaml`](../../../../test/datasets/skymill/skymill-meta-seed-canonical.yaml) etc.) but **no** attachments for **value-mapping** facet types. Developers need **ready-made seeds** to exercise **`ValueSource`**, refresh, and resolver flows without hand-authoring facets for every column.

## Goal

1. **Document column shortlist** — Which attributes are **good value-mapping candidates** (low-cardinality or label↔code semantics, NL-friendly).
2. **Ship optional seed files** (same multi-document **`MetadataEntity`** style as **`skymill-meta-seed-canonical.yaml`**):
   - [`test/datasets/skymill/skymill-extras-seed.yaml`](../../../../test/datasets/skymill/skymill-extras-seed.yaml)
   - [`test/datasets/moneta/moneta-extras-seed.yaml`](../../../../test/datasets/moneta/moneta-extras-seed.yaml)
3. **Loader wiring (documentation)** — How to merge extras with canonical seeds (e.g. **`mill.metadata.seed.resources[n]`** after **`platform-bootstrap`** + canonical file). Authors wire seeds in their **own test bench**; no requirement for an in-repo integration test in this WI.

## Column rationale (from dataset definitions)

### Skymill (`schema: skymill`)

| Attribute URN | Why |
|-----------------|-----|
| **`skymill.cities.state`** | US **state** names; aligns with [`ChromaSkymillDistinctVectorIT`](../../../../ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt) (`SELECT DISTINCT state`). Strong NL ↔ stored label mapping. |
| **`skymill.passenger.loyalty_tier`** | Small enum: `basic` / `silver` / `gold` / `platinum`. |
| **`skymill.ticket_prices.travel_class`** | `economy` / `business` / `first`. |
| **`skymill.ticket_prices.currency`** | `USD` / `EUR` / `CHF`. |
| **`skymill.delays.reason`** | `weather` / `technical` / `crew` / `other`. |

*Not in initial seed (optional follow-up):* `cancellations.cancellation_reason`, `cargo_clients.region`, `countries.iso_code` (pairs with country name table), `aircraft_types` labels via FK.

### Moneta (`schema: moneta`)

| Attribute URN | Why |
|-----------------|-----|
| **`moneta.clients.country`** | Faker **country** names — same intent as commented **`value-mappings`** in [`moneta-meta.yaml`](../../../../test/datasets/moneta/moneta-meta.yaml) (NL country ↔ stored string). |
| **`moneta.clients.segment`** | `REGULAR` / `WEALTH` / `ULTRA`. |
| **`moneta.accounts.account_type`** | `checking` / `savings`. |
| **`moneta.transactions.transaction_type`** | `deposit` / `withdrawal` / `transfer`. |
| **`moneta.loans.status`** | `active` / `closed` / `defaulted`. |
| **`moneta.stocks.exchange`** | `NYSE` / `NASDAQ` / `LSE` / `HKEX`. |
| **`moneta.trade_orders.status`** | `pending` / `executed` / `cancelled`. |

## Non-goals

- Replacing **`skymill-meta-seed-canonical.yaml`** or **synthetic data generator** output — value-mapping seeds are **additive** files.
- Production tenant metadata — **test datasets only**.
- **Qsynth / [`test/skymill.yaml`](../../../../test/skymill.yaml) / [`test/moneta.yaml`](../../../../test/moneta.yaml) alignment** — extras are **not** required to track regenerated canonical YAML or synthetic data; they are optional hand-maintained snippets for local and bench use.
- **Repository integration tests** that import these files — validation is **out of band** (author’s test bench and `mill.metadata.seed.resources`).

## Acceptance criteria

- Both YAML files are valid multi-doc metadata seeds using **`urn:mill/metadata/facet-type:ai-column-value-mapping`** with payloads matching [**WI-181**](WI-181-value-mapping-facet-types.md) Appendix ( **`context`**, **`similarityThreshold`**, **`nullValues`**, **`data`** ) once **WI-181** registers **`FacetTypeDefinition`** in **`platform-bootstrap.yaml`**.
- **`STORY.md`** references extras under § *Gaps* (closed); **dependency:** **WI-181** **`FacetTypeDefinition`** in **`platform-bootstrap.yaml`** before seeds reference those facet URNs (story § *Dependency hints* — overall WI order is **flexible**).
- This WI documents merge order (e.g. `mill.metadata.seed.resources[n]=file:.../skymill-extras-seed.yaml`) for bench use; **no** mandatory Gradle **`testIT`** in this repo for these files.

## Deliverables

- [`skymill-extras-seed.yaml`](../../../../test/datasets/skymill/skymill-extras-seed.yaml)
- [`moneta-extras-seed.yaml`](../../../../test/datasets/moneta/moneta-extras-seed.yaml)
- This work item file.
