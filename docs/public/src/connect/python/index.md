# Python Client

Connect to Mill data services from Python using **mill-py** — query with SQL,
consume results as native dicts, PyArrow Tables, pandas DataFrames, or polars
DataFrames.

---

## Installation

Install from PyPI:

```bash
pip install mill-py              # core (gRPC + HTTP)
pip install mill-py[arrow]       # + PyArrow support
pip install mill-py[pandas]      # + pandas (includes Arrow)
pip install mill-py[polars]      # + polars (includes Arrow)
pip install mill-py[all]         # everything
```

**Requires** Python 3.10 or later (3.10, 3.11, 3.12, 3.13).

---

## Quick Start

```python
from mill import connect

# Connect via gRPC (default port 9090)
client = connect("grpc://localhost:9090")

# List available schemas
for name in client.list_schemas():
    print(name)

# Run a SQL query
result = client.query('SELECT "id", "city" FROM "skymill"."cities" LIMIT 10')
for row in result:
    print(row["city"])
```

---

## Connecting

The `connect()` factory creates a client for any supported protocol.

### gRPC

```python
client = connect("grpc://localhost:9090")         # insecure
client = connect("grpcs://secure.host:443")       # TLS
```

### HTTP

```python
# JSON encoding (default)
client = connect("http://localhost:8080/services/jet")

# Protobuf encoding (more efficient for large payloads)
client = connect("http://localhost:8080/services/jet", encoding="protobuf")

# HTTPS
client = connect("https://secure.host/services/jet")
```

### URL Overrides

Override specific URL components via keyword arguments:

```python
client = connect("grpc://host:9090", host="other-host", port=9100)
client = connect("http://host:8080/path", base_path="/services/custom")
```

### Context Manager

Always close clients when done. The recommended approach is a context manager:

```python
with connect("grpc://localhost:9090") as client:
    result = client.query("SELECT 1")
    print(result.fetchall())
# client is closed automatically
```

Or close manually:

```python
client = connect("grpc://localhost:9090")
try:
    result = client.query("SELECT 1")
finally:
    client.close()
```

---

## Schema Introspection

Explore available schemas, tables, and columns:

```python
# List schema names
schemas = client.list_schemas()
# ['skymill']

# Get full schema definition
schema = client.get_schema("skymill")
for table in schema.tables:
    print(f"{table.schema_name}.{table.name}")
    for field in table.fields:
        print(f"  {field.name}: {field.type.name}")
```

---

## What's Next

| Topic | Description |
|-------|-------------|
| [Authentication](authentication.md) | Basic auth, bearer tokens, TLS certificates |
| [Querying](querying.md) | SQL queries, ResultSet iteration, paging |
| [DataFrames](dataframes.md) | Arrow, pandas, polars conversions |
| [Async API](async.md) | Async/await with `mill.aio` |
| [Type System](types.md) | Mill's 16 logical types and Python mappings |
| [Discovery](discovery.md) | Service discovery (`.well-known/mill`) |
