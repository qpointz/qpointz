# SQL Dialect YAML Schema — Design & Reference

This document defines the target YAML schema for Mill SQL dialect descriptor files.
It replaces the current ad-hoc structure with a comprehensive schema that serves all
four downstream consumers: SQLAlchemy, JDBC, ibis, and AI NL-to-SQL.

**Related documents:**
- `py-sql-dialect-plan.md` — Phase 9 plan (gap analysis, work items)
- `py-implementation-plan.md` — Parent plan (Phases 0–11)

---

## 1. Context & Motivation

### Current state

The existing YAML files (`core/mill-core/src/main/resources/sql/dialects/{dialect}/{dialect}.yml`)
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

### Java deserialization

The YAML is deserialized into Java records via Jackson (`SqlDialectSpec.java` in
`core/mill-core/.../sql/dialect/`). All new sections use `Optional<>` wrappers so existing
YAML files that lack them will deserialize cleanly during incremental migration.

### Design decisions

1. **Kebab-case keys** — consistent with existing YAML convention; mapped via `@JsonProperty`
2. **`Optional<>` for all new fields** — backward-compatible incremental adoption
3. **`feature-flags` is `Map<String, Boolean>`** — extensible without Java code changes
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

Stored as `Map<String, Boolean>`. `null` means "unknown, needs empirical testing."
Consumers should treat absent keys as `false` (conservative default).

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
The old `limit` and `top` fields are kept as `Optional` in Java for backward compatibility
during migration.

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

## 3. Java Record Structure

```java
public record SqlDialectSpec(
    @JsonProperty("id")                String id,
    @JsonProperty("name")              String name,
    @JsonProperty("read-only")         Optional<Boolean> readOnly,
    @JsonProperty("paramstyle")        Optional<String> paramstyle,
    @JsonProperty("notes")             Optional<List<String>> notes,
    @JsonProperty("identifiers")       Identifiers identifiers,
    @JsonProperty("catalog-schema")    Optional<CatalogSchema> catalogSchema,
    @JsonProperty("transactions")      Optional<Transactions> transactions,
    @JsonProperty("limits")            Optional<Limits> limits,
    @JsonProperty("null-sorting")      Optional<NullSorting> nullSorting,
    @JsonProperty("result-set")        Optional<ResultSetCaps> resultSet,
    @JsonProperty("feature-flags")     Optional<Map<String, Boolean>> featureFlags,
    @JsonProperty("string-properties") Optional<StringProperties> stringProperties,
    @JsonProperty("literals")          Literals literals,
    @JsonProperty("joins")             Joins joins,
    @JsonProperty("paging")            Paging paging,
    @JsonProperty("operators")         Map<String, List<OperatorEntry>> operators,
    @JsonProperty("functions")         Map<String, List<FunctionEntry>> functions,
    @JsonProperty("type-info")         Optional<List<TypeInfo>> typeInfo
) {}
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

### Phase 1 — Update Java records (non-breaking)

Add all new records and `Optional<>` fields to `SqlDialectSpec`. Existing YAML files
continue to deserialize because all new fields are optional.

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
