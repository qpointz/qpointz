# WI-010: Clean Spring / Pure Module Separation

**Type:** refactoring
**Priority:** medium
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `refactor/wi-010-spring-pure-module-separation`

---

## Goal

Enforce a clean boundary between Spring-dependent modules (service layer,
auto-configurations) and pure domain/library modules. Pure modules must have
zero Spring imports in main sources and zero Spring dependencies in their
Gradle build files.

---

## Current State

The codebase is already ~90% separated. Eight modules have no Spring imports
in `src/main`. The remaining issues are structural and build-level:

### Clean modules (no Spring in main sources)

| Module | Lang |
|--------|------|
| `core/mill-core` | Java |
| `core/mill-security` | Java |
| `data/mill-data-backend-core` | Java |
| `data/mill-data-backends` | Java |
| `data/mill-data-source-core` | Kotlin |
| `data/mill-data-source-calcite` | Kotlin |
| `data/mill-data-testkit` | Kotlin |
| `metadata/mill-metadata-core` | Java |

### Intentionally Spring (correct by design)

| Module | Role |
|--------|------|
| `core/mill-service-api` | Conditional annotations, SpringUtils |
| `core/mill-service-security` | Spring Security wiring |
| `core/mill-well-known-service` | Spring MVC controllers |
| `core/mill-test-kit` | Spring test infrastructure |
| `data/mill-data-autoconfigure` | Spring auto-configuration |
| `metadata/mill-metadata-autoconfigure` | Spring auto-configuration |
| `metadata/mill-metadata-service` | Spring MVC controllers |
| `data/services/*` | HTTP/gRPC entry points |
| `ai/mill-ai-v1-nlsql-chat-service` | JPA, WebFlux, chat service |

### AI modules — mixed (Spring AI + Spring Boot in "core" libraries)

The AI modules present a distinct pattern. Spring AI types (`ChatClient`,
`ChatModel`, `Message`, `VectorStore`, `ToolCallback`, `Advisor`) are the
domain framework — they ARE the abstractions that AI code is built on. These
cannot be separated without rewriting everything. However, Spring Boot
wiring (`@Configuration`, `@Component`, `@ConfigurationProperties`, `@Bean`)
is mixed into the same modules that contain domain logic.

| Module | Spring AI (domain) | Spring Boot (wiring) | Notes |
|--------|-------------------|---------------------|-------|
| `mill-ai-v1-core` | ~25 files | ~3 files (`Nl2SqlConfiguration`, `ValueMappingConfiguration`, `ValueMappingComponents`) | Config classes should be in autoconfigure module. Also depends on `data/mill-data-autoconfigure` (a Spring autoconfigure module). |
| `mill-ai-v2` | ~7 files | 2 files (`ChatService.kt` has `@Component`/`@Autowired`, `App.kt` has `@SpringBootApplication`) | `App.kt` is an entry point in a "core" library. `ChatService` uses `@Component`. |
| `mill-ai-v2-test` | 4 files | None | Uses Spring AI ChatClient only — acceptable for test kit. |

**Key distinction for AI:** Spring AI API types in core = acceptable (it's
the domain framework). Spring Boot annotations (`@Configuration`,
`@Component`, `@Bean`, `@SpringBootApplication`) in core = should move to
autoconfigure/service layer.

---

## Problems

### 1. `SecurityProvider` interface in wrong module

`SecurityProvider` (a pure interface: `getPrincipalName()`, `authorities()`)
and its null-object `NoneSecurityProvider` live in `data/mill-data-backend-core`.
The Spring Security implementation `SecurityContextSecurityProvider` lives in
`core/mill-service-security`, which therefore depends on
`data/mill-data-backend-core` — an inverted dependency.

**Current dependency:**
```
core/mill-service-security  ──api──>  data/mill-data-backend-core
```

**Correct dependency:**
```
core/mill-security  (owns SecurityProvider interface)
    ^                     ^
    |                     |
core/mill-service-security    data/mill-data-backend-core
(SecurityContextSecurityProvider)  (NoneSecurityProvider, SecurityDispatcherImpl)
```

**Files involved:**

| File | Current Location | Action |
|------|-----------------|--------|
| `SecurityProvider.java` | `data/mill-data-backend-core` | Move to `core/mill-security` |
| `NoneSecurityProvider.java` | `data/mill-data-backend-core` | Move to `core/mill-security` |
| `SecurityContextSecurityProvider.java` | `core/mill-service-security` | Update import |
| `SecurityDispatcherImpl.java` | `data/mill-data-backend-core` | Update import |
| `DefaultServiceConfiguration.java` | `data/mill-data-autoconfigure` | Update import |
| `BackendContextRunner.kt` | `data/mill-data-testkit` | Update import |
| `CalciteBackendContextRunner.kt` | `data/mill-data-testkit` | Update import |
| `JdbcBackendContextRunner.kt` | `data/mill-data-testkit` | Update import |
| `FlowBackendContextRunner.kt` | `data/mill-data-testkit` | Update import |
| `MillGrpcService.java` | `data/services/mill-data-grpc-service` | Update import |

After this, remove `api(project(":data:mill-data-backend-core"))` from
`core/mill-service-security/build.gradle.kts`.

### 2. `data/mill-data-autoconfigure` depends on metadata modules without using them

`data/mill-data-autoconfigure/build.gradle.kts` declares:

```kotlin
api(project(":metadata:mill-metadata-core"))
api(project(":metadata:mill-metadata-autoconfigure"))
```

Zero files in `data/mill-data-autoconfigure/src/main` import anything from
`io.qpointz.mill.metadata`. This is pure transitive classpath wiring that
causes metadata `@Configuration` classes to leak into data-layer tests.

**Action:** Remove both metadata dependencies. If downstream consumers need
metadata on the classpath, they should declare it themselves.

### 3. `spring-dependency-management` plugin in pure modules

Two pure modules apply the Spring dependency management Gradle plugin solely
for BOM version resolution:

| Module | Has Spring in main sources? |
|--------|---------------------------|
| `core/mill-core` | No |
| `data/mill-data-backends` | No |

**Action:** Replace with Gradle's `platform()` mechanism or manage versions
through the version catalog (`libs.versions.toml`) only. Alternatively, if the
mill convention plugin already applies the BOM, simply remove the redundant
plugin application.

### 4. `boot.starter.test` in pure module tests

`core/mill-core` (a pure protobuf/gRPC library) pulls in `boot.starter.test`
in its test dependencies. This drags the entire Spring Boot test framework
into a module that should only need JUnit + Mockito.

**Action:** Remove `boot.starter.test` from `core/mill-core` test
dependencies. Replace with direct JUnit/Mockito/AssertJ dependencies.

### 5. `mill-ai-v1-core` mixes Spring Boot config into core library

`mill-ai-v1-core` is described as "NL2SQL core library" but contains Spring
Boot `@Configuration` classes in its main sources:

- `Nl2SqlConfiguration.java` — `@Configuration`, `@Bean`, `@Value`, `@Qualifier`
- `ValueMappingConfiguration.java` — `@Configuration`, `@ConfigurationProperties`
- `ValueMappingComponents.java` — `@Component`, `@Autowired`, `@EventListener`

Additionally, `mill-ai-v1-core` depends on `data/mill-data-autoconfigure`
(a Spring autoconfigure module) rather than just `data/mill-data-backend-core`.

> **Note:** `ai-v1-*` modules are being replaced by `ai-v2`. Apply minimal
> changes only — document the issues in the module inventory but do not
> invest in extracting configs or restructuring. Focus refactoring effort on
> `ai-v2` instead.

### 6. `mill-ai-v2` has `@SpringBootApplication` and `@Component` in core

- `App.kt` — `@SpringBootApplication`, `ApplicationRunner` — this is an entry
  point, not a library class.
- `ChatService.kt` — `@Component`, `@Autowired` — Spring-managed bean in a
  "core" module.

### 7. `MainLala.java` dead scaffolding

`core/mill-test-kit/src/main/java/io/qpointz/mill/test/services/MainLala.java`
is a `@SpringBootApplication` with `@ComponentScan("io.qpointz")`. It's unused
and risks being picked up as a configuration source by any test with a broad
scan.

**Action:** Delete it. (Also covered by WI-009, but listed here for
completeness since it's part of the pure/Spring separation story.)

---

## Pre-Implementation Note

This analysis was performed on the `plan/work-items` branch. Additional
structural changes are expected to land before this work item is picked up.
**Re-run the dependency and import analysis before starting implementation** —
the list of affected files, dependency edges, and pure/Spring module
classification may have shifted.

---

## Steps

1. **Move `SecurityProvider` and `NoneSecurityProvider`** from
   `data/mill-data-backend-core` to `core/mill-security`
   - Update package from `io.qpointz.mill.data.backend` to
     `io.qpointz.mill.security` (or keep original package and just move the
     file — decide based on whether the interface is conceptually security or
     data)
   - Update all imports in consumers (~10 files)
   - Remove `api(project(":data:mill-data-backend-core"))` from
     `core/mill-service-security/build.gradle.kts`

2. **Remove metadata dependencies from `data/mill-data-autoconfigure`**
   - Delete `api(project(":metadata:mill-metadata-core"))` and
     `api(project(":metadata:mill-metadata-autoconfigure"))`
   - Verify no compilation failures; add metadata dependencies to any
     downstream module that actually needs them

3. **Remove `spring-dependency-management` plugin from pure modules**
   - `core/mill-core/build.gradle.kts` — remove plugin
   - `data/mill-data-backends/build.gradle.kts` — remove plugin
   - Ensure version resolution still works via version catalog or convention
     plugin

4. **Remove `boot.starter.test` from `core/mill-core` test dependencies**
   - Replace with direct `junit-jupiter`, `mockito-core`,
     `mockito-junit-jupiter` dependencies (most are already there)

5. **`mill-ai-v1-*` — minimal changes (being replaced by v2)**
   - Do NOT restructure or extract configs
   - Document current state in the module inventory for reference
   - Any cleanup happens naturally when v1 is removed

6. **Extract Spring Boot entry point from `mill-ai-v2`**
   - Move `App.kt` (`@SpringBootApplication`) to a separate service module
     or an `examples/` source set
   - Evaluate whether `ChatService.kt` `@Component`/`@Autowired` can be
     replaced with constructor injection without Spring annotations (let
     the service layer do the wiring)

7. **Delete `MainLala.java`**

8. **Verify**
   - `./gradlew clean build` passes
   - No pure module has `org.springframework` imports in `src/main`
   - No pure module has `boot.*` in Gradle dependencies (main scope)
   - Dependency graph shows no core -> data reverse edges

---

## Deliverable: Module Inventory

At the end of this work item, produce a detailed module inventory document at
`docs/design/platform/module-inventory.md`. The inventory must cover every
Gradle subproject in the repository and include:

- Module path and name
- Language (Java / Kotlin / mixed)
- Category: **pure** (no Spring in main sources), **spring-service** (Spring
  by design), or **spring-ai** (Spring AI domain framework, no Spring Boot
  wiring)
- Purpose (one-line description)
- Key dependencies (which other modules it depends on)
- Spring presence: none / Spring AI only / Spring Boot annotations / Spring
  auto-configuration
- Publish status (`publishArtifacts = true/false`)
- Test infrastructure: what test frameworks/starters are used

The inventory serves as the living reference for dependency hygiene going
forward. Any future module addition or refactoring should update it.

---

## Verification

1. No pure module has `org.springframework` imports in `src/main` (except
   Spring AI types in AI core modules, which are the domain framework).
2. No pure module has Spring Boot annotations (`@Configuration`, `@Component`,
   `@Bean`, `@SpringBootApplication`) in `src/main`.
3. `core/mill-service-security/build.gradle.kts` no longer depends on
   `data/mill-data-backend-core`.
4. `data/mill-data-autoconfigure/build.gradle.kts` has no metadata project
   references.
5. `core/mill-core/build.gradle.kts` has no `spring-dependency-management`
   plugin and no `boot.starter.test` in test deps.
6. `MainLala.java` deleted.
7. `mill-ai-v1-*` issues documented in module inventory (no code changes).
8. `mill-ai-v2` has no `@SpringBootApplication` in main sources.
9. `./gradlew clean build` passes across all modules.
10. `docs/design/platform/module-inventory.md` exists and covers every
    Gradle subproject with accurate dependency and Spring classification.

## Estimated Effort

Medium. The `SecurityProvider` move and AI config extraction are the most
impactful changes. Gradle cleanup is mechanical. The AI work may require a
new autoconfigure module if the configs can't simply move to the existing
chat-service module.
