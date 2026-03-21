# WI-084 - AI v3 Chat Service Documentation

Status: `planned`  
Type: `documentation`  
Area: `ai`, `docs`  
Backlog refs: `TBD`

## Problem Statement

The AI v3 chat service work spans persistence, runtime reconstruction, SSE streaming,
controller/service boundaries, REST exception handling, and `mill-ui` integration. Without a
dedicated final documentation step, the resulting behavior is likely to remain fragmented across
work items, code, and partial design notes.

## Goal

Produce complete documentation for the implemented AI v3 chat service after the core work items
are delivered.

## In Scope

1. Document the final module responsibilities across:
   - `mill-ai-v3`
   - `mill-ai-v3-persistence`
   - `mill-ai-v3-service`
   - `persistence/mill-persistence`
2. Document the final REST API surface, including:
   - routes
   - request/response DTOs
   - error responses
   - OpenAPI coverage
3. Document chat persistence and data model decisions:
   - `ChatStore`
   - `ConversationStore`
   - `chatId == conversationId`
   - hard delete and cascade semantics
4. Document runtime rehydration and contextual chat behavior.
5. Document the SSE stream contract and item-based event model.
6. Document frontend integration expectations for `mill-ui`.
7. Add implementation-oriented references for testing and future maintenance.

## Out of Scope

- Reopening core design decisions already closed in prior WIs.
- Large new feature additions during the documentation phase.

## Dependencies

- [Agentic Runtime v3 - Chat Service and API](/C:/Users/vm/wip/qpointz/qpointz/docs/design/agentic/v3-chat-service.md)
- [REST Exception Handling Pattern](/C:/Users/vm/wip/qpointz/qpointz/docs/design/platform/rest-exception-handling-pattern.md)
- [WI-082 - mill-ui Unified AI Chat Integration](/C:/Users/vm/wip/qpointz/qpointz/docs/workitems/WI-082-mill-ui-unified-ai-chat-integration.md)

## Implementation Plan

1. **Architecture summary**
   - Capture the final module split and ownership boundaries.
2. **API documentation**
   - Consolidate the final REST contract and error model.
3. **Persistence and runtime**
   - Document metadata persistence, transcript persistence, and runtime reconstruction.
4. **Streaming**
   - Document the final SSE event envelope and consumer semantics.
5. **Frontend guidance**
   - Document how `mill-ui` is expected to consume the service.
6. **Maintenance guidance**
   - Add testing and extension notes for future chat capabilities.

## Acceptance Criteria

- The implemented AI v3 chat service is documented end-to-end in a coherent location.
- A new engineer can understand:
  - module boundaries
  - REST API usage
  - persistence model
  - error handling
  - SSE streaming model
  - frontend integration expectations
- Documentation references the final implementation rather than only planning artifacts.

## Deliverables

- This work item definition (`docs/workitems/WI-084-ai-v3-chat-service-documentation.md`).
- Final consolidated AI v3 chat service documentation in `docs/design` and/or module docs.
