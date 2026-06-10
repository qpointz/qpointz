# ADO.NET Provider WI Draft

This document is the implementation draft for the developer onboarding into `C-17`.
It complements the formal work item in `docs/workitems/WI-077-adonet-provider.md`.

## Scope Summary

Deliver a managed ADO.NET provider for Mill with:

- dual transport support: gRPC and HTTP
- `DbConnection`, `DbCommand`, `DbDataReader`
- read-only SQL execution
- schema and dialect introspection
- integration tests against a skymill-backed Mill service

Out of scope:

- ODBC
- native OLE DB COM provider
- write transactions
- EF Core provider work

## Draft Work Item

### Problem

Mill has Python and JDBC clients, but no first-party ADO.NET provider. This blocks standard
.NET integration through `DbConnection`/`DbCommand`/`DbDataReader`, including lightweight
consumer stacks such as Dapper.

### Goal

Create a production-viable managed provider that behaves consistently with existing Mill
clients and can execute against both gRPC and HTTP service endpoints.

### Deliverables

1. .NET client/provider module skeleton
2. generated or checked-in C# proto bindings for required contracts
3. shared transport abstraction
4. gRPC transport implementation
5. HTTP transport implementation
6. `MillConnection`
7. `MillCommand`
8. `MillDataReader`
9. limited parameter collection / command configuration support
10. metadata support for handshake, schema discovery, and dialect discovery
11. integration test suite against skymill-backed service
12. basic usage documentation

### Recommended Implementation Breakdown

#### Phase 1: transport and contracts

- generate or import C# types for:
  - `data_connect_svc.proto`
  - `dialect.proto`
  - `statement.proto`
  - `vector.proto`
- define internal transport interface:
  - `HandshakeAsync`
  - `ListSchemasAsync`
  - `GetSchemaAsync`
  - `GetDialectAsync`
  - `ExecuteQueryAsync`

#### Phase 2: connection and command

- implement connection string parsing
- implement provider config and auth/TLS settings
- implement command text, timeout, cancellation boundary
- enforce read-only transaction semantics

#### Phase 3: reader over vector blocks

- decode field metadata from vector block schema
- implement ordinal/name lookup
- implement typed getters
- implement null handling
- implement iteration across:
  - streamed gRPC responses
  - paged HTTP responses

#### Phase 4: metadata and polish

- expose provider metadata
- wire schema discovery
- wire dialect discovery where capability is enabled
- add smoke coverage for Dapper-style usage

## Integration Testing Approach

The ADO.NET provider should follow the pattern already used by Python and JDBC clients:

- unit tests for transport-independent behavior
- live integration tests against a running Mill service
- skymill fixture as the canonical schema
- environment-variable driven test configuration

### Existing Test References

Mirror these existing suites as closely as practical:

- Python:
  - `clients/mill-py/tests/integration/conftest.py`
  - `clients/mill-py/tests/integration/test_handshake.py`
  - `clients/mill-py/tests/integration/test_schemas.py`
  - `clients/mill-py/tests/integration/test_query.py`
- JDBC:
  - `clients/mill-jdbc-driver/src/testIT/java/io/qpointz/mill/TestITProfile.java`
  - `clients/mill-jdbc-driver/src/testIT/java/io/qpointz/mill/client/MillClientTestIT.java`
  - `clients/mill-jdbc-driver/src/testIT/java/io/qpointz/mill/JetDriverTestIT.java`

### Recommended .NET Test Layers

#### 1. Pure unit tests

No live Mill service required.

Cover:

- connection string parsing
- auth/TLS option mapping
- HTTP endpoint composition
- gRPC call setup
- vector-to-row decoding
- `DbDataReader` getters and metadata
- null and type conversion rules

#### 2. Transport contract tests

Use mocked gRPC channel / mocked HTTP server.

Cover:

- `Handshake`
- `ListSchemas`
- `GetSchema`
- `GetDialect`
- HTTP paging flow:
  - `SubmitQuery`
  - `FetchQueryResult`
- gRPC streaming flow for `ExecQuery`

#### 3. Live integration tests

Run against a skymill-backed Mill service image.

Recommended baseline scenarios:

- handshake succeeds and reports `V1_0`
- list schemas contains `skymill`
- get schema returns expected tables
- select from `skymill.cities`
- where filter returns expected rows
- join `segments` to `cities`
- large result requires multiple pages/blocks
- repeated execution returns stable counts
- `GetDialect` works when capability is enabled

### Skymill Service For Integration Tests

Use this skymill-backed service image for onboarding and integration tests:

- `qpointz/mill-service-samples:v0.6.2`

The contract for the image should be:

- expose Mill data service over gRPC and/or HTTP
- load the `skymill` fixture
- keep a stable schema name: `skymill`
- support anonymous auth for the default happy-path suite
- optionally support auth/TLS variants in a second-stage matrix

This image should be treated as the default test fixture target for the initial .NET provider
implementation unless a later CI-specific wrapper image is introduced.

### Suggested Local Startup

Start the sample service locally with Docker:

```bash
docker run --rm -p 8080:8080 -p 9090:9090 qpointz/mill-service-samples:v0.6.2
```

If the container exposes only one transport in a given environment, keep the other transport's
tests separately selectable rather than forcing a single-process all-modes suite.

### Suggested Test Configuration Contract

Reuse the same environment variable family already used by Python/JDBC:

- `MILL_IT_HOST`
- `MILL_IT_PORT`
- `MILL_IT_PROTOCOL`
- `MILL_IT_BASE_PATH`
- `MILL_IT_TLS`
- `MILL_IT_TLS_CA`
- `MILL_IT_TLS_CERT`
- `MILL_IT_TLS_KEY`
- `MILL_IT_AUTH`
- `MILL_IT_USERNAME`
- `MILL_IT_PASSWORD`
- `MILL_IT_TOKEN`
- `MILL_IT_SCHEMA`

Recommended defaults:

- gRPC: `grpc://localhost:9090`
- HTTP: `http://localhost:8501/services/jet`
- schema: `skymill`
- auth: none
- TLS: false

This keeps .NET integration testing aligned with the current cross-client convention.

## Quick Start Digest For The New Developer

### Read first

1. `docs/design/client/01-adonet-provider-start-here.md`
2. `docs/design/client/02-adonet-provider-data-lane.md`
3. `docs/design/client/adonet-provider-design.md`
4. `docs/workitems/WI-077-adonet-provider.md`

### Inspect these code paths

- `proto/data_connect_svc.proto`
- `services/mill-data-grpc-service/.../MillGrpcService.java`
- `services/mill-data-http-service/.../AccessServiceController.java`
- `data/mill-data-backend-core/.../DataOperationDispatcherImpl.java`
- `clients/mill-jdbc-driver/.../GrpcMillClient.java`
- `clients/mill-jdbc-driver/.../HttpMillClient.java`
- `clients/mill-py/tests/integration/`

### Keep these public docs open

- <https://docs.qpointz.io/quickstart/>
- <https://docs.qpointz.io/connect/python/>
- <https://docs.qpointz.io/connect/python/querying/>
- <https://docs.qpointz.io/backends/calcite/>
- <https://docs.qpointz.io/backends/flow/>
- <https://docs.qpointz.io/reference/skymill-schema/>

### First implementation target

The shortest path to useful progress is:

1. implement a transport abstraction
2. prove `Handshake`, `ListSchemas`, `GetSchema`
3. implement query execution
4. implement `DbDataReader` over result blocks
5. then add metadata and Dapper smoke coverage

## Acceptance Checklist For Initial Delivery

- provider can open against both gRPC and HTTP endpoints
- provider can execute a simple query against `skymill.cities`
- provider can iterate all rows using `DbDataReader`
- provider exposes column names/types/nullability with reasonable accuracy
- provider can discover schemas and tables
- provider has integration coverage aligned to Python/JDBC happy-path scenarios
- provider documents unsupported features explicitly
