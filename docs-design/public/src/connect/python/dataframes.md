# DataFrame Conversions

Convert query results to PyArrow Tables, pandas DataFrames, or polars
DataFrames. All conversions use PyArrow as the foundation.

---

## Install Extras

DataFrame support requires optional extras:

```bash
pip install qpointz-mill-py[arrow]       # PyArrow only
pip install qpointz-mill-py[pandas]      # pandas (includes Arrow)
pip install qpointz-mill-py[polars]      # polars (includes Arrow)
pip install qpointz-mill-py[all]         # everything
```

---

## PyArrow Table

```python
from mill import connect

with connect("grpc://localhost:9090") as client:
    result = client.query('SELECT "id", "city" FROM "skymill"."cities"')
    table = result.to_arrow()

print(table.schema)
# id: int32
# city: string

print(table.num_rows)
# 100

# Filter with Arrow compute
import pyarrow.compute as pc
filtered = table.filter(pc.field("id") > 50)
```

---

## pandas DataFrame

```python
result = client.query('SELECT "id", "city", "country_code" FROM "skymill"."cities"')
df = result.to_pandas()

# Standard pandas operations
print(df.head())
print(df.groupby("country_code").size())
print(df.describe())
```

The conversion uses Arrow-backed dtypes (`pd.ArrowDtype`) for efficient
memory usage and native null support.

---

## polars DataFrame

```python
result = client.query('SELECT "id", "city", "country_code" FROM "skymill"."cities"')
df = result.to_polars()

# Standard polars operations
print(df.head())
print(df.group_by("country_code").count())
print(df.filter(df["id"] > 50))
```

polars uses zero-copy conversion from Arrow for best performance.

---

## Conversion Architecture

All DataFrame conversions go through the same pipeline:

```
Server VectorBlock → pyarrow.RecordBatch → pyarrow.Table
                                                ├─→ pandas.DataFrame  (via to_pandas())
                                                └─→ polars.DataFrame  (via from_arrow())
```

This means:

- Arrow logic is written **once** — pandas and polars are thin wrappers.
- All 16 Mill types are correctly mapped to Arrow types (see [Type System](types.md)).
- Null values are preserved through the entire chain.

---

## Missing Extras

If you call a DataFrame method without the required extra installed,
a clear `ImportError` is raised:

```python
result = client.query("SELECT 1")
result.to_pandas()
# ImportError: pandas is required for to_pandas().
# Install it with: pip install qpointz-mill-py[pandas]
```

---

## Large Results

For large result sets, consider using Arrow directly for memory efficiency:

```python
result = client.query("SELECT * FROM large_table")

# to_arrow() forces full consumption into memory
table = result.to_arrow()

# Process in batches if needed
for batch in table.to_batches(max_chunksize=10000):
    process(batch)
```

Or iterate row-by-row without loading everything into memory:

```python
result = client.query("SELECT * FROM large_table")
for row in result:
    process_row(row)  # one row at a time, constant memory
```
