# ibis Backend

Use `mill.ibis` to run ibis expressions on Mill data services.

---

## Install

```bash
pip install "qpointz-mill-py[ibis]"
```

---

## Connect

```python
from mill.ibis import connect

# gRPC
backend = connect("grpc://localhost:9090")

# HTTP
backend_http = connect(
    "http://localhost:8501",
    base_path="/services/jet",
    encoding="json",
)
```

---

## Query with ibis expressions

```python
cities = backend.table("cities", database="skymill")
expr = cities.select("id", "city").limit(10)

frame = backend.execute(expr)
print(frame.head())
```

---

## Raw SQL with ibis wrappers

```python
import ibis

expr = backend.sql(
    'SELECT "id", "city" FROM "skymill"."cities" LIMIT 5',
    schema=ibis.schema({"id": "int32", "city": "string"}),
)
frame = backend.execute(expr)
```

---

## First Iteration Limitations

- Backend is read-only in WI-025 slice 1.
- Capability-gating is active for features like `WITH`, `INTERSECT`, `EXCEPT`, and `LATERAL`.
- Full correctness certification and feature matrix are tracked in WI-023.
