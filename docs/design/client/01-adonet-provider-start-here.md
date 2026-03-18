# ADO.NET Provider Start Here

This document is the entry point for a developer joining the managed ADO.NET provider
implementation track (`C-17` / `WI-077`).

Read in this order:

1. [ADO.NET Provider Design](adonet-provider-design.md) for the current target surface.
2. [ADO.NET Provider Data Lane](02-adonet-provider-data-lane.md) for how Mill reads data,
   plans SQL, executes queries, and returns results to clients.
3. [ADO.NET Provider WI Draft](03-adonet-provider-wi-draft.md) for implementation scope,
   integration testing strategy, and onboarding checklist.

## Problem Statement

Mill already has a stable remote query surface and first-party client patterns in Python and JDBC,
but it still has no native .NET access path built around standard ADO.NET abstractions. That gap
matters for three reasons.

First, .NET is still the default integration layer for a large class of enterprise applications,
internal tools, reporting jobs, data-access services, and lightweight APIs. Without an ADO.NET
provider, a .NET team cannot use Mill through the APIs they already know: `DbConnection`,
`DbCommand`, `DbDataReader`, connection strings, provider factories, and the ecosystem built on
top of them.

Second, the absence of an ADO.NET provider makes Mill feel incomplete as a platform client story.
Today, Java users have JDBC and Python users have `mill-py`, but .NET users would need to either:

- write transport code directly against gRPC/HTTP
- build and maintain their own provider wrapper
- avoid Mill entirely in .NET-heavy environments

That creates unnecessary adoption friction and duplicates work outside the product boundary.

Third, an ADO.NET provider is the right abstraction layer for the current Mill service model.
Mill already exposes:

- connection-style metadata calls (`Handshake`, `ListSchemas`, `GetSchema`, `GetDialect`)
- command-style SQL execution
- streamed/paged query results
- a read-mostly, service-oriented access pattern

Those capabilities map naturally onto ADO.NET. We do not need to invent a new .NET-specific API;
we need to expose the existing Mill contract through the .NET standard data access surface.

In short, we implement this provider to remove a platform gap, unlock normal .NET adoption, and
package Mill's existing service semantics into the standard abstraction expected by .NET users and
tools.

## What This Track Is

The goal is a first-party managed .NET provider for Mill:

- `DbConnection`
- `DbCommand`
- `DbDataReader`
- gRPC and HTTP transport support
- read-only query execution
- schema and dialect introspection

This is not an ODBC or native OLE DB effort. OLE DB remains an optional follow-up only.

## What The Implementer Needs To Understand First

At the client boundary, Mill is a transport-accessible query service, not an embedded SQL
engine inside the client.

The ADO.NET provider only needs to do four things well:

1. Build requests for the existing Mill service contracts.
2. Support both Mill transports:
   - gRPC server-streaming for `ExecQuery`
   - HTTP request/paged-result flow via `SubmitQuery` + `FetchQueryResult`
3. Turn returned vector blocks into row-oriented ADO.NET reader semantics.
4. Surface metadata and errors in a standard .NET way.

## Existing Client Implementations To Reuse Conceptually

The fastest way to understand the target behavior is to compare the two existing client tracks:

- Java/JDBC transport surface:
  - `clients/mill-jdbc-driver/src/main/java/io/qpointz/mill/client/MillClient.java`
  - `clients/mill-jdbc-driver/src/main/java/io/qpointz/mill/client/GrpcMillClient.java`
  - `clients/mill-jdbc-driver/src/main/java/io/qpointz/mill/client/HttpMillClient.java`
- Python transport and result handling:
  - `clients/mill-py/mill/_transport/_grpc.py`
  - `clients/mill-py/mill/_transport/_http.py`
  - `clients/mill-py/tests/integration/`

The ADO.NET provider should follow these semantics rather than inventing a new contract.

## Public Documentation Digest

These public docs are the quickest external orientation:

- Quickstart: <https://docs.qpointz.io/quickstart/>
- Python client overview: <https://docs.qpointz.io/connect/python/>
- Python querying model: <https://docs.qpointz.io/connect/python/querying/>
- SQLAlchemy reflection/introspection example: <https://docs.qpointz.io/connect/python/sqlalchemy/>
- Calcite backend overview: <https://docs.qpointz.io/backends/calcite/>
- Flow backend overview: <https://docs.qpointz.io/backends/flow/>
- Skymill schema reference: <https://docs.qpointz.io/reference/skymill-schema/>
- Skymill KPI cookbook: <https://docs.qpointz.io/reference/skymill-kpi-cookbook/>

Why these matter for `C-17`:

- The backend pages explain what the server is actually querying.
- The Python pages show the intended shape of the client-facing API behavior.
- The skymill pages define the fixture schema used by integration tests and examples.

## Quick Start Digest For The Implementer

### 1. Learn the service contract first

Start with:

- `proto/data_connect_svc.proto`
- `proto/dialect.proto`
- `proto/statement.proto`
- `proto/vector.proto`

For the ADO.NET MVP, the critical operations are:

- `Handshake`
- `ListSchemas`
- `GetSchema`
- `GetDialect`
- `ExecQuery` for gRPC
- `SubmitQuery` and `FetchQueryResult` for HTTP paging

### 2. Learn the transport behavior from existing clients

Key observations from the current implementations:

- gRPC uses unary calls for metadata operations and server-streaming for query results.
- HTTP uses POST endpoints under `/services/jet`.
- HTTP metadata calls send JSON or protobuf payloads and receive JSON or protobuf responses.
- HTTP query execution is paged:
  - initial request: `SubmitQuery`
  - continuation request: `FetchQueryResult`
- Current default test paths are:
  - gRPC: `localhost:9090`
  - HTTP: `localhost:8501` or a service-specific port with `/services/jet`

### 3. Use skymill as the canonical fixture

The integration target schema is `skymill`.

For local onboarding and integration testing, use:

- Docker image: `qpointz/mill-service-samples:v0.6.2`

Core expected tables include:

- `cities`
- `segments`
- `aircraft`
- `bookings`
- `flight_instances`
- `cargo_shipments`

Useful local references:

- `test/datasets/skymill/README.md`
- `docs/public/src/reference/skymill-schema.md`
- `docs/public/src/reference/skymill-kpi-cookbook.md`

### 4. Build the provider in thin layers

Recommended implementation order:

1. C# proto generation and transport abstraction
2. `MillConnection`
3. `MillCommand`
4. `MillDataReader`
5. metadata support and provider factory
6. integration tests against skymill-backed service

### 5. Defer unnecessary scope

Do not start with:

- transactions
- updates/inserts
- EF Core provider integration
- BI-tool-specific workarounds
- OLE DB compatibility

## Local Documents Worth Keeping Open

- [ADO.NET Provider Design](adonet-provider-design.md)
- [ADO.NET Provider Data Lane](02-adonet-provider-data-lane.md)
- [ADO.NET Provider WI Draft](03-adonet-provider-wi-draft.md)
- [Python cold start](py-cold-start.md)
- [Python implementation plan](py-implementation-plan.md)

## Definition Of "Done" For Initial Onboarding

The developer should be able to answer these questions after reading this packet:

- What Mill service operations must the provider call?
- How do HTTP and gRPC query execution differ?
- What is the server-side SQL execution engine?
- Why does the provider need a vector-to-row adapter layer?
- Which existing tests and fixtures should be mirrored for .NET?
- What is explicitly in scope vs out of scope for `C-17`?
