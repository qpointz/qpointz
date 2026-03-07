# WI-021 — Python Remote Dialect Consumption (gRPC/HTTP)

Status: `planned`  
Type: `✨ feature`  
Area: `client`, `data`  
Backlog refs: `C-1`, `C-2`, `C-3`, `C-4`, `C-5`, `D-8`

## Problem Statement

Python dialect behavior currently depends on local/static assumptions and does not yet consume the
server-provided dialect descriptor over gRPC/HTTP. This prevents full alignment with server
dialect capabilities and migrated YAML truth.

## Goal

Make Python dialect behavior remote-driven by server `GetDialect`, with transport parity and safe
fallback behavior when dialect capability is unavailable.

## In Scope

1. Add Python client capability negotiation for `supports_dialect`.
2. Fetch dialect descriptor over gRPC and HTTP using new contracts.
3. Map remote payload to Python-side descriptor model used by downstream integrations.
4. Use server-provided metadata for feature flags/functions/type mapping decisions.
5. Keep backward-compatible fallback when server capability is missing.
6. Add parity and fallback tests for both transports.

## Out of Scope

- Redesign of Python DBAPI/SQLAlchemy/ibis APIs beyond dialect source switch.
- Additional transport protocol changes unrelated to dialect retrieval.
- Server contract or endpoint redesign.

## Implementation Plan

1. **Capability handshake**
   - Add detection path for dialect support.
2. **Transport fetch**
   - Implement gRPC and HTTP dialect retrieval calls.
3. **Model mapping**
   - Convert remote descriptor payload into Python internal dialect representation.
4. **Runtime integration**
   - Wire consumers to prefer remote dialect over local static defaults.
5. **Fallback**
   - Preserve local fallback path when server lacks capability.

## Acceptance Criteria

- Python client retrieves and uses server dialect descriptors over gRPC and HTTP.
- Behavior is consistent across both transports for equivalent dialect payloads.
- Fallback path remains functional for older servers without dialect support.
- Feature/function behavior aligns with server migrated dialect metadata.

## Test Plan (during implementation)

### Unit

- Payload mapping tests for descriptor sections and defaults.
- Handshake branch tests (supports vs no-support).

### Integration

- gRPC and HTTP end-to-end dialect fetch against server implementation.
- Parity assertions across transport results.
- Fallback tests against simulated legacy server behavior.

## Risks and Mitigations

- **Risk:** Divergent behavior between transport decoders.  
  **Mitigation:** Shared normalization layer and cross-transport parity tests.

- **Risk:** Regressions in existing Python consumers from source switch.  
  **Mitigation:** Keep local fallback and add targeted compatibility tests.

## Deliverables

- This work item definition (`docs/workitems/WI-021-python-remote-dialect-consumption.md`).
- Python client remote dialect retrieval and mapping for gRPC/HTTP.
- Transport parity and fallback test coverage.
