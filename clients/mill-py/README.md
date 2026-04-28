# mill-py (PyPI: qpointz-mill-py)

Python client for [Mill](https://github.com/qpointz/qpointz) data services.

Connect via **gRPC** or **HTTP** (JSON / Protobuf), query data with SQL, and
consume results as native Python dicts, PyArrow Tables, pandas DataFrames, or
polars DataFrames.

## Installation

```bash
pip install qpointz-mill-py              # core (gRPC + HTTP)
pip install qpointz-mill-py[arrow]       # + PyArrow support
pip install qpointz-mill-py[pandas]      # + pandas (includes Arrow)
pip install qpointz-mill-py[polars]      # + polars (includes Arrow)
pip install qpointz-mill-py[all]         # everything
```

**Requirements**: Python 3.10 – 3.13.

## Quick Start

```python
from mill import connect

# Connect via gRPC
client = connect("grpc://localhost:9090")

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
with connect("grpc://localhost:9090") as client:
    result = client.query("SELECT 1")
    print(result.fetchall())
```

## Authentication

```python
from mill import connect
from mill.auth import BasicAuth, BearerToken

# Basic auth (username + password)
client = connect("grpc://localhost:9090", auth=BasicAuth("user", "pass"))

# Bearer token (OAuth2 / JWT)
client = connect("grpc://localhost:9090", auth=BearerToken("eyJhbG..."))

# Anonymous (default — no auth header)
client = connect("grpc://localhost:9090")
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
table = result.to_arrow()       # requires qpointz-mill-py[arrow]

# pandas DataFrame
df = result.to_pandas()          # requires qpointz-mill-py[pandas]

# polars DataFrame
df = result.to_polars()          # requires qpointz-mill-py[polars]
```

## Async API

The `mill.aio` module mirrors the synchronous API with `async`/`await`.

```python
from mill.aio import connect as aconnect
from mill.auth import BasicAuth

async with await aconnect("grpc://localhost:9090") as client:
    schemas = await client.list_schemas()

    result = await client.query('SELECT * FROM "skymill"."CITIES"')
    async for row in result:
        print(row["CITY"])

    # DataFrame conversion
    df = await result.to_pandas()
```

## Platform HTTP (metadata + schema explorer)

These APIs use the **Mill HTTP origin** (same host as the Spring Boot app), not the
Jet path `/services/jet`. Clients share TLS/auth helpers via `mill._http_common`
and set `Accept` / `Content-Type` per request.

| Package | Sync | Async |
|---------|------|--------|
| Metadata (`/api/v1/metadata`) | `mill.metadata.connect` → `MetadataClient` | `mill.metadata.aio.connect` → `AsyncMetadataClient` |
| Schema explorer (`/api/v1/schema`) | `mill.schema_explorer.connect` → `SchemaExplorerClient` | `mill.schema_explorer.aio.connect` → `AsyncSchemaExplorerClient` |

```python
from mill.auth import BasicAuth
from mill.metadata import connect as meta_connect
from mill.schema_explorer import connect as schema_connect

origin = "http://localhost:8080"

with meta_connect(origin, auth=BasicAuth("u", "p")) as meta:
    scopes = meta.list_scopes()
    yaml_export = meta.export_metadata(scope="global", format="yaml")

with schema_connect(origin, auth=BasicAuth("u", "p")) as ex:
    root = ex.get_model_root(facet_mode="direct")
    # Use root.metadata_entity_id when calling metadata write APIs
```

**Export:** `MetadataClient.export_metadata(..., format="yaml"|"json")` passes through
to `GET /api/v1/metadata/export` (`scope` controls which facet rows are included;
see metadata service docs). **Import:** multipart YAML via `import_metadata`.

**Skymill-style bundles:** concatenate multiple YAML seeds client-side, then one import:

```python
from pathlib import Path

from mill.auth import BasicAuth
from mill.metadata import (
    connect as meta_connect,
    import_metadata_bundle,
    export_canonical,
    parse_metadata_export_json,
)

with meta_connect(origin, auth=BasicAuth("u", "p")) as meta:
    import_metadata_bundle(
        meta,
        [Path("seed-a.yaml"), Path("seed-b.yaml")],
        mode="MERGE",
        actor="loader",
    )
    json_text = export_canonical(meta, scope="all", format="json")
    docs = parse_metadata_export_json(json_text)
```

Async equivalents: `import_metadata_bundle_async`, `export_canonical_async` on
`AsyncMetadataClient`. Helpers use stdlib `json` only for JSON export parsing (no PyYAML
required for the import path beyond building the YAML bytes you upload).

**URNs:** Logical model entity rules live in Kotlin
[`ModelEntityUrn.kt`](../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/ModelEntityUrn.kt)
and
[`SchemaModelRoot.kt`](../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/SchemaModelRoot.kt);
prefer `metadata_entity_id` from schema explorer DTOs for metadata mutations.

Integration tests also support **`MILL_IT_PLATFORM_ORIGIN`** (full URL to the Mill HTTP
server) for `tests/integration/test_platform_http.py`; when unset, those tests skip.

## Schema Introspection

```python
client = connect("grpc://localhost:9090")

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
    client = connect("grpc://localhost:9090")
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
