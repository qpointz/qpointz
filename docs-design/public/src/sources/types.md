# Type System

Mill uses a fixed set of data types for all sources, queries, and result sets. Understanding
which types are available helps you choose the right column types in source configurations and
interpret query results correctly.

## Supported Types

| Type | Description | Example values |
|------|-------------|----------------|
| `BOOL` | Boolean true/false | `true`, `false` |
| `TINY_INT` | 8-bit signed integer (stored as 32-bit) | `-128` .. `127` |
| `SMALL_INT` | 16-bit signed integer (stored as 32-bit) | `-32768` .. `32767` |
| `INT` | 32-bit signed integer | `-2147483648` .. `2147483647` |
| `BIG_INT` | 64-bit signed integer | Large whole numbers |
| `FLOAT` | 32-bit floating point | `3.14` |
| `DOUBLE` | 64-bit floating point | `3.141592653589793` |
| `STRING` | UTF-8 text | `"hello world"` |
| `BINARY` | Arbitrary byte sequence | Binary data, images, etc. |
| `DATE` | Calendar date | `2025-01-15` |
| `TIME` | Time of day (nanosecond precision) | `14:30:00.000000000` |
| `TIMESTAMP` | Date and time without timezone (millisecond precision) | `2025-01-15T14:30:00.000` |
| `TIMESTAMP_TZ` | Date and time with UTC timezone (millisecond precision) | `2025-01-15T14:30:00.000Z` |
| `INTERVAL_DAY` | Duration in days | `30` (days) |
| `INTERVAL_YEAR` | Duration in years | `2` (years) |
| `UUID` | Universally unique identifier (128-bit) | `550e8400-e29b-41d4-a716-446655440000` |

## Type Hints in Source Configuration

When configuring [table attributes](configuration.md#table-attributes), you can specify a type
hint to control how extracted values are interpreted:

| Type hint | Mill type | Coercion behavior |
|-----------|-----------|-------------------|
| `string` | `STRING` | Value used as-is |
| `int` | `INT` | Parsed as 32-bit integer |
| `long` | `BIG_INT` | Parsed as 64-bit integer |
| `float` | `FLOAT` | Parsed as 32-bit float |
| `double` | `DOUBLE` | Parsed as 64-bit float |
| `bool` | `BOOL` | `"true"` (case-insensitive) → `true`, everything else → `false` |
| `date` | `DATE` | Parsed using the `format` property (e.g. `yyyy-MM-dd`) |
| `timestamp` | `TIMESTAMP` | Parsed using the `format` property |

## Python Client Types

When reading query results through the Python client (`mill-py`), values are returned as:

| Mill type | Python type |
|-----------|-------------|
| `STRING` | `str` |
| `BOOL` | `bool` |
| `TINY_INT`, `SMALL_INT`, `INT` | `int` |
| `BIG_INT` | `int` |
| `FLOAT` | `float` |
| `DOUBLE` | `float` |
| `BINARY` | `bytes` |
| `UUID` | `uuid.UUID` |
| `DATE` | `whenever.Date` |
| `TIME` | `whenever.Time` |
| `TIMESTAMP` | `whenever.LocalDateTime` |
| `TIMESTAMP_TZ` | `whenever.ZonedDateTime` |

When converting to Pandas DataFrames (via PyArrow), the types are mapped to appropriate
Arrow types automatically.

## JDBC Driver Types

The Mill JDBC driver maps standard SQL types to Mill types. Most common SQL types (integers,
floats, strings, dates, timestamps, booleans, binary) are supported. Complex types such as
arrays, structs, and XML are not currently supported.

## Technical Reference

### Schema and Field Definitions

Mill uses Protocol Buffers to define schemas and transfer data between services and clients.

A **Schema** is a collection of **Tables**. Each table has a name, an optional schema name,
a table type (`TABLE` or `VIEW`), and an ordered list of **Fields**. Every field carries a
name, a zero-based index, and a **DataType** — which combines a logical type with nullability.

```
Schema
 └── Table (name, schemaName, tableType)
      └── Field (name, fieldIdx, DataType)
           ├── LogicalDataType (typeId, precision, scale)
           └── Nullability (NULL | NOT_NULL)
```

The `LogicalDataType` specifies the semantic type (one of the 16 type IDs listed above) plus
optional precision and scale for types that support them (e.g. `FLOAT`, `DOUBLE`, `STRING`).

### Vector Block Format

Query results are transmitted as **VectorBlocks** — a columnar data format where each column
is encoded as a separate **Vector**.

```
VectorBlock
 ├── VectorBlockSchema
 │    └── Field[] (same structure as table fields)
 ├── vectorSize (number of rows)
 └── Vector[] (one per column)
      ├── fieldIdx (which field this vector belongs to)
      ├── NullsVector (per-row boolean null mask)
      └── values (one of the typed vector variants)
```

Each `Vector` carries a null mask (`NullsVector`) — a list of booleans where `true` means the
value at that row position is null. The actual data is stored in one of seven typed variants:

| Vector variant | Wire type | Used by |
|----------------|-----------|---------|
| `StringVector` | `repeated string` | `STRING` |
| `I32Vector` | `repeated int32` | `TINY_INT`, `SMALL_INT`, `INT`, `INTERVAL_DAY`, `INTERVAL_YEAR` |
| `I64Vector` | `repeated int64` | `BIG_INT`, `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ` |
| `FP32Vector` | `repeated float` | `FLOAT` |
| `FP64Vector` | `repeated double` | `DOUBLE` |
| `BoolVector` | `repeated bool` | `BOOL` |
| `BytesVector` | `repeated bytes` | `BINARY`, `UUID` |

Multiple logical types can share the same physical vector encoding. For example, `DATE`,
`TIME`, `TIMESTAMP`, and `TIMESTAMP_TZ` all use `I64Vector` but interpret the integer values
differently:

- **DATE**: epoch days (days since 1970-01-01)
- **TIME**: nanoseconds since midnight
- **TIMESTAMP**: milliseconds since Unix epoch (no timezone)
- **TIMESTAMP_TZ**: milliseconds since Unix epoch (UTC)

Similarly, `UUID` uses `BytesVector` with each value being exactly 16 bytes.

A single response may contain one or more `VectorBlock`s, allowing results to be streamed
in chunks. Each block is self-describing — it includes its own schema, so blocks can be
processed independently.

## Limitations

- **No decimal type**: SQL `NUMERIC` and `DECIMAL` are mapped to 64-bit floats, which may lose
  precision for values with more than ~15 significant digits.
- **No complex types**: Arrays, maps, structs, and nested types are not supported.
- **Intervals**: `INTERVAL_DAY` and `INTERVAL_YEAR` are defined but have limited support in
  JDBC and Python clients.
