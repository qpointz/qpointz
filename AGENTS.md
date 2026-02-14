Repository Guidelines
=====================

## Project Structure & Module Organization
The repo is a Gradle multi-repo with self-contained modules. Core libraries live in `core/` (shared service code, plan helpers, vectors). Runtime services sit inside `services/` (gRPC/HTTP entry points) and `ai/` (LLM and NL-to-SQL flows). Client-facing assets are under `clients/`, while `apps/` contains demo front ends. Build logic and conventions reside in `build-logic/`. Generated protobuf stubs are committed under `proto/`. Production sources follow `src/main/java` (or `kt`), and tests mirror them under `src/test/java`. Use the module-level `gradlew` wrapper (for example, `services/gradlew`) when working inside a specific subproject.

## Build, Test, and Development Commands
- `./gradlew build` (from a module root) compiles code, runs unit tests, and assembles artifacts.
- `./gradlew test` executes JUnit suites; scope it with `:core:mill-service-core:test` to focus on one library.
- `./gradlew jacocoTestReport` (where available) produces coverage reports in `build/reports/jacoco/`.
- `./gradlew clean` removes build outputs before a fresh iteration.
- `./gradlew tasks --group application` lists runnable entry points exposed by the `io.qpointz.mill` plugin (HTTP/gRPC services).

## Coding Style & Naming Conventions
Java sources use four-space indentation, `PascalCase` for classes, and lower-snake protobuf fields. Favor Lombok annotations (`@Slf4j`, `@Getter`, `val`) instead of manual boilerplate. Spring configuration classes belong in `io.qpointz.mill.services.configuration`. Test doubles live alongside tests under `src/test/java`. Generated code stays out of manual edits—extend it rather than patching.

## Testing Guidelines
Tests rely on JUnit 5 and Mockito. Name test classes `<Subject>Test` and methods in `shouldX_whenY` style for clarity. To validate service flows, prefer Spring Boot slice tests under `services/*/src/test/java`. Keep coverage above the module’s Jacoco threshold (default 0.8) and ensure new endpoints have both happy-path and failure-path assertions. Use `./gradlew test --tests <Pattern>` for targeted runs.

## Branching Strategy
For every work item (or phase), always create a **new dedicated branch** from `origin/feature`. Run `git fetch origin && git checkout -b <branch-name> origin/feature` before starting. Never commit directly to `feature`. Never reuse a previous work-item branch—each item starts fresh from the latest `origin/feature`. Name branches descriptively (e.g. `feat/port-flow-<wi>`). Push the branch to origin when complete; the user reviews and merges into `feature`. Do not merge or push to `feature` yourself.

## Commit & Pull Request Guidelines
Follow the existing bracketed prefix style: `[feat]`, `[fix]`, `[change]`, `[docs]`, `[wip]`. Summaries should be imperative and under 72 characters. **Never** add `Co-Authored-By` or similar trailers to commit messages. PRs must describe scope, testing evidence (`./gradlew test` output), and link to Jira or GitHub issues. Include screenshots or curl snippets for API-visible changes. Rebase onto the latest main branch before requesting review.

## Security & Configuration Tips
Keep secrets out of the repo; rely on environment variables consumed by Spring configuration (`mill.security.*`). When altering authorization facets, update policies in `core/mill-service-core/src/main/resources` and mention migrations in the PR description. Clean up temporary datasets under `dumper.py` outputs before pushing.

## Module Notes
- `services/mill-grinder-ui`: React + TypeScript + Vite single-page app that uses Mantine’s `AppShell` and React Router to expose chat, data-model explorer, and concepts flows (see `src/App.tsx`). Generated Mill REST clients live under `src/api/mill/` (OpenAPI artifacts are committed), so avoid manual edits there. The README is still the stock Vite template—remember to document Mill-specific scripts if you touch the module.
- `ai/`: Stand-alone Gradle build (`ai/gradlew`) that aggregates `mill-ai-core` (NL-to-SQL chat core) and `mill-ai-nlsql-chat-service` (Spring Boot service wrapping the core). Root tasks `test`/`testIT` just depend on subproject tasks. `mill-ai-core` exposes chat abstractions plus template utilities and wires Spring AI/OpenAI dependencies into a dedicated `testIT` suite. `mill-ai-nlsql-chat-service` layers persistence (JPA/H2), WebFlux controllers, and Spring AI chat memory repositories; its integration tests spin up the entire stack. CI jobs in `ai/.gitlab-ci.yml` call `./gradlew --no-daemon --console plain test compileTestIT` (build) and `./gradlew --no-daemon --console plain testIT` (integration), so keep those tasks healthy when modifying AI code.
