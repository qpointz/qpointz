# Story: metadata-and-ui-improve-and-clean

**Branch:** `fix/metadata-and-ui-improve-and-clean`

**Goal:** Clean up the metadata module: remove dead code, demote legacy facet classes to plain data, introduce **multi-origin** read facets (`MetadataSource`, **`FacetOrigin`**, merge, **`model`** root), expose resolved facets in APIs and UI, and close with design + public documentation.

## Normative spec

**[`SPEC.md`](SPEC.md)** — contracts, merge rules, WI order, implementation standards (§0), and process (tracking + commits per WI).

## Context

The codebase mixed a generic **`FacetInstance`** pipeline with Kotlin **facet classes** carrying `merge()` / `validate()` / `setOwner()`, which duplicated shapes and semantics.

- Facet elimination narrative: [`docs/design/metadata/facet-class-elimination.md`](../../design/metadata/facet-class-elimination.md)
- Layered sources / inferred facets: [`docs/design/metadata/metadata-layered-sources-and-ephemeral-facets.md`](../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md)

**Historical design checklist:** [`DESIGN-GAPS.md`](DESIGN-GAPS.md) — decisions are folded into **`SPEC.md`** (especially §3h, §3i). Use **SPEC** for planning.

## Completed outside this checklist

- **WI-131** (explorer shell + bootstrap copy) — see **SPEC §4**.

## Work items — implement in SPEC §6 order

Check boxes as each WI lands; **one commit per WI** with checklist update (see **SPEC §0**, [`RULES.md`](../RULES.md)).

1. [ ] [`WI-132`](WI-132-metadata-source-contract-and-repository-adapter.md) — Metadata contracts + `RepositoryMetadataSource`
2. [ ] [`WI-137`](WI-137-model-root-entity.md) — `model` root entity
3. [ ] [`WI-133`](WI-133-read-path-facet-merge.md) — Read-path facet merge
4. [ ] [`WI-138`](WI-138-backend-logical-layout-inferred-facets.md) — Logical-layout inferred `MetadataSource`
5. [ ] [`WI-134`](WI-134-resolved-facets-read-api-and-openapi.md) — Resolved read API + OpenAPI
6. [ ] [`WI-135`](WI-135-mutation-guards-for-ephemeral-facets.md) — Mutation guards
7. [ ] [`WI-136`](WI-136-ui-full-facet-constellation-view.md) — UI full constellation
8. [ ] [`WI-140`](WI-140-facet-class-demotion.md) — Facet class demotion (SPEC §2)
9. [ ] [`WI-130`](WI-130-remove-dead-code.md) — Dead code removal (final sweep)
10. [ ] [`WI-141`](WI-141-story-documentation-closure.md) — `docs/design/` + `docs/public/` closure

**Deferred (follow-up story — intentionally no checkbox on this branch):**

- [`WI-139`](WI-139-flow-physical-source-inferred-facets.md) — Physical / flow inferred facets — tracked in a **future** story’s **`STORY.md`**; **do not** mark `[x]` here when closing **`metadata-and-ui-improve-and-clean`**.
