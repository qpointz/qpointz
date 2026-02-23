# WI-002: Split Security into Service Authentication and Data Authorization

**Type:** refactoring
**Priority:** high
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `refactor/wi-002-security-module-split`

---

## Goal

Separate security into two concerns:

1. **Service security (authentication)** — Spring-dependent, stays in `core/mill-security-autoconfigure`
   (rename to `core/mill-service-security`). Handles HTTP filter chains, OAuth2, Basic auth,
   Entra ID, Spring Security wiring.

2. **Data authorization (policies)** — Pure Java, no Spring dependency. Policy evaluation,
   actions, selectors, expression filters. Either a new `data/mill-data-security` module or
   consolidated into `data/mill-data-backend-core`.

The two modules intersect only through a thin API: the data authorization layer needs
a principal name and a set of granted scopes/roles. This is provided via a pure-Java
interface (`SecurityProvider`) that the Spring authentication module implements externally.

## Related Backlog Items

- P-15: Resolve duplicate mill.security.authorization.policy prefix across modules
- R-2: Remove Spring contamination from mill-metadata-core
- R-3: Remove Spring contamination from mill-data-backends

## Current State

### `core/mill-security` (pure Java — already Spring-free)

Package: `io.qpointz.mill.security`

Dependencies: Jackson, Lombok only — **no Spring**.

Contains:
- `authorization/policy/Action` — action interface
- `authorization/policy/ActionVerb` — ALLOW/DENY enum
- `authorization/policy/PolicyAction` — policy + verb + action tuple
- `authorization/policy/PolicyRepository` — stores policy actions
- `authorization/policy/PolicySelector` — selects policies by context (e.g. granted authorities)
- `authorization/policy/PolicyEvaluator` / `PolicyEvaluatorImpl` — evaluates policies against subjects
- `authorization/policy/PolicyActionDescriptor` — descriptor for deserialization
- `authorization/policy/repositories/InMemoryPolicyRepository`, `PolicyActionDescriptorRepository`
- `authorization/policy/actions/ExpressionFilterAction`, `TableReadAction`
- `authentication/AuthenticationContext`, `AuthenticationMethodDescriptor`, `AuthenticationType`

### `core/mill-security-autoconfigure` (Spring-dependent)

Package: `io.qpointz.mill.security`

Dependencies: Spring Boot, Spring Security, OAuth2, OkHttp.

Contains:
- `authentication/*` — Basic auth, OAuth2, Entra ID, Bearer token readers, methods, providers
- `configuration/*` — SecurityConfig, ApiSecurityConfiguration, AppSecurityConfiguration,
  ServicesSecurityConfiguration, AuthRoutesSecurityConfiguration, WellKnownSecurityConfiguration,
  SwaggerSecurityConfig, PolicyActionsConfiguration
- `annotations/*` — ConditionalOnSecurity, OnSecurityEnabledCondition (note: these are in
  `core/mill-service-api` now)

### Data backend security classes (scattered)

- `data/mill-data-backend-core`:
  - `SecurityProvider` — pure interface: `getPrincipalName()`, `authorities()`
  - `NoneSecurityProvider` — no-op impl
  - `dispatchers/SecurityDispatcher` — wraps SecurityProvider
  - `dispatchers/SecurityDispatcherImpl` — delegates to SecurityProvider
  - `rewriters/TableFacet*` — uses PolicyEvaluator to build plan rewrite facets
  - `security/authorization/policy/GrantedAuthoritiesPolicySelector` — uses SecurityDispatcher

- `data/mill-data-autoconfigure`:
  - `SecurityContextSecurityProvider` — Spring Security impl of SecurityProvider
  - `configuration/PolicyConfiguration` — Spring `@ConfigurationProperties` for policy config
  - `configuration/DefaultFilterChainConfiguration` — Spring wiring of PolicyRepository,
    PolicyEvaluator, PolicySelector, TableFacetFactory, PlanRewriter

## Target State

### Module 1: `core/mill-service-security` (renamed from `core/mill-security-autoconfigure`)

**Spring-dependent.** Authentication only.

Contains:
- All authentication classes (Basic, OAuth2, Entra ID, Bearer)
- All HTTP security filter chains (Api, App, Services, Auth routes, Swagger, WellKnown)
- `SecurityConfig` (Spring Security master config)
- `SecurityContextSecurityProvider` (moved from `data/mill-data-autoconfigure`) —
  implements `SecurityProvider` by reading from Spring's `SecurityContextHolder`
- `PolicyActionsConfiguration` — Spring config properties for `mill.security.authorization.policy`
- `PolicyConfiguration` — Spring config properties for selector/remap

### Module 2: `core/mill-security` (stays as-is, pure Java)

**No Spring dependency.** Data authorization core.

Already pure. Contains policy model, evaluator, repository, actions.

**Note:** `GrantedAuthoritiesPolicySelector` stays in `data/mill-data-backend-core` — it
cannot move here because it depends on `SecurityDispatcher` (from backend-core), which
would create a circular dependency (`core/mill-security` <-> `data/mill-data-backend-core`).
This is correct: `GrantedAuthoritiesPolicySelector` is glue code that bridges the
`SecurityDispatcher` with the `PolicySelector` interface.

### Module 3: `data/mill-data-backend-core` (keep pure)

**No Spring dependency.**

Keeps:
- `SecurityProvider` interface — the thin API bridge
- `NoneSecurityProvider` — default no-op
- `SecurityDispatcher` / `SecurityDispatcherImpl` — wraps SecurityProvider
- `GrantedAuthoritiesPolicySelector` — bridges SecurityDispatcher to PolicySelector
- `rewriters/TableFacet*` — plan rewriting using PolicyEvaluator

### Module 4: `data/mill-data-autoconfigure` (Spring wiring)

Keeps:
- `DefaultFilterChainConfiguration` — wires Spring beans for policy evaluation
- `DefaultServiceConfiguration` — wires ServiceHandler, dispatchers

Loses:
- `SecurityContextSecurityProvider` — moves to `core/mill-service-security`
- `PolicyConfiguration` — moves to `core/mill-service-security`

## Key Design Decisions

### The bridge: `SecurityProvider` interface

```
core/mill-service-security (Spring)
    |
    |  implements SecurityProvider
    |  (SecurityContextSecurityProvider reads from SecurityContextHolder)
    |
    v
data/mill-data-backend-core (pure Java)
    SecurityProvider interface
        getPrincipalName() -> String
        authorities() -> Collection<String>
    |
    v
    SecurityDispatcher -> PolicySelector -> PolicyEvaluator -> TableFacetFactory
```

The data authorization layer never imports Spring. It receives principal/roles through
`SecurityProvider`, which is injected at wiring time by the autoconfigure module.

### `GrantedAuthoritiesPolicySelector` location

Stays in `data/mill-data-backend-core`. It depends on `SecurityDispatcher` (also in
backend-core), so moving it to `core/mill-security` would create a circular dependency:

```
core/mill-security -> data/mill-data-backend-core  (for SecurityDispatcher)
data/mill-data-backend-core -> core/mill-security   (for PolicySelector, PolicyEvaluator)
```

This is the correct layering: `core/mill-security` defines the authorization abstractions,
`data/mill-data-backend-core` provides the concrete wiring between the security provider
and the policy selector.

### `SecurityProvider` vs `SecurityDispatcher` duplication

These two interfaces are nearly identical:

- `SecurityProvider`: `getPrincipalName()`, `authorities()`
- `SecurityDispatcher`: `principalName()`, `authorities()`

`SecurityDispatcherImpl` is just a null-safe wrapper around `SecurityProvider`.
Consider consolidating: remove `SecurityDispatcher`, use `SecurityProvider` directly
everywhere in backend-core. The null-safety can move to the factory/wiring layer.

## Steps

1. Rename `core/mill-security-autoconfigure` to `core/mill-service-security`.
2. Move `SecurityContextSecurityProvider` from `data/mill-data-autoconfigure` to
   `core/mill-service-security`.
3. Move `PolicyConfiguration` from `data/mill-data-autoconfigure` to
   `core/mill-service-security`.
4. Consider consolidating `SecurityDispatcher` into `SecurityProvider` in
   `data/mill-data-backend-core` (remove the unnecessary wrapper).
5. Update all Gradle `project()` references to the renamed module.
6. Update all imports across affected modules.
7. Verify no Spring imports exist in `core/mill-security` or `data/mill-data-backend-core`.

## Verification

1. `core/mill-security` must have **zero** Spring dependencies in `build.gradle.kts`.
2. `data/mill-data-backend-core` must have **zero** Spring dependencies.
3. `./gradlew build` succeeds from `core/` and `data/` roots.
4. `./gradlew test` passes in all affected modules.
5. `rg 'org\.springframework' core/mill-security/src/` returns zero hits.
6. `rg 'org\.springframework' data/mill-data-backend-core/src/main/` returns zero hits.

## Estimated Effort

Medium — module rename, a few class moves, import updates, Gradle wiring.
