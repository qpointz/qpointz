# WI-009: Spring Test Configuration Cleanup

**Type:** refactoring
**Priority:** high
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `refactor/wi-009-spring-test-config-cleanup`

---

## Goal

Eliminate fragile Spring context wiring in tests across the codebase. Tests
currently use broad `@ComponentScan` and unfiltered `@EnableAutoConfiguration`,
which causes cross-module bean collisions. Every new `@Configuration` class
anywhere in the repo risks breaking unrelated tests.

---

## Problem

### Broad `@ComponentScan`

Tests use `@ComponentScan(basePackages = {"io.qpointz"})` or
`@ComponentScan("io.qpointz.mill")`, which scans every `@Component`,
`@Configuration`, and `@Service` on the entire test classpath. Modules that
should be isolated (security, metadata, data, AI) bleed into each other.

**Files using overly broad scans:**

| File | Scan Scope | Module |
|------|-----------|--------|
| `BaseIntegrationTestIT.java` | `io.qpointz` | ai/mill-ai-v1-core |
| `ChatAppScenarioBase.java` | `io.qpointz` | ai/mill-ai-v1-core |
| `SchemaMessageSpecTest.java` | `io.qpointz` + `io.qpointz.mill` | ai/mill-ai-v1-core |
| `NlSqlChatServiceImplTestIT.java` | `io.qpointz` | ai/mill-ai-v1-nlsql-chat-service |
| `NlSqlChatControllerTestIT.java` | `io.qpointz` | ai/mill-ai-v1-nlsql-chat-service |
| `AuthenticationBaseTest.java` | `io.qpointz.mill.security` (fixed) | core/mill-service-security |
| `HttpServiceBasicSecurityTest.java` | `io.qpointz.mill` | core/mill-well-known-service |
| `BaseSecurityTest.java` | `io.qpointz.mill` | core/mill-well-known-service |
| `AccessServiceControllerTest.java` | `io.qpointz.mill` | data/mill-data-http-service |
| `MainLala.java` | `io.qpointz` | core/mill-test-kit (main!) |

### Unfiltered `@EnableAutoConfiguration`

Tests use `@EnableAutoConfiguration` without exclusions, pulling in every
auto-configuration on the classpath — JDBC, JPA, metadata, security — even
when the test doesn't need them. This causes:
- Unnecessary bean creation failures (missing datasource, missing properties)
- Cross-module interference
- Slow test startup

**Files using unfiltered auto-configuration:**

| File | Module |
|------|--------|
| `BaseIntegrationTestIT.java` | ai/mill-ai-v1-core |
| `BaseIntentTestIT.java` | ai/mill-ai-v1-core |
| `ChatAppScenarioBase.java` | ai/mill-ai-v1-core |
| `SchemaMessageSpecTest.java` | ai/mill-ai-v1-core |
| `NlSqlChatServiceImplTestIT.java` | ai/mill-ai-v1-nlsql-chat-service |
| `NlSqlChatControllerTestIT.java` | ai/mill-ai-v1-nlsql-chat-service |
| `HttpServiceBasicSecurityTest.java` | core/mill-well-known-service |
| `HttpServiceNoSecurityTest.java` | core/mill-well-known-service |
| `AccessServiceControllerTest.java` | data/mill-data-http-service |
| `GrpcServiceSecurityConfiguration.java` | data/mill-data-grpc-service (main!) |

### `MainLala.java`

`core/mill-test-kit` contains a `MainLala.java` — a `@SpringBootApplication`
with `@ComponentScan(basePackages = "io.qpointz")`. This appears to be leftover
scaffolding. If any test accidentally picks it up as a configuration class, it
triggers a full scan of everything.

---

## Fix Strategy

### Rule 1: No broad `@ComponentScan` in tests

Replace `@ComponentScan(basePackages = {"io.qpointz"})` with explicit `@Import`
of the specific configurations the test needs:

```java
// Before (fragile)
@ComponentScan(basePackages = {"io.qpointz"})
@EnableAutoConfiguration

// After (explicit)
@Import({
    SecurityConfig.class,
    PasswordAuthenticationConfiguration.class,
    TestController.class
})
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    MetadataCoreConfiguration.class
})
```

### Rule 2: `@EnableAutoConfiguration` with explicit exclusions

Every test that uses `@EnableAutoConfiguration` must exclude auto-configurations
it doesn't need. Alternatively, don't use `@EnableAutoConfiguration` at all —
use `@Import` to pull in only what's needed.

### Rule 3: Delete dead test scaffolding

Remove `MainLala.java` from `mill-test-kit`. It's unused scaffolding that risks
being picked up by tests.

### Rule 4: Reduce cross-module test dependencies

Review test `build.gradle.kts` files. Tests should not depend on modules they
don't test. For example, `mill-service-security` tests should not need
`mill-data-autoconfigure` on the classpath.

---

## Steps

### Phase 1: Core module tests (highest risk)

1. Delete `MainLala.java` from `core/mill-test-kit`
2. Fix `core/mill-well-known-service` tests:
   - `HttpServiceBasicSecurityTest` — narrow `@ComponentScan`, add exclusions
   - `BaseSecurityTest` — narrow `@ComponentScan`, add exclusions
   - `HttpServiceNoSecurityTest` — add `@EnableAutoConfiguration` exclusions
3. Review `core/mill-service-security` test dependencies — remove
   `mill-data-autoconfigure` from test classpath if not needed

### Phase 2: Data service tests

4. Fix `data/mill-data-http-service` test:
   - `AccessServiceControllerTest` — narrow `@ComponentScan`, add exclusions
5. Review `data/mill-data-grpc-service`:
   - `GrpcServiceSecurityConfiguration` has `@EnableAutoConfiguration` in main
     source — this is suspicious, review if it's needed
   - `MillGrpcServiceBaseTest` already has exclusions — verify sufficient

### Phase 3: AI module tests

6. Fix `ai/mill-ai-v1-core` tests:
   - `BaseIntegrationTestIT` — narrow `@ComponentScan` to AI-specific packages
   - `ChatAppScenarioBase` — narrow `@ComponentScan`, add exclusions
   - `SchemaMessageSpecTest` — has TWO `@ComponentScan` annotations (likely a
     bug), fix to use explicit `@Import`
   - `BaseIntentTestIT` — add `@EnableAutoConfiguration` exclusions
7. Fix `ai/mill-ai-v1-nlsql-chat-service` tests:
   - `NlSqlChatServiceImplTestIT` — narrow scan
   - `NlSqlChatControllerTestIT` — narrow scan

### Phase 4: Verify and clean up

8. Run full `./gradlew clean build` to verify no regressions
9. Run `./gradlew clean test` with `--rerun` flag to catch classpath-order issues
10. Remove any remaining broad `@ComponentScan` across the codebase

---

## Out of Scope

- Migrating tests from `@SpringBootTest` to slice tests (`@WebMvcTest`,
  `@DataJpaTest`) — that's a larger effort.
- Migrating from MockMvc to WebTestClient (tracked in P-4).
- Restructuring module dependencies — only test dependency cleanup.

---

## Verification

1. No test class uses `@ComponentScan(basePackages = {"io.qpointz"})`.
2. No test class uses `@ComponentScan("io.qpointz.mill")` without justification.
3. Every `@EnableAutoConfiguration` in test classes has explicit `exclude` list
   or is replaced by `@Import`.
4. `MainLala.java` no longer exists.
5. `./gradlew clean build` passes.
6. Adding a new `@Configuration` class in any module does not break tests in
   other modules.

## Estimated Effort

Medium — ~15 test files to fix across 5 modules. Each fix is mechanical
(narrow scan, add exclusions) but requires understanding what each test actually
needs on its Spring context. AI module is the heaviest.
