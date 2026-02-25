# Async API

The `mill.aio` module provides an asynchronous API that mirrors the synchronous
client exactly, using `async`/`await`.

---

## When to Use Async

Use the async API when:

- Your application is already async (FastAPI, aiohttp, etc.)
- You need to run multiple queries concurrently
- You're building event-driven data pipelines

For notebooks and scripts, the synchronous API is usually simpler.

---

## Connecting

```python
from mill.aio import connect

# Await the connect() coroutine
client = await connect("grpc://localhost:9090")

# Or use as an async context manager
async with await connect("grpc://localhost:9090") as client:
    schemas = await client.list_schemas()
```

`connect()` accepts the same parameters as the sync version — `auth`,
`encoding`, `tls_ca`, `tls_cert`, `tls_key`, and URL overrides.

```python
from mill.aio import connect
from mill.auth import BasicAuth

async with await connect(
    "http://localhost:8080/services/jet",
    auth=BasicAuth("user", "pass"),
    encoding="protobuf",
) as client:
    ...
```

---

## Schema Operations

All methods are coroutines — use `await`:

```python
# List schemas
schemas = await client.list_schemas()

# Get schema details
schema = await client.get_schema("skymill")
for table in schema.tables:
    print(table.name)

# Handshake
resp = await client.handshake()
```

---

## Querying

```python
result = await client.query('SELECT "id", "city" FROM "skymill"."cities"')

# Async iteration
async for row in result:
    print(row["city"])
```

### Fetch All

```python
result = await client.query("SELECT ...")
rows = await result.fetchall()
```

### DataFrame Conversion

```python
result = await client.query("SELECT ...")

# All to_* methods are async
table = await result.to_arrow()
df = await result.to_pandas()
pl_df = await result.to_polars()
```

---

## Concurrent Queries

Run multiple queries in parallel:

```python
import asyncio
from mill.aio import connect

async with await connect("grpc://localhost:9090") as client:
    # Launch queries concurrently
    results = await asyncio.gather(
        client.query('SELECT COUNT(*) AS "n" FROM "skymill"."cities"'),
        client.query('SELECT COUNT(*) AS "n" FROM "skymill"."countries"'),
    )

    for rs in results:
        rows = await rs.fetchall()
        print(rows[0]["n"])
```

---

## Full Example

```python
import asyncio
from mill.aio import connect
from mill.auth import BasicAuth

async def main():
    async with await connect("grpc://localhost:9090") as client:
        # Introspection
        schemas = await client.list_schemas()
        print(f"Schemas: {schemas}")

        # Query to pandas
        result = await client.query('''
            SELECT "country_code", COUNT(*) AS "cnt"
            FROM "skymill"."cities"
            GROUP BY "country_code"
            ORDER BY "cnt" DESC
            LIMIT 5
        ''')
        df = await result.to_pandas()
        print(df)

asyncio.run(main())
```

---

## API Reference

| Sync (`mill`) | Async (`mill.aio`) |
|---|---|
| `connect(url)` | `await connect(url)` |
| `client.list_schemas()` | `await client.list_schemas()` |
| `client.get_schema(name)` | `await client.get_schema(name)` |
| `client.query(sql)` | `await client.query(sql)` |
| `for row in result` | `async for row in result` |
| `result.fetchall()` | `await result.fetchall()` |
| `result.to_arrow()` | `await result.to_arrow()` |
| `result.to_pandas()` | `await result.to_pandas()` |
| `result.to_polars()` | `await result.to_polars()` |
| `client.close()` | `await client.close()` |
| `with connect(...) as c` | `async with await connect(...) as c` |
