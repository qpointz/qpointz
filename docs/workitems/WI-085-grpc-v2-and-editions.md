# WI-085 — gRPC v2 module (raw grpc-java) and Gradle editions for v1/v2

Status: `planned`  
Type: `refactoring`  
Area: `platform`, `services`, `apps`, `build`  
Backlog refs: `P-6`

## Problem Statement

The `net.devh` gRPC Spring Boot starter does not support Spring Boot 4. The existing server lives in
`services/mill-data-grpc-service` and must remain stable while a migration path is proven.

Implementing a replacement in-place would risk regressions and complicate rollback. The build also
needs a **single** gRPC implementation on the classpath at a time, while **integration testing**
must exercise both the legacy stack and the new stack in CI.

## Goal

1. Add a **single** new module `services/mill-data-grpc-service-v2` that reimplements the data
   gRPC server with **raw grpc-java** (no `net.devh`), with **Kotlin** as the default implementation
   language (`src/main/kotlin`) and **Java** for Spring configuration classes (see below), and **the
   same dependency level as v1**:
   `mill-spring-support`, `mill-data-backends`, `mill-data-autoconfigure`, Spring Security starters,
   grpc-java artifacts, etc. — everything lives in this one module (no separate Spring-free
   runtime).
2. Leave **`services/mill-data-grpc-service` (v1) unchanged** in behavior and keep the `net.devh`
   starter there until a follow-up work item removes v1.
3. Introduce **Gradle edition features** so `apps/mill-service` selects **either** v1 **or** v2 via
   `-Pedition=...`, enabling **CI matrix** runs without duplicate `BindableService` beans.

## In Scope

1. **Editions / features** ([`docs/design/build-system/gradle-editions.md`](../design/build-system/gradle-editions.md)):
   - Define features, e.g. `grpc-v1` → `:services:mill-data-grpc-service`, `grpc-v2` →
     `:services:mill-data-grpc-service-v2`.
   - Move gRPC from a hardcoded `implementation(project(":services:mill-data-grpc-service"))` in
     `apps/mill-service/build.gradle.kts` into edition feature wiring.
   - Ensure **no edition activates both** `grpc-v1` and `grpc-v2`.
   - Default edition (e.g. `minimal`) keeps current behavior by including **`grpc-v1`** only
     until product flips default to v2.
   - Add CI-oriented editions (names to be chosen), e.g. `integration-grpc-v1` and
     `integration-grpc-v2`, aligned with the existing `integration` edition (sample data/certs,
     etc.).
   - Document inspection commands (`millEditionMatrix`, `dependencies -Pedition=...`).

2. **Module `services/mill-data-grpc-service-v2`:**
   - **Align `build.gradle.kts` with v1** for non-starter dependencies: same Spring / Mill stack
     (`mill-spring-support`, `mill-data-backends`, `mill-data-autoconfigure`, Security OAuth2
     starters, Jackson where applicable). **Replace** `api(libs.bootGRPC.server)` with explicit
     grpc-java libraries only.
   - Register in `settings.gradle.kts`; `java` + **`org.jetbrains.kotlin.jvm`** (and Dokka if aligned
     with sibling services); `mill {}` / publishing like v1.
   - **No** `net.devh` in v2. Do **not** remove `bootGRPC` from `libs.versions.toml` in this WI (v1
     still needs it).
   - **Distinct Kotlin package** from v1 (e.g. `io.qpointz.mill.data.backend.grpc.v2`) for Kotlin
     sources; Java config classes live under the same logical package tree in `src/main/java` as
     needed.
   - **Documentation:** **KDoc** on all public Kotlin API; **Javadoc** on all public Java API, down to
     method and parameter level, per repository rules ([`CLAUDE.md`](../../CLAUDE.md)).
   - **Spring configuration in Java:** Implement `@Configuration`, `@ConfigurationProperties`, and other
     Spring-centric configuration types in **`src/main/java`** so the
     **`spring-boot-configuration-processor`** generates
     **`META-INF/spring-configuration-metadata.json`** automatically. Prefer **not** to define
     `@ConfigurationProperties` in Kotlin unless accompanied by a complete
     **`META-INF/additional-spring-configuration-metadata.json`** (Kotlin-only properties are easy to
     get wrong for IDE validation).
   - `@ConditionalOnService(value = "grpc", group = "data")`; `mill.data.services.grpc.*`.
   - Parity: exception mapping (v1 `MillGrpcServiceExceptionAdvice`), security (v1
     `GrpcServiceSecurityConfiguration` + `AuthenticationMethods` / `AuthenticationManager`),
     `GrpcServiceDescriptor` equivalent.
   - **No** `@SpringBootApplication` on the service class; `apps/mill-service` remains the sole
     Boot app entry.

3. **Tests** in `mill-data-grpc-service-v2`:
   - **Unit tests** (`src/test/kotlin`): Prefer **Mockito** (`mockito-core`, **`mockito-kotlin`**) to
     isolate interceptors, configuration, and service edges; avoid Spring context where a pure unit
     test suffices. Use **JUnit Jupiter**. Run via **`test`**.
   - **Integration tests** (`src/testIT/kotlin`): Use the **`testIT`** JvmTestSuite (same Gradle pattern
     as other Mill modules — `register<JvmTestSuite>("testIT")`, `boot.starter.test`, etc.). Skymill-backed
     tests run here only. Spring Boot test slice or full context as appropriate; load backend
     configuration that uses the **Skymill** dataset: canonical model **[`test/skymill.yaml`](../../test/skymill.yaml)**
     with materialized data under **[`test/datasets/skymill/`](../../test/datasets/skymill/)** (CSV/Parquet
     as generated or checked in). Reuse the same wiring pattern as
     [`data/mill-data-backends/config/test/flow-skymill.yaml`](../../data/mill-data-backends/config/test/flow-skymill.yaml)
     (path adjusted for the gRPC v2 module location) so RPCs run against real **skymill** schemas
     (e.g. `cities`, `segments`). YAML must not use legacy `grpc.server` / `grpc.client` net.devh keys.
     Run via **`testIT`** (`./gradlew :services:mill-data-grpc-service-v2:testIT`).
   - Parity coverage vs v1 module tests where applicable.

4. **Client integration parity (JDBC + Python)** — exercise **grpc-v2** from real clients, not only
   from in-process Java tests:
   - **JDBC** ([`clients/mill-jdbc-driver`](../../clients/mill-jdbc-driver)): add or extend **integration**
     tests that run against a **grpc-v2** backend (same scenarios as today for v1 / in-process: metadata,
     handshake, query paths as applicable). Use the shared in-process or test harness pattern after v2
     server bootstrap is stable; dependency on `mill-data-grpc-service-v2` at test scope where needed.
   - **Python** ([`clients/mill-py`](../../clients/mill-py)): add or extend **integration** tests (pytest)
     that call Mill over **gRPC** with **grpc-v2** (e.g. against a test `mill-service` edition or a
     dedicated test fixture process — exact wiring in implementation). Goal: parity of behavior with
     v1 for the covered RPC surface.
   - **CI:** matrix or jobs run **JDBC** and **Python** client integration suites against **grpc-v2**
     in addition to any existing v1 coverage, so regressions in wire protocol or semantics are caught
     before clients ship.

5. **Completion hygiene** (per [`RULES.md`](RULES.md)): when done, delete this file, mark **P-6**
   done in [`BACKLOG.md`](BACKLOG.md), and update [`docs/design/platform/spring4-migration-plan.md`](../design/platform/spring4-migration-plan.md) with the `services/` paths and edition model.

## Out of Scope

- Code changes inside `services/mill-data-grpc-service` (v1) beyond what is **strictly required**
  for `mill-service` edition wiring (removal of unconditional dependency from the app module).
- Removing `net.devh` / `bootGRPC` from the catalog or deleting v1 (separate work item after v2 is
  default).
- Spring Boot 4, Jackson 3, Spring AI 2 — tracked elsewhere.

## Implementation Plan (phases)

| Phase | Deliverable |
| ----- | ----------- |
| 0 | Edition features `grpc-v1` / `grpc-v2`; `mill-service` depends on gRPC only via features; default edition includes `grpc-v1`; add `integration-grpc-v1` / `integration-grpc-v2` (or equivalent) for CI |
| 1 | New `mill-data-grpc-service-v2` skeleton + `settings.gradle.kts` |
| 2 | v2 `build.gradle.kts` — **mirror v1 dependency level** except swap `bootGRPC` for grpc-java; **`spring-boot-configuration-processor`** (annotation processor) for Java config; register **`testIT`** suite + `tasks.named<Test>("testIT")` logging |
| 3 | v2 **Java** `@ConfigurationProperties` + `@Configuration` (metadata via processor); KDoc/Javadoc; no `grpc.server.*` for v2 |
| 4 | v2 server lifecycle (`SmartLifecycle`) + `NettyServerBuilder` + interceptor registration |
| 5 | v2 `BindableService` + `@Component` / `@Configuration` (no `@SpringBootApplication` on service) |
| 6 | v2 exception `ServerInterceptor` (v1 advice parity) |
| 7 | v2 security interceptors (v1 `GrpcServiceSecurityConfiguration` parity) |
| 8 | v2 **test** (unit, Mockito) + **testIT** (Skymill integration under `src/testIT/kotlin`); parity vs v1 |
| 9 | **Client IT:** JDBC + **Python** integration tests against **grpc-v2**; CI jobs/matrix; optional test fixtures |
| 10 | Full validation, BACKLOG P-6, design doc cross-links |

**Suggested order:** Phase 0 early (keep the repo buildable), then 1–8 on v2, then 9–10.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../design/platform/spring4-migration-plan.md) (Appendix A — rationale for raw grpc-java)
- [`docs/design/build-system/gradle-editions.md`](../design/build-system/gradle-editions.md)
- [`build-logic/.../MillEditionPackaging.kt`](../../build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillEditionPackaging.kt) (edition → `implementation` wiring)
- [`apps/mill-service/build.gradle.kts`](../../apps/mill-service/build.gradle.kts)
- [`services/mill-data-grpc-service/`](../../services/mill-data-grpc-service/) (v1 — reference only)
- [`test/skymill.yaml`](../../test/skymill.yaml) (Skymill dataset model); [`test/datasets/skymill/`](../../test/datasets/skymill/)
- [`data/mill-data-backends/config/test/flow-skymill.yaml`](../../data/mill-data-backends/config/test/flow-skymill.yaml) (example Calcite/flow test config for Skymill CSV paths)
- [`clients/mill-jdbc-driver/`](../../clients/mill-jdbc-driver/) · [`clients/mill-py/`](../../clients/mill-py/) (client parity testing)

## Acceptance Criteria

- `./gradlew :apps:mill-service:dependencies --configuration runtimeClasspath -Pedition=<v1-edition>`
  resolves **v1 gRPC module only**; same for **v2 edition** resolves **v2 module only** (no both).
- `./gradlew :services:mill-data-grpc-service-v2:test` and **`testIT`** pass (Skymill coverage in **testIT** only).
- CI runs **both** gRPC edition variants for agreed integration / smoke scope; **JDBC** and **Python**
  client integration tests pass against **grpc-v2** (Phase 9).
- v1 module behavior unchanged for consumers who stay on `grpc-v1` / default edition.
- **KDoc/Javadoc** complete on public production API; **Java** `@ConfigurationProperties` produce **`META-INF/spring-configuration-metadata.json`** via the configuration processor (no missing IDE metadata for `mill.data.services.grpc.*`).
