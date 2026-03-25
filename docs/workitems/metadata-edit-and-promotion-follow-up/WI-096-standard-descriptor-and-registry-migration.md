# WI-096 — Standard Descriptor and Registry Migration

Status: `planned`
Type: `✨ feature`
Area: `metadata`, `configuration`, `migration`
Backlog refs: `M-10`, `M-20`

---

## Goal

Migrate current built-in ("standard") facet definitions to descriptor-native form and introduce
facet type registry strategy configuration, keeping validation local to each Mill instance.

This WI follows WI-094 and WI-095 and prepares safe rollout for WI-090.

---

## Prerequisites

- WI-094 — Facet Type Descriptor Foundation
- WI-095 — Facet Type Management UI

---

## Scope

### 1) Standard facet descriptor migration

- Represent standard facet types using the same descriptor notation used for custom facet types.
- Keep existing standard facet URNs stable.
- Ensure descriptor-level and per-field metadata completeness:
  - `title`
  - `description`
- Preserve `applicableTo` semantics:
  - empty/omitted => any entity type
  - non-empty => restricted to listed URN targets

### 2) Registry strategy configuration

- Introduce metadata configuration:
  - `mill.metadata.facet-type-registry.type`
- Default to `local`.
- `local` strategy uses local `FacetTypeRepository` (same instance persistence).
- Future `portal` strategy is descriptor source only (registry of facet types), not runtime validator.

### 3) Validation boundary enforcement

- Keep validation execution local regardless of registry type.
- Ensure registry-provided descriptor data is sufficient for local assignment and content validation.
- No remote runtime validation dependency.

### 4) Compatibility and migration handling

- Add migration path for existing seeded/fixture standard facet definitions.
- Ensure all seeded standard descriptors are URN-normalized and unique by `typeKey`.
- Reject duplicate URN definitions with clear conflict errors.

### 5) Runtime and API behavior consistency

- Safe delete rule remains in effect: cannot delete facet type while used by entities.
- Standard/system facet types remain protected by policy defined in WI-094 implementation.
- API and import/export behavior remains deterministic under the new descriptor-native standards.

---

## Out of Scope

- Full portal synchronization workflow and remote release orchestration.
- New advanced schema composition features.
- End-user collaboration workflow changes (promotion/review UX).

---

## Acceptance Criteria

- All standard facet types are defined and loaded via descriptor-native representation.
- Standard facet URNs remain stable and unique.
- Registry strategy config exists with default `local` behavior.
- Validation remains local for all registry strategies.
- Existing tests/fixtures are migrated to descriptor-native standard definitions.
- Duplicate standard facet URNs are rejected during load/import.
- Story docs and design notes reflect the migrated standard descriptor and registry strategy model.

---

## Implementation Notes

- Keep migration explicit and test-backed rather than implicit runtime conversion.
- Prefer deterministic bootstrap order for standard descriptors to avoid non-reproducible conflicts.
- Add integration tests covering:
  - local registry startup
  - descriptor loading for standard facets
  - local validation with standard descriptors
  - conflict handling on duplicate standard URNs

