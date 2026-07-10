# WI-390 - Spring Feature Flag Resolver Foundation

Type: `✨ feature` / `🧪 test`

## Goal

Implement the Spring foundation that collects feature flag override contributions, resolves
conflicts, and serves the final partial override map to **mill-ui**.

This WI delivers the extension framework only. It must not implement real domain contributors for
AI, export, search, metadata, auth, profile, user, or tenant decisions.

## Scope

- Add backend contracts for feature flag override resolution:
  - `FeatureFlagContributor`,
  - `FeatureFlagContribution`,
  - `FeatureFlagContributionKind`,
  - `FeatureFlagResolver`,
  - `FeatureFlagResolutionContext` if needed,
  - `FeatureFlagAggregationService`.
- Add a default resolver:
  - highest priority wins,
  - same-priority same-value contributions resolve normally,
  - same-priority conflicting contributions resolve deterministically,
  - conflicts are logged with key/source/reason details.
- Add support for optional specific resolvers:
  - if any resolver supports a key, it handles that key,
  - otherwise the default resolver handles it.
- Add `GET /api/v1/session/features` returning the resolved partial override map.
- Add focused tests using test-only contributors/resolvers to prove:
  - empty contributions produce `{}`,
  - single contribution appears in response,
  - highest priority wins,
  - same-priority conflict is deterministic,
  - key-specific resolver overrides default resolver behavior,
  - omitted flags are not serialized.

## Out of Scope

- Real service/capability detection contributors.
- Real security/user/profile/tenant contributors.
- Frontend implementation changes beyond what is required to keep the existing client compatible.
- A diagnostics endpoint or exposing contribution metadata to normal UI users.

## Acceptance Criteria

- The backend exposes `GET /api/v1/session/features`.
- The endpoint returns a JSON object of only resolved overrides.
- The service works when there are no contributor beans.
- Multiple contributors for the same key are resolved deterministically.
- Specific resolvers can be registered by Spring and selected by key.
- Tests cover resolver behavior without relying on real domain contributors.

## Verification

- Run the relevant Gradle test task for the module where the feature flag endpoint/foundation lands.
- If the implementation is placed in a shared/autoconfigure module, run both the module test and any
  app/service slice test needed to prove endpoint wiring.
