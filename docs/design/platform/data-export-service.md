# Data Export Service – gRPC Data Provider Interface

## Overview

The Data Export Service defines a lightweight gRPC contract that decouples the Mill query engine from individual data sources. A **data provider** is any process — written in any language — that can describe its catalog and stream table data as `VectorBlock` chunks. The query layer (Calcite / Mill) retains full ownership of SQL parsing, planning, joins, and aggregations; providers are passive data feeders.

---

## Motivation

The Mill query engine currently executes plans against backends that are tightly coupled to the Java runtime (Calcite adapters, JDBC drivers). This creates two limitations:

1. **Non-SQL data sources** (REST APIs, file stores, NoSQL databases, in-memory datasets) cannot be accessed without writing a Java/Calcite adapter.
2. **Non-Java ecosystems** (Python, Go, Rust) cannot serve as data backends without bridging into the JVM.

By introducing a gRPC boundary between the query layer and data sources, any process that speaks protobuf can become a first-class Mill data provider.

---

## Architecture

```
┌──────────────────────────────────┐
│      Mill Query Engine           │
│   (Calcite SQL / Substrait)      │
│                                  │
│  ┌────────────────────────────┐  │
│  │  DataExport gRPC Client    │  │
│  └────────┬───────────────────┘  │
└───────────┼──────────────────────┘
            │ gRPC (HTTP/2 + Protobuf)
            │
    ┌───────┴────────┬──────────────────┐
    │                │                  │
    ▼                ▼                  ▼
┌─────────┐   ┌───────────┐   ┌──────────────┐
│ Python  │   │   Go      │   │   Any Lang   │
│ Provider│   │  Provider │   │   Provider   │
│ (pandas,│   │ (custom   │   │              │
│  DB-API)│   │  source)  │   │              │
└─────────┘   └───────────┘   └──────────────┘
```

Each provider implements the same 4-RPC service. The query engine discovers available schemas and tables through catalog RPCs, then streams table data on demand via `PullTable`. Multiple providers can run concurrently, enabling federated queries across heterogeneous data sources.

---

## Service Definition

The service reuses existing Mill proto message types from `common.proto` and `vector.proto`. Only a single new `.proto` file is required.

### Proto

```protobuf
syntax = "proto3";
package io.qpointz.mill;

import "common.proto";
import "vector.proto";

service DataExportService {
  // Protocol handshake — returns server version
  rpc Handshake (HandshakeRequest) returns (HandshakeResponse);

  // Catalog browsing
  rpc ListSchemas (ListSchemasRequest) returns (ListSchemasResponse);
  rpc GetSchema (GetSchemaRequest) returns (GetSchemaResponse);

  // Data streaming — full table pull
  rpc PullTable (PullTableRequest) returns (stream PullTableResponse);
}

// --- Handshake ---

message HandshakeRequest {}

message HandshakeResponse {
  ProtocolVersion version = 1;
}

// --- Schema browsing ---

message ListSchemasRequest {}

message ListSchemasResponse {
  repeated string schemas = 1;
}

message GetSchemaRequest {
  string schema_name = 1;
}

message GetSchemaResponse {
  Schema schema = 1;
}

// --- Table pull ---

message PullTableRequest {
  string schema_name = 1;
  string object_name = 2;
}

message PullTableResponse {
  VectorBlock vector = 1;
}
```

### Reused Messages

The following messages are imported from existing protos — no duplication:

| Message | Source | Purpose |
|---------|--------|---------|
| `ProtocolVersion` | `common.proto` | Protocol negotiation |
| `Schema` | `common.proto` | Collection of tables |
| `Table` | `common.proto` | Table metadata (name, type, fields) |
| `Field` | `common.proto` | Column metadata (name, index, data type) |
| `DataType` | `common.proto` | Logical type + nullability |
| `LogicalDataType` | `common.proto` | Type ID, precision, scale |
| `VectorBlock` | `vector.proto` | Columnar data batch |
| `VectorBlockSchema` | `vector.proto` | Schema of a vector block |
| `Vector` | `vector.proto` | Typed column data (string, i32, i64, fp32, fp64, bool, bytes) |

### Not Needed

The following existing protos are **not** imported or required:

- `statement.proto` — SQL statements and parameters
- `substrait/*.proto` — Substrait plan definitions
- `data_connect_svc.proto` — existing full query service

---

## RPC Specifications

### Handshake

| | |
|---|---|
| **Type** | Unary |
| **Purpose** | Protocol version negotiation |
| **Request** | Empty |
| **Response** | `ProtocolVersion` (e.g. `V1_0`) |

The client uses the handshake to verify compatibility before issuing catalog or data requests.

### ListSchemas

| | |
|---|---|
| **Type** | Unary |
| **Purpose** | Enumerate available schemas |
| **Request** | Empty |
| **Response** | `repeated string schemas` — list of schema names |

### GetSchema

| | |
|---|---|
| **Type** | Unary |
| **Purpose** | Retrieve full metadata for a schema |
| **Request** | `schema_name` — target schema |
| **Response** | `Schema` containing `Table` entries, each with `Field` definitions |

The response provides everything the query engine needs to register tables in the Calcite catalog: table names, column names, data types, nullability, and field ordering.

### PullTable

| | |
|---|---|
| **Type** | Server-streaming |
| **Purpose** | Stream full table contents as columnar batches |
| **Request** | `schema_name` + `object_name` — identifies the table |
| **Response stream** | Sequence of `PullTableResponse`, each containing one `VectorBlock` |

The provider reads the underlying data source in batches (e.g. 1000–5000 rows per block), converts each batch to a `VectorBlock`, and streams it to the client. The stream completes when all rows have been sent.

#### VectorBlock Structure

Each `VectorBlock` contains:
- `VectorBlockSchema` — field definitions (name, index, type) for columns in this block
- `vectorSize` — number of rows in this block
- `repeated Vector` — one `Vector` per column, each containing:
  - `fieldIdx` — column index
  - `NullsVector` — boolean array marking null positions
  - Typed values (one of): `StringVector`, `I32Vector`, `I64Vector`, `FP32Vector`, `FP64Vector`, `BoolVector`, `BytesVector`

#### Type Mapping

The provider maps source data types to the 7 available vector types:

| LogicalDataTypeId | Vector Type | Notes |
|---|---|---|
| `STRING` | `StringVector` | |
| `TINY_INT`, `SMALL_INT`, `INT` | `I32Vector` | |
| `BIG_INT` | `I64Vector` | |
| `FLOAT` | `FP32Vector` | |
| `DOUBLE` | `FP64Vector` | |
| `BOOL` | `BoolVector` | |
| `BINARY` | `BytesVector` | |
| `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ` | `StringVector` | ISO-8601 string serialization |
| `UUID` | `StringVector` | Standard UUID string |
| `INTERVAL_DAY`, `INTERVAL_YEAR` | `StringVector` | ISO-8601 duration |

---

## Error Handling

Providers use standard gRPC status codes:

| Condition | gRPC Status |
|---|---|
| Schema not found | `NOT_FOUND` |
| Table not found | `NOT_FOUND` |
| Provider cannot connect to underlying source | `UNAVAILABLE` |
| Internal error during data read | `INTERNAL` |
| Request validation failure | `INVALID_ARGUMENT` |

---

## Provider Implementation Contract

A conforming provider must:

1. **Implement all 4 RPCs** — even if the provider has a single schema, `ListSchemas` must return it.
2. **Return consistent metadata** — the `Field` types in `GetSchema` must match the `Vector` types streamed by `PullTable`.
3. **Stream in bounded batches** — each `VectorBlock` should contain a reasonable number of rows (recommended: 1000–5000) to avoid unbounded memory use.
4. **Handle concurrent requests** — the gRPC server must support multiple simultaneous `PullTable` streams.

A provider does **not** need to:
- Parse or understand SQL
- Handle Substrait plans
- Manage query pagination or cursors
- Implement authentication (handled at transport/infrastructure level)

---

## Complexity Estimate

For a reference Python implementation backed by SQLAlchemy / DB-API2:

| Component | Effort | Size (est.) |
|---|---|---|
| Proto definition | 0.25 day | ~40 lines |
| gRPC server skeleton | 0.25 day | ~60 lines |
| VectorBlock construction | 1–2 days | ~200–250 lines |
| Catalog metadata RPCs | 0.5 day | ~80 lines |
| Handshake | trivial | ~5 lines |
| **Total** | **~2–3 days** | **~400 lines** |

The VectorBlock construction layer (row-to-columnar conversion with type mapping and null tracking) is the single non-trivial component. All other RPCs are thin catalog wiring.

---

## Implementation Plan

### Project Location

New module under `services/mill-data-export-py/` — a standalone Python project, sibling to the existing Java services. Follows the same patterns as the existing Python client at `clients/mill-py/` (betterproto for codegen, poetry for dependency management).

```
services/mill-data-export-py/
├── pyproject.toml
├── codegen.py
├── README.md
├── mill_data_export/
│   ├── __init__.py
│   ├── server.py              # gRPC server entry point + wiring
│   ├── service.py             # DataExportServicer implementation (4 RPCs)
│   ├── catalog.py             # Schema/table metadata provider (abstract + SQLAlchemy impl)
│   ├── vectors.py             # VectorBlock construction from tabular data
│   └── proto/                 # Generated proto stubs (betterproto)
│       └── io/qpointz/mill/
│           └── __init__.py
└── tests/
    ├── __init__.py
    ├── test_vectors.py        # VectorBlock construction unit tests
    ├── test_catalog.py        # Catalog metadata tests
    └── test_service.py        # Service integration tests
```

### Step 1: New Proto File

Create `proto/data_export_svc.proto` alongside the existing protos. Imports only `common.proto` and `vector.proto` — no Substrait, no statement protos.

File contents are as specified in the **Service Definition** section above.

### Step 2: Proto Codegen

Reuse the codegen pattern from `clients/mill-py/codegen.py` — it uses `grpc_tools.protoc` with `--python_betterproto_out` to generate Python stubs. The new `codegen.py` will:

1. Point `in_dir` to `../../proto` (same relative path as the existing client)
2. Generate only the 3 needed protos: `common.proto`, `vector.proto`, `data_export_svc.proto`
3. Output to `mill_data_export/proto/`

Key reference — existing codegen in `clients/mill-py/codegen.py`:

```python
args = [
    "",
    f"-I{in_dir}",
    "-I/usr/include/",
    f"--python_betterproto_out={out_dir}"
]
args = args + glob.glob(f"{in_dir}/**/*.proto") + glob.glob(f"{in_dir}/*.proto")
protoc.main(command_arguments=args)
```

For the export service, limit the glob to only the needed files instead of `**/*.proto` to avoid pulling in Substrait protos.

### Step 3: Catalog Provider

Abstract interface for schema/table metadata, with a concrete SQLAlchemy implementation.

`catalog.py` defines:

```python
class CatalogProvider(ABC):
    @abstractmethod
    def list_schemas(self) -> list[str]: ...

    @abstractmethod
    def get_schema(self, schema_name: str) -> Schema: ...

    @abstractmethod
    def read_table(self, schema_name: str, object_name: str, batch_size: int) -> Iterator[list[tuple]]: ...

    @abstractmethod
    def get_table_fields(self, schema_name: str, object_name: str) -> list[Field]: ...
```

`SqlAlchemyCatalogProvider(CatalogProvider)` implements this using:
- `inspect(engine).get_schema_names()` for `list_schemas`
- `inspect(engine).get_columns(table, schema=schema)` for field metadata
- `connection.execute(select(...)).fetchmany(batch_size)` for `read_table`

The type mapping from SQLAlchemy column types to `LogicalDataTypeId` lives here. Reference the existing reverse mapping in `clients/mill-py/millclient/utils.py` — the server-side mapping is the inverse:

| SQLAlchemy Type | LogicalDataTypeId |
|---|---|
| `String`, `Text`, `Unicode` | `STRING` |
| `SmallInteger` | `SMALL_INT` |
| `Integer` | `INT` |
| `BigInteger` | `BIG_INT` |
| `Float` | `FLOAT` |
| `Numeric`, `Double` | `DOUBLE` |
| `Boolean` | `BOOL` |
| `LargeBinary` | `BINARY` |
| `Date` | `DATE` |
| `Time` | `TIME` |
| `DateTime` (no tz) | `TIMESTAMP` |
| `DateTime` (with tz) | `TIMESTAMP_TZ` |
| `Uuid` | `UUID` |
| `Interval` | `INTERVAL_DAY` |

### Step 4: VectorBlock Construction

`vectors.py` is the core piece (~200-250 lines). It converts batches of rows from `CatalogProvider.read_table()` into `VectorBlock` proto messages.

The construction is the inverse of what `clients/mill-py/millclient/utils.py` does for reading. That file reads vectors into Python values; this file writes Python values into vectors.

Per-column writer functions, keyed by `LogicalDataTypeId`:

| LogicalDataTypeId | Writer Logic | Target Vector |
|---|---|---|
| `STRING` | `str(value)` | `StringVector` |
| `TINY_INT`, `SMALL_INT`, `INT` | `int(value)` | `I32Vector` |
| `BIG_INT` | `int(value)` | `I64Vector` |
| `FLOAT` | `float(value)` | `FP32Vector` |
| `DOUBLE` | `float(value)` | `FP64Vector` |
| `BOOL` | `bool(value)` | `BoolVector` |
| `BINARY` | `bytes(value)` | `BytesVector` |
| `DATE` | `int(days_since_epoch)` | `I64Vector` |
| `TIME` | `int(nanoseconds)` | `I64Vector` |
| `TIMESTAMP` | `int(millis_since_epoch)` | `I64Vector` |
| `TIMESTAMP_TZ` | `int(millis_since_epoch)` | `I64Vector` |
| `UUID` | `uuid.bytes` | `BytesVector` |

Note: temporal types use I64 encoding to match the existing Java server wire format (as observed in `utils.py` readers — `__date_reader`, `__timestamp_reader` etc. all read from `i64_vector`).

Core function signature:

```python
def rows_to_vector_block(
    fields: list[Field],
    rows: list[tuple],
) -> VectorBlock:
    """Convert a batch of rows into a columnar VectorBlock."""
```

Logic:
1. Initialize per-column accumulators (typed value lists + null lists)
2. Iterate rows; for each row, append each column value to the appropriate accumulator, tracking nulls
3. Build `Vector` messages from accumulators using the writer for each field's `LogicalDataTypeId`
4. Assemble `VectorBlock` with `VectorBlockSchema`, `vectorSize`, and the vector list

### Step 5: gRPC Service Implementation

`service.py` — subclass of generated `DataExportServiceBase` (betterproto async style):

```python
class DataExportServiceImpl(DataExportServiceBase):

    def __init__(self, catalog: CatalogProvider, batch_size: int = 2000):
        self.catalog = catalog
        self.batch_size = batch_size

    async def handshake(self, request: HandshakeRequest) -> HandshakeResponse:
        return HandshakeResponse(version=ProtocolVersion.V1_0)

    async def list_schemas(self, request: ListSchemasRequest) -> ListSchemasResponse:
        schemas = self.catalog.list_schemas()
        return ListSchemasResponse(schemas=schemas)

    async def get_schema(self, request: GetSchemaRequest) -> GetSchemaResponse:
        schema = self.catalog.get_schema(request.schema_name)
        return GetSchemaResponse(schema=schema)

    async def pull_table(self, request: PullTableRequest) -> AsyncIterator[PullTableResponse]:
        fields = self.catalog.get_table_fields(request.schema_name, request.object_name)
        for batch in self.catalog.read_table(request.schema_name, request.object_name, self.batch_size):
            block = rows_to_vector_block(fields, batch)
            yield PullTableResponse(vector=block)
```

### Step 6: Server Entry Point

`server.py` — configures and starts the gRPC server:

- Parse CLI args or env vars: `--db-url` (SQLAlchemy connection string), `--port` (default 50051), `--batch-size` (default 2000)
- Create `SqlAlchemyCatalogProvider(engine)`
- Create `DataExportServiceImpl(catalog)`
- Start `grpclib` async server (matching the existing `mill-py` client's use of `grpclib`)

### Dependencies

`pyproject.toml`:

```toml
[tool.poetry.dependencies]
python = "^3.10,<4.0"
protobuf = "^5.27.2"
betterproto = "^2.0.0b6"
grpclib = "*"
sqlalchemy = "^2.0"

[tool.poetry.group.dev.dependencies]
betterproto = {version="^2.0.0b6", extras=["compiler"]}
grpcio-tools = "*"
pytest = "*"
pytest-asyncio = "*"
```

DB-specific drivers (e.g. `psycopg2`, `pymysql`) are left as optional extras for the user to install depending on their backend.

### File-by-File Summary

| File | Lines (est.) | Purpose |
|---|---|---|
| `proto/data_export_svc.proto` | ~40 | New service + request/response definitions |
| `codegen.py` | ~40 | Proto compilation script |
| `pyproject.toml` | ~30 | Project metadata + dependencies |
| `mill_data_export/catalog.py` | ~120 | Abstract catalog + SQLAlchemy implementation |
| `mill_data_export/vectors.py` | ~200 | Row-to-VectorBlock columnar conversion |
| `mill_data_export/service.py` | ~60 | gRPC service implementation (4 RPCs) |
| `mill_data_export/server.py` | ~50 | Server entry point + config |
| **Total** | **~540** | |

---

## Future Extensions

These are explicitly **out of scope** for the initial design but can be added as optional fields:

- **Column projection** in `PullTableRequest` — request specific columns instead of full table scan
- **Row filtering** in `PullTableRequest` — push simple predicates down to the provider
- **Partitioning hints** — allow the query engine to parallelize pulls across partitions
- **Schema change notifications** — streaming metadata updates when the underlying source evolves
