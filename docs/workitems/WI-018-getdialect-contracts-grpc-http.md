# WI-018 — `GetDialect` Contracts (gRPC + HTTP + Handshake)

Status: `completed`  
Type: `✨ feature`  
Area: `data`, `platform`, `core`  
Backlog refs: `D-8`

## Problem Statement

The typed runtime model cannot be consumed by clients until transport contracts expose dialect
metadata consistently. Current protocols do not provide a complete dialect descriptor contract
with capability negotiation.

## Goal

Define and validate full transport contracts for dialect discovery and retrieval:

- gRPC `GetDialect` RPC/messages,
- HTTP equivalent payload/endpoint,
- handshake `supports_dialect` capability.

## In Scope

1. Add/extend proto definitions for dialect descriptor and `GetDialect`.
2. Add handshake capability flag to advertise support.
3. Define HTTP contract equivalent to gRPC semantics.
4. Define response versioning/hash metadata policy for cache/coherency.
5. Add contract-level tests for gRPC/HTTP parity.

## Out of Scope

- Server business logic that serves dialect values.
- AI/Python consumer wiring.
- Further YAML migrations.

## Implementation Plan

1. **Proto design**
   - Mirror typed model shape in proto without ambiguous map-heavy fallback sections.
   - Include future-safe extension points where needed.
2. **Handshake negotiation**
   - Add `supports_dialect` and document fallback semantics for unsupported servers.
3. **HTTP parity**
   - Define endpoint path, response schema, and error shape aligned with gRPC behavior.
4. **Contract tests**
   - Add shape/parity assertions for equivalent responses over both transports.

## Acceptance Criteria

- Proto contract compiles and includes full descriptor coverage.
- Handshake capability is present and documented.
- HTTP contract is explicitly defined and semantically equivalent.
- Contract tests validate parity between transport representations.

## Test Plan (during implementation)

### Contract

- Proto compile checks and schema compatibility checks.
- Serialization/deserialization tests for full descriptor payloads.

### Parity

- Compare canonical gRPC and HTTP payload examples for equivalence.

## Risks and Mitigations

- **Risk:** Transport contracts drift from runtime model.  
  **Mitigation:** Maintain explicit mapping tests from Kotlin model to proto/HTTP payload.

- **Risk:** Backward compatibility confusion on handshake absence.  
  **Mitigation:** Document and test strict fallback behavior.

## Deliverables

- This work item definition (`docs/workitems/WI-018-getdialect-contracts-grpc-http.md`).
- Updated proto and HTTP contract specs for dialect retrieval.
- Parity-focused contract tests.
