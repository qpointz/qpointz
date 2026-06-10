# REST Exception Handling Pattern

## Status

Implemented design baseline with first reference adoption in `mill-ai-v3-service`.

## Purpose

Define one reusable exception-to-HTTP mapping pattern for upcoming REST APIs so controllers stay
thin, service layers stay framework-neutral, and error payloads remain consistent across
modules.

This pattern is intended to be reused by multiple REST APIs and is already reflected in the
current `mill-ai-v3-service` implementation.

## Problem

The current repository has the beginnings of an HTTP status exception model, but it is not yet
documented as a reusable platform convention:

- [MillCodeException.java](/C:/Users/vm/wip/qpointz/qpointz/core/mill-core/src/main/java/io/qpointz/mill/MillCodeException.java)
  is a generic code exception and does not carry HTTP semantics.
- [MillStatusException.java](/C:/Users/vm/wip/qpointz/qpointz/core/mill-core/src/main/java/io/qpointz/mill/excepions/statuses/MillStatusException.java)
  is the better foundation for API-facing status failures.
- [MillStatuses.java](/C:/Users/vm/wip/qpointz/qpointz/core/mill-core/src/main/java/io/qpointz/mill/excepions/statuses/MillStatuses.java)
  exists, but currently exposes only a minimal helper surface.
- [GlobalExceptionHandler.java](/C:/Users/vm/wip/qpointz/qpointz/ai/mill-ai-v1-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/configuration/GlobalExceptionHandler.java)
  shows the mapping approach, but only in one service and only for one status.

Without a documented shared pattern, upcoming APIs are likely to diverge in:

- exception types
- status mapping
- response body shape
- controller branching
- test strategy

## Design Goals

1. Keep controllers thin.
2. Keep service-layer exceptions independent of Spring transport classes.
3. Use one reusable status exception hierarchy across REST APIs.
4. Centralize HTTP mapping in shared advice rather than per-controller logic.
5. Standardize the error response shape.
6. Make the pattern reusable for future UI and CLI consumers where the same error `code`
   remains meaningful.

## Non-Goals

- Reworking all existing services immediately.
- Introducing transport semantics directly into `MillCodeException`.
- Designing gRPC error mapping in this document.

## Recommended Pattern

### 1. Base exception layering

Use a layered exception model:

- `MillCodeException`
  Generic application/code exception base.
- `MillStatusException`
  Semantic exception for API/service failures that must map to an HTTP status.

`MillCodeException` should remain generic. HTTP semantics should live in the status-exception
layer, not in the generic base.

### 2. Status exception contract

`MillStatusException` should become the reusable public API failure contract.

It should carry, either directly or via a stable internal mapping:

- internal Mill status representation
- stable machine-readable `code`
- human-readable `message`
- optional structured `details`
- optional `cause`

This allows service code to throw semantic failures without depending on Spring-specific
exceptions such as `ResponseStatusException`.

Decision:

- core exception/status types should not depend on Spring HTTP classes
- `MillStatusException` should use an internal Mill status representation
- HTTP advice should map that internal status to Spring/web HTTP responses at the transport edge

### 3. Central factory helpers

`MillStatuses` should be the central factory/helper class used by services.

Expected helper families:

- `badRequest(...)`
- `unauthorized(...)`
- `forbidden(...)`
- `notFound(...)`
- `conflict(...)`
- `unprocessableEntity(...)`
- `tooManyRequests(...)`
- `internalError(...)`

The helper surface should stay small and predictable so all services throw exceptions in the
same way.

### 4. Shared HTTP advice

REST modules should rely on shared `@RestControllerAdvice` or a shared advice base that maps:

- `MillStatusException` -> standard error payload with matching HTTP status
- validation exceptions -> standard validation error payload
- unknown exceptions -> generic 500 payload

Controllers should not construct error `ResponseEntity` objects for normal domain failures.

Decision:

- reusable HTTP advice and the standard error payload should live in one shared web/platform
  module
- service-specific REST modules should depend on that shared transport mapping rather than
  creating independent local conventions
- `mill-service-api` is not the preferred host if it is meant to remain framework-light

Current state:

- `mill-ai-v3-service` uses the documented status-exception pattern already
- shared Spring web extraction is still deferred; local advice remains acceptable only as a
  transition step until the shared module is introduced

### 5. Standard error payload

All REST APIs should converge on one response shape:

```json
{
  "code": "chat.not_found",
  "message": "Chat not found",
  "status": 404,
  "details": null,
  "timestamp": 1710000000000
}
```

Recommended optional extensions:

- `path`
- `traceId`
- `errors` for validation field issues

## Controller Pattern

Controllers should:

- accept request DTOs
- invoke service methods
- map successful results to HTTP responses
- never own business-status branching

For planned AI chat APIs this means:

- `AiChatController` calls `ChatService`
- `ChatService` throws semantic status exceptions
- advice translates exceptions into HTTP responses

## Testing Pattern

### Controller unit tests

Controller tests should:

- mock the service
- verify request mapping
- verify response serialization
- verify status/error payload behavior through advice

### Service integration tests

Service tests should be the first real integration level and should verify:

- persistence-backed behavior
- orchestration behavior
- semantic exception throwing for failure cases

This keeps transport tests light and business integration tests meaningful.

## Why Not Use `MillCodeException` Directly For HTTP

`MillCodeException` is too generic for reusable REST status propagation because it does not
define:

- HTTP status
- stable API error code
- transport-oriented response semantics

Using it directly would either:

- force controller-specific mapping logic everywhere, or
- pollute the generic exception base with transport concerns

Both outcomes are weaker than a dedicated status-exception layer.

## Recommended Adoption Path

1. Treat this as the standard pattern for new REST APIs.
2. Expand `MillStatuses` from the current minimal surface.
3. Introduce a dedicated shared Spring web module for HTTP advice and error payload reuse.
4. Apply it in `mill-ai-v3-service`, `mill-service-common`, and
   `mill-metadata-service`.
5. Migrate older services opportunistically rather than as a mandatory big-bang refactor.

## Reuse Confirmation

The shared web-layer extraction is justified because the pattern is already expected to be used
by at least these REST modules:

- `ai/mill-ai-v3-service`
- `services/mill-service-common`
- `metadata/mill-metadata-service`

That is enough reuse pressure to prefer one shared Spring web module over repeated local advice
implementations.

## Open Questions

- Should the internal Mill status representation be a dedicated enum or a richer status object?
- What should the new shared Spring web module be named and where should it live in the module
  layout?
- Should validation failures use the same top-level payload with nested field errors?

## Related Work

- [agentic/v3-chat-service.md](/C:/Users/vm/wip/qpointz/qpointz/docs/design/agentic/v3-chat-service.md)
- [persistence/persistence-overview.md](/C:/Users/vm/wip/qpointz/qpointz/docs/design/persistence/persistence-overview.md)
