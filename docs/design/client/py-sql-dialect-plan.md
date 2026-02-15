# Mill SQL Dialect Foundation — Design & Tracking

This document is the authoritative design and tracking document for the Mill SQL Dialect
Foundation (Phase 9 of the `mill-py` refactoring). It covers the dialect descriptor model,
gap analysis across all four consumers, incremental delivery strategy, and work item tracking.

**Branch**: `refactor/py-client`

Related documents:
- `py-implementation-plan.md` — Parent plan (Phases 0–11)
- `py-cold-start.md` — Codebase analysis and cold-start guide
- `py-sql-dialect-report.md` — Live validation findings (produced by Slice 9B)

---

## 1. Goal

Build the shared `mill/sql/` module that captures Mill's SQL dialect rules and canonical
type mappings. This foundation is consumed by **four** downstream technologies:

| # | Consumer | Location | What it needs from the dialect |
|---|----------|----------|-------------------------------|
| 1 | **SQLAlchemy** (Phase 11) | `mill/sqlalchemy/`, `mill/dbapi.py` | Identifier rules, schema/catalog topology, transactions, feature flags, type mapping, paramstyle |
| 2 | **Java JDBC driver** | `clients/mill-jdbc-driver/` | Full `DatabaseMetaData` population: ~130 methods including limits, type info, ResultSet caps |
| 3 | **ibis backend** (Phase 10) | `mill/ibis/` | Function catalog (window, stats, math), feature flags (CTE, set ops), type mapping |
| 4 | **AI NL-to-SQL** | `ai/mill-ai-v1-core/`, `ai/mill-ai-v2/` | Identifier rules, function catalog, feature flags — rendered into LLM system prompts |

---

## 2. Approach — Server-Driven Dialect

Rather than hard-coding Calcite rules, the long-term design provides dialect metadata from
the server via a `GetDialect` RPC. The `Handshake` response will include a flag indicating
whether the server supports dialect introspection.

Phase 9 builds the *client-side consumer* — the `mill/sql/` module — which initially
hard-codes the Calcite dialect as a default, but is structured so it can be hydrated from a
future `GetDialect` response. When the server-side proto is implemented (separate server work
item), the client will prefer the server-provided descriptor over the built-in default.

```
┌──────────────────────────────────────────────────────────────────┐
│  MillDialectDescriptor  (shared dialect contract)                │
│  Python: mill/sql/dialect.py + types.py                          │
│  Proto:  GetDialect RPC (future)                                 │
│  YAML:   core/mill-core/.../sql/dialects/*.yml  (source of truth)│
└──┬───────────────────────────────────┬───────────────────────────┘
   │                                   │
   │  9A (P0+P1: structural)           │  9B (P2+P3: dialect tester)
   │  ──────────────────────►          │  ───────────────────────────►
   │                                   │
   │                                   │  DialectTester → ~80 SQL queries
   │                                   │  parse_sql() → plan = ✅ / error = ❌
   │                                   │  DialectReport → feature_flags()
   │                                   │                → to_markdown()
   ▼                                   ▼
 Phase 11: DBAPI + SA POC            Phase 10: ibis backend POC
 Java JDBC driver refactoring        AI NL-to-SQL enrichment
 AI NL-to-SQL prompt foundation        (window funcs, stats, math)
```

---

## 3. Framework Requirements Analysis

Before defining the dialect descriptor shape, we analysed the minimum information SQLAlchemy,
ibis, JDBC, and AI need from a dialect. Cross-referencing against the existing server-side
YAML dialect files (`core/mill-core/src/main/resources/sql/dialects/calcite/calcite.yml`)
yielded the following coverage map.

### 3.1 Aspect-by-aspect breakdown

| # | Aspect | SQLAlchemy needs | ibis / sqlglot needs | In server YAML? | Gap? |
|---|--------|------------------|----------------------|------------------|------|
| 1 | **Identifier quote char** | `IdentifierPreparer(initial_quote, final_quote, escape_quote)` | `sqlglot.Dialect.IDENTIFIER_START/END` | YES (`identifiers.quote.start/end`) | — |
| 2 | **Case normalization** | `requires_name_normalize` (bool) | `sqlglot.Dialect.NORMALIZATION_STRATEGY` | YES (`identifiers.case`: UPPER/AS_IS) | — |
| 3 | **Max identifier length** | `max_identifier_length` (int) | Not needed | NO | Minor |
| 4 | **Schema support** | `supports_schemas` (bool) | Backend `list_tables(schema=)` | Implicit | Minor |
| 5 | **String quoting** | Compiler uses `'` | `sqlglot.Dialect.QUOTE_START/END` | YES (`literals.strings.quote`) | — |
| 6 | **String concat op** | `concat_op` | `sqlglot.DPIPE_IS_STRING_CONCAT` | YES (`literals.strings.concat`) | — |
| 7 | **Boolean literals** | `supports_native_boolean` | Not directly | YES | — |
| 8 | **Date/time literal syntax** | Type rendering | `sqlglot.DATE_FORMAT`, `TIME_FORMAT` | YES (detailed) | — |
| 9 | **Interval support** | Not critical | sqlglot temporal ops | YES | — |
| 10 | **Join types (INNER/LEFT/RIGHT/FULL/CROSS)** | SA generates natively | ibis generates via sqlglot | YES (all with enabled flags) | — |
| 11 | **SEMI/ANTI join** | Not standard | `sqlglot.SUPPORTS_SEMI_ANTI_JOIN` | NO | **Gap** |
| 12 | **LATERAL join** | Not standard | ibis unnest/correlated | NO | **Gap** |
| 13 | **NULLS FIRST/LAST** | Not critical | `sqlglot.NULL_ORDERING` | YES | — |
| 14 | **ORDER BY alias/label** | `supports_simple_order_by_label` | Not directly | NO | Minor |
| 15 | **LIMIT/OFFSET syntax** | `limit_clause()` override | sqlglot per dialect | YES | — |
| 16 | **`no_limit_value`** | Not needed | ibis compiler flag | NO | Minor |
| 17 | **FETCH FIRST (ANSI)** | SA for ANSI mode | sqlglot per dialect | NO | Minor |
| 18 | **Comparison / arithmetic / logical ops** | SA natively | sqlglot BINARY_INFIX_OPS | YES | — |
| 19 | **LIKE / ILIKE** | SA natively | ibis `StringSQLLike`/`StringSQLILike` | YES (ILIKE varies) | — |
| 20 | **BETWEEN / IN / IS NULL** | SA natively | ibis natively | YES | — |
| 21 | **IS NOT DISTINCT FROM** | `supports_is_distinct_from` | ibis `IdenticalTo` | YES | — |
| 22 | **CAST / TRY_CAST** | SA `CAST` | ibis both | YES (with supported flags) | — |
| 23 | **Regex operators** | Not standard SA | ibis `RegexSearch/Extract/Replace` | YES | — |
| 24 | **Bitwise operators** | Not standard SA | ibis `BitwiseAnd/Or/Xor/Shift` | NO | **Gap** |
| 25 | **String functions** | `func.*` passthrough | ibis ~20 string ops | YES (detailed, with args) | — |
| 26 | **Core numeric functions** | `func.*` passthrough | ibis `ABS`, `ROUND`, `CEIL`, etc. | YES (ROUND, TRUNC, CEIL, FLOOR, ABS, POWER, MOD) | — |
| 27 | **Extended math functions** | Not standard SA | ibis: `SIGN`, `SQRT`, `LN`, `LOG`, `LOG2`, `EXP`, trig (`SIN`/`COS`/`TAN`/`ASIN`/`ACOS`/`ATAN`/`ATAN2`), `DEGREES`, `RADIANS` | NO | **Gap** |
| 28 | **Core aggregates** | `func.count/sum/avg` | ibis COUNT/SUM/AVG/MIN/MAX | YES | — |
| 29 | **Statistical aggregates** | Not standard SA | ibis: `STDDEV_SAMP/POP`, `VAR_SAMP/POP`, `COVAR_SAMP/POP`, `CORR`, `APPROX_COUNT_DISTINCT` | NO | **Gap** |
| 30 | **Window functions** | `over()` | ibis: `ROW_NUMBER`, `RANK`, `DENSE_RANK`, `LAG`, `LEAD`, `FIRST_VALUE`, `LAST_VALUE`, `NTILE`, `CUME_DIST`, `PERCENT_RANK`, `NTH_VALUE` | NO | **Major gap** |
| 31 | **Date/time functions** | `extract`, `func.now` | ibis EXTRACT, DATE_TRUNC, etc. | YES | — |
| 32 | **Conditional functions** | `case`, `coalesce` | ibis COALESCE, NULLIF, CASE, GREATEST, LEAST | YES | — |
| 33 | **CTE support** | `cte_follows_insert` | ibis `visit_CTE` | NO | **Gap** |
| 34 | **QUALIFY support** | Not in SA | ibis `supports_qualify` | NO | Minor |
| 35 | **UNION / INTERSECT / EXCEPT** | SA generates | ibis generates | NO | **Gap** |
| 36 | **Read-only flag** | SA needs to know no DDL/DML | Not directly | NO | **Gap** |
| 37 | **`div_is_floordiv`** | SA flag | sqlglot `TYPED_DIVISION` | NO | **Gap** |
| 38 | **Parameter binding style** | SA DBAPI `paramstyle` | Not needed by ibis | NO | **Gap** (client-side) |

### 3.2 Type mapping — fully covered

The server-side `CalciteTypeMapper` in
`source/mill-source-calcite/src/main/kotlin/.../CalciteTypeMapper.kt` already defines the
complete Mill-to-Calcite-SQL-type mapping for all 16 logical types:

| Mill Logical Type | Calcite SqlTypeName | SQL Keyword | Prec/Scale |
|---|---|---|---|
| `TINY_INT` | `TINYINT` | `TINYINT` | no |
| `SMALL_INT` | `SMALLINT` | `SMALLINT` | no |
| `INT` | `INTEGER` | `INTEGER` | no |
| `BIG_INT` | `BIGINT` | `BIGINT` | no |
| `FLOAT` | `FLOAT` | `FLOAT` | ignored |
| `DOUBLE` | `DOUBLE` | `DOUBLE` | ignored |
| `BOOL` | `BOOLEAN` | `BOOLEAN` | no |
| `STRING` | `VARCHAR` | `VARCHAR(n)` | precision = length |
| `BINARY` | `VARBINARY` | `VARBINARY(n)` | precision = length |
| `DATE` | `DATE` | `DATE` | no |
| `TIME` | `TIME` | `TIME` | no |
| `TIMESTAMP` | `TIMESTAMP` | `TIMESTAMP` | no |
| `TIMESTAMP_TZ` | `TIMESTAMP_WITH_LOCAL_TIME_ZONE` | `TIMESTAMP WITH LOCAL TIME ZONE` | no |
| `INTERVAL_DAY` | `INTERVAL_DAY` | `INTERVAL DAY` | no |
| `INTERVAL_YEAR` | `INTERVAL_YEAR` | `INTERVAL YEAR` | no |
| `UUID` | `VARCHAR` | `VARCHAR` | treated as string |

Source: `DatabaseType` record carries `(logicalType, nullable, precision, scale)`.

---

## 4. Gap Analysis

### 4.1 Combined gap priority (4 consumers)

| # | Gap | SA | JDBC | ibis | AI | Overall |
|---|-----|:--:|:----:|:----:|:--:|---------|
| B1 | Quoted vs unquoted identifier storage (8 booleans) | Critical | Critical | Minor | Critical | **Critical** |
| B2 | Schema/Catalog topology (12 items) | Critical | Critical | Minor | Important | **Critical** |
| B3 | SQL feature flags (~30 booleans) | Critical | Critical | Important | Important | **Critical** |
| B4 | Limits (~20 `max_*` integers) | Important | Critical | — | — | **Critical** |
| B5 | Transaction semantics (8 items) | Critical | Critical | — | Minor | **Critical** |
| B6 | String/keyword properties | Important | Critical | Minor | Important | **Important** |
| B7 | Type info enrichment (per-type metadata) | Important | Critical | Minor | Important | **Important** |
| B8 | ResultSet capability flags (12 booleans) | — | Critical | — | — | **Important** |
| 30 | Window functions | Minor | — | Critical | Critical | **Important** |
| 29 | Statistical aggregates | — | — | Critical | Important | **Important** |
| 27 | Extended math functions | — | — | Critical | Important | **Important** |
| 33 | CTE support flag | Important | Minor | Important | Critical | **Important** |
| 35 | UNION / INTERSECT / EXCEPT flags | Important | Minor | Important | Important | **Important** |
| 36 | Read-only flag | Critical | Critical | — | Minor | **Critical** |
| 37 | `div_is_floordiv` | Important | Minor | Important | Minor | **Important** |
| 38 | Parameter binding style (`paramstyle`) | Critical | — | — | — | **Important** |
| 11 | SEMI/ANTI join | — | — | Important | Minor | Minor |
| 24 | Bitwise operators | — | — | Minor | — | Minor |
| 34 | QUALIFY | — | — | Minor | — | Minor |
| 12 | LATERAL join | — | — | Minor | — | Minor |
| 16 | `no_limit_value` | — | — | Minor | — | Minor |

Legend: **Critical** = blocks basic functionality. **Important** = needed for correctness
but has workarounds. **Minor** = nice-to-have. **—** = not relevant.

### 4.2 Per-technology breakdowns

See the detailed per-technology gap tables in the appendix (Section 9).

### 4.3 Priority matrix

| Priority window | Gaps to address | Beneficiaries |
|---|---|---|
| **P0+P1 → Slice 9A** | B1, B2, B3, B4, B5, B6, B7, B8, 36, 38 | SA ✅ JDBC ✅ AI partial ✅ |
| **P2+P3 → Slice 9B** | 30, 29, 27, 33, 35, 37, 11, 24, 34, 12, 16 | ibis ✅ AI ✅ SA partial |

---

## 5. Dialect Descriptor Shape

The `MillDialectDescriptor` model defines the client-side shape of the dialect metadata.
Initially populated from hard-coded Calcite defaults; later hydrated from a `GetDialect`
RPC response. This structure serves all four consumers.

```python
@dataclass(frozen=True)
class IdentifierRules:
    quote_start: str = '"'
    quote_end: str = '"'
    escape_quote: str = '"'
    unquoted_storage: str = "UPPER"       # UPPER | LOWER | MIXED
    supports_mixed_case_identifiers: bool = False
    quoted_storage: str = "AS_IS"         # UPPER | LOWER | AS_IS
    supports_mixed_case_quoted_identifiers: bool = True
    max_length: int = 9999
    extra_name_characters: str = ""
    use_fully_qualified: bool = True

@dataclass(frozen=True)
class CatalogSchemaRules:
    supports_schemas: bool = True
    supports_catalogs: bool = False
    catalog_separator: str = "."
    catalog_at_start: bool = True
    schema_term: str = "schema"
    catalog_term: str = "catalog"
    procedure_term: str = "procedure"
    schemas_in_dml: bool = True
    schemas_in_procedure_calls: bool = False
    schemas_in_table_definitions: bool = False
    schemas_in_index_definitions: bool = False
    schemas_in_privilege_definitions: bool = False
    catalogs_in_dml: bool = False
    catalogs_in_procedure_calls: bool = False
    catalogs_in_table_definitions: bool = False
    catalogs_in_index_definitions: bool = False
    catalogs_in_privilege_definitions: bool = False

@dataclass(frozen=True)
class TransactionRules:
    supports_transactions: bool = False
    default_isolation: int = 0
    supports_multiple_transactions: bool = False
    supports_ddl_and_dml_transactions: bool = False
    supports_dml_transactions_only: bool = False
    ddl_causes_commit: bool = False
    ddl_ignored_in_transactions: bool = False

@dataclass(frozen=True)
class Limits:
    max_binary_literal_length: int = 0
    max_char_literal_length: int = 0
    max_column_name_length: int = 128
    max_columns_in_group_by: int = 0
    max_columns_in_index: int = 0
    max_columns_in_order_by: int = 0
    max_columns_in_select: int = 0
    max_columns_in_table: int = 0
    max_connections: int = 0
    max_index_length: int = 0
    max_schema_name_length: int = 128
    max_catalog_name_length: int = 128
    max_row_size: int = 0
    max_row_size_includes_blobs: bool = False
    max_statement_length: int = 0
    max_statements: int = 0
    max_table_name_length: int = 128
    max_tables_in_select: int = 0

@dataclass(frozen=True)
class TypeDescriptor:
    sql_name: str
    jdbc_type_code: int
    precision: int = 0
    literal_prefix: str | None = None
    literal_suffix: str | None = None
    case_sensitive: bool = False
    searchable: int = 3
    unsigned: bool = False
    fixed_prec_scale: bool = False
    auto_increment: bool = False
    minimum_scale: int = 0
    maximum_scale: int = 0
    num_prec_radix: int = 10

@dataclass(frozen=True)
class LimitOffsetStyle:
    limit_syntax: str = "LIMIT {n}"
    offset_syntax: str = "OFFSET {m}"
    no_limit_value: str | None = None

@dataclass(frozen=True)
class FunctionDescriptor:
    name: str
    synonyms: list[str] = ()
    category: str = "scalar"    # scalar | aggregate | window
    supported: bool = True

@dataclass(frozen=True)
class MillDialectDescriptor:
    id: str = "CALCITE"
    name: str = "Apache Calcite"
    read_only: bool = True
    identifiers: IdentifierRules = IdentifierRules()
    catalog_schema: CatalogSchemaRules = CatalogSchemaRules()
    transactions: TransactionRules = TransactionRules()
    limits: Limits = Limits()
    string_quote: str = "'"
    string_concat: str = "||"
    string_escape: str = "STANDARD"
    paging: LimitOffsetStyle = LimitOffsetStyle()
    nulls_sorted_high: bool = False
    nulls_sorted_low: bool = True
    nulls_sorted_at_start: bool = False
    nulls_sorted_at_end: bool = False
    feature_flags: dict[str, bool] = ...   # ~50 flags (see Section 5.1)
    supports_result_set_type_forward_only: bool = True
    supports_result_set_type_scroll_insensitive: bool = False
    supports_result_set_type_scroll_sensitive: bool = False
    supports_result_set_concurrency_read_only: bool = True
    supports_result_set_concurrency_updatable: bool = False
    paramstyle: str = "qmark"
    sql_keywords: str = ""
    search_string_escape: str = "\\"
    system_functions: str = ""
    functions: dict[str, FunctionDescriptor] = ...
    type_info: dict[str, TypeDescriptor] = ...
```

### 5.1 Feature flags (exhaustive list)

`supports_column_aliasing`, `supports_expressions_in_order_by`,
`supports_order_by_unrelated`, `supports_group_by`,
`supports_group_by_unrelated`, `supports_group_by_beyond_select`,
`supports_like_escape_clause`, `supports_non_nullable_columns`,
`supports_table_correlation_names`, `supports_different_table_correlation_names`,
`null_plus_non_null_is_null`, `supports_convert`,
`supports_outer_joins`, `supports_full_outer_joins`, `supports_limited_outer_joins`,
`supports_subqueries_in_comparisons`, `supports_subqueries_in_exists`,
`supports_subqueries_in_ins`, `supports_subqueries_in_quantifieds`,
`supports_correlated_subqueries`,
`supports_union`, `supports_union_all`, `supports_intersect`, `supports_except`,
`supports_cte`, `supports_window_functions`, `supports_qualify`,
`supports_semi_anti_join`, `supports_lateral`,
`supports_is_distinct_from`, `supports_ilike`, `supports_try_cast`,
`supports_native_boolean`, `div_is_floordiv`,
`supports_alter_table_add_column`, `supports_alter_table_drop_column`,
`supports_select_for_update`, `supports_stored_procedures`,
`supports_positioned_delete`, `supports_positioned_update`,
`supports_batch_updates`, `supports_savepoints`, `supports_named_parameters`,
`supports_multiple_result_sets`, `supports_multiple_open_results`,
`supports_get_generated_keys`, `supports_statement_pooling`,
`supports_stored_functions_using_call_syntax`,
`supports_minimum_sql_grammar`, `supports_core_sql_grammar`,
`supports_extended_sql_grammar`,
`supports_ansi92_entry_level`, `supports_ansi92_intermediate`, `supports_ansi92_full`,
`supports_integrity_enhancement`,
`auto_commit_failure_closes_all_result_sets`, `generated_key_always_returned`

---

## 6. Incremental Delivery Strategy

Phase 9 is split into **two slices** aligned with two natural priority windows:

```
9A (P0+P1: structural)  ──────►  9B (P2+P3: dialect tester + functions)
no live service needed            requires running Mill + skymill schema
        │                                   │
        │                           DialectTester → ~80 queries
        │                           parse_sql() validates each
        │                           DialectReport auto-updates defaults
        ▼                                   ▼
  Phase 11 (SA POC)                   Phase 10 (ibis POC)
  Java JDBC refactoring               AI function catalog
  AI prompt foundation
```

### Slice 9A — structural metadata (P0 + P1)

No live service required. Delivers the complete `MillDialectDescriptor` model:

| What | Gaps addressed |
|------|----------------|
| `MillDialectDescriptor` model (all sub-dataclasses) | Foundation |
| `IdentifierRules` — quoted/unquoted storage (8 booleans) | B1 |
| `CatalogSchemaRules` — full schema/catalog topology | B2 |
| `TransactionRules` — all `false`/`NONE` | B5 |
| `Limits` — all ~18 `max_*` values (0 = unlimited for Calcite) | B4 |
| `TypeDescriptor` per-type enrichment | B7 |
| ResultSet capability flags | B8 |
| `feature_flags` — all ~50 booleans (conservative `False` for unvalidated) | B3 |
| `read_only = True` | 36 |
| `paramstyle = "qmark"` | 38 |
| Null sorting, grammar compliance, keywords | B6, misc |
| `quote_identifier()`, `qualify()` helpers | Helpers |
| `mill/sql/types.py` — full type mappings + `to_sa_type()` | Types |
| `CALCITE_DEFAULT` descriptor | Defaults |
| Unit tests | Quality |

**After 9A → Phase 11 (SA POC), Java JDBC, and AI prompt foundation start.**

### Slice 9B — live validation + function catalog (P2 + P3)

Requires running Mill service + `skymill` schema.

#### Dialect Tester approach

Validation is automated via a **dialect tester** — a module that generates ~80 SQL queries
covering every dialect feature, runs each through `ParseSql` (plan compilation), and
classifies results. A successful plan compilation confirms the feature is supported; an
error confirms it is not. No data retrieval is needed for most features.

**Inputs**: `MillClient` (any transport), schema name (`skymill`), table/column metadata
from `GetSchema`.

**Validation mechanism**: `client.parse_sql(schema, sql)` — returns a plan on success,
raises `MillError` on failure.

**Exception**: Division semantics (`SELECT 5/2`) requires `client.query()` to inspect the
actual result value (`2` = floordiv, `2.5` = truediv).

**Location**: `mill/sql/_tester.py` (private module) + `tests/integration/test_dialect.py`.

#### Generated query catalog (~80 queries)

| Category | # | Example SQL | Feature flag / gap |
|----------|:-:|-------------|-------------------|
| **Identifier quoting** | 2 | `SELECT "col" FROM "skymill"."table"` | B1 |
| **Schema-qualified names** | 2 | `SELECT * FROM "skymill"."airports"` | B2 |
| **Column aliasing** | 1 | `SELECT "col" AS x FROM ...` | `supports_column_aliasing` |
| **ORDER BY label** | 1 | `SELECT "col" AS x ... ORDER BY x` | `supports_expressions_in_order_by` |
| **ORDER BY unrelated** | 1 | `SELECT "a" FROM ... ORDER BY "b"` | `supports_order_by_unrelated` |
| **GROUP BY** | 3 | `... GROUP BY "col"`, `GROUP BY "col" HAVING COUNT(*) > 0`, GROUP BY unrelated | `supports_group_by`, `_unrelated`, `_beyond_select` |
| **LIMIT / OFFSET** | 2 | `SELECT ... LIMIT 10`, `... LIMIT 10 OFFSET 5` | paging |
| **FETCH FIRST** | 1 | `SELECT ... FETCH FIRST 10 ROWS ONLY` | paging alt |
| **INNER JOIN** | 1 | `... INNER JOIN ... ON ...` | joins |
| **LEFT/RIGHT JOIN** | 2 | `... LEFT JOIN ...`, `... RIGHT JOIN ...` | `supports_limited_outer_joins` |
| **FULL OUTER JOIN** | 1 | `... FULL OUTER JOIN ...` | `supports_full_outer_joins` |
| **CROSS JOIN** | 1 | `... CROSS JOIN ...` | joins |
| **SEMI join (EXISTS)** | 1 | `WHERE EXISTS (SELECT 1 FROM ...)` | `supports_semi_anti_join` |
| **ANTI join (NOT EXISTS)** | 1 | `WHERE NOT EXISTS (SELECT 1 FROM ...)` | `supports_semi_anti_join` |
| **Scalar subquery** | 1 | `SELECT (SELECT COUNT(*) FROM ...)` | `supports_subqueries_in_comparisons` |
| **IN subquery** | 1 | `WHERE col IN (SELECT col FROM ...)` | `supports_subqueries_in_ins` |
| **EXISTS subquery** | 1 | `WHERE EXISTS (SELECT ...)` | `supports_subqueries_in_exists` |
| **Correlated subquery** | 1 | `WHERE col > (SELECT AVG(col) FROM ... WHERE ...)` | `supports_correlated_subqueries` |
| **CTE** | 1 | `WITH cte AS (SELECT ...) SELECT * FROM cte` | `supports_cte` |
| **UNION** | 1 | `SELECT ... UNION SELECT ...` | `supports_union` |
| **UNION ALL** | 1 | `SELECT ... UNION ALL SELECT ...` | `supports_union_all` |
| **INTERSECT** | 1 | `SELECT ... INTERSECT SELECT ...` | `supports_intersect` |
| **EXCEPT** | 1 | `SELECT ... EXCEPT SELECT ...` | `supports_except` |
| **CAST** | 2 | `SELECT CAST("col" AS DOUBLE)`, `CAST(... AS VARCHAR)` | casting |
| **TRY_CAST** | 1 | `SELECT TRY_CAST('x' AS INTEGER)` | `supports_try_cast` |
| **IS DISTINCT FROM** | 1 | `WHERE "col" IS NOT DISTINCT FROM 1` | `supports_is_distinct_from` |
| **ILIKE** | 1 | `WHERE "col" ILIKE '%a%'` | `supports_ilike` |
| **LIKE ESCAPE** | 1 | `WHERE "col" LIKE '%\%%' ESCAPE '\'` | `supports_like_escape_clause` |
| **BETWEEN / IN / IS NULL** | 3 | `WHERE col BETWEEN 1 AND 10`, `IN (1,2)`, `IS NULL` | basic predicates |
| **Boolean literals** | 1 | `SELECT TRUE, FALSE` | `supports_native_boolean` |
| **NULL arithmetic** | 1 | `SELECT NULL + 1` | `null_plus_non_null_is_null` |
| **NULLS FIRST / LAST** | 2 | `ORDER BY "col" NULLS FIRST`, `NULLS LAST` | null sorting |
| **Division semantics** | 1 | `SELECT 5/2` *(requires `query()` to inspect value)* | `div_is_floordiv` |
| **String concat** | 1 | `SELECT 'a' \|\| 'b'` | `string_concat` |
| **Window: ROW_NUMBER** | 1 | `SELECT ROW_NUMBER() OVER(ORDER BY "col")` | `supports_window_functions`, gap 30 |
| **Window: RANK** | 1 | `SELECT RANK() OVER(ORDER BY "col")` | gap 30 |
| **Window: DENSE_RANK** | 1 | `SELECT DENSE_RANK() OVER(ORDER BY "col")` | gap 30 |
| **Window: LAG** | 1 | `SELECT LAG("col") OVER(ORDER BY "col")` | gap 30 |
| **Window: LEAD** | 1 | `SELECT LEAD("col") OVER(ORDER BY "col")` | gap 30 |
| **Window: FIRST_VALUE** | 1 | `SELECT FIRST_VALUE("col") OVER(ORDER BY "col")` | gap 30 |
| **Window: LAST_VALUE** | 1 | `SELECT LAST_VALUE("col") OVER(...)` | gap 30 |
| **Window: NTILE** | 1 | `SELECT NTILE(4) OVER(ORDER BY "col")` | gap 30 |
| **Window: CUME_DIST** | 1 | `SELECT CUME_DIST() OVER(ORDER BY "col")` | gap 30 |
| **Window: PERCENT_RANK** | 1 | `SELECT PERCENT_RANK() OVER(ORDER BY "col")` | gap 30 |
| **Window: NTH_VALUE** | 1 | `SELECT NTH_VALUE("col", 2) OVER(ORDER BY "col")` | gap 30 |
| **Stats: STDDEV_SAMP** | 1 | `SELECT STDDEV_SAMP("num_col") FROM ...` | gap 29 |
| **Stats: STDDEV_POP** | 1 | `SELECT STDDEV_POP("num_col") FROM ...` | gap 29 |
| **Stats: VAR_SAMP** | 1 | `SELECT VAR_SAMP("num_col") FROM ...` | gap 29 |
| **Stats: VAR_POP** | 1 | `SELECT VAR_POP("num_col") FROM ...` | gap 29 |
| **Stats: COVAR_SAMP** | 1 | `SELECT COVAR_SAMP("a", "b") FROM ...` | gap 29 |
| **Stats: COVAR_POP** | 1 | `SELECT COVAR_POP("a", "b") FROM ...` | gap 29 |
| **Stats: CORR** | 1 | `SELECT CORR("a", "b") FROM ...` | gap 29 |
| **Stats: APPROX_COUNT_DISTINCT** | 1 | `SELECT APPROX_COUNT_DISTINCT("col") FROM ...` | gap 29 |
| **Math: SIGN** | 1 | `SELECT SIGN("num_col") FROM ...` | gap 27 |
| **Math: SQRT** | 1 | `SELECT SQRT(ABS("num_col")) FROM ...` | gap 27 |
| **Math: LN** | 1 | `SELECT LN(ABS("num_col") + 1) FROM ...` | gap 27 |
| **Math: LOG10** | 1 | `SELECT LOG10(ABS("num_col") + 1) FROM ...` | gap 27 |
| **Math: EXP** | 1 | `SELECT EXP(1)` | gap 27 |
| **Math: SIN/COS/TAN** | 3 | `SELECT SIN(1), COS(1), TAN(1)` | gap 27 |
| **Math: ASIN/ACOS/ATAN** | 3 | `SELECT ASIN(0.5), ACOS(0.5), ATAN(1)` | gap 27 |
| **Math: ATAN2** | 1 | `SELECT ATAN2(1, 1)` | gap 27 |
| **Math: DEGREES/RADIANS** | 2 | `SELECT DEGREES(1), RADIANS(90)` | gap 27 |
| **Bitwise: AND/OR/XOR** | 3 | `SELECT 5 & 3`, `5 \| 3`, `5 ^ 3` (syntax varies) | gap 24 |
| **QUALIFY** | 1 | `SELECT ... QUALIFY ROW_NUMBER() OVER(...) = 1` | `supports_qualify` |

#### Tester output

The tester produces a `DialectReport` dataclass:

```python
@dataclass
class FeatureResult:
    category: str         # e.g. "window_functions"
    name: str             # e.g. "ROW_NUMBER"
    sql: str              # generated SQL
    status: str           # "supported" | "unsupported" | "partial" | "error"
    error: str | None     # error message if failed
    gap_id: str | None    # e.g. "30", "B1"

@dataclass
class DialectReport:
    schema: str
    transport: str        # "grpc" | "http"
    timestamp: str
    features: list[FeatureResult]

    def feature_flags(self) -> dict[str, bool]:
        """Export as feature_flags dict for CALCITE_DEFAULT."""
        ...

    def to_markdown(self) -> str:
        """Export as markdown report (py-sql-dialect-report.md)."""
        ...

    def summary(self) -> dict[str, int]:
        """{'supported': N, 'unsupported': N, 'partial': N}"""
        ...
```

**After 9B → Phase 10 (ibis POC) starts. AI prompts get full function catalog.**

---

## 7. Work Items

### Slice 9A — structural metadata

- [ ] **9A.1** `mill/sql/__init__.py` — package init.
- [ ] **9A.2** `mill/sql/dialect.py` — Full `MillDialectDescriptor` model with all
  sub-dataclasses. All fields with Calcite defaults. `feature_flags` dict with all ~50
  boolean flags. Flags that need live testing set to conservative `False`.
- [ ] **9A.3** `CALCITE_DEFAULT: MillDialectDescriptor` — Hard-coded Calcite defaults for
  everything known without live testing.
- [ ] **9A.4** Helper functions: `quote_identifier()`, `qualify()`.
- [ ] **9A.5** `mill/sql/types.py` — `SQL_TYPE_NAMES`, `PYTHON_TYPES`, `DBAPI_TYPE_CODES`,
  `TYPE_INFO`, `to_sa_type()`. Source: `CalciteTypeMapper.kt` and `JdbcUtils.java`.
- [ ] **9A.6** Unit tests: `test_sql_dialect.py`, `test_sql_types.py`.
- [ ] **9A.7** Docs update (cold-start, plan checklist).

### Slice 9B — dialect tester + function catalog

- [ ] **9B.1** `mill/sql/_tester.py` — `DialectTester` class. Takes `MillClient` + schema
  name. Generates ~80 SQL queries from query catalog (see §6 table). Runs each via
  `parse_sql()`. Collects results into `DialectReport`. Division test uses `query()`.
- [ ] **9B.2** `DialectReport` — `FeatureResult` + `DialectReport` dataclasses.
  `feature_flags()` export, `to_markdown()` export, `summary()` stats.
- [ ] **9B.3** `tests/integration/test_dialect.py` — Integration test that instantiates
  `DialectTester`, runs full suite against live Mill, asserts no unexpected errors,
  writes `py-sql-dialect-report.md`.
- [ ] **9B.4** Update `CALCITE_DEFAULT` — use `report.feature_flags()` output to flip
  conservative `False` values to confirmed `True` in `mill/sql/dialect.py`.
- [ ] **9B.5** Populate full function catalog (`functions` dict) from tester results —
  scalar, aggregate, window functions with confirmed support status.
- [ ] **9B.6** `to_ibis_dtype()` adapter in `mill/sql/types.py` (lazy import).
- [ ] **9B.7** `docs/design/client/py-sql-dialect-report.md` — auto-generated markdown
  with ✅/❌/⚠️ per feature, final `CALCITE_DEFAULT` values, recommendations for
  `GetDialect` RPC and AI prompt updates.
- [ ] **9B.8** Docs update (cold-start, plan checklist).

---

## 8. Reference: Existing Java JDBC Driver

The repo contains a **working Java JDBC driver** at `clients/mill-jdbc-driver/` and an
interactive SQL shell at `clients/mill-jdbc-shell/` (sqlline repack).

### Key files

| Java File | Relevance |
|-----------|-----------|
| `MillDatabaseMetadata.java` | Full `DatabaseMetaData` impl — maps to SA dialect capabilities |
| `JdbcUtils.java` | Mill-to-JDBC type mapping (reuse in `mill/sql/types.py`) |
| `MillConnection.java` | Read-only, `TRANSACTION_NONE`, forward-only — replicate in DBAPI |
| `Driver.java` | JDBC URL format — inform SA URL scheme |
| `MillUrlParser.java` | Connection string parsing |
| `MillRecordReaderResultSet.java` | VectorBlock → JDBC ResultSet — inform DBAPI `Cursor` |
| `ColumnsMetadata.java` | Schema introspection → inform SA `get_columns()` |
| `MillClient.java` | Abstract transport — mirrors Python `Transport` |

### Known bugs to avoid in Python

| Bug | Java Location | Python Fix |
|-----|---------------|-----------|
| `BOOL → Types.BLOB` (should be `BOOLEAN`) | `JdbcUtils.java:33` | Use `BOOLEAN` in `mill/sql/types.py` |
| Identifier quote = backtick (should be `"`) | `MillDatabaseMetadata.java:245` | Use double-quote from dialect YAML |
| `supportsGroupBy() = false` (incorrect) | `MillDatabaseMetadata.java:328` | Live-test to set correct flag |
| `supportsColumnAliasing() = false` (incorrect) | `MillDatabaseMetadata.java:295` | Live-test |
| Many features return `false` as stubs | Throughout `MillDatabaseMetadata` | Live-test each capability |

### Concrete decisions from Java driver

1. **Read-only** — `isReadOnly() = true`. Python: `commit()` = no-op, DDL raises error.
2. **Transaction isolation** — `TRANSACTION_NONE`. Python DBAPI level = 0.
3. **Forward-only cursors** — `TYPE_FORWARD_ONLY` + `CONCUR_READ_ONLY`.
4. **No catalogs** — `getCatalog() = null`. Python SA: `supports_catalogs = False`.
5. **Holdability** — `HOLD_CURSORS_OVER_COMMIT`. Python: N/A.

---

## 9. Appendix: Per-Technology Gap Breakdowns

### SQLAlchemy (Phase 11)

| Criticality | # | Gap | Why |
|:-----------:|---|-----|-----|
| Critical | B1 | Identifier storage rules | `IdentifierPreparer` needs quoted/unquoted storage booleans |
| Critical | B2 | Schema/Catalog topology | `supports_schemas`, catalog separator, `supportsSchemasInDataManipulation` |
| Critical | B3 | SQL feature flags | `supportsGroupBy`, `supportsCorrelatedSubqueries`, `supportsColumnAliasing` |
| Critical | B5 | Transaction semantics | `supports_transactions = False` to skip `BEGIN`/`COMMIT` |
| Critical | 36 | Read-only flag | Prevents DDL/DML compilation |
| Critical | 38 | Parameter binding style | `paramstyle` for bind param rendering |
| Important | B4 | Limits | `max_identifier_length` for name truncation |
| Important | B6 | String/keyword properties | `searchStringEscape` for `LIKE` |
| Important | B7 | Type info enrichment | Type precision/scale/nullable for `get_columns()` |
| Important | 33 | CTE support | `supports_cte = True` enables `WITH` clause |
| Important | 35 | UNION / INTERSECT / EXCEPT | SA `union()`, `intersect()`, `except_()` |
| Important | 37 | `div_is_floordiv` | Integer division rendering |
| Minor | 30 | Window functions | SA `over()` — less critical than ibis |

### JDBC (Java driver)

| Criticality | # | Gap | Why |
|:-----------:|---|-----|-----|
| Critical | B1–B8, 36 | All structural gaps | BI tools probe every `DatabaseMetaData` method |

### ibis (Phase 10)

| Criticality | # | Gap | Why |
|:-----------:|---|-----|-----|
| Critical | 30 | Window functions | ibis analytical expressions |
| Critical | 29 | Statistical aggregates | `col.std()`, `col.var()`, `col.corr()` |
| Critical | 27 | Extended math functions | `col.sqrt()`, `col.ln()`, trig |
| Important | B3, 33, 35, 37, 11 | Feature flags + set ops | Correct SQL generation |

### AI NL-to-SQL

The AI layer (`ai/mill-ai-v1-core`) uses `SqlDialect` / `SpecSqlDialect` to generate LLM
system prompts from dialect YAML (`calcite.yml`) via Pebble templates (`sql-features.prompt`).

**Already has**: identifiers, literals, joins, ordering, paging, operators, core functions.

| Criticality | # | Gap | Why |
|:-----------:|---|-----|-----|
| Critical | B1 | Identifier quoting | Current YAML uses backtick, should be `"`. LLM generates wrong quoting. |
| Critical | 33 | CTE support | LLMs generate `WITH ... AS` for complex queries |
| Critical | 30 | Window functions | LLMs generate `ROW_NUMBER() OVER(...)` for analytical questions |
| Important | B2, B3, 29, 27, 35, B6, B7 | Various | Correct SQL generation, type awareness |

**Key insight**: `SpecSqlDialect.fromResource()` already loads from YAML. If
`MillDialectDescriptor` serializes to the same shape, the AI gets improvements for free.

---

## 10. Future: `GetDialect` RPC

When the server implements `GetDialect`, the handshake response includes
`supports_dialect: bool`. The proto mirrors `MillDialectDescriptor`:

```protobuf
message DialectDescriptor {
  string id = 1;
  string name = 2;
  bool read_only = 3;
  IdentifierRules identifiers = 4;
  CatalogSchemaRules catalog_schema = 5;
  TransactionRules transactions = 6;
  Limits limits = 7;
  LimitOffsetStyle paging = 8;
  map<string, bool> feature_flags = 9;
  repeated FunctionDescriptor functions = 10;
  map<string, TypeDescriptor> type_info = 11;
  bool nulls_sorted_high = 12;
  bool nulls_sorted_low = 13;
  string sql_keywords = 14;
  string search_string_escape = 15;
  string system_functions = 16;
  string string_quote = 17;
  string string_concat = 18;
  string paramstyle = 19;
}
```

The server populates from existing YAML files. Gap items (window functions, stats, math,
feature flags) need YAML additions first.
