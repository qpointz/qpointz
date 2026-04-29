# WI-202 — Bump Spring Boot to 4.0.6 and fix immediate breakage

Status: `planned`  
Type: `refactoring`  
Area: `platform`, `build`  
Backlog refs: `P-5`  
Depends on: WI-201

## Goal

Move `springBoot` in the version catalog to **4.0.6** and restore compilation/tests enough that
subsequent WIs can proceed.

## Scope

- Update `springBoot` in `libs.versions.toml`.
- Fix first-wave compilation and dependency-resolution failures (expected after a major BOM bump).
- Keep changes minimal and mechanical; defer deeper refactors (Jackson 3, Security 7) to their WIs.

## Proof commands (run on the implementation branch)

- Fast signal:\n  - `./gradlew :apps:mill-service:compileJava` (or Kotlin compile tasks in affected modules)\n  - `./gradlew :services:mill-data-grpc-service:compileJava :services:mill-data-grpc-service:compileKotlin`\n- Then:\n  - `./gradlew test` (expect failures; capture and route to downstream WIs)\n
## Acceptance Criteria

- Repo compiles far enough to run targeted tests.\n- No “version selection churn”: Boot stays pinned at **4.0.6** for the story.\n
## Notes

If Gradle resolution requires explicit BOM import in modules, prefer the existing pattern used for
`boot-dependencies` alignment (introduced during WI-099 pre-migration cleanup).

