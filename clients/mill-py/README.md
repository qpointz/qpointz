# mill-py

Python client for [Mill](https://github.com/qpointz/qpointz) data services.

Connect via **gRPC** or **HTTP** (JSON / Protobuf), query data with SQL, and
consume results as native Python dicts, PyArrow Tables, pandas DataFrames, or
polars DataFrames.

## Installation

```bash
pip install mill-py              # core (gRPC + HTTP)
pip install mill-py[arrow]       # + PyArrow support
pip install mill-py[pandas]      # + pandas (includes Arrow)
pip install mill-py[polars]      # + polars (includes Arrow)
pip install mill-py[all]         # everything
```

**Requirements**: Python 3.10 – 3.13.

## Quick Start

```python
from mill import connect

# Connect via gRPC
client = connect("grpc://localhost:9099")

# List schemas
for name in client.list_schemas():
    print(name)

# Run a query
result = client.query('SELECT "ID", "CITY" FROM "skymill"."CITIES"')
for row in result:
    print(row["CITY"])
```

### Connect via HTTP

```python
# HTTP with JSON encoding (default)
client = connect("http://localhost:8080/services/jet")

# HTTP with protobuf encoding (more efficient)
client = connect("http://localhost:8080/services/jet", encoding="protobuf")
```

### Context Manager

```python
with connect("grpc://localhost:9099") as client:
    result = client.query("SELECT 1")
    print(result.fetchall())
```

## Authentication

```python
from mill import connect
from mill.auth import BasicAuth, BearerToken

# Basic auth (username + password)
client = connect("grpc://localhost:9099", auth=BasicAuth("user", "pass"))

# Bearer token (OAuth2 / JWT)
client = connect("grpc://localhost:9099", auth=BearerToken("eyJhbG..."))

# Anonymous (default — no auth header)
client = connect("grpc://localhost:9099")
```

## TLS / Mutual-TLS

```python
# TLS with custom CA certificate
client = connect("grpcs://secure.backend:443", tls_ca="/path/to/ca.pem")

# Mutual-TLS (client certificate + key)
client = connect(
    "grpcs://mtls.backend:443",
    tls_ca="/path/to/ca.pem",
    tls_cert="/path/to/client.pem",
    tls_key="/path/to/client-key.pem",
)
```

## DataFrame Conversion

All DataFrame conversions use PyArrow as the foundation.

```python
result = client.query('SELECT * FROM "skymill"."CITIES"')

# PyArrow Table
table = result.to_arrow()       # requires mill-py[arrow]

# pandas DataFrame
df = result.to_pandas()          # requires mill-py[pandas]

# polars DataFrame
df = result.to_polars()          # requires mill-py[polars]
```

## Async API

The `mill.aio` module mirrors the synchronous API with `async`/`await`.

```python
from mill.aio import connect as aconnect
from mill.auth import BasicAuth

async with await aconnect("grpc://localhost:9099") as client:
    schemas = await client.list_schemas()

    result = await client.query('SELECT * FROM "skymill"."CITIES"')
    async for row in result:
        print(row["CITY"])

    # DataFrame conversion
    df = await result.to_pandas()
```

## Schema Introspection

```python
client = connect("grpc://localhost:9099")

# List schema names
schemas = client.list_schemas()

# Get full schema with tables and fields
schema = client.get_schema("skymill")
for table in schema.tables:
    print(f"{table.schema_name}.{table.name}")
    for field in table.fields:
        print(f"  {field.name}: {field.type.name} (nullable={field.nullable})")
```

## Mill Type System

mill-py supports all 16 Mill logical types with automatic conversion to Python
native types:

| Mill Type | Python Type | PyArrow Type |
|-----------|-------------|--------------|
| `TINY_INT` | `int` | `int8` |
| `SMALL_INT` | `int` | `int16` |
| `INT` | `int` | `int32` |
| `BIG_INT` | `int` | `int64` |
| `BOOL` | `bool` | `bool_` |
| `FLOAT` | `float` | `float32` |
| `DOUBLE` | `float` | `float64` |
| `STRING` | `str` | `string` |
| `BINARY` | `bytes` | `binary` |
| `UUID` | `uuid.UUID` | `binary(16)` |
| `DATE` | `datetime.date` | `date32` |
| `TIME` | `datetime.time` | `time64('ns')` |
| `TIMESTAMP` | `datetime.datetime` | `timestamp('ms')` |
| `TIMESTAMP_TZ` | `datetime.datetime` (UTC) | `timestamp('ms', tz='UTC')` |
| `INTERVAL_DAY` | `datetime.timedelta` | `duration('s')` |
| `INTERVAL_YEAR` | `int` | `int32` |

## Error Handling

```python
from mill import connect, MillConnectionError, MillAuthError, MillQueryError

try:
    client = connect("grpc://localhost:9099")
    result = client.query("SELECT bad syntax")
except MillConnectionError:
    print("Cannot reach the server")
except MillAuthError:
    print("Authentication failed")
except MillQueryError as e:
    print(f"Query failed: {e}")
```

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.
