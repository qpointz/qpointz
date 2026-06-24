# Data quality — relational plan sketches (L1/L2)

**Status:** Design reference for **M-16** execution compiler  
**Last updated:** 2026-06-24  
**Contract:** [`dq-rule-facet-types.md`](dq-rule-facet-types.md) · **Catalog:** [`dq-rules.md`](../data/dq-rules.md)

Pseudo SQL below is **illustrative** — table/column identifiers are resolved from facet assignment + payload at compile time. Dialect details (regex, timezone, cron) are engine responsibilities.

---

## Conventions

| Symbol | Meaning |
|--------|---------|
| `$hostTable` | Physical table for **`FacetAssignment.entityId`** when `applicableTo` is `table` |
| `$hostColumn` | Physical column for assignment when `applicableTo` is `attribute` |
| `$hostSchema.$hostTable` | Catalog path from assigned entity |
| `:payload.*` | Bound from facet instance JSON (rule-specific + common fields) |
| `:evalTime` | Evaluation timestamp (wall-clock or job trigger) |
| `:tolerance` | `COALESCE(payload.tolerance, 0)` |

**Single plan rule:** one compiled statement (CTEs/subqueries allowed) producing **`scanned_row_count`**, **`violating_row_count`**, and optional **`metric_*`** columns for aggregate rules.

---

## Shared post-processing

All row-scan rules (most L1 + `dq-predicate`, `dq-composite-uniqueness`, referential checks) share:

```text
inputs:  scanned_row_count, violating_row_count, payload.severity, payload.profile
rate     = (violating_row_count / scanned_row_count) * 100   -- when scanned_row_count > 0
raw_pass = (violating_row_count = 0)

if scanned_row_count = 0:
  outcome = PASS

elif payload.profile is null:
  outcome = PASS if raw_pass else payload.severity (default error)

elif payload.profile matches warn:error (e.g. 10:80):
  outcome = PASS           if rate <= warn
          = WARNING        if warn < rate <= error
          = ERROR          if rate > error

elif payload.profile is single integer N:
  outcome = PASS           if rate <= N
          = payload.severity (default error) if rate > N
```

**Aggregate / reconciliation rules** (`dq-data-age-check`, `dq-sla-compliance-check`, `dq-cross-table-reconciliation`) use **binary pass** on the metric comparison; `profile` applies only when the compiler maps them to a **row denominator** (documented per type). Parent–child reconciliation uses **parent-row denominator** (see § Parent–child).

---

## L1 — column rules (`applicableTo: attribute`)

### `dq-null-check`

```text
plan:
  scan: $hostSchema.$hostTable
  filter: $hostColumn IS NULL
  metrics: scanned_row_count = COUNT(*), violating_row_count = COUNT(*) WHERE filter
pass (raw): violating_row_count = 0
postprocess: shared
```

```sql
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (WHERE :hostColumn IS NULL) AS violating_row_count
FROM :hostSchema.:hostTable;
```

---

### `dq-empty-value-check`

```text
plan:
  let trim = COALESCE(:payload.trimWhitespace, true)
  filter: CASE WHEN trim THEN TRIM(:hostColumn) = '' ELSE :hostColumn = '' END
            AND :hostColumn IS NOT NULL
  metrics: scanned_row_count, violating_row_count
pass (raw): violating_row_count = 0
postprocess: shared
```

```sql
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (
    WHERE :hostColumn IS NOT NULL
      AND (
        CASE WHEN :trimWhitespace
             THEN TRIM(:hostColumn) = ''
             ELSE :hostColumn = ''
        END
      )
  ) AS violating_row_count
FROM :hostSchema.:hostTable;
```

---

### `dq-unique-value-check`

```text
plan:
  subquery: GROUP BY :hostColumn HAVING COUNT(*) > 1
  metrics: scanned_row_count = COUNT(*) FROM base
           violating_row_count = SUM(group_size) FROM duplicate groups
           -- alt: violating_row_count = rows participating in duplicate groups
pass (raw): duplicate_groups = 0
postprocess: shared (denominator = all rows; numerator = rows in duplicate groups)
```

```sql
WITH base AS (
  SELECT :hostColumn AS v FROM :hostSchema.:hostTable
),
dup_groups AS (
  SELECT v, COUNT(*) AS cnt
  FROM base
  GROUP BY v
  HAVING COUNT(*) > 1
)
SELECT
  (SELECT COUNT(*) FROM base) AS scanned_row_count,
  (SELECT COALESCE(SUM(cnt), 0) FROM dup_groups) AS violating_row_count;
```

---

### `dq-allowed-values-check`

```text
plan:
  filter: :hostColumn NOT IN (:payload.allowedValues)
  metrics: scanned_row_count, violating_row_count
pass (raw): violating_row_count = 0
postprocess: shared
```

```sql
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (
    WHERE :hostColumn NOT IN (:allowedValues)  -- NULL typically not in set
  ) AS violating_row_count
FROM :hostSchema.:hostTable;
```

---

### `dq-pattern-check`

```text
plan:
  dialect = COALESCE(:payload.patternDialect, 'sql_like')
  filter: NOT pattern_match(:hostColumn, :payload.pattern, dialect)
  metrics: scanned_row_count, violating_row_count
pass (raw): violating_row_count = 0
postprocess: shared
```

```sql
-- sql_like example
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (
    WHERE NOT (:hostColumn LIKE :pattern)
  ) AS violating_row_count
FROM :hostSchema.:hostTable;
```

---

### `dq-min-max-check`

```text
plan:
  filter: (:payload.min IS NOT NULL AND :hostColumn < :min)
       OR (:payload.max IS NOT NULL AND :hostColumn > :max)
  metrics: scanned_row_count, violating_row_count
pass (raw): violating_row_count = 0
postprocess: shared
```

```sql
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (
    WHERE (:min IS NOT NULL AND :hostColumn < :min)
       OR (:max IS NOT NULL AND :hostColumn > :max)
  ) AS violating_row_count
FROM :hostSchema.:hostTable;
```

---

## L1 — table rules

### `dq-data-age-check`

```text
plan:
  zone   = COALESCE(:payload.expectedScheduleTimezone, platformLocal())
  anchor = scheduleTick(:payload.expectedScheduleCron, :evalTime, zone) ?? :evalTime
  filter rows: :payload.timestampColumn < anchor   (when cron set; else all rows)
  metric: latest_ts = MAX(:payload.timestampColumn)
pass (raw): latest_ts >= anchor - parseDuration(:payload.maxAge)
postprocess: binary — no row rate; outcome PASS/FAIL from raw_pass; profile optional only if M-16 maps to synthetic 0/1 counts
```

```sql
-- schedule-relative freshness (cron set)
WITH bounds AS (
  SELECT
    :anchor AS anchor_ts,
    :anchor - INTERVAL :maxAge AS min_ok_ts
),
agg AS (
  SELECT MAX(:timestampColumn) AS latest_ts
  FROM :hostSchema.:hostTable
  WHERE :timestampColumn < (SELECT anchor_ts FROM bounds)  -- omit WHERE when no cron
)
SELECT
  1 AS scanned_row_count,
  CASE WHEN latest_ts >= (SELECT min_ok_ts FROM bounds) THEN 0 ELSE 1 END AS violating_row_count
FROM agg;
```

---

## L1 — referential integrity (triple)

Join predicate: `COALESCE(:payload.joinSql, equiJoin(sourceCols, targetCols))`.

### `dq-referential-integrity` (schema assignment)

```text
plan:
  scan: :source.schema.:source.table AS s
  join: LEFT JOIN :target.schema.:target.table AS t ON <join>
  filter: t.<targetKey> IS NULL AND s.<sourceKey> IS NOT NULL
  metrics: scanned_row_count = COUNT(*) FROM s, violating_row_count
postprocess: shared
```

```sql
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (
    WHERE t.:targetKey IS NULL AND s.:sourceKey IS NOT NULL
  ) AS violating_row_count
FROM :sourceSchema.:sourceTable s
LEFT JOIN :targetSchema.:targetTable t
  ON :joinPredicate;
```

### `dq-referential-source` (host = source / child FK table)

```sql
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (WHERE t.:targetKey IS NULL AND s.:sourceKey IS NOT NULL) AS violating_row_count
FROM :hostSchema.:hostTable s
LEFT JOIN :targetSchema.:targetTable t ON :joinPredicate;
```

### `dq-referential-target` (host = target / parent table)

```sql
-- inbound orphans: child rows pointing to missing parent keys on host
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (WHERE t.:targetKey IS NULL AND s.:sourceKey IS NOT NULL) AS violating_row_count
FROM :sourceSchema.:sourceTable s
LEFT JOIN :hostSchema.:hostTable t ON :joinPredicate;
```

---

## L2 — table rules

### `dq-predicate`

```text
plan:
  filter: NOT (:payload.predicate)
  metrics: scanned_row_count, violating_row_count
pass (raw): violating_row_count = 0
postprocess: shared
```

```sql
SELECT
  COUNT(*) AS scanned_row_count,
  COUNT(*) FILTER (WHERE NOT (:predicate)) AS violating_row_count
FROM :hostSchema.:hostTable;
```

---

### `dq-composite-uniqueness`

```sql
WITH grouped AS (
  SELECT :col1, :col2 /* ... */, COUNT(*) AS cnt
  FROM :hostSchema.:hostTable
  GROUP BY :col1, :col2 /* ... */
  HAVING COUNT(*) > 1
)
SELECT
  (SELECT COUNT(*) FROM :hostSchema.:hostTable) AS scanned_row_count,
  (SELECT COALESCE(SUM(cnt), 0) FROM grouped) AS violating_row_count;
```

---

### `dq-parent-child-reconciliation`

**Intent:** For each parent row on the assignment target, **`parentMeasure`** must match **`child.aggregate`** over lines linked by **`parentKey` / `child.foreignKey`**, within **`tolerance`**.

```text
plan:
  host:   $hostSchema.$hostTable AS p
  child:  :payload.child.schema.:payload.child.table AS c
  join:   COALESCE(:payload.joinSql, c.:child.foreignKey = p.:parentKey)
  per_parent:
    child_agg = eval(:payload.child.aggregate) GROUP BY p.:parentKey
    delta     = ABS(p.:parentMeasure - child_agg)
  filter: delta > :tolerance
  metrics:
    scanned_row_count   = COUNT(DISTINCT p.:parentKey)   -- all parents
    violating_row_count = COUNT(*) WHERE filter          -- parents out of tolerance
pass (raw): violating_row_count = 0
postprocess: shared (rate = violating parents / all parents)
```

**Pseudo SQL (invoice total vs line sum example):**

```sql
WITH child_agg AS (
  SELECT
    c.invoice_id AS parent_key,
    SUM(c.line_amount) AS child_total
  FROM billing.invoice_lines c
  GROUP BY c.invoice_id
),
recon AS (
  SELECT
    p.invoice_id,
    p.total_amount AS parent_measure,
    COALESCE(ca.child_total, 0) AS child_aggregate,
    ABS(p.total_amount - COALESCE(ca.child_total, 0)) AS delta
  FROM billing.invoices p
  LEFT JOIN child_agg ca
    ON ca.parent_key = p.invoice_id
  -- joinSql override replaces ON clause when present
)
SELECT
  (SELECT COUNT(*) FROM billing.invoices) AS scanned_row_count,
  (SELECT COUNT(*) FROM recon WHERE delta > :tolerance) AS violating_row_count
FROM (SELECT 1) _one;
```

**Post-processing notes:**

| Case | Handling |
|------|----------|
| Parent with **no child rows** | `COALESCE(child_aggregate, 0)` — parent measure compared to zero unless join excludes orphans |
| **`joinSql` set** | Replaces equi-join; compiler validates expressions reference `p` / `c` aliases |
| **`parentMeasure` / `child.aggregate`** | Opaque expressions compiled by engine (column ref or `SUM(...)` etc.) |
| **`profile`** | Applied to **parent-row rate** (violating parents ÷ total parents), not line-row rate |
| **`severity`** | Used when `profile` is single-threshold or omitted (via shared post-process) |

**Optional detail query (remediation sample — not part of pass/fail aggregate):**

```sql
SELECT invoice_id, parent_measure, child_aggregate, delta
FROM recon
WHERE delta > :tolerance
ORDER BY delta DESC
LIMIT 100;
```

---

### `dq-cross-table-reconciliation`

**Intent:** Scalar **`aggregate`** on host table must match **`other.aggregate`** on partner table (optional **`filter`** / **`other.filter`**), within **`tolerance`**.

```text
plan:
  host_metric  = eval(:payload.aggregate)  OVER :hostSchema.:hostTable [WHERE :payload.filter]
  other_metric = eval(:payload.other.aggregate) OVER :other.schema.:other.table [WHERE :payload.other.filter]
  delta        = ABS(host_metric - other_metric)
pass (raw): delta <= :tolerance
metrics:
  scanned_row_count   = 1                    -- single comparison unit
  violating_row_count = CASE WHEN pass THEN 0 ELSE 1 END
postprocess:
  binary by default — profile/severity apply to 0/1 violating_row_count
  (rate is 0% or 100%; single-threshold profile rarely useful unless extended in M-16)
```

**Pseudo SQL (orders total vs shipments total):**

```sql
WITH host_metric AS (
  SELECT SUM(o.order_total) AS v
  FROM sales.orders o
  WHERE o.status = 'CLOSED'          -- :payload.filter when set
),
other_metric AS (
  SELECT SUM(s.shipment_total) AS v
  FROM warehouse.shipments s
  WHERE s.status = 'SHIPPED'         -- :payload.other.filter when set
),
compare AS (
  SELECT
    h.v AS host_aggregate,
    t.v AS other_aggregate,
    ABS(h.v - t.v) AS delta,
    :tolerance AS tolerance
  FROM host_metric h
  CROSS JOIN other_metric t
)
SELECT
  1 AS scanned_row_count,
  CASE WHEN delta <= tolerance THEN 0 ELSE 1 END AS violating_row_count,
  host_aggregate,
  other_aggregate,
  delta
FROM compare;
```

**Post-processing notes:**

| Case | Handling |
|------|----------|
| **Either aggregate NULL** | Engine policy: treat NULL as 0 vs fail — document in M-16 (recommend **fail** with explicit error) |
| **Empty filtered set** | `SUM` → NULL; compiler should `COALESCE(..., 0)` or fail fast |
| **`profile`** | Binary rule → rate is 0% or 100%; two-threshold profile still works but is coarse |
| **Remediation** | Return `host_aggregate`, `other_aggregate`, `delta` in result payload for dashboards |

**Multi-metric extension (out of v1 metadata):** same facet type; compiler remains one host scalar vs one partner scalar.

---

### `dq-sla-compliance-check`

```text
plan:
  zone    = COALESCE(:payload.expectedScheduleTimezone, platformLocal())
  window  = slaWindow(:payload.expectedScheduleCron, :payload.deadlineLocalTime,
                      :payload.graceDuration, zone, :evalTime)
  metric: latest_arrival = MAX(:payload.timestampColumn) WHERE ts IN window
pass (raw): latest_arrival IS NOT NULL AND latest_arrival <= window.deadline + grace
postprocess: binary (scanned_row_count = 1, violating_row_count ∈ {0,1})
```

```sql
WITH window AS (
  SELECT
    :windowStart AS start_ts,
    :deadlineTs AS deadline_ts,
    :deadlineTs + INTERVAL :graceDuration AS grace_end_ts
),
agg AS (
  SELECT MAX(:timestampColumn) AS latest_arrival
  FROM :hostSchema.:hostTable, window w
  WHERE :timestampColumn >= w.start_ts
    AND :timestampColumn < w.grace_end_ts
)
SELECT
  1 AS scanned_row_count,
  CASE
    WHEN latest_arrival IS NULL THEN 1
    WHEN latest_arrival > (SELECT deadline_ts + INTERVAL :graceDuration FROM window) THEN 1
    ELSE 0
  END AS violating_row_count
FROM agg;
```

---

## Quick index (15 facet types)

| Facet type | § |
|------------|---|
| `dq-null-check` | L1 column |
| `dq-empty-value-check` | L1 column |
| `dq-unique-value-check` | L1 column |
| `dq-allowed-values-check` | L1 column |
| `dq-pattern-check` | L1 column |
| `dq-min-max-check` | L1 column |
| `dq-data-age-check` | L1 table |
| `dq-referential-integrity` | L1 referential |
| `dq-referential-source` | L1 referential |
| `dq-referential-target` | L1 referential |
| `dq-predicate` | L2 |
| `dq-composite-uniqueness` | L2 |
| `dq-parent-child-reconciliation` | L2 (extended) |
| `dq-cross-table-reconciliation` | L2 (extended) |
| `dq-sla-compliance-check` | L2 |

---

## See also

- [`dq-rule-facet-types.md`](dq-rule-facet-types.md) — payload contract, threshold profile, feasibility
- [`platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml) · [`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml)
