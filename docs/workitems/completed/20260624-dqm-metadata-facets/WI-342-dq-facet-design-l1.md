# WI-342 — DQ facet design contract and L1 plan sketches

| Field | Value |
|--------|--------|
| **Story** | [`dqm-metadata-facets`](STORY.md) |
| **Status** | `done` |
| **Type** | `docs` |
| **Area** | `metadata`, `data-quality` |
| **Depends on** | [`docs/design/data/dq-rules.md`](../../../design/data/dq-rules.md) (L1 catalog rows) |
| **Enables** | [**WI-343**](WI-343-dq-facet-design-l2.md), [**WI-344**](WI-344-dq-facet-seed-types.md) |

## Problem

The DQ rule catalog ([`dq-rules.md`](../../../design/data/dq-rules.md)) lists L1/L2 checks but metadata
has **no facet types** to document them. Without a normative contract, assignment-target binding rules,
and per-type relational-plan sketches, a future execution engine would invent ad hoc shapes.

## Goal

Create [`docs/design/metadata/dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md) with:

1. **Shared contract** — category, cardinality, common payload, binding, tags, seed file layout.
2. **Relational plan pseudo-language** — sketch template (not executable SQL).
3. **All 10 L1 facet types** — `contentSchema` fields, `applicableTo`, plan sketch, feasibility note.
4. **L2 placeholder** — pointer to WI-343 (do not document L2 types here beyond a stub section).

## Deliver

### 1. Design doc — introduction and contract

**File:** [`docs/design/metadata/dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md) (new)

**Purpose and scope**

- Metadata capture for DQ rules expressible as a **single relational plan**.
- Execution engine: future story (**M-16**); this doc defines types and plan sketches only.
- URN pattern: `urn:mill/metadata/facet-type:dq-<slug>`.
- Category: **`data-quality`**; cardinality: **`MULTIPLE`**.

**Common payload fields** (every type’s `contentSchema`):

| Field | Schema | Notes |
|-------|--------|-------|
| `name` | STRING, required | Operator label for this rule instance |
| `description` | STRING, optional | Narrative intent |
| `severity` | ENUM `info` \| `warning` \| `error` | Default `error` |
| `enabled` | BOOLEAN | Omitted ⇒ **enabled** at execution time (M-16); explicit `false` disables |
| `tags` | ARRAY of STRING, optional | Copy `descriptive` facet field + `stereotype: tags` from [`platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml); free-form instance labels |

**Physical binding (assignment target)**

Resolved from **`FacetAssignment.entityId`** — not from `column` / `table` structural facets:

| `applicableTo` | Binding |
|----------------|---------|
| `attribute` | Assigned attribute entity → column under test (URN → catalog path) |
| `table` | Assigned table entity → primary scan/join anchor on that side |
| `schema` | Full referential rule (`dq-referential-integrity`) — both ends from payload; schema entity is documentation anchor |

Partner tables/columns: explicit payload fields (relation-aligned shapes for referential types).

**Platform seeds (document only — YAML in WI-344)**

- L1: [`platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml) — 10 types
- L2: [`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml) — 6 types (WI-343)

**Alignment:** [`facet-type-descriptor-formats.md`](../../../design/metadata/facet-type-descriptor-formats.md), [`facet-payload-schema-reference.md`](../../../design/metadata/facet-payload-schema-reference.md), [`facet-class-elimination.md`](../../../design/metadata/facet-class-elimination.md).

### 2. Relational plan pseudo-language

Fixed sketch template:

```text
target: <attribute|table|schema>
parameters: { ... rule-specific payload fields ... }
plan:
  scan: $entityTable
  [join: $otherTable ON ...]   # optional
  filter: <predicate>
  aggregate: violation_count = COUNT(*) | metric = <expr>
pass: violation_count = 0 | <comparison on metric>
bindings:
  $entityTable ← assignment target (table URN → catalog path)
  $columnRef ← assignment target (attribute URN → catalog path); column types only
  $otherTable ← explicit payload reference (cross-entity rules)
notes: <feasibility: in | borderline + conditions>
```

Rules: single plan tree; pseudo predicates (not raw SQL); explicit pass criteria.

### 3. Referential integrity — relation facet triple (L1)

**Reference:** `relation`, `relation-source`, `relation-target` in [`platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml).

| DQ type | Relation analogue | `applicableTo` | Payload ( + common fields ) | Binding |
|---------|-------------------|----------------|-----------------------------|---------|
| `dq-referential-integrity` | `relation` | schema | `source`, `target`, optional `joinSql` | Both ends in payload; optional column lists |
| `dq-referential-source` | `relation-source` | table | `sourceColumns?`, `target`, optional `joinSql` | Source table = assignment target |
| `dq-referential-target` | `relation-target` | table | `source`, `targetColumns?`, optional `joinSql` | Target table = assignment target |

Reuse `source` / `target` / `sourceColumns` / `targetColumns` shapes from relation seeds. **Column lists optional** on all three types; optional **`joinSql`** join expression with the same semantics (paired columns and/or expression; expression wins when both present).

**Plan sketch (all three):** orphan join — `LEFT JOIN` referenced side; `violation_count` = rows with missing parent; `pass: violation_count = 0`. Feasibility: **in**.

### 4. Other L1 facet types — schemas and sketches

| URN slug | Rule-specific fields (draft) | Feasibility |
|----------|-------------------------------|-------------|
| `dq-null-check` | (common only) | **in** |
| `dq-empty-value-check` | `trimWhitespace` (BOOLEAN, default true) | **in** |
| `dq-unique-value-check` | (common only) | **in** |
| `dq-allowed-values-check` | `allowedValues` (ARRAY STRING, required) | **in** |
| `dq-pattern-check` | `pattern` (STRING), `patternDialect` (ENUM: `sql_like`, `sql_regex`; default `sql_like`) | **in** |
| `dq-min-max-check` | `min`, `max` (NUMBER or STRING; ≥1 required) | **in** |
| `dq-data-age-check` | `timestampColumn` (STRING), `maxAge` (ISO-8601 duration STRING), `expectedScheduleCron` (STRING, optional), `expectedScheduleTimezone` (IANA STRING, optional) | **in** |

Each type: full sketch block + field-level description notes for WI-344.

### 5. Cross-links

- [`dq-rules.md`](../../../design/data/dq-rules.md) L1 section → design doc.
- Design doc → [`STORY.md`](STORY.md), WI-343 (L2).

## Out of scope

- L2 type definitions (WI-343)
- Seed YAML files (WI-344)
- Backend, tests, mill-ui

## Acceptance criteria

- [x] Design doc exists with contract + pseudo-language
- [x] All **10** L1 types documented (7 column/table checks + referential triple + data-age)
- [x] Referential payloads aligned with relation facet shapes
- [x] Assignment-target binding documented; no structural-facet binding
- [x] Common `tags` field documented (`stereotype: tags`)
- [x] L2 stub points to WI-343
- [x] `dq-rules.md` links to design doc (L1)

## Suggested commit

`[docs] WI-342: DQ facet contract and L1 relational plan sketches`
