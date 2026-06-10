# ibis + Mill Example (WI-025 slice 1)

This document captures the first implementation slice for ibis integration in
`clients/mill-py/mill/ibis`.

## 1. Installation

```bash
pip install "qpointz-mill-py[ibis]"
```

## 2. Connect over gRPC or HTTP

```python
from mill.ibis import connect

# gRPC
grpc_backend = connect("grpc://localhost:9090")

# HTTP
http_backend = connect(
    "http://localhost:8501",
    base_path="/services/jet",
    encoding="json",
)
```

## 3. Build and execute ibis expressions

```python
cities = grpc_backend.table("cities", database="skymill")
expr = cities.select("id", "city").limit(10)

frame = grpc_backend.execute(expr)
print(frame.head())
```

## 4. Raw SQL interoperability

```python
import ibis

sql_expr = grpc_backend.sql(
    'SELECT "id", "city" FROM "skymill"."cities" LIMIT 5',
    schema=ibis.schema({"id": "int32", "city": "string"}),
)
rows = grpc_backend.execute(sql_expr)
```

## 5. Known limitations surfaced in this iteration

- Backend is intentionally read-only (`create_table`, `drop_table`, `create_view`, `drop_view` are not supported).
- Set-operation coverage (`INTERSECT`, `EXCEPT`) is gated by dialect flags and still needs WI-023 certification.
- SQL compilation currently uses ibis SQL compiler defaults (DuckDB-style SQL generation) plus Mill capability checks; dialect-specific rewrites are pending.
- Function-by-function certification against dialect YAML metadata remains pending WI-023.
