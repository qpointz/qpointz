# SQLAlchemy + Mill Example

This document describes the delivered WI-024 SQLAlchemy integration surface in
`clients/mill-py/mill/sqlalchemy`.

## 0. Installation

```bash
pip install "qpointz-mill-py[sqlalchemy]"
```

## 1. Engine Creation

```python
from sqlalchemy import create_engine

# gRPC transport
grpc_engine = create_engine("mill+grpc://localhost:9090")

# HTTP transport
http_engine = create_engine(
    "mill+http://localhost:8080"
    "?base_path=/services/jet"
    "&encoding=json"
)
```

## 2. Core Query Execution

```python
from sqlalchemy import text

with grpc_engine.connect() as conn:
    rows = conn.execute(text('SELECT "ID", "CITY" FROM "skymill"."CITIES"')).fetchall()
    for row in rows:
        print(row)
```

## 3. Reflection / Introspection

```python
from sqlalchemy import inspect

inspector = inspect(grpc_engine)

schemas = inspector.get_schema_names()
tables = inspector.get_table_names(schema="skymill")
columns = inspector.get_columns("CITIES", schema="skymill")

print(schemas)
print(tables)
print(columns[0]["name"], columns[0]["type"])
```

## 4. Notes

- Dialect metadata is fetched from Mill server `GetDialect` when available.
- Compiler behavior (quoting/paging) reads dialect metadata with conservative fallbacks.
- Current DBAPI adapter is read-only and intended for query/introspection paths.
