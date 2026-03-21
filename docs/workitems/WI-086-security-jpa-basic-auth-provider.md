# WI-086 - JPA-backed BasicAuth Provider

Status: `done`
Type: `feature`
Area: `security`
Backlog refs: `SEC-1` (partial)

## Problem Statement

After WI-085 introduces the JPA user identity schema and `UserIdentityResolutionService`, there is no
bridge between the persistent user store and the existing Spring Security basic-auth chain.
`UserRepoAuthenticationProvider` is the correct integration point — it accepts any `UserRepo` —
but `UserRepo` is currently only populated from a YAML file.

## Core Design Constraint — Resolution via `user_identities`

**Local/basic auth must resolve through `user_identities` exactly like OAuth will.**

The `JpaUserRepo` must NOT look up users directly via `user_credentials`. Instead, it must use
`UserIdentityResolutionService.resolve("local", username)` to get the canonical `UserRecord`,
then separately verify the password via `UserCredentialRepository`. This ensures:

- A local user is always represented as `UserIdentityRecord(provider="local", subject=username)`.
- The same `users.id` is used regardless of whether the user later also has an OAuth identity.
- Future OAuth wiring uses the identical `resolve("entra", sub)` path — no special-casing needed.

### Local user identity lifecycle

When a local user is created (admin seeding, future self-registration):
1. `UserRecord` created with stable `userId`.
2. `UserCredentialRecord` created: `passwordHash = passwordHasher.hash(plaintext)`,
   `algorithm = passwordHasher.algorithmId`.
3. `UserIdentityRecord(provider="local", subject=username)` created via `resolveOrProvision()`.
4. `UserProfileRecord` auto-created (empty).

Authentication lookup at login time:
```
UserRepoAuthenticationProvider
  → JpaUserRepo.getUsers()
     → UserIdentityRepository.findByProviderAndSubject("local", username) → UserRecord
     → UserCredentialRepository.findByUserIdAndEnabledTrue(userId) → passwordHash
     → maps to User(name, passwordHash, groups)
```

## Pluggable Password Hashing Design

### Problem

`UserCredentialRecord` must store a password hash plus the algorithm used. When a credential is
created (admin seed, future self-registration), something must produce the `{prefix}encoded` string
stored in `passwordHash`. That **hashing responsibility is separate from verification**.

Verification is already solved: `PasswordEncoderFactories.createDelegatingPasswordEncoder()` reads
the `{prefix}` from the stored value and dispatches to the correct decoder automatically. No changes
needed on the verification side.

### `PasswordHasher` interface (contracts — `mill-security`)

A **pure Kotlin interface**, no Spring/JPA annotations. Lives in `mill-security` alongside
`UserIdentityResolutionService`.

```kotlin
/**
 * Strategy for producing password hashes when creating or updating credentials.
 *
 * The [hash] method returns a Spring-Security-compatible encoded string (e.g. `{noop}password`
 * or `{bcrypt}$2a$10$...`). The prefix is consumed by [PasswordEncoderFactories] on the
 * verification side — no custom verification logic is required.
 *
 * Implementations live in persistence or autoconfigure modules. The active strategy is
 * injected as a `@Bean`; production deployments replace the default by declaring a stronger
 * `PasswordHasher` bean.
 */
interface PasswordHasher {
    /**
     * Short identifier stored in [UserCredentialRecord.algorithm] for audit/rotation purposes.
     * Examples: `"noop"`, `"bcrypt"`, `"argon2"`.
     */
    val algorithmId: String

    /**
     * Produces the encoded credential string to be stored in [UserCredentialRecord.passwordHash].
     *
     * @param plaintext the raw password supplied by the user
     * @return Spring-Security-compatible `{prefix}encoded` string
     */
    fun hash(plaintext: String): String
}
```

### Implementations

| Class | `algorithmId` | Stored format | When to use |
|-------|---------------|---------------|-------------|
| `NoOpPasswordHasher` | `"noop"` | `{noop}password` | **Dev/test only** — plaintext, no real hashing |
| `BCryptPasswordHasher` *(future)* | `"bcrypt"` | `{bcrypt}$2a$10$...` | Production default |
| `Argon2PasswordHasher` *(future)* | `"argon2"` | `{argon2}...` | High-security deployments |

**Initial implementation: `NoOpPasswordHasher`** — ships with WI-086, explicitly annotated as
`@DevOnly` (or documented as dev/test), replaces by declaring any other `PasswordHasher` bean.

### Wiring

`JpaPasswordAuthenticationConfiguration` exposes the default bean:

```kotlin
@Bean
@ConditionalOnMissingBean(PasswordHasher::class)
fun passwordHasher(): PasswordHasher = NoOpPasswordHasher()
```

Production deployments override by declaring a `@Bean` of type `PasswordHasher` (e.g. in an
application configuration class). No changes to `mill-security-persistence` code needed.

### Credential creation flow

```
caller (admin seed / future registration)
  → passwordHasher.hash(plaintext)         → "{noop}password"
  → UserCredentialRecord(
        userId    = ...,
        algorithm = passwordHasher.algorithmId,   // "noop"
        passwordHash = "{noop}password",
        ...
    )
  → UserCredentialRepository.save(record)

authentication verification (unchanged):
  → DelegatingPasswordEncoder.matches(rawPassword, "{noop}password")
  → reads {noop} prefix → dispatches to NoOpPasswordEncoder.matches()
```

---

## Module Placement Decision

`JpaUserRepo` and `JpaPasswordAuthenticationConfiguration` are added to **`security/mill-security-persistence`**
(created in WI-085) — not a separate module. Rationale: both are JPA adapters of contracts defined
in `mill-security`; merging them avoids a redundant module while keeping `mill-service-security`
free of JPA coupling. `mill-security-auth-service` depends only on `mill-security` (contracts),
not on `mill-security-persistence`, so there is no unwanted coupling.

## In Scope

1. **`PasswordHasher` interface** (pure Kotlin, added to `security/mill-security`):
   - Contract only — no Spring/JPA annotations.
   - `val algorithmId: String` + `fun hash(plaintext: String): String`.
   - Full KDoc on interface, property, and method.

2. **`NoOpPasswordHasher`** (Kotlin, `security/mill-security-persistence`):
   - `algorithmId = "noop"`, produces `{noop}password`.
   - **Default autoconfigured hasher** — dev/test only. Replaced in production by declaring
     any other `PasswordHasher` bean.
   - Full KDoc noting dev-only intent.

3. **`JpaUserRepo`** (Kotlin, added to `security/mill-security-persistence`):
   - Implements `UserRepo` interface from `mill-security` (contracts).
   - Does **not** use `getUsers()` bulk scan. Overrides the resolution path by implementing
     `authenticate(username, password)` directly (if `UserRepo` supports it) or returning a
     single-element list from a targeted JPA query.
   - Resolution: `UserIdentityResolutionService.resolve("local", username)` → `UserRecord`.
   - Credential check: `UserCredentialRepository.findByUserIdAndEnabledTrue(userId)`.
   - Groups: `GroupMembershipRepository.findGroupsByUserId(userId)`.
   - Maps to `User(name=username, password=passwordHash, groups)`.
   - Returns empty / null for unknown usernames or users with no enabled `UserCredentialRecord`.
   - Full KDoc on class and all methods.

4. **`JpaPasswordAuthenticationConfiguration`** Spring `@Configuration` (Kotlin):
   - `@ConditionalOnSecurity` + `@ConditionalOnBean(UserIdentityRepository::class)`
   - Provides `PasswordEncoder` bean (`PasswordEncoderFactories.createDelegatingPasswordEncoder()`)
     — handles multi-algorithm verification via `{prefix}` in stored hash.
   - Provides `PasswordHasher` bean (`NoOpPasswordHasher`) with `@ConditionalOnMissingBean` —
     replaced by any stronger `PasswordHasher` bean declared in the application context.
   - Provides `BasicAuthenticationMethod` backed by `UserRepoAuthenticationProvider(jpaUserRepo, passwordEncoder)`.
   - Priority: `AuthenticationType.BASIC` — replaces the file-store method when JPA beans are present.
   - File-store method remains active if JPA beans are absent (graceful fallback).
   - Full KDoc on class and all `@Bean` methods.

5. **Unit tests** (`src/test/`):
   - `JpaUserRepoTest`: mocked `UserIdentityResolutionService`, `UserCredentialRepository`,
     `GroupMembershipRepository` — covers found/not-found, disabled credential, no credential cases.
   - `NoOpPasswordHasherTest`: `hash()` produces `{noop}` prefix; `algorithmId` is `"noop"`.

6. **Integration tests** (`src/testIT/` against H2):
   - Seed: `UserRecord` + `UserCredentialRecord` (`{noop}` hash via `NoOpPasswordHasher`) +
     `UserIdentityRecord("local", "alice")` + group.
   - Assert: `UserRepoAuthenticationProvider.authenticate("alice", correctPassword)` returns token with group authorities.
   - Assert: disabled `UserCredentialRecord` → authentication returns null.
   - Assert: unknown username → null (no exception).
   - Assert: OAuth-only user (no `UserCredentialRecord`) → cannot authenticate via basic auth.
   - Assert: file-store provider still works when JPA beans absent.

## Out of Scope

- OAuth token validation / OIDC filter (future)
- REST login/logout/me endpoints (WI-087)
- User self-registration
- PAT authentication (SEC-2)

## Dependencies

- WI-085 (`security/mill-security-persistence` — entities, repositories, `JpaUserIdentityResolutionService`)
- `security/mill-security` — `UserRepo`, `UserIdentityResolutionService` interfaces
- `security/mill-service-security` — `User`, `UserRepoAuthenticationProvider`, `BasicAuthenticationMethod`, `AuthenticationType`

## Implementation Plan

1. Add `PasswordHasher` interface to `security/mill-security` with full KDoc.
2. Add `NoOpPasswordHasher` to `security/mill-security-persistence` with full KDoc (dev-only note).
3. Add `JpaUserRepo` to `security/mill-security-persistence` with full KDoc.
4. Add `JpaPasswordAuthenticationConfiguration` with `PasswordEncoder`, `PasswordHasher`
   (`@ConditionalOnMissingBean`), and `BasicAuthenticationMethod` beans; full KDoc.
5. Unit tests: `JpaUserRepoTest`, `NoOpPasswordHasherTest`.
6. Integration tests: seed DB using `NoOpPasswordHasher` → authenticate → assert token + authorities; negative cases.

## Acceptance Criteria

- `PasswordHasher` interface is in `mill-security` with no Spring/JPA annotations.
- `NoOpPasswordHasher` is the autoconfigured default (`@ConditionalOnMissingBean`); declaring any
  other `PasswordHasher` bean in the application context replaces it without code changes.
- `UserCredentialRecord.algorithm` stores `passwordHasher.algorithmId`; `passwordHash` stores the
  `{prefix}encoded` string produced by `passwordHasher.hash()`.
- `UserRepoAuthenticationProvider` authenticates a local user whose record exists only in the JPA DB
  (verified via `user_identities(provider="local")`) without any modification to `UserRepoAuthenticationProvider`.
- An OAuth-only user (`UserIdentityRecord` with `provider="entra"`, no `UserCredentialRecord`) cannot
  authenticate via basic auth.
- A user's groups from `group_memberships` appear as `GrantedAuthority` on the returned `Authentication`.
- The file-store basic-auth path continues to work when JPA beans are absent.
- Integration tests pass against H2.

## Deliverables

- This work item definition.
- `PasswordHasher` interface added to `security/mill-security` (pure Kotlin, fully KDoc'd).
- `NoOpPasswordHasher` added to `security/mill-security-persistence` (dev-only default, fully KDoc'd).
- `JpaUserRepo` added to `security/mill-security-persistence` (Kotlin, fully KDoc'd).
- `JpaPasswordAuthenticationConfiguration` added to `security/mill-security-persistence` with
  `PasswordEncoder` + `PasswordHasher` (`@ConditionalOnMissingBean`) + `BasicAuthenticationMethod`
  beans (fully KDoc'd).
- Unit tests (`JpaUserRepoTest`, `NoOpPasswordHasherTest`).
- Integration tests (H2, `testIT` suite).
