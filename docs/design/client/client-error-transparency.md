# Client error transparency (data plane)

Normative design note for **structured failure surfaces** on the Mill **data lane** (gRPC and Jet
HTTP under `/services/jet`): how the server encodes errors, and how **mill-py** and the **JDBC driver**
surface them to callers. Work item context: `docs/workitems/completed/20260429-client-error-transparency/WI-013.md`.

## Goals

- Operators and tooling (SQL clients, scripts) see **actionable text**, not only `500` / `INTERNAL`.
- **Correlation**: `traceId` (HTTP) and equivalent **gRPC trailing metadata** (e.g. `x-trace-id`) are
  preserved when present.
- **Parity**: Python and JDBC map the same logical fields where the wire format allows.
- **Backward compatibility**: legacy Spring Boot JSON envelopes and older `{ "code", "message" }`
  bodies remain parseable during rollout.

## Server: HTTP (`application/problem+json`)

Central handler: `services/mill-data-http-service/.../access/http/advice/AccessServiceProblemAdvice.java`
(`@RestControllerAdvice` on `AccessServiceController`).

- **Content type**: `application/problem+json`.
- **RFC 9457 Problem Details** fields include at least `type`, `title`, `status`, `detail`, plus
  extension properties for **trace correlation** and **legacy** clients:
  - `traceId` (also duplicated under `properties.traceId` when Spring nests custom properties).
  - `code` and `message` mirroring gRPC status semantics for clients that still expect the older
    map-shaped JSON from the controller-era handler.

**Payload / parse errors** (`MillRuntimeException` from JSON↔protobuf bridging) map to **400** with a
stable `urn:mill:error:payload` problem type. **Unexpected failures** map to **500** with a generic
client-safe `detail`; full stack traces stay in server logs.

`AccessServiceProblemAdvice` is registered alongside `AccessServiceController` and `MessageHelper`;
requests flow through `DataOperationDispatcher` on success paths.

## Server: gRPC

Existing advice: `services/mill-data-grpc-service/.../MillGrpcServiceExceptionAdvice.java` (status
mapping, `StatusRuntimeException` pass-through). Structured **Google RPC** status details binaries are
not required for the Python/JDBC parity described here; clients rely on **status code + description +
trailing metadata** for correlation.

Services that emit tracing should attach **string** trailing metadata keys such as `x-trace-id` or
`x-correlation-id` so clients can populate the same conceptual `trace_id` field as HTTP.

## Python client (`mill-py`)

Module: `clients/mill-py/mill/exceptions.py`.

- **HTTP** (`raise_for_status` → `_from_http_status` → `_parse_http_body`):
  - Parses **Problem Details** (`detail`, `title`, `type`, numeric `status`, `traceId`) and nested
    `properties` when present.
  - Retains legacy keys (`message`, `error`, `path`, timestamp, Mill `mill_status` / `mill_details`).
  - `MillQueryError` carries `problem_*`, `trace_id`, and related HTTP context; **`MillAuthError`** and
    generic **`MillError`** also receive **`trace_id`** when the payload includes it (e.g. 401/403).

- **gRPC** (`_from_grpc_error`):
  - Reads **`trailing_metadata()`** when available; builds a lowercase string map (`grpc_trailing_metadata`).
  - Resolves **`trace_id`** from `x-trace-id`, `trace-id`, `traceid`, `x-request-id`,
    `x-correlation-id` (first match).
  - User-facing **`str(exception)`** prefers RPC **details**, then a `gRPC {STATUS}` fallback, then
    appends `[traceId=…]` when not already present in the text (aligned with JDBC message shaping).
  - **`MillQueryError`** sets `legacy_grpc_code` / `legacy_grpc_message` from gRPC for diagnostics.

Base **`MillError.as_dict()`** includes `trace_id` and `grpc_trailing_metadata`; **`MillQueryError.as_dict()`**
extends with HTTP-shaped fields.

## JDBC driver (`mill-jdbc-driver`)

Type: `clients/mill-jdbc-driver/src/main/java/io/qpointz/mill/client/HttpMillErrorBodies.java` (package
internal to the client).

For non-success HTTP responses, **`HttpMillClient`** reads the **entire response body** and parses
JSON when possible: prefers **`detail`**, then legacy **`message`** / **`error`** / **`title`**, GRPC-style
 **`code`** + **`detail`**, then **`traceId`**, **`type`** URI, plain text snippet, then the useless
reason phrase last.

Failures surface as **`MillCodeException`** with a single enriched message string suitable for SQL
tools (e.g. DBeaver) via **`SQLException`**.

## Related paths

| Area | Pointer |
|------|---------|
| Work item | `docs/workitems/completed/20260429-client-error-transparency/WI-013.md` |
| Python transport | `mill/_transport/_http.py`, `mill/_transport/_grpc.py`, `mill/aio/_transport/*` |
| JDBC HTTP client | `HttpMillClient.java` |
| Data lane overview | [`mill-data-lane-onepager.md`](../platform/mill-data-lane-onepager.md) |
| Platform REST (metadata/schema) HTTP | [`mill-py-platform-http.md`](./mill-py-platform-http.md) — same **`mill-py`** parsers apply to **`httpx`** error bodies where servers return Problem Details or Spring-style JSON |
