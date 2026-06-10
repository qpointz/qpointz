# SQLAlchemy

Use the Mill SQLAlchemy dialect to run SQLAlchemy Core queries over Mill gRPC or
HTTP transports.

---

## Install

```bash
pip install "qpointz-mill-py[sqlalchemy]"
```

---

## Connect

```python
from sqlalchemy import create_engine

# gRPC
grpc_engine = create_engine("mill+grpc://localhost:9090")

# HTTP (JSON encoding)
http_engine = create_engine(
    "mill+http://localhost:8501?base_path=/services/jet&encoding=json"
)
```

---

## Query

```python
from sqlalchemy import text

with grpc_engine.connect() as conn:
    rows = conn.execute(
        text('SELECT "id", "city" FROM "skymill"."cities" LIMIT 10')
    ).fetchall()
    print(rows[0])
```

---

## Introspection

```python
from sqlalchemy import inspect

inspector = inspect(grpc_engine)
schemas = inspector.get_schema_names()
tables = inspector.get_table_names(schema="skymill")
columns = inspector.get_columns("cities", schema="skymill")
```

---

## Current Scope

- Read/query and schema introspection are supported.
- Write paths are intentionally not implemented in the DBAPI adapter.
- Dialect metadata is consumed from `GetDialect` when available.
