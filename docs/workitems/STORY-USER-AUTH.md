# Story: User Authentication and Profile Management

## Summary

Enable real user authentication in Mill. Currently all security is mock — `mill-ui` starts with
`isAuthenticated=true`, the login page is a no-op, and user data is hardcoded. The YAML-file user
store has no path to OAuth federation or user self-management.

This story delivers a persistent JPA user identity model, a real login/logout/session API,
Spring Security chain integration with clean public/secured path separation, and a fully wired
`mill-ui` with login, profile editing, and optional self-registration. All authentication methods
(local password, OAuth) resolve to the same canonical `users.id` through a `user_identities`
bridge table — ensuring OAuth and local users share the same profile, groups, and owned resources.

## Module Layout

All security modules live under `security/`. Dependency flow:

```
mill-security (contracts)
    ├── mill-service-security       (implements auth method providers)
    ├── mill-security-persistence   (implements JPA adapters)
    ├── mill-security-autoconfigure (wires everything via Spring)
    └── mill-security-auth-service  (REST layer — depends on contracts only)
```

| Module | Role | Spring? | JPA? |
|--------|------|---------|------|
| `security/mill-security` | Pure contracts: interfaces (`UserRepo`, `UserIdentityResolutionService`), DTOs, policy model | No | No |
| `security/mill-service-security` | Auth method implementations: basic, OAuth, Entra providers | Yes | No |
| `security/mill-security-autoconfigure` | Spring wiring: `SecurityFilterChain` configs, `AuthenticationManager`, `@ConditionalOnSecurity` | Yes | No |
| `security/mill-security-persistence` | JPA adapter: entities, repositories, `JpaUserRepo`, `JpaUserIdentityResolutionService`, `PasswordEncoder` bean | Yes | Yes |
| `security/mill-security-auth-service` | Auth REST endpoints | Yes | No |

> **Pre-requisite refactoring — ✅ done:**
> 1. ~~`git mv core/mill-security security/mill-security`~~ — done
> 2. ~~`git mv services/mill-service-security security/mill-service-security`~~ — done
> 3. `security/mill-security-autoconfigure` — not extracted yet; `SecurityFilterChain` configs
>    remain in `mill-service-security` for now (deferred — no blocking dependency).
> 4. ~~Add `UserRepo` and `UserIdentityResolutionService` interfaces to `mill-security`~~ — done
> 5. ~~Update `settings.gradle.kts` references~~ — done

## Work Items (Execution Order)

- [x] 1. [WI-085](WI-085-security-jpa-user-identity-persistence.md) — JPA User Identity Schema and Repositories (`security`, `persistence`)
- [x] 2. [WI-086](WI-086-security-jpa-basic-auth-provider.md) — JPA-backed BasicAuth Provider (`security`)
- [x] 3. [WI-087](WI-087-mill-security-auth-service.md) — mill-security-auth-service Backend (`security`)
- [x] 4. [WI-090](WI-090-mill-ui-login-integration.md) — mill-ui Login Integration (`ui`)
- [ ] 5. [WI-088](WI-088-mill-ui-user-profile.md) — mill-ui User Profile UI (`security`, `ui`)
- [ ] 6. [WI-089](WI-089-user-registration.md) — User Registration (`security`, `ui`)

> WI-088 and WI-089 are independent of each other and can be parallelized once WI-090 is complete.

## Scope Boundaries

**In scope:**
- Persistent user identity model (`users`, `user_identities`, `user_credentials`, `groups`,
  `group_memberships`, `user_profiles`)
- Local/password authentication via JPA with BCrypt
- `POST /auth/public/login`, `POST /auth/logout`, `GET /auth/me` REST endpoints
- Spring Security chain with clean path separation (`/auth/public/**` always-public,
  `/auth/me` + `/auth/logout` + `/auth/profile` dual-bean security-on/off)
- `mill-ui` real `AuthContext`, loading state, `RequireAuth` route guard
- User profile read/update (`GET /auth/me` with profile, `PATCH /auth/profile`)
- Optional self-registration (`POST /auth/public/register`, gated by `mill.security.allow-registration`)
- Security-off mode: all auth endpoints return anonymous user; login page never shown

**Out of scope:**
- OAuth / SSO provider wiring (SEC-4)
- Personal Access Tokens (SEC-2)
- "Forgot password?" flow (future WI)
- Admin user management (future WI)
- PAT management UI (SEC-2)
