# WI-033 — Metadata Service API Cleanup and Error Handling

Status: `planned`  
Type: `🐛 fix`  
Area: `metadata`, `platform`  
Backlog refs: `M-21`, `M-22`

## Problem Statement

The metadata service still has known cleanup items around HTTP/protobuf descriptor handling and
error reporting. These are smaller than the major metadata feature tracks but important for API
correctness and maintainability.

## Goal

Clean up the metadata service API surface so error messages are accurate and service descriptors
are wired consistently across transports.

## In Scope

1. Fix parse-error reporting in `MessageHelper` so metadata-related failures surface the correct
   details.
2. Remove dead `ProtobufUtils` code where it is no longer needed.
3. Register or correct HTTP `ServiceDescriptor` wiring for metadata service exposure.
4. Add regression tests around the cleaned-up paths.

## Out of Scope

- Broad error-contract redesign across all services.
- New metadata feature work.

## Dependencies

- None beyond the existing metadata service modules and transport plumbing.

## Implementation Plan

1. **Error-path audit**
   - Reproduce and lock current parse/service-descriptor issues with tests.
2. **Cleanup**
   - Remove dead code and fix descriptor wiring.
3. **Verification**
   - Add regression coverage for parse errors and service exposure.

## Acceptance Criteria

- Metadata parse errors no longer surface misleading generic service payloads.
- Dead protobuf utility code is removed or justified.
- Metadata HTTP descriptor wiring is explicit and tested.
- Regression tests cover the fixed paths.

## Test Plan (during implementation)

### Unit

- MessageHelper error mapping tests.

### Integration

- Metadata HTTP service descriptor exposure tests.
- Regression tests for malformed request handling.

## Risks and Mitigations

- **Risk:** cleanup overlaps with broader transport code shared by other services.  
  **Mitigation:** isolate metadata-specific fixes and add transport regression coverage.

## Deliverables

- This work item definition (`docs/workitems/WI-033-metadata-service-cleanup.md`).
- Recorded cleanup scope for metadata service transport/error issues.
