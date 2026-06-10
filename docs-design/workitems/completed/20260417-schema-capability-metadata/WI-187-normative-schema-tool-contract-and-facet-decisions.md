# WI-187 — Normative schema tool contract and facet decisions

Status: `planned`  
Type: `📝 docs` / `📐 design`  
Area: `metadata`, `data`, `ai`  
Milestone: `0.8.0`

## Source of truth (normative)

**Facet types** are authoritative:

- **`FacetTypeDefinition`** documents loaded from seeds — primarily [`platform-bootstrap.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) and, where listed in [`mill.metadata.seed.resources`](../../../design/metadata/mill-metadata-domain-model.md), [`platform-flow-facet-types.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml).
- Each type’s **`contentSchema`** (field names, types, required flags) and **`applicableTo`** (entity kinds) define the **wire contract** for facet instance payloads.

This WI **does not** invent alternate JSON shapes. It documents how **runtime** (Kotlin facet types, `SchemaFacets`, REST DTOs, AI **`SchemaCatalogPort`** tool fields) **align** to the **respective** `FacetTypeDefinition` for `descriptive`, `relation`, `relation-source`, `relation-target`, `column`, `table`, `schema`, and any other types in scope for schema exploration — and flags gaps where code must change in **WI-188** to match the seed.

## Problem Statement

Schema exploration tools and Kotlin bindings have **drifted** from some **`contentSchema`** definitions (see [`FINDINGS-facet-reconciliation.md`](FINDINGS-facet-reconciliation.md)). Reconciliation means **bringing code and tools in line with facet types**, not redefining platform types outside the seed/catalog.

## Goal

Produce a **short design note** (under `docs/design/metadata/` or `docs/design/data/`) that:

1. For each **in-scope facet type** (by URN / slug), tabulates **`contentSchema`** (from seed) ↔ **current Kotlin / tool field** mapping; where they differ, specifies **target alignment** to the facet type (or an explicit, documented exception with URN reference — exceptions should be rare).
2. States **`descriptive`** alignment: seed field names (`title`, …) ↔ **`DescriptiveFacet`** / serde (aliases, migration policy for old rows if needed).
3. States **`relation` / `relation-source` / `relation-target`** alignment: nested vs flat shapes **as required by each type’s `contentSchema`** ↔ `RelationFacet` or dedicated mappers.
4. Maps which **facet types** participate in **schema exploration** surfaces (`SchemaFacetService`, REST, AI `list_*`) based on **`applicableTo`** + product rules — **ai-column-value-mapping*** inclusion follows the same principle (attribute-scoped types vs table/schema listing).
5. Cross-links [`FINDINGS-facet-reconciliation.md`](FINDINGS-facet-reconciliation.md) and defers **code** to **WI-188** / **WI-189**.

## In Scope

- Authoritative prose + diagrams as needed; **no production code** in this WI (docs only).

## Out of Scope

- Code changes (later WIs).
- Redefining **`contentSchema`** in seeds **unless** a separate metadata story explicitly updates `platform-bootstrap.yaml` (this WI assumes seeds stay the contract unless changed elsewhere).

## Acceptance Criteria

- Design doc merged; **facet types / `contentSchema`** cited as the baseline for each decision.
- **`STORY.md`** open gaps under “Normative decisions” can be checked off or narrowed once this WI lands.

## Deliverables

- New or updated markdown under `docs/design/` (component-appropriate folder per [`RULES.md`](../../RULES.md)).
