# WI-343 — L2 plan sketches and scope assessment

| Field | Value |
|--------|--------|
| **Story** | [`dqm-metadata-facets`](STORY.md) |
| **Status** | `done` |
| **Type** | `docs` |
| **Area** | `metadata`, `data-quality` |
| **Depends on** | [**WI-342**](WI-342-dq-facet-design-l1.md) |
| **Enables** | [**WI-344**](WI-344-dq-facet-seed-types.md) |

## Problem

L2 rules include multi-column predicates, aggregates across tables, and SLA checks. Without documented
plan sketches, assignment-target binding, and an honest single-plan feasibility assessment, seeds in
[`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml) may encode rules outside story scope.

## Goal

Complete [`docs/design/metadata/dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md) with:

1. **All 6 L2 facet types** — schemas, plan sketches, feasibility notes.
2. **Catalog → facet mapping** — three consistency catalog rows → `dq-predicate`.
3. **Cross-table plan patterns** and **scope assessment** table (all 16 types).
4. **L3 out-of-scope** section.

## Delivered

- Design doc § **L2 facet types** — cross-table patterns, predicate dialect, six type sketches.
- Design doc § **Scope assessment (all 16 facet types)** and § **L3 out of scope**.
- Design doc § **Catalog cross-reference (L2)**.
- [`dq-rules.md`](../../../design/data/dq-rules.md) — L2 catalog → facet type table.

## Out of scope

- Seed YAML (WI-344)
- Backend, mill-ui

## Acceptance criteria

- [x] `dq-predicate` + 3 catalog examples documented
- [x] All **6** L2 types with sketches and feasibility
- [x] Assessment table covers **16** facet types
- [x] L3 out-of-scope section
- [x] `dq-rules.md` updated (L2 + mapping)

## Suggested commit

`[docs] WI-343: DQ L2 relational plan sketches and scope assessment`
