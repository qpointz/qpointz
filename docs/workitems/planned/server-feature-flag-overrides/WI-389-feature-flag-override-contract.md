# WI-389 - Feature Flag Override Contract and Dependency Model

Type: `✨ feature` / `📝 docs`

## Goal

Define the backend contract and dependency-resolution model for **mill-ui** feature flag overrides.
The design must preserve UI-owned defaults while allowing Spring backend modules to contribute
contextual overrides.

## Scope

- Define the semantic contract for `GET /api/v1/session/features`:
  - omitted key means "use UI default",
  - present boolean means "backend override",
  - unknown keys are ignored by the UI,
  - backend response is partial by design.
- Decide and document endpoint placement:
  - effective feature flag overrides live under `/api/v1/session/features`,
  - `/api/v1/session/*` is for effective client-facing state for the current request/session,
  - whether any public bootstrap/deployment facts belong under `/.well-known/mill`,
  - how anonymous startup, login-page flags, and future user-specific flags interact.
- Define backend contribution vocabulary:
  - contribution key,
  - value,
  - contribution kind,
  - priority,
  - source,
  - reason.
- Define conflict-resolution principles:
  - per-key resolver if one exists,
  - standard priority resolver otherwise,
  - deterministic handling of same-priority conflicts,
  - logging/diagnostics expectations.
- Define the dependency model as backend-side override calculation, not as UI-side dependency
  enforcement.
- Update or create a design document under `docs/design/platform/` or `docs/design/ui/mill-ui/`
  capturing these decisions.

## Out of Scope

- Implementing the Spring interfaces or endpoint.
- Implementing concrete capability, policy, profile, user, or tenant contributors.
- Changing `ui/mill-ui` feature flag defaults.
- Changing the frontend merge behavior beyond documenting the intended contract.

## Acceptance Criteria

- A design document clearly states that the UI owns feature flag names and defaults.
- The backend endpoint is specified as returning `Partial<FeatureFlags>` overrides.
- The contract states that backend-provided booleans are authoritative overrides.
- The contribution and resolver model is specified enough for implementation in WI-390.
- Conflict-resolution examples cover at least:
  - missing backend capability forcing a flag off,
  - operator config enabling an opt-in UI feature,
  - user/security policy disabling a feature,
  - same-priority conflict.
- Endpoint placement is documented with a clear rule for public bootstrap facts versus
  session-specific effective overrides.

## Verification

- Documentation review only.
- No runtime tests required for this WI.
