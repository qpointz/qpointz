# WI-208 — Transport + client re-validation under Boot 4

Status: `planned`  
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
