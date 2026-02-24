# Client Libraries

Design documents for Mill client libraries and their supporting artifacts.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Python client (mill-py) architecture, implementation plans, or codebase analysis
- JDBC driver design and implementation
- SQL dialect descriptor schemas and their consumers (SQLAlchemy, JDBC, ibis, AI)
- Client-side API design, type mappings, or cold-start references
- Any client library's internal refactoring or phase tracking

## Does NOT Belong Here

- The Mill type system itself (protobuf types, vector encoding) → `data/`
- Server-side gRPC/HTTP protocol definitions → `platform/`
- AI-specific flows that happen to use a client → `ai/`
- General Python or Java tooling not specific to a Mill client → `platform/`

## Documents

| File | Description |
|------|-------------|
| `adonet-provider-design.md` | Managed .NET provider (ADO.NET) design track with phased architecture and delivery plan |
| `odbc-driver-design.md` | ODBC driver design track with native/bridge strategy options, risks, and conformance plan |
| `py-cold-start.md` | mill-py cold-start guide: codebase analysis, server contracts, how to resume work |
| `py-implementation-plan.md` | mill-py refactoring implementation plan: scope, architecture, phases, tracking |
| `py-sql-dialect-plan.md` | Mill SQL dialect foundation design and tracking (Phase 9 of mill-py) |
| `sql-dialect-yaml-schema.md` | Target YAML schema for SQL dialect descriptors across all consumers |
