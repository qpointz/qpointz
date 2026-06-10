# User Identity and JPA Persistence — Implementation Design

**Status:** Implemented (phases 1 and 2 of `auth-profile-pat-architecture.md`)
**Date:** March 2026
**Modules:** `mill-security`, `mill-security-persistence`, `mill-security-auth-service`, `mill-security-autoconfigure`, `mill-persistence`

---

## 1. Overview

This document describes the implemented design for user identity persistence, authentication,
profile management, and audit trail. It covers the full vertical slice from database schema
through JPA entities, repository and service layers, to the auth REST API.

The design follows the contract purity rule established in `auth-profile-pat-architecture.md`:
interfaces and domain types in `mill-security` carry no persistence annotations; JPA entities
live exclusively in `mill-security-persistence` and are mapped to domain types before being
returned to any caller outside the persistence module.

---

## 2. Module Structure

```
security/
├── mill-security                    # Contract layer — pure domain types, no framework deps
│   ├── domain/
│   │   ├── ResolvedUser             # Canonical user value object returned by all auth paths
│   │   ├── UserStatus               # Enum: ACTIVE | DISABLED | LOCKED
│   │   ├── UserIdentityResolutionService  # Interface: resolve / resolveOrProvision / loadAuthorities
│   │   └── PasswordHasher           # Interface for credential hashing
│   └── audit/
│       ├── AuthAuditService         # Interface: record(type, subject, userId, ip, ua, reason)
│       └── AuthEventType            # Enum: LOGIN_SUCCESS | LOGIN_FAILURE | LOGOUT | ...
│
├── mill-security-persistence        # JPA implementation of the contract layer
│   ├── entities/                    # @Entity classes (never exposed outside this module)
│   │   ├── UserRecord
│   │   ├── UserIdentityRecord
│   │   ├── UserCredentialRecord
│   │   ├── UserProfileRecord
│   │   └── AuthEventRecord
│   ├── repositories/                # Spring Data JPA interfaces
│   │   ├── UserRepository
│   │   ├── UserIdentityRepository
│   │   ├── UserCredentialRepository
│   │   ├── UserProfileRepository
│   │   ├── GroupMembershipRepository
│   │   └── AuthEventRepository
│   ├── service/
│   │   └── JpaUserIdentityResolutionService  # Implements UserIdentityResolutionService
│   ├── auth/
│   │   └── JpaUserRepo              # Feeds UserRepoAuthenticationProvider
│   ├── audit/
│   │   └── JpaAuthAuditService      # Implements AuthAuditService
│   └── configuration/
│       ├── SecurityJpaConfiguration          # @AutoConfiguration entry point
│       └── JpaPasswordAuthenticationConfiguration  # Wires JPA basic auth
│
├── mill-security-auth-service       # Auth REST API (login, register, me, profile, logout)
│   ├── controllers/
│   │   ├── AuthController           # GET /auth/me, PATCH /auth/profile, POST /auth/logout
│   │   └── AuthPublicController     # POST /auth/public/login, POST /auth/public/register
│   ├── service/
│   │   └── UserProfileService       # Profile get-or-create and partial-update logic
│   ├── dto/                         # AuthMeResponse, UserProfileResponse, UserProfilePatch, ...
│   └── configuration/
│       └── AuthServiceConfiguration # @AutoConfiguration entry point for auth service beans
│
└── mill-security-autoconfigure      # Security filter chain wiring (extracted from mill-service-security)
    └── configuration/
        ├── SecurityConfig           # Central @EnableWebSecurity, AuthenticationManager bean
        ├── ApiSecurityConfiguration
        ├── AppSecurityConfiguration
        ├── AuthRoutesSecurityConfiguration
        ├── PolicyAuthorizationConfiguration
        └── ...
```

---

## 3. Database Schema

All schema lives in a single Flyway migration (`V3__user_identity.sql` in `mill-persistence`).

### 3.1 `users` — canonical user record

```sql
CREATE TABLE users (
    user_id       VARCHAR(255)  PRIMARY KEY,   -- stable UUID, never reused
    status        VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | DISABLED | LOCKED
    display_name  VARCHAR(512),
    primary_email VARCHAR(512),
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL,
    validated     BOOLEAN       NOT NULL DEFAULT TRUE,  -- email confirmed
    locked        BOOLEAN       NOT NULL DEFAULT FALSE, -- admin lock
    lock_date     TIMESTAMP,                            -- when locked was set TRUE
    lock_reason   VARCHAR(1024)                         -- free-text lock reason
);
```

`validated` and `locked` are login guards (see §6.3). `status` reflects the coarser lifecycle
state (`ACTIVE` / `DISABLED` / `LOCKED`). A row in `users` is the single authoritative identity
record regardless of authentication method.

### 3.2 `user_credentials` — local password credentials

```sql
CREATE TABLE user_credentials (
    credential_id VARCHAR(255)  PRIMARY KEY,
    user_id       VARCHAR(255)  NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    password_hash VARCHAR(1024) NOT NULL,   -- {prefix}hash format for DelegatingPasswordEncoder
    algorithm     VARCHAR(64)   NOT NULL,   -- e.g. "noop", "bcrypt"
    enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL
);
```

Only local/basic-auth users have rows here. OAuth users authenticate through `user_identities`
alone and have no credential row. A user can have their credential disabled (`enabled=FALSE`)
independently of the account `locked` flag.

### 3.3 `user_identities` — identity bridge (OAuth-ready)

```sql
CREATE TABLE user_identities (
    identity_id   VARCHAR(255) PRIMARY KEY,
    provider      VARCHAR(128) NOT NULL,   -- "local" | "entra" | "google" | ...
    subject       VARCHAR(512) NOT NULL,   -- username (local) | OAuth sub claim
    user_id       VARCHAR(255) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    claims_snapshot TEXT,                  -- optional JSON of OAuth claims at last login
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uq_user_identities_provider_subject UNIQUE (provider, subject)
);
```

This is the primary lookup table for all authentication paths. The unique constraint on
`(provider, subject)` prevents duplicate provisioning. The same user can have multiple rows
(e.g. one `provider=local` and one `provider=entra`) all pointing to the same `user_id`.

### 3.4 `groups` and `group_memberships`

```sql
CREATE TABLE groups (
    group_id    VARCHAR(255) PRIMARY KEY,
    group_name  VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    CONSTRAINT uq_groups_group_name UNIQUE (group_name)
);

CREATE TABLE group_memberships (
    user_id  VARCHAR(255) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    group_id VARCHAR(255) NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);
```

Group names become `GrantedAuthority` values in Spring Security and feed the existing
policy engine without any changes to the policy layer.

### 3.5 `user_profiles` — editable user preferences

```sql
CREATE TABLE user_profiles (
    user_id      VARCHAR(255) PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    display_name VARCHAR(512),
    email        VARCHAR(512),
    theme        VARCHAR(32),
    locale       VARCHAR(64),
    updated_at   TIMESTAMP    NOT NULL
);
```

Created lazily on first `GET /auth/me` or `PATCH /auth/profile`. Intentionally separate from
`users` so that profile edits do not touch the identity record. Theme preference is present
in the schema but not wired to the UI at this stage.

### 3.6 `auth_events` — audit trail

```sql
CREATE TABLE auth_events (
    event_id       VARCHAR(255)  PRIMARY KEY,   -- UUID assigned at write time
    event_type     VARCHAR(64)   NOT NULL,       -- LOGIN_SUCCESS | LOGIN_FAILURE | ...
    user_id        VARCHAR(255),                 -- nullable: unknown on failed logins
    subject        VARCHAR(512),                 -- username/email as presented
    ip_address     VARCHAR(64),
    user_agent     VARCHAR(1024),
    failure_reason VARCHAR(255),                 -- BAD_CREDENTIALS | DUPLICATE_EMAIL | ...
    occurred_at    TIMESTAMP     NOT NULL
);
```

`user_id` has no foreign key intentionally — failed logins against unknown subjects must be
recorded without a matching `users` row. The table is append-only by convention; no rows
are ever updated or deleted.

---

## 4. Domain Contract Layer (`mill-security`)

### 4.1 `ResolvedUser`

The single value object returned by all authentication resolution paths:

```kotlin
data class ResolvedUser(
    val userId: String,        // stable UUID, FK to users.user_id
    val displayName: String?,
    val primaryEmail: String?,
    val status: UserStatus,    // ACTIVE | DISABLED | LOCKED
)
```

Callers outside `mill-security-persistence` only ever see `ResolvedUser` — never a JPA entity.

### 4.2 `UserIdentityResolutionService`

```kotlin
interface UserIdentityResolutionService {
    fun resolve(provider: String, subject: String): ResolvedUser?
    fun resolveOrProvision(provider, subject, displayName?, email?): ResolvedUser
    fun loadAuthorities(userId: String): List<String>
}
```

- **`resolve`** — used by auth controllers after a session is established, to get the canonical
  `userId` from the authenticated principal.
- **`resolveOrProvision`** — used during registration to create a new user row plus identity
  mapping on first login. Idempotent: repeated calls for the same `(provider, subject)` return
  the same `userId`.
- **`loadAuthorities`** — returns group name strings for population of `GrantedAuthority`.

### 4.3 `AuthAuditService`

```kotlin
interface AuthAuditService {
    fun record(type: AuthEventType, subject: String?, userId: String?,
               ipAddress: String?, userAgent: String?, failureReason: String? = null)
}
```

Always optional-injected (`null` when no persistence module is present). Controllers call it
after every security-relevant action regardless of success or failure.

---

## 5. JPA Entities (`mill-security-persistence`)

All entities are `class` (not `data class`) to satisfy JPA proxy requirements. None are
exposed to callers outside `mill-security-persistence`; the service layer maps them to
domain types before returning.

| Entity | Table | Key fields |
|--------|-------|-----------|
| `UserRecord` | `users` | `userId` (PK), `status`, `validated`, `locked`, `lockDate`, `lockReason` |
| `UserIdentityRecord` | `user_identities` | `identityId` (PK), `provider`, `subject`, `userId` (FK) |
| `UserCredentialRecord` | `user_credentials` | `credentialId` (PK), `userId` (FK), `passwordHash`, `enabled` |
| `UserProfileRecord` | `user_profiles` | `userId` (PK+FK), `displayName`, `email`, `locale`, `theme` |
| `AuthEventRecord` | `auth_events` | `eventId` (PK), `eventType`, `userId` (no FK), all fields `val` |

`AuthEventRecord` is fully immutable (all fields are `val`) because audit rows must never
be modified. All other entities use `var` for mutable fields and `val` for immutable ones.

---

## 6. JPA Service Layer

### 6.1 `JpaUserIdentityResolutionService`

Implements `UserIdentityResolutionService`. `@Transactional` is on the mutating methods
only (`resolveOrProvision`), not on the interface.

**`resolve(provider, subject)`**

```
user_identities.findByProviderAndSubject(provider, subject)
  → UserIdentityRecord
    → users.findById(identity.userId)
      → UserRecord.toResolvedUser()  [maps to domain type]
```

**`resolveOrProvision(provider, subject, displayName, email)`**

Checks for an existing identity first. If absent:
1. Saves a new `UserRecord` (status=`ACTIVE`, `validated=TRUE`, `locked=FALSE`).
2. Saves a new `UserIdentityRecord` linking `(provider, subject)` to the new `userId`.
3. Saves an empty `UserProfileRecord` (all nullable fields null).
4. Returns `UserRecord.toResolvedUser()`.

The entire operation runs in a single transaction; partial failure rolls back all three
inserts.

**`loadAuthorities(userId)`**

```
group_memberships.findGroupsByUserId(userId)  → List<GroupRecord>
  → map { it.groupName }                      → List<String>
```

### 6.2 `JpaUserRepo` — password authentication feed

Used exclusively by `UserRepoAuthenticationProvider` (basic/local auth). `getUsers()` returns
only users that are currently eligible to authenticate:

```
user_identities.findByProvider("local")
  → for each identity:
      users.findById(identity.userId)            → skip if absent
      if user.locked || !user.validated          → skip
      user_credentials.findByUserIdAndEnabledTrue(identity.userId)  → skip if absent
      group_memberships.findGroupsByUserId(identity.userId)
      → User(name=identity.subject, password=credential.passwordHash, groups=...)
```

The four rejection conditions in order:
1. No `UserRecord` found for the identity (data integrity issue).
2. `UserRecord.validated == false` — email not confirmed.
3. `UserRecord.locked == true` — admin lock applied.
4. No enabled `UserCredentialRecord` — credential disabled or missing.

### 6.3 Account Login Gates

Two independent boolean flags on `UserRecord` act as login gates, checked in `JpaUserRepo`:

| Flag | Default | Meaning | Effect on login |
|------|---------|---------|-----------------|
| `validated` | `TRUE` | Email address confirmed | `false` → rejected |
| `locked` | `FALSE` | Administratively locked | `true` → rejected |
| `lockDate` | `NULL` | When lock was applied | Informational only |
| `lockReason` | `NULL` | Free-text lock reason | Informational only |

These flags are orthogonal to `status`. Current defaults (`validated=TRUE`, `locked=FALSE`)
mean all existing and newly registered users can log in immediately. Email verification
(flip `validated` to `FALSE` on registration and `TRUE` on confirmation) is a future
extension that requires no schema change.

### 6.4 `JpaAuthAuditService`

Implements `AuthAuditService`. Assigns a fresh `UUID.randomUUID()` as `eventId` and
calls `repository.save()` synchronously. No retry, no batching. Write failures are not
propagated — the calling controller's audit write is fire-and-forget in terms of the
HTTP response.

---

## 7. Auth REST API (`mill-security-auth-service`)

### 7.1 Endpoints

| Method | Path | Controller | Auth required |
|--------|------|-----------|---------------|
| `POST` | `/auth/public/login` | `AuthPublicController` | No |
| `POST` | `/auth/public/register` | `AuthPublicController` | No |
| `GET` | `/auth/me` | `AuthController` | Yes |
| `PATCH` | `/auth/profile` | `AuthController` | Yes |
| `POST` | `/auth/logout` | `AuthController` | No (session invalidated) |

### 7.2 Login flow (`POST /auth/public/login`)

```
LoginRequest{username, password}
  → AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
      → JpaUserRepo.getUsers()  [applies validated/locked/credential filters]
      → UserRepoAuthenticationProvider verifies password hash
  → on success:
      SecurityContext saved to HttpSession (JSESSIONID cookie set)
      identityResolutionService.resolve("local", username)  → ResolvedUser
      return AuthMeResponse{userId, email, displayName, groups, securityEnabled=true}
  → on BadCredentialsException:
      authAuditService.record(LOGIN_FAILURE, ..., failureReason="BAD_CREDENTIALS")
      return 401 ErrorResponse
```

When `mill.security.enable=false`, authentication is skipped and an anonymous
`AuthMeResponse{userId="anonymous", securityEnabled=false}` is returned immediately.

### 7.3 Registration flow (`POST /auth/public/register`)

Prerequisites checked in order:
1. `mill.security.allow-registration=true` — else `403`.
2. Email format valid (regex `^[^@\s]+@[^@\s]+\.[^@\s]+$`) — else `422`.
3. Password not blank — else `422`.
4. No existing `user_identities(provider="local", subject=email)` — else `409`.

On success:
1. `resolveOrProvision("local", email, displayName, email)` — creates user + identity + profile.
2. `UserCredentialRecord` saved with `hasher.hash(password)` and `algorithm=hasher.algorithmId`.
3. Session established via `AuthenticationManager` (best-effort; account is created regardless).
4. Returns `201 AuthMeResponse`.

### 7.4 `GET /auth/me`

Extracts `(provider, subject)` from the `Authentication` principal:
- `UsernamePasswordAuthenticationToken` → `provider="local"`, `subject=authentication.name`
- `OAuth2AuthenticationToken` → `provider=registrationId`, `subject=principal.name`
- null / `anonymousUser` → `401`

Then:
```
identityResolutionService.resolve(provider, subject)  → ResolvedUser
userProfileService?.getOrCreate(resolved.userId)      → UserProfileRecord (if service present)
return AuthMeResponse{userId, email, displayName, groups, profile}
```

### 7.5 `PATCH /auth/profile`

Accepts `UserProfilePatch{displayName?, email?, locale?}`. Null fields are ignored (partial
update semantics). Calls `userProfileService.update(userId, patch)` which uses `getOrCreate`
internally so a missing profile row is never an error.

### 7.6 Provider/subject extraction summary

```
Authentication type              provider          subject
──────────────────────────────────────────────────────────
UsernamePasswordAuthenticationToken  "local"       authentication.name
OAuth2AuthenticationToken            registrationId  principal.name
```

---

## 8. `UserProfileService`

```kotlin
class UserProfileService(private val userProfileRepository: UserProfileRepository) {
    fun getOrCreate(userId: String): UserProfileRecord
    fun update(userId: String, patch: UserProfilePatch): UserProfileRecord
}
```

`getOrCreate` performs a read-then-write: `findByUserId(userId) ?: save(emptyRecord)`.
`update` calls `getOrCreate` first (ensuring a row exists), then applies non-null patch
fields. `updatedAt` is always refreshed even if no fields changed.

---

## 9. Audit Trail

Every security-relevant operation in both controllers calls `authAuditService?.record(...)`.
The `?.` operator means the call is silently skipped when no persistence module is present.

| Operation | EventType | failureReason values |
|-----------|-----------|---------------------|
| Successful login | `LOGIN_SUCCESS` | — |
| Failed login (bad password) | `LOGIN_FAILURE` | `BAD_CREDENTIALS` |
| Failed login (other) | `LOGIN_FAILURE` | `AUTH_ERROR` |
| Logout | `LOGOUT` | — |
| Successful registration | `REGISTER_SUCCESS` | — |
| Registration: disabled | `REGISTER_FAILURE` | `REGISTRATION_DISABLED` |
| Registration: bad email | `REGISTER_FAILURE` | `VALIDATION_ERROR` |
| Registration: blank password | `REGISTER_FAILURE` | `VALIDATION_ERROR` |
| Registration: duplicate email | `REGISTER_FAILURE` | `DUPLICATE_EMAIL` |
| Profile updated | `PROFILE_UPDATE` | — |

IP is extracted from `X-Forwarded-For` (first value before `,`) with fallback to
`request.remoteAddr`. User-Agent is read verbatim from the request header.

---

## 10. Autoconfiguration and Bean Wiring

### 10.1 `SecurityJpaConfiguration` (`mill-security-persistence`)

`@AutoConfiguration`, `@ConditionalOnClass(UserRecord::class)`. Activates only when
`mill-security-persistence` is on the classpath.

- Registers `@EntityScan` for `io.qpointz.mill.persistence.security.jpa.entities`.
- Registers `@EnableJpaRepositories` for the repositories package.
- Exposes `authAuditService(repo: AuthEventRepository): AuthAuditService`.
- Exposes `userIdentityResolutionService(...): UserIdentityResolutionService` backed by `JpaUserIdentityResolutionService`.
- `@Import`s `JpaPasswordAuthenticationConfiguration`.

### 10.2 `JpaPasswordAuthenticationConfiguration`

`@ConditionalOnSecurity` + `@ConditionalOnClass(UserIdentityRepository::class)`.

> **Why `@ConditionalOnClass` not `@ConditionalOnBean`:** JPA repository beans are registered
> lazily after `@EnableJpaRepositories` processing, which occurs after `@Import` evaluation.
> `@ConditionalOnBean` would silently fail (no bean found yet), resulting in an empty
> `ProviderManager` and a runtime `IllegalArgumentException`. `@ConditionalOnClass` checks
> the classpath at parse time and is always reliable here.

Exposes:
- `jpaPasswordEncoder(): PasswordEncoder` (delegating, multi-algorithm).
- `passwordHasher(): PasswordHasher` (`@ConditionalOnMissingBean` — `NoOpPasswordHasher` by default, replace in production).
- `jpaBasicAuthMethod(...): AuthenticationMethod` at priority 299 — just below the file-store BASIC default of 300, ensuring JPA wins when both are present.

### 10.3 `AuthServiceConfiguration` (`mill-security-auth-service`)

`@AutoConfiguration`. Exposes:
- `userProfileService(repo?: UserProfileRepository): UserProfileService?` — returns `null` when `UserProfileRepository` is absent (no persistence module).
- `authPublicController(...)`: all constructor parameters `@Autowired(required=false)`.
- `authController(...)`: all constructor parameters `@Autowired(required=false)`.

Both controllers accept a `null` `UserIdentityResolutionService`, `AuthAuditService`, and
`UserProfileService` — they degrade gracefully when security is disabled or the persistence
module is absent.

---

## 11. Security OFF Mode

When `mill.security.enable=false`:

- `AuthPublicController.login` → returns anonymous `AuthMeResponse{userId="anonymous", securityEnabled=false}` without touching `AuthenticationManager`.
- `AuthPublicController.register` → always returns `403`.
- `AuthController.getMe` → returns anonymous `AuthMeResponse`.
- `AuthController.updateProfile` → returns `401`.
- `AuthController.logout` → always returns `200` (session invalidated if present).

This behaviour is deterministic and does not depend on which other beans happen to be present.

---

## 12. Default Seed Data

`V3__user_identity.sql` inserts a default admin user for development:

| Column | Value |
|--------|-------|
| `user_id` | `00000000-0000-0000-0000-000000000001` |
| `status` | `ACTIVE` |
| `validated` | `TRUE` |
| `locked` | `FALSE` |
| `display_name` | `Administrator` |
| `primary_email` | `admin@mill.local` |
| credential `password_hash` | `{noop}admin` |
| identity `provider/subject` | `local` / `admin` |
| group | `admins` |

The `{noop}` prefix is interpreted by `DelegatingPasswordEncoder` as a no-op (plaintext
comparison). Replace the hash in production or declare a `PasswordHasher` bean to use bcrypt.

---

## 13. Feature Flags (UI)

Social login buttons in `mill-ui` are gated by feature flags in `defaults.ts`:

| Flag | Default | Controls |
|------|---------|---------|
| `loginPassword` | `true` | Username/password form |
| `loginRegistration` | `true` | "Sign up" link on login page |
| `loginGithub` | `false` | GitHub OAuth button |
| `loginGoogle` | `false` | Google OAuth button |
| `loginMicrosoft` | `false` | Microsoft OAuth button |
| `loginAws` | `false` | AWS OAuth button |
| `loginAzure` | `false` | Azure AD OAuth button |

Social provider flags default to `false` because OAuth provider registration (client ID/secret)
must be configured explicitly per environment before the buttons are useful.

---

## 14. Open Items and Future Extensions

- **Email verification**: flip `validated=FALSE` on registration; send confirmation link;
  flip to `TRUE` on confirmation. Schema already supports this.
- **PAT (Personal Access Tokens)**: described in `auth-profile-pat-architecture.md` phase 3.
  Requires `personal_access_tokens` table and a new `AuthenticationMethod` provider.
- **OAuth federation**: `user_identities` already supports multiple providers per user.
  Adding OAuth login requires an `OAuth2UserService` that calls `resolveOrProvision`.
- **Account lock API**: admin endpoint to set `locked=TRUE` with `lockDate` and `lockReason`.
- **Brute-force protection**: increment a failure counter; auto-lock after N failures.
- **Password rotation**: disable old `UserCredentialRecord`, insert new one; history in a
  separate `credential_history` table if required.
