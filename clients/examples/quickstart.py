"""mill-py quick-start examples.

Run with:
    python quickstart.py

Assumes a Mill service is running locally.
Adjust HOST / PORT / SCHEMA constants below to match your setup.
"""
from __future__ import annotations

# ---------------------------------------------------------------------------
# Configuration — adjust to your environment
# ---------------------------------------------------------------------------
GRPC_HOST = "localhost"
GRPC_PORT = 9099
HTTP_HOST = "localhost"
HTTP_PORT = 8080
HTTP_BASE_PATH = "/services/jet"
SCHEMA = "skymill"


def example_grpc_connect():
    """Connect via gRPC, list schemas, run a query."""
    from mill import connect

    print("=" * 60)
    print("Example 1: gRPC connection")
    print("=" * 60)

    with connect(f"grpc://{GRPC_HOST}:{GRPC_PORT}") as client:
        # Handshake
        resp = client.handshake()
        print(f"Protocol version: {resp.version}")

        # List schemas
        schemas = client.list_schemas()
        print(f"Available schemas: {schemas}")

        # Query
        sql = f'SELECT "id", "city", "country_code" FROM "{SCHEMA}"."cities" LIMIT 5'
        print(f"\nSQL: {sql}")
        result = client.query(sql)
        for row in result:
            print(f"  {row}")

    print()


def example_http_connect():
    """Connect via HTTP (JSON encoding)."""
    from mill import connect

    print("=" * 60)
    print("Example 2: HTTP connection (JSON)")
    print("=" * 60)

    url = f"http://{HTTP_HOST}:{HTTP_PORT}{HTTP_BASE_PATH}"
    with connect(url) as client:
        schemas = client.list_schemas()
        print(f"Schemas: {schemas}")

        sql = f'SELECT "id", "city" FROM "{SCHEMA}"."cities" LIMIT 3'
        result = client.query(sql)
        rows = result.fetchall()
        print(f"Fetched {len(rows)} rows:")
        for row in rows:
            print(f"  {row}")

    print()


def example_http_protobuf():
    """Connect via HTTP with protobuf encoding (more efficient)."""
    from mill import connect

    print("=" * 60)
    print("Example 3: HTTP connection (protobuf encoding)")
    print("=" * 60)

    url = f"http://{HTTP_HOST}:{HTTP_PORT}{HTTP_BASE_PATH}"
    with connect(url, encoding="protobuf") as client:
        sql = f'SELECT COUNT(*) AS "cnt" FROM "{SCHEMA}"."cities"'
        result = client.query(sql)
        row = next(iter(result))
        print(f"City count: {row['cnt']}")

    print()


def example_schema_introspection():
    """Explore schema metadata — tables, fields, types."""
    from mill import connect

    print("=" * 60)
    print("Example 4: Schema introspection")
    print("=" * 60)

    with connect(f"grpc://{GRPC_HOST}:{GRPC_PORT}") as client:
        schema = client.get_schema(SCHEMA)

        for table in schema.tables:
            print(f"\n{table.schema_name}.{table.name} ({table.table_type.name})")
            for field in table.fields:
                null = "NULL" if field.nullable else "NOT NULL"
                print(f"  [{field.index}] {field.name}: {field.type.name} {null}")

    print()


def example_authentication():
    """Connect with authentication credentials."""
    from mill import connect
    from mill.auth import BasicAuth, BearerToken

    print("=" * 60)
    print("Example 5: Authentication")
    print("=" * 60)

    # Basic auth
    try:
        with connect(
            f"grpc://{GRPC_HOST}:{GRPC_PORT}",
            auth=BasicAuth("reader", "password"),
        ) as client:
            resp = client.handshake()
            print(f"Basic auth — identity: {resp.authentication.name}")
    except Exception as e:
        print(f"Basic auth — {type(e).__name__}: {e}")

    # Bearer token
    try:
        with connect(
            f"grpc://{GRPC_HOST}:{GRPC_PORT}",
            auth=BearerToken("eyJhbGciOiJSUzI1NiJ9.example"),
        ) as client:
            resp = client.handshake()
            print(f"Bearer auth — identity: {resp.authentication.name}")
    except Exception as e:
        print(f"Bearer auth — {type(e).__name__}: {e}")

    # Anonymous (default)
    with connect(f"grpc://{GRPC_HOST}:{GRPC_PORT}") as client:
        resp = client.handshake()
        print(f"Anonymous — identity: {resp.authentication.name}")

    print()


def example_dataframes():
    """Convert query results to Arrow, pandas, and polars DataFrames."""
    from mill import connect

    print("=" * 60)
    print("Example 6: DataFrame conversions")
    print("=" * 60)

    with connect(f"grpc://{GRPC_HOST}:{GRPC_PORT}") as client:
        sql = f'SELECT "id", "city", "country_code" FROM "{SCHEMA}"."cities" LIMIT 10'

        # PyArrow Table
        result = client.query(sql)
        arrow_table = result.to_arrow()
        print(f"Arrow Table: {arrow_table.num_rows} rows x {arrow_table.num_columns} cols")
        print(f"  Schema: {arrow_table.schema}")

        # pandas DataFrame
        result = client.query(sql)
        df = result.to_pandas()
        print(f"\npandas DataFrame:\n{df.to_string(index=False)}")

        # polars DataFrame
        result = client.query(sql)
        pl_df = result.to_polars()
        print(f"\npolars DataFrame:\n{pl_df}")

    print()


def example_error_handling():
    """Demonstrate error handling patterns."""
    from mill import connect, MillConnectionError, MillQueryError

    print("=" * 60)
    print("Example 7: Error handling")
    print("=" * 60)

    # Bad SQL
    try:
        with connect(f"grpc://{GRPC_HOST}:{GRPC_PORT}") as client:
            client.query("SELECT FROM WHERE INVALID").fetchall()
    except MillQueryError as e:
        print(f"Query error (expected): {e}")

    # Unreachable server
    try:
        with connect("grpc://nonexistent.host:9099") as client:
            client.handshake()
    except MillConnectionError as e:
        print(f"Connection error (expected): {e}")

    print()


def example_async():
    """Async API — mirrors sync exactly with async/await."""
    import asyncio

    async def _run():
        from mill.aio import connect as aconnect

        print("=" * 60)
        print("Example 8: Async API")
        print("=" * 60)

        async with await aconnect(f"grpc://{GRPC_HOST}:{GRPC_PORT}") as client:
            schemas = await client.list_schemas()
            print(f"Schemas: {schemas}")

            sql = f'SELECT "id", "city" FROM "{SCHEMA}"."cities" LIMIT 5'
            result = await client.query(sql)

            print("Rows (async for):")
            async for row in result:
                print(f"  {row}")

            # Async DataFrame conversion
            result2 = await client.query(sql)
            df = await result2.to_pandas()
            print(f"\nAsync pandas:\n{df.to_string(index=False)}")

        print()

    asyncio.run(_run())


def example_result_iteration():
    """Demonstrate ResultSet lazy iteration and re-iteration."""
    from mill import connect

    print("=" * 60)
    print("Example 9: ResultSet — lazy iteration & caching")
    print("=" * 60)

    with connect(f"grpc://{GRPC_HOST}:{GRPC_PORT}") as client:
        sql = f'SELECT "id", "city" FROM "{SCHEMA}"."cities" LIMIT 10'
        result = client.query(sql)

        # Partial iteration — only pull first 3 rows
        print("First 3 rows (lazy — only fetches what's needed):")
        for i, row in enumerate(result):
            print(f"  {row}")
            if i == 2:
                break

        # Re-iterate — cached rows replayed, then remaining fetched
        print("\nAll rows (re-iteration — cached + remaining):")
        all_rows = result.fetchall()
        print(f"  Total: {len(all_rows)} rows")

        # Field metadata available after first fetch
        print(f"\nFields: {[f'{f.name} ({f.type.name})' for f in result.fields]}")

    print()


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    import sys

    examples = {
        "grpc": example_grpc_connect,
        "http": example_http_connect,
        "protobuf": example_http_protobuf,
        "schema": example_schema_introspection,
        "auth": example_authentication,
        "dataframes": example_dataframes,
        "errors": example_error_handling,
        "async": example_async,
        "iteration": example_result_iteration,
    }

    if len(sys.argv) > 1:
        # Run specific example(s)
        for name in sys.argv[1:]:
            if name in examples:
                examples[name]()
            else:
                print(f"Unknown example: {name}")
                print(f"Available: {', '.join(examples)}")
                sys.exit(1)
    else:
        # Run all examples
        print("mill-py Examples")
        print("================")
        print(f"gRPC endpoint: grpc://{GRPC_HOST}:{GRPC_PORT}")
        print(f"HTTP endpoint: http://{HTTP_HOST}:{HTTP_PORT}{HTTP_BASE_PATH}")
        print(f"Schema: {SCHEMA}")
        print()
        for fn in examples.values():
            try:
                fn()
            except Exception as e:
                print(f"  [SKIP] {type(e).__name__}: {e}\n")
