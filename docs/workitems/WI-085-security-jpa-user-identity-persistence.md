# WI-085 - JPA User Identity Schema and Repositories

Status: `done`
Type: `feature`
Area: `security`, `persistence`
Backlog refs: `SEC-1`

## Problem Statement

The current user store is file-based (`passwd.yml` loaded by `UserRepo`). There is no persistent user
identity model, no profile storage, and no path to user self-management or OAuth federation. The design
document at `docs/design/security/auth-profile-pat-architecture.md` defines the canonical multi-table
schema to solve this.

## Core Design Constraint — Universal Identity Resolution

**Every authentication method (basic/local, OAuth, PAT) must resolve to the same canonical `users.id`
through `user_identities`.**

This is the most important structural requirement. The `user_identities` table is the bridge:

| Auth method | `provider` | `subject` | Result |
|-------------|-----------|-----------|--------|
| Local / basic | `"local"` | username | → `users.id` |
| OAuth / OIDC | `"entra"`, `"oidc"` | `<issuer-sub>` | → `users.id` |
| Future PAT | `"pat"` | token id prefix | → `users.id` |

Because all methods resolve through `user_identities`, any downstream feature — user profiles, chat
ownership, PAT issuance, metadata scopes — works identically regardless of how the user authenticated.
An OAuth user can log in for the first time and immediately have access to the same `UserProfileRecord`,
group memberships, and owned resources as a local user.

## Established Autoconfigure Pattern

The existing AI persistence module (ai/mill-ai-v3-persistence`) establishes the pattern to follow:
- Entities live under the `io.qpointz.mill.persistence.*` umbrella (e.g. `io.qpointz.mill.persistence.ai.jpa.*`).
- A companion autoconfigure class uses `EntityScanPackages.register()` + `AutoConfigurationPackages.register()`
  via `ImportBeanDefinitionRegistrar` — it does **not** modify `PersistenceAutoConfiguration`.
- The autoconfigure class is `@ConditionalOnClass` on a class from the persistence module.

This WI follows the same approach.

## In Scope

### 0. Contracts in `security/mill-security` (pre-requisite, part of the module restructuring)

Before any JPA work, the following pure domain types and interfaces are added to `mill-security`
(pure Kotlin — no Spring, no JPA, no framework annotations of any kind). This follows the
**persistence contract purity rule** (see CLAUDE.md): all persistence implementations map their
internal entity/document types to these shared domain types before returning them, enabling
alternative backends (MongoDB, etc.) without touching this layer.

**Domain types** (`io.qpointz.mill.security.domain`):

- **`ResolvedUser`** — `data class(userId: String, displayName: String?, primaryEmail: String?,
  status: UserStatus)` — returned by `UserIdentityResolutionService`. No `@Entity`, no persistence
  annotations.
- **`UserStatus`** — `enum { ACTIVE, DISABLED, LOCKED }`
- **`UserProfile`** — `data class(userId: String, displayName: String?, email: String?,
  locale: String?)` — returned by profile-related operations (used in WI-088).

**Interfaces**:

- **`UserRepo`** — moved/promoted from `mill-service-security` to `mill-security`.
- **`UserIdentityResolutionService`** — `resolve()` returns `ResolvedUser?`;
  `resolveOrProvision()` returns `ResolvedUser`; `loadAuthorities()` returns `List<String>`.
  No `@Transactional` on the interface — that belongs on the implementation only.

### 1. New Gradle module `security/mill-security-persistence` (Kotlin only)

**Entities** (`io.qpointz.mill.persistence.security.jpa.entities`):

- **`UserRecord`** — canonical user; stable `userId` (UUID), `status` (ACTIVE/DISABLED/LOCKED),
  `displayName`, `primaryEmail`, `createdAt`, `updatedAt`. This is the single authoritative row
  regardless of how the user authenticated.

- **`UserCredentialRecord`** — local password hash; `userId` FK to `UserRecord`, `passwordHash`,
  `algorithm` (BCrypt/etc.), `enabled`, `createdAt`, `updatedAt`. Only populated for local/basic users.
  OAuth users have no row here.

- **`UserIdentityRecord`** — **the bridge table**; `provider` (String), `subject` (String),
  `userId` FK to `UserRecord`, optional `claimsSnapshot` (JSON), `createdAt`, `updatedAt`.
  Unique constraint: `(provider, subject)`. Every user must have at least one row here.
  Local users get `provider="local", subject=<username>`. OAuth users get `provider=<issuer>, subject=<sub>`.

- **`GroupRecord`** — `groupId`, `groupName`, `description`.

- **`GroupMembershipRecord`** — `userId` FK, `groupId` FK. Composite PK.

- **`UserProfileRecord`** — `userId` FK (one-to-one), `displayName`, `email`, `theme`
  (`light`/`dark`/`system`), `locale`, `updatedAt`. Created lazily on first access.

**Repositories** (`io.qpointz.mill.persistence.security.jpa.repositories`):

- `UserRepository` — `findByPrimaryEmail`
- `UserCredentialRepository` — `findByUserIdAndEnabledTrue`
- `UserIdentityRepository` — `findByProviderAndSubject` *(the primary resolution query)*
- `GroupRepository`, `GroupMembershipRepository` — `findGroupsByUserId`
- `UserProfileRepository` — `findByUserId`

### 2. `JpaUserIdentityResolutionService` — JPA implementation of `UserIdentityResolutionService`

Implements the `UserIdentityResolutionService` interface defined in `mill-security`. Returns
`ResolvedUser` domain type — **never** `UserRecord` (the JPA entity). Internal mapping:
`UserRecord → ResolvedUser` happens inside this class; callers see only the domain type.

Pure Kotlin, no Spring annotations, constructor-injected only. `@Transactional` on the
implementation methods only. Exposed as a `@Bean` typed to the interface.

```kotlin
/**
 * JPA implementation of [UserIdentityResolutionService].
 *
 * Resolves and provisions user identities across all authentication methods.
 * Internally operates on [UserRecord] JPA entities but maps to [ResolvedUser]
 * domain type before returning — callers never see JPA entity types.
 *
 * @param userRepo repository for canonical user records
 * @param identityRepo repository for provider/subject → userId mappings
 * @param profileRepo repository for user profile records
 */
class JpaUserIdentityResolutionService(
    private val userRepo: UserRepository,
    private val identityRepo: UserIdentityRepository,
    private val profileRepo: UserProfileRepository,
) : UserIdentityResolutionService {

    /**
     * Looks up an existing user by provider/subject pair.
     *
     * @param provider authentication provider identifier (e.g. "local", "entra")
     * @param subject provider-specific user identifier (username, OAuth sub, etc.)
     * @return [ResolvedUser] domain object, or null if no matching identity exists
     */
    override fun resolve(provider: String, subject: String): ResolvedUser?

    /**
     * Returns an existing user or provisions a new one on first login.
     *
     * Creates [UserRecord] + [UserIdentityRecord] + empty [UserProfileRecord] on first call.
     * Idempotent: subsequent calls with the same (provider, subject) return the same userId.
     *
     * @param provider authentication provider identifier
     * @param subject provider-specific user identifier
     * @param displayName optional display name hint (used only when creating a new record)
     * @param email optional email hint (used only when creating a new record)
     * @return [ResolvedUser] domain object
     */
    @Transactional
    override fun resolveOrProvision(
        provider: String,
        subject: String,
        displayName: String? = null,
        email: String? = null,
    ): ResolvedUser

    /**
     * Loads the group names for a user, used to populate [GrantedAuthority].
     *
     * @param userId canonical users.id
     * @return list of group name strings
     */
    override fun loadAuthorities(userId: String): List<String>
}
```

`resolveOrProvision` is the single hook for all auth methods:
- **Local/basic auth (WI-086)**: called with `("local", username)` during user seed / admin creation.
- **OAuth (future)**: called with `("entra", sub)` on every token validation; provisions on first
  login, no-ops on subsequent logins.

`SecurityJpaConfiguration` exposes the implementation as a `@Bean` typed to the interface:
```kotlin
@Bean
fun userIdentityResolutionService(
    userRepo: UserRepository,
    identityRepo: UserIdentityRepository,
    profileRepo: UserProfileRepository,
): UserIdentityResolutionService = JpaUserIdentityResolutionService(userRepo, identityRepo, profileRepo)
```

### 3. Flyway migration V3

File: `persistence/mill-persistence/src/main/resources/db/migration/V3__user_identity.sql`

- Tables: `users`, `user_credentials`, `user_identities`, `groups`, `group_memberships`, `user_profiles`
- Constraints: unique `(provider, subject)` on `user_identities`; FK + index on every `user_id` column
- No seed data (admin bootstrap is out of scope).

### 4. Autoconfiguration registrar

A `SecurityJpaConfiguration` class (either in `persistence/mill-persistence-autoconfigure` or a new
`security/mill-security-autoconfigure` module) following the `MillAiV3JpaConfiguration` pattern:
- `@ConditionalOnClass(name = ["io.qpointz.mill.persistence.security.jpa.entities.UserRecord"])`
- Inner `ImportBeanDefinitionRegistrar`:
  - `EntityScanPackages.register(registry, "io.qpointz.mill.persistence.security.jpa.entities")`
  - `AutoConfigurationPackages.register(registry, "io.qpointz.mill.persistence.security.jpa")`
- Registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

## Out of Scope

- Auth provider wiring and `JpaUserRepo` adapter (WI-086)
- REST endpoints for user/profile management (WI-087, WI-088)
- OAuth token validation / OIDC filter (future SEC-4)
- PAT tables (SEC-2)
- Admin user bootstrap / seeding

## Module Structure

```
security/
  mill-security-persistence/
    build.gradle.kts            ← plugins: kotlin jvm, spring, jpa, dokka; testIT suite
    src/main/kotlin/io/qpointz/mill/persistence/security/jpa/
      entities/
        UserRecord.kt           ← KDoc on class and all fields
        UserCredentialRecord.kt
        UserIdentityRecord.kt
        GroupRecord.kt
        GroupMembershipRecord.kt
        UserProfileRecord.kt
      repositories/
        UserRepository.kt       ← KDoc on interface and query methods
        UserCredentialRepository.kt
        UserIdentityRepository.kt
        GroupRepository.kt
        GroupMembershipRepository.kt
        UserProfileRepository.kt
      service/
        JpaUserIdentityResolutionService.kt   ← implements UserIdentityResolutionService interface from mill-security
    src/test/kotlin/...         ← unit tests for JpaUserIdentityResolutionService (mocked repos)
    src/testIT/kotlin/...       ← H2 round-trip and resolution service integration tests
```

`build.gradle.kts` follows the `mill-ai-v3-persistence` pattern exactly:
- Plugins: `kotlin("jvm")`, `spring.dependency.management`, `kotlin.spring`, `kotlin.jpa`,
  `io.qpointz.plugins.mill`, `org.jetbrains.dokka`
- Dependencies: `security/mill-security` (contracts), `persistence/mill-persistence`,
  `boot.starter.data.jpa`, `kotlin("reflect")`, `h2.database` (runtimeOnly)
- `testing { suites { register<JvmTestSuite>("testIT") { ... } } }` — see CLAUDE.md Testing Structure

## Dependencies

- `security/mill-security` — `UserRepo` and `UserIdentityResolutionService` interfaces (pre-requisite: module restructuring)
- `persistence/mill-persistence` — JPA infrastructure, Flyway
- `persistence/mill-persistence-autoconfigure` — autoconfiguration conventions
- [`docs/design/security/auth-profile-pat-architecture.md`](../design/security/auth-profile-pat-architecture.md)

## Implementation Plan

1. Add `security/mill-security-persistence` module to `settings.gradle.kts`.
2. Write `build.gradle.kts` following `mill-ai-v3-persistence` pattern (plugins, dependencies, testIT suite).
3. Write `V3__user_identity.sql` (all six tables, constraints, indexes).
4. Implement Kotlin JPA entity data classes with full KDoc on class, fields, and constructors.
5. Implement Spring Data JPA repositories with KDoc on interface and every query method.
6. Implement `UserIdentityResolutionService` with full KDoc on class, all methods, all parameters.
7. Add `SecurityJpaConfiguration` autoconfigure registrar (exposes `UserIdentityResolutionService` as `@Bean`).
8. Unit tests (`src/test/`): `UserIdentityResolutionServiceTest` with mocked repositories —
   `resolve()` found/not-found cases, `resolveOrProvision()` create-vs-return logic.
9. Integration tests (`src/testIT/`):
   - H2 save/load round-trip for all six entity types.
   - `resolveOrProvision("local", "alice")` creates user + identity + profile (first call).
   - `resolveOrProvision("local", "alice")` returns same `userId` (idempotent, second call).
   - `resolveOrProvision("entra", "sub-123", "Alice", "a@b.com")` provisions OAuth user.
   - Same OAuth subject provisioned twice returns same `userId`.
   - `loadAuthorities(userId)` returns group names from `group_memberships`.

## Acceptance Criteria

- `V3__user_identity.sql` applies cleanly on top of existing V1 + V2 migrations.
- All six entity types can be persisted and retrieved in H2 testIT.
- `UserIdentityResolutionService.resolveOrProvision()` is idempotent: calling it twice with the
  same `(provider, subject)` returns the same `userId` both times.
- An OAuth-provisioned user (no `UserCredentialRecord`) has the same `UserProfileRecord` access
  path as a local user.
- Autoconfiguration picks up entities without changes to `PersistenceAutoConfiguration`.

## Deliverables

- This work item definition.
- `ResolvedUser`, `UserStatus`, `UserProfile` domain types added to `security/mill-security`.
- `UserIdentityResolutionService` interface (returning domain types) added to `security/mill-security`.
- `security/mill-security-persistence` Gradle module with `build.gradle.kts`.
- `V3__user_identity.sql` Flyway migration.
- JPA entity classes with full KDoc.
- Spring Data JPA repositories with KDoc.
- `JpaUserIdentityResolutionService` (maps `UserRecord → ResolvedUser`; exposed as `@Bean` typed to interface), fully KDoc'd.
- `SecurityJpaConfiguration` autoconfiguration registrar.
- Unit tests (`JpaUserIdentityResolutionServiceTest`).
- Integration tests (H2, `testIT` suite).
