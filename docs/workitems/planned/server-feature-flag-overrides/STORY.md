# Server Feature Flag Overrides

Build the backend foundation for serving **mill-ui** feature flag overrides from Spring while
preserving the UI as the owner of feature flag names and default values.

The target model is:

```text
effective UI flags = ui/mill-ui defaultFeatureFlags + backend overrides
```

The backend endpoint returns only authoritative overrides for the current deployment and request
context. A missing flag in the response means "use the UI default". A present boolean value always
overrides the UI default.

This is a **foundation story**. It establishes the backend contract, resolver extension model, and
endpoint infrastructure. It does **not** implement concrete capability, policy, profile, tenant, or
user-specific contributors beyond test/demo fixtures required to prove the framework.

## Product / Architecture Intent

- Keep feature flags as client-side UI controls: the UI owns the flag inventory and defaults.
- Let the backend dynamically configure the UI by returning contextual overrides.
- Support future backend inputs:
  - available services/modules/capabilities,
  - deployment/operator configuration,
  - security and authorization policy,
  - user, tenant, or profile decisions once those domains exist.
- Avoid hard-coding all conflict semantics into one global rule by supporting optional per-key
  resolvers with a standard resolver fallback.

## Proposed Foundation

```text
FeatureFlagContributor
  emits FeatureFlagContribution records

FeatureFlagResolver
  optionally resolves contributions for specific keys

DefaultFeatureFlagResolver
  resolves ordinary keys by priority and deterministic conflict policy

FeatureFlagAggregationService
  collects contributions, groups them by key, applies specific resolvers or default resolver,
  and returns final overrides

GET /api/v1/session/features
  returns Partial<FeatureFlags> as JSON
```

The target endpoint is `GET /api/v1/session/features`. The `/api/v1/session/*` namespace is for
effective client-facing state derived for the current request/session context. It may vary by
anonymous/authenticated user, role, tenant, deployment capabilities, or policy. Public deployment
facts may still belong under `/.well-known/mill`, but user-, tenant-, session-, or policy-sensitive
effective flags should not depend on an anonymous-only well-known endpoint.

Future session endpoints may include session-scoped statistics or client-safe capability summaries,
but this story only implements the feature flag override foundation.

Contribution metadata should be rich enough for later maintainers to understand and extend conflict
resolution:

```text
key
value
kind: CAPABILITY | POLICY | OPERATOR | PROFILE | MODULE | TEST
priority
source
reason
```

The wire contract remains intentionally simple:

```json
{
  "viewChat": false,
  "chatSqlExecute": false,
  "headerGlobalSearch": true
}
```

## Non-Goals

- Do not move `defaultFeatureFlags` ownership from `ui/mill-ui` to the backend.
- Do not return a complete feature flag object from the backend.
- Do not implement real capability contributors for AI, export, search, metadata, auth, profiles,
  users, or tenants in this story.
- Do not require the UI to understand backend contribution kinds, priorities, or diagnostics.

## Work Items

- [ ] WI-389 - Feature flag override contract and dependency model (`WI-389-feature-flag-override-contract.md`)
- [ ] WI-390 - Spring feature flag resolver foundation (`WI-390-spring-feature-flag-resolver-foundation.md`)
- [ ] WI-391 - Verification and documentation alignment (`WI-391-feature-flag-foundation-verification-docs.md`)

## References

- [`docs/design/ui/mill-ui/FEATURE-FLAGS.md`](../../../design/ui/mill-ui/FEATURE-FLAGS.md)
- [`docs/design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)
- [`ui/mill-ui/src/features/defaults.ts`](../../../../ui/mill-ui/src/features/defaults.ts)
- [`ui/mill-ui/src/features/FeatureFlagContext.tsx`](../../../../ui/mill-ui/src/features/FeatureFlagContext.tsx)
