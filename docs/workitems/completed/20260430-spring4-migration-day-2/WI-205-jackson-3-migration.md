# WI-205 — Jackson 3 migration (tools.jackson + JsonMapper)

Status: `done` (on branch `feat/spring-4-migration`, 2026-04-30)  
Type: `refactoring`  
Area: `platform`  
Backlog refs: `P-7`  
Depends on: WI-202

## Goal

Migrate explicit Jackson dependencies and source usages from Jackson 2 → **Jackson 3** (Boot 4 line):

- Maven group move: `com.fasterxml.jackson` → `tools.jackson`
- Package move: `com.fasterxml.jackson.*` → `tools.jackson.*` (annotations stay `com.fasterxml.jackson.annotation`)
- Mechanical API shifts (`ObjectMapper` → `JsonMapper`, exception types)

## Inputs

- Inventory: [`docs/design/platform/spring4-boot4-jump-start-inventory.md`](../../../design/platform/spring4-boot4-jump-start-inventory.md)

## Scope

- Update `libs.versions.toml` Jackson entries to Jackson 3 coordinates.
- Refactor code and tests to match Jackson 3.
- Address behavior changes that can break tests (date timestamps, null primitives, property ordering).

## Proof commands (run on the implementation branch)

- Start tight:
  - `./gradlew :core:mill-core:test`
  - `./gradlew :security:mill-security:test`
  - `./gradlew :metadata:mill-metadata-core:test`
- Then broaden:
  - `./gradlew test`

## Acceptance Criteria

- `./gradlew test` is green.
- No compile-time dependency on Jackson 2 artifacts remains in `libs.versions.toml` (Jackson 3 coordinates only).

## Completion notes (2026-04-30)

- **`./gradlew compileJava compileKotlin`** and **`./gradlew test`** were run successfully on the implementation branch.
- **`JsonPolicyImporter` / `YamlPolicyImporter`** enable **`MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS`** so policy fixtures using uppercase enum literals (for example `ALLOW`) still deserialize against lowercase Java enum constants.
- **`BACKLOG.md` P-7** marked **`done`**; design plan section 3 (Jackson 3) updated with branch status.
