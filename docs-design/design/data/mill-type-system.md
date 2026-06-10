# Mill Schema & Type System Reference

This document is an inventory of the Mill type system: protobuf definitions, Java type
abstractions, vector encoding, JDBC mappings, Python/PyArrow mappings, and known gaps.

---

## 1. Protobuf Type Hierarchy

All wire-format type definitions live in `proto/common.proto` and `proto/vector.proto`.

### 1.1 LogicalDataType

`LogicalDataType` describes the logical meaning of a column value.

```protobuf
message LogicalDataType {
  enum LogicalDataTypeId {
    NOT_SPECIFIED_TYPE = 0;
    TINY_INT          = 1;   // 8-bit signed integer
    SMALL_INT         = 2;   // 16-bit signed integer
    INT               = 3;   // 32-bit signed integer
    BIG_INT           = 4;   // 64-bit signed integer
    BINARY            = 5;   // arbitrary byte sequence
    BOOL              = 6;   // boolean
    DATE              = 7;   // calendar date (epoch days as i64)
    FLOAT             = 8;   // 32-bit IEEE 754 float
    DOUBLE            = 9;   // 64-bit IEEE 754 double
    INTERVAL_DAY      = 10;  // day-level interval (i32)
    INTERVAL_YEAR     = 11;  // year-level interval (i32)
    STRING            = 12;  // UTF-8 text
    TIMESTAMP         = 13;  // local timestamp (epoch millis as i64)
    TIMESTAMP_TZ      = 14;  // zoned timestamp (epoch millis as i64, UTC)
    TIME              = 15;  // time-of-day (nanos since midnight as i64)
    UUID              = 16;  // UUID (16-byte binary)
  }

  LogicalDataTypeId typeId = 2;
  int32 precision          = 3;
  int32 scale              = 4;
}
```

**17 enum values** including `NOT_SPECIFIED_TYPE` (sentinel/default). 16 are meaningful type IDs.

### 1.2 DataType

Wraps a `LogicalDataType` with nullability information.

```protobuf
message DataType {
  enum Nullability {
    NOT_SPECIFIED_NULL = 0;
    NULL               = 1;
    NOT_NULL           = 2;
  }
  LogicalDataType type     = 1;
  Nullability nullability  = 2;
}
```

### 1.3 Field

A named, indexed column with a `DataType`.

```protobuf
message Field {
  string name     = 1;
  uint32 fieldIdx = 2;
  DataType type   = 3;
}
```

### 1.4 Schema and Table

Top-level schema container. Each `Table` carries its own field list.

```protobuf
message Schema {
  repeated Table tables = 1;
}

message Table {
  enum TableTypeId {
    NOT_SPECIFIED_TABLE_TYPE = 0;
    TABLE = 1;
    VIEW  = 2;
  }
  string schemaName          = 1;
  string name                = 2;
  TableTypeId tableType      = 3;
  repeated Field fields      = 4;
}
```

### 1.5 VectorBlockSchema and VectorBlock

Columnar data transport format. Each `VectorBlock` carries a schema and a list of `Vector` columns.

```protobuf
message VectorBlockSchema {
  repeated Field fields = 3;
}

message VectorBlock {
  VectorBlockSchema schema = 1;
  uint32 vectorSize        = 2;
  repeated Vector vectors  = 3;
}
```

### 1.6 Vector — columnar encoding

Each `Vector` has a `fieldIdx`, a `NullsVector` (per-element boolean null mask), and a typed
`oneof values` carrying the actual data.

```protobuf
message Vector {
  message NullsVector  { repeated bool   nulls  = 2; }
  message StringVector { repeated string values = 1; }
  message I32Vector    { repeated int32  values = 1; }
  message I64Vector    { repeated int64  values = 1; }
  message FP64Vector   { repeated double values = 1; }
  message FP32Vector   { repeated float  values = 1; }
  message BoolVector   { repeated bool   values = 1; }
  message BytesVector  { repeated bytes  values = 1; }

  uint32 fieldIdx         = 1;
  NullsVector nulls       = 2;
  oneof values {
    StringVector stringVector = 100;
    I32Vector    i32Vector    = 101;
    I64Vector    i64Vector    = 102;
    FP64Vector   fp64Vector   = 103;
    FP32Vector   fp32Vector   = 104;
    BoolVector   boolVector   = 105;
    BytesVector  byteVector   = 106;
  }
}
```

**7 vector variants**: `StringVector`, `I32Vector`, `I64Vector`, `FP64Vector`, `FP32Vector`, `BoolVector`, `BytesVector`.

---

## 2. Java Type Abstractions

### 2.1 PhysicalType — wire-level encoding

`PhysicalType<B>` represents the physical encoding used in a `Vector`. There are **7 implementations**
(one per vector variant):

| PhysicalType class | Java carrier type | Vector variant | Proto builder |
|--------------------|-------------------|----------------|---------------|
| `BoolPhysical`     | `Boolean`         | `BoolVector`   | `Vector.BoolVector.Builder` |
| `I32Physical`      | `Integer`         | `I32Vector`    | `Vector.I32Vector.Builder` |
| `I64Physical`      | `Long`            | `I64Vector`    | `Vector.I64Vector.Builder` |
| `FP32Physical`     | `Float`           | `FP32Vector`   | `Vector.FP32Vector.Builder` |
| `FP64Physical`     | `Double`          | `FP64Vector`   | `Vector.FP64Vector.Builder` |
| `StringPhysical`   | `String`          | `StringVector` | `Vector.StringVector.Builder` |
| `BytesPhysical`    | `byte[]`          | `BytesVector`  | `Vector.BytesVector.Builder` |

Each `PhysicalType` creates a `VectorProducer` that knows how to append values and build the
corresponding proto vector.

`PhysicalTypeShuttle<T>` is the visitor for physical types (7 visit methods).

### 2.2 LogicalType — semantic type

`LogicalType<E, P extends PhysicalType<E>>` maps a semantic type to its physical encoding.
There are **16 implementations** (one per meaningful `LogicalDataTypeId`):

| LogicalType class      | LogicalDataTypeId | PhysicalType    | Java element type | Notes |
|------------------------|-------------------|-----------------|--------------------|-------|
| `TinyIntLogical`       | `TINY_INT`        | `I32Physical`   | `Integer`          | Widened from 8-bit |
| `SmallIntLogical`      | `SMALL_INT`       | `I32Physical`   | `Integer`          | Widened from 16-bit |
| `IntLogical`           | `INT`             | `I32Physical`   | `Integer`          | |
| `BigIntLogical`        | `BIG_INT`         | `I64Physical`   | `Long`             | |
| `BoolLogical`          | `BOOL`            | `BoolPhysical`  | `Boolean`          | |
| `FloatLogical`         | `FLOAT`           | `FP32Physical`  | `Float`            | |
| `DoubleLogical`        | `DOUBLE`          | `FP64Physical`  | `Double`           | |
| `StringLogical`        | `STRING`          | `StringPhysical`| `String`           | |
| `BinaryLogical`        | `BINARY`          | `BytesPhysical` | `byte[]`           | |
| `DateLogical`          | `DATE`            | `I64Physical`   | `Long`             | Epoch days; convert via `LocalDateToEpochConverter` |
| `TimeLogical`          | `TIME`            | `I64Physical`   | `Long`             | Nanos since midnight; convert via `LocalTimeToNanoConverter` |
| `TimestampLogical`     | `TIMESTAMP`       | `I64Physical`   | `Long`             | Epoch millis; convert via `LocalDateTimeToEpochMilli` |
| `TimestampTZLogical`   | `TIMESTAMP_TZ`    | `I64Physical`   | `Long`             | Epoch millis (UTC); convert via `ZonedDateTimeToEpochMillis` |
| `IntervalDayLogical`   | `INTERVAL_DAY`    | `I32Physical`   | `Integer`          | Day count |
| `IntervalYearLogical`  | `INTERVAL_YEAR`   | `I32Physical`   | `Integer`          | Year count |
| `UUIDLogical`          | `UUID`            | `BytesPhysical` | `byte[]`           | 16-byte binary; convert via `BinaryToUUIDConverter` |

`LogicalTypeShuttle<T>` is the visitor (16 visit methods). `LogicalTypeIdMapper<T>` dispatches
by `LogicalDataTypeId` enum value via a switch statement.

### 2.3 DatabaseType — nullability-aware type record

`DatabaseType` is a Java `record` that bundles a `LogicalType`, nullability, precision, and scale.
It is the primary type abstraction used at the schema/column level.

```java
public record DatabaseType(
    LogicalType<?,?> type,
    boolean nullable,
    int precision,
    int scale
)
```

**Factory methods** (`PREC_SCALE_NOT_APPLICABLE = -1`):

| Factory method | LogicalType | Precision | Scale |
|----------------|-------------|-----------|-------|
| `bool(nullable)` | `BoolLogical` | n/a | n/a |
| `string(nullable, size)` | `StringLogical` | size | n/a |
| `i16(nullable)` | `SmallIntLogical` | n/a | n/a |
| `i32(nullable)` | `IntLogical` | n/a | n/a |
| `i64(nullable)` | `BigIntLogical` | n/a | n/a |
| `fp32(nullable, prec, scale)` | `FloatLogical` | prec | scale |
| `fp64(nullable, prec, scale)` | `DoubleLogical` | prec | scale |
| `binary(nullable, size)` | `BinaryLogical` | size | n/a |
| `date(nullable)` | `DateLogical` | n/a | n/a |
| `time(nullable)` | `TimeLogical` | n/a | n/a |
| `timetz(nullable)` | `TimestampTZLogical` | n/a | n/a |

`asDataType()` converts to protobuf `DataType`. `asLogicalDataType()` converts to protobuf `LogicalDataType`.

### 2.4 Value Converters

Converters bridge between Java temporal types and the physical i64/i32 representation:

| Converter | From | To | Used by |
|-----------|------|----|---------|
| `LocalDateToEpochConverter` | `LocalDate` | `Long` (epoch days) | `DateLogical` |
| `LocalTimeToNanoConverter` | `LocalTime` | `Long` (nanos) | `TimeLogical` |
| `LocalDateTimeToEpochMilli` | `LocalDateTime` | `Long` (epoch millis) | `TimestampLogical` |
| `ZonedDateTimeToEpochMillis` | `ZonedDateTime` | `Long` (epoch millis) | `TimestampTZLogical` |
| `BinaryToUUIDConverter` | `java.util.UUID` | `byte[]` (16 bytes) | `UUIDLogical` |
| `SqlDateToEpochDayConverter` | `java.sql.Date` | `Long` (epoch days) | JDBC ResultSet reading |
| `SqlTimestampToEpochMicrosConverter` | `java.sql.Timestamp` | `Long` (epoch micros) | JDBC ResultSet reading |
| `SqlTimeToMicrosConverter` | `java.sql.Time` | `Long` (micros) | JDBC ResultSet reading |

---

## 3. Vector Encoding

The mapping from `LogicalDataTypeId` through `LogicalType` → `PhysicalType` → `Vector` variant:

| LogicalDataTypeId | PhysicalType | Vector oneof field | Wire type |
|-------------------|--------------|-------------------|-----------|
| `TINY_INT`        | `I32Physical`    | `i32Vector`    | `repeated int32` |
| `SMALL_INT`       | `I32Physical`    | `i32Vector`    | `repeated int32` |
| `INT`             | `I32Physical`    | `i32Vector`    | `repeated int32` |
| `BIG_INT`         | `I64Physical`    | `i64Vector`    | `repeated int64` |
| `BOOL`            | `BoolPhysical`   | `boolVector`   | `repeated bool` |
| `FLOAT`           | `FP32Physical`   | `fp32Vector`   | `repeated float` |
| `DOUBLE`          | `FP64Physical`   | `fp64Vector`   | `repeated double` |
| `STRING`          | `StringPhysical` | `stringVector` | `repeated string` |
| `BINARY`          | `BytesPhysical`  | `byteVector`   | `repeated bytes` |
| `DATE`            | `I64Physical`    | `i64Vector`    | `repeated int64` (epoch days) |
| `TIME`            | `I64Physical`    | `i64Vector`    | `repeated int64` (nanos since midnight) |
| `TIMESTAMP`       | `I64Physical`    | `i64Vector`    | `repeated int64` (epoch millis) |
| `TIMESTAMP_TZ`    | `I64Physical`    | `i64Vector`    | `repeated int64` (epoch millis, UTC) |
| `INTERVAL_DAY`    | `I32Physical`    | `i32Vector`    | `repeated int32` (day count) |
| `INTERVAL_YEAR`   | `I32Physical`    | `i32Vector`    | `repeated int32` (year count) |
| `UUID`            | `BytesPhysical`  | `byteVector`   | `repeated bytes` (16-byte binary) |

All 16 logical types have a complete encoding path from `LogicalType` → `PhysicalType` → `VectorProducer` → proto `Vector`.

---

## 4. JDBC Type Mappings

`JdbcTypeMapper<E>` dispatches on `java.sql.JDBCType`. `JdbcDatabaseTypeMapper` is the concrete
implementation that maps JDBC types to `DatabaseType`.

| JDBC Type | DatabaseType factory | Notes |
|-----------|---------------------|-------|
| `BOOLEAN` | `bool(nullable)` | |
| `BIT` | `bool(nullable)` | |
| `TINYINT` | `i16(nullable)` | Widened to SmallInt (not TinyInt!) |
| `SMALLINT` | `i32(nullable)` | Widened to Int |
| `INTEGER` | `i32(nullable)` | |
| `BIGINT` | `i64(nullable)` | |
| `FLOAT` | `fp32(nullable, prec, scale)` | |
| `DOUBLE` | `fp64(nullable, prec, scale)` | |
| `REAL` | `fp64(nullable, prec, scale)` | Mapped to Double (not Float) |
| `NUMERIC` | `fp64(nullable, prec, scale)` | Lossy for high-precision values |
| `DECIMAL` | `fp64(nullable, prec, scale)` | Lossy for high-precision values |
| `VARCHAR` | `string(nullable, prec)` | |
| `CHAR` | `string(nullable, prec)` | |
| `NVARCHAR` | `string(nullable, prec)` | |
| `NCHAR` | `string(nullable, prec)` | |
| `LONGVARCHAR` | `string(nullable, prec)` | |
| `LONGNVARCHAR` | `string(nullable, prec)` | |
| `NCLOB` | `string(nullable, prec)` | |
| `CLOB` | `string(nullable, prec)` | |
| `BINARY` | `binary(nullable, prec)` | |
| `VARBINARY` | `binary(nullable, prec)` | |
| `LONGVARBINARY` | `binary(nullable, prec)` | |
| `BLOB` | `binary(nullable, prec)` | |
| `DATE` | `date(nullable)` | |
| `TIME` | `time(nullable)` | |
| `TIMESTAMP` | `timetz(nullable)` | Maps to TimestampTZ (not Timestamp) |
| `TIME_WITH_TIMEZONE` | `timetz(nullable)` | Maps to TimestampTZ |
| `TIMESTAMP_WITH_TIMEZONE` | `timetz(nullable)` | Maps to TimestampTZ |
| `NULL` | throws | Not supported |
| `OTHER` | throws | Not supported |
| `JAVA_OBJECT` | throws | Not supported |
| `DISTINCT` | throws | Not supported |
| `STRUCT` | throws | Not supported |
| `ARRAY` | throws | Not supported |
| `REF` | throws | Not supported |
| `DATALINK` | throws | Not supported |
| `ROWID` | throws | Not supported |
| `SQLXML` | throws | Not supported |
| `REF_CURSOR` | throws | Not supported |

### JDBC reading (ResultSet → Vector)

`ResultSetVectorProducerFactory` creates `MappingVectorProducer<ResultSetColumnReader, ?>` for
each logical type, reading column values from a JDBC `ResultSet` and writing them into vectors.

- `IntervalDayLogical` and `IntervalYearLogical` **throw "Not implemented yet"** — these have
  no standard JDBC representation.

### JDBC writing (Vector → ResultSet)

`VectorColumnReaderFactory` creates `ColumnReader` instances that read from `Vector` proto
objects and expose values through the JDBC `ResultSet` interface. All 16 types are supported here.

---

## 5. Python Type Mappings

`clients/mill-py/millclient/utils.py` contains both the native Python reader and the PyArrow
type mapper.

### 5.1 Python native type mapping (`__get_reader`)

| LogicalDataTypeId | Vector field read | Python type | Notes |
|-------------------|-------------------|-------------|-------|
| `STRING` | `string_vector.values[idx]` | `str` | |
| `BOOL` | `bool_vector.values[idx]` | `bool` | |
| `TINY_INT` | `i32_vector.values[idx]` | `int` | |
| `SMALL_INT` | `i32_vector.values[idx]` | `int` | |
| `INT` | `i32_vector.values[idx]` | `int` | |
| `BIG_INT` | `i64_vector.values[idx]` | `int` | |
| `FLOAT` | `fp32_vector.values[idx]` | `float` | |
| `DOUBLE` | `fp64_vector.values[idx]` | `float` | |
| `BINARY` | `byte_vector.values[idx]` | `bytes` | |
| `UUID` | `byte_vector.values[idx]` → `uuid.UUID(bytes=...)` | `uuid.UUID` | |
| `DATE` | `i64_vector.values[idx]` → epoch days → `whenever.Date` | `whenever.Date` | |
| `TIME` | `i64_vector.values[idx]` → nanos → `whenever.Time` | `whenever.Time` | |
| `TIMESTAMP` | `i64_vector.values[idx]` → epoch millis → `whenever.LocalDateTime` | `whenever.LocalDateTime` | |
| `TIMESTAMP_TZ` | `i64_vector.values[idx]` → epoch millis → `whenever.ZonedDateTime` (UTC) | `whenever.ZonedDateTime` | |
| `INTERVAL_DAY` | **not supported** — falls through to `MillError` | — | |
| `INTERVAL_YEAR` | **not supported** — falls through to `MillError` | — | |

### 5.2 PyArrow type mapping (`__get_pyarrow_type`)

| LogicalDataTypeId | PyArrow type | Conversion lambda | Notes |
|-------------------|-------------|-------------------|-------|
| `STRING` | `pa.string()` | None | |
| `BOOL` | `pa.bool_()` | None | |
| `TINY_INT` | `pa.int32()` | None | Widened (pa.int8 would be more precise) |
| `SMALL_INT` | `pa.int32()` | None | Widened (pa.int16 would be more precise) |
| `INT` | `pa.int32()` | None | |
| `BIG_INT` | `pa.int64()` | None | |
| `FLOAT` | `pa.float32()` | None | |
| `DOUBLE` | `pa.float64()` | None | |
| `BINARY` | `pa.binary()` | None | |
| `UUID` | `pa.string()` | `str(x)` | Converted to string representation |
| `DATE` | `pa.date64()` | `x.py_date()` | `whenever.Date` → `datetime.date` |
| `TIME` | `pa.time64('us')` | `x.py_time()` | `whenever.Time` → `datetime.time` |
| `TIMESTAMP` | `pa.date64()` | `x.py_datetime()` | Uses `date64` (should be `pa.timestamp`) |
| `TIMESTAMP_TZ` | `pa.date64()` | `x.py_datetime()` | Uses `date64` (should be `pa.timestamp('ms', tz='UTC')`) |
| `INTERVAL_DAY` | **not supported** — falls through to `MillError` | — | |
| `INTERVAL_YEAR` | **not supported** — falls through to `MillError` | — | |

---

## 6. Calcite Type Mappings

`RelToDatabaseTypeConverter` maps Calcite `RelDataType` (via `SqlTypeName`) to Mill `DatabaseType`.

| Calcite SqlTypeName | LogicalType | Notes |
|---------------------|-------------|-------|
| `BOOLEAN` | `BoolLogical` | |
| `TINYINT` / `UTINYINT` | `TinyIntLogical` | Unsigned widened to signed |
| `SMALLINT` / `USMALLINT` | `SmallIntLogical` | |
| `INTEGER` / `UINTEGER` | `IntLogical` | |
| `BIGINT` / `UBIGINT` | `BigIntLogical` | |
| `FLOAT` | `FloatLogical` | |
| `DOUBLE` / `REAL` / `DECIMAL` | `DoubleLogical` | DECIMAL is lossy |
| `CHAR` / `VARCHAR` | `StringLogical` | |
| `BINARY` / `VARBINARY` | `BinaryLogical` | |
| `DATE` | `DateLogical` | |
| `TIME` / `TIME_WITH_LOCAL_TIME_ZONE` | `TimeLogical` | |
| `TIMESTAMP` / `TIMESTAMP_WITH_LOCAL_TIME_ZONE` | `TimestampLogical` | |
| `TIMESTAMP_WITH_TIME_ZONE` / `TIME_WITH_TIME_ZONE` | `TimestampTZLogical` | TIME_WITH_TZ → TimestampTZ is arguably a mismatch |
| `UUID` | `UUIDLogical` | Calcite 1.41+ supports UUID natively |
| All intervals | throws `notImplemented` | |
| `ARRAY`, `MAP`, `MULTISET`, `ROW`, `STRUCTURED`, `DISTINCT` | throws `notImplemented` | Complex types |
| `NULL`, `ANY`, `SYMBOL`, `CURSOR`, etc. | throws `notImplemented` | Special types |

---

## 7. Cross-Reference Table

| # | LogicalDataTypeId | LogicalType | PhysicalType | Vector | DatabaseType factory | JDBC types (supported) | Python native | PyArrow |
|---|-------------------|-------------|--------------|--------|---------------------|----------------------|---------------|---------|
| 1 | `TINY_INT` | `TinyIntLogical` | `I32Physical` | `i32Vector` | (none — uses `i16`) | `TINYINT`→`i16` | `int` | `pa.int32()` |
| 2 | `SMALL_INT` | `SmallIntLogical` | `I32Physical` | `i32Vector` | `i16(nullable)` | `SMALLINT`→`i32` | `int` | `pa.int32()` |
| 3 | `INT` | `IntLogical` | `I32Physical` | `i32Vector` | `i32(nullable)` | `INTEGER`→`i32` | `int` | `pa.int32()` |
| 4 | `BIG_INT` | `BigIntLogical` | `I64Physical` | `i64Vector` | `i64(nullable)` | `BIGINT`→`i64` | `int` | `pa.int64()` |
| 5 | `BOOL` | `BoolLogical` | `BoolPhysical` | `boolVector` | `bool(nullable)` | `BOOLEAN`,`BIT`→`bool` | `bool` | `pa.bool_()` |
| 6 | `FLOAT` | `FloatLogical` | `FP32Physical` | `fp32Vector` | `fp32(n,p,s)` | `FLOAT`→`fp32` | `float` | `pa.float32()` |
| 7 | `DOUBLE` | `DoubleLogical` | `FP64Physical` | `fp64Vector` | `fp64(n,p,s)` | `DOUBLE`,`REAL`,`NUMERIC`,`DECIMAL`→`fp64` | `float` | `pa.float64()` |
| 8 | `STRING` | `StringLogical` | `StringPhysical` | `stringVector` | `string(n,sz)` | `VARCHAR`,`CHAR`,`NVARCHAR`,`NCHAR`,`LONGVARCHAR`,`LONGNVARCHAR`,`CLOB`,`NCLOB`→`string` | `str` | `pa.string()` |
| 9 | `BINARY` | `BinaryLogical` | `BytesPhysical` | `byteVector` | `binary(n,sz)` | `BINARY`,`VARBINARY`,`LONGVARBINARY`,`BLOB`→`binary` | `bytes` | `pa.binary()` |
| 10 | `DATE` | `DateLogical` | `I64Physical` | `i64Vector` | `date(nullable)` | `DATE`→`date` | `whenever.Date` | `pa.date64()` |
| 11 | `TIME` | `TimeLogical` | `I64Physical` | `i64Vector` | `time(nullable)` | `TIME`→`time` | `whenever.Time` | `pa.time64('us')` |
| 12 | `TIMESTAMP` | `TimestampLogical` | `I64Physical` | `i64Vector` | (none) | `TIMESTAMP`→`timetz` | `whenever.LocalDateTime` | `pa.date64()` |
| 13 | `TIMESTAMP_TZ` | `TimestampTZLogical` | `I64Physical` | `i64Vector` | `timetz(nullable)` | `TIMESTAMP_WITH_TZ`,`TIME_WITH_TZ`→`timetz` | `whenever.ZonedDateTime` | `pa.date64()` |
| 14 | `INTERVAL_DAY` | `IntervalDayLogical` | `I32Physical` | `i32Vector` | (none) | (not supported) | (not supported) | (not supported) |
| 15 | `INTERVAL_YEAR` | `IntervalYearLogical` | `I32Physical` | `i32Vector` | (none) | (not supported) | (not supported) | (not supported) |
| 16 | `UUID` | `UUIDLogical` | `BytesPhysical` | `byteVector` | (none) | (not mapped) | `uuid.UUID` | `pa.string()` |

---

## 8. Known Gaps

### 8.1 DatabaseType factory gaps

| LogicalType | Missing factory | Impact |
|-------------|----------------|--------|
| `TinyIntLogical` | No `DatabaseType.i8(nullable)` factory | Must use `DatabaseType.of(TinyIntLogical.INSTANCE, nullable)` directly |
| `TimestampLogical` | No `DatabaseType.timestamp(nullable)` factory | Must use `DatabaseType.of(TimestampLogical.INSTANCE, nullable)` directly |
| `IntervalDayLogical` | No `DatabaseType.intervalDay(nullable)` factory | Must use `DatabaseType.of(...)` directly |
| `IntervalYearLogical` | No `DatabaseType.intervalYear(nullable)` factory | Must use `DatabaseType.of(...)` directly |
| `UUIDLogical` | No `DatabaseType.uuid(nullable)` factory | Must use `DatabaseType.of(...)` directly |

The `timetz` factory name is misleading — it creates a `TimestampTZLogical`, not a time-with-timezone type.

### 8.2 JDBC mapping issues

| Issue | Details |
|-------|---------|
| `TINYINT` widened to `i16` | `JdbcDatabaseTypeMapper.mapTinyInt` returns `i16(nullable)` (SmallInt), not TinyInt. The actual value is read as `Short` and widened to `Integer` via `TinyIntLogical.valueFrom(Short)`. |
| `SMALLINT` widened to `i32` | `mapSmallInt` returns `i32(nullable)` (Int, not SmallInt). |
| `REAL` mapped to `fp64` | Standard SQL REAL is single-precision, but mapped to `fp64` (Double). |
| `NUMERIC`/`DECIMAL` mapped to `fp64` | Lossy for values exceeding `double` precision (~15 significant digits). No BigDecimal support. |
| `TIMESTAMP` mapped to `timetz` | JDBC `TIMESTAMP` (no timezone) maps to `TimestampTZLogical` instead of `TimestampLogical`. |
| Intervals not supported | JDBC has no standard interval type, so `IntervalDay`/`IntervalYear` can't be read from JDBC ResultSets. |

### 8.3 Python / PyArrow mapping issues

| Issue | Details |
|-------|---------|
| `INTERVAL_DAY` / `INTERVAL_YEAR` not supported | Python reader raises `MillError`. No PyArrow type mapper either. |
| `TIMESTAMP` / `TIMESTAMP_TZ` use `pa.date64()` | Should use `pa.timestamp('ms')` or `pa.timestamp('ms', tz='UTC')` respectively. `pa.date64()` is a date type, not a timestamp type. |
| `TINY_INT` / `SMALL_INT` use `pa.int32()` | Could use `pa.int8()` / `pa.int16()` for tighter representation. |
| `UUID` serialized as string | Loses binary compactness; `pa.binary(16)` or `pa.large_binary()` could be alternatives. |

### 8.4 Types with no end-to-end path

| Type | Java→Vector | Vector→JDBC | Vector→Python | JDBC→Vector |
|------|-------------|-------------|---------------|-------------|
| `INTERVAL_DAY` | OK | OK | **MISSING** | **MISSING** |
| `INTERVAL_YEAR` | OK | OK | **MISSING** | **MISSING** |

Both interval types have full Java support (`LogicalType` → `PhysicalType` → `VectorProducer` → `VectorColumnReader`)
but cannot be read from JDBC sources or consumed by the Python client.

### 8.5 Calcite adapter gaps

- All Calcite interval types (`INTERVAL_*`) throw `notImplemented` in `RelToDatabaseTypeConverter`.
- Complex types (`ARRAY`, `MAP`, `ROW`, `MULTISET`, `STRUCT`) are not supported.
- `TIME_WITH_TIME_ZONE` is mapped to `TimestampTZLogical` — a semantic mismatch (time vs timestamp).

### 8.6 Missing Decimal / BigDecimal support

There is no `DECIMAL` logical type with arbitrary precision. `NUMERIC` and `DECIMAL` JDBC types
are mapped to `fp64`, which is lossy for financial and high-precision use cases. A future
`DecimalLogical` backed by a `StringVector` or a custom encoding would close this gap.

### 8.7 Missing complex types

No support for `ARRAY`, `MAP`, `STRUCT`, or `ROW` types at any level (proto, Java, JDBC, Python).
These would require new `Vector` oneof variants and corresponding `LogicalType` / `PhysicalType`
implementations.
