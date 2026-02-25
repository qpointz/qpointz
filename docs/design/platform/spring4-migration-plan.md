# Spring Boot 3.5 to Spring Boot 4.0 Migration Plan

## Current State

| Component                              | Current Version      |
|----------------------------------------|----------------------|
| Spring Boot                            | 3.5.10               |
| Spring Framework                       | 6.x (managed by Boot)|
| Spring Security                        | 6.x (test hardcoded at 6.5.7) |
| Spring AI                              | 1.1.2                |
| Spring Cloud Function                  | 3.2.12               |
| SpringDoc OpenAPI                      | 2.8.14               |
| gRPC Spring Boot Starter (`net.devh`)  | 3.1.0.RELEASE        |
| `spring-dependency-management` plugin  | 1.1.6                |
| Kotlin                                 | 2.3.10               |
| Jackson                                | 2.21.0               |

## Target State (Spring Boot 4.0)

| Component          | Required Version                         |
|--------------------|------------------------------------------|
| Spring Boot        | 4.0.x                                    |
| Spring Framework   | **7.0**                                  |
| Spring Security    | **7.0**                                  |
| Spring Data        | **2025.1**                               |
| Jakarta EE         | **11** (Servlet 6.1)                     |
| Hibernate          | **7.1**                                  |
| Jackson            | **3.0** (Jackson 2 ships deprecated)     |
| Kotlin             | 2.2+ (already on 2.3.10 -- OK)          |
| Java               | 17+ (already met)                        |

---

## CRITICAL Issues (Blockers)

### 1. gRPC Spring Boot Starter (`net.devh`) -- NO Spring Boot 4 support

**Severity: BLOCKER (unblocked -- see [Appendix A](#appendix-a-grpc-migration-to-raw-grpc-java) for solution)**

The `net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE` does **not** have a Spring Boot 4 compatible version.

**Resolution:** Remove the `net.devh` starter entirely and reimplement the gRPC server on top of raw **grpc-java** (no Spring gRPC, no framework wrappers). This eliminates the third-party dependency and gives full control over the server lifecycle. See [Appendix A](#appendix-a-grpc-migration-to-raw-grpc-java) for detailed analysis and implementation plan.

**Impact:**
- `data/mill-data-grpc-service` (main source + tests)
- `clients/mill-jdbc-driver` (tests only)
- `apps/mill-service` (configuration)

**Action required:**
- Remove `net.devh` dependencies from `libs.versions.toml` and all `build.gradle.kts` files
- Reimplement gRPC server lifecycle, security interceptors, and exception handling using raw grpc-java
- Rewrite test infrastructure to use grpc-java `InProcessServer`/`InProcessChannel`
- Update application configuration (`grpc.server.*` → custom `mill.services.grpc.*` properties)

### 2. Spring AI Version Incompatibility

**Severity: BLOCKER**

Spring AI 1.1.2 is built for Spring Boot 3.x. Spring Boot 4 requires **Spring AI 2.0.x** (currently at 2.0.0-M2, not yet GA).

**Impact:**
- All `ai/` modules (`mill-ai-core`, `mill-ai-core-ext`, `mill-ai-nlsql-chat-service`)
- `apps/mill-service`
- All usages of `spring-ai-client-chat`, `spring-ai-vector-store`, model starters, MCP server starters, `spring-ai-starter-model-chat-memory-repository-jdbc`

**Action required:**
- Upgrade to Spring AI 2.0.x (currently M2 -- may need to wait for GA)
- Spring AI 2.0 has its own breaking changes: API renames, new artifact coordinates
- There are known dependency resolution issues with Spring AI + Spring Boot 4
- Spring AI 2.0 requires **Java 21** for development

### 3. Jackson 3.0 Migration

**Severity: HIGH**

Spring Boot 4 ships with Jackson 3.0 as the default. This is a major breaking change.

**Impact:** The project explicitly declares Jackson 2.x dependencies in `libs.versions.toml`:
- `jackson-core`
- `jackson-databind`
- `jackson-dataformat-yaml`
- `jackson-datatype-jsr310`
- `jackson-datatype-jdk8`
- `jackson-module-kotlin`

**Key breaking changes:**
- Package namespace shift: `com.fasterxml.jackson.*` → `tools.jackson.*` (annotations stay under `com.fasterxml.jackson.annotation`)
- Maven group IDs change: `com.fasterxml.jackson` → `tools.jackson`
- `ObjectMapper` replaced by immutable `JsonMapper` (builder pattern)
- Checked `JsonProcessingException` → unchecked `JacksonException`
- Default behavior changes:
  - `WRITE_DATES_AS_TIMESTAMPS` now defaults to `false` (ISO-8601 strings)
  - `FAIL_ON_NULL_FOR_PRIMITIVES` defaults to `true`
  - `SORT_PROPERTIES_ALPHABETICALLY` defaults to `true`

**Action required:**
- Update all Jackson dependencies in `libs.versions.toml` to Jackson 3.0 coordinates
- Search all source code for `ObjectMapper` usage and migrate to `JsonMapper`
- Review any custom serializers/deserializers
- Consider using the OpenRewrite recipe `UpgradeJackson_2_3` to automate refactoring

### 4. Spring Cloud Function Compatibility

**Severity: LOW (not a blocker)**

Spring Cloud Function 3.2.12 is not compatible with Spring Boot 4. A compatible version (likely 5.x) is needed but no GA release exists yet.

**Impact:**
- `misc/cloud/mill-azure-service-function`
- Related cloud modules using `spring-cloud-function-adapter-azure`, `spring-cloud-function-context`, `spring-cloud-function-grpc`, `spring-cloud-starter-function-web`
- The `spring-boot-experimental-thin-launcher` plugin (v1.0.31.RELEASE) is also likely incompatible

**Note:** The Azure function module is present in the codebase but is **not compiled or included in the distribution**. This means it does not block the Spring Boot 4 migration. It can be upgraded independently later when Spring Cloud Function releases a compatible version, or excluded/removed if no longer needed.

**Action required:**
- Exclude from the migration scope for now
- Monitor Spring Cloud release train for Spring Boot 4 compatible versions when/if needed

### 5. SpringDoc OpenAPI -- Upgrade to 3.x Required

**Severity: MODERATE**

SpringDoc OpenAPI 2.x is built for Spring Boot 3.x. Per the [SpringDoc compatibility matrix](https://springdoc.org/faq.html#_what_is_the_compatibility_matrix_of_springdoc_openapi_with_spring_boot), **springdoc-openapi 3.x.x is compatible with Spring Boot 4.x.x**. This is not a blocker but requires a version bump.

**Impact:**
- `apps/mill-service` (uses `springdoc-openapi-starter-webmvc-ui` and `springdoc-openapi-starter-webflux-api`)
- `services/mill-metadata-service` (hardcoded `springdoc-openapi-starter-webmvc-ui:2.3.0`)

**Action required:**
- Upgrade `springDoc` version in `libs.versions.toml` from `2.8.14` to latest `3.x.x`
- Fix `mill-metadata-service` hardcoded `2.3.0` to use version catalog
- Review for any breaking API changes between springdoc-openapi 2.x and 3.x

---

## MODERATE Issues (Require Code Changes)

### 6. Starter POM Renames

Spring Boot 4 renamed several starters. The old names are deprecated but still work.

| Current in `libs.versions.toml`                            | Spring Boot 4 Replacement                                  |
|------------------------------------------------------------|------------------------------------------------------------|
| `spring-boot-starter-web`                                  | `spring-boot-starter-webmvc`                               |
| `spring-boot-starter-oauth2-client`                        | `spring-boot-starter-security-oauth2-client`               |
| `spring-boot-starter-oauth2-resource-server`               | `spring-boot-starter-security-oauth2-resource-server`      |

**Files to update:**
- `libs.versions.toml` lines 40-42: update Maven coordinates for the above starters
- All `build.gradle.kts` files referencing `libs.boot.starter.web` (used in ~8 modules)

### 7. Remaining `javax.*` Imports

The following files still use `javax.*` imports that must be migrated to `jakarta.*`:

| File | Import | Replacement |
|------|--------|-------------|
| `misc/rapids/rapids-srv-worker/.../ODataServlet.java` | `javax.servlet.*` | `jakarta.servlet.*` |
| `misc/rapids/rapids-srv-worker/.../JdbcService.java` | `javax.servlet.http.HttpServletRequest` | `jakarta.servlet.http.HttpServletRequest` |
| `ai/mill-ai-core-ext/.../ChatService.kt` | `javax.annotation.PostConstruct` | `jakarta.annotation.PostConstruct` |
| `libs.versions.toml` line 96 | `javax-annotation-api` dependency | Remove or replace with `jakarta.annotation-api` |

### 8. `spring.factories` Legacy Auto-Configuration

**File:** `core/mill-metadata-core/src/main/resources/META-INF/spring.factories`

This still registers auto-configuration classes via the legacy `spring.factories` mechanism. Spring Boot 4 ignores `spring.factories` for auto-configuration discovery.

The new-format file already exists at:
`core/mill-metadata-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Action:** Remove the `spring.factories` file.

### 9. Spring Security 7.0 Changes

Spring Security 7.0 includes breaking changes. While the security code already uses modern APIs (`authorizeHttpRequests`, `SecurityFilterChain`), Security 7.0:
- Changes default behavior for CSRF, headers, and other security defaults
- May change OAuth2 login/resource server configuration
- Has package reorganizations

**Files to review:**
- `core/mill-security-core/src/main/java/.../ApiSecurityConfiguration.java`
- `core/mill-security-core/src/main/java/.../AppSecurityConfiguration.java`
- `core/mill-security-core/src/main/java/.../ServicesSecurityConfiguration.java`
- `core/mill-security-core/src/main/java/.../SwaggerSecurityConfig.java`
- `core/mill-security-core/src/main/java/.../WellKnownSecurityConfiguration.java`
- `core/mill-security-core/src/main/java/.../AuthRoutesSecurityConfiguration.java`
- `data/mill-data-grpc-service/src/main/java/.../GrpcServiceSecurityConfiguration.java`

**Also:** remove hardcoded `spring-security-test` at `6.5.7` in `libs.versions.toml` line 34 and let Spring Boot 4 manage it.

### 10. Modularization Impact

Spring Boot 4 has been completely modularized with new package structure. Code importing from `org.springframework.boot.*` internal packages may break:

- `BootstrapRegistry` moved from `org.springframework.boot` to `org.springframework.boot.bootstrap`
- `EnvironmentPostProcessor` moved from `org.springframework.boot.env` to `org.springframework.boot`
- Auto-configuration classes are now package-private (cannot be referenced directly)

**Tip:** Use `spring-boot-starter-classic` and `spring-boot-starter-test-classic` temporarily during migration to get the old all-in-one classpath while fixing imports.

---

## LOW Issues (Minor / Already Good)

### 11. Pre-existing Config Inconsistencies

Fix these before the upgrade:
- `misc/rapids/` has its own version catalog with Spring Framework 6.1.1 hardcoded and broken version references (`libs.versions.boot` doesn't exist in rapids catalog)
- `services/mill-metadata-service` has hardcoded `springdoc-openapi-starter-webmvc-ui:2.3.0` instead of using version catalog

### 12. `PropertyMapper` API Changes

Spring Boot 4 changes `PropertyMapper` null-handling behavior. The `alwaysApplyingNotNull()` method is removed. If any code uses `PropertyMapper`, review configuration binding code.

### 13. Kotlin Version

Already at 2.3.10 which exceeds the 2.2 minimum. No issue.

### 14. Java Version

Requires Java 17+. Already met given Spring Boot 3.5.10 also requires Java 17+.
**Note:** Spring AI 2.0 requires Java 21 -- this may force a Java version bump across the project.

### 15. `spring-dependency-management` Plugin

The `io.spring.dependency-management` plugin (currently 1.1.6) continues to work with Spring Boot 4. No change required, but consider Gradle's native BOM support for faster builds.

---

## Recommended Upgrade Strategy

### Pre-condition: Wait for Third-Party GA Releases

The following dependencies do not yet have GA releases compatible with Spring Boot 4:
- **Spring AI 2.0** (currently M2)
**SpringDoc OpenAPI 3.x.x** is confirmed compatible with Spring Boot 4.x ([source](https://springdoc.org/faq.html#_what_is_the_compatibility_matrix_of_springdoc_openapi_with_spring_boot)).

**Spring Cloud Function** has no Spring Boot 4 compatible version yet, but the Azure function module (`misc/cloud/`) is not compiled or included in the distribution, so this is **not a blocker**.

Until Spring AI 2.0 GA is available, a full migration is **not possible** without accepting milestone/snapshot risk.

### Phase 1 -- Pre-migration Cleanup (on 3.5.x)

These changes can be done now, independently of the upgrade:

- [ ] Clean up hardcoded versions: align `mill-metadata-service` springdoc and `spring-security-test` with version catalog
- [ ] Remove `spring.factories` from `core/mill-metadata-core` (keep only `AutoConfiguration.imports`)
- [ ] Fix remaining `javax.*` imports → `jakarta.*`
- [ ] Remove or replace `javax-annotation-api` dependency
- [ ] Fix `misc/rapids/` broken version catalog references
- [ ] Update deprecated starter names in version catalog (`starter-web` → `starter-webmvc`, OAuth2 renames)

### Phase 2 -- Core Upgrade

- [ ] Bump `springBoot` to `4.0.x` in `libs.versions.toml`
- [ ] Replace `net.devh` gRPC starters with Spring gRPC 1.0.0
- [ ] Upgrade Jackson dependencies to 3.0 coordinates (group ID + artifact changes)
- [ ] Upgrade Spring AI to 2.0.x
- [ ] ~~Upgrade Spring Cloud Function~~ (out of scope -- `misc/cloud/` not in distro)
- [ ] Upgrade SpringDoc OpenAPI from 2.x to 3.x (confirmed Boot 4 compatible)
- [ ] Remove hardcoded `spring-security-test` version (let Boot manage it)
- [ ] Temporarily add `spring-boot-starter-classic` if needed during migration

### Phase 3 -- Fix Compilation and Tests

- [ ] Fix Jackson package imports (`com.fasterxml.jackson.*` → `tools.jackson.*`)
- [ ] Fix any `ObjectMapper` → `JsonMapper` migration
- [ ] Fix Spring Security 7.0 breaking changes in security configuration classes
- [ ] Fix gRPC service wiring with Spring gRPC (annotations, config, properties)
- [ ] Fix any Spring Boot modularization import breakages
- [ ] Review `PropertyMapper` usage
- [ ] Run full test suite and fix failures

### Phase 4 -- Validation

- [ ] Run `./gradlew build` across all modules
- [ ] Run `./gradlew test` across all modules
- [ ] Run integration tests (`testIT`)
- [ ] Validate gRPC endpoints manually
- [ ] Validate OpenAPI documentation endpoints
- [ ] Validate AI chat functionality
- [ ] Validate OAuth2 security flows
- [ ] Remove `spring-boot-starter-classic` if it was used temporarily

---

## Module Impact Matrix

| Module | Impact | Key Issues |
|--------|--------|------------|
| `core/mill-core` | Low | Test dependencies only |
| `core/mill-service-core` | Medium | Security, auto-config, modularization |
| `core/mill-security-core` | High | Spring Security 7.0 changes, OAuth2 starter renames |
| `core/mill-metadata-core` | Medium | Remove `spring.factories`, auto-config imports |
| `core/mill-test-kit` | Low | Test starter updates |
| `data/mill-data-grpc-service` | **Critical** | gRPC starter replacement, security rewrite |
| `data/mill-data-http-service` | Low | Test dependencies only |
| `data/mill-data-backends` | Low | Starter + config processor |
| `services/mill-well-known-service` | Medium | Security, web starter rename |
| `services/mill-metadata-service` | Medium | Hardcoded springdoc, web starter rename |
| `ai/mill-ai-core` | **Critical** | Spring AI 2.0 migration |
| `ai/mill-ai-core-ext` | **Critical** | Spring AI 2.0, `javax.annotation.PostConstruct` fix |
| `ai/mill-ai-nlsql-chat-service` | **Critical** | Spring AI 2.0, JPA/Data 2025.1, Security 7.0 |
| `apps/mill-service` | **Critical** | All of the above converge here |
| `ui/mill-grinder-service` | Low | Web starter rename |
| `clients/mill-jdbc-driver` | Medium | gRPC client starter replacement (tests) |
| `misc/cloud/mill-azure-service-function` | N/A (not in distro) | Spring Cloud Function, thin-launcher -- out of migration scope |
| `misc/rapids/*` | Medium | javax imports, broken version catalog |

---

## References

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Framework 7.0 Release Notes](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes)
- [Spring Security 7.0 Migration](https://docs.spring.io/spring-security/reference/7.0/migration/)
- [Spring gRPC 1.0 Announcement](https://spring.io/blog/2025/11/11/spring-grpc-next-steps)
- [Spring AI 2.0 M1 Announcement](https://spring.io/blog/2025/12/11/spring-ai-2-0-0-M1-available-now)
- [Jackson 3.0 in Spring Boot 4](https://blog.vvauban.com/blog/spring-boot-4-moves-to-jackson-3-already-in-m3)
- [grpc-java GitHub](https://github.com/grpc/grpc-java)

---

## Appendix A: gRPC Migration to Raw grpc-java

### Rationale

Instead of migrating from `net.devh` to another framework wrapper (Spring gRPC), reimplement on raw **grpc-java**. Benefits:
- Zero dependency on third-party Spring-gRPC integration libraries
- Full control over server lifecycle, threading, and shutdown
- No compatibility concerns with future Spring Boot upgrades
- grpc-java is the official Google-maintained library -- stable, well-documented, long-term supported
- The existing codebase already depends on `io.grpc:grpc-*` transitively; the `net.devh` starter is a thin wrapper

### Current Implementation Inventory

#### Production Code (3 files + 1 descriptor)

| File | `net.devh` Usage | Complexity |
|------|-----------------|------------|
| `MillGrpcService.java` | `@GrpcService` annotation (service registration) | **Low** -- just annotation removal + manual server registration |
| `MillGrpcServiceExceptionAdvice.java` | `@GrpcAdvice`, `@GrpcExceptionHandler` (exception mapping) | **Medium** -- rewrite as a grpc-java `ServerInterceptor` |
| `GrpcServiceSecurityConfiguration.java` | `BasicGrpcAuthenticationReader`, `BearerAuthenticationReader`, `CompositeGrpcAuthenticationReader`, `GrpcSecurityMetadataSource`, `AccessPredicateVoter`, `ManualGrpcSecurityMetadataSource` | **High** -- most complex; rewrite as a grpc-java `ServerInterceptor` that extracts credentials from metadata and delegates to Spring Security's `AuthenticationManager` |
| `GrpcServiceDescriptor.java` | None (pure Spring `@Component`) | **None** -- no changes needed |

#### Test Code (5 test files + 1 config)

| File | `net.devh` Usage | Complexity |
|------|-----------------|------------|
| `MillServiceBaseTestConfiguration.java` | `@GrpcClient`, `GrpcClientAutoConfiguration`, `GrpcAdviceAutoConfiguration`, `@ImportAutoConfiguration` | **Medium** -- replace with grpc-java `InProcessServer` + `InProcessChannel` setup |
| `MillGrpcServiceBaseTest.java` | Injects `DataConnectServiceGrpc.DataConnectServiceBlockingStub` (provided by `net.devh` auto-config) | **Low** -- stub creation moves to manual `InProcessChannel` |
| `MillGrpcServiceMetadataTest.java` | Uses injected blocking stub | **Low** -- no direct `net.devh` imports |
| `MillGrpcServiceExecuteTest.java` | Uses injected blocking stub | **Low** -- no direct `net.devh` imports |
| `MillGrpcServiceRewriteTest.java` | Uses injected blocking stub | **Low** -- no direct `net.devh` imports |
| `BootstrapTest.java` / `H2Db.java` | None | **None** |

#### External Module Dependencies (test-only)

| File | `net.devh` Usage | Complexity |
|------|-----------------|------------|
| `clients/mill-jdbc-driver/InProcessTest.java` | `GrpcAdviceAutoConfiguration`, `@Value("${grpc.server.in-process-name}")` | **Low** -- replace with manual in-process setup |
| `clients/mill-jdbc-driver/ColumnsMetadataTest.java` | `GrpcAdviceAutoConfiguration` | **Low** -- same pattern |

#### Configuration Files

| File | Content to Change |
|------|------------------|
| `data/mill-data-grpc-service/src/test/resources/application-test.yml` | Remove `grpc.server.*` and `grpc.client.*` sections |
| `apps/mill-service/src/main/resources/application.yml` | Remove `grpc.server.*`; keep `mill.services.grpc.*` |
| `data/mill-data-backends/src/test/resources/application-test-calcite.yml` | Remove `grpc.server.*` and `grpc.client.*` sections |
| `data/mill-data-grpc-service/src/main/resources/META-INF/additional-spring-configuration-metadata.json` | Remove `net.devh` references from descriptions |

#### Build Files

| File | Change |
|------|--------|
| `libs.versions.toml` | Remove `bootGRPC` version, `bootGRPC-server`, `bootGRPC-client` entries |
| `data/mill-data-grpc-service/build.gradle.kts` | Replace `libs.bootGRPC.server` with `libs.grpc.netty.shaded` (already present); remove `libs.javax.annotation.api` |
| `clients/mill-jdbc-driver/build.gradle.kts` | Remove `libs.bootGRPC.client` and `libs.bootGRPC.server` from test dependencies; add `libs.grpc.inprocess` |

### Migration Strategy: Side-by-Side Module

The architecture fully supports spinning up a new pure grpc-java module alongside the existing `net.devh`-based module with **no blockers**.

#### Why side-by-side works

1. **`@ConditionalOnService("grpc")` isolates everything.** All 4 production classes in the current module are gated by this annotation, which checks `mill.services.grpc.enable` in the environment. Both modules can coexist on the classpath; activation is controlled by config.

2. **No auto-configuration registration.** The `data/mill-data-grpc-service` module has no `spring.factories` or `AutoConfiguration.imports`. Beans are discovered via component scanning. The new module follows the same pattern -- no conflict.

3. **Clean dependency graph.** Both modules depend on `data:mill-data-backends` (provides `ServiceHandler`, `DataOperationDispatcher`) and `core:mill-security-core`. No circular dependencies.

4. **`apps/mill-service` is the only consumer.** It includes `data:mill-data-grpc-service` as an `implementation` dependency. Add the new module alongside it during transition.

5. **RPC handler bodies are pure grpc-java.** They use `io.grpc.stub.StreamObserver`, `ServerCallStreamObserver`, and protobuf stubs. The new module can reuse the handler code verbatim.

6. **`clients/mill-jdbc-driver` tests** depend on the gRPC module at test scope only. During transition they can depend on both; once validated, switch to the new one.

#### One thing to fix in the new module

`MillGrpcService` in the current module is annotated `@SpringBootApplication` (making it both a service bean and a component-scan root). The new module must **not** replicate this -- the gRPC service should be a plain `@Component` and let `apps/mill-service` remain the sole `@SpringBootApplication`.

#### Module layout during transition

```
data/
  mill-data-grpc-service/        ← existing (net.devh) -- unchanged
  mill-data-grpc-service-v2/     ← new module (pure grpc-java)
```

**`settings.gradle.kts`** -- add:
```kotlin
include(":data:mill-data-grpc-service-v2")
```

**`apps/mill-service/build.gradle.kts`** -- include both during transition:
```kotlin
implementation(project(":data:mill-data-grpc-service"))     // old
implementation(project(":data:mill-data-grpc-service-v2"))  // new
```

#### Activation modes during transition

| Mode | Config | Effect |
|------|--------|--------|
| Old only (current) | `mill.services.grpc.enable=true` | Only `net.devh`-based module activates (no v2 beans scanned) |
| New only (target) | `mill.services.grpc-v2.enable=true` | Only pure grpc-java module activates |
| Both (comparison) | Both enabled, different ports | Run both simultaneously for side-by-side testing |

During comparison testing the new module can bind to a separate port (e.g. `mill.services.grpc-v2.port=9098`) while the old module keeps `9090`.

#### Cutover steps

1. Create `data/mill-data-grpc-service-v2` with pure grpc-java implementation
2. Add to `settings.gradle.kts` and `apps/mill-service/build.gradle.kts`
3. Use `mill.services.grpc-v2.enable=true` for testing, keeping old module on `mill.services.grpc.enable=true`
4. Validate all gRPC endpoints, security flows, and streaming behavior
5. Switch `ConditionalOnService` annotation in v2 from `"grpc-v2"` to `"grpc"`
6. Remove old `data/mill-data-grpc-service` module
7. Optionally rename `mill-data-grpc-service-v2` → `mill-data-grpc-service`

### Implementation Plan

#### Step 1: gRPC Server Lifecycle Bean (Low complexity)

Create a Spring `@Configuration` that starts/stops a grpc-java `Server` tied to the Spring application lifecycle:

```java
@Configuration
@ConditionalOnService("grpc")
public class GrpcServerConfiguration {

    @Bean
    public Server grpcServer(
            MillGrpcService service,
            List<ServerInterceptor> interceptors,
            @Value("${mill.services.grpc.port:9090}") int port,
            @Value("${mill.services.grpc.address:*}") String address) {

        var builder = NettyServerBuilder
                .forPort(port)
                .addService(ServerInterceptors.intercept(service, interceptors));

        return builder.build();
    }

    @Bean
    public SmartLifecycle grpcServerLifecycle(Server grpcServer) {
        return new GrpcServerLifecycle(grpcServer);
    }
}
```

The `GrpcServerLifecycle` implements `SmartLifecycle` to call `server.start()` on startup and `server.shutdown()` / `server.awaitTermination()` on context close. This is ~40 lines of boilerplate.

#### Step 2: Replace `@GrpcService` with plain Spring `@Component` (Low complexity)

`MillGrpcService` already extends `DataConnectServiceGrpc.DataConnectServiceImplBase` which is a pure grpc-java class. The only `net.devh` coupling is the `@GrpcService` annotation. Replace with `@Component`:

```java
@Slf4j
@Component  // was @GrpcService
@ConditionalOnService("grpc")
public class MillGrpcService extends DataConnectServiceGrpc.DataConnectServiceImplBase {
    // ... rest unchanged
}
```

The server configuration from Step 1 registers this service manually.

#### Step 3: Exception Handling Interceptor (Medium complexity)

Replace `@GrpcAdvice` / `@GrpcExceptionHandler` with a standard grpc-java `ServerInterceptor`:

```java
@Component
@ConditionalOnService("grpc")
public class GrpcExceptionInterceptor implements ServerInterceptor {
    @Override
    public <Q, S> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, S> call,
            Metadata headers,
            ServerCallHandler<Q, S> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
                next.startCall(call, headers)) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (StatusRuntimeException e) {
                    call.close(e.getStatus(), e.getTrailers());
                } catch (StatusException e) {
                    call.close(e.getStatus(), e.getTrailers());
                } catch (Exception e) {
                    call.close(Status.INTERNAL
                        .withDescription("ERROR:" + e.getMessage())
                        .withCause(e), new Metadata());
                }
            }
        };
    }
}
```

This replaces the 3 exception handlers from `MillGrpcServiceExceptionAdvice` (~30 lines).

#### Step 4: Security Interceptor (High complexity -- most effort)

Replace the entire `GrpcServiceSecurityConfiguration` with a grpc-java `ServerInterceptor` that:
1. Extracts credentials from gRPC `Metadata` (Basic auth header or Bearer token)
2. Creates a Spring Security `Authentication` object
3. Authenticates via Spring's `AuthenticationManager`
4. Sets the `SecurityContextHolder` for the call duration

```java
@Component
@ConditionalOnSecurity
@ConditionalOnService("grpc")
public class GrpcSecurityInterceptor implements ServerInterceptor {

    private final AuthenticationManager authenticationManager;

    @Override
    public <Q, S> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, S> call,
            Metadata headers,
            ServerCallHandler<Q, S> next) {

        String authHeader = headers.get(
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (authHeader == null) {
            call.close(Status.UNAUTHENTICATED, new Metadata());
            return new ServerCall.Listener<>() {};
        }

        Authentication authentication = parseAuthHeader(authHeader);
        Authentication authenticated = authenticationManager.authenticate(authentication);
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(authenticated);
        SecurityContextHolder.setContext(ctx);

        try {
            return next.startCall(call, headers);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private Authentication parseAuthHeader(String header) {
        if (header.startsWith("Basic ")) {
            // decode base64 -> UsernamePasswordAuthenticationToken
        } else if (header.startsWith("Bearer ")) {
            // extract token -> BearerTokenAuthenticationToken
        }
        throw new StatusRuntimeException(Status.UNAUTHENTICATED);
    }
}
```

This replaces all 6 `net.devh` security classes (`BasicGrpcAuthenticationReader`, `BearerAuthenticationReader`, `CompositeGrpcAuthenticationReader`, `GrpcSecurityMetadataSource`, `ManualGrpcSecurityMetadataSource`, `AccessPredicateVoter`). The existing `AuthenticationMethods` bean and `AuthenticationType` enum are reused as-is.

**Estimated size:** ~80-100 lines for the interceptor + helper methods.

#### Step 5: Test Infrastructure (Medium complexity)

Replace `net.devh` test auto-configuration with raw grpc-java in-process testing:

```java
@Configuration
public class GrpcTestConfiguration {

    private Server server;

    @Bean
    public Server grpcTestServer(MillGrpcService service,
                                  List<ServerInterceptor> interceptors) throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(ServerInterceptors.intercept(service, interceptors))
                .build()
                .start();
        return server;
    }

    @Bean
    public ManagedChannel grpcTestChannel(Server server) {
        // extract in-process name from server
        return InProcessChannelBuilder.forName(/* name */)
                .directExecutor()
                .build();
    }

    @Bean
    public DataConnectServiceGrpc.DataConnectServiceBlockingStub blockingStub(
            ManagedChannel channel) {
        return DataConnectServiceGrpc.newBlockingStub(channel);
    }
}
```

This replaces `@GrpcClient("test")` injection and `GrpcClientAutoConfiguration` / `GrpcAdviceAutoConfiguration` imports. The existing test classes that inject `DataConnectServiceGrpc.DataConnectServiceBlockingStub` via `@Autowired` continue to work unchanged.

The `clients/mill-jdbc-driver` tests follow the same pattern -- replace `GrpcAdviceAutoConfiguration` import and `@Value("${grpc.server.in-process-name}")` with a bean that provides the channel/server name directly.

### Complexity Summary

| Component | Effort | Lines (estimate) | Risk |
|-----------|--------|-------------------|------|
| New module scaffolding (`build.gradle.kts`, settings) | Low | ~30 | None |
| Server lifecycle bean | Low | ~60 | Low |
| gRPC service `@Component` (copy handlers) | Trivial | ~80 (copied, ~2 changed) | None |
| Exception interceptor | Low-Medium | ~40 | Low |
| Security interceptor | Medium-High | ~100 | Medium -- must preserve Basic + OAuth2 auth flows |
| Test infrastructure | Medium | ~60 | Low -- well-scoped, in-process only |
| Config file additions (`mill.services.grpc-v2.*`) | Low | ~15 | None |
| JDBC driver test updates (after cutover) | Low | ~20 lines across 2 files | Low |
| Old module removal + build cleanup (after cutover) | Low | ~20 | None |
| **Total** | **~2-3 days** | **~400 lines new** | **Medium overall** |

The side-by-side approach adds ~100 lines of scaffolding overhead compared to an in-place rewrite, but significantly reduces risk since the old module is untouched until the new one is fully validated.

### What Stays Unchanged

- `MillGrpcService` method implementations (all 7 RPC handlers) -- these use only `io.grpc.stub.*` which is raw grpc-java
- `GrpcServiceDescriptor` -- pure Spring component, no `net.devh` dependency
- `DataConnectServiceGrpc` generated stubs -- unchanged protobuf output
- All proto definitions -- no changes
- `ServiceHandler`, `DataOperationDispatcher`, `VectorBlockIterator` -- business logic untouched
- `H2Db` and SQL test scripts -- test utilities untouched
- Test logic in all 4 test classes -- only the stub injection mechanism changes, not the test assertions

### Dependencies After Migration

**Remove from `libs.versions.toml`:**
- `bootGRPC` version entry
- `bootGRPC-server` library
- `bootGRPC-client` library

**Keep (already present):**
- `grpc-netty-shaded` (server transport)
- `grpc-core`, `grpc-api`, `grpc-stub`, `grpc-protobuf` (core gRPC)
- `grpc-testing`, `grpc-inprocess` (test infrastructure)

**No new external dependencies required.**
