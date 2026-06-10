# Type System

Mill defines 16 logical types for columnar data. mill-py maps each type to
a Python native type and a PyArrow type for DataFrame conversions.

---

## Mill Logical Types

| Mill Type | Python Type | PyArrow Type | Notes |
|-----------|-------------|--------------|-------|
| `TINY_INT` | `int` | `int8` | 8-bit integer (wire: int32) |
| `SMALL_INT` | `int` | `int16` | 16-bit integer (wire: int32) |
| `INT` | `int` | `int32` | 32-bit integer |
| `BIG_INT` | `int` | `int64` | 64-bit integer |
| `BOOL` | `bool` | `bool_` | |
| `FLOAT` | `float` | `float32` | IEEE 754 single precision |
| `DOUBLE` | `float` | `float64` | IEEE 754 double precision |
| `STRING` | `str` | `string` | UTF-8 |
| `BINARY` | `bytes` | `binary` | Arbitrary byte data |
| `UUID` | `uuid.UUID` | `binary(16)` | 16-byte MSB+LSB layout |
| `DATE` | `datetime.date` | `date32` | Encoded as epoch days |
| `TIME` | `datetime.time` | `time64('ns')` | Nanoseconds since midnight |
| `TIMESTAMP` | `datetime.datetime` | `timestamp('ms')` | Epoch millis, no timezone |
| `TIMESTAMP_TZ` | `datetime.datetime` (UTC) | `timestamp('ms', tz='UTC')` | Epoch millis, UTC |
| `INTERVAL_DAY` | `datetime.timedelta` | `duration('s')` | Day count |
| `INTERVAL_YEAR` | `int` | `int32` | Year/month count |

---

## MillType Enum

The `MillType` enum gives programmatic access to type metadata:

```python
from mill.types import MillType

# Access type properties
print(MillType.INT.python_type)     # <class 'int'>
print(MillType.DATE.vector_field)   # 'i64Vector'
print(MillType.STRING.proto_id)     # 12

# Convert from protobuf ID
mt = MillType.from_proto(3)         # MillType.INT
```

---

## MillField

Each column in a result or schema is represented as a `MillField`:

```python
from mill.types import MillField

# Fields are typically obtained from schema introspection or ResultSet
schema = client.get_schema("skymill")
table = schema.tables[0]

for field in table.fields:
    print(f"{field.name}: {field.type.name}")
    print(f"  index={field.index}, nullable={field.nullable}")
    print(f"  precision={field.precision}, scale={field.scale}")
```

| Attribute | Type | Description |
|-----------|------|-------------|
| `name` | `str` | Column name |
| `index` | `int` | Zero-based ordinal position |
| `type` | `MillType` | Logical type |
| `nullable` | `bool` | Whether `None` values are allowed |
| `precision` | `int` | Type precision (0 when unused) |
| `scale` | `int` | Type scale (0 when unused) |

---

## MillTable and MillSchema

Schema introspection returns structured metadata:

```python
schema = client.get_schema("skymill")

for table in schema.tables:
    print(f"{table.schema_name}.{table.name} ({table.table_type.name})")
    # skymill.cities (TABLE)
    # skymill.countries (TABLE)

    for field in table.fields:
        null = "NULL" if field.nullable else "NOT NULL"
        print(f"  {field.name}: {field.type.name} {null}")
```

| Class | Attributes |
|-------|------------|
| `MillSchema` | `tables: tuple[MillTable, ...]` |
| `MillTable` | `name`, `schema_name`, `table_type`, `fields: tuple[MillField, ...]` |
| `TableType` | `NOT_SPECIFIED`, `TABLE`, `VIEW` |

---

## Temporal Encoding

Mill encodes temporal values as integers on the wire:

| Type | Wire Encoding | Python Decoding |
|------|--------------|-----------------|
| `DATE` | `int64` epoch days | `datetime.date.fromordinal(epoch_ordinal + days)` |
| `TIME` | `int64` nanoseconds since midnight | `datetime.time` (microsecond precision) |
| `TIMESTAMP` | `int64` epoch milliseconds | `datetime.datetime` (naive, no tz) |
| `TIMESTAMP_TZ` | `int64` epoch milliseconds | `datetime.datetime` (UTC) |
| `INTERVAL_DAY` | `int32` day count | `datetime.timedelta(days=n)` |
| `INTERVAL_YEAR` | `int32` year/month count | `int` (raw value) |

**Note**: Python's `datetime.time` has microsecond precision. Sub-microsecond
nanosecond values from `TIME` columns are truncated.

---

## Null Handling

Null values are represented as `None` in Python dicts and as native nulls in
Arrow/pandas/polars:

```python
result = client.query("SELECT ...")
for row in result:
    if row["some_column"] is None:
        print("null value")
```

In Arrow Tables, nulls are tracked via Arrow's built-in null bitmap.
In pandas DataFrames (with Arrow-backed dtypes), nulls use `pd.NA`.
In polars DataFrames, nulls use polars' native null representation.
