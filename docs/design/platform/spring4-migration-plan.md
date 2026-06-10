# Spring Boot 3.5 to Spring Boot 4.0 Migration Plan

## Repository version baseline (Spring Boot 4 line)

Values below match **`libs.versions.toml`** on the **Spring Boot 4** line (story archive **[`spring4-migration-day-2`](../../workitems/completed/20260430-spring4-migration-day-2/STORY.md)**, WIs **WI-201**–**WI-209**). User-facing summary: [`docs/public/src/reference/platform-runtime.md`](../../public/src/reference/platform-runtime.md).

| Component                              | Version / notes |
|----------------------------------------|-----------------|
| Spring Boot                            | **4.0.6** |
| Spring Framework                       | **7.0** (managed by Boot) |
| Spring Security                        | **7.0** (BOM-aligned **`spring-security-test`** via **`platform(libs.boot.dependencies)`** in **`services/mill-service-common`** tests — **WI-099**); **WI-206** aligned auth/persistence **`testIT`** with Security 7 and Boot 4 test APIs |
| Spring AI                              | **2.0.0-M5** (milestone; **WI-203**) |
| Spring Cloud Function                  | 3.2.12 ( **`misc/cloud/`** only — not in distro) |
| SpringDoc OpenAPI                      | **3.0.3** (**WI-204**) |
| gRPC data plane                        | grpc-java **1.79.x** (see note below) |
| `spring-dependency-management` plugin  | 1.1.6 |
| Kotlin                                 | 2.3.10 |
| Jackson                                | **3.1.2** (`tools.jackson:*`, **WI-205**) |

**Historical baseline (pre–day-2, Boot 3.5.x):** Spring Boot **3.5.10**, Spring AI **1.1.2**, SpringDoc **2.8.14**, Jackson **2.21** — superseded by the table above after **`spring4-migration-day-2`** (closed **2026-04-30**).

**gRPC:** The composite build uses **`services/mill-data-grpc-service`** on **raw grpc-java** (Netty server, Spring-managed lifecycle, Kotlin `MillGrpcService` + interceptors). **`net.devh` is not** declared in **`libs.versions.toml`**. A legacy tree **`misc/spring-3/mill-data-grpc-service`** (still illustrating `net.devh` usage) remains for historical comparison but is **not** included in **`settings.gradle.kts`**.

## Target State (Spring Boot 4.0)

These rows remain the **architectural target** for the migration; concrete pins are in the **Repository version baseline** table at the top (for example Spring Boot **4.0.6**, Jackson **3.1.x**).

| Component          | Required Version                         |
|--------------------|------------------------------------------|
| Spring Boot        | 4.0.x                                    |
| Spring Framework   | **7.0**                                  |
| Spring Security    | **7.0**                                  |
| Spring Data        | **2025.1**                               |
| Jakarta EE         | **11** (Servlet 6.1)                     |
| Hibernate          | **7.1**                                  |
| Jackson            | **3.x** (`tools.jackson`, Boot-managed)  |
| Kotlin             | 2.2+ (already on 2.3.10 -- OK)          |
| Java               | **21+** where Spring AI 2.0 / full stack is used (**WI-203**); catalog and CI assume Java **21** |

---

## CRITICAL Issues (Blockers)

### 1. gRPC Spring Boot Starter (`net.devh`) -- NO Spring Boot 4 support

**Severity: was BLOCKER for Boot 4 — mitigated on the main build (raw grpc-java).**

The `net.devh:grpc-server-spring-boot-starter` line does **not** have a Spring Boot 4–compatible release.

**Resolution (chosen):** Dropped `net.devh` from the **main** Gradle build and implemented the data-plane gRPC server on **raw grpc-java** (not Spring’s optional gRPC starter). [Appendix A](#appendix-a-grpc-migration-to-raw-grpc-java) documents the original plan and remains useful for rationale and the legacy layout.

**Main-build layout today:**
- `services/mill-data-grpc-service` (production + `testIT`)
- `clients/mill-jdbc-driver` (no `net.devh` in JDBC tests)
- `apps/mill-service` (**`mill.data.services.grpc.*`** configuration; see `GrpcServerProperties` in **`services/mill-data-grpc-service`**)

**Boot 4 validation:** **WI-208** re-validated **`services/mill-data-grpc-service`** and JDBC driver integration tests (including **`EmbeddedSkymillGrpcServer`** for Skymill-style **`testIT`**). No further `net.devh` removal is required in the composite build.

**Optional cleanup:** Delete or clearly mark **`misc/spring-3/mill-data-grpc-service`** as archival-only so it is not mistaken for active code.

### 2. Spring AI Version Incompatibility

**Severity: was BLOCKER — mitigated on the migration branch using Spring AI 2.0 milestone.**

Spring AI 1.1.x targets Spring Boot 3.x. Spring Boot 4 requires **Spring AI 2.0.x**. The **day-2** story pins **Spring AI `2.0.0-M5`** in **`libs.versions.toml`** (**WI-203**); GA remains optional for a future bump.

**Impact (historical):**
- AI Spring modules: e.g. `ai/mill-ai-v1-core`, `ai/mill-ai-v1-nlsql-chat-service` (and any other modules pulling Spring AI BOM artifacts)
- `apps/mill-service`
- All usages of `spring-ai-client-chat`, `spring-ai-vector-store`, model starters, MCP server starters, `spring-ai-starter-model-chat-memory-repository-jdbc`

**Action required (status):**
- Upgrade to Spring AI 2.0.x — **done** on the Boot 4 branch at **2.0.0-M5**; follow Spring AI release notes for API/coordinate changes
- Spring AI 2.0 requires **Java 21** for development and runtime where those modules are used

### 3. Jackson 3.0 Migration

**Severity: was HIGH — mitigated; catalog and code on Jackson 3.x (`tools.jackson`, `JsonMapper`) per WI-205.**

Spring Boot 4 ships with Jackson 3.x as the default. This was a major breaking change for code that targeted Jackson 2 only.

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

**Implementation status (2026-04-30):** **WI-205** (Jackson 3), **WI-203** (Spring AI **2.0.0-M5**), **WI-204** (SpringDoc **3.0.3**), **WI-206** (Spring Security 7 **`testIT`** / API alignment), **WI-208** (gRPC/HTTP **`testIT`** wiring, JDBC driver **`EmbeddedSkymillGrpcServer`** + profile-based host/port), and **WI-209** (full-repo **`./gradlew clean build`**, **`./gradlew test`**, **`./gradlew testIT`** green on CI/CD, migration plan + public docs + backlog) are complete. The version catalog uses **`tools.jackson:*`** at **3.1.2**. Production and test code use **`JsonMapper` / `YAMLMapper` builders**, **`ValueDeserializer`** where Jackson 2’s `JsonDeserializer`/`getCodec()` patterns no longer apply, and **`JacksonException`** instead of `JsonProcessingException`. Policy import (`JsonPolicyImporter`, `YamlPolicyImporter`) enables **`MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS`** so existing policy documents with uppercase enum literals keep working. Story tracker: [`completed/20260430-spring4-migration-day-2/STORY.md`](../../workitems/completed/20260430-spring4-migration-day-2/STORY.md).

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
- `metadata/mill-metadata-service`, `services/mill-service-common`, `data/mill-data-schema-service`, and other modules via **`libs.springdoc.*`** from the version catalog (formerly `services/mill-metadata-service` had a hardcoded SpringDoc version — **fixed**)

**Action required (for Boot 4 readiness on 3.5.x):**
- Upgrade `springDoc` version in `libs.versions.toml` from `2.8.14` to latest `3.x.x` **when** moving to Spring Boot 4
- Review for any breaking API changes between springdoc-openapi 2.x and 3.x

---

## MODERATE Issues (Require Code Changes)

### 6. Starter POM Renames

Spring Boot 4 renamed several starters. The old names are deprecated but still work. On the **Boot 4** branch, **`libs.versions.toml`** / Gradle aliases follow the new coordinates (**WI-207**).

| Previous / deprecated alias intent                         | Spring Boot 4 Replacement                                  |
|------------------------------------------------------------|------------------------------------------------------------|
| `spring-boot-starter-web`                                  | `spring-boot-starter-webmvc`                               |
| `spring-boot-starter-oauth2-client`                        | `spring-boot-starter-security-oauth2-client`               |
| `spring-boot-starter-oauth2-resource-server`               | `spring-boot-starter-security-oauth2-resource-server`      |

**Files to update:**
- `libs.versions.toml` (entries for `boot-starter-web`, OAuth2 starters — see table above)
- All `build.gradle.kts` files referencing `libs.boot.starter.web` and related aliases

### 7. Remaining `javax.*` Imports

**Historical:** Older revisions listed `misc/rapids/*`, `ai/mill-ai-core-ext/.../ChatService.kt`, and **javax.servlet** usages. The **`misc/rapids/`** tree is **no longer** in this repository, and a repo-wide scan shows **no** `javax.servlet` / `javax.annotation` imports in current **`*.java` / `*.kt`** sources.

**Resolved (Boot 3.5 housekeeping):**

| Location | Notes |
|----------|--------|
| `libs.versions.toml` — `jakarta-annotation-api` | Catalog uses **`jakarta.annotation:jakarta.annotation-api`**; product modules use **`libs.jakarta.annotation.api`** (no `javax.annotation-api` coordinate). |

### 8. `spring.factories` Legacy Auto-Configuration

**Historical path in this doc:** `core/mill-metadata-core/src/main/resources/META-INF/spring.factories`.

**Current repo:** Autoconfigure and registration live under **`metadata/mill-metadata-autoconfigure`** (and related modules). **`metadata/mill-metadata-core`** resources no longer include **`META-INF/spring.factories`** (or `AutoConfiguration.imports`).

**Action:** Keep auto-configuration registered **only** via **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** where applicable. Occasional **repo-wide greps** for `spring.factories` / `EnableAutoConfiguration` remain good hygiene before Boot 4 (see backlog **P-5** / spring4 pre-migration story).

### 9. Spring Security 7.0 Changes

Spring Security 7.0 includes breaking changes. While the security code already uses modern APIs (`authorizeHttpRequests`, `SecurityFilterChain`), Security 7.0:
- Changes default behavior for CSRF, headers, and other security defaults
- May change OAuth2 login/resource server configuration
- Has package reorganizations

**Files to review (representative — adjust packages as code evolves):**
- `security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/ApiSecurityConfiguration.java` (and related configs under **`security/mill-security-autoconfigure`**, **`security/mill-service-security`**, **`security/mill-security`**, **`security/mill-security-auth-service`**)
- `services/mill-data-grpc-service/src/main/kotlin/io/qpointz/mill/data/backend/grpc/GrpcSecurityInterceptor.kt`

**Also:** `spring-security-test` is aligned with the Boot BOM via **`platform(libs.boot.dependencies)`** in **`services/mill-service-common`** tests (**WI-099**); keep that pattern when adding new consumers.

### 10. Modularization Impact

Spring Boot 4 has been completely modularized with new package structure. Code importing from `org.springframework.boot.*` internal packages may break:

- `BootstrapRegistry` moved from `org.springframework.boot` to `org.springframework.boot.bootstrap`
- `EnvironmentPostProcessor` moved from `org.springframework.boot.env` to `org.springframework.boot`
- Auto-configuration classes are now package-private (cannot be referenced directly)

**Tip:** Use `spring-boot-starter-classic` and `spring-boot-starter-test-classic` temporarily during migration to get the old all-in-one classpath while fixing imports.

---

## LOW Issues (Minor / Already Good)

### 11. Pre-existing Config Inconsistencies

Address before or during the Boot 4 upgrade:
- **`spring-security-test`** — aligned with Boot BOM in **`services/mill-service-common`** (**WI-099**)
- **Deprecated starter coordinates** (§6) — **resolved** on the Boot 4 branch via **WI-207** (catalog + Gradle aliases to **`spring-boot-starter-webmvc`** and **`spring-boot-starter-security-oauth2-*`**)

**Resolved on main:** `metadata/mill-metadata-service` uses the shared **`libs.springdoc.*`** catalog entries (no hardcoded SpringDoc artifact version in that module’s `build.gradle.kts`).

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

### Pre-condition: Third-Party GA vs milestone

**Spring AI 2.0** is not yet GA; this repository **accepts milestone risk** for the Boot 4 line (**`2.0.0-M5`**, **WI-203**) until a GA upgrade is scheduled.

**SpringDoc OpenAPI 3.x.x** is confirmed compatible with Spring Boot 4.x ([source](https://springdoc.org/faq.html#_what_is_the_compatibility_matrix_of_springdoc_openapi_with_spring_boot)); the repo is on **3.0.3** (**WI-204**).

**Spring Cloud Function** has no Spring Boot 4 compatible version yet, but the Azure function module (`misc/cloud/`) is not compiled or included in the distribution, so this is **not a blocker**.

### Phase 1 -- Pre-migration Cleanup (on 3.5.x)

These changes can be done now, independently of the upgrade:

- [x] `metadata/mill-metadata-service` SpringDoc — uses **version catalog** (`libs.springdoc.*`); repo-wide **`*.kts` / `*.toml`** audit — **no** hardcoded **`org.springdoc:*`** pins (**WI-101**); bump to SpringDoc **3.x** stays **P-8** / Boot 4
- [x] `net.devh` gRPC — **removed** from main build; **`services/mill-data-grpc-service`** uses **grpc-java**
- [x] Align **`spring-security-test`** with Boot BOM — **`services/mill-service-common`** test suite uses **`platform(libs.boot.dependencies)`** + unversioned coordinate; no standalone pin in **`libs.versions.toml`** (**WI-099**)
- [x] Legacy **`spring.factories`** at old **`mill-metadata-core`** path — **gone** from **`metadata/mill-metadata-core`**; **WI-102** repo audit: **no** `META-INF/spring.factories` files; Boot 3 autoconfig via **`AutoConfiguration.imports`** only (see WI-102 completion notes)
- [x] Historical **`javax.*` servlet / `PostConstruct`** paths — **no** remaining matches in current sources; annotation API is **`jakarta.annotation-api`** in the catalog (WI-098)
- [x] `misc/rapids/` — **removed** from repo (no action)
- [x] **`misc/`** hygiene — non-product trees **documented** (`misc/README.md`, `misc/cloud/README.md`); design docs no longer point at removed **`misc/rapids/`** paths (**WI-100**)
- [x] Replace **`javax-annotation-api`** with **`jakarta.annotation-api`** where possible; remove `javax` coordinates when unused
- [x] Boot 4 **jump-start inventory** (grep snapshot) — [`spring4-boot4-jump-start-inventory.md`](spring4-boot4-jump-start-inventory.md) (**WI-103**)
- [x] Migration plan **Phase 1 / current-state / Appendix A** alignment pass (**WI-104**)
- [x] Update deprecated starter names in version catalog (`starter-web` → `starter-webmvc`, OAuth2 renames) — **WI-207**

*(Use `[x]` = satisfied on the Boot 4 migration line after **WI-201**–**WI-209**; story **`spring4-migration-day-2`** closed **2026-04-30**.)*

### Phase 2 -- Core Upgrade

- [x] Bump `springBoot` to `4.0.x` in `libs.versions.toml` — **WI-202** (**4.0.6**)
- [x] **gRPC:** `net.devh` **removed** from main build; data plane on **raw grpc-java** in **`services/mill-data-grpc-service`** (re-validated under Boot 4 — **WI-208**)
- [x] Upgrade Jackson dependencies to 3.0 coordinates (group ID + artifact changes) — **WI-205**
- [x] Upgrade Spring AI to 2.0.x — **WI-203** (**2.0.0-M5**)
- [ ] ~~Upgrade Spring Cloud Function~~ (out of scope -- `misc/cloud/` not in distro)
- [x] Upgrade SpringDoc OpenAPI from 2.x to 3.x (confirmed Boot 4 compatible) — **WI-204** (**3.0.3**)
- [x] Temporarily add `spring-boot-starter-classic` if needed during migration — **not used**; migration completed without classic starters

***Note:** Phase 2 previously mentioned “Spring gRPC 1.0.0” — that was **incorrect** for this repo. The adopted approach is **raw grpc-java**, consistent with [Appendix A](#appendix-a-grpc-migration-to-raw-grpc-java). [Spring gRPC](https://spring.io/blog/2025/11/11/spring-grpc-next-steps) remains an **optional alternative**, not the current implementation.*

### Phase 3 -- Fix Compilation and Tests

**Jump-start:** Pre-upgrade grep inventory (ObjectMapper, PropertyMapper, Boot extension hooks, Security paths) — [`spring4-boot4-jump-start-inventory.md`](spring4-boot4-jump-start-inventory.md) (**WI-103**).

- [x] Fix Jackson package imports (`com.fasterxml.jackson.*` → `tools.jackson.*`) — **WI-205**
- [x] Fix any `ObjectMapper` → `JsonMapper` migration — **WI-205**
- [x] Fix Spring Security 7.0 breaking changes in security configuration classes — **WI-206** (incl. auth/persistence **`testIT`**; **`WebTestClient`** where **`TestRestTemplate`** was removed)
- [x] Re-validate **grpc-java** server wiring (`services/mill-data-grpc-service`: lifecycle, interceptors, **`mill.data.services.grpc.*`**) under Boot 4 — **WI-208**
- [x] Fix any Spring Boot modularization import breakages — addressed during **WI-202** / follow-on compile fixes
- [x] Review `PropertyMapper` usage — **no production usages** in repo (`grep` clean as of migration)
- [x] Run full test suite and fix failures — **`test`** and **`testIT`** green (**WI-209**, CI/CD **2026-04-30**)

### Phase 4 -- Validation

**WI-209 (2026-04-30):** **`./gradlew clean build`**, **`./gradlew test`**, and **`./gradlew testIT`** confirmed green on CI/CD; recorded in [`WI-209-full-repo-green-and-docs.md`](../../workitems/completed/20260430-spring4-migration-day-2/WI-209-full-repo-green-and-docs.md).

- [x] Run `./gradlew clean build` across all modules
- [x] Run `./gradlew test` across all modules
- [x] Run integration tests (`testIT`)
- [ ] Validate gRPC endpoints manually (optional follow-up beyond automated **`testIT`**)
- [ ] Validate OpenAPI documentation endpoints (optional follow-up)
- [ ] Validate AI chat functionality (optional follow-up)
- [ ] Validate OAuth2 security flows (optional follow-up)
- [x] Remove `spring-boot-starter-classic` if it was used temporarily — **N/A** (never added)

---

## Module Impact Matrix

| Module | Impact | Key Issues |
|--------|--------|------------|
| `core/mill-core` | Low | Test dependencies only |
| `core/mill-service-core` | Medium | Security, auto-config, modularization |
| `security/*` (e.g. `mill-service-security`, `mill-security-autoconfigure`) | High | Spring Security 7.0 changes, OAuth2 starter renames |
| `metadata/mill-metadata-core` | Low | Domain resources; autoconfigure lives in **`metadata/mill-metadata-autoconfigure`** |
| `core/mill-test-kit` | Low | Test starter updates |
| `services/mill-data-grpc-service` | **Medium** (was Critical) | Raw **grpc-java** stack — re-validate under Boot 4 / Security 7 |
| `services/mill-data-http-service` | Low | Test dependencies only |
| `data/mill-data-backends` | Low | Starter + config processor |
| `services/mill-service-common` | Medium | Security, web starter rename |
| `metadata/mill-metadata-service` | Low–Medium | Web starter rename; SpringDoc already on catalog |
| `ai/mill-ai-v1-core` | **Critical** | Spring AI 2.0 migration |
| `ai/mill-ai-v1-nlsql-chat-service` | **Critical** | Spring AI 2.0, JPA/Data 2025.1, Security 7.0 |
| `apps/mill-service` | **Critical** | All of the above converge here |
| `services/mill-ui-service` | Low | Web starter rename |
| `clients/mill-jdbc-driver` | Low–Medium | Skymill **`testIT`** uses **`EmbeddedSkymillGrpcServer`** + dynamic host/port (**WI-208**); align with **`services/mill-data-grpc-service`** |
| `misc/cloud/mill-azure-service-function` | N/A (not in distro) | Spring Cloud Function, thin-launcher -- out of migration scope |
| `misc/spring-3/mill-data-grpc-service` | N/A (not in build) | Archival **net.devh** reference only — optional delete |

---

## References

- [spring4-boot4-jump-start-inventory.md](spring4-boot4-jump-start-inventory.md) — grep snapshot before Boot 4 (**WI-103**)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Framework 7.0 Release Notes](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes)
- [Spring Security 7.0 Migration](https://docs.spring.io/spring-security/reference/7.0/migration/)
- [Spring gRPC next steps](https://spring.io/blog/2025/11/11/spring-grpc-next-steps) — **Not used** in this repo; raw **grpc-java** was adopted instead ([Appendix A](#appendix-a-grpc-migration-to-raw-grpc-java)).
- [Spring AI 2.0 M1 Announcement](https://spring.io/blog/2025/12/11/spring-ai-2-0-0-M1-available-now)
- [Jackson 3.0 in Spring Boot 4](https://blog.vvauban.com/blog/spring-boot-4-moves-to-jackson-3-already-in-m3)
- [grpc-java GitHub](https://github.com/grpc/grpc-java)

---

## Appendix A: gRPC Migration to Raw grpc-java

> **Historical / superseded narrative (WI-104):** Production gRPC lives in **`services/mill-data-grpc-service`** on **raw grpc-java**. The **side-by-side `mill-data-grpc-service-v2`** plan and **`data/mill-data-grpc-service`** paths below record the **original migration design** from **`net.devh`**; they are **not** the current module graph. Use this appendix for **rationale and migration history** only.

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

| File | Notes |
|------|--------|
| `clients/mill-jdbc-driver/src/test/java/...` (unit) | Historical **`net.devh`** / in-process patterns were removed with raw **grpc-java**; unit tests use in-process or stubs as appropriate. |
| `clients/mill-jdbc-driver/src/testIT/...` (integration) | Skymill JDBC **`testIT`** spins up **`EmbeddedSkymillGrpcServer`** with an ephemeral port and **`TestITProfile`**-bound **`application-skymill.yml`** / flow resources (**WI-208**). |

#### Configuration Files (historical — see banner above)

| File | Notes (at cutover time) |
|------|-------------------------|
| `services/mill-data-grpc-service/src/testIT/resources/application-test.yml` | Test-only gRPC settings aligned with **`mill.data.services.grpc.*`** |
| `apps/mill-service/src/main/resources/application.yml` | Binds gRPC port via **`mill.data.services.grpc.*`** placeholders |

#### Build Files

| File | Change |
|------|--------|
| `libs.versions.toml` | Remove `bootGRPC` version, `bootGRPC-server`, `bootGRPC-client` entries (**done**); use **`jakarta-annotation-api`** instead of `javax-annotation-api` (**done**, WI-098) |
| `services/mill-data-grpc-service/build.gradle.kts` | Raw grpc-java + **`libs.jakarta.annotation.api`**; no `bootGRPC` (supersedes historical `data/mill-data-grpc-service` row) |
| `clients/mill-jdbc-driver/build.gradle.kts` | Remove `libs.bootGRPC.client` and `libs.bootGRPC.server` from test dependencies; add `libs.grpc.inprocess` |

### Migration Strategy: Side-by-Side Module

The architecture fully supports spinning up a new pure grpc-java module alongside the existing `net.devh`-based module with **no blockers**.

#### Why side-by-side works

1. **`@ConditionalOnService(value = "grpc", group = "data")` isolates the data-plane gRPC stack.** Beans in **`services/mill-data-grpc-service`** use this annotation together with **`mill.data.services.grpc.*`** server properties (`GrpcServerProperties`).

2. **No auto-configuration registration.** The **`services/mill-data-grpc-service`** module has no `spring.factories` or `AutoConfiguration.imports` for the gRPC server; beans are discovered via component scanning.

3. **Clean dependency graph.** The gRPC module depends on **`data:mill-data-backends`** (provides `ServiceHandler`, `DataOperationDispatcher`) and security support modules. No circular dependencies.

4. **`apps/mill-service` consumer.** It includes **`services:mill-data-grpc-service`** as an `implementation` dependency (see `apps/mill-service/build.gradle.kts`).

5. **RPC handler bodies are pure grpc-java.** They use `io.grpc.stub.StreamObserver`, `ServerCallStreamObserver`, and protobuf stubs. The new module can reuse the handler code verbatim.

6. **`clients/mill-jdbc-driver` tests** depend on the gRPC module at test scope only. During transition they can depend on both; once validated, switch to the new one.

#### One thing to fix in the new module

`MillGrpcService` in the current module is annotated `@SpringBootApplication` (making it both a service bean and a component-scan root). The new module must **not** replicate this -- the gRPC service should be a plain `@Component` and let `apps/mill-service` remain the sole `@SpringBootApplication`.

#### Module layout during transition (not implemented as written)

The repo **did not** add a permanent **`mill-data-grpc-service-v2`** sibling under **`data/`**. Instead, **`net.devh`** was removed and **raw grpc-java** landed directly in **`services/mill-data-grpc-service`**. Treat the **v2 / side-by-side** sketch below as **historical** planning text only.

<details>
<summary>Original side-by-side sketch (archival)</summary>

```
data/
  mill-data-grpc-service/        ← existing (net.devh) -- unchanged
  mill-data-grpc-service-v2/     ← new module (pure grpc-java)
```

Cutover steps originally envisioned a second module, dual toggles, and removal of the legacy module — superseded by the direct **`services/mill-data-grpc-service`** implementation.

</details>

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
            @Value("${mill.data.services.grpc.port:9090}") int port,
            @Value("${mill.data.services.grpc.address:*}") String address) {

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
