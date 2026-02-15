# mill-py Refactoring — Implementation Plan

This document is the authoritative implementation plan for the `mill-py` Python client
refactoring. It covers scope, target architecture, phases, work items, and a tracking
checklist. All phases are implemented on a single branch: `refactor/py-client`.

Related: `py-cold-start.md` (codebase analysis and reference data for cold-start).

---

## 1. Scope

Refactor `clients/mill-py` from its current state (package `millclient`, betterproto/grpclib,
async-only internals) into a production-quality, PyPI-published library:

| Attribute | Current | Target |
|-----------|---------|--------|
| pip install name | `mill-py` | `mill-py` (unchanged) |
| import name | `millclient` | `mill` |
| Proto stubs | `betterproto` 2.0.0b6 (beta) | `grpcio` + `protobuf` (stable) |
| gRPC transport | `grpclib` (async-only) | `grpcio` (sync native, `grpc.aio` for async) |
| HTTP transport | `aiohttp` (async-only, JSON-only send) | `httpx` (sync + async, JSON or protobuf encoding) |
| HTTP encodings | sends JSON / receives protobuf (hardcoded) | configurable: `json` or `protobuf` for both directions |
| API style | async-first, manual event loop | sync-first, async via `mill.aio` |
| Type coverage | 14/16 logical types | 16/16 logical types |
| DataFrame support | pandas/pyarrow baked in | optional extras: `[arrow]`, `[pandas]`, `[ibis]` |
| Temporal lib | `whenever` (third-party) | `datetime` (stdlib) |
| Documentation | none | README + docstrings (Google style) |
| PyPI readiness | not publishable | full metadata, LICENSE, classifiers |
| Python versions | not specified | 3.10 (negotiable), 3.11, 3.12, 3.13 (negotiable) |
| Tests | mostly commented out | unit + integration (new schema TBD) |

---

## 2. Target Package Layout

```
clients/mill-py/
  pyproject.toml                # poetry, PyPI metadata, optional extras
  README.md                     # quickstart, API overview, extras, auth
  LICENSE                       # license file (TBD)
  codegen.py                    # updated for grpcio + protobuf protoc plugin
  mill/                         # import mill
    __init__.py                 # public API: connect(), types, auth, exceptions
    types.py                    # MillType enum, MillField, MillSchema wrappers
    vectors.py                  # VectorBlock -> Python native values (all 16 types)
    client.py                   # MillClient (sync, primary API)
    result.py                   # ResultSet — lazy-with-cache iterable, .fetchall(), .to_arrow(), .to_pandas()
    auth.py                     # BasicAuth, BearerToken
    exceptions.py               # MillError, MillConnectionError, MillQueryError
    discovery.py                # MillServiceDescriptor, fetch_descriptor() — stub for future auto-discovery
    _transport/                 # internal transport abstraction
      __init__.py               # Transport ABC
      _grpc.py                  # gRPC sync transport (grpcio)
      _http.py                  # HTTP sync transport (httpx)
    _proto/                     # generated grpcio + protobuf stubs
      __init__.py
      common_pb2.py
      common_pb2.pyi
      vector_pb2.py
      vector_pb2.pyi
      statement_pb2.py
      statement_pb2.pyi
      data_connect_svc_pb2.py
      data_connect_svc_pb2.pyi
      data_connect_svc_pb2_grpc.py
      substrait/                # substrait proto stubs
        ...
    aio/                        # async API (mill.aio)
      __init__.py               # async connect(), AsyncMillClient
      client.py                 # AsyncMillClient implementation
      _transport/
        _grpc.py                # grpc.aio transport
        _http.py                # httpx.AsyncClient transport
    extras/                     # optional DataFrame backends (all build on Arrow)
      __init__.py
      arrow.py                  # VectorBlock -> pyarrow.RecordBatch -> pyarrow.Table (foundation)
      pandas.py                 # Arrow -> pandas.DataFrame (thin wrapper)
      polars.py                 # Arrow -> polars.DataFrame (thin wrapper)
  tests/
    unit/
      test_types.py             # MillType, MillField, MillSchema
      test_vectors.py           # all 16 type readers
      test_client.py            # client creation, URL parsing
      test_result.py            # ResultSet iteration
      test_arrow.py             # arrow conversions (foundation)
      test_pandas.py            # pandas conversions
      test_polars.py            # polars conversions
    integration/
      conftest.py               # profiles, fixtures
      test_grpc.py              # gRPC transport end-to-end
      test_http.py              # HTTP transport end-to-end
      test_types_e2e.py         # all 16 types round-trip
      test_auth.py              # auth scenarios
```

---

## 3. Target Public API

### 3.1 Sync Client (primary)

```python
from mill import connect

# --- Three transport modes ---

# 1. gRPC (binary protobuf over HTTP/2, server-streaming for queries)
client = connect("grpc://localhost:9099")

# 2. HTTP with JSON encoding (human-readable, good for debugging)
client = connect("http://localhost:8080/services/jet")                    # default: json
client = connect("http://localhost:8080/services/jet", encoding="json")   # explicit

# 3. HTTP with protobuf encoding (binary, more efficient)
client = connect("http://localhost:8080/services/jet", encoding="protobuf")

# Secure variants
client = connect("grpcs://localhost:9099")                                # gRPC + TLS
client = connect("https://localhost:8443/services/jet")                   # HTTP + TLS

# --- Authentication ---

# Anonymous (default — no auth header sent)
client = connect("grpc://localhost:9099")

# Basic auth (username + password, base64-encoded)
from mill.auth import BasicAuth, BearerToken
client = connect("grpc://localhost:9099", auth=BasicAuth("user", "pass"))
client = connect("http://localhost:8080/services/jet", auth=BasicAuth("user", "pass"))

# Bearer token (OAuth2 / JWT)
client = connect("grpc://localhost:9099", auth=BearerToken("eyJhbG..."))
client = connect("http://localhost:8080/services/jet", auth=BearerToken("eyJhbG..."))

# --- Context manager ---
with connect("grpc://localhost:9099") as client:
    ...

# --- Schema operations ---
schemas: list[str] = client.list_schemas()
schema: MillSchema = client.get_schema("MONETA")

for table in schema.tables:
    print(table.name, table.schema_name)
    for field in table.fields:
        print(f"  {field.name}: {field.type}")

# --- Query -> ResultSet ---
result = client.query("SELECT * FROM `MONETA`.`CLIENTS`")

# Core iteration (no extras needed)
for row in result:
    print(row["FIRST_NAME"])

rows: list[dict[str, Any]] = result.fetchall()

# With extras (all build on PyArrow as foundational layer)
table = result.to_arrow()     # requires mill-py[arrow]   -> pyarrow.Table
df = result.to_pandas()       # requires mill-py[pandas]  -> pandas.DataFrame
df = result.to_polars()       # requires mill-py[polars]  -> polars.DataFrame
```

**Encoding parameter** (`encoding`):
- `"json"` (default for HTTP) — requests sent as JSON, responses parsed as JSON. Uses
  `Content-Type: application/json` and `Accept: application/json`.
- `"protobuf"` — requests sent as serialized protobuf, responses parsed as binary protobuf.
  Uses `Content-Type: application/protobuf` and `Accept: application/protobuf`.
- Ignored for gRPC (always binary protobuf over HTTP/2).

### 3.2 Service Discovery (stub — future)

When only a host is provided (no scheme), the client should eventually auto-discover available
protocols by fetching the well-known descriptor. For now this raises `NotImplementedError`.

```python
# Future: auto-discovery via /.well-known/mill
client = connect("myhost.example.com")
# -> GET http://myhost.example.com/.well-known/mill
# -> parse descriptor, pick best protocol, connect
# Currently raises NotImplementedError("Service discovery not implemented yet")

# Explicit URL always works (no discovery)
client = connect("grpc://myhost.example.com:9099")
```

The server exposes `GET /.well-known/mill` (unauthenticated, permitted by
`WellKnownSecurityConfiguration`). Response JSON structure:

```json
{
  "services": [
    { "stereotype": "grpc" },
    { "stereotype": "jet-http" }
  ],
  "security": {
    "enabled": true,
    "authMethods": [
      { "authType": "BASIC" },
      { "authType": "OAUTH2" }
    ]
  },
  "schemas": {
    "MONETA": {
      "name": "MONETA",
      "link": "http://localhost:8080/.well-known/mill/schemas/MONETA"
    }
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `services` | `array` | Available service endpoints. Each has `stereotype`: `"grpc"` or `"jet-http"`. |
| `services[].stereotype` | `string` | Protocol identifier. `"grpc"` = gRPC endpoint, `"jet-http"` = HTTP REST endpoint at `/services/jet`. |
| `security.enabled` | `boolean` | Whether authentication is required. |
| `security.authMethods` | `array` | Supported auth methods. Each has `authType`: `"BASIC"`, `"OAUTH2"`, or `"CUSTOM"`. |
| `schemas` | `object` | Map of schema name to descriptor with `name` and `link` (URL to schema detail). |

**Implementation plan**: Phase 4 adds a stub in `connect()`. When the URL has no recognized
scheme (`grpc://`, `http://`, etc.), the client calls `GET {host}/.well-known/mill`, parses
the response, and raises `NotImplementedError("Service discovery not implemented yet. Use an explicit URL: grpc://host:port or http://host:port/path")`. The parsed descriptor model
(`MillServiceDescriptor`) is defined so future work can implement auto-discovery.

### 3.2 Async Client

```python
from mill.aio import connect as aconnect

async with aconnect("grpc://localhost:9099") as client:
    schemas = await client.list_schemas()
    result = await client.query("SELECT ...")
    async for row in result:
        print(row["FIRST_NAME"])
```

### 3.3 ResultSet — Lazy-with-Cache Design

`ResultSet` uses a **lazy-with-cache** strategy for consuming VectorBlocks from the transport:

1. VectorBlocks are **not** pulled into memory on construction.
2. On first iteration (or `fetchall()` / `to_pandas()`), blocks are fetched lazily from
   the transport iterator — one at a time, on demand.
3. Each block is **cached** internally as it is consumed.
4. Subsequent iterations replay from the cache, fetching remaining blocks if the first pass
   was interrupted (e.g. early `break`).
5. `to_pandas()` / `to_arrow()` / `fetchall()` force full consumption of all remaining blocks.

This gives memory efficiency for large streaming results (process and stop early) while
supporting the common notebook pattern of re-iterating or converting after a first look.

```python
result = client.query("SELECT * FROM large_table")

# First pass — lazy, blocks fetched on demand from transport
for row in result:
    if row["amount"] > 1000:
        break  # only consumed partial data

# Second pass — replays cached blocks, fetches remaining lazily
df = result.to_pandas()  # forces full consumption, uses cache for already-read blocks

# Third pass — fully cached, no transport calls
for row in result:
    print(row["name"])
```

Internal structure:

```python
class ResultSet:
    _source: Iterator[QueryResultResponse]  # transport iterator (lazy)
    _cache: list[VectorBlock]               # consumed blocks (grows as we iterate)
    _exhausted: bool                        # True when source is fully consumed
    _fields: list[MillField] | None         # populated from first block's schema
```

For the **async** variant (`AsyncResultSet`), the same pattern applies with
`AsyncIterator` and `async for`.

### 3.4 Pythonic Type Wrappers

Proto messages are never exposed in the public API. Wrappers provide:

- `MillType` — enum mirroring `LogicalDataTypeId` with Pythonic names
- `MillField` — `name: str`, `type: MillType`, `nullable: bool`, `precision: int`, `scale: int`
- `MillSchema` — `tables: list[MillTable]`
- `MillTable` — `name: str`, `schema_name: str`, `fields: list[MillField]`
- `ResultSet` — lazy-with-cache iterable (see above), with `.fetchall()`, `.to_pandas()`, `.to_arrow()`

---

## 4. Type System — Full 16-Type Coverage

### 4.1 Vector -> Python Native (core, `mill/vectors.py`)

| # | LogicalDataTypeId | Vector field | Python type | Notes |
|---|-------------------|-------------|-------------|-------|
| 1 | `TINY_INT` | `i32_vector` | `int` | |
| 2 | `SMALL_INT` | `i32_vector` | `int` | |
| 3 | `INT` | `i32_vector` | `int` | |
| 4 | `BIG_INT` | `i64_vector` | `int` | |
| 5 | `BOOL` | `bool_vector` | `bool` | |
| 6 | `FLOAT` | `fp32_vector` | `float` | |
| 7 | `DOUBLE` | `fp64_vector` | `float` | |
| 8 | `STRING` | `string_vector` | `str` | |
| 9 | `BINARY` | `byte_vector` | `bytes` | |
| 10 | `UUID` | `byte_vector` | `uuid.UUID` | 16-byte binary -> UUID |
| 11 | `DATE` | `i64_vector` | `datetime.date` | epoch days |
| 12 | `TIME` | `i64_vector` | `datetime.time` | nanos since midnight |
| 13 | `TIMESTAMP` | `i64_vector` | `datetime.datetime` | epoch millis, naive (no tz) |
| 14 | `TIMESTAMP_TZ` | `i64_vector` | `datetime.datetime` | epoch millis, `tzinfo=UTC` |
| 15 | `INTERVAL_DAY` | `i32_vector` | `datetime.timedelta` | day count -> timedelta (NEW) |
| 16 | `INTERVAL_YEAR` | `i32_vector` | `int` | year count as int (NEW) |

Use stdlib `datetime` everywhere. Drop `whenever` dependency.

### 4.2 Vector -> PyArrow (extras, `mill/extras/arrow.py`)

| # | LogicalDataTypeId | PyArrow type | Notes |
|---|-------------------|-------------|-------|
| 1 | `TINY_INT` | `pa.int8()` | was `pa.int32()` — FIX |
| 2 | `SMALL_INT` | `pa.int16()` | was `pa.int32()` — FIX |
| 3 | `INT` | `pa.int32()` | |
| 4 | `BIG_INT` | `pa.int64()` | |
| 5 | `BOOL` | `pa.bool_()` | |
| 6 | `FLOAT` | `pa.float32()` | |
| 7 | `DOUBLE` | `pa.float64()` | |
| 8 | `STRING` | `pa.string()` | |
| 9 | `BINARY` | `pa.binary()` | |
| 10 | `UUID` | `pa.binary(16)` | was `pa.string()` — FIX for compactness |
| 11 | `DATE` | `pa.date32()` | |
| 12 | `TIME` | `pa.time64('ns')` | was `'us'` — FIX to match nanos encoding |
| 13 | `TIMESTAMP` | `pa.timestamp('ms')` | was `pa.date64()` — BUG FIX |
| 14 | `TIMESTAMP_TZ` | `pa.timestamp('ms', tz='UTC')` | was `pa.date64()` — BUG FIX |
| 15 | `INTERVAL_DAY` | `pa.duration('s')` | NEW |
| 16 | `INTERVAL_YEAR` | `pa.int32()` | NEW (no native Arrow interval-year) |

---

## 5. Dependencies

### 5.1 Core (always installed)

```toml
[tool.poetry.dependencies]
python = "^3.10,<4.0"   # 3.10 (negotiable), 3.11, 3.12 (firm), 3.13 (negotiable)
grpcio = "^1.60"
protobuf = "^5.27"
httpx = "^0.27"
```

### 5.2 Extras (optional)

```toml
[tool.poetry.extras]
arrow = ["pyarrow"]
pandas = ["pyarrow", "pandas"]
polars = ["pyarrow", "polars"]
all = ["pyarrow", "pandas", "polars"]
```

### 5.3 Dev / Test

```toml
[tool.poetry.group.dev.dependencies]
grpcio-tools = "*"
pytest = "*"
pytest-asyncio = "*"
pytest-mock = "*"
coverage = "*"
pyarrow = "*"
pandas = "*"
polars = "*"
```

### 5.4 Removed dependencies

| Package | Reason |
|---------|--------|
| `betterproto` | replaced by `grpcio` + `protobuf` |
| `grpclib` | replaced by `grpcio` |
| `aiohttp` | replaced by `httpx` |
| `aiostream` | no longer needed (no manual async iteration) |
| `whenever` | replaced by stdlib `datetime` |
| `logo` | unused |
| `urllib3` | not needed with `httpx` |

---

## 6. Phases and Work Items

**Cross-cutting rule — document as you go**:

1. Every developer-facing (public) function, class, and method must receive a Google-style
   docstring **in the WI that creates it**. Do not defer documentation to Phase 8. Phase 8
   is an audit/polish pass, not initial writing.

2. **User documentation** must be updated alongside each WI when relevant. The public docs
   live under `docs/public/src/connect/python/` (MkDocs Material site). Target audience:
   **Python developers connecting to Mill**. The section is registered in `mkdocs.yml` under
   `Connect > Python`. After completing a WI that introduces or changes user-visible
   behaviour (new API, new auth mode, new extras, etc.), update the corresponding page(s):

   | Page | Covers | Relevant phases |
   |------|--------|-----------------|
   | `connect/python/index.md` | Overview, installation, quickstart | 1, 4 |
   | `connect/python/authentication.md` | BasicAuth, BearerToken, anonymous | 3, 5 |
   | `connect/python/querying.md` | `connect()`, `query()`, ResultSet iteration, `fetchall()` | 4, 5 |
   | `connect/python/dataframes.md` | `to_arrow()`, `to_pandas()`, `to_polars()`, extras install | 6 |
   | `connect/python/async.md` | `mill.aio`, `AsyncMillClient` | 7 |
   | `connect/python/types.md` | Mill type system, Python mappings, PyArrow mappings | 2, 6 |
   | `connect/python/discovery.md` | `.well-known/mill` endpoint, future auto-discovery | 4 |

   Pages can be created incrementally — add each page in the phase where its content first
   becomes relevant. Not every WI needs a docs update; use judgement.

3. **Cleanup after every WI**: At the end of each WI, remove all unused files, functions,
   modules, and dependencies. Dead code must not survive a WI commit. This includes old test
   files, stale imports, and deprecated helpers.

4. **Tests must cover the phase scope**: Tests for each phase must adequately exercise the code
   introduced or modified in that phase. Unit and integration tests are independently
   executable via pytest markers (`-m unit` is the default, `-m integration` for integration).
   After each phase, run coverage and update Section 8 ("Latest Test Results") with the latest
   numbers only.

### Phase 0 — Archive Existing Client

**Branch**: `refactor/py-client`

**Goal**: Preserve the current `clients/mill-py` code as-is under `misc/mill-py/` for
reference during the refactoring. This is a straight copy — no modifications to the archived
code. The original `clients/mill-py/` directory is then cleared and used for the new
implementation starting in Phase 1.

**Handoff gate**: After Phase 0 is complete and pushed, development can be continued by a
different agent or on a different machine. Everything needed is in the repo: the archived
reference code in `misc/mill-py/`, the clean working directory at `clients/mill-py/`, the
`Makefile` for tool verification and proto generation, and the full plan + cold-start docs
under `docs/design/client/`. A new agent should:
1. `git clone` / `git checkout refactor/py-client`
2. Read `docs/design/client/py-cold-start.md` (full context)
3. Read `docs/design/client/py-implementation-plan.md` (phases, rules, checklist)
4. Run `make check-tools` (from repo root) to verify the environment
5. Continue from Phase 1

- [x] **0.1** Copy `clients/mill-py/` to `misc/mill-py/` — Full copy: `millclient/`, `tests/`, `pyproject.toml`, `poetry.lock`, `codegen.py`, notebooks (`sample.ipynb`, `sample_func.ipynb`, `base64.ipynb`). Preserves the old code for side-by-side reference.
- [x] **0.2** Clean `clients/mill-py/` for fresh start — Remove old `millclient/` package, old notebooks, `.build/` if present. Keep `pyproject.toml` (will be rewritten in Phase 1) and `tests/` skeleton.
- [x] **0.3** Add `check-tools` target to root `Makefile` — Verifies all required tools (JDK 21+, Python 3.10+, git, protoc, poetry, node, npm) and optional tools (docker, helm, kubectl, terraform) are installed. Prints versions, fails with clear message if any required tool is missing. Uses a `_check_tool` macro so adding new tools is a one-liner. This is the repo-wide environment check — run `make check-tools` from the repo root.
- [x] **0.4** Verify — `misc/mill-py/` contains a complete snapshot. `clients/mill-py/` is ready for the new `mill/` package. `make check-tools` runs successfully.

---

### Phase 1 — Project Scaffold + Proto Codegen

**Branch**: `refactor/py-client`

**Goal**: Rename package, switch build tooling, generate new proto stubs.

- [x] **1.1** Rename `millclient/` to `mill/` — Move directory, update all internal imports.
- [x] **1.2** Update `pyproject.toml` — New name (`mill-py`), packages `[{include = "mill"}]`, swap dependencies (drop betterproto/grpclib/aiohttp/whenever/logo, add grpcio/protobuf/httpx), add extras sections, add PyPI metadata (description, authors, urls, classifiers, readme).
- [x] **1.3** Update `codegen.py` — Switch from `--python_betterproto_out` to `--python_out` + `--grpc_python_out` + `--pyi_out` targeting `mill/_proto/`. Handle substrait imports. Generate `__init__.py` files.
- [x] **1.4** Generate new stubs — Run `codegen.py`, verify stubs compile. Proto sources: `proto/common.proto`, `proto/vector.proto`, `proto/statement.proto`, `proto/data_connect_svc.proto` + substrait protos.
- [x] **1.5** Create package skeleton — Empty `mill/__init__.py`, `mill/types.py`, `mill/vectors.py`, `mill/client.py`, `mill/result.py`, `mill/auth.py`, `mill/exceptions.py`, `mill/_transport/__init__.py`, `mill/aio/__init__.py`, `mill/extras/__init__.py`.
- [x] **1.6** Verify `poetry install` — Ensure clean install in fresh venv, imports work.
- [x] **1.7** Update `.gitignore` — Add `mill/_proto/` generated stubs pattern if desired, or commit them.

**Decision**: Proto stubs should be committed (same as current approach) so that users installing from PyPI get them without needing protoc.

---

### Phase 2 — Type System + Vector Reading

**Branch**: `refactor/py-client`

**Goal**: Implement Pythonic type wrappers and vector-to-native readers for all 16 types.

- [x] **2.1** `mill/types.py` — `MillType` enum — Enum with 16 members mapping to `LogicalDataTypeId`. Each member carries metadata: `physical_type` (which vector field to read), `python_type` (native type). Provide `from_proto(LogicalDataTypeId) -> MillType` class method.
- [x] **2.2** `mill/types.py` — `MillField` — Dataclass: `name: str`, `index: int`, `type: MillType`, `nullable: bool`, `precision: int`, `scale: int`. Factory: `from_proto(Field) -> MillField`.
- [x] **2.3** `mill/types.py` — `MillTable`, `MillSchema` — `MillTable`: `name`, `schema_name`, `table_type`, `fields: list[MillField]`. `MillSchema`: `tables: list[MillTable]`. Both with `from_proto()` factories.
- [x] **2.4** `mill/vectors.py` — vector readers — Registry of reader functions keyed by `MillType`. Each reader: `(Vector, index) -> native_value`. All 16 types. Use stdlib `datetime` for temporals. Use `datetime.timezone.utc` for `TIMESTAMP_TZ`.
- [x] **2.5** `mill/vectors.py` — `read_vector_block()` — `read_vector_block(VectorBlock) -> tuple[list[MillField], list[dict[str, Any]]]`. Reads a proto `VectorBlock` into field metadata + list of row dicts. Handles nulls via `NullsVector`.
- [x] **2.6** Unit tests — `tests/unit/test_types.py`: enum coverage, from_proto round-trips. `tests/unit/test_vectors.py`: reader for each of 16 types with known input/output, null handling. Reuse existing binary test fixtures from `tests/unit/` if compatible with new proto format.

---

### Phase 3 — Transport Layer

**Branch**: `refactor/py-client`

**Goal**: Abstract transport, implement gRPC and HTTP sync transports.

- [x] **3.1** `mill/_transport/__init__.py` — Transport ABC — Abstract class with methods: `handshake()`, `list_schemas()`, `get_schema(name)`, `parse_sql(sql)`, `exec_query(request) -> Iterator[QueryResultResponse]`. Plus `close()`.
- [x] **3.2** `mill/_transport/_grpc.py` — GrpcTransport — Uses `grpc.insecure_channel()` / `grpc.secure_channel()` + generated blocking stub. `exec_query()` returns server-streaming iterator. Constructor takes `host`, `port`, `ssl`, `metadata`.
- [x] **3.3** `mill/_transport/_http.py` — HttpTransport — Uses `httpx.Client`. Base URL construction from host/port/ssl/base_path. Constructor takes `encoding` parameter (`"json"` or `"protobuf"`). JSON mode: serialize request via `protobuf-java`-compatible JSON (`google.protobuf.json_format`), parse response JSON. Protobuf mode: serialize via `message.SerializeToString()`, parse via `Message.FromString()`. Sets `Content-Type` and `Accept` headers accordingly. `exec_query()` implements paging: calls `SubmitQuery`, then iterates `FetchQueryResult` until no more `paging_id`.
- [x] **3.4** `mill/auth.py` — credentials — Three auth modes matching the server's `AuthenticationType` enum (`BASIC`, `OAUTH2`, `CUSTOM`). `BasicAuth(username, password)` — base64-encodes `user:pass`, produces `Authorization: Basic ...`. `BearerToken(token)` — produces `Authorization: Bearer ...`. Anonymous — no auth header (default when `auth=None`). All credentials expose a unified interface: `to_headers() -> dict[str, str]` (used by both transports). For gRPC: injected as call metadata (lowercase `authorization` key). For HTTP: injected as request headers (capitalized `Authorization` key). The transport layer handles the key-casing difference internally.
- [x] **3.5** `mill/exceptions.py` — error hierarchy — `MillError` (base), `MillConnectionError` (transport failures), `MillQueryError` (server-side query errors), `MillAuthError` (auth/permission failures — maps from gRPC `UNAUTHENTICATED`/`PERMISSION_DENIED` and HTTP 401/403). Map gRPC status codes and HTTP status codes to appropriate exceptions.
- [x] **3.6** Unit tests — Mock-based tests for transport URL construction, header generation (Basic base64 encoding, Bearer token format), error mapping, anonymous (no headers). No live server needed.

---

### Phase 4 — Sync Client + ResultSet

**Branch**: `refactor/py-client`

**Goal**: User-facing sync client and result iteration.

- [x] **4.1** `mill/client.py` — `MillClient` — Constructor takes a `Transport`. Methods: `list_schemas() -> list[str]`, `get_schema(name) -> MillSchema`, `query(sql, *, fetch_size=10000) -> ResultSet`, `parse_sql(sql) -> Plan`. Context manager (`__enter__`/`__exit__`). `close()` delegates to transport.
- [x] **4.2** `mill/result.py` — `ResultSet` — **Lazy-with-cache** design. Constructed from `Iterator[QueryResultResponse]` (transport source). Internal `_cache: list[VectorBlock]` grows as blocks are consumed. `_exhausted: bool` tracks source completion. `_fields` populated from first block's schema. `__iter__` yields `dict[str, Any]` — replays cached blocks first, then pulls remaining from source (caching each). `fetchall()` forces full consumption, returns all rows. `to_arrow()` / `to_pandas()` delegate to extras with lazy imports. Re-iteration replays cache + fetches remaining.
- [x] **4.3** `mill/__init__.py` — `connect()` factory — `connect(url, *, auth=None, encoding="json", **kwargs) -> MillClient`. Parses URL scheme (`grpc://`, `grpcs://`, `http://`, `https://`), extracts host/port/path, creates appropriate transport. `encoding` parameter (`"json"` or `"protobuf"`) passed to HTTP transport (ignored for gRPC). Supports keyword overrides: `host`, `port`, `base_path`. When URL has no recognized scheme (bare hostname), fetches `GET http://{host}/.well-known/mill`, parses the descriptor, and raises `NotImplementedError("Service discovery not implemented yet. Use an explicit URL: grpc://host:port or http://host:port/path")`.
- [x] **4.4** `mill/discovery.py` — discovery stub — `MillServiceDescriptor` dataclass modeling the `.well-known/mill` JSON response: `services: list[MillServiceEndpoint]` (each with `stereotype: str`), `security: MillSecurityDescriptor` (`enabled: bool`, `auth_methods: list[str]`), `schemas: dict[str, MillSchemaLink]` (`name: str`, `link: str`). `fetch_descriptor(host) -> MillServiceDescriptor` function that does `GET http://{host}/.well-known/mill` via `httpx` and parses JSON. This is the extension point for future auto-discovery.
- [x] **4.5** `mill/__init__.py` — public exports — Export: `connect`, `MillClient`, `MillType`, `MillField`, `MillTable`, `MillSchema`, `ResultSet`, `BasicAuth`, `BearerToken`, `MillError`, `MillConnectionError`, `MillQueryError`, `MillAuthError`.
- [x] **4.6** Unit tests — `test_client.py`: URL parsing in `connect()`, client method delegation, **bare hostname triggers discovery stub and raises NotImplementedError**. `test_result.py`: ResultSet iteration with mock responses, fetchall, empty results, **re-iteration after partial consumption (cache replay)**, **re-iteration after full consumption**, **early break then to_pandas() forces full fetch**, **fields populated from first block**.

---

### Phase 5 — Integration Tests (connectivity gate)

**Branch**: `refactor/py-client`

**Goal**: Validate end-to-end connectivity against a live service **before** proceeding to
DataFrame extras. This ensures the core client (transport, types, ResultSet) works correctly
with real gRPC and HTTP services.

**Configuration** — all via environment variables (defaults to `grpc://localhost:9099`, no TLS, no auth):

| Variable | Description | Default |
|---|---|---|
| `MILL_IT_HOST` | Server hostname | `localhost` |
| `MILL_IT_PORT` | Server port (protocol-dependent) | `9099` (gRPC), `8501` (HTTP) |
| `MILL_IT_PROTOCOL` | Protocol: `grpc`, `http-json`, `http-protobuf` | `grpc` |
| `MILL_IT_BASE_PATH` | HTTP base path prefix (ignored for gRPC) | `/services/jet` |
| `MILL_IT_TLS` | Enable TLS: `true` / `false` | `false` |
| `MILL_IT_TLS_CA` | Path to PEM CA certificate file for server verification | _(system default)_ |
| `MILL_IT_TLS_CERT` | Path to PEM client certificate file (mutual-TLS) | _(none)_ |
| `MILL_IT_TLS_KEY` | Path to PEM client private-key file (mutual-TLS) | _(none)_ |
| `MILL_IT_AUTH` | Auth mode: `none`, `basic`, `bearer` | `none` |
| `MILL_IT_USERNAME` | Basic-auth username | `reader` |
| `MILL_IT_PASSWORD` | Basic-auth password | `reader` |
| `MILL_IT_TOKEN` | Bearer token string | _(empty)_ |
| `MILL_IT_SCHEMA` | Schema name to test against | `skymill` |

**Example invocations**:

```bash
# Default — gRPC, localhost:9099, no auth, no TLS
poetry run pytest -m integration

# gRPC on custom host/port
MILL_IT_HOST=backend.local MILL_IT_PORT=9100 poetry run pytest -m integration

# HTTP JSON with custom base path
MILL_IT_PROTOCOL=http-json MILL_IT_HOST=localhost MILL_IT_PORT=8080 \
  MILL_IT_BASE_PATH=/services/jet \
  poetry run pytest -m integration

# HTTP JSON with basic auth and TLS
MILL_IT_PROTOCOL=http-json MILL_IT_HOST=backend.local MILL_IT_PORT=8501 \
  MILL_IT_TLS=true MILL_IT_AUTH=basic MILL_IT_USERNAME=reader MILL_IT_PASSWORD=reader \
  poetry run pytest -m integration

# HTTP protobuf with bearer token
MILL_IT_PROTOCOL=http-protobuf MILL_IT_HOST=host MILL_IT_PORT=8501 \
  MILL_IT_AUTH=bearer MILL_IT_TOKEN=eyJhbGci... \
  poetry run pytest -m integration

# gRPC with custom CA certificate
MILL_IT_TLS=true MILL_IT_TLS_CA=/path/to/ca.pem \
  MILL_IT_HOST=secure.backend MILL_IT_PORT=443 \
  poetry run pytest -m integration

# Mutual-TLS (client certificate + key)
MILL_IT_TLS=true MILL_IT_TLS_CA=/path/to/ca.pem \
  MILL_IT_TLS_CERT=/path/to/client.pem MILL_IT_TLS_KEY=/path/to/client-key.pem \
  MILL_IT_HOST=mtls.backend MILL_IT_PORT=443 \
  poetry run pytest -m integration
```

- [x] **5.1** IntegrationConfig dataclass + `conftest.py` — env var driven (`MILL_IT_*`), session-scoped `mill_client` fixture. Supports grpc, http-json, http-protobuf protocols; TLS toggle; none/basic/bearer auth.
- [x] **5.2** `test_handshake.py` — Validates `ProtocolVersion.V1_0` and capabilities populated.
- [x] **5.3** `test_schemas.py` — list_schemas, get_schema with 17-table validation against skymill, CITIES field verification, nonexistent schema error.
- [x] **5.4** `test_query.py` — SELECT, WHERE filtering, JOIN, empty result with fields, fetchall, iteration, re-iteration, large-query paging (CARGO_SHIPMENTS, BOOKINGS).
- [x] **5.5** `test_auth.py` — Authenticated identity check, wrong-password rejection (basic), invalid-token rejection (bearer). Skipped when `MILL_IT_AUTH=none`.
- [x] **5.6** Docs update — plan checkboxes, test results section.

---

### Phase 6 — DataFrame Extras

**Branch**: `refactor/py-client`

**Goal**: Optional pyarrow (foundation), pandas, and polars conversions.

PyArrow is the **foundational extra** — all DataFrame conversions go through Arrow as the
intermediate format. The conversion chain is:

```
VectorBlock → pyarrow.RecordBatch → pyarrow.Table → pandas.DataFrame / polars.DataFrame
```

This means Arrow conversion logic is written **once**, and pandas/polars are thin wrappers.

- [x] **6.1** `mill/extras/arrow.py` — `result_to_arrow()` + `vector_block_to_record_batch()`. All 16 types mapped per Section 4.2. Narrow int types (TINY_INT, SMALL_INT) cast from i32 wire format via `safe=False`. Null handling via `read_column`. UUID → `binary(16)` bytes, INTERVAL_DAY → `duration('s')` seconds, TIME → `time64('ns')` nanoseconds.
- [x] **6.2** `mill/extras/pandas.py` — `result_to_pandas()`. Thin wrapper: `result_to_arrow(result).to_pandas(types_mapper=pd.ArrowDtype)`.
- [x] **6.3** `mill/extras/polars.py` — `result_to_polars()`. Thin wrapper: `polars.from_arrow(result_to_arrow(result))`.
- [x] **6.4** `mill/result.py` — `to_arrow()`, `to_pandas()`, `to_polars()` already wired in Phase 4 via lazy imports. Verified working.
- [x] **6.5** Unit tests — `test_arrow.py` (22 tests: type mapping, RecordBatch conversion, multi-block Table, nulls, binary fixture), `test_pandas.py` (6 tests: DataFrame basics, nulls, empty), `test_polars.py` (7 tests: DataFrame basics, nulls, empty).

---

### Phase 7 — Async API

**Branch**: `refactor/py-client`

**Goal**: Provide `mill.aio` module mirroring the sync API.

- [x] **7.1** `mill/aio/_transport/__init__.py` — `AsyncTransport` ABC with async abstract methods mirroring sync `Transport`.
- [x] **7.2** `mill/aio/_transport/_grpc.py` — `AsyncGrpcTransport` using `grpc.aio.insecure_channel()` / `grpc.aio.secure_channel()`. Same interface as sync but all methods are `async`. Reuses `_read_pem` and `_from_grpc_error` from sync.
- [x] **7.3** `mill/aio/_transport/_http.py` — `AsyncHttpTransport` using `httpx.AsyncClient`. Same paging logic as sync but with `await`.
- [x] **7.4** `mill/aio/client.py` — `AsyncMillClient` — Mirrors `MillClient`: `await client.list_schemas()`, `await client.query(sql)`. Returns `AsyncResultSet`. Context manager via `__aenter__`/`__aexit__`.
- [x] **7.5** `mill/aio/result.py` — `AsyncResultSet` — Same lazy-with-cache design, backed by `AsyncIterator` source and `async for` iteration. `to_arrow()`/`to_pandas()`/`to_polars()` are async, build from cache after exhaustion.
- [x] **7.6** `mill/aio/__init__.py` — `connect()` — Async version of the factory. Same URL parsing and `encoding` parameter logic.
- [x] **7.7** Unit tests — `test_async_transport.py` — ABC compliance, channel construction, auth headers, encoding.
- [x] **7.8** Unit tests — `test_async_client.py` — Method delegation, `query()` returns `AsyncResultSet`, context manager, `connect()` URL parsing.
- [x] **7.9** Unit tests — `test_async_result.py` — Iteration, `fetchall()`, cache replay, `to_arrow()`/`to_pandas()`/`to_polars()`.
- [x] **7.10** Integration tests — `test_async_query.py` — Async handshake, list schemas, query, fetchall, paging, DataFrame conversion.
- [x] **7.11** `pyproject.toml` — Added `asyncio_mode = "auto"` for pytest-asyncio.
- [x] **7.12** Plan, cold-start, test results updated.

---

### Phase 8 — Documentation + PyPI Polish

**Branch**: `refactor/py-client`

**Goal**: Make the package publishable and well-documented, add `clients/Makefile` for
developer workflows, and document the full PyPI publishing pipeline.

- [ ] **8.1** `README.md` — Sections: Overview, Installation (`pip install mill-py`, extras), Quickstart (connect, query, iterate, DataFrame), Authentication, Async Usage, Type Reference table, Contributing.
- [ ] **8.2** Docstrings — audit & polish — Developer-facing functions must already have docstrings (written during each WI). This WI is an **audit pass**: verify completeness, consistency, add missing usage examples, ensure all `Args`/`Returns`/`Raises` sections are present.
- [ ] **8.3** `LICENSE` — Add Apache-2.0 license file.
- [ ] **8.4** `pyproject.toml` polish — Verify: `readme = "README.md"`, `license = "Apache-2.0"`, `homepage`, `repository`, `keywords`, `classifiers` (Framework, License, Python versions, Topic).
- [ ] **8.5** Build verification — `poetry build` produces valid sdist + wheel. `poetry publish --dry-run` succeeds. Verify `pip install dist/mill_py-*.whl` in clean venv. Test `import mill; mill.connect(...)`.
- [ ] **8.6** Clean up old files — Remove old `sample.ipynb`, `sample_func.ipynb`, `base64.ipynb` (or move to `examples/`). Remove `.build/` directory.
- [ ] **8.7** `clients/Makefile` — Developer workflow targets:
  - `make codegen` — regenerate proto stubs (calls `codegen.py` inside `mill-py/`).
  - `make test` — run unit tests with coverage (`poetry run pytest --cov ...`).
  - `make test-integration` — run integration tests (`poetry run pytest -m integration -v`).
  - `make install` — install `mill-py` into the local Python environment (`pip install -e clients/mill-py[all]`).
  - `make uninstall` — remove `mill-py` from the local environment (`pip uninstall mill-py`).
  - `make build` — `poetry build` (produces sdist + wheel in `clients/mill-py/dist/`).
  - `make publish-test` — publish to PyPI Test (TestPyPI staging).
  - `make publish` — publish to PyPI production (release).
  - `make clean` — remove `dist/`, `.pytest_cache/`, coverage artifacts.
- [ ] **8.8** Playbook updates — Add to the Playbook section (Section 10) of this document:
  - **Install / uninstall mill-py locally** — editable install, clean uninstall, venv tips.
  - **Proto codegen** — regenerate stubs via Makefile.
  - **Publish to TestPyPI (staging)** — step-by-step commands for CI/CD staging pipeline.
  - **Publish to PyPI (release)** — step-by-step commands for release CI/CD pipeline.
  - **Makefile target reference** — table of all targets with descriptions.

---

### Phase 9 — SQL Dialect Foundation

> **Full design, gap analysis, and tracking:
> [`py-sql-dialect-plan.md`](py-sql-dialect-plan.md)**

**Branch**: `refactor/py-client`

**Goal**: Build the shared `mill/sql/` module — dialect descriptor, type mappings, helpers.

**Dependencies for downstream phases**:

| Consumer | Needs from Phase 9 | Slice |
|----------|---------------------|:-----:|
| **Phase 11 (SQLAlchemy)** | `MillDialectDescriptor`, `IdentifierRules`, `CatalogSchemaRules`, `TransactionRules`, `Limits`, `feature_flags`, `paramstyle`, `read_only`, `quote_identifier()`, `qualify()` | 9A |
| **Phase 11 (SQLAlchemy)** | `to_sa_type()`, `SQL_TYPE_NAMES`, `DBAPI_TYPE_CODES`, `TYPE_INFO` | 9A |
| **Phase 11 (SQLAlchemy)** | Confirmed `supports_cte`, `supports_union/intersect/except`, `div_is_floordiv` | 9B |
| **Phase 10 (ibis)** | `to_ibis_dtype()`, full function catalog (window, stats, math) | 9B |
| **Phase 10 (ibis)** | Confirmed `supports_window_functions`, `supports_semi_anti_join`, `supports_cte` | 9B |
| **Phase 10 (ibis)** | `MillDialectDescriptor.feature_flags` for sqlglot dialect subclass | 9A |

---

### Phase 10 — ibis Backend POC

**Branch**: `refactor/py-client`

**Depends on**: Phase 9B — dialect tester + function catalog
([details](py-sql-dialect-plan.md#slice-9b--dialect-tester--function-catalog-p2--p3)).

**Goal**: Proof-of-concept ibis backend that generates SQL from ibis expressions, executes
them on Mill, and returns results as Arrow/DataFrame. This is exploratory — the goal is to
validate the approach and identify gaps, not to ship a production-ready backend.

**Architecture**:

```
ibis expression tree
        |
   ibis SQL compiler (sqlglot / Calcite SQL dialect)
        |
   Generated SQL string
        |
   mill.MillClient.query(sql)
        |
   ResultSet → pyarrow.Table
        |
   ibis Table (backed by Arrow)
```

The ibis backend API:

```python
import mill.ibis as mill_ibis

# Connect to Mill as an ibis backend
con = mill_ibis.connect("grpc://localhost:9099")

# List available schemas/tables (from Mill's get_schema)
con.list_tables(schema="MONETA")

# Reference a table — ibis lazy expression, no query yet
t = con.table("CLIENTS", schema="MONETA")

# Build expression — ibis compiles to SQL only on .execute()
expr = t.filter(t.CLIENT_ID > 100).select("FIRST_NAME", "LAST_NAME")

# Execute — ibis generates SQL, Mill executes, returns DataFrame
result = expr.execute()  # pandas.DataFrame by default
result = expr.to_pyarrow()  # pyarrow.Table
```

- [ ] **10.1** Research ibis backend API — Study ibis `BaseBackend` interface (ibis 7.x+). Understand required methods: `list_tables()`, `table()`, `_to_sqlglot()`, `execute()`, `_fetch_from_cursor()`. Identify the closest sqlglot dialect (likely `calcite` or `ansi`).
- [ ] **10.2** `mill/ibis/__init__.py` — `MillBackend` — Subclass `ibis.backends.BaseBackend`. Constructor wraps a `MillClient`. `connect()` creates the client. `list_tables(schema)` calls `client.get_schema(schema)`. `table(name, schema)` builds ibis schema from `MillField` list using `mill.sql.types.to_ibis_dtype()`.
- [ ] **10.3** SQL generation — Map ibis expressions to SQL via sqlglot. Use dialect rules from `mill/sql/dialect.py`. Test generated SQL against Mill's `ParseSql` to verify compatibility. Document any SQL gaps (e.g. Mill functions not in ibis, or ibis expressions Mill can't handle).
- [ ] **10.4** Execution + result consumption — `execute()` calls `client.query(generated_sql)`, converts `ResultSet` to `pyarrow.Table`, then to pandas (ibis default). Wire `to_pyarrow()` for Arrow output.
- [ ] **10.5** POC tests — Basic queries: filter, select, aggregate, join. Verify SQL generation. Verify results match direct `client.query()` results. Document limitations.
- [ ] **10.6** POC report — Document findings: what works, what doesn't, SQL dialect gaps, ibis version requirements, performance observations, recommended next steps for production-grade backend.

**Dependencies** (dev/optional only — not shipped as a core extra yet):
```toml
[tool.poetry.group.dev.dependencies]
ibis-framework = "*"
```

**This phase is exploratory.** The output is a working POC + a findings document, not a
polished public API.

---

### Phase 11 — DBAPI + SQLAlchemy POC

**Branch**: `refactor/py-client`

**Depends on**: Phase 9A — structural metadata
([details](py-sql-dialect-plan.md#slice-9a--structural-metadata-p0--p1)).

**Goal**: Make `mill-py` a PEP 249 DBAPI 2.0 compliant driver **and** a SQLAlchemy-compatible
dialect. This is exploratory — validate the approach, identify SQL compatibility gaps, and
document limitations. The POC delivers two layers: the DBAPI shim is standalone (usable
without SQLAlchemy), and the SQLAlchemy dialect builds on top of it.

**Architecture**:

```
SQLAlchemy ORM / Core expressions
        |
   SQLAlchemy Dialect  (mill/sqlalchemy/)
        |
   DBAPI 2.0 shim     (mill/dbapi.py)
        |
   mill.MillClient.query(sql)
        |
   ResultSet → rows / cursor
```

**DBAPI target API** (PEP 249 — usable without SQLAlchemy):

```python
import mill.dbapi

conn = mill.dbapi.connect("grpc://localhost:9099")
cursor = conn.cursor()
cursor.execute('SELECT "ID", "CITY" FROM "skymill"."CITIES"')
print(cursor.description)  # ((name, type_code, ...), ...)
for row in cursor.fetchall():
    print(row)
cursor.close()
conn.close()

# Works with pandas directly:
import pandas as pd
df = pd.read_sql('SELECT "ID", "CITY" FROM "skymill"."CITIES"', conn)
```

**SQLAlchemy target API** (built on top of DBAPI):

```python
from sqlalchemy import create_engine, text, MetaData, Table, select

# Connect via mill:// URL scheme
engine = create_engine("mill+grpc://localhost:9099/skymill")
# HTTP variant:
engine = create_engine("mill+http://localhost:8080/skymill?base_path=/services/jet")

# Raw SQL
with engine.connect() as conn:
    result = conn.execute(text('SELECT "ID", "CITY" FROM "CITIES"'))
    for row in result:
        print(row)

# Table reflection
meta = MetaData(schema="skymill")
cities = Table("CITIES", meta, autoload_with=engine)
print(cities.columns.keys())  # ['ID', 'CITY', 'STATE', ...]

# Core expression
stmt = select(cities.c.ID, cities.c.CITY).where(cities.c.ID > 10)
with engine.connect() as conn:
    rows = conn.execute(stmt).fetchall()
```

- [ ] **11.1** `mill/dbapi.py` — PEP 249 DBAPI 2.0 shim:
  - Module constants: `apilevel = "2.0"`, `paramstyle = "qmark"`, `threadsafety = 1`.
  - `connect(url, **kwargs)` → `Connection` — parses URL, creates `MillClient` internally.
  - `Connection` — wraps `MillClient`. Methods: `cursor()`, `close()`, `commit()` (no-op — Mill is read-only), `rollback()` (no-op).
  - `Cursor` — wraps `ResultSet`. Methods: `execute(sql, parameters=None)`, `fetchone()`, `fetchmany(size)`, `fetchall()`, `close()`, `__iter__`. Properties: `description` (column name/type tuples from `ResultSet.fields` using `mill.sql.types.DBAPI_TYPE_CODES`), `rowcount`.
  - Exception hierarchy: `Error`, `DatabaseError`, `OperationalError`, `InterfaceError` — mapped from `MillError` subtypes.
- [ ] **11.2** `mill/sqlalchemy/__init__.py` — `MillDialect`:
  - Subclass `sqlalchemy.engine.default.DefaultDialect`.
  - `name = "mill"`, `driver = "grpc"` (or `"http"`).
  - Read-only flags: `supports_alter = False`, `supports_sane_rowcount = False`.
  - `create_connect_args(url)` — parse SQLAlchemy URL into `mill.dbapi.connect()` args.
  - `do_execute(cursor, statement, parameters, context)` — pass SQL to DBAPI cursor.
  - Type mapping via `mill.sql.types.to_sa_type()`.
  - `MillSQLCompiler` subclass if needed (identifier quoting via `mill.sql.dialect`, `LIMIT`/`OFFSET` rendering).
- [ ] **11.3** Schema introspection on `MillDialect`:
  - `get_schema_names(connection)` → `list_schemas()`.
  - `get_table_names(connection, schema)` → `get_schema(schema)` → table names.
  - `get_columns(connection, table_name, schema)` → column dicts (`name`, `type`, `nullable`).
  - `get_pk_constraint()` → empty dict (Mill doesn't expose PKs).
  - `get_foreign_keys()` → empty list (Mill doesn't expose FKs).
  - `get_indexes()` → empty list (Mill doesn't expose indexes).
  - `has_table(connection, table_name, schema)` → boolean.
- [ ] **11.4** Entry point registration — Register the dialect so `create_engine("mill+grpc://...")` works:
  - `pyproject.toml` entry point: `[tool.poetry.plugins."sqlalchemy.dialects"]` with `mill.grpc = "mill.sqlalchemy:MillDialect"` and `mill.http = "mill.sqlalchemy:MillDialect"`.
- [ ] **11.5** POC tests:
  - DBAPI: `connect()`, `cursor.execute()`, `fetchone()`/`fetchmany()`/`fetchall()`, `description`, exception mapping, `pandas.read_sql()`.
  - SQLAlchemy: `create_engine()` with `mill+grpc://` and `mill+http://` URLs, `conn.execute(text(...))` raw SQL, table reflection via `MetaData.reflect()`, Core expressions (`select()`, `where()`, `join()`), `get_schema_names()`, `get_table_names()`, `get_columns()`.
  - Document what works and what doesn't (no writes, no transactions, no DDL).
- [ ] **11.6** POC findings report — Document: what works, what doesn't, SQL dialect gaps (identifier quoting, type coercion), SQLAlchemy version requirements (2.0+), performance, Calcite SQL compatibility with SA-generated SQL, `paramstyle` limitations (Mill has no parameterised queries — inline only), recommended next steps.

**Key constraints**:
- **Mill is read-only** — `commit()`, `rollback()`, DDL are all no-ops or raise errors.
- **No parameterised queries** — Mill doesn't support bind parameters. The DBAPI `Cursor.execute(sql, params)` must inline parameters into the SQL string. Document security implications.
- **Calcite SQL** — SQLAlchemy's default ANSI compiler should be mostly compatible (double-quote identifiers). A custom `MillSQLCompiler` may be needed for edge cases.

**Dependencies** (dev/optional only — not shipped as a core extra yet):
```toml
[tool.poetry.group.dev.dependencies]
sqlalchemy = ">=2.0"
```

**This phase is exploratory.** The output is a working POC + a findings document, not a
polished public API.

---

## 7. Tracking Checklist

**[Phase 0 — Archive Existing Client](#phase-0--archive-existing-client)**
- [x] 0.1 Copy `clients/mill-py/` to `misc/mill-py/`
- [x] 0.2 Clean `clients/mill-py/` for fresh start
- [x] 0.3 Add `check-tools` target to root `Makefile`
- [x] 0.4 Verify archive, clean working dir, `make check-tools`

**[Phase 1 — Project Scaffold + Proto Codegen](#phase-1--project-scaffold--proto-codegen)**
- [x] 1.1 Rename `millclient/` directory to `mill/`
- [x] 1.2 Rewrite `pyproject.toml` (deps, extras, metadata)
- [x] 1.3 Update `codegen.py` for grpcio + protobuf
- [x] 1.4 Generate and commit new proto stubs in `mill/_proto/`
- [x] 1.5 Create package skeleton (empty modules)
- [x] 1.6 Verify `poetry install` and basic imports
- [x] 1.7 Update `.gitignore`

**[Phase 2 — Type System + Vector Reading](#phase-2--type-system--vector-reading)**
- [x] 2.1 `MillType` enum with all 16 types
- [x] 2.2 `MillField` dataclass + `from_proto()`
- [x] 2.3 `MillTable` + `MillSchema` + `from_proto()`
- [x] 2.4 Vector readers for all 16 types (stdlib datetime)
- [x] 2.5 `read_vector_block()` — VectorBlock to rows
- [x] 2.6 Unit tests for types and vectors

**[Phase 3 — Transport Layer](#phase-3--transport-layer)**
- [x] 3.1 Transport ABC
- [x] 3.2 GrpcTransport (grpcio sync)
- [x] 3.3 HttpTransport (httpx sync)
- [x] 3.4 Auth module (BasicAuth, BearerToken)
- [x] 3.5 Exception hierarchy
- [x] 3.6 Transport unit tests

**[Phase 4 — Sync Client + ResultSet](#phase-4--sync-client--resultset)**
- [x] 4.1 `MillClient` class
- [x] 4.2 `ResultSet` with lazy-with-cache iteration + fetchall
- [x] 4.3 `connect()` factory with URL parsing + discovery stub
- [x] 4.4 `discovery.py` — `MillServiceDescriptor` model + `fetch_descriptor()` + `NotImplementedError`
- [x] 4.5 Public `__init__.py` exports
- [x] 4.6 Client + ResultSet + discovery unit tests

**[Phase 5 — Integration Tests (connectivity gate)](#phase-5--integration-tests-connectivity-gate)**
- [x] 5.1 IntegrationConfig + conftest fixtures (env var driven, skip if unset)
- [x] 5.2 test_handshake — protocol version, capabilities
- [x] 5.3 test_schemas — list/get schema, field validation against skymill
- [x] 5.4 test_query — SELECT, WHERE, JOIN, empty results, fetchall, re-iteration, paging
- [x] 5.5 test_auth — authenticated identity, bad credentials rejection
- [x] 5.6 Docs update

**[Phase 6 — DataFrame Extras](#phase-6--dataframe-extras)**
- [x] 6.1 `extras/arrow.py` — `result_to_arrow()` + `vector_block_to_record_batch()`
- [x] 6.2 `extras/pandas.py` — `result_to_pandas()` (thin Arrow wrapper)
- [x] 6.3 `extras/polars.py` — `result_to_polars()` (thin Arrow wrapper)
- [x] 6.4 Wire `to_arrow()` / `to_pandas()` / `to_polars()` into ResultSet (already stubbed in Phase 4)
- [x] 6.5 Arrow (22 tests) + Pandas (6 tests) + Polars (7 tests) unit tests

**[Phase 7 — Async API](#phase-7--async-api)**
- [x] 7.1 `AsyncTransport` ABC
- [x] 7.2 `AsyncGrpcTransport` (`grpc.aio`)
- [x] 7.3 `AsyncHttpTransport` (`httpx.AsyncClient`)
- [x] 7.4 `AsyncMillClient`
- [x] 7.5 `AsyncResultSet`
- [x] 7.6 Async `connect()` factory
- [x] 7.7–7.9 Unit tests (transport, client, result)
- [x] 7.10 Integration tests (async query, DataFrame)
- [x] 7.11 `pyproject.toml` — `asyncio_mode = "auto"`
- [x] 7.12 Plan, cold-start, test results updated

**[Phase 8 — Documentation + PyPI Polish](#phase-8--documentation--pypi-polish)**
- [x] 8.1 `README.md`
- [x] 8.2 Docstrings audit & polish
- [x] 8.3 `LICENSE` — Apache-2.0
- [x] 8.4 `pyproject.toml` metadata polish
- [x] 8.5 `poetry build` + install verification
- [x] 8.6 Clean up old notebooks and build artifacts
- [x] 8.7 `clients/Makefile` (codegen, test, test-integration, install, uninstall, build, publish-test, publish, clean)
- [x] 8.8 Playbook updates (local install/uninstall, codegen, TestPyPI staging, PyPI release, Makefile reference)
- [x] 8.9 Examples (`clients/examples/quickstart.py` + `mill_py_quickstart.ipynb`)
- [x] 8.10 User documentation (`docs/public/src/connect/python/` — 7 pages in nav + installation.md draft)

**[Phase 9A — Structural Metadata](py-sql-dialect-plan.md#slice-9a--structural-metadata)** (unblocks Phase 11, JDBC, AI — no live Mill needed)
- [ ] 9A.1–9A.7 (tracked in [`py-sql-dialect-plan.md`](py-sql-dialect-plan.md#7-work-items))

**[Phase 11 — DBAPI + SQLAlchemy POC](#phase-11--dbapi--sqlalchemy-poc)** (depends on 9A)
- [ ] 11.1 `mill/dbapi.py` — PEP 249 DBAPI 2.0 shim (Connection, Cursor, exceptions)
- [ ] 11.2 `mill/sqlalchemy/` — `MillDialect` (DefaultDialect subclass, type mapping, compiler)
- [ ] 11.3 Schema introspection (`get_schema_names`, `get_table_names`, `get_columns`, etc.)
- [ ] 11.4 Entry point registration (`mill+grpc://`, `mill+http://`)
- [ ] 11.5 POC tests (DBAPI, SQLAlchemy raw SQL, reflection, Core expressions, `pandas.read_sql`)
- [ ] 11.6 POC findings report

**[Phase 9B — Dialect Tester + Function Catalog](py-sql-dialect-plan.md#slice-9b--dialect-tester--function-catalog-p2--p3)** (unblocks Phase 10, enriches AI — requires live Mill)
- [ ] 9B.1–9B.8 (tracked in [`py-sql-dialect-plan.md`](py-sql-dialect-plan.md#7-work-items))

**[Phase 10 — ibis Backend POC](#phase-10--ibis-backend-poc)** (depends on 9B)
- [ ] 10.1 Research ibis `BaseBackend` API
- [ ] 10.2 `mill/ibis/` — `MillBackend` subclassing `BaseBackend`
- [ ] 10.3 SQL generation — ibis expressions → Calcite-compatible SQL
- [ ] 10.4 Execution + result consumption via `MillClient.query()`
- [ ] 10.5 POC tests — filter, select, aggregate, join
- [ ] 10.6 POC findings report

---

## 8. Latest Test Results

Updated after each phase. Shows the most recent run only.

| Metric | Value |
|--------|-------|
| **Phase** | 8 — Documentation + PyPI Polish |
| **Unit tests** | 461 passed |
| **Integration tests** | 23 sync (19 passed, 4 skipped auth) + 12 async tests |
| **Coverage** | 85% (900 stmts, 132 missed) |
| **Build** | `poetry build` — sdist + wheel OK; clean venv import verified |
| **Unit command** | `cd clients && make test` |
| **Integration command** | `cd clients && make test-integration` |

---

## 9. Git Workflow — Branch & Commit Rules

All implementation happens on a **single branch**: `refactor/py-client`.

### Rules

1. **One commit per work item.** Each WI (e.g. 0.1, 1.3, 4.2) must be represented as
   exactly one commit on `refactor/py-client`.
2. **Squash during work.** While implementing a WI you may create as many intermediate
   commits as needed. Once the WI is complete and passing, squash all intermediate commits
   for that WI into a single commit before moving to the next WI.
3. **Commit message format.** Use the bracketed prefix style with the WI number:
   ```
   [refactor] 2.3 MillTable + MillSchema dataclasses with from_proto()
   ```
   Keep the summary line under 72 characters. Add a body if the change is non-trivial.
4. **Push after every WI.** After squashing, push to `origin/refactor/py-client` so
   progress is always backed up remotely.
5. **Never rebase or force-push published history.** Once a squashed WI commit has been
   pushed, treat it as immutable. Only squash *unpushed* intermediate commits.

### Typical WI lifecycle

```bash
# 1. Implement (may involve multiple intermediate commits)
git add -A && git commit -m "[wip] 2.3 initial MillTable"
# ... more work ...
git add -A && git commit -m "[wip] 2.3 add from_proto and tests"

# 2. Squash all WI commits into one
git rebase -i HEAD~2          # squash the 2 intermediate commits
# edit message → [refactor] 2.3 MillTable + MillSchema dataclasses with from_proto()

# 3. Push
git push origin refactor/py-client
```

### Branch setup (one-time)

The branch `refactor/py-client` should already exist. If starting fresh:

```bash
git fetch origin
git checkout refactor/py-client        # or create from origin/feature if needed
```

---

## 10. Playbook

### Run unit tests

Unit tests require no running server. They use synthetic protobuf data and mocks.

```bash
cd clients/mill-py

# Install dependencies (first time / CI setup)
poetry install --all-extras

# Run unit tests (default — only tests marked "unit")
poetry run pytest

# With coverage report
poetry run pytest --cov=mill --cov-report=term-missing tests/unit/

# Produce XML coverage for CI upload
poetry run pytest --cov=mill --cov-report=xml:coverage.xml tests/unit/

# Produce JUnit XML results for CI
poetry run pytest --junitxml=results.xml tests/unit/

# Combined (typical CI command)
poetry run pytest --cov=mill --cov-report=xml:coverage.xml --junitxml=results.xml tests/unit/
```

**Requirements**: Python 3.10–3.13, Poetry.  
**Expected result**: 409+ tests pass, 88%+ coverage.  
**Exit code**: `0` on success, non-zero on failure.

### Run integration tests

Integration tests require a running Mill service with the **skymill** dataset.
All configuration is via environment variables. Defaults to `grpc://localhost:9099`.

```bash
cd clients/mill-py

# Install dependencies (first time / CI setup)
poetry install --all-extras

# Default — gRPC on localhost:9099, no auth, no TLS
poetry run pytest -m integration -v

# HTTP JSON
MILL_IT_PROTOCOL=http-json \
  MILL_IT_HOST=localhost \
  MILL_IT_PORT=8080 \
  poetry run pytest -m integration -v

# HTTP protobuf
MILL_IT_PROTOCOL=http-protobuf \
  MILL_IT_HOST=localhost \
  MILL_IT_PORT=8080 \
  poetry run pytest -m integration -v

# gRPC with TLS and basic auth
MILL_IT_PROTOCOL=grpc \
  MILL_IT_HOST=backend.local \
  MILL_IT_PORT=443 \
  MILL_IT_TLS=true \
  MILL_IT_TLS_CA=/path/to/ca.pem \
  MILL_IT_AUTH=basic \
  MILL_IT_USERNAME=reader \
  MILL_IT_PASSWORD=reader \
  poetry run pytest -m integration -v

# Produce JUnit XML for CI
MILL_IT_PROTOCOL=http-json MILL_IT_HOST=localhost MILL_IT_PORT=8080 \
  poetry run pytest -m integration --junitxml=integration-results.xml -v
```

**Environment variables**:

| Variable | Description | Default |
|---|---|---|
| `MILL_IT_HOST` | Server hostname | `localhost` |
| `MILL_IT_PORT` | Server port | `9099` (gRPC) / `8501` (HTTP) |
| `MILL_IT_PROTOCOL` | `grpc`, `http-json`, `http-protobuf` | `grpc` |
| `MILL_IT_BASE_PATH` | HTTP base path prefix (ignored for gRPC) | `/services/jet` |
| `MILL_IT_TLS` | `true` / `false` | `false` |
| `MILL_IT_TLS_CA` | Path to PEM CA certificate | _(system default)_ |
| `MILL_IT_TLS_CERT` | Path to PEM client certificate (mTLS) | _(none)_ |
| `MILL_IT_TLS_KEY` | Path to PEM client private key (mTLS) | _(none)_ |
| `MILL_IT_AUTH` | `none`, `basic`, `bearer` | `none` |
| `MILL_IT_USERNAME` | Basic-auth username | `reader` |
| `MILL_IT_PASSWORD` | Basic-auth password | `reader` |
| `MILL_IT_TOKEN` | Bearer token string | _(empty)_ |
| `MILL_IT_SCHEMA` | Schema name | `skymill` |

**Requirements**: Python 3.10–3.13, Poetry, a running Mill service with skymill data.  
**Expected result**: 24+ tests pass (auth tests skip when `MILL_IT_AUTH=none`).  
**Exit code**: `0` on success, non-zero on failure.

### Proto codegen

Regenerate protobuf stubs when `.proto` files change. Requires `grpcio-tools`.

```bash
# Via clients/Makefile
cd clients && make codegen

# Or directly
cd clients/mill-py && python codegen.py
```

This regenerates `mill/_proto/` from `proto/*.proto` and `proto/substrait/*.proto`.
Generated stubs are committed to git (required for PyPI distribution).

### Install / uninstall mill-py locally

```bash
# Via clients/Makefile — editable install with all extras
cd clients && make install

# Or directly with pip
pip install -e "clients/mill-py[all]"

# Verify
python -c "import mill; print(mill.__all__)"

# Uninstall
cd clients && make uninstall
# Or: pip uninstall mill-py
```

**Tips**:
- Use a virtual environment: `python -m venv .venv && source .venv/bin/activate`
- Editable install (`-e`) means code changes take effect immediately.
- `[all]` installs arrow, pandas, and polars extras. For core only: `pip install -e clients/mill-py`

### Publish to TestPyPI (staging)

TestPyPI is the staging environment for validating packages before a production release.
Use this in CI/CD staging pipelines.

```bash
cd clients/mill-py

# 1. Ensure version is set correctly in pyproject.toml
#    (bump version before publishing)

# 2. Build sdist + wheel
poetry build

# 3. Configure TestPyPI repository (one-time per environment)
poetry config repositories.testpypi https://test.pypi.org/legacy/

# 4. Configure TestPyPI API token (one-time or via CI secret)
poetry config pypi-token.testpypi pypi-XXXXXXXXXXXX

# 5. Publish to TestPyPI
poetry publish --repository testpypi

# 6. Verify installation from TestPyPI
pip install --index-url https://test.pypi.org/simple/ --extra-index-url https://pypi.org/simple/ mill-py
python -c "from mill import connect; print('OK')"
```

**Via Makefile** (steps 2 + 5 combined, assumes token is already configured):

```bash
cd clients && make build && make publish-test
```

**CI/CD environment variables**:
- `POETRY_PYPI_TOKEN_TESTPYPI` — TestPyPI API token (set as CI secret).

### Publish to PyPI (production release)

Production releases go to the real PyPI. Use this in release CI/CD pipelines.

```bash
cd clients/mill-py

# 1. Ensure version is correct and changelog is updated
# 2. Ensure all tests pass: make test && make test-integration

# 3. Build
poetry build

# 4. Configure PyPI API token (one-time or via CI secret)
poetry config pypi-token.pypi pypi-XXXXXXXXXXXX

# 5. Publish
poetry publish

# 6. Verify
pip install mill-py && python -c "from mill import connect; print('OK')"
```

**Via Makefile** (steps 3 + 5):

```bash
cd clients && make build && make publish
```

**CI/CD environment variables**:
- `POETRY_PYPI_TOKEN_PYPI` — PyPI API token (set as CI secret).

**Version bumping**: Use `poetry version patch/minor/major` or edit `pyproject.toml`
directly. Follow semantic versioning. Tag the release commit with `v<version>`.

### Makefile target reference (`clients/Makefile`)

| Target | Description |
|---|---|
| `make help` | Show all available targets |
| `make codegen` | Regenerate proto stubs via `codegen.py` |
| `make test` | Run unit tests with coverage |
| `make test-integration` | Run integration tests (requires running Mill service) |
| `make install` | Install mill-py in editable mode (`pip install -e`) |
| `make uninstall` | Uninstall mill-py from current environment |
| `make build` | Build sdist + wheel (`poetry build`) |
| `make publish-test` | Publish to TestPyPI (staging) |
| `make publish` | Publish to PyPI (production) |
| `make clean` | Remove dist/, caches, coverage artifacts |

---

## 11. Open Decisions

| # | Question | Status |
|---|----------|--------|
| 1 | License type for the package | **Apache-2.0** — matches rest of Mill codebase; all deps compatible (audit done). |
| 2 | Integration test schema / data model | TBD — user will provide |
| 3 | Should proto stubs be committed or .gitignored? | Committed (required for PyPI sdist) |
| 4 | Calcite SQL dialect compatibility (shared by ibis + SQLAlchemy) | Gap analysis complete (Phase 9 §9A). Type mapping fully covered via `CalciteTypeMapper`. Main gaps: window functions, statistical aggregates, extended math functions, CTE/set-op flags. Server-driven `GetDialect` RPC planned. |
| 5 | ibis backend — ship as `mill-py[ibis]` extra or separate package? | Phase 10 POC findings |
| 6 | SQLAlchemy — ship as `mill-py[sqlalchemy]` extra or separate package? | Phase 11 POC findings |
| 7 | DBAPI paramstyle — Mill has no bind params; inline-only security implications? | Phase 11 POC will document. `paramstyle = "qmark"` planned but params must be inlined. |
| 8 | gRPC reflection / health check support? | Out of scope for this round |
| 9 | Service discovery — full implementation? | Stub only (Phase 4); future work after server-side matures |
| 10 | Server-side `GetDialect` RPC | Planned (Phase 9 §9D). Client builds `MillDialectDescriptor` model now with hard-coded Calcite defaults; server populates from YAML later. Handshake flag `supports_dialect` gates the call. |
| 11 | Server YAML dialect gaps (window funcs, stats, math) | Need server-side YAML additions before `GetDialect` can report them. Phase 9.1 live testing will determine what Calcite actually supports. |
