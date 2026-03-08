# SQL Dialect YAML Schema — Design & Reference

This document defines the target YAML schema for Mill SQL dialect descriptor files.
It replaces the current ad-hoc structure with a comprehensive schema that serves all
four downstream consumers: SQLAlchemy, JDBC, ibis, and AI NL-to-SQL.

**Related documents:**
- `py-sql-dialect-plan.md` — Phase 9 plan (gap analysis, work items)
- `py-implementation-plan.md` — Parent plan (Phases 0–11)
- `sql-dialect-maintainer-guide.md` — Maintainer process for onboarding new dialects

---

## 1. Context & Motivation

### Current state

The existing YAML files (`core/mill-sql/src/main/resources/sql/dialects/{dialect}/{dialect}.yml`)
were designed primarily for the AI NL-to-SQL prompt pipeline. They capture enough to tell an
LLM "here's how to write SQL" but cover only ~25% of what the full `MillDialectDescriptor`
requires.

### Consumers

| # | Consumer | What it needs |
|---|----------|---------------|
| 1 | **SQLAlchemy** (Phase 11) | Identifier rules, schema/catalog topology, transactions, feature flags, type mapping, paramstyle |
| 2 | **Java JDBC driver** | Full `DatabaseMetaData` population: ~130 methods including limits, type info, ResultSet caps |
| 3 | **ibis backend** (Phase 10) | Function catalog (window, stats, math), feature flags (CTE, set ops), type mapping |
| 4 | **AI NL-to-SQL** | Identifier rules, function catalog, feature flags — rendered into LLM system prompts |

### Kotlin deserialization

The YAML is deserialized into Kotlin data classes via Jackson (`SqlDialectSpec.kt` in
`core/mill-sql/.../sql/v2/dialect/`). The v2 model is strict (`ignoreUnknown=false`) and validated
at load time by `DialectValidator`.

### Design decisions

1. **Kebab-case keys** — consistent with existing YAML convention; mapped via `@JsonProperty`
2. **Strict typed sections with explicit nullability only where meaningful** — predictable runtime contracts
3. **`feature-flags` is `Map<String, Boolean?>`** — extensible without code changes, with nullable tri-state support
4. **`type-info` is a list** — because different SQL types can share JDBC codes
5. **`operators` and `functions` remain `Map<String, List<...>>`** — new categories added as map keys
6. **Flags are authoritative, function lists are informational** — flags answer "can I use this?" while function lists answer "how do I use it?"
7. **Return types use `ANY`** — for polymorphic functions (MIN, MAX, COALESCE, LAG, etc.)

---

## 2. YAML Schema Reference

### 2.1 Top-level fields

```yaml
id: H2                              # Dialect identifier (required)
name: "H2 Database"                 # Display name (required)
read-only: false                    # true for read-only engines like Calcite
paramstyle: qmark                   # qmark | numeric | named | format | pyformat

notes:                              # Free-form notes about the dialect
  - "When using regular expressions use Java regex syntax."
```

### 2.2 Identifiers

```yaml
identifiers:
  quote: { start: "\"", end: "\"" }       # Identifier quote characters (required)
  alias-quote: { start: "\"", end: "\"" } # Alias quote characters (required)
  escape-quote: "\""                       # Escape char within quoted identifiers
  unquoted-storage: UPPER                  # UPPER | LOWER | AS_IS
  quoted-storage: AS_IS                    # UPPER | LOWER | AS_IS
  supports-mixed-case: false               # Unquoted mixed-case preserved?
  supports-mixed-case-quoted: true         # Quoted mixed-case preserved?
  max-length: 256                          # Max identifier length in characters
  extra-name-characters: ""                # Non-standard chars allowed in identifiers
  use-fully-qualified-names: true          # Schema.table.column qualification
```

**Rationale**: `unquoted-storage` replaces the ambiguous `case` field. The split between
`unquoted-storage` and `quoted-storage` maps directly to JDBC's
`storesUpperCaseIdentifiers()` / `storesMixedCaseQuotedIdentifiers()` family.

### 2.3 Catalog & Schema topology

```yaml
catalog-schema:
  supports-schemas: true
  supports-catalogs: true
  catalog-separator: "."
  catalog-at-start: true
  schema-term: "schema"
  catalog-term: "catalog"
  procedure-term: "procedure"
  schemas-in-dml: true
  schemas-in-procedure-calls: true
  schemas-in-table-definitions: true
  schemas-in-index-definitions: true
  schemas-in-privilege-definitions: true
  catalogs-in-dml: true
  catalogs-in-procedure-calls: false
  catalogs-in-table-definitions: true
  catalogs-in-index-definitions: false
  catalogs-in-privilege-definitions: false
```

Maps 1:1 to JDBC `DatabaseMetaData` schema/catalog methods and SA's `supports_schemas`.

### 2.4 Transaction semantics

```yaml
transactions:
  supported: true
  default-isolation: READ_COMMITTED   # NONE | READ_UNCOMMITTED | READ_COMMITTED
                                      # | REPEATABLE_READ | SERIALIZABLE
  supports-multiple: true
  supports-ddl-and-dml: true
  supports-dml-only: false
  ddl-causes-commit: false
  ddl-ignored-in-transactions: false
```

For read-only engines (Calcite): `supported: false`, all others are moot.

### 2.5 Limits

```yaml
limits:
  max-binary-literal-length: 0       # 0 = unlimited / unknown
  max-char-literal-length: 0
  max-column-name-length: 256
  max-columns-in-group-by: 0
  max-columns-in-index: 0
  max-columns-in-order-by: 0
  max-columns-in-select: 0
  max-columns-in-table: 0
  max-connections: 0
  max-index-length: 0
  max-schema-name-length: 256
  max-catalog-name-length: 256
  max-row-size: 0
  max-row-size-includes-blobs: false
  max-statement-length: 0
  max-statements: 0
  max-table-name-length: 256
  max-tables-in-select: 0
```

All integer fields default to 0 (unlimited). Precise values come from vendor docs or
empirical testing. JDBC `DatabaseMetaData.getMax*()` methods return these directly.

### 2.6 Null sorting

```yaml
null-sorting:
  nulls-sorted-high: true            # NULLs sort higher than all non-null values
  nulls-sorted-low: false
  nulls-sorted-at-start: false
  nulls-sorted-at-end: false
  supports-nulls-first: true         # ORDER BY ... NULLS FIRST clause supported
  supports-nulls-last: true          # ORDER BY ... NULLS LAST clause supported
```

Replaces the old `ordering.order-by-nulls: "NULLS FIRST/LAST"` string. The four boolean
flags map to JDBC `nullsAreSortedHigh()` etc. The two `supports-*` flags indicate whether
the explicit `NULLS FIRST/LAST` clause is available.

### 2.7 Result set capabilities

```yaml
result-set:
  forward-only: true
  scroll-insensitive: true
  scroll-sensitive: false
  concurrency-read-only: true
  concurrency-updatable: true
```

Maps to JDBC `supportsResultSetType()` / `supportsResultSetConcurrency()`.

### 2.8 Feature flags

```yaml
feature-flags:
  # ── Column / expression ──
  supports-column-aliasing: true
  supports-expressions-in-order-by: true
  supports-order-by-unrelated: true
  supports-group-by: true
  supports-group-by-unrelated: true
  supports-group-by-beyond-select: true
  supports-like-escape-clause: true
  supports-non-nullable-columns: true
  supports-table-correlation-names: true
  supports-different-table-correlation-names: false

  # ── Null arithmetic ──
  null-plus-non-null-is-null: true

  # ── Conversion ──
  supports-convert: true

  # ── Joins ──
  supports-outer-joins: true
  supports-full-outer-joins: true
  supports-limited-outer-joins: false
  supports-semi-anti-join: false
  supports-lateral: false

  # ── Subqueries ──
  supports-subqueries-in-comparisons: true
  supports-subqueries-in-exists: true
  supports-subqueries-in-ins: true
  supports-subqueries-in-quantifieds: true
  supports-correlated-subqueries: true

  # ── Set operations ──
  supports-union: true
  supports-union-all: true
  supports-intersect: true
  supports-except: true

  # ── Advanced SQL ──
  supports-cte: true
  supports-window-functions: true
  supports-qualify: true
  supports-is-distinct-from: true
  supports-ilike: true
  supports-try-cast: false
  supports-native-boolean: true
  div-is-floordiv: null               # null = needs empirical testing

  # ── DDL / DML ──
  supports-alter-table-add-column: true
  supports-alter-table-drop-column: true
  supports-select-for-update: true
  supports-stored-procedures: true
  supports-batch-updates: true
  supports-savepoints: true
  supports-named-parameters: false
  supports-multiple-result-sets: false
  supports-multiple-open-results: false
  supports-get-generated-keys: true
  supports-statement-pooling: false
  supports-stored-functions-using-call-syntax: true

  # ── Positioned operations ──
  supports-positioned-delete: false
  supports-positioned-update: false

  # ── SQL grammar compliance ──
  supports-minimum-sql-grammar: true
  supports-core-sql-grammar: true
  supports-extended-sql-grammar: false
  supports-ansi92-entry-level: true
  supports-ansi92-intermediate: false
  supports-ansi92-full: false
  supports-integrity-enhancement: true

  # ── Miscellaneous ──
  auto-commit-failure-closes-all-result-sets: false
  generated-key-always-returned: true
```

Stored as `Map<String, Boolean?>`. `null` means "unknown, needs empirical testing."
Consumers should treat absent keys as `false` (conservative default).

### 2.8.1 Feature flag catalog (complete, current schema)

The list below is the full catalog currently used by the four reference dialects.

| Flag | Meaning when `true` |
|---|---|
| `supports-column-aliasing` | Column aliases are supported in `SELECT` lists. |
| `supports-expressions-in-order-by` | `ORDER BY` can reference expressions, not only projected columns. |
| `supports-order-by-unrelated` | `ORDER BY` can reference columns/expressions not present in `SELECT`. |
| `supports-group-by` | `GROUP BY` is supported. |
| `supports-group-by-unrelated` | `GROUP BY` can reference columns not projected in `SELECT`. |
| `supports-group-by-beyond-select` | Grouping can include extra expressions beyond the projection list. |
| `supports-like-escape-clause` | `LIKE ... ESCAPE ...` syntax is supported. |
| `supports-non-nullable-columns` | `NOT NULL` column constraints are supported. |
| `supports-table-correlation-names` | Table aliases/correlation names are supported in `FROM`. |
| `supports-different-table-correlation-names` | Table aliases can differ from base table names where required. |
| `null-plus-non-null-is-null` | `NULL + X` evaluates to `NULL`. |
| `supports-convert` | Conversion operator/function (for example `CONVERT`) is supported. |
| `supports-outer-joins` | Outer joins are supported. |
| `supports-full-outer-joins` | `FULL OUTER JOIN` is supported. |
| `supports-limited-outer-joins` | Outer joins are supported with documented limitations. |
| `supports-semi-anti-join` | SEMI/ANTI join semantics are supported. |
| `supports-lateral` | LATERAL joins/subqueries are supported. |
| `supports-subqueries-in-comparisons` | Subqueries are supported in comparison predicates. |
| `supports-subqueries-in-exists` | Subqueries are supported in `EXISTS` predicates. |
| `supports-subqueries-in-ins` | Subqueries are supported in `IN (...)` predicates. |
| `supports-subqueries-in-quantifieds` | Subqueries are supported in quantified predicates (`ANY/ALL/SOME`). |
| `supports-correlated-subqueries` | Correlated subqueries are supported. |
| `supports-union` | `UNION` is supported. |
| `supports-union-all` | `UNION ALL` is supported. |
| `supports-intersect` | `INTERSECT` is supported. |
| `supports-except` | `EXCEPT` (or equivalent minus set op) is supported. |
| `supports-cte` | Common table expressions (`WITH`) are supported. |
| `supports-window-functions` | Window/analytic functions are supported. |
| `supports-qualify` | `QUALIFY` clause is supported. |
| `supports-is-distinct-from` | Null-safe distinct comparison (`IS [NOT] DISTINCT FROM`) is supported. |
| `supports-ilike` | Case-insensitive `ILIKE` operator is supported. |
| `supports-try-cast` | Safe cast form (`TRY_CAST`) is supported. |
| `supports-native-boolean` | Dialect has a native boolean type/semantics. |
| `div-is-floordiv` | Integer division semantics are floor/truncating division (`null` = unknown). |
| `supports-alter-table-add-column` | `ALTER TABLE ... ADD COLUMN` is supported. |
| `supports-alter-table-drop-column` | `ALTER TABLE ... DROP COLUMN` is supported. |
| `supports-select-for-update` | `SELECT ... FOR UPDATE` row locking is supported. |
| `supports-stored-procedures` | Stored procedures are supported. |
| `supports-batch-updates` | Batched DML execution is supported by the driver/engine. |
| `supports-savepoints` | Transaction savepoints are supported. |
| `supports-named-parameters` | Named statement parameters are supported. |
| `supports-multiple-result-sets` | Multiple result sets can be returned from one execution. |
| `supports-multiple-open-results` | Multiple open result sets can coexist. |
| `supports-get-generated-keys` | Generated keys retrieval is supported. |
| `supports-statement-pooling` | Statement pooling is supported. |
| `supports-stored-functions-using-call-syntax` | Stored functions can be invoked via JDBC call syntax. |
| `supports-positioned-delete` | Positioned deletes via cursors are supported. |
| `supports-positioned-update` | Positioned updates via cursors are supported. |
| `supports-minimum-sql-grammar` | Minimum SQL grammar profile is supported. |
| `supports-core-sql-grammar` | Core SQL grammar profile is supported. |
| `supports-extended-sql-grammar` | Extended SQL grammar profile is supported. |
| `supports-ansi92-entry-level` | ANSI-92 entry-level grammar is supported. |
| `supports-ansi92-intermediate` | ANSI-92 intermediate grammar is supported. |
| `supports-ansi92-full` | ANSI-92 full grammar is supported. |
| `supports-integrity-enhancement` | SQL integrity enhancement facility is supported. |
| `auto-commit-failure-closes-all-result-sets` | Auto-commit failure closes all open result sets. |
| `generated-key-always-returned` | Insert/update generated keys are always returned. |

### 2.9 String & keyword properties

```yaml
string-properties:
  search-string-escape: "\\"
  sql-keywords: "LIMIT,MINUS,ROWNUM,..."        # Comma-separated non-standard keywords
  system-functions: "DATABASE,USER,..."          # Comma-separated system function names
```

### 2.10 Literals

Unchanged from current structure:

```yaml
literals:
  strings:
    quote: "'"
    concat: "||"
    escape: STANDARD                  # STANDARD | BACKSLASH | DOUBLING
    note: "..."
  booleans: [ "TRUE", "FALSE", "UNKNOWN" ]
  null: "NULL"
  dates-times:
    date:      { syntax: "DATE 'YYYY-MM-DD'",                    quote: "'", pattern: "YYYY-MM-DD" }
    time:      { syntax: "TIME 'HH:MI:SS'",                      quote: "'", pattern: "HH:MI:SS" }
    timestamp: { syntax: "TIMESTAMP 'YYYY-MM-DD HH:MI:SS'",      quote: "'", pattern: "YYYY-MM-DD HH:MI:SS" }
    interval:  { supported: true, style: ANSI }
```

### 2.11 Joins

Unchanged from current structure:

```yaml
joins:
  style: explicit
  cross-join:  { enabled: true,  keyword: "CROSS JOIN" }
  inner-join:  { keyword: "INNER JOIN", require-on: true,  null-safe: false }
  left-join:   { enabled: true,  keyword: "LEFT JOIN",  require-on: true,  null-safe: false }
  right-join:  { enabled: true,  keyword: "RIGHT JOIN", require-on: true,  null-safe: false }
  full-join:   { enabled: true,  keyword: "FULL JOIN",  require-on: true,  null-safe: false }
  on-clause:   { keyword: "ON", require-condition: true }
```

### 2.12 Paging

Updated to support multiple syntax styles:

```yaml
paging:
  styles:
    - { syntax: "FETCH FIRST {n} ROWS ONLY", type: standard }
    - { syntax: "LIMIT {n}",                 type: compat, deprecated: true }
    - { syntax: "TOP {n}",                   type: compat, deprecated: true }
  offset: "OFFSET {m} ROWS"
  no-limit-value: null                # null = not applicable
```

The `styles` list replaces the old `limit`/`top` fields. Styles are ordered by preference.
Legacy `limit` and `top` keys are not part of the v2 schema and should not be authored.

### 2.13 Operators

Unchanged structure (`Map<String, List<OperatorEntry>>`), with new categories:

```yaml
operators:
  equality:    [...]
  inequality:  [...]
  comparison:  [...]
  arithmetic:  [...]    # renamed from arithmetic_operators
  logical:     [...]
  null-checks: [...]
  null-safe:   [...]
  set:         [...]
  like:        [...]
  between:     [...]
  regex:       [...]
  bitwise:     [...]    # NEW category
  casting:     [...]
```

Each entry: `{ symbol, syntax?, description, supported?, deprecated? }`

### 2.14 Functions

Unchanged structure (`Map<String, List<FunctionEntry>>`), with new categories:

```yaml
functions:
  strings:      [...]     # existing
  regex:        [...]     # existing
  numerics:     [...]     # existing
  math:         [...]     # NEW — trig, logarithmic, etc.
  aggregates:   [...]     # existing
  statistics:   [...]     # NEW — STDDEV, VAR, COVAR
  window:       [...]     # NEW — ROW_NUMBER, RANK, LAG, etc.
  dates-times:  [...]     # existing (renamed from dates_times)
  conditionals: [...]     # existing
```

Each entry:

```yaml
- name: FUNCTION_NAME
  synonyms: [ ALIAS1, ALIAS2 ]       # optional
  return: { type: TYPE, nullable: bool }
  syntax: "FUNCTION_NAME(args...)"
  args:                               # optional, [] for no-arg functions
    - { name: arg, type: TYPE, required: bool, ... }
  notes:                              # optional
    - "Additional information"
```

### 2.15 Type info

```yaml
type-info:
  - sql-name: INTEGER
    jdbc-type-code: 4
    precision: 10
    literal-prefix: null              # e.g. "'" for strings, "DATE '" for dates
    literal-suffix: null
    case-sensitive: false
    searchable: 3                     # 0=none, 1=char-only, 2=all-except-like, 3=all
    unsigned: false
    fixed-prec-scale: false
    auto-increment: true
    minimum-scale: 0
    maximum-scale: 0
    num-prec-radix: 10
```

One entry per SQL type supported by the dialect. Maps to JDBC `getTypeInfo()` ResultSet.

---

## 3. Kotlin Data Class Structure

```kotlin
data class SqlDialectSpec(
    val id: String,
    val name: String,
    @JsonProperty("read-only") val readOnly: Boolean,
    val paramstyle: String,
    val notes: List<String> = emptyList(),
    val identifiers: Identifiers,
    @JsonProperty("catalog-schema") val catalogSchema: CatalogSchema,
    val transactions: Transactions,
    val limits: Limits,
    @JsonProperty("null-sorting") val nullSorting: NullSorting,
    @JsonProperty("result-set") val resultSet: ResultSetCaps,
    @JsonProperty("feature-flags") val featureFlags: Map<String, Boolean?> = emptyMap(),
    @JsonProperty("string-properties") val stringProperties: StringProperties,
    val literals: Literals,
    val joins: Joins,
    val paging: Paging,
    val operators: Map<String, List<OperatorEntry>> = emptyMap(),
    val functions: Map<String, List<FunctionEntry>> = emptyMap(),
    @JsonProperty("type-info") val typeInfo: List<TypeInfo> = emptyList()
)
```

### Sub-records (new)

| Record | Section | Fields |
|--------|---------|--------|
| `CatalogSchema` | `catalog-schema` | 16 fields |
| `Transactions` | `transactions` | 7 fields |
| `Limits` | `limits` | 18 fields |
| `NullSorting` | `null-sorting` | 6 fields |
| `ResultSetCaps` | `result-set` | 5 fields |
| `StringProperties` | `string-properties` | 3 fields |
| `TypeInfo` | `type-info` list entries | 13 fields |

### Sub-records (modified)

| Record | Changes |
|--------|---------|
| `Identifiers` | +6 fields: `escape-quote`, `unquoted-storage`, `quoted-storage`, `supports-mixed-case`, `supports-mixed-case-quoted`, `max-length`, `extra-name-characters`; removed `case` |
| `Paging` | Added `styles` list and `no-limit-value`; kept `limit`/`top` as `Optional` for migration |

### Sub-records (unchanged)

`Literals`, `Joins`, `OperatorEntry`, `FunctionEntry`, `QuotePair`, `ReturnType`, `FunctionArg`

---

## 4. Migration Strategy

### Phase 1 — Update Kotlin model + validation (non-breaking)

Add all new fields to `SqlDialectSpec` and associated data classes. Existing YAML files
continue to deserialize during migration where defaults are defined.

### Phase 2 — Update one reference dialect (H2 or Calcite)

Populate the full schema for one dialect as a reference. Validate deserialization with
existing unit tests + new tests for the expanded sections.

### Phase 3 — Populate initial core dialects (4)

Apply the expanded schema to the initial `core/mill-sql` dialect set:
`CALCITE`, `POSTGRES`, `H2`, `MYSQL`. Content comes from vendor documentation
(~75% coverable) and empirical testing via the Slice 9B `DialectTester` (~25%).

Further dialects can be added in later migration waves once the core set is stable.

### Phase 4 — Rewrite AI consumer

Replace `SpecSqlDialect` (raw `Map<String, Object>`) + Pebble template with a typed prompt
builder that consumes `SqlDialectSpec` directly. Target AI v2.

### Phase 5 — Remove deprecated fields

Drop the old `identifiers.case` field, the old `paging.limit`/`paging.top` fields, and the
old `ordering` section. Update all YAML files.

---

## 5. H2 Validation Notes

The H2 dialect was validated against the official H2 documentation
(https://h2database.com/html/grammar.html, functions.html, datatypes.html, commands.html).
Key findings that informed this schema:

### Errors found in current `h2.yml`

| Issue | Current value | Correct value |
|-------|---------------|---------------|
| `identifiers.case` | `AS_IS` | `UPPER` (H2 converts unquoted to uppercase) |
| `REGEXP_INSTR` function | Listed | Does not exist in H2 |
| `NOW()` function | Listed | Does not exist in H2 |
| Regex flags | `[i,c,m,n,u,s,x]` | Only `[i,c,n,m]` supported; others throw exceptions |
| `POSITION` syntax | `POSITION(substr IN str)` | H2 uses `POSITION(substr, str)` with comma |
| `REGEXP_SUBSTR` signature | Missing position/group args | Full signature has 6 parameters |
| `CONVERT` | Not marked deprecated | H2 docs: "deprecated, use CAST" |
| `CURRENT_TIME` return | `TIME` | `TIME WITH TIME ZONE` |
| `CURRENT_TIMESTAMP` return | `TIMESTAMP` | `TIMESTAMP WITH TIME ZONE` |
| `paging.top` | Listed as supported | Not in official H2 SELECT grammar |

### Coverage gap

The current `h2.yml` covers ~25% of the `MillDialectDescriptor` shape. The H2 documentation
can fill ~75% of the total gap. The remaining ~25% (JDBC-specific limits, some feature flags
like `div-is-floordiv`, driver-level capabilities) requires empirical testing.

---

## 6. Function Category Reference

| Category | Description | Example functions |
|----------|-------------|-------------------|
| `strings` | String manipulation | CHAR_LENGTH, UPPER, LOWER, SUBSTRING, CONCAT, TRIM, REPLACE, POSITION |
| `regex` | Regular expression | REGEXP_LIKE, REGEXP_REPLACE, REGEXP_SUBSTR |
| `numerics` | Core numeric | ABS, ROUND, CEIL, FLOOR, MOD, POWER, TRUNC |
| `math` | Extended math (trig, log) | SIGN, SQRT, LN, LOG, LOG10, EXP, SIN, COS, TAN, ASIN, ACOS, ATAN, ATAN2, DEGREES, RADIANS, PI |
| `aggregates` | Core aggregates | COUNT, SUM, AVG, MIN, MAX, LISTAGG |
| `statistics` | Statistical aggregates | STDDEV_SAMP, STDDEV_POP, VAR_SAMP, VAR_POP, COVAR_SAMP, COVAR_POP, CORR |
| `window` | Window / analytical | ROW_NUMBER, RANK, DENSE_RANK, PERCENT_RANK, CUME_DIST, NTILE, LAG, LEAD, FIRST_VALUE, LAST_VALUE, NTH_VALUE |
| `dates-times` | Date/time functions | CURRENT_DATE, CURRENT_TIMESTAMP, LOCALTIME, LOCALTIMESTAMP, EXTRACT, DATEADD, DATEDIFF, DATE_TRUNC |
| `conditionals` | Conditional expressions | COALESCE, NULLIF, GREATEST, LEAST, NVL2, CASE, DECODE |

---

## 7. Operator Category Reference

| Category | Description | Example operators |
|----------|-------------|-------------------|
| `equality` | Equality | `=` |
| `inequality` | Inequality | `<>`, `!=` |
| `comparison` | Comparison | `>`, `>=`, `<`, `<=` |
| `arithmetic` | Arithmetic | `+`, `-`, `*`, `/`, `%` |
| `logical` | Logical | `NOT`, `AND`, `OR` |
| `null-checks` | Null testing | `IS NULL`, `IS NOT NULL` |
| `null-safe` | Null-safe comparison | `IS NOT DISTINCT FROM` |
| `set` | Set membership | `IN`, `NOT IN` |
| `like` | Pattern matching | `LIKE`, `ILIKE`, `SIMILAR TO` |
| `between` | Range | `BETWEEN` |
| `regex` | Regex matching | `REGEXP`, `~`, `~*` (dialect-specific) |
| `bitwise` | Bitwise operations | `BITAND`, `BITOR`, `BITXOR`, `BITNOT`, `LSHIFT`, `RSHIFT` |
| `casting` | Type casting | `CAST`, `::`, `CONVERT`, `TRY_CAST` |

---

## 8. Representability Matrix (Section 3)

This matrix tracks whether the YAML schema can *represent* each requirement class from
`py-sql-dialect-plan.md` section 3. It is representability-first: values may be unknown or
conservative, but the shape must exist.

| Section 3 aspect | Represented by YAML sections | Status |
|---|---|---|
| Identifier quote/case/storage/length | `identifiers` | representable |
| Schema/catalog topology | `catalog-schema` | representable |
| String quoting/concat/escape | `literals.strings` | representable |
| Boolean/date/time/interval literals | `literals` | representable |
| Join capability + join syntax | `joins`, `feature-flags` | representable |
| Null ordering and nulls-first/last support | `null-sorting` | representable |
| Paging forms and no-limit sentinel | `paging` | representable |
| Feature toggles (CTE, set ops, lateral, qualify, etc.) | `feature-flags` | representable |
| Predicate/operator families (like, between, regex, bitwise, casting) | `operators`, `feature-flags` | representable |
| Function coverage (scalar/aggregate/window/statistics/math) | `functions` | representable |
| Read-only behavior | top-level `read-only` | representable |
| Division semantics (`div-is-floordiv`) | `feature-flags.div-is-floordiv` | representable |
| Parameter binding style | top-level `paramstyle` | representable |
| Type-system and JDBC type metadata | `type-info` | representable |
| Result set capabilities | `result-set` | representable |
| Transaction semantics | `transactions` | representable |
| Limits (`max_*`) | `limits` | representable |
| SQL keywords/system functions/search escape | `string-properties` | representable |

---

## 9. Gap-to-Schema Matrix (Section 4)

This matrix maps section 4 gaps from `py-sql-dialect-plan.md` to schema fields.

| Gap id | Gap summary | YAML fields | Status |
|---|---|---|---|
| B1 | Identifier quoted/unquoted storage rules | `identifiers.unquoted-storage`, `identifiers.quoted-storage`, mixed-case flags, quotes | representable |
| B2 | Schema/catalog topology | `catalog-schema.*` | representable |
| B3 | SQL capability flags | `feature-flags.*` | representable |
| B4 | Limits metadata | `limits.*` | representable |
| B5 | Transaction semantics | `transactions.*` | representable |
| B6 | String/keyword properties | `string-properties.*`, `literals.strings.*` | representable |
| B7 | Type info enrichment | `type-info[*]` | representable |
| B8 | ResultSet capability flags | `result-set.*` | representable |
| 11 | SEMI/ANTI join capability | `feature-flags.supports-semi-anti-join` | representable |
| 12 | LATERAL join capability | `feature-flags.supports-lateral` | representable |
| 16 | `no_limit_value` support | `paging.no-limit-value` | representable |
| 24 | Bitwise operators | `operators.bitwise`, relevant `feature-flags` | representable |
| 27 | Extended math functions | `functions.math` | representable |
| 29 | Statistical aggregates | `functions.statistics` | representable |
| 30 | Window functions | `functions.window`, `feature-flags.supports-window-functions` | representable |
| 33 | CTE support | `feature-flags.supports-cte` | representable |
| 34 | QUALIFY support | `feature-flags.supports-qualify` | representable |
| 35 | UNION/INTERSECT/EXCEPT | `feature-flags.supports-union*`, `supports-intersect`, `supports-except` | representable |
| 36 | Read-only flag | top-level `read-only` | representable |
| 37 | `div_is_floordiv` | `feature-flags.div-is-floordiv` | representable |
| 38 | Paramstyle | top-level `paramstyle` | representable |

---

## 10. Representability Rules

- Unknown or unverified values are allowed where appropriate (for example nullable booleans or
  conservative defaults), but missing *shape* is not allowed for required capability areas.
- Every requirement from section 3 and every gap id from section 4 must map to at least one YAML
  field in this schema.
- Accuracy validation is a separate phase; representability is the required baseline.

---

## 11. Normative Field Rules

This section defines what is required versus optional for schema authoring in `core/mill-sql`.

### 11.1 Required top-level sections

The following top-level fields are required for every dialect file:

- `id`
- `name`
- `read-only`
- `paramstyle`
- `identifiers`
- `catalog-schema`
- `transactions`
- `limits`
- `null-sorting`
- `result-set`
- `feature-flags`
- `string-properties`
- `literals`
- `joins`
- `paging`
- `operators`
- `functions`
- `type-info` (can be empty list)

### 11.2 Optional top-level sections

- `notes`

### 11.3 Required function categories

`functions` must contain all categories below (empty lists are allowed when unknown or unsupported):

- `strings`
- `regex`
- `numerics`
- `math`
- `aggregates`
- `statistics`
- `window`
- `dates-times`
- `conditionals`

### 11.4 Required operator categories

`operators` should provide all categories used by consumer mappings. Categories can be empty lists
if unknown or unsupported:

- `equality`
- `inequality`
- `comparison`
- `arithmetic`
- `logical`
- `null-checks`
- `null-safe`
- `set`
- `like`
- `between`
- `regex`
- `bitwise`
- `casting`

### 11.5 Defaulting policy

- Booleans in `feature-flags`: default to conservative `false` when value is unknown.
- Tristate capability (`div-is-floordiv`): nullable (`true`/`false`/`null`).
- Integer limits: `0` means unknown or unlimited unless explicitly documented otherwise.
- `type-info`: empty list is valid in bootstrap/migration phase.

---

## 12. Authoring Rules

### 12.1 Naming and key conventions

- Use kebab-case YAML keys.
- Dialect ids are uppercase logical identifiers (`CALCITE`, `POSTGRES`, `H2`, `MYSQL`).
- Function and operator names should follow engine-native names and syntax.

### 12.2 Allowed enum/value sets

- `paramstyle`: `qmark`, `numeric`, `named`, `format`, `pyformat`
- `identifiers.unquoted-storage`: `UPPER`, `LOWER`, `AS_IS`
- `identifiers.quoted-storage`: `UPPER`, `LOWER`, `AS_IS`
- `paging.styles[*].type`: `standard`, `compat`
- `literals.strings.escape`: `STANDARD`, `BACKSLASH`, `DOUBLING`

### 12.3 Unknown/unverified values

- Prefer representable placeholders over omitted shape.
- Use conservative values in `feature-flags`.
- Use `null` only where nullability is semantically meaningful (for example `div-is-floordiv`).
- Document uncertainty in `notes` or field-level notes.

### 12.4 Deprecation policy (legacy schema)

Legacy fields are not part of the target schema and should not be used in new files:

- `identifiers.case`
- `paging.limit`
- `paging.top`
- `ordering`
- `operators.arithmetic_operators`
- `operators.null_checks`
- `functions.dates_times`

When migrating legacy files, map to target fields:

- `identifiers.case` -> `unquoted-storage` + `quoted-storage`
- `paging.limit/top` -> `paging.styles`
- `operators.arithmetic_operators` -> `operators.arithmetic`
- `operators.null_checks` -> `operators.null-checks`
- `functions.dates_times` -> `functions.dates-times`

---

## 13. Complete Reference Examples (Current Scope)

The canonical complete examples for WI-015/WI-016 scope are the full YAML files below:

- `core/mill-sql/src/main/resources/sql/dialects/calcite/calcite.yml`
- `core/mill-sql/src/main/resources/sql/dialects/postgres/postgres.yml`
- `core/mill-sql/src/main/resources/sql/dialects/h2/h2.yml`
- `core/mill-sql/src/main/resources/sql/dialects/mysql/mysql.yml`

These files are the source of truth for full-shape examples and are expected to remain schema-valid
as migration continues. Documentation snippets in this file are illustrative; the files above are
authoritative examples.

---

## 14. Consumer Mapping Notes

### 14.1 Kotlin typed runtime model (`core/mill-sql`)

- YAML is deserialized into strict typed Kotlin data classes.
- Loader validation enforces required sections and category presence.
- Registry exposes dialect descriptors by id for server/runtime consumers.

### 14.2 gRPC/HTTP dialect contracts

- Contract shape mirrors the typed runtime model.
- Handshake capability (`supports_dialect`) gates dialect retrieval.
- Transport responses should preserve semantic parity (same capabilities and categories).

### 14.3 AI dialect consumer

- AI prompt generation reads dialect metadata from typed runtime model.
- `feature-flags`, `functions`, and `identifiers` are primary prompt-shaping inputs.

### 14.4 Python consumer

- Python fetches dialect metadata remotely over gRPC/HTTP.
- Local behavior should prefer server-provided dialect metadata with fallback only when capability
  is unavailable.

---

## 15. Change Management and Review Checklist

### 15.1 Versioning approach

- Treat schema changes as versioned contract changes.
- Backward-compatible additions: new optional fields/categories.
- Breaking changes: key renames/removals, required-field additions.

### 15.2 Required updates in a schema change PR

When schema changes:

1. Update this document (normative rules and examples).
2. Update typed Kotlin model and validator in `core/mill-sql`.
3. Update migrated reference dialect YAMLs if impacted.
4. Update transport contract mappings if impacted.
5. Update AI/Python consumer mappings if impacted.
6. Add/adjust tests for parser and validation behavior.

### 15.3 Documentation quality gate

- Every section 3 requirement remains mapped.
- Every section 4 gap id remains mapped.
- Reference example files remain present and parseable.

---

## 16. Complete YAML Element Dictionary (normative)

This section describes every YAML element currently supported by the strict Kotlin model in
`core/mill-sql`.

### 16.1 Top-level elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `id` | string | yes | Stable dialect id (`H2`, `POSTGRES`, `CALCITE`, `MYSQL`). |
| `name` | string | yes | Human-friendly dialect name. |
| `read-only` | boolean | yes | Indicates whether dialect should be treated as read-only. |
| `paramstyle` | string | yes | Parameter style (`qmark`, `numeric`, `named`, `format`, `pyformat`). |
| `notes` | list<string> | no | Free-form authoring notes and caveats. |
| `identifiers` | object | yes | Identifier quoting/casing/name rules. |
| `catalog-schema` | object | yes | Catalog/schema topology and naming terms. |
| `transactions` | object | yes | Transaction support and DDL transactional behavior. |
| `limits` | object | yes | Metadata max-values (`DatabaseMetaData.getMax*`). |
| `null-sorting` | object | yes | Null ordering defaults and explicit clause support. |
| `result-set` | object | yes | ResultSet type/concurrency capability flags. |
| `feature-flags` | map<string, boolean|null> | yes, non-empty | Capability booleans and tri-state experimental flags. |
| `string-properties` | object | yes | Search escape and keyword/function string lists. |
| `literals` | object | yes | Literal syntax for string, boolean, null, date/time, interval. |
| `joins` | object | yes | Join keywords and join-clause behavior. |
| `paging` | object | yes | Limit/offset syntax styles and no-limit sentinel. |
| `operators` | map<string, list<object>> | yes, non-empty | Operator catalog grouped by category. |
| `functions` | map<string, list<object>> | yes, non-empty | Function catalog grouped by category. |
| `type-info` | list<object> | yes (empty allowed) | JDBC-like per-type metadata rows. |

### 16.2 `identifiers` elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `identifiers.quote.start` | string | yes | Opening identifier quote token. |
| `identifiers.quote.end` | string | yes | Closing identifier quote token. |
| `identifiers.alias-quote.start` | string | yes | Opening quote token for aliases. |
| `identifiers.alias-quote.end` | string | yes | Closing quote token for aliases. |
| `identifiers.escape-quote` | string | yes | Escape sequence for embedded quote characters. |
| `identifiers.unquoted-storage` | string | yes | Storage casing for unquoted identifiers (`UPPER/LOWER/AS_IS`). |
| `identifiers.quoted-storage` | string | yes | Storage casing for quoted identifiers (`UPPER/LOWER/AS_IS`). |
| `identifiers.supports-mixed-case` | boolean | yes | Mixed-case preservation/support for unquoted identifiers. |
| `identifiers.supports-mixed-case-quoted` | boolean | yes | Mixed-case preservation/support for quoted identifiers. |
| `identifiers.max-length` | int | yes | Maximum identifier length. |
| `identifiers.extra-name-characters` | string | yes | Additional valid identifier characters. |
| `identifiers.use-fully-qualified-names` | boolean | yes | Preference for fully-qualified references. |

### 16.3 `catalog-schema` elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `catalog-schema.supports-schemas` | boolean | yes | Schema concept exists. |
| `catalog-schema.supports-catalogs` | boolean | yes | Catalog/database concept exists. |
| `catalog-schema.catalog-separator` | string | yes | Token between catalog/schema/object components. |
| `catalog-schema.catalog-at-start` | boolean | yes | Catalog appears first in qualified names. |
| `catalog-schema.schema-term` | string | yes | Product-specific name for schema concept. |
| `catalog-schema.catalog-term` | string | yes | Product-specific name for catalog concept. |
| `catalog-schema.procedure-term` | string | yes | Product-specific name for procedure concept. |
| `catalog-schema.schemas-in-dml` | boolean | yes | Schema qualification in DML. |
| `catalog-schema.schemas-in-procedure-calls` | boolean | yes | Schema qualification in procedure calls. |
| `catalog-schema.schemas-in-table-definitions` | boolean | yes | Schema qualification in DDL table defs. |
| `catalog-schema.schemas-in-index-definitions` | boolean | yes | Schema qualification in index defs. |
| `catalog-schema.schemas-in-privilege-definitions` | boolean | yes | Schema qualification in privilege defs. |
| `catalog-schema.catalogs-in-dml` | boolean | yes | Catalog qualification in DML. |
| `catalog-schema.catalogs-in-procedure-calls` | boolean | yes | Catalog qualification in procedure calls. |
| `catalog-schema.catalogs-in-table-definitions` | boolean | yes | Catalog qualification in table defs. |
| `catalog-schema.catalogs-in-index-definitions` | boolean | yes | Catalog qualification in index defs. |
| `catalog-schema.catalogs-in-privilege-definitions` | boolean | yes | Catalog qualification in privilege defs. |

### 16.4 `transactions`, `limits`, `null-sorting`, `result-set`

| YAML path prefix | Required | Description |
|---|---|---|
| `transactions.*` | yes | `supported`, `default-isolation`, multi-transaction support, and DDL-in-transaction behavior fields. |
| `limits.*` | yes | 18 numeric/boolean max-limit fields (all currently modeled fields are required). |
| `null-sorting.*` | yes | Four default null-order booleans + `supports-nulls-first/last`. |
| `result-set.*` | yes | Five ResultSet capability flags (`forward-only`, scroll modes, concurrency modes). |

### 16.5 `string-properties` elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `string-properties.search-string-escape` | string | yes | Escape token for pattern search text. |
| `string-properties.sql-keywords` | string | yes | Comma-separated non-standard SQL keywords. |
| `string-properties.system-functions` | string | yes | Comma-separated built-in system function names. |

### 16.6 `literals` elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `literals.strings.quote` | string | yes | String literal delimiter. |
| `literals.strings.concat` | string | yes | String concatenation operator/function token. |
| `literals.strings.escape` | string | yes | Escape strategy identifier (`STANDARD`, `BACKSLASH`, `DOUBLING`). |
| `literals.strings.note` | string | no | Free-form note about string literal behavior. |
| `literals.booleans` | list<string> | yes | Canonical boolean literal tokens. |
| `literals.null` | string | yes | Null literal token. |
| `literals.dates-times.date.syntax` | string | yes | DATE literal syntax template. |
| `literals.dates-times.date.quote` | string | yes | Date literal quote token. |
| `literals.dates-times.date.pattern` | string | yes | Date value pattern hint. |
| `literals.dates-times.date.notes` | list<string> | no | Date literal notes. |
| `literals.dates-times.time.syntax` | string | yes | TIME literal syntax template. |
| `literals.dates-times.time.quote` | string | yes | Time literal quote token. |
| `literals.dates-times.time.pattern` | string | yes | Time value pattern hint. |
| `literals.dates-times.time.notes` | list<string> | no | Time literal notes. |
| `literals.dates-times.timestamp.syntax` | string | yes | TIMESTAMP literal syntax template. |
| `literals.dates-times.timestamp.quote` | string | yes | Timestamp literal quote token. |
| `literals.dates-times.timestamp.pattern` | string | yes | Timestamp value pattern hint. |
| `literals.dates-times.timestamp.notes` | list<string> | no | Timestamp literal notes. |
| `literals.dates-times.interval.supported` | boolean | yes | Interval literal support flag. |
| `literals.dates-times.interval.style` | string | yes | Interval syntax/style family. |
| `literals.dates-times.interval.notes` | list<string> | no | Interval literal notes. |

### 16.7 `joins` elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `joins.style` | string | yes | Join style family (for example `explicit`). |
| `joins.cross-join.enabled` | boolean | no | Explicit enablement for CROSS JOIN. |
| `joins.cross-join.keyword` | string | no | Join keyword token/string. |
| `joins.cross-join.require-on` | boolean | no | Whether `ON` is required for this join kind. |
| `joins.cross-join.null-safe` | boolean | no | Whether join kind is null-safe by semantics. |
| `joins.cross-join.notes` | string | no | Free-form note. |
| `joins.inner-join.*` | mixed | no/yes | Same shape as `cross-join`; values depend on dialect authoring. |
| `joins.left-join.*` | mixed | no/yes | Same shape as `cross-join`; values depend on dialect authoring. |
| `joins.right-join.*` | mixed | no/yes | Same shape as `cross-join`; values depend on dialect authoring. |
| `joins.full-join.*` | mixed | no/yes | Same shape as `cross-join`; values depend on dialect authoring. |
| `joins.on-clause.keyword` | string | yes | `ON` clause keyword token. |
| `joins.on-clause.require-condition` | boolean | yes | Whether an ON predicate is mandatory. |

### 16.8 `paging` elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `paging.styles` | list<object> | yes, non-empty | Ordered preferred paging syntaxes. |
| `paging.styles[].syntax` | string | yes | Syntax template using placeholders (`{n}`, `{m}`). |
| `paging.styles[].type` | string | yes | Style type (`standard` or `compat`). |
| `paging.styles[].deprecated` | boolean | no | Marks style as deprecated for new generation. |
| `paging.offset` | string | yes | Offset syntax template. |
| `paging.no-limit-value` | string | no | Sentinel for "no explicit limit" when required by dialect. |

### 16.9 `operators` category + entry elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `operators.<category>` | list<object> | yes | Operator list for category (`equality`, `comparison`, etc.). |
| `operators.<category>[].symbol` | string | yes | Canonical operator symbol/token. |
| `operators.<category>[].syntax` | string | no | Syntax template form. |
| `operators.<category>[].description` | string | no | Human-readable semantics. |
| `operators.<category>[].supported` | boolean | no | Optional per-entry support override. |
| `operators.<category>[].deprecated` | boolean | no | Marks operator form as deprecated. |

### 16.10 `functions` category + entry elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `functions.<category>` | list<object> | yes | Function list for required category. |
| `functions.<category>[].name` | string | yes | Canonical function name. |
| `functions.<category>[].synonyms` | list<string> | no | Alternate names accepted by dialect. |
| `functions.<category>[].return.type` | string | yes | Return type token (`ANY` allowed for polymorphic). |
| `functions.<category>[].return.nullable` | boolean | yes | Return nullability. |
| `functions.<category>[].syntax` | string | yes | Syntax template with argument placeholders. |
| `functions.<category>[].args` | list<object> | no | Function argument definitions. |
| `functions.<category>[].args[].name` | string | yes | Argument name. |
| `functions.<category>[].args[].type` | string | yes | Argument type token. |
| `functions.<category>[].args[].required` | boolean | yes | Mandatory argument indicator. |
| `functions.<category>[].args[].variadic` | boolean | no | Variadic argument indicator. |
| `functions.<category>[].args[].multi` | boolean | no | Multi-valued argument semantics. |
| `functions.<category>[].args[].min` | int | no | Lower cardinality bound for variadic args. |
| `functions.<category>[].args[].max` | int | no | Upper cardinality bound for variadic args. |
| `functions.<category>[].args[].enum` | list<string> | no | Allowed value set for argument. |
| `functions.<category>[].args[].default` | string | no | Default value literal/expression. |
| `functions.<category>[].args[].notes` | string | no | Per-argument note. |
| `functions.<category>[].notes` | list<string> | no | Per-function notes/caveats. |

### 16.11 `type-info` entry elements

| YAML path | Type | Required | Description |
|---|---|---|---|
| `type-info[].sql-name` | string | yes | SQL type name. |
| `type-info[].jdbc-type-code` | int | yes | JDBC type code integer. |
| `type-info[].precision` | int | no | Numeric/char precision. |
| `type-info[].literal-prefix` | string | no | Literal prefix for this type. |
| `type-info[].literal-suffix` | string | no | Literal suffix for this type. |
| `type-info[].case-sensitive` | boolean | no | Case-sensitive comparison/storage. |
| `type-info[].searchable` | int | no | Searchability level (JDBC conventions). |
| `type-info[].unsigned` | boolean | no | Unsigned numeric semantics. |
| `type-info[].fixed-prec-scale` | boolean | no | Fixed precision/scale indicator. |
| `type-info[].auto-increment` | boolean | no | Auto-increment support indicator. |
| `type-info[].minimum-scale` | int | no | Minimum supported scale. |
| `type-info[].maximum-scale` | int | no | Maximum supported scale. |
| `type-info[].num-prec-radix` | int | no | Numeric precision radix (usually 2 or 10). |
