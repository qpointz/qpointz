# WI-391 - Feature Flag Foundation Verification and Documentation Alignment

Type: `📝 docs` / `🧪 test`

## Goal

Align documentation and verification around the new backend feature flag override foundation, without
adding concrete real-world contributors.

## Scope

- Update the mill-ui feature flag documentation to use precise terminology:
  - UI feature flags,
  - UI defaults,
  - backend feature flag overrides,
  - effective flags after merge.
- Align endpoint references on `GET /api/v1/session/features`.
- Correct stale statements that imply omitted backend flags default to `true`; omitted flags keep
  `defaultFeatureFlags`.
- Document the backend resolver extension points for future contributors:
  - capability contributors,
  - policy/security contributors,
  - operator/deployment contributors,
  - profile/user/tenant contributors.
- Add developer guidance for adding a future contributor:
  - when to emit no contribution,
  - when to emit `false`,
  - when to emit `true`,
  - when to add a key-specific resolver instead of relying on the default resolver.
- Verify backend and UI docs point to the same endpoint semantics.

## Out of Scope

- Implementing actual contributor beans for specific product capabilities.
- Adding a user-facing feature flag admin UI.
- Changing feature flag names or frontend defaults unless needed to fix a documentation mismatch
  discovered during verification.

## Acceptance Criteria

- `docs/design/ui/mill-ui/FEATURE-FLAGS.md` accurately describes backend responses as partial
  overrides.
- Backend API documentation describes `GET /api/v1/session/features` using the same semantics.
- A design or platform document explains the Spring contributor/resolver extension model.
- Any stale "omitted means true" wording in touched feature-flag docs is removed or corrected.
- Test evidence from WI-390 is summarized in the story or final handoff when the WI is completed.

## Verification

- Documentation review.
- Run relevant documentation/test checks only if the implementation or docs tooling requires them.
