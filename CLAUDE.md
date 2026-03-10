# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Mill** is a data integration platform providing unified SQL querying across multiple data formats (CSV, Parquet, Avro, JDBC), a RESTful service layer, JDBC drivers, a Python client, and an AI-powered chat interface for natural language data exploration.

## Build Commands

```bash
# From repo root or any module root
./gradlew build              # Compile, run unit tests, assemble artifacts
./gradlew test               # All unit tests (aggregated)
./gradlew testIT             # All integration tests
./gradlew clean              # Remove build outputs
./gradlew jacocoTestReport   # Coverage reports in build/reports/jacoco/
./gradlew tasks --group application  # List runnable entry points

# Target a specific module
./gradlew :core:mill-core:test
./gradlew :data:mill-data-backends:testIT

# AI modules have a separate Gradle wrapper
cd ai && ./gradlew test
cd ai && ./gradlew testIT

# Run a single test or pattern
./gradlew test --tests "io.qpointz.mill.SomeTest"
./gradlew test --tests "io.qpointz.mill.SomeTest.shouldX*"
```

**Makefile shortcuts:**
```bash
make build              # Full Gradle build
make test               # All tests
make clean              # Clean outputs
make ai-test            # AI module tests only
make svc-build          # Service + Docker image
make maven-local-publish # Publish to Maven local
make check-tools        # Verify required dev tools
```

**UI (React/Vite):**
```bash
cd ui/mill-grinder-ui
npm install && npm run dev    # Dev server with HMR
npm run build                 # Production build
npm run test                  # Vitest suite
npm run lint                  # ESLint
```

**Python regression CLI:**
```bash
cd apps/mill-regression-cli
poetry install
pytest
```

**Docs:**
```bash
make docs-build    # Build API docs + MkDocs site
make docs-serve    # Serve locally at localhost:8000
```

## Architecture

The repo is a Gradle multi-module build with 66 included modules (`settings.gradle.kts`). Dependency versions are centralized in `libs.versions.toml`. Shared build conventions live in `build-logic/` via the custom plugin `io.qpointz.plugins.mill`.

**Key module groups:**
- `core/` — Shared libraries: interfaces, Protobuf stubs, security/RBAC, Spring integration, test utilities
- `data/` — Data backends (Calcite, JDBC, Flow), format handlers (Parquet, Avro, Arrow, Excel, CSV), gRPC/HTTP data services, Spring auto-configuration
- `ai/` — **Standalone Gradle build** (`ai/gradlew`). V1/V2 NL-to-SQL stacks (Spring AI) plus the new **V3 agentic runtime** (LangChain4j, framework-free core). CI calls `./gradlew test compileTestIT` (build) and `./gradlew testIT` (integration)
- `metadata/` — Metadata service and auto-configuration
- `clients/` — JDBC driver and interactive JDBC shell
- `apps/mill-service` — Main Spring Boot application entry point
- `ui/mill-grinder-ui` — React 19 + TypeScript + Vite SPA using Mantine AppShell. Generated REST clients under `src/api/mill/` are OpenAPI artifacts — do not edit them manually
- `proto/` — Protocol Buffer definitions (Protobuf stubs are committed and auto-compiled by `com.google.protobuf` Gradle plugin; extend rather than patch generated code)

**Key frameworks:** Spring Boot 3.5, Apache Calcite 1.41, Substrait 0.60, gRPC 1.79, Spring AI 1.1, LangChain4j 1.11, Java 21.

### AI v3 agentic runtime

V3 is a Kotlin-only, Spring-free agentic runtime built alongside v1/v2. Key modules:

| Module | Role |
|--------|------|
| `ai/mill-ai-v3-core` | `AgentEvent`, `Capability`, `CapabilityRegistry`, `AgentProfile`, `RunState` — no framework deps |
| `ai/mill-ai-v3-capabilities` | `ConversationCapability`, `DemoCapability` — discovered via `ServiceLoader` |
| `ai/mill-ai-v3-langchain4j` | `OpenAiHelloWorldAgent` — LangChain4j adapter, only integration module |
| `ai/mill-ai-v3-test` | Integration test scenarios |
| `ai/mill-ai-v3-cli` | Interactive REPL for manual testing (`./gradlew :ai:mill-ai-v3-cli:run`) |

**Running the interactive CLI:**
```bash
OPENAI_API_KEY=sk-...  ./gradlew :ai:mill-ai-v3-cli:run --console=plain
# Optional: OPENAI_MODEL (default gpt-4o-mini), OPENAI_BASE_URL
```

**Key v3 design rules:**
- Core types (`AgentEvent`, `Capability`, etc.) live in `mill-ai-v3-core` with no LangChain4j or Spring imports
- LangChain4j is confined to `mill-ai-v3-langchain4j`; it must not define the core runtime architecture
- New `AgentEvent` subtypes are added to `mill-ai-v3-core`; the CLI renders them automatically via JSON serialization — no CLI changes needed
- Capability discovery uses Java `ServiceLoader` (`META-INF/services/io.qpointz.mill.ai.CapabilityProvider`)
- Design documents: `docs/design/agentic/`

## Testing Guidelines

- Test classes: `<Subject>Test`; methods: `shouldX_whenY` style
- Unit tests: `src/test/java(kt)/`; Integration tests: `src/testIT/java(kt)/` (JvmTestSuite)
- Prefer Spring Boot slice tests for service flows
- Jacoco coverage threshold: 0.8 (default); new endpoints need happy-path + failure-path assertions

## Code Conventions

- Java/Kotlin: 4-space indentation, `PascalCase` classes, lower-snake protobuf fields
- Favor Lombok annotations (`@Slf4j`, `@Getter`, `val`) over manual boilerplate
- Spring config classes belong in `io.qpointz.mill.services.configuration`
- Test doubles live alongside tests in `src/test/java`
- Keep secrets out of the repo; use environment variables consumed by Spring config (`mill.security.*`)

## Branching & Commits

- Branch from `origin/dev`; rebase against `origin/dev` before pushing
- Never commit directly to `dev` or `main`; each work item gets a fresh branch
- Commit message prefix style: `[feat]`, `[fix]`, `[change]`, `[docs]`, `[wip]`; imperative, under 72 chars
- **Never** add `Co-Authored-By` trailers to commit messages
- Do not force-push to protected branches
