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
| `01-adonet-provider-start-here.md` | Entry-point onboarding packet for the ADO.NET provider implementation track; read this first |
| `02-adonet-provider-data-lane.md` | High-level explanation of Mill's server data lane, SQL engine, transports, and result flow for client implementers |
| `03-adonet-provider-wi-draft.md` | Draft delivery guide for C-17: scope, integration testing approach, and quick-start checklist |
| `odbc-driver-design.md` | ODBC driver design track with native/bridge strategy options, risks, and conformance plan |
| `py-cold-start.md` | mill-py cold-start guide: codebase analysis, server contracts, how to resume work |
| `py-implementation-plan.md` | mill-py refactoring implementation plan: scope, architecture, phases, tracking |
| `py-sql-dialect-plan.md` | Mill SQL dialect foundation design and tracking (Phase 9 of mill-py) |
| `ibis-gap-matrix.md` | WI-025 delivered scope vs remaining correctness/certification gaps for WI-023 |
| `ibis-mill-example.md` | ibis backend connect/query examples, capability-gating behavior, and known first-iteration gaps |
| `sqlalchemy-mill-example.md` | SQLAlchemy Core engine/query/introspection examples for Mill grpc/http dialects |
| `sql-dialect-maintainer-guide.md` | Maintainer technical runbook for adding new dialects end-to-end |
| `sql-dialect-yaml-schema.md` | Target YAML schema for SQL dialect descriptors across all consumers |
| `mill-py-platform-http.md` | mill-py wrappers for platform REST (`/api/v1/metadata`, `/api/v1/schema`), shared `_http_common`, canonical export/import (WI-202) |
| `client-error-transparency.md` | Data-plane error contract: RFC 9457 HTTP responses, Python/JDBC mapping, gRPC trailing metadata parity (WI-013 alignment) |
