# Calcite Dialect Functions Comparison

This document compares PostgreSQL functions defined in `postgres.yml` with Apache Calcite's `SqlLibraryOperators` to identify coverage gaps and signature mismatches.

## Overview

Apache Calcite provides built-in function libraries through:
- `SqlStdOperatorTable` — Standard SQL functions
- `SqlLibraryOperators` — Dialect-specific functions (annotated with `@LibraryOperator`)

Functions are grouped by `SqlLibrary` enum: `POSTGRESQL`, `MYSQL`, `ORACLE`, `BIG_QUERY`, `SPARK`, etc.

---

## PostgreSQL Functions Inventory (Calcite SqlLibraryOperators)

### String Functions

| Function | Calcite Definition | Libraries |
|----------|-------------------|-----------|
| `LEFT(string, length)` | `(VARCHAR, INTEGER) → VARCHAR` | POSTGRESQL, MYSQL, SPARK |
| `RIGHT(string, length)` | `(VARCHAR, INTEGER) → VARCHAR` | POSTGRESQL, MYSQL, SPARK |
| `LPAD(string, length, pad)` | `(VARCHAR, INTEGER, VARCHAR) → VARCHAR` | POSTGRESQL, MYSQL, ORACLE, SPARK |
| `RPAD(string, length, pad)` | `(VARCHAR, INTEGER, VARCHAR) → VARCHAR` | POSTGRESQL, MYSQL, ORACLE, SPARK |
| `INITCAP(string)` | `(VARCHAR) → VARCHAR` | POSTGRESQL, ORACLE |
| `CONCAT_WS(separator, args...)` | `(VARCHAR, VARCHAR...) → VARCHAR` | POSTGRESQL, MYSQL |
| `STRPOS(string, substring)` | `(VARCHAR, VARCHAR) → INTEGER` | POSTGRESQL, BIG_QUERY |
| `REPEAT(string, count)` | `(VARCHAR, INTEGER) → VARCHAR` | POSTGRESQL, MYSQL, SPARK |
| `REVERSE(string)` | `(VARCHAR) → VARCHAR` | POSTGRESQL, MYSQL, SPARK |
| `SPLIT_PART(string, delimiter, index)` | `(VARCHAR, VARCHAR, INTEGER) → VARCHAR` | POSTGRESQL, SPARK |
| `TRANSLATE(string, from, to)` | `(VARCHAR, VARCHAR, VARCHAR) → VARCHAR` | POSTGRESQL, ORACLE, SPARK |
| `REGEXP_REPLACE(string, pattern, replacement)` | `(VARCHAR, VARCHAR, VARCHAR) → VARCHAR` | POSTGRESQL, MYSQL, ORACLE |
| `MD5(string)` | `(VARCHAR) → VARCHAR` | POSTGRESQL, MYSQL, SPARK |
| `SHA1(string)` | `(VARCHAR) → VARCHAR` | POSTGRESQL, MYSQL, SPARK |
| `ENCODE(data, format)` | `(VARBINARY, VARCHAR) → VARCHAR` | POSTGRESQL |
| `DECODE(string, format)` | `(VARCHAR, VARCHAR) → VARBINARY` | POSTGRESQL |
| `CHR(code)` | `(INTEGER) → VARCHAR` | POSTGRESQL, ORACLE, SPARK |
| `ASCII(string)` | `(VARCHAR) → INTEGER` | POSTGRESQL, MYSQL, ORACLE, SPARK |
| `FORMAT(pattern, args...)` | `(VARCHAR, ANY...) → VARCHAR` | POSTGRESQL |

### Numeric Functions

| Function | Calcite Definition | Libraries |
|----------|-------------------|-----------|
| `TRUNCATE(numeric, scale)` | `(NUMERIC, INTEGER) → NUMERIC` | POSTGRESQL, MYSQL, SPARK |
| `LOG(base, value)` | `(NUMERIC, NUMERIC) → DOUBLE` | POSTGRESQL, MYSQL |
| `CBRT(numeric)` | `(NUMERIC) → DOUBLE` | POSTGRESQL |
| `SINH(numeric)` | `(NUMERIC) → DOUBLE` | POSTGRESQL, SPARK |
| `COSH(numeric)` | `(NUMERIC) → DOUBLE` | POSTGRESQL, SPARK |
| `TANH(numeric)` | `(NUMERIC) → DOUBLE` | POSTGRESQL, SPARK |
| `SIGN(numeric)` | `(NUMERIC) → INTEGER` | Standard |
| `RANDOM()` | `() → DOUBLE` | POSTGRESQL |

### Date/Time Functions

| Function | Calcite Definition | Libraries |
|----------|-------------------|-----------|
| `DATE_TRUNC(unit, timestamp)` | `(VARCHAR, TIMESTAMP) → TIMESTAMP` | POSTGRESQL |
| `MAKE_DATE(year, month, day)` | `(INTEGER, INTEGER, INTEGER) → DATE` | POSTGRESQL |
| `MAKE_TIME(hour, minute, second)` | `(INTEGER, INTEGER, NUMERIC) → TIME` | POSTGRESQL |
| `MAKE_TIMESTAMP(y, m, d, h, min, sec)` | `(INT, INT, INT, INT, INT, NUMERIC) → TIMESTAMP` | POSTGRESQL |
| `TO_DATE(string, format)` | `(VARCHAR, VARCHAR) → DATE` | POSTGRESQL, ORACLE |
| `TO_TIMESTAMP(string, format)` | `(VARCHAR, VARCHAR) → TIMESTAMP WITH TIME ZONE` | POSTGRESQL, ORACLE |
| `TO_CHAR(value, format)` | `(ANY, VARCHAR) → VARCHAR` | POSTGRESQL, ORACLE |
| `DATE_PART(field, source)` | `(VARCHAR, TIMESTAMP) → NUMERIC` | POSTGRESQL |

### Array Functions

| Function | Calcite Definition | Libraries |
|----------|-------------------|-----------|
| `ARRAY_AGG(value)` | `(ANY) → ARRAY` | POSTGRESQL |
| `ARRAY_APPEND(array, element)` | `(ARRAY, ANY) → ARRAY` | POSTGRESQL, SPARK |
| `ARRAY_PREPEND(element, array)` | `(ANY, ARRAY) → ARRAY` | POSTGRESQL |
| `ARRAY_CAT(array1, array2)` | `(ARRAY, ARRAY) → ARRAY` | POSTGRESQL |
| `ARRAY_CONCAT(array1, array2)` | `(ARRAY, ARRAY) → ARRAY` | POSTGRESQL, SPARK |
| `ARRAY_LENGTH(array, dimension)` | `(ARRAY, INTEGER) → INTEGER` | POSTGRESQL |
| `ARRAY_REVERSE(array)` | `(ARRAY) → ARRAY` | POSTGRESQL, SPARK |
| `ARRAY_TO_STRING(array, delimiter)` | `(ARRAY, VARCHAR) → VARCHAR` | POSTGRESQL, SPARK |
| `CARDINALITY(array)` | `(ARRAY) → INTEGER` | Standard |

### Aggregate Functions

| Function | Calcite Definition | Libraries |
|----------|-------------------|-----------|
| `STRING_AGG(value, delimiter)` | `(VARCHAR, VARCHAR) → VARCHAR` | POSTGRESQL |
| `BOOL_AND(value)` | `(BOOLEAN) → BOOLEAN` | POSTGRESQL |
| `BOOL_OR(value)` | `(BOOLEAN) → BOOLEAN` | POSTGRESQL |
| `EVERY(value)` | `(BOOLEAN) → BOOLEAN` | POSTGRESQL |
| `BIT_AND(value)` | `(INTEGER) → INTEGER` | POSTGRESQL, MYSQL |
| `BIT_OR(value)` | `(INTEGER) → INTEGER` | POSTGRESQL, MYSQL |

### Conditional Functions

| Function | Calcite Definition | Libraries |
|----------|-------------------|-----------|
| `GREATEST(args...)` | `(COMPARABLE...) → COMPARABLE` | POSTGRESQL, MYSQL, ORACLE, SPARK |
| `LEAST(args...)` | `(COMPARABLE...) → COMPARABLE` | POSTGRESQL, MYSQL, ORACLE, SPARK |
| `NVL(value, default)` | `(ANY, ANY) → ANY` | ORACLE (use COALESCE for PG) |

### JSON Functions

| Function | Calcite Definition | Libraries |
|----------|-------------------|-----------|
| `JSON_VALUE(json, path)` | `(ANY, VARCHAR) → VARCHAR` | POSTGRESQL, MYSQL |
| `JSON_QUERY(json, path)` | `(ANY, VARCHAR) → ANY` | POSTGRESQL |
| `JSON_EXISTS(json, path)` | `(ANY, VARCHAR) → BOOLEAN` | POSTGRESQL |
| `JSON_ARRAYAGG(value)` | `(ANY) → JSON` | POSTGRESQL, MYSQL |
| `JSON_OBJECTAGG(key, value)` | `(VARCHAR, ANY) → JSON` | POSTGRESQL, MYSQL |

---

## Comparison: postgres.yml vs Calcite

### Coverage Summary

| Category | postgres.yml | Calcite (POSTGRESQL) | Coverage |
|----------|-------------|---------------------|----------|
| String Functions | 17 | 25+ | ~70% |
| Numeric Functions | 7 | 15+ | ~45% |
| Aggregate Functions | 6 | 10+ | ~60% |
| Date/Time Functions | 11 | 12+ | ~90% |
| Conditional Functions | 4 | 4 | 100% |
| Array Functions | 0 | 10+ | 0% |
| JSON Functions | 0 | 5+ | 0% |

### Signature Mismatches

#### Return Type Differences

| Function | postgres.yml | Calcite | Impact |
|----------|-------------|---------|--------|
| `COUNT(*)` | `INTEGER` | `BIGINT` | Type mismatch in validation |
| `EXTRACT(field FROM ts)` | `INTEGER` | `NUMERIC` | SECOND returns fractional |
| `NOW()` | `TIMESTAMP` | `TIMESTAMP WITH TIME ZONE` | Timezone handling |
| `TO_TIMESTAMP()` | `TIMESTAMP` | `TIMESTAMP WITH TIME ZONE` | Timezone handling |

#### Argument Type Differences

| Function | postgres.yml | Calcite | Notes |
|----------|-------------|---------|-------|
| `DATE_TRUNC(unit, ts)` | `(ENUM, TIMESTAMP)` | `(VARCHAR, TIMESTAMP)` | Unit is string literal |
| `EXTRACT(field FROM ts)` | `(ENUM, TIMESTAMP)` | `(SYMBOL, DATETIME)` | Field is TimeUnit symbol |
| `MOD(a, b)` | `(INTEGER, INTEGER)` | `(NUMERIC, NUMERIC)` | Accepts any numeric |
| `TRUNC(n, s)` | Named `TRUNC` | Named `TRUNCATE` | Function name differs |

#### Polymorphic Functions

These functions in postgres.yml are typed as `STRING`, but Calcite defines them as polymorphic:

| Function | postgres.yml | Calcite |
|----------|-------------|---------|
| `COALESCE(a, b, ...)` | `(STRING...) → STRING` | `(ANY...) → LEAST_RESTRICTIVE` |
| `NULLIF(a, b)` | `(STRING, STRING) → STRING` | `(ANY, ANY) → ARG0_TYPE` |
| `GREATEST(a, b, ...)` | `(STRING...) → STRING` | `(COMPARABLE...) → LEAST_RESTRICTIVE` |
| `LEAST(a, b, ...)` | `(STRING...) → STRING` | `(COMPARABLE...) → LEAST_RESTRICTIVE` |

### Functions Missing from Calcite

These PostgreSQL-specific features have no equivalent in Calcite:

| Feature | Description | Workaround |
|---------|-------------|------------|
| `AGE(ts1, ts2)` | Interval between timestamps | Custom operator |
| `ILIKE` operator | Case-insensitive LIKE | `LOWER(x) LIKE LOWER(pattern)` |
| `~`, `~*`, `!~`, `!~*` | POSIX regex operators | `REGEXP_LIKE` or custom |
| `::` cast operator | PostgreSQL cast syntax | Use `CAST(x AS type)` |
| `SIMILAR TO` | SQL regex pattern | Use `REGEXP_LIKE` |

### Functions in postgres.yml Not Needing SqlLibraryOperators

These are already in `SqlStdOperatorTable` (standard SQL):

```
LENGTH, CHAR_LENGTH, OCTET_LENGTH
LOWER, UPPER
SUBSTRING, OVERLAY
TRIM, LTRIM, RTRIM
CONCAT, POSITION
ROUND, CEIL, FLOOR, ABS, POWER, MOD
COUNT, SUM, AVG, MIN, MAX
CURRENT_DATE, CURRENT_TIMESTAMP, EXTRACT
COALESCE, NULLIF, CASE
```

---

## Recommendations

### 1. Use Combined Operator Table

```java
SqlOperatorTable operatorTable = ChainedSqlOperatorTable.of(
    SqlStdOperatorTable.instance(),
    SqlLibraryOperatorTableFactory.INSTANCE.getOperatorTable(
        EnumSet.of(
            SqlLibrary.STANDARD,
            SqlLibrary.POSTGRESQL
        )
    )
);
```

### 2. Add Custom Operators for Missing Functions

```java
public class PostgresCustomOperators {
    
    // AGE(timestamp, timestamp) → INTERVAL
    public static final SqlFunction AGE = SqlBasicFunction.create(
        "AGE",
        ReturnTypes.explicit(SqlTypeName.INTERVAL_DAY_TIME),
        OperandTypes.TIMESTAMP_TIMESTAMP,
        SqlFunctionCategory.TIMEDATE
    );
    
    // ILIKE operator (case-insensitive LIKE)
    public static final SqlSpecialOperator ILIKE = new SqlLikeOperator(
        "ILIKE",
        SqlKind.LIKE,
        false,  // negated
        false   // case sensitive = false
    );
}
```

### 3. Update postgres.yml Type Mappings

For accurate validation, consider updating postgres.yml:

```yaml
# COUNT returns BIGINT, not INTEGER
- name: "COUNT"
  return: { type: BIGINT, nullable: false }

# EXTRACT returns NUMERIC (for fractional seconds)
- name: "EXTRACT"
  return: { type: NUMERIC, nullable: true }

# Polymorphic functions should use generic types
- name: "COALESCE"
  return: { type: ANY, nullable: true }
```

---

## References

- [SqlLibraryOperators.java](https://github.com/apache/calcite/blob/main/core/src/main/java/org/apache/calcite/sql/fun/SqlLibraryOperators.java)
- [SqlLibrary.java](https://github.com/apache/calcite/blob/main/core/src/main/java/org/apache/calcite/sql/fun/SqlLibrary.java)
- [SqlStdOperatorTable.java](https://github.com/apache/calcite/blob/main/core/src/main/java/org/apache/calcite/sql/fun/SqlStdOperatorTable.java)
- [PostgreSQL Function Documentation](https://www.postgresql.org/docs/current/functions.html)
