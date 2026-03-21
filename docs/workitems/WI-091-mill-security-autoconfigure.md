# WI-091 — Extract `mill-security-autoconfigure` from `mill-service-security`

| Field  | Value                              |
|--------|------------------------------------|
| Status | done                               |
| Type   | refactor                           |
| Area   | security                           |

## Problem

`mill-service-security` mixes two distinct concerns:

1. **Authentication domain** — authentication method abstractions (`AuthenticationMethod`,
   `AuthenticationMethods`, `UserRepoAuthenticationProvider`, `BasicAuthenticationMethod`,
   `OAuth2ResourceServiceAuthenticationMethod`, `EntraIdAuthenticationMethod`, etc.) and the
   reader/provider chain.
2. **Spring wiring** — `@Configuration` classes that wire Spring Security's
   `SecurityFilterChain` beans, `AuthenticationManager`, and `@ConditionalOnSecurity`-gated
   policy configurations.

Because both concerns live in the same module there is no Spring Boot autoconfigure entry-point,
forcing downstream modules (e.g. `apps/mill-service`) to rely on component-scan instead of
autoconfiguration. Additionally:

- `@EnableWebSecurity` is scattered across eight configuration classes (only one should carry it).
- `AuthRoutesSecurityConfiguration`, `ApiSecurityConfiguration`, `AppSecurityConfiguration`,
  `ServicesSecurityConfiguration`, `WellKnownSecurityConfiguration`, and `SwaggerSecurityConfig`
  all carry a `@ConditionalOnMissingBean(name = "functionContextFlag")` hack that is no longer
  relevant.
- `PolicyConfiguration` (`@Component`) and `PolicyActionsConfiguration` (`@Configuration`) bind
  the same `mill.security.authorization.policy` prefix across two separate classes, causing
  potential binding conflicts and making the API harder to follow.

## Goal

Create `security/mill-security-autoconfigure` — a dedicated Java module that:

- Owns all Spring Security wiring `@Configuration` classes.
- Exposes an `AutoConfiguration.imports` file so that Spring Boot auto-discovers the security
  wiring without requiring component-scan.
- Eliminates the `@EnableWebSecurity` scatter and the `functionContextFlag` hack.
- Merges `PolicyConfiguration` and `PolicyActionsConfiguration` into a single
  `PolicyAuthorizationConfiguration` class.

`mill-service-security` retains the authentication domain types and stays as the library
dependency; `mill-security-autoconfigure` adds the wiring on top.

## In Scope

- Create `security/mill-security-autoconfigure` Gradle module (Java).
- Move all `@Configuration` wiring classes from `mill-service-security` to the new module
  (same packages).
- Create `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Move `additional-spring-configuration-metadata.json` to the new module.
- Remove `@EnableWebSecurity` from all classes except `SecurityConfig`.
- Remove `@ConditionalOnMissingBean(name = "functionContextFlag")` from all classes.
- Merge `PolicyConfiguration` + `PolicyActionsConfiguration` into
  `PolicyAuthorizationConfiguration`.
- Update `apps/mill-service/build.gradle.kts` to depend on the new autoconfigure module.
- Add `:security:mill-security-autoconfigure` to `settings.gradle.kts`.

## Out of Scope

- Changes to `mill-security-auth-service` or `mill-security-persistence` module structure.
- Changes to authentication method implementations inside `mill-service-security`.
- Changing public API of any moved class (same package, same class name where applicable).
- Migrating `mill-data-autoconfigure` or other autoconfigure modules.

## Implementation Plan

1. Create work item document (this file).
2. Create `security/mill-security-autoconfigure/build.gradle.kts`.
3. Add `include(":security:mill-security-autoconfigure")` to `settings.gradle.kts`.
4. `git mv` all configuration classes from `mill-service-security` to new module (same packages).
5. Fix Issue A — remove `@EnableWebSecurity` from all classes except `SecurityConfig`.
6. Fix Issue B — remove `@ConditionalOnMissingBean(name = "functionContextFlag")` from all classes.
7. Fix Issue C — merge `PolicyConfiguration` + `PolicyActionsConfiguration` into
   `PolicyAuthorizationConfiguration`; delete old files.
8. Create `AutoConfiguration.imports` in `mill-security-autoconfigure`.
9. Move `additional-spring-configuration-metadata.json` to `mill-security-autoconfigure`.
10. Update `apps/mill-service/build.gradle.kts`.
11. Build and verify all affected modules compile cleanly.

## Acceptance Criteria

- `./gradlew :security:mill-security-autoconfigure:build` passes.
- `./gradlew :security:mill-service-security:build` passes (no configuration classes remain).
- `./gradlew :apps:mill-service:build` passes.
- No `@EnableWebSecurity` outside `SecurityConfig.java`.
- No `@ConditionalOnMissingBean(name = "functionContextFlag")` anywhere in the codebase.
- `PolicyConfiguration.java` and `PolicyActionsConfiguration.java` are deleted; replaced by
  `PolicyAuthorizationConfiguration.java`.
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` exists in
  `mill-security-autoconfigure`.
- `additional-spring-configuration-metadata.json` lives in `mill-security-autoconfigure`.
