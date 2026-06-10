# WI-077 - Managed ADO.NET Provider for Mill

Status: `planned`  
Type: `feature`  
Area: `client`, `platform`  
Backlog refs: `C-17`

## Problem Statement

Mill currently has Python and JDBC client surfaces, but there is no first-party .NET client
surface that integrates with standard ADO.NET consumers. This blocks direct use from
plain `DbConnection`/`DbCommand` code, limits compatibility with Dapper-style consumers,
and leaves .NET users without a supported path that matches existing gRPC and HTTP
transport capabilities.

The backlog already distinguishes this track from ODBC and from any native OLE DB work.
The immediate need is a managed ADO.NET provider, not a COM/OLE DB provider.

## Goal

Deliver a first-party managed ADO.NET provider for Mill with:

- dual transport support (`gRPC` and `HTTP`)
- read-only query execution over existing Mill service contracts
- `DbDataReader`-based result consumption over streamed/vectorized results
- schema metadata discovery aligned with existing Mill introspection APIs
- authentication and TLS configuration parity with current clients

Phase 1 should be usable from plain ADO.NET code and reasonable read-only Dapper scenarios.

## In Scope

1. Create a dedicated .NET client module for the managed provider.
2. Generate or otherwise maintain C# bindings for required Mill protobuf contracts.
3. Implement a shared transport abstraction with gRPC and HTTP variants.
4. Implement minimal ADO.NET provider primitives:
   - `MillConnection : DbConnection`
   - `MillCommand : DbCommand`
   - `MillDataReader : DbDataReader`
   - provider-specific parameter collection with limited initial support
   - provider factory if needed for standard registration flows
5. Support query execution using existing Mill query APIs and streamed/pageable results.
6. Support basic metadata/introspection:
   - handshake/capabilities
   - list schemas
   - get schema
   - dialect discovery where available
   - reader schema / column metadata exposure
7. Implement baseline .NET type mapping from Mill logical types to CLR and `DbType`.
8. Add unit and integration tests for both transports.

## Out of Scope

- Native ODBC driver implementation.
- Native OLE DB COM provider implementation.
- Full EF Core provider implementation.
- Write/update/delete semantics or real transaction support beyond explicit read-only behavior.
- Broad BI-tool certification beyond basic compatibility smoke tests.

## Proposed Architecture

1. **Transport layer**
   - Introduce an internal transport contract that exposes the subset of Mill service calls
     needed by the provider (`Handshake`, `ListSchemas`, `GetSchema`, `GetDialect`, `ExecQuery`,
     and paging helpers if required).
   - Provide `GrpcTransport` and `HttpTransport` implementations with equivalent behavior.
2. **ADO.NET surface**
   - `MillConnection` owns connection string parsing, provider settings, capability discovery,
     and transport lifecycle.
   - `MillCommand` handles SQL text, command timeout, cancellation hooks where possible, and
     query dispatch.
   - `MillDataReader` projects vectorized result blocks into row/column accessors and schema
     metadata methods.
3. **Type mapping**
   - Define a central mapping table for Mill type -> CLR type -> `DbType`.
   - Handle nullability, binary values, and temporal values consistently across transports.
4. **Packaging**
   - Produce a distributable .NET package and document provider registration/usage.

## Implementation Plan

1. **Module bootstrap**
   - Create a dedicated client module for the managed .NET provider and establish build,
     package, and test conventions.
2. **Proto and transport parity**
   - Generate required C# protocol types and implement gRPC/HTTP transport clients that match
     existing Java/Python semantics.
3. **Connection and command surface**
   - Implement `MillConnection` and `MillCommand` with minimal read-only ADO.NET compliance.
4. **Reader and vector decoding**
   - Implement `MillDataReader` over query responses, including row iteration, typed getters,
     null handling, and schema table metadata.
5. **Metadata support**
   - Wire `ListSchemas`, `GetSchema`, handshake capability checks, and optional dialect
     discovery into provider metadata surfaces.
6. **Authentication and TLS**
   - Add configuration for anonymous/basic/bearer flows as supported by current service
     contracts, plus TLS settings parity for both transports.
7. **Compatibility and packaging**
   - Add smoke tests for plain ADO.NET usage and Dapper-style query execution, then package
     the provider for internal/public consumption.

## Acceptance Criteria

- A managed .NET provider exists and exposes working `DbConnection`, `DbCommand`, and
  `DbDataReader` primitives for Mill.
- Both `gRPC` and `HTTP` transports are supported with equivalent read-only query behavior.
- Query results can be consumed through standard `DbDataReader` APIs, including typed getters,
  ordinal/name lookup, and null handling.
- Basic schema/introspection APIs are available through the provider and/or related metadata
  surfaces.
- Authentication and TLS settings are configurable for both transports.
- Unit and integration tests cover transport parity, type conversion, and reader behavior.
- OLE DB remains explicitly out of scope for this work item.

## Test Plan (during implementation)

### Unit

- Connection string and provider option parsing.
- Type mapping and null conversion rules.
- `DbDataReader` getters, ordinals, schema metadata, and lifecycle behavior.
- Command lifecycle and unsupported transaction/write-path behavior.

### Integration

- gRPC query execution against a live Mill service.
- HTTP query execution against a live Mill service.
- Schema discovery parity (`Handshake`, `ListSchemas`, `GetSchema`, `GetDialect` where supported).
- Authentication/TLS smoke tests for supported modes.
- Basic Dapper compatibility smoke tests.

## Risks and Mitigations

- **Risk:** ADO.NET consumers vary in what parts of the provider contract they assume.  
  **Mitigation:** target strict correctness for core ADO.NET primitives first, then validate
  against plain ADO.NET and Dapper before expanding scope.

- **Risk:** Vectorized results do not map cleanly to row-oriented reader semantics.  
  **Mitigation:** keep a dedicated decoding layer inside `MillDataReader` and verify it with
  mixed-type and null-heavy test cases.

- **Risk:** Temporal type behavior differs by target framework and consumer expectations.  
  **Mitigation:** define explicit baseline mappings and document any framework-specific fallbacks.

- **Risk:** HTTP and gRPC error/query paging behavior diverge.  
  **Mitigation:** mirror existing Java/Python client semantics and enforce parity in
  integration tests.

## Deliverables

- This work item definition (`docs/workitems/WI-077-adonet-provider.md`).
- A new managed .NET client/provider module in the repository.
- Tests proving read-only ADO.NET functionality over both transports.
- Usage documentation for connection setup and supported provider capabilities.
