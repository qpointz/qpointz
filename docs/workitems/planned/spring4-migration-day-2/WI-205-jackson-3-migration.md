# WI-205 — Jackson 3 migration (tools.jackson + JsonMapper)

Status: `planned`  
Type: `refactoring`  
Area: `platform`  
Backlog refs: `P-7`  
Depends on: WI-202

## Goal

Migrate explicit Jackson dependencies and source usages from Jackson 2 → **Jackson 3** (Boot 4 line):

- Maven group move: `com.fasterxml.jackson` → `tools.jackson`\n- Package move: `com.fasterxml.jackson.*` → `tools.jackson.*` (annotations stay `com.fasterxml.jackson.annotation`)\n- Mechanical API shifts (`ObjectMapper` → `JsonMapper`, exception types)\n
## Inputs

- Inventory: [`docs/design/platform/spring4-boot4-jump-start-inventory.md`](../../../design/platform/spring4-boot4-jump-start-inventory.md)\n
## Scope

- Update `libs.versions.toml` Jackson entries to Jackson 3 coordinates.\n- Refactor code and tests to match Jackson 3.\n- Address behavior changes that can break tests (date timestamps, null primitives, property ordering).\n
## Proof commands (run on the implementation branch)

- Start tight:\n  - `./gradlew :core:mill-core:test`\n  - `./gradlew :security:mill-security:test`\n  - `./gradlew :metadata:mill-metadata-core:test`\n- Then broaden:\n  - `./gradlew test`\n
## Acceptance Criteria

- No remaining compile-time dependency on Jackson 2 artifacts in the version catalog.\n- `./gradlew test` is green (or any remaining failures are routed to their owning WI).\n
