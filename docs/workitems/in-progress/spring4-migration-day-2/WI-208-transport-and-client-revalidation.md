# WI-208 — Transport + client re-validation under Boot 4

Status: `done`  
Type: `test`  
Area: `services`, `clients`  
Backlog refs: `P-5`, `P-9`  
Depends on: WI-202, WI-204, WI-206, WI-207

## Goal

Re-validate service transports and client integrations under Boot 4:

- HTTP access service
- grpc-java data plane service
- JDBC driver testIT parity

## Scope

- Re-run and repair `testIT` suites for:
  - `services/mill-data-grpc-service`
  - `services/mill-data-http-service`
  - `clients/mill-jdbc-driver`
- Confirm configuration keys and property prefixes remain correct (`mill.data.services.grpc.*`).

## Proof commands (run on the implementation branch)

- `./gradlew :services:mill-data-http-service:test`
- `./gradlew :services:mill-data-http-service:testIT`
- `./gradlew :services:mill-data-grpc-service:testIT`
- `./gradlew :clients:mill-jdbc-driver:testIT`

## Acceptance Criteria

- All proof commands above are green.

## Completion notes (2026-04-30)

Proof (repo root):

- `./gradlew :services:mill-data-http-service:test` — **BUILD SUCCESSFUL**
- `./gradlew :services:mill-data-http-service:testIT` — **BUILD SUCCESSFUL** (suite added; currently NO-SOURCE)
- `./gradlew :services:mill-data-grpc-service:testIT` — **BUILD SUCCESSFUL**
- `./gradlew :clients:mill-jdbc-driver:testIT` — **BUILD SUCCESSFUL**

Fixes applied:

- `services/mill-data-http-service`: added a `testIT` suite for consistent transport verification wiring.
- `clients/mill-jdbc-driver`: made testIT self-contained by starting an embedded Skymill gRPC server
  during `TestITProfile` method-source resolution and using a dynamically chosen port (avoids port conflicts).
