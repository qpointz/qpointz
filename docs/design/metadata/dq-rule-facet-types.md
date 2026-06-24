# Data quality — metadata facet types (L1/L2)

**Status:** L1/L2 contract, plan sketches, and **platform seeds** complete (**WI-342**, **WI-343**, **WI-344**)  
**Last updated:** 2026-06-24  
**Story:** [`dqm-metadata-facets`](../../workitems/in-progress/dqm-metadata-facets/STORY.md)  
**Rule catalog:** [`dq-rules.md`](../data/dq-rules.md)  
**Related:** [`platform-standard-facet-types.md`](platform-standard-facet-types.md), [`facet-payload-schema-reference.md`](facet-payload-schema-reference.md), [`facet-class-elimination.md`](facet-class-elimination.md), [**Relational plan sketches (pseudo SQL)**](dq-rule-relplan-sketches.md)

---

## Purpose

Define **metadata facet types** in category **`data-quality`** so operators can document data quality (DQ)
rules on model entities. Each rule that can be evaluated as a **single relational plan** (one scan tree,
one aggregate result) gets a facet type with a documented plan sketch.

**This document does not implement execution.** A future engine (**BACKLOG M-16**) will compile sketches
to SQL or Rel plans. Here we fix **payload shapes**, **assignment binding**, and **feasibility**.

**Scope:** L1 and L2 rules from [`dq-rules.md`](../data/dq-rules.md) that fit single-plan evaluation.
L3 (anomaly, drift, fuzzy duplicate, cross-system, lifecycle) is **out of scope**.

---

## Facet inventory summary

| Level | Seed file | Count | Notes |
|-------|-----------|-------|-------|
| L1 | [`platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml) | **10** | This document § L1 types |
| L2 | [`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml) | **5** | This document § L2 types |

**Total:** 15 facet types. Four catalog L2 rule names map to one type: **`dq-predicate`** (see WI-343).

URN pattern: `urn:mill/metadata/facet-type:dq-<slug>`.

---

## Shared contract

### Category and cardinality

- **`category`:** `data-quality` on every seed row.
- **`targetCardinality`:** `MULTIPLE` — many rule instances per entity.
- **`mandatory`:** `false` for all DQ types in v1.

### Common payload fields

Every facet type’s `contentSchema` includes these fields (rule-specific fields are additive):

**Seed field order (facet UI):** `name`, `description`, then **rule-specific fields**, then **`severity`**, **`profile`**, **`enabled`**, **`tags`** last so operator controls appear at the bottom of the form.

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `name` | STRING | yes | Short operator label for this rule instance (e.g. `customer_id_not_null`). |
| `description` | STRING | no | Narrative intent, business context, or remediation notes. |
| `severity` | ENUM: `info`, `warning`, `error` | no | Reporting severity when the rule fails. **Without `profile`:** any violation (`violation_count > 0`) reports at this level (default **`error`**). **With single-threshold `profile`:** severity applies **above** the threshold (see **Threshold profile grammar**). **With two-threshold `profile`:** ignored — bands are fixed pass / warning / error. |
| `profile` | STRING | no | Optional **violation-rate bands** (percent of scanned rows that fail the rule). **`warn:error`** (e.g. `10:80`) for three fixed bands, or a **single integer** (e.g. `10`) for pass vs **`severity`**. See **Threshold profile grammar**. |
| `enabled` | BOOLEAN | no | When false, documentation-only / skip execution. Schema **`default: true`**; execution (M-16) applies schema defaults when the field is omitted. |
| `tags` | ARRAY of STRING | no | Free-form labels on the instance (e.g. `pii`, `regulatory`, `nightly`). Wire shape and **`stereotype: tags`** match [`descriptive`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) — see [`mill-ui-facet-stereotypes.md`](mill-ui-facet-stereotypes.md). No separate tag registry; filter by string equality on payloads. |

### Threshold profile grammar (`profile`)

**Violation rate** at execution time (M-16):

```text
rate = (violating_row_count / scanned_row_count) × 100
```

When `scanned_row_count = 0`, treat as **pass** (no rate to evaluate).

**Wire format** — integer percentages `0`–`100`, no spaces, no `%` suffix:

| Form | Example | `rate` outcome |
|------|---------|----------------|
| Two thresholds | `10:80` | `rate ≤ 10` → **pass**; `10 < rate ≤ 80` → **warning**; `rate > 80` → **error** (`severity` ignored) |
| Single threshold | `10` | `rate ≤ 10` → **pass**; `rate > 10` → report at **`severity`** (e.g. `warning` → warn, `error` → error) |

**Validation (metadata / M-16):**

- **Two thresholds:** `warn:error` — both integers `0`–`100`, **`warn < error`** (strict).
- **Single threshold:** one integer `0`–`100` with **no** colon.
- Reject empty strings, partial colons (`:10`, `10:`), or non-numeric tokens.

**Interaction with `severity`:**

| `profile` | `severity` role |
|-----------|-----------------|
| Omitted | Binary: `violation_count = 0` → pass; else report at **`severity`** (default `error`). |
| Single integer (e.g. `10`) | Below/equal threshold → **pass**; above threshold → **`severity`**. Use when only two outcomes are needed (tolerate small defect rates, escalate at chosen level). |
| Two thresholds (e.g. `10:80`) | Fixed **pass / warning / error** bands; **`severity` is ignored**. |

**Examples (1000-row table, 50 violating rows → rate = 5%):**

| `severity` | `profile` | Outcome |
|------------|-----------|---------|
| `error` | *(omitted)* | **Error** (any violation) |
| `warning` | `10` | **Pass** (5% ≤ 10%) |
| `warning` | `10:80` | **Pass** (5% ≤ 10%) |

**Examples (150 violating rows → rate = 15%):**

| `severity` | `profile` | Outcome |
|------------|-----------|---------|
| `warning` | `10` | **Warning** (15% > 10%) |
| `error` | `10` | **Error** (15% > 10%) |
| `warning` | `10:80` | **Warning** (10% < 15% ≤ 80%) |

### Physical binding — assignment target

Physical table/column resolution uses **`FacetAssignment.entityId`** (the entity the facet is **assigned to**).
Do **not** resolve the host column or table from `column` / `table` structural facets on the same entity.

| `applicableTo` | Host binding |
|--------------|--------------|
| `urn:mill/metadata/entity-type:attribute` | Assigned **attribute** entity → column under test (`urn:mill/model/attribute:<schema>.<table>.<column>` → catalog path). |
| `urn:mill/metadata/entity-type:table` | Assigned **table** entity → primary scan or join anchor on that side. |
| `urn:mill/metadata/entity-type:schema` | Full referential rule (`dq-referential-integrity`) — **both** source and target from payload; schema entity is the documentation anchor. |

**Partner** tables/columns (FK target, reconciliation partner) come from **explicit payload fields** only.

### Platform seeds

YAML is delivered in **WI-344**. Load order (documented for operators):

```yaml
mill.metadata.seed.resources: >-
  classpath:metadata/platform-bootstrap.yaml,
  classpath:metadata/platform-flow-facet-types.yaml,
  classpath:metadata/platform-dq-l1-facet-types.yaml,
  classpath:metadata/platform-dq-l2-facet-types.yaml
```

`application.yml` wiring is optional until runtime registration is requested.

### Manifest alignment

- Descriptor envelope: [`facet-type-descriptor-formats.md`](facet-type-descriptor-formats.md)
- Payload trees: [`facet-payload-schema-reference.md`](facet-payload-schema-reference.md)
- No Kotlin facet lifecycle classes: [`facet-class-elimination.md`](facet-class-elimination.md)

---

## Relational plan pseudo-language

Sketches are **not executable SQL**. They document how a future compiler maps a facet instance to one plan.

**Full sketches with pseudo SQL and post-processing for every L1/L2 type:** [`dq-rule-relplan-sketches.md`](dq-rule-relplan-sketches.md).

### Template

```text
urn: mill/metadata/facet-type:dq-<slug>
catalog: <rule name from dq-rules.md>
target: <attribute|table|schema>
parameters: { ... rule-specific payload fields + common fields ... }
plan:
  scan: $entityTable
  [join: $otherTable ON <join-predicate>]   # optional, repeatable
  filter: <row-predicate>
  aggregate: violation_count = COUNT(*) | metric = <expr>
pass: violation_count = 0 | <comparison on metric>
[outcome: map violation_rate via profile when set; else severity when violation_count > 0]
bindings:
  $entityTable ← FacetAssignment.entityId when applicableTo is table (or derived table for attribute rules)
  $columnRef ← FacetAssignment.entityId when applicableTo is attribute
  $otherTable ← payload (cross-entity rules)
notes: feasibility = in | borderline (<preconditions>)
```

### Rules

1. **Single plan** — one scan tree (joins allowed); one aggregate row or violation count.
2. **Pass** — explicit; default is zero violating rows.
3. **Pseudo predicates** — `IS NULL`, `NOT IN`, `BETWEEN`, `REGEX_MATCH`, `TRIM`, etc., for human review.
4. **Feasibility** — `in` = always single plan; `borderline` = only when preconditions hold (document them).

---

## L1 facet types

Catalog reference: [`dq-rules.md`](../data/dq-rules.md) § L1.

### `urn:mill/metadata/facet-type:dq-null-check`

**Catalog:** Null Check (Completeness, column)  
**`applicableTo`:** `urn:mill/metadata/entity-type:attribute`  
**Rule-specific payload:** (common fields only)

```text
target: attribute
parameters: { name, description?, severity?, enabled?, tags? }
plan:
  scan: $entityTable
  filter: $columnRef IS NULL
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings:
  $columnRef ← assignment target attribute entity
  $entityTable ← table containing $columnRef (from catalog path)
notes: feasibility = in
```

**WI-344 field hints:** No fields beyond common envelope.

---

### `urn:mill/metadata/facet-type:dq-empty-value-check`

**Catalog:** Empty Value Check  
**`applicableTo`:** attribute

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `trimWhitespace` | BOOLEAN | no | When true (default), treat whitespace-only strings as empty via `TRIM($columnRef) = ''`. |

```text
target: attribute
parameters: { trimWhitespace?, ...common }
plan:
  scan: $entityTable
  filter: TRIM($columnRef) = '' AND $columnRef IS NOT NULL
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings: $columnRef ← assignment target
notes: feasibility = in
```

---

### `urn:mill/metadata/facet-type:dq-unique-value-check`

**Catalog:** Unique Value Check  
**`applicableTo`:** attribute

```text
target: attribute
parameters: { ...common }
plan:
  scan: $entityTable
  aggregate: duplicate_groups = COUNT(*) FROM (
    GROUP BY $columnRef HAVING COUNT(*) > 1
  )
pass: duplicate_groups = 0
bindings: $columnRef ← assignment target
notes: feasibility = in — single aggregate over grouped subquery or equivalent plan node
```

---

### `urn:mill/metadata/facet-type:dq-allowed-values-check`

**Catalog:** Allowed Values Check  
**`applicableTo`:** attribute

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `allowedValues` | ARRAY of STRING | yes | Canonical domain; violating rows have values not in this set (NULL handling: document per engine — typically NULL is not in set unless listed). |

```text
target: attribute
parameters: { allowedValues, ...common }
plan:
  scan: $entityTable
  filter: $columnRef NOT IN allowedValues
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings: $columnRef ← assignment target
notes: feasibility = in
```

---

### `urn:mill/metadata/facet-type:dq-pattern-check`

**Catalog:** Pattern Check  
**`applicableTo`:** attribute

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `pattern` | STRING | yes | Pattern string per `patternDialect` (e.g. `%@%.%` for LIKE). |
| `patternDialect` | ENUM: `sql_like`, `sql_regex` | no | Schema **`default: sql_like`**. `sql_regex` uses vendor/dialect-specific regex at execution time. |

```text
target: attribute
parameters: { pattern, patternDialect?, ...common }
plan:
  scan: $entityTable
  filter: NOT SQL_PATTERN_MATCH($columnRef, pattern, patternDialect)   # LIKE or dialect regex
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings: $columnRef ← assignment target
notes: feasibility = in — sql_regex semantics resolved by execution engine / dialect (M-16)
```

---

### `urn:mill/metadata/facet-type:dq-min-max-check`

**Catalog:** Min/Max Check  
**`applicableTo`:** attribute

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `min` | NUMBER or STRING | no | Inclusive lower bound (NUMBER for numeric columns; STRING ISO date for dates). At least one of `min` / `max` required. |
| `max` | NUMBER or STRING | no | Inclusive upper bound. |

```text
target: attribute
parameters: { min?, max?, ...common }
plan:
  scan: $entityTable
  filter: $columnRef NOT BETWEEN min AND max
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings: $columnRef ← assignment target
notes: feasibility = in
```

---

### `urn:mill/metadata/facet-type:dq-data-age-check`

**Catalog:** Data Age Check (Freshness, table)  
**`applicableTo`:** `urn:mill/metadata/entity-type:table`

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `timestampColumn` | STRING | yes | Column on the host table holding last-update or ingest time (e.g. `updated_at`). |
| `maxAge` | STRING | yes | ISO-8601 duration (e.g. `PT24H`, `P1D`) — maximum allowed age relative to the **evaluation anchor** (see below). |
| `expectedScheduleCron` | STRING | no | Cron for when data is expected (Spring 6-field or Unix 5-field). Example: `0 0 6 * * MON-FRI` for weekday 06:00 arrivals. Omit for continuous / daily freshness against wall-clock `now()`. |
| `expectedScheduleTimezone` | STRING | no | IANA timezone ID for cron tick resolution (e.g. `Europe/Amsterdam`). **When omitted:** platform **local timezone**. Ignored when `expectedScheduleCron` is omitted. |

**Evaluation anchor**

| `expectedScheduleCron` | Anchor at evaluation time `t` |
|------------------------|--------------------------------|
| omitted | `now()` (astronomic / wall-clock) |
| set | **Expected date** — the latest schedule tick ≤ `t`, computed in `expectedScheduleTimezone` or platform local timezone when timezone is omitted (e.g. most recent Mon–Fri 06:00 before `t` in that zone) |

**Pass rule**

Let `anchor` be the evaluation anchor and `latest` = `MAX(timestampColumn)` over rows where
`timestampColumn < anchor` (strictly before the expected arrival instant).

- **Pass:** `latest >= anchor - maxAge` (equivalently: age of `latest` relative to `anchor` ≤ `maxAge`).
- When `expectedScheduleCron` is omitted, use all rows (or equivalently `latest` = table-wide `MAX(timestampColumn)`) and `anchor = now()`.

**Why schedule-relative:** a Mon–Fri feed should not fail all weekend because wall-clock age exceeds
`maxAge`; freshness is measured from the last **expected** arrival, not calendar time since Friday.

```text
target: table
parameters: { timestampColumn, maxAge, expectedScheduleCron?, expectedScheduleTimezone?, ...common }
plan:
  let zone = expectedScheduleTimezone ?? platformLocalTimezone()
  let anchor = expectedScheduleCron ? latestScheduleTick(expectedScheduleCron, now(), zone) : now()
  scan: $entityTable
  filter: timestampColumn < anchor   # omitted when no schedule cron
  aggregate: latest = MAX(timestampColumn)
pass: latest >= anchor - maxAge
bindings: $entityTable ← assignment target table entity
notes: feasibility = in; schedule tick resolution = M-16
```

**Example (weekday feed):** `expectedScheduleCron = 0 0 6 * * MON-FRI`, `expectedScheduleTimezone = Europe/Amsterdam`, `maxAge = PT24H`, evaluated
Wednesday 10:00 CET → anchor = Wednesday 06:00 Amsterdam → pass if the newest row with `updated_at < Wed 06:00` is
no older than 24h before Wed 06:00.

---

## Referential integrity (relation facet triple)

**Catalog:** Foreign Key Check — documented as **three** facet types mirroring join metadata facets in
[`platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml):
`relation`, `relation-source`, `relation-target`.

Reuse **`source`**, **`target`**, **`sourceColumns`**, **`targetColumns`** object shapes from relation
seeds (schema, table, optional columns arrays). Add DQ common fields to each type.

**Join semantics (all three referential types):**

| Field | `dq-referential-integrity` | `dq-referential-source` | `dq-referential-target` |
|-------|---------------------------|-------------------------|-------------------------|
| Source-side columns | `source.columns` (optional) | `sourceColumns` (optional) | `source.columns` (optional) |
| Target-side columns | `target.columns` (optional) | `target.columns` (optional) | `targetColumns` (optional) |
| Join expression | `joinSql` (optional) | `joinSql` (optional) | `joinSql` (optional) |

Provide **paired column lists** for default equi-join, and/or **`joinSql`** when lists are omitted or
insufficient. When both are present, **`joinSql` takes precedence** at compile time. At least one join
specification path must be present for execution (future **M-16**).

### `urn:mill/metadata/facet-type:dq-referential-integrity`

| | |
|--|--|
| **Relation analogue** | `relation` |
| **`applicableTo`** | `urn:mill/metadata/entity-type:schema` |
| **Payload** | `source` (OBJECT), `target` (OBJECT), optional `joinSql` (STRING) + common fields |
| **Binding** | Both ends from payload; assign on the schema entity that owns the relationship |

```text
target: schema
parameters: { source, target, joinSql?, ...common }
plan:
  scan: $sourceTable AS s
  join: LEFT JOIN $targetTable AS t ON <joinSql or equi-join on source.columns / target.columns>
  filter: t.<target-key> IS NULL AND s.<source-key> IS NOT NULL
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings:
  $sourceTable, $targetTable ← payload source/target objects
notes: feasibility = in
```

### `urn:mill/metadata/facet-type:dq-referential-source`

| | |
|--|--|
| **Relation analogue** | `relation-source` |
| **`applicableTo`** | table |
| **Payload** | `sourceColumns` (ARRAY STRING, optional), `target` (OBJECT, optional columns), `joinSql` (optional) + common |
| **Binding** | **Source table** = assignment target; target table in payload |

```text
target: table
parameters: { sourceColumns?, target, joinSql?, ...common }
plan:
  scan: $entityTable AS s
  join: LEFT JOIN $targetTable AS t ON <joinSql or equi-join on sourceColumns / target.columns>
  filter: orphan FK rows (referenced key missing)
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings:
  $entityTable ← assignment target (outbound FK host)
  $targetTable ← payload.target
notes: feasibility = in
```

### `urn:mill/metadata/facet-type:dq-referential-target`

| | |
|--|--|
| **Relation analogue** | `relation-target` |
| **`applicableTo`** | table |
| **Payload** | `source` (OBJECT, optional columns), `targetColumns` (ARRAY STRING, optional), `joinSql` (optional) + common |
| **Binding** | **Target table** = assignment target; source table in payload |

```text
target: table
parameters: { source, targetColumns?, joinSql?, ...common }
plan:
  scan: $sourceTable AS s
  join: LEFT JOIN $entityTable AS t ON <joinSql or equi-join on source.columns / targetColumns>
  filter: orphan inbound references (child points to missing parent row on host)
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings:
  $entityTable ← assignment target (referenced / parent host)
  $sourceTable ← payload.source
notes: feasibility = in
```

---

## L2 facet types

Catalog reference: [`dq-rules.md`](../data/dq-rules.md) § L2.

Three catalog consistency rules (**Cross-Column Consistency**, **Derived Value Validation**, **Conditional Completeness**) map to one facet type: **`dq-predicate`**. All other L2 catalog rows map 1:1 to a slug below.

### Cross-table plan patterns

Host table/column always from **assignment target** (`FacetAssignment.entityId`). Partner entities and join keys come from **payload** only.

| Pattern | Facet type(s) | Plan shape |
|---------|---------------|------------|
| Assignment anchor | All L1/L2 | `$entityTable` / `$columnRef` from assigned entity |
| Row predicate | `dq-predicate` | Scan host; `filter: NOT (predicate)`; violation count |
| Duplicate groups | `dq-composite-uniqueness` | `GROUP BY` payload columns; `HAVING COUNT(*) > 1` |
| Parent–child aggregate | `dq-parent-child-reconciliation` | Join host (parent) to child; compare parent measure vs child aggregate |
| Cross-table aggregate | `dq-cross-table-reconciliation` | Two-table metrics (host + payload partner); compare aggregates |
| Freshness / SLA | `dq-data-age-check` (L1), `dq-sla-compliance-check` | Timestamp column vs deadline or maxAge anchor |
| Column semantic | `dq-predicate` | Row filter in `predicate` (e.g. `birth_date <= CURRENT_DATE`) |

---

### Predicate dialect (`dq-predicate`)

The **`predicate`** field is a **row-level boolean expression** evaluated per row on the host table. Column names are **unqualified** identifiers on `$entityTable` unless the engine documents otherwise at **M-16**.

| Construct | Example |
|-----------|---------|
| Comparison | `start_date <= end_date`, `total = subtotal + tax` |
| Logical | `status <> 'TERMINATED' OR termination_date IS NOT NULL` |
| Null tests | `termination_date IS NULL`, `middle_name IS NOT NULL` |
| Range / set | `age BETWEEN 0 AND 120`, `region IN ('EU', 'US')` |

**Violations:** rows where `NOT (predicate)`. **Pass:** zero violating rows.

Typed facet variants (separate fields per catalog rule) may be added later if form UX requires guided inputs; v1 uses one **`dq-predicate`** type.

---

### `urn:mill/metadata/facet-type:dq-predicate`

**Catalog:** Cross-Column Consistency; Derived Value Validation; Conditional Completeness; Semantic Validation  
**`applicableTo`:** `urn:mill/metadata/entity-type:table`

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `predicate` | STRING | yes | Row-level boolean expression on the host table (see **Predicate dialect**). |

**Catalog examples (instances of this type):**

| Catalog rule | Example `predicate` |
|--------------|---------------------|
| Cross-Column Consistency | `start_date <= end_date` |
| Derived Value Validation | `total = subtotal + tax` |
| Conditional Completeness | `status <> 'TERMINATED' OR termination_date IS NOT NULL` |
| Semantic Validation | `birth_date <= CURRENT_DATE`, `amount >= 0` |

```text
target: table
parameters: { predicate, ...common }
plan:
  scan: $entityTable
  filter: NOT (predicate)
  aggregate: violation_count = COUNT(*)
pass: violation_count = 0
bindings:
  $entityTable ← assignment target table entity
notes: feasibility = in
```

---

### `urn:mill/metadata/facet-type:dq-composite-uniqueness`

**Catalog:** Composite Uniqueness  
**`applicableTo`:** table

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `columns` | ARRAY of STRING | yes | Two or more column names; combination must be unique across rows (NULL handling per engine at M-16). |

```text
target: table
parameters: { columns, ...common }
plan:
  scan: $entityTable
  aggregate: duplicate_groups = COUNT(*) FROM (
    GROUP BY columns[] HAVING COUNT(*) > 1
  )
pass: duplicate_groups = 0
bindings:
  $entityTable ← assignment target
notes: feasibility = in — same pattern as L1 unique check with multi-column GROUP BY
```

---

### `urn:mill/metadata/facet-type:dq-parent-child-reconciliation`

**Catalog:** Parent-Child Reconciliation  
**`applicableTo`:** table (host = **parent** / summary side)

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `child` | OBJECT | yes | Child/detail side: `schema`, `table`, `foreignKey`, `aggregate`. |
| `parentKey` | STRING | yes | Column on host (parent) for join (e.g. `invoice_id`). |
| `parentMeasure` | STRING | yes | Expression or column on parent to compare (e.g. `total_amount`). |
| `joinSql` | STRING | no | Optional join override when equi-join on keys is insufficient; takes precedence when set. |
| `tolerance` | NUMBER | no | Absolute allowed difference between `parentMeasure` and `child.aggregate` (schema default **0**). |

```text
target: table
parameters: { child: { schema, table, foreignKey, aggregate }, parentKey, parentMeasure, joinSql?, tolerance?, ...common }
plan:
  scan: $entityTable AS p
  join: LEFT JOIN $childTable AS c ON <joinSql or c.foreignKey = p.parentKey>
  aggregate: GROUP BY p.<parentKey>
  filter: ABS(p.parentMeasure - $child.aggregate) > COALESCE(tolerance, 0)
  metric: violation_count = COUNT(*)
pass: violation_count = 0
bindings:
  $entityTable ← assignment target (parent)
  $childTable ← payload.child
notes: feasibility = in — single plan with grouped join aggregate
```

**Pseudo SQL, orphan handling, and post-processing:** [`dq-rule-relplan-sketches.md`](dq-rule-relplan-sketches.md) § `dq-parent-child-reconciliation`.

---

### `urn:mill/metadata/facet-type:dq-cross-table-reconciliation`

**Catalog:** Cross-Table Reconciliation  
**`applicableTo`:** table (host = **this** side of the comparison)

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `other` | OBJECT | yes | Partner side: `schema`, `table`, `aggregate`, optional `filter`. |
| `aggregate` | STRING | yes | Scalar aggregate on host (e.g. `SUM(order_total)`). |
| `filter` | STRING | no | Optional WHERE fragment for host scan. |
| `tolerance` | NUMBER | no | Absolute allowed difference between `aggregate` and `other.aggregate` (schema default **0**). |

```text
target: table
parameters: { other: { schema, table, aggregate, filter? }, aggregate, filter?, tolerance?, ...common }
plan:
  scan: $entityTable [WHERE filter]
  aggregate: this_metric = aggregate
  scan: $otherTable [WHERE other.filter]
  aggregate: other_metric = other.aggregate
  compare: ABS(this_metric - other_metric) <= COALESCE(tolerance, 0)
pass: compare holds
bindings:
  $entityTable ← assignment target
  $otherTable ← payload.other
notes: feasibility = in — two scalar aggregates in one plan (cross-table metric compare)
```

**Pseudo SQL, NULL/empty aggregates, and post-processing:** [`dq-rule-relplan-sketches.md`](dq-rule-relplan-sketches.md) § `dq-cross-table-reconciliation`.

---

### `urn:mill/metadata/facet-type:dq-sla-compliance-check`

**Catalog:** SLA Compliance Check  
**`applicableTo`:** table

Distinct from L1 **`dq-data-age-check`**: SLA checks **arrival before a daily deadline** on a schedule, not “latest timestamp within maxAge.”

| Field | Schema | Required | Description |
|-------|--------|----------|-------------|
| `timestampColumn` | STRING | yes | Ingest or batch timestamp on the host table (e.g. `loaded_at`). |
| `deadlineLocalTime` | STRING | yes | Local deadline as `HH:mm` (e.g. `06:00`). |
| `expectedScheduleCron` | STRING | no | Cron for expected arrival days (e.g. `0 0 * * * *` daily). When omitted, every calendar day in `timezone`. |
| `expectedScheduleTimezone` | STRING | no | IANA timezone for deadline and cron (platform local when omitted). |
| `graceDuration` | STRING | no | ISO-8601 duration after deadline still acceptable (e.g. `PT15M`). |

```text
target: table
parameters: { timestampColumn, deadlineLocalTime, expectedScheduleCron?, expectedScheduleTimezone?, graceDuration?, ...common }
plan:
  let zone = expectedScheduleTimezone ?? platformLocalTimezone()
  let window = scheduleWindow(expectedScheduleCron, deadlineLocalTime, graceDuration, zone, evaluationTime)
  scan: $entityTable
  filter: MAX(timestampColumn) within window is missing OR MAX(timestampColumn) > window.end
  aggregate: violation_count = COUNT(*)  # or single boolean fail for table-level SLA
pass: latest batch arrived before deadline (+ grace) for the evaluation window
bindings:
  $entityTable ← assignment target
notes: feasibility = borderline — requires reliable timestamp column and schedule window resolution (M-16); single plan when preconditions hold
```

**Relationship to L1:** use **`dq-data-age-check`** for freshness relative to maxAge / feed schedule anchor; use **`dq-sla-compliance-check`** for “must land before 06:00” timeliness.

---

## Scope assessment (all 15 facet types)

| Facet type | Catalog rule(s) | Feasibility | Notes |
|------------|-------------------|-------------|-------|
| `dq-null-check` | Null Check | **in** | |
| `dq-empty-value-check` | Empty Value Check | **in** | |
| `dq-unique-value-check` | Unique Value Check | **in** | |
| `dq-allowed-values-check` | Allowed Values Check | **in** | |
| `dq-pattern-check` | Pattern Check | **in** | `sql_regex` dialect at M-16 |
| `dq-min-max-check` | Min/Max Check | **in** | |
| `dq-data-age-check` | Data Age Check | **in** | Schedule-relative anchor optional |
| `dq-referential-integrity` | Foreign Key Check (full) | **in** | |
| `dq-referential-source` | Foreign Key Check (outbound) | **in** | |
| `dq-referential-target` | Foreign Key Check (inbound) | **in** | |
| `dq-predicate` | Cross-Column; Derived; Conditional Completeness | **in** | Three catalog names → one type |
| `dq-composite-uniqueness` | Composite Uniqueness | **in** | |
| `dq-parent-child-reconciliation` | Parent-Child Reconciliation | **in** | |
| `dq-cross-table-reconciliation` | Cross-Table Reconciliation | **in** | |
| `dq-sla-compliance-check` | SLA Compliance Check | **borderline** | Needs timestamp + schedule/deadline resolution |

**Removed:** `dq-semantic-validation` — folded into **`dq-predicate`** (enum kinds are expressible as row predicates with no extra facet type).

| Verdict | Meaning |
|---------|---------|
| **in** | Single relational plan with documented bindings |
| **borderline** | Single plan when documented preconditions hold |
| **out** | Not in this story (L3 only) |

---

## L3 out of scope

Rules in [`dq-rules.md`](../data/dq-rules.md) § L3 are **not** modeled as facet types in this story. They typically need history, baselines, ML, or cross-system orchestration beyond one static relational plan:

- Row count anomaly, distribution drift, statistical outliers
- Master data / external reference validation (unless folded into future typed facets)
- Temporal integrity across effective dates
- Fuzzy duplicate detection
- Cross-system reconciliation
- Lifecycle and state-transition validation

Future stories may add L3 facet types or a separate monitoring layer (**M-16** and beyond).

---

## Catalog cross-reference (L1)

| Catalog rule | Facet type URN |
|--------------|----------------|
| Null Check | `dq-null-check` |
| Empty Value Check | `dq-empty-value-check` |
| Unique Value Check | `dq-unique-value-check` |
| Allowed Values Check | `dq-allowed-values-check` |
| Pattern Check | `dq-pattern-check` |
| Foreign Key Check | `dq-referential-integrity`, `dq-referential-source`, `dq-referential-target` |
| Data Age Check | `dq-data-age-check` |
| Min/Max Check | `dq-min-max-check` |

---

## Catalog cross-reference (L2)

| Catalog rule | Facet type URN |
|--------------|----------------|
| Cross-Column Consistency | `dq-predicate` |
| Derived Value Validation | `dq-predicate` |
| Conditional Completeness | `dq-predicate` |
| Semantic Validation | `dq-predicate` |
| Composite Uniqueness | `dq-composite-uniqueness` |
| Parent-Child Reconciliation | `dq-parent-child-reconciliation` |
| Cross-Table Reconciliation | `dq-cross-table-reconciliation` |
| SLA Compliance Check | `dq-sla-compliance-check` |

Four catalog L2 rows share **`dq-predicate`** (different `predicate` values).

---

## See also

- [`dq-rules.md`](../data/dq-rules.md) — full rule catalog and roadmap
- [`dq-rule-relplan-sketches.md`](dq-rule-relplan-sketches.md) — pseudo SQL and post-processing per facet type
- [`STORY.md`](../../workitems/in-progress/dqm-metadata-facets/STORY.md) — delivery checklist
- Future execution: **M-16** in [`BACKLOG.md`](../../workitems/BACKLOG.md)
