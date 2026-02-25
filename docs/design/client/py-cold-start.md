# mill-py Cold Start — Codebase Analysis & Agent Reference

This document captures everything an agent needs to understand the current `mill-py` codebase,
the server-side services it communicates with, the protobuf contract, the Mill type system, and
all known issues. Read this before starting any phase of the refactoring.

Related:
- `py-implementation-plan.md` — phases, work items, overall tracking.
- `py-sql-dialect-plan.md` — Phase 9 (SQL Dialect Foundation) design, gap analysis, and tracking.

### Quick Start for New Agents

**Current state** (update this after each phase):

- **Branch**: `refactor/py-client` — all work happens here.
- **Last completed phase**: **Phase 8 — Documentation + PyPI Polish**.
- **Next phase**: **Phase 9 — SQL Dialect Foundation** (see dedicated
  [`py-sql-dialect-plan.md`](py-sql-dialect-plan.md) for full design, gap analysis, and tracking).
- **Tests**: 461 unit tests passing (85% coverage excl. `_proto/`), 23 sync integration tests (19 pass, 4 skipped auth) + 12 async integration tests.
- **Build**: `poetry build` produces sdist + wheel; verified via clean venv install.
- **License**: Apache-2.0 (`LICENSE` file + `pyproject.toml` classifier).
- **No old code remains in `clients/mill-py/`** — everything references the new `mill` package.

**Steps to resume**:

1. `git fetch origin && git checkout refactor/py-client` — all work happens on this branch.
2. `cd clients/mill-py && poetry install --all-extras` — set up the Python environment.
3. `cd clients && make test` — verify 461 unit tests pass (default runs only `unit` marker).
4. `cd clients && make test-integration` — run integration tests (requires a live server;
   configure via `MILL_IT_*` env vars — see `py-implementation-plan.md` Section 10).
5. `make check-tools` (from repo root) — verify your environment has all required tools.
6. Read **this document** end-to-end for codebase context, type system, protocols, and rules.
7. Read `py-implementation-plan.md` for overall plan, then
   [`py-sql-dialect-plan.md`](py-sql-dialect-plan.md) for Phase 9 details and tracking.
8. Reference code is archived at `misc/mill-py/` for side-by-side comparison.
9. Follow all rules in Section 16 of this document (git workflow, code style, testing, etc.).

**Key files**:

| File | What it does |
|------|-------------|
| `mill/__init__.py` | Public API, sync `connect()` factory |
| `mill/types.py` | `MillType` enum (16 types), `MillField`, `MillTable`, `MillSchema` dataclasses with `from_proto()` |
| `mill/vectors.py` | Per-type reader functions, `read_column()`, `read_vector_block()` |
| `mill/_proto/` | Generated grpcio/protobuf stubs (do not hand-edit) |
| `mill/_transport/__init__.py` | `Transport` ABC |
| `mill/_transport/_grpc.py` | `GrpcTransport` (sync, grpcio) |
| `mill/_transport/_http.py` | `HttpTransport` (sync, httpx) |
| `mill/auth.py` | `BasicAuth`, `BearerToken`, `_auth_headers()` |
| `mill/exceptions.py` | `MillError` hierarchy + error mappers |
| `mill/client.py` | `MillClient` — sync client |
| `mill/result.py` | `ResultSet` — lazy-with-cache result iteration |
| `mill/extras/arrow.py` | `result_to_arrow()`, `vector_block_to_record_batch()` |
| `mill/extras/pandas.py` | `result_to_pandas()` |
| `mill/extras/polars.py` | `result_to_polars()` |
| `mill/discovery.py` | `MillServiceDescriptor` + `fetch_descriptor()` stub |
| `mill/aio/__init__.py` | Async `connect()` factory |
| `mill/aio/client.py` | `AsyncMillClient` — async client |
| `mill/aio/result.py` | `AsyncResultSet` — async lazy-with-cache iteration |
| `mill/aio/_transport/__init__.py` | `AsyncTransport` ABC |
| `mill/aio/_transport/_grpc.py` | `AsyncGrpcTransport` (grpc.aio) |
| `mill/aio/_transport/_http.py` | `AsyncHttpTransport` (httpx.AsyncClient) |
| `tests/unit/` | 461 unit tests (types, vectors, transport, client, result, arrow, pandas, polars, async) |
| `tests/integration/` | Integration tests (handshake, schemas, query, auth, async query) |
| `tests/conftest.py` | Auto-applies `unit`/`integration` pytest markers based on directory |
| `pyproject.toml` | Poetry config with pytest markers, coverage config, extras, `asyncio_mode = "auto"` |
| `LICENSE` | Apache-2.0 license file |
| `README.md` | Full library documentation: install, quickstart, auth, TLS, async, types, errors |
| `codegen.py` | Proto stub generator (grpcio-tools → `mill/_proto/`) |
| `clients/Makefile` | Dev targets: codegen, test, test-integration, install, uninstall, build, publish-test, publish, clean |
| `clients/examples/quickstart.py` | 9 runnable examples (gRPC, HTTP, auth, DataFrames, async, errors) |
| `clients/examples/mill_py_quickstart.ipynb` | Jupyter notebook walkthrough (12 cells) |
| `docs/public/src/connect/python/` | User-facing MkDocs docs (7 pages in nav + installation.md draft) |
| `docs/design/client/py-sql-dialect-plan.md` | Phase 9 (SQL Dialect) design, gap analysis, descriptor shape, work item tracking |

---

## 1. Repository Structure

The mono-repo lives at the workspace root. Relevant modules:

```
clients/mill-py/          # THE MODULE BEING REFACTORED
data/mill-data-grpc-service/   # Java gRPC server (Spring Boot)
data/mill-data-http-service/   # Java HTTP server (Spring Boot)
data/mill-data-backends/       # Shared backend (Calcite/JDBC execution)
core/mill-service-core/        # Shared service core (dispatchers, providers, security)
proto/                         # Protobuf definitions (source of truth)
docs/design/source/mill-type-system.md  # Type system reference
```

Build: Gradle multi-repo. Each module has its own `gradlew`. Python module uses Poetry.

---

## 2. Protobuf Contract

Proto sources live in `proto/`. Four Mill-specific files plus substrait protos.

### 2.1 `proto/common.proto`

Defines the schema and type model.

```protobuf
package io.qpointz.mill;

enum ProtocolVersion { UNKNOWN = 0; V1_0 = 1; }

message Schema { repeated Table tables = 1; }

message Table {
  enum TableTypeId { NOT_SPECIFIED_TABLE_TYPE = 0; TABLE = 1; VIEW = 2; }
  string schemaName = 1;
  string name = 2;
  TableTypeId tableType = 3;
  repeated Field fields = 4;
}

message LogicalDataType {
  enum LogicalDataTypeId {
    NOT_SPECIFIED_TYPE = 0;
    TINY_INT = 1; SMALL_INT = 2; INT = 3; BIG_INT = 4;
    BINARY = 5; BOOL = 6; DATE = 7; FLOAT = 8; DOUBLE = 9;
    INTERVAL_DAY = 10; INTERVAL_YEAR = 11; STRING = 12;
    TIMESTAMP = 13; TIMESTAMP_TZ = 14; TIME = 15; UUID = 16;
  }
  LogicalDataTypeId typeId = 2;
  int32 precision = 3;
  int32 scale = 4;
}

message DataType {
  enum Nullability { NOT_SPECIFIED_NULL = 0; NULL = 1; NOT_NULL = 2; }
  LogicalDataType type = 1;
  Nullability nullability = 2;
}

message Field {
  string name = 1;
  uint32 fieldIdx = 2;
  DataType type = 3;
}
```

### 2.2 `proto/vector.proto`

Columnar data transport.

```protobuf
message VectorBlockSchema { repeated Field fields = 3; }

message VectorBlock {
  VectorBlockSchema schema = 1;
  uint32 vectorSize = 2;
  repeated Vector vectors = 3;
}

message Vector {
  message NullsVector  { repeated bool   nulls  = 2; }
  message StringVector { repeated string values = 1; }
  message I32Vector    { repeated int32  values = 1; }
  message I64Vector    { repeated int64  values = 1; }
  message FP64Vector   { repeated double values = 1; }
  message FP32Vector   { repeated float  values = 1; }
  message BoolVector   { repeated bool   values = 1; }
  message BytesVector  { repeated bytes  values = 1; }

  uint32 fieldIdx   = 1;
  NullsVector nulls = 2;
  oneof values {
    StringVector stringVector = 100;
    I32Vector    i32Vector    = 101;
    I64Vector    i64Vector    = 102;
    FP64Vector   fp64Vector   = 103;
    FP32Vector   fp32Vector   = 104;
    BoolVector   boolVector   = 105;
    BytesVector  byteVector   = 106;
  }
}
```

**7 physical vector types** encoding **16 logical types**.

### 2.3 `proto/statement.proto`

```protobuf
message Parameter {
  uint32 index = 1;
  optional string name = 2;
  DataType type = 3;
  oneof value {
    bool booleanValue = 10; string stringValue = 11;
    int32 int32Value = 12; int64 int64Value = 13;
    float floatValue = 14; double doubleValue = 15;
  }
}

message SQLStatement {
  string sql = 1;
  repeated Parameter parameters = 2;
}

message PlanStatement { substrait.Plan plan = 1; }

message TextPlanStatement {
  string plan = 1;
  enum TextPlanFormat { JSON = 0; YAML = 1; }
  TextPlanFormat format = 2;
}
```

### 2.4 `proto/data_connect_svc.proto`

The service RPC definitions:

```protobuf
service DataConnectService {
  rpc Handshake (HandshakeRequest) returns (HandshakeResponse);
  rpc ListSchemas(ListSchemasRequest) returns (ListSchemasResponse);
  rpc GetSchema(GetSchemaRequest) returns (GetSchemaResponse);
  rpc ParseSql(ParseSqlRequest) returns (ParseSqlResponse);
  rpc ExecQuery(QueryRequest) returns (stream QueryResultResponse);  // SERVER STREAMING
  rpc SubmitQuery(QueryRequest) returns (QueryResultResponse);       // UNARY (paging)
  rpc FetchQueryResult(QueryResultRequest) returns (QueryResultResponse); // UNARY (paging)
}
```

Key messages:

```protobuf
message HandshakeRequest {}
message HandshakeResponse {
  ProtocolVersion version = 2;
  message Capabilities { bool supportSql = 1; bool supportResultPaging = 2; }
  Capabilities capabilities = 3;
  message AuthenticationContext { string name = 1; }
  AuthenticationContext authentication = 4;
  map<int32, MetaInfoValue> metas = 1;
}

message QueryRequest {
  QueryExecutionConfig config = 1;
  oneof query {
    substrait.Plan plan = 2;
    SQLStatement statement = 3;
  }
}

message QueryExecutionConfig {
  int32 fetchSize = 1;
  optional message Attributes { repeated string names = 1; repeated int32 indexes = 2; } attributes = 2;
}

message QueryResultRequest { string pagingId = 1; int32 fetchSize = 2; }
message QueryResultResponse { optional string pagingId = 1; optional VectorBlock vector = 2; }
```

**Two query execution paths**:
- `ExecQuery`: server-streaming — gRPC client receives stream of `QueryResultResponse`
- `SubmitQuery` + `FetchQueryResult`: paging — HTTP client calls submit, gets first page + `pagingId`, then fetches subsequent pages until `pagingId` is empty or `vector` is absent

### 2.5 Substrait protos

Located at `proto/substrait/`. Used for `ParseSql` response and `QueryRequest.plan`. Files:
`plan.proto`, `algebra.proto`, `type.proto`, `type_expressions.proto`, `function.proto`,
`extensions/extensions.proto`, `parameterized_types.proto`, `capabilities.proto`,
`extended_expression.proto`.

---

## 3. Current Python Client — File-by-File Analysis

Location: `clients/mill-py/`

### 3.1 `pyproject.toml`

Poetry-managed. Package name `mill-py`, version `0.1.0`, packages `[{include = "millclient"}]`.

**Current dependencies** (all to be replaced):
- `betterproto ^2.0.0b6` — beta protobuf library generating Python dataclasses
- `grpclib *` — async-only gRPC client (used by betterproto)
- `grpcio ^1.64.1` — present but only used indirectly
- `aiohttp *` — async HTTP client
- `aiostream *` — async stream utilities
- `pyarrow *` — Arrow columnar format
- `pandas *` — DataFrames
- `whenever *` — third-party temporal types
- `logo *` — appears unused
- `urllib3 *` — appears unused directly

Dev deps: `betterproto[compiler]`, `grpcio-tools`, `tox`.
Test deps: `certifi`, `coverage`, `pytest`, `pytest-mock`, `devpi-process`.

### 3.2 `millclient/__init__.py`

```python
import base64
from abc import abstractmethod
from .client import *
from .utils import *
from .exceptions import *
from .auth import *
```

Star-imports everything. The `base64` and `abstractmethod` imports are unused at this level.

### 3.3 `millclient/client.py` (320 lines)

The main module. Contains ALL client classes in one file.

**`AuthType(enum.Enum)`** (lines 24-27): Unused enum `NONE=0, BASIC=1, BEARER=2`.

**`MillQuery`** (lines 30-63): Abstract base for query results.
- `__event_loop__()` — abstract, returns `AbstractEventLoop`
- `responses()` — abstract async generator of `QueryResultResponse`
- `responses_async()` — collects via `aiostream.stream.list`
- `responses_fetch()` — sync wrapper via `run_until_complete`
- `record_batches()` — async generator converting responses to `pa.RecordBatch`
- `to_pandas()` / `to_pandas_async()` — converts to DataFrame

**`MillSqlQuery`** (line 66-67): **DUPLICATE** — empty stub class, real impl at lines 266-276.

**`MillClient`** (lines 70-157): Abstract base client.
- Constructor takes optional `event_loop`, defaults to `asyncio.get_event_loop()` (DEPRECATED)
- `__fetch_size = 10000` hardcoded default
- Context manager support (`__enter__`/`__exit__`)
- `handshake()`, `list_schemas()`, `get_schema()` — sync wrappers calling `run_until_complete`
- `exec_query()` — async generator
- `exec_query_fetch()` — sync wrapper
- `sql_query()` — returns `MillSqlQuery`
- `grpc_error_message()` — formats gRPC errors

**`MillGrpcClient(MillClient)`** (lines 159-178): gRPC implementation.
- Takes `DataConnectServiceStub` (betterproto-generated)
- `exec_query_async_iter()` delegates to stub's `exec_query()` (server streaming)
- All other methods delegate to stub

**`MillHttpSession`** (lines 180-197): HTTP session wrapper.
- Wraps `aiohttp.ClientSession`
- `post(command, req, res)` — posts JSON, parses response via betterproto `res.parse(cnt)`
- Error handling returns `MillServerError`

**`MillHttpClient(MillClient)`** (lines 200-263): HTTP implementation.
- Constructor builds URL from parts, sets Content-Type/Accept headers
- Sends `application/json`, accepts `application/x-protobuf`
- `exec_query_async_iter()` — contains inner class `PagingIterator`:
  - First call: `SubmitQuery`, saves `paging_id`
  - Subsequent calls: `FetchQueryResult` with `paging_id`
  - Stops when no `vector` in response

**`MillSqlQuery`** (lines 266-276): Real implementation (overrides the earlier stub).
- Wraps a `MillClient` + `QueryRequest`
- `responses()` delegates to `client.exec_query()`
- `__event_loop__()` returns client's event loop

**`create_client()`** (lines 280-319): Factory function.
- Parameters: `channel`, `creds`, `url`, `protocol`, `host`, `port`, `base_path`, `ssl`, `event_loop`, `metadata`
- Parses URL via `urllib.parse.urlparse`
- Dispatches to `MillGrpcClient` or `MillHttpClient` based on protocol
- Default `base_path="/services/jet/"`

### 3.4 `millclient/auth.py` (60 lines)

**`MillCallCredentials`** (ABC):
- `get_metadata()` — for gRPC (lowercase `authorization` key, as gRPC metadata convention)
- `get_headers()` — for HTTP (capitalized `Authorization` key, as HTTP convention)
- `creds_type()` — returns string identifier

**`BasicAuthCredentials`**:
- Base64-encodes `username:password` (`base64.b64encode(f"{user}:{pass}".encode()).decode()`)
- Returns `[("authorization", "Basic {encoded}")]` for gRPC
- Returns `[("Authorization", "Basic {encoded}")]` for HTTP

**`BearerTokenCredentials`**:
- Returns `[("authorization", "Bearer {token}")]` for gRPC
- Returns `[("Authorization", "Bearer {token}")]` for HTTP

**Anonymous** (implicit):
- When `creds=None` in `create_client()`, no auth headers/metadata are added.
- The server treats unauthenticated requests as anonymous when security is disabled
  (`@ConditionalOnSecurity` not active), or rejects with `UNAUTHENTICATED` / HTTP 401 when
  security is enabled.

**Key implementation details for the new client**:
- gRPC metadata keys are always lowercase (gRPC convention). The current code uses
  `"authorization"` (lowercase) — this is correct.
- HTTP headers are case-insensitive per RFC 7230, but the current code uses `"Authorization"`
  (capitalized) — this is conventional.
- In the current `create_client()`, credentials are merged into a shared `metadata` dict
  before being passed to the transport constructor. gRPC uses `creds.get_metadata()`,
  HTTP uses `creds.get_headers()`.
- The new client should unify this: auth credentials expose `to_headers() -> dict[str, str]`,
  and the transport layer handles key casing internally.

### 3.5 `millclient/utils.py` (163 lines)

Type conversion utilities. Two main sections:

**`__get_reader(type_id)`** (lines 14-88): Returns a function `(Vector, idx) -> native_value`.
- 14 types supported. `INTERVAL_DAY` and `INTERVAL_YEAR` fall through to `MillError`.
- Uses `whenever` library for temporal types:
  - `DATE` → `whenever.Date` (epoch days via `i64_vector`)
  - `TIME` → `whenever.Time` (nanos via `i64_vector`)
  - `TIMESTAMP` → `whenever.LocalDateTime` (millis via `i64_vector`)
  - `TIMESTAMP_TZ` → `whenever.ZonedDateTime` (millis via `i64_vector`, UTC)

**`__get_pyarrow_type(type_id)`** (lines 90-121): Returns `(pa_type, optional_converter)`.
- **BUG**: `TIMESTAMP` and `TIMESTAMP_TZ` both map to `pa.date64()` — should be `pa.timestamp('ms')`.
- `UUID` maps to `pa.string()` with `str(x)` converter.
- `TINY_INT`/`SMALL_INT` both map to `pa.int32()` (could be tighter).
- `INTERVAL_DAY`/`INTERVAL_YEAR` unsupported.

**`vector_to_array(type_id, vector, mapper)`** (lines 123-142): Reads a single Vector column into
a Python list, respecting nulls.

**`vector_block_to_record_batch(vector: VectorBlock)`** (lines 144-153): Converts a VectorBlock
to a PyArrow `RecordBatch`.

**`vector_block_to_pandas(vector: VectorBlock)`** (lines 155-159): Converts to pandas DataFrame
via RecordBatch.

**`response_to_record_batch(response)`** (lines 161-162): Unwraps `QueryResultResponse.vector`.

### 3.6 `millclient/exceptions.py` (9 lines)

```python
class MillError(Exception):
    pass

class MillServerError(MillError):
    def __init__(self, message, origin: Exception):
        self.origin = origin
    pass
```

Note: `MillServerError.__init__` does not call `super().__init__(message)`, so `str(error)` will
be empty.

### 3.7 `millclient/proto/` — Generated betterproto stubs

Generated by `codegen.py` using `--python_betterproto_out`. Single file at
`millclient/proto/io/qpointz/mill/__init__.py` (~559 lines).

Contains:
- Enums: `ProtocolVersion`, `TableTableTypeId`, `LogicalDataTypeLogicalDataTypeId`, `DataTypeNullability`, etc.
- Message dataclasses: `Schema`, `Table`, `Field`, `LogicalDataType`, `DataType`, `VectorBlock`, `Vector`, etc.
- `DataConnectServiceStub(betterproto.ServiceStub)` — async client stub with all 7 RPCs
- `DataConnectServiceBase(ServiceBase)` — server base class

Also: `millclient/proto/substrait/__init__.py` and `millclient/proto/substrait/extensions/__init__.py`.

### 3.8 `codegen.py` (42 lines)

Uses `grpc_tools.protoc` with `--python_betterproto_out` flag. Resolves proto sources from
`../../proto/` (the repo-level proto directory). Cleans and regenerates `millclient/proto/`.

Key args: `-I{in_dir}`, `-I/usr/include/`, `--python_betterproto_out={out_dir}`, plus
`site-packages/grpc_tools/_proto` for well-known types.

### 3.9 `.test-profiles` (removed in Phase 8)

The old `.test-profiles` file was removed in Phase 8. Integration tests now use
`MILL_IT_*` environment variables exclusively (see `py-implementation-plan.md` Section 10).

### 3.10 `.gitignore`

Standard Python gitignore. Notably does NOT ignore `millclient/proto/` (stubs are committed).

---

## 4. Test Files — Current State

### 4.1 `tests/unit/test_type_support.py`

Tests vector reading against binary fixtures. Loads `.bin` + `.ref` files from
`tests/unit/../../../../test/messages/logical-types`. Uses `RefData` dataclass to parse reference
values. Tests all supported types via `utils.vector_to_array()`.

**This test depends on betterproto message parsing** (`vb.parse(data)`) and `whenever` types
for temporal assertions. Both will need rewriting.

Binary test fixtures (`.bin` files) are in betterproto binary format — need to check if they're
standard protobuf wire format (they should be, since betterproto uses standard protobuf encoding).

### 4.2 `tests/unit/test_create_client.py`

**ENTIRELY COMMENTED OUT**. Was testing `create_client()` URL parsing and client type dispatch.

### 4.3 `tests/test_connect.py`

**ENTIRELY COMMENTED OUT**. Was testing TLS connections with certificate verification.

### 4.4 `tests/integration/test_client.py`

Active. Parameterized with `@pytest.mark.parametrize("profile", profiles())`.

Tests:
- `test_handshake` — verifies `ProtocolVersion.V1_0`
- `test_list_schemas` — verifies non-empty schema list
- `test_get_schema` — gets "MONETA" schema, verifies tables
- `test_schema_doesnt_exist` — expects `MillServerError`
- `test_exec_query` — SQL query with fetch
- `test_sql_querty_trivial` — MillSqlQuery flow
- `test_query_record_batches` — RecordBatch conversion
- `test_query_to_pandas` — DataFrame conversion
- `test_empty_query_returns_schema` — empty result has columns

### 4.5 `tests/integration/profiles.py`

Reads `.test-profiles` or `TEST_PROFILES` env var. Parses `host,port,protocol,tls,auth` lines
into `TestITProfile` dataclasses. Used by parametrized integration tests.

---

## 5. Server-Side — gRPC Service

File: `data/mill-data-grpc-service/src/main/java/io/qpointz/mill/services/MillGrpcService.java`

- Extends `DataConnectServiceGrpc.DataConnectServiceImplBase` (standard protobuf-java codegen)
- Annotated `@GrpcService`, `@SpringBootApplication`, `@ConditionalOnService("grpc")`
- Delegates all operations to `DataOperationDispatcher` (via `ServiceHandler`)
- `handshake()`, `listSchemas()`, `getSchema()`, `parseSql()` — unary: `replyOne()` pattern
- `execQuery()` — server streaming via `VectorBlockIterator`:
  ```java
  while (iterator.hasNext()) {
      val vectorBlock = iterator.next();
      val resp = QueryResultResponse.newBuilder().setVector(vectorBlock).build();
      callObserver.onNext(resp);
  }
  callObserver.onCompleted();
  ```

Security: `GrpcServiceSecurityConfiguration` wires Basic + OAuth2 bearer auth via
`grpc-spring-boot-starter` security framework.

Exception handling: `MillGrpcServiceExceptionAdvice` catches `StatusRuntimeException`,
`StatusException`, and `Error`, maps to gRPC status codes.

Build: `data/mill-data-grpc-service/build.gradle.kts` — depends on `mill-data-backends`,
`grpc-netty-shaded`, Spring Security, `grpc-spring-boot-starter`.

---

## 6. Server-Side — HTTP Service

File: `data/mill-data-http-service/src/main/java/io/qpointz/mill/services/access/http/controllers/AccessServiceController.java`

- `@RestController` at `/services/jet`
- `@ConditionalOnService("jet-http")`
- Supports both `application/json` and `application/protobuf` content types
- Delegates to same `DataOperationDispatcher`

Endpoints (all POST):
- `/services/jet/Handshake` → `dispatcher::handshake`
- `/services/jet/ListSchemas` → `dispatcher::listSchemas`
- `/services/jet/GetSchema` → `dispatcher::getSchema`
- `/services/jet/ParseSql` → `dispatcher::parseSql`
- `/services/jet/SubmitQuery` → `dispatcher::submitQuery` (returns first page + `pagingId`)
- `/services/jet/FetchQueryResult` → `dispatcher::fetchResult` (returns next page by `pagingId`)

Content negotiation handled by `MessageHelper`:
- JSON input: parsed via `JsonFormat.parser().merge()`
- Protobuf input: parsed via `Message.parseFrom()`
- JSON output: `JsonFormat.printer().print()`
- Protobuf output: `message.toByteArray()`

**Known bug**: All error messages in `MessageHelper` parse methods say "Failed to parse
HandshakeResponse" regardless of actual message type (copy-paste error).

`ProtobufUtils.java` — empty class (dead code).

Build: `data/mill-data-http-service/build.gradle.kts` — depends on `mill-data-backends`,
`protobuf-java-util`.

---

## 7. Shared Backend — DataOperationDispatcher

File: `core/mill-service-core/src/main/java/io/qpointz/mill/services/dispatchers/DataOperationDispatcher.java`

```java
public interface DataOperationDispatcher {
    HandshakeResponse handshake(HandshakeRequest request);
    ListSchemasResponse listSchemas(ListSchemasRequest request);
    GetSchemaResponse getSchema(GetSchemaRequest request);
    ParseSqlResponse parseSql(ParseSqlRequest request);
    QueryResultResponse submitQuery(QueryRequest request);
    QueryResultResponse fetchResult(QueryResultRequest request);
    VectorBlockIterator execute(QueryRequest request);
}
```

Both gRPC and HTTP services use this interface. The gRPC service uses `execute()` (streaming),
while HTTP uses `submitQuery()`/`fetchResult()` (paging).

`ServiceHandler` is the Spring-managed holder that provides access to dispatchers:
- `data()` → `DataOperationDispatcher`
- `security()` → `SecurityDispatcher`
- `substrait()` → `SubstraitDispatcher`

---

## 8. Mill Type System — Complete Reference

Source: `docs/design/source/mill-type-system.md`

### 8.1 Three-Layer Architecture

1. **LogicalType** (16 types) — semantic meaning of a value
2. **PhysicalType** (7 types) — wire encoding in Vector proto
3. **DatabaseType** — bundles logical type + nullability + precision + scale

### 8.2 Logical-to-Physical Mapping

| # | LogicalDataTypeId | PhysicalType | Vector oneof | Wire type | Encoding semantics |
|---|-------------------|--------------|--------------|-----------|--------------------|
| 1 | `TINY_INT` | I32 | `i32Vector` | `repeated int32` | 8-bit widened to 32-bit |
| 2 | `SMALL_INT` | I32 | `i32Vector` | `repeated int32` | 16-bit widened to 32-bit |
| 3 | `INT` | I32 | `i32Vector` | `repeated int32` | native 32-bit |
| 4 | `BIG_INT` | I64 | `i64Vector` | `repeated int64` | native 64-bit |
| 5 | `BOOL` | Bool | `boolVector` | `repeated bool` | |
| 6 | `FLOAT` | FP32 | `fp32Vector` | `repeated float` | IEEE 754 |
| 7 | `DOUBLE` | FP64 | `fp64Vector` | `repeated double` | IEEE 754 |
| 8 | `STRING` | String | `stringVector` | `repeated string` | UTF-8 |
| 9 | `BINARY` | Bytes | `byteVector` | `repeated bytes` | arbitrary |
| 10 | `DATE` | I64 | `i64Vector` | `repeated int64` | **epoch days** |
| 11 | `TIME` | I64 | `i64Vector` | `repeated int64` | **nanos since midnight** |
| 12 | `TIMESTAMP` | I64 | `i64Vector` | `repeated int64` | **epoch millis (naive)** |
| 13 | `TIMESTAMP_TZ` | I64 | `i64Vector` | `repeated int64` | **epoch millis (UTC)** |
| 14 | `INTERVAL_DAY` | I32 | `i32Vector` | `repeated int32` | **day count** |
| 15 | `INTERVAL_YEAR` | I32 | `i32Vector` | `repeated int32` | **year count** |
| 16 | `UUID` | Bytes | `byteVector` | `repeated bytes` | **16-byte binary** |

### 8.3 Java Value Converters (server-side encoding reference)

These define how the server encodes values into vectors — the Python client must decode
using the inverse:

| Converter | Java type | Wire type | Encoding |
|-----------|-----------|-----------|----------|
| `LocalDateToEpochConverter` | `LocalDate` | `Long` | epoch days (`date.toEpochDay()`) |
| `LocalTimeToNanoConverter` | `LocalTime` | `Long` | `time.toNanoOfDay()` |
| `LocalDateTimeToEpochMilli` | `LocalDateTime` | `Long` | millis since 1970-01-01T00:00 |
| `ZonedDateTimeToEpochMillis` | `ZonedDateTime` | `Long` | millis since epoch (UTC) |
| `BinaryToUUIDConverter` | `java.util.UUID` | `byte[]` | `mostSigBits + leastSigBits` (16 bytes) |

**Python decoding must match these exactly**:
- `DATE`: `datetime.date.fromordinal(datetime.date(1970,1,1).toordinal() + epoch_days)`
  or `datetime.date(1970,1,1) + datetime.timedelta(days=epoch_days)`
- `TIME`: `(datetime.datetime.min + datetime.timedelta(microseconds=nanos // 1000)).time()`
  — note: `datetime.time` has microsecond precision, nanos will be truncated
- `TIMESTAMP`: `datetime.datetime(1970,1,1) + datetime.timedelta(milliseconds=millis)`
- `TIMESTAMP_TZ`: same but with `tzinfo=datetime.timezone.utc`
- `UUID`: `uuid.UUID(bytes=byte_value)` — standard 16-byte MSB+LSB layout

---

## 9. Known Bugs and Issues in Current Code

### 9.1 Python Client Bugs

| # | Location | Issue | Severity |
|---|----------|-------|----------|
| 1 | `client.py:66-67` | `MillSqlQuery` defined twice — empty stub at line 66, real impl at line 266 | Medium |
| 2 | `client.py:73` | `asyncio.get_event_loop()` deprecated since Python 3.10 | High |
| 3 | `utils.py:117-119` | `TIMESTAMP`/`TIMESTAMP_TZ` map to `pa.date64()` instead of `pa.timestamp('ms')` | High |
| 4 | `utils.py:90-121` | `INTERVAL_DAY`/`INTERVAL_YEAR` unsupported — raises `MillError` | Medium |
| 5 | `exceptions.py:6` | `MillServerError.__init__` doesn't call `super().__init__(message)` — `str(error)` is empty | Medium |
| 6 | `client.py:215` | `aiohttp.ClientSession(loop=event_loop)` — `loop` parameter deprecated | Medium |
| 7 | `test_create_client.py` | Entire file commented out | Low |
| 8 | `test_connect.py` | Entire file commented out | Low |
| 9 | `__init__.py:1-2` | Unused `import base64` and `from abc import abstractmethod` | Low |
| 10 | `client.py` HTTP path (`MillHttpSession.post` / `MillHttpClient`) | HTTP transport is effectively hardwired to protobuf response parsing (`res.parse(cnt)`) while protocol naming and mode selection imply JSON should be valid too; this is a content negotiation/decoder mismatch. Integration tests should be reviewed: current profile segregation (`http-json`, `http-protobuf`) implies distinct response serialization modes, but implementation appears incomplete/inconsistent. Track as backlog `C-21`. | High |

### 9.2 HTTP Server Bugs (for reference)

| # | Location | Issue |
|---|----------|-------|
| 1 | `MessageHelper.java` lines 90-136 | All parse error messages say "Failed to parse HandshakeResponse" regardless of message type |
| 2 | `ProtobufUtils.java` | Empty class — dead code |

### 9.3 Type System Gaps

| Gap | Impact on Python client |
|-----|------------------------|
| `INTERVAL_DAY`/`INTERVAL_YEAR` never sent from JDBC sources | Tests may not encounter these types from JDBC-backed services |
| Server's JDBC `TIMESTAMP` maps to `TimestampTZ` (not `Timestamp`) | Python client may receive `TIMESTAMP_TZ` for plain SQL `TIMESTAMP` columns |
| `TIME` precision is nanos but `datetime.time` max precision is microseconds | Sub-microsecond time values will be truncated |

---

## 10. Server-Side Authentication

The server supports three authentication modes, configured via Spring Security:

### 10.1 `AuthenticationType` enum (server-side)

```java
public enum AuthenticationType {
    CUSTOM(0),    // custom auth — not wired by default
    OAUTH2(100),  // bearer token (JWT / OAuth2 resource server)
    BASIC(300);   // HTTP Basic auth (username:password base64)
}
```

### 10.2 gRPC Authentication

`GrpcServiceSecurityConfiguration` (conditional on security enabled) registers auth readers:
- `BASIC` → `BasicGrpcAuthenticationReader` — reads `authorization: Basic ...` from gRPC metadata
- `OAUTH2` → `BearerAuthenticationReader` → `BearerTokenAuthenticationToken` — reads
  `authorization: Bearer ...` from gRPC metadata
- `CUSTOM` → ignored (returns `Optional.empty()`)

All registered readers are composed via `CompositeGrpcAuthenticationReader`.

When security is enabled, all `DataConnectService` RPCs require `AccessPredicate.authenticated()`.
Without valid credentials, the server returns gRPC status `UNAUTHENTICATED`.

### 10.3 HTTP Authentication

HTTP service uses standard Spring Security filter chain (via `ServicesSecurityConfiguration`
and `AppSecurityConfiguration` in `mill-security-core`). Basic auth is enabled via
`http.httpBasic(Customizer.withDefaults())`. OAuth2/Bearer is handled by the resource server
configuration. Unauthenticated requests receive HTTP 401.

### 10.4 Anonymous Access

When `@ConditionalOnSecurity` is not active (security disabled), both gRPC and HTTP services
accept unauthenticated requests. The handshake response's `authentication.name` field will
reflect the anonymous principal (typically `"ANONYMOUS"`).

### 10.5 What the Python Client Must Support

| Auth mode | `connect()` usage | Wire format (gRPC) | Wire format (HTTP) |
|-----------|-------------------|--------------------|--------------------|
| Anonymous | `connect(url)` (no `auth` param) | no `authorization` metadata | no `Authorization` header |
| Basic | `connect(url, auth=BasicAuth("user", "pass"))` | `authorization: Basic base64(user:pass)` | `Authorization: Basic base64(user:pass)` |
| Bearer | `connect(url, auth=BearerToken("tok"))` | `authorization: Bearer tok` | `Authorization: Bearer tok` |

The handshake response can be used to verify auth state:
```python
resp = client.handshake()
print(resp.authentication.name)  # "ANONYMOUS" or authenticated principal name
```

---

## 11. Service Discovery — `.well-known/mill` Endpoint

The server exposes a discovery endpoint at `GET /.well-known/mill` that returns a JSON
descriptor of available services, security configuration, and schemas. This endpoint is
**always unauthenticated** (permitted by `WellKnownSecurityConfiguration` at `@Order(0)`).

### 11.1 Server-Side Implementation

**Controller**: `services/mill-well-known-service/src/main/java/io/qpointz/mill/services/controllers/ApplicationDescriptorController.java`

```java
@RestController
@RequestMapping("/.well-known")
@ConditionalOnService("meta")
public class ApplicationDescriptorController {
    @GetMapping("mill")
    public ApplicationDescriptor getInfo() { return this.applicationDescriptor; }
}
```

**Model**: `core/mill-service-core/src/main/java/io/qpointz/mill/services/descriptors/ApplicationDescriptor.java`

The `ApplicationDescriptor` aggregates:
- `Collection<ServiceDescriptor> services` — each service contributes a descriptor
- `SecurityDescriptor security` — auth config
- `Map<String, SchemaDescriptor> schemas` — available schemas with links

### 11.2 Service Descriptors

Each protocol module registers a `ServiceDescriptor` bean with a `stereotype` string:

| Module | Stereotype | Condition |
|--------|-----------|-----------|
| `mill-data-grpc-service` | `"grpc"` | `@ConditionalOnService("grpc")` — `GrpcServiceDescriptor` |
| `mill-data-http-service` | `"jet-http"` | No `ServiceDescriptor` registered currently (gap) |

The `ServiceDescriptor` interface:
```java
public interface ServiceDescriptor { String getStereotype(); }
```

**Note**: The HTTP service does NOT currently register a `ServiceDescriptor`. Only the gRPC
service has `GrpcServiceDescriptor`. This is a known gap — the HTTP service's stereotype
would be `"jet-http"` to match its `@ConditionalOnService("jet-http")` annotation.

### 11.3 Security Descriptor

```java
public record SecurityDescriptor(boolean enabled, Collection<AuthenticationMethodDescriptor> authMethods) {}
```

Each `AuthenticationMethodDescriptor` serializes as `{"authType": "BASIC"}` or
`{"authType": "OAUTH2"}` (via Jackson `@JsonProperty("authType")`).

When security is disabled, `enabled=false` and `authMethods` is empty.

### 11.4 Schema Descriptors

```java
public record SchemaDescriptor(String name, URI link) {}
```

Currently hardcodes `http://localhost:8080/.well-known/mill/schemas/{name}` as the link
(this is a known issue — the link should be relative or configurable).

### 11.5 Example Response

```json
{
  "services": [
    { "stereotype": "grpc" }
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

### 11.6 Security Configuration for Well-Known

`WellKnownSecurityConfiguration` (in `mill-security-core`) registers a `SecurityFilterChain`
at `@Order(0)` that permits all requests to `/.well-known/**` without authentication:

```java
http.securityMatcher("/.well-known/**")
    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
    .csrf(AbstractHttpConfigurer::disable)
    .cors(AbstractHttpConfigurer::disable)
```

### 11.7 Python Client Plan

The new client will include a `discovery.py` module with:
- `MillServiceDescriptor` — dataclass modeling the JSON response
- `fetch_descriptor(host) -> MillServiceDescriptor` — fetches and parses `GET /.well-known/mill`
- `connect()` with bare hostname (no scheme) will fetch the descriptor, then raise
  `NotImplementedError("Service discovery not implemented yet")` as a stub for future work.

---

## 12. HTTP Protocol Details

The HTTP service at `/services/jet` uses a POST-based RPC-over-HTTP pattern.

### 12.1 Content Negotiation

| Header | Supported values |
|--------|-----------------|
| `Content-Type` | `application/json`, `application/protobuf` |
| `Accept` | `application/json`, `application/protobuf` |

The server supports all four combinations (JSON/protobuf request x JSON/protobuf response).

The **current** Python HTTP client sends `Content-Type: application/json` (serializes via
`req.to_json()`) and accepts `Accept: application/x-protobuf` (parses via betterproto
`res.parse(cnt)` — binary protobuf). So it is a hybrid: JSON out, protobuf back.

The **new** client must support two clean modes:
- **JSON mode** (`encoding="json"`): sends `application/json`, accepts `application/json`.
  Uses `google.protobuf.json_format` for serialization.
- **Protobuf mode** (`encoding="protobuf"`): sends `application/protobuf`, accepts
  `application/protobuf`. Uses `message.SerializeToString()` / `Message.FromString()`.

Note: the server uses Spring's `MediaType.APPLICATION_PROTOBUF` which also matches
`application/x-protobuf`. The new client should use the standard `application/protobuf`.

### 12.2 Paging Protocol

For query execution over HTTP (no streaming):

1. Client sends `POST /services/jet/SubmitQuery` with `QueryRequest` body
2. Server returns `QueryResultResponse` with `vector` (first batch) and `pagingId`
3. Client sends `POST /services/jet/FetchQueryResult` with `QueryResultRequest { pagingId }`
4. Server returns next `QueryResultResponse` with `vector` and updated `pagingId`
5. Repeat step 3-4 until `vector` is absent or `pagingId` is empty
6. When no more data: response has no `vector` field → stop

### 12.3 Endpoint Summary

| Endpoint | Request type | Response type |
|----------|-------------|---------------|
| `POST /services/jet/Handshake` | `HandshakeRequest` | `HandshakeResponse` |
| `POST /services/jet/ListSchemas` | `ListSchemasRequest` | `ListSchemasResponse` |
| `POST /services/jet/GetSchema` | `GetSchemaRequest` | `GetSchemaResponse` |
| `POST /services/jet/ParseSql` | `ParseSqlRequest` | `ParseSqlResponse` |
| `POST /services/jet/SubmitQuery` | `QueryRequest` | `QueryResultResponse` |
| `POST /services/jet/FetchQueryResult` | `QueryResultRequest` | `QueryResultResponse` |

---

## 13. gRPC Protocol Details

Standard gRPC over HTTP/2.

| RPC | Cardinality | Request | Response |
|-----|------------|---------|----------|
| `Handshake` | unary | `HandshakeRequest` | `HandshakeResponse` |
| `ListSchemas` | unary | `ListSchemasRequest` | `ListSchemasResponse` |
| `GetSchema` | unary | `GetSchemaRequest` | `GetSchemaResponse` |
| `ParseSql` | unary | `ParseSqlRequest` | `ParseSqlResponse` |
| `ExecQuery` | server-streaming | `QueryRequest` | `stream QueryResultResponse` |
| `SubmitQuery` | unary | `QueryRequest` | `QueryResultResponse` |
| `FetchQueryResult` | unary | `QueryResultRequest` | `QueryResultResponse` |

The gRPC service supports both streaming (`ExecQuery`) and paging (`SubmitQuery`/`FetchQueryResult`).
The current Python gRPC client uses streaming; the new client should also prefer streaming for
gRPC since it's more efficient.

Service path: `io.qpointz.mill.DataConnectService`

Auth: via gRPC metadata headers — `authorization: Basic ...` or `authorization: Bearer ...`
(lowercase key, as is standard for gRPC metadata).

---

## 14. Integration Test Infrastructure

### 14.1 Profile System

`.test-profiles` format: `host,port,protocol,tls,auth` (CSV, `#` comments).

Environment variable `TEST_PROFILES` overrides the file path.

Current profiles:
```
localhost,8080,http,N,N
localhost,9090,grpc,N,N
```

### 14.2 Current Test Schema

Integration tests use schema `MONETA` with table `CLIENTS` having at least columns:
`CLIENT_ID`, `FIRST_NAME`, `LAST_NAME`.

**This schema will change** — user will provide the new test data model.

---

## 15. Key Design Decisions

These decisions were made during planning and must be respected by all phases.

### 15.1 ResultSet — Lazy-with-Cache Strategy

`ResultSet` does **not** pull all VectorBlocks into memory eagerly. It uses a
**lazy-with-cache** strategy:

1. Constructed with a transport iterator (`Iterator[QueryResultResponse]`).
2. On first iteration, VectorBlocks are pulled from the source **one at a time, on demand**.
3. Each consumed block is **cached** in an internal `_cache: list[VectorBlock]`.
4. `_exhausted: bool` tracks whether the source iterator is fully consumed.
5. On subsequent iterations, cached blocks are replayed first, then remaining blocks are
   pulled from the source (if the first pass was interrupted, e.g. via `break`).
6. `fetchall()`, `to_pandas()`, `to_arrow()` all force full consumption of the source.

This gives:
- **Memory efficiency**: for large streaming results, process row-by-row and stop early.
- **Re-iterability**: after first pass, data is cached and can be re-iterated.
- **Notebook-friendly**: common pattern of `for row in result: ... break` then `result.to_pandas()`.

Transport behavior:
- **gRPC**: server pushes VectorBlocks via streaming; client's `grpc` iterator yields them lazily.
- **HTTP**: client controls pace; `FetchQueryResult` only called when next block is needed.

The async variant (`AsyncResultSet`) follows the same pattern with `AsyncIterator` source
and `async for` iteration.

### 15.2 Three Transport Modes — gRPC, HTTP-JSON, HTTP-Protobuf

The current client supports gRPC and HTTP. The current HTTP client sends JSON and receives
protobuf (hardcoded). The server (`AccessServiceController`) supports all combinations of
JSON and protobuf for both Content-Type and Accept.

The new client must support three distinct transport modes:

| Mode | URL scheme | Encoding | Wire format |
|------|-----------|----------|-------------|
| gRPC | `grpc://`, `grpcs://` | always binary protobuf | HTTP/2 streaming |
| HTTP-JSON | `http://`, `https://` | `encoding="json"` (default) | JSON request + JSON response |
| HTTP-Protobuf | `http://`, `https://` | `encoding="protobuf"` | binary protobuf request + response |

```python
client = connect("grpc://localhost:9090")                                    # gRPC
client = connect("http://localhost:8080/services/jet")                       # HTTP-JSON (default)
client = connect("http://localhost:8080/services/jet", encoding="protobuf")  # HTTP-Protobuf
```

HTTP encoding details:
- `"json"`: `Content-Type: application/json`, `Accept: application/json`. Serialize via
  `google.protobuf.json_format.MessageToJson()`, parse via `json_format.Parse()`.
- `"protobuf"`: `Content-Type: application/protobuf`, `Accept: application/protobuf`. Serialize
  via `message.SerializeToString()`, parse via `Message.FromString()`.
- The `encoding` parameter is ignored for gRPC transport.

The server's `MessageHelper.java` handles both formats:
- JSON: `JsonFormat.parser().merge()` / `JsonFormat.printer().print()`
- Protobuf: `Message.parseFrom()` / `message.toByteArray()`

### 15.3 Sync-first, Async Available

The primary API is synchronous (`mill.MillClient`, `mill.connect()`). This serves the main
use case: notebooks, interactive analysis, pulling data to pandas. The async API lives in
`mill.aio` and mirrors the sync surface exactly. No manual event loop management anywhere.

### 15.4 Proto Messages Hidden Behind Pythonic Wrappers

Users never interact with raw protobuf messages. All public API types (`MillType`, `MillField`,
`MillSchema`, `MillTable`, `ResultSet`) are pure Python classes with `from_proto()` factory
methods for internal conversion.

### 15.5 DataFrame Support via Optional Extras

PyArrow is the **foundational extra** — all DataFrame conversions go through Arrow:

```
VectorBlock → pyarrow.RecordBatch → pyarrow.Table → pandas / polars
```

| Extra | pip install | Method | Output |
|-------|-----------|--------|--------|
| (core) | `pip install mill-py` | `result.fetchall()` | `list[dict]` |
| arrow | `pip install mill-py[arrow]` | `result.to_arrow()` | `pyarrow.Table` |
| pandas | `pip install mill-py[pandas]` | `result.to_pandas()` | `pandas.DataFrame` |
| polars | `pip install mill-py[polars]` | `result.to_polars()` | `polars.DataFrame` |

Pandas and polars are thin wrappers on top of Arrow (`table.to_pandas()` and
`polars.from_arrow(table)` respectively). Arrow logic is written once.

`ResultSet.to_*()` methods use lazy imports and raise `ImportError` with install instructions
when the extra is missing.

### 15.6 SQL Dialect Foundation (Phase 9 — incremental delivery)

> Full design, gap analysis, descriptor shape, and work item tracking:
> **[`py-sql-dialect-plan.md`](py-sql-dialect-plan.md)**

Phase 9 is split into **two slices** serving **four consumers** (SA, JDBC, ibis, AI NL-to-SQL):

| Slice | Scope | Unblocks |
|-------|-------|----------|
| **9A** (P0+P1) | `MillDialectDescriptor` model, `CALCITE_DEFAULT`, type mappings, helpers, unit tests | Phase 11 (SA), JDBC, AI prompts |
| **9B** (P2+P3) | **Dialect tester** (`mill/sql/_tester.py`) — generates ~80 SQL queries, validates via `parse_sql()`, produces `DialectReport` with `feature_flags()` and `to_markdown()` exports. Function catalog, confirmed flags, `to_ibis_dtype()` | Phase 10 (ibis), AI enrichment |

**Key 9B concept — automated dialect tester**: `DialectTester` takes a `MillClient` + schema
name, generates SQL for every dialect feature (joins, CTEs, window functions, stats, math,
set ops, etc.), runs each through `parse_sql()` (plan compilation = supported, error = not
supported). Division semantics uses `query()` to inspect actual result. Output is a
`DialectReport` that auto-updates `CALCITE_DEFAULT` flags and generates the markdown report.
Location: `mill/sql/_tester.py` + `tests/integration/test_dialect.py`.

**Execution order**: 9A → 11 (SA POC) → 9B → 10 (ibis POC).

**Reference**: The Java JDBC driver (`clients/mill-jdbc-driver/`) provides a working
`DatabaseMetaData` implementation with known bugs (see §15.9 below).

### 15.9 Existing Java JDBC Driver — Reference for Phase 9/11

The repo contains a **working Java JDBC driver** at `clients/mill-jdbc-driver/` and an
interactive SQL shell at `clients/mill-jdbc-shell/` (sqlline repack). These are important
references for Phase 9 (SQL Dialect Foundation) and Phase 11 (DBAPI + SQLAlchemy).

**Architecture overview**:

```
┌─────────────────────────────────────────────────────┐
│  clients/mill-jdbc-driver/                          │
│                                                     │
│  Driver.java ─────► MillConnection ──────► MillClient (abstract)
│  (java.sql.Driver)  (java.sql.Connection)    ├─► GrpcMillClient
│                       │                      └─► HttpMillClient
│                       ├─► MillCallableStatement
│                       │   (java.sql.CallableStatement)
│                       │
│                       ├─► MillDatabaseMetadata
│                       │   (java.sql.DatabaseMetaData)
│                       │   └─► SchemasMetadata, TablesMetadata,
│                       │       ColumnsMetadata, CatalogsMetadata,
│                       │       TableTypesMetadata
│                       │
│                       └─► MillRecordReaderResultSet
│                           (java.sql.ResultSet via RecordReaderResultSetBase)
└─────────────────────────────────────────────────────┘
```

**Key implementation decisions already established by the Java driver** (reuse these in
Python Phase 11):

| Aspect | Java Driver Implementation | Python Phase 11 Implication |
|--------|---------------------------|----------------------------|
| **Read-only** | `isReadOnly() = true`, `setAutoCommit()` throws, `commit()`/`rollback()` throw | Python DBAPI: `commit()` = no-op, DDL/DML raise errors |
| **Transaction isolation** | `TRANSACTION_NONE` only | `cursor.execute("BEGIN")` raises error |
| **Forward-only cursors** | `TYPE_FORWARD_ONLY` enforced, `CONCUR_READ_ONLY` enforced | Python DBAPI `Cursor` is forward-only |
| **No catalogs** | `getCatalog() = null`, `supportsCatalogsIn*() = false` | SA dialect: `supports_catalogs = False` |
| **Schema support** | `getSchemas()` returns schemas via `ListSchemas` RPC | SA: `get_schema_names()` = `client.list_schemas()` |
| **Identifier quoting** | `getIdentifierQuoteString() = backtick` (NOTE: inconsistent with Calcite which uses `"`) | Python should use double-quote `"` per Calcite standard |
| **Null ordering** | `nullsAreSortedHigh/Low/Start/End` all `false` | Need live validation in Phase 9.1 |
| **JDBC URL format** | `jdbc:mill:grpc://host:port`, `jdbc:mill:http://host:port/path` | Python SA entry point: `mill+grpc://`, `mill+http://` |
| **Auth** | `user`/`password` props → Basic auth, `bearerToken` → Bearer | Already matches Python `BasicAuth`/`BearerToken` |
| **TLS** | `tlsKeyCertChain`, `tlsKeyPrivateKey`, `tlsTrustRootCert` | Already matches Python `tls_cert`/`tls_key`/`tls_ca` |

**Type mapping** (`JdbcUtils.logicalTypeIdToJdbcTypeId()`):

| MillType | JDBC Type | Note |
|----------|-----------|------|
| `TINY_INT` | `TINYINT` | |
| `SMALL_INT` | `SMALLINT` | |
| `INT` | `INTEGER` | |
| `BIG_INT` | `BIGINT` | |
| `BINARY` | `BINARY` | |
| `BOOL` | `BLOB` | **BUG**: should be `BOOLEAN` (line 33 says `Types.BLOB`) |
| `DATE` | `DATE` | |
| `FLOAT` | `FLOAT` | |
| `DOUBLE` | `DOUBLE` | |
| `INTERVAL_DAY` | `BIGINT` | Mapped to integer |
| `INTERVAL_YEAR` | `BIGINT` | Mapped to integer |
| `STRING` | `NVARCHAR` | |
| `TIMESTAMP` | `TIMESTAMP` | |
| `TIMESTAMP_TZ` | `TIMESTAMP_WITH_TIMEZONE` | |
| `TIME` | `TIME` | |
| `UUID` | `BINARY` | Mapped to binary |

**Known bugs in Java driver** (avoid in Python):
1. `BOOL → Types.BLOB` instead of `Types.BOOLEAN` in `JdbcUtils.java:33`
2. `getIdentifierQuoteString()` returns backtick but Calcite uses double-quote
3. Many `DatabaseMetaData` methods return `false`/`0`/`null` as stubs — Calcite actually
   supports features like GROUP BY, ORDER BY expressions, LIKE, subqueries, outer joins, etc.
4. `supportsGroupBy()` returns `false` — incorrect for Calcite
5. `supportsColumnAliasing()` returns `false` — incorrect for Calcite
6. HTTP client response handling has a content negotiation/decoder mismatch risk: request path may advertise JSON mode, but response decode path is protobuf-oriented. Integration tests should be reviewed: current profile segregation (`http-json`, `http-protobuf`) indicates expected response serialization separation, but implementation appears incomplete/inconsistent. Track as backlog `C-20`.

**mill-jdbc-shell** (`clients/mill-jdbc-shell/`): Thin wrapper — `mainClass = sqlline.SqlLine`
with `mill-jdbc-driver` as a dependency. Provides an interactive CLI SQL shell. No custom
code, just dependency wiring and a Docker packaging layer.

### 15.7 ibis Backend — POC (Phase 10)

ibis is **not** a DataFrame output format — it's a **query frontend**. The ibis backend POC
explores using ibis as a query abstraction that generates SQL, sends it to Mill for
execution, and returns results as Arrow/DataFrame:

```
ibis expression → SQL (sqlglot/Calcite dialect) → MillClient.query(sql) → ResultSet → Arrow
```

Uses `mill/sql/types.py` for `MillType` → ibis dtype mapping and `mill/sql/dialect.py` for
SQL dialect rules.

```python
import mill.ibis as mill_ibis
con = mill_ibis.connect("grpc://localhost:9090")
t = con.table("CLIENTS", schema="MONETA")
expr = t.filter(t.CLIENT_ID > 100).select("FIRST_NAME", "LAST_NAME")
result = expr.execute()  # pandas.DataFrame
```

Key questions the POC must answer:
- Can ibis's SQL generation produce Calcite-compatible SQL?
- What ibis operations work out of the box vs need custom compilation?
- Should the ibis backend ship as `mill-py[ibis]` or as a separate package?
- What are the performance characteristics vs direct `client.query()`?

### 15.8 DBAPI + SQLAlchemy — POC (Phase 11)

Phase 11 makes `mill-py` a **PEP 249 DBAPI 2.0** compliant driver and a **SQLAlchemy 2.0**
compatible dialect. Uses `mill/sql/dialect.py` and `mill/sql/types.py` from Phase 9.

Two layers:

1. **`mill/dbapi.py`** — standalone DBAPI 2.0 shim: `Connection`, `Cursor`, PEP 249
   exception hierarchy. Usable without SQLAlchemy (e.g. `pandas.read_sql()`).
2. **`mill/sqlalchemy/`** — `MillDialect` subclassing `DefaultDialect`, schema introspection,
   type mapping, `mill+grpc://` and `mill+http://` URL schemes.

```
SQLAlchemy Core / ORM → MillDialect → DBAPI Cursor → MillClient.query() → ResultSet → rows
```

Key constraints:
- Mill is **read-only** — `commit()`/`rollback()`/DDL are no-ops or raise errors.
- Mill has **no parameterised queries** — `Cursor.execute(sql, params)` must inline parameters.
- Calcite SQL is mostly ANSI-compatible — double-quote identifiers should work by default.

Key questions the POC must answer:
- Does SQLAlchemy's ANSI SQL compiler produce Calcite-compatible SQL?
- What table reflection / introspection features work vs are empty stubs?
- Can `pandas.read_sql()` and `pandas.read_sql_query()` use the DBAPI directly?
- Should the SQLAlchemy dialect ship as `mill-py[sqlalchemy]` or a separate package?

---

## 16. Rules, Conventions & Mandatory Practices

This section is the **authoritative reference** for all rules governing the `mill-py`
refactoring. An agent must follow every rule here. When in doubt, these rules override
any assumptions.

### 16.1 Git Workflow

**Single branch**: All implementation happens on `refactor/py-client`. Do **not** create
per-phase branches (the branch names listed per phase in the plan are historical — ignore them).

| Rule | Detail |
|------|--------|
| **One commit per work item** | Each WI (e.g. 0.1, 2.3, 5.4) = exactly one commit on the branch. |
| **Squash intermediate commits** | During a WI you may make many commits. Before moving to the next WI, squash them into one. Only squash *unpushed* commits. |
| **Push after every WI** | After squashing, `git push origin refactor/py-client`. Progress is always backed up. |
| **Never force-push published history** | Once a squashed commit is pushed, treat it as immutable. |
| **Never commit to `feature` or `main`** | Only work on `refactor/py-client`. The user merges. |
| **No `Co-Authored-By` trailers** | Never add co-author or similar trailers to commit messages. |

**Commit message format** — bracketed prefix + WI number:

```
[refactor] 2.3 MillTable + MillSchema dataclasses with from_proto()
```

- Imperative mood, under 72 characters for the summary line.
- Add a body paragraph for non-trivial changes.
- Use `[refactor]` for implementation WIs, `[wip]` for intermediate (pre-squash) commits,
  `[fix]` for bug fixes discovered during implementation, `[docs]` for documentation-only.

**WI lifecycle**:

```bash
# 1. Implement (intermediate commits are fine)
git add -A && git commit -m "[wip] 2.3 initial MillTable"
git add -A && git commit -m "[wip] 2.3 add from_proto and tests"

# 2. Squash into one WI commit (only unpushed commits)
git rebase -i HEAD~2
# → squash, edit message to: [refactor] 2.3 MillTable + MillSchema dataclasses with from_proto()

# 3. Push
git push origin refactor/py-client
```

### 16.2 Python Code Style

| Convention | Rule |
|-----------|------|
| **Formatter** | Follow PEP 8. Four-space indentation. Max line length ~100 chars (soft). |
| **Naming** | `snake_case` for functions, methods, variables, modules. `PascalCase` for classes. `UPPER_SNAKE` for constants. |
| **Type hints** | All public functions and methods must have full type annotations (parameters + return). Use `from __future__ import annotations` at the top of every module. |
| **Docstrings** | Google-style docstrings on **all developer-facing** (public) classes, methods, and functions. Include `Args:`, `Returns:`, `Raises:` sections. Add brief usage examples for key classes. **Write docstrings as you implement each WI** — do not defer to Phase 8. Phase 8 is for polishing and filling gaps, not for initial documentation. |
| **Imports** | Absolute imports only (`from mill.types import MillType`). No star-imports (`from x import *`). Group: stdlib, third-party, local — separated by blank lines. |
| **Private modules** | Prefix with underscore: `_transport/`, `_proto/`. These are internal — users should not import from them directly. |
| **`__init__.py` exports** | Explicitly list public API in `mill/__init__.py` via imports. Use `__all__` if needed. Do not re-export internal symbols. |
| **No manual proto edits** | Never hand-edit files under `mill/_proto/`. They are generated by `codegen.py`. Extend via wrappers in `mill/types.py`, `mill/vectors.py`, etc. |
| **`datetime` only** | No third-party temporal libraries (no `whenever`, no `arrow`, no `pendulum`). Use stdlib `datetime` module for all temporal types. Use `datetime.timezone.utc` for UTC. |
| **Lazy imports for extras** | `to_arrow()`, `to_pandas()`, `to_polars()` must use lazy imports. If the extra is not installed, raise `ImportError` with install instructions (e.g. `"Install mill-py[arrow]"`). Never import `pyarrow`, `pandas`, or `polars` at module top-level in core code. |
| **User documentation** | Public docs live at `docs/public/src/connect/python/` (MkDocs Material, nav registered in `mkdocs.yml` under `Connect > Python`). Target audience: Python developers connecting to Mill. After each WI that changes user-visible behaviour, update the relevant page(s). Pages: `index.md` (overview/quickstart), `authentication.md`, `querying.md`, `dataframes.md`, `async.md`, `types.md`, `discovery.md`. Create pages incrementally as content becomes relevant. |

### 16.3 Testing Rules

| Rule | Detail |
|------|--------|
| **Framework** | `pytest` for all tests. `pytest-asyncio` for async tests. `pytest-mock` for mocking. |
| **Test file naming** | `test_<module>.py` (e.g. `test_types.py`, `test_vectors.py`). |
| **Test function naming** | `test_<what>_<condition>` style (e.g. `test_read_date_from_epoch_days`, `test_connect_bare_hostname_raises`). |
| **Unit tests** | Must not require a running server. Use mock transports / mock proto messages. Live in `tests/unit/`. |
| **Integration tests** | Require a running Mill service. Live in `tests/integration/`. Configured via `MILL_IT_*` env vars (protocol, host, port, TLS, auth). Skipped when `MILL_IT_PROTOCOL` is unset. Run with `pytest -m integration`. |
| **Independent execution** | Unit and integration tests must be independently executable. Use pytest markers: `@pytest.mark.unit` (default), `@pytest.mark.integration`. Configure in `pyproject.toml` so `pytest` runs only unit tests by default, and `pytest -m integration` runs integration tests. Both suites must pass in isolation without cross-dependencies. |
| **Adequate coverage per phase** | Tests must adequately cover the scope of each phase. After each phase, run coverage and record the result in the "Latest Test Results" section of the implementation plan. Coverage reports are generated via `pytest --cov=mill --cov-report=term-missing`. |
| **Phase 5 is a gate** | Integration tests (Phase 5) must pass before proceeding to DataFrame extras (Phase 6). This validates that the core client, transport, types, and ResultSet work against real services. |
| **Binary test fixtures** | Existing `.bin` fixtures in `tests/unit/` use standard protobuf wire format (betterproto uses standard encoding). They should be compatible with the new `protobuf` library's `ParseFromString()`. Verify and reuse if possible. |
| **Coverage** | Aim for high coverage on `mill/types.py`, `mill/vectors.py`, `mill/result.py`. Every logical type must have at least one test for native conversion and one for Arrow conversion. |
| **Cleanup** | At the end of each WI, remove any files, functions, modules, or dependencies that are no longer used. Do not leave dead code. This includes old test files from the previous implementation that have been superseded. |

### 16.4 Architecture & Design Rules

| Rule | Detail |
|------|--------|
| **Sync-first** | The primary API is synchronous (`mill.MillClient`, `mill.connect()`). Async lives in `mill.aio` and mirrors sync exactly. No manual event loop management anywhere. |
| **No raw protobuf in public API** | Users never see proto message classes. All public types (`MillType`, `MillField`, `MillSchema`, `MillTable`, `ResultSet`) are pure Python. Proto conversion happens via `from_proto()` factory methods internally. |
| **ResultSet is lazy-with-cache** | Do not change this to eager. VectorBlocks are pulled on-demand from the transport, cached once consumed, and replayed on re-iteration. `fetchall()` / `to_*()` force full consumption. See Section 15.1. |
| **PyArrow is the foundation for DataFrames** | All DataFrame conversions go through Arrow: `VectorBlock → RecordBatch → Table → pandas/polars`. Arrow logic is written once in `mill/extras/arrow.py`. Pandas and polars are thin wrappers. |
| **Three transport modes** | gRPC (binary protobuf, server-streaming), HTTP-JSON (JSON both ways), HTTP-Protobuf (binary both ways). The `encoding` parameter on `connect()` controls HTTP mode. gRPC always uses binary. |
| **Transport ABC** | `mill/_transport/__init__.py` defines the abstract transport. gRPC and HTTP transports implement it. The client (`MillClient`) depends only on the abstract interface. |
| **Auth via `to_headers()`** | All credential types expose `to_headers() -> dict[str, str]`. Transports handle key casing: gRPC uses lowercase `authorization`, HTTP uses capitalized `Authorization`. |
| **Discovery is a stub** | `connect()` with a bare hostname (no scheme) fetches `/.well-known/mill` and raises `NotImplementedError`. The `MillServiceDescriptor` model exists for future work. Do not implement auto-protocol-selection yet. |
| **Dead code cleanup** | At the end of every WI, remove unused files, functions, modules, and dependencies. No dead code should remain after a WI commit. |
| **Coverage per phase** | After every phase, run `pytest --cov=mill --cov-report=term-missing tests/unit/` and record the result in Section 8 ("Latest Test Results") of the implementation plan. Only keep the latest result. |

### 16.5 Dependency Rules

| Rule | Detail |
|------|--------|
| **Python version** | Target: 3.10 (negotiable), 3.11, 3.12 (firm), 3.13 (negotiable). `python = "^3.10,<4.0"` in `pyproject.toml`. Do not use features exclusive to 3.11+ (e.g. `ExceptionGroup`, `tomllib`) without a 3.10 fallback. |
| **Core deps** (always installed) | `grpcio`, `protobuf`, `httpx`. No other runtime dependencies in core. |
| **Extras** | `arrow` = `[pyarrow]`, `pandas` = `[pyarrow, pandas]`, `polars` = `[pyarrow, polars]`, `all` = `[pyarrow, pandas, polars]`. |
| **Removed deps** | `betterproto`, `grpclib`, `aiohttp`, `aiostream`, `whenever`, `logo`, `urllib3`. Do not re-introduce. |
| **Dev deps** | `grpcio-tools`, `pytest`, `pytest-asyncio`, `pytest-mock`, `pytest-cov`, `coverage`, `pyarrow`, `pandas`, `polars`. Keep in `[tool.poetry.group.dev.dependencies]`. |
| **No version pinning in extras** | Use `"*"` or compatible ranges in extras. Pin versions only in core where needed for API stability. |
| **Poetry** | Project uses Poetry for dependency management and packaging. Use `pyproject.toml` (not `setup.py`). |

### 16.6 Repository-Level Rules (from AGENTS.md)

These apply to the whole repo, including `mill-py`:

- **Never update git config.**
- **Never push to `feature` or `main`.** Only push to the working branch (`refactor/py-client`).
- **Commit messages**: bracketed prefix style (`[refactor]`, `[fix]`, `[docs]`, `[wip]`),
  imperative mood, under 72 characters.
- **No `Co-Authored-By`** or similar trailers in commit messages.
- **Keep secrets out of the repo.** No API keys, tokens, or passwords in source files.
  Test credentials (for integration tests) come from env vars or `.test-profiles` (gitignored
  or contains only `localhost` entries).
- **Generated code**: proto stubs are committed (required for PyPI). Do not hand-edit them.
  Regenerate via `codegen.py`.

### 16.7 Phase Ordering & Dependencies

Phases must be executed in order. Each phase builds on the previous:

```
Phase 0: Archive existing code → misc/mill-py/
Phase 1: Project scaffold, pyproject.toml, proto codegen → mill/_proto/
Phase 2: Type system, vector readers → mill/types.py, mill/vectors.py
Phase 3: Transport layer → mill/_transport/, mill/auth.py, mill/exceptions.py
Phase 4: Sync client, ResultSet, connect() → mill/client.py, mill/result.py, mill/__init__.py
Phase 5: Integration tests (GATE — must pass before Phase 6)
Phase 6: DataFrame extras → mill/extras/
Phase 7: Async API → mill/aio/
Phase 8: Documentation + PyPI polish
Phase 9: SQL Dialect Foundation → mill/sql/ (shared by Phases 10–11)
Phase 10: ibis backend POC (exploratory)
Phase 11: DBAPI + SQLAlchemy POC (exploratory)
```

**Do not skip phases.** Phase 5 (integration tests) is an explicit gate: it validates that
the core works against real services before building DataFrame and async layers on top.

### 16.8 File & Module Naming

| Pattern | Rule |
|---------|------|
| Public modules | `mill/types.py`, `mill/client.py`, `mill/result.py`, `mill/auth.py` — no underscore prefix. |
| Internal modules | `mill/_transport/`, `mill/_proto/` — underscore prefix signals "private". |
| Extras | `mill/extras/arrow.py`, `mill/extras/pandas.py`, `mill/extras/polars.py`. |
| Async mirror | `mill/aio/client.py`, `mill/aio/_transport/`. Same structure as sync but under `aio/`. |
| Tests | `tests/unit/test_*.py`, `tests/integration/test_*.py`. |
| Config | `pyproject.toml` (Poetry), `codegen.py` (proto generation), `.test-profiles` (integration test profiles). |

### 16.9 Error Handling Conventions

- **All custom exceptions** inherit from `MillError` (base).
- **Exception hierarchy**: `MillError` > `MillConnectionError`, `MillQueryError`, `MillAuthError`, `MillServerError`.
- `MillConnectionError`: transport-level failures (can't reach host, timeout, TLS errors).
- `MillQueryError`: server returned an error for a query (bad SQL, missing table).
- `MillAuthError`: authentication/authorization failure (gRPC `UNAUTHENTICATED`/`PERMISSION_DENIED`, HTTP 401/403).
- Always call `super().__init__(message)` in exception constructors (fixes existing bug).
- Map gRPC status codes and HTTP status codes to the appropriate exception type in each transport.
- Include the original error as `__cause__` (use `raise MillConnectionError(...) from original`).

### 16.10 What NOT To Do

| Don't | Why |
|-------|-----|
| Don't expose proto messages in public API | Users must never import from `mill._proto`. |
| Don't use `asyncio.get_event_loop()` | Deprecated since Python 3.10. The sync API is truly synchronous (grpcio + httpx are sync-native). |
| Don't use star-imports | Breaks explicit API surface, makes debugging harder. |
| Don't add `whenever` or other temporal libs | stdlib `datetime` only. |
| Don't make DataFrame deps required | Always optional extras with lazy imports. |
| Don't implement service discovery logic | Only the stub + `NotImplementedError`. The model classes exist for future work. |
| Don't create per-phase branches | Everything goes on `refactor/py-client`. |
| Don't skip the integration test gate | Phase 5 must pass before Phase 6. |
| Don't hand-edit generated proto stubs | Modify `codegen.py` and regenerate. |
| Don't push to `feature` or `main` | Only push to `refactor/py-client`. User merges. |

---

## 17. File Inventory — Quick Reference

### Python client files (current state after Phase 8)

| File | Status | Purpose |
|------|--------|---------|
| `pyproject.toml` | **Done (P8)** | Poetry config — deps, extras, classifiers, license, pytest markers |
| `codegen.py` | Done (P1) | Proto stub generator — grpcio-tools, import fixing |
| `README.md` | **Done (P8)** | Full docs: install, quickstart, auth, TLS, async, types, errors |
| `LICENSE` | **Done (P8)** | Apache-2.0 license |
| `.gitignore` | Done (P1) | Ignores `.build/`, keeps `_proto/` |
| `mill/__init__.py` | **Done (P4)** | Public API, sync `connect()` factory |
| `mill/types.py` | **Done (P2)** | `MillType` enum (16), `MillField`, `MillTable`, `MillSchema` with `from_proto()` |
| `mill/vectors.py` | **Done (P2)** | 16 reader functions, `read_column()`, `read_vector_block()` |
| `mill/client.py` | **Done (P4)** | `MillClient` — sync client |
| `mill/result.py` | **Done (P4)** | `ResultSet` — lazy-with-cache iteration |
| `mill/auth.py` | **Done (P3)** | `BasicAuth`, `BearerToken`, `_auth_headers()` |
| `mill/exceptions.py` | **Done (P3)** | `MillError` hierarchy + error mappers |
| `mill/discovery.py` | **Done (P4)** | `MillServiceDescriptor` + `fetch_descriptor()` stub |
| `mill/_transport/__init__.py` | **Done (P3)** | `Transport` ABC |
| `mill/_transport/_grpc.py` | **Done (P3/P8)** | `GrpcTransport` (sync, grpcio) |
| `mill/_transport/_http.py` | **Done (P3/P8)** | `HttpTransport` (sync, httpx) |
| `mill/aio/__init__.py` | **Done (P7)** | Async `connect()` factory |
| `mill/aio/client.py` | **Done (P7)** | `AsyncMillClient` — async client |
| `mill/aio/result.py` | **Done (P7)** | `AsyncResultSet` — async lazy-with-cache iteration |
| `mill/aio/_transport/__init__.py` | **Done (P7)** | `AsyncTransport` ABC |
| `mill/aio/_transport/_grpc.py` | **Done (P7/P8)** | `AsyncGrpcTransport` (grpc.aio) |
| `mill/aio/_transport/_http.py` | **Done (P7/P8)** | `AsyncHttpTransport` (httpx.AsyncClient) |
| `mill/extras/__init__.py` | Done (P6) | DataFrame extras package |
| `mill/extras/arrow.py` | **Done (P6)** | `arrow_type()`, `vector_block_to_record_batch()`, `result_to_arrow()` — all 16 types |
| `mill/extras/pandas.py` | **Done (P6)** | `result_to_pandas()` — thin wrapper via Arrow |
| `mill/extras/polars.py` | **Done (P6)** | `result_to_polars()` — thin wrapper via `polars.from_arrow()` |
| `mill/_proto/` | Done (P1) | Generated grpcio/protobuf stubs (43 files, committed) |
| `tests/conftest.py` | **Done (P2)** | Auto-applies unit/integration markers |
| `tests/unit/conftest.py` | **Done (P2)** | Unit test fixtures |
| `tests/unit/test_scaffold.py` | Done (P1) | 19 import smoke tests |
| `tests/unit/test_types.py` | **Done (P2)** | 72 type system tests |
| `tests/unit/test_vectors.py` | **Done (P2)** | 49 vector reader tests |
| `tests/unit/test_transport.py` | **Done (P3)** | Sync transport mock tests |
| `tests/unit/test_client.py` | **Done (P4)** | MillClient mock tests |
| `tests/unit/test_result.py` | **Done (P4)** | ResultSet tests |
| `tests/unit/test_arrow.py` | **Done (P6)** | 22 Arrow conversion tests (all 16 types) |
| `tests/unit/test_pandas.py` | **Done (P6)** | Pandas conversion tests |
| `tests/unit/test_polars.py` | **Done (P6)** | Polars conversion tests |
| `tests/unit/test_synthetic_*.py` | **Done (P6)** | Synthetic all-types tests for Arrow/Pandas/Polars |
| `tests/unit/test_async_transport.py` | **Done (P7)** | 16 async transport tests |
| `tests/unit/test_async_client.py` | **Done (P7)** | 20 async client tests |
| `tests/unit/test_async_result.py` | **Done (P7)** | 16 async result tests |
| `tests/integration/conftest.py` | **Done (P5/P7)** | `IntegrationConfig`, env-var config, async fixtures |
| `tests/integration/test_handshake.py` | **Done (P5)** | 2 tests — protocol version, capabilities |
| `tests/integration/test_schemas.py` | **Done (P5)** | 7 tests — list/get schema, field validation |
| `tests/integration/test_query.py` | **Done (P5)** | 10 tests — SELECT, WHERE, JOIN, paging |
| `tests/integration/test_auth.py` | **Done (P5)** | 4 tests — identity, bad creds |
| `tests/integration/test_async_query.py` | **Done (P7)** | 12 async integration tests |
| `clients/Makefile` | **Done (P8)** | Dev targets: codegen, test, test-integration, install, uninstall, build, publish |
| `clients/examples/quickstart.py` | **Done (P8)** | 9 runnable examples — gRPC, HTTP, auth, DataFrames, async, errors |
| `clients/examples/mill_py_quickstart.ipynb` | **Done (P8)** | Jupyter notebook walkthrough (12 cells) |
| `docs/public/src/connect/python/index.md` | **Done (P8)** | User docs: overview, install, quickstart, connect() |
| `docs/public/src/connect/python/authentication.md` | **Done (P8)** | User docs: BasicAuth, BearerToken, TLS/mTLS |
| `docs/public/src/connect/python/querying.md` | **Done (P8)** | User docs: SQL queries, ResultSet, error handling |
| `docs/public/src/connect/python/dataframes.md` | **Done (P8)** | User docs: Arrow, pandas, polars extras |
| `docs/public/src/connect/python/async.md` | **Done (P8)** | User docs: mill.aio, async connect, concurrent queries |
| `docs/public/src/connect/python/types.md` | **Done (P8)** | User docs: 16 Mill types, Python/Arrow mappings |
| `docs/public/src/connect/python/discovery.md` | **Done (P8)** | User docs: .well-known/mill stub |
| `docs/public/src/connect/python/installation.md` | **Done (P8)** | User docs: detailed install guide (NOT in mkdocs nav yet) |
| `mill/sql/__init__.py` | *Planned (P9A)* | SQL dialect package init |
| `mill/sql/dialect.py` | *Planned (P9A)* | `MillDialectDescriptor`, `CALCITE_DEFAULT`, `quote_identifier()`, `qualify()` |
| `mill/sql/types.py` | *Planned (P9A)* | `SQL_TYPE_NAMES`, `PYTHON_TYPES`, `DBAPI_TYPE_CODES`, `TYPE_INFO`, `to_sa_type()` |
| `mill/sql/_tester.py` | *Planned (P9B)* | `DialectTester` — generates ~80 SQL queries, validates via `parse_sql()` |
| `tests/unit/test_sql_dialect.py` | *Planned (P9A)* | Descriptor, quoting, qualify, feature flags, limits |
| `tests/unit/test_sql_types.py` | *Planned (P9A)* | Type mappings, `to_sa_type()` |
| `tests/integration/test_dialect.py` | *Planned (P9B)* | Runs `DialectTester`, writes `py-sql-dialect-report.md` |
| `misc/mill-py/` | Archive | Old `millclient` code for reference (Phase 0) |

### Server-side files (reference only — not modified)

| File | Purpose |
|------|---------|
| `data/mill-data-grpc-service/.../MillGrpcService.java` | gRPC service impl |
| `data/mill-data-grpc-service/.../MillGrpcServiceExceptionAdvice.java` | gRPC error handling |
| `data/mill-data-grpc-service/.../GrpcServiceSecurityConfiguration.java` | gRPC security |
| `data/mill-data-grpc-service/.../GrpcServiceDescriptor.java` | Service descriptor |
| `data/mill-data-http-service/.../AccessServiceController.java` | HTTP controller |
| `data/mill-data-http-service/.../MessageHelper.java` | HTTP content negotiation |
| `data/mill-data-http-service/.../ProtobufUtils.java` | Dead code |
| `core/.../DataOperationDispatcher.java` | Shared dispatcher interface |
| `core/.../ServiceHandler.java` | Dispatcher holder |

### Proto files (source of truth)

| File | Content |
|------|---------|
| `proto/common.proto` | Schema, Table, Field, LogicalDataType, DataType, ProtocolVersion |
| `proto/vector.proto` | VectorBlockSchema, VectorBlock, Vector (7 variants) |
| `proto/statement.proto` | SQLStatement, Parameter, PlanStatement, TextPlanStatement |
| `proto/data_connect_svc.proto` | DataConnectService (7 RPCs), request/response messages |
| `proto/substrait/*.proto` | Substrait plan format (9 files) |
