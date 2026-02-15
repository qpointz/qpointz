# Querying

Execute SQL queries against Mill and consume results row-by-row or in bulk.

---

## Running a Query

```python
from mill import connect

with connect("grpc://localhost:9099") as client:
    result = client.query('SELECT "id", "city" FROM "skymill"."cities" LIMIT 10')
    for row in result:
        print(row)
    # {'id': 1, 'city': 'Tokyo', ...}
```

The `query()` method returns a `ResultSet` — a lazy iterator over result rows.

### SQL Conventions

Mill uses Apache Calcite SQL. Key conventions:

- **Double-quote** all identifiers: `"schema"."table"."column"`
- Identifiers are **case-sensitive** when quoted
- Use `schema.table` format: `"skymill"."cities"`

```python
# Correct — quoted identifiers
result = client.query('SELECT "first_name" FROM "skymill"."customers"')

# Aggregation
result = client.query('''
    SELECT "country_code", COUNT(*) AS "cnt"
    FROM "skymill"."cities"
    GROUP BY "country_code"
    ORDER BY "cnt" DESC
    LIMIT 5
''')
```

### Fetch Size

Control how many rows the server sends per page:

```python
result = client.query("SELECT ...", fetch_size=1000)   # smaller pages
result = client.query("SELECT ...", fetch_size=50000)   # larger pages
```

Default is 10,000. Larger values reduce round-trips but use more memory.

---

## ResultSet

`ResultSet` uses a **lazy-with-cache** strategy:

1. Rows are fetched from the server **on demand** as you iterate.
2. Fetched blocks are **cached internally** for re-iteration.
3. `fetchall()` forces complete consumption.

### Row-by-Row Iteration

Each row is a `dict[str, Any]` mapping column names to Python-native values:

```python
result = client.query('SELECT "id", "city" FROM "skymill"."cities"')

for row in result:
    print(f"{row['id']}: {row['city']}")
```

### Fetch All Rows

```python
rows = result.fetchall()
# [{'id': 1, 'city': 'Tokyo'}, {'id': 2, 'city': 'London'}, ...]
```

### Partial Iteration

You can stop early — only the needed data is fetched:

```python
result = client.query("SELECT ... FROM large_table")

# Only fetch first 5 rows
for i, row in enumerate(result):
    print(row)
    if i == 4:
        break
```

### Re-Iteration

After partial or full iteration, you can iterate again. Cached rows are
replayed, then any remaining data is fetched:

```python
result = client.query('SELECT "id", "city" FROM "skymill"."cities"')

# First pass — fetch first 3 rows
for i, row in enumerate(result):
    if i == 2:
        break

# Second pass — replays cached rows, then fetches the rest
all_rows = result.fetchall()
```

### Field Metadata

Access column metadata after the first row is fetched:

```python
result = client.query('SELECT "id", "city" FROM "skymill"."cities"')
_ = result.fetchall()  # ensure at least one block is consumed

for field in result.fields:
    print(f"{field.name}: {field.type.name} (nullable={field.nullable})")
# id: INT (nullable=True)
# city: STRING (nullable=True)
```

---

## Error Handling

Query errors raise `MillQueryError`:

```python
from mill import connect, MillQueryError

try:
    client.query("SELECT bad syntax").fetchall()
except MillQueryError as e:
    print(f"Query failed: {e}")
```

Connection errors raise `MillConnectionError`:

```python
from mill import connect, MillConnectionError

try:
    client = connect("grpc://unreachable:9099")
    client.handshake()
except MillConnectionError as e:
    print(f"Cannot reach server: {e}")
```

All Mill exceptions inherit from `MillError`:

| Exception | When |
|-----------|------|
| `MillError` | Base class for all Mill errors |
| `MillConnectionError` | Server unreachable, timeout, TLS failure |
| `MillAuthError` | Authentication or authorization failure |
| `MillQueryError` | Bad SQL, missing table, server execution error |
