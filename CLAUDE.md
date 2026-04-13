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
- Capability discovery uses Java `ServiceLoader` (`META-INF/services/io.qpointz.mill.ai.core.capability.CapabilityProvider`)
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
- **Persistence contract purity**: interfaces and domain types in contract modules (e.g.
  `mill-security`, future `mill-*-api` modules) must be free of any persistence-framework
  annotations (`@Entity`, `@Document`, `@Column`, etc.). Persistence implementations (JPA, MongoDB,
  etc.) map their internal entity/document types to the shared domain types before returning them.
  This rule enables swapping the persistence backend without touching the contract layer or any
  module that depends on it. **Contract interfaces must never return or accept persistence entity
  classes directly — only pure domain types.**
- **Documentation**: all production code must carry JavaDoc (Java) or KDoc (Kotlin) down to method
  and parameter level. This includes entities, repositories, services, configuration classes,
  controllers, and DTOs. Test classes and test methods are exempt; public test-utility helpers
  should be documented. Generated code (protobuf stubs, OpenAPI clients) is exempt.
- **`@ConfigurationProperties` metadata**: any autoconfigure module that defines
  `@ConfigurationProperties`-bound classes must ensure IDE and tooling metadata is generated.
  Two compliant approaches:
  - Implement the properties class in **Java** — the `spring-boot-configuration-processor`
    annotation processor generates `META-INF/spring-configuration-metadata.json` automatically.
  - Implement in **Kotlin** — but provide
    `META-INF/additional-spring-configuration-metadata.json` manually alongside the class.
  Kotlin without explicit metadata silently breaks IDE autocomplete and property validation for
  all `mill.*` properties in that module.

## Testing Structure

Every new Gradle module must configure both unit and integration test suites following the
`mill-ai-v3-persistence` pattern in `build.gradle.kts`:

```kotlin
testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                runtimeOnly(libs.h2.database)
            }
        }
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                }
            }
        }
    }
}
tasks.named<Test>("testIT") {
    testLogging { events("passed", "failed", "skipped") }
}
```

- Unit tests (`src/test/`): pure logic, no Spring context, no DB — fast
- Integration tests (`src/testIT/`): Spring Boot test slice or full context, H2 in-memory DB
- Run: `./gradlew :module:test` (unit) and `./gradlew :module:testIT` (integration)

## Stories, Work Items & Branching

Work is organised into **stories**. A story is a coherent delivery unit that maps to one Git branch
merged into `dev` by the user.

### Story folder layout (`planned/` / `in-progress/` / `completed/`)

Active stories live under **`docs/workitems/planned/<story-slug>/`** until at least one WI is checked
off, then under **`docs/workitems/in-progress/<story-slug>/`**. Closed stories are archived under
**`docs/workitems/completed/YYYYMMDD-<story-slug>/`**. The `docs/workitems/` root holds only
`RULES.md`, `BACKLOG.md`, `MILESTONE.md`, and **`releases/`** — not loose story folders.

```
docs/workitems/
  RULES.md
  BACKLOG.md
  MILESTONE.md
  releases/
  planned/<story-slug>/
    STORY.md
    WI-NNN-<title>.md
  in-progress/<story-slug>/
    STORY.md
    WI-NNN-<title>.md
  completed/YYYYMMDD-<story-slug>/
    STORY.md
    WI-NNN-<title>.md
```

- **Checkbox rule:** all WIs unchecked → **`planned/`**; any WI `[x]` → **`in-progress/`** (move the
  whole folder when the first checkbox is marked). Full detail: **`docs/workitems/RULES.md`** →
  **Placement rule (checkbox-based)**.
- Folder name: lowercase hyphen-separated slug of the story topic.
- `STORY.md` must contain a short goal description and an ordered checkbox list of all WIs — this
  checklist is the story **tracking list** for the branch. **After each WI is completed:** set its
  box to `[x]`, update its `WI-*.md` when the story requires it, and **commit** (one commit per WI;
  see **Branching & Commits**). Keep the tracking list and Git history in step; do not batch several
  finished WIs without updating `STORY.md` or without committing.
- WI files live inside the story folder under **`planned/`** or **`in-progress/`** — **not** at the
  top level of `docs/workitems/`.

### Story closure (before branch is merge-ready)

0. **MR-ready history** — replay/squash all commits from the story branch **since its merge base**
   (usually `origin/dev`, but match the branch you actually branched from / will target). **Group
   commits logically by what changed** (not only by WI count); aim for a **small, readable** set of
   commits (**~10 or fewer** is the usual target — flexible for large stories). Then open the MR. Full
   detail: `docs/workitems/RULES.md` → **Completion (Story level)**.
1. Update `docs/workitems/MILESTONE.md` — add completed WIs to the appropriate milestone.
2. Update `docs/workitems/BACKLOG.md` — set related rows to **`done`**; add deferred follow-ups as
   needed. **Row removal** happens at **release housekeeping**, not at story closure (`RULES.md` §
   **Release (version) process**).
3. Update / create design docs under the relevant `docs/design/<component>/` section
   (e.g. `agentic/`, `metadata/`, `platform/`). Design docs are filed by logical component,
   not by story.
4. Update / create user docs under `docs/public/src/`.
5. **Archive the story folder** — move (do not delete) from `planned/<story-slug>/` or
   `in-progress/<story-slug>/` to
   `docs/workitems/completed/YYYYMMDD-<story-slug>/` (closure date + original slug). Preserves
   STORY and WI history; MILESTONE, `releases/`, BACKLOG (`done` until next release), and design/public
   docs remain the summary record.
   **Most recent first:** sort archived folder names **descending**, or use
   `docs/workitems/completed/README.md`.

Full rules: `docs/workitems/RULES.md`.

### Branching & Commits

- Branch **usually** from `origin/dev`; rebase against that (or your MR target) before pushing. If the
  story started from another integration branch, use that consistently at closure when squashing.
- All WIs within a story share the **same branch**; no sub-branches per WI unless there is an
  explicit dependency on unmerged prior work.
- Never commit directly to `dev` or `main`.
- One logical commit per WI (squash at WI completion).
- **After each WI is complete (story implementation):** (1) **Update the tracking list** in
  `docs/workitems/planned/<story>/STORY.md` or `docs/workitems/in-progress/<story>/STORY.md` (mark
  that WI `[x]`; **move** `planned/…` → `in-progress/…` on the first `[x]`). (2) Update the corresponding
  `WI-NNN-*.md` if the WI or story template expects notes or status there. (3) **Commit** the
  **full working copy** for that WI — every intentional file touched (code, tests, story tracking,
  WI markdown, etc.) in **one** commit so the tree is **clean** before starting the next WI. Do not
  leave a finished WI unchecked or uncommitted. Exclude build artifacts, secrets, and unrelated
  changes outside the story.
- **At story closure**: squash and **regroup by change content** (coherent themes, not necessarily
  one commit per WI) so the branch is **MR-ready**; **~10 commits** above the merge base is a soft
  maximum. Use interactive rebase against the real merge target (often `origin/dev`). See
  `docs/workitems/RULES.md` → **Completion (Story level)**.
- Commit prefix style: `[feat]`, `[fix]`, `[change]`, `[docs]`, `[wip]`; imperative, under 72 chars.
- **Never** add `Co-Authored-By` trailers to commit messages.
- Do not force-push to protected branches.

Full detail: `docs/workitems/RULES.md` (Commits → **Complete working copy per WI**).
