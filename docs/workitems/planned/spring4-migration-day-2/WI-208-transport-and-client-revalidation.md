# WI-208 — Transport + client re-validation under Boot 4

Status: `planned`  
Type: `test`  
Area: `services`, `clients`  
Backlog refs: `P-5`, `P-9`  
Depends on: WI-202, WI-206

## Goal

Re-validate service transports and client integrations under Boot 4:

- HTTP access service\n- grpc-java data plane service\n- JDBC driver testIT parity\n
## Scope

- Re-run and repair `testIT` suites for:\n  - `services/mill-data-grpc-service`\n  - `services/mill-data-http-service`\n  - `clients/mill-jdbc-driver`\n- Confirm configuration keys and property prefixes remain correct (`mill.data.services.grpc.*`).\n
## Proof commands (run on the implementation branch)

- `./gradlew :services:mill-data-http-service:test`\n- `./gradlew :services:mill-data-grpc-service:testIT`\n- `./gradlew :clients:mill-jdbc-driver:testIT`\n
## Acceptance Criteria

- gRPC + HTTP services are green in unit/integration tests.\n- JDBC driver testIT is green and still exercises the intended transports.\n
